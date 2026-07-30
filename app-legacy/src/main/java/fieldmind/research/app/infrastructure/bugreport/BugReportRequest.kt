package fieldmind.research.app.infrastructure.bugreport

import com.google.gson.annotations.SerializedName

/**
 * Top-level POST body sent to the Cloudflare Worker (orskipped entirely
 * when the URL is empty — see [BugReporter.send]).
 *
 * NOTE: Extra fields ([appVersion], [installMethod], etc.) MUST be embedded in
 * the Markdown [body], not passed as separate top-level keys. The Worker
 * accepts only these three top-level keys and drops extras silently.
 */
data class BugReportRequest(
    val title: String,
    val body: String,
    val labels: List<String> = listOf("bug")
)

/**
 * Server shape returned by the Cloudflare Worker. Always HTTP 200 from the
 * Worker's PoV — the [ok] field tells the client whether the issue was created.
 */
data class BugReportResponse(
    @SerializedName("ok") val ok: Boolean = false,
    @SerializedName("issueNumber") val issueNumber: Int? = null,
    @SerializedName("issueUrl") val issueUrl: String? = null,
    @SerializedName("error") val error: String? = null,
    @SerializedName("status") val status: Int? = null,
    @SerializedName("message") val message: String? = null
)

/**
 * Result of [BugReporter.send]. Either:
 *  - The Worker accepted the report and returned issue metadata → [Success].
 *  - The Worker rejected it (auth, validation, GitHub-API error) → [SoftFail].
 *  - The Worker was unreachable OR `BUG_REPORTER_URL` is empty AND we fell
 *    back to opening the GitHub web-URL fallback → [WebUrl].
 *  - No fallback was possible (offline, nothing to send) → [HardFail].
 */
sealed class BugReportResult {
    data class Success(val issueNumber: Int, val issueUrl: String) : BugReportResult()
    data class SoftFail(val message: String, val upstreamStatus: Int? = null) : BugReportResult()
    data class WebUrl(val url: String, val title: String, val body: String) : BugReportResult()
    data class HardFail(val reason: String) : BugReportResult()
}

/**
 * GitHub repository constants used by both the Worker URL and the web-URL
 * fallback. Kept in lock-step with the deployment README.
 */
object BugReportRepo {
    const val OWNER = "firefly-sylestia"
    const val NAME = "FieldMinds"
    const val ISSUES_PATH = "https://github.com/$OWNER/$NAME/issues"
}
