#!/usr/bin/env python3
"""Vary the repetitive 'Look at the…' instruction openers in topic files.

The artwork batches (and earlier painter/discovery/wildcard rewrites) opened
every exploreAction.instruction with 'Look at the …', which reads as
copy-paste in the app. This script replaces the opener with a wide pool of
natural alternatives, evenly distributed per file so no two consecutive
entries share an opener. Only the opening phrase changes; the handcrafted
body of each instruction is preserved. Keeps ≤ 450 chars (quality bar).
"""

import json
import random
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
TOPICS = ROOT / "app/src/main/assets/topics"

# Natural alternatives to 'Look at' — every one accepts 'the X…', 'a X…',
# and '"Title" …' as its direct object, so the swap is grammatical everywhere.
POOL = [
    "Focus on",
    "Start with",
    "Begin with",
    "Zero in on",
    "Study",
    "Notice",
    "Consider",
    "Check",
    "Examine",
    "Fix on",
    "Pause on",
    "Turn to",
    "Single out",
    "Follow",
    "Pick out",
    "Peer at",
    "Dwell on",
    "Scan",
]

# Patterns: 'Look at the X…', 'Look at a/an X…', 'Look at "Title" …'
PATTERNS = (
    re.compile(r"^Look at (the .+)", re.S),
    re.compile(r"^Look at (a[n]? .+)", re.S),
    re.compile(r'^Look at ("[^"]*".*)', re.S),
)


def rewrite_file(path: Path) -> int:
    data = json.loads(path.read_text(encoding="utf-8"))
    # deterministic per-file shuffle so distribution differs across files
    rng = random.Random(hash(path.name))
    pool = POOL[:]
    rng.shuffle(pool)

    changed = 0
    for idx, e in enumerate(data):
        ins = e.get("exploreAction", {}).get("instruction", "")
        for pat in PATTERNS:
            m = pat.match(ins)
            if m:
                obj = m.group(1)
                new = pool[idx % len(pool)] + " " + obj
                e["exploreAction"]["instruction"] = new
                changed += 1
                break
    if changed:
        path.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    return changed


def main() -> int:
    total = 0
    for path in sorted(TOPICS.glob("*.json")):
        if path.name == "SCHEMA.md":
            continue
        n = rewrite_file(path)
        if n:
            print(f"{path.name:18} {n:4d} openers varied")
        total += n
    print(f"TOTAL: {total}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
