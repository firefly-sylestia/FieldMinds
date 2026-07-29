# Curio App Module (Active Build) — AGENTS.md

## DOX Framework

This file is a child of the DOX hierarchy defined in `master.md`. It follows the root `AGENTS.md` as its parent DOX rail.

**DOX chain:** `master.md` ← `AGENTS.md` (root) ← `app/AGENTS.md` (this file)

Read `master.md` and root `AGENTS.md` first, then this file for app-module-specific contracts.

## Purpose

The `app/` module is the active Android application — **Curio**, a discovery app that hands the user a topic (via "The Spin" roulette) to explore in the real world, then captures what they found into "The Cabinet" library. The full UX/UI spec lives at [`CURIO_SPEC.md`](CURIO_SPEC.md) at the repo root of this module.

The legacy FieldMind codebase is preserved at `app-legacy/`. Curio inherits two things from it: the Material Symbols variable font and `geom.ttf` display typography (see `CURIO_SPEC.md` §0.4 + §0.6). The icon + font files are **copied** into `app/src/main/res/font/` — `app-legacy/` is never modified.

## Ownership

Current files (Phase 1 placeholder state, will be updated by Phase 2 implementation):

- `app/build.gradle.kts` — Module build config (Android application, Compose). **Currently still has Phase 1 placeholder values: `namespace = "fieldmind.research.app"`, `applicationId = "fieldmind.research.app"`. Phase 2 implementation will update both to `com.curio.app`.**
- `app/src/main/AndroidManifest.xml` — Minimal launcher manifest
- `app/src/main/java/fieldmind/research/app/MainActivity.kt` — Placeholder Compose activity (package `fieldmind.research.app/` — will move to `com.curio.app/` in Phase 2)
- `app/src/main/res/values/strings.xml` — `app_name = "FieldMind"` (placeholder; Curio will override)
- `app/src/main/res/values/themes.xml` — `Theme.FieldMindRebuild` (placeholder)
- `app/proguard-rules.pro` — Empty placeholder; release build disables minification
- `app/.gitignore` — Standard `/build`

Canonical design doc:

- `app/CURIO_SPEC.md` — Curio's full UI/UX spec (v2). **Read this before adding any screen or component** — it is the source of truth for design decisions.

## Local Contracts

### Identity
- `namespace = "com.curio.app"` (new package, separate from FieldMind)
- `applicationId = "com.curio.app"` (new install, separate from FieldMind; users install Curio as a separate app)
- `minSdk = 26`, `targetSdk = 37`, `compileSdk = 37`
- `versionName = "0.1.0-curio"`, `versionCode = 1`
- No product flavors yet — flavors will be reintroduced in a later phase when the real UI lands
- Inherits `material_symbols_outlined.ttf` + `geom.ttf` from `app-legacy/src/main/res/font/` (copied into `app/src/main/res/font/` during initial scaffold — see `CURIO_SPEC.md` §0.4 + §0.6)

### Curio Database (separate from FieldMind)
- Curio installs as a separate app under `applicationId = "com.curio.app"` — its data directory is `/data/data/com.curio.app/databases/curio_database` (DB name TBD in Phase 2).
- FieldMind's data lives in FieldMind's separate install at `/data/data/fieldmind.research.app/databases/fieldmind_database`. Curio CANNOT access it directly.
- FieldMind data is recoverable only by sideloading the legacy FieldMind APK (built from `app-legacy/`) and using its built-in V3 backup exporter (see `app-legacy/src/main/java/.../data/export/FieldMindExport.kt`).
- The two apps do not share DB names, schemas, or SharedPreferences namespaces — fully isolated.

### UI
- All UI must be Compose. No XML layouts for screens.
- `MainActivity` is the only entry point for now.

## Work Guidance

This module is under active rebuild. The general workflow:

1. **Package layout**: code lives at `app/src/main/java/com/curio/app/...` (not `fieldmind.research.app/...`). Per Phase 2: each Curio screen/feature goes in `app/src/main/java/com/curio/app/features/{feature}/...` (e.g. `features/home/`, `features/spin/`, `features/cabinet/`, `features/capture/`).
2. Wire navigation through `MainActivity`'s `NavHost` (added in Phase 2).
3. Room entities: place under `app/src/main/java/com/curio/app/data/database/...` until the shared `:data` library module is introduced (later phase). Curio's schema is its own — it does NOT inherit FieldMind's 27 entities.
4. Design system primitives live under `app/src/main/java/com/curio/app/ui/theme/...` (CurioTheme, CurioColors, CurioTypography, CurioShapes, CurioIcons).
5. When adding new dependencies, update `gradle/libs.versions.toml` first, then reference via `libs.` in `app/build.gradle.kts`.
6. Read [`CURIO_SPEC.md`](CURIO_SPEC.md) before adding any screen or component — it is the canonical design source of truth.

## Verification

- `MainActivity` must compile and run after Phase 2 implementation updates `app/build.gradle.kts` to the new `com.curio.app` namespace and applicationId (currently still on the legacy `fieldmind.research.app` from Phase 1's preservation commit).
- DB name will be `curio_database` (TBD) — separate from FieldMind's `fieldmind_database`.
- No background workers, no widgets, no emergency-recovery hooks yet — those arrive in later phases.
- **Known spec-vs-code gap**: as of this writing, `app/build.gradle.kts` still has `fieldmind.research.app` (the Phase 1 placeholder values). The Identity section above describes the target state. Phase 2 implementation will reconcile.
- **CI gate**: this environment has no Android SDK, so CI on push to `revamp` is the source of truth for compilation. If CI fails on the placeholder, fix the placeholder before adding real screens.

## Child DOX Index

- [`CURIO_SPEC.md`](CURIO_SPEC.md) — Canonical UX/UI spec for Curio (per `master.md` Update After Editing rule: changes affecting screen design, navigation, or component behavior go in this doc, not buried in code comments).
- (Future) `app/src/main/java/com/curio/app/features/{home,spin,cabinet,capture}/AGENTS.md` — per-screen feature contracts, added as each screen is built in Phase 2+.
- (Future) `app/src/main/java/com/curio/app/ui/theme/AGENTS.md` — design system primitive contracts, added when CurioTheme / CurioColors / CurioTypography / CurioIcons land.