package fieldmind.research.app.infrastructure.bugreport

import android.util.Log
import com.google.gson.Gson
import fieldmind.research.app.BuildConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * Sends a [BugReportRequest] to the Cloudflare Worker configured via
 * `BuildConfig.BUG_REPORTER_URL`. If that URL is empty OR the Worker can't
 * be reached, builds a GitHub-web-URL fallback that the caller can open with
 * `Intent.ACTION_VIEW` (the user signs in with their own GitHub account and
 * submits the pre-filled issue from their browser — zero token / zero infra).
 *
 * Pure Kotlin — no third-party HTTP dependency. Drop-in for ViewModels or
 * coroutine scopes.
 */
class BugReportReporter(
    private val repo: BugReportRepo = BugReportRepo,
    private val io: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * Try the Worker first; on any unreachable / non-2xx / invalid-json
     * condition, fall back to building a [BugReportResult.WebUrl].
     */
    suspend fun send(req: BugReportRequest): BugReportResult {
        val url = BuildConfig.BUG_REPORTER_URL.trim()
        if (url.isEmpty()) {
            Log.i(TAG, "BUG_REPORTER_URL is empty; falling back to web URL.")
            return webUrlFallback(req)
        }
        return withContext(io) {
            runCatching {
                val raw = HttpClientForReporter.postJson(
                    url = url,
                    body = Gson().toJson(req),
                    timeoutMs = 12_000
                )
                parseWorkerResponse(raw)
            }.getOrElse { error ->
                Log.w(TAG, "Worker POST failed; offering web URL fallback", error)
                webUrlFallback(req, error.message ?: error::class.java.simpleName)
            }
        }
    }

    /**
     * Build the GitHub web-URL the user should be handed when no Worker
     * is reachable. Title and body are URL-encoded.
     */
    fun webUrlFallback(req: BugReportRequest, unusedReason: String? = null): BugReportResult.WebUrl {
        val encodedTitle = URLEncoderCompat.encode(req.title)
        val encodedBody = URLEncoderCompat.encode(req.body)
        val template = "bug-report.yml"
        val url = "${repo.ISSUES_PATH}/new?template=$template&title=$encodedTitle&body=$encodedBody"
        return BugReportResult.WebUrl(url, req.title, req.body)
    }

    private fun parseWorkerResponse(raw: String): BugReportResult {
        val parsed = runCatching { Gson().fromJson(raw, BugReportResponse::class.java) }.getOrNull()
            ?: return BugReportResult.SoftFail("Worker returned non-JSON response")
        return if (parsed.ok && parsed.issueNumber != null && !parsed.issueUrl.isNullOrBlank()) {
            BugReportResult.Success(parsed.issueNumber, parsed.issueUrl)
        } else if (parsed.error != null) {
            BugReportResult.SoftFail(parsed.message ?: parsed.error, upstreamStatus = parsed.status)
        } else {
            BugReportResult.SoftFail("Unknown Worker response shape", upstreamStatus = parsed.status)
        }
    }

    companion object {
        private const val TAG = "BugReportReporter"
    }
}

/**
 * Wraps java.net.URLEncoder without forcing callers to add an import.
 * URLEncoderCompat handles `+` (` `) and `%20` consistently.
 */
internal object URLEncoderCompat {
    fun encode(s: String): String = java.net.URLEncoder.encode(s, "UTF-8")
}

/** Default [HttpClient] implementation for this reporter (POST with JSON body). */
internal object HttpClientForReporter : HttpClient {
    override fun get(url: String, timeoutMs: Int): String =
        throw UnsupportedOperationException("BugReporter only uses POST")

    fun postJson(url: String, body: String, timeoutMs: Int = 12_000): String {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = timeoutMs
            readTimeout = timeoutMs
            doOutput = true
            doInput = true
            useCaches = false
            setRequestProperty("content-type", "application/json; charset=utf-8")
            setRequestProperty("accept", "application/json")
            setRequestProperty("user-agent", "fieldmind-android-bug-reporter")
        }
        try {
            conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
        } catch (e: Throwable) {
            // Fall through to error response below
            runCatching { conn.disconnect() }
            throw e
        }
        val code = conn.responseCode
        if (code !in 200..299) {
            // Worker always returns 200 with {ok, error} OR a plain error.
            // A non-2xx means the URL itself rejected us (e.g. 5xx from another service).
            throw IllegalStateException("HTTP $code from $url")
        }
        val stream = runCatching { conn.inputStream }.getOrElse { conn.errorStream } ?: error("Empty response from $url")
        return stream.bufferedReader().use { it.readText() }
    }
}
