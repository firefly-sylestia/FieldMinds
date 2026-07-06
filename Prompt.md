# Prompt.md — Research & Analysis Log

## DOX Framework

**DOX chain:** `master.md` ← `AGENTS.md` (root) ← `Prompt.md` (this file — work log)

## Request Summary

Fix the CI Kotlin compilation failure reported for the fdroid debug build after recent field capture, compass/level, data tools, detail, and task-detail changes.

## Context Gathered

- Re-read `master.md`, root `AGENTS.md`, `app/AGENTS.md`, `features/field/AGENTS.md`, `features/field/data/AGENTS.md`, and `features/field/presentation/AGENTS.md` before editing data and presentation files.
- The CI log included Parcelize model errors, missing imports/constants in the compass/level screen, composable-theme reads inside Canvas draw lambdas, malformed syntax in observe/detail screens, and missing task-detail picker state.
- Gradle compile/build/lint/test commands remain prohibited by project instructions in this environment, so validation is limited to static checks.

## Implementation Plan

1. Restore Parcelable supertypes for `@Parcelize` models used by persisted capture state.
2. Fix compass/level imports, sensor constants, Canvas color capture, math type conversions, and Row-scoped weight usage.
3. Fix Data Tools theme color scoping.
4. Repair malformed braces/syntax in detail and observe screens.
5. Restore task-detail species/evidence picker state and source collections.
6. Run static brace-depth and diff whitespace checks, then commit, push, and create a PR record.

## Completion Summary

Implemented targeted CI compile fixes:
- Added explicit `Parcelable` supertypes to location/weather parcelized models.
- Annotated nested capture state parcelized fields with `@RawValue` and fixed broken observe-screen copy/weather references.
- Fixed compass/level imports, sensor accuracy constants, Canvas-safe Material colors, Float-to-Double math conversions, and weighted tilt gauge parameters.
- Fixed Data Tools color scoping.
- Restored top-level detail helper declarations by repairing project-task/detail braces.
- Restored task-detail species/evidence picker state and data collections.

## Verification Notes

- Ran static Python brace-depth checks for `FieldMindDetailScreen.kt`, `FieldMindTaskDetailScreen.kt`, and `FieldMindObserveScreen.kt`; final brace balances are zero and detail/task helper declarations are top-level.
- Ran `git diff --check`; no whitespace errors reported.
- Did not run Gradle compile/build/lint/test commands because repository DOX explicitly prohibits them in this environment.
