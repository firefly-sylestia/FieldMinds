#!/usr/bin/env python3
"""Batch 6: add 50 new handcrafted artworks to artworks.json (ids 355-404).

Renaissance masters (Leonardo, Raphael, Michelangelo, Botticelli, Bruegel),
Dutch Golden Age (Rembrandt, Vermeer, Fabritius), American art (Cole, Homer,
Eakins, Durand), Caravaggio & Gentileschi, Sargent, Gauguin, Hockney.
All names verified unique against the existing 306 entries. Real fun facts,
handcrafted teasers and quality-bar instructions.
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
    # ---------- Dutch Golden Age ----------
    "artw-the-goldfinch-355": _entry(
        "Carel Fabritius (1654)",
        "The Goldfinch (1654)",
        "Fabritius's tiny painting of a chained goldfinch perched on its feeder box — a masterpiece of Dutch realism that became world-famous in 2013 when Donna Tartt named her Pulitzer-winning novel after it. The painting is only 33 by 23 cm, and the chain and the little bird are painted with such tenderness that it has been called the most beloved small painting in the world. Fabritius, Vermeer's teacher, died months later in the Delft gunpowder explosion that destroyed his studio and most of his work.",
        "Look at the bird first: it is chained by its leg to the feeder — a pet, caught forever — and its head is turned slightly, watching you. Then the wall behind it: the pale plaster is painted in a few loose strokes, with the bird's shadow falling on it, and the whole scene is lit from the left with a warmth that makes the tiny canvas feel like a room. Fabritius was the most promising painter of his generation, a pupil of Rembrandt and the teacher of Vermeer; he died in the Delft gunpowder explosion of 1654, and only about a dozen of his paintings survive. The Goldfinch is in the Mauritshuis, The Hague — the museum that also owns Girl with a Pearl Earring.",
        "The Goldfinch (1654) — the chained bird and the loose wall",
        ["Dutch Golden Age", "Animal Painting"],
    ),
    "artw-the-love-letter-356": _entry(
        "Johannes Vermeer (c. 1669)",
        "The Love Letter (c. 1669)",
        "Vermeer's painting of a maid handing a letter to her seated mistress — with the whole scene seen through a doorway, as if we are spying on them from another room. The foreground is dark and out of focus, with a crumpled rug, a broom, and a lute on the floor, and through the door the two women share a knowing look — the letter is a love letter, and the music on the wall hints at the heart's tune.",
        "Look through the door first: the foreground — the broom, the crumpled rug, the slippers — is dark and slightly out of focus, while the two women beyond are sharp and brightly lit, so you are the third person in the house, watching through the doorway. Then the details: the maid has a knowing half-smile, the mistress looks up mid-reading, and on the wall behind them hangs a landscape with a storm and a lute — the lute, in Dutch painting, is the symbol of love. The map on the wall and the music hint that this letter travels far and speaks to the heart. Vermeer painted this in his late period, and it hangs in the Rijksmuseum, Amsterdam.",
        "The Love Letter (c. 1669) — the letter through the doorway",
        ["Dutch Golden Age", "Genre Painting"],
    ),
    "artw-the-art-of-painting-357": _entry(
        "Johannes Vermeer (c. 1666)",
        "The Art of Painting (c. 1666)",
        "Vermeer's largest and most complex painting: an artist sits with his back to us, painting a model dressed as Clio, the muse of history, while a chandelier, a map, and a curtain frame the scene. The artist is believed to be Vermeer himself, and the painting is his manifesto — he kept it in his studio and refused to sell it, even when he was drowning in debt.",
        "Look at the artist first: he sits with his back to us, painting the model — and the model is Clio, the muse of history, with her laurel wreath, her trumpet, and her book. Then the room: the chandelier above is painted with a precision that makes it gleam, the map of the Netherlands hangs on the back wall, and the curtain in the foreground — pulled back by a trick of perspective — invites you in. Vermeer refused to sell this painting, keeping it in his studio even when his family was in debt; it is the only work of his where the artist is shown at work. It hangs in the Kunsthistorisches Museum, Vienna.",
        "The Art of Painting (c. 1666) — the artist painting the muse",
        ["Dutch Golden Age", "Self-Portrait"],
    ),
    "artw-the-geographer-358": _entry(
        "Johannes Vermeer (c. 1669)",
        "The Geographer (c. 1669)",
        "Vermeer's painting of a scholar in his study, pausing with a pair of dividers in his hand and a globe on the cabinet — one of only two paintings Vermeer made of a man alone (the other is The Astronomer), and both were probably painted from the same model. The geographer's face is lit by the window, his map-covered walls and his instruments tell a story of a man measuring the world.",
        "Look at the geographer first: he pauses, dividers in hand, looking up from his maps as if he has just solved a problem — the light from the window falls across his face and his papers. Then the room: the walls are covered with maps and charts, a globe stands on the cabinet, and a book lies open on the table — a scholar's world. The painting was probably made from the same model as The Astronomer, and the two works have always hung together; the geographer has often been read as a portrait of Antonie van Leeuwenhoek, Vermeer's neighbor and the inventor of the microscope. It is in the Städel Museum, Frankfurt.",
        "The Geographer (c. 1669) — the pause with the dividers",
        ["Dutch Golden Age", "Portrait"],
    ),
    "artw-girl-interrupted-at-her-music-359": _entry(
        "Johannes Vermeer (c. 1658)",
        "Girl Interrupted at Her Music (c. 1658)",
        "Vermeer's painting of a young woman at a virginal — a keyboard instrument — turning to look at us as if we have just walked in and interrupted her song. A man's silhouette is hinted in a painting on the wall, and the wine on the table suggests she was not practicing alone. The girl's glance out of the canvas is one of Vermeer's most direct engagements with the viewer.",
        "Look at the girl first: she has just stopped playing and turned to face us, her hands still at the keys — Vermeer lets us walk into the room and break the music. Then the clues: the wine glass and the jug on the table, the viola da gamba on the floor, and the dark painting on the wall that seems to show a man watching — the scene is a courtship, and we are the intruders. The mirror, the sheet music, and the tiled floor are painted with Vermeer's usual geometry. The painting is in the Frick Collection, New York, where it hangs among Vermeer's other interiors.",
        "Girl Interrupted at Her Music (c. 1658) — the glance as you enter",
        ["Dutch Golden Age", "Genre Painting"],
    ),
    "artw-woman-with-a-pearl-necklace-360": _entry(
        "Johannes Vermeer (c. 1664)",
        "Woman with a Pearl Necklace (c. 1664)",
        "Vermeer's painting of a young woman before a mirror, holding up two yellow ribbons to her pearl necklace — the mirror shows only a pale reflection of her face, and the pearls lie on the table, waiting. The painting is a study of vanity and light: the woman checks her jewels, the mirror reflects without showing, and the light from the window turns everything to gold.",
        "Look at the mirror first: it shows only the faintest reflection of her face — the mirror, in Dutch painting, is the symbol of vanity, and here it reveals nothing. Then the ribbons: she holds two yellow ribbons up to the pearls, checking how they look, and the pearls themselves still lie on the table — the moment is before, not after. The white wall behind her, the fur-trimmed jacket, and the soft window light make the painting a study in stillness. Vermeer painted the woman alone, absorbed in herself, and the painting hangs in the Gemäldegalerie, Berlin.",
        "Woman with a Pearl Necklace (c. 1664) — the mirror and the ribbons",
        ["Dutch Golden Age", "Genre Painting"],
    ),
    "artw-the-return-of-the-prodigal-son-361": _entry(
        "Rembrandt van Rijn (c. 1669)",
        "The Return of the Prodigal Son (c. 1669)",
        "Rembrandt's last great painting: the parable of the prodigal son, painted at the end of the artist's life — bankrupt, grieving, and stripped of everything. The father bends over his returning son, his hands on the boy's shoulders, while the older brother watches from the shadow. It is Rembrandt's final statement about forgiveness, and it is often called the greatest religious painting of the 17th century.",
        "Look at the father first: he is nearly blind, bent with age, and his hands rest on the ragged son's shoulders with a tenderness that makes the whole painting — the hands, the light on them, the son's head against the father's chest — feel like a single act of forgiveness. Then the son: his shoes are worn through, his back is bare, his head is shaved like a beggar's. The older brother stands in shadow at the right, excluded and resentful. Rembrandt painted this at the end of his life, after losing his wife, his son, and his fortune — it is the work of a man who understood both the waste and the welcome. It hangs in the Hermitage, St Petersburg.",
        "The Return of the Prodigal Son (c. 1669) — the father's hands",
        ["Dutch Golden Age", "Religious"],
    ),
    "artw-belshazzars-feast-362": _entry(
        "Rembrandt van Rijn (1635)",
        "Belshazzar's Feast (1635)",
        "Rembrandt's painting of the Babylonian king Belshazzar recoiling as a divine hand writes Hebrew letters on the wall — 'Mene, Mene, Tekel, Upharsin' — 'numbered, weighed, divided,' the prophecy of his fall. Rembrandt painted the mysterious handwriting correctly: he had a Jewish scholar copy the real Aramaic script, and the letters are written right to left, the way the king's guests would have read them.",
        "Look at the king first: he twists away from the table, his face lit by the ghostly letters, his crown askew — the wine in his goblet is still, caught mid-splash. Then the letters: they are real Aramaic, written right to left, and Rembrandt got a Jewish scholar to copy them accurately — the inscription reads 'Mene, Mene, Tekel, Upharsin,' the famous writing on the wall. The gold and silver of the feast, the jewels, the flames, and the terrified faces are painted with Rembrandt's early dramatic style, full of baroque movement. The painting is in the National Gallery, London.",
        "Belshazzar's Feast (1635) — the handwriting on the wall",
        ["Dutch Golden Age", "Religious"],
    ),
    "artw-danae-363": _entry(
        "Rembrandt van Rijn (1636)",
        "Danaë (1636)",
        "Rembrandt's painting of Danaë, the princess whom Zeus visited as a shower of gold — here shown as a real, ordinary woman welcoming the light that is the god's arrival. Rembrandt worked on it for over a decade, repainting the head of Danaë and the face of the maidservant, and in 1985 the painting was slashed and doused with acid by a visitor; it took years of restoration to bring it back.",
        "Look at Danaë first: she is not a classical goddess but a flesh-and-blood woman, raising herself on the bed as the golden light — the god — pours over her. Then the details: the maidservant pulls back the curtain, the cupid above the bed cries, and the golden shower of light is the entire subject — the painting is about the moment before. Rembrandt repainted Danaë's face and the maidservant over the years, leaving a patchwork of styles. In 1985 a visitor slashed the canvas and threw acid on it; the Hermitage spent 12 years restoring it, and the damage is still faintly visible as a scar in the painting's history.",
        "Danaë (1636) — the golden light and the welcoming body",
        ["Dutch Golden Age", "Mythology"],
    ),
    "artw-the-polish-rider-364": _entry(
        "Rembrandt van Rijn (c. 1655)",
        "The Polish Rider (c. 1655)",
        "Rembrandt's painting of a young horseman riding through a dark, mountainous landscape — his fur cap, his saber, his yellow coat, his calm face. No one knows who the rider is: a Polish nobleman, a Hungarian, a soldier of fortune, or simply an ideal of the wanderer. The painting's mystery is its power — the rider rides through a landscape of shadows toward something we cannot see.",
        "Look at the rider first: he sits calmly on a pale horse, his fur cap and his saber suggesting a soldier, but his face is thoughtful, almost gentle — he is no portrait, but an idea of the solitary traveler. Then the landscape: the hills are dark, the sky is gathering, and the path ahead is hidden — the rider moves out of the darkness into more darkness. The painting has been called Rembrandt's most romantic work, and its meaning has never been settled: a portrait, a Biblical figure, a Polish exile, or an allegory of life as a journey. It is in the Frick Collection, New York.",
        "The Polish Rider (c. 1655) — the rider into the dark",
        ["Dutch Golden Age", "Equestrian"],
    ),
    # ---------- Italian Renaissance & Baroque ----------
    "artw-the-annunciation-leonardo-365": _entry(
        "Leonardo da Vinci (c. 1472)",
        "The Annunciation (c. 1472)",
        "Leonardo's early painting of the angel Gabriel announcing to Mary that she will bear the son of God — painted when he was about 20, still working in the workshop of Verrocchio. The painting is full of his signature touches already: the angel's wing is anatomically wrong (it belongs to a bird), and the marble table in front of Mary is painted with a perspective that has a hidden flaw — the young Leonardo was already breaking rules.",
        "Look at the angel first: Gabriel kneels in the garden, his right hand raised in blessing, and his wing is painted with the feathers of a bird — a mistake in anatomy that scholars use to date the painting to Leonardo's youth. Then Mary: she sits behind a marble table, her right hand raised in surprise, her left hand on the book she was reading. The painting is Leonardo's earliest known independent work, made when he was about 20, and the garden with the cypress and the walled city in the distance already shows his love of landscape. Look at the table's perspective — it does not quite meet the horizon, a flaw the young master would never repeat.",
        "The Annunciation (c. 1472) — the bird-winged angel and the flawed table",
        ["Renaissance", "Religious"],
    ),
    "artw-the-virgin-of-the-rocks-366": _entry(
        "Leonardo da Vinci (1483-86)",
        "The Virgin of the Rocks (1483-86)",
        "Leonardo's painting of the Virgin Mary, the infant Jesus, the infant John the Baptist, and an angel in a dark, fantastic grotto of rocks and water — the first great painting of his Milan years. The figures are arranged in a pyramid, the light comes from nowhere and everywhere, and the landscape behind them is a dream of caves and mist. Leonardo painted two versions — the Louvre version and a later one in London — because he and his patrons could not agree on payment.",
        "Look at the grotto first: the rocks are not a real cave but a geological fantasy — limestone towers, dark water, and plants that Leonardo drew from life, arranged like a dream of the earth's beginning. Then the figures: Mary's hand hovers over the infant John, the angel points at the infant Jesus, and the four of them form a pyramid that holds the dark landscape in balance. The painting's composition became the model for a generation of Italian altarpieces. Leonardo made two versions of the Virgin of the Rocks — the first, now in the Louvre, was the subject of a long lawsuit over payment; the second, in London's National Gallery, was painted with his assistants a decade later.",
        "The Virgin of the Rocks (1483-86) — the pyramid in the grotto",
        ["Renaissance", "Religious"],
    ),
    "artw-ginevra-de-benci-367": _entry(
        "Leonardo da Vinci (c. 1474)",
        "Ginevra de' Benci (c. 1474)",
        "Leonardo's portrait of a young Florentine woman, Ginevra de' Benci — the only Leonardo painting in the Americas. Ginevra was a poet and a philosopher's daughter, and Leonardo painted her against a juniper bush, because 'ginevra' sounds like the Italian for juniper, 'ginepro' — a visual pun. The back of the panel is painted with a wreath of laurel and palm, the symbols of poetry and virtue.",
        "Look at her face first: she is pale, composed, and her eyes do not quite meet yours — she is painted in profile-three-quarter, and the light models her face softly, the way only Leonardo could. Then the juniper: the bush behind her is a pun on her name — 'Ginevra' sounds like 'ginepro,' juniper — and the sprigs are painted with botanical care. The painting is the only Leonardo in the Americas, and it is a fragment: the lower part was cut off, probably a version of the hands we see in his later portraits. The reverse of the panel bears a wreath of laurel and palm around a sprig of juniper, with the motto 'Beauty Adorns Virtue.'",
        "Ginevra de' Benci (c. 1474) — the juniper pun and the pale face",
        ["Renaissance", "Portrait"],
    ),
    "artw-the-baptism-of-christ-368": _entry(
        "Andrea del Verrocchio & Leonardo da Vinci (c. 1475)",
        "The Baptism of Christ (c. 1475)",
        "The painting of John the Baptist baptizing Jesus in the Jordan, made in Verrocchio's workshop — with the famous detail that the left-hand angel was painted by the young apprentice Leonardo, and it is so much more alive than the rest that legend says Verrocchio gave up painting forever. The story is probably untrue, but the angel is the first brushwork we can certainly attribute to Leonardo.",
        "Look at the angel first: on the left, a kneeling angel with golden curls, turning his head toward the viewer — his face and his hair are painted with a softness the rest of the painting lacks, and this figure is the work of the young Leonardo, then a teenager in Verrocchio's workshop. Then the rest: John the Baptist, the dove, the hand of God, and the river are painted in the harder, flatter style of the older workshop. Vasari tells the legend that Verrocchio, seeing his student's angel, never touched a brush again. The painting is in the Uffizi, Florence, and the two hands in the same picture let you compare a master and his student at the exact moment the student became greater.",
        "The Baptism of Christ (c. 1475) — find the angel Leonardo painted",
        ["Renaissance", "Religious"],
    ),
    "artw-pallas-and-the-centaur-369": _entry(
        "Sandro Botticelli (c. 1482)",
        "Pallas and the Centaur (c. 1482)",
        "Botticelli's painting of Pallas Athena, goddess of wisdom, gripping the hair of a centaur — half man, half horse — who bows before her, armed with a bow and a quiver. The painting's meaning is debated: Athena taming the wild forces of passion, or a Medici allegory of reason defeating instinct, painted for the ruler of Florence. The centaur's face, resigned and beautiful, makes the painting a meditation on the cost of civilization.",
        "Look at the centaur first: he is half man, half horse, with a bow in his hand and a resigned, almost gentle face — he bows, and Athena's hand in his hair is both a grip and a caress. Then Athena: her dress is embroidered with the three rings of the Medici, and her spear and her calm face make her the figure of reason and rule. The painting was probably made for Lorenzo de' Medici, and it has been read as wisdom mastering instinct, or the state taming its wilder subjects. The landscape behind is a calm sea with ships, the world that reason protects. It hangs in the Uffizi, Florence, near Botticelli's Birth of Venus and Primavera.",
        "Pallas and the Centaur (c. 1482) — the hand in the centaur's hair",
        ["Renaissance", "Mythology"],
    ),
    "artw-the-mystic-nativity-370": _entry(
        "Sandro Botticelli (1500-01)",
        "The Mystic Nativity (1500-01)",
        "Botticelli's strange, apocalyptic Nativity — the last painting he dated, signed in Greek, with angels and devils, a dark cave, and the Holy Family in a world that is falling apart. At the top, angels dance in a circle, and below, three men embrace three angels — Botticelli painted this at the turn of the century, when Florence was gripped by the preacher Savonarola's warnings of doom, and the painting is full of that fear and hope.",
        "Look at the top first: twelve angels dance in a circle over the roof of the stable — and beneath the roof, three angels embrace three men, while three devils flee into the cracks of the earth. Then the Nativity itself: Mary kneels before the child, Joseph sleeps, and the ox and the ass look on — but the stable is a cave, and the sky is dark and strange. Botticelli signed the painting in Greek, with a warning: 'I, Sandro, painted this at the end of the year 1500, in the troubles of Italy.' The painting, made in the shadow of Savonarola's prophecies, mixes the Christmas story with the end of the world. It is in the National Gallery, London.",
        "The Mystic Nativity (1500-01) — the dancing angels and the fleeing devils",
        ["Renaissance", "Religious"],
    ),
    "artw-the-madonna-of-the-meadow-371": _entry(
        "Raphael (1506)",
        "The Madonna of the Meadow (1506)",
        "Raphael's painting of Mary with the infant Jesus and the infant John the Baptist in a green meadow — the three figures form a perfect pyramid, and the composition became the model for every Madonna painted after it. The child Jesus reaches for a small cross held by John — a premonition of the Crucifixion — and the whole painting is built on balance, calm, and the geometry of love.",
        "Look at the pyramid first: Mary's head is the apex, and her blue robe and red dress spread to the base where the two children sit — the composition is so balanced that the painting feels inevitable. Then the cross: John the Baptist holds a small reed cross, and the infant Jesus reaches for it — the child reaching for the instrument of his own death, painted with complete serenity. The meadow is painted with botanical detail, and the church on the horizon echoes the pyramid. Raphael painted this in Florence in 1506, learning from Leonardo's pyramids, and it hangs in the Kunsthistorisches Museum, Vienna.",
        "The Madonna of the Meadow (1506) — the child reaching for the cross",
        ["Renaissance", "Religious"],
    ),
    "artw-the-sistine-madonna-372": _entry(
        "Raphael (1513-14)",
        "The Sistine Madonna (1513-14)",
        "Raphael's most famous Madonna — Mary carrying the infant Jesus, stepping forward out of a curtained heaven, with Saint Sixtus and Saint Barbara kneeling at the sides. At the bottom, two little angels lean on the frame, looking up — the most copied cherubs in art history, reproduced on everything from greeting cards to wine labels. The painting was made for the church of San Sisto in Piacenza, and the two cherubs were painted last, from life, possibly two boys Raphael met in the street.",
        "Look at the bottom first: two cherubs lean on the frame, elbows on the wood, looking up at the Virgin — they were added at the end, and they have become the most famous angels in Western art. Then the Virgin: she steps forward out of the clouds, holding the child, her feet bare, her face young and grave, and the curtain behind her is pulled back as if heaven has opened. Saint Sixtus points toward the viewer, Saint Barbara gazes up, and the painting is built on the balance of those looks. The painting hung in Dresden for two centuries, where it drew its own pilgrimage of artists, and it was saved from the collapsing Soviet vaults after WWII and returned to the Gemäldegalerie.",
        "The Sistine Madonna (1513-14) — the two cherubs on the frame",
        ["Renaissance", "Religious"],
    ),
    "artw-the-marriage-of-the-virgin-373": _entry(
        "Raphael (1504)",
        "The Marriage of the Virgin (1504)",
        "Raphael's early masterpiece, painted when he was 21 — the wedding of Mary and Joseph, with the high priest joining their hands, the suitors with their withered rods, and the great temple of Jerusalem in the background, drawn in perfect perspective. The painting is famous for that temple: a domed building with colonnades, painted with a vanishing point that makes the whole scene feel real and deep. Raphael was working in Perugia, and he signed the painting on the temple's foundation stone.",
        "Look at the temple first: the great domed building in the background is drawn with a perspective so precise that the eye travels through the whole painting to its vanishing point — Raphael, at 21, was showing off. Then the ceremony: the high priest joins the hands of Mary and Joseph, while the disappointed suitors snap their withered rods — the legend says only Joseph's rod flowered. The painting was made for the Città di Castello, and it owes its composition to Perugino's painting of the same subject — but Raphael's temple, his figures, and his space surpass his teacher's. It hangs in the Brera in Milan, and the signature on the temple's foundation stone reads 'Raphael Urbinas.'",
        "The Marriage of the Virgin (1504) — the temple and the withered rods",
        ["Renaissance", "Religious"],
    ),
    "artw-st-george-and-the-dragon-374": _entry(
        "Raphael (c. 1506)",
        "St George and the Dragon (c. 1506)",
        "Raphael's small painting of Saint George in armor, on a rearing white horse, spearing the dragon while the princess flees in the background — one of the most copied images of the saint. The painting is only 28 by 21 cm, made to be carried, and it was given to Henry VII of England — Raphael's first contact with the English court that would later own so many of his works. The dragon's body twists beneath the horse, and the landscape opens behind them into a golden distance.",
        "Look at the strike first: George's lance pierces the dragon's mouth, the horse rears, and the saint leans into the blow — the whole action is frozen at the instant of the kill. Then the princess: she flees in the background, small and golden, and the landscape opens into a distance of hills and light. The painting is tiny — made to be held — and it was a gift to Henry VII of England, part of a diplomatic exchange between the courts. Raphael painted at least two versions of the subject, and this one, with its rearing horse and its calm saint, is the more dynamic. It is in the National Gallery, Washington.",
        "St George and the Dragon (c. 1506) — the lance in the dragon's mouth",
        ["Renaissance", "Religious"],
    ),
    "artw-the-deposition-375": _entry(
        "Raphael (1507)",
        "The Deposition (1507)",
        "Raphael's painting of Christ's body being carried from the cross to the tomb — four men carry the dead Christ, Mary Magdalene holds his hand, and the Virgin Mary faints in the arms of the women at the left. The painting was commissioned for a church in Perugia, and it shows Raphael at the moment he was absorbing the drama of Michelangelo — the bodies twist, the grief is physical, and the composition breaks out of the calm of his early work.",
        "Look at Christ first: his body is carried diagonally, head down, his arm hanging — the dead weight is real, and the men straining under it are painted with effort in every muscle. Then the grief: the Virgin has fainted and is held up by the women, Mary Magdalene grasps Christ's hand, and the young man at the right carries the crown of thorns and the nails. The painting was made for the Atalanta Baglioni chapel in Perugia, and the composition of the mourning group was copied from an ancient Roman relief. It is in the Galleria Borghese, Rome.",
        "The Deposition (1507) — the dead weight of Christ",
        ["Renaissance", "Religious"],
    ),
    "artw-the-dying-slave-376": _entry(
        "Michelangelo (1513-16)",
        "The Dying Slave (1513-16)",
        "Michelangelo's marble figure of a young man, one hand on his chest, one arm raised, his body twisting in a slow, graceful arc — made for the tomb of Pope Julius II, along with its companion, the Rebellious Slave. The slaves were meant to symbolize the arts, imprisoned by death, and the Dying Slave has been read as the soul's last breath, the body's surrender, and the most beautiful figure Michelangelo ever carved. It was sold out of the tomb project and ended up in the Louvre.",
        "Look at the body first: the young man's torso twists in a long, soft curve — the head falls back, the arm rises, and the whole figure seems to be released, not struggling. Then the face: it is calm, almost asleep, which is why the figure has been read as death's gentle arrival rather than resistance. Michelangelo carved the slave for the tomb of Pope Julius II, one of a series of captives meant to represent the arts bound by death. The statue was never installed in the tomb; it was given to a French nobleman and reached the Louvre, where it stands near the Rebellious Slave, its opposite — one resisting, one yielding.",
        "The Dying Slave (1513-16) — the body released",
        ["Renaissance", "Sculpture"],
    ),
    "artw-moses-michelangelo-377": _entry(
        "Michelangelo (1513-15)",
        "Moses (1513-15)",
        "Michelangelo's marble Moses, carved for the tomb of Pope Julius II — a figure with horns, a flowing beard, and a body of coiled power, seated with the tablets of the Law under his arm. The horns come from a mistranslation of the Bible: the Latin said Moses' face 'shone' with light, but the translator used the word for 'horned,' and Michelangelo followed the text. The statue is so forceful that it was said Michelangelo, on finishing, struck it and cried 'Speak!'",
        "Look at the horns first: Moses has two horns — they come from a famous mistranslation, where the Hebrew for 'rays of light' became the Latin for 'horns,' and Michelangelo faithfully carved what the text said. Then the body: the beard flows like water, the muscles are coiled, and the figure sits with one foot raised, ready to rise — the moment before he smashes the tablets. Michelangelo carved Moses for the tomb of Julius II, and it is the centerpiece of the monument in San Pietro in Vincoli, Rome. Legend says the artist, seeing his finished work, struck the knee and cried, 'Speak!' — and a small scar is still pointed out on the marble.",
        "Moses (1513-15) — the horns and the coiled power",
        ["Renaissance", "Sculpture"],
    ),
    "artw-david-with-the-head-of-goliath-378": _entry(
        "Caravaggio (c. 1610)",
        "David with the Head of Goliath (c. 1610)",
        "Caravaggio's painting of the young David holding up the severed head of Goliath — and the head is a self-portrait of Caravaggio. He painted it in the last year of his life, exiled from Rome for murder and hoping for a pardon, and the painting was sent to the papal authorities as a plea. David looks at the head with pity and horror, and the head — Caravaggio's own face — is dead, the mouth open, the eyes still.",
        "Look at the head first: it is Caravaggio's own face — he painted his self-portrait as the dead giant, sending it to the pope as a confession and a plea for pardon. Then David: the boy holds the head at arm's length, his sword still in his hand, and his expression is not triumph but horror — he looks at the face he has killed as if he recognizes it. Caravaggio painted this in 1610, the last year of his life, after fleeing Rome where he had killed a man; the painting was given to the papal nephew as a petition. It is in the Galleria Borghese, Rome, and the light falls on the two faces — the living boy and the dead man — with brutal clarity.",
        "David with the Head of Goliath (c. 1610) — the self-portrait as the dead giant",
        ["Baroque", "Religious"],
    ),
    "artw-narcissus-caravaggio-379": _entry(
        "Caravaggio (c. 1597)",
        "Narcissus (c. 1597)",
        "Caravaggio's painting of Narcissus, the youth who fell in love with his own reflection and drowned — here he kneels at the edge of a dark pool, his hands braced on the water, staring at his own face. The painting is built on the symmetry of the boy and his reflection, and the dark, empty background makes the two faces — one real, one watery — the entire world of the picture.",
        "Look at the symmetry first: the kneeling boy and his reflection form a single circle, his face and its double — the painting's whole meaning is that symmetry, the self meeting itself. Then the darkness: the background is black and empty, and only the boy's shoulder and face catch the light, as if the world has disappeared into his obsession. Caravaggio painted this in Rome in his twenties, and the subject — the youth who drowns reaching for his own image — became the emblem of the Baroque obsession with reflection and illusion. The painting is in the Galleria Nazionale d'Arte Antica, Rome.",
        "Narcissus (c. 1597) — the boy and his reflection",
        ["Baroque", "Mythology"],
    ),
    "artw-judith-slaying-holofernes-380": _entry(
        "Artemisia Gentileschi (c. 1620)",
        "Judith Slaying Holofernes (c. 1620)",
        "Artemisia Gentileschi's painting of Judith and her maidservant killing the Assyrian general Holofernes — blood spraying, arms straining, the two women working together to cut the general's throat. Artemisia painted this version in Florence, years after she was raped by her teacher and testified in a brutal trial; the painting's violence is often read as her revenge made art. It is one of the most powerful images of women in Western painting.",
        "Look at the violence first: Judith saws through Holofernes' neck while her maidservant pins his shoulders down — the blood sprays in arcs, the general's legs kick, and the two women strain with real physical effort. Then the light: the scene is lit from the left, and the faces are in the half-darkness — this is not a sanitized Biblical scene but a real killing. Artemisia painted the subject at least twice; this version, made in Florence around 1620, is the more ferocious. She had been raped by her teacher Agostino Tassi in 1611 and testified against him under torture, and scholars have long read her Judith paintings as the revenge she could not take in court. It is in the Uffizi, Florence.",
        "Judith Slaying Holofernes (c. 1620) — the two women and the blood",
        ["Baroque", "Biblical"],
    ),
    "artw-self-portrait-allegory-of-painting-381": _entry(
        "Artemisia Gentileschi (1638-39)",
        "Self-Portrait as the Allegory of Painting (1638-39)",
        "Artemisia's self-portrait as the personification of Painting itself — the first time a woman artist painted herself as the allegory of her own art. She holds a brush in one hand and a palette in the other, her arm raised, her hair loose, her face turned toward the work. No male artist had painted himself as the female allegory of Painting; Artemisia took the symbol and made it her own face.",
        "Look at the face first: Artemisia looks out from the canvas, not at the viewer — she is caught at the moment of turning toward her work, and the concentration is real. Then the pose: her raised arm, the brush, the palette, and the loose hair are the exact attributes of the allegorical figure of Painting, a symbol usually shown as an anonymous woman — Artemisia made it her own self-portrait, the first time a woman painted herself as the art. The gold chain around her neck has a mask-shaped pendant, an emblem of imitation and art. She painted it in London, at the court of Charles I, and it hangs in the Royal Collection.",
        "Self-Portrait as the Allegory of Painting (1638-39) — the painter as Painting",
        ["Baroque", "Self-Portrait"],
    ),
    # ---------- Bruegel ----------
    "artw-the-fall-of-icarus-382": _entry(
        "Pieter Bruegel the Elder (c. 1560)",
        "Landscape with the Fall of Icarus (c. 1560)",
        "Bruegel's painting of the myth of Icarus, who flew too close to the sun and fell — but Bruegel painted the fall almost invisibly: in the corner of a busy harbor scene, Icarus's legs splash into the sea while a farmer plows, a shepherd leans on his staff, and a fisherman casts his line, all ignoring the drowning boy. The painting is the great image of the world's indifference to individual tragedy.",
        "Look for Icarus first: his legs kick out of the water in the lower right corner — the rest of him is already under — and no one is looking at him. Then the others: the farmer plows, the shepherd stares at the sky (at the sun, not the boy), the fisherman casts his line, and the ship sails on, its sails full of wind. The painting is the definitive image of the world's indifference: the myth says Icarus fell, and Bruegel shows that the world did not even notice. The painting in Brussels is one of two versions, and the shepherd and the plowman are painted with the dignity that Bruegel gave to working people — the world goes on, and that is the tragedy.",
        "Landscape with the Fall of Icarus (c. 1560) — find the legs in the water",
        ["Northern Renaissance", "Mythology"],
    ),
    "artw-hunters-in-the-snow-383": _entry(
        "Pieter Bruegel the Elder (1565)",
        "Hunters in the Snow (1565)",
        "Bruegel's painting of hunters returning through the snow, their dogs at their heels, their spears over their shoulders — with a village below, skaters on a frozen pond, and the mountains of the Alps on the horizon. It is the most famous winter scene in Western art, and the whole cycle of seasonal paintings it belongs to was made for a wealthy Antwerp patron. The painting hangs in Vienna, where it is one of the most loved works in the Kunsthistorisches Museum.",
        "Look at the hunters first: three men walk downhill with their dogs, carrying their spears — they have caught almost nothing, a fox is all that hangs from one spear. Then the village: below them, in the valley, people skate and play on a frozen pond, a fire burns, and the houses smoke — the world goes on, warm and busy, while the hunters descend into it. Bruegel painted this as part of a series of the months, made in 1565; it is the January scene. The black trees against the white snow, the green sky, and the blue-grey mountains make the painting a study of cold light, and the tiny skaters below are painted with a warmth that makes the whole scene human.",
        "Hunters in the Snow (1565) — the hunters and the skaters",
        ["Northern Renaissance", "Winter"],
    ),
    "artw-the-tower-of-babel-384": _entry(
        "Pieter Bruegel the Elder (1563)",
        "The Tower of Babel (1563)",
        "Bruegel's painting of the Tower of Babel from the Bible — a colossal spiral tower rising into the clouds, surrounded by a bustling city and harbor, built with every kind of crane and scaffold. The tower is painted like a real construction site, with hundreds of tiny workers, and Bruegel based its spiral form on the Colosseum in Rome, which he had seen on his Italian journey. The tower's builder, King Nimrod, visits the site in the lower left — and the tower is doomed, because God will scatter the builders and confuse their language.",
        "Look at the tower first: it spirals into the sky in concentric rings, each level a construction site with cranes, scaffolding, and tiny workers — Bruegel based the design on the Roman Colosseum. Then the details: the stone arches, the brick courses, the roads winding up the sides, and at the base, King Nimrod in red, inspecting the work. The painting is a warning about pride — the tower that reaches to heaven is exactly the kind of ambition God punishes. Bruegel painted two versions, and this one is the more finished. The painting hangs in the Kunsthistorisches Museum, Vienna, and the closer you look, the more the tower's impossible size and its doomed builders come into focus.",
        "The Tower of Babel (1563) — the spiral tower and its tiny builders",
        ["Northern Renaissance", "Religious"],
    ),
    # ---------- American Art ----------
    "artw-the-oxbow-385": _entry(
        "Thomas Cole (1836)",
        "View from Mount Holyoke (The Oxbow) (1836)",
        "Thomas Cole's painting of the Connecticut River bending through the landscape from the top of Mount Holyoke — the founding image of American landscape painting. The view splits in two: on the left, a dark, storm-torn wilderness with a dead tree and a single artist sketching; on the right, a sunlit valley of cultivated fields. Cole painted the moment when the American wilderness was turning into farmland, and the painting asks whether that change is a triumph or a loss.",
        "Look at the split first: the painting is divided diagonally — the dark, wild side with the storm and the dead tree, and the bright, tamed side with the fields and the river. Then the artist: on the rocky foreground at the left, a tiny figure with an umbrella sits sketching — it is Cole himself, and he has placed his own easel on the wild side. The river bends like a snake, and the storm is breaking, leaving the sunlit valley. Cole painted this in 1836, and it has become the most studied American painting of the 19th century — the moment the wilderness, which Cole mourned even as he painted it, gave way to the farm. It is in the Metropolitan Museum of Art.",
        "The Oxbow (1836) — the split between wilderness and farm",
        ["American", "Landscape"],
    ),
    "artw-the-course-of-empire-386": _entry(
        "Thomas Cole (1833-36)",
        "The Course of Empire (1833-36)",
        "Cole's five-painting series showing the rise and fall of a civilization in the same landscape: The Savage State, The Arcadian or Pastoral State, The Consummation of Empire, Destruction, and Desolation. Painted in the 1830s, the series is a warning to America: every empire that reached the peak of its glory — like the marble city in the third painting — was destroyed, and the same hill ends as ruins. The series is Cole's masterpiece and his most famous argument.",
        "Look at the arc first: the same bay and hill appear in all five paintings — first wilderness with a lone Indian, then a pastoral village, then a marble city of columns and domes at the height of power. Then the fall: in the fourth painting, Destruction, the same city burns while a general on horseback crosses a bridge, and in the fifth, Desolation, the ruins stand empty under a dead moon. Cole painted the series in the 1830s, at the height of American confidence, as a warning: the empire of the future will fall like Rome. The series hangs in the New-York Historical Society, and seeing all five in a row — the rise and the ruin of the same hill — is one of the great experiences of American art.",
        "The Course of Empire (1833-36) — the same hill, five ages",
        ["American", "Allegory"],
    ),
    "artw-kindred-spirits-387": _entry(
        "Asher B. Durand (1849)",
        "Kindred Spirits (1849)",
        "Durand's painting of the painter Thomas Cole and the poet William Cullen Bryant standing on a rocky ledge in the Catskills, looking into a wild valley — a tribute painted after Cole's death, showing the two friends who founded the Hudson River School. The painting is the emblem of American romanticism: two men, a wilderness, and the belief that nature was the nation's true cathedral. It was owned for 150 years by the New York Public Library, where it was one of the most beloved American paintings.",
        "Look at the two men first: Cole stands pointing into the valley while Bryant, his arms crossed, follows his gaze — they are 'kindred spirits' in the title, friends sharing the wilderness. Then the setting: a rock ledge in the Catskills, with the stream far below and the mountains fading into blue haze — the landscape is painted from sketches Durand made in the actual gorge. Durand painted the tribute after Cole's death in 1848, and the two figures — the painter and the poet — embody the Hudson River School's belief that America's nature was its greatest art. The painting was a gift to the New York Public Library, where it hung for 150 years, and it sold at auction in 2005 for $35 million — a record for an American painting at the time.",
        "Kindred Spirits (1849) — the painter and the poet in the Catskills",
        ["American", "Landscape"],
    ),
    "artw-the-gulf-stream-388": _entry(
        "Winslow Homer (1899)",
        "The Gulf Stream (1899)",
        "Homer's painting of a lone Black sailor on a broken boat in the Gulf Stream — the mast snapped, the tiller gone, sharks circling the hull, a waterspout on the horizon. Homer reworked the painting for years, and critics read it as a man adrift, beyond help, surrounded by danger — a meditation on race, fate, and the sea. The sailor, calm and indifferent, does not even look at the sharks.",
        "Look at the sailor first: he lies on the broken deck, propped on his elbow, staring past us — he does not look at the sharks, the broken mast, or the waterspout, and his calm is the painting's deepest mystery. Then the danger: the sharks circle the hull, one already biting at the bow, and the sky gathers a waterspout on the horizon — the sea is full of death, and the man is beyond caring. Homer painted the subject over many years, leaving the boat broken and the man passive; when critics asked what would happen to him, Homer said the man would be picked up by a ship — but he never painted the rescue. The painting is in the Metropolitan Museum of Art.",
        "The Gulf Stream (1899) — the calm sailor among the sharks",
        ["American", "Marine"],
    ),
    "artw-snap-the-whip-389": _entry(
        "Winslow Homer (1872)",
        "Snap the Whip (1872)",
        "Homer's painting of barefoot country boys playing snap-the-whip in a schoolyard meadow — a chain of children running and whipping, the last boys flung outward by the force. Painted in 1872, it became the emblem of American childhood: freedom, summer, and the open field. Homer painted the boys in the aftermath of the Civil War, and the image of carefree children running through the grass was his answer to a country that had spent years learning the cost of whips.",
        "Look at the chain first: the boys hold hands and run, and the force of the whip flings the last two boys outward — their legs fly, their hats fly, and the chain is about to break. Then the setting: a schoolhouse in the background, a meadow of wildflowers, and the mountains in the distance — the whole painting is summer. Homer painted the scene in 1872, from sketches made in the countryside, and the boys' joyful freedom was his image of America's hope after the Civil War. The painting exists in two versions, one in the Butler Institute and one in the Metropolitan Museum; the Met's version, with the longer chain and the flung boys, is the one that made the image famous.",
        "Snap the Whip (1872) — the chain about to break",
        ["American", "Genre Painting"],
    ),
    "artw-the-gross-clinic-390": _entry(
        "Thomas Eakins (1875)",
        "The Gross Clinic (1875)",
        "Eakins's painting of the surgeon Dr. Samuel Gross performing surgery in an amphitheater of medical students — a live operation, with blood on the surgeon's hands and the patient's wound open on the table. It was painted as a contribution to the Centennial Exhibition, and it was rejected for its realism: the blood, the scalpel, and the bare wound were considered unfit for public display. It is now considered the greatest American painting of the 19th century.",
        "Look at the surgeon first: Dr. Gross stands in the middle of the amphitheater, a bloody scalpel in his raised hand, lecturing as he operates — his head is crowned by the light from above, making him a priest of the new scientific medicine. Then the scene: the patient's leg is open on the table, his mother cowers in the shadows at the left, and a woman in the foreground shields her eyes — the only figure who cannot look. Eakins painted the clinic in 1875 for the Centennial; the exhibition jury hid it in a corner, and it was rejected from the art display for its brutality. It now hangs in the Philadelphia Museum of Art, where it is the museum's most famous work.",
        "The Gross Clinic (1875) — the scalpel raised in the light",
        ["American", "Realism"],
    ),
    "artw-the-agnew-clinic-391": _entry(
        "Thomas Eakins (1889)",
        "The Agnew Clinic (1889)",
        "Eakins's second great medical painting — a mastectomy in progress, with the surgeon Dr. D. Hayes Agnew presiding over a clean, white amphitheater, his hand raised in mid-lecture. Compared to the Gross Clinic, the operating room is transformed: everyone wears white, the patient is draped, and a nurse stands by — a painting of medicine becoming antiseptic, modern, and female. Eakins painted the nude patient's breast frankly, and the painting was again attacked as indecent.",
        "Look at the change first: compared to the Gross Clinic, everything is white — the surgeons, the gowns, the amphitheater — and the patient, draped and unconscious, is tended by a nurse, a new figure in the operating room. Then the surgeon: Dr. Agnew stands with his hand raised, mid-sentence, above the operation, a lecturer as much as a surgeon. Eakins painted this in 1889 as a tribute to Agnew, and he showed the patient's body with the same frankness that had scandalized the city in the Gross Clinic — the painting was exhibited at a medical convention and again attacked for indecency. The two paintings together — the bloody Gross Clinic of 1875 and the antiseptic Agnew Clinic of 1889 — chart the transformation of surgery in a generation. The Agnew Clinic hangs at the University of Pennsylvania.",
        "The Agnew Clinic (1889) — the white room and the draped patient",
        ["American", "Realism"],
    ),
    "artw-max-schmitt-in-a-single-scull-392": _entry(
        "Thomas Eakins (1871)",
        "Max Schmitt in a Single Scull (1871)",
        "Eakins's painting of his friend Max Schmitt, a champion oarsman, rowing on the Schuylkill River in Philadelphia — with the painter himself rowing in a second scull in the middle distance, recognizable by his beard and hat. The painting is the first great rowing picture, and it shows Eakins's obsession with exactness: the bridge, the boathouses, and the water are painted from direct observation, and the light on the river is precise to the hour of the day.",
        "Look at the oarsman first: Max Schmitt sits in his scull, his oars trailing, resting mid-stroke, his face turned toward the painter — and in the middle distance, a second sculler rows toward the viewer: that is Eakins himself, putting himself into his own painting. Then the water: the river is painted with a glassy exactness, reflecting the bridge and the boathouses, and the light is set to a specific late-afternoon hour. Eakins was a rower himself, and he painted the Schuylkill with an engineer's love of accuracy — the bridge is the real Columbia Bridge, and the boathouses are the real ones. The painting is in the Philadelphia Museum of Art, and it is the first of the great American rowing pictures.",
        "Max Schmitt in a Single Scull (1871) — find the painter in the second boat",
        ["American", "Sport"],
    ),
    "artw-madame-x-393": _entry(
        "John Singer Sargent (1884)",
        "Portrait of Madame X (1884)",
        "Sargent's portrait of the American-born Parisian beauty Virginie Gautreau — her pale skin, her dark hair, her bare shoulders, and her famous profile. When it was shown at the Paris Salon in 1884, the public and the critics were scandalized: her bare shoulder, her pose, and even the strap of her dress slipping were read as indecent, and Sargent left Paris for London, his reputation in France destroyed. The painting, originally showing the strap on her shoulder, was repainted by Sargent with the strap in place — but the damage was done, and the portrait became the most famous scandal of the Salon.",
        "Look at the profile first: Madame Gautreau's head is turned in sharp profile against the dark background, her skin almost white, her nose and chin drawn with a precision that makes her look like a marble statue. Then the details: her bare shoulders, the jeweled strap of her dress, and the deep V of her bodice — the 1884 Salon public found her pose and her pallor indecent, and the scandal drove Sargent out of Paris. He had originally painted the strap fallen off her shoulder; he repainted it in place after the uproar. The portrait was his ticket out of France and his masterpiece; it hangs in the Metropolitan Museum, where Sargent asked that it be known as 'Madame X.'",
        "Portrait of Madame X (1884) — the pale profile that scandalized Paris",
        ["American", "Portrait"],
    ),
    "artw-the-daughters-of-edward-boit-394": _entry(
        "John Singer Sargent (1882)",
        "The Daughters of Edward Darley Boit (1882)",
        "Sargent's portrait of the four Boit sisters in their family's Paris apartment — the eldest two stand at the back, the youngest sits on the floor, and one little girl stands in the foreground, staring out at us with a doll in her arms. The girls are scattered across a dim, cavernous room with two enormous Japanese vases, and the painting breaks every rule of the formal family portrait: the children are separated, the space is empty, and the youngest seems to be leaving the room. Some see it as a portrait of childhood; others see the beginning of the family's tragedy, as the Boit girls grew into recluses.",
        "Look at the arrangement first: the four sisters are not posed together — one sits on the floor, one stands before the vases, two stand at the back in the shadows — and the space between them is as much the subject as the girls. Then the little girl in front: she stares straight at us with her doll, the only one who meets our eyes, and she seems about to step out of the painting. The two blue-and-white Japanese vases are real — the Boits' vases, now in the Museum of Fine Arts, Boston, where the painting hangs. Sargent painted the girls in 1882, and the painting's emptiness and its scattered children have been read as a portrait of a family already drifting apart.",
        "The Daughters of Edward Darley Boit (1882) — the scattered sisters and the vases",
        ["American", "Portrait"],
    ),
    "artw-carnation-lily-lily-rose-395": _entry(
        "John Singer Sargent (1885-86)",
        "Carnation, Lily, Lily, Rose (1885-86)",
        "Sargent's painting of two little girls in a garden at dusk, lighting paper lanterns among the flowers — the title comes from a popular song of the time. He painted it outdoors, in the English countryside, only at dusk, when the light was exactly right — the painting took two summers and hundreds of sessions, and the girls, the real daughters of his host, had to stand still in the twilight with lanterns that were relit every time the light failed. The result, with its glowing lanterns against the blue dusk, made Sargent's name in England.",
        "Look at the light first: the painting was made only at dusk, when the lanterns' glow and the fading blue of the sky balanced exactly — Sargent spent two summers, painting only in that hour, with the girls standing still while assistants relit the lanterns. Then the girls: they are painted from the back and side, absorbed in their task, their white dresses glowing in the twilight. The title comes from the refrain of a music-hall song: 'Carnation, lily, lily, rose.' The painting was shown at the Royal Academy in 1887, where it made Sargent a star overnight. It hangs in the Tate Britain.",
        "Carnation, Lily, Lily, Rose (1885-86) — the lanterns in the dusk",
        ["American", "Genre Painting"],
    ),
    # ---------- Gauguin ----------
    "artw-women-of-tahiti-396": _entry(
        "Paul Gauguin (1891)",
        "Women of Tahiti (1891)",
        "Gauguin's first painting of Tahitian women — two women seated on the sand, one in a red pareu, one in blue, in the shade of a tree, with the sea behind them. Gauguin had just arrived in Tahiti, fleeing Europe, and he painted the women as he dreamed they were: calm, monumental, and remote from the world he left. The painting's peace is deceptive — Gauguin's Tahiti was partly a fantasy, and his life there was full of conflict.",
        "Look at the two women first: they sit in the shade, one in red, one in blue, their faces calm and distant, their bodies painted in broad, flat shapes — Gauguin simplified everything into color and stillness. Then the setting: the tree above, the sand, and the sea beyond, painted in bands of blue and green, with no perspective and no hurry. Gauguin painted this soon after arriving in Tahiti in 1891, and the women are painted with a monumentality that makes them seem carved from the landscape. The painting is one of the first of his Tahiti period, and it is in the Musée d'Orsay, Paris.",
        "Women of Tahiti (1891) — the two women in the shade",
        ["Post-Impressionism", "Portrait"],
    ),
    "artw-the-yellow-christ-397": _entry(
        "Paul Gauguin (1889)",
        "The Yellow Christ (1889)",
        "Gauguin's painting of a crucifixion in the Breton countryside — a yellow Christ on a cross above three kneeling women, with the fields of Brittany behind them, painted in flat bands of gold, green, and red. Gauguin painted the crucifix as an image of rural faith: the women kneel in their Breton caps, the harvest is gathered, and the yellow Christ seems to belong to the autumn fields. It is the most famous of Gauguin's Breton paintings.",
        "Look at the color first: the Christ is painted bright yellow, and the fields behind are bands of gold and green — Gauguin abandoned natural color for the color of feeling, and the crucifix glows like a harvest sun. Then the women: three Breton women in white caps kneel at the cross's foot, their heads bowed, and the landscape — a Breton village with its church spire — is painted flat, like a tapestry. Gauguin painted the Yellow Christ in 1889, at Pont-Aven in Brittany, and the crucifixion is as much an image of Breton peasant faith as of the Bible story. The painting is in the Albright-Knox Art Gallery, Buffalo.",
        "The Yellow Christ (1889) — the gold crucifix in the autumn fields",
        ["Post-Impressionism", "Religious"],
    ),
    "artw-nevermore-398": _entry(
        "Paul Gauguin (1897)",
        "Nevermore (1897)",
        "Gauguin's painting of a naked Tahitian woman lying on a bed, with a black bird — a raven — perched behind her, and two figures whispering in the shadows. The title, 'Nevermore,' quotes Poe's raven, but Gauguin said the painting was 'not about Poe' — the raven is an image of the dark thoughts that filled his last years in Tahiti. He painted it in 1897, sick, in debt, and hearing of the death of his daughter in Europe — and the painting is full of that grief.",
        "Look at the woman first: she lies naked on the bed, her head turned away, her body calm and heavy — she does not look at the raven, and the raven does not look at her. Then the raven: it perches on the frame behind her, a black presence, and in the shadows two women whisper — the painting is full of watching. Gauguin gave the painting the title 'Nevermore' after Poe, though he said the subject was his own; he made it in 1897, the year his daughter Aline died in France, news he learned in Tahiti. It is one of the last great paintings of his life, and it hangs in the Courtauld Gallery, London.",
        "Nevermore (1897) — the raven and the turning woman",
        ["Post-Impressionism", "Symbolism"],
    ),
    # ---------- Modern ----------
    "artw-mr-and-mrs-clark-and-percy-399": _entry(
        "David Hockney (1970-71)",
        "Mr and Mrs Clark and Percy (1970-71)",
        "Hockney's double portrait of the fashion designer Ossie Clark and his wife Celia Birtwell in their London flat — he stands with his hand on his hip, she sits in a chair with a cat, Percy, on her lap. The painting is the most famous British portrait of the 20th century: a young, beautiful couple at the peak of the Swinging Sixties, painted with a flatness borrowed from photography and a tenderness that hides a coming separation — the Clarks divorced a few years later.",
        "Look at the couple first: Ossie Clark stands by the open balcony door, hand on hip, in a printed shirt; Celia sits in the chair, barefoot, a cat on her lap — they look at each other, and we are the third person in the room. Then the space: the flat is Hockney's friends' real flat, with the white furniture, the rug, and the balcony door open to the light. Hockney painted the portrait over months in 1970-71, and he made the couple sit for hours at a time. The painting was bought by the Tate, where it is one of the most popular works — and the marriage it celebrates ended in divorce, which gives the calm portrait its strange sadness.",
        "Mr and Mrs Clark and Percy (1970-71) — the couple and the cat",
        ["British", "Portrait"],
    ),
    "artw-cape-cod-morning-400": _entry(
        "Edward Hopper (1950)",
        "Cape Cod Morning (1950)",
        "Hopper's painting of a woman leaning out of her kitchen window, her hands on the sill, looking up at the sky — the morning light streams through the windows and throws long shadows across the lawn. Hopper's wife Jo was the model, and she remembered him arranging her pose for hours: the woman looks not at the sea but at the sky above it, and the whole painting is a held breath before the day begins.",
        "Look at the woman first: she leans out of the window, hands on the sill, chin up, looking at the sky — Hopper had his wife Jo pose for hours to get the angle right, and the woman's gaze is the painting's whole subject. Then the light: the morning sun pours through the two windows, cutting sharp shadows across the lawn, and the yellow of the house glows against the green. Hopper painted Cape Cod Morning in 1950, in his late period, and the painting's mood — a woman at a window, a bright day, a moment before something — is pure Hopper: ordinary and unreadable. It hangs in the Smithsonian American Art Museum, Washington.",
        "Cape Cod Morning (1950) — the woman at the window looking up",
        ["American", "Realism"],
    ),
    "artw-black-on-maroon-401": _entry(
        "Mark Rothko (1958)",
        "Black on Maroon (1958)",
        "Rothko's painting of a dark rectangle floating on a maroon field — one of a series he made in 1958, in which the colors darkened and the rectangles began to feel like doorways. The painting became famous for a different reason in 2012, when a visitor at the Tate Modern wrote graffiti on it with a marker pen: 'Vladimir Umanets, a potential piece of yellowism.' The mark was removed after restoration, but the painting's brief desecration made it world news.",
        "Look at the rectangle first: a dark, almost black form floats inside the maroon field, and the edges are ragged and smoky, as if the darkness is eating into the color around it — Rothko's late paintings darkened as his mood darkened, and the black shapes read like doorways or voids. Then the surface: the maroon is built from thin layers of paint, glowing from within, so the dark rectangle seems to hover rather than sit. In 2012, a visitor to the Tate Modern wrote graffiti across the painting with a marker — a protest he called 'yellowism' — and the restorers spent months removing it. The painting survives, and the vandalism became part of its story. It hangs in the Tate, London.",
        "Black on Maroon (1958) — the dark doorway in the glowing field",
        ["Abstract Expressionism", "Color Field"],
    ),
    "artw-one-number-31-1950-402": _entry(
        "Jackson Pollock (1950)",
        "One: Number 31, 1950 (1950)",
        "Pollock's most famous drip painting — 2.7 by 5.3 meters of black, white, and aluminum paint, thrown, dripped, and splattered across the canvas laid flat on the floor. Pollock walked around the canvas, dripping paint from sticks and cans, and the painting has no center, no top, and no bottom — it is an all-over field of energy that keeps the eye moving forever. Pollock's drip technique made him the most famous artist in America, and this is his masterpiece.",
        "Look at the surface first: the paint is built in layers — black and white drips over aluminum silver, lines crossing and looping with no beginning and no end — Pollock laid the canvas flat and walked around it, dripping paint from a stick, so the painting has no top and no bottom. Then the scale: it is over five meters wide, and standing in front of it, the web of lines fills your whole field of vision — you are inside the painting, not in front of it. Pollock made this in 1950, at the height of his powers, and the drip technique grew out of his belief that the canvas should record the energy of the body. The painting hangs at MoMA.",
        "One: Number 31, 1950 (1950) — the web with no center",
        ["Abstract Expressionism", "Action Painting"],
    ),
    "artw-orange-and-yellow-403": _entry(
        "Mark Rothko (1956)",
        "Orange and Yellow (1956)",
        "Rothko's painting of a great orange rectangle above a smaller yellow one, on a field of deeper orange — one of the 'classic' Rothkos of the mid-1950s, in which the rectangles float in a luminous haze. The painting is nearly three meters tall, and Rothko designed such canvases to be hung low, so that standing before them, the color becomes an environment. He once said he wanted his paintings to be experienced 'like a landscape,' from the inside.",
        "Look at the two rectangles first: a wide orange band floats above a smaller yellow one, and the edges are so soft that the colors glow against each other rather than meeting — Rothko built the surface with thin washes of paint, layering until the color seemed to emit light. Then the size: the canvas is almost three meters high, and Rothko insisted his big paintings hang low, close to the floor, so that the viewer stands inside the color field. He said his paintings were about 'basic human emotions — tragedy, ecstasy, doom' and that people should stand close and let the color work on them. Orange and Yellow is in the Albright-Knox Art Gallery, Buffalo.",
        "Orange and Yellow (1956) — the glowing bands",
        ["Abstract Expressionism", "Color Field"],
    ),
    "artw-two-forms-404": _entry(
        "Barbara Hepworth (1969)",
        "Two Forms (1969)",
        "Hepworth's sculpture of two interlocking bronze forms — one pierced with holes, the other solid — standing together like two figures, made in the last year of her life. Hepworth carved and cast abstract forms all her career, and she said her work was about 'the relationship of one form to another' — here, the two shapes lean toward each other, one open, one closed, like a conversation in bronze. The sculpture stands in the Hepworth Wakefield gallery, in the town where she was born.",
        "Look at the two forms first: one is pierced with circular holes, the other is solid and curved — they lean toward each other like two figures, one open and one closed, and the space between them is as much the sculpture as the bronze. Then the surfaces: the bronze is polished in some areas and roughly textured in others, so the light moves across it as you walk around. Hepworth made Two Forms in 1969, the year before she died, and she had been exploring the relationship of paired forms for decades — her 'family group' sculptures of the 1940s were the same conversation in wood and stone. The sculpture is in the collection of the Hepworth Wakefield.",
        "Two Forms (1969) — the open form and the closed form",
        ["British", "Sculpture"],
    ),
}


def main() -> int:
    data = json.loads(PATH.read_text(encoding="utf-8"))
    existing = {t["id"] for t in data}
    existing_names = {t["name"] for t in data}
    dup_ids = [i for i in NEW if i in existing]
    dup_names = [n for n in (v["name"] for v in NEW.values()) if n in existing_names]
    if dup_ids or dup_names:
        print("ABORT: collisions found!")
        if dup_ids:
            print("  duplicate ids:", dup_ids)
        if dup_names:
            print("  duplicate names:", dup_names)
        return 1

    SUBTYPES = {
        "artw-the-dying-slave-376": "Sculpture",
        "artw-moses-michelangelo-377": "Sculpture",
        "artw-two-forms-404": "Sculpture",
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
