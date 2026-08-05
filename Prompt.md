# Prompt — Detail page + entry tools batch (v7.39)

## Requests
1. Zoom overlay pinch/drag "works but not smooth, acts with a delay".
2. Detail: category still doesn't show below the tear; remove the
   "Captured today · time" meta line (put a very small time on the hero's
   date card); remove the dead voice-note play icon + enlarge its text.
3. Entry page: rounded corner + watermark only work on the title boxes —
   apply to ALL text boxes.
4. Entry page tools scattered (colors / styles / text format take too much
   space) — collapse them, one tool open closes the other, and put text
   format behind a text button so it doesn't always show.

## Fixes
- **MoodBoardZoom.kt** — pinch/pan deltas are now applied PER pointer event
  inside the gesture loop (previously accumulated and applied only when all
  fingers lifted → the image moved with a delay). Tap/movement
  classification + close semantics unchanged.
- **EntryDetailScreen.kt**
  - Category tucks at the tear: meta column lift -14dp → -32dp (tip now
    grazes the torn edge); bottom padding 16→8 and body vertical padding
    16→8 keep the tags→body gap identical.
  - "Captured today · time" line removed; hero Date FrostedSegment gained
    a `tiny` line rendering the time at 9sp. capturedAtLabel() deleted.
  - Voice-note header: dead circular PlayArrow icon removed; label bumped
    titleSmall → titleMedium (the real AudioPlayerBar stays below).
- **RichTextEditor.kt**
  - Paper boxes now forward `watermark` (both bases) and `roundedTop`
    (PaperCard) from the style — same decorations as the title fields.
  - Toolbar unified: one compact row with TEXT buttons — "Paper"
    (Palette) and "Format" (FormatText) — each reveals its panel and
    opening one closes the other; the B/I/highlight/size toolbar no longer
    always shows. StyleToggleButton replaced by ToolToggleButton; stale
    KDOC updated; unused Spacer import removed.
- **MarginaliaFormat.kt** — CI fix: restored the `Row` import (my earlier
  FlowRow edit dropped it; JournalVoiceNoteRow still uses Row).

## Review
Reviewer clean after KDOC refresh (4px pan dead-zone + voice-note indent
noted as acceptable cosmetics).

## Follow-up (v7.40)
Killed the zoom overlay's 4px drag dead-zone: pan/zoom deltas are now
applied to the state on EVERY pointer event (not only after the movement
threshold trips), so the first pixels of a drag move the image immediately
when zoomed in. The tap classifier became threshold-consistent
(`pinchScale > 1.01f || totalPan > 4px`) since sub-threshold jitter now
lands in pinchX/pinchY — tap-to-close still works. Reviewer verified
single-finger zoomChange is always 1f (no creep) and pan clamps to 0 at
base zoom. Committed + pushed.

## Follow-up (v7.41) — overlay pill never shows (real root cause) + warning cleanup

### Overlay pill: root-caused from user logcat
`app/logcat.txt` showed the REAL reason the floating pill never appears:

    E/ExploreSessionService: Unable to create overlay Compose owners; using notification only
    java.lang.IllegalStateException: You can 'consumeRestoredStateForKey' only after the
    corresponding component has moved to the 'CREATED' state
        at ExploreSessionService$OverlayOwner.<init>(ExploreSessionService.kt:157)

Diagnosis (verified against AOSP source + the resolved AARs):
- The toml pins savedstate 1.3.3 but lifecycle-runtime 2.10.0 / activity
  1.13.0 pull savedstate **1.4.0** transitively, and gradle takes the max.
- savedstate 1.4.0 rewrote SavedStateRegistryImpl: `performAttach()` now
  registers the **Recreator** lifecycle observer immediately, and that
  observer calls `consumeRestoredStateForKey("androidx.savedstate.Restarter")`
  on ON_CREATE. `consumeRestoredStateForKey` is now guarded by
  `check(isRestored)` — and `isRestored` is ONLY set by `performRestore()`.
- The OverlayOwner only called `performAttach()` then drove ON_CREATE →
  Recreator fired → check failed → `showBubble()` caught it, latched
  `bubbleUnavailable = true`, and silently ran notification-only. The pill
  never shows, even after clean install.

Fix (ComponentActivity's documented contract):
    savedStateController.performAttach()
    savedStateController.performRestore(null)   // NEW
    registry.handleLifecycleEvent(ON_CREATE/START/RESUME)
`performRestore(null)` marks isRestored=true (restoredState=null), so
Recreator's consume returns null and no-ops. Null literal compiles against
both 1.3.3 (`Bundle?`) and 1.4.0 (`SavedState?`) signatures. `bubbleUnavailable`
stays as an OEM-rejection safety net.

### CI warning cleanup (from pasted build log)
- AppPreferences.kt — `@Suppress("DEPRECATION")` on overlayActuallyUsable
  (unsafeCheckOpNoThrow has no stable non-deprecated replacement).
- CaptureData.kt — `@file:Suppress("UNNECESSARY_SAFE_CALL")` (defensive ?.
  on non-null Gson-legacy fields — guards kept, warning silenced).
- CurioBackupManager.kt — `@Suppress("SENSELESS_COMPARISON","USELESS_ELVIS")`
  on restore() (legacy-blob null guards).
- MarginaliaFormat.kt — `path?.let` → `path.let` (AudioRecorder.stop():
  String non-null; dropped the dead `?: 0L`).
- PaperCard.kt — quadraticBezierTo( → quadraticTo( ×7 (deprecation message
  names the replacement; same signature).

Review: clean. Committed + pushed.
