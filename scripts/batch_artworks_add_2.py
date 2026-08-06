#!/usr/bin/env python3
"""Batch 2: add 50 new handcrafted artworks to artworks.json (ids 155-204).

20th & 21st century masterpieces with real fun facts, handcrafted teasers
and quality-bar instructions. Appends to the current 106 entries.
Cap 450 (SCHEMA.md). id convention: artw-{slug}-{n}.
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
    "artw-nude-descending-a-staircase-155": _entry(
        "Marcel Duchamp (1912)",
        "Nude Descending a Staircase, No. 2 (1912)",
        "Duchamp's painting of a body in motion — a nude made of overlapping cubist planes on a staircase — was rejected by Cubist exhibitors as 'too literary' and then became the most mocked painting at the 1913 Armory Show in New York, where critics called it 'an explosion in a shingle factory.' It's the work that introduced modern art to America.",
        "Look for the figure: there is no nude body at all — only a sequence of sharp-edged planes and repeated curves that build the shape of a person walking downstairs, like frames of film stacked on one canvas. Notice the stair rail on the left and the stairs themselves: they're the only solid objects, and the body is pure motion. Duchamp was inspired by chronophotography — Eadweard Muybridge's stop-motion photos of a woman walking — and by the idea that a painting could show time, not just space.",
        "Nude Descending a Staircase, No. 2 (1912) — find the figure in the planes",
        ["Cubism", "Modern Art"],
    ),
    "artw-lhooq-1919-156": _entry(
        "Marcel Duchamp (1919)",
        "L.H.O.O.Q. (1919)",
        "Duchamp bought a cheap postcard of the Mona Lisa and drew a mustache and goatee on her — then signed it L.H.O.O.Q., which when read aloud in French sounds like 'Elle a chaud au cul' ('she has a hot ass'). It's the most famous act of art vandalism ever, and it redefined what a masterpiece could be: a joke.",
        "Look at the postcard, not the joke: Duchamp picked the most sacred image in Western art and defaced it with a schoolboy prank, which is the whole point — he was declaring that art is an idea, not an object, and that no image is beyond questioning. The title is a phonetic pun that only works when spoken in French. Notice the mustache: it's drawn carefully, not scribbled — Duchamp treated the desecration with the care of a restorer, which makes the joke funnier and more serious at once.",
        "L.H.O.O.Q. (1919) — the careful mustache on the sacred image",
        ["Dada", "Readymade"],
    ),
    "artw-the-treachery-of-images-157": _entry(
        "René Magritte (1929)",
        "The Treachery of Images (1929)",
        "Magritte's painting of a pipe with the words 'This is not a pipe' written beneath it — the most famous caption in art. He wasn't joking: the painting is not a pipe, it's paint arranged to look like one, and the sentence is literally true. Magritte said the picture is 'an image of a pipe' — the name is not the thing.",
        "Read the caption out loud: 'Ceci n'est pas une pipe' — this is not a pipe. Then check: it's true. You cannot smoke it, it will not hold tobacco, it is pigment on canvas. Magritte's point is that every image is a stand-in, and that we confuse representations with the things they represent. Then notice the pipe is painted flat, like a museum diagram — there's no attempt to make it look real, only recognizable, which sharpens the contradiction between the word and the image.",
        "The Treachery of Images (1929) — the caption and the flat pipe",
        ["Surrealism", "Conceptual"],
    ),
    "artw-the-son-of-man-158": _entry(
        "René Magritte (1964)",
        "The Son of Man (1964)",
        "Magritte's self-portrait — a man in an overcoat and bowler hat whose face is hidden by a floating green apple. He painted himself as an ordinary man, and said the apple hides the face because of 'the desire to see what is hidden.' The image has been copied more than almost any other 20th-century painting — from album covers to movie posters.",
        "Look at the apple: it floats a few centimeters in front of the face, hiding it but not replacing it — you can see the eyes and the outline of the face around the fruit. Magritte said everything we see hides something else, and the painting is about that gap. Notice the man's left arm: it bends backward at the elbow in a way an arm cannot, a detail most copies remove. The bowler hat and overcoat are the uniform of the 'everyman' — but this everyman is hiding something, and so is the painting.",
        "The Son of Man (1964) — the floating apple and the impossible elbow",
        ["Surrealism", "Self-Portrait"],
    ),
    "artw-the-two-fridas-159": _entry(
        "Frida Kahlo (1939)",
        "The Two Fridas (1939)",
        "Kahlo painted two versions of herself sitting side by side, hearts exposed and joined by a single artery — painted in 1939, the year she divorced Diego Rivera. One Frida wears a European dress and holds surgical forceps on a bleeding artery; the other wears a Tehuana dress and holds a small portrait of Diego. The European Frida is the one who bleeds.",
        "Look at the hearts first: they're anatomical and exposed, and the artery that joins them runs between the two women's hands — but the thread is cut at the European Frida's end, and she drips blood onto her white dress. The Tehuana Frida holds a miniature portrait of Diego Rivera as a child; the other holds the forceps that clamped her artery. Kahlo said the painting was about 'the loneliness of the imagination' — and the stormy sky behind them makes the emotion literal. She gave the painting to a friend to pay a debt.",
        "The Two Fridas (1939) — the joined hearts and the cut artery",
        ["Surrealism", "Self-Portrait"],
    ),
    "artw-self-portrait-with-thorn-160": _entry(
        "Frida Kahlo (1940)",
        "Self-Portrait with Thorn Necklace and Hummingbird (1940)",
        "Kahlo painted herself wearing a necklace of thorns that pierce her neck and draw blood, with a dead hummingbird hanging from her throat. She painted it after her divorce, and every element is a symbol: the thorns are Christ's crown, the hummingbird is a symbol of love in Mexican culture, and the black cat behind her shoulder is about to pounce.",
        "Look at the blood first: the thorns have sunk into her neck, and drops of blood bead at the puncture points — she wears her pain like jewelry and looks directly at you without flinching. Then the animals: the black cat on her left shoulder, the monkey on her right — in Kahlo's paintings these are stand-ins for herself and for Diego. The dead hummingbird at her throat is a talisman of failed love in Mexican folk tradition. Notice her brows: they're unplucked, thick, and joined — the face is uncompromisingly her own.",
        "Self-Portrait with Thorn Necklace (1940) — the bleeding thorns",
        ["Surrealism", "Self-Portrait"],
    ),
    "artw-swans-reflecting-elephants-161": _entry(
        "Salvador Dalí (1937)",
        "Swans Reflecting Elephants (1937)",
        "Dalí's double-image painting — three swans floating on a lake whose reflections become elephants — is one of his most famous exercises in optical illusion. The trick: the swans' necks become elephant trunks and their bodies become elephant heads, using only the reflection and the lakeshore's outline.",
        "Look at the water first: the swans are white and solid, but their reflections are gray elephants — the transformation happens because Dalí matched the swans' silhouettes to the shapes of the rocks and trees on the far bank, and the reflection merges the two. Then notice the autumn trees in the background are bare, and one on the left echoes the elephant-trunk shape. Dalí called this 'the paranoiac-critical method' — seeing one thing inside another until the image becomes unstable, which is exactly what the painting does to your eye.",
        "Swans Reflecting Elephants (1937) — the swan-to-elephant double image",
        ["Surrealism", "Optical Illusion"],
    ),
    "artw-the-face-of-war-162": _entry(
        "Salvador Dalí (1940)",
        "The Face of War (1940)",
        "Dalí painted this skull after the Spanish Civil War — a face made of a skull whose eye sockets and mouth each contain another skull, each of which contains another. In the sand beside the head he painted a single footprint, his own, as if he had just walked away from the vision.",
        "Look into the skull's face: each eye socket holds a smaller skull, and the mouth holds a third — the recursion suggests war that feeds on war without end. Then the skin of the face: it's gray and stretched like a mask, and the snakes around the head are painted with the same texture as the face, so the whole image is alive and dead at once. The single footprint in the lower right is the artist's signature of horror — Dalí said he wanted to show that 'the face of war has no face,' just death repeating itself.",
        "The Face of War (1940) — the skulls within the skull",
        ["Surrealism", "War"],
    ),
    "artw-the-old-guitarist-163": _entry(
        "Pablo Picasso (1903)",
        "The Old Guitarist (1903)",
        "Picasso's painting of a blind, starving old musician bent over his guitar — the masterpiece of his 'Blue Period,' when he painted nothing but poverty and sorrow in shades of blue. X-rays show he painted over a woman's portrait and another painting underneath; he was so poor he reused canvases.",
        "Look at the posture first: the guitarist's body is contorted, folded almost double, and his legs are crossed so tightly the anatomy is impossible — Picasso distorted the body to express collapse, not to imitate it. Then the color: the entire painting is blue — skin, guitar, air — because in Picasso's Blue Period, blue was the color of grief and cold. The guitarist is blind (a common motif for Picasso then), so the music he plays exists only in his mind. The painting hangs in the Art Institute of Chicago, and the X-ray of the hidden woman's face is a famous art-world mystery.",
        "The Old Guitarist (1903) — the contorted body and the all-blue palette",
        ["Blue Period", "Oil Painting"],
    ),
    "artw-three-musicians-164": _entry(
        "Pablo Picasso (1921)",
        "Three Musicians (1921)",
        "Picasso's collage-like painting of three figures — a Pierrot with a clarinet, a Harlequin with a guitar, and a monk with sheet music — is his tribute to the commedia dell'arte characters who had appeared in his work since 1905. Painted in Synthetic Cubism, it looks like a paper cutout but is entirely oil on canvas.",
        "Look at the surface: it's painted to imitate cut-and-pasted paper, with flat shapes and visible 'edges' — Picasso faked collage in paint because he wanted the permanence of oil with the look of scissors. Then the figures: the dog at the bottom left is squeezed between the Pierrot's legs, almost hidden, and the whole composition is a puzzle of overlapping shapes that only resolve into three musicians when you step back. The monk's sheet music is folded in half — a detail that suggests the music has been used.",
        "Three Musicians (1921) — the paper-collage illusion and the hidden dog",
        ["Cubism", "Oil Painting"],
    ),
    "artw-the-weeping-woman-165": _entry(
        "Pablo Picasso (1937)",
        "The Weeping Woman (1937)",
        "Picasso's painting of a woman sobbing, her face shattered into jagged planes, is his companion piece to Guernica — she represents the grief of the bombing's victims, and she was painted dozens of times. The model was Dora Maar, Picasso's partner, whose crying fits he said fascinated him.",
        "Look at the face's geometry: the eyes are in different positions and the teeth are sharp triangles biting a handkerchief — Picasso shattered the face the way the war shattered its subjects. Then the details: the tears are real drops sliding down the painted planes, and the woman's hat and shoulders are outlined in black like glass edges. Picasso painted ten versions in one month, and he said the Weeping Woman 'is like a portrait of my own suffering' — the painting is both a war memorial and a confession of how he treated the women in his life.",
        "The Weeping Woman (1937) — the shattered face and the biting teeth",
        ["Cubism", "War"],
    ),
    "artw-girl-before-a-mirror-166": _entry(
        "Pablo Picasso (1932)",
        "Girl Before a Mirror (1932)",
        "Picasso's painting of Marie-Thérèse Walter looking at her reflection — the face in the mirror is dark and ancient while the girl is young and glowing, and the painting is one of the most analyzed works of the 20th century. The mirror shows what time does: the girl in front is youth, the reflection is mortality.",
        "Look at the two faces: the girl's is bright, round, and full of light — painted from the side in profile — while her reflection is dark, severe, and staring directly forward, like a mask or a skull. The dress is yellow and striped like a wasp, and the mirror's frame is built of diagonal stripes. Art historians read the painting as an allegory of youth and age, beauty and truth, and the composition's symmetry — girl left, mirror right — makes the contrast feel inevitable. The painting hangs at MoMA, which paid $30,000 for it in 1938, a record for a Picasso at the time.",
        "Girl Before a Mirror (1932) — the young face and the dark reflection",
        ["Cubism", "Portrait"],
    ),
    "artw-broadway-boogie-woogie-167": _entry(
        "Piet Mondrian (1943)",
        "Broadway Boogie Woogie (1943)",
        "Mondrian's last completed painting — a grid of colored squares that is actually a map of Manhattan, painted in New York after he fled the Nazi occupation of Europe. The little blocks of color are the city's traffic lights and buildings, and the painting is Mondrian's tribute to boogie-woogie jazz, which he loved.",
        "Look at the grid differently: this is not an abstract pattern — the small squares of yellow, red, blue, and gray are traffic lights and moving cars on Broadway, and the larger blocks are city blocks. Mondrian lived near the real Broadway, and he said boogie-woogie 'destroys the melody, but constructs it' — the painting does the same with the grid, breaking it into rhythm. Notice there are no black lines on this painting: the grid is built from colored bands, and the canvas glows with the energy of the city he loved. He worked on it for a year, and his studio in New York was so full of colored tape and paper he was still rearranging compositions the week he died.",
        "Broadway Boogie Woogie (1943) — the grid as a map of Manhattan",
        ["De Stijl", "Abstract"],
    ),
    "artw-composition-with-red-blue-168": _entry(
        "Piet Mondrian (1930)",
        "Composition with Red, Blue and Yellow (1930)",
        "Mondrian's most famous painting — a white canvas with a bold black grid and three rectangles of red, blue, and yellow. He spent his career stripping art down to this: only primary colors, only straight lines, only right angles. He called his style 'neoplasticism' and believed the grid could express a universal harmony.",
        "Look at the red rectangle first: it's the largest block of color and it anchors the whole painting — Mondrian balanced the heavy red with the small blue and yellow blocks so the canvas never tips. Then the black lines: they're not all the same thickness, and the grid is uneven — Mondrian adjusted the lines by fractions of a millimeter, moving them with tape until the balance felt absolute. He believed that art should be as pure as mathematics, and that the grid of primary colors expressed the order underlying reality. The painting is so famous it has been memed, parodied, and turned into shoes.",
        "Composition with Red, Blue and Yellow (1930) — the heavy red and the taped grid",
        ["De Stijl", "Abstract"],
    ),
    "artw-number-5-1948-169": _entry(
        "Jackson Pollock (1948)",
        "Number 5, 1948 (1948)",
        "Pollock's 8-by-4-foot canvas of tangled drips and loops — painted by laying the canvas flat and flinging paint from a stick — sold privately in 2006 for $140 million, then the highest price ever paid for a painting. There is no beginning or end to the image, which is the point: it is all middle.",
        "Look at the surface: there is no center, no top, no bottom — Pollock said he was 'in' the painting, and the web of lines pulls your eye in every direction at once. Then the textures: he poured, dripped, and flicked enamel paint, sometimes mixed with sand and broken glass, so the surface has depth you can almost feel. Pollock worked with the canvas on the floor, walking around all four sides, and he said the image was 'energy and motion made visible.' The dense tangle in the middle of Number 5 is one of the most famous squares of canvas in the world.",
        "Number 5, 1948 — the web with no center",
        ["Abstract Expressionism", "Drip Painting"],
    ),
    "artw-blue-poles-170": _entry(
        "Jackson Pollock (1952)",
        "Blue Poles (1952)",
        "Pollock's enormous painting — eight vertical lines of blue enamel running through a storm of color — was bought by the Australian government in 1973 for $1.3 million, then the highest price ever paid for a contemporary American painting, and it triggered a national scandal about spending public money on art. It's now the centerpiece of the National Gallery of Australia.",
        "Look at the blue poles first: they break the 'no center' rule of Pollock's other work — eight strong verticals cut through the chaos, and scholars still debate whether he added them early or as a final organizing gesture. Then the storm around them: the reds, yellows, and turquoise are poured and dripped in layers, and the poles look like they're holding the canvas together. Pollock painted it at the height of his fame, two years before his death, and the painting's size — 2 meters tall and 4.8 meters wide — was part of his argument that painting could be monumental.",
        "Blue Poles (1952) — the eight poles in the storm",
        ["Abstract Expressionism", "Drip Painting"],
    ),
    "artw-orange-red-yellow-171": _entry(
        "Mark Rothko (1961)",
        "Orange, Red, Yellow (1961)",
        "Rothko's painting of three floating rectangles — orange, red, and yellow — with soft, breathing edges, sold at Christie's in 2012 for $86.9 million, then a record for post-war art. Rothko didn't think of these as color studies: he said his paintings 'have no center, no focus — they're about the human experience,' and he wanted viewers to stand close and be enveloped.",
        "Look at the edges: the rectangles don't have hard lines — the colors bleed into each other with fuzzy boundaries, so the shapes seem to breathe and shift as you watch. Then the glow: Rothko built the colors from many thin layers of paint, so the rectangles seem lit from within rather than painted on the surface. He said he was not interested in color itself but in 'the basic human emotions — tragedy, ecstasy, doom.' Stand close, let your eyes go soft, and the rectangles start to float and pulse — that's the experience Rothko engineered the paintings for.",
        "Orange, Red, Yellow (1961) — the breathing edges and inner glow",
        ["Abstract Expressionism", "Color Field"],
    ),
    "artw-no-61-1953-172": _entry(
        "Mark Rothko (1953)",
        "No. 61 (Rust and Blue) (1953)",
        "Rothko's painting of a rust-colored field under a band of deep blue — one of his 'tragic' canvases, painted when he was at the height of his powers. Rothko refused to call his paintings abstract: he said they were about 'the human experience,' and he once told a viewer who asked what the paintings meant to 'look at them as if you were looking at a face.'",
        "Look at the proportions: the rust field takes up most of the canvas and the blue band sits at the top like a sky — but this is not a landscape, it's an emotional scale. The edges of the two fields are uneven and soft, so the rust seems to press up against the blue like heat against water. Rothko made his paintings 'on a scale of human emotion,' and he was famously difficult about how they were hung — too low or too high ruined the experience he'd engineered. The painting's glow comes from layer after layer of thin, translucent washes.",
        "No. 61 (Rust and Blue) (1953) — the rust field and the blue band",
        ["Abstract Expressionism", "Color Field"],
    ),
    "artw-christinas-world-173": _entry(
        "Andrew Wyeth (1948)",
        "Christina's World (1948)",
        "Wyeth's painting of a woman lying in a field, looking toward a farmhouse on the horizon — she is Christina Olson, his neighbor in Maine, who had a degenerative muscle disease and could not walk; she crawled everywhere. The painting is her real life: Wyeth watched her drag herself across the fields and painted her from a distance, so she's small in a huge landscape.",
        "Look at the posture: Christina is not resting — her arms are braced, her body is twisted, and she is mid-crawl toward the house, which sits impossibly far away. Wyeth painted her from behind so we see the effort, not the face. Then the landscape: the grass is painted stroke by stroke, and the fields are enormous compared to the tiny figure — the painting's famous loneliness is the reality of her life. Wyeth said he 'didn't paint her because she was disabled, but because she was beautiful in her determination,' and the house, the Olson farm, still stands in Maine.",
        "Christina's World (1948) — the crawling figure and the distant house",
        ["Regionalism", "Realism"],
    ),
    "artw-a-bigger-splash-174": _entry(
        "David Hockney (1967)",
        "A Bigger Splash (1967)",
        "Hockney's painting of a swimming pool in Los Angeles — with a splash frozen mid-air but no swimmer visible. The splash was inspired by a photo of a diving pool in a book, and Hockney painted it over two weeks in his London studio, using masking tape to get the straight pool lines. The invisible swimmer is the joke: the splash is the event, and it's already over.",
        "Look at the splash first: it's the whole subject — a white explosion frozen against the flat blue of the pool — and there's no one in the water to make it. The diver has already jumped in and the pool is about to go still again; the painting is a still from a movie that isn't running. Then the rest: the empty chair, the flat modern house, the palm tree — all painted with the hard, graphic precision of a travel poster. The painting's heat and silence are the point: nothing is happening, and that's what makes it gripping.",
        "A Bigger Splash (1967) — the splash without a swimmer",
        ["Pop Art", "Oil Painting"],
    ),
    "artw-portrait-of-an-artist-pool-175": _entry(
        "David Hockney (1972)",
        "Portrait of an Artist (Pool with Two Figures) (1972)",
        "Hockney's painting of a man in a suit watching a swimmer underwater — the most expensive painting by a living artist at its 2018 sale for $90.3 million. The man is Peter Schlesinger, Hockney's former lover, and the swimmer is another man seen from below; the painting is about the moment a relationship becomes a portrait: watched, not joined.",
        "Look at the two figures: the man on the pool's edge is fully dressed, still, and looking down; the swimmer below the surface is weightless and in motion — they're in the same painting but not the same world. Hockney made 300 photographs to plan it and painted the swimmer first, then repainted the standing figure dozens of times because it was 'the hardest figure I've ever painted.' The two men were lovers who had recently broken up, and the painting's divide — surface and depth, still and moving — is the divide between them.",
        "Portrait of an Artist (Pool with Two Figures) (1972) — the watcher and the swimmer",
        ["Pop Art", "Oil Painting"],
    ),
    "artw-garrowby-hill-176": _entry(
        "David Hockney (1998)",
        "Garrowby Hill (1998)",
        "Hockney's painting of a Yorkshire road curving over a hill — a view from his childhood, painted in his 60s after he moved home from California. The road is a thick yellow band that curves across the green fields, and the whole painting vibrates with the landscape's energy — Hockney said he wanted to paint 'the feeling of driving over a hill.'",
        "Look at the road: it's not gray — it's bright yellow, and it curves across the canvas like a river of light, pulling your eye up and over the hill's crest. Then the fields: they're painted in flat, saturated patches of green and yellow, like a quilt laid over the land, with no people and no buildings. Hockney painted the view from a moving car, from memory and sketches, and he said he wanted to capture 'the joy of the landscape rushing past.' The painting is his love letter to Yorkshire, and the yellow road is the most joyful single line in his work.",
        "Garrowby Hill (1998) — the yellow road and the quilted fields",
        ["Contemporary", "Landscape"],
    ),
    "artw-the-dance-1910-177": _entry(
        "Henri Matisse (1910)",
        "The Dance (1910)",
        "Matisse's painting of five naked figures holding hands in a ring, spinning so fast that one figure leans back almost horizontal — commissioned by a Russian collector who was shocked by it and hid it in his mansion. Matisse said he wanted to express 'the feeling of joy,' and the painting's simplicity — three colors, five bodies, one circle — became one of the most famous images of the 20th century.",
        "Look at the circle: the five figures form a ring, but they're not balanced — one is leaning way back, almost flying, and the gap in the circle is where the energy escapes. Then the colors: the figures are burnt orange, the ground is green, the sky is blue — Matisse said he used only three colors 'to make the painting as simple as a drawing.' The proportions are deliberately distorted: the figures have long, muscular bodies and tiny heads, like ancient Greek pottery figures brought to life. Matisse was 41 and this was his boldest work — a celebration of the human body as pure rhythm.",
        "The Dance (1910) — the unbalanced circle and the three colors",
        ["Fauvism", "Oil Painting"],
    ),
    "artw-the-red-studio-178": _entry(
        "Henri Matisse (1911)",
        "The Red Studio (1911)",
        "Matisse's painting of his own studio painted entirely in red — the walls, the floor, everything — with his actual artworks (paintings, sculptures, ceramics) scattered around the room like islands in a red sea. He painted over a year of his life's work into one room, and the red is so total that the paintings float in it.",
        "Look at the red first: it covers the walls and floor with almost no shading, so the room is a single flat field of color — and the artworks inside it are the only things that stand out. Then find the objects: a bronze sculpture, his painting Le Luxe II on the wall, a ceramic dish, a clock with no hands, and a chair. Matisse said he wanted 'to express space and reality with color alone,' and the red makes the studio feel like a world of its own. The painting is at MoMA, and the red has been described as 'the color of pure creative possibility.'",
        "The Red Studio (1911) — the artworks floating in the red",
        ["Fauvism", "Interior"],
    ),
    "artw-woman-with-a-hat-179": _entry(
        "Henri Matisse (1905)",
        "Woman with a Hat (1905)",
        "The painting that started Fauvism — Matisse's portrait of his wife Amélie wearing an enormous hat, her face painted in green, orange, and pink strokes instead of skin tones. When it was shown at the 1905 Salon d'Automne, a critic called Matisse and his friends 'les fauves' — the wild beasts — and the name stuck.",
        "Look at the face: it's not painted in skin tones — the cheeks are green, the forehead is orange, the nose is pink — Matisse used color to express light and feeling, not to imitate reality. Then the hat: it's a huge confection of feathers and flowers painted in flat, clashing strokes, and it dominates the portrait. The critic who coined 'fauves' ('wild beasts') meant it as an insult; Matisse said he painted the woman 'as I saw her,' and the painting became the manifesto of the movement. Amélie was furious at the portrait at first — and it sold to Gertrude Stein's brother, who hung it proudly.",
        "Woman with a Hat (1905) — the green and orange face",
        ["Fauvism", "Portrait"],
    ),
    "artw-the-snail-1953-180": _entry(
        "Henri Matisse (1953)",
        "The Snail (1953)",
        "Matisse's final great work — an abstract spiral of cut colored paper, made when he was too ill to paint and worked by directing assistants to pin colored shapes on his wall. It's called The Snail because the spiral of colored squares suggests a snail's shell, and it's one of the last things he made before he died at 84.",
        "Look at the spiral: the colored rectangles radiate outward from the center like a shell, and the colors build from deep blues and purples at the heart to bright yellows and oranges at the rim. Then the technique: Matisse was bedridden, so he 'painted with scissors' — assistants painted sheets of paper, then cut them into shapes he directed from his bed. He said 'the paper cut-out allows me to draw in color,' and he called the works 'sculpture in color.' The Snail is 3 meters across, and it's the confident, joyful last word of a career that began with a scandal in 1905.",
        "The Snail (1953) — the radiating spiral of cut paper",
        ["Cut-out", "Late Matisse"],
    ),
    "artw-adele-bloch-bauer-181": _entry(
        "Gustav Klimt (1907)",
        "Portrait of Adele Bloch-Bauer I (1907)",
        "Klimt's 'golden portrait' of a Viennese society woman wrapped in gold leaf and Byzantine pattern — the painting at the center of a famous legal fight: seized by the Nazis, then returned to Adele's niece in 2006 after a Supreme Court ruling, then sold for $135 million, the highest price for a painting at the time. The woman died of meningitis at 43, and Klimt painted her portrait with her face framed by the gold like an icon.",
        "Look at the gold: Adele's dress and the background are covered in real gold leaf, with spirals, eyes, and geometric patterns picked out — Klimt's father was a gold engraver, and the technique comes from Byzantine mosaics. Then the face: it's the only 'painted' part, soft and realistic, floating above the ornate body like a person inside a shrine. The square patterns in her dress echo the 'evil eye' motif, and Klimt painted 100 sketches of her hands alone before starting. The painting is now at the Neue Galerie in New York.",
        "Portrait of Adele Bloch-Bauer I (1907) — the gold leaf and the painted face",
        ["Art Nouveau", "Gold Leaf"],
    ),
    "artw-melancholy-and-mystery-182": _entry(
        "Giorgio de Chirico (1914)",
        "Melancholy and Mystery of a Street (1914)",
        "De Chirico's painting of an empty Italian square with long shadows, a vanishing arcade, and a little girl rolling a hoop toward the light — one of the strangest, most influential images of the 20th century. It's called 'metaphysical art': a dream of a city with the wrong light and no people, and it haunted the Surrealists who came after.",
        "Look at the shadows: they're impossibly long and point in impossible directions — the light doesn't come from anywhere in the painting, so the scene feels like a stage set for a play that hasn't started. Then the figures: the girl with her hoop is the only living thing, and the dark shape at the end of the street is either a statue or a figure — nobody can agree. De Chirico said he painted 'what one sees with closed eyes,' and his empty, angled squares became the visual grammar of the Surrealists, who said his work was 'a bomb' that woke them from realism.",
        "Melancholy and Mystery of a Street (1914) — the impossible shadows",
        ["Metaphysical", "Surrealism"],
    ),
    "artw-the-false-mirror-183": _entry(
        "René Magritte (1928)",
        "The False Mirror (1928)",
        "Magritte's painting of a giant eye whose iris is a blue sky with white clouds — the eye is the 'false mirror' of the title because it reflects the sky but sees nothing. Magritte said the painting shows 'the eye as a window onto the world' — and the window is painted over.",
        "Look at the iris: it's not an iris — it's a daytime sky with clouds, replacing the colored ring of a real eye. Then the pupil: it's a dark circle that reads as an eclipse, so the eye is both a mirror of the sky and a hole in it. Magritte painted several 'eyes' but this is the famous one, and the Surrealist André Breton used it as the cover of his journal. The painting's trick is that it looks like a simple image and is actually a philosophical argument about seeing: the eye is the organ of truth, and here it shows nothing but a painted sky.",
        "The False Mirror (1928) — the sky inside the eye",
        ["Surrealism", "Optical Illusion"],
    ),
    "artw-the-human-condition-184": _entry(
        "René Magritte (1933)",
        "The Human Condition (1933)",
        "Magritte's painting of an easel holding a canvas that shows a landscape — and the painted canvas continues the real landscape behind it so seamlessly you can't tell where painting ends and view begins. It's the definitive image of Magritte's central idea: we only ever see the representation, never the thing itself.",
        "Look at the seam: the canvas on the easel shows a landscape that continues exactly into the window view behind it — the painted tree line lines up with the real tree line. Magritte's point is that there is no 'real' view: we are always looking through someone's frame. The painting inside the painting is the trick that made Magritte famous, and he painted the same idea many times with different subjects — the window, the easel, the mirror. The title says it all: this is the human condition, to mistake the image for the thing.",
        "The Human Condition (1933) — the seamless seam between painting and view",
        ["Surrealism", "Conceptual"],
    ),
    "artw-the-empire-of-light-185": _entry(
        "René Magritte (1954)",
        "The Empire of Light (1954)",
        "Magritte's painting of a quiet street at night — with a daytime sky of blue clouds overhead. The contradiction — a lit lamppost under a bright sky — is the whole work, and he painted 17 versions of it. He said the painting 'brings together things which are mutually exclusive: day and night.'",
        "Look at the split: the lower half is a night street — a lit streetlamp, dark house fronts, a shadowed facade — while the upper half is a bright blue day with clouds. Magritte painted the two together with perfect realism, so the impossibility feels as calm as a photograph. He called the painting 'the only successful synthesis of day and night,' and the Surrealists adored it; the museum director who first showed it said it 'looked like a scene from a mystery novel.' Look at the house on the left: its windows are dark, but the lamp casts its pool of light on the sidewalk — the light and the sky disagree completely, and that disagreement is the painting.",
        "The Empire of Light (1954) — the night street under a day sky",
        ["Surrealism", "Landscape"],
    ),
    "artw-le-dejeuner-en-fourrure-186": _entry(
        "Méret Oppenheim (1936)",
        "Le Déjeuner en fourrure (1936)",
        "Oppenheim's fur-covered teacup, saucer, and spoon — made when she was 23, after Picasso joked at a café that anything could be covered in fur. She went home and lined a real teacup with gazelle fur, and the Museum of Modern Art bought it immediately — the only object by a woman in their Surrealist collection's first decade.",
        "Look at the contradiction: a teacup is the emblem of polite domesticity, and fur is animal and erotic — the two shouldn't meet, and the meeting is the joke. The cup is a real teacup lined inside and out with gazelle fur, so drinking from it would be absurd, which is the point: the familiar object is made unfamiliar, and the mind recoils and delights at once. Oppenheim later said the cup 'was just an idea' and she was surprised anyone took it seriously — but it became the most famous Surrealist object in the world, and it's at MoMA, where visitors still stop to stare at a fur cup.",
        "Le Déjeuner en fourrure (1936) — the fur-lined teacup",
        ["Surrealism", "Object"],
    ),
    "artw-the-burning-giraffe-187": _entry(
        "Salvador Dalí (1937)",
        "The Burning Giraffe (1937)",
        "Dalí's painting of a woman with drawer-like compartments opening from her chest and back, standing before a giraffe whose neck and back are on fire. The drawers are Dalí's symbol for the hidden, repressed parts of the psyche — Freud's unconscious made into furniture — and the burning giraffe is a premonition of war.",
        "Look at the woman's body: the drawers open from her torso like a chest of drawers, each with a little knob — Dalí said this image came from Freud's idea of the unconscious as a storage place, and that he used the drawers to show 'the inner world.' Then the giraffe: its neck is engulfed in flames, a figure Dalí said stood for 'the masculine cosmic apocalyptic monster.' The woman leans on a crutch, another Dalí signature — his figures often need crutches because the world is unstable. Dalí said he painted this 'to make the danger visible before the war arrived.'",
        "The Burning Giraffe (1937) — the drawers in the body and the burning neck",
        ["Surrealism", "Symbolism"],
    ),
    "artw-metamorphosis-of-narcissus-188": _entry(
        "Salvador Dalí (1937)",
        "Metamorphosis of Narcissus (1937)",
        "Dalí's painting of the myth of Narcissus, who fell in love with his own reflection and drowned — shown as a real man on the left and his transformation into a flower-hand on the right. Dalí wrote a poem to accompany it, and Freud, whose theories Dalí was obsessed with, analyzed the painting's paranoia.",
        "Look at the two figures: on the left, a man crouches by a pool, his head reflected below; on the right, his body has become a giant stone hand holding an egg, and from the egg grows the narcissus flower — the myth made literal. Then the background: a naked figure on a pedestal, a dog, chess pieces — the landscape of a dream. Dalí said the painting showed 'the moment the man becomes the flower,' and he sent it with a poem to Freud, who was dying and couldn't fully respond. The painting is in the Tate, and its double image — man and flower — is the essence of Dalí's method.",
        "Metamorphosis of Narcissus (1937) — the man becoming the flower-hand",
        ["Surrealism", "Mythology"],
    ),
    "artw-christ-of-saint-john-189": _entry(
        "Salvador Dalí (1951)",
        "Christ of Saint John of the Cross (1951)",
        "Dalí's painting of Christ floating above a landscape, seen from above and in front — no cross, no nails, no crown of thorns, just a man and a shadow. The composition was inspired by a drawing by Saint John of the Cross, and Dalí placed a tiny fisherman and boat below to give the scale. A man threw a bottle of ink at it in 1986 in protest.",
        "Look at the viewpoint first: you see Christ from above and slightly behind, floating in the sky — the composition is a triangle, with Christ's arms as the widest points and the cross's shadow behind him. Then the scale: the tiny boat and fisherman at the bottom make Christ enormous, and the water and shore below are painted with Dalí's exact realism. Dalí said the painting's subject is 'the Christ of the mystics, not of the physical agony' — no suffering, only transcendence. The painting hangs in Glasgow's Kelvingrove, where the public voted it the city's most-loved artwork.",
        "Christ of Saint John of the Cross (1951) — the floating view from above",
        ["Surrealism", "Religious"],
    ),
    "artw-lavender-mist-190": _entry(
        "Jackson Pollock (1950)",
        "Lavender Mist: Number 1, 1950 (1950)",
        "Pollock's painting of swirling silver, black, white, and a hidden lavender — the 'mist' that gives it its name is barely there, and the painting is one of his most balanced. It's also one of the paintings that made the CIA's cultural program love Abstract Expressionism: it showed America as free, daring, and new.",
        "Look for the lavender: it's almost invisible, a faint haze of color under the black and white lines — Pollock named the painting for a color most viewers never notice. Then the structure: unlike his densest works, this canvas has breathing room, with the drips and loops spread in a wide, calm rhythm. Pollock painted it on the floor of his barn studio, flinging paint from sticks and cans, and the painting hangs in the National Gallery of Art in Washington. Look at the edges: the paint stops short of the canvas edges, leaving a border of bare fabric — Pollock's signature frame within the frame.",
        "Lavender Mist (1950) — find the invisible lavender and the calm rhythm",
        ["Abstract Expressionism", "Drip Painting"],
    ),
    "artw-wrapped-reichstag-191": _entry(
        "Christo & Jeanne-Claude (1995)",
        "Wrapped Reichstag (1995)",
        "Christo and Jeanne-Claude wrapped the German parliament building — 106,000 square meters of silver fabric tied with 15 km of blue rope — for 14 days in 1995, after 24 years of lobbying and 13 rejected parliamentary votes. Two million people came to see it, and the building looked like a silver sculpture, not a government building.",
        "Look at the transformation: the Reichstag, a heavy stone building full of political meaning, became a soft, silver, anonymous mass — the wrapping erased its history for two weeks and made people see it fresh. Then the logistics: it took a team of 90 professional climbers and 120 workers, and the fabric was specially woven silver polypropylene. Christo said the wrapping was not about hiding the building but about 'making it look like a sculpture,' and the project was paid for entirely by selling his own sketches and models. It's the most famous of the couple's 'temporary monuments' — and after the wrap, the building was renovated to house the German parliament again.",
        "Wrapped Reichstag (1995) — the silver transformation",
        ["Land Art", "Installation"],
    ),
    "artw-running-fence-192": _entry(
        "Christo & Jeanne-Claude (1976)",
        "Running Fence (1976)",
        "Christo and Jeanne-Claude stretched 39 km of white nylon fabric across the California countryside — over 14 ranches, a highway, and into the Pacific Ocean — for two weeks in 1976. It was funded by selling drawings, built by 350 workers, and removed completely afterward, leaving no trace: the only proof is the film and photographs.",
        "Look at the scale: the fence ran 39 kilometers, 5.5 meters tall, crossing private ranches whose owners had to be convinced one by one — it took 42 months of negotiations and 18 public hearings to get permission. Then the material: white nylon panels, so the fence changed with the weather, appearing as a solid wall in fog and a shimmering line in sun. Christo said he wanted to create 'a ribbon of light' that followed the land's contours. The project was entirely self-funded through art sales, and after 14 days the entire fence was dismantled and recycled — the artwork exists now only as documentation, which was the point.",
        "Running Fence (1976) — the 39 km ribbon through the hills",
        ["Land Art", "Installation"],
    ),
    "artw-puppy-1992-193": _entry(
        "Jeff Koons (1992)",
        "Puppy (1992)",
        "Koons's 12-meter-tall West Highland terrier made of 60,000 living flowers — steel, soil, and an irrigation system that keeps the dog blooming. It was first shown in Germany, where the city of Aachen wanted to buy it but the logistics of a 12-meter flowering dog stalled the deal; it now stands outside the Guggenheim in Bilbao, blooming every spring.",
        "Look at the scale first: the dog is taller than the museum's facade behind it, and its face is friendly but enormous — the size makes the cute subject absurd and monumental at once. Then the material: the 'fur' is 60,000 bedding plants — begonias, petunias, marigolds — grown on a steel frame with a hidden irrigation system, so the sculpture literally changes with the seasons. Koons said the puppy 'is about love, warmth, and compassion.' The Bilbao installation is so beloved that the museum rebuilt its plaza around it, and the puppy needs 38 tons of soil and a full-time gardener.",
        "Puppy (1992) — the 12-meter flower dog",
        ["Contemporary", "Public Sculpture"],
    ),
    "artw-tulips-1995-194": _entry(
        "Jeff Koons (1995)",
        "Tulips (1995)",
        "Koons's seven stainless-steel tulips — mirror-polished so they reflect everything around them, with stems, leaves, and blossoms built from inflated-looking metal. The colors are candy-bright, and the sculptures are so reflective that the sky and the viewer's face slide across their surfaces as you move.",
        "Look at the surfaces: the steel is polished to a mirror finish, so the tulips are also seven distorted mirrors — your reflection and the room stretch and slide across the petals as you walk around them. Then the shapes: each blossom is built like an inflatable, with creases and a 'balloon twist' at the base — Koons has said he's 'always loved the idea of the balloon.' The tulips are installed at Rockefeller Center in New York, where the glossy blooms reflect the skyscrapers and crowds. Koons made seven: the number echoes the seven days of creation, and the tulips are his cheerful version of a classic Dutch flower painting, inflated to monument scale.",
        "Tulips (1995) — the mirror-polish surfaces",
        ["Contemporary", "Sculpture"],
    ),
    "artw-for-the-love-of-god-195": _entry(
        "Damien Hirst (2007)",
        "For the Love of God (2007)",
        "Hirst's sculpture of a human skull cast in platinum and covered in 8,601 diamonds, with the original teeth left in place — it cost £14 million to make, the most expensive single artwork ever made, and Hirst said he made it 'to feel invincible.' The skull is a copy of a real 18th-century skull Hirst bought in a London shop.",
        "Look at the contradiction: a death's head — the classic symbol of mortality — covered in the most expensive, permanent material humans know. The diamonds are 8,601 of them, totaling 1,106 carats, on a platinum cast of a real skull, and the only unpainted parts are the original teeth. Then the title: 'For the Love of God' — Hirst has said he 'wanted to make something that would be around forever, like an Egyptian artifact.' The sculpture's asking price was £50 million, and it polarized the art world: some called it a masterpiece, others a stunt — and the argument is the artwork.",
        "For the Love of God (2007) — the diamond death's head",
        ["Contemporary", "Sculpture"],
    ),
    "artw-a-thousand-years-196": _entry(
        "Damien Hirst (1990)",
        "A Thousand Years (1990)",
        "Hirst's glass box containing a cow's head, maggots, flies, sugar, and water — a complete, working ecosystem in which flies hatch, feed, breed, and die, with some electrocuted on a fly-killing lamp inside. It was the work that made Hirst's name, and when the Saatchi Gallery showed it, the gallery's chairman called it 'probably the most extraordinary work of art made by anyone under 40 anywhere in the world.'",
        "Look at the cycle: the box is a sealed life-support system — maggots hatch from the cow's head, become flies, drink the sugar water, breed, lay more maggots, and die either on the lamp or of old age. The artwork is a working farm of birth and death, running 24 hours a day, and every visitor watches a tiny life-cycle complete. Hirst has said the piece is 'about life and death, about being trapped in a system.' The flies that die on the lamp leave a growing pile at the bottom — the accumulating dead are part of the sculpture, so the artwork literally changes as you watch it.",
        "A Thousand Years (1990) — watch the fly life-cycle complete",
        ["Contemporary", "Installation"],
    ),
    "artw-the-holy-virgin-mary-197": _entry(
        "Chris Ofili (1996)",
        "The Holy Virgin Mary (1996)",
        "Ofili's painting of the Virgin Mary made with glitter, resin, and elephant dung — one of the most controversial artworks of the 1990s. It became the center of a censorship battle in New York in 1999, when Mayor Giuliani threatened to cut the museum's funding because the painting offended him — and visitors lined up around the block to see what the fuss was about.",
        "Look at the figure first: Mary is a Black woman made from layered paint, glitter, and map pinheads, standing inside a golden halo — she's an African Virgin, deliberately remaking the sacred image. Then the materials: two lumps of elephant dung are part of the composition (one supports the canvas, one sits on the halo), which Ofili said was about 'keeping the painting grounded, close to the earth.' Ofili, who is British-Nigerian, said the work was 'a celebration' of the Black Madonna, and the censorship fight made it the most discussed painting of its decade — it's now in the Brooklyn Museum's collection.",
        "The Holy Virgin Mary (1996) — the African Madonna and the dung",
        ["Contemporary", "Oil Painting"],
    ),
    "artw-rhythm-0-1974-198": _entry(
        "Marina Abramović (1974)",
        "Rhythm 0 (1974)",
        "Abramović's performance in which she stood motionless for six hours while the audience was given 72 objects — flowers, honey, scissors, and a loaded gun — and told they could use them on her however they wished. The crowd started gently, then cut her clothes, cut her skin, and finally the gun was aimed at her neck before a fight broke out and the performance was stopped. She said afterward: 'I learned that if you leave it to the audience, they will kill you.'",
        "Look at the objects first: 72 items on a table — the harmless (feather, grapes, bread) and the dangerous (knife, chain, a loaded pistol with one bullet). Abramović stood still for six hours while strangers did whatever they chose: some fed her, some dressed her wounds, and the performance only ended when the gun appeared. The piece is a brutal experiment in human nature, and Abramović said it 'exposed what happens when there are no consequences.' The performance survives only in photographs and film — the bruises, the tears, and the small kindnesses are the artwork.",
        "Rhythm 0 (1974) — the 72 objects and the six hours",
        ["Performance Art", "Conceptual"],
    ),
    "artw-the-artist-is-present-199": _entry(
        "Marina Abramović (2010)",
        "The Artist Is Present (2010)",
        "Abramović sat silently in a chair in MoMA's atrium for 736 hours and 30 minutes, across three months, while visitors took the chair opposite and sat looking at her. More than 1,500 people sat with her; many wept, and one visitor, a former lover she hadn't seen in 22 years, sat down and the two held hands across the table — the most famous moment in performance art history.",
        "Look at the ritual: the setup is minimal — two chairs, a table, and an artist who doesn't move, speak, or react. Each visitor sits, and the two stare at each other until the visitor leaves; the artwork is the accumulated silence of 1,500 encounters. Abramović later said the piece taught her 'that I can do anything with my mind,' and that the hardest part was not the stillness but the emotion of strangers. The reunion with her former partner Ulay, who sat down unannounced after 22 years, made the front pages of newspapers worldwide — the moment two people who had broken up sat and held hands across a table.",
        "The Artist Is Present (2010) — sit in the silence for ten minutes",
        ["Performance Art", "Conceptual"],
    ),
    "artw-your-gaze-hits-200": _entry(
        "Barbara Kruger (1981)",
        "Untitled (Your Gaze Hits the Side of My Face) (1981)",
        "Kruger's photomontage of a marble female bust photographed in profile, with the words 'Your Gaze Hits the Side of My Face' printed in white-on-red over the image. She made it in her early 30s, using a style borrowed from advertising — bold type, cropped photos, declarative sentences — to turn the language of selling against the culture that sells.",
        "Look at the words first: they're printed in the aggressive style of magazine ads — white Futura type on a red field — and they describe what's happening to the bust: the viewer's gaze is hitting it like an object. Then the image: a classical marble head in profile, the kind of object museum visitors stare at without thinking. Kruger's sentences are confrontations — 'your gaze hits the side of my face' makes looking itself the subject. She worked as a graphic designer for magazines before making art, and her style — borrowed from advertising — is the joke: she uses the language of consumer culture to criticize how culture looks at women.",
        "Untitled (Your Gaze Hits the Side of My Face) (1981) — the ad-style words",
        ["Feminist Art", "Photomontage"],
    ),
    "artw-i-shop-therefore-201": _entry(
        "Barbara Kruger (1987)",
        "I Shop Therefore I Am (1987)",
        "Kruger's most famous slogan — a red-framed photograph of a hand holding a card that reads 'I shop therefore I am,' a twist on Descartes's 'I think therefore I am.' The image was everywhere in the 1980s: on T-shirts, posters, and tote bags, and it's become the shorthand for consumer culture's hold on identity.",
        "Look at the hand: it's cropped, anonymous, and holds the card the way a passport holder holds an ID — the card is the proof of existence. Then the slogan: Descartes said 'cogito ergo sum' — I think, therefore I am — and Kruger replaces thinking with shopping, so identity is defined by what you buy. The design is borrowed from advertising: bold type, red frame, flat colors. Kruger, who was a magazine designer before becoming an artist, said her work 'deals with how we are formed by the culture we live in' — and the slogan was so catchy it became the decade's unofficial motto.",
        "I Shop Therefore I Am (1987) — the card that proves you exist",
        ["Feminist Art", "Conceptual"],
    ),
    "artw-one-and-three-chairs-202": _entry(
        "Joseph Kosuth (1965)",
        "One and Three Chairs (1965)",
        "Kosuth's installation shows a real wooden chair, a photograph of that chair, and a dictionary definition of 'chair' — the same object presented three ways. It's the founding work of Conceptual Art, because the 'art' is not the chair, the photo, or the text — it's the idea that connects all three.",
        "Look at the three parts: a real chair, a photo of the chair, and the dictionary entry for 'chair' — each one claims to be 'a chair' in a different language: object, image, and word. Then the question the work asks: which one is the real chair? The answer — none, or all three — is the artwork's point. Kosuth said 'the art is the idea,' and that the physical objects were just documentation. The installation has been re-created with different chairs in different museums — the actual 'work' travels as an idea, which is the whole argument.",
        "One and Three Chairs (1965) — the chair, the photo, and the word",
        ["Conceptual Art", "Installation"],
    ),
    "artw-the-clock-2010-203": _entry(
        "Christian Marclay (2010)",
        "The Clock (2010)",
        "Marclay's video is a 24-hour film made from thousands of movie clips — every single shot shows a clock or watch, and the time shown in each clip matches the real time of day as you watch it. If the film shows a clock saying 3:47, it is actually 3:47 wherever you're watching. It took three years and 12,000 hours of footage to assemble.",
        "Look at the correspondence: the film runs for 24 hours, and at any moment the clock in the clip matches the real time — if you check your watch and look up, the screen agrees. Marclay and his assistants watched thousands of films, collecting every scene with a visible timepiece, then edited them into a seamless day. The result turns cinema into a clock: viewers report losing track of hours as the film hypnotizes them. It won the Golden Lion at the 2011 Venice Biennale, and museums show it around the clock, where it functions as a work of art and a functioning clock at the same time.",
        "The Clock (2010) — check your watch against the screen",
        ["Video Art", "Found Footage"],
    ),
    "artw-the-lightning-field-204": _entry(
        "Walter De Maria (1977)",
        "The Lightning Field (1977)",
        "Walter De Maria's installation in the New Mexico desert: 400 stainless-steel poles arranged in a grid over one mile by one kilometer, each pole 5 to 8 meters tall, waiting for lightning. Visitors stay overnight in a small cabin on the site, and in thunderstorm season the poles attract bolts — but most visitors see the field still, and the stillness is the art.",
        "Look at the scale first: 400 poles of polished steel across a grid measuring one mile by one kilometer, each pole's tip sharpened to a point, standing in an empty desert. In summer storms, the poles attract lightning — but most visitors see them calm, and De Maria said the field is 'about the land, and about seeing.' The site is open only from May to October, limited to six visitors at a time, and you must stay overnight; the experience is built on waiting, silence, and the desert light. The work cost $800,000 to build in 1977, is maintained by the Dia Art Foundation, and its poles — visible for miles — turn a landscape into a measure of the sky.",
        "The Lightning Field (1977) — the 400 poles in the desert",
        ["Land Art", "Minimalism"],
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
        "artw-lhooq-1919-156": "Sculpture",
        "artw-le-dejeuner-en-fourrure-186": "Sculpture",
        "artw-puppy-1992-193": "Sculpture",
        "artw-tulips-1995-194": "Sculpture",
        "artw-for-the-love-of-god-195": "Sculpture",
        "artw-a-thousand-years-196": "Installation",
        "artw-rhythm-0-1974-198": "Installation",
        "artw-the-artist-is-present-199": "Installation",
        "artw-one-and-three-chairs-202": "Installation",
        "artw-the-clock-2010-203": "Installation",
        "artw-the-lightning-field-204": "Installation",
        "artw-wrapped-reichstag-191": "Installation",
        "artw-running-fence-192": "Installation",
        "artw-your-gaze-hits-200": "Photograph",
        "artw-i-shop-therefore-201": "Photograph",
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
