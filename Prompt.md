# Prompt — Zoom overlay gesture fix (v7.38)

## Request
"after i double tap to open a image after it opens up it doesnt do the pinch to zoom or anything sometimes and also when i try to double tap to close it doesnt work its laggy and not smooth fix it"

## Root causes (MoodBoardZoom.kt)
1. **Double-tap did NOT close** — a double-tap on the magnified image ran the "double-tap zooms in one more step" spring (zoomStepTick → pinchScale ×1.5) instead of closing, so double-tap-to-close appeared broken.
2. **Laggy close** — a clean tap scheduled the close on a 300ms delayed coroutine (the double-tap disambiguation window) → close started a third of a second late.
3. **Pinch unreliable after double-tap-open** — the zoom-step spring wrote pinchScale concurrently with the user's pinch gesture (racing writes), and the movement detection used exact float equality (`zoomChange != 1f || panChange != Offset.Zero`) so micro-movements were misread as taps → a close/reset fired mid-pinch ("it didn't zoom").

## Fix (MoodBoardZoom.kt)
- **Removed** the double-tap-zoom-in machinery (zoomStepTick, stepScale Animatable + LaunchedEffect) and the 300ms delayed pending-tap path (overlayScope/rememberCoroutineScope, pendingTap Job, lastTapDownUptime, DoubleTapTimeoutMs, kotlinx.coroutines.delay/Job imports).
- **New tap semantics — immediate and predictable**: a clean tap closes (glides back to the tile, 170ms) when at base zoom; when pinched/panned it springs back to base and stays open. Double-tap closes too — the first tap already starts the close, so no timing window, no delay.
- **Movement detection hardened**: pan/zoom deltas are ALWAYS accumulated (slow drags still pan); a cumulative-pan tolerance (>4px) or any real zoom change (>0.02) or a second finger marks the gesture as movement — so a pinch/drag is never misread as a tap. Tap branch guarded with `if (zoomState.closing)` so the second tap of a double-tap-close can't start a reset spring mid-close.
- Close glide was already a fast 170ms tween — unchanged.

## Review
Reviewer clean after two edge-case fixes applied (slow-drag accumulation + closing guard).

## Status
DONE — implemented, reviewed, Prompt.md updated, committed + pushed.
