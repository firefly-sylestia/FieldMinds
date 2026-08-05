#!/usr/bin/env python3
"""
Enrich the Discoveries catalog with `byline` fields (the discoverer) so the
Topic Reveal hero card and the Spin shuffle hero can show a
"Discovered by · Alexander Fleming" style pill.

Fills every discovery in `discoveries.json` that has a NAMED discoverer /
inventor (curated id → discoverer map below). Topics without a single named
discoverer (prehistoric fire, the wheel, agriculture, writing, ongoing
programs, team inventions with no lead) are deliberately left without a
byline so no invented credit is shown — the pill simply doesn't render.

Idempotent: safe to re-run; existing byline values are overwritten with the
same deterministic values. Output written with literal UTF-8 + indent=2 to
match the checked-in JSON formatting exactly (real accented characters like
Lemaître / Röntgen / Gödel are preserved).

Run from the repo root:  python3 scripts/enrich_discoveries_bylines.py
"""

import json
import os

TOPICS_DIR = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "assets", "topics")
FILE = os.path.join(TOPICS_DIR, "discoveries.json")

# id → discoverer (single string; multiple discoverers joined with " & "
# or ", " — matching the byline pill's single-line format).
DISCOVERER = {
    # ── Tier-1 marquee discoveries ────────────────────────────────────────
    "discovery-penicillin": "Alexander Fleming",
    "discovery-dna-structure": "James Watson & Francis Crick",
    "discovery-gravitational-waves": "Weiss, Thorne & the LIGO Team",
    "discovery-higgs-boson": "Peter Higgs & the CERN teams",
    "discovery-relativity": "Albert Einstein",
    "discovery-quantum-mechanics": "Heisenberg & Schrödinger",
    "discovery-evolution-natural-selection": "Charles Darwin & Alfred Wallace",
    "discovery-periodic-table": "Dmitri Mendeleev",
    "discovery-photosynthesis": "Joseph Priestley",
    "discovery-radioactivity": "Henri Becquerel",
    "discovery-germ-theory": "Louis Pasteur",
    "discovery-big-bang": "Georges Lemaître",
    "discovery-cmb": "Arno Penzias & Robert Wilson",
    "discovery-plate-tectonics": "Alfred Wegener",
    "discovery-calculus": "Isaac Newton & Gottfried Leibniz",
    "discovery-godel-incompleteness": "Kurt Gödel",
    "discovery-transistor": "Bardeen, Brattain & Shockley",
    "discovery-integrated-circuit": "Jack Kilby",
    "discovery-laser": "Theodore Maiman",
    "discovery-internet": "Vint Cerf & Bob Kahn",
    "discovery-world-wide-web": "Tim Berners-Lee",
    "discovery-crispr": "Doudna & Charpentier",
    "discovery-mri": "Paul Lauterbur",
    "discovery-helicobacter-pylori": "Barry Marshall & Robin Warren",
    "discovery-printing-press": "Johannes Gutenberg",
    "discovery-lightning-as-electricity": "Benjamin Franklin",
    "discovery-antibiotics-streptomycin": "Albert Schatz & Selman Waksman",
    "discovery-antiseptic-surgery": "Joseph Lister",
    "discovery-mendel-genetics": "Gregor Mendel",
    "discovery-oxygen-lavoisier": "Antoine Lavoisier",
    "discovery-viviani-theorem": "Vincenzo Viviani",
    "discovery-fibonacci-sequence": "Leonardo of Pisa",
    "discovery-protons-rutherford": "Ernest Rutherford",
    # ── Long-tail discoveries ─────────────────────────────────────────────
    "disc-the-cosmic-microwave-background-163": "Arno Penzias & Robert Wilson",
    "disc-exoplanets-1995-164": "Michel Mayor & Didier Queloz",
    "disc-evolution-by-natural-selection-121": "Charles Darwin & Alfred Wallace",
    "disc-the-periodic-table-1869-122": "Dmitri Mendeleev",
    "disc-radiocarbon-dating-1949-123": "Willard Libby",
    "disc-the-polio-vaccine-1955-124": "Jonas Salk",
    "disc-the-first-organ-transplant-125": "Joseph Murray",
    "disc-ct-scanning-1971-126": "Godfrey Hounsfield",
    "disc-stem-cells-1998-127": "James Thomson",
    "disc-induced-pluripotent-stem-cells-128": "Shinya Yamanaka",
    "disc-the-first-exoplanet-1992-129": "Aleksander Wolszczan & Dale Frail",
    "disc-the-accelerating-universe-1998-130": "Perlmutter, Schmidt & Riess",
    "disc-the-god-particle-2012-131": "Peter Higgs & the CERN teams",
    "disc-graphene-2004-132": "Andre Geim & Konstantin Novoselov",
    "disc-the-first-black-hole-133": "EHT Collaboration",
    "disc-chaos-theory-1960s-134": "Edward Lorenz",
    "disc-the-world-wide-web-135": "Tim Berners-Lee",
    "disc-public-key-cryptography-1976-136": "Diffie & Hellman",
    "disc-deep-learning-neural-networks-137": "Krizhevsky, Sutskever & Hinton",
    "disc-self-driving-cars-2010s-138": "Sebastian Thrun",
    "disc-the-structure-of-the-139": "Ramakrishnan, Steitz & Yonath",
    "disc-apoptosis-1972-140": "Kerr, Wyllie & Currie",
    "disc-the-first-clone-dolly-141": "Ian Wilmut",
    "disc-the-microbiome-2010s-142": "Joshua Lederberg",
    "disc-the-ozone-layer-recovery-143": "Molina & Rowland",
    "disc-the-keeling-curve-1958-144": "Charles David Keeling",
    "disc-the-extinction-of-the-145": "Luis & Walter Alvarez",
    "disc-homo-naledi-2013-146": "Lee Berger",
    "disc-the-printing-press-1440-149": "Johannes Gutenberg",
    "disc-electricity-1800-150": "Alessandro Volta",
    "disc-the-light-bulb-1879-151": "Thomas Edison",
    "disc-television-1927-152": "Philo Farnsworth",
    "disc-the-microprocessor-1971-153": "Hoff, Faggin & Mazor",
    "disc-the-search-engine-1996-154": "Larry Page & Sergey Brin",
    "disc-the-structure-of-dna-155": "James Watson & Francis Crick",
    "disc-anesthesia-1846-156": "William Morton",
    "disc-pasteurization-1864-157": "Louis Pasteur",
    "disc-x-rays-1895-158": "Wilhelm Röntgen",
    "disc-the-electron-1897-159": "J.J. Thomson",
    "disc-relativity-1905-160": "Albert Einstein",
    "disc-the-proton-1919-161": "Ernest Rutherford",
    "disc-nuclear-fission-1938-162": "Otto Hahn & Lise Meitner",
    "disc-the-laser-1960-163": "Theodore Maiman",
    "disc-pulsars-1967-164": "Jocelyn Bell Burnell",
    "disc-the-ozone-hole-1985-165": "Joseph Farman",
    "disc-the-higgs-boson-2012-166": "Peter Higgs & CERN's ATLAS/CMS teams",
    "disc-the-big-bang-theory-167": "Georges Lemaître",
    "disc-mendelian-genetics-1866-168": "Gregor Mendel",
    "disc-the-electron-microscope-1931-169": "Ernst Ruska",
    "disc-the-first-antibiotic-1932-170": "Gerhard Domagk",
    "disc-the-structure-of-insulin-171": "Frederick Sanger",
    "disc-mri-imaging-1973-172": "Paul Lauterbur",
    "disc-the-human-genome-project-173": "Human Genome Project Consortium",
    "disc-rna-interference-1998-174": "Andrew Fire & Craig Mello",
    "disc-mrna-vaccines-2020-175": "Katalin Karikó & Drew Weissman",
    "disc-dark-energy-1998-176": "Perlmutter, Schmidt & Riess",
    "disc-neutrino-oscillations-1998-177": "Takaaki Kajita & Arthur McDonald",
    "disc-topological-insulators-2007-178": "Charles Kane & Eugene Mele",
    "disc-fullerenes-1985-179": "Kroto, Smalley & Curl",
    "disc-fractals-1975-181": "Benoit Mandelbrot",
    "disc-the-internet-protocol-1974-182": "Vint Cerf & Bob Kahn",
    "disc-alphafold-protein-folding-2020-184": "Demis Hassabis & John Jumper",
    "disc-reusable-rockets-2015-185": "Elon Musk & SpaceX",
    "disc-telomerase-1984-186": "Blackburn & Greider",
    "disc-the-cell-cycle-2001-187": "Hartwell, Hunt & Nurse",
    "disc-optogenetics-2005-188": "Karl Deisseroth",
    "disc-the-first-gene-therapy-189": "W. French Anderson",
    "disc-climate-change-science-1988-190": "James Hansen",
    "disc-lucy-the-australopithecus-1974-192": "Donald Johanson",
    "disc-homo-floresiensis-2003-193": "Peter Brown & Michael Morwood",
    "disc-the-steam-engine-1712-196": "Thomas Newcomen",
    "disc-the-telephone-1876-197": "Alexander Graham Bell",
    "disc-radio-1895-198": "Guglielmo Marconi",
    "disc-the-computer-1940s-199": "Eckert & Mauchly",
    "disc-the-internet-1983-200": "Vint Cerf & Bob Kahn",
    "disc-vaccination-1796-202": "Edward Jenner",
    "disc-germ-theory-of-disease-203": "Louis Pasteur",
    "disc-quantum-theory-1900-204": "Max Planck",
    "disc-the-atomic-nucleus-1911-205": "Ernest Rutherford",
    "disc-the-neutron-1932-206": "James Chadwick",
    "disc-the-transistor-1947-207": "Bardeen, Brattain & Shockley",
}


def load(name):
    with open(os.path.join(TOPICS_DIR, name), encoding="utf-8") as f:
        return json.load(f)


def save(name, data):
    with open(os.path.join(TOPICS_DIR, name), "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
        f.write("\n")


def insert_byline(topic, byline):
    """Key-order preservation: insert `byline` right before `exploreAction`."""
    rebuilt = {}
    for k, v in topic.items():
        if k == "exploreAction" and "byline" not in rebuilt:
            rebuilt["byline"] = byline
        rebuilt[k] = v
    if "byline" not in rebuilt:
        rebuilt["byline"] = byline
    topic.clear()
    topic.update(rebuilt)


def main():
    topics = load("discoveries.json")
    added, skipped, unknown = 0, 0, []
    for topic in topics:
        tid = topic["id"]
        if tid in DISCOVERER:
            insert_byline(topic, DISCOVERER[tid])
            added += 1
        else:
            skipped += 1
            unknown.append(tid)
    save("discoveries.json", topics)
    print(f"Added byline to {added} of {len(topics)} discoveries.")
    print(f"Skipped {skipped} (no single named discoverer):")
    for tid in unknown:
        print(f"  - {tid}")


if __name__ == "__main__":
    main()
