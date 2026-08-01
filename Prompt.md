# Prompt.md — Request Log

## Current Request: "Edit entry" for multi-section entries

**User request:** Add an "Edit entry" option in the detail dropdown for multi-section entries so users can reopen the whole portfolio (all takes) in the universal editor, not just mood boards.

**Context:** The universal editor (`SaveCaptureScreen` + `FormatBodyForCategory`) was ALREADY fully generic — it loads any saved entry, unwraps Portfolios into takes, and saves back in place. The gap was purely routing + discoverability: only an `edit-moodboard` route existed and the detail dropdown only offered "Edit mood board" for mood-board-ish entries.

## Implementation (complete)

### Routing
- `CurioRoutes.kt`: added `EDIT_ENTRY = "edit-entry/{entryId}"` + `editEntry(entryId)` builder (alongside the existing `editMoodBoard`).
- `CurioNavHost.kt`: registered `EDIT_ENTRY` → `SaveCaptureScreen(editEntryId = entryId)` — identical to the EDIT_MOODBOARD registration (both reopen the saved entry preloaded, re-save in place via Room REPLACE).

### Detail dropdown (EntryDetailScreen.kt)
- New `isMultiSectionEntry(entry)` = `captureData is CaptureData.Portfolio` (Portfolios are by construction 2+ takes).
- Dropdown logic: Portfolio → "Edit entry" → `editEntry` route; else mood-board → "Edit mood board" → `editMoodBoard` (unchanged).
- `isMoodBoardEntry` simplified to drop the Portfolio clause (Portfolios are now handled by the "Edit entry" branch; the function's only caller is the dropdown). Direct GalleryWall and OpenNotebook-GalleryWall still get "Edit mood board".
- A Portfolio containing a GalleryWall now shows "Edit entry" — the universal editor still opens on the mood-board section (existing `activeIndex` GalleryWall-first logic).

### Docs
- `SaveCaptureScreen.kt`: KDoc updated — edit mode now covers single mood boards AND whole multi-section Portfolios.

## Validation
- code-reviewer-deepseek-flash: clean — routes match templates, no double registration, no entry type lost an edit affordance, no dead code/missing imports, universal editor confirmed to reopen every take.
- grep verified `EDIT_ENTRY`/`editEntry` wiring across routes/navhost/dropdown; git diff scoped to 4 files (51 insertions, 10 deletions).
- No gradle build per AGENTS.md (CI owns compilation on push).

## Status
DONE — committed & pushed (feat: edit entry for multi-section portfolios).
