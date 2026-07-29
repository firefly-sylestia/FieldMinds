#!/usr/bin/env python3
"""
Curio Topic Expansion Script v2
===============================
Generates properly-named topics with real-world names per category.
No more "Topic #N" — every generated topic gets a plausible real name.

Usage: python3 scripts/expand_topics.py
"""

import json
import os
import sys
import random
from pathlib import Path

TOPICS_DIR = Path("app/src/main/assets/topics")
random.seed(42)

# ═══════════════════════════════════════════════════════════════════════════
# Real topic name pools — ~200 per category
# ═══════════════════════════════════════════════════════════════════════════

ARTIST_NAMES = [
    "Aretha Franklin", "Johnny Cash", "Ella Fitzgerald", "Chuck Berry", "The Who",
    "The Kinks", "Velvet Underground", "The Doors", "Jimi Hendrix", "Janis Joplin",
    "The Byrds", "Simon and Garfunkel", "Creedence Clearwater", "The Band", "Neil Young",
    "Crosby Stills and Nash", "Elton John", "Pink Floyd", "Led Zeppelin", "Black Sabbath",
    "Deep Purple", "The Ramones", "Talking Heads", "Blondie", "The Clash",
    "Joy Division", "The Cure", "Depeche Mode", "New Order", "The Smiths",
    "R.E.M.", "U2", "The Police", "Michael Jackson", "Whitney Houston",
    "Madonna", "Janet Jackson", "George Michael", "Phil Collins", "Peter Gabriel",
    "Tracy Chapman", "Suzanne Vega", "Sinéad O'Connor", "Annie Lennox", "Kate Bush",
    "Lauryn Hill", "The Fugees", "A Tribe Called Quest", "De La Soul", "Beastie Boys",
    "Public Enemy", "Run-DMC", "Eric B. and Rakim", "Nas", "Jay-Z",
    "The Notorious B.I.G.", "Snoop Dogg", "Dr. Dre", "Eminem", "Missy Elliott",
    "OutKast", "Kendrick Lamar", "Frank Ocean", "Solange", "Tyler the Creator",
    "Childish Gambino", "Lizzo", "Dua Lipa", "The Weeknd", "H.E.R.",
    "Alicia Keys", "John Legend", "Adele", "Ed Sheeran", "Sam Smith",
    "Amy Winehouse", "Florence and the Machine", "Arctic Monkeys", "Vampire Weekend", "Tame Impala",
    "Grimes", "FKA twigs", "James Blake", "Bon Iver", "Sufjan Stevens",
    "St. Vincent", "Arcade Fire", "LCD Soundsystem", "Gorillaz", "Daft Punk",
    "Massive Attack", "Portishead", "Tricky", "Björk", "Aphex Twin",
    "Underworld", "The Chemical Brothers", "Moby", "Fatboy Slim", "The Prodigy",
    "Beck", "The Flaming Lips", "Wilco", "Modest Mouse", "The Strokes",
    "Interpol", "Yeah Yeah Yeahs", "The White Stripes", "Queens of the Stone Age", "Foo Fighters",
    "Nine Inch Nails", "Tool", "System of a Down", "Slipknot", "Korn",
    "Rage Against the Machine", "Green Day", "Blink-182", "Weezer", "Red Hot Chili Peppers",
    "Pearl Jam", "Soundgarden", "Alice in Chains", "Stone Temple Pilots", "Smashing Pumpkins",
    "Lana Del Rey", "Lorde", "Billie Eilish", "Olivia Rodrigo", "Phoebe Bridgers",
    "Maggie Rogers", "Clairo", "Japanese Breakfast", "Wet Leg", "boygenius",
    "Burna Boy", "Wizkid", "Tems", "Rema", "Amaarae",
    "ROSALÍA", "C. Tangana", "Nathy Peluso", "Kali Uchis", "Karol G",
    "BTS", "BLACKPINK", "IU", "Radwimps", "YOASOBI",
    "Stromae", "Angèle", "Christine and the Queens", "Aya Nakamura", "Orelsan",
    "Sevdaliza", "Altin Gün", "Tinariwen", "Bombino", "Mdou Moctar",
    "Khruangbin", "King Gizzard", "Black Midi", "Black Country New Road", "Squid",
    "Caroline Polachek", "Charli XCX", "Rina Sawayama", "Shygirl", "PinkPantheress",
    "Fred again..", "Four Tet", "Jamie xx", "Floating Points", "Overmono",
    "Kelela", "Yves Tumor", "Blood Orange", "Moses Sumney", "Perfume Genius",
]

