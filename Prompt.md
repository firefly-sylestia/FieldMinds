# Request: Dark-mode wash for red/pink/light-blue still whitewashed

## Request
In dark mode, the category tint for red (Movies/Rose), pink (Wildcard/Coral), and light blue (Science/Sky) still looks whitewashed — needs a better dark shade.

## Changes
- `CategoryInk.kt` — `DarkWashTuning` gained a `darken: Float = 0f` field and a `resolveMidTone(accent, lightAccent)` member (lerps accent→lightAccent at `midToneFactor`, then lerps toward `Color.Black` when `darken > 0`). Both `categoryBackgroundWash` and `categorySurface` now call `tuning.resolveMidTone(...)` — the duplicated inline mid-tone lerp is gone.
- Tuning updated for the three whitewashed families:
  - MOVIES (rose/red): midToneFactor 0.35 → **0.15** (closer to deep accent), blend 0.18 → **0.20**.
  - SCIENCE (sky/light blue): midToneFactor 0.35 → **0.15**, blend 0.18 → **0.20**.
  - WILDCARD (coral/pink): midToneFactor 0.35 → **0.15**, blend 0.20 → **0.22**, plus `darken = 0.28` — pastel coral has no deep twin, so the mid-tone is pushed toward black to give an actual pink shade instead of a pale white haze.
- `DEFAULT_DARK_WASH` unchanged (0.5f, 0.15f) — the `darken = 0f` default means untuned families render exactly as before.

## Validation
- Code reviewer confirmed clean: member function on the private class valid, `Color.Black` covered by the existing `Color` import, both call sites consistent, `darken` default preserves other families, no external callers of `DarkWashTuning` (private to file).

## Completion summary
- Committed & pushed.
