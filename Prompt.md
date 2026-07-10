# In-app Update Checker + Bug Reporter — Completion Summary

## Task

Build the in-app Kotlin pieces to mirror the previously-shipped Cloudflare Worker pipeline:

1. Surface **GitHub Releases** as a top slide-down banner with a 24h throttle.
2. Expose a **Bug Report** screen that mirrors `.github/ISSUE_TEMPLATE/bug-report.yml` and POSTs to the Worker (with a GitHub web-URL fallback).

All integration points with existing code paths had to be AGENTS.md compile-safe.

## What landed

### New files (7)

| File | Purpose |
|---|---|
| `app/.../infrastructure/updates/GitHubRelease.kt` | Gson DTO for GitHub `/releases/latest`. |
| `app/.../infrastructure/updates/UpdateChecker.kt` | Coroutine service, 24 h throttle, `sealed interface UpdateInfo` (Idle / Loading / UpdateAvailable / UpToDate / Unavailable / Errored). |
| `app/.../infrastructure/bugreport/BugReportRequest.kt` | Worker POST payload + `sealed class BugReportResult` (Success / SoftFail / WebUrl / HardFail). |
| `app/.../infrastructure/bugreport/BugReportSanitizer.kt` | PII-strip on the in-app preview (PAt prefixes, Bearer/Basic headers, file paths, emails, IPv4). |
| `app/.../infrastructure/bugreport/BugReporter.kt` | `BugReportReporter` that hits the Worker; falls back to GitHub web-URL on any non-2xx / empty URL. |
| `app/.../presentation/components/UpdateBannerOverlay.kt` | Top-slide-down banner peer to `DailyFieldJournalOverlay`. Actions: **Update**, **Later**, **Notes**. |
| `app/.../presentation/screens/FieldMindBugReportScreen.kt` | Full-screen form mirrored from bug-report.yml; auto-attaches the latest entry of `AppSettings.crashLogHistory` (sanitized for preview). |

### Edited files (5)

| File | Change |
|---|---|
| `app/.../shared/data/model/AppSettings.kt` | New "Update Checker" section with 8 keys + 8 StateFlows + 8 setters, slotted between **Crash Reporting** and **Theme**. |
| `app/.../features/field/data/settings/FieldMindSettings.kt` | New `bugReportsAttachCrashLog` StateFlow + setter + sink in `toExportJson` / `applyFromJson` / refresh-tail / `clearAllPreferences`. |
| `app/build.gradle.kts` | New `buildConfigField("String", "BUG_REPORTER_URL", "\"\"")` on both `fdroid` + `github` flavors. |
| `app/.../presentation/navigation/FieldMindNavigation.kt` | Added `FieldMindScreen.BugReport` sealed-class entry, registered composable, mounted `UpdateBannerOverlay` in `FieldMindApp` (peered to the journal overlay), threaded `onOpenBugReport` callback through to `FieldMindSettingsScreen`. |
| `app/.../presentation/screens/FieldMindSettingsScreen.kt` | Added `onOpenBugReport` parameter; new **Updates** section above **About & advanced** containing the auto-attach toggle plus **Check for updates** and **Report a bug** nav cards. |

## Self-corrections during review

Three issues were caught by `code-reviewer-minimax-m3` and applied before completion:

1. `FieldMindBugReportScreen.kt` originally used `var includeCrashLog by remember { mutableStateOf(viewModel.fieldSettings.bugReportsAttachCrashLog.value) }` — a `.value`-direct-read off a StateFlow, the same anti-pattern that was removed from the journal overlay last turn. Migrated to `val includeCrashLog by viewModel.fieldSettings.bugReportsAttachCrashLog.collectAsState()`. Required dropping the now-illegal `includeCrashLog = value` re-assignment inside the Switch's `onCheckedChange` (State<T> has no setValue operator); the StateFlow round-trip via `setBugReportsAttachCrashLog` → `_bugReportsAttachCrashLog` → `collectAsState` keeps the Compose state in sync.
2. `BugReportReporter` had a dead `private val client: HttpClient = HttpClientForReporter` constructor parameter that was never read (the class dispatches directly to `HttpClientForReporter.postJson(...)`). Removed the field, the constructor's matching parameter, and the `import fieldmind.research.app.infrastructure.updates.HttpClient` line.
3. `UpdateBannerOverlay.kt` was using a fully-qualified `androidx.compose.foundation.layout.Column {` inside the banner — replaced with the imported `Column {`.