ALBUM_NAMES = [
    "Thriller by Michael Jackson", "Back in Black by AC/DC", "Rumours by Fleetwood Mac",
    "The Dark Side of the Moon by Pink Floyd", "Born to Run by Bruce Springsteen",
    "London Calling by The Clash", "Exile on Main St. by The Rolling Stones",
    "Blue by Joni Mitchell", "Bridge Over Troubled Water by Simon and Garfunkel",
    "Aja by Steely Dan", "Graceland by Paul Simon", "Tracy Chapman by Tracy Chapman",
    "The Joshua Tree by U2", "Automatic for the People by R.E.M.", "Jagged Little Pill by Alanis Morissette",
    "OK Computer by Radiohead", "Kid A by Radiohead", "In Rainbows by Radiohead",
    "Is This It by The Strokes", "Funeral by Arcade Fire", "Elephant by The White Stripes",
    "Yankee Hotel Foxtrot by Wilco", "Illinois by Sufjan Stevens", "For Emma by Bon Iver",
    "Channel Orange by Frank Ocean", "Blonde by Frank Ocean", "good kid mAAd city by Kendrick Lamar",
    "Lemonade by Beyoncé", "Anti by Rihanna", "CTRL by SZA",
    "After Hours by The Weeknd", "Future Nostalgia by Dua Lipa", "SOUR by Olivia Rodrigo",
    "folklore by Taylor Swift", "evermore by Taylor Swift", "Midnights by Taylor Swift",
    "Melodrama by Lorde", "Norman Rockwell by Lana Del Rey", "Punisher by Phoebe Bridgers",
    "Fetch the Bolt Cutters by Fiona Apple", "Jubilee by Japanese Breakfast", "Preachers Daughter by Ethel Cain",
    "An Evening with Silk Sonic", "Gemini Rights by Steve Lacy", "RENAISSANCE by Beyoncé",
    "Un Verano Sin Ti by Bad Bunny", "MOTOMAMI by ROSALÍA", "El Mal Querer by ROSALÍA",
    "Carrie and Lowell by Sufjan Stevens", "22 A Million by Bon Iver", "Dawn FM by The Weeknd",
    "Currents by Tame Impala", "Lonerism by Tame Impala", "The Slow Rush by Tame Impala",
    "Random Access Memories by Daft Punk", "Discovery by Daft Punk", "Homework by Daft Punk",
    "Since I Left You by The Avalanches", "Cosmogramma by Flying Lotus", "Syro by Aphex Twin",
    "Immunity by Jon Hopkins", "Promises by Floating Points", "Untrue by Burial",
    "Dummy by Portishead", "Mezzanine by Massive Attack", "Maxinquaye by Tricky",
    "Vespertine by Björk", "Homogenic by Björk", "Post by Björk",
    "Hounds of Love by Kate Bush", "The Kick Inside by Kate Bush", "The Sensual World by Kate Bush",
    "Souvlaki by Slowdive", "Loveless by My Bloody Valentine", "Heaven or Las Vegas by Cocteau Twins",
    "Disintegration by The Cure", "The Queen Is Dead by The Smiths", "Unknown Pleasures by Joy Division",
    "Power Corruption and Lies by New Order", "Violator by Depeche Mode", "Songs from the Big Chair by Tears for Fears",
    "Achtung Baby by U2", "Zooropa by U2", "Sign o the Times by Prince",
    "Purple Rain by Prince", "1999 by Prince", "Dirty Mind by Prince",
    "What's Going On by Marvin Gaye", "Innervisions by Stevie Wonder", "Songs in the Key of Life by Stevie Wonder",
    "Head Hunters by Herbie Hancock", "Bitches Brew by Miles Davis", "A Love Supreme by John Coltrane",
    "Kind of Blue by Miles Davis", "The Shape of Jazz to Come by Ornette Coleman", "Time Out by Dave Brubeck",
    "Moanin by Art Blakey", "Saxophone Colossus by Sonny Rollins", "Giant Steps by John Coltrane",
    "A Charlie Brown Christmas by Vince Guaraldi", "Getz/Gilberto by Stan Getz", "Wave by Antonio Carlos Jobim",
    "Buena Vista Social Club", "Talking Timbuktu by Ali Farka Touré", "Graceland by Paul Simon",
    "Paul's Boutique by Beastie Boys", "3 Feet High and Rising by De La Soul", "The Low End Theory by A Tribe Called Quest",
    "Illmatic by Nas", "Ready to Die by Biggie", "The Miseducation by Lauryn Hill",
    "Things Fall Apart by The Roots", "The Score by Fugees", "Madvillainy by Madvillain",
    "Donuts by J Dilla", "Endtroducing by DJ Shadow", "Since I Left You by The Avalanches",
    "Remain in Light by Talking Heads", "Fear of Music by Talking Heads", "Parallel Lines by Blondie",
    "Marquee Moon by Television", "Entertainment by Gang of Four", "Q Are We Not Men by Devo",
    "Transformer by Lou Reed", "The Idiot by Iggy Pop", "Raw Power by The Stooges",
    "Horses by Patti Smith", "Marquee Moon by Television", "Rocket to Russia by Ramones",
    "Doolittle by Pixies", "Surfer Rosa by Pixies", "Daydream Nation by Sonic Youth",
    "Goo by Sonic Youth", "Slanted and Enchanted by Pavement", "Crooked Rain by Pavement",
    "Spiderland by Slint", "The Lonesome Crowded West by Modest Mouse", "The Moon and Antarctica by Modest Mouse",
    "Either/Or by Elliott Smith", "XO by Elliott Smith", "Figure 8 by Elliott Smith",
    "Grace by Jeff Buckley", "Sketches for My Sweetheart by Jeff Buckley", "O by Damien Rice",
    "The Bends by Radiohead", "Pablo Honey by Radiohead", "Hail to the Thief by Radiohead",
    "A Moon Shaped Pool by Radiohead", "Amnesiac by Radiohead", "King of Limbs by Radiohead",
    "Whatever People Say by Arctic Monkeys", "AM by Arctic Monkeys", "Tranquility Base by Arctic Monkeys",
]

# More name pools for the other categories...
FILM_NAMES = [
    "Citizen Kane (1941)", "Casablanca (1942)", "The Third Man (1949)", "Rashomon (1950)",
    "Singin' in the Rain (1952)", "Tokyo Story (1953)", "Rear Window (1954)", "Seven Samurai (1954)",
    "The Night of the Hunter (1955)", "The Seventh Seal (1957)", "Vertigo (1958)", "Breathless (1960)",
    "Psycho (1960)", "La Dolce Vita (1960)", "Yojimbo (1961)", "Lawrence of Arabia (1962)",
    "8½ (1963)", "Dr. Strangelove (1964)", "The Good the Bad and the Ugly (1966)", "2001 A Space Odyssey (1968)",
    "Once Upon a Time in the West (1968)", "The Conformist (1970)", "A Clockwork Orange (1971)", "The Godfather (1972)",
    "Aguirre the Wrath of God (1972)", "Chinatown (1974)", "The Godfather Part II (1974)", "Taxi Driver (1976)",
    "Network (1976)", "Annie Hall (1977)", "Close Encounters of the Third Kind (1977)", "Apocalypse Now (1979)",
    "Stalker (1979)", "Raging Bull (1980)", "Blade Runner (1982)", "Koyaanisqatsi (1982)",
    "The Thing (1982)", "Paris Texas (1984)", "Brazil (1985)", "Blue Velvet (1986)",
    "Wings of Desire (1987)", "Akira (1988)", "Do the Right Thing (1989)", "Goodfellas (1990)",
    "Reservoir Dogs (1992)", "The Piano (1993)", "Schindler's List (1993)", "Pulp Fiction (1994)",
    "Chungking Express (1994)", "Before Sunrise (1995)", "Fargo (1996)", "Titanic (1997)",
    "The Big Lebowski (1998)", "The Matrix (1999)", "Beau Travail (1999)", "In the Mood for Love (2000)",
    "Amélie (2001)", "Mulholland Drive (2001)", "Spirited Away (2001)", "City of God (2002)",
    "Lost in Translation (2003)", "Oldboy (2003)", "Eternal Sunshine (2004)", "Brokeback Mountain (2005)",
    "Pan's Labyrinth (2006)", "Children of Men (2006)", "There Will Be Blood (2007)", "No Country for Old Men (2007)",
    "WALL-E (2008)", "The Dark Knight (2008)", "A Serious Man (2009)", "Inglourious Basterds (2009)",
    "Inception (2010)", "The Social Network (2010)", "A Separation (2011)", "The Tree of Life (2011)",
    "Amour (2012)", "Her (2013)", "Gravity (2013)", "Boyhood (2014)",
    "Whiplash (2014)", "Mad Max Fury Road (2015)", "Moonlight (2016)", "Arrival (2016)",
    "Get Out (2017)", "Call Me by Your Name (2017)", "Roma (2018)", "Parasite (2019)",
    "Portrait of a Lady on Fire (2019)", "Soul (2020)", "Dune (2021)", "Drive My Car (2021)",
    "The Power of the Dog (2021)", "Everything Everywhere All at Once (2022)", "Tár (2022)", "The Fabelmans (2022)",
    "Oppenheimer (2023)", "Barbie (2023)", "Killers of the Flower Moon (2023)", "Past Lives (2023)",
    "Poor Things (2023)", "The Zone of Interest (2023)", "Anatomy of a Fall (2023)", "The Holdovers (2023)",
]

