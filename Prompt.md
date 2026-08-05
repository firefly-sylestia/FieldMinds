# Prompt.md — Request Log

## Latest Request (COMPLETED)

**Restore Home random/mixed shuffle and reduce the brown cast in Home pastel color**

### What was requested

Restore the Home shuffle button's ability to open either a random single category or a random multi-category mix. Tune the Home pastel hero color so it no longer reads too brownish. The user also requested a durable workflow preference: ask before removing any existing behavior or UI in future changes.

### What changed

- `HomeScreen.kt`: preserved the existing `Random.nextBoolean()` choice between one random category and a 2–3 category shuffled mix.
- `HomeScreen.kt`: changed the Home shuffle navigation to a fresh parameterized Spin destination with state restoration disabled for this explicit action, so the selected deck is visible even when a previous Spin screen exists. Deduplication is disabled so repeated identical random results still start a fresh shuffle.
- `HomeScreen.kt`: tuned only Home's pastel resolver by shifting the rosewood hue 15° toward pink and lifting light-mode lightness slightly. Home greeting/stat/sticky ink now resolves from that same tuned fill. Other category pastels remain unchanged.
- `AGENTS.md`: recorded the user's durable preference that existing features, behavior, UI, or code paths must not be removed without asking first.

### Validation

- `scripts/check_braces.py` passed for `HomeScreen.kt`.
- `git diff --check` passed.
- Static assertions confirmed the random single/mix algorithm remains, fresh route behavior is active, and Home-only hue tuning is present.
- Reviewer found no issue after the repeated-result navigation edge case was addressed.
- Gradle/build/lint/test commands were not run because the repository explicitly forbids local Android compilation; CI remains the compilation gate.
- Store changelog `fastlane/metadata/android/en-US/changelogs/20260810.txt` updated.
