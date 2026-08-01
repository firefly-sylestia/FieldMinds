# Request: Category tint on Category Picker + Edit Entry screens

## Request
Apply the category tint (wash) to the Category Picker screen and the Edit Entry screen so the whole flow is consistent.

## Analysis
- Spin, Topic Reveal, Save/Capture root, and Cabinet already wear `categoryBackgroundWash()`.
- CategoryPickerScreen: root Column was plain `MaterialTheme.colorScheme.background` — no tint. It hands off to the Shuffle tab, so the last-used deck is the natural tint source.
- Edit Entry = SaveCaptureScreen in edit mode (`edit-entry/{entryId}` → SaveCaptureScreen): root Column ALREADY had the wash, but the sticky bottom Save CTA tray was plain `background` + tonalElevation 2dp, looking odd against the tinted page (same as the Spin bottom bar before its fix).

## Changes
1. `CategoryPickerScreen.kt` — added `categoryBackgroundWash` import; `val washCat = remember { getLastSpinCategories(context).firstOrNull() ?: getLastSpinCategory(context); byId(id) }`; root Column `.background(washCat.categoryBackgroundWash())`.
2. `SaveCaptureScreen.kt` — bottom Save CTA Surface color → `cat.categoryBackgroundWash()`, tonalElevation 2dp → 0dp so the tray blends into the tinted page.

## Validation
- AppPreferences signatures verified (getLastSpinCategories → List<CategoryId>, getLastSpinCategory → CategoryId, WILDCARD fallback).
- Code reviewer passed: imports alphabetical, types check (CategoryId? ?: CategoryId = CategoryId), no dead code, `cat`/`context` in scope, wash already imported in SaveCaptureScreen.

## Completion summary
- Committed & pushed.
