# Prompt.md — Request Log

## Latest Request (COMPLETED)

**Spin density 2x tier + layout pinning — 3-way density picker (Off / Compact / 2x), Categories/Filter pinned to the nav bar, category-picker Mix button always visible**

### What was asked

“when i have density option on sometimes in some dpi the padding looks off the category and filters gets too much above the nav buttons and the done and mix button for category picker also hides can u pin it properly and also add a 2x density option for more lower dpi like 350 properly fix it” — ask_user: density strength picker (Off / Compact / Extra-compact), Mix row cut off in BOTH pickers, gap is above the app nav bar.

### What was done

- **AppPreferences.kt (v7.4):** the boolean `smartDensityLayoutState` became a 3-way `SmartDensityMode` enum (OFF / COMPACT / EXTRA_COMPACT) stored under a new string key, with one-time migration of the legacy boolean (true → COMPACT, false → OFF) and removal of the old key. Default COMPACT preserves the old always-on behavior.
- **SpinScreen.kt:** density tiers now read the mode — COMPACT keeps the classic rule (< 440 dpi → smaller, 440+ → roomier); EXTRA_COMPACT adds a 2x tier below the new `SpinExtraLowDensityDpi = 350`: deck scale 0.72 (`SpinDensityExtraCompactDeckScale`), 325dp carousel box, 12dp deck spacer + tighter spin-button padding.
- **BottomCta gap fix:** removed `navigationBarsPadding()` from both the normal row and the vertical-pill branch — verified from the bundled M3 1.5.0-alpha20 Scaffold source that `innerPadding.bottom = bottomBarHeight` exactly when the bottom bar exists, so the in-content inset padding was creating the floating gap above the nav bar. The Categories/Filter bar now sits flush on the app bottom nav.
- **Category-picker Mix row fix (both pickers):** the grid's `Modifier.weight(1f)` sat inside `MorphEntrance { … }` (a plain lambda), so the sheet Column measured the grid with unbounded height → full-height grid → Mix/Cancel row pushed off-screen. The grid now sits in a weighted `Box` that is a DIRECT child of the sheet Column (MorphEntrance moved inside), bounding the grid so the action row stays pinned — same pattern as the already-working FilterSheet.
- **SettingsScreen.kt:** the density Switch became a `SingleChoiceSegmentedButtonRow` (Off / Compact / 2x) with a summary subtitle, matching the theme-style picker pattern.
- **Fastlane:** new changelog `20260804.txt`.

### Validation

- `check_braces.py` BALANCED on all 4 touched files; no stale references to the old boolean API; the 2x tier takes priority over plain compact in every `when` (deck scale, carousel box, spacers, button padding).
- Code-reviewer: clean — confirmed the weight fix matches the M3 sheet internals (content lambda is `ColumnScope.() -> Unit`, no internal verticalScroll) and the inset reasoning against the bundled Scaffold source. Suggested a real-device visual check of the 0.72 deck on ~350 dpi.
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.

---

## Previous Requests (COMPLETED)

**Paper-texture redesign — real grain + crumpled creases on every page, torn pages gain coffee & folded options, coffee + folded quality pass**

### What was asked

“the pages dont have actualy texture like when its crumbled and open up it shold have that too. and also the torn pages also shpoul d have folded and coffee options and also redesign the cofee and folded paper pages as its not up to the design and mark in quality” — sequenced AFTER the Spin density toggle per ask_user (work order: Spin toggle first, then paper).

### What was done

- **Real paper texture (both card families):** new `drawPaperTexture` — fine grain (the shared torn-note bitmap tiled at 0.30 alpha on ruled sheets, full strength on torn slips) PLUS soft S-curve crease lines with an offset light bulge — the “crumpled then flattened” tooth of real paper. Drawn under the rules/ink on EVERY paper page (editor fields, save, detail). Fixed seed per size → deterministic; typing never re-rolls it.
- **Torn + coffee/folded combos:** `NotePaperStyle` gained `TORN_COFFEE` + `TORN_FOLDED` (appended → Gson-safe). `TornPaperCard` gained `coffeeStains` + `folded` params (fold flap overlays the torn outline; content inset 24dp so text never runs under it). The `NotePaperCard` dispatcher and the `RichTextEditor` when now cover all 8 styles; the style toggle shows Rules / Coffee / Folded sub-chips while a torn style is active (mutually exclusive taps).
- **Coffee quality pass:** irregular wobbling rim rings (radial-jittered Paths instead of compass circles), faint wet body inside, occasional double rings where the cup rocked, satellite splatter dots, plus two very faint washed patches.
- **Folded quality pass:** three-stop flap gradient (dark crease → light tip), a thin specular highlight just off the crease, a softer gradient drop shadow, crisper crease halo + hairline, and a corner-tip light spot.

### Validation

- `check_braces.py` BALANCED on all 3 files; both exhaustive `when`s cover all 8 styles; no stale refs (the per-card `grainBrush` remember was replaced by one shared lazy brush).
- Code-reviewer: clean after one visual fix — the crease “highlight” was drawn on the SAME path as the dark line (which would cancel it); now a parallel offset line reads as a fold bulge.
- Note for the user: no crumple/unfold ANIMATION exists in the app — the creases ARE the crumpled-then-flattened texture baked into every paper surface.
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push. Pushed.

---

## Previous Requests (COMPLETED)

**Smart density layout — two-way Spin sizing (toggleable): low-dpi phones get a smaller deck, high-dpi phones a roomier one**

### What was asked

“the low density thing we added give it a toggle and make it work revrse like if its a low dpi suppose 360 380 ddpi then the screen gets smaller and when its more dpi suppose 440 450 then its becomes larger you get it ?” — plus the earlier paper-texture request (real crumpled/torn texture; torn pages also getting folded + coffee options; redesigning the coffee & folded paper styles), sequenced AFTER this per ask_user. ask_user: density rule = **two-way scaling** (one toggle gates BOTH low → smaller AND high → larger); work order = **Spin toggle first, then paper redesign**.

### What was done

- **AppPreferences.kt:** new `smartDensityLayoutState` (default ON) + `KEY_SMART_DENSITY_LAYOUT` + is/set + seed in `initThemeMode` — mirrors the `smartSpinLayoutState` pattern exactly.
- **SpinScreen.kt (v7.3):** the density rule is now gated by that toggle and works BOTH ways:
  - `lowDensity = enabled && dpi < 440` → compact (smaller) — as before, but no longer always-on.
  - `highDensity = enabled && dpi >= 440` → new **roomy tier**: `SpinRoomyDeckScale` 1.05, carousel box 470dp (vs 444), roomier `SpinDeckSection` spacing (56dp spacer, 40/26 spin-button padding).
  - `roomy = highDensity && !heightCompact` — keyed off the RAW height (not the toggle-gated `compactHeight`), so a short high-dpi screen never gets the bigger deck even when the dimension rule is off.
  - New constants: `SpinHighDensityDpi = 440`, `SpinRoomyDeckScale = 1.05f`.
- **CurioIcons.kt:** added `PhotoSizeSelectLarge` glyph.
- **SettingsScreen.kt:** Experimental card gained a “Smart density layout” switch (“Smaller on low-density phones · larger on high-density”); Smart Spin layout subtitle simplified.

### Validation

- `check_braces.py` BALANCED on all 4 touched files; tier logic read back correctly.
- Code-reviewer: clean after one fix — it caught that `roomy` gated on the toggle-gated `compactHeight` could fire on a SHORT high-dpi screen when Smart Spin layout is off; keyed to raw `heightCompact` instead.
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push. Pushed.

### Follow-up — CI fix (Profile revamp compile error)

CI failed on the PROFILE rewrite (`8d1806f9` build): `ProfileScreen.kt:132:46 — @Composable invocations can only happen from the context of a @Composable function`. Cause: `CurioCategory.themedAccent()` is `@Composable` (CategoryInk.kt:46) and was called inside a `remember(categoryCounts) { … }` block (not a composable context). Fix: the hero accent/glyph are now computed as plain vals in the composable body (`topLane` → `themedAccent()` / `iconGlyph` with coral/sparkle fallbacks), still re-deriving from `categoryCounts`. Other `themedAccent()` call sites in the file were already in composable contexts (Surface/CurioIcon params). Braces balanced; committed + pushed.

### Next

Paper-texture redesign (real crumpled/torn texture, torn + folded/coffee combos, coffee/folded quality pass) — sequenced next per ask_user.

---

**Profile + Settings full revamp — quest-card hero, identity & stats Profile, Settings grouped into sections with an Experimental area, dead shared components deleted**

### What was asked

