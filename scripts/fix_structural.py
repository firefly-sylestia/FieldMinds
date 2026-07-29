#!/usr/bin/env python3
"""Wave-1 structural repair script — fix remaining JSON-level breakage.

Three categories:
  1. artworks.json: ~19 topics where the template was mis-pasted — duplicate
     `"imageUrl": "",` line + `tags` and `tier` accidentally nested INSIDE
     `exploreAction` instead of at topic level. Fix via regex on raw text
     because the JSON is currently un-parseable.
  2. Two `"explart"` typos (should be `"exploreAction"`).
  3. books.json: orphan `book-jane-eyre-wuthering` topic with broken
     structure (missing exploreAction wrapper, duplicate imageUrl, dangling
     closing brace). Replace with a properly-structured `book-persuasion`.

Plus two topic-level schema fixes:
  - discoveries.json `discovery-antibiotics-streptomycin`: missing
    `targetName` and `durationMinutes` in exploreAction.
  - scientists.json `scientist-erdos`: missing `instruction` in exploreAction.

Idempotent — running twice produces the same output.
"""
import json
import re
import sys
from pathlib import Path

ROOT = Path("app/src/main/assets/topics")


def fix_artworks():
    """Repair the nested-tags/tier bug + duplicate imageUrl + explart typo."""
    p = ROOT / "artworks.json"
    text = p.read_text()

    # Fix the "explart" typo (appears 2 times — line 510 + scientists file)
    n_explart = text.count('"explart"')
    text = text.replace('"explart"', '"exploreAction"')

    # Remove duplicate `"imageUrl": "",` line that appears before exploreAction
    imageurl_pattern = re.compile(
        r'"imageUrl": "",\s*\n\s*"imageUrl": "",\s*\n\s*"exploreAction"'
    )
    n_imageurl = len(imageurl_pattern.findall(text))
    text = imageurl_pattern.sub(
        '"imageUrl": "",\n    "exploreAction"',
        text
    )

    # Fix nested tags/tier: pattern is `,\n      "tags": [...],\n      "tier": N\n    }`
    # Becomes: `,\n    },\n    "tags": [...],\n    "tier": N`
    nested_pattern = re.compile(
        r',\n(      )"tags":\s*(\[[^\]]*?\]),\n\1"tier":\s*(\d+)\n(\s+)\}'
    )
    matches = nested_pattern.findall(text)
    n_nested = len(matches)
    text = nested_pattern.sub(
        r',\n    },\n    "tags": \2,\n    "tier": \3',
        text
    )

    # Verify the result parses
    try:
        data = json.loads(text)
    except json.JSONDecodeError as e:
        return {"file": "artworks.json", "status": "FAILED",
                "error": str(e),
                "explart_fixed": n_explart,
                "duplicate_imageUrl_removed": n_imageurl,
                "nested_tags_fixed": n_nested}

    # Re-serialize
    p.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n")
    return {"file": "artworks.json", "status": "OK",
            "explart_fixed": n_explart,
            "duplicate_imageUrl_removed": n_imageurl,
            "nested_tags_fixed": n_nested,
            "topics": len(data)}


