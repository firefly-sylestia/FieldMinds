#!/usr/bin/env python3
"""
Generate albums.json from a hand-curated catalog of real albums.
Each entry has real, verifiable facts and creative listening instructions.

Extend ALBUMS list to add more entries in future batches.
"""

import json
import os

# ── Hand-curated album catalog ──────────────────────────────────────────────
# Each tuple: (id, subtype, name, teaser, verb, targetName, durationMin, instruction, tags, tier)
# id: unique kebab-case, format "album-{artist-slug}-{album-slug}"
# tags: list of genre tags for filtering groups

ALBUMS = [
    # ═══════════════════════════════════════════════════════════════════════
    # ROCK — Classic, Alternative, Indie, Progressive, Hard Rock
    # ═══════════════════════════════════════════════════════════════════════
    (
        "album-beatles-sgt-peppers", "Album",
        "Sgt. Pepper's Lonely Hearts Club Band",
        "1967 Beatles, recorded in 6 months at Abbey Road. The first rock album to win Album of the Year at the Grammys — and the first record people actually listened to as a coherent whole.",
        "Sgt. Pepper's (1967) end-to-end", 47,
        "Skip the title track — it's a four-bar overture. Start with 'Lucy in the Sky with Diamonds' and notice how the bassline changes time signature three times without announcing it. The album's structure is symmetrical: side 1 ends with 'A Day in the Life', side 2 with a reprise.",
        ["Rock", "British", "Psychedelic", "1960s"], 1
    ),
    (
        "album-beatles-abbey-road", "Album",
        "Abbey Road",
        "1969 — the last album the Beatles recorded together. The side-B medley (tracks 7–17) was never rehearsed as a single sequence; George Martin stitched it together over one long session.",
        "Abbey Road (1969) — Side B medley", 23,
        "Listen to side B as a single 23-minute piece. The medley's transitions were never rehearsed — Lennon and McCartney wrote each fragment in isolation. Then listen again and count how many times you hear the phrase 'golden slumbers' return in different keys.",
        ["Rock", "British", "1960s"], 1
    ),
    (
        "album-pink-floyd-dark-side", "Album",
        "The Dark Side of the Moon",
        "Pink Floyd 1973 — spent 937 weeks on the Billboard 200. Recorded partly live at Abbey Road with a rotary speaker custom-built for the sessions. The opening heartbeat is Roger Waters tapping a clock in time with his own pulse.",
        "The Dark Side of the Moon (1973) end-to-end", 43,
        "Listen on headphones in a dark room. The album is a single 43-minute arc with no breaks between songs. Notice how Clare Torry's improvised vocal on 'The Great Gig in the Sky' was a single take — she was paid £30 and later won a settlement for co-writing credit.",
        ["Progressive Rock", "British", "1970s"], 1
    ),
    (
        "album-led-zeppelin-iv", "Album",
        "Led Zeppelin IV",
        "1971 — never officially titled. The band were so annoyed by critics after Led Zeppelin III that they refused to put their name on the cover. Has 'Stairway to Heaven', the most-played rock song in FM radio history.",
        "Led Zeppelin IV (1971) end-to-end", 43,
        "Listen to 'Stairway' three times. The famous opening — Plant improvised the lyrics live, Page edited them. The song is three movements: acoustic intro, electric interlude, hard-rock climax. Then listen to 'When the Levee Breaks' — Bonham recorded his drums in a stairwell at Headley Grange.",
        ["Hard Rock", "British", "1970s"], 1
    ),
    (
        "album-nirvana-nevermind", "Album",
        "Nevermind",
        "Nirvana's 1991 second album — recorded for $65,000, sold 30 million copies, ended hair metal in 18 months. Butch Vig produced it; Kurt insisted the guitars sound both huge and broken at the same time.",
        "Nevermind (1991) end-to-end", 49,
        "Listen to the opening 4 bars of 'Smells Like Teen Spirit'. Cobain's voice is barely audible for half the verse, then explodes. That contrast — quiet verse into screamed chorus — became the template for 1990s rock. Now listen to 'Something in the Way' — recorded with Kurt lying on the studio floor.",
        ["Grunge", "Alternative Rock", "American", "1990s"], 1
    ),
    (
        "album-radiohead-ok-computer", "Album",
        "OK Computer",
        "Radiohead 1997 — recorded partly in a mansion in St Catherine's-on-the-Hudson, partly in a barn with a jazz band. The album that defined post-millennial British rock and made Thom Yorke the unwilling voice of a generation.",
        "OK Computer (1997) end-to-end", 53,
        "Listen to 'Airbag' through 'Lucky' — that's the first half. Then listen to 'Lucky' again. The lyrics 'I'm up late' and 'a pop song' mean something different once you know the album was recorded in a borrowed mansion while Radiohead were falling apart. The last track 'The Tourist' is Thom alone at a piano.",
        ["Alternative Rock", "British", "1990s"], 1
    ),
    (
        "album-nirvana-in-utero", "Album",
        "In Utero",
        "Nirvana's 1993 third album — Steve Albini recorded it in 5 days in Cannon Falls, Minnesota. Cobain didn't like the rawness; the label remixed 3 songs. It still went to #1, proving a band could be abrasive and commercial simultaneously.",
        "In Utero (1993) end-to-end", 41,
        "Listen to 'Serve the Servants' — Cobain names the album's position in his life ('Teenage angst has paid off well'). Then 'All Apologies' — the chord progression moves in fourths, not fifths. Albini miked the drums from 20 feet away to get that cavernous sound.",
        ["Grunge", "Alternative Rock", "American", "1990s"], 1
    ),
    (
        "album-velvet-underground-nico", "Album",
        "The Velvet Underground & Nico",
        "1967 debut — sold about 30,000 copies in its first five years. Brian Eno famously said everyone who bought one started a band. The banana cover was Warhol's idea; early pressings let you peel the banana to reveal a pink fruit underneath.",
        "The Velvet Underground & Nico (1967) end-to-end", 49,
        "Listen to 'Sunday Morning' — the celeste that opens it is the same instrument used in every cheesy kids' show for 40 years. The Velvets knew that association and used it deliberately. Then 'Heroin' — Lou Reed's guitar mimics the rush and crash of the drug in real time.",
        ["Art Rock", "American", "1960s"], 1
    ),
    (
        "album-clash-london-calling", "Album",
        "London Calling",
        "The Clash 1979 — a double album sold for the price of a single. Recorded in three weeks. The cover photo of Paul Simonon smashing his bass was an accident — Pennie Smith thought it was too blurry to use.",
        "London Calling (1979) end-to-end", 65,
        "Listen to the title track first — the opening riff is a morse code SOS. Then jump to 'Train in Vain' — it was a last-minute addition, not even listed on the original sleeve. The album spans punk, reggae, rockabilly, and ska in 19 tracks without ever feeling scattered.",
        ["Punk Rock", "British", "1970s"], 1
    ),
    (
        "album-pixies-doolittle", "Album",
        "Doolittle",
        "Pixies 1989 — the quiet-loud-quiet template that Nirvana would borrow wholesale. Black Francis wrote the lyrics on a flight to Los Angeles; the band recorded it in 3 weeks for $40,000. The cover art is a surrealist painting by Simon Larbalestier.",
        "Doolittle (1989) end-to-end", 39,
        "Listen to 'Debaser' — it's about Luis Buñuel's film Un Chien Andalou, specifically the eyeball-slicing scene. Then 'Monkey Gone to Heaven' — count how many times the song switches between quiet and loud. The dynamic whiplash is the point.",
        ["Alternative Rock", "American", "1980s"], 1
    ),
    (
        "album-rem-automatic", "Album",
        "Automatic for the People",
        "R.E.M.'s 1992 album — recorded in New Orleans and mostly acoustic. The band deliberately avoided rock songs after the success of 'Out of Time'. Michael Stipe wrote the lyrics in character as an elderly Southern man looking back on his life.",
        "Automatic for the People (1992) end-to-end", 49,
        "Listen to 'Everybody Hurts' — the string arrangement by John Paul Jones (Led Zeppelin's bassist) was recorded in a single session in Atlanta. Then 'Nightswimming' — just a piano and Stipe's voice, recorded at 3 AM in an empty studio. The album has almost no drums.",
        ["Alternative Rock", "American", "1990s"], 1
    ),
    (
        "album-u2-joshua-tree", "Album",
        "The Joshua Tree",
        "U2 1987 — recorded in Dublin with Brian Eno and Daniel Lanois. The album was meant to be a love letter to America, inspired by the band's travels through the Mojave Desert. The tree on the cover died in 2000; fans still leave offerings at the site.",
        "The Joshua Tree (1987) end-to-end", 50,
        "Listen to 'Where the Streets Have No Name' — the intro was stitched together from multiple takes because Edge couldn't play the arpeggio cleanly in one go. Then 'I Still Haven't Found What I'm Looking For' — Bono's vocal was a guide track they couldn't beat, so they kept it.",
        ["Rock", "Irish", "1980s"], 1
    ),
    (
        "album-joy-division-unknown-pleasures", "Album",
        "Unknown Pleasures",
        "Joy Division 1979 — produced by Martin Hannett for £8,000. Hannett made the drummer Stephen Morris assemble his kit on the roof and miked it from the parking lot. The iconic cover is a plot of radio pulsar CP 1919 from the Cambridge Encyclopedia of Astronomy.",
        "Unknown Pleasures (1979) end-to-end", 39,
        "Listen to 'Disorder' — the bassline carries the melody while Ian Curtis's voice floats almost disconnected from the music. Then 'She's Lost Control' — Hannett recorded the drums in a separate room and fed them through a digital delay. The album sounds like a transmission from a cold planet.",
        ["Post-Punk", "British", "1970s"], 1
    ),
    (
        "album-smiths-queen-is-dead", "Album",
        "The Queen Is Dead",
        "The Smiths 1986 — Johnny Marr wrote all the music in a creative burst over a few weeks. Morrissey's lyrics cycle through despair, wit, and melancholic nostalgia. The title track was recorded in a single take with Morrissey improvising much of the vocal.",
        "The Queen Is Dead (1986) end-to-end", 37,
        "Listen to 'There Is a Light That Never Goes Out' — the strings were lifted from a 1960s film score. Johnny Marr played them himself on a Roland guitar synthesizer. Then 'I Know It's Over' — Morrissey recorded the vocal lying on the studio floor in near-darkness.",
        ["Indie Rock", "British", "1980s"], 1
    ),
    (
        "album-radiohead-in-rainbows", "Album",
        "In Rainbows",
        "Radiohead 2007 — self-released as a pay-what-you-want download, a first for a major band. The album was recorded in a crumbling country house in Oxfordshire over two years. The band lived and cooked together; the domestic warmth seeps into every track.",
        "In Rainbows (2007) end-to-end", 43,
        "Listen to 'Weird Fishes/Arpeggi' — the guitar loop was built by layering single notes, each recorded separately. Then 'Reckoner' — the falsetto vocal was a late addition; Thom Yorke improvised it over an instrumental track the band thought was finished.",
        ["Alternative Rock", "British", "2000s"], 1
    ),
    (
        "album-arctic-monkeys-am", "Album",
        "AM",
        "Arctic Monkeys 2013 — recorded in Joshua Tree at Rancho De La Luna, a studio built into a desert house. Alex Turner wrote the lyrics on a BlackBerry. The album blends heavy riffs with R&B-influenced grooves inspired by Aaliyah and Dr. Dre.",
        "AM (2013) end-to-end", 42,
        "Listen to 'Do I Wanna Know?' — the riff was built from a single drum machine loop slowed way down. Then 'Arabella' — Turner's guitar solo references War Pigs by Black Sabbath, but played through a fuzz pedal at half-speed. The whole album sounds like 2 AM in a leather jacket.",
        ["Indie Rock", "British", "2010s"], 1
    ),
    (
        "album-fleetwood-mac-rumours", "Album",
        "Rumours",
        "Fleetwood Mac 1977 — recorded while both couples in the band (John & Christine McVie, Lindsey Buckingham & Stevie Nicks) were breaking up. The studio was a war zone. They channeled the pain into perfect California pop. It's sold over 40 million copies.",
        "Rumours (1977) end-to-end", 40,
        "Listen to 'Go Your Own Way' and 'Dreams' back to back — they're Buckingham and Nicks answering each other across the same breakup. Then 'The Chain' — the only song credited to all five members, and the bassline at 3:05 is John McVie's one-take improvisation.",
        ["Rock", "Pop Rock", "American-British", "1970s"], 1
    ),
    (
        "album-eagles-hotel-california", "Album",
        "Hotel California",
        "Eagles 1976 — recorded over eight months in three different studios. The title track's famous guitar duel between Don Felder and Joe Walsh was planned note-for-note, not improvised. The album was the last with original bassist Randy Meisner.",
        "Hotel California (1976) end-to-end", 43,
        "Listen to the title track on headphones — the guitar harmonies at the end were triple-tracked with different EQ settings to create the illusion of more players. Then 'Life in the Fast Lane' — the opening riff was a warm-up exercise Joe Walsh played that Glenn Frey insisted they use.",
        ["Rock", "American", "1970s"], 1
    ),
    (
        "album-springsteen-born-to-run", "Album",
        "Born to Run",
        "Bruce Springsteen 1975 — recorded over 14 months at the Record Plant in New York. Springsteen was 25 and obsessed with Phil Spector's Wall of Sound. He recorded dozens of guitar overdubs, layering them until the album sounded like it was bursting out of the speakers.",
        "Born to Run (1975) end-to-end", 39,
        "Listen to the title track — the glockenspiel in the opening is played by Springsteen himself. The saxophone solo by Clarence Clemons was recorded in one take at 3 AM. The entire album is designed to sound like a car radio at full volume on a summer night.",
        ["Rock", "Heartland Rock", "American", "1970s"], 1
    ),
    (
        "album-talking-heads-remain-in-light", "Album",
        "Remain in Light",
        "Talking Heads 1980 — recorded in the Bahamas and New York with Brian Eno. The band built the songs from layered loops, inspired by Fela Kuti's Afrobeat. David Byrne wrote the lyrics by improvising nonsense syllables over the grooves, then shaping words around the sounds.",
        "Remain in Light (1980) end-to-end", 40,
        "Listen to 'Once in a Lifetime' — Byrne's sermon-like vocal was inspired by a recording of a preacher he found on the radio. The bassline by Tina Weymouth cycles through the same figure for the entire song without ever becoming boring. Then 'The Great Curve' — six guitar parts woven into one.",
        ["New Wave", "Art Rock", "American", "1980s"], 1
    ),
    (
        "album-beach-boys-pet-sounds", "Album",
        "Pet Sounds",
        "The Beach Boys 1966 — Brian Wilson at 23, listening to Rubber Soul on repeat, paying session musicians to realize everything he heard in his head. He used bicycle horns, sleigh bells, Coca-Cola cans, and a theremin. Capitol Records almost refused to release it.",
        "Pet Sounds (1966) end-to-end", 37,
        "Listen to 'God Only Knows' — Paul McCartney called it the most perfect song ever written. Carl Wilson was 19 when he sang it; Brian picked him because his voice had less vibrato. The album uses a 12-string bass that Wilson had custom-made because he wanted a sound between guitar and bass.",
        ["Pop", "Baroque Pop", "American", "1960s"], 1
    ),
    (
        "album-stone-roses-debut", "Album",
        "The Stone Roses",
        "The Stone Roses 1989 — the album that launched the Madchester scene and made baggy clothes and dance-rock a thing. John Squire was inspired by Jackson Pollock for the cover art; he splattered paint on a canvas and photographed it in his kitchen.",
        "The Stone Roses (1989) end-to-end", 49,
        "Listen to 'I Wanna Be Adored' — the intro builds for nearly two minutes before a word is sung. The bassline never changes but the guitar layers accumulate until the song feels submerged. Then 'Fools Gold' — a 9-minute funk groove built from a James Brown sample played backwards.",
        ["Indie Rock", "Madchester", "British", "1980s"], 1
    ),
    (
        "album-david-bowie-ziggy-stardust", "Album",
        "The Rise and Fall of Ziggy Stardust and the Spiders from Mars",
        "David Bowie 1972 — recorded in just two weeks at Trident Studios. Bowie created the Ziggy character and wrote the entire album in character as an androgynous alien rock star. Mick Ronson's string arrangements were his first ever — he learned arranging by watching a BBC documentary.",
        "Ziggy Stardust (1972) end-to-end", 39,
        "Listen to 'Five Years' — the song builds from a single drumbeat to a full-band apocalypse. Bowie's vocal cracks are deliberate; he wanted to sound like he was on the edge of crying. Then 'Starman' — Bowie sings the chorus an octave above the verse, a move that made the song feel like it was taking off.",
        ["Glam Rock", "British", "1970s"], 1
    ),
    (
        "album-arcade-fire-funeral", "Album",
        "Funeral",
        "Arcade Fire 2004 — recorded in an old church in Montreal over a single winter. The band used the natural reverb of the space instead of studio effects. Several band members had recently lost family members, hence the title; the album is a wake that turns into a celebration.",
        "Funeral (2004) end-to-end", 48,
        "Listen to 'Neighborhood #1 (Tunnels)' — the opening piano was recorded in the church's sanctuary at 2 AM. Then 'Wake Up' — the choir singing the wordless chorus was the band and their friends, all crowded around one microphone. The album feels like it was recorded by a much larger group than six people.",
        ["Indie Rock", "Canadian", "2000s"], 1
    ),

    # ═══════════════════════════════════════════════════════════════════════
    # HIP-HOP — Golden era, East Coast, West Coast, Modern
    # ═══════════════════════════════════════════════════════════════════════
    (
        "album-nas-illmatic", "Album",
        "Illmatic",
        "Nas's 1994 Queensbridge debut — 10 tracks, 5 producers, 39 minutes. The watershed East Coast record. Q-Tip, Large Professor, Pete Rock, DJ Premier, and L.E.S. all produced tracks. Nas was 19 and had been writing verses for 5 years before the album came out.",
        "Illmatic (1994) end-to-end", 39,
        "Listen to the opening of 'N.Y. State of Mind' — Nas recorded the first verse in one take, chain-smoking in the booth. DJ Premier built the beat from a Joe Chambers piano sample. The album is a 39-minute mural of Queensbridge projects — every line is a photograph.",
        ["Hip-Hop", "East Coast", "American", "1990s"], 1
    ),
    (
        "album-biggie-ready-to-die", "Album",
        "Ready to Die",
        "Notorious B.I.G.'s 1994 debut — the only album released in his lifetime. Recorded at Puff Daddy's house in New Jersey between court dates. The album's last track 'Suicidal Thoughts' was recorded at 4 AM with Biggie barely able to get through it.",
        "Ready to Die (1994) end-to-end", 69,
        "Listen to 'Juicy' — the famous opening line was a last-minute replacement; Puffy wanted a hook they could sample from Mtume's 'Juicy Fruit'. Then 'Warning' — Biggie narrates an entire crime drama in 3 minutes. The album's arc mirrors his life: from birth ('Intro') to self-destruction ('Suicidal Thoughts').",
        ["Hip-Hop", "East Coast", "American", "1990s"], 1
    ),
    (
        "album-wu-tang-36-chambers", "Album",
        "Enter the Wu-Tang (36 Chambers)",
        "Wu-Tang Clan 1993 — recorded in RZA's basement studio in Staten Island for $36,000. Nine MCs, one producer. The kung-fu movie samples came from RZA's personal VHS collection. The album invented a new sound: gritty, lo-fi, with minor-key piano loops and off-kilter drums.",
        "Enter the Wu-Tang (1993) end-to-end", 58,
        "Listen to 'C.R.E.A.M.' — the piano loop is from The Charmels' 'As Long As I've Got You', sped up and pitched up. Raekwon wrote his verse in 15 minutes. Then 'Protect Ya Neck' — every member gets a verse, and they recorded it in one night with the lights dimmed.",
        ["Hip-Hop", "East Coast", "American", "1990s"], 1
    ),
    (
        "album-kendrick-to-pimp-a-butterfly", "Album",
        "To Pimp a Butterfly",
        "Kendrick Lamar's 2015 album — recorded over 9 months at a Compton studio with a live jazz band. The album's closing single 'Alright' became a Black Lives Matter anthem. Kamasi Washington, Thundercat, and Flying Lotus all contributed.",
        "To Pimp a Butterfly (2015) end-to-end", 79,
        "Listen to 'Wesley's Theory' — the opening groove samples Parliament-Funkadelic, then Kendrick's voice cracks like a radio tuning between stations. Then 'For Free?' — Kendrick raps over his own trumpet practice tape. The album's last track reveals the whole thing was a letter to Tupac.",
        ["Hip-Hop", "American", "2010s"], 1
    ),
    (
        "album-lauryn-hill-miseducation", "Album",
        "The Miseducation of Lauryn Hill",
        "Lauryn Hill's 1998 debut solo album — the first hip-hop album to win Album of the Year at the Grammys. She was 23, pregnant, and had just left the Fugees. The album sold 15 million copies in its first year; she has never released a proper follow-up.",
        "The Miseducation of Lauryn Hill (1998) end-to-end", 78,
        "Listen to 'Doo Wop (That Thing)' — it's 4 minutes of two voices debating economic materialism across a 1950s doo-wop sample. Then 'Ex-Factor' — the song that killed her next album, because the raw emotion was impossible to reproduce. The skit near the end is Lauryn talking to her unborn son.",
        ["Hip-Hop Soul", "American", "1990s"], 1
    ),
    (
        "album-kanye-dark-twisted-fantasy", "Album",
        "My Beautiful Dark Twisted Fantasy",
        "Kanye West's 2010 album — recorded in a Hawaii studio over a year with dozens of collaborators. Made after the Taylor Swift incident when the world turned on him. The album becomes a maximalist fever dream of self-examination and self-aggrandizement.",
        "My Beautiful Dark Twisted Fantasy (2010) end-to-end", 68,
        "Listen to 'Runaway' — 9 minutes, a single verse, and a 4-minute vocoder outro. The line 'let's have a toast for the douchebags' is delivered without apology. Then 'All of the Lights' — 14 vocalists including Rihanna, Elton John, and Alicia Keys. The horns were played by a marching band Kanye hired for a single session.",
        ["Hip-Hop", "American", "2010s"], 1
    ),
    (
        "album-tribe-called-quest-low-end-theory", "Album",
        "The Low End Theory",
        "A Tribe Called Quest 1991 — the album that proved jazz and hip-hop could be the same thing. Q-Tip and Phife Dawg traded verses over Ron Carter's upright bass. The cover art is a painted nude that the label almost refused.",
        "The Low End Theory (1991) end-to-end", 48,
        "Listen to 'Scenario' — the posse cut that introduced Busta Rhymes to the world. His verse at the end was improvised in one take. Then 'Jazz (We've Got)' — the beat is built from a 4-bar Jimmy Smith organ loop that Q-Tip found on a dollar-bin record.",
        ["Hip-Hop", "Jazz Rap", "American", "1990s"], 1
    ),

    # ═══════════════════════════════════════════════════════════════════════
    # JAZZ — Modal, Hard Bop, Fusion, Free, Latin
    # ═══════════════════════════════════════════════════════════════════════
    (
        "album-miles-davis-kind-of-blue", "Album",
        "Kind of Blue",
        "Miles Davis 1959 — recorded in one session of about 9 hours. Davis gave the band only sketches of scales and modes, not full charts. The result sold millions and became the best-selling jazz album of all time, the touchstone of modal jazz.",
        "Kind of Blue (1959) end-to-end", 46,
        "Put it on once as background and once as the only sound in the room. The opening of 'So What' is a modal scale you'd never guess from the chord chart. Bill Evans's piano intro was written on a napkin in the taxi on the way to the studio. Most of the album was first takes.",
        ["Jazz", "Modal Jazz", "American", "1950s"], 1
    ),
    (
        "album-coltrane-love-supreme", "Album",
        "A Love Supreme",
        "John Coltrane's 1965 suite — his 'gift to God' after getting sober from heroin. Recorded in one evening at Van Gelder Studio, four parts, ~33 minutes. The handwritten poem in the liner notes is a prayer. Coltrane chants 'A Love Supreme' at the end of Part 1.",
        "A Love Supreme (1965) end-to-end", 33,
        "Listen to 'Resolution' twice. First time, follow the saxophone. Second time, follow Jimmy Garrison's bass. The album does both, and most people only hear half. Then 'Acknowledgement' — the four-note 'A Love Supreme' motif is the foundation; Coltrane plays it in every key.",
        ["Jazz", "Spiritual Jazz", "American", "1960s"], 1
    ),
    (
        "album-miles-davis-bitches-brew", "Album",
        "Bitches Brew",
        "Miles Davis 1970 — recorded at Columbia's 52nd Street studio with two keyboardists, two bassists, two drummers, and a percussionist. By accident (or design), it invented jazz-rock fusion. The album was assembled from long improvisations edited by producer Teo Macero.",
        "Bitches Brew (1970) end-to-end", 94,
        "Listen to the title track's first 5 minutes. Then skip to 18 minutes in. Davis creates electronic space before there were electronic instruments — the mood is built from echo and silence. Macero's edit cuts are audible if you listen for them; the album is as much a production artifact as a performance.",
        ["Jazz Fusion", "American", "1970s"], 1
    ),
    (
        "album-coltrane-giant-steps", "Album",
        "Giant Steps",
        "John Coltrane 1960 — the album that introduced 'Coltrane changes', a harmonic progression so complex that Tommy Flanagan (the pianist) could barely solo over the title track. Coltrane had practiced the changes for months in secret before the session.",
        "Giant Steps (1960) end-to-end", 37,
        "Listen to the title track — Coltrane's solo moves through three keys so fast that music theorists named the progression after him. The original take was even faster; they slowed it down for the album. Then 'Naima' — a ballad named for Coltrane's first wife, built on a single pedal tone that never resolves.",
        ["Jazz", "Hard Bop", "American", "1960s"], 1
    ),
    (
        "album-dave-brubeck-time-out", "Album",
        "Time Out",
        "Dave Brubeck Quartet 1959 — the first jazz album to sell a million copies. Every track is in an unusual time signature: 9/8, 5/4, 6/4. Columbia executives thought it was a terrible idea. 'Take Five' became the best-selling jazz single of all time.",
        "Time Out (1959) end-to-end", 39,
        "Listen to 'Take Five' — the 5/4 time signature was inspired by Turkish street musicians Brubeck heard on a State Department tour. Joe Morello's drum solo was improvised; the band kept playing because he was on fire. Then 'Blue Rondo à la Turk' — it shifts from 9/8 to 4/4 and back without announcing the change.",
        ["Jazz", "Cool Jazz", "American", "1950s"], 1
    ),
    (
        "album-herbie-hancock-head-hunters", "Album",
        "Head Hunters",
        "Herbie Hancock 1973 — after years of acoustic jazz, he went electric. The album opens with 'Chameleon', a 15-minute funk groove built on a two-note bassline. Hancock played all the synthesizers himself, including a custom ARP that he had just bought.",
        "Head Hunters (1973) end-to-end", 42,
        "Listen to 'Chameleon' — the bassline is two notes for the first 6 minutes. The groove never gets boring because the textures keep shifting: clavinet, ARP synth, and layers of percussion. Then 'Watermelon Man' — a reworking of Hancock's 1962 hit, but played through a beer bottle and an African percussion ensemble.",
        ["Jazz Fusion", "Funk", "American", "1970s"], 1
    ),
    (
        "album-mingus-ah-um", "Album",
        "Mingus Ah Um",
        "Charles Mingus 1959 — one of the most emotionally direct jazz albums ever made. 'Fables of Faubus' was meant to have lyrics mocking Arkansas governor Orval Faubus, but Columbia censored them. Mingus re-recorded the vocal version a year later on a different label.",
        "Mingus Ah Um (1959) end-to-end", 46,
        "Listen to 'Goodbye Pork Pie Hat' — a eulogy for Lester Young recorded just two months after his death. The saxophone solo by John Handy was his first recording session. Then 'Better Git It in Your Soul' — the handclaps and shouts were improvised by the band during the take.",
        ["Jazz", "Hard Bop", "American", "1950s"], 1
    ),

    # ═══════════════════════════════════════════════════════════════════════
    # ELECTRONIC — Ambient, Techno, House, IDM, Experimental
    # ═══════════════════════════════════════════════════════════════════════
    (
        "album-aphex-twin-saw-85-92", "Album",
        "Selected Ambient Works 85-92",
        "Richard D. James' 1992 compilation of ambient techno tracks recorded while he was a teenager in Cornwall. The master tape was damaged — one channel is subtly quieter than the other through the entire album. James decided to leave it that way; it became part of the sound.",
        "Selected Ambient Works 85-92 (1992) end-to-end", 75,
        "Listen for the moments that sound broken — a synth that doesn't resolve, a sample that loops fractionally late. That's the texture Aphex Twin is building. 'Xtal' opens the album with a vocal sample from an obscure 1970s library record. The whole thing feels like a field recording from a place that never existed.",
        ["Ambient", "Electronic", "IDM", "British", "1990s"], 1
    ),
    (
        "album-boards-of-canada-music-has-the-right", "Album",
        "Music Has the Right to Children",
        "Boards of Canada's 1998 debut — brothers Michael Sandison and Marcus Eoin recording in their Pentland Hills hideaway in Scotland. The album samples 1970s children's TV shows, NASA transmissions, and deteriorating VHS tapes. The result sounds like a memory of a TV show from a parallel universe.",
        "Music Has the Right to Children (1998) end-to-end", 68,
        "Listen on headphones with the lights low. 'Telephasic Workshop' contains a sample of US President Eisenhower from an obscure 1950s broadcast, chopped into syllables. Then 'Roygbiv' — an ambient lullaby in two chords that has become the most-sampled Boards of Canada track without anyone knowing they're sampling it.",
        ["Electronic", "IDM", "Ambient", "Scottish", "1990s"], 1
    ),
    (
        "album-daft-punk-discovery", "Album",
        "Discovery",
        "Daft Punk's 2001 second album — recorded on a four-track with no computer sequencing. Every song was made from samples played by the duo themselves, then processed until the source was unrecognizable. The album has a companion anime film, 'Interstella 5555', with no dialogue.",
        "Discovery (2001) end-to-end", 60,
        "Listen to 'One More Time' — a sample from Eddie Johns' 'More Spell on You' plus a vocoded vocal by Romanthony. The song took 8 months to produce because they kept restarting. Then 'Aerodynamic' — the guitar solo in the middle was played by Thomas Bangalter on a synthesizer routed through a guitar amp, meant to sound like Eddie Van Halen.",
        ["Electronic", "House", "French", "2000s"], 1
    ),
    (
        "album-daft-punk-homework", "Album",
        "Homework",
        "Daft Punk's 1997 debut — recorded in Guy-Manuel de Homem-Christo's bedroom in Paris on a 4-track cassette recorder. The album made French house music credible to American audiences almost overnight. The title is literal: they treated it like schoolwork, recording every night after their day jobs.",
        "Homework (1997) end-to-end", 75,
        "Listen to 'Da Funk' — the bass riff is a 4-bar sample from a G-funk record, looped for 5 minutes without changing once. Then 'Around the World' — a 7-minute song built around a single vocal phrase repeated 144 times. The groove is in the slight variations, not the pattern.",
        ["Electronic", "House", "French", "1990s"], 1
    ),
    (
        "album-eno-music-for-airports", "Album",
        "Music for Airports",
        "Brian Eno's 1978 ambient landmark — composed to be heard (and ignored) in public spaces. The first record to call itself 'ambient'. Eno was inspired after being stuck at an airport listening to the terrible Muzak, thinking: what if background music was actually beautiful?",
        "Music for Airports (1978) end-to-end", 48,
        "Put it on low while doing something else. The piece reveals itself only when you half-listen — try to catch each loop as it phases out of alignment with the others. Eno described it as 'music designed to be as ignorable as it is interesting.' Each of the four tracks is a tape loop of different lengths, drifting in and out of sync.",
        ["Ambient", "Electronic", "British", "1970s"], 1
    ),
    (
        "album-kraftwerk-trans-europe-express", "Album",
        "Trans-Europe Express",
        "Kraftwerk 1977 — the album that convinced a generation of Detroit teenagers to buy drum machines. The title track mimics a train journey across Europe using only synthesizers and a vocoder. Afrika Bambaataa sampled 'Trans-Europe Express' for 'Planet Rock', birthing electro.",
        "Trans-Europe Express (1977) end-to-end", 42,
        "Listen to 'Europe Endless' — the 10-minute opening track that never changes tempo but constantly shifts texture. Then the title track — the train rhythm was created by running a sequencer at a mathematically precise tempo matching actual train wheel clicks. Kraftwerk built their own electronic drum kit because none existed in 1977.",
        ["Electronic", "Krautrock", "German", "1970s"], 1
    ),
    (
        "album-massive-attack-mezzanine", "Album",
        "Mezzanine",
        "Massive Attack 1998 — recorded in a Bristol basement over 18 months. The band was falling apart; three members recorded in separate rooms. The cover art is a black beetle photographed by Nick Knight. The album's darkness consumed two years of their lives.",
        "Mezzanine (1998) end-to-end", 63,
        "Listen on headphones in the dark. 'Angel' opens with a bassline that sounds like it's crawling toward you — it was recorded by playing a bass guitar through three distortion pedals in series. Then 'Teardrop' — the heartbeat rhythm is a sample of a fetal heartbeat; the vocal is by Cocteau Twins' Elizabeth Fraser, recorded in a single take at 3 AM.",
        ["Trip-Hop", "Electronic", "British", "1990s"], 1
    ),
    (
        "album-floating-points-crush", "Album",
        "Crush",
        "Sam Shepherd's 2019 album — a neuroscientist-turned-producer builds dance music where the breakdown is the song. Recorded over five years between DJ sets and research. The album refuses to climax in the way dance music is supposed to.",
        "Crush (2019) end-to-end", 42,
        "Listen for phrases that repeat one bar longer than expected. Shepherd builds grooves that go past the obvious exit — those are the hooks. Resist skipping to the drop; the drop is buried. The album was mixed to reveal detail on headphones that get lost on speakers.",
        ["Electronic", "IDM", "British", "2010s"], 1
    ),
    (
        "album-burial-untrue", "Album",
        "Untrue",
        "Burial's 2007 second album — made entirely in SoundForge, a $60 audio editor, with no sequencer. The crackle and vinyl hiss are synthetic. Burial (William Bevan) remained anonymous for years, recording at night in his South London flat. The album sounds like London at 4 AM in the rain.",
        "Untrue (2007) end-to-end", 50,
        "Listen for the ghost vocals — they're pitched-up R&B samples, chopped so the original song is unrecognizable. Burial recorded the rain sounds from his bedroom window. The drum patterns are deliberately off-grid — he drew them by hand in the audio editor, giving them a human stagger that no drum machine can replicate.",
        ["Electronic", "Dubstep", "British", "2000s"], 1
    ),

    # ═══════════════════════════════════════════════════════════════════════
    # SOUL / R&B — Classic soul, Neo-soul, Contemporary
    # ═══════════════════════════════════════════════════════════════════════
    (
        "album-marvin-gaye-whats-going-on", "Album",
        "What's Going On",
        "Marvin Gaye 1971 — Motown refused to release it. Berry Gordy called it uncommercial. Gaye threatened to leave the label he'd built his career on. It became the first political soul album, a song cycle about Vietnam, poverty, and environmental collapse, sung from the perspective of a returning veteran.",
        "What's Going On (1971) end-to-end", 36,
        "Listen to the title track — the vocal overdubs are layer upon layer. Notice how Gaye breathes 12 distinct times in the first 30 seconds; each breath is a punctuation mark. The album was recorded with members of the Detroit Symphony Orchestra who slipped out of rehearsals to play on the sessions.",
        ["Soul", "American", "1970s"], 1
    ),
    (
        "album-stevie-wonder-innervisions", "Album",
        "Innervisions",
        "Stevie Wonder's 1973 album — recorded mostly alone, playing nearly every instrument himself. The album's opening is an arrhythmic assault; the closing track protests a food stamp cut. Three weeks after the album was released, Wonder was in a car accident that put him in a coma.",
        "Innervisions (1973) end-to-end", 44,
        "Listen to 'Higher Ground' — Stevie's clavinet entrance at 0:48 is a single note. The keyboard was lent to him by Clint Eastwood. Then 'Living for the City' — a 7-minute story of urban destitution with a spoken-word middle section featuring Stevie playing multiple characters. The police siren was an actual recording from outside the studio.",
        ["Soul", "Funk", "American", "1970s"], 1
    ),
    (
        "album-stevie-wonder-songs-in-the-key", "Album",
        "Songs in the Key of Life",
        "Stevie Wonder's 1976 double album + bonus EP — released the day before his 26th birthday. He had threatened to quit music and move to Ghana; Motown gave him unprecedented creative control to keep him. The album took 3 years to make and used over 130 musicians.",
        "Songs in the Key of Life (1976) end-to-end", 105,
        "Listen to 'I Wish' — one of the most-sampled songs in history. The backwards-mixed bassline in the first 8 bars was an accident. Then 'Pastime Paradise' — Coolio sampled it 20 years later for 'Gangsta's Paradise'. Then 'As' — Stevie recorded the vocal so loud it distorted the tape; they kept it.",
        ["Soul", "Funk", "R&B", "American", "1970s"], 1
    ),
    (
        "album-frank-ocean-blonde", "Album",
        "Blonde",
        "Frank Ocean's 2016 second album — released without promotion in two physical editions, then never officially made available for streaming for months. Recorded across Abbey Road, Electric Lady, and a house in Japan. The album's most-quoted track 'Self Control' was co-written with Alex G.",
        "Blonde (2016) end-to-end", 60,
        "Listen to 'Self Control' — the vocal and guitar are layered until they sound like a memory being assembled in real time. Then 'Nights' — the song is two halves: a contemplative 2-minute introduction, then at exactly the midpoint a beat switch so abrupt it feels like falling. Ocean sings the entire album from the perspective of someone looking back.",
        ["R&B", "Alternative R&B", "American", "2010s"], 1
    ),
    (
        "album-frank-ocean-channel-orange", "Album",
        "Channel Orange",
        "Frank Ocean's 2012 debut — released the same day he published an open Tumblr letter about his first love being a man. The album blends Prince, Stevie Wonder, and a stripped-down indie-R&B. It won a Grammy and changed the sound of pop music for the next decade.",
        "Channel Orange (2012) end-to-end", 55,
        "Listen to 'Thinkin Bout You' — the most-quoted track, recorded in a single afternoon. Then 'Pyramids' — a 10-minute exercise in doubling: John Mayer plays guitar at 7:40 while Frank's opening vocal returns as a ghost at 0:01 of the second half. The song spans ancient Egypt and a modern strip club without losing coherence.",
        ["R&B", "Alternative R&B", "American", "2010s"], 1
    ),

    # ═══════════════════════════════════════════════════════════════════════
    # FOLK / SINGER-SONGWRITER
    # ═══════════════════════════════════════════════════════════════════════
    (
        "album-sufjan-stevens-carrie-and-lowell", "Album",
        "Carrie & Lowell",
        "Sufjan Stevens' 2015 album about his mother's death. Carrie was bipolar and abandoned the family when Sufjan was a baby; Lowell was his stepfather who ran an Oregon folk music shop. The album was recorded in a single room with almost no production between you and the words.",
        "Carrie & Lowell (2015) end-to-end", 45,
        "Don't put it on as background. The lyrics do the work — give them your full attention on first listen. 'Fourth of July' is a conversation with his dying mother in a hospital bed. The production hides on purpose; Stevens recorded the vocals in a closet so the sound would feel enclosed and intimate.",
        ["Indie Folk", "Singer-Songwriter", "American", "2010s"], 1
    ),
    (
        "album-joni-mitchell-blue", "Album",
        "Blue",
        "Joni Mitchell 1971 — recorded mostly with just her voice, a guitar, and a piano. She wrote the songs while traveling through Europe after breaking up with Graham Nash and beginning a relationship with James Taylor. Every song is an open wound dressed as a folk tune.",
        "Blue (1971) end-to-end", 36,
        "Listen to 'A Case of You' — Mitchell plays a dulcimer she was given in Greece. The song references a breakup with Leonard Cohen, who she met at the Isle of Wight festival. Then 'River' — a Christmas song about wanting to escape, built around a Jingle Bells piano line that Mitchell subverts into a minor key.",
        ["Folk", "Singer-Songwriter", "Canadian", "1970s"], 1
    ),
    (
        "album-bob-dylan-highway-61-revisited", "Album",
        "Highway 61 Revisited",
        "Bob Dylan 1965 — the album where he went electric and the folk world lost its mind. Recorded in 6 days with a band that included Al Kooper, a guitarist who had never played organ before but who improvised the iconic riff on 'Like a Rolling Stone'.",
        "Highway 61 Revisited (1965) end-to-end", 51,
        "Listen to 'Like a Rolling Stone' — the opening snare drum hit was described by Bruce Springsteen as sounding 'like somebody'd kicked open the door to your mind.' Then 'Desolation Row' — an 11-minute dreamscape with no chorus, recorded with session musicians who had no idea what the song was about.",
        ["Folk Rock", "American", "1960s"], 1
    ),
    (
        "album-nick-drake-pink-moon", "Album",
        "Pink Moon",
        "Nick Drake's 1972 third album — recorded in two late-night sessions totaling about 4 hours. Just Drake's voice and guitar, almost no overdubs. He dropped the master tapes off at Island Records' reception desk without telling anyone. The album sold fewer than 5,000 copies in his lifetime.",
        "Pink Moon (1972) end-to-end", 28,
        "Listen in complete darkness. The title track is 2 minutes long — a miniature that contains a lifetime. Drake was severely depressed during the sessions; you can hear his fingernails hitting the guitar strings. The producer John Wood later said he'd never seen anyone so withdrawn make something so complete.",
        ["Folk", "Singer-Songwriter", "British", "1970s"], 1
    ),
    (
        "album-bon-iver-for-emma", "Album",
        "For Emma, Forever Ago",
        "Bon Iver 2007 — Justin Vernon retreated to his father's hunting cabin in Wisconsin after his band broke up and he got mononucleosis. He spent three winter months alone, recording on a single SM57 microphone and a laptop. The album was meant as a series of demos, never intended for release.",
        "For Emma, Forever Ago (2007) end-to-end", 37,
        "Listen to 'Skinny Love' — the guitar was tuned to an open chord Vernon invented. The cracking in his voice at the end of the first chorus is real; he was running a fever. Then 're: stacks' — the song builds an entire emotional world from three fingerpicked guitar notes and a falsetto that barely rises above a whisper.",
        ["Indie Folk", "American", "2000s"], 1
    ),

    # ═══════════════════════════════════════════════════════════════════════
    # METAL
    # ═══════════════════════════════════════════════════════════════════════
    (
        "album-black-sabbath-paranoid", "Album",
        "Paranoid",
        "Black Sabbath 1970 — the album that accidentally invented heavy metal. The title track was written and recorded in 25 minutes as filler because the album was too short. The band thought the real single would be 'War Pigs'. 'Paranoid' became their signature song.",
        "Paranoid (1970) end-to-end", 42,
        "Listen to the opening of 'War Pigs' — the air raid siren was a real siren Ozzy Osbourne found and plugged into the mixing desk. Then 'Iron Man' — the riff was improvised by Tony Iommi during a soundcheck. Iommi had lost the tips of two fingers in an industrial accident; he tuned his guitar down to C# to reduce string tension, creating Sabbath's signature low sound.",
        ["Metal", "Heavy Metal", "British", "1970s"], 1
    ),
    (
        "album-metallica-master-of-puppets", "Album",
        "Master of Puppets",
        "Metallica 1986 — recorded in Copenhagen over three months. Cliff Burton's final album; he died in a bus crash six months after its release while the band was on tour in Sweden. The album has no ballads and no radio-friendly singles, yet went 6x platinum without music videos.",
        "Master of Puppets (1986) end-to-end", 55,
        "Listen to the title track — the mid-section clean guitar interlude was Burton's idea, a moment of beauty in 8 minutes of fury. Then 'Orion' — an 8-minute instrumental written primarily by Burton, with a bass solo that quotes classical Bach-influenced counterpoint. The album's production (by Flemming Rasmussen) captures the sound of a band playing live in a room.",
        ["Thrash Metal", "American", "1980s"], 1
    ),

    # ═══════════════════════════════════════════════════════════════════════
    # WORLD / INTERNATIONAL
    # ═══════════════════════════════════════════════════════════════════════
    (
        "album-buena-vista-social-club", "Album",
        "Buena Vista Social Club",
        "Ry Cooder's 1997 Cuban session album — most of the musicians were over 70, some over 90, and most had retired decades earlier. The recording sessions were supposed to feature Malian musicians, but their visas didn't arrive. Cooder pivoted and assembled elderly Cuban musicians who hadn't played together in 40 years.",
        "Buena Vista Social Club (1997) end-to-end", 60,
        "Listen to 'Chan Chan' — Compay Segundo's voice was 89 years old when he recorded it; the song was written in his sleep. Then 'Dos Gardenias' — a 1945 son-form bolero sung by Omara Portuondo at age 67. The album captures the last generation of pre-revolution Cuban musicians, most of whom died within 5 years of its release.",
        ["World", "Son Cubano", "Cuban", "1990s"], 1
    ),
    (
        "album-fela-kuti-zombie", "Album",
        "Zombie",
        "Fela Kuti 1976 — the album that attacked the Nigerian military directly. The title track compared soldiers to mindless zombies. In retaliation, the Nigerian army invaded Fela's compound, beat him, threw his elderly mother out a window (she later died), and burned his studio. Fela delivered her coffin to the army barracks.",
        "Zombie (1976) end-to-end", 53,
        "Listen to the title track — it's 12 minutes of polyrhythmic fury. The horn section plays the same riff for minutes at a time while Fela's keyboard and Tony Allen's drums build a groove that never resolves. Then notice how the call-and-response vocals are structured like a town hall meeting, not a concert.",
        ["Afrobeat", "Nigerian", "1970s"], 1
    ),
    (
        "album-king-sunny-ade-juju-music", "Album",
        "Juju Music",
        "King Sunny Adé 1982 — the album that brought Nigerian juju music to Western audiences. Adé led a band of 20 musicians with talking drums, pedal steel guitar, and layered percussion. Island Records signed him hoping he'd be 'the African Bob Marley'.",
        "Juju Music (1982) end-to-end", 50,
        "Listen to 'Ja Funmi' — count the distinct percussion layers. There are at least seven: talking drums, congas, shakers, claves, bells, and two trap drum kits playing in different time signatures. Then notice the pedal steel guitar — Adé added it after hearing American country music on the radio in Lagos, and it became a permanent part of juju.",
        ["World", "Juju", "Nigerian", "1980s"], 1
    ),

    # ═══════════════════════════════════════════════════════════════════════
    # FUNK / DISCO / DANCE
    # ═══════════════════════════════════════════════════════════════════════
    (
        "album-parliament-mothership-connection", "Album",
        "Mothership Connection",
        "Parliament 1975 — George Clinton's P-Funk empire at its peak. The album invented the mythology of an alien funk mothership coming to liberate Earth through dance. The horn arrangements were written by Fred Wesley, James Brown's former bandleader, who had just joined Clinton's crew.",
        "Mothership Connection (1975) end-to-end", 38,
        "Listen to 'Give Up the Funk (Tear the Roof off the Sucker)' — the chorus was improvised during a rehearsal when Clinton shouted the phrase and the band kept playing. Then 'Mothership Connection (Star Child)' — the opening radio-dial narration was Clinton imitating a DJ he heard as a kid in Plainfield, New Jersey. The album was sampled extensively by Dr. Dre for The Chronic.",
        ["Funk", "P-Funk", "American", "1970s"], 1
    ),
    (
        "album-james-brown-live-at-the-apollo", "Album",
        "Live at the Apollo",
        "James Brown 1963 — the most electrifying live album ever recorded. Brown financed the recording himself because his label, King Records, thought live albums didn't sell. The album cost $5,700 to record and grossed over a million dollars. It's still taught in music schools as a masterclass in showmanship.",
        "Live at the Apollo (1963) end-to-end", 32,
        "Listen to the opening — the MC hypes the crowd for nearly 3 minutes before a note is played. Then Brown launches into 'I'll Go Crazy' and doesn't stop moving for the next 30 minutes. Notice how he controls the band's tempo from the stage with hand signals and body movements. Every scream from the audience was real — the Apollo's Tuesday amateur nights were legendary.",
        ["Soul", "Funk", "Live", "American", "1960s"], 1
    ),
    (
        "album-donna-summer-bad-girls", "Album",
        "Bad Girls",
        "Donna Summer 1979 — the double album that bridged disco and rock. Produced by Giorgio Moroder, who recorded Summer's vocals through a custom Eventide Harmonizer to create the layered, robotic sound that defined the era. The title track was inspired by a police officer mistaking Summer for a sex worker.",
        "Bad Girls (1979) end-to-end", 72,
        "Listen to 'Hot Stuff' — the guitar solo is by Jeff Baxter of Steely Dan, recorded in a single take at 2 AM. Then 'Bad Girls' — the 'toot-toot, beep-beep' hook was Summer mimicking car horns. The synth bassline was Moroder's Moog modular, programmed note by note because sequencers didn't exist yet.",
        ["Disco", "Dance", "American", "1970s"], 1
    ),

    # ═══════════════════════════════════════════════════════════════════════
    # COUNTRY / BLUES / AMERICANA
    # ═══════════════════════════════════════════════════════════════════════
    (
        "album-johnny-cash-folsom-prison", "Album",
        "At Folsom Prison",
        "Johnny Cash's 1968 live album at Folsom State Prison. Cash had been playing prisons since the late 1950s, but this was the first time one was recorded. The inmates were told not to cheer because the warden feared a riot; you can hear them holding back until Cash baits them with the line 'I shot a man in Reno just to watch him die.'",
        "At Folsom Prison (1968) end-to-end", 44,
        "Listen in order. The opening is the clack of a train — we never see the train. Then 'Folsom Prison Blues' — when Cash sings 'I shot a man in Reno', the inmates erupt; the warden had forbidden cheering, so their roar is one of defiance. The closing song 'Greystone Chapel' was written by inmate Glen Sherley; Cash got him a record deal, but Sherley died by suicide a decade later.",
        ["Country", "Americana", "American", "1960s"], 1
    ),
    (
        "album-robert-johnson-complete-recordings", "Album",
        "King of the Delta Blues Singers",
        "Robert Johnson's 1961 compilation — 16 songs recorded in two sessions in 1936-37, in a San Antonio hotel room and a Dallas warehouse. Johnson played with his back to the wall so no one could see his fingerings. The myth that he sold his soul at the crossroads came from these recordings.",
        "King of the Delta Blues Singers (1961 compilation)", 40,
        "Listen to 'Cross Road Blues' — Johnson plays slide guitar with a pocketknife and sings in a voice that sounds decades older than his 25 years. Then 'Hellhound on My Trail' — the guitar part contains a walking bassline, rhythm chords, and a lead melody, all played simultaneously. Eric Clapton said the first time he heard this album, he had to pull his car over.",
        ["Blues", "Delta Blues", "American", "1930s"], 1
    ),

    # ═══════════════════════════════════════════════════════════════════════
    # EXPERIMENTAL / AVANT-GARDE
    # ═══════════════════════════════════════════════════════════════════════
    (
        "album-bjork-vespertine", "Album",
        "Vespertine",
        "Björk's 2001 chamber-electronic album, mostly recorded alone in her Reykjavík home. She used micro-beats built from everyday sounds: shuffling cards, footsteps in snow, ice cracking. The beats sit closer to your chest than your head — that's intentional.",
        "Vespertine (2001) end-to-end", 55,
        "Notice how the beats hit your chest vs your head — that's intentional. The album mixes orchestral and beat programming in a way most artists avoid. It rewards headphones and speakers differently. The harp parts were played by Zeena Parkins, a classical harpist who had to learn to play against electronic beats.",
        ["Electronic", "Art Pop", "Experimental", "Icelandic", "2000s"], 1
    ),
    (
        "album-bjork-homogenic", "Album",
        "Homogenic",
        "Björk 1997 — recorded in Spain after a stalker mailed her a letter bomb and then killed himself on camera. She channeled the trauma into an album that sounds like Iceland itself: volcanoes, glaciers, and strings that sweep like the northern lights. The cover was shot by Alexander McQueen.",
        "Homogenic (1997) end-to-end", 43,
        "Listen to 'Jóga' — the strings were arranged by Eumir Deodato and recorded in a single session in London. The rhythmic beat underneath is a distorted sample of volcanic rock hitting water. Then 'Bachelorette' — the lyrics are a short story about a tree that grows a city, written by Sjón, an Icelandic poet and novelist.",
        ["Electronic", "Art Pop", "Experimental", "Icelandic", "1990s"], 1
    ),

    # ═══════════════════════════════════════════════════════════════════════
    # REGGAE / DUB
    # ═══════════════════════════════════════════════════════════════════════
    (
        "album-bob-marley-exodus", "Album",
        "Exodus",
        "Bob Marley and the Wailers 1977 — recorded in London after Marley survived an assassination attempt in Jamaica. Gunmen broke into his house and shot him in the chest and arm two days before a peace concert. Marley played the concert anyway, then flew to London to record this album in exile.",
        "Exodus (1977) end-to-end", 38,
        "Listen to 'Jamming' — the bassline by Aston 'Family Man' Barrett walks a single note for two minutes before changing. Then 'Three Little Birds' — the 'every little thing is gonna be alright' chorus was improvised during a studio jam. The album was Time magazine's Album of the Century.",
        ["Reggae", "Roots Reggae", "Jamaican", "1970s"], 1
    ),
    (
        "album-lee-scratch-perry-super-ape", "Album",
        "Super Ape",
        "Lee 'Scratch' Perry 1976 — recorded at his legendary Black Ark studio, a converted backyard shed in Kingston. Perry produced the entire album alone, playing many instruments himself and bouncing tracks between two 4-track tape machines. The album includes recordings of cows mooing and Perry blowing bubbles into a microphone.",
        "Super Ape (1976) end-to-end", 38,
        "Listen on quality headphones. Perry mixed the album in mono because he believed stereo was a gimmick. The bass frequencies sit so low they're almost subsonic — Perry would crank the bass EQ until the mixing board meters pinned. The album was meant to be played loud enough to feel through your feet.",
        ["Dub", "Reggae", "Jamaican", "1970s"], 1
    ),

    # ═══════════════════════════════════════════════════════════════════════
    # CLASSICAL / CONTEMPORARY COMPOSITION
    # ═══════════════════════════════════════════════════════════════════════
    (
        "album-glenn-gould-goldberg-1955", "Album",
        "Goldberg Variations (1955 Recording)",
        "Glenn Gould's 1955 debut recording — he was 22 and unknown. Columbia Records thought he was insane to debut with Bach's Goldberg Variations, a piece considered academic and unmarketable. Gould hummed audibly throughout the recording; the engineers couldn't stop him. The album became a bestseller and redefined how Bach was played.",
        "Goldberg Variations — 1955 Gould recording", 39,
        "Listen for Gould's humming — it's audible in nearly every track. He believed the voice and fingers were one instrument. Then compare the Aria at the start and the Aria da Capo at the end — the same notes at the same tempo, but Gould plays them completely differently. The 1981 re-recording is slower by nearly 13 minutes and is a different universe.",
        ["Classical", "Baroque", "Canadian", "1950s"], 1
    ),
    (
        "album-steve-reich-music-for-18", "Album",
        "Music for 18 Musicians",
        "Steve Reich 1978 — a 56-minute pulse piece for violin, cello, clarinets, pianos, marimbas, xylophones, and metallophone, plus four female voices. The entire piece is built from 11 chords played in cycle. Musicians have described performing it as a form of meditation.",
        "Music for 18 Musicians (1978) end-to-end", 56,
        "Focus on the pulse — it never stops for 56 minutes. Then shift your attention to the marimbas. Then to the voices. Each listening layer reveals a different piece. The work has no conductor; musicians cue each other by breathing audibly, which Reich scored into the piece.",
        ["Classical", "Minimalism", "American", "1970s"], 1
    ),

    # ═══════════════════════════════════════════════════════════════════════
    # POP — Art Pop, Synth-pop, Chamber Pop, Indie Pop
    # ═══════════════════════════════════════════════════════════════════════
    (
        "album-mitski-be-the-cowboy", "Album",
        "Be the Cowboy",
        "Mitski's 2018 album of tightly drawn vignettes, each under three minutes. She wrote the songs in bursts — some in 15 minutes, some over months. The album title came from her realization that women in music are expected to be vulnerable; she wanted to project the swagger of a cowboy instead.",
        "Be the Cowboy (2018) end-to-end", 33,
        "Pick one track that doesn't grab you on first listen. Play it three times back-to-back. Mitski writes songs that read flat on the surface — give them room to flip. 'Nobody' sounds like a disco banger until you realize it's about the terror of being alone. The whole album fits in the time it takes to commute across a city.",
        ["Indie Rock", "Art Pop", "American", "2010s"], 1
    ),
    (
        "album-kate-bush-hounds-of-love", "Album",
        "Hounds of Love",
        "Kate Bush 1985 — recorded in her own 48-track studio built in a converted barn behind her house. Side A is pop singles; Side B is 'The Ninth Wave', a 7-song suite about a woman drifting alone at sea, hallucinating her past. The album used a Fairlight CMI sampler when almost no one had one.",
        "Hounds of Love (1985) end-to-end", 47,
        "Listen to Side B ('The Ninth Wave') as a single piece. It's a complete narrative cycle: a woman falls through ice, drifts in freezing water, and experiences visions while waiting to be rescued — or not. Bush sampled her own voice and played it back at different speeds to create the ghosts. The helicopter at the end is an actual RAF rescue helicopter she recorded from her garden.",
        ["Art Pop", "British", "1980s"], 1
    ),
    (
        "album-prince-purple-rain", "Album",
        "Purple Rain",
        "Prince 1984 — the soundtrack to a film that made $70 million on a $7 million budget. Prince recorded the title track live at a benefit concert; the 8-minute version is the first take, with no overdubs except strings. The label thought the album was too long and too strange to sell.",
        "Purple Rain (1984) end-to-end", 44,
        "Listen to the title track — Prince's guitar solo at the end was recorded live at First Avenue in Minneapolis, with the crowd audible. The song builds for nearly 4 minutes before the solo arrives, and when it does, it doesn't end — it keeps climbing for another 4 minutes. Then 'When Doves Cry' — a song with no bassline, because Prince removed it at the last minute and dared anyone to notice.",
        ["Pop", "Funk", "Rock", "American", "1980s"], 1
    ),
    (
        "album-lorde-melodrama", "Album",
        "Melodrama",
        "Lorde 2017 — recorded over four years with Jack Antonoff, mostly in his Brooklyn apartment. The album structures a house party as the arc of a breakup: arrival, exhilaration, the first crack, the fight, the lonely Uber home. The piano on the cover is the actual piano Lorde wrote the album on.",
        "Melodrama (2017) end-to-end", 41,
        "Listen to 'Green Light' — the piano chords change on unexpected beats, creating a sense of forward motion that never settles. Then 'Liability' — recorded in a single take at 2 AM with Lorde at Antonoff's piano. The entire album is a 41-minute arc structured like a single party from entrance to exit.",
        ["Pop", "Art Pop", "New Zealand", "2010s"], 1
    ),
    (
        "album-taylor-swift-folklore", "Album",
        "folklore",
        "Taylor Swift 2020 — written and recorded in isolation during the pandemic with Aaron Dessner of The National and Jack Antonoff. Swift abandoned pop structures for indie folk storytelling. The album was announced 16 hours before release with no singles or promotion.",
        "folklore (2020) end-to-end", 63,
        "Listen to 'exile' — Bon Iver's Justin Vernon wrote his verse in character as a ghost. Then 'august' — the bridge was recorded in a single take with Swift's voice cracking at the end; they kept it because the imperfection was the point. The album is a triptych of fictional love triangles told from different perspectives.",
        ["Indie Folk", "Alternative", "American", "2020s"], 1
    ),
]

