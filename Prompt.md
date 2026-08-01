# Current Request

## Status: IN PROGRESS — edits applied, review + commit pending

"make the shuffle card background card size increase too and bring back the button category and filter button card style back from this commit just that nothing else https://github.com/firefly-sylestia/FieldMinds/pull/244/commits/5b489fdad7e76bda775147734a06cabc5b8ba0d5 and make the stars have an outline and remove the home time and also remove the cabinet all tint. keep it as it is without the tint"

User clarifications (ask_user):
- Stars: FILLED + outline (not hollow outline-only).
- "home time" = the Home category TINT (NOT the greeting — greeting stays).
- Cabinet: remove ONLY the page-level wash; keep chip/card tints.
- Dark-mode rose/coral tint → #5e0034.

## Changes (5 files)

1. **`app/src/main/java/com/curio/app/features/spin/SpinScreen.kt`**
   - `DeckControlButton` (Categories/Filter bottom buttons) reverted to the
     pre-5b489fda pill-box style: solid accent fill + white content when
     selected; solid surface fill + accent ink + border when unselected.
   - Peek cards (shuffle-deck background cards) grew ~6% to match the v6.3
     hero ticket: 300×96 → 318×102 (near), 272×78 → 288×84 (far), corners
     16 → 17 / 12 → 13. Header doc gets a v6.4 note.

2. **`app/src/main/java/com/curio/app/features/capture/formats/CaptureFormatComponents.kt`**
   - `FilledStar` now draws the solid fill PLUS an outline stroke (~7% of
     star size; 0.85 alpha when filled, 0.45 when ghost) so stars read
     outlined. Added `androidx.compose.ui.graphics.drawscope.Stroke` import.

3. **`app/src/main/java/com/curio/app/features/home/HomeScreen.kt`**
   - Home category tint removed: page background is the plain theme
     background; all `homeTintSurface(...)` call sites reverted to base
     surface colors; the helper, `washCat` and `homeTintOn` deleted; unused
     `categoryBackgroundWash` / `categorySurface` imports removed.

4. **`app/src/main/java/com/curio/app/features/cabinet/CabinetScreen.kt`**
   - Page-level category wash removed (plain theme background). Chips,
     search pill and cards keep their category surfaces/borders.
   - Removed unused imports (`foundation.background`, `categoryBackgroundWash`).

5. **`app/src/main/java/com/curio/app/ui/theme/CategoryInk.kt`**
   - Dark-mode wash tuning: MOVIES (rose) + WILDCARD (coral) families now
     deepen toward `deepTwin = #5E0034` (darken 0.60) instead of black /
     #BE185D — dark burgundy rose/coral tint in dark mode.

## Notes
- Home "tint" settings toggle still exists in Settings (now inert for
  Home); the global tint toggle still affects other screens. Left out of
  scope per user request.
- Greeting hero left untouched (user clarified "home time" = tint).

## Review
- code-reviewer-deepseek-flash: pending

## CI
- Compile gate = GitHub Actions on push (per AGENTS.md — no local Gradle).
