package com.curio.app.data

/**
 * The unified Curio topic schema.
 *
 * Backwards-compatible with the legacy `MockTopic` (same field names +
 * types). The only additive field is [musicGenre], which is non-null
 * ONLY for Music topics (the genre-picker filtering on SpinScreen reads
 * this) and `null` for every other category. Adding a Music topic
 * without a genre is a data-layer bug; the [TopicCatalog] enforces it
 * at construction time.
 *
 * Lifecycle:
 *  - Phase 0 (placeholder): TopicCatalog holds hardcoded topics.
 *  - Phase 4 (data layer):  Topics get loaded from `assets/topics/{id}.json`
 *                           via the JSON-driven pipeline; the schema
 *                           stays identical so consumers don't change.
 *
 * Consumers: SpinScreen, TopicRevealScreen, SaveCaptureScreen, CabinetScreen,
 * EntryDetailScreen, TopicHistoryScreen, LightboxScreen.
 */
data class CurioTopic(
    val id: String,
    val categoryId: CategoryId,
    /** "Album" / "Artist" / "Movie" / "Director" / "Painting" / "Movement" / "Book" / "Author" / "Field" / etc. */
    val subtype: String,
    val name: String,
    /** 1–2 sentence intriguing teaser (per CURIO_SPEC §6). */
    val teaser: String,
    /** Future image URL — empty string for placeholder phase. */
    val imageUrl: String,
    val exploreAction: ExploreAction,
    /** Only set when [categoryId] is [CategoryId.MUSIC]; required for that case. */
    val musicGenre: MusicGenre? = null
) {
    init {
        require(categoryId != CategoryId.MUSIC || musicGenre != null) {
            "Music topic '$name' (id=$id) must declare a non-null musicGenre."
        }
        require(categoryId == CategoryId.MUSIC || musicGenre == null) {
            "Non-Music topic '$name' (id=$id) must NOT declare a musicGenre " +
            "(got ${musicGenre?.name})."
        }
    }
}

/**
 * What the user should DO with the topic — per CURIO_SPEC §6 ("the
 * Explore Action" / "scratchpad area").
 *
 * Concretely this surfaces on TopicRevealScreen as the "Listen to /
 * Watch / Read / Look at / Explore ..." prompt with a concrete target
 * (album name, film name, museum collection, etc.) and a one-paragraph
 * instruction on what to look for.
 */
data class ExploreAction(
    val verb: String,
    val targetName: String,
    val durationMinutes: Int,
    val instruction: String
)

/**
 * One captured entry shown in the Cabinet grid + EntryDetail.
 *
 * Phase 4 wires real Room persistence via the same shape; Phase 0 uses
 * [TopicCatalog.sampleEntries] as the visual mock.
 *
 * @property format Which capture format the entry used (Sound Bite,
 *   Reel Notes, Marginalia, Gallery Wall, Field Notes, or Open
 *   Notebook for Wildcard). Drives the EntryDetail render body.
 * @property bodyPreview One-line preview shown on the Cabinet card.
 * @property bodyContent Multi-line content shown on EntryDetail.
 *   For Sound Bite format, this is a transcript-style caption
 *   (real audio lands with the asset pipeline phase).
 */
data class CurioEntry(
    val id: String,
    val topic: CurioTopic,
    val capturedAtDaysAgo: Int,
    val format: CaptureFormat,
    val bodyPreview: String,
    val bodyContent: String
)

/** The six capture formats from CURIO_SPEC.md section 8. */
enum class CaptureFormat {
    SoundBite,    // Music: voice note
    ReelNotes,    // Movies: review + collage
    Marginalia,   // Books: journal + quotes
    GalleryWall,  // Visual Art: moodboard
    FieldNotes,   // Science: 3-section report
    OpenNotebook  // Wildcard: pick your format
}