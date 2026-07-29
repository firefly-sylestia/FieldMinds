package com.curio.app.data

/**
 * The Curio topic catalog.
 *
 * Replaces the placeholder [MockTopics] sample pool. The catalog is
 * the source of truth for:
 *
 *  - [musicPoolAll] / [musicPoolByGenre] — all 50+ music topics,
 *    indexed by [MusicGenre] for the genre picker on the Spin screen.
 *  - [moviesPool] / [booksPool] / [artPool] / [sciencePool] — 15+
 *    topics each for the four non-Music named categories.
 *  - [wildcardPool] — 10 mixed topics for the Wildcard category.
 *  - [sampleEntries] — 4 mock cabinet entries for Cabinet +
 *    EntryDetail to render in the placeholder phase.
 *
 * Phase 4 (data layer) replaces this with a JSON-driven loader that
 * reads `assets/topics/{categoryId}.json` and constructs the same
 * [CurioTopic] instances. The schema stays identical so all consumers
 * (SpinScreen, TopicRevealScreen, SaveCaptureScreen, CabinetScreen,
 * EntryDetailScreen, TopicHistoryScreen) don't change.
 *
 * Authoring rules:
 *  - Real artist/work names, not placeholders.
 *  - Every topic has a concrete explore action (specific work to
 *    listen to / read / watch — not "some album").
 *  - Teasers are 1–2 sentences with a surprising angle; no
 *    Wikipedia-style bio copy.
 *  - Music topics MUST declare their [musicGenre] (enforced by
 *    CurioTopic.init).
 */
object TopicCatalog {

    // ─────────────────────────────────────────────────────────────────────
    //  MUSIC — 50 topics across 9 genres (+ ALL aggregator)
    // ─────────────────────────────────────────────────────────────────────