“the profile and settings feel out of place like its not consistent with the app design. and also its hero card of profile. fully redesign it. from the base like actually entire profile and settings revamp with experimental settings on.” ask_user: hero = **Quest-card style** (like Home's quest card); Experimental = **move existing experiments** (Smart Spin layout toggle); **Profile = identity & stats** only.

### Root cause

The old Profile used a fixed coral/peach/yellow gradient hero + deep-plum text that belonged to no other screen; Settings was a flat generic card list. Neither followed the app's shared language (category-tinted gradient heroes, paper cards with hairline borders, solid surface stat pills, watermark glyphs, icon-chip card headers).

### What was done

**Profile — `features/profile/ProfileScreen.kt` (full rewrite):**
- **Quest-card hero** — vertical `cardGradient(accent)` where accent + watermark glyph follow your most-explored lane (brand coral + sparkles before first save), letter-spaced “YOUR PROFILE” kicker, initial avatar, white-glass Edit + streak pills. Matches Home's quest card.
- **Stats strip** — solid `surfaceContainerLow` pills (streak / saved / lanes) in Home's stat-pill language instead of the old gradient-and-plum boxes. Lanes counts USED lanes once entries exist (falls back to visible count pre-save).
- **Level card** — gradient level medallion + progress bar; **Lanes card** — category chips + “Open the Cabinet”; **Settings nav card** — single entry (gear glyph + forward arrow); **Support & diagnostics** — report bug / crash logs / test crash.
- Removed the old duplicate Theme + Settings cards; personalization lives only in Settings now. Stats reload on composition entry AND ON_RESUME (app-switcher return) via `rememberCoroutineScope`.

**Settings — `features/settings/SettingsScreen.kt`:** reorganized into **General** (Profile → display name, Appearance), **Preferences** (Recording, Notifications, Categories), **Data** (Backup & restore), **Experimental** (Smart Spin layout toggle — moved out of Appearance, exactly as chosen), **Support** (About Curio). Card language unchanged and consistent with Profile.

**Cleanup — `ui/components/CurioSettingsComponents.kt`:** stripped to the only live symbol (`CurioSectionLabel`); deleted the dead 3-param `CurioSettingsRow`, `CurioToggleRow`, `CurioTimePickerRow`, `CurioStepButton`, and `CurioThemeMode` (zero external references confirmed).

### Validation

- `check_braces.py` BALANCED on all 3 touched files; every symbol the rewrite references verified to exist (`CurioGradients` in CurioColors.kt, `CurioCategories` in Category.kt, `CurioForwardArrow` in CurioTopBar.kt, all 13 icons, routes, streak/crash APIs).
- Code-reviewer pass: clean — all imports used, 4-arg `CurioSettingsRow` resolves, BOM-safe `LinearProgressIndicator` lambda. Two nits accepted & fixed: lanes stat semantics + ON_RESUME stats refresh.
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push. Pushed.

---

**CI compile fix for the bubble unfurl + Spin smart-layout system (low-density + short-screen compact tiers with vertical edge buttons, toggleable)**

### What was asked

“fix these also the compact mode doesnt trigger with dpi below 440 but also create a smart system where below 440 dpi it gets compact mode and also when the screen length is smaller according to the dimension it gets even smaller or maybe the category button goes to the left vertically and filters goes to the right vertically. add this as an option to turn off as well. not the smart system for dpi but the dimension. so each screen properly fits” — plus the pasted CI log: `ExploreBubbleContent.kt` fails to compile (2-param `onSizeChanged` lambda; `AnimatedContent(transitionState=…)` overload missing in the resolved animation version). ask_user: extra-small tier = **BOTH** (shrink the deck AND vertical edge buttons); toggle lives in the **Appearance card**.

### Root causes

1. **CI failure (blocking)** — the v6.12 bubble unfurl used `Modifier.onSizeChanged { w, h -> … }` (the modifier takes a single `(IntSize) -> Unit`) and `AnimatedContent(transitionState = transition, …)` — the `transitionState` overload doesn’t exist in the resolved animation version, so the whole call (and the `togetherWith`/`SizeTransform` inside its transitionSpec) failed to resolve.
2. **Compact mode never triggered on low density** — `SpinCompactThresholdHeight` keyed compact mode on screen HEIGHT only; a low-dpi device with a tall layout never compacted.
3. **No “even smaller” tier** — a single compact scale (0.88) was the only fallback, and the bottom bar always stayed a horizontal row.

### What was done

**CI fix — `ui/components/ExploreBubbleContent.kt`:** `onSizeChanged` now takes the single `IntSize` and forwards `size.width / size.height`; `AnimatedContent` switched to the `targetState = minimized` overload (the `updateTransition` still drives the corner morph + `isRunning` size-gate — both animate on the same `minimized` flip). **Follow-up (v6.12.1, second CI round):** the custom `transitionSpec` (slide + fade + `SizeTransform`) still didn’t resolve against the pinned **animation 1.11.2** API (`togetherWith` receiver mismatch — even though the identical pattern compiles in SpinScreen), so the bubble now uses the DEFAULT `AnimatedContent` transition — crossfade + built-in SizeTransform size spring — with the corner morph + window centering unchanged. Six now-unused imports removed.

**Spin smart layout — `SpinScreen.kt` (v7.2):**
- **Low-density rule (always on):** `densityDpi < 440` → compact layout no matter the height (`SpinLowDensityDpi`).
- **Dimension rule (toggleable):** new `AppPreferences.smartSpinLayoutState` (default ON) gates the height tiers: `< 680dp` compact (scrollable band + 0.88 deck), `< 600dp` extra-compact (`SpinExtraCompactThresholdHeight`) — deck scales 0.78 / carousel 350dp / tighter paddings.
- **Vertical edge buttons (extra-compact):** `BottomCta(vertical = true)` renders Categories left / Filter right as tall 54×112dp pills (`VerticalDeckButton`) pinned `CenterStart/CenterEnd`, leaving the middle clear.
- `SpinDeckSection` + `Carousel` gained `extraCompact` params (default false — normal layout byte-identical).

**Settings — `SettingsScreen.kt` + `AppPreferences.kt` + `CurioIcons.kt`:** Appearance card gained a “Smart Spin layout” Switch (AspectRatio glyph, `KEY_SMART_SPIN_LAYOUT`, seeded in `initThemeMode`, reactive state read by Spin).

### Validation

- `check_braces.py` BALANCED on all 5 touched files.
- Code-reviewer pass: clean — the CI fix addresses every error line in the pasted log; tier logic matches the spec exactly (extra-compact implies compact since 600 < 680; the toggle gates only the height rules); no missing imports / dead code. Two non-blocking nits accepted (state var `private set` consistency; fixed 112dp pill height fine at the threshold).
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push. Pushed.

---

**Peek-card reel fixed (direction, cut-off, speed) + “Done exploring” on the notification now opens the app at the write-it-down entry page with a HOME-anchored back stack**

### What was asked

“the peek card animation got better but now it looks cut off… the above peek cards should animate downwards not upwards. its also too fast and does not look that smooth. also when i tap done exploring on notification it just closes it and doesnt open the app with the entry page. and if i open the entry page and tap back it just exits.”

### Root causes

1. **Peek wipes sliced the content** — every peek card wiped UPWARD (in from below, out the top) as a full-height hard slot slide at 190/160ms, under a 200ms tick floor. Top peek titles (placed at the card TOP via `Arrangement.Top`) were the first thing sliced off the top edge — the “cut off” look — and a 190ms wipe barely finished before the next 200ms tick, reading fast + glitchy.
2. **“Done exploring” was dismiss-only** — the notification action fired `ExploreReminderReceiver.ACTION_STOP`, which cleared the session + stopped the service but never opened the app; and nothing anchored a back stack under a deep-opened entry page, so Back could exit the app.

### What was done

**`SpinScreen.kt` (v7.1):**
- **Directional wipes** — top peek cards now feed the deck from ABOVE (content DROPS down into the card, `dir = -1f`); bottom peeks rise up (`dir = +1f`). Idle re-fan (landing re-deal / category switch) uses the same per-side direction.
- **Soft glides, not hard cuts** — the full-height slot wipe is replaced by a partial-height glide (45% travel) + fade at 320/300ms (idle 300/280ms) with `SizeTransform(clip = false)`; hero content reel durations bumped 190/160 → 300/260ms to match.
- **Calmer cadence** — tick interval floor raised 200 → 340ms (`340 + 180·eased`, still easing to 520) so every wipe completes before the next tick lands; header change-log updated (v7.1 items 28–29) + new `PeekWipe*` constants.

**Notification → entry page (the “Done exploring” action now lands somewhere useful):**
- `navigation/CurioRoutes.kt` — new `PendingEntryOpen` out-of-band handoff (same pattern as `LightboxTarget`): extra-name constants, `capture()/take()/trigger`; new `bootGatePrefixes` set (SPLASH/ONBOARDING/CRASH).
- `infrastructure/ExploreReminderReceiver.kt` — `ACTION_STOP` now reads the session BEFORE clearing it and launches MainActivity with `FLAG_ACTIVITY_NEW_TASK|SINGLE_TOP|CLEAR_TOP` plus the topic’s category slug + name extras.
- `MainActivity.kt` — `PendingEntryOpen.capture(intent)` in onCreate (cold start) + a new `onNewIntent` override (warm start).
- `navigation/CurioNavHost.kt` — new `LaunchedEffect(currentRoute, PendingEntryOpen.trigger)`: returns WITHOUT consuming on null / boot-gate routes (re-runs when the splash lands on HOME), pops to HOME first when not already there, then navigates `captureFor(slug, name)` — so Back from the entry page returns HOME instead of exiting. The “Done — write about it” dialog navigation got the same HOME anchoring.

### Validation

- `check_braces.py` BALANCED on all 5 touched files; grep confirms zero stale refs and that the new extras/constants are referenced everywhere needed.
- Code-reviewer pass: (see review results — no blockers).
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push. Pushed.

---

## Previous Requests (COMPLETED)

**Bubble expand/collapse is now an animated unfurl (size + corner + position), and the clipped boxy shadow is gone — the pill shape was refined**

### What was asked

"Add a size/position transition so the bubble animates smoothly when it expands and collapses instead of resizing instantly also redesign the pill collapse shape i mean refine it as there a weird shadow boxy shadow around it and the look also feels bad."

### Root causes

1. **Instant resize** — `ExploreBubbleContent` hard-swapped between `MinimizedPill` and `ExpandedPanel` inside the Surface, so the WRAP_CONTENT overlay window snapped to the new size with zero motion; the pill/panel shape also swapped (RoundedCornerShape(50) percent capsule ⇄ 20dp card) with no morph.
2. **Boxy shadow** — `shadowElevation = 8.dp` on the root Surface renders the elevation shadow BEYOND the surface bounds, but the overlay window is exactly the surface size, so the window clipped the soft shadow into a hard, boxy edge around the pill.
3. **Position jump / off-screen** — the window is gravity TOP|START anchored at its top-left; expanding (pill ~54dp → panel ~150dp) grew right-down from the top-left and, near the screen edges, pushed the panel off-screen.

### What was done

**`ExploreBubbleContent.kt` (v6.12):**
- **Animated transition** — `updateTransition(minimized)` drives an `AnimatedContent(transitionState = transition)` with fade + vertical slide + `SizeTransform(clip = false)` (the size spring does the actual pill ⇄ panel resize smoothly), and `transition.animateDp` morphs the corner radius (24dp near-capsule pill ⇄ 18dp card) instead of hard-swapping the shape.
- **Refined pill** — `shadowElevation 8dp → 0dp` (kills the clipped boxy shadow; the crisp accent border carries the definition, alpha bumped 0.45 → 0.50); the collapsed pill is a slightly squarer near-capsule (24dp) that reads cleaner than the extreme 50%-percent capsule.
- **Gated size callback** — new `onSizeChanged: (wPx, hPx) -> Unit` (default no-op) forwarded from `Modifier.onSizeChanged` ONLY while `transition.isRunning`, so the per-second timer tick (a 1-2px width change) can never nudge the window.

**`ExploreSessionService.kt`:**
- **Center-anchored position compensation** — the new `onSizeChanged` callback moves the window by half the size delta per frame (x -= Δw/2, y -= Δh/2) so the bubble grows/shrinks around its center instead of anchored to the top-left, clamped to screen margins so the expanded panel never leaves the screen; deltas tracked via `bubbleLastW/H` (the view's own width can lag the window relayout a frame), applied via `view.post { updateViewLayout }` (never relayout mid-layout-pass), and cleared in `removeBubble()` (reviewer nit).

### Validation

- `check_braces.py` BALANCED on both files.
- API precedent verified: `togetherWith`/`SizeTransform`/`using` (ContentTransform member — no import needed) already proven in SpinScreen.kt; `updateTransition`/`animateDp`/`AnimatedContent(transitionState)`/IntSize destructuring all stable in the resolved BOM.
- Code-reviewer pass: compile-safe, center-anchored math correct for gravity TOP|START, timer-tick suppression sound; applied its one nit (clear bubbleLastW/H in removeBubble). Runtime note: WRAP_CONTENT overlay + per-frame size animation is a known-working pattern — verify on device for OEM-specific smoothness (fallback would be a fixed-size window + graphicsLayer scale).
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push. Pushed.

---

## Previous Requests (COMPLETED)

**Light-mode + Material gradient fixes, peek-card suggestions, small-screen Spin layout** — on-hue pastel wash enriched (0.32/0.85) so red/teal/sky pages and the detail hero blend read their true category color; Material style dropped the 40% dynamic-primary accent blend (canonical category colors on dynamic surfaces); Spin page gets a BoxWithConstraints compact layout for short screens; `app/PEEK_CARD_DESIGN_SUGGESTIONS.md` written (pending review). See the `fix: category gradients stay true…` commit.

**Spin shuffle reel fluidity + peek wipe + dice tumble** — fan dealt once per pool (no start glitch), peek wipes rise through the card window at full height (190/160ms, no fades), dice tumbles on a seamless loop and morphs into the resting die. See the `feat: fluid shuffle reel…` commit.

**Floating pill + explore notification trimmed to the essentials; expanded pill redesigned (no more circle look); topic slow-scrolls (marquee)**

### What was asked

"the floating pill in curio and its explore now notification should not show too much details like even the description. make it short and redesign the expanded floating pill as the current one looks like a circle when expanded so fix it, and make the floating pill topic animate so show the full topic slowly." — a refinement of existing bubble/notification behavior (no toggle question per root AGENTS.md).

### What was done

1. **Pill content is now short** — the second line dropped "· {verb} {target}" entirely (minimized: topic + "12:34" chronometer; expanded: topic + "12m 5s"). No descriptions anywhere on the bubble.
2. **Expanded pill redesigned (no more circle)** — the expanded state was a 50% capsule (`RoundedCornerShape(50)`) holding a chip + text + 4 icon buttons in one row, which read as a circle. It's now a **rounded card panel** (`RoundedCornerShape(20.dp)`): a header row (glyph chip + topic + elapsed + Minimize chevron) over a row of **labeled** pill buttons — Pause/Resume, Stop, Hide (icon + text — clearer than bare icons). Minimized stays a compact capsule.
3. **Topic slow-scrolls (marquee)** — new `MarqueeTopicText`: measures the single-line topic with `rememberTextMeasurer`, draws it at full measured width (via `requiredWidth` — escapes parent constraints) inside a `min(textWidth, cap)` clipped box (110dp pill / 180dp panel), and glides it left→right at ~42 px/s with holds at each end (ping-pong `Animatable`) when it overflows. Short topics sit still. The overflowing tail actually renders instead of being ellipsized.

**`ExploreSessionService.kt`** — notification text trimmed to the minimum:
- Live contentText: "12m 5s in" (was "12m 5s in · exploring X · ~30 min"); paused: "Paused · 12m 5s".
- `BigTextStyle` removed entirely (no "timing your explore", "Tap Done exploring…", "~N min recommended", category name) — the shade shows just the short line. Progress bar + Pause/Resume + Done exploring actions kept.
- Bubble-only notification contentText: "12m 5s in" (was "Floating timer active — 12m 5s in").

**`fastlane/.../20260803.txt`** — bubble + notification changelog lines updated to the short behavior.

### Validation

- `check_braces.py` BALANCED on both Kotlin files.
- Grep: zero stale `verb.lowercase()`/`targetName` refs in the bubble/notification paths (verb/target still drive the done-prompt + session model, untouched).
- Code-reviewer pass: compile-safe (TextMeasurer/Animatable/requiredWidth/graphicsLayer all stable in BOM 2026.05.01; requiredWidth lets the marquee text exceed its parent's constraints), marquee math + clip behavior verified, no dead imports.
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push. Pushed.

---

## Previous Requests (COMPLETED)

**Bubble UX rework: drag now works (Compose-level), minimized-by-default pill with expand/minimize, richer + audible live notification with progress bar**

### What was asked

"the floating overlay works but i cant reposition it and also add minimise and by default make it small too also the live notification doesnt work and rework on the notification view." ask_user: minimize + default-small **always-on** (no toggle); notification — **just want it richer**.

### Root causes

1. **Drag broken** — the drag listener was a View-level `setOnTouchListener` on the overlay's `ComposeView`. The composed child (`AndroidComposeView`) consumes every touch, so the ViewGroup's dispatch returns true at the child and the View's own `onTouchEvent` (and its listener) never fires. Dragging had to move into Compose.
2. **No minimize, always full-size** — the bubble only had one (expanded) shape; nothing smaller to collapse to.
3. **Live notification too quiet** — channel was `IMPORTANCE_LOW` (collapses into the silent section — easily missed), text only re-rendered on start/pause (progress/elapsed went stale on long runs), and the expanded view was minimal.

### What was done

**`ExploreBubbleContent.kt`** —
- **Drag moved into Compose**: the root `Surface` now has `Modifier.pointerInput(Unit) { detectDragGestures(...) }` reporting raw deltas via new `onDragBy(dx, dy)` and release via `onDragEnd`. Slop-gated and placed OUTER to the pill's clickable, so taps on pill/buttons still land while real drags reposition the window (local-coordinate deltas self-cancel against the window movement — the window tracks the finger exactly, no drift).
- **Minimized by default**: `var minimized by remember { mutableStateOf(true) }` — the bubble starts as a compact chip + topic + chronometer-style timer (`compactElapsed` → "12:34") + expand chevron; the whole pill is tappable to expand (conditional `Modifier.clickable`, applied only while minimized — no dead clickable semantics when expanded).
- **Expanded controls**: existing Pause/Resume + Stop, NEW **Minimize** button (KeyboardArrowDown) collapsing back to the pill, and Hide's icon changed KeyboardArrowDown → **Close** (X) so it reads as dismiss, not collapse.

**`ExploreSessionService.kt`** —
- Removed the dead View-level listener + drag fields + `MotionEvent`/`ViewConfiguration`/`hypot`/`View` imports. New `bubbleParams` field; window params are now created BEFORE the ComposeView so the content closures capture them; `onDragBy` moves the window (`runCatching` — never crash the START_STICKY service), `onDragEnd` → field-based `snapBubble()` (same edge-snap + clamp).
- **Richer live notification**: channel `IMPORTANCE_LOW → IMPORTANCE_DEFAULT` (actually seen/heard; `onlyAlertOnce` prevents nagging) + `PRIORITY_DEFAULT`, added a **progress bar** (`setProgress(durationMinutes, elapsedMinutes, false)`, clamped), richer `BigTextStyle` (category `displayName`, verb/target, "Elapsed: X of ~Y min recommended"; paused branch keeps frozen readout + frozen progress).
- **Periodic refresh**: a main-looper `Handler` tick (`NOTIFICATION_REFRESH_MS = 60s`) re-runs `render()` while a live notification is wanted — the progress bar creeps forward and the expanded text never goes stale (single chain via `removeCallbacks` before `postDelayed`; cleared in `onDestroy`; `render()` stops the service — and the loop — when nothing wants it).

### Validation

- `check_braces.py` BALANCED on both files; grep: zero stale `makeBubbleTouchListener`/`MotionEvent`/`ViewConfiguration`/`dragStartX`/`dragTouchX`/`dragging`/`hypot` refs.
- Code-reviewer pass (no blockers): gesture ordering verified (drag outer vs clickable inner — slop-gated drag wins in the Initial pass, taps land on the innermost clickable, nested buttons don't double-fire), drag-delta feedback cancels exactly (window tracks the finger), closure capture of `params` safe, `displayName` field confirmed, progress clamped, chronometer anchor preserved, channel re-create bumps importance on existing installs unless user-modified. Applied its 2 nits: conditional clickable (no dead semantics on expanded) + the periodic notification refresh so progress/text stay live.
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push. Pushed.

---

## Previous Requests (COMPLETED)

**Saved-entry detail polish: roomier text boxes, 'Rate quality' caption, lighter star card, mirrored (non-random) hero watermark**

### What was asked

"The text box feels cramped—give it more padding inside. Add subtle help text under the star rating ('Rate quality'). Star section background could be slightly lighter to match the palette better. Tighten the decorative icons—some feel randomly placed. do these in detail view"

### What was done (all in `EntryDetailScreen.kt`)

1. **Roomier text boxes** — every saved-entry `NotePaperCard` (SoundBite note, ReelNotes review + fallback, Marginalia journal, GalleryWall caption, FieldNotes observed/surprised/learn-next) bumped `contentPadding` 16/16 → 20/18 (8 cards). Ruled-line alignment is padding-anchored, so the shift is safe.
2. **'Rate quality' caption** — the saved star-rating card now wraps stars + a subtle `labelSmall` caption (`onSurfaceVariant` @ 0.75 alpha) in a Column (6dp spacing) — same help language as the capture editor's rating row.
3. **Lighter star card** — background is now `lightAccentTint(accent, 0.18, 0.93)` in light (a soft, barely-there whisper — lighter + less saturated than the other cards) and the palette-matched `categorySurface` mid-tone in dark/AMOLED where a pale tint would be invisible. New import `com.curio.app.ui.theme.lightAccentTint` (internal, same module).
4. **Tightened hero watermark** — `HeroSymbolScatter` reworked from 10 irregular slots to 5 MIRRORED pairs (left glyph at (-biasX, biasY) with -rotation, right at (+biasX, biasY) with +rotation, equal size/alpha; per-tier alphas 0.16/0.19/0.21/0.19/0.16). Reads as a deliberate symmetric frame instead of randomly placed icons. `HeroWatermarkSlot` → `HeroWatermarkPair` + `HeroWatermarkGlyph` helper.

### Validation

- `check_braces.py` BALANCED; grep confirms 8 cards at 20dp and zero stale `HeroWatermarkSlot` refs.
- Code-reviewer pass: compile-safe (import + `category.accent` public, `internal` helper legal across packages in-module), mirrored pairs keep every glyph clear of each other + centered column + top buttons (pairwise distances 83–145dp vs 46–53dp radius sums), dark/AMOLED rating path untouched. Applied its one nit: `forEachIndexed { i, pair -> }` idiom.
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push. Pushed.

---

**Mood-board watermark rework: expanded board is no longer sparse — interior ring fills the middle, denser counts keep the same density on big canvases**

### What was asked

"the moodboard glyph watermarks are even worse now very less and so much scattered that the middle part have none when expanded"

### Root cause

`buildMoodBoardPattern` (the distance-checked mood-board sampler) placed glyphs in ONE far-first greedy pass with a large centre exclusion (`exclFrac 0.225`) and a hard count cap of 16. On the expanded full-screen board (~384×832dp, area ratio 1.93) the perimeter filled to the 16 cap before the interior cells were ever reached, and the 0.225 exclusion left the middle band (~170×170dp) completely empty — "very few, scattered, no middle". The count cap also throttled the expanded count below the linear-area density the inline board got, so the expanded board thinned out.

### What was done

**`CurioWatermarkBackdrop.kt` / `buildMoodBoardPattern` — two-phase interior-first placement + denser tuning:**
- **Phase A — INTERIOR ring**: the band just outside the tiny core (`coreFrac 0.225→0.10`, only the exact middle stays clear) is seeded FIRST with small glyphs (26–33dp), quota `max(2, round(target×0.22))` — the middle of the collage is never bare. **Phase B — PERIMETER**: far cells first until the target. (A single far-first pass was the bug: it filled the edges before ever reaching the interior.)
- **Denser counts**: `(9 + nextInt(4)) × density`, density cap `2.6→3.0`, `coerceIn(9, 22)` (was 8..10 / cap 16) — the expanded board now keeps the same per-area density as the inline card instead of thinning out.
- **Sizes** 26..54dp (was 34..54) with a wider `((e-1)/5)` span so interior glyphs read smaller; `marginFrac 0.095`, `cellFrac 0.17`, `jitter 0.34`. Glyph size is ALSO capped at `2 × marginFrac × short` (reviewer nit) so the radius can never exceed the canvas margin — a hard in-bounds guarantee at any canvas width.
- **Alpha** base bumped 0.10→0.12 (dark) / 0.14→0.16 (light) so the denser collage (and the interior ring) reads.
- KDocs updated (interior ring + two-phase description, verification numbers).

### Validation

- JS simulation (deterministic xorshift32 model of the exact algorithm) over 40 seeds × 7 canvases (280×560 … 411×915 dp): min center-distance ratio ≥ 1.06 ALWAYS (no overlap, by construction via `clearsAll`), all glyphs in bounds (280×560 now passes thanks to the size cap), interior ring always places — avg 2 middle glyphs inline / 3.5–3.7 expanded. Old params for contrast: expanded avg 15–16 placed with 0 middle.
- `check_braces.py` BALANCED; grep confirms no stale `perimeterCount`/`exclFrac` refs (the rename `exclFrac→coreFrac` missed two lines in the first edit — caught + fixed before validation).
- Code-reviewer pass: two-phase logic sound (a Phase-A cell simply fails `clearsAll` against itself in Phase B — harmless), compile-safe (local fun captures, Int arithmetic), inline board stays reasonable (~9-10 + 2 small middle). Applied its nit: the size-vs-margin cap.
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push. Pushed.

---

## Previous Requests (COMPLETED)

**Light-mode gradient fix: teal / light-blue / red detail heroes no longer blend through foreign colors — the light wash is now a hue-preserving pastel of the accent**

### What was asked

"the red teal and light blue have gradient issue in light mode they show entirely some differnt blending gradient in detail screen"

### Root cause

The light-mode category page wash was `lerp(cream, lerp(accent, White, 0.30f), 0.14f)` — ~86% cream + 14% accent tint, and cream's warm hue (~38°) dominates the mix. Teal (hue ≈175°) and sky (≈201°) washes therefore landed in the GREEN zone (~82°/110°), and the detail hero's HSL glide (accent → wash) swung through green for teal/light-blue and through a yellow band for red before settling on the wash — the "entirely different blending gradient" in light mode. (Indigo/amber/coral sit near cream's hue, so they never showed it.)

### What was done

- **`CurioColors.kt`** — new `internal fun lightAccentTint(accent, saturation = 0.22f, lightness = 0.88f)`: builds the pastel from the accent's OWN hue via the existing private `toHsl`/`fromHsl`, so every shade stays on the accent's hue family.
- **`CategoryInk.kt`** — the 4 light-mode tint formulas (`categoryBackgroundWash`, `categorySurface`, `categoryChipSurface`, `categorySurfaceMoodBoard`) now use `lightAccentTint` instead of the cream blend; a shared private `lightSurfaceTint(accent)` (0.28f / 0.89f) keeps the three surface families in sync. Dark / AMOLED / Material paths untouched.
- The detail hero's final stop is `categoryBackgroundWash`, so both HSL-gradient endpoints now share the accent hue (dh = 0): deep teal → pale teal, sky → pale azure, red → rose. By root cause, Spin / Reveal / Cabinet / Save teal & sky pages also now wear a pale category tint instead of grey-green.

### Validation

- `check_braces.py` BALANCED on both files; grep confirms the old cream-blend pattern exists nowhere else (the mixed-deck wash uses an 0.85 pastel fraction — hue-dominant, untouched).
- Code-reviewer pass: compiles (internal same-package, no unused imports — `lerp`/`Color`/`background`/`base` still used by toggle-off + dark paths), HSL math on-hue; applied its nit by extracting the duplicated (0.28f, 0.89f) tuning into `lightSurfaceTint`.
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push. Pushed.

---

## Previous Requests (COMPLETED)

**Explore service start is now crash-proof at the choke point — synchronous start failures are logged and swallowed instead of killing the app**

### What was asked

"Add a try/catch around ExploreSessionService.start calls so a service failure can never kill the app again — log and continue gracefully instead."

### What was done

**`ExploreSessionService.kt` companion — single choke point.** `start()` and `sync()` now wrap `ContextCompat.startForegroundService(...)` (including `session.toJsonString()`) in `try { ... } catch (e: Exception) { Log.e(TAG, ...) }`. Because every caller goes through the companion (CurioNavHost ×2, ExploreBootReceiver, AppPreferences toggles ×2 + sync ×2, TopicRevealScreen ×3), one guarded spot protects all of them — no per-call-site try/catch needed. Added `android.util.Log` import + companion `const val TAG`. `catch (Exception)` (not Throwable) — won't swallow Errors/OOM. The comment explicitly scopes this to SYNCHRONOUS failures (background FGS start on Android 12+, SecurityException, dead context); the async constructor-crash path is covered by the earlier OverlayOwner fix + crash-loop guard. Nothing is lost on a swallowed failure: the session is persisted and the reminder scheduled BEFORE `start()` runs, and the done-prompt still fires on return.

### Validation

- `check_braces.py` BALANCED; grep confirms the only `startForegroundService` calls in the module are the two guarded ones (no bypass path).
- Code-reviewer pass: compiles (companion TAG referenced unqualified), catch(Exception) appropriate, `toJsonString()` inside the try, no caller relies on `start()` throwing, no double-catch. Applied its comment-scope nit.
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push. Pushed.

---

## Previous Requests (COMPLETED)

**Saved-entry detail: background watermark glyphs padded below the hero — no more "cut" glyphs at the gradient blend (hero card untouched)**

### What was asked

"In detail view saved entry add a padding for the background watermark glyph — for the background, not the hero card, just that — so it doesn't get between the blend and shows cut." ask_user: glyphs are **cut at the hero's color fade**, and the user wants them **all below the hero card**.

### Root cause

On the saved-entry page the background watermark (`CurioWatermarkBackdrop`) scatter uses fixed `BiasAlignment` slots across the FULL screen, while the 380dp hero banner sits on top. On the user's device (384×832dp) the "movie" and "casino" slots straddle the hero's bottom edge — their slivers poke out below the gradient blend and look chopped. (Hero card's own `HeroSymbolScatter` was fine — untouched.)

### What was done

- **`CurioWatermarkBackdrop.kt` — new `topClearance: Dp = 0.dp` param.** When > 0 the layout switches from the full-screen Box+BiasAlignment set to a **lower-band mode**: `BoxWithConstraints` + offset math places glyphs strictly inside the band below the clearance (`x = (bandW - size)·(1+biasX)/2`, `y = bandTop + (bandH - size)·(1+biasY)/2`) so none can cross the line or the screen edges on any screen. Glyph sizes derive from the band's short side (`coerceIn(36..88.dp)`) so the collage adapts and the hand-tuned 8-slot set stays non-overlapping (pairwise-verified on 384×432 and ~300dp bands; active category's glyph always present + boosted, `distinct()` keeps 7-8 entries safe against `slots[i % 8]`). Default `0.dp` → Home / Spin / Reveal / Cabinet pixel-identical (grep-verified all 5 callers).
- **`EntryDetailScreen.kt`** — backdrop call passes `topClearance = EntryDetailHeroClearance`; new file-level `EntryDetailHeroHeight = 380.dp` + `EntryDetailHeroClearance = height + 20.dp` pair, and the hero Box now uses `EntryDetailHeroHeight` too, so the coupling is explicit and a future hero-height change can't silently put glyphs back behind it.

### Validation

- `check_braces.py` BALANCED on both files.
- Code-reviewer pass: confirmed all APIs compile (Dp×Float, Dp.coerceIn, minOf(Dp,Dp) — already used by the mood-board pattern, BoxWithConstraintsScope import, offset{IntOffset}, LocalDensity), bounds math keeps glyphs inside the band for any bias ∈ [-1,1], zero overlaps on the target band (a ~3dp graze only on ≤300dp bands — imperceptible), else-branch byte-identical, no unused imports/params. Applied its 2 nits: named hero-height constants + KDoc now documents the lower-band mode.
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push. Pushed.

---

## Previous Requests (COMPLETED)

**Always-on crash-loop guard: repeated crashes flip "safe mode" — stops the explore service + reminder, routes to the crash-log screen, and restarts cleanly**

### What was asked

"Add a crash prevent which kills the task so i can see the logs in app and restart safely." ask_user: **always-on** guard (no Settings toggle), and safe mode should **stop the crash source and show logs**.

### Why it was needed

The existing `CurioCrashReporter` (persists crash log + pending flag → splash routes to the crash screen) had NO loop protection: when the crashing component kept getting re-armed (explore service re-started by NavHost restore / ON_RESUME / boot receiver / settings toggles), each relaunch died before the splash could render the crash screen — "keeps crashing, nothing shows up".

### What was done

- **`CurioCrashReporter.kt` — crash-loop guard (always-on).** Gap-based detection: crash timestamps persisted in `curio_crash_logs`; 3+ crashes within a 90s window → `safe_mode = true`. Old stamps decay out of the window, so a lone crash after a healthy stretch never trips it. On loop detection the uncaught handler ACTIVELY stops the explore service (`stopService`, runCatching) and cancels the explore reminder — killing the re-arm source so the loop ends. New `isSafeMode()` / `resetLoopGuard()` (clears pending + safe mode + timestamps, keeps history). **Reviewer catch applied:** the handler-path prefs writes (`persistCrash`, timestamps, safe_mode) switched from `.apply()` to `.commit()` — the process is killed right after and the 300ms sleep runs ON the crashing thread, so async apply() flushes were not guaranteed (a lost flag = the feature silently doing nothing).
- **`ExploreSessionService.kt` — single choke point.** Companion `start()` and `sync()` return early when `CurioCrashReporter.isSafeMode(context)` — covers every re-arm path (NavHost ×2, boot receiver, AppPreferences toggles, TopicRevealScreen, notification action intents).
- **`ExploreBootReceiver.kt`** — early return in safe mode (no background re-arms after reboot/app-update).
- **`SplashScreen.kt`** — routes to the crash screen when `hasPendingCrash || isSafeMode`.
- **`CurioCrashScreen.kt`** — new "Repeated crashes detected" banner (errorContainer) explaining that the explore timer/bubble/reminders were paused so the app could open. **Restart Curio**: in safe mode → `resetLoopGuard()` + relaunch MainActivity with `FLAG_ACTIVITY_CLEAR_TASK | NEW_TASK` (+ finishAffinity) — a true task-kill + clean restart; for a single crash → existing fast path (clear pending + navigate Home).

### Validation

- `check_braces.py` BALANCED on all 5 touched files.
- Code-reviewer pass: confirmed loop logic (no single-crash false positives, decay), choke point covers all callers, stopService-from-handler safe, CLEAR_TASK restart correct, navController still used (no unused param), imports all used/scoped (same-package service/reporter = no import needed). Applied its one real catch: `.commit()` for the dying-process writes.
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push. Pushed.

---

## Previous Requests (COMPLETED)

**Crash fix: tapping Explore crashed the whole app — overlay-owner init order (`Restarter must be created only during owner's initialization stage`)**

### What was asked

"Still crashing after i tap explore now it opens up the browser and after that the app wont open it keeps crashing and nothing even shows up in notification" + logcat (`FATAL EXCEPTION: main` — `Unable to create service com.curio.app.infrastructure.ExploreSessionService`, `IllegalStateException: Restarter must be created only during owner's initialization stage` at `ExploreSessionService$OverlayOwner.<init>`).

### Root cause

Regression from the "unified overlay owner" commit. `OverlayOwner`'s property initializers ran in this order: `LifecycleRegistry.createUnsafe(this).apply { currentState = RESUMED }` FIRST, then `SavedStateRegistryController.create(this).apply { performRestore(null) }`. `performRestore` → `performAttach` REQUIRES the owner's lifecycle to still be `INITIALIZED`; because the registry was already advanced to RESUMED, it threw. The owner is a property initializer on the Service, so the throw happened in the SERVICE CONSTRUCTOR → `handleCreateService` failed → the whole process died.

Why it matched the report exactly: (1) browser opened fine (the search intent fires before/independent of the service), (2) the app then "keeps crashing / won't open" because the session is persisted — every relaunch re-arms the service via the CurioNavHost startup `LaunchedEffect` + ON_RESUME observer + boot receiver + settings toggles, each re-triggering the constructor crash, and (3) "nothing shows up in notification" — the FGS notification is only promoted inside `onStartCommand` (`render()`), which never ran.

### What was done

**`ExploreSessionService.kt`** —
- **Ordering fix (THE fix):** `OverlayOwner` now declares `store` → `registry = LifecycleRegistry.createUnsafe(this)` (still INITIALIZED) → `controller = SavedStateRegistryController.create(this).apply { performRestore(null) }` → `init { registry.currentState = RESUMED }` — restore attaches while INITIALIZED, then the state advances. `registry` stays non-private (`val`, class itself is private) so `onDestroy()` can still move it to DESTROYED (reviewer caught that `private val` would have been a compile error — private nested-class members aren't visible to the enclosing class).
- **Hardening:** `overlayOwner` is now `by lazy { OverlayOwner() }` — a throw here can never take down service CREATION again (constructor crash = process death + crash loop); the owner is only created when the bubble actually shows.

### Validation

- `check_braces.py` BALANCED on the touched file.
- Code-reviewer pass: confirmed the ordering contract (performRestore needs INITIALIZED, advancing to RESUMED dispatches ON_CREATE→ON_RESUME normally), lazy safe (both call sites on main thread), no imports needed (`by lazy` stdlib), and caught + fixed the `private val registry` compile break.
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push. Pushed.

---

## Previous Requests (COMPLETED)

**Crash + permission fixes: explore bubble no longer crashes (overlay ViewTree owners), "Display over other apps" asked in setup & on Explore now, and newly saved mood boards no longer render blank**

### What was asked

1. "fix the crash it happens when i tap explore now and also it doesnt ask for display over apps permission in setup neither after i tap explore now"
2. "fix this as the moodboard isnt showing after i save it it shows me blank page not even the moodboard layout. but the old moodboards are visible" — commit + push both.

### Root causes

**Crash** — `java.lang.IllegalStateException: ViewTreeLifecycleOwner not found from ComposeView` (crash-loop, 7+ process restarts). The floating bubble is a `TYPE_APPLICATION_OVERLAY` window hosted by `ExploreSessionService`; `WindowManager.addView` attaches its ComposeView with no Activity behind it, so Compose finds no ViewTree lifecycle/ViewModelStore/SavedStateRegistry owners on attach and throws. Every bubble path (Explore now → grant → ON_RESUME continuation, NavHost restore, boot receiver, settings toggles) hit it.

**Overlay permission never asked** — onboarding only asked Notifications + Microphone; and the reveal-flow ask was gated behind a ONE-TIME `overlay_prompt_seen` pref, so after one show (or dev testing) it never re-appeared.

**Mood board blank after save** — `CaptureConverters.deserializeCaptureData` field-detection checked `map.containsKey("quotes") → Marginalia` BEFORE `map.containsKey("caption") → GalleryWall`. The mood board gained a `quotes` list, so NEW GalleryWall blobs serialize `"quotes":[]` and were decoded as Marginalia; the detail screen's `entry.captureData as? CaptureData.GalleryWall ?: return` then silently rendered an empty body ("blank page, not even the moodboard layout"). Old boards (no quotes key) fell through to `caption` → fine; every other format has an earlier discriminating key (durationSeconds / rating) → "others are fine".

### What was done

**1 — Bubble crash fix (`ExploreSessionService.kt`)** — service-owned ViewTree owners set on the bubble's ComposeView BEFORE `addView`: `overlayLifecycleOwner` (`LifecycleRegistry.createUnsafe`, held at RESUMED), `overlayViewModelStoreOwner` (`ViewModelStore()`), `overlaySavedStateRegistryOwner` (`SavedStateRegistryController`, `performRestore(null)`). All three are required (`getWindowRecomposer` throws on the missing lifecycle owner; `resolveComposeViewContext` on the other two). `onDestroy` moves the registry to DESTROYED. Fix lives in the service, so every bubble-start path is covered.

**2 — Onboarding asks for overlay (`OnboardingScreen.kt`)** — new "Display over other apps" PermissionCard (bubble_chart glyph) on the setup slide; "Allow" opens `ACTION_MANAGE_OVERLAY_PERMISSION` (special access — no runtime dialog on Android 10+); `overlayGranted` re-read from `Settings.canDrawOverlays` in the existing ON_RESUME observer (launcher callback intentionally empty — fires before the grant lands).

**3 — Explore now always asks (`TopicRevealScreen.kt`)** — removed the one-time `overlay_prompt_seen` gate: the "Floating explore bubble?" dialog now shows whenever the bubble toggle is ON and the permission is missing ("Not now" proceeds without it; the prompt returns next session). Dead pref + accessors removed from `AppPreferences.kt`.

**4 — Mood board fix (`CaptureEntity.kt`)** — `caption → GalleryWall` moved BEFORE `quotes → Marginalia` in `deserializeCaptureData` (with a comment explaining why). Stored JSON was always a valid GalleryWall blob, so already-broken saved boards self-heal on next read — no data migration. Also fixes OpenNotebook/Portfolio-nested boards and backup/restore (same shared deserializer).

**5 — Changelog** — `fastlane/.../20260803.txt` bubble line updated (overlay bubble + permission ask).

### Validation

- `check_braces.py` BALANCED on all 5 Kotlin files; grep confirms zero stale `isOverlayPromptSeen`/`overlay_prompt_seen` refs.
- Code-reviewer pass (2): crash fix is the complete standard pattern (all 3 owners before addView; `createUnsafe` correct for lifecycle 2.10 — constructor deprecated); detection-matrix walk confirms the reorder breaks nothing (Marginalia has no `caption`, SoundBite/ReelNotes checked earlier, `caption` unique to GalleryWall — `imageCount` would NOT have been safe since ReelNotes has it too); applied the nit (empty onboarding launcher callback).
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.

---

## Previous Requests (COMPLETED)

**CI fix: CurioTheme unresolved `isDark` (compile error)**

- The bubble-theme refactor moved `isDark` into `curioColorScheme()`, but `CurioTheme`'s `SideEffect` still referenced it → `Unresolved reference 'isDark'` at CurioTheme.kt:206-207.
- Fix: `val isDark = isCurioDarkTheme()` recomputed in `CurioTheme`'s composable scope (the SideEffect block is not a @Composable context). Quick fix, no review (per user).

Pushed; CI re-validates on push.

---

## Previous Requests (COMPLETED)

**Crash fix: Cabinet NPE on legacy entries — null-hardened CaptureData preview/content + detail renders (mood board now shows)**

### What was asked

"Fix this crash and also the mood board wasn't showing — is it because of this crash?" Crash report 2026-08-03: `java.lang.NullPointerException — Parameter specified as non-null is null: kotlin.text.StringsKt__StringsKt.isBlank` at `CaptureData.toPreview(CaptureData.kt:315)` → `CurioEntry.getBodyPreview` → `CurioTopicCard` in the Cabinet `LazyGrid`.

### Root cause

YES — the mood board not showing was this crash. `toPreview()` called `.isBlank()` / `.take()` / `.firstOrNull()` directly on capture fields; legacy entries decode missing Kotlin-default fields to NULL (Gson allocates via Unsafe, skipping constructor defaults — the app's repeatedly-documented failure mode). One bad card NPE'd while composing → the whole Cabinet grid (mood boards included) died. The crashing branch (Marginalia `journalText`) plus every other format branch had the same latent NPE.

### What was done

**1 — `CaptureData.kt`: `toPreview()` + `toFullContent()` fully null-hardened** — every String field via `.orEmpty()` / `isNullOrBlank()`, every List via `.orEmpty()`, quote-list elements null-checked in filters, OpenNotebook `subFormat?.name ?: "Wildcard"` + `subData?.` (safe-call on a non-null-typed property — benign "unnecessary safe call" warning only, no warnings-as-errors in the project), Portfolio `sections.orEmpty()`. Sibling helpers hardened the same way: `audioFilePaths`, `imageUrisAll`, `withImageUris` (OpenNotebook branch `subData?.let { copy(subData = it.withImageUris(remap)) } ?: this` — avoids a null-into-non-null copy mismatch), `notePaperStyle`.

**2 — `EntryDetailScreen.kt`: five render-site guards** — `data.title`/`data.note` (SoundBite), `data.reviewText` (ReelNotes), `data.journalText` (Marginalia), `data.caption` (**GalleryWall mood board**) all switched `.isNotBlank()` → `!.isNullOrBlank()`, so opening a legacy entry — mood board included — can't crash the detail page either.

### Validation

- `check_braces.py` BALANCED on both files; grep confirms no remaining unguarded `.isNotBlank()`/direct field access on those Gson fields in the touched paths (remaining hits are inside the now-guarded `Text(...)` bodies).
- Code-reviewer pass: clean — confirmed no type mismatches, no missing imports (orEmpty/isNullOrBlank are stdlib), the `subData?.let` form, and applied its one nit: `subFormat?.name ?: "Wildcard"` guard in the two OpenNotebook branches.
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.

---

## Previous Requests (COMPLETED)

**Star rating polish — roomier review text box, "Rate quality" help text, lighter palette-matched rating card, redesigned stars (gradient fill + hollow slots) in entry and saved detail**

### What was asked

"The text box feels cramped—give it more padding inside. Add subtle help text under the star rating ('Rate quality'). Star section background could be slightly lighter to match the palette better. And the star design can be more better in both entry and saved detail view."

### What was done

**1 — Star redesign (`CaptureFormatComponents.kt`, shared `FilledStar` → both views)** — filled stars now draw a vertical-gradient fill (18% white-lightened crown → base color; moderated so dark-mode pastel ink doesn't wash out) plus the crisp outline stroke; the inner radius was finessed 42% → 45% for a sleeker silhouette. Unfilled stars are now clean HOLLOW outlines (no fill), so a rating row reads filled-or-slot. `StarRating` (the capture picker) gained a bouncy per-star pop (`animateFloatAsState` 1.14×, `CurioMotion.Springs.Bouncy` — the same language as the mood chips) and the row spacing grew 4dp → 6dp. New imports: `Brush`, `lerp`.

**2 — Roomier review box (`ReelNotesFormat.kt`)** — the review field's note-paper content padding grew 16/14 → 20/16 (air off the paper edges + a touch more vertical; the ruled lines anchor to the top padding, so they stay aligned under the text lines).

**3 — "Rate quality" help text (`ReelNotesFormat.kt`)** — a subtle `bodySmall` caption under the star picker (onSurfaceVariant at 0.75 alpha).

**4 — Lighter rating card (`EntryDetailScreen.kt`)** — the saved Reel Notes rating section's background swapped from `themedAccent().copy(alpha = 0.08f)` (muddy / near-invisible in dark) to the palette-matched `category.categorySurface(surfaceContainerHigh)` (soft pastel in light, visible mid-tone in dark; degrades to plain surface when the tint toggle is off).

### Validation

- `check_braces.py` BALANCED on all 3 files; greps confirm the gradient/scale/help-text/surface swaps are in place.
- Code-reviewer pass: clean — confirmed the vararg `Brush.verticalGradient` overload (this BOM lacks the List overload), imports present/used, modifier order scale-before-clickable fine, ruled-line alignment invariant to top padding, TornPaperCard floors (16/14) don't conflict with 20/16. Applied its one nit: gradient crown lightening 22% → 18% so bright pastel stars don't wash out in dark/AMOLED.
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.

---

## Previous Requests (COMPLETED)

**Floating explore bubble — a system overlay that floats over OTHER apps (the browser), replacing the in-app pill**

### What was asked

"Yess thats what i actually want not inside my app" — the user wants a Messenger-style bubble that floats OVER other apps while exploring (not the in-app pill). ask_user: **Settings toggle (default ON)** for the bubble, and **no in-app pill fallback** when the permission is missing (notification only).

### What was done

**1 — Permission + manifest** — `SYSTEM_ALERT_WINDOW` ("Display over other apps", a special-access permission with no runtime dialog on Android 10+) added to the manifest. Flows to grant it: Settings → Notifications row opens `ACTION_MANAGE_OVERLAY_PERMISSION` (package URI, `StartActivityForResult`, applies on return); TopicRevealScreen shows a ONE-TIME prompt when starting a session with the bubble on and the permission missing (Allow → system settings → ON_RESUME continuation; "Not now" proceeds without).

**2 — Bubble window in `ExploreSessionService` (rewritten)** — unified `render()`: live notifications ON → full chronometer notification; bubble wanted (toggle ON + `canDrawOverlays` + not hidden) → adds a `TYPE_APPLICATION_OVERLAY` window hosting a `ComposeView`; only-bubble → quiet minimal FGS notification (Android mandates one); neither → service stops quietly. Bubble: Messenger-style drag via slop-gated View-level `OnTouchListener` (taps still hit the Compose buttons), snaps to the nearest horizontal edge, initial placement bottom-center; Pause/Resume/Stop/Hide buttons operate the store in-process and re-render. `windowManager.addView` guarded with `runCatching` (reviewer catch: an uncaught throw in a START_STICKY service would crash-loop). `onDestroy` removes the window.

**3 — Theme for the overlay** — extracted `curioColorScheme()` from `CurioTheme` so the bubble renders with the app palette (light/dark/AMOLED/Material) WITHOUT the Activity-window SideEffect (would crash on a service context).

**4 — Settings toggle** — "Floating explore bubble" row in Notifications: switch + subtitle (Off / floats over other apps / "Needs Display over other apps — tap to allow"); row tap opens the permission page when needed. `AppPreferences`: `overlay_bubble_enabled` (default true, reactive) + `overlay_prompt_seen` + `exploreServiceShouldRun()`; `setLiveNotificationsEnabled(false)` keeps the service when the bubble still wants it; `setOverlayBubbleEnabled` starts/stops accordingly.

**5 — In-app pill removed** — `ExploreSessionPill.kt` deleted; its visuals extracted to `ExploreBubbleContent.kt` (shared by the overlay). `CurioNavHost`: pill block gone, ON_RESUME restores the bubble when hidden + live notifications off + bubble enabled (and re-arms the service — also covers permissions granted mid-session), startup path re-arms too. Boot receiver uses `exploreServiceShouldRun`.

### Validation

- `check_braces.py` BALANCED on all 9 touched files; no `ExploreSessionPill`/`ACTION_HIDE_OVERLAY`/`bubbleParams` refs remain.
- Code-reviewer pass: caught + fixed (1) unguarded `addView` crash-loop risk (wrapped), (2) dead `ACTION_HIDE_OVERLAY` constant + write-only `bubbleParams` (removed), (3) rotation mid-dialog trade-off (documented comment), (4) unguarded settings intent (runCatching). Confirmed: `return@ExploreBubbleContent` label valid (matches existing `return@Surface`), `maximumWindowMetrics` API-30 guarded, `doOnLayout` from core-ktx, all FGS starts foreground/exempt (no background starts), render state machine coherent.
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.

---

## Previous Requests (COMPLETED)

**Fix: floating explore pill never renders + POST_NOTIFICATIONS never requested for the live-notification flow**

### What was asked

"The floating pill doesn't work, neither its permission, neither the live notification." — the explore-timer feature shipped as if dead: no pill, no permission prompt, no ongoing notification.

### Root causes found

1. **Pill deadlock (never renders).** `ExploreSessionPill` only composed its `Surface` when `offsetX/Y` were non-null, but those were only set after `onSizeChanged` measured the pill — composition gated on the offset, offset gated on measurement → the pill NEVER appeared. The full-screen Box existed but was empty, so nothing visibly happened when a session started.
2. **Permission never requested for the explore flow.** `POST_NOTIFICATIONS` was only asked at onboarding and via the daily-reminder toggle in Settings. The "Live explore notification" toggle enabled the pref directly, and starting a session started the FGS directly — on Android 13+ the FGS notification is invisible without the permission, so "live notification doesn't work" was the default outcome for anyone who skipped/denied onboarding.

### What was done

**1 — Pill placement rewritten (`ExploreSessionPill.kt`)** — the `Surface` is now ALWAYS composed; only its offset, `graphicsLayer` alpha (0 until placed, avoids a top-left flash) and `pointerInput` drag (attached only once placed) wait on `placed = offsetX != null && offsetY != null`. `onSizeChanged` now always fires → the initial-placement `LaunchedEffect` (keyed on `placed`) positions the pill bottom-center, 88dp clear of the nav bar. Same snap-to-edge/clamp behavior as before.

**2 — Settings live-notification toggle requests permission (`SettingsScreen.kt`)** — new pending-action launcher pattern: `pendingNotificationEnable` + `requestNotificationPermission(onGranted)`; the granted callback runs the EXACT action the user was performing, so a request from one toggle never silently enables another. `setLiveNotifications` (and the existing `setReminder`) route through it. Toggling "Live explore notification" ON now asks for `POST_NOTIFICATIONS` first (Android 13+).

**3 — Explore-now requests permission before the browser opens (`TopicRevealScreen.kt`)** — starting a session with live notifications ON and no permission stores `pendingNotificationSession` and launches the permission request, deferring the browser-open + Home navigation to the callback (`openExploreBrowserAndGoHome`). The callback starts the FGS while the activity is STILL FOREGROUND (reviewer catch: the original order fired the browser immediately after `launch()`, so the grant callback ran backgrounded → `ForegroundServiceStartNotAllowedException` crash on Android 12+). Denied/dismissed → the flow still proceeds (pill + reminder work without the notification). Added `hasNotificationPermission` helper (API < 33 = granted).

### Validation

- `check_braces.py` BALANCED on all 3 touched files.
- Code-reviewer pass (2 rounds): first round caught the background-FGS-start crash risk in the permission callback ordering → fixed with the continuation pattern; second round confirmed the restructure (no background start possible, early `return` correct, pending read at callback time, imports all used, deny/dismiss/permanently-denied all proceed without hanging). Minor accepted trade-off: `pendingNotificationSession` is plain `remember` — a rotation mid-dialog drops the continuation but the session is already persisted and the user just taps "Explore now" again (noted in a comment).
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.

---

## Previous Requests (COMPLETED)

**Floating explore-timer pill (draggable, theme-aware) + live-notification toggle + upgraded timer notification + pause support**

### What was asked

A new pill-shaped floating pill that shows a timer when exploring starts, with Pause, Stop and Hide options (hiding leaves the notification as the controller); a Settings option to enable live notifications (like Samsung/Google's live-updating ongoing notifications); make the notification better; make the pill theme-aware.

User chose (ask_user): **draggable anywhere** (snaps to edge, Messenger-style); **pause = timer only** (reminder still fires at original start + duration); **live-notification toggle controls the persistent notification** (ON default = chronometer with controls, OFF = only the end-of-session reminder); **keep the Home "Currently exploring" card** alongside the pill; and do a proper multi-level review (imports etc.) so it doesn't fail CI.

### What was done

**1 — Session pause + pill state (`ExploreSession.kt`)**
- `ExploreSession` gained `paused` / `pausedAtMillis` / `accumulatedPausedMillis` / `pillHidden` (all defaulted → legacy sessions decode cleanly); new `elapsedMillis(now)` helper banks paused time so elapsed freezes while paused and continues after resume. JSON round-trip handles `pausedAtMillis` null-vs-value.
- Store gained `pauseSession` / `resumeSession` / `setPillHidden` (reuse `startSession` so prefs + reactive state stay in sync).

**2 — Live-notification toggle (`AppPreferences.kt`, `SettingsScreen.kt`)**
- New `live_notifications_enabled` pref (default true) + reactive state; the setter starts the FGS for the active session when flipped ON and stops it when flipped OFF (session + reminder + pill unaffected).
- Settings → Notifications gained a "Live explore notification" row under Explore sessions (icon + subtitle + switch).

**3 — Better notification (`ExploreSessionService.kt`, rewritten)**
- Tinted with the topic's category accent (`accent.toArgb()`), `CATEGORY_PROGRESS`, `setOnlyAlertOnce`, BigTextStyle.
- **Pause/Resume** action (PendingIntent.getService → `ACTION_TOGGLE_PAUSE` flips the store and re-renders) + existing **Done exploring** action.
- Paused render: chronometer dropped, frozen "Paused · 12m 5s" text + Resume action. Running render: chronometer anchored at `startMillis + accumulatedPausedMillis` so it shows ACTIVE elapsed after pause/resume cycles.
- `ACTION_SYNC` re-renders from the persisted session (pill → notification in step); both branches + `sync()` guard on the live-notification toggle so a turned-off setting can never resurrect the notification.

**4 — Reminder wiring without the service**
- Reminder scheduling moved to session start (`TopicRevealScreen`) + `ExploreBootReceiver` always re-arms it, so it fires even when live notifications are OFF (no FGS to arm it); service start is now conditional on the toggle. Scheduling is idempotent (cancel-then-set, same request code).

**5 — Draggable floating pill (`ExploreSessionPill.kt`, new)**
- Hosted at the `CurioNavHost` root (Scaffold wrapped in a Box; pill drawn above all screens while a session is active and not hidden). Full-screen `BoxWithConstraints` with no pointer input → touches pass through to screens below; only the pill itself is interactive.
- Messenger-style drag (`detectDragGestures` + offset state) that snaps to the nearest horizontal edge and clamps between the status and nav bars; initial placement bottom-center, clear of the bottom nav bar (88dp first-margin per review).
- Shows category glyph chip, topic name (width-capped → ellipsizes), live elapsed, and **Pause/Resume · Stop · Hide** circular buttons. Theme-aware: `surfaceContainerHigh`/`onSurface`/`error` from MaterialTheme (light/dark/AMOLED/Material) + category accent/ink.
- **Hide** persists `pillHidden` on the session; ON_RESUME auto-restores the pill when it was hidden AND live notifications are off (otherwise there'd be no controller).

**6 — Pause-aware surfaces (`CurioNavHost.kt`, `HomeScreen.kt`)**
- Done-exploring dialog + Home `CurrentlyExploringCard` now use `session.elapsedMillis()` (frozen while paused) and show a "Paused at …" readout with a Pause icon; dialog's ticker re-keys on `paused`.

### Validation

- Multi-level review: code-reviewer pass caught (1) initial pill placement overlapping the bottom nav bar → 88dp first-placement margin; (2) unbounded pill width on long topic names → `widthIn(max = 150.dp)`. Confirmed: pill imports all used, `Surface(onClick)` stable in this M3 (already app-wide), PendingIntent codes (4201/4202/4204) don't collide with scheduler 4210 / receiver 4212-4213, reminder scheduling idempotent across service/reveal/boot, notification-pause stays in sync with the pill via reactive store state, full-screen Box doesn't block touches.
- Structural: brace/paren balance on all 9 touched files (the +1 paren in TopicRevealScreen is pre-existing, verified against HEAD). Import audit: only pre-existing unused imports remain in NavHost (Intent/PaddingValues, were unused in HEAD too). `mutableLongStateOf` matches the app's existing `mutableIntStateOf` (Compose state 1.5+).
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.

---

## Previous Requests (COMPLETED)

**Mood-board watermark pattern reworked — guaranteed no-overlap collage, weird middle glyph removed, edge-anchored drawing**

### What was asked

The mood-board watermark pattern still needs proper refinement; the middle glyph looks weird; sometimes glyphs still overlap.

### What was done

**`CurioWatermarkBackdrop.kt` / `buildMoodBoardPattern` — full rework of the scatter algorithm:**
- The old fixed-bias slot ring only LOOKED collision-free: slots crowd at the corners, the ±0.05 jitter let neighbours drift into each other, and the two centre slots sat close enough to collide on smaller canvases — the overlapping glyphs the user kept seeing.
- NEW jittered-grid, Poisson-disc-style sampler: positions are NORMALIZED canvas fractions; candidates are generated on a grid (cell = 0.185 × short side, jitter = 0.32 of the cell), sorted far-from-centre first, and accepted ONLY if they clear the centre exclusion (radius 0.225 × short side) AND stay a **radius-sum × 1.06 spacing margin** away from every already-placed glyph (checked in canvas dp, so the guarantee holds at any board size). Glyph size grows 34→54dp toward the edges and shrinks toward the middle, so the collage reads as a natural scatter that fades out where the tiles sit. Counts scale with canvas AREA (8-16 per perimeter, 360×460dp reference) so the inline card and the expanded full-screen board keep the same density.
- **Middle glyph REMOVED entirely** — the board's centre stays completely clear for the tiles (the old lone centre icon was the "weird middle glyph"). 1-2 ring glyphs were tried (validated in simulation) but any glyph near the middle still reads as an odd lone icon, so they're gone.
- **Draw-anchor fix** (reviewer catch): the placement math treats (xFrac, yFrac) as the glyph CENTRE, but `Modifier.offset` shifts the icon's top-left — so every glyph rendered down-right of its placement and the edge glyphs could clip past the board's right/bottom borders. The draw loop now backs off by half the glyph's pixel size so the icon is centred on its placement point.

### Validation

- EXACT-model JS simulation of the Kotlin algorithm (raw dp radii, no scaling, matching the draw loop): 40 seeds × 6 canvas sizes (300×420 … 430×900 dp) — min centre-distance ratio ≥ 1.06 ALWAYS (no overlap, no crowding), all glyphs fully in bounds, middle always clear, full requested count always places. Earlier variants (spiral, ring, loose grid) were rejected by the same simulator because they placed too few glyphs or allowed tight/overlapping ratios.
- Code-reviewer pass: found + fixed the top-left-vs-centre anchor mismatch (the simulator's bounds check assumed centre anchoring, so it couldn't catch the render shift) and removed a dead `placements` list left over from the mid-glyph version. All imports used (cos/sin gone, only fully-qualified `kotlin.math.hypot` remains), `remember(seed, accentByGlyph, …)` caching is correct since Kotlin Map equality is structural, negative entry-id seeds are safe for `Random(Int)`.
- Seeds are stable at both call sites (`remember(boardSeed, initialData)` in GalleryWallFormat, `remember(entry.id) { entry.id.hashCode() }` in EntryDetailScreen), so the pattern never re-scatters mid-edit.
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.

---

## Previous Requests (COMPLETED)

**Capture-flow fixes: back/exit never silently drops drafts, format-switch gains "Save and switch", mood board carries a mood, formatting icons readable in dark/AMOLED**

### What was asked

1. Going to write about a topic then pressing back just exits and whatever was written is gone.
2. Why doesn't the switch-format dialog show "Save and switch" — and why doesn't it even show up in some fields (the optional ones)?
3. Why isn't "How did it make you feel?" in the mood board?
4. The bold/italic icons, the color-change icon, and the Add quote option look bad in dark and AMOLED mode — change their color in those two modes for better visibility.

### What was done

**1 + 2 — draft-safe leave + format-switch (SaveCaptureScreen.kt, + the 4 format editors)**
- The leave dialog (system back + top-bar back) was gated on `canSave`, which only counts a format's PRIMARY field — optional-only drafts (a rating without a review, images without text, a caption-only mood board, a typed title without a recording…) emitted null data, so back just exited and the content vanished. NEW `hasAnyDraft` state (any section with `data != null || busy`) gates both back paths via a new `onDraftChange` callback; the switch-format confirmation and the take-remove confirmation now trigger on the same any-draft rule.
- The switch-format dialog gained **"Save and switch"** (primary): it snapshots the drafted take into a NEW take at the same position (data/seed/mood/canSave copied), then switches the original take to the new format — nothing is lost, the draft lives on as its own tab. "Switch and clear" (error-colored) and "Keep editing" (moved to the dismiss slot) remain.
- To close the rating-only / caption-only / title-only holes at the source, each format's `canSave` now also counts optional content: ReelNotes (+ `rating > 0 || imageUris.isNotEmpty()`), GalleryWall (+ `caption.isNotBlank()`), SoundBite (typed title/note/quotes now saveable even without a recording). Marginalia and FieldNotes already counted attachments.

**3 — mood board mood (CaptureData.kt, GalleryWallFormat.kt, EntryDetailScreen.kt, SaveCaptureScreen.kt)**
- `CaptureData.GalleryWall` gained `mood: JournalMood? = null` (Gson legacy-safe); `GalleryWallFormat` holds + emits it; the universal mood row is no longer hidden for GalleryWall (`moodCapable = true`); `moodOf()`/`withMood()` cover GalleryWall (incl. OpenNotebook recursion); `EntryMetaCard` reads the mood for GalleryWall directly and inside OpenNotebook.

**4 — toolbar visibility in dark/AMOLED (PaperPalette.kt, RichTextEditor.kt)**
- The paper-mode toolbar/controls used the fixed warm amber `paperAccent()` (#9A7B2F — a brown that vanished on the midnight page). NEW theme-aware `paperControlAccent()`: bright amber #E3B84F in dark/AMOLED, unchanged in light (the paper slips themselves stay cream in both themes).
- Toolbar buttons gained dark-aware alphas (border/icon/active-fill 0.45/0.75/0.18 light → 0.75/1.0/0.28 dark) threaded through FormatToolbar / SelectionFormatBar / StyleToggleButton / SizePickerButton / FormatToolButton; the selection bar border too. Removed the unused `paperAccent` import.

### Validation

- Code-reviewer pass: caught that canSave-based gaps remained (rating-only/images-only/caption-only/title-only) → fixed at the format level so every draft emits saveable data; verified the insert-at-activeIndex "Save and switch" keeps activeIndex on the original take, `key(current.id)` stays stable, withMood/moodOf exhaustive, paperControlAccent composable-safe, new toolbar params default to the old constants (untouched call sites compile). Applied its nit: "Keep editing" moved to the dialog's dismiss slot.
- Structural checks: all toolbar call sites consistent (named args), no orphaned imports/refs, GalleryWall constructor sites (TopicCatalog, CaptureEntity) use the new default.
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.

---

## Previous Requests (COMPLETED)

**Detail hero gradient now blends accent→wash in HSL space ("proper gradient blend") — fixes muddy RGB banding in light, dark AND AMOLED**

### What was asked

In dark mode and AMOLED the detail-view blending still looked bad — and in light mode too. Use a proper gradient blend with the 2 colors (the accent and the page wash).

### What was done

- **`CurioColors.kt`** — `Hsl` / `toHsl` / `fromHsl` moved out of `object CurioMixedDeck` to file-level `private` (shared by the mixed-deck blends and the new helper). NEW `CurioGradients.hslGradientStops(from, to, steps)`: interpolates in HSL along the shortest hue path and returns `steps` evenly-spaced colors. Achromatic endpoints (pure black/white/grey — e.g. the AMOLED black wash) have no meaningful hue, so the path anchors on the chromatic endpoint's hue — a deep accent fades to black ON-HUE instead of swinging through foreign hues. Naive RGB `lerp` between a deep accent and a light/dark wash passes through muddy grey midtones — that grey band was the "bad blending" in every theme.
- **`EntryDetailScreen.kt`** — the hero banner's background is now `Brush.verticalGradient(colorStops = heroStops)` where the stops hold the vivid accent behind the icon/title zone (white-on-glass legibility) and then glide through 8 HSL-sampled colors to exactly `wash` at the bottom edge (seamless page merge): dark/AMOLED hold to 0.70 then glide 0.70→1.00; light hold to 0.88 (below the frosted bar, since cream brightening would wash out white text) then glide 0.88→1.00. First glide sample skipped (`drop(1)`) so stop positions stay strictly increasing (no duplicate-position stops for the shader). Removed the now-unused `lerp` import.

### Validation

- Node HSL-math over representative blends: LIGHT indigo→cream now passes through rich periwinkle/lavender (#3C32B6 → #6142C2 → #875DC3 → #A476C5 → … → #E6DFE3) instead of the RGB midpoint #9189CD (muddy grey-purple); DARK rose/amber/coral glide through deep on-hue tones; AMOLED sky/coral/rose fade to black on-hue with no purple detour.
- Code-reviewer pass: file-level helper move leaves `CurioMixedDeck`'s references resolving, object braces balanced, hue-anchoring correct, fraction math exact (0.70+8·0.30/8 = 1.00). Applied both reviewer nits: dropped the duplicate-position first glide sample, and switched to the `colorStops` List overload (no per-recomposition array allocation).
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.

---

## Previous Requests (COMPLETED)

**Home "Recents" merge, Profile gets an inline Theme card (recent activity removed), mixed-deck dark wash deepened**

### What was asked

1. Home: remove the "Recently unexplored" section; rename "Recently explored" to "Recents"; show the unexplored topics INSIDE Recents with a tag.
2. Profile: remove recent activity; add a theme change (style/mode) to the Profile page.
3. The mixed color background tints look bad in dark mode.

### What was done

- **`HomeScreen.kt`** — section 7b ("Recently unexplored") deleted; section 7 header renamed to "Recents"; unexplored topics now render inside Recents via the existing `ExploreTopicRow` with `tag = "Unexplored"` (tap still resumes the reveal), after the explored rows and before the Cabinet entries. Empty-state condition extended to cover the unexplored list too. Section doc comment updated.
- **`ProfileScreen.kt`** — removed `RecentActivityCard` / `RecentEntryRow` / `capturedLabel` / `recentEntries` (+ the `CurioEntry` import) and the "Recent" stat from `StatsStrip` (its data source was the activity list); SettingsCard subtitle now "Reminders · audio · backup". NEW inline `ThemeCard` (right below the Settings card): Curio/AMOLED/Material style segmented row + Light/Dark/System mode segmented row (disabled while AMOLED) + Category tint switch — mirrors Settings → Appearance, writes prefs immediately so `CurioTheme` recomposes app-wide; `themeMode`/`themeStyle` local state refreshed in the existing ON_RESUME observer. New imports: SegmentedButton(+Defaults)/SingleChoiceSegmentedButtonRow/Switch/alpha/ExperimentalMaterial3Api.
- **`ExploreSession.kt`** — `recordExplored` now also calls `removeUnexplored` when the topic was previously unexplored, so a resumed topic can never sit in BOTH lists (the merged Recents would otherwise show it twice: once "Resumed", once "Unexplored"). Call sites already did this; now it's guaranteed at the data layer.
- **`CurioColors.kt` / `CurioMixedDeck.mixedDeckWash`** — dark branch changed from `lerp(background, blend, 0.70f)` (a loud saturated banner, e.g. a vivid purple page) to `lerp(background, lerp(blend, Color.Black, 0.35f), 0.45f)` — a deep, muted jewel tone; each mix stays clearly distinct (deep plum vs maroon vs jade vs violet) while reading as a tasteful dark background. Light mode untouched.

### Validation

- Node color-math over all 15 pair + 20 triple curated blends: OLD dark wash was saturated mid-tones (#78249D, #891A15, #A71135, #7218BA…) → NEW is deep muted (#37164C, #3E1213, #4A0E21, #341158…), all distinct per mix.
- Code-reviewer pass: imports verified (no orphaned CurioEntry/TextOverflow/etc.), the double-brace Switch idiom matches Settings (CI-postmortem rule), ON_RESUME refresh correct. Reviewer's one real catch — the resume-duplicate — fixed at the data layer (see above).
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.

---

## Previous Requests (COMPLETED)

**Spin page Categories/Filter pills — icon+label centered in the box, label text bumped 14→16sp (text only)**

### What was asked

On the Spin page, the category and filter pill buttons: make the text centered along with its icon in its box, and make the text a little larger — the text only.

### What was done

- **`SpinScreen.kt` / `DeckControlButton`** (the Categories · Filter pill buttons in the bottom bar):
  - The icon + label `Row` was left-flush inside the pill (`Arrangement.spacedBy(10.dp)`). It now uses `Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)` — the icon/text pair stays 10dp apart but is CENTERED as one unit inside the pill box.
  - The label text grew 14sp → 16sp (`labelLarge.copy(fontSize = 16.sp, fontWeight = ExtraBold)`); the icon stays 24dp — text only, per the request. `sp`/`Alignment`/`Arrangement`/`FontWeight` imports were already present.

### Validation

- Targeted 2-line change in one component; verified in place via code search. No new imports, no API risk (`Arrangement.spacedBy(Dp, Alignment.Horizontal)` and `TextStyle.copy(fontSize = …)` are bedrock Compose).
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.

---

## Previous Requests (COMPLETED)

**Python 3 + pip installed in the IDX environment and persisted in .idx/dev.nix**

### What was asked

Install Python 3 (etc.) and add them to the IDX dev.nix.

### What was done

- **`.idx/dev.nix`** — added `pkgs.python3` + `pkgs.python3Packages.pip` to the `packages` list (channel stays `stable-24.05`), so a workspace rebuild installs them persistently. Syntax verified with `nix-instantiate --parse`.
- **Current environment** — installed the SAME channel attrs (`nix-env -iA nixpkgs_24_05.python3 nixpkgs_24_05.python3Packages.pip`) → **Python 3.11.10 + pip 24.0** landed in `~/.nix-profile/bin`, the exact version dev.nix resolves after a rebuild (no mismatch).
- **`~/.bashrc`** — appended `export PATH=$PATH:$HOME/.nix-profile/bin` (the user profile wasn't on PATH), so `python3`/`pip3` resolve in new shells immediately. (~/.bashrc lives outside the repo — not committed.)

### Validation

- Fresh interactive shell: `python3 --version` → Python 3.11.10; `pip3 --version` → pip 24.0.
- `nix-instantiate --parse .idx/dev.nix` → syntax OK.
- Committed & pushed (`.idx/dev.nix` is git-tracked).

---

## Previous Requests (COMPLETED)

**Dark/AMOLED detail-page hero gradient — smooth theme-aware fade instead of the bottom plunge**

### What was asked

In AMOLED mode or dark mode the color blending on the saved-entry detail page looks bad.

User chose (ask_user): smooth gradual fade — start the fade higher up the banner and spread it over the bottom third; light mode stays exactly as it is now.

### What was done

- **`EntryDetailScreen.kt`** — the saved-entry hero banner's gradient was a single 3-stop brush (`0.00 → heroStart`, `0.90 → heroStart 18% toward wash`, `1.00 → wash`). Because the 0.90 stop held the accent nearly flat, the ENTIRE color transition was compressed into the bottom 10% of the 380dp banner. In light mode that band brightens into the cream page (reads as a paper wash-out — fine); but in dark/AMOLED the wash is a near-black tint (pure black in AMOLED), so the band PLUNGED from a vivid accent into darkness — e.g. wildcard `#C56E80 → #2E1A2A`, music `#362F9F → #1B1F36`, AMOLED → `#000000` — the muddy band the user saw.
- The gradient is now theme-aware via `isCurioDarkTheme()`: dark/AMOLED uses 4 stops — `0.00 → heroStart`, `0.72 → heroStart` (keeps the vivid accent behind the icon + title), `0.90 → heroStart 50% toward wash`, `1.00 → wash` — gliding over the bottom third (steepest segment ≈ half of light mode's). Light mode keeps the original 3 stops pixel-identical. The final stop is still exactly `wash` by construction, so the hero-to-page seam stays invisible.

### Validation

- Node color-math check: sampled the gradient every 5% from 0.70–1.00 for all six families — the OLD brush held flat 0.70–0.90 then plunged; the NEW brush glides evenly (e.g. coral `#E68193 → #D7798A → #BD6A7C → #A45C6D → #8A4E5F → #5C3444 → #2E1A2A`).
- Code-reviewer pass below; `isCurioDarkTheme` / `lerp` / `Brush` imports were already present in the file.
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.

---

## Previous Requests (COMPLETED)

**Topic Reveal gains the watermark backdrop; background-level cards confirmed opaque**

### What was asked

Add the watermark pattern to the Topic Reveal screen too, and make its transparent elements sitting directly above the background opaque — NOT the hero card, just the background.

User clarified (ask_user): the teaser / action-prompt cards become fully solid; keep the tag chips as they are.

### What was done

- **`TopicRevealScreen.kt`** — the root `Column` (which owned the category wash background + verticalScroll) is now wrapped in a `Box` that keeps `cat.categoryBackgroundWash()`; `CurioWatermarkBackdrop(activeCat = cat)` sits as a FIXED sibling behind the scrollable `Column` (same pattern as Spin / EntryDetail), so the page's quiet glyph collage shows in the gaps around the content while the content scrolls over it. Added the `CurioWatermarkBackdrop` import.
- **Opaque cards**: the teaser card + action-prompt card already render on `category.categorySurface(...)` — a fully opaque color (lerp of opaque colors, no alpha) — so the watermark never bleeds through them; documented this contract in the new backdrop comment. Tag chips keep their 0.18f accent alpha per the user's clarification; the hero card (its own gradient + pills) is untouched per "not the hero card".

### Validation

- Brace/paren balance verified (87/87 `{}`, 266/266 `()`). Box closes before BackHandler; ConfettiBurst + AlertDialog remain outside the Box (confetti still overlays full-screen).
- Code-reviewer pass: wash correctly moved from Column → Box (no double background), verticalScroll kept on the inner Column so the backdrop stays fixed, cards' `categorySurface` confirmed opaque, imports resolve, no dead code.
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.

---

## Previous Requests (COMPLETED)

**Mood-board fixes: expanded view keeps its tint in AMOLED, watermark density matches inline, 1-2 center glyphs**

### What was asked

1. The expanded mood-board view in AMOLED mode loses its tint color — looks very dark.
2. The watermark pattern looks fine in the mood board's small view but scatters more when expanded — fix it.
3. There are very few / no icons around the middle — add just 1 or 2 (not a lot).

### What was done

- **`CurioWatermarkBackdrop.kt` / `CurioMoodBoardBackdrop`**:
  - Wrapped the pattern in `BoxWithConstraints` and now scale the PERIMETER glyph count by the canvas AREA vs a 360×460dp reference (`density = (area/refArea).coerceIn(1f, 2.6f)`, `count = (9..11 × density).roundToInt().coerceIn(9, 14)` — the honest cap is the 14 perimeter slots). The inline card (~1.0×) keeps its old 9-11 look; the expanded full-screen board (~1.7-2× area) now fills all 14 perimeter slots, so the pattern reads the SAME density instead of scattering sparse across the bigger canvas.
  - Added 2 quiet CENTER slots (`BiasAlignment(-0.12f, -0.05f)` / `(0.10f, 0.08f)`), of which 1-2 are always seeded (small 30..48dp glyphs, vs the perimeter's 46..84dp) — the board's middle is no longer bare, without crowding the tiles. Same seeded determinism, jitter, theme-aware alpha (0.10 dark / 0.14 light × boost) preserved.
- **`EntryDetailScreen.kt` / `ExpandedMoodBoardDialog`**: the full-screen dialog's background was `wash` (= `categoryBackgroundWash()`, which returns PLAIN BLACK in AMOLED because `tintWashEffective()` is false there) — that's why the expanded board lost its tint. It now takes `boardSurface` and is painted with `category.categorySurfaceMoodBoard()` — the same AMOLED-immune tinted surface the INLINE board already uses (gates on `tintWashEnabledState`, not `tintWashEffective()`), so the expanded board keeps its category tint on pure black exactly like the small card.

### Validation

- Brace/paren balance on both files (node script) — balanced.
- Code-reviewer pass: BoxWithConstraintsScope maxWidth/maxHeight are Dp (`.value` = Float), Dp-as-remember-key valid, Pair destructuring compiles, no stale `wash` refs in the dialog (remaining hits are the page background/hero gradient, which correctly keep `wash`), `categorySurfaceMoodBoard` import present, `categoryBackgroundWash` still used (no orphaned import). Applied its one nit: perimeter count cap corrected 20 → 14 (the true slot count; take() beyond list size silently caps — the 20 was unreachable).
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.

---

## Previous Requests (COMPLETED)

**Mixed-deck page wash now genuinely wears THE blend color — decks visibly repaint the page**

### What was asked

The mixed gradient's background tint is not different across mixes — it should be THE color the mix resolves to.

### What was done

- **`CurioColors.kt` / `CurioMixedDeck.mixedDeckWash`** — the wash was `lerp(background, lerp(blend, White, 0.30f), 0.32f)` light / `lerp(background, lerp(blend, White, 0.22f), 0.24f)` dark — i.e. ~70% of the final color was still the theme background, so every mix washed to nearly the same cream/midnight and decks looked identical. Now the blend DOMINATES:
  - Light: `lerp(background, lerp(blend, White, 0.40f), 0.85f)` — a strong pastel twin of the blend (e.g. indigo↔rose → soft lilac, amber↔sky → light mint), light enough for the dark maroon onSurface ink to keep reading.
  - Dark: `lerp(background, blend, 0.70f)` — the pure blend over midnight (deep plum, deep jade…), close to the literal blend color; blends are WCAG-AA vs white by design and midnight only darkens, so near-white ink stays crisp.
  - Still honors the Settings tint toggle (off → plain theme background) and AMOLED/Material styles (via `isCurioDarkTheme` + `MaterialTheme.colorScheme.background`).
- **`SpinScreen.kt`** — background comment under `.background(pageWash)` updated to describe the mixed deck's high-strength blended-color wash.

### Validation

- Brace/paren balance on both files (node script) — balanced.
- Code-reviewer pass: light-mode pastel keeps maroon ink readable (~#E2E7E7 luminance), dark-mode 70% blend keeps white ink AA-clear, AMOLED/Material/tint-off paths unchanged, no dead code/import issues. Two non-blocking watch-notes accepted as-is: peek cards (`categorySurface`, 24% tint in light) may sit closer in lightness to the now-stronger page — still distinct since they tint with the blend accent too; and light mode is a pastel twin rather than the literal blend (readability tradeoff, defensible).
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.

---

## Previous Requests (COMPLETED)

**Saved-entry hero gains a category-symbol watermark scatter (hero only, no overlap)**

### What was asked

Add a different watermark to the saved-entry detail hero — more symbols of that category (artists → instruments etc.), only in the hero top area (not the page background), with a proper non-transparent color blend and no symbol overlap.

### What was done

- **`CurioIcons.kt`** — new `CurioIcons.heroWatermarkSymbols(family)` returning exactly 10 Material Symbols OUTLINED ligature names per family (one per scatter slot): Music → music_note/library_music/headphones/mic/album/equalizer/piano/radio/audiotrack/queue_music; Movies → movie/videocam/theater_comedy/local_movies/movie_filter/play_circle/ondemand_video/video_library/theaters/smart_display; Books → menu_book/auto_stories/library_books/edit_note/book/format_quote/import_contacts/local_library/create/menu_open; Visual Art → brush/palette/colorize/photo_library/museum/photo_camera/wallpaper/architecture/photo/landscape; Science → science/biotech/lightbulb/functions/psychology/bubble_chart/explore/hub/online_prediction/genetics; Wildcard → casino/auto_awesome/explore/bolt/star/nightlight/public/spa/diamond/rocket_launch.
- **`EntryDetailScreen.kt`** — new private `BoxScope.HeroSymbolScatter(cat)` drawn INSIDE the hero banner Box (before the content column, so behind it): 10 fixed `BiasAlignment` slots around the perimeter (corners + edge midpoints), 42–60dp glyphs rotated −16..16°, solid WHITE at alternating 0.16/0.20 alpha (visible, never transparent), one glyph per slot. Slots verified non-overlapping and clear of the centered icon/title/frosted bar and the top back/more buttons; top-corner slots sit below the status-bar band. New `HeroWatermarkSlot` data class + imports (BoxScope, BiasAlignment, graphicsLayer). Page background's `CurioWatermarkBackdrop` untouched.

### Validation

- Brace/paren balance on both files (node script).
- Code-reviewer pass: slot-to-slot distances (~140–230dp vs ~47dp combined radii) and center-content clearance verified; imports all used; applied its 3 nits — expanded symbol sets 8→10 so no glyph repeats at the corners, swapped `imagesmode`/`border_color` for safer `photo_library`/`local_library`-style glyphs, nudged top corners below the status-bar band.
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.

---

## Previous Requests (COMPLETED)

**Removed 700 fake/placeholder topic entries — kept real ones; albums + artists untouched**

### What was asked

Except albums and artists, remove all other fake topic entries and keep the real ones — more will be added later.

### What was done

- Identified the fake entries: AI placeholder names matching the `{Adjective} {Noun} #{N}` pattern ("Hidden Scribe #1", "Forgotten Canvas #4", "Eternal Finding #9"…). Verified the pattern matches every `#`-named entry with ZERO unmatched, and that lookalike REAL titles (Unknown Pleasures, Superunknown, Eternal Sunshine (2004), Catch-22 (1961), Untitled (1991) by Gonzalez-Torres…) were NOT removed.
- Removed **700 placeholders** from the 9 non-music files (albums.json 448 + artists.json 304 fully kept): authors 76→130, books 89→149, directors 70→109, discoveries 83→124, painters 85→117, scientists 92→132, wildcard 78→154, films 79→120, artworks 48→56.
- Textual per-entry removal preserved the pretty-printed JSON byte-for-byte (split on `\n  {`, re-add newline on join) — final diff is pure deletions (13,300 lines removed, 0 insertions).

### Validation

- Node mirror of `validateTopics` (the Gradle gate): every file is a non-empty bare array, ids unique cross-file, categoryId matches filename, required fields + exploreAction fields present, instruction ≤ 450 chars (the real CI limit — scripts/validate_topics.py's 280 is stale), tier 1..3 when present — ALL OK before and after.
- `TopicCatalog.sampleEntries()` IDs (artist-bowie, album-ziggy…) are real named entries, not placeholders; no non-JSON code references placeholder IDs (grep 0 matches).
- NO local Gradle build (per AGENTS.md) — CI validates on push.

---

## Previous Requests (COMPLETED)

**Mixed-deck gradient rework — non-linear hero arrangements, screen wears THE blend color**

### What was asked

Work on the mixed gradient (Spin screen's multi-category deck): the linear vertical gradient looks bad — do something better; the screen's background tint should be THE one color the mixing results in; add more mixed combinations and support different gradient arrangements too.

### What was done

- **`CurioColors.kt` / `CurioMixedDeck`**:
  - `mixedDeckGradient` is now a lean duotone glide — each accent followed by the curated pair blend with its neighbor ([a, mid(a,b), b, mid(b,c), c, …]) instead of the old 5-stop-per-pair HSL-intermediate ribbon (9+ stops = rainbow banding). Cap raised take(3) → take(4), so up to 4 accents sweep (more mixing in one gradient). Removed the now-unused private `hslLerp`.
  - NEW `mixedDeckWash(blend)` — the page wash derived PURELY from the mix result (light: 32% of a 30%-lightened blend over cream; dark: 24% of a 22%-lightened blend over midnight) — markedly stronger than the single-category wash, honors the tint toggle/theme style.
  - NEW `mixedDeckHeroBrush(stops, wPx, hPx, seed)` — lays the stops out non-linearly: diagonal ↘ / diagonal ↗ / radial glow (center behind the watermark glyph), chosen by `((seed % 3) + 3) % 3`.
- **`SpinScreen.kt`**: `mixSeed = remember(activeCatIds) { activeCatIds.sorted().hashCode() }` (stable per deck composition); `pageWash = mixedDeckWash(deckAccent)` when mixed else `deckCat.categoryBackgroundWash()` — used for the screen background AND the `CurioNavTint` publish; `isMixed` + `mixSeed` threaded Carousel → HeroTicketCard, which builds `ticketBrush` at the card's pixel size (LocalDensity, 286×310dp) and replaces `Brush.verticalGradient`. Single decks keep the plain vertical theme-aware card gradient (no regression).

### Validation

- Brace/paren balance verified on both files (node script). No leftover `hslLerp`/`ticketGradient` refs.
- Code-reviewer pass: API signatures (linear/radialGradient colors+start/end+center/radius), radial radius covers the far corner (294.5dp > 244dp), declaration order (pageWash after isMixedDeck/deckCat/deckAccent — no forward refs), single-deck + AMOLED/Material tint-off paths unchanged, imports all used. Applied its feedback: wash fractions strengthened (0.24→0.32 light, 0.16→0.24 dark) and dropped the redundant `spinPageWash` alias.
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.

### Notes / follow-ups

- "More mixed combination" delivered in the SWEEP (4-accent cap + blend seams); the single blend color for 4+ decks still uses the runtime `hslCentroid` (a curated 4-accent table is a possible follow-up).

---

## Previous Requests (COMPLETED)

**Saved-entry hero blends seamlessly into the page — square edges, no fade scrim**

### What was asked

The saved detail (EntryDetail) screen's hero banner should blend better into the screen background, and it should NOT have rounded bottom edges (they looked weird).

### What was done

- **EntryDetailScreen.kt** — the hero banner:
  - Removed the `RoundedCornerShape(bottomStart/bottomEnd = 28dp)` clip → the banner is now a square, edge-to-edge header (no rounded card look).
  - Replaced the shared `CurioGradients.cardGradient(...)` (which ends ~70% accent, far from the page color) with a custom 3-stop vertical gradient whose FINAL stop is the page's exact category wash color: `0.0 → categoryCardFill(themedAccent)`, `0.90 → lerp(start, wash, 0.18)`, `1.0 → wash`. The wash-out only begins below the title + frosted-bar zone, so white-on-glass stays legible even for two-line titles; the seam is pixel-identical (invisible).
  - Deleted the old 36dp bottom fade overlay Box (it was clipped by the rounded corners — the corner notches were the "weird" look). The gradient itself now dissolves into the page.
  - Hoisted `val wash = cat.categoryBackgroundWash()` once (next to `cat`) and shared it between the page background and the hero's gradient end, so the seam matches by construction. Added the `androidx.compose.ui.graphics.lerp` import.

### Validation

- Brace/paren balance verified (node script; 295/295 `{}`, 962/962 `()`).
- Code-reviewer pass: `androidx.compose.ui.graphics.lerp` resolves, `categoryCardFill` is public, gradient stays rich (82% accent) through the 0.90 stop → frosted bar legible for 1- and 2-line titles in light/dark/AMOLED/Material; applied its DRY suggestion (shared `wash` val).
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.

---

## Previous Requests (COMPLETED)

**Universal 'How did it make you feel?' mood row — right above the capture options, animated**

### What was asked

The mood row was previously rendered inside each format editor (Marginalia, ReelNotes, SoundBite, FieldNotes), each with its own `var mood` state. The user wanted it **universal**: ONE shared row sitting right above the 'How do you want to capture this one?' format options, with proper animation when the mood changes.

### What was done

1. **Animated `MoodChipsRow`** (CaptureFormatComponents.kt) — selected chip now springs to 1.08x (`animateFloatAsState` + `CurioMotion.Springs.Bouncy` + `Modifier.scale`) and its fill + ink crossfade to the accent (`animateColorAsState` tween). Unselected chips stay at 1x and crossfade back.
2. **Removed the per-editor mood rows** from Marginalia, ReelNotes, SoundBite, FieldNotes — each editor's `MoodChipsRow` block deleted (plus now-unused `AppPreferences` imports in three of them).
3. **Hoisted mood to section state** in SaveCaptureScreen — `CaptureSectionState` gains `var mood`; one universal `MoodChipsRow` renders in `FormatBodyForCategory` above the 'How do you want to capture this one?' options, driven by the ACTIVE take's mood, and stamps the pick into the live data via the new `CaptureData.withMood()` helper (recurses into OpenNotebook's subData) on every change + editor emit. `moodOf` reads it back for seeding.
4. **GalleryWall gate** — mood-board takes carry no mood field, so the row hides for them (and for OpenNotebook-wrapped mood boards); otherwise a pick would animate and silently vanish. The row shows only when the 'Entry date & mood' setting is on.

### Reviewer feedback addressed

- Silent mood drop on GalleryWall → row hidden for mood-incapable takes.
- Dead editor mood state kept intentionally (minimal diff; the section stamp always wins).
- Stamping loop risk checked — one-way assignment, no re-entrancy; `key(current.id)` dispatch means mood changes don't recreate editors.

### Files touched

- `app/src/main/java/com/curio/app/features/capture/SaveCaptureScreen.kt` (hoist + universal row + stamping + gate)
- `app/src/main/java/com/curio/app/features/capture/formats/CaptureFormatComponents.kt` (animation)
- `app/src/main/java/com/curio/app/features/capture/formats/MarginaliaFormat.kt`, `ReelNotesFormat.kt`, `SoundBiteFormat.kt`, `FieldNotesFormat.kt` (row removal, import cleanup)

---

## Previous Requests (COMPLETED)

**Dark/AMOLED visual fixes — creamy icons, invisible format-chip text, mood row moved above**

### What was asked

1. The "creamy" icons (meta-card icons tinted CoralBlush, waveform unplayed bars using the 20% tint wash) look bad in dark mode and AMOLED.
2. In AMOLED the texts of Voice/Review/Journal (the capture format chips) weren't visible.
3. Place the "How did it make you feel?" (mood) row ABOVE the entry content.

### What was done

- **SaveCaptureScreen.kt format chips** — the ACTIVE chip's icon+text used `category.themedAccent()` (deep accent) as their color while the chip background was ALSO `themedAccent()` when the tint wash was off (AMOLED/Material) → deep-accent text on a deep-accent chip = invisible. Now: wash on → `category.categoryInk()` (theme-aware), wash off → `Color.White`. The "+ Add take" button also switched `category.accent` → `category.categoryInk()` when the wash is on (was deep-on-murk in dark).
- **EntryDetailScreen.kt** — (a) `AudioPlayerBar` gained a `playedAccent` param; in dark mode both the played and unplayed waveform bars now use the pastel ink twin (`category.categoryInk()`) so the progress split reads consistently instead of a deep accent inverting the readout against the now-light unplayed bars. (b) `EntryMetaCard` `MetaSegment` icons: `primary` (CoralBlush — a cream pastel that reads washed-out on the dark meta card) → `onSurfaceVariant` in dark, `primary` in light. Added `isCurioDarkTheme` import.
- **MoodChipsRow moved ABOVE** — in Marginalia (right under the journal page), ReelNotes (under the review field), SoundBite (under the note field) it now sits ABOVE QuoteCardsSection; in FieldNotes it sits above the photo attach. The old trailing FieldNotes mood block was removed (no duplicate).

### Validation
- Braces/parens balanced on all 6 edited files; code-reviewer pass confirmed the four theme combos (light+wash, dark+wash, AMOLED, Material) all readable; reviewer's readout-inversion nit addressed via the new `playedAccent` param.

**Migrate LocalLifecycleOwner off the deprecated compose-ui import**

- OnboardingScreen.kt + CurioNavHost.kt now import `androidx.lifecycle.compose.LocalLifecycleOwner` (the lifecycle-runtime-compose 2.10.0 dependency was already present; SettingsScreen/ProfileScreen already used it) — clears both `LocalLifecycleOwner is deprecated` lint warnings. Pure import swap, no behavior change.

Pushed; CI re-validates on push.

## Latest Request (COMPLETED)

**CI fix: lint NewApi error in CurioTheme**

- `dynamicDarkColorScheme`/`dynamicLightColorScheme` (API 31) were called unconditionally at CurioTheme.kt:172 — lint's 2 errors (one per call) aborted the build (minSdk 26).
- Guarded with `Build.VERSION.SDK_INT >= S`, falling back to the Curio palettes below API 31.
- Non-blocking deprecation warnings (LocalLifecycleOwner in Onboarding/NavHost) left as-is.

Pushed; CI re-validates on push.

## Latest Request (COMPLETED)

**CI fix: HomeScreen.kt compile errors**

- `unexploredTopics` (and `exploredTopics`) were declared inside the "Recently explored" section's Column but the "Recently unexplored" section referenced them outside that scope — hoisted both declarations to the outer scrollable Column (shared scope for sections 7 + 7b).
- `ExploreTopicRow` called `CurioForwardArrow(label = …)` but the component's parameter is `contentDescription` — renamed the named arg.

Pushed; CI re-validates on push.

## Latest Request (COMPLETED)

**'Currently exploring' card on Home (live elapsed time + Done / Keep exploring)**

- HomeScreen: when `ExploreSessionStore.activeSessionState` is non-null, a new `CurrentlyExploringCard` renders between the stats strip and Categories — accent-bordered card with Timer glyph, "Currently exploring" label, topic name, and a live elapsed line ticking every second (remember(startMillis) + LaunchedEffect 1s loop, auto-cancelled on dismiss).
- Two actions: **Done — write about it** (accent Button) tears down the session (clear + cancel reminder + stop service) and navigates to capture; **Keep exploring** (OutlinedButton) re-opens the Google search. The card disappears reactively the moment the session ends.
- The recommendation line switches phrasing once elapsed passes the mark ("…past the ~45 min mark" instead of "…~45 min recommended").
- `formatElapsed` moved from CurioNavHost's private copy to a shared public helper in ExploreSession.kt (NavHost now imports it) so the dialog and card format identically.

### Review

Reviewer pass (clean; applied the past-the-mark phrasing polish). CI validates on push.

## Latest Request (COMPLETED)

**Done-exploring dialog now shows live elapsed explore time**

- CurioNavHost: the "Done exploring <topic>?" dialog gained a Timer-icon line — "You've been exploring for 12m 5s" — computed from ExploreSession.startMillis, ticking live every second via a remember(startMillis) state + LaunchedEffect while(true) loop (auto-cancelled on dismiss; survives process restarts since it's derived from the persisted start).
- New private top-level `formatElapsed(millis)` helper: "34s" / "12m 5s" / "1h 24m"; coerceAtLeast(0) guards backwards clock.
- Reviewer: clean pass.

## Latest Request (COMPLETED)

**Setup revamp — onboarding now includes a permissions step**

### What was asked

Revamp the first-run setup “with permissions etc”. Previously onboarding was 3 informational slides with ZERO permission requests (mic was requested on first record tap; notifications only in Settings).

### Decisions (from ask_user)

- One setup slide with both toggles (Notifications + Microphone), each with its own Allow button / Granted badge.
- Ask in setup whether to turn the daily shuffle reminder on (not auto-on).

### Implementation (OnboardingScreen.kt)

- Pager grows to 4 pages; the final page is an interactive Setup slide: “Make Curio yours” with a Notifications permission card + Microphone permission card (Allow button → system prompt, “Granted ✓” badge once granted), and a “Daily shuffle reminder” Switch row that only appears once notifications are granted (toggling it calls AppPreferences.setReminderEnabled immediately).
- State: hasNotificationPermission (API<33 → true) / hasMicPermission via ContextCompat; RequestPermission launchers for each; DisposableEffect ON_RESUME observer re-reads grants when returning from system Settings.
- Page dots hidden on the last page (empty row keeps layout stable); setup content centered-when-fits, scrollable on small screens.
- Skip / Let's go both mark onboarding complete; reminder choice applied at toggle time.

### Review

Reviewer pass: added missing `material3.Surface` import (compile fix), fixed SetupSlide centering with verticalScroll (Box contentAlignment wrapper), kept dots-row height stable on the last page. CI validates on push.

## Latest Request (COMPLETED)

**Backup/restore now restores photo attachments (moodboard + all image attachments)**

### Bug

Backup & restore only bundled audio bytes; image attachments were exported as bare URI strings. Since photos come from a document picker (content:// with persisted permission, never copied to app storage), restored captures pointed at dead URIs — moodboard photos and other attachments didn't come back.

### Fix (backup format v3, mirrors the existing v2 audio bundling)

- `CaptureData.kt` — `imageUrisAll()` (recursive through OpenNotebook/Portfolio) + `withImageUris(remap)`; GalleryWall remaps BOTH flat `imageUris` and `tileLayouts[].uri` so the board renders from stored positions.
- `data/ImageStorageManager.kt` (new) — restores images to `filesDir/images/{entryId}/{n}.img`, per-entry delete, wipe-all; **path-traversal hardening** (rejects separators/dot segments from user-supplied backup + canonical-path containment check).
- `CurioBackupManager.kt` — `FORMAT_VERSION 2→3`; `BackupPayload.imageFiles: Map<String, ByteArray>` keyed by original URI (deduped); export reads each image via `contentResolver.openInputStream` (unreadable → skipped); restore wipes images dir, writes bundled photos, rewrites URIs to `file://` paths; legacy v1/v2 backups degrade gracefully (URIs unchanged); skips JSON rewrite when nothing was remapped.
- `EntryDetailScreen.kt` — entry delete also cleans the entry's restored image files.

### Review

Reviewer pass: fixed path-traversal in `restoreImage` + pointless re-serialization guard. CI validates on push.

## Latest Request (COMPLETED)

**Explore-system revamp: timed explore sessions with notifications, done-prompt, and recently-explored/unexplored tracking**

### What was asked

1. Tapping Explore on a topic reveal auto-records it as **recently explored** (even without a Cabinet save).
2. Exiting a topic without doing anything records it in a new **Recently unexplored** Home section.
3. The CTA opens a dialog with **Explore now / Write about it**.
4. **Explore now** → opens a Google search for the topic (with year, and artist for albums) → starts a **timer notification** (elapsed-time chronometer, not a countdown) → at the recommended duration a **reminder notification** pops (“done or not?”) naming the topic and what to do.
5. On returning to the app, it **asks if the user is done exploring**, then lets them write about it. The session must **not die** in the background (persisted + foreground service + boot restore).

### Decisions (from ask_user)

- **Settings toggle, default ON** (Settings → Notifications → “Explore sessions”). Off still opens the browser and records recently-explored, but no timer/reminder/done-prompt.
- **Recently unexplored = new Home section** (below Recently explored, “tap to resume”).
- Done-prompt on **every app return**, incl. mid-session and after background kill.

### Implementation

- `data/ExploreSession.kt` — `ExploreSession`/`ExploredTopic`/`UnexploredTopic` models + `ExploreSessionStore` (JSON-persisted active session + two reactive Home lists; serialization helpers `toJsonString()`/`parseExploreSession()`).
- `data/ExploreSearch.kt` — Google search URL builder (year from name/era tag; artist from album teaser; subtype disambiguator).
- `data/ExploreReminderScheduler.kt` + `infrastructure/ExploreReminderReceiver.kt` — AlarmManager reminder at start+duration; “Done exploring <topic>?” notification; ACTION_STOP tears the session down.
- `infrastructure/ExploreSessionService.kt` — foreground service (specialUse type), chronometer notification w/ topic + verb + target, “Done exploring” action; **self-heals on START_STICKY restart** via the persisted session.
- `infrastructure/ExploreBootReceiver.kt` — resumes session after boot/app-update/clock change.
- `Manifest` — FOREGROUND_SERVICE(+SPECIAL_USE) perms, service (`foregroundServiceType="specialUse"` + subtype property), receivers.
- `TopicRevealScreen.kt` — CTA records explored + opens dialog; Explore now starts session + opens browser + navigates Home; close/shuffle/back while not engaged records unexplored (BackHandler).
- `CurioNavHost.kt` — “Done exploring?” dialog on every ON_RESUME + startup restore (rememberSaveable-guarded, rotation-safe).
- `HomeScreen.kt` — recently-explored topic rows (write about it) + new Recently unexplored section (resume).
- `SettingsScreen.kt` — “Explore sessions” toggle row; **disabling mid-session tears down service/alarm/session**.
- `MainActivity.kt` — seeds `ExploreSessionStore` in onCreate.

### Review

Code reviewer pass: fixed FGS self-heal fallback, toggle-off teardown, and rotation-safe dialog state. CI validates on push.

## Latest Request (COMPLETED)

**Journal image attachments raised from 3 to 6 — saved view shows ALL images in a scrollable strip (single image still full-width)**

### What was asked

Allow attaching more than 3 images to a journal entry.

### What was changed

- **`MarginaliaFormat.kt`** — the journal editor's image cap raised 3 → 6 (matches Field Notes' existing 6-cap, so the app is consistent): `(imageUris + uris).take(3)` → `take(6)`, the "up to 3" label → "up to 6", the Add button guard `imageUris.size < 3` → `< 6`, and the state comment updated. The picker still persists URI permissions for every picked uri.
- **`EntryDetailScreen.kt`** (`MarginaliaRender`) — the saved journal's image row no longer silently drops images past 3 (`attachedUris.take(3)` in a `weight(1f)` row). It's now a horizontally scrollable strip (`horizontalScroll(rememberScrollState())`) rendering ALL attached images as fixed `150.dp × 120.dp` tiles (tap → Lightbox, unchanged); a single image goes FULL-WIDTH at `280.dp` height, mirroring the Reel Notes `singleImage` pattern so lone-image journals don't shrink (reviewer catch).

### Review

code-reviewer-deepseek-flash: clean pass after one applied fix — the reviewer flagged that the first version turned a single attached image into a small 150×120 tile (the old `weight(1f)` row showed a lone image full-width); fixed with the `singleImage` full-width branch matching Reel Notes. Noted as out-of-scope follow-up: FieldNotesRender has the SAME latent bug (its editor caps at 6 but the saved view still `take(3)`s) — the identical silent-drop the journal just fixed. Verified `Modifier.size(150.dp, 120.dp)` compiles, all imports already present, braces balanced.

### Follow-ups / notes

- Field Notes' saved view still caps displayed images at 3 (editor allows 6) — same silent-drop bug the journal just fixed; offered as a follow-up.
- Edit-mode restore reads `initialData?.imageUris.orEmpty()` with no re-cap, so entries saved with >6 images keep every image if the cap ever rises again.
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.


## Previous Requests

**Mood pickers in every format — Reel Notes, Field Notes, and Sound Bite join the journal, all behind the existing "Entry date & mood" toggle**

### What was asked

Add mood pickers to the other formats (Reel Notes, Field Notes, Sound Bite) so every entry can carry a mood.

User chose (ask_user): ride the existing "Entry date & mood" Settings toggle (default ON) — the same switch that already gates the journal's mood row + meta card.

### What was changed

- **`CaptureData.kt`** — `SoundBite`, `ReelNotes`, `FieldNotes` each gain `mood: JournalMood? = null` (trailing default; Gson legacy-safe — old entries decode to null → no mood).
- **`CaptureFormatComponents.kt`** — NEW shared `MoodChipsRow(mood, accent, onMoodChange)`: the journal's horizontally-scrollable mood chip row extracted into a reusable component (tap sets / tap again clears; selected chip fills the accent with white icon+text). Imports added: `horizontalScroll`, `rememberScrollState`, `JournalMood`, `glyph`, `CurioIcon` (deduped — the file already imported it).
- **`MarginaliaFormat.kt`** — the inline mood row is now a `MoodChipsRow(...)` call; removed the four orphaned imports (`horizontalScroll`, `rememberScrollState`, `glyph`, `JournalMood` — type inference makes the last one unneeded).
- **`ReelNotesFormat.kt`** — `mood` state seeded from `initialData?.mood`, added to the LaunchedEffect emit keys, emitted as `mood = mood`, and `MoodChipsRow` rendered between the quote cards and the image row, behind `if (AppPreferences.entryMetaEnabledState)`. `AppPreferences` import added.
- **`FieldNotesFormat.kt`** — same wiring; `MoodChipsRow` after the photo-attach row at the end of the column.
- **`SoundBiteFormat.kt`** — same wiring; `MoodChipsRow` after `QuoteCardsSection` at the end of the column.
- **`EntryDetailScreen.kt`** — `EntryMetaCard` mood extraction now covers all four formats directly plus `OpenNotebook` unwraps for all four sub-formats (was journal-only), so wildcard takes show their mood too.

### Review

code-reviewer-deepseek-flash: clean pass, no blockers. Notes: (1) SoundBite's mood row stays tappable while RECORDING (unlike the frozen title field/quote cards) — harmless because canSave requires STOPPED + file, and the row has no enabled param by design; (2) the mood board (GalleryWall) is now the only format without a mood — outside the explicit ask (user listed Reel Notes/Field Notes/Sound Bite), offered as a follow-up. Verified: `JournalMood` import removal is safe in MarginaliaFormat (type inferred from `initialData?.mood`), meta-card nested `when` exhaustive, `mood` is a stable emit key in all three formats, canSave semantics correctly exclude mood-only entries (mood is metadata riding on real content), CurioIcon deduped with no orphaned imports, braces balanced across all 7 files.

### Follow-ups / notes

- OpenNotebook (wildcard) automatically inherits mood through the sub-format editors + the meta-card unwrap — no extra wiring needed.
- If the mood board should carry a mood too, that's a clean follow-up (GalleryWallFormat gains the same state + row + field).
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.


## Previous Requests

**Size dropdown replaces the buggy A+/A− step buttons (icon stays lit while armed) + CI fix for MarginaliaFormat's missing padding import**

### What was asked

1. The text-size increase icon shows active even when it isn't (the A+/A− buttons lit from whatever size sat under the caret, not from any armed state).
2. Turn it into a feature: making the icon active shows a dropdown with sizes; the chosen size stays active so the next text typed is that size — same for the other icon.
3. Fix the CI failure: `Unresolved reference 'padding'` × 7 in MarginaliaFormat.kt (missing `androidx.compose.foundation.layout.padding` import after the Entry date & mood commit).

### What was changed

- **`MarginaliaFormat.kt`** — added the missing `import androidx.compose.foundation.layout.padding` (CI fix).
- **`RichTextEditor.kt`** — the A+/A− step buttons became a **size dropdown**:
  - `FONT_SIZE_STEP` / `applyFontSize` removed; `SIZE_OPTIONS` = 12..24sp in 2sp steps minus the 16sp base (computed once, top-level).
  - `applyExactSize(targetSp)` replaces `applySize(deltaSp)`: applies the EXACT picked size to the selection via `setSpanSize` (or `clearSpanSize` when the user picks "Default" = 16sp), and always arms `pendingSizeSp = targetSp` (null for Default) so the next text typed carries that size.
  - New `SizePickerButton` composable: `FormatToolButton` in a `Box` + `DropdownMenu` — a "Default · 16sp" item first, then `SIZE_OPTIONS` items with a `Check` glyph on the current size; picking calls `onPick(sp)`.
  - `FormatToolbar` + `SelectionFormatBar` signatures changed `sizeUpActive/sizeDownActive/onSizeUp/onSizeDown` → `sizeActive/currentSp/onSizePick`; all 3 call sites pass `sizeActive = pendingSizeSp != null` (the TRUE active state — armed only, fixing the false-lit bug), `currentSp = currentSizeSp()`, `onSizePick = { applyExactSize(it) }`.
  - New imports: `Box`, `DropdownMenu`, `DropdownMenuItem`, `HorizontalDivider` (FontWeight/sp/CurioIcons.Check already present).

### Review

code-reviewer-deepseek-flash: clean pass, no blockers. Notes accepted as intended: (1) picking "Default" with an active selection clears the selection's size spans (via `clearSpanSize`) in addition to un-arming — consistent with the other items which also apply to the selection; (2) A+ and A− are now functionally identical (both open the same dropdown) — exactly what the user asked ("same for the other icon"). Verified: all imports added, 0 stale refs to applySize/FONT_SIZE_STEP/sizeUpActive, no dead code, braces balanced, the `leadingIcon` lambdas (block-body `if` with expected `() -> Unit`) compile, DropdownMenu nests fine inside the SelectionFormatBar's non-focusable Popup (its own popup is focusable), the "5 buttons" width comment stays accurate.

### Follow-ups / notes

- The armed size persists until the user picks "Default · 16sp" or a different size — mirroring the bold/italic sticky model.
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.


## Previous Requests

**Topic like/dislike on the reveal screen feeding smart shuffle weighting + explored topics excluded from the spin**

### What was asked

1. Add like/dislike for topics on the Topic Reveal screen.
2. Use that data to show less of a disliked genre (without fully stopping it) and show more of a liked category.
3. If a topic is already explored (captured), don't show it again in shuffle.

User chose (ask_user): always-on (no Settings toggle).

### What was changed

- **`AppPreferences.kt`** — topic sentiment storage: `SENTIMENT_LIKE`/`SENTIMENT_DISLIKE`/`SENTIMENT_NONE` constants; JSON-object pref `topic_sentiments` keyed `"CATEGORY:topicId"` → "like"/"dislike" (PinnedTopic-style pattern); reactive `topicSentimentsState` seeded in `initThemeMode`; `topicSentiment(categoryId, topicId)` reactive lookup; `setTopicSentiment` (SENTIMENT_NONE removes the vote); `categoryAffinityMap()` = net likes − dislikes per category name.
- **`CurioIcons.kt`** — `ThumbUp`/`ThumbDown` glyphs (thumb_up / thumb_down).
- **`TopicRevealScreen.kt`** — new "Like / Dislike" row (section 6.5, between the action prompt and the CTA) with two circular `SentimentButton`s — active state fills with the category accent, tap again clears, votes write reactively so the buttons flip instantly.
- **`SpinScreen.kt`** — `pickFrom` now takes `exploredIds` + sentiment map + category affinity. It excludes recent AND already-explored topics (repo.getAll() called inside the LaunchedEffect, runCatching fallback to no-exclusion), falling back to the full pool only when everything is seen/explored so the shuffle never runs dry. Per-topic weight = tier base (100/60/20/30) × topic factor (liked 2.0, disliked 0.25 — never zero) × category factor (affinity > 0 → up to 2.5×, affinity < 0 → down to a 0.25× floor) — so a disliked genre shows less but is never fully blocked, and a liked genre surfaces more.

### Review

code-reviewer-deepseek-flash: clean pass, no blockers. Noted: (1) "genre" is weighted at the CATEGORY (family) level rather than the genre/era tags shown on the reveal — matches "make the liked category be more shown"; per-tag weighting is a possible follow-up, (2) the two sentiment toggle lambdas are mildly duplicated (acceptable), (3) only the actual pick is filtered/weighted — the peek fan (displayPool) can still show explored topics (stated boundary, "in shuffle" = the pick). Verified the suspend repo call inside LaunchedEffect, `Random.nextDouble(Double)` exists, JSON-iterator `buildMap` compiles, key format consistency, no dead code, braces balance.

### Follow-ups / notes

- Explored exclusion re-queries the repo on every spin, so a freshly captured topic is excluded immediately after saving.
- The explored fallback guarantees the shuffle can never run dry (single-candidate pools still resolve).
- If the user wants per-genre-tag weighting instead of per-category, that's a clean follow-up (affinity keyed by tag rather than category).
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.


## Previous Requests

**Saved quotes shelf (bookmark quote cards → Home "Saved" shelf with pinned topics) + paper style/color controls hidden behind a toggle in rich-text fields**

### What was asked

1. Add a feature to save/bookmark quotes after they are added.
2. Like the text-format toggle, add a toggle for the other (paper style + color) controls so fields don't look complicated for users who don't want to format or change the style.

User chose (ask_user): a saved-quotes shelf shown on the HOME screen together with the saved (pinned) topics — not Profile; always-on (no Settings toggle); the paper-style toggle applies to rich-text fields only.

### What was changed

- **`AppPreferences.kt`** — new `SavedQuote(entryId, topicName, categoryId, quoteText, savedAtMillis)` persisted as a JSON array (mirrors the `PinnedTopic` pattern), reactive `savedQuotesState` seeded in `initThemeMode`, plus `getSavedQuotes` / `saveQuote` (deduped by entry+quote, blank-guarded, newest first) / `removeSavedQuote`.
- **`EntryDetailScreen.kt`** — `RenderQuoteCards` gained `entryId` + `topicName` params (all 4 call sites — SoundBite/ReelNotes/Marginalia/GalleryWall — pass `entry.id` / `entry.topic.name`); each saved quote card shows a bookmark toggle (CircleShape Surface, `Bookmark`/`BookmarkBorder` glyph, accent-filled when saved) that calls `saveQuote`/`removeSavedQuote`; state reads the reactive `savedQuotesState` so the icon flips instantly.
- **`HomeScreen.kt`** — new **"Saved"** section between Categories and Recently explored (hidden when both empty): `SavedQuoteRow`s (category tint dot + FormatQuote glyph, 2-line ellipsis quote, "from {topicName}" caption, bookmark-border remove, tap → `entryDetail(entryId)`) and `PinnedTopicRow`s (bookmark glyph, topic name + category, unpin, tap → `revealFor(categoryId.routeSlug, topicName)` — same nav as Topic History). Doc comment section list updated (Saved = 6, renumbered 7/8).
- **`RichTextEditor.kt`** — paper style + color controls now sit behind a new `StyleToggleButton` (palette glyph, mirrors the FormatText button) driven by a `styleExpanded` state. MAIN mode: format tools stay visible, palette button right-aligned via a weight spacer, expanded chips/swatches render below. TOGGLE mode: palette button left + format button right (SpaceBetween), same expanded section. Quote cards + all paper rich-text fields inherit the decluttered look.

### Review

code-reviewer-deepseek-flash: clean pass with 2 findings applied — (1) **critical**: `Spacer(Modifier.weight(1f))` in the new MAIN-mode toolbar row needed the `androidx.compose.foundation.layout.Spacer` import (the file never used Spacer before — would have failed CI); (2) removed the dead `isQuoteSaved` prefs helper (the UI checks the reactive state inline). Nits applied: HomeScreen's top doc-comment section list updated for the new Saved section. Accepted as-is: PinnedTopicRow lightly duplicates TopicHistory's private PinnedRow (private scope), and a saved quote whose entry was deleted navigates to the existing missing-entry fallback.

### Follow-ups / notes

- Quotes are bookmarked per exact quote text + entry; removing the entry does not auto-prune saved quotes (they still navigate to the detail fallback).
- Always-on per user (no Settings toggle), so no experiment-closeout needed.
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.


## Previous Requests

**Entry date & mood — auto date/time display, journal mood picker, journal attachments (images + voice note), theme-aware meta card in saved entries (Settings toggle, default ON)**

### What was asked

1. When adding a journal or any entry, automatically note the date and time.
2. Add a mood picker to the journal, and attachments too.
3. In the saved entry, right below "Captured today" and above the entry, show the date and the mood (with icons), then the data and the type in a theme-aware card view, in 2 or 3 segments — and the same in other formats.
4. Show the time alongside "Captured today".

User chose (ask_user): 6 moods with icons; images + audio attachments; behind a Settings toggle, default ON.

### What was changed

- **`CaptureData.kt`** — new `JournalMood` enum (CALM/HAPPY/CURIOUS/INSPIRED/TIRED/OVERWHELMED with a `label`); `Marginalia` gains `mood: JournalMood?`, `imageUris: List<String>`, and the voice-note fields (`audioFilePath`, `audioDurationSeconds`, `audioFileSizeBytes`, `audioEncodingFormat`) — all Gson legacy-safe (null/empty for old entries); `audioFilePaths()` now recurses Marginalia so delete/backup clean up journal recordings too.
- **`CurioIcons.kt`** — 6 mood glyph constants (self_improvement / sentiment_satisfied / psychology / lightbulb / bedtime / mood_bad) + `JournalMood.glyph` extension (label lives on the enum) + `CalendarToday` for the meta card.
- **`AppPreferences.kt`** — new `entry_meta_enabled` pref (default true) with reactive `entryMetaEnabledState` + get/set, seeded in `initThemeMode`.
- **`SettingsScreen.kt`** — Appearance card gains an "Entry date & mood" switch row (icon + description + Switch bound to the new pref).
- **`MarginaliaFormat.kt`** — behind the toggle: a horizontally-scrollable 6-chip mood row (tap to set, tap again to clear; selected chip fills accent), an attach-images row (reuses `ImageThumb`/`AddImageButton`, OpenMultipleDocuments picker, up to 3, persistable URI permission like Reel Notes), and a compact `JournalVoiceNoteRow` (record → stop → keep/remove, discard while recording, runtime mic permission via launcher, `AudioRecorder` reuse, 1s timer). `canSave` includes attachments; LaunchedEffect keys + emit include mood/imageUris/audio fields (incl. `audioState` per review).
- **`EntryDetailScreen.kt`** — "Captured today" label appends the wall-clock time ("· 3:42 PM", SimpleDateFormat); new theme-aware `EntryMetaCard` below it and above the format body — equal-weight segments (icon over label): date & time | mood (journals only, unwraps OpenNotebook wildcard journals) | type (format `shortName` + existing `formatGlyph`, "Portfolio" for multi-section) with `VerticalDivider`s — 3 segments when a mood exists, 2 otherwise; plain theme surfaces (no category tint) so it stays neutral in Curio/AMOLED/Material. Both gated on the toggle (off = old label, no card). `MarginaliaRender` gained a `navController` param (single call site) and now renders journal attachments: images as tappable tiles → Lightbox route (same pattern as FieldNotes) + the voice note via the shared `AudioPlayerBar`.

### Review

code-reviewer-deepseek-flash: clean pass with 4 applied items — (1) removed dead `CurioIcons.Description` (the existing `ui.components.formatGlyph` is reused instead), (2) dropped the unused `hasPermission` param from `JournalVoiceNoteRow` (permission handling lives at the call site), (3) meta card now unwraps `OpenNotebook` sub-format so wildcard-journal moods surface too, (4) `audioState` added to the editor's LaunchedEffect keys. Verified Gson legacy handling (orEmpty guards at seed + render; positional `Marginalia("", emptyList())` fallback compiles via trailing defaults), enum-by-name serialization, `VerticalDivider`/`RowScope`/`glyph` imports, `AudioPlayerBar` param order matches SoundBiteRender, the settings Switch is the safe single-brace form, entry data is preserved even when the toggle is off, and braces balance across all 6 files.

### Follow-ups / notes

- `JournalMood` lives on the data enum (label) with glyph in `ui.theme` (extension) — no data→ui dependency.
- Audio cleanup on delete works via the new `audioFilePaths()` Marginalia branch (delete + backup flows already call it).
- Edge: a wildcard (OpenNotebook) journal's mood shows in the meta card but the in-editor mood row is the same shared Marginalia editor — consistent.
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.


## Previous Requests

**Agent rule reminder: a toggle decided at ask-time is NOT permanent — remove it per the experiment-closeout rule**

### What was asked

Add a reminder to the agent instructions: once a toggleable feature is decided, the toggle can be removed per the experiment-closeout rule.

### What was changed

- **`AGENTS.md` (root)** — the "🆕 NEW FEATURES — ASK THE USER: TOGGLEABLE OR NOT?" section gained a **Reminder** paragraph: a toggleable choice is NOT permanent — once the feature is decided/settled, REMOVE the toggle and hardcode the winning behavior (rule 3 of the 🧪 EXPERIMENTAL CHANGES section); a toggle decided at ask-time is a ship vehicle, not a permanent Settings fixture.
- **`app/AGENTS.md`** — the new-measures bullet in "Experimental features (A/B testing)" mirrors it: the toggle is NOT permanent — remove it and hardcode the winning behavior once the feature is decided (experiment-closeout rule).

### Review

Not applicable — small agent-instruction doc change (no code).

### Follow-ups / notes

- Agent instruction changes are committed and pushed immediately so every agent sees them (per root AGENTS.md).


## Previous Requests

**CI fix: confetti block in SaveCaptureScreen referenced `tintWash` out of scope**

### What was asked

CI failed: `SaveCaptureScreen.kt:482:53 Unresolved reference 'tintWash'` after the AMOLED/Material theme commit.

### What was changed

- **`SaveCaptureScreen.kt`** — root cause: the topic-strip `val tintWash = AppPreferences.tintWashEffective()` is a LOCAL declared inside the top-bar composable (line 280, scope ends ~477), but the confetti burst lives in the main `SaveCaptureScreen` body (line 482) and read the out-of-scope local. Fixed by calling `AppPreferences.tintWashEffective()` directly there, matching the file's other call sites (lines 645/757/768/774). Remaining bare `tintWash` refs are all inside the declaring function.

### Review

Trivial one-line fix; verified remaining refs are in-scope and braces balance.

### Follow-ups / notes

- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.


## Previous Requests

**Agent rule: when ADDING a new feature, ask the user whether it should be toggleable or always-on (refinements/fixes ship without the ask)**

### What was asked

Add to the agent instructions: when adding a new measure, ask the user if they want it toggleable or not — not for refinements or fixes, only for new things.

### What was changed

- **`AGENTS.md` (root)** — new section **"🆕 NEW FEATURES — ASK THE USER: TOGGLEABLE OR NOT?"** before the experimental section: whenever an agent is adding a NEW measure (a new feature/capability/behavior), ask the user via ask_user whether it should be toggleable (behind a user-facing Settings option) or always-on, and follow their answer. Explicitly scoped OUT: refinements and fixes of existing behavior ship as-is without the question.
- **`app/AGENTS.md`** — mirrored the rule in the app module's "Experimental features (A/B testing)" section as a new bullet (new measures → ask first; refinements/fixes → no ask).

### Review

Not applicable — small agent-instruction doc change (no code).

### Follow-ups / notes

- Agent instruction changes are committed and pushed immediately so every agent sees them (per root AGENTS.md).


## Previous Requests

**Note-paper polish: torn pages get the rigid-surface sheen, coffee stains and the folded corner redone to actually look good**

### What was asked

1. The torn pages don't have the rigid-surface effect in the background.
2. The coffee and folded note-paper styles look bad — fix them properly.

### What was changed

- **`PaperCard.kt`** — three fixes:
  - NEW shared `rigidCardSheen()` brush (White 0.10 → transparent → Black 0.06, slightly stronger than the old inline 0.08/0.05). `PaperCard` keeps it as the inner Box background; `TornPaperCard`'s Box background was REMOVED and the sheen is now drawn as the LAST `drawRect` inside its Canvas — ON TOP of the grain texture (and any torn ruled lines). Root cause: under the grain, the vertical light gradient was visually flattened, so torn slips looked flat while ruled pages showed the sheen. The sheen brush is hoisted into a `remember` in the torn composable so it isn't reallocated per frame (this file's per-frame history).
  - `drawCoffeeStains` rewritten from 5 near-invisible 7–12dp blotches at 5–11% alpha into real dried-cup rings: 4 main stains (14–24dp radius, seeded size fractions near edges/corners, writing area clean) each with a faint radial wet body, a classic darker rim ring (Stroke 2.0–3.6dp at 14–24% warm coffee brown 0xFF6B4226), a second fainter inner ring on alternating stains, and 3 satellite drip dots around alternating stains (1.2–2.6dp, canvas-bounded).
  - `drawFoldFlap` rewritten (param `size` → `canvasSize` per the naming rule): the flap is no longer a flat 7%-darker triangle (effectively invisible) — it now wears a `linearGradient` from lerp(surface, black, 0.20) at the crease to lerp(surface, black, 0.05) at the tip (the paper back catching light), a soft 2dp drop-shadow wedge mirrors it toward the page interior (alpha 0.10), and the crease gets a soft 2.6dp halo plus the crisp 1dp fold line (lerp(paperEdge, black, 0.32)). The call site is positional so the rename is safe.

### Review

code-reviewer-deepseek-flash: clean pass with one applied improvement — hoisted the torn-canvas sheen brush into `remember { rigidCardSheen() }` (the torn canvas redraws every frame and per-frame Brush allocation was an earlier lag source in this file). Verified `DrawScope.drawPath(path, brush)` / `drawCircle(brush)` overloads exist in this Compose version, `rigidCardSheen()` is not @Composable so it is safe inside draw lambdas, no unused imports (background/Brush/Stroke/lerp/cos/sin all still used), comment-aware paren check balances exactly (422/422), the fold shadow wedge geometry stays inside the card on the page side of the crease diagonal, and the torn canvas order (grain → rules → sheen) keeps the ruled cadence intact.

### Follow-ups / notes

- All six paper styles share the one component set, so the fixes apply in the editor AND the saved entry views (NotePaperCard dispatch) with no call-site changes.
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.


## Previous Requests

**AMOLED + Material theme styles — Settings → Appearance, with the default Curio theme untouched**

### What was asked

1. An **AMOLED** option that automatically switches to dark mode, turns off the category tint, and makes the background pure black.
2. A **Material** theme that also turns off the tint, and turns the category colors into a shade of the device's Material palette — not fully off; each category keeps its hue, blended into the color the material theme has according to the device.
3. Do not break the default theme in the process.

### What was changed

- **`AppPreferences.kt`** — new theme-style pref `theme_style` (constants THEME_STYLE_DEFAULT / THEME_STYLE_AMOLED / THEME_STYLE_MATERIAL) with reactive `themeStyleState` (seeded in `initThemeMode`), `getThemeStyle` / `setThemeStyle`, and `tintWashEffective()` = `tintWashEnabledState && themeStyleState == DEFAULT` — the single source of truth for whether washes are really on.
- **`CurioTheme.kt`** — new `CurioAmoledColorScheme` (darkColorScheme with pure-black background/surface and near-black container steps so OLED pixels switch fully off); `isCurioDarkTheme()` forces true in AMOLED; `CurioTheme` picks the scheme by style — AMOLED → amoled scheme, Material → `dynamicDarkColorScheme` / `dynamicLightColorScheme` (device Material You colors, still honoring Light/Dark/System), default → the existing warm palettes unchanged.
- **`CategoryInk.kt`** — new `@Composable CurioCategory.themedAccent()`: unchanged in Curio/AMOLED; in Material it lerps the accent 40% toward the device's dynamic primary (a shade of the category color with the material color); `categoryInk()` light branch uses it; all four wash helpers (`categoryBackgroundWash` / `categorySurface` / `categoryChipSurface` / `categoryBorder`) now gate on `tintWashEffective()`.
- **`CurioColors.kt`** — `cardGradient` ends on `MaterialTheme.colorScheme.background` instead of a hardcoded cream/black so cards echo the active surface (cream light / midnight dark / pure black AMOLED / dynamic Material). Default light mode unchanged (background IS SoftCream); default dark gradient end shifts from pure black to #0B1018 (near-identical, more correct).
- **Category-accent sweep** — every `category.accent`-driven fill/ink/gradient now reads `themedAccent()` so Material shades the category colors app-wide: SpinScreen (deck accents moved OUT of remember so the blend updates on style change), HomeScreen, CabinetScreen, ProfileScreen, EntryDetailScreen, SaveCaptureScreen (incl. the 6 format-constructor accents), TopicRevealScreen, CurioTopicCard, CurioCategoryCard, CurioCategoryChip, CurioHeroCard, CurioWatermarkBackdrop (both glyph maps de-remembered).
- **Tint-off sweep** — wash surfaces gated on `tintWashEffective()` with plain-theme fallbacks: GalleryWallFormat board (transparent), SaveCaptureScreen strip / save-button / gradient / format-chip-selected / Add-take (accent fill + white icon/text when off), EntryDetailScreen header pill / SoundBite card / ReelNotes null card / GalleryWall image strip (surfaceVariant / surfaceContainerHigh). SaveCapture's local tintWash reads the effective value.
- **`SettingsScreen.kt`** — new Theme style segmented row (Curio / AMOLED / Material) with a per-style description; the Light/Dark/System row dims (.alpha 0.4) and its buttons disable while AMOLED (always dark); the Category tint switch reflects `tintWashEffective()` and disables (null onCheckedChange) outside the Curio style — the stored toggle is preserved for when the user returns.

### Review

code-reviewer-deepseek-flash: clean pass with one critical fix applied — the Settings Category-tint Switch's `onCheckedChange = if (...) { {lambda} } else null` single-brace form parses the if-branch as a BLOCK (Kotlin grammar: a leading `{` commits to controlStructureBody = block), leaving `it` unresolved; fixed with the double-brace idiom so an inner lambda is the block value. Reviewer verified all themedAccent() call sites are composable-scope, `SegmentedButton(enabled = ...)` and the nullable Switch onCheckedChange exist in material3 1.5.0-alpha20, dynamicColorScheme imports resolve, no unused imports, and default-style equivalence (themedAccent → accent, tintWashEffective → raw toggle). Applied its second-round suggestion: a whole-tree `.accent` grep found CurioCategoryCard / CurioWatermarkBackdrop / CurioHeroCard / CurioCategoryChip / TopicRevealScreen reads the initial sweep missed — all now themed.

### Follow-ups / notes

- Known deliberate boundary: small decorative tint fills (selected category-chip container, confetti particles, topic-history / manage-categories dots, format-internal tint fills) keep their tint in AMOLED/Material — the same scope the existing Category tint toggle has always had (page washes + major surfaces).
- dynamicColorScheme on API < 31 falls back to a baseline palette instead of wallpaper colors (graceful, no crash).
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.


## Previous Requests

**Per-letter font-size tool (A+/A−) in the rich-text toolbar**

### What was asked

Add a font size tool that can increase individual letters — not just the whole field.

### What was changed

- **`CaptureData.kt`** — `TextSpan` gains `fontSizeSp: Float? = null` (plain Float, not TextUnit, so Gson serializes it cleanly; legacy entries omit it → null).
- **`CurioIcons.kt`** — new `TextIncrease` (`text_increase`) / `TextDecrease` (`text_decrease`) glyphs.
- **`RichTextEditor.kt`** — the A+/A− tools:
  - `buildRichAnnotated` renders `fontSize = sp.fontSizeSp?.sp`; `extractRichSpans` reads it back via `TextUnit.isSpecified`; `merged()` merges adjacent spans only when `fontSizeSp` also matches (so a sized span can't absorb a same-flag neighbor); the 3 positional `TextSpan()` calls in `rebaseSpans` now pass `sp.fontSizeSp`.
  - New `setSpanSize(spans, s, e, targetSp)` — splits every overlapping span, drops ONLY the size in the middle, adds a size-only span over the selection, re-merges (bold/italic/highlight spans are untouched and coexist with the size span). `applyFontSize` steps from the LARGEST size already in the selection (or the 16sp field default) by ±2sp, clamped 12–24sp (fits the paper's 24sp ruled line height).
  - `pendingSizeSp` armed target — tapping A+/A− with a collapsed caret arms a fixed size for the next typed chars; applying to a selection keeps the applied size armed; `emit()` inherits the size of the span under the caret and applies the armed size to inserted ranges (mirrors the B/I/highlight sticky model).
  - `applySize(deltaSp)` + `currentSizeSp()` power the toolbar: A+ is lit while the effective size > 16sp, A− while < 16sp.
  - `FormatToolbar` + `SelectionFormatBar` gained the two buttons (all 3 call sites); the floating selection bar widened 132dp → 180dp for 5 buttons.
  - `spansFullyCovered` now filters to flag-carrying spans so a size-only span overlapping a bold/italic/highlight span can't make the toolbar report the flag as missing (or trigger a redundant re-add).

### Review

code-reviewer-deepseek-flash: clean pass with two applied fixes — (1) `spansFullyCovered` filtered to flag-carrying spans (a size-only span stable-sorting before a flag span at the same start made `hasFlagAt` report styled text as unstyled and re-add the flag redundantly; size spans make overlapping heterogeneous spans common, and the same latent quirk existed for bold+highlight), (2) dropped the dead `mid.fontSizeSp != null` condition in `setSpanSize` (mid is explicitly copied with fontSizeSp = null). Verified `SpanStyle.fontSize`, `TextUnit.isSpecified`, `Float.sp` exist in the Compose version, Gson-safety of `Float?`, the split/merge roundtrip preserves bold/italic/highlight while resizing, all 3 toolbar call sites updated, and braces balance.

### Follow-ups / notes

- Saved entries render sized letters automatically (buildRichAnnotated is shared); the quote-card +1 span shift preserves size via copy().
- The armed size (like bold) persists until replaced — there's no dedicated "off" state; A− arms 2sp below the caret's size.
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.


## Previous Requests

**Bold/italic rendering, highlight-sticking, and a saved mood-board crash**

### What was asked

1. Bold / italic (etc.) formatting stopped rendering in the paper text fields.
2. Highlight (and bold) kept applying to newly typed text even after toggling it off, whenever the caret touched an existing highlight.
3. Crash: `NullPointerException: ... int java.util.List.size() ... on a null object reference` at `EntryDetailScreenKt.RenderQuoteCards` (EntryDetailScreen.kt:1149) ← `GalleryWallRender` (:1422) when opening a saved mood board saved before quote cards existed.

### What was changed

- **`CurioTypography.kt`** — `PatrickHandFontFamily` collapsed from four `Font()` entries (all pointing at the same regular Patrick Hand TTF) to a SINGLE regular entry. Root cause of #1: declaring Bold/Italic entries against one regular file made Compose's `FontMatcher` return an "exact" match for every weight/style request, so the mismatch that triggers `FontSynthesis.synthesizeTypeface` (fake-bold stroke / oblique) never fired — every style rendered as the regular face (the "bold/italic stopped working" regression from the Patrick Hand commit). With one entry the requests always mismatch and synthesis kicks in. Removed the now-unused `FontStyle` import.
- **`RichTextEditor.kt`** — `buildRichAnnotated` (shared by the editor AND the saved detail view) now sets `fontSynthesis = FontSynthesis.All` on every styled span, so the platform applies fake bold / oblique from the single-weight font instead of relying on the unreliable default.
- **`RichTextEditor.kt`** — `emit()`'s caret inheritance is now exclusive at the span end (`sp.start <= caret && caret < sp.end`): typing INSIDE a styled run (or at its start) still continues the style, but typing right AFTER it starts a fresh un-styled run — so toggling bold/highlight OFF actually stops it. Continuing after an explicit apply is still handled by the armed (sticky) pending flags.
- **`EntryDetailScreen.kt`** — crash fix: `RenderQuoteCards`' `quotes` argument is now `data.quotes.orEmpty()` at all 4 call sites (SoundBite / ReelNotes / Marginalia / GalleryWall), and the function itself guards `val safeQuotes = quotes.orEmpty()` — legacy Gson blobs decode missing Kotlin-default List fields to null, and a mood board saved before quote cards existed had no `quotes` field → `.size()` on null.

### Review

code-reviewer-deepseek-flash: clean pass. Verified `FontSynthesis.All` is a valid `SpanStyle` arg in Compose 1.11.x, the new import is used, `extractRichSpans` only reads fontWeight/fontStyle/background (the added span property can't break it), the single-entry family can't crash `FontMatcher` (closest-weight fallback, never empty), the exclusive-end inheritance is sound (a boundary between two styled runs still inherits the right-hand run via `sp.start <= caret`), and the call sites + internal guard cover the crash. Applied its one suggestion: the defensive `quotes.orEmpty()` inside `RenderQuoteCards` itself.

### Follow-ups / notes

- Existing saved entries with bold/italic spans will now render bold/italic again — their span data was always saved; only rendering was broken.
- Fake bold/oblique quality depends on the platform synthesizer; if the stroke reads too thin on some devices, bundling real bold/italic Patrick Hand files is the upgrade path (Google Fonts currently ships only the regular face).
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.


## Previous Requests

**CI fix: ReelNotes image-strip zoom block — BoxWithConstraintsScope is NOT a Density here, and maxWidth/maxHeight aren't reachable inside the Row lambda**

### What was asked

CI failed on EntryDetailScreen.kt (ReelNotesRender image strip, the c64fa044 zoom block): `Unresolved reference 'toPx'` at lines 961-964 (`maxWidth.toPx()`, `tileSize.toPx()`, `maxHeight.toPx()`) and `'val maxWidth: Dp' cannot be called in this context with an implicit receiver` at 975 (the `Modifier.size(if (singleImage) maxWidth else tileSize, ...)` inside the Row content lambda).

### What was changed

- **`EntryDetailScreen.kt`** — root causes: (1) this Compose version's `BoxWithConstraintsScope` is NOT a `Density`, so `Dp.toPx()` on `maxWidth`/`tileSize` can't resolve (my earlier comment's assumption was wrong); (2) inside the `Row { }` content lambda the implicit receiver is `RowScope`, so the outer scope's `maxWidth`/`maxHeight` are unreachable by implicit receiver. Fix: at the TOP of the `BoxWithConstraints` scope (where the scope IS the receiver) capture `val density = LocalDensity.current`, `val boxMaxWidth = maxWidth`, `val boxMaxHeight = maxHeight`; compute `tileW`/`tileH`/`viewW`/`viewH` with `with(density) { boxMaxWidth.toPx() }` etc.; and the Surface modifier uses the captured Dp vals — `Modifier.size(if (singleImage) boxMaxWidth else tileSize, if (singleImage) boxMaxHeight else tileSize)`. Corrected the comment to state the real constraint. The existing `with(density) { maxWidth.toPx() }` sites (GalleryWallRender line ~1292, dialog ~1501) confirm this is the file's established pattern.

### Review
code-reviewer-deepseek-flash: clean pass. Verified LocalDensity imported (line 68), `maxWidth`/`maxHeight` access at the top of the scope is legal (scope is the implicit receiver there), no bare `.toPx()` on Dp remains (all conversions inside `with(density)`; the remaining grep hits at 1292/1501 are pre-existing `with(density)`-wrapped sites), `Modifier.size(Dp, Dp)` compiles, braces balanced, `onClick` captures the precomputed Float vals fine, all imports present. One cosmetic nit (duplicated `if (singleImage) boxMaxWidth else tileSize` expression — DRY-able but fine as-is).

### Follow-ups / notes
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.


## Previous Requests

**CI fix: PaperCard.safePadding re-broken — `PaddingValues` has no `left`/`right` parameters (previous e3482999 "fix" introduced the bug)**

### What was asked

CI failed on PaperCard.kt again — `No parameter with name 'left'/'right' found` at the `safePadding` `PaddingValues(...)` call, with cascading `Cannot infer type parameter 'R'`, `Unresolved reference 'calculateTopPadding'`, and `operator modifier required on compareTo` downstream.

### What was changed

- **`PaperCard.kt`** — root cause: commit e3482999 rebuilt `safePadding` in Dp (correct — `calculate*Padding` return Dp in this Compose version) but used `left =` / `right =` named arguments, which do NOT exist on `androidx.compose.foundation.layout.PaddingValues` — its constructor is `PaddingValues(start, top, end, bottom)` (plus `all` / `horizontal`+`vertical` overloads). `safePadding` became an error type, and every consumer cascaded (ruleStart's `with(density)` R-inference, `calculateTopPadding` unresolved, the `while (y < size.height)` compareTo on an error-typed Float). Fix: renamed the named args to `start =` / `end =` (the app is LTR-only, so start = left / end = right; the values still come from `calculateLeftPadding(LayoutDirection.Ltr)` / `calculateRightPadding(LayoutDirection.Ltr)`). Added a comment explaining the param-name constraint so it never regresses again.

### Review
code-reviewer-deepseek-flash: clean pass. Verified `start`/`end` are the correct `PaddingValues` param names, the `start =`/`end =` usages at the `drawLine`/`drawPath` Offset sites (~182-193, 350-351) are unrelated (Offset params, not PaddingValues), `TornPaperCard.safeContentPadding` uses the valid `horizontal`/`vertical` overload, the cascading errors all stem from the error-typed `safePadding` and resolve once it's well-typed, braces balanced, and a repo-wide grep confirmed NO other `left =`/`right =` PaddingValues args leaked anywhere in `app/src/main`.

### Follow-ups / notes
- The user's earlier "fix review stars + highlight color on colored paper" request was superseded by this CI fix ("nvm that fix this") — the ReelNotes stars/images/highlight polish from commit c64fa044 is still in place; if the stars/highlight still look off visually, that's a follow-up.
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.


## Previous Requests

**ReelNotes saved-entry polish: visible review stars, in-place image zoom (no Lightbox page, proper landscape), readable highlights on colored paper**

### What was asked

1. Fix the review stars in the saved review entry.
2. Proper view for landscape images + proper zoom like the mood board, WITHOUT opening a new page (the old thumbnails navigated to the Lightbox route).
3. The highlight color looks bad on colored note paper.

### What was changed

- **Stars — `EntryDetailScreen.kt` (`ReelNotesRender`)** — the ghost (unfilled) star color was `MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)`, but `FilledStar` ALREADY fades unfilled stars internally (0.25 fill / 0.45 stroke) — so ghosts were double-faded to ~invisible. Now full-strength `onSurfaceVariant`: visible 5-slot scale in both themes. Also cleaned the mis-indented comment block.
- **Images — `EntryDetailScreen.kt` (`ReelNotesRender`)** — replaced the 3-thumbnail Row (`take(3)`, `ContentScale.Fit`, `navController.navigate(CurioRoutes.lightbox(uri))`) with ALL attached images in a `BoxWithConstraints` strip: single image goes FULL-WIDTH (280dp box — proper landscape view), multiple images are 170.dp tiles in a horizontally scrollable row (240dp box), all `ContentScale.Crop` (fills the tile edge-to-edge, no letterboxing). Tapping magnifies IN PLACE via the existing mood-board machinery (`rememberMoodBoardZoomState` + `MoodBoardZoomOverlay`): image springs up centered + straight, pinch/pan refine up to 8x, double-tap resets, tap closes — no navigation. Zoom wiring mirrors `GalleryWallRender` (`animateFloatAsState` with `snap()` while gestureActive else spring; `zoomIn(uri, tileW, tileH, viewW, viewH)` from `maxWidth`/`maxHeight` px inside the Density scope). Removed the now-unused `navController` param from `ReelNotesRender` + its call site (FieldNotes keeps the lightbox nav — untouched). Legacy imageCount badge fallback kept.
- **Highlights — `PaperPalette.kt`** — `notePaperHighlight()` tones were same-hue pastels at 40% alpha (rose on pink, mint on mint...) that VANISHED against the colored paper. Each marker is now a DEEPER, more opaque stroke of its sheet's family (alpha 0x66→0x99): cream `FFC933`, butter `EE9E2D`, pink `E97E72`, mint `7FB877`, sky `6DA4D9`, lilac `A585D9` — dark ink still reads through at 60%. `paperHighlight()` (cream default, also the RichTextEditor default) bumped to `0x99FFC933` for consistency.

### Review
code-reviewer-deepseek-flash: clean pass (two rounds). Verified braces balance, `Modifier.size(Dp, Dp)` with `maxWidth`/`maxHeight` resolves inside `BoxWithConstraintsScope` (RowScope has no such properties — no ambiguity), `.toPx()` legal in the Density scope, no NEW imports needed (BoxWithConstraints, rememberMoodBoardZoomState, MoodBoardZoomOverlay, animateFloatAsState, snap, spring, size, height, fillMaxSize, horizontalScroll, rememberScrollState, ContentScale all already present), `weight` removal has no import fallout (RowScope member), `CurioRoutes.lightbox` still used by FieldNotes so no unused-import cleanup, `navController` removal complete, single-image full-width improvement applied after first pass. One accepted note: a single full-width image opens at ~1.1x (fit-based — it already fills the box; pinch reaches 8x).

### Follow-ups / notes
- FieldNotes (and any other format) still uses the Lightbox route for images — if the user wants the same in-place zoom everywhere, that's a clean follow-up.
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.


## Previous Requests

**CI compile fix — PaperCard.safePadding now builds in Dp (calculate*Padding return Dp, not px)**

### What was asked

CI failed on PaperCard.kt (`Cannot infer type for type parameter 'R'`, `No parameter with name 'top' found`, `maxOf` Dp-vs-Float mismatch, unresolved `.toDp()`, cascading `calculateTopPadding`/`compareTo` errors).

### What was changed

- **`PaperCard.kt`** — the `safePadding` block treated `PaddingValues.calculate*Padding()` as px Floats (called `.toDp()` on them and mixed `marginInset + 8.dp.toPx()` — Float — into `maxOf` against Dp), but in this Compose version `calculate*Padding` returns **Dp** (proven by `TornPaperCard`'s `safeContentPadding` feeding them straight into a `PaddingValues` constructor and `RichTextEditor` calling `.toPx()` on them). Rewritten to build the safe inset in Dp directly: `maxOf(contentPadding.calculateLeftPadding(LayoutDirection.Ltr), marginInsetDp + 8.dp)` with `marginInsetDp`/`foldInsetDp = 22.dp`, and top/right/bottom pass `calculate*Padding` through unchanged; the only px conversion kept is `marginInset = with(density) { marginInsetDp.toPx() }` for the red-margin Canvas rule. Removed the `with(density) { … }` wrapper (which caused the un-inferable 'R') and all `.toDp()` calls; the `foldInset` px var dropped (only used by old safePadding; `drawFoldFlap` computes its own `22.dp.toPx()`). Cascading errors at ruleStart/compareTo resolve once `safePadding` is well-typed.

### Review
code-reviewer-deepseek-flash: clean pass. Verified all remaining `calculate*Padding` uses are Dp-typed, `maxOf(Dp, Dp)` compiles (Dp is Comparable), no leftover `.toDp()`, no new/unused imports, `foldInset` removal safe, braces balanced.

### Follow-ups / notes
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.


## Previous Requests

**Quote-card tilt pivot fixed — 72dp min-height hoisted BEFORE the rotate in the saved-view modifier chain**

### What was asked

Hoist heightIn before the quote cards' rotate so the tilt pivot stays centered when a single-line quote grows to 72dp.

### What was changed

- **`EntryDetailScreen.kt`** (`RenderQuoteCards`) — the saved quote-card `NotePaperCard` call previously passed `minHeight = 72.dp` as a param; `NotePaperCard`'s dispatch appends `modifier.heightIn(min = minHeight)` AFTER the call-site modifier, so the chain was `fillMaxWidth → rotate(rotation) → heightIn(72)` — the rotation layer grew to 72dp and the tilt pivot shifted for short single-line quotes. Fix: hoisted the floor INTO the call-site chain before the tilt — `modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp).rotate(rotation)` and dropped the `minHeight` param (defaults to 0.dp → no-op in `NotePaperCard` and the concrete cards). Final chain: `fillMaxWidth → heightIn(72) → rotate` — the tilt now pivots around the fixed 72dp card center, stable whether the quote is one line or five.

### Review
code-reviewer-deepseek-flash: clean pass. Verified the old chain (rotate before heightIn = the pivot-shift bug) vs new (heightIn before rotate), only one effective heightIn remains (appended `heightIn(min=0)` are no-ops), `heightIn` import already present (line 24) and now genuinely used, braces balanced, and the editor's `QuoteCard` in CaptureFormatComponents.kt correctly untouched (it rotates a naturally-growing Column with no post-rotate floor).

### Follow-ups / notes
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.


## Previous Requests

**Mood board detail page: overlapping watermarks fixed — saved board surface is now opaque**

### What was asked

In the detail page mood board, the two watermarks are overlapping with each other — fix it.

### What was changed

- **`EntryDetailScreen.kt`** (`GalleryWallRender`) — the saved board `Surface` color changed from `if (AppPreferences.tintWashEnabledState) category.tint else surfaceContainerHigh` to `category.categorySurface(MaterialTheme.colorScheme.surfaceContainerHigh)`. Root cause: `category.tint` is a 20%-alpha translucent wash (`Category{Family}Tint = Category{Family}.copy(alpha = 0.20f)`), so the page-level `CurioWatermarkBackdrop` (11 category glyphs drawn behind ALL detail content) bled through the board and visually collided with the board's own seeded `CurioMoodBoardBackdrop` — two overlapping watermark layers. `categorySurface` returns an OPAQUE category-tinted card color (lerp of opaque colors = opaque in both themes) and already honors the Settings tint toggle (returns `base` unchanged when off), so the page watermark is now hidden behind the board and only the board's own seeded glyph pattern shows. Also removed the now-unused `AppPreferences` import (the removed conditional was its only use in the file).

### Review
code-reviewer-deepseek-flash: clean pass. Verified `categorySurface` is already imported (used for the board's Edit button in the same render), opaque in both themes, toggle-honoring; the editor board (`GalleryWallFormat`'s own `MoodBoardCanvas` color) and the full-screen `ExpandedMoodBoardDialog` (opaque wash background, no page watermark behind a dialog window) correctly left untouched. One nit applied: removed the orphaned `AppPreferences` import.

### Follow-ups / notes
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.


## Previous Requests

**Quote cards added to Reel Notes (review), Sound Bite (voice) and Mood Board (GalleryWall) — extracted Marginalia's "Favorite quotes" into a shared component**

### What was asked

Add a quote field option in more places — such as review, voice, and mood board — like Marginalia already has.

### What was changed

- **`CaptureData.kt`** — `SoundBite`, `ReelNotes` and `GalleryWall` each gained the 5 quote fields (`quotes`, `quoteSpans: List<List<TextSpan>>`, `quoteTilts`, `quoteStyles`, `quoteColors`) with `emptyList()` defaults (Gson legacy-safe — old entries keep their shape). `toFullContent()` now appends the quotes for all three.
- **`CaptureFormatComponents.kt`** — NEW shared machinery extracted from Marginalia: `QuoteCardsState` (parallel `SnapshotStateList`s for text/spans/tilt/style/color with `addCard`/`removeCard`/`setText`/`setStyle`/`setColor`/`hasContent` — tilt generated once at card creation, never re-rolled), `rememberQuoteCardsState(...)` (seeded from `initialData` with legacy padding), public `QuoteCardsSection` (header + count, per-card `QuoteCard`, dashed "Add quote" button inheriting `newCardStyle`/`newCardColor`), private `QuoteCard` (rotated paper card w/ rich-text toolbar + style/color toggles + Remove, toolbar OUTSIDE the paper slip), and `randomQuoteTilt()`.
- **`MarginaliaFormat.kt`** — refactored onto the shared `rememberQuoteCardsState` + `QuoteCardsSection` (removed the inline lists, the private `QuoteCard`, `randomTilt()`, and the now-unused imports). Behavior identical.
- **`ReelNotesFormat.kt`** — quote state seeded from `initialData`; `canSave = reviewText.isNotBlank() || quoteCards.hasContent`; `LaunchedEffect` keys + emit include all 5 lists; `QuoteCardsSection` sits after the review field, before the image row.
- **`SoundBiteFormat.kt`** — same wiring; `canSave` unchanged (recording-based); section after the note field, `enabled = recordingState != RECORDING` (frozen mid-capture like the note).
- **`GalleryWallFormat.kt`** — same wiring; `canSave = tiles.isNotEmpty() || quoteCards.hasContent`; section after the caption.
- **`EntryDetailScreen.kt`** — NEW shared private `RenderQuoteCards(quotes, spans, tilts, styles, colors, fallbackStyle, category, label)` extracted from MarginaliaRender (pads spans to quotes length, keeps ORIGINAL index through the blank filter so saved tilts stay aligned, curly-quote wrap with +1 span shift, `remember(origIndex)` fallback tilt); MarginaliaRender + SoundBiteRender + ReelNotesRender + GalleryWallRender all call it. Added `NotePaperStyle` + `TextSpan` imports.

### Review
code-reviewer-deepseek-flash: clean pass. Verified all new imports resolve, removed MarginaliaFormat imports aren't referenced by remaining code, `weight()` used in RowScope, `rememberQuoteCardsState` keys stable, `remember(origIndex)` valid, all CaptureData constructors use named args so the new fields can't break call sites. Nits applied: removed the now-unused `TextSpan` import from MarginaliaFormat and unified the redundant `if (data.quotes.any { it.isNotBlank() })` guards in the three saved renders (RenderQuoteCards already no-ops on empty — MarginaliaRender was already unguarded). One behavioral note accepted: GalleryWall can now save a quote-only board (0 tiles) — consistent with Marginalia's quote-or-primary philosophy.

### Follow-ups / notes
- OpenNotebook (wildcard) automatically passes `initialData` to all sub-formats, so quotes preload in edit mode with no extra wiring.
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.


## Previous Requests

**Saved-entry paper text boxes no longer collapse — every paper slip gets a min-height floor**

### What was asked

The saved entry text box height is still low — especially for single-line text. Fix it properly.

### What was changed

- **`PaperCard.kt`** — new `minHeight: Dp = 0.dp` param on `PaperCard` and `TornPaperCard`, applied as `modifier.heightIn(min = minHeight).rotate(rotation)` on the Surface (layout constraint before the draw-level rotation — same order as before). `NotePaperCard` gained the param and threads it into ALL 6 dispatch branches. Default `0.dp` keeps every existing caller unchanged (e.g. the editor's `PaperLineField`).
- **`EntryDetailScreen.kt`** — all 9 saved-view `NotePaperCard` sites got explicit floors: paragraph fields (SoundBite note, ReelNotes review + "No review written yet" fallback, Marginalia journal, FieldNotes observed/surprised/learnNext) = **96.dp** (matching the editor's field `minHeight = 96.dp`), quote cards + caption = **72.dp** so a single-line slip still reads as a proper note instead of text + padding.

### Review
code-reviewer-deepseek-flash: clean pass. All 6 dispatch branches + all 9 call sites verified (basher grep), `heightIn` before `rotate` is a legal layout-then-draw ordering preserving the pre-existing rotation, imports sorted (`heightIn` added to both files), types resolve, the only other `NotePaperCard` caller (`CaptureFormatComponents.kt` PaperLineField) is unaffected by the 0.dp default.

### Follow-ups / notes
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.


## Previous Requests

**Patrick Hand handwritten font for the paper text fields (quotes, journal, review, notes, captions, field notes)**

### What was asked

Use the Patrick Hand Google font (designed by Patrick Wagesreiter) for the text fields, quotes, etc. Make sure bold/italic etc. still work with this font — if not, include the italic version too for formatting.

### What was changed

- **New asset** — `app/src/main/res/font/patrick_hand_regular.ttf` (214KB TrueType, downloaded from google/fonts `ofl/patrickhand`). Google Fonts ships Patrick Hand as a SINGLE regular file — no italic/bold TTF exists (404 verified) — so the font registers all four style entries against that one file and the Android text stack SYNTHESIZES bold (fake-bold) and italic (oblique), the same pattern MaterialSymbolsFontFamily already uses.
- **`CurioTypography.kt`** — new `PatrickHandFontFamily` (Normal/Italic/Bold/BoldItalic → same file); `FontStyle` import added.
- **`RichTextEditor.kt`** — paper-mode field textStyle + placeholder now use `fontFamily = if (paper) PatrickHandFontFamily else FontFamily.Default`; imports added. Non-paper fields keep the neutral sans.
- **`CaptureFormatComponents.kt`** — `PaperLineField` (always paper) textStyle + placeholder use Patrick Hand.
- **`EntryDetailScreen.kt`** — all 8 saved-view paper Text sites (SoundBite note, ReelNotes review, Marginalia journal + quote, GalleryWall caption, FieldNotes ×3) use `bodyLarge.copy(fontFamily = PatrickHandFontFamily)`. Share-card teaser left on the default sans (it's not on paper).

### Review
code-reviewer-deepseek-flash: (pending — spawned in parallel).

### Follow-ups / notes
- `bodyLarge.copy()` preserves lineHeight 24sp, so the paper ruled-line cadence stays aligned.
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.


### What was asked

Make the torn pages' sides more rough.

### What was changed

- **`PaperCard.kt`** — `buildTornPath` jitter amplitudes raised for a rougher rip: bite 2.0→3.0dp, tear 1.0→1.6dp, and the perimeter step 8→6dp (more vertices on the edge → jagged + fibrous instead of softly undulating). Worst-case inward ≈ bite + tear ≈ 4.6dp, still well inside TornPaperCard's 16/14dp content-inset floor, so the rips read rough without ever clipping text.

### Review
code-reviewer-deepseek-flash: clean pass — edit is only the three constants + comment, no type errors, math matches the 16/14dp safety floor, grain/shadow/padding untouched.

### Follow-ups / notes
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.


### What was asked

Add more note-paper styles: a coffee-stain edge, a folded-corner page, or a ruled-with-red-margin notebook line as extra toggle options.

### What was changed

- **`CaptureData.kt`** — `NotePaperStyle` grew from RULED/TORN/TORN_RULED to +COFFEE / FOLDED / RED_MARGIN. Gson-safe: old persisted entries only reference old names.
- **`CurioIcons.kt`** — new chip glyphs: `LocalCafe` (local_cafe), `FoldedCorner` (auto_stories), `RedMarginLine` (border_clear).
- **`PaperCard.kt`** — `PaperCard` gained `redMargin` / `coffeeStains` / `folded` decoration flags. New `safePadding` logic: red margin indents content left past the 22dp red rule; folded pads content right so text never runs under the flap. New private `FoldedCornerShape` (rounded rect with the top-right corner cut along a diagonal dog-ear — Surface clips content to it). New `DrawScope` extensions `drawCoffeeStains` (seeded `Random(0xCAFE5EED)`, radial-gradient blobs + ring strokes, deterministic per size) and `drawFoldFlap` (flap triangle in lerp-darkened paper, crease line, soft drop shadow). `NotePaperCard` now dispatches all 6 styles; `NotePaperStyleToggle` is a horizontally-scrollable 6-chip row (Ruled / Torn / Coffee / Folded / Red Margin + Rules chip while torn). New imports: `horizontalScroll`, `rememberScrollState`, `Stroke`, `lerp`.
- **`RichTextEditor.kt`** — `when(paperStyle)` adds COFFEE / FOLDED / RED_MARGIN → `PaperCard` with flags; both `NotePaperStyleToggle` call sites (MAIN toolbar row + TOGGLE SpaceBetween row) gained `Modifier.weight(1f)` so the scrollable chip row takes leftover width instead of overflowing the toolbar.
- **`CaptureFormatComponents.kt`** — `PaperLineField` rewritten onto the central `NotePaperCard(style = paperStyle, ...)` dispatch (removed the torn/not-torn if/else and the `@Composable (PaddingValues) -> Unit` card lambda); the style chips moved to their own full-width scrollable row; imports swapped (NotePaperCard added, PaperCard/TornPaperCard removed).

### Review
code-reviewer-deepseek-flash: first pass flagged ONE real compile blocker — `safePadding` fed px Floats (`calculateLeftPadding` etc.) into the `PaddingValues` constructor, which requires Dp. Fixed by wrapping the construction in `with(density)` and calling `.toDp()` on every Float (incl. `maxOf(Float, Float).toDp()`). Second pass clean: FoldedCornerShape path walk is a valid clockwise outline, both dispatch `when`s cover all 6 enum values, `weight`/`horizontalScroll` combos legal (weight applied in RowScope at both call sites), PaperLineField import swap clean (PaddingValues/Alignment/Arrangement still used), fold/red-margin padding math keeps text clear of the flap and margin line. Minor nit applied: `drawCoffeeStains` param renamed `size` → `canvasSize` (AGENTS.md rule 7). Braces balanced across all 5 files (basher).

### Follow-ups / notes
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.


### What was asked

Let the paper color also drive the highlighter/ink tone per sheet so colored notes get a matching marker color.

### What was changed

- **`PaperPalette.kt`** — new `notePaperHighlight(color)` — each `NotePaperColor` gets its OWN translucent marker tone (amber on CREAM, warm gold on BUTTER, rose on PINK, mint green on MINT, sky blue on SKY, lavender on LILAC), so a highlighted phrase on a colored note reads as a marker that belongs to that page. `notePaperInk(color)` changed from a fixed dark to a `when()` with subtle hue-shifted darks per sheet (still warm-dark and readable on every pastel).
- **`RichTextEditor.kt`** — new `effectiveHighlight = if (paper) notePaperHighlight(paperColor) else highlightColor` used in the `tfv` remember init, the `LaunchedEffect(text, spans)` reseed, `emit()`, and `applyFlag()` (non-paper fields keep the caller's amber default). New `LaunchedEffect(paper, paperColor, effectiveHighlight)` repaints existing highlight spans in the new marker tone when the user taps a swatch — spans only carry the highlight FLAG, the color is baked at build time — preserving selection + composition.
- **`EntryDetailScreen.kt`** — all saved-view `NotePaperCard` sites (SoundBite note, ReelNotes review + "No review written yet" fallback, Marginalia journal + per-quote cards, GalleryWall caption, FieldNotes ×3) hoist a `*Sheet` val and pass `notePaperHighlight(sheet)` to `buildRichAnnotated` + `notePaperInk(sheet)` as the Text color; quote-icon tint + placeholder alphas derive from the sheet too. Imports swapped from `paperHighlight`/`paperInk` to `notePaperHighlight`/`notePaperInk`.

### Review
code-reviewer-deepseek-flash: clean pass. Verified both `when()` blocks exhaustive (no `else` → compiler-enforced), the repaint `LaunchedEffect` can't loop (keys never include `tfv`), all 8 saved-view sites updated with the import swap confirmed clean by whole-file search, fallback alphas correct. Two cosmetic nits accepted (effective* declarations grouped apart; one redundant first-composition repaint — harmless).

### Follow-ups / notes
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.


### What was asked

1. Make the colors have a toggle which opens it.
2. Add a subtle rigid surface look to the papers.
3. In saved entries the paper looks better a little taller / less slim.
4. Add the watermark in detail saved-entry pages.
5. Also in the Cabinet page.

### What was changed

- **`PaperCard.kt`** — `NotePaperColorToggle` is now COLLAPSIBLE: a compact "Color" chip (Palette icon + a live 14dp dot of the current paper color + label) that expands a 6-swatch row below it via `remember { mutableStateOf(false) }`. `PaperCard` + `TornPaperCard` inner Boxes gained a subtle rigid-card sheen — `Modifier.background(Brush.verticalGradient(White 0.08, Transparent, Black 0.05))` — so the slip reads as stiff paper stock instead of a flat fill. Imports added: `background`, `border`, `Brush`, `getValue`/`mutableStateOf`/`setValue`.
- **`CaptureFormatComponents.kt`** — `PaperLineField`: the color toggle moved OUT of the label row (which keeps the style chips) onto its OWN row below, because `NotePaperColorToggle` is now a Column (chip + expandable) and the SpaceBetween label row can't hold it.
- **`EntryDetailScreen.kt`** — root Column wrapped in a Box: the Box owns the category wash background, `CurioWatermarkBackdrop(activeCat = cat)` floats behind, and the inner Column keeps `fillMaxSize + verticalScroll`. Saved-view `NotePaperCard` contentPadding bumped 16/14 → 16/16 and quote cards 12/10 → 14/14 so the papers aren't slim.
- **`CabinetScreen.kt`** — root Column wrapped in a Box with `CurioWatermarkBackdrop(activeCat = filterCat ?: WILDCARD)` behind the grid (`statusBarsPadding` moved to the Box; `Box` import added).

### Review
code-reviewer-deepseek-flash: clean pass with one applied nit — the chip's current-color dot used `Modifier.border(1.dp, color)` which defaults to `RectangleShape`, drawing a square outline over the circular fill; fixed with `border(1.dp, color, CircleShape)`. Verified brace balance of both Box wrappers, `CurioWatermarkBackdrop` signature `(activeCat, modifier)`, `PaperLineField` label/style/color combination logic preserved, no unused imports, and both gradients share the same subtle sheen.

### Follow-ups / notes
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.


**Paper-corner clipping during entry fixed (both ruled + torn) + 'Edit entry' available on EVERY saved entry**

### What was asked

1. The paper boxes in both styles still have a corner issue — text hides behind the corner during entry.
2. Add Edit to all saved entries.

### What was changed

- **Corner clipping — `RichTextEditor.kt`** — root cause: the field's inner `Surface(shape = RoundedCornerShape(14.dp))` CLIPS its content (M3 Surface clips to shape), and in paper mode the field padding is 0 — so the first characters sat at the rounded corner and their tops were sliced. The outer paper card already owns the margins (16/14 or 12/10), so paper mode now uses a SQUARE shape (`RoundedCornerShape(0.dp)`) — no clip at all. Non-paper mode (14dp + fieldPadding 14/12) unchanged.
- **Edit everywhere — `EntryDetailScreen.kt`** — the overflow menu gained an `else` branch: every saved format (SoundBite, ReelNotes, Marginalia, FieldNotes, non-moodboard OpenNotebook) now shows "Edit entry" → `CurioRoutes.editEntry(id)`. The editEntry route already preloads ANY entry's data (SaveCaptureScreen dispatches on `editingEntry?.format` with `initialData = editingEntry?.captureData`), so all formats reopen with their saved content. Portfolio + mood-board branches keep priority.

### Review
code-reviewer-deepseek-flash: clean pass. Verified M3 Surface clip semantics (outer cards' own shapes don't clip text because their contentPadding clears the corners; the inner field Surface was the only offender), the if/else-if/else chain is balanced and can't shadow the Portfolio/mood-board branches (a Portfolio whose first section is a GalleryWall still routes to the Portfolio "Edit entry"), `editEntry` with `launchSingleTop` matches the existing pattern and preloads via the entry's own format.

### Follow-ups / notes
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.


**Torn-paper texture polish (softer, less grainy) + rich-text formatting finally survives typing AND save**

### What was asked

1. Polish the torn page paper texture more properly — don't make it too grainy.
2. Bold/italic/highlight still "shows then after a space or anything it disappears" and doesn't stay in save.

### What was changed

- **Grain — `PaperCard.kt`** — `buildGrainBitmap` softened: 360 → 150 speckles (alpha cap 55 → 22, slightly smaller radii) and 44 → 18 fiber dashes (alpha cap 34 → 16, shorter). Dense high-alpha specks read as "dirty"; the torn slip now reads as clean paper with a subtle tooth.
- **Formatting survival — `RichTextEditor.kt`** — root cause found: `emit()`'s text-unchanged else-branch trusted `extractRichSpans(new.annotatedString)` — the AnnotatedString BasicTextField reports back, which silently drops styles we set programmatically. After typing a space the IME fires an extra same-text re-report (caret/selection move), that branch read the field's span-dropped value, wiped OUR spans to empty, and `onRichTextChange(text, [])` cleared the parent's span state → formatting vanished and never reached CaptureData (nothing saved). Fix: the else-branch now returns `extractRichSpans(tfv.annotatedString)` — OUR tracked spans, which we always build ourselves, are the single source of truth on BOTH paths. `applyFlag()` also now applies directly (builds the styled `TextFieldValue`, sets `tfv`, calls `onRichTextChange` itself) instead of round-tripping through `emit()` — because `emit` derives spans from `tfv`, which isn't updated yet at that point.

### Review
code-reviewer-deepseek-flash: clean pass. Verified (a) `emit()` is now only called from `onValueChange` (applyFlag reports directly, no leftover `emit(TextFieldValue(...))` calls), (b) styled value keeps `selection = sel`, (c) pending flags still armed after direct apply, (d) the else-branch change can't break the initial apply path (it bypasses emit), (e) no unused imports (TextFieldValue already imported; `cos`/`sin` still used in the softened grain loop).

### Follow-ups / notes
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.


**Note-paper COLORS per text box — a swatch picker (cream/butter/pink/mint/sky/lilac) next to the Ruled/Torn toggle in each field's toolbar, persisted per field**

### What was asked

Add note-paper colors as an option per text box — a small color swatch picker in the toolbar alongside Ruled/Torn.

### What was changed

- **Data — `CaptureData.kt`** — new `NotePaperColor` enum (CREAM, BUTTER, PINK, MINT, SKY, LILAC) + nullable per-field color fields on every variant (SoundBite `titleColor`/`noteColor`, ReelNotes `reviewColor`, Marginalia `journalColor` + `quoteColors: List` parallel to quotes, GalleryWall `captionColor`, FieldNotes `observedColor`/`surprisedColor`/`learnNextColor`). Gson legacy-safe (null → CREAM fallback), mirrors the per-field style pattern.
- **Palette — `PaperPalette.kt`** — `notePaperSurface/Ink/Rule/Border(color)` theme-agnostic mappings; CREAM exactly matches the old paper constants, so default rendering is unchanged.
- **Cards — `PaperCard.kt`** — `PaperCard`/`TornPaperCard`/`NotePaperCard` gained `paperColor: NotePaperColor = CREAM` (surface/border/rules resolve via `notePaper*`); new public `NotePaperColorToggle` — compact circular swatches, active wears an accent ring + paper-ink check, `Modifier.semantics` color-name labels.
- **Editor — `RichTextEditor.kt`** — `paperColor` + `onPaperColorChange` params; `effectiveInk = if (paper) notePaperInk(paperColor) else ink`; the swatch picker sits on its OWN row below the format/style row in BOTH MAIN and TOGGLE modes (six swatches + chips would overflow a phone-width row — Rows don't wrap).
- **Line field — `CaptureFormatComponents.kt`** — `PaperLineField` gained `paperColor` + `onPaperColorChange`; swatches render next to the label; ink follows the sheet; unused `paperInk` import removed.
- **Formats** — per-field color state seeded from `initialData` with CREAM fallback, emitted, LaunchedEffect keys include colors (SoundBite, ReelNotes, Marginalia journal + per-quote `quoteColors` with add/remove syncing, GalleryWall, FieldNotes ×3).
- **Saved view — `EntryDetailScreen.kt`** — `paperColor` at all 9 `NotePaperCard` sites with `?: NotePaperColor.CREAM` fallback.

### Review
code-reviewer-deepseek-flash: two passes. First pass clean on data/palette/cards/formats/saved-view; flagged a REAL overflow bug — the 6-swatch picker added to the already-full toolbar rows would clip on phone widths (Rows don't wrap). Fixed: swatches moved to their own row below the format/style row in both toolbar modes; also replaced the transparent-icon accessibility hack with a `Modifier.semantics` color-name label. Second pass verified balanced braces, no duplicate toggle calls, semantics modifier valid, `contentDescription = null` pattern matches existing usage.

### Follow-ups / notes
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.


**Paper visual polish: format/style chips wear the warm paper accent (not theme colors), torn corners stop clipping text, paper border visible again**

### What was asked

1. The format button / paper-style toggle chips look weird — wrong colors (they were theme-aware on always-cream paper).
2. The Marginalia note still has corner text clipping.
3. The paper border color is off / invisible.

### What was changed

- **`PaperPalette.kt`** — new `paperAccent()` (warm amber `0xFF9A7B2F`) for paper-mode controls; `paperBorder()` → visible warm tan `0xFFCBB98F` (the old near-cream edge was effectively invisible).
- **`PaperCard.kt`** — `NotePaperStyleToggle.accent` defaults to `paperAccent()`; `NotePaperStyleChip` inactive tint switched from `MaterialTheme.colorScheme.onSurfaceVariant` (reads wrong on cream in dark mode) to `paperInk().copy(alpha = 0.55f)`. Torn bite 2.6→2.0dp / tear 1.4→1.0dp and `TornPaperCard` safe content floor raised 14/12→16/14dp — at the corners two torn edges meet and their inward bites compound diagonally into the first characters, so smaller rips + a bigger inset guarantee the text is never clipped.
- **`RichTextEditor.kt`** — new `effectiveAccent = if (paper) paperAccent() else accent` used for the MAIN + TOGGLE toolbars, the `NotePaperStyleToggle` calls, the expand-format button, `cursorBrush`, and the floating `SelectionFormatBar` — so every paper-mode control harmonizes with the cream slip in BOTH themes regardless of what accent the format passes (the formats still pass `MaterialTheme.colorScheme.tertiary`, which is now overridden centrally). `FormatToolButton` inactive tint changed from `onSurfaceVariant` to `accent.copy(alpha = 0.45f)`.
- **`CaptureFormatComponents.kt`** — `PaperLineField.accent` default → `paperAccent()`.

### Review
code-reviewer-deepseek-flash: clean pass. Verified no unused imports (`MaterialTheme` still used in all three files), `effectiveAccent` computed in composable scope (not inside any non-composable lambda), `paperAccent()` as a default param is legal (same pattern as the existing `paperHighlight()` default), and every `NotePaperStyleToggle` call site uses named args (signature reorder nit applied — `accent` default added in place).

### Follow-ups / notes
- Next per user: note-paper COLORS (new palette per style) — per-field style is already persisted, so a color companion is a clean follow-up.
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.


**Torn-note paper rework: lighter/faster torn rendering (fractal-noise technique from the TornPaper repo), per-text-box Ruled/Torn style toggle + "rules on torn" toggle moved into each field's toolbar (section-level Paper chip row removed), ruled notes untouched**

### What was asked

1. Torn notes are too heavy and lagging.
2. Don't change the ruled notes — torn stays an EXTRA option alongside them.
3. Add an option to add ruled lines to torn pages too (toggle alongside the toolbox).
4. Place the note-style option in the field's toolbar, NOT in a section-level row above that affects all.
5. Torn page quality itself isn't good enough — use the TornPaper repo (happy358/TornPaper) technique and adapt it.

User confirmed: both styles stay always-cream (no theme change), and the style picker lives per text box in the toolbar.

### What was changed

**Data — `CaptureData.kt`**
- `NotePaperStyle` gained `TORN_RULED` (torn slip WITH ruled lines).
- Per-field style fields on every leaf variant: SoundBite `titleStyle`/`noteStyle`, ReelNotes `reviewStyle`, Marginalia `journalStyle` + `quoteStyles: List<NotePaperStyle>` (parallel to quotes), GalleryWall `captionStyle`, FieldNotes `observedStyle`/`surprisedStyle`/`learnNextStyle`. All nullable; legacy entries (Gson → null) fall back to the take-level `paperStyle` → RULED. New entries mirror the primary field's style into `paperStyle` so `notePaperStyle()` stays meaningful.

**Rendering — `PaperCard.kt` (the lag + quality fix)**
- `TornPaperShape` now displaces the perimeter with multi-octave FRACTAL noise (`hash2`/`valueNoise`/`fractalNoise` — the repo's feTurbulence + displacement-map technique, base freq ~0.06).
- The Shape instance is `remember`ed AND its computed outline is CACHED per size — `createOutline` no longer rebuilds a ~150-point path on every recomposition (the old lag).
- The grunge texture is now a pre-rendered 192px bitmap drawn via ONE `ShaderBrush(ImageShader(TileMode.Repeat))` rect per frame instead of ~90 per-frame `drawCircle`s. One shared lazy singleton texture for all torn cards (the per-card seed makes each EDGE unique, so the generic grain is shared — also kills per-card ~100KB bitmaps).
- `shadowElevation = 0` — rasterizing a shadow for a jagged outline every frame was the other lag source.
- `TornPaperCard` gained `ruled: Boolean` — draws the notebook ruled lines inside the torn outline (the "rules on torn" look).
- `NotePaperCard` dispatches all 3 styles (TORN → torn no rules, TORN_RULED → torn with rules, RULED → classic paper).
- New public `NotePaperStyleToggle` (compact Ruled / Torn chips + a Rules chip that appears while torn) + private `NotePaperStyleChip`.

**Wiring**
- `RichTextEditor` — `torn: Boolean` replaced by `paperStyle: NotePaperStyle` + `onPaperStyleChange`; renders via `when()` over 3 styles; the toggle rides the MAIN toolbar row and stays visible in the TOGGLE collapsed row (SpaceBetween when paper).
- `PaperLineField` (title/caption) — `torn` replaced by `paperStyle` + optional `onPaperStyleChange`; toggle renders next to the label.
- All 5 formats (SoundBite, ReelNotes, Marginalia incl. per-quote styles, GalleryWall, FieldNotes) — `paperStyle` param removed; per-field style state seeded from `initialData` with `?: paperStyle ?: RULED` fallback; emitted with the legacy `paperStyle` mirror; LaunchedEffect keys include the per-field styles.
- `OpenNotebookFormat` — `paperStyle` param + import removed; now passes `initialData` to ALL 5 sub-formats (canPreload = initialData != null) so per-field styles + content persist through wildcard edit mode.
- `SaveCaptureScreen` — the section-level "Paper" chip row REMOVED (per user: not above, affecting all); `CaptureSectionState.paperStyle` and the `NotePaperStyle` import removed.
- `EntryDetailScreen` — all 9 saved-view `NotePaperCard` sites use the per-field style with `?: data.notePaperStyle()` fallback (SoundBite note, ReelNotes review + fallback, Marginalia journal + quotes via `quoteStyles.getOrNull(origIndex)`, GalleryWall caption, FieldNotes ×3).

### Review
code-reviewer-deepseek-flash: clean pass. Verified `buildTornPath(seed, size, density)` signature fix, shader imports/constructor valid, no `size` shadowing, no @Composable calls in non-composable lambdas (incl. the `card` local composable lambda), exhaustive 3-branch `when(NotePaperStyle)`, LaunchedEffect keys cover per-field styles, Gson legacy lists `orEmpty()`-guarded, no lingering `torn =` params or SaveCaptureScreen paperStyle refs. Three polish notes applied: shared grunge texture singleton, tile 160→192px with softened alpha (avoid visible repeat), hoisted LocalDensity.

### Follow-ups / notes
- Next per user: note-paper COLORS (new palette per style) — per-field style is persisted, so a color companion is a clean follow-up.
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.


**Three capture-editor bug fixes: quote tilt no longer re-rolls, bold/italic/highlight formatting survives typing, torn-paper corners no longer clip the text**

### What was asked

1. The tilt that shows during entry addition (quote cards) keeps changing every time — save it so it doesn't change.
2. The bold text format shows when applied or while typing, but moments later it's gone — formatting isn't surviving/staying.
3. The corner of the paper text box is cutting off the beginning text.

### What was changed

**Tilt persistence — `CaptureData.kt` + `MarginaliaFormat.kt` + `EntryDetailScreen.kt`**
- `CaptureData.Marginalia` gained `quoteTilts: List<Float> = emptyList()` — the hand-placed angle per quote card, generated ONCE at card creation and saved with the entry. Legacy entries omit it (Gson → empty), callers fall back to a stable per-index random tilt.
- `MarginaliaFormat` — `quoteTilts` mutableStateListOf seeded from `initialData` and padded parallel to quotes; each card reads `quoteTilts.getOrElse(i)`; Remove deletes the tilt; "+ Add quote" adds a fresh one; `quoteTilts.toList()` is a `LaunchedEffect` key so saved tilts persist. `randomTilt()` helper defined AFTER the import block (was briefly inserted mid-imports — a compile error — fixed before commit).
- `EntryDetailScreen.MarginaliaRender` — quote pairs carry their ORIGINAL index through the blank filter, and rotation reads `data.quoteTilts.orEmpty().getOrNull(origIndex) ?: remember(origIndex) { random }` — saved angle wins, legacy entries get a stable-per-card fallback.

**Formatting survival — `RichTextEditor.kt`**
- Root cause: `emit()` trusted the AnnotatedString BasicTextField reports back, which can silently drop styles we set programmatically — so bold/italic/highlight vanished moments after applying.
- Fix: rebase OUR OWN tracked spans across each edit (`rebaseSpans(oldText, newText, ...)` — common-prefix/suffix diff, spans before the change keep offsets, after shift by delta, overlapping clip to untouched parts), then emulate caret inheritance from our own spans at the diff start (`sp.start <= caret && caret <= sp.end`, inclusive at span END so typing right after a styled word continues it). The field's reported AnnotatedString is no longer merged in.
- `insertedRange` (findInsertedRange) computed ONCE and shared by the caret-inheritance block and the sticky pending-format block.

**Torn corner clipping — `PaperCard.kt`**
- `TornPaperShape` amplitudes kept modest (2.5dp bite + 1.5dp tear ≈ 4dp worst-case inward) so the ragged edge never reaches the text.
- `TornPaperCard` floors the content inset: `maxOf(horizontal, 14.dp)` / `maxOf(vertical, 12.dp)` so even the tight 10dp quote-card padding can't let a tear clip the first characters near the top-left corner.

### Review
code-reviewer-deepseek-flash: clean pass (two rounds). Verified no declarations between imports, `calculateLeftPadding`/`calculateTopPadding` resolve as PaddingValues member functions (no import needed), inclusive-end caret inheritance is correct half-open math with no out-of-bounds spans, `remember(origIndex)` is a valid composable call inside the inline forEachIndexed, and no @Composable calls leak into non-composable lambdas. Both review notes (hoist insertedRange, inclusive span-end inheritance) applied.

### Follow-ups / notes
- Next per user: note-paper COLORS (new palette per style) — the `NotePaperStyle` field is already persisted, so a color companion is a clean follow-up.
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.

---

**Creator bylines on the Topic Reveal hero card + Hollywood/Bollywood film filters + iconic Bollywood content batch**

### What was asked

1. Show the artist on the hero card of the topic reveal screen; same for books (author) — "you can suggest more".
2. Add Hollywood and Bollywood to the films and directors filters.

### What was changed

**Data (`scripts/enrich_topics.py` — new, idempotent; re-run safe)**
- Added an optional `byline` field to the topic schema and populated it: albums.json (448/448 — artist derived from the `album-{artist-slug}-…` id via a curated slug→name map + 15 per-album overrides for ambiguous slugs + the `album-the-…` 3rd-segment rule), books.json (148/149 — curated author map; `book-the-new-naturals-2023-173` intentionally unmapped), films.json (130/130 — curated director map incl. the new Bollywood batch), artworks.json (56/56 — painter parsed from the trailing " by X" in the name).
- Films + Directors: replaced the plain `American` origin tag with `Hollywood` (66 films, 41 directors — curated by US-studio production). `SpinScreen.NationalityTags` gained `Hollywood` + `Bollywood` so both bucket into the filter sheet's Origin group (alongside British / French / Korean / Indian…).
- Appended a curated iconic Bollywood batch (all tier 1, full teasers/instructions): 10 films (Sholay, Dilwale Dulhania Le Jayenge, Lagaan, 3 Idiots, Dangal, Pyaasa, Mughal-e-Azam, Mother India, Deewaar, Shree 420) + 9 directors (Raj Kapoor, Guru Dutt, Yash Chopra, Ramesh Sippy, Rajkumar Hirani, Sanjay Leela Bhansali, Anurag Kashyap, Karan Johar, Mehboob Khan).
- JSON written with literal UTF-8 + indent=2 (matches the checked-in formatting — first pass used ensure_ascii and churned 2,300+ album lines; fixed). Diff is now 1,385 insertions / 141 deletions (the deletions are just the swapped `American` tags).

**Kotlin**
- `CurioTopic.kt` — new trailing `val byline: String = ""` (defaulted → zero breakage for TopicJsonLoader/CaptureEntity named-arg constructors).
- `TopicJsonLoader.kt` — reads `obj.optString("byline", "")`.
- `TopicRevealScreen.kt` HeroCard — new byline pill bottom-start (mirrors the subtype pill bottom-end): `Artist · The Beatles` / `Author · George Orwell` / `Director · Christopher Nolan` / `Painter · Vincent van Gogh` with the Person glyph, shown only when byline + a label exist.
- `SpinScreen.kt` — `Hollywood`/`Bollywood` added to `NationalityTags`.

### Review
- `scripts/validate_topics.py`: 470 errors are ALL pre-existing album instruction-length violations under the strict local 280-char limit (identical on baseline HEAD via git stash; the Gradle task allows 450) — zero errors mention the new content or byline. Cross-file id uniqueness passes.
- code-reviewer pass (below) on the Kotlin changes: no new imports required (CategoryId, CurioIcons.Person, TextOverflow, Alignment already imported); byline is a pure display read.

### Follow-ups / notes
- Natural next step if wanted: `byline` for discoveries (discoverer, e.g. Penicillin → Alexander Fleming) — ~150-entry curated map, same script pattern.
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.

---

**Entry-detail hero gradient: light-mode banding fix + AMOLED refinement**

### What was asked
Light-mode detail-screen hero gradient looked wrong ("weird out of place gradient"); dark was great; AMOLED wanted more refinement.

### What was changed (`EntryDetailScreen.kt` hero banner, one block + one import)
- Root cause: light mode held the deep accent to 0.88 of the 380dp hero and crammed the whole accent→cream HSL glide into the bottom 12% (~46dp) — a thin high-contrast stripe that read as a broken gradient. Dark mode spreads over 30% (hence "awesome").
- Light: hold now 0.76, glide widened to the bottom 24% (~91dp ≈ 11dp/stop, comparable to dark) — a smooth luminous wash-out. Frosted bar (bottom edge ≤ ~80% worst case) stays on the deep accent since the first glide stops are barely lighter.
- AMOLED (new branch, `AppPreferences.THEME_STYLE_AMOLED`): start deepened ~25% toward black (`categoryCardFill(lerp(accent, Black, 0.15f))`), held to 0.60, then a long on-hue melt into pure black over the bottom 40% — moodier + OLED-friendly.
- Dark: unchanged. Added `androidx.compose.ui.graphics.lerp` import.

### Review
code-reviewer-deepseek-flash: clean. Stop positions strictly increasing in all three branches; legibility margin at 0.76 acceptable (glide still deep through 0.90); double-deepen noted as intentional. check_braces balanced.

### Follow-ups / notes
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.
