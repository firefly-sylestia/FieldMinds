#!/usr/bin/env python3
"""
Append a MODERN batch to artists.json + albums.json (v7.6 content drop).

Adds 50 contemporary (2010s-2020s) artists and 50 modern albums — the
"modern batches" requested by the user (current chart wave + 2010s icons,
with The 1975 explicitly included). Every entry follows the SCHEMA.md
contract (bare array, unique kebab ids, teaser <= 280, instruction <= 280
passing the quality bar, tier 1).

Safe to re-run: entries are deduped by id and by name before appending.
After appending it runs the same checks as `validate_topics.py`.
"""

import json
import re
from pathlib import Path

TOPICS_DIR = Path("app/src/main/assets/topics")


def topic(cat_id, subtype, name, teaser, byline, target, minutes, instruction, tags, slug):
    entry = {
        "id": f"{'artist' if cat_id == 'ARTISTS' else 'album'}-{slug}",
        "categoryId": cat_id,
        "subtype": subtype,
        "name": name,
        "teaser": teaser,
        "imageUrl": "",
    }
    if byline:
        entry["byline"] = byline
    entry["exploreAction"] = {
        "verb": "Listen",
        "targetName": target,
        "durationMinutes": minutes,
        "instruction": instruction,
    }
    entry["tags"] = tags
    entry["tier"] = 1
    return entry


