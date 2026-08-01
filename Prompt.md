# Spin Deck Background Cards — Cool Shade Palette

## Request

User: "research and give the main card's background cards cool shades too matching the vibe but not totally different — it should have a beautiful palette and don't modify the main card colors, just the background card."

## Analysis

- The background cards are the `PeekCard` deck fanned behind the hero ticket in `SpinScreen.kt` (near/far layers at 0.28 / 0.42 darkening).
- They were colored with a raw `lerp(cat.accent, Color.Black, depth)` — a plain saturated darkening with no tonal design, which reads muddy and oversaturated next to the muted hero card.
- Web research (Material 3 tonal principles + muted pastel curation): background cards should be low-chroma "ambient echoes" of the accent hue family — desaturated with a cool periwinkle/slate undertone (e.g. rose → muted mauve, teal → slate-teal, coral → dusty rose) rather than hue darkened toward black. Deck stays dark with white content readable (layered depth, no transparency).

## Plan

- Add `CurioGradients.deckCardShade(accent, depth)` = `lerp(lerp(accent, CoolDeckSlate 0xFF8FA3BC, 0.55f), Color.Black, depth)` with a private `CoolDeckSlate` val in the object.
- `PeekCard` in SpinScreen.kt: swap `lerp(cat.accent, Color.Black, ...)` → `CurioGradients.deckCardShade(cat.accent, ...)`; update the shading comment. Hero ticket card colors untouched.
- Remove the now-unused `androidx.compose.ui.graphics.lerp` import from SpinScreen.kt.
- Validate braces/refs + code review; commit & push.

## Completion Summary

- `deckCardShade` added to `CurioGradients` (top-level member of the object; `CoolDeckSlate` private val `0xFF8FA3BC`; `lerp` already imported in CurioColors.kt). Only the deck background cards changed — hero `cardGradient` and every other screen untouched.
- Validation green: zero `lerp` refs left in SpinScreen, `deckCardShade` defined once + one call site, braces balanced (SpinScreen 257/257, CurioColors 3/3), code review clean (reviewer confirmed scope + flagged the unused import, which was removed).
- Visual: each category's deck now reads as a calm, cohesive muted palette (cool periwinkle-slate echo) with the near/far depth layering preserved; white glyph/text stays readable.
- Gradle build/lint NOT run (forbidden in this environment; CI validates on push).
