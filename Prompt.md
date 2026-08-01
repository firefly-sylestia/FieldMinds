# Remap Shuffle Card Category Colors

## Request

User: the shuffle card colors need a full remap — some are too bright, some feel weird. Use well-researched, eye-pleasing shades for each category. Direction chosen via ask_user: **researched balanced** palette.

## Analysis

The code used warm pastel category accents (`Lilac #C9A6F2`, `DustyBlue #9BB8E8`, `Sage #A8C99A`, `Peach #FFB585`, `Teal #6FC7BE`) mapped in `data/Category.kt`, with `DeepPlum` content on accent fills and accent-as-text on surfaces. Research (Tailwind harmonized palettes) produced a 6-hue family; the raw 600-level values were deepened one step to **Tailwind-700** so white content clears WCAG AA ≥ 4.5:1 on EVERY accent (verified with luminance math: indigo 7.9:1, rose 6.3:1, amber 5.0:1, teal 5.5:1, sky 6.0:1, purple 7.0:1). Because dark surfaces are midnight `#111722`, deep accents fail as *ink* (text/icons) in dark mode — each accent got a light 300-level twin resolved theme-aware.

## Plan

- Add `CurioColors.Category*` tokens: 6 deep accents + 6 light inks + 6 tints; deepen `cardGradient` cream-lerp 0.42 → 0.30 so white text holds.
- Add `CurioCategory.lightAccent` field + `CurioCategory.categoryInk()` theme-aware helper (new `ui/theme/CategoryInk.kt`).
- Remap `Category.kt`: Music→Indigo, Movies→Rose, Books→Amber, Visual Art→Teal, Science→Sky, Wildcard→Purple (hero cards keep rainbow).
- Flip DeepPlum content → White on accent fills (SpinScreen apply button + dice glyph + chips, TopicReveal CTA, SaveCapture save, SoundBite trim); wildcard flat accent CoralBlush→Purple (Home quest + Surprise chip, CurioCategoryCard, Spin ticket card); accent-ink text sites use `categoryInk()` (Spin cat chip + DeckControlButton, SaveCapture strip, TopicHistory label, Cabinet FilterChipLite).
- Update CURIO_SPEC.md §0.2; static checks + code review; commit & push (CI validates compile on push).

## Completion Summary

- `CurioColors.kt`: added CategoryIndigo/Rose/Amber/Teal/Sky/Purple (700) + Ink twins (300) + Tints (20%); legacy pastels kept for brand/decorative use only; `cardGradient` lerp → 0.30.
- `CategoryInk.kt` (new): `@Composable fun CurioCategory.categoryInk()` = lightAccent in dark, accent in light.
- `Category.kt`: `lightAccent` field added; all 11 entries remapped to researched accents/tints/inks.
- Screens: white content on all accent fills (SpinScreen ×4, TopicReveal CTA, SaveCapture, SoundBite); wildcard → CategoryPurple everywhere flat (Home ×2, CurioCategoryCard, Spin ticket); `categoryInk()` for accent text on surfaces (Spin cat chip, DeckControlButton, SaveCapture strip ×2, TopicHistory, Cabinet FilterChipLite).
- `CURIO_SPEC.md` §0.2: category accent table now documents the researched palette + ink twins + WCAG note.
- Static checks: braces/parens balanced; no orphaned `CurioColors` imports (still referenced); code review passed. Gradle build/lint NOT run (forbidden in this environment; CI validates on push).
