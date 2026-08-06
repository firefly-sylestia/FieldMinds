#!/usr/bin/env python3
"""Batch 7: add 50 new handcrafted artworks to artworks.json (ids 405-454).

Impressionist & Post-Impressionist gaps (La Grenouillère, Gare Saint-Lazare,
Woman with a Parasol, Bathers at Asnières, van Gogh's Yellow House & At
Eternity's Gate, Gauguin's Tahiti works, Lautrec's La Goulue), early
modernism (The Large Glass, White on White, Twittering Machine, Senecio),
Expressionism (Munch, Schiele, Klimt), Surrealism (Dalí nuclear period),
Pop (Warhol Flowers & Race Riot, Lichtenstein In the Car, Rauschenberg Bed,
Johns Painted Bronze), Abstract Expressionism (de Kooning Woman I, Pollock
Full Fathom Five, Frankenthaler Mountains and Sea), Minimalism (Judd Stack),
and Land Art (Double Negative, Roden Crater).

All names verified unique against the existing 356 entries. Real fun facts,
handcrafted teasers, personal-voice instructions (no template scaffolding).
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
    # ---------- Impressionism & Post-Impressionism ----------
    "artw-la-grenouillere-405": _entry(
        "Pierre-Auguste Renoir (1869)",
        "La Grenouillère (1869)",
        "The riverside bathing spot on the Seine where Renoir and Monet set up easels side by side in the summer of 1869 — painting the same floating café, the same bathers, the same rippling water. The two near-identical canvases are considered the birthplace of Impressionism: loose strokes, broken color, light caught at speed. Renoir's version hangs in Stockholm, Monet's in New York.",
        "Compare it to Monet's nearly identical canvas from the same spot if you can — the two friends painted side by side and the differences show each painter's instinct. The water is the subject: short broken strokes of blue, green, and white instead of smooth blends, the way light actually shatters on a moving surface. The island café with its little gangplank is where Parisians went to see and be seen, and this is the exact moment the loose brushwork became a movement.",
        "La Grenouillère (1869) — the floating café and the broken water",
        ["Impressionism", "Landscape"],
    ),
    "artw-woman-with-a-parasol-406": _entry(
        "Claude Monet (1875)",
        "Woman with a Parasol (1875)",
        "Monet's wife Camille and son Jean, caught mid-walk on a windy hillside — the parasol tilted against the sun, her dress whipping, her face a few quick strokes rather than a portrait. Painted outdoors in one sitting at Argenteuil, it is Monet's happiest picture: a family moment, a gust of wind, and the whole scene dissolving into moving light.",
        "Stand in the wind with her: the parasol is angled against the sun, Camille's dress and veil stream sideways, and her face is barely there — a handful of strokes, because the moment mattered more than the likeness. Everything is in motion, from the clouds to the grass, and Monet painted it on the spot in one go. Then notice the view from below: we are looking up the hill at her, the sky taking half the canvas, so the family feels small against the weather and the light.",
        "Woman with a Parasol (1875) — the windy hillside and the moving light",
        ["Impressionism", "Figure"],
    ),
    "artw-gare-saint-lazare-407": _entry(
        "Claude Monet (1877)",
        "Gare Saint-Lazare (1877)",
        "Monet dressed in his best suit, invented a painter's name, and talked his way into Paris's busiest train station to paint it — the first time an artist was allowed inside. He painted a dozen canvases of the glass-and-iron shed filled with steam and smoke, the locomotives looming out of the haze, the light turning gold in the vapor. The modern city, painted like a cathedral.",
        "Feel the steam first: Monet painted this station a dozen times in 1877, and here the smoke is the real subject — it fills the glass roof, glows gold against the light, and half-hides the locomotives like weather. He got permission by dressing up and announcing a false painter's name, then had the trains kept running so he could catch them at speed. The iron columns and glass roof make the station a cathedral of the industrial age, and the anonymous crowd on the platform is the new city.",
        "Gare Saint-Lazare (1877) — the steam-filled train shed",
        ["Impressionism", "Cityscape"],
    ),
    "artw-boulevard-montmartre-at-night-408": _entry(
        "Camille Pissarro (1897)",
        "Boulevard Montmartre at Night (1897)",
        "Pissarro, nearly blind in one eye and 67 years old, rented a room above the Boulevard Montmartre and painted the same street fourteen times — morning, dusk, rain, and night. This is the night view: the first electric streetlights turning the avenue into rivers of gold, carriages dissolving into the glow, the whole modern city hum. It is one of the first great paintings of the electrified night.",
        "Watch the new light take over: this is the electric streetlamp, barely a decade old, turning the boulevard into pools of gold and the wet pavement into mirrors. Pissarro painted this same street fourteen times from a rented hotel room — snow, sun, fog — and the night version is the strangest because the city is turning into something never seen before: a modern street glowing after dark. The carriages shrink into the light, the lamps repeat into the distance, and the whole painting hums.",
        "Boulevard Montmartre at Night (1897) — the first electric lights",
        ["Impressionism", "Cityscape", "Night"],
    ),
    "artw-bathers-at-asnieres-409": _entry(
        "Georges Seurat (1884)",
        "Bathers at Asnières (1884)",
        "Workers cooling off in the Seine at a factory suburb of Paris — the calm, monumental study for La Grande Jatte, painted when Seurat was 24 and rejected by the Salon. The bodies are simplified into stillness, the water painted in dots and dashes of color that blend in the eye. It is the first great canvas of pointillism, three meters wide, and nobody bought it for years.",
        "Let your eye do the mixing: the water, the grass, and the boys are built from thousands of separate dots and dashes of pure color that only blend into a scene at a distance — Seurat called it optical mixing, and it was a science experiment as much as a painting. The boys are industrial workers from the suburb of Asnières, resting on a Sunday, and their stillness is the point: flat, monumental, timeless. The rejected canvas sat unsold for years before becoming a foundation stone of modern art.",
        "Bathers at Asnières (1884) — the dotted Seine and the still workers",
        ["Post-Impressionism", "Pointillism"],
    ),
    "artw-the-yellow-house-410": _entry(
        "Vincent van Gogh (1888)",
        "The Yellow House (1888)",
        "The house in Arles where van Gogh dreamed of founding a 'studio of the south' — the place Gauguin was meant to join him, where the two would paint together and the friendship would end in the ear incident. Van Gogh painted it weeks after moving in, the yellow walls blazing against a blue sky, the night café next door visible. The house was destroyed by an Allied bomb in 1944; only this painting survives.",
        "Walk up to the house he rented: this is 2 Place Lamartine in Arles, painted weeks after van Gogh moved in, when the 'studio of the south' was still a dream and Gauguin was still coming. The yellow walls blaze against the cobalt sky — yellow was his color of hope and sun. The building next door with the awning is the night café he painted separately, and the house was destroyed by a bomb in 1944, so the painting is the only address left.",
        "The Yellow House (1888) — the studio of the south",
        ["Post-Impressionism", "Architecture"],
    ),
    "artw-at-eternitys-gate-411": _entry(
        "Vincent van Gogh (1890)",
        "At Eternity's Gate (1890)",
        "An old man in a poorhouse chair, head buried in his hands, weeping — van Gogh's copy of his own 1882 drawing, painted at Auvers in the last months of his life. He described the subject as 'an old man who is the eternal type of those who are in a state of mourning.' The book cover of a famous van Gogh biography, and one of the most painful self-portraits-by-proxy ever painted.",
        "Sit with the old man: he is an inmate of an almshouse, and van Gogh painted him with his head in his hands, elbows on his knees, the whole body a knot of grief. He copied this from his own 1882 drawing years later, in the last months of his life, and said the figure was 'the eternal type of those who are in a state of mourning.' The color is warm — a blue coat, an orange floor — but nothing moves, and the chair is turned away from us, as if we shouldn't be watching.",
        "At Eternity's Gate (1890) — the man with his head in his hands",
        ["Post-Impressionism", "Figure"],
    ),
    "artw-the-vision-after-the-sermon-412": _entry(
        "Paul Gauguin (1888)",
        "The Vision After the Sermon (1888)",
        "Breton peasant women leaving church, still possessed by the sermon, see Jacob wrestling the angel in a field — and Gauguin paints the vision with no perspective: the red field tips up like a wall, the wrestling figures float above the women, everything flat and impossible. Painted in Brittany a year before he left for Tahiti, it is the manifesto of his 'synthetist' style: color as emotion, not description.",
        "Stand in the red field: the ground is a flat, tipped-up expanse of vermilion that refuses all depth, and the wrestling Jacob and angel float above the peasant women as if weightless. That is the point — the vision happens inside the women's heads after the sermon, so it can't obey the rules of space. The apple tree cuts the canvas diagonally, the white Breton caps anchor the foreground, and Gauguin was telling Europe: color and flatness can carry meaning without pretending to be real.",
        "The Vision After the Sermon (1888) — the red field of faith",
        ["Post-Impressionism", "Symbolism"],
    ),
    "artw-spirit-of-the-dead-watching-413": _entry(
        "Paul Gauguin (1892)",
        "Spirit of the Dead Watching (1892)",
        "Gauguin's first Tahitian masterpiece: his young wife Tehura lies naked on the bed, face-down and terrified, while behind her a dark spirit — a tupapau — watches with glowing eyes. He painted the fear he had actually seen: coming home late and finding her convinced a ghost was in the room. The purple sheet glows, the background is invented, and the painting is as much about his own guilt as her terror.",
        "Understand what is watching: the dark figure behind the bed is a tupapau, a Tahitian spirit of the dead, and Gauguin painted this after coming home late to find his young wife Tehura frozen with fear that a ghost was in the room. She lies face down, the glow of her skin against the purple sheet — he said the glow was the light of fear. The background and the flowers are invented, pure decoration, and the whole image hovers between a portrait of terror and a confession of his own absence.",
        "Spirit of the Dead Watching (1892) — the fear in the room",
        ["Post-Impressionism", "Symbolism"],
    ),
    "artw-moulin-rouge-la-goulue-414": _entry(
        "Henri de Toulouse-Lautrec (1891)",
        "Moulin Rouge: La Goulue (1891)",
        "The poster that made Toulouse-Lautrec famous overnight — La Goulue ('the glutton,' the can-can star famous for drinking customers' glasses), twirling in her petticoats, with the boneless contortionist Valentin in silhouette behind her. Three thousand copies papered the streets of Paris in 1891, and the poster is now considered the first great work of graphic design. It launched the art of the billboard.",
        "Read it like a poster on a Paris wall in 1891: three thousand of these were pasted up overnight, and the city stopped to stare — La Goulue, the can-can star, kicks in her petticoats while the contortionist Valentin waits in black silhouette. Lautrec drew from the actual show night after night, sketching his friends on stage. The flat red and black shapes, the lettering as part of the design, the way the dancer fills the frame — this poster invented the modern billboard, and it made him a star.",
        "Moulin Rouge: La Goulue (1891) — the poster that papered Paris",
        ["Post-Impressionism", "Poster"],
    ),
    # ---------- Early Modernism ----------
    "artw-the-fifer-415": _entry(
        "Édouard Manet (1866)",
        "The Fifer (1866)",
        "A boy in the imperial guard playing a fife, painted flat — no shading on his body, no depth behind him, just a pale grey wall. Critics called it 'a painting of a cardboard figure,' and Manet answered by doubling down: the flatness was the point. Inspired by Velázquez and Japanese prints, The Fifer is the moment painting admitted it was a flat surface — and the doorway to everything that came after.",
        "Notice what is missing: the boy's body has almost no shading — no shadows on his uniform, no depth behind him, just a flat pale wall. The 1866 Salon called him a cardboard cutout, and Manet didn't care: he was showing that a painting is a flat surface, not a window. The single red stripe down the trouser, the black cap, the silver buttons — every shape is a clean silhouette. Half a century later this flatness became modernism, and it started here with a boy playing a fife.",
        "The Fifer (1866) — the boy and the flat wall",
        ["Impressionism", "Figure"],
    ),
    "artw-la-vie-416": _entry(
        "Pablo Picasso (1903)",
        "La Vie (1903)",
        "The great canvas of Picasso's Blue Period — a naked couple embracing, a mother holding a child, and a mysterious cloaked figure pointing. Underneath the surface, X-rays reveal Picasso painted over an earlier self-portrait: his own face once looked out from the mother's place. The blue is poverty, grief, and the years when Picasso painted the dispossessed of Barcelona.",
        "Walk through the three figures like a riddle: a naked couple clings together on the left, a mother with an infant on the right, and behind them a cloaked figure points a finger that no one follows. X-rays show Picasso painted his own self-portrait underneath, then covered it — so the picture is a palimpsest of his own despair. The single blue palette does the emotional work: no reds, no warmth, just the cold tone of poverty in the Barcelona of 1903. Scholars still argue what the pointing figure means.",
        "La Vie (1903) — the blue riddle",
        ["Cubism", "Blue Period"],
    ),
    "artw-family-of-saltimbanques-417": _entry(
        "Pablo Picasso (1905)",
        "Family of Saltimbanques (1905)",
        "A troupe of traveling acrobats — harlequin, clown, children — standing in an empty landscape, each one alone even in the group. Painted at the hinge between Picasso's Blue and Rose periods, it is his farewell to the outcasts who peopled his early work: performers who belong nowhere and perform for no one. The painting hangs in Washington's National Gallery, and the harlequin is Picasso's stand-in for himself.",
        "Count the distances: six performers stand in a row across the canvas, and none of them touches another — the harlequin, the pierrot, the children, each isolated in the empty landscape. Picasso painted this as his Blue Period was warming into Rose, and the troupe is his self-portrait of the artist as outsider: people who perform for crowds and belong nowhere. The sky is pale, the ground bare, and the group is a family only in name — the loneliness is the subject.",
        "Family of Saltimbanques (1905) — the acrobats who belong nowhere",
        ["Cubism", "Rose Period"],
    ),
    "artw-the-joy-of-life-418": _entry(
        "Henri Matisse (1906)",
        "The Joy of Life (1906)",
        "Matisse's Fauvist manifesto: nudes dancing, piping, and lounging in an Arcadian grove where the sky is pink, the ground is orange, and the trees are green spirals. Critics were scandalized; Picasso was so stung by it he painted Les Demoiselles d'Avignon the next year to compete. 'The Joy of Life' is art's great dream of happiness — a paradise painted in colors that never existed.",
        "Let the colors argue with each other: a pink sky, an orange field, green spiraling trees, and a circle of naked figures dancing in the middle — Matisse threw out every rule of how a landscape should look and kept only the feeling. The 1906 Salon was scandalized, and the story goes that Picasso, seeing it, went home and started Les Demoiselles d'Avignon to answer it. This is the dream of Eden painted by a man who believed painting should be a comfortable armchair — paradise, on purpose.",
        "The Joy of Life (1906) — the impossible paradise",
        ["Fauvism", "Allegory"],
    ),
    "artw-blue-nude-419": _entry(
        "Henri Matisse (1907)",
        "Blue Nude (Souvenir de Biskra) (1907)",
        "Matisse sculpted a reclining figure in clay, then painted it from an odd angle — the twisted, over-articulated body scandalized Paris when it was shown, with its pink shadow and contorted pose. It was burned in effigy by students in Chicago years later. The Blue Nude is Fauvism pushed to its breaking point: a woman turned into pure shape and heat.",
        "Trace the impossible curve: the woman's body twists in ways a spine shouldn't — the arm reaching back, the head turned away, the legs folded at odd angles. Matisse built the pose from a small sculpture he'd made, then painted it from a deliberately strange viewpoint, distorting the figure into pure design. The pink shadow under her and the blue body against the orange ground are colors that fight and sing at once. Chicago art students burned it in effigy in 1913; it has outlived them all.",
        "Blue Nude (Souvenir de Biskra) (1907) — the twisted body",
        ["Fauvism", "Nude"],
    ),
    "artw-goldfish-420": _entry(
        "Henri Matisse (1912)",
        "Goldfish (1912)",
        "Matisse kept goldfish and painted them again and again — the bowl, the water, the glass, the refraction — fascinated by how light bends through three substances at once. Here the fish swim in a blue-green world of plants and a round table, painted during his Moroccan-influenced years. It is a painting about looking: the fish are seen from above and from the side at the same time.",
        "Count the views at once: the goldfish bowl is seen from above — the surface with its reflections — and from the side at the same moment, two realities in one image. Matisse kept fish in his studio and painted them obsessively, fascinated by how light passes through water and glass and bends the fish inside. The blue-green world around them, the round table, the trailing plants, the vertical strokes of the background — everything is arranged so the eye swims, and the fish are the calm orange heart of the picture.",
        "Goldfish (1912) — the bowl and the bending light",
        ["Fauvism", "Still Life"],
    ),
    "artw-the-large-glass-421": _entry(
        "Marcel Duchamp (1915-23)",
        "The Bride Stripped Bare by Her Bachelors, Even (The Large Glass) (1915-23)",
        "Duchamp's great enigma: a nine-foot glass divided into two panels — the Bride in her halo above, the Bachelor Machine of nine 'malic molds' below, joined by a network of wires and sieves. He worked on it for eight years, declared it 'definitively unfinished,' and when the glass cracked in shipping he said the cracks completed the work. Nobody agrees what it means; that is the point.",
        "Read the two halves like a machine for a joke: above, the Bride hangs in a milky halo; below, nine 'malic molds' — the bachelors — stand in their glass uniforms, wired to sieves, scissors, and a chocolate grinder. Duchamp spent eight years on it, then declared it 'definitively unfinished.' When the glass cracked in transit he announced the cracks were part of the work — chance had signed it. Nobody has decoded it, and that was the design.",
        "The Large Glass (1915-23) — the bride and her bachelors",
        ["Dada", "Installation"],
    ),
    "artw-yellow-red-blue-422": _entry(
        "Wassily Kandinsky (1925)",
        "Yellow-Red-Blue (1925)",
        "Kandinsky's grand statement of his color theory: three fields — yellow, red, blue — each with its own emotional temperature, overlaid with geometric and drifting forms. Yellow he called warm and aggressive, blue calm and spiritual, red restless. Painted at the Bauhaus, it is abstract art as a science of feeling: shapes as sounds, colors as moods.",
        "Feel the three temperatures: yellow pushes toward you — Kandinsky called it warm, sharp, almost aggressive; blue pulls away, calm and spiritual; red vibrates between them. He painted this at the Bauhaus in 1925, teaching a theory that color and shape are sounds and moods. The left side is all geometry and sharpness, the right side drifts into floating forms — the painting is a lecture about the soul, delivered in pure paint.",
        "Yellow-Red-Blue (1925) — the science of feeling",
        ["Abstract", "Bauhaus"],
    ),
    "artw-white-on-white-423": _entry(
        "Kazimir Malevich (1918)",
        "White on White (1918)",
        "The end of the road for Malevich's Suprematism: a white square floating on a slightly whiter field, painted with nothing but white on white. He called it the 'final painting' — art reduced to the absolute zero of form and color, leaving only the feeling of space. The square is tilted by a few degrees, and the whole canvas is a barely-there universe of difference.",
        "Give it a full minute: a white square floats on a white field, and the only thing to look at is the difference — the tilt, the brushstrokes, the faint seam where one white meets another. Malevich called this the final painting of art: after the black square came the red, and then this, the zero point where color disappears and only space and feeling remain. It looks like nothing, which was exactly the point — the end of painting as the world knew it.",
        "White on White (1918) — the zero of painting",
        ["Abstract", "Suprematism"],
    ),
    "artw-twittering-machine-424": _entry(
        "Paul Klee (1922)",
        "Twittering Machine (1922)",
        "Four birds perch on a wire cranked by a hand wheel — a little machine for making birdsong, drawn in a few wiry lines. But the joke is darker than it looks: the birds are trapped, the crank is the handle of a birdcatcher's device, and the song they 'twitter' is a snare. Klee's tiny, poisonous cartoon is one of the most reproduced images in modern art.",
        "Work the crank in your head: a hand wheel turns a wire on which four birds stand, their beaks open — they are twittering, but the machine is a snare, and the handle is there to turn the trap. Klee built the whole thing from a few wiry lines, ink and watercolor, and the lightness hides the cruelty: the birds sing because they cannot fly away. It has been reproduced more than almost any modern drawing — a small machine that caught the world's imagination.",
        "Twittering Machine (1922) — the birds and the crank",
        ["Expressionism", "Drawing"],
    ),
    "artw-senecio-425": _entry(
        "Paul Klee (1922)",
        "Senecio (1922)",
        "A face built from geometric planes — a mask with segmented forehead, cheeks, and eyes, one brow arched, the colors glowing against a deep background. 'Senecio' is Latin for 'old man,' and Klee built the head from the same language of squares and triangles he taught at the Bauhaus: a portrait reduced to architecture, both playful and unsettling.",
        "Read the face as a building: forehead, cheeks, and chin are separate colored planes — orange, red, ochre — fitted together like masonry, with two dark eyes and one raised eyebrow giving it a sly, ancient expression. Klee taught color theory at the Bauhaus when he painted this, and the head is a demonstration: a portrait built from pure geometry, half mask, half old man. The circles and angles hover between cartoon and icon.",
        "Senecio (1922) — the geometric mask",
        ["Expressionism", "Portrait"],
    ),
    # ---------- Expressionism ----------
    "artw-puberty-426": _entry(
        "Edvard Munch (1894-95)",
        "Puberty (1894-95)",
        "A naked adolescent girl sits on the edge of a bed, hands clasped in her lap, staring straight at us while a great dark shadow looms behind her. Painted over and over by Munch, who was haunted by the moment childhood ends. The shadow is everything she is about to become — sexuality, anxiety, the adult world — and the painting made Munch notorious.",
        "Meet her stare: a girl on the edge of a bed, naked, hands clasped in her lap, looks straight at you with a gaze that is half defiance and half terror — while behind her a dark mass rises like a door opening. Munch painted this subject repeatedly, obsessed with the instant childhood ends. The bed, the sheets, the shadow: every element is stripped to its simplest form, and the painting's power is that nothing is explained. It made him notorious in 1895 and it still holds.",
        "Puberty (1894-95) — the girl and the shadow",
        ["Expressionism", "Figure"],
    ),
    "artw-vampire-427": _entry(
        "Edvard Munch (1893-94)",
        "Vampire (1893-94)",
        "A woman with flowing red hair bends over a man, her lips pressed to his neck — originally titled 'Love and Pain,' renamed 'Vampire' by a friend, and it stuck. The red hair pours down like blood, the embrace is both comfort and consumption, and Munch painted six versions. Love as a wound: the most tender horror painting ever made.",
        "Find the blood before the kiss: the woman's red hair pours down over the man's shoulders and neck like a wound, and her face against his skin is both an embrace and a bite. Munch titled it 'Love and Pain'; a friend renamed it 'Vampire,' and the new title won. He painted six versions over twenty years, and the image keeps its ambiguity — is she comforting him or feeding? The bodies melt into one dark shape, and the red hair is the only color in the room.",
        "Vampire (1893-94) — the red hair and the kiss",
        ["Expressionism", "Symbolism"],
    ),
    "artw-portrait-of-wally-428": _entry(
        "Egon Schiele (1912)",
        "Portrait of Wally (1912)",
        "Wally Neuzil, Schiele's lover and model — they met when she was seventeen, and he painted her with an unflinching directness that shocked Vienna: pale skin, dark circles, a gaze that holds nothing back. Schiele later left her for his wife; Wally died of scarlet fever in 1917, and Schiele died of Spanish flu the next year. The painting was looted by Nazis and spent a decade in a famous restitution battle.",
        "Hold her gaze: Wally Neuzil was seventeen when she became Schiele's model and lover, and he painted her with a directness Vienna found scandalous — the pale face, the dark eyes, the bare shoulder against the dark ground. Schiele left her to marry another woman; she died of scarlet fever in 1917 and he died of Spanish flu the next year. The portrait was stolen by the Nazis and spent decades in courtrooms over restitution — a face that carried a century of argument.",
        "Portrait of Wally (1912) — the unflinching gaze",
        ["Expressionism", "Portrait"],
    ),
    "artw-judith-and-the-head-of-holofernes-429": _entry(
        "Gustav Klimt (1901)",
        "Judith and the Head of Holofernes (1901)",
        "Klimt's Judith — gold, bare-shouldered, eyes half-closed with triumph — holds the severed head of Holofernes at arm's length. Vienna immediately mistook her for Salome, the femme fatale; Klimt insisted she was Judith, the biblical heroine. The gold-leaf background and the erotic charge made it the sensation of the Secession, and the painting still reads as a portrait of feminine power wearing its victory like jewelry.",
        "Look at her face before the head: eyes half-closed, lips parted, a gold collar tight at her throat — she looks not triumphant but intoxicated, holding the severed head as if it were a handbag. Vienna assumed she was Salome; Klimt insisted she was Judith, the widow who saved her people. The gold leaf is hammered into the background like jewelry, her bare shoulder gleams, and the head is painted with a grim realism that makes the fantasy bite.",
        "Judith and the Head of Holofernes (1901) — the gold and the head",
        ["Art Nouveau", "Symbolism"],
    ),
    "artw-the-tree-of-life-stoclet-frieze-430": _entry(
        "Gustav Klimt (1909)",
        "The Tree of Life (Stoclet Frieze) (1909)",
        "Klimt's only major architectural commission outside Vienna: a mosaic frieze for a Brussels mansion, its center a great spiraling tree with gold branches — black trunks twisting into curls of gold and enamel, with a standing female figure and a pair embracing in the side panels. The swirl of the tree has been borrowed by a thousand designers; it is Klimt's cosmos as a garden.",
        "Follow one spiral and get lost: the tree's trunk twists into curling gold branches that never quite repeat, an abstract forest of enamel, gold, and semiprecious stone that Klimt designed for a Brussels dining room in 1909. It is his cosmos as a garden — the spirals suggesting growth, time, and eternity. On the left a woman stands in a jeweled dress; on the right a couple embraces in a shower of gold. The whole frieze is about love, and the tree is its heartbeat.",
        "The Tree of Life (Stoclet Frieze) (1909) — the golden spiral",
        ["Art Nouveau", "Mosaic"],
    ),
    "artw-dream-caused-by-the-flight-of-a-bee-431": _entry(
        "Salvador Dalí (1944)",
        "Dream Caused by the Flight of a Bee Around a Pomegranate a Second Before Awakening (1944)",
        "Dalí's chain-reaction dream: a sleeping Gala floats above a rock; from a pomegranate bursts a fish, from the fish's mouth two tigers and a rifle with a bayonet — all triggered, Dalí explained, by the sound of a bee, the way a single sting can ignite a whole dream. Painted for a dream sequence in a Hitchcock film that was never made, it is Surrealism as an equation.",
        "Read it as a chain reaction: the bee (tiny, lower left) stings, and the sound detonates the dream — pomegranate, fish, tigers, rifle, all exploding from one another in sequence, while Gala sleeps suspended above the sea. Dalí said the image was the literal illustration of a dream mechanism: one sensory jolt, and the mind builds a narrative. He painted it for a Hitchcock dream sequence that was cut from the film, so the dream survives only here.",
        "The flight of a bee dream (1944) — the chain reaction",
        ["Surrealism", "Dream"],
    ),
    "artw-galatea-of-the-spheres-432": _entry(
        "Salvador Dalí (1952)",
        "Galatea of the Spheres (1952)",
        "Gala's face built entirely from spheres — thousands of painted balls of color that only assemble into a portrait from a distance. Painted in Dalí's 'nuclear mysticism' period, after Hiroshima, when he was trying to paint matter as particles and the atom as the new holy image. Stand close and it is dots; step back and it is his wife's face.",
        "Do the experiment the painting demands: stand close and it is thousands of colored spheres — particles of paint, no face at all. Step back and the spheres assemble into Gala, his wife's face, floating in a space that looks like an atomic diagram. Dalí painted this after Hiroshima, in his 'nuclear mysticism' phase, when he believed matter itself was the new sacred image. The whole portrait is a theory of the universe: everything is particles, and the face is what the particles become when you believe.",
        "Galatea of the Spheres (1952) — the face of particles",
        ["Surrealism", "Portrait"],
    ),
    "artw-self-portrait-with-cropped-hair-433": _entry(
        "Frida Kahlo (1940)",
        "Self-Portrait with Cropped Hair (1940)",
        "After divorcing Diego Rivera, Frida cut off her long hair, dressed in an oversized man's suit, and painted herself holding the scissors — strands of severed hair scattered around her. The lyrics at the top, from a Mexican song, say: 'Look, if I loved you it was because of your hair; now that you're bald, I don't love you anymore.' Defiance, grief, and reinvention in one canvas.",
        "Read the lyrics she wrote across the top: 'Look, if I loved you it was because of your hair; now that you're bald, I don't love you anymore' — a Mexican song, painted by Frida right after her divorce from Diego Rivera. She sits in an oversized man's suit, scissors in one hand, the shorn hair scattered in dark snakes around the chair. She cut it herself, and the painting is her middle finger to the man who loved her hair — she controls the portrait now, in his clothes.",
        "Self-Portrait with Cropped Hair (1940) — the scissors and the suit",
        ["Surrealism", "Self-Portrait"],
    ),
    "artw-viva-la-vida-watermelons-434": _entry(
        "Frida Kahlo (1954)",
        "Viva la Vida, Watermelons (1954)",
        "Kahlo's last painting — watermelons, the Mexican fruit of the Day of the Dead, split open to show their red flesh, painted days before she died at 47. She signed it with her name, the date, and 'Viva la Vida' — long live life — across the red heart of the melons. A still life that is also a farewell, full of the taste and color of Mexico.",
        "Notice what she signed: 'Viva la Vida — Frida Kahlo, 1954, Coyoacán' written across the red flesh of a cut watermelon, the Mexican fruit of the Day of the Dead. It was painted days before she died at 47, and it is a goodbye that refuses to be sad: the melons are split open, glowing red, seed-spattered, alive. The blue sky and the red flesh are the colors of Mexican celebration, and the painting says, in the voice of a woman who suffered her whole life, that life was worth it.",
        "Viva la Vida, Watermelons (1954) — the last melons",
        ["Surrealism", "Still Life"],
    ),
    "artw-man-at-the-crossroads-435": _entry(
        "Diego Rivera (1934)",
        "Man at the Crossroads (1934)",
        "Rivera's mural for Rockefeller Center in New York — a worker at the center of a wheel of modern life, with telescopes, factories, and a portrait of Lenin. Nelson Rockefeller asked Rivera to remove Lenin; Rivera refused, and the mural was destroyed in 1934. Rivera recreated it the next year in Mexico City as 'Man, Controller of the Universe.' The most famous destroyed painting of the 20th century.",
        "Stand at the center with the worker: the mural was a giant wheel of modern life for Rockefeller Center — telescopes, machines, crowds — and at its heart Rivera painted Lenin, which is why Nelson Rockefeller ordered it destroyed. Rivera refused to remove it, so the mural was demolished in 1934; he repainted it in Mexico City as 'Man, Controller of the Universe.' In the surviving version, look at the two hands reaching across the center — the crossroads of a world choosing its direction.",
        "Man at the Crossroads (1934) — the mural Rockefeller destroyed",
        ["Muralism", "Social Realism"],
    ),
    "artw-black-iris-436": _entry(
        "Georgia O'Keeffe (1926)",
        "Black Iris (1926)",
        "An iris blown up until it fills the whole canvas — dark velvet petals, a black heart, a flower the size of a landscape. Critics immediately read it as anatomy; O'Keeffe spent decades denying it. She said she painted flowers large because 'nobody sees a flower — really — it is so small,' and her big flowers made her the most famous woman artist in America.",
        "Get close enough that the flower becomes a landscape: O'Keefe enlarged this iris until its dark petals fill the canvas and its center becomes a black, velvet tunnel. She said she painted flowers big because nobody really sees a flower — it is so small — and the scale turns the petal into a mountain. The public insisted it was a portrait of a woman's body; she insisted it was a flower, and the argument is half the painting's life. Look at it without deciding, and it is simply enormous, fragile, and exact.",
        "Black Iris (1926) — the flower as landscape",
        ["Modernism", "Still Life"],
    ),
    # ---------- American Scene ----------
    "artw-automat-437": _entry(
        "Edward Hopper (1927)",
        "Automat (1927)",
        "A woman sits alone at a window in a coin-operated cafeteria at night, a cup of coffee before her, the black window behind her — one of the great images of urban loneliness. Hopper's wife Jo posed for it. The automat, where city workers ate alone in the glow of electric light, was the perfect stage for his favorite subject: people together in public, alone in private.",
        "Notice the window does the work: it is a black rectangle, not a view — the night outside is just darkness, so the woman's isolation is doubled, reflected in glass she can't see through. Hopper's wife Jo posed for this, sitting alone in an automat, the coin-operated cafeteria where the city ate without speaking. The two cups on the table (one is hers, one is the machine's), the glow of the lights, the winter coat — every detail is a small answer to the question of why the city is so full and so empty.",
        "Automat (1927) — the black window and the coffee",
        ["American Scene", "Urban"],
    ),
    "artw-house-by-the-railroad-438": _entry(
        "Edward Hopper (1925)",
        "House by the Railroad (1925)",
        "A lonely Victorian house stands beside an empty railroad track, cut off at the bottom of the canvas — Hopper's first great mature painting, and the image that made his name. The house has no people, no motion, no warmth; the tracks are empty; the sky is flat. Hitchcock's art director copied it directly for the Bates house in Psycho.",
        "Let the house stare back: a Victorian mansion stands alone above an empty railroad track, cropped at the bottom of the frame like a photograph, and nothing in it moves. Hopper painted this in 1925 and it made him famous — the architecture of loneliness, a house that looks lived-out. The tracks are empty, the sky is flat, no people, no weather. Hitchcock's set designer copied the house almost exactly for Psycho's Bates mansion, so you have seen this building in a nightmare.",
        "House by the Railroad (1925) — the house that became Psycho's",
        ["American Scene", "Architecture"],
    ),
    "artw-freedom-from-want-439": _entry(
        "Norman Rockwell (1943)",
        "Freedom from Want (1943)",
        "The Thanksgiving painting: a family around a table as the grandmother sets down the turkey, and everyone turns to face us. It is one of Rockwell's Four Freedoms, inspired by Roosevelt's 1941 speech, and the series sold $132 million in war bonds. The grandmother is Rockwell's real cook; the turkey took days to paint and the whole table is a portrait of a nation's self-image.",
        "Count the faces turned to you: every person at the table looks out at the viewer, not at the turkey — Rockwell's grandmother (actually his cook) is placing the bird, and the scene is less a dinner than a declaration. Painted in 1943 as one of the Four Freedoms after Roosevelt's speech, it became the most reproduced American image of its decade and helped sell $132 million in war bonds. The wholesome table was partly an ideal — rationing was real — but the painting's job was to show what the war was for.",
        "Freedom from Want (1943) — the Thanksgiving table",
        ["American Scene", "Illustration"],
    ),
    "artw-the-problem-we-all-live-with-440": _entry(
        "Norman Rockwell (1964)",
        "The Problem We All Live With (1964)",
        "Six-year-old Ruby Bridges walks to school in New Orleans escorted by four federal marshals, past a wall scrawled with racial slurs and a smashed tomato at her feet — Rockwell's first major civil-rights painting, published in Look magazine in 1964. Ruby, the first Black child to integrate her school, grew up to meet President Obama at the White House, where the painting now hangs.",
        "Walk with the smallest figure in American civil-rights art: six-year-old Ruby Bridges, in a white dress, carrying her books, flanked by four faceless federal marshals. The wall behind her carries the word no one in the painting says aloud, and a tomato has splattered at her feet. Rockwell, known for warm humor, painted this in 1964 for Look magazine, and it was his first major statement on race. Ruby survived that walk, grew up, and met President Obama in the Oval Office — the painting hangs there today.",
        "The Problem We All Live With (1964) — Ruby's walk to school",
        ["American Scene", "Civil Rights"],
    ),
    # ---------- Pop & Postwar ----------
    "artw-in-the-car-441": _entry(
        "Roy Lichtenstein (1963)",
        "In the Car (1963)",
        "A couple in a convertible — the woman crying, the man staring ahead — blown up from a comic-book panel with Ben-Day dots and a speech bubble that says 'I don't care! I'd rather sink — than call Brad for help!' Lichtenstein took melodrama from the funnies and made it monumental, and the dots are the point: the artificial made honest.",
        "Read the bubble out loud: 'I don't care! I'd rather sink — than call Brad for help!' — comic-book melodrama, blown up to a wall-sized canvas with the Ben-Day dots you can count from two feet away. Lichtenstein copied the panel from an actual comic and made the artificiality the subject: the dots, the primary colors, the frozen drama. The man stares ahead, the woman weeps, and the whole scene is a soap opera made of printed dots — high art quoting trash, and loving it.",
        "In the Car (1963) — the dots and the drama",
        ["Pop Art", "Comic"],
    ),
    "artw-flowers-442": _entry(
        "Andy Warhol (1964)",
        "Flowers (1964)",
        "Warhol's flowers — silk-screened hibiscus blooms cropped from a magazine photograph, repeated in candy colors across a green background. Made in his Factory, mass-produced like cans of soup, they were shown at the 1964 Venice Biennale as a suite. The flowers are pure surface: cheerful, decorative, and completely deadpan — nature turned into product.",
        "Look at the surface and ask where the feeling went: these flowers were cropped from a photography magazine, silk-screened by Factory assistants, and repeated in candy colors — hibiscus blooms as standardized as soup cans. The green background is a single flat field, the flowers have no stems or context, and each print is slightly misregistered, the silk-screen wobble showing through. Warhol showed them as a series at the 1964 Venice Biennale: nature, reproduced until it means nothing and glows forever.",
        "Flowers (1964) — the factory blooms",
        ["Pop Art", "Silkscreen"],
    ),
    "artw-race-riot-443": _entry(
        "Andy Warhol (1964)",
        "Race Riot (1964)",
        "Warhol's first political series: a Life magazine photo of Birmingham police dogs attacking a Black protester, repeated in harsh reds and blues. He was 'surprised' people were shocked, he said — he thought it was 'just a picture.' The repetition drains and sharpens the violence at once: the image is everywhere and nowhere, and the painting refuses to let you look away or look once.",
        "Feel the repetition do the violence: Warhol took one photo — police dogs attacking a Black protester in Birmingham — and printed it again and again in brutal red and blue, the image wobbling out of register like a damaged newspaper. He said he was surprised people found it shocking, that it was 'just a picture.' The repeating faces force the horror into a pattern, and the pattern is the point: America saw this photograph everywhere and kept looking. It is his angriest painting, disguised as indifference.",
        "Race Riot (1964) — the dogs and the repetition",
        ["Pop Art", "Political"],
    ),
    "artw-bed-444": _entry(
        "Robert Rauschenberg (1955)",
        "Bed (1955)",
        "A real quilt and pillow, stretched on a wooden frame and slashed with paint — Rauschenberg's own bed, hung on the wall as the first 'combine' between painting and sculpture. He was broke, he said, so the bed was the only canvas he had. The paint runs down the quilt like blood or like the stains of living, and the piece sits somewhere between painting, collage, and confession.",
        "Touch the history under the paint: this is Rauschenberg's actual bed — the quilt, the pillow, the sheet — hung vertically and covered with paint that drips and smears like a crime scene or a night of living. He called it a 'combine,' part painting, part sculpture, made in 1955 when he was too broke to buy canvas, so he used what he had. The pillow stays white and untouched at the top, the paint grows heavier toward the bottom, and the piece turns the most private object in a room into public art.",
        "Bed (1955) — the quilt as canvas",
        ["Pop Art", "Combine"],
    ),
    "artw-painted-bronze-445": _entry(
        "Jasper Johns (1960)",
        "Painted Bronze (1960)",
        "Two bronze objects cast from real things and painted to look real: a Savarin coffee can stuffed with brushes, and a pair of Ballantine ale cans. Johns made the ale cans after Willem de Kooning complained about a gallery owner, and the story goes a friend said 'if you make it, you can't buy it' — so he did. Bronze pretending to be tin: a joke about art, money, and seeing.",
        "Wait for the moment of double take: these look like a coffee can of brushes and two ale cans, but they are bronze — cast from the real objects and painted by hand to fool your eye. Jasper Johns made them around 1960, reportedly after a remark that if he made the cans himself he couldn't be charged gallery prices for them. The brush handles, the ale labels, the drips of paint — every detail is a tiny lie told in bronze, and the piece is a deadpan joke about how much things are worth.",
        "Painted Bronze (1960) — the bronze ale cans",
        ["Pop Art", "Sculpture"],
    ),
    "artw-woman-i-446": _entry(
        "Willem de Kooning (1952)",
        "Woman I (1952)",
        "De Kooning's Woman — painted, scraped, and repainted for two years, a figure built from furious strokes: huge eyes, toothy grin, body carved out of slabs of color. Critics called it a portrait of misogyny; de Kooning called it 'a woman,' grinning and monstrous and alive. It is the most fought-over painting of Abstract Expressionism: the human figure refusing to disappear from abstract art.",
        "Feel the fight in the paint: de Kooning worked on this one canvas for two years, painting it out and starting over until the surface is a battlefield of scraped and repainted layers. The woman is all contradictions — enormous eyes, a grin that is either welcoming or devouring, a body built from slabs of color that never quite resolve. Some critics called it hatred; de Kooning called her 'a woman.' It is the human figure refusing to leave abstract painting, and the struggle to pin her down is the painting.",
        "Woman I (1952) — the woman who wouldn't leave",
        ["Abstract Expressionism", "Figure"],
    ),
    "artw-full-fathom-five-447": _entry(
        "Jackson Pollock (1947)",
        "Full Fathom Five (1947)",
        "One of Pollock's first great drip paintings — and hidden in the web of paint are real objects: coins, nails, buttons, cigarettes, matches, a paint tube, embedded in the surface like fossils. The title comes from Shakespeare's The Tempest ('Full fathom five thy father lies'), and the canvas is a portrait of the studio floor: everything that fell into the picture stayed.",
        "Hunt for the buried objects: this is one of Pollock's first mature drip paintings, and pressed into the black and silver web are real things — coins, nails, buttons, cigarettes, matches — that fell in as he worked and were sealed there forever. He laid the canvas flat and dripped from above, and whatever hit the surface became part of the painting, which is why the title, from Shakespeare's Tempest, fits: 'full fathom five thy father lies' — things drowned and transformed. It is the studio floor, fossilized.",
        "Full Fathom Five (1947) — the buried coins and nails",
        ["Abstract Expressionism", "Drip"],
    ),
    "artw-mountains-and-sea-448": _entry(
        "Helen Frankenthaler (1952)",
        "Mountains and Sea (1952)",
        "Frankenthaler, 23 years old, poured thinned paint onto raw, unprimed canvas so the color sank into the fiber instead of sitting on top — the 'soak-stain' method that birthed Color Field painting. The result looks like a watercolor the size of a wall: mountains and sea dissolving into each other. Morris Louis and Kenneth Noland came to her studio, saw it, and changed their art overnight.",
        "Notice that the canvas has no back: Frankenthaler poured paint so thin it sank into the raw fabric instead of sitting on the surface, staining the cloth like a giant watercolor. She was 23 when she made it, and the title tells you what to find — a landscape of mountains and sea dissolving into veils of blue and green. Morris Louis and Kenneth Noland visited her studio, saw the stain, and changed direction on the spot; without this canvas, Color Field painting might not exist.",
        "Mountains and Sea (1952) — the stain that started a movement",
        ["Abstract Expressionism", "Color Field"],
    ),
    # ---------- Minimalism & Land Art ----------
    "artw-untitled-stack-449": _entry(
        "Donald Judd (1967)",
        "Untitled (Stack) (1967)",
        "Ten identical galvanized iron boxes, each exactly 9 inches tall, mounted one above another on the wall with 9-inch gaps — pure repetition, pure module. Judd called his works 'specific objects': neither painting nor sculpture, just things that exist in real space. The stack is Minimalism's logo: the same shape repeated until the wall becomes the sculpture.",
        "Count the rhythm: ten identical iron boxes march up the wall, each the same size, each separated by exactly the same gap — the repetition is the sculpture. Judd rejected both painting and sculpture and called these 'specific objects': real things in real space, made of industrial materials with no message and no metaphor. The stack turns the whole wall into the artwork, and your body measuring the height is part of it. Stand at the end of the row and the boxes line up into a single vertical bar.",
        "Untitled (Stack) (1967) — the iron boxes on the wall",
        ["Minimalism", "Sculpture"],
    ),
    "artw-double-negative-450": _entry(
        "Michael Heizer (1969)",
        "Double Negative (1969)",
        "Two enormous cuts — 1,500 feet long and 50 feet deep — gouged into a mesa in the Nevada desert, moving 240,000 tons of rock. The artwork is not what was built but what was removed: two negative spaces facing each other across a gap. Owned by MoMA and sitting in the middle of nowhere, Double Negative is land art's most literal monument to absence.",
        "Walk the line of the cut: two trenches, each 1,500 feet long and 50 feet deep, carved into opposite sides of a mesa so they face each other across a gap — the sculpture is the space where the rock used to be. Heizer moved 240,000 tons of desert stone in 1969 and left the hole as the artwork. You can walk the whole length on the flat floor of the cut, the walls towering beside you, and at the end you stand at the edge looking across the empty span to the other cut — absence, at the scale of a canyon.",
        "Double Negative (1969) — the canyon made by removal",
        ["Land Art", "Earthwork"],
    ),
    "artw-roden-crater-451": _entry(
        "James Turrell (1979-)",
        "Roden Crater (1979-)",
        "James Turrell has spent more than forty years turning an extinct volcano in the Arizona desert into a naked-eye observatory — rooms, tunnels, and apertures engineered so the sky itself becomes the artwork: the sun, moon, and stars framed and manipulated through openings carved into the crater. Still incomplete, open only by reservation, it is the largest artwork ever made with light alone.",
        "Understand the trick of the rooms: Turrell has been carving this extinct volcano since 1979 into a series of chambers where the ceiling is a hole and the sky becomes a flat, colored disc — light that looks like a painting but is just atmosphere seen through engineered openings. One room is a perfect bowl that captures the dawn; another frames the moon at a precise angle. It is not finished, and you can only visit by lottery-like reservation, which is part of the work: the greatest artwork of light asks you to wait for it.",
        "Roden Crater (1979-) — the volcano that holds the sky",
        ["Land Art", "Light"],
    ),
    "artw-narcissus-garden-452": _entry(
        "Yayoi Kusama (1966)",
        "Narcissus Garden (1966)",
        "Kusama's first outdoor installation: 1,500 mirrored steel balls spread across the lawn of the Venice Biennale — and she sold them for two dollars each, a protest against the art market, until the Biennale stopped her. The mirrored orbs reflect the sky, the pavilions, and the viewer, a thousand small Narcissuses in the garden of art.",
        "Walk into the field of mirrors: 1,500 stainless steel balls stretch across the lawn, each one reflecting the sky, the pavilions, and you — a thousand small versions of Narcissus staring at themselves. Kusama made it for the 1966 Venice Biennale and immediately started selling the balls for two dollars each, a protest against an art world that priced everything and owned nothing. The Biennale shut her down, which was part of the performance. The piece returns everywhere now; the point was the gesture.",
        "Narcissus Garden (1966) — the field of mirrors",
        ["Installation", "Mirrors"],
    ),
    "artw-another-place-453": _entry(
        "Antony Gormley (1997)",
        "Another Place (1997)",
        "One hundred cast-iron figures — every one cast from Gormley's own body — stand on Crosby Beach near Liverpool, facing the horizon, staring out to sea. The tide covers them twice a day and they emerge again, barnacled, patient, anonymous. Each figure weighs about 650 kilograms, and the whole work is a meditation on time, mortality, and the shore between.",
        "Watch the sea take them: a hundred iron men, all cast from Gormley's own body, stand in the surf at Crosby Beach facing the horizon. At low tide they stand chest-deep in sand; at high tide the water swallows them completely, and they re-emerge twice a day, covered in barnacles. Walk out to the farthest one and stand beside it — the figures are your height, your weight, your silence. The work is about standing still: in the face of the tide, the cold, and time.",
        "Another Place (1997) — the hundred iron men at sea",
        ["Sculpture", "Public Art"],
    ),
    "artw-comedian-454": _entry(
        "Maurizio Cattelan (2019)",
        "Comedian (2019)",
        "A banana duct-taped to a wall with grey tape — sold at Art Basel Miami in 2019 for $120,000 (two editions plus a third at $150,000), and then eaten by a performance artist mid-display. Cattelan, the prankster of contemporary art, said: 'The banana is a banana.' It was the most argued-about artwork of the decade: is it a joke, a critique, or both?",
        "Decide what you are looking at: a single banana, duct-taped to a white wall with one strip of grey tape. Cattelan, who also made the golden toilet, said 'the banana is a banana.' Three editions sold for $120,000 to $150,000 at Art Basel Miami in 2019, and a performance artist walked up and ate the banana — the gallery replaced it. The price, the tape, the fruit, and the argument about whether it is art: all of it is the artwork, and you are now part of the argument.",
        "Comedian (2019) — the banana and the tape",
        ["Contemporary", "Installation"],
    ),
}


def main() -> int:
    data = json.loads(PATH.read_text(encoding="utf-8"))
    existing_ids = {e["id"] for e in data}
    existing_names = {e["name"] for e in data}

    dup_ids = sorted(existing_ids & set(NEW.keys()))
    dup_names = sorted(n for n in [s["name"] for s in NEW.values()] if n in existing_names)
    if dup_ids or dup_names:
        print("  duplicate ids:", dup_ids)
        print("  duplicate names:", dup_names)
        return 1

    SUBTYPES = {
        "artw-the-large-glass-421": "Installation",
        "artw-twittering-machine-424": "Watercolor",
        "artw-the-tree-of-life-stoclet-frieze-430": "Mosaic",
        "artw-bed-444": "Assemblage",
        "artw-painted-bronze-445": "Sculpture",
        "artw-untitled-stack-449": "Sculpture",
        "artw-double-negative-450": "Land Art",
        "artw-roden-crater-451": "Land Art",
        "artw-narcissus-garden-452": "Installation",
        "artw-another-place-453": "Sculpture",
        "artw-comedian-454": "Installation",
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
