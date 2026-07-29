package com.curio.app.data

/**
 * The unified Curio topic schema.
 *
 * Topics are loaded from `assets/topics/{categoryId}.json` at runtime via
 * [TopicJsonLoader]. The JSON schema mirrors this data class 1:1 — see
 * [TopicJsonLoader] for the deserialization code.
 *
 * The previous `musicGenre` field (an enum) has been replaced with a generic
 * `tags: List<String>` field. This lets the Spin screen dynamically
 * generate filter chips for *any* category (e.g. genres for Artists,
 * eras for Films, mediums for Painters) without hardcoding enums. The
 * quality of curation lives in the JSON, not in code.
 *
 * Consumers: SpinScreen, TopicRevealScreen, SaveCaptureScreen,
 * CabinetScreen, EntryDetailScreen, TopicHistoryScreen, LightboxScreen.
 *
 * @property tags Free-form string tags for the Spin screen's dynamic
 *   filter chip row. Tags are category-specific: Artists might use
 *   ["Rock", "1970s"], Films might use ["Drama", "1990s"], Painters
 *   might use ["Impressionism", "Oil"]. Empty list = no filters.
 * @property tier Quality tier for the random picker. 1 = human-curated
 *   marquee (highest quality, surfaces most often). 2+ = AI-generated
 *   long tail (still good, just less hand-tended). Default = 1 so all
 *   loaded topics are presumed marquee unless tagged otherwise.
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
    /** Free-form tags for dynamic Spin filter chips. Empty = no filters. */
    val tags: List<String> = emptyList(),
    /** Quality tier (1 = marquee, 2+ = long tail). */
    val tier: Int = 1
) {
    init {
        require(id.isNotBlank()) { "CurioTopic id must not be blank." }
        require(name.isNotBlank()) { "CurioTopic name must not be blank." }
        require(teaser.isNotBlank()) { "CurioTopic teaser must not be blank for '$id'." }
        require(tier in 1..3) {
            "CurioTopic tier must be 1, 2, or 3 (got $tier for '$id')."
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

/**
 * The six capture formats from CURIO_SPEC.md section 8.
 *
 * 11 categories map onto these 6 format bodies — see
 * [CurioCategories] for the mapping. Two categories from the same
 * family share a format when the capture experience is similar
 * (Artists → SoundBite, Albums → ReelNotes; Films + Authors + Books
 * all share Marginalia since reading journals work the same way).
 */
enum class CaptureFormat {
    SoundBite,    // Voice note
    ReelNotes,    // Review + collage
    Marginalia,   // Journal + quotes
    GalleryWall,  // Moodboard
    FieldNotes,   // 3-section report
    OpenNotebook  // Wildcard: pick your own format
}