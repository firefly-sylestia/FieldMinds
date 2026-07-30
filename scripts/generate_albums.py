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
    # ROCK BATCH 2 — Classic Rock, Punk, Post-Punk, Shoegaze, Britpop, Emo, Stoner
    # ═══════════════════════════════════════════════════════════════════════
    (
        "album-rolling-stones-exile", "Album",
        "Exile on Main St.",
        "The Rolling Stones 1972 — recorded in the basement of Keith Richards' rented villa in the South of France while the band was fleeing UK tax authorities. The sessions were chaos: musicians came and went at all hours, drug dealers doubled as roadies, and the electricity kept cutting out.",
        "Exile on Main St. (1972) end-to-end", 67,
        "Listen to 'Tumbling Dice' — the backing vocals by Merry Clayton and Clydie King were recorded at 3 AM after they'd been woken up to sing. Then 'Shine a Light' — Mick Jagger's vocal was recorded in a tiny bathroom for natural reverb. The album sounds like a radio station broadcasting from a house party you weren't invited to.",
        ["Rock", "Blues Rock", "British", "1970s"], 1
    ),
    (
        "album-rolling-stones-sticky-fingers", "Album",
        "Sticky Fingers",
        "The Rolling Stones 1971 — the first album on their own label, with the famous Andy Warhol zipper cover that damaged adjacent records in shipping. Recorded partly at Muscle Shoals in Alabama, partly at Mick Jagger's country house. The opening riff of 'Brown Sugar' was written by Jagger in 45 minutes during a film shoot.",
        "Sticky Fingers (1971) end-to-end", 46,
        "Listen to 'Wild Horses' — Keith Richards wrote the chorus while sitting in a bathroom, feeling guilty about leaving his newborn son to go on tour. Then 'Can't You Hear Me Knocking' — the jam at the end was an accident; the band thought the song was over and kept playing. The tape was still rolling.",
        ["Rock", "Blues Rock", "British", "1970s"], 1
    ),
    (
        "album-who-whos-next", "Album",
        "Who's Next",
        "The Who 1971 — salvaged from Pete Townshend's abandoned 'Lifehouse' project, a sci-fi rock opera he never finished. The songs were rescued and re-recorded with producer Glyn Johns. The famous synth loop on 'Baba O'Riley' was Townshend's Lowrey organ fed through a VCS3 synthesizer, programmed to cycle forever.",
        "Who's Next (1971) end-to-end", 43,
        "Listen to 'Baba O'Riley' — the violin solo at the end is Dave Arbus of East of Eden, recorded in one take. Then 'Behind Blue Eyes' — Townshend wrote it after a show when he felt like the audience wanted the rock star, not the person. The song shifts from ballad to full-band assault in the bridge.",
        ["Rock", "Hard Rock", "British", "1970s"], 1
    ),
    (
        "album-hendrix-are-you-experienced", "Album",
        "Are You Experienced",
        "Jimi Hendrix 1967 — the debut that rewired what electric guitar could sound like. Hendrix was 24, had been in the US Army, and had backed Little Richard and the Isley Brothers. He recorded the album in London with a mostly British rhythm section who couldn't believe what they were hearing.",
        "Are You Experienced (1967) end-to-end", 40,
        "Listen to 'Purple Haze' — the famous opening riff came to Hendrix in a dream. The solo was recorded in two passes, panned hard left and right. Then 'Third Stone from the Sun' — Hendrix slows down his guitar to sound like a cello, then a spaceship. He told the engineer to leave the mistakes in because 'mistakes are just notes that didn't know where they were going.'",
        ["Psychedelic Rock", "Blues Rock", "American", "1960s"], 1
    ),
    (
        "album-doors-debut", "Album",
        "The Doors",
        "The Doors 1967 — recorded in 6 days at Sunset Sound in Hollywood for $10,000. Jim Morrison was so nervous about his voice that he recorded many of the vocals in the control room with the lights off. The album ends with 'The End', an 11-minute Oedipal nightmare they'd been kicked off stage for playing live.",
        "The Doors (1967) end-to-end", 44,
        "Listen to 'Light My Fire' — the extended organ and guitar solos were edited down from a 7-minute jam. Robby Krieger wrote the melody; it was his first song. Then 'The End' — Morrison's vocal was recorded in one continuous take, and you can hear a door creak open at 4:22 that they decided to leave in.",
        ["Psychedelic Rock", "American", "1960s"], 1
    ),
    (
        "album-cream-disraeli-gears", "Album",
        "Disraeli Gears",
        "Cream 1967 — the first supergroup (Clapton, Bruce, Baker) recorded their second album in 3 days at Atlantic Studios in New York, fueled by psychedelics and sleeplessness. The album cover was designed by Australian artist Martin Sharp, who lived with Clapton in a Chelsea flat that became a psychedelic crash pad.",
        "Disraeli Gears (1967) end-to-end", 33,
        "Listen to 'Sunshine of Your Love' — the riff was inspired by a Jimi Hendrix show. Bruce adapted the lyric from a poem by friend Pete Brown. Then 'Tales of Brave Ulysses' — Clapton used a wah-wah pedal for the first time, having borrowed it from Hendrix. The song's lyric was written on a napkin at a Greek restaurant.",
        ["Blues Rock", "Psychedelic Rock", "British", "1960s"], 1
    ),
    (
        "album-neil-young-harvest", "Album",
        "Harvest",
        "Neil Young 1972 — recorded mostly in a barn in rural California with an ad-hoc band called the Stray Gators. The London Symphony Orchestra appeared on two tracks. 'Heart of Gold' became his only #1 single. Young later said the album 'put me in the middle of the road' and deliberately made darker music to escape it.",
        "Harvest (1972) end-to-end", 37,
        "Listen to 'Heart of Gold' — James Taylor and Linda Ronstadt sing backing vocals. The harmonica parts were recorded in a separate session in Nashville. Then 'The Needle and the Damage Done' — recorded live at UCLA, a stark solo acoustic performance about friends lost to heroin. The audience doesn't clap at the end.",
        ["Folk Rock", "Singer-Songwriter", "Canadian", "1970s"], 1
    ),
    (
        "album-neil-young-tonight-the-night", "Album",
        "Tonight's the Night",
        "Neil Young 1975 — recorded as a raw wake for roadie Bruce Berry and Crazy Horse guitarist Danny Whitten, both dead from heroin overdoses. Young played the album for his label's executives; they asked where the 'real' album was. He shelved it for two years before releasing it unchanged.",
        "Tonight's the Night (1975) end-to-end", 45,
        "Listen to the title track — Young's voice cracks on the first word and never recovers. The whole album was recorded in a single studio with the lights dimmed and the air thick with tequila and grief. Nils Lofgren plays piano on several tracks while crying. No album sounds more like a wake.",
        ["Rock", "Singer-Songwriter", "Canadian", "1970s"], 1
    ),
    (
        "album-patti-smith-horses", "Album",
        "Horses",
        "Patti Smith 1975 — recorded at Electric Lady Studios, the studio built for Jimi Hendrix who died before it was finished. Smith opened the album with 'Jesus died for somebody's sins but not mine', a line that made John Cale (her producer, from the Velvet Underground) know he was working with something dangerous.",
        "Horses (1975) end-to-end", 43,
        "Listen to 'Gloria' — Smith transforms Van Morrison's garage-rock classic into a feminist manifesto in the opening monologue. Then 'Birdland' — inspired by a Peter Reich memoir about his father Wilhelm Reich, the controversial psychoanalyst. Smith recorded the vocal reading from the book.",
        ["Punk Rock", "Art Rock", "American", "1970s"], 1
    ),
    (
        "album-ramones-debut", "Album",
        "Ramones",
        "Ramones 1976 — 14 songs in 29 minutes, recorded in 7 days for $6,400. The fastest, simplest rock album ever made. Johnny Ramone played every downstroke as hard as he could; Tommy played the drums standing up because his stool broke. The album invented punk rock while selling only 6,000 copies in its first year.",
        "Ramones (1976) end-to-end", 29,
        "Listen to 'Blitzkrieg Bop' — the 'Hey ho, let's go' chant was Tommy's idea, borrowed from the Bay City Rollers. Every song on the album has the same tempo: fast. Then 'I Wanna Be Your Boyfriend' — the slowest track at a relatively leisurely 3 minutes. The Ramones claimed they were just a bubblegum pop band playing too fast.",
        ["Punk Rock", "American", "1970s"], 1
    ),
    (
        "album-sex-pistols-never-mind-the-bollocks", "Album",
        "Never Mind the Bollocks, Here's the Sex Pistols",
        "Sex Pistols 1977 — the only studio album from the band that terrified the British establishment. The title got them taken to court for obscenity (they won). Recorded over several chaotic sessions with producers being fired mid-album. The band couldn't really play their instruments, which was the point.",
        "Never Mind the Bollocks (1977) end-to-end", 39,
        "Listen to 'God Save the Queen' — released during the Queen's Silver Jubilee, the BBC banned it. It still reached #2 on the charts (many believe it was #1 but kept off for political reasons). Then 'Anarchy in the UK' — Johnny Rotten's sneer was real; he'd been rejected by every school and employer in London.",
        ["Punk Rock", "British", "1970s"], 1
    ),
    (
        "album-buzzcocks-singles-going-steady", "Album",
        "Singles Going Steady",
        "Buzzcocks 1979 — a compilation of 8 UK singles that became the template for pop-punk. Pete Shelley wrote songs about unrequited love, sexual confusion, and boredom over three-chord guitar rushes that rarely passed the 2:30 mark. The album is a singles band proving the singles format is the point, not the limitation.",
        "Singles Going Steady (1979) end-to-end", 35,
        "Listen to 'Ever Fallen in Love (With Someone You Shouldn't've)' — Shelley wrote it about a man he was in love with while still publicly closeted. Then 'What Do I Get?' — the question is genuinely asked, not rhetorical. The Buzzcocks made punk that admitted vulnerability instead of hiding behind aggression.",
        ["Punk Rock", "Pop Punk", "British", "1970s"], 1
    ),
    (
        "album-gang-of-four-entertainment", "Album",
        "Entertainment!",
        "Gang of Four 1979 — Marxist theory set to funk-punk rhythms. Andy Gill's guitar playing was described by one critic as sounding like 'a dentist's drill attacking sheet metal'. The band was named after the Chinese Communist Party leaders arrested during the Cultural Revolution.",
        "Entertainment! (1979) end-to-end", 43,
        "Listen to 'Damaged Goods' — the bassline by Dave Allen is a funk groove played by someone who learned bass last week. Then 'Anthrax' — two vocalists sing completely different lyrics simultaneously: one a love song, the other an essay about why love songs are ideologically suspect. The album influenced R.E.M., Red Hot Chili Peppers, and Franz Ferdinand equally.",
        ["Post-Punk", "British", "1970s"], 1
    ),
    (
        "album-wire-pink-flag", "Album",
        "Pink Flag",
        "Wire 1977 — 21 songs in 35 minutes, none longer than 4 minutes. The album reduces punk to its atomic components: a riff, a verse, a chorus, stop. Producer Mike Thorne insisted on clean recording quality when every other punk band wanted distortion, making the album sound even more alien and precise.",
        "Pink Flag (1977) end-to-end", 35,
        "Listen to 'Three Girl Rhumba' — the entire song is one riff played 66 times with five words of lyric. Elastica were later sued for basing 'Connection' on it. Then 'Reuters' — the opening track that sounds nothing like the rest of the album: a slow-building drone while a BBC news broadcast plays in the background.",
        ["Post-Punk", "British", "1970s"], 1
    ),
    (
        "album-cure-disintegration", "Album",
        "Disintegration",
        "The Cure 1989 — Robert Smith was turning 30 and terrified of becoming irrelevant. He locked himself in his room and wrote the darkest album of his career. The band nearly broke up during recording; Lol Tolhurst was fired for alcoholism mid-session. Smith later said the album saved his life.",
        "Disintegration (1989) end-to-end", 72,
        "Listen to 'Pictures of You' — the opening guitar line was inspired by a fire that destroyed Smith's childhood home; photographs were among the only things he saved. Then 'Lullaby' — the spider-creature is a metaphor for Smith's depression. The album was mixed on headphones, making it sound enormous and claustrophobic simultaneously.",
        ["Post-Punk", "Gothic Rock", "British", "1980s"], 1
    ),
    (
        "album-siouxsie-and-the-banshees-juju", "Album",
        "Juju",
        "Siouxsie and the Banshees 1981 — the album that perfected gothic post-punk. John McGeoch's guitar work is now taught in music schools; he used a flanger pedal so creatively that Johnny Marr called him the most inventive guitarist of his generation. Siouxsie Sioux wrote the lyrics in a single feverish week.",
        "Juju (1981) end-to-end", 41,
        "Listen to 'Spellbound' — the drum pattern is a 12/8 gallop that never stops. McGeoch's guitar harmonics at 2:15 sound like glass breaking in slow motion. Then 'Arabian Knights' — inspired by the Middle Eastern political turmoil of 1981, the song's tension never releases. McGeoch had a nervous breakdown shortly after the album.",
        ["Post-Punk", "Gothic Rock", "British", "1980s"], 1
    ),
    (
        "album-depeche-mode-violator", "Album",
        "Violator",
        "Depeche Mode 1990 — the synth-pop band's pivot to stadium-sized dark electronic rock. Recorded across 8 studios in 4 countries. The album sold 7.5 million copies. The band's near-constant infighting during recording was captured on tape and later sampled into the album.",
        "Violator (1990) end-to-end", 47,
        "Listen to 'Enjoy the Silence' — Martin Gore wrote it as a ballad; Alan Wilder turned it into the disco-goth anthem by adding the beat without telling anyone. Then 'Personal Jesus' — the famous guitar riff was played by Gore on a Gretsch through a vintage amp, processed to sound like a synthesizer. The album made darkness danceable.",
        ["Synth-Pop", "Alternative Rock", "British", "1990s"], 1
    ),
    (
        "album-new-order-power-corruption-lies", "Album",
        "Power, Corruption & Lies",
        "New Order 1983 — the band's second album after Ian Curtis's suicide and their transition from Joy Division. Peter Saville designed the cover using a 19th-century French painting; the color-code wheel on the back became the band's visual signature. The album reconciled electronic music with post-punk.",
        "Power, Corruption & Lies (1983) end-to-end", 43,
        "Listen to 'Age of Consent' — the opening bassline is Peter Hook playing the melody high up on the neck, his signature technique. Then 'Blue Monday' — released as a standalone 12-inch single and not on the original UK album, it became the best-selling 12-inch single of all time and bankrupted Factory Records because the die-cut sleeve cost more to produce than the record sold for.",
        ["New Wave", "Post-Punk", "British", "1980s"], 1
    ),
    (
        "album-echo-and-the-bunnymen-ocean-rain", "Album",
        "Ocean Rain",
        "Echo and the Bunnymen 1984 — the album they declared would be 'the greatest album ever made' before a note was written. They recorded the strings with a 35-piece orchestra in a Paris studio built by Napoleon III. The confidence paid off: it's now considered their masterpiece.",
        "Ocean Rain (1984) end-to-end", 37,
        "Listen to 'The Killing Moon' — Ian McCulloch wrote the lyric in a dream and woke up to scribble it down. The mandolin was played by a session musician who'd never heard the band before. Then 'Ocean Rain' — the strings were recorded in a single take because the orchestra had to catch a train. The album cover was shot in Carnglaze Caverns, an abandoned slate mine in Cornwall.",
        ["Post-Punk", "Alternative Rock", "British", "1980s"], 1
    ),
    (
        "album-television-marquee-moon", "Album",
        "Marquee Moon",
        "Television 1977 — the CBGB scene's most musically ambitious band. Tom Verlaine and Richard Lloyd traded guitar solos like jazz musicians trading fours. The title track is 10 minutes of interlocking guitar lines that never repeat. The album was rejected by every major label before Elektra took a chance.",
        "Marquee Moon (1977) end-to-end", 46,
        "Listen to the title track — Verlaine and Lloyd recorded their guitar solos facing each other in the studio, each one responding to the other's phrases in real time. Then 'Venus' — the riff sounds like it's walking upstairs, repeatedly trying and failing to resolve. Television made punk smart without making it less urgent.",
        ["Art Punk", "Post-Punk", "American", "1970s"], 1
    ),
    (
        "album-iggy-pop-lust-for-life", "Album",
        "Lust for Life",
        "Iggy Pop 1977 — recorded in Berlin with David Bowie producing. Iggy had just gotten clean from heroin; Bowie was about to record 'Heroes' in the same studio. The album was written and recorded in 8 days. The title track's famous drum beat was borrowed from the Motown standard 'You Can't Hurry Love' and later became the opening theme for Trainspotting.",
        "Lust for Life (1977) end-to-end", 41,
        "Listen to 'The Passenger' — Iggy wrote it riding the Berlin S-Bahn. The guitar riff was Bowie's idea, based on a Ricky Nelson song. Then 'Tonight' — Bowie later recorded his own version with Tina Turner, but Iggy's original, sung in a cracked baritone while barely holding a note, is the definitive take.",
        ["Punk Rock", "American", "1970s"], 1
    ),
    (
        "album-sonic-youth-daydream-nation", "Album",
        "Daydream Nation",
        "Sonic Youth 1988 — a double album that made noise beautiful. The band used screwdrivers, drumsticks, and various objects wedged between guitar strings to create sounds no one had heard before. The album was picked as one of the first 50 recordings preserved in the Library of Congress's National Recording Registry.",
        "Daydream Nation (1988) end-to-end", 71,
        "Listen to 'Teen Age Riot' — the opening track was originally named after Dinosaur Jr.'s J Mascis, a fantasy about an alternative rock revolution. Then 'The Sprawl' — inspired by William Gibson's cyberpunk novels. Thurston Moore and Lee Ranaldo's guitars are tuned to at least four different tunings across the album.",
        ["Alternative Rock", "Noise Rock", "American", "1980s"], 1
    ),
    (
        "album-smashing-pumpkins-mellon-collie", "Album",
        "Mellon Collie and the Infinite Sadness",
        "Smashing Pumpkins 1995 — a 28-song double album spanning 121 minutes, recorded over a year in Chicago. Billy Corgan wrote nearly 50 songs for the project. The album debuted at #1 and sold 10 million copies. The recording process was so intense that drummer Jimmy Chamberlin entered rehab shortly after completion.",
        "Mellon Collie (1995) — disc 1", 60,
        "Listen to '1979' — the drum machine loop was a mistake; Corgan programmed it wrong and liked the off-kilter result. Then 'Tonight, Tonight' — the orchestra was the Chicago Symphony, recorded in an old church. Corgan demanded 35 string players; the producer thought he was insane until he heard the playback.",
        ["Alternative Rock", "American", "1990s"], 1
    ),
    (
        "album-pearl-jam-ten", "Album",
        "Ten",
        "Pearl Jam 1991 — recorded in 3 weeks at London Bridge Studio in Seattle for $25,000 while the band was still called Mookie Blaylock (a basketball player who later sued). The album went 13x platinum but the band famously refused to make music videos for it, pulling back from the spotlight even as they were propelled into it.",
        "Ten (1991) end-to-end", 53,
        "Listen to 'Alive' — Eddie Vedder wrote the lyrics imagining a son discovering his father is actually his stepfather. The guitar solo by Mike McCready was played in one take; he thought it was just a warm-up. Then 'Jeremy' — based on a real suicide by a 15-year-old in front of his classmates in Richardson, Texas.",
        ["Grunge", "Alternative Rock", "American", "1990s"], 1
    ),
    (
        "album-soundgarden-superunknown", "Album",
        "Superunknown",
        "Soundgarden 1994 — the grunge band that could actually play anything. Chris Cornell's four-octave range and Kim Thayil's odd-time-signature riffs met on songs in 5/4, 7/4, and 9/8 without sounding prog. The album's cover art is a distorted photo of the band in a field of fire, representing their emotional state at the time.",
        "Superunknown (1994) end-to-end", 70,
        "Listen to 'Black Hole Sun' — Cornell wrote it in 15 minutes, thinking it was too poppy for the band. The surreal, melting-face music video defined 90s MTV. Then 'Spoonman' — the title refers to a real Seattle street performer, Artis the Spoonman, who plays spoons on the track. The song is in 7/4.",
        ["Grunge", "Alternative Rock", "American", "1990s"], 1
    ),
    (
        "album-alice-in-chains-dirt", "Album",
        "Dirt",
        "Alice in Chains 1992 — the darkest grunge album, a raw chronicle of Layne Staley's heroin addiction. The band was using heavily during recording. Staley's vocal harmonies with Jerry Cantrell created a sound that was simultaneously beautiful and terrifying, like a choir singing from the bottom of a well.",
        "Dirt (1992) end-to-end", 57,
        "Listen to 'Down in a Hole' — Cantrell wrote the lyrics about his own failing relationship, but Staley sang them as if about his addiction. Then 'Would?' — the bassline by Mike Starr is one note for nearly the entire song, creating a trance-like foundation. The track was a tribute to Andrew Wood of Mother Love Bone, who died of a heroin overdose.",
        ["Grunge", "Alternative Metal", "American", "1990s"], 1
    ),
    (
        "album-stone-temple-pilots-purple", "Album",
        "Purple",
        "Stone Temple Pilots 1994 — the album that proved they weren't grunge imitators. Recorded in a mansion in Atlanta, the band fought constantly. Scott Weiland wrote the lyrics while struggling with heroin. Despite the chaos, the album debuted at #1 and produced three top-10 singles.",
        "Purple (1994) end-to-end", 47,
        "Listen to 'Interstate Love Song' — the opening riff came to Dean DeLeo in a dream during a thunderstorm. Then 'Big Empty' — written in a single afternoon when the band needed one more track. Weiland's vocal floats over a sparse arrangement that builds into a wall of guitars, perfectly capturing the push-pull of addiction.",
        ["Alternative Rock", "Grunge", "American", "1990s"], 1
    ),
    (
        "album-my-bloody-valentine-loveless", "Album",
        "Loveless",
        "My Bloody Valentine 1991 — recorded over 2 years across 19 studios, nearly bankrupting Creation Records. Kevin Shields used the tremolo arm of his guitar on every chord, creating a sound that literally bends pitch. The album cost £250,000 and the label's founder said it 'nearly destroyed us', but it became the definitive shoegaze record.",
        "Loveless (1991) end-to-end", 48,
        "Listen to 'Only Shallow' — the opening guitar sounds like a jet engine taking off. Then 'When You Sleep' — Shields recorded the guitars at a low volume so the microphones would pick up room sound, creating the sensation of distance. The vocals were deliberately buried in the mix so you'd strain to hear them, like listening through a wall.",
        ["Shoegaze", "Alternative Rock", "Irish-British", "1990s"], 1
    ),
    (
        "album-slowdive-souvlaki", "Album",
        "Souvlaki",
        "Slowdive 1993 — recorded during a period when the British music press had turned viciously against shoegaze. Rachel Goswell and Neil Halstead had just broken up emotionally while still being bandmates. Brian Eno contributed keyboards to two tracks after randomly meeting them in the studio hallway.",
        "Souvlaki (1993) end-to-end", 41,
        "Listen to 'Alison' — Halstead wrote it about a friend who'd died, but the lyric is ambiguous enough to be a love song. Then 'When the Sun Hits' — the song is structured like a wave: quiet verse, massive wall of guitar for the chorus, retreat, repeat. Eno's contribution on 'Sing' is a single sustained note held for three minutes.",
        ["Shoegaze", "Dream Pop", "British", "1990s"], 1
    ),
    (
        "album-ride-nowhere", "Album",
        "Nowhere",
        "Ride 1990 — the album that defined the heavier side of shoegaze before the term existed. Produced by Marc Waterman, who insisted on recording the drums in a stairwell to get the right amount of natural reverb. The guitar interplay between Andy Bell and Mark Gardener became the blueprint for what a two-guitar band could do without a lead guitarist.",
        "Nowhere (1990) end-to-end", 48,
        "Listen to 'Vapour Trail' — the string arrangement at the end is not strings at all but layered and pitch-shifted guitar feedback. Then 'Dreams Burn Down' — the song builds over four minutes from a whisper to a hurricane, with the drums seemingly trying to escape the song. The album's closing 8-minute title track is a single chord cycle that never resolves.",
        ["Shoegaze", "Alternative Rock", "British", "1990s"], 1
    ),
    (
        "album-cocteau-twins-heaven-or-las-vegas", "Album",
        "Heaven or Las Vegas",
        "Cocteau Twins 1990 — Elizabeth Fraser's voice as a musical instrument, singing in a self-invented language of sounds rather than words. The album was recorded during a period of severe drug addiction and the birth of Fraser's first child. The title came from a neon sign Fraser saw while driving through Nevada.",
        "Heaven or Las Vegas (1990) end-to-end", 38,
        "Listen to 'Cherry-coloured Funk' — try to make out the lyrics. You can't, and that's the point; Fraser's voice was an additional instrument, not a vehicle for meaning. Then the title track — the most straightforward song they ever recorded, with Robin Guthrie's shimmering guitar and Simon Raymonde's melodic bass finally letting Fraser's voice float above rather than within.",
        ["Dream Pop", "Shoegaze", "Scottish", "1990s"], 1
    ),
    (
        "album-oasis-definitely-maybe", "Album",
        "Definitely Maybe",
        "Oasis 1994 — the fastest-selling debut in British history at the time. Recorded in 19 days at Monnow Valley in Wales, then completely re-recorded at Sawmills in Cornwall. Noel Gallagher wrote every song; Liam Gallagher sang them with a sneer you could hear through walls. The album captured a specific British moment: late Thatcher hangover, early Cool Britannia optimism.",
        "Definitely Maybe (1994) end-to-end", 52,
        "Listen to 'Live Forever' — Noel wrote it in response to Nirvana's 'I Hate Myself and Want to Die', deliberately creating an anthem of defiance. Then 'Slide Away' — Liam's vocal take was his first and only; he went outside for a cigarette and never came back to re-record it. The album sounds like someone kicking open every door in the house.",
        ["Britpop", "Rock", "British", "1990s"], 1
    ),
    (
        "album-blur-parklife", "Album",
        "Parklife",
        "Blur 1994 — the Britpop album that made Damon Albarn the voice of suburban British youth irony. Recorded in a small studio on Borough High Street in London. Phil Daniels (the actor from Quadrophenia) narrated the title track as a cockney geezer. The album is a love letter to a very specific, very mundane version of England.",
        "Parklife (1994) end-to-end", 53,
        "Listen to 'Girls & Boys' — the disco beat was Graham Coxon's idea, played on a drum machine. The lyrics about package holidays and casual sex were Albarn observing British tourists in Magaluf. Then 'This Is a Low' — the lyrics are weather shipping forecast regions: 'Biscay, Trafalgar, Dogger, Rockall.' Albarn turned the BBC shipping forecast into poetry.",
        ["Britpop", "Indie Rock", "British", "1990s"], 1
    ),
    (
        "album-pulp-different-class", "Album",
        "Different Class",
        "Pulp 1995 — Jarvis Cocker's magnum opus about class, sex, and the absurdity of British life. Cocker was 32 when the album came out, having been in Pulp since he was 15. The album cover was shot at a wedding reception; the band photoshopped themselves into the guests.",
        "Different Class (1995) end-to-end", 53,
        "Listen to 'Common People' — Cocker wrote it based on a real Greek art student who told him she wanted to 'live like common people.' The song builds from a single keyboard note to a full-band crescendo over 6 minutes. Then 'Disco 2000' — based on Cocker's childhood crush Deborah, who he found on Facebook 25 years later.",
        ["Britpop", "Indie Rock", "British", "1990s"], 1
    ),
    (
        "album-suede-debut", "Album",
        "Suede",
        "Suede 1993 — the fastest-selling debut album in British history until Oasis broke the record a year later. Brett Anderson's androgynous vocals and Bernard Butler's fluid guitar lines created a glam-rock revival filtered through British miserabilism. The cover photo of two women kissing was considered shocking for a major-label release in 1993.",
        "Suede (1993) end-to-end", 43,
        "Listen to 'Animal Nitrate' — Butler's guitar solo is a single sustained note fed through a wah-wah pedal. Then 'The Drowners' — the opening riff sounds like it was beamed in from a 1973 David Bowie session. Butler recorded the album's guitar parts in a closet to capture a claustrophobic, radio-unfriendly ambience.",
        ["Britpop", "Alternative Rock", "British", "1990s"], 1
    ),
    (
        "album-manic-street-preachers-holy-bible", "Album",
        "The Holy Bible",
        "Manic Street Preachers 1994 — the most harrowing album of the Britpop era. Richey Edwards wrote lyrics about anorexia, the Holocaust, capital punishment, and self-harm while in the grip of severe depression. Four months after the album's release, Edwards disappeared; his car was found near the Severn Bridge. He has never been found.",
        "The Holy Bible (1994) end-to-end", 57,
        "Listen to '4st 7lb' — the title is the weight at which an anorexic person is at risk of death. Edwards hospitalised himself during the writing. Then 'Faster' — the opening line is 'I am an architect, they call me a butcher.' The album's references range from Primo Levi to Sylvia Plath to the Hungerford massacre.",
        ["Alternative Rock", "Post-Punk", "British", "1990s"], 1
    ),
    (
        "album-verve-urban-hymns", "Album",
        "Urban Hymns",
        "The Verve 1997 — the album that broke up the band. Richard Ashcroft wrote the songs after a three-year heroin detox. The string arrangement on 'Bitter Sweet Symphony' used a sample from an orchestral version of a Rolling Stones song, leading to a lawsuit that gave Mick Jagger and Keith Richards 100% of the royalties. Ashcroft earned nothing from his most famous song for 22 years.",
        "Urban Hymns (1997) end-to-end", 76,
        "Listen to 'Bitter Sweet Symphony' — the strings were recorded at Olympic Studios. Then 'The Drugs Don't Work' — Ashcroft wrote it watching his father die of cancer. The song's devastating chorus was sung in a single take. The album broke up the band literally: they split during the tour, reunited briefly, and split again.",
        ["Britpop", "Alternative Rock", "British", "1990s"], 1
    ),
    (
        "album-modest-mouse-lonesome-crowded-west", "Album",
        "The Lonesome Crowded West",
        "Modest Mouse 1997 — Isaac Brock's blistering indictment of suburban sprawl, strip malls, and spiritual emptiness. Recorded in a makeshift studio in a house in Seattle. Brock's guitar was tuned to an open chord he'd invented so he could play while drinking. The album was recorded for $10,000 and sounds like it cost ten times that.",
        "The Lonesome Crowded West (1997) end-to-end", 74,
        "Listen to 'Trailer Trash' — Brock's vocal cracks are real; he was drinking whiskey during the session. Then 'Cowboy Dan' — the song starts as a whisper, becomes a scream about a man who 'moves to the city' and 'shoots his rifle in the air.' Brock intentionally recorded the vocals too close to the microphone so you can hear the spit.",
        ["Indie Rock", "American", "1990s"], 1
    ),
    (
        "album-pavement-crooked-rain", "Album",
        "Crooked Rain, Crooked Rain",
        "Pavement 1994 — the slacker-rock masterpiece that almost made them famous. Stephen Malkmus wrote the lyrics by cutting up newspapers and rearranging the words. The album was recorded in a studio that was also a working horse barn in rural Virginia. The single 'Cut Your Hair' was their attempt at a hit; it nearly worked.",
        "Crooked Rain, Crooked Rain (1994) end-to-end", 42,
        "Listen to 'Cut Your Hair' — the melody sounds like a nursery rhyme filtered through guitar fuzz. Then 'Range Life' — Malkmus's gentle mockery of the Smashing Pumpkins and Stone Temple Pilots apparently made Billy Corgan refuse to play festivals with them. The album's charm is its refusal to try too hard.",
        ["Indie Rock", "Lo-Fi", "American", "1990s"], 1
    ),
    (
        "album-built-to-spill-keep-it-like-a-secret", "Album",
        "Keep It Like a Secret",
        "Built to Spill 1999 — Doug Martsch's guitar meditation disguised as an indie rock album. Recorded piece by piece over a year, with Martsch playing most of the instruments and layering guitars like Brian Wilson on a budget. The album title describes Martsch's philosophy: hold something back, don't give it all away.",
        "Keep It Like a Secret (1999) end-to-end", 47,
        "Listen to 'Carry the Zero' — the song starts as a simple chord progression and builds into a three-guitar tapestry that peaks at 4:12 but keeps going. Then 'Else' — Martsch's guitar solo is played through a rotating Leslie speaker, making it sound like it's orbiting the rest of the song. The album rewards headphones: every listen reveals a new guitar line.",
        ["Indie Rock", "American", "1990s"], 1
    ),
    (
        "album-interpol-turn-on-the-bright-lights", "Album",
        "Turn On the Bright Lights",
        "Interpol 2002 — the album that made post-punk revival a thing. Recorded in a converted church in Connecticut over a bleak winter. Paul Banks' baritone and Daniel Kessler's angular guitar lines drew constant Joy Division comparisons. The album captured post-9/11 New York anxiety without explicitly mentioning it.",
        "Turn On the Bright Lights (2002) end-to-end", 49,
        "Listen to 'Obstacle 1' — the opening guitar riff was Kessler trying to play a Joy Division song from memory. Then 'NYC' — the chorus 'New York cares' was written before September 11; after, it sounded like an elegy. The album's atmosphere is so consistent that it sounds like it was recorded in a single room at 3 AM.",
        ["Post-Punk Revival", "Indie Rock", "American", "2000s"], 1
    ),
    (
        "album-strokes-is-this-it", "Album",
        "Is This It",
        "The Strokes 2001 — 36 minutes that made rock music feel dangerous again. Recorded in 6 weeks at a studio on Manhattan's Lower East Side. Julian Casablancas recorded his vocals through a small guitar amp to get the right amount of distortion. The album's original cover featured a black-and-white photo of a naked bottom; the US release replaced it with a more abstract image.",
        "Is This It (2001) end-to-end", 36,
        "Listen to 'Last Nite' — the riff was Casablancas trying to write a song in the style of Tom Petty. Then 'Someday' — Albert Hammond Jr.'s guitar solo is two notes repeated for 10 seconds while the rhythm section carries the song. The album's brevity is a statement: leave before they want more.",
        ["Indie Rock", "Garage Rock Revival", "American", "2000s"], 1
    ),
    (
        "album-libertines-up-the-bracket", "Album",
        "Up the Bracket",
        "The Libertines 2002 — recorded in 2 weeks with Mick Jones of The Clash producing. Pete Doherty and Carl Barât wrote the songs in a flat on the Caledonian Road they called 'The Albion Rooms'. Doherty was already using heroin heavily. The album sounds like a party that's about to be broken up by the police — which it was, several times.",
        "Up the Bracket (2002) end-to-end", 39,
        "Listen to the title track — Doherty's vocal is slurred but the melody is perfect. Then 'Time for Heroes' — the lyrics are a snapshot of the 2000 May Day riots in London, which Barât and Doherty attended together. The album captured a specific moment of British youth culture: pre-smartphone, post-Thatcher, fuelled by cheap lager and idealism.",
        ["Indie Rock", "Garage Rock Revival", "British", "2000s"], 1
    ),
    (
        "album-killers-hot-fuss", "Album",
        "Hot Fuss",
        "The Killers 2004 — a Las Vegas band that sounded more British than most British bands. Recorded in Berkeley, California after the band's van broke down there. Brandon Flowers wrote densely narrative lyrics influenced by growing up Mormon in Sin City. The album's synth-heavy sound was a deliberate rejection of the garage-rock revival happening around them.",
        "Hot Fuss (2004) end-to-end", 46,
        "Listen to 'Mr. Brightside' — written in one night after Flowers caught his girlfriend cheating. The guitar riff was played by Dave Keuning on an Ibanez through a delay pedal. Then 'All These Things That I've Done' — the 'I got soul but I'm not a soldier' bridge was improvised in the studio. The gospel choir was a local Berkeley church group.",
        ["Indie Rock", "New Wave Revival", "American", "2000s"], 1
    ),
    (
        "album-franz-ferdinand-debut", "Album",
        "Franz Ferdinand",
        "Franz Ferdinand 2004 — the album that made art-school dance-rock a global phenomenon. Recorded in a former church in Glasgow. Alex Kapranos wrote lyrics about dinner parties, jealousy, and the Scottish art scene. The band's tight, clipped guitar style was inspired by the idea of making music that sounded like it was wearing a slim-fit suit.",
        "Franz Ferdinand (2004) end-to-end", 39,
        "Listen to 'Take Me Out' — the song famously changes tempo and feel at 0:55, a deliberate misdirection. Then 'Michael' — a disco-rock song about a male crush, sung without irony. The band's secret weapon was drummer Paul Thomson, whose hi-hat work on every song turns standard rock rhythms into dance beats.",
        ["Indie Rock", "Dance-Punk", "Scottish", "2000s"], 1
    ),
    (
        "album-white-stripes-elephant", "Album",
        "Elephant",
        "The White Stripes 2003 — recorded in 2 weeks at Toe Rag Studios in London, using only pre-1963 analog equipment. Jack White insisted on no computers, no Pro Tools. The album was recorded on 8-track tape. Meg White played a drum kit with only a kick, snare, and one cymbal. The limitations became the sound.",
        "Elephant (2003) end-to-end", 50,
        "Listen to 'Seven Nation Army' — the riff was played on a guitar tuned down an octave through a DigiTech Whammy pedal, creating a bass-like tone. Then 'Ball and Biscuit' — Jack's 7-minute blues solo was his attempt to play like Blind Willie McTell through three Marshall stacks. The album proved that restrictions breed creativity.",
        ["Garage Rock", "Blues Rock", "American", "2000s"], 1
    ),
    (
        "album-yeah-yeah-yeahs-fever-to-tell", "Album",
        "Fever to Tell",
        "Yeah Yeah Yeahs 2003 — Karen O's debut as a frontwoman possessed. Recorded in a converted church in Brooklyn. Nick Zinner's guitar was run through so many effects pedals that his pedalboard looked like a small city. The album was produced by David Andrew Sitek of TV on the Radio, who layered ambient noise under every track.",
        "Fever to Tell (2003) end-to-end", 39,
        "Listen to 'Maps' — Karen O wrote it about her then-boyfriend Angus Andrew of Liars, who was leaving for tour. She cried during the recording; the 'they don't love you like I love you' is addressed directly to him. Then 'Y Control' — Zinner's guitar sounds like a malfunctioning fire alarm in the best possible way.",
        ["Indie Rock", "Garage Rock Revival", "American", "2000s"], 1
    ),
    (
        "album-lcd-soundsystem-sound-of-silver", "Album",
        "Sound of Silver",
        "LCD Soundsystem 2007 — James Murphy's meditation on getting older, losing friends, and the redemptive power of dance music. Recorded in a converted barn in upstate New York. Murphy was 37, newly married, and questioning whether he should still be making dance-punk.",
        "Sound of Silver (2007) end-to-end", 56,
        "Listen to 'All My Friends' — a 7-minute build around a single piano figure. The lyrics catalog a lifetime of parties, friendships, and regrets. Then 'Someone Great' — written after Murphy's therapist died suddenly. The song uses the structure of dance music — repetition, build, release — to process grief. Murphy later said it was the most personal thing he'd ever written.",
        ["Dance-Punk", "Electronic Rock", "American", "2000s"], 1
    ),
    (
        "album-national-boxer", "Album",
        "Boxer",
        "The National 2007 — the album where they stopped trying to be a rock band and became The National. Recorded in a house in Bridgeport, Connecticut. Matt Berninger's baritone was recorded in a closet filled with blankets for soundproofing. The album's characters are lonely, drunk, anxious professionals navigating dinner parties and office politics.",
        "Boxer (2007) end-to-end", 43,
        "Listen to 'Fake Empire' — the song's odd time signature (4/4 over a waltzing 3/4 piano) creates a constant sense of unease. The horn arrangement was written by Padma Newsome, who also played viola. Then 'Mistaken for Strangers' — Berninger wrote it about running into a friend who didn't recognize him, which became a metaphor for his relationship with himself.",
        ["Indie Rock", "American", "2000s"], 1
    ),
    (
        "album-tame-impala-lonerism", "Album",
        "Lonerism",
        "Kevin Parker 2012 — recorded entirely alone in a rented house in Perth, Australia, on a laptop. Parker played every instrument, sang every vocal, produced every track. The album is about social isolation from someone who turned isolation into a creative method. The drums were recorded first, then everything else was built around them.",
        "Lonerism (2012) end-to-end", 52,
        "Listen to 'Feels Like We Only Go Backwards' — the swirling synth was a Korg MS-20 routed through a phaser pedal. Then 'Apocalypse Dreams' — the song's mid-point key change was Parker accidentally hitting the wrong chord and deciding it sounded better. The album's drums sound like they're recorded in a room the size of a cathedral.",
        ["Psychedelic Rock", "Indie Rock", "Australian", "2010s"], 1
    ),
    (
        "album-foals-total-life-forever", "Album",
        "Total Life Forever",
        "Foals 2010 — the Oxford band's pivot from math-rock to something more expansive and emotional. Recorded in a church in Gothenburg, Sweden during the coldest winter in decades. Yannis Philippakis wrote the lyrics after devouring books on futurism and transhumanism. The album's aquatic themes were inspired by the Swedish archipelago.",
        "Total Life Forever (2010) end-to-end", 50,
        "Listen to 'Spanish Sahara' — the song builds for 6 minutes from a single quiet guitar note to a wall of noise, then retreats. Then 'Blue Blood' — the interlocking guitar lines were recorded separately without either guitarist hearing the other's part. Philippakis said the album was about 'what happens when we upload our consciousness.'",
        ["Indie Rock", "Math Rock", "British", "2010s"], 1
    ),
    (
        "album-queens-of-the-stone-age-songs-for-the-deaf", "Album",
        "Songs for the Deaf",
        "Queens of the Stone Age 2002 — a concept album structured as a drive through the California desert, with fake radio station interludes between tracks. Dave Grohl played drums as a session musician, recording his parts in 3 days. Josh Homme tuned his guitar down to C-standard, creating the low, heavy 'robot rock' sound that defined the band.",
        "Songs for the Deaf (2002) end-to-end", 59,
        "Listen to 'No One Knows' — the riff was Homme trying to write a polka. Grohl's drum fill at 2:14 is one of the most-emulated in rock. Then 'Go with the Flow' — the distorted piano was played by Homme through a guitar amp. The entire album sounds like a car radio at full volume crossing the Mojave at midnight.",
        ["Stoner Rock", "Hard Rock", "American", "2000s"], 1
    ),
    (
        "album-kyuss-welcome-to-sky-valley", "Album",
        "Welcome to Sky Valley",
        "Kyuss 1994 — the album that invented stoner rock. Recorded in a remote studio in the California desert called Rancho De La Luna. The band recorded at night because the daytime heat made the equipment malfunction. Guitarist Josh Homme tuned down to C-standard and played through bass amps to create tones that sound like a truck engine idling.",
        "Welcome to Sky Valley (1994) end-to-end", 52,
        "Listen to 'Gardenia' — the opening riff was played through a bass amp with all the dials turned to 10. Then 'Demon Cleaner' — the slowed-down groove was achieved by recording at a faster tempo and pitching the tape down. The album is three suites of connected tracks, meant to be listened to as movements, not individual songs.",
        ["Stoner Rock", "Desert Rock", "American", "1990s"], 1
    ),
    (
        "album-sleep-dopesmoker", "Album",
        "Dopesmoker",
        "Sleep 2003 — a single 63-minute song about a caravan of weed priests crossing the desert. The band's label refused to release it; they spent years fighting over the master tapes. Originally recorded in 1996, it wasn't officially released until 2003. The album has since become the most legendary stoner metal record ever made.",
        "Dopesmoker (2003) — the full 63-minute version", 63,
        "Listen to the whole thing in one sitting. The riff doesn't change for the first 12 minutes — bassist/vocalist Al Cisneros's mantra-like delivery treats the riff as a meditation object. Then notice how the tempo gradually, almost imperceptibly slows across the full 63 minutes, like a machine running out of fuel. The album was the sole reason Sleep were dropped from London Records.",
        ["Stoner Metal", "Doom Metal", "American", "2000s"], 1
    ),
    (
        "album-sunny-day-real-estate-diary", "Album",
        "Diary",
        "Sunny Day Real Estate 1994 — the album that invented emo as an art form rather than a punchline. Recorded in a basement in Seattle for $5,000. Jeremy Enigk's lyrics were written after he converted to Christianity, though the album is more spiritual questioning than religious. The album's emotional directness influenced an entire genre.",
        "Diary (1994) end-to-end", 53,
        "Listen to 'Seven' — the opening riff is in an unusual time signature that shifts between 4/4 and 6/8. Then 'In Circles' — Enigk's vocal jumps from a whisper to a scream in the space of a single syllable. The band broke up shortly after the album because the emotional intensity of the songs was too much to sustain live night after night.",
        ["Emo", "Post-Hardcore", "American", "1990s"], 1
    ),
    (
        "album-american-football-lp1", "Album",
        "American Football",
        "American Football 1999 — a single album recorded by University of Illinois students in a college dorm, then the band broke up. Over the next 15 years, the album grew from obscurity into the definitive Midwest emo record. The house on the cover became a pilgrimage site for fans; the band eventually bought it.",
        "American Football (1999) end-to-end", 41,
        "Listen to 'Never Meant' — the opening guitar line is in an alternate tuning (FACGCe) that Mike Kinsella invented. Then 'The Summer Ends' — the trumpet solo at the end was played by a friend who happened to be visiting the dorm during recording. The album's math-rock time signatures make the emotional content feel even more exposed.",
        ["Emo", "Math Rock", "American", "1990s"], 1
    ),
    (
        "album-deftones-white-pony", "Album",
        "White Pony",
        "Deftones 2000 — the album that proved a nu-metal band could make art. Recorded in Sausalito at the Record Plant, where Fleetwood Mac made Rumours. Chino Moreno brought in influences from The Cure, Depeche Mode, and My Bloody Valentine. The album's atmosphere is equal parts violence and sensuality.",
        "White Pony (2000) end-to-end", 48,
        "Listen to 'Change (In the House of Flies)' — the whispered verses give way to a chorus that feels like drowning. Then 'Passenger' — Maynard James Keenan of Tool trades vocals with Moreno, each trying to out-menace the other. The album's secret is DJ Frank Delgado's turntable work: subtle scratches and samples add texture without ever sounding like a gimmick.",
        ["Alternative Metal", "Post-Hardcore", "American", "2000s"], 1
    ),
    (
        "album-tool-aenima", "Album",
        "Ænima",
        "Tool 1996 — 77 minutes of mathematically precise progressive metal. The album opens with a Bill Hicks sample about fear-based media. The songs are built from Fibonacci sequences, polyrhythms, and shifting time signatures that music students still diagram. Despite the complexity, the album went triple platinum.",
        "Ænima (1996) end-to-end", 77,
        "Listen to 'Forty Six & 2' — the song is about Jungian shadow integration. The bassline by Justin Chancellor is in 7/8 while the drums are in 4/4, creating a relentless forward momentum. Then 'Eulogy' — the song's structure mirrors the five stages of grief. Tool wrote the album as a single continuous arc, not a collection of songs.",
        ["Progressive Metal", "Alternative Metal", "American", "1990s"], 1
    ),
    (
        "album-rage-against-the-machine-debut", "Album",
        "Rage Against the Machine",
        "Rage Against the Machine 1992 — recorded in 3 months at a warehouse in North Hollywood. Tom Morello played guitar through a Whammy pedal, a DigiTech delay, and a wah-wah in ways no manufacturer intended, creating sounds that people initially assumed were synths. Zack de la Rocha's rap-delivery made radical leftist politics radio-friendly.",
        "Rage Against the Machine (1992) end-to-end", 53,
        "Listen to 'Killing in the Name' — the famous 'Fuck you, I won't do what you tell me' closing chant was improvised. Then 'Know Your Enemy' — Morello's 'guitar solo' at 2:30 is him unplugging and re-plugging his guitar cable at specific intervals to create a rhythmic pulse. The album was produced by Garth Richardson, who had to learn Morello's self-invented techniques on the fly.",
        ["Rap Metal", "Alternative Metal", "American", "1990s"], 1
    ),
    (
        "album-system-of-a-down-toxicity", "Album",
        "Toxicity",
        "System of a Down 2001 — recorded in Hollywood just before 9/11. The album was accidentally released a week early; tower records in New York sold copies before street date. The band's Armenian heritage infused the music with Middle Eastern scales and rhythms no metal band had used before. Serj Tankian's operatic delivery made political songs about prison reform and the Armenian genocide feel like anthems.",
        "Toxicity (2001) end-to-end", 44,
        "Listen to 'Chop Suey!' — the song's title came from the way a dead body looks after autopsy. The 'father, father' bridge was Tankian quoting a Bible verse. Then 'Aerials' — the time signature shifts from 4/4 to 3/4 to 6/8 without the listener noticing. Daron Malakian wrote most of the music on an acoustic guitar in his bedroom.",
        ["Alternative Metal", "Nu Metal", "American", "2000s"], 1
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
    # HIP-HOP BATCH 3 — West Coast, Southern, Trap, Conscious, UK, Deep Cuts
    # ═══════════════════════════════════════════════════════════════════════
    (
        "album-dr-dre-the-chronic", "Album",
        "The Chronic",
        "Dr. Dre 1992 — the album that invented G-funk and made the West Coast the center of hip-hop. Recorded at Death Row Studios with a young Snoop Doggy Dogg writing most of the lyrics. Dre used a live band to replay Parliament-Funkadelic samples, creating a sun-drenched, low-rider sound that dominated the 90s.",
        "The Chronic (1992) end-to-end", 63,
        "Listen to 'Nuthin' but a G Thang' — the synthesizer whine was played on a Minimoog by Dre himself. Then 'Let Me Ride' — the chorus samples a Parliament track at half-speed. Dre's secret was the low-end: he mixed the bass frequencies higher than anyone else at the time, making the album literally shake car speakers.",
        ["Hip-Hop", "G-Funk", "West Coast", "American", "1990s"], 1
    ),
    (
        "album-snoop-dogg-doggystyle", "Album",
        "Doggystyle",
        "Snoop Doggy Dogg 1993 — the fastest-selling debut album in hip-hop history at the time. Recorded while Snoop was on trial for murder (he was acquitted). Dr. Dre's production is G-funk at its silkiest. The album was recorded almost entirely at night because the studio's daytime clients were intimidated by Death Row's reputation.",
        "Doggystyle (1993) end-to-end", 53,
        "Listen to 'Gin and Juice' — Snoop wrote the chorus in 10 minutes. The laid-back delivery was not an act; Snoop recorded most of his vocals while high, and Dre liked the looseness. Then 'Murder Was the Case' — Snoop narrates his own fictional death, which became eerily prescient given his legal situation at the time.",
        ["Hip-Hop", "G-Funk", "West Coast", "American", "1990s"], 1
    ),
    (
        "album-tupac-all-eyez-on-me", "Album",
        "All Eyez on Me",
        "2Pac 1996 — the first double album in hip-hop history. Recorded in just 2 weeks after Suge Knight bailed Pac out of prison. Pac was living at the studio, writing and recording 3-4 songs a day. The album documents his transformation from political poet to thug icon in real time. He was dead 7 months after its release.",
        "All Eyez on Me (1996) — disc 1", 70,
        "Listen to 'California Love' — Dr. Dre produced it; the talkbox hook is Roger Troutman of Zapp. Then 'Ambitionz Az a Ridah' — Pac recorded it at 4 AM on his first night out of prison. The album's 27 tracks capture a man who knew he was running out of time and was determined to say everything.",
        ["Hip-Hop", "West Coast", "American", "1990s"], 1
    ),
    (
        "album-tupac-me-against-the-world", "Album",
        "Me Against the World",
        "2Pac 1995 — released while Pac was in prison serving a sentence for sexual assault. It debuted at #1, making him the first artist to have a #1 album while incarcerated. The album's introspection and vulnerability set it apart from the gangsta rap dominating the charts.",
        "Me Against the World (1995) end-to-end", 66,
        "Listen to 'Dear Mama' — a tribute to his mother Afeni Shakur, a Black Panther who was pregnant with Pac while defending herself in court. Then 'So Many Tears' — Pac wrote it on his 23rd birthday after being shot 5 times in a robbery attempt. The album reads like a suicide note from someone who survived.",
        ["Hip-Hop", "West Coast", "Conscious Rap", "American", "1990s"], 1
    ),
    (
        "album-ice-cube-amerikkkas-most-wanted", "Album",
        "AmeriKKKa's Most Wanted",
        "Ice Cube 1990 — his solo debut after leaving N.W.A. over a financial dispute. Recorded in New York with Public Enemy's production team The Bomb Squad, creating a West Coast/East Coast fusion that shouldn't have worked but became iconic. Cube was 21 and angrier than any rapper had ever been on record.",
        "AmeriKKKa's Most Wanted (1990) end-to-end", 50,
        "Listen to the title track — Cube narrates America from the perspective of its most wanted citizen. Then 'The Nigga Ya Love to Hate' — the chorus sarcastically thanks the listener for their voyeurism. Cube wrote the whole album in 3 weeks and recorded it in 6.",
        ["Hip-Hop", "West Coast", "Political Rap", "American", "1990s"], 1
    ),
    (
        "album-nwa-straight-outta-compton", "Album",
        "Straight Outta Compton",
        "N.W.A. 1988 — the album that put gangsta rap on the map and got the FBI to send a warning letter to the record label. Recorded for $12,000 in a small Torrance studio. The group's lyrics about police brutality, drug dealing, and life in Compton were so controversial that radio stations refused to play them and they still went platinum.",
        "Straight Outta Compton (1988) end-to-end", 60,
        "Listen to 'Fuck tha Police' — the song that prompted the FBI letter. Dr. Dre built the beat from a single drum loop and a siren sound. Then 'Express Yourself' — a track Dre produced as a counterpoint, sampling Charles Wright. The album's impact went beyond music: it forced America to confront what police were doing in Black neighborhoods.",
        ["Hip-Hop", "Gangsta Rap", "West Coast", "American", "1980s"], 1
    ),
    (
        "album-public-enemy-it-takes-a-nation", "Album",
        "It Takes a Nation of Millions to Hold Us Back",
        "Public Enemy 1988 — the album that turned hip-hop into a revolutionary force. The Bomb Squad's production was unlike anything before: layers of sirens, speeches, funk samples, and noise compressed into a sonic assault. Chuck D called rap 'the Black CNN.' The album made the case.",
        "It Takes a Nation of Millions (1988) end-to-end", 58,
        "Listen to 'Bring the Noise' — the opening siren is a recording from an actual police car. Then 'Rebel Without a Pause' — the squealing horn sample was pitched up so high it sounds like a fire alarm. Chuck D's baritone was a deliberate contrast to the high-pitched production, anchoring the chaos.",
        ["Hip-Hop", "East Coast", "Political Rap", "American", "1980s"], 1
    ),
    (
        "album-public-enemy-fear-of-a-black-planet", "Album",
        "Fear of a Black Planet",
        "Public Enemy 1990 — recorded during the controversy over Professor Griff's antisemitic remarks, which nearly destroyed the band. The album's denser, darker production reflected the internal chaos. 'Fight the Power' had premiered in Spike Lee's Do the Right Thing a year earlier and became an anthem.",
        "Fear of a Black Planet (1990) end-to-end", 52,
        "Listen to 'Fight the Power' — the song references Elvis, John Wayne, and the entire white power structure in under 5 minutes. Then '911 Is a Joke' — Flavor Flav's solo track about ambulance response times in Black neighborhoods. The Bomb Squad reportedly used 150 different samples across the album.",
        ["Hip-Hop", "East Coast", "Political Rap", "American", "1990s"], 1
    ),
    (
        "album-eric-b-and-rakim-paid-in-full", "Album",
        "Paid in Full",
        "Eric B. & Rakim 1987 — the album that changed how rappers rapped. Before Rakim, hip-hop flow was relatively simple; after Rakim, internal rhyme schemes, multi-syllabic patterns, and relaxed delivery became the standard. Eric B.'s production was built from James Brown samples slowed to a crawl.",
        "Paid in Full (1987) end-to-end", 45,
        "Listen to 'I Ain't No Joke' — Rakim's opening verse contains 16 bars with 14 internal rhymes. Then 'Eric B. Is President' — recorded in one take in Marley Marl's living room. Rakim was 18. The album cost $5,000 to make and became the blueprint for every rapper who followed.",
        ["Hip-Hop", "East Coast", "American", "1980s"], 1
    ),
    (
        "album-mobb-deep-the-infamous", "Album",
        "The Infamous",
        "Mobb Deep 1995 — recorded in a Queensbridge apartment by two teenagers, Havoc and Prodigy. Havoc, who had never produced before, taught himself on an Akai MPC and a cheap keyboard. The album's sound — minor-key piano loops, sparse drums, whispered threats — defined East Coast hardcore hip-hop.",
        "The Infamous (1995) end-to-end", 67,
        "Listen to 'Shook Ones, Pt. II' — Havoc found the piano sample on a Herbie Hancock record and slowed it down until it sounded haunted. Then 'Survival of the Fittest' — the beat is two notes and a snare. Prodigy was 19 when he wrote 'I'm only 19 but my mind is old.' The album made Queensbridge the most famous housing project in music.",
        ["Hip-Hop", "East Coast", "Hardcore Hip-Hop", "American", "1990s"], 1
    ),
    (
        "album-jay-z-reasonable-doubt", "Album",
        "Reasonable Doubt",
        "Jay-Z 1996 — recorded when no label would sign him, so he started Roc-A-Fella Records with Damon Dash and Kareem Burke. The album was recorded in various New York studios, often at night after other sessions had finished. Jay-Z was 26, a former drug dealer who'd decided rap was his way out.",
        "Reasonable Doubt (1996) end-to-end", 55,
        "Listen to 'Can't Knock the Hustle' — Mary J. Blige sings the hook. Jay-Z's flow is so relaxed it sounds like a conversation. Then 'Dead Presidents II' — the Nas sample was a deliberate homage to Illmatic. The album's mafioso-rap style — all Italian suits, Cuban cigars, and champagne — created Jay-Z's persona as hip-hop's CEO.",
        ["Hip-Hop", "East Coast", "Mafioso Rap", "American", "1990s"], 1
    ),
    (
        "album-jay-z-the-blueprint", "Album",
        "The Blueprint",
        "Jay-Z 2001 — released on September 11, 2001. Despite the attacks, it debuted at #1 and sold 427,000 copies in its first week. Recorded in just 30 days with producers Kanye West (then unknown) and Just Blaze. Jay-Z was simultaneously preparing for a trial on assault charges; he recorded vocals during court recesses.",
        "The Blueprint (2001) end-to-end", 63,
        "Listen to 'Izzo (H.O.T.A.)' — Kanye built the beat from a Jackson 5 sample pitched up to chipmunk speed, a technique that would define 2000s hip-hop. Then 'Song Cry' — Jay-Z's vulnerability here was unprecedented for a rapper at his commercial peak. The album's soul-sample production was Kanye's audition for his own career.",
        ["Hip-Hop", "East Coast", "American", "2000s"], 1
    ),
    (
        "album-outkast-aquemini", "Album",
        "Aquemini",
        "OutKast 1998 — the album where André 3000 and Big Boi proved the South could make art as ambitious as anyone. The title combines their zodiac signs (Aquarius and Gemini). Recorded in Atlanta with live instrumentation when most hip-hop relied on samples. George Clinton called it 'the funkiest album I've heard in years.'",
        "Aquemini (1998) end-to-end", 74,
        "Listen to 'Rosa Parks' — the song's energy is so infectious that Rosa Parks herself sued for using her name (the case was eventually settled). Then 'SpottieOttieDopaliscious' — a 7-minute track with no chorus, just horn stabs and spoken-word verses about Atlanta nightlife. André's verse about the club shooting is one of the most vivid stories in hip-hop.",
        ["Hip-Hop", "Southern Hip-Hop", "American", "1990s"], 1
    ),
    (
        "album-outkast-stankonia", "Album",
        "Stankonia",
        "OutKast 2000 — recorded in a converted Atlanta warehouse they called Stankonia Studios. The album broke every rule: funk, psychedelia, drum & bass, gospel, and Prince-influenced freak-outs. 'B.O.B.' was recorded at 165 BPM when most rap was under 100. It became their commercial breakthrough.",
        "Stankonia (2000) end-to-end", 73,
        "Listen to 'B.O.B. (Bombs Over Baghdad)' — the drum programming by André 3000 was inspired by UK jungle music. The guitar solo was played through a wah-wah pedal by a session guitarist who'd never heard OutKast before. Then 'Ms. Jackson' — André wrote it as an apology to Erykah Badu's mother after their breakup.",
        ["Hip-Hop", "Southern Hip-Hop", "Funk", "American", "2000s"], 1
    ),
    (
        "album-ugk-ridin-dirty", "Album",
        "Ridin' Dirty",
        "UGK 1996 — Bun B and Pimp C's masterpiece. Pimp C, incarcerated at the time of release, produced most of the album from prison using a cassette recorder to sketch ideas. The album's title refers to driving with illegal substances or weapons, a constant reality for the Port Arthur, Texas duo.",
        "Ridin' Dirty (1996) end-to-end", 65,
        "Listen to 'One Day' — Pimp C's production layers a Gospel sample over a slow-rolling bassline. Then 'Murder' — Bun B's verse is a masterclass in Southern storytelling, delivered with the authority of someone who saw everything he describes. The album defined Southern hip-hop's independent spirit before the mainstream caught on.",
        ["Hip-Hop", "Southern Hip-Hop", "American", "1990s"], 1
    ),
    (
        "album-scarface-the-diary", "Album",
        "The Diary",
        "Scarface 1994 — the Geto Boys member's third solo album, recorded during a period of severe depression. Scarface's storytelling — about poverty, mental illness, and survival — brought literary depth to gangsta rap. The album's production by N.O. Joe and Mike Dean created the template for Southern hip-hop's dark, soulful sound.",
        "The Diary (1994) end-to-end", 50,
        "Listen to 'I Seen a Man Die' — Scarface narrates a death from three perspectives: the victim, the shooter, and God. Then 'Mind Playin' Tricks on Me' (originally a Geto Boys track) — Scarface's verse about paranoia and hallucinations is based on his own experiences with depression. He later checked himself into a psychiatric hospital.",
        ["Hip-Hop", "Southern Hip-Hop", "American", "1990s"], 1
    ),
    (
        "album-lil-wayne-tha-carter-iii", "Album",
        "Tha Carter III",
        "Lil Wayne 2008 — recorded during what Wayne called his 'mixtape era', when he was releasing a new track almost daily. The album sold 1 million copies in its first week, the last hip-hop album to do so for nearly a decade. Wayne's wordplay — puns, metaphors, free association — rewired what rap lyrics could be.",
        "Tha Carter III (2008) end-to-end", 77,
        "Listen to 'A Milli' — the beat is a single vocal sample repeated for 3 minutes. Wayne recorded his verse in 20 minutes without writing anything down. Then 'Lollipop' — the Auto-Tuned hook was Wayne's first experiment with singing, which would define the next decade of hip-hop. Static Major, who co-wrote the hook, died a week before the album's release.",
        ["Hip-Hop", "Southern Hip-Hop", "American", "2000s"], 1
    ),
    (
        "album-ti-king", "Album",
        "King",
        "T.I. 2006 — the album that crowned him 'King of the South.' Recorded in Atlanta with a roster of producers including Just Blaze, Toomp, and Swizz Beatz. T.I.'s cocky, conversational flow — half-rapping, half-talking — bridged the gap between Southern trap and mainstream hip-hop.",
        "King (2006) end-to-end", 64,
        "Listen to 'What You Know' — the synth line was played by DJ Toomp on a Roland Juno-106. T.I.'s opening line 'Don't you know I got the key by me?' is delivered with the confidence of someone who doesn't need to raise his voice. Then 'Why You Wanna' — the song samples a Crystal Waters house track, proving Southern hip-hop could make dance records too.",
        ["Hip-Hop", "Southern Hip-Hop", "Trap", "American", "2000s"], 1
    ),
    (
        "album-young-jeezy-thug-motivation-101", "Album",
        "Let's Get It: Thug Motivation 101",
        "Young Jeezy 2005 — the album that made trap music a national phenomenon. Recorded in Atlanta with Shawty Redd's minimalist production: 808 drums, church bells, and synth strings. Jeezy's ad-libs ('yeahhh', 'that's riiight') became as quotable as his verses.",
        "Thug Motivation 101 (2005) end-to-end", 65,
        "Listen to 'Soul Survivor' — Akon sings the hook about surviving the streets. Jeezy's verse is a checklist of everything he overcame. Then 'Go Crazy' — the beat is an 808 kick and a single synth note. Jay-Z's guest verse was recorded in 15 minutes in a hotel room. The album's snowman logo became a cultural symbol.",
        ["Hip-Hop", "Southern Hip-Hop", "Trap", "American", "2000s"], 1
    ),
    (
        "album-gucci-mane-the-state-vs-radric-davis", "Album",
        "The State vs. Radric Davis",
        "Gucci Mane 2009 — his major-label debut, named after his ongoing legal case. Gucci's stream-of-consciousness delivery and unpredictable punchlines made him a cult hero. The album was recorded between jail stints. The cover is a literal mugshot.",
        "The State vs. Radric Davis (2009) end-to-end", 60,
        "Listen to 'Wasted' — featuring Plies, the chorus about getting drunk became a college anthem. Then 'Lemonade' — the beat sounds like a carnival ride breaking down. Gucci's influence on the next decade of hip-hop — from Migos to 21 Savage — is hard to overstate.",
        ["Hip-Hop", "Southern Hip-Hop", "Trap", "American", "2000s"], 1
    ),
    (
        "album-future-ds2", "Album",
        "DS2 (Dirty Sprite 2)",
        "Future 2015 — the album that perfected the sound of narcotic melancholy. Recorded with Metro Boomin, Southside, and Zaytoven in Atlanta. Future's Auto-Tuned voice, half-sung and half-cried, captured a specific kind of post-success emptiness. The album's influence is audible in almost every rap record made since.",
        "DS2 (2015) end-to-end", 53,
        "Listen to 'Thought It Was a Drought' — Future mumbles the chorus through a fog of cough syrup, and somehow it's a hit. Then 'Where Ya At' — Drake's guest verse was recorded via email. The album's production is built almost entirely from 808s, hi-hats, and atmospheric synths — the definitive trap sound.",
        ["Hip-Hop", "Trap", "Southern Hip-Hop", "American", "2010s"], 1
    ),
    (
        "album-migos-culture", "Album",
        "Culture",
        "Migos 2017 — the album that turned the triplet flow into a global phenomenon. Recorded in Atlanta and Los Angeles over a year. The trio — Quavo, Offset, and Takeoff — traded verses with a chemistry that felt telepathic. 'Bad and Boujee' became a meme, a #1 single, and the cultural moment when trap officially conquered pop.",
        "Culture (2017) end-to-end", 58,
        "Listen to 'Bad and Boujee' — Lil Uzi Vert's ad-libbed verse was recorded in 10 minutes. The 'raindrop, drop-top' opening couplet became one of the most-quoted lyrics of the decade. Then 'T-Shirt' — the triplet flow on the verses turns the English language into pure rhythm. Offset's verse on 'Deadz' was widely considered the best rap verse of 2017.",
        ["Hip-Hop", "Trap", "Southern Hip-Hop", "American", "2010s"], 1
    ),
    (
        "album-travis-scott-astroworld", "Album",
        "Astroworld",
        "Travis Scott 2018 — named after the demolished Houston amusement park, an album-length nostalgia trip. Recorded over two years with dozens of producers. Scott's production philosophy — maximalist, atmospheric, deliberately disorienting — made the album feel like a theme park ride itself.",
        "Astroworld (2018) end-to-end", 59,
        "Listen to 'Sicko Mode' — the song has three distinct beat switches, each produced by a different team. Drake's verse appears at a seemingly random moment. Then 'Stop Trying to Be God' — Stevie Wonder plays harmonica and James Blake sings the bridge. Scott's genius is making chaos feel like inevitability.",
        ["Hip-Hop", "Trap", "Southern Hip-Hop", "American", "2010s"], 1
    ),
    (
        "album-common-resurrection", "Album",
        "Resurrection",
        "Common 1994 — recorded when he was 22, still called Common Sense (he later dropped 'Sense' after a lawsuit from a reggae band). The album's production by No I.D. (then unknown, later Kanye's mentor) established the soul-sampling Chicago sound. Common's battle-rap skills and social consciousness made him the Midwest's answer to Nas.",
        "Resurrection (1994) end-to-end", 54,
        "Listen to 'I Used to Love H.E.R.' — the 'her' in question is hip-hop itself, personified as a woman who's lost her way. The extended metaphor runs for the entire song. Then 'Resurrection' — the title track's opening bars are a direct response to Ice Cube dissing him. No I.D. built the beat from a single Ahmad Jamal piano loop.",
        ["Hip-Hop", "Conscious Rap", "Midwest", "American", "1990s"], 1
    ),
    (
        "album-mos-def-black-on-both-sides", "Album",
        "Black on Both Sides",
        "Mos Def 1999 — a solo debut that ranges from Afrocentric poetry to jazz-inflected rap to straight-up rock. Recorded in New York with a live band on several tracks. Mos Def sang and played drums, bass, and keyboards himself. The album is a deliberate argument that hip-hop could be as musically sophisticated as any genre.",
        "Black on Both Sides (1999) end-to-end", 71,
        "Listen to 'Mathematics' — DJ Premier's beat is built from a single 2-bar loop. Mos Def packs references to statistics, history, and street mathematics into every verse. Then 'Umi Says' — the wordless chorus was improvised in the studio. Mos Def later said the album was his attempt to 'make hip-hop that my mother could listen to.'",
        ["Hip-Hop", "Conscious Rap", "East Coast", "American", "1990s"], 1
    ),
    (
        "album-talib-kweli-quality", "Album",
        "Quality",
        "Talib Kweli 2002 — the Brooklyn rapper's most fully realized solo album. Produced primarily by Kanye West (pre-fame) and Hi-Tek. Kweli's lyrical density — five-syllable internal rhymes packed into every bar — made the album a Rosetta Stone for aspiring MCs.",
        "Quality (2002) end-to-end", 64,
        "Listen to 'Get By' — Kanye's beat samples Nina Simone's 'Sinnerman' pitched to a frantic tempo. Kweli's three verses each describe different paths to survival: hustling, education, and faith. Then 'The Proud' — produced by Ayatollah, the song is a roll call of Black pride references woven into a love song.",
        ["Hip-Hop", "Conscious Rap", "East Coast", "American", "2000s"], 1
    ),
    (
        "album-dead-prez-lets-get-free", "Album",
        "Let's Get Free",
        "dead prez 2000 — recorded in New York by stic.man and M-1, two activists who met at a Black Panther event. The album's revolutionary politics — veganism, Black nationalism, anti-capitalism — were so far left of mainstream hip-hop that they made Public Enemy sound moderate. The production, dark and minimal, matched the urgency.",
        "Let's Get Free (2000) end-to-end", 58,
        "Listen to 'Hip-Hop' — the chorus is a single repeated phrase: 'It's bigger than hip-hop.' The song became an activist anthem. Then 'Mind Sex' — a love song about intellectual and emotional intimacy as political resistance. The album was produced by Lord Jamar of Brand Nubian, who gave it a gritty East Coast foundation.",
        ["Hip-Hop", "Conscious Rap", "Political Rap", "American", "2000s"], 1
    ),
    (
        "album-dizzee-rascal-boy-in-da-corner", "Album",
        "Boy in da Corner",
        "Dizzee Rascal 2003 — the album that invented grime. Recorded in a bedroom in Bow, East London, on a PlayStation and a cheap PC. Dizzee was 18, processing knife crime, teenage pregnancy, and inner-city London life into something entirely new. The beats — built from video game sounds, ringtones, and distorted bass — sounded like nothing else.",
        "Boy in da Corner (2003) end-to-end", 57,
        "Listen to 'I Luv U' — the beat is a single distorted bass note and a snare. The call-and-response between Dizzee and the female vocalist narrates a teenage pregnancy argument. Then 'Fix Up, Look Sharp' — the Billy Squier sample was Dizzee's idea after hearing it in a Nike commercial. The album won the Mercury Prize; Dizzee was 19.",
        ["Hip-Hop", "Grime", "UK", "British", "2000s"], 1
    ),
    (
        "album-wiley-treddin-on-thin-ice", "Album",
        "Treddin' on Thin Ice",
        "Wiley 2004 — the godfather of grime's debut album. Recorded in Bow, East London, the same neighborhood as Dizzee Rascal. Wiley's production — sparse, cold, built from video game soundtracks and pirate radio static — created the sonic template for grime. The album was released on XL Recordings after a bidding war.",
        "Treddin' on Thin Ice (2004) end-to-end", 45,
        "Listen to 'Wot Do U Call It?' — Wiley addresses the question everyone was asking: what is this music? The answer: 'It's not garage, it's not hip-hop, it's just... this.' Then 'Eskimo' — the instrumental track that became the foundation of grime production. Wiley's Eskibeat sound — cold synth lines over 140 BPM drums — influenced Skepta, Stormzy, and an entire scene.",
        ["Hip-Hop", "Grime", "UK", "British", "2000s"], 1
    ),
    (
        "album-skepta-konnichiwa", "Album",
        "Konnichiwa",
        "Skepta 2016 — the album that brought grime to the global mainstream. Recorded in London and Tokyo. Skepta produced most of the album himself on a laptop. The album won the Mercury Prize. Drake and Kanye West both publicly championed it, but Skepta refused to dilute the sound for American audiences.",
        "Konnichiwa (2016) end-to-end", 50,
        "Listen to 'Shutdown' — the opening line 'truss me daddy' became a catchphrase. The video, shot on a London estate, was banned from some platforms for being 'too provocative.' Then 'Man (Gang)' — the beat is a single bass note and an air horn. Skepta's delivery makes 'I said real gangsters don't flex' sound like a universal truth.",
        ["Hip-Hop", "Grime", "UK", "British", "2010s"], 1
    ),
    (
        "album-stormzy-gang-signs-and-prayer", "Album",
        "Gang Signs & Prayer",
        "Stormzy 2017 — the first grime album to debut at #1 on the UK charts. Recorded over two years in London. Stormzy's range — from gospel-choir choruses to hard-edged grime bars — showed that British rap could be commercially massive without compromise. The album cover is Stormzy in a suit, holding a Bible.",
        "Gang Signs & Prayer (2017) end-to-end", 59,
        "Listen to 'Blinded by Your Grace, Pt. 2' — the song is a gospel hymn featuring a full choir, and it became a festival singalong. Then 'Big for Your Boots' — a straight grime track produced by Sir Spyro. The album's title is literal: it alternates between street narratives and spiritual searching.",
        ["Hip-Hop", "Grime", "UK", "British", "2010s"], 1
    ),
    (
        "album-the-streets-original-pirate-material", "Album",
        "Original Pirate Material",
        "Mike Skinner (The Streets) 2002 — the album that proved British rap didn't need to sound American. Recorded in a Brixton bedroom on a laptop. Skinner narrated the mundane details of British working-class life — kebabs, minicabs, PlayStation, cheap lager — over garage-influenced beats. His Birmingham accent was so thick Americans needed subtitles.",
        "Original Pirate Material (2002) end-to-end", 47,
        "Listen to 'Has It Come to This?' — Skinner's deadpan delivery of observations about pub life, Nokia phones, and Channel 4. Then 'Weak Become Heroes' — a 5-minute story about a night out at a club, taking ecstasy, and the sunrise after. The piano sample is from a 1970s library music record Skinner found at a car boot sale.",
        ["Hip-Hop", "UK Hip-Hop", "UK Garage", "British", "2000s"], 1
    ),
    (
        "album-dave-psychodrama", "Album",
        "Psychodrama",
        "Dave 2019 — the album that won the Mercury Prize and made British rap feel like literature. Recorded over a year in London and Los Angeles. Dave plays piano on nearly every track. The album is structured as a therapy session, which is literal: Dave's brother is serving a life sentence, and the album processes that trauma.",
        "Psychodrama (2019) end-to-end", 51,
        "Listen to 'Black' — Dave lists the experience of being Black in Britain in a single verse that runs for 3 minutes without a chorus. Then 'Lesley' — an 11-minute narrative about an abusive relationship, told from the perspective of a concerned friend. The album's therapy-session framing gives it a structure no other rap album has attempted.",
        ["Hip-Hop", "UK Rap", "Conscious Rap", "British", "2010s"], 1
    ),
    (
        "album-chief-keef-finally-rich", "Album",
        "Finally Rich",
        "Chief Keef 2012 — the album that invented drill music. Recorded in Chicago when Keef was 16 and already a legend on the South Side. The production by Young Chop — sparse, menacing, built from 808s and horror-movie synths — created a new sonic language. Keef's delivery, which sounded half-asleep, was actually a refusal to perform emotion.",
        "Finally Rich (2012) end-to-end", 45,
        "Listen to 'I Don't Like' — the song that got Kanye West's attention, leading to a remix that introduced drill to the world. Then 'Love Sosa' — the call-and-response chorus was recorded at 3 AM after Keef had been up for two days. The album's influence on modern hip-hop — from the ad-libs to the production style — is impossible to overstate.",
        ["Hip-Hop", "Drill", "Midwest", "American", "2010s"], 1
    ),
    (
        "album-pop-smoke-shoot-for-the-stars", "Album",
        "Shoot for the Stars, Aim for the Moon",
        "Pop Smoke 2020 — released posthumously after the 20-year-old was murdered in a home invasion. The album bridges Brooklyn drill with mainstream hip-hop. 50 Cent executive-produced it. Pop Smoke's gravelly voice — he sounded at least a decade older than he was — made drill radio-friendly without losing its edge.",
        "Shoot for the Stars (2020) end-to-end", 56,
        "Listen to 'Dior' — the song that became a global anthem after Pop Smoke's death. The 808 pattern was created by producer 808Melo in 15 minutes. Then 'What You Know Bout Love' — a pop-rap love song that samples a UK garage track, proving drill could be tender. The album debuted at #1; Pop Smoke never lived to see it.",
        ["Hip-Hop", "Drill", "East Coast", "American", "2020s"], 1
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