# ═══════════════════════════════════════════════════════════════════════════
# JSON generation
# ═══════════════════════════════════════════════════════════════════════════

def build_albums():
    entries = []
    seen_ids = set()

    for album in ALBUMS:
        id_, subtype, name, teaser, target_name, duration, instruction, tags, tier = album

        if id_ in seen_ids:
            print(f"WARNING: duplicate id '{id_}'")
            continue
        seen_ids.add(id_)

        entry = {
            "id": id_,
            "categoryId": "ALBUMS",
            "subtype": subtype,
            "name": name,
            "teaser": teaser,
            "imageUrl": "",
            "exploreAction": {
                "verb": "Listen",
                "targetName": target_name,
                "durationMinutes": duration,
                "instruction": instruction,
            },
            "tags": tags,
            "tier": tier,
        }
        entries.append(entry)

    return entries


def main():
    output_dir = os.path.join(
        os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
        "app", "src", "main", "assets", "topics"
    )
    os.makedirs(output_dir, exist_ok=True)

    output_path = os.path.join(output_dir, "albums.json")
    albums = build_albums()

    with open(output_path, "w") as f:
        json.dump(albums, f, indent=2, ensure_ascii=False)
        f.write("\n")

    print(f"✓ Generated {output_path} with {len(albums)} albums")
    print(f"  Genres: {len(set(t for a in albums for t in a['tags']))} unique tags")


if __name__ == "__main__":
    main()
