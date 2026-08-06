# Prompt — Detail page + entry tools batch (v7.39)

## Requests
1. Zoom overlay pinch/drag "works but not smooth, acts with a delay".
2. Detail: category still doesn't show below the tear; remove the
   "Captured today · time" meta line (put a very small time on the hero's
   date card); remove the dead voice-note play icon + enlarge its text.
3. Entry page: rounded corner + watermark only work on the title boxes —
   apply to ALL text boxes.
4. Entry page tools scattered (colors / styles / text format take too much
   space) — collapse them, one tool open closes the other, and put text
   format behind a text button so it doesn't always show.

## Fixes
- **MoodBoardZoom.kt** — pinch/pan deltas are now applied PER pointer event
  inside the gesture loop (previously accumulated and applied only when all
  fingers lifted → the image moved with a delay). Tap/movement
  classification + close semantics unchanged.
- **EntryDetailScreen.kt**
  - Category tucks at the tear: meta column lift -14dp → -32dp (tip now
    grazes the torn edge); bottom padding 16→8 and body vertical padding
    16→8 keep the tags→body gap identical.
  - "Captured today · time" line removed; hero Date FrostedSegment gained
    a `tiny` line rendering the time at 9sp. capturedAtLabel() deleted.
  - Voice-note header: dead circular PlayArrow icon removed; label bumped
    titleSmall → titleMedium (the real AudioPlayerBar stays below).
- **RichTextEditor.kt**
  - Paper boxes now forward `watermark` (both bases) and `roundedTop`
    (PaperCard) from the style — same decorations as the title fields.
  - Toolbar unified: one compact row with TEXT buttons — "Paper"
    (Palette) and "Format" (FormatText) — each reveals its panel and
    opening one closes the other; the B/I/highlight/size toolbar no longer
    always shows. StyleToggleButton replaced by ToolToggleButton; stale
    KDOC updated; unused Spacer import removed.
- **MarginaliaFormat.kt** — CI fix: restored the `Row` import (my earlier
  FlowRow edit dropped it; JournalVoiceNoteRow still uses Row).

## Review
Reviewer clean after KDOC refresh (4px pan dead-zone + voice-note indent
noted as acceptable cosmetics).

## Follow-up (v7.40)
Killed the zoom overlay's 4px drag dead-zone: pan/zoom deltas are now
applied to the state on EVERY pointer event (not only after the movement
threshold trips), so the first pixels of a drag move the image immediately
when zoomed in. The tap classifier became threshold-consistent
(`pinchScale > 1.01f || totalPan > 4px`) since sub-threshold jitter now
lands in pinchX/pinchY — tap-to-close still works. Reviewer verified
single-finger zoomChange is always 1f (no creep) and pan clamps to 0 at
base zoom. Committed + pushed.

## Follow-up (v7.41) — overlay pill never shows (real root cause) + warning cleanup

### Overlay pill: root-caused from user logcat
`app/logcat.txt` showed the REAL reason the floating pill never appears:

    E/ExploreSessionService: Unable to create overlay Compose owners; using notification only
    java.lang.IllegalStateException: You can 'consumeRestoredStateForKey' only after the
    corresponding component has moved to the 'CREATED' state
        at ExploreSessionService$OverlayOwner.<init>(ExploreSessionService.kt:157)

Diagnosis (verified against AOSP source + the resolved AARs):
- The toml pins savedstate 1.3.3 but lifecycle-runtime 2.10.0 / activity
  1.13.0 pull savedstate **1.4.0** transitively, and gradle takes the max.
- savedstate 1.4.0 rewrote SavedStateRegistryImpl: `performAttach()` now
  registers the **Recreator** lifecycle observer immediately, and that
  observer calls `consumeRestoredStateForKey("androidx.savedstate.Restarter")`
  on ON_CREATE. `consumeRestoredStateForKey` is now guarded by
  `check(isRestored)` — and `isRestored` is ONLY set by `performRestore()`.
- The OverlayOwner only called `performAttach()` then drove ON_CREATE →
  Recreator fired → check failed → `showBubble()` caught it, latched
  `bubbleUnavailable = true`, and silently ran notification-only. The pill
  never shows, even after clean install.

