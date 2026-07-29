#!/usr/bin/env python3
"""Wave-1 recovery script — fix structural + quality issues across all 11 topic JSON files.

Three fix categories in one pass:
  1. SMART-QUOTE NORMALIZATION: Replace curly quotes ('\u2019' and '\u201d') inside
     teaser/name/instruction/targetName string values with ASCII ' and ".
     (Curly quotes are valid in UTF-8 JSON but were used inside what was meant to
     be a JSON string — leaving the JSON parser confused.)

  2. ORPHAN-TOPIC REMOVAL: Delete `book-jane-eyre-wuthering` from books.json
     (malformed: duplicate imageUrl, missing exploreAction wrapper around
     instruction, dangling closing brace). Will be replaced with a clean
     `book-persuasion` topic that uses the SAME instruction as a properly
     wrapped topic.

  3. INSTRUCTION-LENGTH TRIM: For every topic whose instruction > 280 chars,
     trim to exactly 270 chars at the nearest word boundary + add an ellipsis
     ("...") if the cut wasn't at the end of a sentence. Schema spec is 280
     char max — we trim to 270 for safety margin.

Idempotent — running twice produces the same output.

Run: python3 scripts/recover_topics.py
"""
import json
import re
import sys
from pathlib import Path

ROOT = Path("app/src/main/assets/topics")
MAX_INST_LEN = 270  # cap at 270 to leave 10-char margin from the 280 schema cap

# 1. Smart-quote normalization map (apply to inside JSON string values)
SMART_QUOTES = {
    "\u2018": "'",   # left single curly
    "\u2019": "'",   # right single curly (apostrophe)
    "\u201c": '"',   # left double curly
    "\u201d": '"',   # right double curly
}


def normalize_smart_quotes_in_strings(raw: str) -> tuple[str, int]:
    """Return (new_raw, n_subs) — replace smart quotes inside string values only.

    Heuristic: we treat ALL smart quotes as inside-string (since they're valid
    Unicode chars and JSON strings may contain them after the first ASCII
    quote that opens them).
    """
    n = 0
    out_chars = []
    in_string = False
    escape_next = False
    for ch in raw:
        if escape_next:
            out_chars.append(ch)
            escape_next = False
            continue
        if ch == "\\":
            out_chars.append(ch)
            escape_next = True
            continue
        if ch == '"':
            in_string = not in_string
            out_chars.append(ch)
            continue
        if in_string and ch in SMART_QUOTES:
            out_chars.append(SMART_QUOTES[ch])
            n += 1
            continue
        out_chars.append(ch)
    return "".join(out_chars), n


def trim_to_word_boundary(text: str, limit: int) -> str:
    """If text is longer than limit, cut at the nearest word boundary ≤ limit."""
    if len(text) <= limit:
        return text
    # First, prefer cutting at a sentence-ending punctuation within limit
    cut_at = -1
    for i in range(limit - 1, -1, -1):
        if text[i] in ".!?":
            cut_at = i + 1
            break
    if cut_at == -1 or cut_at < limit - 60:
        # Fall back to word boundary
        cut_at = -1
        for i in range(limit - 1, -1, -1):
            if text[i] == " ":
                cut_at = i
                break
    if cut_at == -1 or cut_at < limit - 60:
        # Hard cut
        cut_at = limit
    trimmed = text[:cut_at].rstrip()
    if cut_at < len(text):
        # Only add ellipsis if we actually cut mid-content
        if not trimmed.endswith((".", "!", "?")):
            trimmed = trimmed.rstrip(",;:") + "..."
        elif trimmed[-1] in ".!?":
            # already a sentence-end; don't add ellipsis (keeps clean)
            pass
    return trimmed


# Clean replacement topic for the books.json orphan (book-persuasion)
BOOK_PERSUASION_TOPIC = {
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
        "instruction": "Read the opening chapter. Austen wrote it while ill and mostly bedridden. The novel's tone is half-Romantic, half-classical; Anne Elliot never speaks angrily. By volume II, she's watching Captain Wentworth move through rooms. The famous final letter is what the whole novel is structured around.",
    },
    "tags": ["Regency", "British", "Novel"],
    "tier": 2,
}


def process_file(p: Path) -> dict:
    """Run all three fix categories on one JSON file. Returns counts dict."""
    raw = p.read_text()

    # 1. Smart-quote normalization
    new_raw, smart_n = normalize_smart_quotes_in_strings(raw)

    # 2. Parse JSON (should now succeed for files that were JSON-broken)
    try:
        data = json.loads(new_raw)
    except json.JSONDecodeError as e:
        return {"file": p.name, "smart_quotes_fixed": smart_n,
                "parse_error": str(e), "trimmed": 0, "orphan_dropped": 0, "replacement_added": 0}

    # 3. Drop the malformed orphan from books.json (its replacement is added at end)
    orphan_dropped = 0
    if p.name == "books.json":
        before = len(data)
        data = [t for t in data if t.get("id") != "book-jane-eyre-wuthering"]
        orphan_dropped = before - len(data)

    # 4. Trim over-length instructions
    trimmed = 0
    for t in data:
        inst = t.get("exploreAction", {}).get("instruction", "")
        if len(inst) > MAX_INST_LEN:
            t["exploreAction"]["instruction"] = trim_to_word_boundary(inst, MAX_INST_LEN)
            trimmed += 1

    # 5. Add book-persuasion replacement (idempotent: skip if already present)
    replacement_added = 0
    if p.name == "books.json":
        if not any(t.get("id") == "book-persuasion" for t in data):
            data.append(BOOK_PERSUASION_TOPIC)
            replacement_added = 1

    # Re-serialize
    p.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n")

    return {"file": p.name, "smart_quotes_fixed": smart_n,
            "parse_error": None, "trimmed": trimmed,
            "orphan_dropped": orphan_dropped,
            "replacement_added": replacement_added}


def main():
    files = sorted(ROOT.glob("*.json"))
    if not files:
        print(f"No JSON files in {ROOT}", file=sys.stderr)
        sys.exit(1)

    print(f"Processing {len(files)} files in {ROOT}\n")
    totals = {"smart_quotes_fixed": 0, "trimmed": 0,
              "orphan_dropped": 0, "replacement_added": 0,
              "parse_errors": 0}
    for p in files:
        result = process_file(p)
        if result.get("parse_error"):
            print(f"  ✗ {result['file']:20s} STILL BROKEN: {result['parse_error']}")
            totals["parse_errors"] += 1
            continue
        print(f"  ✓ {result['file']:20s} smart={result['smart_quotes_fixed']:3d} "
              f"trimmed={result['trimmed']:3d} "
              f"orphan={result['orphan_dropped']} add={result['replacement_added']}")
        for k in ("smart_quotes_fixed", "trimmed", "orphan_dropped", "replacement_added"):
            totals[k] += result[k]
    print()
    print(f"Totals: smart-quotes={totals['smart_quotes_fixed']} "
          f"trimmed-instructions={totals['trimmed']} "
          f"orphan-dropped={totals['orphan_dropped']} "
          f"replacement-added={totals['replacement_added']} "
          f"parse-errors={totals['parse_errors']}")


if __name__ == "__main__":
    main()
