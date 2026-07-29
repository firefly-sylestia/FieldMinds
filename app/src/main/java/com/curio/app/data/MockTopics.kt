package com.curio.app.data

/**
 * Mock topic data for the Spin -> Topic Reveal -> Save/Capture -> Cabinet
 * -> Entry Detail flow.
 *
 * This is a placeholder for the real data layer that lands in Phase 4 (Room
 * + JSON seed flow). The shape mirrors [CurioTopic] + [ExploreAction] in
 * CURIO_DATA_PLAN.md section 2 so swapping to Room-backed data later is
 * a one-line change in the consuming screens.
 *
 * For now, this gives the Spin -> Reveal happy path something to actually
 * land on, and gives Cabinet + Entry Detail real-looking mock entries to
 * render. Every topic here is one of the 8 already authored in
 * `app/src/main/assets/topics/music.json` (commit ac9a0742) so the visual
 * fidelity is grounded in real content.
 *
 * @see CURIO_DATA_PLAN.md section 2 for the full schema this mirrors.
 */
object MockTopics {

    /**
     * One hardcoded sample topic used by the Spin -> Topic Reveal flow
     * for the prototype. The Spin screen picks one at random (or via
     * a weighted draw) from this pool. Phase 4 replaces this with the
     * real Room-backed query.
     */
    val samplePool: List<MockTopic> = listOf(
        MockTopic(
            id = "music-bjork-vespertine",
            categoryId = CategoryId.MUSIC,
            subtype = "Album",
            name = "Vespertine",
            teaser = "Björk's 2001 chamber-electronic album, mostly recorded alone in her Reykjavík home. The beats sit closer than they should.",
            imageUrl = "",
            actionPrompt = MockExploreAction(
                verb = "Listen",
                targetName = "Vespertine (2001) end-to-end",
                durationMinutes = 55,
                instruction = "Notice how the beats hit your chest vs your head — that's intentional. The album mixes orchestral and beat programming in a way most artists avoid."
            )
        ),
        MockTopic(
            id = "music-brian-eno-music-for-airports",
            categoryId = CategoryId.MUSIC,
            subtype = "Album",
            name = "Music for Airports",
            teaser = "Brian Eno's 1978 ambient landmark, composed to be heard (and ignored) in public spaces. The first record to call itself ambient.",
            imageUrl = "",
            actionPrompt = MockExploreAction(
                verb = "Listen",
                targetName = "Music for Airports (1978) end-to-end",
                durationMinutes = 48,
                instruction = "Put it on low in the background while you do something else. The piece is built to reveal itself only when you half-listen — try to catch each of the four loops phasing in and out of each other."
            )
        ),
        MockTopic(
            id = "music-mitski-be-the-cowboy",
            categoryId = CategoryId.MUSIC,
            subtype = "Album",
            name = "Be the Cowboy",
            teaser = "Mitski's 2018 album of tightly drawn vignettes, each under three minutes. It sounds sparse until you try to sing along.",
            imageUrl = "",
            actionPrompt = MockExploreAction(
                verb = "Listen",
                targetName = "Be the Cowboy (2018) end-to-end",
                durationMinutes = 33,
                instruction = "Pick one track that doesn't grab you on first listen. Play it three times back-to-back. Mitski writes songs that read flat on the surface — give them room to flip."
            )
        )
    )

    /** Pick a random topic from the pool — used by SpinScreen on landing. */
    fun randomPick(): MockTopic = samplePool.random()

    /**
     * Sample entry data for Cabinet — varied formats to exercise the
     * different card rendering shapes (voice note, text journal, quote).
     * Each entry has a hand-built "renderBody" so EntryDetail can show
     * format-specific content without needing Room or audio playback.
     */
    val sampleEntries: List<MockEntry> = listOf(
        MockEntry(
            id = "entry-1",
            topic = samplePool[0],
            capturedAtDaysAgo = 0,
            format = CaptureFormat.SoundBite,
            bodyPreview = "Voice note — 42s",
            bodyContent = "Played through Vespertine twice. The beats really do hit different — first listen the bass felt heavy in my headphones but on speakers the harpsichord comes through more. The production on \"Hidden Place\" is what got me — every layer is doing something different. Saved a clip of the final track; my note to myself was 're-listen in the bath'. Recommend."
        ),
        MockEntry(
            id = "entry-2",
            topic = samplePool[1],
            capturedAtDaysAgo = 2,
            format = CaptureFormat.Marginalia,
            bodyPreview = "\"The first record to call itself ambient.\"",
            bodyContent = "Brian Eno invented the term. Before this, music was either listened to actively or ignored. Music for Airports proposes a third mode — sound that shapes your experience of a space without demanding attention. Useful for any open-plan office, honestly."
        ),
        MockEntry(
            id = "entry-3",
            topic = samplePool[2],
            capturedAtDaysAgo = 5,
            format = CaptureFormat.ReelNotes,
            bodyPreview = "4 out of 5 — almost every track under 3 minutes.",
            bodyContent = "Mitski has this discipline of cutting every track right when you've had enough. No outros. The album is 31 minutes long and feels like 12 because of how much space is around each song. Be the Cowboy works because Mitski trusts the listener to fill in the rest."
        ),
        MockEntry(
            id = "entry-4",
            topic = samplePool[0],
            capturedAtDaysAgo = 14,
            format = CaptureFormat.SoundBite,
            bodyPreview = "Voice note — 1m 18s",
            bodyContent = "Read about how the album was recorded with a chamber choir inside an Icelandic fishing village's tiny church. The acoustic carries through on every track — you can hear the room. Try 'Undo' first; it builds like nothing else."
        )
    )
}

/**
 * Mock mirror of the CurioTopic schema from CURIO_DATA_PLAN.md section 2.
 * Used until Phase 4 wires the real Room-backed data layer.
 */
data class MockTopic(
    val id: String,
    val categoryId: CategoryId,
    val subtype: String,
    val name: String,
    val teaser: String,
    val imageUrl: String,
    val actionPrompt: MockExploreAction
)

/** Mock mirror of the ExploreAction schema (CURIO_DATA_PLAN.md section 2.1). */
data class MockExploreAction(
    val verb: String,
    val targetName: String,
    val durationMinutes: Int,
    val instruction: String
)

/**
 * One captured entry shown in the Cabinet grid + EntryDetail. The
 * [format] determines the card + detail rendering shape.
 */
data class MockEntry(
    val id: String,
    val topic: MockTopic,
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