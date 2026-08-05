# Prompt — Home screen duplicate entries (v7.39)

## Request
"in home screen when a topic is explored dont show multiple entries of it and avoid duplicate entries"

## Root cause (RecentScreen.kt)
`buildRecentFeed(entries, explored, unexplored)` merged every saved entry
individually with the explored/unexplored topic rows and sorted newest
first — so one explored topic with several captures produced MULTIPLE rows
for the same topic on Home's five-item preview (and on the Recents page).

## Fix
Collapse the merged feed to ONE row per topic — the newest item wins:
- Multiple entries of the same topic → only the newest capture shows.
- A topic's explored/unexplored row is superseded by its newest saved
  entry (or vice-versa by timestamp), so a topic never appears twice.
- `topicIdentityKey()` groups by `categoryId + topicName` across all three
  item kinds (entries carry a `CurioTopic`; topic rows carry
  `ExploredTopic`/`UnexploredTopic`).
- Used `maxByOrNull` + `filterNotNull` (non-deprecated; groups never
  empty) to stay clean under Kotlin 2.3.21.
- Applies to both Home's preview and the shared Recents page (same builder).

## Review
Reviewer clean after the `maxBy` → `maxByOrNull` deprecation fix.

## Status
DONE — implemented, reviewed, Prompt.md updated, committed + pushed.
