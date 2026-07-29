#!/usr/bin/env python3
"""
Wave-1 final recovery for the Curio content drop on `revamp` branch.

Fixes three classes of damage from the Wave-1 authoring pass:

1. artworks.json — every topic has `tags` and `tier` accidentally nested INSIDE
   `exploreAction` (6-space indent) instead of at topic level (4-space indent).
   Result: JSON.parse fails at the second topic because exploreAction never
   closes before the next topic opens.

2. books.json — 18 topics have over-length instructions (>280 chars) per the
   SCHEMA.md cap. Auto-trim at word boundary to <=270 chars (safer margin).

3. scientists.json — `scientist-erdos` topic is missing the required
   `instruction` field inside `exploreAction`. Add one.

Idempotent: safe to re-run.
"""
from __future__ import annotations

import json
import re
from pathlib import Path
from typing import Any

ROOT = Path("app/src/main/assets/topics")
TOPICS = ROOT


def fix_artworks() -> tuple[int, list[str]]:
    """Rebuild artworks.json topic-by-topic with correct structure.

    For each topic, extract the canonical fields and re-emit in proper order:
        id, categoryId, subtype, name, teaser, imageUrl,
        exploreAction { verb, targetName, durationMinutes, instruction },
        tags, tier
    """
    p = TOPICS / "artworks.json"
    raw = p.read_text(encoding="utf-8")
    fixes = []

    # Strategy: use a depth-aware walker to split the file into topic-blob
    # segments, then rebuild each. The array is the only top-level structure.
    # Topics are object literals at depth 1 inside the array.

    # Strip the surrounding [ and ]
    inner = raw.strip()
    assert inner.startswith("[") and inner.endswith("]"), "Expected bare array"
    inner = inner[1:-1].strip()

    topics = _split_topics(inner)
    print(f"  artworks: found {len(topics)} topic-blobs")

    rebuilt: list[str] = []
    for blob in topics:
        topic = _parse_topic_blob(blob)
        if topic is None:
            fixes.append(f"FAILED to parse blob ({blob[:80]!r})")
            continue
        # Re-emit
        rebuilt.append(json.dumps(topic, indent=2, ensure_ascii=False))

    out = "[\n" + ",\n".join(rebuilt) + "\n]\n"
    p.write_text(out, encoding="utf-8")
    fixes.append(f"rebuilt {len(rebuilt)} topics with normalized structure")
    return len(rebuilt), fixes


def _split_topics(text: str) -> list[str]:
    """Walk through the text and split into per-topic blobs.

    Tracks brace depth and string boundaries so we correctly identify the
    boundaries of each top-level topic object.
    """
    topics: list[str] = []
    cur: list[str] = []
    depth = 0
    in_string = False
    escape = False
    started = False

    for ch in text:
        if not started:
            # Skip until we see the first `{`
            if ch == "{":
                started = True
                depth = 1
                cur.append(ch)
            continue

        cur.append(ch)

        if escape:
            escape = False
            continue
        if ch == "\\":
            escape = True
            continue
        if ch == '"':
            in_string = not in_string
            continue
        if in_string:
            continue
        if ch == "{":
            depth += 1
        elif ch == "}":
            depth -= 1
            if depth == 0:
                # End of topic
                blob = "".join(cur).strip().rstrip(",").strip()
                topics.append(blob)
                cur = []

    # Re-join any remaining text between topics (commas, whitespace)
    return topics


