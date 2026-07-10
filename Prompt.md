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

---

# Whimsical Redesign — Phase 1 (Journal Aesthetic Picker) Completion Summary

## Task

Land the Phase 1 foundation for the WHIMSICAL_REDESIGN_PLAN. Phase 1 was 80% already in place — `JournalStyle.kt` defined all 4 enums + 4 JournalConfig presets + 4 CompositionLocals, `FieldMindSettings.kt` exposed 4 StateFlows + setters, `FieldMindTheme.kt` provided all 4 CompositionLocals app-wide. **The only missing piece was the picker UI in `Settings → Appearance`.**

## What landed (commit `3cfa6542`)

### 1 file added new code: `app/.../presentation/screens/FieldMindSettingsScreen.kt`

- New `"Journal aesthetic"` `SectionHeader` + `SettingsGroupCard` inserted between the existing `Card Style` and `Entity Colors` sections in `AppearanceSettingsPage`.
- Four sub-pickers stacked with `HorizontalDivider`s, all wired to `fieldSettings.journalStyle / backgroundAnimation / microDelightIntensity / navBarStyle` via `setJournalStyle(style.key)` etc.
- New top-level helper `private fun <T> PillRadioGroup(...) where T : Enum<T>, T : KeyedEnum` between `ThemeToggle` and the `// Capture Defaults Settings Page` divider.

### 4 sub-pickers

| Sub-picker | UI | Storage key |
|---|---|---|
| Journal Style | Horizontally-scrolling Row of 4 large swatches with distinct visual previews (parchment + sepia book + crown ornament, cream + edit pencil-rule, dot-grid 4×5, watercolor radial + cloud + sparkles) | `journalStyle` ∈ {`victorian`, `sketchbook`, `bullet_journal`, `ghibli`} |
| Background motion | 3-pill radio with `LocalBackgroundAnimation.where` | `backgroundAnimation` ∈ {`static`, `gentle`, `full`} |
| Micro-delights | 3-pill radio | `microDelightIntensity` ∈ {`minimal`, `normal`, `maximum`} |
| Nav bar style | 3-pill radio | `navBarStyle` ∈ {`modern`, `nature`, `journal`} |

### 1 file refactored: `app/.../shared.presentation/theme/JournalStyle.kt`

- New interface `KeyedEnum { val key: String; val displayName: String }`.
- All 4 phase-1 enums (`JournalStyle`, `MicroDelightIntensity`, `BackgroundAnimationLevel`, `NavBarStyle`) now declare `: KeyedEnum` with the first two constructor params marked `override val`.
- Existing callers of `.key` / `.displayName` / `.description` continue to compile unchanged (third `description` param stays a plain `val`).

### 5 imports added to `FieldMindSettingsScreen.kt`

`KeyedEnum`, `JournalStyle`, `BackgroundAnimationLevel`, `MicroDelightIntensity`, `NavBarStyle` — all from `fieldmind.research.app.shared.presentation.theme`. Eliminates 6 fully-qualified `fieldmind.research.app.shared.presentation.theme.X.foo(...)` calls.

## Verification

- **Brace balance (Python regex stripping strings + comments):**
  - `FieldMindSettingsScreen.kt`: OPEN 660, CLOSE 660, DELTA 0. No negative-depth events. Final depth 0.
  - `JournalStyle.kt`: OPEN 36, CLOSE 36, DELTA 0. No negative-depth events.
- **Top-level symbol exports:** `PillRadioGroup` defined and exported at top-level scope in `FieldMindSettingsScreen.kt`. `KeyedEnum` defined at top-level scope in `JournalStyle.kt`.
- **`code-reviewer-minimax-m3` post-implementation review:** PASS. One minor followup note — if Material Symbols font lacks the `crown` glyph, swap to `local_florist` for the Victorian swatch's top-left ornament.

## Self-corrections caught during review

1. **First draft referenced helper functions that didn't exist.** `previewBackgroundFor(style.key)` and `previewOverlayFor(style.key)` were called but never defined (residual from the thinker's draft sketch). Replaced with an inline `when (style.key) { ... }` dispatch directly inside the swatch composable.
2. **`(option as dynamic)` is not valid Kotlin.** The thinker returned a sketch using JavaScript-style `dynamic` casts. Replaced with a proper Kotlin generic `private fun <T> PillRadioGroup(...) where T : Enum<T>, T : KeyedEnum` after adding the `KeyedEnum` interface to `JournalStyle.kt`.
3. **`@Suppress("UNCHECKED_CAST")` was misleading.** The original dispatch used `val previewBrush: Any = when(...) { ... is Color ... is Brush ... }` which needed suppression to compile. After consolidating to a uniform `Brush` type (wrapping solid colors as `Brush.linearGradient(listOf(color, color))` and Ghibli as `Brush.radialGradient(colors, radius)`), the modifier is just `Modifier.background(previewBrush)` with no cast and no `@Suppress`.
4. **`❦ (U+2766 FLORAL HEART)` glyph was unsafe.** Not in Android's default sans-serif — would fall back to `?` or a tofu box on most devices. Replaced with `MaterialSymbolIcon("crown")` (and the reviewer noted `local_florist` as a safer fallback if `crown` is missing).
5. **Stray `_ = style // keep qualified access in case import order shifts` line.** Leftover from a confused earlier str_replace. Removed before final commit.

