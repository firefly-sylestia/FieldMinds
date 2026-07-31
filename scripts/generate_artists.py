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

    # ═══════════════════════════════════════════════════════════════════════
    # BATCH 2 — fun & indie artists (fun facts, quirky catalog)
    # ═══════════════════════════════════════════════════════════════════════

    # ── FUN / QUIRKY ──
    (
        "artist-they-might-be-giants", "Artist",
        "They Might Be Giants",
        "Named after a 1971 George C. Scott film about a man who thinks he's Sherlock Holmes. They ran Dial-A-Song — a phone line you could call for a new song — and played 52 shows in 52 states in 52 days. 'Istanbul (Not Constantinople),' their biggest hit, is a cover of a 1953 song.",
        "They Might Be Giants — Flood (1990) end-to-end", 41,
        "Listen to 'Birdhouse in Your Soul' — it's sung from the perspective of a canary nightlight in a blue house. Then 'Istanbul (Not Constantinople)' and count the history facts crammed into three minutes. Linnell and Flansburgh met in elementary school and still record in Brooklyn.",
        ["Alternative Rock", "Indie Pop", "Comedy", "American", "1990s"], 1
    ),
    (
        "artist-weird-al", "Artist",
        "\"Weird Al\" Yankovic",
        "The best-selling comedy artist of all time, and a genuine accordion virtuoso — his parents bought his first accordion from a door-to-door salesman for $40. He studied architecture at Cal Poly. An asteroid, 28403 Weird Al, is named after him. He's also an Eagle Scout.",
        "Weird Al — The Food Album (1993) end-to-end", 55,
        "Play 'Amish Paradise' against Coolio's 'Gangsta's Paradise' — Coolio was furious for years before they made up. Then 'White & Nerdy' — he got Donny Osmond in the video. Artists literally send Al their songs hoping to be parodied; it's an honor.",
        ["Comedy", "Pop", "Parody", "American", "1980s"], 1
    ),
    (
        "artist-ok-go", "Artist",
        "OK Go",
        "'Here It Goes Again' — the famous treadmill video — was one continuous take in the band's backyard on four treadmills. It won a Grammy and launched a series of choreographed one-take videos with drones, cars, and a giant Rube Goldberg machine.",
        "OK Go — Oh No (2005) end-to-end", 36,
        "Watch 'This Too Shall Pass' (the Rube Goldberg version) — the machine took two months to build and dozens of takes. Then 'The One Moment' — the whole video is four real seconds slowed down. OK Go's videos are engineering projects as much as songs.",
        ["Indie Rock", "Alternative Rock", "American", "2000s"], 2
    ),
    (
        "artist-b52s", "Artist",
        "The B-52s",
        "Named after the beehive hairdo. They formed in Athens, Georgia after a night of flaming-volcano drinks at a Chinese restaurant, then wrote 'Rock Lobster' in a living room hours later. John Lennon heard it in a club and it inspired him to come back to music after five years away.",
        "The B-52s — Cosmic Thing (1989) end-to-end", 43,
        "Listen to 'Rock Lobster' and identify every animal sound in the bridge. Then 'Love Shack' — it's about a real shack in Athens, Georgia where the band partied. Fred Schneider's deadpan vocals are the whole aesthetic: no one else would dare.",
        ["New Wave", "Pop Rock", "American", "1980s"], 2
    ),
    (
        "artist-devo", "Artist",
        "Devo",
        "Art students from Kent State, on campus in 1970 when the National Guard shot four students — the experience shaped their theory that humans are 'de-evolving.' Their red 'energy dome' hats were made from plastic flowerpots, and they weaponized irony.",
        "Devo — Q: Are We Not Men? A: We Are Devo! (1978) end-to-end", 34,
        "Listen to 'Whip It' with the video — the song was nearly left off the album. Then 'Beautiful World' — the cheery melody over apocalyptic lyrics is the entire Devo trick. Mark Mothersbaugh later scored Rugrats, The Royal Tenenbaums, and every Wes Anderson film's trailer.",
        ["New Wave", "Synth-Pop", "Punk", "American", "1970s"], 2
    ),
    (
        "artist-talking-heads", "Artist",
        "Talking Heads",
        "Formed at the Rhode Island School of Design. David Byrne's jerky dance in 'Once in a Lifetime' was improvised. Stop Making Sense — directed by Jonathan Demme — is often called the greatest concert film ever made, and the 'big suit' Byrne wears was entirely his own idea.",
        "Talking Heads — Stop Making Sense (1984) end-to-end", 78,
        "Watch 'Once in a Lifetime' — Byrne says the lyrics came from words that 'felt good in the mouth.' Then 'Psycho Killer' — the French verse is about a paranoid killer. Bassist Tina Weymouth learned to play bass just weeks before the band's first show.",
        ["New Wave", "Art Rock", "American", "1980s"], 1
    ),
    (
        "artist-tenacious-d", "Artist",
        "Tenacious D",
        "Jack Black and Kyle Gass met at an acting conservatory in LA. Their first gig was at a coffee shop where they screamed 'Fuck Her Gently' at six people. 'Tribute' — their signature song — is a tribute to 'the greatest song in the world,' which they claim to have forgotten.",
        "Tenacious D — Tenacious D (2001) end-to-end", 47,
        "Listen to 'Tribute' — legend says Kyle once quit the band, and Jack wrote it to win him back. Then 'Kielbasa Sausage' — the very first song they ever wrote together. Their HBO show won an Emmy, and every D song is a miniature rock opera with a punchline.",
        ["Comedy", "Hard Rock", "American", "2000s"], 2
    ),
    (
        "artist-flight-of-the-conchords", "Artist",
        "Flight of the Conchords",
        "Bret and Jemaine met at university in Wellington, NZ. Their hit HBO show grew out of a BBC radio series. Bret won an Oscar for writing 'Man or Muppet' — and the Conchords are famous for deadpan harmonies about business time and the Hiphopopotamus vs. the Rhymenoceros.",
        "Flight of the Conchords — Flight of the Conchords (2008) end-to-end", 45,
        "Listen to 'Hiphopopotamus vs. Rhymenoceros' — a battle rap between two rappers too shy to insult each other. Then 'The Most Beautiful Girl (In the Room)' — Jemaine's accidental love song. Most of their songs were written in a single night, and it shows in the best way.",
        ["Comedy", "Folk Pop", "New Zealand", "2000s"], 2
    ),
    (
        "artist-presidents-of-usa", "Artist",
        "The Presidents of the United States of America",
        "Chris Ballew plays the 'basitar' — a three-string bass tuned like a guitar, built from a salvaged guitar neck. 'Lump' became one of the most-played songs of the 90s, and the band's huge debut album was recorded on a tiny budget with absurdly short songs about peaches and candy.",
        "The Presidents of the United States of America — debut (1995) end-to-end", 37,
        "Listen to 'Lump' — the nonsense lyrics were improvised in an afternoon. Then 'Peaches' — the bassline lives on the three-string basitar, tuned like a guitar so it feels upside down. 'Kitty' — the song about a cat who moves out. The band is named after the presidents.",
        ["Alternative Rock", "Comedy", "American", "1990s"], 2
    ),
    (
        "artist-lemon-demon", "Artist",
        "Lemon Demon",
        "Neil Cicierega's flash-era hit 'The Ultimate Showdown of Ultimate Destiny' — Abraham Lincoln vs. Chuck Norris vs. Mr. Rogers — introduced millions to internet music. He also made the Potter Puppet Pals videos and 'Mouth Sounds,' an album of surreal pop mashups.",
        "Lemon Demon — Spirit Phone (2016) end-to-end", 48,
        "Listen to 'The Ultimate Showdown of Ultimate Destiny' and count the celebrities who die. Then 'Two Trucks' — a romantic ballad about trucks, somehow. His 'Mouth' albums mash dozens of pop songs into impossible new ones. Every Lemon Demon song is a cartoon in audio form.",
        ["Comedy", "Electronic", "Indie Pop", "American", "2000s"], 3
    ),
    (
        "artist-tally-hall", "Artist",
        "Tally Hall",
        "Five guys from Michigan who wear matching colored ties — red, yellow, blue, green, and gray. Their debut album is named after a real arcade-oddities museum. 'Ruler of Everything' went viral on TikTok in 2020, a full decade after they made it.",
        "Tally Hall — Marvin's Marvelous Mechanical Museum (2005) end-to-end", 52,
        "Listen to 'Ruler of Everything' — the song flips between two voices and accelerates into cheerful madness. Then 'Good Day' — genuinely the happiest song about a terrible day. The band calls their sound 'fabloo,' a made-up word, and they met in a college a cappella group.",
        ["Indie Pop", "Alternative Rock", "Comedy", "American", "2000s"], 3
    ),
    (
        "artist-jack-stauber", "Artist",
        "Jack Stauber",
        "Makes lo-fi synth-pop with stop-motion claymation videos he animates himself. 'Buttercup' became a TikTok phenomenon in 2020. His surreal animated musical 'Opal' (2019) packs a whole nightmare into eight minutes, and he records everything on deliberately degraded gear.",
        "Jack Stauber — HiLo (2018) end-to-end", 30,
        "Listen to 'Buttercup' — the song about wanting someone's affection so badly it hurts. Then watch 'Opal' — his animated short about a girl in a house with a lamp. 'Fighter' — the upbeat song that's secretly devastating. His music sounds like a broken TV that makes you cry.",
        ["Indie Pop", "Experimental", "Lo-Fi", "American", "2010s"], 3
    ),
    (
        "artist-lonely-island", "Artist",
        "The Lonely Island",
        "Andy Samberg, Akiva Schaffer, and Jorma Taccone turned Saturday Night Live's Digital Shorts into a comedy-music empire. 'Dick in a Box' won an Emmy for Outstanding Original Music and Lyrics, and 'I'm on a Boat' with T-Pain earned a Grammy nomination for a song about a boat.",
        "The Lonely Island — Turtleneck & Chain (2011) end-to-end", 44,
        "Listen to 'I'm on a Boat' and remember it's unironically great. Then 'Jizz in My Pants' — a four-minute joke with a real music video budget. The three met in high school in Berkeley, California, and their songs are sketches with genuine beats underneath.",
        ["Comedy", "Hip-Hop", "American", "2010s"], 2
    ),
    (
        "artist-sparks", "Artist",
        "Sparks",
        "Brothers Ron and Russell Mael have released records in six consecutive decades — a feat almost no one has matched. 'This Town Ain't Big Enough for Both of Us' made them heroes to every new-wave kid who followed, and they wrote the entire 2021 film 'Annette' starring Adam Driver.",
        "Sparks — Kimono My House (1974) end-to-end", 36,
        "Listen to 'This Town Ain't Big Enough for Both of Us' — the operatic falsetto over a crashing band. Then 'The Number One Song in Heaven' — their Giorgio Moroder collaboration. Ron's mustache hasn't changed since 1971, and they were the template for every art-pop band since.",
        ["Art Pop", "Glam Rock", "New Wave", "American", "1970s"], 2
    ),

    # ── INDIE ROCK ──
    (
        "artist-strokes", "Artist",
        "The Strokes",
        "Julian Casablancas is the son of the founder of the Elite modeling agency. The US cover of 'Is This It' — a buttock in a latex glove — was swapped after 9/11. The band met at a Swiss boarding school and a New York music scene, and 'Last Nite' launched the 2000s NYC rock revival.",
        "The Strokes — Is This It (2001) end-to-end", 36,
        "Listen to 'Last Nite' — the riff is basically Tom Petty's 'American Girl' sped up, and Petty was a fan. Then 'Reptilia' — Julian's vocal melody was improvised in the booth. The album was recorded in a tiny Manhattan studio in about three weeks, and it saved rock radio.",
        ["Indie Rock", "Garage Rock Revival", "American", "2000s"], 1
    ),
    (
        "artist-arctic-monkeys", "Artist",
        "Arctic Monkeys",
        "'Whatever People Say I Am, That's What I'm Not' became the fastest-selling debut album in UK history, shifting 363,000 copies in its first week. They got famous by giving away free demo CDs at Sheffield gigs, which fans ripped and spread online. Alex Turner was 19.",
        "Arctic Monkeys — Whatever People Say I Am (2006) end-to-end", 41,
        "Listen to 'A Certain Romance' — the album's wise closer about nights out. Then 'Fluorescent Adolescent' — lyrics written about losing your youth. 'Do I Wanna Know?' — the drum intro that defined the 2010s. The debut is basically a diary of being young in Sheffield.",
        ["Indie Rock", "British", "2000s"], 1
    ),
    (
        "artist-arcade-fire", "Artist",
        "Arcade Fire",
        "'Funeral' is named after the funerals of several band members' grandparents during recording. Win Butler and Régine Chassagne met at McGill University in Montreal, and Win's grandfather was Alvino Rey, a pioneering electric guitarist. 'Wake Up' became a stadium anthem.",
        "Arcade Fire — Funeral (2004) end-to-end", 48,
        "Listen to 'Neighborhood #1 (Tunnels)' — the album opens with two people in a snowstorm. Then 'Wake Up' — the choir-style anthem sports arenas adopted. 'Funeral' was recorded in a Montreal loft with a hurdy-gurdy, a glockenspiel, and a lot of grief.",
        ["Indie Rock", "Art Rock", "Canadian", "2000s"], 1
    ),
    (
        "artist-shins", "Artist",
        "The Shins",
        "In the movie Garden State, Natalie Portman's character says 'New Slang' will 'change your life' — it did, for a generation of indie kids. James Mercer recorded the debut in a bedroom in Albuquerque on a broken four-track, vocals at 3 AM so the neighbors wouldn't hear.",
        "The Shins — Oh, Inverted World (2001) end-to-end", 36,
        "Listen to 'New Slang' first — it's the song that made a million mixtapes. Then 'Phantom Limb' — the best of their pop-perfect second era. Mercer wrote most of the debut alone in his bedroom; the hiss on the recordings is part of the charm.",
        ["Indie Rock", "Indie Pop", "American", "2000s"], 1
    ),
    (
        "artist-modest-mouse", "Artist",
        "Modest Mouse",
        "The name comes from a Virginia Woolf short story. 'Float On' — their biggest hit — is a cheerful anthem written the year their drummer died. They recorded 'The Moon & Antarctica' in a cabin; Brock's lyrics read like nervous breakdowns you can dance to.",
        "Modest Mouse — The Moon & Antarctica (2000) end-to-end", 60,
        "Listen to 'Float On' — the chorus is a survival mantra: 'we'll all float on okay.' Then 'The Good Times Are Killing Me' — a party song that's secretly about death. '3rd Planet' — the opener that sets the album's lonely tone.",
        ["Indie Rock", "Alternative Rock", "American", "2000s"], 1
    ),
    (
        "artist-death-cab-for-cutie", "Artist",
        "Death Cab for Cutie",
        "Named after a Bonzo Dog Doo-Dah Band song from the Beatles' film Magical Mystery Tour. Ben Gibbard wrote most of 'Transatlanticism' during a long-distance relationship, and the title track is about that distance. The band became the biggest indie act of the 2000s.",
        "Death Cab for Cutie — Transatlanticism (2003) end-to-end", 44,
        "Listen to 'Transatlanticism' — the title track builds for seven minutes into a wall of sound. Then 'I Will Follow You into the Dark' — the song Gibbard wrote in one sitting, about following someone anywhere. His other project, The Postal Service, shares the same voice.",
        ["Indie Rock", "Indie Pop", "American", "2000s"], 1
    ),
    (
        "artist-postal-service", "Artist",
        "The Postal Service",
        "Ben Gibbard and Jimmy Tamborello recorded their album by mailing hard drives between LA and Seattle — that's the name. They didn't tour for a decade, and 'Such Great Heights' became an anthem anyway. 'Give Up' went platinum ten years after release.",
        "The Postal Service — Give Up (2003) end-to-end", 45,
        "Listen to 'Such Great Heights' — the melody that launched a thousand weddings and movie scenes. Then 'The District Sleeps Alone Tonight' — the song about arriving somewhere alone. Tamborello's drum machines are the whole heartbeat of the record.",
        ["Indie Pop", "Electronic", "American", "2000s"], 2
    ),
    (
        "artist-wilco", "Artist",
        "Wilco",
        "Their masterpiece 'Yankee Hotel Foxtrot' was rejected by their own label, who called it 'career suicide.' Wilco bought the master tapes back for $50,000 and streamed the album free on their website — it became their best-seller and one of the most acclaimed albums of the century.",
        "Wilco — Yankee Hotel Foxtrot (2002) end-to-end", 52,
        "Listen to 'I Am Trying to Break Your Heart' — the opening track was assembled from random tape experiments. Then 'Jesus, Etc.' — the lap steel lullaby. The album was made while the band was imploding, and the chaos is right there in the music.",
        ["Alternative Rock", "Indie Rock", "American", "2000s"], 2
    ),
    (
        "artist-decemberists", "Artist",
        "The Decemberists",
        "'The Mariner's Revenge Song' is an 8-minute murder ballad that ends with the narrator getting swallowed by a whale — along with the mother of the man who ruined his life. Colin Meloy studied creative writing, and it shows: the band's songs are literary short stories with hooks.",
        "The Decemberists — Picaresque (2005) end-to-end", 50,
        "Listen to 'The Mariner's Revenge Song' all the way through — it's a complete story with a twist ending. Then '16 Military Wives' — the marching-band satire. 'O Valencia!' — a modern Romeo and Juliet set to accordion. No other band writes songs like a Victorian novel.",
        ["Indie Rock", "Folk Rock", "American", "2000s"], 2
    ),
    (
        "artist-vampire-weekend", "Artist",
        "Vampire Weekend",
        "The members met at Columbia University. 'Oxford Comma' is literally named after the punctuation mark, written while Ezra Koenig was being pressured to use one. The band name comes from a short film Ezra made, and 'A-Punk' brought Afro-pop guitars to indie radio.",
        "Vampire Weekend — Vampire Weekend (2008) end-to-end", 34,
        "Listen to 'Oxford Comma' and appreciate a song about grammar being a hit. Then 'Cape Cod Kwassa Kwassa' — the title translates to a Congolese dance. 'Harmony Hall' — the 2019 comeback about how growing up is a trap. The first album was recorded in a cabin in Vermont.",
        ["Indie Rock", "Indie Pop", "American", "2000s"], 1
    ),
    (
        "artist-tame-impala", "Artist",
        "Tame Impala",
        "Kevin Parker is the entire band — he writes, records, plays every instrument, and produces alone in his home studio. His drums are famously 'wrong' sounding on purpose. He also drums for the band Pond. 'Currents' was his breakup masterpiece and the sound of a new decade.",
        "Tame Impala — Currents (2015) end-to-end", 51,
        "Listen to 'Let It Happen' — the seven-minute song where the music literally glitches and reassembles. Then 'The Less I Know the Better' — the funk single about betrayal. 'Eventually' — the mantra that 'it's okay to be sad.' Parker recorded the whole album alone in Fremantle.",
        ["Psychedelic Rock", "Synth-Pop", "Australian", "2010s"], 1
    ),
    (
        "artist-mgmt", "Artist",
        "MGMT",
        "Andrew VanWyngarden and Ben Goldwasser met at Wesleyan's electronic music lab. 'Time to Pretend' — their breakthrough — is a satirical fantasy about rock-star excess, written as a joke. 'Kids' came from a toy keyboard, and they were uncomfortable with the fame it brought.",
        "MGMT — Oracular Spectacular (2007) end-to-end", 40,
        "Listen to 'Kids' — the riff came from a cheap keyboard preset. Then 'Electric Feel' — the bass-heavy funk cut. 'Congratulations' — the follow-up where they refused to repeat the formula and their label panicked. They've spent their career running from their own hits.",
        ["Indie Rock", "Synth-Pop", "Psychedelic Pop", "American", "2000s"], 1
    ),
    (
        "artist-phoenix", "Artist",
        "Phoenix",
        "Four childhood friends from Versailles, France, who played their first show at 13. Singer Thomas Mars is married to director Sofia Coppola. '1901' — their US breakthrough — has a chorus that made every indie band want a Moog. 'Wolfgang Amadeus Phoenix' won a Grammy.",
        "Phoenix — Wolfgang Amadeus Phoenix (2009) end-to-end", 37,
        "Listen to '1901' — the bass and the Moog synth line lock into pure joy. Then 'Lisztomania' — the song named after a 1975 film about Liszt. 'Long Distance Call' — the ballad that sneaks up on you. French pop filtered through 80s synths and a lot of confidence.",
        ["Indie Rock", "Synth-Pop", "French", "2000s"], 1
    ),
    (
        "artist-yeah-yeah-yeahs", "Artist",
        "Yeah Yeah Yeahs",
        "Karen O was born in Busan, South Korea, and raised in New Jersey. 'Maps' is an acronym — 'My Angus Please Stay' — written for her boyfriend who was leaving on tour. Her stage outfits, including a baby-doll nightgown, became part of the legend.",
        "Yeah Yeah Yeahs — Fever to Tell (2003) end-to-end", 40,
        "Listen to 'Maps' — the song that made everyone cry at festivals. Then 'Heads Will Roll' — the disco-punk anthem that took over clubs. 'Zero' — the glam opener. Karen O's live shows are part sermon, part exorcism, all outfit.",
        ["Indie Rock", "Art Punk", "American", "2000s"], 1
    ),
    (
        "artist-interpol", "Artist",
        "Interpol",
        "One of the bands that revived New York rock in the early 2000s. Their debut 'Turn On the Bright Lights' was recorded in about a week. Paul Banks' deep baritone and Carlos Dengler's basslines defined the sound, and 'Evil' became their biggest song by accident.",
        "Interpol — Turn On the Bright Lights (2002) end-to-end", 49,
        "Listen to 'Obstacle 1' — the bassline that started a thousand imitators. Then 'Evil' — the singalong about a girl with 'a thousand and one'. 'PDA' — the song with the legendary outro. The album's opening track, 'Untitled', is still the best way to start a record.",
        ["Indie Rock", "Post-Punk Revival", "American", "2000s"], 2
    ),
    (
        "artist-national", "Artist",
        "The National",
        "Matt Berninger's deep, conversational baritone is the band's signature. The five members are friends from Cincinnati who formed the band in Brooklyn. Aaron Dessner has also produced albums for Taylor Swift, and 'Bloodbuzz Ohio' is about homesickness and debt.",
        "The National — High Violet (2010) end-to-end", 47,
        "Listen to 'About Today' — the quiet four-minute song that builds to a wall of sound. Then 'Mr. November' — the song where Matt screams 'I won't fuck us over.' Their lyrics read like overheard therapy sessions set to strings.",
        ["Indie Rock", "Art Rock", "American", "2010s"], 2
    ),
    (
        "artist-king-gizzard", "Artist",
        "King Gizzard & the Lizard Wizard",
        "The most prolific band alive — 25+ albums since 2012, including five in a single year. 'Polygondwanaland' was given away free, with an invitation for fans to press their own vinyl. Their microtonal album 'Flying Microtonal Banana' used custom-built quarter-tone guitars.",
        "King Gizzard — Flying Microtonal Banana (2017) end-to-end", 42,
        "Listen to 'Polygondwanaland' — the album is one continuous suite with a theme about a utopia. Then 'Rattlesnake' — the hypnotic opener that goes on forever and never gets old. The band is from Melbourne and their live shows are relentless, rotating drummers and all.",
        ["Psychedelic Rock", "Garage Rock", "Australian", "2010s"], 2
    ),
    (
        "artist-foster-the-people", "Artist",
        "Foster the People",
        "Mark Foster was a commercial jingle writer before forming the band. 'Pumped Up Kicks' — a song about a school shooting — was written in about 20 minutes and became one of the biggest indie-pop hits of the decade despite its subject matter. The sunny sound is the point.",
        "Foster the People — Torches (2011) end-to-end", 47,
        "Listen to 'Pumped Up Kicks' twice — once for the melody, once for the story. Then 'Houdini' — the song about escaping. 'Best Friend' — the weird synth-pop cut. The contrast between the bouncy production and the dark lyrics is the whole project.",
        ["Indie Pop", "Alternative Rock", "American", "2010s"], 2
    ),

    # ── INDIE FOLK / SINGER-SONGWRITER ──
    (
        "artist-bon-iver", "Artist",
        "Bon Iver",
        "Justin Vernon recorded 'For Emma, Forever Ago' alone in his father's hunting cabin in Wisconsin over a snowy winter, surviving on deer meat and a wood stove. 'Bon Iver' is misspelled French for 'good winter.' The self-released album became a worldwide phenomenon.",
        "Bon Iver — For Emma, Forever Ago (2007) end-to-end", 37,
        "Listen to 'Re: Stacks' — the album's quiet closer, recorded by a fire. Then 'Holocene' — the song about a moment of feeling tiny and okay with it. 'Skinny Love' — the song that launched a thousand covers. Everything was recorded on a four-track in a cabin with no running water.",
        ["Indie Folk", "Singer-Songwriter", "American", "2000s"], 1
    ),
    (
        "artist-fleet-foxes", "Artist",
        "Fleet Foxes",
        "The band's layered vocal harmonies sound like a forest choir. 'White Winter Hymnal' — their signature song — has at least six voices stacked. Robin Pecknold wrote 'Helplessness Blues' about feeling lost in your twenties, and the debut album was recorded in a Seattle basement.",
        "Fleet Foxes — Fleet Foxes (2008) end-to-end", 39,
        "Listen to 'White Winter Hymnal' and try to separate the voices — they're stacked like a Renaissance choir. Then 'Mykonos' — the song that shifts halfway. 'Helplessness Blues' — the title track about wanting to be anything but yourself. Sun Giant was recorded in a garage.",
        ["Indie Folk", "Folk Rock", "American", "2000s"], 1
    ),
    (
        "artist-sufjan-stevens", "Artist",
        "Sufjan Stevens",
        "He once announced a plan to make an album for all 50 US states — he got through two (Michigan and Illinois) before giving up. 'Illinois' is packed with state trivia, and 'Casimir Pulaski Day' is about a friend who died of bone cancer. He plays 20+ instruments.",
        "Sufjan Stevens — Illinois (2005) end-to-end", 74,
        "Listen to 'Concerning the UFO Sighting Near Highland, Illinois' — an album opener about aliens. Then 'Casimir Pulaski Day' — the saddest banjo song ever written. 'Chicago' — the anthem about moving on. He recorded 'Illinois' with a 40-piece orchestra in a church.",
        ["Indie Folk", "Singer-Songwriter", "Baroque Pop", "American", "2000s"], 1
    ),
    (
        "artist-elliott-smith", "Artist",
        "Elliott Smith",
        "'Miss Misery' — from the film Good Will Hunting — was nominated for an Oscar, and Smith performed it at the 1998 ceremony in a white suit, looking like he'd rather be anywhere else. He recorded 'Either/Or' in a tiny studio, often keeping imperfect live takes on purpose.",
        "Elliott Smith — Either/Or (1997) end-to-end", 37,
        "Listen to 'Angeles' — one of the most beautiful fingerpicking patterns ever recorded. Then 'Between the Bars' — the lullaby about drinking. 'Say Yes' — the gentle closer that sounds like hope. His songs feel like secrets told to a friend.",
        ["Indie Folk", "Singer-Songwriter", "Lo-Fi", "American", "1990s"], 1
    ),
    (
        "artist-neutral-milk-hotel", "Artist",
        "Neutral Milk Hotel",
        "'In the Aeroplane Over the Sea' is a surreal concept album that references Anne Frank, recorded by Jeff Mangum and his Elephant 6 friends. After touring, Mangum vanished from music for 15 years, then returned for reunion shows. The cover is an old anonymous family photo.",
        "Neutral Milk Hotel — In the Aeroplane Over the Sea (1998) end-to-end", 40,
        "Listen to 'Holland, 1945' — the song that ties the album to Anne Frank. Then 'Two-Headed Boy' — the emotional core. 'The King of Carrot Flowers' — the fever-dream opener. The album was recorded in a Denver house with friends playing horns and saws.",
        ["Indie Folk", "Psychedelic Folk", "Lo-Fi", "American", "1990s"], 1
    ),
    (
        "artist-bright-eyes", "Artist",
        "Bright Eyes",
        "Conor Oberst released his first recording at 13 and ran the Omaha indie scene by 18. 'First Day of My Life' is his rare unguarded love song, and 'Lua' is a hushed song about addiction. He was called 'the next Bob Dylan' before he was 20, which he hated.",
        "Bright Eyes — I'm Wide Awake, It's Morning (2005) end-to-end", 42,
        "Listen to 'Lua' — four minutes of voice and guitar recorded almost live. Then 'First Day of My Life' — the wedding song that isn't for anyone in particular. 'Four Winds' — the folk-rock stomp. Oberst's songs are confessional in a way that made everyone else sound fake.",
        ["Indie Folk", "Singer-Songwriter", "American", "2000s"], 2
    ),
    (
        "artist-mountain-goats", "Artist",
        "The Mountain Goats",
        "John Darnielle recorded his early albums on a $200 Panasonic boombox, sometimes writing four songs a day. He worked as a psychiatric nurse for years before music paid. 'No Children' — a duet about mutual hatred — is somehow the band's most beloved song.",
        "The Mountain Goats — Tallahassee (2002) end-to-end", 43,
        "Listen to 'No Children' and try not to sing 'I hope you die' along — impossible. Then 'This Year' — the anthem that says 'I am gonna make it through this year if it kills me.' 'The Best Ever Death Metal Band in Denton' — the song about two kids who form a band against the world.",
        ["Indie Folk", "Singer-Songwriter", "Lo-Fi", "American", "2000s"], 2
    ),
    (
        "artist-daniel-johnston", "Artist",
        "Daniel Johnston",
        "The lo-fi legend who recorded on a boombox at his parents' house in West Virginia. Kurt Cobain wore a t-shirt with Johnston's 'Hi, How Are You' frog drawing. 'True Love Will Find You in the End' became a mantra for the heartbroken. He died in 2019.",
        "Daniel Johnston — Hi, How Are You (1983) end-to-end", 34,
        "Listen to 'True Love Will Find You in the End' — a two-minute promise. Then 'Speeding Motorcycle' — covered by everyone from Yo La Tengo to Mary Lou Lord. 'Walking the Cow' — the anthem of the restless. The documentary 'The Devil and Daniel Johnston' is essential viewing.",
        ["Lo-Fi", "Singer-Songwriter", "Outsider Music", "American", "1980s"], 2
    ),
    (
        "artist-mac-demarco", "Artist",
        "Mac DeMarco",
        "'Ode to Viceroy' is literally a love song to his favorite cigarette brand. He's known for goofy stage antics, a permanently laid-back vibe, and recording at home — '2' was made in a friend's living room on a four-track. He was born in British Columbia and raised in Edmonton.",
        "Mac DeMarco — Salad Days (2014) end-to-end", 35,
        "Listen to 'My Kind of Woman' — the slow-dance love song. Then 'Chamber of Reflection' — the synth ballad based on a 70s Japanese TV theme. 'Salad Days' — the title track about getting old at 23. His concerts end with kids getting pulled on stage.",
        ["Indie Rock", "Lo-Fi", "Jangle Pop", "Canadian", "2010s"], 2
    ),
    (
        "artist-father-john-misty", "Artist",
        "Father John Misty",
        "Josh Tillman drummed for Fleet Foxes for four years before going solo under a new name. 'I Love You, Honeybear' is an album-long love letter to his wife Emma, and 'Real Love Baby' is the rare straightforward pop song. His stage persona is a parody of a folk singer.",
        "Father John Misty — I Love You, Honeybear (2015) end-to-end", 45,
        "Listen to 'Hollywood Forever Cemetery Sings' — a love song set in a graveyard. Then 'I Love You, Honeybear' — the title track, the most romantic thing he's ever recorded. 'Real Love Baby' — three minutes of pure pop. His shows feature long, hilarious monologues.",
        ["Indie Folk", "Singer-Songwriter", "American", "2010s"], 2
    ),

    # ── BEDROOM POP / NEW INDIE ──
    (
        "artist-phoebe-bridgers", "Artist",
        "Phoebe Bridgers",
        "'Motion Sickness' — her breakthrough — is a song about her relationship with Ryan Adams, whom she opened for in her early 20s. She founded the label Saddest Factory Records, and 'Kyoto' is a song about her dad in Japan written from a tour bus. She's one third of boygenius.",
        "Phoebe Bridgers — Punisher (2020) end-to-end", 41,
        "Listen to 'Motion Sickness' — the song that made everyone in indie music gasp. Then 'Kyoto' — the trumpet-filled song about being on tour while your dad is far away. 'I Know the End' — the album closer that builds into a full scream. 'Scott Street' — the song about old friends.",
        ["Indie Rock", "Singer-Songwriter", "American", "2010s"], 1
    ),
    (
        "artist-mitski", "Artist",
        "Mitski",
        "Born in Japan and raised across Malaysia, Turkey, and the US, Mitski studied at a music conservatory in New York. 'Nobody' took four years to write and became her viral breakout. 'Be the Cowboy' imagines a cowboy persona. Her lyrics are precise and devastating.",
        "Mitski — Be the Cowboy (2018) end-to-end", 33,
        "Listen to 'First Love / Late Spring' — the wailing climax that made her a legend. Then 'Nobody' — the song about wanting to be wanted. 'Washing Machine Heart' — the one that went viral on TikTok. 'Last Words of a Shooting Star' — the closer about dying in a plane crash.",
        ["Indie Rock", "Art Pop", "American", "2010s"], 1
    ),
    (
        "artist-clairo", "Artist",
        "Clairo",
        "'Pretty Girl' was recorded in GarageBand in her bedroom at 16 and posted with a webcam video — it blew up overnight and defined bedroom pop. Her real name is Claire Cottrill. 'Bags' and 'Sofia' showed she was a real songwriter, not a meme.",
        "Clairo — Immunity (2019) end-to-end", 43,
        "Listen to 'Pretty Girl' — the raw bedroom recording that started everything. Then 'Bags' — the song about unspoken feelings between friends. 'Sofia' — the queer love song that became a singalong. She made 'Immunity' with producer Jack Antonoff, but the honesty is all hers.",
        ["Bedroom Pop", "Indie Pop", "American", "2010s"], 2
    ),
    (
        "artist-snail-mail", "Artist",
        "Snail Mail",
        "Lindsey Jordan signed to indie giant Matador Records at 17 while still in high school in Maryland. Her debut album 'Lush' — recorded at 18 — captures teenage heartbreak with the force of a grown-up rock band. She taught herself guitar from YouTube.",
        "Snail Mail — Lush (2018) end-to-end", 39,
        "Listen to 'Pristine' — the album's opener about loving someone completely. Then 'Heat Wave' — the single with the huge chorus. 'Forever (Sailing)' — the emotional closer. Jordan's songs sound like diary entries recorded through a cranked guitar amp.",
        ["Indie Rock", "Bedroom Pop", "American", "2010s"], 2
    ),
    (
        "artist-soccer-mommy", "Artist",
        "Soccer Mommy",
        "Sophie Allison started posting home recordings to Bandcamp from her Nashville bedroom. 'Your Dog' — her breakout single — is a snarling anti-possession song. Her songs sound like 90s alt-rock filtered through a laptop, which is exactly the point.",
        "Soccer Mommy — Clean (2018) end-to-end", 40,
        "Listen to 'Your Dog' — the chorus is a rejection of being treated like a pet. Then 'Bloodstream' — the dreamy single. 'Circle the Drain' — the grunge-tinged closer. She recorded 'Color Theory' — her second album — during the pandemic, which you can hear in its darkness.",
        ["Bedroom Pop", "Indie Rock", "American", "2010s"], 3
    ),
    (
        "artist-boygenius", "Artist",
        "boygenius",
        "The supergroup of Phoebe Bridgers, Julien Baker, and Lucy Dacus — three of the most celebrated indie songwriters of their generation. Their debut EP was recorded in four days and named after a joke in a group chat. Their first full album 'the record' topped charts worldwide.",
        "boygenius — the record (2023) end-to-end", 42,
        "Listen to 'Me & My Dog' — the song about co-dependence. Then 'Not Strong Enough' — the song where all three trade lines about depression. 'Cool About It' — the song about pretending to be fine. The three met on tour in 2016 and became best friends first, band second.",
        ["Indie Rock", "Singer-Songwriter", "American", "2020s"], 2
    ),
    (
        "artist-st-vincent", "Artist",
        "St. Vincent",
        "Annie Clark named herself after a Nick Cave song ('There She Goes, My Beautiful World'). She played in The Polyphonic Spree and Sufjan Stevens' touring band before going solo. Her album 'Masseduction' won a Grammy for best alternative album.",
        "St. Vincent — Strange Mercy (2011) end-to-end", 41,
        "Listen to 'Digital Witness' — the song about living under surveillance. Then 'Cruel' — the singalong with the unsettling chorus. 'Los Ageless' — the song about LA's hollow glitter. She's one of the best guitar players alive, and her shows are pure theater.",
        ["Art Rock", "Indie Rock", "American", "2010s"], 2
    ),
    (
        "artist-grimes", "Artist",
        "Grimes",
        "Claire Boucher recorded her breakout album 'Visions' in three weeks of sleepless, manic sessions in a Montreal apartment. 'Kill V. Maim' is sung from the perspective of Al Pacino's character in The Godfather Part II, reimagined as a vampire gangster. She taught herself to sing.",
        "Grimes — Visions (2012) end-to-end", 44,
        "Listen to 'Oblivion' — the deceptively gentle song about being followed home. Then 'Kill V. Maim' — the shout-along from a vampire's perspective. 'Genesis' — the video shot in a volcano. She makes her own visuals, and her world-building is half the art.",
        ["Art Pop", "Synth-Pop", "Electronic", "Canadian", "2010s"], 2
    ),
    (
        "artist-beach-house", "Artist",
        "Beach House",
        "Victoria Legrand is the niece of Michel Legrand, the legendary French film composer. 'Space Song' — their most streamed track — went viral on TikTok years after release. The Baltimore duo make dream-pop that feels like a memory of a dream.",
        "Beach House — Bloom (2012) end-to-end", 61,
        "Listen to 'Space Song' in the dark — the shoegaze wash is the whole point. Then 'Myth' — the opener of 'Bloom'. 'PPP' — the song that sounds like a slow motion car crash in the best way. Their shows are loud enough to feel physical.",
        ["Dream Pop", "Shoegaze", "American", "2010s"], 2
    ),
    (
        "artist-alex-g", "Artist",
        "Alex G",
        "Alexander Giannascoli put out seven self-made albums on Bandcamp from his childhood bedroom in Pennsylvania before signing to Domino Records. He's also a sought-after producer who worked with Frank Ocean. His songs are lo-fi, weird, and strangely beautiful.",
        "Alex G — Rocket (2017) end-to-end", 43,
        "Listen to 'Gnaw' — the unhinged chorus that becomes a mantra. Then 'Proud' — the gentle country-ish ballad. 'Runner' — the synth-led single. He produced tracks for Frank Ocean's Blonde, which is how half the internet found him.",
        ["Indie Rock", "Lo-Fi", "American", "2010s"], 3
    ),
    (
        "artist-lorde", "Artist",
        "Lorde",
        "She was 16 when 'Royals' hit #1 in the US — the youngest solo artist to top the chart in years. She wrote it in about 30 minutes, inspired by a photo of a baseball player signing autographs. Her real name is Ella Yelich-O'Connor, and she's from New Zealand.",
        "Lorde — Melodrama (2017) end-to-end", 41,
        "Listen to 'Royals' — the anti-luxury anthem written by someone with no luxury. Then 'Ribs' — the song about the fear of growing up. 'Green Light' — the breakup banger built on a piano riff. 'Melodrama' is structured like a house party from start to finish.",
        ["Art Pop", "Electropop", "New Zealand", "2010s"], 1
    ),
    (
        "artist-billie-eilish", "Artist",
        "Billie Eilish",
        "'Ocean Eyes' — written by her brother Finneas — was uploaded to SoundCloud when she was 14 and went viral overnight. She and Finneas record in his childhood bedroom in Los Angeles; her debut album was made entirely in that small room with bunk beds. She has Tourette's.",
        "Billie Eilish — When We All Fall Asleep (2019) end-to-end", 43,
        "Listen to 'Ocean Eyes' — the song that started it all, recorded in a bedroom. Then 'everything i wanted' — the song about a dream where she drowned. 'bad guy' — the bass-heavy hit. Their bedroom studio in Highland Park has bunk beds and poster-covered walls.",
        ["Art Pop", "Electropop", "American", "2010s"], 1
    ),

    # ── FUNK-ADJACENT / MODERN INDIE INSTRUMENTAL ──
    (
        "artist-vulfpeck", "Artist",
        "Vulfpeck",
        "The band famously released 'Sleepify' — an album of complete silence — and asked fans to stream it on repeat, using the Spotify royalties to fund a completely free tour. Their tight, bass-driven funk made 'Dean Town' and '1612' internet anthems.",
        "Vulfpeck — The Beautiful Game (2016) end-to-end", 31,
        "Listen to 'Dean Town' — the bassline that bass players worship. Then 'Animal Spirits' — the funk with the falsetto hook. '1612' — the song about the year. They record albums live in one room with no overdubs, like a 60s soul session with jokes.",
        ["Funk", "Jazz Fusion", "American", "2010s"], 2
    ),
    (
        "artist-khruangbin", "Artist",
        "Khruangbin",
        "'Khruangbin' is Thai for 'aeroplane' — literally 'engine fly.' Bassist Laura Lee spotted the word on a Thai cassette and adopted it. The Houston trio play a dubbed-out mix of Thai funk, Persian pop, and surf rock, and famously dress in matching black.",
        "Khruangbin — The Universe Smiles Upon You (2015) end-to-end", 43,
        "Listen to 'White Gloves' — the dreamy instrumental that made them famous. Then 'People Everywhere (Still Alive)' — the funk cut. Their sound is built on space: bass, guitar, drums, and silence. 'Texas Sun' — their collaboration with Leon Bridges — is essential.",
        ["Funk", "Psychedelic Rock", "Thai", "2010s"], 2
    ),
    (
        "artist-thundercat", "Artist",
        "Thundercat",
        "Stephen Bruner — known as Thundercat — is one of the greatest living bass players, famous for his six-string fretless. He played bass all over Kendrick Lamar's 'To Pimp a Butterfly,' and 'Dragonball Durag' is a love song about his durag and Dragon Ball Z.",
        "Thundercat — Drunk (2017) end-to-end", 51,
        "Listen to 'Them Changes' — the groove built on a vintage soul sample and Bruner's own bass. Then 'Dragonball Durag' — the absurdly smooth R&B song about his durag. 'Oh Sheit It's X' — the one that sounds like a video game boss fight. His music is jazz, funk, and cartoons.",
        ["Neo-Soul", "Jazz Fusion", "Funk", "American", "2010s"], 2
    ),
    (
        "artist-flying-lotus", "Artist",
        "Flying Lotus",
        "Steven Ellison is the great-nephew of jazz legends Alice Coltrane and, by marriage, John Coltrane. He runs the Brainfeeder label, and 'Never Catch Me' features Kendrick Lamar, who appears on the cover in a casket. His music blends jazz, hip-hop, and sci-fi.",
        "Flying Lotus — You're Dead! (2014) end-to-end", 38,
        "Listen to 'Never Catch Me' — the Kendrick collaboration with a stunning dance video. Then 'Coronus, the Terminator' — the sci-fi jazz cut. 'Black Balloons Reprise' — the song with Denzel Curry. He's also a filmmaker — he directed the feature 'Kuso.'",
        ["Electronic", "Jazz Fusion", "Instrumental Hip-Hop", "American", "2010s"], 2
    ),
    (
        "artist-badbadnotgood", "Artist",
        "BADBADNOTGOOD",
        "A group of Canadian jazz-school students who went viral covering Odd Future songs as jazz in 2011. That led to real collaborations with Kendrick Lamar, Tyler, the Creator, and Ghostface Killah. Their albums blend jazz, hip-hop, and funk into something new.",
        "BADBADNOTGOOD — III (2014) end-to-end", 42,
        "Listen to 'Time Moves Slow' — the moody track with Sam Herring of Future Islands. Then 'Lavender' — the woozy single with Kaytranada. 'Can't Push Too Much' — the one that sounds like a jazz band crashing a rap session. They recorded 'III' in a church.",
        ["Jazz Fusion", "Instrumental Hip-Hop", "Canadian", "2010s"], 2
    ),
    (
        "artist-men-i-trust", "Artist",
        "Men I Trust",
        "The Canadian trio make hazy, bass-driven dream-pop. 'Show Me How' became a slow-burn internet hit years after release. Their music videos — shot on 16mm film — feel like home movies. They record in a cabin studio in the woods of Quebec, which is why everything sounds so calm.",
        "Men I Trust — Oncle Jazz (2019) end-to-end", 55,
        "Listen to 'Show Me How' — the hypnotic bassline and whisper vocals. Then 'Lauren' — the song that sounds like driving at dusk. 'Numb' — the one that builds and never quite explodes. Their whole catalog rewards headphones and patience.",
        ["Dream Pop", "Indie Pop", "Canadian", "2010s"], 3
    ),
    (
        "artist-crumb", "Artist",
        "Crumb",
        "The Brooklyn quartet formed while studying at Tufts University in Boston. 'Locket' — their breakout — has a trippy animated video. Their sound is a dreamy mix of psych-rock and jazz, led by guitarist Lila Ramani's whispery vocals.",
        "Crumb — Jinx (2019) end-to-end", 35,
        "Listen to 'Locket' — the hypnotic bass riff that made the song viral. Then 'Jinx' — the title track. 'Fall Down' — the one with the unexpected tempo change. Their songs sound like a lava lamp feels.",
        ["Psychedelic Pop", "Indie Rock", "Jazz Rock", "American", "2010s"], 3
    ),
    (
        "artist-still-woozy", "Artist",
        "Still Woozy",
        "Sven Gamsky writes, records, and produces everything from a backyard studio in Oakland. 'Goodie Bag' — his debut single — became a streaming hit with no label push. His songs blend lo-fi beats, funk bass, and falsetto hooks into instant serotonin.",
        "Still Woozy — Lately EP (2019) end-to-end", 20,
        "Listen to 'Goodie Bag' — the song that made him a star from a bedroom. Then 'Habit' — the one about being unable to let go. 'Wolfcat' — the fuzzy, weird one. He plays every instrument, recording vocals in his closet for the right sound.",
        ["Bedroom Pop", "Indie Pop", "Funk", "American", "2010s"], 3
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
