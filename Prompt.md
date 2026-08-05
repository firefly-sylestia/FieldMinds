# Prompt.md — Request Log

## Latest Request (COMPLETED)

**Fix CI compile failure — unresolved local function beginExploreSession**

### What was requested

CI `:app:compileDebugKotlin` failed with
`TopicRevealScreen.kt:306:9 Unresolved reference 'beginExploreSession'`.

### Root cause

Kotlin local functions are scoped from their declaration point onward — a
local function cannot call another local function declared LATER in the
same block (unlike class members, which are order-independent).
`startExploreSession` (declared first) called `beginExploreSession` whose
definition sat after it, so the call was an unresolved reference. Every CI
run since the conflict-dialog feature landed failed at compile; the earlier
lint log was from a pre-feature run.

### What changed

- `TopicRevealScreen.kt` — moved the `beginExploreSession` definition
  ABOVE `startExploreSession` so the call chain is strictly
  declaration-before-use: openExploreBrowserAndGoHome (194) →
  continueExploreFlow (243) → beginExploreSession (282) →
  startExploreSession (325). Added a KDoc note explaining the ordering
  requirement so a future edit doesn't reintroduce the bug.

### Validation

- Verified every local-function caller sits after its declaration (callers
  at 217/251/321/354/635/669/679/717/788).
- `scripts/check_braces.py` passed for all four feature files.
- `git diff --check` passed.
- Code review confirmed the reorder and that the other queued-session files
  (ExploreSession.kt object members, HomeScreen.kt top-level composable,
  AppPreferences.kt clearQueued) hold no similar ordering hazard.
- Gradle/build commands were not run because the repository forbids local
  Android compilation; CI remains the compilation gate.

## Latest Request (COMPLETED)

**Fix CI lint failure — NonObservableLocale in EntryDetailScreen**

### What was requested

CI `:app:lintDebug` failed the build with one error: reading
`Locale.getDefault()` in a non-observable way inside the @Composable
`formatMetadataTimestamp` helper in `EntryDetailScreen.kt`.

### What changed

- Added `import androidx.compose.ui.platform.LocalLocale`.
- `formatMetadataTimestamp` now reads the locale via Compose's observable
  `LocalLocale.current.platformLocale` (imported `java.util.Locale`, safe
  for `SimpleDateFormat`) instead of `Locale.getDefault()`, so the
  timestamp re-formats when the user changes the system locale.

### Validation

- `scripts/check_braces.py` passed for `EntryDetailScreen.kt`.
- `git diff --check` passed.
- Compose BOM 2026.05.01 includes `LocalLocale`/`platformLocale` (available
  since Compose UI 1.1.0). Lint reported exactly one error and it is the
  one fixed; remaining `Locale.getDefault()` uses in `app/` live in
  non-composable helpers/lambdas that lint does not flag.
- Gradle/build commands were not run because the repository forbids local
  Android compilation; CI remains the compilation gate.

## Latest Request (COMPLETED)

**Detail hero tear more uneven + subtle seeded tilt**

### What was requested

In the entry detail view, make the torn-paper seam a little more uneven
(not too much), and give the whole banner a random little remembered tilt
(stable per entry, never re-rolls).

### What changed

- `PaperCard.kt` — `SoftTearParams` amplitudes nudged up ~25% (tooth
  5.0+1.8 → 6.4+2.2dp, deep 1.8+1.2 → 2.4+1.5dp, micro 0.8+0.6 →
  1.0+0.8dp, ripple 1.0+0.7 → 1.3+0.9dp) so the hero seam reads a touch
  rawer and more hand-torn while the broad wave rhythm still dominates;
  the worst-case bite stays ~10dp, well inside the hero content.
- `EntryDetailScreen.kt` — the whole torn banner (white under-sheet + hero
  + content + back/more buttons) now rotates by a tiny seeded angle
  (`heroTilt = Random(tearSeed*31 + 0x0CAFE11E).nextFloat()*2.4f - 1.2f`, i.e.
  ±1.2°) via `Modifier.rotate` on the outer banner Box. `heroTilt` and
  `tearSeed` are declared before the Box that uses them (fixes a forward-
  reference compile error from the first draft); the old in-Box duplicate
  `tearSeed` declaration was removed.

### Validation

