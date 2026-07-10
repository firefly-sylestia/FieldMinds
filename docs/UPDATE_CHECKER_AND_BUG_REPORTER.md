# In-App Update Checker & Bug Reporter

This document describes the in-app implementation of two dev-loop
features. The Cloudflare-side proxy code lives in
[`../infrastructure/workers/bug-reporter/`](../infrastructure/workers/bug-reporter/)

---

## Update Checker — `infrastructure/updates/`

### Files

| File | Purpose |
|------|---------|
| `infrastructure/updates/GitHubRelease.kt` | Data class for GitHub `/releases/latest` response. |
| `infrastructure/updates/UpdateChecker.kt` | Suspend fun `checkForUpdate(now: Long, currentVersionName: String)`: returns `UpdateInfo` (Latest, UpToDate, Unavailable, Errored). 24-hour throttle via `AppSettings`. |

### AppSettings additions

```kotlin
private const val KEY_UPDATE_LAST_CHECK_TIME = "update_last_check_time"  // Long
private const val KEY_UPDATE_LAST_DISMISSED_TAG = "update_last_dismissed_tag"  // String

private val _updateLastCheckTime = MutableStateFlow(
    prefs.getLong(KEY_UPDATE_LAST_CHECK_TIME, 0L)
)
val updateLastCheckTime: StateFlow<Long> = _updateLastCheckTime.asStateFlow()

private val _updateLastDismissedTag = MutableStateFlow(
    prefs.getString(KEY_UPDATE_LAST_DISMISSED_TAG, null)
)
val updateLastDismissedTag: StateFlow<String?> = _updateLastDismissedTag.asStateFlow()
```

### Banner UX (`features/field/presentation/components/UpdateBannerOverlay.kt`)

- Top slide-down banner (does **not** block the rest of the app).
- Renders only when:
  - `latest != null`
  - `latest.tag != currentVersionName`
  - `latest.tag != updateLastDismissedTag` (user hasn't tapped "Later")
- Three actions:
  1. **Update** — opens the release URL in the browser via `Intent.ACTION_VIEW`.
  2. **Later** — sets `updateLastDismissedTag` to the current tag → overlay stops immediately, returns on app restart.
  3. **Open changelog** — routes to `FieldMindChangelogScreen`.
- Banner respects `prefers-reduced-motion` (no slide animation if set).

### Mount point

In `features/field/presentation/navigation/FieldMindNavigation.kt::FieldMindApp`:

```kotlin
// After the lock screen, alongside the DailyFieldJournalOverlay
val updateInfo by updateChecker.updateInfo.collectAsState()
if (updateInfo is UpdateInfo.UpdateAvailable) {
    UpdateBannerOverlay(
        info = updateInfo,
        onUpdate = { /* open release URL */ },
        onLater = { appSettings.setUpdateLastDismissedTag(updateInfo.tag) },
        onOpenChangelog = { /* nav to changelog */ }
    )
}
```

### Throttling

`UpdateChecker.checkForUpdate()` early-returns the cached value when:

- `now - updateLastCheckTime < 24h` (skip the network round-trip).
- AND the cached value is still relevant (no force-recheck requested).

`FieldMindApp` calls `updateChecker.checkForUpdate(force = false)` once on first
composition (after the splash). A "Check now" tile in Settings calls with
`force = true`.

---

## Bug Reporter — `infrastructure/bugreport/`

### Files

| File | Purpose |
|------|---------|
| `infrastructure/bugreport/BugReportRequest.kt` | Sealed-class payload that the Worker consumes. |
| `infrastructure/bugreport/BugReporter.kt` | Coroutine-based POST service. Falls back to a web URL if `BuildConfig.BUG_REPORTER_URL == ""`. |

### Worker contract

- **Method:** `POST`
- **Headers:** `Content-Type: application/json`
- **Body (only three top-level fields; everything else gets baked into the Markdown body):**
  ```json
  {
    "title": "[BUG]: <user input>",
    "body":  "<pre-formatted Markdown blob matching .github/ISSUE_TEMPLATE/bug-report.yml>",
    "labels": ["bug"]
  }
  ```
  Anything else (e.g. `appVersion`, `installMethod`, `androidVersion`, `device`)
  MUST be embedded in the Markdown body — the Worker accepts only these three
  top-level keys and silently drops any others.
- **Response (200):**
  ```json
  { "ok": true, "issueNumber": 42, "issueUrl": "https://github.com/..." }
  ```
- **Response (200, soft-fail):**
  ```json
  { "error": "github_rejected", "status": 422, "message": "..." }
  ```

The Android client maps `{ ok: true }` → success dialog with the issue number
and link. Soft-fail → share intent fallback (`Intent.ACTION_SEND`) so the user
can paste the body into email / Discord / another channel.

### `.github/ISSUE_TEMPLATE/bug-report.yml` mapping

The form on the Android side mirrors the YAML template. The Submission
button joins all fields into a single Markdown body that the GitHub
issue-form parser expects.

```
## Bug Description
<user input>

## Steps to Reproduce
<user input>

## Expected vs Actual Behavior
<user input>

---
**App Version:** <pre-filled from BuildConfig.VERSION_NAME>
**Android Version:** <pre-filled from user-selected API level>
**Device:** <pre-filled from Build.MODEL + Build.MANUFACTURER>
**Installation Method:** <pre-filled from FLAVOR build config and a small dropdown>

## Additional Context
<user input>

## Logs
```text
<last crash log, sanitized>
```
```

### Crash log attachment (privacy first)

When the user opens the bug-report form:

1. Read `appSettings.crashLogHistory.firstOrNull()`.
2. If present, append it under `## Logs` wrapped in a fenced code block.
3. **The Worker still re-sanitizes** (defense in depth), but the client also
   strips paths, bearer tokens, and email addresses from the visible preview
   before the user taps "Send" — so they get a chance to see what they're
   sending.

If no crash log exists, the Logs section is hidden (the YAML template makes
it optional).

### Zero-infra fallback

If `BuildConfig.BUG_REPORTER_URL` is empty (or the Worker is unreachable), the
client constructs

```
https://github.com/firefly-sylestia/FieldMinds/issues/new?template=bug-report.yml&title=…&body=…
```

via `Intent.ACTION_VIEW` and shows a "We'll open your browser to finish
filing — sign in to GitHub there" notice.

---

## Privacy & abuse notes

- **Path sanitation:** the Worker strips Android-internal `/data/user/…` and
  `/storage/emulated/…` paths before forwarding.
- **Token sanitation:** the Worker strips `Bearer …`, `Basic …`, GitHub PAT
  prefixes (`ghp_`, `gho_`, `ghu_`, `ghs_`, `ghr_`, `github_pat_`), and Slack
  tokens.
- **Email & IP:** replaced with placeholders.
- **Rate limit:** Cloudflare free tier caps each Worker at 100k req/day. Add a
  per-IP rate limit rule if needed (5 req / 10s).

If the user wants a hostile review (manually paste secrets into the form),
both client and Worker will strip them — but neither trusts the other.
