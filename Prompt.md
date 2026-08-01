# Current Request

## Status: COMPLETED — committed and pushed to `revamp`

"Extend the removal confirmation to the per-tile × inside the GalleryWall
mood board editor too, so single-take mood-board edits also confirm before
deleting a tile."

## Change (1 file)

**`app/src/main/java/com/curio/app/features/capture/formats/GalleryWallFormat.kt`**
- `MoodBoardCanvas` gains `pendingRemoveTileId: Int?` state.
- The per-tile × `onRemove` callback now sets `pendingRemoveTileId` instead
  of deleting directly.
- New AlertDialog ("Remove this image?" / **Remove** in error color /
  **Keep**) deletes the tile on confirm — mirrors the existing
  `showClearConfirm` dialog pattern. Reset on dismiss, Keep, and Remove.
- This covers single-take mood-board edits (where no take-tab × exists) —
  the × in both the inline canvas and the full-screen expanded editor route
  through the same dialog.
- Always-confirms (not gated on edit mode): consistent with the existing
  unconditional "Clear board" confirmation, and a tile is always content.

## Review
- code-reviewer-deepseek-flash: clean — dialog mirrors showClearConfirm,
  imports already present (AlertDialog/TextButton/MaterialTheme), removal
  logic matches old direct path (indexOfFirst by id + removeAt), reset paths
  complete, dialogs mutually exclusive. Non-blocking note (accepted):
  always-confirm vs edit-mode-only — kept always-confirm to match Clear
  board precedent and because tiles are always content.

## CI
- Compile gate = GitHub Actions on push (per AGENTS.md — no local Gradle).
