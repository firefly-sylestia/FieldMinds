# Request: Proper borders where cards/buttons blend into the tint

## Request
Cards and buttons that wear tinted surfaces on category-tinted pages visually melt into the page wash — add proper borders so they read as distinct surfaces.

## Changes
- `CategoryInk.kt` — new `@Composable fun CurioCategory.categoryBorder(fallback: BorderStroke? = null): BorderStroke?`. Toggle-aware: returns `fallback` (null default = no border) when the Settings tint toggle is off, else `BorderStroke(1.dp, categoryInk().copy(alpha = 0.30f))` — deep accent in light, light twin in dark, same resolution as `categoryInk()` and the mood-board border pattern. New imports `androidx.compose.foundation.BorderStroke` + `androidx.compose.ui.unit.dp`.
- `SpinScreen.kt` — EmptyPoolHint card, bottom-bar pill (unselected), and CompactChip (new `chipBorder` param, 4 call sites, fallback keeps pre-tint outlineVariant when toggle off) get `cat.categoryBorder()`.
- `TopicRevealScreen.kt` — TeaserCard + ActionPromptCard get `border = cat.categoryBorder()`.
- `EntryDetailScreen.kt` — top-bar menu button, AudioPlayerBar capsule (new `border: BorderStroke?` param passed from SoundBiteRender), ReelNotes review card, GalleryWall edit/expand buttons, PortfolioRender section chips (unselected only).
- `CabinetScreen.kt` — search bar + both FilterChipLite call sites (new `chipBorder: BorderStroke? = null` param) get `border = filterCat.categoryBorder()`.
- `SaveCaptureScreen.kt` — topic-reminder strip gets `cat.categoryBorder()`; format chips use `if (active.format == fmt) BorderStroke(accent 0.5) else category.categoryBorder(fallback = BorderStroke(1.dp, outline))` so the pre-tint outline border is preserved when the toggle is off; section tabs get a border when unselected.
- `CurioTopicCard.kt` — CurioEntryCard gets `border = cat.categoryBorder()`.

## Validation
- Code reviewer passed 3 passes. Findings fixed as prescribed: (1) SaveCaptureScreen format chips would lose their pre-existing outline border with the toggle off → fixed with the `fallback` param; (2) SpinScreen CompactChip paired a tinted fill with a neutral grey border → fixed with the `chipBorder` param + outlineVariant fallback (mirrors FilterChipLite). All scopes (`cat`/`category`/`filterCat`) resolve, named args make param ordering safe, imports alphabetical, `Surface(border:)` accepts `BorderStroke?`, no regressions for surfaces that previously had no border (they correctly get none when toggle off).

## Completion summary
- Committed & pushed.