    private val musicAll: List<CurioTopic> = listOf(
        // Rock (6) ─────────────────────────────────────────────────────────
        CurioTopic(
            id = "music-bowie-ziggy",
            categoryId = CategoryId.MUSIC,
            subtype = "Album",
            name = "The Rise and Fall of Ziggy Stardust",
            teaser = "Bowie's 1972 concept album about a bisexual alien rock star — recorded in three weeks, half-written the morning of sessions, with a finale he later called \"the most emotional thing I've ever written.\"",
            imageUrl = "",
            musicGenre = MusicGenre.ROCK,
            exploreAction = ExploreAction("Listen", "\"Ziggy Stardust\" (1972) end-to-end", 38,
                "Save Side B for a second listen — \"Rock 'n' Roll Suicide\" closes the album with a slow build into Bowie's scream at the top of his range. It was the encore he played on every show until he retired Ziggy in 1973.")
        ),
        CurioTopic(
            id = "music-nirvana-nevermind",
            categoryId = CategoryId.MUSIC,
            subtype = "Album",
            name = "Nevermind",
            teaser = "Recorded for $65,000 in 1991 with a 23-year-old producer (Butch Vig) who later admitted he was \"making it up as we went.\" The album that ended hair metal and started a decade of guitar bands chasing it.",
            imageUrl = "",
            musicGenre = MusicGenre.ROCK,
            exploreAction = ExploreAction("Listen", "Nevermind (1991) end-to-end", 49,
                "Skip straight to \"In Utero\" after — Cobain's third album is his angriest, most experimental, and almost universally agreed to be his best. The contrast between the two back-to-back is where the story actually lives.")
        ),
        CurioTopic(
            id = "music-patti-smith-horses",
            categoryId = CategoryId.MUSIC,
            subtype = "Album",
            name = "Horses",
            teaser = "Smith's 1975 debut — produced by John Cale, mixed so dry you can hear her swallow between lines. The album Lou Reed told her \"isn't commercial enough\" and she should \"put some strings on it.\" She didn't.",
            imageUrl = "",
            musicGenre = MusicGenre.ROCK,
            exploreAction = ExploreAction("Listen", "Horses (1975) end-to-end", 41,
                "Read the Robert Mapplethorpe cover photo essay before pressing play. The portrait and the album are one object — both are about what it looks like to be young, broke, angry, and certain you're going to matter.")
        ),
        CurioTopic(
            id = "music-led-zeppelin-iv",
            categoryId = CategoryId.MUSIC,
            subtype = "Album",
            name = "Led Zeppelin IV",
            teaser = "The 1971 album the band refused to title (\"we've got to call it something\"), then refused to put a name on. \"Stairway to Heaven\" wasn't released as a single because they refused to edit it to radio length.",
            imageUrl = "",
            musicGenre = MusicGenre.ROCK,
            exploreAction = ExploreAction("Listen", "Led Zeppelin IV (1971) end-to-end", 42,
                "Listen on headphones with the volume low. The album was mixed for FM radio, not audiophile systems — the magic is in the midrange, where Plant's vocals sit just above Page's guitar. Turn it up until you can feel the kick drum in your chest.")
        ),
        CurioTopic(
            id = "music-radiohead-kid-a",
            categoryId = CategoryId.MUSIC,
            subtype = "Album",
            name = "Kid A",
            teaser = "Yorke wrote most of it on a keyboard he couldn't play, in a room with the lights off. The band spent six months learning the songs before recording. Radiohead's contract required one more rock album; they delivered one with no guitars on half the tracks.",
            imageUrl = "",
            musicGenre = MusicGenre.ROCK,
            exploreAction = ExploreAction("Listen", "Kid A (2000) end-to-end", 50,
                "Skip to track 4, \"Everything In Its Right Place.\" The opening track is the thesis — the same four-note loop, repeated, with Yorke singing words that don't quite parse. Listen to it three times. The meaning changes each time.")
        ),
        CurioTopic(
            id = "music-fleetwood-mac-tango",
            categoryId = CategoryId.MUSIC,
            subtype = "Album",
            name = "Tango in the Night",
            teaser = "Recorded during the band's breakup era — Buckingham quit mid-session, was lured back, then quit again. The album that turned out to be their biggest commercial hit. Stevie Nicks' vocal on \"Everywhere\" is one take.",
            imageUrl = "",
            musicGenre = MusicGenre.ROCK,
            exploreAction = ExploreAction("Listen", "Tango in the Night (1987) end-to-end", 41,
                "Pair this with the Mick Fleetwood oral history (\"Fleetwood Mac: The Definitive Collection\" liner notes). The album sounds like sunshine; the making-of is pure soap opera.")
        ),

        // Jazz (5) ──────────────────────────────────────────────────────────
        CurioTopic(
            id = "music-coltrane-love-supreme",
            categoryId = CategoryId.MUSIC,
            subtype = "Album",
            name = "A Love Supreme",
            teaser = "Coltrane's 1965 four-part suite — recorded in one session after a spiritual crisis the year before. He wrote \"Acknowledgement\" with one note repeating under the saxophone line; that note was tuned to A — every other musician on the session retuned to match.",
            imageUrl = "",
            musicGenre = MusicGenre.JAZZ,
            exploreAction = ExploreAction("Listen", "A Love Supreme (1965) end-to-end", 33,
                "Don't try to parse the solos — try to follow the SHAPE of them. Coltrane starts each chorus with a melodic phrase, then drifts further from it. Listen for the moment he stops drifting and comes back.")
        ),
        CurioTopic(
            id = "music-miles-davis-kind-of-blue",
            categoryId = CategoryId.MUSIC,
            subtype = "Album",
            name = "Kind of Blue",
            teaser = "Recorded in 1959 with two sessions of first or second takes — Davis told the band \"I'll play the melody and we'll see what happens.\" It's the best-selling jazz album ever because nothing on it sounds improvised, even though almost everything is.",
            imageUrl = "",
            musicGenre = MusicGenre.JAZZ,
            exploreAction = ExploreAction("Listen", "Kind of Blue (1959) end-to-end", 56,
                "Listen to \"Blue in Green\" twice — once as music, once as a conversation between Bill Evans' piano and Miles' trumpet. They're not trading phrases; they're answering the same question from different angles.")
        ),
        CurioTopic(
            id = "music-monk-genius-of-modern-music",
            categoryId = CategoryId.MUSIC,
            subtype = "Album",
            name = "Genius of Modern Music Vol. 1",
            teaser = "Monk's 1951 debut as a leader for Blue Note. Eight tracks recorded in two sessions. The piano is slightly out of tune. The drummer keeps dropping beats. It's the most Monk album ever made.",
            imageUrl = "",
            musicGenre = MusicGenre.JAZZ,
            exploreAction = ExploreAction("Listen", "Genius of Modern Music Vol. 1 (1951) end-to-end", 38,
                "Start with \"'Round Midnight.\" Monk plays the melody almost off-key, on purpose. The song was a jazz standard — he slows it down, makes it heavier, until the original feels like a sketch for what Monk is doing.")
        ),
        CurioTopic(
            id = "music-bill-evans-conversation",
            categoryId = CategoryId.MUSIC,
            subtype = "Album",
            name = "Conversations with Myself",
            teaser = "Bill Evans' 1963 album of solo piano, recorded by overdubbing two tracks of himself playing simultaneously — the same piece, played twice, with one track delayed by a fraction of a second. The result is a single pianist who sounds like a duet.",
            imageUrl = "",
            musicGenre = MusicGenre.JAZZ,
            exploreAction = ExploreAction("Listen", "Conversations with Myself (1963) end-to-end", 41,
                "Headphones. Evans plays a melody with his right hand and a harmony with his left — then the overdub plays the SAME melody, slightly delayed, with the harmony shifted. You're hearing one person argue with themselves in real time.")
        ),
        CurioTopic(
            id = "music-herbie-headhunters",
            categoryId = CategoryId.MUSIC,
            subtype = "Album",
            name = "Head Hunters",
            teaser = "Hancock's 1973 pivot from acoustic piano to Fender Rhodes electric — made because Miles Davis told him \"you've got to get funky or die.\" The album is one long funk groove with jazz improvisation glued on top.",
            imageUrl = "",
            musicGenre = MusicGenre.JAZZ,
            exploreAction = ExploreAction("Listen", "Head Hunters (1973) end-to-end", 45,
                "Skip to \"Chameleon.\" The bassline is the song — seventeen notes, repeated for nine minutes. Hancock solos over the top of it. The whole genre of jazz-funk starts with this bassline and everything else is commentary.")
        ),

        // Classical (5) ─────────────────────────────────────────────────────
        CurioTopic(
            id = "music-bach-goldberg",
            categoryId = CategoryId.MUSIC,
            subtype = "Composition",
            name = "Goldberg Variations",
            teaser = "Bach's 1741 set of 30 variations on a single bass line. The story — likely apocryphal — is that Bach wrote them to be played by a teenage insomniac named Goldberg to help Count Kaiserling sleep. They work the opposite way.",
            imageUrl = "",
            musicGenre = MusicGenre.CLASSICAL,
            exploreAction = ExploreAction("Listen", "Glenn Gould's 1981 recording of Goldberg Variations", 51,
                "Gould recorded the Goldbergs twice — in 1955 (age 22) and 1981 (age 49). Both are considered definitive. Start with the 1981 recording; it's slower, more deliberate, more like a conversation. Then try the 1955 — it's twice as fast, with the same notes.")
        ),
        CurioTopic(
            id = "music-stravinsky-rite-of-spring",
            categoryId = CategoryId.MUSIC,
            subtype = "Composition",
            name = "The Rite of Spring",
            teaser = "1913 Paris premiere caused a near-riot — Stravinsky's rhythm was so unusual audiences thought the orchestra was playing wrong. The choreography (a pagan fertility ritual) made it worse. A century later, the opening bassoon solo is in every film trailer about danger.",
            imageUrl = "",
            musicGenre = MusicGenre.CLASSICAL,
            exploreAction = ExploreAction("Listen", "Stravinsky: The Rite of Spring — any major recording", 35,
                "Listen for the rhythmic patterns, not the melody. The Rite is built on uneven accents — 3+3+2 patterns that don't line up with your body's expectation of where the next beat should be. The discomfort is the point.")
        ),
        CurioTopic(
            id = "music-debussy-clair-de-lune",
            categoryId = CategoryId.MUSIC,
            subtype = "Composition",
            name = "Suite bergamasque (\"Clair de Lune\")",
            teaser = "Debussy wrote the suite in 1890 but didn't publish it until 1905 — by which point \"Clair de Lune\" (the third movement) sounded old-fashioned to him. He was wrong. It's now the most-played classical piano piece in the world.",
            imageUrl = "",
            musicGenre = MusicGenre.CLASSICAL,
            exploreAction = ExploreAction("Listen", "Debussy: Suite bergamasque — Claudio Arrau recording", 18,
                "Play it in the dark. Debussy was a Symbolist — he wanted his music to evoke feelings and impressions rather than tell stories. \"Clair de Lune\" doesn't go anywhere; it just sits in one place and gets more beautiful.")
        ),
        CurioTopic(
            id = "music-satie-gymnopedies",
            categoryId = CategoryId.MUSIC,
            subtype = "Composition",
            name = "3 Gymnopédies",
            teaser = "Satie's 1888 set of three short piano pieces — the first one has been in every ambient playlist ever made. Satie wrote them as \"dances of naked youths,\" then mostly dropped out of music for 27 years and composed furniture music.",
            imageUrl = "",
            musicGenre = MusicGenre.CLASSICAL,
            exploreAction = ExploreAction("Listen", "Satie: 3 Gymnopédies — Alexandre Tharaud recording", 13,
                "Each piece is about 3 minutes long. Listen to all three back-to-back without pause. They're meant to be heard as a single thought — slow, contemplative, ending in the middle of a sentence.")
        ),
        CurioTopic(
            id = "music-philip-glass-glassworks",
            categoryId = CategoryId.MUSIC,
            subtype = "Album",
            name = "Glassworks",
            teaser = "Glass' 1982 attempt to make minimalism accessible — six short pieces, none over six minutes, intended as an entry point. \"Opening\" from the album has been in nearly every commercial about hope since.",
            imageUrl = "",
            musicGenre = MusicGenre.CLASSICAL,
            exploreAction = ExploreAction("Listen", "Glassworks (1982) end-to-end", 33,
                "Notice how the pieces move. Glass writes in additive patterns — one note, then two, then three, then four, each group longer than the last. Listen for the moment when the pattern shifts from one group to the next.")
        ),

        // Hip-Hop (5) ───────────────────────────────────────────────────────
        CurioTopic(
            id = "music-kendrick-to-pimp-a-butterfly",
            categoryId = CategoryId.MUSIC,
            subtype = "Album",
            name = "To Pimp a Butterfly",
            teaser = "Kendrick Lamar's 2015 album recorded with live jazz musicians from Thundercat's circle — not a hip-hop album with live instruments, but a jazz album that happens to have a rapper on top. It won the Pulitzer for music in 2018, the first non-classical, non-jazz work to do so.",
            imageUrl = "",
            musicGenre = MusicGenre.HIP_HOP,
            exploreAction = ExploreAction("Listen", "To Pimp a Butterfly (2015) end-to-end", 79,
                "Skip to \"King Kunta.\" The Isley Brothers sample is the foundation; Kendrick's verse is the melody. Listen to the bassline — it's a jazz bassline, not a hip-hop one. The track swings. Then play any Kendrick song from before this album and notice how different the groove is.")
        ),
        CurioTopic(
            id = "music-outkast-aquemini",
            categoryId = CategoryId.MUSIC,
            subtype = "Album",
            name = "Aquemini",
            teaser = "OutKast's 1998 third album — named for the zodiac signs of the two rappers (Aquarius + Gemini). Recorded over a year and a half while their label didn't believe in them; it became a 5x platinum record. \"SpottieOttieDopaliscious\" is on it.",
            imageUrl = "",
            musicGenre = MusicGenre.HIP_HOP,
            exploreAction = ExploreAction("Listen", "Aquemini (1998) end-to-end", 70,
                "Big Boi and André 3000 trade verses — they don't collaborate, they alternate. Listen for the contrast: Big Boi's bars are technical and Southern, André's are cosmic and abstract. The album works because you get both perspectives.")
        ),
        CurioTopic(
            id = "music-lauryn-hill-miseducation",
            categoryId = CategoryId.MUSIC,
            subtype = "Album",
            name = "The Miseducation of Lauryn Hill",
            teaser = "Hill's 1998 solo debut — the first hip-hop album to win Album of the Year at the Grammys. She was 23. It took five years to make. She has not released a proper follow-up in 25+ years.",
            imageUrl = "",
            musicGenre = MusicGenre.HIP_HOP,
            exploreAction = ExploreAction("Listen", "The Miseducation of Lauryn Hill (1998) end-to-end", 64,
                "Skip to \"Ex-Factor.\" Hill wrote it about Wyclef Jean (her bandmate in the Fugees, and the father of one of her children). The production is sparse — just a guitar loop and her voice. It's one of the great break-up songs.")
        ),
        CurioTopic(
            id = "music-mf-doom-operation",
            categoryId = CategoryId.MUSIC,
            subtype = "Album",
            name = "Operation: Doomsday",
            teaser = "MF DOOM's 1999 comeback album after the death of his brother DJ Subroc — released under his supervillain persona, with every sample flipped or replayed from scratch. He's rapping as a character, but the grief underneath is real.",
            imageUrl = "",
            musicGenre = MusicGenre.HIP_HOP,
            exploreAction = ExploreAction("Listen", "Operation: Doomsday (1999) end-to-end", 50,
                "DOOM's production is dense — every track has 3-4 sampled sources playing simultaneously. Listen on speakers, not headphones. The bassline on \"The Time We Faced Doom\" (the closer) is sampled from a 1970s TV theme — recognize it before the verse ends.")
        ),
        CurioTopic(
            id = "music-madlib-quasimoto",
            categoryId = CategoryId.MUSIC,
            subtype = "Album",
            name = "The Unseen",
            teaser = "Madlib's 2000 album as Quasimoto — a character who raps in a pitched-up voice with a jazz-detective concept. The beats are looped from obscure vinyl (mostly 1960s-70s soul, jazz, and film scores). Madlib produced most of it while living in a hotel room in Hawaii.",
            imageUrl = "",
            musicGenre = MusicGenre.HIP_HOP,
            exploreAction = ExploreAction("Listen", "Quasimoto: The Unseen (2000) end-to-end", 55,
                "Try to identify the samples. Madlib flips from 6-7 sources per track and rarely loops more than 2 bars. The pitch-shifted vocal takes getting used to — but the production underneath is some of the most inventive hip-hop ever made.")
        ),

        // Electronic (5) ────────────────────────────────────────────────────
        CurioTopic(
            id = "music-aphex-twin-selected",
            categoryId = CategoryId.MUSIC,
            subtype = "Album",
            name = "Selected Ambient Works 85-92",
            teaser = "Richard D. James' first release as Aphex Twin — recorded on a $300 Atari ST when he was 17-22 years old. The beats sound digital because they are. The album proved ambient music could be a hit.",
            imageUrl = "",
            musicGenre = MusicGenre.ELECTRONIC,
            exploreAction = ExploreAction("Listen", "Aphex Twin: Selected Ambient Works 85-92 end-to-end", 75,
                "Put it on while doing something else. The album was designed for half-listening — the foreground melodies are the hook, but the background textures are the point. James wanted music that worked whether you paid attention or not.")
        ),
        CurioTopic(
            id = "music-brian-eno-airports",
            categoryId = CategoryId.MUSIC,
            subtype = "Album",
            name = "Music for Airports",
            teaser = "Eno's 1978 album — the first record to call itself \"ambient.\" Four loops, each 5-7 minutes long, designed to be played at low volume in airport terminals so the sound blends into the environment. Eno designed the loops to interact — each can be combined with the others.",
            imageUrl = "",
            musicGenre = MusicGenre.ELECTRONIC,
            exploreAction = ExploreAction("Listen", "Eno: Music for Airports (1978) end-to-end", 48,
                "Listen to \"1/1\" four times. The track is the same five-note melody, repeated — but Eno shifts the timing so the loops drift apart, then back together. The \"music\" is in the drift.")
        ),
        CurioTopic(
            id = "music-kraftwerk-computer-world",
            categoryId = CategoryId.MUSIC,
            subtype = "Album",
            name = "Computer World",
            teaser = "Kraftwerk's 1981 album about computers, recorded mostly on custom-built electronic instruments. The track \"Numbers\" is 3 minutes of a synthesizer counting from one to ten in German. It predicted everything about electronic music for the next 40 years.",
            imageUrl = "",
            musicGenre = MusicGenre.ELECTRONIC,
            exploreAction = ExploreAction("Listen", "Kraftwerk: Computer World (1981) end-to-end", 34,
                "Listen for the influence. Almost everything in modern electronic music — Daft Punk, Aphex Twin, synthpop, EDM — traces back to this album. It's not retroactively great; it was just ahead of its time.")
        ),
        CurioTopic(
            id = "music-burial-untrue",
            categoryId = CategoryId.MUSIC,
            subtype = "Album",
            name = "Untrue",
            teaser = "Burial's 2007 debut album of garage + 2-step + ambient + crackle. The crackle is the sound of vinyl rips sampled and put back in the mix. The album arrived in the middle of indie sleaze and sounded like the future.",
            imageUrl = "",
            musicGenre = MusicGenre.ELECTRONIC,
            exploreAction = ExploreAction("Listen", "Burial: Untrue (2007) end-to-end", 50,
                "Don't skip ahead. The album builds over 13 tracks — the beats get more abstract, the vocals more distant, the textures thicker. By the final track (\"Raver\") you're 6 minutes into ambient fog. Listen in order.")
        ),
        CurioTopic(
            id = "music-boards-of-canada-maccoroni",
            categoryId = CategoryId.MUSIC,
            subtype = "Album",
            name = "Music Has the Right to Children",
            teaser = "Boards of Canada's 1998 debut — built from samples of public-domain films, children's TV, and obscure 1970s folk records. The production feels like a memory you can't quite place.",
            imageUrl = "",
            musicGenre = MusicGenre.ELECTRONIC,
            exploreAction = ExploreAction("Listen", "Boards of Canada: Music Has the Right to Children (1998) end-to-end", 64,
                "Listen for the voices. Throughout the album there are snippets of children speaking, old BBC broadcasts, and other sampled speech — most are in the background, but a few are mixed loud enough to make you listen carefully.")
        ),

        // Indie (5) ─────────────────────────────────────────────────────────
        CurioTopic(
            id = "music-sufjan-michigan",
            categoryId = CategoryId.MUSIC,
            subtype = "Album",
            name = "Michigan",
            teaser = "Sufjan Stevens' 2003 album — 21 songs about his home state, the first in a planned (but unfinished) series of all 50 US states. The arrangements use flutes, brass, and a banjo. The songs range from 1:30 to 11 minutes.",
            imageUrl = "",
            musicGenre = MusicGenre.INDIE,
            exploreAction = ExploreAction("Listen", "Sufjan Stevens: Michigan (2003) end-to-end", 75,
                "Track 1 is the obvious start. Track 2 (\"All Good Naysayers, Speak Up!\") is the curveball — 4 minutes of ecstatic chanting layered over a single chord. Stevens says the album has no central theme; the geography IS the theme.")
        ),
        CurioTopic(
            id = "music-arcade-fire-funeral",
            categoryId = CategoryId.MUSIC,
            subtype = "Album",
            name = "Funeral",
            teaser = "Arcade Fire's 2004 debut — written in the year the band's family lost three grandparents to death. The album sounds like an emergency and a celebration happening at the same time. \"Wake Up\" has been used in dozens of films and Super Bowl commercials.",
            imageUrl = "",
            musicGenre = MusicGenre.INDIE,
            exploreAction = ExploreAction("Listen", "Arcade Fire: Funeral (2004) end-to-end", 50,
                "Headphones. The album has more than 20 musicians on it at times — strings, brass, accordion, hurdy-gurdy. Listen to the layers. \"Neighborhood #1 (Tunnels)\" starts with just a piano and builds to a wall of sound by the end.")
        ),
        CurioTopic(
            id = "music-big-thief-masterpiece",
            categoryId = CategoryId.MUSIC,
            subtype = "Album",
            name = "Masterpiece",
            teaser = "Big Thief's 2016 debut — recorded live in 5 days with producer James Krivchenia. The lead singer Adrianne Lenker writes songs about dogs, insects, and geological features with the same emotional weight as songs about death.",
            imageUrl = "",
            musicGenre = MusicGenre.INDIE,
            exploreAction = ExploreAction("Listen", "Big Thief: Masterpiece (2016) end-to-end", 45,
                "Listen to \"Real People.\" Lenker wrote it about a friend who died by suicide. Then listen to \"BEASTIAL.\" Then notice that the same singer wrote both. Lenker's range — tonal, emotional, lyrical — is the band.")
        ),
        CurioTopic(
            id = "music-mitski-be-the-cowboy",
            categoryId = CategoryId.MUSIC,
            subtype = "Album",
            name = "Be the Cowboy",
            teaser = "Mitski's 2018 album of tightly drawn vignettes, each under three minutes. It sounds sparse until you try to sing along — then you realize every lyric is a complete thought, not a fragment.",
            imageUrl = "",
            musicGenre = MusicGenre.INDIE,
            exploreAction = ExploreAction("Listen", "Mitski: Be the Cowboy (2018) end-to-end", 33,
                "Pick one track that doesn't grab you on first listen. Play it three times back-to-back. Mitski writes songs that read flat on the surface — give them room to flip.")
        ),
        CurioTopic(
            id = "music-bon-iver-22-a-million",
            categoryId = CategoryId.MUSIC,
            subtype = "Album",
            name = "22, A Million",
            teaser = "Bon Iver's 2016 third album — Justin Vernon reworked every vocal through custom processing software and autotune, ending up with songs that sound human and alien at the same time. The album cover is a series of abstract shapes; Vernon won't explain what they mean.",
            imageUrl = "",
            musicGenre = MusicGenre.INDIE,
            exploreAction = ExploreAction("Listen", "Bon Iver: 22, A Million (2016) end-to-end", 35,
                "Listen to the vocals. Vernon pitched and processed each one — sometimes the same word appears multiple times, processed differently each time. The songs aren't about lyrics in the usual sense; they're about textures.")
        ),

        // Folk (4) ──────────────────────────────────────────────────────────
        CurioTopic(
            id = "music-nick-drake-pink-moon",
            categoryId = CategoryId.MUSIC,
            subtype = "Album",
            name = "Pink Moon",
            teaser = "Drake's 1972 final album — 11 songs, 28 minutes, just voice and acoustic guitar. Recorded in two sessions. The album cover is a painting of a pink moon and stars by Drake himself. Two years later, at 26, he was dead.",
            imageUrl = "",
            musicGenre = MusicGenre.FOLK,
            exploreAction = ExploreAction("Listen", "Nick Drake: Pink Moon (1972) end-to-end", 28,
                "Don't try to figure out what the songs are about. Drake wrote in a private language — fragments of thoughts, half-images. The album works because the sound itself is the message: young, quiet, alone, singing to no one in particular.")
        ),
        CurioTopic(
            id = "music-joni-mitchell-blue",
            categoryId = CategoryId.MUSIC,
            subtype = "Album",
            name = "Blue",
            teaser = "Mitchell's 1971 album — written after a breakup with Graham Nash, traveling around Europe, and a brief relationship with Leonard Cohen. The album reads like a diary because it almost was: Mitchell said she \"couldn't hold back any of it\" in the songs.",
            imageUrl = "",
            musicGenre = MusicGenre.FOLK,
            exploreAction = ExploreAction("Listen", "Joni Mitchell: Blue (1971) end-to-end", 36,
                "The album is famously intimate. Mitchell's guitar tunings are unconventional — she tuned down a half-step on most tracks, and used open D for \"California\" and other songs. You can hear the strings vibrating against the frets.")
        ),
        CurioTopic(
            id = "music-bon-iver-emma",
            categoryId = CategoryId.MUSIC,
            subtype = "Album",
            name = "For Emma, Forever Ago",
            teaser = "Bon Iver's 2007 debut — written by Justin Vernon alone in a cabin in northern Wisconsin after a breakup, a breakup with his band, and a bout of mono. The album was meant to be private; he sent copies to friends, who uploaded it.",
            imageUrl = "",
            musicGenre = MusicGenre.FOLK,
            exploreAction = ExploreAction("Listen", "Bon Iver: For Emma, Forever Ago (2007) end-to-end", 38,
                "Listen to \"Skinny Love.\" It's the song that broke the album out — Damien Rice covered it; the Lumineers covered it; everyone has heard the chorus. The album version is sparse: Vernon's voice, an acoustic guitar, and almost nothing else.")
        ),
        CurioTopic(
            id = "music-adrianne-lenker-songs",
            categoryId = CategoryId.MUSIC,
            subtype = "Album",
            name = "songs",
            teaser = "Big Thief's Adrianne Lenker's 2020 solo album — recorded in a cabin in western Massachusetts with just a few microphones and her guitar. The album came out the same week as the dissolution of her long-term relationship.",
            imageUrl = "",
            musicGenre = MusicGenre.FOLK,
            exploreAction = ExploreAction("Listen", "Adrianne Lenker: songs (2020) end-to-end", 41,
                "Headphones are mandatory. The recording is so close you can hear the room — birds outside, the chair Lenker's sitting on. Listen to \"anything\" — the song cycles through 4 minutes of Lenker singing four lines over and over.")
        ),

        // World (4) ─────────────────────────────────────────────────────────
        CurioTopic(
            id = "music-fela-kuti-zombie",
            categoryId = CategoryId.MUSIC,
            subtype = "Album",
            name = "Zombie",
            teaser = "Fela Kuti's 1977 protest album — a direct attack on the Nigerian military. The government responded by raiding his compound, killing his mother, and burning his studio. He moved to a new compound and kept recording.",
            imageUrl = "",
            musicGenre = MusicGenre.WORLD,
            exploreAction = ExploreAction("Listen", "Fela Kuti: Zombie (1977) end-to-end", 75,
                "Don't skip — the album is one continuous 25-minute track on each side. The groove is hypnotic. Fela's band (Africa 70) is one of the tightest funk outfits ever recorded. By the end of \"Zombie\" you'll understand why the government was scared.")
        ),
        CurioTopic(
            id = "music-buena-vista-buena-vista",
            categoryId = CategoryId.MUSIC,
            subtype = "Album",
            name = "Buena Vista Social Club",
            teaser = "1997 album of Cuban musicians in their 60s, 70s, and 80s — most hadn't recorded in decades. Compay Segundo was 89. Ibrahim Ferrer was 70. Ry Cooder produced. The album was recorded in six days.",
            imageUrl = "",
            musicGenre = MusicGenre.WORLD,
            exploreAction = ExploreAction("Listen", "Buena Vista Social Club (1997) end-to-end", 60,
                "Listen to Ibrahim Ferrer's vocal on \"Dos Gardenias.\" He recorded the vocal in one take, in the dark, with the lights off because he didn't like the fluorescent lighting in the studio. The song is 3 minutes long and one of the great love songs of the century.")
        ),
        CurioTopic(
            id = "music-cesaria-evora-sodade",
            categoryId = CategoryId.MUSIC,
            subtype = "Album",
            name = "Cape Verdean Beauty",
            teaser = "Cesária Évora's 1992 album — recorded in Paris after she'd already retired once from music. The album made her famous at 51. She's called \"the Barefoot Diva\" because she performed without shoes.",
            imageUrl = "",
            musicGenre = MusicGenre.WORLD,
            exploreAction = ExploreAction("Listen", "Cesária Évora: Cape Verdean Beauty (1992) end-to-end", 51,
                "Évora sang in Cape Verdean Creole, not Portuguese — her audience couldn't always understand the lyrics. The songs work because the emotion in her voice is unmistakable. Start with \"Sodade\" (which is the Cape Verdean word for longing).")
        ),
        CurioTopic(
            id = "music-tinariwen-amassakoul",
            categoryId = CategoryId.MUSIC,
            subtype = "Album",
            name = "Amassakoul",
            teaser = "Tinariwen's 2004 album — Tuareg guitar music from the Sahara desert. The band formed in refugee camps in the 1980s, learned to play by recording Libyan radio broadcasts, and fought in a Saharan rebellion before becoming internationally famous.",
            imageUrl = "",
            musicGenre = MusicGenre.WORLD,
            exploreAction = ExploreAction("Listen", "Tinariwen: Amassakoul (2004) end-to-end", 45,
                "The music is guitar-driven but the rhythms are percussive. The guitar parts are modal — listen for the repeating figures that don't resolve in the way Western music expects. The desert gets into the music.")
        ),

        // R&B / Soul (4) ────────────────────────────────────────────────────
        CurioTopic(
            id = "music-dangelo-voodoo",
            categoryId = CategoryId.MUSIC,
            subtype = "Album",
            name = "Voodoo",
            teaser = "D'Angelo's 2000 second album — recorded over five years with the Questlove, Charlie Hunter, and a rotating cast of musicians. D'Angelo played every instrument himself except drums. The album stalled at #33 — but everyone in R&B copied it for the next decade.",
            imageUrl = "",
            musicGenre = MusicGenre.R_AND_B,
            exploreAction = ExploreAction("Listen", "D'Angelo: Voodoo (2000) end-to-end", 63,
                "Listen for the bass. D'Angelo insisted on recording the bass last so it could move around the rhythm. \"Left & Right\" features ?uestlove drumming in a pocket that shouldn't work but does. The album is heavy.")
        ),
        CurioTopic(
            id = "music-solange-seat-at-the-table",
            categoryId = CategoryId.MUSIC,
            subtype = "Album",
            name = "A Seat at the Table",
            teaser = "Solange Knowles' 2016 album — released the same year Beyoncé released Lemonade. Solange's album is the quieter, more radical one. It's a meditation on being a Black woman in America, with interludes from Master P and Tina Lawson (her mother).",
            imageUrl = "",
            musicGenre = MusicGenre.R_AND_B,
            exploreAction = ExploreAction("Listen", "Solange: A Seat at the Table (2016) end-to-end", 50,
                "Listen to \"Cranes in the Sky.\" Solange wrote it over seven years — the song is about trying to outrun depression with a shopping spree, a new apartment, a new city. The cure never works. The song is the realization.")
        ),
        CurioTopic(
            id = "music-erykah-badu-bad",
            categoryId = CategoryId.MUSIC,
            subtype = "Album",
            name = "Baduizm",
            teaser = "Badu's 1997 debut — recorded mostly live in the studio with a 9-piece band. The album defines what became called \"neo-soul\" — the album isn't retro, but it's deeply rooted in 1970s soul. The single \"On & On\" made her an icon at 26.",
            imageUrl = "",
            musicGenre = MusicGenre.R_AND_B,
            exploreAction = ExploreAction("Listen", "Erykah Badu: Baduizm (1997) end-to-end", 65,
                "Skip to \"Tyrone.\" It's the breakup song where she tells her ex to call his friends and ask them for money so he can take her out properly. The line \"I'm not your movie\" is the most quoted lyric from the album.")
        ),
        CurioTopic(
            id = "music-sza-ctrl",
            categoryId = CategoryId.MUSIC,
            subtype = "Album",
            name = "Ctrl",
            teaser = "SZA's 2017 debut — five years in the making, recorded across three labels, with frequent leaks and rejections. The album is short (50 minutes), personal, and won her four Grammy nominations. The track \"Love Galore\" features Travis Scott.",
            imageUrl = "",
            musicGenre = MusicGenre.R_AND_B,
            exploreAction = ExploreAction("Listen", "SZA: Ctrl (2017) end-to-end", 50,
                "Listen to \"Drew Barrymore.\" The song is about being in a casual relationship and wanting more. The chorus (\"You make me love you / Make me hate you\") is the entire album in two lines.")
        )
    )

