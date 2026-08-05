# Prompt.md — Request Log

## Latest Request (COMPLETED)

**Floating explore bubble still doesn't appear at all**

### What was requested

"The floating pill still doesn't appear at all." — the recurring bubble
issue, after earlier fixes (per-start overlay retry, notification-gate
removal, reactive-store seed, Android 16 owner/FrameLayout hardening).

### Audit (what was verified correct)

- Manifest: `SYSTEM_ALERT_WINDOW`, `FOREGROUND_SERVICE`,
  `FOREGROUND_SERVICE_SPECIAL_USE` all present.
- Start flow (`TopicRevealScreen.beginExploreSession`): the service starts
  via `exploreServiceShouldRun` BEFORE the browser opens (activity still
  foreground — no Android 12+ background-FGS throw); permission paths defer
  their start to foreground callbacks.
- Gates: bubble toggle defaults ON (survives data clear); `pillHidden`
  resets with each fresh session; the deferred composition post runs after
  window attach on the main looper (HandlerActionQueue flush), so the
  `isAttachedToWindow` guard is sound.

### Root cause of the remaining silent-invisible failures

Two holes in `ExploreSessionService.showBubble()`:
1. A window that attached but never composed is zero-size and INVISIBLE
   (the posted composition could bail or render nothing) — no error, no
   retry, `bubbleView != null` blocks re-adds: the user sees nothing.
2. ANY single transient attach/composition failure set `bubbleUnavailable`
   permanently for the session (cleared only on the next explicit start).

### What changed (`ExploreSessionService.kt`)

- `bubbleRetryCount` (cap `MAX_BUBBLE_RETRIES = 2` per explicit start;
  reset on EXTRA_SESSION starts and onDestroy).
- `scheduleBubbleRetry()` — after an addView OR composition failure, a
  1.2s delayed retry clears the latch and re-attempts `showBubble()` once
  (count-capped → a persistent device rejection can't restart-loop).
- `verifyBubbleVisibleOnce()` — 2s after a successful addView, checks the
  attached window actually has size > 0; if still empty it logs, tears down
  and rebuilds once (count-capped). Heals the silent empty-window case.
- `doOnLayout` positioning wrapped in `runCatching` (a throw there would
  have crashed); `handleOverlayFailure`'s whole cleanup wrapped so a
  teardown throw can't escape the posted onFailure and skip the retry.
- Android 16 owner/FrameLayout/deferred-composition hardening untouched.

### Notes

- Self-heal only covers POST-start failures. If the user still sees no
  bubble, the next diagnostic is logcat for "Failed to start explore
  service" — a background FGS-start throw (e.g. the ExploreBootReceiver
  BOOT_COMPLETED path) is a separate failure class.

### Validation

- `scripts/check_braces.py` passed; `git diff --check` clean; audit of the
  new symbols/constants clean.
- Reviewer approved; its two catches applied (cleanup runCatching + verify
  delay 1.5s → 2s).
- Gradle/build commands were not run because the repository forbids local
  Android compilation; CI remains the compilation gate.
- Store changelog `20260810.txt` updated.

## Previous Request (COMPLETED)

**Detail hero title white + gradient pill with the background color**

### What was requested

"Make the detail screen title color white and make that card a little
gradient style with the background color."

### What changed (`EntryDetailScreen.kt`)

- **Title color** → `Color.White` always. Previously the title used
  `heroInk` (`cat.onAccent()`), which is white in non-pastel but the
  ACCENT ink in pastel light mode — and pastel is the shipped default, so
  most users saw a colored title. Now it's the classic white hero title in
  every theme.
