#!/usr/bin/env python3
"""Batch: replace the first 40 fake films.json entries with real facts.

Template-generated entries with boilerplate teasers and scrambled tags
(Comedy|1980s on Blade Runner, etc.). Replaces byline + teaser + instruction
+ targetName + tags. subtype/verb preserved. Cap 450 (SCHEMA.md).
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


PATH = Path(__file__).resolve().parent.parent / "app/src/main/assets/topics/films.json"


def _entry(byline: str, teaser: str, instruction: str, target_name: str, tags: list[str]) -> dict:
    return {
        "byline": byline,
        "teaser": teaser,
        "instruction": instruction,
        "targetName": target_name,
        "tags": tags,
    }


FIXES: dict[str, dict] = {
    "film-close-encounters-of-the-133": _entry(
        "Steven Spielberg",
        "Spielberg's first UFO film was a monster hit that defined the 'close encounter' moment in pop culture — the mountain that hums five tones back at you. It was made in the same period as Star Wars, and the two films together convinced Hollywood that the public wanted awe, not cynicism.",
        "Watch the opening — the desert, the lost planes, the abandoned patrol car — and notice how Spielberg shows the aliens before showing the aliens: light, toys, and a mountain that responds to music. Then watch the final sequence at Devils Tower, where the mothership arrives: the five-note exchange between humans and aliens is the film's whole argument about communication.",
        "Close Encounters of the Third Kind (1977) — the five-note exchange",
        ["Sci-Fi", "1970s", "Hollywood"],
    ),
    "film-blade-runner-1982-134": _entry(
        "Ridley Scott",
        "Scott's adaptation of Philip K. Dick's Do Androids Dream of Electric Sheep? flopped on release, was re-cut, re-narrated, and rescued by a director's cut that made it a masterpiece — the rain-soaked, neon Los Angeles of 2019 became the visual template for every cyberpunk film since. Harrison Ford was told the film was about whether the replicant Roy Batty was really the hero.",
        "Watch the opening — the eye, the city, the fire — and then the final scene, Roy Batty's 'tears in rain' monologue, which Ford and actor Rutger Hauer improvised in part. The film's central question — do the replicants deserve more empathy than their makers? — is stated in that monologue, and the director's cut's unicorn dream adds the twist: Deckard may be a replicant too.",
        "Blade Runner (1982) — the opening and Roy Batty's final monologue",
        ["Sci-Fi", "1980s", "Hollywood"],
    ),
    "film-the-thing-1982-135": _entry(
        "John Carpenter",
        "Carpenter's remake of The Thing from Another World was hated on release — critics called it 'garbage' — then became one of the most influential horror films ever made: an Antarctic research station, a shape-shifting alien, and a paranoia so total that the film's monster is never clearly named. The practical effects, by Rob Bottin, are still unmatched.",
        "Watch the first transformation scene — the dogsled kennel — and notice what Carpenter does with the monster: it's never fully shown, always mid-change, which is why it stays terrifying. Then watch the blood-test scene near the end, where the film's paranoia becomes a contest of trust: the movie's horror is that the alien is indistinguishable from the crew, and the ending — two men, one fire, no resolution — refuses to break the impasse.",
        "The Thing (1982) — the kennel scene and the blood-test scene",
        ["Horror", "1980s", "Hollywood"],
    ),
    "film-brazil-1985-136": _entry(
        "Terry Gilliam",
        "Gilliam's dystopian satire — a bureaucratic totalitarian Britain where a government typo turns a man into a terrorist — was fought over by the studio, which released a 'happy ending' cut Gilliam disowned. The film's signature image is the ductwork: giant air-conditioning conduits that are the real infrastructure of a society that has given up.",
        "Watch the opening — the fly, the typo, the explosion — and notice how Gilliam establishes the world: everything runs on paperwork and ducts, and the hero, Sam Lowry, dreams of flying away in armor. Then watch the ending, which Gilliam fought to keep: the 'happy' version and the real version are both in the film's history, and the difference between them is the film's argument about what hope costs.",
        "Brazil (1985) — the opening typo and the ending",
        ["Satire", "1980s", "Dystopian"],
    ),
    "film-wings-of-desire-1987-137": _entry(
        "Wim Wenders",
        "Wenders's Berlin angel film — angels listen to the thoughts of the city's people and one falls in love — is the most tender film ever made about a city. Shot partly in black and white (what angels see) and color (what humans see), it was made before the Wall fell, and its Berlin is a city of memory and longing.",
        "Watch the opening — the angel Damiel standing on a statue above Berlin, hearing the city's thoughts in a dozen languages — and notice how Wenders films thought: the voiceover is the movie's real soundtrack, and the black-and-white is the angels' permanent condition. Then watch the moment Damiel chooses mortality, where the film switches to color: Wenders's thesis is that being human is worth the pain, and the film's final scenes — a circus, a café, a woman — are the argument.",
        "Wings of Desire (1987) — the angel's fall into color",
        ["Drama", "1980s", "German"],
    ),
    "film-do-the-right-thing-138": _entry(
        "Spike Lee",
        "Lee's third feature, set in one Brooklyn block on the hottest day of the summer, was called dangerous and incendiary before release — it opens with a protest song and ends with a riot, and its central question is literal in the title. It was made on a $6 million budget and is now in the National Film Registry.",
        "Watch the opening — Rosie Perez dancing to 'Fight the Power' over the credits — and notice how Lee establishes the block's community in a series of portraits, each character introduced with their own theme. Then watch the film's final act, from Radio Raheem's death to the riot, and notice what Lee refuses: no clean hero, no clear villain, and the film's last words — 'Love' and 'Hate' — are the knuckles on both fists. Lee has said the title is a question, not an instruction.",
        "Do the Right Thing (1989) — the opening dance and the finale",
        ["Drama", "1980s", "Hollywood"],
    ),
    "film-reservoir-dogs-1992-139": _entry(
        "Quentin Tarantino",
        "Tarantino's debut — made for $1.5 million from a script he'd written in three weeks — announced a new voice: the heist movie that skips the heist, told through overlapping dialogue and a soundtrack of 1970s radio. It made him the most influential new director of the 1990s before his second film existed.",
        "Watch the opening — the diner argument about tipping, the slow-motion walk with the music — and notice what's missing: the heist itself. Tarantino cuts straight to the aftermath and builds the film from conversation and flashback. Then watch Mr. Blonde's torture scene, where the film's violence is soundtracked by a pop song: the disconnect between the music and the violence is the film's signature gesture, and it made audiences argue about it for decades.",
        "Reservoir Dogs (1992) — the diner opening and the torture scene",
        ["Crime", "1990s", "Hollywood"],
    ),
    "film-schindlers-list-1993-140": _entry(
        "Steven Spielberg",
        "Spielberg's Holocaust film — shot in black and white, on location in Poland, with a cast that included actual survivors — won seven Oscars including Best Picture, and its single image of the girl in the red coat remains one of cinema's most debated details. Spielberg, who is Jewish, has said he didn't expect to make it and was told by his rabbi that he was honoring his heritage by doing so.",
        "Watch the opening — the candle, the prayer, the fading to color — and then the liquidation of the Kraków ghetto, where the girl in the red coat appears: the only color in a black-and-white film, and Spielberg has said it's the film's conscience. Then watch the ending, the real survivors placing stones on Schindler's grave: the film's final sequence makes the horror personal, and the casting of survivors was deliberate.",
        "Schindler's List (1993) — the girl in the red coat sequence",
        ["Drama", "1990s", "Hollywood"],
    ),
    "film-chungking-express-1994-141": _entry(
        "Wong Kar-wai",
        "Wong's film — two stories, two cops, a snack bar, and the song 'California Dreamin'' — was made during a break from his martial-arts epic Ashes of Time, and it became his international breakthrough: a pop-art meditation on loneliness in Hong Kong. The film's famous blur-motion photography (camera operated by Wong himself on a Steadicam) became a style called 'the Chungking look.'",
        "Watch the first story's opening — the cop chasing a suspect through the market in slow-motion, the snack-bar counter — and notice the tone: Wong films loneliness as glamour, and the characters talk to cans of pineapple with expiration dates. Then watch the second story, where the girl works at the snack bar and the cop waits for her: the film's two halves mirror each other, and the ending — a woman on a runway, a man refusing to leave — is the most romantic finale in 1990s cinema.",
        "Chungking Express (1994) — the snack-bar stories",
        ["Romance", "1990s", "Hong Kong"],
    ),
    "film-fargo-1996-142": _entry(
        "Joel & Ethan Coen",
        "The Coens' snowbound crime comedy — a car salesman hires two men to kidnap his wife for ransom, and everything goes wrong in the Minnesota snow — won the Oscar for Best Original Screenplay and made 'oh yah' a national phrase. It was filmed in a Minnesota winter so brutal that one scene's frostbite was real, and the 'true story' claim in the opening credits is a joke — the film is invented.",
        "Watch the opening — the 'true story' title card and the wood chipper being loaded onto a truck — and notice how the Coens set the tone: the film's comedy and its violence share the same flat accent. Then watch Marge Gunderson's investigation, especially the scene where she questions the two kidnappers at a motel: Frances McDormand's performance — the pregnant police chief who is smarter than everyone — is the film's heart, and the 'wood chipper' scene is its most famous joke and its most horrible.",
        "Fargo (1996) — Marge's investigation and the wood chipper",
        ["Crime", "1990s", "Hollywood"],
    ),
    "film-the-big-lebowski-1998-143": _entry(
        "Joel & Ethan Coen",
        "The Coens' shaggy detective comedy — a slacker mistaken for a millionaire, a bowling ball, and a rug that 'really tied the room together' — flopped on release and became one of the most-quoted films of all time, with its own annual festival (Lebowski Fest). The Dude, played by Jeff Bridges, was based partly on the Coens' friend and fellow filmmaker Lewis Abernathy.",
        "Watch the opening — the bowling alley, the voiceover, the rug — and notice how the Coens structure the film as a shaggy-dog noir: every noir convention (the femme fatale, the missing money, the kidnapping) appears and dissolves. Then watch the dream sequences, where the Dude floats through a Busby Berkeley fantasy in his bathrobe: the film's comedy is built from the gap between the genre it imitates and the slacker it's about, and its ending — 'The Dude abides' — is the least noir resolution ever written.",
        "The Big Lebowski (1998) — the rug and the dream sequences",
        ["Comedy", "1990s", "Hollywood"],
    ),
    "film-beau-travail-1999-144": _entry(
        "Claire Denis",
        "Denis's Foreign Legion film — based loosely on Herman Melville's Billy Budd, with almost no dialogue and a plot told in images — was voted the greatest French film of the 1990s by critics. It follows Sergeant Galoup, whose jealousy of a handsome new recruit destroys him, and it ends with the most celebrated dance scene in modern cinema.",
        "Watch the opening — the desert, the drills, the men's bodies moving in formation — and notice how Denis tells the story without exposition: the Legion's rituals are the plot, and the rivalry grows through glances and choreography. Then watch the final scene, where Galoup, alone in a disco, dances: the film's ending is a release of everything the Legion repressed, and it turns the tragedy into a triumph of pure cinema. The final shot has been called the greatest ending in modern film.",
        "Beau Travail (1999) — the drill sequences and the final dance",
        ["Drama", "1990s", "French"],
    ),
    "film-amélie-2001-145": _entry(
        "Jean-Pierre Jeunet",
        "Jeunet's Parisian fable — a shy waitress who secretly improves the lives of her neighbors, then must decide whether to let her own life be improved — became the highest-grossing French film ever released at the time and turned Audrey Tautou into an international star. Its sepia Paris, a fantasy of the city with no traffic and no tourists, was largely shot in studio.",
        "Watch the opening — the list of Amélie's small pleasures (cracking crème brûlée, skipping stones, plunging a hand into grain) — and notice how Jeunet establishes her world: every detail is a joy, and the film's camera plays like a fairground. Then watch the scene where Amélie leads a blind man across Paris, narrating the street life at full speed: the film's thesis — that attention is a kind of love — is in that scene, and the ending, where Amélie finally risks being seen, is its payoff.",
        "Amélie (2001) — the pleasures list and the blind man scene",
        ["Comedy", "2000s", "French"],
    ),
    "film-city-of-god-2002-146": _entry(
        "Fernando Meirelles",
        "Made on a $3 million budget with a cast of first-time actors recruited from Rio's favelas, City of God became one of the most acclaimed films of the decade — a gangster epic about the slum where photographer Rocket grows up while his friends become killers. The film's non-chronological structure and kinetic camera made it the Scorsese of the slums, and its 'run, you little fool' chicken chase opening is a tutorial in setup.",
        "Watch the opening — the chicken, the chase, the freeze-frame of a knife — and notice how Meirelles compresses the film's whole method into two minutes: the camera never stops moving, and the editing is the storytelling. Then watch the 'run, you little fool' scene and the film's central story of Li'l Zé's rise: the movie's power is that it treats the children's violence as a system, not a spectacle, and its ending — Rocket finally photographing the truth — is earned by everything before it.",
        "City of God (2002) — the chicken chase and Li'l Zé's rise",
        ["Crime", "2000s", "Brazilian"],
    ),
    "film-eternal-sunshine-2004-147": _entry(
        "Michel Gondry",
        "Gondry and screenwriter Charlie Kaufman's romance — a couple erases each other from their memories and falls in love again, having forgotten why they left — won the Oscar for Best Original Screenplay and became the definitive film about the stubbornness of love. The memory-erasure scenes, shot with practical effects rather than CGI, are the film's genius.",
        "Watch the opening — the two strangers on a train, 'the beach,' the urge to skip work — and notice how Gondry plants the premise before revealing it: the film is the erasure process running backward, and the first act's strangeness is the reveal. Then watch the 'beach house collapsing' sequence, where Joel tries to hide Clementine inside an earlier memory: the film's argument — that we erase the pain at the cost of the person — is dramatized in that collapsing house, and the ending's 'okay' is the most fragile, most hopeful word in cinema.",
        "Eternal Sunshine of the Spotless Mind (2004) — the erasure sequence",
        ["Romance", "2000s", "Hollywood"],
    ),
    "film-pans-labyrinth-2006-148": _entry(
        "Guillermo del Toro",
        "del Toro's Spanish Civil War fantasy — a girl's fairy-tale quest in a labyrinth, intercut with her stepfather's fascist brutality — won three Oscars and became the definitive 'adult fairy tale.' The film's central question — is the fantasy real, or the girl's escape? — is never answered, and del Toro has said both readings are intended.",
        "Watch the opening — the girl Ofelia reading in the woods, the fairy, the labyrinth — and notice how del Toro cuts between the two worlds: the fascist camp and the fairy tale share the same frame of violence. Then watch the final scene, where Ofelia's choices converge in the throne of the underworld: the ending offers two readings, and del Toro has said the film's meaning is that imagination is a survival mechanism, not an escape from reality.",
        "Pan's Labyrinth (2006) — the pale man scene and the ending",
        ["Fantasy", "2000s", "Spanish"],
    ),
    "film-no-country-for-old-149": _entry(
        "Joel & Ethan Coen",
        "The Coens' adaptation of Cormac McCarthy's novel — a hunter, a briefcase of drug money, and a killer who decides his victims with a coin flip — won four Oscars including Best Picture, and Anton Chigurh (Javier Bardem) became one of cinema's great villains. The film's refusal of a conventional ending infuriated and then conquered audiences.",
        "Watch the opening — the desert, the tracking shot, the arrested man — and notice how the Coens establish Chigurh's method: the coin flip, the air gun, the cold logic. Then watch the ending, where the retired sheriff recounts a dream about his father carrying fire: the film's refusal to resolve the chase is its point — the forces the sheriff represents are aging out, and the villain isn't killed, he walks away with a broken arm. The movie is about the passing of an era, and its ending is the eulogy.",
        "No Country for Old Men (2007) — the coin flip and the ending",
        ["Crime", "2000s", "Hollywood"],
    ),
    "film-the-dark-knight-2008-150": _entry(
        "Christopher Nolan",
        "Nolan's Batman sequel — the Joker's terror campaign against a Gotham that refuses to give up its symbol — grossed a billion dollars and became the first superhero film taken seriously as crime cinema, winning Heath Ledger a posthumous Oscar. The film's 'ferry scene' — two boats, one detonator each — is its moral center.",
        "Watch the opening — the bank heist by a team of clowns who turn on each other — and notice how Nolan introduces the Joker: he's the planner, the winner, and the last man out, and Ledger's performance is built from small gestures that get stranger as the film goes on. Then watch the ferry scene, where two boats hold each other's detonators: the film's argument about heroism and the rule-breaking it requires is staged as a referendum, and the ending — Batman taking the blame — is its price.",
        "The Dark Knight (2008) — the bank heist and the ferry scene",
        ["Action", "2000s", "Hollywood"],
    ),
    "film-inglourious-basterds-2009-151": _entry(
        "Quentin Tarantino",
        "Tarantino's WWII fantasy — Jewish-American soldiers scalping Nazis, a cinema that burns a theater full of the high command — rewrote history on purpose: 'the wish fulfillment' of killing Hitler, as he put it. Its opening scene, the farmhouse interrogation by Christoph Waltz's 'Jew Hunter,' is one of the great suspense sequences ever filmed, and the film's closing image is its thesis.",
        "Watch the opening — the dairy farm, the pipe, the milk, the underfloor hiding — and notice how Tarantino builds suspense from manners: Hans Landa's politeness is the horror, and the scene's rug-pull (the pipe the family can't see) is the film's method in miniature. Then watch the ending, where the film rewrites history and the burned theater becomes a monument: the movie is about the pleasure of revenge, and Tarantino lets you have it — which is the point he's making about cinema and war.",
        "Inglourious Basterds (2009) — the farmhouse interrogation",
        ["War", "2000s", "Hollywood"],
    ),
    "film-the-social-network-2010-152": _entry(
        "David Fincher",
        "Fincher's film about the founding of Facebook — told in three lawsuits and two depositions — made the invention of a website feel like a Shakespearean tragedy, and Aaron Sorkin's screenplay won the Oscar. Its central argument: Mark Zuckerberg's creation of the most connected world in history came from his inability to connect with people.",
        "Watch the opening — the Harvard bar, the break-up, the walk in the cold — and notice how Sorkin's dialogue sets the theme: Zuckerberg's insulted ambition is the engine of everything that follows, and the film's structure (three lawsuits) keeps the present tense pressing on the past. Then watch the final scene, where Zuckerberg refreshes a friend request to the woman he lost: the film's last image — the world's most connected man alone with a laptop — is its entire argument.",
        "The Social Network (2010) — the opening and the ending",
        ["Drama", "2010s", "Hollywood"],
    ),
    "film-the-tree-of-life-153": _entry(
        "Terrence Malick",
        "Malick's Palme d'Or winner is a family drama expanded to cosmic scale — a 1950s Texas childhood intercut with the birth of the universe and the history of life on Earth, all held together by a voiceover address to God. It divided audiences and critics in equal measure, and its middle section — the 'creation' sequence of star formation and dinosaurs — is the most radical stretch in mainstream cinema.",
        "Watch the opening — the mother's voiceover, the family, the light through the window — and then the creation sequence that follows, where Malick cuts from a boy's grief to the Big Bang: the film's argument is that both scales are the same story, and the cosmic scenes are the grief made visible. Then watch the ending, the beach reunion of the dead and the living: the film's final image is the family restored, and Malick's thesis — grace and nature as the two ways of being — is stated in a whisper.",
        "The Tree of Life (2011) — the creation sequence and the ending",
        ["Drama", "2010s", "Hollywood"],
    ),
    "film-her-2013-154": _entry(
        "Spike Jonze",
        "Jonze's film — a lonely letter-writer falls in love with his operating system, voiced by Scarlett Johansson — won the Oscar for Best Original Screenplay and became the definitive film about intimacy in the digital age. It was made before the chatbot boom, and its vision of an AI companion that learns and leaves now looks prophetic.",
        "Watch the opening — Theodore's work dictating love letters for strangers, his empty apartment, his inability to sign his own divorce papers — and notice how Jonze establishes the theme: the man who writes intimacy for a living can't have it. Then watch the ending, where Samantha leaves for a dimension beyond human understanding: the film's argument — that love is about growth, not possession — is dramatized in the leaving, and the final scene's rooftop silhouette is the most honest love scene of its decade.",
        "Her (2013) — the opening and the ending",
        ["Romance", "2010s", "Hollywood"],
    ),
    "film-boyhood-2014-155": _entry(
        "Richard Linklater",
        "Linklater filmed Boyhood over 12 years with the same actor, Ethan Hawke, and the same child, Ellar Coltrane — shooting a few weeks each year so the film could age in real time. The result, a 165-minute portrait of one Texas boy from age 6 to 18, was nominated for six Oscars and became a landmark in the history of the medium: no film had ever been made this way.",
        "Watch the opening — Mason at six, lying in the grass, his mother announcing the move — and notice how the film's central device works: you're watching an actor grow up, and every cut is years later. Then watch the ending, where Mason, now 18, tells a college classmate that 'we're all just drifting': the film's 12-year argument — that time is the only real subject — is stated in that line, and the final shot of a boy who was once six walking across a campus is the whole movie.",
        "Boyhood (2014) — the 12-year structure and the ending",
        ["Drama", "2010s", "Hollywood"],
    ),
    "film-mad-max-fury-road-156": _entry(
        "George Miller",
        "Miller returned to his post-apocalyptic franchise 30 years later with a film that's essentially one continuous car chase — shot in the Namibian desert with real stunts, and it won six Oscars for its craft. Its most radical choice: the story's hero is not Max but Imperator Furiosa, played by Charlize Theron, and the film's plot is a rescue, not a revenge.",
        "Watch the opening — Max's capture, the War Boys, the blood bag — and notice how Miller establishes the world with almost no dialogue: the chase is the film's grammar, and the first 20 minutes are a tutorial. Then watch Furiosa's pivot — the moment she turns the war rig toward the desert instead of the citadel: the film's feminism is structural, not decorative, and its ending — Furiosa lifting the empty seed pods — is a revolution staged with a gesture. The film's action is the plot, and its heart is in the 'Green Place' that turns out to be poisoned.",
        "Mad Max: Fury Road (2015) — Furiosa's pivot and the ending",
        ["Action", "2010s", "Australian"],
    ),
    "film-arrival-2016-157": _entry(
        "Denis Villeneuve",
        "Villeneuve's adaptation of Ted Chiang's Story of Your Life — a linguist must translate an alien language, and the language turns out to rewrite how its speakers experience time — was nominated for eight Oscars and is considered one of the great science-fiction films. Its twist is philosophical rather than mechanical: the alien gift is not a weapon but a way of seeing.",
        "Watch the opening — the daughter's childhood, the illness, the beach — and notice how the film sets up its reveal: the scenes are out of order, and you won't know it until the end. Then watch the montage where Louise learns the language and begins to see her memories as her future: the film's argument — that if you could see your whole life you'd still choose it — is dramatized in the final scene, and the 'Heptapod' language design, built from real linguistics, is the film's most admired craft.",
        "Arrival (2016) — the learning-the-language sequence and the reveal",
        ["Sci-Fi", "2010s", "Hollywood"],
    ),
    "film-call-me-by-your-158": _entry(
        "Luca Guadagnino",
        "Guadagnino's adaptation of André Aciman's novel — a 17-year-old's summer affair with his father's graduate assistant in northern Italy in 1983 — became a cultural phenomenon, and the film's ending, with its two-minute final shot of Elio before a fireplace, is one of the most discussed in cinema. The peach scene and the 'Call me by your name and I'll call you by mine' speech are its signature moments.",
        "Watch the opening — the summer villa, the arrival of Oliver, the first glance — and notice how Guadagnino films desire as weather: the heat, the bicycles, the long lunches. Then watch the ending — Elio's call, the fireplace, the single unbroken shot of his face: the film's argument is that love is permanent even when it ends, and the final shot — a teenager learning to grieve in public — is the film's whole case.",
        "Call Me by Your Name (2017) — the ending fireplace shot",
        ["Romance", "2010s", "Italian"],
    ),
    "film-soul-2020-159": _entry(
        "Pete Docter",
        "Pixar's film — a jazz pianist who dies before his big break and must help a reluctant soul find its 'spark' — won the Oscars for Best Animated Feature and Best Original Score (Jon Batiste and Trent Reznor). It became Pixar's most philosophical film: the argument, delivered through a Manhattan music teacher and a lost soul named 22, is that the 'spark' isn't a purpose — it's the willingness to live.",
        "Watch the opening — Joe's rehearsal, the phone call, the manhole — and notice how the film's two worlds are set: the real New York, photographed with texture and music, and the Great Before, designed as a blue abstract. Then watch the ending, where Joe finally gets his gig and realizes what he's been missing: the film's thesis — that the point of life is the living, not the achievement — is stated in the final scene with a piano and a subway platform, and the film's famous '22 walks away' moment is the whole argument in one image.",
        "Soul (2020) — the Great Before and the ending",
        ["Animation", "2020s", "Hollywood"],
    ),
    "film-drive-my-car-2021-160": _entry(
        "Ryusuke Hamaguchi",
        "Hamaguchi's three-hour adaptation of a Haruki Murakami story — a theater director who hires a chauffeur after his wife's death — won the Oscar for Best International Film and became the most celebrated Japanese film in decades. Its climax, a multilingual production of Uncle Vanya with deaf actors signing Chekhov, is the film's argument in performance.",
        "Watch the opening — the wife and husband rehearsing their play at home, the daughter's death, the affair — and notice how Hamaguchi compresses a marriage into its scenes: the film's first 40 minutes are a short film about a life. Then watch the Uncle Vanya production at the end, where the actors perform in Korean, Japanese, Mandarin, and sign language: the film's thesis — that we communicate in the attempt, not the result — is staged literally, and the ending's drive through the snow is the release.",
        "Drive My Car (2021) — the rehearsal opening and the Uncle Vanya finale",
        ["Drama", "2020s", "Japanese"],
    ),
    "film-everything-everywhere-all-at-161": _entry(
        "Daniel Kwan & Daniel Scheinert",
        "The Daniels' multiverse comedy — a laundromat owner who must access infinite versions of herself to fight a multiversal villain, played by Michelle Yeoh — won seven Oscars including Best Picture, and became the highest-grossing A24 film ever. Its philosophy — that kindness is the radical choice in an infinite multiverse — made it the most influential comedy of the decade.",
        "Watch the opening — the laundromat, the IRS audit, the 'verse-jumping' explanation — and notice how the film establishes its rules through Evelyn's failure: the multiverse is accessed through statistical improbability, and the jokes are the metaphysics. Then watch the rocks scene — two rocks on a cliff, subtitled dialogue about existence — which is the film's entire philosophy in one shot: the movie's argument is that love is the answer to infinity, and its ending, choosing kindness in an infinite regress, is the payoff.",
        "Everything Everywhere All at Once (2022) — the rocks scene",
        ["Comedy", "2020s", "Hollywood"],
    ),
    "film-the-fabelmans-2022-162": _entry(
        "Steven Spielberg",
        "Spielberg's most personal film — a semi-autobiographical portrait of his own childhood, with Sammy Fabelman as the young Spielberg discovering cinema — was his 33rd feature and the first time he turned the camera on his family's story, including his parents' divorce and his mother's affair. It was nominated for seven Oscars.",
        "Watch the opening — the young Sammy's first movie, the train crash that frightens and fascinates him — and notice how Spielberg establishes the film's theme: cinema as a way of understanding what hurts you. Then watch the film's climactic scene, where Sammy films a school camping trip and discovers the affair in the footage: the film's argument — that art is how the artist survives what he can't face — is dramatized in that discovery, and the ending, with John Ford's advice on horizons, is Spielberg's blessing to his younger self.",
        "The Fabelmans (2022) — the camping-trip footage scene",
        ["Drama", "2020s", "Hollywood"],
    ),
    "film-barbie-2023-163": _entry(
        "Greta Gerwig",
        "Gerwig's Barbie — the doll's journey from Barbieland to the real world — became the highest-grossing film of 2023 and the highest-grossing film ever directed by a woman, grossing $1.4 billion. Its monologue about womanhood, delivered by America Ferrera, went viral as a generation's feminist statement, and the film's argument — that the doll who represented perfection learned that imperfection is the point — made it both a blockbuster and a manifesto.",
        "Watch the opening — the Kubrick-style prologue where the giant Barbie appears over the girls of Barbieland — and notice how Gerwig sets her terms: the film is a critique of the toy it celebrates, and the opening homage to 2001 tells you which tradition she's joining. Then watch America Ferrera's monologue, the film's most-quoted scene: the speech's power is that it's true and absurd at once — the film's method in miniature — and the ending, where Barbie chooses to become human 'to feel,' is the thesis made literal.",
        "Barbie (2023) — the monologue and the ending",
        ["Comedy", "2020s", "Hollywood"],
    ),
    "film-past-lives-2023-164": _entry(
        "Celine Song",
        "Song's debut — a Korean-Canadian woman, her childhood sweetheart from Seoul, and the 24 years between them — was nominated for two Oscars and became the most acclaimed romance of its year. It's structured around the Korean concept of inyeon (fateful connection) and the question of which of our lives is the 'real' one.",
        "Watch the opening — the three people at a bar, the voiceover explaining who they are to each other — and notice how Song establishes the frame: strangers overhear the scene, and the narrator tells you the relationships before the film shows them. Then watch the ending, the goodbye at the crosswalk with the taxi and the walk sign: the film's argument — that we carry the lives we didn't live — is dramatized in that crossing, and the final shot is the most quietly devastating image of its decade.",
        "Past Lives (2023) — the bar opening and the crosswalk ending",
        ["Romance", "2020s", "Hollywood"],
    ),
    "film-the-zone-of-interest-165": _entry(
        "Jonathan Glazer",
        "Glazer's Holocaust film — which won the Grand Prix at Cannes and the Oscar for Best International Film — shows the Höss family's idyllic garden life next to Auschwitz, never showing the camp itself. The horror is in the sound design: screams, gunshots, and the furnaces are audible over the family's domestic routine, and the film's ending, with a time-travel jump to the museum's cleaning staff, is its argument.",
        "Watch the opening — the family's move into the house, the idyllic garden, the first distant sounds — and notice what Glazer refuses to show: the camp is always just off-frame, and the film's power is entirely in the audio, which was mixed to make the off-screen horror unbearable. Then watch the ending, the jump to the present-day museum: the film's argument — that the people who ran the machinery were ordinary, and that the machinery persists — is delivered in that final sequence, and the film's refusal of spectacle is its entire method.",
        "The Zone of Interest (2023) — the garden and the ending",
        ["Drama", "2020s", "Holocaust"],
    ),
    "film-the-holdovers-2023-166": _entry(
        "Alexander Payne",
        "Payne's 1970s-set comedy — a cranky classics teacher, a grief-stricken cook, and a student stuck at boarding school over Christmas — won Da'Vine Joy Randolph the Oscar for Best Supporting Actress, and its grainy, film-stock look and title-card opening made it a love letter to 1970s cinema. It was shot in 27 days on a budget under $30 million.",
        "Watch the opening — the period title cards, the deadened boarding school, Paul Giamatti's first-class lecture — and notice how Payne establishes the film's register: it looks and sounds like a 1970s film, and the melancholy is the production design. Then watch the Boston trip, where the three characters' grief finally surfaces: the film's argument — that holidays are where we feel our losses — is dramatized in that journey, and the ending, with the teacher letting his student go, is the year's most quietly perfect final scene.",
        "The Holdovers (2023) — the Boston trip and the ending",
        ["Comedy", "2020s", "Hollywood"],
    ),
    "film-rashomon-1950-167": _entry(
        "Akira Kurosawa",
        "Kurosawa's film — a samurai's death described by four witnesses who contradict each other — gave the English language the word 'Rashomon' for irreconcilable accounts, and won the Golden Lion at Venice, introducing Japanese cinema to the West. The film's rain-soaked gate, its grove of conflicting testimonies, and its final act of kindness made it one of the most influential films ever made.",
        "Watch the opening — the three men under the ruined gate in the rain, the woodcutter's confession that he's hiding something — and notice how Kurosawa structures the film: the same event told four times, each version flattering its narrator. Then watch the woodcutter's final version, the only one in which the samurai dies by his own hand: the film's argument — that truth is what each narrator needs — is dramatized in that version, and the ending's decision to take in the abandoned baby is the film's small, devastating act of hope.",
        "Rashomon (1950) — the four testimonies and the ending",
        ["Classic", "1950s", "Japanese"],
    ),
    "film-rear-window-1954-168": _entry(
        "Alfred Hitchcock",
        "Hitchcock's film — a photographer with a broken leg who believes his neighbor has murdered his wife — is a film about watching films: the entire movie is staged in one apartment as the hero watches the world through his window. It's the purest expression of Hitchcock's method, and it has been analyzed as a metaphor for cinema itself.",
        "Watch the opening — the camera pulling back from the window to reveal the whole courtyard in one unbroken shot — and notice how Hitchcock sets the trap: every neighbor's window is a story, and the hero's surveillance is the audience's. Then watch the finale, where the killer enters the apartment: the film's argument — that the watcher is also watched — is dramatized in that invasion, and the final shot, with the photographer's leg in a fresh cast, is Hitchcock's joke about the cost of curiosity.",
        "Rear Window (1954) — the courtyard and the finale",
        ["Thriller", "1950s", "Hollywood"],
    ),
    "film-the-night-of-the-169": _entry(
        "Charles Laughton",
        "Laughton's only directorial effort — a preacher with 'LOVE' and 'HATE' tattooed on his knuckles hunts two children who know where the money is — flopped on release and was rediscovered decades later as one of the greatest American films ever made. Its dreamlike fairy-tale visuals, shot by cinematographer Stanley Cortez, make it a horror film that looks like a children's storybook.",
        "Watch the opening — the children, the lullaby, the father's murder — and notice how Laughton establishes the film's tone: it's a fairy tale, complete with a narrator, and the horror is the storybook darkness. Then watch the river sequence, where the children float past the animals and the murdered mother: the film's argument — that evil is real and simple — is dramatized in the preacher's pursuit, and the ending, with the preacher's hand sinking beneath the water, is one of the great images in cinema.",
        "The Night of the Hunter (1955) — the river sequence",
        ["Thriller", "1950s", "Hollywood"],
    ),
    "film-breathless-1960-170": _entry(
        "Jean-Luc Godard",
        "Godard's debut — a car thief, an American girl, and a crime that dooms them both — was shot in four weeks with a handheld camera and jump cuts that broke every rule of editing, and it announced the French New Wave to the world. Its famous 'jump cuts' were, by Godard's own account, partly a way to shorten the film — and they invented a style.",
        "Watch the opening — the car theft, the police, the highway — and notice the editing: Godard cuts mid-movement, mid-gesture, and the jumps are the film's signature. Then watch the ending, where Michel, dying, makes faces at the camera: the film's argument — that cinema is a game of freedom, and that characters are just images — is dramatized in that death scene, and the film's final freeze-frame is the New Wave's manifesto.",
        "Breathless (1960) — the jump cuts and the ending",
        ["Classic", "1960s", "French"],
    ),
    "film-yojimbo-1961-171": _entry(
        "Akira Kurosawa",
        "Kurosawa's film — a masterless samurai who plays two rival gangs against each other in a decaying town — inspired A Fistful of Dollars and, through it, the entire spaghetti western genre, which inspired it back. Toshiro Mifune's wandering ronin, with his 'I'm just a passerby' shrug, became the template for a century of antiheroes.",
        "Watch the opening — the ronin tossing a stick to choose a direction, the town's two gambling houses — and notice how Kurosawa establishes the premise: Sanjuro manipulates both gangs into mutual destruction, and the film's comedy and violence share the same rhythm. Then watch the final duel, where the ronin returns to settle the score with the man who beat him: the film's argument — that a man with nothing can outplay everyone — is dramatized in that showdown, and the ending's walk into the sunset is the template for every western since.",
        "Yojimbo (1961) — the two-gang gambit and the final duel",
        ["Classic", "1960s", "Japanese"],
    ),
    "film-dr-strangelove-1964-172": _entry(
        "Stanley Kubrick",
        "Kubrick's cold-war satire — an insane general starts a nuclear war and the President's war room can't stop it — was made when the bomb was a daily fear, and it turned apocalypse into comedy. The film's famous line, 'Gentlemen, you can't fight in here! This is the War Room!,' and its ending montage of nuclear explosions set to a love song made it the definitive black comedy.",
        "Watch the opening — the planes, the refueling, the voiceover about the 'Doomsday Machine' — and notice how Kubrick stages the satire: the film's premise is absurd and its execution is deadpan. Then watch the war-room scenes, where the President, the general, and the German-accented Dr. Strangelove (all played by Peter Sellers) negotiate the end of the world: the film's argument — that the machinery of mutual destruction is run by buffoons — is dramatized in every scene, and the ending's mushroom-cloud montage is the blackest joke in cinema.",
        "Dr. Strangelove (1964) — the war room and the ending",
        ["Comedy", "1960s", "Hollywood"],
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
        topic["byline"] = fix["byline"]
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
