# Spin Screen: Deck Slide During Spin + Per-Tick Hero Bounce + Smoothness + Pixelated Borders

## Request

User: (1) when the spin starts the background cards don't have that slide animation — it only shows when it finishes — fix it; (2) the main card bounce should follow each switch so it looks good; (3) make the spin smoother; (4) still seeing pixelated borders on the spin screen.

## Analysis

All four issues live in `SpinScreen.kt`:

1. **Deck doesn't slide during spin**: `PeekCard`'s `AnimatedContent.transitionSpec` used a pure 70/60ms crossfade while `shuffling == true` (chosen to avoid jank at ~90ms ticks), and the 240ms `slideInVertically` only ran when `shuffling == false` — i.e. AFTER the spin finished. Exactly what the user observed.
2. **Hero bounce disconnected from the wheel**: the front card's bounce was a free-running 1.2s sine (`heroPulse`/`bounceWave`) unrelated to tick cadence, and its rotation came from a per-topic *hash* (`cycleIndexPulse`) that jumped randomly each tick — the "jittery/not smooth" feel.
3. **Smoothness**: hash-jitter rotation + tick floor (90ms) shorter than the slide it was meant to accompany.
4. **Pixelation**: rotated + scaled deck layers rasterized at default quality, aliasing the hairline `BorderStroke`s.

## Plan

- PeekCard: shuffling branch → short 80–90ms directional slide + fade so the deck visibly reels during the spin (not just after); raise tick floor 90 → 105ms so the 90ms slide completes even on the fastest ticks.
- HeroTicketCard: replace sine + hash with a per-tick pulse keyed on `topic?.id` (`snapTo(1.065)` → `animateTo(1f, spring(0.6, 1200))`), alternating `tickDir` driving `rotationZ = (pulse-1)*80*tickDir`; add a category-switch entrance bounce (`LaunchedEffect(cat.id)`, guarded `!shuffling && !landed`); landing settle snaps to `tickPulse.value` for a seamless handoff; idle scale tracks `tickPulse.value` (rests exactly 1f).
- Add `renderQuality = RenderQuality.High` to hero + peek `graphicsLayer`s (kills aliased borders); remove dead `cycleIndexPulse`.
- Static checks + code review; commit & push (CI validates compile on push).

## Completion Summary

- `SpinScreen.kt`: all four fixes as planned. Imports added: `androidx.compose.animation.core.spring`, `androidx.compose.ui.graphics.RenderQuality`.
- Code review (2 passes) clean; tuning applied per reviewer (snapTo-kick so even fast ticks visibly pulse; rotation factor 80). Braces balanced (258/258, 803/803); no dangling refs to removed symbols.
- Reviewer's non-blocking notes: fast phase may read as a hover rather than per-switch kicks (tuning judgment), and single-topic pools never change `topic?.id` so won't pulse (edge case, 100+ topics per category).
- Gradle build/lint NOT run (forbidden in this environment; CI validates on push).