- **Title pill** → from a flat translucent tint to a soft glass band: the
  `Surface` is now transparent with a hairline rim (`heroInk` 30%) and
  wraps a Box whose background is `Brush.verticalGradient` built from the
  BANNER's own color — top `lerp(heroStart, Black, 0.10f)@0.30` (soft
  tinted frost) → bottom `lerp(heroStart, Black, 0.30f)@0.60` (deepened
  color band) — with the rounded shape applied to the background itself
  (Surface doesn't clip content). Both stops carry the banner color so a
  two-line title's first line sits on the tinted band, not bare banner
  (reviewer's pastel-light readability note — the first draft's `heroInk`
  16% top stop was nearly invisible on airy pastel banners).
- Added the missing `androidx.compose.ui.graphics.lerp` import.

### Validation

- `scripts/check_braces.py` passed; `git diff --check` clean; import
  audit clean (`lerp` 2 = import + 1 use, `Brush` still used elsewhere,
  `heroInk` still used by the glyph + rim, `heroStart` in scope at the
  hero).
- Reviewer approved; its tuning note (weaker top stop on airy pastels)
  applied via the deepened both-stop gradient.
- Gradle/build commands were not run because the repository forbids local
  Android compilation; CI remains the compilation gate.
- Store changelog `20260810.txt` updated.

## Previous Request (COMPLETED)

**Cabinet watermark no longer shifts between filter pages**

### What was requested

"Fix the watermark shifting in cabinet and keep it same level so switching
page doesn't affect it."

### Root cause

`CurioWatermarkBackdrop` highlights the active category's glyph with a
stronger alpha "whisper", and each category glyph sits at a DIFFERENT fixed
bias position in the scatter. The Cabinet passed `activeCat = filterCat ?:
WILDCARD`, so switching filter pages (All → Artists → Legacy) made the
highlighted glyph jump across the screen — the perceived "shifting"
watermark. Glyph POSITIONS were always fixed; only the emphasis moved.

### What changed

`CabinetScreen.kt` — the backdrop now always passes
`activeCat = CurioCategories.byId(CategoryId.WILDCARD)` (the same constant
Home uses), so the watermark is fully static on every Cabinet page: same
glyphs, same positions, same emphasis, no matter which filter is open. The
active category is still carried by the page wash
(`categoryBackgroundWash`), the chip row tints and the card tints. Inline
comment updated to explain why the backdrop must not follow the filter.

### Notes

- Scoped to the Cabinet only — Home / Spin / Reveal / Detail keep their
  category echo. "All" page looks identical to before (it already showed
  the wildcard boost); category pages just lose the moving highlight.
- Reviewer approved; flagged the design tradeoff (the category echo on
  category pages is gone — a pinned fixed-position "spotlight" could
  restore it if the user misses it) and an alternative reading (tab-switch
  level mismatch caused by the Cabinet root Box's statusBarsPadding —
  intentional, to keep glyphs out from behind the status-bar icons).

### Validation

- `scripts/check_braces.py` passed for `CabinetScreen.kt`; `git diff
  --check` clean; `filterCat` still used 3× (wash, CurioNavTint handoff,
  empty states), `CategoryId` still used 6× — no dead references.
- Gradle/build commands were not run because the repository forbids local
  Android compilation; CI remains the compilation gate.
- Store changelog `20260810.txt` updated.

## Previous Request (COMPLETED)

**Home redesign — hero hierarchy, quest block below the tear, tinted recents**

### What was requested

1. Greeting one line, name below it.
2. Streak · Cabinet · Recent card much lower — just above the tear — with a blurry gradient in the card color (not much white).
3. Shuffle button bigger + solid color; clicking it picks a random category or a random mix; don't make the whole hero clickable.
4. Proper hierarchy for TODAY'S QUEST and the texts above it; remove the indicator bar before the eyebrow.
5. Move "Shuffle the deck" below the hero card, between the Recents section and the hero tear.
6. "View all" at the Recents arrow.
7. Topic rows get a solid background matching the category gradients.
8. Different glyph watermark on the hero.

### What changed (all `HomeScreen.kt`)

- **Hero** (`HomeQuestHeroHeight` 340 → 300dp): greeting is now one line (`greetingWordForNow()`) with `displayName` beneath it (both centered, maxLines 1); the Streak · Cabinet · Recent bar is pinned just above the tear by a weight spacer and now sits on a soft rose gradient pane — `Surface(color = Transparent, hairline rim)` wrapping a Box with `Brush.verticalGradient(heroFill@0.12 → lerp(heroFill, White, 0.26)@0.55)` *with the card's rounded shape applied to the background itself* (Surface doesn't clip content — reviewer catch, otherwise square corners bled past the rounded border). The banner Surface lost its `onClick` (no longer tappable). `heroFill` now resolves through the shared `homeRoseAccent()`. Watermark glyph swapped Casino → AutoAwesome.
- **Quest block below the hero**: new `QuestShuffleCard` — "TODAY'S QUEST" eyebrow (no indicator bar), "Shuffle the deck" title, and a 56dp solid rose button "Shuffle a random deck" (ink = `pastelFillInk`). `onShuffle` picks `Random.nextBoolean()` → a mix (2–3 random categories) or a single random category from `CurioCategories.all`, persists `setLastSpinCategories`, then `navigateToTab(SPIN)` — the plain Shuffle tab is authoritative from prefs (SpinScreen's `LaunchedEffect(categorySlug)` applies `getLastSpinCategories` on entry, the same pattern the category picker uses).
- **Recents**: header arrow replaced with a "View all" pill (label + chevron → Cabinet); `ExploreTopicRow` and `RecentEntryRow` are now solid category-tinted cards (`Surface(onClick, color = category.categorySurface())`, 20dp radius) instead of backgroundless rows.

### Validation

- `scripts/check_braces.py` passed; `git diff --check` clean; import audit clean (`pastelAccent`/`isCurioDarkTheme` still used by `homeRoseAccent`, `clip`/`clickable`/`Casino`/`CurioForwardArrow` still used elsewhere, `greetingForNow` gone).
- Reviewer approved; its one real catch (gradient corners needing the rounded shape on the background) applied; placement judgment call (quest block right after the hero, most prominent — on typical screens it sits directly between the tear and Recents) kept.
- Gradle/build commands were not run because the repository forbids local Android compilation; CI remains the compilation gate.
- Store changelog `20260810.txt` updated.

## Previous Request (COMPLETED)

**Detail title: transparent glass pane (drop glass-text glyphs)**

### Changes (EntryDetailScreen.kt)

- **Problem**: the carved glass-text title (TextStyle brush gradient + Shadow on the glyphs) "isn't working" — the letterform treatment didn't render as hoped. User asked to go back to the previous background-card style but TRANSPARENT, not blurry.
- **Title**: now a `Surface(RoundedCornerShape(20.dp), color = heroInk.copy(alpha = 0.18f), border = BorderStroke(1.dp, heroInk.copy(alpha = 0.26f)))` wrapping plain ExtraBold geom Text in `heroInk` with `padding(horizontal = 20.dp, vertical = 10.dp)` — the transparent tint pill language (same as Home hero's top-bar controls), NO blur, banner color shows through. 18% (not 14%) so it still reads as a card on airy pastel banners.
- Removed now-unused `androidx.compose.ui.graphics.Shadow` import (Brush/Offset still used).
- Fixed two stale comments referencing the title as a heroFrostPlate user.

### Completed: detail title pill → bright clean blue

User: "the title background looks darker, make it brighter instead of the dark vibe, and make it clean blue."

- `titlePaneGradient` now a vertical gradient `#7CA6EF → #3B82F6` (~0.96 alpha) — an ice-to-vivid clean blue band, category-agnostic, white rim 0.45, title stays `Color.White`.
- Removed the now-unused `androidx.compose.ui.graphics.lerp` import; stale comment above the pill updated.
- Reviewer: white-on-blue contrast strong in all themes; its top-stop note applied (deepened `#9DC0F7 → #7CA6EF` so a two-line title's first line doesn't wash out).

### Completed: Home polish pass (greeting, quest block, currently exploring, stat card)

User asks: right-align greeting + name; darker TODAY'S QUEST; shuffle button on the same level as the title; redesign Currently exploring to match; stat-card icons visible (same hero color, darker) + glyph pattern on the hero background.

- Greeting + name: `textAlign` Center → End (hug the right edge, banner Column still centers the stat card).
- QuestShuffleCard: eyebrow wears the deep `ink` twin instead of the airy pastel accent; the full-width 56dp button became a compact inline 52dp solid button (icon + "Shuffle") in a Row on the SAME level as the "Shuffle the deck" title (title Column weight(1f) + ellipsis).
- CurrentlyExploringCard: neutral surfaceContainerLow → solid `cat.categorySurface()` (matches recents rows), wrapped in Box with a faint category glyph watermark (96dp, accent 10%, CenterEnd) and the header restyled to the quest-eyebrow treatment (labelSmall ExtraBold + 1.4.sp letterSpacing).
- Stat card: Streak · Cabinet · Recent icons now wear `questInk` (the deep hero ink) instead of pastel FireOrange/Sage/Lilac; added a sparkle (AutoAwesome) watermark inside the gradient pane. FireOrange is now unused in HomeScreen (only Sage/Lilac remain for empty-state icons).

### Completed: peek cards wear the deck's real mixed gradient + family-kept pastels

User: "make the peek cards gets the mixed card gradients much better and also the pastel colors much better they look odd."

- Root cause 1: PeekCard received only the single blended `deckAccent` and rebuilt `cardGradient(accent)` — mixed decks' peeks flattened to one hue while the hero ticket wore the true multi-accent `deckGradient` sweep.
- Root cause 2: pastel peek level-crush black-lerped (near 0.13 / far 0.22 light), greying the airy pastels into muddy mids.
- Fix: Carousel now passes `gradient = deckGradient` into PeekCard (new `gradient: List<Color>` param); `blendStops = gradient`. Pastel mode steps depth by dropping HSL LIGHTNESS per stop (fromHsl(h, s, l - drop): near 0.06 light / 0.09 dark, far 0.10 light / 0.14 dark) holding hue + saturation; non-pastel keeps the classic black-lerp 0.28/0.42.
- Added imports `fromHsl` / `toHsl` (internal in CurioColors.kt, same module). Only one PeekCard call site (Carousel).

### Validation

- `scripts/check_braces.py` passed; `git diff --check` clean.
- Reviewer approved after one fix: stale v7.8.1 comment block still described the old black-lerp pastel levels — trimmed.
- Gradle/build commands were not run because the repository forbids local Android compilation; CI remains the compilation gate.
