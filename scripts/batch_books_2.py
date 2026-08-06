#!/usr/bin/env python3
"""Batch: replace books.json fakes #2 — ids 173–216 (The New Naturals → Tom Lake).

Same contract as batch_books_1.py. Cap 450 (SCHEMA.md).
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
    "book-the-new-naturals-2023-173": _entry(
        "Gabriel Bump's National Book Award finalist imagines a group of Black Americans who abandon the country to found a utopian community in an abandoned mall — and the novel tracks the dream from its founding euphoria to its unraveling. It's a comedy about utopia, written in the shadow of every failed paradise that came before.",
        "Read the opening — the founding of 'The New Naturals' and the manifesto moment — and notice how Bump sets up the community's fatal tension: everyone agrees on the dream, nobody agrees on the rules. Then read the chapter where the mall's first dispute erupts, and see how the novel turns political argument into comedy without letting it stop being an argument.",
        "The New Naturals (2023) — the founding and the first dispute",
        ["American", "Utopia", "Satire"],
    ),
    "book-pride-and-prejudice-1813-174": _entry(
        "Austen's novel — originally titled First Impressions — opens with one of the most famous sentences in English, and its heroine Elizabeth Bennet rejects a marriage proposal from the wealthiest man she's ever met, then falls in love with him over two hundred pages of misunderstanding. The novel invented the enemies-to-lovers plot and has never been out of print.",
        "Read the first chapter aloud — Mrs. Bennet's scheming dialogue — and notice how Austen establishes character entirely through speech. Then read the letter Darcy writes Elizabeth, the novel's hinge: every line re-reads what came before. Austen herself said she had no 'fondness' for Darcy; the novel is funnier if you remember the author is on Elizabeth's side, not his.",
        "Pride and Prejudice (1813) — the first chapter and Darcy's letter",
        ["British", "Classic", "Romance"],
    ),
    "book-crime-and-punishment-1866-175": _entry(
        "Dostoevsky's novel gives a poor St. Petersburg student a theory — that extraordinary men may break the law — and then makes him test it with an axe. Raskolnikov's murder of a pawnbroker occupies the first part; the other five parts are the punishment, which is not prison but his own unraveling mind.",
        "Read just the murder scene and notice the details Dostoevsky withholds: the axe is described obsessively, but the violence is almost skipped, because the book isn't about the crime. Then read the epilogue, where Raskolnikov — in prison, convicted — finally collapses and accepts love. The novel argues the punishment was always internal, and the legal sentence is an afterthought.",
        "Crime and Punishment (1866) — the murder scene and the epilogue",
        ["Russian", "Classic", "Psychology"],
    ),
    "book-heart-of-darkness-1899-176": _entry(
        "Conrad's novella is a frame within a frame: a sailor on a Thames boat tells the story of Marlow's journey up the Congo to find Kurtz, an ivory agent who has gone native — and it became the most argued-over short novel in English, the source of 'The horror! The horror!' and of the critique that it both exposed and reproduced colonialism.",
        "Read the opening — Marlow's listeners on the Thames, the city as 'the beginning of the world' — and notice how Conrad sets up the novel's central irony: the 'darkness' is in Europe all along. Then read Kurtz's report to the International Society, with its scrawled postscript — 'Exterminate all the brutes!' — which is the whole book in one line.",
        "Heart of Darkness (1899) — the frame opening and Kurtz's report",
        ["British", "Classic", "Colonialism"],
    ),
    "book-to-the-lighthouse-1927-177": _entry(
        "Woolf's novel is one family and one question — will the Ramsays ever sail to the lighthouse? — told in three movements, including 'Time Passes,' the famous middle section where a decade passes in twenty pages while the house decays. The novel is the masterpiece of her stream-of-consciousness method.",
        "Read the first movement's opening — Mrs. Ramsay's consciousness as the house's center — and notice how Woolf moves between minds without scene breaks. Then read 'Time Passes' in one sitting: the bracketed death notices ('[Mr. Ramsay, stumbling along a passage one dark morning...]') are the novel's most radical formal gesture, grief reduced to a parenthesis.",
        "To the Lighthouse (1927) — the opening and the 'Time Passes' section",
        ["British", "Modernist", "Literary"],
    ),
    "book-the-grapes-of-wrath-178": _entry(
        "Steinbeck's Pulitzer winner follows the Joad family from the Oklahoma dust bowl to California along Route 66 — and its ending, with Rose of Sharon nursing a starving stranger, scandalized and moved the country in equal measure. It sold 430,000 copies in its first year and was banned in its own state's farm country.",
        "Read the intercalary chapters — the dust storm, the used-car lot, the turtle — and notice how Steinbeck alternates the Joads' story with a sociological chorus that widens the book into the whole migration. Then read the final chapter twice: the milk poured on the ground by the California growers is the novel's argument, and Rose of Sharon's last gesture is its answer.",
        "The Grapes of Wrath (1939) — the intercalary chapters and the ending",
        ["American", "Classic", "Depression"],
    ),
    "book-1984-1949-179": _entry(
        "Orwell's novel gave the world Big Brother, Newspeak, and doublethink — the vocabulary of totalitarianism itself — and it remains the best-selling dystopia ever written. Winston Smith's rebellion lasts exactly as long as it takes the Party to catch him, and the ending, in which he finds he loves Big Brother, is the bleakest in the canon.",
        "Read the opening — the telescreen's 'Big Brother is watching you' — and then the appendix, which is written in standard English and dated years after the novel's events: the appendix quietly argues that Newspeak failed, which changes the ending's meaning. Then read the torture scene in Room 101 and decide whether the novel allows any hope at all.",
        "1984 (1949) — the opening and the Room 101 scene",
        ["British", "Dystopian", "Classic"],
    ),
    "book-invisible-man-1952-180": _entry(
        "Ellison's only completed novel opens with its narrator underground, 'invisible... simply because people refuse to see me' — and the 1,369 light bulbs he's stolen electricity to light are the novel's central image: he is making himself visible by force. It won the National Book Award in 1953, the first by a Black writer.",
        "Read the Prologue and the Epilogue together — they're the same man in the same basement, and the book between them is the explanation of how he got there. Then read the 'battle royal' scene, where the narrator is made to fight blindfolded for white men's entertainment: the novel's thesis — that Black Americans are given 'opportunity' only to be managed — is stated in that boxing ring.",
        "Invisible Man (1952) — the Prologue and the battle royal",
        ["American", "Classic", "Race"],
    ),
    "book-lord-of-the-flies-181": _entry(
        "Golding's novel strands a group of British schoolboys on an island and watches civilization evaporate in weeks: the conch shell that orders their meetings loses authority, the signal fire goes out, and the 'beast' they fear turns out to be the darkness in themselves. It was rejected by 21 publishers before becoming a school staple.",
        "Read the opening — Ralph and Piggy's first meeting, the conch discovery — and track the conch's power as the novel's measure of order: it works until it doesn't. Then read the chapter where the boys kill Simon, mistaking him for the beast: the novel's turning point, and the moment Golding stops being allegorical and becomes something harder to look at.",
        "Lord of the Flies (1954) — the conch opening and the Simon scene",
        ["British", "Classic", "Allegory"],
    ),
    "book-things-fall-apart-1958-182": _entry(
        "Achebe wrote Things Fall Apart at 28 as a deliberate answer to Heart of Darkness — the story of an Igbo village told from the inside, in which the title's collapse is both a man's and a culture's. Okonkwo, a wrestler who built his life on repressing everything he fears in his father, is destroyed by the arrival of missionaries and by his own rigidity.",
        "Read the opening — Okonkwo's fame from wrestling, his dread of his lazy father — and notice how Achebe embeds Igbo proverbs, gods, and customs as the narrative's natural furniture, not exotica. Then read the final chapter, where the District Commissioner's thoughts close the novel: the colonizer's book about Okonkwo's world is a paragraph, and the joke is on him.",
        "Things Fall Apart (1958) — the opening and the final chapter",
        ["Nigerian", "Classic", "Postcolonial"],
    ),
    "book-catch-22-1961-183": _entry(
        "Heller's novel invented the paradox that gave it its name — a soldier who asks to be grounded for madness proves he's sane by asking, so he can't be grounded — and turned the World War II novel into a time-loop comedy of bureaucratic terror. Its nonlinear structure was a scandal in 1961 and a template by 1970.",
        "Read the first chapter and the last chapter back to back — Heller begins in the middle and ends with the event that explains everything: Snowden, and the secret Yossarian was never able to tell. Then read the catch-22 explanation scene aloud: the logic is airtight and insane at once, which is the novel's whole argument about institutions.",
        "Catch-22 (1961) — the opening and the Snowden chapter",
        ["American", "Satire", "WWII"],
    ),
    "book-the-bell-jar-1963-184": _entry(
        "Plath's only novel — published under the pseudonym Victoria Lucas a month before her death — follows Esther Greenwood, a college student in 1950s New York, through her internship at a fashion magazine and her slow descent into breakdown. Its famous fig-tree passage imagines all her possible futures as figs that rot as she hesitates to choose.",
        "Read the opening — Esther's New York internship, the Rosenberg executions as backdrop — and notice how Plath renders the 1950s woman's options as a series of doors closing. Then read the fig-tree paragraph: it's the novel's thesis in miniature, and it explains the 'bell jar' — the suffocation of every possibility by the pressure to choose one.",
        "The Bell Jar (1963) — the opening and the fig-tree passage",
        ["American", "Classic", "Mental Health"],
    ),
    "book-slaughterhouse-five-1969-185": _entry(
        "Vonnegut's anti-war novel is built from his own experience as a POW in Dresden during the firebombing — he survived in a slaughterhouse, which is the book's title and its setting. Billy Pilgrim has 'come unstuck in time,' and the novel's response to mass death is the refrain 'So it goes.'",
        "Read the first chapter — Vonnegut as himself, trying to write the book for 23 years — and notice how it breaks the novel's frame before the novel starts. Then read the Dresden chapters, where the horror is delivered in the same flat tone as the jokes: the novel's method is the point, and 'So it goes' is not resignation but a refusal to dignify death with narrative.",
        "Slaughterhouse-Five (1969) — the frame chapter and the Dresden chapters",
        ["American", "WWII", "Anti-War"],
    ),
    "book-the-bluest-eye-1970-186": _entry(
        "Morrison's debut is narrated over the frame of the Dick and Jane schoolbook primer, which it dismembers sentence by sentence — because the story is the anti-primer: Pecola Breedlove, an 11-year-old Black girl in 1940s Ohio, believes she will be loved only if she has blue eyes. It is a novel about how standards of beauty are taught, and who pays.",
        "Read the primer passages that open each section — 'Here is the house...' — and notice how Morrison's version of the sentences degrades as the book proceeds, mirroring what the ideal does to Pecola. Then read the chapter where Pecola's father rapes her, narrated in the same lyrical register as the novel's beauty: the horror is that Morrison refuses to separate the tenderness from the violation.",
        "The Bluest Eye (1970) — the primer frame and the Cholly chapter",
        ["American", "Classic", "Race"],
    ),
    "book-the-gulag-archipelago-1973-187": _entry(
        "Solzhenitsyn's three-volume history of the Soviet prison camp system is subtitled 'An Experiment in Literary Investigation' and is built from the testimony of 227 witnesses — including himself. The opening volume was smuggled to the West and published in 1973; Solzhenitsyn was stripped of his citizenship the following year.",
        "Read the opening — the author's own arrest in 1945, for a joke in a letter — and the famous first line of the second part: 'Bless you, prison.' Then read the statistics chapter, where Solzhenitsyn assembles the arithmetic of the camps: he argues the total was tens of millions, and the book's power is that each number has a testimony attached to it.",
        "The Gulag Archipelago (1973) — the arrest opening and the arithmetic",
        ["Russian", "History", "Testimony"],
    ),
    "book-the-hitchhikers-guide-to-188": _entry(
        "Douglas Adams's novel began as a radio series and answers the ultimate question of life, the universe, and everything with the number 42 — then reveals that the question itself was lost when Earth was demolished to make way for a hyperspace bypass. The Guide's advice on towels and the 'Don't Panic' cover made it a cult object before it was a bestseller.",
        "Read the opening — Arthur Dent's house being demolished for a bypass, the prelude to Earth's demolition — and notice how Adams sets the novel's rule: the mundane and the cosmic run on the same absurd logic. Then read the scene where the supercomputer Deep Thought announces 42 after 7.5 million years of computation: the joke is that the answer is useless without the question.",
        "The Hitchhiker's Guide to the Galaxy (1979) — the opening and Deep Thought",
        ["British", "Science Fiction", "Comedy"],
    ),
    "book-the-color-purple-1982-189": _entry(
        "Alice Walker's Pulitzer winner is written entirely as letters — first to God, then to her sister — from Celie, a poor Black woman in rural Georgia whose letters begin after she is married off at 14. It was the first novel by a Black woman to win the Pulitzer, and its frankness about abuse made it one of the most banned books of the 1980s.",
        "Read the first three letters and notice what Celie does not say: the abuse is reported in the same flat register as the weather, and the letters to God are her only privacy. Then read the letters after Celie meets Shug Avery, where the novel's radical claim — that love can be a way out — begins to take hold. The epistolary form is the point: the book is about who gets to have a voice.",
        "The Color Purple (1982) — the opening letters and the Shug chapters",
        ["American", "Epistolary", "Classic"],
    ),
    "book-the-handmaids-tale-1985-190": _entry(
        "Atwood wrote Gilead — a theocratic United States where fertile women are assigned as 'handmaids' — by inventing nothing: every practice in the novel has a historical precedent she documented. Its narrator Offred tells her story in fragments to an unknown listener, and the novel's famous final section reveals the whole account as a historical transcript.",
        "Read the opening — the gymnasium scene, the women in red — and notice how Atwood withholds the world's rules and lets you assemble them. Then read the 'Historical Notes' epilogue, set 200 years later, where academics discuss Offred's testimony as a relic: the ending re-frames everything as evidence, and the novel's warning is that the scholars' comfort is the same comfort that let Gilead happen.",
        "The Handmaid's Tale (1985) — the opening and the Historical Notes",
        ["Canadian", "Dystopian", "Classic"],
    ),
    "book-norwegian-wood-1987-191": _entry(
        "Murakami's fifth novel was his breakthrough in Japan — selling millions, largely because it was, unusually for him, a realistic story: a love triangle among students in 1960s Tokyo, narrated by Toru Watanabe remembering his friend Kizuki's suicide and his love for Kizuki's girlfriend Naoko. Its first line — 'I was thirty-seven, then' — begins the memory.",
        "Read the opening — Watanabe on a plane, hearing 'Norwegian Wood' by the Beatles, and the memory flooding back — and notice how Murakami frames the whole novel as recollection. Then read the scenes at Naoko's mountain sanatorium, where the novel's theme — that death is 'not the opposite of life but a part of it' — is stated almost as a thesis.",
        "Norwegian Wood (1987) — the opening and the sanatorium chapters",
        ["Japanese", "Literary", "Romance"],
    ),
    "book-the-remains-of-the-192": _entry(
        "Ishiguro's Booker winner is the memoir of Stevens, an aging English butler driving through the countryside and remembering his service at Darlington Hall — and gradually revealing that the 'greatness' he served was collaboration with fascism, and that his one possible love was lost to his own professional discipline. The novel is a tragedy told in the most polite voice in literature.",
        "Read the opening — Stevens's employer, the American Mr. Farraday, teasing him about his dignity — and notice the voice: perfectly composed, and every evasion is visible through it. Then read the scene at the pier at the end, where Stevens thinks about his 'dignity' and his life: Ishiguro gives him the book's only moment of near-honesty, and the novel ends without him acting on it.",
        "The Remains of the Day (1989) — the opening and the pier scene",
        ["British", "Literary", "Memory"],
    ),
    "book-american-psycho-1991-193": _entry(
        "Bret Easton Ellis's novel about Patrick Bateman, a Wall Street banker who tortures and murders with an accountant's obsession, was condemned before publication — its publisher dropped it, and the controversy made it famous. Its method is the point: the brand-name liturgy of Bateman's world is rendered in the same deadpan as the violence, which is the novel's argument that the two are one system.",
        "Read the opening chapters — the restaurant reviews, the business cards, the morning routine — and notice how Ellis never signals irony: the obsession with status is narrated exactly like the murder scenes. Then read the chapter where Bateman describes his 'deepest, darkest fantasies' with his therapist: the novel's central ambiguity — is any of it real? — is never resolved, which is the whole design.",
        "American Psycho (1991) — the morning routine and the therapy chapter",
        ["American", "Satire", "Dark"],
    ),
    "book-the-virgin-suicides-1993-194": _entry(
        "Eugenides's debut is narrated by a collective 'we' — the neighborhood boys who never stop investigating the Lisbon sisters, five girls who kill themselves one by one in 1970s suburban Detroit. The novel is told entirely in retrospect, from the boys' obsession, and it never explains the suicides, which is the point.",
        "Read the opening — Cecilia's first attempt, the party where she jumps — and notice how Eugenides withholds the sisters' interiority: the 'we' narrator can observe everything and understand nothing. Then read the ending, where the boys recover the sisters' diaries, journals, and records: the archive is all they ever had, and the novel's grief is that the explanation is in it and unreachable at once.",
        "The Virgin Suicides (1993) — the opening and the diaries ending",
        ["American", "Literary", "Suburbia"],
    ),
    "book-trainspotting-1993-195": _entry(
        "Irvine Welsh's novel about Edinburgh heroin addicts is written in a phonetic Scots dialect so dense it comes with its own reading challenge — and its opening monologue, 'Choose Life,' became one of the most-quoted passages in modern Scottish literature. It's a comedy of despair, narrated from inside the addiction it neither glamorizes nor judges.",
        "Read the opening — Renton's 'Choose Life' monologue, delivered to no one — and notice how Welsh's phonetics force you to read aloud in your head: the dialect is the novel's method of making the world feel lived-in. Then read the scene where the crew discusses the 'Begbie problem' — the novel's comedy and its menace in the same paragraph.",
        "Trainspotting (1993) — the 'Choose Life' monologue and the Begbie scenes",
        ["Scottish", "Literary", "Addiction"],
    ),
    "book-fight-club-1996-196": _entry(
        "Palahniuk's novel about an insomniac office worker and the soap salesman who gives him a new religion — fight club, then Project Mayhem — became a generation's counterculture text and a film that outlived the book. Its twist, revealed in the final third, is that the narrator and Tyler Durden are the same man: the novel is a first-person account of schizophrenia, and the famous rule is that you don't talk about it.",
        "Read the first chapter — the narrator on the roof of the Parker-Morris building, Tyler's gun in his mouth — and notice how the novel begins at its own ending. Then re-read the book's first half after the reveal: Palahniuk plants the split in plain sight ('I know this because Tyler knows this'). The novel's argument is that consumer boredom and masculine violence are two ends of one system.",
        "Fight Club (1996) — the opening and the twist's setup chapters",
        ["American", "Satire", "Cult"],
    ),
    "book-harry-potter-1997-197": _entry(
        "Rowling's first novel — rejected by 12 publishers — introduced Harry Potter, the boy who discovers at 11 that he's a wizard and that the parents he thought died in a car crash were murdered by the most feared wizard in history. The series sold over 500 million copies and turned a children's book into a global industry.",
        "Read the opening — the Dursleys, the letters arriving impossibly through every crack — and notice how Rowling sets the novel's engine: the ordinary world and the magical world run on parallel absurd rules. Then read the chapter where Harry first sees Diagon Alley and hears his own name: the world-building is the plot, and the mystery of the Boy Who Lived is the spine.",
        "Harry Potter and the Philosopher's Stone (1997) — the letters and Diagon Alley",
        ["British", "Fantasy", "Middle Grade"],
    ),
    "book-disgrace-1999-198": _entry(
        "Coetzee's double Booker winner — he won the prize twice, and this was the second — follows David Lurie, a disgraced professor who moves to his daughter's small farm in South Africa, where a violent attack forces him to confront everything he assumed about power, race, and his own body. Its title names its subject: the novel is about what it means to lose standing and keep living.",
        "Read the opening — Lurie's seduction of a student, narrated with a coldness that implicates the reader — and notice how Coetzee refuses to let the novel become a redemption story. Then read the chapters after the attack on the farm, where Lurie's daughter refuses his version of events: the novel's hardest argument is that his disgrace is deserved and his care is also real.",
        "Disgrace (1999) — the opening and the post-attack chapters",
        ["South African", "Literary", "Booker"],
    ),
    "book-the-corrections-2001-199": _entry(
        "Franzen's third novel — a National Book Award winner that made him a celebrity after a public fight with Oprah — follows the Lambert family's final Christmas as the parents age and the three adult children fail at their own lives. The 'corrections' of the title are both the market's and the family's: everyone is being corrected, and no one is being saved.",
        "Read the opening — Enid Lambert's desperation about Christmas, her husband Alfred's tremor — and notice how Franzen switches into each character's consciousness with no warning, giving every family member a full interior life. Then read the chapter where Chip's 'academic correction' career collapses: the novel's comedy and its grief are the same machinery.",
        "The Corrections (2001) — the opening and the Chip chapters",
        ["American", "Literary", "Family"],
    ),
    "book-life-of-pi-2001-200": _entry(
        "Yann Martel's Booker winner is the story of Pi Patel, a zookeeper's son shipwrecked in the Pacific with a Bengal tiger named Richard Parker — 227 days on a lifeboat, told with such straight-faced realism that the tiger almost becomes plausible. Its final section offers a second version of the story, and the choice between them is the novel.",
        "Read the opening — Pi's childhood at the zoo, the animals' escape attempts — and notice how the book earns its later symbolism: the zoo chapters are the novel's philosophy disguised as natural history. Then read the final version of the shipwreck, where no tiger appears: the novel's famous question — which story do you prefer? — is asked directly, and the reader's answer is the book's meaning.",
        "Life of Pi (2001) — the zoo opening and the two endings",
        ["Canadian", "Literary", "Survival"],
    ),
    "book-the-kite-runner-2003-201": _entry(
        "Hosseini's debut — the first novel in English by an Afghan author to become a global bestseller — follows Amir, a privileged boy in 1970s Kabul, and Hassan, the Hazara servant's son who is his best friend and his shame. The kite tournament of the title is where Amir fails Hassan, and the novel spends the rest of its length on the debt.",
        "Read the opening — Amir in California, a phone call from Pakistan that 'made me who I am today' — and notice how the novel frames itself as a confession. Then read the kite tournament and its aftermath, the novel's hinge: the moment Amir chooses not to save Hassan is rendered in a single image (the blue kite, the alley), and every later chapter is that image being paid back.",
        "The Kite Runner (2003) — the opening and the kite tournament",
        ["Afghan", "Literary", "Redemption"],
    ),
    "book-never-let-me-go-202": _entry(
        "Ishiguro's sixth novel is narrated by Kathy H., recalling her childhood at Hailsham, an idyllic English boarding school — and slowly revealing that she and her friends are clones, raised to donate organs until they 'complete.' The novel's horror is that nobody in it finds this monstrous, which is the point.",
        "Read the opening — Kathy's casual description of being a 'carer' and the word 'donations' — and notice how Ishiguro withholds the book's premise so gently that you may not register it at first. Then read the scene where the students learn the truth from Miss Emily and Madame: the revelation is delivered in the same quiet register as everything else, which is the novel's whole method of making you complicit.",
        "Never Let Me Go (2005) — the opening and the Miss Emily revelation",
        ["British", "Literary", "Science Fiction"],
    ),
    "book-the-brief-wondrous-life-203": _entry(
        "Junot Díaz's Pulitzer winner tells the story of Oscar de León, an overweight Dominican-American sci-fi nerd in New Jersey who wants nothing more than love — narrated by his cynical ex-roommate Yunior, who interrupts the story with footnotes about Dominican history, 'fukú' curses, and the Trujillo dictatorship. It mixes Spanish, sci-fi, and history into a new kind of novel.",
        "Read the opening — Yunior's first line about Oscar ('Our hero was not one of those Dominican cats...') — and notice how the footnotes work: they break the story to explain the Dominican context, which is the novel's argument that Oscar's family history and his comic-book life are the same story. Then read the chapters about Oscar's mother, Beli, in the Dominican Republic: the novel's heart is in the past its narrator keeps footnoting.",
        "The Brief Wondrous Life of Oscar Wao (2007) — the opening and the Beli chapters",
        ["Dominican-American", "Literary", "Pulitzer"],
    ),
    "book-the-help-2009-204": _entry(
        "Stockett's debut — set in Jackson, Mississippi, in 1962 — is narrated by three voices: Skeeter, a young white woman who wants to be a writer, and two Black maids, Aibileen and Minny, who agree to tell their stories for her book. It sold 10 million copies and became a film, and its central controversy — a white author writing Black maids' voices — is part of its history.",
        "Read Aibileen's first chapter and notice the 'story' she tells the white child she raises: 'You is kind. You is smart. You is important.' The novel's tenderness and its problem are in that sentence together. Then read Minny's pie chapter, the novel's most famous set piece, where the comedy and the fury are the same scene.",
        "The Help (2009) — Aibileen's first chapter and Minny's pie",
        ["American", "Historical", "Civil Rights"],
    ),
    "book-swamplandia-2011-205": _entry(
        "Karen Russell's Pulitzer-finalist debut is narrated by Ava Bigtree, 12, whose family runs Swamplandia!, a failing alligator-wrestling theme park in the Florida Everglades. When her mother dies, the family scatters, and Ava sets off alone into the swamp to find the 'underworld' — a journey that is both a literal trip and a childhood ending.",
        "Read the opening — the Bigtree alligator act, the park's golden age — and notice how Russell makes the gothic and the comic share one register: the park's decline is narrated with the same wonder as its rise. Then read the chapters where Ava paddles into the swamp with the mysterious 'Oswald the Bird-Man': the novel's ending refuses to say what was real, which is the whole design.",
        "Swamplandia (2011) — the opening and the underworld journey",
        ["American", "Literary", "Magic Realism"],
    ),
    "book-the-goldfinch-2013-206": _entry(
        "Donna Tartt's Pulitzer winner opens with a terrorist bomb in a museum that kills Theo Decker's mother and leaves him with a stolen painting — Carel Fabritius's The Goldfinch — which he keeps for the rest of the novel, across decades of grief, crime, and obsession. The novel is 771 pages about what art is for, disguised as a thriller.",
        "Read the opening — the museum, the bomb, the painting's chain being broken — and notice how Tartt makes the theft almost involuntary: Theo walks out with the painting because he can't put it down. Then read the ending, where Theo, having lost everything, delivers the book's thesis about art ('a great sorrow, and one that we... can transform'): the whole novel is an argument for why we keep beautiful things.",
        "The Goldfinch (2013) — the museum opening and the ending",
        ["American", "Literary", "Art"],
    ),
    "book-all-the-light-we-207": _entry(
        "Doerr's Pulitzer winner alternates between Marie-Laure, a blind French girl who flees Paris with a diamond hidden in her model of the city, and Werner, a German boy who becomes a radio operator for the Nazis. Their paths converge in the walled city of Saint-Malo in 1944, and the novel's title names its method: light as physics, light as hope, light as what we cannot see.",
        "Read the opening — Marie-Laure's father building her the model of the city, the braille labels — and notice how Doerr gives her a different sense of the world than sight. Then read the radio chapters, where Werner's gift for radios is both his escape and his complicity: the novel's argument is that the same technology can free and condemn, and it never lets either character be simply good or bad.",
        "All the Light We Cannot See (2014) — the model-city opening and the radio chapters",
        ["American", "WWII", "Pulitzer"],
    ),
    "book-the-underground-railroad-2016-208": _entry(
        "Colson Whitehead's Pulitzer and National Book Award winner takes the Underground Railroad literally: Cora, an enslaved woman in Georgia, escapes on an actual underground train, and each state she passes through is a different version of American racism — a plantation idyll, a white-supremacist laboratory, a surveillance state. The novel won both major American fiction prizes in the same year.",
        "Read the opening — Cora's life on the Randall plantation, her mother's escape years before — and notice how Whitehead grounds the fantastical premise in brutal realism. Then read the South Carolina chapters, where the 'progressive' state's medicine and eugenics are exposed: the novel's horror is that the alternative states are not alternatives at all.",
        "The Underground Railroad (2016) — the plantation opening and the South Carolina chapters",
        ["American", "Historical", "Slavery"],
    ),
    "book-sing-unburied-sing-2017-209": _entry(
        "Jesmyn Ward's National Book Award winner follows 13-year-old Jojo on a road trip across Mississippi with his mother, who is chasing her white boyfriend, and his younger sister — a journey haunted by the ghost of a murdered boy who has unfinished business with Jojo's family. It's a ghost story about the living, and about who gets to appear in the story of the South.",
        "Read the opening — Jojo's care for his grandfather, the birth of the goat, and the presence of the 'dead' — and notice how Ward makes the supernatural feel like the most natural thing in the novel. Then read the chapters narrated by Richie, the ghost: the novel's structure — living narration interrupted by the dead — is its argument that the South's unburied history won't stay down.",
        "Sing, Unburied, Sing (2017) — the opening and the Richie chapters",
        ["American", "Literary", "Ghosts"],
    ),
    "book-circe-2018-210": _entry(
        "Madeline Miller's retelling of Greek myth gives the witch Circe — a minor figure in the Odyssey — a full novel of her own, narrated in first person from her exile on the island of Aea. Its opening line — 'When I was born, the name for what I was did not exist' — announces its project: making a woman who was a plot device into a protagonist.",
        "Read the opening — Circe's childhood among the gods, her discovery of her power — and notice how Miller turns the Odyssey's one-scene witch into a character with a history. Then read the chapters where Odysseus arrives: the novel's central scene, and its point is that the Odyssey's version was never Circe's story. Miller's sequel-novel, The Song of Achilles, is the same project in reverse.",
        "Circe (2018) — the opening and the Odysseus chapters",
        ["American", "Mythology", "Retelling"],
    ),
    "book-piranesi-2020-211": _entry(
        "Susanna Clarke's follow-up to Jonathan Strange & Mr Norrell is set entirely inside the House — an infinite labyrinth of halls filled with statues, where Piranesi, the narrator, lives in blissful routine, keeping a journal and worshipping the tides. The reader slowly realizes what Piranesi doesn't: that he is a prisoner, and that his world is not what it seems. It won the Women's Prize and was shortlisted for the Booker.",
        "Read the opening — Piranesi's journal entries describing his daily circuit of the House — and notice how Clarke makes his contentment the mystery: the horror is not what he suffers but what he doesn't notice. Then read the chapter where the Other warns him about a new intruder: the novel's structure — revelation arriving in installments through an unreliable happy narrator — is its whole method.",
        "Piranesi (2020) — the journal opening and the intruder chapters",
        ["British", "Fantasy", "Mystery"],
    ),
    "book-sea-of-tranquility-2022-212": _entry(
        "Emily St. John Mandel's novel — a follow-up to Station Eleven — tracks one moment across centuries: a violinist in 1912, a wealthy exile in a moon colony, a writer during a pandemic, and a time traveler investigating an anomaly. The novel's twist is that its various centuries are a simulation, which makes its central question — what is real enough to mourn? — sharper than it first appears.",
        "Read the opening — the violinist in 1912 hearing the strange sound in the forest — and notice how Mandel plants the book's 'anomaly' as a recurring note across eras. Then read the chapters set in the pandemic-era 'gaslighting' arc: the novel's empathy for its fictional pandemic world is the same empathy it asks for ours, and the simulation reveal re-frames every chapter that came before.",
        "Sea of Tranquility (2022) — the 1912 opening and the simulation reveal",
        ["Canadian", "Science Fiction", "Literary"],
    ),
    "book-tomorrow-and-tomorrow-2022-213": _entry(
        "Gabrielle Zevin's novel follows Sam and Sadie, childhood friends who become game designers — and whose collaboration, friendship, and unspoken love play out across three decades and several invented video games. The title, from Macbeth's 'tomorrow and tomorrow and tomorrow,' names its theme: time, and what we make with it.",
        "Read the opening — Sam's college years, the first game they build together — and notice how Zevin renders the games themselves as characters: each invented game is a chapter of Sam and Sadie's relationship. Then read the chapter where the two friends fight over the design of their hit game: the novel's argument — that creative work is a way of talking about feelings the characters can't say directly — is dramatized in that argument.",
        "Tomorrow, and Tomorrow, and Tomorrow (2022) — the opening and the game-design chapters",
        ["American", "Literary", "Games"],
    ),
    "book-the-fraud-2023-214": _entry(
        "Zadie Smith's historical novel is set in 1873 London and centers on the Tichborne Trial — the case of a butcher claiming to be a missing heir — told through Eliza Touchet, housekeeper to a minor novelist, who watches the trial consume Victorian England. It's Smith's first historical novel, and it's about how stories, lies, and England itself are all fictions.",
        "Read the opening — Eliza Touchet's household, her employer William Ainsworth's literary decline — and notice how Smith establishes the novel's double subject: the trial's lies and the novelist's lies are the same industry. Then read the chapters where the trial testimony is quoted verbatim: the trial transcripts are the novel's strangest passages, and Smith's argument is that the Victorians' reality was already a courtroom drama.",
        "The Fraud (2023) — the opening and the trial chapters",
        ["British", "Historical", "Literary"],
    ),
    "book-the-bee-sting-2023-215": _entry(
        "Paul Murray's Booker-shortlisted Irish novel is 650 pages of family catastrophe narrated from four perspectives — a father selling his BMW online, a mother running from her past, and two teenage children — each chapter a different voice, each hiding a different version of the truth. It's a comic tragedy that builds to a reckoning that never quite arrives.",
        "Read the opening — Dickie Barnes's failed attempt to sell his car online, the 'Bee Sting' of the title's setup — and notice how Murray establishes each narrator's blind spot in the first chapters. Then read the chapters narrated by the daughter, Cass, whose 'Dark Web' section is the novel's formal experiment: Murray switches to a graphic-novel chapter mid-book, and the shift is doing real work about how teenagers process catastrophe.",
        "The Bee Sting (2023) — the opening and the Cass chapters",
        ["Irish", "Literary", "Family"],
    ),
    "book-tom-lake-2023-216": _entry(
        "Ann Patchett's novel is set in the spring of 2020, on a Michigan cherry farm, where three adult daughters are home with their mother — and the novel is mostly the mother telling them about the summer she spent in the 1980s doing community theater with a man who later became a movie star. The novel's frame is the pandemic; its content is the story about a story about love.",
        "Read the opening — the farm, the pandemic, the daughters — and notice how Patchett makes the quarantine the least interesting thing about the novel: it's the container, not the content. Then read the chapters where the mother describes rehearsing 'Our Town' with the future star: the novel's argument is that the play within the novel — about how little we notice the life we're living — is the novel's own method.",
        "Tom Lake (2023) — the farm opening and the 'Our Town' chapters",
        ["American", "Literary", "Pandemic"],
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
