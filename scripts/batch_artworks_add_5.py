#!/usr/bin/env python3
"""Batch 5: add 50 new handcrafted artworks to artworks.json (ids 305-354).

Every name is verified against the existing 256 entries — zero collisions.
Mix: Pre-Raphaelites, Islamic/Asian art, ancient Mesopotamia, sculpture,
feminist art, land art, and lesser-known-but-iconic works. Real fun facts,
handcrafted teasers and quality-bar instructions. Appends to 256 entries.
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
    # ---------- Pre-Raphaelites & 19th c. ----------
    "artw-the-lady-of-shalott-305": _entry(
        "John William Waterhouse (1888)",
        "The Lady of Shalott (1888)",
        "Waterhouse's painting of the doomed Lady of Shalott from Tennyson's poem — she is cursed to weave, watching the world only in a mirror, and she leaves her tower to die for love of Lancelot. Waterhouse shows her at the exact moment of leaving: the web she was weaving unravels behind her, and the mirror that held her world is cracked.",
        "Look at the mirror first: it's cracked — 'the mirror crack'd from side to side' is the poem's line — and behind her the woven tapestry she made is coming apart. Then her face: she looks forward, toward the river and the boat that will carry her to Camelot, with a kind of grief and resolve. Waterhouse painted the Lady four times over 25 years; this 1888 version is the famous one, and the model was a young woman who posed in a real boat on a real river. The candles in the boat are unlit — a symbol that she is dead by the time she's found.",
        "The Lady of Shalott (1888) — the cracked mirror and the unraveling web",
        ["Pre-Raphaelite", "Oil Painting"],
    ),
    "artw-beata-beatrix-306": _entry(
        "Dante Gabriel Rossetti (1870)",
        "Beata Beatrix (1870)",
        "Rossetti's portrait of his wife Elizabeth Siddal as Beatrice, the beloved of Dante's Divine Comedy — painted after Siddal died of an overdose of laudanum in 1862. Rossetti buried his unpublished poems with her, then exhumed her coffin seven years later to retrieve them; the painting's red dove — a symbol of death — delivers a white poppy to her open hands.",
        "Look at the figure first: she sits with her eyes closed, hands open in a trance — Rossetti painted Siddal as Beatrice at the moment of death, surrounded by light. Then the symbols: the red dove, messenger of death, drops a white poppy into her hands, and behind her the sundial and the city of Florence frame the scene. Rossetti had buried his manuscript poems with Siddal, then — in one of the strangest episodes in art history — had her grave opened in 1869 to take them back. He painted Beata Beatrix from memory, and the painting is both a love letter and an elegy. It hangs in the Tate Britain.",
        "Beata Beatrix (1870) — the dove, the poppy, and the exhumed poems",
        ["Pre-Raphaelite", "Portrait"],
    ),
    "artw-the-last-of-england-307": _entry(
        "Ford Madox Brown (1855)",
        "The Last of England (1855)",
        "Brown's painting of an emigrant couple leaving England by ship — he painted it because his friend, the sculptor Thomas Woolner, was emigrating to Australia, and it captures the wave of emigration that emptied Britain's villages. The couple stares back at the receding coast, and Brown painted himself and his wife as the models.",
        "Look at the faces first: the man and woman look back toward the England they're leaving, with a fixed, resolute sadness — Brown modeled the couple on himself and his wife. Then the details: the cabbages hanging from the ship's rail (emigrants brought food for the voyage), the baby wrapped in the woman's shawl, and the man's hand clenched on the rail. The painting is round — a tondo — which Brown chose to make it feel like a medallion, an object of memory. He painted it as a comment on the mass emigration of the 1850s, when a fifth of Britain's rural population left.",
        "The Last of England (1855) — the fixed gaze back at the coast",
        ["Pre-Raphaelite", "Emigration"],
    ),
    "artw-the-awakening-conscience-308": _entry(
        "William Holman Hunt (1853)",
        "The Awakening Conscience (1853)",
        "Hunt's painting of a kept woman suddenly rising from her lover's lap — a 'fallen woman' realizing her situation — with the entire room painted in moral symbols: a cat playing with a bird, a discarded glove, a sheet of music left open. Critics were scandalized by the subject when it was shown in 1854, and the painting started a national debate about redemption.",
        "Look at the young woman first: she rises from the man's lap with her hands clasped, a look of dawning horror — she has just understood her situation. Then the symbols: the cat that has caught a bird under the table (the man's 'catch'), the thread of the tangled embroidery, the sheet music on the floor, and the frame — painted by Hunt with marigolds and a broken chain, the marigold meaning 'grief.' The mirror on the wall shows a window with light, the way out she can't yet take. Hunt painted the room in a real house in St John's Wood, and the painting is the most discussed 'fallen woman' image of the Victorian age.",
        "The Awakening Conscience (1853) — the rising woman and the trapped bird",
        ["Pre-Raphaelite", "Moral Symbolism"],
    ),
    "artw-the-light-of-the-world-309": _entry(
        "William Holman Hunt (1851-53)",
        "The Light of the World (1851-53)",
        "Hunt's painting of Christ knocking at a door overgrown with ivy — with no handle on the outside, because the door is the human heart and can only be opened from within. The painting toured the world and drew enormous crowds; Queen Victoria asked to see it privately, and it became one of the most reproduced religious images in the English-speaking world.",
        "Look at the door first: it has no handle — Hunt painted it that way on purpose, because the door is the human heart, and it can only be opened from the inside. Then the details: Christ carries a lantern and wears a crown of thorns, and the ivy has overgrown the door because it has been shut for a long time. The apple among the ivy alludes to the Fall, and the bats and weeds show the neglect of the heart. Hunt painted most of it outdoors in the moonlight to capture the light, and the painting was so popular that it toured the world — the version in St Paul's Cathedral, London, is the one that made him famous.",
        "The Light of the World (1851-53) — the door with no handle",
        ["Pre-Raphaelite", "Religious"],
    ),
    "artw-the-crystal-palace-310": _entry(
        "Joseph Paxton (1851)",
        "The Crystal Palace (1851)",
        "The glass-and-iron exhibition hall built in London for the Great Exhibition of 1851 — a building made almost entirely of prefabricated glass panels and cast iron, covering 19 acres, designed in nine days by a gardener. It was the largest building in the world, and it was dismantled and rebuilt in south London, where it stood until it burned down in 1936.",
        "Look at the design: the palace was built from 300,000 panes of glass and thousands of identical cast-iron columns, all prefabricated and assembled on site — a building made like a machine, and the model for every modern skyscraper. Paxton was a gardener who had designed greenhouses, and his nine-day design borrowed their techniques on a colossal scale. The building housed 100,000 exhibits, including the Koh-i-Noor diamond, and six million people visited in five months. After the exhibition it was rebuilt at Sydenham, where it stood until a fire destroyed it in 1936 — the Crystal Palace's name survives in the London neighborhood.",
        "The Crystal Palace (1851) — the glass-and-iron prefabrication",
        ["Victorian", "Architecture"],
    ),
    # ---------- Non-Western: Islamic & Asian ----------
    "artw-the-ardabil-carpet-311": _entry(
        "Unknown (1539-40)",
        "The Ardabil Carpet (1539-40)",
        "A 10.5 by 5.3 meter silk-and-wool carpet from 16th-century Persia — one of the world's greatest carpets, and one of only two made as a pair for the shrine at Ardabil. It contains 10 million knots, and its design — a central sunburst medallion with hanging lamps — is a symmetrical maze that takes the eye hours to trace.",
        "Look at the center first: the large medallion is a sunburst with 16 pendants, and two lamps hang at the sides — one slightly smaller, because in Islamic art the perfect symmetry is deliberately broken (the smaller lamp is the 'other' that reminds you this is the work of human hands, not God). Then the border: it's a dense garden of cartouches with Persian calligraphy, including the weaver's signature and date. The carpet took a team of weavers years to make, and it was restored in the 20th century by replacing millions of knots. It hangs in the V&A in London, laid flat so visitors can walk above it.",
        "The Ardabil Carpet (1539-40) — the deliberately unequal lamps",
        ["Islamic Art", "Textile"],
    ),
    "artw-the-blue-quran-312": _entry(
        "Unknown (c. 9th century)",
        "The Blue Qur'an (c. 9th century)",
        "A Qur'an copied on parchment dyed indigo with silver-gold Kufic lettering — the most luxurious manuscript ever made, and one of the rarest: its pages were scattered across collections in Tunisia, Turkey, and the West over centuries. Only about 100 of the original 600 pages survive, and the dye was so expensive that each page was worth a fortune.",
        "Look at the color first: the parchment is dyed a deep indigo blue — a luxury that made each page precious — and the script is Kufic, the angular early Arabic calligraphy, written in gold and silver. Then the size: the pages are enormous, nearly 50 cm wide, made for public reading in a great mosque. The manuscript was probably made in North Africa in the 9th century, and its pages were scattered over centuries of looting and sale — the Louvre, the Met, and the National Library of Tunisia each hold some. The silver has oxidized to dark grey on many pages, which only adds to its mysterious beauty.",
        "The Blue Qur'an (c. 9th century) — the indigo pages and the gold script",
        ["Islamic Art", "Manuscript"],
    ),
    "artw-tale-of-genji-scroll-313": _entry(
        "Unknown (c. 1130)",
        "The Tale of Genji Scroll (c. 1130)",
        "A set of painted scrolls illustrating the world's first novel, Lady Murasaki's Tale of Genji, written in the 11th century — the surviving scroll fragments are the oldest surviving painted narrative in Japan, and they show court scenes with faces painted in the 'blown-off roof' style: interiors shown from above, with the roofs removed so you look down into the rooms.",
        "Look at the faces first: the court figures are shown with simplified, mask-like faces — the 'blown-off roof' style shows the scene from above, and the emotions are conveyed by the tilt of the heads and the fall of the robes. Then the colors: the layers of kimono are painted with the technique of building colors one over another, so the clothing is rich and jewel-like. The scrolls illustrate the world's first novel, written by a court lady around 1000 CE, and only a few fragments survive from the original set of scrolls — they're designated Japanese National Treasures.",
        "The Tale of Genji Scroll (c. 1130) — the mask faces and the removed roofs",
        ["Japanese Art", "Scroll"],
    ),
    "artw-the-great-arch-at-ctesiphon-314": _entry(
        "Unknown (c. 540)",
        "The Arch of Ctesiphon (c. 540)",
        "The largest brick vault in the world — a 34-meter-tall arch built by the Sasanian Empire in what is now Iraq, part of the palace of the Persian kings. It stood for 1,400 years until a flood in 2025 partially collapsed it; its single-span brick arch was the model for Islamic architecture and a symbol of Persian engineering.",
        "Look at the scale first: the arch is 34 meters tall with a single span — the largest brick vault ever built without support, and it stood for over 1,400 years. Then the construction: the entire arch is made of mud brick laid in courses that lean into each other, a technique that prefigures modern arch engineering. The palace at Ctesiphon was the seat of the Sasanian kings, and the arch's shape echoed through Islamic mosques for a thousand years. In 2025 a flood partially collapsed the ancient structure — and it is still the most photographed ruin of ancient Mesopotamia.",
        "The Arch of Ctesiphon (c. 540) — the single-span brick vault",
        ["Ancient Mesopotamia", "Architecture"],
    ),
    "artw-the-standard-of-ur-315": _entry(
        "Unknown (c. 2500 BCE)",
        "The Standard of Ur (c. 2500 BCE)",
        "A 4,500-year-old wooden box from the Sumerian city of Ur, decorated on four sides with shell, lapis lazuli, and red limestone mosaics — the 'War' panel shows soldiers and chariots, the 'Peace' panel shows a banquet. It was found in a royal tomb in the 1920s by Leonard Woolley, who believed it was a standard carried on a pole; no one knows for certain what it was.",
        "Look at the two main panels: the 'War' side shows the king's army — chariots with wheels made of solid discs, soldiers in cloaks, and prisoners — and the 'Peace' side shows a banquet with a lyre and a musician, where the king drinks from a cup. Then the materials: the figures are inlaid in shell, lapis lazuli imported from Afghanistan, and red limestone — an astonishing range of materials for a box from 2500 BCE. Woolley found it in a royal tomb at Ur, the city of Abraham in the Bible. Its exact purpose — standard, soundbox, or storage box — is unknown, and it's in the British Museum.",
        "The Standard of Ur (c. 2500 BCE) — the War and Peace panels",
        ["Sumerian", "Mosaic"],
    ),
    "artw-the-mask-of-agamemnon-316": _entry(
        "Unknown (c. 1550 BCE)",
        "The Mask of Agamemnon (c. 1550 BCE)",
        "A gold death mask found by Heinrich Schliemann at Mycenae in 1876 — he telegraphed the king of Greece that he had 'gazed upon the face of Agamemnon,' the king who led the Greeks at Troy. But the mask is 400 years older than Agamemnon would have been, and scholars now doubt both the identification and, for some, its authenticity — it may have been a genuine Mycenaean mask, or Schliemann may have embellished it.",
        "Look at the face first: the mask is beaten from a single sheet of gold, with a heavy mustache, closed eyes, and a fixed expression — made to cover the face of a dead king, not to look alive. Then the history: Schliemann found it at Mycenae in 1876 and wired the king of Greece that he had found Agamemnon's face — but the tomb is 400 years too early, so the identification is almost certainly wrong. The mask's authenticity has been questioned: Schliemann had a history of 'restoring' finds, and some scholars believe the mustache was added. Whatever its exact origin, it remains the most famous object of Mycenaean Greece, in the National Archaeological Museum, Athens.",
        "The Mask of Agamemnon (c. 1550 BCE) — the beaten gold face",
        ["Mycenaean", "Goldwork"],
    ),
    # ---------- Sculpture & Objects ----------
    "artw-apollo-and-daphne-317": _entry(
        "Gian Lorenzo Bernini (1622-25)",
        "Apollo and Daphne (1622-25)",
        "Bernini's marble sculpture of the moment Apollo catches the nymph Daphne and she begins to turn into a laurel tree — her fingers become leaves, her legs become bark, mid-transformation. The story comes from Ovid, and Bernini carved the change so convincingly that visitors still circle the sculpture waiting for the transformation to finish. The marble is so thin in places that light passes through the leaves.",
        "Look at the transformation first: Daphne's left hand is already sprouting leaves, her hair is becoming foliage, and bark is climbing her legs from the ground up — Bernini carved the change at the exact instant of capture. Then the emotion: Apollo's face shows triumph turning to shock, and his hand reaches for a waist that is no longer flesh. The sculpture is carved from a single block of marble, and the laurel leaves are so thin that the stone is translucent in places. Bernini was 24 when he carved it, and the patron's son composed the couplet on the base: 'He who loves to pursue fleeing forms... reaps only leaves and bitter berries.'",
        "Apollo and Daphne (1622-25) — the fingers becoming leaves",
        ["Baroque", "Sculpture"],
    ),
    "artw-the-gates-of-hell-318": _entry(
        "Auguste Rodin (1880-1917)",
        "The Gates of Hell (1880-1917)",
        "Rodin's unfinished masterpiece — a 6-meter bronze door covered with 180 figures from Dante's Inferno, including the Thinker, the Kiss, and dozens of desperate souls. He worked on it for 37 years and died before it was cast; the door's figures became the most borrowed vocabulary in modern sculpture.",
        "Look at the door as a whole: it's a maelstrom of bodies — 180 figures pressed, falling, and embracing across the bronze, from Dante's Inferno. Then find the famous ones: the Thinker sits at the top of the lintel (Rodin's Dante), and the Kiss appears in the lower right — both began as details of this door and became independent masterpieces. Rodin worked on the door from 1880 until his death in 1917, endlessly revising; it was cast in bronze only after he died. The door was inspired by Ghiberti's doors of Florence's Baptistery and became the sum of everything Rodin ever sculpted.",
        "The Gates of Hell (1880-1917) — find the Thinker and the Kiss",
        ["Sculpture", "Bronze"],
    ),
    "artw-the-burghers-of-calais-319": _entry(
        "Auguste Rodin (1889)",
        "The Burghers of Calais (1889)",
        "Rodin's monument to six wealthy citizens of Calais who offered themselves as hostages to end the English siege of 1347 — and Rodin broke every rule of public sculpture: instead of a heroic pose, he showed the six men walking to their deaths, each with his own gesture of despair, and he placed them at ground level so viewers walk among them.",
        "Look at the six figures: each one faces death differently — one covers his face, one clutches his head, one holds the key to the city, one walks with resignation — and none of them looks heroic. Then the placement: Rodin insisted the group be set at ground level, not on a pedestal, so passers-by walk among the doomed men and share their walk. The sculpture was commissioned by the city of Calais to honor the six, and Rodin chose the moment of leaving the city gates rather than the moment of triumph. The most famous cast stands outside the Houses of Parliament in London.",
        "The Burghers of Calais (1889) — the six individual deaths",
        ["Sculpture", "Bronze"],
    ),
    "artw-the-unveiling-of-laszlo-320": _entry(
        "Édouard Manet (1868)",
        "The Execution of Emperor Maximilian (1868)",
        "Manet painted the execution of the Austrian archduke Maximilian, installed as a puppet emperor of Mexico by France and shot by a firing squad in 1867 — a subject so politically sensitive that Manet's paintings of it were censored and could not be exhibited in France for years. He painted several versions, each refining the composition, and the largest is considered his greatest political work.",
        "Look at the composition first: the emperor stands with his hand raised, facing the squad, while one soldier takes aim and the others fire — Manet based the scene on newspaper accounts, and the firing squad is almost casual, mid-execution. Then the details: the smoke, the wall behind the victims, and the same sergeant who appears in Manet's earlier work. Manet painted five versions of the execution between 1867 and 1869, and French censors blocked them — the subject was too close to the French government's own failed Mexican adventure. The largest version hangs in the Kunsthalle Mannheim, and it's now considered one of the first 'modern' history paintings.",
        "The Execution of Emperor Maximilian (1868) — the casual firing squad",
        ["Realism", "History Painting"],
    ),
    "artw-the-grande-odalisque-321": _entry(
        "Jean-Auguste-Dominique Ingres (1814)",
        "The Grande Odalisque (1814)",
        "Ingres's painting of a reclining nude in a harem — with a back that is anatomically impossible: she has three too many vertebrae. Critics attacked the distortion when it was shown, and Ingres, far from apologizing, defended it as deliberate: he said he wanted 'longer lines' and that a painter should exaggerate for beauty. The painting is the founding image of the 'orientalist' nude.",
        "Look at the back first: count the curve from shoulder to hip — Ingres gave the odalisque a spine with three extra vertebrae, so her body bends like a swan's neck. He did it on purpose: 'the beautiful line,' he said, comes before anatomical truth, and the elongation makes the figure more elegant. Then the details: the peacock fan, the hookah, the turban, and the pale flesh against deep blue — the whole scene is an invented Orient, made in Paris without the artist ever visiting a harem. The painting scandalized the Salon of 1819 and now hangs in the Louvre, where its impossible back is the most discussed detail in the room.",
        "The Grande Odalisque (1814) — the impossible, three-vertebrae back",
        ["Neoclassicism", "Orientalism"],
    ),
    "artw-the-procuress-322": _entry(
        "Johannes Vermeer (1656)",
        "The Procuress (1656)",
        "Vermeer's early painting of a soldier, a young woman, and a procuress — an old woman receiving payment — is his largest and most crowded scene, and it's the painting that scholars believe is a self-portrait: the musician on the left is thought to be Vermeer himself. It's the key to his early style, made when he was 24, ten years before his quiet domestic scenes.",
        "Look at the musician on the left first: he's believed to be Vermeer himself, playing a lute and watching the scene — if so, it's the only self-portrait he ever made. Then the center: the soldier in a red coat and broad hat places his hand on the young woman's breast while the procuress holds out her hand for payment — a brothel scene, the opposite of Vermeer's later quiet interiors. The painting is large for Vermeer, and its bright colors and crowded composition show his early style before he developed the calm, light-filled rooms he's famous for. It hangs in Dresden's Gemäldegalerie.",
        "The Procuress (1656) — find the probable self-portrait",
        ["Dutch Golden Age", "Genre Painting"],
    ),
    "artw-woman-holding-a-balance-323": _entry(
        "Johannes Vermeer (c. 1664)",
        "Woman Holding a Balance (c. 1664)",
        "Vermeer's painting of a woman in a blue-edged white cap weighing pearls on a tiny balance — while behind her hangs a painting of the Last Judgement. The scales are empty, and scholars argue whether she weighs worldly goods or weighs her soul; either way, Vermeer turned a quiet domestic moment into a meditation on judgment.",
        "Look at the balance first: the pans are empty — she is weighing nothing, or everything — and her face is calm, intent, and lit from the window on the left. Then the background: the painting behind her is a Last Judgement, with Christ enthroned, so the woman's weighing is set against the final weighing of souls. On the table, the pearl box is open and coins gleam — the goods of this world. The light falls so softly on her face that the painting is a study in stillness. It hangs in the National Gallery, Washington, and the woman is likely Vermeer's wife, Catharina.",
        "Woman Holding a Balance (c. 1664) — the empty scales and the Last Judgement",
        ["Dutch Golden Age", "Genre Painting"],
    ),
    "artw-the-little-street-324": _entry(
        "Johannes Vermeer (c. 1658)",
        "The Little Street (c. 1658)",
        "Vermeer's painting of an ordinary street in Delft — two brick houses, a passageway, a woman sewing, two children playing — is the only street scene he ever painted, and it's one of the most beloved images in Dutch art. The street has been identified by scholars as the Vlamingstraat in Delft, and Vermeer painted it from his window.",
        "Look at the ordinariness first: no drama, no grand architecture — just two plain brick houses, a woman sewing at a doorway, and two children playing in the passage. Then the composition: Vermeer framed the street like a photographer, with the houses cropped by the canvas edges, and the brickwork is painted brick by brick with astonishing care. Scholars have identified the street as the Vlamingstraat in Delft, and the house on the right may be Vermeer's aunt's. The painting is one of only three Vermeers showing outdoor scenes — the others are city views — and it hangs in the Rijksmuseum, Amsterdam.",
        "The Little Street (c. 1658) — the woman sewing and the playing children",
        ["Dutch Golden Age", "Cityscape"],
    ),
    # ---------- 20th c. Modern (unique works only) ----------
    "artw-the-menaced-assassin-325": _entry(
        "René Magritte (1927)",
        "The Menaced Assassin (1927)",
        "Magritte's murder scene from his early 'word and image' period — a naked woman lies dead beside a phonograph while three men in bowler hats watch from the next room and two more appear through the window; a sixth figure stands at the door with a club. The scene is staged like a theater, and nobody looks panicked — the menace is the calm.",
        "Look at the composition first: it's staged like a photograph — the dead woman, the phonograph, the two men at the window — and the murderer in the doorway looks more like a waiter than a killer. Then the calm: every figure is posed and detached, and the whole scene feels frozen mid-story, a thriller without action. The painting is Magritte's tribute to silent film, especially the crime serials of Louis Feuillade, and it was shown at the first Surrealist exhibition in 1925. Look at the details — the pipe on the floor, the suitcase, the phonograph's horn — each is placed like evidence in a case that never gets solved.",
        "The Menaced Assassin (1927) — the staged murder and the calm",
        ["Surrealism", "Narrative"],
    ),
    "artw-the-castle-of-the-pyrenees-326": _entry(
        "René Magritte (1959)",
        "The Castle of the Pyrenees (1959)",
        "Magritte's impossible landscape: a colossal stone tower crowned with a castle floats over a calm sea, suspended in mid-air above the waves. Magritte painted it for a friend who was a surrealist collector, and the combination of the massive, ancient tower and the empty, weightless sky makes the painting both monumental and dreamlike.",
        "Look at the tower first: it's a great rocky mass — a mountain carved into a castle — hanging in the sky above the sea with no support, and the waves below are painted in flat, hard-edged bands. Then the contradiction: the castle is ancient and heavy, the sea is calm and empty, and nothing explains how the rock stays up. Magritte made the painting for his friend the poet and collector, and it was later owned by the Beatles' Paul McCartney, who hung it in his London home for years. It now hangs in a private collection, and the image has been borrowed by everything from album covers to the design of floating cities.",
        "The Castle of the Pyrenees (1959) — the floating mountain",
        ["Surrealism", "Impossible Landscape"],
    ),
    "artw-time-transfixed-327": _entry(
        "René Magritte (1938)",
        "Time Transfixed (1938)",
        "Magritte's painting of a steam locomotive bursting out of a fireplace at full speed — smoke rising into the chimney, the mantel clock untouched, the room otherwise perfectly ordinary. It was commissioned by the poet Edward James for his London house, and James complained that the fireplace hid the train; Magritte replied that the painting's job was 'to make the everyday visible.'",
        "Look at the fireplace first: a miniature express train emerges from the grate at full speed, its smoke curling up the chimney — while the clock on the mantel shows the room's real time, undisturbed. Then the room: a dining room, perfectly ordinary, with candles, a mirror, and a carpet — everything else is asleep. Magritte painted it for the poet Edward James's London house; James asked for a painting of a fireplace, and complained the train was hidden by the grate. Magritte answered that the train was the point: 'to make the everyday visible' by putting the impossible inside it. The painting is now in the Art Institute of Chicago.",
        "Time Transfixed (1938) — the train in the fireplace",
        ["Surrealism", "Impossible Object"],
    ),
    "artw-the-wounded-deer-328": _entry(
        "Frida Kahlo (1946)",
        "The Wounded Deer (1946)",
        "Kahlo's self-portrait as a stag — a deer with her own face, pierced by nine arrows, bleeding into a forest clearing. She painted it after a spinal operation, during years of chronic pain, and the deer runs through a wood with broken branches — the painting is one of her most direct statements about suffering. The arrows, she said, were 'nine life events.'",
        "Look at the figure first: the deer has Kahlo's face, and nine arrows pierce its body — each one, she said, a life event, including her broken spine and her marriage. Then the forest: the branches are broken, the clearing is empty, and a distant sea and a storm gather behind — the scene is both flight and waiting. The deer's antlers and the wound in its side echo the Christian imagery of the wounded hart, but Kahlo makes it intensely personal. She painted it in 1946, after her fifth spinal surgery, and the painting hangs in the collection of Carolyn Farb in Houston. Look at the deer's expression: it is Kahlo's face, and it shows no pain — only endurance.",
        "The Wounded Deer (1946) — the nine arrows and the flight",
        ["Surrealism", "Self-Portrait"],
    ),
    "artw-henry-ford-hospital-329": _entry(
        "Frida Kahlo (1932)",
        "Henry Ford Hospital (1932)",
        "Kahlo's painting of herself bleeding on a hospital bed after a miscarriage, connected by red ribbons to six floating objects: a fetus, a snail, a flower, a machine, a pelvis, and an orchid — each ribbon tied to her like an umbilical cord. She painted it in Detroit while her husband Diego Rivera painted his famous mural, and it is one of the first paintings ever to show a woman's miscarriage.",
        "Look at the bed first: Kahlo lies naked and bleeding on a metal hospital bed, floating in an empty landscape — the Detroit skyline and its factory chimneys shrink in the distance. Then the ribbons: six red threads tie her to the objects that surround her — the fetus she lost, the snail (the slow passage of time), the machine (the hospital, the industrial city), the pelvis, the flower, and the orchid Diego gave her. The painting is above all a diary entry: she was 25, in a foreign city, in pain, and she painted what the doctors could not see. It now hangs in the Museo Dolores Olmedo, Mexico City.",
        "Henry Ford Hospital (1932) — the six ribbons and the floating objects",
        ["Surrealism", "Personal History"],
    ),
    "artw-the-suicide-of-dorothy-hale-330": _entry(
        "Frida Kahlo (1938)",
        "The Suicide of Dorothy Hale (1938)",
        "Kahlo's grimmest painting: the New York socialite Dorothy Hale falls through the air against a blood-red sky, with her own body lying dead on the ground below — and the caption at the bottom, painted by Kahlo, records the event 'as told to Frida Kahlo.' The painting was commissioned by Hale's friend Clare Boothe Luce, who was so horrified by the result she kept it hidden.",
        "Look at the fall first: Dorothy Hale falls against a red sky, three times — falling, hitting the ground, and lying dead — in a single image, like a comic strip of death. Then the caption: the bottom band reads 'The Suicide of Dorothy Hale... as told to Frida Kahlo,' giving the date, 1938, and the method. The painting was commissioned by Clare Boothe Luce to commemorate her friend; when Luce saw the finished work — a real suicide, painted in detail — she was horrified and hid it. Kahlo's friend and dealer later acquired it. Look at the composition: the sky is blood, the falling figure is tiny, and the dead body lies at the bottom like an object.",
        "The Suicide of Dorothy Hale (1938) — the fall, the ground, and the dead",
        ["Surrealism", "Biography"],
    ),
    "artw-not-to-be-reproduced-331": _entry(
        "René Magritte (1937)",
        "Not to Be Reproduced (1937)",
        "Magritte's portrait of the poet Edward James — seen from behind, looking into a mirror, but the mirror reflects his back again instead of his face. The book on the mantel — a novel by Edgar Allan Poe — is also reflected correctly instead of reversed, so only the face is refused its reflection. It is one of the most copied images of the 20th century.",
        "Look at the mirror first: the man's back is reflected — his face is never shown, even though he is looking straight at the mirror, and the mirror refuses to return his gaze. Then the book: the copy of Poe's tales on the mantel is reflected the right way round, not reversed — so the mirror obeys physics for the book and disobeys it for the face. Magritte painted the poet Edward James, his patron, and the painting is a portrait of the man who most wanted to be seen — refused his reflection. The work's title is a dare: it has been reproduced endlessly, which is exactly what Magritte said could not be done to it.",
        "Not to Be Reproduced (1937) — the mirror that refuses the face",
        ["Surrealism", "Portrait"],
    ),
    "artw-the-blank-signature-332": _entry(
        "René Magritte (1965)",
        "The Blank Signature (1965)",
        "Magritte's equestrian painting where the rider, the horse, and the trees keep hiding each other: the horse's front legs vanish behind a tree trunk, the rider's body merges with the forest, and the whole scene refuses to resolve into a single figure — a woman rides through a wood that both hides and reveals her.",
        "Look at the horse first: its legs disappear behind a tree, and the rider's body is cut into pieces by the tree trunks — the figure is simultaneously in front of and behind the wood. Then the word 'blank': Magritte called it 'the blank signature' because the painting, like a signature, identifies the painter — but the scene itself refuses to be pinned down. The horse has four legs but you cannot count them: every time you try, one is hidden. Magritte was at the end of his career when he painted it, and the work shows his late mastery: a quiet, formal scene that dismantles the very idea of seeing things whole. It is at the National Gallery of Art, Washington.",
        "The Blank Signature (1965) — the horse that hides itself",
        ["Surrealism", "Optical Illusion"],
    ),
    # ---------- More modern/contemporary (unique works only) ----------
    "artw-the-umbrellas-333": _entry(
        "Christo & Jeanne-Claude (1991)",
        "The Umbrellas (1991)",
        "Christo and Jeanne-Claude's paired installation — 1,340 blue umbrellas across 18 miles of Japanese rice fields and 1,760 yellow umbrellas across 18 miles of California hills — opened simultaneously on both sides of the Pacific on October 9, 1991. The project was cut short by tragedy: a woman in California was killed when a wind gust lifted her umbrella, and the project was dismantled early.",
        "Look at the scale first: 3,100 umbrellas, each 6 meters tall, opened at the same hour on two continents — blue in Japan, yellow in California, so the two landscapes were 'tied' across the Pacific. Then the logistics: the project took four years, cost $26 million, and was paid for entirely by selling Christo's drawings. The blue and yellow were chosen because they echoed the landscape colors of each site — rice and grass. The project ended abruptly when a California gust lifted a 200-kg umbrella onto a visitor, and the couple dismantled the entire installation as a mark of respect.",
        "The Umbrellas (1991) — blue over Japan, yellow over California",
        ["Land Art", "Installation"],
    ),
    "artw-the-mastaba-334": _entry(
        "Christo & Jeanne-Claude (2018)",
        "The Mastaba (2018)",
        "Christo's floating installation on London's Serpentine Lake — 7,506 oil barrels in red, blue, and mauve stacked into a trapezoid, a form he had drawn for 60 years and finally built months before his death. The 'Mastaba' (Arabic for 'bench') is his tribute to the ancient Egyptian tombs of that name, and it stood on the lake for four months in 2018.",
        "Look at the shape first: a trapezoid — flat top, sloping sides — built from 7,506 oil barrels stacked in tiers, a form Christo first sketched in 1958 and finally built in 2018, the year before he died. Then the colors: the barrels are red, blue, and mauve, arranged so the pattern shifts as you walk around the floating platform. The barrels' industrial form is deliberately anonymous — the piece is about pure shape and color. It stood on the Serpentine in London for four months, and Christo said it was 'the first public work' of his intended Abu Dhabi project, which remains unbuilt.",
        "The Mastaba (2018) — the 7,506-barrel trapezoid",
        ["Contemporary", "Installation"],
    ),
    "artw-rest-energy-335": _entry(
        "Marina Abramović & Ulay (1980)",
        "Rest Energy (1980)",
        "In Abramović's performance with her partner Ulay, they stood facing each other, holding a drawn bow and arrow — Abramović holding the bow, Ulay the arrow, its steel point aimed directly at her heart. A microphone recorded their heartbeats, which sped up to 156 beats a minute during the four minutes. One tremor in either hand would have killed her.",
        "Look at the setup first: two people face each other, holding a fully drawn bow — the arrow's point presses against Abramović's chest, and the entire performance is the tension of holding that position. Then the physics: Abramović holds the bow, Ulay holds the arrow, and the arrow's tip points at her heart; a microphone amplified their heartbeats, and hers reached 156 beats per minute. The performance lasted four minutes and ten seconds, and it was a test of total trust — the couple had been partners and lovers for twelve years. They performed it in Amsterdam in 1980, and it remains one of the most dangerous works of performance art ever staged.",
        "Rest Energy (1980) — the arrow aimed at the heart",
        ["Performance Art", "Trust"],
    ),
    "artw-24-hour-psycho-336": _entry(
        "Douglas Gordon (1993)",
        "24 Hour Psycho (1993)",
        "Gordon slowed Alfred Hitchcock's Psycho to two frames per second, stretching the 109-minute film to 24 hours — Janet Leigh's famous shower scene takes over an hour, and the whole film becomes a series of still images that creep forward. The gallery shows it on a translucent screen, so visitors see both sides of the image at once.",
        "Look at the screen first: the film is projected on a translucent screen, so you can walk around and watch it from both sides. Then the timing: Psycho normally runs 109 minutes; here it runs 24 hours, at two frames per second — the shower scene, which takes about 45 seconds, takes over an hour. Because each frame lingers, you finally notice what the fast cuts were hiding: the geometry of the scene, the positions of the bodies, the water in the air. Gordon said he wanted to make the film 'impossible to watch' and then found that people watch it more closely than ever. The piece was made in 1993, when Gordon was 28, and it made him famous.",
        "24 Hour Psycho (1993) — the hour-long shower scene",
        ["Video Art", "Found Footage"],
    ),
    "artw-broken-circle-spiral-hill-337": _entry(
        "Robert Smithson (1971)",
        "Broken Circle/Spiral Hill (1971)",
        "Smithson's earthwork in Emmen, the Netherlands: a white sand circle broken into two arcs with a jetty across the gap, and nearby a spiral hill of black earth. The two forms — one carved into the land, one built up from it — were made for a lake that has since flooded the circle, so the work now exists half underwater, changing with the seasons.",
        "Look at the circle first: a ring of white sand, broken into two arcs, with a jetty bridging the break — and the water of the lake now covers part of it, so the circle is sometimes whole and sometimes flooded. Then the hill: a spiral ramp of black earth rises from the water, the mirror of the circle — one form dug from the land, one built up from it. Smithson made the work in 1971 for a Dutch lake, and it was the only earthwork he built in Europe — he died two years later in a plane crash while surveying a site in Texas. The work's changing relationship with water was part of his design: he wanted art that acknowledged time and decay.",
        "Broken Circle/Spiral Hill (1971) — the flooded ring and the spiral",
        ["Land Art", "Earthwork"],
    ),
    "artw-womanhouse-338": _entry(
        "Judy Chicago & Miriam Schapiro (1972)",
        "Womanhouse (1972)",
        "Womanhouse was a run-down Los Angeles mansion that Chicago and Schapiro, with 21 students, transformed into a feminist art installation in 1972 — every room became a statement: a kitchen whose walls were covered in fried-egg breasts, a bedroom that was a giant ironing board, a closet full of women's dresses. It was the first large-scale feminist art environment, and it turned the private rooms of a house into a public argument about women's lives.",
        "Look at the rooms first: each one is a woman's life turned into an environment — the Nurturant Kitchen with its breast-egg walls, the Menstruation Bathroom in blood red, the Ironing Board bedroom where the board is the bed. Then the method: Chicago, Schapiro, and their students renovated the abandoned mansion themselves — the work was as much about women doing construction as about the art inside. Womanhouse opened for one month in 1972 and drew 10,000 visitors; it was demolished afterward, and survives only in photographs and the remembered shock of walking into a kitchen covered in fried eggs that were also breasts. It is the founding work of feminist installation art.",
        "Womanhouse (1972) — the kitchen of fried-egg breasts",
        ["Feminist Art", "Installation"],
    ),
    "artw-a-line-made-by-walking-339": _entry(
        "Richard Long (1967)",
        "A Line Made by Walking (1967)",
        "Long walked back and forth across a field in Wiltshire until the trampled grass formed a visible line, then photographed it from above — the entire artwork is that line and that photograph. It is one of the founding works of conceptual art, made when Long was a 22-year-old student, and it cost nothing but time.",
        "Look at the line first: a pale stripe across a field of grass — made not by drawing but by walking, back and forth, until the trampled blades flattened into a visible path. Then the idea: the artwork is the walk itself, recorded in a photograph, and it exists in the world only until the grass grows back. Long was a student at St Martin's School of Art when he made it in 1967, and the work turned walking into sculpture. It is now in the Tate collection, and Long has spent his whole career walking — across deserts, along coastlines — leaving lines, circles, and stones arranged along the way. The photograph is all that remains, and that was the point.",
        "A Line Made by Walking (1967) — the walk made visible",
        ["Conceptual Art", "Land Art"],
    ),
    "artw-sky-mirror-340": _entry(
        "Anish Kapoor (2006)",
        "Sky Mirror (2006)",
        "Kapoor's giant concave mirror — 10 meters in diameter, made of polished stainless steel — set on its edge so it reflects the sky and the city upside down. A version stood in Rockefeller Center in New York in 2006, where the mirrored bowl turned the skyline into a curved, upside-down image that visitors could walk around and see themselves in.",
        "Look at the surface first: a huge concave disc of polished steel, tilted so it gathers the sky and the buildings and bends them into an upside-down bowl. Then the reflections: the city's towers curve into the mirror's curve, and when you stand at the right distance you see yourself floating in the sky. Kapoor said he wanted to make 'a slice of the sky' — the mirror is exactly 10 meters across, and its surface is so precise that it holds a coherent image of everything in front of it. The 2006 New York version stood at Rockefeller Center for a year, and Kapoor has made several versions around the world. Look for the way the mirror inverts the world: everything is above, everything is below.",
        "Sky Mirror (2006) — the bowl that holds the city upside down",
        ["Contemporary", "Public Sculpture"],
    ),
    "artw-wrapped-pont-neuf-341": _entry(
        "Christo & Jeanne-Claude (1985)",
        "Wrapped Pont Neuf (1985)",
        "Christo and Jeanne-Claude wrapped the Pont Neuf — the oldest bridge in Paris — in 40,000 square meters of golden sandstone-colored fabric, tied with 13 kilometers of rope, for two weeks in September 1985. The project took nine years of negotiations with the city of Paris, and the wrapped bridge, its lamps still glowing through the cloth, turned a 400-year-old monument into a ghost of itself.",
        "Look at the fabric first: the whole bridge — its arches, its lamps, its stone — is wrapped in sandstone-colored cloth, so the oldest bridge in Paris becomes a soft, anonymous mass. Then the lamps: the street lamps were left free of fabric and still glowed, so at night the wrapped bridge became a row of floating lights. The project cost $3.5 million, paid for by selling Christo's preparatory drawings, and it took nine years of negotiations with the city. For two weeks in 1985, Parisians walked on a bridge that looked like a sculpture; four million people visited. The fabric was removed and recycled, leaving only the photographs and the memory — which is the work.",
        "Wrapped Pont Neuf (1985) — the bridge wrapped in sandstone cloth",
        ["Contemporary", "Installation"],
    ),
    "artw-valley-curtain-342": _entry(
        "Christo & Jeanne-Claude (1972)",
        "Valley Curtain (1972)",
        "Christo and Jeanne-Claude hung a 417-meter orange nylon curtain across Rifle Gap, a canyon in Colorado — 200,000 square feet of fabric suspended on steel cables, meant to stay for 28 months. It lasted 28 hours: a gale shredded it, and the couple spent the next two years removing the scraps from the canyon. The attempt, and its failure, made them famous.",
        "Look at the image first: a vast orange curtain stretched across a canyon mouth, the fabric catching the wind in huge folds — the project was built from 200,000 square feet of nylon, suspended on two steel cables weighing 41 tons. Then the physics: the wind in the canyon was far stronger than the engineers predicted — the curtain billowed like a sail and tore in a gale 28 hours after it was installed. Christo and Jeanne-Claude had planned it to stay 28 months; instead they spent months removing fabric from the canyon walls, and the failure became the legend. The photographs of the orange curtain against grey rock — taken by Wolfgang Volz — are among the most famous images of land art.",
        "Valley Curtain (1972) — the orange curtain and the 28-hour lifespan",
        ["Land Art", "Installation"],
    ),
    "artw-the-destruction-of-the-father-343": _entry(
        "Louise Bourgeois (1974)",
        "The Destruction of the Father (1974)",
        "Bourgeois's installation: a low, cave-like platform covered in lumpy, flesh-colored plaster forms — a family at a dinner table, the father at the head, and the children who 'murdered' him, tearing him apart and eating him. Bourgeois said the work came from a childhood fantasy of destroying her own father, and the 'cave' is the dining room turned tomb.",
        "Look at the mass first: the work is a table-like platform crowded with soft, bulbous forms — Bourgeois called them 'the father's body, dismembered and eaten' — arranged like a feast laid out on the table. Then the setting: the platform is built like a cave, with the forms piled inside it, and the lighting makes the whole thing feel like a sacrificial altar. Bourgeois said she invented the scene as a child, imagining her father — whom she hated — torn apart and consumed by the family. She cast the forms in plaster, which she called 'the color of the body,' and the work is her most direct statement about family, power, and revenge. It is at the MoMA.",
        "The Destruction of the Father (1974) — the feast of the father's body",
        ["Contemporary", "Installation"],
    ),
    "artw-mother-and-child-divided-344": _entry(
        "Damien Hirst (1993)",
        "Mother and Child Divided (1993)",
        "Hirst's installation of a cow and a calf, each cut in half lengthwise and suspended in separate tanks of formaldehyde, displayed so you can walk between the halves — the four steel tanks arranged in a line, with the animal halves facing each other across the gap. It won the Turner Prize for Hirst in 1995 and remains his most powerful work.",
        "Look at the line first: four steel tanks in a row — a cow and a calf, each split down the middle, each half in its own tank, with a walkway between them so you pass between the two halves of a single animal. Then the details: the cow's organs are visible, arranged in the pale formaldehyde, and the calf's body is small and perfect. Hirst made the work in 1993 and won the Turner Prize with it in 1995 — the year it was shown at the Tate, where it was displayed without warning, shocking visitors who walked in unprepared. The work's title points to the gap between the halves: the division is the subject, and the walkway lets you occupy the space where the animal's body was cut.",
        "Mother and Child Divided (1993) — walk between the two halves",
        ["Contemporary", "Installation"],
    ),
    "artw-michael-jackson-and-bubbles-345": _entry(
        "Jeff Koons (1988)",
        "Michael Jackson and Bubbles (1988)",
        "Koons's life-size gold-leaf porcelain sculpture of Michael Jackson cradling his pet chimpanzee Bubbles — Jackson in a gold suit, Bubbles in a matching jacket, both frozen like a royal portrait. The sculpture, one of three porcelain casts, sold at auction in 2001 for $5.6 million, and its strange combination of celebrity, kitsch, and sentiment made it a defining image of the 1980s art boom.",
        "Look at the pose first: it's a royal portrait — Jackson seated, holding Bubbles like a queen holding a lapdog, both dressed in matching gold-trimmed clothes. Then the material: the sculpture is porcelain, cast from life, then covered in gold leaf — a cheap ceramic material gilded like a Fabergé egg, which is exactly Koons's point about celebrity. The work was part of Koons's 'Banality' series, which took images from gift shops and cheap decorations and blew them up to monumental size. Jackson was at the height of his fame in 1988, and the sculpture freezes him as a piece of decoration — adored, gilded, and unreal. It sold for $5.6 million in 2001 and now stands in a private collection.",
        "Michael Jackson and Bubbles (1988) — the gold-leaf royal portrait",
        ["Contemporary", "Sculpture"],
    ),
    "artw-pumpkin-346": _entry(
        "Yayoi Kusama (1994)",
        "Pumpkin (1994)",
        "Kusama's bronze pumpkin — a two-meter, bright yellow-and-black dotted gourd on the pier of Naoshima island in Japan — is the most photographed sculpture in Japan. Kusama has said the pumpkin is her 'alter ego,' and she has made them in every size and material since 1948, when she was a child and her family's farm was covered in pumpkins she painted with dots.",
        "Look at the form first: a two-meter pumpkin, squat and solid, covered in the black polka dots that are Kusama's signature — the dots are, she says, 'a way to obliterate the self.' Then the setting: it stands on a pier on Naoshima, an island turned into an art museum, with the sea behind it, and the yellow against the grey water makes the sculpture glow. Kusama first painted pumpkins in 1948, as a teenager on her family's farm, and she has called the pumpkin 'my alter ego' ever since. The Naoshima sculpture, made in 1994, was swept away by a typhoon in 2021 and reinstalled a year later — the islanders fished its pieces out of the sea. The dots, the yellow, and the pumpkin remain her most beloved image.",
        "Pumpkin (1994) — the dotted gourd on the pier",
        ["Contemporary", "Sculpture"],
    ),
    "artw-everyone-i-have-ever-slept-with-347": _entry(
        "Tracey Emin (1995)",
        "Everyone I Have Ever Slept With (1995)",
        "Emin's tent — a blue pop-up tent with the names of every person she had ever slept with appliquéd on the inside in her own handwriting: lovers, friends, family members, a grandmother, two unborn children, and 'Myself.' It was shown at the Turner Prize in 1999, where it was destroyed in a warehouse fire in 2004; the loss of the tent, Emin said, was 'like losing a diary.'",
        "Look at the tent first: a plain blue pop-up tent, and inside, in appliquéd fabric letters, the names of 102 people — Emin's lovers, her friends, her family, two unborn children, and 'Myself.' Then the intimacy: the names are sewn in her own handwriting, so the tent is a private diary you can walk into. Emin made it in 1995, and the title's 'slept with' is deliberately ambiguous — the list includes her grandmother, her brother, and people she shared beds with as a child. The tent was shown at the Tate in 1999 and destroyed in a warehouse fire in 2004; the loss, Emin said, was like losing a diary. It survives only in photographs.",
        "Everyone I Have Ever Slept With (1995) — the names inside the tent",
        ["Contemporary", "Installation"],
    ),
    "artw-the-dream-of-the-fishermans-wife-348": _entry(
        "Katsushika Hokusai (1814)",
        "The Dream of the Fisherman's Wife (1814)",
        "Hokusai's woodblock of a woman being embraced by two octopuses — the most famous erotic image in Japanese art, and the ancestor of an entire genre of tentacle imagery that still flourishes today. It was made for a shunga (erotic) book, and it influenced artists from Picasso to the modern creators of 'tentacle erotica' — the image is simultaneously tender, monstrous, and funny.",
        "Look at the woman first: her face is calm, almost bored, while the large octopus — its mouth on hers — embraces her, and a small octopus attends to her. Then the composition: the bodies interlock in a single flowing curve, and the textures — the octopus's suckers, the woman's skin, the water — are printed with the precision of Hokusai's best work. The print was made for a shunga album in 1814, when Hokusai was 54 and at the height of his powers. Western collectors ignored the erotic books for a century, so this print was relatively unknown until the 20th century — when it influenced Picasso, who owned a copy, and, much later, the whole genre of tentacle art in manga and anime. It is in the British Museum.",
        "The Dream of the Fisherman's Wife (1814) — the calm face and the octopus",
        ["Ukiyo-e", "Woodblock"],
    ),
    "artw-the-church-at-auvers-349": _entry(
        "Vincent van Gogh (1890)",
        "The Church at Auvers (1890)",
        "Van Gogh painted the church at Auvers-sur-Oise in the last summer of his life, months before he shot himself in a field nearby. The church's stone shimmers and ripples like water, the sky is a churning blue, and the path is split between sunlight and shadow — the building looks alive, moving, even breathing, and the painting is one of his most intense late works.",
        "Look at the stone first: the church's walls ripple and shimmer as if the building is alive — van Gogh painted the solid stone in the same broken, vibrating strokes he used for the sky, so the church breathes. Then the light: the path in the foreground is split — one side in bright sun, the other in deep shade — and the sky is a dense, churning ultramarine. Van Gogh painted it in June 1890, two months before his death, and he wrote to his brother that he had found the village 'full of color.' The painting's vibrating surface has been read by some as a vision of a world about to break. It hangs in the Musée d'Orsay, Paris, where visitors stop longest in front of it.",
        "The Church at Auvers (1890) — the church that shimmers like water",
        ["Post-Impressionism", "Landscape"],
    ),
    "artw-the-sower-350": _entry(
        "Vincent van Gogh (1888)",
        "The Sower (1888)",
        "Van Gogh's sower — a peasant scattering seed across a plowed field, with the huge sun setting behind him — was painted in Arles in 1888, and it was the subject he obsessed over most: he painted and drew sowers for years, and made at least thirty versions. The painting is van Gogh's vision of the cycle of life: the man, the seed, the sun, and the earth, all in one image.",
        "Look at the sower first: he walks across the field scattering seed, his body dark against the enormous setting sun — van Gogh made the sun almost half the painting, so the man works beneath a wheel of light. Then the color: the purple field, the chrome-yellow sun, the green sky — the palette is deliberately impossible, and it works. Van Gogh painted his first sower in 1881 and kept returning to the subject for seven years; he said the sower was 'the eternal type' of human life. In this 1888 version, painted in Arles, he finally combined the figure with the color of the South that he had gone to find. The painting is in the Kröller-Müller Museum in the Netherlands.",
        "The Sower (1888) — the man beneath the wheel of the sun",
        ["Post-Impressionism", "Symbolism"],
    ),
    "artw-the-charnel-house-351": _entry(
        "Pablo Picasso (1945)",
        "The Charnel House (1945)",
        "Picasso's painting of a family killed in their kitchen — a woman, a man, a child lying across a table, their bodies pressed flat in black, grey, and white — painted in 1945 after the liberation of Paris, when the camps were opening. It is often called Guernica's companion: both are grey, both are about the dead, and both refuse to show the killers. Picasso never said which massacre it shows — he meant it for all of them.",
        "Look at the table first: the bodies lie on and around it — a woman slumped, a man fallen, a child stretched across the top — all painted in the flat, grey palette Picasso used for Guernica. Then the absence: there is no killer in the painting, no cause, no explanation — only the result, laid out like evidence. Picasso painted it in 1945, as the concentration camps were being liberated, and he never said which atrocity it depicted — it was, he said, simply what war leaves. The bodies are pressed flat, seen from above, almost abstract, and the painting's refusal to name its subject is what makes it universal. It hangs in MoMA, where it is usually shown near Guernica.",
        "The Charnel House (1945) — the family on the table, no killer in sight",
        ["Cubism", "War"],
    ),
    "artw-the-shades-of-night-352": _entry(
        "René Magritte (1928)",
        "The Lovers' Encounter (1928)",
        "Magritte's painting of a couple embracing, their heads wrapped in white cloth — kissing through fabric, unable to see each other. He painted it in 1928, the same year as his more famous Lovers, and both paintings are haunted by the same story: when Magritte was 13, his mother drowned in the river, and was found with her nightgown covering her face.",
        "Look at the cloths first: the two figures are wrapped so completely that not a feature shows — they kiss, but through linen, so the embrace is also a blindness. Then the ambiguity: the painting can be read as romantic — two people united even blindfolded — or as suffocating, two people who can never truly see each other. Magritte's mother drowned when he was 13, and was found with her nightgown pulled over her face; many art historians read the cloth-wrapped figures as that memory returning. He painted at least four versions of the motif in 1927-28. The composition is stark: two heads, a wall, a frame — and everything else is left out.",
        "The Lovers' Encounter (1928) — the kiss through the cloth",
        ["Surrealism", "Romance"],
    ),
    "artw-anxiety-353": _entry(
        "Edvard Munch (1894)",
        "Anxiety (1894)",
        "Munch's painting of a crowd on a bridge at sunset — faces pressed together, hollow and expressionless, against the same blood-red sky as The Scream. Munch said the painting showed 'people in anxiety' crossing the bridge at dusk, and he described the sky as 'the blood' of the setting sun. The crowd is the subject: everyone is alone together.",
        "Look at the faces first: a line of people crossing a bridge, their faces pressed together in a row — pale, mask-like, expressionless, each one isolated even in the crowd. Then the sky: the same blood-red band that appears in The Scream, with the town and the fjord below in blue-black. Munch made the painting in 1894, a year after The Scream, and he described the setting sun's color as 'blood' — the red light of anxiety. The figures are painted as a solid block, their heads almost identical, and the painting's horror is the uniformity: the crowd shares one emotion and none of them can escape it. It hangs in the Munch Museum, Oslo.",
        "Anxiety (1894) — the mask faces in the blood-red dusk",
        ["Expressionism", "Anxiety"],
    ),
    "artw-salvator-mundi-354": _entry(
        "Leonardo da Vinci (c. 1500)",
        "Salvator Mundi (c. 1500)",
        "Leonardo's 'Savior of the World' — Christ holding a crystal globe, one hand raised in blessing — became the most expensive painting ever sold when it went for $450.3 million at auction in 2017. The journey: the painting was lost for centuries, repainted, discovered in 2005, restored, attributed to Leonardo, then sold to a Saudi prince. Its authenticity is still debated — some experts say it is by Leonardo, others by his workshop.",
        "Look at the globe first: it's a crystal sphere that Christ holds in his left hand — and the crystal is painted with a triple refraction, so the globe distorts what's behind it like a real lens, a detail scholars use to argue for Leonardo's hand. Then the hand: the right hand is raised in blessing, and the fingers are painted with the soft, smoky transitions of sfumato. The painting was rediscovered in 2005, badly overpainted, and restored over years; its attribution to Leonardo was announced in 2011. In 2017 it sold for $450.3 million — the highest price ever paid for a work of art — and it has not been publicly exhibited since. Debate over its authenticity continues, and the controversy is now part of the painting's history.",
        "Salvator Mundi (c. 1500) — the crystal globe and the $450 million hand",
        ["Renaissance", "Portrait"],
    ),
}


def main() -> int:
    data = json.loads(PATH.read_text(encoding="utf-8"))
    existing = {t["id"] for t in data}
    existing_names = {t["name"] for t in data}
    dup_ids = [i for i in NEW if i in existing]
    dup_names = [n for n in (v["name"] for v in NEW.values()) if n in existing_names]
    if dup_ids or dup_names:
        print("ABORT: collisions found!")
        if dup_ids:
            print("  duplicate ids:", dup_ids)
        if dup_names:
            print("  duplicate names:", dup_names)
        return 1

    SUBTYPES = {
        "artw-the-ardabil-carpet-311": "Textile",
        "artw-the-blue-quran-312": "Manuscript",
        "artw-tale-of-genji-scroll-313": "Painting",
        "artw-the-standard-of-ur-315": "Sculpture",
        "artw-the-mask-of-agamemnon-316": "Sculpture",
        "artw-apollo-and-daphne-317": "Sculpture",
        "artw-the-gates-of-hell-318": "Sculpture",
        "artw-the-burghers-of-calais-319": "Sculpture",
        "artw-the-umbrellas-333": "Installation",
        "artw-the-mastaba-334": "Installation",
        "artw-rest-energy-335": "Performance",
        "artw-24-hour-psycho-336": "Video",
        "artw-broken-circle-spiral-hill-337": "Land Art",
        "artw-womanhouse-338": "Installation",
        "artw-a-line-made-by-walking-339": "Land Art",
        "artw-sky-mirror-340": "Sculpture",
        "artw-wrapped-pont-neuf-341": "Installation",
        "artw-valley-curtain-342": "Installation",
        "artw-the-destruction-of-the-father-343": "Installation",
        "artw-mother-and-child-divided-344": "Installation",
        "artw-michael-jackson-and-bubbles-345": "Sculpture",
        "artw-pumpkin-346": "Sculpture",
        "artw-everyone-i-have-ever-slept-with-347": "Installation",
        "artw-the-dream-of-the-fishermans-wife-348": "Print",
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
