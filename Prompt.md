# Prompt.md — Running Request Log

## Latest Request — "More color blends, properly analyse the blends"

### Status: ✅ Complete (committed & pushed)

### What the analysis found
`scripts/analyze_blends.py` (kept in repo) verified every CurioMixedDeck blend
against canonical HSL math + WCAG contrast vs white:

- All 15 curated pairs were hue-correct, but **5 fell below WCAG AA (4.5:1)**:
  Amber+Coral 3.86, Indigo+Coral 3.99, Rose+Coral 4.02, Amber+Sky 4.14,
  Amber+Teal 4.36 — they were deepened to the brightest shade still clearing
  4.5:1 (new #E32D0F, #BE39CE, #EA1142, #0B8484, #15875A).
- Triples had NO curated values — the old `distinct.reduce { hslBlend }` was
  **order-dependent** (selection order changed the color) and could drift.
- 4+ combos used the same flawed reduce.

### Changes (CurioColors.kt)
1. Refined the 5 sub-AA pair blends (hue/sat preserved, lightness deepened).
2. New `TripleBlends` map: 20 curated entries (all C(6,3) combos) computed as
   the order-independent HSL centroid + 4.5:1 lightness steering.
3. `mixedDeckAccent`: pairs→PairBlends, triples→TripleBlends (fallback
   hslCentroid), 4+→hslCentroid — no more sequential reduce.
4. New runtime `hslCentroid` (circular hue mean via atan2 of cos/sin sums,
   mean sat +0.05 boost) → `steerLightness` (32-iter binary search for
   brightest shade at 4.5:1 contrast) → `contrastVsWhite`/`toLinear`
   (WCAG relative luminance, `kotlin.math.pow`).
5. KDoc updated; every blend now clears 4.5:1 vs white.

### Validation
- Braces 21/21; 35 blend entries (15 + 20); zero stale reduce; spot-checked
  values match the analysis script; code review clean (one KDoc nit fixed).

### Prior work (this session)
- CI compile fixes (smart cast, RectangleShape, version-proof colors) —
  committed `5d52312d`
- Mood board pin-to-front + clear board — `f128ddfd`
- Edit mood board reuses saved entry-id watermark seed — `c6262518`
- Category pickers tap/long-press multi-select — `395f0abf`
- Premium mixed-deck colors (original) — `01414c20`
