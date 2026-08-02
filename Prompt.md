# Prompt.md — Request Log

## Latest Request (COMPLETED)

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
