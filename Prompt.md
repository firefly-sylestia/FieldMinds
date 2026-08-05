# Prompt.md — Request Log

## Latest Request (COMPLETED)

**Stable per-entry torn-paper tears + a different Home tear pattern**

### Requested

- The paper bottom tear style doesn't stay the same — it should have a
  UNIQUE seed per entry, not be the same everywhere; make it remember.
- Change the home screen's tear style a little — a different pattern.

### Analysis

- `PaperCard` (ruled) seeded its bottom torn seam + texture with
  `remember { Random.nextInt(...) }` → re-rolled on every fresh
  composition (every open of the detail page). `TornPaperCard` already
  took `seed: Int?` but no caller passed one. `NotePaperCard` had no seed
  plumbing. Result: every paper card on the detail page (journal, reviews,
  quotes, field-note sections, on-board floating cards) tore differently
  on each visit.
- The Home hero used a fixed `HOME_TEAR_SEED = 0x5EED` and the SAME soft
  tear language as the detail hero → identical-looking seam everywhere.

### Plan

1. `PaperCard`: new `seed: Int? = null` — tear seed = `seed ?: random`,
   grain derived `seed * 0x51A7 + 7` (keeps editor behavior, makes saved
   views deterministic). `NotePaperCard` gains `seed` and forwards it to
   `TornPaperCard` / `PaperCard`.
2. Detail page: `noteSeed(entryId, salt)` helper (hashCode xor salt,
   masked) seeded ALL 9 paper-card call sites (soundbite note, review +
   fallback, journal, quote cards per-index, caption, observed/surprised/
   learnNext) AND the mood-board on-board floating slips (via new
   `seed` params on `MoodBoardFloatingCards`/`MoodBoardFloatingCard`).
   Unique per entry, distinct per card, never re-rolls.
3. Home: new `HOME_TEAR_SEED = 0xC0FEE` + a `bold` tear personality on
   `SoftTearParams`/`SoftTornBottomShape`/`SoftTornSheetShape` (waves
   ×1.2, tooth ×1.35, deep ×1.5) — Home's hero now tears as a rougher,
   deeper seam than the detail hero, both shapes passing `bold = true` so
   they stay pixel-aligned. Default false keeps the detail hero (and every
   other caller) unchanged.

### Status

- Edits applied: PaperCard.kt (seed plumbing + bold personality),
  HomeScreen.kt (new seed + bold), EntryDetailScreen.kt (noteSeed + 11
  seeded call sites), MoodBoardZoom.kt (floating-slip seeds).
- Code-reviewer-deepseek-flash verified compile-safety (defaults preserve
  all existing callers; entry.id in scope at every site) and caught one
  gap — the on-board floating quote cards still re-rolled — which was
  fixed by threading seeds through MoodBoardFloatingCards/Card.
- Committed and pushed.
