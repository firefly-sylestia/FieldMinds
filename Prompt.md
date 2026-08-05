# Prompt.md — Request Log

## Latest Request (COMPLETED)

**Add proper top padding above the Home shuffle deck**

### What was requested

The Home shuffle deck/Today's Quest block should not sit too close to the top hero; add proper breathing room above it.

### What changed

- `HomeScreen.kt`: increased the spacer between the hero's white sheet and `QuestShuffleCard` from 14dp to 26dp. Shuffle behavior and navigation were unchanged.
- `fastlane/metadata/android/en-US/changelogs/20260810.txt`: added the user-visible spacing polish entry.

### Validation

- `scripts/check_braces.py` passed for `HomeScreen.kt`.
- `git diff --check` passed.
- Code review found no actionable issues.
- Gradle/build/lint/test commands were not run because the repository explicitly forbids local Android compilation; CI remains the compilation gate.
- Ready to commit and push.
