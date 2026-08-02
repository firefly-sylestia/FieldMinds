# Current Request

## Status: COMPLETED — committed and pushed to `revamp`

"use the same backgroud and and the back fucntion with a top button to make
the category tab dismiss like its in filters page, and in dark mode make the
category chips be more less saturated but more contrast"

## Change 1 — Cabinet wears the filters-page background + top back button

**`app/src/main/java/com/curio/app/features/cabinet/CabinetScreen.kt`**
- Root Column now wears `filterCat.categoryBackgroundWash()` — the same
  theme-aware category wash as the filters page (CategoryPicker). Selecting
  a category colors the whole page (wildcard coral when "All" is active,
  restoring the pre-0ad3af3c behavior).
- Top bar: when `selectedFilter != null`, a `CurioBackButton` appears before
  the title and dismisses the active category filter back to "All" — the
  same back affordance as the filters page (no popBackStack since Cabinet
  is a bottom-nav tab).

## Change 2 — dark-mode chips: less saturated, more contrast

**`app/src/main/java/com/curio/app/ui/theme/CategoryInk.kt`**
- New `categoryChipSurface()`: light mode identical to `categorySurface`'s
  soft cream tint; dark mode pulls the family mid-tone toward neutral grey
  (`lerp(midTone, Color(0xFF9AA3B0), 0.40f)`) then blends at
  `blendFraction + 0.40f` (max 0.64 < 1) — desaturated so deep accents stop
  reading muddy, but blended harder than the page wash so the chip lifts off
  the dark background (more contrast). Honors the Settings tint toggle.

**`CabinetScreen.kt`** — category chips now use `categoryChipSurface` and
`categoryBorder(fallback = outlineVariant)` (same border pattern as
FilterSheet's CompactChip — light-twin hairline in dark adds the crisp edge).
All chip unchanged.

## Review
- code-reviewer-deepseek-flash (x2): clean. Braces balanced (nested top-bar
  Row), imports all live (`categorySurface` stays via the search button),
  dark-mode blend math coherent, wash + back button correctly mirror the
  filters page. One optional note (light-branch duplication in
  categoryChipSurface) left as-is — matches the codebase's per-helper style.
- Per AGENTS.md no local Gradle build — CI validates compilation on push.
