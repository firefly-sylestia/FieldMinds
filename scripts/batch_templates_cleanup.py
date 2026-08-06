#!/usr/bin/env python3
"""Batch: replace 20 leftover template entries (duplicated film teasers +
template books/discoveries/wildcard) with handcrafted descriptions.

- films.json: 12 entries whose teasers are identical template sentences
  ("The most memorable scene was improvised on set...", "This film was made
  for roughly the cost of a modest house...") with scrambled tags.
- books.json: 2 (Godfather, Fear and Loathing) with the "6-week creative
  burst" template family + scrambled tags.
- discoveries.json: 2 (CMB, Exoplanets) template family + scrambled tags.
- wildcard.json: 4 (Ikebana, Wabi-Sabi, Lagom, Sisu) "What makes this so
  fascinating is that it shouldn't exist" template family.

Replaces teaser + instruction + targetName + tags; preserves id/name/subtype/
verb/durationMinutes/byline/tier. Cap 450 (SCHEMA.md).
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


def _entry(teaser: str, instruction: str, target_name: str, tags: list[str]) -> dict:
    return {
        "teaser": teaser,
        "instruction": instruction,
        "targetName": target_name,
        "tags": tags,
    }


FIXES: dict[str, dict] = {
    # ---------- books.json ----------
    "book-the-godfather-1969-145": _entry(
        "Mario Puzo was 45, broke, and drowning in gambling debt when he wrote it — 'I was 45 years old and tired of being an artist.' He sold the movie rights for $12,500 before the book even hit shelves, and the novel spent 67 weeks on the bestseller list while the film he'd half-joked about became the greatest in Hollywood history.",
        "Read the opening wedding scene. Puzo's trick is that the violence happens off-screen — you hear about the horse's head and Sonny's beating through dialogue. Notice how the book's first chapter establishes the family's rule ('never go against the family') through ceremony, not shooting. Then skip to the baptism scene at the end and compare the ritual bookending: the novel is a family saga disguised as a crime novel, and the crime is the family's business, not its point.",
        "The Godfather (1969) — the opening wedding and the baptism finale",
        ["Classic", "Crime", "20th Century"],
    ),
    "book-fear-and-loathing-in-146": _entry(
        "Hunter S. Thompson wrote it in a rented room in 1971 with a typewriter, a bottle of Chivas, and a note taped to the wall: 'Never again.' It ran in Rolling Stone as a 40,000-word magazine piece — about a drug-fueled trip to Las Vegas that was actually an assignment to cover a motorcycle race — and invented 'gonzo journalism' where the reporter is the story.",
        "Read the opening — 'We were somewhere around Barstow on the edge of the desert when the drugs began to take hold.' Thompson builds the whole book on that one sentence's rhythm: a journalist's matter-of-fact tone colliding with the surreal. Notice how he treats Las Vegas as a symbol — the 'American Dream' as a casino that always wins. The book's actual event, the Mint 400 race, barely appears; the story is the hallucination, and that's the point.",
        "Fear and Loathing in Las Vegas (1971) — the opening and the casino scenes",
        ["Classic", "Gonzo", "20th Century"],
    ),
    # ---------- discoveries.json ----------
    "disc-the-cosmic-microwave-background-163": _entry(
        "In 1964, two Bell Labs engineers pointed a horn antenna at the sky to test satellite communications and heard a hiss that wouldn't go away — even after they cleaned pigeon droppings out of the horn. The hiss was the afterglow of the Big Bang, cooling for 13.8 billion years, and it had been predicted by theory just 16 years earlier.",
        "Read how the discovery worked: the universe's first light, released 380,000 years after the Big Bang, has stretched from white-hot to microwave wavelengths by the expansion of space — about 3 degrees above absolute zero. Penzias and Wilson had no idea what the hiss was; they spent months eliminating every earthly source, and only learned it was the 'primordial fireball' when a Princeton physicist heard about their problem. Watch the sky map from the Planck satellite: the tiny temperature ripples are the seeds of every galaxy that ever formed.",
        "The cosmic microwave background — Penzias & Wilson's 1964 hiss",
        ["Cosmology", "Big Bang", "20th Century"],
    ),
    "disc-exoplanets-1995-164": _entry(
        "For centuries astronomers assumed other stars had planets, but no one could see one — then in 1995 two Swiss astronomers found a planet orbiting the star 51 Pegasi by watching the star wobble. The planet was a 'hot Jupiter,' a gas giant orbiting closer than Mercury, and it rewrote every theory of how solar systems form.",
        "Understand the method first: a planet's gravity tugs its star, and that wobble shifts the star's light slightly red then blue — the radial velocity method. Mayor and Queloz detected a 4-day wobble in 51 Pegasi, meaning a planet 150 times Earth's mass orbiting far too close to its star to exist by the old rules. Then think about the numbers: as of today over 5,000 exoplanets are confirmed, and the Kepler mission alone found thousands by watching stars dim as planets crossed in front. The first exoplanet discovery won the 2019 Nobel Prize in Physics.",
        "51 Pegasi b — the first confirmed exoplanet (1995)",
        ["Astronomy", "Exoplanets", "20th Century"],
    ),
    # ---------- films.json ----------
    "film-singin-in-the-rain-165": _entry(
        "The most expensive musical of its time and the greatest film about Hollywood ever made — a satire of the painful switch from silent films to talkies, built around the real crisis that wrecked dozens of silent stars whose voices didn't match their faces. Gene Kelly's title dance was shot in one long take with a fever of 103°F.",
        "Watch the 'Make 'em Laugh' number — it's a love letter to physical comedy, with Donald O'Connor doing stunts that sent him to the hospital. Then the title song: Kelly dances in the rain for real, on a flooded set, and the joy is earned because the character has just decided to be himself. The film's secret weapon is the sound-within-sound gag — the new 'talkie' where the sound is out of sync — which is still the sharpest joke about Hollywood's relationship with technology.",
        "Singin' in the Rain (1952) — the title number and 'Make 'em Laugh'",
        ["Musical", "1950s", "Hollywood"],
    ),
    "film-the-seventh-seal-1957-166": _entry(
        "Bergman shot it in 35 days on a tiny budget, casting himself as the knight's silent squire, and it became the film that defined existential cinema: a medieval knight plays chess with Death to buy time for one meaningful act. The final dance of death on the hilltop, shot at the edge of the Baltic Sea, was improvised with real actors in a real fog.",
        "Watch the opening — the knight on the beach, the chess game with the hooded figure — and notice that Bergman doesn't make Death evil: it's calm, patient, and honest. The film is a medieval setting for a modern question — if God is silent, is life meaningless? — and Bergman answers with the small acts the knight performs: sharing a meal with a family of players, letting the girl escape. The ending's silhouetted dance on the hill is the film's whole argument in one image: death is certain, but how we face it is chosen.",
        "The Seventh Seal (1957) — the chess opening and the dance of death",
        ["Classic", "1950s", "Swedish"],
    ),
    "film-psycho-1960-167": _entry(
        "Hitchcock made it on his TV crew's schedule with a $800,000 budget — the first 'slasher' — and rewrote the rules of cinema: he killed his biggest star 45 minutes in, and forced theaters to bar latecomers so no one spoiled it. The 'shower scene' took 7 days to shoot, used 70 camera setups, and contains no visible stab wounds — only editing.",
        "Watch the shower scene frame by frame: there is no shot of the knife entering Marion — Hitchcock builds the violence from quick cuts of knife, body, water, and the famous screeching strings (played by violins mimicking screams). Then watch the 'psycho' reveal scene with the psychiatrist explaining Norman's condition: the film's ending is a lecture that raises more questions than it answers, which is why the film is called a masterpiece of manipulation. Notice how Hitchcock makes you complicit — you root for Marion to escape the stolen money, then you watch her die anyway.",
        "Psycho (1960) — the shower scene and the reveal",
        ["Thriller", "1960s", "Hollywood"],
    ),
    "film-lawrence-of-arabia-1962-168": _entry(
        "David Lean's 227-minute epic was shot in the desert with a 34-year-old stage actor named Peter O'Toole in his first starring role, and its 'sunrise over the desert' cut — from match flame to desert sun — is one of the most famous transitions in film. It won seven Oscars, and its theme by Maurice Jarre is the most recognized film score ever written.",
        "Watch the first 15 minutes: the map sequence — Lawrence wiping his hand across the desert and declaring 'sweet to ride forth at evening from the wells' — establishes the film's method, where landscape is character. Then the crossing of the Nefud desert, where the film's scale makes the actors tiny specks: Lean shot real dunes in real heat, and the 'mirage' effect was real. The film's tragedy is that Lawrence's triumph — uniting the Arab tribes — is used by the British for their own empire, and the ending's return to the desert is the point.",
        "Lawrence of Arabia (1962) — the match cut and the desert crossing",
        ["Epic", "1960s", "Hollywood"],
    ),
    "film-the-good-the-bad-169": _entry(
        "Leone's third and greatest 'spaghetti western' — an 8-month shoot in the Spanish desert with three actors who barely spoke each other's languages, climaxing in the famous three-way standoff that lasts over three minutes of pure silence. Clint Eastwood's poncho and Ennio Morricone's howling score became the genre's visual and sonic signatures.",
        "Watch the opening credits: the three title characters each get their own theme — the flute, the ocarina, the coyote howl — and Morricone's score is the film's fourth character. Then watch the ending standoff in the circular cemetery: Leone holds the shot for over three minutes with no dialogue, only the music shifting between the three themes, and the duel's winner is decided by who's willing to wait. The film's famous 'The Ecstasy of Gold' cue — the dying man's race to the grave — is the sequence Tarantino and every action director since has tried to copy.",
        "The Good, the Bad and the Ugly (1966) — the final standoff",
        ["Western", "1960s", "Spaghetti"],
    ),
    "film-once-upon-a-time-170": _entry(
        "Leone called it his most personal film and spent two years on the script, and its 14-minute opening — three gunmen waiting at a train station while a harmonic plays — is the slowest, greatest action scene ever filmed. It flopped in America (studio cut 20 minutes) before being restored to become a masterpiece.",
        "Watch the entire 14-minute opening: the windmill, the creaking sign, the three hired killers arriving on the train — the film's title doesn't even appear until the station sequence ends. Leone's method is delay: he withholds the violence so completely that when it comes — the massacre of the McBain family — it's shocking. Then watch the 'Man with a Harmonica' theme build through the film: the harmonic is the mystery of the stranger's past, and its resolution in the final flashback is the film's emotional payoff.",
        "Once Upon a Time in the West (1968) — the opening sequence",
        ["Western", "1960s", "Spaghetti"],
    ),
    "film-a-clockwork-orange-1971-171": _entry(
        "Kubrick's adaptation of Anthony Burgess's novel was banned in Britain by Kubrick himself after death threats — he pulled it from distribution for 27 years until after his death. Its infamous 'Ludovico technique' scene, where the violent Alex is conditioned against violence, was filmed with real eye-clamps and a real nausea-inducing setup.",
        "Watch the opening — the close-up of Alex's eye, the zoom back to the milk bar — and notice Kubrick's method: he films ultraviolence with classical music (Beethoven's Ninth, 'Singin' in the Rain') to make the audience feel the pleasure of it, then makes them watch the 'cure.' The film's question is whether conditioning a person against evil is worse than letting them choose it — the movie's answer is deliberately uncomfortable, and the ending's eye-to-camera stare is Kubrick's refusal to resolve it.",
        "A Clockwork Orange (1971) — the Ludovico technique scene",
        ["Dystopian", "1970s", "Hollywood"],
    ),
    "film-chinatown-1974-172": _entry(
        "Roman Polanski's neo-noir is famous for its ending — a 'happy ending' would have meant a just world, and screenwriter Robert Towne refused. Jack Nicholson improvised the line that gave the film its name, and the film's plot is a fictionalized retelling of the real California water wars that made Los Angeles possible.",
        "Watch the opening — the private detective photo session, the case that seems simple — and notice how the film deepens: what starts as a cheating-husband case becomes a story of water, incest, and the city's founding sin. Then the ending, where the truth is spoken and the city looks away: Chinatown, the place where 'you can't always tell what's happening,' is the film's metaphor for the whole world of power. The film's most famous line — 'Forget it, Jake, it's Chinatown' — is the final admission that justice was never the point.",
        "Chinatown (1974) — the ending",
        ["Noir", "1970s", "Hollywood"],
    ),
    "film-oppenheimer-2023-173": _entry(
        "Christopher Nolan's three-hour biopic was made from 500 pages of a Pulitzer-winning book and shot with an entirely practical approach — no CGI for the Trinity test, which used real explosions and a 10-mile camera setup. It grossed nearly a billion dollars and became the highest-grossing biopic in history.",
        "Watch the opening — the rain, the eyes, the hearing room — and notice how Nolan splits the film into two colors: black-and-white for the political hearing (Strauss's perspective) and color for Oppenheimer's subjective experience. The Trinity test sequence is the film's center: shot without digital effects, the silence before the blast and the roar that follows are the film's argument that this was a moment humanity couldn't unsee. The ending — the repeated line about destroying the world — is Oppenheimer's guilt made literal, and it's the question the film leaves you with.",
        "Oppenheimer (2023) — the Trinity test sequence",
        ["Drama", "2020s", "Hollywood"],
    ),
    "film-killers-of-the-flower-174": _entry(
        "Martin Scorsese spent 15 years developing it and filmed on the actual Oklahoma land where the Osage murders happened, with Osage elders and descendants on set as consultants. Its three-and-a-half-hour length is the point: the film refuses to speed through a genocide that took decades.",
        "Watch the opening — the Osage people emerging from the earth, the oil derricks — and notice how Scorsese reframes the genre: this isn't a detective story about who solved the case, but a story about how the Osage were made to doubt themselves. The film's central performance choice is Leonardo DiCaprio's Ernest, who is both complicit and loving — the film's argument is that the killers were not monsters but ordinary men, and that's more frightening. The ending's radio-play coda, where the history is spoken plainly, is Scorsese's refusal to let the audience feel comfortable.",
        "Killers of the Flower Moon (2023) — the ending coda",
        ["Drama", "2020s", "Hollywood"],
    ),
    "film-poor-things-2023-175": _entry(
        "Yorgos Lanthimos's feminist Frankenstein — a woman with a baby's brain in an adult body, played by Emma Stone, who wins the Oscar — was shot largely on location with in-camera effects and hand-built sets, and its black-and-white opening gives way to saturated color as its heroine gains experience.",
        "Watch the opening — the blue-tinted Victorian world, Bella's first steps, the childish speech in an adult body — and notice how the film maps knowledge onto color: the world darkens as Bella sees more of it. The film's method is to treat Bella's journey as pure curiosity: she tries sex, philosophy, poverty, and anarchy with the same wide-eyed interest, and the film's argument is that experience, not innocence, is what makes a person free. The ending, where Bella takes charge of the society she's seen through, completes the arc from experiment to author.",
        "Poor Things (2023) — the opening and Bella's journey",
        ["Comedy", "2020s", "Hollywood"],
    ),
    "film-anatomy-of-a-fall-176": _entry(
        "Justine Triet's courtroom drama — a woman on trial for her husband's death — won the Palme d'Or and became the most honored French film in years, and its central performance by Sandra Hüller is built on the film's refusal to tell you the truth. The 'fight scene' — a 12-minute audio recording played in court — is the film's entire argument.",
        "Watch the trial scenes with attention to what the film refuses: no flashbacks to the fall, no confession, no truth — only testimony, interpretation, and the couple's own recorded fight, which the court hears as evidence while the audience hears it as a marriage. The film's question is whether a marriage can be judged from the outside at all, and the answer is both yes and no. The ending, where the verdict is announced but the doubt remains, is the film's final refusal to resolve.",
        "Anatomy of a Fall (2023) — the courtroom scenes",
        ["Drama", "2020s", "French"],
    ),
    # ---------- wildcard.json ----------
    "wild-ikebana-190": _entry(
        "Ikebana — the Japanese art of arranging flowers — is closer to sculpture than decoration: the rules dictate the angles of stems (heaven, earth, and man), and a single branch can be a complete composition. The oldest school, Ikenobō, traces itself back 550 years to a Kyoto temple priest.",
        "The key difference from Western bouquets: ikebana is about empty space as much as flowers — the stems create lines that the eye travels, and the arrangement is meant to be viewed from the front, like a painting. Try a simple exercise: take three stems and arrange them at 0°, 40°, and 75° from vertical, then photograph the result. The art's philosophy comes from Zen: arranging flowers is a meditation where the goal is restraint, not abundance.",
        "Try arranging 3 stems at 0°/40°/75° — heaven, earth, man",
        ["Practice", "Japanese", "Art"],
    ),
    "wild-wabi-sabi-191": _entry(
        "Wabi-sabi is the Japanese aesthetic of imperfection — the cracked tea bowl repaired with gold, the faded leaf, the weathered wall — and it's the opposite of the Western ideal of flawless symmetry. The concept is deliberately hard to define: it's a feeling about time, wear, and the beauty of things that have lived.",
        "The best way to understand wabi-sabi is to find it: look for the chipped cup you still use, the weathered wooden table, the crack in the wall you've stopped noticing. Japanese tea masters prized bowls with flaws and even named their favorite cracks; the repair technique kintsugi uses gold to make the break part of the object's history rather than hide it. The philosophy's claim is that permanence is an illusion, and that accepting decay is the beginning of calm.",
        "Find 3 worn objects around you and look at them as wabi-sabi",
        ["Philosophy", "Japanese", "Aesthetic"],
    ),
    "wild-lagom-192": _entry(
        "Lagom is the Swedish word for 'just the right amount' — not too much, not too little — and it's the cultural operating system of a country that prizes moderation. The word may come from the old Norse phrase 'laget om,' meaning 'according to the law,' and Swedes use it for everything from coffee to furniture to work-life balance.",
        "Lagom is less a rule than a reflex: the Swedes' famous fika (coffee break with something sweet) is lagom applied to work — a pause that's neither stingy nor excessive. Notice how the concept shows up in design (IKEA's clean functionalism), in law (Sweden's strong workers' rights), and in the national allergy to bragging. The interesting question: does 'just right' become a pressure of its own? Try the exercise of asking yourself, for one day, what the lagom amount of anything would be.",
        "Spend a day asking 'what is the lagom amount?' of everything",
        ["Philosophy", "Swedish", "Culture"],
    ),
    "wild-sisu-193": _entry(
        "Sisu is the Finnish concept of extraordinary perseverance — the ability to keep going when all rational reasons to continue are gone. Finland's national identity is built on it: it's the word used for the winter war against the Soviet Union, for the sauna-then-ice-bath tradition, and for the athlete who collapses at the finish line.",
        "The key distinction: sisu is not stubbornness — it's a calm determination that kicks in when resources run out. Finns use it for extreme endurance feats (the 100 km ski, the ice swim) but also for everyday life: it's the quiet national answer to Nordic lagom's moderation. There's even a word for the extra reserve, 'sisukas,' meaning someone with a deep well of it. The concept became internationally known during the 1939-40 Winter War, when a small country outlasted a superpower through sheer refusal to quit.",
        "The Winter War (1939-40) — Finland's national test of sisu",
        ["Philosophy", "Finnish", "Culture"],
    ),
}


def main() -> int:
    # group by file
    from collections import defaultdict
    by_file: dict[str, dict[str, dict]] = defaultdict(dict)
    for i, fix in FIXES.items():
        if i.startswith("film-"):
            by_file["films"][i] = fix
        elif i.startswith("book-"):
            by_file["books"][i] = fix
        elif i.startswith("disc-"):
            by_file["discoveries"][i] = fix
        elif i.startswith("wild-"):
            by_file["wildcard"][i] = fix
        else:
            print(f"WARN: unknown prefix for {i}")
            return 1

    total = 0
    for fname, fixes in by_file.items():
        path = Path(__file__).resolve().parent.parent / f"app/src/main/assets/topics/{fname}.json"
        data = json.loads(path.read_text(encoding="utf-8"))
        by_id = {t["id"]: t for t in data}
        missing = [i for i in fixes if i not in by_id]
        if missing:
            print(f"ERROR [{fname}]: ids not in file: {missing}")
            return 1
        changed = 0
        for topic in data:
            fix = fixes.get(topic["id"])
            if fix is None:
                continue
            topic["teaser"] = _trim(fix["teaser"])
            topic["exploreAction"]["instruction"] = _trim(fix["instruction"])
            topic["exploreAction"]["targetName"] = fix["targetName"]
            topic["tags"] = fix["tags"]
            changed += 1
        path.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
        print(f"{fname}: updated {changed}")
        total += changed
    print(f"TOTAL updated: {total}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