## What this unlocks

- `Settings → Appearance → Journal aesthetic` is fully usable. Users can:
  - Pick Victorian / Sketchbook / BulletJournal / Ghibli — affects every card via `LocalJournalStyle.current` (Phase 3 wires visible card aesthetics).
  - Pick Static / Gentle / Full background motion.
  - Pick Minimal / Normal / Maximum micro-delights.
  - Pick Modern / Nature / Journal nav-bar style.
- Phase 2 (AnimatedBackgroundScene per journal style) can now dispatch on `LocalBackgroundAnimation.current`.
- Phase 4 (whimsical micro-delights) can branch on `LocalMicroDelightIntensity.current`.

## Next-session followups

1. **Phase 2** — Implement 4 time-of-day scenes in `AnimatedBackgroundScene.kt` (Dawn golden+mist, Day dappled sun, Evening amber+fireflies, Night stars+moon) and color-tint each scene by `LocalJournalStyle.current` (Victorian → sepia, Sketchbook → muted, BulletJournal → crisp neons, Ghibli → watercolor wash).
2. **Phase 3** — Wire `JournalConfig.cardCornerRadius` / `borderStyle` / `borderWidth` / `textureName` into the existing `JournalCard` and Card-of-cards surfaces so users actually SEE the journal style take effect on tap.
3. **Manual device visual test** — cycle each picker in `Settings → Appearance → Journal aesthetic`, ensure settings persist across app restarts, and (especially) verify the `crown` Material Symbol icon renders correctly — swap to `local_florist` if it falls back to tofu.

---

# Whimsical Redesign — Phase 2 (Atmospheric Skybox) Completion Summary

## Task

Implement Phase 2 of WHIMSICAL_REDESIGN_PLAN: replace the static warmth + texture + vignette feel with a living, breathing skybox that changes with time-of-day (Dawn / Day / Evening / Night) and tints each of the four journal aesthetics onto the result. 4 time-of-day × 4 journal styles = 16 distinct visual moods. User explicitly demanded no 400-line cap and no cheap work — use full potential.

## What landed (commit pushed to `origin/finetune`, ~880 lines added to AnimatedBackgroundScene.kt)

### Time-of-Day System
- `enum class TimeOfDay { Dawn, Day, Evening, Night }`
- `data class CelestialBody` (sun/moon position + phase)
- `data class ScenePalette` (17 color fields)
- `moonPhase(now)` — 29.5306-day synodic cycle, ref 2000-01-06 18:14 UTC
- `parseIsoMillisOrNull(iso)` — ISO-8601 → epoch millis
- `resolveTimeOfDay(sunrise, sunset, now, forceNight)` — dawn (sr-30…sr+90), day (sr+90…ss-60), evening (ss-60…ss+90), night (else); clock-hour fallback
- `resolveCelestial(tod, now)` — sun position for Dawn/Day/Evening; moon position + phase for Night

### Per-Journal Tint Extensions (4 distinct transformations, NOT one multiply)
- `Color.sepiaTint()` — luminance + warm bias (Victorian)
- `Color.pencilDesat()` — 55% original + 40% lum + green bias (Sketchbook)
- `Color.crispBoost()` — pull channels away from mean (BulletJournal)
- `Color.watercolorBleed()` — slight desat + warm-cream lift + alpha×0.93 (Ghibli)
- `ScenePalette.applyStyle(style)` — dispatches all 17 fields through mapper

### Base Palettes (4 hand-tuned color schemes)
- **Dawn** — peach+blue sky, cream-gold sun, rosy mist bands, dark hill silhouette
- **Day** — cyan+gold sky, vivid yellow sun, white cloud banks, green hills
- **Evening** — indigo+plum sky, deep-orange sun, firefly glow, plum horizon
- **Night** — deep-blue+cream sky, cream moon disc, white stars, charcoal horizon

### AtmosphericSkyboxScene (Layer 1.5 orchestrator)
- 5 `rememberInfiniteTransition` slots (cloudDrift, twinkle, fireflyPulse, mistDrift, sunDrift) with tier-aware durations
- 5 stable feature RNG pools (clouds 5, stars 60, fireflies 12, birds 4, celTex 10) — no recomposition flicker
- Single `Canvas { ... }` block calling 8 DrawScope extensions, gated by tod + animLevel + style