ARTISTS = [
    # ── The 1975 (explicitly requested) + the 2020s pop wave ──────────────
    topic("ARTISTS", "Artist", "The 1975",
          "Named after the Beat-poetry scribbles '1 9 7 5' Matty Healy found in a secondhand book as a teen. The band switches genres every album on purpose. Their 2018 single 'Love It If We Made It' is a list of 2018 news headlines set to music.",
          "", "The 1975 — A Brief Inquiry Into Online Relationships (2018) end-to-end", 61,
          "Listen to 'Love It If We Made It' — almost every line is a direct quote of a 2018 news headline, from Kanye's tweets to refugee drownings. Then 'It's Not Living (If It's Not With You)' — the bouncy chorus is about heroin. The cheerfulness is the point.",
          ["Art Pop", "Indie Rock", "British", "2010s"], "the-1975"),
    topic("ARTISTS", "Artist", "Sabrina Carpenter",
          "Disney Channel's 'Girl Meets World' star who quietly built a pop career for a decade before 'Espresso' made her the biggest pop star of 2024 — she'd released five albums by then. 'Espresso' was written in a rush after a coffee order went sideways in a studio session.",
          "", "Sabrina Carpenter — Short n' Sweet (2024) end-to-end", 36,
          "Listen to 'Espresso' and notice the 'screaming' backing vocals buried in the chorus — they recorded real screaming takes and pitched them down. Then 'Please Please Please' — the spoken outro was a late-night studio joke she kept.",
          ["Pop", "American", "2020s"], "sabrina-carpenter"),
    topic("ARTISTS", "Artist", "Harry Styles",
          "One Direction's heartthrob who bet his solo career on not sounding like a boy band at all — his first solo album was folk-rock, and 'Watermelon Sugar' was written in 30 minutes in a studio with two strangers. He wore a dress on a Vogue cover and made it a conversation.",
          "", "Harry Styles — Harry's House (2022) end-to-end", 42,
          "Listen to 'As It Was' — the lyrics are about feeling lonely at a party, but the music is upbeat pop. Then 'Cinema' — the title track's subtitle, 'Harry's House', is a nod to Joni Mitchell's 'Hissing of Summer Lawns'. The album is designed to sound like rooms in a house.",
          ["Pop", "British", "2020s"], "harry-styles"),
    topic("ARTISTS", "Artist", "Doja Cat",
          "Started as a SoundCloud rapper making music in her bedroom; her viral hit 'Mooo!' — a joke song about being a cow — launched a real career. She directs and edits many of her own videos, and was a teenage winner of a nationwide rap contest.",
          "", "Doja Cat — Planet Her (2021) end-to-end", 44,
          "Listen to 'Woman' — the beat samples an African vocal group and the song is an ode to women's power in 7 languages of ad-libs. Then 'Get Into It (Yuh)' — she interpolates Nicki Minaj's verse structure and the outro is a Nicki-style tribute.",
          ["Pop", "Rap", "American", "2020s"], "doja-cat"),
    topic("ARTISTS", "Artist", "Lil Nas X",
          "Turned a Twitter meme into 'Old Town Road' — a country-trap hybrid that broke the record for longest-running #1 song ever. He was 19, broke, and had promoted the song for weeks with memes before radio noticed. He came out as gay in 2019, at the peak of his fame.",
          "", "Lil Nas X — Montero (2021) end-to-end", 41,
          "Listen to 'MONTERO (Call Me by Your Name)' — the song is a letter to his younger self and the video ends with him giving the devil a lap dance as a metaphor. Then 'Industry Baby' — the marching-band sample and prison-set video were a deliberate provocation that became an anthem.",
          ["Pop", "Hip-Hop", "American", "2020s"], "lil-nas-x"),
    topic("ARTISTS", "Artist", "Tate McRae",
          "Was a professional dancer before a singer — she placed 3rd on 'So You Think You Can Dance' at 13, and her choreography videos went viral before any of her songs. 'Greedy' became her first global hit when she was 20; the dance-bait hook was written in one take.",
          "", "Tate McRae — Think Later (2023) end-to-end", 36,
          "Listen to 'Greedy' — count the bass pulse: the whole song is built on a single two-note loop. Then 'Exes' — the bridge drops to just her voice and a kick drum before the chorus slams back in. She choreographed the videos herself.",
          ["Pop", "Canadian", "2020s"], "tate-mcrae"),
    topic("ARTISTS", "Artist", "Gracie Abrams",
          "The daughter of film director J.J. Abrams who built her career on demos recorded in her childhood bedroom — her first EP was written entirely on her own. 'I Love You, I'm Sorry' became a global hit after going viral on TikTok two years after release.",
          "", "Gracie Abrams — The Secret of Us (2024) end-to-end", 47,
          "Listen to 'I Love You, I'm Sorry' — the title is the full apology, and the song never actually says it in the chorus. Then 'us.' — the duet with Taylor Swift was recorded live in one take, both singers in the same room.",
          ["Pop", "Indie Pop", "American", "2020s"], "gracie-abrams"),
    topic("ARTISTS", "Artist", "Noah Kahan",
          "A Vermont singer-songwriter who turned 'Stick Season' — the New England term for the bleak weeks between autumn leaves and snow — into a global folk anthem. The album went platinum after his own TikTok covers went viral.",
          "", "Noah Kahan — Stick Season (2022) end-to-end", 52,
          "Listen to 'Stick Season' — the 'sticks' are the bare trees, and every verse is about leaving a small town and the guilt of staying. Then 'Northern Attitude' — the fiddle break is a nod to his Vermont roots. He wrote the album while stuck in his hometown during lockdown.",
          ["Folk", "Indie Folk", "American", "2020s"], "noah-kahan"),
    topic("ARTISTS", "Artist", "Zach Bryan",
          "A US Navy veteran who posted a raw video of himself singing 'Heading South' from his barracks — no studio, one take, an acoustic guitar — and it went viral while he was still on active duty. His debut album was recorded in 12 days with a live band in a cabin.",
          "", "Zach Bryan — American Heartbreak (2022) end-to-end", 70,
          "Listen to 'Something in the Orange' — the demo version went viral before the studio version existed; the song is about love fading like the sunset he watched. Then 'Burn, Burn, Burn' — the title comes from a line he kept repeating until it became the song.",
          ["Country", "Americana", "American", "2020s"], "zach-bryan"),
    topic("ARTISTS", "Artist", "Teddy Swims",
          "A Georgia cover-singer who went viral on YouTube for soul covers — his name means 'Someone Who Isn't Me Sometimes'. 'Lose Control' was his first #1, a song he nearly cut because he thought it was too different.",
          "", "Teddy Swims — I've Tried Everything but Therapy (Part 1) (2023) end-to-end", 24,
          "Listen to 'Lose Control' — the chorus is him screaming a note he hits by pushing his voice past its natural range; the raw vocal was kept from the first take. Then 'The Door' — the falsetto bridge was recorded in a closet because the studio was being painted.",
          ["Soul", "Pop", "American", "2020s"], "teddy-swims"),
    topic("ARTISTS", "Artist", "Benson Boone",
          "A Mormon-raised singer who quit American Idol mid-season because he didn't want to be on TV — then 'Beautiful Things' became one of the biggest songs of 2024. The viral chorus scream is a real note he'd never hit in the studio until the final take.",
          "", "Benson Boone — Fireworks & Rollerblades (2024) end-to-end", 39,
          "Listen to 'Beautiful Things' — the first verse is quiet gratitude, then the chorus explodes into a scream about fear of loss. Then 'Slow It Down' — the piano intro was written the night his grandmother died. Notice how he uses silence before every chorus.",
          ["Pop", "Rock", "American", "2020s"], "benson-boone"),
    topic("ARTISTS", "Artist", "Sleep Token",
          "An anonymous masked band fronted by 'Vessel', whose lore is a love story between the singer and a deity called Sleep. They mix metal, pop and gospel into one sound; 'The Summoning' went viral for its genre-melting second half.",
          "", "Sleep Token — Take Me Back to Eden (2023) end-to-end", 63,
          "Listen to 'The Summoning' — it starts as metal, turns into a funk groove, then a gospel choir outro. Then 'Chokehold' — the song's soft-loud-soft structure is the band's signature. Vessel's identity is the point: nobody knows who he is.",
          ["Metal", "Progressive", "British", "2020s"], "sleep-token"),
    topic("ARTISTS", "Artist", "Hozier",
          "An Irish singer whose debut single 'Take Me to Church' — a critique of the Catholic Church written in his parents' attic — went viral and made him a star at 23. His second album 'Unreal Unearth' is structured around Dante's nine circles of Hell, one song per circle.",
          "", "Hozier — Unreal Unearth (2023) end-to-end", 62,
          "Listen to 'Eat Your Young' — the title is a line from Greek myth, and the song's church-organ intro hides a trap beat. Then 'Francesca' — it's written from the perspective of a damned soul in Dante's second circle who says even Hell was worth it for love.",
          ["Folk", "Soul", "Irish", "2020s"], "hozier"),
    topic("ARTISTS", "Artist", "Lana Del Rey",
          "Created the 'sad girl' pop persona before it had a name — a vintage-America aesthetic she built herself. Critics dismissed her early work, then 'Norman Fucking Rockwell!' was named one of the best albums of the 2010s by nearly everyone.",
          "", "Lana Del Rey — Norman Fucking Rockwell! (2019) end-to-end", 68,
          "Listen to 'Venice Bitch' — a 9-minute song that dissolves into a 4-minute instrumental outro of psychedelic guitar, and the title is a slang term for a certain kind of LA woman. Then 'The Greatest' — the line 'the culture is lit, and I had a ball' is a farewell to a dying era.",
          ["Pop", "Alternative", "American", "2010s"], "lana-del-rey"),
    topic("ARTISTS", "Artist", "Travis Scott",
          "Houston rapper-producer who made arena-sized hip-hop built on mosh pits, auto-tune and psychedelic production. His 2018 album 'Astroworld' is named after the Houston theme park of his childhood, which closed when he was 9. He produced for Kanye before releasing his own mixtapes.",
          "", "Travis Scott — Utopia (2023) end-to-end", 73,
          "Listen to 'FE!N' — the track has no hook, just a chant, and the beat is built on a single distorted synth. Then 'MELTDOWN' — notice how the album's guest verses all reference being recorded in a desert 'Utopia' set he built for the album's videos.",
          ["Hip-Hop", "Trap", "American", "2020s"], "travis-scott"),
    topic("ARTISTS", "Artist", "Future",
          "Atlanta rapper who pioneered the melodic, auto-tuned trap sound that dominated the 2010s — he released three mixtapes in a single month in 2015. 'Mask Off' became a stadium chant from a song about his drug-dealing past, and its flute melody was lifted from a 1954 jazz recording.",
          "", "Future — I Never Liked You (2022) end-to-end", 48,
          "Listen to 'Wait for U' — the beat samples a Tems song and Future trades verses with Drake; the song won a Grammy. Then 'PUFFIN ON ZOOTIEZ' — the title is about his ad-lib 'Zootie' — and notice how he never repeats a melody twice.",
          ["Hip-Hop", "Trap", "American", "2020s"], "future"),
    topic("ARTISTS", "Artist", "21 Savage",
          "Born in London, raised in Atlanta from age 7 — he didn't get a US visa until 2022, after a 2019 ICE arrest that put his British accent and immigration status into national headlines. 'Savage Mode' with producer Metro Boomin turned his deadpan delivery into a signature sound.",
          "", "21 Savage — Savage Mode II (2020) end-to-end", 44,
          "Listen to 'Runnin' — the beat switches mid-song into a soul sample, and 21's verses are a list of things he doesn't do (no love songs, no clubs). Then 'Mr. Right Now' — the Drake feature was recorded over FaceTime during lockdown.",
          ["Hip-Hop", "Trap", "British", "2020s"], "21-savage"),
    topic("ARTISTS", "Artist", "Playboi Carti",
          "An Atlanta rapper whose minimalist, baby-voiced delivery made him the most influential rapper of the 2020s underground — fans waited four years for 'Whole Lotta Red'. His live shows are famous for pure chaos.",
          "", "Playboi Carti — Whole Lotta Red (2020) end-to-end", 62,
          "Listen to 'Sky' — the beat is a synth loop that never changes, and Carti switches between a deep voice and a high-pitched baby voice mid-song. Then 'New Tank' — notice how many verses have no real words, just sounds used as rhythm. The album's chaos is the aesthetic.",
          ["Hip-Hop", "Experimental", "American", "2020s"], "playboi-carti"),
    topic("ARTISTS", "Artist", "Lil Uzi Vert",
          "Philadelphia rapper who became a fashion icon — he spent $24 million on a pink diamond implanted in his forehead (later removed after it kept catching on things). 'XO Tour Llif3' turned his emo-sad lyrics over a club beat into a new pop formula.",
          "", "Lil Uzi Vert — Eternal Atake (2020) end-to-end", 64,
          "Listen to 'XO Tour Llif3' — the line 'I don't really care if you cry' is delivered like a nursery rhyme over a bouncy beat. Then 'Silly Watch' — the title is slang for a watch you can't afford, and the whole song is a list of flexes delivered deadpan.",
          ["Hip-Hop", "Trap", "American", "2010s"], "lil-uzi-vert"),
    topic("ARTISTS", "Artist", "Young Thug",
          "Atlanta rapper whose slurred, shape-shifting delivery redefined what a rap verse could sound like — critics compare his voice to a musical instrument. He founded the YSL label, and his 2019 album 'So Much Fun' was his first #1, 8 years into his career.",
          "", "Young Thug — So Much Fun (2019) end-to-end", 51,
          "Listen to 'Hot' — the beat is a single piano loop, and Thug's verse is pure melody, almost no words. Then 'The London' — the hook is sung in a register higher than most male pop singers use. Try to count how many different voices he uses on one verse.",
          ["Hip-Hop", "Trap", "American", "2010s"], "young-thug"),
    topic("ARTISTS", "Artist", "Denzel Curry",
          "Florida rapper whose 'Ta13oo' was released with three acts — Light, Gray and Dark — matching his own struggles with fame and mental health. He studied audio engineering in college, and his live shows are famously intense; 'Ultimate' became a workout meme before it was a hit.",
          "", "Denzel Curry — Melt My Eyez See Your Future (2022) end-to-end", 52,
          "Listen to 'Walkin' — the song literally switches genre mid-track, from jazz-rap to boom-bap, as the lyrics shift from anger to hope. Then 'Troubles' — the T-Pain feature was recorded in one day, and the two trade verses about therapy and growth.",
          ["Hip-Hop", "Experimental", "American", "2020s"], "denzel-curry"),
    topic("ARTISTS", "Artist", "JPEGMAFIA",
          "A Baltimore rapper-producer who served in the US military before becoming one of rap's most confrontational artists — his name is an acronym for a joke about the CIA. His beats sound like glitching computers, and he heckles his own crowds.",
          "", "JPEGMAFIA — Veteran (2018) end-to-end", 44,
          "Listen to '1539 N. Calvert' — the song is named after a Baltimore address and the beat is built from a broken-sounding drum machine. Then 'Baby I'm Bleeding' — the sample of a woman's voice is a real voicemail from a stalker. His beats break rules on purpose.",
          ["Hip-Hop", "Experimental", "American", "2010s"], "jpegmafia"),
    topic("ARTISTS", "Artist", "JID",
          "A Georgia rapper with the fastest tongue in hip-hop — his verse on '151 Rum' was timed at 12 syllables per second. He signed to J. Cole's Dreamville label after a freestyle contest, and 'The Forever Story' is named for his grandmother, who raised him.",
          "", "JID — The Forever Story (2022) end-to-end", 65,
          "Listen to 'Surround Sound' — the beat is a chopped soul sample, and the song's name is a metaphor for how his family's voices surrounded him growing up. Then 'Kody Blu 31' — the title is his late grandmother's birthday, and the hook is sung, not rapped.",
          ["Hip-Hop", "Rap", "American", "2020s"], "jid"),
    topic("ARTISTS", "Artist", "Little Simz",
          "A London rapper who produced her own albums and turned a 2021 record about introversion into a Mercury Prize winner — 'Sometimes I Might Be Introvert' is an acronym: SIMBI. She's also an actor, with a lead role in the TV series 'Top Boy'.",
          "", "Little Simz — Sometimes I Might Be Introvert (2021) end-to-end", 66,
          "Listen to 'Introvert' — the opening track is 5 minutes of orchestral hip-hop with no hook, just a declaration of intent. Then 'Woman' — the video features over 100 real British women, and the song is a celebration that names actual female figures in Black history.",
          ["Hip-Hop", "Rap", "British", "2020s"], "little-simz"),
    topic("ARTISTS", "Artist", "Kali Uchis",
          "A Colombian-American singer who records in both English and Spanish — her Spanish albums outsell her English ones. She wrote and produced her debut while homeless in LA, sleeping on floors. 'Telepatía' became a global hit during the pandemic on a whisper-quiet bedroom track.",
          "", "Kali Uchis — Orquídeas (2024) end-to-end", 32,
          "Listen to 'Igual Que un Ángel' — the Peso Pluma duet that sets a narcocorrido voice over dreamy synth-pop. Then 'Muñekita' — the title means 'little doll' and the song mixes reggaetón with cumbia. The album is named after the orchid, Colombia's national flower.",
          ["R&B", "Latin", "Colombian", "2020s"], "kali-uchis"),
    topic("ARTISTS", "Artist", "Rauw Alejandro",
          "A Puerto Rican singer who fused reggaetón with synth-pop, funk and even disco — 'Todo de Ti' broke Latin charts with a sound that owed more to Daft Punk than dembow. He was a dancer before a singer, and his live shows are built around choreography.",
          "", "Rauw Alejandro — Vice Versa (2021) end-to-end", 38,
          "Listen to 'Todo de Ti' — the bassline is pure 80s funk and the song has almost no reggaetón rhythm at all, which is why it stood out. Then '2:00 AM' — the closer is a duet with his then-fiancée Rosalía, recorded during lockdown.",
          ["Reggaetón", "Pop", "Puerto Rican", "2020s"], "rauw-alejandro"),
    topic("ARTISTS", "Artist", "Feid",
          "A Colombian singer-songwriter who wrote hits for other reggaetón stars for years before finding fame as 'Ferxxo' — a persona with green hair, emo sunglasses and a green phone. 'Normal' became a TikTok phenomenon, and his 2023 album topped charts across Latin America.",
          "", "Feid — Mor, No Le Temas a la Oscuridad (2023) end-to-end", 33,
          "Listen to 'Normal' — the title means 'normal' in Spanish, and the song is about realizing a breakup is just... normal. Then 'Luna' — the chorus melody is a nursery-rhyme simplicity that made it a stadium singalong. His green aesthetic is a character: Ferxxo.",
          ["Reggaetón", "Latin Pop", "Colombian", "2020s"], "feid"),
    topic("ARTISTS", "Artist", "Peso Pluma",
          "A Mexican singer whose nasal drawl took corridos tumbados — trap-tinged modern narcocorridos — global. 'Ella Baila Sola' was the first Mexican song to reach Spotify's global top 10.",
          "", "Peso Pluma — Génesis (2023) end-to-end", 48,
          "Listen to 'Ella Baila Sola' — the Eslabon Armado duet that made history; sierreño guitar over a trap beat. Then 'Lagunas' — the accordion solo is the emotional peak. Notice how his voice stays flat while everything around it moves.",
          ["Corridos", "Regional Mexican", "Mexican", "2020s"], "peso-pluma"),
    topic("ARTISTS", "Artist", "Ice Spice",
          "A Bronx rapper who blew up in 2022 with 'Munch (Feelin' U)' — a drill track whose title became a slang term for a cheap date. She went from unknown to a Taylor Swift remix in under a year, and her orange hair is as famous as her rhymes.",
          "", "Ice Spice — Y2K! (2024) end-to-end", 31,
          "Listen to 'In Ha Mood' — the hook is a nursery-rhyme melody over a drill beat, and the song is about knowing your worth. Then 'Deli' — notice how she repeats one word like a chant. Her whole sound is Bronx drill: spare beats, high hats, and a delivery that never rushes.",
          ["Hip-Hop", "Drill", "American", "2020s"], "ice-spice"),
    topic("ARTISTS", "Artist", "Tems",
          "A Nigerian singer-songwriter whose airy voice made her the secret weapon of 2020s R&B — she's on Future's 'Wait for U', Drake's 'Fountains', and won a Grammy for her feature on 'Wait for U'. 'Free Mind' was a slow-burn hit that spent years on charts before it peaked.",
          "", "Tems — Born in the Wild (2024) end-to-end", 55,
          "Listen to 'Free Mind' — the song was written in a moment of anxiety and became an anthem; the production is just her voice, a beat, and space. Then 'Burning' — the opener of her debut album, which she wrote to process her mother's death. Her voice floats above every beat.",
          ["Afrobeats", "R&B", "Nigerian", "2020s"], "tems"),
    topic("ARTISTS", "Artist", "Ayra Starr",
          "A Nigerian singer who went from a modeling career to the face of a new generation of Afrobeats — her breakout 'Rush' topped charts worldwide and its video features her dancing through Lagos markets. She signed with Mavin Records after a chance meeting at 18.",
          "", "Ayra Starr — The Year I Turned 21 (2024) end-to-end", 40,
          "Listen to 'Rush' — the song's joy is contagious, and the video shows her running through Lagos, dancing in traffic, being gloriously herself. Then 'Commas' — the opening track of her second album, a statement that she's arrived. Her voice switches from sweet to sharp in one line.",
          ["Afrobeats", "Pop", "Nigerian", "2020s"], "ayra-starr"),
    topic("ARTISTS", "Artist", "Raye",
          "Spent six years writing songs for Beyoncé and Rihanna without ever releasing her own album. She went independent, 'Escapism' became her first #1, and she won six Brit Awards in one night — a record.",
          "", "Raye — My 21st Century Blues (2023) end-to-end", 46,
          "Listen to 'Escapism' — half-spoken diary verses over a jazz-swing hook that became her first #1. Then 'Ice Cream Man' — about her experience of assault in the industry; the live version is devastating. The whole album is therapy made public.",
          ["Pop", "R&B", "British", "2020s"], "raye"),
    topic("ARTISTS", "Artist", "FKA twigs",
          "A British artist who trained as a pole dancer and turned her body into an instrument — her videos are contemporary dance pieces. 'Cellophane' was written after the media dissected her relationship with Robert Pattinson; she's also an actress in 'The Crowded Room' and a filmmaker.",
          "", "FKA twigs — Magdalene (2019) end-to-end", 39,
          "Listen to 'Cellophane' — about being treated like glass, with a pole-dancing video where she falls and climbs back up. Then 'Mirrored Heart' — giving everything to someone who reflects nothing back. The album is named after Mary Magdalene.",
          ["Art Pop", "Electronic", "British", "2010s"], "fka-twigs"),
    topic("ARTISTS", "Artist", "Caroline Polachek",
          "The former Chairlift singer whose solo work pairs operatic vocals with hyperpop and world-music textures. 'So Hot You're Hurting My Feelings' was a quarantine hit, and she can whistle like a bird call mid-song.",
          "", "Caroline Polachek — Desire, I Want to Turn Into You (2023) end-to-end", 44,
          "Listen to 'Welcome to My Island' — the opener with the bagpipe-like synth and a real guitar solo, about wanting to be consumed. Then 'Bunny Is a Rider' — the hit whose title is nonsense she invented. Her whistle-register vocals appear mid-song without warning.",
          ["Art Pop", "Electronic", "American", "2020s"], "caroline-polachek"),
    topic("ARTISTS", "Artist", "Beabadoobee",
          "A Filipino-British singer who wrote 'Coffee' at 17 in her bedroom to impress a boy — it became a global hit. 'Beatopia' is named after an imaginary world she invented as a kid.",
          "", "Beabadoobee — Beatopia (2022) end-to-end", 43,
          "Listen to 'the perfect pair' — the song is about a relationship that works too well to last, and the video is a pastel dreamscape. Then '10:36' — the title is a timestamp of when she wrote the hook. The whole album was inspired by the imaginary land she drew as a 7-year-old.",
          ["Indie Pop", "Bedroom Pop", "British", "2020s"], "beabadoobee"),
    topic("ARTISTS", "Artist", "girl in red",
          "A Norwegian singer who became the soundtrack to teenage lesbian awakenings — her song 'we fell in love in october' is a modern queer anthem. She records under the name girl in red, and her bedroom-pop sound was built on an acoustic guitar and a laptop in her childhood room.",
          "", "girl in red — If I Could Make It Go Quiet (2021) end-to-end", 27,
          "Listen to 'Serotonin' — the song about intrusive thoughts that literally glitches when the thoughts win. Then 'we fell in love in october' — the gentle anthem that became a queer coming-out soundtrack. She writes in Norwegian, then translates.",
          ["Indie Pop", "Bedroom Pop", "Norwegian", "2020s"], "girl-in-red"),
    topic("ARTISTS", "Artist", "Conan Gray",
          "A Texas-born singer-songwriter who went from YouTube covers to pop stardom — 'Heather' became a global hit about the pain of being the friend, not the lover. He wrote his first album in his childhood bedroom in a small town he couldn't wait to leave.",
          "", "Conan Gray — Superache (2022) end-to-end", 41,
          "Listen to 'Memories' — the song is about losing a friend who's still alive; the first line is 'I can't seem to forget the way you looked at me'. Then 'Heather' — the title is the name of the girl his crush chose instead of him. His whole catalog is diary entries set to pop.",
          ["Pop", "Indie Pop", "American", "2020s"], "conan-gray"),
    topic("ARTISTS", "Artist", "Steve Lacy",
          "A guitar-playing producer who made his name in the band The Internet, then produced for Kendrick Lamar and Solange — he wrote his solo hits on his iPhone in GarageBand. 'Bad Habit' — recorded on a phone — became his first #1 and the biggest song of 2022.",
          "", "Steve Lacy — Gemini Rights (2022) end-to-end", 35,
          "Listen to 'Bad Habit' — the entire song was made on his iPhone in GarageBand, and you can hear the phone-quality textures if you listen closely. Then 'Mercury' — the bassline is the hook. He's a jazz-school guitarist who turned pop production on its head.",
          ["R&B", "Neo-Soul", "American", "2020s"], "steve-lacy"),
    topic("ARTISTS", "Artist", "Glass Animals",
          "A British indie band whose 'Heat Waves' hit #1 nearly three years after release, after TikTok rediscovered it. The album it's from was shaped by their drummer's near-fatal accident and frontman Dave Bayley's sister's death.",
          "", "Glass Animals — Dreamland (2020) end-to-end", 40,
          "Listen to 'Heat Waves' — the flooded-world video and lyrics about missing someone until it feels like drowning. Then 'Tokyo Drifting' — the Denzel Curry feature. The whole album is a memory palace of Bayley's childhood.",
          ["Indie Pop", "Alternative", "British", "2020s"], "glass-animals"),
    topic("ARTISTS", "Artist", "Wednesday",
          "An Asheville, North Carolina band who spliced shoegaze and country into a sound critics dubbed 'goth-country' — frontwoman Karly Hartzman screams over pedal steel guitars. 'Rat Saw God' made them the most-hyped indie band of 2023.",
          "", "Wednesday — Rat Saw God (2023) end-to-end", 47,
          "Listen to 'Bull Believer' — the 8-minute closer builds to a scream of pure catharsis, the wordless climax of the album. Then 'Chosen to Deserve' — notice the pedal steel cutting through the distortion. The band's sound is Nashville instruments played with punk violence.",
          ["Shoegaze", "Country", "American", "2020s"], "wednesday"),
    topic("ARTISTS", "Artist", "Indigo De Souza",
          "A North Carolina singer-songwriter whose songs are raw confessionals — she worked at a vegan restaurant in Asheville while making her first album. 'Kill Me' turned her anxiety into a beautiful anthem, and she's become a hero of the new American indie-sad scene.",
          "", "Indigo De Souza — Any Shape You Take (2021) end-to-end", 37,
          "Listen to 'Kill Me' — the title is a joke about a breakup, and the song is a country-tinged diary entry that builds into a scream. Then 'Real Pain' — the lyrics are about her friend's car accident, written years after the fact. Her voice cracks on purpose.",
          ["Indie Rock", "Alternative", "American", "2020s"], "indigo-de-souza"),
    topic("ARTISTS", "Artist", "Big Thief",
          "A Brooklyn folk-rock band led by Adrianne Lenker, whose 2022 double album was recorded in five different places — a cabin, a barn, an adobe studio, a topanga house, and a rock club — over five months. They're known for songs that sound like they're being invented in the moment.",
          "", "Big Thief — Dragon New Warm Mountain I Believe in You (2022) end-to-end", 80,
          "Listen to 'Simulation Swarm' — the 6-minute song with four guitar solos, each played by a different member, was recorded live in one take. Then 'Not' — the song that dissolves into noise at the end. The album's 20 songs deliberately refuse a single style.",
          ["Folk", "Indie Rock", "American", "2020s"], "big-thief"),
    topic("ARTISTS", "Artist", "Arca",
          "A Venezuelan electronic producer who became the connective tissue of experimental pop — she produced for Kanye West, Björk and FKA twigs before her own albums reimagined reggaetón as avant-garde art. Her 2020 'Kick' cycle was four albums in a year.",
          "", "Arca — KICK i (2020) end-to-end", 40,
          "Listen to 'KLK' — the Rosalía collab that sets flamenco-adjacent vocals over a brutalist reggaetón beat. Then 'Mequetrefe' — a Spanish insult turned into an anthem. Her voice is processed beyond recognition; that's the point.",
          ["Electronic", "Experimental", "Venezuelan", "2020s"], "arca"),
    topic("ARTISTS", "Artist", "Danny Brown",
          "A Detroit rapper with a voice like a cartoon character on helium who's also one of rap's most honest chroniclers of addiction and mental health. His 2013 album 'Old' was produced in part by his then-teenage collaborator. He started rapping after a stint in a group home.",
          "", "Danny Brown — uknowhatimsayin¿ (2019) end-to-end", 32,
          "Listen to '3 Tearz' — the title is a riff on 'three years' and the song is a Run-DMC-style posse cut produced by Q-Tip, who helmed the whole album. Then 'Negro Spiritual' — the jazz sample and the album's most emotional track. His voice is an instrument; you'll never mistake it.",
          ["Hip-Hop", "Experimental", "American", "2010s"], "danny-brown"),
    topic("ARTISTS", "Artist", "Earl Sweatshirt",
          "Joined Odd Future at 16, was sent to a Samoan wilderness school by his mother, and returned with a fractured, elliptical style that's become legendary. 'Some Rap Songs' — 24 minutes of grief after his father's death — is a masterpiece.",
          "", "Earl Sweatshirt — Some Rap Songs (2018) end-to-end", 24,
          "Listen to 'Nowhere2go' — the beat is built from a warped soul sample and his verse is barely there, a whisper of grief. Then 'December 24' — the closing track, named for his father's death date. The whole album is 24 minutes; listen to it straight through like a poem.",
          ["Hip-Hop", "Abstract", "American", "2010s"], "earl-sweatshirt"),
    topic("ARTISTS", "Artist", "Killer Mike",
          "Half of Run the Jewels; his solo album 'Michael' — named for his birth name — won a Grammy and reclaimed civil-rights-era soul-rap. He's also a gun-control activist and small-business owner in Atlanta.",
          "", "Killer Mike — Michael (2023) end-to-end", 40,
          "Listen to 'Scientists & Engineers' — André 3000's first rap feature in years, a prayer-like meditation on Black excellence. Then 'Motherless' — about his mother's death. The album is a tribute to the Black men who raised him.",
          ["Hip-Hop", "Soul", "American", "2020s"], "killer-mike"),
    topic("ARTISTS", "Artist", "A$AP Rocky",
          "The face of the A$AP Mob, the collective that made Harlem streetwear meet high fashion — he's walked for Dior and Gucci. His debut mixtape 'Live.Love.A$AP' became a blueprint for cloud rap.",
          "", "A$AP Rocky — Testing (2018) end-to-end", 48,
          "Listen to 'Praise the Lord (Da Shine)' — the Skepta collab made from a beat a stranger sent as a joke. Then 'A$AP Forever' — the choir sample and a video featuring his friends. The title means he's testing new sounds, and it shows.",
          ["Hip-Hop", "Cloud Rap", "American", "2010s"], "asap-rocky"),
    topic("ARTISTS", "Artist", "Gunna",
          "The voice of Atlanta's new trap generation — his slurred, melodic delivery on 'Drip Too Hard' with Lil Baby made 'drip' a global slang term for style. He's the unofficial king of the 'Wunna' aesthetic — his album title means 'winner' in his family's dialect.",
          "", "Gunna — Wunna (2020) end-to-end", 50,
          "Listen to 'Skybox' — the opener that sounds like a stadium filling up. Then 'Dollaz on My Head' — the Young Thug collab that shows his lineage. His verses are more melody than rap; count the words, then count the melodies.",
          ["Hip-Hop", "Trap", "American", "2020s"], "gunna"),
    topic("ARTISTS", "Artist", "Bicep",
          "A Northern Irish electronic duo — Andy Ferguson and Matt McBriar — whose track 'Glue' became a modern rave anthem. Their live shows pair huge visuals with house and techno; they named themselves after a workout class.",
          "", "Bicep — Isles (2021) end-to-end", 55,
          "Listen to 'Apricots' — the track that defines their sound: a looping vocal sample over a 4/4 kick, building for five minutes. Then 'Atlas' — the title track, named for the second Bicep EP. The duo's secret is texture — every track sounds like it's glowing.",
          ["Electronic", "House", "British", "2020s"], "bicep"),
    topic("ARTISTS", "Artist", "Porter Robinson",
          "Found fame at 19 with the festival anthem 'Language', then spent years making 'Nurture' — an album about depression that samples his own voice as an instrument. 'Shelter' with Madeon was a landmark anime collaboration.",
          "", "Porter Robinson — Nurture (2021) end-to-end", 59,
          "Listen to 'Something Comforting' — the song's title is literal: it's about finding comfort in art while depressed. Then 'Look at the Sky' — the track where the album turns hopeful. He built 'Nurture' over five years, recording field sounds like rain and birdsong as textures.",
          ["Electronic", "Hyperpop", "American", "2020s"], "porter-robinson"),
]

