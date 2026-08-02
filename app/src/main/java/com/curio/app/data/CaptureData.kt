package com.curio.app.data

import com.curio.app.data.CaptureFormat

/**
 * The note-paper look a capture's text boxes wear. [RULED] is the classic
 * notebook page (cream paper + ruled lines); [TORN] is the torn-note style —
 * a properly ripped paper slip with jagged edges, no ruled lines, slight
 * rotation for a hand-placed feel; [TORN_RULED] is a torn slip that ALSO
 * carries ruled lines (the "rules on torn" toggle) — ripped edge, notebook
 * cadence inside. [COFFEE] is a ruled page with coffee-stain blotches along
 * the edges; [FOLDED] is a ruled page with a folded (dog-ear) top-right
 * corner; [RED_MARGIN] is the classic school-notebook ruled page with a red
 * vertical margin line and the text indented past it.
 *
 * Chosen PER TEXT BOX (each field's toolbar holds the style chips +
 * rules-on-torn toggle) and persisted per field on every [CaptureData]
 * variant, so each note keeps its own look across save → detail view.
 * Legacy entries omit the per-field fields (Gson → null) and resolve to
 * the take-level [CaptureData.paperStyle] or [RULED].
 */
enum class NotePaperStyle { RULED, TORN, TORN_RULED, COFFEE, FOLDED, RED_MARGIN }

/**
 * The note-paper COLOR a capture's text boxes wear — a small swatch picker
 * sits next to the Ruled/Torn toggle in each field's toolbar. [CREAM] is the
 * classic warm note (and the default / legacy fallback); the others are
 * pastel note colors that keep the dark warm ink readable. Chosen PER TEXT
 * BOX and persisted per field on every [CaptureData] variant (parallel to
 * the per-field [NotePaperStyle]), so each note keeps its own paper color
 * across save → detail view.
 */
enum class NotePaperColor { CREAM, BUTTER, PINK, MINT, SKY, LILAC }

/**
 * A styled run over a string of text — half-open [start, end) character
 * offsets, plus the rich-text flags that style that run. Used by the
 * formats' text fields (Marginalia journal + quotes, Reel Notes review,
 * Field Notes sections, Sound Bite note) so saved captures keep their
 * bold / italic / highlight formatting.
 *
 * Offsets are into the ORIGINAL plain text (the `*Text`/`*Spans` parallel
 * fields in [CaptureData]), so legacy entries without spans decode fine and
 * any consumer can build an AnnotatedString from the pair.
 */
data class TextSpan(
    val start: Int,
    val end: Int,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val highlight: Boolean = false,
    // Optional per-letter font size in sp — lets the A+/A− rich-text tools
    // enlarge/shrink INDIVIDUAL letters while the rest of the field keeps
    // its default size. null = inherit the field's bodyLarge size. Stored as
    // a plain Float (not TextUnit) so Gson serializes it cleanly in saved
    // entries; legacy entries omit it → null.
    val fontSizeSp: Float? = null
)

/**
 * Sealed class hierarchy for structured capture data produced by each format.
 *
 * Each format produces its own typed data object. When saving, the data is
 * serialized to JSON and stored in the Room database as a JSON blob, then
 * deserialized back for rendering in EntryDetail.
 */
sealed class CaptureData {

    /** Sound Bite (§8.1): voice recording with optional title, audio file path, and metadata. */
    data class SoundBite(
        val durationSeconds: Int,
        val title: String,
        val note: String = "",
        val audioFilePath: String? = null,
        val fileSizeBytes: Long = 0,
        val encodingFormat: String = "AAC",
        // Optional rich-text formatting for the note field (bold/italic/
        // highlight). Legacy entries omit it → null, guard with orEmpty().
        val noteSpans: List<TextSpan> = emptyList(),
        // Note-paper style per text box — the title slip and the note wear
        // their OWN choice (ruled page / torn note / torn with rules).
        // Legacy entries omit them (Gson → null) and fall back to
        // [paperStyle] → [NotePaperStyle.RULED].
        val titleStyle: NotePaperStyle? = null,
        val noteStyle: NotePaperStyle? = null,
        // Note-paper COLOR per text box (parallel to the per-field styles).
        // Legacy entries omit them (Gson → null) and fall back to
        // [NotePaperColor.CREAM].
        val titleColor: NotePaperColor? = null,
        val noteColor: NotePaperColor? = null,
        // Optional quote cards — the same hand-placed paper slips as
        // Marginalia's "Favorite quotes". Legacy entries omit them (Gson →
        // empty) so saved voice notes keep their shape.
        val quotes: List<String> = emptyList(),
        val quoteSpans: List<List<TextSpan>> = emptyList(),
        val quoteTilts: List<Float> = emptyList(),
        val quoteStyles: List<NotePaperStyle> = emptyList(),
        val quoteColors: List<NotePaperColor> = emptyList(),
        // Take-level note-paper style — legacy fallback + the "primary"
        // field's style for old consumers. New entries set per-field styles
        // and mirror the note here so [notePaperStyle] stays meaningful.
        val paperStyle: NotePaperStyle? = null
    ) : CaptureData()

