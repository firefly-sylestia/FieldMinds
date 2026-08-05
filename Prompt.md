# Prompt — CI compile fixes + category as text (v7.38)

## Request
1. User: "i actually dont want category pill i just want text" — convert the detail page's category pill to plain text.
2. Fix the CI compile failures (5 errors from the previous gallery/zoom/tear commits — Kotlin 2.3.21 + coroutines 1.11.0):

```
EntryDetailScreen.kt:3014:45 Unresolved reference 'entry'
AdaptiveImageGallery.kt:229:23 sumOf overload ambiguity (Int/UInt)
AdaptiveImageGallery.kt:230:25 'operator' modifier required on Comparable.compareTo (cascade)
AdaptiveImageGallery.kt:280:45 Argument type mismatch: String? vs String
MoodBoardZoom.kt:542:42 'launch' can not be called without the corresponding coroutine scope (coroutines 1.11 deprecation-as-error)
```

## Fixes (all verified by code-reviewer)
1. **Category pill → plain text** (EntryDetailScreen): the chip row is now a label line — category glyph + name in `categoryInk()` (labelLarge SemiBold), entry title as text (weight 1, maxLines 1, ellipsis), Legacy as muted text. No Surface backgrounds.
2. **Unresolved 'entry'** — `ExpandedMoodBoardDialog` has no `CurioEntry`; `seed = noteSeed(entry.id, 60)` → `seed = noteSeed(seed.toString(), 60)` (salted from the dialog's entry-derived seed so slips stay distinct from the board, matching the inline card's split). Inline GalleryWallRender call keeps `noteSeed(entry.id, 60)`.
3. **sumOf(Float) ambiguity** — `row.sumOf { it.second }` → `row.fold(0f) { acc, pair -> acc + pair.second }` (also clears the compareTo cascade).
4. **String? mismatch** — `ExifInterface(uri.path)` → `uri.path?.let { path -> ExifInterface(path).getAttributeInt(...) }` (when branches share Int? common type).
5. **launch scope** — captured `val gestureScope = this` (PointerInputScope : CoroutineScope) at the top of the pointerInput block; delayed single-tap action uses `gestureScope.launch { delay(DoubleTapTimeoutMs) ... }`.

## Status
DONE — implemented, reviewed, Prompt.md updated, committed + pushed. Awaiting CI.
