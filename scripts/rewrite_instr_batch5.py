#!/usr/bin/env python3
"""Rewrite artworks instructions — batch 5/5 (chunk 4: ids 354-404).

Final artworks batch: kill the "VERB the X first: ... Then the Y: ..."
template. Each instruction gets a personal, painting-specific voice —
experiential, no first/then scaffolding, <=450 chars.
"""
import json

PATH = "app/src/main/assets/topics/artworks.json"

REWRITES = {
    "artw-salvator-mundi-354": (
        "The most expensive painting ever sold is all in the hand. Christ holds a crystal sphere painted "
        "with triple refraction — the globe bends the background like a real lens, a detail scholars cite "
        "for Leonardo's authorship. The other hand rises in blessing, fingers dissolving into the smoky "
        "softness of sfumato, every edge blurred, as if the blessing is still being given. It sold for "
        "$450.3 million in 2017, and the attribution fight is still running."
    ),
    "artw-the-goldfinch-355": (
        "Get close — the whole painting is only 33 by 23 centimeters. A goldfinch is chained by its leg "
        "to a feeder box, head turned, watching you with one dark eye. The pale wall behind is painted in "
        "a few loose strokes with the bird's shadow falling across it, and the warmth of the light makes "
        "this tiny canvas feel like a whole room. It is the most beloved small painting in the world."
    ),
    "artw-the-art-of-painting-357": (
        "Walk into Vermeer's studio: the artist sits with his back to you, painting a model dressed as "
        "Clio, the muse of history, with her laurel wreath and trumpet. The chandelier above gleams with "
        "candlelight, a map of the Netherlands covers the back wall, and the heavy curtain in the "
        "foreground is pulled aside to let you in. Vermeer refused to sell this painting even when he was "
        "drowning in debt — it is his manifesto, and he kept it."
    ),
    "artw-the-geographer-358": (
        "Catch the scholar mid-thought: dividers in hand, he has just looked up from his maps as if a "
        "question answered itself, and the window light falls across his face and papers. The walls are "
        "covered with charts, a globe stands on the cabinet, and a book lies open on the table. Vermeer "
        "painted only two lone men — this and The Astronomer — probably from the same model, measuring "
        "the world."
    ),
    "artw-girl-interrupted-at-her-music-359": (
        "You just walked into the room and broke the song. The girl has stopped playing mid-note and "
        "turned to face you, hands still at the keys of the virginal. The wine glass and jug on the table, "
        "the viola da gamba on the floor, and the dark painting on the wall that seems to show a man "
        "watching — the scene is a courtship, and you are the intruder."
    ),
    "artw-woman-with-a-pearl-necklace-360": (
        "Watch the mirror do almost nothing: it returns only the faintest pale reflection of her face — "
        "in Dutch painting the mirror is vanity, and here it refuses to show her. She holds two yellow "
        "ribbons up to the pearls to test them, and the pearls themselves still lie on the table, "
        "waiting. The moment is before, not after, and the window light turns everything to gold."
    ),
    "artw-the-return-of-the-prodigal-son-361": (
        "The hands say everything: the father, nearly blind and bent with age, rests his hands on the "
        "ragged son's shoulders with a tenderness that is the whole painting. The son's shoes are worn "
        "through, his back bare, his head shaved like a beggar's. The older brother stands in shadow at "
        "the right, excluded and resentful. Rembrandt painted this at the end of his life — bankrupt, "
        "grieving, stripped — and it is his last word on forgiveness."
    ),
    "artw-belshazzars-feast-362": (
        "Watch the king flinch: Belshazzar twists away from the table, crown askew, face lit by ghostly "
        "letters that no one else can read. The wine in his goblet is caught mid-splash. Rembrandt had a "
        "Jewish scholar copy real Aramaic, written right to left the way the guests would read it: Mene, "
        "Mene, Tekel, Upharsin — numbered, weighed, divided. The writing on the wall, painted correctly."
    ),
    "artw-danae-363": (
        "She is not a goddess but a flesh-and-blood woman raising herself on the bed as the golden light "
        "— the god's arrival — pours over her. The maidservant pulls back the curtain, the cupid above "
        "the bed cries, and the entire subject is the moment before. Rembrandt repainted Danaë's head and "
        "the maidservant across a decade, and in 1985 a visitor slashed the canvas and threw acid on it; "
        "the restoration took years."
    ),
    "artw-the-polish-rider-364": (
        "Ride into the dark with a stranger: a young man sits calmly on a pale horse, fur cap and saber "
        "marking him a soldier, but his face is thoughtful, almost gentle. The hills darken, the sky "
        "gathers, and the path ahead is hidden — he moves out of one darkness into another. Nobody knows "
        "who he is: a Polish nobleman, a mercenary, or an idea of the solitary traveler."
    ),
    "artw-the-annunciation-leonardo-365": (
        "Check the angel's wing before the message: it is built of bird feathers, anatomically impossible "
        "— a mistake that dates this to Leonardo's early twenties, still in Verrocchio's workshop. "
        "Gabriel kneels in the garden, hand raised in blessing; Mary sits behind a marble table, her "
        "right hand lifting in surprise, her left still on the book she was reading. The young Leonardo "
        "was already breaking rules."
    ),
    "artw-the-virgin-of-the-rocks-366": (
        "This is not a real cave but a geological fantasy: limestone towers, dark water, and plants "
        "drawn from life, arranged like a dream of the earth's beginning. Inside it, Mary's hand hovers "
        "over the infant John while the angel points at the infant Jesus — four figures locked in a "
        "pyramid that holds the strangeness in balance. The light comes from nowhere and everywhere."
    ),
    "artw-ginevra-de-benci-367": (
        "Her eyes do not quite meet yours. Ginevra, a poet and a philosopher's daughter, is painted "
        "against a juniper bush — a pun on her name, since 'ginevra' sounds like 'ginepro,' juniper — "
        "and the sprigs are drawn with botanical care. The panel's back carries a wreath of laurel and "
        "palm, the symbols of poetry and virtue. It is the only Leonardo painting in the Americas."
    ),
    "artw-the-baptism-of-christ-368": (
        "Find the angel on the left: golden curls, head turned toward you, painted with a softness the "
        "rest of the picture lacks. That is Leonardo, then a teenager in Verrocchio's workshop — and "
        "legend says the master was so outdone he gave up painting forever. Compare it with the harder, "
        "flatter John the Baptist, the dove, and the river: the difference is the first brushwork we can "
        "certainly give to Leonardo."
    ),
    "artw-pallas-and-the-centaur-369": (
        "Read the centaur's face: half man, half horse, bow in hand, he bows to Athena with a "
        "resignation that is almost gentle — her grip on his hair is both a hold and a caress. Her dress "
        "is embroidered with the three rings of the Medici, and her calm, spear-straight stillness is "
        "reason ruling instinct. Scholars still argue whether the painting is about wisdom, politics, or "
        "the cost of civilization."
    ),
    "artw-the-mystic-nativity-370": (
        "Look up before you look down: twelve angels dance in a circle over the stable roof while, "
        "beneath it, three angels embrace three men and three devils flee into cracks in the earth. "
        "Below, Mary kneels before the child and Joseph sleeps — but the stable is a cave and the sky is "
        "dark and strange. Botticelli signed it in Greek at the century's turn, when Savonarola had "
        "Florence trembling with doom — the painting is that fear, and the hope inside it."
    ),
    "artw-the-madonna-of-the-meadow-371": (
        "Trace the triangle: Mary's head is the apex, her blue robe and red dress spreading to the base "
        "where the two children sit — a composition so balanced it feels inevitable. Then watch the "
        "smallest gesture in the painting: John holds a reed cross, and the infant Jesus reaches for it, "
        "the child reaching for the instrument of his own death, painted with complete serenity. This "
        "pyramid became the model for every Madonna painted after it."
    ),
    "artw-the-sistine-madonna-372": (
        "Start at the bottom, where two cherubs lean on the wooden frame, looking up at the Virgin — "
        "added at the end, they became the most copied angels in Western art. Then Mary herself: "
        "barefoot, young and grave, she steps forward out of the clouds holding the child, and the "
        "curtain behind her is pulled back as if heaven has opened. Saint Sixtus and Saint Barbara "
        "kneel at the sides — but it is the two bored-looking cherubs who stole the painting."
    ),
    "artw-the-marriage-of-the-virgin-373": (
        "The temple is the star: a domed building with colonnades drawn in perspective so precise that "
        "your eye travels through the whole painting to its vanishing point — Raphael at 21, showing "
        "off. Down front, the high priest joins Mary's and Joseph's hands while the disappointed suitors "
        "snap their withered rods: the legend says only Joseph's rod flowered. Raphael signed and dated "
        "this, his first fully formed masterpiece."
    ),
    "artw-st-george-and-the-dragon-374": (
        "The whole painting fits in your palm — 28 by 21 centimeters, made to be carried. The lance "
        "pierces the dragon's mouth at the instant of the kill, the horse rears, the saint leans into "
        "the blow, and the princess flees small and golden in the background. Raphael gave it to Henry "
        "VII of England, his first contact with the English court — a tiny painting carrying a "
        "diplomatic message."
    ),
    "artw-the-deposition-375": (
        "Feel the dead weight: Christ's body is carried diagonally, head down, one arm hanging — the "
        "men under him strain in every muscle. The Virgin has fainted and is held up by the women, Mary "
        "Magdalene grasps Christ's hand, and the young man at right carries the crown of thorns and the "
        "nails. Raphael painted this as he was absorbing Michelangelo's drama: the grief is physical, "
        "and the calm of his early work is breaking apart."
    ),
    "artw-the-dying-slave-376": (
        "Watch a body let go: the young man's torso twists in one long, soft curve — head falling back, "
        "arm rising, not struggling but released. The face is calm, almost asleep, which is why the "
        "figure reads as death's gentle arrival rather than resistance. Michelangelo carved him for the "
        "tomb of Pope Julius II, one of a series of captives meant to show the arts imprisoned by death."
    ),
    "artw-moses-michelangelo-377": (
        "Moses has horns. They come from a famous mistranslation — the Hebrew for 'rays of light' became "
        "the Latin for 'horns,' and Michelangelo carved exactly what the text said. From the horns, the "
        "beard flows like water and the muscles coil; one foot is raised, ready — this is the instant "
        "before he smashes the tablets. The statue sits, but everything in it is about to move."
    ),
    "artw-david-with-the-head-of-goliath-378": (
        "The dead face is Caravaggio's own — he painted himself as Goliath, sending the canvas to the "
        "pope as a confession and a plea for pardon after fleeing Rome for murder. David holds the head "
        "at arm's length, sword still in hand, and his expression is not triumph but horror: he looks at "
        "the face he has killed as if he recognizes it. The boy is mercy; the head is the artist."
    ),
    "artw-narcissus-caravaggio-379": (
        "The painting is built on one symmetry: the kneeling boy and his reflection form a single circle, "
        "face meeting face, the self meeting itself. The background is black and empty — only his "
        "shoulder and face catch the light, as if the world has disappeared into his obsession. His "
        "hands brace against the water as he leans in, and the mirror he is falling into gives back "
        "everything and nothing."
    ),
    "artw-judith-slaying-holofernes-380": (
        "Do not flinch: Judith saws through Holofernes' neck while her maidservant pins his shoulders "
        "down, the blood spraying in arcs, the general's legs kicking, both women straining with real "
        "physical effort. Artemisia painted this in Florence years after her rape and her brutal trial, "
        "and the violence is often read as her revenge made art. It is not a sanitized Bible scene — it "
        "is a killing, lit like a stage."
    ),
    "artw-self-portrait-allegory-of-painting-381": (
        "She looks at you mid-turn, caught on her way back to the canvas — brush in one hand, palette in "
        "the other, hair loose, sleeve pushed up. The attributes are those of Painting personified, an "
        "allegory usually shown as an anonymous woman; Artemisia made it her own face, the first time a "
        "woman painted herself as the art. The concentration is not posed. It is the job."
    ),
    "artw-the-fall-of-icarus-382": (
        "Find the tragedy nobody is watching: in the lower right corner, two legs kick out of the sea — "
        "Icarus is already almost gone. The farmer plows, the shepherd stares at the sky (at the sun, "
        "not the boy), the fisherman casts, and the ship sails on with full wind. Bruegel turned the "
        "grandest myth of ambition into the great image of the world's indifference to a drowning boy."
    ),
    "artw-hunters-in-the-snow-383": (
        "Descend the hill with the hunters: three men and their dogs walk down through the snow carrying "
        "spears, and the only kill between them is one small fox. Below spreads the whole frozen world — "
        "skaters on a pond, a fire burning, houses smoking — warm and busy while the hunters come down "
        "into it. Bruegel painted this in 1565 for a series of the months; this is January, and the cold "
        "has never been painted better."
    ),
    "artw-the-tower-of-babel-384": (
        "Climb the construction site: a colossal tower spirals into the clouds in concentric rings, each "
        "level crowded with cranes, scaffolding, and tiny workers — Bruegel modeled its spirals on the "
        "Roman Colosseum he saw in Italy. Trace the roads winding up the sides, the brick courses, the "
        "arches; find King Nimrod in red at the base, inspecting the work. The hubris is drawn like an "
        "engineering drawing, which is what makes it funny and terrifying."
    ),
    "artw-the-oxbow-385": (
        "The painting splits down the middle, and you have to choose a side: on the left, a dark, "
        "storm-torn wilderness with a dead tree; on the right, a sunlit valley of neat fields and a "
        "bending river. On the rocky foreground at the left sits a tiny figure with an umbrella, "
        "sketching — Cole himself, easel planted on the wild side. The storm is breaking, the valley is "
        "lit, and the river bends like a snake."
    ),
    "artw-the-course-of-empire-386": (
        "Walk the same hill through five ages: a lone Indian in the wilderness, then a pastoral village, "
        "then a marble city of columns and domes at the height of glory — and then Destruction, the same "
        "city burning while a general on horseback crosses a bridge, and finally Desolation, ruins "
        "standing empty under a dead moon. Cole painted the series in the 1830s as a warning to America: "
        "every empire that reached this peak was destroyed."
    ),
    "artw-kindred-spirits-387": (
        "Stand on the ledge between the two friends: Cole points into the wild Catskill valley while "
        "Bryant, arms crossed, follows his gaze. Durand painted this after Cole's death, from sketches "
        "made in the actual gorge, as a tribute to the founders of the Hudson River School. The stream "
        "runs far below, the mountains fade into blue haze — the whole image is American romanticism's "
        "creed: two men, a wilderness, and nature as the nation's true cathedral."
    ),
    "artw-the-gulf-stream-388": (
        "The calm is the mystery. A lone Black sailor lies on the broken deck of his boat, propped on "
        "his elbow, staring past you — he does not look at the sharks circling the hull, the snapped "
        "mast, or the waterspout gathering on the horizon. Homer reworked this painting for years, and "
        "critics read it as a man adrift and beyond help: the sea full of death, and the sailor beyond "
        "caring. Race, fate, and the ocean, held in one steady gaze."
    ),
    "artw-snap-the-whip-389": (
        "Feel the crack travel down the chain: barefoot boys hold hands and run across a schoolyard "
        "meadow, and the whipping force flings the last two outward — legs flying, hats flying, the "
        "chain about to break. The schoolhouse sits in the background, the meadow runs with wildflowers, "
        "the mountains haze in the distance. Homer painted American childhood in 1872 as freedom, "
        "summer, and an open field — and it became the emblem of all three."
    ),
    "artw-the-gross-clinic-390": (
        "Stand in the amphitheater: Dr. Samuel Gross lectures over a live operation, bloody scalpel "
        "raised, his head crowned by the light from above — a priest of the new scientific medicine. "
        "The patient's leg lies open on the table, his mother cowers in the shadows at left, and a "
        "woman in the foreground shields her eyes — the only figure who cannot look. The 1875 "
        "Centennial rejected the painting: the blood, the scalpel, the bare wound were unfit to show."
    ),
    "artw-the-agnew-clinic-391": (
        "Compare the room to its predecessor and see medicine change: everything is white now — the "
        "surgeons, the gowns, the amphitheater — the patient is draped and unconscious, and a nurse "
        "stands by, a figure who did not exist in Eakins's Gross Clinic fourteen years earlier. Dr. "
        "Agnew presides with one hand raised mid-sentence above the operation, a lecturer as much as a "
        "surgeon. The blood is gone; the antiseptic age has begun."
    ),
    "artw-max-schmitt-in-a-single-scull-392": (
        "Find the painter inside his own painting: Max Schmitt rests mid-stroke, oars trailing, face "
        "turned toward you — and in the middle distance a second sculler rows directly at the viewer: "
        "that is Eakins, bearded and hatted, putting himself into the scene. The Schuylkill is painted "
        "with a glassy exactness, reflecting the bridge and boathouses, and the light is set to one "
        "specific late-afternoon hour."
    ),
    "artw-madame-x-393": (
        "The profile is everything: Virginie Gautreau's head turns in sharp silhouette against the "
        "dark, her skin almost white, her nose and chin drawn with a precision that makes her look "
        "carved from marble. The details that wrecked a career are in the dress — bare shoulders, the "
        "jeweled strap slipping, the deep plunge of the bodice. The Salon found her pose indecent; the "
        "scandal drove Sargent out of Paris. He repainted the strap — and never forgave it."
    ),
    "artw-the-daughters-of-edward-boit-394": (
        "The subject is the space between the four sisters: no one posed them together. One sits on the "
        "floor, one stands before the huge vases, two stand back in the shadows — and the little girl "
        "in front stares straight at you, doll in her arms, as if about to step out of the painting. "
        "Sargent arranged the room like a stage and let the girls drift through it; the gap between them "
        "is what the painting is about."
    ),
    "artw-carnation-lily-lily-rose-395": (
        "Wait for dusk with the girls: two children light paper lanterns among the flowers, and the "
        "whole painting exists only in the half hour when the lantern glow and the fading blue of the "
        "sky balance exactly. Sargent spent two summers painting just that hour, the girls standing "
        "still while assistants relit the lanterns. Absorbed in their task, their white dresses glow in "
        "the twilight — the most patient light in Victorian art."
    ),
    "artw-women-of-tahiti-396": (
        "Sit in the shade with them: two women, one in red, one in blue, sit on the sand beneath a "
        "tree, faces calm and distant, bodies simplified into broad flat shapes — Gauguin stripped the "
        "world down to color and stillness. The sea behind is painted in bands of blue and green, no "
        "perspective, no hurry. He had just fled Europe, and this is the Tahiti he dreamed before the "
        "real one disappointed him — the peace of the painting is partly a fantasy."
    ),
    "artw-the-yellow-christ-397": (
        "Let the color do the believing: the crucified Christ is painted bright yellow, and the Breton "
        "fields behind him run in bands of gold and green — Gauguin traded natural color for the color "
        "of feeling, and the crucifix glows like a harvest sun. Three Breton women in white caps kneel "
        "at its foot, heads bowed, and the village with its church spire sits flat as a tapestry. Rural "
        "faith, painted from the inside of autumn."
    ),
    "artw-nevermore-398": (
        "She does not look at the raven, and the raven does not look at her. The Tahitian woman lies "
        "naked on the bed, head turned away, body calm and heavy, while a black bird perches on the "
        "frame behind her and two figures whisper in the shadows. Gauguin borrowed the title from Poe "
        "but said the painting was not about Poe — the raven is the dark thought that followed him "
        "through his last years in Tahiti, and the whole room is full of watching."
    ),
    "artw-mr-and-mrs-clark-and-percy-399": (
        "You are the third person in the room. Ossie Clark stands by the open balcony door, hand on "
        "hip, in a printed shirt; his wife Celia sits barefoot in the chair with the cat, Percy, on her "
        "lap — they are looking at each other, not at you, and the flat is their real flat, white "
        "furniture and all. Hockney painted the couple at the hinge of their marriage; the light comes "
        "in from the balcony, and the cat is the only one who notices the painter."
    ),
    "artw-cape-cod-morning-400": (
        "Watch a held breath before the day begins: a woman leans out of her kitchen window, hands on "
        "the sill, chin lifted, looking at the sky — not the sea, the sky above it. Hopper made his "
        "wife Jo pose for hours to get the angle right, and her upward gaze is the whole subject. The "
        "morning sun pours through the two windows, cutting sharp shadows across the lawn, and the "
        "yellow house glows against the green."
    ),
    "artw-black-on-maroon-401": (
        "Stand close and let the darkness happen: a black form floats inside the maroon field, its "
        "edges ragged and smoky, as if the dark is eating into the color around it. Rothko's late "
        "paintings darkened as his mood darkened, and the black shapes read like doorways — or voids. "
        "The maroon glows from within, built from thin layers, so the rectangle seems to hover rather "
        "than sit."
    ),
    "artw-one-number-31-1950-402": (
        "There is no top and no bottom — walk around it and let the eye go anywhere. Black and white "
        "drip over aluminum silver, lines crossing and looping with no beginning and no end, the whole "
        "2.7-by-5.3-meter field woven from paint thrown, dripped, and splattered from sticks and cans "
        "while Pollock walked the canvas laid flat on the floor. The painting is an all-over field of "
        "energy, and it keeps the eye moving forever."
    ),
    "artw-orange-and-yellow-403": (
        "Let the color become a place: a wide orange band floats above a smaller yellow one, the edges "
        "so soft that the colors glow against each other instead of meeting. The canvas is nearly three "
        "meters tall, and Rothko insisted his big paintings hang low, close to the floor, so you stand "
        "inside the color field rather than in front of it — 'like a landscape,' he said. Give it the "
        "time it wants; it changes while you watch."
    ),
    "artw-two-forms-404": (
        "Walk around the conversation: one bronze form is pierced with circular holes, the other is "
        "solid and curved, and they lean toward each other like two figures — one open, one closed. The "
        "space between them is as much the sculpture as the bronze itself. Hepworth polished some areas "
        "and left others rough, so the light moves across the surfaces as you circle, and the two shapes "
        "keep finding new ways to relate. She made it the last year of her life."
    ),
}


def main():
    data = json.load(open(PATH, encoding="utf-8"))
    by_id = {e["id"]: e for e in data}
    done = 0
    for eid, new_ins in REWRITES.items():
        assert eid in by_id, f"missing id {eid}"
        assert len(new_ins) <= 450, f"{eid} too long: {len(new_ins)}"
        by_id[eid]["exploreAction"]["instruction"] = new_ins
        done += 1
    json.dump(data, open(PATH, "w", encoding="utf-8"), ensure_ascii=False, indent=2)
    print(f"rewrote {done} instructions")


if __name__ == "__main__":
    main()
