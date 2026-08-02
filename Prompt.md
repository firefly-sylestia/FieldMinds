# Current Request

## Status: COMPLETED — committed and pushed to `revamp`

"restore the backgroud tint iin cabinet categories excpet the all one and dont
make the button adat just the backgroud"

## Root cause / context

Commit f1ec0d19 ("cabinet filter chips get neutral inactive colors") had
removed the tinted idle backgrounds because the old code used the SELECTED
filter's `filterCat.categorySurface(...)` — tapping a category re-tinted ALL
chips with that color. The fix isn't to keep chips flat neutral; it's to give
each chip its OWN category tint so no tap re-tints its neighbors.

## Change (`app/src/main/java/com/curio/app/features/cabinet/CabinetScreen.kt`)

- Category chips now pass
  `chipSurface = cat.categorySurface(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))`
  — each chip wears its own category's tinted surface when idle (per-`cat`,
  not the old `filterCat`), and still pops to the full `cat.tint` when
  selected (FilterChipLite `color = if (selected) tint else chipSurface`).
- `ink` pinned to `MaterialTheme.colorScheme.onSurfaceVariant` for category
  chips so the label text NEVER adapts to the category color — only the
  background carries the tint (FilterChipLite `if (selected) ink else
  onSurfaceVariant`).
- The "All" chip is untouched (neutral surfaceVariant idle + primaryContainer
  selected) — "except the all one".
- Filter-chip-row comment rewritten to describe the new behavior.

`categorySurface` honors the Settings tint toggle: when off, chips fall back
to the flat neutral surface (pre-change look), consistent with the rest of
the app.

## Review
- code-reviewer-deepseek-flash: clean. Two minor non-blocking notes:
  pre-existing unused `accent` param in FilterChipLite; idle-vs-selected is
  a hue-vs-grey-blend + border-removal cue (matches intended pre-f1ec0d19
  structure).
- Per AGENTS.md no local Gradle build — CI validates compilation on push.
