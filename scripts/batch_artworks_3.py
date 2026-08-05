#!/usr/bin/env python3
"""Batch 3: replace the last 21 fake/generic artwork descriptions with real facts.

The earlier batches (1 & 2) fixed ~35 entries. These 21 were still carrying
boilerplate teasers ("The kind of work that rewards patience...") with wrong
tags and generic instructions. Each REPLACE is a full entry replacement keyed
by id, guarded by the current name in the file so we never clobber an entry
that has since been edited elsewhere.

Some names are corrected to their official titles:
  - "Impression Sunrise (1872)"            -> "Impression, Sunrise (1872)"
  - "Nocturne in Black and Gold (1875)"    -> "Nocturne in Black and Gold: The Falling Rocket (1875)"
  - "Autumn Rhythm Number 30 (1950)"       -> "Autumn Rhythm (Number 30) (1950)"
  - "Balloon Dog Orange (2000)"            -> "Balloon Dog (Orange) (1994-2000)"
  - "Infinity Mirror Room (1965)"          -> "Infinity Mirror Room - Phalli's Field (1965)"
"""

from pathlib import Path
import json
import sys

PATH = Path(__file__).resolve().parent.parent / "app/src/main/assets/topics/artworks.json"


def _entry(
    name: str,
    byline: str,
    teaser: str,
    instruction: str,
    tags: list[str],
    duration: int,
) -> dict:
    return {
        "name": name,
        "teaser": teaser,
        "byline": byline,
        "exploreAction": {
            "verb": "Look at",
            "targetName": f"{name} by {byline}",
            "durationMinutes": duration,
            "instruction": instruction,
        },
        "tags": tags,
    }


