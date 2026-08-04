# Prompt.md — Request Log

## Latest Request (COMPLETED)

**Default-off experimental voice-to-text**

### What was requested

Make voice-to-text disabled by default and expose an opt-in toggle in Settings → Experimental.

### What changed

- Added a persisted `AppPreferences.voiceToTextEnabledState` preference with a default of `false`.
- Added the discoverable Experimental → Voice-to-text switch; ordinary voice recording remains unaffected.
- Gated Sound Bite dictation buttons, transcription panels, recognizer creation, and permission callbacks.
- Gated saved voice-note transcription in Entry Detail.
- Cancels active dictation, clears pending requests, and destroys recognizers when the experiment is disabled or the screen is disposed.

### Validation

- `scripts/check_braces.py` passed for all changed Kotlin files.
- `git diff --check` passed.
- Gradle/build commands were not run because the repository forbids local Android compilation; CI remains the compilation gate.

## Latest Request (COMPLETED)

**Restrict FieldMind observation session to legacy entries**

### What was requested

Remove the FieldMind observation-session action from native Curio detail menus and keep it available only in the Legacy Cabinet/detail entries.

### What changed

- Wrapped the detail overflow-menu action in `resolvedEntry.isLegacy`.
- The condition uses persisted legacy provenance, not category, subtype, or display text.
- Native Curio detail menus no longer show the FieldMind observation action; restored legacy entries retain it.

### Validation

- Static validation and review completed.
- Gradle/build commands were not run because the repository forbids local Android compilation; CI remains the compilation gate.


## Latest Request (COMPLETED)

**Thin shared-wave detail tear and Cabinet bulk delete**

### What was requested

- Keep the detail hero's paper tear, but make the exposed white backing very thin and uneven.
- Give the hero and white backing the same broad wave rhythm while keeping their fine bumps slightly different for a realistic layered-paper tear.
- Add Cabinet mass delete triggered by long-press selection, with category/filter-scoped select-all.

### What was changed

- Reduced the detail white backing to a 6dp lip with a 10dp baseline and 12dp reserved layout extent.
- Split the seeded tear math into a shared broad-wave foundation and independent fine tooth; the white sheet now follows the broad waves at reduced amplitude with its own shallow bump layer.
- Added long-press selection to Cabinet cards, selected-state styling, category/search-scoped Select all, cancel selection, confirmation, and bulk delete.
- Bulk deletion removes Room rows first through a single `WHERE id IN (...)` DAO query, then cleans each entry's audio and image files.
- Native detail navigation remains unchanged outside selection mode; normal taps open detail, while selection-mode taps toggle entries.

### Validation

- `scripts/check_braces.py` passed for all changed Kotlin files.
- `git diff --check` passed.
- Gradle/build commands were not run because the repository forbids local Android compilation; CI remains the compilation gate.


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

## Latest Request (COMPLETED)

**Persist legacy provenance explicitly**

FieldMind imports must be marked legacy at restore time, not inferred from category, subtype, or another display field. Add a persisted Room provenance flag, migrate existing imported rows safely, pass the flag through entity/domain conversion, and keep native Curio captures false.

### Completion

- Added and pushed explicit persisted legacy provenance in commit `fbfa4633`.

## Latest Request (IN PROGRESS)

**Refine detail hero torn seam**

The detail hero seam needs 2–3 broad waves with smaller bumpy ripples inside them, applied consistently to both the hero edge and exposed white under-sheet. The white layer must extend below the hero enough to eliminate background gaps while retaining the seeded, stable per-entry shape.

### Implementation notes

- Reduced the primary tear rhythm to roughly 2.2–3 broad waves.
- Added a shallow 7–11-cycle ripple layer plus fine seeded fiber noise to the shared displacement function.
- Applied the shared broad/ripple rhythm to the white sheet's exposed lower edge.
- Increased the white lip to 24dp, baseline to 30dp, and reserved 48dp of layout extent so the sheet cannot overlap the next content section.
- Static brace and whitespace checks passed; Gradle/build commands remain prohibited by repository instructions.

## Latest Request (COMPLETED)

**Harden Curio backup restore and preserve FieldMind text/metadata**

### What was requested

- Investigate intermittent "Backup failed" / restore failures.
- Audit whether the multiple FieldMind observation and note text fields are fully accounted for.

### What was changed

- Curio backup restore now rejects unreadable, missing, malformed, or invalid capture payloads before deleting current media or database rows, preventing a corrupt file from being treated as an empty restore.
- Older backups remain compatible through collection/default normalization and legacy tag-column handling.
- Backup export tolerates a stale or unreadable supplementary species catalog instead of failing an otherwise valid capture/settings backup.
- FieldMind observation metadata now preserves timing/change/lifecycle fields, weather snapshot, parent/follow-up references, quality, and all exported text fields.
- FieldMind note metadata now preserves category, tags, status, project/source references, and lifecycle timestamps.
- Attachment captions are retained in the imported Curio text instead of being silently discarded.
- The Curio FieldMind metadata card displays the expanded lifecycle and timing metadata with readable timestamps.

### Validation

- `scripts/check_braces.py` passed for all changed Kotlin files.
- `git diff --check` passed.
- Gradle/build commands were not run because the repository forbids local Android compilation; CI remains the compilation gate.

## Latest Request (COMPLETED)

**Fix FieldMind observation screen CI compile errors**

CI reported `rememberSaveable` as unresolved in `FieldMindObservationScreen.kt`, causing cascading errors around the metadata constructor, `takeIf`, and button negation. The screen imported it from the wrong Compose package; the correct import is `androidx.compose.runtime.saveable.rememberSaveable`.
