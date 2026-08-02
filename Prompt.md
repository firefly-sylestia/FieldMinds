# Current Request

## Status: COMPLETED — committed and pushed to `revamp`

"Remove the tinted background behind the category and filter card — the
tinted strip above the nav bar in the Shuffle page, between the main spin
background and the Categories · Filter buttons."

## Clarification flow

The user first asked to remove a tinted background behind "the category and
filter card," then clarified (via ask_user, twice): it is NOT the card/chip
surfaces themselves, and NOT the page wash — it is the **BottomCta tray**
on the Spin (Shuffle) page: the band sitting directly above the nav bar
that holds the Categories · Filter buttons, which wore
`cat.categoryBackgroundWash()`.

Confirmed scope: **only that bottom tray** on the Spin page — page
background wash, picker/filter sheets, and other screens untouched.
Confirmed replacement: **no background at all** (transparent).

## Change (1 file)

**`app/src/main/java/com/curio/app/features/spin/SpinScreen.kt`**
- `BottomCta` Surface color changed from `cat.categoryBackgroundWash()` to
  `Color.Transparent` — the Categories/Filter buttons now sit directly on
  the Spin page background, with no tinted band between them and the nav
  bar. Comment updated accordingly.
- `navigationBarsPadding()` and the `DeckControlButton`s are unchanged.
- No import/param fallout: `categoryBackgroundWash` still used for the page
  background + CategoryPickerSheet; `cat` still passed to DeckControlButton;
  `Color` already imported.

## Review
- code-reviewer-deepseek-flash: clean — no unused imports/params, visual
  result matches the user's confirmed "no background at all" choice. No
  further changes needed.

## CI
- Compile gate = GitHub Actions on push (per AGENTS.md — no local Gradle).
