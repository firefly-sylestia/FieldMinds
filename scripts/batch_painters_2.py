#!/usr/bin/env python3
"""Batch: replace the final 37 fake painters.json entries with real facts.

Toulouse-Lautrec → Whistler (ids 166–202). Same contract as
batch_painters_1.py. Cap 450 (SCHEMA.md).
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


PATH = Path(__file__).resolve().parent.parent / "app/src/main/assets/topics/painters.json"


def _entry(teaser: str, instruction: str, target_name: str, tags: list[str]) -> dict:
    return {
        "teaser": teaser,
        "instruction": instruction,
        "targetName": target_name,
        "tags": tags,
    }


FIXES: dict[str, dict] = {
    "pain-henri-de-toulouse-lautrec-166": _entry(
        "Toulouse-Lautrec — aristocrat, dwarf, chronicler of Montmartre's nightlife — painted and drew the dancers and drinkers of the Moulin Rouge in posters that invented modern graphic design. His legs stopped growing after two childhood falls; he made his short stature the vantage point from which he saw everything.",
        "Look at 'At the Moulin Rouge' (1892) and find the figures Lautrec painted repeatedly: the dancers, the absinthe drinkers, and the painter himself at the back, small and peripheral. Then look at his poster of La Goulue (1891) — flat color, bold type, cropped figures — and read why it mattered: it turned the poster into an art form and made Montmartre's performers into icons. His world was loud, and his line is the sharpest in the century.",
        "Henri de Toulouse-Lautrec — 'At the Moulin Rouge' (1892) and the La Goulue poster",
        ["Post-Impressionism", "French", "Poster"],
    ),
    "pain-egon-schiele-167": _entry(
        "Schiele — Gustav Klimt's protégé — drew the human body with an intensity that got him arrested: in 1912 the police seized his drawings and he spent 24 days in prison on charges of distributing erotic images. He died in the 1918 flu pandemic at 28, having produced some 3,000 works in his final decade.",
        "Look at a Schiele self-portrait and notice what he does to the body: the limbs are twisted, the ribs and tendons drawn as if under pressure, the face gaunt — the body as confession rather than beauty. Then read the 1912 arrest: his drawings of adolescent nudes led to a trial, and the judge burned one drawing in the courtroom. He died three days after Klimt, both taken by the 1918 flu — the last year of his life produced some of his most assured work.",
        "Egon Schiele — the twisted self-portraits and the 1912 arrest",
        ["Expressionism", "Austrian", "Figurative"],
    ),
    "pain-kazimir-malevich-168": _entry(
        "Malevich painted the most radical painting of the 20th century — Black Square (1915), a plain black square on a white ground — and declared art's 'zero of form': the end of representation. He called the movement Suprematism, and the painting's first public display hung it high in a corner, where Russians hang icons.",
        "Look at Black Square and then read the icon theory: Malevich hung it in the corner reserved for Orthodox icons, and its meaning is inseparable from that gesture — the square as a replacement for the divine image. Then look at 'Suprematist Composition: White on White' (1918), three years later: the square has become almost invisible, and art has nearly vanished into pure feeling. Under Stalin, Malevich was forced back into figurative painting; his Black Square accompanied his funeral procession in 1935.",
        "Kazimir Malevich — 'Black Square' (1915) and 'White on White' (1918)",
        ["Suprematism", "Russian", "Avant-Garde"],
    ),
    "pain-rené-magritte-169": _entry(
        "Magritte painted pipes that are 'not a pipe,' apples that hide faces, and men in bowler hats whose heads float in clouds — the most imitated images in modern art. His 1929 painting 'The Treachery of Images,' with its famous caption 'Ceci n'est pas une pipe' ('This is not a pipe'), is the single most quoted artwork about images in existence.",
        "Look at 'The Treachery of Images' and read the paradox slowly: the painting shows a pipe and says it isn't one — and it's right, because it's a painting of a pipe. Then look at 'The Son of Man' (1964), the bowler-hatted man with an apple in front of his face, and read Magritte's explanation: the apple hides the face, but the face is also partly visible through it — 'so everything visible hides something else.' Magritte painted ideas, and his ideas are about how images lie.",
        "René Magritte — 'The Treachery of Images' (1929) and 'The Son of Man'",
        ["Surrealism", "Belgian", "Conceptual"],
    ),
    "pain-joan-miró-170": _entry(
        "Miró's paintings — stars, moons, ladders, and biomorphic shapes floating on bright fields — look childlike and are anything but: he called them 'assassinations of painting,' and they were worked and reworked over months. The ladder is his recurring symbol for escape — he used it to leave the earth and, he said, to leave the studio.",
        "Look at 'The Hunter (Catalan Landscape)' (1923–24) and find the coded elements: the hunter's eye, the heart, the ladder, the sardine — Miró said the painting is a map of a day in the countryside. Then look at the 'Constellation' series (1940–41), made during the German occupation of France: small, jewel-like canvases of stars and arcs painted while Europe was dark, which Miró called his 'sky paintings.'",
        "Joan Miró — 'The Hunter (Catalan Landscape)' (1923) and the Constellations",
        ["Surrealism", "Spanish", "Abstract"],
    ),
    "pain-tamara-de-lempicka-171": _entry(
        "Lempicka painted the Jazz Age's art deco icons — sleek, angular women with polished faces and cigarette holders, figures of cool glamour. She was a Polish aristocrat who fled the Russian Revolution, arrived in Paris, and made herself the painter of the era's wealth; her 1925 self-portrait in a green Bugatti is the image of modern femininity she invented for herself.",
        "Look at 'Self-Portrait in a Green Bugatti' (1925) — she never actually owned a Bugatti — and notice the angles: the scarf, the head tilt, the sharp chin all cut the canvas like facets. Then read the paradox of her career: she painted the rich and became one of the most successful women painters of the century, then was forgotten for decades and revived in the 1970s. Her work is often dismissed as decorative and defended as genuinely modern — the argument is the point.",
        "Tamara de Lempicka — 'Self-Portrait in a Green Bugatti' (1925)",
        ["Art Deco", "Polish-French", "Portraiture"],
    ),
    "pain-grant-wood-172": _entry(
        "Wood's American Gothic (1930) — the farmer with the pitchfork and his stern companion — is the most parodied painting in American history, and Wood intended it as a dignified portrait of rural Iowa, not a satire. He based the farmer on his dentist and the woman on his sister, who insisted the painting showed them as 'proper people.'",
        "Look at American Gothic and notice the details that are actually Gothic: the arched window, the pitchfork echoing the overalls, the strict symmetry — Wood called it a portrait of 'the kind of people I fancied should live in that house.' Then read the reception: Iowans were furious at first, seeing caricature; the painting won a prize at the Art Institute of Chicago and became a national icon within a decade. The ambiguity — tribute or mockery — is what made it immortal.",
        "Grant Wood — 'American Gothic' (1930)",
        ["Regionalism", "American", "20th Century"],
    ),
    "pain-andrew-wyeth-173": _entry(
        "Wyeth painted Christina's World (1948), the second-most-famous American painting after American Gothic: a woman in a pink dress crawling through a field toward a farmhouse. The model, Christina Olson, was paralyzed from the waist down and refused a wheelchair — she crawled everywhere, which is exactly what the painting shows, though Wyeth removed the signs of struggle.",
        "Look at Christina's World and notice what Wyeth changed: Christina was 55 when he painted her; the figure is young, and the distance to the house is compressed. The painting's pull comes from that tension — dignity against the impossibility of the task. Then read the context: Wyeth was dismissed by modernists as an illustrator, and the 2017 discovery that he made a secret series of nudes of a neighbor, Helga, reignited the debate over whether his realism hides obsession.",
        "Andrew Wyeth — 'Christina's World' (1948)",
        ["Regionalism", "American", "Realism"],
    ),
    "pain-lucian-freud-174": _entry(
        "Freud — grandson of Sigmund — painted the human body with an unblinking scrutiny that made him the most important figurative painter of his era: sagging skin, awkward poses, hours-long sittings. His 'Benefits Supervisor Sleeping' (1995) set the record in 2008 for the most expensive painting by a living artist ($33.6 million).",
        "Look at a Freud portrait and read the method behind the mercilessness: he painted from life, sometimes for 16-month sittings, and his portraits record the sitter's endurance as much as their face. Then look at 'Benefits Supervisor Sleeping' — a 275-pound woman asleep on a sofa — and read the response: critics called it grotesque, and its record price made it famous. Freud's own account of his work is simpler: 'I paint people, not because of what they are like... but because of how they happen to be.'",
        "Lucian Freud — 'Benefits Supervisor Sleeping' (1995) and the long sittings",
        ["Figurative", "British", "20th Century"],
    ),
    "pain-gerhard-richter-175": _entry(
        "Richter is the most expensive living artist in history (an abstract sold for $46.3 million in 2015) and the rare painter who works in two opposite styles at once: photo-realist paintings blurred from photographs, and huge abstract canvases scraped with a squeegee. He has said both are attempts 'to make a picture without knowing what it is.'",
        "Look at a blurred photopainting ('Betty,' 1988, is the masterpiece — his daughter seen from behind, based on a photo) and notice what the blur does: it makes a photograph into a painting, and makes memory — indistinct, emotional — the real subject. Then look at an abstract squeegee painting and read the process: Richter paints layers, then drags a long squeegee across them, letting chance destroy what he made. The two bodies of work are one project: images we can't fully see.",
        "Gerhard Richter — 'Betty' (1988) and the squeegee abstracts",
        ["Contemporary", "German", "Abstract"],
    ),
    "pain-georg-baselitz-176": _entry(
        "Baselitz turned his paintings upside down in 1969 — and has painted most of his work that way since, as a way of 'liberating the subject from content.' He is the most important German painter to grapple with the country's postwar shame, and his 1963 exhibition was closed by prosecutors for obscenity.",
        "Look at an inverted Baselitz and notice the effect: once the painting is upside down, the image (a figure, an eagle, a forest) becomes paint first and subject second — which is the point he has spent 50 years making. Then read the 1963 scandal: his painting 'The Big Night Down the Drain' was seized by German prosecutors for obscenity, making him famous at 25. His upside-down bodies are an argument about what Germany could and couldn't look at.",
        "Georg Baselitz — the inverted paintings and the 1963 seizure",
        ["Contemporary", "German", "Figurative"],
    ),
    "pain-takashi-murakami-177": _entry(
        "Murakami coined 'Superflat' — the aesthetic that flattens Japanese art history, anime, and consumer culture into one surface — and his smiling flowers, mushroom clouds, and skulls became the most recognizable contemporary Japanese art of the century. He also collaborated with Louis Vuitton, which made his art global in a way galleries never could.",
        "Look at a Murakami flower and notice what's missing: no depth, no shading, just outline and flat color — the Superflat principle, which Murakami argues is the continuation of Japanese art from ukiyo-e through anime. Then read the darker layer: the same smiling flower appears in 'The Castle of Tin Tin' and in his mushroom-cloud paintings, and he has said his work is about Japan's postwar trauma and its consumer compensation. The smile is not simple.",
        "Takashi Murakami — the Superflat flowers and the mushroom-cloud series",
        ["Superflat", "Japanese", "Contemporary"],
    ),
    "pain-zao-wou-ki-178": _entry(
        "Zao Wou-Ki fused Chinese calligraphy and landscape with Western abstraction — he was born in China in 1920, moved to Paris in 1948, and became one of the most successful Asian painters of the 20th century. His large abstract canvases — which he said were landscapes in his bones — sold for record prices in the 2010s, decades after his best period.",
        "Look at a Zao Wou-Ki abstract from the 1960s and notice the two traditions meeting: the sweeping, ink-like gestures of calligraphy and the layered oil color of European abstraction — his paintings are landscapes that don't name themselves. Then read the turning point: in 1971 he stopped painting for a year after his second wife's death, and when he returned, the work opened up into the luminous, monumental canvases that made his late reputation. He has said his painting is 'a way of living between two cultures.'",
        "Zao Wou-Ki — the calligraphic abstracts (1960s) and the late canvases",
        ["Abstract", "Chinese-French", "Lyrical"],
    ),
    "pain-helen-frankenthaler-179": _entry(
        "Frankenthaler invented the 'soak-stain' technique: pouring thinned paint onto raw canvas so it soaked in like dye, eliminating the brushstroke entirely. Her 1952 painting 'Mountains and Sea' — painted when she was 23 — is considered the bridge between abstract expressionism and color field painting, and she was the movement's most influential woman.",
        "Look at 'Mountains and Sea' (1952) and notice the consequence of the technique: there is no brushstroke, no texture, no edge — the color is inside the canvas, and the painting is as much an object as an image. Then read the history: Frankenthaler showed the soaking method to Morris Louis and Kenneth Noland, who built Color Field painting from it — she never patented the idea, and her generosity shaped a generation. She has said the technique came from looking at Pollock and 'wanting to take it further.'",
        "Helen Frankenthaler — 'Mountains and Sea' (1952) and the soak-stain technique",
        ["Abstract Expressionism", "American", "Color Field"],
    ),
    "pain-joan-mitchell-180": _entry(
        "Mitchell painted fierce, gestural abstractions — sweeping arcs of color that look like storms, flowers, and memory — and she was among the few women in the first rank of abstract expressionism. She left New York for the French countryside in 1967, where her paintings grew into huge, confident canvases built around remembered landscapes.",
        "Look at a late Mitchell ('Noon' or any of the 'Sunflowers' series) and notice the scale and confidence: the canvases are bigger than a person, and the strokes have the sweep of weather. Then read the turning point: Mitchell, like her friend Frankenthaler, was initially overlooked by the critics who canonized her male peers, and her late recognition was part of the 1970s revision of the movement's history. Her paintings of the 1980s, made as her health failed, are the most powerful of her career.",
        "Joan Mitchell — the 'Sunflowers' series and the late French canvases",
        ["Abstract Expressionism", "American", "Gesture"],
    ),
    "pain-faith-ringgold-181": _entry(
        "Ringgold made story quilts — painted narratives stitched into fabric borders — that fuse her African American family history, feminism, and political art into a form no one else was using. Her first quilt, 'Who's Afraid of Aunt Jemima?' (1983), was a direct answer to the mammy figure, and her best-known work, 'Tar Beach' (1988), was turned into a children's book.",
        "Look at 'Tar Beach Part I' (1988) and read the story in the fabric: the girl Cassie flies over the George Washington Bridge, claiming the sky — and the quilt's borders carry text narrating her flight, so the art is read as much as seen. Then read the form's history: Ringgold began quilting with her mother, a fashion designer, and turned the craft into a vehicle for stories about Black life that painting alone couldn't carry. The flying girl is autobiographical: Ringgold said she has been 'flying' in her art all her life.",
        "Faith Ringgold — 'Tar Beach' (1988) and the story-quilt form",
        ["Contemporary", "American", "Story Quilt"],
    ),
    "pain-howardena-pindell-182": _entry(
        "Pindell is best known for her 'video drawings' — canvases covered in thousands of small punched-out circles, many backed with paper dots — and for 'Free, White and 21' (1980), a 12-minute video in which she performs her experiences of racism against a white actress dismissing them. She has been unflinching about both aesthetics and politics since the 1960s.",
        "Look at a 'video drawing' up close and then from a distance: the punched dots read as pure pattern from across the room and as an obsessive, physical labor when you're near — Pindell has said the repetitive process is part of the meaning. Then watch 'Free, White and 21': Pindell, in a blonde wig and whiteface, plays the dismissive white woman while her real voice narrates experiences of discrimination. The format — a woman testifying and being ignored — is the art.",
        "Howardena Pindell — the video drawings and 'Free, White and 21' (1980)",
        ["Contemporary", "American", "Conceptual"],
    ),
    "pain-kerry-james-marshall-183": _entry(
        "Marshall paints Black life at monumental scale — interiors, gardens, housing projects, portraits — in a style that mixes realism with flat, graphic color, and he has said his project is to make Black figures so present in painting that they can't be ignored. His work sells for tens of millions, and he is widely considered the most important living American painter.",
        "Look at 'Vignette (The Joy of Living)' or the 'Garden Project' series and notice the two directions: real spaces (the housing project, the garden) and impossible ones (the same spaces transformed into paradise). Then read his stated ambition: he has said he wants to create images of Black life with 'the authority of a master' — European painting's language used for subjects it never admitted. The flat black skin tones are deliberate: he mixes them to read as solid, heroic color, not shadow.",
        "Kerry James Marshall — the 'Garden Project' and the Black-pantheon portraits",
        ["Contemporary", "American", "Figurative"],
    ),
    "pain-amy-sherald-184": _entry(
        "Sherald paints Black Americans in flat, gray-scale skin against saturated backgrounds — most famously her 2018 official portrait of Michelle Obama, which made her the first Black woman to paint a First Lady's portrait. The gray skin is her signature: she has said it 'removes the color from the conversation' and lets the viewer see the person.",
        "Look at the Michelle Obama portrait and notice what Sherald kept and removed: the First Lady sits in a geometric dress by a Black designer, against a patterned background — and her skin is flat gray, which shocked the public and was exactly the point. Then read her method: she works from photographs, and the gray skin pushes the figure into a space that's neither naturalistic nor abstract. Her subjects — people from her community, often strangers she approaches — hold their poses with a poise that turns portraiture into celebration.",
        "Amy Sherald — the Michelle Obama portrait (2018) and the gray-skin portraits",
        ["Contemporary", "American", "Portraiture"],
    ),
    "pain-njideka-akunyili-crosby-185": _entry(
        "Akunyili Crosby builds large figurative paintings from photo transfers, acrylic, charcoal, and collaged fabric — dense domestic scenes of Nigerian life that layer her two homes, Lagos and Los Angeles, into one image. Her breakthrough works show a young couple (she and her husband) in interiors where Nigerian family photographs and American pop imagery share the same wall.",
        "Look at 'Drown' (2012) or 'The Beautiful Ones' and spend time with the surfaces: the figures are painted, but the backgrounds are built from photo transfers of Nigerian family archives and patterned fabric — the domestic details (the lacy curtains, the portraits) are as much the subject as the people. Then read the project: Akunyili Crosby has said she is 'trying to understand how a relationship between two cultures works inside one home.' The paintings are arguments that identity is made of layers, not choices.",
        "Njideka Akunyili Crosby — 'Drown' (2012) and the domestic layers",
        ["Contemporary", "Nigerian-American", "Figurative"],
    ),
    "pain-wangechi-mutu-186": _entry(
        "Mutu makes collages, paintings, and sculptures that splice the female body with machine parts, plants, and animal forms — a science-fiction critique of how Black women's bodies are represented and mutilated. She grew up in Nairobi, trained in sculpture, and her work has moved from intimate collage to monumental bronze figures in public plazas.",
        "Look at an early collage (the 'Pin Up' and 'Sleeping Serpent' series) and read the hybrid bodies: the figures combine magazine cutouts, ink, and organic forms into creatures that are beautiful and wounded at once. Then read her stated subject: the imagery of the exoticized, violated, medicalized Black female body in Western culture — and her answer, which is to build new bodies entirely. Her recent bronzes, like the monumental figures at New York's Rockefeller Center in 2023, take the same project into the round.",
        "Wangechi Mutu — the hybrid collages and the Rockefeller Center bronzes",
        ["Contemporary", "Kenyan-American", "Sculpture"],
    ),
    "pain-lynette-yiadom-boakye-187": _entry(
        "Yiadom-Boakye paints invented portraits of Black people who don't exist — figures made up from her imagination, titled after the times they could have lived ('The Hour of Bewilderbeast'). She has said she 'makes paintings with the people in them as props' and that she has 'no interest in realism as a process.'",
        "Look at a Yiadom-Boakye portrait and read the absence: the sitters are fictional, with no backstory, no commission, no identity — which is precisely the point she has made for two decades. Then read her method: she paints quickly, often in a day, in a muted palette that evokes Old Master portraits, and she has said the paintings are 'less about the person and more about the painting.' Her fictional Black sitters fill a gap in art history — and the silence of their invented identities is the content.",
        "Lynette Yiadom-Boakye — the invented portraits and 'The Hour of Bewilderbeast'",
        ["Contemporary", "British", "Figurative"],
    ),
    "pain-jadé-fadojutimi-188": _entry(
        "Fadojutimi — born in London in 1993, of Nigerian and Japanese heritage — paints huge, saturated canvases of memory and emotion: layered strokes, bold color, and gestural forms that suggest places and feelings without naming them. She is among the youngest artists ever to see her work auctioned for millions, and she has said her paintings are 'maps of my mind.'",
        "Look at a Fadojutimi canvas and notice the size and the color first: the paintings swallow the wall, and the palette — electric blues, pinks, yellows — is emotional before it's pictorial. Then read her stated method: she paints from memory of places she's been (her mother's garden, a walk) and of feelings, and the canvases are built over many sessions of layering and erasing. The titles ('Yet Another Day to Wake,' 'The Woven Warped Garden of Ponders') are poetry, and the paintings are her attempt to make the poem visible.",
        "Jadé Fadojutimi — the memory maps and the saturated canvases",
        ["Contemporary", "British", "Abstract"],
    ),
    "pain-salman-toor-189": _entry(
        "Toor paints intimate, nocturnal scenes of queer South Asian life — men in bars, apartments, and hammams, rendered with loose brushwork and glowing color. Born in Lahore, he came out as gay while living in Pakistan, and his work has been read as a portrait of 'what it's like to be gay and South Asian' — a subject he has said is 'both personal and political.'",
        "Look at a Toor painting ('The Bar' or 'Friends in a Green Room') and notice the light: the scenes glow from within, and the figures — men dancing, talking, watching — inhabit the space with ease that took the artist decades to find. Then read the biography behind the work: Toor grew up in Lahore, moved to the US, and his painting of a queer South Asian domestic life is the subject of a major 2022 exhibition that explicitly framed it as 'queer diasporic' art. The tenderness is the politics.",
        "Salman Toor — 'The Bar' and the queer South Asian scenes",
        ["Contemporary", "Pakistani-American", "Queer"],
    ),
    "pain-amoako-boafo-190": _entry(
        "Boafo — from Accra, Ghana — paints Black subjects, often people he knows, with bold finger-painted marks and expressive color against abstracted flat backgrounds. His portraits made him a market phenomenon in the early 2020s, and he has said his work is a way of 'celebrating Black identity and Black joy' without trauma as the frame.",
        "Look at a Boafo portrait and notice the technique first: much of the skin and clothing is applied with fingers and hands, leaving visible strokes and dabs that make the surface feel alive. Then read his project: his subjects — friends, fashion figures, community members — are painted large and close, with backgrounds reduced to abstract blocks of color. Boafo has been explicit that his portraits refuse the 'suffering narrative' of much Black art: the figures' confidence and ease is the argument.",
        "Amoako Boafo — the finger-painted portraits of Black joy",
        ["Contemporary", "Ghanaian", "Portraiture"],
    ),
    "pain-tunji-adeniyi-jones-191": _entry(
        "Adeniyi-Jones makes large-scale paintings of Black figures in rhythmic, dancing poses — often in indigo and ultramarine — that channel the Nigerian modernists and the Yoruba traditions he grew up with. He has said his work is about 'the cadence of the Black body,' and his compositions repeat like music or textile patterns.",
        "Look at an Adeniyi-Jones canvas and follow the repetition: the figures recur across the canvas like notes or motifs, and the blue-on-blue palette creates a rhythm that is the painting's real subject. Then read the influences he names: the Nigerian master painters (like Uche Okeke) and the indigo dye traditions of West African cloth. His dancers are not portraits — they're a physical idea: the body as pattern, memory, and music.",
        "Tunji Adeniyi-Jones — the dancing figures and the indigo palette",
        ["Contemporary", "Nigerian-British", "Figurative"],
    ),
    "pain-louise-giovanelli-192": _entry(
        "Giovanelli paints scenes that hover between realism and abstraction — a stage curtain, a swimmer, a shimmering surface — working from photographs of staged scenes and live performance. Her canvases of the early 2020s (the 'Seance' paintings) show faces emerging from darkness like apparitions, and she has become one of the most discussed young painters in Europe.",
        "Look at a Giovanelli painting of a performer or a curtain and notice the focus: the image is almost photographic at the center and dissolves at the edges, so the painting seems to be happening in front of you. Then read her method: she photographs staged scenes and live performances, then paints from the images, and she has said she is interested in 'the moment before something is understood.' The 'Seance' faces — emerging from dark ground — make that threshold literal.",
        "Louise Giovanelli — the 'Seance' paintings and the staged-performance method",
        ["Contemporary", "British", "Painting"],
    ),
    "pain-giotto-193": _entry(
        "Giotto — the 14th-century Florentine painter Dante called 'the pride of the painters' — is credited with inventing modern painting: he gave figures weight, volume, and real emotion, breaking the flat Byzantine style that had dominated for centuries. His fresco cycle in the Scrovegni Chapel in Padua is one of the foundations of Western art.",
        "Look at the Scrovegni Chapel's 'Lamentation' and notice what was new: the figures have mass, the composition has depth, and the mourners express grief with their bodies — angels literally tumble out of the sky. Then compare a Giotto with any Byzantine painting of the same subject: the Byzantine figures float and stare; Giotto's stand, weep, and grieve. Dante mentioned him by name in the Divine Comedy, and the chapel is the closest thing painting has to a birth certificate.",
        "Giotto — the Scrovegni Chapel 'Lamentation' (c. 1305)",
        ["Proto-Renaissance", "Italian", "Fresco"],
    ),
    "pain-piero-della-francesca-194": _entry(
        "Piero della Francesca — a mathematician who painted — built his compositions on geometry so rigorous that his 'Flagellation of Christ' is measured in 3D reconstructions. He wrote treatises on perspective and the regular solids, and his fresco of the resurrected Christ in Sansepolcro was called 'the greatest painting in the world' by Aldous Huxley.",
        "Look at the 'Flagellation of Christ' (c. 1455) and notice the split: the flagellation happens in a classical colonnade on the left, while three modern men confer on the right — and the mystery of who they are and why they're there has occupied scholars for centuries. Then measure the painting yourself: Piero constructed the floor in perfect perspective, and the figures' positions obey a strict geometry. His 'Resurrection' in Sansepolcro was painted so the light in the chapel seems to come from Christ — an optical trick built from the same mathematics.",
        "Piero della Francesca — 'The Flagellation of Christ' (c. 1455)",
        ["Renaissance", "Italian", "Perspective"],
    ),
    "pain-matthias-grünewald-195": _entry(
        "Grünewald — the most intense religious painter of the German Renaissance — made the Isenheim Altarpiece (c. 1512–16), whose crucified Christ is covered in wounds, thorns, and spasms of agony. The altarpiece was painted for a hospital chapel whose patients suffered from skin diseases, which is why the body of Christ mirrors their suffering.",
        "Look at the Isenheim Altarpiece's Crucifixion and notice the anatomy: Christ's body is contorted, skin greenish and covered in lash wounds, fingers rigid — painted for an audience of hospital patients who bore the same marks. Then look at the altarpiece's closed and open states: closed, it shows the agony; opened, the resurrection glows in gold and light. Grünewald's identity was so obscure that his name was lost until the early 20th century — only his work survived, and it needed no signature.",
        "Matthias Grünewald — the Isenheim Altarpiece (c. 1512–16)",
        ["Northern Renaissance", "German", "Religious"],
    ),
    "pain-pieter-bruegel-the-elder-196": _entry(
        "Bruegel painted the lives of peasants — weddings, dances, winter hunts — at a scale and detail no one had attempted, and his landscapes of 1565, the months cycle, helped invent the genre of landscape painting. He was called 'Peasant Bruegel,' but his peasant scenes are moral allegories: his 'Land of Cockaigne' shows the lazy dreaming of food while the world starves.",
        "Look at 'Hunters in the Snow' (1565) — the first great winter landscape — and read the human details: the hunters return tired, the dogs trail, the village glows below the hills, and the whole painting is one long exhale. Then look at 'Land of Cockaigne' (1567) and see the satire: three men lie asleep under a table of food, with a roasted pig walking toward them carrying a knife — the medieval fantasy of effortless plenty, painted as a warning about greed and sloth.",
        "Pieter Bruegel the Elder — 'Hunters in the Snow' (1565) and 'Land of Cockaigne'",
        ["Northern Renaissance", "Flemish", "Genre"],
    ),
    "pain-artemisia-gentileschi-197": _entry(
        "Gentileschi was the first woman admitted to Florence's Accademia, a friend of Galileo, and the painter of the most ferocious Judith in art history — her 'Judith Slaying Holofernes' (c. 1620) shows two women working together to behead a general, blood spraying. At 17 she was raped by her father's colleague and testified in a trial that used thumbscrews to test her truthfulness.",
        "Look at 'Judith Slaying Holofernes' and notice the realism of the violence: Judith and her maid work as a team, arms braced, and the blood is anatomically plausible — Gentileschi had studied the body in a way few women painters could. Then read the biography against the painting: the trial of her rapist, which she won, is inseparable from the fury in her canvases, and scholars have argued the self-portraits — Judith as a self-portrait is one theory — show her making the story her own. She painted Holofernes being beheaded more than once, which is its own statement.",
        "Artemisia Gentileschi — 'Judith Slaying Holofernes' (c. 1620)",
        ["Baroque", "Italian", "Caravaggisti"],
    ),
    "pain-rachel-ruysch-198": _entry(
        "Ruysch — daughter of a botanist — painted flower still lifes so precise that her 1716 painting of a rose in a glass vase took eight years. She worked from the age of 15 into her 80s, outliving her male rivals, and her work sold for double that of Rembrandt's at her death; her flowers, she admitted, could never have grown together in nature — the bouquets were ideal, not real.",
        "Look at a Ruysch bouquet and read the impossibility: flowers from different seasons bloom together in one vase, and the composition is balanced with a naturalist's eye and a mathematician's control. Then read her method and career: her father's botanical garden gave her models, and she painted with a precision the microscope was beginning to rival. The vase of flowers in one of her paintings is famously a single rose that took years — and her signature is a small fly, her play on her own name.",
        "Rachel Ruysch — the flower still lifes and the eight-year rose",
        ["Dutch Golden Age", "Still Life", "Baroque"],
    ),
    "pain-élisabeth-vigée-le-brun-199": _entry(
        "Vigée Le Brun was Marie Antoinette's favorite portraitist — she painted the queen over 30 times — and one of the most successful women artists in history, with a client list that ran from the Queen of France to Catherine the Great. She fled the Revolution and painted her way across Europe for twelve years, and she is one of the only major portraitists to paint herself smiling.",
        "Look at her 1790 self-portrait — in which she paints her own portrait, with a palette and a smile, and a fresh young face at 35 — and read what was radical: formal self-portraits were not supposed to smile, and hers broke the rule. Then read her survival story: as the Revolution closed in, she escaped Paris with her daughter and a passport she obtained from the revolutionary government itself — because, she wrote, her passport declared her 'the painter.' Her portraits of Marie Antoinette, painted in gauzy dresses, were the queen's own PR campaign, and they worked until they didn't.",
        "Élisabeth Vigée Le Brun — the 1790 self-portrait and the Marie Antoinette portraits",
        ["Rococo", "French", "Portraiture"],
    ),
    "pain-camille-corot-200": _entry(
        "Corot painted the golden light of French landscapes for six decades and was the bridge between classical landscape and Impressionism — the Impressionists called him 'Papa Corot,' and his open-air paintings taught them how to see. He also made the 'souvenir' paintings — invented, idealized landscapes of places he'd been — which made him rich and, later, the most forged painter in history.",
        "Look at a Corot landscape ('The Bridge at Narni' or a 'souvenir') and notice the silver-gray tonality and the soft light — his paintings look like memory, which is why his late 'souvenirs' blur real places into ideal ones. Then read the forgery story: Corot reportedly signed or let stand over 2,000 'authentic' Corots in his lifetime, and he famously told a collector, 'I'm not sure I haven't painted this one myself.' The Impressionists studied his value of tone over line — Monet called him 'the only master.'",
        "Camille Corot — 'The Bridge at Narni' (1826) and the 'souvenirs'",
        ["Realism", "French", "Landscape"],
    ),
    "pain-gustave-courbet-201": _entry(
        "Courbet invented Realism and declared himself 'the first and only representative' of it — he painted laborers, burials, and his own enormous self-portrait studio instead of the gods and nymphs the academy demanded. His 'The Stone Breakers' (1849) and 'A Burial at Ornans' put working life on the scale of history painting, and his politics were as confrontational as his art: he was jailed for his role in the Paris Commune and died in exile.",
        "Look at 'A Burial at Ornans' (1850) and read the scandal: a rural funeral painted on a canvas the size of a history painting — the academy's grandest format used for ordinary people — and the critics called it ugly, democratic, and dangerous. Then read the politics: Courbet refused the Legion of Honor, joined the Commune, and was ordered to pay for the demolition of the Vendôme Column — a bill that bankrupted him and drove him to Switzerland, where he died. 'I have never been a communist,' he said, 'I have never been a revolutionary' — the paintings say otherwise.",
        "Gustave Courbet — 'A Burial at Ornans' (1850) and the Realist manifesto",
        ["Realism", "French", "19th Century"],
    ),
    "pain-james-mcneill-whistler-202": _entry(
        "Whistler — American-born, London-based — painted 'Arrangement in Grey and Black No. 1' (1871), the portrait of his mother that became an icon, and then sued the critic John Ruskin for calling one of his paintings 'a pot of paint flung in the public's face.' Whistler won the libel case, was awarded a farthing (a quarter of a penny), and was bankrupted by the costs — but the trial established that art could be judged on its own terms.",
        "Look at 'Arrangement in Grey and Black No. 1' and read Whistler's own title: it is not 'Portrait of My Mother' — the human subject is deliberately secondary to the arrangement of tones, which was the radical claim. Then read the Ruskin trial of 1878: Ruskin, Europe's most powerful critic, attacked 'Nocturne in Black and Gold,' Whistler sued, and in court Whistler defended his 'Nocturne' by saying it was about the art of painting, not the scene. The trial's real verdict — delivered by history — was Whistler's: criticism would never have that power again.",
        "James McNeill Whistler — 'Arrangement in Grey and Black No. 1' (1871) and the Ruskin trial",
        ["Aestheticism", "American-British", "Portraiture"],
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
