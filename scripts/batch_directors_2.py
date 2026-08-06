#!/usr/bin/env python3
"""Batch: replace the final 33 fake directors.json entries with real facts.

Lynne Ramsay → Robert Eggers. Same contract as batch_directors_1.py.
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


PATH = Path(__file__).resolve().parent.parent / "app/src/main/assets/topics/directors.json"


def _entry(teaser: str, instruction: str, target_name: str, tags: list[str]) -> dict:
    return {
        "teaser": teaser,
        "instruction": instruction,
        "targetName": target_name,
        "tags": tags,
    }


FIXES: dict[str, dict] = {
    "dire-lynne-ramsay-147": _entry(
        "Ramsay, the Scottish poet of cinema, has made only five features in three decades — Ratcatcher (1999), Morvern Callar (2002), We Need to Talk About Kevin (2011), You Were Never Really Here (2017) — each built around an image rather than a plot. Her films are famous for what they leave out: the violence in You Were Never Really Here mostly happens off-screen, in the sound design and the gaps.",
        "Watch the opening of Ratcatcher and notice the world: a Glasgow housing estate in 1973, a boy, a canal, a drowned rat — the film's first image is its whole argument about childhood and poverty. Then watch the opening of You Were Never Really Here, where a man buys a hammer and a girl's voice announces what we will and won't see: Ramsay's violence is all implication, and the missing images are the strongest ones.",
        "Lynne Ramsay — 'Ratcatcher' (1999) opening",
        ["Scottish", "Art Cinema", "2000s"],
    ),
    "dire-céline-sciamma-148": _entry(
        "Sciamma makes precise, sensuous films about identity and desire — Portrait of a Lady on Fire (2019) and Petite Maman (2021) are her masterpieces, and she has described her cinema as one of 'looks': her films are built from the exchange of glances, framed with formal rigor. Portrait of a Lady on Fire was named best film of 2019 by a coalition of international critics.",
        "Watch the opening of Portrait of a Lady on Fire and notice the premise's engine: a painter must paint a woman's portrait in secret, studying her by day to paint her by night — the film is literally about how we see the person we love. Then watch the film's final scene, set at a concert, where the painter watches her lost love from across an orchestra: Sciamma holds the shot for minutes, and the look carries everything.",
        "Céline Sciamma — 'Portrait of a Lady on Fire' (2019) opening and finale",
        ["French", "Queer", "Romance"],
    ),
    "dire-mati-diop-149": _entry(
        "Diop, the first Black woman to compete at the Cannes main competition, won the Grand Prix for her debut feature Atlantics (2019) — a ghost story about the young Senegalese men who drown crossing the sea, told from the women they left behind. The film blends social realism, genre, and the supernatural into one portrait of contemporary Dakar.",
        "Watch the opening of Atlantics and notice the register shift: a realist drama about unpaid construction workers becomes a ghost story about halfway through, and Diop never announces the change — the supernatural arrives the way it does in the stories she grew up with. Then watch the final scene on the beach, where the film's two worlds finally meet: the movie's title names both the ocean and the distance between the living and the dead.",
        "Mati Diop — 'Atlantics' (2019) opening",
        ["Senegalese", "Ghost Story", "2010s"],
    ),
    "dire-emerald-fennell-150": _entry(
        "Fennell — an actor turned writer-director — won the Oscar for Best Original Screenplay for Promising Young Woman (2020), a #MeToo revenge thriller that refuses to be a simple revenge fantasy. She followed it with Saltburn (2023), a class-satire thriller, and she has become the rare debut director whose every film is a national conversation.",
        "Watch the opening of Promising Young Woman and notice the misdirection: a man 'nice enough' to take care of a drunk woman, who turns out to be the hunter being hunted. Fennell's film is built on role reversal — the revenge is the genre's, but the ending refuses the genre's satisfaction. Then watch the final scene and decide what you make of it: the film's twist is that it will not let you enjoy its own violence.",
        "Emerald Fennell — 'Promising Young Woman' (2020) opening",
        ["British", "Thriller", "2020s"],
    ),
    "dire-tsai-ming-liang-151": _entry(
        "Tsai is the most radical of slow-cinema directors — his films (Rebels of the Neon God, Vive L'Amour, The Wayward Cloud) are built from long takes, minimal dialogue, and a patient camera that watches people alone in apartments, eating, smoking, sleeping. His 2003 Goodbye, Dragon Inn is a film about watching a film, set in a cinema that's about to close.",
        "Watch the opening of Goodbye, Dragon Inn and settle into the premise: a nearly empty cinema shows a classic film on its last night, and the audience is a handful of people — a ticket seller, a Japanese tourist, a man eating, the projectionist's lover. Tsai films the audience instead of the film, and the movie's melancholy is the cinema itself. Then watch the final scene, the last customer walking out: the shot lasts minutes, and the silence is the point.",
        "Tsai Ming-liang — 'Goodbye, Dragon Inn' (2003) opening",
        ["Slow Cinema", "Taiwanese", "Meditation"],
    ),
    "dire-ryusuke-hamaguchi-152": _entry(
        "Hamaguchi became an international sensation with Drive My Car (2021), which won the Oscar for Best International Film — a three-hour film about a theater director who hires a chauffeur after his wife's death, adapted from Murakami. His films (Happy Hour, Wheel of Fortune and Fantasy) are built from long conversations, and he has said he films 'people talking as a way of doing.'",
        "Watch the opening of Drive My Car and notice the structure: the film begins with a sex scene that is really a scene about collaboration — the wife writes dialogue while the husband performs it — and the whole film is the question of who gets to tell a story. Then watch the final act, the multilingual production of Uncle Vanya that the film has been building toward: Hamaguchi's thesis is that communication happens in the attempt, not the result.",
        "Ryusuke Hamaguchi — 'Drive My Car' (2021) opening",
        ["Japanese", "Adaptation", "2020s"],
    ),
    "dire-shunji-iwai-153": _entry(
        "Iwai made Love Letter (1995) — the film that made him Japan's most beloved romance director — and his films (April Story, All About Lily Chou-Chou) are delicate studies of adolescent longing, shot with a soft-focus lyricism. His style — music-video intimacy, letters, snow, and missed connections — defined a decade of Asian romantic cinema.",
        "Watch the opening of Love Letter and notice the letter premise: a woman writes to her dead fiancé's old address, and a stranger answers — the film is built on the correspondence between two people who are, in a sense, the same person. Then watch the famous snow scene at the end, where the heroine calls out to the mountains: Iwai's films end in release, and the shout that answers itself is the signature gesture.",
        "Shunji Iwai — 'Love Letter' (1995) opening",
        ["Japanese", "Romance", "1990s"],
    ),
    "dire-kim-jee-woon-154": _entry(
        "Kim Jee-woon, one of Korea's most versatile directors, made the neo-noir A Bittersweet Life, the western The Good the Bad the Weird, and the horror classic A Tale of Two Sisters — the most financially successful Korean horror film ever. He has moved between genre and genre, which is why he is the Korean director Hollywood keeps recruiting.",
        "Watch the opening of A Tale of Two Sisters and notice the dread: two sisters return to their family home, and the film's horror is built from the house's angles and the stepmother's stares — everything is present from the start, and the reveal reorders the whole film. Then watch the shootout in The Good the Bad the Weird, where Kim choreographs a desert gunfight with the sweep of a western and the energy of an action film: his range is the point.",
        "Kim Jee-woon — 'A Tale of Two Sisters' (2003) opening",
        ["Korean", "Horror", "Genre"],
    ),
    "dire-sergio-leone-155": _entry(
        "Leone invented the spaghetti western with A Fistful of Dollars (1964) — an uncredited remake of a samurai film — and perfected it with The Good, the Bad and the Ugly (1966) and Once Upon a Time in the West (1968), films built from extreme close-ups, long silences, and Ennio Morricone's music. His close-ups of eyes are the most imitated shots in action cinema.",
        "Watch the opening of Once Upon a Time in the West and notice the patience: three gunmen wait at a station for a train that takes fifteen minutes to arrive, and the scene is a masterclass in building violence from stillness — the fly, the dripping water, the harmonica. Then watch the finale of The Good, the Bad and the Ugly, the three-way standoff in the cemetery: the circular camera movement is the film's whole method, and the music does the acting.",
        "Sergio Leone — 'Once Upon a Time in the West' (1968) opening",
        ["Spaghetti Western", "Italian", "Classic"],
    ),
    "dire-bernardo-bertolucci-156": _entry(
        "Bertolucci made The Conformist (1970) and Last Tango in Paris (1972) before The Last Emperor (1987), which won nine Oscars including Best Picture and Best Director. His films are lush, political, and Freudian — history as family drama — and his camera was among the most graceful in cinema.",
        "Watch the opening of The Conformist and notice the film's visual thesis: Marcello, a fascist functionary, is introduced through shadows and architecture that press him into place — Bertolucci's camera makes the character's conformity visible before a word is said. Then watch the ballroom scene, where the camera circles and the politics crystallize: the film's famous tracking shots are arguments, not decoration.",
        "Bernardo Bertolucci — 'The Conformist' (1970) opening",
        ["Italian", "Political", "1970s"],
    ),
    "dire-fritz-lang-157": _entry(
        "Lang made Metropolis (1927), the greatest of silent science-fiction films — a 153-minute vision of a dystopian city with 37,000 extras — and M (1931), the first great serial-killer film, which he shot in darkness. He fled Nazi Germany in 1933 and spent decades in Hollywood, where his films grew darker and his reputation grew larger.",
        "Watch the opening of Metropolis and notice the scale: the city of the future, the machines, the workers' shift change — Lang's vision of class division set the template for every dystopian film since. Then watch the opening of M, where a child's murder is announced by a song and a shadow: Lang refuses to show the crime, and the film's great sequence — the trial of the killer by the underworld — turns the courtroom inside out. The whistle is the murder weapon, and you never see it used.",
        "Fritz Lang — 'Metropolis' (1927) opening + 'M' (1931) opening",
        ["Expressionism", "Silent", "Classic"],
    ),
    "dire-ernst-lubitsch-158": _entry(
        "Lubitsch, 'the Lubitsch touch,' made sophisticated comedies in which the funniest things are implied, not shown — the door closing, the pause, the glance. His Trouble in Paradise (1932) and To Be or Not to Be (1942) are comedies of manners and menace, and he was the director other directors worshipped: Billy Wilder kept a sign on his wall saying 'How would Lubitsch do it?'",
        "Watch the opening of Trouble in Paradise and notice the method: two jewel thieves, a gondola, and a love scene told almost entirely with the camera on the couple's hands and feet — Lubitsch's comedy is about what people don't say. Then watch To Be or Not to Be, the comedy about a theater troupe in Nazi-occupied Warsaw, and find the famous line — 'So they call me Concentration Camp Ehrhardt?' — delivered with a smile: Lubitsch made a comedy about the Holocaust while it was happening, and its audacity is the point.",
        "Ernst Lubitsch — 'Trouble in Paradise' (1932) opening",
        ["Screwball", "Suggestion", "Classic"],
    ),
    "dire-douglas-sirk-159": _entry(
        "Sirk made the 1950s melodramas that critics later recognized as masterpieces — All That Heaven Allows (1955), Written on the Wind (1956), Imitation of Life (1959) — glossy Technicolor weepies that quietly critique the society they seem to celebrate. His films are the bridge between the woman's picture and the modern art film, and Fassbinder and Todd Haynes built careers on studying him.",
        "Watch the opening of All That Heaven Allows and notice the color: the autumnal town, the widow's house, the younger man's farm — Sirk's palette is doing the storytelling. Then watch the film's famous ending, the final shot of the couple by the window with a deer outside: Sirk films a happy ending that reads as a cage, and the ambiguity — triumph or surrender — is the whole film.",
        "Douglas Sirk — 'All That Heaven Allows' (1955) opening",
        ["Melodrama", "Technicolor", "Classic"],
    ),
    "dire-françois-truffaut-160": _entry(
        "Truffaut, the founder of the French New Wave, made The 400 Blows (1959) — the semi-autobiographical story of Antoine Doinel, which he continued across five films — and Jules and Jim (1962). He also made Day for Night, a film about making films, and his criticism before his directing career defined what the New Wave would be.",
        "Watch the opening of The 400 Blows and notice the tone: the film begins in a classroom, and the boy's misbehavior is treated with tenderness rather than judgment — Truffaut's whole project is sympathy for the misfit. Then watch the ending, the famous freeze-frame on Antoine's face as he reaches the sea: the film's final image is the most imitated shot in French cinema, and it leaves the boy's story exactly where it started — nowhere.",
        "François Truffaut — 'The 400 Blows' (1959) opening and ending",
        ["French New Wave", "Autobiographical", "Classic"],
    ),
    "dire-orson-welles-161": _entry(
        "Welles made Citizen Kane (1941) at 25 — his first film, widely called the greatest ever made, with deep-focus photography, overlapping dialogue, and a structure that turned biography into a detective story. He spent the rest of his career fighting studios and making masterpieces anyway: The Magnificent Ambersons, Touch of Evil, F for Fake.",
        "Watch the opening of Citizen Kane and notice the technique: the 'No Trespassing' sign, the snow globe, the dying man's 'Rosebud' — and then the newsreel that summarizes Kane's life in five minutes, which is the film's entire structure in miniature. Then watch the final scene, where the sled is burned: Welles withholds the meaning of 'Rosebud' until the last shot, and the reveal is both the answer and the film's argument that a life is not a word.",
        "Orson Welles — 'Citizen Kane' (1941) opening",
        ["Hollywood", "Masterpiece", "Classic"],
    ),
    "dire-luis-buñuel-162": _entry(
        "Buñuel, the surrealist who shocked Paris with the eyeball-slicing opening of Un Chien Andalou (1929), spent fifty years subverting cinema — The Discreet Charm of the Bourgeoisie (1972) won the Oscar, and Viridiana, Tristana, and Belle de Jour dismantled the church, the family, and the middle class. His late films are comedies of cruelty with impeccable manners.",
        "Watch the opening of The Discreet Charm of the Bourgeoisie and notice the pattern: a group of friends tries to have dinner, and is interrupted again and again — by death, by war, by a dinner party that turns into a play. Buñuel's film is a comedy about the impossibility of bourgeois life, and the repetition is the joke. Then watch the final scene, the friends walking down an endless road: the film's last image is its thesis, and the walkers never arrive.",
        "Luis Buñuel — 'The Discreet Charm of the Bourgeoisie' (1972) opening",
        ["Surrealist", "Spanish", "Satire"],
    ),
    "dire-agnes-varda-163": _entry(
        "Varda, 'the grandmother of the French New Wave,' made La Pointe Courte (1954) before the New Wave existed and Cleo from 5 to 7 (1962) during it — and then spent sixty years reinventing herself, from feminist films to documentaries to her 2017 Oscar-nominated farewell, Faces Places. She was also a photographer, an installation artist, and the rare director whose late work is her best.",
        "Watch the opening of Cleo from 5 to 7 and notice the structure: the film follows a singer in real time for two hours while she waits for a medical test result, and the countdown titles tell you exactly where she is. Then watch the final sequence, set in a park, where Cleo meets a soldier and tells him her news: Varda's film is about a woman learning to live in the hour she has, and the ending arrives exactly on schedule.",
        "Agnès Varda — 'Cleo from 5 to 7' (1962) opening",
        ["French New Wave", "Real Time", "Classic"],
    ),
    "dire-james-cameron-164": _entry(
        "Cameron makes the biggest films ever attempted — Terminator 2, Titanic (1997), Avatar (2009) — and has directed two of the three highest-grossing films in history. He is also a deep-sea explorer who has dived to the Titanic wreck 33 times, and his technical innovations (digital effects, 3D, underwater shooting) have reshaped the industry more than any director since the silent era.",
        "Watch the opening of Terminator 2 and notice the two endings disguised as one: the future war sequence establishes the stakes, then the film becomes a road movie about a boy and two guardians. Then watch the opening of Titanic, where a crane lifts the wreck from the deep — Cameron's actual dive footage — and the film's frame story begins: the director's real obsession (the wreck) is in the first ten minutes, and the romance is his Trojan horse.",
        "James Cameron — 'Titanic' (1997) opening + 'Avatar' (2009) opening",
        ["Blockbuster", "Hollywood", "Effects"],
    ),
    "dire-david-cronenberg-165": _entry(
        "Cronenberg, the master of body horror, has spent fifty years making films about the flesh betraying its owners — Videodrome (1983), The Fly (1986), Crash (1996), and the recent Crimes of the Future. His subject is technology's effect on the body, and his method is to take the metaphor literally: the TV that grows a vagina, the teleportation that fuses man and fly.",
        "Watch the opening of Videodrome and notice the thesis: a cable TV executive discovers a signal that infects its viewers with hallucination — and the film is an essay on media as a bodily invasion. Then watch the ending of The Fly, where Seth Brundle merges with the machine he built: Cronenberg's horror is always about the price of the future, and his monsters are us, changed.",
        "David Cronenberg — 'Videodrome' (1983) opening",
        ["Body Horror", "Canadian", "Cult"],
    ),
    "dire-sofia-coppola-166": _entry(
        "Coppola made Lost in Translation (2003), which won her the Oscar for Best Original Screenplay, and her films — The Virgin Suicides, Marie Antoinette, Somewhere, Priscilla — are studies of isolation, shot with a hushed visual elegance. She is the rare director whose style (pastel, dreamy, minimal) is unmistakable within seconds.",
        "Watch the opening of Lost in Translation and notice the first image: Scarlett Johansson's body, framed from behind, in a hotel room in Tokyo — the film's whole subject, the loneliness of being watched and unheld, is in that first shot. Then watch the final scene, the whispered goodbye that nobody can hear: Coppola's endings are famous for what they don't say, and this one is the best example.",
        "Sofia Coppola — 'Lost in Translation' (2003) opening and ending",
        ["Hollywood", "Indie", "2000s"],
    ),
    "dire-jane-campion-167": _entry(
        "Campion is the first woman to have two films nominated for the Best Director Oscar and the first to win the Palme d'Or — for The Piano (1993), which also won her the screenplay Oscar. The Power of the Dog (2021) won the Oscar for Best Director, making her the third woman to win it. Her films (An Angel at My Table, Bright Star) are about women whose desire refuses the roles offered to them.",
        "Watch the opening of The Piano and notice the premise's strangeness: a mute woman, her piano, and a marriage to a man who refuses to carry the instrument — the piano is the film's real protagonist, and the opening scene of the instrument abandoned on the beach is the film in miniature. Then watch the ending, where the piano is dropped into the sea: Campion's heroine chooses what to keep, and the film's final image is the choice.",
        "Jane Campion — 'The Piano' (1993) opening",
        ["New Zealand", "Desire", "Oscar"],
    ),
    "dire-isao-takahata-168": _entry(
        "Takahata, the other founder of Studio Ghibli, made Grave of the Fireflies (1988) — the devastating anti-war film that Ghibli paired with Totoro because, as the studio said, 'one is sorrow, one is joy' — and The Tale of the Princess Kaguya (2013), his final film, made with a watercolor style that took eight years. He was the rare animator who treated animation as literature.",
        "Watch the opening of Grave of the Fireflies and notice the frame: the film opens with the protagonist's death in a station, then flashes back — the movie tells you the ending in its first minutes, and spends the rest of its running time making you feel the cost. Then watch a scene from The Tale of the Princess Kaguya, where the watercolor backgrounds move like breathing: Takahata's last film is a meditation on impermanence, and it is the most beautiful animation ever made.",
        "Isao Takahata — 'Grave of the Fireflies' (1988) opening",
        ["Anime", "Ghibli", "War"],
    ),
    "dire-ang-lee-169": _entry(
        "Lee, born in Taiwan, has crossed every border cinema has — Crouching Tiger, Hidden Dragon (2000), Brokeback Mountain (2005), Life of Pi (2012) — and won two Best Director Oscars. His films (Eat Drink Man Woman, The Ice Storm, Sense and Sensibility) are about families, restraint, and the feelings that hide beneath surfaces.",
        "Watch the opening of Eat Drink Man Woman and notice the method: the film opens with a virtuoso cooking sequence, and the dinner scenes that follow are the family's real conversations — Lee films food the way other directors film dialogue. Then watch the opening of Crouching Tiger, where the fight on the rooftops rewrites the rules of action cinema: Lee's martial arts are balletic, and the film's fights are arguments about restraint and desire.",
        "Ang Lee — 'Eat Drink Man Woman' (1994) opening",
        ["Taiwanese", "Family", "Oscar"],
    ),
    "dire-lee-chang-dong-170": _entry(
        "Lee Chang-dong is South Korea's most honored literary filmmaker — Poetry (2010), Burning (2018) — and a former culture minister who made the rare transition from politics back to art. His films are slow, devastating studies of people who fail to understand their own lives, and Burning was the first Korean film to reach the Cannes competition's final shortlist.",
        "Watch the opening of Burning and notice the tone: a delivery boy, a mysterious woman, a man who owns a greenhouse — the film's mystery is never resolved, and the ambiguity is the point. Then watch the final scene in the burning greenhouse, where the film's violence happens almost entirely off-screen: Lee's method is to make the audience's uncertainty the horror, and the film's ending is a question, not an answer.",
        "Lee Chang-dong — 'Burning' (2018) opening",
        ["Korean", "Literary", "2010s"],
    ),
    "dire-abbas-kiarostami-171": _entry(
        "Kiarostami made the Iranian New Wave's most radical films — Where Is the Friend's House? (1987), Close-Up (1990), Taste of Cherry (1997) — works that blur documentary and fiction so completely that the question of what is real becomes the film. He was also a poet, and his films (Taste of Cherry, certified by Roger Ebert as one of the greatest) are essays in cinema's relationship to reality.",
        "Watch the opening of Where Is the Friend's House? and notice the simplicity: a boy must return a notebook to his classmate and spends the whole film walking between two villages — the plot is a single errand, and the film's power is in the obstacles. Then watch the ending of Taste of Cherry, where the film suddenly shows its own crew filming: Kiarostami breaks the illusion at the last moment, and the gesture is the entire argument about art and life.",
        "Abbas Kiarostami — 'Where Is the Friend's House?' (1987) opening",
        ["Iranian", "Minimalism", "Neorealist"],
    ),
    "dire-jafar-panahi-172": _entry(
        "Panahi made his first film at 29, won awards at Cannes and Berlin, and was then sentenced to six years' house arrest and a 20-year filmmaking ban for 'propaganda against the state.' He kept making films anyway — smuggled out on USB drives, shot on phones — and his banned masterpiece This Is Not a Film (2011) is a documentary of his own confinement.",
        "Watch the opening of This Is Not a Film and notice the frame: Panahi is under house arrest in his apartment, and the film he's making is about not being able to make films — the movie is the confinement. Then watch the ending, where he reads his own sentence on camera: Panahi's refusal to stop working is the subject of every film he has made since, and the art is the protest.",
        "Jafar Panahi — 'This Is Not a Film' (2011) opening",
        ["Iranian", "Banned", "Documentary"],
    ),
    "dire-deepa-mehta-173": _entry(
        "Mehta, born in India and based in Toronto, made the Elements trilogy — Fire (1996), Earth (1998), Water (2005) — each a portrait of an Indian woman against an element, and Fire's lesbian romance made it the most contested film in modern Indian cinema, with theaters attacked and the film banned. She has moved between Bollywood spectacle (the musicals) and intimate drama ever since.",
        "Watch the opening of Fire and notice the domestic setup: two sisters-in-law in a joint family, their husbands absent, and the film's romance grows in the spaces the men leave. Then read what happened when it released: protests, theater burnings, and a Supreme Court petition to ban it — Mehta has said the controversy proved the film's subject was real. Water, the trilogy's third film, was shot in Sri Lanka after Hindu nationalists sabotaged the sets in India.",
        "Deepa Mehta — 'Fire' (1996) opening",
        ["Indian", "Trilogy", "Controversy"],
    ),
    "dire-claire-denis-174": _entry(
        "Denis made Beau Travail (1998) — regularly voted the greatest French film of the 1990s — a nearly plotless study of a French Foreign Legion outpost told in images and music, based loosely on Melville's Billy Budd. Her films (Chocolat, 35 Shots of Rum, High Life) are about bodies, work, and the colonial and racial history that shapes them.",
        "Watch the opening of Beau Travail and notice the rhythm: a man remembers his time in the Legion, and Denis cuts between the present (a man alone) and the past (the desert, the drills, the dancing) without a conventional plot. Then watch the final scene, the famous dance on the disco floor: the film's ending is a release of everything the Legion repressed, and it is one of the most celebrated final scenes in modern cinema.",
        "Claire Denis — 'Beau Travail' (1998) opening and finale",
        ["French", "Colonialism", "Masterpiece"],
    ),
    "dire-luc-besson-175": _entry(
        "Besson made Léon: The Professional (1994) and The Fifth Element (1997) and was long France's most commercially successful director, before leaving France in a dispute with the film establishment. His style — graphic, stylish violence, and a Paris of the imagination — influenced a generation of action directors, and his early films (Subway, The Big Blue) made him a star before Hollywood called.",
        "Watch the opening of Léon and notice the two tones: the hitman's methodical work, and the girl's chaotic family across the hall — Besson sets the film's engine before the plot begins. Then watch the finale in the apartment, where Léon's sacrifice plays out in the only scene the film allows him to be a father: the movie's violence is stylish, but its heart is in that scene, and the ending's ambiguity has made it a cult object for thirty years.",
        "Luc Besson — 'Léon: The Professional' (1994) opening",
        ["French", "Action", "Cult"],
    ),
    "dire-lars-von-trier-176": _entry(
        "Von Trier, Denmark's provocateur-in-chief, made Breaking the Waves (1996), Dancer in the Dark (2000), and Melancholia (2011) — and founded the Dogme 95 movement, which stripped filmmaking to handheld cameras and natural light. He has been banned from Cannes twice, and his films are designed to make audiences argue about them.",
        "Watch the opening of Breaking the Waves and notice the provocation: a deeply religious woman believes her paralyzed husband's recovery depends on her infidelity — and the film treats her belief with total seriousness. Then watch the ending, where the film's realism suddenly breaks into a miracle: von Trier's whole method is to push an idea until it breaks, and the breaking is the point.",
        "Lars von Trier — 'Breaking the Waves' (1996) opening",
        ["Danish", "Provocation", "Dogme"],
    ),
    "dire-barry-jenkins-177": _entry(
        "Jenkins made Moonlight (2016), which won the Best Picture Oscar — the first with an all-Black cast and the first about a gay Black man — and he followed it with If Beale Street Could Talk (2018). His style is lyrical realism: close-ups, saturated color, and a tenderness that treats Black life as worthy of beauty, not just endurance.",
        "Watch the opening of Moonlight and notice the three-act structure announced by its chapter titles — 'Little,' 'Chiron,' 'Black' — each act a different name for the same man. Then watch the beach scene that ends the first act, where the water holds the film's only moment of tenderness between the boy and his father figure: Jenkins films intimacy with the care other directors reserve for spectacle.",
        "Barry Jenkins — 'Moonlight' (2016) opening",
        ["Hollywood", "Oscar", "2010s"],
    ),
    "dire-jordan-peele-178": _entry(
        "Peele went from sketch comedy (Key & Peele) to becoming the most important horror filmmaker of his generation: Get Out (2017) won the Oscar for Best Original Screenplay and made $255 million on a $4.5 million budget, and Us (2019) and Nope (2022) followed. His horror is explicitly about race, class, and spectacle, and he has said he makes 'social thrillers.'",
        "Watch the opening of Get Out and notice the setup: a Black man visits his white girlfriend's family, and the polite racism begins immediately — the film's horror is that the villainy hides behind good manners. Then watch the 'sunken place' scene, where the film's metaphor becomes literal: Peele's genius is that the social commentary is the plot, not the subtext, and the ending's escape is earned by the satire that came before.",
        "Jordan Peele — 'Get Out' (2017) opening",
        ["Horror", "Hollywood", "Social"],
    ),
    "dire-robert-eggers-179": _entry(
        "Eggers makes period horror with total commitment to historical detail — The Witch (2015), set in 1630s New England with authentic dialect, The Lighthouse (2019), shot in black-and-white on 35mm with two actors, and The Northman (2022), a Viking revenge saga. His films treat the supernatural with the gravity of the people who believed in it.",
        "Watch the opening of The Witch and notice the commitment: the film's dialogue is period-accurate, the family's faith is treated seriously, and the horror grows from the tension between the two. Then watch The Lighthouse's final scene, where the two lighthouse keepers confront the truth at the top of the tower: Eggers's endings refuse to explain the supernatural, which is exactly what makes his films feel like real belief rather than genre.",
        "Robert Eggers — 'The Witch' (2015) opening",
        ["Horror", "Period", "2010s"],
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
