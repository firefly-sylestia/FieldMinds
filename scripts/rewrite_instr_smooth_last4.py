#!/usr/bin/env python3
"""Smooth the last 4 scaffold-y pre-existing artworks instructions (ids 109, 123, 124, 295)."""
import json

PATH = "app/src/main/assets/topics/artworks.json"

REWRITES = {
    "artw-stele-of-hammurabi-109": (
        "The top scene is the claim: Hammurabi stands before the seated sun god Shamash, who hands him "
        "the rod and ring of justice — the law is divine. Below, the laws are not abstract — they price "
        "injuries (a man who knocks out another's tooth pays 1/3 mina of silver), set wages, and protect "
        "widows. One rule is famous for its cruelty: a builder whose house collapses and kills its owner "
        "is put to death."
    ),
    "artw-the-sistine-chapel-ceiling-123": (
        "Adam's and God's fingers hover a few millimeters apart, and the space between them has been "
        "called the most important gap in art — the moment before the spark. Now look at God's side: "
        "many art historians argue the cloak and the figures around God outline a human brain, painted "
        "300 years before the discovery that the brain is where the soul lives."
    ),
    "artw-the-ambassadors-1533-124": (
        "Move to the far right of the painting (or tilt your screen) until the smear at the bottom "
        "resolves into a skull — a memento mori that only reveals itself from one angle. On the table "
        "between the two men sit globes, a lute, and books: emblems of knowledge and harmony — and the "
        "lute has a broken string, the classic signal of discord and death."
    ),
    "artw-truisms-295": (
        "'Abuse of power comes as no surprise,' 'A little knowledge can go a long way,' 'Protect me "
        "from what I want' — each sounds like folk wisdom, and Holzer made some of them contradict "
        "others on purpose. She pasted the early Truisms as anonymous posters all over Manhattan at "
        "night, then printed them on hats, T-shirts, and stone benches — the art is the words in public "
        "space, not on a wall."
    ),
}


def main():
    data = json.load(open(PATH, encoding="utf-8"))
    by_id = {e["id"]: e for e in data}
    for eid, new_ins in REWRITES.items():
        assert eid in by_id, f"missing id {eid}"
        assert len(new_ins) <= 450, f"{eid} too long: {len(new_ins)}"
        by_id[eid]["exploreAction"]["instruction"] = new_ins
    json.dump(data, open(PATH, "w", encoding="utf-8"), ensure_ascii=False, indent=2)
    print(f"smoothed {len(REWRITES)} instructions")


if __name__ == "__main__":
    main()
