#!/usr/bin/env python3
"""Batch 3: add 50 new handcrafted artworks to artworks.json (ids 205-254).

Ghent Altarpiece through contemporary installation — real fun facts,
handcrafted teasers and quality-bar instructions. Appends to 156 entries.
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
    "artw-the-ghent-altarpiece-205": _entry(
        "Hubert & Jan van Eyck (1432)",
        "The Ghent Altarpiece (1432)",
        "The Adoration of the Mystic Lamb — a 12-panel altarpiece in Ghent, Belgium, and the most stolen artwork in history: it has been looted, dismembered, and hidden six times over 600 years, and one panel (The Just Judges) is still missing, replaced by a copy. It's also one of the first great works in oil paint, with colors that still glow after six centuries.",
        "Look at the central panel first: the Lamb of God stands on an altar bleeding into a chalice, while fountains, prophets, and crowds fill a panoramic landscape — the entire Christian story compressed into one image. Then the panels: the closed wings show the Annunciation in grisaille (paint that mimics stone sculpture), and opening the altarpiece reveals the full color world inside. The van Eycks' oil technique — glazing thin layers of color over each other — let them paint light itself, and the altarpiece was the template for Northern Renaissance painting. The missing Just Judges panel has never been found, and its theft in 1934 remains Belgium's greatest unsolved art crime.",
        "The Ghent Altarpiece (1432) — the central Lamb panel and the grisaille wings",
        ["Northern Renaissance", "Oil Painting"],
    ),
    "artw-the-last-judgement-sistine-206": _entry(
        "Michelangelo (1541)",
        "The Last Judgement (1541)",
        "Michelangelo's giant fresco behind the Sistine Chapel altar — hundreds of muscular, naked souls rising to heaven and falling to hell — scandalized the Church so much that after his death, another painter was hired to paint loincloths on the figures. The nudity censorship campaign is called the 'fig-leaf campaign,' and it took 300 years to complete.",
        "Look at the center: Christ returns at the top, his arm raised in judgment, with Mary tucked under his arm — and every figure around them is a naked athlete, because Michelangelo painted the body as God's image, not as shame. Then the bottom right: the damned are dragged into hell, and Charon, the ferryman of Greek myth, clubs them with his oar — a pagan figure inside a Christian Last Judgement. Michelangelo painted himself in the skin of St Bartholomew, flayed and held up by the saint — a self-portrait of a man who felt his own skin peeled by his critics.",
        "The Last Judgement (1541) — Christ's raised arm and the flayed self-portrait",
        ["Renaissance", "Fresco"],
    ),
    "artw-the-wedding-at-cana-207": _entry(
        "Paolo Veronese (1563)",
        "The Wedding at Cana (1563)",
        "The largest canvas in the Louvre — 6.77 by 9.94 meters — showing the wedding where Christ turned water into wine, with 130 guests including the artist himself, who painted his own portrait into the group of musicians at the center. It was painted for a monastery's refectory, where it would have appeared to extend the dining hall into the biblical banquet.",
        "Look at the musicians in the center foreground: the man in white playing the viola da gamba is Veronese himself, and the man next to him in blue is believed to be the architect Andrea Palladio — the painter put his friends into the Bible. Then the banquet: the guests are dressed in 16th-century Venetian fashion, not ancient robes, because Veronese painted the Bible as a contemporary Venetian feast. The painting was looted by Napoleon's army in 1797 and cut into strips for transport, then reassembled in Paris, where it remains — the monastery's refectory has a photograph of it instead.",
        "The Wedding at Cana (1563) — find the painter in the musicians",
        ["Venetian Renaissance", "Oil Painting"],
    ),
    "artw-the-entombment-of-christ-208": _entry(
        "Caravaggio (1603)",
        "The Entombment of Christ (1603)",
        "Caravaggio's painting of Christ's body being lowered into the tomb — six figures struggling with the weight of a corpse, with one detail that scandalized viewers: Nicodemus's bare, dirty feet are thrust directly at the viewer. The painting is considered one of the greatest altarpieces of the Baroque.",
        "Look at the feet first: Nicodemus's legs are exposed and his feet, bare and grimy, point straight at you — Caravaggio's way of dragging the sacred down to street level, which is what made him famous and infamous. Then the weight: every figure strains — the bearded man holds Christ's knees, another grips the shroud, and the women's faces are lit from above by an invisible light. The marble slab of the tomb forms a diagonal that leads the eye from the dead Christ up to the grieving Mary. The painting is in the Vatican, and it was copied by Rubens and imitated by a generation of painters.",
        "The Entombment of Christ (1603) — the bare feet pointing at you",
        ["Baroque", "Oil Painting"],
    ),
    "artw-the-nightmare-1781-209": _entry(
        "Henry Fuseli (1781)",
        "The Nightmare (1781)",
        "Fuseli's painting of a sleeping woman with a demon crouched on her chest and a horse's head emerging from the curtains — the original horror-movie poster. It was painted in 1781 and became an instant sensation, and the word 'nightmare' literally means 'night-mare': the horse (mare) was believed to sit on sleepers' chests and crush them.",
        "Look at the woman: she lies limp on her back, arms and head hanging off the bed, while the incubus — a demon of folklore — squats on her chest. Then find the horse: its head and neck burst through the curtain at the back, its eyes wild — the painting is the origin of the 'nightmare horse' image. Fuseli based the demon on local folklore and on his own sleep paralysis, and the painting was so popular that he made engraved copies. Look at the palette: the room is lit by an unseen source that picks out the woman's skin and the demon's back, leaving everything else in darkness.",
        "The Nightmare (1781) — the demon on the chest and the horse in the curtain",
        ["Romanticism", "Gothic"],
    ),
    "artw-washington-crossing-210": _entry(
        "Emanuel Leutze (1851)",
        "Washington Crossing the Delaware (1851)",
        "The most famous American history painting — but it's full of deliberate inaccuracies: painted in Germany by a German artist who never saw the Delaware, it shows Washington standing in a small boat that would have sunk, a flag that didn't exist in 1776, and ice that wouldn't have been there. Washington never stood; the crossing happened in darkness, not daylight.",
        "Look at the boat: it's impossibly crowded and Washington stands tall in it — in reality the crossing was at night in freezing weather, and Washington almost certainly stayed seated to avoid capsizing. Then the flag: the Betsy Ross-style flag with 13 stars in a circle wasn't used until 1777, a year after the crossing. Leutze painted the scene in Düsseldorf, using the Rhine as a stand-in for the Delaware, and he modeled Washington on an American tourist. The painting became a symbol of American resolve — it hung in the Capitol for decades and was used on posters during WWII.",
        "Washington Crossing the Delaware (1851) — count the historical errors",
        ["American", "History Painting"],
    ),
    "artw-the-ninth-wave-211": _entry(
        "Ivan Aivazovsky (1850)",
        "The Ninth Wave (1850)",
        "Aivazovsky's towering painting of shipwreck survivors clinging to a mast as a giant wave — the 'ninth wave' of Russian sea lore, the one that breaks hardest — towers over them at sunrise. The painting is the most famous seascape ever painted, and Aivazovsky painted over 6,000 sea paintings in his life, mostly from memory.",
        "Look at the wave: it's enormous, translucent green, and about to break over the survivors, yet the painting is not despairing — the sunrise behind it floods the scene with gold, and the survivors have survived the night. Then the light: Aivazovsky painted the sun as a white-hot ball that breaks through the cloud, and he could paint a convincing sea entirely from imagination — he said the sea was 'my life.' The survivors cling to the mast, one waving a cloth, and the tiny figures against the huge water make the painting about scale: human hope against the ocean. It hangs in the Russian Museum in St Petersburg.",
        "The Ninth Wave (1850) — the wave versus the sunrise",
        ["Romanticism", "Seascape"],
    ),
    "artw-whistlers-mother-212": _entry(
        "James McNeill Whistler (1871)",
        "Whistler's Mother (1871)",
        "The most famous portrait of a mother in the world — but Whistler didn't call it that. He titled it Arrangement in Grey and Black No. 1, because he insisted his paintings were about color and composition, not people. His mother Anna posed because his model was sick, and she sat for the portrait in profile because she was too old to sit for a frontal view comfortably.",
        "Look at the title first: 'Arrangement in Grey and Black No. 1' — Whistler deliberately named the painting after its color scheme, treating his mother as a still-life arrangement of tones. Then the composition: the profile, the black dress, the white cap and handkerchief, the grey wall with a framed print — everything is a study in muted tones, and the picture is perfectly balanced like a geometric diagram. The portrait was rejected in England and became a hit in Paris, and it's now an icon of American identity — it was even the subject of a famous 1960s song. Look at the bare lower-left corner: the emptiness is part of the design.",
        "Whistler's Mother (1871) — the color arrangement, not the portrait",
        ["American", "Portrait"],
    ),
    "artw-the-floor-scrapers-213": _entry(
        "Gustave Caillebotte (1875)",
        "The Floor Scrapers (1875)",
        "Caillebotte's painting of three workers on their knees scraping a Paris floor — the first major painting of working men doing physical labor, with their bare backs and tools made the subject of high art. It was rejected by the Salon and shown instead at the second Impressionist exhibition, where it made the movement's reputation for depicting modern life.",
        "Look at the bodies: three men in profile, working in rhythm, their backs and arms curved in identical postures — Caillebotte painted labor with the seriousness usually reserved for heroes. Then the light: the sun falls in a broad diagonal across the floor, and the scraped wood gleams while the men cast long shadows. Notice the details: the bottles of wine and the brazier, the dust, the rolled sleeves — the scene is real Paris, not an allegory. Caillebotte was an amateur who painted as a hobby (he was a wealthy engineer), and he funded the Impressionist exhibitions — his paintings of modern urban life are now counted among the movement's best.",
        "The Floor Scrapers (1875) — the rhythm of the three backs",
        ["Impressionism", "Realism"],
    ),
    "artw-labsinthe-214": _entry(
        "Edgar Degas (1876)",
        "L'Absinthe (1876)",
        "Degas's painting of a man and a woman sitting silently at a café table with a glass of green absinthe in front of her — the definitive image of modern loneliness. The woman was a real actress named Ellen Andrée, and the man the painter Marcellin Desboutin; when the painting was shown in London, critics were horrified by what they saw as a portrait of two drunks.",
        "Look at the composition: the two figures are pushed to the right of the canvas, leaving a huge empty tabletop and floor to their left — the emptiness is the subject, and the angle is tilted as if seen from a neighboring table. Then the glasses: hers is the tall green absinthe glass, his a wine glass, and both are nearly empty — but the point is not drinking, it's the silence between them; they don't look at each other or at us. The green glass glows against the marble, and the painting was so famous that absinthe — later banned — became linked to this image forever.",
        "L'Absinthe (1876) — the empty half of the canvas",
        ["Impressionism", "Realism"],
    ),
    "artw-paris-street-rainy-day-215": _entry(
        "Gustave Caillebotte (1877)",
        "Paris Street; Rainy Day (1877)",
        "Caillebotte's enormous painting of a rainy Paris intersection — men and women in 1870s coats and umbrellas crossing the Place de Dublin — is the definitive image of the modern city. The man in the foreground, cropped by the frame like a photograph, walks toward you under a grey sky while everyone else is caught mid-step.",
        "Look at the foreground man: he's cut off at the knees by the painting's edge — a radical composition borrowed from photography, which was new — and he walks directly toward the viewer, while the other pedestrians are shown mid-stride as if the rain froze them. Then the surfaces: the wet cobblestones reflect the streetlights and the umbrellas, and the buildings recede with mathematical perspective. The painting is at the Art Institute of Chicago, where it hangs in a room that draws crowds the way the Louvre draws crowds for the Mona Lisa.",
        "Paris Street; Rainy Day (1877) — the cropped foreground figure",
        ["Impressionism", "Cityscape"],
    ),
    "artw-the-cradle-1872-216": _entry(
        "Berthe Morisot (1872)",
        "The Cradle (1872)",
        "Morisot's painting of her sister Edma watching her baby daughter sleep — the first Impressionist painting of motherhood, shown at the first Impressionist exhibition of 1874. Morisot was one of the few women at the movement's core, and she painted the private world of women that male artists rarely showed.",
        "Look at the mother: she leans over the cradle, her face softened into a dream — Morisot painted the gaze of a woman at her child with a tenderness that is also a kind of privacy. Then the baby: barely visible under the gauze curtain, a suggestion of a face — Morisot leaves the child almost abstract, so the painting is about the mother's attention, not the baby's features. The white gauze is painted with quick, loose strokes, and the whole canvas has the lightness of a moment that could dissolve. Morisot exhibited this painting in the 1874 show that launched Impressionism, and it was her favorite of her own works.",
        "The Cradle (1872) — the mother's gaze and the gauze",
        ["Impressionism", "Portrait"],
    ),
    "artw-the-bath-cassatt-217": _entry(
        "Mary Cassatt (1893)",
        "The Bath (1893)",
        "Cassatt's painting of a mother bathing her daughter — shown in the famous 1893 impressionist room at the Chicago World's Fair — is the most celebrated image of maternal care in American art. Cassatt, an American who lived in Paris, painted the everyday intimacy of mothers and children, and she insisted on showing women as strong, competent people.",
        "Look at the composition: the mother and child form a compact pyramid, and the mother's striped dress and the round basin repeat the curves — everything is organized around the small body being washed. Then the gesture: the mother's left hand steadies the child's leg while her right hand washes the foot, and the child's hand presses against the mother's arm — a real, working moment, not a posed one. The print on the wall behind them — of a woman nursing — echoes the theme of care. Cassatt's Japanese-style printmaking influenced this flattened, patterned look.",
        "The Bath (1893) — the mother's hands and the pyramid of care",
        ["Impressionism", "Portrait"],
    ),
    "artw-wheatfield-with-crows-218": _entry(
        "Vincent van Gogh (1890)",
        "Wheatfield with Crows (1890)",
        "Van Gogh's stormy wheatfield with a dark road splitting the golden crop and black crows rising — painted in the last weeks of his life in Auvers-sur-Oise, and long believed to be his final painting. The sky churns with black clouds, the crows are painted in quick dark strokes, and three roads lead nowhere into the field.",
        "Look at the sky first: it's a living storm of short, curling strokes, and the black clouds press down on the field. Then the crows: they're painted as dark V-shapes that scatter toward the viewer, and they're some of the most famous birds in art. The three paths vanish into the wheat without reaching the horizon — a detail many read as a dead end. Van Gogh shot himself days later, and while scholars now doubt this was literally his last canvas, the painting's turmoil made it his best-known farewell. Notice the colors: the yellow wheat and the blue-black sky fight for the canvas, and the painting vibrates with it.",
        "Wheatfield with Crows (1890) — the three dead-end roads",
        ["Post-Impressionism", "Oil Painting"],
    ),
    "artw-sunflowers-1888-219": _entry(
        "Vincent van Gogh (1888)",
        "Sunflowers (1888)",
        "Van Gogh painted seven versions of sunflowers in vases — and they became the most famous flower paintings in the world, partly because he made them as a welcome gift for his friend Paul Gauguin, who never quite appreciated them. The sunflowers were painted in Arles as decoration for the 'Yellow House' studio he hoped to share with Gauguin.",
        "Look at the texture: the sunflower petals and centers are built with thick, sculptural dabs of paint — van Gogh squeezed the paint so thickly that the flowers seem to pop off the canvas. Then the colors: he used at least 12 different yellows, from pale lemon to deep ochre, and the flowers range from full bloom to withered, showing a whole life cycle in one vase. Van Gogh said the sunflower 'is a thank-you' and that the paintings 'will be seen as a symbol of gratitude.' The versions now hang in museums across the world, and one sold in 1987 for $39.9 million — a record that made van Gogh the highest-priced artist of his era.",
        "Sunflowers (1888) — the thick paint and the range of yellows",
        ["Post-Impressionism", "Still Life"],
    ),
    "artw-almond-blossoms-220": _entry(
        "Vincent van Gogh (1890)",
        "Almond Blossoms (1890)",
        "Van Gogh painted almond blossoms for his newborn nephew — the only child of his brother Theo — and hung it above the family bed. The painting is his gentlest work: white blossoms against a bright blue sky, painted with the delicacy of Japanese prints. The baby was named Vincent, after him.",
        "Look at the calm: after years of storms and wheatfields, this painting is pure tenderness — white flowers against a flat blue sky, with no turbulence at all. The composition borrows from Japanese woodblock prints, which van Gogh loved: branches cropped at the edges, blossoms floating on an empty background. The almond tree blooms in early spring — a symbol of new life — and van Gogh painted it as a gift for his brother's son, who was born the same month. Today the Almond Blossom is one of his most beloved paintings, and the Van Gogh Museum in Amsterdam uses a branch from it as part of its logo.",
        "Almond Blossoms (1890) — the Japanese-print calm",
        ["Post-Impressionism", "Oil Painting"],
    ),
    "artw-self-portrait-bandaged-ear-221": _entry(
        "Vincent van Gogh (1889)",
        "Self-Portrait with Bandaged Ear (1889)",
        "Van Gogh's self-portrait after he cut off his ear — the bandaged head, the fur cap, the pipe, and behind him a Japanese print and an empty canvas on an easel. He painted it in the weeks after the ear incident, looking straight at the viewer, calm and defiant, in a way that has made it one of the most recognized faces in art.",
        "Look at the bandage: it covers the ear he injured, and he's wearing a winter cap that holds it in place — he painted himself in the aftermath, not hiding from it. Then the background: a Japanese print on the wall and an empty canvas on an easel — he's showing himself as an artist, still working. His eyes are steady and the face is composed, not tormented, which is what makes the painting unsettling: the man who cut off his ear painted himself as serene. The painting is at the Courtauld Gallery in London, and its careful composition — the red ground, the green coat, the orange background — shows him fully in control of his craft.",
        "Self-Portrait with Bandaged Ear (1889) — the steady eyes",
        ["Post-Impressionism", "Self-Portrait"],
    ),
    "artw-portrait-of-dr-gachet-222": _entry(
        "Vincent van Gogh (1890)",
        "Portrait of Dr. Gachet (1890)",
        "Van Gogh's portrait of the homeopathic doctor who cared for him in Auvers — a man resting his head on his hand beside a foxglove plant (the source of the heart medicine digitalis). It became the most expensive painting in the world when it sold for $82.5 million in 1990 — a record that held for 14 years.",
        "Look at the pose: Gachet rests his head on his hand with the same exhausted gesture as van Gogh's own self-portraits — the doctor and the patient mirror each other. Then the foxglove: its flowers produce digitalis, a heart medicine, so the plant is both a doctor's badge and a symbol of the melancholy that runs through the painting. Van Gogh wrote that he painted Gachet 'with the sorrowful expression of our time,' and he used a palette of blues — the doctor's coat, the table, the hills — that glow against the pale face. The painting sold for $82.5 million in 1990 to a Japanese businessman, and it was later sold again in a private deal for even more.",
        "Portrait of Dr. Gachet (1890) — the mirrored pose and the foxglove",
        ["Post-Impressionism", "Portrait"],
    ),
    "artw-the-large-bathers-223": _entry(
        "Paul Cézanne (1906)",
        "The Large Bathers (1906)",
        "Cézanne's final masterpiece — a monumental painting of nude bathers arranged under the arching branches of trees, which he worked on for years and left unfinished at his death. It's considered the bridge between Impressionism and Cubism, and it directly inspired Picasso and Matisse, who called it 'the father of us all.'",
        "Look at the composition: the bathers are arranged in a triangle, the trees arch over them to form a dome, and the whole painting is built from geometric masses — the bodies are simplified into almost columnar shapes. Then the brushwork: the paint is applied in parallel strokes that build form the way a mason builds a wall, and the canvas is left unfinished in places, showing bare cloth. Cézanne said he wanted to 'do Poussin over again from nature' — to bring the order of classical art into the open air. Picasso's Les Demoiselles d'Avignon and Matisse's Bathers both descend from this painting, which hangs in the Philadelphia Museum of Art.",
        "The Large Bathers (1906) — the arching trees and geometric bodies",
        ["Post-Impressionism", "Oil Painting"],
    ),
    "artw-the-dance-of-life-224": _entry(
        "Edvard Munch (1900)",
        "The Dance of Life (1900)",
        "Munch's painting of a summer dance on the pier — a couple in the center, the woman in a red dress (Munch's model Tulla Larsen), a young girl in white on the left, and a dark figure on the right. It's the centerpiece of his 'Frieze of Life,' the series he called 'a poem about life, love, and death.'",
        "Look at the central couple: the woman in red is the same figure who appears throughout Munch's work — he painted her in red for passion, and the man bending toward her is Munch himself. Then the flanking figures: the girl in white on the left is innocence, and the dark woman on the right is age or death — the painting is a single image of a woman's whole life. The moon's reflection on the water forms a vertical stripe, and the pier is crowded with dancers who blur into the night. Munch said the Frieze of Life was 'a poem about life, love, and death,' and this painting is its heart.",
        "The Dance of Life (1900) — the red dress and the white and black figures",
        ["Expressionism", "Symbolism"],
    ),
    "artw-the-broken-column-225": _entry(
        "Frida Kahlo (1944)",
        "The Broken Column (1944)",
        "Kahlo's self-portrait after spinal surgery — her torso split open to reveal a broken Ionic column where her spine should be, held together by nails and a surgical brace. She painted it after a year of operations following the bus accident that had shattered her body at 18.",
        "Look at the column: where her spine should be, a cracked Ionic column stands, its sections held by metal brackets — Kahlo turned her own body into a ruined classical monument. Then the nails: they're driven into her face and torso, each one a small point of pain, while the surgical corset straps hold her up. The tears on her face are painted carefully, one at a time, and the barren landscape behind her cracks open with the same fault lines as her body. Kahlo painted her suffering again and again, and she said 'I paint myself because I am so often alone.' The painting is one of her most direct statements about the pain she carried every day.",
        "The Broken Column (1944) — the ruined column and the nails",
        ["Surrealism", "Self-Portrait"],
    ),
    "artw-portrait-of-gertrude-stein-226": _entry(
        "Pablo Picasso (1906)",
        "Portrait of Gertrude Stein (1906)",
        "Picasso painted the American writer Gertrude Stein at least 90 times before he was satisfied — he scraped the face off and repainted it from memory after a trip to Spain, telling her 'I look at you, I see you.' Stein, who became one of the century's greatest art patrons, said the portrait would 'survive us both.'",
        "Look at the face first: it's mask-like and angular, almost sculptural, while her body is painted in loose, brown tones — Picasso repainted the face from memory after his trip to Spain, giving it the weight of an Iberian sculpture. Then the pose: Stein sits massive and still, her hands folded, like a monument to herself — she later said 'I am the only person who has never been bored by Picasso.' The portrait hangs at the Met, and Picasso kept it in his studio for years, refusing to sign it until Stein's heirs asked. It's the moment before Cubism, when Picasso started building faces like sculpture.",
        "Portrait of Gertrude Stein (1906) — the mask face and the monumental pose",
        ["Proto-Cubism", "Portrait"],
    ),
    "artw-houses-at-lestaque-227": _entry(
        "Georges Braque (1908)",
        "Houses at L'Estaque (1908)",
        "Braque's painting of a Provençal village reduced to cubes — the painting that gave Cubism its name, when a critic said Braque was 'reducing everything to little cubes.' It was shown in 1908 and mocked, but Picasso saw it and the two artists began the most famous collaboration in art history.",
        "Look at the houses: they're not cubes exactly, but the roofs, walls, and trees are all built from geometric slabs, and the whole village looks like a cluster of carved blocks. Then the space: there's almost no horizon — the houses stack upward, and the sky is reduced to a few patches, because Braque was flattening perspective into a single plane. The palette is deliberately muted — browns, greens, greys — because Braque wanted the forms, not the colors, to do the work. The critic who wrote the word 'cubes' meant it as an insult; Braque and Picasso adopted it as a badge, and modern art's most famous movement had a name.",
        "Houses at L'Estaque (1908) — the geometric village",
        ["Cubism", "Landscape"],
    ),
    "artw-violin-and-candlestick-228": _entry(
        "Georges Braque (1910)",
        "Violin and Candlestick (1910)",
        "Braque's painting of a violin and a candle — seen from every angle at once, broken into planes so the objects are almost unrecognizable. It's a masterpiece of Analytic Cubism, the style Braque and Picasso developed in 1910-11, in which objects are shattered into facets and shown from multiple viewpoints simultaneously.",
        "Look for the violin: the curves of its body, the f-holes, and the strings are suggested by lines and planes, but the instrument is broken apart and seen from the front, side, and top at the same time. Then the candlestick: its flame is a small bright shape near the top, and its stem is a series of vertical planes. The painting is mostly brown and grey, because Braque believed color distracted from the structure — he said he 'wanted to make art as solid as a wall.' Look at how the objects melt into the space around them: in Analytic Cubism, the table, the air, and the objects are made of the same pictorial substance.",
        "Violin and Candlestick (1910) — find the violin in the facets",
        ["Cubism", "Still Life"],
    ),
    "artw-soft-construction-229": _entry(
        "Salvador Dalí (1936)",
        "Soft Construction with Boiled Beans (1936)",
        "Dalí's painting of a monstrous figure tearing itself apart — a body with arms and legs that pull in opposite directions, its head screaming, set in a barren landscape with a plate of boiled beans. He painted it as his response to the Spanish Civil War, and he said the figure is 'a human body with its own limbs tearing each other.'",
        "Look at the monster: it's made of a ribcage, a head, and limbs that yank against each other — one leg reaches up, another down, and the hands claw at the body itself, so the figure is both the war and its victim. Then the landscape: the sky is flat and dead, and the small figure of a woman below and the plate of beans are tiny and incongruous — Dalí said the beans were 'the poor food of Spain.' The title is deliberately anti-heroic: 'soft construction with boiled beans' turns a war into a kitchen still life. Dalí painted the war 'without the need for realism' — this is war as a biological event, a body that cannot stop destroying itself.",
        "Soft Construction with Boiled Beans (1936) — the self-tearing body",
        ["Surrealism", "War"],
    ),
    "artw-the-elephants-1948-230": _entry(
        "Salvador Dalí (1948)",
        "The Elephants (1948)",
        "Dalí's painting of two elephants with impossibly long, spindly legs — one carrying an obelisk, the other a load of stones — walking through a desert toward a sunset. The elephants have appeared in many Dalí paintings, and their legs are so thin they seem to be walking on stilts, inspired by Gian Lorenzo Bernini's sculpture of an elephant carrying an obelisk.",
        "Look at the legs: they're as thin as matchsticks, yet they carry an obelisk and a pile of rocks — Dalí took Bernini's real elephant-and-obelisk sculpture and stretched the legs until the animals seem to float. Then the scale: the elephants are huge in the foreground but the desert around them is empty, and the sunset's orange light turns the scene into a dream. Dalí said the elephants 'represent the future,' and their thin legs make them both powerful and fragile. The painting shows his late style: precise, luminous, and full of symbols that resist a single meaning.",
        "The Elephants (1948) — the impossibly thin legs",
        ["Surrealism", "Symbolism"],
    ),
    "artw-the-temptation-of-st-231": _entry(
        "Salvador Dalí (1946)",
        "The Temptation of St Anthony (1946)",
        "Dalí's painting of the hermit saint Anthony on a rock, raising a cross against a parade of temptations: a horse with a tower on its back, an elephant with a naked woman on its back, another with a building, all walking on spider-leg stilts. He painted it for a 1946 contest for a film of The Private Affairs of Bel Ami — he didn't win.",
        "Look at the temptations: each animal carries a symbol of worldly desire — the horse carries a phallic tower, the first elephant carries a naked woman, the others carry buildings and monuments — and all of them walk on legs too thin to be real. Then the saint: Anthony is tiny, on the right, raising a cross — the only solid thing in the painting — and Dalí shows him refusing the parade. The painting's message is about resisting desire, but the temptation is painted so beautifully that the painting makes the sin look like the better deal. The contest was for a film adaptation, and although Dalí lost, the painting is now his most famous religious work.",
        "The Temptation of St Anthony (1946) — the parade on stilt legs",
        ["Surrealism", "Religious"],
    ),
    "artw-golconda-1953-232": _entry(
        "René Magritte (1953)",
        "Golconda (1953)",
        "Magritte's painting of a grey sky filled with identical men in overcoats and bowler hats, raining down like a flock of birds — with one man in the foreground, larger, as if descending toward the viewer. The title comes from Golconda, an Indian diamond town associated with wealth; Magritte chose it, he said, for its sound.",
        "Look at the pattern: dozens of identical men in identical coats and hats, each separated by the same gap, raining from the sky at the same angle — the painting is a machine of repetition. Then the foreground figure: one man is bigger and closer, breaking the pattern and pointing the rain toward the viewer, so the painting asks whether you're next. Magritte said the bowler-hatted man was 'just a man' — an everyman, anonymous and replaceable — and the painting is his comment on conformity and the modern crowd. The title, Golconda, refers to the fabled city of diamonds; Magritte said he only picked it because he liked the word.",
        "Golconda (1953) — the raining identical men",
        ["Surrealism", "Conceptual"],
    ),
    "artw-the-lovers-1928-233": _entry(
        "René Magritte (1928)",
        "The Lovers (1928)",
        "Magritte's painting of a man and a woman kissing — their faces completely covered by white cloths. It's one of his most haunting images: intimacy and anonymity at once, two people who can never see or fully know each other, even while kissing. The painting's exact meaning is disputed — but the cloths make the kiss both tender and suffocating.",
        "Look at the cloths: they cover the faces completely, with no holes for eyes or mouths — the kiss happens between two blank masks, so the painting is both the most romantic and most alienating kiss in art. Then the background: a featureless wall and a corner of a room — no context, just two people and a mystery. Magritte painted the Lovers after the death of his mother, who drowned with her nightgown covering her face — many art historians read the cloths as a reference to that image. The painting is part of a series of four versions, and the ambiguity is the point: the lovers are united and separated at the same moment.",
        "The Lovers (1928) — the kiss through the cloths",
        ["Surrealism", "Romance"],
    ),
    "artw-the-listening-room-234": _entry(
        "René Magritte (1952)",
        "The Listening Room (1952)",
        "Magritte's painting of a giant green apple filling an entire room, from floor to ceiling, in a space with no windows — the apple is so large it has become the room. It's the 'listening room' of the title, a sound-proofed chamber, and the apple is his famous motif pushed to an impossible scale.",
        "Look at the scale: the apple is the same shape as a normal apple, but it fills the whole room — the wall, floor, and ceiling all meet its green surface, so the familiar fruit has become an alien presence. Then the room: no windows, no door, just a space that exists to be filled — a 'listening room' with nothing to listen to. Magritte said he wanted 'to make the most everyday object shriek aloud,' and the apple — which he painted over and over — here becomes a giant that traps you. The painting exists in several versions, and its trick is that the impossibility looks completely plausible, like a photograph of a dream.",
        "The Listening Room (1952) — the apple that is the room",
        ["Surrealism", "Conceptual"],
    ),
    "artw-number-1a-1948-235": _entry(
        "Jackson Pollock (1948)",
        "Number 1A, 1948 (1948)",
        "Pollock's breakthrough painting — the canvas that showed his 'drip' method fully formed: poured and dripped enamel in black, white, and colour over the whole surface, with a single red drip line running through it. It's at MoMA, where it's considered one of the most important paintings of the 20th century.",
        "Look at the red line: a single trail of red paint cuts through the tangle of black and white — Pollock poured it at the end, and it acts as the painting's signature and its spine. Then the surface: the paint is built in layers — black under white under color — so the painting has physical depth, and the drips loop and tangle without a center. Pollock painted it by laying the canvas on his studio floor and moving around it, and he said he was 'in the painting.' The painting's size — over 2 meters — and its all-over web made it the symbol of the new American art that would conquer the world after WWII.",
        "Number 1A, 1948 — follow the red line through the web",
        ["Abstract Expressionism", "Drip Painting"],
    ),
    "artw-convergence-1952-236": _entry(
        "Jackson Pollock (1952)",
        "Convergence (1952)",
        "Pollock's dense, colorful web of drips — one of his most famous works, named because the black lines converge and diverge across the canvas, with bursts of white, red, yellow, and blue. It became famous beyond the art world when a reproduction of it was used as a poster for a 1950s art textbook.",
        "Look at the convergence: the black lines cross and re-cross the canvas, and at certain points they knot together — Pollock named the painting for those meeting points. Then the color: unlike his earlier black-and-white works, Convergence explodes with red, yellow, and blue poured over the black web, so the painting reads as a battle between order and eruption. Pollock painted it at the height of his powers, two years before he stopped making such works. The painting hangs in the Albright-Knox Gallery in Buffalo, and its image — a reproduction — helped make drip painting the most recognizable style of American modern art.",
        "Convergence (1952) — the knots where the lines meet",
        ["Abstract Expressionism", "Drip Painting"],
    ),
    "artw-white-center-1950-237": _entry(
        "Mark Rothko (1950)",
        "White Center (1950)",
        "Rothko's painting of a white rectangle floating between bands of red, yellow, and pink — it sold in 2007 for $72.8 million, a record for post-war art at the time. Rothko considered his 1950 paintings his breakthrough: he stopped painting recognizable shapes and made the color itself the subject.",
        "Look at the white center: it's not pure white but a subtle layering of off-whites, and the bands around it — red, yellow, rose — glow against each other like a sunset held still. Then the edges: each rectangle's border is soft and blurred, so the bands breathe and vibrate against each other instead of sitting flat. Rothko said his paintings were 'not about color' but about 'basic human emotions,' and he wanted viewers to stand close and be immersed. The painting belonged to the Rockefeller family, and its 2007 sale — for $72.8 million — made headlines worldwide.",
        "White Center (1950) — the glowing bands and the blurred edges",
        ["Abstract Expressionism", "Color Field"],
    ),
    "artw-victory-boogie-woogie-238": _entry(
        "Piet Mondrian (1944)",
        "Victory Boogie Woogie (1944)",
        "Mondrian's last, unfinished painting — a dense grid of tiny colored squares that dances across the canvas, his most joyful work. He worked on it in New York during WWII until his death from pneumonia in 1944, and the painting was still on his easel, unfinished — it's now considered a national treasure of the Netherlands.",
        "Look at the density: unlike his earlier grids with large blocks, this canvas is packed with small squares of color, and the grid breaks apart into a rhythm that seems to move — the painting is literally boogie-woogie, the jazz Mondrian loved, translated into paint. Then the unfinished parts: on the left and in the corners, the canvas is bare, with painted paper strips still taped in place — Mondrian was still adjusting the composition when he died. The painting is owned by the Dutch state and shown in the Gemeentemuseum in The Hague, and it's considered the culmination of his life's work — the grid dissolving into pure rhythm.",
        "Victory Boogie Woogie (1944) — the dancing grid and the unfinished edge",
        ["De Stijl", "Abstract"],
    ),
    "artw-brillo-boxes-239": _entry(
        "Andy Warhol (1964)",
        "Brillo Boxes (1964)",
        "Warhol's wooden sculptures that look exactly like shipping boxes for Brillo soap pads — and they started the biggest philosophical argument in modern art: what makes art art? The philosopher Arthur Danto used the Brillo Boxes to ask why a cardboard Brillo box in a supermarket is garbage but a wooden copy in a gallery is art. The answer, he argued, is the art world itself.",
        "Look at the boxes first: they're wooden, hand-painted, and stacked exactly like the real thing — Warhol had the real Brillo boxes in his studio and made wooden replicas because they'd last longer. Then the question: the real boxes hold soap pads; these hold nothing. Danto said the difference between them is invisible to the eye and visible only to the mind — the gallery, the artist, the history. Warhol's boxes are the moment when the question 'what is art?' replaced 'what does it look like?' Look at the stenciled lettering and the red-and-white design: Warhol copied the commercial design exactly, which is the point — the artwork is indistinguishable from its source, and the meaning is in the difference nobody can see.",
        "Brillo Boxes (1964) — the boxes that ask what art is",
        ["Pop Art", "Sculpture"],
    ),
    "artw-drowning-girl-240": _entry(
        "Roy Lichtenstein (1963)",
        "Drowning Girl (1963)",
        "Lichtenstein's painting of a woman drowning in a melodramatic sea, with the thought bubble 'I don't care! I'd rather sink — than call Brad for help!' — blown up from a comic book panel. It's the masterpiece of Pop Art's most ironic move: high art copying low art, with the tragedy rendered in comic-book dots.",
        "Look at the speech bubble first: the woman would rather drown than call Brad — the drama is deliberately absurd, and Lichtenstein painted it with complete seriousness. Then the technique: the face and waves are built from Ben-Day dots — the printed dot pattern of comics — which Lichtenstein painted by hand or with stencils, enlarging them until they become abstract patterns. The tears are perfect circles, the hair is a mass of black waves, and the palette is limited to red, yellow, blue, and grey like cheap printing ink. Lichtenstein was accused of copying comics outright, but he cropped, recut, and repainted them — the painting is both a comic and a critique of comics.",
        "Drowning Girl (1963) — the Ben-Day dots and the absurd bubble",
        ["Pop Art", "Oil Painting"],
    ),
    "artw-triple-elvis-241": _entry(
        "Andy Warhol (1963)",
        "Triple Elvis (1963)",
        "Warhol's silkscreen of Elvis Presley repeated three times, drawn with a revolver and silver paint — part of his series of repeated celebrities that turned stars into products. Warhol silkscreened dozens of Elvises from a single publicity photo, and the repetitions made Elvis both omnipresent and anonymous.",
        "Look at the repetition: the same Elvis three times, each print slightly offset, like a film strip or a row of identical products — Warhol said he repeated images so people would notice the differences, not the sameness. Then the silver: Warhol coated the canvas with metallic paint, so the painting reflects light and looks like a movie screen — Elvis is a star made of light and repetition. The revolver in Elvis's hand comes from the source publicity still of the 1960 western Flaming Star. Warhol's celebrity paintings made the point that fame is a kind of product: mass-produced, reproduced, and infinitely repeatable.",
        "Triple Elvis (1963) — the silver reflection and the repeat",
        ["Pop Art", "Silkscreen"],
    ),
    "artw-rabbit-1986-242": _entry(
        "Jeff Koons (1986)",
        "Rabbit (1986)",
        "Koons's stainless-steel rabbit — smooth, mirror-polished, and inflated-looking, holding a carrot — sold in 2019 for $91.1 million, a record for a work by a living artist. The rabbit looks like a party balloon but weighs over 30 kg, and its mirror surface reflects the room so the sculpture appears to be made of light.",
        "Look at the surface: the steel is polished to a mirror finish, so the rabbit reflects the gallery, the walls, and you — the sculpture is also a giant curved mirror, and it looks weightless while weighing more than 30 kg. Then the proportions: the ears, the body, and the carrot are all slightly off, like a balloon animal drawn by someone who never saw one — Koons said he wanted it to look 'inflated, but solid.' The carrot is detachable, and the rabbit's blank face gives it a deadpan stare. Koons described it as 'a metaphor for the future,' and the 2019 sale made it the most expensive sculpture by a living artist.",
        "Rabbit (1986) — the mirror surface and the inflated look",
        ["Contemporary", "Sculpture"],
    ),
    "artw-sun-tunnels-243": _entry(
        "Nancy Holt (1976)",
        "Sun Tunnels (1976)",
        "Nancy Holt's land art in the Utah desert: four concrete tunnels, each 5.5 meters long, arranged in an open cross, so that at sunrise and sunset on the solstices the sun shines straight through each tube. Holt drilled holes in the tunnels' walls that map the constellations of Draco, Perseus, Columba, and Capricorn.",
        "Look at the tunnels first: four giant concrete cylinders in the empty desert, each open at both ends — from inside, the desert is framed in a circle, and the sky is framed in another. Then the holes: Holt drilled them to match the stars of four constellations, so by day the sun projects points of light onto the tunnel floor, and by night the stars shine through the same holes — the tunnel is an observatory that works both ways. On the summer and winter solstices, the sun rises and sets directly through the tubes. The tunnels are in the Great Basin Desert, reachable only by a rough road, and visitors are encouraged to stay overnight.",
        "Sun Tunnels (1976) — look through the tunnel at the framing desert",
        ["Land Art", "Sculpture"],
    ),
    "artw-vietnam-veterans-memorial-244": _entry(
        "Maya Lin (1982)",
        "Vietnam Veterans Memorial (1982)",
        "Maya Lin's design — a V-shaped wall of black granite sunk into the ground, carved with the names of 58,000 dead — was chosen when she was a 21-year-old Yale student, and it provoked a furious public debate before it was built. The wall is a wound in the earth that heals as you walk its length: the names rise from knee height to head height at the center, then sink away.",
        "Look at the wall's shape: it's a V sunk into the earth, its polished black granite reflecting the viewer, the sky, and the trees — you see yourself in the memorial while reading the names, which was Lin's intention: the living and the dead share the surface. Then the names: they're carved in chronological order of death, and the wall rises as the war's toll rises, peaking at its center. Lin said she wanted 'a journey' — you descend into the ground, walk its full length, and climb out. The memorial provoked outrage from veterans who wanted a heroic monument; Lin's response was a wall that does not judge, only remembers. It is now the most visited memorial in Washington.",
        "Vietnam Veterans Memorial (1982) — walk the length and watch yourself reflected",
        ["Memorial", "Landscape"],
    ),
    "artw-the-angel-of-the-north-245": _entry(
        "Antony Gormley (1998)",
        "The Angel of the North (1998)",
        "Gormley's 20-meter-tall steel angel with 54-meter wingspan stands on a hill beside a highway in Gateshead, England — the largest angel sculpture in the world, built from 200 tons of weathering steel that turns rust-red with age. It was so controversial before construction that locals feared it would be 'a giant statue with a skirt.'",
        "Look at the wings: they're angled forward, not spread flat — Gormley said the angel is 'not a symbol but a body turned into space,' and the forward-swept wings make it look like it's leaning into the wind. Then the material: weathering steel, which rusts to a permanent red-brown — the sculpture is literally aging and changing color as it stands. Gormley made it from his own body: the shape is based on a cast of the artist's torso, stretched to 20 meters. The sculpture now greets millions of drivers a year, and the locals who once opposed it now use it as the region's symbol — it appears on the region's football shirts and road signs.",
        "The Angel of the North (1998) — the forward-swept wings",
        ["Contemporary", "Public Sculpture"],
    ),
    "artw-spoonbridge-and-cherry-246": _entry(
        "Claes Oldenburg & Coosje van Bruggen (1988)",
        "Spoonbridge and Cherry (1988)",
        "The giant stainless-steel spoon with a cherry on top — 16 meters long, weighing 1,600 kg of steel — stands in the Minneapolis Sculpture Garden, where water sprays from the cherry stem like a fountain. It's the most photographed sculpture in the Midwest, and the artists' gift to their favorite city.",
        "Look at the scale first: the spoon is as long as a bus, and the cherry weighs more than a car — Oldenburg and van Bruggen specialized in blowing ordinary objects up to absurd sizes. Then the fountain: water jets from the top of the cherry and arcs over the spoon's bowl, so the sculpture is also a working fountain that freezes in Minnesota winters. The design began as a doodle in 1985 and took three years to engineer — the cherry is steel painted in seven layers of enamel, and the spoon's handle twists to reflect the pool. Oldenburg said his giant objects are 'a way of making the ordinary strange,' and the spoon and cherry have become Minneapolis's unofficial emblem.",
        "Spoonbridge and Cherry (1988) — the bus-length spoon",
        ["Pop Art", "Public Sculpture"],
    ),
    "artw-cadillac-ranch-247": _entry(
        "Ant Farm (1974)",
        "Cadillac Ranch (1974)",
        "Ten half-buried Cadillacs standing nose-down in a Texas field, their tailfins pointing at the sky — the most famous roadside art in America, built by the art collective Ant Farm in 1974 on a ranch outside Amarillo. The cars are buried at the same angle as the Great Pyramid of Giza, and visitors are encouraged to spray-paint them — they're repainted constantly.",
        "Look at the row: ten Cadillacs from 1949 to 1963, buried nose-first at the same angle as the Great Pyramid, their tailfins rising like a wave — the fins chart the evolution of American car design in a single line. Then the paint: the cars are covered in layers of spray-paint put there by visitors — the ranch encourages it, and the cars are repainted and re-covered in an endless cycle. The sculpture began as a tribute to the American car and its tailfins, and it's now both a monument and a canvas: no two visits look the same. The site is free and open around the clock, and its silhouette against the Texas sunset is one of the great images of American roadside culture.",
        "Cadillac Ranch (1974) — the tailfin wave at sunset",
        ["Land Art", "Public Sculpture"],
    ),
    "artw-watts-towers-248": _entry(
        "Simon Rodia (1954)",
        "Watts Towers (1954)",
        "An Italian immigrant named Simon Rodia spent 33 years building a cluster of seventeen towers from steel pipes, wire, and found objects — broken bottles, seashells, tiles — bound in mortar, in his yard in Los Angeles. He called it 'Nuestro Pueblo' ('our town'), and when a demolition order threatened the towers in 1959, a group of citizens saved them by testing their strength with a cable and crane — they held.",
        "Look at the tallest tower: it rises 30 meters — as tall as a 10-story building — and was built without scaffolding, without drawings, and almost entirely by one man with hand tools. Then the materials: the towers are decorated with fragments of broken bottles, mirrors, tiles, and seashells set into the mortar — Rodia gathered them from the neighborhood and from demolition sites. He worked every day after his factory shift, and the towers were declared a National Historic Landmark. Rodia left Los Angeles in 1955, never returned, and died without seeing them celebrated — the towers he built alone now draw visitors from around the world.",
        "Watts Towers (1954) — the 30-meter hand-built towers",
        ["Outsider Art", "Architecture"],
    ),
    "artw-girl-with-balloon-249": _entry(
        "Banksy (2002)",
        "Girl with Balloon (2002)",
        "Banksy's stencil of a little girl reaching for a red heart-shaped balloon — the most reproduced image of 21st-century street art, spray-painted on walls around the world. In 2018, a framed copy of the print sold at auction for £1 million — and the moment the hammer fell, it shredded itself through a shredder hidden in the frame. The piece was renamed Love is in the Bin, and the shredded work became worth even more.",
        "Look at the image first: the girl's arm stretches toward a red heart on a string, and the balloon is just out of reach — Banksy's critics call it sentimental, and his defenders say the sentiment is the point. Then the shredding: at the 2018 auction, as the £1 million hammer fell, the painting passed through a hidden shredder built into the frame — Banksy had installed it years earlier and the shredded work, now renamed Love is in the Bin, became the most famous art performance of the decade. The stencil has appeared on walls from London to Gaza, and its simple heart and child have made it the definitive image of the artist who refuses to reveal his face.",
        "Girl with Balloon (2002) — the shredded auction moment",
        ["Street Art", "Stencil"],
    ),
    "artw-the-sleeping-gypsy-250": _entry(
        "Henri Rousseau (1897)",
        "The Sleeping Gypsy (1897)",
        "Rousseau's painting of a sleeping woman in a desert with a lion standing over her — the lion, though hungry, does not move. Rousseau, a self-taught toll collector, was mocked by critics who called his work childish, and he sold this painting for 300 francs in 1897; it now hangs in MoMA, one of the most beloved paintings of the 20th century.",
        "Look at the lion first: it stands inches from the sleeping woman, staring straight ahead, but it does not attack — Rousseau said the lion, though hungry, did not move, and the mystery of why is the painting. Then the details: the woman's striped robe, the mandolin, the water jar, and the desert with its flat horizon and pale moon — everything is painted with the exact, unblinking clarity of a child's vision, which is what made the critics laugh and what makes the painting unforgettable. The cat's tail curves up, the woman's hand rests on the sand, and the whole scene has the stillness of a dream that never ends.",
        "The Sleeping Gypsy (1897) — the lion that does not move",
        ["Naive Art", "Dreamscape"],
    ),
    "artw-napoleon-crossing-the-alps-251": _entry(
        "Jacques-Louis David (1801)",
        "Napoleon Crossing the Alps (1801)",
        "David's heroic portrait of Napoleon on a rearing white horse against the Alps — painted from imagination, because Napoleon refused to pose, and the result is pure propaganda: the real crossing happened on a mule, in good weather, with an escort. David painted five versions, each slightly different, so the Emperor could gift them to allies.",
        "Look at the pose first: Napoleon points up the mountain on a rearing horse, cape flying — but the real crossing was made on a mule, in clear weather, weeks after the snow, with guides. David never saw Napoleon on the pass; the Emperor told him 'it is not important to be exact.' Then the details: the names carved on the rocks — BONAPARTE, HANNIBAL, KAROLUS MAGNUS — place Napoleon among the generals who crossed the Alps before him. Napoleon's name is written largest, of course. Five versions exist, each with small differences in the cape and horse, and they were sent to the courts of Europe as diplomatic gifts.",
        "Napoleon Crossing the Alps (1801) — the names carved in the rock",
        ["Neoclassicism", "Propaganda"],
    ),
    "artw-i-and-the-village-252": _entry(
        "Marc Chagall (1911)",
        "I and the Village (1911)",
        "Chagall's painting of his childhood village in Russia — a green-faced man and a cow stare at each other across a dreamscape of memory, with a tiny milkmaid, a tree, and upside-down people. He painted it in Paris, two years after leaving home, and it's the painting that made his name as the poet of memory.",
        "Look at the two big faces: on the right, the artist's green profile; on the left, a cow's head with a smaller cow being milked inside its cheek — Chagall painted memory as a place where the living and the remembered share one space. Then find the details: the milkmaid, the tree with a woman's face, the man with a scythe, the upside-down couple, and the little house with a church — each one a fragment of the village he left behind. Chagall said 'the artist must be a dreamer,' and the painting arranges the dreams the way a child arranges toys: all on one table, all equally real.",
        "I and the Village (1911) — the green face and the cow",
        ["Cubism", "Memory"],
    ),
    "artw-saturn-devouring-his-son-253": _entry(
        "Francisco Goya (1819-23)",
        "Saturn Devouring His Son (1819-23)",
        "Goya's horrifying painting of the Titan Saturn eating his own child — painted on the walls of his own house in Madrid, in the series now called the Black Paintings. He painted it directly on the plaster walls for himself, never intending it for display; it was transferred to canvas decades after his death. Saturn ate his children because a prophecy said one would overthrow him.",
        "Look at the eyes first: Saturn's are wide, white, and staring — and the headless, armless body he grips is painted in cold flesh tones against the dark background. Goya painted the Black Paintings on the walls of his home, the Quinta del Sordo, after a near-fatal illness left him deaf and isolated — they were never meant to be seen. The painting was removed from the wall in the 1870s by cutting the plaster, which damaged it, and it now hangs in the Prado. The myth is Greek — Saturn eating his children — but Goya made it personal: an old man, alone, devouring what he loves, painted by a man who had seen war and madness up close.",
        "Saturn Devouring His Son (1819-23) — the staring eyes",
        ["Romanticism", "Black Paintings"],
    ),
    "artw-the-death-of-socrates-254": _entry(
        "Jacques-Louis David (1787)",
        "The Death of Socrates (1787)",
        "David's painting of Socrates calmly taking the hemlock cup while his students grieve around him — the philosopher chose death over renouncing his ideas, and David painted it as a heroic martyrdom. Every element is engineered: the light, the pointing finger of Socrates, the despairing students — and the painting became the visual definition of principle over life.",
        "Look at Socrates: he sits upright, one hand raised as he makes his final point, the other reaching for the poison cup — he is the only calm figure in the room. Then the students: one hides his face, one clutches Socrates's thigh, and the old man at the foot of the bed is Plato, painted with the face of the ancient philosopher but shown at the age of the real Socrates's execution — David mixed history and fiction freely. The composition is a stage set: the light falls on Socrates and the cup, and the stone walls frame the scene like a theater. David, who would later paint the French Revolution's martyrs, made dying for an idea look like the most dignified act a person can perform.",
        "The Death of Socrates (1787) — the calm hand and the poison cup",
        ["Neoclassicism", "History Painting"],
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
        "artw-sun-tunnels-243": "Sculpture",
        "artw-cadillac-ranch-247": "Sculpture",
        "artw-watts-towers-248": "Sculpture",
        "artw-vietnam-veterans-memorial-244": "Installation",
        "artw-the-angel-of-the-north-245": "Sculpture",
        "artw-spoonbridge-and-cherry-246": "Sculpture",
        "artw-brillo-boxes-239": "Sculpture",
        "artw-rabbit-1986-242": "Sculpture",
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
