#!/usr/bin/env python3
"""Batch: replace the first 40 fake books.json entries with real facts.

The FIXES entries were template-generated (boilerplate teaser, generic
instruction, placeholder tags). This replaces teaser + instruction +
targetName + tags. byline/name/verb/durationMinutes/tier preserved.
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


PATH = Path(__file__).resolve().parent.parent / "app/src/main/assets/topics/books.json"


def _entry(teaser: str, instruction: str, target_name: str, tags: list[str]) -> dict:
    return {
        "teaser": teaser,
        "instruction": instruction,
        "targetName": target_name,
        "tags": tags,
    }


FIXES: dict[str, dict] = {
    "book-sula-1973-133": _entry(
        "Toni Morrison's second novel splits its story between two Black girls in the 'Bottom' neighborhood — Sula Peace, who leaves and returns as the town's scapegoat, and Nel Wright, who stays. Morrison called it a book about good and evil, then admitted she meant 'to ask how evil comes to be.'",
        "Read the first chapter, '1919,' and find the image that starts everything: Shadrack's National Suicide Day. Then read the chapter where the town's 'goodness' is revealed as a reaction to Sula's freedom — Morrison arranges the neighbors' sudden virtue so you see it as their invention, not her absence.",
        "Sula (1973) — chapters '1919' and '1941'",
        ["American", "Literary", "1970s"],
    ),
    "book-song-of-solomon-1977-134": _entry(
        "Morrison's third novel follows Milkman Dead — a Black man in Michigan who sets out to find a bag of gold and instead finds his family's story, ending with a leap that re-enacts his enslaved ancestor's flight. The title is taken from the biblical book; Morrison said the novel is about flight and who gets to take it.",
        "Read the first pages for the tone: a man leaps from the roof of Mercy Hospital, and the narrator treats it as a neighborhood fact, not a mystery. Then follow Milkman's discovery in Shalimar — the children's song about Solomon, the flying African — and notice how the novel's biggest plot reveal is hidden in a playground rhyme.",
        "Song of Solomon (1977) — the opening 'leap' and the Shalimar song",
        ["American", "Literary", "1970s"],
    ),
    "book-midnights-children-1981-135": _entry(
        "Rushdie's Booker-winning novel gives Saleem Sinai, born at the exact stroke of India's independence, telepathic connection to the other 1,000 children of midnight — and makes his life a running allegory for the nation's first 30 years. The novel's conceit: one man's cracked nose contains a country.",
        "Read the opening — Saleem's birth scene, where the switching of two babies at the hospital sets up the whole book. Then read the chapter where the midnight children convene, and notice how Rushdie turns the 'Children of Midnight' conference into a satire of Indian politics. The voice — grand, self-mocking, digressive — is the actual protagonist.",
        "Midnight's Children (1981) — the birth scene and the Children's Conference",
        ["Indian", "Magic Realism", "Postcolonial"],
    ),
    "book-neuromancer-1984-136": _entry(
        "William Gibson coined 'cyberspace' in this novel — 'a consensual hallucination experienced daily by billions' — and invented the cyberpunk template: washed-up console cowboy Case, a mysterious employer, an AI with a plan. It became the first novel to win the Hugo, Nebula, and Philip K. Dick awards in the same year.",
        "Read the famous first line and then the scene where Case first 'jacks in' — Gibson never explains the tech, he just narrates the sensation. Then track the AI's manipulation: Wintermute talks to Case through other characters' bodies, which is the book's real theme in disguise — who is running whom.",
        "Neuromancer (1984) — the opening line and Case's first jack-in",
        ["Science Fiction", "Cyberpunk", "1980s"],
    ),
    "book-beloved-1987-137": _entry(
        "Beloved is based on Margaret Garner, who killed her daughter rather than let her be returned to slavery — Morrison turns that act into a haunted-house story where the ghost is memory itself. It won the Pulitzer in 1988, and when it lost the National Book Award the year before, 48 Black writers published a protest letter.",
        "Read the opening: '124 was spiteful.' The house's ghost is never explained, only experienced. Then read Sethe's account of the escape — the famous 'milk' passage, where her stolen milk becomes the story's central symbol of what slavery took. Notice how the ghost gets a name (Beloved) before the reader learns why.",
        "Beloved (1987) — the '124 was spiteful' opening and the milk passage",
        ["American", "Literary", "Slavery"],
    ),
    "book-the-satanic-verses-1988-138": _entry(
        "Rushdie's novel about two Indian actors falling from a hijacked plane — one becomes an angel, one a devil — sparked the 1989 fatwa that forced him into hiding for nearly a decade. The 'satanic verses' of the title are a disputed episode in early Islam, and the book's dream sequences satirize a prophet named Mahound.",
        "Read the opening fall from the plane — the two men singing 'born again' as they plummet — and notice how the book establishes its miracle/reality ambiguity in the first pages. Then read the chapter where the prophet-figure's followers consult an oracle; the 'satanic verses' episode is the hinge the whole controversy turns on, and Rushdie's framing of it is deliberately ambiguous.",
        "The Satanic Verses (1988) — the opening fall and the 'Mahound' chapters",
        ["British", "Magic Realism", "Controversy"],
    ),
    "book-the-things-they-carried-139": _entry(
        "Tim O'Brien's collection about Vietnam is built on the 'things' soldiers literally carried — rifles, letters, fear, grief — each weighed and named. Its central move is to blur memoir and invention, declaring 'story-truth' more real than what happened, in a war novel that is also a meditation on how we tell war.",
        "Read the title story's inventory — the exact weights of what each man carried — and notice the math: the heavier the object, the more it stands for something unnameable. Then read 'How to Tell a True War Story,' where O'Brien directly argues that a true war story is never about war, and that the one that feels true is usually invented.",
        "The Things They Carried (1990) — 'The Things They Carried' and 'How to Tell a True War Story'",
        ["American", "Vietnam", "Short Stories"],
    ),
    "book-the-english-patient-1992-140": _entry(
        "Ondaatje's Booker winner strands four people in an Italian villa at the end of World War II — a burned man who may be the real Hungarian desert explorer Count Almásy, a Canadian nurse, a Sikh sapper, and a thief. The novel won the Booker and was adapted into the 1996 Oscar-winning film.",
        "Read the opening — the burned man's 'I believe in such things as autumn' — and notice how Ondaatje gives each character a separate history that only intersects in the villa. Then read the desert chapters, where the affair with Katharine is narrated as cartography: the love story is told as a map being drawn.",
        "The English Patient (1992) — the villa's four voices and the desert chapters",
        ["Canadian", "Literary", "WWII"],
    ),
    "book-snow-crash-1992-141": _entry(
        "Neal Stephenson's novel invented a 'Metaverse' before the internet was mainstream — a virtual city where the hero delivers pizza, then fights a virus that infects people through language itself, spreading via an ancient Sumerian brain hack. Hiro Protagonist (yes, that's his name) is a hacker and swordsman who works as a courier.",
        "Read the opening — Hiro is delivering pizza in a car with a nuclear bomb attached — and notice how fast Stephenson establishes the two worlds (real and Metaverse) as equal realities. Then follow the 'neurolinguistic virus' explanation, where the book's wildest idea — language as executable code — is delivered in straight-faced tech prose.",
        "Snow Crash (1992) — the opening delivery and the Sumerian virus chapters",
        ["Science Fiction", "Cyberpunk", "1990s"],
    ),
    "book-infinite-jest-1996-142": _entry(
        "Wallace's 1,079-page novel (plus 388 footnotes, some with their own footnotes) is set in a near-future where years are sponsored — 'Year of the Depend Adult Undergarment' — and centers on a film so entertaining it kills anyone who watches it. Its structure is famously a circle: the novel's first sentence continues its last.",
        "Read the first chapter and the last chapter back to back — they're the same sentence split across the book, which is the novel's whole argument about addiction in a single formal gesture. Then read a block of footnotes end-to-end: Wallace does more world-building in the notes than in the main text, and skipping them is how the book punishes you.",
        "Infinite Jest (1996) — the circular first/last sentences and a footnote chain",
        ["American", "Postmodern", "Addiction"],
    ),
    "book-memoirs-of-a-geisha-143": _entry(
        "Arthur Golden's novel follows Sayuri from a fishing village to the geisha houses of Gion in 1920s–30s Kyoto — and its authenticity was contested from day one: Golden was accused of appropriating the life story of real geisha Mineko Iwasaki, who sued and later published her own memoir, Geisha, a Life.",
        "Read the opening — Sayuri's childhood in Yoroido and the meeting with Mr. Tanaka — and notice how Golden builds the geisha world as a closed system with its own economics. Then read the scene where Sayuri's virginity is auctioned: the book's most controversial episode, and the one Iwasaki said misrepresented her life.",
        "Memoirs of a Geisha (1997) — the auction scene and its controversy",
        ["Japanese", "Historical", "1990s"],
    ),
    "book-the-god-of-small-144": _entry(
        "Arundhati Roy's Booker-winning debut — she had written one novel, and it became a global phenomenon — is set in Kerala in 1969 and told by twins Rahel and Estha, whose childhood is destroyed by the 'Love Laws' that decide who can be loved and how much. Roy spent four years writing it; the advance and rights set records for a first novel.",
        "Read the opening — 'May in Ayemenem is a hot, brooding month' — and notice how Roy withholds the central event while circling it for pages. Then read the chapter where the twins' world comes apart, and track the recurring images (the river, the History House, the word 'later') that carry the book's grief.",
        "The God of Small Things (1997) — the opening and the twins' reckoning",
        ["Indian", "Literary", "Family"],
    ),
    "book-white-teeth-2000-145": _entry(
        "Zadie Smith wrote White Teeth at 24, and the novel — two families, one Jamaican-British, one Bengali-British, meeting in North London — became the defining multicultural novel of the 2000s before its author could legally rent a car. It won multiple first-novel prizes and sold over a million copies.",
        "Read the opening — Archie Jones attempting suicide in a car that won't start, saved by a butcher's shop — and notice how Smith sets the novel's tone: comic, generous, and determined to keep every character in the frame. Then read the Irie/Millat chapters, where the book's argument about inheritance and identity plays out between generations.",
        "White Teeth (2000) — the opening suicide scene and the Irie/Millat chapters",
        ["British", "Comedy", "Multicultural"],
    ),
    "book-atonement-2001-146": _entry(
        "Ian McEwan's novel is a single lie and its lifelong consequences: 13-year-old Briony's false accusation splits two lovers apart before World War II, and the book's final section reveals the narrator has been controlling the story all along. The twist — revealed on the last pages — re-reads every chapter before it.",
        "Read the first part as a closed story — the day at the Tallis house, the fountain, the accusation — and notice how Briony's literary imagination is the engine of the lie. Then read the epilogue, set in 1999, where the narrator confesses what really happened. The book asks you to re-judge everything you just read.",
        "Atonement (2001) — Part One's lie and the 1999 epilogue",
        ["British", "Literary", "WWII"],
    ),
    "book-middlesex-2002-147": _entry(
        "Eugenides's Pulitzer winner is a Greek-American family saga narrated by Cal, a woman raised as Calliope who discovers she has 5-alpha-reductase deficiency — a condition that made her body develop as male at puberty. The novel opens with a line that announces the whole project: 'I was born twice.'",
        "Read the opening — 'I was born twice: first, as a baby girl, on a remarkably smogless Detroit day in January of 1960; and then again, as a teenage boy, in an emergency room near Petoskey, Michigan, in August of 1974' — and notice how Cal treats his intersex body as a family inheritance. Then read the Detroit chapters, where the novel becomes a history of a city as much as a person.",
        "Middlesex (2002) — the 'born twice' opening and the Detroit chapters",
        ["American", "Literary", "Family"],
    ),
    "book-cloud-atlas-2004-148": _entry(
        "David Mitchell's novel stacks six stories from the 1850s to a post-apocalyptic future, each nested inside the next — a journal found in a letter, a letter read in a manuscript, a manuscript seen in a film — then unwinds them in reverse. The Booker-shortlisted structure is the point: 'Souls cross ages like clouds cross skies.'",
        "Read the first and last halves of the nested structure deliberately: the novel cuts each inner story mid-sentence when the next one takes over. Track one 'soul' — the composer Frobisher's music, or the publisher's book — reappearing in each era, which is how Mitchell argues identity persists across time.",
        "Cloud Atlas (2004) — the six nested stories and their shared souls",
        ["British", "Literary", "Structure"],
    ),
    "book-the-road-2006-149": _entry(
        "McCarthy's Pulitzer winner is a father-and-son journey through an ash-grey post-apocalyptic America, written in stripped prose with no quotation marks. The father's only creed — 'carry the fire' — is never explained, which is the book's power: the boy asks what the fire is, and the father says he doesn't know either.",
        "Read the first page and notice what McCarthy withholds: no cause for the apocalypse, no names, no history — just 'the road' and the pushcart. Then read the ending, where the father's death leaves the boy with strangers. The final lines resolve nothing and everything: the fire passes on, unproven.",
        "The Road (2006) — the opening and the ending",
        ["American", "Post-Apocalyptic", "Literary"],
    ),
    "book-wolf-hall-2009-150": _entry(
        "Hilary Mantel's Booker winner follows Thomas Cromwell — the blacksmith's son who became Henry VIII's fixer — through the fall of Anne Boleyn, narrated in the present tense with a deliberately confusing 'he' that refuses to name who's speaking. It won the Booker and its sequel, Bring Up the Bodies, won it again two years later.",
        "Read the opening — the boy Cromwell beaten by his father, the future already decided — and notice how Mantel writes Cromwell from the inside, his memory as a tool. Then read a scene where 'he' shifts without warning between Cromwell and another man: the ambiguity is the technique, forcing you to think like a man who never reveals himself.",
        "Wolf Hall (2009) — the opening and the shifting 'he' chapters",
        ["British", "Historical", "Tudor"],
    ),
    "book-a-visit-from-the-151": _entry(
        "Jennifer Egan's Pulitzer winner is 13 linked stories about music-industry people orbiting one record producer — told in different genres across decades, culminating in a chapter written entirely as a PowerPoint presentation by a 12-year-old. The title's 'goon squad' is time: 'the goon squad... at your back as you age.'",
        "Read the first story, 'Found Objects,' and the last, 'Pure Language,' as a pair — the same characters bookend the book 40 years apart. Then read the PowerPoint chapter: Egan uses the form's awkwardness on purpose, and its emotional payload lands because the format is so reductive. Notice how every story connects through one girl's lost bracelet.",
        "A Visit from the Goon Squad (2010) — 'Found Objects' and the PowerPoint chapter",
        ["American", "Experimental", "Music"],
    ),
    "book-gone-girl-2012-152": _entry(
        "Gillian Flynn's thriller is famous for its midpoint rug-pull: the novel's second half reveals the 'victim' wife Amy is the architect of her own disappearance. The 'cool girl' passage — Amy's manifesto about performing the woman men want — became the book's most-quoted pages and its feminist thesis in one.",
        "Read to the chapter titled 'Amazing Amy' and stop before the reveal: notice how Flynn plants Amy's diary as an unreliable narrator in plain sight. Then read the 'cool girl' monologue aloud — it's the moment the novel stops being a mystery and starts being an argument. The ending offers no justice, only two people trapped in a marriage of mutual performance.",
        "Gone Girl (2012) — the 'cool girl' monologue and the midpoint reveal",
        ["American", "Thriller", "2010s"],
    ),
    "book-americanah-2013-153": _entry(
        "Adichie's novel follows Ifemelu, a Nigerian woman who emigrates to America, becomes a successful blogger on race, and returns to Lagos — a story told against the immigrant's central question: which country is home? Its blog posts ('A Homogenized World of Racism for Dummies') are threaded through the novel verbatim.",
        "Read the opening — Ifemelu in a Baltimore hair salon, getting her hair braided before returning to Nigeria — and notice how the scene sets up the book's thesis: hair, like race, is a system of judgment she can navigate but never escape. Then read one full blog post, which Adichie includes word for word: the novel's sharpest social analysis is meta-fictional.",
        "Americanah (2013) — the hair salon opening and a full blog post",
        ["Nigerian", "Literary", "Immigration"],
    ),
    "book-between-the-world-and-154": _entry(
        "Ta-Nehisi Coates's National Book Award winner is a letter to his teenage son about what it means to live in a Black body in America — a direct descendant of James Baldwin's The Fire Next Time, which Coates cites and revises. Its key image: the 'Dreamers' who built and benefit from a system they refuse to see.",
        "Read the opening letter frame and then the chapter on Prince Jones, the Howard University student killed by police — Coates's most personal chapter, and the hinge where abstract analysis becomes grief. Notice how he refuses consolation: the book's power is that it never promises the son a way out.",
        "Between the World and Me (2015) — the Prince Jones chapter",
        ["American", "Memoir", "Race"],
    ),
    "book-lincoln-in-the-bardo-155": _entry(
        "George Saunders's Booker winner is set in a single night — the bardo, the Tibetan Buddhist limbo between death and rebirth — where Abraham Lincoln's son Willie, newly dead, refuses to move on, surrounded by a chorus of ghosts who narrate the novel together. It's built from hundreds of voices and invented historical quotations.",
        "Read the opening — the ghosts introducing themselves in a crowded choral narration — and notice how Saunders makes you assemble the story from fragments. Then find the invented 'historical sources' footnoted throughout: Saunders fabricates quotes and attributions wholesale, a formal trick that asks whether any account of grief can be authoritative.",
        "Lincoln in the Bardo (2017) — the ghost chorus and the fake citations",
        ["American", "Experimental", "Historical"],
    ),
    "book-normal-people-2018-156": _entry(
        "Rooney's second novel traces Connell and Marianne from a small Irish town through Trinity College Dublin — a love story told almost entirely in what they don't say to each other, and rendered in a prose style so stripped it made 'Rooney realism' a genre. It won the Costa Novel Award and became a TV series.",
        "Read the first chapter — the teenagers' secret relationship in school — and notice how Rooney withholds interiority: the narration stays close to both characters but never lets them know each other's minds, which is the whole tragedy. Then read the 'conversation' chapters at Trinity, where their miscommunications are structured like dialogue that always arrives a beat too late.",
        "Normal People (2018) — the first chapter and a Trinity dialogue scene",
        ["Irish", "Literary", "Romance"],
    ),
    "book-the-testaments-2019-157": _entry(
        "Atwood returned to Gilead 34 years after The Handmaid's Tale with this Booker-winning sequel, told in three voices — two young women raised inside the regime and Aunt Lydia, the system's most feared enforcer, revealed as a secret dissident. It won the Booker jointly with Bernardine Evaristo's Girl, Woman, Other.",
        "Read Aunt Lydia's opening testimony — her 'Ardua Hall' chapters are the book's engine — and notice how Atwood makes the villain sympathetic without forgiving her. Then read the two girls' accounts side by side: the novel is structured as evidence in a trial, and the reader is the jury on whether Gilead can ever be dismantled from within.",
        "The Testaments (2019) — Aunt Lydia's Ardua Hall testimony",
        ["Canadian", "Dystopian", "Sequel"],
    ),
    "book-klara-and-the-sun-158": _entry(
        "Ishiguro's Nobel-winning follow-up to Never Let Me Go is narrated by Klara, an 'Artificial Friend' — a solar-powered robot companion — who watches the world from a shop window and makes bargains with the Sun. The novel is a study of love and sacrifice told from outside the human experience of them.",
        "Read the opening — Klara in the store window, dividing the world into grid squares — and notice how Ishiguro renders her perception as a machine's, then lets it shade into something else. Then read the chapter where Klara negotiates with the Sun to save her human: the book's emotional peak depends on you not knowing whether her sacrifice is real or a delusion.",
        "Klara and the Sun (2021) — the shop window opening and the Sun bargain",
        ["British", "Literary", "Science Fiction"],
    ),
    "book-demon-copperhead-2022-159": _entry(
        "Kingsolver's Pulitzer winner transplants David Copperfield to the mountains of southwest Virginia, where orphaned Demon grows up inside the opioid crisis — Dickens's Victorian poverty reimagined as modern Appalachia's. Kingsolver spent two decades planning the transposition and works in Dickens's plot beats chapter by chapter.",
        "Read the opening — Demon's birth, the death of his mother, the 'copperhead' family — and notice how Kingsolver maps Dickens's characters onto new names. Then read the chapters where Demon discovers the morphine economy: the novel's thesis is that Dickens's world of debt, orphanhood, and addiction survived two centuries to become rural America.",
        "Demon Copperhead (2022) — the opening and the morphine economy chapters",
        ["American", "Literary", "Appalachia"],
    ),
    "book-chain-gang-all-stars-2023-160": _entry(
        "Adjei-Brenyah's novel imagines a near-future America where convicted prisoners fight gladiatorial battles to the death for televised entertainment — and follows two women superstars, 'Hammer' and 'Melancholia,' who have promised each other a final showdown. It was a National Book Award finalist and a literary satire of the prison-industrial complex.",
        "Read the opening fight and the 'spectator experience' framing — Adjei-Brenyah shows the violence through the lens of the corporations selling it. Then read the chapters narrated by the prisoners' families and the abolitionist organizers: the novel keeps switching registers from spectacle to grief to policy, which is its argument in form.",
        "Chain-Gang All-Stars (2023) — the opening bout and the corporate framing",
        ["American", "Dystopian", "Satire"],
    ),
    "book-yellowface-2023-161": _entry(
        "Kuang's novel is a satire of the publishing industry's race politics: white author June Hayward inherits a brilliant manuscript from her dead Asian-American friend, publishes it under the ethnically ambiguous pen name 'Juniper Song,' and watches it become a bestseller — while the truth sits in her desk drawer.",
        "Read the opening — the death of Athena Liu, June's 'friend' and rival, in a freak accident — and notice how Kuang makes you complicit before you've judged: June's voice is so plausible that the theft feels almost reasonable. Then read the internet-cancelation chapters, where the novel dramatizes how Twitter verdicts and book deals move at the same speed.",
        "Yellowface (2023) — the opening theft and the cancellation chapters",
        ["American", "Satire", "Publishing"],
    ),
    "book-north-woods-2023-162": _entry(
        "Daniel Mason's novel tells the story of one house in a Massachusetts forest over 400 years — through Puritan settlers, an apple farmer, a painter, an escaped enslaved man, and a 21st-century couple — in a different genre for each era. The house is the protagonist; the people are its weather.",
        "Read the first chapter — the Puritan couple who build the house — and the last, and notice the recurring image that bookends them: the apple tree. Then read the chapter narrated by the house itself, where Mason breaks the historical pattern on purpose. The novel's argument is ecological: a place outlives every story told about it.",
        "North Woods (2023) — the opening and the house-narrated chapter",
        ["American", "Literary", "Experimental"],
    ),
    "book-wellness-2023-163": _entry(
        "Nathan Hill's novel — his follow-up to The Nix — follows a married couple, Jack and Elizabeth, whose relationship is examined through the wellness industry, internet placebo effects, and the science of belief. Its chapters are built around the couple's online relationship history, and Hill's research on placebo became the novel's spine.",
        "Read the opening — the couple's affair 'ending' over a text message sent to the wrong person — and then the chapter where Hill explains the placebo research that underpins their marriage therapy. Notice the structure: every chapter pairs a present-tense relationship scene with a past-tense explanation of why we believe what we believe.",
        "Wellness (2023) — the wrong-text opening and the placebo chapters",
        ["American", "Literary", "Marriage"],
    ),
    "book-birnam-wood-2023-164": _entry(
        "Eleanor Catton's Booker-winning follow-up to The Luminaries is a thriller about a guerrilla-gardening collective that trespasses onto a billionaire's New Zealand estate — and gets tangled with an eco-terrorist who may be setting traps. The title nods to Macbeth: Birnam Wood comes to Dunsinane, but here the wood is activists.",
        "Read the opening — the collective's founding and its moral compromises — and notice how Catton withholds the eco-terrorist's identity for hundreds of pages while making every character plausibly guilty. Then read the scene where the collective votes to accept the billionaire's money: the novel's whole argument about ethics under capitalism in one meeting.",
        "Birnam Wood (2023) — the collective's founding and the funding vote",
        ["New Zealand", "Thriller", "Environmental"],
    ),
    "book-the-wager-2023-165": _entry(
        "David Grann's nonfiction account of the 1742 wreck of the HMS Wager — a British warship that ran aground off Patagonia, whose crew split into mutiny and cannibalism — won the Baillie Gifford Prize and became a bestseller. The survivors' return to England produced two contradictory accounts, and the Admiralty's trial became a national sensation.",
        "Read the opening — the wreck and the two survival narratives — and notice how Grann sets up the book's central problem: the same events, told by officers and crew, produce irreconcilable truths. Then read the mutiny chapters, where the book's claim — that the Wager's story is a microcosm of empire — becomes explicit.",
        "The Wager (2023) — the wreck and the two rival accounts",
        ["Nonfiction", "Maritime", "History"],
    ),
    "book-king-a-life-2023-166": _entry(
        "Jonathan Eig's Pulitzer-winning biography of Martin Luther King Jr. was the first in decades to draw on the newly opened archives — including King's own FBI file and recordings of his marital infidelities, which Eig publishes with context rather than scandal. It restores King's radicalism: the Cold War warrior king is largely a later invention.",
        "Read the opening — King's childhood in Atlanta, including the suicide attempt of his grandmother — and notice how Eig builds the man before the myth. Then read the chapters on the Montgomery bus boycott and the FBI's campaign against him: the book's argument is that King's government-sanctioned enemies did more to shape his fate than any single opponent.",
        "King: A Life (2023) — the Atlanta childhood and the FBI chapters",
        ["Nonfiction", "Biography", "Civil Rights"],
    ),
    "book-the-vaster-wilds-2023-167": _entry(
        "Lauren Groff's novel is a survival story set in 1609 colonial Virginia: a servant girl flees the Jamestown fort during a starvation winter and walks north through a hostile wilderness that is also a wonder. Groff wrote it as a deliberate counter-narrative to the myth of American origin — the story of one anonymous girl who escapes rather than conquers.",
        "Read the opening — the girl's escape from the fort — and notice how Groff renders the wilderness as both threat and sanctuary, alternating terror with awe. Then read the chapter where she finds the abandoned indigenous village: the novel's argument — that the land was already a country with its own history — lands in that empty clearing.",
        "The Vaster Wilds (2023) — the escape and the abandoned village",
        ["American", "Historical", "Literary"],
    ),
    "book-western-lane-2023-168": _entry(
        "Chetna Maroo's Booker-shortlisted debut is a slim novel about an 11-year-old Gujarati girl in London who channels her grief for her mother into competitive squash. The book's prose is as spare as its heroine's game — every sentence is a rally, and the squash court becomes a place where the family's silences are played out.",
        "Read the opening — the family's move after the mother's death, the father's stern training sessions — and notice how little Maroo explains: grief is expressed only through the sport. Then read the tournament chapters, where the novel's technical descriptions of squash become a language for things the characters cannot say.",
        "Western Lane (2023) — the training opening and the tournament chapters",
        ["British", "Literary", "Debut"],
    ),
    "book-old-gods-time-2023-169": _entry(
        "Sebastian Barry's Booker-shortlisted novel follows Tom Kettle, a retired Irish policeman living a monkish life in a Dublin castle, whose quiet is broken by two detectives who bring up a case from decades past. It's a crime novel that is really a novel about memory, trauma, and the institutional abuses of the Catholic Church in Ireland.",
        "Read the opening — Tom's solitary life in the castle, the sea outside — and notice how Barry establishes his unreliable, drifting narration: Tom's memory is the novel's landscape, and the plot arrives through it. Then read the chapters where the case resurfaces, and notice that the crime is never the point — what happened after it is.",
        "Old God's Time (2023) — the castle opening and the case's return",
        ["Irish", "Literary", "Crime"],
    ),
    "book-the-guest-2023-170": _entry(
        "Emma Cline's follow-up to The Girls follows Alex, a 22-year-old adrift on Long Island's wealthy summer circuit, who has been told to leave and instead keeps orbiting the people she's crashed with — borrowing, conning, and performing her way through a season. The novel is a study of a woman who is only ever 'the guest.'",
        "Read the opening — Alex swimming at dawn, the text message that sets the plot in motion — and notice how Cline establishes her as a professional at being tolerated. Then read the chapter where Alex invents a story about her past for a new mark: the novel's whole method is watching her revise herself, and it asks how much of any identity is performance.",
        "The Guest (2023) — the opening swim and the reinvention chapters",
        ["American", "Literary", "Summer"],
    ),
    "book-tremor-2023-171": _entry(
        "Teju Cole's novel follows Tunde, a Nigerian-born art professor at Harvard, through a year of lectures, galleries, and travels — interleaved with accounts of empire, violence, and the objects museums keep. Cole writes the novel as a series of scenes and digressions that refuse a single plot, in the tradition of his Open City.",
        "Read the opening — Tunde in a Harvard lecture hall, a slide of a Benin bronze on the screen — and notice how Cole immediately links the art object to the colonial violence that took it. Then read the chapters on the Benin bronzes' repatriation, where the novel's essayistic strain becomes its argument: whose history does the museum display?",
        "Tremor (2023) — the Benin bronze lecture and the repatriation chapters",
        ["Nigerian-American", "Literary", "Essayistic"],
    ),
    "book-everythings-fine-2023-172": _entry(
        "Cecilia Rabess's debut follows Jess, a young Black woman who takes a job at Goldman Sachs and falls for a white co-worker who votes against everything she believes — a 'love across the aisle' romance that was itself controversial before publication, with a publisher's sensitivity read generating headlines. The novel's debate is the point: can intimacy survive politics?",
        "Read the opening — Jess's first day at the bank, the whiteness of the trading floor — and notice how Rabess renders the workplace as a system of micro-judgments. Then read the scenes where Jess and Josh argue politics in private: the novel deliberately stages the same fights its readers were having about it, and leaves the central question — is Josh a good man or a comfortable bigot? — unresolved.",
        "Everything's Fine (2023) — the first-day opening and the Josh arguments",
        ["American", "Romance", "Debate"],
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
        topic["exploreAction"]["targetName"] = fix["targetName"]
        topic["tags"] = fix["tags"]
        changed += 1

    PATH.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"updated {changed} entries")
    return 0


if __name__ == "__main__":
    sys.exit(main())
