# Request: Cabinet saved-entry pages get the category tint

## Request
Give the Cabinet saved-entry pages the category tint too (so entries opened from the Cabinet match the rest of the flow).

## Analysis
- CabinetScreen, Spin, Save/Capture, Topic Reveal, Category Picker all wear `categoryBackgroundWash()`.
- `EntryDetailScreen.kt` (the saved-entry page opened from Cabinet cards) still used plain `MaterialTheme.colorScheme.background` at the root. `cat` (the entry's own category) is already resolved in scope.

## Changes
- `EntryDetailScreen.kt`: added `categoryBackgroundWash` import; root Column `.background(MaterialTheme.colorScheme.background)` → `.background(cat.categoryBackgroundWash())` so each saved entry reads in its category's color story (matches the Cabinet filter wash / Spin / Save).

## Validation
- Code reviewer passed: import placement ASCII-correct, `cat` in scope, @Composable extension valid in modifier chain, MaterialTheme still used elsewhere (no dead import).

## Completion summary
- Committed & pushed.
