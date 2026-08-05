# Prompt — Detail page + entry tools batch (v7.39)

## Requests
1. Zoom overlay pinch/drag "works but not smooth, acts with a delay".
2. Detail: category still doesn't show below the tear; remove the
   "Captured today · time" meta line (put a very small time on the hero's
   date card); remove the dead voice-note play icon + enlarge its text.
3. Entry page: rounded corner + watermark only work on the title boxes —
   apply to ALL text boxes.
4. Entry page tools scattered (colors / styles / text format take too much
   space) — collapse them, one tool open closes the other, and put text
   format behind a text button so it doesn't always show.

## Fixes
- **MoodBoardZoom.kt** — pinch/pan deltas are now applied PER pointer event
  inside the gesture loop (previously accumulated and applied only when all
  fingers lifted → the image moved with a delay). Tap/movement
  classification + close semantics unchanged.
- **EntryDetailScreen.kt**
  - Category tucks at the tear: meta column lift -14dp → -32dp (tip now
    grazes the torn edge); bottom padding 16→8 and body vertical padding
    16→8 keep the tags→body gap identical.
  - "Captured today · time" line removed; hero Date FrostedSegment gained
    a `tiny` line rendering the time at 9sp. capturedAtLabel() deleted.
  - Voice-note header: dead circular PlayArrow icon removed; label bumped
    titleSmall → titleMedium (the real AudioPlayerBar stays below).
- **RichTextEditor.kt**
  - Paper boxes now forward `watermark` (both bases) and `roundedTop`
    (PaperCard) from the style — same decorations as the title fields.
  - Toolbar unified: one compact row with TEXT buttons — "Paper"
    (Palette) and "Format" (FormatText) — each reveals its panel and
    opening one closes the other; the B/I/highlight/size toolbar no longer
    always shows. StyleToggleButton replaced by ToolToggleButton; stale
    KDOC updated; unused Spacer import removed.
- **MarginaliaFormat.kt** — CI fix: restored the `Row` import (my earlier
  FlowRow edit dropped it; JournalVoiceNoteRow still uses Row).

## Review
Reviewer clean after KDOC refresh (4px pan dead-zone + voice-note indent
noted as acceptable cosmetics).

## Status
DONE — implemented, reviewed, Prompt.md updated, committed + pushed.
