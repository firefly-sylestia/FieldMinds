# Profile & Settings Screens — Consistency + Unhide Settings

## Request

User: "fix the profile and settings screen as they are above as different and the settings are hidden" — i.e. (1) Profile and Settings render inconsistently (different visual language), and (2) the Settings screen is unreachable (hidden — `CurioRoutes.SETTINGS` was registered in the nav host but nothing navigated to it).

## Analysis

- Profile used rich 28dp `ProfileCard`s (surfaceContainerLow, tonalElevation 3, border) with icon-chip `CardHeader`s and arrow `ProfileSettingRow`s.
- Settings used flat 14dp `SettingsItem`/`SettingsToggle` rows directly on the background with plain-text `SectionHeader`s — a visibly different language.
- No entry point to `CurioRoutes.SETTINGS` existed anywhere (Home drawer → PROFILE; Profile had no settings link).

## Plan

- Create shared primitives in `ui/components/CurioSettingsCard.kt`: `CurioSettingsCard` (28dp paper card), `CurioCardHeader` (icon-chip), `CurioSettingsRow` (arrow row), `CurioSettingsInfoRow`, `CurioSettingsDivider`, plus shared top-level `formatHour`.
- Restyle SettingsScreen to the card language using the shared components, preserving ALL logic (state, lifecycle observer, backup/restore launchers, notification permission, name/quality/restore/status dialogs, reminder LazyRow).
- ProfileScreen: use the shared components; delete its private `ProfileCard`/`CardHeader`/`ProfileSettingRow`/`ProfileDivider`; add a gear `Surface` button in the top bar → `CurioRoutes.SETTINGS` (unhides Settings).
- Clean unused imports in both screens; remove duplicate `formatHour` copies in favor of the shared one.
- Validate braces/refs/imports + code review; commit & push.

## Completion Summary

- Shared `CurioSettingsCard.kt` created; both Profile and Settings now render from the exact same card primitives (can never drift).
- Settings restyled: 7 cards (Profile, Appearance, Recording, Notifications, Categories, Backup & restore, About) with icon-chip headers + arrow rows; reminder time chips use shared `formatHour` (was `String.format("%02d:00")`).
- Profile top bar gained a gear button (mirror of `CurioBackButton`) navigating to Settings — the previously hidden screen is now reachable.
- Private duplicates + now-unused imports removed from both screens (Profile: ColumnScope, HorizontalDivider; Settings: Box, ColumnScope, size, HorizontalDivider, TextOverflow; CurioForwardArrow was already absent from Settings).
- Validation green: zero leftover private-component refs, `formatHour` defined exactly once (4 refs each screen = import + 3 usages), braces balanced (Profile 183/183·461/461, Settings 125/125·233/233, shared 17/17·60/60), code review clean (2 passes).
- Reviewer nits (non-blocking): Settings cards where header icon == first row icon (DarkMode/Mic/DragHandle/Notifications) read slightly redundant vs Profile's distinct header pattern; gear button hand-rolls CurioBackButton styling. Left as-is to avoid churn.
- Gradle build/lint NOT run (forbidden in this environment; CI validates on push).
