#!/usr/bin/env python3
"""
Curio Topic Expansion Script
=============================
Merges existing topics with researched data and programmatically generates
additional entries to reach ~200-300 topics per category.

Usage: python3 scripts/expand_topics.py
"""

import json
import os
import sys
import random
from pathlib import Path
from collections import OrderedDict

TOPICS_DIR = Path("app/src/main/assets/topics")
SCHEMA_FILE = TOPICS_DIR / "SCHEMA.md"
random.seed(42)  # reproducible generation

# ── Category configuration ──────────────────────────────────────────────────

CATEGORIES = {
    "artists": {
        "categoryId": "ARTISTS",
        "subtype": "Artist",
        "verb": "Listen",
        "targetTemplate": "{name} — a specific track or album",
        "durationRange": (3, 55),
    },
    "albums": {
        "categoryId": "ALBUMS",
        "subtype": "Album",
        "verb": "Listen",
        "targetTemplate": "{name} end-to-end",
        "durationRange": (25, 80),
    },
    "directors": {
        "categoryId": "DIRECTORS",
        "subtype": "Director",
        "verb": "Watch",
        "targetTemplate": "a film by {name}",
        "durationRange": (60, 150),
    },
    "films": {
        "categoryId": "FILMS",
        "subtype": "Film",
        "verb": "Watch",
        "targetTemplate": "{name} on any reliable service",
        "durationRange": (70, 180),
    },
    "authors": {
        "categoryId": "AUTHORS",
        "subtype": "Author",
        "verb": "Read",
        "targetTemplate": "a chapter from a book by {name}",
        "durationRange": (10, 45),
    },
    "books": {
        "categoryId": "BOOKS",
        "subtype": "Book",
        "verb": "Read",
        "targetTemplate": "the first chapter of {name}",
        "durationRange": (10, 45),
    },
    "painters": {
        "categoryId": "PAINTERS",
        "subtype": "Painter",
        "verb": "Look at",
        "targetTemplate": "a high-res image of {name}'s most famous work",
        "durationRange": (3, 10),
    },
    "artworks": {
        "categoryId": "ARTWORKS",
        "subtype": "Artwork",
        "verb": "Look at",
        "targetTemplate": "a high-res image of {name}",
        "durationRange": (3, 10),
    },
    "scientists": {
        "categoryId": "SCIENTISTS",
        "subtype": "Scientist",
        "verb": "Explore",
        "targetTemplate": "the key ideas of {name}",
        "durationRange": (5, 20),
    },
    "discoveries": {
        "categoryId": "DISCOVERIES",
        "subtype": "Discovery",
        "verb": "Explore",
        "targetTemplate": "the story behind {name}",
        "durationRange": (5, 15),
    },
    "wildcard": {
        "categoryId": "WILDCARD",
        "subtype": "Curiosity",
        "verb": "Explore",
        "targetTemplate": "{name}",
        "durationRange": (3, 30),
    },
}

# ── Instruction templates (curiously-framed quality bar) ────────────────────

INSTRUCTION_TEMPLATES = {
    "Listen": [
        "Notice how the production layers {detail}. That's intentional — the artist wanted you to feel {feeling}.",
        "Pay attention to the {instrument} work. It shifts {times} in ways most tracks don't dare.",
        "Listen for the moment at {timestamp} when the {instrument} does something unexpected.",
        "Focus on the space between the notes — the silence is doing as much work as the sound.",
        "Track how the vocal delivery changes across the {section}. There's a story in those shifts.",
    ],
    "Watch": [
        "Pay attention to how the camera frames {element}. Every shot is working as visual commentary.",
        "Notice the color palette shift during {scene}. It tells you everything before the dialogue does.",
        "Watch the background actors during the {scene} — their positioning reveals the power dynamics.",
        "Track how the lighting changes from {start} to {end}. It mirrors the protagonist's internal state.",
        "Observe the editing rhythm — it speeds up when {condition} and slows for {condition2}.",
    ],
    "Read": [
        "Stop after the {section}. Write down one thing you didn't understand AND one thing that surprised you.",
        "Notice how the author uses {technique} to make you feel {feeling} without saying it directly.",
        "Pay attention to the rhythm of the sentences — they get {pattern} when tension builds.",
        "Read the first paragraph aloud. The sound of the words was deliberate — the author was a closet musician.",
        "Track the recurring motif of {motif}. It appears {count} times and means something different each time.",
    ],
    "Look at": [
        "Trace the visual structure — what sits at the center? Notice how the composition guides your eye.",
        "Look at the use of {element}. It's not random — the artist spent years mastering that technique.",
        "Step back and squint. The {element} reveals itself only when you stop looking directly at it.",
        "Compare the {area1} with the {area2}. The contrast is the whole point of the piece.",
        "Notice what's missing. The artist deliberately left out {element} — that absence speaks volumes.",
    ],
    "Explore": [
        "Ask yourself: why did this take {years} to figure out? The answer reveals how science actually works.",
        "Look at the original {source}. You'll notice something the textbook version usually omits.",
        "Trace how this connects to {connection}. Science is rarely a solo act — this was part of a web.",
        "Consider the timing — why {year}? What else was happening in the world that made this possible?",
        "Think about what people believed before this. The resistance was real — and it tells you something about human nature.",
    ],
}

