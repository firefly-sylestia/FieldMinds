# Prompt — Peek shadow fix + detail hierarchy (v7.38)

## Request (two parts)
1. "the experiment of the peek card shadow well they are bad so fix that shadow look" — the Settings "Deck cards → Soft shadows" experiment's peek-card shadows look bad.
2. "the category doesnt have a differnt hierarcy that the quik fact make the quick fact more smaller and its still doesnt look like its behind the tear incrase its size. and dont give it the entrace animation"

## Changes
### SpinScreen.kt (PeekCard)
- Replaced the single `Surface.shadowElevation` (near 5dp / far 4dp — read as a hard dark halo hugging every fanned card, muddy black rings on the deck) with the hero's proven LAYERED soft shadow on the outer Box modifier (before graphicsLayer, clip=false): a broad ambient glow tinted with the card's accent (14/10dp, alpha 0.14/0.10) + a tight dark contact shadow (5/3dp, black 0.18-0.24/0.12-0.16). Far cards sit lower/fainter; Surface stays flat. `shadowsOn` toggle still controls it.

### EntryDetailScreen.kt (meta column)
- Hierarchy: category is now the PRIMARY line — titleLarge ExtraBold + 22dp glyph (was labelLarge + 16dp), so it clearly outranks the quick fact AND its bigger tip reads tucked behind the tear. Quick fact shrunk to SECONDARY: labelMedium semibold heading + 14dp icon + bodyMedium teaser (was titleSmall/16dp/bodyLarge), "…more/…less" labelMedium.
- Entrance animation: the category and the quick fact are both STATIC (no MorphEntrance — a scale-in pop fights the tucked-behind-the-tear look and the user asked to drop it); only the captured-at + tags block keeps MorphEntrance. Outer meta Column keeps the -14dp lift so the category tip tucks under the paper lip.

## Review
Reviewer clean; flagged the "it" ambiguity (category vs quick fact) — resolved by making BOTH static and keeping the entrance only on captured-at/tags.

## Status
DONE — implemented, reviewed, Prompt.md updated, committed + pushed.
