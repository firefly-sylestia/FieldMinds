#!/usr/bin/env python3
"""Batch: replace discoveries.json fakes #2 — ids 161–202 (Proton → Vaccination).

Same contract as batch_discoveries_1.py. Cap 450 (SCHEMA.md).
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
    "disc-the-proton-1919-161": _entry(
        "Discovery",
        "Ernest Rutherford identified the proton in 1919 by firing alpha particles at nitrogen and knocking hydrogen nuclei out of the atoms — the first artificial transmutation of one element into another, 5,000 years after the alchemists gave up. He named the hydrogen nucleus 'proton' in 1920, from the Greek for 'first.'",
        "Read the 1919 paper's experiment and notice the elegance: alpha particles (helium nuclei) striking nitrogen produced oxygen and a hydrogen nucleus, which Rutherford recognized as the building block inside every atom. Then read the naming story — Rutherford proposed 'proton' from the Greek 'protos' (first) — and connect it to the model of the atom he was assembling: positive nucleus, electrons outside, the whole thing mostly empty space.",
        "Rutherford's 1919 transmutation experiments",
        ["Physics", "Particles", "1910s"],
    ),
    "disc-nuclear-fission-1938-162": _entry(
        "Discovery",
        "Nuclear fission was discovered in December 1938 when Otto Hahn and Fritz Strassmann bombarded uranium with neutrons and found barium — an atom half uranium's size — a result they could not explain. The explanation came from Lise Meitner, in exile in Sweden, who with her nephew Otto Frisch named the process 'fission' in a letter, and calculated the enormous energy released.",
        "Read the sequence of the discovery and notice who was where: Meitner, Jewish, had fled Berlin months earlier; Hahn wrote to her in Stockholm with the baffling barium result, and she worked out the physics — the uranium nucleus had split, and the mass difference had become energy via E=mc². Then read the Nobel aftermath: Hahn alone received the 1944 prize, and the omission of Meitner is now widely regarded as one of the committee's great injustices.",
        "The 1938 barium experiment + the Meitner–Frisch explanation",
        ["Physics", "Nuclear", "1930s"],
    ),
    "disc-the-laser-1960-163": _entry(
        "Invention",
        "Theodore Maiman built the first working laser on 16 May 1960 at Hughes Research Laboratories — a ruby rod flashed with a photographer's lamp, producing the first beam of coherent light. The press called it 'a solution looking for a problem'; sixty years later, lasers read your discs, cut your metal, measure the Moon, and run fiber-optic internet.",
        "Read Maiman's own account of the breakthrough and notice how it was dismissed: his Nature paper was initially rejected (the editors thought it too sensational), and the field's leaders believed a ruby laser couldn't work because the numbers said the ruby would need too much pumping. Then trace the laser's spread: from the first bar-code scanner (1974) to CD players (1982) to the LIGO gravitational-wave detectors — one technology, a dozen revolutions.",
        "Maiman's 1960 ruby laser + its first applications timeline",
        ["Physics", "Optics", "1960s"],
    ),
    "disc-pulsars-1967-164": _entry(
        "Discovery",
        "Pulsars were discovered in 1967 by Jocelyn Bell Burnell, a 24-year-old graduate student who noticed 'scruff' — a regular pulse of radio waves, 1.3 seconds apart, in her chart recordings. She and her supervisor Anthony Hewish briefly nicknamed the source LGM-1 ('Little Green Men') before identifying it as a rotating neutron star — the collapsed corpse of a massive star, spinning and beaming like a lighthouse.",
        "Read Bell Burnell's account of the discovery and notice the conditions: she was analyzing 120 feet of chart paper per day by eye, and the signal's extreme regularity — so precise it initially looked artificial — is what made her rule out interference. Then read the Nobel controversy: the 1974 prize went to Hewish, her supervisor, and Bell Burnell was excluded — an omission she has since described with remarkable grace, and which remains a reference point in every discussion of scientific credit.",
        "Bell Burnell's 1967 'scruff' discovery + the 1974 Nobel exclusion",
        ["Astronomy", "Neutron Stars", "1960s"],
    ),
    "disc-the-ozone-hole-1985-165": _entry(
        "Discovery",
        "The ozone hole was discovered in 1985 by a small British Antarctic Survey team led by Joe Farman — who initially thought his instruments were broken when they showed a 40% seasonal loss of ozone over Halley Bay. NASA's satellite had seen the same depletion for years but its software had been programmed to discard the readings as impossible errors.",
        "Read Farman's 1985 Nature paper and the dramatic footnote it contains: the measurements had actually begun in the 1970s, and the ozone loss appeared suddenly in the late 1970s — a breakpoint that pointed straight at CFCs. Then read how the discovery's rejection history worked: NASA's automated processing threw out the 'impossible' low readings, which is why the hole was found by a two-man team with a ground instrument, not by the world's most powerful satellite.",
        "Farman's 1985 Nature paper + the NASA data-discarding story",
        ["Chemistry", "Environment", "1980s"],
    ),
    "disc-the-higgs-boson-2012-166": _entry(
        "Discovery",
        "The Higgs boson — the last missing piece of the Standard Model — was discovered on 4 July 2012 at CERN's Large Hadron Collider, ending a search that had run since the particle was predicted in 1964. The LHC had been running for barely two years; the discovery required 3,000 physicists and collisions at 13 trillion electron-volts.",
        "Watch the 4 July 2012 seminar (the one where Peter Higgs, now 83, wiped his glasses and wept) and read the announcement's language: the teams reported a 'new boson' at 125 GeV with 5-sigma confidence — the threshold particle physicists use for 'discovery' instead of 'evidence.' Then read what a 5-sigma standard means: it's the probability of a false alarm at about 1 in 3.5 million, a bar set after a series of famous near-misses in the field.",
        "The 4 July 2012 Higgs seminar + the 5-sigma standard",
        ["Physics", "Particle Physics", "2010s"],
    ),
    "disc-the-big-bang-theory-167": _entry(
        "Theory",
        "The Big Bang theory — that the universe began as an extremely hot, dense point and has been expanding ever since — was first proposed by Belgian priest and physicist Georges Lemaître in 1927 and confirmed by Hubble's 1929 discovery that galaxies are rushing apart. The name was coined dismissively by Fred Hoyle in 1949, and the theory's decisive evidence came in 1964 with the accidental discovery of the cosmic microwave background.",
        "Read the evidence chain in order: Lemaître's 1927 paper (published two years before Hubble's), Hubble's redshift measurements, and then the 1964 discovery by Penzias and Wilson — two radio astronomers who spent a year trying to remove an annoying 'noise' from their antenna before realizing it was the afterglow of the Big Bang itself. Then read the modern picture: the CMB maps the universe at 380,000 years old, and its temperature fluctuations are the seeds of every galaxy.",
        "The 1964 cosmic microwave background discovery + the Hubble expansion",
        ["Astronomy", "Cosmology", "Theory"],
    ),
    "disc-mendelian-genetics-1866-168": _entry(
        "Discovery",
        "Gregor Mendel's pea experiments — 22,000 plants, seven traits, eight years in a monastery garden — established the laws of heredity in 1866, and were then ignored for 35 years until three botanists independently rediscovered them in 1900. Mendel died in 1884 without knowing his work would found the science of genetics; the word 'genetics' wasn't even coined until 1905.",
        "Read Mendel's 1866 paper and notice the mathematics that made it radical: he counted, rather than described, tracking each trait across generations and finding the 3:1 ratio that implied discrete hereditary 'factors' (which we now call genes). Then read the rediscovery story — de Vries, Correns, and Tschermak all cited Mendel within months of each other in 1900 — and consider the unanswered question of why a paper with such clear data was ignored for a generation.",
        "Mendel's 1866 pea experiments + the 1900 rediscovery",
        ["Biology", "Genetics", "1800s"],
    ),
    "disc-the-electron-microscope-1931-169": _entry(
        "Invention",
        "Ernst Ruska built the first electron microscope in 1931, using a beam of electrons — whose wavelength is about 100,000 times shorter than light's — to image objects far below the resolution of any optical microscope. It took the 1986 Nobel Prize, and modern electron microscopes resolve individual atoms; the instrument revealed the viruses and cell structures that light could never show.",
        "Read the physics of the resolution limit first: an optical microscope cannot resolve objects smaller than about half the wavelength of light (~200 nanometers), which is why viruses were invisible until the electron microscope. Then read Ruska's insight: electrons are particles with a wavelength, and a magnetic 'lens' can focus them the way glass focuses light. The first images were of metal grids at 400x magnification; within a decade the instrument was resolving viruses.",
        "Ruska's 1931 electron microscope + the wavelength-resolution comparison",
        ["Physics", "Microscopy", "1930s"],
    ),
    "disc-the-first-antibiotic-1932-170": _entry(
        "Discovery",
        "The first antibacterial drug that could be mass-produced was Prontosil, developed by Gerhard Domagk in 1932 and used from 1935 — a red dye that cured bacterial infections in mice and, famously, Domagk's own daughter. Penicillin had been discovered in 1928 but couldn't be mass-produced until the 1940s; the sulfa drugs were the first to save lives at scale, and Domagk won the 1939 Nobel.",
        "Read the Prontosil story and its twist: the dye itself was inactive — it worked because the body metabolized it into the active sulfanilamide, which is why the later, colorless sulfa drugs worked identically. Then read the timeline against penicillin: Fleming discovered penicillin in 1928 but abandoned it (it was hard to purify); Florey and Chain revived it in 1939 and mass production arrived just in time for World War II, which is why penicillin is remembered and the sulfas are not.",
        "Domagk's 1932 Prontosil + the penicillin timeline",
        ["Medicine", "Antibiotics", "1930s"],
    ),
    "disc-the-structure-of-insulin-171": _entry(
        "Discovery",
        "Dorothy Hodgkin solved the three-dimensional structure of insulin in 1969 — the climax of 35 years of work — having already deciphered penicillin and vitamin B12, for which she won the 1964 Nobel. By the end she was tracing electron-density maps with a magnifying glass, her hands crippled by rheumatoid arthritis; the structure she produced is the one still taught.",
        "Read about the 35-year span and notice what changed along the way: Hodgkin had been working on insulin since 1934, but the structure required better X-ray sources and computers, so her success was timed to the technology's maturity as much as her patience. Then read the outcome: the insulin structure guided the design of synthetic insulin (the first genetically engineered drug, approved 1982), which is the practical payoff of a purely 'curiosity-driven' project.",
        "Hodgkin's 1969 insulin structure + the synthetic insulin outcome",
        ["Biology", "Crystallography", "1960s"],
    ),
    "disc-mri-imaging-1973-172": _entry(
        "Invention",
        "MRI (magnetic resonance imaging) was born in 1973 when Paul Lauterbur published the first cross-sectional NMR image, made by adding magnetic field gradients to nuclear magnetic resonance. The technique images soft tissue without radiation — brain, muscle, cartilage — and the 2003 Nobel went to Lauterbur and Peter Mansfield, in a decision so contentious that Raymond Damadian, who had patented the medical scanner, ran full-page ads protesting his exclusion.",
        "Read Lauterbur's 1973 Nature paper — titled 'Image Formation by Induced Local Interactions: Examples Employing Nuclear Magnetic Resonance' — and notice the modesty of the first image: a pair of water-filled tubes. Then read the method's core trick: the resonance frequency of nuclei depends on the local magnetic field, so by varying the field across the body, the machine turns space into frequency, and a computer sorts the signals back into an image.",
        "Lauterbur's 1973 Nature paper + the Nobel controversy",
        ["Medicine", "Imaging", "1970s"],
    ),
    "disc-the-human-genome-project-173": _entry(
        "Discovery",
        "The Human Genome Project — 13 years, 20 labs, $3 billion, and 3 billion base pairs — produced a complete draft of the human genetic code in 2000 and the finished sequence in 2003. The genome turned out to contain roughly 20,000 protein-coding genes — fewer than a grape — and the project's real legacy is the technology it forced into existence.",
        "Read the 2001 announcement history and notice the race: a private company, Celera, ran a parallel genome project, and the public project published simultaneously to avoid losing the credit. Then read the scientific surprises: the human genome has only ~20,000 genes (estimates before the project ran as high as 100,000), and more than 98% of it is non-coding — a fact that launched the 'junk DNA' debates that sequencing has been resolving ever since.",
        "The 2001 draft sequence announcement + the gene-count surprise",
        ["Biology", "Genomics", "2000s"],
    ),
    "disc-rna-interference-1998-174": _entry(
        "Discovery",
        "RNA interference — the discovery that double-stranded RNA silences genes — was announced by Andrew Fire and Craig Mello in a 1998 Nature paper, and it won the 2006 Nobel. The mechanism turned out to be an ancient immune system: cells use small RNAs to find and destroy foreign or duplicate genetic messages, and scientists can hijack it to switch off any gene on demand.",
        "Read the 1998 Nature paper and notice the unglamorous details: Fire and Mello injected double-stranded RNA into C. elegans worms and watched specific genes go silent — and the double-stranded form worked a hundredfold better than single strands, which is the clue that cracked the mechanism. Then read the technique's impact: RNAi gave biologists a universal 'off switch' for genes, and RNA-based drugs — including the mRNA vaccines' cousins — descend from the machinery it revealed.",
        "Fire & Mello's 1998 Nature paper on RNAi",
        ["Biology", "Genetics", "1990s"],
    ),
    "disc-mrna-vaccines-2020-175": _entry(
        "Invention",
        "The mRNA vaccines that ended the COVID-19 pandemic's worst phase were the product of a 40-year scientific arc: messenger RNA — the molecule cells use to read genes — repurposed as a drug-delivery system. The key work was done in 2005 by Katalin Karikó and Drew Weissman, who solved the problem of mRNA triggering inflammation; they shared the 2023 Nobel.",
        "Read the 2005 Karikó–Weissman paper and notice the drama the award ceremony glossed over: Karikó had been demoted at her university and was a decade into grant rejections when she and Weissman showed that modified nucleosides let mRNA slip past the immune system. Then read the 2020 application: the vaccine instructs cells to make the spike protein, and the 'm' in mRNA is the whole revolution — the message can be rewritten for any virus in weeks.",
        "The 2005 Karikó–Weissman paper + the 2020 vaccine deployment",
        ["Medicine", "Vaccines", "2020s"],
    ),
    "disc-dark-energy-1998-176": _entry(
        "Discovery",
        "Dark energy — the unknown force accelerating the universe's expansion — was inferred in 1998 from distant supernovae that were dimmer than they should be, meaning the expansion isn't slowing as gravity demands but speeding up. It accounts for about 68% of the universe's total content, yet nobody knows what it is; the 2011 Nobel went to the two teams' leaders.",
        "Read the 1998 papers and notice what the discoverers expected to find: both teams designed the experiment to measure deceleration — the universe slowing under gravity — and both were forced by their own data to conclude the opposite. Then read the vacuum-energy explanation: the leading candidate for dark energy is the energy of empty space itself, but quantum calculations of its size are wrong by 120 orders of magnitude, which physicists call the worst prediction in science.",
        "The 1998 accelerating-expansion papers + the vacuum-energy problem",
        ["Astronomy", "Cosmology", "1990s"],
    ),
    "disc-neutrino-oscillations-1998-177": _entry(
        "Discovery",
        "In 1998, the Super-Kamiokande detector in Japan announced that neutrinos — the universe's most ghostly particles, which pass through the Earth by the trillions — change flavor as they travel, a phenomenon called oscillation. The discovery proved neutrinos have mass, contradicting the Standard Model's assumption and forcing a rewrite of physics; it brought the 2015 Nobel to Takaaki Kajita (and, for solar neutrinos, Arthur McDonald).",
        "Read how the measurement works: Super-Kamiokande is a 50,000-ton tank of pure water lined with 13,000 light detectors, built to catch the rare flashes when a neutrino collides with a water molecule. The oscillation signature showed up as a deficit: neutrinos created in the atmosphere as muon-type were arriving as fewer muon-neutrinos than expected, with the deficit growing with distance — the telltale of flavor-switching in flight.",
        "The 1998 Super-Kamiokande announcement + the oscillation method",
        ["Physics", "Particles", "1990s"],
    ),
    "disc-topological-insulators-2007-178": _entry(
        "Discovery",
        "Topological insulators — materials that insulate electricity in their interior while conducting it perfectly along their surfaces — were predicted and observed in the late 2000s, and they showed that the shape of an electron's wavefunction ('topology') can dictate a material's properties as surely as its chemistry. The field's pioneers won the 2016 Nobel, and the materials are candidates for fault-tolerant quantum computers.",
        "Read the concept through the familiar example the papers use: a Möbius strip's twist is a global property you can't remove by local changes — similarly, the 'twist' in an electron's quantum state can't be undone by impurities, which is why the surface conduction is so robust. Then read the experimental confirmation (the 2007 bismuth-antimony experiments): the surface electrons' spin locked to their direction of travel — a signature physicists could measure directly.",
        "The 2007 topological insulator experiments + the Möbius analogy",
        ["Physics", "Materials", "2000s"],
    ),
    "disc-fullerenes-1985-179": _entry(
        "Discovery",
        "Buckminsterfullerene — a molecule of 60 carbon atoms arranged like a soccer ball — was discovered in 1985 by Harold Kroto, Richard Smalley, and Robert Curl, who vaporized graphite with a laser and found the carbon atoms assembling themselves into the ball. They named it after Buckminster Fuller, inventor of the geodesic dome, and won the 1996 Nobel; the discovery opened the field of carbon nanostructures that led to nanotubes and graphene.",
        "Read the discovery story and notice the accident within the discovery: the team was trying to understand carbon chains in space (Kroto's interest) and found instead a 60-atom molecule so stable it shouldn't have formed by chance. The 'aha' came when Smalley realized the soccer-ball structure would be perfectly stable. Then read the aftermath: fullerenes led to carbon nanotubes (1991) and graphene (2004), the materials that define modern nanoscience.",
        "The 1985 laser-vaporization experiment + the C60 structure insight",
        ["Chemistry", "Nanoscience", "1980s"],
    ),
    "disc-the-theory-of-everything-180": _entry(
        "Theory",
        "A 'theory of everything' — one set of equations uniting gravity with quantum mechanics — has been the holy grail of physics for a century, since Einstein spent his last 30 years chasing it and failed. String theory and loop quantum gravity are the leading candidates, but neither has produced a single experimentally confirmed prediction, and the quest is, famously, unfinished.",
        "Read why gravity refuses to fit: quantum mechanics describes the very small with probabilistic waves, general relativity describes the very large with smooth spacetime, and the two formalisms contradict each other at the extremes — inside black holes and at the universe's first instant. Then read the candidates honestly: string theory predicts extra dimensions and has no confirmed experiment; loop quantum gravity is more modest but also untested. The 'theory of everything' is an active research program, not a discovery — which is exactly why it's worth reading about carefully.",
        "The gravity-vs-quantum contradiction + the two leading candidates",
        ["Physics", "Cosmology", "Unified"],
    ),
    "disc-fractals-1975-181": _entry(
        "Discovery",
        "Benoit Mandelbrot coined the word 'fractal' in 1975 for shapes whose complexity repeats at every scale — a coastline measured with a longer ruler gets longer without limit. His 1967 paper, 'How Long Is the Coast of Britain?,' founded the field, and the Mandelbrot set he visualized in 1980 became the most famous mathematical image ever made, its boundary infinitely detailed by a two-line formula.",
        "Read the 1967 coastline paper and follow the paradox: if you measure a coastline with 100 km rulers you get one answer; with 10 km rulers you get a longer one, and the length grows without bound as the rulers shrink — the coast is a fractal, with a dimension between 1 and 2. Then look at the Mandelbrot set and read how it's generated: iterate z = z² + c for every point in the plane; points that escape to infinity are colored, and the boundary between escaping and staying is infinitely complex.",
        "Mandelbrot's 'How Long Is the Coast of Britain?' + the Mandelbrot set",
        ["Mathematics", "Geometry", "1970s"],
    ),
    "disc-the-internet-protocol-1974-182": _entry(
        "Invention",
        "TCP/IP — the protocol pair that lets every computer on Earth talk to every other — was designed by Vint Cerf and Bob Kahn in 1974, in a paper titled 'A Protocol for Packet Network Intercommunication.' The internet's birth is usually dated to 1 January 1983, when the ARPANET switched over to the new protocol in a single coordinated 'flag day.'",
        "Read the 1974 paper and notice the design principle that made it universal: the protocol doesn't care what kind of network carries the packets, so radio, satellite, and wired networks could all interoperate — that's the 'inter' in 'internet.' Then read the 1983 flag-day story: the ARPANET cut over to TCP/IP overnight, with zero tolerance for error, and the network that had connected a few universities became the skeleton of a worldwide system.",
        "The 1974 Cerf–Kahn paper + the 1983 flag day",
        ["Computing", "Internet", "1970s"],
    ),
    "disc-the-smartphone-2007-183": _entry(
        "Invention",
        "The smartphone era is usually dated to 9 January 2007, when Steve Jobs introduced the iPhone — a phone, an iPod, and an internet communicator in one touchscreen device with no keyboard. Smartphones themselves predate it (IBM's Simon, 1992), but the iPhone's app store and capacitive touchscreen defined the form every phone since has copied, and there are now more smartphones on Earth than people with bank accounts.",
        "Watch the 2007 keynote and notice what Jobs emphasized — 'a revolutionary user interface' — and what he didn't mention: there were no apps initially; the App Store arrived a year later and created the ecosystem that made the device a platform. Then read the numbers: by 2016, two billion people had smartphones, and the devices had become the primary camera, map, and bank for most of humanity — a scale no previous technology reached in a decade.",
        "The 9 January 2007 iPhone keynote + the App Store's arrival",
        ["Technology", "Mobile", "2000s"],
    ),
    "disc-alphafold-protein-folding-2020-184": _entry(
        "Discovery",
        "AlphaFold — DeepMind's neural network — cracked protein structure prediction in 2020, solving a problem biologists had chased for 50 years: given a protein's amino-acid sequence, predict the 3D shape it folds into. In the biennial CASP competition it scored near-experimental accuracy, and in 2024 its developers shared the Nobel in Chemistry with protein-design pioneer David Baker.",
        "Read why folding matters: a protein's shape determines what it does, and knowing a protein's structure is the foundation of drug design — but experimental methods (X-ray crystallography, cryo-EM) can take months or years per protein. Then read what AlphaFold actually does: it learned from the database of ~170,000 experimentally solved structures, and its 2022 expansion predicted structures for essentially every known protein — hundreds of millions — collapsing a field's bottleneck from years to minutes.",
        "The 2020 CASP14 results + the 2022 proteome-wide expansion",
        ["Biology", "AI", "2020s"],
    ),
    "disc-reusable-rockets-2015-185": _entry(
        "Invention",
        "On 21 December 2015, SpaceX landed a Falcon 9 rocket booster upright on a landing pad — the first time an orbital-class rocket had flown again after delivering its payload. For six decades, rockets were single-use: every launch meant building a new rocket, which is why spaceflight cost hundreds of millions per flight. Reusability is the change that made satellite constellations and routine launches economically possible.",
        "Read the 2015 landing's history and the failures before it: the first landing attempts exploded at sea, and the company was nearly bankrupt in 2008 — the reusable rocket was a decade-long bet that looked reckless for years. Then read the economics: a Falcon 9's first stage is roughly 70% of the vehicle's cost, so landing it cuts launch prices several-fold, and the resulting price collapse is why thousands of small satellites now orbit instead of a few hundred.",
        "The 21 December 2015 booster landing + the cost-reduction math",
        ["Space", "Rockets", "2010s"],
    ),
    "disc-telomerase-1984-186": _entry(
        "Discovery",
        "Telomerase — the enzyme that lengthens the protective caps (telomeres) on chromosome ends — was discovered in 1984 by Elizabeth Blackburn and Carol Greider, who shared the 2009 Nobel with Jack Szostak. It's the molecule that lets some cells divide forever: active in stem cells and embryos, hijacked by cancers, and absent in most adult cells, which is why our chromosomes shorten as we age.",
        "Read the discovery story and notice the unlikely setting: Greider was a graduate student who ran the experiments over a Christmas holiday, looking for an enzyme that lengthens the six-base repeating 'TTAGGG' sequence at chromosome tips. Then read the two-faced biology: telomerase keeps germ cells and stem cells young, but ~90% of cancers activate it too — which is why the enzyme is simultaneously a longevity lead and a cancer target.",
        "The 1984 telomerase discovery + its role in cancer",
        ["Biology", "Aging", "1980s"],
    ),
    "disc-the-cell-cycle-2001-187": _entry(
        "Discovery",
        "The discovery of the cell cycle's molecular clock — the cyclins and cyclin-dependent kinases that decide when a cell divides — won the 2001 Nobel for Leland Hartwell, Tim Hunt, and Paul Nurse. The machinery they found is so universal that the yeast they studied and human cells run on the same proteins, which is why the cell-cycle research became the foundation of cancer biology.",
        "Read the three contributions as one story: Hartwell's yeast genetics found the 'checkpoints' that gate division, Hunt's sea-urchin experiments found cyclins (proteins that accumulate and then vanish each cycle), and Nurse's work found the kinase that the cyclins switch on. Then read the payoff: when the cell-cycle clock breaks, cells divide out of control — which is cancer — and most cancer drugs in development act on the very proteins this research identified.",
        "The 2001 Nobel work — cyclins, CDKs, and checkpoints",
        ["Biology", "Cell Biology", "2000s"],
    ),
    "disc-optogenetics-2005-188": _entry(
        "Discovery",
        "Optogenetics — controlling brain cells with light — was demonstrated in 2005 by Karl Deisseroth's lab at Stanford, which combined a light-sensitive protein from pond algae (channelrhodopsin) with a way to switch it on in specific neurons. The technique lets researchers turn individual circuits in a living brain on and off with millisecond precision, and it was named Nature Methods' Method of the Year in 2010.",
        "Read the 2005 paper and notice what was actually new: algae had been known to use channelrhodopsin for years, but Deisseroth's team got the gene into mammalian neurons and showed a light pulse could fire them — the first time light controlled a mammalian neuron from the outside. Then read the technique's reach: optogenetics has mapped fear, memory, addiction, and depression circuits in mice, and its medical promise (a blindness trial is underway) descends directly from that algae gene.",
        "The 2005 optogenetics paper + the Method of the Year story",
        ["Neuroscience", "Biology", "2000s"],
    ),
    "disc-the-first-gene-therapy-189": _entry(
        "Discovery",
        "The first approved gene therapy trial began in September 1990, when four-year-old Ashanti DeSilva — born with ADA-SCID, a near-fatal immune deficiency — received her own white blood cells, modified with a working copy of the ADA gene. The treatment worked, and although the early field was set back by a 1999 trial death, gene therapy has since matured into approved drugs for sickle cell disease, blindness, and hemophilia.",
        "Read the 1990 trial's outcome and its caveats: Ashanti's treated cells worked but the effect faded, and she needed ongoing enzyme therapy — the trial proved the concept (gene-corrected cells survive and function) more than it cured. Then read the field's trajectory honestly: the 1999 death of Jesse Gelsinger, in a different trial, halted clinical gene therapy for years; the modern successes (Luxturna for blindness, 2017; Casgevy for sickle cell, 2023) are the payoff of two hard decades of safety engineering.",
        "The 1990 ADA-SCID trial + the 1999 setback and the modern approvals",
        ["Medicine", "Gene Therapy", "1990s"],
    ),
    "disc-climate-change-science-1988-190": _entry(
        "Discovery",
        "Climate change entered the mainstream on 23 June 1988, when NASA scientist James Hansen told the US Senate that 'the greenhouse effect has been detected and is changing our climate now' — testimony delivered during the hottest summer on record, in a Washington gripped by 100°F heat. The same year, the UN created the IPCC, which has since produced the definitive assessments that drive global policy.",
        "Read Hansen's 1988 testimony and notice the audacity: he stated with confidence, before a congressional committee, that the warming signal had emerged from the noise — a claim most colleagues still considered premature. Then read what's happened to his projection: Hansen's models predicted the temperature range we've actually seen, and the 2020s have tracked the upper end of his scenarios. The 1988 hearing is the moment 'global warming' became a policy issue rather than a laboratory one.",
        "Hansen's 23 June 1988 Senate testimony + the IPCC's founding",
        ["Climate", "Science", "Policy"],
    ),
    "disc-ocean-acidification-2000s-191": _entry(
        "Discovery",
        "Ocean acidification — the 'other CO2 problem' — became a recognized global threat in the 2000s: the oceans absorb about a quarter of the CO2 humans emit, and that absorption is acidifying seawater. Surface pH has already dropped by about 0.1 unit since the industrial revolution — a 30% increase in acidity — and the change is fastest in the cold polar seas.",
        "Read the chemistry first: dissolved CO2 forms carbonic acid, which releases hydrogen ions — the more CO2 the ocean absorbs, the more acidic it becomes. The oceans' buffering kept pH nearly stable for millennia, so the current shift is geologically abrupt. Then read the biological consequence that alarms marine biologists: acidified water makes it harder for shellfish, corals, and plankton to build calcium carbonate shells — the base of the marine food web — and lab studies already show pteropods' shells dissolving in Southern Ocean water.",
        "The ocean pH decline + the shell-dissolution studies",
        ["Climate", "Oceans", "2000s"],
    ),
    "disc-lucy-the-australopithecus-1974-192": _entry(
        "Discovery",
        "Lucy — the 3.2-million-year-old Australopithecus afarensis skeleton found in Ethiopia in 1974 — was 40% complete, enough to prove that a human ancestor walked upright over a million years before big brains evolved. She was named after the Beatles' 'Lucy in the Sky with Diamonds,' which the camp played on the night of the discovery, and she remains the most famous fossil ever found.",
        "Read the 1974 discovery account and notice what made Lucy's pelvis so decisive: her knee and pelvis showed bipedal walking, but her brain was chimp-sized — overturning the assumption that big brains came first and walking later. Then read the controversy she still generates: footprints at Laetoli (3.7 million years old) show the same upright gait, but whether Lucy's species climbed trees too remains an active debate, with the latest 2024 reconstructions arguing for significant tree use.",
        "The 1974 Lucy discovery + the Laetoli footprints comparison",
        ["Anthropology", "Hominins", "1970s"],
    ),
    "disc-homo-floresiensis-2003-193": _entry(
        "Discovery",
        "Homo floresiensis — nicknamed 'the hobbit' — was discovered in 2003 in Liang Bua cave on the Indonesian island of Flores: a full adult skeleton standing barely a meter tall, with a chimp-sized brain, dating to as recently as 50,000–60,000 years ago, possibly coexisting with modern humans. The tiny hominin's tools and fire suggest a surprisingly sophisticated culture for its brain size.",
        "Read the discovery and the immediate disbelief: a one-meter-tall hominin with stone tools and fire, living at the same time as Homo sapiens, seemed too good to be true — and some researchers argued the skeleton was a modern human with microcephaly or island dwarfism. Then read where the debate settled: multiple skeletons of the same small size (including a small adult arm bone found in 2014) support the interpretation of a distinct species, and 'island dwarfing' — large animals shrinking on small islands — is a well-documented pattern.",
        "The 2003 Liang Bua discovery + the dwarfing debate",
        ["Anthropology", "Hominins", "2000s"],
    ),
    "disc-the-wheel-3500-bc-194": _entry(
        "Discovery",
        "The wheel was invented in Mesopotamia around 3500 BC — probably a potter's wheel first, with wheeled vehicles following within a few centuries. The Americas never developed the wheel for transport (pre-Columbian wheeled toys exist but no wheeled vehicles), and the wheel's spread was slowed by the absence of roads and suitable draft animals in many regions.",
        "Read the archaeology of the wheel's invention and notice the sequence: the potter's wheel appears in Mesopotamia ~4500 BC, but the first wheels for vehicles (~3500–3200 BC) are solid discs, not spoked — spokes come from the steppe cultures ~2000 BC and make chariots possible. Then read the Americas question: why no wheeled transport? The answer combines absent draft animals (no horses or oxen), mountainous terrain, and no need — which is why the wheel is a great example of a 'good idea' that wasn't inevitable.",
        "The potter's-wheel-to-chariot sequence + the Americas puzzle",
        ["Technology", "History", "Prehistory"],
    ),
    "disc-writing-3200-bc-195": _entry(
        "Discovery",
        "Writing was invented independently in at least three places — cuneiform in Sumer (~3200 BC), hieroglyphs in Egypt, and later in China and Mesoamerica. The first cuneiform tablets were not literature but accounting: records of grain, cattle, and labor, scratched as pictograms into clay. Literature followed centuries later, and the world's first named author, the priestess Enheduanna, wrote hymns around 2300 BC.",
        "Read what the earliest tablets actually say and notice how unglamorous they are: most are receipts and ledgers, which is why scholars argue writing was invented for bureaucracy, not poetry. Then read the transition from pictures to sounds: Sumerian signs came to stand for syllables, and once a script represents sound rather than meaning, it can write any language — the step that made writing a technology rather than an art form. The first 'literature' (Enheduanna's hymns, ~2300 BC) comes centuries after the first receipts.",
        "The earliest cuneiform tablets + the sound-based writing step",
        ["History", "Writing", "Ancient"],
    ),
    "disc-the-steam-engine-1712-196": _entry(
        "Invention",
        "Thomas Newcomen's steam engine of 1712 — the first practical one — pumped water out of English coal mines by condensing steam inside a cylinder to create a vacuum that pulled a piston down. It was crude (it burned coal to pump water out of coal mines) but it worked, and James Watt's separate condenser (1769) made the engine efficient enough to power factories, railways, and eventually the Industrial Revolution.",
        "Read the Newcomen engine's operation and notice the elegant inefficiency: it was an 'atmospheric' engine — steam pushed the piston up, then cold water condensed the steam, and atmospheric pressure pushed the piston back down. Then read the Watt improvement: Watt's separate condenser kept the cylinder hot, roughly quadrupling efficiency, and his engine was the first that could rotate a shaft rather than just pump — the step that let steam drive mills and locomotives instead of only mines.",
        "Newcomen's 1712 engine vs. Watt's 1769 separate condenser",
        ["Technology", "Engineering", "1700s"],
    ),
    "disc-the-telephone-1876-197": _entry(
        "Invention",
        "Alexander Graham Bell was granted US patent 174,465 for the telephone on 7 March 1876 — and filed it just hours before rival Elisha Gray's caveat for a similar device, one of the most contested patent moments in history. Three days later, Bell spoke the first complete sentence over a wire: 'Mr. Watson, come here, I want to see you.'",
        "Read the race between Bell and Gray and notice how close it was: Gray filed a patent caveat (an intent to invent) for a liquid transmitter on the same day Bell filed his patent, and the dispute over who invented the telephone ran through the courts for years — Bell's patent was upheld in 600+ cases. Then read the first sentence's mundane glory: Bell spilled acid on his clothes and called for his assistant, and that accidental sentence became the first words ever transmitted electrically.",
        "The 1876 Bell–Gray race + the first telephone sentence",
        ["Technology", "Invention", "1870s"],
    ),
    "disc-radio-1895-198": _entry(
        "Discovery",
        "Guglielmo Marconi sent the first wireless telegraph signals across his family's estate in Italy in 1895, and in 1901 transmitted the letter 'S' across the Atlantic — the moment radio became transcontinental. The invention's priority is tangled: Nikola Tesla and Russian physicist Alexander Popov both demonstrated wireless transmission around the same time, and Tesla's patents were upheld after Marconi's in 1943.",
        "Read the 1901 transatlantic experiment and notice the conditions: the signal (the letter S in Morse) was received in Newfoundland using a kite-borne antenna, and the feat was partly theater — skeptics noted the signal could have been atmospheric noise. Then read the priority story: Marconi won the 1909 Nobel, but the US Supreme Court, in 1943, ruled that Tesla's earlier patents covered the essential tuned circuits — a decision issued months after Tesla's death.",
        "Marconi's 1901 transatlantic 'S' + the Tesla patent ruling",
        ["Technology", "Wireless", "1890s"],
    ),
    "disc-the-computer-1940s-199": _entry(
        "Discovery",
        "The electronic computer emerged in the 1940s from a wartime race: the Colossus (Britain, 1943, for codebreaking), the ENIAC (US, 1945, for artillery tables), and the stored-program architecture proposed by John von Neumann (1945) that every modern computer still uses. The first programmable electronic computer was arguably the German Z3 (1941), which the war kept secret.",
        "Read the ENIAC's specs and notice the scale: 17,000 vacuum tubes, 30 tons, 150 kilowatts, and programming done by physically rewiring the machine — with women mathematicians, the 'ENIAC Six,' doing the rewiring. Then read the conceptual leap that followed: the stored-program idea — put the instructions in the same memory as the data — is what turned a calculator into a general-purpose machine, and von Neumann's 1945 'First Draft' paper is the blueprint every computer since has followed.",
        "The ENIAC + von Neumann's 1945 stored-program architecture",
        ["Computing", "History", "1940s"],
    ),
    "disc-the-internet-1983-200": _entry(
        "Discovery",
        "The internet — as we define it — was born on 1 January 1983, when the ARPANET switched from its original protocol to TCP/IP in a single coordinated cutover, allowing networks to interconnect. The ARPANET itself had been connecting universities since 1969, but 'internetworking' — networks talking to networks — is what the new protocol made possible, and the first email had been sent a decade earlier, in 1971.",
        "Read the 1983 cutover story and notice what 'flag day' meant: at midnight, every host on the network had to switch protocols simultaneously or be cut off — the internet equivalent of changing every phone number in the world in one night. Then read the timeline that led there: ARPANET (1969), email (1971), the TCP/IP design (1974), and the domain name system (1984), which replaced numeric addresses with names like 'mit.edu' — the piece that made the internet usable by humans rather than engineers.",
        "The 1983 TCP/IP cutover + the 1984 domain name system",
        ["Computing", "Internet", "1980s"],
    ),
    "disc-social-media-2004-201": _entry(
        "Discovery",
        "The modern social media era began with the founding of Facebook in 2004 — 'Thefacebook,' from a Harvard dorm room — which brought social networking to real names and real friend lists at mass scale. Social sites predate it (Friendster 2002, MySpace 2003), but Facebook's news feed (2006) invented the algorithmic stream that every platform since has copied, and the feed is what made social media a new kind of mass medium.",
        "Read the 2004–2006 sequence and notice which innovation was the real turning point: the news feed, introduced in September 2006, replaced the 'visit a profile' model with an algorithmically ranked stream — the first time a platform decided what you saw without being asked. Then read the consequence: the feed's successors (Twitter, Instagram, TikTok) all optimize for engagement, which is the design decision that explains everything from viral misinformation to the attention economy.",
        "The 2006 news feed launch + the engagement-algorithm lineage",
        ["Technology", "Social Media", "2000s"],
    ),
    "disc-vaccination-1796-202": _entry(
        "Discovery",
        "Edward Jenner performed the first vaccination in 1796 by deliberately infecting a boy with cowpox — a mild relative of smallpox — and then showing he was immune to the real disease. The word 'vaccine' comes from vacca, Latin for cow, and Jenner's method — using a related harmless virus to train immunity — is the same principle behind every vaccine since, including the mRNA shots of 2020.",
        "Read the 1798 paper's experiment and notice the audacity: Jenner took pus from a milkmaid's cowpox sores and scratched it into eight-year-old James Phipps's arm, then, six weeks later, exposed the boy to smallpox itself. The 'monstrous' procedure worked, and Jenner's follow-up — over 20 more vaccinations and a refusal to profit from them — turned a folk observation (milkmaids never got smallpox) into a reproducible medical method. Smallpox was declared eradicated in 1980, the only human disease ever eliminated.",
        "Jenner's 1796 experiment + the smallpox eradication of 1980",
        ["Medicine", "Vaccines", "1700s"],
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
