# Prompt.md — Request Log

## Latest Request (COMPLETED)

**Add Home-style pop-up + sticky top bar to the Entry Detail screen**

### Requested

- Mirror the Home screen's menu / profile pill pop animation and sticky
  style on the detail screen page (its back / more controls).

### Plan

1. Hoist the detail screen's scroll state so a pinned bar can read it.
2. Move the in-hero back / more controls out of the scroll content into a
   sticky top bar pinned in the root Box.
3. Drive the pop from the same scroll-linked clock Home uses
   (`FastOutSlowInEasing` over a 90dp threshold): scale 0.97→1, ride-up
   from the glyph band (72dp) to the top edge (12dp), shadow 0→6dp.
4. Give `CurioBackButton` an optional `shadowElevation` param (default 0).

### Outcome

- `EntryDetailScreen.kt`: hoisted `detailScroll`; controls moved to a
  sticky Row outside the scroll content; scroll-scrubbed pop with layout-
  space `Modifier.offset` ride-up (critical: a draw-time graphicsLayer
  translation would leave the more-menu's DropdownMenu anchored 60dp below
  the popped pill, since popups anchor to layout position).
- `CurioTopBar.kt`: `CurioBackButton` gained `shadowElevation: Dp = 0.dp`
  (backwards compatible; 11 other callers unaffected).
- Braces balanced; code-reviewer-glm approved (flagged popup-anchor concern
  as correctly handled).
