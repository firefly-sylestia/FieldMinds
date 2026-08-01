# Prompt.md — Request Log

## Current Request: Fix CI compile failure

**User request:** CI failed on `Task :app:compileDebugKotlin` — `GalleryWallFormat.kt:817:31 Unresolved reference 'rotationDegrees'. Fix this.`

## Root cause
The mood-board EXIF-aware tile sizing (from the earlier editor-polish change) called `ExifInterface(stream).rotationDegrees`. The `rotationDegrees` property only exists on the **AndroidX** `androidx.exifinterface.media.ExifInterface`; the **framework** `android.media.ExifInterface` (what the file imports, and the only one available without adding a dependency) exposes the raw `TAG_ORIENTATION` attribute + `ORIENTATION_*` constants instead. CI was the only place this surfaced (local gradle builds are forbidden by AGENTS.md).

## Fix (GalleryWallFormat.kt `decodeImageBounds`)
Replaced the property access with the framework API:
- `ExifInterface(stream).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)`
- Map `ORIENTATION_ROTATE_90/180/270` → degrees via a `when` (180 needs no swap; only 90/270 swap width/height).
- All referenced members exist in `android.media.ExifInterface` at minSdk 26 (`ExifInterface(InputStream)` ctor is API 24+); the enclosing `runCatching` already absorbs the ctor's IOException.

## Validation
- code-reviewer-deepseek-flash: clean — API verified against framework class, mapping correct, no ambiguity (explicit import), runCatching covers IOException. Minor non-blocking notes: EXIF transpose/transverse (5/7) orientations also swap aspect but are vanishingly rare; the ExifInterface read opens a second stream on the main thread (pre-existing pattern).
- grep: `rotationDegrees` now appears only in a comment; `TAG_ORIENTATION`/`getAttributeInt`/`ORIENTATION_*` all present.
- No local gradle build per AGENTS.md — CI on push is the compile gate.

## Status
DONE — committed & pushed (fix: use framework ExifInterface orientation API — resolves CI compile failure).
