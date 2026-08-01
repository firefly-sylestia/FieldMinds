#!/usr/bin/env python3
"""Finalize CurioMixedDeck blends.

Refines the 15 curated pairs (brightest shade that still clears 4.5:1 vs
white), computes curated triples (HSL centroid + same 4.5:1 steering), and
prints the Kotlin-ready tables for CurioColors.kt. Also verifies every
resulting blend.
"""
import math
from itertools import combinations

ACCENTS = {
    "Indigo": (0x43, 0x38, 0xCA),
    "Rose":   (0xBE, 0x12, 0x3C),
    "Amber":  (0xB4, 0x53, 0x09),
    "Teal":   (0x0F, 0x76, 0x6E),
    "Sky":    (0x03, 0x69, 0xA1),
    "Coral":  (0xFF, 0x8F, 0xA3),
}

CURATED_PAIRS = {
    ("Indigo", "Rose"):   (0xA7, 0x2C, 0xD6),
    ("Indigo", "Amber"):  (0xA9, 0x26, 0xB5),
    ("Indigo", "Teal"):   (0x1F, 0x62, 0xA8),
    ("Indigo", "Sky"):    (0x16, 0x49, 0xC4),
    ("Indigo", "Coral"):  (0xC4, 0x4A, 0xD2),
    ("Rose", "Amber"):    (0xBF, 0x1E, 0x14),
    ("Rose", "Teal"):     (0x4A, 0x12, 0xA8),
    ("Rose", "Sky"):      (0x6D, 0x0B, 0xB8),
    ("Rose", "Coral"):    (0xF0, 0x2D, 0x59),
    ("Amber", "Teal"):    (0x15, 0x8A, 0x5C),
    ("Amber", "Sky"):     (0x0C, 0x8B, 0x8A),
    ("Amber", "Coral"):   (0xF0, 0x3F, 0x22),
    ("Teal", "Sky"):      (0x06, 0x7E, 0x94),
    ("Teal", "Coral"):    (0x6C, 0x18, 0xF5),
    ("Sky", "Coral"):     (0x9E, 0x1B, 0xFF),
}

PAIR_ORDER = [  # display order matching the Kotlin table groups
    ("Indigo", "Rose"), ("Indigo", "Amber"), ("Indigo", "Teal"),
    ("Indigo", "Sky"), ("Indigo", "Coral"),
    ("Rose", "Amber"), ("Rose", "Teal"), ("Rose", "Sky"), ("Rose", "Coral"),
    ("Amber", "Teal"), ("Amber", "Sky"), ("Amber", "Coral"),
    ("Teal", "Sky"), ("Teal", "Coral"), ("Sky", "Coral"),
]


def hexs(rgb):
    return "#%02X%02X%02X" % rgb


def luminance(rgb):
    def f(c):
        c = c / 255.0
        return c / 12.92 if c <= 0.03928 else ((c + 0.055) / 1.055) ** 2.4
    r, g, b = map(f, rgb)
    return 0.2126 * r + 0.7152 * g + 0.0722 * b


def contrast(rgb):
    l = luminance(rgb)
    return (1.0 + 0.05) / (l + 0.05)


def to_hsl(rgb):
    r, g, b = (c / 255.0 for c in rgb)
    mx, mn = max(r, g, b), min(r, g, b)
    l = (mx + mn) / 2.0
    d = mx - mn
    if d == 0:
        return (0.0, 0.0, l)
    s = d / (1 - abs(2 * l - 1))
    if mx == r:
        h = ((g - b) / d) % 6
    elif mx == g:
        h = (b - r) / d + 2
    else:
        h = (r - g) / d + 4
    return (h * 60.0, s, l)


def from_hsl(h, s, l):
    c = (1 - abs(2 * l - 1)) * s
    hp = h / 60.0
    x = c * (1 - abs(hp % 2 - 1))
    if hp < 1: r, g, b = c, x, 0.0
    elif hp < 2: r, g, b = x, c, 0.0
    elif hp < 3: r, g, b = 0.0, c, x
    elif hp < 4: r, g, b = 0.0, x, c
    elif hp < 5: r, g, b = x, 0.0, c
    else: r, g, b = c, 0.0, x
    m = l - c / 2
    return tuple(round((v + m) * 255) for v in (r, g, b))


def steer(h, s, target=4.5):
    """Brightest shade of (h, s) clearing `target` WCAG contrast vs white."""
    lo, hi = 0.0, 1.0
    for _ in range(48):
        mid = (lo + hi) / 2
        if contrast(from_hsl(h, s, mid)) >= target:
            lo = mid
        else:
            hi = mid
    return from_hsl(h, s, lo)


def centroid(colors):
    hs = [to_hsl(c) for c in colors]
    sx = sum(math.cos(math.radians(h[0])) for h in hs) / len(hs)
    sy = sum(math.sin(math.radians(h[0])) for h in hs) / len(hs)
    h = math.degrees(math.atan2(sy, sx)) % 360
    s = min(1.0, sum(h[1] for h in hs) / len(hs) + 0.05)
    return h, s


def kt(rgb):
    return "Color(0xFF%02X%02X%02X)" % rgb


# ── 1) Final pair values: keep curated if >= 4.5, else brighten/darken to 4.5
print("== FINAL PAIR BLENDS ==")
final_pairs = {}
for key in PAIR_ORDER:
    curated = CURATED_PAIRS[key]
    c = contrast(curated)
    if c >= 4.5:
        final = curated
        note = "keep"
    else:
        h, s, _ = to_hsl(curated)
        final = steer(h, s)
        note = "steered %.2f -> %.2f" % (c, contrast(final))
    final_pairs[key] = final
    print("  %-28s %s  %s  vs-white=%.2f  %s" % (
        "%s + %s" % key, hexs(final), kt(final), contrast(final), note))

# ── 2) Curated triples
print()
print("== FINAL TRIPLE BLENDS ==")
names = list(ACCENTS.keys())
final_triples = {}
for combo in combinations(names, 3):
    h, s = centroid([ACCENTS[n] for n in combo])
    final = steer(h, s)
    final_triples[combo] = final
    print("  %-34s %s  %s  vs-white=%.2f" % (
        " + ".join(combo), hexs(final), kt(final), contrast(final)))

# ── 3) Kotlin-ready blocks
print()
print("== KOTLIN PairBlends ==")
for key in PAIR_ORDER:
    print("        setOf(CurioColors.Category%s, CurioColors.Category%s) to %s," % (
        key[0], key[1], kt(final_pairs[key])))
print()
print("== KOTLIN TripleBlends ==")
for combo in combinations(names, 3):
    print("        setOf(CurioColors.Category%s, CurioColors.Category%s, CurioColors.Category%s) to %s," % (
        combo[0], combo[1], combo[2], kt(final_triples[combo])))

# ── 4) Sanity: every curated + runtime candidate clears 4.5
print()
print("== SANITY (min contrast across all blends) ==")
allb = list(final_pairs.values()) + list(final_triples.values())
print("  pairs min=%.2f, triples min=%.2f" % (
    min(contrast(v) for v in final_pairs.values()),
    min(contrast(v) for v in final_triples.values())))