Fix (ComponentActivity's documented contract):
    savedStateController.performAttach()
    savedStateController.performRestore(null)   // NEW
    registry.handleLifecycleEvent(ON_CREATE/START/RESUME)
`performRestore(null)` marks isRestored=true (restoredState=null), so
Recreator's consume returns null and no-ops. Null literal compiles against
both 1.3.3 (`Bundle?`) and 1.4.0 (`SavedState?`) signatures. `bubbleUnavailable`
stays as an OEM-rejection safety net.

### CI warning cleanup (from pasted build log)
- AppPreferences.kt — `@Suppress("DEPRECATION")` on overlayActuallyUsable
  (unsafeCheckOpNoThrow has no stable non-deprecated replacement).
- CaptureData.kt — `@file:Suppress("UNNECESSARY_SAFE_CALL")` (defensive ?.
  on non-null Gson-legacy fields — guards kept, warning silenced).
- CurioBackupManager.kt — `@Suppress("SENSELESS_COMPARISON","USELESS_ELVIS")`
  on restore() (legacy-blob null guards).
- MarginaliaFormat.kt — `path?.let` → `path.let` (AudioRecorder.stop():
  String non-null; dropped the dead `?: 0L`).
- PaperCard.kt — quadraticBezierTo( → quadraticTo( ×7 (deprecation message
  names the replacement; same signature).

Review: clean. Committed + pushed.

## Follow-up (v7.41b) — CI compile fix: EntryDetailScreen @Composable placement
CI failed: "Functions which invoke @Composable functions must be marked with
the @Composable annotation" (EntryDetailScreen.kt:1117) + "@Composable
invocations can only happen from the context of a @Composable function"
(1127). The v7.40 hero-date edit had stranded the @Composable annotation
between two KDocs: it landed on the PLAIN helpers heroDateTinyLabel and
moodOf() (both String/JournalMood? formatters that compiled as
@Composable-returning-values, masking the issue) while the real composable
FrostedSegment (uses Column/Text/CurioIcon) lost its @Composable entirely.
Also removed the orphaned "Theme-aware entry meta card" KDoc (that card was
removed in the v7.39 batch) whose @Composable had strayed onto moodOf, and
collapsed a doubled /** + double blank line. Fixed, review clean, pushed.

## v7.42 — detail view: boxless notes, tighter paper-card gaps, artwork shows behind
1. **Paper-card gaps** — the detail body's paper boxes (journal + hand-placed
   quote notecards of mixed styles/colors) stacked at 16dp; reduced to 12dp in
   SoundBiteRender, ReelNotesRender, MarginaliaRender, FieldNotesRender
   (PortfolioRender's 14dp untouched).
2. **Voice note box gone** — SoundBiteRender's tinted Surface ("the white
   layer") is now transparent with a 0dp shape (no clip on torn edges); the
   "Voice note · 12s · 1.2MB" label + title row + encodingFormat quality chip
   deleted; the capsule AudioPlayerBar + transcribe chip + note + quote cards
   now sit directly on the page wash. Dead formatFileSize() helper removed.
3. **Category artwork behind** — with the opaque box gone, the root-level
   CurioWatermarkBackdrop glyphs read through behind the voice note + quotes
   ("place the artwork behind the white layer" — that layer is the removed
   box). QuickFactCard was already backgroundless.
Reviewer caught that the transparent Surface still clipped to RoundedCornerShape(20.dp),
which would have shaved the torn paper corners — set to 0dp. Pushed.

## chore — GitHub Actions Node 20 deprecation
Bumped the workflow actions to Node-24 majors: actions/checkout@v4→v5 (3x),
actions/setup-java@v4→v5 (3x), actions/upload-artifact@v4→v5 (2x) in
android.yml + release.yml. softprops/action-gh-release@v2 left as-is (3rd-party,
not flagged).

## feat — artworks batch 3: last 21 fake descriptions replaced with real facts
New scripts/batch_artworks_3.py replaced the 21 remaining boilerplate artwork
teasers ("The kind of work that rewards patience…") with real, verified
art-history facts — teaser + exploreAction.instruction + accurate tags for each
(Night Watch, Las Meninas, Death of Marat, Raft of the Medusa, Great Wave,
Olympia, Impression Sunrise, Nocturne/Falling Rocket, Starry Night, Scream,
Demoiselles d'Avignon, Composition VII, Fountain, Persistence of Memory,
Autumn Rhythm, Marilyn Diptych, Balloon Dog, Sunflower Seeds, Infinity Mirror
Room, My Bed, The Gates). Corrected 5 display names to official titles
(Impression, Sunrise; Nocturne in Black and Gold: The Falling Rocket; Autumn
Rhythm (Number 30); Balloon Dog (Orange) (1994-2000); Infinity Mirror Room -
Phalli's Field). Script is id-guarded (expectName, SKIP on mismatch; the
olympia id is artw-olympia-1863-by-édouard-89 — ids never renamed). Also
synced the 5 titles in scripts/expand_topics.py ARTWORK_NAMES seed list.
Review pass fixed two fact nits: Marat "within days" → "in the months after"
(finished Oct 1793), and Balloon Dog "more than a car" → "roughly a tonne".
All 56 artworks now have real descriptions; 0 boilerplate left. Pushed.

## v7.43 — voice-note label back, recording visualizer fixed, tighter editor chips
Requests: (1) restore the voice-note text above the voice note on the detail
page; (2) the recording visualizer only had its LAST bar reacting, the rest
stayed still; (3) tighten the "huge spaces" between the paper chips (Ruled /
Torn / +Coffee / … / Color) and the tag + mood chips in the editing screen.

1. **EntryDetailScreen.kt** — the v7.42-removed voice-note label is back:
   "Voice note · 12s · 1.2MB" (mic glyph + titleSmall SemiBold, category ink)
   with the optional saved title and the encodingFormat quality chip, sitting
   right ABOVE the capsule AudioPlayerBar (same audioFilePath condition).
   Re-added the deleted private formatFileSize() helper next to formatMs().
2. **CurioAnimations.kt LiveWaveform** — the history loop decayed every bar
   toward the floor in a few frames, so only the last (front) bar ever moved.
   Replaced with a true ring-buffer shift: every frame each bar inherits its
   right neighbour and the newest mic level eases into the front bar, so the
   whole 36-bar row ripples with a trailing tail. Idle (pause/stop) keeps the
   old fast-settle behavior: every bar eases to the 0.06–0.2 quiet floor.
3. **PaperCard.kt** — NotePaperStyleToggle merged its two FlowRows (base Ruled /
   Torn + decorations) into ONE tight 4dp chip cloud (was 5dp + a row gap), so
   the base and +Coffee/+Folded/+Red Margin/+Watermark/+Rounded top buttons sit
   close together; CompactPaperChip padding 7×3 → 6×2; NotePaperColorToggle
   column + swatch row 6dp → 4dp. Kdoc updated to match.
4. **CaptureFormatComponents.kt MoodChipsRow** — chip row 8dp → 6dp.
5. **SaveCaptureScreen.kt TagEditorRow** — section column 8dp → 6dp, chip flow
   6dp → 5dp, tag-input row 8dp → 6dp.

Also committed the uncommitted scientists.json batch (scripts/batch_scientists.py)
from the earlier enrichment pass so it stops riding along as a dirty tree.
Brace-check script BALANCED on all five touched files; no Gradle build run
(CI owns compilation).

## Follow-up (v7.44) — voice-note label back to titleMedium, title on its own line
User wanted the restored label bigger — titleMedium (the pre-v7.42 size)
with the saved title on its OWN line below it. Restructured the v7.43 label
into a Column: primary row = 18dp mic glyph + "Voice note · 12s · 1.2MB"
(titleMedium SemiBold, category ink, weight(fill=false)) + the encoding
chip; the title renders below on its own line (bodySmall, muted,
indented 26dp to align under the label text). Braces BALANCED; pushed.

## (v7.45) authors batch — real bios for all 95 fake authors + cap 280 → 450
Replaced all 95 template-authored entries in authors.json ("Author Topic #36
wrote their first published work…", "Often cited but rarely fully
understood…" etc.) with real, verified literary biography via
scripts/batch_authors.py: real teasers, real instructions, corrected
targetName (real works), and corrected tags (was: random genre/century
pairs like Poetry+19th Century on a contemporary novelist). name / byline /
verb / durationMinutes / tier preserved.

User pushed back on trimming the 10 longest entries: the 280-char cap in
validate_topics.py was STALE — the real Gradle validateTopics task (what CI
runs) enforces instruction <= 450 and doesn't cap teasers at all, and shipped
data already ran to 417 (teaser) / 399 (instruction). So instead of cutting
content, raised the schema cap everywhere to 450:
  - scripts/validate_topics.py: MAX_INSTRUCTION_LEN 280 → 450 (matches
    app/build.gradle.kts validateTopics; docstring already claimed 450)
  - app/src/main/assets/topics/SCHEMA.md + app/CURIO_DATA_PLAN.md: 280 → 450
  - scripts/batch_authors.py: _trim safety net 280 → 450; the 10 previously
    trimmed entries restored to full text (Jackson's "in one sitting",
    Vuong's nail-salon detail, Twain's full Huck quote, Díaz's "DJ
    interruptions", etc.)
Validation now passes CLEAN: 1962 topics across 11 files, 0 errors (bonus:
cleared the 486 pre-existing over-280 errors that had been piling up in
albums/artists/etc. since the earlier rich batches). Committed & pushed.

## (v7.46) fake-teaser cleanup — audit + wildcard batch 1/3 (40 entries)
Audited all 11 topic files for template-generated entries (boilerplate teasers
"Often cited but rarely fully understood…", "Hiding in plain sight…", etc.).
Remaining fake counts: wildcard 118, books 106, discoveries 87, painters 77,
directors 73, films 67, artists 1 (albums/artworks/authors/scientists CLEAN).
User wants file-by-file cleanup in batches of 30–40.

Batch 1: scripts/batch_wildcard_1.py — 40 wildcard entries (ids 115–154:
KonMari, Bullet Journal, Voynich, Nazca, Terracotta, Angkor Wat, Moai,
Chichén Itzá, Great Wall, Forbidden City, Sagrada, Golden Gate, Northern
Lights, Victoria Falls, Reef, Sahara, Yosemite, Galápagos, Seed Vault, LHC,
JWST, Voyager 1, Dead Sea Scrolls, Bayeux, Magna Carta, Constitution, Turing
Test, Butterfly Effect, Placebo, Lucid Dreaming, Uncanny Valley, Fibonacci,
Alexandria, Atlantis, Bermuda Triangle, Tunguska, Taos Hum, Roanoke). Each got:
real teaser (quirky verifiable fact), specific targetName (archive/museum/
simulation/photograph), quality-bar instruction, real 2–3 tags (was placeholder
pairs like Oddity|Historical), and a proper subtype (Artifact/Site/Practice/
Event/… was blanket "Curiosity"). Validation 0 errors; committed per-batch.

## (v7.46 cont.) wildcard + books now CLEAN; discoveries next
wildcard.json: all 118 fakes replaced (3 batches: 40+40+38) with real facts,
proper subtypes, real tags. books.json: all 106 fakes replaced (3 batches:
40+44+22) — 2023 releases and the classics (Moby-Dick, Anna Karenina,
Gatsby, Lolita, One Hundred Years of Solitude, …), real teasers + reading
instructions + corrected tags. Validation still 0 errors. Next up in the
queue: wildcard + books + discoveries are now CLEAN (0 fakes each, validation 0
errors throughout).painters + directors are now CLEAN (painters 40+37, directors 40+33).

## (v7.46 end) ALL 11 topic files CLEAN — films + artists finished
films.json: all 67 fakes replaced (2 batches: 40+27) with real facts +
correct byline (director — the fakes lacked bylines entirely) + corrected
tags (Close Encounters, Blade Runner, Spirited Away, 2001, Matrix, Inception,
Titanic, Get Out, Dune, Tár, …). artists.json: the last "fake" (Lana Del
Rey) turned out to be a FALSE POSITIVE — its teaser is already real,
high-quality content that merely contains "critics dismissed" mid-sentence;
same for wildcard's Overview Effect ("original interviews" appears naturally
in a valid instruction). Strict boilerplate sweep ("Cientit Topic #",
"often cited but rarely fully understood", "Widely discussed yet still full
of surprises", "The kind of work that rewards patience", "Hiding in plain
sight", "A fascinating figure whose work rewards close attention") now
returns **0 across all 1962 topics / 11 files**; validate_topics.py: 0 errors.
Summary of the whole campaign: authors 95 (1 batch) + wildcard 118 (3) +
books 106 (3) + discoveries 87 (3) + painters 77 (2) + directors 73 (2) +
films 67 (2) = **623 template entries replaced with real, verified content**
(plus the earlier scientists + artworks batches). Scripts kept in scripts/
batch_*.py; every batch committed & pushed individually.

## (v7.47) final 20 leftover template entries — handcrafted, fun facts
A deeper audit (never-touched entries + second template family) found 20
more entries that had slipped through: 12 films with **duplicated template
teasers** (identical sentences: "The most memorable scene was improvised on
set...", "This film was made for roughly the cost of a modest house...") and
scrambled tags (Comedy|1980s on Psycho, Animation|2000s on The Seventh
Seal), plus 8 entries from a *different* template generator family ("written
in a 6-week creative burst", "my feet were doing the thinking", "What makes
this so fascinating is that it shouldn't exist...").

scripts/batch_templates_cleanup.py replaced all 20 with handcrafted
descriptions: Singin' in the Rain, The Seventh Seal, Psycho, Lawrence of
Arabia, Good/Bad/Ugly, Once Upon a Time in the West, Clockwork Orange,
Chinatown, Oppenheimer, Killers of the Flower Moon, Poor Things, Anatomy of
a Fall, The Godfather, Fear and Loathing in Las Vegas, CMB, Exoplanets,
Ikebana, Wabi-Sabi, Lagom, Sisu. Real fun facts (Psycho's 70 camera setups,
Sisu and the Winter War, Puzo's $12,500 movie-rights sale, the Planck sky
map ripples), proper specific targetNames, and corrected tags. The 7
"mismatched" scientist/painter ids were checked and are FALSE positives —
content matches the name field (ids like scientist-gauss-anecdote → Emmy
Noether are just ugly generator slots). Full template sweep (12 families,
30+ phrases) now returns **0 across all 1962 topics**; validate 0 errors.
Committed + pushed.
