# Prompt.md — Request Log

## Current Request: Home hero card doesn't match cream background

**User request (verbatim):** "the home hero card doesnt matc te roer color fix it"

## Root cause
The Home hero card (`CurioHeroCard.kt`) uses `CurioGradients.cardGradient()`, whose light-mode gradient end was still pure `Color.White` — after the previous change made the light background SoftCream (`#F7F0E4`), the hero card (and every other card-gradient consumer: category cards, TopicReveal, EntryDetail, Profile, quest cards) faded toward white and clashed with the cream surface.

## Fix
`CurioColors.kt` — `CurioGradients.cardGradient()` light-mode end changed `Color.White` → `CurioColors.SoftCream`, so all card gradients now wash into the cream background. Dark mode end (`Color.Black`) untouched. Doc comment updated.

## Validation
- code-reviewer-deepseek-flash: clean — `CurioColors.SoftCream` resolves from sibling `CurioGradients` object, `cardGradient` already @Composable, dark mode untouched. Optional note (not taken): dark gradient fades to pure black vs midnight `#0B1018` — pre-existing, out of scope.
- No local gradle per AGENTS.md — CI on push is the compile gate.

## Status
DONE — committed & pushed.

---

## Previous Request: Light-mode cream background + Spin-page category tint

**User request (verbatim):** "make te ap ligt mode white color a less white color not dark color that creamy color but not tat black and like add the card category tint to it only in spin page"

## Clarified via ask_user
1. **Cream level:** Soft cream (`#F7F0E4`) — gentle warm off-white, barely not white, not dark/black.
2. **Spin tint scope:** Background wash — the whole Spin page background gets a subtle wash of the active category's tint behind the deck.

## Changes
- `ui/theme/CurioColors.kt` — added `SoftCream = Color(0xFFF7F0E4)`; reworded `CreamWhite` comment (now ink/decoration only, no longer a surface).
- `ui/theme/CurioTheme.kt` — light scheme `background`/`surface`/`surfaceContainerLowest` → `SoftCream`. Container steps deepened (Variant/Low/Container/High/Highest) so cards/sheets stay distinct on the cream surface. Dark scheme untouched.
- `features/spin/SpinScreen.kt` — root Box adds `.background(deckCat.tint)` after the theme background, so **only** the Spin page wears the category-tint wash (subtle 20% alpha over both cream light and midnight dark).
- `app/AGENTS.md` — recorded the durable user design preferences under UI section.

## Validation
- code-reviewer-deepseek-flash (2 passes): clean — modifier layering correct (background paints bottom, tint over it, content above), hierarchy coherent, no dark-mode drift, no broken references. Nitpick fixed (CreamWhite comment).
- grep: SoftCream used in light scheme; only SpinScreen has the tint wash (all other screens keep plain `colorScheme.background`).
- No local gradle per AGENTS.md — CI on push is the compile gate.

## Status
DONE — committed & pushed.