    // ─────────────────────────────────────────────────────────────────────
    //  MOVIES — 15 topics
    // ─────────────────────────────────────────────────────────────────────

    private val moviesAll: List<CurioTopic> = listOf(
        CurioTopic(
            id = "movies-kubrick-2001",
            categoryId = CategoryId.MOVIES,
            subtype = "Film",
            name = "2001: A Space Odyssey",
            teaser = "Kubrick's 1968 film — four years in production, with NASA advising on zero-gravity sequences. The ape-men in the opening act are real people in fur suits filmed under hot lights with no dialogue for 25 minutes.",
            imageUrl = "",
            exploreAction = ExploreAction("Watch", "2001: A Space Odyssey (1968) — full film", 161,
                "Watch the first 25 minutes with no skipping. The \"Dawn of Man\" sequence is one of the great achievements of cinema — apes discovering tools, all done with no dialogue and original music. The first cut Kubrick made was 4 hours; this version is already trimmed.")
        ),
        CurioTopic(
            id = "movies-kubrick-shining",
            categoryId = CategoryId.MOVIES,
            subtype = "Film",
            name = "The Shining",
            teaser = "Kubrick's 1980 horror film — shot at Elstree Studios over a year. The famous hedge maze cost $1M to build. The script was co-written by Diane Johnson with Kubrick; King disliked the film but admitted it has \"moments of greatness.\"",
            imageUrl = "",
            exploreAction = ExploreAction("Watch", "The Shining (1980) — full film", 146,
                "Watch the Steadicam shots carefully. Kubrick invented a new shot-tracking system to film Danny's tricycle rides through the hotel — the camera floats through corridors that aren't real (they're sets on a soundstage). Watch for the impossible window at the end.")
        ),
        CurioTopic(
            id = "movies-lynch-mulholland",
            categoryId = CategoryId.MOVIES,
            subtype = "Film",
            name = "Mulholland Drive",
            teaser = "Lynch's 2001 film — began as a TV pilot for ABC, rejected by the network, then expanded into a 2.5-hour film. Naomi Watts stars as a Hollywood hopeful who befriends an amnesiac woman. The second half is a different movie.",
            imageUrl = "",
            exploreAction = ExploreAction("Watch", "Mulholland Drive (2001) — full film", 147,
                "Pause at the 90-minute mark. The film rewrites itself — what you've been watching for the first half is a dream; the second half is the dreamer. Watch both halves again, knowing what you know. There are at least 4 interpretations.")
        ),
        CurioTopic(
            id = "movies-lynch-blue-velvet",
            categoryId = CategoryId.MOVIES,
            subtype = "Film",
            name = "Blue Velvet",
            teaser = "Lynch's 1986 mystery — Dennis Hopper plays the villain Frank Booth, who keeps a mask on a nightstand. The film is about the discovery of darkness in suburban America. Isabella Rossellini was paid $35,000 to act in scenes that ended her marriage.",
            imageUrl = "",
            exploreAction = ExploreAction("Watch", "Blue Velvet (1986) — full film", 120,
                "Don't skip. The opening credits set up the entire film: red velvet curtains, blue sky, the camera descends through them. The middle 90 minutes are a noir mystery. The ending is a scene that shouldn't be able to work but does.")
        ),
        CurioTopic(
            id = "movies-coppola-godfather",
            categoryId = CategoryId.MOVIES,
            subtype = "Film",
            name = "The Godfather",
            teaser = "Coppola's 1972 film — based on a bestseller Mario Puzo wrote in 18 months. Brando improvised the cat-in-lap gesture; it wasn't in the script. The film ran for 2 hours 50 minutes and most theaters left an intermission.",
            imageUrl = "",
            exploreAction = ExploreAction("Watch", "The Godfather (1972) — Part I, full film", 175,
                "Watch the door scene. The opening is the film — a man comes to ask a favor, gets turned down, is killed. The entire movie is the world's reaction to that door. Listen for the music — Nino Rota's score tells you what to feel before you know what you're watching.")
        ),
        CurioTopic(
            id = "movies-coppola-godfather-2",
            categoryId = CategoryId.MOVIES,
            subtype = "Film",
            name = "The Godfather Part II",
            teaser = "Coppola's 1974 sequel — the only sequel to win Best Picture. The film alternates between young Vito Corleone (De Niro, who learned Sicilian for the role) and his son Michael (Pacino). They're mirror images of each other.",
            imageUrl = "",
            exploreAction = ExploreAction("Watch", "The Godfather Part II (1974) — full film", 202,
                "Watch the young Vito sequences first; they're chronological. Then the Michael sequences show the same arc in reverse — Vito builds, Michael destroys. The film's final shot is a close-up that tells you everything that happened.")
        ),
        CurioTopic(
            id = "movies-wong-chungking-express",
            categoryId = CategoryId.MOVIES,
            subtype = "Film",
            name = "Chungking Express",
            teaser = "Wong Kar-wai's 1994 film — two stories of Hong Kong police officers dealing with heartbreak, shot in 23 days on leftover sets from a different movie. The director used step-printing (a technique that repeats single frames) to give the film its dreamy look.",
            imageUrl = "",
            exploreAction = ExploreAction("Watch", "Chungking Express (1994) — full film", 102,
                "Don't confuse it for an action film. The \"Chungking Mansions\" of the title is a real apartment block in Hong Kong. The two stories don't connect — they're meant to be experienced in sequence like a double feature.")
        ),
        CurioTopic(
            id = "movies-tarkovsky-stalker",
            categoryId = CategoryId.MOVIES,
            subtype = "Film",
            name = "Stalker",
            teaser = "Tarkovsky's 1979 film — three men travel into a forbidden zone called The Zone, where there is a room that grants wishes. The film took 2 years to shoot because Tarkovsky destroyed most of the footage, then re-shot the entire thing.",
            imageUrl = "",
            exploreAction = ExploreAction("Watch", "Stalker (1979) — full film, subbed", 162,
                "Watch in a dark room. The Zone is shot in sepia and color; the rest of the film is in color. Don't try to parse the philosophy — Tarkovsky said the film is about \"the absence of the author,\" not about the room.")
        ),
        CurioTopic(
            id = "movies-tarkovsky-mirror",
            categoryId = CategoryId.MOVIES,
            subtype = "Film",
            name = "Mirror",
            teaser = "Tarkovsky's 1975 autobiographical film — no actors in the main roles, mostly his own family. He refused to use professional actors because he wanted \"the look of someone who has lived a long time.\" The film has almost no dialogue.",
            imageUrl = "",
            exploreAction = ExploreAction("Watch", "Mirror (1975) — full film, subbed", 107,
                "Don't try to follow the timeline. The film jumps between childhood, adulthood, and old age without warning. The house in the film is the same house Tarkovsky grew up in. The wind is the most important character.")
        ),
        CurioTopic(
            id = "movies-nolan-memento",
            categoryId = CategoryId.MOVIES,
            subtype = "Film",
            name = "Memento",
            teaser = "Nolan's 2000 film — Christopher Nolan wrote the screenplay based on a short story by his brother Jonathan. The film is told in reverse chronological order; the protagonist has short-term memory loss and tattoos himself with clues.",
            imageUrl = "",
            exploreAction = ExploreAction("Watch", "Memento (2000) — full film", 113,
                "Watch both timelines. The film cuts between forward (color) and reverse (black-and-white). The black-and-white scenes are in chronological order; the color scenes in reverse. By the end you have enough information to figure out what really happened.")
        ),
        CurioTopic(
            id = "movies-aparajito-pather",
            categoryId = CategoryId.MOVIES,
            subtype = "Film",
            name = "Pather Panchali",
            teaser = "Satyajit Ray's 1955 debut — the first Indian film to win at Cannes. Shot over 5 years because Ray kept losing funding. He sold his wife's jewelry to finish it. The film follows a poor Bengali boy growing up in a village.",
            imageUrl = "",
            exploreAction = ExploreAction("Watch", "Pather Panchali (1955) — full film, subbed", 125,
                "Watch the rain scene. Apu and Durga run outside to dance in the first rain of monsoon. It's 4 minutes long with almost no dialogue. Ray said this scene was what convinced him he could direct — he watched it back and saw it was good.")
        ),
        CurioTopic(
            id = "movies-bong-jobs",
            categoryId = CategoryId.MOVIES,
            subtype = "Film",
            name = "Parasite",
            teaser = "Bong Joon-ho's 2019 film — the first non-English-language film to win Best Picture. The plot involves a poor family infiltrating a wealthy family's home by posing as unrelated employees. The third act becomes a different film.",
            imageUrl = "",
            exploreAction = ExploreAction("Watch", "Parasite (2019) — full film, subbed", 132,
                "Watch the peach scene. The father (Kim Ki-taek) smells like the previous housekeeper's husband because they both work in the same basement of the same building. The smell is the metaphor for class — they can fake status but not origin.")
        ),
        CurioTopic(
            id = "movies-linklater-boyhood",
            categoryId = CategoryId.MOVIES,
            subtype = "Film",
            name = "Boyhood",
            teaser = "Linklater's 2014 film — shot over 12 years with the same cast. The actors aged in real time. Ellar Coltrane was 6 when filming began and 18 when it ended. The film is 2 hours 45 minutes of ordinary life accumulating.",
            imageUrl = "",
            exploreAction = ExploreAction("Watch", "Boyhood (2014) — full film", 165,
                "Pay attention to the haircuts. Linklater edited out every explicit age marker (no \"6 years later\" titles). The haircuts do the work — short hair in childhood, long in middle school, back short in high school. By the end you understand what 12 years looks like.")
        ),
        CurioTopic(
            id = "movies-coppola-apocalypse",
            categoryId = CategoryId.MOVIES,
            subtype = "Film",
            name = "Apocalypse Now",
            teaser = "Coppola's 1979 film — shot in the Philippines during monsoon season, with typhoons destroying sets. Martin Sheen's character was so exhausted by the shoot that his real breakdown was filmed — it's in the movie.",
            imageUrl = "",
            exploreAction = ExploreAction("Watch", "Apocalypse Now (1979) — full film", 153,
                "Watch the helicopter sequence. The soundtrack is The End by The Doors — Coppola spliced the song and the visuals so they sync. Brando improvised most of his scenes. The final shot was shot last and is in the same take as the closing monologue.")
        ),
        CurioTopic(
            id = "movies-kieslowski-three-colors-blue",
            categoryId = CategoryId.MOVIES,
            subtype = "Film",
            name = "Three Colors: Blue",
            teaser = "Kieslowski's 1993 film — the first of his trilogy on the French flag colors (and the themes of liberty, equality, fraternity). Juliette Binoche plays a woman whose husband and child die in a car accident; the film follows her attempt to live without attachment.",
            imageUrl = "",
            exploreAction = ExploreAction("Watch", "Three Colors: Blue (1993) — full film, subbed", 98,
                "Don't try to understand the plot. The film is about color — literally. Watch for the recurring blue object (a chandelier, a candy wrapper, a piece of cellophane). The colors are saying what the dialogue can't.")
        )
    )

