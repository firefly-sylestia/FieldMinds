# Prompt.md — Request Log

## Current Request: Cabinet card preview — stacked badge for portfolio entries

**User request:** Update the Cabinet card preview for portfolio entries to show a small stacked badge of all section format icons instead of just the first one.

## Implementation (complete)

### CurioTopicCard.kt (`CurioEntryCard` — the only Cabinet card component; sole user is CabinetScreen)
- The card's bottom-right corner previously rendered a single `CurioIcon(formatGlyph(entry.format))` — for a Portfolio that's only the FIRST section's format.
- Added `EntryFormatBadges(entry)`:
  - Non-Portfolio entries (or an empty/malformed Portfolio) → unchanged single-glyph fallback (`formatGlyph(entry.format)`), byte-for-byte the old rendering.
  - Portfolio entries → a small STACKED badge cluster: one 18dp circular badge per section's format glyph (`formatGlyph(section.format)`), overlapping like an avatar stack via `Arrangement.spacedBy((-6).dp)` (later circles draw on top), each separated by a 1dp `surface`-colored border. Capped at 3 badges; extra takes roll up into a "+N" overflow chip. The card's `bodyPreview` already lists every take's short name ("2 takes · Voice + Review").
- New imports: `BorderStroke`, `size`, `CircleShape`, `FontWeight`, `CaptureData`.

## Validation
- code-reviewer-deepseek-flash: clean — imports complete, negative-spacing stack renders correctly, non-portfolio behavior preserved via the `sections.isEmpty()` fallback, badge cluster fits the 2-col grid (~60dp vs ~134dp available). Only cosmetic nits (duplicated circle structure for "+N"; 1dp tonalElevation makes the pure-`surface` border read a hair off — imperceptible).
- git diff scoped to 1 file (75 insertions, 5 deletions); confirmed `CurioEntryCard` has no other users.
- No gradle build per AGENTS.md (CI owns compilation on push).

## Status
DONE — committed & pushed (feat: cabinet card stacked format badges for portfolios).
