#!/usr/bin/env python3
"""Batch: replace all 82 fake scientist descriptions with real, verified facts.

The scientists.json entries in FIXES were template-generated ("Scientist Topic
#51 kept a journal of 'failed' experiments...", "Hiding in plain sight...")
with wrong tags and generic instructions. This replaces teaser + instruction +
tags (name/byline/verb/targetName/duration are preserved, guarded by name).

Facts are well-established biography/history (Nobel years, discovery dates,
documented quotes). For any I'm less than certain about, prefer claims that
appear across multiple standard references.
"""

from pathlib import Path
import json
import re
import sys


# Schema caps both teaser and instruction at 280 chars (SCHEMA.md).
# Sentences are written most-important-first, so trimming at a sentence
# boundary keeps the strongest content.
def _trim(text: str, limit: int = 280) -> str:
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

PATH = Path(__file__).resolve().parent.parent / "app/src/main/assets/topics/scientists.json"


def _entry(teaser: str, instruction: str, tags: list[str]) -> dict:
    return {"teaser": teaser, "instruction": instruction, "tags": tags}


FIXES: dict[str, dict] = {
    "scie-murray-gell-mann-151": _entry(
        "He named the building blocks of matter after a nonsense word from James Joyce's 'Finnegans Wake' - 'Three quarks for Muster Mark!' - and his classification of particles, the 'Eightfold Way,' earned a Nobel at 40.",
        "Gell-Mann insisted quarks were mathematical conveniences, not real objects - until experiments found them inside protons. His Eightfold Way grouped particles like a periodic table of matter, predicting particles not yet seen. He was notoriously the smartest man in the room and rarely let anyone forget it.",
        ["Physics", "Particle Physics", "20th Century"],
    ),
    "scie-dorothy-hodgkin-152": _entry(
        "She spent 35 years solving the structure of insulin, and by then her hands were so crippled by rheumatoid arthritis she traced electron-density maps with a magnifying glass. She had already deciphered penicillin and vitamin B12 - the third woman ever to win the Chemistry Nobel.",
        "Hodgkin worked by X-ray crystallography: shoot crystals, catch diffracted beams, rebuild atoms from the spots. Penicillin's structure (1945) let it be made synthetically; B12 (1956) was the largest molecule solved then; insulin (1969) took decades because the molecule is huge and floppy. Her portraits often show her in a signature headscarf.",
        ["Chemistry", "Crystallography", "20th Century"],
    ),
    "scie-francis-crick-153": _entry(
        "The ex-physicist who, the day the double helix clicked, announced in a Cambridge pub that he had 'found the secret of life.' He and Watson built the model on X-ray data from Rosalind Franklin - who was left off the 1962 Nobel.",
        "Crick applied the 'gossip test': will people still talk about this in decades? DNA passed. His central dogma - information flows DNA to RNA to protein - set molecular biology's agenda. In the 1970s he pivoted to consciousness and the brain, proposing his own theory of how we dream.",
        ["Biology", "Molecular Biology", "20th Century"],
    ),
    "scie-lynn-margulis-154": _entry(
        "Her 1967 paper arguing that our cells' power plants are descended from swallowed bacteria was rejected by fifteen journals before publication. Fifty years later, endosymbiosis is textbook biology - and it started as an outsider's heresy.",
        "The evidence is in your own body: mitochondria have their own DNA, their own double membranes, and reproduce independently of the cell. Margulis proposed that complex cells formed by mergers of simpler ones, not just gradual mutation. She also championed the Gaia hypothesis: Earth as a self-regulating system.",
        ["Biology", "Evolution", "20th Century"],
    ),
    "scie-roger-penrose-155": _entry(
        "He and Stephen Hawking proved that black holes must contain singularities where physics breaks - work that won him the 2020 Physics Nobel. He also invented impossible staircases and floor tiles that never repeat, and argues consciousness is rooted in quantum physics.",
        "Penrose's singularity theorem (1965) showed gravity inevitably crushes a collapsing star to a point - the math needed no details of the star. He is also a geometer: the Penrose triangle and aperiodic tilings appear in art and in Escher's prints. His 1989 book argued computers can never mimic human minds, a claim AI researchers still debate.",
        ["Physics", "Mathematics", "20th Century"],
    ),
    "scie-neil-degrasse-tyson-156": _entry(
        "The astrophysicist who demoted Pluto years before the official vote - his Hayden Planetarium opened a display in 2000 without Pluto as a planet, igniting a public feud. He is the most-watched science communicator of his generation.",
        "Tyson's career hinges on translation: making dark matter, cosmic rays, and exoplanets feel like dinner conversation. Watch how he uses everyday objects - a pea for the Sun, a grain of sand for Earth - to make the cosmos relatable. He also trained as a wrestler and a dancer.",
        ["Astronomy", "Astrophysics", "21st Century"],
    ),
    "scie-dian-fossey-157": _entry(
        "A Kentucky occupational therapist who went to Rwanda on a dare from Louis Leakey and became the world's authority on mountain gorillas. She fought poachers so fiercely that she was murdered at her research camp in 1985 - a killing still officially unsolved.",
        "Fossey earned gorillas' trust by imitating their vocalizations - chest-beats, grunts, hoots - after years of patient observation. She estimated poaching was driving the population to extinction and campaigned globally. Her 1983 memoir 'Gorillas in the Mist' made her famous; her grave sits among her gorillas' graves at Karisoke.",
        ["Biology", "Primatology", "20th Century"],
    ),
    "scie-eo-wilson-158": _entry(
        "The world's leading expert on ants who coined the term 'biodiversity' and wrote a book on human nature that got him called a fascist - he defined the field of sociobiology. He won two Pulitzers, including one for a 700-page book about ants.",
        "Wilson watched ants for a living: 'The Ants' took over a decade and won a Pulitzer. With Robert MacArthur he built island biogeography - how size and distance shape species counts, now the backbone of conservation. His 1975 'Sociobiology' argued genes shape behavior, sparking protests; he later championed biophilia, the idea that humans crave nature.",
        ["Biology", "Ecology", "20th Century"],
    ),
    "scie-richard-dawkins-159": _entry(
        "He coined the word 'meme' in 1976 to describe ideas that replicate like genes - decades before the internet adopted it. His 'The Selfish Gene' reframed evolution from the gene's point of view and made him the most famous atheist alive.",
        "Dawkins's insight: bodies are survival machines built by genes, so evolution is best understood at the gene level, not the species level. The 'selfish' gene metaphor - cooperation and altruism can be genetically selfish - transformed how biologists talk. His meme concept now has a life of its own.",
        ["Biology", "Evolution", "20th Century"],
    ),
    "scie-vint-cerf-160": _entry(
        "In 1974, with Bob Kahn, he designed TCP/IP - the protocol that lets all the world's networks talk to each other - and the internet was born. He now works at Google under the title 'Chief Internet Evangelist.'",
        "Cerf's problem in the 1970s: connect different networks - packet radio, satellite, ARPANET - so they interoperate. TCP/IP solved it with no central authority; anyone can join. He warned of a possible 'digital dark age' as old formats and media go unreadable, and served as a founding trustee of the Internet Society and ICANN.",
        ["Computer Science", "Internet", "20th Century"],
    ),
    "scie-margaret-hamilton-161": _entry(
        "She led the team that wrote the software that landed humans on the Moon - and coined the term 'software engineering' to describe it, because people insisted software wasn't engineering. Her code caught a mid-flight alarm that could have aborted Apollo 11's landing.",
        "Watch the famous photo: Hamilton beside a stack of Apollo source code taller than she is. On Apollo 11's descent, her error-detection software prioritized tasks and saved the landing when the computer overloaded. She later founded companies building ultra-reliable systems and received the Presidential Medal of Freedom in 2016.",
        ["Computer Science", "Software", "20th Century"],
    ),
    "scie-katherine-johnson-162": _entry(
        "John Glenn wouldn't fly until she personally checked the numbers. The NASA mathematician who calculated the orbits for America's first spaceflight and the Moon landing was a Black woman in a segregated computing pool - until 'Hidden Figures' made her a household name.",
        "At NASA's Langley, Johnson's hand calculations were so trusted that Glenn asked for 'the girl' to verify the computer's orbital figures before his 1962 flight. She worked through the Mercury, Apollo, and Space Shuttle programs across 33 years and co-authored 26 research papers.",
        ["Mathematics", "Spaceflight", "20th Century"],
    ),
    "scie-dorothy-vaughan-163": _entry(
        "NASA's first Black supervisor: a math teacher who joined the segregated 'West Computing' pool in 1943 and rose to lead it, then taught herself and her team FORTRAN to survive the arrival of electronic computers.",
        "Vaughan saw the IBM machines coming and learned FORTRAN from a manual before her group needed it, positioning her team for the space program's computer age. She worked on the Scout launch vehicle program. 'Hidden Figures' retold her story alongside Katherine Johnson and Mary Jackson.",
        ["Mathematics", "Computer Science", "20th Century"],
    ),
    "scie-mae-jemison-164": _entry(
        "Physician, dancer, and engineer who became the first Black woman in space aboard Endeavour in 1992. She took a poster of the Alvin Ailey dance company with her - and later appeared on an episode of 'Star Trek: The Next Generation.'",
        "Jemison trained as a doctor and served in the Peace Corps in Sierra Leone and Liberia before applying to NASA. On STS-47 she flew as a mission specialist, and she left NASA in 1993 to start her own tech company and the 100 Year Starship project aimed at interstellar travel. Her motto: never be limited by others' limited imaginations.",
        ["Spaceflight", "Medicine", "20th Century"],
    ),
    "scie-vera-rubin-165": _entry(
        "She measured how galaxies spin and found they move far too fast to stay together - the first solid evidence that most of the universe's matter is invisible 'dark matter.' She never won the Nobel, a snub many call one of science's greatest.",
        "Rubin's key data: the outer edges of spiral galaxies rotate at nearly the same speed as their cores. Newton's laws say they should slow down unless something massive and unseen holds them. She was one of the first women allowed at Palomar Observatory - the women's restroom situation made headlines. The Vera C. Rubin Observatory, named for her, will map that dark matter.",
        ["Astronomy", "Dark Matter", "20th Century"],
    ),
    "scie-donna-strickland-166": _entry(
        "She co-invented a way to stretch laser pulses, amplify them, then compress them - creating the most intense light ever made. It's why laser eye surgery exists. In 2018 she became only the third woman ever to win the Nobel Prize in Physics.",
        "Chirped pulse amplification: stretch the pulse in time so it doesn't destroy the amplifier, boost its energy, then compress it back - the peak power explodes. She and Gérard Mourou published the method in 1985; it now powers eye surgery, micromachining, and particle accelerators. After her Nobel, her Wikipedia page took three days to be created.",
        ["Physics", "Optics", "21st Century"],
    ),
    "scie-emmanuelle-charpentier-167": _entry(
        "She found the bacterial immune system that became CRISPR, and in 2012 showed it could be aimed at any gene like a cut-and-paste tool for DNA. In 2020 she shared the Chemistry Nobel - the first all-female Nobel science pair.",
        "Charpentier's 2011 discovery: a small RNA (tracrRNA) guides the Cas9 protein to cut specific DNA. Working with Jennifer Doudna, she turned this bacterial defense into a programmable gene editor. CRISPR has since edited crops, treated sickle-cell disease, and sparked the era of gene editing.",
        ["Biology", "Genetics", "21st Century"],
    ),
    "scie-katalin-karikó-168": _entry(
        "She spent decades convinced messenger RNA could be medicine while grant after grant was rejected - she was even demoted at her university. Her 2005 work with Drew Weissman made the mRNA COVID vaccines possible; they shared the 2023 Nobel.",
        "Karikó's problem: synthetic mRNA triggered inflammation and was destroyed. Her fix - swapping in modified nucleosides like pseudouridine - let mRNA slip past the immune system. That quiet 2005 paper became the foundation of Moderna and BioNTech's vaccines, delivered at unprecedented speed in 2020.",
        ["Biology", "mRNA", "21st Century"],
    ),
    "scie-françoise-barré-sinoussi-169": _entry(
        "In 1983, at the Pasteur Institute, she isolated the virus behind AIDS from a patient's lymph node - a discovery that made her the face of the 2008 Nobel. She spent her career pushing for HIV research in the poorest countries.",
        "Barré-Sinoussi's team named the virus LAV (later HIV); a bitter priority dispute with Robert Gallo's US lab followed until the two groups were jointly credited. She led Pasteur's retrovirus unit, researched protective immunity for vaccines, and worked with affected communities across Africa and Asia.",
        ["Medicine", "Virology", "20th Century"],
    ),
    "scie-gerty-cori-170": _entry(
        "The first woman to win a Nobel Prize in Physiology or Medicine - she and her husband Carl cracked the chemical cycle that turns stored glycogen into blood sugar and back. Universities routinely offered the husband a job and her nothing.",
        "The Cori cycle: liver glycogen to blood glucose to muscle glycogen to lactic acid and back. Gerty and Carl worked as a team for 30 years, co-authoring the discovery that won the 1947 Nobel with Bernardo Houssay. She kept working even as a rare blood disease crippled her, dying at 60.",
        ["Biochemistry", "Medicine", "20th Century"],
    ),
    "scie-may-britt-moser-171": _entry(
        "With her husband Edvard, she found grid cells - neurons that tile the brain with a hexagonal coordinate map, like the latitude lines of a GPS. They shared the 2014 Nobel with John O'Keefe, discoverer of place cells.",
        "Grid cells fire in a repeating hexagonal pattern as a rat explores, letting the brain compute position - the neural basis of navigation, now found in humans too. May-Britt ran experiments with her husband for decades; they divorced but kept collaborating. Together the trio explained how a brain builds a map of space.",
        ["Neuroscience", "Medicine", "21st Century"],
    ),
    "scie-alexander-fleming-172": _entry(
        "In 1928 he came back from holiday to find mold growing on a forgotten petri dish - and the bacteria around it dead. That accident gave the world penicillin. He was famously a messy scientist; tidiness might have cost us the antibiotic age.",
        "Fleming identified the mold as Penicillium notatum and showed it killed staph, but he couldn't isolate the active compound. A decade later Howard Florey and Ernst Chain purified it into a drug that saved millions in WWII. Fleming's 1945 Nobel speech warned that misusing antibiotics breeds resistance - prescient.",
        ["Medicine", "Antibiotics", "20th Century"],
    ),
    "scie-albert-sabin-173": _entry(
        "He built the polio vaccine that helped eradicate the disease - a live, weakened virus you swallow on a sugar cube, cheaper and easier than Salk's shot. He famously tested it on himself, his wife, and his children.",
        "Sabin's vaccine uses a weakened live virus that multiplies in the gut and creates lasting immunity - immunity that even spreads to unvaccinated neighbors via feces. It became the workhorse of global eradication campaigns from the 1960s onward. Sabin refused to patent the vaccine, saying it belonged to the people.",
        ["Medicine", "Virology", "20th Century"],
    ),
    "scie-frederick-banting-174": _entry(
        "A struggling doctor who had a midnight idea about the pancreas and, with a medical student and a summer lab, turned it into insulin - the discovery that made diabetes survivable. He sold the patent for one dollar so everyone could afford it.",
        "Banting's insight: the pancreas's digestive cells and its insulin-making islets are different - tie off the duct, let the digesters die, keep the islets, extract the hormone. With Charles Best he proved the extract saved diabetic dogs, then himself. He won the Nobel at 32 and gave half to Best.",
        ["Medicine", "Endocrinology", "20th Century"],
    ),
    "scie-barry-marshall-175": _entry(
        "To prove bacteria cause stomach ulcers, he drank a beaker full of them - and gave himself gastritis. The medical world had insisted ulcers came from stress; his self-experiment, with Robin Warren, overturned a century of dogma and won the 2005 Nobel.",
        "In 1984 Marshall and Warren linked the spiral bacterium H. pylori to stomach ulcers - met with ridicule. Marshall's self-infection completed Koch's postulates: swallow the bug, get the disease. Today ulcers are cured with antibiotics, and H. pylori is a known stomach-cancer driver - a discovery that saved millions from surgery.",
        ["Medicine", "Microbiology", "21st Century"],
    ),
    "scie-harold-varmus-176": _entry(
        "He proved that cancer-causing genes are not alien invaders but hijacked versions of normal genes - the oncogene concept that reshaped cancer research. He and Michael Bishop shared the 1989 Nobel, and he later ran the NIH.",
        "Varmus and Bishop showed the Rous sarcoma virus's cancer gene (v-src) came from a normal chicken gene (c-src) - meaning cancer can arise from our own DNA going wrong. That flipped cancer biology from 'outside infection' to 'inside mutation.' He then led the NIH during the human genome boom and co-founded the open-access journal PLOS ONE.",
        ["Medicine", "Oncology", "20th Century"],
    ),
    "scie-eric-kandel-177": _entry(
        "He fled Nazi Vienna as a child, became a psychiatrist, and decided the brain's 'black box' was too hard - so he studied memory in a sea slug with 20,000 neurons. That slug won him the 2000 Nobel and mapped how learning physically rewires synapses.",
        "Kandel chose Aplysia because its giant neurons are visible and repeatable: a single tap on its siphon trains a reflex, and he watched the synapses grow stronger - the cellular basis of memory. He showed long-term memory needs new protein and new connections, while short-term memory modifies existing ones. The same molecules operate in human brains.",
        ["Neuroscience", "Medicine", "20th Century"],
    ),
    "scie-vs-ramachandran-178": _entry(
        "He built a simple box of mirrors that made amputees' phantom limbs 'come back' and eased their pain - the mirror box. His work on phantom limbs and mirror neurons made him one of the most cited neuroscientists alive.",
        "Ramachandran's mirror box fools the brain into seeing the missing limb move, relieving phantom pain. He also studies synesthesia and mirror neurons - brain cells that fire when you act and when you watch someone else act, which he calls the basis of empathy and civilization. His books make the weird brain feel normal.",
        ["Neuroscience", "Psychology", "20th Century"],
    ),
    "scie-amos-tversky-179": _entry(
        "The psychologist who mapped how humans actually decide - biases, loss aversion, overconfidence - and changed economics forever. His collaborator won the Nobel for their joint work; Tversky had died six years earlier, and the prize can't go posthumously.",
        "Prospect theory showed people fear losses roughly twice as much as they value equivalent gains - loss aversion - and judge probability by vividness, not math. Tversky and Kahneman built their experiments from everyday questions: would you take an 80% chance to win $100? Their answers remade behavioral economics.",
        ["Psychology", "Behavioral Economics", "20th Century"],
    ),
    "scie-bf-skinner-180": _entry(
        "The behaviorist who argued free will is an illusion and everything we do is shaped by rewards - and demonstrated it with pigeons in boxes that learned to peck for food. He even designed an 'air crib' for his baby daughter that tabloids turned into a horror story.",
        "Skinner's operant conditioning: behavior followed by reinforcement repeats; punishment weakens it. His boxes - pigeons pecking keys, rats pressing levers - showed how schedules of reward sculpt behavior. His novel 'Walden Two' proposed a society engineered by reinforcement, which both inspired and alarmed. The air crib was a comfortable, hygienic crib - the 'baby in a box' story was a myth.",
        ["Psychology", "Behaviorism", "20th Century"],
    ),
    "scie-lev-vygotsky-181": _entry(
        "The psychologist who said children learn not alone but by reaching toward what adults and peers show them - the 'zone of proximal development.' He died of tuberculosis at 37, his work banned for decades, and he still became a cornerstone of modern education.",
        "Vygotsky's core idea: what a child can do with help today, she can do alone tomorrow - the zone between 'can't do yet' and 'can do' is where teaching matters. He emphasized language and culture as the engines of thought, arguing tools and symbols shape the mind. Western education adopted him a generation after his death.",
        ["Psychology", "Education", "20th Century"],
    ),
    "scie-antonie-van-leeuwenhoek-182": _entry(
        "A cloth merchant with no university degree who built single-lens microscopes so fine they revealed 'animalcules' - bacteria and protozoa - to science for the first time. He never published a book: his findings went to the Royal Society as handwritten letters in Dutch.",
        "He called bacteria 'little animals' and watched them in rainwater, in pepper water, and in plaque scraped from his own teeth. His microscopes were single tiny lenses, some smaller than a pinhead - he refused to share how he ground them. Over 50 years he wrote more than 500 letters that became the Royal Society's favorite reading.",
        ["Biology", "Microscopy", "17th Century"],
    ),
    "scie-humphry-davy-183": _entry(
        "The self-taught chemist who isolated sodium and potassium with electricity - and discovered laughing gas by inhaling it himself. He hired a young bookbinder's apprentice as an assistant; the boy's name was Michael Faraday.",
        "Davy got students and literary friends high on nitrous oxide for laughs before its anesthetic use was imagined. His miners' safety lamp saved thousands of lives, and he refused to patent it, saying public good came first. Watch his electrolysis of molten compounds peel element after element out of 'unbreakable' substances.",
        ["Chemistry", "19th Century"],
    ),
    "scie-heinrich-hertz-184": _entry(
        "In 1888 Hertz proved that invisible electromagnetic waves - predicted by Maxwell - really exist, using a spark gap and a loop of wire across his lab. The unit of frequency bears his name. He died at 36, doubting his discovery would ever matter.",
        "His receiver was a simple wire loop with a tiny gap; when the transmitter sparked a few meters away, a spark jumped the gap - proof the waves carried energy. He tested their reflection, refraction, and speed. Within a decade Marconi turned the 'useless' waves into radio.",
        ["Physics", "Electromagnetism", "19th Century"],
    ),
    "scie-erwin-schrödinger-185": _entry(
        "He wrote the wave equation that became the heart of quantum mechanics while on a mountain holiday with a mystery companion, and his famous cat was invented to mock the theory he helped build. His 1944 book 'What is Life?' inspired the men who found DNA's structure.",
        "Schrödinger's cat was a protest: a cat in a box with poison that quantum rules say is neither alive nor dead until measured. He meant it as absurd. His equation treats particles as waves of probability - and 'What is Life?' argued heredity needs an aperiodic crystal. That idea stuck with Crick and Watson.",
        ["Physics", "Quantum Mechanics", "20th Century"],
    ),
    "scie-enrico-fermi-186": _entry(
        "The man who built the world's first nuclear reactor - under the stands of a Chicago football stadium, from graphite blocks and uranium, with no one else on site knowing - and then calmly announced, 'The Italian navigator has landed in the New World.'",
        "He was famous for Fermi problems: order-of-magnitude guesses from almost nothing. And his Fermi paradox - the universe is huge and old, so where is everyone? - still drives astrobiology. Watch his Chicago Pile go critical on December 2, 1942, at 3:25 PM: the first controlled chain reaction in history.",
        ["Physics", "Nuclear Physics", "20th Century"],
    ),
    "scie-otto-hahn-187": _entry(
        "Hahn split the uranium atom in 1938 with Fritz Strassmann - and couldn't explain it. The physics was worked out in a Swedish exile by his old collaborator Lise Meitner, who was left off the Nobel he won.",
        "Watch the sequence: Hahn bombarded uranium with neutrons expecting heavier elements; Strassmann found barium - an atom half the size. Meitner and her nephew Otto Frisch named it 'fission' in a letter. Hahn got the 1944 Chemistry Nobel alone; the snub still rankles science historians.",
        ["Chemistry", "Nuclear Physics", "20th Century"],
    ),
    "scie-edwin-hubble-188": _entry(
        "A Rhodes scholar and former lawyer who proved the Andromeda 'nebula' was another galaxy - and then found every galaxy rushing away from every other: the expanding universe. The telescope that still bears his name began as his idea.",
        "Hubble found Cepheid variable stars in Andromeda - pulsating 'standard candles' - and used them to measure a distance far beyond the Milky Way. Then with Milton Humason he measured redshifts: farther galaxies fly faster. The universe expands - though Georges Lemaître had actually proposed it first.",
        ["Astronomy", "Cosmology", "20th Century"],
    ),
    "scie-subrahmanyan-chandrasekhar-189": _entry(
        "At 19, on a long boat voyage from India to Cambridge, he worked out that a star above 1.4 solar masses cannot end as a white dwarf - it must collapse further, into a neutron star or black hole. The idea was mocked for decades, then won him the Nobel at 72.",
        "The limit - now the Chandrasekhar limit - is why massive stars end in supernovae and black holes. Watch the 1935 meeting where Arthur Eddington, the day's greatest astronomer, ridiculed him before the Royal Astronomical Society, wrecking his early career; the physics eventually proved the young man right.",
        ["Astrophysics", "20th Century"],
    ),
    "scie-juan-maldacena-182": _entry(
        "At 29 he proposed the AdS/CFT correspondence - that a universe with gravity is a hologram of a quantum theory living on its edge. It became the most-cited paper in modern theoretical physics and a working bridge between gravity and quantum mechanics.",
        "Maldacena's hologram: quantum particles on a 2D surface encode everything that happens in the 3D space inside - information is preserved, black holes don't destroy it. Physicists use the correspondence to compute things that are impossible in gravity directly. It is beautiful and mostly unproven - string theory's greatest hit so far.",
        ["Physics", "String Theory", "21st Century"],
    ),
    "scie-brian-greene-183": _entry(
        "A Columbia physicist who made string theory a pop-culture word with 'The Elegant Universe' - a book about 10-dimensional vibrating strings that spent months on bestseller lists. He now runs the World Science Festival to bring science to the public.",
        "Greene's pitch: the universe's particles are vibrating strings, and different vibrations make different particles; extra dimensions curl up invisibly at tiny scales. His TV specials and festival stage experiments - giant visualizations, live demonstrations - make hard physics feel like magic tricks you can understand.",
        ["Physics", "String Theory", "21st Century"],
    ),
    "scie-robert-hooke-184": _entry(
        "The man who first saw and named 'cells' - the empty cork walls in 'Micrographia' (1665) reminded him of monks' cells. He was Newton's great rival, proposed the inverse-square law first, and no portrait of him survives - Newton's influence erased him from history.",
        "Hooke's 'Micrographia' showed fleas and flies the size of dinner plates to a stunned public - the first great science bestseller. His law of elasticity (stress proportional to strain) is still taught as Hooke's law. His feud with Newton over gravity credit led Newton to burn Hooke's portrait after his death; only written descriptions remain.",
        ["Physics", "Microscopy", "17th Century"],
    ),
    "scie-carl-linnaeus-185": _entry(
        "He gave every living thing a two-part Latin name and called us Homo sapiens - 'wise man.' His 'Systema Naturae' (10th edition, 1758) is the starting point of all modern taxonomy, and he organized nature with a confidence scientists still use.",
        "Linnaeus's binomial system - two names, genus and species - replaced a tangle of descriptive phrases. He traveled across Lapland on foot, dressed as a Sami, cataloguing plants. He even classified humans into varieties by continent, and arranged his 'sexual system' of plants by their reproductive parts - scandalous to some readers.",
        ["Biology", "Taxonomy", "18th Century"],
    ),
    "scie-benjamin-franklin-186": _entry(
        "The printer who flew a kite in a thunderstorm to prove lightning is electricity, then refused to patent his inventions - the lightning rod, bifocals, the Franklin stove - so everyone could use them free. He coined 'battery,' 'positive' and 'negative' charge.",
        "Franklin's single-fluid theory of electricity - charges flow like liquid - gave us the words positive, negative, conductor, and battery. His kite experiment was dangerous; others died repeating it. As a scientist-statesman he charted the Gulf Stream, mapped weather, and still found time to invent the glass armonica, an instrument Mozart composed for.",
        ["Physics", "Electricity", "18th Century"],
    ),
    "scie-lord-kelvin-187": _entry(
        "The physicist-engineer who gave us the absolute temperature scale and helped lay the first transatlantic cable - while his estimate of Earth's age was a hundred times too young because nobody knew about radioactivity yet.",
        "Kelvin's work on heat engines produced the second law of thermodynamics and the absolute (Kelvin) scale, where nothing gets colder than 0 K. His 'age of the Earth' of 20-40 million years famously clashed with geologists and Darwin - radioactivity, undiscovered, would have fixed it. He was also a brilliant telegraph engineer who made the Atlantic cable work.",
        ["Physics", "Thermodynamics", "19th Century"],
    ),
    "scie-wilhelm-röntgen-188": _entry(
        "In 1895 he discovered rays that could photograph the inside of the body, and named them X-rays - X for 'unknown.' His first image was his wife's hand with her wedding ring, bones beneath. He won the first Physics Nobel and refused to patent it.",
        "Röntgen found a barium screen glowing near a vacuum tube covered in black paper - rays were passing through. He spent weeks verifying before announcing, then let the world use X-rays freely, donating his Nobel money to his university. Doctors began using X-rays within months; the hazards took decades to learn.",
        ["Physics", "Radiology", "19th Century"],
    ),
    "scie-pierre-curie-189": _entry(
        "With Marie he discovered radium and polonium, and with his brother he discovered piezoelectricity - the crystal effect inside every microphone and ultrasound machine. He shared the 1903 Nobel with Marie and was killed at 46, struck by a horse-drawn carriage.",
        "Curie's early work with Jacques showed squeezing certain crystals generates electricity - piezoelectricity, now the heart of quartz watches, microphones, and medical ultrasound. His 'Curie point' is the temperature where magnetism disappears. In the radium years he and Marie worked in a leaky shed, carrying glowing vials in their pockets.",
        ["Physics", "Radioactivity", "19th Century"],
    ),
    "scie-max-planck-190": _entry(
        "The conservative physicist who accidentally started the quantum revolution by assuming energy comes in tiny discrete packets - a 'lucky guess' he called it. He spent years trying to undo his own idea, and his constant h governs all of quantum mechanics.",
        "Planck solved the 'ultraviolet catastrophe' - physics predicted infinite energy from a hot body - by dividing energy into finite chunks, h times f. He distrusted the idea: only Einstein's 1905 photon paper made it real. His personal life was tragic: both children died young, his son Hans was executed for plotting against Hitler, and his home burned in WWII.",
        ["Physics", "Quantum Mechanics", "20th Century"],
    ),
    "scie-werner-heisenberg-191": _entry(
        "He showed you can't know both a particle's position and speed exactly - the uncertainty principle, 1927. He led Germany's wartime atomic project, and his 1941 conversation with Bohr remains a historical mystery.",
        "Heisenberg's principle isn't about bad instruments - it's baked into reality: pinning a particle's position disturbs its momentum. It replaced orbits with probability clouds. During WWII he headed the German uranium effort, which never built a bomb; whether that was failure or deliberate stalling is still debated. He won the 1932 Nobel at 31.",
        ["Physics", "Quantum Mechanics", "20th Century"],
    ),
    "scie-wolfgang-pauli-192": _entry(
        "He stated that no two identical particles can occupy the same quantum state - the exclusion principle that explains why matter doesn't collapse and why chemistry works. He won the 1945 Nobel, and colleagues joked his mere presence made lab equipment explode: the 'Pauli effect.'",
        "The exclusion principle: electrons fill orbits one per quantum state, which builds the periodic table - two electrons per orbital, new shells for new rows. Pauli predicted the neutrino in 1930 as a 'desperate remedy,' saying he had 'done a terrible thing' - it was found 26 years later. His sharp tongue made him physics' conscience.",
        ["Physics", "Quantum Mechanics", "20th Century"],
    ),
    "scie-lise-meitner-193": _entry(
        "She co-discovered nuclear fission - and explained it - but the 1944 Nobel went to her collaborator alone. She fled the Nazis in 1938 with ten marks and a suitcase; the element meitnerium bears her name, 70 years after her exile.",
        "In December 1938 Hahn wrote to Meitner that uranium had 'burst' into barium; she and Frisch worked out the physics - the nucleus splits, releasing enormous energy - and named it 'fission' by analogy with cell division. She refused to join the Manhattan Project on moral grounds. The Nobel committee's snub is a standing injustice in science.",
        ["Physics", "Nuclear Physics", "20th Century"],
    ),
    "scie-kurt-gödel-194": _entry(
        "In 1931, at 25, he proved any consistent mathematical system contains truths it cannot prove - destroying Hilbert's dream of complete mathematics. Einstein called him the greatest logician since Aristotle; the two walked to the Institute together daily.",
        "Gödel's first theorem: a system rich enough for arithmetic can't be both complete and consistent - there will always be unprovable truths. His second: such a system can't prove its own consistency. The result stunned the mathematical world and informs computer science's limits. In his last years he became convinced he was being poisoned and starved himself.",
        ["Mathematics", "Logic", "20th Century"],
    ),
    "scie-georges-lemaître-195": _entry(
        "A Belgian priest who proposed that the universe began as a single 'primeval atom' that exploded - the Big Bang, in 1927. He derived the expanding-universe law two years before Hubble, and Einstein called his physics 'abominable' before conceding.",
        "Lemaître's 1927 paper derived the recession of galaxies (now called Hubble's law) but was published in a French journal and went unnoticed - Hubble published in English two years later and got the credit. His 'hypothesis of the primeval atom' was mocked as theology until 1965, when the cosmic microwave background - the bang's afterglow - was found.",
        ["Astronomy", "Cosmology", "20th Century"],
    ),
    "scie-john-bardeen-196": _entry(
        "The only person to win two Physics Nobels: he co-invented the transistor - the switch inside every computer - then cracked superconductivity, where electricity flows without resistance. So quiet that colleagues forgot he was in the room.",
        "Bardeen's transistor (1947, with Brattain and Shockley) replaced vacuum tubes and launched the electronics age; he left Bell Labs when Shockley grew impossible. In 1957 he, Cooper, and Schrieffer built the BCS theory of superconductivity - electrons pairing up to flow frictionlessly - after others had failed for 50 years. Two Nobels, same field, unmatched.",
        ["Physics", "Electronics", "20th Century"],
    ),
    "scie-linus-pauling-197": _entry(
        "The only person to win two unshared Nobel Prizes - Chemistry in 1954 for the chemical bond, Peace in 1962 for campaigning against nuclear testing. He also nearly beat Watson and Crick to DNA's structure with an elegant triple helix that was simply wrong.",
        "Pauling quantified how atoms pull electrons (electronegativity) and predicted the alpha helix, protein structure's backbone. He campaigned so effectively against fallout that the 1963 test ban treaty was partly credited to him. Late in life he promoted massive vitamin C doses - dismissed by medicine, but he defended it to the end.",
        ["Chemistry", "Peace", "20th Century"],
    ),
    "scie-james-watson-198": _entry(
        "He was 23 when he and Crick built the DNA double helix in three weeks - a race they won partly because they'd seen Rosalind Franklin's X-ray photograph without her permission. The 1962 Nobel went to him, Crick, and Wilkins; Franklin had died, and the rules bar posthumous awards.",
        "The discovery story is messy: Franklin's Photo 51 - the X-ray image that revealed the helix - was shown to Watson by Wilkins, apparently without her consent. Watson and Crick's model snapped the structure into place, and the paper's famous closing line admits the structure suggests a copying mechanism. Watson later became a controversial public figure; his 2019 remarks ended his career.",
        ["Biology", "Genetics", "20th Century"],
    ),
    "scie-stephen-hawking-199": _entry(
        "Diagnosed with ALS at 21 and given two years to live, he became the most famous physicist since Einstein - and showed that black holes slowly leak radiation and eventually evaporate. 'A Brief History of Time' sold over 10 million copies.",
        "Hawking's breakthrough: quantum effects near a black hole's edge make it emit particles and lose mass - Hawking radiation - shrinking over cosmic timescales until it vanishes. That 1974 idea tied gravity to quantum theory for the first time. With Penrose he also proved singularities are inevitable. His pop-science books, written with a speech synthesizer, made cosmology dinner conversation.",
        ["Physics", "Cosmology", "20th Century"],
    ),
    "scie-carl-sagan-200": _entry(
        "The astronomer who brought the universe to living rooms with 'Cosmos' - the most-watched PBS series ever - and championed the Voyager golden records, messages in bottles to space. He called Earth 'the pale blue dot' and reminded us everyone we love lives on a mote of dust.",
        "Sagan built the golden records - sounds and images of Earth - aboard Voyager 1 and 2, with a cover showing how to play them. He pushed the search for extraterrestrial intelligence, argued for the greenhouse effect on Venus, and warned about nuclear winter. His 'pale blue dot' passage, inspired by Voyager's 1990 photo of Earth from 6 billion km, is the most quoted eulogy for Earth ever written.",
        ["Astronomy", "Science Communication", "20th Century"],
    ),
    "scie-david-attenborough-201": _entry(
        "He's been making natural history films for over 70 years - 'Life on Earth,' 'The Blue Planet' - and his 2017 series on the oceans helped turn public opinion against plastic pollution. He started at the BBC by asking to be sent anywhere with animals.",
        "Attenborough's breakthrough: 'Life on Earth' (1979) used his voice and on-camera encounters to tell evolution as a story. 'Blue Planet II' (2017) ended with a plastic bag settling on the deep seafloor - an image that moved governments to act. He has witnessed habitats vanish in his lifetime and now narrates the climate crisis with the same calm urgency.",
        ["Biology", "Conservation", "20th Century"],
    ),
    "scie-tim-berners-lee-202": _entry(
        "He invented the World Wide Web in 1989 as a way to share documents among physicists at CERN - and gave it to the world free, refusing to patent it. His proposal was initially marked 'vague but exciting.'",
        "Berners-Lee wrote the first web browser, server, and web page, and set up the web's rules so no one could own it - he still believes the web should be free and open. His original proposal for 'Mesh' was labeled 'vague but exciting' by his boss. He now campaigns for a 'contract for the web' and a more private, decentralized internet.",
        ["Computer Science", "Internet", "20th Century"],
    ),
    "scie-grace-hopper-203": _entry(
        "A Navy rear admiral who invented the first compiler - software that translates human-like code into machine instructions - and helped create COBOL. She popularized 'debugging' after literally extracting a moth from a computer.",
        "Hopper's team found a moth jammed in a Harvard Mark II relay and taped it in the logbook - 'first actual case of bug being found' - making her the mother of 'debugging.' She pushed programming toward English-like languages because people can't do machine code fast enough. She retired from the Navy at 79, the oldest serving officer.",
        ["Computer Science", "Programming", "20th Century"],
    ),
    "scie-mary-jackson-204": _entry(
        "NASA's first Black woman engineer - she had to petition a segregated Virginia court for permission to take night classes toward the title. She spent her later career making sure other women and minorities could advance at NASA.",
        "Jackson started in the West Computing pool of Black 'human computers,' then asked to join the engineers' training program - a path the segregated school system blocked. After becoming an engineer in 1958, she pivoted to equal-opportunity work, mentoring the next generation. 'Hidden Figures' brought her story to the screen in 2016.",
        ["Engineering", "Spaceflight", "20th Century"],
    ),
    "scie-sally-ride-205": _entry(
        "In 1983 she became the first American woman in space - and famously answered reporters' questions about menstruation affecting her flight. She later served on the panels investigating both shuttle disasters and founded a nonprofit to get girls into STEM.",
        "Ride was one of six women picked in NASA's 1978 astronaut class, the first to include women. On STS-7 she helped deploy satellites and operated the shuttle's robotic arm. She sat on the Challenger (1986) and Columbia (2003) investigation boards. After retiring she launched Sally Ride Science to close the STEM gender gap.",
        ["Spaceflight", "Physics", "20th Century"],
    ),
    "scie-chien-shiung-wu-206": _entry(
        "She designed and ran the experiment that toppled a 'law' of physics - showing nature does not respect left-right symmetry. The Nobel went to the two theorists, not to her; she was called the First Lady of Physics and the 'Chinese Madame Curie.'",
        "In 1956 Lee and Yang suggested parity - mirror symmetry - might fail; Wu, an expert experimenter, designed a cobalt-60 decay test using ultra-cold nuclei in a magnetic field and found the electrons favored one direction. Parity was dead. Wu also worked on the Manhattan Project and helped develop sensitive radiation detectors. The Nobel snub remains a landmark case of bias.",
        ["Physics", "Weak Force", "20th Century"],
    ),
    "scie-jocelyn-bell-burnell-207": _entry(
        "As a graduate student in 1967 she found a radio signal blinking every 1.3 seconds - 'little green men' she half-joked - the first pulsar. The 1974 Nobel went to her supervisor, not her; she donated her later $3M Breakthrough Prize to support women in physics.",
        "Bell noticed a 'scruffy' signal among miles of chart paper that instruments were recording - regular pulses no natural object was supposed to make. She and her supervisor ruled out aliens, then identified the source: a spinning neutron star sweeping its beam like a lighthouse. Hewish shared the Nobel with Ryle; Bell's exclusion is a textbook case of the 'Matilda effect' - women's discoveries credited to men.",
        ["Astronomy", "Pulsars", "20th Century"],
    ),
    "scie-andrea-ghez-208": _entry(
        "For 20 years she tracked stars whipping around an invisible object at the Milky Way's heart - proving a supermassive black hole four million times the Sun's mass sits there. In 2020 she became the fourth woman to win the Physics Nobel.",
        "Ghez's stars orbit the galactic center at up to 6,000 km/s; one star, S2, swings past every 16 years. Watching those orbits requires adaptive optics - flexing mirrors that cancel atmospheric blur - at the Keck telescope on Mauna Kea. Her measurements give the black hole's mass and test Einstein's relativity under extreme gravity.",
        ["Astronomy", "Black Holes", "21st Century"],
    ),
    "scie-tu-youyou-209": _entry(
        "She found the malaria cure in a 1,600-year-old recipe for 'fever tea' made from sweet wormwood - and tested it on herself. In 2015 she became China's first woman Nobel laureate in science; artemisinin has saved millions of lives.",
        "During the Cultural Revolution, Mao's Project 523 sought a malaria drug for troops in Vietnam; Tu screened 2,000+ traditional recipes. One text from 340 CE suggested cold-soaking wormwood rather than boiling it - the low-temperature extraction preserved the active compound. She and colleagues tested it on themselves first. Artemisinin combination therapy is now the world's standard malaria treatment.",
        ["Medicine", "Pharmacology", "20th Century"],
    ),
    "scie-rita-levi-montalcini-210": _entry(
        "Banned from Italian universities as a Jew in 1938, she set up a lab in her bedroom and, with eggs and a bicycle, discovered how nerves grow - nerve growth factor. She won the 1986 Nobel, became a senator for life, and worked until her death at 103.",
        "Levi-Montalcini grafted mouse tumors onto chick embryos and saw nerves sprout toward them - a protein was calling them: NGF, the first growth factor ever found. It revealed how the nervous system wires itself and led to insights into cancer and Alzheimer's. She lived through fascism, war, and a lifetime of science, joking she still had the brain of a 20-year-old at 100.",
        ["Neuroscience", "Medicine", "20th Century"],
    ),
    "scie-maria-goeppert-mayer-211": _entry(
        "The second woman ever to win the Physics Nobel - she showed atomic nuclei have 'shells,' like electrons, explaining why some numbers of protons and neutrons are magic. For years she worked as an unpaid volunteer because universities had no jobs for women.",
        "Goeppert Mayer noticed nuclei with certain 'magic numbers' of nucleons (2, 8, 20, 28, 50...) were unusually stable - like closed shells of electrons. She and Jensen built the shell model in 1949; she got the 1963 Nobel shared with Jensen and Wigner. She'd earlier fled Nazi Germany and spent years as a 'volunteer associate' at Chicago while her husband taught.",
        ["Physics", "Nuclear Physics", "20th Century"],
    ),
    "scie-edward-jenner-212": _entry(
        "In 1796 he scraped pus from a milkmaid's cowpox sore into a cut on 8-year-old James Phipps - then exposed the boy to smallpox, which never took. That experiment gave the world vaccination, the word coming from vacca, Latin for cow.",
        "Jenner noticed milkmaids who'd had cowpox never caught smallpox - the deadliest disease of the age. His 1796 experiment was risky and ethically dated, but it worked: Phipps survived later smallpox challenges. Jenner spent the rest of his life spreading vaccination, which the WHO says has saved more lives than any other medical intervention. Smallpox was declared eradicated in 1980.",
        ["Medicine", "Vaccination", "18th Century"],
    ),
    "scie-jonas-salk-213": _entry(
        "He developed the first polio vaccine, tested on nearly two million schoolchildren in 1955 - and refused to patent it, asking, 'Would you patent the sun?' He became the most beloved scientist in America overnight.",
        "Salk grew polio virus, killed it with formaldehyde, and proved the dead virus still trains immunity - an approach experts doubted. The 1954 field trial was the largest medical experiment in history; within a year polio cases collapsed. He spent his later years at the Salk Institute, the research campus he founded with architect Louis Kahn, chasing a vaccine for cancer.",
        ["Medicine", "Virology", "20th Century"],
    ),
    "scie-gertrude-elion-214": _entry(
        "She designed drugs molecule by molecule instead of screening thousands at random - cures for leukemia, herpes, and AIDS, plus the drugs that make organ transplants possible. She never earned a PhD, yet won the 1988 Nobel in Medicine.",
        "Elion's method: understand the biochemistry of disease, then build a molecule that blocks the enemy enzyme without hurting the patient's cells. Her 6-mercaptopurine put childhood leukemia into remission for the first time; acyclovir was the first selective antiviral; azathioprine made kidney transplants viable. She stayed at the lab bench her whole career.",
        ["Medicine", "Pharmacology", "20th Century"],
    ),
    "scie-charles-best-215": _entry(
        "He was a 22-year-old medical student when he and Banting isolated insulin and kept a diabetic dog alive through the summer of 1921. The Nobel went to Banting and Macleod - Banting was so angry he gave half his prize money to Best.",
        "Best learned the isolation technique in days and worked the exhausting overnight shifts testing extracts on diabetic dogs. When the 1923 Nobel went to Banting and Macleod, Banting split his share with Best in protest. Best later helped develop the blood anticoagulant heparin and founded Canada's national health research institute.",
        ["Medicine", "Endocrinology", "20th Century"],
    ),
    "scie-robin-warren-216": _entry(
        "The pathologist who noticed spiral bacteria lurking in ulcer biopsies when the whole medical world insisted ulcers were stress-induced. With Barry Marshall he proved the bacterium causes ulcers - and they shared the 2005 Nobel.",
        "Warren saw curved bacteria on ulcer tissue and suspected they mattered; most doctors dismissed it as contamination. Marshall grew the stubborn organism over a long Easter weekend, by luck. Together they faced years of skepticism - Marshall eventually drinking the culture - until the proof was undeniable. Warren later moved to rural Western Australia and kept a quiet life.",
        ["Medicine", "Pathology", "21st Century"],
    ),
    "scie-santiago-ramón-y-cajal-217": _entry(
        "The father of modern neuroscience - he proved the brain is made of separate cells, not a continuous web, by drawing them in exquisite detail. A failed art student who was told he had 'no aptitude for drawing,' he won the 1906 Nobel for his neuron doctrine.",
        "Cajal used a silver stain (invented by Golgi, who shared the Nobel but disagreed with him) to reveal individual neurons, then drew them obsessively - his illustrations still appear in textbooks. He showed information flows one way through neurons and that the brain can regenerate connections, anticipating plasticity. His motto: every man can, if he so desires, become the sculptor of his own brain.",
        ["Neuroscience", "Anatomy", "19th Century"],
    ),
    "scie-oliver-sacks-218": _entry(
        "A neurologist who told his patients' stories so well that 'Awakenings' became a Robert De Niro film - people with catatonia awakened by a drug, then losing the miracle. He called himself a 'clinical tales' writer, and his books made the strangest brains feel human.",
        "Sacks treated patients with conditions most doctors never see - someone who couldn't recognize faces, a man who heard music constantly, a surgeon who mistook his wife for a hat. He described their inner worlds without reducing them to cases. 'Awakenings' chronicled L-DOPA reviving encephalitis survivors in the 1960s. He wrote until days before his death in 2015.",
        ["Neuroscience", "Medicine", "20th Century"],
    ),
    "scie-daniel-kahneman-219": _entry(
        "A psychologist who won the Nobel Prize in Economics - the only one ever - for showing humans are systematically irrational: we fear losses twice as much as we enjoy gains. 'Thinking, Fast and Slow' made the science of human error a bestseller.",
        "Kahneman's work, mostly with the late Amos Tversky, catalogued the biases that steer us: anchoring, availability, overconfidence, framing. Prospect theory (1979) explained decisions under risk that standard economics couldn't - people overweight small probabilities and fear losses. The committee cited him for integrating psychological research into economic science.",
        ["Psychology", "Behavioral Economics", "21st Century"],
    ),
    "scie-noam-chomsky-220": _entry(
        "He argued that language is not learned from scratch but grown from an innate grammar hardwired into every human brain - a claim that rewrote linguistics. He's also spent 60 years as the world's most prominent critic of US foreign policy.",
        "Chomsky's 'Syntactic Structures' (1957) showed sentences are generated by recursive rules - a finite system making infinite sentences. His universal grammar says children acquire language too fast and too uniformly for pure imitation; the brain must come pre-equipped. That idea shaped cognitive science, AI, and philosophy - and his political books outsell the linguistics ones.",
        ["Linguistics", "Cognitive Science", "20th Century"],
    ),
    "scie-jean-piaget-221": _entry(
        "The psychologist who mapped how children's minds grow in stages - he published his first scientific paper at age 10, about albino sparrows. His famous conservation tests showed a child believes a taller glass holds more water until a certain age.",
        "Piaget watched his own children and concluded thinking develops in four stages: sensorimotor (0-2), preoperational (2-7), concrete operational (7-11), formal operational (11+). His experiments - pouring water between differently shaped glasses, flattening a clay ball - reveal when children grasp that quantity survives appearance changes. His stage theory shaped classrooms worldwide, though modern research has softened its edges.",
        ["Psychology", "Development", "20th Century"],
    ),
    "scie-edward-witten-222": _entry(
        "The only physicist ever to win the Fields Medal, mathematics' top prize - for uniting string theory into M-theory, an 11-dimensional framework some call the 'theory of everything' candidate. He's been called the greatest living theoretical physicist.",
        "In 1995 Witten showed the five competing string theories were facets of one deeper theory - M-theory - in 11 dimensions, where strings are slices of higher-dimensional membranes. He won the Fields in 1990 for his mathematical insight; string theory's dualities, mirror symmetry, and topological ideas all bear his mark. He started as a history major and worked on McGovern's 1972 campaign.",
        ["Physics", "String Theory", "20th Century"],
    ),
    "scie-cumrun-vafa-223": _entry(
        "A Harvard string theorist from Tehran who invented F-theory and, with Andrew Strominger, used string theory to count the microstates of black holes - explaining their entropy from the inside. He's among the most-cited physicists of his generation.",
        "Vafa's 1996 work with Strominger counted black hole quantum states in string theory and matched Bekenstein-Hawking entropy exactly - one of string theory's most concrete successes. F-theory (1996) offered a compact way to build particle physics from geometry. His papers set the agenda for the 'string landscape' debate about why our universe looks the way it does.",
        ["Physics", "String Theory", "21st Century"],
    ),
    "scie-lisa-randall-224": _entry(
        "She proposed that the universe is a 3D island - a 'brane' - floating in a higher-dimensional space where gravity leaks in from a hidden dimension. The Randall-Sundrum model gave extra dimensions a new job and made her one of physics' best-known voices.",
        "Randall's idea with Raman Sundrum: extra dimensions can be 'warped,' solving the hierarchy problem - why gravity is so weak compared to other forces - without exotic particles. Testable predictions (graviton states) were sought at the LHC. She was the first woman to get tenure in Princeton's physics department and writes for the public about the Large Hadron Collider.",
        ["Physics", "Particle Physics", "21st Century"],
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
        topic["teaser"] = _trim(fix["teaser"])
        topic["exploreAction"]["instruction"] = _trim(fix["instruction"])
        topic["tags"] = fix["tags"]
        changed += 1

    PATH.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"updated {changed} entries")
    return 0


if __name__ == "__main__":
    sys.exit(main())
