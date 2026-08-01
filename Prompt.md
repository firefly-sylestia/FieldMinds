# Prompt.md — Request Log

## Current Request: Universal multi-section capture ("Save your take" redesign)

**User request:** Redesign the capture flow so the "How do you want to capture this one?" format picker is UNIVERSAL for every category (Wildcard's pick-any style), with the category's dedicated format pre-selected as the default. Users can add MULTIPLE different takes (sections) that all save into ONE entry, and the detail page shows a compact section switcher (never merged into a single page). Default stays pre-selected, picker must be compact.

**User clarifications (ask_user):**
1. Storage: "one entry multiple sections" — a single entry holds several sections, with an option to switch between them on the detail page (not shown as one merged page).
2. Picker: default format pre-selected; a compact picker/selector that doesn't take much space.

## Implementation (complete)

### Data model
- `CaptureData.kt`: added nested `CaptureSection(format, data, title?)` + `CaptureData.Portfolio(sections)` container. Added `toPreview()`/`toFullContent()` branches ("N takes · Voice + Journal") and `audioFilePaths()` recursion for delete/backup flows.
- `CurioTopic.kt`: added top-level `val CaptureFormat.shortName` (Voice/Review/Journal/Moodboard/Field notes/Wildcard) used by picker chips + detail switcher + previews.
- `CaptureEntity.kt`: `deserializeCaptureData` now detects the `sections` key and reconstructs Portfolio sections recursively (format via `CaptureFormat.valueOf`, nested data via recursive deserialize, optional title). Ordering safe: Portfolio (no `subFormat` key) is caught inside the `subFormat == null` branch before per-type field checks.
- `CurioBackupManager.kt`: `audioPathOrNull()` + `withAudioPath()` recurse through Portfolio sections.

### Editors (edit/section-switch preload)
- `SoundBite/ReelNotes/Marginalia/FieldNotesFormat.kt`: added `initialData: CaptureData.X? = null`, seeded via `remember(initialData)` so re-saving an entry restores saved content (fixes the old "edit wipes content" class of bugs for non-moodboard formats).
- `SoundBiteFormat.kt`: added `onBusyChange` so the picker can confirm before switching format on a LIVE recording; trimmer auto-open gated on `!restoredRecording`.

### Universal picker (SaveCaptureScreen.kt)
- `FormatBodyForCategory` rewritten as universal multi-section body: compact chip row of the 5 concrete formats (category default pre-selected; Wildcard → Voice), section tab chips with add/remove, active section editor under `key(current.id)`, `snapshotActive()` preserves per-section content on switch.
- Save aggregates: 1 section → bare data (backward compatible); 2+ → `CaptureData.Portfolio`.
- `persistAudioDeep` (recursive audio persist through Portfolio/OpenNotebook) + `formatOf(data)` (first section's format for the entry column).
- Format switch on a filled/live take → confirmation dialog.
- Edit mode title generalized: "Edit entry".

### Detail page (EntryDetailScreen.kt)
- `FormatBody` dispatches Portfolio → new `PortfolioRender`: compact chip switcher (formatGlyph + shortName), `rememberSaveable` active index, renders the active section via recursive `FormatBody` on a sub-`CurioEntry`.
- `isMoodBoardEntry` also true for Portfolio containing a GalleryWall section.
- Delete flow uses `captureData.audioFilePaths()`.

### Misc
- `CurioTopicCard.kt`: `formatGlyph` private → internal for reuse.

## Validation
- code-reviewer-deepseek-flash reviewed main change + busy-state delta (2 review rounds) — clean, 2 issues found & fixed (trimmer auto-open on preload; silent wipe on format switch → confirm dialog + busy guard).
- code-searcher verified all imports/usages wired; no dangling refs.
- No gradle build per AGENTS.md (CI owns compilation on push).

## Status
DONE — committed & pushed (feat: universal multi-section capture).
