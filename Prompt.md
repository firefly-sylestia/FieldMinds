# Remove Spin Page Optional Features, Add Watermark Backdrop

## Request

User disliked all the Spin-page features added in Settings (roulette dial, ritual & anticipation, deck enrichment, screen furniture) and asked to remove them so the Spin page goes back to how it was — and instead add background components with the category icons as a muted-shade watermark in the background.

## Plan

- Read DOX chain (master.md → AGENTS.md → app/AGENTS.md) and the Spin/settings/prefs files.
- Remove the four Spin-page feature prefs from AppPreferences (keys, reactive states, seeding, accessors).
- Remove the "Spin page" toggle section from SettingsScreen.
- Strip the v5.9 feature code from SpinScreen (RouletteDial, RitualHeader, SlotWindowPointer, InDeckStrip, FurnitureChips, RecentlyExploredStrip, enrich/idlePulse params, feature reads) and restore the pre-v5.9 flat layout (44dp gap → carousel → spin button → weight spacer → bottom bar).
- Add a muted category-glyph watermark backdrop behind all Spin content (active category glyph gets a faint accent tint).
- Remove now-unused imports; update header doc; update store changelog; commit + push.

## Completion Summary

- AppPreferences: removed KEY_SPIN_* constants, spin*State mirrors, initThemeMode seeding, and the four is/set accessor pairs (57 lines).
- SettingsScreen: removed the entire "Spin page" toggle section (4 toggles + header).
- SpinScreen: removed all v5.9 feature composables (~400 lines) and parameters (Carousel.enrich, HeroTicketCard.enrich, SpinButton.idlePulse); restored the pre-v5.9 non-scrollable layout; kept v5.10 dice-button changes (not part of the settings features).
- New `SpinWatermarkBackdrop` + `WatermarkGlyph` composables scatter all 11 category glyphs around the screen edges in `onSurface` @ 5% alpha, with the active category's glyph at its accent @ 11% alpha.
- Static checks: braces/parens balanced (255/255, 765/765), no dangling references to removed symbols, unused imports cleaned.
- Gradle build/lint deliberately NOT run (forbidden in this environment; CI validates on push).

## Follow-up (dice fix)

User: no need to revert the dice button, but the die had a weird square inside it — fix it.

- Removed the rounded-square die body (`drawRoundRect` with `CornerRadius`/`Size`) that sat behind the six orbiting pips in `ShuffleGlyph`, so the shuffle dice is now just the tumbling pips.
- Removed the now-unused `CornerRadius`/`Size` geometry imports and updated the v5.10 header doc note.
- Verified: no leftover references, braces 255/255, code review clean.
- Committed + pushed: `3a4455ba`.

## Follow-up (Home watermark)

User: add the same muted category-icon watermark backdrop to the Home screen so the design language carries across pages.

- Extracted the Spin-only watermark into a shared component `app/src/main/java/com/curio/app/ui/components/CurioWatermarkBackdrop.kt` (`CurioWatermarkBackdrop(activeCat, modifier)` + private `BoxScope.WatermarkGlyph`).
- SpinScreen now calls the shared component (removed its private `SpinWatermarkBackdrop`/`WatermarkGlyph` and the now-unused `Dp` import).
- HomeScreen adds the backdrop inside its outer `Box` behind the scrollable content, with `activeCat = selectedCategory ?: CurioCategories.byId(CategoryId.WILDCARD)` so "Surprise" highlights the wildcard die.
- Code review caught a latent compile bug (inherited from the original Spin private copy): `Modifier.align` is a `BoxScope` member extension, so `WatermarkGlyph` is now declared `private fun BoxScope.WatermarkGlyph(...)` — this would have failed CI once compiled.
- Verified: braces balanced, no dangling refs to old private names, review clean. Gradle build/lint NOT run (forbidden here; CI validates on push).

## Follow-up (CI compile fix)

User: pasted a CI failure from `:app:compileDebugKotlin`.

- Errors: `Argument type mismatch: actual type is 'Float', but '(IntSize, IntSize, LayoutDirection) -> IntOffset' was expected` + `Too many arguments for 'fun Alignment(...)'` on the watermark tile lines, plus `Unresolved reference 'align'` — all pointing at SpinScreen.kt (a stale pre-extraction run).
- Root cause: `Alignment(horizontalBias, verticalBias)` is NOT a valid constructor in Compose BOM 2026.05.01 — `Alignment` only takes an alignment function (or none). The correct API is `BiasAlignment(horizontalBias, verticalBias)`.
- Fix: in the shared `CurioWatermarkBackdrop.kt`, converted all 11 `Alignment(float, float)` calls to `BiasAlignment(float, float)` and added `import androidx.compose.ui.BiasAlignment`. The `WatermarkGlyph` param stays typed `Alignment` (BiasAlignment is a subclass); `Modifier.align(Alignment)` accepts it. The old `Unresolved reference 'align'` was already resolved by the BoxScope extension during extraction.
- Audited the whole repo: no other float-arg `Alignment(` usages outside this file; braces balanced; review clean.
- Committed + pushed: (see next commit).