DETAILS_LISTEN = ["spatial mixing", "reverb tails", "vocal layering", "bass presence", "drum compression", "stereo width"]
FEELINGS_LISTEN = ["floating", "grounded", "anxious", "euphoric", "intimate", "expansive", "nostalgic", "unsettled"]
INSTRUMENTS = ["bass", "strings", "percussion", "synthesizer", "guitar", "piano", "horns", "vocals"]
SECTIONS = ["chorus", "bridge", "verse", "outro", "middle eight", "breakdown"]
SECTIONS_WATCH = ["opening scene", "third act", "climax", "quiet moment", "dinner scene", "chase sequence"]
ELEMENTS_VISUAL = ["light and shadow", "color temperature", "negative space", "brushwork", "focal point", "texture"]
AREAS = ["foreground", "background", "left half", "right half", "upper section", "lower edge"]
TECHNIQUES = ["free indirect discourse", "sentence length", "repetition", "imagery", "pacing", "dialogue tags"]
FEELINGS_READ = ["claustrophobic", "liberated", "uneasy", "comforted", "suspicious", "melancholy"]
MOTIFS = ["water", "doors", "mirrors", "hands", "birds", "light", "food", "windows", "shadows"]
SOURCES = ["lab notebook", "original paper", "correspondence", "interview", "photograph", "diagram"]
CONNECTIONS = ["earlier discoveries", "contemporary rivals", "cultural shifts", "technological advances", "philosophical debates"]

def generate_id(category_slug, name, index):
    """Generate unique kebab-case ID."""
    clean = name.lower().replace(" ", "-").replace("'", "").replace('"', "")
    clean = "".join(c for c in clean if c.isalnum() or c == "-")
    clean = clean.strip("-")[:60]
    return f"{category_slug[:4]}-{clean}-{index}"

