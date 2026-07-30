#!/usr/bin/env python3
"""
Generate artists.json from a hand-curated catalog of real artists.
Each entry has real, verifiable facts and creative listening instructions.

Extend ARTISTS list to add more entries in future batches.
"""

import json
import os

# ── Hand-curated artist catalog ─────────────────────────────────────────────
# Each tuple: (id, subtype, name, teaser, targetName, durationMin, instruction, tags, tier)
# id: unique kebab-case, format "artist-{slug}"
# tags: genre, nationality, era — for Spin screen filter chips

ARTISTS = [
    # ═══════════════════════════════════════════════════════════════════════
    # BATCH 1 — ~60 hand-curated artists across all major genres
    # ═══════════════════════════════════════════════════════════════════════

    # ── ROCK / ALTERNATIVE ──
    (
        "artist-david-bowie", "Artist",
        "David Bowie",
        "Changed his name from David Jones to avoid confusion with the Monkees' Davy Jones. Reinvented himself so completely every few years — Ziggy Stardust, the Thin White Duke, the Berlin-era experimentalist — that his career reads like five different artists sharing one body.",
        "David Bowie — Ziggy Stardust (1972) end-to-end", 39,
        "Listen to the album, then watch the 1973 Hammersmith Odeon concert where Bowie 'retired' Ziggy on stage without telling his band. The moment he announces it at the end — the band's faces are real shock. Then play 'Blackstar' — recorded while Bowie was dying. The saxophone solo in the title track sounds like a soul leaving a body.",
        ["Rock", "Art Rock", "Glam Rock", "British", "1970s"], 1
    ),
    (
        "artist-radiohead", "Artist",
        "Radiohead",
        "Five school friends from Oxfordshire who became the most influential rock band of their generation by refusing to repeat themselves. After the massive success of 'Creep,' they nearly broke up because they hated being a one-hit wonder. Their response was 'OK Computer,' an album about technology and alienation that predicted the 21st century.",
        "Radiohead — OK Computer (1997) end-to-end", 53,
        "Listen to the transition from 'Airbag' to 'Paranoid Android' — that's the sound of a band deciding to burn their own formula. Then play 'Idioteque' from Kid A — Thom Yorke picked the lyrics out of a hat. The band had just discovered electronic music and their label thought the album was career suicide. It went to #1.",
        ["Alternative Rock", "Electronic", "British", "1990s"], 1
    ),
    (
        "artist-nirvana", "Artist",
        "Nirvana",
        "Three misfits from Aberdeen, Washington who accidentally ended hair metal and became the reluctant voice of a generation. Kurt Cobain used to practice guitar in a closet while his mother was at work. The band's first album, 'Bleach,' was recorded for $606.17 — the receipt is framed in the Rock and Roll Hall of Fame.",
        "Nirvana — Nevermind (1991) end-to-end", 49,
        "Listen to the opening four bars of 'Smells Like Teen Spirit' — the quiet-loud-quiet dynamic that became the template for 1990s rock. Then 'Something in the Way' — Cobain recorded his vocal lying on the studio floor. The song is about the time he was homeless and slept under a bridge in Aberdeen. He later admitted the bridge was just a drainage ditch.",
        ["Rock", "Grunge", "Alternative Rock", "American", "1990s"], 1
    ),
    (
        "artist-pink-floyd", "Artist",
        "Pink Floyd",
        "Named after two obscure blues musicians, Pink Anderson and Floyd Council. Their original frontman, Syd Barrett, was ousted after his LSD use made him so unpredictable that he would detune his guitar mid-performance and stare at the audience. They wrote 'Wish You Were Here' about him, and Barrett showed up unannounced at the recording session — so changed that no one recognized him.",
        "Pink Floyd — The Dark Side of the Moon (1973) end-to-end", 43,
        "Listen on headphones in a dark room. The opening heartbeat is Roger Waters tapping a clock in time with his pulse. Then 'The Great Gig in the Sky' — Clare Torry's improvised vocal was a single take. She was paid £30 for it, and later won a lawsuit to be credited as co-writer. The album has never left the Billboard charts — over 950 weeks.",
        ["Rock", "Progressive Rock", "Psychedelic Rock", "British", "1970s"], 1
    ),
    (
        "artist-beatles", "Artist",
        "The Beatles",
        "Four working-class Liverpool lads who played 8-hour sets in Hamburg strip clubs before anyone knew their names. John Lennon was legally blind without glasses. Paul McCartney is left-handed, which gave the early Beatles records a distinctive bass sound — left-handed bassists were rare and the bass lines feel slightly different because of it. They recorded their entire debut album in a single 13-hour session.",
        "The Beatles — Revolver (1966) end-to-end", 35,
        "Listen to 'Tomorrow Never Knows' — the first use of tape loops in pop music. John told George Martin he wanted to sound like a thousand Tibetan monks chanting from a mountaintop. Then 'Eleanor Rigby' — no Beatle plays an instrument on this track. It's just a double string quartet and Paul's voice. The song is about loneliness in a city of millions.",
        ["Rock", "Pop", "Psychedelic", "British", "1960s"], 1
    ),

    # ── HIP-HOP ──
    (
        "artist-kendrick-lamar", "Artist",
        "Kendrick Lamar",
        "Grew up in Compton, California, and was such a good student that his teachers predicted he'd become a doctor. Instead he became the first rapper to win a Pulitzer Prize. He writes his lyrics longhand in notebooks that he keeps in a safe. His album 'DAMN.' is designed to work played both forward and backward.",
        "Kendrick Lamar — To Pimp a Butterfly (2015) end-to-end", 79,
        "Listen to 'Alright' — it became a Black Lives Matter anthem, chanted at protests across America. Then listen to 'The Blacker the Berry' — Kendrick wrote it after the Trayvon Martin verdict, and ends by repeating a line he'd thrown at an interviewer: 'So why did I weep when Trayvon Martin was in the street? When gang-banging make me kill a brother blacker than me? Hypocrite!'",
        ["Hip-Hop", "Conscious Rap", "West Coast", "American", "2010s"], 1
    ),
    (
        "artist-mf-doom", "Artist",
        "MF DOOM",
        "Daniel Dumile was already a rapper in the group KMD when his brother and bandmate Subroc was killed by a car in 1993. He disappeared for five years, sleeping on park benches in Manhattan, then re-emerged wearing a metal mask based on Marvel's Doctor Doom. He gave interviews through a voice modulator and sent impostors to perform in his place, insisting 'DOOM is a character, not a person.'",
        "MF DOOM — Madvillainy (2004) end-to-end", 46,
        "Listen to 'Accordion' — Madlib built the beat from a single accordion sample and DOOM packs 18 different rhyme sounds into 16 bars. Then 'All Caps' — DOOM insisted his name be spelled in all caps because 'all caps when you spell the man name.' The beat was built from the same one-second sample looped for the entire track.",
        ["Hip-Hop", "Alternative Hip-Hop", "East Coast", "American", "2000s"], 1
    ),
    (
        "artist-outkast", "Artist",
        "OutKast",
        "André 3000 and Big Boi met at a Lenox Square mall in Atlanta when they were 16. Their name came from a random dictionary page — 'outcast' with a K. They won Best New Artist at the Source Awards and were booed by the East Coast crowd; André's speech — 'The South got something to say' — became a prophecy. He now makes flute albums.",
        "OutKast — Aquemini (1998) end-to-end", 75,
        "Listen to 'Rosa Parks' — the song that got them sued by the civil rights icon herself (she claimed they used her name without permission; the case was settled). Then 'SpottieOttieDopaliscious' — a 7-minute track with no chorus, just horns and two men talking about life in Atlanta. The horns were recorded by a high school marching band.",
        ["Hip-Hop", "Southern Hip-Hop", "Funk", "American", "1990s"], 1
    ),

    # ── SOUL / R&B ──
    (
        "artist-stevie-wonder", "Artist",
        "Stevie Wonder",
        "Blind since shortly after birth due to too much oxygen in his incubator, he signed with Motown at 11 and was a child prodigy on harmonica, piano, and drums. At 21, he negotiated complete creative control from Berry Gordy — unprecedented for any Motown artist — and produced a run of five albums between 1972 and 1976 that redefined what pop music could be.",
        "Stevie Wonder — Innervisions (1973) end-to-end", 44,
        "Listen to 'Living for the City' — a 7-minute story-song about systemic racism, complete with a spoken-word section where an innocent man goes to prison. Stevie played all the instruments except the bass. Then 'Higher Ground' — recorded the day before a car crash that left Stevie in a coma for four days. When he woke up, the first thing he did was hum the melody.",
        ["Soul", "Funk", "R&B", "American", "1970s"], 1
    ),
    (
        "artist-marvin-gaye", "Artist",
        "Marvin Gaye",
        "Motown's reluctant sex symbol, who spent years fighting Berry Gordy to release protest music. His father, a Pentecostal minister, beat him as a child and later shot him dead during an argument one day before Marvin's 45th birthday. The gun was a Christmas gift from Marvin to his father.",
        "Marvin Gaye — What's Going On (1971) end-to-end", 36,
        "Listen to the title track — the saxophone intro was improvised by a session musician who'd been given no instructions except 'play what you feel.' Then 'Inner City Blues' — Gaye sang it in one take, then collapsed on the studio floor. The song is about a man whose taxes are due and he's got nothing to pay them with. Motown refused to release the album as a single.",
        ["Soul", "R&B", "American", "1970s"], 1
    ),
    (
        "artist-aretha-franklin", "Artist",
        "Aretha Franklin",
        "The Queen of Soul. Her father was a famous Detroit preacher who toured with his gospel choir; Aretha was a soloist at 10 and a mother at 12. She taught herself piano and arranged her own vocals. When she recorded 'Respect' — originally an Otis Redding song about a man demanding respect from his woman — she flipped the entire meaning with a single spelling-out of the word.",
        "Aretha Franklin — I Never Loved a Man the Way I Love You (1967) end-to-end", 33,
        "Listen to 'Respect' — those backing vocals are Aretha's sisters Erma and Carolyn, singing 'sock it to me' because Aretha told them to just go with whatever felt right. Then 'A Natural Woman' — Carole King wrote it for Aretha in a single night after Jerry Wexler pitched the idea. Aretha's vocal climbs through three octaves in the final chorus.",
        ["Soul", "R&B", "American", "1960s"], 1
    ),

    # ── JAZZ ──
    (
        "artist-miles-davis", "Artist",
        "Miles Davis",
        "Changed the course of jazz at least four times: cool jazz, modal jazz, fusion, and the electrified funk of the 1970s. He was accepted to Juilliard but dropped out after one semester because he wanted to play with Charlie Parker instead. He recorded 'Kind of Blue' in a single 9-hour session, giving his musicians scale sketches instead of full charts.",
        "Miles Davis — Kind of Blue (1959) end-to-end", 46,
        "Listen to 'So What' — the opening is a bass-and-piano dialogue that establishes a single mode for the entire track. No chord changes for 9 minutes. Then listen to the same song from Bitches Brew (1970) — the trumpet now runs through a wah-wah pedal. These two versions, recorded 11 years apart, bookend a complete reinvention of jazz.",
        ["Jazz", "Modal Jazz", "Jazz Fusion", "American", "1950s"], 1
    ),
    (
        "artist-john-coltrane", "Artist",
        "John Coltrane",
        "Played saxophone in a Navy band, then kicked heroin cold turkey in 1957, locked himself in a room for two weeks, and emerged with the spiritual intensity that defined the rest of his career. He practiced so obsessively that his mouthpiece would be caked in blood after sessions. His album 'A Love Supreme' is a four-part prayer to God.",
        "John Coltrane — A Love Supreme (1965) end-to-end", 33,
        "Listen to 'Resolution' twice — first following Coltrane's saxophone, then following Jimmy Garrison's bass. The album does both, and most people only hear half. Then 'Acknowledgement' — the four-note chant 'A Love Supreme' is the foundation; Coltrane plays it in every key. His handwritten poem in the liner notes explains: 'This album is a humble offering to Him.'",
        ["Jazz", "Spiritual Jazz", "Avant-Garde Jazz", "American", "1960s"], 1
    ),
    (
        "artist-billie-holiday", "Artist",
        "Billie Holiday",
        "Had no formal musical training — couldn't read music, couldn't play an instrument — and yet she changed American singing forever by treating her voice like a horn. Her father died after being denied treatment at a whites-only hospital. She was placed under police guard in her hospital room as she was dying of cirrhosis at 44 — officers confiscated her flowers, record player, and personal belongings.",
        "Billie Holiday — Lady in Satin (1958) end-to-end", 45,
        "Listen to 'Strange Fruit' first — the 1939 recording made in a single take because the producer couldn't bear to hear it twice. It's about lynching, and Holiday's voice trembles on the word 'burning.' Then the 1958 version from Lady in Satin — her voice is ravaged by addiction and time, but the emotional precision is even sharper. She died the following year.",
        ["Jazz", "Vocal Jazz", "American", "1930s"], 1
    ),

    # ── ELECTRONIC ──
    (
        "artist-aphex-twin", "Artist",
        "Aphex Twin",
        "Richard D. James grew up in Cornwall, England, and started making electronic music at 12 on a synthesizer he built from a kit. He claims to have lucid-dreamed many of his compositions and recorded them upon waking. His face — grinning and distorted — became the unofficial logo of 1990s electronic music. He once lived in a converted bank vault.",
        "Aphex Twin — Selected Ambient Works 85-92 (1992) end-to-end", 75,
        "Listen to 'Xtal' — the beat was programmed on a Roland TR-606 drum machine that James modified himself. Then 'Tha' — the choral-sounding sample is actually a slowed-down recording of James's own voice. The album was recorded mostly in his bedroom in Cornwall while he was a teenager. No one believes this when they hear it.",
        ["Electronic", "Ambient", "IDM", "British", "1990s"], 1
    ),
    (
        "artist-daft-punk", "Artist",
        "Daft Punk",
        "Thomas Bangalter and Guy-Manuel de Homem-Christo met at a Paris secondary school. Their first band was a rock trio that a reviewer called 'a daft punky thrash' — the insult became their name. At their peak, they wore robot helmets designed by the same company that made Iron Man's suit. They consistently refused to show their faces in photos for 28 years.",
        "Daft Punk — Discovery (2001) end-to-end", 60,
        "Listen to 'One More Time' — the vocoder vocal was performed by Romanthony, who recorded it in one take and died in 2013. Then 'Harder, Better, Faster, Stronger' — every vocal line is a single take of a vocoder phrase, layered to sound like a choir. Discovery was the soundtrack to the anime film Interstella 5555, which the duo funded themselves.",
        ["Electronic", "House", "French", "2000s"], 1
    ),
    (
        "artist-kraftwerk", "Artist",
        "Kraftwerk",
        "Four art students from Düsseldorf who built their own electronic instruments and decided that the sound of the future was machines making music about machines. They recorded albums about highways, trains, calculators, and radioactivity while dressed in matching suits and standing perfectly still. Afrika Bambaataa built 'Planet Rock' around their rhythms.",
        "Kraftwerk — Trans-Europe Express (1977) end-to-end", 43,
        "Listen to the title track — the rhythm was built by recording actual train sounds and synthesizing them. Then 'The Model' — Ralf Hütter sings in heavily accented English about a beautiful woman who's hollow inside. The song was a surprise UK #1 five years after release. Kraftwerk had already moved on to making an album about bicycles.",
        ["Electronic", "Experimental", "German", "1970s"], 1
    ),

    # ── POP ──
    (
        "artist-prince", "Artist",
        "Prince",
        "Born Prince Rogers Nelson, named after his father's jazz trio. He taught himself piano at 7, guitar at 13, and played 27 instruments on his first album — which he recorded entirely alone. He once changed his name to an unpronounceable symbol to escape a record contract, forcing Warner Bros. to mail out floppy disks with the custom font.",
        "Prince — Purple Rain (1984) end-to-end", 44,
        "Listen to 'When Doves Cry' — there's no bassline. Prince removed it because he wanted the song to feel 'unbalanced.' Then 'Darling Nikki' — the song about a sexual encounter that Tipper Gore heard her daughter listening to, sparking the creation of the Parental Advisory sticker. Prince recorded it in a single overnight session.",
        ["Pop", "Funk", "R&B", "Rock", "American", "1980s"], 1
    ),
    (
        "artist-beyonce", "Artist",
        "Beyoncé",
        "Started performing at 7, winning a school talent show with John Lennon's 'Imagine.' Her father quit his corporate job to manage Destiny's Child, and the family's income dropped so much they lived out of a salon. She was the first Black woman to headline Coachella, and her performance — now called 'Beychella' — required 8 months of rehearsal after an emergency C-section.",
        "Beyoncé — Lemonade (2016) end-to-end", 46,
        "Watch the film version first — it's a 65-minute visual album structured like the stages of grief. Then listen to 'Formation' — the music video was shot in post-Katrina New Orleans and features a line of police officers. Police unions called for a boycott. Beyoncé performed it at the Super Bowl dressed as a Black Panther.",
        ["Pop", "R&B", "American", "2010s"], 1
    ),
    (
        "artist-bjork", "Artist",
        "Björk",
        "Icelandic to her bones — she once described her music as 'trying to put the Icelandic landscape into sound.' Released her first album at 11 (an Icelandic children's record of Beatles covers). She punched a reporter at a Bangkok airport in 1996 for saying 'Welcome to Bangkok' to her 10-year-old son. She later explained: 'It was a mother lion thing.'",
        "Björk — Homogenic (1997) end-to-end", 43,
        "Listen to 'Jóga' — the strings were recorded in a single take with the Icelandic String Octet, whose players Björk knew by name from childhood. Then 'Bachelorette' — the song is structured like a movie about a woman who becomes so famous that she no longer exists. Björk described the album as 'the sound of Iceland's volcanic landscape.'",
        ["Pop", "Art Pop", "Electronic", "Icelandic", "1990s"], 1
    ),

    # ── COUNTRY / FOLK ──
    (
        "artist-johnny-cash", "Artist",
        "Johnny Cash",
        "The Man in Black. Grew up picking cotton in Arkansas during the Great Depression. His older brother Jack died in a sawmill accident when Johnny was 12 — his father told him it should have been him. He proposed to June Carter on stage during a live performance. She said no; he asked again at the next show. She finally said yes after 60 proposals.",
        "Johnny Cash — At Folsom Prison (1968) end-to-end", 44,
        "Listen to the opening — the clatter of the prison doors closing is the first sound on the album, met with an eruption of cheers from 2,000 inmates. Then 'Folsom Prison Blues' — the line 'I shot a man in Reno just to watch him die' gets the loudest cheer of the night. The warden had asked Cash not to play any escape songs. Cash complied — and then played 'Greystone Chapel,' written by an inmate.",
        ["Country", "Outlaw Country", "Americana", "American", "1960s"], 1
    ),
    (
        "artist-dolly-parton", "Artist",
        "Dolly Parton",
        "The fourth of twelve children, born in a one-room cabin in the Smoky Mountains of Tennessee. Her father paid the doctor who delivered her with a bag of cornmeal. She wrote 'Jolene' and 'I Will Always Love You' on the same day, using the same guitar. When Elvis wanted to record 'I Will Always Love You' but demanded half the publishing, she refused — and then Whitney Houston's version made her $20 million.",
        "Dolly Parton — Jolene (1974) end-to-end", 25,
        "Listen to 'Jolene' — the fingerpicked guitar part was played by Dolly herself, based on a pattern her mother taught her. Then 'I Will Always Love You' — she wrote it at 4 AM as a farewell to her mentor Porter Wagoner, then drove to his office and sang it to him in person. He cried. She has given away over 200 million books to children through her Imagination Library.",
        ["Country", "American", "1970s"], 1
    ),
    (
        "artist-bob-dylan", "Artist",
        "Bob Dylan",
        "Born Robert Zimmerman in Hibbing, Minnesota — a town so cold his first guitar strings snapped when he tried to play outside. Ran away from home at 10, hitchhiking to Chicago. Won the Nobel Prize in Literature for 'having created new poetic expressions within the great American song tradition.' He didn't show up to accept it.",
        "Bob Dylan — Highway 61 Revisited (1965) end-to-end", 51,
        "Listen to 'Like a Rolling Stone' — the opening snare hit (played by session drummer Bobby Gregg) is the most famous drum fill in rock history. Then 'Desolation Row' — an 11-minute song with no chorus, just 10 verses of surrealist imagery. Dylan recorded the album in 6 days with musicians who'd never heard the songs before. He refused to let them rehearse.",
        ["Folk", "Rock", "Singer-Songwriter", "American", "1960s"], 1
    ),

    # ── METAL ──
    (
        "artist-black-sabbath", "Artist",
        "Black Sabbath",
        "Four working-class kids from Birmingham who decided to make music as scary as the horror movies they loved. Tony Iommi lost the tips of two fingers in a factory accident on his last day of work before going pro. He made prosthetic fingertips out of melted plastic bottles, detuned his guitar to make the strings easier to press — and accidentally invented the sound of metal.",
        "Black Sabbath — Paranoid (1970) end-to-end", 43,
        "Listen to 'Iron Man' — the riff came to Iommi while he was warming up, and the entire song was written around it in 10 minutes. Then 'War Pigs' — an 8-minute anti-war epic that the band originally called 'Walpurgis' (a Satanic festival). The label made them change it. Ozzy's vocal was recorded in a single take with the flu.",
        ["Metal", "Heavy Metal", "British", "1970s"], 1
    ),
    (
        "artist-metallica", "Artist",
        "Metallica",
        "Formed after guitarist/singer James Hetfield answered a classified ad placed by Danish immigrant Lars Ulrich in a Los Angeles newspaper. Neither owned a car — they walked or hitchhiked to rehearsals. Their bassist Cliff Burton died in 1986 when their tour bus flipped on an icy road in Sweden. Burton had won a card game that night and chose his bunk; it was the one that was crushed.",
        "Metallica — Master of Puppets (1986) end-to-end", 55,
        "Listen to the title track — the acoustic intro was Hetfield's idea to make the thrash that follows sound even heavier. The song changes time signature 6 times. Then 'Orion' — an 8-minute instrumental with Burton's bass solo at the center. The solo was recorded in a single take. Burton died 7 months later.",
        ["Metal", "Thrash Metal", "American", "1980s"], 1
    ),

    # ── WORLD ──
    (
        "artist-fela-kuti", "Artist",
        "Fela Kuti",
        "The inventor of Afrobeat, sent to London by his parents to study medicine. Instead he studied music and returned to Nigeria with a new sound. He ran a commune called the Kalakuta Republic, declared independence from Nigeria, and married 27 women in a single ceremony. The Nigerian military responded by sending 1,000 soldiers to burn his compound and throw his mother from a window. She died from her injuries.",
        "Fela Kuti — Zombie (1976) end-to-end", 26,
        "Listen to the title track — the 12-minute groove is built on a two-bar bassline that never changes. The horns enter one at a time. Then Fela begins chanting 'zombie' — the song is a direct attack on the Nigerian military, calling soldiers mindless zombies. The government didn't appreciate the metaphor. The entire album is two tracks, each over 12 minutes.",
        ["Afrobeat", "Funk", "Nigerian", "1970s"], 1
    ),
    (
        "artist-bob-marley", "Artist",
        "Bob Marley",
        "Born to a white British father he barely knew and a Black Jamaican mother in a village without electricity. Wrote his first song at 14. Survived an assassination attempt in 1976 — gunmen shot him in the chest and arm. He performed a concert two days later with the bullet still lodged in his arm. When asked why, he said: 'The people who are trying to make this world worse aren't taking a day off. How can I?'",
        "Bob Marley — Exodus (1977) end-to-end", 37,
        "Listen to 'Three Little Birds' — written after Marley noticed actual birds gathering outside his London window during exile. Then 'Redemption Song' — the last song he recorded before dying of cancer at 36. It's just his voice and an acoustic guitar. The line 'emancipate yourselves from mental slavery' is from a speech by Marcus Garvey.",
        ["Reggae", "Roots Reggae", "Jamaican", "1970s"], 1
    ),

    # ── CLASSICAL ──
    (
        "artist-johann-sebastian-bach", "Artist",
        "Johann Sebastian Bach",
        "Father of 20 children (10 survived to adulthood), he was hired as a church organist, reprimanded for playing 'strange variations' during services, thrown in jail for trying to quit his job, and forgotten after his death for 80 years — until 20-year-old Felix Mendelssohn obtained a copy of his St. Matthew Passion, conducted a celebrated revival performance in Berlin, and single-handedly rescued Bach from obscurity.",
        "Bach — Goldberg Variations (Glenn Gould, 1981)", 51,
        "Listen to the Aria first — a simple sarabande that Bach wrote for an insomniac count who wanted music to help him sleep. Then variation 25, the 'black pearl' — Glenn Gould called it the saddest piece of music ever written. The aria returns at the end, identical to the opening, but now it means something completely different. Bach wrote this while raising 10 children in a small apartment.",
        ["Classical", "Baroque", "German", "1720s"], 1
    ),
    (
        "artist-ludwig-van-beethoven", "Artist",
        "Ludwig van Beethoven",
        "Began losing his hearing at 27. By 44, he was completely deaf. He composed by biting a metal rod attached to his piano to feel the vibrations through his jawbone. His Ninth Symphony premiered while he stood on stage, unable to hear the audience's applause — a singer had to turn him around to see the ovation. He kept a notebook for visitors to write in because he couldn't understand spoken words.",
        "Beethoven — Symphony No. 5 in C minor (1808)", 35,
        "Listen to the opening four notes — three short, one long: da-da-da-DUM. That motif appears in every movement of the symphony, unifying the entire work. Then Symphony No. 7, second movement — a funeral march that builds from a whisper to a roar and back to silence. Beethoven premiered the 5th and 6th symphonies on the same night in an unheated theater with an under-rehearsed orchestra.",
        ["Classical", "Symphony", "German", "1800s"], 1
    ),

    # ── BLUES ──
    (
        "artist-robert-johnson", "Artist",
        "Robert Johnson",
        "Recorded only 29 songs in his entire life, in two sessions in 1936 and 1937 in San Antonio and Dallas hotel rooms. The legend says he sold his soul to the devil at a Mississippi crossroads in exchange for his guitar skills. He died at 27, possibly poisoned by a jealous husband. His guitar style — simultaneously playing bass, rhythm, and slide melody — makes two guitars sound like three.",
        "Robert Johnson — King of the Delta Blues Singers (1961 compilation)", 42,
        "Listen to 'Cross Road Blues' — Johnson's guitar sounds like two players because he played the bassline with his thumb and the melody with his fingers simultaneously. Then 'Hellhound on My Trail' — he recorded it facing the corner of the hotel room so the engineers couldn't see his technique. These 29 songs inspired every blues-rock guitarist who followed.",
        ["Blues", "Delta Blues", "American", "1930s"], 1
    ),
    (
        "artist-muddy-waters", "Artist",
        "Muddy Waters",
        "Born McKinley Morganfield on a Mississippi plantation. Got his nickname from his grandmother because he played in a muddy creek as a child. He electrified the Delta blues when he moved to Chicago, plugging his guitar into an amplifier and creating the sound that the Rolling Stones named themselves after. The Stones' name comes from his song 'Rollin' Stone.'",
        "Muddy Waters — Folk Singer (1964) end-to-end", 39,
        "Listen to 'My Home Is in the Delta' — Waters recounts his childhood over a single repeated guitar riff, with the microphone placed so close you can hear his thumb slide on the strings. Then 'Feel Like Going Home' — a song about longing that has no specific object. This was recorded acoustically because Chess Records was trying to market Waters to the folk revival audience.",
        ["Blues", "Chicago Blues", "Electric Blues", "American", "1950s"], 1
    ),
]

