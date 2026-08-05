#!/usr/bin/env python3
"""
Batch 1 — real descriptions for the first 15 artworks in artworks.json.

Replaces the generic/fabricated teaser + exploreAction.instruction text with
factual, real descriptions, corrects obviously-wrong tags, and replaces the
fabricated "Shark (1988) by Jean-Michel Basquiat" entry (no such work exists)
with his real record-breaking *Untitled (1982)* skull painting.

Run: python3 scripts/batch_artworks_1.py
Keyed on unique topic id so entries sharing the same fake teaser text are all
fixed (several artworks reused the exact same filler copy).
"""
import json
import sys
from pathlib import Path

PATH = Path("app/src/main/assets/topics/artworks.json")

# (name, teaser, [tags], instruction) — keyed by unique topic id.
# Tags only present when they need correcting.
FIXES = {
    "artw-the-physical-impossibility-of-134": dict(
        name="The Physical Impossibility of Death (1991) by Damien Hirst",
        teaser=(
            "A 14-foot tiger shark suspended in a tank of formaldehyde — bought by advertising "
            "mogul Charles Saatchi for £50,000 while Hirst was still a student. It became the emblem "
            "of the Young British Artists movement and won Hirst the Turner Prize in 1995."
        ),
        instruction=(
            "Circle the tank slowly and watch how the shark's silhouette changes against the blue — "
            "the formaldehyde keeps it frozen mid-strike, both menacing and serene. Think about the "
            "title: death made physically impossible to imagine, yet held right in front of you."
        ),
    ),
    "artw-flag-1954-by-jasper-51": dict(
        name="Flag (1954) by Jasper Johns",
        teaser=(
            "Johns painted this after dreaming of the American flag — a familiar symbol rendered in "
            "encaustic (hot wax pigment) over newspaper scraps. It quietly ended Abstract "
            "Expressionism's reign by asking whether a flag is a thing or a painting."
        ),
        tags=["Encaustic", "Pop Art"],
        instruction=(
            "Look at the surface, not the symbol — the waxy brushstrokes and the newsprint bleeding "
            "through make the flag feel handmade. Ask yourself: is it a flag, or is it paint arranged "
            "to look like one?"
        ),
    ),
    "artw-love-1970-by-robert-52": dict(
        name="LOVE (1970) by Robert Indiana",
        teaser=(
            "Four stacked letters — L-O-V-E — with the O tilted, born as a 1964 MoMA Christmas card "
            "and later cast in steel and aluminum across the world. It turned a two-letter Americanism "
            "into one of Pop Art's most reproduced images."
        ),
        tags=["Pop Art", "Sculpture"],
        instruction=(
            "Notice the O is tilted, not stacked — that tiny rotation is what gives the sculpture its "
            "motion. Count how many LOVE sculptures you can name in different cities; each one is a "
            "different color scheme."
        ),
    ),
    "artw-water-lilies-1919-by-53": dict(
        name="Water Lilies (1919) by Claude Monet",
        teaser=(
            "Monet painted his Giverny pond's water lilies obsessively for the last 30 years of his "
            "life, chasing the same light across 250 canvases. This late panel, painted as cataracts "
            "clouded his eyes, dissolves the pond into pure color and reflection."
        ),
        tags=["Impressionism", "Oil Painting"],
        instruction=(
            "Step back until the lilies stop being flowers and become smears of light — that's the "
            "distance Monet wanted. Then look at the sky reflected in the water; the real subject is "
            "never the lilies, but the light on the surface."
        ),
    ),
    "artw-the-last-supper-1498-54": dict(
        name="The Last Supper (1498) by Leonardo da Vinci",
        teaser=(
            "Painted directly onto a refectory wall in Milan with an experimental oil-tempera mix "
            "instead of true fresco — a gamble that began crumbling within decades. It captures the "
            "exact instant Christ says one of the twelve will betray him."
        ),
        tags=["Renaissance", "Mural"],
        instruction=(
            "Follow the perspective lines — they all converge on Christ's head. Then look at the "
            "apostles in their groups of three: shock, denial, betrayal. Find Judas, the only one "
            "whose face is in shadow."
        ),
    ),
    "artw-the-creation-of-adam-55": dict(
        name="The Creation of Adam (1512) by Michelangelo",
        teaser=(
            "The centerpiece of the Sistine Chapel ceiling — God's outstretched finger reaching toward "
            "Adam's, the spark of life suspended in the gap between them. Painted over four years on "
            "his back, it remains the most famous image of divine creation in Western art."
        ),
        tags=["Renaissance", "Fresco"],
        instruction=(
            "Measure the space between the two fingers — the gap IS the painting. Then notice God's "
            "arm is wrapped around a shape that anatomists have argued resembles a cross-section of "
            "the human brain."
        ),
    ),
    "artw-the-school-of-athens-56": dict(
        name="The School of Athens (1511) by Raphael",
        teaser=(
            "Raphael's fresco gathers the great philosophers of antiquity under one vaulted hall in "
            "the Vatican — Plato and Aristotle at center, pointing up and down respectively. It's a "
            "love letter to the wisdom of the ancient world."
        ),
        tags=["Renaissance", "Fresco"],
        instruction=(
            "Find Plato on the left, pointing to the heavens, and Aristotle beside him, palm facing "
            "the earth — the two poles of Western thought. Raphael even painted himself in at the far "
            "right, listening in."
        ),
    ),
    "artw-girl-with-a-pearl-57": dict(
        name="Girl with a Pearl Earring (1665) by Johannes Vermeer",
        teaser=(
            "Often called the 'Mona Lisa of the North' — a nameless girl in a turban turning toward "
            "the viewer, set against pure black. The pearl is barely a smudge of paint, yet it reads "
            "as the most luminous object in the room."
        ),
        tags=["Dutch Golden Age", "Oil Painting"],
        instruction=(
            "Move your eye from the earring to her lips — both are made of almost nothing, a few "
            "strokes that the mind completes. Notice how the darkness makes her skin glow; the pearl "
            "is a lie your brain believes."
        ),
    ),
    "artw-the-swing-1767-by-58": dict(
        name="The Swing (1767) by Jean-Honoré Fragonard",
        teaser=(
            "A Rococo flirtation staged in a garden: a young woman swings high while a hidden admirer "
            "in the bushes watches from below — a bishop pushes her. Commissioned by a baron who "
            "wanted a picture of his mistress, it's the epitome of 18th-century playful indulgence."
        ),
        tags=["Rococo", "Oil Painting"],
        instruction=(
            "Find the suitor hidden in the foliage on the left and the shoe flying off her foot toward "
            "him — a wink about what's happening just out of frame. Note the sculpture of Cupid "
            "hushing: the whole garden conspires to keep the secret."
        ),
    ),
    "artw-the-third-of-may-59": dict(
        name="The Third of May 1808 (1814) by Francisco Goya",
        teaser=(
            "Goya's harrowing record of Napoleon's firing squad executing Madrid's rebels the night "
            "after the uprising. The white-shirted man with arms flung wide stands under a lantern "
            "like a crucifixion — the first modern anti-war painting."
        ),
        tags=["Romanticism", "Oil Painting"],
        instruction=(
            "Compare the faceless, machine-like firing squad with the glowing victim — the lantern "
            "turns a mass execution into a stage. Notice there is no heroism here, only terror; Goya "
            "painted it six years later, still raw."
        ),
    ),
    "artw-liberty-leading-the-people-60": dict(
        name="Liberty Leading the People (1830) by Eugène Delacroix",
        teaser=(
            "Delacroix's allegory of the July Revolution of 1830 — Marianne, bare-breasted and "
            "hoisting the tricolor, leads Parisians over the barricades. Part history painting, part "
            "propaganda, it became the visual shorthand for revolution itself."
        ),
        tags=["Romanticism", "Oil Painting"],
        instruction=(
            "Look at Liberty's face — fierce, not pretty — and the boy with pistols beside her, "
            "modeled on a real street urchin. Then check the skyline: Notre-Dame peeks through the "
            "smoke, grounding the allegory in real Paris."
        ),
    ),
    "artw-the-fighting-temeraire-1839-61": dict(
        name="The Fighting Temeraire (1839) by J.M.W. Turner",
        teaser=(
            "The HMS Temeraire — hero of Trafalgar — is towed by a black steam tug to its final berth "
            "to be broken up. Turner's elegy for the age of sail was voted Britain's greatest painting "
            "in 2005, and he refused to sell it in his lifetime."
        ),
        tags=["Romanticism", "Oil Painting"],
        instruction=(
            "Compare the ghost-pale Temeraire with the small, dirty tug — the past being dragged into "
            "the future. Watch the sun setting behind them; the entire painting is a slow goodbye, and "
            "the real subject is time itself."
        ),
    ),
    "artw-the-luncheon-on-the-62": dict(
        name="The Luncheon on the Grass (1863) by Édouard Manet",
        teaser=(
            "A nude woman picnicking with two fully dressed men scandalized Paris when the Salon "
            "rejected it and Emperor Napoleon III set up the Salon des Refusés to show it anyway. "
            "Manet's flattened light and frank gaze launched modern painting."
        ),
        tags=["Realism", "Impressionism"],
        instruction=(
            "The woman looks straight at YOU, not at her companions — breaking every rule of classical "
            "decorum. Then look at the second woman wading in the background, oddly out of scale; the "
            "painting is a collage of borrowed Renaissance motifs."
        ),
    ),
    "artw-the-dance-class-1874-63": dict(
        name="The Dance Class (1874) by Edgar Degas",
        teaser=(
            "Degas found his true subject in the Paris Opera ballet — not the performance, but the "
            "rehearsal. Dancers stretch, scratch, and adjust their shoes under the master's eye, "
            "caught in unguarded, off-balance moments that feel almost candid."
        ),
        tags=["Impressionism", "Oil Painting"],
        instruction=(
            "Notice how no one is posed — one girl scratches her back, another adjusts her dress. "
            "Follow the diagonal of the floorboards to the door at the back; Degas built the whole "
            "composition around off-center, snapshot-like framing."
        ),
    ),
}

