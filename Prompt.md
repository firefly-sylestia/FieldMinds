# Prompt — Running Request Log

## Current Request

**"When opening the categories from the 'What are we exploring?' page (Home → All pill), open them on the Shuffle page instead of a different page, and save the mixed selection too so it survives back etc."**

User clarified: the All pill opens the What-are-we-exploring page; categories tapped there were opening a *separate* shuffle instance (a `spin/{slug}` page) instead of the persistent Shuffle tab.

## Status: COMPLETE

## What was done

1. **AppPreferences** (`app/src/main/java/com/curio/app/data/AppPreferences.kt`)
   - Added `KEY_LAST_SPIN_CATEGORIES` (comma-joined set key) + `getLastSpinCategories(context)` / `setLastSpinCategories(context, ids)`.
   - `getLastSpinCategories` parses the set, falls back to the single-category key (then WILDCARD) when unset.
   - `setLastSpinCategories` stores distinct names comma-joined and keeps the single-category key in sync with the first entry (backwards compatible).

2. **CategoryPickerScreen** (`app/src/main/java/com/curio/app/features/picker/CategoryPickerScreen.kt`)
   - Single tap: now persists `setLastSpinCategories(context, listOf(cat.id))` then `navigateToTab(CurioRoutes.SPIN)` — lands on the real Shuffle tab, not a separate `spin/{slug}` page. Removed the old `navigate(spinWithCategory) { popUpTo(HOME) }`.
   - Done (multi-select): resolves slugs → ids via `CurioCategories.byRouteSlug(...)?.id`, persists the FULL set, then `navigateToTab(SPIN)`.
   - Added imports: `LocalContext`, `AppPreferences`, `navigateToTab`.

3. **SpinScreen** (`app/src/main/java/com/curio/app/features/spin/SpinScreen.kt`)
   - Plain-tab seeding (`categorySlug == null`): `initialCats` now comes from `getLastSpinCategories` (full set), not just the single last category.
   - Slug-launch persist effect now saves the FULL launch set (mixed survives).
   - New null-slug branch in `LaunchedEffect(categorySlug)`: re-derives `activeCatIds` from prefs so `restoreState` can't resurrect a stale deck when the picker landed on the plain tab.
   - CategoryPickerSheet `onCategorySelected` / `onCategoriesSelected` now persist the full set.

## Validation

- code-searcher confirmed all edits landed (picker: imports + navigateToTab + setLastSpinCategories; spin: seeding + effect + sheet persist) and zero leftover references.
- code-reviewer-deepseek-flash reviewed clean (brace balance, imports resolve, rememberSaveable input-keying invalidates stale restore, list equality valid). Noted one-time migration quirk (old in-session mixed deck falls back to single on first post-update entry) — acceptable.
- No local gradle build per AGENTS.md — CI owns compilation on push.

## Commit

- `feat: category picker opens categories on the Shuffle tab (not a separate spin page) and persists mixed multi-select decks across back/tab/relaunch`
