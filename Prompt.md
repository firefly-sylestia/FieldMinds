# Request: Dark-mode tint contrast — red/pink/blue look white-washed

## Request
In dark mode the tint for red (Movies/Rose), pink (Wildcard/Coral) and blue (Science/Sky) still looks white-washed — increase the contrast a little.

## Root cause
The dark-mode wash builds a mid-tone as `lerp(accent, lightAccent, 0.5f)` then blends at 15% over midnight. For those three families the midpoint lands too pale — worst for Coral, whose accent `#FF8FA3` is already a pastel pink — so the hue flattens to grey-white.

## Changes
`app/src/main/java/com/curio/app/ui/theme/CategoryInk.kt`:
- Added `private class DarkWashTuning(midToneFactor: Float, blendFraction: Float)` with `DEFAULT_DARK_WASH = (0.5f, 0.15f)` (byte-identical to the previous hardcoded behavior).
- Added `DARK_WASH_TUNING: Map<CategoryFamily, DarkWashTuning>` — MOVIES (Rose, red) `(0.35, 0.18)`, SCIENCE (Sky, blue) `(0.35, 0.18)`, WILDCARD (Coral, pink) `(0.35, 0.20)` — pulls the mid-tone closer to the deep accent and blends stronger so the hue survives over midnight.
- `categoryBackgroundWash()` dark branch now looks up `DARK_WASH_TUNING[family] ?: DEFAULT_DARK_WASH`; light branch and the tint-wash settings toggle untouched. All other families (Indigo/Amber/Teal) keep the exact previous look.
- Added `import com.curio.app.data.CategoryFamily` (alphabetical). KDoc for the function kept attached directly above it; tuning declarations live below.

## Validation
- Code reviewer passed (2 passes): import order, `family` public on CurioCategory, `lerp(Color, Color, Float)` signature, map covers exactly the three flagged families with fallback preserving old behavior for the rest, no dead code, doc attachment fixed after first review flagged it.

## Completion summary
- Committed & pushed.
