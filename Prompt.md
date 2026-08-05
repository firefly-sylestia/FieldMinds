# Prompt.md — Request Log

## Latest Request (COMPLETED)

**Detail view: add a quick fact, fix spacing, fix font colors + hierarchy across all modes**

### Requested

- Add a "quick fact" to the detail view (the saved-entry detail page).
- Fix spacing.
- Fix font colors and hierarchy in the detail view in every color mode
  (default Curio, Material blend, pastel) and every theme (light/dark).

### Analysis

- The "detail view" is `EntryDetailScreen` (the reveal screen already shows
  the topic teaser as its "One quirky fact to get you curious" card, so a
  quick fact there would be redundant). The detail page showed the entry's
  own capture but NEVER the topic teaser.
- **Font-color bug:** the hero title was hardcoded `Color.White`. On the
  pastel-light hero fill (an airy ~0.80-lightness pastel via
  `categoryCardFill`), a white title washed out — every pastel mode. The
  glyph/scatter already used the theme-aware `heroInk` (`cat.onAccent()`).
- **Spacing:** the topic-meta column padded 28dp while the format body
  padded 20dp horizontal — a ragged left edge under the hero; vertical
  rhythm was tight (8dp) and the body sat 8dp from the meta.
- **Hierarchy:** captured-at line used full `onSurface` — same weight as
  the category chips.

### Plan

1. New `QuickFactCard` composable (mirrors the reveal `TeaserCard`:
   category surface + border, sparkles glyph, "Quick fact" heading, topic
   teaser body) placed at the end of the topic-meta column, above the
   format body.
2. Hero title: `Color.White` → `heroInk` (deep accent in pastel light,
   light twin in pastel dark, white otherwise — matches the existing
   `blendActive` contrast guard).
3. Spacing: meta column → start/end 20dp (aligned with the body gutter),
   top 28dp preserved (white under-sheet lip clearance), bottom 16dp,
   spacedBy 12dp; format-body wrapper vertical padding 8→16dp.
4. Hierarchy: captured-at line muted to `onSurfaceVariant`.
5. Inline v7.35 comments; Prompt.md; commit + push.

### Status

- All edits applied to `EntryDetailScreen.kt` (QuickFactCard + invocation,
  heroInk title, gutters/rhythm, captured-at ink).
- Code-reviewer-deepseek-flash verified compile-safety (all imports
  present, `categoryBorder()` signature matches the reveal's identical
  call) and the heroInk semantics across pastel light/dark + non-pastel.
  Suggested the captured-at `onSurfaceVariant` mute (applied). Flagged the
  TeaserCard duplication as optional (per-screen private components are
  the established pattern — left as-is).
- Committed and pushed.
