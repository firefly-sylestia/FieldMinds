# Request: Mood board background tint + true full screen

## Request
1. Expand the tint to the mood board background too (the board surface itself should carry the category tint, not just the page around it).
2. When the mood board is full screen, make it PROPER full screen — it currently looks like a dialog page, not full screen.

## Changes

### GalleryWallFormat.kt (editor)
- `MoodBoardCanvas` gained a `tint: Color` param; both call sites (inline + full-screen) pass `tint = tint`. The board Surface color changed `Color.Transparent` → `tint` so the collage reads as a tinted surface.
- Full-screen Dialog: `DialogProperties(usePlatformDefaultWidth = false)` → `(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)` so the dialog window draws behind the system bars — true full screen.
- Controls gained conditional insets: expand/collapse button + pin-to-front drop zone get `Modifier.statusBarsPadding()` when fullScreen; Add-images + Clear-board buttons get `Modifier.navigationBarsPadding()` when fullScreen. New imports: `navigationBarsPadding`, `statusBarsPadding`.

### EntryDetailScreen.kt (saved view)
- Saved GalleryWallRender board Surface color: `surfaceContainerHigh` → `category.tint` (keeps the faint BorderStroke border).
- `ExpandedMoodBoardDialog` gained a `wash: Color` param (call site passes `category.categoryBackgroundWash()`); removed the now-unused `val isDark = isCurioDarkTheme()` and its import (verified only 2 matches in file: import + isDark val).
- DialogProperties also gets `decorFitsSystemWindows = false`; Box background `if (isDark) … else …` → `background(wash)`.
- Edit + Close buttons get `.statusBarsPadding()`; hint text gets `.navigationBarsPadding()`. New import `navigationBarsPadding`.

## Validation
- Code reviewer passed: `decorFitsSystemWindows` available in Compose BOM (2026.05.01, added UI 1.4+), `tint`/`wash` in scope, imports alphabetical, `isDark`/`isCurioDarkTheme` removal safe, `Color`/`MaterialTheme` still used, no dead code. Minor non-blocking note: the editor full-screen outer Box still uses plain background, but the tint Surface fills it entirely so it's invisible.

## Completion summary
- Committed & pushed.