DIRECTOR_NAMES = [
    "Alfred Hitchcock", "Akira Kurosawa", "Ingmar Bergman", "Federico Fellini", "Stanley Kubrick",
    "Andrei Tarkovsky", "François Truffaut", "Jean-Luc Godard", "Satyajit Ray", "Robert Bresson",
    "Yasujirō Ozu", "John Ford", "Billy Wilder", "Orson Welles", "David Lean",
    "Luis Buñuel", "Michelangelo Antonioni", "Agnes Varda", "Wim Wenders", "Rainer Werner Fassbinder",
    "Martin Scorsese", "Francis Ford Coppola", "Steven Spielberg", "George Lucas", "David Lynch",
    "Woody Allen", "Spike Lee", "Terrence Malick", "Ridley Scott", "James Cameron",
    "John Carpenter", "David Cronenberg", "Coen Brothers", "Tim Burton", "Quentin Tarantino",
    "Paul Thomas Anderson", "Wes Anderson", "Sofia Coppola", "Kathryn Bigelow", "Jane Campion",
    "Hayao Miyazaki", "Isao Takahata", "Wong Kar-wai", "Zhang Yimou", "Ang Lee",
    "Park Chan-wook", "Bong Joon-ho", "Chan-wook Park", "Lee Chang-dong", "Hirokazu Kore-eda",
    "Abbas Kiarostami", "Asghar Farhadi", "Jafar Panahi", "Mira Nair", "Deepa Mehta",
    "Pedro Almodóvar", "Guillermo del Toro", "Alejandro González Iñárritu", "Alfonso Cuarón", "Lucrecia Martel",
    "Claire Denis", "Jean-Pierre Jeunet", "Luc Besson", "Michael Haneke", "Lars von Trier",
    "Christopher Nolan", "Denis Villeneuve", "Damien Chazelle", "Barry Jenkins", "Greta Gerwig",
    "Chloé Zhao", "Jordan Peele", "Ari Aster", "Robert Eggers", "Sean Baker",
    "Yorgos Lanthimos", "Paolo Sorrentino", "Ruben Östlund", "Lynne Ramsay", "Andrea Arnold",
    "Céline Sciamma", "Alice Rohrwacher", "Mati Diop", "Lulu Wang", "Emerald Fennell",
    "Edward Yang", "Hou Hsiao-hsien", "Tsai Ming-liang", "Apichatpong Weerasethakul", "Ryusuke Hamaguchi",
    "Naoko Ogigami", "Shunji Iwai", "Na Hong-jin", "Kim Jee-woon", "Johnnie To",
    "Sergio Leone", "Dario Argento", "Bernardo Bertolucci", "Nanni Moretti", "Paolo Sorrentino",
    "Fritz Lang", "F.W. Murnau", "Ernst Lubitsch", "Max Ophüls", "Douglas Sirk",
]

AUTHOR_NAMES = [
    "Jane Austen", "Charles Dickens", "George Eliot", "Emily Brontë", "Herman Melville",
    "Mark Twain", "Oscar Wilde", "Henry James", "Thomas Hardy", "Joseph Conrad",
    "Edith Wharton", "Marcel Proust", "D.H. Lawrence", "Ernest Hemingway", "Zora Neale Hurston",
    "Graham Greene", "Evelyn Waugh", "Albert Camus", "Jean-Paul Sartre", "Simone de Beauvoir",
    "J.D. Salinger", "John Steinbeck", "Truman Capote", "Harper Lee", "James Baldwin",
    "Jack Kerouac", "Tennessee Williams", "Ralph Ellison", "Saul Bellow", "John Updike",
    "Cormac McCarthy", "Philip Roth", "Don DeLillo", "Thomas Pynchon", "Kurt Vonnegut",
    "Alice Munro", "Margaret Atwood", "Toni Morrison", "Maya Angelou", "Ursula K. Le Guin",
    "Gabriel García Márquez", "Isabel Allende", "Milan Kundera", "Jorge Luis Borges", "Julio Cortázar",
    "Chinua Achebe", "Chimamanda Ngozi Adichie", "Wole Soyinka", "Ngũgĩ wa Thiong'o", "Arundhati Roy",
    "Salman Rushdie", "V.S. Naipaul", "Kazuo Ishiguro", "Yukio Mishima", "Haruki Murakami",
    "Orhan Pamuk", "Elena Ferrante", "J.K. Rowling", "George R.R. Martin", "Stephen King",
    "Neil Gaiman", "Terry Pratchett", "Douglas Adams", "Octavia Butler", "N.K. Jemisin",
    "Ted Chiang", "Liu Cixin", "Colson Whitehead", "Jesmyn Ward", "Ocean Vuong",
    "Sally Rooney", "Brandon Taylor", "R.F. Kuang", "Susanna Clarke", "Madeline Miller",
    "Han Kang", "Sayaka Murata", "Mieko Kawakami", "Yoko Ogawa", "Hiromi Kawakami",
    "Clarice Lispector", "Machado de Assis", "Roberto Bolaño", "Mariana Enríquez", "Samanta Schweblin",
    "David Foster Wallace", "Jonathan Franzen", "Zadie Smith", "Jeffrey Eugenides", "Junot Díaz",
    "Michael Chabon", "Marilynne Robinson", "Louise Erdrich", "Barbara Kingsolver", "Ann Patchett",
    "Hilary Mantel", "Ian McEwan", "Julian Barnes", "A.S. Byatt", "Kazuo Ishiguro",
    "Daphne du Maurier", "Patricia Highsmith", "Agatha Christie", "Raymond Chandler", "Dashiell Hammett",
    "Shirley Jackson", "Flannery O'Connor", "Eudora Welty", "Carson McCullers", "Willa Cather",
]