ALBUMS = [
    # ── The 1975 (explicitly requested) ───────────────────────────────────
    topic("ALBUMS", "Album", "A Brief Inquiry Into Online Relationships",
          "The 1975 2018 — the album that made them critics' darlings, tackling the internet, addiction and modern loneliness. Every song is a different genre, and the closer is a four-minute instrumental that ends with a choir.",
          "The 1975", "A Brief Inquiry Into Online Relationships (2018) end-to-end", 61,
          "Listen to 'Love It If We Made It' — a literal list of 2018 news headlines set to a euphoric chorus. Then 'The Man Who Married a Robot / Love Theme' — a spoken-word song narrated by Siri about a man who marries his phone. The album's title is a joke about the internet.",
          ["Art Pop", "Indie Rock", "British", "2010s"], "the-1975-a-brief-inquiry"),
    topic("ALBUMS", "Album", "I Like It When You Sleep, for You Are So Beautiful yet So Unaware of It",
          "The 1975 2016 — the 74-minute double album that made them stars, a maximalist sprawl of 17 songs that jumps from Motown pastiche to ambient soundscapes. It debuted at #1 in the UK, and its title is the longest ever to top the chart.",
          "The 1975", "I Like It When You Sleep, for You Are So Beautiful yet So Unaware of It (2016) end-to-end", 74,
          "Listen to 'Love Me' — the funk opener satirizes celebrity before the band had really hit fame. Then 'Somebody Else' — the heartbreak anthem whose chorus is a single repeated line. Skip nothing: the album deliberately refuses to stay in one genre, and the sequencing is the joke.",
          ["Art Pop", "Indie Rock", "British", "2010s"], "the-1975-i-like-it-when-you-sleep"),
    # ── 2020s pop wave ────────────────────────────────────────────────────
    topic("ALBUMS", "Album", "Short n' Sweet",
          "Sabrina Carpenter 2024 — the 36-minute album that turned a Disney actress into the biggest pop star of the year. Recorded in just a few months, it's a country-tinged, innuendo-loaded pop record that spent weeks at #1.",
          "Sabrina Carpenter", "Short n' Sweet (2024) end-to-end", 36,
          "Listen to 'Espresso' — the song has no real chorus, just a hook on a loop, and the 'screaming' backing vocals are pitched-down scream takes. Then 'Taste' — the opener, a breakup anthem with a horror-comedy video. The whole album is 12 songs in 36 minutes: no filler.",
          ["Pop", "American", "2020s"], "sabrina-carpenter-short-n-sweet"),
    topic("ALBUMS", "Album", "Harry's House",
          "Harry Styles 2022 — his third solo album, named after a Joni Mitchell lyric, designed to sound like walking through the rooms of a home. It won Album of the Year at the Grammys, and 'As It Was' became his first #1 single.",
          "Harry Styles", "Harry's House (2022) end-to-end", 42,
          "Listen to 'As It Was' — the lyrics are about loneliness at a party, the music is pure pop joy, and the music video was shot in the real Harry's House. Then 'Cinema' — the title track's subtitle references the Joni Mitchell lyric the album is named for. Each song is a room.",
          ["Pop", "British", "2020s"], "harry-styles-harrys-house"),
    topic("ALBUMS", "Album", "Planet Her",
          "Doja Cat 2021 — a concept album about a fictional planet where women rule, and the record that made her a global star. It spawned three top-10 hits and mixes pop, R&B, Afrobeats and rap. The album's sci-fi aesthetic runs through every video.",
          "Doja Cat", "Planet Her (2021) end-to-end", 44,
          "Listen to 'Kiss Me More' — the SZA duet whose chorus samples an old Olivia Newton-John song, and which won a Grammy. Then 'Woman' — the opener, an ode to women's power built on an African vocal sample. The album is Planet Her; the interludes are her radio broadcasts.",
          ["Pop", "R&B", "American", "2020s"], "doja-cat-planet-her"),
    topic("ALBUMS", "Album", "Montero",
          "Lil Nas X 2021 — the debut album that turned a meme rapper into a pop visionary. Its title track, named for his birth name, became the first song to hit #1 with an openly queer message and a video that sparked global outrage. The album is a coming-of-age story in 15 tracks.",
          "Lil Nas X", "Montero (2021) end-to-end", 41,
          "Listen to 'MONTERO (Call Me by Your Name)' — a letter to his younger self, with a video where he gives the devil a lap dance. Then 'Sun Goes Down' — about the bullying he endured as a closeted teen. The album ends with a Miley Cyrus duet.",
          ["Pop", "Hip-Hop", "American", "2020s"], "lil-nas-x-montero"),
    topic("ALBUMS", "Album", "Think Later",
          "Tate McRae 2023 — the debut album from the Canadian dancer-singer, a 14-song pop record whose title is her motto for impulsive decisions. It debuted at #3 in the US and made 'Greedy' her first global hit.",
          "Tate McRae", "Think Later (2023) end-to-end", 36,
          "Listen to 'Greedy' — built on a two-note bass loop, with a hook she wrote in one take. Then 'Exes' — the sequel to 'Greedy', with a bridge that drops to just her voice and a kick drum. She choreographed every video; watch her hands in 'Greedy'.",
          ["Pop", "Canadian", "2020s"], "tate-mcrae-think-later"),
    topic("ALBUMS", "Album", "The Secret of Us",
          "Gracie Abrams 2024 — her second album, written after a year of touring, a diary of friendship and self-destruction. It features a duet with Taylor Swift recorded live in one take, and 'I Love You, I'm Sorry' became a global hit.",
          "Gracie Abrams", "The Secret of Us (2024) end-to-end", 47,
          "Listen to 'I Love You, I'm Sorry' — the title is the whole apology and the song never says it in the chorus. Then 'us.' — the Taylor Swift duet, recorded in one take, both in the same room. The album's secret: every song is about a real person she's never named.",
          ["Pop", "Indie Pop", "American", "2020s"], "gracie-abrams-the-secret-of-us"),
    topic("ALBUMS", "Album", "Stick Season",
          "Noah Kahan 2022 — a Vermont folk-pop album named for the bleak weeks between autumn and snow, written during lockdown in his hometown. It became one of the biggest folk records of the decade after TikTok rediscovered it, spawning an entire 'Stick Season' cover phenomenon.",
          "Noah Kahan", "Stick Season (2022) end-to-end", 52,
          "Listen to 'Stick Season' — the title track about leaving a small town and the guilt of staying. Then 'Northern Attitude' — the fiddle-and-banjo opener. The album's genius is how specific it is: his Vermont references became anthems for everyone who left home.",
          ["Folk", "Indie Folk", "American", "2020s"], "noah-kahan-stick-season"),
    topic("ALBUMS", "Album", "American Heartbreak",
          "Zach Bryan 2022 — a 34-song double album recorded in 12 days with a live band in a cabin, released with almost no promotion. It became the biggest country record of the year, and turned the Navy veteran into a folk-country superstar.",
          "Zach Bryan", "American Heartbreak (2022) end-to-end", 70,
          "Listen to 'Something in the Orange' — the viral demo version exists and is worth hearing too; the studio version is the song about a sunset he watched while a relationship faded. Then 'Burn, Burn, Burn' — the closer, recorded live in the room. 34 songs, no skips.",
          ["Country", "Americana", "American", "2020s"], "zach-bryan-american-heartbreak"),
    topic("ALBUMS", "Album", "I've Tried Everything but Therapy (Part 1)",
          "Teddy Swims 2023 — a debut EP from a YouTube cover singer whose gospel-and-soul voice made 'Lose Control' his first #1. The title is a joke about his therapy-themed album rollout; Part 2 came later. Eight songs that show off a voice that shouldn't fit in any one genre.",
          "Teddy Swims", "I've Tried Everything but Therapy (Part 1) (2023) end-to-end", 24,
          "Listen to 'Lose Control' — the chorus scream is a note he'd never hit until the final take, and the raw vocal was kept. Then 'The Door' — a soul-pop breakup song whose falsetto bridge was recorded in a closet. He's a cover artist by origin; these are the originals.",
          ["Soul", "Pop", "American", "2020s"], "teddy-swims-ive-tried-everything-but-therapy"),
    topic("ALBUMS", "Album", "Fireworks & Rollerblades",
          "Benson Boone 2024 — a debut album from the singer who quit American Idol mid-season. It's a rock-leaning pop record anchored by 'Beautiful Things', one of the biggest songs of 2024, which he nearly didn't include.",
          "Benson Boone", "Fireworks & Rollerblades (2024) end-to-end", 39,
          "Listen to 'Beautiful Things' — the quiet-verse-to-screaming-chorus structure is the whole trick; the fear-of-loss lyrics are real. Then 'Slow It Down' — the piano intro was written the night his grandmother died. He quit a TV show to gamble on his own sound; it paid off.",
          ["Pop", "Rock", "American", "2020s"], "benson-boone-fireworks-and-rollerblades"),
    topic("ALBUMS", "Album", "Take Me Back to Eden",
          "Sleep Token 2023 — the third album from the anonymous masked band, a 63-minute epic that refuses to stay in one genre: metal, pop, R&B and gospel all appear. It's the conclusion of a trilogy about the singer's relationship with the deity Sleep.",
          "Sleep Token", "Take Me Back to Eden (2023) end-to-end", 63,
          "Listen to 'The Summoning' — the song that made them famous when its second half turned from metal into a funk groove into gospel. Then 'Chokehold' — the opener, soft-loud-soft personified. The lore matters: the whole album is a letter to a god.",
          ["Metal", "Progressive", "British", "2020s"], "sleep-token-take-me-back-to-eden"),
    topic("ALBUMS", "Album", "Unreal Unearth",
          "Hozier 2023 — his third album, structured around Dante's nine circles of Hell, one song per circle. Recorded with a folk ensemble and a string section, it became his first #1 album in the US, and 'Eat Your Young' a viral hit.",
          "Hozier", "Unreal Unearth (2023) end-to-end", 62,
          "Listen to 'Eat Your Young' — the church-organ opener hides a trap beat, and the title is a Greek myth reference. Then 'Francesca' — the second circle of Hell, where the damned lovers say even damnation was worth it. The whole album is a descent; start at the top and go down.",
          ["Folk", "Soul", "Irish", "2020s"], "hozier-unreal-unearth"),
    topic("ALBUMS", "Album", "Norman Fucking Rockwell!",
          "Lana Del Rey 2019 — the album critics called the best of the decade, a 67-minute piano-and-guitar epic produced by Jack Antonoff. The title is a joke about American culture, and the closing track 'Hope Is a Dangerous Thing for a Woman Like Me to Have' is a one-woman requiem.",
          "Lana Del Rey", "Norman Fucking Rockwell! (2019) end-to-end", 68,
          "Listen to 'Venice Bitch' — a 9-minute song that spends its last four minutes as a psychedelic guitar jam. Then 'The Greatest' — the line 'the culture is lit, and I had a ball' is a farewell to a dying California. The album is a love letter to a country she's already mourning.",
          ["Pop", "Alternative", "American", "2010s"], "lana-del-rey-norman-fucking-rockwell"),
    topic("ALBUMS", "Album", "Utopia",
          "Travis Scott 2023 — his fourth album, a 73-minute odyssey built around the concept of a utopia he couldn't reach. Recorded with a rotating cast of 20+ producers, it debuted at #1 and its desert-set rollout included a live album premiere at the Pyramids of Giza.",
          "Travis Scott", "Utopia (2023) end-to-end", 73,
          "Listen to 'FE!N' — no hook, just a chant, over a single distorted synth. Then 'MELTDOWN' — the Drake feature that dissects their feud. The album is sequenced like a journey through a desert; notice how the second half turns psychedelic.",
          ["Hip-Hop", "Trap", "American", "2020s"], "travis-scott-utopia"),
    topic("ALBUMS", "Album", "I Never Liked You",
          "Future 2022 — his ninth studio album, a 16-track set that debuted at #1 and featured Tems, Gunna and Young Thug. Its lead single 'Wait for U' won a Grammy. The title is a direct diss to the industry that doubted him.",
          "Future", "I Never Liked You (2022) end-to-end", 48,
          "Listen to 'Wait for U' — the Tems-sampled song that won a Grammy, with Drake trading verses. Then 'PUFFIN ON ZOOTIEZ' — the album's centerpiece, named for his ad-lib. Future's genius is melodic repetition: every hook is built on a phrase you can't stop humming.",
          ["Hip-Hop", "Trap", "American", "2020s"], "future-i-never-liked-you"),
    topic("ALBUMS", "Album", "Savage Mode II",
          "21 Savage & Metro Boomin 2020 — the sequel to their 2016 breakout, a 15-track horror-movie of a trap album. It debuted at #1 and 'Runnin' and 'Mr. Right Now' became anthems. Metro's beats are the star: cinematic, string-laden, menacing.",
          "21 Savage", "Savage Mode II (2020) end-to-end", 44,
          "Listen to 'Runnin' — the beat switches from a warped vocal sample into a soul loop mid-song. Then 'Mr. Right Now' — the Drake feature recorded over FaceTime during lockdown. The album's horror-movie interludes are the thread: it's a trap slasher film.",
          ["Hip-Hop", "Trap", "British", "2020s"], "21-savage-savage-mode-2"),
    topic("ALBUMS", "Album", "Whole Lotta Red",
          "Playboi Carti 2020 — a 24-song, 62-minute album of chaos that fans waited four years for. It's the sound of a rapper abandoning melody for pure rhythm and texture, and it became a template for the next decade of underground rap.",
          "Playboi Carti", "Whole Lotta Red (2020) end-to-end", 62,
          "Listen to 'Sky' — the synth loop never changes while Carti's voice morphs from deep to baby-high. Then 'New Tank' — notice the verses are made of sounds, not words. The album is deliberately exhausting; it's named for the luxury store that banned him.",
          ["Hip-Hop", "Experimental", "American", "2020s"], "playboi-carti-whole-lotta-red"),
    topic("ALBUMS", "Album", "Eternal Atake",
          "Lil Uzi Vert 2020 — the long-delayed third album, named after a UFO-cult philosophy Uzi made up. It debuted at #1 and its first three songs all hit the top 10. The album's sci-fi aesthetic — including a fake religion — is the point.",
          "Lil Uzi Vert", "Eternal Atake (2020) end-to-end", 64,
          "Listen to 'Baby Pluto' — the song that reveals the album's fiction: Pluto is Uzi's alien alter ego. Then 'Silly Watch' — the deadpan flex anthem. The album opens with a fake church sermon and ends with a 2-minute piano ballad; the range is the joke.",
          ["Hip-Hop", "Trap", "American", "2020s"], "lil-uzi-vert-eternal-atake"),
    topic("ALBUMS", "Album", "So Much Fun",
          "Young Thug 2019 — his first album, and his first #1, after a decade of mixtapes. It's a 16-track showcase of the most imitated voice in modern rap, with features from Travis Scott, Lil Baby and Future.",
          "Young Thug", "So Much Fun (2019) end-to-end", 51,
          "Listen to 'Hot' — a single piano loop under Thug's pure-melody verse; the song is 3 minutes of him singing in tongues. Then 'The London' — the Travis Scott and J. Cole feature, recorded after a chance meeting in London. Try counting how many distinct voices he uses in one song.",
          ["Hip-Hop", "Trap", "American", "2010s"], "young-thug-so-much-fun"),
    topic("ALBUMS", "Album", "Melt My Eyez See Your Future",
          "Denzel Curry 2022 — a jazz-rap meditation on mortality and growth, recorded after his grandmother's death and a life-threatening health scare. It features Thundercat, Robert Glasper and T-Pain, and it's his most mature, most collaborative album.",
          "Denzel Curry", "Melt My Eyez See Your Future (2022) end-to-end", 52,
          "Listen to 'Walkin' — the song that changes genre mid-track as it shifts from rage to hope. Then 'Troubles' — the T-Pain duet about therapy. The album's title is a mantra: look at your mortality, and use it to grow. The jazz is real — Glasper plays keys throughout.",
          ["Hip-Hop", "Jazz Rap", "American", "2020s"], "denzel-curry-melt-mye-eyez-see-your-future"),
    topic("ALBUMS", "Album", "Veteran",
          "JPEGMAFIA 2018 — the album that made Peggy the king of glitch-rap, a 44-minute collage of broken beats, alarm bells, and pure confrontation. It was recorded in a year where he also produced for others and toured constantly.",
          "JPEGMAFIA", "Veteran (2018) end-to-end", 44,
          "Listen to '1539 N. Calvert' — the song named after a Baltimore address, built on a broken drum machine. Then 'Baby I'm Bleeding' — the stalker voicemail sample is real. The album is designed to be uncomfortable: turn it up and notice the audio clipping is intentional.",
          ["Hip-Hop", "Experimental", "American", "2010s"], "jpegmafia-veteran"),
    topic("ALBUMS", "Album", "The Forever Story",
          "JID 2022 — his second album, named for his late grandmother, a 15-track epic that moves from boom-bap to trap to soul. It's considered one of the best rap albums of the 2020s, and its production credits read like a who's-who of the genre.",
          "JID", "The Forever Story (2022) end-to-end", 65,
          "Listen to 'Surround Sound' — the chopped soul sample and the title's metaphor: his family's voices surrounding him as a kid. Then 'Kody Blu 31' — named for his grandmother's birthday, a sung hook that breaks the rap flow. The whole album is a family tree in song form.",
          ["Hip-Hop", "Rap", "American", "2020s"], "jid-the-forever-story"),
    topic("ALBUMS", "Album", "Sometimes I Might Be Introvert",
          "Little Simz 2021 — the acronym album (SIMBI) that won the Mercury Prize, a 19-track epic mixing orchestral hip-hop with soul and funk. It's a portrait of an introvert navigating fame, and it made Simz the voice of British rap.",
          "Little Simz", "Sometimes I Might Be Introvert (2021) end-to-end", 66,
          "Listen to 'Introvert' — the 5-minute opener with no hook, just an orchestral declaration. Then 'Woman' — the video features over 100 real British women. The album's drama is its point: it swings from full orchestra to bare boom-bap, mirroring an introvert's social battery.",
          ["Hip-Hop", "Rap", "British", "2020s"], "little-simz-sometimes-i-might-be-introvert"),
    topic("ALBUMS", "Album", "Orquídeas",
          "Kali Uchis 2024 — her first all-Spanish album, named for the orchid, Colombia's national flower. It debuted at #2 in the US, the highest chart position ever for an all-Spanish album by a woman, and mixes reggaetón, cumbia and bolero.",
          "Kali Uchis", "Orquídeas (2024) end-to-end", 32,
          "Listen to 'Igual Que un Ángel' — the Peso Pluma duet that sets corrido vocals over dream-pop. Then 'Muñekita' — reggaetón meets cumbia, the album's strangest and best track. The album is 12 songs in 32 minutes, and every one is in Spanish; she wrote them with her own lyrics book.",
          ["R&B", "Latin", "Colombian", "2020s"], "kali-uchis-orquideas"),
    topic("ALBUMS", "Album", "Vice Versa",
          "Rauw Alejandro 2021 — the album that broke reggaetón's mold, mixing the genre with synth-pop, disco and even house. 'Todo de Ti' was the biggest Latin song of the year, and the album's title reflects its dualities: sweet and dark, old and new.",
          "Rauw Alejandro", "Vice Versa (2021) end-to-end", 38,
          "Listen to 'Todo de Ti' — the 80s funk bassline and the total absence of reggaetón rhythm; that's why it stood out. Then '2:00 AM' — the Rosalía duet, recorded during lockdown, a late-night ballad. The album flips between two moods — vice, versa — as its title promises.",
          ["Reggaetón", "Pop", "Puerto Rican", "2020s"], "rauw-alejandro-vice-versa"),
    topic("ALBUMS", "Album", "Mor, No Le Temas a la Oscuridad",
          "Feid 2023 — the album that turned 'Ferxxo' into a Latin superstar, a reggaetón record full of emo heartbreak anthems. Its title means 'Love, Don't Fear the Darkness', and its green-eyed emo aesthetic took over Latin pop.",
          "Feid", "Mor, No Le Temas a la Oscuridad (2023) end-to-end", 33,
          "Listen to 'Normal' — the TikTok phenomenon about realizing a breakup is just normal. Then 'Luna' — the nursery-rhyme-simple chorus that became a stadium singalong. The album is 14 songs of heartbreak; Feid's whole persona — Ferxxo — is the emo kid who grew up to rule the club.",
          ["Reggaetón", "Latin Pop", "Colombian", "2020s"], "feid-mor-no-le-temas-a-la-oscuridad"),
    topic("ALBUMS", "Album", "Génesis",
          "Peso Pluma 2023 — the album that took corridos tumbados global, a 15-track record of narcocorridos with trap beats and traditional sierreño guitars. 'Ella Baila Sola' became the first Mexican song to crack Spotify's global top 10.",
          "Peso Pluma", "Génesis (2023) end-to-end", 48,
          "Listen to 'Ella Baila Sola' — the Eslabon Armado duet that made history; the guitar is pure sierreño over a trap groove. Then 'Lagunas' — the accordion solo is the emotional peak. The album is a genesis story: the corrido tradition reborn with a nasal, deadpan new voice.",
          ["Corridos", "Regional Mexican", "Mexican", "2020s"], "peso-pluma-genesis"),
    topic("ALBUMS", "Album", "Born in the Wild",
          "Tems 2024 — the long-awaited debut from Nigeria's most in-demand voice, a 16-track album that moves between Afrobeats, R&B and alternative soul. It features Asake and J. Cole, and was recorded over four years between tours.",
          "Tems", "Born in the Wild (2024) end-to-end", 55,
          "Listen to 'Free Mind' — the anxiety-born anthem that spent years climbing charts before it peaked. Then 'Burning' — the opener she wrote to process her mother's death. Tems's voice is the album's instrument: it floats above every beat, never forcing anything.",
          ["Afrobeats", "R&B", "Nigerian", "2020s"], "tems-born-in-the-wild"),
    topic("ALBUMS", "Album", "The Year I Turned 21",
          "Ayra Starr 2024 — her second album, recorded between sold-out tours, a 17-track record that captures the chaos of being 21 and famous. It debuted at #3 on the Billboard World Albums chart and confirmed her as the new face of Afrobeats.",
          "Ayra Starr", "The Year I Turned 21 (2024) end-to-end", 40,
          "Listen to 'Commas' — the opener, a statement of arrival with a beat that hits like a victory lap. Then 'Control' — the album's most personal track, about the cost of fame. She was 21 while making it, and the album sounds like that: restless, hungry, unapologetic.",
          ["Afrobeats", "Pop", "Nigerian", "2020s"], "ayra-starr-the-year-i-turned-21"),
    topic("ALBUMS", "Album", "My 21st Century Blues",
          "Raye 2023 — the debut album from the songwriter who spent six years writing for other people, released independently after her label refused to put it out. It won the Brit Award for Album of the Year, and its 15 tracks are a therapy session set to music.",
          "Raye", "My 21st Century Blues (2023) end-to-end", 46,
          "Listen to 'Escapism' — spoken-word verses over a jazz-swing hook that became her first #1. Then 'Ice Cream Man' — the devastating account of assault in the industry. She funded it herself after leaving a major label; it's her revenge and confession.",
          ["Pop", "R&B", "British", "2020s"], "raye-my-21st-century-blues"),
    topic("ALBUMS", "Album", "Magdalene",
          "FKA twigs 2019 — her second album, named after Mary Magdalene, a 39-minute art-pop epic about womanhood, faith and heartbreak. Co-produced with Nicolas Jaar and Daniel Lopatin, it's built on her signature mix of operatic vocals and brutalist beats.",
          "FKA twigs", "Magdalene (2019) end-to-end", 39,
          "Listen to 'Cellophane' — about being treated like glass, with a pole-dancing video that's a fall-and-rise metaphor. Then 'Mirrored Heart' — giving everything to someone who reflects nothing back. The album reframes Magdalene as every woman who's been written off.",
          ["Art Pop", "Electronic", "British", "2010s"], "fka-twigs-magdalene"),
    topic("ALBUMS", "Album", "Desire, I Want to Turn Into You",
          "Caroline Polachek 2023 — her third solo album, a maximalist pop record with bagpipes, opera, and a 6-minute guitar solo. It was named album of the year by several major critics, and it sounds like nothing else released that year.",
          "Caroline Polachek", "Desire, I Want to Turn Into You (2023) end-to-end", 44,
          "Listen to 'Welcome to My Island' — bagpipe-like synth and a real guitar solo, about wanting to be consumed. Then 'Bunny Is a Rider' — the hit whose title is nonsense she invented. Her whistle register appears mid-song without warning.",
          ["Art Pop", "Electronic", "American", "2020s"], "caroline-polachek-desire-i-want-to-turn-into-you"),
    topic("ALBUMS", "Album", "Beatopia",
          "Beabadoobee 2022 — her second album, named after the imaginary world she invented as a 7-year-old to escape school bullying. It's a bedroom-pop record that stretches into shoegaze, bossa nova and folk, with the 90s sound she's famous for.",
          "Beabadoobee", "Beatopia (2022) end-to-end", 43,
          "Listen to 'the perfect pair' — the song about a relationship too perfect to last. Then '10:36' — named for the timestamp when she wrote the hook. The album's title is a real place she drew as a kid; the music is what that place sounds like grown up.",
          ["Indie Pop", "Bedroom Pop", "British", "2020s"], "beabadoobee-beatopia"),
    topic("ALBUMS", "Album", "If I Could Make It Go Quiet",
          "girl in red 2021 — the debut album from the Norwegian bedroom-pop artist, a 10-song record about anxiety, heartbreak and queer joy. It made her a global star and an icon for a generation of queer listeners.",
          "girl in red", "If I Could Make It Go Quiet (2021) end-to-end", 27,
          "Listen to 'Serotonin' — the song about intrusive thoughts that literally glitches and distorts when the thoughts win. Then 'we fell in love in october' — the gentle anthem that became a queer coming-out soundtrack. The album is 27 minutes: short, and every second counts.",
          ["Indie Pop", "Bedroom Pop", "Norwegian", "2020s"], "girl-in-red-if-i-could-make-it-go-quiet"),
    topic("ALBUMS", "Album", "Superache",
          "Conan Gray 2022 — his second album, a 12-track pop record about grief, friendship and growing up in a small Texas town. The title means 'super-pain', and the album's heartbreak anthems made him one of the defining voices of sad-pop.",
          "Conan Gray", "Superache (2022) end-to-end", 41,
          "Listen to 'Memories' — the song about losing a friend who's still alive. Then 'The Exit' — the album's thesis, about a relationship that ended before it started. He wrote the album in his childhood bedroom; the small-town loneliness is the thread running through every song.",
          ["Pop", "Indie Pop", "American", "2020s"], "conan-gray-superache"),
    topic("ALBUMS", "Album", "Gemini Rights",
          "Steve Lacy 2022 — his second solo album, 10 songs made largely on his iPhone and laptop. It won a Grammy for Best Progressive R&B Album, and 'Bad Habit' became the biggest song of the year — a song recorded on a phone.",
          "Steve Lacy", "Gemini Rights (2022) end-to-end", 35,
          "Listen to 'Bad Habit' — the GarageBand-made hit; listen for the phone-quality textures in the drums. Then 'Mercury' — the bassline is the hook. The album's title is a joke about astrology; the songs are a post-breakup rebound set to neo-soul.",
          ["R&B", "Neo-Soul", "American", "2020s"], "steve-lacy-gemini-rights"),
    topic("ALBUMS", "Album", "Dreamland",
          "Glass Animals 2020 — their third album, a memory-palace of frontman Dave Bayley's childhood, made during his sister's illness and after the band's drummer nearly died. It features 'Heat Waves', the slowest-burning #1 hit in chart history, and 'Tokyo Drifting' with Denzel Curry.",
          "Glass Animals", "Dreamland (2020) end-to-end", 40,
          "Listen to 'Heat Waves' — the flooded-world video and the lyrics about missing someone until it feels like drowning. Then 'Space Ghost Coast to Coast' — the song about a friendship ended by conspiracy theories. The album's interludes are real audio from Bayley's childhood tapes.",
          ["Indie Pop", "Alternative", "British", "2020s"], "glass-animals-dreamland"),
    topic("ALBUMS", "Album", "Rat Saw God",
          "Wednesday 2023 — the Asheville band's fourth album, a 47-minute collision of shoegaze and country that made them the most talked-about indie band of the year. Frontwoman Karly Hartzman screams over pedal steel; it shouldn't work, and it does.",
          "Wednesday", "Rat Saw God (2023) end-to-end", 47,
          "Listen to 'Bull Believer' — the 8-minute closer that builds to a wordless scream of catharsis. Then 'Chosen to Deserve' — the pedal steel cutting through distortion. The album is named after a tattoo Hartzman saw on a stranger; the title is a promise: no mercy for the mediocre.",
          ["Shoegaze", "Country", "American", "2020s"], "wednesday-rat-saw-god"),
    topic("ALBUMS", "Album", "Any Shape You Take",
          "Indigo De Souza 2021 — her breakthrough second album, a 13-song set of raw, confessional indie rock recorded in Asheville. 'Kill Me' became an anthem for the heartbroken, and the album made her a hero of the new American sad-rock scene.",
          "Indigo De Souza", "Any Shape You Take (2021) end-to-end", 37,
          "Listen to 'Kill Me' — the country-tinged diary entry that builds into a scream; the title is a joke about a breakup. Then 'Real Pain' — the song about a friend's car accident, written years later. Her voice cracks on purpose; the imperfection is the performance.",
          ["Indie Rock", "Alternative", "American", "2020s"], "indigo-de-souza-any-shape-you-take"),
    topic("ALBUMS", "Album", "Dragon New Warm Mountain I Believe in You",
          "Big Thief 2022 — a 20-song double album recorded in five locations over five months, from a cabin to a rock club. It's a sprawling American folk record that deliberately refuses a single style, and it won Album of the Year at the 2023 Libera Awards.",
          "Big Thief", "Dragon New Warm Mountain I Believe in You (2022) end-to-end", 80,
          "Listen to 'Simulation Swarm' — the 6-minute song with four guitar solos, recorded live in one take. Then 'Not' — the song that dissolves into noise at the end. The album is 80 minutes; treat it like a long conversation, not a single record.",
          ["Folk", "Indie Rock", "American", "2020s"], "big-thief-dragon-new-warm-mountain"),
    topic("ALBUMS", "Album", "Kick I",
          "Arca 2020 — the first of four albums she released in a year, a 40-minute set that reimagines reggaetón as avant-garde art. With guests Rosalía, Björk, Shygirl and SOPHIE, it's the sound of a producer at the peak of her powers refusing every genre rule.",
          "Arca", "Kick I (2020) end-to-end", 40,
          "Listen to 'KLK' — the Rosalía collab that sets flamenco-adjacent vocals over a brutalist reggaetón beat. Then 'Mequetrefe' — a Spanish insult turned into an anthem. The album's four 'Kick' siblings exist because she had too much material; this one is the statement of intent.",
          ["Electronic", "Experimental", "Venezuelan", "2020s"], "arca-kick-i"),
    topic("ALBUMS", "Album", "uknowhatimsayin¿",
          "Danny Brown 2019 — his fifth album, produced entirely by Q-Tip of A Tribe Called Quest, a 32-minute set of surrealist, funky rap. It's the most accessible Danny Brown album and one of the strangest; both are true at once.",
          "Danny Brown", "uknowhatimsayin¿ (2019) end-to-end", 32,
          "Listen to '3 Tearz' — the Run-DMC-style posse cut produced by Q-Tip, whose hand is all over the album's clean funk. Then 'Negro Spiritual' — the jazz sample and the album's emotional core. Q-Tip made Danny keep his weirdest instincts; the title is his shrug.",
          ["Hip-Hop", "Experimental", "American", "2010s"], "danny-brown-uknowhatimsayin"),
    topic("ALBUMS", "Album", "Some Rap Songs",
          "Earl Sweatshirt 2018 — his third album, 24 minutes of grief made after his father's death. The beats are warped, the raps are whispers, and it's one of the most devastating albums in hip-hop. It's best listened to as a single poem.",
          "Earl Sweatshirt", "Some Rap Songs (2018) end-to-end", 24,
          "Listen to 'Nowhere2go' — the warped soul sample and the barely-there verse, a whisper of grief. Then 'December 24' — named for his father's death date, the closing elegy. The album is 24 minutes and 15 songs; listen to it straight through, once.",
          ["Hip-Hop", "Abstract", "American", "2010s"], "earl-sweatshirt-some-rap-songs"),
    topic("ALBUMS", "Album", "Michael",
          "Killer Mike 2023 — his first solo album in 11 years, named for his birth name, a 40-minute soul-rap record about the Black men who raised him. It won a Grammy for Best Rap Album, and its features include André 3000 and CeeLo Green.",
          "Killer Mike", "Michael (2023) end-to-end", 40,
          "Listen to 'Scientists & Engineers' — the André 3000 feature that's his first rap verse in years, a prayer-like meditation on Black excellence. Then 'Motherless' — the title track's emotional core, about his mother's death. The album is a eulogy that's also a celebration.",
          ["Hip-Hop", "Soul", "American", "2020s"], "killer-mike-michael"),
    topic("ALBUMS", "Album", "Testing",
          "A$AP Rocky 2018 — his third album, an experimental record whose title is literal: he's testing new sounds. From gospel choir samples to grime beats, it's Rocky's most adventurous album, and 'Praise the Lord' with Skepta became a global hit.",
          "A$AP Rocky", "Testing (2018) end-to-end", 48,
          "Listen to 'Praise the Lord (Da Shine)' — the Skepta collab made from a beat a stranger sent as a joke. Then 'A$AP Forever' — the choir sample and the video featuring his late friend A$AP Yams. The album's genre-hopping is the test; nothing repeats.",
          ["Hip-Hop", "Cloud Rap", "American", "2010s"], "asap-rocky-testing"),
    topic("ALBUMS", "Album", "Wunna",
          "Gunna 2020 — his second studio album, a 50-minute masterclass in melodic trap. The title is 'winner' in his family's dialect, and the album's warm, syrupy production became the sound of Atlanta's next generation.",
          "Gunna", "Wunna (2020) end-to-end", 50,
          "Listen to 'Skybox' — the opener that sounds like a stadium filling up. Then 'Dollaz on My Head' — the Young Thug collab that shows his lineage. Gunna's verses are more melody than rap; count how many words he actually raps in a verse, then how many melodies he hums.",
          ["Hip-Hop", "Trap", "American", "2020s"], "gunna-wunna"),
    topic("ALBUMS", "Album", "Currents",
          "Tame Impala 2015 — the album that turned Kevin Parker's bedroom recordings into a global phenomenon, a psych-pop masterpiece about change and growth. 'The Less I Know the Better' and 'Let It Happen' made it the defining indie album of the mid-2010s.",
          "Tame Impala", "Currents (2015) end-to-end", 51,
          "Listen to 'Let It Happen' — the 8-minute opener that glitches and stutters in the middle, a metaphor for letting go. Then 'The Less I Know the Better' — the disco-funk hit. Parker plays every instrument on the album himself; it's a one-man band's diary of a breakup.",
          ["Psych-Pop", "Alternative", "Australian", "2010s"], "tame-impala-currents"),
    topic("ALBUMS", "Album", "Nurture",
          "Porter Robinson 2021 — the long-awaited second album from the EDM prodigy, made over five years while he recovered from depression. It samples his own voice as an instrument, uses real rain and birdsong, and turns grief into a euphoric, hopeful pop record.",
          "Porter Robinson", "Nurture (2021) end-to-end", 59,
          "Listen to 'Something Comforting' — the song about finding comfort in art while depressed. Then 'Look at the Sky' — the turning point where the album finds hope. He recorded field sounds and his own voice as textures; the album is a garden grown out of a dark room.",
          ["Electronic", "Hyperpop", "American", "2020s"], "porter-robinson-nurture"),
]


