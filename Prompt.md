# Prompt.md — Running Request Log

## Latest Request — "Blends have bugs: it selects a category when it should trigger a Mixed category"

### Status: ✅ Complete (committed & pushed)

### The bug
The blended COLORS worked, but a multi-select deck still presented as the
FIRST selected category: top bar name/glyph, watermark accent, hero + peek
card glyphs, bottom CTA "Categories" label, filter sheet header, and the
picker sheet indicator all used `activeCategory` (first id) — no "Mixed"
identity existed.

### The fix (SpinScreen.kt)
1. New mixed-deck identity: `isMixedDeck = activeCatIds.distinct().size > 1`,
   and a display-only synthetic `deckCat` built with `activeCategory.copy(...)`:
   displayName = "Mixed", iconGlyph = CurioIcons.AutoAwesome (sparkles),
   accent/tint = the blended deck colors, lightAccent = pastel twin of the
   blend (lerp toward white) so `categoryInk()` stays readable in dark mode.
   `id` is PRESERVED (first category) so all logic keys keep working: landed
   topic, filter state, shuffle reveal guard, last-used prefs, onCardTap
   cached lookups.
2. Threaded `deckCat` through every display surface: TopBar, watermark
   backdrop, Carousel (→ hero glyph, peek glyphs, empty hint), BottomCta,
   FilterSheet, CategoryPickerSheet indicator, and ConfettiBurst tint.
3. BottomCta gained `mixedCount: Int = 1` — the Categories button now reads
   "Mixed · N" for multi-select decks (falls back to the category name).

### Notes
- Watermark backdrop renders neutral for mixed decks (sparkles isn't in the
  11-glyph scatter, so no active whisper) — intended.
- Hero bounce is keyed on the first id, so changing the mix while the first
  id stays the same won't re-bounce — cosmetic only.

### Validation
- Braces 296/296; every display call site switched to deckCat; no logic path
  touched (shuffle guard + onCardTap still use the real category); imports
  (lerp, Color, CurioIcons) already present; code review clean.

### Prior work (this session)
- Richer mixed-deck blends (curated triples, 4+ centroid, AA-verified) —
  committed `b354ef21`
- CI compile fixes — `5d52312d`
- Mood board pin-to-front + clear board — `f128ddfd`
- Edit mood board reuses saved entry-id watermark seed — `c6262518`
- Category pickers tap/long-press multi-select — `395f0abf`
