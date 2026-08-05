# Prompt.md — Request Log

## Latest Request (IN PROGRESS)

**Home + detail tear shadows, hero name height, detail pop-up pill ripple glitch**

### Requested

1. Home hero tear AND detail hero tear: add a thin (~0.1 mm) dark shadow
   right at the torn edge so the tear looks more realistic.
2. Home hero: increase the name height where it says "Curious Explorer"
   (the display name under the greeting) — there was a dead space below it;
   scale it up to fill most of the space. User picked "Fill most of the
   space" (~30sp name, gap shrinks).
3. Detail screen: the popped-up back/more icons had a weird circular
   ripple glitch behind them — fix by reusing the Home sticky-pill logic
   (clickable with `indication = null`).

### Plan

1. `HomeScreen.kt` — hero tear: new `Box` between the white under-sheet
   and the banner, `height(HomeQuestHeroHeight).offset(y = 1.dp)
   .clip(heroTornShape).background(Color.Black.copy(alpha = 0.20f))` — a
   hairline dark rim hugging the seeded torn seam (hidden behind the
   opaque banner; reads as the paper edge casting a thin shadow onto the
   sheet; in the up-bites the rim hugs the bite bottom while white still
   reads above).
2. `EntryDetailScreen.kt` — identical shadow `Box` (`height
   (EntryDetailHeroHeight)`, same offset/clip/color) between the sheet
   and the hero backdrop.
3. `HomeScreen.kt` — hero name: `titleMedium` (16sp) →
   `headlineMedium.copy(fontWeight = Medium, fontSize = 30.sp,
   lineHeight = 46.sp)`; the tall leading makes the name block itself
   fill the dead space above the stat bar (weight spacer shrinks).
4. `EntryDetailScreen.kt` — more button: `Surface(onClick = …)` →
   plain `Surface` + `clickable(interactionSource, indication = null)`
   (mirrors Home's `TopBarPill`); back button: pass the new
   `disableRipple = true` to `CurioBackButton`. Added
   `foundation.clickable` + `foundation.interaction.MutableInteractionSource`
   imports.
5. `CurioTopBar.kt` — `CurioBackButton` gains `disableRipple: Boolean =
   false`; when true it uses the rippleless clickable pattern; all other
   screens keep the standard ripple.
6. Changelog + Prompt.md.

### Outcome

- Changes applied to all four files; markers verified via code search.
- Awaiting code review + commit/push.
