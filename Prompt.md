# App icon and download presentation redesign — completion summary

## Request

Redesign the app icon and fix the icon appearing too zoomed in around the website's download-app presentation.

## Changes

- Replaced the oversized rotated card-stack Android foreground with a centered six-section discovery wheel using the Curio palette and generous adaptive-icon safe-zone padding.
- Added a dedicated monochrome launcher foreground for Android themed icons and wired it into both adaptive launcher declarations.
- Added `web/assets/icon.svg` as the scalable web counterpart of the launcher mark.
- Updated landing, help, privacy, and updates pages plus JavaScript fallbacks, favicons, and social metadata to use the new SVG asset consistently.
- Added a compact download-panel icon treatment and tightened download-panel spacing, button sizing, and mobile behavior so the app mark no longer dominates the download area.
- Preserved all existing download links and responsive interactions.

## Verification

- Parsed all changed Android XML and SVG files successfully with Python ElementTree.
- Confirmed the SVG viewBox and web asset references resolve.
- Confirmed no active web references to the old `assets/icon.png` remain.
- Confirmed CSS braces are balanced.
- `git diff --check` passes.
- Code review found no actionable blockers.
- No Gradle compile/build/test/lint command was run because the repository's AGENTS.md explicitly forbids local Android build validation; CI remains the source of truth.

## Closeout

- Changed files are ready to commit and push on branch `revamp`.
- No app What's New entry was added because this request affects launcher/marketing assets and the active Curio module has no changelog screen matching the legacy root instruction path.
