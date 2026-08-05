# Prompt.md — Request Log

## Latest Request (COMPLETED)

**Shuffle page: creator tag pills on the hero card + drop "by …" from artwork names**

### Requested

- Add the tag-pill style to the shuffle page category (hero ticket) card,
  with the creator name shown as a top-corner tag.
- Films → "Director", books → "Author", artworks → "Painter", albums →
  "Artist", each as a `Label · Name` pill.
- Remove the trailing " by [painter]" from artwork names.
- ("add 1 in the top corner" clarified by user = the creator-name pill,
  not a numeric badge.)

### Plan

1. `SpinScreen.kt` — `HeroTicketCard`: new creator byline pill pinned
   `Alignment.TopStart` (padding 20dp) in the band the old subtype badge
   owned (content column's 28dp spacer keeps the title clear). Label comes
   from the TOPIC's own `categoryId` so mixed decks stay correct. Text-only
   pill, `ink@18%`, `labelMedium` bold, maxLines 1 ellipsis. Matches the
   Topic Reveal byline-pill language.
2. `artworks.json` — strip the trailing `" by <byline>"` suffix from all
   56 artwork names (byline field kept; two-pass byte-safe replacement for
   escaped vs literal UTF-8 names).

### Outcome

- Pushed `16740d4a` ("feat: creator byline tag pill on shuffle hero card,
  strip painter suffix from artwork names"). Braces balanced, JSON valid,
  `CategoryId` import present, no code parses the " by " suffix.
- code-reviewer-glm approved: pill/title clearance exactly flush, null-safe
  for byline-less topics. Minor note: the Artist/Author/Director/Painter
  mapping is duplicated in `TopicRevealScreen.kt` — acceptable at 4 lines.
- Note: earlier in this cycle the paper batch was pushed as `8e66f19e`
  (realistic dog-ear, soft all-sides torn paper, watermark paper style,
  refined coffee, roomier saved notes, detail-style home hero scatter).