BOOK_NAMES = [
    "Pride and Prejudice (1813)", "Moby-Dick (1851)", "Crime and Punishment (1866)", "Anna Karenina (1877)",
    "Heart of Darkness (1899)", "The Great Gatsby (1925)", "To the Lighthouse (1927)", "Brave New World (1932)",
    "The Grapes of Wrath (1939)", "Native Son (1940)", "1984 (1949)", "The Catcher in the Rye (1951)",
    "Invisible Man (1952)", "Fahrenheit 451 (1953)", "Lord of the Flies (1954)", "Lolita (1955)",
    "Things Fall Apart (1958)", "To Kill a Mockingbird (1960)", "Catch-22 (1961)", "One Flew Over the Cuckoo's Nest (1962)",
    "The Bell Jar (1963)", "One Hundred Years of Solitude (1967)", "Slaughterhouse-Five (1969)", "The Godfather (1969)",
    "The Bluest Eye (1970)", "Fear and Loathing in Las Vegas (1971)", "Sula (1973)", "The Gulag Archipelago (1973)",
    "Song of Solomon (1977)", "The Hitchhiker's Guide to the Galaxy (1979)", "Midnight's Children (1981)", "The Color Purple (1982)",
    "Neuromancer (1984)", "The Handmaid's Tale (1985)", "Beloved (1987)", "Norwegian Wood (1987)",
    "The Satanic Verses (1988)", "The Remains of the Day (1989)", "The Things They Carried (1990)", "American Psycho (1991)",
    "The English Patient (1992)", "The Virgin Suicides (1993)", "Snow Crash (1992)", "Trainspotting (1993)",
    "Infinite Jest (1996)", "Fight Club (1996)", "Memoirs of a Geisha (1997)", "Harry Potter (1997)",
    "The God of Small Things (1997)", "Disgrace (1999)", "White Teeth (2000)", "The Corrections (2001)",
    "Atonement (2001)", "Life of Pi (2001)", "Middlesex (2002)", "The Kite Runner (2003)",
    "Cloud Atlas (2004)", "Never Let Me Go (2005)", "The Road (2006)", "The Brief Wondrous Life of Oscar Wao (2007)",
    "Wolf Hall (2009)", "The Help (2009)", "A Visit from the Goon Squad (2010)", "Swamplandia (2011)",
    "Gone Girl (2012)", "The Goldfinch (2013)", "Americanah (2013)", "All the Light We Cannot See (2014)",
    "Between the World and Me (2015)", "The Underground Railroad (2016)", "Lincoln in the Bardo (2017)", "Sing Unburied Sing (2017)",
    "Normal People (2018)", "Circe (2018)", "The Testaments (2019)", "Piranesi (2020)",
    "Klara and the Sun (2021)", "Sea of Tranquility (2022)", "Demon Copperhead (2022)", "Tomorrow and Tomorrow (2022)",
    "Chain-Gang All-Stars (2023)", "The Fraud (2023)", "Yellowface (2023)", "The Bee Sting (2023)",
    "North Woods (2023)", "Tom Lake (2023)", "Wellness (2023)", "The Heaven Earth Grocery (2023)",
    "Birnam Wood (2023)", "Lone Women (2023)", "The Wager (2023)", "How to Say Babylon (2023)",
    "King A Life (2023)", "Blackouts (2023)", "The Vaster Wilds (2023)", "Prophet Song (2023)",
    "Western Lane (2023)", "This Other Eden (2023)", "Old God's Time (2023)", "In Memoriam (2023)",
    "The Guest (2023)", "Let Us Descend (2023)", "Tremor (2023)", "Roman Stories (2023)",
    "Everything's Fine (2023)", "Land of Milk and Honey (2023)", "The New Naturals (2023)", "Family Meal (2023)",
]

PAINTER_NAMES = [
    "Giotto", "Fra Angelico", "Piero della Francesca", "Hieronymus Bosch", "Matthias Grünewald",
    "Albrecht Dürer", "Pieter Bruegel the Elder", "El Greco", "Artemisia Gentileschi", "Judith Leyster",
    "Rachel Ruysch", "Angelica Kauffman", "Élisabeth Vigée Le Brun", "Francisco Goya", "J.M.W. Turner",
    "John Constable", "Eugène Delacroix", "Camille Corot", "Jean-François Millet", "Gustave Courbet",
    "Rosa Bonheur", "James McNeill Whistler", "Édouard Manet", "Berthe Morisot", "Mary Cassatt",
    "Edgar Degas", "Paul Cézanne", "Georges Seurat", "Vincent van Gogh", "Paul Gauguin",
    "Henri de Toulouse-Lautrec", "Gustav Klimt", "Egon Schiele", "Edvard Munch", "Wassily Kandinsky",
    "Piet Mondrian", "Kazimir Malevich", "Marc Chagall", "René Magritte", "Salvador Dalí",
    "Joan Miró", "Paul Klee", "Frida Kahlo", "Diego Rivera", "Tamara de Lempicka",
    "Georgia O'Keeffe", "Edward Hopper", "Grant Wood", "Norman Rockwell", "Andrew Wyeth",
    "Francis Bacon", "Lucian Freud", "David Hockney", "Bridget Riley", "Gerhard Richter",
    "Anselm Kiefer", "Georg Baselitz", "Yayoi Kusama", "Takashi Murakami", "Yoshitomo Nara",
    "Zao Wou-Ki", "Lee Krasner", "Helen Frankenthaler", "Agnes Martin", "Joan Mitchell",
    "Alice Neel", "Faith Ringgold", "Betye Saar", "Howardena Pindell", "Lorna Simpson",
    "Kerry James Marshall", "Kehinde Wiley", "Amy Sherald", "Toyin Ojih Odutola", "Njideka Akunyili Crosby",
    "Mickalene Thomas", "Wangechi Mutu", "Julie Mehretu", "Lynette Yiadom-Boakye", "Tschabalala Self",
    "Jadé Fadojutimi", "Flora Yukhnovich", "Salman Toor", "Shara Hughes", "Amoako Boafo",
    "Otis Kwame Kye Quaicoe", "Tunji Adeniyi-Jones", "Issy Wood", "Louise Giovanelli", "Caroline Walker",
]

ARTWORK_NAMES = [
    "The Arnolfini Portrait (1434) by Jan van Eyck", "The Last Supper (1498) by Leonardo da Vinci",
    "Mona Lisa (1503) by Leonardo da Vinci", "The Creation of Adam (1512) by Michelangelo",
    "The Garden of Earthly Delights (1505) by Hieronymus Bosch", "The School of Athens (1511) by Raphael",
    "The Night Watch (1642) by Rembrandt", "Girl with a Pearl Earring (1665) by Johannes Vermeer",
    "Las Meninas (1656) by Diego Velázquez", "The Swing (1767) by Jean-Honoré Fragonard",
    "The Death of Marat (1793) by Jacques-Louis David", "The Third of May 1808 (1814) by Francisco Goya",
    "The Raft of the Medusa (1819) by Théodore Géricault", "Liberty Leading the People (1830) by Eugène Delacroix",
    "The Great Wave off Kanagawa (1831) by Hokusai", "The Fighting Temeraire (1839) by J.M.W. Turner",
    "Olympia (1863) by Édouard Manet", "The Luncheon on the Grass (1863) by Édouard Manet",
    "Impression Sunrise (1872) by Claude Monet", "The Dance Class (1874) by Edgar Degas",
    "Nocturne in Black and Gold (1875) by Whistler", "A Sunday on La Grande Jatte (1886) by Georges Seurat",
    "The Starry Night (1889) by Vincent van Gogh", "Irises (1889) by Vincent van Gogh",
    "The Scream (1893) by Edvard Munch", "The Kiss (1908) by Gustav Klimt",
    "Les Demoiselles d'Avignon (1907) by Picasso", "Guernica (1937) by Pablo Picasso",
    "Composition VII (1913) by Wassily Kandinsky", "Black Square (1915) by Kazimir Malevich",
    "Fountain (1917) by Marcel Duchamp", "American Gothic (1930) by Grant Wood",
    "The Persistence of Memory (1931) by Salvador Dalí", "Nighthawks (1942) by Edward Hopper",
    "Autumn Rhythm Number 30 (1950) by Jackson Pollock", "Campbell's Soup Cans (1962) by Andy Warhol",
    "Marilyn Diptych (1962) by Andy Warhol", "The Dinner Party (1979) by Judy Chicago",
    "Balloon Dog Orange (2000) by Jeff Koons", "Cloud Gate (2006) by Anish Kapoor",
    "Sunflower Seeds (2010) by Ai Weiwei", "The Weather Project (2003) by Olafur Eliasson",
    "Infinity Mirror Room (1965) by Yayoi Kusama", "Maman (1999) by Louise Bourgeois",
    "My Bed (1998) by Tracey Emin", "Spiral Jetty (1970) by Robert Smithson",
    "The Gates (2005) by Christo and Jeanne-Claude", "Rain Room (2012) by Random International",
    "The Physical Impossibility of Death (1991) by Damien Hirst", "Untitled (1991) by Felix Gonzalez-Torres",
    "Shark (1988) by Jean-Michel Basquiat", "Untitled Boxer (1982) by Jean-Michel Basquiat",
    "Flag (1954) by Jasper Johns", "Whaam! (1963) by Roy Lichtenstein",
    "LOVE (1970) by Robert Indiana", "The Dinner Party (1979) by Judy Chicago",
    "Water Lilies (1919) by Claude Monet",
]

