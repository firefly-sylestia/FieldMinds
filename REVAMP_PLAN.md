# FieldMind Revamp Plan

This document tracks the multi-phase rewrite of FieldMind. The original Android app is preserved at `app-legacy/` (NOT built, NOT shipped) as a reference snapshot and emergency data-recovery tool. The new app lives at `app/` and is being rebuilt incrementally.

## Status

- ✅ **Phase 1 — Preservation** (current): Legacy `app/` moved to `app-legacy/` via `git mv`. New minimal `app/` placeholder created. Same `applicationId` and Room DB name so user data persists across the upgrade.
- ⏳ **Phase 2 — UX shell**: New navigation graph, theme system, empty placeholder screens for every feature. Compose Material3.
- ⏳ **Phase 3 — Read-only legacy DB visibility**: New app reads the v16 Room DB on disk. All user data visible, no edits.
- ⏳ **Phase 4 — New data model + writes**: Pick the new V4 schema. Drop dead fields. Bump Room to v17 with migrations. Enable writes through the new UI.
- ⏳ **Phase 5 — V4 export writer + V3 read-only import**: JSON + gzip + optional encryption. V3 reader preserved forever.
- ⏳ **Phase 6 — Polish**: Animation framework, journal-style picker, iconography, app icon.

## Hard Constraints

These do not change across phases:

1. **`applicationId` = `fieldmind.research.app`**. Single installed app. New installs upgrade cleanly over the legacy one.
2. **Room DB filename = `fieldmind_database`**. User data persists across the upgrade. No destructive migration.
3. **SharedPreferences namespace = `fieldmind.research.app_preferences`**. Same reason.
4. **V3 backup format (`fieldmind-archive-v3`) stays importable forever**. Old `.fieldmind` backup files from any prior install work in the new app.
5. **`app-legacy/` is preserve-only**. Never modified, never added to Gradle, never linked. Recovery only.

## Decision Log

| Decision | Choice | Why |
|---|---|---|
| Two modules vs. one | One installed app + `app-legacy/` snapshot | User chose "keep it one the original one" — single installable app, no side-by-side |
| Data preservation scope | All DB entities migrated | User chose "all DB entities migrated" — keep every observation / note / project / etc. |
| Backup compat | Read-only import old backups, no export old format | User chose "read-only" — V3 in, V4 out |
| Rollout | Phased | User chose "data + splash → screens → polish" |
| Where legacy code lives | `app-legacy/` directory, not git branch | Single working tree, easy to read legacy sources via editor, `git mv` preserves history |
| Package names in new app | Same `fieldmind.research.app.*` | Zero import churn as legacy code is referenced for reference |

## Architecture Invariants

The new app reuses these without modification (until later phases):

- **V3 backup parser** (`FieldMindExport.kt: parseArchiveJson`) — frozen, lives in `app-legacy/` for reference; the new app will re-implement the read path in its own `data/export/` package, byte-identical to the legacy parser
- **Room DB schema (v16)** — frozen until Phase 4 introduces V4
- **All 27 entity types** — preserved in the DB; the new app reads them and presents them through new UI

## Risks

- **Per-app file storage**: Android 11+ scoped storage means the new app cannot read photo URIs written by the legacy app without a migration step. Mitigation: in Phase 5, copy media to the new app's own scoped storage on first launch.
- **CI build time**: CI now builds the placeholder + (in Phase 4+) the legacy module's tests if any. Mitigation: keep placeholder minimal until the rebuild begins.
- **SharedPreferences key drift**: If the new app reads legacy prefs but writes new ones under different keys, users see default settings. Mitigation: read legacy prefs, copy to new keys, default new keys to legacy values where reasonable.

## When to Start Phase 2

Phase 2 (UX shell) starts once:
- ✅ Phase 1 (this phase) is merged into the revamp branch
- The team confirms the new app placeholder installs and launches cleanly on a test device
- The team decides on the new design system (Compose Material3 expressive, Material You, custom?)

See the conversation history in `Prompt.md` for the detailed decision-making that produced this plan.