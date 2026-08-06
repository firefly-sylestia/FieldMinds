#!/usr/bin/env python3
"""Batch: replace the first 40 fake wildcard.json entries with real facts.

The entries in FIXES were template-generated (boilerplate teasers like
"The kind of work that rewards patience...", generic instructions, placeholder
tags like Oddity|Historical, and a blanket "Curiosity" subtype). This replaces
teaser + instruction + targetName + tags + subtype (id / categoryId / name /
verb / durationMinutes / tier preserved). Cap is 450 chars per SCHEMA.md.
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


PATH = Path(__file__).resolve().parent.parent / "app/src/main/assets/topics/wildcard.json"


def _entry(subtype: str, teaser: str, instruction: str, target_name: str, tags: list[str]) -> dict:
    return {
        "subtype": subtype,
        "teaser": teaser,
        "instruction": instruction,
        "targetName": target_name,
        "tags": tags,
    }


FIXES: dict[str, dict] = {
    "wild-the-konmari-method-115": _entry(
        "Practice",
        "Marie Kondo's 2011 decluttering method — keep only what 'sparks joy' — turned her into a global brand, and she's also a Shinto priestess. Her system works category by category (clothes first, mementos last), never room by room, because she argues order should be built from the least sentimental things up.",
        "Open 'The Life-Changing Magic of Tidying Up' at the opening chapters and note the order: clothes, books, papers, komono, then mementos. Kondo's core claim is that tidying is a one-time event, not a habit — you do it fully once, then maintain. Test her 'spark joy' test on one drawer and notice how the decision gets easier as you go.",
        "'The Life-Changing Magic of Tidying Up' by Marie Kondo (Part 1, the KonMari method)",
        ["Lifestyle", "Practice", "2010s"],
    ),
    "wild-the-bullet-journal-116": _entry(
        "Practice",
        "The Bullet Journal was invented by Brooklyn designer Ryder Carroll as a coping system for his own ADHD. Its whole grammar fits in one page: rapid logging with bullets (• task, ◦ event, − note), signifiers for priority and migration, and an index that turns a notebook into a searchable database.",
        "Skim Carroll's official guide (bulletjournal.com or the book 'The Bullet Journal Method'). The invention is the migration step: every month you review unfinished tasks and consciously re-copy the ones that still matter — the act of rewriting is the filter. Try a one-week rapid log.",
        "Ryder Carroll's 'The Bullet Journal Method' — the system's origin page",
        ["Lifestyle", "Productivity", "Practice"],
    ),
    "wild-the-slow-movement-117": _entry(
        "Movement",
        "The Slow Movement began as a protest: in 1986, McDonald's opening near Rome's Spanish Steps triggered the slow food movement. Journalist Carl Honoré then broadened it into a philosophy in 2004 — not doing everything slower, but doing everything at the right speed, which he argues is the opposite of laziness.",
        "Read Honoré's 'In Praise of Slowness' chapter on the origin story, then watch how the idea spread from food to cities (Cittaslow), travel, and even sex. The key reframe is that 'slow' isn't a pace — it's a ratio of speed to attention.",
        "'In Praise of Slowness' by Carl Honoré (the opening manifesto)",
        ["Lifestyle", "Philosophy", "Modern"],
    ),
    "wild-the-tiny-house-movement-118": _entry(
        "Movement",
        "Tiny houses are usually under 400 square feet — one-sixth the size of the average American home — and many are built on trailers specifically to dodge building-code minimums. The movement's modern figurehead is Jay Shafer, who built his first 89-square-foot house on wheels in 1999.",
        "Watch a build walkthrough of a trailer-based tiny house and note the legal workaround: wheels make it a 'vehicle,' not a dwelling, so it escapes minimum-square-footage rules. Then compare that against one city that legalized tiny houses on foundations — the zoning fight is the real story.",
        "A tour video of a Jay Shafer-style tiny house on wheels",
        ["Lifestyle", "Housing", "Modern"],
    ),
    "wild-the-voynich-manuscript-119": _entry(
        "Artifact",
        "Carbon-dated to 1404–1438, the Voynich Manuscript is written in a script nobody has ever deciphered, filled with plants that match no known species. It sat in a Jesuit library for centuries before dealer Wilfrid Voynich bought it in 1912 — and it has defied cryptographers, codebreakers, and AI ever since.",
        "Page through the full digitized scans at Yale's Beinecke Library (MS 408). Look at the 'balneological' section of naked women in green baths — the illustrations are as indecipherable as the text. Then read why the leading theory is that it's an elaborate hoax or a lost cipher, and notice what evidence each camp leans on.",
        "The Voynich Manuscript (MS 408), digitized at Yale's Beinecke Library",
        ["Mystery", "Cryptography", "Medieval"],
    ),
    "wild-the-nazca-lines-120": _entry(
        "Site",
        "Etched into Peru's desert between 200 BC and AD 500, the Nazca Lines include figures up to 370 meters long — a hummingbird, a spider, a monkey — that only make sense from the air. The people who made them never flew, which is exactly the mystery: they were laid out with surveying precision using stakes and cord.",
        "Pull up the Nazca Lines on a satellite map and zoom to the hummingbird figure. The shapes were made by scraping away dark surface stones to expose lighter soil — the desert's dryness preserved them for 1,500 years. Compare the line figures with the trapezoids, which some archaeologists read as water-ritual spaces tied to the region's aquifers.",
        "The Nazca Lines on satellite view (hummingbird + spider glyphs)",
        ["Archaeology", "Peru", "Ancient"],
    ),
    "wild-the-terracotta-army-121": _entry(
        "Site",
        "China's first emperor, Qin Shi Huang, built an underground army of over 8,000 life-size terracotta soldiers — every face unique, no two alike — to guard his tomb for eternity. Farmers digging a well stumbled on it in 1974; the emperor's actual burial chamber remains unopened.",
        "Look at the warriors' faces in a high-resolution gallery and count how many distinct facial types you can spot — each soldier was individually modeled, not cast from a mold. Then find the bronze chariots, which were assembled from hundreds of parts, and reflect on what mass production 2,200 years ago must have taken.",
        "The Terracotta Army (Pit 1) — Xi'an, high-resolution museum imagery",
        ["Archaeology", "China", "Ancient"],
    ),
    "wild-angkor-wat-122": _entry(
        "Site",
        "Angkor Wat is the largest religious monument ever built — a 12th-century temple-mountain in Cambodia covering 400 acres, ringed by a moat 190 meters wide. It was built as a Hindu shrine to Vishnu, then slowly became Buddhist, and its name literally means 'city of temples.'",
        "Trace the bas-relief on the outer gallery — nearly 800 meters of carved storytelling, including the Churning of the Sea of Milk, where gods and demons pull a serpent to stir up the elixir of immortality. The temple faces west, unusual for Khmer temples, because it was designed as Vishnu's home.",
        "Angkor Wat's Churning of the Sea of Milk bas-relief, eastern gallery",
        ["Archaeology", "Cambodia", "Khmer"],
    ),
    "wild-easter-island-moai-123": _entry(
        "Site",
        "Nearly 900 moai statues — some 20 tons, the largest 270 tons and never finished — were carved from volcanic tuff on Easter Island between the 13th and 16th centuries. Recent experiments showed they could have 'walked' upright, rocking from side to side, which would explain the islanders' oral tradition that the statues walked.",
        "Watch the 2013 experiment where a replica moai was walked upright by three teams of ropes, then look at the quarry at Rano Raraku where dozens of half-carved statues still sit in the rock. The unfinished ones are the best evidence for how they were made.",
        "The Rano Raraku quarry, where half-carved moai remain in the rock",
        ["Archaeology", "Rapa Nui", "Megalithic"],
    ),
    "wild-chichén-itzá-124": _entry(
        "Site",
        "The Maya pyramid El Castillo at Chichén Itzá has 91 steps on each side plus the platform — 365 total, one for every day of the year. Twice a year, at the equinoxes, the late-afternoon sun makes a serpent of shadow crawl down the balustrade to join a carved snake head at the base.",
        "Watch the equinox shadow-serpent on the northern staircase — the effect is engineered to within days of perfect alignment. Then look at the great ball court, the largest in Mesoamerica, and notice the carved scene showing a player being decapitated: the game's stakes were real.",
        "El Castillo's equinox serpent shadow — Chichén Itzá, Yucatán",
        ["Archaeology", "Maya", "Mesoamerica"],
    ),
    "wild-the-great-wall-of-125": _entry(
        "Site",
        "The Great Wall isn't one wall — it's a 2,000-year patchwork of walls, trenches, and natural barriers stretching over 21,000 km when all dynasties are counted. The myth that it's visible from the moon is false; from low Earth orbit it's visible but barely, and only in the right conditions.",
        "Compare a Ming-dynasty brick section (like Badaling) with the earlier rammed-earth walls on a map — the 'wall' is really a network. Then find the story of the beacon towers: smoke by day, fire by night, signals relayed from tower to tower at up to 700 km/h in ideal conditions.",
        "A Ming-dynasty watchtower section — Badaling vs. the rammed-earth frontier walls",
        ["History", "China", "Architecture"],
    ),
    "wild-the-forbidden-city-126": _entry(
        "Site",
        "Beijing's Forbidden City is a 72-hectare walled palace with 980 surviving buildings, built in 15 years starting 1406 — construction used a million laborers and logs floated from forests 1,000 km away. Legend says it has 9,999 rooms: one short of heaven's 10,000, because only the Jade Emperor could have 10,000.",
        "Walk the central axis from the Meridian Gate to the Hall of Supreme Harmony in a virtual tour and count the courtyards — the design deliberately hides the throne from the entrance. Then look at the roof ridge animals on the Hall of Supreme Harmony: ten beasts, the maximum, reserved for the emperor alone.",
        "The Hall of Supreme Harmony, Forbidden City central axis (virtual tour)",
        ["History", "China", "Architecture"],
    ),
    "wild-sagrada-família-127": _entry(
        "Structure",
        "Antoni Gaudí's Sagrada Família has been under construction since 1882 — longer than the pyramids took to build — with completion now targeted for 2026, the centenary of his death. It's financed entirely by ticket sales and private donations, and Gaudí is buried in the crypt he designed.",
        "Look at the Passion façade — designed by Gaudí but carved in the 1980s from his drawings, in sharp angular contrast to the organic Nativity façade he completed himself. The difference between the two facades is a masterclass in how much of the building is the original architect's versus his successors'.",
        "The Passion vs. Nativity façades of the Sagrada Família, Barcelona",
        ["Architecture", "Spain", "Art Nouveau"],
    ),
    "wild-the-golden-gate-bridge-128": _entry(
        "Structure",
        "When the Golden Gate Bridge opened in 1937 it was the longest suspension span in the world, and its 'International Orange' color was chosen because it's the most visible shade in San Francisco's fog. A safety net beneath the deck saved 19 workers — who formed the 'Halfway to Hell Club.'",
        "Look at archival photos of the 1930s construction: workers riveted the towers without harnesses above the net, and the towers rose from the water with no crane tall enough — they were built as a self-climbing rig. Then find the wind-tunnel test footage that reshaped the deck in 1951, an engineering first.",
        "1930s construction photos of the Golden Gate Bridge towers",
        ["Engineering", "USA", "Architecture"],
    ),
    "wild-the-northern-lights-129": _entry(
        "Phenomenon",
        "The northern lights happen when charged particles from the sun are funneled by Earth's magnetic field into the polar atmosphere — the green glow is oxygen atoms 100 km up re-emitting absorbed energy. Solar activity runs on an 11-year cycle, so aurora years come in waves; the next peak is around 2025.",
        "Find the live aurora forecast and compare the Kp index with a webcam from Tromsø or Iceland — the lights are frequently captured on camera when barely visible to the eye. Then look at a satellite aurora image from above: from space you see the full oval ring, not an arc.",
        "Live aurora webcams from Tromsø, Norway + the Kp-index forecast",
        ["Astronomy", "Geophysics", "Nature"],
    ),
    "wild-victoria-falls-130": _entry(
        "Site",
        "Victoria Falls is neither the tallest nor the widest waterfall, but it's the largest single curtain of falling water on Earth — 1.7 km wide and 108 m tall. The local name, Mosi-oa-Tunya, means 'the smoke that thunders,' from the plume of spray visible 50 km away.",
        "Watch a drone video from the Zambian side at low water, when the falls split into separate chasms and you can see the basalt shelf that shapes them. The gorge below has carved seven successive zig-zag gorges over millions of years — the falls are marching upstream.",
        "Mosi-oa-Tunya — Victoria Falls from the Zambian rim at low water",
        ["Nature", "Africa", "Geology"],
    ),
    "wild-the-great-barrier-reef-131": _entry(
        "Site",
        "The Great Barrier Reef is the largest living structure on Earth — 2,300 km of coral visible from orbit, built by billions of tiny coral animals over thousands of years. It's home to 400+ coral species and 1,500 fish species, and it has lost roughly half its coral cover since 1995.",
        "Look at before-and-after bleaching imagery from the 2016 and 2017 heatwaves: the white 'bleached' corals are alive but starving, having expelled their algae partners. Then find the coral-spawning timelapse — one synchronized night a year, the reef reproduces en masse.",
        "Before/after coral bleaching imagery, Great Barrier Reef Marine Park",
        ["Nature", "Australia", "Marine"],
    ),
    "wild-the-sahara-desert-132": _entry(
        "Place",
        "The Sahara, the largest hot desert on Earth, was green grassland with lakes and crocodiles as recently as 5,000–10,000 years ago — the 'African Humid Period.' Rock art in the desert's caves depicts people swimming, cattle, and giraffes, not camels.",
        "Look at the Tassili n'Ajjer rock art, which shows a lush savanna world — swimming figures and herds — then check a paleoclimate map of northern Africa 8,000 years ago. The desertification that followed is one of the fastest climate shifts in the human record.",
        "Tassili n'Ajjer rock art (swimming + cattle scenes), Sahara",
        ["Geology", "Africa", "Climate"],
    ),
    "wild-yosemite-national-park-133": _entry(
        "Place",
        "Yosemite's El Capitan is the largest exposed granite monolith on Earth — a single 3,000-foot rock face, so sheer that the first climb to its top took 47 days in 1958 and the modern speed record is under 2 hours. The valley's cliffs were carved by glaciers, not rivers.",
        "Watch the classic rope-solo film 'Valley Uprising' clip of the first ascent of the Nose, or find the modern speedrun of the same route. Then look at Half Dome's cut face — the missing half was carried away by a glacier, which is why the summit is a sheer cliff on one side.",
        "El Capitan's Nose route — first ascent (1958) vs. modern speed record",
        ["Nature", "USA", "Geology"],
    ),
    "wild-galápagos-islands-134": _entry(
        "Place",
        "The Galápagos finches Darwin studied in 1835 gave him his first clear evidence of evolution: 13 species with beaks adapted to different foods, all descended from a single mainland ancestor. The islands' giant tortoises live past 100 — Lonesome George, the last of his subspecies, died in 2012 after decades as the world's most famous bachelor.",
        "Compare the beaks of the ground finches and the woodpecker finch — which famously uses a cactus spine as a tool — in a species gallery. Then read the story of Lonesome George: his subspecies was hunted to extinction by whalers and sailors, and conservationists now breed tortoises from surviving relatives.",
        "The 13 Galápagos finch species — beak comparison gallery",
        ["Nature", "Ecuador", "Evolution"],
    ),
    "wild-the-svalbard-global-seed-135": _entry(
        "Site",
        "Buried in a mountain 1,300 km from the North Pole, the Svalbard Global Seed Vault holds over 1.2 million seed samples in permafrost kept at −18°C — a backup for the world's crop diversity. Its nickname, the 'Doomsday Vault,' undersells it: seeds have already been withdrawn once, by Syria's gene bank during its civil war.",
        "Take the virtual tour of the vault's concrete tunnel — it's dug 120 meters into the mountain, designed to stay frozen even without power for 200 years. The Syrian withdrawal in 2015 was the first in its history: seeds of drought-resistant crops were returned to rebuild the Aleppo gene bank.",
        "Svalbard Global Seed Vault virtual tour (entrance tunnel + seed chambers)",
        ["Science", "Norway", "Conservation"],
    ),
    "wild-the-large-hadron-collider-136": _entry(
        "Facility",
        "The Large Hadron Collider is a 27-kilometer ring of superconducting magnets 100 meters under the Swiss-French border, firing protons at 99.9999991% of light speed. In 2012 it produced the Higgs boson — the particle that gives other particles mass — confirming a 48-year-old prediction.",
        "Watch the 2012 announcement footage: the Higgs signal appears as a small bump in the data plots. Then find the accelerator's 'beam dump' explanation — full-power beams carry enough energy to melt a tonne of copper, which is why the machine stops particles into graphite blocks.",
        "The 2012 Higgs boson announcement — CMS/ATLAS data plots",
        ["Physics", "CERN", "Science"],
    ),
    "wild-the-james-webb-space-137": _entry(
        "Spacecraft",
        "The James Webb Space Telescope observes in infrared from a million and a half kilometers beyond Earth, parked at the L2 point. Its 6.5-meter mirror — 18 gold-coated segments — unfolds like origami, and its tennis-court-sized sunshield keeps it at −233°C so its own heat doesn't blind it.",
        "Look at the 'Deep Field' image released July 2022 and compare it with Hubble's — the red smudges are galaxies more than 13 billion light-years away, seen as they were shortly after the Big Bang. Then watch the deployment animation of the mirror segments: each one must align to nanometers, automatically.",
        "The JWST Deep Field vs. Hubble Ultra-Deep Field comparison",
        ["Astronomy", "Space", "Science"],
    ),
    "wild-voyager-1-spacecraft-138": _entry(
        "Spacecraft",
        "Voyager 1, launched in 1977, is the most distant human-made object — over 24 billion km away and still transmitting on 23 watts, roughly the power of a fridge lightbulb. In 1990 it turned back and photographed Earth as the 'pale blue dot' from 6 billion km, at Carl Sagan's urging.",
        "Read Sagan's 'Pale Blue Dot' passage about that photograph — the whole of human history on a pixel. Then look at the Golden Record's cover: engraved instructions (and a pulsar map) for any alien who finds it, including the position of Earth relative to 14 pulsars.",
        "The Pale Blue Dot photograph + the Golden Record cover instructions",
        ["Space", "Exploration", "Science"],
    ),
    "wild-the-dead-sea-scrolls-139": _entry(
        "Artifact",
        "Found in 1947 by a Bedouin shepherd in caves above the Dead Sea, the scrolls pushed the oldest surviving biblical manuscripts back by a thousand years — to 250 BC–AD 68. The Great Isaiah Scroll is 7.3 meters of intact text, and its wording is startlingly close to the Hebrew Bible used today.",
        "Zoom into the Great Isaiah Scroll on the Israel Museum's digital site — you can read the ancient Hebrew letters directly. Then find the Community Rule scroll, which describes a sect's strict communal life; whether those people were the Essenes is still one of archaeology's open arguments.",
        "The Great Isaiah Scroll — digitized in full at the Israel Museum",
        ["Archaeology", "Ancient", "Religion"],
    ),
    "wild-the-bayeux-tapestry-140": _entry(
        "Artifact",
        "Despite its name, the Bayeux Tapestry is embroidery, not tapestry — 70 meters of linen with 58 scenes and 626 figures narrating the Norman conquest of England, stitched within a generation of 1066. Its Latin text even records Halley's comet, which appeared that year and was read as an omen of Harold's doom.",
        "Scroll the full digitized tapestry scene by scene and track the comet panel — stitched 'they marvel at the star' as courtiers point at Halley's comet, which indeed passed in April 1066. Then look at the final, damaged scene: the tapestry ends mid-story, and its last panel is lost.",
        "The full Bayeux Tapestry digitized — the Halley's comet panel",
        ["Medieval", "Art", "History"],
    ),
    "wild-the-magna-carta-141": _entry(
        "Document",
        "In 1215 a group of English barons forced King John to seal a charter limiting royal power — of its 63 clauses, only three remain law in England today, and the rest were mostly about feudal grievances. Four original 1215 copies survive, and it took centuries, not decades, for it to become the 'cornerstone of liberty.'",
        "Read the three clauses still in force: the Church's freedom, the 'lawful judgment of peers' clause, and the one promising no one will be sold or delayed justice. Then trace how the Magna Carta was cited by 17th-century parliamentarians and American colonists — its fame as liberty's cornerstone is largely a later story.",
        "Clauses 1, 39, and 40 of the 1215 Magna Carta",
        ["History", "Law", "Medieval"],
    ),
    "wild-the-constitution-142": _entry(
        "Document",
        "The US Constitution is the world's oldest national constitution still in force, and at about 4,500 words it's shorter than this batch script. It was written in 116 days by 39 men in secret — windows nailed shut — and has been amended only 27 times, the first ten of which were added before it was even two years old.",
        "Read the original text and count how many words it takes before the first substantive rule — the Preamble's 52 words are all mission statement. Then look at Article I, Section 8, the enumerated powers: every federal power the government exercises has to trace back to a clause there or to an amendment.",
        "The original US Constitution text (Article I, Section 8)",
        ["History", "Law", "USA"],
    ),
    "wild-the-turing-test-143": _entry(
        "Concept",
        "Alan Turing proposed his test in 1950 not as a definition of machine intelligence but as a way to sidestep the unanswerable question 'can machines think?' — a human judge chats with a machine and a human, and the machine wins by being indistinguishable. He predicted that by 2000, machines would fool a judge 30% of the time.",
        "Chat with a modern LLM or read transcripts of past Loebner Prize conversations, then judge honestly whether Turing's bar — sustained, unfocused conversation — is easier or harder than passing. The trick of the test is that it measures behavior, not mind: a perfect imitation passes by definition.",
        "Turing's 1950 paper 'Computing Machinery and Intelligence' — the imitation game",
        ["AI", "Philosophy", "Science"],
    ),
    "wild-the-butterfly-effect-144": _entry(
        "Concept",
        "The butterfly effect was born from a rounding error: in 1961 meteorologist Edward Lorenz re-ran a weather simulation with 0.506 instead of 0.506127 and got a completely different forecast. His 1972 talk title asked whether a butterfly flapping in Brazil could set off a tornado in Texas — and chaos theory was named.",
        "Watch the famous double-pendulum or Lorenz attractor simulation — two systems starting a hair apart diverge into entirely different paths. Lorenz's real discovery is in the shapes: the 'attractor' plots show order inside the chaos, which is why weather can be predicted days, not weeks, ahead.",
        "The Lorenz attractor simulation (butterfly-shaped chaos plot)",
        ["Science", "Chaos", "Mathematics"],
    ),
    "wild-the-placebo-effect-145": _entry(
        "Concept",
        "The placebo effect is a genuine biological response, not just 'mind over matter': sham operations — like the knee arthroscopy trials where patients received fake surgery — produced real improvement. The word comes from Latin 'I shall please,' and a negative twin, the nocebo effect, can make inert pills produce real side effects.",
        "Read the sham-surgery trials of the 2000s, where patients who got an incision but no actual procedure reported as much knee-pain relief as those who got the real operation. Then find the color-and-price studies: red placebos work better for pain, and expensive 'placebo' pills outperform cheap ones.",
        "The sham knee-surgery trial — NEJM 2002 results",
        ["Science", "Medicine", "Psychology"],
    ),
    "wild-lucid-dreaming-146": _entry(
        "Phenomenon",
        "In a lucid dream you know you're dreaming while it's happening — roughly half of people have experienced at least one, and a few practice it nightly. In the 1980s Stanford psychologist Stephen LaBerge proved they were real by having dreamers signal from inside the dream with agreed eye movements, visible on an EEG.",
        "Watch LaBerge's original eye-signal footage from the lab — the dreamer 'looks' left-right-left-right on cue while asleep in REM. Then try the simplest induction: reality checks (counting fingers, testing light switches) done during the day, which carry over into dreams as the cue to go lucid.",
        "LaBerge's eye-signaling experiment footage (Stanford, 1980s)",
        ["Psychology", "Sleep", "Science"],
    ),
    "wild-the-uncanny-valley-147": _entry(
        "Concept",
        "Japanese roboticist Masahiro Mori coined 'uncanny valley' in 1970: as robots and animations become more humanlike, our affinity rises — then plunges into revulsion at near-perfect imitation, before recovering at full human fidelity. Mori's original graph plots it with prosthetic hands and corpses as the valley's bottom.",
        "Watch a modern 'uncanny' CGI or android clip, then read Mori's original 1970 paper, translated in IEEE Spectrum — his graph was drawn for robots, but animation studios now design character faces specifically to avoid the valley. The paper ends with a strange twist: Mori suggests the valley's depth depends on whether the thing moves.",
        "Mori's 1970 'Bukimi no Tani' paper — the original uncanny valley graph",
        ["Robotics", "Psychology", "Design"],
    ),
    "wild-fibonacci-sequence-148": _entry(
        "Concept",
        "The Fibonacci sequence — 0, 1, 1, 2, 3, 5, 8… where each number is the sum of the two before — was introduced to Europe by Fibonacci's 1202 book Liber Abaci as a puzzle about breeding rabbits. Indian mathematicians had described the same sequence centuries earlier, and pinecones, sunflowers, and nautilus shells all show its spirals.",
        "Count the spirals on a sunflower or pinecone photo — you'll get two consecutive Fibonacci numbers (like 34 and 55), which is the most efficient packing for seeds. Then read the rabbit problem in Liber Abaci: Fibonacci's rabbits are a fantasy, but the sequence he used to solve it shows up all over nature.",
        "Sunflower head spiral counts + Liber Abaci's rabbit problem",
        ["Mathematics", "Nature", "History"],
    ),
    "wild-the-library-of-alexandria-149": _entry(
        "Site",
        "The Library of Alexandria was antiquity's attempt to collect every book in the world — Ptolemy's agents searched incoming ships and confiscated scrolls, returning only copies. The story of a single catastrophic fire is mostly myth; the library declined over centuries through neglect, war, and politics.",
        "Read how the library actually worked: Ptolemy III's decree that every ship docking in Alexandria surrender its scrolls for copying, and the famous loan of the Athenian state copies of the great tragedians — the originals were never returned. Then separate the three different 'destructions' (Caesar's fire, Aurelian's war, the Serapeum's demolition) from the myth of one great burning.",
        "Ptolemy III's ship-search decree + the history of Alexandria's libraries",
        ["History", "Ancient", "Books"],
    ),
    "wild-atlantis-150": _entry(
        "Legend",
        "Atlantis appears in exactly two texts — Plato's Timaeus and Critias, written around 360 BC — as a cautionary tale of an island empire that sank in a day and night for growing corrupt. Most scholars read it as a philosophical invention: Plato invented a state to contrast with his ideal one.",
        "Read the Atlantis passage in the Timaeus and notice the frame: Plato says the story came from Egyptian priests via Solon — a claim made, conveniently, 9,000 years too far back to verify. Then trace the modern mythology: Atlantis mania began in the 19th century with Ignatius Donnelly, not with Plato.",
        "Plato's Timaeus 24e–25d — the Atlantis passage itself",
        ["Myth", "Philosophy", "Ancient"],
    ),
    "wild-the-bermuda-triangle-151": _entry(
        "Legend",
        "The Bermuda Triangle entered pop culture in 1950s magazine stories built on a handful of unrelated disappearances — the 1945 loss of Flight 19, five torpedo bombers that vanished on a training flight, is the anchor. The US Coast Guard's position: given how many ships and planes cross that stretch of ocean, the record shows no statistical anomaly.",
        "Read the Flight 19 transcript: the pilots were heard getting lost over radio, the flight leader heard saying 'we can't make out anything' — a training accident, not a mystery. Then check the Lloyds of London and Coast Guard statements, which both say loss rates in the Triangle are unremarkable for the traffic volume.",
        "The Flight 19 radio transcripts, 5 December 1945",
        ["Myth", "Aviation", "Ocean"],
    ),
    "wild-the-tunguska-event-152": _entry(
        "Event",
        "On 30 June 1908, something exploded 5–10 km above the Siberian forest with the force of 12+ megatons — flattening 2,150 square kilometers of trees in a radial pattern, with no crater and no meteorite found. The leading explanation is a stony asteroid or comet fragment that airburst before reaching the ground.",
        "Look at the aerial photos of the blast zone: the trees lie in a perfect radial pattern away from the epicenter, like spokes. Then read the witness accounts — even 60 km away, people were knocked off their feet and reported the sky glowing for nights afterward. The 2013 Chelyabinsk meteor, also an airburst, is the modern echo.",
        "The 1927 Kulik expedition photos of the Tunguska blast zone",
        ["Science", "Astronomy", "Mystery"],
    ),
    "wild-the-taos-hum-153": _entry(
        "Phenomenon",
        "Since the early 1990s, some residents of Taos, New Mexico, have heard a persistent low-frequency hum — 30 to 80 Hz — that others in the same room cannot hear at all. Scientific studies measuring the sound could not identify a source, and similar 'hum' reports exist around the world (Windsor, Kokomo, Bristol).",
        "Read the 1993–95 Los Alamos study: sensors recorded a background hum at 50 Hz, but the listeners' reports didn't correlate with it — the instruments found nothing matching what they heard. Then consider the strongest theory: that some people are unusually sensitive to low-frequency infrastructure noise or their own physiological rhythms.",
        "The 1993 Los Alamos Taos Hum study report",
        ["Mystery", "Science", "Sound"],
    ),
    "wild-the-lost-colony-of-154": _entry(
        "Mystery",
        "In 1587, 116 English colonists landed on Roanoke Island; when their governor returned three years later, everyone was gone — the only clue was 'CROATOAN' carved on a post. No remains have ever been conclusively found, though DNA work and archaeology now point to the colonists integrating with nearby tribes.",
        "Read John White's 1590 account of finding the carved word — he intended to sail to Croatoan Island, but a storm forced the ship home and he never returned. Then look at the 21st-century evidence trail: pottery at a site called Site X, and 2019 excavations suggesting a fort may have stood there all along.",
        "John White's 1590 account of the 'CROATOAN' carving",
        ["Mystery", "History", "Colonial"],
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
