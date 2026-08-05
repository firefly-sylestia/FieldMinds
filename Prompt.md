# Prompt.md — Request Log

## Latest Request (COMPLETED)

**Detail page: adaptive gallery attachments + in-place zoom; fix moodboard double-tap zoom**

### Requested

- The attachments view for images on the saved-entry detail page is
  squared — make it a gallery where images adjust themselves by aspect
  ratio (e.g. one horizontal + one vertical, then three verticals in a
  line), like Google Photos.
- The zoom view should pop up from the tapped image's place, WITHOUT a
  darker background, NOT on a new page (no Lightbox) — over the detail
  page, just like the mood board's zoom.
- Also: the double-tap-zoomed images in the mood board need pinch-to-zoom
  and drag to work after the double-tap.

### Analysis

- Three squared attachment views existed in `EntryDetailScreen`:
  ReelNotes (170dp square Crop strip, already in-place zoom), Marginalia
  (150×120 strip → Lightbox page), FieldNotes (weighted 150×120 row,
  `take(3)`, → Lightbox page).
- The mood board's `MoodBoardZoomOverlay` is exactly the requested zoom UX:
  glides the tapped image from its spot to center, pinch/pan, tap close,
  NO dark scrim, in place (no page). It was already imported by the detail
  screen.
- Moodboard pinch-after-double-tap bug: the overlay used TWO gesture
  detectors (a parent pan/zoom `pointerInput` + the image's own
  `detectTapGestures`). After a double-tap, the tap detector consumed
  events and the parent's `awaitFirstDown()` (requireUnconsumed=true)
  skipped consumed downs — the two detectors desynced and a pinch/drag
  begun right after a double-tap could be eaten.

### Plan

1. NEW `ui/components/AdaptiveImageGallery.kt`: measures each image's
   aspect ratio (header-only BitmapFactory decode + EXIF rotation, content
   URI / file path, on Dispatchers.IO), packs images into JUSTIFIED rows
   (base width = rowHeight × aspect; rows stretch to fill the container,
   heights follow — no distortion), single image at natural aspect (capped
   for extreme portraits); tap → in-place `MoodBoardZoomOverlay` (gallery
   zIndex 1000 while zooming so later sections don't paint over it).
2. Replace the three squared strips (ReelNotes / Marginalia / FieldNotes)
   with `AdaptiveImageGallery` — Lightbox navigation gone from the detail
   page; FieldNotes now shows ALL images (was take(3)).
3. `MoodBoardZoom.kt` gesture rewrite: ONE `pointerInput` with
   `awaitFirstDown(requireUnconsumed = false)` classifies pinch/pan vs
   taps on the same stream — no dual-detector race. Single tap resets a
   refinement or closes (delayed by the double-tap window, cancellable);
   double-tap on the magnified image adds ONE MORE zoom step (×1.5,
   capped at 8× total) when un-refined, else resets. Taps anywhere close
   (no scrim).
4. Import cleanup (ContentScale, rememberAsyncImagePainter removed from
   EntryDetailScreen); docs; Prompt.md; commit + push.

### Status

- All edits applied: AdaptiveImageGallery.kt (new), MoodBoardZoom.kt
  (single-detector gestures + zoom-step + doc), EntryDetailScreen.kt
  (3 call sites + imports).
- Code-reviewer-deepseek-flash verified compile-safety (imports, layout
  math, division-by-zero guards, AwaitPointerEventScope supports launch,
  no stale `isPinched` refs) — only minor notes (double-tap window uses
  down-to-down 300ms; reset/step animatables could race in an edge case;
  aspect decode opens the stream twice). No action needed.
- Committed and pushed.
