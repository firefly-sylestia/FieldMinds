# Prompt.md — Request Log

## Latest Request (IN PROGRESS)

**Redesign the Material palette card gradients — 2 colors only**

### Requested

Redesign the Material color palette and the gradients: card gradients
should use only TWO colors — ~90% the device's Material You gradient color
(the less-pastel M3 color) with 10% (or 5%) category color as a sprinkle.

### Analysis

The Material branch of `CurioGradients.cardGradient()` previously built a
THREE-stop gradient mixing `scheme.primary/secondary/tertiary` through a
6-band arrangement wheel, with 12–20% (light) / 42–52% (dark) category
tint pulled into every stop — a loud multi-color blend where the device
palette barely read. The user wants the device palette to dominate.

### Plan

1. `CurioColors.cardGradient()` Material branch → clean TWO-color gradient:
   - Anchor: the device M3 PRIMARY (the least-pastel of the M3 trio;
     secondary/tertiary are the muted pastel ones). Pastel mode softens it
     via `pastelAccent`; otherwise the raw device color is floored for
     white ink (`floorForWhiteInk` — no-op in light where dynamic primary
     is already dark, pulls the pastel-pale dark-mode primary down, hue
     untouched).
   - Sprinkle: the category accent (deepened 8%, or pastel twin in pastel
     mode) lerped at 5% top → 10% bottom.
   - Dark non-pastel only: sprinkle raised to 10% → 18% so categories stay
     distinguishable on the floored muted device color (the old wheel had
     42-52% there; 5-10% would make every deck card read the same).
2. `SettingsScreen` — "Material card blends" copy: "Cards wear your
   device's palette with just a sprinkle of category color".
3. Removed the now-dead 6-band wheel + secondary/tertiary mixing (locals
   `p/s/t/hsl/tint/pCat/…` gone; `toHsl` still used elsewhere, so no
   unused-import issue).
4. Changelog + Prompt.md.

### Status

- Edits applied: CurioColors.kt (cardGradient Material branch + docs +
  floorForWhiteInk doc), SettingsScreen.kt (copy).
- Reviewer verified: compile-safe (`toHsl` still used by pastelAccent /
  lightAccentTint / hslBlend / hslCentroid; `.first()` callers like
  EntryDetailScreen work with 2 stops; contrast guard intact).
- Changelog + Prompt.md updated. Awaiting review + commit/push.
