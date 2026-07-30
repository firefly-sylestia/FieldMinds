# Home + Profile redesign — completion summary

## Task

User asked to redesign the Home screen with a better look and fully redesign the Profile page with a modern Material theme.

## What changed

### `app/src/main/java/com/curio/app/features/home/HomeScreen.kt` (rewritten, 926 lines)

Replaced the previous 5-stacked-section layout with a single, tighter scroll:

1. **Minimal top bar** — drawer menu (40dp) on the left + avatar pill (40dp) on the right. No big "Curio ✦" wordmark.
2. **Personalized greeting hero** — "Good afternoon, Alex" using `AppPreferences.getDisplayName`.
3. **Quest card** (168dp) — accent-tinted surface with watermark glyph, "TODAY'S QUEST" eyebrow text, headline ("$catName" or "Spin the wheel"), supporting line, and a single primary CTA. Switches color when a category is selected.
4. **4-pill stat strip** — Streak · Saved · Recent · Lanes, each a compact 18dp-corner pill in the matching family tint.
5. **Categories chip row** — horizontal scroll of pill-shaped chips (28dp corners) with leading glyph + label. "Surprise" wildcard pinned first. Selected chip pops scale (1.0 → 1.04) and switches to a filled state.
6. **Recently explored** — compact `RecentEntryRow` list with category-color swatch, name, "Category · Nd ago" subtitle, trailing chevron. Replaces the 160w slide-rail cards. Shows a `FirstTimeEmpty` card with dual CTA ("Surprise me" + "Pick a lane") when the cabinet is empty.
7. **Reminder nudge** — shown only when `AppPreferences.isReminderEnabled == false` — a soft ButterYellow card pointing to Settings.
8. **Drawer** — slimmed-down with a single hero header that greets the user by name.

Top-of-bar padding tightened (`statusBarsPadding() + vertical = 4dp`), and `WindowInsets.navigationBars.asPaddingValues()` reserves the last 24dp + nav-bar inset as bottom padding.

### `app/src/main/java/com/curio/app/features/profile/ProfileScreen.kt` (rewritten, 1048 lines)

A modern Material 3 redesign composed of stacked, independently-animated cards:

1. **HeroProfileCard** — coral-tinted surface, 80dp initial-letter avatar bubble, `headlineSmall` name, context-aware tagline (`taglineForStreak`), optional streak pill.
2. **StatsGrid** — 2×2 grid in a single surface: Streak · Captured · Recent · Lanes. Each cell is a 32dp-corner icon swatch + ExtraBold value + labelSmall caption.
3. **LevelCard** — 8-tier progression (1 → 500 captures) with a 48dp gradient circle containing the current level + dynamic title (`levelTitle(level)`: "First spark" → "Master explorer") + Material 3 `LinearProgressIndicator` showing fraction toward next badge.
4. **PreferencesCard** — single Material 3 Surface containing grouped rows: Display name · Theme (`SingleChoiceSegmentedButtonRow` Light/Dark/System inline) · Audio quality · Notifications (`Switch`) · Manage categories. Thin internal dividers between rows.
5. **CategoriesPreviewCard** — top 3 most-captured lanes as colored pill chips with count + "Manage" link + "Open the Cabinet" quick-action row. Computed once via `CurioRepositoryHolder.repo.getAll().groupingBy { it.topic.categoryId }.eachCount()`.
6. **RecentActivityCard** — up to 5 `RecentEntryInline` rows with category swatch + name + age.
7. **DevAboutCard** — single Material 3 Surface hosting Report a bug · Test crash · Crash logs (conditional on `crashCount > 0`) · Replay intro · Version. Crash count badge surfaces in the section header.

All cards use consistent 24dp corners, `MaterialTheme.colorScheme.surface` background, 1.dp shadow + 1.dp tonal elevation. `ThinDivider` keeps row separation muted (50% outlineVariant, indented 54.dp).

## Verification

- Per the root DOX (`AGENTS.md` → "❌ NEVER RUN COMPILE OR BUILD COMMANDS"), did not run Gradle compile/build/test/lint in this environment.
- Spawned `code-reviewer-minimax-m3` twice:
  - First pass flagged 1 compile blocker (`private val Int.sp` shadowing using `TextUnit(Float, TextUnitType)` internal constructor wasn't compileable) + 3 unused imports + 1 dead helper + 1 tedious math simplification.
  - All 5 issues fixed (replaced the unsafe extension with `import androidx.compose.ui.unit.sp`, removed `CurioHeroSpinCard` / `Brush` / `tween` / `BorderStroke` / `verticalGradientBrush`, simplified the LevelCard supporting text).
  - Final pass: **SHIP**.
- No emoji — all glyphs are Material Symbols on the existing `material_symbols_outlined.ttf`.
- Reused existing primitives only: `CurioIcon`, `CurioColors`, `CurioGradients`, `CurioMotion`, `MorphEntrance`, `StaggeredEntrance`, `StaggeredItem`, `CurioBackButton`, `LinearProgressIndicator`, `AlertDialog`/`OutlinedTextField`, `SingleChoiceSegmentedButtonRow`, `Switch`, `WindowInsets.navigationBars`.
- Did not modify the existing modal navigation drawer (kept as-is, only the header text changed).

## Out of scope (deliberate)

- Left the previous Spin-screen redesign untouched.
- Did not add unit/integration tests — per AGENTS.md the environment doesn't run Gradle. The level progression math (`levelFor` + `progressTowardsNextLevel` in `ProfileScreen.kt`) would be the natural target for a future test source set.
- Did not commit / push — leaving the diff uncommitted per environment convention; user can commit when ready.
- Profile drawer's `your lanes` count is computed at first load only, not as a `Flow.collect`. Acceptable for the placeholder phase; would benefit from a reactive `observeAll().map { it.groupingBy { ... } }` once Flow plumbing is wired.
