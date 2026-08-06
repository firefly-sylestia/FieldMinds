# Curio GitHub Configuration — AGENTS.md

## DOX Framework

This file is a child of the DOX hierarchy defined in `master.md`. It follows the root `AGENTS.md` as its parent DOX rail.

**DOX chain:** `master.md` ← `AGENTS.md` (root) ← `.github/AGENTS.md` (this file)

Read `master.md` and root `AGENTS.md` first, then this file for GitHub-specific contracts.

## Purpose

GitHub Actions automation and contributor templates for the Curio Android repository.

## Ownership

- `.github/workflows/android.yml` — Branch and pull-request verification
- `.github/workflows/release.yml` — Tag-triggered signed release publishing
- `.github/ISSUE_TEMPLATE/bug-report.yml` — Curio Android bug report form
- `.github/ISSUE_TEMPLATE/feature-request.yml` — Curio product and UX request form
- `.github/PULL_REQUEST_TEMPLATE.md` — Curio pull-request review template

## Local Contracts

### Android CI workflow

`android.yml` runs on pushes and pull requests targeting `revamp`, plus manual dispatch. It:

- Validates all topic catalogs with `python3 scripts/validate_topics.py`.
- Installs Android platform API 37 and build-tools 35.0.0, then runs the Gradle `lintDebug`, `validateTopics`, and `assembleDebug` checks in GitHub Actions.
- Uploads lint reports and the debug APK for 14 days.
- Uses no signing secrets and never publishes an APK.
- Cancels an older in-progress run for the same ref when a newer run starts.

### Release workflow

`release.yml` runs only for `v*` tags. It:

- Requires `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, and `KEY_PASSWORD`.
- Installs Android platform API 37 and build-tools 35.0.0, decodes the repository keystore, runs `validateTopics assembleRelease`, and verifies the APK signature is not the Android debug key.
- Publishes the release APK through a GitHub Release, marking `alpha`, `beta`, and `rc` tags as prereleases.
- Never falls back to debug signing for a published release.

### Contributor templates

- Bug reports collect reproducible steps, expected and actual behavior, Curio area, app/device versions, logs, and sanitized screenshots.
- Feature requests collect the user problem, proposed experience, product area, expected scope, alternatives, and references.
- Pull requests identify change type, affected Curio experience, validation, visual evidence, data/permission impact, and reviewer checks.

### Secrets

Only the release workflow consumes signing secrets:

- `KEYSTORE_BASE64` — Base64-encoded Android keystore
- `KEYSTORE_PASSWORD` — Keystore password
- `KEY_ALIAS` — Signing key alias
- `KEY_PASSWORD` — Signing key password

## Work Guidance

- Keep workflow names, artifact names, and user-facing copy Curio-specific.
- Keep every workflow and template focused on the current Curio product and its Android delivery path.
- Keep release signing mandatory and never commit keystores or decoded credentials.
- Update this contract whenever workflow triggers, required secrets, artifact behavior, or template fields change.
- Do not run Gradle compile, build, lint, or test commands in the local workspace; CI performs those checks.

## Verification

- Validate changed YAML with a YAML parser or GitHub's workflow checks when available.
- Run `git diff --check` and inspect the rendered template structure.
- Run `python3 scripts/validate_topics.py` when repository changes touch the Curio data or CI validation path.
- Confirm no secrets, generated APKs, or release keystores are tracked.

## Child DOX Index

No child AGENTS.md files defined yet.
