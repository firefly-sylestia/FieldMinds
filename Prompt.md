# Prompt.md — Request Log

## Latest Request (IN PROGRESS)

**Main card: enhanced typography default, discoveries credit their
discoverer, refined border, refined pastel ink**

### Requested

1. Use the enhanced typography of the main (hero) card — ship it now
   without a setting.
2. Add the discoveries' discoverer to the card ("who discovered it").
3. Don't add an accent border by default — refine it, it's too harsh.
4. Refine the font color in pastel mode on the main card — some look weird.

### Analysis

- Enhanced typography lives behind the v7.13 `heroTitlesState` toggle
  (34sp ExtraBold display title, tracked bold tag pills, roomier teaser).
  Per DOX, a settled experiment gets hardcoded and the toggle removed.
- `CurioTopic.byline` already exists and the hero card already renders a
  creator byline pill (Artist/Author/Director/Painter), but `DISCOVERIES`
  was not in the label map, and `discoveries.json` had NO byline data at
  all (124 topics).
- The hero border: default OFF already draws a 1.5dp ink outline at 0.35
  alpha (harsh), and the ON state draws a 2dp white→accent rim-light + a
  black bevel hairline (the "harsh" look).
- Pastel hero ink used `cat.onAccent()` — raw Tailwind-700 accent, plus a
  fixed `DeepPlum` special-case for pale accents (wildcard coral) — the
  odd cases. Every other pastel surface uses `pastelFillInk(accent)`.

### Plan

1. `SpinScreen.HeroTicketCard`:
   - Hardcode the enhanced typography branches (title / tags / teaser /
     ink alphas); remove `heroTitlesOn`.
   - Border: default → 1dp hairline at 0.18 ink alpha (no accent border by
     default); ON rim-light softened (1.5dp stroke, gentler brighten/accent
     lerps, fainter bevel hairline).
   - Pastel ink → `if (pastel) pastelFillInk(accent) else cat.onAccent()`.
   - Byline label map: add `DISCOVERIES -> "Discovered by"`.
2. `TopicRevealScreen` byline map: add the same DISCOVERIES label.
3. `AppPreferences` + `SettingsScreen`: remove the heroTitles preference,
   key, state, init read, and the "Enhanced typography" Settings row.
4. Data: new `scripts/enrich_discoveries_bylines.py` (curated id →
   discoverer map) fills `byline` on 114 of 124 discoveries; 10 anonymous
   (prehistoric fire/wheel/agriculture/writing, Theory of Everything,
   smartphone, social media, ocean acidification) stay uncredited.
5. SCHEMA.md byline doc + changelog + Prompt.md.

### Status

- Code edits applied (SpinScreen / TopicRevealScreen / AppPreferences /
  SettingsScreen); enrichment script written (114/124 bylines); SCHEMA.md
  + changelog updated.
- Zero `heroTitles` references remain in `app/`.
- Reviewer verified compile-safety; long bylines trimmed to glanceable
  credits (longest now 33 chars). discoveries.json validates clean.
- Applied, committed, pushed.