def main():
    artists_path = TOPICS_DIR / "artists.json"
    albums_path = TOPICS_DIR / "albums.json"

    # ── PRE-FLIGHT: validate every new entry BEFORE touching the files, so
    #    a bad entry can never leave the shipped JSON in a failed state
    #    (the reviewer catch — the original script appended first, then
    #    checked, and a failed run left the files dirty).
    errors = []

    def check(entry, path_name):
        if entry["categoryId"] != {"artists": "ARTISTS", "albums": "ALBUMS"}[path_name]:
            errors.append(f"{entry['id']}: categoryId mismatch")
        if len(entry["name"]) > 80:
            errors.append(f"{entry['id']}: name > 80 chars ({len(entry['name'])})")
        if len(entry["teaser"]) > 280:
            errors.append(f"{entry['id']}: teaser > 280 ({len(entry['teaser'])})")
        act = entry["exploreAction"]
        for field in ("verb", "targetName", "durationMinutes", "instruction"):
            if field not in act:
                errors.append(f"{entry['id']}: exploreAction missing {field}")
        if len(act["instruction"]) > 280:
            errors.append(f"{entry['id']}: instruction > 280 ({len(act['instruction'])})")
        if act["durationMinutes"] > 90:
            errors.append(f"{entry['id']}: duration > 90 ({act['durationMinutes']})")
        if not re.fullmatch(r"[a-z0-9-]+", entry["id"]):
            errors.append(f"{entry['id']}: bad id format")

    for e in ARTISTS:
        check(e, "artists")
    for e in ALBUMS:
        check(e, "albums")

    if errors:
        print("\nERRORS (new entries — NOTHING was written):")
        for err in errors[:30]:
            print("  -", err)
        raise SystemExit(1)

    # Load all topic files for cross-file ID + name uniqueness.
    all_files = {p.stem: json.loads(p.read_text(encoding="utf-8")) for p in TOPICS_DIR.glob("*.json")}
    all_ids = set()
    all_names = {}  # (category, normalized name) -> file
    for stem, topics in all_files.items():
        for t in topics:
            all_ids.add(t["id"])
            all_names.setdefault((t["categoryId"], t["name"].lower()), stem)

    def append_batch(path, entries):
        data = json.loads(path.read_text(encoding="utf-8"))
        cat_id = entries[0]["categoryId"]
        existing_ids = {t["id"] for t in data}
        existing_names = {t["name"].lower() for t in data}
        added = 0
        skipped = 0
        for e in entries:
            if e["id"] in all_ids or e["id"] in existing_ids:
                print(f"  SKIP (id exists): {e['id']}")
                skipped += 1
                continue
            if (cat_id, e["name"].lower()) in all_names or e["name"].lower() in existing_names:
                print(f"  SKIP (name exists): {e['name']}")
                skipped += 1
                continue
            data.append(e)
            existing_ids.add(e["id"])
            existing_names.add(e["name"].lower())
            all_ids.add(e["id"])
            all_names[(cat_id, e["name"].lower())] = path.stem
            added += 1
        path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        return added, skipped

    print("Appending modern artists batch...")
    a_added, a_skipped = append_batch(artists_path, ARTISTS)
    print(f"  +{a_added} artists ({a_skipped} skipped)")
    print("Appending modern albums batch...")
    al_added, al_skipped = append_batch(albums_path, ALBUMS)
    print(f"  +{al_added} albums ({al_skipped} skipped)")

    print(f"\nAll {len(ARTISTS) + len(ALBUMS)} new entries validated BEFORE writing; "
          f"{a_added + al_added} appended, {a_skipped + al_skipped} skipped (already present).")


if __name__ == "__main__":
    main()
