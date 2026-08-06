#!/usr/bin/env python3
"""Rewrite artworks instructions — batch 4/5 (chunk 3: ids 302-353).

Kill the "VERB the X first: ... Then the Y: ..." template. Each instruction
gets a personal, painting-specific voice — experiential, no first/then
scaffolding, <=450 chars.
"""
import json

PATH = "app/src/main/assets/topics/artworks.json"

REWRITES = {
    "artw-benin-bronzes-302": (
        "Walk the palace corridors as they stood in 1800: these brass plaques lined the walls, a "
        "continuous record of the Oba's court — warriors, attendants, and the Portuguese traders who "
        "arrived by sea, each with his distinguishing features. Every plaque is a page of a history the "
        "1897 expedition scattered across Europe; the plaques in museums now are a looted kingdom's library."
    ),
    "artw-napoleon-wiley-303": (
        "Get the joke and the challenge in one glance: the rearing horse, the pointing arm, the flying "
        "cape are straight out of David's Napoleon — but the rider is a young Black man in a hoodie and "
        "Timberlands, and the rock under the hooves is carved 'WILEY' where 'BONAPARTE' used to be. Look "
        "at the pattern on the shirt and the bandana, and ask who gets to be painted as a conqueror."
    ),
    "artw-tilted-arc-304": (
        "Stand where the plaza workers stood: a 36-meter wall of rusted steel slashes diagonally across "
        "the Federal Plaza, splitting a shortcut you used to take. Feel the annoyance before you feel the "
        "art — that frustration was the piece working. Then argue with yourself whether a sculpture you "
        "can't walk around is a sculpture at all; that public fight demolished it in 1989."
    ),
    "artw-the-lady-of-shalott-305": (
        "Find the moment of no return: the mirror is cracked from side to side — the poem's exact line — "
        "and the tapestry behind her unravels as she rises. She has left the tower for a man she has "
        "never met, and the boat below will carry her dead to Camelot. Notice her gaze, forward and "
        "steady: she already knows."
    ),
    "artw-beata-beatrix-306": (
        "Meet the woman behind the symbol: this is Elizabeth Siddal, Rossetti's wife, painted as Dante's "
        "Beatrice at the instant of death — eyes closed, hands open, the world's light about to leave. "
        "The red dove drops a white poppy into her palms. Rossetti buried his poems with her and dug "
        "them up seven years later; that grief lives in the painting's stillness."
    ),
    "artw-the-last-of-england-307": (
        "Get on the boat with them: a couple stares back at the receding English coast, faces tight with "
        "a grief too fixed for tears. Brown painted himself and his wife as the models — emigrants in the "
        "wave that emptied Britain's villages. The cabbages hung on the rail were their food for the "
        "voyage; the baby is wrapped in the woman's shawl."
    ),
    "artw-the-awakening-conscience-308": (
        "Watch the moment the song stops: the young woman rises from her lover's lap, hands clasped, "
        "dawning horror on her face as she understands what she has become. The whole room shouts the "
        "same message — the cat toys with the bird, the glove lies discarded, the sheet music is open to "
        "the tune that woke her conscience."
    ),
    "artw-the-light-of-the-world-309": (
        "Check the door before you look at Christ: it has no handle. Hunt painted it deliberately — the "
        "door is the human heart, and it can only be opened from within. Christ waits in the ivy-choked "
        "doorway with his lantern and crown of thorns; the apple among the weeds recalls the Fall. The "
        "painting toured the world drawing crowds, and Queen Victoria asked to see it in private."
    ),
    "artw-the-ardabil-carpet-311": (
        "Walk the 10.5-by-5.3-meter garden of ten million knots. The central sunburst medallion is "
        "flanked by two hanging lamps — one deliberately smaller, because Islamic art breaks perfect "
        "symmetry with a single flaw, a reminder that only God's work is flawless. Then find the weaver's "
        "signature and date hidden in the border cartouches."
    ),
    "artw-the-blue-quran-312": (
        "Touch nothing: the page you're looking at is dyed indigo and the letters are silver and gold. "
        "This was the most expensive manuscript ever made — the dye alone made each of its six hundred "
        "pages worth a fortune. Only about a hundred survive. Read the angular Kufic script against the "
        "blue, and imagine the mosque where pages this wide were read aloud."
    ),
    "artw-tale-of-genji-scroll-313": (
        "Fly over the roofs: the 'blown-off roof' technique removes the ceilings so you look straight "
        "down into the court scenes of 11th-century Japan. The faces are mask-like on purpose — emotion "
        "is carried by the tilt of a head and the fall of a kimono. These are the oldest surviving "
        "painted narratives in Japan, illustrating the world's first novel."
    ),
    "artw-the-great-arch-at-ctesiphon-314": (
        "Stand under the largest brick vault ever built: 34 meters of mud brick in a single span — no "
        "centering, no steel, just courses of brick leaning into each other. It stood for 1,400 years "
        "until the 2025 flood. The Sasanian kings received ambassadors in its shadow, and its shape "
        "echoed through a thousand years of Islamic mosques."
    ),
    "artw-the-standard-of-ur-315": (
        "Read it like a comic strip from 4,500 years ago: on the 'War' side, chariots with solid disc "
        "wheels, soldiers, and prisoners; on the 'Peace' side, a banquet with a lyre and the king "
        "drinking. The figures are inlaid shell, lapis lazuli carried from Afghanistan, and red "
        "limestone. Nobody knows what the box was for — Woolley guessed a standard on a pole."
    ),
    "artw-the-mask-of-agamemnon-316": (
        "Look into the face Schliemann wired the king of Greece about: beaten from a single sheet of "
        "gold, heavy mustache, eyes closed — a mask made to cover a dead king's face, not to imitate a "
        "living one. The catch: the tomb is 400 years older than Agamemnon, so the famous identification "
        "is almost certainly wrong."
    ),
    "artw-apollo-and-daphne-317": (
        "Catch the transformation mid-breath: Daphne's fingers are already sprouting leaves, her hair is "
        "turning to foliage, bark climbs her legs — Bernini froze the instant Apollo's hand lands on a "
        "waist that is no longer flesh. His face is triumph turning to shock. In places the marble is so "
        "thin that light passes through the leaves."
    ),
    "artw-the-gates-of-hell-318": (
        "Stand before a maelstrom: 180 figures from Dante's Inferno pressed, falling, and embracing "
        "across a six-meter bronze door. Find the Thinker at the top of the lintel and the Kiss low on "
        "the right — both began as details of this door and became independent masterpieces. Rodin "
        "worked on it for 37 years and died before it was cast."
    ),
    "artw-the-burghers-of-calais-319": (
        "Walk among six doomed men and notice how ordinary they look: none of them heroic. One covers "
        "his face, one clutches his head, one holds the key to the surrendered city, one walks with "
        "resignation. Rodin broke every rule — no pedestal, no heroism — so you share their walk to the "
        "English camp."
    ),
    "artw-the-unveiling-of-laszlo-320": (
        "Look at the smoke before the victim: the execution is caught mid-volley, the squad casual, "
        "almost bored — Manet reconstructed the scene from newspaper accounts of Maximilian's firing-squad "
        "death in Mexico. The subject was so politically dangerous that the painting could not be shown "
        "in France for years."
    ),
    "artw-the-grande-odalisque-321": (
        "Count the vertebrae from shoulder to hip: there are three too many. Ingres knew it and didn't "
        "care — he wanted 'longer lines,' and the impossible spine bends her body into a swan's neck. "
        "Critics attacked the distortion; the painter defended it as beauty over anatomy. She is the "
        "founding image of the orientalist nude."
    ),
    "artw-the-procuress-322": (
        "Look left before you look at the brothel scene: the musician with the lute is believed to be "
        "Vermeer himself, watching — if so, his only self-portrait. Then the center: a soldier's hand on "
        "a young woman's breast while the procuress holds out her palm for payment. This is Vermeer at "
        "24, ten years before his quiet interiors — his largest and loudest painting."
    ),
    "artw-woman-holding-a-balance-323": (
        "Watch the stillness: her hands hover over a balance with empty pans — she weighs nothing, or she "
        "weighs everything. Behind her hangs the Last Judgement, so her quiet domestic weighing is set "
        "against the final weighing of souls. The pearl box lies open, the coins gleam, the light comes "
        "from the window at left. Nothing moves, and everything is at stake."
    ),
    "artw-the-little-street-324": (
        "Walk down the Vlamingstraat in Delft on an ordinary afternoon: two plain brick houses, a woman "
        "sewing in a doorway, two children playing in the passage — no drama, no grand architecture, "
        "just the street Vermeer saw from his window, cropped like a photograph. It is the only street "
        "scene he ever painted, and the brickwork is laid brick by brick."
    ),
    "artw-the-menaced-assassin-325": (
        "Freeze the thriller one frame before the action: a naked woman lies dead beside a phonograph, "
        "three bowler-hatted men watch from the next room, two more lean in through the window, and the "
        "man at the door holds a club like a waiter. Nobody looks panicked. The menace is the calm."
    ),
    "artw-the-castle-of-the-pyrenees-326": (
        "Ask the physics question first: a mountain-sized tower crowned with a castle hangs over a flat, "
        "hard-edged sea with nothing holding it up. It is heavy, ancient, impossible — and the waves "
        "below are painted in tidy bands, indifferent to the mass above. The absurdity is the point: the "
        "rock stays up because Magritte decided so."
    ),
    "artw-time-transfixed-327": (
        "A full-speed locomotive bursts out of a fireplace grate, smoke curling up the chimney, while the "
        "mantel clock keeps the room's ordinary time. The dining room — candles, mirror, carpet — is "
        "asleep. The poet who commissioned it complained the fireplace hid the train; Magritte answered "
        "that the painting's job was 'to make the everyday visible.'"
    ),
    "artw-the-wounded-deer-328": (
        "Follow the nine arrows: each one, Kahlo said, was a life event — her spine, her marriage, her "
        "surgeries. The deer has her face, antlers still attached, and it runs through a wood of broken "
        "branches toward a waiting sea and storm. She painted it after a spinal operation, and the pain "
        "is anatomically honest: the wound is in the side, and the branch stumbles in the foreground."
    ),
    "artw-henry-ford-hospital-329": (
        "Lie on the bed with her: Kahlo bleeds on a metal hospital cot floating in an empty landscape "
        "while six red ribbons — like umbilical cords — tie her to the fetus she lost, the snail of slow "
        "time, the industrial machine, a pelvis, a flower, and an orchid. She painted it in Detroit while "
        "Rivera covered the city's walls with murals; the skyline shrinks behind her. It is one of the "
        "first paintings of a miscarriage ever made."
    ),
    "artw-the-suicide-of-dorothy-hale-330": (
        "Watch a fall in three frames: Dorothy Hale drops against a blood-red sky, hits the ground, and "
        "lies dead — all in one image, like a comic strip of a tragedy. The band below reads 'The Suicide "
        "of Dorothy Hale… as told to Frida Kahlo.' The friend who commissioned it as a memorial was so "
        "horrified she kept it hidden for years."
    ),
    "artw-not-to-be-reproduced-331": (
        "Try to find the face: the man at the mirror is reflected from behind — the mirror refuses to "
        "return his gaze, showing his back twice. But the Poe novel on the mantel reflects correctly, the "
        "right way round. The mirror obeys physics for the book and disobeys it for the face, and the "
        "title is the punchline: it cannot be reproduced."
    ),
    "artw-the-blank-signature-332": (
        "Count the horse's legs and fail: every time you try, one disappears behind a tree trunk, and "
        "the rider's body is cut into pieces by the wood. The figure is simultaneously in front of the "
        "forest and behind it. Magritte called it the 'blank signature' — the scene identifies its "
        "painter but refuses to be pinned down."
    ),
    "artw-the-umbrellas-333": (
        "Be in two places at once: on the morning of October 9, 1991, 1,340 blue umbrellas opened across "
        "18 miles of Japanese rice fields while 1,760 yellow umbrellas opened across 18 miles of "
        "California hills — two landscapes tied across the Pacific. The project cost $26 million, paid "
        "for by selling Christo's drawings, and ended early when a gust killed a woman in California."
    ),
    "artw-the-mastaba-334": (
        "Float with it: 7,506 oil barrels in red, blue, and mauve stacked into a trapezoid on London's "
        "Serpentine Lake — a form Christo first sketched in 1958 and finally built in 2018, the year "
        "before he died. The barrel is deliberately anonymous; the piece is pure shape and color. The "
        "name is Arabic for 'bench,' a tribute to the ancient Egyptian tombs."
    ),
    "artw-rest-energy-335": (
        "Hold your breath for four minutes: Abramović holds the bow, Ulay holds the arrow, its steel "
        "point pressed against her heart. One tremor in either hand kills her. Their heartbeats were "
        "amplified through a microphone — hers reached 156 beats a minute. The title says it all: the "
        "entire work is the energy of staying still."
    ),
    "artw-24-hour-psycho-336": (
        "Watch the shower scene for an hour. Gordon slowed Hitchcock's Psycho to two frames a second, "
        "stretching 109 minutes into 24 hours, so the film creeps forward like a flipbook. It is "
        "projected on a translucent screen, so you can walk around and watch the backs of the frames too."
    ),
    "artw-broken-circle-spiral-hill-337": (
        "Walk the two gestures the land makes: a ring of white sand broken into two arcs with a jetty "
        "bridging the gap, and nearby a spiral hill of black earth — one form dug out of the land, one "
        "piled up from it. The lake has since flooded the circle, so the work changes with the seasons: "
        "sometimes whole, sometimes half underwater."
    ),
    "artw-womanhouse-338": (
        "Open the front door of the abandoned mansion the students renovated themselves: a kitchen whose "
        "walls are covered in fried-egg breasts, a bedroom that is one giant ironing board, a bathroom in "
        "menstrual red. Every room is a woman's life turned into an environment. Chicago, Schapiro, and "
        "21 students built it in 1972 — the construction work was as much the art as the rooms."
    ),
    "artw-a-line-made-by-walking-339": (
        "Watch the grass remember a walk: Long paced back and forth across a Wiltshire field until the "
        "trampled blades flattened into a pale line, then photographed it from above. The artwork is the "
        "walking itself, recorded in a photograph — it exists only until the grass grows back. A "
        "22-year-old student turned walking into sculpture in 1967."
    ),
    "artw-sky-mirror-340": (
        "Walk around the disc and watch the city bend: a 10-meter concave mirror of polished steel tilts "
        "up, gathering the skyline into an upside-down bowl. Stand at the right distance and you'll float "
        "in the sky with the towers. In 2006 it sat at Rockefeller Center, and all of midtown curved "
        "inside its curve."
    ),
    "artw-wrapped-pont-neuf-341": (
        "See the oldest bridge in Paris as a ghost of itself: 40,000 square meters of golden "
        "sandstone-colored cloth wrapped around every arch, lamp, and stone of the Pont Neuf, tied with "
        "13 kilometers of rope. Nine years of negotiations for two weeks of wrapping in 1985. At night "
        "the lamps — left free of fabric — glowed through the cloth, a row of floating lights."
    ),
    "artw-valley-curtain-342": (
        "Feel the wind that shredded the dream: a 417-meter orange nylon curtain across Rifle Gap in "
        "Colorado, 200,000 square feet of fabric on 41 tons of cable. It was built to last 28 months and "
        "lasted 28 hours — a gale tore it apart, and the artists spent two years picking scraps out of "
        "the canyon. The failure made them famous."
    ),
    "artw-the-destruction-of-the-father-343": (
        "Step into the cave of the dining table: lumpy, flesh-colored plaster forms piled low like a "
        "feast — Bourgeois called it the father's body, dismembered and eaten, from a childhood fantasy "
        "of destroying her own father. The lighting turns the platform into a sacrificial altar, and the "
        "cave is the family room made tomb."
    ),
    "artw-mother-and-child-divided-344": (
        "Walk the line of four steel tanks: a cow and her calf, each split down the middle, each half in "
        "its own tank of formaldehyde, arranged so you pass between the two halves of a single animal. "
        "The organs float pale in the fluid; the calf's body is small and perfect. It won the Turner "
        "Prize in 1995."
    ),
    "artw-michael-jackson-and-bubbles-345": (
        "Read the pose as royal portraiture: Jackson seated, cradling his chimp like a queen with a "
        "lapdog, both dressed in matching gold-trimmed clothes. The material is the joke — porcelain "
        "gilded like a Fabergé egg, a cheap ceramic dressed as treasure. It sold for $5.6 million in 2001."
    ),
    "artw-pumpkin-346": (
        "Meet Kusama's alter ego on a pier in the Seto Inland Sea: a two-meter bronze pumpkin in bright "
        "yellow with black dots, the sea behind it. The dots, she says, are a way to obliterate the self "
        "— the pumpkin that ate the artist's identity. She has made them since childhood, when her "
        "family's farm was covered in pumpkins she painted with dots."
    ),
    "artw-everyone-i-have-ever-slept-with-347": (
        "Step inside a diary: a blue pop-up tent with 102 names appliquéd inside in Emin's own "
        "handwriting — lovers, friends, family, a grandmother, two unborn children, and 'Myself.' The "
        "tent was destroyed in a warehouse fire in 2004; Emin said the loss was 'like losing a diary.' "
        "You're inside it now."
    ),
    "artw-the-dream-of-the-fishermans-wife-348": (
        "Notice the calm before the strangeness: the woman's face is serene, almost bored, while a large "
        "octopus — mouth on hers — embraces her and a small one attends to her. The bodies lock in one "
        "flowing curve, printed with Hokusai's precision. It is the ancestor of an entire genre, and it "
        "influenced Picasso."
    ),
    "artw-the-church-at-auvers-349": (
        "Watch the stone breathe: the church's walls ripple and shimmer in broken, vibrating strokes — "
        "van Gogh painted solid stone the way he painted sky, so the building looks alive. He painted it "
        "in the last summer of his life, months before he shot himself in a nearby field. The path "
        "splits between sunlight and shadow, and the ultramarine sky churns."
    ),
    "artw-the-sower-350": (
        "Step into the wheel of light: the sower walks across the purple field scattering seed, dark "
        "against a chrome-yellow sun that takes up half the canvas. Van Gogh made thirty versions of "
        "this subject — the man, the seed, the sun, the earth, the whole cycle of life in one image. The "
        "colors are impossible, and they work."
    ),
    "artw-the-charnel-house-351": (
        "Look at the table and refuse the easy reading: a woman, a man, and a child lie across it, "
        "flattened in the grey palette of Guernica. There is no killer, no cause, no explanation — only "
        "the result laid out like evidence. Picasso painted it after the liberation of Paris, and never "
        "said which massacre it shows: he meant it for all of them."
    ),
    "artw-the-shades-of-night-352": (
        "Kiss through the linen: two figures embrace with their heads wrapped in white cloth, features "
        "hidden, unable to see each other. Magritte painted this in 1928, the year before his more "
        "famous Lovers, and both are haunted by the same memory — his mother was found drowned with her "
        "nightgown over her face. Romantic or suffocating: the painting won't decide."
    ),
    "artw-anxiety-353": (
        "Join the crowd on the bridge at dusk: faces pressed together in a row — pale, mask-like, "
        "expressionless — each person isolated inside the crowd, under the same blood-red sky as The "
        "Scream. Munch described the setting sun as 'blood.' He painted it a year after The Scream; the "
        "crowd is the subject, and everyone is alone together."
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
