# Request: Polish Settings screen grouping

## Analysis

The Settings screen (`SettingsScreen.kt`) is now the single home for all
settings (Profile page duplicates were removed in the previous request). Its
7 cards already had icon-chip headers (`CurioCardHeader`), so the polish was
to add **section labels** between logical groups. The shared
`CurioSectionLabel` component (in `CurioSettingsComponents.kt`) existed but
was completely unused — the perfect tool.

## Changes (app/src/main/java/com/curio/app/features/settings/SettingsScreen.kt)

Added 4 section labels to the LazyColumn, reusing `CurioSectionLabel`:

- **General** — Profile (display name), Appearance (theme)
- **Preferences** — Recording (audio quality), Notifications (reminder),
  Categories (manage)
- **Data** — Backup & restore
- **Support** — About Curio (replay intro, version)

Also added the `CurioSectionLabel` import (alphabetically placed between
CurioCardHeader and CurioSettingsCard) and small `// ──` group comments.

## Validation

- code-searcher: 5 matches — 1 import (line 64) + 4 labels (lines 301/351/
  439/462), all correctly wrapped in `item {}`.
- code-reviewer-deepseek-flash: clean pass — import resolves, item wrapping
  correct, no brace imbalance, activates previously-dead shared code.
  Cosmetic note only: first label sits ~26dp below the header (content
  padding + fixed label top padding) — accepted as-is.
- No local gradle build per AGENTS.md — CI owns compilation on push.

## Status

Complete. Commit `TBD` on branch `revamp`.
