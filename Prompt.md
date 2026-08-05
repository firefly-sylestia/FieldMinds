# Prompt.md — Request Log

## Latest Request (COMPLETED)

**Home: backgroundless quest card + fix pastel icon/tag colors**

### Requested

- "Today's Quest / Shuffle the deck" should have no card background and no
  leading icon — just the text with its shuffle button.
- The row icons for titles and the "Unexplored" etc. tag texts on Home use
  different category colors that look bad in pastel mode — fix them.

### Plan

1. `QuestShuffleCard`: background → `Color.Transparent` (whole row stays
   tappable), remove the leading 54dp Casino icon box, trim vertical
   padding 16→12dp.
2. Home row icons (Saved quote, Pinned topic, Recent entry, Explore
   topic, Queued explore) + tag text + forward arrows: switch from the
   pastel `themedAccent()` to the deep `categoryInk()`.
3. Give the Unexplored/Resumed tag chips the detail page's hairline rim
   (border accent@40%) for consistency.

### Outcome

- `HomeScreen.kt`: quest card stripped to bare text + shuffle button; all
  row icons/tags/arrows now use deep category ink (readable in pastel);
  tag chips got the hairline border. Braces balanced; code-reviewer-glm
  approved (only remaining accent tint is the decorative card watermark).