    /** Reel Notes (§8.2): review with rating, text, and attached images. */
    data class ReelNotes(
        val rating: Int,
        val reviewText: String,
        val imageCount: Int,
        // Real attached image URIs (poster / stills) so saved entries can show
        // the actual images — legacy entries only stored a count, so this
        // defaults to empty and stays backward-compatible.
        val imageUris: List<String> = emptyList(),
        // Optional rich-text formatting for the review field. Legacy entries
        // omit it → null, guard with orEmpty().
        val reviewSpans: List<TextSpan> = emptyList(),
        // Note-paper style for the review box (ruled / torn / torn with
        // rules). Legacy entries omit it (Gson → null) and fall back to
        // [paperStyle] → [NotePaperStyle.RULED].
        val reviewStyle: NotePaperStyle? = null,
        // Note-paper COLOR for the review box — legacy entries omit it
        // (Gson → null) and fall back to [NotePaperColor.CREAM].
        val reviewColor: NotePaperColor? = null,
        // Optional quote cards — the same hand-placed paper slips as
        // Marginalia's "Favorite quotes". Legacy entries omit them (Gson →
        // empty) so saved reviews keep their shape.
        val quotes: List<String> = emptyList(),
        val quoteSpans: List<List<TextSpan>> = emptyList(),
        val quoteTilts: List<Float> = emptyList(),
        val quoteStyles: List<NotePaperStyle> = emptyList(),
        val quoteColors: List<NotePaperColor> = emptyList(),
        // Take-level note-paper style — legacy fallback.
        val paperStyle: NotePaperStyle? = null
    ) : CaptureData()

    /** Marginalia (§8.3): journal entry with favorite quotes. */
    data class Marginalia(
        val journalText: String,
        val quotes: List<String>,
        // Rich-text formatting (bold/italic/highlight). journalSpans maps 1:1
        // to journalText; quoteSpans is parallel to quotes (one span list per
        // quote). Legacy entries omit both → null, guard with orEmpty().
        val journalSpans: List<TextSpan> = emptyList(),
        val quoteSpans: List<List<TextSpan>> = emptyList(),
        // Hand-placed tilt (degrees) per quote card — generated ONCE when a
        // card is created and saved with the entry, so the angle you saw while
        // adding persists into the saved view instead of re-rolling every
        // recomposition / section switch / revisit. Legacy entries omit it →
        // empty, callers fall back to a stable random tilt.
        val quoteTilts: List<Float> = emptyList(),
        // Note-paper style per text box — the journal page and EACH quote
        // card wear their own choice (ruled / torn / torn with rules).
        // quoteStyles is parallel to quotes (one style per card). Legacy
        // entries omit them (Gson → null, guard with orEmpty()) and fall
        // back to [paperStyle] → [NotePaperStyle.RULED].
        val journalStyle: NotePaperStyle? = null,
        val quoteStyles: List<NotePaperStyle> = emptyList(),
        // Note-paper COLOR per text box — the journal page and EACH quote
        // card wear their own color. quoteColors is parallel to quotes (one
        // color per card). Legacy entries omit them (Gson → null / empty)
        // and fall back to [NotePaperColor.CREAM].
        val journalColor: NotePaperColor? = null,
        val quoteColors: List<NotePaperColor> = emptyList(),
        // Take-level note-paper style — legacy fallback.
        val paperStyle: NotePaperStyle? = null
    ) : CaptureData()

    /** A single tile's layout on the mood board canvas. */
    data class TileLayout(
        val uri: String,
        val offsetXPx: Float,
        val offsetYPx: Float,
        val rotationDeg: Float,
        val widthPx: Float,
        val heightPx: Float
    )

