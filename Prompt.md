# Card Gradients, Watermark Colors & Wildcard Recolor (coral)

## Request

User: (1) match the card color gradient with the category card colors; (2) make the background have the shade of white (light) / black (dark) matching the theme; (3) give the watermark icons the exact gradient colors of the main cards; (4) change the wildcard purple — it's awful — and fix the Surprise main card having a different color than the category tiles; (5) give it the app theme colors (research previous colors and use them).

## Analysis

- `CurioGradients.cardGradient(accent)` was theme-agnostic: `accent → lerp(accent, CreamWhite, 0.30)` regardless of dark mode, and the start color didn't tie into the flat category-card treatment (`lerp(accent, Black, 0.08–0.10)`).
- `CurioWatermarkBackdrop` tinted all 11 glyphs with a single muted neutral (onSurface 5% / WarmWatermarkInk 16%) + accent whisper for the active glyph — not the category colors.
- Wildcard flat accent was `CategoryPurple #7E22CE` on tiles/chips/peek-cards, but the Surprise **main card** used a different warm rainbow gradient (`wildcardCardGradient`) — the mismatch the user saw.
- User chose **coral pink (brand primary #FF8FA3)** for wildcard via ask_user (accepted: white text gets less contrast on flat fills).

## Plan

- `CurioColors.kt`: rename `CategoryPurple*` → `CategoryCoral*` (= CoralBlush brand primary; ink #FFC2CE; tint 20%). Make `cardGradient` `@Composable` + theme-aware: opens on new `categoryCardFill(accent) = lerp(accent, Black, 0.10)` (the category-card fill) and fades toward `White` (light) / `Black` (dark) at 30%. Remove `wildcardCardGradient()`; keep `WildcardGradientStops` for decorative Onboarding/Profile only.
- `CurioWatermarkBackdrop.kt`: each glyph tinted with its own category's accent from `CurioCategories.all` (active glyph 15% dark / 26% light; others 7% dark / 12% light).
- All hero/ticket/quest/reveal/entry gradients → `CurioGradients.cardGradient(accent)` (wildcard branch removed; now @Composable calls moved out of `remember{}` lambdas).
- `CurioCategoryCard` uses `categoryCardFill()` for all categories; `Category.kt` wildcard entry → coral tokens.
- `CURIO_SPEC.md` §0.2: watermark-ink paragraph, wildcard color line, wildcard-tile bullet → coral.
- Validate refs/braces/imports + code review; commit & push.

## Completion Summary

- All palette/gradient/watermark changes applied as planned. Wildcard is consistently brand coral everywhere (tiles, chips, peek cards, hero/ticket/quest/reveal/entry gradients); card gradients open on the category-card fill and fade toward white/black per theme; watermark glyphs carry per-category accent colors.
- Fixed reviewer-found compile error: `CurioCategoryCard.kt` was missing `import ...CurioGradients`; also dropped unused `lerp`/`CurioColors` there, `CategoryId`/`CurioGradients` in CurioTopicCard, `CategoryId` in EntryDetailScreen.
- Validation green: zero `CategoryPurple`/`wildcardCardGradient` refs repo-wide, no `cardGradient()` calls inside `remember{}` (non-composable context), braces balanced in all 10 touched files, imports clean.
- Reviewer notes (non-blocking): coral ticket white-text contrast ~2:1 (accepted tradeoff; not a regression vs. old rainbow pastels — could deepen wildcard fill to `lerp(CoralBlush, Black, ~0.22)` if it reads washed out); gradient lists now reallocated per recomposition (trivial).
- Gradle build/lint NOT run (forbidden in this environment; CI validates on push).
