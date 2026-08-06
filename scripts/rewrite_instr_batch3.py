#!/usr/bin/env python3
"""Rewrite artworks instructions — batch 3 of 5 (chunk 2, 51 entries).

Handcrafted, painting-specific voices — no "VERB the X first / Then the Y"
scaffolding. ≤ 450 chars each.
"""

import json
import sys
from pathlib import Path

PATH = Path(__file__).resolve().parent.parent / "app/src/main/assets/topics/artworks.json"

REWRITES = {
    "artw-cadillac-ranch-247": (
        "Ten Cadillacs, 1949 to 1963, buried nose-first at the same angle as the Great Pyramid, "
        "their tailfins rising in a wave — the whole history of American car design in a single "
        "row. The paint is the art now: visitors spray the cars, the layers build up, and the "
        "ranch encourages it. No fence, no fee, no guard — drive up and add your own layer."
    ),
    "artw-watts-towers-248": (
        "The tallest tower rises 30 meters — ten stories — and it was built by one man, Simon "
        "Rodia, over 33 years, with no scaffolding, no drawings, and hand tools. The surfaces are "
        "set with broken bottles, mirrors, tiles, and seashells he gathered from the neighborhood "
        "and from demolition sites. In 1959 the city ordered the towers demolished; citizens "
        "attached a cable to a crane and pulled — they held."
    ),
    "artw-girl-with-balloon-249": (
        "The girl's arm reaches toward a red heart on a string, and the balloon is just out of "
        "reach — critics call it sentimental, and defenders say the sentiment is the point. Then "
        "the scandal: at a 2018 auction, the framed print shredded itself through a shredder "
        "hidden in the frame the moment the hammer fell. Renamed Love is in the Bin, the shredded "
        "work became worth more than the whole one."
    ),
    "artw-the-sleeping-gypsy-250": (
        "The lion stands inches from the sleeping woman, staring straight ahead, and does not "
        "attack. Rousseau, a self-taught toll collector, painted the desert, the moon, the "
        "mandolin, and the pitcher with a flat, exact clarity that critics mocked as childish — "
        "and collectors ignored. He sold it for 300 francs. It now hangs at MoMA, one of the most "
        "beloved paintings of the 20th century. The question — why doesn't the lion move? — is "
        "the painting."
    ),
    "artw-napoleon-crossing-the-alps-251": (
        "Napoleon never posed, so David painted the whole scene from imagination: the rearing "
        "white horse, the cape, the heroic finger pointing up the pass. The real crossing was made "
        "on a mule, in good weather, weeks after the snow, with guides. The Emperor's instruction "
        "was 'it is not important to be exact.' The names carved on the rocks — BONAPARTE, "
        "HANNIBAL, KAROLUS MAGNUS — put him among the generals who crossed before him."
    ),
    "artw-i-and-the-village-252": (
        "The artist's green profile on the right stares at a cow on the left, and inside the cow's "
        "cheek a smaller cow is being milked — memory as a place where the living and the "
        "remembered share one space. Chagall painted his Russian village from Paris, two years "
        "after leaving it. Find the milkmaid, the tree with a woman's face, the man with a scythe, "
        "the upside-down couple, and the little church — each one a fragment of home."
    ),
    "artw-saturn-devouring-his-son-253": (
        "The eyes are the painting: wide, white, staring, and utterly mad, while the headless, "
        "armless body in his grip is painted in cold, pale flesh against the black. Goya painted "
        "this on the plaster wall of his own house, after a near-fatal illness left him deaf and "
        "isolated — it was never meant to be seen. It was transferred to canvas decades after his "
        "death, and it has terrified viewers ever since."
    ),
    "artw-the-death-of-socrates-254": (
        "Socrates is the only calm figure in the room: he sits upright, one hand raised mid-"
        "argument, the other reaching for the poison cup, choosing death over renouncing his "
        "ideas. The students grieve in a storm around him — one hides his face, one clutches his "
        "thigh. The old man at the foot of the bed is Plato, painted with a philosopher's face "
        "but at the age Socrates actually was — David mixed history and fiction freely to make "
        "the martyrdom perfect."
    ),
    "artw-primavera-255": (
        "The three Graces dance on the left in transparent white dresses, the central one turning "
        "her back — a figure so graceful it launched a thousand copies. At the center stands "
        "Venus, the whole scene an orange grove of nine mythological figures painted for a Medici "
        "wedding. Nobody agrees what it means: Neoplatonic allegory, a calendar of spring, a "
        "political gift. The grass holds more than 500 flower species, each painted from life."
    ),
    "artw-lady-with-an-ermine-256": (
        "The ermine is a triple pun: the Greek word 'galé' plays on Gallerani's name, the animal "
        "was the emblem of the Duke of Milan's order, and its white fur meant purity. Then the "
        "pose: Cecilia's head and body twist in opposite directions, a spiral Leonardo invented "
        "that makes the portrait feel alive — the first 'motion portrait' in art. The light falls "
        "so softly on her face that the painting glows."
    ),
    "artw-the-transfiguration-257": (
        "The painting has two halves that should not meet: above, Christ floats between Moses and "
        "Elijah in a blaze of white light; below, the apostles fail to heal a possessed boy whose "
        "body contorts in the dark. Raphael joined two separate gospel events into one canvas, and "
        "the contrast is the point — glory above, helplessness below. It was still on his easel "
        "when he died at 37, and it was carried in his funeral procession."
    ),
    "artw-venus-of-urbino-258": (
        "She looks straight at you — not at the sky or a god, but at the buyer — and her hand "
        "rests over her body in a gesture that is both modest and staged. The Duke of Urbino "
        "commissioned her as a wedding gift, and the symbols are marital: the sleeping dog is "
        "fidelity, the myrtle is Venus's plant, the roses are love, and the maidservant opening "
        "the chest is hope. Every reclining nude from Manet to today descends from this pose."
    ),
    "artw-portrait-of-innocent-x-259": (
        "The eyes are narrowed, the brows pinched, the mouth a hard line — Velázquez painted the "
        "man's suspicion, not his office, and the pope himself reportedly said 'Troppo vero' — "
        "too true. The cape, chair, and skullcap are built from layers of red that glow like "
        "embers, cut by the white lace surplice. Francis Bacon made a screaming, distorted "
        "version of this portrait 300 years later, which tells you everything about its power."
    ),
    "artw-the-jewish-bride-260": (
        "The hands are the painting: his left hand rests flat on her breast, her hand covers his, "
        "and the touch is so tender van Gogh said he would give ten years of life to sit before "
        "it for two weeks. The man's sleeve is built from thick, glowing strokes of gold and "
        "ochre like liquid metal — Rembrandt in his late years painted with a knife as much as a "
        "brush. The title is a mistake — nobody knows if the couple were Jewish or married — "
        "but it stuck."
    ),
    "artw-view-of-delft-261": (
        "The sky fills more than half the canvas — a bank of grey clouds with the sun breaking "
        "through over the city, weather painted as a character. The calm river mirrors the "
        "buildings and boats, and the composition splits into thirds: water, city, sky. The light "
        "on the far buildings is so precise that scholars have calculated the exact time of day. "
        "Proust wrote pages about one patch of yellow wall in it; see if you can find it."
    ),
    "artw-the-astronomer-262": (
        "The scholar's hand rests on a celestial globe as he consults an open book — measuring "
        "the universe from a quiet room. The astrolabe on the cabinet, the star chart on the "
        "wall, the window light falling on his face and hands: everything in the room is a tool "
        "for understanding the sky. Vermeer painted this a year before the Dutch government "
        "officially declared that the Earth orbits the Sun — an idea still controversial when "
        "this was made."
    ),
    "artw-supper-at-emmaus-263": (
        "The basket teeters on the table's edge, the pomegranate and grapes so close you could "
        "reach for them — Caravaggio loved objects breaking the picture plane. Then the "
        "recognition: the disciple on the left throws his arms back in astonishment, the one on "
        "the right grips his chair as if to rise, and Christ — beardless, an early Christian "
        "convention — blesses the bread; the innkeeper watches, uncomprehending."
    ),
    "artw-the-sleep-of-reason-264": (
        "Owls watch the sleeper, bats swarm the dark, and a lynx crouches at the desk — all "
        "painted as flat, graphic shapes against the black. The caption says it all: 'Fantasy "
        "abandoned by reason produces impossible monsters.' The sleeping figure is Goya himself, "
        "and this etching, the doorway to his Los Caprichos series of 80 satires, is also the "
        "doorway to his later Black Paintings. When reason sleeps, the monsters come out."
    ),
    "artw-the-sea-of-ice-265": (
        "The slabs are not flat ice — they are sharp, faceted crystals spiking upward like a "
        "frozen cathedral, and the ship's broken stern is buried between them, its hull crushed. "
        "The sky is flat, grey, and indifferent, revealing nothing. Friedrich painted the ice "
        "from studies of the frozen Elbe, and the painting's original title was The Wreck of "
        "Hope, after a real Arctic expedition that never returned."
    ),
    "artw-rain-steam-and-speed-266": (
        "The train is a dark wedge hurtling across the bridge, its furnace glowing orange at the "
        "front — Turner painted the machine as a force of nature, not a piece of engineering. "
        "Rain, steam, and speed are painted with the same loose strokes, so the train dissolves "
        "into the weather it creates. Then the famous detail: a hare races along the track ahead "
        "of the train — nature fleeing the machine."
    ),
    "artw-luncheon-of-the-boating-party-267": (
        "The woman playing with the dog is Aline Charigot, Renoir's future wife; the dog is a "
        "real dachshund the group brought that day. Caillebotte the painter sits bottom-right, "
        "legs apart, back to the view; the woman in the straw hat across from him was the actress "
        "Ellen Andrée; the man in the white vest is the restaurant owner's son. The Maison "
        "Fournaise on the Seine still exists, and the balcony still looks like this."
    ),
    "artw-rouen-cathedral-269": (
        "The cathedral is there, but its towers and carvings dissolve into strokes of color — "
        "Monet painted the light on the stone, not the stone. Compare any two canvases: the "
        "facade is a white blaze at noon, a violet mass at dusk, barely visible in fog. He set up "
        "in a room across the street and worked on up to a dozen canvases at once, switching as "
        "the light changed. He said he wanted 'the envelope of light around the building.'"
    ),
    "artw-the-night-cafe-270": (
        "The walls are blood red, the ceiling sickly green, the billiard table a slab of yellow — "
        "van Gogh chose clashing colors 'to express the terrible passions of humanity.' He called "
        "the café 'a place where one can ruin oneself, go mad, or commit a crime.' A few patrons "
        "slump at tables, the owner stands in his white coat, and at the back a clock reads "
        "midnight. The café's owner later sold the actual chair from the painting."
    ),
    "artw-starry-night-over-the-rhone-271": (
        "The stars are painted as crosses and halos of light — van Gogh wrote that he painted "
        "them 'as I feel them,' not as they are. The gas lamps on the quay throw yellow stripes "
        "across the river, and the reflections shatter against the blue. The couple at the "
        "bottom right, walking arm in arm, are the only humans, dwarfed by the sky. This is the "
        "direct ancestor of the more famous Starry Night, painted a year later."
    ),
    "artw-at-the-moulin-rouge-273": (
        "The woman on the left has a face painted lurid green, lit by the gas lamps from below — "
        "critics were baffled, and Lautrec never explained. The table group includes the painter "
        "himself, small at the back, and La Goulue, 'the Glutton,' the club's star. Lautrec, "
        "1.52 meters tall, lived at the Moulin Rouge and painted its regulars from inside the "
        "scene. The tilted, crowded composition cuts figures off at the frame like a snapshot."
    ),
    "artw-the-basket-of-apples-274": (
        "The table doesn't lie flat — the near edge dips and the far edge rises, so the apples "
        "and the tipped basket float on a surface that defies gravity. Each apple is a small "
        "block of color with a dark outline, and the pile is built apple by apple like a wall. "
        "Cézanne said he wanted to 'astonish Paris with an apple,' and this is the painting that "
        "made the tilted still life a genre."
    ),
    "artw-the-dream-1910-275": (
        "A red velvet chaise in the middle of a jungle — the critics laughed, and Rousseau "
        "answered that the woman is dreaming, so the sofa is wherever her dream puts it. The "
        "jungle leaves are painted in dozens of greens, each outlined and exact, and the animals "
        "hide among them: the lion, the elephant, the birds, the snake. The flutist in the dark "
        "at the left plays for the dreamer. It was his last major painting."
    ),
    "artw-composition-viii-276": (
        "The circle dominates the upper-left corner, ringed and floating over the crossing lines — "
        "Kandinsky called it 'the most peaceful shape, but also the most restless.' He had "
        "synesthesia, seeing colors when he heard music, and he composed this canvas like a "
        "symphony: the diagonals and arcs cross and echo like phrases, the small circles and "
        "triangles act as accents. Painted at the Bauhaus, it was his first fully abstract "
        "masterpiece."
    ),
    "artw-the-birthday-277": (
        "Chagall floats upside down, neck bent, to kiss Bella, who stands firmly on the ground — "
        "love as a force that lifts the lover off his feet. She holds the flowers she brought him "
        "for his birthday; the red floor, the window onto the village, the lamp and carpet anchor "
        "the dream in a real room. He painted this the day after, from memory, and the levitation "
        "is his declaration that love defies gravity."
    ),
    "artw-reclining-nude-278": (
        "The body is stretched and elongated — long neck, sloping shoulders, narrow face with "
        "almond eyes and no pupils — Modigliani's style came from African masks and Italian "
        "Mannerism. One arm bends behind her head, the other rests along her hip, and her body is "
        "one continuous warm curve against the deep red couch. The police closed his only solo "
        "exhibition for indecency in 1917; he died three years later at 35."
    ),
    "artw-the-harlequins-carnival-279": (
        "The harlequin has a checked red-and-blue body, a mustache, and a guitar, filling the "
        "left side of the canvas. Around him: a ladder reaches to the sky, a fish flies, a cat "
        "dances, and black shapes with eyes and legs fill the room with motion. Miró painted it "
        "during a period of hunger and poverty, and he said the joy was his answer to despair: "
        "'I was hungry, and I painted the carnival.'"
    ),
    "artw-jimson-weed-280": (
        "The blossoms fill the entire canvas, larger than life — you don't look at a flower, "
        "you're inside one. O'Keeffe said: 'Nobody sees a flower really — it is so small — so I "
        "said to myself, I'll paint it big, and they will be surprised into taking time to look "
        "at it.' The petals' curves and the stamens' shapes are painted with a precision that "
        "feels both scientific and sensuous. It sold for $44.4 million in 2014, a record for a "
        "woman artist."
    ),
    "artw-three-studies-figures-281": (
        "Each figure has a huge, open, screaming mouth — Bacon said he wanted to paint 'the "
        "scream,' the sound of a century that had seen too much. The bodies are half-human, "
        "half-animal, skin like raw meat, eyes blind and staring. The triptych format echoes "
        "religious altarpieces, but Bacon filled the sacred structure with monsters. Shown in "
        "1945, the year WWII ended, it was called 'the most frightening painting of the century.'"
    ),
    "artw-big-self-portrait-282": (
        "The face is enormous — 2.7 meters of skin, stubble, and cigarette smoke, all painted "
        "from a black-and-white photograph, not a mirror. Close used an airbrush to spray paint "
        "through stencils, hiding every trace of the artist's hand, so the portrait looks like a "
        "giant photograph. The smoke is airbrushed haze, the glasses' reflection a grid of tiny "
        "details. His friend Philip Glass sat nearby playing music while Close worked."
    ),
    "artw-three-flags-283": (
        "Three flags, front to back, each a bit smaller — the symbol repeated until it becomes "
        "an object with depth, a sculpture of itself. The paint is encaustic — hot wax mixed "
        "with pigment — laid over newspaper, so the flags have a physical, waxy texture you "
        "almost feel. MoMA paid $1 million for it in 1980, then a record for a living artist."
    ),
    "artw-monogram-284": (
        "A real stuffed angora goat with a car tire around its belly, standing on a painted "
        "platform covered in collage — newspaper, photographs, paint. Rauschenberg called these "
        "hybrids 'combines' because they combined painting and sculpture, and this one took him "
        "three years to get right. The goat stares at nothing, wearing a tire like a halo, and "
        "it has been one of the most famous — and strangest — objects in modern art ever since."
    ),
    "artw-relativity-286": (
        "Each figure walks on a different 'floor' — some climb toward the top, some descend, "
        "and the figures on the side walls walk horizontally — and every one believes they're "
        "upright. The staircases connect the three gravity systems, so a stairway that is 'up' "
        "for one figure is 'sideways' for another. Escher drew the whole impossible world with "
        "such mechanical clarity that it reads as plausible, which is exactly why it unsettles."
    ),
    "artw-untitled-film-stills-287": (
        "Each photograph shows Sherman as a different woman — the lonely housewife at the "
        "window, the starlet on the phone, the traveler with a suitcase — and every one is a "
        "role, not a self-portrait. She shot all 69 alone, with a self-timer, and the grain, "
        "lighting, and black-and-white mimic 1950s film stills so closely that the photos look "
        "like forgotten movie frames. It became one of the most influential bodies of work in "
        "contemporary art."
    ),
    "artw-supermarket-shopper-288": (
        "The figure is cast from a real person — real skin texture, real hair, real clothes, a "
        "real shopping cart with real groceries — and visitors have been fooled into talking to "
        "her. Museum guards have tried to help her with her cart. Hanson cast her in polyester "
        "resin and dressed her from a real housecoat and curlers: a monument to the ordinary, "
        "so real it stops you in your tracks."
    ),
    "artw-dead-dad-289": (
        "The body is three-quarters life size, so the dead man seems shrunken — smaller than he "
        "was in life, which makes the sculpture tender rather than shocking. The silicone skin, "
        "the real hair, the ribs and veins: Mueck builds his figures from casts and adds every "
        "hair by hand. The nakedness is not erotic; it is the body after death, emptied. It "
        "made him famous overnight at the 1997 Sensation exhibition."
    ),
    "artw-migrant-mother-290": (
        "The furrowed brow, the hand at the chin, the eyes looking past the camera — Thompson "
        "was 32 but looks decades older, and the two children press against her shoulders, "
        "their faces hidden. The tent, the dirt, the thumb hooked in her mouth: every element "
        "says poverty without showing hunger directly. She later said Lange photographed her "
        "'like I wasn't there' and that she was angry at becoming a symbol she never chose."
    ),
    "artw-moonrise-hernandez-291": (
        "The moon is tiny and bright above a band of dark clouds, and the light catches the snow "
        "on the distant mountains — a balance of near-black and near-white. The adobe church, "
        "the graveyard crosses, the fence: all rendered with Adams's clarity, every tonal step "
        "from black to white visible. He saw the scene from his car, fumbled for his camera, and "
        "got one exposure before the light vanished — a minute slower and the shot would be a "
        "lesser picture."
    ),
    "artw-identical-twins-292": (
        "The twin on the left smiles slightly; the one on the right doesn't — and the difference "
        "is tiny but total, making the photograph a riddle about identity. The identical white "
        "dresses, identical haircuts, identical barrettes: the sameness of the costume makes the "
        "difference in expression loom. It is the most famous photograph Arbus ever made, and "
        "one of the most unsettling."
    ),
    "artw-rhein-ii-293": (
        "The photograph is a flat grid of horizontal bands — land, water, land, sky — with no "
        "people, no buildings, and nothing for the eye to rest on. The trick: Gursky shot the "
        "river with its factories and joggers, then digitally removed them all, so the 'natural' "
        "landscape is a deliberate construction. At 1.9 meters wide it towers over you like a "
        "wall of calm — and it sold for $4.3 million, the most expensive photograph ever sold."
    ),
    "artw-afghan-girl-294": (
        "The eyes are a startling, piercing green, and the girl stares straight at the camera "
        "with an expression mixing defiance, fear, and pride — the photo's power is that gaze. "
        "The torn red shawl, the dirt, the hard life in a face that is still a child's. McCurry "
        "shot her in a refugee camp in 1984; the image ran on National Geographic's cover in 1985 "
        "and became its most famous picture. She was identified 17 years later, by her eyes."
    ),
    "artw-house-whiteread-296": (
        "The concrete block is a house's negative space — where rooms were, there is now solid "
        "concrete, the fireplaces and doorways appearing as raised shapes on the outside. The "
        "roofline, the chimney, the window frames: all inverted, the house present as absence. "
        "Whiteread sprayed concrete inside the real terraced house, then demolished the original "
        "around it. The council demolished the sculpture itself 80 days later — which made it "
        "famous."
    ),
    "artw-clothespin-297": (
        "A clothespin the height of a four-story building, in rusted steel — Oldenburg's whole "
        "project was taking objects so ordinary we stop seeing them and making them impossible "
        "to ignore. The coil spring is real and functional-looking, and the wooden halves are "
        "reproduced in steel with visible grain. It stands outside Philadelphia's City Hall, "
        "and the two halves echo the shape of William Penn's statue across the square."
    ),
    "artw-descent-into-limbo-298": (
        "The black circle looks like a painted disc on the floor, but it is actually a 25-foot "
        "pit — the pigment is so light-absorbing that depth reads as flatness, and visitors have "
        "walked into it. The eye sees a surface; a step would find a void. Kapoor developed his "
        "own 'superblack' that swallows almost all light, and the sculpture makes space itself "
        "disappear. The paradox is the work."
    ),
    "artw-qingming-festival-299": (
        "A handscroll is made to be unrolled section by section — you travel through the Song "
        "capital as you go, from countryside to gates to river to markets. The artist packed the "
        "5.28 meters with over 800 people, 60 animals, and 28 boats, each figure individually "
        "posed: vendors, porters, scholars, and a crowd watching a boat about to hit the bridge. "
        "Emperors and forgers copied it for centuries — it is the most famous painting in "
        "Chinese history."
    ),
    "artw-red-fuji-300": (
        "The red slopes are banded by shadow, rising against a sky of striped blue — the print "
        "is built from just a few flat colors, each from a separate carved block. The mountain "
        "is off-center and low in the frame, with the sky taking the top two-thirds — a daring "
        "emptiness that makes the peak feel vast. Hokusai was about 70 when he made the 36 "
        "Views, and he wrote that everything he had done before was 'not worth taking into "
        "account.'"
    ),
    "artw-aztec-sun-stone-301": (
        "The face of Tonatiuh, the sun god, sits at the center, a tongue shaped like a "
        "sacrificial obsidian knife — the Aztecs believed the sun needed blood to rise. The "
        "inner ring shows the four previous 'suns,' the eras that ended in catastrophe, and the "
        "next ring lists the 20 day-signs of the calendar. The 3.6-meter, 24-ton stone maps the "
        "cosmos and declares imperial power — buried by the Spanish, rediscovered in 1790 under "
        "Mexico City's main square."
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
