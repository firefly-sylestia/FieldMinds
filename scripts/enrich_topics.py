#!/usr/bin/env python3
"""
Enrich the Curio topic catalog:

  1. Adds a `byline` field (creator name) to work-based categories so the
     Topic Reveal hero card can show an "Artist · The Beatles" style pill:
       - albums.json     → artist  (derived from the `album-{artist-slug}-…`
                                   id, via a curated slug→name map + overrides
                                   for the handful of ambiguous slugs)
       - books.json      → author  (curated id→author map)
       - films.json      → director (curated id→director map)
       - artworks.json   → painter (parsed from the trailing " by X" in name)
  2. Replaces the "American" origin tag with "Hollywood" on films + directors
     (Hollywood/Bollywood read as industry-region tags), so the Spin filter's
     Origin bucket offers Hollywood · Bollywood · British · French … instead of
     "American".
  3. Appends a curated batch of iconic Bollywood films + directors so the
     Bollywood filter actually has content to filter (the catalog previously
     had none).

Idempotent-ish: safe to re-run; existing byline/region values are overwritten
with the same deterministic values. Output written with literal UTF-8 + indent=2 to match the existing checked-in
JSON formatting exactly (the checked-in files keep real accented characters).
"""

import json
import os
import sys

TOPICS_DIR = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "assets", "topics")


def load(name):
    with open(os.path.join(TOPICS_DIR, name), encoding="utf-8") as f:
        return json.load(f)