- `scripts/check_braces.py` passed for both changed Kotlin files.
- `git diff --check` passed.
- Gradle/build commands were not run because the repository forbids local
  Android compilation; CI remains the compilation gate.

## Latest Request (COMPLETED)

**Conflict dialog when starting a new explore mid-session + queued sessions**

### What was requested

Starting a new explore while another session is running silently discarded
the old one. Add a dialog: "Save for later" pins the new topic (current
session keeps running); "Explore now"/"Start new explore" queues the
current session and starts the new one.

### What changed

- `ExploreSessionStore` gained a persisted queued-sessions list (cap 3,
  JSON in prefs, reactive `queuedSessionsState` seeded at startup):
  `queueActiveSession` (pauses + banks time + vacates the active slot),
  `removeQueued`, `clearQueued`, `resumeQueuedSession` (removes target
  FIRST, pause-banks the current session into the queue, activates the
  resumed one and banks its paused span).
- `TopicRevealScreen.startExploreSession` now checks for an active session
  (different topic) and shows the conflict dialog before starting. Decline
  records the new topic as recently-unexplored; Save-for-later pins it;
  confirm queues the running session (reminder cancelled) and starts the
  new one via the extracted `beginExploreSession`.
- `HomeScreen` gained a "Queued explores" section (tap to resume — swap,
  cancel reminder, re-arm reminder + service gated on
  `exploreServiceShouldRun`; ✕ discards).
- `AppPreferences.setExploreSessionsEnabled(false)` clears the queue too.

### Validation

- `scripts/check_braces.py` passed for all changed Kotlin files.
- `git diff --check` passed.
- Gradle/build commands were not run because the repository forbids local
  Android compilation; CI remains the compilation gate.

## Latest Request (COMPLETED)

**Cabinet Legacy chip ordering/visibility + bottom nav category tint**

### What was requested

In the Cabinet: (1) the Legacy category/filter should go last in the chip
row, (2) the Legacy chip should be hidden when there are no legacy entries,
and (3) the bottom nav color should match the category background color.

### What changed

- `CabinetScreen.kt` — moved the Legacy chip to the END of the filter row
  (after all native categories) and gated it behind
  `entries.any { it.isLegacy } || showLegacyOnly`, so it only appears when
  restored FieldMind records exist (or the legacy view is currently open).
- `CabinetScreen.kt` — publishes the active filter's `categoryBackgroundWash`
  via `CurioNavTint.publishCabinetWash(...)` (LaunchedEffect keyed on the
  wash, cleared on dispose), mirroring SpinScreen's existing handoff.
- `CurioBottomNav.kt` — `CurioNavTint` gained a `cabinetWash` state; the nav
  bar's `containerColor` now uses it on the CABINET route (Spin already used
  its wash; Home stays on the plain surface).

### Notes

- With the chip hidden when empty, the "No legacy captures yet" empty state
  (and its Open-settings CTA) is no longer reachable from the chip — the
  intended consequence of hiding it when nothing exists.

### Validation

- `scripts/check_braces.py` passed for both changed Kotlin files.
- `git diff --check` passed.
- Gradle/build commands were not run because the repository forbids local
  Android compilation; CI remains the compilation gate.

## Latest Request (COMPLETED)

**Floating explore bubble disappears after clearing app data**

### What was requested

After using "Explore now" once (bubble works), clearing app data and starting a
new explore session leaves the floating bubble missing — even though "Display
over other apps" still shows Allowed (system permission survives a data clear).
It used to crash (Android 16 overlay attach) and now silently fails.

### What changed

- `ExploreSessionService.onStartCommand` now clears the `bubbleUnavailable`
  latch whenever an explicit start intent carries `EXTRA_SESSION` (every new
  session / re-arm / settings sync). A transient WindowManager or Android 16
  attach failure can no longer permanently disable the bubble for the life of
  the process — each fresh "Explore now" retries the overlay once. The latch
  still stops per-tick retry spam (restart-loop guard) since the 60s
  notification tick and pause toggles never reset it.
- `removeBubble()` now also destroys and nulls `overlayOwner`, so hide→show
  and session cycles build a fresh owner instead of reusing a stale RESUMED
  one (ComposeView resolves ViewTree owners during attachment).
- The `WindowManager.addView` failure branch now logs the actual throwable
  ("Explore overlay window add failed") so a persistent device-level rejection
  is diagnosable in logcat instead of silent.

