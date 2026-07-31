# Solid Gradient Card Redesign

## Request

Redesign the entire app with a "material solid gradient card style" — fully opaque category-specific gradients on all cards, no borders/outlines, no transparency, no glowing/shimmer effects. Shadow elevation only for depth. Category chip icons directly on background, no inner containers.

## Design rules

- **100% opaque** — no `.copy(alpha = ...)` on card surfaces
- **Solid gradients** — `CurioGradients.cardGradient(accent)` and `wildcardCardGradient()` produce 3-6 stop fully opaque gradients
- **No borders** — removed from quest cards, spin tickets, hero cards, category tiles
- **No glowing** — removed shimmer overlay, breathing animation from hero card
- **Shadow elevation** — `shadowElevation = 10.dp` for soft blurry depth on cards
- **Category chips** — icons rendered directly in chip Row, no inner container Surface

## Completed implementation

- `CurioColors.kt` — Replaced alpha tints with opaque `lerp()` variants; added `cardGradient()` and `wildcardCardGradient()` helpers; removed unused `ticketStops()` and `wildcardTicketStops()`.
- `CurioHeroCard.kt` — Solid vertical gradient background; removed shimmer and breathing animations; all text now white.
- `HomeScreen.kt` — Quest card uses solid gradient instead of paper+border+tint; category chips lost inner icon containers, gradient on selected state.
- `SpinScreen.kt` — HeroTicketCard uses solid vertical gradient, removed border, side rule, and tonalElevation orphan; all text white on gradient; added `Brush` and `CurioGradients` imports.
- `TopicRevealScreen.kt` — HeroCard uses solid gradient with white text and white pill badges (verb/duration, subtype).
- `CategoryPickerScreen.kt` — All category tiles use solid gradients (category-specific or wildcard spectrum), no flat/accent colors.

## Validation

- Python brace check passes cleanly.
- Code reviewer identified and fixed: CategoryChip gradient Box sizing bug, tonalElevation orphan on transparent Surface.
- Gradle compilation/build/lint/test are not run locally because repository instructions forbid Android build commands; CI remains the compiler check.

## Remaining

- ProfileScreen still has some alpha-background sections (subtle supporting elements, not primary cards).
- `rememberShimmerBrush` in CurioAnimations.kt is dead code (only defined, never called).
- SpinScreen's SpinButton still uses paper/border style — could be updated to gradient in a follow-up.
