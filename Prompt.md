# Request: Remove dark zoom overlay + add top dismiss button

## Analysis

User: "why theres a dark overlay though i dont want that overlay remove it and
also at the top add a dismiss for the zoom or if the image is open"

The mood-board zoom overlays (`MoodBoardZoomOverlay` for a single image and
`MoodBoardZoomCanvas` for the whole board) both painted a dark scrim
(`Color.Black.copy(alpha = 0.55f)`) over the entire canvas behind the zoomed
content. The user wants that scrim gone and a visible × dismiss button at the
top of the zoom.

## Changes (app/src/main/java/com/curio/app/ui/components/MoodBoardZoom.kt)

1. Removed `import androidx.compose.foundation.background` (no longer used).
2. Added `com.curio.app.ui.theme.CurioIcon` + `CurioIcons` imports.
3. `MoodBoardZoomOverlay` (image zoom): removed the `.background(...)` scrim
   from the gesture box; added a top-right dismiss Surface
   (`CurioIcons.Close`, `zoomState.zoomOut()`, surface.copy(alpha=0.9f),
   TopEnd + 12dp padding, 36dp) as a child of the gesture box.
4. `MoodBoardZoomCanvas` (board magnifier): same — scrim removed, dismiss
   button added.

Gesture behavior preserved: the overlay box is transparent but still
hit-testable via its pointerInput handlers, so tap-anywhere closes and
pinch/pan refine the zoom; the child Surface(onClick) consumes its own tap
(no double-fire with the parent detectTapGestures — and both call the
idempotent zoomOut() anyway).

## Validation

- code-searcher: 0 matches for `background(Color.Black` / background import
  in the file; CurioIcon/CurioIcons.Close confirmed at lines 45-46, 349-351,
  447-449.
- code-reviewer-deepseek-flash: clean pass — brace balance, imports resolve,
  gesture conflict harmless, transparent overlay still captures events,
  dismiss covers the board's expand button (zIndex 1000 vs 999) while zoomed
  (desirable). Optional nit only: dismiss Surface duplicated in both overlays
  (matches existing inline style).
- No local gradle build per AGENTS.md — CI owns compilation on push.

## Status

Complete. Commit `TBD` on branch `revamp`.
