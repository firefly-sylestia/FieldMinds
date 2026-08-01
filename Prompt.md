# Mixed-Category Deck — Premium Blended Colors

## Request

User (design direction): "give the mixed category cards its unique color based on the colors user selected so it should give the user the mixed color look of the spin screen. but dont make it ugly do a research and add all the combinations shades and premium colors"

## Research

- Spawned researcher-web: HSL blending best practices — shortest hue path, saturation boosting to avoid muddy midpoints, known-muddy pairs (teal↔amber, sky↔amber cross the olive-green dead zone), duotone gradient guidance (Spotify/Duolingo/Headspace style).
- Computed HSL midpoints for all 15 pairs of the 6 category accents via a throwaway python script (scripts/compute_blends.py, removed after use).

## Analysis

- Multi-select launches (`spin/artists,albums`) merged topic pools, but all deck chrome (hero ticket, peek cards, spin button, confetti) still used the FIRST category's accent — no visible mixing.
- `CurioGradients.cardGradient(accent)` is the single source of card gradients; `categoryCardFill` deepens an accent 10% toward black.

## Plan

1. **`CurioColors.kt`** — new `CurioMixedDeck` object:
   - `PairBlends`: 15 hand-curated premium blends (HSL midpoints along shortest hue path + saturation boost; amber↔teal → jade #158A5C, amber↔sky → teal #0C8B8A to steer off the olive dead zone; coral pairs deepened for white-text contrast).
   - `mixedDeckAccent(accents)`: dedupe by ARGB → 0→coral, 1→itself, 2→curated (fallback `hslBlend`), 3+→sequential `hslBlend` reduce.
   - `@Composable mixedDeckGradient(accents)`: single accent → standard `cardGradient`; multi → `categoryCardFill` of first 3 distinct accents (multi-stop Spotify-style gradient, capped to avoid rainbow).
   - `private hslBlend(a,b)`: shortest hue path via `Color.hue/saturation/lightness` + `Color.hsl`.
2. **`SpinScreen.kt`** — derive `deckAccents` from `activeCatIds`; `deckAccent` (remember) + `deckGradient` (computed in composition, theme-aware); thread through Carousel → HeroTicketCard (new `gradient` param) + PeekCard (new `accent` param, `remember(accent, far)`), SpinButton `tint`, ConfettiBurst `colors`. Removed now-unused `CurioGradients` import.

## Completion Summary

- Validation green: braces (SpinScreen 268/268, CurioColors 15/15), zero `CurioGradients` refs left in SpinScreen, all HeroTicketCard/PeekCard call sites updated (both private, single caller), helper script removed.
- Code review clean (2 passes): `Color.hue/saturation/lightness` + `Color.hsl` exist in Compose 1.11.2; `Set<Color>` value-class map keys reliable; `@Composable` layering correct; `remember(accent, far)` sound; single-category behavior unchanged. Reviewer's only flag (unused `CurioGradients` import) fixed.
- Behavioral note (intended): multi-accent gradients skip the theme-surface fade, so mixed decks render full-bleed saturated stops — the signature "mixed look".
- Gradle build/lint NOT run (forbidden in this env; CI validates on push).
