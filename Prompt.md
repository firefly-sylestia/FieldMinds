# Prompt.md — Request Log

## Current Request: Mood board editor polish (laggy drag · ugly box outline · zoom glitch)

**User request:** "In moodboard editing the photo drag is laggy and the outline of the box around images looks bad and it glitches when I expand the image, continue this."

**User clarification (ask_user):** Tile style — **Frameless float**: no box behind images; photos float on the board with rounded corners.

## Implementation (complete)

### Laggy drag → commit-on-release preview (GalleryWallFormat.kt)
- Root cause: every drag frame mutated the `SnapshotStateList`, restarting the top-level `LaunchedEffect(tiles.toList())` → rebuilt `CaptureData.GalleryWall` and re-fired `onDataChanged` on EVERY pointer move (and recomposed every tile).
- Extracted `MoodBoardEditorTile`: the drag/pinch gesture accumulates into a per-tile `TileDragPreview` held in `remember(tile.id)` state, so per-frame writes recompose ONLY the dragged tile. The tile list is mutated once via `onCommit` when the finger lifts.
- Render clamps mirror commit clamps exactly (incl. a pre-measure canvas-size fallback) so tiles never snap or collapse on release.
- `currentTile by rememberUpdatedState(tile)` — keyed `pointerInput` never restarts, so gestures read the LIVE tile (fixed stale pin-zone base offset).
- Pin-zone UI + pin-to-front gated to single-finger drags via a new `byDrag` flag (a pure pinch no longer flashes the zone or pins on release — parity with the original).
- Dragged tile zIndex-boosts above siblings; `inPinZone` parent state written only on flip.

### Ugly box outline → frameless tiles
- Editor tiles no longer render on a hardcoded `Color.White` card — the photo itself is the tile: `size → rotate → clip(RoundedCornerShape(14.dp))` (rotate-before-clip so the rounded shape rotates intact).
- `decodeImageBounds` now reads EXIF rotation (`android.media.ExifInterface`, minSdk 26) and swaps bounds for 90°/270° so tile aspect matches Coil's rotated rendering — no letterbox bars inside tiles.

### Zoom glitch → internal spring + stacked painters (MoodBoardZoom.kt)
- Open used to POP in at 2.4x: the call-site `animateFloatAsState` initializes to its target on first composition. Overlay/canvas now animate their own `overlayScale` via `animate(1f → target, spring)` keyed on `scaleTarget` — open AND close spring smoothly; pinch retargets mid-flight. Removed `animatedScale` param from `MoodBoardZoomOverlay`/`MoodBoardZoomCanvas`; call sites updated in GalleryWallFormat.kt + EntryDetailScreen.kt.
- The overlay re-fetched the image at a bigger decode size → blank flash while it streamed in. Now stacks the board-size painter (already cached) UNDER the hi-res painter — no blank frame.
- Zoom overlay is frameless too (no surface card, no 6dp padding gap).

## Validation
- code-reviewer-deepseek-flash: 2 rounds. Round 1 found 3 issues (stale tile capture in the pin-zone check; 2-finger gestures triggering drag UI + pin-on-release; pre-measure commit clamp collapsing tiles) — all fixed. Round 2: clean, only cosmetic nits.
- grep confirmed no leftover `animatedScale` references; `git diff` scoped to 3 files.
- No gradle build per AGENTS.md (CI owns compilation on push).

## Status
DONE — committed & pushed (fix: mood board editor polish).
