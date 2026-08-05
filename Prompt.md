# Prompt.md — Request Log

## Latest Request (COMPLETED)

**Unify the hero's glass elements into one frosted-plate family**

### What was requested

Apply the same frosted-plate treatment to the hero's other glass elements
so the frost language is consistent. On the ask about whether the Date card
should change, the user chose: "unify with the card style, not the other
way" — the card keeps its bright frosted-white look and everything else
adopts it.

### What changed

- `EntryDetailScreen.kt` — added a shared frosted-glass helper: `heroFrostGradient`
  (now the card's near-opaque white: `Brush.verticalGradient` 0.99 → 0.94)
  and `heroFrostPlate(ink, shape)` = clip + background + 1dp hairline rim
  at ink@0.32. The title plate now uses it and the title text switched from
  `heroInk` to the card's deep-slate `heroCardInk`. The banner's back and
  more buttons dropped their solid category-surface fills for the same
  frosted plate with `heroCardInk` icons, so title + both controls + the
  Date card read as one bright-white/dark-slate glass family. Date card
  rim bumped 0.20 → 0.32 to match.
- `CurioTopBar.kt` — `CurioBackButton` gained optional `containerColor` /
  `contentColor` / `border` params (defaults unchanged — every other
  screen keeps the surfaceVariant circle); the hero passes the frosted
  plate through them.

### Validation

- `scripts/check_braces.py` passed for both changed Kotlin files.
- `git diff --check` passed.
- Code review approved both rounds; KDoc nit fixed.
- Gradle/build commands were not run because the repository forbids local
  Android compilation; CI remains the compilation gate.
- Store changelog `20260810.txt` updated.

## Latest Request (COMPLETED)

**Hero title glass effect broken + ghost overlay behind title**

### What was requested

The detail hero's title "glass frosty" effect isn't working and there's a
weird background overlay behind/with the title.

### Root cause

The old "glass title" was two stacked Text composables: a blurred 38%-alpha
copy of the title (with a heavy black TextStyle shadow) behind the crisp
copy (also with a black shadow). Blurring a duplicate of the text doesn't
read as glass — it looked like a ghost/smudge overlay behind the title, and
the drop shadows smudged the letterforms.

### What changed

`EntryDetailScreen.kt` — the two-Text aura is replaced with ONE crisp title
on a real frosted glass plate matching the Date · Mood · Type card below:
a Box with `RoundedCornerShape(20.dp)`, a `Brush.verticalGradient` frosted
white (0.28 → 0.16 alpha), a `Modifier.border` hairline rim in `heroInk`
(0.32 alpha), padding, and the title in `heroInk` @ 0.97. Reads as milky
frost on deep banners and as a defined glass plate on light pastels (the
rim carries it there). Removed the now-unused `Shadow` import; added
`androidx.compose.foundation.border`. Only one Text remains, so the old
`clearAndSetSemantics` dedup is correctly gone.

### Validation

- `scripts/check_braces.py` passed for `EntryDetailScreen.kt`.
- `git diff --check` passed.
- Code review approved; its tuning note (frost barely visible on light
  pastel banners) addressed by raising the white to 0.28→0.16.
- Gradle/build commands were not run because the repository forbids local
  Android compilation; CI remains the compilation gate.
- Store changelog `20260810.txt` updated.

## Latest Request (COMPLETED)

**Dark-mode invisible paper chips + double-confirmed Cancel in done dialog**

### What was requested

1. In dark mode the inactive paper-style buttons (fold / coffee / color
   etc.) in the entry are greyed out so much they're invisible.
2. Add an in-app Cancel button to the "Done exploring" dialog, behind a
   double confirmation.

### What changed

**PaperCard.kt** — `CompactPaperChip` (Ruled/Torn bases + +Rules / +Coffee /
+ Folded / +Red Margin decoration chips) and the `NotePaperColorToggle`
"Color" chip used the theme-agnostic dark-brown `paperInk()` at 50-55%
alpha for inactive labels — on the dark page background they vanished.
Now inactive labels flip to a warm light tan (`0xFFE0D5BC` @ 0.90) in dark
mode via the newly-imported `isCurioDarkTheme()` (the same pattern
`paperControlAccent()` already used), light mode keeps warm brown at a
slightly stronger 0.62 alpha; inactive hairline borders bumped (chips
0.18→0.24 light / 0.42 dark; Color chip 0.25→0.30 light / 0.42 dark).

**CurioNavHost.kt** — the Done-exploring dialog gains a red "Cancel
session" button next to "Keep exploring". Tapping it flips the dialog into
a confirm step (new `rememberSaveable confirmSessionCancel`): title
"Cancel this explore?", a warning that the elapsed time isn't saved, and
"Yes, cancel session" (red) which quietly clears the session, cancels the
reminder and stops the service — same teardown as the notification's Cancel
action, no write-it-down page. "Keep exploring" backs out of the confirm
step. `onDismissRequest` and the two lifecycle observers that re-show the
dialog also reset `confirmSessionCancel` so a background/foreground cycle
never reopens it mid-confirm.

### Validation

- `scripts/check_braces.py` passed for both changed Kotlin files.
- `git diff --check` passed.
- Code review approved; reviewer's edge-case note (reset confirmSessionCancel
  in the observers) was applied.
