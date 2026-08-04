# Prompt.md — Request Log

## Latest Request (COMPLETED)

**Mood-board NaN crash, inline editor stability, and Explore overlay crash**

### What was requested

Fix the mood-board crash reported from the device log (`Cannot round NaN value` in `GalleryWallFormat.kt`), and make the small inline mood-board editor stay centered and keep its existing cropped-preview behavior while images are moved. Also fix the Explore overlay service lifecycle crash seen in the supplied logs.

### What was done

- Sanitized mood-board tile positions, sizes, gesture deltas, scale, rotation, board bounds, offsets, and zoom geometry before arithmetic and layout conversion.
- Guarded the `IntOffset` `roundToInt()` inputs so malformed or pre-measure geometry cannot pass `NaN` into Compose placement.
- Kept the inline board's centered crop and stopped movement from recalculating the crop extent: the inline board extent is captured/grown on tile-count changes, not on drag coordinates, so the viewport does not shrink or jump while images move.
- Kept tile movement clamped to the visible raw board bounds and preserved full-screen editing as the raw 1:1 placement view.
- Removed the invalid saved-state owner construction from the overlay service, which caused `Restarter must be created only during owner's initialization stage`; the overlay now supplies only lifecycle and ViewModelStore owners required by Compose and clears the store on teardown.

### Validation

- `scripts/check_braces.py` balanced both changed Kotlin files; `git diff --check` passed.
- Final code review found no release-blocking compile, NaN, viewport, or service-lifecycle issues.
- Local Gradle compile/build/test commands were not run because the repository's AGENTS.md explicitly forbids them; CI remains the compilation gate.

---

## Previous Request (COMPLETED)

**FieldMind legacy restore into Curio Cabinet + Curio backup preservation**

### What was requested

Prepare Curio's Cabinet/detail view to accept FieldMind data and its backup, add a legacy FieldMind observation restore action in Settings with Curio styling, and restore observations/notes with metadata, images, and species data without changing current Curio functionality. User chose: Settings → Backup & restore; imported records become Cabinet entries marked legacy; restore observations and notes plus save the species catalog for later; always-on behavior.

### What was done

- Added `FieldMindLegacyImport` for plain FieldMind V3 `archive.json` exports and `.fieldmind` ZIP packages.
- Imported observations become legacy `FieldNotes` entries; notes become legacy `Marginalia` entries. Deterministic `fieldmind-obs-*` / `fieldmind-note-*` IDs make repeated restores skip existing rows, including duplicate IDs inside one archive.
- Preserved capture timestamps, categories, confidence/location/weather tags, FieldMind metadata (date/time, coordinates, structured details, duration, weather readings, status and links), note metadata, package captions in the source media handling, supported image attachments, and the species catalog at `filesDir/fieldmind/species.json`.
- Hardened ZIP extraction against path traversal and cleans temporary extraction media on success or failure. Plain JSON attachment URIs are copied when accessible. Unsupported non-image media is not presented as an image attachment.
- Added species catalog JSON to Curio's existing backup payload so Curio backup/restore carries the imported catalog without affecting older backups.
- Added the Settings → Data → Backup & restore picker, preview/confirm dialog, additive restore result dialog, and non-destructive messaging.
- Added `Legacy` badges on Cabinet cards and the Entry Detail metadata row. Existing native Curio entries and the existing replacement-style Curio backup restore remain unchanged.

### Validation

- `scripts/check_braces.py` balanced all changed Kotlin files; `git diff --check` clean.
- Code review completed; ZIP safety, duplicate-ID handling, temporary cleanup, image filtering, and species backup compatibility were reviewed.
- Local Gradle compile/build/test commands were not run because the repository's AGENTS.md explicitly forbids them; CI remains the compilation gate.

---
