#!/usr/bin/env python3
"""Diagnose schema errors from the topic-content drop.

Runs the Python validator against all 11 JSON topic files, captures every error
emitted by validate_topics.py, categorizes them by error TYPE (e.g. over-length
instruction) and per FILE, and emits a clean human-readable report to stdout.

Used to triage the 136-error residue from the wave-1 content drop.
"""
import subprocess
import re
import json
from collections import Counter, defaultdict
from pathlib import Path

ROOT = Path("app/src/main/assets/topics")

# Run validator and capture output
result = subprocess.run(
    ["python3", "scripts/validate_topics.py"],
    capture_output=True, text=True
)
output = result.stdout + result.stderr

# Every error line begins with two spaces + bullet +
# e.g. "  • albums.json: topic '...' foo is N chars (max 280)"
# Note the bullet is literal U+2022 in the validator output.
ERR_RE = re.compile(r"^  [•] (\S+?\.json):\s*topic '([^']+)' (.+)$", re.MULTILINE)

by_file = defaultdict(list)
by_type = Counter()
all_errors = []

for match in ERR_RE.finditer(output):
    fname, topic_id, rest = match.groups()
    by_file[fname].append((topic_id, rest))
    all_errors.append((fname, topic_id, rest))

# Heuristically classify "rest" by keywords
def classify(rest):
    if "instruction is" in rest and "max 450" in rest:
        return "instruction_over_length_280"
    if "exploreAction" in rest and "missing" in rest:
        return "missing_exploreAction"
    if any(k in rest for k in ("Missing id", "Missing name", "Missing teaser",
                                "Missing targetName", "Missing verb",
                                "Missing instruction", "Missing categoryId",
                                "Missing subtype", "Missing durationMinutes")):
        return "missing_required_field"
    if "tier" in rest:
        return "tier_invalid"
    return "other:" + rest[:60]

for fname, tid, rest in all_errors:
    by_type[classify(rest)] += 1

print("=" * 60)
print(f"TOTAL ERRORS: {len(all_errors)}")
print("=" * 60)

print("\nPER FILE:")
for fname in sorted(by_file):
    errs = by_file[fname]
    print(f"  {fname:20s} {len(errs)} errors")

print("\nBY TYPE:")
for kind, count in by_type.most_common():
    print(f"  {count:4d}  {kind}")

print("\nSAMPLE ERRORS BY TYPE (first 3 each):")
by_kind = defaultdict(list)
for fname, tid, rest in all_errors:
    by_kind[classify(rest)].append((fname, tid, rest))
for kind in sorted(by_kind):
    print(f"\n  [{kind}] x{len(by_kind[kind])}")
    for fname, tid, rest in by_kind[kind][:3]:
        print(f"    {fname}: '{tid}' → {rest[:90]}")

# Also: per-file topic counts
print("\n" + "=" * 60)
print("PER-FILE TOPIC COUNTS (from validator)")
print("=" * 60)
COUNT_RE = re.compile(r"^(\S+?\.json)\s+(\d+) topics (validated|\d+ errors)")
for m in COUNT_RE.finditer(output):
    print(f"  {m.group(1):20s} {m.group(2):>4} topics ({m.group(3)})")

# Also determine how many topics pass per file cleanly
print("\n" + "=" * 60)
print("WHICH FILES ARE FULLY VALID?")
print("=" * 60)
for fname in sorted(by_file):
    n = len(by_file[fname])
    status = "OK" if n == 0 else f"{n} errors"
    print(f"  {fname:20s} {status}")
