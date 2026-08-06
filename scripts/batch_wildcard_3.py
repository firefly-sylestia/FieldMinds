#!/usr/bin/env python3
"""Batch: replace wildcard.json fakes #3 — ids 195–232 (Geothermal → Monument Valley).

Same contract as batch_wildcard_1/2.py. Cap 450 (SCHEMA.md).
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
    "wild-icelands-geothermal-springs-195": _entry(
        "Place",
        "Iceland sits on the Mid-Atlantic Ridge, where two tectonic plates pull apart at about 2 cm a year — which is why the island has 600 hot springs and geysers, and why the word 'geyser' itself comes from Icelandic Geysir. Roughly 90% of Icelandic homes are heated by geothermal water.",
        "Look at a map of the Mid-Atlantic Ridge running through Iceland and note the rift valleys, then find the Strokkur geyser — it erupts every 6–10 minutes, unlike its older sibling Geysir which now erupts only after earthquakes. The same heat that powers the geysers runs the country's district heating, an infrastructure choice made since the 1930s.",
        "Strokkur's regular eruptions + the Mid-Atlantic Ridge rift through Iceland",
        ["Iceland", "Geology", "Nature"],
    ),
    "wild-the-international-space-station-196": _entry(
        "Facility",
        "The International Space Station is the largest structure ever put in orbit — about the size of a football field, built by 15 nations and continuously crewed since November 2000. It circles Earth every 90 minutes at 28,000 km/h, and its 8 km of wiring carry enough power (from solar arrays) to run about 40 homes.",
        "Watch a time-lapse of the station transiting the Sun or Moon, then find the Cupola — the seven-window observatory where crews photograph Earth. The station's orbit is the key constraint: it flies at 51.6° inclination to reach Russian launch sites, which is why you can predict exactly when it will pass over your town.",
        "The ISS Cupola + a station solar-transit time-lapse",
        ["Space", "Science", "Engineering"],
    ),
    "wild-the-hubble-telescope-197": _entry(
        "Spacecraft",
        "The Hubble Space Telescope launched in 1990 with a mirror ground to perfection — and then sent back blurry images, because the mirror had been ground to the wrong shape by 2.2 microns. Astronauts fixed it in 1993 with corrective optics, and Hubble has since imaged galaxies 13 billion light-years away from its orbit 540 km up.",
        "Look at the 1990 vs. 1994 images of the same star — the COSTAR fix turned a $1.5 billion paperweight into the most productive observatory in history. Then find the 1995 'Pillars of Creation' image: the pillars are gas columns being eroded by starlight, and the box Hubble pointed at for 34 hours was nearly empty sky.",
        "Hubble's pre/post-COSTAR images + the Pillars of Creation",
        ["Space", "Astronomy", "Science"],
    ),
    "wild-the-curiosity-mars-rover-198": _entry(
        "Spacecraft",
        "NASA's Curiosity rover has been driving on Mars since August 2012 — a one-ton, car-sized laboratory that has covered over 30 km of Gale Crater. Its 2013 discovery that ancient Mars had drinkable lakes and rivers rewrote the search for life: the crater it explores is a dried lakebed at the base of a 5.5 km mountain.",
        "Trace Curiosity's path on the NASA map and notice the stops are chosen by what's visible in the crater walls — each rock layer is a chapter of Martian climate history. Then find the 'selfie' technique: the rover photographs itself by aiming its camera arm and stitching dozens of shots, since no one is there to take the picture.",
        "Curiosity's Gale Crater traverse map + the lakebed discovery layers",
        ["Mars", "Space", "Science"],
    ),
    "wild-apollo-11-moon-landing-199": _entry(
        "Event",
        "On 20 July 1969, Apollo 11 landed humans on the Moon — with roughly 30 seconds of fuel left, after the computer flagged multiple alarms during descent. Neil Armstrong's 'one small step' was broadcast live to an estimated 600 million people, and the astronauts left a plaque: 'We came in peace for all mankind.'",
        "Listen to the actual landing loop: the 1202 and 1201 program alarms that almost caused an abort, and Armstrong taking manual control to steer past a boulder field. Then read the checklist margin note — the 'small step' line was Armstrong's own wording, added on the spot, not a prepared speech.",
        "The Apollo 11 landing audio loop (1202 alarms + manual landing)",
        ["Space", "History", "Exploration"],
    ),
    "wild-the-rosetta-stone-200": _entry(
        "Artifact",
        "The Rosetta Stone, carved in 196 BC, carries the same decree in three scripts — hieroglyphs, demotic, and Greek — which let scholars crack hieroglyphs for the first time. The French found it in 1799 during Napoleon's Egypt campaign, and the British took it in 1801 under the Treaty of Alexandria; Egypt's government has asked for it back repeatedly.",
        "Read the stone's three registers and start with the Greek, which scholars could read — it's a priestly decree praising Ptolemy V. Then find Jean-François Champollion's 1822 breakthrough: he used the cartouche of Ptolemy to identify phonetic hieroglyphs, proving the script wasn't purely symbolic.",
        "The Rosetta Stone's three scripts + Champollion's cartouche method",
        ["Egypt", "Ancient", "Language"],
    ),
    "wild-the-gutenberg-bible-201": _entry(
        "Artifact",
        "Johannes Gutenberg's Bible, printed around 1455 in Mainz, was the first major book made with movable type — about 180 copies, of which 49 survive. Each copy is a two-volume work of 1,282 pages, and the type was cast from a metal alloy, which is the actual invention: metal type could be reused, rearranged, and printed by machine.",
        "Look at a digitized page and notice the two innovations at once: the black text is machine-printed, but the red headings and decorations were added by hand afterward, which is why each copy is unique. Then read why the press mattered: a scribe took a year per Bible; Gutenberg's press could produce the same work in weeks, collapsing the price of a book.",
        "A digitized Gutenberg Bible page — printed text + hand-rubricated initials",
        ["History", "Printing", "Books"],
    ),
    "wild-the-declaration-of-independence-202": _entry(
        "Document",
        "The Declaration of Independence was signed on 4 July 1776 — but the famous large parchment copy wasn't signed until 2 August, and most signatures came later still. Thomas Jefferson's draft originally condemned the slave trade, a passage struck out at the insistence of South Carolina and Georgia delegates.",
        "Read the deleted passage in Jefferson's draft — it calls the slave trade an 'execrable commerce' and blames the king for it — and note who removed it. Then look at the engrossed parchment's signatures: John Hancock's is famously large, 'so King George can read it without his spectacles,' and the print run was ordered by Congress the same day.",
        "Jefferson's deleted anti-slavery passage + the 2 August signing",
        ["History", "USA", "Politics"],
    ),
    "wild-the-universal-declaration-of-203": _entry(
        "Document",
        "Drafted in the shadow of World War II, the Universal Declaration of Human Rights was adopted in 1948 with eight abstentions — Saudi Arabia, South Africa, and the Soviet bloc — and no votes against. Its 30 articles fit in about 1,800 words, and Eleanor Roosevelt, who led the drafting, called it 'a Magna Carta for all mankind.'",
        "Read Article 1 and notice the deliberate omission: the drafters avoided grounding rights in God or nature, choosing 'all human beings are born free and equal in dignity and rights' so that every legal system could sign on. Then read Article 29, the least-cited article, which sets limits: rights carry duties to the community.",
        "The UDHR Articles 1 and 29 — the drafting's secular compromise",
        ["History", "Human Rights", "Politics"],
    ),
    "wild-schrödingers-cat-204": _entry(
        "Concept",
        "Schrödinger's cat was invented in 1935 as a mockery, not a real thought experiment — Erwin Schrödinger meant to show how absurd quantum superposition becomes at the scale of everyday objects. A cat in a box with a poison flask triggered by a radioactive atom is, per quantum rules, neither alive nor dead until someone opens the box.",
        "Read Schrödinger's original 1935 paper passage and notice his tone: he calls the superposition 'quite ridiculous' when applied to a cat. The experiment isn't about cats — it's about when the wavefunction collapses, the deepest open question in quantum mechanics. Compare it with the many-worlds answer, which says the box opens in both outcomes at once.",
        "Schrödinger's 1935 'Die gegenwärtige Situation' — the original cat passage",
        ["Physics", "Quantum", "Philosophy"],
    ),
    "wild-the-mandela-effect-205": _entry(
        "Phenomenon",
        "The Mandela Effect is named after a false memory so widespread that millions of people 'remember' Nelson Mandela dying in prison in the 1980s — he actually died in 2013. Other famous examples: the Berenstain Bears (most recall 'Berenstein'), and the nonexistent monocle on the Monopoly man. Research suggests these are confabulated collective memories, not parallel universes.",
        "Test yourself on the classic list — the Fruit of the Loom cornucopia, 'Life is like a box of chocolates,' the location of New Zealand on a world map — then read the psychology: our brains reconstruct memories from gist, and popular culture (like the misheard line in Forrest Gump) supplies the details. The effect feels supernatural precisely because the reconstruction is invisible.",
        "The Berenstain Bears + Monopoly monocle memory studies",
        ["Psychology", "Memory", "Culture"],
    ),
    "wild-synesthesia-206": _entry(
        "Phenomenon",
        "Synesthesia is a real neurological condition where senses cross: some people see colors when they hear music, others taste shapes or feel numbers in space. Roughly 4% of people have some form, it runs in families, and brain scans show the 'wrong' sensory areas genuinely light up — it's not metaphor or memory.",
        "Look at fMRI comparisons of a synesthete's brain: when they hear a tone, visual cortex regions activate that stay dark in a typical brain. Then try the diagnostic: grapheme-color synesthetes are consistent — ask the same person the color of '5' months apart and they'll give the identical answer, while a non-synesthete guessing won't match even 30% of the time.",
        "The fMRI cross-activation images + the consistency test",
        ["Neuroscience", "Perception", "Psychology"],
    ),
    "wild-the-overview-effect-207": _entry(
        "Phenomenon",
        "Astronauts who see Earth from orbit report the 'overview effect': a shift in perspective where national borders vanish, the planet looks like one fragile system, and everyday conflicts shrink. The term was coined in 1987 by space writer Frank White, and some astronauts describe it as the most profound experience of their lives.",
        "Watch the 2012 short film 'Overview' — astronauts describe the same experience in their own words: no lines on the planet, an atmosphere 'thin as a layer of varnish.' Then read Frank White's original interviews and notice the pattern: the effect is strongest in first-time viewers and is not predicted by their politics or faith.",
        "'Overview' (2012) — the astronaut interviews on seeing Earth whole",
        ["Space", "Psychology", "Philosophy"],
    ),
    "wild-the-golden-ratio-208": _entry(
        "Concept",
        "The golden ratio — 1.618… — is the number where a line divides so that the whole is to the larger part as the larger is to the smaller. Found in Greek geometry (not Greek temples, which don't actually use it), it became a Renaissance aesthetic doctrine, and its association with beauty is largely a 19th-century marketing story — nature's famous 'golden spirals' are approximations at best.",
        "Measure a sunflower or pinecone spiral count and check the ratio of consecutive Fibonacci numbers — they converge on 1.618, which is real. Then read the debunking of 'golden ratio in the Parthenon and the Mona Lisa': the measurements were fitted after the fact. The ratio is genuinely elegant in geometry and genuinely oversold in aesthetics.",
        "The Fibonacci-to-golden-ratio convergence vs. the Parthenon myth",
        ["Mathematics", "Art", "History"],
    ),
    "wild-pi-day-209": _entry(
        "Event",
        "Pi Day — 14 March — became an unofficial holiday in 1988 at San Francisco's Exploratorium, chosen because 3/14 is the first digits of π. It took off after 2009, when the US House passed a resolution encouraging schools to observe it, and 3.14 is also Albert Einstein's birthday. Pi itself has been computed to over 100 trillion digits.",
        "Read the Exploratorium's founding story — physicist Larry Shaw started it with fruit pies on 14 March 1988 — then look at pi's oddest record: the 2022 computation to 100 trillion digits took 157 days of computing. The digits show no pattern anyone has found, which is why the search for a pattern is itself a research field.",
        "The Exploratorium's first Pi Day (1988) + the 100-trillion-digit record",
        ["Mathematics", "Culture", "Science"],
    ),
    "wild-the-hanging-gardens-of-210": _entry(
        "Legend",
        "The Hanging Gardens of Babylon — the only one of the Seven Wonders whose location has never been found — may never have existed in Babylon at all. No Babylonian text mentions them; the only accounts come from Greek writers centuries later, and some historians argue the gardens were actually in Nineveh, where the Assyrian king Sennacherib built a documented 'wonder for all peoples' with water-lifting screws.",
        "Read the two candidate locations and the evidence for each: Babylon has no archaeological trace of gardens or the water machinery described, while Nineveh has both inscriptions and visible aqueducts. Then look at the water problem the Greeks described — lifting enough water for a mountain of gardens — and the Assyrian screw technology that could have done it.",
        "The Babylon-vs-Nineveh evidence for the Hanging Gardens",
        ["Ancient", "Archaeology", "Legend"],
    ),
    "wild-el-dorado-211": _entry(
        "Legend",
        "El Dorado began as a real ritual, not a place: the Muisca people of Colombia covered a new chief in gold dust and offered gold into Lake Guatavita during his investiture. Spanish conquistadors turned that ceremony into a rumor of a city of gold, and three centuries of expeditions — including a 1540s attempt to drain Lake Guatavita — searched for a place that never existed.",
        "Read the original chronicle of the Muisca ceremony, then look at the drainage attempts: in the 1580s, workers cut a notch in the lake's rim and found gold objects in the mud — real artifacts, which is what kept the myth alive. The lesson of El Dorado is that the legend is a translation error: a man became a city became a continent.",
        "The Muisca gold ceremony + the Lake Guatavita drainage attempts",
        ["Colombia", "Legend", "History"],
    ),
    "wild-crop-circles-212": _entry(
        "Phenomenon",
        "Crop circles became a global phenomenon in the 1980s, and the mystery collapsed in 1991 when two Englishmen, Doug Bower and Dave Chorley, demonstrated how they'd made hundreds of circles with a plank and rope. A minority remain unexplained, but none has ever been shown to require anything beyond human construction.",
        "Read the 1991 confession and watch the demonstration video — Bower and Chorley show a simple wooden board and a rope make perfect circles in minutes. Then notice the dates: circle reports surged in the 1980s after media coverage, not before, which is the signature of a manufactured phenomenon.",
        "The Bower–Chorley 1991 demonstration of circle-making",
        ["Mystery", "Hoax", "Culture"],
    ),
    "wild-the-wow-signal-213": _entry(
        "Phenomenon",
        "On 15 August 1977, radio astronomer Jerry Ehman recorded a 72-second signal from the direction of Sagittarius so strong it matched the expected signature of an extraterrestrial transmission — he circled it on the printout and wrote 'Wow!' The signal has never been detected again, and its origin remains the most famous SETI puzzle.",
        "Read the technical details of the Wow! signal: it was at 1420 MHz, the exact frequency hydrogen emits — the frequency SETI researchers argued aliens would use, since it's the universe's most basic beacon. It has been searched for again over 200 times with no repeat, and the leading mundane explanations (a comet, a satellite glitch) each have holes.",
        "The 1977 Wow! signal printout — the 1420 MHz 'hydrogen line'",
        ["Space", "SETI", "Mystery"],
    ),
    "wild-the-dancing-plague-of-214": _entry(
        "Event",
        "In July 1518, in Strasbourg, a woman began dancing in the street and within days dozens joined her — within a month, hundreds danced until they collapsed, and some reportedly danced themselves to death. Authorities responded by building a stage and hiring musicians, which made it worse; the official response of 'dance it out' extended the mania.",
        "Read the contemporary chronicles of the 1518 event — the dancers couldn't stop, and the town council's advice (more dancing) shows how little medical understanding existed. The leading modern explanation is mass psychogenic illness, a contagious stress response, but why the dancing was the symptom — and why it recurs in other eras — is still debated.",
        "The 1518 Strasbourg chronicles + the mass-psychogenic-illness theory",
        ["History", "Medicine", "Mystery"],
    ),
    "wild-the-mary-celeste-215": _entry(
        "Mystery",
        "The Mary Celeste was found in 1872 drifting in the Atlantic with its sails set, cargo intact, and lifeboat missing — its ten crew and the captain's family gone without a trace. The famous 'telltale' details — the unfinished meal, the untouched gold — grew with retelling; the actual evidence is thinner but no less strange.",
        "Read the salvage hearing records and separate fact from the myth: the ship was seaworthy, the cargo was largely intact, and the captain was an experienced sailor with his wife and child aboard. The most credible theories — a faulty alcohol cargo producing fumes, or a misread barometer — explain an evacuation that then went wrong.",
        "The Mary Celeste salvage hearing records (1873)",
        ["Mystery", "Sea", "History"],
    ),
    "wild-the-dyatlov-pass-incident-216": _entry(
        "Mystery",
        "In February 1959, nine experienced Soviet hikers died on a slope of the Ural Mountains, their tent cut open from inside, some found barefoot and in underwear in −25°C cold, with injuries including a crushed skull and a missing tongue. The official investigation closed with a verdict of 'a compelling natural force,' and half a century of theories followed.",
        "Read the 2019 forensic re-analysis, which found the hikers died of hypothermia and blunt trauma consistent with an avalanche — the tent was pitched on a lee slope, and a delayed slab avalanche could have swept the site. The strangeness (missing tongue, radiation traces) has mundane explanations that the original secret-era report suppressed.",
        "The 2019 forensic re-analysis of the Dyatlov Pass deaths",
        ["Mystery", "Russia", "Forensics"],
    ),
    "wild-db-cooper-217": _entry(
        "Mystery",
        "On 24 November 1971, a man calling himself Dan Cooper bought a one-way ticket from Portland to Seattle, claimed a bomb, collected $200,000, and parachuted from the plane's rear stairs into the night — never to be found. It remains the only unsolved skyjacking in US history, and in 1980 a child found a bundle of the cash, rotting, on a Columbia River sandbar.",
        "Read the FBI file's key details: Cooper jumped over southwest Washington in a rainstorm wearing a business suit, and the $5,800 in recovered bills (out of $200,000) is the only physical trace ever found. The 2016 FBI decision to close the case after 45 years says the bureau used its best evidence and found no usable suspect — the file is now public.",
        "The FBI's D.B. Cooper file — the 1980 money find and case closure",
        ["Mystery", "Crime", "USA"],
    ),
    "wild-the-tamam-shud-case-218": _entry(
        "Mystery",
        "The Somerton Man's case (1948) is named for the scrap of paper in his pocket reading 'Tamám Shud' — 'It is ended' — torn from a rare edition of Omar Khayyám's Rubaiyat. Matching the scrap led police to a copy of the book left in a parked car, with a telephone number and a cipher in its back cover that remains unsolved.",
        "Read how the book was traced: the phrase appears at the very end of the Rubaiyat's 11th-century Persian original, and finding the exact edition let police identify the owner's circle. Then look at the cipher — 11 lines of letters and symbols — which has resisted cryptographers for 70 years, and the 2021 genealogical identification of the man himself.",
        "The Rubaiyat's 'Tamám Shud' page + the back-cover cipher",
        ["Mystery", "Australia", "Cipher"],
    ),
    "wild-the-isdal-woman-219": _entry(
        "Mystery",
        "In 1970, a woman's body was found in a gully near Bergen, Norway, with all labels removed from her clothes, burns on her fingertips, and a stash of cash in a case. Her identity remained unknown for 47 years — she was finally identified in 2016 as a Czech woman who had traveled Europe as a tourist, and her death is still unexplained.",
        "Read the case's forensic details: the labels were cut from every garment, her dental work was expensive but untraceable, and the police released an e-fit that drew thousands of tips — none correct. The 2016 identification came from a genealogist's hunch and a DNA match, but the reason she was in that gully was never established.",
        "The Isdal Woman case file — the 2016 DNA identification",
        ["Mystery", "Norway", "Forensics"],
    ),
    "wild-savants-220": _entry(
        "Phenomenon",
        "Savant syndrome — remarkable ability in a narrow domain, often memory, music, or calendar calculation, alongside significant disability — was described by physician Darold Treffert, who studied Kim Peek, the man who inspired 'Rain Man.' Peek could read two book pages at once and memorize thousands of books, yet couldn't button his own shirt.",
        "Read about Peek's abilities and limits together: he read with both eyes on facing pages and retained essentially everything, but his cognitive profile was uneven enough that he needed help with everyday tasks. Then compare with 'acquired savant' cases — people who developed abilities after brain injury, which is why savantism is a window into how the brain reorganizes.",
        "Kim Peek's dual-page reading + Treffert's savant studies",
        ["Psychology", "Memory", "Neuroscience"],
    ),
    "wild-synesthetes-221": _entry(
        "Phenomenon",
        "Synesthetes — people with synesthesia — often report the condition runs in families, and it appears roughly 4% of people have some form. Famous synesthetes include composers who saw color in keys: Rimsky-Korsakov described C major as white, E major as 'blue, sapphire.' The condition is consistent, involuntary, and usually pleasant.",
        "Read Rimsky-Korsakov's own description of key colors and compare it with other musicians' reported palettes — the associations differ from person to person, which is evidence they're not cultural. Then look at the heritability research: synesthesia clusters in families, and genetic studies have linked it to regions involved in cross-modal wiring.",
        "Rimsky-Korsakov's key-colors + the heritability studies",
        ["Neuroscience", "Perception", "Psychology"],
    ),
    "wild-the-sedlec-ossuary-222": _entry(
        "Site",
        "The Sedlec Ossuary in the Czech Republic is a chapel decorated with the bones of an estimated 40,000 to 70,000 people, arranged in the 1870s by a woodcarver named František Rint — including a chandelier made of every bone in the human body. The bones came from the 14th-century Black Death and the Hussite Wars, which filled the church's small cemetery.",
        "Look at Rint's signature, itself built from bones, in the chapel's corner — the woodcarver was commissioned to arrange the disinterred skeletons after the cemetery was dug up for a new church. Then read how the chapel got its bone supply: after the 14th-century plague, the cemetery was famous for its soil, which supposedly made the dead vanish within days.",
        "The bone chandelier + Rint's bone signature, Sedlec Ossuary",
        ["Czechia", "Art", "History"],
    ),
    "wild-aokigahara-forest-223": _entry(
        "Site",
        "Aokigahara, the 'Sea of Trees' at the base of Mount Fuji, is a 35-square-kilometer forest on volcanic ground where the magnetic basalt disorients compasses — which contributed to its reputation as a place to get lost. Its dark fame as a suicide site began with a 1961 novel and grew through media coverage, and Japanese authorities now patrol and post crisis helplines.",
        "Read the forest's geography first: the trees grow from a lava field with caves and uneven ground, and compasses drift on the basalt — a hiker without a map can walk in circles. Then read the history of its reputation: the 1961 novel 'Nami no Tō' and 1993 film coverage created the association, which is why the government's response has been signage and patrols, not secrecy.",
        "The lava-field geography of Aokigahara + the compass anomaly",
        ["Japan", "Forest", "Mystery"],
    ),
    "wild-pripyat-chernobyl-224": _entry(
        "Site",
        "Pripyat, built in 1970 to house Chernobyl's power plant workers, was evacuated in a single day on 27 April 1986 — the day after the reactor explosion — and its 49,000 residents left with what they could carry, expecting to return in three days. The city has stood empty ever since, its Ferris wheel (never opened) a symbol of a future that stopped.",
        "Look at the evacuation order's wording: residents were told to leave for 'two or three days' — the buses collected people in under three hours, and the ferris wheel that was scheduled to open for May Day never turned. Then read the dose map of the 1986 fallout and note how far the contamination spread before anyone outside knew.",
        "The 27 April 1986 evacuation of Pripyat + the fallout map",
        ["Ukraine", "History", "Disaster"],
    ),
    "wild-socotra-island-225": _entry(
        "Place",
        "Socotra, an island in the Arabian Sea off Yemen, is often called the most alien-looking place on Earth: a third of its plant species exist nowhere else, including the dragon's blood tree, whose red resin was traded as medicine and dye for millennia. The island separated from mainland Africa so long ago that it evolved its own miniature ecosystem.",
        "Look at the dragon's blood trees' umbrella canopies — the shape catches fog, channeling moisture to the roots in an almost rainless climate. Then read why the resin was valuable: 'dragon's blood' was used as a varnish, a dye, and a medicine across the ancient world, and the island was a stop on the incense trade routes.",
        "The dragon's blood tree canopies of Socotra",
        ["Yemen", "Island", "Botany"],
    ),
    "wild-tristan-da-cunha-226": _entry(
        "Place",
        "Tristan da Cunha is the most remote inhabited place on Earth: a volcanic island in the South Atlantic, 2,400 km from the nearest continent and with fewer than 250 residents, all descended from a handful of 19th-century settlers. There is no airport — the only way in or out is a six-day ship voyage from South Africa, and the island has one 'town,' Edinburgh of the Seven Seas.",
        "Read how the island's mail works: a ship from Cape Town calls roughly nine times a year, and everything — people, mail, medicine — arrives and leaves on it. Then look at the 1961 volcano evacuation: the entire population was moved to England for two years, then chose to return and rebuild on the island they'd left.",
        "The 1961 volcano evacuation of Tristan da Cunha",
        ["Island", "Geography", "History"],
    ),
    "wild-the-eye-of-the-227": _entry(
        "Place",
        "The Eye of the Sahara — the Richat Structure in Mauritania — is a 40-km-wide ring of rock that looks like a giant eye from space, so striking that early astronauts used it as a landmark. It was long assumed to be a meteorite impact, but geologists now agree it's an eroded geological dome, exposed in concentric rings like the layers of an onion.",
        "Compare the satellite view with the ground photos: the 'eye' is a series of concentric ridges of quartzite and other hard rock, the remnant of a dome pushed up and then eroded flat over millions of years. Then read the earlier impact theory and why the evidence (no shocked quartz, no crater rim) ruled it out.",
        "The Richat Structure from orbit vs. ground-level geology",
        ["Mauritania", "Geology", "Space"],
    ),
    "wild-the-great-blue-hole-228": _entry(
        "Place",
        "The Great Blue Hole off Belize is a 300-meter-wide, 124-meter-deep perfect circle of dark blue in the turquoise reef — a collapsed limestone cave that flooded when sea levels rose. It was made famous by Jacques Cousteau, who dived it in 1971 and declared it one of the top ten dive sites in the world.",
        "Look at the satellite image and read why it's circular: it was a dry cave system whose roof collapsed, and stalactites found at depth prove it was above water during the last ice age, when the sea was about 120 meters lower. The deep layers below 90 meters hold hydrogen sulfide, so dense that divers report a 'milky river' at the boundary — no diving below that line.",
        "The Great Blue Hole's stalactite evidence + the hydrogen-sulfide layer",
        ["Belize", "Geology", "Diving"],
    ),
    "wild-salar-de-uyuni-229": _entry(
        "Place",
        "Salar de Uyuni in Bolivia is the world's largest salt flat — 10,582 square kilometers of salt crust, the dried bed of an ancient lake, so flat that it's used to calibrate satellite altimeters. After rain it becomes a mirror, and it holds roughly 10 billion tonnes of salt and the world's largest lithium reserve.",
        "Watch the mirror-season footage: after a few millimeters of rain, the flat becomes a perfect reflective surface, and horizon and sky merge — photographers exploit the effect for the famous 'vanishing' shots. Then read the lithium story: the brine beneath the crust is the richest known lithium deposit, and Bolivia's politics of extracting it have shaped the global battery trade.",
        "The mirror season of Salar de Uyuni + the lithium brine",
        ["Bolivia", "Geology", "Nature"],
    ),
    "wild-zhangjiajie-national-forest-230": _entry(
        "Place",
        "Zhangjiajie National Forest Park in China is a forest of hundreds of sandstone pillars up to 200 meters tall, formed by erosion along vertical joints — and it inspired the floating 'Hallelujah Mountains' in the film Avatar. The park also has the world's tallest outdoor elevator, a glass elevator built up the cliff face.",
        "Look at the pillar geometry: the quartz-sandstone columns formed because the rock's vertical fractures channeled rainwater erosion, leaving towers behind — the 'stone forest' is a landscape in slow collapse. Then find the glass elevator and the 430-meter glass bridge: the park's engineering is as vertical as its geology.",
        "The sandstone pillars of Zhangjiajie (the Avatar inspiration)",
        ["China", "Geology", "Nature"],
    ),
    "wild-antelope-canyon-231": _entry(
        "Place",
        "Antelope Canyon in Arizona is a slot canyon so narrow that in places you can touch both walls at once — carved by flash floods over hundreds of thousands of years from Navajo sandstone. Its walls glow orange and pink when sunlight angles in, and the famous 'light beams' appear only at midday, when the sun is nearly overhead and dust hangs in the air.",
        "Look at the canyon from inside, where the light shafts are most visible around noon — the beams are dust and air, not the rock, which is why guides kick up sand to 'create' them for photos. Then read the 1997 flash-flood disaster: eleven hikers died when a storm miles away sent a wall of water through the narrow canyon with no warning.",
        "The midday light beams of Antelope Canyon + the 1997 flood",
        ["USA", "Geology", "Photography"],
    ),
    "wild-monument-valley-232": _entry(
        "Place",
        "Monument Valley's red buttes — some 300 meters tall — are the eroded remnants of sandstone mesas on the Navajo Nation, and the landscape has appeared in more films than any actor: John Ford's westerns, Back to the Future III, and Forrest Gump's cross-country run. The valley is actually a basin, not a valley — the monuments are what's left after the softer rock around them washed away.",
        "Read the Navajo interpretation of the monuments: each butte has a name and a story (the Mittens, for example) in the Diné tradition, and the land is a working ranch, not a park. Then look at a geological cross-section: the monuments are the tips of a former plateau, and the 'valley' floor was once the plateau's surface — you're seeing erosion in place.",
        "The Mittens buttes of Monument Valley — geology + Navajo names",
        ["USA", "Geology", "Cinema"],
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
