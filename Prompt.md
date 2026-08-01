# Request: Improve journal favorite-quotes + make review stars filled

## Request
(1) Improve the journal entry favorite quotes feature; (2) in the review, make the stars fill instead of just outline.

## Root cause
The app's icon font is Material Symbols **Outlined** (`material_symbols_outlined.ttf`) — even the `star` ligature renders as a hollow outline, so "filled" rating stars were just tinted outlines in both the capture form and the saved review.

## Changes
- `CurioIcons.kt` — new `FormatQuote = "format_quote"` glyph const.
- `CaptureFormatComponents.kt` — NEW `@Composable fun FilledStar(color, starSize, modifier, filled)` that draws a solid 5-pointed star path on Canvas (outer radius = `size.minDimension/2`, inner at 0.42×, angle = `Math.toRadians(-90 + i*36)`); unfilled stars draw the same solid path at 25% alpha (filled-or-ghost, never hollow). `StarRating` rewritten to use it (32dp, clickable, `.semantics { contentDescription = "N star(s)" }` restored since Canvas has no automatic label). New imports: `Canvas`, `Offset`, `Path`, `kotlin.math.cos/sin`, `semantics`/`contentDescription` (ASCII-ordered).
- `MarginaliaFormat.kt` — capture-side `QuoteCard` upgraded: header row with FormatQuote glyph + "Quote N" label + Remove button (moved out of the bottom), accent rule border (28% alpha) + 2dp shadow lift so each card reads as a placed notecard. New imports `Spacer` + `width`.
- `EntryDetailScreen.kt` — saved review (`ReelNotesRender`) now renders 5 `FilledStar` (imported from capture.formats): filled in `categoryInk()`, remainder as outline-ghost. Saved `MarginaliaRender` upgraded: new `MarginaliaSectionHeader` (FormatQuote glyph + label + optional count) above "My thoughts" and "Favorite quotes"; journal + quote cards use `categorySurface(...)` + `categoryBorder()` and quotes get a FormatQuote glyph, accent rule, 1dp shadow and the existing ±1.5° rotation — mirroring the capture form.

## Validation
- Code reviewer passed 3 passes. One fix applied as prescribed: the rewritten `StarRating` lost the old CurioIcon's `contentDescription` — restored via `.semantics {}` on the star so the tappable rating row stays accessible. Star geometry/scope/imports verified; reviewer confirmed ready to push.

## Completion summary
- Committed & pushed.
