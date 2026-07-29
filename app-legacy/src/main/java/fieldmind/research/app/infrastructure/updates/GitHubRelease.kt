package fieldmind.research.app.infrastructure.updates

import com.google.gson.annotations.SerializedName

/**
 * Subset of the GitHub `/releases/latest` JSON we actually use.
 * All fields default to safe placeholders so a partial response
 * (or an unexpected additional field) can't crash the parser.
 *
 * Example JSON shape:
 * {
 *   "tag_name": "v0.46.0",
 *   "name": "v0.46.0",
 *   "html_url": "https://github.com/firefly-sylestia/FieldMinds/releases/tag/v0.46.0",
 *   "body": "## Highlights\n- ...",
 *   "published_at": "2026-07-10T12:34:56Z",
 *   "prerelease": false,
 *   "draft": false
 * }
 */
data class GitHubRelease(
    @SerializedName("tag_name") val tagName: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("html_url") val htmlUrl: String = "",
    @SerializedName("body") val body: String = "",
    @SerializedName("published_at") val publishedAt: String = "",
    @SerializedName("prerelease") val prerelease: Boolean = false,
    @SerializedName("draft") val draft: Boolean = false
)
