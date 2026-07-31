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

    # ═══════════════════════════════════════════════════════════════════════
    # BATCH 3 — Hip-Hop, Electronic, Jazz, World, Metal, Country, Blues, Classical, 2020s Pop
    # ═══════════════════════════════════════════════════════════════════════

    # ── HIP-HOP ──
    (
        "artist-nas", "Artist",
        "Nas",
        "Released 'Illmatic' at 19 — ten tracks, five producers, no filler — and it's been called the greatest hip-hop album ever. His father, jazz trumpeter Olu Dara, played on 'Life's a Bitch.' He was briefly a graffiti artist before rapping.",
        "Nas — Illmatic (1994) end-to-end", 39,
        "Listen to 'N.Y. State of Mind' — Nas wrote it in a single take after hearing the beat. Then 'The World Is Yours' — the track that made everyone believe a 19-year-old could be the king of New York. His voice is calm even when the stories are violent.",
        ["Hip-Hop", "East Coast", "American", "1990s"], 1
    ),
    (
        "artist-tupac", "Artist",
        "Tupac Shakur",
        "Named after Túpac Amaru II, an 18th-century Peruvian revolutionary, and raised by a Black Panther mother. He trained as a ballet dancer and studied theater as a teen. The 'Thug Life' tattoo spelled out a warning about how the system treats Black youth.",
        "Tupac — Me Against the World (1995) end-to-end", 66,
        "Listen to 'Dear Mama' — a love letter to his mother, written while he was in jail. Then 'Changes' — built on Bruce Hornsby's 'The Way It Is.' Tupac released two #1 albums in the same week while serving a prison sentence.",
        ["Hip-Hop", "West Coast", "Conscious Rap", "American", "1990s"], 1
    ),
    (
        "artist-biggie", "Artist",
        "The Notorious B.I.G.",
        "Christopher Wallace was a 6'3\", 300-lb former drug dealer from Brooklyn who became the greatest storyteller in rap. He didn't write lyrics down — he composed whole songs in his head. His debut 'Ready to Die' is the only album he released while alive.",
        "The Notorious B.I.G. — Ready to Die (1994) end-to-end", 70,
        "Listen to 'Juicy' — the rags-to-riches anthem written while he was still on welfare. Then 'Suicidal Thoughts' — the album's closer where he imagines his own death, recorded just days before he was killed. 'Big Poppa' — the smooth one everyone knows.",
        ["Hip-Hop", "East Coast", "Gangsta Rap", "American", "1990s"], 1
    ),
    (
        "artist-jay-z", "Artist",
        "Jay-Z",
        "Shawn Carter grew up in Brooklyn's Marcy Houses and turned street hustling into a billion-dollar empire. He never writes lyrics down — he freestyles in the booth. His debut 'Reasonable Doubt' initially flopped before becoming a classic. He has 50+ top-10 albums.",
        "Jay-Z — Reasonable Doubt (1996) end-to-end", 58,
        "Listen to 'Dead Presidents' — the opener that declared his intent. Then '99 Problems' — the second verse is legally accurate about search-and-seizure law. Jay-Z famously said 'I'm not a businessman, I'm a business, man.'",
        ["Hip-Hop", "East Coast", "American", "1990s"], 1
    ),
    (
        "artist-eminem", "Artist",
        "Eminem",
        "Marshall Mathers from Detroit, a battle rapper who became the best-selling artist of the 2000s. 'Rap God' holds a Guinness record for most words in a hit single — 1,560 words in six minutes. 'Lose Yourself' won an Oscar; he didn't attend the ceremony.",
        "Eminem — The Marshall Mathers LP (2000) end-to-end", 72,
        "Listen to 'Stan' — the story of an obsessed fan that inspired the Oxford English Dictionary to add the word 'stan.' Then 'Lose Yourself' — written on set while filming 8 Mile. 'The Real Slim Shady' — the satire that took over the radio.",
        ["Hip-Hop", "Midwest", "American", "2000s"], 1
    ),
    (
        "artist-drake", "Artist",
        "Drake",
        "Aubrey Graham was a child actor on Degrassi for seven years before music — his stage name is literally his middle name. He's the most-streamed artist in Spotify history, and his 2011 hit 'The Motto' invented the phrase 'YOLO.' He's from Toronto.",
        "Drake — Take Care (2011) end-to-end", 81,
        "Listen to 'Marvin's Room' — the drunk-dial anthem recorded in one emotional take. Then 'Started From the Bottom' — the song about his rise. 'Hotline Bling' — the video's dance moves broke the internet. Drake turned rap into a place for feelings.",
        ["Hip-Hop", "Canadian", "2010s"], 1
    ),
    (
        "artist-kanye-west", "Artist",
        "Kanye West",
        "A producer first — he made beats for Jay-Z before anyone believed he could rap. A 2002 car crash left his jaw wired shut; he recorded 'Through the Wire' anyway, rapping through clenched teeth. He was rejected by two labels before 'The College Dropout.'",
        "Kanye West — The College Dropout (2004) end-to-end", 76,
        "Listen to 'Through the Wire' — recorded three weeks after the crash, still recovering in the video. Then 'Jesus Walks' — the gospel rap single labels called career suicide. 'Graduation' — the album that beat 50 Cent in a famous release-date showdown.",
        ["Hip-Hop", "Chicago", "American", "2000s"], 1
    ),
    (
        "artist-tribe-called-quest", "Artist",
        "A Tribe Called Quest",
        "Q-Tip and Phife Dawg grew up two doors apart in Queens and met at age two. Their jazzy, Afrocentric sound defined the Native Tongues era. The group's name came from a recommendation on a Nation of Islam radio show. Phife's verses sharpened as his health worsened.",
        "A Tribe Called Quest — The Low End Theory (1991) end-to-end", 48,
        "Listen to 'Can I Kick It?' — the rap song built on Lou Reed's 'Walk on the Wild Side' bassline. Then 'Electric Relaxation' — the smoothest track ever made. 'Scenario' — the posse cut where Busta Rhymes stole the show with one verse.",
        ["Hip-Hop", "Jazz Rap", "Alternative Hip-Hop", "American", "1990s"], 2
    ),
    (
        "artist-wu-tang-clan", "Artist",
        "Wu-Tang Clan",
        "Nine members from Staten Island named after a martial-arts film. Their debut 'Enter the Wu-Tang (36 Chambers)' was recorded for about $40,000. RZA produced everything, and each member negotiated solo-deal freedom — a contract clause that changed the industry forever.",
        "Wu-Tang Clan — Enter the Wu-Tang (36 Chambers) (1993) end-to-end", 61,
        "Listen to 'C.R.E.A.M.' — the song about money with the immortal Raekwon and Inspectah Deck verses. Then 'Protect Ya Neck' — the first single. Ol' Dirty Bastard crashed the 1998 Grammys on stage to complain about losing.",
        ["Hip-Hop", "East Coast", "Hardcore Hip-Hop", "American", "1990s"], 1
    ),
    (
        "artist-run-dmc", "Artist",
        "Run-DMC",
        "The first rap group to go gold, get on MTV, and appear on American Bandstand. 'Walk This Way' with Aerosmith — the first rap-rock crossover — introduced rap to rock radio. Their Adidas endorsement made sneakers a hip-hop thing. Jam Master Jay was murdered in 2002.",
        "Run-DMC — Raising Hell (1986) end-to-end", 40,
        "Listen to 'Walk This Way' — the song that built a bridge between rock and rap. Then 'It's Tricky' — the beat borrowed from the Knack's 'My Sharona.' 'Down with the King' — the later anthem. Run and DMC's matching leather made hip-hop a uniform.",
        ["Hip-Hop", "Old School Hip-Hop", "American", "1980s"], 2
    ),
    (
        "artist-public-enemy", "Artist",
        "Public Enemy",
        "Chuck D's booming voice called rap 'the black CNN,' and producer team the Bomb Squad built beats from walls of noise. Flavor Flav wore a giant clock around his neck — the group's visual symbol. 'Fight the Power' was commissioned by Spike Lee for Do the Right Thing.",
        "Public Enemy — It Takes a Nation of Millions to Hold Us Back (1988) end-to-end", 58,
        "Listen to 'Fight the Power' — the anthem that opens Do the Right Thing. Then '911 Is a Joke' — Flavor Flav's sarcastic takedown of emergency services. 'Bring the Noise' — the song that proved samples could be weaponized.",
        ["Hip-Hop", "Political Rap", "East Coast", "American", "1980s"], 2
    ),
    (
        "artist-snoop-dogg", "Artist",
        "Snoop Dogg",
        "Calvin Cordozar Broadus Jr. got the nickname 'Snoopy' from his mother, who thought he looked like the Peanuts character. Dr. Dre heard a cassette of him rapping and signed him in 1992. His debut 'Doggystyle' was the first debut album to enter the Billboard 200 at #1.",
        "Snoop Dogg — Doggystyle (1993) end-to-end", 44,
        "Listen to 'Gin and Juice' — the laid-back classic with the unforgettable hook. Then 'Nuthin' but a 'G' Thang' — the Dr. Dre collab that made him a star. Snoop's smooth flow invented the West Coast sound's cool.",
        ["Hip-Hop", "West Coast", "G-Funk", "American", "1990s"], 1
    ),
    (
        "artist-dr-dre", "Artist",
        "Dr. Dre",
        "A founding member of N.W.A, then the architect of G-funk with 'The Chronic,' then the mentor behind Eminem, 50 Cent, and Kendrick Lamar. He has an honorary doctorate from USC — he wore cap and gown on stage. His Beats headphones sold to Apple for $3 billion.",
        "Dr. Dre — The Chronic (1992) end-to-end", 63,
        "Listen to 'Nuthin' but a 'G' Thang' — the G-funk groove that defined 90s West Coast. Then 'Still D.R.E.' — the comeback single with Snoop. Dre is a perfectionist: Eminem says he had him re-record verses 20 times.",
        ["Hip-Hop", "West Coast", "G-Funk", "American", "1990s"], 1
    ),
    (
        "artist-tyler-the-creator", "Artist",
        "Tyler, the Creator",
        "Founded Odd Future from a message-board posse and produced his early beats in his bedroom. He was banned from the UK over lyrics he later disowned. 'IGOR' — his fifth album — won Best Rap Album at the Grammys. He also runs the Golf Wang clothing brand.",
        "Tyler, the Creator — IGOR (2019) end-to-end", 40,
        "Listen to 'EARFQUAKE' — the love song with Charlie Wilson and Playboi Carti. Then 'Yonkers' — the $2,000 video that made him famous overnight. 'See You Again' — the vulnerable gem. Tyler went from shock rapper to Grammy-winning artist.",
        ["Hip-Hop", "Alternative Hip-Hop", "American", "2010s"], 2
    ),
    (
        "artist-j-cole", "Artist",
        "J. Cole",
        "From Fayetteville, North Carolina, he got a basketball scholarship to St. John's University before choosing rap. His debut '2014 Forest Hills Drive' — named after his childhood home — went platinum with no features. He's one of the few rappers with no ghostwriters.",
        "J. Cole — 2014 Forest Hills Drive (2014) end-to-end", 65,
        "Listen to 'Wet Dreamz' — the hilarious, honest story of losing his virginity. Then 'No Role Modelz' — the hit that samples a Will Smith speech. 'Love Yourz' — the album's closing message that money isn't the point.",
        ["Hip-Hop", "Conscious Rap", "American", "2010s"], 2
    ),
    (
        "artist-childish-gambino", "Artist",
        "Childish Gambino",
        "Donald Glover was a writer on 30 Rock at 23 before rapping as Childish Gambino. His 2018 single 'This Is America' won Song and Record of the Year at the Grammys, with a video packed with symbolic details. He also created the series Atlanta.",
        "Childish Gambino — Because the Internet (2013) end-to-end", 57,
        "Watch 'This Is America' twice — the video is a choreography of cultural references. Then 'Redbone' — the funk slow jam that became a meme and a protest anthem. 'Sober' — the song that proved he could sing. Glover does everything: writer, actor, rapper.",
        ["Hip-Hop", "Alternative Hip-Hop", "American", "2010s"], 2
    ),
    (
        "artist-frank-ocean", "Artist",
        "Frank Ocean",
        "Wrote songs for Justin Bieber and Beyoncé before releasing his own debut. 'Channel Orange' — released the day he came out in an open letter — redefined R&B. His second album 'Blonde' took four years to make. He was born in New Orleans and moved to LA for film school.",
        "Frank Ocean — Blonde (2016) end-to-end", 60,
        "Listen to 'Thinkin Bout You' — the song that proved R&B could be this vulnerable. Then 'Nights' — the two-in-one song that flips halfway. 'Pink + White' — produced with Pharrell. Frank turned heartbreak into high art.",
        ["R&B", "Hip-Hop Soul", "American", "2010s"], 1
    ),

    # ── ELECTRONIC ──
    (
        "artist-burial", "Artist",
        "Burial",
        "The anonymous South London producer behind 'Untrue' — one of the most influential albums of the 2000s. His real name, William Bevan, stayed secret for years; he gave one famously awkward radio interview and vanished. He makes music on a laptop in his bedroom.",
        "Burial — Untrue (2007) end-to-end", 50,
        "Listen to 'Archangel' — the track that sounds like a memory of a song. Then 'Untrue' — the title track built from a 1960s soul sample. Burial's music is the sound of London at night — lonely, warm, and lit by streetlights. It won the Mercury Prize in 2008.",
        ["Electronic", "UK Garage", "Ambient", "British", "2000s"], 2
    ),
    (
        "artist-four-tet", "Artist",
        "Four Tet",
        "Kieran Hebden studied literature at university, then became one of electronic music's most beloved figures. He's known for ecstatic festival DJ sets and intricate sampling — he, Fred again.., and Skrillex released a joint live album. His 'Rounds' is a laptop-built classic.",
        "Four Tet — Rounds (2003) end-to-end", 53,
        "Listen to 'She Moves She' — the opener built from a guitar sample that never repeats the same way. Then 'Two Thousand and Seventeen' — the breakthrough single. Four Tet treats samples like instruments, not loops — every listen reveals new details.",
        ["Electronic", "IDM", "British", "2000s"], 2
    ),
    (
        "artist-boards-of-canada", "Artist",
        "Boards of Canada",
        "Brothers-in-law Michael Sandison and Marcus Eoin record in a secluded Scottish studio. Their music sounds like 1970s educational films — warm tape hiss, forgotten melodies. 'Music Has the Right to Children' made nostalgia a genre, inspired by a National Film Board documentary.",
        "Boards of Canada — Music Has the Right to Children (1998) end-to-end", 68,
        "Listen to 'Roygbiv' — the beloved track named after the colors of the rainbow. Then 'Dayvan Cowboy' — the track with the famous skydiver video. Boards of Canada bury melodies in tape noise; headphones reveal new layers on every listen.",
        ["Electronic", "Ambient", "IDM", "Scottish", "1990s"], 2
    ),
    (
        "artist-brian-eno", "Artist",
        "Brian Eno",
        "The man who coined 'ambient music.' He played keyboards in Roxy Music, produced albums for U2, David Bowie, and Talking Heads, and invented the Oblique Strategies card deck to break creative block. He also created the Microsoft Windows startup sound.",
        "Brian Eno — Music for Airports (1978) end-to-end", 48,
        "Listen to 'Music for Airports' — music designed to be as ignorable as it is interesting. Then 'An Ending (Ascent)' — the piece used in a hundred documentaries. Eno once said 'the studio is an instrument' — he treated the mixing desk as a musical tool.",
        ["Electronic", "Ambient", "British", "1970s"], 1
    ),
    (
        "artist-massive-attack", "Artist",
        "Massive Attack",
        "The Bristol trio who invented trip-hop. 'Unfinished Sympathy' — with its famous continuous walking-shot video — made them icons. 'Teardrop' became the theme for the TV show House. Their 1991 debut 'Blue Lines' mixed hip-hop, dub, and soul into a new sound.",
        "Massive Attack — Blue Lines (1991) end-to-end", 45,
        "Watch 'Unfinished Sympathy' — one continuous shot of singer Shara Nelson walking down a street. Then 'Teardrop' — the song whose video is a fetus in a womb. 'Blue Lines' took two years to record; the band nearly didn't finish it.",
        ["Electronic", "Trip-Hop", "British", "1990s"], 1
    ),
    (
        "artist-portishead", "Artist",
        "Portishead",
        "The Bristol trio whose 1994 debut 'Dummy' sold millions and won the Mercury Prize. Beth Gibbons' voice is unmistakable — 'Glory Box' became an instant classic. Their follow-up took three years and pushed the band to the edge, using live strings and dusty samples.",
        "Portishead — Dummy (1994) end-to-end", 48,
        "Listen to 'Glory Box' — the song that samples Isaac Hayes and turns it into heartbreak. Then 'Sour Times' — the haunted single. 'Wandering Star' — built on a Lalo Schifrin sample. Portishead's music sounds like a record being dragged through a haunted house.",
        ["Electronic", "Trip-Hop", "British", "1990s"], 2
    ),
    (
        "artist-chemical-brothers", "Artist",
        "The Chemical Brothers",
        "Tom Rowlands and Ed Simons met at university in Manchester. Their 'big beat' sound filled dance floors and arenas. 'Block Rockin' Beats' won a Grammy, and 'Galvanize' — with Q-Tip — did too. They've made albums for over 25 years and still headline festivals.",
        "The Chemical Brothers — Dig Your Own Hole (1997) end-to-end", 63,
        "Listen to 'Galvanize' — the Q-Tip collab that opens with a firework sound. Then 'Block Rockin' Beats' — built from a breakbeat sampled a hundred times before. 'Hey Boy Hey Girl' — the rave anthem. Their live shows are legendary for a reason.",
        ["Electronic", "Big Beat", "British", "1990s"], 2
    ),
    (
        "artist-prodigy", "Artist",
        "The Prodigy",
        "Liam Howlett formed the band; Keith Flint's shaved head and mohawk made him its face. 'Firestarter' — with its famous tunnel video — became a global hit. 'Smack My Bitch Up' sparked a censorship debate. Keith Flint died in 2019.",
        "The Prodigy — The Fat of the Land (1997) end-to-end", 57,
        "Listen to 'Firestarter' — the electronic punk anthem that scared parents. Then 'Breathe' — the darker follow-up. 'Smack My Bitch Up' — the controversial banger. The Prodigy proved dance music could be dangerous and loud.",
        ["Electronic", "Big Beat", "Rave", "British", "1990s"], 2
    ),
    (
        "artist-fatboy-slim", "Artist",
        "Fatboy Slim",
        "Norman Cook — a former member of indie band the Housemartins — reinvented himself as Fatboy Slim. 'Praise You' — with its guerrilla-style video shot without permits — won MTV awards. 'Weapon of Choice' featured Christopher Walken dancing. He's a Brighton legend.",
        "Fatboy Slim — You've Come a Long Way, Baby (1998) end-to-end", 47,
        "Watch 'Weapon of Choice' — Christopher Walken floating through a hotel lobby, one of the greatest music videos ever. Then 'Praise You' — filmed outside a cinema without permission. 'The Rockafeller Skank' — the sample that made every club bounce.",
        ["Electronic", "Big Beat", "British", "1990s"], 2
    ),
    (
        "artist-moby", "Artist",
        "Moby",
        "Richard Melville Hall's stage name comes from his family connection to Herman Melville — the author of Moby-Dick. His 1999 album 'Play' was the first album whose every track was licensed for ads and films, and it sold 12 million copies. He's a vegan activist.",
        "Moby — Play (1999) end-to-end", 65,
        "Listen to 'Porcelain' — the dreamy track that became a soundtrack staple. Then 'Why Does My Heart Feel So Bad?' — built from a 1950s gospel sample. 'Natural Blues' — the aching centerpiece. Moby licensed every song on 'Play' to advertisers — a move that changed the industry.",
        ["Electronic", "Ambient", "American", "1990s"], 2
    ),
    (
        "artist-justice", "Artist",
        "Justice",
        "The French duo of Gaspard Augé and Xavier de Rosnay — two friends who made electronic music that rocks. Their debut '†' spawned 'D.A.N.C.E.' — with its t-shirt animation video. 'We Are Your Friends' and 'Safe and Sound' became festival anthems.",
        "Justice — † (2007) end-to-end", 48,
        "Listen to 'D.A.N.C.E.' — the joyful song with the famous T-shirt video. Then 'Genesis' — the pounding opener of their debut. 'Safe and Sound' — the head-nodder. Justice's glowing cross and arena shows made them icons of the 2000s electro sound.",
        ["Electronic", "House", "French", "2000s"], 2
    ),
    (
        "artist-caribou", "Artist",
        "Caribou",
        "Dan Snaith — a mathematician with a PhD from Imperial College — makes dance music with a scientist's precision. 'Swim' and 'Our Love' are modern classics. He also records as Daphni. 'Can't Do Without You' is his most beloved song.",
        "Caribou — Our Love (2014) end-to-end", 49,
        "Listen to 'Can't Do Without You' — the love song built on a simple vocal loop that builds and builds. Then 'Odessa' — the synth-funk single. 'Sun' — the shimmering closer. Snaith's PhD is in mathematics, but he says music is the harder math.",
        ["Electronic", "House", "Canadian", "2010s"], 2
    ),
    (
        "artist-jon-hopkins", "Artist",
        "Jon Hopkins",
        "Classically trained and raised on raves, Jon Hopkins worked as a keyboard tech for Imogen Heap before going solo. 'Immunity' — his 2013 album — is a modern electronic masterpiece. He also composed the film score for 'Monsters.' His live sets are famously intense.",
        "Jon Hopkins — Immunity (2013) end-to-end", 64,
        "Listen to 'Open Eye Signal' — the 12-minute centerpiece of 'Immunity' that never sits still. Then 'Singularity' — the title track that builds to a crushing climax. 'Abandon Window' — the piano piece that shows his classical side. Hopkins blurs dance and art music.",
        ["Electronic", "Techno", "Ambient", "British", "2010s"], 2
    ),

    # ── JAZZ ──
    (
        "artist-charlie-parker", "Artist",
        "Charlie Parker",
        "The saxophonist who invented bebop — jazz's revolution. His nickname 'Bird' came from his love of fried chicken, 'yardbird.' He was from Kansas City, where he soaked up the blues. Parker played so fast and so new that musicians had to relearn jazz to follow him. He died at 34.",
        "Charlie Parker — The Savoy Recordings (1945) end-to-end", 50,
        "Listen to 'Ko-Ko' — the bebop landmark that rewrote 'Cherokee.' Then 'Ornithology' — the tune built on 'How High the Moon.' Parker once said 'music is your own experience, your thoughts, your wisdom.' The man could make a saxophone sound like laughing.",
        ["Jazz", "Bebop", "American", "1940s"], 1
    ),
    (
        "artist-duke-ellington", "Artist",
        "Duke Ellington",
        "Edward Kennedy Ellington got the nickname 'Duke' as a kid for his elegant manners. He led one of jazz's greatest orchestras for 50 years and wrote over 1,000 compositions — many with collaborator Billy Strayhorn. He considered his orchestra his real instrument.",
        "Duke Ellington — Ellington at Newport (1956) end-to-end", 53,
        "Listen to 'Take the A Train' — the band's theme, written by Billy Strayhorn about the subway line to Harlem. Then 'It Don't Mean a Thing (If It Ain't Got That Swing)' — the song that named an era. 'Mood Indigo' — the haunting signature tune.",
        ["Jazz", "Big Band", "American", "1930s"], 1
    ),
    (
        "artist-louis-armstrong", "Artist",
        "Louis Armstrong",
        "Born in New Orleans' poorest neighborhood, he sang on street corners for coins as a kid. He got his first cornet at 13 from a pawn shop. Satchmo made the solo and scat singing what they are. 'What a Wonderful World' flopped in the US — and became a hit in the UK.",
        "Louis Armstrong — The Complete Hot Five Recordings (1928) end-to-end", 60,
        "Listen to 'What a Wonderful World' — the song that became his signature decades after it flopped at home. Then 'West End Blues' — the 1928 solo that changed jazz forever. 'Hello, Dolly!' — his biggest hit, which knocked the Beatles off the #1 spot.",
        ["Jazz", "Traditional Jazz", "American", "1920s"], 1
    ),
    (
        "artist-thelonious-monk", "Artist",
        "Thelonious Monk",
        "The pianist who played 'the wrong notes' — until everyone realized they were right. He wore odd hats and would get up and dance during his own songs. His tune 'Round Midnight' is the most-recorded jazz standard. He composed angular, catchy melodies now in the canon.",
        "Thelonious Monk — Brilliant Corners (1957) end-to-end", 42,
        "Listen to 'Round Midnight' — the moody standard that became his calling card. Then 'Blue Monk' — the blues tune with the unmistakable melody. 'Straight, No Chaser' — the tune with the famous dissonant intro. Monk's playing was percussive and strange — and genius.",
        ["Jazz", "Bebop", "American", "1950s"], 2
    ),
    (
        "artist-charles-mingus", "Artist",
        "Charles Mingus",
        "A bassist and composer of ferocious energy, he wrote 'Goodbye Pork Pie Hat' as a tribute to Lester Young. His bands were workshops where musicians argued and improvised. He taught himself bass as a teen in LA. His music swings hard and then flies off the rails — on purpose.",
        "Charles Mingus — Mingus Ah Um (1959) end-to-end", 46,
        "Listen to 'Goodbye Pork Pie Hat' — the tender elegy for Lester Young. Then 'Haitian Fight Song' — the roaring, gospel-tinged epic. 'Better Git Hit in Your Soul' — the opening that feels like a revival meeting. Mingus called his music 'spontaneous composition.'",
        ["Jazz", "Hard Bop", "American", "1950s"], 2
    ),
    (
        "artist-herbie-hancock", "Artist",
        "Herbie Hancock",
        "A piano prodigy who played with the Chicago Symphony at 11, then joined Miles Davis' second great quintet at 23. 'Cantaloupe Island' became one of the most-sampled jazz tunes ever. He won an Oscar for the Round Midnight score. In the 80s, 'Rockit' made him a pop star.",
        "Herbie Hancock — Maiden Voyage (1965) end-to-end", 42,
        "Listen to 'Maiden Voyage' — the modal classic about a ship's first journey. Then 'Cantaloupe Island' — the funky tune sampled by US3 for 'Cantaloop.' 'Rockit' — the synth hit with the famous robot video. Hancock reinvents himself every decade.",
        ["Jazz", "Jazz Fusion", "American", "1960s"], 1
    ),
    (
        "artist-nina-simone", "Artist",
        "Nina Simone",
        "Born Eunice Waymon, she trained as a classical pianist, dreaming of being the first Black concert pianist. She changed her name to hide it from her family. 'Mississippi Goddam' — her civil rights anthem — was banned in the South.",
        "Nina Simone — Pastel Blues (1965) end-to-end", 40,
        "Listen to 'Mississippi Goddam' — the furious, sarcastic anthem she wrote in an hour after a church bombing. Then 'Feeling Good' — the song that became an anthem decades later. 'Sinnerman' — the 10-minute gospel tour de force. Nina did everything her way.",
        ["Jazz", "Vocal Jazz", "Soul", "American", "1960s"], 1
    ),
    (
        "artist-ella-fitzgerald", "Artist",
        "Ella Fitzgerald",
        "The 'First Lady of Song' — she won 13 Grammys and could scat faster than any horn player. She entered Apollo's Amateur Night planning to dance — she sang instead and won. Her Songbook albums are jazz landmarks. She had perfect pitch and a three-octave range.",
        "Ella Fitzgerald — Ella Sings the Gershwin Songbook (1959) end-to-end", 55,
        "Listen to 'Mack the Knife' — her famous live version where she forgets the lyrics and improvises the rest. Then 'How High the Moon' — the scat showcase. 'Summertime' — the timeless reading. Ella turned her voice into an instrument; no one has matched her since.",
        ["Jazz", "Vocal Jazz", "American", "1940s"], 1
    ),
    (
        "artist-chet-baker", "Artist",
        "Chet Baker",
        "The 'James Dean of jazz' — a handsome trumpeter who sang in a fragile, whispery voice. 'My Funny Valentine' made him a heartthrob. His life was shadowed by heroin addiction; he lost his teeth at 30 and learned to play again. He fell from an Amsterdam hotel window in 1988.",
        "Chet Baker — Chet Baker Sings (1954) end-to-end", 42,
        "Listen to 'My Funny Valentine' — the version that made him famous. Then 'Let's Get Lost' — the title of both his hit and his documentary. 'Almost Blue' — the aching ballad. Baker's voice and trumpet sound like one instrument — vulnerable and achingly cool.",
        ["Jazz", "Cool Jazz", "American", "1950s"], 2
    ),
    (
        "artist-bill-evans", "Artist",
        "Bill Evans",
        "The pianist whose chord voicings changed jazz harmony forever. He played on Miles Davis' 'Kind of Blue.' His trio with Scott LaFaro and Paul Motian redefined the jazz trio — LaFaro died in a car crash ten days after their Village Vanguard recordings.",
        "Bill Evans — Sunday at the Village Vanguard (1961) end-to-end", 55,
        "Listen to 'Waltz for Debby' — written for his niece, recorded live at the Village Vanguard. Then 'Peace Piece' — the meditative improvisation. 'Blue in Green' — the Kind of Blue track he co-wrote with Miles. Evans' music is intimate, like thinking out loud.",
        ["Jazz", "Cool Jazz", "Modal Jazz", "American", "1960s"], 2
    ),
    (
        "artist-wes-montgomery", "Artist",
        "Wes Montgomery",
        "The guitarist who played every note with his thumb — no pick — and never learned to read music. He worked factory jobs and gigged at night until his 40s. 'The Incredible Jazz Guitar' made him a star overnight. His octave technique is still studied by guitarists everywhere.",
        "Wes Montgomery — The Incredible Jazz Guitar (1960) end-to-end", 40,
        "Listen to 'Four on Six' — the bluesy original that became a standard. Then 'A Day in the Life' — the Beatles cover that won him a Grammy. 'Round Midnight' — his reading of the Monk classic. Montgomery's thumb was so fast that guitarists assumed he used two players.",
        ["Jazz", "Hard Bop", "American", "1960s"], 2
    ),

    # ── WORLD ──
    (
        "artist-ravi-shankar", "Artist",
        "Ravi Shankar",
        "The sitar master who brought Indian classical music to the West. He taught George Harrison to play sitar — 'Norwegian Wood' was the first pop song with it. He played Monterey Pop and Woodstock. His daughter is Norah Jones. He spent his life proving Indian music is universal.",
        "Ravi Shankar — Three Ragas (1956) end-to-end", 48,
        "Listen to 'Raga Jog' — the live sitar masterpiece that hypnotized Woodstock. Then the recordings he made with George Harrison, where sitar met rock. Shankar said the sitar's buzzing strings sound like 'a thousand birds.' His music is meditation with rhythm.",
        ["World", "Indian Classical", "Indian", "1960s"], 1
    ),
    (
        "artist-ali-farka-toure", "Artist",
        "Ali Farka Touré",
        "The Malian guitarist known as 'the African John Lee Hooker' for his hypnotic desert blues. He was also a farmer and a village chief who refused to tour. His album 'Talking Timbuktu' with Ry Cooder won a Grammy. He said 'music is the food of the soul.'",
        "Ali Farka Touré — Talking Timbuktu (1994) end-to-end", 61,
        "Listen to 'Ai Du' — the track that sounds like the desert itself. Then 'Talking Timbuktu' — the album that won the Grammy. Ali Farka's guitar is played like a drum and a voice at once — hypnotic, earthy, and completely original.",
        ["World", "Malian Blues", "Malian", "1990s"], 2
    ),
    (
        "artist-youssou-ndour", "Artist",
        "Youssou N'Dour",
        "The Senegalese singer called 'the Voice of Africa.' His song '7 Seconds' with Neneh Cherry was a global hit. He fused traditional mbalax rhythms with modern pop. He later became Senegal's minister of culture. He has released over 25 albums and filled stadiums on three continents.",
        "Youssou N'Dour — Egypt (2004) end-to-end", 55,
        "Listen to '7 Seconds' — the duet about a child's first moments that became an anthem. Then 'Egypt' — the album that celebrates Sufi traditions. N'Dour's voice can be tender or thunderous. He once said 'I sing for the whole world, but my roots are in Dakar.'",
        ["World", "Mbalax", "Senegalese", "1990s"], 2
    ),
    (
        "artist-cesaria-evora", "Artist",
        "Cesária Évora",
        "The 'Barefoot Diva' of Cape Verde, who sang morna — the islands' melancholy blues — in bare feet as a tribute to the poor. She was a domestic worker who sang in bars for decades before being discovered at 47. 'Sodade' made her world-famous. She never wore shoes on stage.",
        "Cesária Évora — Miss Perfumado (1992) end-to-end", 50,
        "Listen to 'Sodade' — the longing-filled song about emigration that became her anthem. Then 'Besame Mucho' — her reading of the classic. Évora's voice is smoky and gentle, the sound of the Atlantic. She performed barefoot at every venue, from bars to the Opera House.",
        ["World", "Morna", "Cape Verdean", "1980s"], 2
    ),
    (
        "artist-peter-tosh", "Artist",
        "Peter Tosh",
        "The third Wailer alongside Bob Marley — and the militant one. He was arrested for marijuana possession, which inspired his solo album 'Legalize It.' He was killed in 1987 during a home invasion. He preached 'equal rights and justice' in military-style clothes.",
        "Peter Tosh — Legalize It (1976) end-to-end", 38,
        "Listen to 'Legalize It' — the reggae anthem that campaigned for marijuana law reform. Then 'Equal Rights' — the title track demanding justice. 'Get Up, Stand Up' — the song he co-wrote with Marley. Tosh was the hardest voice in reggae — his lyrics were declarations.",
        ["Reggae", "Roots Reggae", "Jamaican", "1970s"], 2
    ),
    (
        "artist-lee-scratch-perry", "Artist",
        "Lee \"Scratch\" Perry",
        "The eccentric genius who invented dub — the art of remixing a song into a new one. At his Black Ark studio in Jamaica he recorded Bob Marley and the Congos. He once burned the studio down, saying it was haunted. He wore foil and crystals and claimed to talk to spirits.",
        "Lee \"Scratch\" Perry — Super Ape (1976) end-to-end", 32,
        "Listen to 'Super Ape' — the dub masterpiece where instruments become ghosts. Then 'Roast Fish & Cornbread' — his strange solo album. Perry's production is full of echoes, sirens, and mad laughter. He called himself 'the Upsetter' — the man who upset music's rules.",
        ["Dub", "Reggae", "Jamaican", "1970s"], 3
    ),
    (
        "artist-mulatu-astatke", "Artist",
        "Mulatu Astatke",
        "The Ethiopian composer called 'the father of Ethio-jazz.' He studied in London and New York in the 1950s, then fused jazz with Ethiopian scales. His music lay forgotten until the Éthiopiques series revived it — and Jim Jarmusch used it in 'Broken Flowers.'",
        "Mulatu Astatke — Éthiopiques Vol. 4 (1998) end-to-end", 44,
        "Listen to 'Yèkèrmo Sèw' — the Ethio-jazz classic with the haunting vibraphone line. Then 'Tezeta' — the 'nostalgia' melody. Astatke's music sounds like jazz from another planet — Ethiopian pentatonic scales over 60s American grooves.",
        ["World", "Ethio-Jazz", "Ethiopian", "1970s"], 3
    ),
    (
        "artist-oumou-sangare", "Artist",
        "Oumou Sangaré",
        "The Malian singer known as 'the Songbird of Wassoulou.' Her debut 'Moussolou' — meaning 'Women' — sold hundreds of thousands of copies, a record in Africa. She sings about women's rights in a culture that doesn't always want to hear it. She also runs a successful car business.",
        "Oumou Sangaré — Moussolou (1991) end-to-end", 40,
        "Listen to 'Moussolou' — the album that made her a star across West Africa. Then 'Saa Magni' — the powerful follow-up. Sangaré's voice is warm and commanding, backed by the hunting rhythms of Wassoulou. She built her success on her own terms.",
        ["World", "Wassoulou", "Malian", "1990s"], 3
    ),
    (
        "artist-jorge-ben", "Artist",
        "Jorge Ben",
        "The Brazilian musician behind 'Mas que Nada' — one of the most famous songs in the world, covered by Sergio Mendes. He blends samba, funk, and soul into a joyful stew. He sued Rod Stewart over 'Do Ya Think I'm Sexy?' — and won, because it sampled his 'Taj Mahal.'",
        "Jorge Ben — África Brasil (1976) end-to-end", 43,
        "Listen to 'Mas que Nada' — the samba-funk classic everyone knows. Then 'Taj Mahal' — the song whose melody Rod Stewart borrowed. 'Oba, Lá Vem Ela' — the sunshine anthem. Ben's guitar is tuned like a cavaquinho and his songs are pure joy.",
        ["World", "Samba", "Brazilian", "1960s"], 2
    ),
    (
        "artist-milton-nascimento", "Artist",
        "Milton Nascimento",
        "The Brazilian singer with the angelic voice, adopted and raised in a small town. 'Clube da Esquina' — his 1972 album with Lô Borges — is a masterpiece of MPB. He has collaborated with everyone from Wayne Shorter to Björk. His voice sounds like a choir in one person.",
        "Milton Nascimento — Clube da Esquina (1972) end-to-end", 66,
        "Listen to 'Clube da Esquina No. 2' — the dreamlike title track. Then 'Travessia' — the song that made his name. 'Coração de Estudante' — the beloved classic. Nascimento's voice climbs and soars; Brazilians call him 'Bituca,' a childhood nickname.",
        ["World", "MPB", "Brazilian", "1970s"], 2
    ),

    # ── METAL ──
    (
        "artist-iron-maiden", "Artist",
        "Iron Maiden",
        "The band with mascot Eddie, a monster on every album cover. Singer Bruce Dickinson is a commercial airline pilot and a fencing champion. 'The Number of the Beast' got them accused of Satanism. They've sold over 100 million albums and fly their own plane, 'Ed Force One.'",
        "Iron Maiden — The Number of the Beast (1982) end-to-end", 40,
        "Listen to 'The Trooper' — the galloping anthem about the Charge of the Light Brigade. Then 'Run to the Hills' — the song about colonization. 'Fear of the Dark' — the crowd-pleaser that makes arenas sing. Maiden's galloping bass and Dickinson's operatic wail are iconic.",
        ["Metal", "Heavy Metal", "British", "1980s"], 1
    ),
    (
        "artist-slayer", "Artist",
        "Slayer",
        "The thrash band whose 1986 album 'Reign in Blood' — all 29 minutes of it — remains a landmark of extremity. 'Angel of Death' — about Nazi doctor Josef Mengele — caused outrage. Drummer Dave Lombardo is considered one of metal's greatest. They played at 200 miles per hour.",
        "Slayer — Reign in Blood (1986) end-to-end", 29,
        "Listen to 'Raining Blood' — the album's closer, ending in rain and thunder. Then 'Angel of Death' — the controversial opener. 'South of Heaven' — the slower, more menacing follow-up. Slayer's riffs sound like a chainsaw cutting through the walls of rock.",
        ["Metal", "Thrash Metal", "American", "1980s"], 2
    ),
    (
        "artist-megadeth", "Artist",
        "Megadeth",
        "Dave Mustaine was kicked out of Metallica a day before they recorded their debut — so he started Megadeth to prove them wrong. 'Rust in Peace' — with its legendary lineup — is a thrash masterpiece. The title track is about a nuclear war that never ends.",
        "Megadeth — Rust in Peace (1990) end-to-end", 47,
        "Listen to 'Holy Wars... The Punishment Due' — the opening epic of 'Rust in Peace.' Then 'Hangar 18' — the song about Area 51. 'Symphony of Destruction' — the radio hit with the piano riff. Megadeth is thrash with a technical edge.",
        ["Metal", "Thrash Metal", "American", "1980s"], 2
    ),
    (
        "artist-pantera", "Artist",
        "Pantera",
        "The Texas band that made groove metal — 'Cowboys from Hell' reinvented the genre. Guitarist Dimebag Darrell was murdered on stage in 2004 by a deranged fan, one of music's darkest nights. Phil Anselmo's roar and Dimebag's riffs made them stadium metal gods.",
        "Pantera — Vulgar Display of Power (1992) end-to-end", 49,
        "Listen to 'Walk' — the stomping riff every metal fan knows. Then 'Cowboys from Hell' — the title track that announced the new sound. 'Cemetery Gates' — the ballad that shows their range. Pantera were a glam band first — they invented themselves twice.",
        ["Metal", "Groove Metal", "American", "1990s"], 2
    ),
    (
        "artist-tool", "Artist",
        "Tool",
        "The art-rock metal band fronted by Maynard James Keenan, who also makes wine in Arizona. 'Lateralus' is structured around the Fibonacci sequence. Fans waited 13 years between '10,000 Days' and 'Fear Inoculum.' Their music is dense, mathematical, and hypnotic.",
        "Tool — Lateralus (2001) end-to-end", 79,
        "Listen to 'Schism' — the Grammy-winning single in shifting time signatures. Then 'Lateralus' — the title track where the syllables follow the Fibonacci numbers. 'Pneuma' — the modern epic. Tool's music rewards patience; every listen reveals new layers.",
        ["Metal", "Progressive Metal", "American", "1990s"], 1
    ),
    (
        "artist-opeth", "Artist",
        "Opeth",
        "The Swedish band who mixed death metal growls with beautiful acoustic passages — 'progressive death metal.' Mikael Åkerfeldt's vocals switch between demonic and angelic. 'Blackwater Park' — produced with Steven Wilson — is their masterpiece.",
        "Opeth — Blackwater Park (2001) end-to-end", 67,
        "Listen to 'The Drapery Falls' — the centerpiece that swings between metal and folk. Then 'Harvest' — the acoustic song with the soaring chorus. 'Windowpane' — the haunting cut. Opeth proved heavy music could be elegant; their transitions are the real show.",
        ["Metal", "Progressive Death Metal", "Swedish", "2000s"], 2
    ),
    (
        "artist-judas-priest", "Artist",
        "Judas Priest",
        "The band that defined heavy metal's leather-and-bikes look. Rob Halford — 'the Metal God' — has one of the most powerful voices in rock. He came out as gay in 1998, the first major metal frontman to do so. They influenced everyone from Metallica to Slipknot.",
        "Judas Priest — British Steel (1980) end-to-end", 36,
        "Listen to 'Breaking the Law' — the anthem about frustration and rebellion. Then 'Painkiller' — the opening scream is one of metal's greatest moments. 'Turbo Lover' — the synth-metal hit. Halford rides a motorcycle on stage — the leather icon of metal.",
        ["Metal", "Heavy Metal", "British", "1970s"], 2
    ),
    (
        "artist-system-of-a-down", "Artist",
        "System of a Down",
        "The Armenian-American band who mixed metal with absurdist, political lyrics. 'Toxicity' — their breakthrough — went to #1. They use their platform to advocate for recognition of the Armenian genocide. 'Chop Suey!' became a meme decades after release. They reunited in 2010.",
        "System of a Down — Toxicity (2001) end-to-end", 44,
        "Listen to 'Chop Suey!' — the song that refused to follow song structure. Then 'Toxicity' — the title track about mental illness. 'B.Y.O.B.' — the anti-war anthem that won a Grammy. SOAD's sound is part metal, part circus, part revolution.",
        ["Metal", "Alternative Metal", "American", "2000s"], 2
    ),

    # ── COUNTRY ──
    (
        "artist-willie-nelson", "Artist",
        "Willie Nelson",
        "The outlaw who grew a beard, broke Nashville's rules, and released 'Red Headed Stranger' — a concept album the label hated — that became a #1. His guitar 'Trigger' has a hole worn through it. He wrote 'Crazy' for Patsy Cline.",
        "Willie Nelson — Red Headed Stranger (1975) end-to-end", 33,
        "Listen to 'On the Road Again' — the anthem about life on tour. Then 'Blue Eyes Crying in the Rain' — the stripped-down hit. 'Always on My Mind' — the cover that became his. Willie's voice is weathered and warm; at 90 he still tours and releases albums.",
        ["Country", "Outlaw Country", "American", "1970s"], 1
    ),
    (
        "artist-hank-williams", "Artist",
        "Hank Williams",
        "The hillbilly Shakespeare — he wrote songs in 10 minutes that people still sing. He died at 29 in the back of his Cadillac, on the way to a show he'd been fired from for drinking. 'Your Cheatin' Heart' — written during a hangover — is one of the most covered songs ever.",
        "Hank Williams — 40 Greatest Hits (compilation) end-to-end", 55,
        "Listen to 'Your Cheatin' Heart' — the hurt-soaked classic. Then 'I'm So Lonesome I Could Cry' — the song Bob Dylan called 'the saddest song.' 'Hey Good Lookin'' — the joyful one. Hank was the first country superstar; his son Hank Jr. and grandson carry the name.",
        ["Country", "Honky-Tonk", "American", "1940s"], 1
    ),
    (
        "artist-patsy-cline", "Artist",
        "Patsy Cline",
        "The voice that brought country to the pop charts. 'Crazy' — written by a young Willie Nelson — was her signature. She died in a plane crash at 30, weeks after her final recording session. She was the first woman inducted into the Country Music Hall of Fame.",
        "Patsy Cline — Showcase with the Jordanaires (1961) end-to-end", 33,
        "Listen to 'Crazy' — the song Willie Nelson wrote that became hers. Then 'I Fall to Pieces' — the follow-up hit. 'Sweet Dreams' — her last recording, released after she died. 'Walking After Midnight' — the crossover that started it.",
        ["Country", "Nashville Sound", "American", "1960s"], 2
    ),
    (
        "artist-loretta-lynn", "Artist",
        "Loretta Lynn",
        "The 'Coal Miner's Daughter' — she was married at 15 and had four kids by 21. Her song 'The Pill' — about birth control — was banned by many radio stations. She wrote 'Coal Miner's Daughter' about her Kentucky childhood; it became a book and a movie.",
        "Loretta Lynn — Coal Miner's Daughter (1970) end-to-end", 30,
        "Listen to 'Coal Miner's Daughter' — the autobiographical classic. Then 'The Pill' — the scandalous hit about birth control. 'You Ain't Woman Enough' — the song that told a rival to back off. Loretta was the first woman named Entertainer of the Year at the CMAs.",
        ["Country", "American", "1970s"], 2
    ),
    (
        "artist-merle-haggard", "Artist",
        "Merle Haggard",
        "The 'Poet of the Common Man' — he spent time in San Quentin Prison, where he saw Johnny Cash perform. That concert inspired him to go straight and become a country star. 'Okie from Muskogee' — about hippie-hating rednecks — divided the country.",
        "Merle Haggard — Mama Tried (1968) end-to-end", 29,
        "Listen to 'Mama Tried' — the song about a mother's failed attempts to save her son. Then 'Okie from Muskogee' — the anthem that divided the country. 'The Fightin' Side of Me' — the defiant one. Haggard's prison years shaped the outlaw in his music.",
        ["Country", "Bakersfield Sound", "American", "1960s"], 2
    ),
    (
        "artist-kacey-musgraves", "Artist",
        "Kacey Musgraves",
        "The Texas singer who brought progressive themes to country. 'Follow Your Arrow' — about acceptance — won Song of the Year at the CMAs. 'Golden Hour' won Album of the Year at the Grammys, crossing over to pop. 'Same Trailer Different Park' was her celebrated debut.",
        "Kacey Musgraves — Golden Hour (2018) end-to-end", 46,
        "Listen to 'Follow Your Arrow' — the song that tells you to live your truth. Then 'Space Cowboy' — the psychedelic breakup song. 'Slow Burn' — the opener that sets the mood. Musgraves makes country that sounds like a dream; her voice is pure Texas honey.",
        ["Country", "American", "2010s"], 2
    ),

    # ── BLUES ──
    (
        "artist-bb-king", "Artist",
        "B.B. King",
        "The King of the Blues, who named his guitar 'Lucille' after a woman in a club fire — he ran back into the burning building to save it. He recorded over 40 albums and played 15,000 concerts. 'The Thrill Is Gone' is his most famous song. He was born on a Mississippi plantation.",
        "B.B. King — Live at the Regal (1965) end-to-end", 35,
        "Listen to 'The Thrill Is Gone' — the song that won him his first Grammy. Then 'Lucille' — the instrumental tribute to his guitar. 'Sweet Little Angel' — the live favorite. B.B.'s style — single notes bent with vibrato — defined electric blues guitar.",
        ["Blues", "Electric Blues", "American", "1950s"], 1
    ),
    (
        "artist-howlin-wolf", "Artist",
        "Howlin' Wolf",
        "Born Chester Burnett — 6'3\" and 300 pounds, with a voice like gravel and thunder. He learned guitar from Charley Patton, the father of Delta blues. 'Smokestack Lightning' is his signature. The Rolling Stones covered him — he was their favorite bluesman.",
        "Howlin' Wolf — Moanin' in the Moonlight (1959) end-to-end", 38,
        "Listen to 'Smokestack Lightning' — the hypnotic one-chord epic. Then 'Spoonful' — the song about desire. 'Killing Floor' — the track Led Zeppelin borrowed for 'The Lemon Song.' Wolf's band shook the stage; he called his style 'the howling blues.'",
        ["Blues", "Chicago Blues", "American", "1950s"], 1
    ),
    (
        "artist-john-lee-hooker", "Artist",
        "John Lee Hooker",
        "The 'Hook' — he played a boogie so personal that no band could follow him. 'Boogie Chillen' — recorded alone in 1948 — was his first hit and one of the first million-selling blues records. 'Boom Boom' became a barroom anthem. He sometimes recorded in hotel rooms.",
        "John Lee Hooker — The Healer (1989) end-to-end", 44,
        "Listen to 'Boogie Chillen' — the one-man boogie that started it all. Then 'Boom Boom' — the song that took over bars. 'One Bourbon, One Scotch, One Beer' — the spoken-word epic. Hooker's voice talks over his guitar like a late-night conversation.",
        ["Blues", "Boogie", "American", "1940s"], 2
    ),
    (
        "artist-etta-james", "Artist",
        "Etta James",
        "The singer behind 'At Last' — the wedding song that became a standard. Her voice could be a whisper or a roar. She struggled with addiction and was inducted into the Rock and Roll Hall of Fame in 1993. 'I'd Rather Go Blind' is her rawest song.",
        "Etta James — At Last! (1960) end-to-end", 37,
        "Listen to 'At Last' — the song that defined her. Then 'I'd Rather Go Blind' — the song she recorded while heartbroken. 'Tell Mama' — the gritty soul favorite. Etta's voice was gospel, blues, and soul in one body — she lived every lyric.",
        ["Blues", "Soul", "American", "1960s"], 2
    ),
    (
        "artist-buddy-guy", "Artist",
        "Buddy Guy",
        "The Chicago blues guitarist who played with his teeth and walked through audiences on tables. He was mentored by Muddy Waters and in turn inspired Jimi Hendrix and Eric Clapton — Clapton called him 'the greatest guitarist alive.' 'Damn Right, I've Got the Blues' won him a Grammy.",
        "Buddy Guy — Damn Right, I've Got the Blues (1991) end-to-end", 46,
        "Listen to 'Damn Right, I've Got the Blues' — the comeback album that won him a Grammy. Then 'Stone Crazy' — the frantic showpiece. 'Feels Like Rain' — the soulful one. Buddy's solos are conversations — they start quiet and shout by the end. He's still playing at 80+.",
        ["Blues", "Chicago Blues", "American", "1990s"], 2
    ),

    # ── CLASSICAL ──
    (
        "artist-mozart", "Artist",
        "Wolfgang Amadeus Mozart",
        "The child prodigy who composed his first symphony at 8 and wrote over 600 works before dying at 35. His unfinished 'Requiem' — which he believed was for his own funeral — was completed by a student. The Salieri murder legend is a myth.",
        "Mozart — Requiem in D minor (1791)", 55,
        "Listen to the 'Requiem' — the masterpiece he never finished, and his final artistic statement. Then 'Eine kleine Nachtmusik' — the serenade everyone knows. 'The Magic Flute' — the opera with the famous Queen of the Night aria. Mozart's music is joyful even when it's sad.",
        ["Classical", "Classical Period", "Austrian", "1780s"], 1
    ),
    (
        "artist-chopin", "Artist",
        "Frédéric Chopin",
        "The Polish pianist who wrote almost exclusively for the piano. He fled Warsaw during the Russian uprising at 20 and spent his life in Paris, giving only about 30 public concerts — he preferred salons. He was frail and died at 39; his heart is buried in Poland.",
        "Chopin — Nocturnes (1830s-1840s)", 60,
        "Listen to 'Nocturne Op. 9 No. 2' — the piece everyone recognizes. Then the 'Revolutionary Étude' — written when he heard Warsaw had fallen. 'Ballade No. 1' — the emotional tour de force. Chopin's music is the piano's greatest literature.",
        ["Classical", "Romantic", "Polish", "1830s"], 1
    ),
    (
        "artist-debussy", "Artist",
        "Claude Debussy",
        "The French composer who painted with sound — 'Clair de Lune' is his moonlit masterpiece. He hated being called an impressionist, preferring 'symbolist.' 'La Mer' — his orchestral seascape — took years to finish. He once said 'music is the space between the notes.'",
        "Debussy — Suite Bergamasque (1890) end-to-end", 20,
        "Listen to 'Clair de Lune' — the third movement of 'Suite Bergamasque,' the piano piece everyone knows. Then 'La Mer' — the orchestral ocean. 'Prélude à l'après-midi d'un faune' — the revolutionary tone poem. Debussy's harmonies dissolve like watercolors.",
        ["Classical", "Impressionist", "French", "1890s"], 2
    ),
    (
        "artist-stravinsky", "Artist",
        "Igor Stravinsky",
        "The composer whose 'Rite of Spring' caused a riot at its 1913 premiere — the audience fought in the aisles over the ballet's dissonance. He reinvented himself repeatedly: Russian ballets, neoclassicism, serialism. He moved to the US and became a citizen. He lived to 88.",
        "Stravinsky — The Rite of Spring (1913)", 35,
        "Listen to 'The Rite of Spring' — the opening bassoon solo is a high note no one expected. Then 'The Firebird' — the ballet that made his name. 'Petrushka' — the puppet drama. Stravinsky said his music 'survived the scandal' — it now fills concert halls.",
        ["Classical", "20th Century", "Russian", "1910s"], 2
    ),
    (
        "artist-philip-glass", "Artist",
        "Philip Glass",
        "The father of minimalism — he drove a taxi and worked as a plumber before his music caught on. 'Einstein on the Beach' — his five-hour opera with no intermission — made him famous. He has scored films including 'Koyaanisqatsi' and 'The Hours.'",
        "Philip Glass — Glassworks (1982) end-to-end", 50,
        "Listen to 'Glassworks' — the album that introduced minimalism to the public. Then 'Koyaanisqatsi' — the hypnotic film score. 'Einstein on the Beach' — the opera where time stands still. Glass's repeating patterns build like machines; critics hated it in the 70s, now it's canon.",
        ["Classical", "Minimalism", "American", "1970s"], 2
    ),

    # ── 2020s POP / INDIE ──
    (
        "artist-olivia-rodrigo", "Artist",
        "Olivia Rodrigo",
        "The Disney star whose 'drivers license' broke Spotify's record for most streams in a week. She wrote her debut 'Sour' with producer Dan Nigro while still a teenager. 'Good 4 U' and 'Vampire' followed. Her albums turn heartbreak into chart-topping confessions.",
        "Olivia Rodrigo — Sour (2021) end-to-end", 35,
        "Listen to 'drivers license' — the song that made everyone cry in 2021. Then 'Vampire' — the piano ballad that became her second #1. 'get him back!' — the chaotic rocker. Rodrigo writes with brutal honesty — her diary entries are hits.",
        ["Pop", "Alternative Pop", "American", "2020s"], 1
    ),
    (
        "artist-charli-xcx", "Artist",
        "Charli XCX",
        "The hyperpop queen who turned 'Brat' into the summer of 2024 — its lime-green aesthetic dominated everything. She started performing at illegal London raves as a teen. 'Boom Clap' was her first big hit; '360' defined a moment. She also writes hits for other stars.",
        "Charli XCX — Brat (2024) end-to-end", 41,
        "Listen to 'Von dutch' — the brat-era banger. Then '360' — the anthem about being that girl. 'Boys' — the video full of famous men. Charli is the most collaborative artist in pop — her remixes and features are events.",
        ["Pop", "Hyperpop", "Electropop", "British", "2020s"], 1
    ),
    (
        "artist-rosalia", "Artist",
        "Rosalía",
        "The Catalan singer who rebuilt flamenco for the 21st century — 'El Mal Querer' won a Latin Grammy and a Grammy. 'Motomami' — her third album — is a genre-defying masterpiece. She collaborates with everyone from Bad Bunny to The Weeknd and still sings in Spanish.",
        "Rosalía — Motomami (2022) end-to-end", 42,
        "Listen to 'Malamente' — the flamenco-trap fusion that announced her. Then 'Despechá' — the summer anthem. 'Con Altura' — the reggaeton hit with J Balvin. Rosalía's voice is pure flamenco, her production is pure future.",
        ["Pop", "Flamenco", "Latin Pop", "Spanish", "2010s"], 2
    ),
    (
        "artist-bad-bunny", "Artist",
        "Bad Bunny",
        "The Puerto Rican superstar who has been Spotify's most-streamed artist multiple years in a row. He started as a supermarket bagger who posted songs to SoundCloud. 'YHLQMDLG' — his 2020 album — became the highest-charting all-Spanish album in US history.",
        "Bad Bunny — YHLQMDLG (2020) end-to-end", 66,
        "Listen to 'Yo Perreo Sola' — the feminist anthem with the drag video. Then 'Dákiti' — the global smash. 'Tití Me Preguntó' — the banger about being asked about marriage. Bad Bunny turned reggaeton into the world's most-played music.",
        ["Pop", "Reggaeton", "Puerto Rican", "2020s"], 1
    ),
    (
        "artist-chappell-roan", "Artist",
        "Chappell Roan",
        "The Midwest Princess — her drag-inspired, hyper-theatrical pop made her 2024's breakout star. 'Good Luck, Babe!' was her first top-10 hit. She developed her persona after moving to LA and dropping out of music school. Her shows are pop operas with full choreography.",
        "Chappell Roan — The Rise and Fall of a Midwest Princess (2023) end-to-end", 55,
        "Listen to 'Good Luck, Babe!' — the song about doomed love that became a phenomenon. Then 'Pink Pony Club' — the anthem about finding yourself. 'Femininomenom' — the rallying cry. Roan's rise took years — she was dropped by her first label and rebuilt herself.",
        ["Pop", "Synth-Pop", "American", "2020s"], 1
    ),

    # ═══════════════════════════════════════════════════════════════════════
    # BATCH 6 — ~130 artists: rock legends, punk/post-punk, pop icons,
    # funk/soul, hip-hop, R&B/neo-soul, folk, shoegaze, metal, country,
    # reggae/Afrobeats, EDM, bossa nova, Latin, world, blues, film scores, K-pop
    # ═══════════════════════════════════════════════════════════════════════

    # ── ROCK LEGENDS ──
    (
        "artist-led-zeppelin", "Artist",
        "Led Zeppelin",
        "Jimmy Page bought the house that once belonged to occultist Aleister Crowley. 'Stairway to Heaven' was never released as a single — the band refused, insisting you had to hear the whole build. Plant wrote its lyrics in an afternoon at Headley Grange, and Page's solo was improvised in one take.",
        "Led Zeppelin — IV (1971) end-to-end", 42,
        "Listen to 'Stairway to Heaven' with the volume rising — Page built it as three movements: acoustic folk, electric interlude, hard-rock climax. Then 'When the Levee Breaks' — Bonham's drum part was recorded in a hallway with two mics; it's still the most-sampled drum break in hip-hop.",
        ["Rock", "Hard Rock", "British", "1970s"], 1
    ),
    (
        "artist-queen", "Artist",
        "Queen",
        "Freddie Mercury was born Farrokh Bulsara in Zanzibar and worked as an airport baggage handler before fame. He had four extra molars, which he said gave his voice its unique range. 'Bohemian Rhapsody' took three weeks to record — over 180 vocal overdubs stacked into that operatic section.",
        "Queen — A Night at the Opera (1975) end-to-end", 43,
        "Listen to 'Bohemian Rhapsody' all the way through without skipping — the operatic middle was recorded in sections by three different studios. Then 'Somebody to Love' — Freddie's gospel homage. Notice how the band never repeated a formula; every Queen album experiments somewhere.",
        ["Rock", "Glam Rock", "British", "1970s"], 1
    ),
    (
        "artist-rolling-stones", "Artist",
        "The Rolling Stones",
        "Keith Richards has played the same battered guitar, 'Micawber,' since 1964 — it's tuned to open G, giving the Stones their signature strut. The 'Satisfaction' riff came to him in his sleep; he recorded it on a cassette player by his bed before it vanished. The band named itself after a Muddy Waters song.",
        "The Rolling Stones — Exile on Main St. (1972) end-to-end", 67,
        "Listen to 'Exile on Main St.' at night — it was recorded in a sweltering French villa basement. Then 'Gimme Shelter' — Merry Clayton's backing vocal is so intense she reportedly lost her voice and miscarried that week; it's one of rock's most chilling performances.",
        ["Rock", "Blues Rock", "British", "1960s"], 1
    ),
    (
        "artist-the-who", "Artist",
        "The Who",
        "Pete Townshend smashed his first guitar by accident when its neck hit a low ceiling — and the crowd loved it, so he kept doing it. 'My Generation' featured stuttering lyrics to capture the energy of mods on amphetamines. Keith Moon was once voted the greatest drummer in rock by fans and journalists.",
        "The Who — Who's Next (1971) end-to-end", 43,
        "Listen to 'Baba O'Riley' — the synth loop was Townshend improvising on a Lowrey organ, and the violin solo was played by Dave Arbus, a folk fiddler. Then 'Won't Get Fooled Again' — the song ends with Moon's famous drum break, recorded in a single take.",
        ["Rock", "Mod", "British", "1960s"], 1
    ),
    (
        "artist-jimi-hendrix", "Artist",
        "Jimi Hendrix",
        "Hendrix played a right-handed guitar flipped upside down as a lefty, so his chord voicings sound like no one else's. He was a paratrooper in the 101st Airborne before music. When Dylan heard Hendrix's 'All Along the Watchtower,' he reportedly said 'It's his song now.'",
        "Jimi Hendrix — Are You Experienced (1967) end-to-end", 40,
        "Listen to 'Purple Haze' — the famous opening is actually a mistake: the intro chord was never written down. Then 'Little Wing' — just 2 minutes, but it spawned the most covered guitar style in history. Hendrix played it with his amp on full, controlling feedback like a singer controls vibrato.",
        ["Rock", "Psychedelic Rock", "American", "1960s"], 1
    ),
    (
        "artist-fleetwood-mac", "Artist",
        "Fleetwood Mac",
        "'Rumours' was recorded while every band member was going through a breakup — with each other. Stevie Nicks wrote 'Dreams' in ten minutes, sitting on a chair in the studio while Lindsey Buckingham argued nearby. The band's name comes from drummer Mick Fleetwood and bassist John McVie.",
        "Fleetwood Mac — Rumours (1977) end-to-end", 40,
        "Listen to 'The Chain' — the only song on Rumours written by all five members, and its famous bass riff was actually recorded as a separate jam. Then 'Never Going Back Again' — one acoustic guitar, double-tracked, with Lindsey's fingers bleeding from the complex picking.",
        ["Rock", "Soft Rock", "American-British", "1970s"], 1
    ),
    (
        "artist-the-doors", "Artist",
        "The Doors",
        "The band took its name from Aldous Huxley's book 'The Doors of Perception.' Jim Morrison died in Paris at 27 and is buried at Père Lachaise — his grave is one of the most visited in the world. 'Light My Fire' was written by guitarist Robby Krieger, not Morrison, who was initially unsure about it.",
        "The Doors — The Doors (1967) end-to-end", 44,
        "Listen to 'The End' — it began as a goodbye-to-girlfriend song and grew into an Oedipal epic that Morrison stretched live for 15 minutes. Then 'Light My Fire' — Ray Manzarek's organ solo and the band refusing to cut it for radio, only relenting when the label begged.",
        ["Rock", "Psychedelic Rock", "American", "1960s"], 2
    ),
    (
        "artist-ccr", "Artist",
        "Creedence Clearwater Revival",
        "John Fogerty wrote and sang nearly everything, including the draft-exemption song 'Fortunate Son' — about rich kids dodging Vietnam while poor kids went. Fogerty himself was drafted and served in the Army Reserve while CCR was topping charts. 'Proud Mary' was written in twenty minutes.",
        "Creedence Clearwater Revival — Cosmo's Factory (1970) end-to-end", 41,
        "Listen to 'Fortunate Son' — the only political song that still sounds fun. Then 'Run Through the Jungle' and 'Who'll Stop the Rain' back to back — both about Vietnam, both disguised as swamp rock. Fogerty once said his lyrics hid the politics in plain sight.",
        ["Rock", "Swamp Rock", "American", "1960s"], 2
    ),
    (
        "artist-eagles", "Artist",
        "Eagles",
        "'Hotel California' — Don Felder brought a demo with just the chord progression and everyone wrote around it. The band's Greatest Hits (1971–1975) became the best-selling album of the 20th century in the US. The line 'you can check out any time you like, but you can never leave' is about the music industry itself.",
        "Eagles — Hotel California (1976) end-to-end", 43,
        "Listen to the dual-guitar outro of 'Hotel California' — Felder and Joe Walsh trade solos live in concert with no rehearsal. Then 'New Kid in Town' — a song about how fast fame fades. The album's title track was recorded in sections across three studios.",
        ["Rock", "Country Rock", "American", "1970s"], 2
    ),
    (
        "artist-ac-dc", "Artist",
        "AC/DC",
        "Angus Young's schoolboy uniform was his sister Margaret's idea — and he still wears it in his 60s. 'Back in Black' was written to honor singer Bon Scott, who died of alcohol poisoning; it became the second-best-selling album in history. Malcolm Young played the riff before the band knew it was a song.",
        "AC/DC — Back in Black (1980) end-to-end", 42,
        "Listen to the opening riff of 'Back in Black' at full volume — Malcolm Young said it was written on a couch with no guitar in hand. Then 'You Shook Me All Night Long' — Brian Johnson's first-ever vocal take with the band was kept. The album was recorded in the Bahamas in 70 days.",
        ["Rock", "Hard Rock", "Australian", "1980s"], 2
    ),
    (
        "artist-van-halen", "Artist",
        "Van Halen",
        "Eddie Van Halen never had a guitar lesson — he learned by watching players and playing classical piano pieces by ear. His contract famously demanded brown M&Ms backstage: a test to see if venues read the technical rider. 'Eruption' was a mistake he insisted on keeping on the album.",
        "Van Halen — Van Halen (1978) end-to-end", 36,
        "Listen to 'Eruption' — recorded in one take after a rehearsal, it's the song that made tapping guitar technique famous overnight. Then 'Ain't Talkin' 'bout Love' — Eddie's rhythm riff is three chords played with syncopation so tight it sounds like four.",
        ["Rock", "Hard Rock", "American", "1970s"], 2
    ),
    (
        "artist-guns-n-roses", "Artist",
        "Guns N' Roses",
        "'Sweet Child o' Mine' started as a Slash string-skip warm-up exercise — the band heard it and built the song around it. Axl Rose's real name is William Bruce Rose Jr. 'Appetite for Destruction' remains the best-selling debut album in US history at over 30 million copies.",
        "Guns N' Roses — Appetite for Destruction (1987) end-to-end", 54,
        "Listen to the intro of 'Sweet Child o' Mine' three times: once for Slash's riff, once for the bass, once for Axl's falsetto. Then 'Paradise City' — the song ends with a key change the band didn't plan; it just kept going in the studio and stuck.",
        ["Rock", "Hard Rock", "American", "1980s"], 2
    ),
    (
        "artist-red-hot-chili-peppers", "Artist",
        "Red Hot Chili Peppers",
        "Flea was a jazz trumpet player who switched to bass after seeing punk shows. The band's early gigs featured the members in nothing but socks — a protest against LA's conservative club scene. 'Under the Bridge' was written about Anthony Kiedis's loneliness and addiction, not romance.",
        "Red Hot Chili Peppers — Californication (1999) end-to-end", 56,
        "Listen to 'Scar Tissue' — the slide guitar is John Frusciante returning from near-fatal addiction. Then 'Otherside' — the song is about struggling with heroin, disguised as a melody. The album was recorded in a Malibu mansion, and the band re-learned how to play together on it.",
        ["Rock", "Funk Rock", "American", "1990s"], 2
    ),
    (
        "artist-pearl-jam", "Artist",
        "Pearl Jam",
        "Eddie Vedder was a gas-station attendant who mailed demo tapes to the band and was flown in for an audition. 'Jeremy' is based on a real 1991 school shooting in Texas. The band spent years fighting Ticketmaster over service fees, nearly killing their own touring business.",
        "Pearl Jam — Ten (1991) end-to-end", 53,
        "Listen to 'Alive' — the guitar solo is Mike McCready quoting Jimi Hendrix and Neil Young in one take. Then 'Black' — Vedder recorded the vocal in one take and refused to redo it. The album's name 'Ten' was bassist Jeff Ament's jersey number, worn playing basketball.",
        ["Rock", "Grunge", "American", "1990s"], 2
    ),
    (
        "artist-rem", "Artist",
        "R.E.M.",
        "Michael Stipe's early lyrics were indecipherable — he sang in a mumble because he was too shy to write real words. The band's name was chosen from a dictionary; it means rapid eye movement. 'Losing My Religion' is built on a mandolin and became one of the most-played songs of the 1990s.",
        "R.E.M. — Automatic for the People (1992) end-to-end", 49,
        "Listen to 'Nightswimming' — recorded with just piano, bass, strings, and Stipe's voice, in one room. Then 'Everybody Hurts' — the band insisted on a slow song about kindness after realizing no one else was writing one. Notice how the album never once uses a drum machine.",
        ["Rock", "Alternative Rock", "American", "1990s"], 2
    ),
    (
        "artist-u2", "Artist",
        "U2",
        "Bono's real name is Paul Hewson — 'Bono' came from a hearing-aid shop called Bonavox in Dublin. The band formed in 1976 when drummer Larry Mullen posted a note on his school bulletin board asking for musicians. 'Where the Streets Have No Name' was filmed on a rooftop in downtown LA.",
        "U2 — The Joshua Tree (1987) end-to-end", 51,
        "Listen to 'With or Without You' — the bassline was played by The Edge as an afterthought while the band argued in the studio. Then 'Where the Streets Have No Name' — recorded 21 times because the band kept overthinking it. The Edge's real name is Dave Evans; his delay pedals are half the sound.",
        ["Rock", "Alternative Rock", "Irish", "1980s"], 2
    ),

    # ── PUNK / POST-PUNK / NEW WAVE ──
    (
        "artist-ramones", "Artist",
        "The Ramones",
        "All four original members took the stage name Ramone — none of them were related. Joey was born Jeffrey Hyman, Dee Dee was born Douglas Colvin, and Tommy was born Tamás Erdélyi. The band played songs under two minutes, dressed in identical leather jackets, and influenced nearly every punk band that followed.",
        "The Ramones — Ramones (1976) end-to-end", 30,
        "Listen to the entire 14-track debut in one sitting — it's under 30 minutes, one of the shortest great albums ever. Then 'Blitzkrieg Bop' — that 'Hey! Ho! Let's go!' chant was written by manager Tom Erdelyi. The band stayed on the road for 22 years without a single hit.",
        ["Punk", "American", "1970s"], 2
    ),
    (
        "artist-the-clash", "Artist",
        "The Clash",
        "The only punk band that mattered, and they meant it: they signed to CBS with the proviso they could keep making political records. 'London Calling' was recorded in a converted piano factory with a broken heating system. Their guitarist Mick Jones collected records the way others collect stamps.",
        "The Clash — London Calling (1979) end-to-end", 65,
        "Listen to 'London Calling' — the title song's bassline is a Bo Diddley beat played by Paul Simonon. The cover photo is bassist Simonon smashing his bass on stage — the photographer caught it by luck. The album mixes punk, reggae, rockabilly, and jazz in 19 songs.",
        ["Punk", "Post-Punk", "British", "1970s"], 2
    ),
    (
        "artist-sex-pistols", "Artist",
        "Sex Pistols",
        "They released just one studio album — 'Never Mind the Bollocks' — and changed the world anyway. Sid Vicious was hired for his look and famously struggled to play bass. The band's 'God Save the Queen' was banned by the BBC and still reached #2 in the UK.",
        "Sex Pistols — Never Mind the Bollocks (1977) end-to-end", 38,
        "Listen to 'Anarchy in the UK' — the song that made the Sex Pistols famous before they even released an album. Then 'Pretty Vacant' — the band's punk anthem. Notice the bass: guitarist Steve Jones played most of it, because bassist Glen Matlock had left before the album was finished.",
        ["Punk", "British", "1970s"], 3
    ),
    (
        "artist-iggy-pop", "Artist",
        "Iggy Pop",
        "The Godfather of punk — he crawled on broken glass at concerts and smeared himself in peanut butter. His band The Stooges' 'Raw Power' was mixed by David Bowie, who reportedly sat on the faders until the band liked it. Iggy studied to be a drummer before ever singing.",
        "The Stooges — Raw Power (1973) end-to-end", 34,
        "Listen to 'Search and Destroy' — the opening riff is a blues riff sped up until it becomes punk. Then 'Gimme Danger' — a rare tender moment in the Stooges catalog. Iggy's vocals were recorded while he was genuinely unhinged; the band recorded the album in London in two weeks.",
        ["Punk", "Proto-Punk", "American", "1970s"], 2
    ),
    (
        "artist-joy-division", "Artist",
        "Joy Division",
        "Ian Curtis wrote 'Love Will Tear Us Apart' about his failing marriage while suffering from epilepsy and severe depression; he died by suicide at 23, days before the band's first US tour. Their name came from the 'joy divisions' — the name given in Holocaust literature to camp sections where prisoners were forced into sexual servitude.",
        "Joy Division — Unknown Pleasures (1979) end-to-end", 39,
        "Listen to 'Disorder' — the hypnotic, slightly off-kilter drum pattern and bassline open an album with a sound no one had heard before. The cover's white pulses are radio waves from a pulsar — the band found the image in an astronomy book. Every post-punk band you love started here.",
        ["Post-Punk", "British", "1970s"], 2
    ),
    (
        "artist-new-order", "Artist",
        "New Order",
        "Formed from the ashes of Joy Division after Ian Curtis's death, they accidentally invented the sound of the 80s: melancholy guitar over dance beats. 'Blue Monday' is the best-selling 12-inch single of all time. Its cover artwork was designed by the band's Peter Saville — and cost more to produce than the band made from it.",
        "New Order — Power, Corruption & Lies (1983) end-to-end", 43,
        "Listen to 'Blue Monday' — the drum pattern comes from Kraftwerk's 'Uranium' and a borrowed sequencer. Then 'Your Silent Face' — the band's most beautiful melody, written while they were still learning how to be a band without Curtis. The transition from post-punk to dance music happens in this album.",
        ["New Wave", "Synth-Pop", "British", "1980s"], 2
    ),
    (
        "artist-the-cure", "Artist",
        "The Cure",
        "Robert Smith's signature look — smeared lipstick and a bird's-nest haircut — began by accident when his makeup bag opened in his luggage before a TV appearance. The band's name came from a photo caption Smith wrote. 'Friday I'm in Love' was written as a joke about writing a happy song.",
        "The Cure — Disintegration (1989) end-to-end", 72,
        "Listen to 'Pictures of You' — Smith wrote the lyrics from letters he kept after a house fire destroyed his home and photo albums. Then 'Lovesong' — a wedding present for his wife. 'Disintegration' was recorded in a haunted studio and the band felt the album was cursed; it's now considered their masterpiece.",
        ["New Wave", "Gothic Rock", "British", "1980s"], 2
    ),
    (
        "artist-the-smiths", "Artist",
        "The Smiths",
        "Morrissey and Johnny Marr wrote 'How Soon Is Now?' in the studio from a leftover riff — it was nearly left off the album. The Smiths' songs about celibacy, heartbreak, and being an outsider made them the most beloved cult band of the 80s. Marr played every guitar part; Morrissey wrote every lyric.",
        "The Smiths — The Queen Is Dead (1986) end-to-end", 36,
        "Listen to 'There Is a Light That Never Goes Out' — Morrissey's most famous lyric ('if a double-decker bus crashes into us'). Then 'Bigmouth Strikes Again' — the song where he jokes he's Joan of Arc. Notice how Marr's jangly guitar carries melody that Morrissey's words answer.",
        ["Indie Rock", "Jangle Pop", "British", "1980s"], 2
    ),
    (
        "artist-the-velvet-underground", "Artist",
        "The Velvet Underground",
        "Their debut sold terribly but influenced everything — Brian Eno famously said only 30,000 people bought it, but they all started bands. The band was managed by Andy Warhol, who put them with Nico. Lou Reed and John Cale's avant-garde ideas met rock and created art rock.",
        "The Velvet Underground & Nico (1967) end-to-end", 48,
        "Listen to 'Heroin' — the song is in one chord, and the guitar feedback builds as the drums speed up, mirroring the drug's rush. Then 'Venus in Furs' — the viola is played with a bow like a drone. 'Sunday Morning' was written as an afterthought when the label demanded a gentle single.",
        ["Art Rock", "Proto-Punk", "American", "1960s"], 2
    ),
    (
        "artist-pixies", "Artist",
        "Pixies",
        "Kurt Cobain admitted he was basically ripping off the Pixies' loud-quiet-loud dynamic for 'Smells Like Teen Spirit.' Black Francis wrote 'Where Is My Mind?' about watching a tiny submarine pilot navigate — the song became a legend after 'Fight Club.' The band broke up mid-career and reunited 10 years later.",
        "Pixies — Doolittle (1989) end-to-end", 39,
        "Listen to 'Debaser' — inspired by the surrealist film 'Un Chien Andalou' (a razor cutting an eye). Then 'Monkey Gone to Heaven' — about environmental collapse disguised as a singalong. Kim Deal's bass drives every song; Francis and Deal singing together is the Pixies sound.",
        ["Alternative Rock", "American", "1980s"], 2
    ),

    # ── POP ICONS ──
    (
        "artist-michael-jackson", "Artist",
        "Michael Jackson",
        "The King of Pop — he started performing at age 5 with his brothers in the Jackson 5, singing 'ABC' with the family. 'Thriller' remains the best-selling album of all time, and its 14-minute zombie video changed music videos forever. He also bought the Beatles' catalog and owned it for decades.",
        "Michael Jackson — Thriller (1982) end-to-end", 42,
        "Listen to 'Billie Jean' — the bassline is the most recognizable in pop history, played by session legend Louis Johnson on a Yamaha. Then 'Human Nature' — the song Quincy Jones almost rejected. Notice how Jackson's vocals layer in a way that's impossible to recreate — he recorded each harmony himself.",
        ["Pop", "R&B", "American", "1980s"], 1
    ),
    (
        "artist-madonna", "Artist",
        "Madonna",
        "She arrived in New York with $35 in her pocket and took a cab to Times Square — then called home to say she was okay. 'Like a Prayer' — the video with burning crosses got her condemned by the Vatican and made the song her biggest hit. She choreographed her own videos because she knew every step.",
        "Madonna — Like a Prayer (1989) end-to-end", 51,
        "Listen to the title track — the gospel choir, the church organ, the percussion: it's a pop song built like a religious service, and it was written with Patrick Leonard in hours. Then 'Vogue' — the house beat and the spoken 'voguing' instructions came from Harlem ballroom culture.",
        ["Pop", "Dance", "American", "1980s"], 1
    ),
    (
        "artist-whitney-houston", "Artist",
        "Whitney Houston",
        "Whitney's mother Cissy and cousin Dionne Warwick — she grew up surrounded by gospel royalty. Her version of 'The Star-Spangled Banner' at the 1991 Super Bowl is considered one of the greatest national anthems ever sung. 'I Will Always Love You' was originally a Dolly Parton country song.",
        "Whitney Houston — The Bodyguard soundtrack (1992) end-to-end", 57,
        "Listen to 'I Will Always Love You' — she held the note 'and I' for nearly 12 seconds, and the record label fought to keep it. Then 'I Wanna Dance with Somebody' — a gospel-trained voice singing pure pop joy. Her vocal range was often called 'the voice of a generation' for a reason.",
        ["Pop", "R&B", "American", "1980s"], 2
    ),
    (
        "artist-taylor-swift", "Artist",
        "Taylor Swift",
        "She wrote her first song at 12 after a bully took her computer away — the song was about wanting it back. 'Speak Now' was written entirely solo. She re-recorded her first six albums ('Taylor's Version') to own her masters — the most successful catalogue heist in pop history.",
        "Taylor Swift — Folklore (2020) end-to-end", 63,
        "Listen to 'Cardigan' — the whole album was written in quarantine without a single co-writer in the room; she worked with Aaron Dessner over voice memos. Then 'August' — the song's perspective shift mid-album is the point. Folklore proved she could abandon pop spectacle and still dominate.",
        ["Pop", "Indie Folk", "American", "2020s"], 1
    ),
    (
        "artist-adele", "Artist",
        "Adele",
        "She wrote 'Someone Like You' about her ex and performed it at the Grammys; the performance caused the iconic split-screen meme with Beyoncé's reaction. '21' sold over 30 million copies. Her vocal coach once told her she'd never be a singer — she took it as motivation.",
        "Adele — 21 (2011) end-to-end", 49,
        "Listen to 'Rolling in the Deep' — the song started as a stomp beat in her head and Paul Epworth turned it into a gospel-blues monster. Then 'Someone Like You' — just piano and voice, recorded live in one take. She'd already written the whole album; this one came last, fast, and from heartbreak.",
        ["Pop", "Soul", "British", "2010s"], 2
    ),
    (
        "artist-rihanna", "Artist",
        "Rihanna",
        "A military officer discovered her in Barbados at 15 and flew her to New York to audition — she signed within days. 'Umbrella' was offered to Britney Spears and Mary J. Blige first. She became a billionaire off Fenty Beauty, making her music a side project by choice.",
        "Rihanna — Anti (2016) end-to-end", 44,
        "Listen to 'Work' — the dancehall beat and the way the hook syllables run together made it unplayable on American radio at first, then undeniable. Then 'Love on the Brain' — a doo-wop ballad that sounds like a 1950s throwback. Anti was worth the four-year wait because she only released it when it felt right.",
        ["Pop", "R&B", "Barbadian", "2010s"], 2
    ),
    (
        "artist-lady-gaga", "Artist",
        "Lady Gaga",
        "Before fame she performed in NYC clubs in a leotard and studied at NYU's Tisch School. 'Bad Romance' was written in 10 minutes on a piano. She's won an Oscar for 'Shallow' and became the first woman to win a Grammy, Oscar, BAFTA, and Golden Globe in one year.",
        "Lady Gaga — The Fame Monster (2009) end-to-end", 34,
        "Listen to 'Bad Romance' — the 'rah-rah-ah-ah-ah' hook came from a song about a broken heart and a phone call. Then 'Poker Face' — she's actually singing about her own bisexuality in the chorus. The album's 8 songs each represent a 'monster' she feared: fame, loneliness, fear of death.",
        ["Pop", "Dance-Pop", "American", "2000s"], 2
    ),
    (
        "artist-ariana-grande", "Artist",
        "Ariana Grande",
        "She started as a Nickelodeon actress and her 'thank u, next' album was written in two weeks after a public breakup. She's known for her whistle register — notes above the normal soprano range. '7 Rings' spent 8 weeks at #1 and sampled Rodgers & Hammerstein's 'My Favorite Things.'",
        "Ariana Grande — thank u, next (2019) end-to-end", 41,
        "Listen to 'thank u, next' — she name-drops her exes and turns pain into a pop triumph; the song's title is a perfect breakup philosophy. Then '7 Rings' — the whistle notes at the end are her signature. The whole album was recorded while she was grieving the Manchester attack.",
        ["Pop", "R&B", "American", "2010s"], 2
    ),
    (
        "artist-dua-lipa", "Artist",
        "Dua Lipa",
        "Her dad's family is from Kosovo and she moved to London at 15 to chase music. 'Future Nostalgia' was written during a creative retreat with her co-writers — the whole album is a love letter to disco, and it made her one of the UK's biggest pop exports of the 2020s.",
        "Dua Lipa — Future Nostalgia (2020) end-to-end", 37,
        "Listen to 'Don't Start Now' — the bassline is pure disco revival. Then 'Levitating' — the song was almost cut from the album until her label heard the demo. The album is 11 songs of pure dance-floor nostalgia, recorded with a live band to capture the disco feel.",
        ["Pop", "Dance-Pop", "British", "2020s"], 2
    ),
    (
        "artist-elton-john", "Artist",
        "Elton John",
        "He wrote 'Your Song' in under an hour, sitting at Bernie Taupin's lyrics on a Sunday. 'Candle in the Wind 1997' — rewritten for Princess Diana — is the best-selling single in history. He's one of the few artists with a top-40 hit in six different decades.",
        "Elton John — Goodbye Yellow Brick Road (1973) end-to-end", 76,
        "Listen to 'Funeral for a Friend/Love Lies Bleeding' — a 11-minute opener that was a piano-and-synth instrumental before it became a song. Then 'Candle in the Wind' — the original was about Marilyn Monroe. This double album's 17 tracks jump from glam to ballads to reggae.",
        ["Pop", "Glam Rock", "British", "1970s"], 2
    ),
    (
        "artist-bruce-springsteen", "Artist",
        "Bruce Springsteen",
        "The Boss — he earned the nickname by paying his band like it was a real job when no one else did. 'Born to Run' took six months to record because he obsessed over the wall of sound. 'Dancing in the Dark' was written as a hit on demand, and his 'Born in the U.S.A.' is often misunderstood as patriotic.",
        "Bruce Springsteen — Born to Run (1975) end-to-end", 39,
        "Listen to the title track — Springsteen wanted to capture the sound of Roy Orbison meeting Phil Spector's wall of sound. Then 'Thunder Road' — the harmonica intro was added in one take. The album's songs are all about escape, and the E Street Band's live versions stretch them to anthems.",
        ["Rock", "Heartland Rock", "American", "1970s"], 2
    ),
    (
        "artist-abba", "Artist",
        "ABBA",
        "The name is an acronym of the four members' first names: Agnetha, Björn, Benny, Anni-Frid. They won Eurovision 1974 with 'Waterloo' and never looked back. 'Dancing Queen' — written about the euphoria of a night out — was the only ABBA song to top the US chart.",
        "ABBA — Arrival (1976) end-to-end", 35,
        "Listen to 'Dancing Queen' — the piano intro was inspired by George McCrae's disco sound, and the song took four months to perfect. Then 'Money, Money, Money' — Benny's piano line is a pastiche of an old-time music hall song. Their harmonies were two married couples' voices, recorded separately.",
        ["Pop", "Disco", "Swedish", "1970s"], 2
    ),

    # ── FUNK / SOUL ──
    (
        "artist-james-brown", "Artist",
        "James Brown",
        "The Godfather of Soul — he counted 'on the one' (the first beat of the bar), creating funk's signature groove. 'Live at the Apollo' was recorded at a show where he nearly lost his voice. He once convinced a band to stay in the studio for hours to capture the perfect scream — that became 'Papa's Got a Brand New Bag.'",
        "James Brown — Live at the Apollo (1963) end-to-end", 32,
        "Listen to 'I Got You (I Feel Good)' — the sax riff is a call-and-response with his voice. Then 'Papa's Got a Brand New Bag' — Brown told the band to play 'on the one' and music changed forever. The live album was recorded in one night and sold over a million copies.",
        ["Funk", "Soul", "American", "1960s"], 1
    ),
    (
        "artist-sly-and-the-family-stone", "Artist",
        "Sly & the Family Stone",
        "The first integrated, multi-gender band in pop — Black and white, men and women, playing together on stage. 'Everyday People' preached unity and topped the charts. Sly Stone produced, wrote, and played nearly everything, and his drug addiction later derailed the band mid-flight.",
        "Sly & the Family Stone — There's a Riot Goin' On (1971) end-to-end", 47,
        "Listen to 'Family Affair' — the song's eerie feel comes from a drum machine called Maestro Rhythm King, which Sly used because he was too paranoid to play with the band. The album's title mocks the Riot album everyone expected. Its minimalist funk invented the sound of 70s soul.",
        ["Funk", "Soul", "American", "1960s"], 2
    ),
    (
        "artist-parliament-funkadelic", "Artist",
        "Parliament-Funkadelic",
        "George Clinton's funk empire: Parliament (the funk-science band) and Funkadelic (the psychedelic rock band) shared the same musicians and released different albums under two names. Their Mothership landed on stage at their concerts. 'Flash Light' was the first #1 song built around a synthesizer bassline.",
        "Funkadelic — Maggot Brain (1971) end-to-end", 37,
        "Listen to the title track — Eddie Hazel's 10-minute guitar solo was recorded in one take, after Clinton told him to play 'as if his mother had just died.' Then 'One Nation Under a Groove' — the band's biggest hit, a funk anthem about unity. This is the sound hip-hop sampled most.",
        ["Funk", "Psychedelic", "American", "1970s"], 2
    ),
    (
        "artist-earth-wind-fire", "Artist",
        "Earth, Wind & Fire",
        "Maurice White, the band's leader, played drums for Muddy Waters and was a session percussionist for Motown before starting the band. Their horn section and Philip Bailey's falsetto defined 70s soul. 'September' — written to be a chart hit — is still a wedding staple 50 years later.",
        "Earth, Wind & Fire — That's the Way of the World (1975) end-to-end", 37,
        "Listen to 'Shining Star' — the song was recorded in a studio next to a construction site, and the band kept the tape anyway because the take was perfect. Then 'That's the Way of the World' — the album was first a movie soundtrack. Their music blends R&B, jazz, and African rhythms.",
        ["Funk", "Soul", "American", "1970s"], 2
    ),
    (
        "artist-curtis-mayfield", "Artist",
        "Curtis Mayfield",
        "The gentle voice of Black power — his 'Superfly' soundtrack was the first album with socially conscious lyrics atop a funk-soul groove, and it sold millions. He was paralyzed after a stage accident in 1990 but kept writing. He was in the Impressions before going solo.",
        "Curtis Mayfield — Superfly (1972) end-to-end", 37,
        "Listen to 'Pusherman' — the song is from the perspective of a drug dealer, and Mayfield wrote it as a cautionary tale, not a celebration. Then 'Freddie's Dead' — the funk anthem about a street hustler. The album's wah-wah guitar and strings set the template for blaxploitation soundtracks.",
        ["Funk", "Soul", "American", "1970s"], 2
    ),
    (
        "artist-al-green", "Artist",
        "Al Green",
        "'Let's Stay Together' was written in minutes and became his signature. After a religious conversion in the 1970s, he gave up secular music and became a pastor — the church he now leads is his own. His voice blends gospel and soul so seamlessly that his love songs feel like prayers.",
        "Al Green — Let's Stay Together (1972) end-to-end", 35,
        "Listen to 'Let's Stay Together' — the song's signature 'I'm so in love with you' falsetto was improvised in the studio. Then 'Tired of Being Alone' — the song he wrote after being told he couldn't write hits. His producer Willie Mitchell built the band's grooves around Green's voice.",
        ["Soul", "Gospel", "American", "1970s"], 2
    ),
    (
        "artist-otis-redding", "Artist",
        "Otis Redding",
        "'Sittin' on the Dock of the Bay' was recorded just three days before the plane crash that killed him at 26 — he whistled the ending because he hadn't finished the lyrics. It became the first posthumous #1 single in US history. He wrote 'Respect' for Aretha Franklin... actually, he wrote it and she made it hers.",
        "Otis Redding — Otis Blue (1965) end-to-end", 34,
        "Listen to 'I've Been Loving You Too Long' — the song was written with Jerry Butler after a late-night session, and the live version on this album captures Otis at full power. Then 'Try a Little Tenderness' — it builds from a whisper to a scream. This album covers soul, blues, and R&B classics.",
        ["Soul", "Southern Soul", "American", "1960s"], 1
    ),
    (
        "artist-ray-charles", "Artist",
        "Ray Charles",
        "He went blind as a child and began playing piano by ear. He invented soul music by fusing gospel and R&B — his 'I Got a Woman' shocked church audiences because he took a gospel melody and sang about a lover. He was called 'The Genius' by everyone from Frank Sinatra on down.",
        "Ray Charles — Modern Sounds in Country and Western Music (1962) end-to-end", 37,
        "Listen to 'Georgia on My Mind' — written for a girl named Georgia, but it became the official state song of Georgia decades later. Then his version of 'I Can't Stop Loving You' — a country song turned soul classic. This album was the shock crossover that made country sound Black.",
        ["Soul", "R&B", "American", "1960s"], 1
    ),

    # ── HIP-HOP ──
    (
        "artist-lil-wayne", "Artist",
        "Lil Wayne",
        "He joined Cash Money Records at 11 years old and was the youngest member of the Hot Boys. His diamond-encrusted teeth grills became a hip-hop fashion signature. 'Tha Carter III' sold over a million copies in its first week.",
        "Lil Wayne — Tha Carter III (2008) end-to-end", 76,
        "Listen to 'A Milli' — Wayne recorded it on the spur of the moment over a beat that wasn't finished, and it became one of the most quoted rap songs ever. Then 'Lollipop' — the pop-rap crossover that shows his range. Wayne is also a rock guitarist who released a rock album, 'Rebirth.'",
        ["Hip-Hop", "Southern Rap", "American", "2000s"], 2
    ),
    (
        "artist-nicki-minaj", "Artist",
        "Nicki Minaj",
        "She was discovered through a mixtape and became the first female artist to have 100 songs on the Billboard Hot 100. 'Super Bass' — her breakthrough — proved a rapper with alter egos could top the global pop charts. Her characters, Roman Zolanski and Harajuku Barbie, are fully inhabited on record.",
        "Nicki Minaj — Pink Friday (2010) end-to-end", 69,
        "Listen to 'Super Bass' — the playful hook that became a worldwide pop moment. Then 'Moment 4 Life' — the fairytale duet with Drake. Nicki's verses are famous for switching accents and characters mid-song; listen for the moment she becomes 'Roman.'",
        ["Hip-Hop", "Pop Rap", "American", "2010s"], 2
    ),
    (
        "artist-cardi-b", "Artist",
        "Cardi B",
        "Before rap she was a stripper and a reality TV star — and she used that platform to launch a real career. 'Bodak Yellow' made her the first solo female rapper to top the Hot 100 since Lauryn Hill in 1998. She's known for her unfiltered humor and Brooklyn accent.",
        "Cardi B — Invasion of Privacy (2018) end-to-end", 48,
        "Listen to 'Bodak Yellow' — the song's title riffs on Kodak Black's 'No Flockin,' and her spoken ad-libs made it unmistakable. Then 'I Like It' — the Latin trap anthem with Bad Bunny and J Balvin. The album won Best Rap Album at the Grammys, a first for a solo female artist.",
        ["Hip-Hop", "Trap", "American", "2010s"], 2
    ),
    (
        "artist-50-cent", "Artist",
        "50 Cent",
        "He was shot nine times in 2000 and survived — the album 'Get Rich or Die Tryin'' came three years later and sold 12 million copies. He learned to rap after a childhood friend introduced him to the genre, and he built his legend on mixtapes. His debut is a rap textbook.",
        "50 Cent — Get Rich or Die Tryin' (2003) end-to-end", 54,
        "Listen to 'In da Club' — the beat was made by Dr. Dre and the song was written for a gym playlist, meant to make people feel invincible. Then 'Many Men' — about the shooting he survived. The album's title came from a movie poster; the movie came after.",
        ["Hip-Hop", "East Coast Rap", "American", "2000s"], 2
    ),
    (
        "artist-ice-cube", "Artist",
        "Ice Cube",
        "He wrote some of N.W.A's most famous lyrics including 'Straight Outta Compton' before leaving over royalties. 'It Was a Good Day' is famous for being about a real day he had off. He's also a successful actor — 'Friday,' 'Boyz n the Hood,' and three decades of film roles.",
        "Ice Cube — Death Certificate (1991) end-to-end", 51,
        "Listen to 'It Was a Good Day' — the beat samples the Isley Brothers and the lyrics describe an ordinary day in South Central turned into a celebration. Then 'Steady Mobbin'' — the G-funk west coast sound. Cube's political tracks on this album were controversial, which was the point.",
        ["Hip-Hop", "West Coast Rap", "American", "1990s"], 2
    ),
    (
        "artist-krs-one", "Artist",
        "KRS-One",
        "The Teacha — he was homeless and living in a shelter when he started rapping. 'The Bridge Is Over' answered Queens' 'The Bridge' and started the first great rap feud. He named himself KRS-One (Knowledge Reigns Supreme Over Nearly Everyone) and still lectures at universities.",
        "Boogie Down Productions — Criminal Minded (1987) end-to-end", 47,
        "Listen to 'The Bridge Is Over' — one of the greatest diss records ever, aimed at Queensbridge rivals. Then 'Criminal Minded' — the title track that defined hardcore hip-hop. The album was recorded in a studio above a crack house; its cover shows the crew posing with guns.",
        ["Hip-Hop", "Golden Age", "American", "1980s"], 2
    ),
    (
        "artist-mobb-deep", "Artist",
        "Mobb Deep",
        "Havoc and Prodigy were teenagers when they recorded 'The Infamous' — a raw portrait of Queensbridge projects life. 'Shook Ones, Pt. II' is considered one of the greatest rap songs ever. Prodigy's battle with sickle cell anemia shaped the pain in his voice.",
        "Mobb Deep — The Infamous (1995) end-to-end", 65,
        "Listen to 'Shook Ones, Pt. II' — the piano loop and Havoc's beat are so perfect that Jay-Z and Nas both wanted it. Then 'Survival of the Fittest' — the menacing sequel. The album was recorded over two years while both members were teens, giving it a realism no studio could fake.",
        ["Hip-Hop", "East Coast Rap", "American", "1990s"], 2
    ),
    (
        "artist-dmx", "Artist",
        "DMX",
        "His growl and bark made him the first artist to release two albums debuting at #1 in the same year. 'Ruff Ryders' Anthem' turned a simple chant into a hip-hop anthem. He also starred in 'Belly' and 'Romeo Must Die' — and was a devoted gospel churchgoer who prayed on every album.",
        "DMX — It's Dark and Hell Is Hot (1998) end-to-end", 65,
        "Listen to 'Ruff Ryders' Anthem' — the beat is a loop of a gospel organ riff, and DMX's bark makes it iconic. Then 'Slippin'' — the most personal song in his catalog, about his childhood struggles. His raw delivery proved hip-hop could be both terrifying and vulnerable.",
        ["Hip-Hop", "East Coast Rap", "American", "1990s"], 2
    ),
    (
        "artist-megan-thee-stallion", "Artist",
        "Megan Thee Stallion",
        "She's a licensed esthetician and graduated college while charting. 'Savage' — the remix with Beyoncé — became a summer anthem. Her nickname 'Thee Stallion' is a Houston slang for a tall, confident woman. 'Hot Girl Summer' turned her catchphrase into a movement.",
        "Megan Thee Stallion — Good News (2020) end-to-end", 49,
        "Listen to 'Savage' — the beat's flute riff and her rapid-fire flow made it a TikTok phenomenon before the Beyoncé remix. Then 'Body' — the song about loving your curves. Megan's flow is technically dazzling; try to follow her tongue-twister bars on 'Girls in the Hood.'",
        ["Hip-Hop", "Southern Rap", "American", "2020s"], 2
    ),
    (
        "artist-run-the-jewels", "Artist",
        "Run the Jewels",
        "El-P and Killer Mike — an underground producer and an Atlanta political rapper — became the best duo in rap. 'RTJ4' was released early during the 2020 George Floyd protests as a free download. Their live shows are legendary; they even released an official 'close your eyes during this song' track.",
        "Run the Jewels — RTJ4 (2020) end-to-end", 39,
        "Listen to 'Ooh LA LA' — the hook is a Greg Nice sample and the song is a celebration of hip-hop history. Then 'Walking in the Snow' — Killer Mike's verse about police brutality is the album's most devastating moment. El-P's production is dense; listen twice to catch the layers.",
        ["Hip-Hop", "Underground", "American", "2020s"], 2
    ),
    (
        "artist-mac-miller", "Artist",
        "Mac Miller",
        "He was a teenage white rapper from Pittsburgh who grew into one of hip-hop's most soulful artists. 'Swimming' was released a month before his death at 26, and 'Circles' — its companion — was completed posthumously. He produced for others under the name Larry Fisherman.",
        "Mac Miller — Swimming (2018) end-to-end", 57,
        "Listen to 'Self Care' — the song's floating production and his falsetto captured his late-career sound. Then '2009' — a reflective look back at where he started. The album is about learning to live with yourself; his growth from frat rap to jazz-leaning introspection is the whole journey.",
        ["Hip-Hop", "Jazz Rap", "American", "2010s"], 2
    ),

    # ── R&B / NEO-SOUL ──
    (
        "artist-lauryn-hill", "Artist",
        "Lauryn Hill",
        "The Miseducation of Lauryn Hill won Album of the Year — the first hip-hop album to do so. She recorded most of it while pregnant and took a hardline stance on touring conditions. After that album, she disappeared from mainstream music for years, becoming one of music's great mysteries.",
        "Lauryn Hill — The Miseducation of Lauryn Hill (1998) end-to-end", 78,
        "Listen to 'Doo Wop (That Thing)' — the video splits between 1960s doo-wop and 90s street style, and the song is a lecture on self-respect set to soul. Then 'Ex-Factor' — a song about being taken for granted that Drake later sampled for 'Nice for What.' She sings, raps, and writes every word.",
        ["R&B", "Neo-Soul", "Hip-Hop", "American", "1990s"], 1
    ),
    (
        "artist-erykah-badu", "Artist",
        "Erykah Badu",
        "She wears headwraps and herbs in her tea, calls her fans 'analog girls,' and named her second album 'Mama's Gun.' Her debut 'Baduizm' introduced neo-soul to the world. She recorded 'On & On' in a single take and still performs with a live band and a full cosmic philosophy.",
        "Erykah Badu — Baduizm (1997) end-to-end", 58,
        "Listen to 'On & On' — the song's message about reincarnation and meditation set the neo-soul blueprint. Then 'Tyrone' — a live-format diss track she wrote for a boyfriend who kept her waiting. Badu's phrasing bends time; her voice is the instrument and the message.",
        ["R&B", "Neo-Soul", "American", "1990s"], 2
    ),
    (
        "artist-dangelo", "Artist",
        "D'Angelo",
        "His 'Brown Sugar' was one of the first neo-soul albums, and 'Voodoo' — recorded with a live band in a studio with no isolation booths — is considered a masterpiece. The 'Untitled (How Does It Feel)' video made him a sex symbol overnight. He disappeared for 14 years before returning with 'Black Messiah.'",
        "D'Angelo — Voodoo (2000) end-to-end", 78,
        "Listen to 'Untitled (How Does It Feel)' — the falsetto and the 3-minute fade-out are pure seduction. Then 'Chicken Grease' — the band (The Soulquarians: ?uestlove, Pino Palladino) recorded live, no overdubs. The album sounds like 1970s funk recorded in 2000.",
        ["R&B", "Neo-Soul", "American", "2000s"], 2
    ),
    (
        "artist-sza", "Artist",
        "SZA",
        "Her debut album 'Ctrl' — about insecurity, love, and self-doubt — made her the defining R&B voice of her generation. She was a marine-biology student before music. 'Kill Bill' became her first #1 by turning a murder fantasy into a pop hit.",
        "SZA — Ctrl (2017) end-to-end", 49,
        "Listen to 'Love Galore' — the song started as a beat she recorded over and ignored for a year. Then 'The Weekend' — a song about sharing a man, written from a perspective she later said she's not proud of. Her lyrics are unflinchingly honest about her own flaws.",
        ["R&B", "Alternative R&B", "American", "2010s"], 2
    ),
    (
        "artist-anderson-paak", "Artist",
        "Anderson .Paak",
        "The period in his name is a tribute to his late mother's stage name 'Ava .Paak.' He drummed in a church band, sold weed, and was homeless before his breakthrough with Dr. Dre's 'Compton.' 'Ventura' won a Grammy, and he now fronts the rock-soul group NxWorries.",
        "Anderson .Paak — Malibu (2016) end-to-end", 58,
        "Listen to 'The Bird' — a sunny song about a money-driven relationship, delivered with his signature breezy flow. Then 'Come Down' — the funk workout that samples 70s soul. Paak sings, raps, and drums on the record; his live shows feature him drumming while singing.",
        ["R&B", "Funk", "Hip-Hop", "American", "2010s"], 2
    ),
    (
        "artist-the-weeknd", "Artist",
        "The Weeknd",
        "His stage name came from a 'one weekend' he'd rather forget — he dropped the 'e' to avoid a copyright claim. He released three mixtapes in 2011 and the world of dark R&B changed. 'Blinding Lights' became the longest-charting song in Billboard history.",
        "The Weeknd — After Hours (2020) end-to-end", 56,
        "Listen to 'Blinding Lights' — the 80s synth-pop revival that dominated 2020. Then 'Heartless' — the cold banger that precedes the album's emotional spiral. The Weeknd's persona is a character — the red-suit, bloodied-face aesthetic in the videos is him exploring fame's dark side.",
        ["R&B", "Synth-Pop", "Canadian", "2020s"], 2
    ),
    (
        "artist-usher", "Artist",
        "Usher",
        "He was discovered at 13 by a record exec who saw him at a talent show, and signed at 14. 'Confessions' sold 10 million copies and is one of the best-selling R&B albums ever. 'Yeah!' — the crunk anthem with Lil Jon — turned him into a global star. He also mentored Justin Bieber.",
        "Usher — Confessions (2004) end-to-end", 60,
        "Listen to 'Yeah!' — the beat's electronic dancehall sound came from Lil Jon, and Usher's ad-libs made it a club classic. Then 'Confessions Part II' — the song about an affair that everyone assumed was autobiographical. The album's personal songs made it a confession booth.",
        ["R&B", "Pop", "American", "2000s"], 2
    ),
    (
        "artist-sade", "Artist",
        "Sade",
        "The Nigerian-British singer defined smooth soul with a single album, 'Diamond Life,' and never chased trends — her albums are released years apart on purpose. 'Smooth Operator' is her signature. Her band is named after her; the band members are the same four people since 1982.",
        "Sade — Diamond Life (1984) end-to-end", 43,
        "Listen to 'Smooth Operator' — the song's jazz-funk groove and her velvet voice made it a worldwide hit. Then 'Your Love Is King' — the band's first single. Sade's music is restraint itself: sax solos, bass, and a voice that never raises. It's the calmest #1 album in pop history.",
        ["R&B", "Sophisti-Pop", "British", "1980s"], 2
    ),

    # ── FOLK / SINGER-SONGWRITER ──
    (
        "artist-joni-mitchell", "Artist",
        "Joni Mitchell",
        "She taught herself guitar and invented an open-tuning style no one can copy. 'Blue' — recorded in 1971 — is considered one of the most honest albums ever made. She was a visual artist first and painted many of her own album covers, including 'Blue's' self-portrait.",
        "Joni Mitchell — Blue (1971) end-to-end", 36,
        "Listen to 'River' — it's a Christmas song about wanting to escape, written with Joni at a piano playing Gershwin's 'Rhapsody in Blue.' Then 'A Case of You' — a song about a love that stays. The whole album was recorded in four days with just voice, guitar, and piano.",
        ["Folk", "Singer-Songwriter", "Canadian", "1970s"], 1
    ),
    (
        "artist-leonard-cohen", "Artist",
        "Leonard Cohen",
        "He was a poet and novelist before music — he didn't release his first album until he was 33. 'Hallelujah' took years to write, went nowhere, then became one of the most covered songs in history after Jeff Buckley's version. He was ordained as a Zen Buddhist monk in 1994.",
        "Leonard Cohen — Songs of Leonard Cohen (1967) end-to-end", 42,
        "Listen to 'Suzanne' — the song about a real woman he knew in Montreal, with the lyric 'Jesus was a sailor.' Then 'Hallelujah' — try to count the different versions of the song's chords; Cohen rewrote it dozens of times. His baritone voice makes every line sound like scripture.",
        ["Folk", "Singer-Songwriter", "Canadian", "1960s"], 1
    ),
    (
        "artist-nick-drake", "Artist",
        "Nick Drake",
        "He recorded three albums and sold almost nothing while alive; he died at 26 from an antidepressant overdose. 'Pink Moon' — recorded alone with just his guitar in two nights — became a hit after Volkswagen used it in a 1999 commercial. His family says he never recovered from the silence.",
        "Nick Drake — Pink Moon (1972) end-to-end", 28,
        "Listen to the title track — just voice and guitar, recorded at 2am in a silent studio, and the tape hiss is part of the sound. Then 'Place to Be' — his gentle picking and quiet voice. The album's 11 songs total 28 minutes; there is no other record like it.",
        ["Folk", "Singer-Songwriter", "British", "1970s"], 2
    ),
    (
        "artist-cat-stevens", "Artist",
        "Cat Stevens",
        "He nearly died of tuberculosis at 18 and turned to music during recovery. In 1977, after a near-drowning, he converted to Islam, changed his name to Yusuf Islam, and quit music for years. 'Wild World' and 'Father and Son' are his enduring classics.",
        "Cat Stevens — Tea for the Tillerman (1970) end-to-end", 32,
        "Listen to 'Wild World' — the song is a goodbye to his girlfriend, with the famous line 'I'll always remember you like a child, girl.' Then 'Father and Son' — a dialogue where he sings both parts, the father and the son, switching voices. The album is full of piano and gentle philosophy.",
        ["Folk", "Singer-Songwriter", "British", "1970s"], 2
    ),
    (
        "artist-james-taylor", "Artist",
        "James Taylor",
        "He was the first non-Beatle signed to Apple Records. 'Fire and Rain' was written about a friend's suicide and his own heroin addiction. He performed at the 1970 Isle of Wight festival as the Beatles' opener — a stadium of half a million people — at just 22.",
        "James Taylor — Sweet Baby James (1970) end-to-end", 36,
        "Listen to 'Fire and Rain' — the song's three verses each tell a different chapter: his friend's death, his addiction, his success. Then 'Sweet Baby James' — the lullaby he wrote for his nephew. His fingerpicking style became the template for a generation of singer-songwriters.",
        ["Folk", "Singer-Songwriter", "American", "1970s"], 2
    ),
    (
        "artist-simon-and-garfunkel", "Artist",
        "Simon & Garfunkel",
        "Paul Simon and Art Garfunkel met in elementary school and performed as 'Tom & Jerry' as teens. 'Bridge over Troubled Water' — Garfunkel's soaring vocal — is one of the best-selling singles ever. The duo hated each other by the end and broke up at their peak.",
        "Simon & Garfunkel — Bridge over Troubled Water (1970) end-to-end", 36,
        "Listen to the title track — Garfunkel's vocal was recorded in one take and the orchestral crescendo builds to a gospel finish. Then 'The Boxer' — the song's 'lie-la-lie' chorus was improvised in the studio. Paul Simon's lyrics are dense; read them like poetry.",
        ["Folk", "Folk Rock", "American", "1960s"], 2
    ),
    (
        "artist-tracy-chapman", "Artist",
        "Tracy Chapman",
        "'Fast Car' was written when she was 20 and remains one of the most covered songs ever. Her self-titled debut won three Grammys. 'Give Me One Reason' — her blues hit — came nearly a decade later. She famously played 'Fast Car' at the 2024 Grammys with Luke Combs, 36 years after its release.",
        "Tracy Chapman — Tracy Chapman (1988) end-to-end", 37,
        "Listen to 'Fast Car' — the song is a story of escaping poverty, told in first person, and its acoustic guitar pattern is deceptively simple. Then 'Talkin' 'bout a Revolution' — the protest song that made her famous. Her voice is clear and unshakeable; the album needs nothing else.",
        ["Folk", "Singer-Songwriter", "American", "1980s"], 2
    ),
    (
        "artist-john-prine", "Artist",
        "John Prine",
        "He was a mailman who wrote songs on his route — Bob Dylan heard his demo and called him 'one of the best songwriters around.' He wrote 'Angel from Montgomery' at 24, and later survived throat cancer and a lung transplant. His last album, 'The Tree of Forgiveness,' was released a year before his death.",
        "John Prine — John Prine (1971) end-to-end", 40,
        "Listen to 'Angel from Montgomery' — a song about a middle-aged woman wanting to escape her life, written when he was 24. Then 'Sam Stone' — the war veteran's story, with the devastating 'there's a hole in daddy's arm where all the money goes.' His humor and heart are inseparable.",
        ["Folk", "Americana", "American", "1970s"], 2
    ),

    # ── SHOEGAZE / DREAM POP ──
    (
        "artist-my-bloody-valentine", "Artist",
        "My Bloody Valentine",
        "'Loveless' took two years and over $500,000 to record — nearly bankrupting the label. Kevin Shields invented the 'glide guitar' technique (holding the tremolo arm while strumming) to create the album's swirling sound. The band then vanished for 22 years before returning.",
        "My Bloody Valentine — Loveless (1991) end-to-end", 48,
        "Listen on headphones, loud — the opening of 'Only Shallow' is a wall of guitar that reveals melodies underneath. Then 'When You Sleep' — the catchiest song ever buried under distortion. The album's sound is a 'guitar orchestra': every part was recorded hundreds of times and mixed into one wall.",
        ["Shoegaze", "Dream Pop", "British", "1990s"], 2
    ),
    (
        "artist-slowdive", "Artist",
        "Slowdive",
        "Named after a word in a Siouxsie and the Banshees song, Slowdive made dreamy, hazy shoegaze that critics mocked at the time and now call essential. Their 2017 self-titled comeback album was named one of the best of the decade. 'Alison' is their signature haze.",
        "Slowdive — Souvlaki (1993) end-to-end", 41,
        "Listen to 'Alison' — the song's chorus is a melody floating above the guitar noise. Then 'When the Sun Hits' — the dreamiest track on the record. The band even met Brian Eno, who visited the studio and felt they didn't need him. The guitars sound like they're underwater; let the waves wash over.",
        ["Shoegaze", "Dream Pop", "British", "1990s"], 2
    ),
    (
        "artist-cocteau-twins", "Artist",
        "Cocteau Twins",
        "Elizabeth Fraser's vocals are famously nonsensical — she sings in invented languages, pure emotion without words. The band's 'Heaven or Las Vegas' is their most accessible album and a dream-pop landmark. They formed in Scotland in 1979 and never had a conventional hit.",
        "Cocteau Twins — Heaven or Las Vegas (1990) end-to-end", 38,
        "Listen to the title track — Fraser's voice is the lead instrument; try to not understand a word and feel it instead. Then 'Cherry-Coloured Funk' — the band's most famous song. The album is a cloud of guitars, bass, and voice that defined dream pop for everyone after.",
        ["Dream Pop", "Ethereal", "British", "1990s"], 2
    ),
    (
        "artist-cigarettes-after-sex", "Artist",
        "Cigarettes After Sex",
        "Greg Gonzalez named the band after a conversation in 2008 and records everything with one mic technique — his voice is so hushed it feels like a secret. Their songs are slow, cinematic, and unashamedly romantic. 'Apocalypse' went viral and made them a cult phenomenon.",
        "Cigarettes After Sex — Cigarettes After Sex (2017) end-to-end", 47,
        "Listen to 'Apocalypse' — the song's gentle guitar and whispered vocal feel like a memory. Then 'K.' — a song about a one-night stand in a hotel. Gonzalez records in empty halls and warehouses to capture the reverb; the whole album sounds like it was played at 3am.",
        ["Dream Pop", "Slowcore", "American", "2010s"], 2
    ),
    (
        "artist-japanese-breakfast", "Artist",
        "Japanese Breakfast",
        "Michelle Zauner's stage name was a dream she had; her debut was made while she grieved her mother's death. Her memoir 'Crying in H Mart' became a bestseller and was adapted into a film. 'Be Sweet' — her pop-disco hit — is about calling your ex, set to a synth groove.",
        "Japanese Breakfast — Jubilee (2021) end-to-end", 35,
        "Listen to 'Be Sweet' — the disco-pop opening is a shock after her previous albums' indie sound, and it works perfectly. Then 'Paprika' — the album's opener about joy and performance. Zauner's music shifts between grief, humor, and pop gloss; the range is the point.",
        ["Indie Pop", "Dream Pop", "American", "2020s"], 2
    ),

    # ── METAL ──
    (
        "artist-gojira", "Artist",
        "Gojira",
        "French brothers Joe and Mario Duplantier lead one of the most technical metal bands alive — they originally played in a band called Godzilla before renaming it. Their songs are about nature, whales, and the environment; frontman Joe Duplantier is an avid scuba diver.",
        "Gojira — From Mars to Sirius (2005) end-to-end", 66,
        "Listen to 'Flying Whales' — the opening riff is one of the heaviest ever recorded, and the song's whale sounds are actual whale recordings. Then 'The Art of Dying' — a 9-minute meditation on mortality with a drum solo in the middle. The album is a concept record about saving the whales.",
        ["Metal", "Progressive Metal", "French", "2000s"], 2
    ),
    (
        "artist-meshuggah", "Artist",
        "Meshuggah",
        "The Swedish band invented 'djent' — their guitarists play in 4/4 while the drums play in 7/8, creating a hypnotic groove. The band name is Yiddish for 'crazy.' 'Bleed' is considered one of the most physically demanding drum songs ever written.",
        "Meshuggah — ObZen (2008) end-to-end", 53,
        "Listen to 'Bleed' — the drum pattern (double bass at 200 BPM in 4/4 with syncopated accents) is the song's entire story. Then 'ObZen' — the title track about obsession and zen. The guitars are downtuned to the point where they're more like bass; count along and feel the groove warp.",
        ["Metal", "Djent", "Swedish", "2000s"], 2
    ),
    (
        "artist-mastodon", "Artist",
        "Mastodon",
        "The Atlanta band writes concept albums about whales, Moby-Dick, and mythology. 'Crack the Skye' — their masterpiece — is about a tetraplegic journeying out of his body. Drummer Brann Dailor also sings, playing his kit like a solo instrument. They were named after the prehistoric mammal for its heaviness.",
        "Mastodon — Crack the Skye (2009) end-to-end", 54,
        "Listen to 'The Czar' — a 10-minute, four-movement epic about Rasputin, complete with a banjo solo. Then 'Oblivion' — the album's opening track. The drums are impossibly complex; Brann's fills are like jazz solos played with metal force. The whole album is one long cosmic journey.",
        ["Metal", "Progressive Metal", "American", "2000s"], 2
    ),
    (
        "artist-deftones", "Artist",
        "Deftones",
        "The most melodic band in metal — Chino Moreno's singing floats over brutal guitars. 'White Pony' — their breakthrough — featured a guest vocal from Maynard Keenan of Tool. Their sound is called 'shoegaze metal' by fans, blending dreamy textures with crushing riffs.",
        "Deftones — White Pony (2000) end-to-end", 49,
        "Listen to 'Change (In the House of Flies)' — the song's quiet-loud structure and Chino's soaring chorus made it their biggest hit. Then 'Digital Bath' — a song about drowning someone, told seductively. The album's blend of dream pop and metal was unlike anything in 2000.",
        ["Metal", "Alternative Metal", "American", "2000s"], 2
    ),
    (
        "artist-slipknot", "Artist",
        "Slipknot",
        "Nine masked members from Iowa — they wore masks because they wanted the music, not their faces, to be the identity. Their debut captured their live chaos with raw, quick recording. 'Iowa' — their second — is considered one of the darkest albums ever made; the band says they were in a violent state throughout.",
        "Slipknot — Slipknot (1999) end-to-end", 60,
        "Listen to 'Wait and Bleed' — the song's melody and rage made it their first hit. Then 'Spit It Out' — the rap-metal anthem. The band's percussion section (two drummers, plus sampler) creates a wall of noise; count the percussion layers during 'Eyeless.'",
        ["Metal", "Nu Metal", "American", "1990s"], 2
    ),
    (
        "artist-nine-inch-nails", "Artist",
        "Nine Inch Nails",
        "Trent Reznor wrote, played, and produced everything on the early records alone in his Cleveland apartment. 'The Downward Spiral' — his industrial masterpiece — was recorded in the house where Sharon Tate was murdered. He later won an Oscar for 'The Social Network' score.",
        "Nine Inch Nails — The Downward Spiral (1994) end-to-end", 65,
        "Listen to 'Closer' — the song's famous 'I want to fuck you like an animal' is the least interesting part; the industrial clang underneath is the real art. Then 'Hurt' — a song of such raw despair that Johnny Cash covered it and made it his own. The album is a story of self-destruction.",
        ["Metal", "Industrial", "American", "1990s"], 2
    ),

    # ── COUNTRY / AMERICANA ──
    (
        "artist-garth-brooks", "Artist",
        "Garth Brooks",
        "He's the best-selling solo artist in US history — over 170 million records — and he did it with a fake name (Chris Gaines) experiment and elaborate stage shows. 'Friends in Low Places' is his signature. He retired in 2000 to raise his daughters, then returned.",
        "Garth Brooks — No Fences (1990) end-to-end", 40,
        "Listen to 'Friends in Low Places' — the song's crowd-pleasing chorus was written in an hour and became his anthem. Then 'The Dance' — a ballad about regretting nothing, even heartbreak. Brooks brought arena-rock energy to country and changed the genre's live shows forever.",
        ["Country", "American", "1990s"], 2
    ),
    (
        "artist-shania-twain", "Artist",
        "Shania Twain",
        "'Come On Over' is the best-selling country album ever and the best-selling album by a female artist in any genre. She wrote nearly all her songs with then-husband Mutt Lange. She was discovered singing at a bar at 21 after her parents' death made her raise her siblings.",
        "Shania Twain — Come On Over (1997) end-to-end", 60,
        "Listen to 'Man! I Feel Like a Woman!' — the gender-bending pop-country anthem. Then 'You're Still the One' — the ballad about a love that survived. The album blends country with pop and rock, and its crossover success proved country could be global.",
        ["Country", "Pop Country", "Canadian", "1990s"], 2
    ),
    (
        "artist-george-strait", "Artist",
        "George Strait",
        "The King of Country — he's had more #1 country hits than anyone in history (60+) and never chased trends. He was a cattle rancher before fame and still owns a ranch in Texas. 'Amarillo by Morning' — his classic — is about a rodeo cowboy's life on the road.",
        "George Strait — Strait Country (1981) end-to-end", 30,
        "Listen to 'Amarillo by Morning' — the song is a rodeo cowboy's quiet anthem about the road, and it's considered one of the greatest country songs ever written. Then 'Unwound' — his first single. Strait's style is pure traditional country: steel guitar, fiddle, and a voice that never strains.",
        ["Country", "American", "1980s"], 2
    ),
    (
        "artist-chris-stapleton", "Artist",
        "Chris Stapleton",
        "Before going solo he was a Nashville songwriter who wrote hits for George Strait, Kenny Chesney, and Luke Bryan. His voice — a blues-rock growl — won him three Grammys for his debut 'Traveller.' His cover of 'Tennessee Whiskey' — a George Jones classic — became his signature.",
        "Chris Stapleton — Traveller (2015) end-to-end", 55,
        "Listen to 'Tennessee Whiskey' — his version of the George Jones song became a multi-week #1 country hit, with a vocal that turns it into pure soul. Then 'Fire Away' — the devastating ballad about depression. His live 'Nashville sessions' with wife Morgane are the best way in.",
        ["Country", "Outlaw Country", "American", "2010s"], 2
    ),
    (
        "artist-tyler-childers", "Artist",
        "Tyler Childers",
        "The Kentucky singer-songwriter brought raw, honest Appalachia to modern country. 'Purgatory' was produced by Sturgill Simpson. 'Feathered Indians' — his breakthrough — is a love song set in the backwoods. He resisted Nashville's polish and built his career on touring and authenticity.",
        "Tyler Childers — Purgatory (2017) end-to-end", 36,
        "Listen to 'Feathered Indians' — the song's banjo and his Kentucky drawl make it feel like a front-porch confession. Then 'Whitehouse Road' — the rowdy, raw road anthem. Childers's lyrics are full of place and specificity; he writes about the people he grew up with.",
        ["Country", "Americana", "American", "2010s"], 2
    ),

    # ── REGGAE / AFROBEATS ──
    (
        "artist-toots-and-the-maytals", "Artist",
        "Toots and the Maytals",
        "Toots Hibbert invented the word 'reggae' — his 1968 song 'Do the Reggay' named the genre. He was once arrested for marijuana possession and sang about it. 'Pressure Drop' — his classic — was covered by the Clash. He kept performing until days before his death in 2020.",
        "Toots and the Maytals — Funky Kingston (1972) end-to-end", 37,
        "Listen to 'Pressure Drop' — the song is about the feeling of being caught by the police, delivered with Toots's gospel-trained voice. Then 'Monkey Man' — the reggae classic. Toots's voice could do soul, ska, and rock steady; he was reggae's Otis Redding.",
        ["Reggae", "Ska", "Jamaican", "1970s"], 2
    ),
    (
        "artist-jimmy-cliff", "Artist",
        "Jimmy Cliff",
        "He was reggae's first international star — 'The Harder They Come,' the film and its soundtrack, introduced reggae to the world. He received Jamaica's Order of Merit, the country's highest honor for its artists. 'Many Rivers to Cross' is one of the greatest soul-reggae ballads ever.",
        "Jimmy Cliff — The Harder They Come soundtrack (1972) end-to-end", 45,
        "Listen to 'Many Rivers to Cross' — the song is about struggling against impossible odds, and it's been covered by everyone from Cher to Annie Lennox. Then 'The Harder They Come' — the film's defiant title track. The soundtrack introduced reggae to the world outside Jamaica.",
        ["Reggae", "Ska", "Jamaican", "1970s"], 2
    ),
    (
        "artist-koffee", "Artist",
        "Koffee",
        "The youngest artist — and first woman — to win the Grammy for Best Reggae Album. Her breakthrough single 'Toast' was about being proud of her small town and became a global hit. She's also a guitarist who leads a full band, fusing reggae, dancehall, and pop.",
        "Koffee — Rapture (2019) end-to-end", 26,
        "Listen to 'Toast' — the song started as a celebration of her hometown and became a worldwide reggae anthem. Then 'Raggamuffin' — the title track about being a youth from the streets. Koffee's voice and guitar playing make her the future of reggae.",
        ["Reggae", "Dancehall", "Jamaican", "2020s"], 3
    ),
    (
        "artist-burna-boy", "Artist",
        "Burna Boy",
        "The Grammy-winning 'African Giant' who took Afrobeats global. 'On the Low' and 'Last Last' (which sampled Toni Braxton) became worldwide hits. He named his 2022 album 'Love, Damini' after his real name. He's famous for his 'afro-fusion' sound mixing dancehall, reggae, and pop.",
        "Burna Boy — African Giant (2019) end-to-end", 55,
        "Listen to 'On the Low' — the song's afro-fusion groove made it an anthem across Africa and beyond. Then 'Anybody' — the track that samples Fela Kuti's 'Shakara.' Burna calls his sound 'afro-fusion' — reggae, dancehall, and highlife folded together with Nigerian swagger.",
        ["Afrobeats", "Afro-Fusion", "Nigerian", "2010s"], 2
    ),
    (
        "artist-wizkid", "Artist",
        "Wizkid",
        "He was discovered as a teenager and named his debut 'Superstar' at 20. 'Essence' — his song with Tems — became the first Nigerian song to chart on the Billboard Hot 100 and spawned a remix with Justin Bieber. He's collaborated with Drake, Beyoncé, and Skepta.",
        "Wizkid — Made in Lagos (2020) end-to-end", 61,
        "Listen to 'Essence' — the song is a love letter set to a slow afrobeat groove, and it became Afrobeats' global breakthrough. Then 'Ginger' — the track with Burna Boy. Wizkid's voice glides over the production; the album is Lagos nightlife in an hour.",
        ["Afrobeats", "Afro-Pop", "Nigerian", "2020s"], 2
    ),

    # ── ELECTRONIC / EDM ──
    (
        "artist-skrillex", "Artist",
        "Skrillex",
        "He was the singer of the post-hardcore band From First to Last before going solo as a dubstep producer. 'Scary Monsters and Nice Sprites' won three Grammys and made dubstep mainstream. He's won 8 Grammys total — more than any other electronic artist.",
        "Skrillex — Scary Monsters and Nice Sprites (2010) end-to-end", 43,
        "Listen to the title track — the song's aggressive bass wobbles defined 2010s dubstep. Then 'First of the Year (Equinox)' — the call-and-response vocal ('call 911 now!') became a festival anthem. His sound is maximalist; listen to the sound design, not just the drop.",
        ["Electronic", "Dubstep", "American", "2010s"], 2
    ),
    (
        "artist-deadmau5", "Artist",
        "deadmau5",
        "Joel Zimmerman wears a giant mouse helmet and produces progressive house from a Toronto studio. 'Strobe' is considered one of the greatest electronic songs ever — a 6-minute build with no drop. The helmet came from an inside joke about his friend calling him a 'dead mouse.'",
        "deadmau5 — For Lack of a Better Name (2009) end-to-end", 67,
        "Listen to 'Strobe' — the song has no drop; it builds for six minutes and releases in a cascade of melody. Then 'Ghosts 'n' Stuff' — his biggest hit, with guest vocals. He's famously anti-trend; his production is meticulous and his live shows are a single giant cube of light.",
        ["Electronic", "Progressive House", "Canadian", "2000s"], 2
    ),
    (
        "artist-calvin-harris", "Artist",
        "Calvin Harris",
        "The Scottish producer wrote, produced, and sang on 'Summer' — his biggest solo hit. He's the highest-paid DJ in the world most years. 'We Found Love' with Rihanna spent 10 weeks at #1. He started making trance music as a teenager and worked in a supermarket to fund his studio.",
        "Calvin Harris — 18 Months (2012) end-to-end", 47,
        "Listen to 'We Found Love' — the song was written about a couple who are 'hopeless' together, and it spent 10 weeks at #1. Then 'Feel So Close' — his pure dance-pop anthem. Harris's genius is making the melody carry the beat; you can hum every track.",
        ["Electronic", "EDM", "British", "2010s"], 2
    ),
    (
        "artist-avicii", "Artist",
        "Avicii",
        "Tim Bergling was 21 when 'Levels' — built around an Etta James vocal sample — became a global phenomenon. 'Wake Me Up' blended country guitar with EDM and topped charts worldwide. He retired from touring in 2016 for health reasons and died by suicide at 28.",
        "Avicii — True (2013) end-to-end", 50,
        "Listen to 'Wake Me Up' — the song's folk-country vocals (by Aloe Blacc) over EDM was a risk that changed dance music. Then 'Levels' — the Etta James sample that made it a festival anthem. His melodies are simple and unforgettable; that was the point.",
        ["Electronic", "EDM", "Swedish", "2010s"], 2
    ),
    (
        "artist-disclosure", "Artist",
        "Disclosure",
        "Guy and Howard Lawrence were teenagers when they made 'Settle' — garage-house that revived UK dance music. 'Latch' with Sam Smith was their breakthrough. The brothers produce from a bedroom studio and recreate it live with real instruments.",
        "Disclosure — Settle (2013) end-to-end", 57,
        "Listen to 'Latch' — the song was written in an afternoon and became a #1 UK hit; Sam Smith was 19 and unknown. Then 'White Noise' — the album's garage anthem. Disclosure's sound is house music with UK garage swing; the basslines sound like they're breathing.",
        ["Electronic", "House", "UK Garage", "British", "2010s"], 2
    ),
    (
        "artist-jamie-xx", "Artist",
        "Jamie xx",
        "The xx's producer built a solo career on sampling and dance music. 'In Colour' — his debut — mixes UK garage, house, and melancholy pop and won a Grammy. 'I Know There's Gonna Be (Good Times)' samples an old gospel record and features Young Thug rapping.",
        "Jamie xx — In Colour (2015) end-to-end", 45,
        "Listen to 'Gosh' — the track's opening vocal chop builds into the album's biggest dance moment. Then 'Loud Places' — his collaboration with his bandmate Romy, about chasing happiness. He samples records no one else would find; the whole album is a crate-digging love letter.",
        ["Electronic", "UK Garage", "House", "British", "2010s"], 2
    ),
    (
        "artist-fred-again", "Artist",
        "Fred again..",
        "He samples voice memos from friends and strangers and turns them into emotional dance music. His 'Actual Life' albums use real recorded voices as hooks. He produced for Ed Sheeran and Stormzy before going solo. His Boiler Room set became one of dance music's great viral moments.",
        "Fred again.. — Actual Life 3 (2022) end-to-end", 44,
        "Listen to 'Delilah (pull me out of this)' — the song is built around a real voice memo from his friend Delilah. Then 'Jungle' — the track that went viral on TikTok. His music feels like intimate conversations turned into beats; the samples are the stars.",
        ["Electronic", "UK Garage", "British", "2020s"], 2
    ),
    (
        "artist-j-dilla", "Artist",
        "J Dilla",
        "The beatmaker's beatmaker — 'Donuts' was recorded in a hospital bed as he died of a rare blood disease; it's a soul-sampling masterpiece. His 'dilla time' drumming is instantly identifiable by producers. He produced for A Tribe Called Quest, De La Soul, and Badu.",
        "J Dilla — Donuts (2006) end-to-end", 44,
        "Listen to 'Workinonit' — the album opens with a pitched-up soul sample and a drum loop that swings in a way no machine should. Then 'Time: The Donut of the Heart' — a 4-minute suite. 'Donuts' is 31 short tracks, all made from soul records, all from a hospital bed.",
        ["Hip-Hop", "Instrumental", "Electronic", "American", "2000s"], 2
    ),

    # ── JAZZ / BOSSA NOVA ──
    (
        "artist-sonny-rollins", "Artist",
        "Sonny Rollins",
        "He practiced for two years on the Williamsburg Bridge in New York — no recording, just him and his saxophone against the city noise. 'The Bridge' — his comeback album — is named after that practice spot. He kept playing and recording into his eighties.",
        "Sonny Rollins — Saxophone Colossus (1956) end-to-end", 40,
        "Listen to 'St. Thomas' — the calypso-tinged jazz classic that opens the album. Then 'Blue 7' — a 10-minute blues improvisation that became a textbook example of melodic improvising. Rollins plays with a humor and swagger that no other saxophonist matches.",
        ["Jazz", "Hard Bop", "American", "1950s"], 2
    ),
    (
        "artist-art-blakey", "Artist",
        "Art Blakey",
        "His band the Jazz Messengers was a finishing school for jazz greats — Wayne Shorter, Lee Morgan, and Wynton Marsalis all passed through. Blakey's drums were so loud he was told he'd never make it. 'Moanin'' — with Bobby Timmons's gospel-blues head — is his signature.",
        "Art Blakey & the Jazz Messengers — Moanin' (1958) end-to-end", 42,
        "Listen to 'Moanin'' — the bluesy opening melody is one of the catchiest in jazz, and the solos follow in a relay. Then 'Blues March' — the jazz standard about walking like a soldier. Blakey's drumming drives every soloist; he was the band's engine and its dean.",
        ["Jazz", "Hard Bop", "American", "1950s"], 2
    ),
    (
        "artist-dave-brubeck", "Artist",
        "Dave Brubeck",
        "'Take Five' — written by his saxophonist Paul Desmond — is the best-selling jazz single ever, in 5/4 time. Brubeck insisted on odd time signatures at the height of the cool-jazz era; 'Time Out' is the masterpiece of it. He nearly lost his life to polio as a child.",
        "Dave Brubeck — Time Out (1959) end-to-end", 38,
        "Listen to 'Take Five' — count along: it's in 5/4, and the way the bass walks makes it feel natural. Then 'Blue Rondo à la Turk' — the album's opener, inspired by Turkish street musicians playing in 9/8. This is jazz for people who thought they didn't like jazz.",
        ["Jazz", "Cool Jazz", "American", "1950s"], 2
    ),
    (
        "artist-joao-gilberto", "Artist",
        "João Gilberto",
        "The father of bossa nova — he invented the genre's hushed, syncopated guitar pattern in his bedroom in 1958. His whisper-quiet voice and guitar defined Brazil's sound. 'The Girl from Ipanema' — his 1964 recording with Stan Getz — became the best-selling jazz album of all time.",
        "João Gilberto & Stan Getz — Getz/Gilberto (1964) end-to-end", 38,
        "Listen to 'The Girl from Ipanema' — Astrud Gilberto, his wife, sang the English verses in her first-ever studio session; she didn't even know she'd be on the record. Then 'Corcovado' — the hushed bossa standard. Gilberto's guitar sounds like it's whispering; turn it up.",
        ["Bossa Nova", "Brazilian", "1960s"], 2
    ),
    (
        "artist-antonio-carlos-jobim", "Artist",
        "Antônio Carlos Jobim",
        "The composer of bossa nova — he wrote 'The Girl from Ipanema,' 'Corcovado,' and 'Wave.' He was an architect before music, and his melodies have that same clean, structural beauty. His songbook is the most recorded in Brazilian history after the samba classics.",
        "Antônio Carlos Jobim — Wave (1967) end-to-end", 32,
        "Listen to the title track — the melody is so simple and perfect it sounds like it always existed. Then 'Triste' — the melancholy bossa. Jobim's harmonies are lush and unexpected; he borrowed from jazz chords and Debussy. Every bossa nova after him is a footnote.",
        ["Bossa Nova", "Brazilian", "1960s"], 2
    ),
    (
        "artist-kamasi-washington", "Artist",
        "Kamasi Washington",
        "The saxophonist who brought jazz to the streaming generation — his 3-hour debut 'The Epic' featured a full orchestra and choir. He played saxophone on Kendrick Lamar's 'To Pimp a Butterfly.' He's known for his Coltrane-like spiritual improvisation and his band the Next Step.",
        "Kamasi Washington — The Epic (2015) end-to-end", 174,
        "Listen to 'Change of the Guard' — the album's 13-minute opener that announces jazz is alive. Then 'Miss Understanding' — the gospel-jazz centerpiece. 'The Epic' is three hours across 17 tracks; pick one track per sitting and let it unfold like a movie.",
        ["Jazz", "Spiritual Jazz", "American", "2010s"], 2
    ),

    # ── LATIN ──
    (
        "artist-celia-cruz", "Artist",
        "Celia Cruz",
        "The Queen of Salsa — her signature '¡Azúcar!' (sugar!) came from a taxi driver who asked what she wanted and she said sugar. She left Cuba in 1960 and never returned, becoming a symbol of exile. She won five Grammys and sold over 10 million records.",
        "Celia Cruz — Fania All-Stars live era (1970s) end-to-end", 60,
        "Listen to 'Quimbara' — the salsa classic where Celia's voice carries an entire horn section's worth of energy. Then 'La Vida Es un Carnaval' — her anthem of resilience, written about overcoming hard times. Her voice is pure joy; you don't need to understand Spanish to feel it.",
        ["Salsa", "Latin", "Cuban", "1970s"], 2
    ),
    (
        "artist-carlos-santana", "Artist",
        "Carlos Santana",
        "His band's Woodstock performance — including the 11-minute 'Soul Sacrifice' — made them famous overnight. 'Smooth' with Rob Thomas was the last #1 single of the 1990s and won three Grammys. He named his guitar sound 'the voice' and plays it like a singer.",
        "Santana — Abraxas (1970) end-to-end", 38,
        "Listen to 'Oye Como Va' — Tito Puente's mambo turned into Latin rock by Santana's guitar. Then 'Black Magic Woman' — the Fleetwood Mac song he made his own. His guitar tone is instantly recognizable; the album is where Latin rhythms met San Francisco rock.",
        ["Latin Rock", "Mexican", "1970s"], 2
    ),
    (
        "artist-shakira", "Artist",
        "Shakira",
        "She wrote her first song at 8 and taught herself to belly dance at 4 — her hips are a Colombian national treasure. 'Hips Don't Lie' was her global breakthrough. She wrote the 2010 World Cup anthem 'Waka Waka' and has sold over 80 million records.",
        "Shakira — Oral Fixation, Vol. 2 (2005) end-to-end", 44,
        "Listen to 'Hips Don't Lie' — the song's Colombian carnival sound with Wyclef Jean made it one of the best-selling singles of the 2000s. Then 'La Tortura' — her reggaeton crossover with Alejandro Sanz. She sings in English and Spanish with equal ease, sometimes in the same song.",
        ["Latin Pop", "Colombian", "2000s"], 2
    ),
    (
        "artist-vicente-fernandez", "Artist",
        "Vicente Fernández",
        "The king of ranchera music — Mexico's greatest mariachi voice. He started as a street singer and became a national treasure whose funeral was a national mourning. 'Volver, Volver' — his signature — is about begging a lover to return. He recorded over 50 albums.",
        "Vicente Fernández — Ranchera essentials (1980s) end-to-end", 45,
        "Listen to 'Volver, Volver' — the ranchera anthem where his voice cracks with emotion at the climax. Then 'El Rey' — the song about being the king of your own life. His voice is big and open like the sky; the mariachi band plays like a cavalry charge.",
        ["Ranchera", "Mariachi", "Mexican", "1980s"], 2
    ),
    (
        "artist-cafe-tacvba", "Artist",
        "Café Tacvba",
        "Mexico's greatest rock band — they've been called 'the most important rock band in Latin America.' 'La Ingrata' and 'Eres' are their classics. They record across genres — rock, bolero, electronic, sometimes within one song. The 'v' in their name winks at Aztec writing.",
        "Café Tacvba — Re (1994) end-to-end", 52,
        "Listen to 'La Ingrata' — the song that made them stars, with its weird, wonderful bassline. Then 'Eres' — the album's quiet acoustic love song, a total shift from the rock around it. 'Re' is a genre-hopping masterpiece that proves rock belongs in Spanish.",
        ["Latin Rock", "Mexican", "1990s"], 2
    ),

    # ── WORLD ──
    (
        "artist-nusrat-fateh-ali-khan", "Artist",
        "Nusrat Fateh Ali Khan",
        "The Pakistani qawwali singer whose voice spanned four octaves — he could sing for 30 minutes on one breath, some say. He's credited with bringing qawwali (Sufi devotional music) to the world. He collaborated with Eddie Vedder and was sampled by countless hip-hop producers.",
        "Nusrat Fateh Ali Khan — Mustt Mustt (1990) end-to-end", 55,
        "Listen to 'Mustt Mustt' — the song's ecstatic build is qawwali's signature: the harmonium, the clapping, and Nusrat's voice climbing higher and higher. Then 'Tere Bina' — the duet with Eddie Vedder. His voice is a spiritual experience; don't expect Western song structures.",
        ["Qawwali", "Sufi", "Pakistani", "1990s"], 2
    ),
    (
        "artist-tinariwen", "Artist",
        "Tinariwen",
        "The Tuareg band from the Sahara desert who play 'desert blues' — electric guitars over ancient rhythms. Several members are former rebels who fought in the Tuareg uprisings; their songs are about exile and memory. They formed in a Libyan military camp in the 1980s.",
        "Tinariwen — Aman Iman (2007) end-to-end", 52,
        "Listen to 'Cler Achel' — the song's hypnotic guitar lines sound like a caravan crossing the dunes. Then 'Matadjem Yinmixan' — a call for peace between Tuareg factions. The band's music is trance-like and meditative; the guitar parts interlock like voices.",
        ["Desert Blues", "Tuareg", "Malian", "2000s"], 2
    ),
    (
        "artist-king-sunny-ade", "Artist",
        "King Sunny Adé",
        "The king of jùjú music — Nigeria's psychedelic guitar-pop. His band once had 50 members including four pedal-steel guitarists. 'Synchro System' was produced by Brian Eno. He's been called the 'Jimi Hendrix of jùjú' for his hypnotic, overlapping guitar parts.",
        "King Sunny Adé — Juju Music (1982) end-to-end", 45,
        "Listen to 'Ja Funmi' — the opening track's guitar melody weaves over talking drums for 10 minutes. Then 'Sunny Ti De' — the celebratory track about his own arrival. The pedal-steel guitar gives jùjú its dreamy sound; the rhythm section never stops dancing.",
        ["Jùjú", "Nigerian", "1980s"], 3
    ),
    (
        "artist-angelique-kidjo", "Artist",
        "Angélique Kidjo",
        "The Beninese singer has been called 'Africa's premier diva' — she sings in five languages and fuses Afrobeat, funk, and pop. Her cover album 'Remain in Light' reimagined Talking Heads with a full West African band. She won five Grammys and UNICEF named her an ambassador.",
        "Angélique Kidjo — Remain in Light (2018) end-to-end", 51,
        "Listen to 'Once in a Lifetime' — her version turns Talking Heads' paranoid anthem into a joyful West African celebration. Then 'Born Here' — the duet with her mother's blessing. Kidjo's voice is huge and elastic; she makes every song sound like it was always African.",
        ["Afrobeat", "World", "Beninese", "2010s"], 2
    ),
    (
        "artist-miriam-makeba", "Artist",
        "Miriam Makeba",
        "Mama Africa — the South African singer exiled for 31 years over her anti-apartheid activism. 'Pata Pata' — her 1967 global hit — was banned in South Africa. She testified against apartheid at the UN in 1963 and performed at the 1988 Mandela birthday concert.",
        "Miriam Makeba — Pata Pata (1967) end-to-end", 38,
        "Listen to 'Pata Pata' — the song means 'touch touch' in Xhosa, and it's the most joyful protest song ever written. Then 'The Click Song' — sung in Xhosa with its famous click consonants. Makeba's voice carries the warmth and pain of a continent; she was exiled but never silenced.",
        ["Afro-Pop", "World", "South African", "1960s"], 2
    ),

    # ── BLUES ──
    (
        "artist-sister-rosetta-tharpe", "Artist",
        "Sister Rosetta Tharpe",
        "The godmother of rock and roll — she played gospel on an electric guitar with distortion decades before anyone else. Chuck Berry and Elvis both cited her as an influence. She once held a wedding concert in a baseball stadium with 25,000 fans.",
        "Sister Rosetta Tharpe — Gospel Giants (1950s) end-to-end", 40,
        "Listen to 'This Train' — the gospel classic where her guitar rips like a rock solo. Then 'Didn't It Rain' — recorded live with a choir and a storm of guitar. She played her guitar behind her head and between her legs; she was the rock star before there was rock.",
        ["Blues", "Gospel", "Rock and Roll", "American", "1950s"], 2
    ),
    (
        "artist-lead-belly", "Artist",
        "Lead Belly",
        "Huddie Ledbetter sang his way out of prison twice — governors pardoned him after hearing his songs pleading for freedom. 'Goodnight Irene' — a song he learned as a child — became a #1 hit for others. He played 12-string guitar, which he tuned low to make it louder.",
        "Lead Belly — Lead Belly's Last Sessions (1948) end-to-end", 60,
        "Listen to 'Goodnight Irene' — the folk standard that became an international hit after his death. Then 'Where Did You Sleep Last Night' — the murder ballad that Nirvana would later make famous. Lead Belly's voice is enormous; he recorded hundreds of songs in just a few years.",
        ["Blues", "Folk", "American", "1940s"], 2
    ),
    (
        "artist-stevie-ray-vaughan", "Artist",
        "Stevie Ray Vaughan",
        "The greatest blues guitarist of his generation — he went sober in 1986 and recorded his masterpiece 'In Step' three years later. He died in a helicopter crash in 1990 at 35, days after jamming with Eric Clapton. His 'Texas Flood' is a live-recording legend.",
        "Stevie Ray Vaughan — Texas Flood (1983) end-to-end", 39,
        "Listen to 'Pride and Joy' — the opening riff is one of the most joyful in blues history. Then 'Texas Flood' — the slow blues where his guitar literally cries. Vaughan played a battered Stratocaster called 'Number One' and could make it sound like five guitars at once.",
        ["Blues", "Blues Rock", "American", "1980s"], 2
    ),
    (
        "artist-gary-clark-jr", "Artist",
        "Gary Clark Jr.",
        "The Austin guitarist who carries blues into the 21st century — he's opened for the Rolling Stones and jammed with B.B. King. His song 'This Land' won three Grammys and is a searing statement about racism in Texas. He plays everything from blues to funk to hip-hop.",
        "Gary Clark Jr. — This Land (2019) end-to-end", 55,
        "Listen to 'This Land' — the song's opening riff hits like a challenge, and the lyrics are about the racist graffiti he found on his own property. Then 'Bright Lights' — his live anthem about touring. His guitar tone is pure modern blues; he's the genre's heir apparent.",
        ["Blues", "Blues Rock", "American", "2010s"], 2
    ),

    # ── CLASSICAL / FILM COMPOSERS ──
    (
        "artist-tchaikovsky", "Artist",
        "Pyotr Ilyich Tchaikovsky",
        "The Russian composer of 'Swan Lake,' 'The Nutcracker,' and the 1812 Overture — which famously features real cannons. He was terrified of conducting and held his head with his left hand while conducting with his right. He died nine days after premiering his Sixth Symphony.",
        "Tchaikovsky — Swan Lake (1876) end-to-end highlights", 50,
        "Listen to the 'Dance of the Little Swans' — four oboes in perfect unison, one of classical music's most famous moments. Then the 1812 Overture's finale — real cannons, church bells, full orchestra. Tchaikovsky's melodies are why his ballets are the most performed in the world.",
        ["Classical", "Romantic", "Russian", "1870s"], 2
    ),
    (
        "artist-vivaldi", "Artist",
        "Antonio Vivaldi",
        "The Red Priest — he was a red-haired Catholic priest who wrote over 500 concertos. 'The Four Seasons' — his violin concertos depicting each season — is the most recorded piece of classical music ever. He taught at an orphanage for girls, where his students became his orchestra.",
        "Vivaldi — The Four Seasons (1725) end-to-end", 40,
        "Listen to 'Spring' — the opening is the most famous 10 seconds in all of classical music. Then 'Winter' — the second movement's violin solo imitates freezing rain. Vivaldi wrote sonnets to accompany each concerto; listen for the birds, storms, and barking dogs in the music.",
        ["Classical", "Baroque", "Italian", "1700s"], 2
    ),
    (
        "artist-erik-satie", "Artist",
        "Erik Satie",
        "The eccentric French composer who named a piece 'Three Pieces in the Shape of a Pear.' His 'Gymnopédies' are the most calming piano music ever written. He wore the same gray velvet suit every day and kept two pianos — one for friends, one that no one was allowed to touch.",
        "Erik Satie — Gymnopédies (1888) end-to-end", 15,
        "Listen to the first Gymnopédie — three gentle chords repeated with a melody floating above; it's been used in films and countless ads. Then the Gnossiennes — which have no time signature, so they float freely. Satie's music sounds simple; it isn't.",
        ["Classical", "Minimalist", "French", "1880s"], 2
    ),
    (
        "artist-steve-reich", "Artist",
        "Steve Reich",
        "The pioneer of minimalist music — he built 'It's Gonna Rain' from a single recording of a street preacher looped on two tape machines that slowly drift out of sync. 'Music for 18 Musicians' is his masterpiece. His ideas influenced everyone from Brian Eno to Radiohead.",
        "Steve Reich — Music for 18 Musicians (1976) end-to-end", 65,
        "Listen to the opening pulse — 11 chords played over and over that slowly phase and change. Then the second section's marimba melody. Reich's music is hypnotic; let your mind go and the patterns start moving in front of you. It's the root of so much electronic music you love.",
        ["Classical", "Minimalist", "American", "1970s"], 3
    ),
    (
        "artist-hans-zimmer", "Artist",
        "Hans Zimmer",
        "The most famous film composer alive — scores for 'Inception,' 'Interstellar,' 'The Dark Knight,' and 'Gladiator' redefined movie sound. He was a synth player in bands before films. 'Time' from Inception is his most-streamed piece — a two-chord build into a wall of sound.",
        "Hans Zimmer — Inception: Time and Dream Suite (2010) end-to-end", 60,
        "Listen to 'Time' — a two-chord piano theme that builds over four minutes into orchestra and choir; it's the musical definition of a crescendo. Then the 'Interstellar' main theme — an organ that sounds like it's from space. Zimmer's brass and percussion are unmistakable.",
        ["Film Score", "Orchestral", "German", "2010s"], 2
    ),
    (
        "artist-john-williams", "Artist",
        "John Williams",
        "The most recognized melody writer alive — he composed 'Star Wars,' 'Jaws,' 'Indiana Jones,' 'Jurassic Park,' 'Harry Potter,' and 'E.T.' He's won five Oscars and has the most Oscar nominations of any living person. He started as a jazz pianist named 'Johnny Williams.'",
        "John Williams — Star Wars: The Empire Strikes Back (1980) end-to-end", 75,
        "Listen to 'The Imperial March' — a villain theme so iconic it has its own concert encore. Then 'Han Solo and the Princess' — the film's hidden romantic gem. Williams writes leitmotifs — a melody per character, like Wagner; 'Binary Sunset' is the best. Hum it after one listen.",
        ["Film Score", "Orchestral", "American", "1980s"], 1
    ),

    # ── K-POP ──
    (
        "artist-bts", "Artist",
        "BTS",
        "The South Korean group that became the biggest band in the world — seven members from a small label writing about mental health and youth. 'Dynamite' was their first #1 US single. They've addressed the UN twice, and their fan army ARMY is the most organized in music.",
        "BTS — Map of the Soul: 7 (2020) end-to-end", 75,
        "Listen to 'Black Swan' — the song's lyrics are about the fear of losing your passion for art; the choreography tells the same story in dance. Then 'ON' — the epic closer with a choir and trap drums. BTS blend rap, pop, and balladry; each of the seven members gets a solo moment.",
        ["K-Pop", "Korean", "2020s"], 2
    ),
    (
        "artist-blackpink", "Artist",
        "BLACKPINK",
        "The girl group that made K-pop a global phenomenon — the first K-pop group to headline Coachella. 'DDU-DU DDU-DU' has over 2 billion YouTube views. Their name means 'the prettiest parts aren't the pink' — they kept some edginess. Each member also has a solo career.",
        "BLACKPINK — The Album (2020) end-to-end", 24,
        "Listen to 'How You Like That' — the trap-pop anthem with the iconic 'look up in the sky' choreography moment. Then 'Lovesick Girls' — the emotional pop-rock ballad. BLACKPINK's formula is luxury visuals, hard beats, and four distinct voices trading verses in Korean and English.",
        ["K-Pop", "Korean", "2020s"], 2
    ),
    (
        "artist-psy", "Artist",
        "PSY",
        "'Gangnam Style' was the first YouTube video to reach a billion views — the horse-riding dance became a global meme overnight. PSY was a serious Korean pop star for a decade before that. The song satirizes Seoul's wealthy Gangnam district — most non-Korean fans missed the joke.",
        "PSY — Psy 6甲 (Six Rules), Part 1 (2012) end-to-end", 35,
        "Listen to 'Gangnam Style' with the lyrics open — the song mocks the 'gangnam style' of pretending to be rich. Then 'Oppa Is Just My Style' — the parody of his own hit. PSY's music is pure fun with a wink; the dance is half the song.",
        ["K-Pop", "Korean", "2010s"], 3
    ),
]


# ═══════════════════════════════════════════════════════════════════════════
# JSON generation
# ═══════════════════════════════════════════════════════════════════════════

MAX_CHARS = 450


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
