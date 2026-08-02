# Current Request

## Status: COMPLETED — committed and pushed to `revamp`

"The mood board pinch-to-zoom is delayed — it only zooms/minimizes after I
stop the gesture. Fix it without breaking anything else."

## Root cause

The zoom overlays rendered scale through an internal **spring chase**:
`overlayScale` was animated toward `zoomState.scaleTarget` on a stiffness-280
spring (`LaunchedEffect(scaleTarget) { animate(...) }`), and the call-site
pan offsets used `animateFloatAsState` with the same spring. During a pinch,
every pointer event retargeted the spring, so the render lagged behind the
fingers and only caught up after the gesture stopped — the reported delay.

## Change (3 files)

**`app/src/main/java/com/curio/app/ui/components/MoodBoardZoom.kt`**
- `MoodBoardZoomState` gains `gestureActive: Boolean` — set `true` in
  `applyPinch` only on a real move (`zoom != 1f || pan != Offset.Zero`) so
  the landing/engage event stays clear and the open spring still plays;
  cleared (`false`) in `zoomIn`/`zoomBoard`/`zoomOut`/`resetZoom` so
  open/close/reset still spring.
- `applyPinch` gains optional `visualScale` — the first real move of a fresh
  gesture re-anchors to the caller's live on-screen scale before
  compounding, so a pinch that starts while the open/close spring is still
  settling continues smoothly from where the image actually is (no jump).
- `MoodBoardZoomOverlay` + `MoodBoardZoomCanvas`: the `overlayScale`
  `LaunchedEffect` keys on `(scaleTarget, gestureActive)` — **SNAPS**
  `overlayScale = scaleTarget` while pinching (1:1 finger tracking), springs
  otherwise. Both pass their `liveScale` (`rememberUpdatedState`) into
  `applyPinch`.

**`app/src/main/java/com/curio/app/features/capture/formats/GalleryWallFormat.kt`**
- Editor zoom offsets: `animationSpec = if (gestureActive) snap() else
  spring(0.8, 280)`; `snap` imported.

**`app/src/main/java/com/curio/app/features/detail/EntryDetailScreen.kt`**
- Saved board + expanded board zoom offsets: same `snap()`-while-pinching
  spec (x2 offset pairs); `snap` imported.

## Review
- code-reviewer-deepseek-flash (x2): clean. Verified `snap()` valid for BOM
  2026.05.01, closing latch + board auto-close still fire, engage event
  leaves `gestureActive` false so the open spring plays, no dead imports.
  Non-blocking notes (accepted): the board-canvas pinch via
  `moodBoardPinchZoom` (a Modifier extension) can't pass `liveScale`, and a
  landing event with `calculateZoom() != 1f` could skip the board open
  spring — both cosmetic, consistent with pre-existing landing math.

## CI
- Compile gate = GitHub Actions on push (per AGENTS.md — no local Gradle).