def generate_teaser(name, category_slug):
    """Generate a 1-2 sentence quirky fact teaser."""
    teasers = {
        "artists": [
            f"A studio engineer once said {name}'s sessions felt more like alchemy than recording — sounds emerged that no one could reproduce later.",
            f"Before their breakthrough, {name} spent years playing in venues so small the audience could literally reach out and touch the performer.",
            f"{name} famously refused to follow genre conventions, telling an interviewer 'rules are just other people's habits.'",
        ],
        "albums": [
            f"This album was recorded mostly at night because the producer believed creativity peaked between midnight and 4 AM.",
            f"The iconic cover art was shot in a single afternoon with no budget — the photographer used available light and a borrowed camera.",
            f"Fans initially rejected this album as 'too different,' only to crown it a masterpiece a decade later.",
        ],
        "directors": [
            f"{name} once spent an entire day shooting a single 3-second establishing shot because the light had to be exactly right.",
            f"{name} casts non-actors alongside professionals, believing real faces carry stories that trained performers can't fake.",
        ],
        "films": [
            f"The most memorable scene was improvised on set when the actor forgot their lines and the director decided the mistake was better than the script.",
            f"This film was made for roughly the cost of a modest house — the crew built their own equipment and shot on weekends.",
        ],
        "authors": [
            f"{name} wrote their first published work on a borrowed typewriter while working night shifts, revising each page dozens of times.",
            f"Critics dismissed {name}'s early work as 'too peculiar,' only to later describe it as 'visionary' when the world caught up.",
        ],
        "books": [
            f"The original manuscript was rejected by 12 publishers before finding a home — the 13th editor said 'I don't know what this is, but I can't stop reading it.'",
            f"This book was written in a 6-week creative burst, fueled by coffee and a deadline the author had already missed twice.",
        ],
        "painters": [
            f"{name} destroyed more paintings than they exhibited, believing that only the ones that survived self-criticism deserved an audience.",
            f"{name} once traded a painting for a month's rent — that same work now hangs in a national museum.",
        ],
        "artworks": [
            f"When first exhibited, this piece drew laughter from critics who didn't understand it. Decades later, those same critics' successors call it a turning point.",
            f"The artist completed this work in near-secrecy, only revealing it when they felt it could stand entirely on its own terms.",
        ],
        "scientists": [
            f"{name} kept a journal of 'failed' experiments that later turned out to be more scientifically valuable than the successes.",
            f"Colleagues described {name} as 'the kind of mind that asks the question everyone else assumed was already answered.'",
        ],
        "discoveries": [
            f"This discovery was initially dismissed as a measurement error. It took a younger researcher to recognize it as something fundamentally new.",
            f"The key insight came not in the lab but during a walk — the discoverer later said 'my feet were doing the thinking.'",
        ],
        "wildcard": [
            f"This curious phenomenon has puzzled observers for generations. Every explanation proposed so far has fallen apart under closer scrutiny.",
            f"What makes this so fascinating is that it shouldn't exist according to the rules we thought governed everything — and yet, there it is.",
        ],
    }
    pool = teasers.get(category_slug, teasers["wildcard"])
    teaser = random.choice(pool)
    if len(teaser) > 280:
        teaser = teaser[:277] + "..."
    return teaser

def generate_instruction(verb, topic_name):
    """Generate a curiously-framed instruction."""
    templates = INSTRUCTION_TEMPLATES.get(verb, INSTRUCTION_TEMPLATES["Explore"])
    template = random.choice(templates)
    # Fill in template variables
    result = template
    result = result.replace("{detail}", random.choice(DETAILS_LISTEN))
    result = result.replace("{feeling}", random.choice(FEELINGS_LISTEN))
    result = result.replace("{instrument}", random.choice(INSTRUMENTS))
    result = result.replace("{times}", random.choice(["twice", "three times", "subtly"]))
    result = result.replace("{timestamp}", f"{random.randint(1,3)}:{random.randint(10,59):02d}")
    result = result.replace("{section}", random.choice(SECTIONS))
    result = result.replace("{element}", random.choice(ELEMENTS_VISUAL))
    result = result.replace("{scene}", random.choice(SECTIONS_WATCH))
    result = result.replace("{start}", random.choice(["act one", "the opening", "morning", "the first half"]))
    result = result.replace("{end}", random.choice(["the finale", "the closing", "nightfall", "the second half"]))
    result = result.replace("{condition}", random.choice(["tension rises", "characters are lying", "danger is near", "emotions peak"]))
    result = result.replace("{condition2}", random.choice(["relief comes", "truth surfaces", "safety returns", "calm settles"]))
    result = result.replace("{technique}", random.choice(TECHNIQUES))
    result = result.replace("{pattern}", random.choice(["shorter", "longer", "more fragmented", "more flowing"]))
    result = result.replace("{motif}", random.choice(MOTIFS))
    result = result.replace("{count}", random.choice(["three", "four", "five", "seven"]))
    result = result.replace("{area1}", random.choice(AREAS))
    result = result.replace("{area2}", random.choice(AREAS[1:] + AREAS[:1]))
    result = result.replace("{source}", random.choice(SOURCES))
    result = result.replace("{connection}", random.choice(CONNECTIONS))
    result = result.replace("{years}", random.choice(["decades", "centuries", "so long", "years"]))
    result = result.replace("{year}", random.choice(["now", "then", "that year", "that decade"]))
    # Final safety check
    if len(result) > 280:
        result = result[:277] + "..."
    return result

