# Peek Card Design Suggestions (pending user review)

> **Status: RECOMMENDED SET IMPLEMENTED (v7.6, behind a Settings toggle).**
> The recommended set — 1a top-lit gradient, 1b category-tinted hairline,
> soft shadows, two-line near titles — is live in `SpinScreen.kt` behind the
> **Settings → Appearance → "Deck card redesign"** toggle, OFF by default
> (the classic flat deck stays the shipping look until the experiment
> settles). 1c (light-paper deck) is still unimplemented — say the word to
> add it to the toggle.

## Where the peek cards are today

`app/src/main/java/com/curio/app/features/spin/SpinScreen.kt` → `PeekCard`:

- **Size:** far cards 328×96dp, near cards 360×116dp (v6.5 grew them ~13%).
- **Fill:** a flat darkened accent — `lerp(accent, Black, 0.42f)` far /
  `0.28f` near — the same color in light and dark mode.
- **Content:** category glyph (20dp, white @ 0.55/0.75) + topic title
  (`labelLarge` = 14sp Bold, white @ 0.65/1.0, 1 line, ellipsis).
- **Edges:** 15/19dp corners, hairline white border @ 0.14/0.22, no shadow.
- **Fan:** rotations ±1.4°/±3.5°, scale 0.98/0.92, yOffsets ±134/±146/±178/±188.

What's working: the layered dimming reads clearly, the white-on-accent
contrast is strong, and the fan geometry is stable. What's missing: the flat
fill is monochrome (no light/shadow), the border is generic white, and the
title is capped at one 14sp line.

---

## Suggestion 1 — Colors: give the deck light, not just shade

**1a. Two-stop vertical gradient per card (smallest change, biggest win).**
Replace the flat fill with a vertical gradient from the accent to the
darkened accent — top edge catches "light", bottom reads as the card's
base shade, which makes the deck feel dimensional instead of flat slabs:

```
val base   = lerp(accent, Color.Black, if (far) 0.42f else 0.28f)
val crown  = lerp(accent, Color.White, if (far) 0.10f else 0.14f)
Brush.verticalGradient(listOf(crown, base))   // top-lit card
```

The top peek (next-up) catching more light also subconsciously signals
"the next card is coming" — directionally helpful on a reel.

**1b. Tint the hairline border with the category's light twin.** The white
border is the same on every category. In dark mode a tinted edge
(`Color.White.copy(alpha = …)` → the category's light accent at ~0.28/0.40
alpha for far/near) makes each deck layer whisper its category; in light
mode the deck could switch to the deep accent hairline
(`categoryInk()` @ 0.30 — reuse the existing `categoryBorder()` helper).

**1c. (Optional, bolder) Light-paper deck in light mode.** Curio's world is
cream paper + maroon ink. In light mode the peeks could be cream paper cards
(`lightAccentTint(accent, saturation = 0.18f, lightness = 0.94f)`) with the
topic in the deep accent ink — the deck then reads as fanned paper slips,
matching Home/Cabinet. Dark mode keeps today's dark-accent fill (paper
doesn't work on midnight). This is the biggest visual departure — only do it
if the paper metaphor is wanted on the Spin page.

## Suggestion 2 — Design: depth + hierarchy

- **Soft shadow under each card.** `shadowElevation = 0.dp` is flat; a
  small ambient shadow (`1.dp` far / `3.dp` near, or a `graphicsLayer`
  shadow) would lift the deck off the tinted page. Keep it subtle — the
  cards already stack by scale/rotation.
- **One-point fan instead of symmetric V.** Rotations are mirrored today
  (±1.4/±3.5). A slight bias (top cards rotated +1.5° more than their
  bottom mirror) reads as a hand-dealt spread. Small change, feels more
  "shuffled deck".
- **Far-card dimming — gentle slope.** 0.28/0.42 jumps are fine, but a
  three-step slope (near 0.24 / far 0.36) keeps the far pair more visible,
  so the deck never sinks into the background.
- **Reel affordance on the top peek.** A tiny chevron (or the card's glyph
  enlarged) at the top peek's leading edge hints "next up" — the reel
  mechanic users already feel.

## Suggestion 3 — Text size & title treatment

| Element | Today | Proposed |
|---|---|---|
| Near-card title | `labelLarge` 14sp Bold, 1 line | 16sp **SemiBold**, `letterSpacing 0.1sp`, **2 lines** (`maxLines = 2`) |
| Far-card title | same, 1 line | 13sp Medium @ 0.72 alpha, 1 line (they're hints, not reads) |
| Glyph | 20dp @ 0.55/0.75 | 22dp near / 18dp far (proportion with card height) |
| Tracking | none | `letterSpacing 0.15sp` on near titles — long names breathe |

The single biggest readability win is **2-line near titles**: the v6.5 size
bump gave the cards room, but `maxLines = 1` still clips long topic names
(e.g. "The Great Wave off Kanagawa" fits at 2 lines, ellipsizes at 1).

---

## What I'd pick if asked

- 1a (top-lit gradient) + 1b (category-tinted hairline) — cheap, on-brand.
- 2 soft shadows + 3 two-line 16sp near titles — the "beautiful" part.
- Skip 1c (light-paper deck) unless the paper metaphor is wanted app-wide.

All changes stay inside `SpinScreen.kt`; none affect the hero ticket, the
spin button, or the shuffle reel animation.