SCIENTIST_NAMES = [
    "Isaac Newton", "Galileo Galilei", "Nicolaus Copernicus", "Johannes Kepler", "Robert Hooke",
    "Antonie van Leeuwenhoek", "Carl Linnaeus", "Antoine Lavoisier", "Joseph Priestley", "Benjamin Franklin",
    "Humphry Davy", "Michael Faraday", "Charles Darwin", "Gregor Mendel", "Louis Pasteur",
    "Dmitri Mendeleev", "James Clerk Maxwell", "Lord Kelvin", "Heinrich Hertz", "Wilhelm Röntgen",
    "Marie Curie", "Pierre Curie", "Ernest Rutherford", "Niels Bohr", "Max Planck",
    "Albert Einstein", "Werner Heisenberg", "Erwin Schrödinger", "Paul Dirac", "Wolfgang Pauli",
    "Enrico Fermi", "Lise Meitner", "Otto Hahn", "Emmy Noether", "Alan Turing",
    "John von Neumann", "Kurt Gödel", "Edwin Hubble", "Georges Lemaître", "Subrahmanyan Chandrasekhar",
    "John Bardeen", "Richard Feynman", "Murray Gell-Mann", "Linus Pauling", "Dorothy Hodgkin",
    "Rosalind Franklin", "Francis Crick", "James Watson", "Barbara McClintock", "Lynn Margulis",
    "Stephen Hawking", "Roger Penrose", "Carl Sagan", "Neil deGrasse Tyson", "Jane Goodall",
    "Dian Fossey", "Rachel Carson", "E.O. Wilson", "David Attenborough", "Richard Dawkins",
    "Tim Berners-Lee", "Vint Cerf", "Grace Hopper", "Margaret Hamilton", "Ada Lovelace",
    "Katherine Johnson", "Mary Jackson", "Dorothy Vaughan", "Sally Ride", "Mae Jemison",
    "Chien-Shiung Wu", "Vera Rubin", "Jocelyn Bell Burnell", "Donna Strickland", "Andrea Ghez",
    "Emmanuelle Charpentier", "Jennifer Doudna", "Katalin Karikó", "Tu Youyou", "Françoise Barré-Sinoussi",
    "Rita Levi-Montalcini", "Barbara McClintock", "Gerty Cori", "Maria Goeppert Mayer", "May-Britt Moser",
    "Edward Jenner", "Alexander Fleming", "Jonas Salk", "Albert Sabin", "Gertrude Elion",
    "Frederick Banting", "Charles Best", "Barry Marshall", "Robin Warren", "Harold Varmus",
    "Santiago Ramón y Cajal", "Eric Kandel", "Oliver Sacks", "V.S. Ramachandran", "Daniel Kahneman",
    "Amos Tversky", "Noam Chomsky", "B.F. Skinner", "Jean Piaget", "Lev Vygotsky",
    "Edward Witten", "Juan Maldacena", "Cumrun Vafa", "Brian Greene", "Lisa Randall",
]

DISCOVERY_NAMES = [
    "Penicillin (1928)", "The Structure of DNA (1953)", "Vaccination (1796)", "Anesthesia (1846)",
    "Germ Theory of Disease (1860s)", "Pasteurization (1864)", "Antiseptic Surgery (1867)", "X-Rays (1895)",
    "Radioactivity (1896)", "The Electron (1897)", "Quantum Theory (1900)", "Relativity (1905)",
    "The Atomic Nucleus (1911)", "The Proton (1919)", "The Neutron (1932)", "Nuclear Fission (1938)",
    "The Transistor (1947)", "The Laser (1960)", "The Cosmic Microwave Background (1965)", "Pulsars (1967)",
    "Plate Tectonics (1960s)", "The Ozone Hole (1985)", "CRISPR Gene Editing (2012)", "Gravitational Waves (2015)",
    "The Higgs Boson (2012)", "Exoplanets (1995)", "The Big Bang Theory", "Evolution by Natural Selection (1859)",
    "Mendelian Genetics (1866)", "The Periodic Table (1869)", "The Electron Microscope (1931)", "Radiocarbon Dating (1949)",
    "The First Antibiotic (1932)", "The Polio Vaccine (1955)", "The Structure of Insulin (1969)",
    "The First Organ Transplant (1954)", "MRI Imaging (1973)", "CT Scanning (1971)", "The Human Genome Project (2003)",
    "Stem Cells (1998)", "RNA Interference (1998)", "Induced Pluripotent Stem Cells (2006)", "mRNA Vaccines (2020)",
    "The First Exoplanet (1992)", "Dark Energy (1998)", "The Accelerating Universe (1998)", "Neutrino Oscillations (1998)",
    "The God Particle (2012)", "Topological Insulators (2007)", "Graphene (2004)", "Fullerenes (1985)",
    "The First Black Hole Image (2019)", "The Theory of Everything (ongoing)", "Chaos Theory (1960s)", "Fractals (1975)",
    "The World Wide Web (1989)", "The Internet Protocol (1974)", "Public Key Cryptography (1976)", "The Smartphone (2007)",
    "Deep Learning Neural Networks (2012)", "AlphaFold Protein Folding (2020)", "Self-Driving Cars (2010s)", "Reusable Rockets (2015)",
    "The Structure of the Ribosome (2000)", "Telomerase (1984)", "Apoptosis (1972)", "The Cell Cycle (2001)",
    "The First Clone (Dolly 1996)", "Optogenetics (2005)", "The Microbiome (2010s)", "The First Gene Therapy (1990)",
    "The Ozone Layer Recovery (ongoing)", "Climate Change Science (1988)", "The Keeling Curve (1958)", "Ocean Acidification (2000s)",
    "The Extinction of the Dinosaurs (1980)", "Lucy the Australopithecus (1974)", "Homo Naledi (2013)", "Homo Floresiensis (2003)",
    "Fire (prehistoric)", "The Wheel (3500 BC)", "Agriculture (10000 BC)", "Writing (3200 BC)",
    "The Printing Press (1440)", "The Steam Engine (1712)", "Electricity (1800)", "The Telephone (1876)",
    "The Light Bulb (1879)", "Radio (1895)", "Television (1927)", "The Computer (1940s)",
    "The Microprocessor (1971)", "The Internet (1983)", "The Search Engine (1996)", "Social Media (2004)",
]

