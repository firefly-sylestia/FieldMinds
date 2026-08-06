#!/usr/bin/env python3
"""Batch: replace the first 40 fake discoveries.json entries with real facts.

The FIXES entries were template-generated (boilerplate teaser, generic
instruction, placeholder tags like Biology|19th Century regardless of
subject). Replaces subtype + teaser + instruction + targetName + tags.
Cap 450 (SCHEMA.md).
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
    "disc-evolution-by-natural-selection-121": _entry(
        "Theory",
        "Darwin's On the Origin of Species (1859) — 1,250 copies sold out on the first day — proposed that species change by natural selection: variation, inheritance, and differential survival. He spent two decades hesitating to publish, and the theory's famous phrase, 'survival of the fittest,' was actually coined by Herbert Spencer, not Darwin.",
        "Read the Origin's introduction and the final paragraph — Darwin's summary, including the overlooked line that selection works on 'domestic productions' and nature alike. Then read chapter 4's pigeon-breeding analogy: Darwin leaned on breeders' experience because, as he admits, the fossil record of his time showed 'no finely graduated organic chain.'",
        "On the Origin of Species (1859) — chapters 1 and 4",
        ["Biology", "Evolution", "1850s"],
    ),
    "disc-the-periodic-table-1869-122": _entry(
        "Theory",
        "Dmitri Mendeleev published his periodic table in 1869, arranging the 63 known elements by atomic weight — and leaving deliberate gaps for elements that didn't exist yet, predicting gallium, scandium, and germanium to within striking accuracy. He reportedly saw the table in a dream after three days of exhaustion.",
        "Look at Mendeleev's 1869 original table and compare it with the modern one: his rows and columns don't match today's because he ordered by atomic weight, not atomic number. Then find the three predicted elements (eka-aluminum, eka-boron, eka-silicon) and check their properties against Mendeleev's predictions — the germanium match is almost embarrassing in its accuracy.",
        "Mendeleev's 1869 periodic table vs. the modern one",
        ["Chemistry", "Elements", "1860s"],
    ),
    "disc-radiocarbon-dating-1949-123": _entry(
        "Technique",
        "Willard Libby's radiocarbon dating, announced in 1949, measures the decay of carbon-14 (half-life ~5,730 years) to date organic remains up to ~50,000 years old. He proved it worked by dating Egyptian artifacts whose ages were already known — and the technique immediately rewrote archaeology, from the Dead Sea Scrolls to Ötzi the Iceman.",
        "Read the 1949 paper's validation: Libby dated wood from a First Dynasty Egyptian tomb and got within a century of the accepted age, which is what convinced the field. Then understand the calibration problem: atmospheric carbon-14 varies over time (nuclear testing changed it), so raw dates must be calibrated against tree rings — which is why radiocarbon ages carry the 'BP' (before present) notation.",
        "Libby's 1949 Science paper — the Egyptian artifact validation",
        ["Archaeology", "Physics", "Technique"],
    ),
    "disc-the-polio-vaccine-1955-124": _entry(
        "Invention",
        "Jonas Salk's inactivated polio vaccine was declared safe on 12 April 1955 — the same day the field trial of 1.8 million children was announced as a success, making Salk an overnight national hero. Asked who owned the patent, he answered: 'Well, the people, I would say. There is no patent. Could you patent the sun?'",
        "Watch the 1955 announcement newsreel and notice the scale: the field trial was the largest medical experiment in history at the time. Then read about the Cutter Incident, weeks later, when a faulty batch of vaccine paralyzed 200 children — the response created the modern vaccine-safety regulatory system, which is the trial's real legacy.",
        "The 12 April 1955 announcement + the Cutter Incident",
        ["Medicine", "Vaccines", "1950s"],
    ),
    "disc-the-first-organ-transplant-125": _entry(
        "Event",
        "On 23 December 1954, surgeon Joseph Murray transplanted a kidney from Ronald Herrick into his identical twin brother Richard at Boston's Peter Bent Brigham Hospital — the first successful organ transplant in history. It worked precisely because the twins were genetically identical, which sidestepped the immune rejection that had killed all previous attempts.",
        "Read the 1954 case report and notice what it concedes: the transplant 'merely' moved a kidney between genetically identical bodies, so no immunosuppression was needed. Then read the longer arc — the development of immunosuppressive drugs (azathioprine, then cyclosporine) that made transplants between unrelated people possible, for which Murray won the 1990 Nobel.",
        "The 1954 Herrick case report + Murray's 1990 Nobel lecture",
        ["Medicine", "Surgery", "1950s"],
    ),
    "disc-ct-scanning-1971-126": _entry(
        "Invention",
        "The first CT scan of a patient's head was performed in October 1971 at Atkinson Morley Hospital in London — the machine, built by engineer Godfrey Hounsfield, took 160 parallel readings and required a room-sized computer to assemble the image. Hounsfield shared the 1979 Nobel in Physiology or Medicine.",
        "Look at the first CT image — a cyst in a woman's brain, rendered as a grid of numbers — and compare it with a modern scan: the improvement is in resolution, not principle. Then read how the machine worked: an X-ray source rotated around the head taking slice-by-slice readings, and a computer solved the equations to reconstruct the slice, an idea Hounsfield had while wondering how to map the contents of a box without opening it.",
        "The 1971 first CT image vs. a modern head scan",
        ["Medicine", "Imaging", "1970s"],
    ),
    "disc-stem-cells-1998-127": _entry(
        "Discovery",
        "James Thomson isolated the first human embryonic stem cells in 1998 at the University of Wisconsin — cells that can become any tissue in the body. The announcement ignited a research and ethics firestorm that shaped American politics for a decade, with some states funding the work and the federal government restricting it.",
        "Read the 1998 Science paper's restrained opening, then look at the timeline it set: Thomson grew the cells for months and showed they could differentiate into the three germ layers. Then read how the field evolved around the political fight — induced pluripotent stem cells (2006) were developed partly as an ethical end-run around the embryo debate.",
        "Thomson's 1998 Science paper + the ethics timeline",
        ["Biology", "Stem Cells", "1990s"],
    ),
    "disc-induced-pluripotent-stem-cells-128": _entry(
        "Discovery",
        "Shinya Yamanaka announced in 2006 that he had turned adult mouse skin cells back into embryonic-like stem cells using just four genes (Oct4, Sox2, Klf4, c-Myc) — the 'iPS' cell, a Nobel Prize in 2012. The discovery made the embryo debate moot overnight, because the starting material was ordinary adult tissue.",
        "Read Yamanaka's 2006 Cell paper and notice the scale of the effort: he screened 24 candidate genes and found the combination of four that worked, an approach colleagues called reckless until it worked. Then follow the 'Yamanaka factors' into the clinic: patient-specific iPS cells are now used to model diseases and, increasingly, to build transplant tissue.",
        "Yamanaka's 2006 Cell paper — the four-factor screen",
        ["Biology", "Stem Cells", "2000s"],
    ),
    "disc-the-first-exoplanet-1992-129": _entry(
        "Discovery",
        "The first confirmed planets outside our solar system were announced in 1992 — not around a sun-like star, but orbiting a pulsar, the crushed corpse of a dead star, found by Aleksander Wolszczan and Dale Frail. Three years later, 51 Pegasi b became the first planet found around a normal star, earning Mayor and Queloz the 2019 Nobel.",
        "Read the 1992 discovery and notice why the pulsar planets were so hard to believe: the timing anomalies of the pulsar's radio pulses are exquisitely regular, and the planets were inferred from their gravitational tug. Then read about 51 Pegasi b — a 'hot Jupiter' so close to its star that it orbits in four days, which broke every theory of how planets form.",
        "The 1992 pulsar planets paper + the 51 Pegasi b discovery",
        ["Astronomy", "Exoplanets", "1990s"],
    ),
    "disc-the-accelerating-universe-1998-130": _entry(
        "Discovery",
        "In 1998 two rival teams — one led by Saul Perlmutter, the other by Brian Schmidt and Adam Riess — independently found that distant supernovae were dimmer than expected: the universe's expansion is accelerating, pushed by an unknown 'dark energy' that makes up about 68% of everything. It won the 2011 Nobel and rewrote cosmology.",
        "Read how the measurement works: Type Ia supernovae explode with a known peak brightness, so their apparent dimness measures distance — and the 1998 data showed distant ones were farther than their redshift implied. Then read the discovery papers' own surprise: both teams expected to measure deceleration, and both published evidence of acceleration with visible reluctance.",
        "The 1998 Perlmutter and Riess–Schmidt supernova papers",
        ["Astronomy", "Cosmology", "Dark Energy"],
    ),
    "disc-the-god-particle-2012-131": _entry(
        "Discovery",
        "On 4 July 2012, CERN announced the discovery of the Higgs boson — the particle predicted in 1964 that gives other particles mass — after a 48-year search at the Large Hadron Collider. The nickname 'God particle' was coined by Nobel laureate Leon Lederman, who reportedly wanted to call it the 'goddamn particle.'",
        "Watch the 4 July 2012 announcement video and look at the actual data: the Higgs appears as a small bump over background in the invariant-mass plots, and physicists' excitement was proportional to how long they'd waited. Then read why the Higgs matters: without its field, electrons and quarks would be massless and atoms could never form.",
        "The 4 July 2012 CERN announcement — the Higgs bump plots",
        ["Physics", "Particle Physics", "2010s"],
    ),
    "disc-graphene-2004-132": _entry(
        "Discovery",
        "Graphene — a single layer of carbon atoms in a honeycomb lattice, one atom thick — was isolated in 2004 by Andre Geim and Konstantin Novoselov using sticky tape to peel layers off graphite. It's 200 times stronger than steel, flexible, nearly transparent, and conducts electricity better than copper; the pair won the 2010 Nobel.",
        "Watch the 'scotch tape method' video — the Nobel-winning discovery used ordinary adhesive tape — and read the 2004 Science paper's famously modest title ('Electric Field Effect in Atomically Thin Carbon Films'). Then read why it's a wonder material: its electrons move as if massless, which is what makes its conductivity so remarkable.",
        "The 2004 Science paper + the scotch-tape method video",
        ["Physics", "Materials", "2000s"],
    ),
    "disc-the-first-black-hole-133": _entry(
        "Discovery",
        "The first image of a black hole — M87*, the supermassive black hole at the center of galaxy M87, 55 million light-years away — was released on 10 April 2019. It required the Event Horizon Telescope, an array of eight radio observatories spread across the planet acting as one Earth-sized telescope, and it showed the predicted ring of light around darkness.",
        "Look at the image's structure: the bright orange ring is not the black hole itself but the glowing gas falling into it, lensed by gravity into a circle — the 'photon ring.' Then read how the image was made: each observatory recorded petabytes of data, which was physically shipped on hard drives (the internet was too slow) and combined in a computer to synthesize an Earth-sized aperture.",
        "The 2019 M87* image + the EHT data-combining process",
        ["Astronomy", "Black Holes", "2010s"],
    ),
    "disc-chaos-theory-1960s-134": _entry(
        "Theory",
        "Chaos theory was born in 1961 when meteorologist Edward Lorenz rounded a weather-simulation number from 0.506127 to 0.506 and got a completely different forecast — the discovery that tiny changes in initial conditions produce vastly different outcomes. His 1963 paper, 'Deterministic Nonperiodic Flow,' founded the field.",
        "Read Lorenz's own account of the rounding-error moment, then look at his famous 'Lorenz attractor' plots — the butterfly-shaped diagram that shows chaotic systems have hidden order: the paths never repeat, but they always stay on the same strange attractor. That paradox — deterministic but unpredictable — is the field's core, and it's why weather can be forecast days, not months, ahead.",
        "Lorenz's 1963 'Deterministic Nonperiodic Flow' + the attractor plots",
        ["Mathematics", "Chaos", "Weather"],
    ),
    "disc-the-world-wide-web-135": _entry(
        "Invention",
        "Tim Berners-Lee proposed the World Wide Web in March 1989 at CERN — a way to link documents over the internet — and his boss wrote on the cover sheet the most famous memo in computing: 'Vague but exciting.' He built the first browser, server, and webpage (info.cern.ch) by 1991, and gave the technology away rather than patent it.",
        "Read the 1989 proposal ('Information Management: A Proposal') and notice what Berners-Lee got right: the Web was designed for collaboration, with no central control. Then visit the first website's preserved page and read his summary of the project — the Web was conceived as a system for scientists sharing data, and its inventors never imagined shopping, social media, or search engines.",
        "Berners-Lee's 1989 proposal + the preserved first website",
        ["Computing", "Internet", "1980s"],
    ),
    "disc-public-key-cryptography-1976-136": _entry(
        "Invention",
        "Whitfield Diffie and Martin Hellman published public-key cryptography in 1976 — the idea that two strangers can agree on a secret key over an open channel, enabling secure communication without a shared secret. Three years later, Rivest, Shamir, and Adleman built RSA on their foundation, the encryption behind most of the modern internet.",
        "Read the 1976 paper's framing — the 'two-channel cryptography' problem — and understand the core idea: a mathematical function that is easy to compute one way and hard to invert, letting you publish the 'lock' and keep the 'key.' Then read the British twist: GCHQ's James Ellis and Clifford Cocks had invented the same idea years earlier, in secret, which was only declassified in 1997.",
        "Diffie–Hellman 1976 'New Directions in Cryptography' + the GCHQ priority dispute",
        ["Computing", "Cryptography", "1970s"],
    ),
    "disc-deep-learning-neural-networks-137": _entry(
        "Invention",
        "Modern deep learning arrived in 2012 when Alex Krizhevsky, Ilya Sutskever, and Geoffrey Hinton entered AlexNet — a deep neural network trained on GPUs — in the ImageNet competition and halved the error rate of every previous system overnight. The technique had existed since the 1980s; the breakthrough was scale and compute.",
        "Read the AlexNet paper and notice what actually changed: the network had 60 million parameters trained on 1.2 million images over days of GPU time, and its error rate (15.3%) crushed the runner-up's 26.2%. The lesson the field took from it — that bigger networks, more data, and more compute beat cleverer algorithms — set the agenda for the next decade of AI.",
        "The 2012 AlexNet ImageNet paper + the error-rate comparison",
        ["AI", "Neural Networks", "2010s"],
    ),
    "disc-self-driving-cars-2010s-138": _entry(
        "Invention",
        "The modern self-driving era began in 2004 with the DARPA Grand Challenge — a 142-mile desert race in which every vehicle failed, with the best covering only 7 miles. A year later, Stanford's Stanley finished the course, and by 2009 Google had launched its self-driving project (now Waymo) that would log millions of miles of public-road testing.",
        "Watch the 2004 DARPA race footage — the robots crashing, catching fire, and wandering into the desert — and notice what the failure taught the field: the hard problem is not driving but perception under uncertainty. Then read about the 2005 Stanford win and the lidar-based approach Google adopted: the modern stack (lidar + maps + learning) descends directly from those early races.",
        "The 2004 DARPA Grand Challenge + Stanley's 2005 win",
        ["AI", "Robotics", "2010s"],
    ),
    "disc-the-structure-of-the-139": _entry(
        "Discovery",
        "The ribosome — the molecular machine that builds proteins — was mapped at atomic resolution in 2000 by three teams led by Venki Ramakrishnan, Thomas Steitz, and Ada Yonath, who shared the 2009 Nobel. Yonath had spent 20 years crystallizing ribosomes, a project colleagues called impossible; the ribosome is the largest structure ever solved with X-ray crystallography at the time.",
        "Look at a ribbon diagram of the ribosome and notice its two halves — the small subunit that reads the genetic code and the large subunit that stitches amino acids together. Then read about Yonath's perseverance: she crystallized ribosomes from heat-loving bacteria that survive in hot springs, because their ribosomes are robust enough to form crystals.",
        "The 2000 ribosome structures — the Nobel-winning crystallography",
        ["Biology", "Molecular Biology", "2000s"],
    ),
    "disc-apoptosis-1972-140": _entry(
        "Discovery",
        "Apoptosis — programmed cell death — was named in 1972 by John Kerr, Andrew Wyllie, and Alastair Currie, who realized that cells have a built-in suicide program: your body kills about 50 billion cells every day as a routine part of life. The word is Greek for 'falling off,' as leaves from a tree, and cancer is partly a failure of this program.",
        "Read the 1972 paper where the word was coined — Kerr had first described the cell-death pattern in 1965 while studying liver cells — and notice the authors' insight: this death was not injury but choreography. Then read about the C. elegans work (Horvitz, Sulston, Brenner, Nobel 2002) that found the genes running the program: the worm's fixed cell lineage made the suicide genes visible.",
        "The 1972 Kerr–Wyllie–Currie paper + the C. elegans cell-lineage work",
        ["Biology", "Cell Biology", "1970s"],
    ),
    "disc-the-first-clone-dolly-141": _entry(
        "Discovery",
        "Dolly the sheep, born 5 July 1996 at the Roslin Institute, was the first mammal cloned from an adult cell — created by transferring the nucleus of a mammary-gland cell into an egg. Named after Dolly Parton (because the donor cell came from a mammary gland), she was announced in 1997 and immediately became the most famous animal in science.",
        "Read the 1997 Nature paper and notice what the media missed: the cloning had a success rate of about 1 in 277 attempts, and Dolly was the only lamb born from 29 embryos that survived. Then read the follow-up: Dolly developed arthritis and lung disease and was euthanized in 2003, and her early death fueled the debate about whether clones age prematurely — a question still not fully settled.",
        "Wilmut's 1997 Nature paper on Dolly + her health timeline",
        ["Biology", "Cloning", "1990s"],
    ),
    "disc-the-microbiome-2010s-142": _entry(
        "Discovery",
        "The human microbiome — the community of ~100 trillion bacteria, fungi, and viruses living in and on your body — was mapped at scale by the Human Microbiome Project, launched in 2007. The bacteria in your gut weigh about as much as your brain, outnumber your own cells (though the ratio is closer to 1:1 than the old 10:1 claim), and are implicated in everything from digestion to mood.",
        "Read the Human Microbiome Project's results and notice the surprise: each person's microbiome is as individual as a fingerprint, and the same 'species' differ between people at the strain level. Then read the gut-brain research carefully — the 'gut feeling' studies are real but the marketing has outpaced the science: most microbiome claims in wellness products are not yet supported by trials.",
        "The Human Microbiome Project results + the gut-brain evidence review",
        ["Biology", "Microbiome", "2010s"],
    ),
    "disc-the-ozone-layer-recovery-143": _entry(
        "Phenomenon",
        "The ozone hole over Antarctica — discovered in 1985 by a British team led by Joe Farman, who was initially afraid his instruments were broken — was caused by CFCs, chemicals in spray cans and refrigerants. The 1987 Montreal Protocol banned them, the only UN treaty ratified by every country on Earth, and the hole is now slowly healing, expected to close around 2066.",
        "Read Farman's 1985 Nature paper and notice what made it convincing: the British team's ground measurements showed a 40% ozone loss that matched nothing in the models, and the American satellite data had been discarding the same readings as 'errors.' Then read the Montreal Protocol story: the treaty worked because industry had alternatives ready, and the ozone layer's recovery is the existence proof that global environmental action can succeed.",
        "Farman's 1985 Nature paper + the Montreal Protocol timeline",
        ["Chemistry", "Environment", "Policy"],
    ),
    "disc-the-keeling-curve-1958-144": _entry(
        "Discovery",
        "The Keeling Curve — the longest continuous record of atmospheric CO2 — began in 1958 when Charles David Keeling set up instruments at Mauna Loa, Hawaii, high enough to sample well-mixed air. The curve's sawtooth pattern (plants breathing CO2 in summer, releasing it in winter) rides a steady rise from 315 ppm in 1958 to over 420 ppm today — a 34% increase.",
        "Look at the full curve and separate the two signals: the annual sawtooth is the Earth's breathing, and the rising baseline is fossil-fuel emissions accumulating in the atmosphere. Then read why Keeling chose Mauna Loa: at 3,400 meters in the Pacific, the air is well-mixed and far from local sources, making the record a measure of the whole planet, not one country.",
        "The full Keeling Curve — the sawtooth and the rising baseline",
        ["Climate", "Science", "Measurement"],
    ),
    "disc-the-extinction-of-the-145": _entry(
        "Discovery",
        "In 1980, physicist Luis Alvarez and his geologist son Walter proposed that the dinosaurs were killed by an asteroid: a thin layer of iridium — an element rare on Earth but common in asteroids — at the 66-million-year-old K-Pg boundary in rocks worldwide. The theory was mocked for a decade until the Chicxulub crater, 180 km wide, was found in the Gulf of Mexico in 1991.",
        "Read the 1980 Science paper and notice the evidence chain: iridium at 30 times background levels, found in the exact layer where the dinosaurs vanish, at sites across the globe. Then read the Chicxulub discovery — the crater's age matches the boundary to within 30,000 years, and its size implies an impactor roughly 10 km across. The paper that was dismissed as 'unfalsifiable' is now the standard account.",
        "The 1980 Alvarez paper + the Chicxulub crater discovery",
        ["Geology", "Dinosaurs", "Impact"],
    ),
    "disc-homo-naledi-2013-146": _entry(
        "Discovery",
        "In 2013, cavers squeezed through a 12-centimeter gap into the Rising Star cave in South Africa and found over 1,550 fossil fragments of a new human relative, Homo naledi — a small-brained hominin with hands built for climbing, announced in 2015 by Lee Berger's team. The find raised a radical claim: that naledi may have deliberately buried its dead, a behavior previously thought unique to modern humans.",
        "Read the 2015 eLife papers and notice what makes naledi strange: a brain one-third the size of ours combined with human-like hands and feet, dated to only ~250,000 years ago — potentially coexisting with early Homo sapiens. Then read the contested burial claim (2023): the team argues the remains' arrangement and the cave's depth imply intentional deposition, and the debate over that interpretation is ongoing and fierce.",
        "The 2015 Homo naledi announcements + the burial debate",
        ["Anthropology", "Hominins", "2010s"],
    ),
    "disc-fire-prehistoric-147": _entry(
        "Discovery",
        "Controlled fire is one of humanity's oldest technologies, with the earliest strong evidence — ash, burned bone, and heat-altered tools — at Wonderwerk Cave in South Africa dating to about one million years ago. Cooking was the revolution within the revolution: cooked food is easier to digest, and the 'expensive tissue hypothesis' argues the extra energy allowed our ancestors' brains to grow.",
        "Read the Wonderwerk Cave evidence — the oldest well-accepted hearth, a million years old, in a cave in the Kalahari — and compare it with the alternative claims: some sites in Kenya suggest fire use 1.5 million years ago, but the evidence is contested. Then read the cooking argument: Richard Wrangham's claim that cooked food, not tools or language, is the key innovation that made us human is controversial but illuminating.",
        "The Wonderwerk Cave hearth evidence + the cooking hypothesis",
        ["Anthropology", "Technology", "Prehistory"],
    ),
    "disc-agriculture-10000-bc-148": _entry(
        "Discovery",
        "Agriculture — the domestication of plants and animals — began independently in at least seven regions, with wheat and barley in the Fertile Crescent around 10,000–12,000 years ago, rice in China, maize in Mesoamerica, and potatoes in the Andes. The shift from hunting to farming is called the Neolithic Revolution, and it was not obviously an improvement: early farmers were shorter and sicker than the hunter-gatherers they replaced.",
        "Read the 'Göbekli Tepe problem': the monumental stone temple in Turkey, built ~9600 BC by pre-agricultural people, suggests the ritual impulse came first and farming followed to feed the gatherings — inverting the standard story that farming created civilization. Then weigh the evidence: skeletons show early farmers had more cavities, anemia, and infectious disease than foragers, yet farming won because it fed far more people per acre.",
        "The Göbekli Tepe chronology vs. the standard Neolithic story",
        ["Archaeology", "Agriculture", "Prehistory"],
    ),
    "disc-the-printing-press-1440-149": _entry(
        "Invention",
        "Gutenberg's printing press (c. 1440) combined movable metal type with a wine press, and by 1500 — within 50 years — an estimated 20 million books had been printed across Europe, versus the thousands of manuscripts scribes had produced in the previous century. The first product was the 42-line Bible, and the technology is often credited with making the Reformation possible.",
        "Look at a Gutenberg Bible page and notice what printing changed: identical text in identical type, produced in weeks instead of a year per copy. Then read the numbers of the press's spread — printing shops appeared in 255 cities within 50 years, and the price of books collapsed — and consider the argument that the press's real product was not books but standardization: identical texts made shared arguments possible at continental scale.",
        "The Gutenberg 42-line Bible + the spread of printing 1450–1500",
        ["History", "Printing", "Technology"],
    ),
    "disc-electricity-1800-150": _entry(
        "Discovery",
        "The modern age of electricity began in 1800 when Alessandro Volta built the first battery — the voltaic pile, stacks of zinc and copper discs separated by brine-soaked paper — producing a steady, reliable current for the first time. The discovery grew out of a feud: Volta built it to disprove Galvani's claim that animal tissue generated 'animal electricity.'",
        "Read the story of the Galvani–Volta feud: Galvani saw frog legs twitch when touched with two metals and concluded the electricity came from the animal; Volta proved it came from the two metals and made the frog irrelevant. Then find the earliest consequence: within months of the pile's announcement, scientists used its current to split water into hydrogen and oxygen — electrolysis, the parent of electrochemistry.",
        "Volta's 1800 letter describing the pile + the electrolysis experiments",
        ["Physics", "Electricity", "1800s"],
    ),
    "disc-the-light-bulb-1879-151": _entry(
        "Invention",
        "Thomas Edison's incandescent bulb of 1879 was not the first electric light — but it was the first practical one: a carbon filament in a vacuum that lasted over 1,200 hours. The real invention was the system around it: Edison designed the bulb, the generator, the wiring, and the meter as one package, which is why he, not his predecessors, is remembered.",
        "Read the '1,200 experiments' account — Edison's own tally of filament materials tested — and notice what the story omits: Joseph Swan had demonstrated a working carbon-filament lamp in Britain a year earlier, and the two settled patent disputes by merging. Then read what made Edison's version win: not the bulb itself but the integrated electrical grid he built to sell it.",
        "Edison's 1879 filament work vs. Swan's earlier lamp",
        ["Physics", "Invention", "1870s"],
    ),
    "disc-television-1927-152": _entry(
        "Invention",
        "Philo Farnsworth transmitted the first all-electronic television image on 7 September 1927 — a single straight line, which he celebrated by drawing a dollar sign to show his wife the machine could pay. He was 21, working in a Los Angeles lab funded by investors, and he beat the mechanical scanning systems of John Logie Baird by using an electron beam instead of spinning discs.",
        "Watch the 1927 reconstruction or read Farnsworth's patent description and notice the key move: the 'image dissector' scanned the picture with an electron beam, line by line, with no moving parts — the principle behind every TV since. Then read the corporate tragedy: RCA's David Sarnoff fought Farnsworth's patents for years, and Farnsworth, who sold his company and watched television's rise from a farm in Maine, reportedly told his son, 'There's nothing on it worthwhile.'",
        "Farnsworth's 1927 image dissector + the RCA patent fight",
        ["Technology", "Invention", "1920s"],
    ),
    "disc-the-microprocessor-1971-153": _entry(
        "Invention",
        "Intel's 4004, released in November 1971, was the first microprocessor — an entire computer's CPU on a single chip, with 2,300 transistors, built for a Japanese calculator company that had outgrown its custom chips. The team of three (Federico Faggin, Marcian Hoff, Stan Mazor) created the architecture that every modern computer still descends from.",
        "Read the 4004's spec sheet and notice the scale: 2,300 transistors on a chip smaller than a fingernail, running at 740 kHz — slower than a modern calculator — but it replaced a room of electronics. Then read the business accident: Intel had designed the chip for Busicom, then bought back the rights when the calculator company hit financial trouble, which is why the 'microprocessor' became Intel's product rather than one customer's component.",
        "The Intel 4004's 1971 debut + the Busicom rights buyback",
        ["Computing", "Hardware", "1970s"],
    ),
    "disc-the-search-engine-1996-154": _entry(
        "Invention",
        "Google began in 1996 as 'BackRub,' a Stanford research project by Larry Page and Sergey Brin that ranked web pages by counting links — the insight that a link is a vote. The system, PageRank, was good enough that the pair dropped out of their PhDs to found a company in 1998, and 'googling' entered the language within a few years.",
        "Read the 1998 Stanford paper ('The Anatomy of a Large-Scale Hypertextual Web Search Engine') and notice what was radical: instead of counting keyword matches, PageRank treated the web's link structure as a popularity signal, and it worked on pages the crawler had never seen ranked. Then read how the founders tried to sell the technology — Excite and Yahoo both passed — before raising venture money and running the company from a garage.",
        "The 1998 'Anatomy of a Search Engine' paper + the BackRub origin",
        ["Computing", "Internet", "1990s"],
    ),
    "disc-the-structure-of-dna-155": _entry(
        "Discovery",
        "Watson and Crick published the double helix on 25 April 1953 in a 900-word Nature paper — the most famous understatement in science: 'It has not escaped our notice that the specific pairing we have postulated immediately suggests a possible copying mechanism for the genetic material.' The structure explained heredity at a stroke, and the paper ended by thanking no one for the data it used.",
        "Read the 1953 Nature paper in full — it's short enough to read twice — and notice what it does and doesn't say: the structure is stated as a fact with minimal evidence, and the crucial X-ray image (Photo 51, taken by Rosalind Franklin) is acknowledged only in a final footnote. Then read the 'central dogma' consequence: the base-pairing rules (A-T, C-G) are the copying mechanism, which is why the paper is considered the founding document of molecular biology.",
        "The 25 April 1953 Nature paper — the double-helix announcement",
        ["Biology", "Genetics", "1950s"],
    ),
    "disc-anesthesia-1846-156": _entry(
        "Event",
        "On 16 October 1846, dentist William Morton administered ether to a patient while surgeon John Collins Warren removed a tumor from his neck — and the patient reported no pain. Witness Oliver Wendell Holmes called the moment 'the annihilation of pain,' and the news circled the world within months: surgery without anesthesia had been limited to amputations measured in minutes.",
        "Read the first-person account of 'Ether Day' and notice the stakes: the patient, Gilbert Abbott, was undergoing a procedure that would previously have meant being held down. Then read the grim context that made it urgent — pre-anesthesia surgery was so traumatic that many patients chose death — and the ethics footnote: Morton's demonstration succeeded in part because he had quietly administered ether at the same hospital weeks earlier without permission.",
        "The 16 October 1846 Ether Day demonstration",
        ["Medicine", "Anesthesia", "1840s"],
    ),
    "disc-pasteurization-1864-157": _entry(
        "Invention",
        "Louis Pasteur's pasteurization of 1864 — heating wine and beer to 50–60°C to kill the microbes that spoiled them — was a practical fix for a commercial crisis, and it became the proof of his germ theory. The French wine industry was losing exports to spoilage; Pasteur's process saved it, and the same idea now protects milk worldwide.",
        "Read Pasteur's own account of the experiments and notice the industrial framing: he was solving a business problem (soured wine), not a public-health one, and his heating process was calibrated to kill the spoilage microbes while preserving flavor. Then connect it to the bigger fight: pasteurization was Pasteur's applied proof of the germ theory he was then fighting to establish against the 'spontaneous generation' view.",
        "Pasteur's 1864 wine-spoilage experiments",
        ["Biology", "Food", "1860s"],
    ),
    "disc-x-rays-1895-158": _entry(
        "Discovery",
        "Wilhelm Röntgen discovered X-rays on 8 November 1895 while testing whether cathode rays could pass through cardboard — a screen across the room glowed, and the radiation that caused it passed through everything but bone. He took the first X-ray photo, of his wife's hand, and won the first Nobel Prize in Physics in 1901; doctors were using X-rays within months.",
        "Look at Röntgen's first image — his wife Bertha's hand, the bones and wedding ring visible — and read her reported reaction ('I have seen my death'). Then read the discovery's method: Röntgen worked alone for six weeks in his lab, refused to patent the discovery, and gave his notes and apparatus to the scientific community, which is why X-ray use spread so fast.",
        "Röntgen's first X-ray image of his wife's hand",
        ["Physics", "Imaging", "1890s"],
    ),
    "disc-the-electron-1897-159": _entry(
        "Discovery",
        "J.J. Thomson identified the electron in 1897 by measuring how cathode rays bent in magnetic and electric fields — showing they were particles, and that those particles were lighter than any atom: about 1/1836 the mass of a hydrogen atom. It was the first subatomic particle ever found, and it overturned the idea that atoms were indivisible.",
        "Read the 1897 paper ('Cathode Rays') and follow the measurement: Thomson deflected the rays with electric and magnetic fields, and the balance of the two deflections let him compute the particle's mass-to-charge ratio — far smaller than an ion's. Then read the significance: if the electron came from inside atoms, atoms had structure, and the hunt for that structure (Rutherford's nucleus, 1911) followed directly.",
        "Thomson's 1897 'Cathode Rays' paper — the mass-to-charge measurement",
        ["Physics", "Particles", "1890s"],
    ),
    "disc-relativity-1905-160": _entry(
        "Theory",
        "Einstein's special relativity, published in 1905 — his 'miracle year,' when he also explained the photoelectric effect, Brownian motion, and mass-energy equivalence — showed that space and time are not absolute: moving clocks run slow, moving rulers shrink, and nothing can exceed the speed of light. The equation E=mc², its most famous consequence, was in the same year's papers.",
        "Read the 1905 paper's two postulates — the laws of physics are the same in all inertial frames, and light's speed is constant regardless of the observer's motion — and then work through the consequence Einstein draws: if both hold, simultaneity must be relative, and time itself stretches. Then read the 'clock paradox' resolution: the twin who accelerates ages less, which is not a paradox but the theory's signature prediction, now confirmed by GPS satellites that must correct for it daily.",
        "Einstein's 1905 'Zur Elektrodynamik bewegter Körper' — the two postulates",
        ["Physics", "Relativity", "1900s"],
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