### Why not "ask for the perms again"

The system "Display over other apps" grant genuinely survives a data clear
(the user confirmed it shows Allowed), so re-prompting can't change anything —
`Settings.canDrawOverlays()` already returns true and no dialog would fire.
The real failure is the overlay window attach, which is now retried per
session instead of latched permanently.

### Validation

- `scripts/check_braces.py` passed for `ExploreSessionService.kt`.
- `git diff --check` passed.
- Gradle/build commands were not run because the repository forbids local
  Android compilation; CI remains the compilation gate.

## Latest Request (COMPLETED)

**Fix Cabinet bulk-delete CI compilation error**

### What was requested

Fix CI errors reporting unresolved `launch` and a suspend `deleteByIds` call in `CabinetScreen.kt`.

### What changed

- Added the missing `kotlinx.coroutines.launch` import.
- The existing `rememberCoroutineScope()` now correctly launches the suspend bulk-delete operation.
- Confirmed the repository already provides `suspend deleteByIds(Collection<String>)` and media-storage imports are present.

### Validation

- `scripts/check_braces.py` passed for `CabinetScreen.kt`.
- `git diff --check` passed.
- Gradle/build commands were not run because the repository forbids local Android compilation; CI remains the compilation gate.

## Latest Request (COMPLETED)

**Default-off experimental voice-to-text**

### What was requested

Make voice-to-text disabled by default and expose an opt-in toggle in Settings → Experimental.

### What changed

- Added a persisted `AppPreferences.voiceToTextEnabledState` preference with a default of `false`.
- Added the discoverable Experimental → Voice-to-text switch; ordinary voice recording remains unaffected.
- Gated Sound Bite dictation buttons, transcription panels, recognizer creation, and permission callbacks.
- Gated saved voice-note transcription in Entry Detail.
- Cancels active dictation, clears pending requests, and destroys recognizers when the experiment is disabled or the screen is disposed.

### Validation

- `scripts/check_braces.py` passed for all changed Kotlin files.
- `git diff --check` passed.
- Gradle/build commands were not run because the repository forbids local Android compilation; CI remains the compilation gate.

## Latest Request (COMPLETED)

**Restrict FieldMind observation session to legacy entries**

### What was requested

Remove the FieldMind observation-session action from native Curio detail menus and keep it available only in the Legacy Cabinet/detail entries.

### What changed

- Wrapped the detail overflow-menu action in `resolvedEntry.isLegacy`.
- The condition uses persisted legacy provenance, not category, subtype, or display text.
- Native Curio detail menus no longer show the FieldMind observation action; restored legacy entries retain it.

### Validation

- Static validation and review completed.
- Gradle/build commands were not run because the repository forbids local Android compilation; CI remains the compilation gate.


## Latest Request (COMPLETED)

**Thin shared-wave detail tear and Cabinet bulk delete**

### What was requested

- Keep the detail hero's paper tear, but make the exposed white backing very thin and uneven.
- Give the hero and white backing the same broad wave rhythm while keeping their fine bumps slightly different for a realistic layered-paper tear.
- Add Cabinet mass delete triggered by long-press selection, with category/filter-scoped select-all.

### What was changed

- Reduced the detail white backing to a 6dp lip with a 10dp baseline and 12dp reserved layout extent.
- Split the seeded tear math into a shared broad-wave foundation and independent fine tooth; the white sheet now follows the broad waves at reduced amplitude with its own shallow bump layer.
- Added long-press selection to Cabinet cards, selected-state styling, category/search-scoped Select all, cancel selection, confirmation, and bulk delete.
- Bulk deletion removes Room rows first through a single `WHERE id IN (...)` DAO query, then cleans each entry's audio and image files.
- Native detail navigation remains unchanged outside selection mode; normal taps open detail, while selection-mode taps toggle entries.

### Validation

- `scripts/check_braces.py` passed for all changed Kotlin files.
- `git diff --check` passed.
- Gradle/build commands were not run because the repository forbids local Android compilation; CI remains the compilation gate.


## Latest Request (IN PROGRESS → COMPLETED)

**FieldMind restore metadata cards, separate Legacy Cabinet, and Curio observation session**

### What was requested

