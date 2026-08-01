# Mood Board Edit — Reuse Entry-Id Watermark Seed

## Request

User: "Make the edit screen reuse the saved board's entry-id-derived watermark seed so the editor pattern matches the saved view exactly"

## Analysis

- `EntryDetailScreen.GalleryWallRender` derives the backdrop seed from the entry id: `remember(entry.id) { entry.id.hashCode() }`.
- The edit screen (`SaveCaptureScreen` → `FormatBodyForCategory` → `GalleryWallFormat`) preloaded tiles/caption but always generated a **fresh random** seed (`Random.nextInt()`), so the editor's watermark pattern differed from the saved view — the earlier reviewer nit.

## Plan

1. **`GalleryWallFormat.kt`** — added optional `boardSeed: Int? = null` param; local renamed `boardSeed` → `seed` (avoid shadowing) with `remember(boardSeed, initialData) { boardSeed ?: Random.nextInt() }`; both `MoodBoardCanvas` call sites (inline + fullscreen dialog) pass `seed = seed`. KDoc updated to reference the real `[boardSeed]` param.
2. **`SaveCaptureScreen.kt`** — `FormatBodyForCategory` gained `boardSeed: Int? = null` passed through to `GalleryWallFormat`; call site passes `boardSeed = editEntryId?.hashCode()` — same id, same hashCode as EntryDetail, so the editor matches the saved view.

## Completion Summary

- Validation green: braces (GalleryWallFormat 89/89, SaveCaptureScreen 66/66), seed threaded end-to-end, zero stale `val boardSeed` locals, `editEntryId?.hashCode()` == `entry.id.hashCode()` confirmed against EntryDetailScreen line 765.
- Code review clean: defaulted param keeps `OpenNotebookFormat`'s `GalleryWallFormat` call compiling; new-board flow (boardSeed = null) behavior unchanged; KDoc no longer references a non-existent `[seed]` param.
- Store changelog `20260730.txt` updated. Gradle build/lint NOT run (forbidden in this env; CI validates on push).
