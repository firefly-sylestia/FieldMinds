#!/usr/bin/env python3
"""Rewrite 51 artworks instructions — batch 1 of 5.

The previous batches wrote every instruction as a template: "VERB the X
first: [fact]. Then the Y: [fact]." Even with varied opener verbs, the
structure read as copy-paste. This batch replaces each instruction with a
genuinely handcrafted one — a voice specific to that painting, experiential,
with varied structure. ≤ 450 chars each.
"""

import json
import sys
from pathlib import Path

PATH = Path(__file__).resolve().parent.parent / "app/src/main/assets/topics/artworks.json"

# id -> new instruction (handcrafted, painting-specific)
REWRITES = {
    "artw-the-last-supper-1498-54": (
        "Let your eye ride the vanishing lines — every one lands on Christ's head, the still "
        "center of a room in chaos. The apostles break into clusters of three, each answering "
        "the same sentence differently: shock, doubt, accusation. Judas alone leans away from "
        "the light, his hand already reaching for the bread that seals his betrayal."
    ),
    "artw-liberty-leading-the-people-60": (
        "She is not a marble allegory — she is the loudest person in the painting, a working "
        "woman who has grabbed the tricolor and is mid-stride over the bodies. The boy beside "
        "her, a pistol in each hand, was painted from a real street urchin Delacroix saw at the "
        "barricades. Notre-Dame rising through the smoke pins the myth to a real Tuesday in Paris."
    ),
    "artw-venus-of-willendorf-105": (
        "No face, no feet — only belly, breasts, and thighs, carved small enough to close a hand "
        "around. She was made to be held and passed from palm to palm, not displayed; a 3D scan "
        "showed she cannot even stand upright. One theory says she is a self-portrait: the view a "
        "woman has of her own body looking down. Hold your hand in a fist and imagine her inside it."
    ),
    "artw-mask-of-tutankhamun-108": (
        "The eyes are the trick: quartz and obsidian, set to catch light so the mask stares back "
        "from inside its coffin. The blue stripes of the headdress are lapis lazuli hauled two "
        "thousand miles from Afghanistan — blue that had to cross the world to sit on a dead boy's "
        "brow. On the forehead, a cobra and a vulture: the two Egypts, still ruled from the grave."
    ),
    "artw-venus-de-milo-112": (
        "Her hips face right, her shoulders face left — a contrapposto twist that keeps the marble "
        "alive even with both arms gone. The missing arms are the point: a hand holding an apple "
        "was found near her on Milos, and the island's name means apple. Try to imagine the gesture "
        "and you become the sculptor; every visitor does."
    ),
    "artw-augustus-of-prima-porta-115": (
        "Every inch is a press release. The breastplate shows the Parthians handing back the Roman "
        "eagle standards — a diplomatic return staged as a battlefield win. The cupid at his feet "
        "claims descent from Venus; the bare feet say he has already left the mortal realm. Augustus "
        "never wore this much meaning at once, but that was never the point."
    ),
    "artw-the-oath-of-the-horatii-1784-118": (
        "Three arms, three columns, three straight lines — the brothers are built like the "
        "architecture behind them, which is exactly the Neoclassical program. Then the other half "
        "of the canvas: women folded into soft curves, collapsed in grief, literally drawn in "
        "different shapes. David arranged the sexes into geometry, and the painting became the "
        "salute of real revolutionaries a few years later."
    ),
    "artw-the-birth-of-venus-119": (
        "Her neck is too long, her shoulder slopes at an angle no shoulder can make — Botticelli "
        "elongated her on purpose so she reads as a dream, not a woman. She stands dead vertical "
        "inside a tangle of wind gods and falling roses, stillness at the center of motion. The "
        "shell was a pilgrim's badge; the pose copies an ancient statue of Venus Pudica. Everything "
        "about her is borrowed, and everything is new."
    ),
    "artw-david-1504-by-michelangelo-121": (
        "Walk to where the statue was meant to be seen — thirteen meters below. That is why the "
        "right hand is oversized: it is the hand that will kill, and it is the part you meet first "
        "from the ground. His brows are knit, his nostrils flared, his eyes locked left on an enemy "
        "we cannot see. This is not victory; this is the second before the sling swings."
    ),
    "artw-pieta-1499-by-michelangelo-122": (
        "Michelangelo was 24, and the only work he ever signed carries the signature across Mary's "
        "sash — carved there after he heard pilgrims credit the statue to someone else. Mary looks "
        "younger than her son: virgins do not age, he said. The trick is scale — her lap is carved "
        "wide enough to hold a grown man, and the cloth of her robe is a mountain against the "
        "smooth, slack body of Christ."
    ),
    "artw-the-calling-of-saint-matthew-125": (
        "The divine arrives as a shaft of street light and a pointing finger — no halos, no clouds. "
        "The tax collectors are real Roman working men hunched over coins, and the boy at the end "
        "of the table points at himself as if to say 'who, me?' The joke is that you do the same "
        "thing the moment you recognize the scene: this is what a call looks like in a smoky room."
    ),
    "artw-the-anatomy-lesson-of-127": (
        "The corpse's face is half lit, half dark — and it is the only face in the room not watching "
        "the dissection. Scholars read it as death looking back at the living. Rembrandt was 25 and "
        "already breaking the rules: eight surgeons arranged not in a row but in a pyramid aimed at "
        "Tulp's hands, and those hands are pinching the wrong tendons — he painted from a book, not "
        "a cadaver."
    ),
    "artw-the-milkmaid-1658-128": (
        "The milk is frozen mid-stream, a thin white line that has been falling for 350 years. The "
        "wall behind her is blank white — except for the faint shadow of her figure Vermeer let "
        "fall on it. Up close, the bread's crust is hundreds of separate specks of pigment; the "
        "Delftware tile at the baseboard hides a Cupid, and the foot warmer on the floor is a "
        "symbol of longing. A servant, painted with the stillness of a saint."
    ),
    "artw-the-rokeby-venus-129": (
        "She is painted from behind, which lets Velázquez draw one unbroken S down her spine. The "
        "face in the mirror is deliberately blurred — the reflection is softer than the body, as if "
        "mirrors lie. In 1914 a suffragette slashed her seven times with a meat cleaver to protest "
        "the arrest of Emmeline Pankhurst. Cupid holds the mirror not for vanity but so she can see "
        "herself the way we see her: the subject of the painting is being looked at."
    ),
    "artw-the-ecstasy-of-saint-teresa-130": (
        "The chapel is a theater: a hidden window above pours real light down gilded rays, and "
        "marble spectators lean from the side 'balconies' to watch. Teresa's habit is carved in "
        "deep, wind-swept folds — cloth in motion — while the angel's robes are smooth. Her face is "
        "not pain. Bernini found the one expression sculpture can hold that painting cannot: a "
        "swoon that never ends."
    ),
    "artw-the-blue-boy-1770-131": (
        "The academy said blue belonged to skies, never to the main figure — so Gainsborough painted "
        "the entire boy blue on purpose. Up close the satin is a scribble of loose strokes made with "
        "long brushes under candlelight; step back and it resolves into the most convincing fabric "
        "in English painting. The boy is not an aristocrat — he is a hardware dealer's son in a "
        "costume his family bought for the sitting."
    ),
    "artw-an-experiment-on-a-bird-132": (
        "The candle is hidden behind the glass globe, so every face is lit differently: the children "
        "bright, the scientist in profile, the old man sunk in shadow. The bird is a cockatoo — a "
        "precious pet, not a lab animal — and Wright chose it so the sacrifice feels personal. This "
        "is the Enlightenment as family drama: reason is literally the light in the room, and the "
        "bird is the price of knowledge."
    ),
    "artw-the-hay-wain-1821-133": (
        "The water is painted in loose, broken strokes of color that refuse to blend — the move "
        "that electrified the French when this crossed the Channel in 1824. The cottage on the left "
        "belonged to Willy Lott, who supposedly left it for a total of a few days in 80 years. "
        "Constable called the sky 'the chief organ of sentiment,' and this sky is full of moving "
        "clouds he spent a lifetime studying."
    ),
    "artw-the-slave-ship-1840-134": (
        "The horror hides in the beauty: in the lower-right water, chained arms and legs rise from "
        "the waves while fish circle them, and the sunset around them is glorious. Turner based it "
        "on the Zong, whose captain threw 133 enslaved people overboard to claim insurance. He "
        "exhibited it with his own poem: 'Hope, Hope, fallacious Hope!' The storm's heart is a "
        "single white-hot patch — look for the limbs before you let the color win."
    ),
    "artw-wanderer-above-the-sea-of-fog-135": (
        "He stands with his back to us, and that refusal is the whole painting: we cannot see his "
        "face, so we climb into his boots and look at the fog ourselves. The peaks below float like "
        "islands, sketched by Friedrich in the Elbe sandstone mountains. Nobody knows who the man "
        "is — a self-portrait, a forestry official, an invention. Stand where he stands and the "
        "sublime does the rest."
    ),
    "artw-the-gleaners-1857-136": (
        "Three women, three degrees of bending — nearly upright, bent low, doubled over — a "
        "progression that makes the work feel eternal. Behind them the harvest glows in gold; "
        "ahead of them the field is bare. Wealthy Parisians read it as a socialist threat and "
        "compared the women to praying figures. Millet made the people everyone walked past the "
        "subject of a large painting, and the scandal never quite died."
    ),
    "artw-ophelia-1852-137": (
        "Every flower is a line from the play: willow for forsaken love, nettles for pain, daisies "
        "for innocence, poppies for death. Millais painted them from real specimens over five "
        "months while Elizabeth Siddal lay in a studio bathtub — the lamps went out once and the "
        "chill nearly killed her. Ophelia's mouth is open, and her hands float upward: she is "
        "drowning, or she is singing, as Shakespeare wrote, 'as one incapable of her own distress.'"
    ),
    "artw-little-dancer-of-fourteen-139": (
        "Bronze body, real tutu, real ribbon, real slippers — the mix of sculpture and costume "
        "shocked the 1881 exhibition and critics called her rat-faced. She was Marie van Goethem, "
        "a dancer at the Paris Opera who then vanished from the records. Her chin is up, her hands "
        "clasped behind her back, her feet in a working fourth position. This is not a pretty pose; "
        "it is a job."
    ),
    "artw-the-dance-at-le-moulin-140": (
        "The sun comes through the acacia trees and lands on faces as patches of yellow and blue — "
        "light painted as color, not brightness, which is the Impressionist move in a single stroke. "
        "The dancers are real: friends, a Montmartre laundress, a model named Margot, men in straw "
        "hats and cheap suits. Renoir painted it on the spot over weeks, and the crowd is working-"
        "class Paris having a Sunday."
    ),
    "artw-the-potato-eaters-143": (
        "The people are painted in the color of the food they eat — muddy browns and grays — and "
        "van Gogh said that was the point: 'they have earned their meal with these hands digging "
        "in the earth.' One lamp casts a weak yellow glow and everything else sinks into shadow, "
        "so the meal is a small circle of warmth in the dark. He painted it with real laborers as "
        "models and was proudest of this, his first great painting."
    ),
    "artw-cafe-terrace-at-night-145": (
        "This is the first night sky van Gogh painted without black — deep blue with stars that "
        "radiate halos of yellow and white. He painted it after dinner, in the dark, by lamplight, "
        "in the Place du Forum in Arles, where the café still stands with its yellow awning. The "
        "terrace glows warm against the cool blue street beyond: the whole painting is a study of "
        "what artificial light does to the night."
    ),
    "artw-the-kiss-1889-rodin-147": (
        "Their lips never touch — a millimeter of air between them, frozen forever, which is why "
        "the sculpture is more erotic than any completed kiss. Rodin chose the moment of maximum "
        "desire, not its fulfillment. The woman's skin is polished to a gloss while the rock around "
        "them stays rough, so the lovers are carved out of the cave they are hiding in. They are "
        "Paolo and Francesca from Dante, who fell in love reading and died for it."
    ),
    "artw-the-sick-child-1886-148": (
        "The surface looks wounded: long, scraped strokes where Munch dragged a palette knife "
        "through wet paint. He painted this six times over forty years — his sister Sophie died of "
        "tuberculosis when he was 15, and he said 'my art is rooted in this.' The girl's face is "
        "almost dissolved into the pillow, barely there, while the mother's head bends in shadow. "
        "The dying child is less solid than the grief around her."
    ),
    "artw-madonna-1894-149": (
        "She is not a Christian virgin — she is a woman mid-trance, eyes closed, head tilted back, "
        "hair loose, standing in a shallow S-curve. Munch called it 'the pause that the whole world "
        "stops for' and printed versions of it with a border of writhing sperm and a small fetus — "
        "a church icon redesigned as a fertility goddess. The dark halo is not gold; it is the "
        "moment of conception."
    ),
    "artw-the-birth-of-venus-cabanel-150": (
        "One arm above the head, body reclined on the water, utterly passive — the pose the Salon "
        "adored and the Impressionists were about to mock. Napoleon III bought it on sight, and it "
        "became official French taste. The foam-flecked toes, the floating putti, the polished "
        "skin: this is the painting the rebels were rebelling against, and it was the hit of the "
        "very year they were rejected."
    ),
    "artw-the-bathers-151": (
        "Renoir came back from Italy convinced his style was 'too thin' and wanted bodies as solid "
        "as Raphael's. So these women are real — soft stomachs, heavy thighs — painted in a warm "
        "rose that makes them glow. The stream, rocks, and leaves around them use the broken "
        "Impressionist brush; the women are built with smooth, blended strokes. He said he "
        "repainted the legs endlessly, and it shows: they are the point."
    ),
    "artw-the-angelus-1859-152": (
        "Two peasants, two bowed heads, one shared prayer at the sound of the evening bell. The "
        "painting became the most reproduced image in France — and then Dalí had it X-rayed and "
        "found the ghost of a small coffin under the wheelbarrow: the couple may originally have "
        "been standing at a graveside, and the coffin was painted over. The man's cap in his hands, "
        "the setting sun throwing their shadows forward like sundials: a prayer, or a funeral."
    ),
    "artw-the-execution-of-lady-jane-153": (
        "She was queen for nine days, and she is 16, blindfolded, her hands reaching blindly for "
        "the block while a man in white steadies her. Delaroche painted history as a private "
        "tragedy, not a public event — no crowd, no spectacle, just the straw, the axe leaning "
        "against the wall, and the ladies-in-waiting collapsing at the right. It became the most "
        "reproduced painting in England."
    ),
    "artw-the-balcony-1869-by-manet-154": (
        "None of the three looks at any of the others — each stares off in a different direction, "
        "and the servant behind them is only half a face in the dark. The critics called them "
        "'three marionettes,' and that was the compliment: Manet painted modern people together "
        "without connection. The fan, the gloves, the green railings — the props of Parisian "
        "leisure, painted flat and bright, doing nothing."
    ),
    "artw-the-two-fridas-159": (
        "Two Fridas sit side by side, hearts exposed, joined by one artery — painted the year she "
        "divorced Diego. The Frida in the European dress holds forceps on her own cut artery and "
        "bleeds onto her white skirt; the Frida in the Tehuana dress holds a miniature portrait of "
        "Diego as a child and stays whole. The same woman, the same heart, two costumes, one "
        "bleeding: that is the painting in one sentence."
    ),
    "artw-self-portrait-with-thorn-160": (
        "The thorns have sunk into her neck and blood beads at each puncture — she wears her pain "
        "like jewelry and meets your eyes without flinching. The dead hummingbird at her throat is "
        "a Mexican talisman of failed love; the black cat behind one shoulder and the monkey behind "
        "the other are her stand-ins for herself and for Diego. She looks at you until you look away."
    ),
    "artw-swans-reflecting-elephants-161": (
        "Three swans float on the water; their reflections are elephants. The trick is that Dalí "
        "matched the swans' silhouettes to the shapes of the bare trees on the far bank, so the "
        "reflection merges bird and landscape into trunk and ear. One tree on the left even echoes "
        "the elephant-trunk curve. It is a double image that only works because the water is "
        "perfectly still — and it always is."
    ),
    "artw-the-old-guitarist-163": (
        "The guitarist is folded almost double, his legs crossed so tightly the anatomy is "
        "impossible — Picasso distorted the body to express collapse, not to copy it. Everything is "
        "blue: skin, guitar, air. X-rays show he painted this over an earlier painting and a "
        "woman's portrait — he was so poor he reused canvases. The blue is grief and cold, and the "
        "blind man plays for no one."
    ),
    "artw-three-musicians-164": (
        "It is painted to look like cut paper — flat shapes, visible edges — but it is all oil: "
        "Picasso faked collage so he could keep the look with the permanence of paint. Step back "
        "and the overlapping planes snap into three figures — Pierrot with a clarinet, Harlequin "
        "with a guitar, a monk with sheet music. The dog squeezed between their legs at the bottom "
        "left is almost hidden, like a signature."
    ),
    "artw-the-weeping-woman-165": (
        "Her face is shattered into planes: one eye higher than the other, teeth as sharp triangles "
        "biting a handkerchief. Picasso broke the face the way the war broke its subjects — she is "
        "the weeping companion to Guernica. The model was Dora Maar, whose crying fits Picasso said "
        "fascinated him. The tears are real drops sliding down the painted edges, and the black "
        "outlines hold the fragments together like glass."
    ),
    "artw-composition-with-red-blue-168": (
        "The red rectangle is the heavy anchor; the small blue and yellow blocks keep the canvas "
        "from tipping over. Mondrian moved the black lines by fractions of a millimeter, taping "
        "them until the balance felt absolute — the grid is not even, it is tuned. He called this "
        "neoplasticism and believed these three colors and right angles could express universal "
        "harmony. There is nothing else to see, and that is the whole point."
    ),
    "artw-number-5-1948-169": (
        "There is no center, no top, no bottom — Pollock said he was 'in' the painting, and the web "
        "of lines pulls your eye in every direction at once. He poured, dripped, and flicked enamel "
        "from sticks, sometimes mixed with sand and broken glass, so the surface has a texture you "
        "almost feel. It sold privately in 2006 for $140 million — a record — and it is all middle, "
        "deliberately, forever."
    ),
    "artw-blue-poles-170": (
        "Eight vertical lines of blue cut straight through the chaos — and they break every rule of "
        "Pollock's own all-over style, which is why scholars still argue about when he added them. "
        "The storm around them is reds, yellows, and turquoise poured in layers, and the poles read "
        "like the only structure in the room. Australia paid $1.3 million for it in 1973, then a "
        "record, and the country argued about it for years."
    ),
    "artw-orange-red-yellow-171": (
        "The rectangles have no hard edges — the colors bleed into each other, so the shapes seem "
        "to breathe and shift as you watch. Rothko built each color from many thin layers, so the "
        "blocks glow as if lit from within. He hated the idea that these were color studies: they "
        "are, he said, about the human experience. Stand close and let the fuzzy edges do their "
        "work; it sold for $86.9 million in 2012."
    ),
    "artw-christinas-world-173": (
        "She is not resting — her arms are braced, her body twisted, and she is mid-crawl toward a "
        "farmhouse that sits impossibly far away. Wyeth painted his neighbor Christina Olson, who "
        "could not walk and dragged herself everywhere; he watched her do exactly this. He painted "
        "her from behind so we see the effort, not the face, and the fields are enormous around "
        "her. The loneliness is not metaphor; it is her real life."
    ),
    "artw-a-bigger-splash-174": (
        "A white explosion frozen against flat blue — and no one in the water to have made it. The "
        "diver has already jumped in, and the pool is about to go still again; the painting is a "
        "still from a movie that is not running. Hockney painted it from a photo in a book, using "
        "masking tape for the pool lines, in two weeks in his London studio. The empty chair, the "
        "flat house, the palm: Los Angeles, holding its breath."
    ),
    "artw-garrowby-hill-176": (
        "The road is bright yellow — not gray — and it curves across the canvas like a river of "
        "light, pulling your eye up and over the hill's crest. Hockney painted this view from his "
        "Yorkshire childhood after moving home from California, and he said he wanted the feeling "
        "of driving over a hill. The fields are flat, saturated patches of green and yellow, like a "
        "quilt on the land, with no people and no buildings in sight."
    ),
    "artw-the-red-studio-178": (
        "The red is total — walls, floor, everything — one flat field of color with almost no "
        "shading, and Matisse's own artworks float in it like islands. Find them: a bronze "
        "sculpture, Le Luxe II on the wall, a ceramic dish, a clock with no hands, an empty chair. "
        "He said he wanted 'to express space and reality with color alone.' The clock has no hands "
        "because time has stopped inside the red."
    ),
    "artw-woman-with-a-hat-179": (
        "The cheeks are green, the forehead orange, the nose pink — no skin tones anywhere. "
        "Matisse used color to express light and feeling, not to imitate flesh, and the critic who "
        "saw this at the 1905 Salon called the painters 'les fauves' — the wild beasts. The name "
        "stuck. The hat is an enormous confection of feathers and flowers that dominates the "
        "portrait of his wife Amélie, painted in flat, clashing strokes."
    ),
    "artw-the-snail-1953-180": (
        "Colored rectangles radiate outward from the center like a shell, building from deep blues "
        "and purples at the heart to bright yellows and oranges at the rim. Matisse was bedridden, "
        "so he 'painted with scissors' — assistants painted sheets of paper and cut them into "
        "shapes he directed from bed. This was one of the last things he made before he died at "
        "84, and the spiral is a snail's shell only if you step back far enough."
    ),
    "artw-adele-bloch-bauer-181": (
        "The dress and background are covered in real gold leaf, patterned with spirals, eyes, and "
        "Byzantine geometry — Klimt's father was a gold engraver. The face is the only softly "
        "painted part of the canvas, floating above the ornate body like a person inside a shrine. "
        "The painting was seized by the Nazis, returned to Adele's niece in 2006 after a Supreme "
        "Court fight, and sold for $135 million. The woman herself died of meningitis at 43."
    ),
    "artw-melancholy-and-mystery-182": (
        "The shadows are impossibly long and point in directions the light cannot explain — the "
        "sun does not exist inside the painting, so the square feels like a stage set before the "
        "play starts. A little girl rolls a hoop toward the light, the only living thing in the "
        "city. At the end of the street stands a shape that is either a statue or a figure; nobody "
        "has ever agreed. De Chirico called this metaphysical art, and the Surrealists never "
        "recovered."
    ),
    "artw-the-false-mirror-183": (
        "The iris of the eye is a daytime sky with clouds — the organ of sight replaced by the "
        "thing it sees. The pupil is a dark circle that reads as an eclipse, so the eye is both a "
        "mirror of the sky and a hole in it. Magritte called the eye a false mirror because it "
        "reflects the world but sees nothing. The Surrealist André Breton put it on the cover of "
        "his journal, and it has stared back from that position ever since."
    ),
    "artw-metamorphosis-of-narcissus-188": (
        "Left, a man crouches by a pool, his head mirrored in the water. Right, that same body has "
        "become a giant stone hand holding an egg, and from the egg grows the narcissus flower — "
        "the myth made literal, the man turning into the flower he is named for. In the background: "
        "a naked figure on a pedestal, a dog, chess pieces. Dalí wrote a poem to go with it, and "
        "Freud, whom he idolized, analyzed the painting's paranoia."
    ),
    "artw-christ-of-saint-john-189": (
        "You see Christ from above and slightly behind, floating — no cross, no nails, no crown of "
        "thorns, just a man, a shadow, and a triangle of light. Dalí took the composition from a "
        "drawing by Saint John of the Cross. The scale is the shock: a tiny boat with a fisherman "
        "sits far below, making Christ enormous. In 1986 a man threw a bottle of ink at it in "
        "protest. The painting survived; the ink did not."
    ),
    "artw-lavender-mist-190": (
        "The lavender is almost invisible — a faint haze under the black and white lines, and "
        "Pollock named the whole painting for a color most viewers never notice. Unlike his densest "
        "canvases, this one has breathing room: the drips and loops spread in a wide, calm rhythm. "
        "It is one of the works that made the CIA's cultural program fall in love with Abstract "
        "Expressionism — proof, they thought, that America was free, daring, and new."
    ),
    "artw-wrapped-reichstag-191": (
        "For 14 days in 1995 the German parliament became a soft, silver, anonymous mass — 106,000 "
        "square meters of fabric tied with 15 km of blue rope. The wrapping erased the building's "
        "history for two weeks and made people see it fresh. It took Christo and Jeanne-Claude 24 "
        "years and 13 rejected parliamentary votes to get permission, and two million people came "
        "to look at a government building wearing a blanket."
    ),
    "artw-running-fence-192": (
        "39 kilometers of white nylon, 5.5 meters tall, crossing 14 private ranches, a highway, and "
        "the edge of the Pacific — for exactly two weeks in 1976. Getting permission took 42 months "
        "and 18 public hearings. The fence changed with the weather: a solid wall in fog, a "
        "shimmering line in sun. Christo called it 'a ribbon of light' following the land's "
        "contours, and after the two weeks it was removed completely, leaving no trace."
    ),
    "artw-puppy-1992-193": (
        "The dog is 12 meters tall and made of 60,000 living flowers — steel, soil, and a hidden "
        "irrigation system. The cute subject becomes absurd and monumental at once, which is the "
        "joke. The 'fur' is begonias, petunias, and marigolds planted on a steel frame, so the "
        "sculpture literally changes with the seasons — it blooms every spring outside the "
        "Guggenheim in Bilbao. A city once tried to buy it and stalled on the logistics of a "
        "flowering terrier."
    ),
    "artw-the-holy-virgin-mary-197": (
        "Mary is a Black woman built from layered paint, glitter, and map pinheads, standing inside "
        "a golden halo — an African Virgin, deliberately remaking the sacred image. The elephant "
        "dung she is made with is Ofili's signature material, and in 1999 Mayor Giuliani threatened "
        "to cut the museum's funding over it. Visitors lined up around the block to see what the "
        "fuss was about. The outrage was the point; the painting outlasted the mayor."
    ),
    "artw-rhythm-0-1974-198": (
        "On a table: 72 objects — feathers, grapes, bread, a knife, a chain, and a loaded pistol "
        "with one bullet. Abramović stood motionless for six hours while the audience was told they "
        "could use anything on her however they wished. It started gentle: people fed her, dressed "
        "her wounds. It ended when the gun was aimed at her neck and a fight broke out. The "
        "performance taught her what people will do when given permission, and she never repeated "
        "it."
    ),
    "artw-your-gaze-hits-200": (
        "The words are printed in the aggressive style of a magazine ad — white Futura on red — "
        "and they describe what is happening in the image: a classical marble bust in profile, "
        "your gaze hitting the side of her face like an object. Kruger borrowed the visual language "
        "of advertising to turn it against the culture that sells. The bust is the kind of thing "
        "museums expect you to stare at without thinking; she makes the staring itself the subject."
    ),
    "artw-i-shop-therefore-201": (
        "A cropped, anonymous hand holds a card the way a passport holder holds an ID — the card "
        "is the proof of existence. Descartes said 'I think, therefore I am'; Kruger replaces "
        "thinking with shopping, so identity becomes what you buy. The design is pure ad: bold "
        "type, red frame, flat colors. It became the slogan of the 1980s — on T-shirts, posters, "
        "and tote bags — and the critique got so popular it became the merchandise."
    ),
    "artw-one-and-three-chairs-202": (
        "A real wooden chair, a photograph of that chair, and the dictionary definition of 'chair' "
        "— the same object in three languages: object, image, and word. Kosuth's point is that the "
        "art is not any one of them but the idea that connects them all, which makes this the "
        "founding work of Conceptual Art. Which one is the real chair? None, or all three — that "
        "is the work."
    ),
    "artw-the-lightning-field-204": (
        "400 stainless-steel poles across a grid one mile by one kilometer, each tip sharpened to a "
        "point, standing in an empty New Mexico desert. In storm season the poles attract "
        "lightning — but most visitors see them still, and De Maria said the stillness is the art. "
        "You must stay overnight in a small cabin on the site, limited to six visitors at a time, "
        "from May to October. The field is a measure of the sky, and it takes a night to see it."
    ),
    "artw-the-ghent-altarpiece-205": (
        "The central panel is the entire Christian story compressed: the Lamb of God bleeding into "
        "a chalice on an altar, surrounded by fountains, prophets, and a panoramic crowd. It is "
        "also the most stolen artwork in history — looted and dismembered six times in 600 years, "
        "and one panel, The Just Judges, is still missing, replaced by a copy. Closed, the wings "
        "show a stone-grey Annunciation; open them and the color world appears."
    ),
    "artw-the-last-judgement-sistine-206": (
        "Christ returns at the top, arm raised in judgment, Mary tucked under his arm — and every "
        "soul around them is a naked athlete, because Michelangelo painted the body as God's image, "
        "not as shame. The Church disagreed: after his death, painters were hired to add loincloths, "
        "a censorship campaign that took 300 years. Look at the bottom right and you will find "
        "Charon — a pagan ferryman from Greek myth — clubbing the damned into hell with his oar."
    ),
    "artw-the-wedding-at-cana-207": (
        "Find the musician in white in the center foreground playing the viola da gamba — that is "
        "Veronese himself, and the man in blue beside him may be the architect Palladio. The painter "
        "put his friends inside the Bible. The guests wear 16th-century Venetian dress, not ancient "
        "robes, because Veronese painted the miracle as a contemporary Venetian feast. At 6.77 by "
        "9.94 meters, it is the largest canvas in the Louvre."
    ),
    "artw-the-entombment-of-christ-208": (
        "The feet come at you first: bare, grimy, thrust straight out of the canvas — Caravaggio's "
        "way of dragging the sacred down to street level, which made him famous and infamous at "
        "once. Six figures strain with the weight of the corpse: one grips Christ's knees, another "
        "bears the shroud, the women's faces are lit from above by a light that comes from nowhere "
        "in the room. It is one of the great altarpieces of the Baroque."
    ),
    "artw-the-nightmare-1781-209": (
        "The word 'nightmare' literally means night-mare: the horse was believed to sit on sleepers' "
        "chests and crush them. Here the woman lies limp, arms and head hanging off the bed, while "
        "the incubus squats on her chest and the horse's wild head bursts through the curtain "
        "behind. Fuseli painted this in 1781 from folklore and his own sleep paralysis, and it "
        "became the original horror poster — he made engraved copies that sold for decades."
    ),
    "artw-washington-crossing-210": (
        "Almost everything in it is wrong: Washington stands in a small boat that would have sunk, "
        "beneath a flag that did not exist until a year after the crossing, in daylight — the real "
        "crossing was at night in freezing weather, and he almost certainly stayed seated. The "
        "painter, a German who never saw the Delaware, made it more heroic than history. The most "
        "famous American history painting, and it is fiction."
    ),
    "artw-whistlers-mother-212": (
        "The title is 'Arrangement in Grey and Black No. 1' — Whistler named the portrait of his "
        "mother after its color scheme, treating her as a still-life arrangement of tones. The "
        "profile, the black dress, the white cap and handkerchief, the grey wall with its framed "
        "print: everything is a study in muted colors, balanced like a geometric diagram. His "
        "mother sat in profile because she was too old to hold a frontal pose comfortably."
    ),
    "artw-the-floor-scrapers-213": (
        "Three men on their knees scraping a Paris floor, bare backs curved in identical working "
        "postures — labor painted with the seriousness usually reserved for heroes. The sun falls "
        "in a broad diagonal, the scraped wood gleams, long shadows stretch from the workers. The "
        "wine bottles, the brazier, the rolled sleeves, the dust: real Paris, not allegory. The "
        "Salon rejected it; the Impressionists showed it and built their name on modern life."
    ),
}

MAX = 450


def main() -> int:
    data = json.loads(PATH.read_text(encoding="utf-8"))
    by_id = {e["id"]: e for e in data}
    missing = [i for i in REWRITES if i not in by_id]
    if missing:
        print("MISSING ids:", missing)
        return 1
    over = [i for i, t in REWRITES.items() if len(t) > MAX]
    if over:
        print("OVER 450:", over)
        for i in over:
            print(" ", len(REWRITES[i]), i)
        return 1
    changed = 0
    for tid, new in REWRITES.items():
        by_id[tid]["exploreAction"]["instruction"] = new
        changed += 1
    PATH.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"rewrote {changed} instructions")
    return 0


if __name__ == "__main__":
    sys.exit(main())
