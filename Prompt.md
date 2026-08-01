# Request: Dark-mode category tint looks white-washed — fix with proper tints

## Request
In dark mode the category tint (background wash) looks white-washed; fix it by using proper tints.

## Analysis
- `categoryBackgroundWash()` in `CategoryInk.kt`: dark mode blended each category's near-white 300-level twin (`lightAccent`, e.g. indigo 0xFFA5B4FC, amber 0xFFFCD34D) at 16% over the midnight background (0xFF0B1018) — inherently white-washed.
- Deep accent alone at 20% over midnight reads muddy (amber→brown, teal→grey-green) — the original reason the twin was chosen.
- Fix: build a saturated mid-tone = `lerp(accent, lightAccent, 0.5f)` (≈500-level shade) and wash it at 15%. Real hue, no white-out, no mud.

## Changes
- `CategoryInk.kt`: dark branch → `val midTone = lerp(accent, lightAccent, 0.5f); lerp(background, midTone, 0.15f)`. Light mode unchanged (20% deep accent over cream). KDoc updated.
- Single helper → fix propagates to Spin, Topic Reveal, Save/Capture, Cabinet (filter + All), Category Picker, and saved-entry detail automatically.

## Validation
- Code reviewer passed: lerp already imported, accent/lightAccent in scope, no callers depend on the old fraction, KDoc accurate.

## Completion summary
- Committed & pushed.
