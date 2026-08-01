# Request: Shuffle card + button size bump (Spin page)

## Request
Increase the size of the shuffle card a little and also its button a little.

## Analysis
- Spin page (`SpinScreen.kt`): the hero "ticket" card (`HeroTicketCard`) was 270×292dp inside a 420dp carousel Box; the center `SpinButton` was 118dp idle / 100dp landed (176dp outer container with an OrbitRing), with 68dp ShuffleGlyph and 56/48dp Casino dice icons.

## Changes (SpinScreen.kt)
1. HeroTicketCard: w 270→286dp, h 292→310dp (~+6%) — comment noted as v6.3.
2. Carousel container Box: 420→444dp so the bigger ticket keeps breathing room.
3. SpinButton: 118→126dp idle, 100→108dp landed (~+7%).
4. Dice glyphs: ShuffleGlyph 68→72dp; Casino 56→60dp idle, 48→52dp landed.

## Validation
- Code reviewer passed: peek-card geometry and the w+24/h+24 outer box derive from w/h so they scale automatically; OrbitRing radius derives from the 176dp container's minDimension so a 126dp button still fits; no other references to the old numbers.

## Completion summary
- Committed & pushed.