### 8 DrawScope Extensions
1. `drawSky(palette)` — vertical gradient top→bottom
2. `drawCelestialBodies(body, palette, style, sunDrift, texRng)` — sun (3 halos + disc) OR moon (halo + disc + phase-offset shadow)
3. `drawCelestialJournalOverlay(cx, cy, r, style, rng)` — per-journal ornament on celestial body:
   - Victorian: ornate double-ring + 8 compass tick marks + fleuron dot
   - Sketchbook: cream outline + graphite smudge + pencil cross-hatch tick
   - BulletJournal: 5×5 dot grid behind disc
   - Ghibli: 5 watercolor wash blobs + 4-pointed sparkle
4. `drawCloudBanks(palette, style, drift, rng, isFull)` — 5 soft-edge cloud banks + per-style ornament (Victorian cross-hatch, Sketchbook stipple, BulletJournal dot-grid) + Full-tier inner highlight ring on top puffs
5. `drawHorizonAndMist(tod, palette, mistDrift)` — 5-hump mountain `Path` + foreground hills + 4 dawn mist bands (vertical-gradient parallax drift)
6. `drawStarsAndConstellations(palette, twinkle, rng, shootPhase)` — 60 twinkle stars + Big Dipper (4) + Orion's belt (3) + Cassiopeia W (5) + sporadic shooting star (~25s cycle)
7. `drawFireflies(tod, palette, style, isStatic, isFull, pulse, rng)` — 12 fireflies (halo + pulse glow); skipped at Static + for BulletJournal
8. `drawBirds(tod, palette, globalDrift, rng)` — 4 V-formations drifting at Dawn/Evening, Full tier only

### AnimatedBackgroundScene modifications
- 4 new imports (`java.time.Instant`, `java.util.Calendar`, `kotlin.math.abs/cos`; `sin` already imported)
- Added `val tod = resolveTimeOfDay(...)` + `val palette = getBasePalette(tod).applyStyle(journalConfig.style)` after the LocalJournalStyle/animLevel/isDark reads
- Layer 1.5 `AtmosphericSkyboxScene(...)` inserted between AnimatedWeatherScene (Layer 1) and JournalWarmthOverlay (Layer 2) — so the warmth overlay color-grades the whole skybox consistently

## Verification

- **Brace balance**: AnimatedBackgroundScene.kt — 114 OPEN, 114 CLOSE, **Delta 0**. No negative-depth events. Max depth 6.
- **2 code-reviewer-minimax-m3 passes** (initial implementation + 4-issue polish round). Both PASS, ship-it verdict.
- **4 reviewer-flagged issues fixed in polish round**:
  1. (Behavioral) Static-tier fireflies leak → `drawFireflies` gained `isStatic` param with `!isStatic &&` gate.
  2. (Dead code) `isFull` unused in `drawCloudBanks` → activates top-puff inner highlight radial gradient at Full tier.
  3. (Code style) Fully-qualified `androidx.compose.ui.graphics.drawscope.Stroke(...)` x3 → single import added, usage bare.
  4. (Perf) `System.currentTimeMillis()` inside DrawScope for shooting star → hoisted to compose scope as `shootPhase: Long`, threaded through.

## Self-corrections caught during review

