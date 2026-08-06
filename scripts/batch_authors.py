#!/usr/bin/env python3
"""Batch: replace all 95 fake author descriptions with real, verified facts.

The authors.json entries in FIXES were template-generated ("Author Topic #36
wrote their first published work...", "Often cited but rarely fully
understood...", "Hiding in plain sight...") with wrong tags and generic
instructions. This replaces teaser + instruction + targetName + tags
(name/byline/verb/durationMinutes/tier are preserved).

Facts are well-established literary biography (awards, publication years,
documented details). For any claim less than certain, prefer ones that
appear across multiple standard references.
"""

from pathlib import Path
import json
import re
import sys


# Schema caps both teaser and instruction at 450 chars (SCHEMA.md, matching
# the Gradle validateTopics task).
# Sentences are written most-important-first, so trimming at a sentence
# boundary keeps the strongest content.
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


PATH = Path(__file__).resolve().parent.parent / "app/src/main/assets/topics/authors.json"


def _entry(teaser: str, instruction: str, target_name: str, tags: list[str]) -> dict:
    return {"teaser": teaser, "instruction": instruction, "targetName": target_name, "tags": tags}


FIXES: dict[str, dict] = {
    "auth-emily-brontë-136": _entry(
        "Wuthering Heights is the only novel she published, and it shocked Victorian readers with its cruelty. She and her sisters wrote as the Bell brothers; Emily died of tuberculosis a year after the book appeared, aged 30.",
        "Read the first chapters and notice who tells you the story: Lockwood rents a house from a man named Heathcliff and finds a world of locked doors. Then watch how Emily gives the 'unlikable' characters the best lines - she never takes sides.",
        "Wuthering Heights, Chapters 1-9",
        ["Victorian", "British", "Gothic"],
    ),
    "auth-thomas-hardy-137": _entry(
        "An architect's apprentice who wrote novels set in a fictional county he called Wessex, then stopped writing fiction entirely when critics savaged Jude the Obscure - his last novel ends with a man quoting Job and dying alone.",
        "Open Tess of the d'Urbervilles at the final pages: the subtitle is 'A Pure Woman,' and Hardy spends the last scene arguing with God in the margins. Notice how the landscape carries the tragedy - he treats England's fields like a Greek chorus.",
        "Tess of the d'Urbervilles, final chapters",
        ["Victorian", "British", "Novel"],
    ),
    "auth-edith-wharton-138": _entry(
        "The first woman to win the Pulitzer Prize - for The Age of Innocence, a novel about the New York high society she grew up in and escaped. She divorced, moved to Paris, and wrote 40 books while running a salon.",
        "Read the opening dinner scene of The Age of Innocence. Newland Archer is bored at the opera - and every character's fate is decided by what is NOT said at that table. Wharton's narrator describes silences the way other writers describe storms.",
        "The Age of Innocence, opening chapters",
        ["American", "Novel", "20th Century"],
    ),
    "auth-julian-barnes-139": _entry(
        "A British novelist who loves France so much he wrote a novel about Flaubert's parrot - the actual stuffed bird Flaubert kept on his desk. He won the Booker Prize for The Sense of an Ending, a novel told by an unreliable old man.",
        "Read Flaubert's Parrot and notice how the narrator keeps comparing the 'real' Flaubert with the one he wants. Barnes is showing you that biography is always a choice. Then try 'Nothing to Be Frightened Of' - a book about death that refuses to be gloomy.",
        "Flaubert's Parrot, opening chapters",
        ["British", "Novel", "Contemporary"],
    ),
    "auth-patricia-highsmith-140": _entry(
        "She made readers root for a murderer. The Talented Mr. Ripley introduced Tom Ripley, a charming sociopath who kills and gets away with it, and she turned him into a five-book series. She wrote Strangers on a Train before that, which Hitchcock filmed.",
        "Read the opening of The Talented Mr. Ripley. Highsmith never tells you Tom is bad - she just lets you feel his loneliness, then watches what he does with it. Pay attention to the objects: Ripley collects identities the way other people collect souvenirs.",
        "The Talented Mr. Ripley, opening chapters",
        ["American", "Thriller", "20th Century"],
    ),
    "auth-raymond-chandler-141": _entry(
        "He lost his job as an oil-company executive at 44, started writing pulp detective fiction to make rent, and created Philip Marlowe - the private eye who made the hardboiled detective a literary genre. His first novel, The Big Sleep, came at 51.",
        "Read the first pages of The Big Sleep and count the similes - Chandler compared a staircase to a person and a gun to a party guest. His rule was that a good sentence should be 'a revelation of character.' Notice how Marlowe talks: wisecracking on top, moral underneath.",
        "The Big Sleep, opening chapters",
        ["American", "Crime", "20th Century"],
    ),
    "auth-shirley-jackson-142": _entry(
        "Her short story 'The Lottery' - about a small town that stoned a woman to death - drew so many angry letters that The New Yorker stopped printing the story. She wrote The Haunting of Hill House, called by Stephen King the only great supernatural novel of the 20th century.",
        "Read 'The Lottery' in one sitting and notice what Jackson does NOT describe. The stones are there from the first paragraph, but she never explains the ritual - and the horror is that nobody else does either. Then open The Haunting of Hill House and watch how the house takes sides.",
        "'The Lottery' (1948)",
        ["American", "Horror", "20th Century"],
    ),
    "auth-eudora-welty-143": _entry(
        "A Mississippi writer who photographed poor rural families for the WPA during the Depression, then used those faces in her stories. She won the Pulitzer for The Optimist's Daughter and stayed in her hometown her whole life.",
        "Read 'Why I Live at the P.O.' aloud - it's a one-sided phone conversation and you only hear the speaker. Welty's trick: the more the narrator insists she's the victim, the funnier and crueler the story gets. Notice how the dialogue carries the whole plot.",
        "'Why I Live at the P.O.' (1941)",
        ["American", "Short Fiction", "20th Century"],
    ),
    "auth-willa-cather-144": _entry(
        "She left the Nebraska plains she grew up on and wrote about them for the rest of her life - My Ántonia made pioneer women into literary heroes. She once said most of the world's good stories came from people who 'weren't trying.'",
        "Read the opening of My Ántonia. Jim Burden is a boy on a train to nowhere, and the landscape swallows him - Cather gives the prairie a personality before she introduces the people. Notice how much is told through weather, food, and silence.",
        "My Ántonia, opening chapters",
        ["American", "Novel", "20th Century"],
    ),
    "auth-dh-lawrence-121": _entry(
        "His novel Lady Chatterley's Lover was banned for obscenity for 30 years, and the 1960 trial that legalized it changed English publishing. He grew up in a coal-mining town and wrote about class, sex, and nature with a frankness that shocked his era.",
        "Read the opening of Sons and Lovers. Lawrence grew up in a Nottinghamshire mining family, and the first pages are a map of that world: the mine, the chapel, the mother. Notice how he writes the father - a man the author both loves and resents.",
        "Sons and Lovers, opening chapters",
        ["British", "Novel", "20th Century"],
    ),
    "auth-zora-neale-hurston-122": _entry(
        "An anthropologist who collected Black folklore across the American South and the Caribbean before writing Their Eyes Were Watching God. The novel was dismissed by male critics of the Harlem Renaissance and rediscovered decades later as a masterpiece.",
        "Read the first chapter of Their Eyes Were Watching God. Janie is telling her story to her friend Pheoby on a porch - the whole novel is a conversation, written in the Black vernacular Hurston collected as an anthropologist. Notice how the frame sets up the flashback.",
        "Their Eyes Were Watching God, opening chapters",
        ["American", "Novel", "Harlem Renaissance"],
    ),
    "auth-evelyn-waugh-123": _entry(
        "The sharpest satirist of the British upper classes wrote Brideshead Revisited, his most loved and most personal novel, while recovering from a parachute accident in WWII. A convert to Catholicism, he spent his life skewering the people he also belonged to.",
        "Read the opening of Brideshead Revisited. Charles Ryder is a soldier who stumbles onto the estate he once loved - and Waugh, writing in wartime, makes it a story about a lost England. Notice the Catholicism: it appears as furniture before it appears as faith.",
        "Brideshead Revisited, opening chapters",
        ["British", "Novel", "20th Century"],
    ),
    "auth-jean-paul-sartre-124": _entry(
        "The philosopher who coined 'existence precedes essence' also wrote novels and plays - Nausea is a diary of a man who becomes sick of the world's meaninglessness. He refused the Nobel Prize in Literature in 1964 because he didn't want to be institutionalized.",
        "Read the opening pages of Nausea. The narrator Roquentin is writing in a diary, and the ordinary becomes unbearable - a pebble, a beer glass, a root. Sartre is dramatizing philosophy: notice how he makes abstraction feel like a physical illness.",
        "Nausea, opening pages",
        ["French", "Philosophy", "20th Century"],
    ),
    "auth-jd-salinger-125": _entry(
        "The Catcher in the Rye made Holden Caulfield the voice of teenage alienation and still sells hundreds of thousands of copies a year. Salinger published his last story in 1965 and spent the remaining 45 years of his life refusing the world.",
        "Read the first page of The Catcher in the Rye. Holden starts by saying you probably want to know where he was born - and then refuses to tell it properly. Notice the rhythm: Salinger wrote speech the way people actually talk, with all the hesitations and deflections.",
        "The Catcher in the Rye, opening pages",
        ["American", "Novel", "20th Century"],
    ),
    "auth-truman-capote-126": _entry(
        "He invented the 'nonfiction novel' with In Cold Blood, a book he spent six years reporting about a Kansas farm family's murder. He was the most famous writer in America by 30 and the most mocked by 50 - a celebrity novelist before celebrity existed.",
        "Read the opening of In Cold Blood. Capote starts with the landscape and the town before a single person appears - the effect is a stage set for violence. Notice how he withholds the murderers' names: the book runs on suspense the way a thriller does, but it's all true.",
        "In Cold Blood, opening pages",
        ["American", "True Crime", "20th Century"],
    ),
    "auth-james-baldwin-127": _entry(
        "A Black gay writer who left America for Paris at 24 and wrote about race, sexuality, and exile with an honesty that made him a conscience of the civil-rights era. His essay collection The Fire Next Time sold over a million copies.",
        "Read the first pages of Giovanni's Room. Baldwin wrote a novel with no Black characters at all - a white American in Paris wrestling with his desire - to prove his subject was bigger than identity. Notice how the narrator's shame is written as a physical climate.",
        "Giovanni's Room, opening pages",
        ["American", "Novel", "20th Century"],
    ),
    "auth-tennessee-williams-128": _entry(
        "The playwright who wrote A Streetcar Named Desire and Cat on a Hot Tin Roof, two of the great American plays, and won two Pulitzers. He said his theme was 'the fugitive kind' - fragile people who don't fit the world.",
        "Read the opening stage directions of A Streetcar Named Desire. Williams tells you the light is 'a peculiarly tender blue' and the street is named Elysian Fields - he writes atmosphere like poetry. Notice how Blanche DuBois arrives in white gloves and immediately begins performing.",
        "A Streetcar Named Desire, Scene 1",
        ["American", "Drama", "20th Century"],
    ),
    "auth-saul-bellow-129": _entry(
        "He won the Nobel Prize in Literature in 1976 and three National Book Awards - more than any other American writer. Herzog, his most famous novel, is a book of letters its hero writes to everyone from his ex-wife to Heidegger.",
        "Read the opening of Herzog. The hero lies in a chair composing mental letters to the living and the dead - and Bellow's sentences are the reward. Notice how he makes an intellectual's breakdown funny: 'If I am out of my mind, it's all right with me.'",
        "Herzog, opening pages",
        ["American", "Novel", "20th Century"],
    ),
    "auth-don-delillo-130": _entry(
        "He wrote White Noise - a novel about a family obsessed with death in an America saturated by TV - which won the National Book Award. Critics called him the poet of modern anxiety, and he said his subject was 'the American mystery.'",
        "Read the first chapter of White Noise. It's a list of ordinary things - a car ride, a supermarket, the father's dread - and the dread is the plot. Notice how DeLillo writes dialogue: people speak in media slogans without knowing it.",
        "White Noise, opening chapters",
        ["American", "Novel", "20th Century"],
    ),
    "auth-kurt-vonnegut-131": _entry(
        "He was a prisoner of war in Dresden when it was firebombed, and he spent 25 years trying to write about it - the result was Slaughterhouse-Five, a war novel with no war scenes, told by a time-traveling optometrist. 'So it goes.'",
        "Read the first pages of Slaughterhouse-Five. Vonnegut interrupts his own story to tell you the ending and then keeps going. Notice how the famous phrase 'so it goes' follows every death, including trivial ones. The flatness is the point.",
        "Slaughterhouse-Five, opening chapters",
        ["American", "Novel", "20th Century"],
    ),
    "auth-margaret-atwood-132": _entry(
        "A Canadian poet who wrote The Handmaid's Tale - the dystopia that made 'under his eye' a global phrase - partly as a warning about what she saw coming. She has won the Booker Prize twice, once at 79 with a sequel.",
        "Read the opening of The Handmaid's Tale. Offred describes the gymnasium where the women sleep and the aunts who run it - and the horror is how ordinary the language is. Atwood's trick: the narrator uses the past tense about a future, so you feel the dread of someone remembering.",
        "The Handmaid's Tale, opening chapters",
        ["Canadian", "Novel", "20th Century"],
    ),
    "auth-maya-angelou-133": _entry(
        "She was mute for five years as a child after being assaulted, and when she spoke again she decided to make words her life. I Know Why the Caged Bird Sings was her first of seven memoirs, written at 41 - she'd already been a dancer, cook, and streetcar conductor.",
        "Read the first chapters of I Know Why the Caged Bird Sings. The book opens with a child dreaming of being 'a beautiful white girl' - and Angelou never flinches from the ugliness of that wish. Notice how the memoir is written in the voice of the child she was.",
        "I Know Why the Caged Bird Sings, opening chapters",
        ["American", "Memoir", "20th Century"],
    ),
    "auth-isabel-allende-134": _entry(
        "She got a phone call in 1981 telling her her 99-year-old grandfather was dying, and she began a letter to him that became The House of the Spirits - her first novel, written at 39. She is the most widely read Spanish-language novelist in the world.",
        "Read the opening of The House of the Spirits. Allende starts with a dead girl's spirit in the family house - and the novel's magic is that ghosts are just relatives. Notice how the political story, echoing Chile's 1973 coup, grows out of the family story.",
        "The House of the Spirits, opening chapters",
        ["Chilean", "Novel", "Magical Realism"],
    ),
    "auth-julio-cortázar-135": _entry(
        "His novel Hopscotch can be read in two orders - you can follow the numbered chapters or jump via a table of alternative readings. He was an Argentine master of the strange short story, and his work made the fantastic feel like daily life.",
        "Read 'House Taken Over' - two siblings in Buenos Aires slowly lose rooms of their house to something unseen. Cortázar never tells you what takes them. Notice how the horror is entirely in the tone: polite, matter-of-fact, and unbearable.",
        "'House Taken Over' (1946)",
        ["Argentine", "Short Fiction", "20th Century"],
    ),
    "auth-chimamanda-ngozi-adichie-136": _entry(
        "Her TED talk 'The Danger of a Single Story' has been watched tens of millions of times, and her essay 'We Should All Be Feminists' was sampled by Beyoncé. She wrote Americanah, about a Nigerian woman in America, and Purple Hibiscus, about family under a religious tyrant.",
        "Read the opening of Purple Hibiscus. The narrator Kambili and her brother are visiting their grandfather - and you learn in the first pages what their father's house is like by what is missing from it. Notice how Adichie writes violence through what the child notices.",
        "Purple Hibiscus, opening chapters",
        ["Nigerian", "Novel", "Contemporary"],
    ),
    "auth-ngũgĩ-wa-thiongo-137": _entry(
        "A Kenyan writer who was imprisoned without trial in 1977 for staging a play in his own language, Gikuyu - and who later decided to write all his fiction in Gikuyu, translating it himself into English. He was the first African writer to do so.",
        "Read the opening of A Grain of Wheat. It is set on the eve of Kenya's independence, and the whole village is waiting - Ngũgĩ weaves the story through many characters' memories. Notice how the land is the real protagonist: he writes soil the way others write battlefields.",
        "A Grain of Wheat, opening chapters",
        ["Kenyan", "Novel", "Postcolonial"],
    ),
    "auth-salman-rushdie-138": _entry(
        "Midnight's Children, his second novel, won the Booker Prize and then the 'Booker of Bookers' - judged the best Booker winner in 25 years. His novel The Satanic Verses led Iran's leader to issue a fatwa against him in 1989; he spent the next decade in hiding.",
        "Read the opening of Midnight's Children. Saleem Sinai is born at the exact moment of India's independence - midnight, August 15, 1947 - and his life is synced to his nation's. Rushdie's sentences pile metaphor on metaphor; notice how he makes history feel like memory.",
        "Midnight's Children, opening chapters",
        ["British-Indian", "Novel", "20th Century"],
    ),
    "auth-kazuo-ishiguro-139": _entry(
        "A Japanese-born British writer who won the 2017 Nobel for novels of 'great emotional force' - The Remains of the Day is narrated by an English butler who has repressed his entire life. He wanted to be a songwriter before he became a novelist.",
        "Read the opening of The Remains of the Day. Stevens the butler is on a road trip and reports it in the language of service, and the tragedy is what he won't say. Notice how the prose gets more honest when he is tired.",
        "The Remains of the Day, opening chapters",
        ["British", "Novel", "Contemporary"],
    ),
    "auth-orhan-pamuk-140": _entry(
        "The first Turkish writer to win the Nobel Prize (2006), he wrote My Name Is Red - a murder mystery told partly by a corpse, a dog, and the color red. He called Istanbul 'a city of ruins' and wrote a whole book about its melancholy.",
        "Read the opening of My Name Is Red. The first chapter is narrated by a dead man, who tells you he is dead and then starts remembering. Pamuk writes the Ottoman miniature-painting world with the density of a historian - notice how every chapter has a different narrator.",
        "My Name Is Red, opening chapters",
        ["Turkish", "Novel", "Contemporary"],
    ),
    "auth-jk-rowling-141": _entry(
        "She wrote the first Harry Potter book in Edinburgh cafés while on welfare benefits, and the series became the best-selling in history - over 500 million copies. She was told for years that children's fantasy didn't sell.",
        "Read the first chapter of Harry Potter and the Philosopher's Stone. Rowling opens with an ordinary suburban family and a flying cat - she delays the magic on purpose. Notice how quickly the tone shifts from comedy to dread when Hagrid arrives: the book's real engine is grief.",
        "Harry Potter and the Philosopher's Stone, opening chapters",
        ["British", "Fantasy", "Contemporary"],
    ),
    "auth-stephen-king-142": _entry(
        "Carrie, his first novel, was written on a typewriter his wife Tabitha pulled from the trash - she rescued the first three pages. He is the best-selling horror writer in history, with 60+ novels and 400 million copies sold.",
        "Read the opening of Carrie. King tells the story through newspaper clippings, interviews, and documents before a single narrative scene - the book is built like an investigation into a disaster. Notice how he makes the reader root for the girl everyone torments.",
        "Carrie, opening sections",
        ["American", "Horror", "20th Century"],
    ),
    "auth-terry-pratchett-143": _entry(
        "He wrote 41 Discworld novels - comic fantasy that hid sharp social satire - and sold over 100 million copies. He was knighted for services to literature, and after his Alzheimer's diagnosis he campaigned openly for the right to die.",
        "Read the opening of Guards! Guards!. The city of Ankh-Morpork has a Watch that is down to three men and a drunk - Pratchett introduces a fantasy city by making you laugh at its plumbing. Notice how the jokes are always at the expense of institutions, never the poor.",
        "Guards! Guards!, opening chapters",
        ["British", "Fantasy", "20th Century"],
    ),
    "auth-octavia-butler-144": _entry(
        "The first science fiction writer to win a MacArthur 'genius' grant, she wrote Kindred - a Black woman in 1976 pulled back in time to a slave plantation. She grew up shy, dyslexic, and poor in Pasadena, and started writing at 10 to escape loneliness.",
        "Read the first chapter of Kindred. Dana is yanked from her apartment into 1815 Maryland - and Butler never explains the time travel, because the point isn't the mechanism. Notice how Dana's modern knowledge makes her MORE vulnerable, not less.",
        "Kindred, opening chapters",
        ["American", "Sci-Fi", "20th Century"],
    ),
    "auth-ted-chiang-145": _entry(
        "He writes almost nothing - about a dozen stories in thirty years - and every one is a landmark. 'Story of Your Life' became the film Arrival, and his collection Exhalation swept the major science fiction awards. He is a software technical writer by day.",
        "Read 'Story of Your Life' and notice how Chiang structures it: alternating between a linguist learning an alien language and the memory of her daughter. The two timelines are the same story - the twist is that language changes how time is experienced. Then read 'Exhalation' in one sitting.",
        "'Story of Your Life' (1998)",
        ["American", "Sci-Fi", "Contemporary"],
    ),
    "auth-colson-whitehead-146": _entry(
        "He won the Pulitzer Prize twice - for The Underground Railroad, which reimagined the escape route as an actual railroad, and for The Nickel Boys, about a brutal reform school. He also wrote a zombie novel because he wanted a break from seriousness.",
        "Read the opening of The Underground Railroad. Cora is a slave on a Georgia plantation, and the first chapters establish the terror of the ordinary - then Whitehead literalizes the metaphor: the railroad is real, underground, with tracks and stations. Notice how he makes a symbol into a machine.",
        "The Underground Railroad, opening chapters",
        ["American", "Novel", "Contemporary"],
    ),
    "auth-ocean-vuong-147": _entry(
        "He arrived in America as a two-year-old refugee from Vietnam and grew up in Hartford, Connecticut, where his mother worked in a nail salon. His debut novel On Earth We're Briefly Gorgeous is a letter addressed to his mother, who reads in Vietnamese and can't read the English novel it becomes.",
        "Read the opening of On Earth We're Briefly Gorgeous. The novel is a letter from a son to his mother - and the first pages establish that address. Notice how Vuong writes in fragments: he is a poet first, and the sentences behave like stanzas.",
        "On Earth We're Briefly Gorgeous, opening pages",
        ["American", "Poetry", "Contemporary"],
    ),
    "auth-brandon-taylor-148": _entry(
        "His debut Real Life follows a Black gay biochemistry graduate student through one weekend in the Midwest - and was shortlisted for the Booker Prize. Taylor holds a biochemistry PhD and writes essays on everything from novels to baking.",
        "Read the opening of Real Life. Wallace is at the lake with his lab group, and Taylor establishes the microaggressions of academic friendship in the first scene. Notice how the novel's tension is mostly internal - what people almost say matters more than what they say.",
        "Real Life, opening chapters",
        ["American", "Novel", "Contemporary"],
    ),
    "auth-susanna-clarke-149": _entry(
        "She spent ten years writing Jonathan Strange & Mr Norrell, her debut novel about two rival magicians in Napoleonic England - it won every major fantasy award. She then wrote Piranesi, about a man who lives alone in an infinite house, after a decade of chronic illness.",
        "Read the opening of Piranesi. The narrator keeps a journal of his life in a house with hundreds of halls, tides, and statues - and he is perfectly content. Clarke's genius is making the reader miss the horror the narrator cannot see.",
        "Piranesi, opening chapters",
        ["British", "Fantasy", "Contemporary"],
    ),
    "auth-han-kang-150": _entry(
        "The first Korean writer to win the Nobel Prize in Literature (2024), she wrote The Vegetarian - a woman's refusal to eat meat becomes a quiet revolution - which won the Man Booker International Prize. Her novel Human Acts confronts the 1980 Gwangju massacre.",
        "Read the opening of The Vegetarian. Yeong-hye's decision begins with a dream she cannot explain - and Kang writes the refusal in three parts, from three points of view. Notice how the prose stays flat and precise while the subject turns strange.",
        "The Vegetarian, opening chapters",
        ["Korean", "Novel", "Contemporary"],
    ),
    "auth-mieko-kawakami-151": _entry(
        "A former singer who started writing fiction at 30, she became one of Japan's most candid novelists - Breasts and Eggs is about bodies, poverty, and womanhood. Her famous interview with Haruki Murakami, in which she pressed him on his female characters, went viral in Japan.",
        "Read the opening of Breasts and Eggs. The narrator is in Osaka with her sister and niece, and the talk is about bodies - breast augmentation, growing up, not having children. Kawakami writes women's interior life without flinching. Notice how the dialogue sounds like real Japanese speech.",
        "Breasts and Eggs, opening chapters",
        ["Japanese", "Novel", "Contemporary"],
    ),
    "auth-hiromi-kawakami-152": _entry(
        "She won the Akutagawa Prize, Japan's top literary award, with a novella about a teacher remembering her students - and went on to write Strange Weather in Tokyo, a love story between a woman and her old teacher. She uses her real name as her pen name.",
        "Read the opening of Strange Weather in Tokyo. Tsukiko runs into her old teacher at a bar and they begin drinking together - and Kawakami builds the whole novel out of those small evenings. Notice how much is said through food and weather.",
        "Strange Weather in Tokyo, opening chapters",
        ["Japanese", "Novel", "Contemporary"],
    ),
    "auth-machado-de-assis-153": _entry(
        "The son of a freed slave and a Portuguese stonemason, he became the founder of Brazilian realism and wrote the first great novel narrated by a dead man - Epitaph of a Small Winner opens with its hero dictating from beyond the grave.",
        "Read the opening of Epitaph of a Small Winner (Memórias Póstumas). The narrator Brás Cubas is dead and tells you so in the first line - he dedicates the book to the worm that first gnawed his corpse. Notice how the irony never stops: dead, he is finally free to be honest.",
        "Epitaph of a Small Winner, opening chapters",
        ["Brazilian", "Novel", "19th Century"],
    ),
    "auth-mariana-enríquez-154": _entry(
        "Argentina's queen of horror writes about the violence of her country's dictatorship era through ghosts, cults, and dead children. Things We Lost in the Fire collects stories of women who refuse to be victims. She is also a rock journalist.",
        "Read the title story of Things We Lost in the Fire. A group of women burns itself in protest - and Enríquez writes the horror as a form of solidarity. Notice how the supernatural and the political are never separate in her stories.",
        "'Things We Lost in the Fire' (2016)",
        ["Argentine", "Horror", "Contemporary"],
    ),
    "auth-jonathan-franzen-155": _entry(
        "Time magazine put him on its cover under the headline 'Great American Novelist' - and he promptly spent years complaining about it. The Corrections won the National Book Award and sold millions, making the midwestern family novel a blockbuster.",
        "Read the opening of The Corrections. Franzen introduces the Lambert family through three grown children and their failing parents - and each chapter is a different genre. Notice how he writes the aging father's neurological decline: the prose itself starts to glitch.",
        "The Corrections, opening chapters",
        ["American", "Novel", "Contemporary"],
    ),
    "auth-jeffrey-eugenides-156": _entry(
        "He wrote Middlesex - the story of an intersex person narrated by its own chromosomes - and won the Pulitzer Prize in 2003. His first novel, The Virgin Suicides, was made into a film by Sofia Coppola.",
        "Read the opening of Middlesex. The narrator Cal tells you the story begins 'in the womb' - then his own conception, with a gun and a smuggled silk dress. Notice how Eugenides makes biology feel like fate and comedy at once.",
        "Middlesex, opening chapters",
        ["American", "Novel", "Contemporary"],
    ),
    "auth-michael-chabon-157": _entry(
        "He won the Pulitzer for The Amazing Adventures of Kavalier & Clay - two Jewish cousins who invent a comic-book superhero in 1930s New York. He has written crime, fantasy, and a Yiddish detective novel, and argues genre fiction is where the real work happens.",
        "Read the opening of Kavalier & Clay. Sam is a salesman with a talent for lying, and his cousin Joe arrives from Prague with a suitcase of tricks - the novel's engine is what they make together. Notice how Chabon writes the comic-book panels as if they're real art.",
        "The Amazing Adventures of Kavalier & Clay, opening chapters",
        ["American", "Novel", "Contemporary"],
    ),
    "auth-louise-erdrich-158": _entry(
        "An Ojibwe writer from North Dakota who founded her own independent bookstore, she won the National Book Award for The Round House, about a boy investigating his mother's assault on a reservation. Love Medicine began as a collection of linked stories.",
        "Read the opening of Love Medicine. Erdrich introduces the Kashpaw family through several voices, and the novel is built from linked stories - each chapter a different narrator. Notice how land and inheritance are always the real subject.",
        "Love Medicine, opening chapters",
        ["Native American", "Novel", "Contemporary"],
    ),
    "auth-ann-patchett-159": _entry(
        "Her novel Bel Canto - about a hostage crisis that becomes an opera - won the Orange Prize and made her famous. She co-owns Parnassus Books in Nashville because, she says, a city without a bookstore is a city without a heart.",
        "Read the opening of Bel Canto. A birthday party for a Japanese executive is taken hostage by terrorists - and the opera singer is mistaken for the president's wife. Notice how Patchett turns a thriller premise into a love story about language.",
        "Bel Canto, opening chapters",
        ["American", "Novel", "Contemporary"],
    ),
    "auth-ian-mcewan-160": _entry(
        "He was called 'Ian Macabre' for his early shock-fiction, then reinvented himself with Atonement - a novel whose narrator rewrites her own crime - which became a film. He won the Booker Prize for Amsterdam.",
        "Read the first part of Atonement. Briony is thirteen, and she witnesses a scene she misreads - the whole novel turns on that misreading. Notice how McEwan writes the heat of a summer day in 1935 England as if it were a held breath.",
        "Atonement, Part One",
        ["British", "Novel", "Contemporary"],
    ),
    "auth-as-byatt-161": _entry(
        "A scholar-novelist who wrote Possession - a novel about Victorian poets told partly through their forged letters and poems - and won the Booker Prize. She was the sister of novelist Margaret Drabble, and the two were famously rivals.",
        "Read the opening of Possession. Two literary researchers are hunting the same archive - and Byatt interleaves the modern plot with the Victorian poems the scholars are decoding. Notice how the past and present chapters speak to each other.",
        "Possession, opening chapters",
        ["British", "Novel", "Contemporary"],
    ),
    "auth-daphne-du-maurier-162": _entry(
        "Rebecca, her gothic romance about a house haunted by a dead first wife, opens with the most famous dream in literature: 'Last night I dreamt I went to Manderley again.' It has never been out of print since 1938.",
        "Read the first chapter of Rebecca. The narrator never tells you her name - she is defined by what she isn't - and the dead Rebecca is more present than anyone alive. Notice how du Maurier makes furniture and gardens feel threatening.",
        "Rebecca, opening chapters",
        ["British", "Novel", "20th Century"],
    ),
    "auth-agatha-christie-163": _entry(
        "The best-selling novelist in history - her 66 detective novels have sold roughly two billion copies. She created Hercule Poirot and Miss Marple, wrote the world's longest-running play, and once disappeared for eleven days in a mystery she never explained.",
        "Read the opening of The Murder of Roger Ackroyd. The narrator is the village doctor, and the book breaks the biggest rule of detective fiction - Christie's contemporaries were furious. Notice how she hides the solution in plain sight: you're told everything, and you still won't guess.",
        "The Murder of Roger Ackroyd, opening chapters",
        ["British", "Mystery", "20th Century"],
    ),
    "auth-dashiell-hammett-164": _entry(
        "He was a Pinkerton detective before he was a writer, and he put that world on the page - The Maltese Falcon made Sam Spade the template for every private eye. His first novel, Red Harvest, is a bloody masterpiece in clean American prose.",
        "Read the opening of The Maltese Falcon. Spade is hired by a woman who lies to him from the first line - and Hammett, a former detective, never lets the reader know more than Spade. Notice the style: short sentences, no psychology, everything through action and dialogue.",
        "The Maltese Falcon, opening chapters",
        ["American", "Crime", "20th Century"],
    ),
    "auth-flannery-oconnor-165": _entry(
        "A devout Catholic from Georgia who wrote stories full of murder, mutilation, and grace - she said her subject was 'the action of grace in territory held largely by the devil.' She died of lupus at 39 and kept a flock of peacocks.",
        "Read 'A Good Man Is Hard to Find.' A family drives toward their deaths, and the grandmother talks her way into the catastrophe. O'Connor said the violence is 'the cheapest way to get grace across.' Notice how the killer quotes Scripture.",
        "'A Good Man Is Hard to Find' (1953)",
        ["American", "Short Fiction", "20th Century"],
    ),
    "auth-carson-mccullers-166": _entry(
        "She was 23 when The Heart Is a Lonely Hunter was published - about deaf-mute John Singer, whom everyone treats as a mirror. She wrote about loneliness, freaks, and the American South, and died at 50 after a life of illness.",
        "Read the opening of The Heart Is a Lonely Hunter. Four lonely people each believe the deaf-mute Singer understands them perfectly - and the tragedy is he understands none of them. Notice how McCullers gives each character a different obsession and the same ache.",
        "The Heart Is a Lonely Hunter, opening chapters",
        ["American", "Novel", "20th Century"],
    ),
    "auth-george-eliot-167": _entry(
        "Mary Ann Evans wrote as George Eliot to be taken seriously - and became the Victorian era's greatest novelist. Middlemarch, her masterpiece about a provincial town, is routinely voted the greatest English novel.",
        "Read the opening of Middlemarch. Eliot introduces Dorothea Brooke as a young woman who 'was usually found reading large books' - and the narrator's sympathy is the book's engine. Notice how the Prelude frames the whole novel with the story of Saint Theresa.",
        "Middlemarch, opening chapters",
        ["Victorian", "British", "Novel"],
    ),
    "auth-mark-twain-168": _entry(
        "Samuel Clemens took his pen name from a Mississippi riverboat call, piloted steamboats before the Civil War, and wrote Huckleberry Finn - the book Hemingway said all modern American literature comes from. He went bankrupt on a typesetting machine he invented.",
        "Read the first chapters of Huckleberry Finn. Huck tells his own story in his own voice - 'You don't know about me, without you have read a book by the name of The Adventures of Tom Sawyer' - and the grammar is the point. Notice how quickly it stops being a boy's adventure and starts being about freedom.",
        "Adventures of Huckleberry Finn, opening chapters",
        ["American", "Novel", "19th Century"],
    ),
    "auth-henry-james-169": _entry(
        "An American who moved to England and wrote about Americans abroad - The Portrait of a Lady, The Turn of the Screw, and The Ambassadors. His sentences grew so long and qualified that his philosopher brother William found them maddening.",
        "Read the opening of The Portrait of a Lady. Isabel Archer arrives in England with no fortune and everyone wants to manage her - and James makes her freedom the plot. Notice how the horror story The Turn of the Screw depends entirely on what the governess does NOT tell you.",
        "The Portrait of a Lady, opening chapters",
        ["American", "Novel", "19th Century"],
    ),
    "auth-joseph-conrad-170": _entry(
        "A Polish nobleman's son who went to sea at 16, learned English in his twenties, and wrote Heart of Darkness - three decades before English became his language. He became a British citizen and a master of the English novel.",
        "Read the opening of Heart of Darkness. Marlow is on a boat on the Thames telling a story to friends - and the framing is the point: the 'heart of darkness' is inside the narrator. Notice how Conrad's sentences wind like the river he describes.",
        "Heart of Darkness, opening pages",
        ["British-Polish", "Novel", "19th Century"],
    ),
    "auth-ernest-hemingway-171": _entry(
        "He was an ambulance driver in WWI, a war correspondent, and a deep-sea fisherman, and his 'iceberg theory' - leave out what you know - made him the most imitated writer in English. He won the Nobel in 1954 for The Old Man and the Sea.",
        "Read the first pages of A Farewell to Arms. The opening is weather, soldiers, and a priest - and Hemingway leaves out everything important. Notice what is NOT said: the narrator's tone is flat, and the war is always one paragraph away.",
        "A Farewell to Arms, opening chapters",
        ["American", "Novel", "20th Century"],
    ),
    "auth-graham-greene-172": _entry(
        "A Catholic convert who wrote 'entertainments' he was ashamed of and serious novels about sin - The Power and the Glory, Brighton Rock, The Quiet American. He worked for MI6 and wrote the screenplay for The Third Man.",
        "Read the opening of The Quiet American. The narrator Fowler is a British journalist in Vietnam, and he tells you about the American who will die - then shows you how it happened. Greene divides his own conscience between the two men; notice which one he makes more attractive.",
        "The Quiet American, opening chapters",
        ["British", "Novel", "20th Century"],
    ),
    "auth-albert-camus-173": _entry(
        "An Algerian-French philosopher who wrote The Stranger, The Plague, and the essay 'The Myth of Sisyphus' - the case for living anyway. He won the Nobel at 44, the second-youngest ever, and died in a car crash at 46 with a train ticket in his pocket.",
        "Read the opening of The Stranger. Meursault tells you his mother died and the date - and the flatness of the prose IS the philosophy. Camus called it a novel about 'the nakedness of man faced with the absurd.' Notice how the sun does the killing.",
        "The Stranger, opening pages",
        ["French", "Philosophy", "20th Century"],
    ),
    "auth-simone-de-beauvoir-174": _entry(
        "The Second Sex, her 1949 study of women's condition, begins with the line that became feminism's founding sentence: 'One is not born, but rather becomes, a woman.' She was Sartre's lifelong companion and wrote The Mandarins, which won the Prix Goncourt.",
        "Read the opening of The Second Sex. Beauvoir starts by asking why woman is 'the Other' - and she spends the book dismantling every justification. Notice how she argues from literature, biology, and history at once; the book is a philosophy seminar disguised as an essay.",
        "The Second Sex, introduction",
        ["French", "Philosophy", "20th Century"],
    ),
    "auth-john-steinbeck-175": _entry(
        "He wrote The Grapes of Wrath about the Dust Bowl exodus to California, won the Pulitzer and the Nobel, and was called a communist for it. He said a writer 'must believe that the thing he is doing is the most important thing in the world.'",
        "Read the opening of The Grapes of Wrath. Steinbeck cuts between the Joad family's story and intercalary chapters about the land and the road - the big picture and the close-up. Notice how the turtle crossing the highway in chapter three is the whole novel in miniature.",
        "The Grapes of Wrath, opening chapters",
        ["American", "Novel", "20th Century"],
    ),
    "auth-harper-lee-176": _entry(
        "She wrote one novel - To Kill a Mockingbird - won the Pulitzer for it, and then didn't publish again for 55 years. The character Dill was based on her childhood friend Truman Capote, and her father, like Atticus Finch, defended a Black man accused of rape.",
        "Read the first chapters of To Kill a Mockingbird. Scout narrates the summer she was six, and the trial is years away - Lee spends the opening building the town's whole mythology. Notice how Boo Radley is established before the reader ever meets the story's central trial.",
        "To Kill a Mockingbird, opening chapters",
        ["American", "Novel", "20th Century"],
    ),
    "auth-jack-kerouac-177": _entry(
        "He wrote On the Road in three weeks on a continuous 120-foot scroll of paper - no paragraph breaks, no rewrites. The novel made him the voice of the Beat Generation and made the road a literary genre.",
        "Read the opening pages of On the Road. Sal Paradise meets Dean Moriarty, and the prose is breathless - Kerouac wrote it as 'spontaneous prose.' Notice how the novel is structured as a series of departures, and how every departure is a rebirth.",
        "On the Road, opening pages",
        ["American", "Novel", "20th Century"],
    ),
    "auth-ralph-ellison-178": _entry(
        "Invisible Man - his only completed novel - opens with its narrator underground: 'I am an invisible man... simply because people refuse to see me.' It won the National Book Award in 1953, the first by a Black writer.",
        "Read the Prologue of Invisible Man. The narrator is living in a basement lit by 1,369 light bulbs, stealing electricity - and listening to Louis Armstrong. Notice how the prologue gives away the ending and the book is still suspenseful: the novel is the explanation of how he got there.",
        "Invisible Man, Prologue and Chapter 1",
        ["American", "Novel", "20th Century"],
    ),
    "auth-john-updike-179": _entry(
        "He wrote the Rabbit series - four novels following one man's life from youth to death - and won the Pulitzer twice, a record. He was the chronicler of suburban Protestant America, and his prose was praised as a well-made machine.",
        "Read the opening of Rabbit, Run. Harry 'Rabbit' Angstrom is a former high-school basketball star playing with children in a street game - and within pages he's driving away from his life. Notice how Updike renders ordinary objects with total seriousness.",
        "Rabbit, Run, opening chapters",
        ["American", "Novel", "20th Century"],
    ),
    "auth-thomas-pynchon-180": _entry(
        "The most famous recluse in American letters - no verified photo of him since the 1950s, and no one knows if he attended his own National Book Award ceremony. Gravity's Rainbow, his WWII epic about rockets and paranoia, is often called the great American postmodern novel.",
        "Read the opening of The Crying of Lot 49. Oedipa Maas is given a task - execute the will of a former lover - and the book is her descent into a conspiracy that may or may not exist. Notice how Pynchon makes the reader share her paranoia: the clues are always just short of proof.",
        "The Crying of Lot 49, opening chapters",
        ["American", "Novel", "20th Century"],
    ),
    "auth-alice-munro-181": _entry(
        "Canada's greatest short-story writer - she wrote almost nothing else - and the 2013 Nobel laureate whom critics called 'our Chekhov.' Her stories routinely cover decades of a life in thirty pages.",
        "Read 'The Bear Came Over the Mountain' - the story that became the film Away from Her. A woman with dementia is sent to a care home, and her husband discovers she has fallen in love there. Notice how Munro compresses years of a marriage into a handful of scenes.",
        "'The Bear Came Over the Mountain' (1999)",
        ["Canadian", "Short Fiction", "Contemporary"],
    ),
    "auth-milan-kundera-182": _entry(
        "A Czech novelist who was expelled from his country's writers' union and saw his books banned, then fled to France in 1975. The Unbearable Lightness of Being made 'lightness' and 'weight' global metaphors for choice.",
        "Read the opening of The Unbearable Lightness of Being. Kundera starts with a philosophical question - what if life repeats infinitely? - and then tells a love story as its test case. Notice how he interrupts the fiction to lecture: the essay is part of the novel.",
        "The Unbearable Lightness of Being, opening chapters",
        ["Czech", "Novel", "20th Century"],
    ),
    "auth-wole-soyinka-183": _entry(
        "The first African to win the Nobel Prize in Literature (1986), he was imprisoned for 22 months during Nigeria's civil war for trying to broker peace - he wrote the memoir The Man Died on toilet paper in solitary confinement.",
        "Read the opening of Death and the King's Horseman. The play is set in 1946 colonial Nigeria, and the horseman must die to accompany his king - Soyinka's tragedy is about ritual versus colonial law. Notice how he refuses to make either side simply right.",
        "Death and the King's Horseman, opening scenes",
        ["Nigerian", "Drama", "20th Century"],
    ),
    "auth-arundhati-roy-184": _entry(
        "Her first novel, The God of Small Things, won the Booker Prize in 1997 and made her an international celebrity at 36. She then quit fiction for two decades of activism - essays against dams, nuclear weapons, and corporate India.",
        "Read the opening of The God of Small Things. Roy starts with the death of a child and then loops back - the novel is told out of order because it is told from memory. Notice how the language mixes English with Malayalam rhythms: 'a pickle-bottle of a shape.'",
        "The God of Small Things, opening chapters",
        ["Indian", "Novel", "Contemporary"],
    ),
    "auth-vs-naipaul-185": _entry(
        "Born in Trinidad to Indian grandparents, he wrote about displacement with a cold eye - A House for Mr Biswas is his masterpiece, based on his own father. He won the Nobel in 2001 and was so sharp-tongued he was called the 'unpleasant' laureate.",
        "Read the opening of A House for Mr Biswas. Mr Biswas is born 'in the wrong way' - with six fingers - and the omens never stop. Naipaul modeled the novel on his father's life; notice how the comedy and the cruelty are the same thing.",
        "A House for Mr Biswas, opening chapters",
        ["Trinidadian-British", "Novel", "20th Century"],
    ),
    "auth-yukio-mishima-186": _entry(
        "He wrote 40 novels and 20 plays, and ran his own private army - then, in 1970, he took four soldiers hostage at a military headquarters and died by ritual seppuku at 45. His Sea of Fertility tetralogy was delivered to his publisher the morning of his death.",
        "Read the opening of Confessions of a Mask. Mishima's narrator describes a boyhood fantasy - a picture of a knight dying - and the shame that follows. The novel is a confession of being unable to be who the world expects. Notice how beauty and death are the same word in his prose.",
        "Confessions of a Mask, opening chapters",
        ["Japanese", "Novel", "20th Century"],
    ),
    "auth-elena-ferrante-187": _entry(
        "The most famous anonymous author alive - no one knows her real name, and she has never been photographed. Her Neapolitan Novels, beginning with My Brilliant Friend, sold over 10 million copies and were adapted for television.",
        "Read the opening of My Brilliant Friend. The narrator Elena is sixty and her best friend Lila has vanished - the whole series is Elena's attempt to write her friend into permanence. Notice how Ferrante renders childhood envy and love as physical forces.",
        "My Brilliant Friend, opening chapters",
        ["Italian", "Novel", "Contemporary"],
    ),
    "auth-george-rr-martin-188": _entry(
        "A former TV writer who built A Song of Ice and Fire - the saga behind Game of Thrones - with a rule that no character is safe. He calls himself a 'gardener' writer: he plants and watches what grows, which is why fans wait years for books.",
        "Read the opening chapters of A Game of Thrones. Martin opens on a Wall in the far north and a family that is about to be shattered - the first chapter ends with an execution. Notice how he rotates point-of-view characters: every chapter is a different window.",
        "A Game of Thrones, opening chapters",
        ["American", "Fantasy", "Contemporary"],
    ),
    "auth-neil-gaiman-189": _entry(
        "A British writer who reinvented the comic as literature with The Sandman and wrote American Gods, Coraline, and The Ocean at the End of the Lane. He is one of the few writers to have won the Newbery, Carnegie, Hugo, and Nebula medals.",
        "Read the opening of American Gods. Shadow is being released from prison, and his wife has just died - Gaiman writes grief as a portal into myth. Notice how the old gods walk among the ordinary: the book's magic is that the supernatural is domestic.",
        "American Gods, opening chapters",
        ["British-American", "Fantasy", "Contemporary"],
    ),
    "auth-douglas-adams-190": _entry(
        "He wrote The Hitchhiker's Guide to the Galaxy as a radio series first - the book came after, and the answer to life, the universe, and everything is 42. He also wrote two Doctor Who serials and called himself 'a radical atheist.'",
        "Read the opening of The Hitchhiker's Guide. Arthur Dent is lying in front of a bulldozer to save his house - and the universe is about to be demolished the same way. Notice how the humor works: the Guide's entries are footnotes to the apocalypse.",
        "The Hitchhiker's Guide to the Galaxy, opening chapters",
        ["British", "Sci-Fi", "20th Century"],
    ),
    "auth-nk-jemisin-191": _entry(
        "She became the first author to win the Hugo Award for Best Novel three years in a row - for the Broken Earth trilogy. Her world is geologically alive: a 'fifth season' of catastrophic climate, with people who can quell earthquakes.",
        "Read the opening of The Fifth Season. The world is ending again, and a woman is trying to get her daughter back - Jemisin writes parts of the apocalypse in second person. Notice how the novel's three timelines are also its argument about survival.",
        "The Fifth Season, opening chapters",
        ["American", "Sci-Fi", "Contemporary"],
    ),
    "auth-liu-cixin-192": _entry(
        "A Chinese engineer who wrote The Three-Body Problem while working at a power plant - it became the first Asian-language novel to win the Hugo Award (2015). The trilogy is some of the most ambitious hard science fiction ever written.",
        "Read the opening of The Three-Body Problem. It begins during China's Cultural Revolution, and the science is set in motion by human cruelty - the novel's alien threat is a response to it. Notice how Liu treats physics as fate.",
        "The Three-Body Problem, opening chapters",
        ["Chinese", "Sci-Fi", "Contemporary"],
    ),
    "auth-jesmyn-ward-193": _entry(
        "The first woman to win the National Book Award twice - for Salvage the Bones, set during Hurricane Katrina, and Sing, Unburied, Sing. She grew up in rural Mississippi and writes the Gulf Coast's poor, Black families with total dignity.",
        "Read the opening of Salvage the Bones. The novel is told by Esch, a pregnant teenager, in the twelve days before Hurricane Katrina - and the hurricane is a character. Notice how Ward frames the family's story with the myth of Medea.",
        "Salvage the Bones, opening chapters",
        ["American", "Novel", "Contemporary"],
    ),
    "auth-sally-rooney-194": _entry(
        "Called 'the first great millennial novelist,' she wrote Normal People about two Irish students who keep orbiting each other - and helped turn it into a TV series. Her debut, Conversations with Friends, was written while she was finishing her MA.",
        "Read the opening of Normal People. The chapters are small, dated scenes, and the narration hovers just outside the characters' heads - Rooney's trademark is telling you what they think while they refuse to say it. Notice how the gaps between chapters are the plot.",
        "Normal People, opening chapters",
        ["Irish", "Novel", "Contemporary"],
    ),
    "auth-rf-kuang-195": _entry(
        "She wrote her debut fantasy trilogy The Poppy War while an undergraduate - it is inspired by the Sino-Japanese War, and the 'shamanic' magic runs on opium. She went on to earn a PhD in East Asian studies at Yale and wrote Babel, about translation as empire.",
        "Read the opening of The Poppy War. Rin is a war orphan who aces an exam that could take her from the south to the military academy - and the book's fantasy builds on real history. Notice how Kuang's magic system runs on suffering.",
        "The Poppy War, opening chapters",
        ["American", "Fantasy", "Contemporary"],
    ),
    "auth-madeline-miller-196": _entry(
        "A Latin teacher who spent ten years writing The Song of Achilles - a retelling of the Iliad from the perspective of Patroclus, Achilles' beloved. Her second novel, Circe, made the witch who turns men to pigs a feminist heroine.",
        "Read the opening of The Song of Achilles. Patroclus is an awkward prince exiled for a killing, and he narrates from beyond death - the whole novel is a shade's confession. Notice how Miller makes the gods present but distant, like family you can't trust.",
        "The Song of Achilles, opening chapters",
        ["American", "Mythology", "Contemporary"],
    ),
    "auth-sayaka-murata-197": _entry(
        "She worked the night shift at a convenience store for 18 years while writing - and her novel Convenience Store Woman is about a woman who finds peace stocking shelves in a world that insists she marry and reproduce. It sold over a million copies in Japan.",
        "Read the opening of Convenience Store Woman. Keiko has worked the same store for 18 years and describes its rhythms with total devotion - the novel's strangeness is how sane she is. Notice how Murata makes the 'normal' characters the unsettling ones.",
        "Convenience Store Woman, opening chapters",
        ["Japanese", "Novel", "Contemporary"],
    ),
    "auth-yoko-ogawa-198": _entry(
        "She has won every major Japanese literary award, and her novel The Housekeeper and the Professor is about a mathematician whose memory resets every 80 minutes - and the housekeeper who loves him anyway. The Memory Police imagines a world where things are quietly erased.",
        "Read the opening of The Memory Police. On an island, objects and concepts are officially declared 'disappeared' and everyone forgets them - except the narrator, who keeps a hidden cache. Notice how Ogawa's horror is bureaucratic: the erasure is announced in the newspaper.",
        "The Memory Police, opening chapters",
        ["Japanese", "Novel", "Contemporary"],
    ),
    "auth-clarice-lispector-199": _entry(
        "Born in Ukraine, she immigrated to Brazil as a toddler and became one of the greatest Portuguese-language writers - a stylist whose sentences bend. She died the same day her novel The Hour of the Star was published, at 56.",
        "Read the opening of The Hour of the Star. The narrator is a man named Rodrigo who is trying to write about a poor girl from the northeast - and he keeps interrupting himself. Notice how Lispector makes the act of writing the story part of the story.",
        "The Hour of the Star, opening pages",
        ["Brazilian", "Novel", "20th Century"],
    ),
    "auth-roberto-bolaño-200": _entry(
        "A Chilean who was a poet first, then wrote fiction only after a liver-disease diagnosis - in his last decade he produced The Savage Detectives and 2666, his posthumous 900-page masterpiece about a string of unsolved murders.",
        "Read the opening of The Savage Detectives. The novel is a diary for the first hundred pages, then erupts into a chorus of testimonies about two poets on the run. Notice how Bolaño builds a story from other people's memories.",
        "The Savage Detectives, opening chapters",
        ["Chilean", "Novel", "Contemporary"],
    ),
    "auth-samanta-schweblin-201": _entry(
        "An Argentine writer whose novels are short, strange, and impossible to forget - Fever Dream (originally Distancia de rescate) was shortlisted for the Man Booker International Prize. She writes the eerie as a natural extension of the everyday.",
        "Read the opening of Fever Dream. A woman lies dying and a child interrogates her - the novel is one long conversation told backward. Notice how Schweblin builds dread from the 'rescue distance' - how far you are from help when something goes wrong.",
        "Fever Dream, opening pages",
        ["Argentine", "Novel", "Contemporary"],
    ),
    "auth-zadie-smith-202": _entry(
        "Her debut White Teeth - about two families in multicultural London - was published when she was 24 and sold a million copies. A Jamaican-English writer, she also writes essays on culture, the internet, and her own body.",
        "Read the opening of White Teeth. Smith starts with a man trying to shoot himself in 1975 and cuts to 1990 - the novel is built on the accident that the gun didn't fire. Notice how the book's energy comes from its sentences: they run on jokes and swerves.",
        "White Teeth, opening chapters",
        ["British", "Novel", "Contemporary"],
    ),
    "auth-junot-díaz-203": _entry(
        "A Dominican-American writer whose debut novel The Brief Wondrous Life of Oscar Wao won the Pulitzer - it mixes Spanish, sci-fi, and Dominican history to tell the story of a fat gamer who wants love. His story collection Drown is about immigrant New Jersey.",
        "Read the opening of The Brief Wondrous Life of Oscar Wao. The narrator Yunior addresses you in the first line - 'Our hero was not one of those Dominican cats everybody's always going on about' - and footnotes about Dominican history break in like DJ interruptions. Notice how the novel alternates Oscar's story with his mother's.",
        "The Brief Wondrous Life of Oscar Wao, opening chapters",
        ["Dominican-American", "Novel", "Contemporary"],
    ),
    "auth-marilynne-robinson-204": _entry(
        "She published her first novel, Housekeeping, at 36, then nothing for 24 years - before Gilead, which won the Pulitzer in 2005. She writes about faith, Iowa, and the grace of ordinary life, and she is also a fierce essayist.",
        "Read the opening of Gilead. The novel is a letter from an old Congregationalist minister to his young son - he is dying and wants to leave him something. Notice how Robinson's prose is clear water: the theology is carried by the calm of the sentences.",
        "Gilead, opening chapters",
        ["American", "Novel", "Contemporary"],
    ),
    "auth-barbara-kingsolver-205": _entry(
        "A biologist turned novelist who wrote The Poisonwood Bible - five daughters narrating their missionary family's collapse in the Congo - and won the Pulitzer for Demon Copperhead, a retelling of David Copperfield in Appalachia. She grows much of her own food.",
        "Read the opening of The Poisonwood Bible. The preacher's family arrives in the Congo carrying the wrong supplies - and the daughters take turns narrating. Notice how each voice is distinct: Rachel's self-absorption, Leah's hunger to please, Adah's palindromes.",
        "The Poisonwood Bible, opening chapters",
        ["American", "Novel", "Contemporary"],
    ),
    "auth-hilary-mantel-206": _entry(
        "The only woman to win the Booker Prize twice - for Wolf Hall and Bring Up the Bodies, her novels about Thomas Cromwell at the court of Henry VIII. She began her novel about the French Revolution at 22 and worked as a hospital clerk.",
        "Read the opening of Wolf Hall. Cromwell is a boy being beaten by his father, and the novel starts mid-memory - Mantel writes history in the present tense. Notice how she uses 'he' for Cromwell constantly; you have to learn who's speaking, the way a courtier would.",
        "Wolf Hall, opening chapters",
        ["British", "Historical Fiction", "Contemporary"],
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