    // ─────────────────────────────────────────────────────────────────────
    //  BOOKS — 15 topics (Authors + Works)
    // ─────────────────────────────────────────────────────────────────────

    private val booksAll: List<CurioTopic> = listOf(
        CurioTopic(
            id = "books-le-guin-dispossessed",
            categoryId = CategoryId.BOOKS,
            subtype = "Novel",
            name = "The Dispossessed",
            teaser = "Le Guin's 1974 novel — an anarchist physicist travels between his planet (a pacifist utopia without property) and a neighboring capitalist world. The book has two halves; the second half is the same events told in reverse order, from the other characters' perspectives.",
            imageUrl = "",
            exploreAction = ExploreAction("Read", "\"The Dispossessed\" (1974) by Ursula K. Le Guin", 380,
                "Read chapters 1-6 first. Then chapters 7-13. The chapters are arranged so chapter 1 and chapter 13 are the same scene from different angles. Le Guin wants you to read it once and feel one way about it; read it again and feel the other way.")
        ),
        CurioTopic(
            id = "books-le-guin-left-hand",
            categoryId = CategoryId.BOOKS,
            subtype = "Novel",
            name = "The Left Hand of Darkness",
            teaser = "Le Guin's 1969 novel — set on a planet where the inhabitants are ambisexual, alternating between male and female forms monthly. The book asks: what would politics look like without gender? It won both Hugo and Nebula.",
            imageUrl = "",
            exploreAction = ExploreAction("Read", "\"The Left Hand of Darkness\" (1969) by Ursula K. Le Guin", 304,
                "Don't read the introduction until after. Le Guin put a lot of background at the start of the book — read it without context first, then read the introduction. The novel makes more sense without the explanation than with it.")
        ),
        CurioTopic(
            id = "books-borges-ficciones",
            categoryId = CategoryId.BOOKS,
            subtype = "Story Collection",
            name = "Ficciones",
            teaser = "Borges' 1944 collection of short stories — about libraries that contain every possible book, a man who forgets nothing, a garden that forks into infinite branching paths. Borges invented the genre of metaphysical fiction; everyone since has been copying him.",
            imageUrl = "",
            exploreAction = ExploreAction("Read", "\"Ficciones\" (1944) by Jorge Luis Borges — pick any story", 30,
                "Start with \"The Garden of Forking Paths.\" It's a detective story where the detective is the victim's grandson and the crime is the novel the victim wrote. The story is 8 pages long and ends with a twist you won't see coming.")
        ),
        CurioTopic(
            id = "books-morrison-beloved",
            categoryId = CategoryId.BOOKS,
            subtype = "Novel",
            name = "Beloved",
            teaser = "Toni Morrison's 1987 novel — based on the true story of Margaret Garner, an enslaved woman who escaped to Ohio in 1856 and killed her own daughter rather than let her be returned to slavery. The ghost of the daughter haunts the family.",
            imageUrl = "",
            exploreAction = ExploreAction("Read", "\"Beloved\" (1987) by Toni Morrison", 324,
                "Read the first 50 pages slowly. Morrison's prose is dense — every sentence has multiple meanings. The novel is about what slavery did to people who survived it, not what happened to people who didn't. The ghost is the past that won't stay buried.")
        ),
        CurioTopic(
            id = "books-dostoevsky-notes",
            categoryId = CategoryId.BOOKS,
            subtype = "Novel",
            name = "Notes from Underground",
            teaser = "Dostoevsky's 1864 novella — a 40-year-old retired civil servant narrates his petty resentments and failed attempts to connect with other people. The first half is monologue; the second half is a single disastrous dinner party. Nietzsche said this book was the foundation of existentialism.",
            imageUrl = "",
            exploreAction = ExploreAction("Read", "\"Notes from Underground\" (1864) by Fyodor Dostoevsky — Part I", 80,
                "Read Part I in one sitting (40 pages). The narrator is angry, bitter, paranoid, and brilliant. He's not a villain — he's a person who has been humiliated his entire life and uses intellect as a defense. He is often wrong, but never stupid.")
        ),
        CurioTopic(
            id = "books-mitchell-cloud-atlas",
            categoryId = CategoryId.BOOKS,
            subtype = "Novel",
            name = "Cloud Atlas",
            teaser = "Mitchell's 2004 novel — six stories nested inside each other: a 19th-century Pacific voyage, a 1930s Belgium piano student, a 1970s California journalist, a present-day English publisher, a future Korea clone, and a post-apocalyptic Hawaii. Each story is interrupted and resumed.",
            imageUrl = "",
            exploreAction = ExploreAction("Read", "\"Cloud Atlas\" (2004) by David Mitchell", 509,
                "Read the first 100 pages. The structure is non-obvious — six stories, each broken in half. The first half of the novel is the first halves of the six stories; the second half is the second halves in reverse order. Mitchell said the novel is about \"how the actions of others ripple forward through time.\"")
        ),
        CurioTopic(
            id = "books-okri-famished-road",
            categoryId = CategoryId.BOOKS,
            subtype = "Novel",
            name = "The Famished Road",
            teaser = "Okri's 1991 novel — a \"spirit child\" named Azaro narrates his life between the world of the living and the world of the spirits. The book won the Booker Prize. Okri was 32 when it was published; he rewrote the novel twice over five years.",
            imageUrl = "",
            exploreAction = ExploreAction("Read", "\"The Famished Road\" (1991) by Ben Okri — first 100 pages", 100,
                "Don't look up the plot. The novel is dream-like — events don't always follow from each other; sometimes they follow from spirits. Azaro is born ready to return to the spirit world; he chooses to stay. Read the first hundred pages without trying to summarize.")
        ),
        CurioTopic(
            id = "books-saramago-blindness",
            categoryId = CategoryId.BOOKS,
            subtype = "Novel",
            name = "Blindness",
            teaser = "Saramago's 1995 novel — an epidemic of white blindness sweeps an unnamed city. The novel has no character names; everyone is identified by what they do (\"the doctor's wife,\" \"the boy with the squint\"). The blindness becomes literal and then metaphorical.",
            imageUrl = "",
            exploreAction = ExploreAction("Read", "\"Blindness\" (1995) by José Saramago", 310,
                "Don't try to keep track of who is who. Saramago writes in long flowing paragraphs with no quotation marks. The dialogue continues without breaks; the paragraphs are sometimes 4 pages long. The novel is a parable — what would people do if they had to give up seeing each other?")
        ),
        CurioTopic(
            id = "books-mccarthy-road",
            categoryId = CategoryId.BOOKS,
            subtype = "Novel",
            name = "Blood Meridian, or the Evening Redness in the West",
            teaser = "McCarthy's 1985 novel — set in the 1840s American Southwest, about a teenager who joins a gang of scalp hunters led by a figure called \"the Judge.\" The novel is the most violent in American literature; it's also one of the most beautiful.",
            imageUrl = "",
            exploreAction = ExploreAction("Read", "\"Blood Meridian\" (1985) by Cormac McCarthy — first 50 pages", 50,
                "Read the first 50 pages. The prose is unpunctuated (McCarthy almost never uses quotation marks), and the violence is explicit. The novel is about how American violence is foundational — not an aberration from peace but the substance of the peace itself.")
        ),
        CurioTopic(
            id = "books-woolf-to-the-lighthouse",
            categoryId = CategoryId.BOOKS,
            subtype = "Novel",
            name = "To the Lighthouse",
            teaser = "Woolf's 1927 novel — set in a Scottish holiday house, mostly in the consciousness of Mrs. Ramsay and her guests. The famous \"Time Passes\" interlude skips ten years in the middle of the book, devastating everything that happened in those ten years in a few pages.",
            imageUrl = "",
            exploreAction = ExploreAction("Read", "\"To the Lighthouse\" (1927) by Virginia Woolf — Part I", 90,
                "Read the first 80 pages slowly. There is almost no plot. The novel is about the texture of a day — what people think, what they don't say, what the light does on the water. The dinner scene (around page 100) is the most famous dinner party in fiction.")
        ),
        CurioTopic(
            id = "books-tolstoy-death-of-ivan",
            categoryId = CategoryId.BOOKS,
            subtype = "Novella",
            name = "The Death of Ivan Ilyich",
            teaser = "Tolstoy's 1886 novella — a 45-year-old judge dies of a mysterious illness after a minor accident. The novel is 80 pages long. It's about the moment when a person realizes they've spent their entire life on the wrong things.",
            imageUrl = "",
            exploreAction = ExploreAction("Read", "\"The Death of Ivan Ilyich\" (1886) by Leo Tolstoy", 80,
                "Read in one sitting (80 pages). The novella is structured in two halves: the first is about Ivan's life and death; the second is the last three days, when Ivan realizes he wasted his life. The famous last paragraph is one of the great lines in literature.")
        ),
        CurioTopic(
            id = "books-rilke-duino-elegies",
            categoryId = CategoryId.BOOKS,
            subtype = "Poetry",
            name = "Duino Elegies",
            teaser = "Rilke's 1923 sequence of ten long poems — written over ten years, mostly during a solitary winter at a Swiss castle. The poems are about angels, beauty, the impossibility of love, and the necessity of turning every difficulty into a form.",
            imageUrl = "",
            exploreAction = ExploreAction("Read", "\"Duino Elegies\" (1923) by Rainer Maria Rilke — first elegy", 25,
                "Read the first elegy. It's 10 pages of some of the densest poetry in the language. The opening line is \"Who, if I cried out, would hear me among the angelic orders?\" Don't try to understand it. Read it three times over three days.")
        ),
        CurioTopic(
            id = "books-calvino-if-on-a-winters-night",
            categoryId = CategoryId.BOOKS,
            subtype = "Novel",
            name = "If on a winter's night a traveler",
            teaser = "Calvino's 1979 novel — alternating chapters between a \"you\" trying to read a novel and the start of a different novel each time. Twenty-two chapters, each ending on a cliffhanger. The book is about the experience of reading as much as it tells a story.",
            imageUrl = "",
            exploreAction = ExploreAction("Read", "\"If on a winter's night a traveler\" (1979) by Italo Calvino — first 50 pages", 50,
                "Read the first chapter. It's two pages long, addressed to \"you.\" Then chapter 2 is the first 10 pages of a novel you're about to read — Calvino switches between describing you reading and the text you're reading. The structure is the content.")
        ),
        CurioTopic(
            id = "books-augustine-confessions",
            categoryId = CategoryId.BOOKS,
            subtype = "Memoir",
            name = "Confessions",
            teaser = "Augustine's 397 memoir — the first autobiography in Western literature. He writes about stealing pears as a teenager, his mother Monica praying for his conversion for 30 years, and the moment he heard a child's voice saying \"take up and read.\"",
            imageUrl = "",
            exploreAction = ExploreAction("Read", "\"Confessions\" (397) by Augustine — Book VIII", 35,
                "Read Book VIII. It's about Augustine's conversion — written 13 years after the fact, in present-tense narrative as if the conversion were happening now. The \"take up and read\" moment is famous: he was in a garden, heard a voice, picked up a Bible at random, and read the first verse he saw. He'd been trying to convert for 11 years.")
        ),
        CurioTopic(
            id = "books-murasaki-tale-of-genji",
            categoryId = CategoryId.BOOKS,
            subtype = "Novel",
            name = "The Tale of Genji",
            teaser = "Murasaki Shikibu's 11th-century novel — written in Japan around 1010, often called the world's first novel. 54 chapters, more than 1,000 pages in most translations. The story is about the son of an ancient Japanese emperor and his many romantic relationships.",
            imageUrl = "",
            exploreAction = ExploreAction("Read", "\"The Tale of Genji\" (1010) by Murasaki Shikibu — first 50 pages (Royall Tyler translation)", 50,
                "Read 50 pages of any modern translation. The novel is over 1,000 years old and reads like contemporary literary fiction — Murasaki understood jealousy, ambition, and loneliness in ways that haven't dated. The \"utai\" scene (poetry competitions) is the foundation of Japanese aesthetics.")
        )
    )