def fix_books_orphan():
    """Replace the malformed `book-jane-eyre-wuthering` orphan with a clean `book-persuasion`."""
    p = ROOT / "books.json"
    text = p.read_text()

    # Find the orphan topic — it's at the end of the file before the closing `]`
    # Pattern: starts with `"id": "book-jane-eyre-wuthering",` and goes to `  }`
    orphan_pattern = re.compile(
        r'\{\s*\n\s*"id":\s*"book-jane-eyre-wuthering",.*?\n\s*\}\s*\n\s*\]',
        re.DOTALL
    )

    replacement_topic = '''{
    "id": "book-persuasion",
    "categoryId": "BOOKS",
    "subtype": "Novel",
    "name": "Persuasion",
    "teaser": "Austen's last completed novel (1818), a short, melancholy work about a 27-year-old woman who broke off an engagement with a poor man eight years before and now encounters him again, wealthy.",
    "imageUrl": "",
    "exploreAction": {
      "verb": "Read",
      "targetName": "Persuasion, Volume I (Chapters 1-12)",
      "durationMinutes": 60,
      "instruction": "Read the opening chapter. Austen wrote it while ill and mostly bedridden. The novel's tone is half-Romantic, half-classical; Anne Elliot never speaks angrily. By volume II, she's watching Captain Wentworth move through rooms. The famous final letter is what the whole novel is structured around."
    },
    "tags": ["Regency", "British", "Novel"],
    "tier": 2
  }
]'''

    matches = orphan_pattern.findall(text)
    n_orphan = len(matches)
    if n_orphan:
        text = orphan_pattern.sub(replacement_topic, text)

    try:
        data = json.loads(text)
    except json.JSONDecodeError as e:
        return {"file": "books.json", "status": "FAILED",
                "error": str(e),
                "orphan_replaced": n_orphan}

    # Remove any duplicate `book-persuasion` entries
    seen_ids = set()
    deduped = []
    for t in data:
        if t.get("id") == "book-persuasion":
            if "book-persuasion" in seen_ids:
                continue
            seen_ids.add("book-persuasion")
        deduped.append(t)
    n_dedup = len(data) - len(deduped)
    data = deduped

    p.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n")
    return {"file": "books.json", "status": "OK",
            "orphan_replaced": n_orphan, "dedup_removed": n_dedup,
            "topics": len(data)}


def fix_discoveries_streptomycin():
    """Add missing targetName + durationMinutes to the streptomycin entry."""
    p = ROOT / "discoveries.json"
    text = p.read_text()
    # This entry is the one I wrote that has incomplete exploreAction.
    # We'll find the streptomycin block, identify the exploreAction, and add the fields.
    # Pattern: looks for the streptomycin block then a `}` then missing targetName/durationMinutes
    pattern = re.compile(
        r'(\{\s*\n\s*"id":\s*"discovery-antibiotics-streptomycin",.*?"instruction":\s*"[^"]*")\s*\n(\s*\}\s*,?\s*\n\s*"tags")',
        re.DOTALL
    )

    def insert_fields(match):
        head = match.group(1)
        tail = match.group(2)
        # Insert targetName + durationMinutes before the closing `}`
        return (
            head + ',\n      "targetName": "Schatz, Bugie, and Waksman, 1944 paper (Proc. Soc. Exp. Biol. Med.)",'
                  '\n      "durationMinutes": 30'
            + '\n    ' + tail
        )

    text2, n = pattern.subn(insert_fields, text)
    if n == 0:
        return {"file": "discoveries.json", "status": "NOT_FOUND"}
    data = json.loads(text2)
    p.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n")
    return {"file": "discoveries.json", "status": "OK", "fixed": n}


def fix_scientists_erdos():
    """Add missing instruction to the erdos entry."""
    p = ROOT / "scientists.json"
    text = p.read_text()
    # Pattern: find erdos block where exploreAction has no instruction field
    pattern = re.compile(
        r'(\{\s*\n\s*"id":\s*"scientist-erdos",.*?"durationMinutes":\s*\d+)\s*\n(\s*"tags")',
        re.DOTALL
    )

    def insert_instruction(match):
        head = match.group(1)
        tail = match.group(2)
        return (
            head + ',\n      "instruction": "Read his papers in analytic number theory — e.g. with Kac, On the number of prime factors of n (1940). Erdős co-authored 1,500+ papers in 50 years with 500+ collaborators across mathematics. He had no permanent home; his collaborators hosted him. His work on the probabilistic method and random graph theory defines modern combinatorics."'
            + '\n    ' + tail
        )

    text2, n = pattern.subn(insert_instruction, text)
    if n == 0:
        return {"file": "scientists.json", "status": "NOT_FOUND"}
    data = json.loads(text2)
    p.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n")
    return {"file": "scientists.json", "status": "OK", "fixed": n}


def main():
    results = [
        fix_artworks(),
        fix_books_orphan(),
        fix_discoveries_streptomycin(),
        fix_scientists_erdos(),
    ]
    for r in results:
        print(f"  {r.get('file', '?'):25s} status={r.get('status'):10s} {r}")


if __name__ == "__main__":
    main()
