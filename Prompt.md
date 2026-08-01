# Prompt.md — Running Request Log

## Latest Request — "No option to edit mood board after saving; mood board watermarks overlap; dark-mode watermarks barely visible"

### Status: ✅ Complete (about to commit & push)

### The three asks
1. **Mood board editing after saving** — user clarified the real bug: there was
   NO visible option to edit a saved mood board.
2. **Mood board watermarks overlapping** — the seeded backdrop used fully
   random bias positions, so glyphs could land on top of each other.
3. **Dark-mode watermarks barely visible** — base alphas (0.05–0.15) read as
   nothing on the midnight surface.

### Fixes
1. **Edit option now reachable two ways (EntryDetailScreen.kt)**
   - New `isMoodBoardEntry()` helper: true for direct `GalleryWall` OR a
     Wildcard `OpenNotebook` whose `subFormat == GalleryWall`. The ⋮ menu's
     "Edit mood board" item now shows for BOTH (previously only direct
     GalleryWall).
   - Added a visible ✏️ Edit button on the saved board itself (top-start,
     mirroring the expand button) that navigates to `editMoodBoard(entry.id)`.
2. **Edit preload for Wildcard mood boards (OpenNotebookFormat.kt +
   SaveCaptureScreen.kt)**
   - `OpenNotebookFormat` gained `initialData: CaptureData.OpenNotebook?` +
     `boardSeed: Int?`; the picker preselects the saved sub-format
     (`fromCaptureFormat` inverse map) and preloads `subData` into the
     GalleryWall sub-body, keyed on `remember(initialData)` so the async
     edit-entry load re-initializes state.
   - `subCanSave` gated on `canPreload` (subFormat == GalleryWall) so
     non-preloadable sub-bodies never silently wipe stored content.
   - `FormatBodyForCategory` passes `initialData` + `boardSeed` through.
   - Re-save keeps the OpenNotebook wrapper (`existingEntry.copy`) so format
     stays stable and `isMoodBoardEntry` still detects it.
3. **No-overlap mood board backdrop (CurioWatermarkBackdrop.kt)**
   - `CurioMoodBoardBackdrop` now draws a seeded SUBSET of 14 fixed, sparse
     ring slots (9–11 glyphs) + tiny jitter — glyphs can never share a slot,
     sizes capped 46–84dp. Same seed contract (entry-id hash) preserved.
4. **Dark-mode visibility raised everywhere**
   - `CurioWatermarkBackdrop`: active 0.15→0.22, inactive 0.07→0.11 (dark);
     light 0.26→0.30 / 0.12→0.15.
   - `CurioMoodBoardBackdrop`: dark base 0.05→0.10, light 0.10→0.14 (× boost).

### Validation
- Code review (deepseek-flash): compile-safe — `BiasAlignment.horizontalBias/
  verticalBias` are public vals, `shuffled(Random)` + `Float.coerceIn` are
  stdlib, M3 `Surface(onClick=)` already used elsewhere, `remember(initialData)`
  handles async load, re-save preserves wrapper. Reviewer feedback applied:
  deduped the doubled `WatermarkGlyph` KDoc and gated `subCanSave` on
  preload-capable sub-formats.
- No gradle build run (per AGENTS.md, CI owns compilation).

### Prior work (this session)
- Mixed-deck identity + blends — committed earlier
- Mood board pin-to-front / clear board / edit seed reuse — committed earlier