- Gradle/build commands were not run because the repository forbids local
  Android compilation; CI remains the compilation gate.
- Store changelog `20260810.txt` updated.

## Latest Request (COMPLETED)

**Frosty white gradient on the detail hero's Date · Mood · Type card**

### What was requested

The Date · Mood · Type card in the entry detail view should get a frosty
white gradient background with ~1% color blur of the background color.

### What changed

`EntryDetailScreen.kt` — the hero's frosted Date · Mood · Type card
background stack (was: a full-strength blurred solid hero color under a
flat 78% white overlay) is now:

- A **faint blurred color pane** — the hero's solid category color at 8%
  alpha, blurred 18dp and clipped to the card, so the backdrop blooms
  through as a ~1% whisper instead of a strong tint.
- A **frosty white gradient** over it — `Brush.verticalGradient` from
  White at 99% (top) to White at 94% (bottom): cleaner, brighter frosted
  glass at the top that lets just a bare hint of the category color
  breathe through toward the bottom edge, like light passing through real
  frosted glass.

The deep-slate content ink (`heroCardInk`) is unchanged and stays legible
on the whiter card. Tuned from an initial draft (78% flat white + 10%
color → ~12% bleed at the bottom) to match the requested ~1% color blur
(bottom bleed now ~5-6%, top ~1%).

### Validation

- `scripts/check_braces.py` passed for `EntryDetailScreen.kt`.
- `git diff --check` passed.
- Code review approved the approach; the reviewer's tuning note (bottom
  edge bleeding ~12% rather than ~1%) was addressed.
- Gradle/build commands were not run because the repository forbids local
  Android compilation; CI remains the compilation gate.
- Store changelog `20260810.txt` updated.

## Latest Request (COMPLETED)

**Skip the notification-permission dialog when the bubble shows the timer**

### What was requested

Don't show the POST_NOTIFICATIONS dialog when the floating bubble is on and
notifications are off — the extra prompt is redundant.

### What changed

