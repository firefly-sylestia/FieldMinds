# Request: Mood board border + save button tint + lighter light-mode wash

## Request
1. In mood board, add a faint border (it looks blended into the tinted page).
2. Expand the tint to the save button background below too.
3. In light mode, make the tint less dark / a little more whitish.

## Changes
1. `GalleryWallFormat.kt` — editor MoodBoardCanvas Surface gets `border = if (fullScreen) null else BorderStroke(1.dp, accent.copy(alpha = 0.26f))` (faint accent rule so the board reads as a distinct surface on the tinted wash). Added `androidx.compose.foundation.BorderStroke` import.
2. `EntryDetailScreen.kt` — saved GalleryWallRender board Surface gets `border = BorderStroke(1.dp, category.categoryInk().copy(alpha = 0.26f))` — theme-aware (accent in light, light twin in dark). Added BorderStroke import.
3. `SaveCaptureScreen.kt` — sticky Save Button: containerColor `cat.accent` → `cat.tint`, contentColor/spinner/check icon `Color.White` → `cat.categoryInk()` so the whole bottom area stays in the page's color story.
4. `CategoryInk.kt` — light-mode wash `lerp(background, accent, 0.20f)` → `lerp(background, lerp(accent, Color.White, 0.30f), 0.14f)` — lighter/whiter. Dark mode unchanged (mid-tone at 15%).

## Validation
- Code reviewer passed: Surface border params accept BorderStroke?, categoryInk() @Composable call valid in Surface params, cat/category in scope, imports ASCII-ordered, Color import still used (section tabs) so no dead import. Minor notes: editor border uses plain accent (low-contrast on dark wash vs saved view's light twin — acceptable since MoodBoardCanvas only gets accent); tinted button may blend with the light tray (matches user's explicit "expand the tint" ask).

## Completion summary
- Committed & pushed.
