# Revamp Guardrails

This document codifies the rules for the `app-legacy/` preserve-only snapshot. It is a **revamp-level constraint**, not a property of the legacy module itself, so it lives at the repo root rather than under `app-legacy/`.

For the historical structural documentation of the legacy module (what each package contained, which classes implemented which interfaces, etc.), see `app-legacy/AGENTS.md` (frozen at the revamp-branch base commit).

## The contract

### `app-legacy/` is preserve-only

The directory `app-legacy/` is a frozen snapshot of the original FieldMind Android app at the git revision where the `revamp` branch was cut. It exists for two reasons:

1. **Reference**: developers working on the new `app/` can read the legacy code to understand prior decisions, original schema, business logic, etc.
2. **Emergency data recovery**: if the new app ever corrupts user data on a device, the legacy snapshot is a known-good fallback that can be checked out, compiled, and used to extract data.

### Hard rules

These rules are non-negotiable:

- **NEVER modify files under `app-legacy/`.** The snapshot is bit-identical to the revamp-branch base commit. Any change breaks both purposes above.
- **NEVER add `app-legacy/` to `settings.gradle.kts`.** It is intentionally not a Gradle module. The new `app/` does not link to it.
- **NEVER reference `app-legacy/` sources from the new `app/` module via Gradle.** The new app reads the Room DB on disk; it does not compile against legacy source.

### Soft rules (recommended)

- **NEVER edit `app-legacy/` even for "tiny fixes".** If something in legacy needs fixing urgently, the right path is: fix it in the new app's replacement code. If no replacement exists yet, branch from the pre-revamp commit, ship a hotfix to a separate APK, then return to the revamp.
- **NEVER delete `app-legacy/`.** Even after Phase 6 (polish) is complete, keep the directory. It costs ~10 MB on disk and is the cheapest insurance against data loss.

## When you need to read legacy code

- **Understanding a legacy class** (Room schema, v3 backup parser, export pipeline, etc.): read it directly via your editor or `cat`. Do not compile it.
- **Comparing current vs. legacy behavior**: the git history of the renamed files (e.g. `git log --follow app-legacy/src/main/java/.../FieldMindDatabase.kt`) shows every change leading up to the snapshot.

## When you need to compile the legacy app

This should never be necessary in normal flow. If it is:

1. Create a new throwaway branch from the pre-revamp commit: `git checkout -b legacy-hotfix 629c7276`
2. In that branch, the legacy `app/` directory is back at its original path. Build it as documented in `wiki/Build-Instructions.md`.
3. Use the resulting APK only for data extraction. Do not merge changes back into `revamp` or `main`.

Do **not** copy `app-legacy/` contents back into `app/` in your working tree — that recreates the original dual-tree problem this revamp is solving.

## Verification

These commands should all return zero output. If any match, the contract is broken.

```bash
# app-legacy/ should not be a Gradle module
grep -r "include.*app-legacy" settings.gradle.kts

# app-legacy/ should be byte-identical to the snapshot
git diff origin/main..HEAD -- app-legacy/

# new app/ should not import anything from app-legacy/
grep -r "import fieldmind.research.app.features.field" app/src/main/java/
```

The third command will eventually have legitimate matches as the new app re-implements legacy functionality (e.g. when Phase 5 brings back the V3 backup reader). When that happens, add a comment in the new code explaining why it's a deliberate re-implementation, not a Gradle link.

## Why this exists separately from `app-legacy/AGENTS.md`

`app-legacy/AGENTS.md` is the historical structural documentation of the legacy module. It was preserved verbatim from `main:app/AGENTS.md` at the moment the `revamp` branch was cut. It describes the legacy app as if it were active, because that's what it was when that AGENTS.md was last authored.

This document, in contrast, describes the **current state**: the legacy module is frozen. Mixing both purposes in one file would either (a) overwrite the historical reference or (b) bury the freeze rules under package-level structural detail. Splitting them keeps both legible.

## When these guardrails can be relaxed

Probably never. The legacy snapshot is intended to outlive the revamp itself — it is the recovery safety net for years. If the project is ever migrated to a different architecture (KMP, new tech stack, etc.), the legacy snapshot stays as a reference for the original Android-only design.