WILDCARD_NAMES = [
    "The Voynich Manuscript", "The Antikythera Mechanism", "The Nazca Lines", "Stonehenge",
    "The Terracotta Army", "Machu Picchu", "Angkor Wat", "Petra",
    "Easter Island Moai", "The Great Pyramid of Giza", "Chichén Itzá", "The Colosseum",
    "The Great Wall of China", "The Taj Mahal", "The Forbidden City", "Mont Saint-Michel",
    "Sagrada Família", "The Sydney Opera House", "The Golden Gate Bridge", "The Eiffel Tower",
    "The Northern Lights", "The Grand Canyon", "Victoria Falls", "Mount Everest",
    "The Great Barrier Reef", "The Amazon Rainforest", "The Sahara Desert", "The Dead Sea",
    "Yosemite National Park", "Yellowstone", "Galápagos Islands", "Iceland's Geothermal Springs",
    "The Svalbard Global Seed Vault", "The International Space Station", "The Large Hadron Collider", "The Hubble Telescope",
    "The James Webb Space Telescope", "The Curiosity Mars Rover", "Voyager 1 Spacecraft", "Apollo 11 Moon Landing",
    "The Dead Sea Scrolls", "The Rosetta Stone", "The Bayeux Tapestry", "The Gutenberg Bible",
    "The Magna Carta", "The Declaration of Independence", "The Constitution", "The Universal Declaration of Human Rights",
    "The Turing Test", "Schrödinger's Cat", "The Butterfly Effect", "The Mandela Effect",
    "The Placebo Effect", "Synesthesia", "Lucid Dreaming", "The Overview Effect",
    "The Uncanny Valley", "The Golden Ratio", "Fibonacci Sequence", "Pi Day",
    "The Library of Alexandria", "The Hanging Gardens of Babylon", "Atlantis", "El Dorado",
    "The Bermuda Triangle", "Crop Circles", "The Tunguska Event", "The Wow Signal",
    "The Taos Hum", "The Dancing Plague of 1518", "The Lost Colony of Roanoke", "The Mary Celeste",
    "The Oak Island Money Pit", "The Dyatlov Pass Incident", "The Zodiac Killer", "D.B. Cooper",
    "The Somerton Man", "The Tamam Shud Case", "The Lead Masks Case", "The Isdal Woman",
    "Polyglots", "Savants", "Prodigies", "Synesthetes",
    "The Paris Catacombs", "The Sedlec Ossuary", "The Island of the Dolls", "Aokigahara Forest",
    "The Winchester Mystery House", "The Winchester Mystery House", "Centralia Pennsylvania", "Pripyat Chernobyl",
    "Hashima Island", "Socotra Island", "Svalbard", "Tristan da Cunha",
    "The Door to Hell Turkmenistan", "The Eye of the Sahara", "Lake Natron Tanzania", "The Great Blue Hole Belize",
    "Waitomo Glowworm Caves", "Salar de Uyuni", "Pamukkale Turkey", "Zhangjiajie National Forest",
    "The Wave Arizona", "Antelope Canyon", "Bryce Canyon", "Monument Valley",
    "The Tea Ceremony", "Ikebana", "Kintsugi", "Wabi-Sabi",
    "Hygge", "Lagom", "Friluftsliv", "Sisu",
    "The KonMari Method", "The Pomodoro Technique", "The Bullet Journal", "The Getting Things Done Method",
    "The Slow Movement", "The Minimalism Movement", "The Tiny House Movement", "The Van Life Movement",
]

# ═══════════════════════════════════════════════════════════════════════════
# Name pools indexed by category slug
# ═══════════════════════════════════════════════════════════════════════════

NAME_POOLS = {
    "artists": ARTIST_NAMES,
    "albums": ALBUM_NAMES,
    "directors": DIRECTOR_NAMES,
    "films": FILM_NAMES,
    "authors": AUTHOR_NAMES,
    "books": BOOK_NAMES,
    "painters": PAINTER_NAMES,
    "artworks": ARTWORK_NAMES,
    "scientists": SCIENTIST_NAMES,
    "discoveries": DISCOVERY_NAMES,
    "wildcard": WILDCARD_NAMES,
}

# ═══════════════════════════════════════════════════════════════════════════
# Category configuration
# ═══════════════════════════════════════════════════════════════════════════

CATEGORIES = {
    "artists":    {"categoryId": "ARTISTS",    "subtype": "Artist",          "verb": "Listen",   "durRange": (3, 55)},
    "albums":     {"categoryId": "ALBUMS",     "subtype": "Album",           "verb": "Listen",   "durRange": (25, 80)},
    "directors":  {"categoryId": "DIRECTORS",  "subtype": "Director",        "verb": "Watch",    "durRange": (60, 150)},
    "films":      {"categoryId": "FILMS",      "subtype": "Film",            "verb": "Watch",    "durRange": (70, 180)},
    "authors":    {"categoryId": "AUTHORS",    "subtype": "Author",          "verb": "Read",     "durRange": (10, 45)},
    "books":      {"categoryId": "BOOKS",      "subtype": "Book",            "verb": "Read",     "durRange": (10, 45)},
    "painters":   {"categoryId": "PAINTERS",   "subtype": "Painter",         "verb": "Look at",  "durRange": (3, 10)},
    "artworks":   {"categoryId": "ARTWORKS",   "subtype": "Artwork",         "verb": "Look at",  "durRange": (3, 10)},
    "scientists": {"categoryId": "SCIENTISTS", "subtype": "Scientist",       "verb": "Explore",  "durRange": (5, 20)},
    "discoveries":{"categoryId": "DISCOVERIES","subtype": "Discovery",       "verb": "Explore",  "durRange": (5, 15)},
    "wildcard":   {"categoryId": "WILDCARD",   "subtype": "Curiosity",       "verb": "Explore",  "durRange": (3, 30)},
}

