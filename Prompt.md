# Prompt.md — Research & Analysis Log

## DOX Framework

**DOX chain:** `master.md` ← `AGENTS.md` (root) ← `Prompt.md` (this file — work log)

## Request Summary

Targeted FieldMind Android stability/security/weather repair pass:
- Crash reporter must show a reliable non-blank crash screen and persist richer crash reports.
- Collaboration sharing must not crash when no share target exists; fake export button must route to real export flow.
- App lock must use exact 4/5/6 digit PINs, in-app numpad, consistent failed-attempt policies, cooldowns, biometric-required enforcement, and safer panic-lock handling.
- Auto-lock must respect background timeout settings.
- Open-Meteo must work without an API key and weather failures must expose actionable diagnostic state.
- DevFullAppTestRunner must be cancellable, less hang-prone, restore settings, and test lock/weather/crash policies.

## Context Gathered

- DOX chain read: `master.md`, root `AGENTS.md`, `app/AGENTS.md`, field data/presentation AGENTS, shared/infrastructure/resource AGENTS, `fastlane/AGENTS.md`.
- Build/lint/test Gradle commands are prohibited in this environment.
- Open-Meteo official docs/search confirm free API has no API key requirement; pricing/customer docs show customer endpoint uses `customer-api.open-meteo.com` and `apikey` query parameter.
- Existing lock screen uses a device keyboard `OutlinedTextField`, cooldown threshold hardcoded at 3 despite UI saying 5, and failed policies are partially unwired.
- Existing crash reporter starts crash activity but returns from uncaught exception handler without a re-entrancy guard or explicit process termination.
- Existing DevFullAppTestRunner has long per-test timeout and no cancellation/restoration wrapper.

## Implementation Plan

1. Add testable lock/security policy helpers and wire lock screen to them.
2. Replace unlock PIN text field with in-app numpad and exact-length verification.
3. Harden crash reporting and crash activity fallback UI.
4. Harden Collaboration share flows and route export to Export Studio.
5. Improve auto-lock timeout mapping and lifecycle manager integration.
6. Fix Open-Meteo optional key behavior, URL builder, and weather diagnostics/fresh location fallback.
7. Improve DevFullAppTestRunner cancellation/progress/settings restore and add policy tests.
8. Update What's New + fastlane changelog.
9. Run allowed static checks only, then commit/push and create PR.

## Completion Summary

Implemented targeted fixes:
- Hardened crash reporting with re-entrancy guard, richer crash metadata, guarded persistence, crash-process launch, and process termination after dispatch.
- Replaced crash screen with minimal Compose UI plus native fallback.
- Hardened Collaboration share/invite actions with clipboard fallback and routed export action to Export Studio.
- Added `LockSecurityPolicy` and wired lock screen to exact PIN length, in-app numpad, 5-attempt policy, cooldown countdown, biometric-required mode, and non-destructive panic reset.
- Improved background auto-lock timeout wiring and reset lock signal after unlock.
- Fixed Open-Meteo free tier as no-key-required, moved URL construction to `HttpUrl.Builder`, and added weather diagnostics/fresh-location fallback.
- Made DevFullAppTestRunner cancellable, shorter-timeout, settings-restoring, and added lock/weather/crash policy smoke checks.
- Updated in-app What's New and fastlane changelog.

## Follow-up: Card→InfoCard CI Compilation Fix (July 6, 2026)

After the main stability/security pass, CI reported 25 compilation errors:
- `FieldMindLibraryScreen.kt` (18 errors): `tonalElevation`/`shadowElevation` not found on `Card`
- `SpeciesBrowserScreen.kt` (3 errors): same cause
- `WeatherDatabaseScreen.kt` (4 errors): same cause

**Root cause:** Material3 `Card` doesn't expose `tonalElevation`/`shadowElevation` as named parameters — those are `Surface`-only. The project's custom `InfoCard` composable wraps `Surface` and accepts both.

**Fix:** Replaced all 25 raw `Card(tonalElevation=…, shadowElevation=…)` calls with `InfoCard(…)`.

**Docs:** Updated `docs/CI_ERROR_POSTMORTEM.md` with Cycle H and SMART instruction #11.

## Verification Notes

- Ran `git diff --check` successfully.
- Ran ripgrep/static checks for the key repaired patterns.
- Did not run Gradle build/lint/test commands because repository DOX explicitly prohibits them in this environment.
