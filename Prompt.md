# Prompt.md — Request Log

## Latest Request (COMPLETED)

**"the quotes entry make them better... give them a paper like texture and in editing give options of highlighting them bold italic etc and make it compact and show it properly in view too"**

### User clarifications (via ask_user)
- **Structured spans in the data model** (backward compatible — legacy entries keep rendering)
- **Dark paper variant** (warm off-black "toned paper" in dark mode, not the app surfaces)
- **Journal + quotes = the main visible formatting option** (always-visible toolbar)
- **Other text fields** (Reel review, Field Notes sections, Sound Bite note) = **small toggle** to show formatting

### What was built
1. **`data/CaptureData.kt`** — new `TextSpan(start, end, bold, italic, highlight)` data class + parallel span fields on Marginalia (`journalSpans`, `quoteSpans`), ReelNotes (`reviewSpans`), FieldNotes (`observedSpans`/`surprisedSpans`/`learnNextSpans`), SoundBite (`noteSpans`), all defaulting to `emptyList()` so legacy Gson blobs decode fine (guarded with `orEmpty()` at read sites).
2. **`ui/theme/PaperPalette.kt`** (NEW) — theme-agnostic note-paper palette: warm cream `FBF4E3` / dark toned paper `2A251D`, warm ink, faint ruled-line color, translucent amber highlighter, hairline border.
3. **`ui/components/PaperCard.kt`** (NEW) — note-paper card with optional ruled-line Canvas texture, slight rotation, compact padding.
4. **`ui/components/RichTextEditor.kt`** (NEW) — shared BasicTextField-based editor with B / I / highlighter toolbar. `MAIN` mode (always visible) for journal + quotes; `TOGGLE` mode (small format button expands the row) for other fields. `buildRichAnnotated()` + `extractRichSpans()` shared render path so formatting looks identical while editing and in the saved view. Unkeyed remember so the parent echo doesn't drop the cursor.
5. **`features/capture/formats/MarginaliaFormat.kt`** — rewritten: paper journal page + compact paper quote cards (each with its own toolbar, ±1.5° rotation, Remove + Add quote), `quoteSpans` kept parallel to `quotes`.
6. **ReelNotesFormat / FieldNotesFormat / SoundBiteFormat** — review/note fields now use `RichTextEditor` in TOGGLE mode; spans emitted through `onDataChanged`.
7. **`features/detail/EntryDetailScreen.kt`** — MarginaliaRender shows paper journal + paper quote cards with spans (quote spans shifted +1 for the curly-quote wrapper; padded zip so spans stay aligned with filtered quotes); ReelNotes/FieldNotes render spans; SoundBiteRender now shows the previously-hidden note with spans. Added `PaddingValues` import (was missing).
8. **`ui/theme/CurioIcons.kt`** — `FormatBold`, `FormatItalic`, `FormatHighlight` (`format_color_fill`), `FormatText` (`text_fields`) — all four glyphs verified present in the bundled `material_symbols_outlined.ttf`.

### Review
3 rounds of code-reviewer-deepseek-flash; all findings fixed (missing PaddingValues import → compile break; quoteSpans index misalignment → padded zip+filter; unused imports removed; null-safe `isNullOrBlank` filter; import ordering). Final review clean; font glyphs verified.

### Follow-ups / notes
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.
- Experiments/design direction unchanged elsewhere; this feature is the permanent behavior per user request (not settings-gated).
- TODO if desired later: a formatting toolbar on the topic-notes / other free-text fields outside the capture formats.

## Previous Requests (brief)
- Added ASK WHEN UNSURE rule (< ~80% understanding → ask user) to AGENTS.md + master.md.
- Fixed CI compile failure: restored missing `gestureActive` declaration in `MoodBoardZoomState`.
- Cabinet: filters-page category wash background + top back button to dismiss filter; dark-mode chips desaturated for contrast.
- Shuffle peek-card cut-off fix (without design change).
- Mood-board pinch-zoom lag fix (transform during gesture).
- Shuffle animation made less violent (background cards animate to front).
- Removed tinted background behind category + filter card (with clarification).
