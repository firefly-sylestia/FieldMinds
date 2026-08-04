# Prompt.md — Request Log

## Latest Request (COMPLETED)

**Further detail hero tear refinement**

### What was requested

Improve the detail hero's seeded wavy tear so it never reads as a straight line, has more than two or three broad waves, and gives the white under-layer a slightly bumpy handmade edge instead of a rigid plain line. Preserve the unique, deterministic look per saved entry and the existing small white reveal below the hero.

### What was done

- Increased the seeded soft tear personality from 2.5–4.5 broad undulations to 6–9 across the full hero width.
- Added a continuous signed wave field alongside seeded value noise, preventing long visually flat plateaus while keeping the edge irregular rather than mechanically repetitive.
- Retained seeded tilt, depth variation, and fine fiber texture so each entry remains unique and stable across recompositions/reopens.
- Added a restrained, independently seeded bump layer to the bottom edge of the white under-sheet. The sheet remains a small lip behind the hero, but its lower silhouette is no longer a rigid straight line.
- Updated the tear documentation to match the new geometry.

### Validation

- `scripts/check_braces.py` passed for `PaperCard.kt` and `EntryDetailScreen.kt`.
- `git diff --check` passed.
- Code review found no compile/runtime blocker; deterministic alignment and the hero-only tear layering remain intact.
- Local Gradle commands were not run because the repository's AGENTS.md explicitly forbids them; CI remains the compilation gate.

---

## Previous Request (COMPLETED)

**Fix Kotlin compile failure from FieldMind importer KDoc**

- Removed dangerous `/*` wildcard path notation from importer KDoc.
- Pushed as commit `95eb42cf`.

---
