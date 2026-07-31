# CURIO — DISCOVERY APP
## UI/UX SPEC v2

Style system: Material Design 3, customized with the Midnight Signal identity: deep navy surfaces, electric-blue signal geometry, orange energy accents, and mint aperture highlights.
**Inherits icon system + geom typography from the legacy FieldMind app** (preserved at `app-legacy/`).

---

## Modification Log

| v | Date | Change |
|---|---|---|
| v1 | (user's original paste) | Initial spec with Category Picker as bottom sheet + Exploration Hub in the Spin→Capture flow + emoji-based category totem icons. |
| **v7** | 2026-07-31 | Profile preferences and activity cards receive a Material Expressive polish pass with stronger card containers, dark-safe edit actions, and inline daily reminder time chips. Category Picker removes the extra Choose a lane hero card and uses compact guidance plus expressive deck tiles. |
| **v2** | today | **Category Picker → full-screen page** (own back-stack entry). **Exploration Hub removed** from the flow (Topic Reveal goes straight to Record/Capture; scratchpad state preserved for future). **No emoji anywhere** — Material Symbols (inherited from `app-legacy/`) for category glyphs, `geom.ttf` (inherited from `app-legacy/`) for display/headline typography. "Recently explored" carousel confirmed on Home + Cabinet stays as separate bottom-nav tab. New §13 (Missing/Additional Screens) + §14 (Open Decisions). |
| **v3** | 2026-07-30 | Category Picker cards use full-accent hero-card styling with 2-column spacing. Shuffle screen includes an in-screen category rail so category changes retarget the current topic pool. First launch routes Splash → Onboarding until the intro is completed. Capture formats support saved voice-note context plus image attachments for Gallery Wall and Field Notes. |
| **v4** | 2026-07-31 | Home, Shuffle, and Topic Reveal use an opaque paper-card visual language: warm solid surfaces, crisp category-color edges, and layered elevation for depth. Ambient background gradients, glassy sheens, translucent card shells, and accent halos are intentionally excluded. |
| **v5** | 2026-07-31 | Replaced the pastel discovery wheel with the Midnight Signal angular portal/beacon mark. Replaced the palette across Android, splash, launcher, themed icon, website, and category accents with navy, electric blue, orange, mint, cyan, and cobalt tokens. |
| **v6** | 2026-07-31 | User-facing roulette language is now Shuffle. The Shuffle deck places the active card visually above two full behind-cards, adds lift/tilt shuffle motion, and uses filled expressive controls. Profile cards move to opaque expressive gradients; detail images open into a real zoomable lightbox. |

Working name for the app: **CURIO**
Working name for the roulette feature: **SHUFFLE**
Working name for the saved-items library: **THE CABINET**

(All names are placeholders — swap freely, everything below still applies.)

---

## 0. DESIGN SYSTEM FOUNDATION

### 0.1 PHILOSOPHY
Curio's whole point is "delighted curiosity." Every screen should feel like opening a little box you didn't know you wanted opened. M3 gives us the structural bones (dynamic color, elevation tiers, shape system, motion tokens) — the custom layer sits on top as personality: rounder corners than stock M3, a mascot-like spin dial, hand-drawn-feeling icons, and soft "paper confetti" micro-animations on rewards.

**Surface direction (v4):** Home, Shuffle, and Topic Reveal are tactile paper interfaces, not glass interfaces. Cards and trays must use opaque theme surfaces, visible category-color rules/edges, and elevation or offset layering to create depth. Do not add ambient background gradients, translucent card shells, blur, glossy sheens, or accent halos to these screens. Alpha is reserved for decorative ink, icon/text hierarchy, and subtle borders.

### 0.2 COLOR SYSTEM — MIDNIGHT SIGNAL
The brand is intentionally not a recolor of the former wheel. Midnight Signal uses a cool, high-contrast foundation with a geometric signal language: midnight ink for focus, electric blue for discovery, orange for energy, mint for the aperture, and cyan/cobalt for category distinction.

  Midnight Ink / On-Primary.. #081B33
  Signal Blue / Primary....... #1264C5
  Electric Blue............... #3D8CFF
  Signal Orange / Secondary... #E6652F
  Aperture Mint / Tertiary.... #009E83
  Light Surface............... #F8FBFF
  Light Container............. #E7EEF6
  Error....................... #BA3A4B

Each category keeps its existing source-compatible token name while adopting a
signal color used for chips, category headers, spin segments, and hero rules:
    Music / Artists ........ Electric Blue #3D8CFF
    Movies / Directors ..... Cobalt        #5B5FEF
    Books / Authors ........ Mint          #16B89A
    Visual Art / Painters .. Orange        #E6652F
    Science & Nature ....... Cyan          #079DB8
    Wildcard ............... Signal-spectrum gradient (blue → cyan → mint →
                              orange → cobalt)

The launcher mark is an angular open portal with a mint aperture and an orange
signal spark. It is used consistently by the adaptive launcher, themed icon,
splash screen, web SVG, and download presentation.

  **Forward note:** the category palette will expand from 6 → 10 categories
  over v1.1–v1.4 (adds Philosophy & Ideas, History & Mythology,
  Architecture & Design, Food & Culture). For the full data-layer roadmap
  — new category accents, topic schema, authoring pipeline, rollout cadence
  — see **`app/CURIO_DATA_PLAN.md`**.

### 0.3 SHAPE SYSTEM
  M3 shape tokens, but pushed rounder across the board:
    Small components (chips, small buttons) ..... 16dp corner radius
    Medium components (cards) ................... 24dp corner radius
    Large components (sheets, dialogs) .......... 32dp corner radius top
    Shuffle dial itself ......................... perfect circle
  Nothing in the app should have a hard 90° corner except dividers/rules.

### 0.4 TYPOGRAPHY **(v2)**
  Display / headline: **`geom.ttf`** (variable font, **inherited from the legacy FieldMind app** at `app-legacy/src/main/res/font/geom.ttf`, copied into `app/src/main/res/font/`). Display = heavy weight (700+).
  Body / UI text: a clean neutral sans (M3 default, e.g. Roboto Flex or Inter) for readability in long essay/journal entries.
  Rule of thumb: `geom` for anything short and emotional (titles, empty-state copy, button labels). Neutral sans for anything long or functional (body copy, form fields, settings).

  `geom` is the only display font — no Fredoka / Baloo / Nunito fallback. It carries the FieldMind legacy into Curio and keeps the visual identity consistent across the two apps for users who have both installed.

### 0.5 MOTION PRINCIPLES
  - Spring-based easing everywhere (M3 "expressive" motion spec), never linear. Things should overshoot slightly and settle — like a gummy bounce, not a rigid slide.
  - Shuffle screen is the one place we allow a longer, showier animation (2.5–4s). Everywhere else, keep transitions under 400ms so the app never feels like it's making you wait to be delighted.
  - Rewarding moments (topic revealed, entry saved) get a small confetti / sparkle burst — a scatter of 6–10 tiny shapes in the category's accent color, fading and falling with slight rotation, ~600ms total.

### 0.6 ICONOGRAPHY **(v2 — no emoji, Material Symbols from legacy)**
  **NO emoji anywhere in the app** — no 🎵, no 🎬, no 📖, no 🎨, no 🌿, no 🎲, no ✨ in any user-facing copy or visual element. Emoji are visually inconsistent across OSes and break the "designed" feel.

  Use **Material Symbols variable font** (inherited from the legacy FieldMind app at `app-legacy/src/main/res/font/material_symbols_outlined.ttf`, copied into `app/src/main/res/font/material_symbols_outlined.ttf`). **ASCII-mockup convention**: the ASCII layout skeletons throughout this spec use Unicode glyphs (`←` back arrow, `✕` close, `☰` menu, etc.) as visual placeholders. The actual implementation will use Material Symbols (`arrow_back`, `close`, `menu`, etc.) — see `CurioIcons.kt` (TBD). Each category has one consistent Material Symbols glyph used everywhere it appears:

    Music ........ `album`              (vinyl record glyph)
    Movies ....... `movie`              (clapperboard)
    Books ........ `menu_book`          (open book)
    Visual Art ... `palette`            (artist palette)
    Science ...... `science`            (atom / flask — pick the variant that reads best)
    Wildcard ..... `casino`             (die)

  UI affordance icons (search, settings, overflow, back arrow, close X, etc.) also come from Material Symbols Outlined. Keep the glyph names in a single Kotlin constants object (`CurioIcons.kt`) so they can be looked up centrally and updated if Material Symbols glyph names change.

---

## 1. SCREEN MAP (navigation overview) **(v2)**

```
  Onboarding (first launch only)
      │
      ▼
  HOME  ──────────────► CATEGORY PICKER (full-screen page, own back-stack entry)
      │                        │
      │                        ▼
      │                  SHUFFLE (roulette)
      │                        │
      │                        ▼
      │                  TOPIC REVEAL
      │                        │
      │                  (NO Exploration Hub — straight to Save/Capture)
      │                        │
      │                        ▼
      │                  SAVE / CAPTURE  (format differs per category)
      │                        │
      │                        ▼
      ├───► THE CABINET (library) ◄─────────┘
      │            │
      │            ▼
      │      ENTRY DETAIL (view a saved capture)
      │
      ▼
  SETTINGS (from Home overflow / top-right avatar)

  Bottom navigation (persistent, 3 destinations):
    [ Home ]     [ Shuffle ]     [ Cabinet ]
  Settings lives in Home's top-right avatar/menu, not in bottom nav — it's
  not a "destination" you explore, so it shouldn't compete for a nav slot.
```

See §13 for the full list of additional / missing screens (lightbox, share preview, splash, etc.).

---

## 2. SCREEN: ONBOARDING (3 slides, first launch only)

**Unchanged from v1.**

PURPOSE
Set expectations in under 20 seconds: this app hands you a topic, you go explore it in the real world (Spotify, YouTube, a museum site, a book), then you come back and capture what you found.

LAYOUT SKELETON
  ┌─────────────────────────────┐
  │                             │
  │        [ illustration ]     │  ← big, centered, playful
  │                             │
  │        Headline text        │  ← geom, 28sp, heavy weight
  │      Supporting subtext     │  ← Inter, 16sp, muted
  │                             │
  │      ● ○ ○   (page dots)    │
  │                             │
  │   [ Skip ]        [ Next ]  │
  └─────────────────────────────┘

SLIDE CONTENT
  1. "Spin into something new" — illustration of the dial mid-spin.
  2. "Go explore it, your way" — illustration of headphones/book/museum icons fanned out.
  3. "Save it your way too" — illustration of a voice waveform, a page, and a photo collage fanned together.

INTERACTIONS
  - Swipe horizontally or tap Next to advance.
  - "Skip" jumps straight to Home from any slide.
  - On slide 3, "Next" becomes "Let's go" (primary filled M3 button, pill-shaped, coral).
  - Page dots are tappable — jumping to any dot navigates directly there.
  - This whole flow never reappears after first completion (persisted flag), but is reachable again from Settings ("Replay intro" → §11 → §13.4). Splash must route first-time installs here before Home.

---

## 3. SCREEN: HOME **(v2 — Recently explored emphasized)**

PURPOSE
The calm "front porch" of the app. Shows the user's momentum (streak, recent captures) and gives one unmistakably obvious way to start: spin.

LAYOUT SKELETON
  ┌─────────────────────────────┐
  │ ☰ Curio              👤    │  ← top app bar, transparent, no elevation
  │                             │
  │  "Good evening, Alex"       │  ← greeting, time-aware
  │  "3-day curiosity streak"   │  ← small pill, only shows if streak > 0
  │                             │
  │   ╭───────────────────╮     │
  │   │                   │     │
  │   │   SPIN THE WHEEL  │     │  ← big hero CTA card, dial illustration
  │   │      (tap)        │     │     peeking out from behind the card
  │   ╰───────────────────╯     │
  │                             │
  │  "Pick a category" ▾        │  ← chip row, horizontally scrollable
  │  [Music][Movies][Books]...  │
  │                             │
  │  Recently explored          │  ← **(v2) horizontal card carousel,
  │  [card][card][card] →       │     duplicates Cabinet's most-recent
  │                             │     saves so Home feels alive**
  │
  │  [ Home ] [ Spin ] [Cabinet]│  ← bottom nav
  └─────────────────────────────┘

COMPONENTS & BEHAVIOR
  - Greeting: "Good morning / afternoon / evening, {name}" — falls back to "Welcome back" if no name is set.
  - Streak pill: only rendered if the user has explored on consecutive days; tapping it opens a small info popover that dismisses on outside tap.
  - Hero Spin card: the single largest tap target on the screen (~40% vertical). Tapping with NO chip selected → Category Picker full-screen page. Tapping WHILE a chip is selected → skips picker, goes straight to Shuffle pre-loaded with that category.
  - Category chip row: filter chips (M3 style, pill), one per category plus a "Surprise me" chip pinned at the far left with the `casino` wildcard glyph. Tapping a chip selects it (fills with that category's accent color, checkmark fades in) and updates the hero card's subtext to "Spin for {Category}". Only one selectable at a time. Tapping the already-selected chip deselects it.
  - **(v2) Recently explored carousel**: horizontally scrolling cards, each a thumbnail of a saved capture (waveform glyph for voice notes, first line of text for essays, image collage thumbnail for moodboards). Mirrors The Cabinet's most-recent saves — same data source, same card rendering component. Tapping a card → Entry Detail. Long-press → quick-action popover [Reopen] [Share] [Delete] (same popover as Cabinet §9).
  - Empty state (brand new user, nothing explored yet): carousel section replaced by a soft illustration + text: "Nothing here yet — give the wheel a spin!" with a small arrow pointing up toward the hero card.

---

## 4. SCREEN: CATEGORY PICKER **(v2 — now a full-screen page, not a bottom sheet)**

PURPOSE
Dedicated screen for category selection when the user taps the hero Spin card without a chip pre-selected. Full-screen gives more room to breathe than a bottom sheet would, lets the user see and compare all 6 categories at once, and supports a deliberate "browse then commit" flow.

LAYOUT SKELETON
  ┌─────────────────────────────┐
  │ ←  What are we exploring?  │  ← own back-stack entry, top-left back arrow
  │                             │     returns to Home (no action taken)
  │  [Focused decks] [Surprise] │  ← compact guidance chips, no large pre-grid lane card
  │  Pick a mood...             │
  │  ┌─────────┐ ┌─────────┐    │
  │  │  Music  │ │ Movies  │    │  ← 2-column expressive deck cards
  │  └─────────┘ └─────────┘    │     (96dp min height each)
  │  ┌─────────┐ ┌─────────┐    │
  │  │  Books  │ │  Art    │    │
  │  └─────────┘ └─────────┘    │
  │  ┌─────────┐ ┌─────────┐    │
  │  │ Science │ │ Wildcard│    │
  │  └─────────┘ └─────────┘    │
  │                             │
  │  [ Manage categories ]      │  ← filled tonal button, opens §13.4
  └─────────────────────────────┘

INTERACTIONS
  - **Own back-stack entry**: navigating here pushes onto the Home screen's back stack. Back arrow pops back to Home with no action taken. This is distinct from a bottom sheet (which would be a transient overlay).
  - Each tile: large tap target (min 96dp height), opaque category gradient, rounded 30dp expressive shape, top icon chip, bottom-aligned deck title/action pill, and a soft watermark glyph. On tap: tile briefly scales to 96% then springs back to 100% (haptic tick), screen auto-pops after ~150ms, and the app navigates straight into Shuffle pre-loaded with that category.
  - The Wildcard tile uses the rainbow gradient background instead of a flat tint — reinforces that it's the "anything goes" option.
  - "Manage categories" is a low-emphasis filled tonal button at the bottom for users who want to hide categories they don't care about — routes to Settings → Manage Categories (§13.4).

---

## 5. SCREEN: SHUFFLE (roulette) — the signature moment

**Unchanged from v1.**

PURPOSE
This is the emotional centerpiece of the whole app. It needs to feel like a genuine little ritual — a breath before something new arrives — not just a spinner widget with a random number generator behind it.

LAYOUT SKELETON
  ┌─────────────────────────────┐
  │ ←  {Category name} · Spin   │  ← top bar shows active category
  │                             │
  │        ╭─────────╮          │
  │      ╭─┤         ├─╮        │
  │      │ │  DIAL   │ │        │  ← large circular dial, ~70% of
  │      ╰─┤         ├─╯        │     screen width, segmented in the
  │        ╰─────────╯          │     category's accent color family
  │             ▲                │  ← pointer/needle at top
  │                             │
  │      "Tap to spin"          │  ← helper text, fades once spinning
  │                             │
  │   ╭───────────────────╮     │
  │   │      S P I N       │     │  ← big pill CTA button, sits BELOW
  │   ╰───────────────────╯     │     the dial (not the dial itself,
  │                             │     to keep dial purely visual/tap-
  │                             │     anywhere-on-dial also works)
  └─────────────────────────────┘

INTERACTIONS
  - Tapping either the dial itself OR the "SPIN" button below it triggers a spin (redundant tap targets).
  - Spin animation: dial rotates 3–5 full rotations with spring-based deceleration (ease-out-back), total duration 2.5–3.5s, landing on a random segment. A soft ratcheting "tick tick tick" haptic plays as segments pass the pointer, slowing as it settles — like a real prize wheel.
  - While spinning: the "SPIN" button disables and its label swaps to "Spinning…" with a subtle shimmer; the dial cannot be tapped again mid-spin.
  - On landing: the winning segment pulses/glows once, a confetti burst fires from the pointer tip in the category's accent color, and after ~400ms the screen auto-transitions (slide + fade) into Topic Reveal. No manual "confirm" tap needed.
  - Segments for named categories (Music, Movies, Books, Art, Science) are pre-defined weighted pools (e.g. genres/eras) that narrow down to a specific topic server-side/algorithmically after landing — the dial doesn't need one segment per possible topic, just enough visual segments (6–8) to feel substantial.
  - Wildcard dial: segments are the OTHER five category icons themselves (Music/Movies/Books/Art/Science) rendered in rainbow-swept color — landing on one silently picks a topic from that category behind the scenes, then reveal still says "Your wildcard pick:".
  - Back arrow (top-left): returns to Home, category selection is discarded.

---

## 6. SCREEN: TOPIC REVEAL **(v2 — "Start exploring →" now skips the Hub)**

PURPOSE
The payoff. Present the assigned topic with just enough context to make the user excited to go explore it, without info-dumping.

LAYOUT SKELETON
  ┌─────────────────────────────┐│ ✕   │  ← close = discard, top-right
  │  (decorative accent)   │
  │                             │
  │      [ topic image ]        │  ← artist photo / book cover / poster /
  │                             │     painting thumbnail / nature photo
  │                             │     (Wildcard: whatever its category is)
  │                             │
  │      Frida Kahlo            │  ← headline, geom, large
  │      Visual Art · Painter   │  ← category + subtype tag chip
  │                             │
  │  "One quirky fact to get    │  ← 1–2 sentence teaser, NOT a full bio
  │   you curious..."           │     (avoid spoiling the exploration)
  │                             │
  │  ╭───────────────────────╮  │
  │  │   Start exploring →   │  │  ← **(v2) primary filled button now
  │  ╰───────────────────────╯  │     routes directly to Save/Capture**
  │  [ Spin again instead ]     │  ← low-emphasis text button
  └─────────────────────────────┘

INTERACTIONS
  - **(v2) "Start exploring →"**: navigates directly to Save/Capture (§8), passing the topic along. The Exploration Hub from v1 is skipped — scratchpad state is preserved for a future iteration but not surfaced in this flow.
  - "Spin again instead": no confirmation dialog (nothing committed yet) — returns straight to Shuffle, same category pre-loaded, ready to spin immediately.
  - "✕" top-right: same as "Spin again" but exits all the way back to Home instead of re-spinning.
  - **(v2) Topic image**: tap opens the Image Lightbox (§13.2) — full-screen, pinch-zoom, swipe down to dismiss.
  - This screen is NOT saved anywhere yet — if the user backs out, the topic is gone for good.

---

## 7. SCREEN: EXPLORATION HUB **(v2 — DEPRECATED)**

**Removed from the v2 flow per user decision.** Topic Reveal now goes straight to Save/Capture.

A scratchpad + timer + quick-jump-off-points screen between Topic Reveal and Save/Capture was considered but cut. The reasoning: it added friction (an extra screen to dismiss or skip), and users tended to either (a) want to go straight to capture or (b) leave the app entirely to research, not stare at an in-app scratchpad.

The scratchpad concept is preserved as state for a possible v3 — see §13.6 (Scratchpad Archive). If reintroduced in a future version, the saved scratchpads from v2-era sessions could be recovered.

---

## 8. SCREEN: SAVE / CAPTURE — one shell, six format bodies

**Unchanged from v1.** All six categories share the same outer shell; only the middle content area changes per the category's unique format.

(Sections 8.1 through 8.6 — Sound Bite, Reel Notes, Marginalia, Gallery Wall, Field Notes, Open Notebook — are unchanged from v1. The shared save behavior at the end of v1 §8 also carries forward verbatim.)

SHARED OUTER SHELL
  ┌─────────────────────────────┐
  │ ←  Save your take            │  ← consistent header across all 6
  │  Frida Kahlo · Visual Art    │  ← slim topic reminder strip, tap to
  │                              │     re-view Topic Reveal card again
  │                              │
  │  [ ...format-specific area... ] │
  │                              │
  │  ╭────────────────────────╮  │
  │  │      Save entry         │  │  ← sticky bottom CTA, all formats
  │  ╰────────────────────────╯  │
  └─────────────────────────────┘

SHARED SAVE BEHAVIOR
  - Tapping "Save entry" (once enabled): brief full-width progress shimmer on the button (~400ms, simulating a save), then a confetti burst in the category's accent color fires from the center of the screen, a success snackbar reads "Saved to your Cabinet" (no emoji — Material Symbols `check` glyph optional but text-only is fine), and the app navigates to Entry Detail for the just-created entry.
  - Back arrow before saving: confirm dialog —
      "Discard this capture?"
      "You'll lose what you've added here. This can't be undone."
      [ Keep editing ]   [ Discard ]

---

## 9. SCREEN: THE CABINET (library)

**Unchanged from v1.**

PURPOSE
Where every saved capture lives — the "trophy shelf" of everything the user has explored, browsable by category or all together.

LAYOUT SKELETON
  ┌─────────────────────────────┐
  │  The Cabinet          🔍    │  ← top bar, search icon top-right
  │                             │
  │  [All][Music][Movies][Books]│  ← filter chip row, horizontally
  │  [Art][Science][Wildcard]   │     scrollable, "All" selected default
  │                             │
  │  ┌─────┐  ┌─────┐          │
  │  │entry│  │entry│          │  ← 2-column masonry/grid of entry cards
  │  └─────┘  └─────┘          │     (card shape/preview differs per
  │  ┌─────┐  ┌─────┐          │     format — waveform icon, text
  │  │entry│  │entry│          │     excerpt, collage thumbnail, etc.)
  │  └─────┘  └─────┘          │
  │                             │
  │  [ Home ] [ Spin ] [Cabinet]│
  └─────────────────────────────┘

(See §13 for empty-state catalog and search-results state.)

---

## 10. SCREEN: ENTRY DETAIL

**Unchanged from v1.**

PURPOSE
A polished, "framed" presentation of one saved capture.

LAYOUT SKELETON
  ┌─────────────────────────────┐
  │ ←                    ⋮      │  ← overflow menu top-right
  │                              │
  │  [ topic image, hero size ] │
  │  Frida Kahlo                 │
  │  Visual Art · Painter        │
  │  Captured 3 days ago         │
  │                              │
  │  ── format-specific render ──│
  └─────────────────────────────┘

BEHAVIOR
  - Renders each format in its most "finished" presentational state — NOT the same editable widgets from Save/Capture.
  - Overflow menu (⋮): [ Edit ]  [ Share ]  [ Delete ].
      - Edit: reopens the Save/Capture screen for that format, pre-filled.
      - Share: opens the OS share sheet with a renderable share card (see §13.3).
      - Delete: confirm dialog.
  - Swipe left/right (optional): navigates to adjacent entry in current filter.

---

## 11. SCREEN: SETTINGS

**Unchanged from v1.**

LAYOUT SKELETON
  ┌─────────────────────────────┐
  │ ←  Settings                  │
  │                              │
  │  Profile                     │
  │  ▸ Name & avatar              │
  │                              │
  │  Categories                  │
  │  ▸ Manage categories          │  ← opens §13.4
  │                              │
  │  Appearance                  │
  │  ▸ Theme (Light/Dark/System) │
  │                              │
  │  Notifications                │
  │  ▸ Daily shuffle reminder[on]│  ← toggle switch
  │                              │
  │  About                       │
  │  ▸ Replay intro               │  ← replays Onboarding (§2)
  │  ▸ Version 1.0                │
  └─────────────────────────────┘

NOTES
  - "Manage categories" opens a reorderable list with visibility toggles per category — see §13.4 for full spec.
  - "Daily shuffle reminder" toggle, when turned on, reveals inline rounded time chips beneath it; selecting a chip updates the reminder immediately without opening a dialog.
  - Settings is intentionally the LEAST "playful" screen in the app.

---

## 12. GLOBAL DIALOG / COMPONENT LIBRARY (reused across screens)

**Mostly unchanged from v1. v2 notes:**
- Confirm dialog: same shape, but remove emoji from any sample copy.
- Snackbar / Toast: success messages use plain text + optional Material Symbols `check` glyph, no emoji.
- Bottom sheet: still used for quick-action popovers (Open / Share / Delete from Cabinet + Entry Detail long-press). NOT used for Category Picker — that became a full-screen page (§4).

(Sub-sections 12.1 through 12.5 carry forward from v1 verbatim with emoji removed from any sample copy.)

---

## 13. NEW — ADDITIONAL / MISSING SCREENS **(v2)**

The v1 spec covered the main happy-path flow but left several referenced screens and edge cases unspecified. This section catalogs them with enough detail to build. Each is its own discrete screen or modal with the same M3 + Curio styling as everything else.

### 13.1 SPLASH / INITIAL LOADING

PURPOSE
The first thing the user sees on app launch. Covers the gap between process start and MainActivity being ready.

LAYOUT
  ┌─────────────────────────────┐
  │                             │
  │                             │
  │                             │
  │      [ Curio logomark ]     │  ← large Material Symbols `auto_awesome`
  │                             │     or a custom mascot SVG, centered
  │      Curio                  │  ← geom, 36sp, heavy
  │                             │
  │                             │
  │  · · ·   (subtle pulse)     │  ← 3 dot loader, geom-text-colored
  │                             │
  └─────────────────────────────┘

BEHAVIOR
  - Shown automatically while the app initializes Room DB + reads onboarding-complete flag from SharedPreferences.
  - Maximum 800ms — if init takes longer, show a generic "Loading…" with the same dot loader.
  - Auto-dismisses when ready; if onboarding hasn't been completed, routes to Onboarding (§2); otherwise routes to Home (§3).
  - No back button. No interaction.

### 13.2 IMAGE LIGHTBOX

PURPOSE
Full-screen viewer for the topic image shown in Topic Reveal (§6) and Entry Detail (§10). Tap-to-zoom, swipe-down-to-dismiss.

LAYOUT (initial state)
  ┌─────────────────────────────┐
  │ ✕                            │  ← top-right close, dismisses to caller
  │                             │
  │                             │
  │      [ topic image ]        │  ← centered, fit-to-screen, no crop
  │                             │
  │                             │
  │                             │
  └─────────────────────────────┘

INTERACTIONS
  - Pinch-to-zoom (up to 4×). Two-finger pan when zoomed.
  - Double-tap to toggle between fit-screen and 2× zoom.
  - Swipe down to dismiss (M3 bottom-sheet-style dismiss gesture).
  - ✕ button also dismisses.
  - No save / share / etc. from here — that's Entry Detail's job.

### 13.3 SHARE PREVIEW

PURPOSE
When the user taps "Share" from Entry Detail (§10), Curio generates a renderable card image (topic + category glyph + 1-line excerpt) and hands it to the OS share sheet. This section defines what that card looks like.

CARD LAYOUT (rendered at 1080×1080 for OG / social, also 1080×1920 for IG stories)
  ┌─────────────────────────────┐
  │  Curio · {Category}         │  ← top-left, geom, 24sp
  │                             │
  │                             │
  │  Frida Kahlo                │  ← geom, 48sp, heavy
  │  Visual Art · Painter       │  ← Inter, 18sp, muted
  │                             │
  │  ───                        │  ← divider
  │                             │
  │  "One quirky fact..."       │  ← 1-line excerpt from the entry,
  │                             │     Inter, 20sp, italic
  │                             │
  │  ───                        │  ← divider
  │                             │
  │  Curio                      │  ← bottom-left, small
  └─────────────────────────────┘

BEHAVIOR
  - Card is rendered server-side via a Compose `Bitmap` capture + Canvas draw, then saved to a temp file in app cache, then handed to `Intent.ACTION_SEND` with `type = "image/png"`.
  - User can pick any installed app from the share sheet (Twitter, IG, FB, email, etc.).
  - No Curio branding watermark beyond the small "Curio.app" mark — keeps the share looking like the user's, not an ad.

### 13.4 MANAGE CATEGORIES

PURPOSE
Referenced from §4 and §11 but not detailed in v1. Lets users show/hide and reorder the 6 categories without losing past entries in hidden categories.

LAYOUT
  ┌─────────────────────────────┐
  │ ←  Manage categories        │
  │                             │
  │  Drag to reorder.           │  ← helper text, Inter, 14sp, muted
  │  Toggle to show or hide.    │
  │                             │
  │  ⋮  Music          [ on ]   │  ← drag handle, label, M3 switch
  │  ⋮  Movies         [ on ]   │
  │  ⋮  Books          [ on ]   │
  │  ⋮  Visual Art     [ on ]   │
  │  ⋮  Science        [ on ]   │
  │  ⋮  Wildcard       [ on ]   │
  │                             │
  │  [ Reset to defaults ]      │  ← text button, restores all-on + default order (see §0.2 for canonical order: Music → Movies → Books → Visual Art → Science → Wildcard)
  └─────────────────────────────┘

INTERACTIONS
  - Long-press any row to enter drag mode. Drag handle (⋮) lights up; the row lifts with elevation; drop to reorder.
  - Tap switch to toggle visibility. Hidden categories don't appear in Home's chip row, Category Picker, or Cabinet's filter chips. **Past entries in hidden categories are preserved** — they reappear in the Cabinet the moment the user re-enables that category.
  - "Reset to defaults" puts all 6 categories back to visible and the default order.

### 13.5 TOPIC HISTORY

PURPOSE
Every topic the user has ever spun lands here, regardless of whether they saved a capture. Lets users revisit a topic they didn't capture the first time, or just see their own curiosity trail.

LAYOUT
  ┌─────────────────────────────┐
  │ ←  Topic history       🔍   │
  │                             │
  │  [All][Music][Movies]...    │  ← filter chips, same pattern as Cabinet
  │                             │
  │  Today                      │  ← sticky day-header
  │  Frida Kahlo · Visual Art   │  ← tap → reopens Topic Reveal
  │  Brian Eno · Music          │
  │                             │
  │  Yesterday                  │
  │  Carol Dweck · Books        │
  │  ...                        │
  │                             │
  └─────────────────────────────┘

ACCESS
  - Not a top-level tab (would dilute the bottom nav). Reachable from Settings → "Topic history" and from Home overflow menu.
  - Stored in the Curio Room DB at write-time of Topic Reveal — no extra work needed at the spin animation, just one INSERT when the spin lands.

### 13.6 SCRATCHPAD ARCHIVE **(placeholder for future) — see §7 deprecation note**

PURPOSE
Reserve the concept for a future iteration when Exploration Hub may return. For now, no UI — scratchpads are not captured at all.

If reintroduced: same sort, filter, and re-open affordances as Topic History (§13.5), with the addition of "Open in Capture" button that pre-loads the topic into Save/Capture.

### 13.7 EMPTY STATE CATALOG

A consistent vocabulary for the empty states across the app. Each empty state follows the same skeleton:

  ┌─────────────────────────────┐
  │                             │
  │      [ illustration ]       │  ← Material Symbols glyph at 96dp,
  │                             │     category accent color tint
  │                             │
  │      Headline (geom)        │  ← 24sp heavy
  │      Subtext (Inter)        │  ← 16sp muted, 1–2 lines
  │                             │
  │      [ Primary CTA ]        │  ← optional, routes to the obvious
  │                             │     "next thing to do"
  └─────────────────────────────┘

Screens with empty states:

| Screen | Glyph | Headline | Subtext | CTA |
|---|---|---|---|---|
| Home (no captures yet) | `auto_awesome` | "Nothing here yet" | "Give the wheel a spin — your first discovery is one tap away." | "Spin the wheel" → Shuffle |
| Cabinet (no entries at all) | `inventory_2` | "Your Cabinet is empty" | "Everything you save will live here." | "Discover something" → Shuffle |
| Cabinet (filtered to empty) | `search_off` | "No {Category} captures yet" | "Spin for {Category} to find your first one." | "Spin for {Category}" → Shuffle (pre-loaded) |
| Topic History (no topics) | `history` | "No spins yet" | "Your first topic will appear here the moment you spin." | (none — Home CTA already covers it) |
| Search in Cabinet (no results) | `search_off` | "Nothing matches" | "Try a different word, or clear the filter." | "Clear search" |

### 13.8 ONBOARDING VARIANTS — first-launch vs. replay

The Onboarding (§2) flow handles both cases but the entry point differs:
- **First launch**: Splash (§13.1) checks `onboardingComplete` flag in SharedPreferences; if false, routes to Onboarding.
- **Replay** (from Settings → "Replay intro"): sets the same flag to false temporarily, routes to Onboarding. After the user reaches slide 3 and taps "Let's go", the flag is reset to true. The Cabinet + Topic History + Settings are NOT cleared — replay is purely a UX walkthrough, not a reset.

No separate "Replay Intro" screen needed; just navigation.

---

## 14. OPEN DECISIONS **(v2)**

Items the user has weighed in on inline during spec evolution. Captured here so future agents and contributors don't re-debate them.

| # | Decision | User's wording | Status |
|---|---|---|---|
| 1 | Category Picker presentation | "Full-screen page (own back-stack entry, more room to breathe)" | ✅ Locked — applied in §1 + §4 |
| 2 | Exploration Hub fate | "dismiss it while saving its state — go straight from Topic Reveal to the Record/Capture screen" | ✅ Locked — Hub removed from flow; scratchpad state preserved as placeholder for future (§13.6) |
| 3 | Cabinet placement | "Keep Home and Cabinet separate, but also show recent saves on Home" | ✅ Locked — Cabinet stays a bottom-nav tab; Home has "Recently explored" carousel |
| 4 | Icon system | "dont use emoji use the icons of the previous app legacy just the icon system and typography geom" | ✅ Locked — Material Symbols + geom from `app-legacy/` (NO emoji anywhere) |
| 5 | Application ID | "New `com.curio.app`" | ✅ Locked — fresh install, separate from FieldMind |
| 6 | Data schema | "Curio schema + legacy tables kept but unused" | ✅ Locked — Curio has its own Room schema in its own data directory (`/data/data/com.curio.app/databases/curio_database`, name TBD in Phase 2). FieldMind's tables remain in FieldMind's separate install (`/data/data/fieldmind.research.app/databases/fieldmind_database`) for forensic recovery — sideload the legacy APK to extract via the V3 backup exporter in `app-legacy/`. The two apps are fully isolated; Curio cannot read FieldMind's tables directly. |
| 7 | **Data layer + category roadmap** | "we will be adding more categories and each category is going to have 100s of topics or more with proper explanation of what to do. and accurately etc we ill go each category one by one." | ✅ Locked — see **`app/CURIO_DATA_PLAN.md`** for the full plan. Taxonomy expands 6 → 10 categories (adds Philosophy, History, Architecture, Food); each category ships with 150+ topics authored via LLM-draft + human-review per the `ExploreAction` schema (verb + targetName + durationMinutes + instruction). Image strategy = URL + Coil (no APK bloat). Wildcard refactored to a meta-spin. Rollout order: Music → Movies → Books → Visual Art → Science → Wildcard → the 4 new categories. |
| 8 | Per-category `isReady` flag | (derived from #7) | ✅ Locked — every `CurioCategory` carries `isReady: Boolean`. Only `isReady = true` categories appear in Home's chip row + the Category Picker. Categories under construction are filtered out and surface as "Coming soon" empty-state slots. See `CURIO_DATA_PLAN.md` §1. |

---

## 15. END OF SPEC v2
### Next: scaffold Phase 2 (theme + icon system + nav + first home screen) on the `revamp` branch per the master plan.

### Companion docs
- **`app/CURIO_DATA_PLAN.md`** — the data layer companion to this UX spec. Owns category taxonomy expansion (6 → 10), topic data schema (`CurioTopic` + `ExploreAction`), authoring pipeline (LLM-draft + human-review), image strategy (URL + Coil, no bundling), and per-category rollout cadence (one category per PR, Music first).
