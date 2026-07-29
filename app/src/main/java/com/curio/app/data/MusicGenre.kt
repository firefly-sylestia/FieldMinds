package com.curio.app.data

/**
 * Music genres — the multi-choice system for the Music category on the
 * Spin screen (per user directive: "for music add genre based with
 * multiple choice system for the spin").
 *
 * When the user picks Music in the Category Picker, the Spin screen
 * renders a horizontal chip row with these genres plus an "All Genres"
 * sentinel chip. Selecting a genre filters the music topic pool; tapping
 * "All Genres" clears the filter. The shuffle then picks from the
 * filtered pool.
 *
 * The "no filter" state is represented as `null` in [SpinScreen], NOT
 * as an enum value — this keeps the enum strictly about genre identity
 * and avoids the ambiguity of an `ALL` value that no [CurioTopic] could
 * ever carry (CurioTopic.init rejects non-Music topics with a genre
 * AND requires Music topics to declare one — so `ALL` could never be
 * assigned to any topic's musicGenre field).
 *
 * Genre choices are intentionally curated to the most distinctive
 * musical worlds rather than exhaustive — adding more genres fragments
 * the pool without meaningfully increasing variety. Each genre has at
 * least 4 topics in [TopicCatalog.musicPoolByGenre] so the picker
 * always has something to pick from.
 *
 * Wildcard does NOT have a genre system — it picks across all 6
 * categories randomly (see [TopicCatalog.wildcardPool]).
 */
enum class MusicGenre(val displayName: String, val glyph: String) {
    ROCK        ("Rock",          "electric_bolt"),
    JAZZ        ("Jazz",          "queue_music"),
    CLASSICAL   ("Classical",     "piano"),
    HIP_HOP     ("Hip-Hop",       "graphic_eq"),
    ELECTRONIC  ("Electronic",    "memory"),
    INDIE       ("Indie",         "radio"),
    FOLK        ("Folk",          "music_note"),
    WORLD       ("World",         "public"),
    R_AND_B     ("R&B / Soul",    "favorite");

    companion object {
        /** All genres in declared order so the chip row reads consistently across launches. */
        val all: List<MusicGenre> get() = values().toList()

        /** Total genre count for layout math. */
        val count: Int get() = values().size
    }
}