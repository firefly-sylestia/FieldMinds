# Prompt.md — Request Log

## Latest Request (COMPLETED)

**Calm the shuffle page's background peek cards (all palettes)**

### Requested

- The peek cards behind the main (hero) card on the Shuffle page now wear
  the deck-gradient family, which is great — but they're too bright and
  vibrant. Tone them down. Clarified to cover EVERY palette: default
  (Curio), Material device-primary blend, and pastel modes alike.

### Analysis

- `SpinScreen.PeekCard` (v7.14/7.15) fills the slim background cards with
  the deck's OWN gradient stops (multi-accent sweep for mixed decks,
  theme-aware card gradient otherwise), stepped a level darker than the
  hero:
  - non-pastel: black-lerp near 0.28 / far 0.42 (vivid stops stayed vivid),
  - pastel: HSL lightness drop near 0.06 / far 0.10 (light), 0.09 / 0.14
    (dark) — barely below the hero's 0.80-lightness crown,
  - plus the experimental "top-lit" gradient crown (white-lerp 0.10–0.14)
    brightening the card tops further.
- Result: peeks glowed beside the hero instead of receding behind it, in
  every palette — brightness from insufficient depth + the crown,
  vibrancy from the saturated device/category stops.

### Plan

1. `PeekCard.cardStops` (feeds BOTH the always-on base fill and the
   toggle-on gradient crown path):
   - non-pastel: black-lerp deepened to near 0.40 / far 0.52, then HSL
     saturation pull ×0.80 capped at 0.50 — deep + calm, white ink stays
     readable.
   - pastel: lightness drop raised to light 0.12/0.18, dark 0.11/0.16,
     with saturation ×0.85 capped at 0.45 — airy pastels stay in family
     but sit a clear step below the hero.
2. `PeekCard.fillBrush` — crown softened to a whisper (pastel light 0.05,
   non-pastel far 0.04 / near 0.06).
3. SpinScreen header doc: v7.17 entry 34 added.

### Status

- Code edits applied to `SpinScreen.kt` (PeekCard cardStops + fillBrush,
  header doc entry).
- Code-reviewer-deepseek-flash verified: compile-safe (toHsl/fromHsl/lerp
  imported, coerceAtMost/coerceIn stdlib), ink contrast sane on darker
  fills, no scope creep. Flagged only that the constants are the iteration
  knob if the user wants them even calmer / or if it overshoots to muddy.
- Committed and pushed.
