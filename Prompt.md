# Curio interaction and capture redesign — Completion Summary

## Task
User requested a broader redesign pass across category cards, status-bar edge-to-edge polish, save persistence, image capture for moodboards/field notes, voice-note notes, topic/category flow wiring, intro startup wiring, and Spin shuffle/card visual improvements.

## What changed
- Category Picker cards now use full-card category-color fills, larger 2-column hero-style cards, white icon badges, oversized background glyphs, and roomier spacing.
- Edge-to-edge system bars are transparent with explicit light/dark icon handling to avoid the awkward status-bar gradient band.
- Splash now checks persisted onboarding state and routes first launches into onboarding; onboarding completion is stored in SharedPreferences and replay/reset flows clear it properly.
- Cabinet now observes the capture repository flow so saved entries appear without requiring a one-shot reload.
- Saved capture data now carries voice-note notes plus image URI lists for Gallery Wall and Field Notes.
- Sound Bite capture adds a longer optional note field.
- Gallery Wall now launches the Android image picker and renders selected images in the collage tiles.
- Field Notes now launches the Android image picker and renders/removes attached photo thumbnails.
- Spin now has an in-screen category row, so changing category updates the Spin topic pool without leaving the page; topic routes now URL-encode names and lightbox URLs.
- Coil Compose was added for rendering selected local image URIs.
- Store changelog for versionCode 1 was added.

## Verification
- Did not run Gradle build/test/lint/assemble/check commands because the root DOX explicitly forbids them in this environment.
- Ran static repository checks with ripgrep and a Python brace-balance script on Kotlin files.

## Reviewer follow-up fixes
- Switched image picking to `OpenMultipleDocuments()` and persistable read URI grants so saved image URIs can survive app restarts.
- Rendered saved Gallery Wall and Field Notes images in Entry Detail.
- Guarded Spin shuffling against empty filtered topic pools and disabled shuffle when a selected filter has no topics.
- Replaced placeholder Topic History samples with real saved-capture history grouped by day and using capture-format glyphs.