    /** Gallery Wall (§8.4): moodboard collage with caption and tile positions. */
    data class GalleryWall(
        val imageCount: Int,
        val caption: String,
        val imageUris: List<String> = emptyList(),
        val tileLayouts: List<TileLayout> = emptyList(),
        // Note-paper style for the caption box (ruled / torn / torn with
        // rules). Legacy entries omit it (Gson → null) and fall back to
        // [paperStyle] → [NotePaperStyle.RULED].
        val captionStyle: NotePaperStyle? = null,
        // Note-paper COLOR for the caption box — legacy entries omit it
        // (Gson → null) and fall back to [NotePaperColor.CREAM].
        val captionColor: NotePaperColor? = null,
        // Optional quote cards — the same hand-placed paper slips as
        // Marginalia's "Favorite quotes", pinned under the collage. Legacy
        // entries omit them (Gson → empty) so saved boards keep their shape.
        val quotes: List<String> = emptyList(),
        val quoteSpans: List<List<TextSpan>> = emptyList(),
        val quoteTilts: List<Float> = emptyList(),
        val quoteStyles: List<NotePaperStyle> = emptyList(),
        val quoteColors: List<NotePaperColor> = emptyList(),
        // Take-level note-paper style — legacy fallback.
        val paperStyle: NotePaperStyle? = null
    ) : CaptureData()

    /** Field Notes (§8.5): three-section observation journal. */
    data class FieldNotes(
        val observed: String,
        val surprised: String,
        val learnNext: String,
        val imageUris: List<String> = emptyList(),
        // Rich-text formatting for the three sections (parallel to the text
        // fields). Legacy entries omit them → null, guard with orEmpty().
        val observedSpans: List<TextSpan> = emptyList(),
        val surprisedSpans: List<TextSpan> = emptyList(),
        val learnNextSpans: List<TextSpan> = emptyList(),
        // Note-paper style per section — each of the three field-journal
        // sections wears its own choice (ruled / torn / torn with rules).
        // Legacy entries omit them (Gson → null) and fall back to
        // [paperStyle] → [NotePaperStyle.RULED].
        val observedStyle: NotePaperStyle? = null,
        val surprisedStyle: NotePaperStyle? = null,
        val learnNextStyle: NotePaperStyle? = null,
        // Note-paper COLOR per section (parallel to the per-field styles).
        // Legacy entries omit them (Gson → null) and fall back to
        // [NotePaperColor.CREAM].
        val observedColor: NotePaperColor? = null,
        val surprisedColor: NotePaperColor? = null,
        val learnNextColor: NotePaperColor? = null,
        // Take-level note-paper style — legacy fallback.
        val paperStyle: NotePaperStyle? = null
    ) : CaptureData()

    /**
     * Open Notebook (§8.6): Wildcard pick-any-format.
     * [subFormat] captures which format the user chose,
     * [subData] contains that format's structured data.
     */
    data class OpenNotebook(
        val subFormat: CaptureFormat,
        val subData: CaptureData
    ) : CaptureData()

    /** One capture section inside a multi-section [Portfolio] entry. */
    data class CaptureSection(
        val format: CaptureFormat,
        val data: CaptureData,
        val title: String? = null
    )

    /**
     * Multi-section entry (universal capture): one saved entry that holds
     * several format bodies, each in its own [CaptureSection]. The detail
     * page shows a compact switcher to flip between sections. Single-section
     * entries are still stored bare (no Portfolio wrapper) so every
     * previously-saved entry keeps its shape.
     */
    data class Portfolio(
        val sections: List<CaptureSection>
    ) : CaptureData()

    /** Returns a human-readable one-line preview for Cabinet cards. */
    fun toPreview(): String = when (this) {
        is SoundBite -> "Voice note · ${durationSeconds}s" +
            if (title.isNotBlank()) " — $title" else ""
        is ReelNotes -> buildString {
            if (rating > 0) append("★".repeat(rating) + " ")
            append(reviewText.take(80))
            if (reviewText.length > 80) append("…")
        }
        is Marginalia -> buildString {
            val source = if (journalText.isNotBlank()) journalText else quotes.firstOrNull().orEmpty()
            append(source.take(80))
            if (source.length > 80) append("…")
        }
        is GalleryWall -> "Moodboard · $imageCount image${if (imageCount != 1) "s" else ""}" +
            if (caption.isNotBlank()) " — ${caption.take(40)}" else ""
        is FieldNotes -> buildString {
            val parts = listOf(observed, surprised, learnNext).filter { it.isNotBlank() }
            append(parts.firstOrNull()?.take(80) ?: "Empty field notes")
            if ((parts.firstOrNull()?.length ?: 0) > 80) append("…")
            if (parts.size > 1) append(" +${parts.size - 1} more")
        }
        is OpenNotebook -> "Wildcard · ${subFormat.name} — ${subData.toPreview().take(60)}"
        is Portfolio -> buildString {
            append("${sections.size} take${if (sections.size != 1) "s" else ""}")
            if (sections.isNotEmpty()) {
                append(" · ")
                append(sections.joinToString(" + ") { it.format.shortName })
            }
        }
    }

