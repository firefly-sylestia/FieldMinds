# Spin / Topic Reveal / Home UI polish — completion summary

## Task

User asked to refine the Spin page (dialogs, shuffle cards, transparent/red topic issues), fix the topic view red color, reduce top-bar vertical space across all pages, and fix Home explore-category pill clipping + recently-explored overlap.

## What changed

### `app/src/main/java/com/curio/app/ui/theme/CurioTheme.kt`
- Added the five Material 3 surface-container color roles (`surfaceContainerLowest` through `surfaceContainerHighest`) to both light and dark schemes so the new solid-card surfaces match the warm Curio palette instead of falling back to Material baseline tones.

### `app/src/main/java/com/curio/app/features/spin/SpinScreen.kt`
- Top bar: `vertical = 0.dp`.
- Category `DropdownMenu`: switched background to `surfaceContainerLow`, title uses `titleSmall`, cleaner menu items.
- Replaced the empty/transparent right-side badge with a compact `filtered / total` count pill.
- Filter `ModalBottomSheet`: added `BottomSheetDefaults.DragHandle()`, `surfaceContainerLow` container, 32.dp top corners, headline title, expressive chip styling.
- Shuffle carousel cards are now solid (`surfaceContainerHighest` center, `surfaceContainer` sides) with subtle accent border, higher elevation, and `AnimatedContent` slot-machine transitions when topics shuffle.
- Topic title uses `MaterialTheme.colorScheme.onSurface` instead of the reddish deep plum.

### `app/src/main/java/com/curio/app/features/reveal/TopicRevealScreen.kt`
- Top bar: `vertical = 0.dp`.
- Topic name now renders in `MaterialTheme.colorScheme.onSurface` (neutral, not red).
- Hero card background switched from `accent.copy(alpha=0.16f)` to solid `surfaceContainerHigh` with a subtle accent border.

### `app/src/main/java/com/curio/app/features/home/HomeScreen.kt`
- Top bar: `vertical = 0.dp`, removed the 4.dp spacer below it.
- "Explore categories" `LazyRow` no longer uses parent horizontal padding; it uses `contentPadding = PaddingValues(horizontal = 16.dp)` so the selected chip scale no longer clips.
- Moved the extra bottom `Spacer` inside the `StaggeredEntrance` (wrapped in `StaggeredItem`) and increased the final bottom spacer to prevent overlap with the bottom/reminder card.

### Top-bar compact pass across the app
- `ProfileScreen`, `CabinetScreen`, `CategoryPickerScreen`, `SettingsScreen`, `ManageCategoriesScreen`, `TopicHistoryScreen`, `BugReportScreen`, `SaveCaptureScreen`, `LightboxScreen`: all top-bar `Row`s now use `vertical = 0.dp` for a tighter, consistent look.

## Verification

- No Gradle builds run per project `AGENTS.md` rules.
- Searched for duplicate imports and unresolved references: `BorderStroke`, `AnimatedContent`, `togetherWith`, `BottomSheetDefaults` imports are present and correct.
- Verified `surfaceContainerLow`/`surfaceContainerHigh`/`surfaceContainerHighest` now have custom definitions in `CurioTheme.kt`.
- Verified `StaggeredItem` usages in `HomeScreen.kt` are inside the parent `StaggeredEntrance`.
- Code-reviewer-kimi review requested.

