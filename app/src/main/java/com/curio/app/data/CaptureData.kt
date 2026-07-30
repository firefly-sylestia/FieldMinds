package com.curio.app.data

import com.curio.app.data.CaptureFormat

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
        val encodingFormat: String = "AAC"
    ) : CaptureData()

    /** Reel Notes (§8.2): review with rating, text, and image count. */
    data class ReelNotes(
        val rating: Int,
        val reviewText: String,
        val imageCount: Int
    ) : CaptureData()

    /** Marginalia (§8.3): journal entry with favorite quotes. */
    data class Marginalia(
        val journalText: String,
        val quotes: List<String>
    ) : CaptureData()

    /** Gallery Wall (§8.4): moodboard collage with caption. */
    data class GalleryWall(
        val imageCount: Int,
        val caption: String,
        val imageUris: List<String> = emptyList()
    ) : CaptureData()

    /** Field Notes (§8.5): three-section observation journal. */
    data class FieldNotes(
        val observed: String,
        val surprised: String,
        val learnNext: String,
        val imageUris: List<String> = emptyList()
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
    }

    /** Returns full multi-line content for EntryDetail rendering. */
    fun toFullContent(): String = when (this) {
        is SoundBite -> buildString {
            appendLine("🎙 Voice note · ${durationSeconds}s")
            if (title.isNotBlank()) appendLine("\"$title\"")
            if (note.isNotBlank()) appendLine(note)
        }
        is ReelNotes -> buildString {
            if (rating > 0) appendLine("★".repeat(rating))
            append(reviewText)
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
            imageUris.forEach { appendLine(it) }
        }
        is FieldNotes -> buildString {
            if (observed.isNotBlank()) {
                appendLine("🔍 Observed:")
                appendLine(observed)
            }
            if (surprised.isNotBlank()) {
                if (observed.isNotBlank()) appendLine()
                appendLine("✨ Surprised me:")
                appendLine(surprised)
            }
            if (learnNext.isNotBlank()) {
                if (observed.isNotBlank() || surprised.isNotBlank()) appendLine()
                appendLine("📖 Want to learn next:")
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
    }
}
