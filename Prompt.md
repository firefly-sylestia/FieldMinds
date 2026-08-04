# Prompt.md — Request Log

## Latest Request (IN PROGRESS → COMPLETED)

**FieldMind restore metadata cards, separate Legacy Cabinet, and Curio observation session**

### What was requested

- Preserve FieldMind observation/note metadata and species information during legacy restore.
- Show dedicated metadata and species cards in Curio detail without affecting native entries.
- Keep restored legacy entries in a separate Cabinet section rather than mixing them with normal Curio captures.
- Add an always-available FieldMind-style observation session action in the detail overflow menu, using Curio UI and saving safely into the existing Curio repository.

### What was done

- Extended optional `FieldMindMetadata` with weather condition, session start/end, and source identifiers; existing native capture constructors remain backward-compatible because all new fields default.
- Improved archive parsing and species matching using FieldMind's `speciesInfo`, taxonomy, conservation, structured details, timestamps, location, weather, quality, status, project, source, and tags.
- Added a dedicated Curio-styled FieldMind metadata card with weather, coordinates, duration, status, tags, structured details, and nested taxonomy/species presentation. The card renders only when restored provenance exists.
- Added an explicit Legacy Cabinet mode. Normal Cabinet mode excludes legacy imports; the Legacy chip opens a separate "Legacy Cabinet" view and clears category filters.
- Added an always-available `fieldmind-observation` route and Curio-styled timed observation screen. Saving produces a normal Field Notes entry with optional FieldMind provenance, so native Curio flows and storage remain unchanged.
- Added the observation-session action to the Entry Detail overflow menu.

### Validation

- `scripts/check_braces.py` passed on all changed Kotlin files.
- `git diff --check` passed.
- Gradle/build commands were not run because the repository's AGENTS.md forbids local Android compilation; CI remains the compilation gate.
- Plain `archive.json` media can only restore files that remain accessible through the exported URI/path; packaged `.fieldmind` media remains the reliable complete-media path.

### Follow-up note

The session screen is intentionally a lightweight Curio-native session capture rather than a second FieldMind database. Future enhancement can add species picker, GPS/weather capture, and attachments without changing the current persistence boundary.
