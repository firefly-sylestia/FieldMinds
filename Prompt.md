# Prompt.md — Request Log

## Latest Request (COMPLETED)

**New torn-paper note style — an option inside the note-add flow, independent of the current ruled paper, per-note persistent**

### What was asked

Add a new full-paper-textured style for the note text boxes: a "proper torn note" look with torn edges/sides, WITHOUT changing the current ruled paper style. Introduce it as an option alongside the current one, selectable inside the note-add options (SaveCaptureScreen's universal capture picker), behaving as its own individual style per take ("behave its own individual"). Note paper colors come next (out of scope).

### What was changed

**Data layer — `CaptureData.kt`**
- New `NotePaperStyle` enum (`RULED` / `TORN`).
- `paperStyle: NotePaperStyle? = null` field on all 5 leaf variants (SoundBite, ReelNotes, Marginalia, GalleryWall, FieldNotes). Legacy entries omit it (Gson → null) and resolve as `RULED`.
- `CaptureData.notePaperStyle()` member — exhaustive `when`; OpenNotebook → `subData`, Portfolio → first section's data.

**Rendering — `PaperCard.kt`**
- `TornPaperShape : Shape` — walks the perimeter and jitters each edge with `tornNoise()` (a pure function of seed + coordinate), producing a jagged ripped outline that is deterministic → typing/recomposition never re-rolls the tears, and cheap to recompute per size change.
- `TornPaperCard` — theme-aware torn note: paper surface (cream light / toned dark), soft shadow that follows the ragged outline, hairline torn-edge border, subtle deterministic paper-grain speckles, NO ruled lines, per-card remembered random seed (stable per composition; optional explicit seed pin).
- `NotePaperCard` — dispatch helper: `TORN` → `TornPaperCard`, else classic `PaperCard` (ruled + corner). Used by the saved detail views.

**Wiring**
- `RichTextEditor` — new `torn: Boolean = false`; `paper && torn` → `TornPaperCard`, else `PaperCard`.
- `PaperLineField` — new `torn: Boolean = false` (torn slip branch).
- All 6 format composables (SoundBite, ReelNotes, Marginalia incl. QuoteCard, FieldNotes ×3, GalleryWall caption, OpenNotebook pass-through) gained `paperStyle: NotePaperStyle = RULED`, added to their emit `LaunchedEffect` keys AND into the emitted `CaptureData`, plus `torn =` flags on editors.
- `SaveCaptureScreen` — `CaptureSectionState.paperStyle`; section init reads `initialData.notePaperStyle()` (Portfolio / OpenNotebook / bare all covered); a "Paper" chip row (Ruled / Torn note, palette vs menu_book glyphs) inside the note-add options controlling the ACTIVE section; `paperStyle` passed to every format call.
- `EntryDetailScreen` — all 9 saved-view `PaperCard` sites dispatch via `NotePaperCard(style = data.notePaperStyle(), ...)` (SoundBite note, ReelNotes review + fallback, Marginalia journal + quotes, GalleryWall caption, FieldNotes ×3); `PaperCard` import replaced with `NotePaperCard`, unused `NotePaperStyle` import removed.

### Review
code-reviewer-deepseek-flash: clean pass. Verified exhaustive `when`, Gson-null-safe legacy decode, no @Composable calls inside Canvas lambdas, no `size` shadowing, valid `Outline.Rectangle` (fully-qualified), `TornPaperCard` import added in CaptureFormatComponents, `paperStyle` keying re-emits on style flip, chips/icons exist, no lingering `PaperCard` references. One OPTIONAL polish note (not a bug): saved-view torn cards use a fresh random seed per composition so the tear pattern differs between editor and saved view — acceptable/organic; could pin to entry-id hash later if consistency is desired.

### Follow-ups / notes
- Next per user: note-paper COLORS (new palette per style) — the `NotePaperStyle` field is already persisted, so a color companion is a clean follow-up.
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.
