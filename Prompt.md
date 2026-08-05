# Prompt.md — Request Log

## Latest Request (IN PROGRESS)

**Enlarge Home quest, limit Home Recents, and add a dedicated Recent page**

### User decisions

- Quest should match the newer Saved-section UI design language, not copy the Saved text.
- Home Recents preview limit: 5 items.
- Dedicated Recent page: always available, no temporary Settings toggle.

### Changes made

- `HomeScreen.kt`: redesigned Today's Quest / Shuffle the deck as a larger, spacious Saved-style surface with a 54dp leading glyph tile, stronger typography, supporting subtitle, and 54dp shuffle action.
- `HomeScreen.kt`: merged explored, unexplored, and saved entries into a timestamp-sorted feed; Home shows only the newest five items and the hero stat uses the full feed count.
- `CurioRoutes.kt` + `CurioNavHost.kt`: added the always-available `recents` push route.
- `RecentScreen.kt`: added a dedicated full Recents page with the shared `CurioWatermarkBackdrop`, newest-first feed, back control, empty state, and navigation into captures/reveals.
- `fastlane/metadata/android/en-US/changelogs/20260810.txt`: documented the user-visible changes.

### Validation

- Brace checks passed for HomeScreen, RecentScreen, CurioRoutes, and CurioNavHost.
- `git diff --check` passed.
- Review follow-ups applied: explicit sealed-feed constructor lambdas, removed redundant bottom inset, removed nested duplicate quest click target, and retained full-feed count for the Home hero.
- Gradle/build/lint/test commands were not run locally because the repository explicitly forbids Android compilation in this environment; CI remains the compilation gate.
