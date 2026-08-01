# Prompt.md — Running Request Log

## Latest Request — Mood Board: pin-to-front drag gesture + clear board

### Status: ✅ Complete (committed & pushed)

### Summary
Added drag-to-pin-to-front and a clear-board action to the mood board editor
(`app/src/main/java/com/curio/app/features/capture/formats/GalleryWallFormat.kt`):

1. **Pin-to-front drag zone** — dragging any tile into a translucent 52dp drop
   zone at the top of the canvas highlights the strip ("Release to pin to
   front") and, on release, brings the tile to the front of the z-order
   (same `tiles.add(tiles.removeAt(idx))` mechanism as tap-to-front). The
   strip only appears while a drag is in progress (`draggingTileId != null`),
   uses `KeyboardArrowUp` + accent colors, and sits at zIndex 500 so touches
   pass through to the dragged tile (plain Box, no pointer handlers).
2. **Clear board** — expanded (full-screen) editor only: a "Clear board" pill
   at BottomStart (errorContainer, `Delete` icon) opens an AlertDialog
   ("Remove all N images?") with Clear/Keep. Hidden when the board is empty
   (`fullScreen && tiles.isNotEmpty()`).
3. Drag gesture upgraded to full `detectDragGestures(onDragStart/onDrag/
   onDragEnd/onDragCancel)` lifecycle; `inPinZone` derived from the tile's
   offsetYPx vs pinZoneHeightPx after each drag move.

### Validation
- Braces balanced: GalleryWallFormat 117/117
- Imports: `layout.width`, `AlertDialog`, `TextButton` added, all used
- Code review: clean verdict (2 minor nits fixed: hidden clear on empty
  board, KDoc mention of pin gesture)

### Prior work (this session)
- Premium mixed-deck colors (CurioMixedDeck in CurioColors.kt, SpinScreen
  wiring) — committed `01414c20`
- Category pickers: tap-to-open default, long-press multi-select with Done,
  no tick (CurioCategoryCard, CategoryPickerScreen, SpinScreen sheet) —
  committed `395f0abf`
- Edit mood board reuses saved entry-id watermark seed (GalleryWallFormat
  boardSeed param + SaveCaptureScreen threading) — committed `c6262518`
