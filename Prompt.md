# Prompt.md — Request Log

## Latest Request (COMPLETED)

**"extend the paper box margin style to more text boxes"**

### What was changed

The note-paper look (warm cream paper in light mode, toned dark paper in dark mode, faint ruled lines) now covers ALL text boxes, not just the Marginalia journal/quotes and Reel Notes review. A new shared helper plus per-format edits:

1. **`PaperLineField`** (new, in `CaptureFormatComponents.kt`) — a single-line text field wearing the paper look: a thin label above a small `PaperCard` slip with `paperInk` text, paper-tinted placeholder, and `SolidColor(accent)` cursor. Used for short inputs (titles, captions) so they match the notebook style instead of a plain outline box.

2. **SoundBite** — the optional title field is now a `PaperLineField` (kept `ImeAction.Next` + enabled-during-recording guard); the rich-text note editor sits directly on a `PaperCard` slip (`ink=paperInk()`, `surface=Color.Transparent`, `fieldPadding=0`, `showFieldBorder=false`) matching the journal/review pattern.

3. **Field Notes** — all three section editors (What I observed / What surprised me / What I want to learn next) are now wrapped in `PaperCard` slips with the same paper params.

4. **Gallery Wall** — the caption field is now a `PaperLineField` (label above the paper slip).

5. **Saved views** (`EntryDetailScreen`) — the SoundBite note, the three Field Notes sections, and the Gallery Wall caption all render on `PaperCard` with `paperInk` when viewing a saved entry, so the saved view mirrors what the user wrote into.

Removed imports that became unused: `OutlinedTextField`/`KeyboardOptions` (SoundBite), `OutlinedTextField`/`KeyboardOptions`/`ImeAction` (GalleryWall). Imports in CaptureFormatComponents were re-verified clean after an initial import-block mishap (fixed with a full rewrite of the import block).

### Review
1 round of code-reviewer-deepseek-flash — clean, no blocking issues. Only nits: three Field Notes sections duplicate the same PaperCard config (matches existing style), and `PaperLineField.placeholder` is unused at call sites (harmless default).

### Follow-ups / notes
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.