Also caught + fixed: `FieldMindBugReportScreen.kt` originally rendered the back button as `.androidx.compose.foundation.clickableSafe(onBack)` (invalid syntax swapped for `Surface(onClick = onBack, …)`).

## Verification chain

- Two `code-reviewer-minimax-m3` passes (post-implementation review + post-self-correction re-review).
- Static grep sanity sweep: all new symbols referenced exactly where expected, no orphan `.clickableSafe`, no double-declared constants, no broken imports across the two new packages.
- Bash `git status --short` confirms the working tree matches the planned set of 7 new files + 5 modified files.
- AGENTS.md compile-safety: no `@Composable` calls inside non-composable lambdas, modifier order respected (`Modifier.fillMaxWidth().graphicsLayer{…}.statusBarsPadding().padding(…)`), `BuildConfig.BUG_REPORTER_URL` is supplied via `buildConfigField`, `collectAsState()` is used everywhere instead of `.value`.

## What this unlocks

- App start → `UpdateChecker.check()` runs on first composition (gated by `appSettings.updateCheckEnabled`). On UpdateAvailable → top banner pops the user into the GitHub release page; "Later" persists the dismissed tag so it doesn't reappear for the same version.
- Settings → **Updates** section → "Report a bug" tile opens the new full-screen form. Submission POSTs JSON `(title, body, labels)` to the Worker URL (per flavor); falls back to opening the GitHub web-URL template form when the Worker is empty (default fdroid/gh with no Worker deployed yet).
- `appSettings.bugReportsAttachCrashLog` toggles whether the most recent entry of `AppSettings.crashLogHistory` is sanitized-and-appended to the report body (Markdown `## Logs` section). The toggle appears both in the Bug Report form and the new Settings tile.

## Still TODO (next-session followups)

1. Stage + commit + push to `origin/finetune` once CI rules on the working tree.
2. Sign in to Cloudflare + deploy the Worker following `infrastructure/workers/bug-reporter/README.md`; run step-5 curl smoke test against the deployed URL; fill in `BUG_REPORTER_URL` per flavor in `app/build.gradle.kts` (or via `gradle.properties` if preferred).
3. Manual e2e: open the app on a device, observe the overlay (forced by toggling `update_check_enabled` off→on), then verify the bug-report flow end-to-end against the deployed Worker.

---

# Round-4 CI Compile Fix — Completion Summary

## Task

Fix `:compileFdroidDebugKotlin` failure with ~100+ Kotlin compile errors spanning 7 files. Errors cascaded from a brace mismatch in `FieldMindHomeScreen.kt` and a `@Composable` leak in `AnimatedBackgroundScene.kt`.

## Root causes

1. **`LiveWeatherDashboardWidget` (HomeScreen.kt 1388-end) had 7 nested block opens but only 6 closing braces at function end.** The unclosed scope swallowed every subsequent function (`ForecastDetailItem`, `weatherConditionIcon`, `WeatherConditionImage`, `QuickActionsRow`, `QuickActionChip`, `ReadingReviewCard`, `MiniActionTile`, `ObservationTimelinePreview`, `TimelinePreviewEvent`, `CurrentProjectResearchCard`, `ProjectAssetChip`, `RecentActivityGroupCard`, `LearnRecommendation`, `recommendedResources`, `ResearchSessionCtaCard`, `SessionObservationsCard`, `DevWeatherTestPanel`, `getMoonPhase`, `formatTimeFromIso`, `computeFieldworkNudge`, `RecentCapturesCard`, `ExpandMetric`, `ExpandInfoChip`, `DataToolMiniCard`, `QuickCaptureSheet`, `QuickCaptureOption`, `VoiceNoteCaptureDialog`) as `local function` inside it, generating ~30 `Modifier 'private' is not applicable to 'local function'` errors and ~15 `Unresolved reference` errors. Forward calls at lines 391–957 in `HomeScreen()` also failed to resolve those swallowed names.

