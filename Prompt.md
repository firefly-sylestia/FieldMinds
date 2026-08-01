# Prompt.md — Running Request Log

## Latest Request — "Check other saved entries save logic — album entry texts disappear / images don't show, similar to that"

### Status: ✅ Complete (about to commit & push)

### The bug (user-reported)
In the Albums (Reel Notes) saved entry, the review text disappears and attached
images never show. "Similar to that" across other formats.

### Root causes
1. **Stale data emission (the "texts disappear" bug)** — `ReelNotesFormat` and
   `MarginaliaFormat` emitted their capture data inside
   `LaunchedEffect(canSave)` — keyed ONLY on the boolean. Once the first
   character was typed (`canSave` flips true) the effect never re-ran, so:
   - more review text typed after the first char → not re-emitted
   - star rating changed → not re-emitted
   - images added → not re-emitted
   - (Marginalia) later journal text / quotes → not re-emitted
   `SaveCaptureScreen` then saved the STALE first snapshot → text/rating/images
   silently dropped. (SoundBite keys title/note; FieldNotes keys all fields;
   GalleryWall keys caption+tiles; OpenNotebook keys subData — those were OK.)
2. **Reel Notes never actually attached images** — `attachedImages` was a
   placeholder `mutableStateListOf<Int>()` ("Add" just appended `0`; no picker),
   and `CaptureData.ReelNotes` had no `imageUris` field → no real image could be
   persisted or rendered.

### Fixes
- **CaptureData.kt** — `ReelNotes` gained `val imageUris: List<String> = emptyList()`
  AFTER `imageCount` (positional constructions like `ReelNotes(0,"",0)` still
  compile; old saved JSON deserializes with the field missing → guarded at render).
- **ReelNotesFormat.kt** — real image picking: `imageUris: List<String>` +
  `OpenMultipleDocuments` launcher with `takePersistableUriPermission`, capped at
  3, `ImageThumb(imageUri = …)`, remove filters the list. Emission now keyed on
  `(canSave, rating, reviewText, imageUris)` and emits
  `ReelNotes(rating, reviewText, imageUris.size, imageUris)`. Removed the now
  unused `mutableStateListOf` import; added activity/result + LocalContext imports.
- **MarginaliaFormat.kt** — emission keyed on `(canSave, journalText, quotes.toList())`.
- **EntryDetailScreen.kt** — `ReelNotesRender` gained `navController`; renders real
  image thumbnails (up to 3, tap → Lightbox) when `imageUris` non-empty, with the
  legacy `imageCount` badge as fallback. `data.imageUris.orEmpty()` guards legacy
  Gson blobs where the missing field decodes to null (Unsafe allocation skips
  Kotlin defaults).

### Validation
- Code review (deepseek-flash, 2 passes): compile-safe — all `ReelNotes`
  constructions (positional fallbacks in CaptureEntity/TopicCatalog, named sample)
  stay valid with the new defaulted field; Gson round-trip + `deserializeCaptureData`
  detection unaffected; no leftover `attachedImages`/`mutableStateListOf<Int>` refs;
  imports verified (Image / rememberAsyncImagePainter / CurioRoutes / height /
  weight already present in EntryDetailScreen). One real concern raised (Gson
  missing-field → null, not default) → fixed with `.orEmpty()` and re-reviewed clean.
- No gradle build run (per AGENTS.md, CI owns compilation).

### Prior work (this session)
- Mood board zoom (centered/straight, board pinch, high-res decode) — committed
- Mixed-deck gradients + category-pick navigation — committed earlier
- Edit-mood-board bug fixes — committed earlier