TAG_POOLS = {
    "artists": [["Rock", "1970s"], ["Jazz", "1950s"], ["Electronic", "2000s"], ["Hip-Hop", "1990s"],
                ["Classical", "19th Century"], ["Pop", "2010s"], ["Folk", "1960s"], ["Soul", "1970s"]],
    "albums": [["Electronic", "1990s"], ["Rock", "1970s"], ["Pop", "2000s"], ["Jazz", "1960s"],
               ["Hip-Hop", "2010s"], ["Soul", "1970s"], ["Alternative", "1990s"], ["Indie", "2020s"]],
    "directors": [["Drama", "20th Century"], ["Thriller", "21st Century"], ["Comedy", "20th Century"],
                  ["Sci-Fi", "21st Century"], ["Indie", "20th Century"], ["Foreign", "21st Century"]],
    "films": [["Drama", "1990s"], ["Sci-Fi", "2000s"], ["Comedy", "1980s"], ["Thriller", "2010s"],
              ["Animation", "2000s"], ["Horror", "1970s"], ["Documentary", "2010s"], ["Foreign", "2000s"]],
    "authors": [["Fiction", "20th Century"], ["Poetry", "19th Century"], ["Non-Fiction", "21st Century"],
                ["Sci-Fi", "20th Century"], ["Philosophy", "19th Century"], ["Mystery", "20th Century"]],
    "books": [["Fiction", "20th Century"], ["Classic", "19th Century"], ["Sci-Fi", "20th Century"],
              ["Memoir", "2000s"], ["Fantasy", "20th Century"], ["Mystery", "20th Century"]],
    "painters": [["Impressionism", "19th Century"], ["Modernism", "20th Century"], ["Baroque", "17th Century"],
                 ["Contemporary", "21st Century"], ["Renaissance", "15th Century"], ["Expressionism", "20th Century"]],
    "artworks": [["Oil Painting", "Classical"], ["Sculpture", "Modern"], ["Installation", "Contemporary"],
                 ["Photography", "20th Century"], ["Mixed Media", "21st Century"]],
    "scientists": [["Physics", "20th Century"], ["Biology", "19th Century"], ["Chemistry", "20th Century"],
                   ["Mathematics", "18th Century"], ["Neuroscience", "21st Century"], ["Astronomy", "20th Century"]],
    "discoveries": [["Physics", "20th Century"], ["Biology", "19th Century"], ["Medicine", "20th Century"],
                    ["Chemistry", "19th Century"], ["Astronomy", "20th Century"]],
    "wildcard": [["Mystery", "Global"], ["Phenomenon", "Earth"], ["Tradition", "Cultural"],
                 ["Oddity", "Historical"], ["Curiosity", "Modern"]],
}

INSTRUCTION_POOLS = {
    "Listen": [
        "Notice how the production layers create space. Listen once with headphones and once without — the mix changes.",
        "Pay attention to the bassline. It shifts subtly from verse to chorus in a way most listeners miss.",
        "Focus on the vocal delivery. Every inflection carries deliberate weight — this wasn't a one-take recording.",
        "Listen for the background textures. The producer buried details that reward headphone-listening.",
        "Track the arrangement. The song builds toward a moment at the midpoint — find it.",
    ],
    "Watch": [
        "Pay attention to how the camera frames each scene. Every composition is working as visual commentary.",
        "Notice the color palette shift as the story progresses. It tells you everything before the dialogue does.",
        "Watch the background actors during the key scene — their positioning reveals the power dynamics.",
        "Track how lighting changes throughout. It mirrors the protagonist's internal state beat for beat.",
        "Observe the editing rhythm. It speeds up when tension rises and breathes when relief comes.",
    ],
    "Read": [
        "Stop after finishing. Write down one thing that surprised you and one thing you found confusing.",
        "Notice the rhythm of the sentences. They shift from short and punchy to long and flowing when the mood changes.",
        "Pay attention to how the narrator describes physical spaces. The rooms mirror the characters' minds.",
        "Track the recurring motifs. Each appearance means something slightly different than the last.",
        "Read the opening paragraph aloud. The author chose every word for sound as much as meaning.",
    ],
    "Look at": [
        "Trace the visual structure. What draws your eye first? The composition is guiding you intentionally.",
        "Notice the use of light and shadow. Where the light falls tells you what the artist wants you to feel.",
        "Step back and squint. The overall balance reveals itself only when you stop examining individual details.",
        "Compare the foreground with the background. The contrast between them is the whole point.",
        "Notice what is absent. The artist deliberately excluded something — that empty space carries meaning.",
    ],
    "Explore": [
        "Ask why this took so long to figure out. The answer reveals how discovery actually works in practice.",
        "Look at the original source material. You will notice something the popular version usually omits.",
        "Trace the connections to other ideas. This didn't happen in isolation — it was part of a larger web.",
        "Consider the timing. Why that moment in history? What else was happening that made this possible?",
        "Think about what people believed before. The resistance to this idea tells you something important about human nature.",
    ],
}

def generate_teaser(name, slug):
    """Generate a natural-sounding one-line teaser."""
    teasers = [
        f"A fascinating figure whose work rewards close attention. There's more here than the surface suggests.",
        f"Widely discussed yet still full of surprises for those willing to look closer.",
        f"Often cited but rarely fully understood. The deeper you go, the more there is to find.",
        f"The kind of work that rewards patience. Give it time and it will give back more than expected.",
        f"Hiding in plain sight. What makes this special is easy to miss on a casual glance.",
    ]
    t = random.choice(teasers)
    return t[:280]

def generate_instruction(verb):
    """Generate a specific, curiously-framed instruction."""
    pool = INSTRUCTION_POOLS.get(verb, INSTRUCTION_POOLS["Explore"])
    return random.choice(pool)

def generate_id(slug, name, counter):
    """Generate unique kebab-case ID from category and name."""
    clean = name.lower().replace("'", "").replace('"', "").replace("(", "").replace(")", "")
    clean = "-".join(clean.split()[:4])
    clean = "".join(c for c in clean if c.isalnum() or c == "-")
    clean = clean.strip("-")[:50]
    return f"{slug[:4]}-{clean}-{counter}"

def load_existing():
    """Load all existing topics."""
    existing = {}
    for jf in sorted(TOPICS_DIR.glob("*.json")):
        slug = jf.stem
        with open(jf) as f:
            existing[slug] = json.load(f)
        print(f"  {slug}: {len(existing[slug])} existing")
    return existing

# Category-appropriate synthetic name prefixes when pools are exhausted
SYNTHETIC_ADJECTIVES = [
    "Hidden", "Forgotten", "Unsung", "Radiant", "Quiet", "Luminous", "Distant",
    "Unexpected", "Eternal", "Restless", "Brilliant", "Enigmatic", "Curious",
    "Subtle", "Vivid", "Ancient", "Modern", "Lost", "Wandering", "Timeless",
]
SYNTHETIC_NOUNS = {
    "artists": ["Voice", "Sound", "Pioneer", "Composer", "Virtuoso", "Performer", "Visionary"],
    "albums": ["Recording", "Session", "Suite", "Collection", "Journey", "Echo", "Tapestry"],
    "directors": ["Auteur", "Storyteller", "Visionary", "Cinematographer", "Narrator", "Observer"],
    "films": ["Frame", "Scene", "Picture", "Chronicle", "Odyssey", "Nocturne", "Passage"],
    "authors": ["Scribe", "Tale", "Voice", "Narrator", "Chronicler", "Correspondent", "Observer"],
    "books": ["Volume", "Tome", "Manuscript", "Chronicle", "Testament", "Memoir", "Codex"],
    "painters": ["Brush", "Palette", "Portraitist", "Colourist", "Visionary", "Artisan"],
    "artworks": ["Canvas", "Study", "Panel", "Sketch", "Composition", "Vision", "Fragment"],
    "scientists": ["Mind", "Inquiry", "Theorist", "Observer", "Experimenter", "Pioneer"],
    "discoveries": ["Finding", "Breakthrough", "Revelation", "Insight", "Principle", "Phenomenon"],
    "wildcard": ["Wonder", "Mystery", "Anomaly", "Enigma", "Oddity", "Relic", "Artifact"],
}

