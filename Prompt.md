# Shuffle Navigation, Motion, and Mood Board Polish

## Request

User asked why Shuffle opened like a separate page from Home, then requested targeted Shuffle polish without changing the overall design: make Home hero/category entry land in the bottom-nav Shuffle experience, redesign Shuffle category/filter buttons to match Explore category tiles without text-heavy treatment, improve Shuffle card/deck and dice animations, make tapping the Shuffle card open the topic instead of spinning, update spin copy while spinning, enable mood-board image expansion/zoom, and refresh Home arrow/inactive category button styling.

## Plan

- Read DOX chain and relevant Curio app files.
- Use current Material motion guidance as inspiration for card/deck transitions and smooth easing.
- Update Home navigation to use bottom-nav-style tab switching for Shuffle category routes.
- Refine Shuffle bottom controls, card stack visuals, dice animation, tap logic, and copy while preserving the screen structure.
- Add in-format full-screen mood-board image viewer with pinch zoom/pan.
- Refresh Home arrow buttons and inactive category chips.
- Update store changelog and completion notes.
- Run non-build static checks only; Gradle build/test/lint commands are forbidden in this environment.

## Completion Summary

- Home category hero navigation now uses bottom-nav tab navigation semantics for category-loaded Shuffle routes.
- Bottom nav selection recognizes parameterized Shuffle routes.
- Shuffle card taps now open landed topics only; the Shuffle CTA owns spinning.
- Shuffle category/filter controls were redesigned as compact deck-style buttons, card stack depth and dice motion were refined, and behind cards gained layered surfaces.
- Mood-board tiles now include an image expand affordance plus full-screen pinch-zoom/pan viewing.
- Home category inactive chips and arrow affordances were refreshed.
- Curio spec and Fastlane changelog were updated.
