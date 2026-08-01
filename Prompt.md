# Request: Mood board pinch-zoom fixes + editor rotate/resize

## Analysis

User feedback (refined mid-task):
1. Board-level pinch zoom feels weird in the **editing** view — remove it there,
   but KEEP it in the **saved board view** (EntryDetail inline + expanded dialog).
2. Pinch-to-zoom works on the expanded image but NOT on the scrim area above
   the image — fix so pinch works anywhere over the overlay.
3. Add resize + rotate controls for images in the mood-board editor.

## Changes

### app/src/main/java/com/curio/app/ui/components/MoodBoardZoom.kt
- `MoodBoardZoomOverlay` and `MoodBoardZoomCanvas` restructured: previously the
  scrim Box (zIndex 1000, with detectTransformGestures + detectTapGestures) and
  the image/collage Box (zIndex 1001) were SIBLINGS — pinching the scrim around
  the image could miss the zoom handler. Now ONE Box owns scrim + gestures with
  the image/collage as a CHILD (`contentAlignment = Alignment.Center`), so every
  pointer event on the overlay reaches the same transform/tap handlers.
- `zoomBoard()`/`boardZoomed`/`moodBoardPinchZoom`/`MoodBoardZoomCanvas` kept —
  still used by the saved board view.

### app/src/main/java/com/curio/app/features/capture/formats/GalleryWallFormat.kt (editor)
- Removed `.moodBoardPinchZoom(zoomState)` from the board canvas and the
  `MoodBoardZoomCanvas` block — board pinch now only lives in the saved view.
- Removed now-unused imports (`MoodBoardZoomCanvas`, `moodBoardPinchZoom`).
- Per-tile controls added alongside the existing × Remove button:
  - ⟳ Rotate button (TopStart, `CurioIcons.Refresh`) — +15° per tap,
    `(rotationDeg + 15f) % 360f`.
  - − / + resize buttons (BottomStart Row) — shrink ×0.8 (min 60dp, offset
    re-clamped to canvas) / grow ×1.2 (clamped to canvas minus offset, so the
    tile can't overflow).

### app/src/main/java/com/curio/app/features/detail/EntryDetailScreen.kt
- UNCHANGED — saved board view keeps board pinch + board magnifier.

## Validation

- code-searcher: 0 matches for `moodBoardPinchZoom`/`MoodBoardZoomCanvas` in the
  editor; rotate/resize and zoomIn/Overlay references confirmed in place.
- code-reviewer-deepseek-flash: clean pass — brace balance, imports, Refresh
  icon exists, child buttons don't double-fire with parent tile gestures
  (awaitFirstDown requires unconsumed), resize clamp math has no empty ranges,
  `zoomBoard()`/`boardZoomed` not dead (saved view still uses them).
- No local gradle build per AGENTS.md — CI owns compilation on push.

## Status

Complete. Commit `TBD` on branch `revamp`.
