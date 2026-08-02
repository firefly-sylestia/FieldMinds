# Current Request

## Status: COMPLETED — committed and pushed to `revamp`

"The shuffle animation looks too violent — properly research animation so
it's not too fast, use smooth animation to mimic background cards coming to
the front after animating. Don't touch the design, just the animation."

## Research

Web + docs research on Compose card-deck/slot-machine animation:
- Use physics springs with higher damping / lower stiffness for a luxurious
  glide (Spring.DampingRatio ~0.85, low stiffness).
- Use `FastOutSlowInEasing` instead of `LinearEasing` for transitions; avoid
  short 90ms linear blurs.
- Prefer eased tweens (200–260ms) for fan-deck back-to-front swaps; apply
  scale/translation via `graphicsLayer`; use `AnimatedContent` for discrete
  slot content swaps so the incoming item glides in rather than snapping.

## Root causes of "violent" shuffle (before)

1. Cadence: squared-sine ease `sin(progress²·π/2)` with 105→400ms intervals
   = "whip fast then slam".
2. Hero tick pulse: snap to 1.035 on a stiffness-1000 spring, 40° tilt
   factor, 18dp hop per tick.
3. Peek card wipes: 90/80ms `LinearEasing` slides = blur.
4. Hero content (title/tags/teaser) snapped instantly every tick (no
   transition at all); landing settled with the extreme `Elastic` spring.

## Change (2 files)

**`app/src/main/java/com/curio/app/ui/theme/CurioMotion.kt`**
- `SpinMin`/`SpinMax` 2400/3200 → 2800/3600ms (slightly longer, unhurried
  window). Header docblock updated to match (was stale at 2400/3200).

**`app/src/main/java/com/curio/app/features/spin/SpinScreen.kt`**
- Shuffle cadence: plain sine ease `sin(progress·π/2)`, intervals
  200→520ms — graceful reel slow-down instead of a whip.
- Hero tick pulse: kick 1.035 → 1.02 on a heavily damped low-stiffness
  spring (damping 0.85, stiffness 420); rock 40° → 16°; hop 18dp → 12dp.
- Landing settle: `Elastic` → `Deliberate` spring (no violent bounce).
- Hero content (name/tags/teaser) wrapped in `AnimatedContent` keyed on
  `topic` — incoming content slides up from the lower edge + fades
  (200/180ms when shuffling, 260/240ms otherwise), mimicking a background
  card rising to the front.
- Peek card wipes: 90/80ms `LinearEasing` → 200/180ms `FastOutSlowInEasing`.
- v6.6 KDoc entry added. Design (sizes, colors, layout) untouched.

## Review
- code-reviewer-deepseek-flash (x2): clean. One concrete nit — stale
  SpinMin/SpinMax header docblock — fixed. Noted non-blocking: `AnimatedContent`
  default `SizeTransform(clip = true)` may clip the height/3 slide on the
  hero; consistent with existing peek cards, only worth addressing if it
  reads as a visible cut. Imports safe (`LinearEasing` still used by
  OrbitRing + dice tumble); braces balanced; `height / 3` Int division
  matches peek style.

## CI
- Compile gate = GitHub Actions on push (per AGENTS.md — no local Gradle).