# The fabricated Basquiat "Shark (1988)" → real Untitled (1982) skull painting.
REPLACE_ENTRY = {
    "id": "artw-untitled-1982-by-jean-michel-135",
    "name": "Untitled (1982) by Jean-Michel Basquiat",
    "teaser": (
        "A skull rendered in oil stick, acrylic and spray paint — the painting that sold for $110.5 "
        "million at Sotheby's in 2017, then the most expensive American artwork ever auctioned. The "
        "crown floating above the skull is Basquiat's signature."
    ),
    "byline": "Jean-Michel Basquiat",
    "exploreAction": {
        "verb": "Look at",
        "targetName": "Untitled (1982) by Jean-Michel Basquiat",
        "durationMinutes": 7,
        "instruction": (
            "Find the crown — Basquiat's recurring mark, floating over the skull like a halo and a "
            "claim to royalty at once. Then trace the raw, scribbled lines: the painting was built "
            "fast, in oil stick and spray, like a page torn from a diary."
        ),
    },
    "tags": ["Neo-Expressionism", "Oil Painting"],
}


def main() -> int:
    data = json.loads(PATH.read_text(encoding="utf-8"))
    changed = 0
    for topic in data:
        if topic["id"] == "artw-shark-1988-by-jean-michel-135":
            for k, v in REPLACE_ENTRY.items():
                topic[k] = v
            changed += 1
            continue
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
