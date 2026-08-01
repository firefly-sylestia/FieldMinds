# Mood Board — Watermark Backdrops, Expanded Editing, Edit Saved Boards

## Request

User (design direction): "the mood board background doesn't have anything it shows blank and expanding too it shows black screen so change it to theme aware watermark background and use a random pattern for each one. and in entry mood board it looks bad pixelated and cropped and in saved view mood board it looked fine so use the same logic and add expanded view so user can add things properly, and also add an edit function for the saved mood board too."

## Analysis

- `GalleryWallFormat` (capture editor): blank `surfaceVariant` canvas; tiles rendered `ContentScale.Crop` (pixelated/cropped) vs the saved view's `Fit` + padding (looked fine); no expanded editing surface.
- `EntryDetailScreen.GalleryWallRender` + `ExpandedMoodBoardDialog`: flat `surfaceContainerHigh` / pure **black** backgrounds — the black screen complaint; no edit entry point.
- No edit flow existed: `SaveCaptureScreen` only created new entries; `EntryDetailScreen` loaded once via one-shot `getById` (stale after edits); no `edit-moodboard` route.

## Plan

1. **`CurioWatermarkBackdrop.kt`** — new `CurioMoodBoardBackdrop(seed, accent, modifier)`: deterministic per-seed random scatter of 9–12 category glyphs (bias/corner placements, random sizes/rotations/tints, per-glyph `alphaBoost`), theme-aware base alpha applied at draw time (so Light/Dark toggles re-render without re-seeding).
2. **`GalleryWallFormat.kt`** — shared `MoodBoardCanvas` (inline card + full-screen `Dialog`); tiles now `ContentScale.Fit` + `padding(6.dp)` + clip (matches saved view); watermark backdrop behind tiles; expand/collapse button (`Fullscreen`/`Close`); optional `initialData: CaptureData.GalleryWall?` preloads tiles+caption for edit mode (`remember(initialData)` keys, index-based tile ids, new ids = `maxOfOrNull+1`).
3. **`EntryDetailScreen.kt`** — `GalleryWallRender` + `ExpandedMoodBoardDialog` render `CurioMoodBoardBackdrop(seed = entry.id.hashCode())` over theme background (no more black); hint text color now `onBackground`; "Edit mood board" dropdown item for `GalleryWall` entries; entry loading switched to `repo.observeAll().collect` flow (live-updates after edit/delete).
4. **`SaveCaptureScreen.kt`** — `editEntryId: String?` param: loads entry, derives category (`fallbackCat` always-remembered to avoid conditional-remember hazard), preloads format via `initialData`, saves in place (same id → Room REPLACE), pops back (detail observes flow); title/label switch ("Edit mood board" / "Save changes").
5. **`CurioRoutes.kt` / `CurioNavHost.kt`** — `EDIT_MOODBOARD = "edit-moodboard/{entryId}"` + `editMoodBoard(entryId)` builder; route registered → `SaveCaptureScreen("", "", editEntryId)`.

## Completion Summary

- 6 files changed; validation green: braces balanced (Watermark 17/17, GalleryWall 89/89, EntryDetail 225/225, SaveCapture 66/66, Routes 19/19, NavHost 44/44); no stale refs; imports verified (`RectangleShape`, `SnapshotStateList`, `kotlin.random.Random`, `CurioMoodBoardBackdrop`, `isCurioDarkTheme`, `CurioIcons.Edit`).
- Code review: clean pass; one fix applied — `CurioMoodBoardBackdrop` originally captured `isDark` inside the `remember(seed, …)` block (theme toggles wouldn't re-render alpha); moved to draw-time base alpha × seeded per-glyph `alphaBoost`.
- Reviewer notes accepted as non-blocking: fullscreen dialog keeps the inline canvas composed underneath (minor double-composition); edit screen re-seeds the backdrop pattern vs the detail view's id-derived seed.
- Store changelog `20260730.txt` updated. Gradle build/lint NOT run (forbidden in this environment; CI validates on push).