def save(name, data):
    with open(os.path.join(TOPICS_DIR, name), "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
        f.write("\n")


# ── Key order preservation: insert "byline" right before "exploreAction" ──
def insert_byline(topic, byline):
    if not byline:
        return
    rebuilt = {}
    for k, v in topic.items():
        if k == "exploreAction" and "byline" not in rebuilt:
            rebuilt["byline"] = byline
        rebuilt[k] = v
    if "byline" not in rebuilt:
        rebuilt["byline"] = byline
    topic.clear()
    topic.update(rebuilt)


# ═══════════════════════════════════════════════════════════════════════════
# ALBUMS — artist per `album-{artist-slug}-…` id.
# The slug is the 2nd id segment, except ids like `album-the-weeknd-…` where
# the 2nd segment is "the" and the artist slug is the 3rd. A handful of slugs
# are ambiguous (bob = Dylan or Marley, john = Coltrane/Cage/Lee Hooker, …)
# and are resolved per-album in ALBUM_OVERRIDES.
# ═══════════════════════════════════════════════════════════════════════════
SLUG_ARTIST = {
    "abba": "ABBA", "adele": "Adele", "al": "Al Green", "albert": "Albert Ayler",
    "ali": "Ali Farka Touré", "alice": "Alice Coltrane", "alicia": "Alicia Keys",
    "american": "American Football", "amon": "Amon Tobin", "amy": "Amy Winehouse",
    "aphex": "Aphex Twin", "arcade": "Arcade Fire", "arctic": "Arctic Monkeys",
    "aretha": "Aretha Franklin", "art": "Art Blakey", "arvo": "Arvo Pärt",
    "asap": "A$AP Rocky", "augustus": "Augustus Pablo", "autechre": "Autechre",
    "barry": "Barry White", "bb": "B.B. King", "beach": "The Beach Boys",
    "beatles": "The Beatles", "bell": "Bell Witch", "between": "Between the Buried and Me",
    "beyonce": "Beyoncé", "big": "Big Bill Broonzy", "biggie": "The Notorious B.I.G.",
    "bill": "Bill Evans", "billie": "Billie Eilish", "bjork": "Björk",
    "black": "Black Sabbath", "blondie": "Blondie", "blur": "Blur",
    "boards": "Boards of Canada", "bob": "Bob Dylan", "bon": "Bon Iver",
    "brian": "Brian Eno", "buena": "Buena Vista Social Club", "built": "Built to Spill",
    "burial": "Burial", "burning": "Burning Spear", "burzum": "Burzum",
    "buzzcocks": "Buzzcocks", "caetano": "Caetano Veloso", "can": "Can",
    "cannibal": "Cannibal Ox", "cannonball": "Cannonball Adderley", "carcass": "Carcass",
    "caribou": "Caribou", "carl": "Carl Craig", "carly": "Carly Rae Jepsen",
    "carole": "Carole King", "cat": "Cat Stevens", "cesaria": "Cesária Évora",
    "charles": "Charles Mingus", "charli": "Charli XCX", "chemical": "The Chemical Brothers",
    "chet": "Chet Baker", "chic": "Chic", "chico": "Chico Buarque",
    "chief": "Chief Keef", "clark": "Clark", "clash": "The Clash",
    "claude": "Claude Debussy", "cocteau": "Cocteau Twins", "coltrane": "John Coltrane",
    "com": "Com Truise", "common": "Common", "converge": "Converge",
    "cream": "Cream", "csny": "Crosby, Stills, Nash & Young", "culture": "Culture",
    "cure": "The Cure", "curtis": "Curtis Mayfield", "daft": "Daft Punk",
    "dangelo": "D'Angelo", "danny": "Danny Brown", "dave": "Dave",
    "david": "David Bowie", "dead": "Dead Prez", "deafheaven": "Deafheaven",
    "death": "Death", "deftones": "Deftones", "denzel": "Denzel Curry",
    "depeche": "Depeche Mode", "derrick": "Derrick May", "desmond": "Desmond Dekker",
    "diana": "Diana Ross", "dizzee": "Dizzee Rascal", "dolly": "Dolly Parton",
    "don": "Don Cherry", "donna": "Donna Summer", "doors": "The Doors",
    "dr": "Dr. Dre", "drake": "Drake", "drudkh": "Drudkh",
    "eagles": "Eagles", "earth": "Earth, Wind & Fire", "echo": "Echo & the Bunnymen",
    "electric": "Electric Wizard", "elis": "Elis Regina", "elton": "Elton John",
    "eminem": "Eminem", "emmylou": "Emmylou Harris", "emperor": "Emperor",
    "eno": "Brian Eno", "eric": "Eric Dolphy", "erykah": "Erykah Badu",
    "etta": "Etta James", "fairport": "Fairport Convention", "fela": "Fela Kuti",
    "fleetwood": "Fleetwood Mac", "floating": "Floating Points", "flylo": "Flying Lotus",
    "foals": "Foals", "four": "Four Tet", "franco": "Franco",
    "frank": "Frank Ocean", "frankie": "Frankie Knuckles", "franz": "Franz Ferdinand",
    "funkadelic": "Funkadelic", "future": "Future", "gang": "Gang of Four",
    "george": "George Jones", "giggs": "Giggs", "gillian": "Gillian Welch",
    "glenn": "Glenn Gould", "gloria": "Gloria Gaynor", "gojira": "Gojira",
    "goldie": "Goldie", "gram": "Gram Parsons", "gucci": "Gucci Mane",
    "hank": "Hank Williams", "hendrix": "Jimi Hendrix", "herbie": "Herbie Hancock",
    "howlin": "Howlin' Wolf", "ice": "Ice Cube", "iggy": "Iggy Pop",
    "igor": "Igor Stravinsky", "interpol": "Interpol", "iron": "Iron Maiden",
    "isaac": "Isaac Hayes", "j": "J. Cole", "james": "James Brown",
    "janelle": "Janelle Monáe", "janet": "Janet Jackson", "jay": "Jay-Z",
    "jeff": "Jeff Mills", "jid": "JID", "jimmy": "Jimmy Cliff",
    "jobim": "Antônio Carlos Jobim", "john": "John Coltrane", "johnny": "Johnny Cash",
    "joni": "Joni Mitchell", "jorge": "Jorge Ben", "joy": "Joy Division",
    "juan": "Juan Atkins", "judas": "Judas Priest", "kacey": "Kacey Musgraves",
    "kanye": "Kanye West", "kate": "Kate Bush", "kavinsky": "Kavinsky",
    "keith": "Keith Jarrett", "kendrick": "Kendrick Lamar", "killers": "The Killers",
    "king": "King Sunny Adé", "kool": "Kool & the Gang", "kraftwerk": "Kraftwerk",
    "kyuss": "Kyuss", "la": "La Monte Young", "lapalux": "Lapalux",
    "larry": "Larry Heard", "laurie": "Laurie Anderson", "lauryn": "Lauryn Hill",
    "lcd": "LCD Soundsystem", "led": "Led Zeppelin", "lee": "Lee \"Scratch\" Perry",
    "leonard": "Leonard Cohen", "libertines": "The Libertines", "lil": "Lil Wayne",
    "little": "Little Simz", "lorde": "Lorde", "loretta": "Loretta Lynn",
    "ltj": "LTJ Bukem", "lucinda": "Lucinda Williams", "mac": "Mac Miller",
    "machinedrum": "Machinedrum", "madonna": "Madonna", "mahavishnu": "Mahavishnu Orchestra",
    "mahmoud": "Mahmoud Ahmed", "manic": "Manic Street Preachers", "manu": "Manu Chao",
    "maria": "Maria Callas", "marvin": "Marvin Gaye", "massive": "Massive Attack",
    "mastodon": "Mastodon", "max": "Max Richter", "maxwell": "Maxwell",
    "megadeth": "Megadeth", "meredith": "Meredith Monk", "merle": "Merle Haggard",
    "meshuggah": "Meshuggah", "metallica": "Metallica", "mf": "MF DOOM",
    "michael": "Michael Jackson", "migos": "Migos", "miles": "Miles Davis",
    "milton": "Milton Nascimento", "mingus": "Charles Mingus", "miriam": "Miriam Makeba",
    "mitski": "Mitski", "mobb": "Mobb Deep", "modest": "Modest Mouse",
    "moodymann": "Moodymann", "mos": "Mos Def", "muddy": "Muddy Waters",
    "mulatu": "Mulatu Astatke", "my": "My Bloody Valentine", "nas": "Nas",
    "national": "The National", "neil": "Neil Young", "neu": "Neu!",
    "new": "New Order", "nick": "Nick Drake", "nina": "Nina Simone",
    "nirvana": "Nirvana", "nusrat": "Nusrat Fateh Ali Khan", "nwa": "N.W.A",
    "oasis": "Oasis", "oneohtrix": "Oneohtrix Point Never", "opeth": "Opeth",
    "orbital": "Orbital", "orchestra": "Orchestra Baobab", "ornette": "Ornette Coleman",
    "oscar": "Oscar Peterson", "ossibisa": "Osibisa", "otis": "Otis Redding",
    "oumou": "Oumou Sangaré", "outkast": "OutKast", "pan": "Pan Sonic",
    "pantera": "Pantera", "parliament": "Parliament", "patsy": "Patsy Cline",
    "patti": "Patti Smith", "paul": "Paul Simon", "pavement": "Pavement",
    "pearl": "Pearl Jam", "pentangle": "Pentangle", "perturbator": "Perturbator",
    "peter": "Peter Tosh", "pharoah": "Pharoah Sanders", "philip": "Philip Glass",
    "pierre": "Pierre Schaeffer", "pink": "Pink Floyd", "pixies": "Pixies",
    "plebeian": "Plebeian Grandstand", "pop": "Pop Smoke", "portishead": "Portishead",
    "prince": "Prince", "public": "Public Enemy", "pulp": "Pulp",
    "queens": "Queens of the Stone Age", "radiohead": "Radiohead", "rage": "Rage Against the Machine",
    "ramones": "Ramones", "ravi": "Ravi Shankar", "ravishankar": "Ravi Shankar",
    "ray": "Ray Charles", "rem": "R.E.M.", "richard": "Richard & Linda Thompson",
    "rick": "Rick James", "ride": "Ride", "rihanna": "Rihanna",
    "rl": "R.L. Burnside", "robert": "Robert Johnson", "robyn": "Robyn",
    "rolling": "The Rolling Stones", "ron": "Ron Trent", "roni": "Roni Size",
    "rtj": "Run the Jewels", "ruben": "Rubén Blades", "sade": "Sade",
    "salif": "Salif Keita", "sam": "Sam Cooke", "scarface": "Scarface",
    "scientist": "Scientist", "scott": "Scott Walker", "sepultura": "Sepultura",
    "sex": "Sex Pistols", "shostakovich": "Dmitri Shostakovich", "shy": "Shy FX",
    "sigur": "Sigur Rós", "simon": "Simon & Garfunkel", "siouxsie": "Siouxsie and the Banshees",
    "skepta": "Skepta", "slayer": "Slayer", "sleep": "Sleep",
    "slowdive": "Slowdive", "slowthai": "Slowthai", "sly": "Sly and the Family Stone",
    "smashing": "The Smashing Pumpkins", "smiths": "The Smiths", "snoop": "Snoop Dogg",
    "solange": "Solange", "son": "Son House", "sonic": "Sonic Youth",
    "sonny": "Sonny Rollins", "sophie": "SOPHIE", "soundgarden": "Soundgarden",
    "springsteen": "Bruce Springsteen", "squarepusher": "Squarepusher", "stan": "Stan Getz",
    "steve": "Steve Reich", "stevie": "Stevie Wonder", "stone": "The Stone Roses",
    "stormzy": "Stormzy", "strokes": "The Strokes", "suede": "Suede",
    "sufjan": "Sufjan Stevens", "sun": "Sun Ra", "sunny": "Sunny Day Real Estate",
    "system": "System of a Down", "sza": "SZA", "taj": "Taj Mahal",
    "talib": "Talib Kweli", "talk": "Talk Talk", "talking": "Talking Heads",
    "tame": "Tame Impala", "taylor": "Taylor Swift", "television": "Television",
    "thelonious": "Thelonious Monk", "ti": "T.I.", "tim": "Tim Hecker",
    "tinariwen": "Tinariwen", "tool": "Tool", "toots": "Toots and the Maytals",
    "tower": "Tower of Power", "townes": "Townes Van Zandt", "tracy": "Tracy Chapman",
    "travis": "Travis Scott", "tribe": "A Tribe Called Quest", "tupac": "2Pac",
    "tyler": "Tyler, the Creator", "type": "Type O Negative", "u2": "U2",
    "ugk": "UGK", "ulver": "Ulver", "underworld": "Underworld",
    "vashti": "Vashti Bunyan", "velvet": "The Velvet Underground", "venetian": "Venetian Snares",
    "verve": "The Verve", "vince": "Vince Staples", "wayne": "Wayne Shorter",
    "weather": "Weather Report", "weeknd": "The Weeknd", "white": "The White Stripes",
    "who": "The Who", "wilco": "Wilco", "wiley": "Wiley",
    "willie": "Willie Nelson", "wire": "Wire", "wu": "Wu-Tang Clan",
    "yeah": "Yeah Yeah Yeahs", "young": "Young Jeezy", "youssou": "Youssou N'Dour",
    # `album-the-…` ids resolve to their 3rd segment as the artist slug.
    "abyssinians": "The Abyssinians", "band": "The Band", "ojays": "The O'Jays",
    "prodigy": "The Prodigy", "streets": "The Streets", "temptations": "The Temptations",
}

# Ambiguous slugs (and the `album-albert-king-…` vs `albert-ayler` case) —
# resolved per album id, keyed by the FULL album id.
ALBUM_OVERRIDES = {
    "album-bob-marley-exodus": "Bob Marley",
    "album-bob-marley-legend": "Bob Marley",
    "album-john-cage-433": "John Cage",
    "album-john-lee-hooker-the-healer": "John Lee Hooker",
    "album-stevie-ray-vaughan-texas-flood": "Stevie Ray Vaughan",
    "album-eric-b-and-rakim-paid-in-full": "Eric B. & Rakim",
    "album-max-roach-we-insist-freedom-now": "Max Roach",
    "album-king-tubby-meets-rockers-uptown": "King Tubby",
    "album-black-uhuru-red": "Black Uhuru",
    "album-dave-brubeck-time-out": "Dave Brubeck",
    "album-steve-earle-copperhead-road": "Steve Earle",
    "album-albert-king-born-under-a-bad-sign": "Albert King",
    "album-alice-in-chains-dirt": "Alice in Chains",
    "album-bill-withers-just-as-i-am": "Bill Withers",
    "album-stone-temple-pilots-purple": "Stone Temple Pilots",
}


def album_artist(album_id):
    if album_id in ALBUM_OVERRIDES:
        return ALBUM_OVERRIDES[album_id]
    parts = album_id.split("-")
    slug = parts[2] if len(parts) > 2 and parts[1] == "the" else parts[1]
    return SLUG_ARTIST.get(slug)


# ═══════════════════════════════════════════════════════════════════════════
# BOOKS — author per book id (curated).
# ═══════════════════════════════════════════════════════════════════════════
BOOK_AUTHORS = {
    "book-iliad": "Homer", "book-odyssey": "Homer", "book-aeneid": "Virgil",
    "book-divine-comedy": "Dante Alighieri", "book-don-quixote": "Miguel de Cervantes",
    "book-pride-and-prejudice": "Jane Austen", "book-jane-eyre": "Charlotte Brontë",
    "book-wuthering-heights": "Emily Brontë", "book-frankenstein": "Mary Shelley",
    "book-great-expectations": "Charles Dickens", "book-oliver-twist": "Charles Dickens",
    "book-war-and-peace": "Leo Tolstoy", "book-crime-and-punishment": "Fyodor Dostoevsky",
    "book-madame-bovary": "Gustave Flaubert", "book-the-brothers-karamazov": "Fyodor Dostoevsky",
    "book-moby-dick": "Herman Melville", "book-leaves-of-grass": "Walt Whitman",
    "book-emma": "Jane Austen", "book-the-picture-of-dorian-gray": "Oscar Wilde",
    "book-the-trial": "Franz Kafka", "book-ulysses": "James Joyce",
    "book-mrs-dalloway": "Virginia Woolf", "book-in-search-of-lost-time": "Marcel Proust",
    "book-the-waste-land": "T.S. Eliot", "book-brave-new-world": "Aldous Huxley",
    "book-1984": "George Orwell", "book-the-catcher-in-the-rye": "J.D. Salinger",
    "book-beloved": "Toni Morrison", "book-one-hundred-years-of-solitude": "Gabriel García Márquez",
    "book-if-on-a-winters-night-a-traveler": "Italo Calvino", "book-house-of-leaves": "Mark Z. Danielewski",
    "book-the-collected-poems-of-pablo-neruda": "Pablo Neruda", "book-the-second-sex": "Simone de Beauvoir",
    "book-the-myth-of-sisyphus": "Albert Camus", "book-the-god-of-small-things": "Arundhati Roy",
    "book-the-left-hand-of-darkness": "Ursula K. Le Guin", "book-the-three-body-problem": "Liu Cixin",
    "book-foundation": "Isaac Asimov", "book-dune": "Frank Herbert",
    "book-the-master-and-margarita": "Mikhail Bulgakov", "book-persuasion": "Jane Austen",
    "book-the-godfather-1969-145": "Mario Puzo", "book-fear-and-loathing-in-146": "Hunter S. Thompson",
    "book-sula-1973-133": "Toni Morrison", "book-song-of-solomon-1977-134": "Toni Morrison",
    "book-midnights-children-1981-135": "Salman Rushdie", "book-neuromancer-1984-136": "William Gibson",
    "book-beloved-1987-137": "Toni Morrison", "book-the-satanic-verses-1988-138": "Salman Rushdie",
    "book-the-things-they-carried-139": "Tim O'Brien", "book-the-english-patient-1992-140": "Michael Ondaatje",
    "book-snow-crash-1992-141": "Neal Stephenson", "book-infinite-jest-1996-142": "David Foster Wallace",
    "book-memoirs-of-a-geisha-143": "Arthur Golden", "book-the-god-of-small-144": "Arundhati Roy",
    "book-white-teeth-2000-145": "Zadie Smith", "book-atonement-2001-146": "Ian McEwan",
    "book-middlesex-2002-147": "Jeffrey Eugenides", "book-cloud-atlas-2004-148": "David Mitchell",
    "book-the-road-2006-149": "Cormac McCarthy", "book-wolf-hall-2009-150": "Hilary Mantel",
    "book-a-visit-from-the-151": "Jennifer Egan", "book-gone-girl-2012-152": "Gillian Flynn",
    "book-americanah-2013-153": "Chimamanda Ngozi Adichie", "book-between-the-world-and-154": "Ta-Nehisi Coates",
    "book-lincoln-in-the-bardo-155": "George Saunders", "book-normal-people-2018-156": "Sally Rooney",
    "book-the-testaments-2019-157": "Margaret Atwood", "book-klara-and-the-sun-158": "Kazuo Ishiguro",
    "book-demon-copperhead-2022-159": "Barbara Kingsolver", "book-chain-gang-all-stars-2023-160": "Nana Kwame Adjei-Brenyah",
    "book-yellowface-2023-161": "R.F. Kuang", "book-north-woods-2023-162": "Daniel Mason",
    "book-wellness-2023-163": "Nathan Hill", "book-birnam-wood-2023-164": "Eleanor Catton",
    "book-the-wager-2023-165": "David Grann", "book-king-a-life-2023-166": "Jonathan Eig",
    "book-the-vaster-wilds-2023-167": "Lauren Groff", "book-western-lane-2023-168": "Chetna Maroo",
    "book-old-gods-time-2023-169": "Sebastian Barry", "book-the-guest-2023-170": "Emma Cline",
    "book-tremor-2023-171": "Teju Cole", "book-everythings-fine-2023-172": "Cecilia Rabess",
    "book-pride-and-prejudice-1813-174": "Jane Austen", "book-crime-and-punishment-1866-175": "Fyodor Dostoevsky",
    "book-heart-of-darkness-1899-176": "Joseph Conrad", "book-to-the-lighthouse-1927-177": "Virginia Woolf",
    "book-the-grapes-of-wrath-178": "John Steinbeck", "book-1984-1949-179": "George Orwell",
    "book-invisible-man-1952-180": "Ralph Ellison", "book-lord-of-the-flies-181": "William Golding",
    "book-things-fall-apart-1958-182": "Chinua Achebe", "book-catch-22-1961-183": "Joseph Heller",
    "book-the-bell-jar-1963-184": "Sylvia Plath", "book-slaughterhouse-five-1969-185": "Kurt Vonnegut",
    "book-the-bluest-eye-1970-186": "Toni Morrison", "book-the-gulag-archipelago-1973-187": "Aleksandr Solzhenitsyn",
    "book-the-hitchhikers-guide-to-188": "Douglas Adams", "book-the-color-purple-1982-189": "Alice Walker",
    "book-the-handmaids-tale-1985-190": "Margaret Atwood", "book-norwegian-wood-1987-191": "Haruki Murakami",
    "book-the-remains-of-the-192": "Kazuo Ishiguro", "book-american-psycho-1991-193": "Bret Easton Ellis",
    "book-the-virgin-suicides-1993-194": "Jeffrey Eugenides", "book-trainspotting-1993-195": "Irvine Welsh",
    "book-fight-club-1996-196": "Chuck Palahniuk", "book-harry-potter-1997-197": "J.K. Rowling",
    "book-disgrace-1999-198": "J.M. Coetzee", "book-the-corrections-2001-199": "Jonathan Franzen",
    "book-life-of-pi-2001-200": "Yann Martel", "book-the-kite-runner-2003-201": "Khaled Hosseini",
    "book-never-let-me-go-202": "Kazuo Ishiguro", "book-the-brief-wondrous-life-203": "Junot Díaz",
    "book-the-help-2009-204": "Kathryn Stockett", "book-swamplandia-2011-205": "Karen Russell",
    "book-the-goldfinch-2013-206": "Donna Tartt", "book-all-the-light-we-207": "Anthony Doerr",
    "book-the-underground-railroad-2016-208": "Colson Whitehead", "book-sing-unburied-sing-2017-209": "Jesmyn Ward",
    "book-circe-2018-210": "Madeline Miller", "book-piranesi-2020-211": "Susanna Clarke",
    "book-sea-of-tranquility-2022-212": "Emily St. John Mandel", "book-tomorrow-and-tomorrow-2022-213": "Gabrielle Zevin",
    "book-the-fraud-2023-214": "Zadie Smith", "book-the-bee-sting-2023-215": "Paul Murray",
    "book-tom-lake-2023-216": "Ann Patchett", "book-the-heaven-earth-grocery-217": "James McBride",
    "book-lone-women-2023-218": "Victor LaValle", "book-how-to-say-babylon-219": "Safiya Sinclair",
    "book-blackouts-2023-220": "Justin Torres", "book-prophet-song-2023-221": "Paul Lynch",
    "book-this-other-eden-2023-222": "Paul Harding", "book-in-memoriam-2023-223": "Alice Winn",
    "book-let-us-descend-2023-224": "Jesmyn Ward", "book-roman-stories-2023-225": "Jhumpa Lahiri",
    "book-land-of-milk-and-226": "C Pam Zhang", "book-family-meal-2023-227": "Bryan Washington",
    "book-moby-dick-1851-228": "Herman Melville", "book-anna-karenina-1877-229": "Leo Tolstoy",
    "book-the-great-gatsby-1925-230": "F. Scott Fitzgerald", "book-brave-new-world-1932-231": "Aldous Huxley",
    "book-native-son-1940-232": "Richard Wright", "book-the-catcher-in-the-233": "J.D. Salinger",
    "book-fahrenheit-451-1953-234": "Ray Bradbury", "book-lolita-1955-235": "Vladimir Nabokov",
    "book-to-kill-a-mockingbird-236": "Harper Lee", "book-one-flew-over-the-237": "Ken Kesey",
    "book-one-hundred-years-of-238": "Gabriel García Márquez",
}

# ═══════════════════════════════════════════════════════════════════════════
# FILMS — director per film id (curated).
# ═══════════════════════════════════════════════════════════════════════════
FILM_DIRECTORS = {
    "film-citizen-kane-1941": "Orson Welles", "film-vertigo-1958": "Alfred Hitchcock",
    "film-2001-a-space-odyssey": "Stanley Kubrick", "film-casablanca-1942": "Michael Curtiz",
    "film-godfather-1972": "Francis Ford Coppola", "film-godfather-part-ii-1974": "Francis Ford Coppola",
    "film-apocalypse-now-1979": "Francis Ford Coppola", "film-raging-bull-1980": "Martin Scorsese",
    "film-taxi-driver-1976": "Martin Scorsese", "film-goodfellas-1990": "Martin Scorsese",
    "film-pulp-fiction-1994": "Quentin Tarantino", "film-bicycle-thieves-1948": "Vittorio De Sica",
    "film-eight-and-a-half-1963": "Federico Fellini", "film-la-dolce-vita-1960": "Federico Fellini",
    "film-tokyo-story-1953": "Yasujirō Ozu", "film-seven-samurai-1954": "Akira Kurosawa",
    "film-persona-1966": "Ingmar Bergman", "film-stalker-1979": "Andrei Tarkovsky",
    "film-breathless-1960": "Jean-Luc Godard", "film-the-400-blows-1959": "François Truffaut",
    "film-annie-hall-1977": "Woody Allen", "film-jaws-1975": "Steven Spielberg",
    "film-start-wars-1977": "George Lucas", "film-monty-python-1975": "Terry Gilliam & Terry Jones",
    "film-the-third-man-1949": "Carol Reed", "film-jules-and-jim-1962": "François Truffaut",
    "film-uhiroshima-mon-amour-1959": "Alain Resnais", "film-spirited-away-2001": "Hayao Miyazaki",
    "film-my-neighbor-totoro-1988": "Hayao Miyazaki", "film-princess-mononoke-1997": "Hayao Miyazaki",
    "film-wall-e-2008": "Andrew Stanton", "film-up-2009": "Pete Docter",
    "film-parasite-2019": "Bong Joon-ho", "film-oldboy-2003": "Park Chan-wook",
    "film-pather-panchali-1955": "Satyajit Ray", "film-rome-open-city-1945": "Roberto Rossellini",
    "film-ikiru-1952": "Akira Kurosawa", "film-rome-2018": "Alfonso Cuarón",
    "film-moonlight-2016": "Barry Jenkins", "film-portrait-of-a-lady-on-fire-2019": "Céline Sciamma",
    "film-there-will-be-blood-2007": "Paul Thomas Anderson", "film-singin-in-the-rain-165": "Gene Kelly & Stanley Donen",
    "film-the-seventh-seal-1957-166": "Ingmar Bergman", "film-psycho-1960-167": "Alfred Hitchcock",
    "film-lawrence-of-arabia-1962-168": "David Lean", "film-the-good-the-bad-169": "Sergio Leone",
    "film-once-upon-a-time-170": "Sergio Leone", "film-a-clockwork-orange-1971-171": "Stanley Kubrick",
    "film-chinatown-1974-172": "Roman Polanski", "film-oppenheimer-2023-173": "Christopher Nolan",
    "film-killers-of-the-flower-174": "Martin Scorsese", "film-poor-things-2023-175": "Yorgos Lanthimos",
    "film-anatomy-of-a-fall-176": "Justine Triet", "film-close-encounters-of-the-133": "Steven Spielberg",
    "film-blade-runner-1982-134": "Ridley Scott", "film-the-thing-1982-135": "John Carpenter",
    "film-brazil-1985-136": "Terry Gilliam", "film-wings-of-desire-1987-137": "Wim Wenders",
    "film-do-the-right-thing-138": "Spike Lee", "film-reservoir-dogs-1992-139": "Quentin Tarantino",
    "film-schindlers-list-1993-140": "Steven Spielberg", "film-chungking-express-1994-141": "Wong Kar-wai",
    "film-fargo-1996-142": "Coen Brothers", "film-the-big-lebowski-1998-143": "Coen Brothers",
    "film-beau-travail-1999-144": "Claire Denis", "film-amélie-2001-145": "Jean-Pierre Jeunet",
    "film-city-of-god-2002-146": "Fernando Meirelles", "film-eternal-sunshine-2004-147": "Michel Gondry",
    "film-pans-labyrinth-2006-148": "Guillermo del Toro", "film-no-country-for-old-149": "Coen Brothers",
    "film-the-dark-knight-2008-150": "Christopher Nolan", "film-inglourious-basterds-2009-151": "Quentin Tarantino",
    "film-the-social-network-2010-152": "David Fincher", "film-the-tree-of-life-153": "Terrence Malick",
    "film-her-2013-154": "Spike Jonze", "film-boyhood-2014-155": "Richard Linklater",
    "film-mad-max-fury-road-156": "George Miller", "film-arrival-2016-157": "Denis Villeneuve",
    "film-call-me-by-your-158": "Luca Guadagnino", "film-soul-2020-159": "Pete Docter",
    "film-drive-my-car-2021-160": "Ryusuke Hamaguchi", "film-everything-everywhere-all-at-161": "Daniels",
    "film-the-fabelmans-2022-162": "Steven Spielberg", "film-barbie-2023-163": "Greta Gerwig",
    "film-past-lives-2023-164": "Celine Song", "film-the-zone-of-interest-165": "Jonathan Glazer",
    "film-the-holdovers-2023-166": "Alexander Payne", "film-rashomon-1950-167": "Akira Kurosawa",
    "film-rear-window-1954-168": "Alfred Hitchcock", "film-the-night-of-the-169": "Charles Laughton",
    "film-breathless-1960-170": "Jean-Luc Godard", "film-yojimbo-1961-171": "Akira Kurosawa",
    "film-dr-strangelove-1964-172": "Stanley Kubrick", "film-2001-a-space-odyssey-173": "Stanley Kubrick",
    "film-the-conformist-1970-174": "Bernardo Bertolucci", "film-aguirre-the-wrath-of-175": "Werner Herzog",
    "film-network-1976-176": "Sidney Lumet", "film-koyaanisqatsi-1982-177": "Godfrey Reggio",
    "film-paris-texas-1984-178": "Wim Wenders", "film-blue-velvet-1986-179": "David Lynch",
    "film-akira-1988-180": "Katsuhiro Otomo", "film-the-piano-1993-181": "Jane Campion",
    "film-before-sunrise-1995-182": "Richard Linklater", "film-titanic-1997-183": "James Cameron",
    "film-the-matrix-1999-184": "The Wachowskis", "film-in-the-mood-for-185": "Wong Kar-wai",
    "film-mulholland-drive-2001-186": "David Lynch", "film-lost-in-translation-2003-187": "Sofia Coppola",
    "film-brokeback-mountain-2005-188": "Ang Lee", "film-children-of-men-2006-189": "Alfonso Cuarón",
    "film-a-serious-man-2009-190": "Coen Brothers", "film-inception-2010-191": "Christopher Nolan",
    "film-a-separation-2011-192": "Asghar Farhadi", "film-amour-2012-193": "Michael Haneke",
    "film-gravity-2013-194": "Alfonso Cuarón", "film-whiplash-2014-195": "Damien Chazelle",
    "film-get-out-2017-196": "Jordan Peele", "film-dune-2021-197": "Denis Villeneuve",
    "film-the-power-of-the-198": "Jane Campion", "film-tár-2022-199": "Todd Field",
    # Bollywood batch (below)
    "film-sholay-1975": "Ramesh Sippy", "film-ddlj-1995": "Aditya Chopra",
    "film-lagaan-2001": "Ashutosh Gowariker", "film-3-idiots-2009": "Rajkumar Hirani",
    "film-dangal-2016": "Nitesh Tiwari", "film-pyaasa-1957": "Guru Dutt",
    "film-mughal-e-azam-1960": "K. Asif", "film-mother-india-1957": "Mehboob Khan",
    "film-deewaar-1975": "Yash Chopra", "film-shree-420-1955": "Raj Kapoor",
}

# Films whose production is primarily the American studio system → Hollywood.
FILM_REGION = {
    "film-citizen-kane-1941", "film-vertigo-1958", "film-2001-a-space-odyssey",
    "film-casablanca-1942", "film-godfather-1972", "film-godfather-part-ii-1974",
    "film-apocalypse-now-1979", "film-raging-bull-1980", "film-taxi-driver-1976",
    "film-goodfellas-1990", "film-pulp-fiction-1994", "film-annie-hall-1977",
    "film-jaws-1975", "film-start-wars-1977", "film-wall-e-2008", "film-up-2009",
    "film-moonlight-2016", "film-there-will-be-blood-2007", "film-singin-in-the-rain-165",
    "film-psycho-1960-167", "film-close-encounters-of-the-133", "film-blade-runner-1982-134",
    "film-the-thing-1982-135", "film-do-the-right-thing-138", "film-reservoir-dogs-1992-139",
    "film-schindlers-list-1993-140", "film-fargo-1996-142", "film-the-big-lebowski-1998-143",
    "film-eternal-sunshine-2004-147", "film-no-country-for-old-149", "film-the-dark-knight-2008-150",
    "film-inglourious-basterds-2009-151", "film-the-social-network-2010-152",
    "film-the-tree-of-life-153", "film-her-2013-154", "film-boyhood-2014-155",
    "film-arrival-2016-157", "film-soul-2020-159", "film-everything-everywhere-all-at-161",
    "film-the-fabelmans-2022-162", "film-barbie-2023-163", "film-past-lives-2023-164",
    "film-the-holdovers-2023-166", "film-rear-window-1954-168", "film-the-night-of-the-169",
    "film-dr-strangelove-1964-172", "film-2001-a-space-odyssey-173", "film-network-1976-176",
    "film-blue-velvet-1986-179", "film-titanic-1997-183", "film-the-matrix-1999-184",
    "film-mulholland-drive-2001-186", "film-lost-in-translation-2003-187",
    "film-brokeback-mountain-2005-188", "film-children-of-men-2006-189",
    "film-a-serious-man-2009-190", "film-inception-2010-191", "film-gravity-2013-194",
    "film-whiplash-2014-195", "film-get-out-2017-196", "film-dune-2021-197",
    "film-oppenheimer-2023-173", "film-killers-of-the-flower-174", "film-tár-2022-199",
    "film-a-clockwork-orange-1971-171", "film-chinatown-1974-172",
}

# Directors whose career is primarily the American studio system → Hollywood.
DIRECTOR_REGION = {
    "director-kubrick", "director-hitchcock", "director-spielberg", "director-scorcese",
    "director-coppola", "director-lynch", "director-tarantino", "director-coen",
    "director-malick", "director-paul-thomas-anderson", "director-spike-lee",
    "director-john-ford", "director-billy-wilder", "director-cassavetes",
    "director-greta-gerwig", "director-maya-deren", "director-david-fincher",
    "director-christopher-nolan", "director-denis-villeneuve", "director-alfonso-cuaron",
    "director-guillermo-del-toro", "dire-george-lucas-124", "dire-woody-allen-125",
    "dire-ridley-scott-126", "dire-john-carpenter-127", "dire-tim-burton-128",
    "dire-wes-anderson-129", "dire-kathryn-bigelow-130", "dire-alejandro-gonzález-iñárritu-138",
    "dire-damien-chazelle-142", "dire-chloé-zhao-143", "dire-ari-aster-144",
    "dire-sean-baker-145", "dire-yorgos-lanthimos-149", "dire-emerald-fennell-150",
    "dire-james-cameron-164", "dire-david-cronenberg-165", "dire-sofia-coppola-166",
    "dire-jordan-peele-178", "dire-barry-jenkins-177", "dire-robert-eggers-179",
}


def set_region(topic, region):
    """Swap the 'American' origin tag for 'Hollywood'/'Bollywood'."""
    if not region:
        return
    tags = topic.get("tags", [])
    tags = [t for t in tags if t not in ("American", "Hollywood", "Bollywood")]
    tags.append(region)
    topic["tags"] = tags


# ═══════════════════════════════════════════════════════════════════════════
# NEW CONTENT — iconic Bollywood films + directors (tagged Bollywood) so the
# Spin filter's Origin bucket has real content. Matches the existing topic
# shape + quality bar (teaser ≤ 280 chars, instruction ≤ 280 chars).
# ═══════════════════════════════════════════════════════════════════════════
def film(tid, name, teaser, target, mins, instruction, tags, byline):
    return {
        "id": tid, "categoryId": "FILMS", "subtype": "Film", "name": name,
        "teaser": teaser, "imageUrl": "", "byline": byline,
        "exploreAction": {
            "verb": "Watch", "targetName": target, "durationMinutes": mins,
            "instruction": instruction,
        },
        "tags": tags, "tier": 1,
    }


def director(tid, name, teaser, target, mins, instruction, tags):
    return {
        "id": tid, "categoryId": "DIRECTORS", "subtype": "Director", "name": name,
        "teaser": teaser, "imageUrl": "",
        "exploreAction": {
            "verb": "Watch", "targetName": target, "durationMinutes": mins,
            "instruction": instruction,
        },
        "tags": tags, "tier": 1,
    }


NEW_FILMS = [
    film("film-sholay-1975", "Sholay (1975)",
         "Ramesh Sippy's 1975 dacoit-western fusion that redefined the Bollywood blockbuster — the highest-grossing Indian film for two decades. Amjad Khan's Gabbar Singh was a last-minute casting gamble; his growl came from a prosthetic jaw.",
         "Sholay (1975) — the first hour", 60,
         "Watch the first hour and count how many times the plot turns on a line of dialogue instead of a gunfight. Notice how Sippy shoots the Thakur-Gabbar confrontation like a Leone western — wide frames, dust, silence — then cuts to melodrama the instant the music starts.",
         ["Bollywood", "Action", "Drama", "1970s"], "Ramesh Sippy"),
    film("film-ddlj-1995", "Dilwale Dulhania Le Jayenge (1995)",
         "1995 — still running at Mumbai's Maratha Mandir decades later, a world record for theatrical longevity. Shah Rukh Khan's Raj made the NRI love story a genre; the mustard-field finale became Indian cinema's most imitated image.",
         "Dilwale Dulhania Le Jayenge (1995) — the first hour", 60,
         "Notice how the first hour delays the couple's first meeting for twenty minutes — the film knows anticipation is the plot. Then watch 'Tujhe Dekha Toh' twice: the lyrics, the wind machine, the train — every frame sells a love you never see consummated.",
         ["Bollywood", "Romance", "1990s"], "Aditya Chopra"),
    film("film-lagaan-2001", "Lagaan (2001)",
         "2001 — India's third Oscar nomination for Best Foreign Language Film. A drought-stricken village bets its taxes on a cricket match against the British Raj; the cast trained with real cricketers, and Aamir Khan learned left-hand batting for one role.",
         "Lagaan (2001) — the first hour", 60,
         "Watch how the film turns a colonial tax dispute into a siege. The villagers' practice montage matters: every bungled catch pays off in the final innings. The match is filmed in real time — no cuts mid-delivery, so the umpire's calls become the drama.",
         ["Bollywood", "Drama", "Sports", "2000s"], "Ashutosh Gowariker"),
    film("film-3-idiots-2009", "3 Idiots (2009)",
         "2009 — the highest-grossing Indian film of its decade. Rajkumar Hirani's takedown of rote education stars Aamir Khan as a genius who asks 'why' instead of 'what'; the 'All Izz Well' scooter scene was filmed with Khan genuinely unable to control the bike.",
         "3 Idiots (2009) — the first hour", 60,
         "Notice how every lecture scene is staged like a courtroom — the professor prosecutes, Rancho defends with questions. The hostel raid is one long tracking shot. Hirani hides the film's argument inside gags: the 'machine' monologue is the thesis.",
         ["Bollywood", "Comedy", "Drama", "2000s"], "Rajkumar Hirani"),
    film("film-dangal-2016", "Dangal (2016)",
         "2016 — China's biggest foreign-language hit in history. Aamir Khan gained 25kg to play the ageing father, then shed it for the flashbacks; the real Phogat sisters coached the actresses. The wrestling is filmed in unbroken takes — no cuts mid-match.",
         "Dangal (2016) — the first hour", 60,
         "Watch the wrestling scenes for the sound design: every throw lands with a wet thud before the crowd reacts. The first hour builds the daughters' rebellion, then the haircut scene flips the mood — notice how the music stops at the barber's chair.",
         ["Bollywood", "Biopic", "Sports", "2010s"], "Nitesh Tiwari"),
    film("film-pyaasa-1957", "Pyaasa (1957)",
         "Guru Dutt's 1957 portrait of a poet crushed by a world that buys and sells words — routinely ranked among the greatest films ever made. 'Jaane Woh Kaise Log The' was filmed in one unbroken take through the streets of a sleeping city.",
         "Pyaasa (1957) — the first hour", 50,
         "Watch 'Jaane Woh Kaise Log The' without reading the subtitles — the camera does the talking. Dutt films Vijay's poems as private objects, then the market scenes turn the same words into currency. That exchange is the whole film.",
         ["Bollywood", "Drama", "Musical", "1950s"], "Guru Dutt"),
    film("film-mughal-e-azam-1960", "Mughal-e-Azam (1960)",
         "1960 — fifteen years in the making and the most expensive Indian film of its era. 'Pyar Kiya To Darna Kya' was shot in a palace of mirrored glass; one war scene used over a hundred thousand extras. A 2004 re-release colorized scenes shot in black-and-white.",
         "Mughal-e-Azam (1960) — the Sheesh Mahal sequence", 45,
         "Skip ahead to the Sheesh Mahal — the mirrored hall where 'Pyar Kiya To Darna Kya' is sung. Anarkali's defiance is framed in shattered reflections: the mirror splits her into many selves while the emperor listens. Then watch the war scene and count the extras.",
         ["Bollywood", "Historical", "Romance", "1960s"], "K. Asif"),
    film("film-mother-india-1957", "Mother India (1957)",
         "1957 — the first Indian film nominated for a Best Foreign Language Oscar. Nargis's Radha became the archetypal Indian mother; she nearly drowned filming the flood sequence. The title deliberately echoed Maxim Gorky's 'Mother'.",
         "Mother India (1957) — the first hour", 60,
         "Watch the flood scene knowing it was filmed with a real surge — Nargis was nearly swept away. The film's engine is Birju's resentment: every time the camera frames Radha's hands in the soil, it's foreshadowing the final act's shovel.",
         ["Bollywood", "Drama", "Classic", "1950s"], "Mehboob Khan"),
    film("film-deewaar-1975", "Deewaar (1975)",
         "1975 — the film that codified Bollywood's 'angry young man'. Amitabh Bachchan's 'Mere paas maa hai' is among the most quoted lines in Indian cinema; Yash Chopra shot the confrontation in one long take to let the silence build.",
         "Deewaar (1975) — the first hour", 60,
         "The film is built on brothers choosing opposite sides of the law. Watch the 'Mere paas maa hai' scene and notice the blocking: both brothers refuse to move and the camera never cuts — the standoff is physical before it's verbal.",
         ["Bollywood", "Crime", "Drama", "1970s"], "Yash Chopra"),
    film("film-shree-420-1955", "Shree 420 (1955)",
         "Raj Kapoor's 1955 tramp-in-the-city satire — 'Mera Joota Hai Japani' became the anthem of post-independence India. The title is slang for a con man, after the penal code's Section 420; Kapoor's Chaplin-esque tramp walked the line between them.",
         "Shree 420 (1955) — the first hour", 45,
         "Watch the opening song: Raj's tramp walks into Bombay singing 'Mera Joota Hai Japani' — the whole film's argument in one chorus. Then notice how the same shoes become the metaphor for selling out when he trades them for a suit.",
         ["Bollywood", "Comedy", "Musical", "1950s"], "Raj Kapoor"),
]

NEW_DIRECTORS = [
    director("dire-raj-kapoor", "Raj Kapoor",
             "The showman of Indian cinema — Chaplin's tramp reborn as the Raj-era everyman. Awaara's 'Mera Joota Hai Japani' made him a household name across the Soviet Union, China and the Middle East, decades before globalisation.",
             "Awaara (1951) — the dream sequence", 40,
             "Kapoor's tramp was his Chaplin homage made political. Watch the Awaara dream sequence: the giant shadows, the surrealist city — German expressionism made Indian. Then watch Shree 420's opening song to see the same tramp grow up.",
             ["Bollywood", "Drama", "1950s"]),
    director("dire-guru-dutt", "Guru Dutt",
             "A melancholic perfectionist who directed only eight features — Pyaasa, Kaagaz Ke Phool, Sahib Bibi Aur Ghulam — and is now ranked among cinema's greats. He controlled camera, light and mood like a poet, and was brutally self-critical.",
             "Kaagaz Ke Phool (1959) — the opening", 40,
             "Dutt's own favourite film — and the flop that broke him. Watch the opening ten minutes: a director walks into a crumbling studio, and the flashback is filmed in shadows so deep the sets vanish. He turned his own decline into the film's subject.",
             ["Bollywood", "Drama", "1950s"]),
    director("dire-yash-chopra", "Yash Chopra",
             "From Deewaar's rage to DDLJ's mustard fields — the 'king of romance'. His Switzerland song sequences invented the modern Bollywood travelogue, and his 1970s films taught Hindi cinema how to make villains charismatic.",
             "Silsila (1981) — the Kashmir song sequence", 40,
             "Chopra's signature is the romantic song in a foreign landscape. Watch any Silsila sequence and notice how the choreography is secondary — the camera, the wind and the colour grading carry the emotion. That grammar still runs Bollywood.",
             ["Bollywood", "Romance", "1970s"]),
    director("dire-ramesh-sippy", "Ramesh Sippy",
             "The man who fused the Hollywood western with the Indian masala film. Sholay took three years to become a hit, then held the box-office record for twenty — the patience made his name.",
             "Sholay (1975) — Gabbar's introduction", 35,
             "Sippy introduced Gabbar Singh from behind — a silhouette, a knife, a menacing voice — before any close-up. Watch how long he withholds the face. Then watch the Thakur-gallows scene: the camera stays low, like the hostages.",
             ["Bollywood", "Action", "1970s"]),
    director("dire-rajkumar-hirani", "Rajkumar Hirani",
             "The soft-spoken storyteller whose comedies — 3 Idiots, PK, Sanju — quietly lecture on education, religion and the news media. Every Hirani film breaks box-office records while sneaking in a manifesto.",
             "3 Idiots (2009) — the 'machine' monologue", 30,
             "Hirani's method: the joke comes first, the argument second. Watch Rancho's machine lecture — a TED talk disguised as a classroom scene. Count how many of his gags land on an actual idea. That's the whole trick.",
             ["Bollywood", "Comedy", "2000s"]),
    director("dire-sanjay-leela-bhansali", "Sanjay Leela Bhansali",
             "The maximalist — Devdas's mirrored halls, Bajirao Mastani's battlefields, Padmaavat's opulence. Every frame is composed like a painting; he is said to storyboard sequences down to a single eyelash.",
             "Devdas (2002) — the 'Dola Re Dola' sequence", 40,
             "Bhansali doesn't do realism — he does theatre. Watch 'Dola Re Dola' and notice the geometry: the camera moves in circles, the dancers mirror each other, the chandeliers frame the frame. Every element is choreographed, even the wind.",
             ["Bollywood", "Drama", "2000s"]),
    director("dire-anurag-kashyap", "Anurag Kashyap",
             "The indie disruptor who taught Bollywood to be dangerous. Gangs of Wasseypur — his two-part, five-hour gangster epic — was shot guerrilla-style across Dhanbad's coal country with local non-actors.",
             "Gangs of Wasseypur (2012) — the opening hour", 60,
             "Kashyap's world is oral history with violence: the film opens with a prophecy. Notice how he uses real locations and real faces — the movie feels like a documentary that suddenly remembers it's a Tarantino film.",
             ["Bollywood", "Crime", "2000s"]),
    director("dire-karan-johar", "Karan Johar",
             "The millennial Bollywood brand — Kuch Kuch Hota Hai, Kabhi Khushi Kabhie Gham, Ae Dil Hai Mushkil. Glossy, emotional, song-drenched family dramas that turned diaspora longing into a global aesthetic.",
             "Kuch Kuch Hota Hai (1998) — the friendship-first montage", 35,
             "Johar's formula is friendship disguised as romance. Watch the college montage and notice how the film delays the confession for two hours — the longing is the product. His scenes are staged for the song, not the dialogue.",
             ["Bollywood", "Romance", "1990s"]),
    director("dire-mehboob-khan", "Mehboob Khan",
             "The studio-era titan behind Mother India — the film that turned a farmer's wife into the icon of Indian resilience. He directed 29 films in three decades, mixing social realism with spectacle before either word was fashionable.",
             "Mother India (1957) — the final act", 45,
             "Mehboob shot the finale — a mother forced to choose between her son and her village — as pure opera. Watch how Nargis's face carries the entire moral argument; the film trusts one close-up over a hundred lines of dialogue.",
             ["Bollywood", "Drama", "1950s"]),
]


# ═══════════════════════════════════════════════════════════════════════════
# Main
# ═══════════════════════════════════════════════════════════════════════════
def main():
    problems = []

    # 1. Albums — byline from artist slug (+ overrides).
    albums = load("albums.json")
    missing = []
    for t in albums:
        byline = album_artist(t["id"])
        if not byline:
            missing.append(t["id"])
        insert_byline(t, byline)
    if missing:
        problems.append("albums.json: no artist resolved for %d ids: %s"
                        % (len(missing), ", ".join(sorted(missing))))
    save("albums.json", albums)
    print("albums.json: %d topics, %d with byline" % (len(albums), sum(1 for t in albums if t.get("byline"))))

    # 2. Books — author map.
    books = load("books.json")
    missing = []
    for t in books:
        byline = BOOK_AUTHORS.get(t["id"])
        if not byline:
            missing.append(t["id"])
        insert_byline(t, byline)
    if missing:
        print("  (no author mapped for: %s)" % ", ".join(sorted(missing)))
    save("books.json", books)
    print("books.json: %d topics, %d with byline" % (len(books), sum(1 for t in books if t.get("byline"))))

    # 3. Films — director map + Hollywood region.
    films = load("films.json")
    missing = []
    for t in films:
        byline = FILM_DIRECTORS.get(t["id"])
        if not byline:
            missing.append(t["id"])
        insert_byline(t, byline)
        if t["id"] in FILM_REGION:
            set_region(t, "Hollywood")
    if missing:
        problems.append("films.json: no director mapped for %d ids: %s"
                        % (len(missing), ", ".join(sorted(missing))))
    save("films.json", films)
    print("films.json: %d topics, %d with byline, %d Hollywood"
          % (len(films), sum(1 for t in films if t.get("byline")),
             sum(1 for t in films if "Hollywood" in t.get("tags", []))))

    # 4. Directors — Hollywood region (no byline: they ARE the people).
    directors = load("directors.json")
    for t in directors:
        if t["id"] in DIRECTOR_REGION:
            set_region(t, "Hollywood")
    save("directors.json", directors)
    print("directors.json: %d topics, %d Hollywood"
          % (len(directors), sum(1 for t in directors if "Hollywood" in t.get("tags", []))))

    # 5. Artworks — painter from the trailing " by X" in the name.
    artworks = load("artworks.json")
    missing = []
    for t in artworks:
        name = t.get("name", "")
        if " by " in name:
            painter = name.rsplit(" by ", 1)[-1].strip()
        else:
            painter = ""
            missing.append(t["id"])
        insert_byline(t, painter)
    if missing:
        print("  (no painter parsed for: %s)" % ", ".join(sorted(missing)))
    save("artworks.json", artworks)
    print("artworks.json: %d topics, %d with byline" % (len(artworks), sum(1 for t in artworks if t.get("byline"))))

    # 6. Append the Bollywood batch.
    films = load("films.json")
    existing_films = {t["id"] for t in films}
    added = 0
    for t in NEW_FILMS:
        if t["id"] in existing_films:
            print("  skip (already exists): %s" % t["id"])
            continue
        films.append(t)
        added += 1
    save("films.json", films)
    print("films.json: +%d Bollywood films (total %d)" % (added, len(films)))

    directors = load("directors.json")
    existing_dirs = {t["id"] for t in directors}
    added = 0
    for t in NEW_DIRECTORS:
        if t["id"] in existing_dirs:
            print("  skip (already exists): %s" % t["id"])
            continue
        directors.append(t)
        added += 1
    save("directors.json", directors)
    print("directors.json: +%d Bollywood directors (total %d)" % (added, len(directors)))

    # 7. Length sanity for the new content (schema bar: teaser/instruction ≤ ~300).
    for t in NEW_FILMS + NEW_DIRECTORS:
        for field in ("teaser", "instruction"):
            n = len(t.get("teaser") if field == "teaser" else t["exploreAction"]["instruction"])
            if n > 300:
                problems.append("%s: %s is %d chars" % (t["id"], field, n))

    if problems:
        print("\n⚠ PROBLEMS:")
        for p in problems:
            print("  -", p)
        sys.exit(1)
    print("\nDone — all bylines + regions applied cleanly.")


if __name__ == "__main__":
    main()
