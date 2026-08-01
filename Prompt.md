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