    /** Returns full multi-line content for EntryDetail rendering. */
    fun toFullContent(): String = when (this) {
        is SoundBite -> buildString {
            appendLine("Voice note · ${durationSeconds}s")
            if (title.isNotBlank()) appendLine("\"$title\"")
            if (note.isNotBlank()) appendLine(note)
            quotes.filter { it.isNotBlank() }.forEach { appendLine("\"$it\"") }
        }
        is ReelNotes -> buildString {
            if (rating > 0) appendLine("★".repeat(rating))
            if (reviewText.isNotBlank()) {
                appendLine(reviewText)
                if (quotes.any { it.isNotBlank() }) appendLine()
            }
            quotes.filter { it.isNotBlank() }.forEach { appendLine("\"$it\"") }
        }
        is Marginalia -> buildString {
            if (journalText.isNotBlank()) {
                appendLine(journalText)
                if (quotes.isNotEmpty()) appendLine()
            }
            quotes.forEachIndexed { i, q ->
                if (q.isNotBlank()) appendLine("\"$q\"")
            }
        }
        is GalleryWall -> buildString {
            appendLine("Moodboard · $imageCount image${if (imageCount != 1) "s" else ""}")
            if (caption.isNotBlank()) appendLine(caption)
            quotes.filter { it.isNotBlank() }.forEach { appendLine("\"$it\"") }
            imageUris.forEach { appendLine(it) }
        }
        is FieldNotes -> buildString {
            if (observed.isNotBlank()) {
                appendLine("Observed:")
                appendLine(observed)
            }
            if (surprised.isNotBlank()) {
                if (observed.isNotBlank()) appendLine()
                appendLine("Surprised me:")
                appendLine(surprised)
            }
            if (learnNext.isNotBlank()) {
                if (observed.isNotBlank() || surprised.isNotBlank()) appendLine()
                appendLine("Want to learn next:")
                appendLine(learnNext)
            }
            if (imageUris.isNotEmpty()) {
                if (observed.isNotBlank() || surprised.isNotBlank() || learnNext.isNotBlank()) appendLine()
                appendLine("Attached images:")
                imageUris.forEach { appendLine(it) }
            }
        }
        is OpenNotebook -> buildString {
            appendLine("Format: ${subFormat.name}")
            append(subData.toFullContent())
        }
        is Portfolio -> buildString {
            sections.forEachIndexed { i, section ->
                appendLine("— ${section.format.shortName} —")
                append(section.data.toFullContent())
                if (i != sections.lastIndex) appendLine()
            }
        }
    }

    /**
     * Every SoundBite audio file path nested inside this data — recurses
     * through OpenNotebook wrappers and Portfolio sections so delete / backup
     * flows can clean up all recordings, not just top-level ones.
     */
    fun audioFilePaths(): List<String> = when (this) {
        is SoundBite -> listOfNotNull(audioFilePath)
        is OpenNotebook -> subData.audioFilePaths()
        is Portfolio -> sections.flatMap { it.data.audioFilePaths() }
        else -> emptyList()
    }

    /**
     * The note-paper style this capture wears — [NotePaperStyle.RULED] for
     * legacy entries that predate the style field (Gson → null). OpenNotebook
     * and Portfolio delegate to their nested data so the whole entry reads
     * one coherent style.
     */
    fun notePaperStyle(): NotePaperStyle = when (this) {
        is SoundBite -> paperStyle ?: NotePaperStyle.RULED
        is ReelNotes -> paperStyle ?: NotePaperStyle.RULED
        is Marginalia -> paperStyle ?: NotePaperStyle.RULED
        is GalleryWall -> paperStyle ?: NotePaperStyle.RULED
        is FieldNotes -> paperStyle ?: NotePaperStyle.RULED
        is OpenNotebook -> subData.notePaperStyle()
        is Portfolio -> sections.firstOrNull()?.data?.notePaperStyle() ?: NotePaperStyle.RULED
    }
}
