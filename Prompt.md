# Home screen dark-mode contrast pass (v5.8) — completion summary

## Task

User asked to polish the Home screen with the same dark-mode contrast pass
(gradients, hairlines, glow) applied to the Spin screen, so the whole app
feels cohesive.

## What changed

### `app/src/main/java/com/curio/app/features/home/HomeScreen.kt`
- **Ambient accent halo** — content wrapped in a `Box` with a radial
  `drawBehind` wash in the active category accent (CoralBlush for wildcard),
  alpha 0.18 dark / 0.08 light, so Home breathes instead of a flat slab.
- **Quest card** — transparent `Surface` with an accent gradient wash child
  (0.26/0.06 dark, 0.16/0.03 light), accent hairline border, soft 2dp shadow.
- **Stat pills** — transparent Surface + vertical gradient wash + tinted
  hairline border (0.35 dark / 0.22 light).
- **Category chips** — transparent Surface; selected chips get a hue-preserving
  vertical gradient pill (white-lerp crown → accent → black-lerp base) with
  stronger hairline; unselected chips get a tint gradient + hairline.
- **Recent entry rows + empty state** — hairline `outlineVariant` borders so
  cards stay defined on the plum background.
- **Reminder nudge** — dark-mode contrast fix: plum text was unreadable on the
  dark plum bg, so text/icon flip to cream in dark mode; card now has a yellow
  gradient wash + hairline.
- **Drawer header** — stronger dark-mode gradient (0.24/0.07), avatar box gets
  hairline + stronger dark tint, and a gradient accent hairline divider under
  the header.
- All dark-mode branches now use the theme-aware `isCurioDarkTheme()` helper.

## Verification

- No Gradle builds run per project `AGENTS.md` rules (CI handles compilation).
- `scripts/check_braces.py` (new helper) reports all five touched files
  BALANCED, including the HomeScreen Box/Column wrapper.
- Grep checks: FQN `androidx.compose.ui.graphics.Brush` fully replaced by the
  import; `Offset` still used by the halo; `lerp`, `drawBehind`,
  `isCurioDarkTheme` imports all consumed; no dangling references.
- Code-reviewer-deepseek-flash reviewed twice: first pass caught a missing
  closing brace in the CategoryChip Box wrapper (fixed), second pass confirms
  correct nesting, no composable calls inside drawBehind lambdas, transparent
  Surfaces keep ripple + shadow. Two optional non-blocking nits noted
  (inert tonalElevation on transparent surfaces; chip crown lightening).

## Commit + push (per user request)

- **Deferred:** the proposed Spin text-animation pass (StaggeredTextReveal,
  hint morphs, CTA label morphs) — user asked to hold off and commit/push the
  completed work instead.
- Committed and pushed the Spin v5.8 gradient redesign + Home contrast pass
  (see commit message for full scope).
