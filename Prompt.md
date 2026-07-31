# Paper-style Home, Spin, and Topic Reveal redesign — completion summary

## Request

Replace the transparent glass-style visual treatment on Home, The Spin, and Topic Reveal with an opaque paper style featuring tactile depth, and remove the background gradients.

## Changes

- Removed Home's ambient radial background halo and replaced translucent hero/stat/chip/reminder surfaces with opaque paper containers.
- Reworked Home's hero card, stats, category chips, empty state, reminder card, drawer header, and controls with solid surfaces, crisp category edges, and restrained elevation.
- Removed Spin's ambient accent backdrop, gradient ticket brush, landing glow, glassy sheen, radial orb rendering, and animated idle halo.
- Rebuilt Spin's hero ticket as an opaque paper sheet with a category-color side rule, strong edge, layered elevation, ink-style watermark, solid peek cards, and an opaque bottom tray.
- Updated Spin top-bar/category controls, topic-count pill, filter controls, empty state, and picker surfaces to use paper containers instead of translucent accent washes.
- Reworked Topic Reveal's hero, teaser, action prompt, and tag surfaces to use opaque paper cards with category-color edges and readable ink hierarchy.
- Preserved all existing navigation, spinning, filtering, auto-open, confetti, and capture behavior.
- Updated `CURIO_SPEC.md` with the durable paper-surface rule and updated the store changelog.

## Verification

- `scripts/check_braces.py` reports HomeScreen, SpinScreen, and TopicRevealScreen as `BALANCED`.
- `git diff --check` passes.
- Targeted source scans confirm no runtime `Brush`, `drawBehind`, `Color.Transparent`, removed glass-theme imports, animated idle halo, or the reviewed translucent paper fills remain in the three requested screens.
- `code-reviewer-luna` completed a final review with no actionable blockers.
- No Gradle compile/build/test/lint task was run because the repository's AGENTS.md explicitly forbids local Gradle validation; CI remains the compilation source of truth.

## Closeout

- Final Home category-chip glyph tiles were changed to opaque paper surfaces while retaining accent borders and icon colors.
- Changes are ready to commit and push on branch `revamp`.

---

# CI lint repair — completion summary

## Failure

The CI debug build compiled successfully but failed `lintDebug` on `SettingsScreen.kt` because `Locale.getDefault()` was read inside a composable (`NonObservableLocale`). CI also reported an unnecessary Home non-null assertion and an always-true Topic Reveal condition.

## Fixes

- Replaced `Locale.getDefault()` with observable `LocalLocale.current.platformLocale` in SettingsScreen date formatting.
- Removed the unused `java.util.Locale` import.
- Replaced Home's redundant `chosen!!` accent access with a nullable-safe fallback.
- Simplified Topic Reveal's action badge condition from `action != null && resolved != null` to `action != null`.

## Verification

- `scripts/check_braces.py` passes for SettingsScreen, HomeScreen, and TopicRevealScreen.
- `git diff --check` passes.
- Targeted static assertions confirm the old locale call, redundant assertion, and always-true condition are gone.
- Code review reports no actionable blockers.
- Local Gradle commands were not run because repository instructions forbid local Android builds; CI remains the source of truth.