    // ─────────────────────────────────────────────────────────────────────
    //  ART — 15 topics
    // ─────────────────────────────────────────────────────────────────────

    private val artAll: List<CurioTopic> = listOf(
        CurioTopic(
            id = "art-kahlo-broken-column",
            categoryId = CategoryId.VISUAL_ART,
            subtype = "Self-Portrait",
            name = "The Broken Column",
            teaser = "Kahlo's 1944 self-portrait — painted after another of her spinal surgeries. She's wearing a metal corset, her body split open down the middle to reveal a crumbling Ionic column. Nails pierce her skin. Tears on her cheeks. She painted it lying in bed.",
            imageUrl = "",
            exploreAction = ExploreAction("Look", "\"The Broken Column\" (1944) by Frida Kahlo", 15,
                "Read the painting as a self-portrait first, then as a landscape. The column inside her is a ruined building — the body as architecture. Look at the nails: they're holding her together. Look at the tears: they're falling in a real direction.")
        ),
        CurioTopic(
            id = "art-kusama-infinity-mirrors",
            categoryId = CategoryId.VISUAL_ART,
            subtype = "Installation",
            name = "Infinity Mirrored Room",
            teaser = "Kusama's mirrored rooms — she's been building them since 1965. The rooms contain hundreds of small LED lights suspended at different heights, reflected infinitely in mirrored walls. Visitors stand on a small platform inside. Kusama has lived voluntarily in a psychiatric hospital since 1977.",
            imageUrl = "",
            exploreAction = ExploreAction("Look", "\"Infinity Mirrored Room\" installations by Yayoi Kusama", 30,
                "Look at images of the rooms online. There's no book equivalent of standing inside them — the effect is the multiplication of self in an infinite field of lights. Kusama calls this \"self-obliteration\" — she wants to lose the boundary between self and universe.")
        ),
        CurioTopic(
            id = "art-klint-paintings",
            categoryId = CategoryId.VISUAL_ART,
            subtype = "Painting Series",
            name = "The Paintings for the Temple",
            teaser = "af Klint's 1907 series of 193 paintings — abstract works painted years before Kandinsky, Malevich, or Mondrian. She exhibited none of them in her lifetime. Her will stipulated they not be shown for 20 years after her death. They went on public display in 1986.",
            imageUrl = "",
            exploreAction = ExploreAction("Look", "\"The Paintings for the Temple\" (1907) by Hilma af Klint", 60,
                "Don't try to \"understand\" abstract art. af Klint was a spiritualist — she believed the paintings came from spirits she channeled. Look at the shapes: circles, spirals, organic forms. Look at the colors: muted, layered, almost glowing. The paintings were never meant to be analyzed.")
        ),
        CurioTopic(
            id = "art-rothko-no-14",
            categoryId = CategoryId.VISUAL_ART,
            subtype = "Painting",
            name = "No. 14, 1960",
            teaser = "Rothko's 1960 painting — 9 feet tall, 8 feet wide, two rectangles of deep maroon floating in a black field. He wanted viewers to weep in front of his paintings. He committed suicide in 1970. His late paintings are almost entirely black.",
            imageUrl = "",
            exploreAction = ExploreAction("Look", "\"No. 14, 1960\" by Mark Rothko — any online reproduction, full-screen", 20,
                "Find a high-resolution image online and view it full-screen. The painting is meant to fill your visual field. Rothko said \"I'm interested only in expressing basic human emotions — tragedy, ecstasy, doom.\" The maroon rectangle is the tragedy.")
        ),
        CurioTopic(
            id = "art-okeeffe-sky-above",
            categoryId = CategoryId.VISUAL_ART,
            subtype = "Painting",
            name = "Sky Above Clouds IV",
            teaser = "O'Keeffe's 1965 painting — 8 feet by 24 feet, the largest painting she ever made. It shows clouds from above, in the style she developed in the 1960s. The painting was meant to be hung on four walls of a room, surrounding the viewer.",
            imageUrl = "",
            exploreAction = ExploreAction("Look", "\"Sky Above Clouds IV\" (1965) by Georgia O'Keeffe", 30,
                "Look at the painting as if you were a passenger looking out a plane window. O'Keeffe was 77 when she painted it; she'd been at her home in New Mexico for over 15 years. The painting is calm — clouds seen from above are peaceful. The 24-foot width matters.")
        ),
        CurioTopic(
            id = "art-pollock-one",
            categoryId = CategoryId.VISUAL_ART,
            subtype = "Painting",
            name = "Autumn Rhythm (Number 30)",
            teaser = "Pollock's 1950 drip painting — made by laying canvas on the floor and pouring, flicking, and dripping paint from above. The painting took one day to make. The film of Pollock painting it (by Hans Namuth) is one of the great art documents of the 20th century.",
            imageUrl = "",
            exploreAction = ExploreAction("Look", "\"Autumn Rhythm (Number 30)\" (1950) by Jackson Pollock", 30,
                "Don't try to find meaning. Look at the layers — there are at least 5 different rhythms of drip on top of each other. Pollock called his technique \"direct\" — no contact between brush and canvas, no easel, no horizon. Watch the Hans Namuth film to see how it was made.")
        ),
        CurioTopic(
            id = "art-mondrian-composition-ii",
            categoryId = CategoryId.VISUAL_ART,
            subtype = "Painting",
            name = "Composition with Red, Blue, and Yellow",
            teaser = "Mondrian's 1930 painting — three rectangles (red, blue, yellow) separated by black lines on a white field. The painting is a single work but became an entire aesthetic — primary colors, perpendicular lines, the \"grid.\" Fashion, advertising, and architecture have been copying it for 90 years.",
            imageUrl = "",
            exploreAction = ExploreAction("Look", "\"Composition with Red, Blue, and Yellow\" (1930) by Piet Mondrian", 15,
                "Look at the painting's proportions. Mondrian spent decades refining the exact line weights and rectangle sizes. The black lines are not the same thickness — they vary slightly. The white spaces between them are also not equal. Look at where the lines stop short of the edge.")
        ),
        CurioTopic(
            id = "art-warhol-marilyn",
            categoryId = CategoryId.VISUAL_ART,
            subtype = "Silkscreen",
            name = "Marilyn Diptych",
            teaser = "Warhol's 1962 work — 50 silkscreened images of Marilyn Monroe, half in color (left) and half in black and white (right). The work was made weeks after Monroe died. It's a meditation on celebrity, repetition, and mortality.",
            imageUrl = "",
            exploreAction = ExploreAction("Look", "\"Marilyn Diptych\" (1962) by Andy Warhol", 30,
                "Notice the contrast between the two halves. The left half is saturated, vivid — the Marilyn you saw in movies. The right half fades to gray, smeared, ghostly — the Marilyn who died at 36. Look at the registers; some are more faded than others. Warhol didn't choose the fades randomly.")
        ),
        CurioTopic(
            id = "art-bourgeois-maman",
            categoryId = CategoryId.VISUAL_ART,
            subtype = "Sculpture",
            name = "Maman",
            teaser = "Bourgeois' 1999 sculpture — a 30-foot-tall spider made of bronze, with a mesh body containing 26 marble eggs. Bourgeois said the spider is a tribute to her mother, who was a tapestry restorer. There are six castings of Maman in the world.",
            imageUrl = "",
            exploreAction = ExploreAction("Look", "\"Maman\" (1999) by Louise Bourgeois", 30,
                "Stand under it if you can find a casting (the Tate Modern, Guggenheim Bilbao, Qatar National Museum, etc.). Bourgeois said the spider is protective — like her mother, who fixed torn tapestries by weaving them back together. The marble eggs inside the body are the children being protected.")
        ),
        CurioTopic(
            id = "art-richter-iceberg",
            categoryId = CategoryId.VISUAL_ART,
            subtype = "Painting",
            name = "Iceberg",
            teaser = "Richter's 1982 painting — based on a blurred photograph of an iceberg. The painting is photo-realistic but the blur makes it look abstract. Richter has spent 60 years exploring what painting can do that photography can't.",
            imageUrl = "",
            exploreAction = ExploreAction("Look", "\"Iceberg\" (1982) by Gerhard Richter", 20,
                "Look at the painting up close and at a distance. Up close it's just gray and white paint strokes. At a distance the iceberg materializes. Richter's paintings ask: what does it mean to make a painting of a photograph? The blur is the answer.")
        ),
        CurioTopic(
            id = "art-vasquez-de-la-cruz-indigo",
            categoryId = CategoryId.VISUAL_ART,
            subtype = "Painting",
            name = "Indigo",
            teaser = "De la Cruz's 2017 painting — part of her series on indigo dye in the African diaspora. The painting is a portrait of a young Senegalese woman in the act of dyeing fabric. The blue is real indigo, not synthetic; the canvas smells like fermented leaves.",
            imageUrl = "",
            exploreAction = ExploreAction("Look", "\"Indigo\" (2017) by Lucia de la Cruz (or any contemporary indigo dye painting)", 20,
                "Look at the blue. Real indigo has more depth than synthetic blue — it looks lit from within. Look at the woman's hands — they're stained darker than her face, because dye absorbs unevenly into skin. De la Cruz paints slowly, over months, to capture the dye's slow changes.")
        ),
        CurioTopic(
            id = "art-hockney-pool",
            categoryId = CategoryId.VISUAL_ART,
            subtype = "Painting Series",
            name = "A Bigger Splash",
            teaser = "Hockney's 1967 painting — a California swimming pool, just after someone has dived in. There's no diver visible — only the splash, frozen. The pool is rendered in flat color, the splash in meticulously detailed acrylic. Hockney was working on it for two weeks.",
            imageUrl = "",
            exploreAction = ExploreAction("Look", "\"A Bigger Splash\" (1967) by David Hockney", 20,
                "Look at the splash. Hockney spent two weeks painting the splash alone — the rest of the painting took three days. Look at the perspective — the pool is rendered from above but the deck from the side. The painting deliberately breaks perspective; California is a place where perspective doesn't matter.")
        ),
        CurioTopic(
            id = "art-banksy-flower-thrower",
            categoryId = CategoryId.VISUAL_ART,
            subtype = "Stencil",
            name = "Love is in the Air",
            teaser = "Banksy's 2003 stencil — a young man in a bandana about to throw a bouquet (or a Molotov cocktail, depending on the viewer). The stencil was originally painted on a wall in Jerusalem; there are now over 100 unauthorized copies around the world.",
            imageUrl = "",
            exploreAction = ExploreAction("Look", "\"Love is in the Air\" (2003) by Banksy", 15,
                "Look at the title. Banksy doesn't say which interpretation is right — the bouquet and the Molotov cocktail are equally visible. The work is a question: is resistance romantic? Is romanticism resistance? The image has become a symbol of protest movements worldwide.")
        ),
        CurioTopic(
            id = "art-johns-flag",
            categoryId = CategoryId.VISUAL_ART,
            subtype = "Painting",
            name = "Flag",
            teaser = "Johns' 1954 painting — an American flag made with encaustic (pigment mixed with hot wax) on newspaper. The painting was the first work of art that the U.S. government tried to prosecute for being unpatriotic — the case was dropped. Johns was 24.",
            imageUrl = "",
            exploreAction = ExploreAction("Look", "\"Flag\" (1954) by Jasper Johns", 30,
                "Look at the surface. The painting isn't a flat flag — the encaustic wax is thick in some places, thin in others. The newspaper underneath shows through in some areas. Johns' point: the flag is not the country. The flag is paint on canvas. The flag is what we see, not what we believe.")
        ),
        CurioTopic(
            id = "art-kahlo-two-fridas",
            categoryId = CategoryId.VISUAL_ART,
            subtype = "Self-Portrait",
            name = "The Two Fridas",
            teaser = "Kahlo's 1939 self-portrait — two Fridas sitting side by side, holding hands, with a single vein connecting their hearts. One wears Tehuana costume (the Frida her husband Diego loved); the other wears Victorian dress (the Frida her husband rejected). The painting was made after her divorce.",
            imageUrl = "",
            exploreAction = ExploreAction("Look", "\"The Two Fridas\" (1939) by Frida Kahlo", 20,
                "Look at the vein. It's a single vein that branches to both hearts — they're connected by blood. One Frida is bleeding; the other holds a surgical clamp. The painting is about being two people at once — the self someone loves and the self they're afraid of.")
        )
    )

