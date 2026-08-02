# Prompt.md — Request Log

## Latest Request (COMPLETED)

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
