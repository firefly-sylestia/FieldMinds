#!/usr/bin/env python3
"""
Batch 2 — real descriptions for artworks 16–35 in artworks.json.

Continues batch 1: replaces the generic AI-filler teaser + exploreAction.instruction
text with factual, real descriptions and corrects the fabricated tags (e.g.
"Guernica" tagged "Installation, Contemporary"). A full realism audit confirmed
all remaining entries are genuine, famous artworks.

Run: python3 scripts/batch_artworks_2.py
Keyed on unique topic id; name guard verifies we edit the entry we intend to.
"""
import json
import sys
from pathlib import Path

PATH = Path("app/src/main/assets/topics/artworks.json")

# (name, teaser, [tags], instruction) — keyed by unique topic id.
FIXES = {
    "artw-a-sunday-on-la-64": dict(
        name="A Sunday on La Grande Jatte (1886) by Georges Seurat",
        teaser=(
            "Seurat spent two years and hundreds of preparatory sketches building this Sunday "
            "on the Seine out of thousands of tiny dots — the masterpiece of pointillism. "
            "Everyone in the park is frozen mid-gesture, strangers sharing a day out but never "
            "quite meeting each other's eyes."
        ),
        tags=["Post-Impressionism", "Oil Painting"],
        instruction=(
            "Move close and the whole picture dissolves into dots; step back and it reassembles "
            "into a park on a Sunday. Watch the couple at center-right, the little monkey on a "
            "leash, the girl in white. Seurat's dots catch the light the way your eye actually does."
        ),
    ),
    "artw-irises-1889-by-vincent-65": dict(
        name="Irises (1889) by Vincent van Gogh",
        teaser=(
            "Van Gogh painted this riot of purple blooms in his first week at the asylum at "
            "Saint-Rémy — the work he later called 'the lightning conductor for my illness.' "
            "The Getty's version is one of the most beloved paintings in the world."
        ),
        tags=["Post-Impressionism", "Oil Painting"],
        instruction=(
            "Follow the single white iris breaking the sea of purple — the one quiet note in a "
            "loud field. There's no horizon: the flowers crowd every edge, buzzing with life. "
            "Van Gogh made this in a week, still glowing with manic energy."
        ),
    ),
    "artw-the-kiss-1908-by-66": dict(
        name="The Kiss (1908) by Gustav Klimt",
        teaser=(
            "A couple fused into a single golden column — Klimt's most famous embrace, painted "
            "at the height of his 'Golden Period.' The man's geometric robes dissolve into the "
            "woman's flowery ones, two bodies becoming one ornament."
        ),
        tags=["Art Nouveau", "Oil Painting"],
        instruction=(
            "Find the boundary where the man's blocky robe meets the woman's round, flowered "
            "one — Klimt turned a kiss into geometry and bloom. The gold is real: gold leaf laid "
            "on like jewelry. The pair kneel on a flower meadow, wrapped so tightly they might "
            "be one figure."
        ),
    ),
    "artw-guernica-1937-by-pablo-67": dict(
        name="Guernica (1937) by Pablo Picasso",
        teaser=(
            "Painted in six weeks after German and Italian planes firebombed the Basque town of "
            "Guernica, Picasso's black-and-white mural is the twentieth century's great anti-war "
            "statement. It stayed exiled in New York until democracy returned to Spain."
        ),
        tags=["Cubism", "Mural"],
        instruction=(
            "Read it left to right like a story: the bull, the grieving mother, the falling "
            "horse, the screaming figure in flames, the mother clutching her dead child. "
            "Everything is grey-on-grey — no blood, yet it is all blood. Find the tiny flower "
            "and the broken sword at the bottom."
        ),
    ),
    "artw-black-square-1915-by-68": dict(
        name="Black Square (1915) by Kazimir Malevich",
        teaser=(
            "Malevich called it 'the zero of form' — a plain black square on a white ground that "
            "declared painting could begin again from nothing. It hung in the corner of his "
            "exhibition, high up like a Russian icon, and launched Suprematism."
        ),
        tags=["Suprematism", "Oil Painting"],
        instruction=(
            "Look at it the way Malevich's audience did: hung high in the corner like an icon of "
            "nothing. The square isn't perfectly straight — you can see the brushmarks and the "
            "craquelure. That imperfection is the point: a handmade beginning, not a machine print."
        ),
    ),
    "artw-american-gothic-1930-by-69": dict(
        name="American Gothic (1930) by Grant Wood",
        teaser=(
            "Wood painted his dentist and his sister as an Iowan farmer and daughter, posed "
            "before a Gothic Revival farmhouse window — the pitchfork held like a trident. It's "
            "been read as both a tribute and a satire of rural America ever since."
        ),
        tags=["Regionalism", "Oil Painting"],
        instruction=(
            "The joke is in the details: the pitchfork's tines rhyme with the stitching on his "
            "overalls and the window's pointed arch. They never smile, never blink. Look at her "
            "hair — a single curl escaping the severe bun, Wood's private sign of life beneath "
            "the stiff pose."
        ),
    ),
    "artw-nighthawks-1942-by-edward-70": dict(
        name="Nighthawks (1942) by Edward Hopper",
        teaser=(
            "Three strangers and a server sit apart in a late-night diner, lit like actors on a "
            "stage while the empty city streets glow outside. Hopper said he painted 'the "
            "loneliness of a big city' — and this is the loneliest painting in America."
        ),
        tags=["Modernist", "Oil Painting"],
        instruction=(
            "Notice the geometry: the window's glass edge divides the picture into lit inside "
            "and dark outside, and nobody looks at anyone else. Hopper left out the door you'd "
            "expect on the corner — no way in, no way out. The man with his back to you is every "
            "commuter."
        ),
    ),
    "artw-campbells-soup-cans-1962-71": dict(
        name="Campbell's Soup Cans (1962) by Andy Warhol",
        teaser=(
            "Thirty-two canvases, one for every flavor of Campbell's soup that existed in 1962, "
            "each hand-stenciled and arranged in a grid like supermarket shelves. Warhol turned "
            "the humblest grocery item into the defining image of Pop Art."
        ),
        tags=["Pop Art"],
        instruction=(
            "Walk the row like a shelf: each can is identical yet different, machine-flat yet "
            "clearly handmade if you look at the stenciled labels up close. Warhol wanted art to "
            "feel like anything you can buy — ask yourself why that felt so shocking in 1962."
        ),
    ),
    "artw-the-dinner-party-1979-72": dict(
        name="The Dinner Party (1979) by Judy Chicago",
        teaser=(
            "A monumental triangular table with thirty-nine place settings, each honoring a "
            "mythical or historical woman, from the primordial goddess to Georgia O'Keeffe — "
            "built over five years by hundreds of volunteers. Judy Chicago's masterwork of "
            "feminist art."
        ),
        tags=["Feminist", "Installation"],
        instruction=(
            "Walk around the triangle — thirteen settings a side — and read the names on the "
            "porcelain floor beneath: 998 women 'who have struggled for recognition.' Each "
            "setting is a ceramic plate unique to its woman, from butterflies to vulvas to thorns."
        ),
    ),
    "artw-cloud-gate-2006-by-73": dict(
        name="Cloud Gate (2006) by Anish Kapoor",
        teaser=(
            "Chicago's stainless-steel 'Bean' — 168 curved plates polished into one seamless "
            "mirror, warping the skyline and everyone who stops to photograph it. Kapoor's "
            "design was inspired by liquid mercury."
        ),
        tags=["Sculpture", "Contemporary"],
        instruction=(
            "Walk underneath and the skyscraper reflections bend over your head like a funhouse "
            "sky — the 'omphalos' belly in the middle distorts everything. Because the seams are "
            "invisible, the whole 110 tons reads as one impossible droplet of liquid metal."
        ),
    ),
    "artw-the-weather-project-2003-74": dict(
        name="The Weather Project (2003) by Olafur Eliasson",
        teaser=(
            "Eliasson filled the Tate Modern's Turbine Hall with a giant semi-circular sun of "
            "200 mono-frequency lamps, a ceiling of mirrors, and a haze of artificial mist. Two "
            "million visitors came to lie on the floor and watch themselves float in the orange "
            "glow."
        ),
        tags=["Installation", "Contemporary"],
        instruction=(
            "Look up at the mirrored ceiling and you'll see the crowd as a sea of tiny black "
            "dots — the work only completes itself when you appear in it. Notice how the orange "
            "light flattens colour: your clothes, the room, everything turns to a single amber hue."
        ),
    ),
    "artw-maman-1999-by-louise-75": dict(
        name="Maman (1999) by Louise Bourgeois",
        teaser=(
            "Bourgeois' giant bronze spider towers over thirty feet tall, carrying a sac of "
            "white marble eggs — a tribute to her mother, a weaver and restorer of tapestries. "
            "The spider is both terrifying protector and patient maker."
        ),
        tags=["Sculpture", "Contemporary"],
        instruction=(
            "Circle underneath and the legs form a cage — the egg sac hangs directly above your "
            "head. Bourgeois' mother was a weaver, and the spider's web-making body is her "
            "portrait. It's huge but not hostile: walk under it and you're in the shelter, not "
            "the trap."
        ),
    ),
    "artw-spiral-jetty-1970-by-76": dict(
        name="Spiral Jetty (1970) by Robert Smithson",
        teaser=(
            "Smithson bulldozed 6,650 tons of basalt and earth into a counterclockwise coil a "
            "quarter-mile long in the Great Salt Lake, where the water is pink with salt-loving "
            "microbes. It disappears underwater for years at a time, then resurfaces like a "
            "prehistoric beast."
        ),
        tags=["Land Art", "Earthwork"],
        instruction=(
            "Walk the coil counterclockwise — against the clockwise spin of time, as Smithson "
            "put it. The basalt is crusted with glittering salt. Whole years the lake swallows "
            "it; you're seeing one chapter of a sculpture that breathes with the water level."
        ),
    ),
    "artw-rain-room-2012-by-77": dict(
        name="Rain Room (2012) by Random International",
        teaser=(
            "A downpour you can walk through without getting wet: 3D tracking cameras read your "
            "body and part the rain around you, step by step. First shown at London's Barbican "
            "in 2012, Rain Room hands you the godlike power of controlling the weather."
        ),
        tags=["Installation", "Contemporary"],
        instruction=(
            "Walk slowly — the rain doesn't just stop above you, it moves with you like a "
            "private bubble. The sound is the storm, the feeling is the spray at the edges. Try "
            "freezing mid-step: the downpour halts exactly at your silhouette."
        ),
    ),
    "artw-untitled-1991-by-felix-78": dict(
        name="Untitled (1991) by Felix Gonzalez-Torres",
        teaser=(
            "A pile of wrapped candies — 175 pounds, the healthy body weight of Gonzalez-Torres' "
            "partner Ross, who died of AIDS in 1991. Visitors take a candy and the museum "
            "refills it, so the work endlessly dies and is reborn, sweet and devastating at once."
        ),
        tags=["Installation", "Conceptual"],
        instruction=(
            "Take a candy — you are part of the artwork, the pile shrinking toward its 'ideal "
            "weight' only to be replenished. This is the portrait of a person: 175 pounds, his "
            "body's weight. What you carry away is love, and grief, one wrapper at a time."
        ),
    ),
    "artw-untitled-boxer-1982-by-79": dict(
        name="Untitled Boxer (1982) by Jean-Michel Basquiat",
        teaser=(
            "A Black figure with arms raised in victory, painted in raw acrylic and oil stick "
            "across a nearly eight-foot canvas in Basquiat's breakout year, 1982. Often read as "
            "a tribute to boxer Sugar Ray Robinson, it is triumph and defiance made into paint."
        ),
        tags=["Neo-Expressionism", "Oil Painting"],
        instruction=(
            "The raised arms are the whole story — a boxer's victory pose that Basquiat turns "
            "into a crown of sorts. Look at the scribbled anatomy: bones, veins, and crown marks "
            "drawn in oil stick over raw canvas. It's fast, deliberate, and not one line is wasted."
        ),
    ),
    "artw-whaam-1963-by-roy-80": dict(
        name="Whaam! (1963) by Roy Lichtenstein",
        teaser=(
            "Two panels: a fighter jet fires and the enemy plane explodes in a yellow burst, "
            "copied and enlarged from a comic-book panel. Lichtenstein's deadpan blow-up made "
            "Pop Art's case — comic pages and gallery walls were the same culture."
        ),
        tags=["Pop Art"],
        instruction=(
            "Look close and you'll see the Ben-Day dots — the printer's halftone pattern "
            "Lichtenstein hand-stenciled so the painting mimics cheap magazine printing. The "
            "'WHAAM!' is painted in the plane's exhaust. It's a comic panel, scaled to a wall, "
            "hung in a museum on purpose."
        ),
    ),
    "artw-the-arnolfini-portrait-1434-81": dict(
        name="The Arnolfini Portrait (1434) by Jan van Eyck",
        teaser=(
            "Van Eyck painted the Italian merchant Giovanni Arnolfini and his wife in their "
            "Bruges bedroom with oil paint so luminous it made the Flemish school famous. The "
            "convex mirror on the wall reflects two figures no one else can see — one of them "
            "the painter himself."
        ),
        tags=["Renaissance", "Oil Painting"],
        instruction=(
            "Read the convex mirror — it shows the whole room from behind, with two visitors in "
            "the doorway, one in a blue cap. Above it van Eyck signed 'Jan van Eyck was here "
            "1434' in tiny script. The little dog, the single candle, the green dress: "
            "everything means something."
        ),
    ),
    "artw-mona-lisa-1503-by-82": dict(
        name="Mona Lisa (1503) by Leonardo da Vinci",
        teaser=(
            "Leonardo's portrait of Lisa Gherardini, wife of a Florentine silk merchant, took "
            "years to paint and never left him. The sfumato haze around her mouth and eyes is "
            "why her smile seems to move — and why the world's most famous painting is actually "
            "about stillness."
        ),
        tags=["Renaissance", "Oil Painting"],
        instruction=(
            "Look at her eyes, then look away, then look back — the smile has changed, or "
            "hasn't. That's sfumato: layers of translucent glaze so thin the contours vanish "
            "into shadow. Notice she has no eyebrows, painted bare by 1500s fashion, and her "
            "hands rest in perfect calm."
        ),
    ),
    "artw-the-garden-of-earthly-83": dict(
        name="The Garden of Earthly Delights (1505) by Hieronymus Bosch",
        teaser=(
            "Bosch's triptych unfolds from Eden through a paradise crowded with naked lovers to "
            "a hell of musical instruments turned torture devices. Painted around 1500, its "
            "strangeness still outpaces every attempt to explain it."
        ),
        tags=["Renaissance", "Oil Painting"],
        instruction=(
            "The panels read like a story: God presents Eve in Eden, humanity indulges in the "
            "garden, and hell is the punchline — a tree-man with the famous 'buttock music' "
            "score painted on him. Start in Eden and move right; every tiny figure is a "
            "cautionary tale."
        ),
    ),
}


def main() -> int:
    data = json.loads(PATH.read_text(encoding="utf-8"))
    changed = 0
    for topic in data:
        fix = FIXES.get(topic["id"])
        if fix is None:
            continue
        # Guard — the name in the file must match what we intend to edit.
        if topic["name"] != fix["name"]:
            print(f"SKIP {topic['id']}: expected {fix['name']!r}, found {topic['name']!r}")
            continue
        topic["teaser"] = fix["teaser"]
        topic["exploreAction"]["instruction"] = fix["instruction"]
        if "tags" in fix:
            topic["tags"] = fix["tags"]
        changed += 1

    PATH.write_text(
        json.dumps(data, indent=2, ensure_ascii=False) + "\n",
        encoding="utf-8",
    )
    print(f"updated {changed} entries")
    return 0


if __name__ == "__main__":
    sys.exit(main())
