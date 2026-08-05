# Prompt.md — Request Log

## Latest Request (COMPLETED)

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

### Validation

- `scripts/check_braces.py` passed; `git diff --check` clean; Shadow verified 0 uses; Brush 4×, Offset 2× still used.
- Reviewer confirmed readability in all theme modes; its tint bump + comment fixes applied.
- Gradle/build commands were not run because the repository forbids local Android compilation; CI remains the compilation gate.
