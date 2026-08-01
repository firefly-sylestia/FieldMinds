# Current Request

## Status: IN PROGRESS — edits applied, review passed, commit pending

"make the category selection page colors like the background tint but a little different and make them proper bright like now when its selected, properly execute this."

## Changes (1 file)

1. **`app/src/main/java/com/curio/app/ui/components/CurioCategoryCard.kt`** (shared by the Spin CategoryPickerSheet and the full-screen CategoryPickerScreen — one change covers both pickers)
   - **Idle (unselected) cards** now wear the category's tinted surface —
     `category.categorySurface(surfaceContainerLow)`, i.e. the page wash's
     stronger sibling ("the background tint, but a little different") — as
     a flat `Brush.solidColor` fill, with a slim `category.categoryBorder()`
     rule, theme text (`onSurface` / `onSurfaceVariant`), and a subtle
     `categoryInk` ghost watermark. No more full-brightness idle tiles.
   - **Selected cards** keep the existing proper-bright treatment untouched:
     full `cardGradient` fill, white content, 2dp white border, sheen
     overlay, and the scale bump.
   - Added imports: `categoryBorder`, `categoryInk`, `categorySurface`.
   - KDoc updated to describe the idle-tint / bright-selected split.

## Review
- code-reviewer-deepseek-flash: first pass caught a compile bug — the idle
  branch passed a `Color` where `Brush` was expected in `Modifier.background`
  (mixed Color/Brush conditional). Fixed with `Brush.solidColor(idleSurface)`
  so both branches are `Brush`. Second pass confirmed clean: all imports
  used, no stale references, types correct.

## CI
- Compile gate = GitHub Actions on push (per AGENTS.md — no local Gradle).
