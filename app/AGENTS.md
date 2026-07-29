# FieldMind App Module (Rebuild-in-Progress) — AGENTS.md

## DOX Framework

This file is a child of the DOX hierarchy defined in `master.md`. It follows the root `AGENTS.md` as its parent DOX rail.

**DOX chain:** `master.md` ← `AGENTS.md` (root) ← `app/AGENTS.md` (this file)

Read `master.md` and root `AGENTS.md` first, then this file for app-module-specific contracts.

## Purpose

The `app/` module is the active Android application under rebuild. It currently ships a **single-screen placeholder** that tells the user "FieldMind is being rebuilt — your saved research data is safe." The real FieldMind UX is being implemented incrementally across future phases — see `REVAMP_PLAN.md` at the repo root.

The legacy FieldMind codebase (120+ Kotlin files, full Compose UI, full Room data layer, widgets, workers, all of it) is preserved at `app-legacy/` for reference and emergency data recovery. See `app-legacy/AGENTS.md`.

## Ownership

- `app/build.gradle.kts` — Module build config (Android application, Compose, applicationId `fieldmind.research.app`)
- `app/src/main/AndroidManifest.xml` — Minimal launcher manifest
- `app/src/main/java/fieldmind/research/app/MainActivity.kt` — Placeholder Compose activity
- `app/src/main/res/values/strings.xml` — `app_name = "FieldMind"`
- `app/src/main/res/values/themes.xml` — `Theme.FieldMindRebuild`
- `app/proguard-rules.pro` — Empty placeholder; release build disables minification
- `app/.gitignore` — Standard `/build`

## Local Contracts

### Identity
- `namespace = "fieldmind.research.app"` (must match legacy for SharedPreferences + Room DB continuity)
- `applicationId = "fieldmind.research.app"` (must match legacy for user data persistence across upgrade)
- `minSdk = 26`, `targetSdk = 37`, `compileSdk = 37`
- `versionName = "0.51.0-rebuild"`, `versionCode = 1`
- No product flavors yet — flavors (`fdroid` / `github`) will be reintroduced in a later phase when the real UI lands

### Room DB Continuity
- The on-device Room database filename **MUST** remain `fieldmind_database` so data created by the legacy app is readable by the new app
- The on-device SharedPreferences namespace **MUST** remain `fieldmind.research.app_preferences` for the same reason
- Do **NOT** drop or rename any DB tables until the new app has a verified migration path (Phase 4+)

### UI
- All UI must be Compose. No XML layouts for screens.
- `MainActivity` is the only entry point for now.

## Work Guidance

This module is under active rebuild. The general workflow:

1. Add the screen / feature in a new package under `app/src/main/java/fieldmind/research/app/features/{feature}/...`
2. Wire navigation through `MainActivity`'s `NavHost` (added in a later phase)
3. When adding Room entities, place them in `app/src/main/java/fieldmind/research/app/data/database/...` until the shared `:data` module is introduced (Phase 4+)
4. When adding new dependencies, update `gradle/libs.versions.toml` first, then reference via `libs.` in `app/build.gradle.kts`

## Verification

- `MainActivity` must compile and run
- `applicationId` and `namespace` must match legacy values
- DB name `fieldmind_database` must remain unchanged
- No background workers, no widgets, no emergency-recovery hooks yet — those arrive in later phases
- **CI gate**: this environment has no Android SDK, so the placeholder is not built locally. CI on push to `revamp` is the source of truth for compilation. If CI fails on the placeholder, fix the placeholder before adding real screens.

## Child DOX Index

No child AGENTS.md files defined yet. The placeholder app is intentionally flat.