`TopicRevealScreen.kt` — the notification prompt is now skipped whenever
the floating bubble will show the timer (bubble enabled AND "Display over
other apps" granted), because the bubble needs only the overlay permission
and a shade notification would be redundant:

- `beginExploreSession` computes `bubbleWillShow = isOverlayBubbleEnabled &&
  Settings.canDrawOverlays(context)` and gates `needsNotification` on
  `!bubbleWillShow`.
- `continueExploreFlow` re-checks the same gate after the overlay-permission
  step resolves, so returning from the overlay settings page with the grant
  lands straight in the browser instead of popping a second permission
  dialog.

Flow behavior: bubble-on + overlay-granted → no notification dialog even
when POST_NOTIFICATIONS is denied; overlay-missing → overlay prompt first,
and the notification dialog fires only if the user declines the overlay
(the shade notification is then the only timer controller); bubble-off →
unchanged. The Settings toggle's own user-initiated request and the
onboarding startup request are untouched.

### Validation

- `scripts/check_braces.py` passed for `TopicRevealScreen.kt`.
- `git diff --check` passed.
- Code review approved (all flow cases traced; minor optional note about
  2-line `bubbleWillShow` duplication, left as-is for minimality).
- Gradle/build commands were not run because the repository forbids local
  Android compilation; CI remains the compilation gate.
- Store changelog `20260810.txt` updated.

## Latest Request (COMPLETED)

**Floating explore bubble not appearing even with permissions granted**

### What was requested

The floating bubble isn't appearing at all now, even after "Display over
other apps" is properly allowed.

### Root causes (two)

1. **Notification-permission gate killed the whole service.** In
   `TopicRevealScreen.beginExploreSession`, when the overlay permission IS
   granted but POST_NOTIFICATIONS (Android 13+) is missing, the flow showed
   the notification-permission dialog and its callback only started
   `ExploreSessionService` when the grant landed (`granted &&
   isLiveNotificationsEnabled`). Denying notifications — a permission the
   bubble does NOT need — meant the service never started → no bubble, no
   notification, even with the overlay permission granted.
2. **Invisible bubble after a process restart.** The bubble's composition
   read only the reactive `ExploreSessionStore.activeSessionState`, seeded
   by MainActivity. A system re-arm (boot receiver, START_STICKY after
   process death) starts the service in a process where MainActivity never
   ran → the state was null → the overlay window composed NOTHING (a
   zero-size invisible bubble).

### What changed

- `TopicRevealScreen.kt` — the notification-permission callback now starts
  the service whenever `AppPreferences.exploreServiceShouldRun(context)` is
  true, regardless of the notification grant; the service's `render()` picks
  what actually shows from the current permission state. Denying
  POST_NOTIFICATIONS no longer suppresses the bubble. (Side benefit: this
  also fixed the sibling path where live-notifs OFF + bubble ON + notification
  granted never started the service.)
- `ExploreSessionService.kt` — `onCreate()` now seeds the reactive store
  (`ExploreSessionStore.seed(this)`), so a fresh-process re-arm has a seeded
  session and pause/resume/hide recomposition works; the bubble composition
  also falls back to the persisted session (`getActiveSession`) if the
  reactive state is somehow still null, instead of composing an empty overlay.

### Validation

- `scripts/check_braces.py` passed for both changed Kotlin files.
- `git diff --check` passed.
- Code review approved both fixes.
- Gradle/build commands were not run because the repository forbids local
  Android compilation; CI remains the compilation gate.
- Store changelog `20260810.txt` updated.

## Latest Request (COMPLETED)

**Pinch-to-zoom for zoomed images + full-screen expand for small ones**

### What was requested

Make zoomed images pinch-to-zoomable and let images that are too small be
expanded, since tiny collage tiles are hard to pinch inside the card.

### What changed

All in `MoodBoardZoom.kt` (v7.30):

- `MoodBoardZoomOverlay` (detail image strip, saved boards, expanded
  boards, editor magnifier) gained a top-end **expand button** next to the
  dismiss button. It lifts the zoomed image into a new full-window viewer;
  the in-place overlay stays composed behind it, so closing the viewer
  returns to the in-place zoom.
- New private `FullScreenImageViewer` — a black full-window `Dialog`
  (`usePlatformDefaultWidth=false`, `decorFitsSystemWindows=false`) with
  the same hi-res Coil painter (2048px decode), fit-to-screen, pinch-to-zoom
  1–8x + one-finger pan, a close button, and a "Pinch to zoom · drag to
  pan" hint. The pan clamp is **derived from the window size × (scale−1)**
  (via `onSizeChanged`), so the pan range always matches the screen — zero
  at 1x (can't drag the image off-center at rest) and it tightens as you
  pinch back out, auto-recentering the image.
- `MoodBoardTiles` (saved boards) now renders a small dark expand chip on
  each zoomable tile's bottom end — one tap opens the same full-screen
  viewer directly, so even the tiniest collage tiles are one tap away from
  a big pinch canvas (the user's exact pain point). Chip taps don't
  trigger the tile's double-tap zoom (the chip's clickable consumes the
  down).

### Validation

- `scripts/check_braces.py` passed for `MoodBoardZoom.kt`.
- `git diff --check` passed; `CurioIcons.Fullscreen`/`Close` confirmed to
  exist.
- Code review approved; reviewer-flagged issue (hardcoded ±1200 pan clamp
  → off-center drift at 1x) fixed by the size-derived clamp.
- Gradle/build commands were not run because the repository forbids local
  Android compilation; CI remains the compilation gate.
- Store changelog `20260810.txt` updated with the feature entry.

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

**Revert title gradient/plate + mood-board double-tap zoom without expand button**

User: (1) disliked the title's gradient font style — change it; (2) doesn't want the background card (frosted plate) behind the title; (3) apply pinch-to-zoom etc. in the mood board's double-tap zoom; (4) don't add another expand button.

### Title (EntryDetailScreen.kt)

- Removed the carved-glass treatment: the icy vertical-gradient brush + TextStyle shadow on the title, AND the frosted-white plate Box behind it (heroFrostPlate). The title is now a plain Text — `headlineMedium.copy(fontWeight = ExtraBold)`, `color = heroInk` (the banner's onAccent ink: white normally, deep in pastel), centered, directly on the banner.
- Removed now-unused imports `androidx.compose.ui.graphics.Shadow` and `androidx.compose.ui.graphics.drawscope.Stroke` (both only lived in the old title treatment; import-usage audit confirmed zero remaining references). `heroFrostPlate`/`heroFrostGradient`/`Brush` stay (back/more buttons + the frosted Date card).

### Mood board (MoodBoardZoom.kt)

- Removed the v7.30 full-screen expand path: the one-tap expand chip on `MoodBoardTiles` tiles (+ its `expandedUri` state + trailing viewer call), the expand button in `MoodBoardZoomOverlay`'s top-end controls (+ `expanded` state + trailing call; top-end is now just the single ✕ Surface), and the entire dead `private fun FullScreenImageViewer`.
- The overlay's built-in pinch-to-zoom (1-8x on top of the fit zoom, up to ~40x total) + one-finger pan + double-tap reset remain — the double-tap zoom is now the sole zoom experience, no extra button.
- Polish per reviewer: the overlay's pan is now CLAMPED to the viewport (same rule the removed viewer used: `maxPan = (tileSize × scale − viewport)/2`, zero at rest) so a tiny tile can never be dragged off-screen and pinching back out recenters.
- Imports cleaned: removed background, detectTransformGestures, navigationBarsPadding, statusBarsPadding, onSizeChanged, Dialog, DialogProperties, TextOverflow (was genuinely unused pre-change), CurioIcons, Arrangement, Row, Color; RE-ADDED TextOverflow because MoodBoardFloatingCards uses `TextOverflow.Ellipsis` (caught by the audit).
- Kept: EntryDetailScreen's pre-existing full-collage expand button (CurioIcons.Fullscreen, opens the whole-board dialog) — a separate feature from the per-image expand the user rejected.

### Validation

- `scripts/check_braces.py` passed (both files); `git diff --check` passed; import-usage audit clean; no leftover references to expandedUri/expanded/FullScreenImageViewer/Shadow(/Stroke(.
- Reviewer confirmed all four requirements met; its pan-clamp suggestion was applied.
- Gradle/build commands were not run because the repository forbids local Android compilation; CI remains the compilation gate.
