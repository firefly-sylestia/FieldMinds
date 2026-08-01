# Current Request

## Status: IN PROGRESS — edits applied, review passed, commit pending

"Remove the now-inert 'Home tint' toggle from the Settings screen since Home is always plain now"

## Changes (2 files)

1. **`app/src/main/java/com/curio/app/features/settings/SettingsScreen.kt`**
   - Removed the "Home screen tint" toggle Row (+ its trailing divider) from
     the Appearance card. The "Category tint" toggle remains the card's
     last row.

2. **`app/src/main/java/com/curio/app/data/AppPreferences.kt`**
   - Removed `KEY_HOME_TINT_ENABLED`, `homeTintEnabledState`,
     `isHomeTintEnabled`, `setHomeTintEnabled`, and the `initThemeMode`
     seed line — dead code after Home stopped using the toggle (prior commit).

## Review
- code-reviewer-deepseek-flash: clean — no dangling references, braces
  balanced, no unused imports.

## CI
- Compile gate = GitHub Actions on push (per AGENTS.md — no local Gradle).