def _parse_topic_blob(blob: str) -> dict[str, Any] | None:
    """Parse a single topic blob and normalize to the canonical schema.

    Strategy: try json.loads first. If it works, return the dict.
    If it fails (because of the nested-tags bug), extract fields manually
    with regex patterns that work for the broken structure.
    """
    # First try: clean parse
    try:
        return json.loads(blob)
    except json.JSONDecodeError:
        pass

    # Fallback: extract fields with regex. The broken structure has tags/tier
    # nested inside exploreAction; we need to pull them out.
    topic: dict[str, Any] = {}

    def extract(pattern: str, flags: int = re.DOTALL) -> str | None:
        m = re.search(pattern, blob, flags)
        return m.group(1).strip() if m else None

    # Required top-level scalars (always at correct indent in broken file too)
    for key in ("id", "categoryId", "subtype", "name"):
        m = re.search(rf'"({key})":\s*"((?:[^"\\]|\\.)*)"', blob)
        if m:
            topic[key] = m.group(2)
    # teaser may contain commas; grab the value via balanced quote match
    m = re.search(r'"teaser":\s*"((?:[^"\\]|\\.)*)"', blob)
    if m:
        topic["teaser"] = m.group(1)
    m = re.search(r'"imageUrl":\s*"((?:[^"\\]|\\.)*)"', blob)
    if m:
        topic["imageUrl"] = m.group(1)

    # exploreAction fields
    explore: dict[str, Any] = {}
    m = re.search(r'"verb":\s*"((?:[^"\\]|\\.)*)"', blob)
    if m:
        explore["verb"] = m.group(1)
    m = re.search(r'"targetName":\s*"((?:[^"\\]|\\.)*)"', blob)
    if m:
        explore["targetName"] = m.group(1)
    m = re.search(r'"durationMinutes":\s*(\d+)', blob)
    if m:
        explore["durationMinutes"] = int(m.group(1))
    m = re.search(r'"instruction":\s*"((?:[^"\\]|\\.)*)"', blob)
    if m:
        explore["instruction"] = m.group(1)
    if explore:
        topic["exploreAction"] = explore

    # tags — may be at 4-space (correct) or 6-space (nested bug)
    tags_match = re.search(r'"tags":\s*(\[[^\]]*\])', blob)
    if tags_match:
        try:
            topic["tags"] = json.loads(tags_match.group(1))
        except json.JSONDecodeError:
            topic["tags"] = []

    # tier — may be at 4-space or 6-space
    tier_match = re.search(r'"tier":\s*(\d+)', blob)
    if tier_match:
        topic["tier"] = int(tier_match.group(1))

    # Verify we got the required fields
    required = ["id", "categoryId", "subtype", "name", "teaser", "imageUrl",
                "exploreAction", "tags", "tier"]
    missing = [k for k in required if k not in topic]
    if missing:
        # Try a more aggressive fallback: find ANY of the tags/tier at any indent
        if "tags" not in topic:
            m = re.search(r'"tags"\s*:\s*(\[[^\]]*\])', blob)
            if m:
                topic["tags"] = json.loads(m.group(1))
        if "tier" not in topic:
            m = re.search(r'"tier"\s*:\s*(\d+)', blob)
            if m:
                topic["tier"] = int(m.group(1))
        missing = [k for k in required if k not in topic]
        if missing:
            print(f"    WARNING: still missing {missing} after fallback for id={topic.get('id', '???')}")
            return None

    return topic


def fix_books() -> tuple[int, list[str]]:
    """Auto-trim over-length instructions in books.json to <=270 chars."""
    p = TOPICS / "books.json"
    data = json.loads(p.read_text(encoding="utf-8"))
    fixes = []
    trimmed = 0
    cap = 270  # safety margin under the 280 validator cap

    for topic in data:
        ea = topic.get("exploreAction", {})
        inst = ea.get("instruction", "")
        if len(inst) > 280:
            ea["instruction"] = _smart_trim(inst, cap)
            trimmed += 1
            fixes.append(f"{topic['id']}: {len(inst)} -> {len(ea['instruction'])} chars")

    p.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    fixes.append(f"trimmed {trimmed} over-length instructions")
    return trimmed, fixes


def _smart_trim(text: str, cap: int) -> str:
    """Trim text to <=cap chars at a word boundary, ending with ellipsis if cut."""
    if len(text) <= cap:
        return text
    # Try to end at last word boundary before cap
    truncated = text[:cap]
    last_space = truncated.rfind(" ")
    if last_space > cap * 0.6:  # only trim at word boundary if reasonable
        truncated = truncated[:last_space]
    return truncated.rstrip(",;:. ") + "..."


def fix_scientists() -> tuple[int, list[str]]:
    """Add missing instruction to scientist-erdos."""
    p = TOPICS / "scientists.json"
    data = json.loads(p.read_text(encoding="utf-8"))
    fixes = []

    for topic in data:
        if topic["id"] == "scientist-erdos":
            ea = topic["exploreAction"]
            if "instruction" not in ea:
                ea["instruction"] = (
                    "Pick one of his 1,500+ papers. Read the abstract and the introduction. "
                    "Notice the spare, direct style — no preamble, no apology. Erdős wrote "
                    "like he talked: in proofs, not in prose. If you finish one, look for the "
                    "\"Erdős number\" — your distance from him through co-authorship."
                )
                fixes.append("scientist-erdos: added missing instruction")
            break

    p.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    return 0 if not fixes else 1, fixes


def main():
    print("=== Wave-1 final recovery ===\n")

    print("--- artworks.json ---")
    n_artworks, fixes_art = fix_artworks()
    for f in fixes_art:
        print(f"  {f}")

    print("\n--- books.json ---")
    n_books, fixes_bk = fix_books()
    for f in fixes_bk:
        print(f"  {f}")

    print("\n--- scientists.json ---")
    n_sci, fixes_sc = fix_scientists()
    for f in fixes_sc:
        print(f"  {f}")

    print("\n=== Re-validate all 11 files ===")
    import subprocess
    result = subprocess.run(
        ["python3", "scripts/validate_topics.py"],
        capture_output=True, text=True
    )
    print(result.stdout)
    if result.stderr:
        print("STDERR:", result.stderr)
    print(f"exit code: {result.returncode}")

    print("\n=== Per-file topic counts ===")
    for f in sorted(TOPICS.glob("*.json")):
        try:
            data = json.loads(f.read_text(encoding="utf-8"))
            print(f"  {f.name:22s} {len(data):3d} topics  {f.stat().st_size:>7d} bytes")
        except json.JSONDecodeError as e:
            print(f"  {f.name:22s} PARSE ERROR: {e}")


if __name__ == "__main__":
    main()
