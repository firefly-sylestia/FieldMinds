# Prompt.md — Running Request Log

## Latest Request — "Edit mood board is broken: opens wrong entry / empty board / save blanks the entry"

### Status: ✅ Complete (about to commit & push)

### The bug (user-reported, data-loss severity)
1. Editing sometimes opened a TOTALLY DIFFERENT saved entry "based on the category"
2. Sometimes the mood board opened EMPTY
3. Saving that empty board BLANKED the original saved entry

### Root causes (found by tracing route → screen → save chain)
1. **Wrong body dispatch** — `FormatBodyForCategory` dispatched on
   `category.defaultFormat`, but `cat` is derived from the ASYNC-loaded
   `editingEntry`. Before it loads, `cat` falls back to WILDCARD (the edit
   route passes `categorySlug=""`) → the Wildcard **OpenNotebook picker**
   rendered instead of the mood board (a "totally different entry based on
   the category").
2. **Empty-board race** — the format body composed before `editingEntry`
   loaded, so `initialData` was null → blank board; a late
   `remember(initialData)` re-init could wipe in-progress edits.
3. **Blanking on save** — `performSave` fell back to creating a FRESH
   `CurioEntry` when `editingEntry` was null; Room REPLACEs by id, so the
   fresh (blank, wrong-format) entry overwrote the original.

### Fixes (SaveCaptureScreen.kt)
- **Dispatch by the saved entry's format**: `FormatBodyForCategory` gained
  `entryFormat: CaptureFormat?` and dispatches on `entryFormat ?:
  category.defaultFormat`. Edit mode passes `editingEntry?.format` so a
  direct GalleryWall OR Wildcard OpenNotebook-wrapped mood board reopens
  with the correct body regardless of category default.
- **Loading gate**: edit mode shows a `CircularProgressIndicator` until
  `editingEntry != null` — the format body (and its data callbacks) never
  render against the wrong fallback category.
- **Save guard**: `if (editEntryId != null && existingEntry == null)
  { saveInProgress = false; return@launch }` — edit mode can never write a
  fresh entry over the original (kills the blanking).
- `editingEntry` falls back to `TopicCatalog.sampleEntries().find { id }`
  so preview/sample boards are editable too (ids never collide with user
  UUIDs).
- `canSave` additionally gated on `editingEntry != null` in edit mode.

### Also in this request
- **Double-tap-to-zoom** on saved-view mood board tiles (inline board +
  expanded dialog), matching the editor: `detectTapGestures(onTap = zoomIn,
  onDoubleTap = zoomIn)`.
- **Edit button in the expanded mood board dialog** (pencil, TopStart,
  mirrors the Close button) — passes `onEdit` through to navigate to
  `editMoodBoard(entry.id)`.

### Validation
- Code review (deepseek-flash): compile-safe — imports verified (Box /
  CircularProgressIndicator / Alignment / CaptureFormat / TopicCatalog),
  `return@launch` pattern matches existing `resolvedTopic == null` guard,
  `sampleEntries()` suspend call valid inside produceState, braces balanced,
  single call site updated. Two non-blocking notes: single-tap zoom now
  waits out the double-tap window (~300ms, consistent with editor); the
  `runCatching` sample fallback would mask a genuine DB exception (defensive
  only, sample ids never collide with UUIDs).
- No gradle build run (per AGENTS.md, CI owns compilation).

### Prior work (this session)
- Mood board pinch-to-zoom + in-place zoom (no image page) — committed
- Editable saved mood boards + non-overlapping, more-visible watermarks —
  committed earlier
- Mixed-deck identity + blends — committed earlier