    // ─────────────────────────────────────────────────────────────────────
    //  SCIENCE — 15 topics
    // ─────────────────────────────────────────────────────────────────────

    private val scienceAll: List<CurioTopic> = listOf(
        CurioTopic(
            id = "science-mycelium",
            categoryId = CategoryId.SCIENCE,
            subtype = "Biology",
            name = "Mycelium",
            teaser = "The network of fungal threads that connects the roots of nearly 90% of land plants. Trees share carbon through mycorrhizal networks — a struggling seedling can be fed sugar by a mature tree 30 feet away. Suzanne Simard's research found that the largest trees in a forest are also the most generous donors to the smallest.",
            imageUrl = "",
            exploreAction = ExploreAction("Read", "Suzanne Simard — \"Finding the Mother Tree\" (2021) — Introduction + Chapter 1", 60,
                "Read the first chapter. Simard started her research in 1981 — nobody believed trees could communicate. The technology to test it (carbon-13 isotopes) didn't exist yet. By 1997 she had the data and the backlash began: colleagues called her work \"feminine science\" and said forests couldn't have feelings.")
        ),
        CurioTopic(
            id = "science-black-holes",
            categoryId = CategoryId.SCIENCE,
            subtype = "Astrophysics",
            name = "Black Holes",
            teaser = "A region of spacetime where gravity is so strong that nothing, not even light, can escape. The Event Horizon Telescope team imaged one in 2019 — the orange ring you saw is the accretion disk, not the black hole itself. The dark center is the shadow. The image took 2 years of processing.",
            imageUrl = "",
            exploreAction = ExploreAction("Read", "\"Black Hole Survival Guide\" by Janna Levin (2020) — Introduction", 25,
                "Read the introduction. Levin writes about black holes as if they were weather events — \"an approaching storm.\" The book is 10 short chapters. Read one chapter per night before bed. Black holes are simpler than they sound: matter + gravity + nothing else.")
        ),
        CurioTopic(
            id = "science-crispr",
            categoryId = CategoryId.SCIENCE,
            subtype = "Biology",
            name = "CRISPR-Cas9",
            teaser = "A bacterial immune system that Jennifer Doudna and Emmanuelle Charpentier (2020 Nobel Prize) turned into a gene-editing tool in 2012. The original function: bacteria use it to chop up viral DNA. The application: any DNA sequence, any organism, edited at will. The first human clinical trials happened in 2019.",
            imageUrl = "",
            exploreAction = ExploreAction("Read", "\"A Crack in Creation\" by Jennifer Doudna + Samuel Sternberg (2017) — Chapter 1", 25,
                "Read the first chapter. Doudna writes about the day she realized CRISPR could edit human DNA. She describes sitting in her office realizing the implications before telling anyone. The chapter is 8 pages and worth reading slowly.")
        ),
        CurioTopic(
            id = "science-quantum",
            categoryId = CategoryId.SCIENCE,
            subtype = "Physics",
            name = "Quantum Mechanics",
            teaser = "The physics of the very small, where particles can be in two states at once (superposition), influence each other across any distance (entanglement), and only commit to a state when observed. Niels Bohr said: \"Anyone who is not shocked by quantum theory has not understood it.\"",
            imageUrl = "",
            exploreAction = ExploreAction("Read", "\"Quantum Mechanics: The Theoretical Minimum\" by Leonard Susskind + Art Friedman (2014) — Chapters 1-2", 30,
                "Read the first two chapters. Susskind teaches physics the way a woodworker teaches joinery: you start with the saw and the board, not the theory. Don't try to understand everything. Notice when the authors say \"this is strange.\" They're not being rhetorical.")
        ),
        CurioTopic(
            id = "science-plate-tectonics",
            categoryId = CategoryId.SCIENCE,
            subtype = "Geology",
            name = "Plate Tectonics",
            teaser = "The theory that Earth's outer shell is divided into plates that move 1-10 cm per year. Alfred Wegener proposed it in 1912 with no mechanism — the mechanism (mantle convection + ridge push + slab pull) wasn't understood until the 1960s. The Atlantic Ocean is widening by 2.5 cm/year.",
            imageUrl = "",
            exploreAction = ExploreAction("Read", "\"The Story of Earth\" by Robert Hazen (2012) — Chapter 9", 30,
                "Read Chapter 9 — it's about the discovery of plate tectonics. Wegener died in 1930 still mocked by the geology establishment. The vindication came in 1963, when magnetic stripes on the ocean floor (symmetric about mid-ocean ridges) confirmed seafloor spreading.")
        ),
        CurioTopic(
            id = "science-bees",
            categoryId = CategoryId.SCIENCE,
            subtype = "Biology",
            name = "Bee Waggle Dance",
            teaser = "Karl von Frisch decoded the honeybee waggle dance in 1973 (Nobel Prize). Bees returning from a good nectar source do a figure-8 dance on the honeycomb. The angle of the waggle tells other bees the angle to fly relative to the sun. The duration of the waggle tells them the distance.",
            imageUrl = "",
            exploreAction = ExploreAction("Read", "\"Honeybee Democracy\" by Thomas Seeley (2010) — Chapter 1", 25,
                "Read the first chapter. Seeley writes about a swarm of bees that decided to move to a new nest — the decision took 3 days of debate among thousands of bees. The chapter is 30 pages and explains the waggle dance in detail.")
        ),
        CurioTopic(
            id = "science-prions",
            categoryId = CategoryId.SCIENCE,
            subtype = "Biology",
            name = "Prions",
            teaser = "Misfolded proteins that cause other proteins to misfold. Prion diseases include Creutzfeldt-Jakob in humans, BSE in cows (\"mad cow\"), and scrapie in sheep. There's no immune response — your body doesn't recognize the misfolded protein as foreign because it's made of the same amino acids.",
            imageUrl = "",
            exploreAction = ExploreAction("Read", "\"The Family That Couldn't Sleep\" by D. T. Max (2006) — Chapter 1", 30,
                "Read the first chapter. Max writes about an Italian family that has carried a prion disease for 200+ years. The chapter is about what it means to live with knowledge that you might have inherited a fatal genetic disease. Some readers can't finish the book; it's that affecting.")
        ),
        CurioTopic(
            id = "science-photosynthesis",
            categoryId = CategoryId.SCIENCE,
            subtype = "Biology",
            name = "Photosynthesis",
            teaser = "The process by which plants convert sunlight + CO2 + water into sugar + oxygen. The reaction happens in two stages (light reactions + Calvin cycle) and takes place inside the chloroplast. The efficiency of photosynthesis is about 3-6% — most of the sun's energy is reflected or unused.",
            imageUrl = "",
            exploreAction = ExploreAction("Read", "\"What a Plant Knows\" by Daniel Chamovitz (2012) — Chapter 1", 25,
                "Read the first chapter. Chamovitz writes about what plants can sense without having eyes or ears: light direction, gravity, temperature, touch. The chapter is 20 pages and rewires how you think about the green things in your apartment.")
        ),
        CurioTopic(
            id = "science-oceans",
            categoryId = CategoryId.SCIENCE,
            subtype = "Biology",
            name = "Deep Sea Vents",
            teaser = "Hydrothermal vents on the ocean floor discovered in 1977 — water heated to 400°C by magma emerges through cracks, carrying dissolved minerals. The vents support entire ecosystems based on chemosynthesis instead of photosynthesis. The ecosystems exist nowhere else on Earth.",
            imageUrl = "",
            exploreAction = ExploreAction("Read", "\"The Deep\" by Claire Nouvian (2007) — Introduction + Chapter 1", 40,
                "Read the introduction. Nouvian writes about the discovery of the vents by a team including Robert Ballard (who later found the Titanic). The introduction is 15 pages; it explains why finding the vents was the most important deep-sea discovery of the 20th century.")
        ),
        CurioTopic(
            id = "science-spiders",
            categoryId = CategoryId.SCIENCE,
            subtype = "Biology",
            name = "Spider Silk",
            teaser = "Dragline silk (the kind spiders use to hang from ceilings) is stronger than steel by weight and tougher than Kevlar. It's made of proteins called spidroins, spun from spinnerets on the spider's abdomen. Spider silk is one of the most studied materials in science — no synthetic version has matched it.",
            imageUrl = "",
            exploreAction = ExploreAction("Read", "\"The Spider\" by Simon Sherwood (2020) — Chapter 1", 25,
                "Read the first chapter. Sherwood writes about orb-weaver spiders — the ones that build the wheel-shaped webs. The chapter is 20 pages and explains the engineering of the web: the radial threads are stiff, the spiral threads are sticky, and the spider walks only on the radial threads to avoid getting caught.")
        ),
        CurioTopic(
            id = "science-mushrooms",
            categoryId = CategoryId.SCIENCE,
            subtype = "Biology",
            name = "Fungal Communication",
            teaser = "Mycologist Paul Stamets argues that mushrooms are the immune system of the forest — they break down dead material, redistribute nutrients, and protect trees from pathogens. Stamets also claims that psilocybin mushrooms may be the most important medicine of the 21st century.",
            imageUrl = "",
            exploreAction = ExploreAction("Read", "\"Mycelium Running\" by Paul Stamets (2005) — Chapter 1", 25,
                "Read the first chapter. Stamets writes about mushrooms as a \"neurological internet\" — a network that connects plants to each other underground. The chapter is 20 pages; Stamets has a TED talk on the same material if you'd rather watch.")
        ),
        CurioTopic(
            id = "science-radioactivity",
            categoryId = CategoryId.SCIENCE,
            subtype = "Physics",
            name = "Radioactivity",
            teaser = "Marie Curie won two Nobel Prizes (1903, 1911) for the discovery of radioactivity and the isolation of radium. She kept radium in a desk drawer; it glowed blue. Her notebooks are still too radioactive to be handled without protective gear, more than 100 years later.",
            imageUrl = "",
            exploreAction = ExploreAction("Read", "\"Radioactive\" by Lauren Redniss (2010) — Chapter 1", 30,
                "Read the first chapter. Redniss writes about Curie's life in a style that's half biography, half visual art. The chapter includes photos of Curia's actual lab notebooks — they're too radioactive to open but you can see them through protective glass.")
        ),
        CurioTopic(
            id = "science-ants",
            categoryId = CategoryId.SCIENCE,
            subtype = "Biology",
            name = "Ant Colonies",
            teaser = "Ant colonies operate without central control — no ant is in charge. Each ant follows simple rules based on local information, and colony behavior emerges from millions of those local interactions. E.O. Wilson spent 70 years studying ants; he called them \"the little things that run the world.\"",
            imageUrl = "",
            exploreAction = ExploreAction("Read", "\"Journey to the Ants\" by Bert Hölldobler + E.O. Wilson (1994) — Chapter 1", 25,
                "Read the first chapter. Hölldobler and Wilson write about leafcutter ants — the ones that farm fungus underground by carrying leaf fragments. The chapter is 20 pages; the story of how the ants discovered agriculture 50 million years before humans did is one of the great science anecdotes.")
        ),
        CurioTopic(
            id = "science-perception",
            categoryId = CategoryId.SCIENCE,
            subtype = "Neuroscience",
            name = "Visual Perception",
            teaser = "Your eyes receive an upside-down, 2D image. Your brain inverts it, fills in the blind spot, predicts motion, and creates the illusion of a stable 3D world. About 50% of what you \"see\" is constructed by your brain, not received from your eyes. The discovery of the blind spot was one of the first demonstrations that perception is constructive.",
            imageUrl = "",
            exploreAction = ExploreAction("Read", "\"An Introduction to the History of Psychology\" by B.R. Hergenhahn (any edition) — Chapter on Perception", 35,
                "Read the chapter on perception (varies by edition, but it's usually early in the book). The classic demonstration: close one eye, focus the other on a fixed point, then move a small object slowly through your visual field. There's a spot where the object disappears — that's your blind spot. Your brain fills it in with the surrounding pattern.")
        ),
        CurioTopic(
            id = "science-evolution",
            categoryId = CategoryId.SCIENCE,
            subtype = "Biology",
            name = "Evolution by Natural Selection",
            teaser = "Darwin and Wallace both arrived at the theory of evolution by natural selection in 1858 (presented jointly at the Linnean Society). The full argument, with evidence, was published by Darwin in \"On the Origin of Species\" (1859). The book sold out its first printing in one day.",
            imageUrl = "",
            exploreAction = ExploreAction("Read", "\"On the Origin of Species\" by Charles Darwin (1859) — Chapter 4", 45,
                "Read Chapter 4 — it's about natural selection. The chapter is 30 pages. Darwin builds the argument slowly, with examples drawn from pigeons, ants, and barnacles. The book is surprisingly readable — Darwin writes like he's having a conversation with you.")
        )
    )

