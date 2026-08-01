# Prompt.md — Running Request Log

## Latest Request — "Mixed gradients look bad" + "Picking a category from Home explore opens a totally different page / Home tab shows that page"

### Status: ✅ Complete (about to commit & push)

### Bug 1 — Mixed gradients look bad
`CurioMixedDeck.mixedDeckGradient` returned the RAW deep category accents as
gradient stops (`[fill(A), fill(B), fill(C)]`). `Brush.verticalGradient`
interpolates them in RGB, which bands through muddy gray/brown — exactly the
failure the codebase's own docs warn about (teal↔amber / sky↔amber cross the
olive dead zone).

**Fix (CurioColors.kt):** `mixedDeckGradient` now builds a smooth HSL sweep —
for each consecutive accent pair it injects `hslLerp(prev, mid, 0.5f)`, the
curated pair blend (`mixedDeckAccent`), and `hslLerp(mid, accent, 0.5f)`
before the next accent (capped to the first 3 accents so a 5-way mix can't
rainbow). Added a private `hslLerp(a, b, t)` reusing the existing
`toHsl`/`fromHsl`/`Hsl` machinery (shortest-hue-path, sat/light lerp,
coerced). 2 accents → 5 stops, 3 accents → 9 stops; `@Composable` retained
for the single-accent `cardGradient` path. All stops stay deep (AA vs white).

### Bug 2 — Wrong page on category pick (navigation)
`SpinScreen` seeds `activeCatIds` from `rememberSaveable`; `navigateToTab`'s
`restoreState = true` resurrects a STALE session for the same route pattern
(e.g. an in-screen category switch made inside an earlier `spin/artists`
visit), so picking "Artists" reopened the deck with Albums' pool — "a totally
different page" while Shuffle stayed highlighted.

**Fixes:**
- **SpinScreen.kt (root cause):** a slug launch is now authoritative — new
  `slugCatIds` (remember(categorySlug) → byRouteSlug → ids) + a
  `LaunchedEffect(categorySlug)` that forces `activeCatIds = slugCatIds`
  whenever a slug is present (guarded with `!=` so fresh launches skip the
  redundant write). In-screen category switches (picker sheet) still win
  (effect keys only on the slug); the generic Spin tab (slug = null) keeps
  its saved state; landed-topic-survives feature and reveal auto-open guard
  unaffected.
- **CategoryPickerScreen.kt (clean stacks):** both spin navigations
  (tap-to-open single select and multi-select Done) now add
  `popUpTo(CurioRoutes.HOME)` so the PICKER (and any spin below it) is
  dropped — back from a fresh deck returns Home, and tabs can never
  resurrect a stale picker under the new deck. Works from both the
  Home-opened and Spin-"Browse all" entry points.

### Validation
- Code review (deepseek-flash, 2 passes — full change + final delta): clean.
  Confirmed `hslLerp` type-checks and is dead-code-free; `mixedDeckGradient`
  never returns empty for ≥2 distinct accents; `popUpTo(HOME)` default
  inclusive=false pops above HOME only; `slugCatIds` smart-cast + List
  equality compile; LaunchedEffect state writes are post-composition (safe);
  imports (CategoryId / CurioCategories / LaunchedEffect / CurioRoutes) all
  resolve. Non-blocking note: one-frame flash of the stale category on
  restoreState resurrection before the force applies — accepted tradeoff
  (a derived-value approach would break in-screen category switching).
- No gradle build run (per AGENTS.md, CI owns compilation).

### Prior work (this session)
- Edit-mood-board bug (wrong entry / empty board / save-blanking) — fixed,
  committed
- Mood board pinch-to-zoom + in-place zoom, edit button in expanded dialog,
  CI compile fixes — committed
- Mixed-deck identity + blends — committed earlier
