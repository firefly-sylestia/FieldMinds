# Prompt — Detail page polish round (v7.38)

## Request
User asked (detail page only):
1. Quick fact: no background card (the page wash IS the background), placed just below the tear, 2 lines collapsed with "…more" expand and tap-again collapse; font color picked for readability in EVERY mode (normal → pastel → material → dark).
2. Fix the weird spacing between the category pill and the tear: category text a little larger, pill placed just a little below the tear with only its tip tucked (looks like it's coming out of the tear).
3. Remove the favorite-quotes count from the detail view; use the quote symbol (FormatQuote glyph) instead of in-text " marks to show start and end of the quote.
4. Fix the blue color issue in the back and menu icon: make the pills non-transparent frosted gradient without the circular donut glitch that appears when scrolling up.

## Analysis
- `EntryDetailScreen.kt` (v7.38): QuickFactCard was a Surface with category fill + border; meta column had 28dp top padding (≈52dp gap between the tear seam and the pill row); the hero's frosted pills used a translucent white gradient (0.99→0.94 alpha) so a blue Material primary / blue category wash bled through and tinted them; their Surface shadowElevation grew on scroll but was drawn INSIDE the plate's clip → clipped into a dark donut ring around the rim.
- MarginaliaSectionHeader (shared, private) rendered a count pill; RenderQuoteCards wrapped quotes in \u201C…\u201D with a +1 span shift.
- `categoryInk()` is the theme-aware readable ink (deep accent light/pastel-light, light twin dark/AMOLED, researched hue in Material) — the right ink for the backgroundless quick fact on the tinted wash.

## Changes made (EntryDetailScreen.kt only)
1. **QuickFactCard** — backgroundless (Surface fill + border removed); ink = cat.categoryInk() for heading, glyph and teaser (readable on every colored wash); collapsed to 2 lines with an overflow-only "…more" toggle (onTextLayout.hasVisualOverflow); tapping "…less" folds back to compact. Moved to sit directly under the chips row (was at the bottom of the meta column).
2. **Category pill** — labelMedium → labelLarge, glyph 14 → 16dp, padding 10/6 → 12/8; meta column top padding 28 → 6dp with a -14dp lift so the pill's top tip (~8dp) tucks up inside the white paper lip under the tear (seam→pill gap cut from ~52dp to ~16dp).
3. **Quotes** — MarginaliaSectionHeader count param + count Text removed; RenderQuoteCards no longer passes count, dropped the curly-quote text wrapper + span shift, and gained a mirrored (rotate 180f) FormatQuote closing glyph bottom-aligned after the quote text so start + end read as symbols bracketing the quote.
4. **Frosted pills** — heroFrostGradient now fully opaque (White → White 0.97) so no hero color bleeds through (kills the blue tint); heroFrostPlate gained elevation param and draws Modifier.shadow(elevation, shape, clip=false) BEFORE the clip (clean outer drop shadow, no clipped donut ring); both back/more Surfaces set shadowElevation = 0 and pass elevation = 6.dp * frostShift to the plate; added androidx.compose.ui.draw.shadow import.

## Review
Code-reviewer clean after two fixes applied: (a) tip tuck deepened -8 → -14dp (was effectively only 2dp); (b) closing quote glyph bottom-aligned so the pair brackets the quote.

## Status
DONE — implemented, reviewed, Prompt.md updated, committed + pushed.
