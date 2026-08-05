# Prompt.md — Request Log

## Latest Request (COMPLETED)

**Detail title gradient follows the card + Home Today's Quest shuffle layout**

### What was requested

Make the detail screen title background gradient follow the background card color and feel less vibrant. Fix the Home screen Today's Quest shuffle deck/button overlap and make shuffling work correctly.

### What changed

- `EntryDetailScreen.kt`: replaced the fixed electric-blue title band with a restrained two-stop gradient derived from the already-resolved detail hero/card color (`heroStart`). Light pastel cards blend toward their deep same-hue ink with a contrast guard; dark and non-pastel themes are gently softened toward black or the active background. The rim now echoes the hero color.
- `HomeScreen.kt`: made the inline Shuffle CTA intrinsically sized instead of letting its child row use `fillMaxSize()`, preventing it from claiming the title column and overlapping on narrow screens.
- `HomeScreen.kt`: Home shuffle now navigates to the parameterized category/mixed-deck Spin route after persisting the chosen IDs, so the new random deck is authoritative even when the plain Spin tab has restored stale UI state.
- No settings toggle added: this is a refinement/fix to existing behavior.

### Validation

- `scripts/check_braces.py` passed for both changed Kotlin files.
- `git diff --check` passed.
- Focused static assertions passed for the fresh route, intrinsic CTA sizing, hero-derived title color, removed hard-coded blue colors, and required import.
- Gradle/build/lint/test commands were not run because the repository explicitly forbids local Android compilation; CI remains the compilation gate.
- Store changelog `fastlane/metadata/android/en-US/changelogs/20260810.txt` updated.
