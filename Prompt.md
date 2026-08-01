# Prompt.md — Request Log

## Current Request: Audio not playing in saved entries

**User request (verbatim):** "the aduio isnt playing in the saved entry"

## Root cause
`AudioStorageManager.persistAudio` stores audio as a **raw absolute filesystem path** (`filesDir/audio/{entryId}.m4a` → `destFile.absolutePath`). `EntryDetailScreen.AudioPlayerBar` fed that string straight to `MediaItem.fromUri(audioFilePath)` — a bare path parses as a **schemeless URI** that ExoPlayer's `DefaultDataSource` cannot resolve, so playback silently failed. (The waveform still rendered because `WaveformExtractor` uses `MediaExtractor.setDataSource(filePath)` which accepts raw paths natively — hence "waveform visible, no audio".)

## Fix
`features/detail/EntryDetailScreen.kt` — `AudioPlayerBar` now builds `audioUri` via `remember(audioFilePath) { val parsed = Uri.parse(audioFilePath); if (parsed.scheme != null) parsed else Uri.fromFile(File(audioFilePath)) }` and keys the player on it. Added imports `android.net.Uri` + `java.io.File`. Passes through already-schemed URIs (content://) defensively.

## Validation
- code-reviewer-deepseek-flash: clean — imports correctly ordered, remember keys sound, player disposal fine (DisposableEffect keyed on player), no other `fromUri` raw-path call sites; only nitpick was the defensive schemed-URI branch (harmless, currently unreachable since every producer stores raw paths).
- No local gradle per AGENTS.md — CI on push is the compile gate.

## Status
DONE — committed & pushed.

---

## Previous Request: Spin page — bottom bar tint wash + dice always filled

**User request (verbatim):** "i like the color in background in spin page and caan u also make the buttom bar have that color too otherwise it looks odd and make the diec button always have color the first dice which have filled color make the 2nd state when it says tap to open make that dice also the same color"

## Changes
`features/spin/SpinScreen.kt`:
1. **BottomCta** — surface color changed from `MaterialTheme.colorScheme.surfaceContainerLow` (tonalElevation 3dp) to `lerp(MaterialTheme.colorScheme.background, cat.accent, 0.20f)` (tonalElevation 0dp), so the bottom tray wears the SAME category-tint wash as the page background instead of reading as a separate odd-colored bar. Matches the root Box wash exactly in light AND dark mode.
2. **SpinButton** — surface color now always `tint` (was neutral `surfaceContainerHigh` when landed), and the Casino dice icon tint now always `Color.White` (was accent-tinted when landed). The dice button stays filled with the category color in BOTH states (idle "Press Shuffle" and landed "Tap to open"), with a white dice on top.

## Validation
- code-reviewer-deepseek-flash: clean — `lerp` import present, `cat` in scope in BottomCta, composite exactly reproduces the root wash, `landedTopic` still used (button/dice sizing), no dead params/imports. Minor non-blocking note: Wildcard coral (light pink) has lower white-dice contrast, but that was already the idle-state design the user asked to replicate.
- No local gradle per AGENTS.md — CI on push is the compile gate.

## Status
DONE — committed & pushed.

---

## Previous Request: Mood board image drag janky when editing an entry

**User request (verbatim):** "the mood board drag is still buggy and jittery i meant when editing ir entry the image drag is so janky"

## Root cause
`MoodBoardEditorTile`'s `awaitEachGesture` drag handler gated **every** pointer-move event by touch slop (`dragAmount.getDistance() >= slop`). At 60–120Hz, per-frame deltas during slow/moderate drags sit far below slop, so the tile only moved when a single event happened to cross it — producing sticky, stuttering, jittery dragging.

## Fix
`features/capture/formats/GalleryWallFormat.kt` — capture the down position (`val down = awaitFirstDown(...)`); the slop check now gates **only the drag start** via total travel from the down point `(change.position - down.position).getDistance() >= slop`. Once `dragged` is true, every event delta applies 1:1. Consumption stays inside `if (dragged)` (taps / parent scroll unaffected within slop), pin-to-front zone logic intact, lift-without-slop still exits cleanly.

## Validation
- code-reviewer-deepseek-flash: clean — correct `down` usage, 1:1 first-move delta with no double-count, consumption correct, pin-zone intact; other drag handlers (`detectTransformGestures` zoom overlays, `moodBoardPinchZoom`) handle slop internally and do NOT share this bug.
- No local gradle per AGENTS.md — CI on push is the compile gate.

## Status
DONE — committed & pushed.

---

## Previous Request: Home hero card doesn't match cream background

**User request (verbatim):** "the home hero card doesnt matc te roer color fix it"

## Root cause
The Home hero card (`CurioHeroCard.kt`) uses `CurioGradients.cardGradient()`, whose light-mode gradient end was still pure `Color.White` — after the previous change made the light background SoftCream (`#F7F0E4`), the hero card (and every other card-gradient consumer: category cards, TopicReveal, EntryDetail, Profile, quest cards) faded toward white and clashed with the cream surface.

## Fix
`CurioColors.kt` — `CurioGradients.cardGradient()` light-mode end changed `Color.White` → `CurioColors.SoftCream`, so all card gradients now wash into the cream background. Dark mode end (`Color.Black`) untouched. Doc comment updated.

## Validation
- code-reviewer-deepseek-flash: clean — `CurioColors.SoftCream` resolves from sibling `CurioGradients` object, `cardGradient` already @Composable, dark mode untouched. Optional note (not taken): dark gradient fades to pure black vs midnight `#0B1018` — pre-existing, out of scope.
- No local gradle per AGENTS.md — CI on push is the compile gate.

## Status
DONE — committed & pushed.

---

## Previous Request: Light-mode cream background + Spin-page category tint

**User request (verbatim):** "make te ap ligt mode white color a less white color not dark color that creamy color but not tat black and like add the card category tint to it only in spin page"

## Clarified via ask_user
1. **Cream level:** Soft cream (`#F7F0E4`) — gentle warm off-white, barely not white, not dark/black.
2. **Spin tint scope:** Background wash — the whole Spin page background gets a subtle wash of the active category's tint behind the deck.

## Changes
- `ui/theme/CurioColors.kt` — added `SoftCream = Color(0xFFF7F0E4)`; reworded `CreamWhite` comment (now ink/decoration only, no longer a surface).
- `ui/theme/CurioTheme.kt` — light scheme `background`/`surface`/`surfaceContainerLowest` → `SoftCream`. Container steps deepened (Variant/Low/Container/High/Highest) so cards/sheets stay distinct on the cream surface. Dark scheme untouched.
- `features/spin/SpinScreen.kt` — root Box adds `.background(deckCat.tint)` after the theme background, so **only** the Spin page wears the category-tint wash (subtle 20% alpha over both cream light and midnight dark).
- `app/AGENTS.md` — recorded the durable user design preferences under UI section.

## Validation
- code-reviewer-deepseek-flash (2 passes): clean — modifier layering correct (background paints bottom, tint over it, content above), hierarchy coherent, no dark-mode drift, no broken references. Nitpick fixed (CreamWhite comment).
- grep: SoftCream used in light scheme; only SpinScreen has the tint wash (all other screens keep plain `colorScheme.background`).
- No local gradle per AGENTS.md — CI on push is the compile gate.

## Status
DONE — committed & pushed.
