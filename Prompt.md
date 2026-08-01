# Current Request

## Status: IN PROGRESS — edits applied, review passed, commit pending

"the background peek cards need to be more larger so that the title the topic it shows doesn't hide, don't change the dimension of the card just increase the size of it and also make the main card bounce animation better like its tilting too much and bouncing too much in spin just make it bounce a little"

## Changes (1 file)

1. **`app/src/main/java/com/curio/app/features/spin/SpinScreen.kt`**
   - **Peek cards ~13% bigger (proportions kept, size only):** near
     318×102 → 360×116, far 288×84 → 328×96; corners scale with height
     (17→19 / 13→15); fan yOffs nudged outward (−178/−134/146/188) so the
     topic title inside each background card has room to read instead of
     hiding behind the fan.
   - **Gentler hero bounce:** per-tick kick 1.065 → 1.035 with a more
     damped spring (0.7/1000), tilt factor 80 → 40 (max ~5.2° → ~1.4°),
     hop factor 30 → 18, category-switch bounce 1.045 → 1.025, landing
     rest scale 1.04 → 1.02. Header doc gets a v6.5 note.

## Review
- code-reviewer-deepseek-flash: clean — no stale old values in live code
  (only the v6.4 historical doc line records the old sizes), types correct.
  Notes: bottom far peek hangs ~10dp past the carousel edge into the 32dp
  padding gap above the SpinButton (no clip, no crowding); peek titles still
  ellipsize on very long names (out of scope — request was size only).

## CI
- Compile gate = GitHub Actions on push (per AGENTS.md — no local Gradle).