def synthetic_name(slug, idx):
    """Generate a plausible category-appropriate synthetic name when pools run dry."""
    adj = SYNTHETIC_ADJECTIVES[idx % len(SYNTHETIC_ADJECTIVES)]
    nouns = SYNTHETIC_NOUNS.get(slug, ["Item"])
    noun = nouns[(idx // len(SYNTHETIC_ADJECTIVES)) % len(nouns)]
    return f"{adj} {noun} #{idx + 1}"

def repair_bad_names(existing):
    """Replace 'Topic #N' and mangled names in existing topics with real names from pools, then synthetic fallbacks."""
    repaired = 0
    for slug, topics in existing.items():
        name_pool = NAME_POOLS.get(slug, [])
        local_names = {t["name"].lower().strip() for t in topics}
        pool_idx = 0
        synthetic_idx = 0
        for t in topics:
            name = t.get("name", "")
            # Detect bad names: "Topic #N", mangled slugs like "Artit Topic #N", "Cientit Topic #N"
            if "Topic #" not in name and "_" not in name:
                continue
            # Try pool first, then synthetic fallback
            new_name = None
            if name_pool:
                for _ in range(len(name_pool)):
                    candidate = name_pool[pool_idx % len(name_pool)]
                    pool_idx += 1
                    if candidate.lower().strip() not in local_names:
                        new_name = candidate
                        break
            # Synthetic fallback — always works, just keep incrementing
            if new_name is None:
                while True:
                    new_name = synthetic_name(slug, synthetic_idx)
                    synthetic_idx += 1
                    if new_name.lower().strip() not in local_names:
                        break
            # Update the topic
            t["name"] = new_name
            t["exploreAction"]["targetName"] = new_name
            # Regenerate ID
            slug_prefix = slug[:4]
            clean = new_name.lower().replace("'", "").replace('"', "").replace("(", "").replace(")", "")
            clean = "-".join(clean.split()[:4])
            clean = "".join(c for c in clean if c.isalnum() or c == "-")
            clean = clean.strip("-")[:50]
            t["id"] = f"{slug_prefix}-{clean}-{repaired}"
            local_names.add(new_name.lower().strip())
            repaired += 1
    print(f"  Repaired {repaired} bad names across all categories")
    return existing

def build_expanded(existing):
    """Build expanded topic lists."""
    result = {}
    used_ids = set()
    used_names = {}

    # Collect existing IDs and names
    for slug, topics in existing.items():
        for t in topics:
            used_ids.add(t["id"])
            used_names.setdefault(slug, set()).add(t["name"].lower().strip())

    for slug, config in CATEGORIES.items():
        existing_topics = existing.get(slug, [])
        expanded = list(existing_topics)
        cat_id = config["categoryId"]
        current = len(expanded)
        target = max(current + 50, min(250, current * 2 + 50))
        needed = target - current
        name_pool = NAME_POOLS.get(slug, [f"Item #{i}" for i in range(300)])
        local_names = used_names.get(slug, set())

        generated = 0
        counter = current + 1
        attempts = 0
        max_attempts = len(name_pool) * 3

        while generated < needed and attempts < max_attempts:
            attempts += 1
            if attempts >= len(name_pool):
                break

            raw_name = name_pool[(counter + attempts) % len(name_pool)]
            if raw_name.lower().strip() in local_names:
                continue

            tid = generate_id(slug, raw_name, counter)
            while tid in used_ids:
                counter += 1
                tid = generate_id(slug, raw_name, counter)

            used_ids.add(tid)
            local_names.add(raw_name.lower().strip())
            verb = config["verb"]
            dmin, dmax = config["durRange"]

            topic = {
                "id": tid,
                "categoryId": cat_id,
                "subtype": config["subtype"],
                "name": raw_name,
                "teaser": generate_teaser(raw_name, slug),
                "imageUrl": "",
                "exploreAction": {
                    "verb": verb,
                    "targetName": raw_name,
                    "durationMinutes": random.randint(dmin, dmax),
                    "instruction": generate_instruction(verb),
                },
                "tags": random.choice(TAG_POOLS.get(slug, [["Misc", "Modern"]])),
                "tier": 2,
            }
            expanded.append(topic)
            generated += 1
            counter += 1

        result[slug] = expanded
        print(f"  {slug}: {current} + {generated} = {len(expanded)}")
    return result

def validate(topics_by_slug):
    """Validate all topics."""
    all_ids = {}
    errors = []
    for slug, topics in topics_by_slug.items():
        expected_cat = CATEGORIES[slug]["categoryId"]
        for i, t in enumerate(topics):
            for field in ["id", "categoryId", "subtype", "name", "teaser", "imageUrl", "exploreAction"]:
                if field not in t:
                    errors.append(f"{slug}[{i}]: missing {field}")
            if "exploreAction" in t:
                for field in ["verb", "targetName", "durationMinutes", "instruction"]:
                    if field not in t["exploreAction"]:
                        errors.append(f"{slug}[{i}].exploreAction: missing {field}")
                if "instruction" in t["exploreAction"] and len(t["exploreAction"]["instruction"]) > 280:
                    errors.append(f"{slug}[{i}]: instruction too long ({len(t['exploreAction']['instruction'])} chars)")
            if "teaser" in t and len(t["teaser"]) > 280:
                errors.append(f"{slug}[{i}]: teaser too long ({len(t['teaser'])} chars)")
            if t.get("categoryId") != expected_cat:
                errors.append(f"{slug}[{i}]: wrong cat ({t.get('categoryId')} != {expected_cat})")
            if not t.get("id"):
                errors.append(f"{slug}[{i}]: blank id")
            tid = t.get("id", "")
            if tid in all_ids:
                errors.append(f"{slug}[{i}]: duplicate id '{tid}' (also in {all_ids[tid]})")
            else:
                all_ids[tid] = slug
    if errors:
        print(f"\n❌ {len(errors)} errors:")
        for e in errors[:15]:
            print(f"  - {e}")
        return False
    total = sum(len(t) for t in topics_by_slug.values())
    print(f"\n✅ {total} topics validated")
    return True

def write_files(topics_by_slug):
    """Write expanded topics to JSON."""
    for slug, topics in topics_by_slug.items():
        fp = TOPICS_DIR / f"{slug}.json"
        with open(fp, "w") as f:
            json.dump(topics, f, indent=2, ensure_ascii=False)
            f.write("\n")
        print(f"  {fp}: {len(topics)} topics")

def main():
    print("=" * 60)
    print("Curio Topic Expansion v2 — Real Names Edition")
    print("=" * 60)
    existing = load_existing()
    total_existing = sum(len(v) for v in existing.values())
    print(f"  Total existing: {total_existing}\n")
    existing = repair_bad_names(existing)
    expanded = build_expanded(existing)
    total = sum(len(v) for v in expanded.values())
    print(f"\n  Total: {total} topics\n")
    if not validate(expanded):
        sys.exit(1)
    print()
    write_files(expanded)
    print(f"\n✅ Done. {total} topics across {len(expanded)} files.")

if __name__ == "__main__":
    main()
