# Curio Topic Schema — Quick Reference

This file is the in-folder quick reference for the canonical topic data format
shipped under `app/src/main/assets/topics/{category}.json`. The full source-of-
truth (schema rationale, authoring pipeline, rollout cadence) lives in
[`app/CURIO_DATA_PLAN.md`](../../CURIO_DATA_PLAN.md) — especially **§2 (Topic
Data Schema)** and **§6 (Authoring Prompt Template)**.

---

## Files in this directory

| File | Purpose |
|---|---|
| `music.json` | Music topic pool (v1: 8 hand-authored examples). Phase 4 will grow to 150. |
| `SCHEMA.md` | This file — quick reference for the topic JSON shape. |

When new categories ship (Movies, Books, Art, Science, then Philosophy, History,
Architecture, Food), each gets its own `{category}.json` next to this one.

---

## JSON shape (root)

```json
{
  "categoryId": "MUSIC",
  "version": 1,
  "curatedDate": "2026-07-29",
  "topics": [ /* see below */ ]
}
```

| Field | Type | Notes |
|---|---|---|
| `categoryId` | string enum | Must match `CategoryId` in `app/data/Category.kt` AND the filename. Valid values: `MUSIC`, `MOVIES`, `BOOKS`, `VISUAL_ART`, `SCIENCE`, `WILDCARD`. (Note: WILDCARD doesn't get its own JSON — it's a meta-spin derived from the others per CURIO_DATA_PLAN.md §1.) |
| `version` | int | Bump on any non-trivial schema change so a stale APK can detect "asset schema newer than runtime knows about". |
| `curatedDate` | ISO-8601 date | When the last topic in this file was added/touched. |
| `topics` | array | The topic entries. |

---

## Per-topic shape

```json
{
  "id": "music-bjork-vespertine",
  "subtype": "Album",
  "name": "Vespertine",
  "teaser": "Björk's 2001 chamber-electronic album...",
  "imageUrl": "https://upload.wikimedia.org/.../Bjork-vespertine.jpg",
  "actionPrompt": {
    "verb": "Listen",
    "targetName": "Vespertine (2001) end-to-end",
    "durationMinutes": 55,
    "instruction": "Notice how the beats hit your chest vs your head..."
  },
  "aliases": ["Björk Vespertine"],
  "relatedTopicIds": [],
  "difficulty": 2,
  "weight": 100,
  "curatedBy": "human",
  "curatedDate": "2026-07-29"
}
```

| Field | Type | Required | Notes |
|---|---|:---:|---|
| `id` | string | ✅ | Stable kebab-case, `{category}-{slug}`. Generated once, never recycled (room DB has FK on this). |
| `subtype` | string | ✅ | Category-specific vocabulary. Music: `Album` \| `Track` \| `Era` \| `Artist`. Future categories get their own enum — see data plan §2. |
| `name` | string | ✅ | Display title. ≤ 80 chars. |
| `teaser` | string | ✅ | 1–2 sentences, ≤ 280 chars. The "one quirky fact" surfaced in §6 Topic Reveal. NOT a full bio. |
| `imageUrl` | string | ✅ | Wikimedia Commons URL (preferred) or empty string `""` if no good image exists. `/thumb/`-shaped to 512px or 1024px wide. |
| `actionPrompt.verb` | string | ✅ | `"Listen"` \| `"Watch"` \| `"Read"` \| `"Look at"` \| `"Try"` \| `"Visit"` \| `"Make"` \| `"Explore"`. Drives the icon glyph on the action card. |
| `actionPrompt.targetName` | string | ✅ | Exact artifact to consume. "Vespertine (2001) end-to-end", not "an album by Björk". |
| `actionPrompt.durationMinutes` | int | ✅ | Realistic human time-to-engage. ≤ 60 unless the artifact genuinely demands more. |
| `actionPrompt.instruction` | string | ✅ | The actual prompt. ≤ 280 chars. Must pass the **quality bar below**. |
| `aliases` | string[] | ❌ | Search keywords. Default `[]`. |
| `relatedTopicIds` | string[] | ❌ | Cross-category links for serendipity. Default `[]`. |
| `difficulty` | int 1–3 | ❌ | Tunes serendipity rate (default 1). 1 = immediate, 3 = deep dive. |
| `weight` | int | ❌ | Pool-relative weight for weighted draw (default 100). Higher = surfaces more often. |
| `curatedBy` | string | ❌ | `"human"` \| `"ai"` \| `"ai+human"`. Trust label. |
| `curatedDate` | ISO-8601 date | ❌ | When this specific topic was last touched. |

