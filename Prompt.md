# Request: Cabinet 'All' state neutral tint

## Request
Give the Cabinet's 'All' state a subtle neutral tint (e.g. wildcard coral) instead of a plain background.

## Analysis
- CabinetScreen.kt: the category wash followed only the active filter chip; with no filter ("All") it fell back to plain `MaterialTheme.colorScheme.background`.
- The empty state already uses `CategoryId.WILDCARD` as the "All" fallback, so wildcard coral is the natural neutral.

## Changes
- `CabinetScreen.kt`: `filterWash` now resolves `(selectedFilter ?: CategoryId.WILDCARD)` and always produces a wash; the background no longer has a plain fallback. Comment updated.

## Validation
- Code reviewer passed: WILDCARD is a valid enum member used elsewhere in the same file, imports present, nothing depends on filterWash being null.

## Completion summary
- Committed & pushed.
