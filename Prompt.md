# Current Request

## Status: COMPLETED — committed and pushed to `revamp`

Two-part request:

1. "similar to white mode tint, make the dark mode tint follow a similar way —
   the dark should be more [visible], same way I implemented, but use the
   proper current color shades for dark mode."
2. "add a dialog in edit entry when I remove something by tapping the cross
   if I've drafted changes."

## Changes (2 files)

1. **`app/src/main/java/com/curio/app/ui/theme/CategoryInk.kt`**
   - `categorySurface()` dark branch: blend fraction raised from
     `tuning.blendFraction + 0.10f` → `+ 0.30f`. Dark cards now wear the
     proper dark mid-tone (the same per-family DARK_WASH_TUNING shades) at
     0.45–0.54 strength, so tiles/chips visibly carry their category tint on
     the midnight page — the same "cards = wash's stronger sibling"
     relationship light mode already has (light stays 0.24, unchanged). The
     page wash itself stays deep; only surfaces get the boost.
   - KDoc updated to note the dark mode blends markedly stronger.

2. **`app/src/main/java/com/curio/app/features/capture/SaveCaptureScreen.kt`**
   - The × on a take tab now confirms before removing when the take holds
     drafted content (`canSave && data != null`) or a live recording is
     running (`busy`) — in edit mode every take arrives prefilled, so the X
     never silently throws away drafted changes. Empty takes (and no live
     recording) still remove freely.
   - New `pendingRemoveIndex` state + AlertDialog ("Remove this take?" with
     **Remove** / **Keep editing**), mirroring the existing
     `pendingFormatSwitch` confirmation pattern.
   - Extracted shared `removeSection(i)` local fun (activeIndex fixup) used
     by both the direct-remove path and the dialog confirm, so the two can
     never drift apart.

## Review
- code-reviewer-deepseek-flash: clean on both changes — dark blend values in
  sane bounds (0.45–0.54), proper dark shades retained, contrast holds;
  remove dialog logic matches the direct-remove path, reset paths complete,
  no stale duplicated block (deduped per review). Noted (accepted): the
  GalleryWall per-tile × in a single-board mood edit is outside this scope —
  the request's "cross" is the take tab × in the edit-entry section strip.

## CI
- Compile gate = GitHub Actions on push (per AGENTS.md — no local Gradle).
