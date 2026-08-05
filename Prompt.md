# Prompt.md — Request Log

## Latest Request (COMPLETED)

**Cabinet opens on All after an app restart**

### What was requested

When the app is restarted, the Cabinet should open on the All tab rather than remembering the previously selected category tab.

### What changed

- `CabinetScreen.kt`: added a process-local `CabinetSessionToken` as an input to the saveable category filter and Legacy-view state.
- The filter still survives recomposition, rotation, and same-process tab navigation, but Android's restored state is discarded after process death because the process token is different. A fresh app process therefore starts on All.
- Other Cabinet saveable state (search, sort, selection, and grid position) was left unchanged.

### Validation

- `scripts/check_braces.py` passed for `CabinetScreen.kt`.
- `git diff --check` passed.
- Static assertions confirmed the process token keys both filter states.
- Compose `rememberSaveable(input, stateSaver = ...)` syntax matches the existing SpinScreen pattern.
- Gradle/build/lint/test commands were not run because the repository explicitly forbids local Android compilation; CI remains the compilation gate.
- Store changelog `fastlane/metadata/android/en-US/changelogs/20260810.txt` updated.
