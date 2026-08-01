# Prompt.md — Running Request Log

## Latest Request — Mood board: double-tap zoom centered+straight, pinch on the board itself, fix blurry imported images

### Status: ✅ Complete (about to commit & push)

### User asks (4)
1. Double-tap-to-zoom on saved mood board tiles (detail view), matching the editor.
2. When it zooms in, make it **centered and straight** (no tile-offset morph, no rotation).
3. Full pinch-to-zoom on the **mood board itself**, not just the images — and after an
   image is zoomed in, pinch again to zoom further.
4. Imported images look low-quality when zoomed — fix it.

### Root causes
- The zoom overlay placed the magnified image at the tile's own board offset with its
  rotation (the "morph from tile position" treatment) — not centered/straight.
- Coil's `rememberAsyncImagePainter(uri)` decoded at the composable's on-screen size
  (~160dp), so 2.4–4x zoom upscaled a tiny bitmap → blurry.
- Pinch only existed per-tile (`moodBoardPinch`) — no board-level magnifier.

### Fixes
**MoodBoardZoom.kt (rewritten):**
- `MoodBoardZoomState` gained `boardZoomed` + `zoomBoard()`; `applyPinch(uri: String?)`
  (null = whole board). `zoomIn`/`zoomBoard` are mutually exclusive.
- `moodBoardPainter(uri, zoomed=false)` — Coil `ImageRequest.Builder(...).size(cap, cap)`:
  1024px for tiles, 2048px for the magnified overlays → crisp zoom (Coil 2.7.0).
- `Modifier.moodBoardPinchZoom(zoomState)` — two-finger pinch on the CANVAS (replaces
  per-tile `moodBoardPinch`): engage → `zoomBoard()`, then `applyPinch(null, …)`.
- `MoodBoardTiles(tiles, canvasW, canvasH, onTileZoom, zoomed)` — shared static collage
  renderer (offsets/rotations, tap + double-tap → zoom) used by saved view, expanded
  dialog and the magnifier.
- `MoodBoardZoomOverlay` — **signature slimmed** (dropped offset/rotation params); the
  magnified image is now CENTERED + STRAIGHT, spring-scaled, high-res; pinch/pan refine
  up to 4x; tap closes.
- `MoodBoardZoomCanvas` — NEW whole-board magnifier: scrim + pinch-to-zoom-further/pan,
  tap closes, auto-closes when pinched back to 1x; renders the collage high-res centered.

**EntryDetailScreen.kt:** inline board + expanded dialog use `moodBoardPinchZoom` on the
board container, `MoodBoardTiles` (with tap/double-tap zoom), `MoodBoardZoomCanvas` when
boardZoomed, and the slimmer `MoodBoardZoomOverlay`. Expanded dialog computes scaled tile
layouts for the magnifier; overlays are siblings of the centering Box (scrim no longer
shifted by the centering offset — fixes a pre-existing quirk).

**GalleryWallFormat.kt (editor):** tiles container gets `moodBoardPinchZoom`; tile images
use `moodBoardPainter` (higher decode); added `MoodBoardZoomCanvas` (board magnifier);
updated overlay call.

### Validation
- Code review (deepseek-flash): clean. Verified Coil 2.7 `ImageRequest.Builder.size(Int,Int)`
  + `rememberAsyncImagePainter(model=ImageRequest)`; `CaptureData.TileLayout` named args
  match existing property names; no leftover references to removed `moodBoardPinch`/old
  overlay params; imports correctly retained (pointerInput/detectTapGestures/rotate/
  rememberAsyncImagePainter still used by WaveformCanvas/MarginaliaRender/FieldNotesRender);
  zoomIn/zoomBoard mutual exclusion + closing-latch interplay sound (auto-close can never
  fire on open — zoomBoard writes scaleTarget=2.4 in the same snapshot).
  Non-blocking notes: editor tile-drag may jiggle one frame when a 2-finger pinch starts
  on a draggable tile (scrim covers immediately); 1024/2048 decode ≈ 48MB worst-case for a
  12-tile board in Coil's cache (acceptable for the quality ask).
- No gradle build run (per AGENTS.md, CI owns compilation).

### Prior work (this session)
- Mixed-deck gradients (HSL-interpolated stops) + category-pick navigation fix — committed
- Edit-mood-board bug fixes — committed earlier
- Mood board pinch-to-zoom + in-place zoom — committed earlier
