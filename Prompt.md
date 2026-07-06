# Prompt.md — Research & Analysis Log

## DOX Framework

**DOX chain:** `master.md` ← `AGENTS.md` (root) ← `Prompt.md` (this file — work log)

## Request Summary

Implement FieldMind QA/UI fixes from the provided task-stubs:
- Persist and strengthen the developer full app tester report.
- Add non-fatal crash capture verification.
- Stabilize hardware-back behavior in the tab container.
- Reserve bottom content space for the floating quick-capture FAB/nav pill.
- Make the live weather widget more adaptive and less prone to clipping.
- Improve weather cloud/rain animation continuity and rain visibility.
- Fix screenshot-block disabling by making `FLAG_SECURE` follow the explicit screenshot toggle.
- Improve dark-mode elevation/depth on touched surfaces.
- Add developer-test checklist categories for UI overlap/clipping risks.

## Context Gathered

- `DevFullAppTestRunner.kt` stored reports only in local Compose state and primarily performed smoke/static checks.
- `CrashReporter.kt` persisted uncaught crash stack traces to `AppSettings` but had no non-fatal capture API for tester verification.
- `FieldMindNavigation.kt` uses a single `field_tab_container` route and custom tab state with `BackHandler` inside `AllTabScreen`.
- `FieldMindHomeScreen.kt` used hardcoded bottom padding (`96.dp` content, `112.dp` FAB) while the floating nav pill is overlaid.
- `MainActivity.kt` set `FLAG_SECURE` when either screenshot protection was enabled or app preview mode was not `Normal`, so disabling the screenshot option could still leave screenshots blocked.
- `AnimatedWeatherScene.kt` tied rain cloud drift to fast rain progress and rendered rain particles only after the physics system filled.

## Implementation Notes

- Added persisted latest developer report storage to `AppSettings`.
- Added `CrashReporter.recordNonFatal()` and a dev-runner sentinel test.
- Updated dev-runner reports with run id, app/device/settings metadata, full stack traces, persisted checkpoints, and UI layout checklist entries.
- Changed non-home tab hardware back to return to Home without popping the NavHost route.
- Added a bottom padding parameter for Home and passed reserved chrome space from navigation.
- Made Home weather metric rows wrap using `FlowRow` and made camera dialog secure policy follow screenshot protection.
- Changed `MainActivity` secure-window application to use only the explicit screenshot toggle.
- Slowed rain cloud drift, normalized/wrapped clouds in `drawCloud`, and added deterministic fallback rain streaks.

## Verification

- Do not run Gradle build/lint/test locally per root AGENTS.md.
- Use static checks only (`git diff --check`, targeted file inspection/search).
