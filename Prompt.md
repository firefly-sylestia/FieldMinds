# Prompt — Profile + Settings redesign (v7.53)

## Request
Redesign Profile and Settings to match the Home/detail visual language, use compact options, and move main card/deck customization into a new always-visible Experiments page reachable from Settings.

## Decisions
- Profile: Home-style identity hero with compact detail-style cards.
- Settings: compact hub with subpages.
- Experiments: reachable from the Settings hub and always visible.

## Changes
- Added `SettingsHubScreen` as the compact Settings root with Personalize, Explore, and Safety & support groups.
- Added Appearance, Notifications, Recording, Backup & restore, and About subpage routing.
- Added `ExperimentsScreen` and moved main-card, deck-card, Material blend, 3D button, pastel crown, Smart Spin, Smart density, and voice-to-text controls there while preserving existing `AppPreferences` keys and setters.
- Added `BackupToolsScreen` for Curio backup/restore and additive FieldMind archive import, retaining previews, confirmations, status feedback, and last-backup display.
- Preserved notification permission requests and restored overlay special-access handling, including reminder-time chips and lifecycle refresh after returning from Android settings.
- Reduced Profile list/card spacing and tightened lane/stat card internals without removing content or quality.
- Kept `SettingsScreen` as a compatibility alias to the new hub so old callers do not retain a second settings implementation.
- Consolidated backup navigation onto `SETTINGS_DATA` and removed the redundant tools route.

## Validation
Brace checks are BALANCED for all changed/new Kotlin files and `git diff --check` is clean. No local Gradle build/compile/lint/test was run because the repository forbids Android build commands here; CI remains the compilation source of truth.