- Preserve FieldMind observation/note metadata and species information during legacy restore.
- Show dedicated metadata and species cards in Curio detail without affecting native entries.
- Keep restored legacy entries in a separate Cabinet section rather than mixing them with normal Curio captures.
- Add an always-available FieldMind-style observation session action in the detail overflow menu, using Curio UI and saving safely into the existing Curio repository.

### What was done

- Extended optional `FieldMindMetadata` with weather condition, session start/end, and source identifiers; existing native capture constructors remain backward-compatible because all new fields default.
- Improved archive parsing and species matching using FieldMind's `speciesInfo`, taxonomy, conservation, structured details, timestamps, location, weather, quality, status, project, source, and tags.
- Added a dedicated Curio-styled FieldMind metadata card with weather, coordinates, duration, status, tags, structured details, and nested taxonomy/species presentation. The card renders only when restored provenance exists.
- Added an explicit Legacy Cabinet mode. Normal Cabinet mode excludes legacy imports; the Legacy chip opens a separate "Legacy Cabinet" view and clears category filters.
- Added an always-available `fieldmind-observation` route and Curio-styled timed observation screen. Saving produces a normal Field Notes entry with optional FieldMind provenance, so native Curio flows and storage remain unchanged.
- Added the observation-session action to the Entry Detail overflow menu.

### Validation

- `scripts/check_braces.py` passed on all changed Kotlin files.
- `git diff --check` passed.
- Gradle/build commands were not run because the repository's AGENTS.md forbids local Android compilation; CI remains the compilation gate.
- Plain `archive.json` media can only restore files that remain accessible through the exported URI/path; packaged `.fieldmind` media remains the reliable complete-media path.

### Follow-up note

The session screen is intentionally a lightweight Curio-native session capture rather than a second FieldMind database. Future enhancement can add species picker, GPS/weather capture, and attachments without changing the current persistence boundary.

## Latest Request (COMPLETED)

**Persist legacy provenance explicitly**

FieldMind imports must be marked legacy at restore time, not inferred from category, subtype, or another display field. Add a persisted Room provenance flag, migrate existing imported rows safely, pass the flag through entity/domain conversion, and keep native Curio captures false.

### Completion

- Added and pushed explicit persisted legacy provenance in commit `fbfa4633`.

## Latest Request (COMPLETED)

**Cabinet discoverability, faster loading, and detail tear-gap fix**

### What was requested

- Restore the Cabinet search option and make the delete action visible.
- Make Cabinet opening less laggy without changing its capture content.
- Fill occasional straight-edge gaps in the detail hero's torn paper seam.

### What changed

- Added an always-visible Cabinet `Select` action; selection mode exposes Select all, Delete, and Cancel while preserving long-press selection.
- Kept search and sort visible in the normal toolbar and preserved filter/search-scoped selection behavior.
- Moved Room entity-to-domain conversion onto `Dispatchers.Default` so Gson/topic reconstruction does not block the Compose collector during Cabinet startup.
- Memoized Cabinet card header gradients by category accent to reduce recomposition allocations.
- Tightened the white under-sheet to a thin, uneven lip, overlapped it farther behind the hero, and clamped its lower tear path inside the measured sheet so seeded peaks cannot reveal page-wash gaps.
- Updated the store changelog entry for the shipped UI/performance refinements.

### Validation

- `scripts/check_braces.py` passed for all changed Kotlin files.
- `git diff --check` passed.
- Gradle/build commands were not run because the repository forbids local Android compilation; CI remains the compilation gate.

## Latest Request (COMPLETED)

**Fix CI compile errors and Android 16 Explore overlay crash**

### What changed

- Fixed `CurioTopicCard` by resolving the composable `CurioGradients.cardGradient()` call in the composable scope before remembering the `Brush`.
- Fixed the `PaperCard` white under-sheet path by declaring its local `x` traversal variable.
- Pushed those CI compile fixes in commit `a9357636`.
- Hardened the Explore overlay: a `FrameLayout` host now carries lifecycle, ViewModel, and saved-state owners before attachment; the nested ComposeView is composed only after the overlay is attached.
- Added guarded failure cleanup that removes the overlay, disposes the composition, destroys the service-owned owners, and disables the bubble for the service session so notification-only Explore mode prevents restart loops.

### Validation

- `scripts/check_braces.py` passed for all changed Kotlin files.
- `git diff --check` passed.
- Gradle/build commands were not run because the repository forbids local Android compilation; CI remains the compilation gate.

## Previous Request (COMPLETED)

