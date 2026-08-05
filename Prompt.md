# Prompt.md — Request Log

## Latest Request (COMPLETED)

**Fix quote-card rounded corners + bigger saved-entry reading view**

### Requested

- The tilted quote notecards look bad: the rounded corner doesn't apply.
- The saved entry's note paper should be bigger for reading — wider ruled
  lines, more text breaking room, bigger text and paper view.
- User asked to confirm details before implementing (ask_user used).

### Confirmed decisions (ask_user)

1. **Quote corner**: fix the rounded corner only (keep the ±2.5° tilt).
2. **Size**: moderate bump — 18sp text, 28dp rule spacing, roomier padding
   (24/22dp), 120dp min height.
3. **Scope**: all note cards AND the tilted quote notecards on the saved
   entry grow together.

### Plan

1. Repair `buildNormalPaperPath` so the top-left corner actually rounds
   (the path's closing segment was chamfering it into a flat diagonal).
2. Add an optional `ruleSpacing` param to PaperCard / TornPaperCard /
   NotePaperCard so the ruled-line cadence can scale with the text.
3. In EntryDetailScreen, add `savedNoteStyle()` (18sp/28sp line, Patrick
   Hand) + `SavedNoteRuleSpacing` (28dp) and apply the bump to all 9
   saved-view paper cards (SoundBite note, ReelNotes review + fallback,
   Marginalia journal, quote cards 16/16 + 84dp floor, GalleryWall caption,
   FieldNotes Observed/Surprised/Learn-next).

### Outcome

- `PaperCard.kt`: corner path fixed; `ruleSpacing` param threaded through
  all three paper components (default = bodyLarge line height, unchanged
  behavior for every other caller).
- `EntryDetailScreen.kt`: 9 saved-view cards bumped; braces balanced;
  code-reviewer-glm approved.
