# Curio GitHub Configuration — AGENTS.md

## DOX Framework

This file is a child of the DOX hierarchy defined in `master.md`. It follows the root `AGENTS.md` as its parent DOX rail.

**DOX chain:** `master.md` ← `AGENTS.md` (root) ← `.github/AGENTS.md` (this file)

Read `master.md` and root `AGENTS.md` first, then this file for GitHub-specific contracts.

## Purpose

CI/CD automation and issue tracking templates for the Curio Android repository on GitHub.

## Ownership

- `.github/workflows/release.yml` — Tag-triggered release build workflow
- `.github/workflows/android.yml` — PR/branch CI build workflow
- `.github/ISSUE_TEMPLATE/bug-report.yml` — Structured bug report form
- `.github/ISSUE_TEMPLATE/feature-request.yml` — Structured feature request form
- `.github/PULL_REQUEST_TEMPLATE.md` — PR description template

## Local Contracts

### Workflows

#### `android.yml` — Android CI
- Triggers: `push` / `pull_request` on the active development branches
- Jobs: `check` (lint) and `build` (assemble debug + release APKs)
- Builds the flavorless Curio debug and release APKs
- Artifacts retained for 14 days

#### `release.yml` — Release Build
- Triggers: tag push matching `v*`
- Builds `assembleRelease` for Curio
- Creates GitHub Release with generated release notes
- Pre-release flag set for tags containing `alpha`, `beta`, or `rc`

### Issue Templates
- Bug reports require: description, steps to reproduce, expected behavior, device/OS info
- Feature requests require: problem statement, proposed solution, alternatives considered

### Secrets Required
- `KEYSTORE_BASE64` — Base64-encoded Android keystore file
- `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` — Keystore credentials

## Work Guidance

- When adding new workflows, keep naming consistent with existing patterns
- Secrets are injected via GitHub Actions secrets; document new secret requirements here
- APK artifacts are collected from `app/build/outputs/apk/`
- PRs should include at minimum a successful `lint` run before merging

## Verification

- Workflow syntax validated by GitHub Actions on push
- Manual: `act` tool for local workflow testing
- PRs must pass CI checks before merge

## Child DOX Index

No child AGENTS.md files defined yet.