2. **`buildList` in `ObservationTimelinePreview` (HomeScreen.kt 2055) lacked explicit type parameter** for the chained `.sortedWith(compareByDescending<TimelinePreviewEvent> { it.date }.thenByDescending { it.time })`, causing ~25 type-inference cascade errors (lines 2055–2097).

3. **`AnimatedBackgroundScene.kt` `DrawScope` extensions (`drawParchmentTexture`, `drawPaperTexture`, `drawWatercolorTexture`) called `rememberTextureRng(...)` (a `@Composable`) from non-`@Composable` context** — flagged at lines 331, 368, 423.

## What landed (commit `836baf2c`)

### `app/src/main/java/fieldmind/research/app/features/field/presentation/screens/FieldMindHomeScreen.kt`
- Added the missing closing `}` at end of `LiveWeatherDashboardWidget`, swapping the trailing `}` chain followed by `@Composable` to add the 7th close before `ForecastDetailItem`. Str_replace anchored on the unique `private fun ForecastDetailItem(...) {` line.
- Changed `val events = buildList {` → `val events = buildList<TimelinePreviewEvent> {` at line 2055 in `ObservationTimelinePreview`.

### `app/src/main/java/fieldmind/research/app/features/field/presentation/components/AnimatedBackgroundScene.kt`
- In `JournalTextureOverlay` (a `@Composable`), allocated the rng once via `val rng = rememberTextureRng(journalConfig.textureName)` in each of the two branches that call `drawJournalTexture`. Passed `rng` into the Canvas lambda → `drawJournalTexture(...)`.
- Added `rng: List<Float>` parameter to `drawJournalTexture`, `drawParchmentTexture`, `drawPaperTexture`, `drawDotGridTexture`, `drawWatercolorTexture`. The two non-using textures (`drawDotGridTexture`, `drawPaperTexture`) carry `@Suppress("UNUSED_PARAMETER")` for cleanliness.
- Removed the internal `val rng = rememberTextureRng("<name>")` lines from `drawParchmentTexture`, `drawPaperTexture`, `drawWatercolorTexture`.

## Verification

- Brace balance (Python regex stripping strings + comments):
  - `HomeScreen.kt`: 660 `{` vs 660 `}` → Delta 0 ✅
  - `AnimatedBackgroundScene.kt`: 39 `{` vs 39 `}` → Delta 0 ✅
- `code-reviewer-minimax-m3` post-implementation review flagged only `@Suppress("UNUSED_PARAMETER")` on `drawPaperTexture`/`drawDotGridTexture` as decorative (Kotlin doesn't warn on unused `fun` parameters); kept as-is for granularity (all 4 texture extension signatures uniform).
- Cross-file errors (LearnScreen, LibraryScreen, SettingsScreen, WeatherCatalogScreen, WeatherDatabaseScreen referencing `recommendedResources`, `formatTimeFromIso`, `DevWeatherTestPanel`, `WeatherConditionImage`, `rec.resource.*`) are CASCADE — they should all resolve once `HomeScreen.kt` compiles, since they were caused by `HomeScreen.kt` failing to export its top-level symbols.

## Self-corrections caught during review

- First skim: read wrong line ranges in `LiveWeatherDashboardWidget` and missed the `}` shortfall. The `thinker-with-files-gemini` agent precisely counted 7 nested-opens (function + Surface + Box + Column + conditionColor/textOnScene/pulseAlpha/glassColor region blocks + else-if + Row + Column) vs 6 visible close braces and identified line 1878 as the location needing the extra close.
- `AnimatedBackgroundScene.kt` fix: chose to allocate rng separately in each branch (Full + else-if) rather than pulling rng allocation outside the `if` chain. Cleaner because `rememberTextureRng` is only invoked when one of the branches actually runs (avoids calling it on null/empty textures).

## What this unlocks

- `:compileFdroidDebugKotlin` should pass; downstream `:compileGithubDebugKotlin` and `:lint` cascade should also pass since they share the same Kotlin sources.
- CI re-runs on push to `origin/finetune` will surface any remaining non-build issues (lint, instrumentation test compile, etc.) without the build-noise from broken Kotlin compile.