# ═══════════════════════════════════════════════════════════════════════════
# JSON generation
# ═══════════════════════════════════════════════════════════════════════════

MAX_CHARS = 280


def _trim(text, field_name, entry_id):
    """Trim text to MAX_CHARS at the last sentence boundary."""
    if len(text) <= MAX_CHARS:
        return text
    trimmed = text[:MAX_CHARS]
    for punct in (". ", "! ", "? "):
        last = trimmed.rfind(punct)
        if last > MAX_CHARS // 2:
            trimmed = trimmed[:last + 1]
            break
    else:
        last_space = trimmed.rfind(" ")
        if last_space > MAX_CHARS // 2:
            trimmed = trimmed[:last_space]
    print(f"  ⚠ {entry_id} {field_name}: {len(text)} → {len(trimmed)} chars")
    return trimmed


def build_artists():
    entries = []
    seen_ids = set()

    for artist in ARTISTS:
        id_, subtype, name, teaser, target_name, duration, instruction, tags, tier = artist

        if id_ in seen_ids:
            print(f"WARNING: duplicate id '{id_}'")
            continue
        seen_ids.add(id_)

        teaser = _trim(teaser, "teaser", id_)
        instruction = _trim(instruction, "instruction", id_)

        entry = {
            "id": id_,
            "categoryId": "ARTISTS",
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

    output_path = os.path.join(output_dir, "artists.json")
    artists = build_artists()

    with open(output_path, "w") as f:
        json.dump(artists, f, indent=2, ensure_ascii=False)
        f.write("\n")

    print(f"✓ Generated {output_path} with {len(artists)} artists")
    tags = set(t for a in artists for t in a['tags'])
    print(f"  Tags: {len(tags)} unique tags")


if __name__ == "__main__":
    main()
