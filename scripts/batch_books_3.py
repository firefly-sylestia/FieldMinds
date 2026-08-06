#!/usr/bin/env python3
"""Batch: replace the final 22 fake books.json entries with real facts.

ids 217–238: 2023 releases (Heaven & Earth Grocery Store → Family Meal) plus
the classics (Moby-Dick → One Hundred Years of Solitude). Same contract as
batch_books_1/2.py. Cap 450 (SCHEMA.md).
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
    "book-the-heaven-earth-grocery-217": _entry(
        "James McBride's novel centers on a 1972 discovery: a skeleton in a well in Pottstown, Pennsylvania, behind the Heaven & Earth Grocery Store — the neighborhood's Black and Jewish community's meeting place. The novel moves backward to tell how the body got there, and forward to show a community holding together through a housing crisis, a dance hall, and a haunted boy.",
        "Read the opening — the skeleton in the well, and the woman who lowers herself down to look — and notice how McBride opens with the mystery's end and then builds the neighborhood from the ground up. Then read the chapters centered on the store's owner, Moshe, and the 'Benevolent Society' of Black church ladies: the novel's argument is that community is the miracle, and the well is just its evidence.",
        "The Heaven & Earth Grocery Store (2023) — the well opening and the community chapters",
        ["American", "Historical", "Community"],
    ),
    "book-lone-women-2023-218": _entry(
        "Victor LaValle's novel is a 1915 western with a supernatural secret: Adelaide Henry leaves her family's California farm after a fire and travels to Montana with a heavy steamer trunk that she will not let anyone open — because it contains her mother's body, and her mother is not staying dead. The novel is both a pioneer story and a horror story about what women carried to survive.",
        "Read the opening — Adelaide's flight, the trunk, the fire — and notice how LaValle makes the trunk's weight the novel's central image: it is grief, guilt, and a monster in one. Then read the chapters on the homestead, where the novel's real subject — the brutal economics of being a woman alone in 1915 — is rendered with the same care as the horror.",
        "Lone Women (2023) — the trunk opening and the homestead chapters",
        ["American", "Horror", "Western"],
    ),
    "book-how-to-say-babylon-219": _entry(
        "Safiya Sinclair's memoir won the National Book Critics Circle Award: growing up in Montego Bay, Jamaica, under a Rastafari father who controlled every aspect of her life — including forbidding her to speak patois, cut her hair, or leave the house — while she escaped through poetry and education. It's a memoir of language as both cage and key.",
        "Read the opening — Sinclair's childhood, her father's rules about her voice — and notice how the memoir's title names its subject: 'Babylon' is the Rastafari term for the corrupt outside world her father kept out. Then read the chapters where she discovers English poetry and her own voice: the novel's argument is that the language her father used to cage her was also the tool she used to escape.",
        "How to Say Babylon (2023) — the opening and the poetry-escape chapters",
        ["Jamaican", "Memoir", "Poetry"],
    ),
    "book-blackouts-2023-220": _entry(
        "Justin Torres's National Book Award winner is a conversation between two men — an aging queer historian and a young man — in a shuttered museum-like institution, built around the historical record of the 'Sex Variants' study of the 1930s–40s, which documented queer lives and then buried them. The novel's 'blackouts' are the passages the archive erased, and the book is about restoring them.",
        "Read the opening — the two men, the dying institution, the oral-history project — and notice how Torres structures the novel as a conversation that is also an archive: the men are literally reading the historical record into being. Then find the real 'Sex Variants' study references, which Torres quotes and embellishes: the novel's argument is that queer history is full of blackouts, and fiction is one way to fill them.",
        "Blackouts (2023) — the museum opening and the Sex Variants chapters",
        ["American", "Queer", "Experimental"],
    ),
    "book-prophet-song-2023-221": _entry(
        "Paul Lynch's Booker winner is set in an Ireland sliding into authoritarianism: Eilish Stack, a biology teacher and mother of four, watches her trade unionist husband disappear and then her children, one by one, as the state's grip tightens. Written in a breathless, present-tense style, it won the Booker to some controversy — and reads as the most direct 'what if' about democracy's collapse since the dystopian canon.",
        "Read the opening — Eilish's ordinary morning, the knock at the door — and notice how Lynch renders the slide into tyranny as a sequence of small accepted losses: each step is individually plausible, which is the horror. Then read the chapters where the family is separated and the state's 'compassion' becomes its cover: the novel's argument is that the first person to disappear is the one who asks questions.",
        "Prophet Song (2023) — the opening and the family-separation chapters",
        ["Irish", "Dystopian", "Booker"],
    ),
    "book-this-other-eden-2023-222": _entry(
        "Paul Harding's Pulitzer winner is set on Apple Island, a real community of mixed-race and Native families off the Maine coast in 1911, whose residents are being evicted by a eugenics-minded state. It's a novel of a utopia under siege, narrated in the voices of the islanders and the men who would 'save' them by removing them.",
        "Read the opening — the island's founding, the families' mixed ancestry, the schoolteacher — and notice how Harding's prose (long, biblical, beautiful) makes the island's way of life feel like scripture being read aloud. Then read the chapters narrated by the visiting 'authorities': the novel's method is to alternate the island's tenderness with the state's cold paperwork, and the eugenics ideology is quoted in the officials' own words.",
        "This Other Eden (2023) — the island opening and the authorities' chapters",
        ["American", "Historical", "Pulitzer"],
    ),
    "book-in-memoriam-2023-223": _entry(
        "Alice Winn's debut novel follows two boys at an English boarding school in 1914 who are in love but can't say so — and then enlists them in the trenches of the First World War, where their letters and the horror of the Somme force the question. It's a First World War novel told through the letters of the generation that wrote itself into history while dying.",
        "Read the opening — the school, the war-fever, the two boys' unspoken attachment — and notice how Winn sets the novel's engine: the public school's codes of honor are the same codes the war will use. Then read the letters from the front, which Winn researched from real war correspondence: the novel's power is that the boys write home about the war in the same voice they used about cricket.",
        "In Memoriam (2023) — the school opening and the war letters",
        ["British", "Historical", "WWI"],
    ),
    "book-let-us-descend-2023-224": _entry(
        "Jesmyn Ward's novel follows Annis, an enslaved woman in the Carolinas who is sold south and forced to walk the 'slave coffle' to New Orleans — and who is accompanied by spirits, including Aza, the warrior woman who guards her. It's a ghost-haunted historical novel in which the supernatural is not escape from the horror but the only way to survive it.",
        "Read the opening — Annis's life on the rice plantation, her mother's sale — and notice how Ward grounds the novel's mysticism in the physical: the spirits are present, but so is the mud, the heat, the hunger. Then read the chapters where Annis, walking south, is accompanied by Aza: the novel's argument is that the ancestors are not consolation in this book — they are company, and company is the only thing that cannot be taken.",
        "Let Us Descend (2023) — the plantation opening and the spirit chapters",
        ["American", "Historical", "Slavery"],
    ),
    "book-roman-stories-2023-225": _entry(
        "Jhumpa Lahiri's collection — written in Italian, her adopted literary language, and translated into English by the author herself — gathers stories of Rome's residents: the Roman-born, the migrants, the tourists, the servants of the rich. It's a portrait of a city told entirely in its outsiders, and a book about what it means to write in a language that isn't yours.",
        "Read the opening story, 'The Boundary,' and notice how Lahiri — writing in Italian — renders the city from the perspective of those who serve it: the stories are told from the edges of Rome's beauty. Then read the collection's middle stories, where migrants and tourists collide: Lahiri's project is to make the city's real residents — not its monuments — the subject.",
        "Roman Stories (2023) — 'The Boundary' and the migrant stories",
        ["Italian", "Short Stories", "Translation"],
    ),
    "book-land-of-milk-and-226": _entry(
        "C Pam Zhang's novel is a foodie dystopia: in a near-future America collapsing under climate catastrophe, a young chef is hired to cook for a private community of the ultra-rich who have retreated to a bunker with a fully stocked pantry and a taste for the exotic. It's The Hunger Games for the tasting-menu set — a satire of luxury as the last thing worth saving.",
        "Read the opening — the chef's interview, the tastings, the vanishing of the outside world — and notice how Zhang makes the food writing do the political work: every luxury dish is a disappearing species or a borrowed culture. Then read the chapters where the bunker's residents' appetites turn literal: the novel's horror is that its satire never has to exaggerate much, because the rich already eat the world.",
        "Land of Milk and Honey (2023) — the tasting opening and the bunker chapters",
        ["American", "Dystopian", "Satire"],
    ),
    "book-family-meal-2023-227": _entry(
        "Bryan Washington's novel follows Cam, a baker who returns to Houston after his boyfriend TJ dies of an overdose — and moves in with TJ's family, where he begins an affair with TJ's married best friend while working in the family's bakery. It's a novel about grief, food, and the families we make — written in Washington's signature tender-gritty register.",
        "Read the opening — Cam's return to Houston, the bakery, the grief — and notice how Washington uses food as the novel's emotional currency: every meal is a negotiation nobody can name. Then read the chapters where Cam and the married friend circle each other: the novel's argument is that mourning TJ and betraying his memory are, for Cam, the same act, and Washington refuses to resolve it.",
        "Family Meal (2023) — the Houston opening and the bakery chapters",
        ["American", "Queer", "Grief"],
    ),
    "book-moby-dick-1851-228": _entry(
        "Melville's novel — a whaling adventure that becomes a metaphysical epic — was a commercial failure in its time and is now one of the most-studied books in English. Its narrator Ishmael signs onto the Pequod, whose captain, Ahab, is hunting one whale: the white whale that took his leg. The novel is 135 chapters that spend hundreds of pages on the science of whales and then, in three chapters, destroy everyone.",
        "Read the first chapter, 'Loomings,' — 'Call me Ishmael' and the sermon on Jonah — and notice how Melville sets up the book's two registers: the adventure and the sermon are the same thing. Then read the chapters on the 'whiteness of the whale' and the ending, where the Pequod sinks and only Ishmael survives on a coffin: the novel's final joke is that the one who tells the story is the one who floated.",
        "Moby-Dick (1851) — 'Loomings' and the whiteness chapter",
        ["American", "Classic", "Epic"],
    ),
    "book-anna-karenina-1877-229": _entry(
        "Tolstoy's novel opens with the sentence that makes marriages tremble — 'All happy families are alike; each unhappy family is unhappy in its own way' — and follows Anna's adulterous love affair to its destruction while the landowner Levin searches for meaning in work, faith, and love. The novel's two plots run side by side, and Tolstoy's point is that they're the same story.",
        "Read the opening — the Oblonsky household in crisis, and the train where Anna first meets Vronsky — and notice how Tolstoy seeds the ending in the first chapters: the railway is the novel's symbol of the modern world that will destroy her. Then read the chapters of Anna's final descent, which Tolstoy wrote in a state of identification that frightened even him: the novel's power is that Anna's ruin is narrated with her own reasoning, never the author's judgment.",
        "Anna Karenina (1877) — the opening and Anna's final chapters",
        ["Russian", "Classic", "Literary"],
    ),
    "book-the-great-gatsby-1925-230": _entry(
        "Fitzgerald's novel — set in the summer of 1922 and told by Nick Carraway, who rents next door to the mysterious Jay Gatsby — is the definitive American novel about money: Gatsby's parties are legend, his love for Daisy is the engine, and his death is the moral. It sold poorly at publication; it is now a fixture of every American reading list.",
        "Read the opening — Nick's move to West Egg, the first glimpse of Gatsby's mansion — and notice how Fitzgerald withholds Gatsby himself for fifty pages, which is the novel's method: Gatsby is built from rumor before he exists as a person. Then read the ending — the valley of ashes, the billboard of Dr. T. J. Eckleburg, and the final page's 'boats against the current': the novel's last paragraph is the most-quoted ending in American fiction.",
        "The Great Gatsby (1925) — the opening and the ending",
        ["American", "Classic", "Jazz Age"],
    ),
    "book-brave-new-world-1932-231": _entry(
        "Huxley's novel imagined the future as a pleasure-state: people are bred in hatcheries, conditioned by caste, and pacified by the drug soma — and the 'savage' John, raised outside the system, is brought in to be exhibited and destroys himself instead. Huxley's famous claim was that his dystopia was more prophetic than Orwell's: we would be seduced, not tortured.",
        "Read the opening — the Central London Hatchery, the Bokanovsky process — and notice how Huxley satirizes Fordism by making Henry Ford a god. Then read the ending, where John the Savage, unable to live in either world, hangs himself: Huxley's point is that the 'savage' world he romanticizes is as unlivable as the one he rejects — there is no third option, which is the tragedy.",
        "Brave New World (1932) — the Hatchery opening and the ending",
        ["British", "Dystopian", "Classic"],
    ),
    "book-native-son-1940-232": _entry(
        "Richard Wright's novel was the first by a Black American writer to become a Book-of-the-Month Club selection — and it opens with an accidental murder: Bigger Thomas, a young Black man in Chicago, smothers the white woman who hired him, then tries to escape a justice system that assumes his guilt. Wright wrote it as a provocation, and it worked.",
        "Read the opening — Bigger's first day, the rat in the room, the fear — and notice how Wright establishes Bigger's psychology as the novel's subject: his violence is born of the cage he lives in. Then read the trial chapters, where the communist lawyer's closing argument delivers the novel's thesis — that Bigger is 'created' by the society that condemns him: the book's power is that the argument is persuasive and also not the whole truth.",
        "Native Son (1940) — the opening and the trial chapters",
        ["American", "Classic", "Race"],
    ),
    "book-the-catcher-in-the-233": _entry(
        "Salinger's novel — the story of Holden Caulfield's three days in New York after being expelled from prep school — has sold 65 million copies and is the most-banned and most-taught American novel of the 20th century. Its title, from a misheard line of a Robert Burns poem, names Holden's fantasy: catching children before they fall off a cliff into adulthood.",
        "Read the opening — Holden's expulsion, the 'phony' refrain — and notice how Salinger makes Holden's voice the entire novel: every judgment is his, and his unreliability is the point. Then read the carousel scene at the end, where Holden finally does what he imagined — catching the falling — and the novel ends with the famous 'Don't ever tell anybody anything' note.",
        "The Catcher in the Rye (1951) — the opening and the carousel ending",
        ["American", "Classic", "Coming of Age"],
    ),
    "book-fahrenheit-451-1953-234": _entry(
        "Bradbury's novel is set in a future where firemen burn books instead of stopping fires — and fireman Guy Montag begins to steal the books he's supposed to destroy. Its title is the temperature at which paper burns, and Bradbury wrote it in the Los Angeles Public Library's basement on a rented typewriter at 10 cents for 10 minutes.",
        "Read the opening — Montag's pleasure in burning, the mechanical hound — and notice how Bradbury's feverish style is itself the novel's argument: the prose is overheated the way a society overheated on screens and slogans. Then read the ending, where Montag meets the 'book people' who memorize books to preserve them: Bradbury's famous claim was that the novel is about TV and screens more than censorship.",
        "Fahrenheit 451 (1953) — the opening and the book-people ending",
        ["American", "Dystopian", "Science Fiction"],
    ),
    "book-lolita-1955-235": _entry(
        "Nabokov's novel is narrated by Humbert Humbert, a middle-aged European professor who obsesses over his stepdaughter, Dolores 'Lolita' Haze — and the book's scandal obscured its design: it is a confession written for a jury, in which the monster is so eloquent that the reader must work to see past him. Nabokov called the novel his 'love affair with the English language.'",
        "Read the opening — 'Lolita, light of my life, fire of my loins' — and notice how Nabokov makes Humbert's prose gorgeous precisely where his acts are indefensible: the style is the seduction, and the reader is the seducee. Then read the 'confession' frame — Humbert writing from prison, for a jury — and the ending, where he admits what the novel has been doing: 'I am thinking of aurochs and angels, the secret of durable pigments...' — the beauty is the crime and the defense at once.",
        "Lolita (1955) — the opening and the confession frame",
        ["American", "Classic", "Unreliable Narrator"],
    ),
    "book-to-kill-a-mockingbird-236": _entry(
        "Harper Lee's only novel for most of her life is narrated by Scout, six years old, in Maycomb, Alabama, during the Depression — as her father Atticus defends a Black man falsely accused of rape. The title names the novel's moral: 'it's a sin to kill a mockingbird' — killing what harms nothing, like Tom Robinson, or like Boo Radley, the recluse who saves the children.",
        "Read the opening — Scout's first day of school, the Finch household — and notice how Lee renders adult moral crisis through a child's perfectly literal narration. Then read the trial chapters, where Atticus's closing argument is the novel's famous set piece, and the ending, where Scout finally meets Boo Radley and says the novel's last line about him: 'he was real nice.' The book's power is that it shows the children learning what the adults know.",
        "To Kill a Mockingbird (1960) — the opening and the trial chapters",
        ["American", "Classic", "Race"],
    ),
    "book-one-flew-over-the-237": _entry(
        "Ken Kesey's novel — written after he worked overnight shifts in a VA hospital's psychiatric ward — follows McMurphy, a convict who fakes insanity to serve his sentence in a mental institution, where he wages war on the ward's tyrannical Nurse Ratched. The novel was a counterculture bible, and the film adaptation won five Oscars.",
        "Read the opening — McMurphy's admission, the ward's routine — and notice how Kesey sets up the novel's binary: McMurphy's chaos against Ratched's order, with the other patients as the battleground. Then read the ending, where McMurphy's lobotomy and the Chief's mercy-killing re-frame the whole book: the novel's argument is that the 'therapy' is punishment, and the 'madness' is freedom.",
        "One Flew Over the Cuckoo's Nest (1962) — the opening and the ending",
        ["American", "Classic", "Counterculture"],
    ),
    "book-one-hundred-years-of-238": _entry(
        "García Márquez's novel follows the Buendía family through seven generations in the fictional town of Macondo — a century of repetition, war, and solitude in which everyone is named José Arcadio or Aureliano until the reader needs a family tree (and the novel provides one). Its opening sentence — 'Many years later, as he faced the firing squad...' — taught a generation of writers how to begin.",
        "Read the opening — Colonel Aureliano Buendía facing the firing squad, remembering the afternoon his father took him to see ice — and notice how the first sentence compresses the whole novel's method: memory, prophecy, and time are the same tense. Then read the last chapter, where the final Aureliano deciphers the manuscripts the gypsy Melquíades left: the ending reveals that the entire novel was being read, and that Macondo was always already written.",
        "One Hundred Years of Solitude (1967) — the opening and the ending",
        ["Colombian", "Magic Realism", "Classic"],
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
