#!/usr/bin/env python3
"""Batch 4: add 50 new handcrafted artworks to artworks.json (ids 255-304).

Renaissance/Baroque masters, photography, non-Western art (China, Japan,
Aztec, Benin), and 20th-21st century sculpture/installation. Real fun
facts, handcrafted teasers and quality-bar instructions. Appends to 206
entries. Cap 450 (SCHEMA.md). id convention: artw-{slug}-{n}.
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


PATH = Path(__file__).resolve().parent.parent / "app/src/main/assets/topics/artworks.json"


def _entry(byline: str, name: str, teaser: str, instruction: str, target_name: str, tags: list[str]) -> dict:
    return {
        "byline": byline,
        "name": name,
        "teaser": teaser,
        "instruction": instruction,
        "targetName": target_name,
        "tags": tags,
    }


NEW: dict[str, dict] = {
    # ---------- Renaissance & Baroque ----------
    "artw-primavera-255": _entry(
        "Sandro Botticelli (c. 1482)",
        "Primavera (c. 1482)",
        "Botticelli's 'Spring' — nine mythological figures in an orange grove, with Venus at the center and the three Graces dancing at the left — was painted for a Medici cousin's wedding, and it's the most discussed pagan painting of the Renaissance. Nobody knows exactly what it means: it's been read as a Neoplatonic allegory, a calendar of spring, and a political gift.",
        "Look at the three Graces on the left first: they dance in a circle of transparent white dresses, and the central Grace turns her back to the viewer — a figure so graceful it launched a thousand copies. Then find the details: Mercury at the far left disperses clouds with his caduceus; the god Zephyr at the far right blows on the nymph Chloris, who becomes Flora, the flower-decked figure beside her — the same woman twice, transformation in one image. The oranges in the background are Medici symbols, and the whole painting is a stilled moment of spring arriving.",
        "Primavera (c. 1482) — the three Graces and the double figure of Flora",
        ["Renaissance", "Mythology"],
    ),
    "artw-lady-with-an-ermine-256": _entry(
        "Leonardo da Vinci (c. 1490)",
        "Lady with an Ermine (c. 1490)",
        "Leonardo's portrait of Cecilia Gallerani, the teenage mistress of the Duke of Milan, holding a white ermine — one of only four portraits of women by Leonardo. The ermine is a triple pun: the Greek word for ermine is 'galé,' a play on her name Gallerani; ermines were symbols of purity; and the Duke's order of knighthood was named after the animal.",
        "Look at the ermine first: it's not a pet — it's a symbol. The Greek word 'galé' (weasel/ermine) puns on Gallerani's name, the animal was an emblem of the Duke of Milan's order, and its white fur stood for purity. Then the pose: Cecilia's head and body twist in opposite directions, a spiral pose Leonardo invented that makes the portrait feel alive — the first such 'motion portrait' in art. Her dress is painted in blue and gold with the sleeves of Milanese fashion, and the ermine's claws curl into her sleeve, so woman and animal are one continuous form.",
        "Lady with an Ermine (c. 1490) — the spiral pose and the punning ermine",
        ["Renaissance", "Portrait"],
    ),
    "artw-the-transfiguration-257": _entry(
        "Raphael (1520)",
        "The Transfiguration (1520)",
        "Raphael's last painting — the Transfiguration of Christ above, and below it a possessed boy writhing while the apostles fail to cure him — was still on his easel when he died at 37, and it was carried in his funeral procession. It's the only painting to combine two biblical moments in one image: the glory on the mountain and the failure at its foot.",
        "Look at the two halves: the top is a vision of light — Christ floating between Moses and Elijah, bathed in white — while the bottom is a storm of dark bodies, the apostles failing to heal a possessed boy. Raphael joined two separate gospel events into one painting, and the contrast is the point: glory above, helplessness below. Then the figures: the boy's body is contorted, his eyes rolled back, and the apostles point at him in confusion. The painting is in the Vatican, and it's considered the climax of Raphael's art — he died working on it.",
        "The Transfiguration (1520) — the light above, the writhing boy below",
        ["Renaissance", "Religious"],
    ),
    "artw-venus-of-urbino-258": _entry(
        "Titian (1538)",
        "Venus of Urbino (1538)",
        "Titian's reclining nude — a Venetian courtesan posing as Venus, staring directly at the viewer while a maidservant rummages in a chest behind her — is the ancestor of every reclining nude in art, from Manet's Olympia to today. The Duke of Urbino commissioned it as a wedding gift, and the myrtle plant and roses are marriage symbols.",
        "Look at her gaze first: she looks straight at you — not at the sky or a god, but at the buyer — and her hand rests over her body in a gesture that is both modest and deliberately staged. Then the details: the sleeping dog at her feet (fidelity), the myrtle (Venus's plant), the roses, and the maidservant in the background opening a chest (hope). Titian placed her in a rich interior, not a mythic landscape, so the 'goddess' is unmistakably a real woman in a real room. The pose was copied by Manet for Olympia 300 years later — and every version after that changed what the painting meant.",
        "Venus of Urbino (1538) — the direct gaze and the sleeping dog",
        ["Venetian Renaissance", "Nude"],
    ),
    "artw-portrait-of-innocent-x-259": _entry(
        "Diego Velázquez (1650)",
        "Portrait of Pope Innocent X (1650)",
        "Velázquez's portrait of a grim, suspicious pope is the most frightening portrait in Western art — the pope himself reportedly said 'Troppo vero' ('too true'). Francis Bacon painted a screaming, distorted version of it 300 years later, and the original still hangs in Rome where the pope's descendants once lived.",
        "Look at the face first: the pope's eyes are narrowed, his brows pinched, his mouth a hard line — Velázquez painted the man's suspicion, not his office, and the result scared viewers from the start. Then the red: the cape, chair, and skullcap are painted in layers of red that glow like embers, with the white lace surplice cutting through. The brushwork is loose up close — strokes of paint, not blended skin — but it resolves into a terrifyingly alive face from a distance. The pope's own comment, 'too true,' became the painting's legend.",
        "Portrait of Pope Innocent X (1650) — the suspicious eyes and the glowing red",
        ["Baroque", "Portrait"],
    ),
    "artw-the-jewish-bride-260": _entry(
        "Rembrandt (c. 1665)",
        "The Jewish Bride (c. 1665)",
        "Rembrandt's painting of a couple — his hand on her chest, her hand resting on his — was called the Jewish Bride by mistake (nobody knows if the subjects were Jewish, or married), but the name stuck. Van Gogh said he'd give ten years of his life to sit before it for two weeks, calling it 'the most tender painting in the world.'",
        "Look at the hands first: his left hand rests flat on her breast, and her hand covers his — the touch is the whole painting, and Rembrandt painted it with a tenderness that made van Gogh weep. Then the paint: the man's sleeve is built from thick, glowing strokes of gold and ochre that look like liquid metal, and the woman's red dress shimmers with the same impasto technique — Rembrandt in his late years painted with a knife as much as a brush. The faces are calm, middle-aged, and utterly unidealized. The 'Bride' is believed to be Isaac and Rebecca from the Bible, but the mystery is part of its power.",
        "The Jewish Bride (c. 1665) — the hands and the liquid-gold sleeve",
        ["Dutch Golden Age", "Portrait"],
    ),
    "artw-view-of-delft-261": _entry(
        "Johannes Vermeer (c. 1660)",
        "View of Delft (c. 1660)",
        "Vermeer's painting of his hometown across the water — the most famous cityscape of the Dutch Golden Age, and the only painting Marcel Proust wrote about at length in In Search of Lost Time, where a dying writer is moved to tears by a small patch of yellow wall in it. The city is shown in a light that flattens the buildings into a frieze under a huge sky.",
        "Look at the sky first: it fills more than half the canvas, a bank of grey clouds with the sun breaking through over the city — Vermeer painted weather as a character. Then the water: the calm river mirrors the buildings and boats, and the whole composition is split in thirds — water, city, sky. The light on the far buildings is so precisely painted that scholars have calculated the exact time of day. Proust's dying character Bergotte fixates on 'the little patch of yellow wall' — and the painting hangs in the Mauritshuis in The Hague, where visitors still search for the patch.",
        "View of Delft (c. 1660) — the breaking light and the still water",
        ["Dutch Golden Age", "Cityscape"],
    ),
    "artw-the-astronomer-262": _entry(
        "Johannes Vermeer (1668)",
        "The Astronomer (1668)",
        "Vermeer's painting of a scholar in a blue robe studying a celestial globe, with a chart of the stars open on his desk — painted a year before the Dutch government declared that the Earth orbits the Sun, a theory still officially controversial when Vermeer made this. It's one of two paintings (with The Geographer) showing Vermeer's only male single figures, both thought to depict the scientist Antonie van Leeuwenhoek.",
        "Look at the globe first: it's a celestial globe showing the constellations, and the astronomer's hand rests on it as he consults a book — the painting is about measuring the universe from a quiet room. Then the details: the astrolabe on the cabinet, the star chart hanging on the wall, the window light falling on the scholar's face and hands. The astronomer is believed to be Antonie van Leeuwenhoek, the microscope pioneer and Vermeer's near-neighbor and possible friend — the same man, some scholars think, posed for both the Astronomer and the Geographer. The painting's quiet is the point: the universe is being mapped one gaze at a time.",
        "The Astronomer (1668) — the celestial globe and the scholar's gaze",
        ["Dutch Golden Age", "Genre Painting"],
    ),
    "artw-supper-at-emmaus-263": _entry(
        "Caravaggio (1601)",
        "Supper at Emmaus (1601)",
        "Caravaggio's painting of the moment two disciples recognize the risen Christ — one throws his arms wide, the other grips his chair, while an innkeeper looks on and a basket of fruit teeters at the table's edge. The basket is painted so close to the edge that it seems about to fall into the viewer's lap.",
        "Look at the basket first: it's perched at the very edge of the table, and the pomegranate and grapes are so real you can almost reach them — Caravaggio loved painting things so close they seem to break the picture plane. Then the figures: the disciple on the left throws his arms back in astonishment, the one on the right grips his chair as if to rise, and Christ — shown beardless, an early Christian convention — quietly blesses the bread. The light falls on Christ's face and hands, making the revelation the brightest thing in the room. The painting is in the National Gallery, London.",
        "Supper at Emmaus (1601) — the teetering basket and the thrown-open arms",
        ["Baroque", "Religious"],
    ),
    "artw-the-sleep-of-reason-264": _entry(
        "Francisco Goya (c. 1799)",
        "The Sleep of Reason Produces Monsters (c. 1799)",
        "Goya's etching of a man asleep at his desk while owls, bats, and a lynx crowd the darkness behind him — the most famous image of the Enlightenment turning on itself. It's the frontispiece of his Los Caprichos series, 80 etchings attacking the follies of Spanish society, and the caption reads 'Fantasy abandoned by reason produces impossible monsters.'",
        "Look at the creatures first: the owls watch the sleeper, the bats swarm the dark, and a lynx crouches at the desk — all painted as flat, graphic shapes against the black. Then the caption: 'Fantasy abandoned by reason produces impossible monsters' — Goya's warning that when reason sleeps, superstition and fear take over. The sleeping figure is Goya himself, and the etching is the doorway to his later, darker work — the Black Paintings. Los Caprichos was withdrawn from sale after two days, apparently under pressure from the Inquisition, which makes the etching both a warning and an act of defiance.",
        "The Sleep of Reason Produces Monsters (c. 1799) — the owls and the bats",
        ["Romanticism", "Etching"],
    ),
    "artw-the-sea-of-ice-265": _entry(
        "Caspar David Friedrich (1824)",
        "The Sea of Ice (1824)",
        "Friedrich's painting of a shipwrecked ship crushed and buried by towering slabs of ice — a mountain of frozen shards that rise like crystals against a grey sky. Friedrich painted the ice from studies of the frozen Elbe river, and the painting's original title was The Wreck of Hope, after a real Arctic expedition that vanished.",
        "Look at the ice first: the slabs are not flat — they're sharp, faceted crystals that spike upward like a frozen cathedral, and the ship's broken stern is buried between them, its hull crushed. Then the sky: flat, grey, and indifferent, with a cold light that reveals nothing. Friedrich painted the ice from sketches of the frozen Elbe, and the painting was originally titled The Wreck of Hope, after a real expedition that never returned. The composition is a pyramid of ice — nature's monument to the failure of human ambition. The painting is in the Kunsthalle Hamburg.",
        "The Sea of Ice (1824) — the crushed ship and the crystal slabs",
        ["Romanticism", "Landscape"],
    ),
    "artw-rain-steam-and-speed-266": _entry(
        "J.M.W. Turner (1844)",
        "Rain, Steam and Speed (1844)",
        "Turner's painting of a steam locomotive racing across a bridge through rain and steam — the first great painting of the industrial age, showing the new machine as a creature of light and speed. The train is barely distinguishable from the elements it tears through, and the painting was shown the year before the first railway boom.",
        "Look for the train first: it's a dark wedge hurtling across the bridge, its furnace glowing orange at the front — Turner painted the machine as a force of nature, not a piece of engineering. Then the elements: rain, steam, and speed are painted with the same loose strokes, so the train seems to dissolve into the weather it creates. The hare running along the track ahead of the train is the famous detail — nature fleeing the machine. Turner was in his 60s, at the height of his powers, and the painting's blur was decades ahead of its time — it looks like an Impressionist painting made before Impressionism existed.",
        "Rain, Steam and Speed (1844) — find the train and the hare",
        ["Romanticism", "Industrial"],
    ),
    "artw-luncheon-of-the-boating-party-267": _entry(
        "Pierre-Auguste Renoir (1881)",
        "Luncheon of the Boating Party (1881)",
        "Renoir's sun-drenched painting of friends lunching on a balcony at the Maison Fournaise restaurant on the Seine — a real place that still exists — with Renoir's future wife Aline at the center playing with a dog. The painter Gustave Caillebotte sits in the foreground with his legs apart, and the woman in the straw hat talking to him was a celebrated actress.",
        "Look at the woman with the dog first: that's Aline Charigot, Renoir's future wife, and the dog is a real dachshund the group brought that day. Then find the guests: Caillebotte, the painter, sits bottom-right with his legs apart and his back to the view; the woman in the hat across from him was the actress Ellen Andrée; and the man in the white vest is the restaurant owner's son. The balcony of the Maison Fournaise restaurant on the Seine is still standing, and the painting — full of faces, fruit, and bottles — captures a single lazy afternoon that Renoir spent months composing.",
        "Luncheon of the Boating Party (1881) — the guests and the dog",
        ["Impressionism", "Oil Painting"],
    ),
    "artw-haystacks-268": _entry(
        "Claude Monet (1891)",
        "Haystacks (1891)",
        "Monet painted the same stack of hay in the field near his house 25 times, at every hour and in every weather — the series that made him famous and changed how art shows time. The haystacks are the same object, but each canvas is a different moment of light: dawn, noon, snow, mist, sunset.",
        "Look at the differences: the haystack is the same shape in every canvas, but the light is the real subject — in one the stack glows gold at sunset, in another it's a blue mass in snow, in a third it's dissolving into mist. Monet worked on several canvases at once, chasing the light as it changed, and he said the haystack 'was the only thing I could see from my window.' The series was a commercial and critical triumph that made his name — and it taught the next generation that a subject could be light itself, not a place or a story.",
        "Haystacks (1891) — compare any two canvases of the same stack",
        ["Impressionism", "Series"],
    ),
    "artw-rouen-cathedral-269": _entry(
        "Claude Monet (1894)",
        "Rouen Cathedral series (1894)",
        "Monet painted the facade of Rouen Cathedral more than 30 times from the same window, showing the stone building dissolving in different lights — dawn, noon, dusk, fog, rain. The paintings are less about the cathedral than about how light destroys form, and Monet said he wanted to paint 'the envelope of light around the building.'",
        "Look at any single canvas: the cathedral is there, but its towers and carvings dissolve into strokes of color — Monet painted the light on the stone, not the stone itself. Then compare: in one the facade is a white blaze at noon, in another a violet mass at dusk, in a third barely visible in fog. Monet set up in a room across the street and worked on up to a dozen canvases at once, switching as the light changed. He called the finished series a failure and then exhibited it triumphantly — the paintings are the proof that Impressionism's real subject is the passing of light itself.",
        "Rouen Cathedral series (1894) — the same facade, thirty different lights",
        ["Impressionism", "Series"],
    ),
    "artw-the-night-cafe-270": _entry(
        "Vincent van Gogh (1888)",
        "The Night Café (1888)",
        "Van Gogh painted a real café in Arles — the Café de la Gare — and described it as 'a place where one can ruin oneself, go mad, or commit a crime,' using clashing reds and greens to make the room feel like a trap. The café's owner, who posed for van Gogh, later sold the actual chair from the painting.",
        "Look at the colors first: the walls are blood red, the ceiling is sickly green, and the billiard table is a slab of yellow — van Gogh deliberately chose clashing colors 'to express the terrible passions of humanity.' Then the figures: a few customers slump at tables, the owner stands in his white coat, and the room stretches back into darkness with a clock at the back that shows midnight. Van Gogh wrote to his brother that the painting was his chance to show 'the red and the green, the yellow and the violet' fighting each other — the café as a stage for loneliness. The real Café de la Gare in Arles still exists, renamed in the painting's honor.",
        "The Night Café (1888) — the clashing red and green walls",
        ["Post-Impressionism", "Oil Painting"],
    ),
    "artw-starry-night-over-the-rhone-271": _entry(
        "Vincent van Gogh (1888)",
        "Starry Night Over the Rhône (1888)",
        "Van Gogh painted this view of the Rhône river in Arles at night, with the Big Dipper blazing above the gas lamps and their reflections shattering in the water — and it's the direct ancestor of his more famous Starry Night, painted a year later. He wrote that he 'went out at night to paint the stars' and that painting under the stars was 'the finest way to learn how to paint.'",
        "Look at the sky first: the stars are painted as crosses and halos of light — van Gogh wrote that he was trying to paint the stars 'as I feel them,' not as they are. Then the water: the gas lamps on the quay throw yellow stripes across the river, and the reflections break into dancing fragments against the blue. The couple at the bottom right — a man and woman walking arm in arm — are the only humans, and they're dwarfed by the sky. Van Gogh set up his easel on the bank at night, with candles stuck in his hat to see, and the painting's blue-and-yellow harmony is his first great night sky.",
        "Starry Night Over the Rhône (1888) — the gas-lamp reflections in the water",
        ["Post-Impressionism", "Night"],
    ),
    "artw-where-do-we-come-from-272": _entry(
        "Paul Gauguin (1897)",
        "Where Do We Come From? What Are We? Where Are We Going? (1897)",
        "Gauguin's enormous Tahitian painting — 1.4 by 3.7 meters, read from right to left like his writing — was his attempt at a final testament: painted after the death of his daughter and his own suicide attempt, on cheap sackcloth because he was penniless. He intended it to be his last work, and he wrote that he put 'all my energy' into it.",
        "Read the painting from right to left: the baby at the far right is birth, the figures in the middle are life — reaching, walking, praying — and the old woman at the far left is death, with a white bird at her feet. Gauguin painted it on coarse sackcloth with thin, rubbed paint, and he wrote that it was 'a philosophical work on a theme comparable to the gospel.' The title is written in the corner, in French, as if asking the question of the viewer directly. The painting hangs in the Museum of Fine Arts, Boston, and its scale and sorrow make it the summit of Gauguin's art.",
        "Where Do We Come From? (1897) — read it right to left, from baby to old woman",
        ["Post-Impressionism", "Symbolism"],
    ),
    "artw-at-the-moulin-rouge-273": _entry(
        "Henri de Toulouse-Lautrec (1892)",
        "At the Moulin Rouge (1892)",
        "Toulouse-Lautrec's painting of the Paris nightclub's regulars — a group of friends at a table, a dancer adjusting her dress, and a startlingly green-faced woman at the left. Lautrec, who was 1.52 m tall, lived at the Moulin Rouge and painted its denizens from inside the scene, and the woman's green face is one of the most bizarre details in Impressionist art.",
        "Look at the woman on the left first: her face is painted a lurid green, lit by the gas lamps from below — critics were baffled, and Lautrec never explained it. Then the table: the group includes the painter himself, seated small at the back, and the dancer La Goulue ('the Glutton'), who was the club's star. The whole composition is tilted and crowded, like a snapshot taken at a party, with figures cut off by the frame. Lautrec lived and drank at the Moulin Rouge, and his posters and paintings of it — prostitutes, dancers, and regulars — turned a nightclub into one of the most documented places in art history.",
        "At the Moulin Rouge (1892) — the green-faced woman",
        ["Post-Impressionism", "Nightlife"],
    ),
    "artw-the-basket-of-apples-274": _entry(
        "Paul Cézanne (c. 1893)",
        "The Basket of Apples (c. 1893)",
        "Cézanne's still life is deliberately 'wrong': the tabletop tilts, the basket is tipped, the bottle is askew, and the apple pile is about to roll — yet the painting holds together. Cézanne said he wanted to 'astonish Paris with an apple,' and this is the painting that made the tilted still life a genre.",
        "Look at the table first: it doesn't lie flat — the near edge dips and the far edge rises, so the apples and basket seem to float on a surface that defies gravity. Then the apples: each one is painted as a small block of color with a dark outline, and the pile is built apple by apple like a wall. Cézanne wrote that he wanted to 'astonish Paris with an apple,' and the painting's quiet radicalism is in the geometry: he was not trying to copy the objects but to build them. Picasso and Matisse both studied this painting — it's the doorway to everything that came after.",
        "The Basket of Apples (c. 1893) — the tilting table and the rolling apples",
        ["Post-Impressionism", "Still Life"],
    ),
    "artw-the-dream-1910-275": _entry(
        "Henri Rousseau (1910)",
        "The Dream (1910)",
        "Rousseau's last major painting — a nude woman reclining on a velvet sofa in a jungle full of lions, snakes, birds, and a pipe-playing flutist — was mocked at the 1910 Salon, where critics laughed at the woman on a sofa in the jungle. Rousseau answered: the sofa is only there because the woman is dreaming on it, and in dreams anything is possible.",
        "Look at the sofa first: a red velvet chaise in the middle of a jungle — the critics laughed, and Rousseau explained that the woman is dreaming, so the sofa is wherever her dream puts it. Then the jungle: the leaves are painted in dozens of greens, each one outlined and exact, and the animals hide among them — the lion, the elephant, the birds, and the snake. The flutist in the dark at the left plays for the dreamer. Rousseau had never seen a jungle — he painted from visits to the Paris botanical gardens and from picture books. He died the same year, and the painting that was laughed at now hangs in MoMA as one of its treasures.",
        "The Dream (1910) — the sofa in the jungle and the hidden animals",
        ["Naive Art", "Dreamscape"],
    ),
    "artw-composition-viii-276": _entry(
        "Wassily Kandinsky (1923)",
        "Composition VIII (1923)",
        "Kandinsky's swirling painting of circles, arcs, and diagonal lines — his first fully abstract masterpiece, painted at the Bauhaus school where he taught. Kandinsky 'saw' colors when he heard music (a condition called synesthesia), and he painted this composition the way a composer writes a symphony, with the circles as the main theme.",
        "Look at the circle first: it dominates the upper-left corner, ringed and floating over the crossing lines — Kandinsky called the circle 'the most peaceful shape, but also the most restless.' Then the structure: the diagonals and arcs are not random — they cross and echo like musical phrases, and the small circles and triangles act as accents. Kandinsky had synesthesia: he heard colors and saw sounds, and he believed painting could work like music, with pure form and color creating emotion without any subject. The painting's geometry is calm and explosive at once — the circle held in a web of lines, like a note ringing in a composition.",
        "Composition VIII (1923) — the circle and the crossing diagonals",
        ["Abstract Art", "Bauhaus"],
    ),
    "artw-the-birthday-277": _entry(
        "Marc Chagall (1915)",
        "The Birthday (1915)",
        "Chagall's painting of himself floating upside down to kiss his future wife Bella — she holds flowers, he bends backward in mid-air to reach her lips. He painted it the day after his birthday, when Bella had visited him with flowers, and the painting's levitation is his declaration that love defies gravity.",
        "Look at the kiss first: Chagall floats upside down, his neck bent, to kiss Bella, who stands firmly on the ground — love as a force that lifts the lover off his feet. Then the room: the red floor, the window with a view of the village, and the small details (the lamp, the carpet) that anchor the dream in a real room. Chagall painted this the day after Bella brought him birthday flowers, and he painted her from memory. The painting is the beginning of his lifelong motif of floating lovers — figures who hover because feeling is stronger than physics.",
        "The Birthday (1915) — the floating kiss",
        ["Modern Art", "Love"],
    ),
    "artw-reclining-nude-278": _entry(
        "Amedeo Modigliani (1917)",
        "Reclining Nude (1917)",
        "Modigliani's painting of a nude woman stretched across the canvas, with an elongated body, a small oval face, and one arm behind her head — the most famous of the nudes that caused a scandal in 1917, when police closed his one and only solo exhibition for indecency. He died three years later at 35.",
        "Look at the body first: it's stretched and elongated — the long neck, the sloping shoulders, the narrow face with almond eyes and no pupils — Modigliani's style came from his study of African masks and Italian Mannerism. Then the pose: one arm bent behind her head, the other resting along her hip, her body a continuous warm curve against the deep red couch. The police closed his only solo show in 1917 for 'indecency,' but the painting survived, and it sold at auction in 2010 for over $150 million, then a record for a Modigliani. Her eyes, which look at nothing, are the famous blankness that makes the painting modern.",
        "Reclining Nude (1917) — the elongated body and the blank eyes",
        ["Modern Art", "Nude"],
    ),
    "artw-the-harlequins-carnival-279": _entry(
        "Joan Miró (1925)",
        "The Harlequin's Carnival (1925)",
        "Miró's painting of a roomful of dancing creatures — a harlequin with a mustache plays a guitar while a ladder, a fish, a cat, and dozens of abstract beings float and dance around him. Miró painted it during a period of poverty and hunger, and he said the painting's joy was his answer to despair: 'I was hungry, and I painted the carnival.'",
        "Look at the harlequin first: he has a checked red-and-blue body, a mustache, and a guitar, and he fills the left side of the canvas — Miró said he painted the carnival 'because I was hungry.' Then the creatures: a ladder reaches to the sky, a fish flies, a cat dances, and the black shapes with eyes and legs fill the room with motion. Miró's style — flat shapes, primary colors, and floating lines — looks playful but came from a rigorous study of art, and the painting's title 'carnival' names the chaos. The canvas is a room turned upside-down, full of beings that exist only in the painting's world.",
        "The Harlequin's Carnival (1925) — the ladder and the dancing creatures",
        ["Surrealism", "Abstract"],
    ),
    "artw-jimson-weed-280": _entry(
        "Georgia O'Keeffe (1932)",
        "Jimson Weed (1932)",
        "O'Keeffe's painting of four white jimson weed blossoms, enlarged until the flowers fill the canvas — the painting that sold at auction in 2014 for $44.4 million, a record for any work by a woman artist. O'Keeffe painted flowers huge because, she said, 'nobody sees a flower really — it is so small — so I said to myself, I'll paint it big.'",
        "Look at the scale first: the blossoms fill the entire canvas, larger than life, so you can't see a flower — you're inside one. O'Keeffe said 'nobody sees a flower really — it is so small — so I said to myself, I'll paint it big, and they will be surprised into taking time to look at it.' Then the forms: the petals' curves and the stamens' shapes are painted with a precision that makes the flowers feel both scientific and sensuous. The painting's 2014 sale for $44.4 million set the record for a work by a woman artist, and the flowers — which some critics read as metaphors — O'Keeffe always insisted were just flowers.",
        "Jimson Weed (1932) — the blossoms that fill the canvas",
        ["Modern Art", "Flowers"],
    ),
    "artw-three-studies-figures-281": _entry(
        "Francis Bacon (1944)",
        "Three Studies for Figures at the Base of a Crucifixion (1944)",
        "Bacon's triptych of three screaming, distorted creatures — half-human, half-beast, with open mouths and blind eyes — was shown in 1945, the year WWII ended, and it horrified London. The critic John Russell called it 'the most frightening painting of the century,' and it announced Bacon as the painter of postwar dread.",
        "Look at the mouths first: each figure has a huge, open, screaming mouth — Bacon said he wanted to paint 'the scream,' the sound of a century that had seen too much. Then the bodies: they're half-human, half-animal, with skin like raw meat and blind, staring eyes. The triptych format — three panels — echoes religious altarpieces, but Bacon filled the sacred structure with monsters. He painted the triptych in 1944, before the war's end, and it made him famous overnight. The painting's figures have been read as the Furies of Greek myth, and its horror is still undimmed — it hangs in the Tate.",
        "Three Studies (1944) — the screaming mouths and the raw-meat bodies",
        ["Modern Art", "Triptych"],
    ),
    "artw-big-self-portrait-282": _entry(
        "Chuck Close (1968)",
        "Big Self-Portrait (1968)",
        "Chuck Close's giant photorealist self-portrait — 2.7 meters tall, painted from a photograph with an airbrush so no brushstroke shows — made him famous at 28 and launched the Photorealist movement. His friend, the composer Philip Glass, posed next to the easel while Close painted, playing music to keep him company.",
        "Look at the scale first: the face is enormous — 2.7 meters of skin, stubble, and cigarette smoke, all painted from a black-and-white photograph, not from a mirror. Then the technique: Close used an airbrush to spray paint through stencils, hiding every trace of the artist's hand, so the painting looks like a giant photograph. The cigarette smoke is airbrushed haze, and the glasses' reflection is a grid of tiny details. Close painted himself looking down, unidealized, with every pore and wrinkle recorded. He later became paralyzed and learned to paint with a brush strapped to his wrist — but this early work made the career.",
        "Big Self-Portrait (1968) — the airbrush face and the cigarette smoke",
        ["Photorealism", "Portrait"],
    ),
    "artw-three-flags-283": _entry(
        "Jasper Johns (1958)",
        "Three Flags (1958)",
        "Johns's painting of three American flags stacked on top of each other, each smaller than the one below — 1958 was the same year he painted Flag (1954-55) that made his name, and the stacking turns the symbol into a sculpture of itself. The Museum of Modern Art bought it in 1980 for $1 million, then the most ever paid for a work by a living artist.",
        "Look at the stacking first: three flags, front to back, each a bit smaller — the flag is repeated so the symbol becomes an object, a thing with depth, not just a picture. Then the surface: the paint is encaustic — hot wax mixed with pigment — applied over newspaper, so the flags have a physical, waxy texture you can almost feel. Johns painted flags, targets, and numbers because, he said, they were 'things the mind already knows' — familiar enough to be seen purely as paint. MoMA paid $1 million for it in 1980, then the record for a living artist.",
        "Three Flags (1958) — the wax surface and the three depths",
        ["Pop Art", "Encaustic"],
    ),
    "artw-monogram-284": _entry(
        "Robert Rauschenberg (1959)",
        "Monogram (1959)",
        "Rauschenberg's 'combine' — a stuffed angora goat with a car tire around its middle, standing on a painted, collaged base — is one of the most famous and strangest objects in modern art. Rauschenberg called his hybrids 'combines' because they combined painting and sculpture, and the goat took him three years to get right.",
        "Look at the goat first: it's a real stuffed angora goat, with a tire around its belly, standing on a painted platform covered in collage — newspaper, photographs, paint. Then the surface: the platform is painted and pasted with found images, so the goat stands on a kind of abstract painting. Rauschenberg called these works 'combines' — neither painting nor sculpture but both — and he said the tire made the goat 'look like it had been in a car accident.' The work's meaning is famously open: it's been read as a satire, a joke, and a portrait of American culture. It hangs in MoMA.",
        "Monogram (1959) — the goat with the tire and the collaged base",
        ["Combine", "Assemblage"],
    ),
    "artw-fall-riley-285": _entry(
        "Bridget Riley (1963)",
        "Fall (1963)",
        "Riley's black-and-white painting of wavy vertical lines that seem to ripple and move as you look at them — the definitive work of Op Art, and the painting that made Riley the movement's leader. The 'movement' is entirely in your eye: the canvas is flat, but the wavy lines create a shimmer that makes the picture pulse.",
        "Look at the lines: they're wavy black and white stripes that compress and expand in bands — and as your eyes move across them, the pattern seems to vibrate and roll, though nothing on the canvas moves. Riley designed the work by measuring and drawing each line, then having assistants paint it — the design is exact, the effect is optical. Op Art — short for optical art — was the movement's name, and Riley's black-and-white works were its purest expression. Look at the painting's edges: the pattern is cropped by the frame, so the movement seems to continue beyond the canvas.",
        "Fall (1963) — watch the straight canvas ripple",
        ["Op Art", "Optical"],
    ),
    "artw-relativity-286": _entry(
        "M.C. Escher (1953)",
        "Relativity (1953)",
        "Escher's lithograph of a world with three separate sources of gravity — staircases where figures walk up, down, and sideways, each believing they're on the only floor. It's the most famous impossible scene ever printed, and it hangs in the National Gallery of Canada, where it's one of the most reproduced artworks of the 20th century.",
        "Look at the figures first: each one walks on a different 'floor' — some climb stairs toward the top, others descend, and the figures on the side walls walk horizontally — and every one believes they're upright. Then the staircases: they connect the three gravity systems, so a stairway that's 'up' for one figure is 'sideways' for another. Escher designed the print with exact architectural drawing, and the illusion works because each staircase is consistent on its own. The print's calm precision is what makes it disturbing: no one in the impossible world notices the impossibility.",
        "Relativity (1953) — the three gravity worlds",
        ["Op Art", "Print"],
    ),
    "artw-untitled-film-stills-287": _entry(
        "Cindy Sherman (1977-80)",
        "Untitled Film Stills (1977-80)",
        "Sherman's series of 69 black-and-white photographs of herself dressed and posed like characters from 1950s films — the housewife, the secretary, the femme fatale — each one an invented movie scene. She took every photo herself, alone, with a self-timer, and the series became one of the most influential bodies of work in contemporary art.",
        "Look at the character first: each photo shows Sherman as a different woman — the lonely housewife at the window, the starlet on the phone, the traveler with a suitcase — and every one is a role, not a self-portrait. Then the style: the grain, the lighting, the black-and-white all mimic 1950s and 60s film stills, so the photos look like forgotten movie frames. Sherman took them all alone, with a self-timer, inventing the character, the setting, and the story for each. The series asks who 'the woman' in images is — and since Sherman is the only model, it also asks where the artist ends and the character begins.",
        "Untitled Film Stills (1977-80) — pick a still and read the invented story",
        ["Photography", "Feminist Art"],
    ),
    "artw-supermarket-shopper-288": _entry(
        "Duane Hanson (1970)",
        "Supermarket Shopper (1970)",
        "Hanson's life-size sculpture of a woman in a housecoat pushing a shopping cart stuffed with groceries — so real that museum guards have tried to help her, and visitors regularly mistake her for a person. Hanson cast her from a real woman and dressed her in real clothes, and the sculpture is a monument to the ordinary.",
        "Look at the realism first: the figure is cast from a real person, with real skin texture, real hair, real clothes, and a real shopping cart with real groceries — visitors have been fooled into talking to her. Then the pose: she leans on the cart, exhausted, mid-shopping, her face tired — Hanson said he wanted to show 'the emptiness of consumer society.' Hanson worked from body casts, and his sculptures are so lifelike that museums put them on platforms to keep people from treating them as living visitors. The sculpture is both a joke and an indictment — the supermarket as the modern cathedral.",
        "Supermarket Shopper (1970) — the life-size realism",
        ["Hyperrealism", "Sculpture"],
    ),
    "artw-dead-dad-289": _entry(
        "Ron Mueck (1997)",
        "Dead Dad (1997)",
        "Mueck's hyperrealistic sculpture of his dead father — made at three-quarters scale, in silicone and hair, naked and still — was one of the most talked-about works at the 1997 Sensation exhibition. Mueck had never shown before; the piece made him famous overnight, and its small scale (the father's body is shorter than life) makes the corpse feel oddly childlike and vulnerable.",
        "Look at the scale first: the body is three-quarters life size, so the dead man seems shrunken — smaller than he was in life, which makes the sculpture tender rather than shocking. Then the detail: the silicone skin, the real hair, the ribs and veins — Mueck builds his figures from models and casts, adding every hair by hand. The nakedness is not erotic; it's the body after death, emptied. Mueck made it from memory of his father, and the sculpture's stillness — a man who was, and is no longer — is the whole subject. It hangs in MoMA.",
        "Dead Dad (1997) — the three-quarter scale and the stillness",
        ["Hyperrealism", "Sculpture"],
    ),
    "artw-migrant-mother-290": _entry(
        "Dorothea Lange (1936)",
        "Migrant Mother (1936)",
        "Lange's photograph of Florence Owens Thompson and her children in a California pea-pickers' camp became the defining image of the Great Depression — and the most reproduced photograph in American history. Thompson was 32, a Cherokee mother of seven, and she later said Lange photographed her 'like I wasn't there' — and that she was angry the photo made her a symbol she never chose.",
        "Look at the face first: the furrowed brow, the hand at her chin, the eyes looking past the camera — Thompson was 32 but looks decades older, and the two children press against her shoulders, their faces hidden. Then the details: the tent behind her, the dirt, the thumb hooked in her mouth — every element says poverty without showing hunger directly. Lange took six shots; this one, with the children cropped out, was the most powerful. The photograph helped get federal aid to migrant camps, and it made Lange famous — but Thompson lived in poverty for years and said she never received money from the photo.",
        "Migrant Mother (1936) — the furrowed brow and the hidden children",
        ["Photography", "Documentary"],
    ),
    "artw-moonrise-hernandez-291": _entry(
        "Ansel Adams (1941)",
        "Moonrise, Hernandez, New Mexico (1941)",
        "Adams's photograph of a full moon rising over a small New Mexican village and its graveyard — the most famous landscape photograph ever made, and one of the few Adams took quickly: he saw the scene from his car, fumbled for his camera, and got one exposure before the light vanished. He wrote later that if he'd been one minute slower, the shot would be a different, lesser picture.",
        "Look at the sky first: the moon is tiny and bright above a band of dark clouds, and the light catches the snow on the distant mountains — the whole image is a balance of near-black and near-white. Then the village: the adobe church, the crosses of the graveyard, the fence — all rendered with Adams's famous clarity, where every tonal step from black to white is visible. Adams calculated the exposure from memory of the moon's brightness and later said it was the only exposure he could take before the light changed. He printed the negative for the rest of his life, each print slightly different, and it became his most-published image.",
        "Moonrise, Hernandez (1941) — the moon and the tonal range",
        ["Photography", "Landscape"],
    ),
    "artw-identical-twins-292": _entry(
        "Diane Arbus (1967)",
        "Identical Twins, Roselle, New Jersey (1967)",
        "Arbus's photograph of twin girls standing side by side, one smiling faintly and one not — the most famous photograph in her catalog, and one of the most unsettling images ever made. The twins are identical, but the photo makes them feel like two versions of the same person, and the viewer can't stop looking for the difference.",
        "Look at the two faces first: the twin on the left smiles slightly, the one on the right doesn't — and the difference is tiny but total, making the photo feel like a riddle about identity. Then the outfits: identical white dresses, identical haircuts with identical barrettes — the sameness of the costume makes the difference in expression loom. Arbus photographed people on the margins — giants, dwarfs, nudists — and she said her subject was 'the gap between intention and effect.' The twins became her most reproduced image, and its ambiguity — innocence or menace? — is what keeps it alive.",
        "Identical Twins (1967) — find the difference",
        ["Photography", "Portrait"],
    ),
    "artw-rhein-ii-293": _entry(
        "Andreas Gursky (1999)",
        "Rhein II (1999)",
        "Gursky's photograph of the Rhine river — a horizontal band of grey water between strips of grey-green land under a pale sky — sold in 2011 for $4.3 million, the most expensive photograph ever sold. The picture looks like a simple landscape, but Gursky digitally removed everything human from it — the joggers, the factory, the people — to make the river 'pure.'",
        "Look at the minimalism first: the photograph is a flat grid of horizontal bands — land, water, land, sky — with no people, no buildings, and no detail to rest your eye on. Then the trick: Gursky shot the river with its factories and joggers, then digitally removed them all, so the 'natural' landscape is a deliberate construction. The photo is 1.9 meters wide, so it towers over you like a wall of calm. Gursky said he wanted to show the Rhine 'in its most essential form,' and the $4.3 million sale in 2011 made it the most expensive photograph ever — a record that made people re-examine what a 'landscape' could be.",
        "Rhein II (1999) — the empty bands and the hidden deletions",
        ["Photography", "Contemporary"],
    ),
    "artw-afghan-girl-294": _entry(
        "Steve McCurry (1984)",
        "Afghan Girl (1984)",
        "McCurry's photograph of a 12-year-old Afghan refugee with piercing green eyes — taken in a refugee camp in 1984 and published on National Geographic's cover in 1985 — is the most famous portrait in the history of photography. The girl, Sharbat Gula, was identified 17 years later when McCurry returned to the camp and found her by her eyes.",
        "Look at the eyes first: they're a startling, piercing green, and the girl stares straight at the camera with an expression that mixes defiance, fear, and pride — the photo's power is that gaze. Then the details: the torn red shawl, the dirt, the hard life visible in a face that's still a child's. McCurry photographed her in a refugee camp in 1984; the image ran on National Geographic's cover in 1985 and became the magazine's most famous picture. In 2002, McCurry returned and found Sharbat Gula, now a grown woman, by the same green eyes — she had never seen the photograph until that day.",
        "Afghan Girl (1984) — the green eyes",
        ["Photography", "Portrait"],
    ),
    "artw-truisms-295": _entry(
        "Jenny Holzer (1977)",
        "Truisms (1977)",
        "Holzer's series of one-line statements — 'Abuse of power comes as no surprise,' 'Protect me from what I want' — printed on posters, T-shirts, and LED signs, starting with anonymous posters she pasted around Manhattan at night in the late 1970s. The 'truisms' are deliberately contradictory: some are wise, some are absurd, and Holzer never signs them, so they read as the voice of the culture itself.",
        "Read the statements first: 'Abuse of power comes as no surprise,' 'A little knowledge can go a long way,' 'Protect me from what I want' — each one sounds like folk wisdom, but Holzer made some of them contradict others on purpose. Then the medium: she pasted the early Truisms as anonymous posters all over Manhattan at night, and later printed them on hats, T-shirts, and stone benches, so the 'art' is the words in public space, not on a wall. Holzer's work asks who speaks the slogans we live by — the state, the advertisers, ourselves? In 1990 she became the first woman to represent the US at the Venice Biennale.",
        "Truisms (1977) — read the slogans and feel their contradictions",
        ["Conceptual Art", "Text Art"],
    ),
    "artw-house-whiteread-296": _entry(
        "Rachel Whiteread (1993)",
        "House (1993)",
        "Whiteread's concrete cast of the inside of a whole terraced house in London's East End — she sprayed concrete into the rooms, then stripped away the building, leaving a ghost of the interior: fireplaces, doorways, and windows filled with solid concrete. The sculpture was so controversial that the local council demolished it 80 days after it was built — and the demolition made it more famous.",
        "Look at the surface first: the concrete block is the negative space of a house — where rooms were, there is now solid concrete, and the fireplaces and doorways appear as raised shapes on the outside. Then the details: the roofline, the chimney, the window frames — all inverted, so the house is present as absence. Whiteread made the cast by spraying concrete inside the real house, then demolishing the original around it. The sculpture won the Turner Prize in 1993 and was demolished by the council 80 days later — the fight over its fate made it the most talked-about British artwork of the decade.",
        "House (1993) — the solid rooms and the ghost of the house",
        ["Contemporary", "Sculpture"],
    ),
    "artw-clothespin-297": _entry(
        "Claes Oldenburg (1976)",
        "Clothespin (1976)",
        "Oldenburg's 13.7-meter steel clothespin standing outside Philadelphia's City Hall — an ordinary household object blown up to a monumental 13.7 meters and built from weathering steel, its spring coil framing a view of the sky. Oldenburg made his career enlarging everyday objects, and this one nods to the city's founder, William Penn: the clothespin's two halves echo the shape of Penn's statue across the square.",
        "Look at the scale first: a clothespin the height of a four-story building, in rusted steel — Oldenburg's whole project was taking objects so ordinary we stop seeing them and making them impossible to ignore. Then the details: the coil spring is real and functional-looking, and the two wooden halves are reproduced in steel with visible grain. Oldenburg chose the clothespin for Philadelphia partly because its two-part form echoes the silhouette of the William Penn statue on City Hall across the street — a giant object quietly echoing a giant founder. He called his enlarged objects 'monuments to the everyday.'",
        "Clothespin (1976) — the four-story peg and the Penn echo",
        ["Pop Art", "Public Sculpture"],
    ),
    "artw-descent-into-limbo-298": _entry(
        "Anish Kapoor (1992)",
        "Descent into Limbo (1992)",
        "Kapoor's installation — a rough concrete cube with a perfect black circle cut into its floor, which looks like a flat painted disc but is actually a 10-meter-deep pit. The black is so absolute that the hole is invisible: visitors have walked into it. Kapoor's 'superblack' pigment absorbs almost all light, so the depth reads as flatness — a visual paradox that has injured people.",
        "Look at the black circle: it looks like a painted disc on the floor, but it's actually a 25-foot-deep pit — the black is so light-absorbing that depth reads as flatness. Then the paradox: the eye sees a surface, but a step would find a void. Kapoor created the pigment Vantablack-style 'superblack' that absorbs 99.9% of light, and his black works make space itself disappear. The piece's danger is part of its meaning — a visitor at a later show fell into a similar black well and was injured, and Kapoor's works now carry warnings. The black is not a color but an absence — which is the whole point.",
        "Descent into Limbo (1992) — the black hole that looks like a disc",
        ["Contemporary", "Installation"],
    ),
    # ---------- Non-Western ----------
    "artw-qingming-festival-299": _entry(
        "Zhang Zeduan (1085-1145)",
        "Along the River During the Qingming Festival (1085-1145)",
        "A 5.28-meter handscroll showing a thousand people in the streets, boats, and markets of Kaifeng, the Song dynasty capital — the most famous painting in Chinese history, and one that has been copied thousands of times by emperors and forgers. The original scroll contains over 800 people, 60 animals, and 28 boats, and viewing it is meant to be a slow journey: the scroll is unrolled hand over hand.",
        "Look at the scroll the way it was made to be seen: it's a handscroll, so you unroll it section by section, traveling through the city as you go — the artist arranged the composition so the viewer moves from countryside to gates to river to markets. Then the details: the hundreds of tiny figures are each individually posed — vendors, porters, scholars, a crowd watching a boat that's about to hit the bridge. The scroll has been copied and forged for 900 years, and the Qingming Festival of the title refers to the spring festival of the dead. The original is in Beijing's Palace Museum, where it's shown for only a few weeks a year.",
        "Along the River During the Qingming Festival (1085-1145) — unroll it in your mind",
        ["Chinese Art", "Handscroll"],
    ),
    "artw-red-fuji-300": _entry(
        "Katsushika Hokusai (c. 1831)",
        "Red Fuji (c. 1831)",
        "Hokusai's woodblock print of Mount Fuji rising from a blue-streaked sky, its slopes burnt red by the sun — one of the 36 Views of Mount Fuji, and the most famous single image in Japanese art. Hokusai was about 70 when he made the series, and he wrote that everything he'd done before was 'not worth taking into account.'",
        "Look at the mountain first: its red slopes are banded by shadows, and it rises against a sky of striped blue — the print is built from just a few flat colors, each printed from a separate carved block. Then the composition: the mountain is off-center, low in the frame, with the sky taking the top two-thirds — a daring emptiness that makes the peak feel vast. Hokusai made the 36 Views when he was about 70, and he wrote that he 'only began to understand nature at 73.' The prints were cheap, mass-produced, and sold to ordinary travelers — and they changed European art when they arrived in Paris.",
        "Red Fuji (c. 1831) — the empty sky and the off-center mountain",
        ["Ukiyo-e", "Woodblock"],
    ),
    "artw-aztec-sun-stone-301": _entry(
        "Unknown Aztec artist (c. 1502)",
        "The Aztec Sun Stone (c. 1502)",
        "The 3.6-meter, 24-ton carved stone that is the icon of Mexico — a complex calendar of concentric rings showing the sun god Tonatiuh at the center, surrounded by the four previous suns, the 20 day-signs, and the symbols of the cosmos. It was buried by the Spanish and rediscovered in 1790 under Mexico City's main square — the discovery that made it a national symbol.",
        "Look at the center first: the face of Tonatiuh, the sun god, with a tongue shaped like a sacrificial obsidian knife — the Aztecs believed the sun needed blood to rise. Then the rings: the inner ring shows the four previous 'suns' (eras) that ended in catastrophe, and the next ring lists the 20 day-signs of the Aztec calendar. The stone is not just a calendar — it's a map of the cosmos and a statement of imperial power. The Spanish buried it after the conquest, and it was rediscovered in 1790 under Mexico City's plaza. It now sits in the National Museum of Anthropology, and its image appears on Mexican coins.",
        "The Aztec Sun Stone (c. 1502) — the god's face and the day-signs",
        ["Mesoamerican", "Sculpture"],
    ),
    "artw-benin-bronzes-302": _entry(
        "Benin artists (16th century)",
        "The Benin Bronzes (16th century)",
        "A thousand brass and ivory plaques and sculptures that decorated the royal palace of the Kingdom of Benin (in modern Nigeria) — made by guild artists for the Oba (king) from the 13th century on. In 1897, a British expedition looted the palace, and the plaques were scattered across European museums — the 'Benin Bronzes' are now the most famous case in the debate about returning looted art.",
        "Look at the plaques first: they're rectangular brass panels that once lined the royal palace's walls, showing the Oba, warriors, and Portuguese traders with their distinguishing features — the Benin artists recorded the Europeans who arrived by sea. Then the technique: the plaques were cast by the lost-wax method, a metalworking skill that European visitors in the 16th century found astonishing — one visitor wrote that Benin's metalwork was 'better than anything in Europe.' The 1897 British expedition looted the palace and sold the plaques to museums. Today, museums around the world are returning them to Nigeria, and the debate over the Bronzes changed how museums think about their collections.",
        "The Benin Bronzes (16th century) — the Oba plaques and the Portuguese figures",
        ["African Art", "Lost-wax"],
    ),
    # ---------- Contemporary ----------
    "artw-napoleon-wiley-303": _entry(
        "Kehinde Wiley (2005)",
        "Napoleon Leading the Army over the Alps (2005)",
        "Kehinde Wiley's reimagining of David's Napoleon Crossing the Alps — with a young Black man in a hoodie, Timberlands, and a patterned bandana riding the same rearing horse, over the same mountains. Wiley takes European masterpieces of power and replaces the white aristocrats with everyday Black men and women, making them heroes and rulers for the first time in the history of that imagery.",
        "Look at the pose first: it's exactly David's Napoleon — the rearing horse, the pointing arm, the cape flying — but the rider is a young man in streetwear, and the names carved on the rocks now read 'WILEY' instead of 'BONAPARTE.' Then the details: the bandana, the Timberland boots, the patterned shirt — everyday Black style placed inside a tradition that never depicted it. Wiley photographs real people on the streets and in studios, then paints them into the compositions of Old Masters, and the contrast — heroic scale, ordinary subject — is the point: he's rewriting who gets to be a hero in art. The painting hangs in the Brooklyn Museum.",
        "Napoleon Leading the Army over the Alps (2005) — the hoodie on the rearing horse",
        ["Contemporary", "Portrait"],
    ),
    "artw-tilted-arc-304": _entry(
        "Richard Serra (1981)",
        "Tilted Arc (1981)",
        "Serra's 36-meter, 3.6-meter-tall wall of rusted steel that sliced across a New York federal plaza — installed in 1981 and demolished in 1989 after a decade-long battle, one of the most famous public-art fights in history. Workers at the plaza hated that it blocked their shortcut; Serra argued that removing the sculpture would destroy it, because the site was part of the work. The demolition made him the most famous sculptor in America.",
        "Look at the shape first: a single curved slab of 3.6-meter rusted steel, 36 meters long, cutting across the open plaza at a tilt — the sculpture was the space it blocked, not the metal itself. Then the fight: workers complained it blocked the plaza's crossing and was an eyesore; Serra testified that the site was part of the art, so removing it 'would destroy the work.' A public hearing in 1985 drew a crowd of hundreds, and the sculpture was cut into sections and hauled away in 1989. The controversy — does the public own public art? — made Tilted Arc the defining case of its kind, and Serra went on to make his massive steel curves in cities around the world.",
        "Tilted Arc (1981) — the steel wall and the fight over public space",
        ["Contemporary", "Public Sculpture"],
    ),
}


def main() -> int:
    data = json.loads(PATH.read_text(encoding="utf-8"))
    existing = {t["id"] for t in data}
    dup = [i for i in NEW if i in existing]
    if dup:
        print(f"ERROR: ids already exist: {dup}")
        return 1

    SUBTYPES = {
        "artw-monogram-284": "Sculpture",
        "artw-supermarket-shopper-288": "Sculpture",
        "artw-dead-dad-289": "Sculpture",
        "artw-house-whiteread-296": "Sculpture",
        "artw-clothespin-297": "Sculpture",
        "artw-descent-into-limbo-298": "Installation",
        "artw-tilted-arc-304": "Sculpture",
        "artw-aztec-sun-stone-301": "Sculpture",
        "artw-benin-bronzes-302": "Sculpture",
        "artw-untitled-film-stills-287": "Photograph",
        "artw-migrant-mother-290": "Photograph",
        "artw-moonrise-hernandez-291": "Photograph",
        "artw-identical-twins-292": "Photograph",
        "artw-rhein-ii-293": "Photograph",
        "artw-afghan-girl-294": "Photograph",
        "artw-relativity-286": "Print",
        "artw-red-fuji-300": "Print",
        "artw-qingming-festival-299": "Painting",
        "artw-the-sleep-of-reason-264": "Print",
    }

    added = 0
    for tid, spec in NEW.items():
        entry = {
            "id": tid,
            "categoryId": "ARTWORKS",
            "subtype": SUBTYPES.get(tid, "Painting"),
            "name": spec["name"],
            "teaser": _trim(spec["teaser"]),
            "imageUrl": "",
            "byline": spec["byline"],
            "exploreAction": {
                "verb": "Look at",
                "targetName": _trim(spec["targetName"]),
                "durationMinutes": 8,
                "instruction": _trim(spec["instruction"]),
            },
            "tags": spec["tags"],
            "tier": 2,
        }
        data.append(entry)
        added += 1

    PATH.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"added {added} entries (total {len(data)})")
    return 0


if __name__ == "__main__":
    sys.exit(main())
