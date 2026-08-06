#!/usr/bin/env python3
"""Batch: replace wildcard.json fakes #2 — ids 155–194 (Oak Island → Yellowstone).

Same contract as batch_wildcard_1.py: real teaser, specific targetName,
quality-bar instruction, real tags, proper subtype. Cap 450 (SCHEMA.md).
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
    "wild-the-oak-island-money-155": _entry(
        "Mystery",
        "Treasure hunters have dug on Nova Scotia's Oak Island since 1795, when three teenagers found a depression over a 30-meter shaft layered with oak platforms — the 'Money Pit.' Centuries of attempts have recovered only fragments (a stone with symbols, a slate, a coin), and the 'curse' holds that seven must die before the treasure is found.",
        "Watch a timeline of the pit's excavation attempts and note the pattern: every dig collapses or floods, and each 'treasure' — from the stone inscriptions to the 2014 discoveries — remains undated and unexplained. The 90-foot stone carved with mysterious symbols was taken away in the 1900s and has never been located.",
        "The Oak Island Money Pit excavation timeline (1795–present)",
        ["Mystery", "Treasure", "Canada"],
    ),
    "wild-the-zodiac-killer-156": _entry(
        "Mystery",
        "Between 1968 and 1969 the Zodiac Killer murdered at least five people in the San Francisco Bay Area and taunted police with cryptogram letters signed with a crossed-circle symbol. One cipher, Z408, was cracked within a week by a couple; the harder Z340 took until December 2020 — and still named no suspect.",
        "Decode the famous Z408 letter's key insight yourself: the killer wrote 'I like killing people because it is so much fun' and signed with the zodiac symbol. Then read the 2020 solution of Z340, where codebreakers found the plaintext reading diagonally — the message was an unrepentant boast, and the identity question remains open.",
        "The Zodiac's Z408 cipher letter and its 1969 solution",
        ["Mystery", "Crime", "USA"],
    ),
    "wild-the-somerton-man-157": _entry(
        "Mystery",
        "In December 1948 a well-dressed man was found dead on Somerton Park beach in Adelaide with every label cut from his clothes and no identification. In his pocket: a scrap of paper reading 'Tamám Shud' — Persian for 'It is ended' — torn from a rare copy of the Rubaiyat, inside which was a cipher never fully decoded.",
        "Read the details of the find: the man's dental work was expensive, his fingerprints matched no record, and the Rubaiyat's final cipher remains unsolved. In 2021 genealogists proposed an identity — an Australian electrical engineer — using DNA from his hair, the closest the case has come to closure.",
        "The Tamám Shud case file — the Rubaiyat cipher and the 2021 DNA lead",
        ["Mystery", "Australia", "Forensics"],
    ),
    "wild-the-lead-masks-case-158": _entry(
        "Mystery",
        "In 1966 two Brazilian electricians were found dead on a hill near Rio wearing lead eye-masks — flat gray discs with wire — with a notebook beside them full of instructions about meeting times and 'the agreed place.' Autopsies found no cause of death, and the masks have never been explained.",
        "Look at the scene photographs: the men lay as if sleeping, the masks over their faces, with a bottle of water and a notebook between them. The notebook's final instruction — '16:30 be at the agreed place, 18:30 consume the capsules' — was followed to the letter. Read the theories, from a UFO experiment to an industrial accident, and weigh which evidence each camp ignores.",
        "The 1966 lead masks case — scene photos and the notebook instructions",
        ["Mystery", "Brazil", "Unsolved"],
    ),
    "wild-polyglots-159": _entry(
        "Phenomenon",
        "Hyperpolyglots — people fluent in six or more languages — are so rare that researchers have studied fewer than a few dozen in depth. Emil Krebs, a German diplomat, reportedly worked in 68; Lomb Kató, a Hungarian interpreter, learned languages as a hobby and claimed 10 by immersion, arguing that language learning is a habit, not a gift.",
        "Read Kató's 'Polyglot: How I Learn Languages' — her core technique is the 'deep dive': pick one text, read it repeatedly, and let grammar emerge from context. Then test her method on a short article in a language you don't know, and notice how much structure you can infer from a single paragraph with a dictionary.",
        "Lomb Kató's 'How I Learn Languages' — the immersion method",
        ["Language", "Psychology", "Practice"],
    ),
    "wild-prodigies-160": _entry(
        "Phenomenon",
        "Mozart composed his first symphonies around age 8; Gauss corrected his father's arithmetic at 3; chess prodigy Judit Polgár beat a grandmaster at 15 — but systematic research finds prodigies are extremely rare, and the deeper puzzle is that most prodigies don't become revolutionaries. The ones who change their field are those who eventually break the rules they mastered as children.",
        "Read the psychology literature on 'precocious mastery vs. creative transformation' — the standard finding is that prodigies master existing conventions extraordinarily early, but transformative work usually requires an adult willingness to violate them. Compare Mozart's childhood concertos (technically perfect imitations) with his later operas.",
        "Ellen Winner's research on giftedness and creative transformation",
        ["Psychology", "Talent", "Science"],
    ),
    "wild-the-paris-catacombs-161": _entry(
        "Site",
        "Beneath Paris lie 300 km of tunnels, and a 1.5 km stretch is an ossuary holding the bones of 6–7 million Parisians, relocated in the late 1700s when the city's cemeteries were literally overflowing — Les Halles' cemetery walls had burst. The entrance warns: 'Stop! This is the empire of death.'",
        "Walk the ossuary's arrangement virtually — the bones were stacked into decorative walls of femurs and skulls in the 1780s, and the arrangement is deliberate design, not chaos. Then read why the transfer took decades: churchyards were closed by royal decree after health crises, and the tunnels beneath Paris were already quarry galleries.",
        "The Paris Catacombs ossuary — the femur-and-skull walls",
        ["History", "France", "Architecture"],
    ),
    "wild-the-island-of-the-162": _entry(
        "Site",
        "On a canal island near Mexico City, doll maker Julián Santana Barrera began hanging dolls from trees in the 1950s to appease the spirit of a girl he said drowned in the canal — or so the story goes. He died in 2001, and the island is now a tourist stop where the weather-beaten dolls, some headless, hang among the reeds.",
        "Watch a documentary on the island and separate the legend from the record: Santana's motivations were documented only after his death, and the island was a quiet chinampa farm before the dolls became famous. The eeriness is real, though — decades of sun and rain have decayed the dolls into forms that look more alive than the originals.",
        "The Island of the Dolls — Xochimilco, Mexico City",
        ["Mexico", "Urban Legend", "Art"],
    ),
    "wild-the-winchester-mystery-house-163": _entry(
        "Structure",
        "Sarah Winchester, heiress to the rifle fortune, believed the ghosts of people killed by Winchester rifles demanded she keep building forever to confuse them. From 1884 until her death in 1922, construction ran around the clock — 160 rooms, stairways that lead to ceilings, and doors that open onto walls, all without a single architectural plan.",
        "Tour the house's strangest features: the 'switchback staircase' with 44 steps and 7 flights for a 2.4-meter climb, and the door that opens onto a 3-story drop. Then read the counter-history: some modern researchers argue Sarah was simply a wealthy eccentric who enjoyed hiring carpenters — the ghost story is unverifiable and convenient.",
        "The switchback staircase and the door-to-nowhere — Winchester Mystery House",
        ["Architecture", "USA", "Urban Legend"],
    ),
    "wild-centralia-pennsylvania-164": _entry(
        "Site",
        "In 1962, workers clearing a Centralia, Pennsylvania landfill lit the garbage — and the fire spread down into a coal seam that has been burning ever since. The town's 2,800 residents were bought out by the state; a few holdouts remain, the ground steams, and the abandoned highway's cracked, sinking pavement inspired the Silent Hill games.",
        "Look at photos of Route 61 south of Centralia: the asphalt has heaved and split, with steam curling from the cracks, and warning signs that the road is closed forever. The fire is still spreading underground — estimates range from decades to centuries of remaining burn time.",
        "Route 61, Centralia — the cracked, steaming abandoned highway",
        ["Pennsylvania", "Fire", "Geology"],
    ),
    "wild-hashima-island-165": _entry(
        "Site",
        "Hashima Island, 19 km off Nagasaki, was built around an undersea coal mine and by 1959 had 5,259 residents on a rock 480 m long — the highest population density ever recorded on Earth. The mine closed in 1974 and the island emptied in months, leaving concrete apartments and schools that now look like a ghost of the future.",
        "Look at the island's concrete walls rising directly from the sea — the 'Battleship Island' silhouette. Then read the numbers that made it possible: the reinforced apartment blocks were so dense that the whole island functioned like a single building, with baths, school, and shops stacked inside the seawall. A section of the island appeared in the James Bond film Skyfall.",
        "Hashima Island's sea-wall apartments — 1959 population records",
        ["Japan", "History", "Industrial"],
    ),
    "wild-svalbard-166": _entry(
        "Place",
        "Svalbard, the Norwegian archipelago 1,300 km from the North Pole, is home to about 2,600 people — and more than 3,000 polar bears. Its main town, Longyearbyen, is so far from anywhere that it's illegal to be born or die there: pregnant women fly to the mainland, and the dead are repatriated because the permafrost won't let graves decompose.",
        "Read Longyearbyen's practical rules: doors aren't locked (polar bears), and everyone carries a rifle outside town. Then look at the Global Seed Vault's home mountain — the archipelago's coal-mining past funded the town, and the permafrost is what keeps the vault's seeds cold even without power.",
        "Longyearbyen's no-birth/no-death rule + polar bear safety protocols",
        ["Arctic", "Norway", "Geography"],
    ),
    "wild-the-door-to-hell-167": _entry(
        "Site",
        "In 1971 Soviet geologists drilling for gas in Turkmenistan hit a cavern and the rig collapsed, opening a crater that released methane. To stop the gas, they set it alight — expecting it to burn out in weeks. More than fifty years later, the 'Door to Hell' at Darvaza still blazes day and night, a 70-meter crater of fire in the desert.",
        "Watch night footage of the crater: the flames rise from a pit that has become a tourist landmark, its heat visible on satellite imagery. Read the 2010s debate over whether to extinguish it (Turkmenistan's president ordered it closed, then reversed course) — the fire that was an accident is now a national symbol.",
        "The Darvaza gas crater ('Door to Hell') — night footage",
        ["Turkmenistan", "Geology", "Energy"],
    ),
    "wild-lake-natron-tanzania-168": _entry(
        "Place",
        "Tanzania's Lake Natron is one of the most caustic bodies of water on Earth: pH around 10.5 and up to 60°C, from volcanic soda and salt washing in from the rift valley. Birds and bats that die in it are sometimes calcified by the minerals — but the lake is also the flamingos' most important breeding site, which is the paradox worth sitting with.",
        "Look at the 2013 photographs of animals 'petrified' on the lake's shore — the calcium carbonate encrusts bodies quickly. Then find the flamingo colonies that nest on the salt islands in the middle, where the caustic water keeps predators away. The lake is simultaneously a killer and a nursery.",
        "Lake Natron's calcified birds + the flamingo breeding colonies",
        ["Tanzania", "Geology", "Nature"],
    ),
    "wild-waitomo-glowworm-caves-169": _entry(
        "Place",
        "New Zealand's Waitomo caves are lit by thousands of glowworms — the larvae of a fungus gnat that dangle glowing threads to lure flying insects into sticky snares. The blue-green light is a chemical reaction, and the deeper caves are quiet enough that visitors float through in near-darkness, the ceiling like a starfield.",
        "Look at a long-exposure photo of the glowworm ceiling: each worm is a point of light, but the 'curtains' are the sticky threads below them, invisible until insects hit them. Then read why the glow is brightest when the worm is hungry — the bioluminescence is a hunting advertisement, not decoration.",
        "Long-exposure glowworm ceiling — Waitomo, New Zealand",
        ["New Zealand", "Biology", "Caves"],
    ),
    "wild-pamukkale-turkey-170": _entry(
        "Site",
        "Pamukkale — Turkish for 'cotton castle' — is a staircase of white travertine terraces in western Turkey, built by hot springs that deposit calcium carbonate as they cascade. The Romans built the spa city of Hierapolis on the cliff above it, and people have bathed in the terraces' pools for two thousand years.",
        "Look at the terraces from above and note the geometry: each pool is a self-built basin that overflows into the next, the water temperature around 35°C at the source. Then find the Roman theater and the Plutonium — a cave shrine where priests survived the volcanic CO2 that killed animals, using it as proof of divine protection.",
        "The Pamukkale travertine terraces + Hierapolis above them",
        ["Turkey", "Geology", "Ancient"],
    ),
    "wild-the-wave-arizona-171": _entry(
        "Site",
        "The Wave in Arizona's Coyote Buttes is a 190-million-year-old Jurassic sand dune, compressed into swirling bands of red and white rock that look painted. Only 20 people per day get permits — allocated by lottery — making it one of the hardest places on Earth to legally visit.",
        "Watch a hiker's video of the 4.8 km approach: there is no trail — navigation is by GPS waypoints across bare rock — which is why the lottery system exists. Then look at the rock's layers up close: the striping records ancient dune migrations, and the cross-bedding shows which way the Jurassic winds blew.",
        "The Wave (Coyote Buttes North) — permit lottery + cross-bedding geology",
        ["USA", "Geology", "Hiking"],
    ),
    "wild-bryce-canyon-172": _entry(
        "Site",
        "Bryce Canyon is not a canyon — it's a series of amphitheaters filled with the world's largest concentration of hoodoos, rock spires up to 50 meters tall carved by frost wedging. The Paiute people called them the Legend People, who they say were turned to stone by a trickster god for their pride.",
        "Look at the hoodoo field from Sunrise Point at sunrise — the 'fairy chimneys' change color as the light moves. The mechanism is counterintuitive: it's not wind but ice — water seeps into joints, freezes, expands, and pries the rock apart, erosion working from the top down at about a centimeter per century.",
        "Sunrise Point hoodoo field, Bryce Canyon — frost-wedging explanation",
        ["USA", "Geology", "Nature"],
    ),
    "wild-the-tea-ceremony-173": _entry(
        "Practice",
        "The Japanese tea ceremony (chanoyu) was perfected by Sen no Rikyū in the 16th century, who stripped it down to a tiny room, one bowl, and four principles: harmony, respect, purity, and tranquility. The whole ceremony is designed to be imperfect — wabi-sabi — with a deliberately rough bowl and a doorway so low every guest must bow to enter.",
        "Watch a full ceremony and track the choreography: the host cleans each utensil in a specific order, the guests admire the scroll and flowers before drinking, and the bowl is turned before sipping so the front isn't touched. Rikyū's radical move was abolishing decoration — his teahouse had almost nothing in it.",
        "A full chanoyu demonstration — Sen no Rikyū's four principles",
        ["Japan", "Ritual", "Philosophy"],
    ),
    "wild-kintsugi-174": _entry(
        "Practice",
        "Kintsugi — 'golden joinery' — repairs broken pottery by filling the cracks with lacquer dusted with gold, making the break the most decorated part of the object. The technique is said to date to the 15th-century shogun Ashikaga Yoshimasa, who sent a cracked tea bowl to China for repair and, disliking the metal staples, asked Japanese craftsmen for something better.",
        "Compare a kintsugi-repaired bowl with a conventionally repaired one and notice the philosophy: the repair doesn't hide the damage, it commemorates it — wabi-sabi treats the crack as history rather than flaw. The gold-filled lines become the object's most valuable feature, both literally and aesthetically.",
        "Kintsugi-repaired ceramics — the Ashikaga tea bowl story",
        ["Japan", "Craft", "Philosophy"],
    ),
    "wild-hygge-175": _entry(
        "Concept",
        "Hygge (pronounced 'hoo-guh') is the Danish concept of cozy togetherness — candles, blankets, hot drinks, and low light — and it's a serious piece of national identity for a country with 17-hour winter nights. Danes report among the highest life-satisfaction scores in the world, and hygge is the cultural ritual most cited for it.",
        "Read about hygge's material culture: the Danes burn more candles per capita than anywhere else in Europe, and the word itself descends from a Norwegian word for 'well-being' that predates the modern cozy-brand. The point is scarcity of light and cold — hygge is a winter survival technology that became an aesthetic.",
        "The Hygge Manifesto — Danish candle culture in winter",
        ["Denmark", "Lifestyle", "Culture"],
    ),
    "wild-friluftsliv-176": _entry(
        "Concept",
        "Friluftsliv — 'open-air living' — is the Norwegian conviction that outdoor life is a right and a need, not a leisure activity. The word was coined by Henrik Ibsen in 1859, and it's backed by the allemannsretten, the right to roam: anyone may camp, hike, and forage on uncultivated land, public or private.",
        "Read the allemannsretten's actual limits: you may camp one or two nights on any uncultivated land, but not within 150 meters of a house, and you must leave no trace. Then find Nansen's polar expeditions, which made friluftsliv a national ideal — Norwegians treat the wilderness as a public commons with duties attached.",
        "The allemannsretten (right to roam) — Norway's outdoor-access law",
        ["Norway", "Outdoors", "Culture"],
    ),
    "wild-the-pomodoro-technique-177": _entry(
        "Practice",
        "The Pomodoro Technique was invented by Francesco Cirillo in the 1980s with a kitchen timer shaped like a tomato (pomodoro in Italian): work 25 minutes, break 5, and repeat. The timer isn't a gimmick — it externalizes time, so your brain stops watching the clock and the work becomes the only task.",
        "Run one real pomodoro today and notice what breaks: the 25-minute block is short enough to start, long enough to enter flow, and the enforced 5-minute break is when your mind consolidates. Cirillo's deeper claim is that the technique works by making the 'next action' visible — a broken task list is the actual problem.",
        "Francesco Cirillo's original Pomodoro Technique — 25/5 protocol",
        ["Productivity", "Practice", "Modern"],
    ),
    "wild-the-getting-things-done-178": _entry(
        "Practice",
        "David Allen's Getting Things Done (2001) is built on one observation: your stress doesn't come from having too much to do — it comes from remembering that you have things to do. The system's answer is to capture everything into a trusted external system, then process it into next actions, so your mind is free to work.",
        "Do the full capture step: write down every open loop in your life — including the small ones you've been carrying for weeks — then apply the 2-minute rule: anything doable in under two minutes gets done now. The 'next action' discipline (what, specifically, is the very next physical step?) is the part most people skip, and it's the part that works.",
        "David Allen's 'Getting Things Done' — the capture + next-action method",
        ["Productivity", "Practice", "Modern"],
    ),
    "wild-the-minimalism-movement-179": _entry(
        "Movement",
        "The modern minimalism movement was popularized by Joshua Fields Millburn and Ryan Nicodemus, two Ohio executives who sold their possessions after their careers collapsed and made 'The Minimalists' into a brand — documentaries, podcasts, and tours. Their core claim is not that owning less is virtuous, but that owning less is a tool for making room for what matters.",
        "Watch their documentary 'Minimalism' and separate the two threads: the consumer-critique argument (we fill emotional gaps with purchases) and the aesthetic one (empty rooms look clean). The movement's own critics point out that minimalism is a choice available mostly to the affluent — weigh whether that critique weakens the practice or just narrows it.",
        "'Minimalism' (2016) — the documentary's consumer-critique argument",
        ["Lifestyle", "Philosophy", "Modern"],
    ),
    "wild-the-van-life-movement-180": _entry(
        "Movement",
        "'Van life' became a cultural movement through the #vanlife hashtag, popularized in 2011 by photographer Foster Huntington's Instagram of friends living in vans. What began as a small counterculture of surfers and skiers exploded during the pandemic — by 2020 there were over 10 million hashtagged posts, and the trend reshaped RV sales, remote work, and the National Parks visitation surge.",
        "Read Foster Huntington's original 'A Home Among the Trees' and the early #vanlife posts, then compare them with the 2020s commercial version: the aesthetic is nearly identical, but the economics flipped from freedom to influencer sponsorship. The movement's own mythology — freedom, minimalism, the open road — is now a product category.",
        "The original #vanlife posts (2011) vs. the modern commercial version",
        ["Lifestyle", "Travel", "Modern"],
    ),
    "wild-the-antikythera-mechanism-181": _entry(
        "Artifact",
        "In 1900, sponge divers off the Greek island of Antikythera found a shipwreck 45 meters down — and among the statues and coins, a corroded lump that proved to be a bronze gearbox from around 60 BC. It predicted eclipses, tracked the planets, and timed the Olympic games: a device of that complexity would not appear again in Europe for over a thousand years.",
        "Read the discovery story, then the 2005 X-ray breakthrough that revealed 30+ interlocking gears inside the corrosion. The device's front dial showed the Sun and Moon against the zodiac; the back dial predicted eclipses using the Saros cycle. No other object like it survives from antiquity — which is its own question: was it unique, or the tip of a lost tradition?",
        "The 2005 X-ray tomography of the Antikythera Mechanism's gears",
        ["Ancient", "Greek", "Mechanical"],
    ),
    "wild-machu-picchu-182": _entry(
        "Site",
        "Machu Picchu was built by the Inca emperor Pachacuti around 1450 at 2,430 meters, and abandoned a century later — yet the Spanish never found it. Its 200-plus buildings were fitted without mortar, the stones cut so precisely that a knife blade can't pass between them, and the whole citadel sits on a mountain ridge above a river bend.",
        "Look at the dry-stone ashlar walls — no mortar, no gaps, with stones locked by their angles rather than cement. Then find the Intihuatana stone, whose name means 'hitching post of the sun,' carved so that on the equinoxes the sun sits directly above it at noon — the Inca used the site as a solar observatory as much as a city.",
        "The Intihuatana stone and the mortarless ashlar walls of Machu Picchu",
        ["Peru", "Inca", "Archaeology"],
    ),
    "wild-petra-183": _entry(
        "Site",
        "Petra, the Nabataean capital carved into rose-red sandstone cliffs in Jordan, was a wealthy trading city by 100 BC — yet it had no natural water source. The Nabataeans solved this with an invisible engineering system: channels, cisterns, and clay pipes that captured every drop of the region's rare rain, letting 30,000 people live in a desert.",
        "Follow the water: enter through the Siq, the 1.2 km canyon whose entrance is barely visible — that hiding made the city practically unassailable. Then find the channel carved along the Siq's wall that carried water from springs kilometers away, and the cisterns under the Treasury. The city was 'rediscovered' by Europeans in 1812, but the Bedouin had always known it was there.",
        "The Siq's water channel and the Treasury — Petra, Jordan",
        ["Jordan", "Nabataean", "Archaeology"],
    ),
    "wild-the-great-pyramid-of-184": _entry(
        "Site",
        "The Great Pyramid of Giza, built for Pharaoh Khufu around 2560 BC, is made of roughly 2.3 million blocks averaging 2.5 tons — about 6 million tonnes of stone, the only one of the Seven Wonders of the ancient world still standing. Its original casing of polished limestone would have made it shine like a mirror, and it was the tallest building on Earth for 3,800 years.",
        "Look at the pyramid's core blocks near the base, then compare with a surviving casing stone at the base or in the British Museum — the casing was stripped in the Middle Ages for Cairo's buildings. The precision is the wonder: the base is square to within 5.5 cm over 230 meters, and the four sides align to the cardinal points within a fraction of a degree.",
        "The Great Pyramid's base alignment + surviving casing stones",
        ["Egypt", "Ancient", "Architecture"],
    ),
    "wild-the-colosseum-185": _entry(
        "Site",
        "Rome's Colosseum, completed in AD 80, held 50,000 to 80,000 spectators who entered through 80 numbered arches and were out of the building in minutes — crowd engineering that stadiums still copy. Under the arena floor was a five-story hypogeum of cages, ramps, and elevators that raised gladiators and animals into the light as if from nowhere.",
        "Look at a cross-section of the hypogeum — the trapdoors and lifts below the sand floor, operated by a system of pulleys and counterweights. Then count the arches and imagine the ticketing: 80 entrances, each with a number, letting a crowd the size of a modern soccer stadium enter and exit without a crush.",
        "The Colosseum hypogeum cross-section — the arena's underground machinery",
        ["Rome", "Ancient", "Architecture"],
    ),
    "wild-the-taj-mahal-186": _entry(
        "Structure",
        "The Taj Mahal was built by Mughal emperor Shah Jahan for his wife Mumtaz Mahal, who died in childbirth in 1631 — the mausoleum took 22 years and some 20,000 artisans. Its white marble appears to change color through the day, and the four minarets lean slightly outward: if they fell, they'd fall away from the tomb.",
        "Read the design logic: the minarets' outward tilt is structural insurance, and the marble's color shift comes from inlaid semi-precious stone catching different light. Then find the calligraphy bands on the gate — the letters are graded in size so the whole inscription appears uniform from ground level, an optical illusion built into the stone.",
        "The Taj Mahal's minaret tilt + graded calligraphy on the great gate",
        ["India", "Mughal", "Architecture"],
    ),
    "wild-mont-saint-michel-187": _entry(
        "Site",
        "Mont Saint-Michel is a granite island off Normandy that becomes inaccessible twice a day when the tide — among the fastest in Europe — rushes in faster than a horse can gallop, rising up to 15 meters. The abbey on top was begun in the 8th century after the archangel Michael appeared to a bishop in a vision.",
        "Read the tide tables and watch a time-lapse of the flood: the bay's tide range is one of the largest in the world, and pilgrims historically crossed the causeway with only the retreating tide as a schedule. The abbey itself is a vertical stack of three churches built one on top of another as the rock got crowded — 'the Wonder of the West.'",
        "The bay tide race + the stacked abbey of Mont Saint-Michel",
        ["France", "Medieval", "Architecture"],
    ),
    "wild-the-sydney-opera-house-188": _entry(
        "Structure",
        "Danish architect Jørn Utzon won the Sydney Opera House competition in 1957 with a sketch of overlapping shells — which engineers initially said couldn't be built. The project ran 10 years late and 14 times over budget, and Utzon resigned in 1966, never returning to see it finished or to collect the Pritzker Prize awarded for it in 2003.",
        "Watch the construction documentary footage: the shells were solved by treating them as sections of the same sphere — every shell rib is part of one imaginary ball, which is why the roof looks organic but was buildable. Then notice the tiles: over a million white and cream ceramic tiles, self-cleaning by rain, made in Sweden.",
        "The sphere-based geometry of the Opera House shells + Swedish tiles",
        ["Australia", "Architecture", "Modern"],
    ),
    "wild-the-eiffel-tower-189": _entry(
        "Structure",
        "The Eiffel Tower was built for the 1889 World's Fair as a temporary structure meant to stand 20 years — and Parisian artists signed a public protest against it, calling it a 'gigantic black smokestack.' It survives today because Gustave Eiffel added a radio antenna, making the tower too useful to demolish.",
        "Read the 1889 protest letter — the signatories included Guy de Maupassant, who reportedly ate lunch in the tower's restaurant because it was the one place in Paris he couldn't see it. Then find the wind-tunnel connection: Eiffel's later aerodynamic experiments on the tower's shape helped pioneer the wind tunnel itself.",
        "The 1889 artists' protest letter + Eiffel's radio-antenna rescue",
        ["France", "Engineering", "History"],
    ),
    "wild-the-grand-canyon-190": _entry(
        "Site",
        "The Grand Canyon exposes nearly two billion years of Earth's history in its walls — the oldest rocks at the bottom are Precambrian, more than half the planet's age — carved by the Colorado River over the past five to six million years. In one famous gap, the 'Great Unconformity,' 1.2 billion years of rock are simply missing.",
        "Look at the canyon wall layers on the Bright Angel Trail route: the Tapeats Sandstone at the bottom sits directly on granite, with over a billion years of missing record between them — the Great Unconformity, visible as a clean horizontal line. Then find the Nankoweap Granaries, where ancient Pueblo people stored corn in cliff caves.",
        "The Great Unconformity — the billion-year gap in the canyon walls",
        ["USA", "Geology", "Nature"],
    ),
    "wild-mount-everest-191": _entry(
        "Place",
        "Mount Everest stands 8,849 meters high and grows about 4 millimeters a year as the Indian plate keeps pushing into Asia. The first summit was in 1953 — Edmund Hillary and Tenzing Norgay — and more than 300 people have died on the mountain since, many of them frozen where they fell.",
        "Read the 1953 expedition account: the route up the South Col, the oxygen strategy, and the final ridge where Hillary and Tenzing chose to climb together rather than race. Then look at the modern climbing statistics — the 2019 'traffic jam' photos show queues above 8,000 meters in the 'death zone,' where the body can't recover from exertion.",
        "The 1953 Hillary–Tenzing summit account vs. modern summit-day traffic",
        ["Nepal", "Mountains", "Exploration"],
    ),
    "wild-the-amazon-rainforest-192": _entry(
        "Place",
        "The Amazon spans 5.5 million square kilometers across nine countries and holds roughly one-tenth of all known species — an estimated 390 billion trees. The 'lungs of the Earth' slogan is only half right: the forest breathes in about as much CO2 as it releases, which is exactly why it matters — it's a vast, fragile carbon store, not a net oxygen factory.",
        "Read the numbers carefully: mature rainforest produces oxygen but consumes almost all of it through respiration — the '20% of Earth's oxygen' claim is a myth. What is real is the carbon: the Amazon holds decades of human emissions, and deforestation releases it. Then find the 2019 fires and the 'arc of deforestation' that tracks roads built since the 1970s.",
        "The Amazon carbon-cycle myth-bust — 390 billion trees in context",
        ["South America", "Rainforest", "Ecology"],
    ),
    "wild-the-dead-sea-193": _entry(
        "Place",
        "The Dead Sea is the lowest place on land — its surface sits about 430 meters below sea level and is still dropping. At roughly 34% salinity, nearly ten times the ocean, nothing but microbes can live in it, which is why it's called dead — and why you float without trying, since the salt makes your body denser than the water.",
        "Read why the level is falling about a meter a year: the Jordan River, its main source, is diverted for agriculture and cities, and mineral-extraction ponds accelerate the loss. The consequence is visible from the road — thousands of sinkholes have opened along the shore as the shoreline recedes, swallowing the resorts that once lined it.",
        "The Dead Sea's falling level + the receding-shoreline sinkholes",
        ["Middle East", "Geography", "Geology"],
    ),
    "wild-yellowstone-194": _entry(
        "Place",
        "Yellowstone, established in 1872, is the world's first national park — and it sits on top of a supervolcano whose caldera is 70 by 45 kilometers. The park holds more than half of all the geysers on Earth, and Old Faithful has erupted roughly every 60–90 minutes for as long as records exist.",
        "Look at the caldera map and notice that most of the park's features — the geysers, the hot springs, the mud pots — trace its rim, where the volcanic heat is closest to the surface. Then find the bison: Yellowstone's herd is the only one in the US to have lived continuously on the same land since prehistoric times, surviving the species' near-extinction.",
        "The Yellowstone caldera map + the continuous prehistoric bison herd",
        ["USA", "Geology", "Nature"],
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
