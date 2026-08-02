# Prompt.md — Request Log

## Latest Request (COMPLETED)

**"make the margins and the texts properly aligned and also use the same paper style box in dark mode too and in review boxes as well"**

### What was changed

Paper style + alignment pass across the note-paper surfaces:

1. **`PaperCard.kt`** — Ruled lines now follow the notebook cadence: spaced at `bodyLarge` line-height and starting one cadence below the top content padding, so the first line of text sits ON the first rule (real-paper feel) instead of floating at a fixed 24dp from the card edge. Guarded against an Unspecified `lineHeight` (falls back to 24dp).

2. **`RichTextEditor.kt`** — New `fieldPadding` (default 14/12) and `showFieldBorder` (default true) params so paper-wrapped editors can drop the inner rounded box + double margin and write directly on the paper.

3. **`MarginaliaFormat.kt`** — Journal + quote-card editors now pass `fieldPadding = 0.dp` / `showFieldBorder = false` (text sits directly on the paper at the card's own content margin). Journal content padding unified to 16/14 and quote cards to 12/10 — matching the saved detail view exactly, so what you type aligns with what you see saved.

4. **`ReelNotesFormat.kt`** — The review field (capture form) now wears the same note-paper box as the Marginalia journal (ruled paper, `paperInk`, transparent surface, no inner border) — light AND dark mode.

5. **`EntryDetailScreen.kt`** — The saved review box (`ReelNotesRender`) switched from the category surface to `PaperCard` with `paperInk`, matching the journal's paper look in both themes; "No review written yet" fallback also wears paper.

### Review
1 round of code-reviewer-deepseek-flash — clean. Actioned its one real concern: `bodyLarge.lineHeight.toPx()` guarded against Unspecified (CurioTypography sets it to 24.sp, so safe either way). Note: in the capture editor the MAIN toolbar renders inside the PaperCard, so the ruled lines align under the field's text in the saved view (text directly in card); in the editor the toolbar shifts the field down slightly — rules remain a faint texture there.

### Follow-ups / notes
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.
