# Prompt.md — Request Log

## Latest Request (COMPLETED)

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
