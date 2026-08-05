# Prompt.md — Request Log

## Latest Request (IN PROGRESS)

**Home default color too brownish + pastel/mixed colors too bright (esp. green)**

### Requested

1. The default Home color is "browish a lot" — make it a beautiful soft
   color instead.
2. Fix the mixed colors' pastel treatment: some pastels are too bright,
   especially colors mixed with green — they look too bright in the palette.

### Analysis

- Home's banner accent is `CurioColors.HomeRosewood = 0xFFB4635A` — a muted
  brownish terracotta (HSL ≈ hue 6°, sat 0.42, light 0.53). The non-pastel
  `homeRoseAccent()` branch kept that same brownish hue (only a slight
  desaturate), so the "default" Home hero reads brown. The pastel twin
  (hue −15°, light 0.82) is an airy pink — fine — but the base is brown.
- `pastelAccent()` light mode held saturation up to 0.72 at lightness 0.80,
  which makes high-saturation hues neon: teal (~0.79 sat), sky (~0.97 sat),
  coral, and green-heavy mixed-deck blends (jade/teal mixes). The
  mixed-deck page wash also laid the blend at 80% strength over cream →
  bright mint floods the Spin page.

### Plan

1. `CurioColors.HomeRosewood` → `0xFFCF8B94` (soft dusty rose, HSL ≈ 352° /
   0.42 / 0.68) — beautiful rose, not brown; dark maroon ink still clears
   contrast on it.
2. `HomeScreen.homeRoseAccent()` non-pastel branch → lift lightness
   (×1.06, cap 0.70) and hold saturation (×0.80, cap 0.40) so the default
   banner reads as a calm soft rose. Pastel branch unchanged (derives the
   airy pink twin from the new rose base automatically).
3. `pastelAccent()` light mode → saturation multiplier 0.80 → 0.70, cap
   0.72 → 0.60 (lightness stays 0.80): teal/mint/sky and green-heavy mixes
   soften to gentle pastels instead of neon.
4. `CurioMixedDeck.mixedDeckWash()` pastel-light strength 0.80 → 0.72 so
   mixed pages no longer flood with bright mint.

### Status

- Edits applied: CurioColors.kt (HomeRosewood + pastelAccent +
  mixedDeckWash), HomeScreen.kt (homeRoseAccent non-pastel branch).
- Old brown value `0xFFB4635A` fully replaced (verified via search).
- Changelog + Prompt.md updated. Awaiting review + commit/push.
