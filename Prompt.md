# Fix Watermark Backdrop in Light Mode

## Request

User: the category-glyph watermark backdrop looks awesome in dark mode but doesn't look good in light mode — fix it.

## Analysis

`CurioWatermarkBackdrop` (shared by Home + Spin) painted all glyphs with `MaterialTheme.colorScheme.onSurface` at 5% alpha and the active category accent at 11%. In dark mode `onSurface` is near-white `#F7F2FA` over a near-black surface — clean soft ghosts, which the user loves. In light mode `onSurface` is deep maroon `#3B0A17` at 5% over warm cream `#FFFBF5` — reads as a muddy, barely-visible smudge. The accent whisper at 11% also gets lost on the light surface.

## Plan

- Add a `WarmWatermarkInk` token to `CurioColors.kt` (warm taupe-gray, designed for light surfaces).
- Make `CurioWatermarkBackdrop` theme-aware via `isCurioDarkTheme()`: dark keeps `onSurface` @ 5% + accent @ 11% (unchanged); light uses `WarmWatermarkInk` @ 16% + accent @ 20%.
- Update `CURIO_SPEC.md` §0.2 with the watermark ink rule; log to Prompt.md.
- Static checks + code review; commit + push (CI validates compile on push).

## Completion Summary

- `CurioColors.kt`: added `WarmWatermarkInk = Color(0xFF8E8177)` with doc comment.
- `CurioWatermarkBackdrop.kt`: added `isCurioDarkTheme()` branch — dark mode pixel-identical to before; light mode uses `WarmWatermarkInk.copy(alpha = 0.16f)` for the neutral glyphs and `accent.copy(alpha = 0.20f)` for the active glyph.
- `CURIO_SPEC.md` §0.2: documented the watermark ink rule (dark: near-white onSurface 5%; light: WarmWatermarkInk ~16%; active accent 11% dark / 20% light).
- Static checks: braces/parens balanced; no unused imports left behind (`MaterialTheme` still used in dark branch). Code review clean. Gradle build/lint NOT run (forbidden in this environment; CI validates on push).
