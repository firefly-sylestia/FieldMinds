package fieldmind.research.app.infrastructure.bugreport

/**
 * Client-side pre-strip of the same PII patterns the Cloudflare Worker also
 * strips. The Worker is **still** the authoritative sanitizer (defense in
 * depth) — this object exists so the in-app "preview" of the report can show
 * users what they're about to send without leaking obvious secrets into the
 * preview text.
 *
 * Each substitution is intentionally lossy but conservative — false-positives
 * (substituting innocent text that happens to look like a token) are an
 * acceptable trade-off, false-negatives (leaking a real secret) are not.
 */
object BugReportSanitizer {

    /**
     * Returns a copy of [input] with the most common PII patterns replaced
     * with placeholders. Pure function — safe to call from any thread.
     */
    fun sanitizeForPreview(input: String): String {
        if (input.isEmpty()) return input
        return input
            // Android internal data paths
            .replace(Regex("/data/user/\\d+"), "/data/user/[REDACTED]")
            .replace(Regex("/storage/emulated/\\d+/Android/data/[^\\s]+"), "/storage/[REDACTED]")
            // Auth headers
            .replace(Regex("Bearer\\s+[A-Za-z0-9_\\-./=]+"), "Bearer [REDACTED]")
            .replace(Regex("Basic\\s+[A-Za-z0-9+/=]+"), "Basic [REDACTED]")
            // GitHub PAT prefixes
            .replace(Regex("github_pat_[A-Za-z0-9_]{20,}"), "github_pat_[REDACTED]")
            .replace(Regex("ghp_[A-Za-z0-9]{20,}"), "ghp_[REDACTED]")
            .replace(Regex("gho_[A-Za-z0-9]{20,}"), "gho_[REDACTED]")
            .replace(Regex("ghu_[A-Za-z0-9]{20,}"), "ghu_[REDACTED]")
            .replace(Regex("ghs_[A-Za-z0-9]{20,}"), "ghs_[REDACTED]")
            .replace(Regex("ghr_[A-Za-z0-9]{20,}"), "ghr_[REDACTED]")
            // Slack tokens
            .replace(Regex("xox[abprs]-[A-Za-z0-9-]{10,}"), "[SLACK_TOKEN_REDACTED]")
            // Email addresses (RFC 5322-ish — covers >=99% of real addresses)
            .replace(
                Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)+"),
                "email@[REDACTED]"
            )
            // IPv4 (catch-all for logs that leak raw egress IPs)
            .replace(Regex("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b"), "[IP_REDACTED]")
    }
}