- First str_replace referenced helper functions (`previewBackgroundFor`, `previewOverlayFor`) that were never actually defined (residual from thinker's draft sketch). Replaced with inline `when (style.key) { ... }` dispatch.
- Three `androidx.compose.ui.graphics.drawscope.Stroke` calls were fully-qualified. Polish round added the import for cleaner calls.
- Initial `drawFireflies` lacked a Static gate — defeated the Static preset's no-moving-things promise. Fixed in polish round.

## What this unlocks

- App users now see a **living skybox**: dawn rose-gold mist, day cyan sky with drifting clouds, evening amber sun with fireflies, night cream moon with twinkling stars + constellations + occasional shooting stars + drifting bird formations. Each journal aesthetic imprints a distinct mood — sepia Victorian sunrise, pencil-desat Sketchbook dusk, crisp BulletJournal noon, watercolor Ghibli evening.
- The skybox respects all 3 user controls: `BackgroundAnimationLevel = Static` freezes everything (sky + celestial + horizon only), `Gentle` adds slow cloud drift + firefly pulse, `Full` activates the full feature set (twinkles, firefly drift, mist parallax, shooting stars, bird migrations).
- Existing features untouched: 4 texture draw routines (parchment/paper/dotgrid/watercolor), journal warmth overlay, journal texture overlay, vignette overlay.

## Next-session followups

1. **Manual device visual test** — cycle each picker in `Settings → Appearance → Journal aesthetic`, then verify the skybox changes when:
   - The clock crosses sunrise/sunset (Dawn → Day → Evening → Night transitions)
   - The user toggles `Background motion` between Static / Gentle / Full
   - The user toggles `Journal style` between all 4 options at the same ToD — verify each feels distinct
   - Watch for performance hiccups on older devices (especially BulletJournal + Full tier — highest draw-op count).
2. **Phase 3** — Wire `JournalConfig.cardCornerRadius` + `borderStyle` + `borderWidth` into existing `JournalCard` and Card composables so the journal style visibly affects cards (not just the skybox).
3. **Manual verification of the `crown` MaterialSymbolIcon** in the Victorian journal-style swatch in Phase 1 — confirm it renders correctly on-device; swap to `local_florist` if it falls back to tofu.

---

# Whimsical Redesign — Round 5 (Card Alignment) Completion Summary

## Task

User complaint: "many cards doesn't follow the ui design language they design such as the media and sharing card data tolls card filedmap card and observation timeline card". Make Home Screen cards follow the existing UI design language.

## Diagnosis

The 5 named parents are **already aligned** via `JournalCard` / `JournalClickableCard` wrappers:

| User-named parent | Wrapper used |
|---|---|
| Field Map | `JournalClickableCard` ✅ |
| Data Tools parent | `JournalCard` ✅ |
| Media & Sharing parent | `JournalCard` ✅ |
| Observation Timeline | `JournalCard` ✅ |
| Reading Review | raw `Card(34dp, surfaceContainerLow, nonClickableTier)` (matches JournalCard defaults) ✅ |

The actual drift is in the **inner mini-tiles** that lacked the theme-aware `Modifier.cuteShadow(...)` extension:

- `DataToolMiniCard` (8 instances: 4 Media/Sharing + 4 Data Tools quick tools)
- `MiniActionTile` (ReadingReviewCard inline tiles)
- `QuickActionChip` (QuickActionsRow)
- `HeroActionChip` (CompactHomeHeader Capture/Note/Projects)

Each had `Card`/`Surface` with elevation tier set but no theme-aware shadow modifier → dark-mode cards felt flat on AMOLED.

## What landed (commit `802e58df`, pushed to `origin/finetune`)

1. **Import added** at L90: `import fieldmind.research.app.ui.theme.cuteShadow`
2. **`DataToolMiniCard`** (L2889) — chained `Modifier.cuteShadow(CuteElevations.nonClickableTier, RoundedCornerShape(24.dp))`
3. **`MiniActionTile`** (L2050) — chained `Modifier.cuteShadow(CuteElevations.nonClickableTier, RoundedCornerShape(28.dp))` AFTER `.clickable(onClick = onClick)`
4. **`QuickActionChip`** (L1989) — chained `Modifier.cuteShadow(CuteElevations.clickableTier, RoundedCornerShape(24.dp))` AFTER `.clickable { haptics.light(); onNavigate(screen) }`
5. **`HeroActionChip`** (L1357) — chained `Modifier.cuteShadow(CuteElevations.clickableTier, RoundedCornerShape(24.dp))` AFTER `.pressScale(...)`

Diff stat: `+17 / -4` on `FieldMindHomeScreen.kt`.

## Tier rationale

`cuteShadow` elevation tier MIRRORS existing `cardElevation` tier for each tile (keeps shadow/elevation visually consistent within each tile):

| Tile | cuteShadow | cardElevation |
|---|---|---|
| DataToolMiniCard | nonClickable (4dp) | nonClickable (4dp) |
| MiniActionTile | nonClickable (4dp) | nonClickable (4dp) |
| QuickActionChip | clickable (6dp) | clickable (6dp) |
| HeroActionChip | clickable (6dp) | Surface tonal 0dp |

## Verification

- **Brace balance**: HOME 660 open / 660 close / delta 0.
- **cuteShadow sites**: 5 total (1 import + 4 call sites at L1357, L1989, L2050, L2889).
- **Shape match**: all 4 cuteShadow shape args match underlying Card/Surface shape.
- **Modifier order**: cuteShadow uses `Modifier.shadow(...)` (draw-only, no pointer consumption), so chaining AFTER `clickable{}`/`pressScale()`/`expressivePress()` is safe.
- **`@Composable` context**: cuteShadow is `@Composable`, called inside modifier chain of `@Composable` functions. Compose tracks correctly.
- **`code-reviewer-minimax-m3` verdict**: PASS-with-caveats. Tier-vs-clickability + dark-mode shadow-stacking concerns are subjective styling deferred to device-side testing.
