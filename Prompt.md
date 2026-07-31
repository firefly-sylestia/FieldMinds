# Curio Material Expressive Redesign

## Request

User asked to work only in `app/` Curio and fully redo UI/UX with Material Expressive / Android 17-style visual language, rename Spin to Shuffle, redesign Shuffle positions/layout/animation while keeping a stacked-card center/top hierarchy, redesign entry pages/profile/what-are-we-exploring, improve mood board detail zoom/cropping, fix save-entry reliability, polish dark mode colors, and avoid glass effects.

## Completed

- Renamed user-facing Spin labels to Shuffle across Curio navigation, Home, Picker, Cabinet empty states, Reveal, onboarding copy, and Shuffle controls while keeping route/class names stable for compatibility.
- Redesigned the Category Picker / "What are we exploring?" page with a Material Expressive hero card, stronger hierarchy, and clearer Surprise/Shuffle guidance.
- Improved saved entry detail presentation with an expressive gradient hero, solid elevated mood-board canvas, larger mood-board detail area, uncropped image rendering, and tap-to-open image lightbox support for mood-board and field-note images.
- Replaced the lightbox placeholder with real Coil image rendering plus pinch-to-zoom/pan support.
- Fixed a save reliability edge case where a null topic could leave the Save screen stuck in progress, and made Gallery Wall data updates react to tile position/layout changes instead of only tile count.
- Improved dark mode by switching to deeper midnight Android 17-style surface layers while leaving light-mode colors intact.
- Removed remaining user-facing emoji strings from capture/detail text paths touched by this request.
- Updated `app/CURIO_SPEC.md` and the Fastlane changelog for versionCode `20260730`.

## Validation

- `git diff --check` passed.
- Static ripgrep checks found no user-facing quoted Spin labels remaining in Curio Kotlin sources.
- Static ripgrep checks found no accidental Shuffle duration/API renames.
- Gradle build/compile/lint/test commands were not run because root DOX forbids Android build commands in this environment.