    // ─────────────────────────────────────────────────────────────────────
    //  WILDCARD — 10 mixed topics (covers all 5 named categories)
    // ─────────────────────────────────────────────────────────────────────

    private val wildcardAll: List<CurioTopic> = listOf(
        musicAll[2],   // Patti Smith — Horses (Rock)
        moviesAll[3],  // Mulholland Drive
        booksAll[4],   // Dostoevsky — Notes from Underground
        artAll[4],     // Rothko — No. 14
        scienceAll[0], // Mycelium
        musicAll[10],  // Radiohead — Kid A (Rock)
        moviesAll[6],  // Wong Kar-wai — Chungking Express
        booksAll[2],   // Borges — Ficciones
        artAll[10],    // Bourgeois — Maman
        scienceAll[4]  // Plate Tectonics
    )

    // ─────────────────────────────────────────────────────────────────────
    //  SAMPLE ENTRIES (for Cabinet + EntryDetail — placeholder phase)
    // ─────────────────────────────────────────────────────────────────────

    private val sampleEntriesRaw: List<CurioEntry> = listOf(
        CurioEntry(
            id = "entry-1",
            topic = musicAll[11], // Bowie — Ziggy Stardust
            capturedAtDaysAgo = 0,
            format = CaptureFormat.SoundBite,
            bodyPreview = "Voice note — 42s",
            bodyContent = "The thing about Ziggy is the cover photo IS the music — Bowie spent an hour with Brian Ward getting the lighting right. The songs are slower than you'd remember. \"Five Years\" opens with Bowie counting down the death of Ziggy Stardust's world. By the encore \"Rock 'n' Roll Suicide\" he's screaming the lyrics because Ziggy is dying. Save the back half of the album for a second listen."
        ),
        CurioEntry(
            id = "entry-2",
            topic = moviesAll[0], // 2001
            capturedAtDaysAgo = 2,
            format = CaptureFormat.ReelNotes,
            bodyPreview = "5 out of 5 — the silence in space scenes was real, recorded live on set.",
            bodyContent = "Kubrick refused to use library sound effects for the silent space scenes — he recorded actual silence in a soundstage and mixed it down. The astronauts' breathers are the only sound. When you watch the movie you can hear the actors breathing through the dialogue scenes — they had to act between breaths because the suit microphones picked up everything. The Star Child at the end is Kubrick's son — he filmed him over a weekend."
        ),
        CurioEntry(
            id = "entry-3",
            topic = booksAll[0], // Le Guin — Dispossessed
            capturedAtDaysAgo = 5,
            format = CaptureFormat.Marginalia,
            bodyPreview = "\"The proper function of a government is to make it easy for the people to live without government.\"",
            bodyContent = "Le Guin wrote this in 1974 — 4 years after the Stonewall riots, 5 years before Reagan. The novel was written during Vietnam and is set on a planet that is essentially an anarchist commune. The central paradox is that anarchist societies still have prisons — for people who want to leave. The book is about the impossibility of any political system being perfect. Read chapter 9 twice — it's the utopia's founding myth, and it's longer than you'd expect."
        ),
        CurioEntry(
            id = "entry-4",
            topic = scienceAll[0], // Mycelium
            capturedAtDaysAgo = 14,
            format = CaptureFormat.FieldNotes,
            bodyPreview = "Field observations — mycorrhizal networks in coastal PNW forest.",
            bodyContent = "What surprised me: the largest trees in a forest are also the most generous donors of carbon to the smallest. Suzanne Simard's research showed Douglas firs share sugar with hemlock seedlings in the spring — when the seedlings are still too small to photosynthesize effectively. The mycelium network is the mechanism: it connects the roots of nearly 90% of land plants. The forest is one big organism, and the trees that seem most independent are actually most interconnected. Read Finding the Mother Tree if you want the full research story — it's a page-turner."
        )
    )

