#!/usr/bin/env python3
"""Batch: replace the final 5 fake discoveries.json entries (203–207).

Germ Theory, Quantum Theory, Atomic Nucleus, Neutron, Transistor. Same
contract as batch_discoveries_1/2.py. Cap 450 (SCHEMA.md).
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


PATH = Path(__file__).resolve().parent.parent / "app/src/main/assets/topics/discoveries.json"


def _entry(subtype: str, teaser: str, instruction: str, target_name: str, tags: list[str]) -> dict:
    return {
        "subtype": subtype,
        "teaser": teaser,
        "instruction": instruction,
        "targetName": target_name,
        "tags": tags,
    }


FIXES: dict[str, dict] = {
    "disc-germ-theory-of-disease-203": _entry(
        "Theory",
        "The germ theory of disease — that microscopic organisms cause infection — was established in the 1860s–70s by Louis Pasteur and Robert Koch, against the millennia-old theory of 'spontaneous generation,' which held that life arose from rotting matter. Pasteur's 1861 swan-neck flask experiments showed that boiled broth stayed sterile unless exposed to air, and Koch's 1882 discovery of the tuberculosis bacterium turned the theory into medicine's working model.",
        "Read Pasteur's swan-neck flask experiment and follow the logic: broth in a flask with a curved neck boiled and stayed clear for years — air reached it, but the dust (and microbes) trapped in the neck's bend did not. Then read Koch's contribution: his four 'postulates' — the microbe must be present, isolatable, able to cause the disease when transferred, and recoverable — gave doctors a checklist that turned germ theory from a philosophy into a laboratory method, and his 1882 TB discovery is the theory's first great clinical proof.",
        "Pasteur's swan-neck flasks (1861) + Koch's TB discovery (1882)",
        ["Biology", "Medicine", "1800s"],
    ),
    "disc-quantum-theory-1900-204": _entry(
        "Theory",
        "Quantum theory began on 14 December 1900, when Max Planck — reluctantly, calling it 'an act of desperation' — proposed that energy comes in discrete packets, or quanta, to explain why heated objects glow the way they do. Einstein extended the idea to light itself in 1905, and within three decades the quantum had explained atoms, chemistry, and nuclear physics — while making physicists argue about what it all means to this day.",
        "Read the 1900 blackbody problem and feel Planck's reluctance: classical physics predicted the wrong color for glowing objects (the 'ultraviolet catastrophe'), and Planck's fix — assuming energy is emitted in chunks of h×f — worked perfectly but had no physical justification he believed in. Then read the two rival accounts of what quantization means: the Copenhagen interpretation (the wavefunction is everything; nature is probabilistic) versus the many-worlds interpretation (every possibility happens, in separate branches) — the disagreement is philosophical, and experiments still cannot settle it.",
        "Planck's 1900 quantum paper + the interpretation debate",
        ["Physics", "Quantum", "1900s"],
    ),
    "disc-the-atomic-nucleus-1911-205": _entry(
        "Discovery",
        "The atomic nucleus was discovered in 1911 by Ernest Rutherford's lab, when a gold-foil experiment — designed to measure the spread of alpha particles — found that a tiny fraction bounced straight back: the atom's mass is concentrated in a nucleus 100,000 times smaller than the atom itself. Rutherford reportedly compared it to firing a 15-inch shell at tissue paper and having it bounce back at you.",
        "Read the 1911 gold-foil experiment and notice the setup: Geiger and Marsden, Rutherford's students, fired alpha particles at gold foil and counted where they emerged — most passed through (the atom is mostly empty space), but about 1 in 8,000 bounced back, which required a dense positive core. Then read Rutherford's own words — the 'tissue paper' analogy appears in his later lectures — and connect the result to the planetary model it produced: electrons in orbits around a nucleus, the picture every atom diagram since has drawn.",
        "The 1911 gold-foil experiment + the nuclear atom model",
        ["Physics", "Atoms", "1910s"],
    ),
    "disc-the-neutron-1932-206": _entry(
        "Discovery",
        "James Chadwick discovered the neutron in 1932 — a neutral particle with roughly the proton's mass, whose existence had been suspected for a decade but never shown. The discovery was the key that unlocked nuclear physics: with the neutron, physicists could explain isotopes, split the atom (fission, 1938), and build the chain reaction — Chadwick won the 1935 Nobel, and his finding made both nuclear power and nuclear weapons possible.",
        "Read the 1932 experiments and notice what made them conclusive: Chadwick bombarded beryllium with alpha particles and got radiation that knocked protons out of paraffin wax with energies no photon could have — the 'rays' had to be particles of proton-like mass. Then read the atomic physics that fell into place: atoms of the same element with different masses (isotopes) were just atoms with different neutron counts — a fact that had been inexplicable for a decade — and the neutron's lack of charge let it penetrate nuclei, which is why it, not the proton, became the key that unlocked the atom.",
        "Chadwick's 1932 experiments + the isotope explanation",
        ["Physics", "Particles", "1930s"],
    ),
    "disc-the-transistor-1947-207": _entry(
        "Invention",
        "The transistor was invented on 23 December 1947 at Bell Labs by John Bardeen, Walter Brattain, and William Shockley — a tiny solid-state device that amplifies or switches electrical signals, replacing the vacuum tube. It is the fundamental building block of everything electronic: a modern computer chip holds billions of transistors, and the three inventors shared the 1956 Nobel.",
        "Read the December 1947 demonstration and notice the scale: the first working transistor was a wedge of gold foil pressed into germanium, amplifying an audio signal by a factor of ~100 — and it worked for 17 hours before failing. Then read the technology's trajectory: Shockley's team spent years trying to get the 'point-contact' device to behave; the field-effect transistor that replaced it (1959) is the kind on every chip today, and Moore's Law — the doubling of transistor counts every two years — is the rule that turned a lab curiosity into the information age.",
        "The 23 December 1947 point-contact transistor + Moore's Law",
        ["Physics", "Electronics", "1940s"],
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
        topic["subtype"] = fix["subtype"]
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
