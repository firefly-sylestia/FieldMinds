# Current Request

## Status: COMPLETED (pushed)

"in topic reveal screen add a pin topic feature which will save that topic later for the user to relog it properly after watching"

## Changes (4 files)

1. **`app/src/main/java/com/curio/app/data/AppPreferences.kt`**
   - New `PinnedTopic(categoryId, topicName, pinnedAtMillis)` top-level data class.
   - JSON persistence (`org.json`, already used by TopicJsonLoader) under `KEY_PINNED_TOPICS`: `getPinnedTopics` (newest first, filterNotNull), `isTopicPinned`, `pinTopic` (deduped), `unpinTopic`, `savePinnedTopics`.
   - Reactive `pinnedTopicsState` (mutableStateOf, private set), seeded in `initThemeMode` so the reveal button + history list update instantly.

2. **`app/src/main/java/com/curio/app/ui/theme/CurioIcons.kt`**
   - Added `Bookmark = "bookmark"` (filled) + `BookmarkBorder = "bookmark_border"` (outline) glyphs.

3. **`app/src/main/java/com/curio/app/features/reveal/TopicRevealScreen.kt`**
   - Top bar now has a pin button (bookmark) before the close ✕: filled + category accent when pinned, outline when not; toggles `pinTopic`/`unpinTopic`.
   - `isPinned` reads the REACTIVE `pinnedTopicsState` (reviewer caught the original prefs-read version that wouldn't recompose — fixed).

4. **`app/src/main/java/com/curio/app/features/topichistory/TopicHistoryScreen.kt`**
   - New "Pinned for later" section at the top of the LazyColumn (bookmark header, `PinnedRow` per topic with category accent dot + name + category + unpin button), above the day-grouped capture history.
   - Tapping a pinned row reopens `revealFor(categorySlug, topicName)`.
   - Empty-state now only shows when there are neither entries nor pins.

## Review
- code-reviewer-deepseek-flash: clean ×2 (caught + fixed the non-reactive `isPinned` bug).

## CI
- Compile gate = GitHub Actions on push (per AGENTS.md — no local Gradle).
