# Prompt.md — Research & Analysis Log

## DOX Framework

**DOX chain:** `master.md` ← `AGENTS.md` (root) ← `app/AGENTS.md` ← `app/src/main/res/AGENTS.md` ← `Prompt.md` (this file — work log)

## Request Summary

Fix the CI Android resource merge failure for `:app:mergeFdroidDebugResources`, where `strings.xml` failed to flatten `widget_fieldmind_species_desc` because of an invalid escape/unicode parsing issue.

## Context Gathered

- Re-read the applicable DOX chain before editing: `master.md`, root `AGENTS.md`, `app/AGENTS.md`, and `app/src/main/res/AGENTS.md`.
- CI reports `widget_fieldmind_species_desc` as the failing string resource.
- Local `strings.xml` contains `widget_fieldmind_species_desc` with the text `Species count, today's sightings, and last species seen`.
- Android resource parsing can be strict about apostrophes/escapes in string resources; removing the apostrophe avoids the flattening/parser issue without changing app behavior materially.
- Gradle compile/build/lint/test commands remain prohibited by project instructions in this environment, so validation is limited to static checks.

## Implementation Plan

1. Update `widget_fieldmind_species_desc` to avoid the apostrophe in `today's`.
2. Run static XML and diff checks only; do not run Gradle tasks.
3. Commit and push the targeted resource fix.
4. Create a PR record.

## Completion Summary

Implemented a targeted resource fix in `app/src/main/res/values/strings.xml`:
- Reworded `widget_fieldmind_species_desc` from `today's sightings` to `sightings today`, removing the apostrophe that triggered Android resource flattening/parsing failure in CI.

## Verification Notes

- Ran a Python XML parse check for `app/src/main/res/values/strings.xml`; it parsed successfully.
- Ran `git diff --check`; no whitespace errors reported.
- Did not run Gradle compile/build/lint/test commands because repository DOX explicitly prohibits them in this environment.
