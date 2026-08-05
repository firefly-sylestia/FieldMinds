# Prompt — Calm pastel Material style (v7.38)

## Request
User: "fix the material theme make it use proper material color given by the device and use light color not too dark and make it calm pastel and unique" + "i meant non vibrant color too".

Clarified via ask_user:
- Dark mode: **Light + softened dark** (light cream in light; a softened pastel-tinted dark, lighter/calmer than the Curio midnight).
- Device presence: **Full device scheme, calmed** (surfaces/backgrounds also carry a light airy tint of the device hue — classic Material You, softened).

## What was wrong
The Material style returned the RAW device dynamic scheme (`dynamicLightColorScheme`/`dynamicDarkColorScheme`) — vivid primaries, grey containers, dark-ish muddy look; nothing calm/pastel/unique about it.

## Changes
1. **app/src/main/java/com/curio/app/ui/theme/CurioTheme.kt** — new `calmMaterialColorScheme(dynamic, dark)`: keeps the device's Material You hues (the "proper material color given by the device") but mutes EVERYTHING through HSL — saturation held low (0.08–0.36, non-vibrant), surfaces light:
   - Light: near-white airy pages tinted with the device hue (l 0.95 / s 0.10), muted pastel accents (l 0.80 / s 0.30) with deep same-hue ink (l 0.24).
   - Dark: soft pastel-tinted night (background l 0.17 — lighter than the 0xFF111722 midnight), pastel mid accents (l 0.62) with deep ink; containers step 0.14→0.31.
   - `curioColorScheme()` MATERIAL branch now builds the dynamic scheme then calms it (API 31+; older devices unchanged).
2. **app/src/main/java/com/curio/app/features/settings/SettingsScreen.kt** — Material style blurb → "Your device's Material hues, calmed into soft pastels over light airy surfaces."

Downstream (pre-existing, unchanged): `CurioGradients.cardGradient` anchors on `colorScheme.primary` → in pastel mode (default) it runs pastelAccent on the already-calmed primary (airier); in non-pastel mode floorForWhiteInk darkens the pale primary for white-text cards (existing pattern).

## Review
Code-reviewer clean — valid Kotlin, correct scheme slots, contrast sane in both modes. Flagged (acceptable): non-pastel card gradients will be floored to lum 0.30 for white text.

## Status
DONE — implemented, reviewed, Prompt.md updated, committed + pushed.