---

## The `instruction` quality bar (from CURIO_DATA_PLAN.md §2.1)

Every `instruction` field must pass all four checks:

1. **Actionable** — user can act without further research. Names the specific artifact.
2. **Specific** — names the actual album / film / book / painting / paper. Not "explore music from 1995."
3. **Time-bounded** — ≤ 60 minutes unless the topic genuinely demands more. `"Read chapter 1"`, not `"read the whole thing"`.
4. **Curiously-framed** — invites the user to notice something they wouldn't notice casually. Not "listen to Vespertine" but "Notice how the beats hit your chest vs your head — that's intentional."

---

## Validation

The Gradle task `validateTopics` (registered in `app/build.gradle.kts`) parses every
JSON file in this directory and asserts:

- Root has `categoryId`, `version`, `curatedDate`, `topics`.
- `topics` array is non-empty.
- All `id`s unique **across all files** (cross-file collisions would break the Room FK on `id`).
- Every topic has `id`, `subtype`, `name`, `teaser`, `imageUrl`, `actionPrompt`.
- Every `actionPrompt` has `verb`, `targetName`, `durationMinutes`, `instruction`.
- Every `instruction` ≤ 280 chars.

The task runs automatically as a `preBuild` dependency (only when this
directory contains JSON files), so any malformed entry fails the assemble.

### NOT validated today (deferred to Phase 4)

- **`imageUrl` reachability.** The task does NOT HEAD the URLs to check
  they're not 404. CURIO_DATA_PLAN.md §5.2 step 3 mentions a future "live"
  validator that does HEAD checks as a *non-blocking* sanity check — that
  ships with the Room + seed flow in Phase 4. Until then, contributors must
  verify URLs manually before pushing.
- **Cross-file `relatedTopicIds` resolution.** The task checks the topic
  fields exist but does not verify that every `relatedTopicIds[i]` matches
  an `id` in some other JSON file. This becomes a hard FK constraint in
  Phase 4 once the Room schema lands.

Both deferrals are explicit per the data plan. If you push a topic with a
broken `imageUrl` or a dangling `relatedTopicId`, the build will still pass
today — please verify manually.

---

## Authoring a new topic (quick recipe)

1. **Pick a real thing** — verify against the relevant Wikipedia article that
   the artifact exists, has the claimed author/date, and has a reasonable
   image on Wikimedia Commons.
2. **Draft the `instruction`** first. If you can't write a 1–2 sentence prompt
   that passes the quality bar, the topic isn't ready — try a different one.
3. **Fill in the rest** (`teaser`, `actionPrompt.verb`, `targetName`,
   `durationMinutes`, `aliases`).
4. **Pick an `imageUrl`** from the article's Wikimedia Commons category page.
   If nothing good exists, set it to `""`.
5. **Add a SOURCES entry** to the PR thread listing the Wikipedia article +
   Commons file page you used.
6. **Run `./gradlew validateTopics`** locally before pushing.

For the full §6 LLM authoring prompt template, see `CURIO_DATA_PLAN.md` §6.

---

## End of SCHEMA.md
For rationale, rollout cadence, and the full LLM authoring prompt, see
[`app/CURIO_DATA_PLAN.md`](../../CURIO_DATA_PLAN.md).