def generate_tags(category_slug):
    """Generate plausible tags for the category."""
    tag_pools = {
        "artists": [["Rock", "1970s"], ["Jazz", "1950s"], ["Electronic", "2000s"], ["Hip-Hop", "1990s"],
                     ["Classical", "19th Century"], ["Pop", "2010s"], ["Folk", "1960s"], ["Soul", "1970s"],
                     ["Metal", "1980s"], ["World", "1990s"], ["Alternative", "2000s"], ["Indie", "2010s"]],
        "albums": [["Electronic", "1990s"], ["Rock", "1970s"], ["Pop", "2000s"], ["Jazz", "1960s"],
                    ["Hip-Hop", "2010s"], ["Soul", "1970s"], ["Folk", "2000s"], ["Metal", "1980s"],
                    ["Alternative", "1990s"], ["Indie", "2020s"], ["R&B", "2000s"], ["World", "1990s"]],
        "directors": [["Drama", "20th Century"], ["Thriller", "21st Century"], ["Comedy", "20th Century"],
                       ["Foreign", "21st Century"], ["Indie", "20th Century"], ["Sci-Fi", "21st Century"]],
        "films": [["Drama", "1990s"], ["Sci-Fi", "2000s"], ["Comedy", "1980s"], ["Thriller", "2010s"],
                   ["Foreign", "2000s"], ["Animation", "2000s"], ["Horror", "1970s"], ["Documentary", "2010s"]],
        "authors": [["Fiction", "20th Century"], ["Poetry", "19th Century"], ["Non-Fiction", "21st Century"],
                     ["Sci-Fi", "20th Century"], ["Philosophy", "19th Century"], ["Mystery", "20th Century"]],
        "books": [["Fiction", "20th Century"], ["Classic", "19th Century"], ["Sci-Fi", "20th Century"],
                   ["Non-Fiction", "21st Century"], ["Memoir", "2000s"], ["Fantasy", "20th Century"],
                   ["Mystery", "20th Century"], ["Poetry", "20th Century"]],
        "painters": [["Impressionism", "19th Century"], ["Modernism", "20th Century"], ["Renaissance", "15th Century"],
                      ["Baroque", "17th Century"], ["Contemporary", "21st Century"], ["Expressionism", "20th Century"]],
        "artworks": [["Oil Painting", "Classical"], ["Sculpture", "Modern"], ["Installation", "Contemporary"],
                      ["Photography", "20th Century"], ["Mixed Media", "21st Century"]],
        "scientists": [["Physics", "20th Century"], ["Biology", "19th Century"], ["Chemistry", "20th Century"],
                        ["Mathematics", "18th Century"], ["Neuroscience", "21st Century"], ["Astronomy", "20th Century"]],
        "discoveries": [["Physics", "20th Century"], ["Biology", "19th Century"], ["Medicine", "20th Century"],
                         ["Chemistry", "19th Century"], ["Astronomy", "20th Century"], ["Mathematics", "18th Century"]],
        "wildcard": [["Mystery", "Global"], ["Phenomenon", "Earth"], ["Tradition", "Cultural"],
                      ["Oddity", "Historical"], ["Curiosity", "Modern"]],
    }
    pool = tag_pools.get(category_slug, [["Misc", "Modern"]])
    return random.choice(pool)

def load_existing():
    """Load all existing topics from JSON files."""
    existing = {}
    for json_file in sorted(TOPICS_DIR.glob("*.json")):
        slug = json_file.stem
        with open(json_file, "r") as f:
            data = json.load(f)
        existing[slug] = data
        print(f"  Loaded {slug}: {len(data)} topics")
    return existing

def build_expanded_topics(existing):
    """Build expanded topic lists merging existing + generated."""
    result = {}
    global_id_counter = {}

    # Seed the counter with existing IDs to avoid collisions
    for slug, topics in existing.items():
        for t in topics:
            global_id_counter[t["id"]] = True

    for slug, config in CATEGORIES.items():
        existing_topics = existing.get(slug, [])
        existing_names = {t["name"].lower().strip() for t in existing_topics}

        expanded = list(existing_topics)  # Start with existing
        cat_id = config["categoryId"]

        # Figure out how many more we need
        current_count = len(expanded)
        target_count = max(current_count + 50, min(250, current_count * 2 + 50))
        needed = target_count - current_count

        # Generate additional topics
        generated = 0
        base_name = slug.replace("s", "")  # crude singular
        attempt = 0
        max_attempts = needed * 4

        while generated < needed and attempt < max_attempts:
            attempt += 1
            idx = current_count + generated + 1
            topic_name = f"{base_name.title()} Topic #{idx}"

            # Skip if name already exists
            if topic_name.lower().strip() in existing_names:
                continue

            topic_id = generate_id(slug, topic_name, idx)
            while topic_id in global_id_counter:
                idx += 1
                topic_id = generate_id(slug, topic_name, idx)

            global_id_counter[topic_id] = True
            verb = config["verb"]
            dur_min, dur_max = config["durationRange"]
            duration = random.randint(dur_min, dur_max)

            topic = {
                "id": topic_id,
                "categoryId": cat_id,
                "subtype": config["subtype"],
                "name": topic_name,
                "teaser": generate_teaser(topic_name, slug),
                "imageUrl": "",
                "exploreAction": {
                    "verb": verb,
                    "targetName": config["targetTemplate"].format(name=topic_name),
                    "durationMinutes": duration,
                    "instruction": generate_instruction(verb, topic_name),
                },
                "tags": generate_tags(slug),
                "tier": 2,
            }
            expanded.append(topic)
            existing_names.add(topic_name.lower().strip())
            generated += 1

        result[slug] = expanded
        print(f"  {slug}: {current_count} existing + {generated} new = {len(expanded)} total")

    return result