**Refine detail hero torn seam**

The detail hero seam needs 2–3 broad waves with smaller bumpy ripples inside them, applied consistently to both the hero edge and exposed white under-sheet. The white layer must extend below the hero enough to eliminate background gaps while retaining the seeded, stable per-entry shape.

### Implementation notes

- Reduced the primary tear rhythm to roughly 2.2–3 broad waves.
- Added a shallow 7–11-cycle ripple layer plus fine seeded fiber noise to the shared displacement function.
- Applied the shared broad/ripple rhythm to the white sheet's exposed lower edge.
- Increased the white lip to 24dp, baseline to 30dp, and reserved 48dp of layout extent so the sheet cannot overlap the next content section.
- Static brace and whitespace checks passed; Gradle/build commands remain prohibited by repository instructions.

## Latest Request (COMPLETED)

**Harden Curio backup restore and preserve FieldMind text/metadata**

### What was requested

- Investigate intermittent "Backup failed" / restore failures.
- Audit whether the multiple FieldMind observation and note text fields are fully accounted for.

### What was changed

- Curio backup restore now rejects unreadable, missing, malformed, or invalid capture payloads before deleting current media or database rows, preventing a corrupt file from being treated as an empty restore.
- Older backups remain compatible through collection/default normalization and legacy tag-column handling.
- Backup export tolerates a stale or unreadable supplementary species catalog instead of failing an otherwise valid capture/settings backup.
- FieldMind observation metadata now preserves timing/change/lifecycle fields, weather snapshot, parent/follow-up references, quality, and all exported text fields.
- FieldMind note metadata now preserves category, tags, status, project/source references, and lifecycle timestamps.
- Attachment captions are retained in the imported Curio text instead of being silently discarded.
- The Curio FieldMind metadata card displays the expanded lifecycle and timing metadata with readable timestamps.

### Validation

- `scripts/check_braces.py` passed for all changed Kotlin files.
- `git diff --check` passed.
- Gradle/build commands were not run because the repository forbids local Android compilation; CI remains the compilation gate.

## Latest Request (COMPLETED)

**Fix FieldMind observation screen CI compile errors**

CI reported `rememberSaveable` as unresolved in `FieldMindObservationScreen.kt`, causing cascading errors around the metadata constructor, `takeIf`, and button negation. The screen imported it from the wrong Compose package; the correct import is `androidx.compose.runtime.saveable.rememberSaveable`.

## Latest Request (COMPLETED)

**Make Smart density 2x actually shrink the Spin UI**

The 2x picker was only setting `densityExtraCompact` when the device's physical `densityDpi` was below 350. On normal/high-density phones that condition stayed false, so selecting 2x had no visible effect.

### What changed

- Made `SmartDensityMode.EXTRA_COMPACT` an explicit user-selected tier on every device, rather than gating it behind physical DPI.
- Ensured 2x selects the compact Spin layout, uses the smallest deck scale, and cannot be overridden by the roomy high-density branch.
- Preserved the existing Off and Compact behavior.
- Removed the obsolete extra-low-density threshold constant.

### Validation

- `scripts/check_braces.py` passed for `SpinScreen.kt`.
- `git diff --check` passed.
- Gradle/build commands were not run because the repository forbids local Android compilation; CI remains the compilation gate.

## Latest Request (COMPLETED)

**Fix Explore overlay crash loop after clearing app data**

The supplied log identified the exact crash when the overlay bubble was first attached:
`IllegalStateException: Composed into the View which doesn't propagate ViewTreeSavedStateRegistryOwner!` from `ComposeView.resolveComposeViewContext()`.

### What changed

- Added a service-owned `SavedStateRegistryOwner` alongside the existing lifecycle and `ViewModelStore` owners for the `TYPE_APPLICATION_OVERLAY` ComposeView.
- Attached the saved-state owner before `setContent`, and initialized its controller/lifecycle before WindowManager attaches the view.
- Added the explicit `androidx.savedstate:savedstate` dependency to the active app module.
- Guarded overlay-owner construction so a device/OEM saved-state failure falls back to the live notification instead of crashing and restarting the process.
- Updated the store changelog.

### Validation

- `scripts/check_braces.py` passed for the changed Kotlin file.
- `git diff --check` passed.
- Gradle/build commands were not run because the repository forbids local Android compilation; CI remains the compilation gate.
