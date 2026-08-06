#!/usr/bin/env python3
"""Rewrite artworks instructions — batch 2 of 5 (chunk 1 leftovers, 29 entries).

Handcrafted, painting-specific voices — no "VERB the X first / Then the Y"
scaffolding. Each instruction is written for that specific work. ≤ 450 chars.
"""

import json
import sys
from pathlib import Path

PATH = Path(__file__).resolve().parent.parent / "app/src/main/assets/topics/artworks.json"

REWRITES = {
    "artw-labsinthe-214": (
        "They sit side by side and do not look at each other — or at us. Degas pushed both figures "
        "to the right of the canvas and left the tabletop and floor mostly empty, so the silence "
        "becomes the subject. The woman was a real actress, Ellen Andrée; the man a painter, "
        "Marcellin Desboutin. London critics saw two drunks and recoiled; what Degas painted was "
        "two people who have run out of things to say."
    ),
    "artw-paris-street-rainy-day-215": (
        "The man in the foreground is cut off at the knees by the frame — a cropping borrowed "
        "straight from the new medium of photography, and in 1877 it felt radical. Everyone is "
        "caught mid-step, the wet cobblestones mirror the umbrellas and streetlights, and the "
        "buildings recede with a perspective so exact you could measure it. Caillebotte turned a "
        "rainy intersection into the definitive image of the modern city."
    ),
    "artw-the-cradle-1872-216": (
        "Morisot painted her sister Edma leaning over a cradle, watching her baby sleep — and the "
        "gaze is the subject. The child is barely visible under the gauze, almost abstract, because "
        "the painting is about the mother's attention, not the baby's face. It was shown at the "
        "first Impressionist exhibition of 1874, and it is one of the few great paintings of "
        "motherhood made by a woman who lived it."
    ),
    "artw-the-bath-cassatt-217": (
        "Everything is organized around the small body being washed: the mother and child form a "
        "pyramid, and the striped dress and round basin echo the curves. The mother's left hand "
        "steadies the child's leg while her right washes the foot; the child's hand presses back "
        "against her arm — a real, working moment, not a pose. Cassatt, an American in Paris, "
        "painted mothers as strong and competent, and this became the most famous of those images."
    ),
    "artw-wheatfield-with-crows-218": (
        "The sky is a storm of short, curling strokes pressing down on the field, and the black "
        "crows scatter toward you in quick dark V-shapes. Three paths enter the wheat and vanish "
        "without reaching the horizon — a detail many read as a dead end. Van Gogh painted this in "
        "the last weeks of his life at Auvers, and it has long been called his final painting. "
        "Watch the crows: they are flying at you, not away."
    ),
    "artw-sunflowers-1888-219": (
        "The petals and centers are built with thick, sculptural dabs of paint — van Gogh squeezed "
        "the tube so hard the flowers seem to lift off the canvas. He used at least a dozen "
        "different yellows, from pale lemon to deep ochre, and the blooms run from full to "
        "withered, so one vase holds a whole life cycle. He painted these to welcome Gauguin to "
        "the Yellow House, and Gauguin never quite appreciated them."
    ),
    "artw-self-portrait-bandaged-ear-221": (
        "The bandage covers the ear he injured, held in place by a winter cap — and he painted "
        "himself in the aftermath, looking straight at you, calm and unflinching. Behind him a "
        "Japanese print hangs on the wall and an empty canvas waits on an easel: he is showing "
        "himself as an artist, still working, even now. The pipe in his mouth is the only thing "
        "in the portrait that looks relaxed."
    ),
    "artw-portrait-of-dr-gachet-222": (
        "Gachet rests his head on his hand with the same exhausted gesture van Gogh used in his "
        "own self-portraits — doctor and patient mirroring each other. The foxglove on the table "
        "produces digitalis, a heart medicine, so the plant is both the doctor's badge and a "
        "symbol of the melancholy that floods the painting. Van Gogh wrote that he had found "
        "'a true friend' in Gachet; the portrait sold for $82.5 million in 1990."
    ),
    "artw-the-large-bathers-223": (
        "The bathers are arranged in a triangle under trees that arch into a dome, and their "
        "bodies are simplified into almost columnar masses — Cézanne built this canvas the way a "
        "mason builds a wall, with parallel strokes. It was left unfinished at his death, and bare "
        "cloth shows through in places. Picasso and Matisse called it 'the father of us all,' and "
        "it is the bridge from Impressionism to Cubism."
    ),
    "artw-the-dance-of-life-224": (
        "The woman in red is the same figure who appears through all of Munch's work — passion, "
        "painted in the red dress of his model Tulla Larsen — and the man bending toward her is "
        "Munch himself. She is flanked by her whole life: the girl in white on the left is "
        "innocence, the dark figure on the right is age and death. One woman, three stages, one "
        "painting — the centerpiece of his 'Frieze of Life.'"
    ),
    "artw-the-broken-column-225": (
        "Where her spine should be stands a cracked Ionic column, its broken sections held by "
        "metal brackets — Kahlo turned her own body into a ruined classical monument. Nails are "
        "driven into her face and torso, each one a small point of pain, and the surgical corset "
        "straps hold her upright. She painted this after a year of operations that followed the "
        "bus accident which shattered her at 18. The landscape is cracked open behind her too."
    ),
    "artw-portrait-of-gertrude-stein-226": (
        "The face is mask-like and angular, almost sculptural, while the body is painted in loose "
        "brown tones — Picasso scraped the face off and repainted it from memory after a trip to "
        "Spain, giving it the weight of an Iberian carving. Stein sat for him some ninety times, "
        "and told him, 'You cannot paint me.' He painted her anyway, and she said the portrait "
        "would survive them both. It did."
    ),
    "artw-houses-at-lestaque-227": (
        "The roofs, walls, and trees are all built from geometric slabs, so the village looks like "
        "a cluster of carved blocks — and a critic, seeing this, said Braque was 'reducing "
        "everything to little cubes.' That gave Cubism its name. There is almost no horizon: the "
        "houses stack upward and the sky shrinks to patches, because Braque was flattening "
        "perspective into a single plane. Picasso saw it, and the great collaboration began."
    ),
    "artw-violin-and-candlestick-228": (
        "The violin is broken apart and shown from front, side, and top at once — its curves, "
        "f-holes, and strings suggested by lines rather than drawn, while the candlestick rises "
        "in vertical planes with a small bright flame near the top. Braque and Picasso developed "
        "this in 1910-11: Analytic Cubism, objects shattered into facets and reassembled from "
        "every viewpoint at once. Find the instrument — it is there, in pieces, everywhere."
    ),
    "artw-the-elephants-1948-230": (
        "The legs are as thin as matchsticks, yet they carry an obelisk and a pile of rocks — Dalí "
        "took Bernini's real elephant-and-obelisk sculpture and stretched the legs until the "
        "animals seem to float. The elephants loom huge in the foreground while the desert behind "
        "them is empty, and the sunset's orange light turns the whole scene into a dream. Nothing "
        "about them is possible, and that is the point."
    ),
    "artw-the-temptation-of-st-231": (
        "Each animal carries a symbol of worldly desire on legs too thin to be real: the horse "
        "bears a phallic tower, the elephants bear a naked woman and monuments. Saint Anthony is "
        "tiny at the right, raising a cross — the only solid thing in the painting — refusing the "
        "parade. Dalí painted it for a 1946 film contest he did not win; the film was never made, "
        "but the temptation has outlived it."
    ),
    "artw-golconda-1953-232": (
        "Dozens of identical men in identical coats and hats rain from the sky at the same angle, "
        "each separated by the same gap — a machine of repetition. Then one man is bigger and "
        "closer, breaking the pattern and descending toward the viewer, so the painting asks "
        "whether you are next. The title is the name of a legendary Indian city of diamonds; "
        "Magritte said he chose it only because he liked the sound."
    ),
    "artw-the-lovers-1928-233": (
        "The cloths cover both faces completely — no eyes, no mouths — so the kiss happens between "
        "two blank masks. It is at once the most romantic and most alienating kiss in art: two "
        "people joined and separated in the same instant. The background is a bare wall and a "
        "corner of a room, nothing more. Magritte painted several versions of this motif; the "
        "meaning has never been settled, and the ambiguity is the work."
    ),
    "artw-the-listening-room-234": (
        "The apple is the same shape as any apple, but it fills the entire room — floor, wall, and "
        "ceiling all meet its green surface, so the familiar fruit becomes an alien presence. The "
        "room has no windows and no door; it exists only to be filled. Magritte's title calls it "
        "a listening room, a place for silence — and the apple takes up all the space the silence "
        "needs."
    ),
    "artw-number-1a-1948-235": (
        "A single trail of red paint cuts through the tangle of black and white — Pollock poured "
        "it last, and it acts as the painting's spine and signature. The paint is built in layers, "
        "black under white under color, so the surface has real physical depth, and the drips loop "
        "and tangle with no center. This is the canvas where the drip method arrived fully formed, "
        "and MoMA treats it as one of the most important paintings of the century."
    ),
    "artw-convergence-1952-236": (
        "The black lines cross and re-cross the canvas and knot together at certain points — "
        "Pollock named the painting for those meeting places. Unlike his earlier black-and-white "
        "works, this one explodes with red, yellow, and blue poured over the black web, so the "
        "canvas reads as a battle between order and eruption. It became famous beyond the art "
        "world when a reproduction illustrated a 1950s art textbook."
    ),
    "artw-white-center-1950-237": (
        "The white center is not pure white but a subtle layering of off-whites, and the bands "
        "around it — red, yellow, rose — glow against each other like a sunset held still. Each "
        "rectangle's edge is soft and blurred, so the bands breathe and vibrate instead of sitting "
        "flat. Rothko considered 1950 his breakthrough year: color itself became the subject. It "
        "sold for $72.8 million in 2007, a post-war record at the time."
    ),
    "artw-brillo-boxes-239": (
        "They are wooden and hand-painted — Warhol made replicas of the real cardboard Brillo "
        "boxes because wood lasts longer. The real boxes hold soap pads; these hold nothing, and "
        "that is the question: what makes art art? The philosopher Arthur Danto argued that the "
        "difference between the supermarket carton and the gallery object is invisible to the "
        "eye and visible only to the mind — the art world itself."
    ),
    "artw-drowning-girl-240": (
        "The thought bubble says it all: 'I don't care! I'd rather sink — than call Brad for "
        "help!' — and Lichtenstein painted the absurd melodrama with complete seriousness, blown "
        "up from a comic-book panel. The face and waves are built from Ben-Day dots, the printed "
        "pattern of comics, enlarged by hand until they become abstract. High art copying low art, "
        "tragedy rendered in dots: the masterpiece of Pop Art's most ironic move."
    ),
    "artw-triple-elvis-241": (
        "The same Elvis three times, each print slightly offset — like a film strip or a row of "
        "identical products. Warhol said he repeated images so people would notice the differences, "
        "not the sameness. The canvas is coated with metallic silver, so it reflects light like a "
        "movie screen: Elvis is a star made of light and repetition, a publicity photo turned into "
        "a product line."
    ),
    "artw-sun-tunnels-243": (
        "Four giant concrete cylinders stand in the empty Utah desert, each open at both ends — "
        "from inside, the desert is framed in a circle and the sky in another. Holt drilled holes "
        "in the walls matching four constellations, so by day the sun projects points of light on "
        "the floor and by night the stars shine through the same holes: an observatory that works "
        "both ways. On the solstices the sun rises and sets straight through the tubes."
    ),
    "artw-vietnam-veterans-memorial-244": (
        "The wall is a V sunk into the earth, its polished black granite reflecting the viewer, "
        "the sky, and the trees — you see yourself in the memorial while you read the names, and "
        "that was Lin's design: the living and the dead share the same surface. The names are "
        "carved in chronological order, and the wall rises as the war's toll rises, peaking at "
        "its center. Maya Lin was 21, a Yale student, when her design won."
    ),
    "artw-the-angel-of-the-north-245": (
        "The wings are angled forward, not spread flat — Gormley said the angel is 'not a symbol "
        "but a body turned into space,' leaning into the wind. It is built from 200 tons of "
        "weathering steel, which rusts to a permanent red-brown, so the sculpture literally ages "
        "and changes color as it stands. Before it was built, locals feared it would be 'a giant "
        "statue with a skirt.' It has been there since 1998, larger than anything for miles."
    ),
    "artw-spoonbridge-and-cherry-246": (
        "The spoon is as long as a bus, and the cherry weighs more than a car — Oldenburg and van "
        "Bruggen built their career blowing ordinary objects up to absurd sizes. Water jets from "
        "the top of the cherry and arcs over the bowl, so it is a working fountain that freezes "
        "solid in Minnesota winters. It has stood in the Minneapolis Sculpture Garden since 1988 "
        "and is the most photographed sculpture in the Midwest."
    ),
}

MAX = 450


def main() -> int:
    data = json.loads(PATH.read_text(encoding="utf-8"))
    by_id = {e["id"]: e for e in data}
    missing = [i for i in REWRITES if i not in by_id]
    if missing:
        print("MISSING ids:", missing)
        return 1
    over = [i for i, t in REWRITES.items() if len(t) > MAX]
    if over:
        print("OVER 450:", over)
        for i in over:
            print(" ", len(REWRITES[i]), i)
        return 1
    changed = 0
    for tid, new in REWRITES.items():
        by_id[tid]["exploreAction"]["instruction"] = new
        changed += 1
    PATH.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"rewrote {changed} instructions")
    return 0


if __name__ == "__main__":
    sys.exit(main())
