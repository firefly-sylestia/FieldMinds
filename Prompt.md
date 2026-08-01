# Request: Remove box behind Spin bottom-bar Categories/Filter buttons and tint them

## Request
The Categories · Filter buttons in the Spin bottom bar are filled pills (a box/background behind them). Remove the box and make the buttons use the category tint.

## Changes
- `SpinScreen.kt` — `DeckControlButton` (both Categories and Filter buttons in `BottomCta`) is now backgroundless: `color = Color.Transparent`, no border, no shadow. Icon + label wear the theme-aware `categoryInk()` (accent in light / light twin in dark) at full strength when active and 68% alpha when idle, since the bottom tray already wears the `categoryBackgroundWash()`. A slim 22×3dp accent underline (also `categoryInk()`) is the only "active" affordance — visible when the button is selected, hidden otherwise — so there's no box to say selected.

## Validation
- Code reviewer confirmed clean: all referenced composables/imports (Spacer, Box, width, height, clip, background, Column, Alignment, Arrangement, FontWeight, RoundedCornerShape, categoryInk) already present in SpinScreen.kt, braces balanced, no leftover surface/border logic.

## Completion summary
- Committed & pushed.
