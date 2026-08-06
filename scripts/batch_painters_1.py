#!/usr/bin/env python3
"""Batch: replace the first 40 fake painters.json entries with real facts.

Template-generated entries with boilerplate teasers and placeholder tags
like Renaissance|15th Century on 20th-century artists. Replaces teaser +
instruction + targetName + tags (subtype/verb preserved). Cap 450.
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


PATH = Path(__file__).resolve().parent.parent / "app/src/main/assets/topics/painters.json"


def _entry(teaser: str, instruction: str, target_name: str, tags: list[str]) -> dict:
    return {
        "teaser": teaser,
        "instruction": instruction,
        "targetName": target_name,
        "tags": tags,
    }


FIXES: dict[str, dict] = {
    "pain-gustav-klimt-177": _entry(
        "The leader of the Vienna Secession scandalized Austrian society by painting sex — the university ceiling commissions were so controversial the faculty refused to hang them. His 'golden phase' — The Kiss, Adele Bloch-Bauer I — is the most reproduced image of the era, and the Bloch-Bauer portrait's legal history (looted by Nazis, recovered, sold for $135 million) made him a courtroom figure a century after his death.",
        "Look at The Kiss and then at the university ceiling paintings (Philosophy, Medicine, Jurisprudence) side by side: one is velvet decoration, the other is a war on convention. Then find the 'Golden Adele' and read its provenance — the 2006 restitution case that rewrote the rules for Nazi-looted art. Klimt's bodies are made of ornament; the question is whether the ornament hides them or IS them.",
        "Gustav Klimt — 'The Kiss' (1908) and the university ceiling paintings",
        ["Art Nouveau", "Austrian", "Symbolism"],
    ),
    "pain-edvard-munch-178": _entry(
        "Munch's The Scream — painted in 1893 — is the second-most-famous image in Western art after the Mona Lisa, and he made four versions of it. He wrote the moment down first: 'I felt the great scream throughout nature,' after a sunset turned 'blood red' — and he described his art as a diary of 'the modern life of the soul.'",
        "Read Munch's diary entry about the sunset, then look at the 1893 version in Oslo's National Museum: the screaming figure has no ears, and the landscape is doing the screaming. Then look at 'The Sun' (1911), painted twenty years later — the same artist who made the scream painted a blazing, joyful sunrise for Oslo University's hall; the difference is the whole story of his career.",
        "Edvard Munch — 'The Scream' (1893) and 'The Sun' (1911)",
        ["Expressionism", "Norwegian", "Symbolism"],
    ),
    "pain-marc-chagall-179": _entry(
        "Chagall painted flying lovers, fiddlers, and upside-down villages — the folk world of his Jewish childhood in Vitebsk lifted into the air. He was a pioneer of modernism who refused to join any modern movement, and he also designed the stained-glass windows of Jerusalem's Hadassah hospital and the ceiling of the Paris Opera.",
        "Look at 'I and the Village' (1911) and trace the non-Euclidean space: the cow's head, the man's face, the village — all sharing one canvas without perspective. Then find the Paris Opera ceiling (1964), which he painted at 77, suspended over the audience — Chagall's answer to the question of how a folk painter survives modernism: he took his village with him wherever he went.",
        "Marc Chagall — 'I and the Village' (1911) and the Paris Opera ceiling",
        ["Modernism", "Russian-French", "Folk"],
    ),
    "pain-salvador-dalí-180": _entry(
        "Dalí painted the most famous surrealist image ever made — The Persistence of Memory (1931), with its melting watches — and turned surrealism into performance art before the term existed: the anteater, the mustache, the submarine press conferences. He claimed his method, 'paranoia-criticism,' let him see images in the world that weren't there.",
        "Look at the melting watches and then read Dalí's own explanation: he said they were 'the camembert cheese of time' — he'd been eating soft cheese before a dream. Then find 'Soft Construction with Boiled Beans (Premonition of Civil War)' (1936): the same painter who made the soft watches painted a screaming self-dismembering figure a few years later, and it is not a joke. Compare the two and decide what Dalí was really after.",
        "Salvador Dalí — 'The Persistence of Memory' (1931) and the Civil War painting",
        ["Surrealism", "Spanish", "20th Century"],
    ),
    "pain-paul-klee-181": _entry(
        "Klee — who described drawing as 'taking a line for a walk' — was a Swiss-German artist who taught at the Bauhaus and made over 9,000 works in small, jewel-like formats. His Twittering Machine (1922) is one of the most beloved and unsettling images of the century: a machine that may be birds, or may be a torture device, drawn with four squiggles.",
        "Look at 'Twittering Machine' and count what's actually there: a crank, four birds, a wire — nothing else, and it still reads as a horror scene. Then read Klee's 1920 'Creative Credo,' where he wrote that art 'does not reproduce the visible; it makes visible.' The tiny scale of most Klees is deliberate — he said the small format forces the viewer to come close, which is where the work happens.",
        "Paul Klee — 'Twittering Machine' (1922) and the 'Creative Credo'",
        ["Bauhaus", "Swiss", "Modernism"],
    ),
    "pain-diego-rivera-131": _entry(
        "Rivera painted the 20th century's most ambitious public murals — the 27 panels of the Detroit Industry cycle and the Rockefeller Center commission that was destroyed when he refused to remove a portrait of Lenin. He is also remembered for his marriage to Frida Kahlo, which was as much an artwork as any mural.",
        "Look at the Detroit Industry murals and notice the politics inside the machinery: Rivera painted assembly lines as cathedrals, with workers as the congregation — and then put a child being born among the gears. Then read the Rockefeller Center story: he painted 'Man at the Crossroads,' the Rockefellers had it chipped off the wall in 1934, and Rivera re-created it in Mexico City as 'Man, Controller of the Universe.'",
        "Diego Rivera — 'Detroit Industry' (1933) and the Rockefeller Center story",
        ["Mexican", "Muralism", "Social Realism"],
    ),
    "pain-edward-hopper-132": _entry(
        "Hopper's Nighthawks (1942) — four people in a diner at night, none talking — is the iconic image of American urban loneliness, and Hopper himself said he was 'unconsciously painting the loneliness of a large city.' He painted it shortly after Pearl Harbor, during the blackouts, when windows were darkened — which may be why the diner glows so hard.",
        "Look at Nighthawks and notice what's missing: no door visible to the street, a counter that ends where the window begins, and the four figures separated by exactly the width of the countertop. Then look at 'Early Sunday Morning' (1930), the empty street Hopper painted a decade before — the loneliness was there before the war. Hopper's wife Jo modeled all the women in his paintings, which adds a strange self-portrait layer to every one.",
        "Edward Hopper — 'Nighthawks' (1942) and 'Early Sunday Morning'",
        ["American", "Realism", "20th Century"],
    ),
    "pain-norman-rockwell-133": _entry(
        "Rockwell painted 322 Saturday Evening Post covers over 47 years, making him the most reproduced artist in American history — and he was dismissed as an illustrator until late in life, when critics began arguing the best of his work is genuine art. His 1964 painting of Ruby Bridges, a six-year-old Black girl walking to school past a wall scrawled with a racial slur, is now among the most important images of the civil rights era.",
        "Look at 'The Problem We All Live With' (1964) and notice the details Rockwell chose: the tomato splatter, the marshals' badges, the white dresses of the guards' presence — and the girl's dignity. Then read the history: Rockwell left the Post the year before over censorship (the Post refused to run an anti-segregation image), and this painting appeared in Look magazine instead.",
        "Norman Rockwell — 'The Problem We All Live With' (1964)",
        ["American", "Illustration", "20th Century"],
    ),
    "pain-francis-bacon-134": _entry(
        "Bacon's screaming, distorted figures — most famously his 1953 reworking of Velázquez's portrait of Pope Innocent X — are among the most violent images in modern painting. He worked from photographs, never from life, and once said of the pope series: 'I wanted to paint the scream more than the horror... I feel ever so sorry for the Pope but I have no choice.'",
        "Look at Velázquez's original portrait of Innocent X (1650) and then Bacon's 'Study after Velázquez's Portrait of Pope Innocent X' (1953) side by side: same pose, different universe. Then read what Bacon kept and broke — the purple drapery, the gilded chair survive; the face dissolves into screaming strokes. Bacon claimed the distortion made the image more real, and the comparison is the test of that claim.",
        "Francis Bacon — 'Study after Velázquez' (1953) vs. the Velázquez original",
        ["Figurative", "Irish-British", "20th Century"],
    ),
    "pain-bridget-riley-135": _entry(
        "Riley's black-and-white paintings of the mid-1960s — precise stripes and curves that appear to move, shimmer, and bulge — made her the leading figure of Op Art and the first woman to win the International Prize at the Venice Biennale (1968). She insisted the movement was in the viewer's eye, not the canvas.",
        "Look at 'Movement in Squares' (1961) and let your eye move across it slowly: the squares contract, bow, and spring back — entirely on the flat canvas. Then read Riley's claim about her intent: she said she was trying to 'make visible the tensions and energies that we feel but cannot name,' and that the optical effects were a means, not an end. Her later work dropped black-and-white for color, which changes everything.",
        "Bridget Riley — 'Movement in Squares' (1961) and the Venice win",
        ["Op Art", "British", "20th Century"],
    ),
    "pain-anselm-kiefer-136": _entry(
        "Kiefer, born in Germany in 1945 — the year the war ended — has spent his career confronting what German artists after the war preferred to forget: his early photographs show him giving the Nazi salute in landscapes, and his paintings are built from ash, straw, lead, and charred books. He is the most important artist to take the Holocaust as his explicit subject.",
        "Look at 'Your Golden Hair, Margarete' (1981) — the poem by Paul Celan about a Jewish woman and a German woman, made visible as straw and ash — and then at Kiefer's lead books, which weigh more than a person and are displayed like reliquaries. The materials matter: ash is memory, lead is history's weight, and Kiefer's scale makes the viewer small inside it.",
        "Anselm Kiefer — 'Your Golden Hair, Margarete' (1981) and the lead books",
        ["Contemporary", "German", "Postwar"],
    ),
    "pain-yayoi-kusama-137": _entry(
        "Kusama has painted dots since childhood — she saw them in hallucinations and believed they were her way to 'obliterate' the world — and she voluntarily moved into a psychiatric hospital in Tokyo in 1977, where she still lives and works. Her Infinity Mirror Rooms, begun in the 1960s and revived in the 2010s, have become the most-photographed artworks on Earth.",
        "Read Kusama's own account of her hallucinations — she wrote that she began making dots 'as a result of the pathology' — then look at 'Infinity Nets' (1958), painted in New York when she was inventing a radical alternative to abstract expressionism. Then find the polka-dot pumpkin she returns to constantly: she has said the pumpkin is her 'alter ego,' and its comforting shape is the exact opposite of the obliterating dots.",
        "Yayoi Kusama — 'Infinity Nets' (1958) and the pumpkins",
        ["Contemporary", "Japanese", "Installation"],
    ),
    "pain-yoshitomo-nara-138": _entry(
        "Nara's paintings of sullen, big-headed children — girls with knives, cigarettes, or simply flat stares — made him the face of Japanese neo-pop and, in 2019, one of the most expensive living Asian artists when one sold for $25 million. He has said the children are not cute but 'the unknown soldier of the void.'",
        "Look at 'Knife Behind Back' (2000) and read the expression carefully: the girl is not angry so much as armored, and the painting's title does the work the face won't. Then read Nara's method: he works from memories of his own isolated childhood in postwar Japan, and the children are never victims — they're survivors. The scale shift matters too: his small paintings feel like secrets, his large ones like accusations.",
        "Yoshitomo Nara — 'Knife Behind Back' (2000) and the neo-pop children",
        ["Neo-Pop", "Japanese", "Contemporary"],
    ),
    "pain-lee-krasner-139": _entry(
        "Krasner was a major abstract expressionist who spent most of her career known as 'Mrs. Jackson Pollock' — the marriage eclipsing the work. Her 'Little Image' series of the late 1940s, dense all-over paintings built from tiny marks, anticipated the style Pollock became famous for, and after his death she emerged as one of the most rigorous painters of her generation.",
        "Look at a 'Little Image' painting (1946–49) and notice the scale: some are under two feet, built from thousands of small strokes in grids and spirals — made before Pollock's drip paintings. Then read the art-historical problem: how much of Pollock's all-over style grew out of watching Krasner work? The scholarship is careful, but the timeline is public. Her later 'umbers' and collages, made in grief after 1956, are where many critics think her best work lives.",
        "Lee Krasner — 'Little Image' series (1946–49) and the postwar collages",
        ["Abstract Expressionism", "American", "20th Century"],
    ),
    "pain-agnes-martin-140": _entry(
        "Martin's paintings — grids, horizontal bands, and delicate lines on six-foot square canvases — look like minimalism but were made as a form of devotion: she called them her 'response to the beauty of the world.' She abandoned painting for eight years in the late 1960s, drove across the country, and returned with the grids that made her famous.",
        "Look at a Martin grid and read her own description of what it is: she said her paintings 'have neither objects, nor space, nor time, nor anything — no forms' — and that she wanted to express 'the abstract response to nature' the way music does. Then read her biography: she was diagnosed with schizophrenia, and she painted the serenity on the other side of the illness. The grids get quieter as she aged, which is the whole story in the difference between 1965 and 1999.",
        "Agnes Martin — the grid paintings (1960s) and 'The Islands' (1999)",
        ["Minimalism", "American", "Abstract"],
    ),
    "pain-alice-neel-141": _entry(
        "Neel painted portraits for 60 years and called herself 'a collector of souls' — her sitters, from neighbors to Andy Warhol to the pregnant Linda Nochlin, are rendered with unsparing honesty and electric color. She was virtually unknown until the 1970s, when the feminist art movement rediscovered her; her portrait of Warhol, showing his surgery scars, is one of the century's great images.",
        "Look at the Warhol portrait (1970) — painted months after Valerie Solanas shot him, Neel shows the scars and the corset — and notice how she refuses flattery while staying tender. Then read her method: she said she painted 'the person, not the pose,' and that she tried to catch 'the absolute reality.' Compare her self-portrait at 80 (1980), naked and unflinching, with any portrait by her male peers: the honesty is the style.",
        "Alice Neel — 'Andy Warhol' (1970) and the 80-year-old self-portrait",
        ["Portraiture", "American", "Figurative"],
    ),
    "pain-betye-saar-142": _entry(
        "Saar's assemblages — boxes and collages built from found objects, racist memorabilia, and spiritual imagery — turned the African American domestic and sacred into high art. Her 1972 work 'The Liberation of Aunt Jemima,' which arms the mammy figure with a rifle and a fist, is one of the most important artworks of the Black Power era.",
        "Look at 'The Liberation of Aunt Jemima' (1972) and unpack the layers: the mammy figurine inside a box, the Aunt Jemima product label, the grenade in one hand and the rifle in the other. Then read Saar's account of finding the figurine and deciding to 'liberate' her. Her later work — 'The Black Girl's Window' (1969) is the masterpiece — folds astrology, mysticism, and memory into the same boxes.",
        "Betye Saar — 'The Liberation of Aunt Jemima' (1972) and 'The Black Girl's Window'",
        ["Assemblage", "American", "Black Art"],
    ),
    "pain-lorna-simpson-143": _entry(
        "Simpson's photographs of the 1980s and 90s — often of Black women seen from behind, paired with text — were among the first artworks to use photography to deconstruct how Black identity is looked at. 'Guarded Conditions' (1989), with its repeated images and ambiguous captions, made her, in 1990, the first Black woman to show at the Venice Biennale's main exhibition.",
        "Look at 'Guarded Conditions' and read the words: 'sex attacks, skin attacks' — the caption system is the work. Then read why Simpson photographs her subjects from behind: she has said the back view denies the viewer the 'read' they expect, forcing attention to the frame, the text, and the systems that shape how Black women are seen. Her later large-scale photo-paintings of the 2010s (the 'Snow' and 'Blue' series) move the same concern into another register.",
        "Lorna Simpson — 'Guarded Conditions' (1989) and the 'Snow' series",
        ["Photography", "American", "Conceptual"],
    ),
    "pain-kehinde-wiley-144": _entry(
        "Wiley paints Black people in the grand poses and settings of European portraiture — his street-cast subjects sit astride rearing horses or against ornate wallpaper — and in 2018 he painted the official portrait of Barack Obama, the first African American president's portrait by an African American artist. The portraits ask who gets to be painted like royalty.",
        "Look at 'Napoleon Leading the Army over the Alps' (2005) — a young Black man in sneakers on a rearing horse, replacing Jacques-Louis David's Napoleon — and notice what Wiley keeps: the pose, the drama, the heroism. Then read his casting method: he approaches strangers on the street and invites them to sit for paintings that cost more than a house. The Obama portrait is the same logic at the highest stakes.",
        "Kehinde Wiley — 'Napoleon Leading the Army over the Alps' (2005) and the Obama portrait",
        ["Contemporary", "American", "Portraiture"],
    ),
    "pain-toyin-ojih-odutola-145": _entry(
        "Odutola draws with ballpoint pens — thousands of dense, deliberate marks that build skin, fabric, and landscape — and her subjects are often imagined: entire fictional aristocratic Nigerian families rendered in ballpoint on paper. She has said she is interested in 'the politics of mark-making,' and her drawings take months each.",
        "Look at a close-up of her ballpoint technique: the skin is built from layered hatch marks in black, blue, and red ink, each stroke deliberate. Then read her 2017 'A Countervailing Theory' series, which imagines two fictional Nigerian families across generations — the drawings are a novel told in portraiture, and the imagined status of the subjects is the point: she draws what representation could look like.",
        "Toyin Ojih Odutola — the ballpoint portraits and 'A Countervailing Theory'",
        ["Contemporary", "Nigerian-American", "Drawing"],
    ),
    "pain-mickalene-thomas-146": _entry(
        "Thomas makes enormous portraits of Black women — often her mother, always from life — covered in rhinestones, patterned fabrics, and household interiors straight out of 1970s Black living rooms. Her 'Le Déjeuner sur l'herbe: Les trois femmes noires' (2010) re-stages Manet's picnic with Black women in full ownership of the frame.",
        "Look at the rhinestones up close: Thomas sets them one by one into the paint, so the work changes from a distance (portrait) to close (glittering surface). Then compare her 'Le Déjeuner sur l'herbe' with Manet's original: same composition, opposite politics — Manet's nude is objectified, Thomas's women are in charge. The materials — rhinestones, wood veneer, enamel — are the language of Black domestic aesthetics she grew up with.",
        "Mickalene Thomas — 'Le Déjeuner sur l'herbe: Les trois femmes noires' (2010)",
        ["Contemporary", "American", "Portraiture"],
    ),
    "pain-julie-mehretu-147": _entry(
        "Mehretu builds paintings at architectural scale from thousands of layered marks — architectural drawings, maps, calligraphy, and abstract gestures — that read like exploded cities seen from orbit. Her work asks what history looks like as form: she has said she wants to 'make a space for the imagination to move in.'",
        "Look at a large Mehretu ('Grey Area' is the gateway) and spend time separating the layers: the base is an architectural plan, the middle is drawn maps and notations, the top is gestural strokes that erase and puncture what's below. Then read her stated interest in 'the collapse of borders' — her paintings are named after histories (ancient empires, colonial cities), and she has said the layering is a way of 'remembering and forgetting at the same time.'",
        "Julie Mehretu — 'Grey Area' (2009) and the layered cities",
        ["Contemporary", "Ethiopian-American", "Abstract"],
    ),
    "pain-tschabalala-self-148": _entry(
        "Self builds Black female bodies from stitched fabric, paint, and print — her figures are part sculpture, part painting, and they inhabit the space of Harlem bodegas, bedrooms, and streets. Her series 'Bodega Run' (2016) turned the corner store into a stage, and her layered technique makes the body itself read as assembled rather than given.",
        "Look at 'Bodega Run' and notice how the figures are made: the skin is fabric sewn into the canvas, the hair is yarn, the pose is painted over the cloth — Self has said she wants to 'represent the black female body as a site of complexity.' Then read her stated project: the bodies are never passive objects of looking; they're occupied, active, and built, and the seams are deliberately visible.",
        "Tschabalala Self — 'Bodega Run' (2016) and the fabric-painted figures",
        ["Contemporary", "American", "Figurative"],
    ),
    "pain-flora-yukhnovich-149": _entry(
        "Yukhnovich paints rococo — the frothy 18th-century style of Tiepolo and Boucher — as seen through a modern lens: her canvases of billowing fabric, clouds, and naked nymphs dissolve into abstract brushwork, and she has said she is 're-presenting' historical painting 'through the filter of how we see images now.'",
        "Look at 'If I Were a Melon' (2018) and then at the Tiepolo it descends from: Yukhnovich keeps the rococo's swelling forms but removes the story, so the composition becomes pure movement. Then read her process: she begins with found images — including low-resolution internet reproductions — and the soft-focus, filtered look of her surfaces is deliberate, not accidental. She is painting what the 18th century looks like after Instagram.",
        "Flora Yukhnovich — 'If I Were a Melon' (2018) and the rococo sources",
        ["Contemporary", "British", "Rococo"],
    ),
    "pain-shara-hughes-150": _entry(
        "Hughes paints invented landscapes — vivid, imaginary views that borrow from salt marshes, coastlines, and the pattern of a tulip poplar, but exist nowhere on Earth. She has said her landscapes are 'portraits' of places she's never been, and her work broke out in the 2010s as a rare fusion of landscape painting and contemporary abstraction.",
        "Look at a Hughes landscape and notice the tension: the forms suggest a real place (a marsh, a ridge) while the color and brushwork refuse to confirm it. Then read her method: she works from memory, imagination, and close observation of natural light, painting the feeling of a place rather than its coordinates. Her titles often name real ecosystems (the Lowcountry, the salt marsh) while the images stay invented — the names are anchors, not descriptions.",
        "Shara Hughes — the invented landscapes and the salt-marsh series",
        ["Contemporary", "American", "Landscape"],
    ),
    "pain-otis-kwame-kye-quaicoe-151": _entry(
        "Quaicoe, a Ghanaian painter, makes large portraits of Black men and women whose presence fills the canvas — subjects in sharp suits, sunglasses, and confident poses against flat, graphic backgrounds. He has said he paints 'the self-assuredness of the people around me,' and his work became a market sensation in the early 2020s.",
        "Look at a Quaicoe portrait and read the pose: the figures meet the viewer's gaze with an ease that borders on challenge, and the flat monochrome backgrounds push the figure forward. Then read the context of his breakthrough: he trained in Ghana, moved to the US, and his portraits of 'men on the move' — musicians, boxers, neighbors — made him, by 2021, one of the most collected living African artists.",
        "Otis Kwame Kye Quaicoe — the 'self-assured' portraits of Black men",
        ["Contemporary", "Ghanaian", "Portraiture"],
    ),
    "pain-issy-wood-152": _entry(
        "Wood paints deadpan images of ordinary things — cars, teeth, silk dresses, a single chair — sourced from screenshots and catalogues, in a muted palette that makes the everyday feel ominous. She also makes music under the name 'Sega Bodega' collaborators, and her paintings' flatness is a statement about how images circulate now.",
        "Look at a Wood painting of an everyday object (a car interior, a dress on a hanger) and notice the tone: the image is rendered with perfect competence and no emotion, which is precisely the point. Then read her method: she works from photographs she collects — including screenshots, catalogues, and her own camera roll — and the paintings' deadpan surface is a critique of how images carry feeling (or fail to). Her titles are often as flat as the paintings.",
        "Issy Wood — the deadpan object paintings and the screenshot method",
        ["Contemporary", "British", "Painting"],
    ),
    "pain-caroline-walker-153": _entry(
        "Walker paints women in domestic and workplace interiors — hotel maids, cleaners, office workers, women at home — with a cool, observational realism. She has said she is interested in 'the gap between the way women are seen and the way they see themselves,' and her paintings are built from photographs of staged scenes.",
        "Look at a Walker painting of a woman at work (a cleaner in a hotel corridor, a nurse in a clinic) and notice the viewpoint: we are never fully inside the woman's experience, never fully outside it either — she has said she wants the viewer 'to be unsure of their position.' Then read her method: she stages scenes with models and photographs them, then paints from the photos — a working method that keeps the paintings at a deliberate distance from documentary.",
        "Caroline Walker — the women-at-work paintings and the staged-photo method",
        ["Contemporary", "British", "Figurative"],
    ),
    "pain-fra-angelico-154": _entry(
        "Fra Angelico — 'the angelic brother' — was a Dominican friar who painted the frescoes of San Marco in Florence, where his own order lived, in the 1430s–40s. Every cell of the monastery's upper floor has one fresco, designed to be prayed in front of; he was beatified by the Catholic Church in 1982, making him possibly the only painter ever formally declared blessed.",
        "Look at the San Marco 'Annunciation' — painted in the corridor where the friars walked — and notice the architecture: the loggia frames the moment so that the viewer stands where the friar stood, in the space the angel and Mary share. Then compare it with his 'Annunciation' in Cortona, made for a different audience: same subject, different geometry, because Fra Angelico painted for the room, not the museum.",
        "Fra Angelico — the San Marco 'Annunciation' (c. 1440)",
        ["Renaissance", "Italian", "Fresco"],
    ),
    "pain-hieronymus-bosch-155": _entry(
        "Bosch painted the strangest images of the Renaissance — the Garden of Earthly Delights (c. 1500) teems with hybrid creatures, giant fruit, and rituals that scholars still argue about, 500 years later. He worked in 's-Hertogenbosch, never left, and his exact theological intent remains the most contested question in the history of art.",
        "Look at the Garden of Earthly Delights' central panel and try to find a single 'normal' human action — the nudity, the giant strawberries, the glass spheres, the owls — and then read the main interpretations: it is either a warning against lust, a celebration of earthly paradise before the Fall, or an alchemical allegory. Then look at the right panel, Hell, where a pair of ears holds a knife: nobody has ever fully explained that, either.",
        "Hieronymus Bosch — 'The Garden of Earthly Delights' (c. 1500), central panel",
        ["Northern Renaissance", "Dutch", "Fantasy"],
    ),
    "pain-albrecht-dürer-156": _entry(
        "Dürer was the first artist to paint himself as Christ, the first to make self-portraits as a deliberate series, and the printmaker whose woodcuts and engravings — the Rhinoceros, Melencolia I — defined what 'a master print' means. He was also the first German artist to be famous in Italy, where he traveled twice to study the new Renaissance ideas.",
        "Look at 'Melencolia I' (1514) and read it as a self-portrait in disguise: the brooding winged figure sits among the tools of geometry and craftsmanship, and the magic square in the corner is Dürer's own design, with his date, 1514, in its bottom row. Then look at the 1500 self-portrait — the Christ-like frontal pose, the fur collar — and read the audacity: a 28-year-old comparing himself to the savior was unprecedented.",
        "Albrecht Dürer — 'Melencolia I' (1514) and the 1500 self-portrait",
        ["Northern Renaissance", "German", "Printmaking"],
    ),
    "pain-el-greco-157": _entry(
        "El Greco — 'the Greek,' born Domenikos Theotokopoulos in Crete — trained as an icon painter, moved through Venice and Rome, and settled in Toledo, Spain, where he painted figures stretched into flame and color detached from realism. His work was forgotten for 300 years and then rediscovered as proto-modern by painters who saw themselves in him.",
        "Look at 'View of Toledo' (c. 1599) and notice what's impossible about it: the sky is storm-laden, the city is rearranged to fit the composition, and the green hills glow — a landscape painted from imagination and memory, centuries before that was allowed. Then look at the 'Burial of the Count of Orgaz' (1586) and find the split: heaven above, earth below, and El Greco's own son standing in the crowd as a witness.",
        "El Greco — 'View of Toledo' (c. 1599) and the 'Burial of the Count of Orgaz'",
        ["Mannerism", "Spanish", "Religious"],
    ),
    "pain-judith-leyster-158": _entry(
        "Leyster was the first woman admitted to the painters' guild of Haarlem — in 1633, at 24 — and one of the few women of the Dutch Golden Age with a documented independent career. Her work was then misattributed to Frans Hals for nearly two centuries, until her monogram — a star, a pun on her surname meaning 'lodestar' — was discovered on the canvas.",
        "Look at 'The Proposition' (1631) and read the scene carefully: a woman sewing, a man leaning over her offering coins — the painting is a warning about solicitation, and the candle and the woman's focused face tell you the moral. Then read the attribution story: 'The Carousing Couple' was hung as a Frans Hals until the 1890s, when Leyster's star monogram was found. She stopped painting around 1636, after marrying — and the silence of her later years is the most commented-on fact of her biography.",
        "Judith Leyster — 'The Proposition' (1631) and the Hals misattribution",
        ["Dutch Golden Age", "Baroque", "Portraiture"],
    ),
    "pain-angelica-kauffman-159": _entry(
        "Kauffman was one of only two female founding members of London's Royal Academy in 1768 — the other being Mary Moser — and one of the most celebrated painters in Europe in her lifetime, a favorite of Goethe. She painted history and myth in the neoclassical manner, and her fame was so complete that her name appeared in the memoirs of her age's greatest men.",
        "Look at 'Zeuxis Choosing Models for His Painting of Helen of Troy' (c. 1778) and notice the feminist argument hidden in the neoclassical surface: Kauffman, a woman, paints the ancient story of a male artist choosing a model — and her composition makes the women the subject, not the selection. Then read the range of her fame: she was a founder of the Royal Academy, her work hung in Catherine the Great's collection, and her letters show a career managed as carefully as any man's.",
        "Angelica Kauffman — 'Zeuxis Choosing Models' (c. 1778)",
        ["Neoclassicism", "Swiss-Austrian", "History Painting"],
    ),
    "pain-jmw-turner-160": _entry(
        "Turner — 'the painter of light' — made the sky itself the subject of painting, dissolving ships, storms, and trains into atmosphere decades before Impressionism. He was so famous in his lifetime that he could exhibit paintings with no titles (the catalog simply said 'the picture of a ship in a storm'), and he left 300 paintings and 19,000 watercolors to the nation.",
        "Look at 'Rain, Steam and Speed' (1844) and find the hare — a tiny animal running across the railway tracks, Turner's symbol of the old world fleeing the new train. Then read the audacity of 'The Fighting Temeraire' (1839): the heroic warship of Trafalgar is being towed to the scrapyard by a dirty little tug, and Turner, who painted it from memory, knew exactly what the image said. The Temeraire was voted Britain's favorite painting in 2005.",
        "J.M.W. Turner — 'Rain, Steam and Speed' (1844) and 'The Fighting Temeraire'",
        ["Romanticism", "British", "Landscape"],
    ),
    "pain-eugène-delacroix-161": _entry(
        "Delacroix's Liberty Leading the People (1830) — Liberty as a bare-breasted woman with a rifle and a flag, stepping over the barricades — has been the image of revolution for two centuries, reproduced in everything from Coldplay albums to French stamps. It was painted in three months after the July 1830 uprising, and the Louvre displayed it behind glass for decades because of repeated vandalism.",
        "Look at Liberty Leading the People and notice who's in the crowd: the worker with the saber, the student with the pistol, the child with the pistols — and the top hat of the bourgeois on the left, who is there to remind you which class won. Then read the painting's politics: Delacroix painted it as propaganda for the July Monarchy, and the regime hung it, then hid it, as its own politics changed. The image outlived its politics — that's the sign of a great painting.",
        "Eugène Delacroix — 'Liberty Leading the People' (1830)",
        ["Romanticism", "French", "History"],
    ),
    "pain-jean-françois-millet-162": _entry(
        "Millet made the peasant the hero of French painting: The Gleaners (1857) shows three women bending to collect leftover grain in the fields, a subject his critics called dangerous and socialist. He was part of the Barbizon school, and his quiet scenes of rural labor influenced van Gogh, who copied him constantly.",
        "Look at The Gleaners and notice the horizon line: the three women are in the foreground, but the abundant harvest behind them — the haystacks, the loaded cart — tells you the gleaners work at the very bottom of the economy. Then read the painting's reception: it was exhibited in 1857 to reviews accusing Millet of 'sowing the seeds of revolution,' which tells you how political a painting of field work could be. Van Gogh's copies of Millet's drawings were made while Van Gogh was in an asylum — the influence was personal, not just stylistic.",
        "Jean-François Millet — 'The Gleaners' (1857) and van Gogh's copies",
        ["Realism", "French", "Rural"],
    ),
    "pain-rosa-bonheur-163": _entry(
        "Bonheur was the most famous woman painter of the 19th century — The Horse Fair (1855), her monumental study of horses at the Paris horse market, toured Europe and America, and Queen Victoria received her. To study animals at slaughterhouses and markets, she got a police permit to wear men's clothing, and she was the first woman awarded the French Legion of Honor.",
        "Look at The Horse Fair and notice the anatomy: Bonheur studied horses at the Paris horse market for 18 months, making endless sketches, and the painting's power comes from that research — the rearing, shoving, muscular realism has no sentimental gloss. Then read her working methods: she kept a menagerie of animals at home, painted from dissections, and treated her career with an ambition that shocked and thrilled her era. The Horse Fair is the size of a wall, and it is meant to be seen that way.",
        "Rosa Bonheur — 'The Horse Fair' (1855)",
        ["Realism", "French", "Animal Painting"],
    ),
    "pain-edgar-degas-164": _entry(
        "Degas painted dancers — some 1,500 works of ballerinas — with the eye of a scientist and the angles of a photographer, though he always denied being an Impressionist. He also made the only sculpture exhibited in his lifetime, the Little Dancer of Fourteen Years, which shocked Paris with its real tutu and real hair.",
        "Look at 'The Dance Class' (1874) and notice the composition's strangeness: the dancers are off-balance, cut off by the frame, and arranged diagonally — Degas was obsessed with the accidental-looking moment. Then look at the Little Dancer (1881) and read its reception: the critics called her a 'rat' and a 'flower of the gutter,' and the sculpture was hidden away for 40 years. The same eye that found beauty in a dancer's exhaustion found it in a teenager's awkwardness.",
        "Edgar Degas — 'The Dance Class' (1874) and 'Little Dancer of Fourteen Years'",
        ["Impressionism", "French", "Dancers"],
    ),
    "pain-georges-seurat-165": _entry(
        "Seurat built A Sunday Afternoon on the Island of La Grande Jatte (1886) from millions of tiny dots of pure color — pointillism — a technique he called divisionism, based on optical theory. It took two years, and when it was exhibited it made him the leader of the avant-garde at 27; he died at 31, leaving the movement he founded to others.",
        "Look at La Grande Jatte from across the room, then walk close: the picture reassembles from dots into solid figures — the dog, the monkey, the woman with the parasol. Then read the theory: Seurat placed complementary colors side by side so the eye, not the paint, would mix them — the same principle behind television pixels. The figures are frozen and posed, and art historians still argue whether the island's Sunday crowd is a satire or a dream.",
        "Georges Seurat — 'A Sunday Afternoon on the Island of La Grande Jatte' (1886)",
        ["Pointillism", "French", "Post-Impressionism"],
    ),
}


def main() -> int:
    data = json.loads(PATH.read_text(encoding="utf-8"))
    by_id = {t["id"]: t for t in data}
    missing = [i for i in FIXES if i not in by_id]
    if missing:
        print(f"ERROR: ids not in file: {missing}")
        return 1

    changed = 0
    for topic in data:
        fix = FIXES.get(topic["id"])
        if fix is None:
            continue
        topic["teaser"] = _trim(fix["teaser"])
        topic["exploreAction"]["instruction"] = _trim(fix["instruction"])
        topic["exploreAction"]["targetName"] = fix["targetName"]
        topic["tags"] = fix["tags"]
        changed += 1

    PATH.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"updated {changed} entries")
    return 0


if __name__ == "__main__":
    sys.exit(main())
