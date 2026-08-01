# Prompt — Running Request Log

## Current Request

Mood-board polish bundle (garbled original, parsed + confirmed via ask_user):
1. Tap-to-zoom is buggy — single taps delayed by the double-tap timeout sometimes.
2. When the mood board panel is NOT expanded, don't allow board-level pinch zoom.
3. In the editor: remove board pinch (already gone), add two-finger pinch = resize + twist = rotate replacing the ⟲ / − / + buttons.
4. The rounded box around images should be tight to the photo (size tiles to the photo's own aspect ratio, remove inner padding).

## Status: COMPLETE

## What was done

1. **MoodBoardZoom.kt**
   - `MoodBoardTiles`: `detectTapGestures` changed from (onTap + onDoubleTap both zoom) to **onTap-only** → single taps zoom immediately (no ~300ms double-tap delay). Double-tap on a saved tile now = two onTaps (same uri, idempotent) — acceptable.
   - Removed the inner `.padding(6.dp)` on the tile Image so the image fills the rounded box edge-to-edge (clip 14→18.dp to match the Surface).

2. **EntryDetailScreen.kt**
   - Inline (non-expanded) board in `GalleryWallRender`: removed `.moodBoardPinchZoom(zoomState)` → board-level pinch zoom now only in the expanded dialog. Inline `MoodBoardZoomCanvas` block is now unreachable (harmless dead code; `zoomBoard()` only set by the pinch modifier).

3. **GalleryWallFormat.kt** (editor)
   - Replaced the tile's `detectDragGestures` with a **custom `awaitEachGesture` handler**: 1 finger drags (move + pin-zone preserved, gated on `viewConfiguration.touchSlop` so a jittered tap doesn't flash the pin zone); 2+ fingers → `calculateZoom()` / `calculateRotation()` resize + rotate the tile, clamped to canvas / 60dp min.
   - **Removed the ⟲ rotate button and − / + resize Row** (now gestures).
   - **New-tile sizing**: `decodeImageBounds(context, uri)` (BitmapFactory `inJustDecodeBounds`) → landscape anchors width, portrait anchors height, clamped 80dp..320dp, so `ContentScale.Fit` fills the rounded box with no bars/cropping.
   - Removed the Image inner `.padding(6.dp)` (clip 14.dp kept).
   - Imports: added `awaitEachGesture`, `awaitFirstDown`, `calculateRotation`, `calculateZoom`, `Context`, `BitmapFactory`, `Uri`; removed `detectDragGestures`.
   - Added `private fun decodeImageBounds` helper at file end.

## Validation

- code-searcher confirmed all edits landed and no dangling references.
- code-reviewer-deepseek-flash reviewed twice (main change + touch-slop delta): both clean. Notes accepted: editor drag now touch-slop gated (reviewer-recommended fix applied); inline boardZoomed block is dead-but-harmless; overlay keeps double-tap-delayed tap-to-close (intentional from prior request); extreme-aspect photos still letterbox after clamp (edge case).
- No local gradle build per AGENTS.md — CI owns compilation on push.

## Commit

- `feat: mood board — instant tap-to-zoom, board pinch only when expanded, editor two-finger resize/rotate gestures, aspect-tight tiles`
