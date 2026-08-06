#!/usr/bin/env python3
"""Batch: replace the final 27 fake films.json entries with real facts.

Template-generated entries with boilerplate teasers and scrambled tags.
Replaces byline + teaser + instruction + targetName + tags.
subtype/verb preserved. Cap 450 (SCHEMA.md).
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
    "film-2001-a-space-odyssey-173": _entry(
        "Stanley Kubrick",
        "Kubrick and Arthur C. Clarke's 'proverbial good science fiction movie' — a monolith, a murderous computer, and a journey beyond Jupiter — took four years, cost $10 million, and opened to baffled reviews before becoming the most influential sci-fi film ever made. Its match-cut from a bone to a spaceship is the most famous cut in cinema history.",
        "Watch the opening — the ape-men, the monolith, the bone tossed into the air — and then the match cut to the orbiting space station: the whole history of human tool-making is compressed into that one cut, and Kubrick's method is scale. Then watch the HAL 9000 sequences, where the film's horror is delivered by a calm red eye and a flat voice: the argument that intelligence without empathy is lethal is dramatized in HAL's 'Dave, stop' — the most chilling line in sci-fi. The ending, with its star child, is an argument about evolution that still divides viewers.",
        "2001: A Space Odyssey (1968) — the match cut and the HAL sequences",
        ["Sci-Fi", "1960s", "Hollywood"],
    ),
    "film-the-conformist-1970-174": _entry(
        "Bernardo Bertolucci",
        "Bertolucci's political thriller — a Mussolini-era bureaucrat who betrays his blind professor and his own past to conform to fascism — is one of the most beautiful films ever made, with cinematography by Vittorio Storaro that influenced The Godfather. Its central irony: the man who joins fascism to be 'normal' is the most visibly tormented person on screen.",
        "Watch the opening — the opera glasses, the face, the blood — and notice how Bertolucci structures the film as memory: the plot moves between 1938 Rome, 1939 Paris, and Clerici's childhood, and every sequence is shot through a lens of guilt. Then watch the assassination scene in the snowy forest, where Clerici's betrayal of his professor reaches its consequence: the film's argument — that conformity is its own fascism, and that the state thrives on ordinary people's cowardice — is dramatized in that execution. The ending, where Clerici confesses to a stranger in the street, is the film's final judgment.",
        "The Conformist (1970) — the assassination in the snow",
        ["Thriller", "1970s", "Italian"],
    ),
    "film-aguirre-the-wrath-of-175": _entry(
        "Werner Herzog",
        "Herzog's conquistador epic — a mad explorer who declares himself the 'Wrath of God' and floats a raft of prisoners down the Amazon searching for El Dorado — was shot in the actual jungle with a cast and crew that nearly died of fever and hunger, and Klaus Kinski's performance became legendary. Its final image, Aguirre alone on a raft surrounded by monkeys, is one of cinema's great visions of madness.",
        "Watch the opening — the army of conquistadors descending the mountain into the jungle, the slaves and the pigs — and notice how Herzog establishes the scale: this is a documentary about a hallucination, and the film's voiceover is a real chronicle read as the empire collapses. Then watch Aguirre's rise, as he declares the expedition a rebellion: the film's argument — that imperial ambition is a form of madness — is dramatized in every scene, and the ending, with Aguirre pacing his raft declaring himself emperor of a world that doesn't exist, is the purest portrait of delusion ever filmed.",
        "Aguirre, the Wrath of God (1972) — the raft and the ending",
        ["Adventure", "1970s", "German"],
    ),
    "film-network-1976-176": _entry(
        "Sidney Lumet",
        "Lumet's satire — a failing anchor who announces he'll shoot himself on air, then becomes a prophet of rage for a ratings-hungry network — won four Oscars including Best Actor and Best Actress, and predicted the cable-news era with terrifying accuracy. Its famous 'I'm as mad as hell' scene became the film's enduring image: ordinary people leaning out of windows to shout at the sky.",
        "Watch the opening — Howard Beale's announcement that he'll kill himself on Tuesday, the newsroom's reaction, the first 'I'm mad as hell' — and notice how Lumet stages the film as a series of escalating surrenders: every ideal in the film is bought, and the characters' compromise is the plot. Then watch the ending, where the network executives decide to kill the prophet they created: the film's argument — that television will commodify even its own destruction — is delivered in the final line, and the film's prophecy of 'entertainment' swallowing news has come true twice over.",
        "Network (1976) — the 'mad as hell' scene and the ending",
        ["Satire", "1970s", "Hollywood"],
    ),
    "film-koyaanisqatsi-1982-177": _entry(
        "Godfrey Reggio",
        "Reggio's wordless documentary — Philip Glass's score over time-lapse footage of nature and civilization — takes its title from a Hopi word meaning 'life out of balance,' and the film has no narration, no characters, and no plot. It became the definitive 'visual music' film and made Glass a household name.",
        "Watch the opening — the slow-motion desert landscapes, the first time-lapse of clouds — and notice how the film establishes its grammar: nature is shown in slow, sacred motion, and the cut to the city is the film's first judgment. Then watch the 'bomb montage' near the end, where rockets and explosions are intercut with speeding traffic: the film's argument — that industrial civilization is a kind of trance, and that its beauty is indistinguishable from its violence — is made entirely in images and music, with no one saying a word. The ending's title card asks the question the whole film was answering.",
        "Koyaanisqatsi (1982) — the city montage and the ending",
        ["Documentary", "1980s", "Avant-garde"],
    ),
    "film-paris-texas-1984-178": _entry(
        "Wim Wenders",
        "Wenders's road film — a man who has been silent for four years walks out of the desert and slowly rebuilds the story of the family he abandoned — won the Palme d'Or and made Harry Dean Stanton an unlikely star. Its final scene, a mother and son speaking through a one-way mirror, is one of the great endings in cinema.",
        "Watch the opening — the man in the desert, the buzzard, the first human contact in four years — and notice how Wenders films America: the film is a road movie in reverse, a man coming home, and the desert cinematography by Robby Müller is the film's first language. Then watch the final scene in the peep-show booth, where Travis tells Jane their whole story through a phone and a mirror: the film's argument — that we can only face our past through a screen — is dramatized in that booth, and the ending's choice, letting the son go with the mother, is the film's act of grace.",
        "Paris, Texas (1984) — the peep-show confession and the ending",
        ["Drama", "1980s", "German"],
    ),
    "film-blue-velvet-1986-179": _entry(
        "David Lynch",
        "Lynch's film — a college student who finds a severed ear and descends into a small town's underworld of violence and perversion — became the defining statement of 1980s cinema's dark side, and Isabella Rossellini's performance as the singer Dorothy Vallens divided audiences and critics. Its famous line — 'It's a strange world' — is the film's whole thesis.",
        "Watch the opening — the white picket fence, the red roses, the slow-motion fire and the severed ear — and notice how Lynch establishes the two worlds: the film is an excavation, and the camera's descent into the grass after the ear is the film's method. Then watch the scenes between Jeffrey and Frank Booth, where Dennis Hopper's performance ('Heineken? F*** that s***! Pabst Blue Ribbon!') becomes the film's id: the film's argument — that the American dream is a mask over a scream — is dramatized in every descent, and the ending's robin, singing at last, is the most contested happy ending in cinema.",
        "Blue Velvet (1986) — the ear in the grass and the descent",
        ["Thriller", "1980s", "Hollywood"],
    ),
    "film-akira-1988-180": _entry(
        "Katsuhiro Otomo",
        "Otomo's animated epic — a biker gang in Neo-Tokyo, a government experiment, and a psychic child named Akira — was the most expensive animated film ever made at the time and single-handedly introduced Japanese animation to the West. Its 150,000 hand-painted cels and its apocalyptic finale made it the reference point for cyberpunk anime for three decades.",
        "Watch the opening — the bike chase through Neo-Tokyo, Kaneda's red bike, the police blockade — and notice how Otomo establishes the world: the film's animation is so detailed that the backgrounds alone took years, and the chase is the film's thesis in motion. Then watch the finale, where Tetsuo's transformation into a cosmic entity levels the city: the film's argument — that power without control is annihilation, and that the state's 'progress' is built on children — is dramatized in that destruction, and the ending's 'Akira' — the name repeated as the city is reborn — is the film's ambiguous hope.",
        "Akira (1988) — the opening bike chase and the finale",
        ["Animation", "1980s", "Japanese"],
    ),
    "film-the-piano-1993-181": _entry(
        "Jane Campion",
        "Campion's film — a mute pianist sent to New Zealand with her piano and her daughter, who is bartered to a settler and falls in love with the man who holds her piano — won the Palme d'Or, making Campion the first woman to win it, plus three Oscars including Best Actress for Holly Hunter. Its central image — Ada's piano stranded on a beach — is one of cinema's great openings.",
        "Watch the opening — the piano on the beach, Ada's voiceover, her refusal to speak — and notice how Campion establishes Ada's world: the piano is her voice, and the film's first act is a portrait of a woman whose silence is a choice. Then watch the scene where Baines offers her the piano back, key by key, for intimacy: the film's argument — that desire is a negotiation, and that Ada's silence is her power — is dramatized in that bargain. The ending, where Ada chooses the piano over the ocean, is the film's final act of self-possession.",
        "The Piano (1993) — the beach opening and the bargain",
        ["Romance", "1990s", "New Zealand"],
    ),
    "film-before-sunrise-1995-182": _entry(
        "Richard Linklater",
        "Linklater's film — an American and a French woman meet on a train, spend one night walking through Vienna, and agree to meet again in six months — was shot in real time across one evening, and its two leads, Ethan Hawke and Julie Delpy, co-wrote much of their dialogue. It launched a trilogy that followed the same couple at nine-year intervals for eighteen years.",
        "Watch the opening — the train, the German couple's argument, the first exchange over a book — and notice how Linklater builds the film from conversation: the plot is walking and talking, and every stop in Vienna is a stage. Then watch the scene on the carousel, or the palm-reading in the alley, where the film's theme — that two strangers can become each other's whole world in a night — is dramatized: the film's argument is that love is made of attention, and the ending's montage of the places they walked, now empty, is the film's quiet heartbreak.",
        "Before Sunrise (1995) — the Vienna walk and the ending",
        ["Romance", "1990s", "Hollywood"],
    ),
    "film-titanic-1997-183": _entry(
        "James Cameron",
        "Cameron's film — a first-class Rose and a steerage Jack, and the ship that sank in 1912 — became the first film to gross a billion dollars and won 11 Oscars including Best Picture, and its 'I'm flying' scene became the most imitated image in cinema. It was the most expensive film ever made at the time, and its obsession with the real wreck, filmed 12,000 feet down, is the film's other story.",
        "Watch the opening — the present-day expedition, the salvage of the safe, the drawing of the naked Rose — and notice how Cameron structures the film as a frame story: the whole romance is a memory, and the film's budget went to making that memory feel physical. Then watch the sinking, the film's 80-minute set piece of engineering and hubris: the film's argument — that the ship is a class system made of steel, and that its doom is a metaphor for the world that built it — is dramatized in the water, and the ending, with the passengers greeting Rose in death, is the film's most sentimental and most sincere scene.",
        "Titanic (1997) — the sinking sequence",
        ["Romance", "1990s", "Hollywood"],
    ),
    "film-the-matrix-1999-184": _entry(
        "Lana & Lilly Wachowski",
        "The Wachowskis' film — a hacker who learns the world is a simulation and that he is 'the One' — won four Oscars for its craft and became a cultural landmark, from its bullet-time effects to its red-pill/blue-pill vocabulary. It was the first film to make computer-generated action feel physical, and its philosophical question — what is real? — proved more influential than its sequels.",
        "Watch the opening — the green code, Trinity's phone call, the impossible jump — and notice how the Wachowskis establish the film's method: every action sequence is a question about perception, and the first 'what is the Matrix?' is answered by Morpheus's speech in the desert. Then watch the lobby shootout and the subway fight, where the film's physics are renegotiated in real time: the film's argument — that reality is a choice, and that the system runs on consent — is dramatized in the red pill and the white rabbit, and the ending's flight, with Neo's 'I know kung fu' smile, is the film's liberation.",
        "The Matrix (1999) — the lobby scene and the reveal",
        ["Sci-Fi", "1990s", "Hollywood"],
    ),
    "film-in-the-mood-for-185": _entry(
        "Wong Kar-wai",
        "Wong's film — two neighbors in 1960s Hong Kong who discover their spouses are having an affair, and who fall in love without touching — was voted the greatest film of the 21st century by critics, and its images — the narrow staircase, the red curtains, the cigarette smoke — are among the most beautiful ever filmed. Its use of the song 'Yumeji's Theme' over slow-motion meetings became the film's signature.",
        "Watch the opening — the two families moving in on the same day, the first glance through the door — and notice how Wong films restraint: the camera is always slightly too close or too far, and the characters' bodies never fully meet. Then watch the rehearsal scenes, where Mr. Chow and Mrs. Chan practice being the spouses who wronged them: the film's argument — that love is a matter of timing and weather, and that duty can outweigh desire — is dramatized in every 'what would you do?' And the ending, where Chow whispers into a hole in a wall at Angkor Wat, is the film's final act of unspoken longing.",
        "In the Mood for Love (2000) — the rehearsals and the ending",
        ["Romance", "2000s", "Hong Kong"],
    ),
    "film-mulholland-drive-2001-186": _entry(
        "David Lynch",
        "Lynch's film — a woman with amnesia, an aspiring actress, and a Hollywood dream that curdles into nightmare — began as a TV pilot that was rejected, then was reshot and re-edited into a feature that won the Palme d'Or and became one of the most analyzed films ever made. Its 'Silencio' club scene is the film's thesis in performance.",
        "Watch the opening — the limousine, the accident, the woman who walks away from the wreck with a purse of cash — and notice how Lynch withholds meaning: the film's first half is a dream that never explains itself, and the mystery is the pleasure. Then watch the Silencio club scene, where a singer collapses and the recording continues: the film's argument — that Hollywood is a machine for manufacturing illusions, and that the dreamer is the illusion — is delivered in that performance. The ending, where the dream collapses into a blue box, is the film's final act of refusal: it never resolves, and that's the point.",
        "Mulholland Drive (2001) — the Silencio scene and the ending",
        ["Mystery", "2000s", "Hollywood"],
    ),
    "film-lost-in-translation-2003-187": _entry(
        "Sofia Coppola",
        "Coppola's film — two lonely Americans in a Tokyo hotel, a fading actor and a recent bride, who find each other in the small hours — was shot in 27 days and won the Oscar for Best Original Screenplay, making Coppola the third woman to be nominated for Best Director. Its ending, a whispered goodbye in a crowded street, is one of the most imitated final scenes in cinema.",
        "Watch the opening — the taxi ride through neon Tokyo, the jet lag, the first glimpse of Bob's hotel window — and notice how Coppola films loneliness: the film's Tokyo is a city of signs Bob can't read, and the silence is the dialogue. Then watch the karaoke night, where the two strangers finally relax into each other: the film's argument — that intimacy can be a season, not a lifetime — is dramatized in every late-night scene, and the ending, with Bob's whispered words we never hear, is the film's final act of privacy.",
        "Lost in Translation (2003) — the karaoke night and the ending",
        ["Romance", "2000s", "Hollywood"],
    ),
    "film-brokeback-mountain-2005-188": _entry(
        "Ang Lee",
        "Lee's film — two cowboys whose one summer in the Wyoming mountains becomes a twenty-year secret — won three Oscars including Best Director, and its story of Ennis and Jack changed how Hollywood told queer stories. Its famous line — 'I wish I knew how to quit you' — became shorthand for the film's refusal to let its characters be simple.",
        "Watch the opening — the sheep, the first campfire, the tent — and notice how Lee films the affair: the film's landscapes are as important as its faces, and the love story is told in glances across a valley. Then watch the reunion scene, where Ennis and Jack kiss in the stairwell of a motel: the film's argument — that love can survive in secrecy and destroy in silence — is dramatized in that embrace and the years that follow, and the ending, with Ennis holding the shirts in the closet, is the film's final act of grief.",
        "Brokeback Mountain (2005) — the reunion and the ending",
        ["Romance", "2000s", "Hollywood"],
    ),
    "film-children-of-men-2006-189": _entry(
        "Alfonso Cuarón",
        "Cuarón's film — set in 2027, when humanity has been infertile for 18 years and a pregnant woman appears — was shot in a London that looks like a war zone, with several action sequences filmed in single unbroken takes. Its central argument — that hope is the last political act — made it the definitive dystopia of its decade.",
        "Watch the opening — the café, the news report, the explosion — and notice how Cuarón establishes the world: the film's future is the present run on, and every set piece is a descent into a world that has stopped caring. Then watch the single-take battle sequence in the refugee camp, where Theo carries Kee through gunfire: the film's argument — that the future belongs to the ones who protect the vulnerable, and that despair is the real enemy — is dramatized in that walk. The ending, with the 'Ark' ship arriving, is the film's small, fragile hope.",
        "Children of Men (2006) — the single-take battle sequence",
        ["Sci-Fi", "2000s", "Hollywood"],
    ),
    "film-a-serious-man-2009-190": _entry(
        "Joel & Ethan Coen",
        "The Coens' film — a physics professor in 1967 Minnesota whose life collapses in a series of Job-like trials, from his wife's affair to a tenure fight — is their most personal film, drawn from their own Jewish upbringing in a Minneapolis suburb. Its opening parable, set in a shtetl in an indeterminate past, sets the film's whole argument in three minutes.",
        "Watch the opening — the dybbuk, the visitor, the man stabbed in the chest who walks in and says 'help me' — and notice how the Coens set the frame: the parable is the film's thesis, and everything that follows is a modern retelling. Then watch Larry's descent, from the parking-lot dispute with his neighbor to the rabbi who offers no answers: the film's argument — that Job's comforters are everywhere, and that God's silence is the premise, not the problem — is dramatized in every failed appeal, and the ending's tornado is the film's final refusal to explain.",
        "A Serious Man (2009) — the opening parable and the ending",
        ["Comedy", "2000s", "Hollywood"],
    ),
    "film-inception-2010-191": _entry(
        "Christopher Nolan",
        "Nolan's film — a thief who enters people's dreams to steal ideas, hired for the reverse: planting one — was a $160 million puzzle box that grossed $830 million and won four Oscars, and its rotating-corridor fight became one of the great images of 2010s cinema. Its final shot, a spinning top that may or may not fall, launched a decade of debate.",
        "Watch the opening — the beach, the old man, the top — and notice how Nolan establishes the film's rules: the film is a heist movie about architecture, and every level of the dream is a new scale of time. Then watch the corridor fight, where the physics of the dream are renegotiated mid-scene: the film's argument — that an idea can be the most dangerous weapon, and that reality is a matter of faith — is dramatized in the three-level climax. The ending, with the top wobbling as the camera pulls away, is the film's final question: does Cobb care if it's real?",
        "Inception (2010) — the corridor fight and the ending",
        ["Sci-Fi", "2010s", "Hollywood"],
    ),
    "film-a-separation-2011-192": _entry(
        "Asghar Farhadi",
        "Farhadi's film — an Iranian couple's divorce, a caregiver's fall, and a legal case that exposes every character's compromise — won the Oscar for Best Foreign Language Film and became the most honored Iranian film ever made. Its genius is that every character is both right and wrong, and the audience is the jury.",
        "Watch the opening — the couple's divorce petition, the judge's office, the first exchange about what each wants — and notice how Farhadi builds the film from a single decision: the divorce is the engine, and every scene is a new piece of the case. Then watch the trial scenes, where the caregiver's claim is tested against the daughter's testimony: the film's argument — that justice in a tangled world is impossible, and that every version of the truth is a partial lie — is dramatized in each witness, and the ending, with the daughter forced to choose between her parents, is the film's final verdict: there is no right answer.",
        "A Separation (2011) — the trial scenes and the ending",
        ["Drama", "2010s", "Iranian"],
    ),
    "film-amour-2012-193": _entry(
        "Michael Haneke",
        "Haneke's film — an elderly Parisian couple, and the wife's stroke that turns their love into a slow, unbearable caregiving — won the Palme d'Or and the Oscar for Best Foreign Language Film, and Emmanuelle Riva became the oldest Best Actress nominee in history. Its title is French for 'love,' and its final act is the hardest examination of what that word means.",
        "Watch the opening — the locked door, the police, the body laid out in the apartment — and notice how Haneke starts with the ending: the film is a flashback to the ordinary life that preceded the tragedy, and the camera's calm is the horror. Then watch Georges's care of Anne, from the hospital to the refusal to let her daughter take her away: the film's argument — that love is not a feeling but a series of decisions, including the final one — is dramatized in every bed bath and every refusal. The ending, with the pigeon and the letter, is the film's quiet, devastating act of mercy.",
        "Amour (2012) — the opening and the final act",
        ["Drama", "2010s", "French"],
    ),
    "film-gravity-2013-194": _entry(
        "Alfonso Cuarón",
        "Cuarón's film — an astronaut stranded in orbit after her shuttle is destroyed — was shot as one long, floating ballet of survival, and won seven Oscars for its craft, with the 13-minute single-take opening becoming the film's signature. It grossed $700 million and made space look, for the first time, both beautiful and lethal.",
        "Watch the opening — the shuttle's silent ballet, the music, the first sight of the debris cloud — and notice how Cuarón establishes the film's physics: there is no sound in space, and the film's silence is its terror. Then watch the tether sequence, where Ryan floats away from the wreck into the blackness: the film's argument — that survival is a matter of will and breath, and that isolation is the real gravity — is dramatized in every small decision, and the ending, with Ryan crawling onto the shore, is the film's most primal image: rebirth.",
        "Gravity (2013) — the opening and the re-entry sequence",
        ["Sci-Fi", "2010s", "Hollywood"],
    ),
    "film-whiplash-2014-195": _entry(
        "Damien Chazelle",
        "Chazelle's film — a young drummer and the tyrannical jazz teacher who abuses him into greatness — won three Oscars including Best Supporting Actor for J.K. Simmons, and its final drum solo, filmed in real time, became one of the great climaxes in cinema. Its central question — is greatness worth the damage? — is never answered.",
        "Watch the opening — the practice room, the first confrontation, the chair thrown at Andrew's head — and notice how Chazelle establishes the film's stakes: the film is about perfection as a form of abuse, and every scene is a negotiation of that abuse. Then watch the final performance, where Andrew hijacks the stage and Fletcher conducts him into a masterpiece: the film's argument — that the relationship between mentor and student is a battle for the soul, and that the applause is the only proof — is dramatized in that solo, and the ending's final glance between them is the film's whole argument in one look.",
        "Whiplash (2014) — the final performance",
        ["Drama", "2010s", "Hollywood"],
    ),
    "film-get-out-2017-196": _entry(
        "Jordan Peele",
        "Peele's debut — a Black photographer meeting his white girlfriend's family, who turn out to be operating a sinister scheme — became the most profitable film of 2017 and the definitive 'social thriller,' winning Peele the Oscar for Best Original Screenplay. Its 'sunken place' became an instant cultural metaphor.",
        "Watch the opening — the suburban street at night, the abduction, the deer — and notice how Peele establishes the film's grammar: every polite gesture in the film is a threat, and the film's horror is in the compliments. Then watch the hypnotherapy scene, where the 'sunken place' is introduced: the film's argument — that racism in liberal form is a kind of possession, and that Blackness is being consumed by a culture that wants its labor and its body — is dramatized in the 'gift shop' scene and the teacup. The ending, with Rod's 'I told you not to go in that house,' is the film's dark comedy breaking through.",
        "Get Out (2017) — the sunken place and the ending",
        ["Horror", "2010s", "Hollywood"],
    ),
    "film-dune-2021-197": _entry(
        "Denis Villeneuve",
        "Villeneuve's adaptation of Frank Herbert's novel — the first of two parts — brought the desert planet Arrakis to the screen with a scale no previous version managed, winning six Oscars and grossing $400 million in a pandemic year. Its 'voice' sequences and sandworm rides made it the defining epic of the decade.",
        "Watch the opening — the voiceover, the desert, the first sight of the sandworm — and notice how Villeneuve establishes Arrakis: the film is about ecology and prophecy, and every scene of the desert is a lesson in Herbert's world-building. Then watch the arrival at Arrakeen and the betrayal that follows, where House Atreides is destroyed by a conspiracy: the film's argument — that the spice is power, and that the prophecy is a weapon of colonization — is dramatized in the emperor's scheme, and the ending, with Paul and Jessica walking into the desert, is the film's promise of the war to come.",
        "Dune (2021) — the sandworm sequences",
        ["Sci-Fi", "2020s", "Hollywood"],
    ),
    "film-the-power-of-the-198": _entry(
        "Jane Campion",
        "Campion's film — a rancher in 1925 Montana who torments his brother's new wife and forms a twisted bond with her son — won the Oscar for Best Director, making Campion the third woman and first New Zealander to win, and was nominated for 12 Oscars overall. Its twist, withheld for two hours, reframes the entire film.",
        "Watch the opening — the cattle drive, the rope, Phil's cruelty to the new wife — and notice how Campion films the western: the film's landscapes are psychological, and Phil's rage is the film's engine. Then watch the film's final act, where the true relationship between Phil and his dead mentor Bronco Henry is revealed: the film's argument — that masculinity is a performance held up by secrecy, and that the closeted man is the most dangerous — is dramatized in every scene of Phil's posturing, and the ending, with the son's quiet act of sabotage, is the film's final, devastating turn.",
        "The Power of the Dog (2021) — the ending reveal",
        ["Western", "2020s", "Hollywood"],
    ),
    "film-tár-2022-199": _entry(
        "Todd Field",
        "Field's film — the fictional conductor Lydia Tár, the first woman to lead the Berlin Philharmonic, whose past abuses catch up with her — was Cate Blanchett's most celebrated performance, earning her the Venice Volpi Cup and an Oscar nomination. Its central question — what do we owe the artists whose work we love, if they are monsters? — is never resolved.",
        "Watch the opening — the New Yorker-style interview, the Juilliard class, the conductor's godlike confidence — and notice how Field builds the film from Tár's control: the film is a portrait of power, and every scene is a scene of Tár managing perception. Then watch the film's slow unraveling, from the apartment scene with the neighbor's daughter to the 'Apartment' book that exposes her: the film's argument — that genius is a system of privilege, and that the music is not a defense — is dramatized in Tár's rehearsals and her fall, and the ending, conducting a video-game score in the Philippines, is the film's final, ambiguous exile.",
        "Tár (2022) — the Juilliard scene and the unraveling",
        ["Drama", "2020s", "Hollywood"],
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
