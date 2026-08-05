# Prompt.md — Request Log

## Latest Request (COMPLETED)

**Peek cards: 5% less saturated**

### Requested

- Take the shuffle page's background peek cards down another 5% in
  saturation (on top of the v7.17 calm-peek tuning).

### Analysis

- `SpinScreen.PeekCard.cardStops` already pulls saturation via HSL:
  non-pastel ×0.80 (cap 0.50), pastel ×0.85 (cap 0.45). "5% less
  saturated" → ease the pull by 0.05: non-pastel 0.80 → 0.75, pastel
  0.85 → 0.80. Caps unchanged.

### Plan / Status

- SpinScreen.kt: both multipliers eased (0.75 / 0.80), inline v7.18
  comments + header doc entry 35 added.
- No behavior beyond the numeric tweak; no imports touched.
- Committed and pushed.
