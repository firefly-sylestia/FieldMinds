# Prompt.md — Research & Analysis Log

## DOX Framework

**DOX chain:** `master.md` ← `AGENTS.md` (root) ← `app/AGENTS.md` ← `infrastructure/workers/` (new sub-tree created this session) + `features/field/presentation/AGENTS.md` (where new screens compose in)

## Request Summary

Add two in-app features:
1. **In-app update checker** — GitHub Releases only, with an **on-startup overlay/banner** that surfaces when a newer release is available.
2. **In-app bug reporter** — Sends bug reports directly to the GitHub repo via a Cloudflare Worker proxy (the user said "idk how to setup" so the proxy comes with full deployment docs).

## Context Gathered

- Read `AGENTS.md` (root), `app/AGENTS.md`, `app/src/main/java/fieldmind/research/app/util/CrashReporter.kt`, `.github/ISSUE_TEMPLATE/bug-report.yml`, `.github/workflows/release.yml`, `app/build.gradle.kts`, `app/src/main/java/fieldmind/research/app/shared/data/model/AppSettings.kt`, and `FieldMindSettings.kt`.
- Confirmed repo slug = `firefly-sylestia/FieldMinds`, project renamed from Rhythm, distributes via GitHub Releases + F-Droid (no Play Store).
- `CrashReporter` already builds rich crash logs and stores them locally via `AppSettings.addCrashLogEntry`. The reporter will reuse this state — no new privacy surface.
- Existing `bug-report.yml` issue template fields must be mirrored in the Android form so the receiver parser is happy.
- `AGENTS.md` forbids Gradle commands locally; validation runs in CI.

## Decisions (confirmed with the user)

Q1 — **Reporter backend** → Cloudflare Worker. User said "idk how to setup" → full step-by-step deploy guide provided.
Q2 — **Update source** → GitHub Releases only. **On-startup overlay** (top slide-down banner), not just a settings tile.

## Implementation Plan

1. Cloudflare Worker source (`worker.js`) + wrangler config + comprehensive README + in-app architecture doc.
2. Update backend with reviewer-driven fixes (IPv6 regex dedup, NaN-safe Content-Length guard).
3. *(Followup)*: AppSettings additions, build.gradle.kts `BUG_REPORTER_URL` buildConfigField, the Kotlin services (`UpdateChecker`, `BugReporter`, `BugReportSanitizer`), the Compose UI (`UpdateBannerOverlay`, `FieldMindBugReportScreen`), and Settings-tile + Navigation-route wiring.

## Completion Summary (this turn)

Cloudflare-side deliverable shipped, reviewer-passed, fixes applied, ready to deploy:

- `infrastructure/workers/bug-reporter/worker.js` — Worker source with PII sanitization, soft-fail semantics, 60 KB body limit, GitHub Issues API forwarding. POST-only guard. Pre-flight `Content-Length` check (with `Number.isFinite` to close NaN bypass). Consolidated IPv6 regex (single regex covers bare + zone-id). Three reviewer passes — clean.
- `infrastructure/workers/bug-reporter/wrangler.toml` — sample CLI config (no real secrets).
- `infrastructure/workers/bug-reporter/README.md` — full 8-step deploy guide from "I have never used Cloudflare" to "I shipped a tested smoke test", plus a zero-infra fallback (web-URL) path that requires no Worker at all.
- `docs/UPDATE_CHECKER_AND_BUG_REPORTER.md` — server contract, PII sanitization rationale, recommended followup file tree + integration mount points for the Kotlin side. *(Worker-contract clarification edit could not be persisted via str_replace due to a byte-mismatch on the file content; expected behavior unchanged, just the prose hasn't been updated to flag appVersion/installMethod as body-embedded rather than top-level fields.)*
- `Prompt.md` — this log.

## Verification Notes

- Spawned `thinker-with-files-gemini` for architecture review before writing (passed).
- Spawned `code-reviewer-minimax-m3` four times on the worker.js — three review passes plus one to validate the IPv6 dedup + NaN hardening. All clean.
- Did not run Gradle commands because the root `AGENTS.md` prohibits them in this environment; CI will validate on push.

## What's left (followups the user can confirm)

1. **Implement the in-app Kotlin scaffolding** — `UpdateChecker`/`BugReporter` services, `UpdateBannerOverlay`/`FieldMindBugReportScreen` Composables, AppSettings + FieldMindSettings new keys, `buildConfigField BUG_REPORTER_URL`, register the new `FieldMindScreen.BugReport` route, add the Settings tiles ("Check for updates", "Report a bug", "Auto-attach crash log"). Roughly 8 new/modified files.
2. **Add changelog + Fastlane entries** (v0.46.1 patch and `fastlane/metadata/android/en-US/changelogs/2104.txt`) for these features.
3. **Deploy the Worker** following the new README, paste the resulting URL into `build.gradle.kts`, and end-to-end test with curl (covered in `README.md` §5) then with a debug APK.
