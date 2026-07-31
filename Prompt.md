# Profile redesign + daily reminder — completion summary

## Request

Redesign the Curio Profile screen so it is polished and consistent, repair the hero-card spacing, make dialog/actions functional, and fix daily reminders and related settings behavior.

## Changes

- Rebuilt `ProfileScreen` around a compact identity hero, balanced stats strip, level progress card, grouped preferences, category activity, recent captures, and an About/diagnostics card.
- Fixed hero-card spacing and visual hierarchy with a constrained avatar/content row, aligned action pills, gradient surface, and responsive text truncation.
- Replaced inert Profile actions with working display-name, recording-quality, reminder-time, version, capture-detail, cabinet, category-management, crash-report, crash-log, bug-report, and onboarding actions.
- Added Android daily reminder scheduling with notification permission handling, a dedicated notification icon/channel, reboot/clock/timezone rescheduling, and one-shot local-time alarms that re-anchor after delivery.
- Wired reminder preferences to reactive Compose state across Home, Profile, and Settings; changing the reminder time reschedules the alarm immediately.
- Made Settings use an explicit recording-quality dialog, informational rows for Last backup/Version, complete reminder time choices, and restore-state refresh without requiring an app restart.
- Re-applied restored theme/reminder state after backup restore and kept the reminder alarm synchronized.
- Added lifecycle refresh for remembered Profile/Settings values and sourced displayed version text from `BuildConfig.VERSION_NAME`.
- Added the lifecycle-runtime-compose dependency and fixed the level card's max-level threshold.

## Verification

- `scripts/check_braces.py` reports all changed Kotlin/XML files as `BALANCED`.
- Targeted static checks pass: `git diff --check`, delegated-state assignment check, lifecycle dependency/import check, and Curio icon declaration check.
- `code-reviewer-luna` found no blocking implementation issues in the final review.
- No Gradle compile/build/test/lint task was run because the repository's AGENTS.md explicitly forbids local Gradle validation; CI remains the compilation source of truth.
- The active Curio module has no in-app changelog screen; the frozen legacy FieldMind changelog was intentionally not modified.