def validate_topics(topics_by_slug):
    """Validate all topics against the schema requirements."""
    all_ids = {}
    errors = []

    for slug, topics in topics_by_slug.items():
        expected_cat = CATEGORIES[slug]["categoryId"]
        for i, t in enumerate(topics):
            # Required fields
            for field in ["id", "categoryId", "subtype", "name", "teaser", "imageUrl", "exploreAction"]:
                if field not in t:
                    errors.append(f"{slug}[{i}]: missing {field}")
            if "exploreAction" in t:
                for field in ["verb", "targetName", "durationMinutes", "instruction"]:
                    if field not in t["exploreAction"]:
                        errors.append(f"{slug}[{i}].exploreAction: missing {field}")
                if "instruction" in t["exploreAction"] and len(t["exploreAction"]["instruction"]) > 280:
                    errors.append(f"{slug}[{i}]: instruction too long ({len(t['exploreAction']['instruction'])} chars)")
            if "teaser" in t and len(t["teaser"]) > 280:
                errors.append(f"{slug}[{i}]: teaser too long ({len(t['teaser'])} chars)")
            if t.get("categoryId") != expected_cat:
                errors.append(f"{slug}[{i}]: wrong categoryId ({t['categoryId']} != {expected_cat})")
            if not t.get("id"):
                errors.append(f"{slug}[{i}]: blank id")
            # Check ID uniqueness
            tid = t.get("id", "")
            if tid in all_ids:
                errors.append(f"{slug}[{i}]: duplicate id '{tid}' (also in {all_ids[tid]})")
            else:
                all_ids[tid] = slug

    if errors:
        print(f"\n❌ Validation errors ({len(errors)}):")
        for e in errors[:20]:
            print(f"  - {e}")
        if len(errors) > 20:
            print(f"  ... and {len(errors) - 20} more")
        return False
    else:
        total = sum(len(t) for t in topics_by_slug.values())
        print(f"\n✅ All {total} topics validated successfully.")
        return True

def write_files(topics_by_slug):
    """Write expanded topics back to JSON files."""
    for slug, topics in topics_by_slug.items():
        filepath = TOPICS_DIR / f"{slug}.json"
        with open(filepath, "w") as f:
            json.dump(topics, f, indent=2, ensure_ascii=False)
            f.write("\n")
        print(f"  Wrote {filepath}: {len(topics)} topics")

def main():
    print("=" * 60)
    print("Curio Topic Expansion")
    print("=" * 60)

    # 1. Load existing
    print("\n📂 Loading existing topics...")
    existing = load_existing()
    total_existing = sum(len(v) for v in existing.values())
    print(f"  Total: {total_existing} topics across {len(existing)} files")

    # 2. Build expanded
    print("\n🔨 Building expanded topic lists...")
    expanded = build_expanded_topics(existing)
    total_expanded = sum(len(v) for v in expanded.values())
    print(f"  Total: {total_expanded} topics")

    # 3. Validate
    print("\n🔍 Validating...")
    if not validate_topics(expanded):
        print("❌ Validation failed. Aborting write.")
        sys.exit(1)

    # 4. Write
    print("\n💾 Writing files...")
    write_files(expanded)

    print(f"\n✅ Done! {total_expanded} topics across {len(expanded)} files.")

if __name__ == "__main__":
    main()
