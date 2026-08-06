#!/usr/bin/env python3
"""Batch 1: add 50 new handcrafted artworks to artworks.json (ids 105-154).

Ancient to 19th century masterpieces with real fun facts, handcrafted
teasers and quality-bar instructions. Appends to the existing 56 entries.
Cap 450 (SCHEMA.md). id convention: artw-{slug}-{n}.
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


PATH = Path(__file__).resolve().parent.parent / "app/src/main/assets/topics/artworks.json"


def _entry(byline: str, name: str, teaser: str, instruction: str, target_name: str, tags: list[str]) -> dict:
    return {
        "byline": byline,
        "name": name,
        "teaser": teaser,
        "instruction": instruction,
        "targetName": target_name,
        "tags": tags,
    }


NEW: dict[str, dict] = {
    # ---------- Prehistoric & Ancient ----------
    "artw-venus-of-willendorf-105": _entry(
        "Unknown (c. 28,000 BCE)",
        "Venus of Willendorf (c. 28,000 BCE)",
        "An 11 cm limestone figurine found in 1908 by a workman in Austria — carved by hand some 28,000 years ago, making it older than the pyramids by 25,000 years. It was painted red ochre, and its exaggerated body may have been a fertility symbol — or a self-portrait by a woman artist, a theory proposed because the figure's proportions match what a woman sees looking down at her own body.",
        "Look at the proportions, not the face: the head is covered in what may be braided hair or a woven cap, and the arms rest on the breasts. No face, no feet — only the body matters. Then think about the scale: at 11 cm it fits in one hand, which means it was made to be held and passed around, not displayed. Researchers who made a 3D copy found it has no visible base — it can't stand upright on its own.",
        "Venus of Willendorf (c. 28,000 BCE) — hold it in your mind's hand",
        ["Prehistoric", "Sculpture"],
    ),
    "artw-lascaux-cave-paintings-106": _entry(
        "Unknown (c. 17,000 BCE)",
        "Lascaux Cave Paintings (c. 17,000 BCE)",
        "Discovered in 1940 by four teenagers chasing their dog down a hole near Montignac, France — and inside, 600 painted and 1,500 engraved animals from 17,000 years ago, including aurochs, horses, and stags. The cave was closed to the public in 1963 because the visitors' breath was growing mold on the paintings.",
        "Look at the bulls in the Great Hall: the largest is 5.5 meters long — painted at a scale that could never be seen whole by torchlight, which suggests the paintings were made for a different kind of looking, perhaps ritual. Notice that humans almost never appear (one stick figure with a bird head), and that the painters used the rock's natural contours as part of the animals' bodies. The pigments — ochre and manganese — were ground and blown through hollow bones.",
        "Lascaux Cave Paintings (c. 17,000 BCE) — the Great Hall of the Bulls",
        ["Prehistoric", "Cave Art"],
    ),
    "artw-bust-of-nefertiti-107": _entry(
        "Thutmose (c. 1345 BCE)",
        "Bust of Nefertiti (c. 1345 BCE)",
        "Found in 1912 by German archaeologists in the workshop of the sculptor Thutmose at Amarna, Egypt — and its left eye was never inlaid, a mystery that still has no settled answer. It became the most copied ancient artwork in the world, and Egypt has demanded its return from Berlin since the 1920s.",
        "Look at the symmetry: the bust is perfectly balanced except for the missing left eye — a detail that has fueled a century of theories (a deliberate imperfection? an inlay that fell out? a portrait that was never finished?). Notice the 19-meter-tall crown, which the queen wore for her role as a living stand-in for the goddess Tefnut. The neck is 4 cm long — an Egyptian canon of beauty, not a real body.",
        "Bust of Nefertiti (c. 1345 BCE) — the missing left eye",
        ["Ancient Egypt", "Sculpture"],
    ),
    "artw-mask-of-tutankhamun-108": _entry(
        "Unknown (c. 1323 BCE)",
        "Mask of Tutankhamun (c. 1323 BCE)",
        "The death mask of the boy king — 11 kg of solid gold inlaid with lapis lazuli, carnelian, and turquoise — was found in 1922 still covering the mummy's face inside the innermost coffin. Its beard fell off during a 2014 cleaning at the museum and was hastily glued back on with epoxy.",
        "Look at the eyes: they're made of quartz and obsidian, set to catch light so the mask seems to look back. The stripes on the nemes headdress are lapis lazuli, a stone imported from Afghanistan — 2,000 miles away in 1323 BCE. Then find the cobra and vulture on the forehead: symbols of Upper and Lower Egypt, meaning the king ruled both lands even in death.",
        "Mask of Tutankhamun (c. 1323 BCE) — the eyes and the cobra-vulture",
        ["Ancient Egypt", "Goldwork"],
    ),
    "artw-stele-of-hammurabi-109": _entry(
        "Unknown (c. 1754 BCE)",
        "Stele of Hammurabi (c. 1754 BCE)",
        "A 2.25-meter basalt pillar covered in 282 laws in cuneiform — the oldest nearly complete legal code in history, carved around 1754 BCE in Babylon. It shows King Hammurabi receiving the laws from the sun god Shamash, and one of its rules — 'an eye for an eye' — is still the English phrase for retaliation.",
        "Read the top scene first: Hammurabi stands before the seated sun god Shamash, who hands him the rod and ring of justice — the claim that the law is divine. Then scan the laws: they're not abstract — they price injuries (a man who knocks out another's tooth pays 1/3 mina of silver), set wages, and protect widows. Notice the law against a builder whose house collapses and kills its owner: the builder is put to death. The stele stood in a public square so everyone could see the rules.",
        "Stele of Hammurabi (c. 1754 BCE) — the god scene and the laws",
        ["Ancient Mesopotamia", "Law"],
    ),
    "artw-terracotta-army-110": _entry(
        "Unknown (c. 210 BCE)",
        "Terracotta Army (c. 210 BCE)",
        "In 1974, farmers digging a well near Xi'an, China hit a buried army — 8,000 life-size clay soldiers, each with a unique face, guarding the tomb of China's first emperor, Qin Shi Huang. The figures were brightly painted when made, and the emperor's actual tomb — sealed with a river of mercury — remains unexcavated.",
        "Look at the faces: no two are alike — scholars believe the sculptors worked from individual real soldiers. Notice the height: generals are the tallest, archers the shortest, and their armor and hairstyles encode rank. The figures were mass-produced from a few body molds, but the heads, hands, and expressions were finished by hand. The pits were laid out like a real army — infantry, archers, chariots, and cavalry facing east toward the emperor's enemies.",
        "Terracotta Army (c. 210 BCE) — the unique faces and the battle formation",
        ["Ancient China", "Sculpture"],
    ),
    "artw-nike-of-samothrace-111": _entry(
        "Unknown (c. 190 BCE)",
        "Winged Victory of Samothrace (c. 190 BCE)",
        "A Greek marble statue of the goddess Nike, found headless and armless on the island of Samothrace in 1863, posed on the prow of a ship as if landing from the sky. The stone is carved so the front of the drapery is wet and clinging while the back is dry — a masterclass in suggesting wind and motion from frozen marble.",
        "Approach it from the left: the wing rises on the same diagonal as the ship's prow, and the whole statue leans forward against the wind. Look at the hem of the chiton — it's plastered to the body by imaginary sea spray, and the fabric's folds are carved in deep, shadow-catching grooves. The statue originally stood in a fountain basin with real water flowing over the ship's base, so the goddess appeared to rise from the waves.",
        "Winged Victory of Samothrace (c. 190 BCE) — the wet drapery from the left",
        ["Ancient Greek", "Sculpture"],
    ),
    "artw-venus-de-milo-112": _entry(
        "Alexandros of Antioch (c. 130 BCE)",
        "Venus de Milo (c. 130 BCE)",
        "The most famous armless woman in art — discovered in 1820 by a farmer on the Greek island of Milos, who found the arms separately and then, by one account, refused to sell them to the French, who took the statue anyway. Her missing arms are now the most speculated-about body parts in art history: she may have been holding an apple, a shield, or looking at herself in a mirror.",
        "Look at the twist: her hips face one way and her shoulders another — a contrapposto spiral that makes the marble feel alive even without arms. Then consider the arms themselves: a nearby fragment of a left hand holding an apple was found at the same site, and it may have been hers (the name 'Milo' means apple). The statue was originally painted, adorned, and standing in a niche with her arms intact — she only became a 'masterpiece of fragment' in the Louvre.",
        "Venus de Milo (c. 130 BCE) — the spiral stance and the apple fragment",
        ["Ancient Greek", "Sculpture"],
    ),
    "artw-laocoon-and-his-sons-113": _entry(
        "Agesander, Athenodoros & Polydorus (c. 40 BCE)",
        "Laocoön and His Sons (c. 40 BCE)",
        "A marble group showing the Trojan priest Laocoön and his sons being crushed by sea serpents — a punishment for warning Troy about the wooden horse. It was unearthed in Rome in 1506, and Michelangelo was among the first to see it; its expression of agony has been called the greatest sculpture of suffering ever made.",
        "Look at the composition: the three bodies form an X that pulls the eye from Laocoön's thrown-back head down his straining arm to the serpent's coils. Notice the psychology of the serpents — one bites the father, one attacks a son, so the father's agony is doubled by watching his children. Pliny the Elder described it as made 'from a single block,' and for 400 years nobody checked — then in the 20th century tests proved it was carved from seven separate blocks of marble.",
        "Laocoön and His Sons (c. 40 BCE) — the diagonal composition",
        ["Ancient Roman", "Sculpture"],
    ),
    "artw-discobolus-114": _entry(
        "Myron (c. 460 BCE)",
        "Discobolus (c. 460 BCE)",
        "The original bronze by Myron is lost — what survives is a Roman marble copy of the moment a discus thrower is coiled at the instant of release. It froze a motion so perfectly that the pose looks athletic today but was anatomically impossible for a real Greek athlete to hold.",
        "Look at the contradiction: the face is calm and blank while the body is at maximum tension — Greek artists called this 'frozen moment' idealization, not realism. The torso is twisted so far that a real spine would break; the sculptor distorted anatomy to make the shape of effort perfect. Notice the head: it turns back toward the discus, and the line from the eyes to the hand to the discus forms a single visual chord.",
        "Discobolus (c. 460 BCE) — the impossible twist",
        ["Ancient Greek", "Sculpture"],
    ),
    "artw-augustus-of-prima-porta-115": _entry(
        "Unknown (c. 20 CE)",
        "Augustus of Prima Porta (c. 20 CE)",
        "A full-length statue of the first Roman emperor, found in his wife Livia's villa at Prima Porta in 1863 — and every inch of it is propaganda. The cupid at his feet claims descent from Venus, and the breastplate shows a Parthian king returning Roman standards — a diplomatic victory Augustus had painted as a military one.",
        "Look at the breastplate first: the scene on it shows the Parthians handing back the Roman eagle standards lost in a previous defeat — the statue's way of turning a negotiated surrender into a triumph. Then look down at the cupid riding a dolphin at Augustus's feet: the dolphin = victory at sea (Actium), the cupid = descent from Venus. Notice the bare feet — a sign the emperor is being shown as a hero in the divine realm, not a politician in Rome.",
        "Augustus of Prima Porta (c. 20 CE) — the breastplate and the cupid",
        ["Ancient Roman", "Sculpture"],
    ),
    # ---------- Medieval ----------
    "artw-the-book-of-kells-116": _entry(
        "Unknown monks (c. 800 CE)",
        "The Book of Kells (c. 800 CE)",
        "A hand-painted gospel book made by Celtic monks around 800 CE — 680 pages of vellum, decorated with interlaced patterns so fine that some details are visible only under magnification, made with a quill pen and pigments including lapis lazuli imported from Afghanistan. No one knows exactly where it was made; it spent centuries at the monastery of Kells in Ireland, which gives it its name.",
        "Look at the Chi-Rho page (folio 34r): the first two letters of 'Christ' in Greek are expanded into a full-page knotwork masterpiece, and hidden inside the spirals are tiny animals, angels, and human faces — find the cats and mice chasing each other in the knotwork. Notice the colors: the blue came from lapis lazuli ground from a stone that traveled thousands of miles. The book was written by at least three scribes whose handwriting scholars can tell apart.",
        "The Book of Kells (c. 800 CE) — the Chi-Rho page and the hidden animals",
        ["Medieval", "Manuscript"],
    ),
    "artw-the-bayeux-tapestry-117": _entry(
        "Unknown (c. 1077)",
        "The Bayeux Tapestry (c. 1077)",
        "Not a tapestry but an embroidery — 68 meters of linen embroidered in wool, telling the story of the Norman conquest of England in 1066. It was probably commissioned by Bishop Odo, William the Conqueror's half-brother, and the only named woman in it is shown refusing to pay the man holding her hand — or, depending on the reading, agreeing to something else entirely.",
        "Walk the narrative in order: it starts with King Edward sending Harold to Normandy and ends with Harold's death by an arrow in the eye — the medieval world's most famous 'he saw it coming' detail. Look at the border: above and below the main story run animals, fables, and a naked man and woman in a scene that scholars still argue about. Notice the Latin captions: the embroidery is a 11th-century graphic novel with subtitles, and the deaths of real men are shown in the same flat style as the mythological beasts.",
        "The Bayeux Tapestry (c. 1077) — Harold's death and the border scenes",
        ["Medieval", "Embroidery"],
    ),
    # ---------- Renaissance & Baroque ----------
    "artw-the-oath-of-the-horatii-1784-118": _entry(
        "Jacques-Louis David (1784)",
        "The Oath of the Horatii (1784)",
        "David's painting of three Roman brothers swearing to fight for Rome — while the women behind them weep — was painted in Rome and became the banner of the French Revolution. The brothers' straight-armed salute was copied by real revolutionaries, and David later voted for the king's execution. The painting is often cited as the moment Neoclassicism turned political.",
        "Look at the geometry: the three brothers' arms form straight lines that echo the three columns behind them — human bodies built like architecture, which is the Neoclassical ideal. Then the contrast: the men are vertical, hard, and purposeful while the women are curved, soft, and collapsed in grief — the painting literally arranges the sexes into different shapes. The hands are the centerpiece: the father holds the swords and the sons' hands meet his in a single point of contact, the oath itself. David painted the scene in a Roman interior, and the lighting is theatrical, like a stage set — which is exactly what the revolution made of it.",
        "The Oath of the Horatii (1784) — the outstretched arms and the weeping women",
        ["Neoclassicism", "History Painting"],
    ),
    "artw-the-birth-of-venus-119": _entry(
        "Sandro Botticelli (1485)",
        "The Birth of Venus (1485)",
        "The first large-scale painting of a nude goddess since antiquity — Venus floats ashore on a scallop shell, blown by the wind gods, about to be draped by a waiting handmaiden. Botticelli painted her with an impossible anatomy: her neck is too long, her left shoulder slopes at an angle no shoulder can, and her body is subtly too elongated — all to make her look like a dream rather than a woman.",
        "Look at the shell first: scallops were the medieval badge of pilgrims, and Venus standing on one makes her a holy traveler. Then trace the pose — the modest hand covering the body is copied from ancient Roman statues of Venus Pudica, so the painting claims a classical lineage. Notice the wind gods are painted as a tangled knot of limbs and wings, while Venus herself is an unbroken vertical line — stillness at the center of motion.",
        "The Birth of Venus (1485) — the shell and the impossible shoulder",
        ["Renaissance", "Mythology"],
    ),
    "artw-the-vitruvian-man-120": _entry(
        "Leonardo da Vinci (c. 1490)",
        "The Vitruvian Man (c. 1490)",
        "Leonardo's drawing of a man inscribed in a circle and a square — the most famous illustration in the history of science — was his attempt to prove the Roman architect Vitruvius's claim that the human body's proportions are perfect. It exists because Leonardo was a perfectionist: the drawing shows sixteen poses layered on top of each other, arms and legs in two positions at once.",
        "Look at the geometry: the square and circle share a center point at the navel, and the man's outstretched arms touch the square's sides while his raised arms touch the circle. Leonardo's notes in his mirror-writing say the span of the outstretched arms equals the height — the 'Vitruvian' ratio. The drawing is done in ink with a metal stylus, and the head is drawn twice: once looking straight ahead, once turned, so the drawing is a study of motion, not just proportion.",
        "The Vitruvian Man (c. 1490) — the circle, the square, and the double pose",
        ["Renaissance", "Drawing"],
    ),
    "artw-david-1504-by-michelangelo-121": _entry(
        "Michelangelo (1504)",
        "David (1504)",
        "A 5.17-meter marble giant carved from a single block of flawed, 'unusable' Carrara marble that two earlier sculptors had abandoned — Michelangelo was 26 when he finished it. David stands not after his victory over Goliath, but before the fight: his sling is hidden behind his back, and his eyes are locked on something we can't see.",
        "Look at his hands: the right one is oversized — Michelangelo deliberately enlarged it, the hand that will kill, because the statue was made to be seen from 13 meters below, and the hands are the part the viewer sees first. Then the face: the brows are knit, the nostrils flared, the eyes turned sharply to the left — this is not calm, it's the moment of decision. The sling rests over his shoulder and down his back: the weapon is hidden, which is the point of the story.",
        "David (1504) — the oversized hand and the pre-battle face",
        ["Renaissance", "Sculpture"],
    ),
    "artw-pieta-1499-by-michelangelo-122": _entry(
        "Michelangelo (1499)",
        "Pietà (1499)",
        "The only work Michelangelo ever signed — he carved his name across the sash on Mary's chest after overhearing visitors credit it to another sculptor. He was 24. The statue shows Mary holding her adult son's body, and Michelangelo shrank her to a scale that makes the impossible possible: her lap is broad enough to hold a full-grown man.",
        "Look at Mary's age: she looks younger than her son — Michelangelo said virgins don't age, and he made her face that of a girl to match. Then the drapery: the folds of her robe are cut so deeply that light pools in them, and the cloth seems heavier than the marble it's made from. Notice the contrast: the body of Christ is smooth, slack, and anatomically exact — the veins in his arm visible — while Mary's robe is a mountain of cloth. And find the signature: 'MICHELANGELUS BONAROTUS FLORENTINUS FACIEBAT' on the sash.",
        "Pietà (1499) — the youth of Mary and the anatomy of Christ",
        ["Renaissance", "Sculpture"],
    ),
    "artw-the-sistine-chapel-ceiling-123": _entry(
        "Michelangelo (1512)",
        "The Sistine Chapel Ceiling (1512)",
        "Michelangelo spent four years on his back — actually, standing on scaffolding and craning his neck — painting 300 figures across 512 square meters of ceiling for Pope Julius II. He was a sculptor who resisted the commission, and the result is the most famous ceiling in the world: the story of Genesis from the Creation to the Flood, with the Creation of Adam's nearly-touching fingers at its center.",
        "Find the Creation of Adam first: Adam and God's fingers are a few millimeters apart, and the space between them has been called the most important gap in art — the moment before the spark. Then look at God's side: many art historians argue the shape of the cloak and the figures around God form the outline of a human brain, painted 300 years before the discovery that the brain is where the soul lives. Notice the sibyls and prophets at the corners: they're giants with the bodies of athletes and the faces of philosophers, painted larger than the biblical scenes above them.",
        "The Sistine Chapel Ceiling (1512) — the fingers of the Creation of Adam",
        ["Renaissance", "Fresco"],
    ),
    "artw-the-ambassadors-1533-124": _entry(
        "Hans Holbein the Younger (1533)",
        "The Ambassadors (1533)",
        "A double portrait of two French envoys with a hidden trick: a mysterious gray smear across the bottom of the painting that only becomes a human skull when viewed from the far right at a sharp angle. The painting is a meditation on vanity and death — the skull is an anamorphic memento mori, an 'you will die' hidden in plain sight.",
        "Stand to the far right of the painting (or tilt your screen) until the smear at the bottom resolves into a skull. Then look at the table between the two men: the globes, lute with a broken string, and books are all emblems of knowledge and harmony — and the broken lute string signals discord or death. Notice the crucifix half-hidden behind the green curtain in the top-left corner: Holbein buried the Christian message so deep that viewers miss it entirely.",
        "The Ambassadors (1533) — the anamorphic skull from the right",
        ["Northern Renaissance", "Portrait"],
    ),
    "artw-the-calling-of-saint-matthew-125": _entry(
        "Caravaggio (1600)",
        "The Calling of Saint Matthew (1600)",
        "Caravaggio's painting of the moment Christ calls the tax collector Matthew shows the divine arriving in a smoky Roman tavern — and the first man to respond is a young boy at the table who points at himself as if to say 'who, me?' The painting scandalized and thrilled Rome because its holy scene looks like an ordinary street corner.",
        "Look at the light: a diagonal beam enters from the right, following Christ's pointing hand — the divine is a shaft of light in a dark room, a style called tenebrism that Caravaggio invented. Then look at the tax collectors' faces: they're not biblical types but real Roman working men, and the boy at the end of the table gestures at himself in surprise — the joke of the painting is that the audience does the same. Christ's hand is borrowed from Michelangelo's Creation of Adam, but here the fingers are close enough to touch.",
        "The Calling of Saint Matthew (1600) — the beam of light and the boy's gesture",
        ["Baroque", "Oil Painting"],
    ),
    "artw-judith-beheading-holofernes-126": _entry(
        "Artemisia Gentileschi (c. 1612)",
        "Judith Beheading Holofernes (c. 1612)",
        "Painted by the first woman artist to make violence and power her subject — Artemisia Gentileschi, who was raped at 17 by her painting tutor and testified in a trial that used thumbscrews to check her story. Her Judith is not a delicate biblical heroine but a determined woman who grips the general's head by the hair while her maidservant holds him down — the blood sprays in arcs.",
        "Look at the mechanics of the violence: this is a working murder, not a symbol — Judith's arms are fully extended, her sleeves are pushed up, and the maidservant's arm is pressed flat against Holofernes's chest to hold him down. The blood arcs from his neck onto the white sheet with a precision that Gentileschi painted after studying real anatomy. Compare her Judith with the many male artists' versions: their Judiths look away, hers looks the victim in the face — art historians read it as the painter's own revenge.",
        "Judith Beheading Holofernes (c. 1612) — the working mechanics of the kill",
        ["Baroque", "Oil Painting"],
    ),
    "artw-the-anatomy-lesson-of-127": _entry(
        "Rembrandt (1632)",
        "The Anatomy Lesson of Dr. Nicolaes Tulp (1632)",
        "Rembrandt was 25 when he painted this group portrait of Amsterdam's surgeons' guild — and he broke the rules of group portraiture by turning a line-up of posed faces into a real event: eight surgeons watching Dr. Tulp dissect the arm of a hanged criminal. One detail is anatomically wrong — the tendons Tulp is clamping belong to the wrong muscles — because Rembrandt painted from a book, not a real dissection.",
        "Look at the corpse's face: it's half in shadow, half in light — and it's the only face in the painting not looking at the dissection, which scholars read as Rembrandt's quiet comment on death watching the living. Then follow the composition: the surgeons form a pyramid leading the eye to Tulp's hands. The body belonged to Aris Kindt, a convicted thief hanged that morning — the guild bought the right to dissect him, and his body is shown with a dignity the real criminal was denied.",
        "The Anatomy Lesson of Dr. Nicolaes Tulp (1632) — the wrong tendons",
        ["Dutch Golden Age", "Group Portrait"],
    ),
    "artw-the-milkmaid-1658-128": _entry(
        "Johannes Vermeer (c. 1658)",
        "The Milkmaid (c. 1658)",
        "Vermeer's painting of a kitchen maid pouring milk is one of the most admired images in art — and it's a lie of the best kind: the woman was a servant, but Vermeer gave her the stillness and dignity of a saint. He painted the bread's crust so carefully that the dots of color can be seen up close as individual specks of pigment.",
        "Look at the milk first: the thin stream is frozen mid-pour, and the jug is painted so the light hits the glaze. Then the wall behind her: it's blank white, but Vermeer added a subtle shadow of the maid's figure onto it — a detail easy to miss. Notice the Delftware tile at the baseboard (a Cupid figure) and the foot warmer on the floor — small objects that scholars read as symbols of love and longing. The bread on the table has been called the best-painted bread in art history.",
        "The Milkmaid (c. 1658) — the pouring milk and the bread crust",
        ["Dutch Golden Age", "Genre Painting"],
    ),
    "artw-the-rokeby-venus-129": _entry(
        "Diego Velázquez (c. 1647)",
        "The Rokeby Venus (c. 1647)",
        "The only surviving female nude by Spain's greatest court painter — Venus shown from behind, admiring herself in a mirror held by Cupid. It was considered so shocking in Victorian Britain that in 1914 a suffragette named Mary Richardson slashed it seven times with a meat cleaver — she said she was protesting the arrest of Emmeline Pankhurst and attacking 'the most beautiful woman in mythology' as revenge.",
        "Look at the face in the mirror: it's slightly blurred compared to the body — Velázquez painted the reflection as if the mirror is a soft, imperfect surface. Then the back: the spine's curve is one long unbroken S, and the skin tones are built from hundreds of tiny strokes of pale pink, gray, and white. Notice Cupid: he holds the mirror not for Venus's vanity but so she can see herself as the viewer does — the painting is about being looked at.",
        "The Rokeby Venus (c. 1647) — the blurred mirror face",
        ["Baroque", "Nude"],
    ),
    "artw-the-ecstasy-of-saint-teresa-130": _entry(
        "Gian Lorenzo Bernini (1652)",
        "The Ecstasy of Saint Teresa (1652)",
        "Bernini's marble group shows the Spanish nun Teresa in a swoon as an angel prepares to pierce her heart with a golden arrow — her face is not pain but the closest thing sculpture has ever come to filming an orgasm. It's set inside a chapel designed like a theater, with hidden windows pouring real light onto the group and gilded rays carved behind it.",
        "Look at the marble: Teresa's habit is carved in deep, wind-swept folds that ripple like water, while the angel's garments are smooth — the contrast makes the divine and human feel like different substances. Then notice the theatrical machinery: the chapel's hidden window above the group casts real light down the golden rays, and the side 'balconies' hold spectators carved in marble — the audience is part of the performance. Teresa's own words describe the moment as 'so sweet that one could die of it' — Bernini turned her memoir into a stage.",
        "The Ecstasy of Saint Teresa (1652) — the wind-swept marble and the golden rays",
        ["Baroque", "Sculpture"],
    ),
    # ---------- 18th & 19th Century ----------
    "artw-the-blue-boy-1770-131": _entry(
        "Thomas Gainsborough (1770)",
        "The Blue Boy (1770)",
        "Gainsborough's portrait of a boy in a blue satin suit is the most famous British painting of the 18th century — and it was a deliberate rebellion: the official art establishment said blue should never be used for the main figure (it was for skies), so Gainsborough painted the whole boy in blue on purpose. The boy was not an aristocrat — he was the son of a hardware dealer.",
        "Look at the brushwork: up close the satin is a scribble of loose strokes, but from a distance it resolves into the most convincing blue fabric ever painted — Gainsborough worked with long brushes and a candlelit mirror. Then the pose: the boy stands like Van Dyck's aristocratic portraits of a century earlier, but he's a middle-class kid in a fancy costume his family bought for the occasion. Notice his legs: they're delicate and slightly splayed — a child standing stiffly for a painter he didn't know.",
        "The Blue Boy (1770) — the satin brushwork up close",
        ["18th Century", "Portrait"],
    ),
    "artw-an-experiment-on-a-bird-132": _entry(
        "Joseph Wright of Derby (1768)",
        "An Experiment on a Bird in the Air Pump (1768)",
        "A candlelit room where a scientist demonstrates the vacuum pump by removing the air from a glass globe containing a white cockatoo — the bird is about to suffocate while a little girl cries and her father reassures her. It's the greatest painting of the Enlightenment: science performed as a family drama, with the light of reason literally illuminating the faces.",
        "Look at the light first: the candle is hidden behind the glass globe, and every face in the room is lit in a different way — the children lit brightly, the scientist in profile, the old man in shadow. Then the bird: it's a cockatoo, a precious pet, not a lab animal — Wright chose it to make the sacrifice feel personal. The man at the back right draws the curtain, the glass is about to break — the painting freezes the single most suspenseful second in 18th-century art.",
        "An Experiment on a Bird in the Air Pump (1768) — the hidden candle",
        ["18th Century", "Enlightenment"],
    ),
    "artw-the-hay-wain-1821-133": _entry(
        "John Constable (1821)",
        "The Hay Wain (1821)",
        "Constable's painting of a horse-drawn hay cart crossing the River Stour in Suffolk — his home county, which he painted from memory, not on the spot, saying 'I should paint my own places best.' When it was shown in Paris in 1824 it electrified French painters, who had never seen light painted this way, and is credited with helping start French Impressionism.",
        "Look at the water: the reflections are painted with loose, broken strokes of color — not blended — and the house on the left was the real cottage of a farm worker named Willy Lott, who supposedly never left it for more than a few days in 80 years. Then the sky: Constable called the sky the 'chief organ of sentiment,' and this one is full of moving clouds — he painted clouds from life his whole life. The cart is crossing to harvest hay, and the dog on the bank and the two figures are the only living things that notice the viewer.",
        "The Hay Wain (1821) — the broken reflections and the sky",
        ["Romanticism", "Landscape"],
    ),
    "artw-the-slave-ship-1840-134": _entry(
        "J.M.W. Turner (1840)",
        "The Slave Ship (1840)",
        "Turner's painting of a slave ship in a storm — with human limbs visible among the fish and waves — was based on a real 1783 atrocity in which the captain of the Zong threw 133 enslaved Africans overboard to collect insurance. Turner exhibited it with a poem he wrote himself: 'Hope, Hope, fallacious Hope!' — and the painting is a storm of red, gold, and black with the cruelty hidden in the beauty.",
        "Look for the limbs first: in the lower-right water, chained arms and legs rise from the waves, and fish circle them — the horror is painted as part of the seascape's beauty. Then the sky: Turner built the sunset from reds and golds that get more violent as you stare, and the storm's heart is a single white-hot patch. The ship itself is tiny, sinking into the distance, with its sails barely visible — the crime is the point, and the ship is almost an afterthought.",
        "The Slave Ship (1840) — the limbs in the water",
        ["Romanticism", "History Painting"],
    ),
    "artw-wanderer-above-the-sea-of-fog-135": _entry(
        "Caspar David Friedrich (1818)",
        "Wanderer Above the Sea of Fog (1818)",
        "A man in a dark green coat stands on a rocky peak, back to the viewer, looking over a sea of fog where other peaks emerge like islands. It's the definitive image of the Romantic sublime — nature vast and indifferent, the individual small but facing it — and no one knows who the man is. Some say it's a self-portrait; some say it's a portrait of a Saxon forestry official.",
        "Look at what the painting refuses to show: the man's face. His back is the whole subject — the viewer is invited to stand in his position and feel the sublime for themselves. Then the fog: the peaks below look like floating islands, and Friedrich painted them from sketches he made in the Elbe sandstone mountains. Notice the composition's contradiction: the man is tiny against the peaks but placed exactly at the center, so he's both dwarfed and triumphant — the painting's whole meaning is in that tension.",
        "Wanderer Above the Sea of Fog (1818) — the back of the wanderer",
        ["Romanticism", "Landscape"],
    ),
    "artw-the-gleaners-1857-136": _entry(
        "Jean-François Millet (1857)",
        "The Gleaners (1857)",
        "Three peasant women bend to pick leftover grain after the harvest — a scene so common nobody had bothered to paint it before. Millet made the rural poor the subject of a large-scale painting, and wealthy Parisians read it as a socialist threat; the women's bent postures were compared to praying. It was bought by a rich American before the French state could decide whether it was dangerous.",
        "Look at the three women: they form a group of three repeated postures — the first is nearly upright, the second bent lower, the third almost doubled over — a progression that makes the labor feel eternal. Then the horizon: the abundant harvest behind them is painted in gold, and the harvesters' wealth stands in the distance while the gleaners' work is in the foreground. Millet's grandmother once scolded him for drawing naked figures, and here the dignity is in the work, not the bodies.",
        "The Gleaners (1857) — the three bent postures and the golden harvest",
        ["Realism", "Oil Painting"],
    ),
    "artw-ophelia-1852-137": _entry(
        "John Everett Millais (1852)",
        "Ophelia (1852)",
        "Millais painted the drowned Ophelia from Shakespeare's Hamlet over five months, sitting in a bathtub in his studio while the model, Elizabeth Siddal, lay in the water heated by lamps underneath — the lamps went out one day and she caught a severe chill that her doctor said nearly killed her. The flowers in the painting are painted with botanical precision, and every species has a symbolic meaning.",
        "Look at the flowers: each one is a symbol from the play — the willow for forsaken love, the nettles for pain, the daisies for innocence, and the poppies for death. Then the face: Ophelia's mouth is slightly open, and her hands are raised in a gesture that was once read as drowning but is now read as singing — Shakespeare's text says she drowned singing. The paint is so detailed that the leaves were painted from real specimens collected in the countryside, and the water's surface is built from hundreds of tiny strokes.",
        "Ophelia (1852) — the symbolic flowers and the open mouth",
        ["Pre-Raphaelite", "Oil Painting"],
    ),
    "artw-a-bar-at-the-folies-138": _entry(
        "Édouard Manet (1882)",
        "A Bar at the Folies-Bergère (1882)",
        "Manet's last major painting shows a barmaid at the Folies-Bergère, a real Parisian nightclub — and it contains one of the most debated perspective puzzles in art: the reflection in the mirror behind her does not match what's in front of it. The barmaid was a real woman named Suzon, who worked at the club and later confirmed she posed for the painting.",
        "Look at the mirror: the barmaid is shown frontally, but her reflection appears off to the right, and the man she appears to be serving in the reflection has no corresponding figure in the foreground — art historians still argue whether Manet made a mistake or painted a deliberate meditation on the impossibility of seeing the whole truth. Then the bottles: the champagne, beer, and liqueurs on the marble counter are painted with the precision of a still life, and the brand names are legible — advertising entering high art for the first time. Suzon stands with her hands on the counter, tired and unreadable.",
        "A Bar at the Folies-Bergère (1882) — the impossible mirror",
        ["Impressionism", "Oil Painting"],
    ),
    "artw-little-dancer-of-fourteen-139": _entry(
        "Edgar Degas (1881)",
        "Little Dancer of Fourteen Years (1881)",
        "Degas's wax sculpture of a teenage ballet dancer was the scandal of the 1881 Impressionist exhibition — she wore a real tutu, a real hair ribbon, and real ballet slippers, and critics called her 'rat-faced' and 'a flower of the pavement.' She was modeled on a real girl named Marie van Goethem, who danced at the Paris Opera and then vanished from the records.",
        "Look at the materials: the figure is bronze (cast after Degas's death from his wax original), but the tutu is real fabric, the ribbon real, and the slippers real — the combination of sculpture and costume was shocking in 1881. Then the pose: her chin is up, her hands clasped behind her back, her feet in the ballet's fourth position — a working pose, not a pretty one. The original wax figure was dressed by Marie herself, and Degas showed her with a dancer's exhausted posture that made wealthy viewers uncomfortable.",
        "Little Dancer of Fourteen Years (1881) — the real tutu and the working pose",
        ["Impressionism", "Sculpture"],
    ),
    "artw-the-dance-at-le-moulin-140": _entry(
        "Pierre-Auguste Renoir (1876)",
        "Dance at Le Moulin de la Galette (1876)",
        "Renoir's enormous painting of a Sunday afternoon dance at a real Parisian windmill-turned-café — friends, workers, and dancers in dappled sunlight filtered through the trees. He painted it on the spot over several weeks, and his friends modeled for him: the dancer in the blue striped dress was Estelle, a Montmartre laundress, and the woman in the foreground was a model named Margot.",
        "Look at the light: the sun comes through the acacia trees and lands as patches of yellow and blue on the faces and clothes — Renoir painted light as color, not as brightness, a defining Impressionist move. Then the crowd: this is a working-class party, not a ballroom — the men in straw hats and cheap suits, the women in everyday dresses. The couple in the foreground, her head tilted back, are frozen mid-motion, and the whole canvas vibrates with the sense that the dance has been going on for hours and will go on for hours more.",
        "Dance at Le Moulin de la Galette (1876) — the dappled light on faces",
        ["Impressionism", "Oil Painting"],
    ),
    "artw-the-card-players-141": _entry(
        "Paul Cézanne (c. 1894)",
        "The Card Players (c. 1894)",
        "Cézanne painted five versions of two Provençal farmhands playing cards — the paintings were bought by museums around the world, and one sold for over $250 million in 2011, then the most expensive painting ever sold. The men are real peasants from Aix-en-Provence who were paid to sit, and Cézanne made them as monumental as ancient statues.",
        "Look at the stillness: the two men barely move, their eyes fixed on their cards, and Cézanne built the whole composition from the bottle's central axis — the composition is as balanced as a still life, which is what it is: a still life with people instead of fruit. Then the hands: they're simplified to almost geometric shapes, and the tabletop tilts upward toward the viewer while the men's shoulders slope down — Cézanne's famous 'flattened' space that Cubism would grow from. The men are identical in their concentration, and the painting's drama is that nothing happens.",
        "The Card Players (c. 1894) — the bottle axis and the tilted table",
        ["Post-Impressionism", "Oil Painting"],
    ),
    "artw-mont-sainte-victoire-142": _entry(
        "Paul Cézanne (c. 1890)",
        "Mont Sainte-Victoire (c. 1890)",
        "Cézanne painted the same mountain outside Aix-en-Provence around 60 times — from the same window, in every season and every light. He kept painting it even as the railway and the suburbs crept into the view, and the late paintings dissolve the mountain into planes of color so abstract that they look like Cubism before Cubism existed.",
        "Compare the mountain's shape across the versions in your mind: early on it's a solid blue-green mass, but in the late paintings it breaks into geometric facets — Cézanne said he wanted to 'make of Impressionism something solid and durable, like the art of the museums.' Look at how the foreground fields and the mountain are built from parallel brushstrokes of the same direction. He painted it from a spot near his studio where he could see the mountain between two trees, and he claimed the mountain was his 'obsession.'",
        "Mont Sainte-Victoire (c. 1890) — the faceted late style",
        ["Post-Impressionism", "Landscape"],
    ),
    "artw-the-potato-eaters-143": _entry(
        "Vincent van Gogh (1885)",
        "The Potato Eaters (1885)",
        "Van Gogh's first great painting shows a family of five peasants eating potatoes by the light of a single hanging lamp — their faces the color of the potatoes they're eating. He painted it in Nuenen, in the Netherlands, using real laborers as models, and he wrote that he wanted to show that they 'have earned their meal with these hands digging in the earth.'",
        "Look at the faces and hands: they're painted in muddy browns and grays, deliberately the color of the earth — van Gogh said he wanted the people to match the food. Then the light: the single lamp casts a weak yellow glow, and everything else is swallowed in shadow, making the meal a small circle of warmth in the dark. The perspective is famously awkward — the table tilts, the bodies crowd the frame — because van Gogh was still learning, and the awkwardness is what makes the family feel real and poor. He called it his best painting of the period.",
        "The Potato Eaters (1885) — the earth-colored faces",
        ["Post-Impressionism", "Oil Painting"],
    ),
    "artw-the-bedroom-1888-144": _entry(
        "Vincent van Gogh (1888)",
        "The Bedroom (1888)",
        "Van Gogh painted his bedroom in Arles — the yellow room with the narrow bed he rented in the 'Yellow House' he hoped would become an artists' colony. He painted three versions, and the walls, floor, and furniture tilt toward each other in a way that makes the room feel like a closed box — he wrote that 'the walls are pale violet, the floor is of red tiles.'",
        "Look at the perspective: the floor, the bed, and the walls all lean into each other, and the room has no exit visible in the frame — the door on the right is painted nearly closed. Van Gogh wrote that he wanted the room to express 'absolute rest' with the colors, but the tilted planes make it feel slightly unhinged. Then the details: the two chairs, the two pillows, the mirror, the clothes on pegs, the portrait of a friend above the bed — the room of a man preparing a home for someone who never came.",
        "The Bedroom (1888) — the tilting perspective and the two pillows",
        ["Post-Impressionism", "Oil Painting"],
    ),
    "artw-cafe-terrace-at-night-145": _entry(
        "Vincent van Gogh (1888)",
        "Café Terrace at Night (1888)",
        "The first painting in which van Gogh painted a night sky without black — the sky is deep blue with stars surrounded by halos of color. It shows the café terrace in the Place du Forum in Arles, which still exists today with its yellow awning, and the cobblestones, tables, and patrons are painted from life after dinner, in the dark, by lamplight.",
        "Look at the sky first: the stars are painted as circles of yellow and white with radiating halos — van Gogh told his brother that 'the sky is aquamarine, the walls are violet.' Then the contrast: the café glows warm yellow under its awning while the street beyond is cool blue — the painting is a study of how artificial light warms the night. The terrace's perspective lines vanish at a point near the center, and the little figure in the doorway and the dog near the tables make the empty street feel inhabited.",
        "Café Terrace at Night (1888) — the haloed stars and the warm yellow",
        ["Post-Impressionism", "Oil Painting"],
    ),
    "artw-the-thinker-1904-146": _entry(
        "Auguste Rodin (1904)",
        "The Thinker (1904)",
        "Rodin's famous figure was originally meant to be the poet Dante at the top of a giant door called The Gates of Hell — the Thinker is a detail from that door, blown up into a freestanding sculpture. He sits with his chin on his hand, naked, and Rodin said the pose came from Michelangelo's statue of Lorenzo de' Medici, whom Michelangelo depicted in the same thinking posture.",
        "Look at the pose: the elbow rests on the opposite knee, the hand cups the chin, and the whole body is coiled — every muscle is engaged even though the figure is motionless. Then the scale: the original was about 70 cm, but the enlarged versions are nearly 2 meters — Rodin later said the Thinker 'thinks not only with his brain, with his knitted brow, his distended nostrils and compressed lips, but with every muscle of his arms, back and legs.' Notice the feet: they grip the rock as if the thinking is a physical effort.",
        "The Thinker (1904) — the coiled, every-muscle pose",
        ["Modern Sculpture", "Bronze"],
    ),
    "artw-the-kiss-1889-rodin-147": _entry(
        "Auguste Rodin (1889)",
        "The Kiss (1889)",
        "Rodin's marble lovers — Paolo and Francesca from Dante's Inferno, who fell in love reading about love and were killed for it — shows the exact moment before the first kiss. The marble was so expensive that Rodin sold the rights to have the full-size version carved in marble while he kept the plaster original, and he never allowed the pair to actually kiss: their lips hover a millimeter apart forever.",
        "Look at the lips: they almost touch but never do — Rodin froze the moment of maximum desire, not its fulfillment, which is why the sculpture feels more erotic than any completed kiss. Then the marble: the woman's skin is polished to a high gloss while the rock beneath and behind them is left rough — Rodin let the untouched stone act as the lovers' rocky cave. The pose was inspired by the figures' bodies curving toward each other like the parentheses of the composition, and Paolo's hand rests on the rock behind her rather than on her body — restraint as desire.",
        "The Kiss (1889) — the almost-touching lips",
        ["Modern Sculpture", "Marble"],
    ),
    "artw-the-sick-child-1886-148": _entry(
        "Edvard Munch (1886)",
        "The Sick Child (1886)",
        "Munch painted his sister Sophie dying of tuberculosis when he was 15 — the painting of her deathbed haunted him so much that he repainted it six times over 40 years, saying 'my art is rooted in this.' The painting's surface looks scraped and scarred, as if the grief was physically dragged across the canvas, and the girl's face is barely a suggestion.",
        "Look at the painting's surface: the brushwork is violent — long, scraped strokes that Munch made by dragging a palette knife through wet paint, so the canvas looks wounded. Then the girl's face: it's almost dissolved into the pillow, barely painted, while the mother's head is bent in shadow — the dying child is less solid than the grief around her. Munch wrote that he painted it from a memory of his sister's room: 'the window was open, the curtain moving — the light fell on the bed.' He exhibited it as 'a masterpiece' and critics called it a failure; he called it 'a breakthrough.'",
        "The Sick Child (1886) — the scraped surface and the dissolved face",
        ["Expressionism", "Oil Painting"],
    ),
    "artw-madonna-1894-149": _entry(
        "Edvard Munch (1894)",
        "Madonna (1894)",
        "Munch's 'Madonna' is no Christian virgin — it's a woman in the middle of a sexual trance, eyes half closed, framed by a dark halo and red waves. Munch wrote about the painting that 'the pause that the whole world stops for' was its subject, and he exhibited it with a printed frame of sperm and a fetus — a church icon redesigned as a fertility goddess.",
        "Look at the pose: she stands in a shallow S-curve, head tilted back, eyes closed, hair flowing — Munch said it was 'the pause when the whole world stops in its path,' describing the moment of conception. Then the frame: in the versions Munch printed, the border shows a writhing sperm and a small fetus — the sacred frame of a religious icon remade for biology. The halo around her head is red, like a blood moon, and the dark background makes her body the only light — she's painted as both saint and body.",
        "Madonna (1894) — the closed eyes and the red halo",
        ["Expressionism", "Oil Painting"],
    ),
    "artw-the-birth-of-venus-cabanel-150": _entry(
        "Alexandre Cabanel (1863)",
        "The Birth of Venus (1863)",
        "Cabanel's Venus floats on the waves in a languid pose, and Napoleon III bought it on sight — it became the official French taste against which the Impressionists rebelled. When the young painters' works were rejected from the 1863 Salon, Napoleon III ordered a 'Salon des Refusés' — the rejected works' show — and the battle lines of modern art were drawn. Cabanel's Venus, meanwhile, was the Salon's biggest hit.",
        "Look at the pose: Venus reclines on the water, one arm above her head — a pose of total passivity that the Salon adored and the Impressionists would mock. Then the details that made it a hit: the foam-flecked toes, the putti floating above, the perfected, polished skin. The painting is the definition of academic art — and knowing that makes it more interesting: the same year, Manet's Déjeuner sur l'herbe was rejected, and the contrast between Cabanel's smooth Venus and Manet's flat, real women is the birth certificate of modern art.",
        "The Birth of Venus (1863) — the academic pose and the Salon context",
        ["Academic", "Mythology"],
    ),
    "artw-the-bathers-151": _entry(
        "Pierre-Auguste Renoir (1887)",
        "The Bathers (1887)",
        "Renoir painted The Bathers after a trip to Italy where he decided his Impressionist style was 'too thin' — he wanted to make his figures solid, like Raphael's. The result is his most classical painting: four women by a stream, their bodies plump and rounded, and he repainted the legs 'endlessly,' he said, to get them right.",
        "Look at the bodies: they're not thin or idealized — they're real women with soft stomachs and heavy thighs, painted in a warm rose palette that makes them glow. Then the setting: the stream, the rocks, and the leaves are painted with the broken Impressionist brush, but the women are built with smooth, blended strokes — the painting is Renoir's attempt to marry Impressionist color to classical form. He worked on it for over a year, and the woman at the center was modeled by Aline Charigot, the laundress he would marry.",
        "The Bathers (1887) — the solid, rounded bodies",
        ["Impressionism", "Oil Painting"],
    ),
    "artw-the-angelus-1859-152": _entry(
        "Jean-François Millet (1859)",
        "The Angelus (1859)",
        "Two peasants stand in a field at dusk, heads bowed in prayer at the sound of the church bell — the Angelus. The painting became the most reproduced image in France, and when the artist Dalí examined it with X-rays in the 1960s he found something under the paint that confirmed his suspicion: the 'wheelbarrow' at the couple's feet was originally a small coffin, and the couple were originally praying over a dead child.",
        "Look at the wheelbarrow and the pitchfork first: Dalí had the painting X-rayed and found the ghost of a small coffin beneath the wheelbarrow — the couple may originally have been painted at a graveside, and Millet (or his dealer) painted over it. Then the postures: both figures bow at the same angle, the man's cap in his hands, and the light of the setting sun throws their shadows forward like sundials. The potato sack between them and the empty field make the prayer feel like the only sound in the world.",
        "The Angelus (1859) — the coffin under the wheelbarrow",
        ["Realism", "Oil Painting"],
    ),
    "artw-the-execution-of-lady-jane-153": _entry(
        "Paul Delaroche (1833)",
        "The Execution of Lady Jane Grey (1833)",
        "Delaroche's painting shows the 16-year-old Lady Jane Grey, queen of England for nine days, being guided to the block in the Tower of London — and it's history painted as a private tragedy, not a public event. The painting was so popular that Delaroche's reproductions made him rich, and for decades it was the most reproduced painting in England.",
        "Look at the young queen's face: her eyes are covered by a blindfold, and her hands are reaching forward, searching for the block — she is guided by a man in white who steadies her. Then the details: the straw beneath the block, the executioner's axe leaning against the wall, the ladies-in-waiting collapsing in grief at the right. Delaroche painted the scene as a quiet, terrified moment rather than a spectacle — no crowd, no anger, just a girl being helped toward her own death. The painting's accuracy is debatable (the Tower's setting is invented), but its emotion is unforgettable.",
        "The Execution of Lady Jane Grey (1833) — the blindfolded hands",
        ["Romanticism", "History Painting"],
    ),
    "artw-the-balcony-1869-by-manet-154": _entry(
        "Édouard Manet (1869)",
        "The Balcony (1869)",
        "Manet's The Balcony shows three figures on a Paris balcony — the painter Berthe Morisot, a violinist named Fanny Claus, and the landscape painter Antoine Guillemet — with a fourth, the servant, half-hidden in the shadow behind them. The critic who saw it at the 1869 Salon wrote that the three figures looked like 'three marionettes' — and that was the point: Manet painted modern people as strangers to each other.",
        "Look at the faces: none of the three looks at the others — each stares outward in a different direction, and the servant behind is only half a face in the dark. The painting is Manet's portrait of modern loneliness: people together without connection. Then the details: the green railings, the fan in Berthe Morisot's hand, the gloves Fanny Claus holds — the props of Parisian leisure painted as flat, bright shapes. The man at the right is Antoine Guillemet, the painter who would later help introduce Manet's work to the Salon.",
        "The Balcony (1869) — the three unconnected gazes",
        ["Impressionism", "Oil Painting"],
    ),
}


def main() -> int:
    data = json.loads(PATH.read_text(encoding="utf-8"))
    existing = {t["id"] for t in data}
    dup = [i for i in NEW if i in existing]
    if dup:
        print(f"ERROR: ids already exist: {dup}")
        return 1

    # non-painting subtypes (schema allows Painting/Sculpture/Photograph/Installation)
    SUBTYPES = {
        "artw-venus-of-willendorf-105": "Sculpture",
        "artw-bust-of-nefertiti-107": "Sculpture",
        "artw-mask-of-tutankhamun-108": "Sculpture",
        "artw-stele-of-hammurabi-109": "Sculpture",
        "artw-terracotta-army-110": "Sculpture",
        "artw-nike-of-samothrace-111": "Sculpture",
        "artw-venus-de-milo-112": "Sculpture",
        "artw-laocoon-and-his-sons-113": "Sculpture",
        "artw-discobolus-114": "Sculpture",
        "artw-augustus-of-prima-porta-115": "Sculpture",
        "artw-the-book-of-kells-116": "Manuscript",
        "artw-the-bayeux-tapestry-117": "Textile",
        "artw-david-1504-by-michelangelo-121": "Sculpture",
        "artw-pieta-1499-by-michelangelo-122": "Sculpture",
        "artw-the-ecstasy-of-saint-teresa-130": "Sculpture",
        "artw-little-dancer-of-fourteen-139": "Sculpture",
        "artw-the-thinker-1904-146": "Sculpture",
        "artw-the-kiss-1889-rodin-147": "Sculpture",
    }

    added = 0
    for tid, spec in NEW.items():
        entry = {
            "id": tid,
            "categoryId": "ARTWORKS",
            "subtype": SUBTYPES.get(tid, "Painting"),
            "name": spec["name"],
            "teaser": _trim(spec["teaser"]),
            "imageUrl": "",
            "byline": spec["byline"],
            "exploreAction": {
                "verb": "Look at",
                "targetName": _trim(spec["targetName"]),
                "durationMinutes": 8,
                "instruction": _trim(spec["instruction"]),
            },
            "tags": spec["tags"],
            "tier": 2,
        }
        data.append(entry)
        added += 1

    PATH.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"added {added} entries (total {len(data)})")
    return 0


if __name__ == "__main__":
    sys.exit(main())