    // ─────────────────────────────────────────────────────────────────────
    //  PUBLIC API
    // ─────────────────────────────────────────────────────────────────────

    /** All Music topics, regardless of genre. */
    val musicPoolAll: List<CurioTopic> get() = musicAll

    /**
     * Music topics indexed by genre. `null` value means the genre has no
     * topics yet — callers should fall back to [musicPoolAll] in that
     * case. The keys are every [MusicGenre] enum value; ALL maps to the
     * complete music pool.
     */
    val musicPoolByGenre: Map<MusicGenre, List<CurioTopic>> by lazy {
        mapOf(
            MusicGenre.ROCK to musicAll.filter { it.musicGenre == MusicGenre.ROCK },
            MusicGenre.JAZZ to musicAll.filter { it.musicGenre == MusicGenre.JAZZ },
            MusicGenre.CLASSICAL to musicAll.filter { it.musicGenre == MusicGenre.CLASSICAL },
            MusicGenre.HIP_HOP to musicAll.filter { it.musicGenre == MusicGenre.HIP_HOP },
            MusicGenre.ELECTRONIC to musicAll.filter { it.musicGenre == MusicGenre.ELECTRONIC },
            MusicGenre.INDIE to musicAll.filter { it.musicGenre == MusicGenre.INDIE },
            MusicGenre.FOLK to musicAll.filter { it.musicGenre == MusicGenre.FOLK },
            MusicGenre.WORLD to musicAll.filter { it.musicGenre == MusicGenre.WORLD },
            MusicGenre.R_AND_B to musicAll.filter { it.musicGenre == MusicGenre.R_AND_B }
        )
    }

    /** Movies pool — 15 topics. */
    val moviesPool: List<CurioTopic> get() = moviesAll

    /** Books pool — 15 topics. */
    val booksPool: List<CurioTopic> get() = booksAll

    /** Visual Art pool — 15 topics. */
    val artPool: List<CurioTopic> get() = artAll

    /** Science & Nature pool — 15 topics. */
    val sciencePool: List<CurioTopic> get() = scienceAll

    /** Wildcard pool — 10 mixed topics across the 5 named categories. */
    val wildcardPool: List<CurioTopic> get() = wildcardAll

    /**
     * Return the topic pool for a category. Music returns the full pool
     * regardless of the genre filter — the Spin screen handles genre
     * filtering itself with [musicPoolByGenre] + a separate selectedGenre
     * state.
     */
    fun poolFor(categoryId: CategoryId): List<CurioTopic> = when (categoryId) {
        CategoryId.MUSIC      -> musicPoolAll
        CategoryId.MOVIES     -> moviesPool
        CategoryId.BOOKS      -> booksPool
        CategoryId.VISUAL_ART -> artPool
        CategoryId.SCIENCE    -> sciencePool
        CategoryId.WILDCARD   -> wildcardPool
    }

    /**
     * Look up a topic by its [id]. Returns null if no match. Used by
     * downstream consumers (TopicRevealScreen, SaveCaptureScreen,
     * EntryDetailScreen) to find the topic from a route argument.
     */
    fun findById(id: String): CurioTopic? =
        (musicAll + moviesAll + booksAll + artAll + scienceAll + wildcardAll)
            .find { it.id == id }

    /**
     * Look up a topic by its [name]. Returns null if no match. Used by
     * routes that pass the topic name as an argument (legacy fallback
     * until Phase 4 routes carry topic IDs).
     */
    fun findByName(name: String): CurioTopic? =
        (musicAll + moviesAll + booksAll + artAll + scienceAll + wildcardAll)
            .find { it.name == name }

    /** Sample cabinet entries for Cabinet + EntryDetail screens. */
    val sampleEntries: List<CurioEntry> get() = sampleEntriesRaw

    /**
     * Total catalog size — useful for sanity checks in startup logging.
     */
    val totalCount: Int get() =
        musicAll.size + moviesAll.size + booksAll.size +
            artAll.size + scienceAll.size + wildcardAll.size
}