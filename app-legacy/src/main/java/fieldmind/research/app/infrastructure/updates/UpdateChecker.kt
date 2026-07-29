package fieldmind.research.app.infrastructure.updates

import android.util.Log
import com.google.gson.Gson
import fieldmind.research.app.BuildConfig
import fieldmind.research.app.shared.data.model.AppSettings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * Lifecycle states surfaced by [UpdateChecker.check].
 */
sealed interface UpdateInfo {
    /** Service was constructed but no check has run yet. */
    data object Idle : UpdateInfo

    /** A check is currently in-flight. */
    data object Loading : UpdateInfo

    /** A newer release is available; the relevant download URL is included. */
    data class UpdateAvailable(
        val tag: String,
        val versionName: String,
        val releaseUrl: String,
        val notes: String,
        val publishedAt: String
    ) : UpdateInfo

    /** Latest release matches (or is older than) the running build. */
    data object UpToDate : UpdateInfo

    /** Service can't reach a determination (network, no releases, user disabled toggle). */
    data class Unavailable(val reason: String) : UpdateInfo

    /** Fetch threw an unexpected error. The previous successful state is preserved. */
    data class Errored(val message: String) : UpdateInfo
}

/**
 * Coroutine-driven update-checker that hits GitHub's public `/releases/latest`
 * endpoint, throttles repeat checks to once every 24 hours, and caches the
 * most recent decision so the `UpdateBannerOverlay` can render it instantly.
 *
 * Designed to be a singleton held by `MainActivity` (or the ViewModel):
 * repeated calls into [check] are inexpensive when within the throttle window.
 */
class UpdateChecker(
    private val appSettings: AppSettings,
    private val repoOwner: String = "firefly-sylestia",
    private val repoName: String = "FieldMinds",
    private val client: HttpClient = HttpClient.Default,
    private val io: CoroutineDispatcher = Dispatchers.IO
) {

    /** Latest result of [check]. Starts as [UpdateInfo.Idle]. */
    private val _updateInfo = MutableStateFlow<UpdateInfo>(UpdateInfo.Idle)
    val updateInfo: StateFlow<UpdateInfo> = _updateInfo.asStateFlow()

    /** Build-time-configurable URL; defaults to the firefly-sylestia/FieldMinds repo. */
    private val apiUrl: String =
        "https://api.github.com/repos/$repoOwner/$repoName/releases/latest"

    /**
     * Run an update check.
     *
     * @param force when true, ignores the 24-hour throttle and re-fetches.
     * @param currentVersionName defaults to [BuildConfig.VERSION_NAME] (e.g. "0.45.0-gh").
     * @return the [UpdateInfo] produced by this run. Also cached for [updateInfo].
     */
    suspend fun check(
        force: Boolean = false,
        currentVersionName: String = BuildConfig.VERSION_NAME
    ): UpdateInfo {
        if (!appSettings.updateCheckEnabled.value) {
            return UpdateInfo.Unavailable("Update check is disabled in Settings")
        }
        val now = System.currentTimeMillis()
        val lastCheck = appSettings.updateLastCheckTime.value
        val cachedTag = appSettings.updateLatestTag.value
        if (!force && cachedTag.isNotBlank() && now - lastCheck < THROTTLE_MS) {
            // Replay the cached decision without hitting the network.
            val cached = if (isNewer(cachedTag, currentVersionName)) {
                UpdateInfo.UpdateAvailable(
                    tag = cachedTag,
                    versionName = appSettings.updateLatestVersionName.value,
                    releaseUrl = appSettings.updateLatestVersionUrl.value,
                    notes = appSettings.updateLatestReleaseNotes.value,
                    publishedAt = appSettings.updateLatestPublishedAt.value
                )
            } else {
                UpdateInfo.UpToDate
            }
            _updateInfo.value = cached
            return cached
        }

        _updateInfo.value = UpdateInfo.Loading
        val result = runCatching {
            withContext(io) {
                val raw = client.get(apiUrl)
                val release = Gson().fromJson(raw, GitHubRelease::class.java)
                appSettings.setUpdateLastCheckTime(now)
                if (release.tagName.isBlank()) {
                    UpdateInfo.Unavailable("No releases published yet")
                } else {
                    appSettings.setUpdateLatestTag(release.tagName)
                    appSettings.setUpdateLatestVersionName(release.name.ifBlank { release.tagName })
                    appSettings.setUpdateLatestVersionUrl(release.htmlUrl)
                    appSettings.setUpdateLatestReleaseNotes(release.body)
                    appSettings.setUpdateLatestPublishedAt(release.publishedAt)
                    if (isNewer(release.tagName, currentVersionName)) {
                        UpdateInfo.UpdateAvailable(
                            tag = release.tagName,
                            versionName = release.name.ifBlank { release.tagName },
                            releaseUrl = release.htmlUrl,
                            notes = release.body,
                            publishedAt = release.publishedAt
                        )
                    } else {
                        UpdateInfo.UpToDate
                    }
                }
            }
        }.getOrElse { error ->
            Log.w(TAG, "Update check failed", error)
            UpdateInfo.Errored(error.message ?: error::class.java.simpleName)
        }
        _updateInfo.value = result
        return result
    }

    companion object {
        private const val TAG = "UpdateChecker"
        /** 24 hours — users get a fresh reminder once per day at most. */
        private const val THROTTLE_MS: Long = 24L * 60L * 60L * 1000L

        /**
         * Compare remote tag (e.g. "v0.46.0") to local version (e.g. "0.45.0-gh").
         * Returns true iff remote is strictly newer.
         * Best-effort integer compare; falls back to lexicographic for non-numeric tags.
         */
        internal fun isNewer(remoteTag: String, localVersion: String): Boolean {
            val remoteClean = stripSuffix(remoteTag.removePrefix("v").trim())
            val localClean = stripSuffix(localVersion.trim())
            val rParts = remoteClean.split(".").mapNotNull { it.toIntOrNull() }
            val lParts = localClean.split(".").mapNotNull { it.toIntOrNull() }
            if (rParts.isEmpty() || lParts.isEmpty()) {
                return remoteTag.trim().removePrefix("v") > localVersion.trim()
            }
            val len = maxOf(rParts.size, lParts.size)
            for (i in 0 until len) {
                val r = rParts.getOrElse(i) { 0 }
                val l = lParts.getOrElse(i) { 0 }
                if (r > l) return true
                if (r < l) return false
            }
            return false
        }

        private fun stripSuffix(s: String): String {
            val dash = s.indexOf('-')
            return if (dash > 0) s.substring(0, dash) else s
        }
    }
}

/** Minimal injectable HTTP client — defaults to [HttpURLConnection]. */
interface HttpClient {
    /** Synchronous GET. Throws on non-2xx. */
    fun get(url: String, timeoutMs: Int = 10_000): String

    object Default : HttpClient {
        override fun get(url: String, timeoutMs: Int): String {
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = timeoutMs
                readTimeout = timeoutMs
                setRequestProperty("accept", "application/vnd.github+json")
                setRequestProperty("user-agent", "fieldmind-android-update-checker")
                setRequestProperty("x-github-api-version", "2022-11-28")
            }
            val code = conn.responseCode
            if (code !in 200..299) error("HTTP $code from $url")
            return conn.inputStream.bufferedReader().use { it.readText() }
        }
    }
}