REPLACES: dict[str, dict] = {
    "artw-the-night-watch-1642-84": {
        "expectName": "The Night Watch (1642)",
        "entry": _entry(
            "The Night Watch (1642)",
            "Rembrandt",
            (
                "Rembrandt's largest painting - four metres of a militia company striding out of shadow. "
                "It only became the 'night watch' after centuries of grimy varnish darkened the daylight "
                "scene, and in 1715 it was trimmed on every side to fit a wall, amputating two whole figures."
            ),
            (
                "Find the little girl in gold near the centre - she carries a dead chicken with claws, "
                "the badge of the militia's 'klauweniers'. The dramatic light is actually daytime, darkened "
                "by varnish. Then look for the gaps: the canvas was cut down on all four sides in 1715, "
                "and two full figures were lost forever."
            ),
            ["Dutch Golden Age", "Oil Painting"],
            7,
        ),
    },
    "artw-las-meninas-1656-by-85": {
        "expectName": "Las Meninas (1656)",
        "entry": _entry(
            "Las Meninas (1656)",
            "Diego Velázquez",
            (
                "The painting is a mirror trick: we stand exactly where the king and queen stood, while "
                "Velázquez paints us from inside the canvas. The infanta, her maids, a dwarf, a dog - and "
                "the royal couple who exist only as a smudge in the mirror on the far wall."
            ),
            (
                "You are the subject. Velázquez looks past the Infanta Margarita - at you, or at the royal "
                "couple whose reflections sit blurred in the back-wall mirror. Trace every gaze in the room: "
                "each one points somewhere different, and the whole picture is built around the space where "
                "the viewer stands."
            ),
            ["Baroque", "Oil Painting"],
            6,
        ),
    },
    "artw-the-death-of-marat-86": {
        "expectName": "The Death of Marat (1793)",
        "entry": _entry(
            "The Death of Marat (1793)",
            "Jacques-Louis David",
            (
                "Painted in the months after the assassin's knife found Marat in his bath, David turned a "
                "murder into a martyrdom - a secular Pieta for the Revolution. The 'A MARAT, David' written "
                "on the crate is signed like a tombstone."
            ),
            (
                "Read it as an altar: the pale body in the tub, the wound barely a slit, the quill still in "
                "his hand. Charlotte Corday's letter lies on the crate - she came asking for help, then struck. "
                "Notice how clean everything is: no blood, no struggle. That serenity is the point."
            ),
            ["Classical", "Oil Painting"],
            4,
        ),
    },
    "artw-the-raft-of-the-87": {
        "expectName": "The Raft of the Medusa (1819)",
        "entry": _entry(
            "The Raft of the Medusa (1819)",
            "Théodore Géricault",
            (
                "After the frigate Meduse ran aground in 1816, some 147 people were set adrift on a raft "
                "built from its timbers - thirteen days later, fifteen survived. Géricault interviewed the "
                "survivors and studied corpses in the morgue to paint this seven-metre-wide epic."
            ),
            (
                "Climb the human pyramid with your eyes: the dead and dying at the bottom, the desperate "
                "straining at the top toward a sail no bigger than a thumbnail on the horizon. Géricault "
                "lived the real story - the wreck, the raft, the fifteen who came back - then chose this "
                "one impossible moment of hope."
            ),
            ["Romanticism", "Oil Painting"],
            8,
        ),
    },
    "artw-the-great-wave-off-88": {
        "expectName": "The Great Wave off Kanagawa (1831)",
        "entry": _entry(
            "The Great Wave off Kanagawa (1831)",
            "Hokusai",
            (
                "A woodblock print barely larger than a sheet of paper - yet the clawed wave swallows the "
                "horizon while tiny fishing boats brace beneath it. Hokusai used Prussian blue, a pigment "
                "newly imported from Europe, to paint the water that made him famous."
            ),
            (
                "Notice the wave's fingers - each curl ends in a claw, and the boats are tiny beneath it. "
                "In the hollow of the wave sits Mount Fuji, calm and small, the fixed point of the whole "
                "series. Remember this is a print: inked woodblocks pulled by hand, thousands of copies, "
                "each one a little different."
            ),
            ["Ukiyo-e", "Woodblock Print"],
            5,
        ),
    },
    "artw-olympia-1863-by-édouard-89": {
        "expectName": "Olympia (1863)",
        "entry": _entry(
            "Olympia (1863)",
            "Édouard Manet",
            (
                "When it hung at the 1865 Salon, guards had to protect it from the crowd. Manet's reclining "
                "nude - modeled by Victorine Meurent - stares straight out with no goddess and no myth, just "
                "a black cat and a servant's bouquet: a real woman rendered with Renaissance composition and "
                "zero apology."
            ),
            (
                "Compare her to Titian's Venus of Urbino and the joke is everywhere: same pose, different "
                "woman - this one looks you in the eye, with a cat arching its back at her feet. She wears "
                "an orchid, a ribbon and one slipper. The maid brings flowers from an admirer; Olympia's "
                "hand answers for her."
            ),
            ["Realism", "Oil Painting"],
            5,
        ),
    },
    "artw-impression-sunrise-1872-by-90": {
        "expectName": "Impression Sunrise (1872)",
        "entry": _entry(
            "Impression, Sunrise (1872)",
            "Claude Monet",
            (
                "Painted from a hotel window over Le Havre's harbour, this hazy dawn gave a movement its "
                "name: in 1874 a critic sneered that it was only an 'impression' - and the Impressionists "
                "adopted the word as a badge of honour."
            ),
            (
                "Look at the sun: a bare orange disc with no halo, dropped straight into blue-grey water, "
                "its reflection broken into strokes of orange. The ships are ghosts of black dabs, the cranes "
                "barely sketched. Count how little detail exists - the whole harbour is a feeling at one "
                "moment of dawn."
            ),
            ["Impressionism", "Oil Painting"],
            4,
        ),
    },
    "artw-nocturne-in-black-and-91": {
        "expectName": "Nocturne in Black and Gold (1875)",
        "entry": _entry(
            "Nocturne in Black and Gold: The Falling Rocket (1875)",
            "Whistler",
            (
                "Fireworks over a London pleasure garden reduced to falling gold sparks on black. When Ruskin "
                "called it 'flinging a pot of paint in the public's face,' Whistler sued for libel - and won "
                "a single farthing, the smallest coin in the realm, after a trial that left him bankrupt."
            ),
            (
                "Follow the trail of the rocket - gold drops falling through black before they reach the "
                "water's glow. Whistler called his paintings 'nocturnes,' like music, and meant the title "
                "literally: this is a mood arranged in colour, not a picture of fireworks. That thrown pot "
                "of paint cost him his fortune."
            ),
            ["Aestheticism", "Oil Painting"],
            4,
        ),
    },
    "artw-the-starry-night-1889-92": {
        "expectName": "The Starry Night (1889)",
        "entry": _entry(
            "The Starry Night (1889)",
            "Vincent van Gogh",
            (
                "Painted from the window of his asylum room at Saint-Remy, with the village below invented "
                "from memory, this is the night sky as a churning river: the moon, the stars, and one dark "
                "cypress reaching up like a flame into the swirl."
            ),
            (
                "Find the cypress first - Van Gogh called it a flame, and it anchors the whole composition "
                "on the left. Above, the sky is one continuous movement: the moon glows, the stars spiral, "
                "and the village below was imagined, not painted from sight. He made it all in a single "
                "long day."
            ),
            ["Post-Impressionism", "Oil Painting"],
            6,
        ),
    },
    "artw-the-scream-1893-by-93": {
        "expectName": "The Scream (1893)",
        "entry": _entry(
            "The Scream (1893)",
            "Edvard Munch",
            (
                "Munch wrote down what inspired it: walking at sunset with two friends, the sky turned "
                "blood-red and 'I sensed an infinite scream passing through nature.' The figure's hands "
                "cover its ears - the scream belongs to the landscape, not to the person."
            ),
            (
                "Look at the face - hands over ears, mouth open, but the horror is in the sky: the blood-red "
                "clouds, the sick blue fjord, the wavy lines that carry the scream through the air. Two "
                "figures walk calmly in the background, unhearing. Munch made four versions; this one is "
                "painted on cardboard."
            ),
            ["Expressionism", "Mixed Media"],
            5,
        ),
    },
    "artw-les-demoiselles-davignon-1907-94": {
        "expectName": "Les Demoiselles d'Avignon (1907)",
        "entry": _entry(
            "Les Demoiselles d'Avignon (1907)",
            "Picasso",
            (
                "Five women in a Barcelona brothel, their faces a mix of Iberian sculpture and African "
                "masks - Picasso made hundreds of studies over months, then kept the painting rolled up in "
                "his studio for nine years before anyone saw it. It detonated modern art."
            ),
            (
                "Look at the two figures on the right - their faces are painted like carved masks, their "
                "bodies bent into angles no human could hold. The fruit bowl at the bottom is a still life "
                "that never resolves. Every 'wrong' thing here is deliberate: space is crushed, and the "
                "viewer is the one being looked at."
            ),
            ["Cubism", "Oil Painting"],
            7,
        ),
    },
    "artw-composition-vii-1913-by-95": {
        "expectName": "Composition VII (1913)",
        "entry": _entry(
            "Composition VII (1913)",
            "Wassily Kandinsky",
            (
                "Kandinsky called it his most complex painting, and it shows: a two-by-three-metre storm of "
                "colour and line that took three days to paint after months of studies. It is music made "
                "visible - he heard colours and saw sounds."
            ),
            (
                "Don't hunt for objects - let the colour move you: the yellow surge, the black scribbles, "
                "the floating circles. Kandinsky had synesthesia and composed like a musician; he made "
                "dozens of studies before letting this one out in three days. Find the boat hull near the "
                "lower centre - one of the few shapes that almost resolves."
            ),
            ["Abstract Art", "Oil Painting"],
            6,
        ),
    },
    "artw-fountain-1917-by-marcel-96": {
        "expectName": "Fountain (1917)",
        "entry": _entry(
            "Fountain (1917)",
            "Marcel Duchamp",
            (
                "A factory-made urinal, turned upside down, signed 'R. Mutt,' submitted to an exhibition "
                "that promised to accept everything - then rejected anyway. The original was lost, and the "
                "joke keeps being remade, still asking the question that won't go away: what makes art art?"
            ),
            (
                "The joke runs deep: the exhibition's rules promised to show everything, and Fountain was "
                "rejected for being a plumbing fixture - proof the rules were never about art. Look at the "
                "photograph Stieglitz took: the upturned urinal glows like a madonna. Duchamp chose it, so "
                "it is art. Why?"
            ),
            ["Dada", "Sculpture"],
            4,
        ),
    },
    "artw-the-persistence-of-memory-97": {
        "expectName": "The Persistence of Memory (1931)",
        "entry": _entry(
            "The Persistence of Memory (1931)",
            "Salvador Dalí",
            (
                "Dali said the melting watches came to him while eating camembert cheese in the sun. This "
                "tiny painting - smaller than a sheet of printer paper - turned soft clocks into the image "
                "of time dissolving, with ants swarming the one hard watch."
            ),
            (
                "Look at the watches: they droop over branches and a cube like cloth, their faces melting. "
                "One watch stays solid - the one crawling with ants, Dali's symbol of decay. The creature "
                "in the middle is a self-portrait. And it's tiny: under 33 centimetres wide, a pocket-sized "
                "earthquake."
            ),
            ["Surrealism", "Oil Painting"],
            4,
        ),
    },
    "artw-autumn-rhythm-number-30-98": {
        "expectName": "Autumn Rhythm Number 30 (1950)",
        "entry": _entry(
            "Autumn Rhythm (Number 30) (1950)",
            "Jackson Pollock",
            (
                "Pollock laid this five-metre canvas flat on his studio floor and poured industrial enamel "
                "straight from sticks and cans - no brush, no easel, no image planned. 'I am nature,' he "
                "said, and the result reads like wind made into a web of paint."
            ),
            (
                "Stand back and the whole thing is one continuous skein; come close and it breaks into "
                "drips, pools and splashes of black, silver and rust. Pollock worked from all four sides, "
                "pouring from sticks - the paint records his whole body's movement. There is no centre and "
                "no top: it's all rhythm."
            ),
            ["Abstract Expressionism", "Oil Painting"],
            5,
        ),
    },
    "artw-marilyn-diptych-1962-by-99": {
        "expectName": "Marilyn Diptych (1962)",
        "entry": _entry(
            "Marilyn Diptych (1962)",
            "Andy Warhol",
            (
                "Fifty silk-screened Marilyns from a single publicity still: twenty-five in hot colour, "
                "twenty-five fading into newsprint black-and-white. Made the month she died, the diptych "
                "is a death portrait that never shows the death."
            ),
            (
                "Read it left to right: the colour side is fame - lipstick, gold hair, glamour. Then the "
                "screen starts to slip: the black-and-white side is an image running out of ink, like a "
                "newspaper photo of someone gone. Same face, same smile, fifty times - the repetition is "
                "the point. She died the month he made it."
            ),
            ["Pop Art", "Silkscreen"],
            4,
        ),
    },
    "artw-balloon-dog-orange-2000-100": {
        "expectName": "Balloon Dog Orange (2000)",
        "entry": _entry(
            "Balloon Dog (Orange) (1994-2000)",
            "Jeff Koons",
            (
                "Ten feet of mirror-polished stainless steel welded into the shape of a party trick, coated "
                "in transparent orange so it reads as pure balloon. One of an edition of five; a sister "
                "cast sold for $58.4 million, then a record for a living artist."
            ),
            (
                "The whole trick is surface: the steel is polished to a mirror, then sprayed with a "
                "transparent colour so it glows like inflated latex. Walk around it - the seams where the "
                "balloon knots are welded steel pretending to be a twist. It looks weightless; it weighs "
                "roughly a tonne."
            ),
            ["Contemporary", "Sculpture"],
            4,
        ),
    },
    "artw-sunflower-seeds-2010-by-101": {
        "expectName": "Sunflower Seeds (2010)",
        "entry": _entry(
            "Sunflower Seeds (2010)",
            "Ai Weiwei",
            (
                "A hundred million porcelain sunflower seeds, each one painted by hand "
                "in Jingdezhen. It took 1,600 craftspeople more than two years to make "
                "a floor that looked exactly like a heap of seeds - and an invitation "
                "to walk on it."
            ),
            (
                "The first impression is dumb abundance: a floor of seeds. Then take in the scale. Every "
                "single seed was individually moulded, fired and painted by hand; the pile weighs tonnes "
                "and the work took 1,600 people over two years. And you were meant to walk on it, hearing "
                "the porcelain crack underfoot."
            ),
            ["Contemporary", "Installation"],
            5,
        ),
    },
    "artw-infinity-mirror-room-1965-102": {
        "expectName": "Infinity Mirror Room (1965)",
        "entry": _entry(
            "Infinity Mirror Room - Phalli's Field (1965)",
            "Yayoi Kusama",
            (
                "Kusama's first infinity mirror room: a small mirrored box packed with hundreds of "
                "red-polka-dotted phallic forms glowing under ultraviolet light, repeated without end in "
                "every direction. One visitor steps inside; the self disappears into the pattern."
            ),
            (
                "Step inside and the walls disappear - every surface is mirror, and the polka-dotted forms "
                "repeat to infinity in all directions. There is room for one viewer: yourself, dissolving "
                "into the pattern. It was the first of the mirror rooms Kusama has built for sixty years."
            ),
            ["Installation", "Contemporary"],
            5,
        ),
    },
    "artw-my-bed-1998-by-103": {
        "expectName": "My Bed (1998)",
        "entry": _entry(
            "My Bed (1998)",
            "Tracey Emin",
            (
                "Emin exhibited her actual bed - the one she retreated to for days, sheets stained, "
                "surrounded by condoms, underwear, vodka bottles and cigarette butts. It earned her a Turner "
                "Prize nomination, national outrage, and a 150,000-pound sale that became 2.5 million at "
                "auction."
            ),
            (
                "Read it as an archaeological dig of one bad week: the stained pillow, the empty vodka, the "
                "tights on the floor, the cigarette butts lined up on the bedside table. Everything is real "
                "and untouched - she didn't arrange it, she stopped living in it. The question is whether "
                "that truth makes it art."
            ),
            ["Installation", "Feminist"],
            5,
        ),
    },
    "artw-the-gates-2005-by-104": {
        "expectName": "The Gates (2005)",
        "entry": _entry(
            "The Gates (2005)",
            "Christo and Jeanne-Claude",
            (
                "For sixteen days in February 2005, 7,503 saffron-orange fabric gates lined 23 miles of "
                "Central Park walkways - a project first dreamed up in 1979 and paid for entirely by the "
                "artists at $21 million. When it came down, the fabric was recycled."
            ),
            (
                "Walk it: the gates are identical, each with a saffron panel that caught the winter light "
                "differently every hour - in sun, in snow, in dusk. The colour is the work: orange against "
                "grey February. It lasted 16 days, cost its makers 21 million dollars, and was always meant "
                "to vanish."
            ),
            ["Installation", "Land Art"],
            6,
        ),
    },
}


def main() -> int:
    data = json.loads(PATH.read_text(encoding="utf-8"))
    by_id = {t["id"]: t for t in data}
    missing = [i for i in REPLACES if i not in by_id]
    if missing:
        print(f"ERROR: ids not in file: {missing}")
        return 1

    changed = 0
    for topic in data:
        fix = REPLACES.get(topic["id"])
        if fix is None:
            continue
        if topic["name"] != fix["expectName"]:
            print(f"SKIP {topic['id']}: expected name {fix['expectName']!r}, found {topic['name']!r}")
            continue
        for k, v in fix["entry"].items():
            topic[k] = v
        changed += 1

    PATH.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"updated {changed} entries")
    return 0


if __name__ == "__main__":
    sys.exit(main())
