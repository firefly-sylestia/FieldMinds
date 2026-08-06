# Prompt — Profile + Settings redesign (v7.53)

## Request
Redesign Profile and Settings to match the Home/detail visual language, use compact options, and move main card/deck customization into a new always-visible Experiments page reachable from Settings.

## Decisions
- Profile: Home-style identity hero with compact detail-style cards.
- Settings: compact hub with subpages.
- Experiments: reachable from the Settings hub and always visible.

## Changes
- Added `SettingsHubScreen` as the compact Settings root with Personalize, Explore, and Safety & support groups.
- Added Appearance, Notifications, Recording, Backup & restore, and About subpage routing.
- Added `ExperimentsScreen` and moved main-card, deck-card, Material blend, 3D button, pastel crown, Smart Spin, Smart density, and voice-to-text controls there while preserving existing `AppPreferences` keys and setters.
- Added `BackupToolsScreen` for Curio backup/restore and additive FieldMind archive import, retaining previews, confirmations, status feedback, and last-backup display.
- Preserved notification permission requests and restored overlay special-access handling, including reminder-time chips and lifecycle refresh after returning from Android settings.
- Reduced Profile list/card spacing and tightened lane/stat card internals without removing content or quality.
- Kept `SettingsScreen` as a compatibility alias to the new hub so old callers do not retain a second settings implementation.
- Consolidated backup navigation onto `SETTINGS_DATA` and removed the redundant tools route.

## Validation
Brace checks are BALANCED for all changed/new Kotlin files and `git diff --check` is clean. No local Gradle build/compile/lint/test was run because the repository forbids Android build commands here; CI remains the compilation source of truth.

## v7.54 — compact paper/style/color controls in the editing page

- Paper style options (Ruled, Torn, Rules, Coffee, Folded, Red Margin, Watermark, Rounded top) now stay on one horizontally scrollable strip instead of wrapping into a tall multi-line block.
- Expanded Color swatches use the same horizontal scrolling treatment.
- Reduced the Paper/Format toggle bottom padding from 6dp to 2dp, the expanded Paper-to-Color gap to 1dp, and the formatting toolbar bottom padding to 2dp.
- Tightened the shared PaperLineField stack spacing from 8dp to 3dp so labels, controls, and the paper field read as one compact group.
- Collapse/expand animations and all paper options remain intact; no behavior or visual quality was removed.
- Validation: brace checks BALANCED for RichTextEditor.kt, PaperCard.kt, and CaptureFormatComponents.kt; `git diff --check` clean. No Gradle command run per repository rules.

## v7.55 — restore detail quote glyphs and tighten detail readability

- Restored the real Material Symbols `format_quote` glyph at the opening and closing edges of saved quote cards; quote text remains raw rich text, so spans and the five-line limit stay accurate.
- Tightened the detail metadata stack and reclaimed the lifted seam space before the format body, removing the oversized visual gap beneath Quick Facts without removing content.
- Added a detail-only watermark alpha scale so the background glyphs remain present but sit quietly behind readable text.
- Validation: brace checks BALANCED for EntryDetailScreen.kt and CurioWatermarkBackdrop.kt; `git diff --check` clean. No Gradle command run per repository rules.

## v7.56 — fix Experiments compilation and mood-board quote expansion

- Restored the missing Smart density labels and summaries in ExperimentsScreen so `densityModeSegmentLabel` and `densityModeSummary` resolve during compilation.
- Mood-board editor quote cards now grow to fit their typed content instead of destructively shortening the preview during measurement; dynamic measured height is used for drag bounds.
- Saved/read-only mood-board previews remain compact with ellipsis, while the existing 280-character/five-line editor limits remain unchanged.
- Validation: brace checks BALANCED for ExperimentsScreen.kt and MoodBoardZoom.kt; `git diff --check` clean. No Gradle command run per repository rules.

## v7.57 — make Smart Density labels compile-proof

- Replaced the two fragile `densityModeSegmentLabel` and `densityModeSummary` helper references in ExperimentsScreen with exhaustive inline `when` expressions over SmartDensityMode.
- Preserved the existing Off, Compact, 2x labels and explanatory summaries while ensuring the reported unresolved-reference failure cannot recur from stale helper scope.
- Validation: ExperimentsScreen brace check BALANCED; `git diff --check` clean; no old helper references remain. No Gradle command run per repository rules.
