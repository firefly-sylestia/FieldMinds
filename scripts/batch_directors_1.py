#!/usr/bin/env python3
"""Batch: replace the first 40 fake directors.json entries with real facts.

Template-generated entries with boilerplate teasers and scrambled tags
(Indie|20th Century on David Lean, etc.). Replaces teaser + instruction +
targetName + tags. subtype/verb preserved. Cap 450 (SCHEMA.md).
"""

from pathlib import Path
import json
import re
import sys


def _trim(text: str, limit: int = 450) -> str:
    if len(text) <= limit:
        return text
    sentences = re.split(r"(?<=[.!?])\s+", text.strip())
    out = ""
    for s in sentences:
        candidate = s if not out else out + " " + s
        if len(candidate) > limit:
            break
        out = candidate
    return out


PATH = Path(__file__).resolve().parent.parent / "app/src/main/assets/topics/directors.json"


def _entry(teaser: str, instruction: str, target_name: str, tags: list[str]) -> dict:
    return {
        "teaser": teaser,
        "instruction": instruction,
        "targetName": target_name,
        "tags": tags,
    }


FIXES: dict[str, dict] = {
    "dire-david-lean-147": _entry(
        "Lean made the biggest pictures of the mid-century — Bridge on the River Kwai, Lawrence of Arabia, Doctor Zhivago — sweeping desert epics shot in 70mm with casts of thousands. He also made Brief Encounter, one of the most intimate love stories ever filmed, which is the part of his career people forget: the man of epic scale began with small, quiet romance.",
        "Watch the opening of Lawrence of Arabia and notice how Lean introduces his hero: a motorcyclist crashes and dies, and only then do we learn the film is about him. Then watch Brief Encounter, his 1945 romance set almost entirely in a railway station tea room — the same director, the opposite scale, and the mastery is identical.",
        "David Lean — 'Lawrence of Arabia' (1962) opening + 'Brief Encounter' (1945)",
        ["Epic", "British", "Classic"],
    ),
    "dire-wim-wenders-148": _entry(
        "Wenders, the road-movie poet of the German New Wave, made Paris, Texas (1984) and Wings of Desire (1987), films about men who can't settle and cities that observe them. He was also a photographer of wide-open American spaces, and his films are as much about light and architecture as about plot.",
        "Watch the opening of Paris, Texas — a man in a red cap walking out of the desert, a blue sky, no dialogue for minutes — and notice how Wenders lets landscape tell the story before a word is spoken. Then watch Wings of Desire, where angels listen to the thoughts of Berliners: the movie is a documentary of a city's inner life disguised as a fantasy.",
        "Wim Wenders — 'Paris, Texas' (1984) opening + 'Wings of Desire' (1987)",
        ["German New Wave", "Road Movie", "1980s"],
    ),
    "dire-yorgos-lanthimos-149": _entry(
        "Lanthimos, the founder of the 'Greek Weird Wave,' makes deadpan films where ordinary life is governed by absurd rules — people forced to choose a mate at a hotel in The Lobster, a family that keeps its children imprisoned in Dogtooth. His 2023 Poor Things won four Oscars and made him the most bankable weirdo in cinema.",
        "Watch the opening of The Lobster and notice the tone immediately: the hotel's rules about singlehood are announced with bureaucratic calm, and the deadpan delivery is the joke and the horror at once. Then watch the famous 'dance scene' in his earlier film Alps or the Emma Stone 'dance' in Poor Things: Lanthimos's actors perform awkward, violent movement the way other directors stage conversations.",
        "Yorgos Lanthimos — 'The Lobster' (2015) opening and the Poor Things dance",
        ["Weird Wave", "Greek", "Satire"],
    ),
    "dire-ruben-östlund-150": _entry(
        "Östlund builds his films around scenes of unbearable social awkwardness stretched to feature length — the avalanche that becomes a morality test in Force Majeure, the dinner-party performance that crumbles in The Square, the cruise-ship class war of Triangle of Sadness. He is the rare filmmaker to win the Palme d'Or twice.",
        "Watch the avalanche scene in Force Majeure and stop when the family sits down to dinner afterward: the entire movie is the question of who moved during the avalanche, and Östlund films the accusation scene like a trial. Then watch the monkey scene or the vomiting scene in Triangle of Sadness — the grotesque comedy is his method of making social class visible.",
        "Ruben Östlund — 'Force Majeure' avalanche scene + 'Triangle of Sadness' (2022)",
        ["Satire", "Swedish", "Social"],
    ),
    "dire-andrea-arnold-151": _entry(
        "Arnold — a former dancer and TV presenter — makes gritty handheld films about working-class British youth: Fish Tank (2009) and American Honey (2016). Her camera stays close, in the moment, and her actresses (Katie Jarvis was found on an estate; Sasha Lane was spotted on a beach) were largely first-time performers.",
        "Watch the opening of Fish Tank and notice the camerawork: handheld, tight, following Mia through the estate with no establishing shots — you learn the world the way she lives in it. Then watch the dance audition scene: Mia's furious, improvised dance is the film's thesis about who gets to be seen.",
        "Andrea Arnold — 'Fish Tank' (2009) opening and the audition scene",
        ["British", "Social Realism", "2010s"],
    ),
    "dire-alice-rohrwacher-152": _entry(
        "Rohrwacher makes magical-realist films set in her native Italian countryside — Happy as Lazzaro (2018), which won the Cannes screenplay prize, follows a saintly farmhand across a time jump that turns a social drama into a fable. Her films feel like folk tales recorded by a documentarian.",
        "Watch the opening of Happy as Lazzaro and notice how Rohrwacher establishes the village: the tobacco fields, the sharecroppers, the patroness — shot with the tenderness of a family album. Then watch the film's mid-film shift, where time itself seems to jump: the moment the film becomes a fable is also the moment its politics become explicit.",
        "Alice Rohrwacher — 'Happy as Lazzaro' (2018) opening and the time shift",
        ["Italian", "Magic Realism", "Fable"],
    ),
    "dire-lulu-wang-153": _entry(
        "Wang made The Farewell (2019) from a true family story — her grandmother was diagnosed with terminal cancer, and the family hid it from her, staging a fake wedding to gather everyone. The film, which she calls 'based on a true lie,' became a breakthrough for Asian-American cinema and made Awkwafina a dramatic actress.",
        "Watch the opening of The Farewell and notice the frame: a young Chinese-American woman in New York learns her grandmother is dying while her family in China already knows. The film's central argument is about which lie is more loving, and Wang stages the wedding scenes so the deception and the joy are the same event.",
        "Lulu Wang — 'The Farewell' (2019) opening and the wedding scenes",
        ["Drama", "Asian-American", "2010s"],
    ),
    "dire-edward-yang-154": _entry(
        "Yang was the great chronicler of Taipei and one of the towering figures of the Taiwanese New Wave — his Yi Yi (2000) is a three-hour portrait of one family told from the perspectives of three generations, and it is routinely named one of the greatest films ever made. He died at 59, having made only seven features.",
        "Watch the opening of Yi Yi and notice the wedding that starts it — a family gathering where everyone is somewhere else emotionally. Then find the film's signature device: the 8-year-old boy, Yang Yang, photographs the backs of people's heads because 'you can't see what I see.' That sentence is the film's whole philosophy in eight words.",
        "Edward Yang — 'Yi Yi' (2000) opening and the back-of-the-heads motif",
        ["Taiwanese", "Family", "Masterpiece"],
    ),
    "dire-apichatpong-weerasethakul-155": _entry(
        "Apichatpong — 'Joe' to his friends — makes slow, trance-like Thai films about memory, spirits, and the border between the living and the dead. Uncle Boonmee Who Can Recall His Past Lives won the Palme d'Or in 2010, and his films treat the supernatural as ordinary weather.",
        "Watch the opening of Uncle Boonmee and settle into the rhythm: long takes, forest sounds, a man dying among family — and then the dead wife appears at the dinner table as if she'd never left. Apichatpong's radical move is refusing to dramatize the supernatural: the ghost is treated with the same calm as the rain.",
        "Apichatpong Weerasethakul — 'Uncle Boonmee' (2010) dinner-table scene",
        ["Thai", "Slow Cinema", "Supernatural"],
    ),
    "dire-naoko-ogigami-156": _entry(
        "Ogigami makes quiet, warm films about food, loneliness, and small communities — Kamome Diner (2006) follows three Japanese women who open a café in Helsinki, and her films are so gentle that they became a genre of their own ('iyashikei' cinema, or healing film). She is one of the most beloved cult directors in Japan.",
        "Watch the opening of Kamome Diner and notice the lack of conflict: three women, a Finnish town, a café that serves Japanese food to skeptical locals — the film's drama is measured in how many customers come back. Ogigami's method is to trust that small exchanges are enough, and the film's cumulative warmth is the point.",
        "Naoko Ogigami — 'Kamome Diner' (2006) opening",
        ["Japanese", "Iyashikei", "Food"],
    ),
    "dire-na-hong-jin-157": _entry(
        "Na Hong-jin makes Korean thrillers that start as genre films and spiral into something else — The Chaser (2008) is a serial-killer chase; The Wailing (2016) begins as a rural murder mystery and ends as a possession horror about faith. His violence is physical, his ambiguity total.",
        "Watch the opening of The Wailing and notice the misdirection: a detective, a village, a mysterious stranger — the film keeps promising a straightforward mystery and keeps refusing to deliver one. Then watch the ending and decide who, if anyone, was evil: Na has said the film is about 'the impossibility of certainty,' and it is designed to be rewatched to change your answer.",
        "Na Hong-jin — 'The Wailing' (2016) opening and its ending",
        ["Korean", "Horror", "Thriller"],
    ),
    "dire-johnnie-to-158": _entry(
        "Johnnie To, the most prolific great director of Hong Kong action cinema, made the Milkyway Image studio the home of the modern gangster film — The Mission (1999) and Election (2005) choreograph gunfights and power struggles with geometric precision. He has made more than 60 films, most of them genre films treated as art.",
        "Watch the famous corridor gunfight in The Mission — five bodyguards, one long take, guns drawn in a choreographed standoff — and notice the stillness: To's violence is about position and timing, not chaos. Then watch Election's parking-garage election scene, where a triad leadership vote is staged with the gravity of a state funeral.",
        "Johnnie To — 'The Mission' (1999) corridor gunfight",
        ["Hong Kong", "Crime", "Action"],
    ),
    "dire-dario-argento-159": _entry(
        "Argento is the godfather of the Italian giallo — lurid murder mysteries in which color, music, and camera movement matter more than motive. His Suspiria (1977), a ballet school run by witches, is a sensory assault of saturated red, and his 1975 Deep Red is the genre's masterpiece. His films are nightmares that look like fashion magazines.",
        "Watch the opening of Suspiria and notice the assault: the color red, the Goblin's prog-rock score, the murder staged in a room of barbed wire and stained glass. Then watch Deep Red and find the famous scene where the camera tilts to reveal a killer who was visible the whole time: Argento's horror is about the eye failing at the exact moment it's needed.",
        "Dario Argento — 'Suspiria' (1977) opening + 'Deep Red' (1975)",
        ["Giallo", "Italian", "Horror"],
    ),
    "dire-nanni-moretti-160": _entry(
        "Moretti, Italy's most personal auteur, has spent four decades making films that are barely disguised self-portraits — Caro Diario (1993) is literally his diary, and The Son's Room (2001) won the Palme d'Or. He is also famous for his political films and for being the rare director whose on-screen persona is as well-known as his films.",
        "Watch the opening of Caro Diario and notice the format: chapters titled like a diary, a scooter ride through Rome, the director narrating his own life with deadpan self-deprecation. Then watch the final chapter, set on the island of Stromboli, where Moretti's comic voice suddenly goes still: the film's turn from comedy to grief is the reason it's a classic.",
        "Nanni Moretti — 'Caro Diario' (1993) scooter prologue",
        ["Italian", "Autobiographical", "1990s"],
    ),
    "dire-fw-murnau-161": _entry(
        "Murnau made Nosferatu (1922), the unauthorized Dracula that defined the horror film's visual language, and Sunrise (1927), which critics have called the greatest film ever made. He was the master of German Expressionism who brought the style to Hollywood, and he died in a car accident at 42, a week before Sunrise's Oscar win.",
        "Watch the opening of Nosferatu and notice the shadows: Murnau shot the vampire as a walking shadow long before the character appears — the film's horror is built in silhouette. Then watch Sunrise's trolley sequence, where a country couple rides a streetcar into the city and Murnau films a nightmare cityscape that dissolves into joy: expressionism used for both fear and love.",
        "F.W. Murnau — 'Nosferatu' (1922) shadow scenes + 'Sunrise' (1927)",
        ["Expressionism", "Silent", "Classic"],
    ),
    "dire-max-ophüls-162": _entry(
        "Ophüls was the great stylist of the moving camera — his films glide through ballrooms, casinos, and staircases in tracking shots so elegant they became the standard for romance and melodrama. His Letter from an Unknown Woman (1948) and La Ronde (1950) are masterpieces of longing, and every modern director of elegant cinema owes him a debt.",
        "Watch the ballroom sequence in Madame de... (1953) and notice the camera: it never stops moving, circling the dancers and the couple at the center of the film's tragedy — the movement is the emotion. Then watch La Ronde, where a narrator introduces a chain of lovers and each story passes to the next like a dance: the structure is the subject.",
        "Max Ophüls — 'Madame de...' (1953) ballroom sequence",
        ["Melodrama", "French", "Classic"],
    ),
    "dire-rainer-werner-fassbinder-123": _entry(
        "Fassbinder, the 'German Godard,' made 44 films in 15 years — working so fast he once made three features in a year — before dying at 37. His Marriage of Maria Braun (1979) and Berlin Alexanderplatz (1980) turned German history into domestic melodrama, and his ruthless productivity was itself the point: he treated filmmaking like a race against death.",
        "Watch the opening of The Marriage of Maria Braun and notice the speed: the film starts during an air raid, Maria marries in seconds, and her husband is gone within minutes — Fassbinder compresses a life into scenes the way other directors compress a scene into shots. Then watch the ending, the most famous final image in New German Cinema, and read what it does to the whole film.",
        "Rainer Werner Fassbinder — 'The Marriage of Maria Braun' (1979) opening and ending",
        ["New German Cinema", "Melodrama", "1970s"],
    ),
    "dire-george-lucas-124": _entry(
        "Lucas invented the modern blockbuster: Star Wars (1977) not only revived a dying studio and created the sequel model, it founded the effects company ILM, whose technology shaped every big movie since. His first feature, THX 1138 (1971), is a dystopia about surveillance — a long way from the galaxy far, far away, and recognizably by the same mind.",
        "Watch the opening of THX 1138 — white corridors, drones, a society that drugs its citizens — and notice the visual signatures Lucas would carry to Star Wars: the wide shots, the industrial scale, the machines that dwarf people. Then watch the first ten minutes of Star Wars and count how fast it establishes the rules of its universe: Lucas's genius was world-building as storytelling.",
        "George Lucas — 'THX 1138' (1971) opening + the 'Star Wars' (1977) opening",
        ["Sci-Fi", "Blockbuster", "Hollywood"],
    ),
    "dire-woody-allen-125": _entry(
        "Allen made a film a year for five decades — Annie Hall (1977), Manhattan (1979), Hannah and Her Sisters (1986) — turning his neurotic, literate New York voice into a genre. He won the directing Oscar without attending the ceremony, preferring to play clarinet in a New York jazz club.",
        "Watch the opening of Annie Hall, where Allen breaks the fourth wall to tell the audience his jokes are old and his relationship is doomed — and then spends the film showing why. The innovation is the form: Allen interrupts the story with asides, subtitles, and cartoons, treating a romance as an essay about romance. Then watch Manhattan's black-and-white opening, narrated by a man who can't get his life in order — the same voice, the opposite tone.",
        "Woody Allen — 'Annie Hall' (1977) opening + 'Manhattan' (1979) opening",
        ["Comedy", "New York", "Hollywood"],
    ),
    "dire-ridley-scott-126": _entry(
        "Scott built worlds — the corporate corridors of Alien (1979), the rain-soaked future of Blade Runner (1982), the desert kingdoms of Gladiator (2000) — and has remained one of the most visually influential directors in cinema for five decades. He is also one of the most prolific, often shooting several films in a decade, most of them about people trapped inside systems they built.",
        "Watch the opening of Blade Runner and notice the world: the city, the fire, the replicant's eye — Scott establishes a fully realized future in the first three minutes without a line of plot. Then watch the opening of Alien, where the camera moves through a spaceship that feels lived-in and industrial: Scott's spaces are the real stars, and his protagonists are usually smaller than their environments.",
        "Ridley Scott — 'Blade Runner' (1982) opening + 'Alien' (1979) opening",
        ["Sci-Fi", "World-Building", "Hollywood"],
    ),
    "dire-john-carpenter-127": _entry(
        "Carpenter made Halloween (1978) for $300,000 and turned it into the most profitable independent film of its era, inventing the slasher formula; his The Thing (1982) is the paranoia horror masterpiece. He also composed his own scores — the pulsing synth theme to Halloween is as iconic as the film — and worked entirely within genre, which critics only recently began to credit.",
        "Watch the opening of Halloween — the first-person point-of-view shot of a killer in a suburban house — and notice the mechanics: the long take, the kid's mask, the family's scream. Carpenter's genius is economy: the sequence is one shot doing the work of a hundred. Then watch The Thing's opening, where a dog runs across snow toward an Antarctic camp: the movie's horror is all anticipation, and the 'Thing' is never shown clearly until it's too late.",
        "John Carpenter — 'Halloween' (1978) opening + 'The Thing' (1982) opening",
        ["Horror", "Cult", "Hollywood"],
    ),
    "dire-tim-burton-128": _entry(
        "Burton built a career on the outcast with the scissors, the goth suburb, and the stop-motion afterlife — Beetlejuice (1988), Edward Scissorhands (1990), The Nightmare Before Christmas (1993). His style, a gothic cartoon, made him one of the most distinctive voices in Hollywood, and his films are almost all about gentle freaks surviving small-minded towns.",
        "Watch the opening of Edward Scissorhands and notice the mismatch: the pastel suburb with identical houses, and the dark castle on the hill — Burton's whole worldview in one establishing shot. Then watch the opening of The Nightmare Before Christmas, where Jack Skellington sings about being tired of Halloween: the movie is a musical about creative burnout disguised as a holiday fantasy.",
        "Tim Burton — 'Edward Scissorhands' (1990) opening + 'The Nightmare Before Christmas' (1993)",
        ["Gothic", "Fantasy", "Hollywood"],
    ),
    "dire-wes-anderson-129": _entry(
        "Anderson makes symmetrical, pastel, deadpan comedies — Rushmore, The Royal Tenenbaums, The Grand Budapest Hotel — in a style so imitable it became its own category, but his subject has always been grief disguised as whimsy. His films are dollhouses: perfectly arranged, brightly lit, and about the mess that families make of perfect arrangements.",
        "Watch the opening of The Royal Tenenbaums and notice the narrator's voice and the book-format framing: Anderson treats his characters like exhibits in a museum, introducing them with freeze-frames and captions. Then watch the slow-motion walk into the ocean in the middle of the film — the movie's joke and its heart are the same gesture, and Anderson lets the sadness have the last step.",
        "Wes Anderson — 'The Royal Tenenbaums' (2001) opening",
        ["Comedy", "Symmetry", "Hollywood"],
    ),
    "dire-kathryn-bigelow-130": _entry(
        "Bigelow became the first woman to win the Best Director Oscar — for The Hurt Locker (2008) — and has spent her career making intense, physical films about men under pressure: the surfer-bank robbers of Point Break, the futuristic LAPD of Strange Days, the SEAL team of Zero Dark Thirty. She has said she makes 'character-driven genre films' and that genre is not a dirty word.",
        "Watch the opening of The Hurt Locker and notice the bomb-disposal sequence: Bigelow films it in handheld close-up, with the camera as anxious as the audience — the war film as a thriller about a single man's compulsion. Then watch the supermarket scene at the end of the film, where the hero, home from war, stands in the cereal aisle unable to choose: that scene is the whole movie.",
        "Kathryn Bigelow — 'The Hurt Locker' (2008) opening bomb sequence",
        ["Action", "Thriller", "Hollywood"],
    ),
    "dire-hayao-miyazaki-131": _entry(
        "Miyazaki, the co-founder of Studio Ghibli, made Spirited Away (2001) — the first anime to win the Oscar for Best Animated Feature — along with My Neighbor Totoro, Princess Mononoke, and Howl's Moving Castle. He has announced his retirement at least four times, and his films are distinguished by flight, food, and a deep distrust of the human damage to nature.",
        "Watch the opening of Spirited Away and notice the world-building: Chihiro's family drives to a new house, takes a wrong turn, and enters a spirit world through a tunnel — Miyazaki makes the transition feel inevitable. Then watch the bathhouse sequence, where Chihiro works to save her parents: the movie is about a 10-year-old learning that the world runs on work, and Miyazaki's food scenes — the parents' gluttony, the dumplings — are as important as the magic.",
        "Hayao Miyazaki — 'Spirited Away' (2001) opening",
        ["Anime", "Ghibli", "Fantasy"],
    ),
    "dire-zhang-yimou-132": _entry(
        "Zhang Yimou, the most famous living Chinese director, made the Fifth Generation's early classics — Raise the Red Lantern, To Live — and then reinvented himself as the master of martial-arts spectacle with Hero (2002) and House of Flying Daggers. He also directed the 2008 Beijing Olympics opening ceremony, the most-watched television event in history.",
        "Watch the opening of Hero and notice the color coding: each retelling of the story has its own palette — red, blue, green, white — so the film is watched like a painting that changes color as truth shifts. Then watch the rain-and-swords fight in the teahouse, where Zhang choreographs violence as calligraphy: the film's argument is that heroism is a story, and the king's decision at the end is the real drama.",
        "Zhang Yimou — 'Hero' (2002) color-coded retellings",
        ["Chinese", "Wuxia", "Epic"],
    ),
    "dire-chan-wook-park-133": _entry(
        "Park Chan-wook made Oldboy (2003) — the film with the most famous fight scene of the century, shot in one continuous take down a corridor — and his 'revenge trilogy' announced the Korean New Wave to the world. His later The Handmaiden (2016) turned Sarah Waters's Victorian novel into a Korean erotic thriller, and his films are elegant, violent, and full of dark humor.",
        "Watch the corridor fight in Oldboy and notice the technique: one take, a hammer, and the knowledge that the fight is staged for a character being watched — Park films violence as performance. Then watch the reveal at the end of the film and read Park's account of it: he has said the movie is about a question his own country kept asking — what revenge does to the person who takes it.",
        "Park Chan-wook — 'Oldboy' (2003) corridor fight",
        ["Korean", "Revenge", "Thriller"],
    ),
    "dire-hirokazu-kore-eda-134": _entry(
        "Kore-eda is the great contemporary chronicler of the Japanese family — Shoplifters (2018) won the Palme d'Or, and his films (Still Walking, Like Father Like Son, Broker) are built around the families people choose when blood fails. His method is observational: long takes, quiet scenes, and a camera that never tells you what to feel.",
        "Watch the opening of Shoplifters and notice the setup: a family on a shoplifting run, the little girl, the frozen-food packs — and then the moment the family takes in a cold, hungry girl from outside. Kore-eda's films are about the gap between what a family is supposed to be and what it is, and the film's final scenes force you to ask which of the two is real.",
        "Hirokazu Kore-eda — 'Shoplifters' (2018) opening",
        ["Japanese", "Family", "2010s"],
    ),
    "dire-asghar-farhadi-135": _entry(
        "Farhadi has won two Oscars for Best Foreign Language Film — A Separation (2011) and The Salesman (2016) — making him the most decorated Iranian director in history. His films are moral labyrinths: ordinary people faced with small lies that grow into catastrophes, told with the structure of a detective story where the crime is always a human failure.",
        "Watch the opening of A Separation and notice how fast the stakes are set: a couple in divorce court, a disagreement about emigrating, and within ten minutes a lie has begun that will drive the whole film. Then watch the famous staircase scene, where the film's central ambiguity is staged: Farhadi films it so that you cannot know what happened, and he refuses to resolve it — the film's power is that your judgment of every character changes by the end.",
        "Asghar Farhadi — 'A Separation' (2011) opening",
        ["Iranian", "Moral Drama", "2010s"],
    ),
    "dire-mira-nair-136": _entry(
        "Nair, born in India and based between Delhi and New York, made Salaam Bombay! (1988) — filmed with real street children — and Monsoon Wedding (2001), which won the Golden Lion. Her films are about the collision of her two homes: the arranged-marriage families of Delhi and the immigrant ambitions of the diaspora.",
        "Watch the opening of Monsoon Wedding and notice the register: a chaotic Punjabi wedding, hundreds of relatives, a bride who may be in love with someone else — and a subplot about abuse that the film refuses to treat as a scandal. Nair's genius is tonal: the film is a comedy that contains real darkness without ever turning cynical. The wedding tent is her recurring image for India itself.",
        "Mira Nair — 'Monsoon Wedding' (2001) opening",
        ["Indian", "Diaspora", "2000s"],
    ),
    "dire-pedro-almodóvar-137": _entry(
        "Almodóvar is Spain's most internationally beloved director — All About My Mother (1999) won the Oscar, Talk to Her (2002) won it again, and his films are a lifelong hymn to women, melodrama, and lurid color. He emerged from Madrid's post-Franco counterculture and turned the telenovela's excess into art.",
        "Watch the opening of Women on the Verge of a Nervous Breakdown and notice the style: saturated reds, answering machines, a woman who has been dumped — Almodóvar makes hysteria look like glamour. Then watch Talk to Her, which switches from comedy to tragedy without warning: the film's bullfighter prologue and its silent-film insert are the two keys to everything Almodóvar believes about storytelling.",
        "Pedro Almodóvar — 'Women on the Verge' (1988) opening",
        ["Spanish", "Melodrama", "Color"],
    ),
    "dire-alejandro-gonzález-iñárritu-138": _entry(
        "Iñárritu won back-to-back Best Director Oscars — for Birdman (2014), shot to look like one continuous take, and The Revenant (2016), shot entirely in natural light. His early films (Amores Perros, 21 Grams, Babel) built intricate multi-story structures, and he is known for subjecting his actors to extreme conditions — Leonardo DiCaprio's bear attack was real, in the sense that the scene was real.",
        "Watch the opening of Birdman and notice the illusion: the film appears to be one unbroken take, and the first scene — a man meditating in his dressing room — establishes both the theatrical setting and the film's anxiety. Then watch the fight scene in the theater lobby, where the 'one take' breaks into chaos: the technical stunt is the movie's argument about the difference between acting and being.",
        "Alejandro González Iñárritu — 'Birdman' (2014) opening",
        ["Hollywood", "Drama", "2010s"],
    ),
    "dire-lucrecia-martel-139": _entry(
        "Martel is Argentina's great slow-cinema director — La Ciénaga (2001), The Headless Woman (2008), and Zama (2017) — and a rare filmmaker whose every feature is a masterpiece. Her films work on the senses first: heat, sound, water, and the long minutes in which the camera watches a family do nothing very much, and everything is revealed.",
        "Watch the opening of La Ciénaga and notice the heat: the film opens with a family at a swimming pool that is stagnant, and the camera stays low, watching bodies move in the humidity. Martel's method is anti-narrative: nothing is explained, everything is felt, and the film's accident — a broken glass, a fall — matters more than any plot. Then watch the ending of Zama, where the colonial antihero's story collapses into the mud: the movie's title could be the name of the place, the man, or the feeling.",
        "Lucrecia Martel — 'La Ciénaga' (2001) opening",
        ["Argentine", "Slow Cinema", "Sensory"],
    ),
    "dire-jean-pierre-jeunet-140": _entry(
        "Jeunet, with his visual partner Marc Caro, made the surreal Delicatessen (1991) and The City of Lost Children (1995) before his solo masterpiece Amélie (2001), the highest-grossing French-language film ever at its release. His style — sepia Paris, eccentric characters, and a camera that plays tricks — is instantly recognizable and endlessly imitated.",
        "Watch the opening of Amélie and notice the montage: Jeunet lists Amélie's small pleasures — cracking crème brûlée, skipping stones — in a rapid-fire sequence that establishes the film's tone before the plot begins. Then watch the scene where Amélie leads a blind man through Paris, describing the street life in detail: the movie's whole philosophy is in that scene, the world made visible by attention.",
        "Jean-Pierre Jeunet — 'Amélie' (2001) opening",
        ["French", "Whimsy", "2000s"],
    ),
    "dire-michael-haneke-141": _entry(
        "Haneke makes films about the violence in ordinary life — Funny Games (1997) holds the audience responsible for wanting violence, Cache (2005) turns surveillance into a moral horror film, and The White Ribbon (2009) traces fascism to a German village's cruelty. His movies refuse comfort and are among the most rigorous in cinema.",
        "Watch the opening of Funny Games and notice the film's first act of aggression: the killers are polite, and the family's destruction is staged with the camera holding steady — Haneke's violence is always filmed without spectacle, which is the point. Then watch the famous rewind scene, where one of the killers reaches into the film to rewind it: Haneke's thesis is that the audience, not the screen, is the subject.",
        "Michael Haneke — 'Funny Games' (1997) opening",
        ["Austrian", "Provocation", "Thriller"],
    ),
    "dire-damien-chazelle-142": _entry(
        "Chazelle became the youngest Best Director winner in history at 32 — for La La Land (2016) — and his films are about obsession and jazz: Whiplash (2014) dramatizes the cost of greatness, and Babylon (2022) is a three-hour history of Hollywood's silent-to-sound transition. His movies are built around music and the people who sacrifice everything for it.",
        "Watch the opening of Whiplash — the drum solo that starts in darkness and never lets up — and notice what Chazelle does with sound: the film is edited to the music, and the drumming is the plot. Then watch the final sequence of La La Land, the 'what if' montage that rewrites the film's ending in a dream: Chazelle's signature is music as memory, and the last five minutes of La La Land are his best argument.",
        "Damien Chazelle — 'Whiplash' (2014) opening",
        ["Hollywood", "Music", "2010s"],
    ),
    "dire-chloé-zhao-143": _entry(
        "Zhao won the Best Director Oscar for Nomadland (2020) — only the second woman to win — and her films (Songs My Brothers Taught Me, The Rider) are shot with non-actors in the American West, blending documentary and fiction. She is the rare filmmaker who treats the land as a character and the people on it as its voice.",
        "Watch the opening of Nomadland and notice the casting: the film's 'van-dwellers' are largely real people playing themselves, and the film's first scenes establish the economics — a company town closes, a woman packs her van. Then watch the scene where Fern discusses her husband's death with another traveler: Zhao's method is to point the camera at real faces and let the moment carry the movie.",
        "Chloé Zhao — 'Nomadland' (2020) opening",
        ["Hollywood", "Drama", "2020s"],
    ),
    "dire-ari-aster-144": _entry(
        "Aster rebooted horror for the 2010s with Hereditary (2018) — a family tragedy that curdles into demonic ritual — and Midsommar (2019), a folk-horror film set in perpetual daylight. His horror is psychological first and supernatural second, and both films are about grief that the characters refuse to process until it processes them.",
        "Watch the opening of Hereditary and notice the funeral: the film's horror is grounded in the details of family grief — the eulogy, the arguments, the mother's obsessiveness — before anything supernatural appears. Then watch the final sequence of Midsommar, where a smiling ritual crowns a film of screaming: Aster's thesis is that the horror is the release, and his endings are ambiguous between triumph and annihilation.",
        "Ari Aster — 'Hereditary' (2018) opening",
        ["Horror", "Hollywood", "2010s"],
    ),
    "dire-sean-baker-145": _entry(
        "Baker makes films about Americans the movies usually ignore — the sex workers of Tangerine (2015), shot on an iPhone, the motel kids of The Florida Project (2017), and the stripper of Anora (2024), which won the Palme d'Or and the Best Picture Oscar. His films are comedies with open wounds, and his casting is half the method.",
        "Watch the opening of Tangerine and notice the format: the film was shot on three iPhone 5s with a $100 anamorphic adapter, and the color and energy of the opening — two trans sex workers in a donut shop — announce that Hollywood production values aren't the point. Then watch the final scene of The Florida Project, where the movie's fantasy ending suddenly turns real: Baker earns the switch, and the tears are the point.",
        "Sean Baker — 'Tangerine' (2015) opening",
        ["Independent", "Hollywood", "2020s"],
    ),
    "dire-paolo-sorrentino-146": _entry(
        "Sorrentino made The Great Beauty (2013), which won the Oscar for Best Foreign Language Film — a Roman party film that turns out to be about death, narrated by a man who has spent his life at parties. His style is baroque: opulent tracking shots, operatic music, and a worldview borrowed from Fellini, whom he openly reveres.",
        "Watch the opening of The Great Beauty and notice the contradiction: a rooftop party, a naked woman diving, champagne — and then the film's narrator, Jep, walks away from the party to stare at the ruins of Rome. Sorrentino's method is to stage splendor and then point at what it costs; the film's famous first scene, the party, is the best argument in modern cinema about how beauty and emptiness share an address.",
        "Paolo Sorrentino — 'The Great Beauty' (2013) opening",
        ["Italian", "Fellini-esque", "Oscar"],
    ),
}


def main() -> int:
    data = json.loads(PATH.read_text(encoding="utf-8"))
    by_id = {t["id"]: t for t in data}
    missing = [i for i in FIXES if i not in by_id]
    if missing:
        print(f"ERROR: ids not in file: {missing}")
        return 1

    changed = 0
    for topic in data:
        fix = FIXES.get(topic["id"])
        if fix is None:
            continue
        topic["teaser"] = _trim(fix["teaser"])
        topic["exploreAction"]["instruction"] = _trim(fix["instruction"])
        topic["exploreAction"]["targetName"] = fix["targetName"]
        topic["tags"] = fix["tags"]
        changed += 1

    PATH.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"updated {changed} entries")
    return 0


if __name__ == "__main__":
    sys.exit(main())
