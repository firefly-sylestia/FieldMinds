# Request: Mood-board zoom — double-tap to open, adaptive scale

## Request
In the saved mood board: (1) opening an image should use double-tap (as the hint says), not single tap; (2) small images don't zoom in enough; (3) big images open up in an overly large view.

## Changes
- `MoodBoardZoom.kt`:
  - `MoodBoardZoomState.zoomIn` now takes tile + viewport dims `(uri, tileW, tileH, viewW, viewH)` and computes a **fit-based** default scale via `fitZoomScale` = `(minOf(viewW/tileW, viewH/tileH) * 0.9f).coerceIn(1.1f, 5f)` (2.4f fallback when dims unknown) — small tiles zoom in up to 5x, while a tile that already fills the board opens at essentially its fit size (straight + slight lift) instead of exploding past the screen.
  - New `defaultScale` field — `resetZoom()` (double-tap on the zoomed image) springs back to the fit-based default instead of a hardcoded 2.4x.
  - `applyPinch` max clamp raised 4f → 8f so small images can be pinched in further.
  - `MoodBoardTiles` now opens tiles on **double-tap** (matching the editor gesture) — `onTileZoom` callback type widened to `((String, Float, Float, Float, Float) -> Unit)?` and passes `(uri, tile.widthPx, tile.heightPx, canvasWPx, canvasHPx)`.
- `EntryDetailScreen.kt` — both `MoodBoardTiles` call sites (inline board + expanded dialog) pass the new dims (`canvasW/canvasH` and `boardW/boardH`); hint text updated to "Double-tap a tile to zoom · pinch to magnify".
- `GalleryWallFormat.kt` — editor tile `onZoomIn` widened to `(String, Float, Float, Float, Float) -> Unit`; double-tap and the search button pass `(uri, tile.widthPx, tile.heightPx, canvasW, canvasH)` so the editor zoom is fit-based too.

## Validation
- Code reviewer passed 2 passes. One refinement applied as prescribed: the `fitZoomScale` floor was lowered 1.4f → 1.1f so a near-full-board tile opens at fit size instead of still overflowing the viewport (directly targeting the "opens up in a large view" symptom). All call sites, dims, and scopes verified; reviewer confirmed ready to push.

## Completion summary
- Committed & pushed.
