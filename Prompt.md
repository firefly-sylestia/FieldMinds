# Category Picker — Multi-Select + Card Visuals

## Request

User (design direction): "the category selection page cards make the full cards have a different slight shade and make the watermark inside the cards in category have the color gradient of the main cards and in active state give it a different look and remove manage all categories and make it done and enable multiple cards select function"

## Analysis

- `CategoryPickerScreen` previously: single-tap → immediate navigate to `spin/{slug}`; bottom button was "Manage categories" (`FilledTonalButton`).
- `CurioCategoryCard` (shared with Spin's picker sheet): flat `categoryCardFill` fill, plain white 10%-alpha watermark, selected = faint white border + white circle w/ accent check. Also had a **sticky pressed bug** — `pressed` set true in `onClick` and never reset, so every tapped card stayed at 0.96 scale forever.
- SpinScreen took a single `categorySlug` (single `CategorySaver`), one-category pool, and revealed via `cat.id.routeSlug`.

## Plan

1. **CurioCategoryCard**: full-card theme-aware gradient (inner Box `Brush.verticalGradient(cardGradient)` — clickable `Surface` has no `brush` param), watermark tinted `lerp(cardColor, Color.White, 0.55f)` @ 18% (echoes the main-card gradient), selected state = 2dp white border + scale 1.03 + accent-filled check badge; replace sticky `pressed` with `MutableInteractionSource` + `collectIsPressedAsState()`.
2. **CategoryPickerScreen**: multi-select toggle (`selectedSlugs` list, saveable), Done button (primary, count label, disabled when empty) replaces Manage categories; navigates `spinWithCategory` (1) or `spinWithCategories` (>1).
3. **CurioRoutes**: add `spinWithCategories(slugs)` = comma-joined `spin/a,b`.
4. **SpinScreen**: parse comma-joined slug → `List<CategoryId>` (`CategoryIdListSaver` replaces `CategorySaver`), merged topic pool across selected cats (dedupe by id, defensive `poolIds` fallback if restore empty), reveals route via each topic's **own** `categoryId.routeSlug`; removed unused `Saver` import.

## Completion Summary

- All 4 files updated; validation green: braces balanced (Spin 265/265, picker 16/16, card 10/10, routes 18/18), zero stale refs (`CategorySaver`, `initialCat`, `FilledTonalButton`, unused `Saver` import), press collector wired to `Surface(onClick, interactionSource)`, single-category flows behaviorally identical (topic.categoryId == active cat).
- Code review clean (2 passes). Minor non-blocking notes: Spin top bar shows only first category name on multi-deck launches; watermark is a single accent-derived tint (CurioIcon takes one tint); picker selection persists across returns (probably desired).
- Gradle build/lint NOT run (forbidden in this environment; CI validates on push).
