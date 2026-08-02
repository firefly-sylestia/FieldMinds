# Prompt.md — Request Log

## Latest Request (COMPLETED)

**"make the quote tilt random and not just 2 same each time"**

### What was changed
Quote card tilt was hardcoded to alternate ±1.5° by index (`if (i % 2 == 0) 1.5f else -1.5f`). Both places now use a **stable-per-card random tilt** of ±2.5°:

1. **`MarginaliaFormat.kt`** (capture editor) — `quotes.forEachIndexed` now uses `remember(i) { kotlin.random.Random.nextFloat() * 5f - 2.5f }`.
2. **`EntryDetailScreen.kt`** (saved detail view, MarginaliaRender) — same random rotation keyed by index, passed to `PaperCard`'s `.rotate(rotation)`.

Keyed on the card index so recomposition / typing / scrolling never re-rolls a card's tilt mid-edit/mid-view. `kotlin.random.Random` used fully-qualified (matches EntryDetailScreen's existing pattern, no import needed). `remember` already imported in both files.

### Review
1 round of code-reviewer-deepseek-flash — clean. Non-blocking notes: the two call sites duplicate the formula (fine at 2 sites; a shared `randomQuoteTilt()` helper in PaperCard.kt would keep them in sync if desired), and add/remove of quote cards in the editor re-rolls tilts of cards after the changed position (inherent to index-keying; acceptable for a "random" look).

### Follow-ups / notes
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.

## Previous Requests (brief)
- Both category pickers (Spin sheet + Browse-all route) converted to the filter page's full-screen ModalBottomSheet + swipe-down dismiss pattern.
- CI compile fix: TextFieldValue.text is String now (use annotatedString), SpanStyle.background non-null Color (use Unspecified), matchParentSize BoxScope member, hoist paperRule() out of Canvas.
- Cabinet wash fix: 'All' page stays on plain theme background; search button always neutral (no tint).
- Quotes entry: note-paper texture + bold/italic/highlight rich-text editing (journal + quotes always-visible toolbar; other text fields small toggle; saved view renders spans on paper cards).
- Added ASK WHEN UNSURE rule (< ~80% understanding → ask user) to AGENTS.md + master.md.
- Fixed CI compile failure: restored missing `gestureActive` declaration in `MoodBoardZoomState`.
- Cabinet: filters-page category wash background + top back button to dismiss filter; dark-mode chips desaturated for contrast.
- Shuffle peek-card cut-off fix (without design change).
- Mood-board pinch-zoom lag fix (transform during gesture).
- Shuffle animation made less violent (background cards animate to front).
- Removed tinted background behind category + filter card (with clarification).
