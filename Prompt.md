# Request: Creamy cards on tinted pages look out of place

## Request
With the category tint enabled, the plain cream cards (theme `surface` / `surfaceContainerLow/High` / `surfaceVariant`) look out of place sitting on the tinted page backgrounds. Fix it "the proper way" — one reusable mechanism, not scattered if/else.

## Root cause
Pages wear `categoryBackgroundWash()` but the cards/chips/sheets on top still used the plain theme surfaces (cream in light, midnight grey in dark), so they read as foreign blocks on the tinted page.

## Changes
- `CategoryInk.kt` — new `@Composable fun CurioCategory.categorySurface(base: Color = MaterialTheme.colorScheme.surfaceContainerLow): Color`. Toggle-aware (returns `base` unchanged when the Settings tint toggle is off), dark branch reuses the per-family `DARK_WASH_TUNING` mid-tone with `blendFraction + 0.10f`, light branch `lerp(base, lerp(accent, White, 0.30f), 0.24f)`. Cards blend a touch stronger than the page wash so they read as tinted elevated surfaces.
- `SpinScreen.kt` — FilterSheet `containerColor`, `CompactChip` unselected state (new `chipSurface` param, passed at all 4 call sites), `EmptyPoolHint` card, bottom-bar pill unselected state → `cat.categorySurface(...)`.
- `TopicRevealScreen.kt` — `TeaserCard`, `ActionPromptCard`, and its icon chip → `cat.categorySurface(...)`.
- `EntryDetailScreen.kt` — top-bar menu button, `AudioPlayerBar` capsule (new `surface` param, passed from `SoundBiteRender`), ReelNotes review card, GalleryWall edit/expand buttons, and `PortfolioRender` section-switcher chips → `category.categorySurface(...)`.
- `CabinetScreen.kt` — replaced inline `filterWash` expr with `filterCat = CurioCategories.byId(selectedFilter ?: WILDCARD)`; search bar + both `FilterChipLite` call sites (new `chipSurface` param) → `filterCat.categorySurface(...)`.
- `SaveCaptureScreen.kt` — format chips + section tabs unselected states → `category.categorySurface(...)`.
- `CurioTopicCard.kt` — `CurioEntryCard` surface → `cat.categorySurface(...)` (Cat is derived from the entry; card is only used in the tinted Cabinet).

## Validation
- Code reviewer passed the full rollout; its one finding (PortfolioRender chips still plain) was fixed with the same pattern. Verified: default params referencing MaterialTheme.colorScheme are legal in @Composable signatures, named-arg call sites make new param ordering irrelevant, `cat`/`category`/`filterCat` in scope everywhere, imports alphabetical. Non-blocking note: cards blend ~1.7–2× stronger than the page wash (defensible "card stands out" choice).

## Completion summary
- Committed & pushed.
