package com.curio.app.features.spin

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.curio.app.data.AppPreferences
import com.curio.app.data.CategoryId
import com.curio.app.data.CurioCategories
import com.curio.app.data.CurioCategory
import com.curio.app.data.CurioRepositoryHolder
import com.curio.app.data.CurioTopic
import com.curio.app.data.SmartDensityMode
import com.curio.app.data.StreakTracker
import com.curio.app.data.TopicJsonLoader
import com.curio.app.navigation.CurioRoutes
import com.curio.app.ui.components.ConfettiBurst
import com.curio.app.ui.components.CurioBackButton
import com.curio.app.ui.components.CurioCategoryCard
import com.curio.app.ui.components.CurioNavTint
import com.curio.app.ui.components.CurioWatermarkBackdrop
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioMixedDeck
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.CurioMotion
import com.curio.app.ui.theme.categoryBackgroundWash
import com.curio.app.ui.theme.categoryBorder
import com.curio.app.ui.theme.categoryInk
import com.curio.app.ui.theme.categorySurface
import com.curio.app.ui.theme.isCurioDarkTheme
import com.curio.app.ui.theme.lightAccentTint
import com.curio.app.ui.theme.onAccent
import com.curio.app.ui.theme.pastelFillInk
import com.curio.app.ui.theme.themedAccent
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import com.curio.app.ui.components.MorphEntrance
import kotlin.random.Random

/**
 * The Spin — see CURIO_SPEC.md §5 (v5 redesign).
 *
 * v5 changes:
 *  1. **Simplified top bar** — back button, category name, topic count only.
 *  2. **Category picker moved to bottom** — "Categories" pill button in the
 *     bottom bar opens a beautiful tile-grid bottom sheet (like the Explore
 *     page) for switching categories.
 *  3. **Filter moved to bottom** — "Filter" pill button next to Categories.
 *  4. **Compact filter sheet** — redesigned with tighter spacing, toggle
 *     chips, and a clean apply button.
 *  5. **Unified bottom bar** — Categories · Filter · Shuffle all in
 *     one row for quick one-thumb access.
 *
 * v5.1 changes:
 *  6. **Fan-deck carousel** — redesigned shuffle cards: a tall paper
 *     "ticket" hero card (watermark glyph, subtype badge, name, tags,
 *     teaser, tap hint) with slim prev/next pill cards fanned above and
 *     below like a slot window.
 *  7. **Tap-to-open** — the landed card opens the topic directly (no
 *     Explore button); the bottom CTA becomes "Spin again".
 *
 * v5.2 changes:
 *  8. **Tap-open landing** — after the shuffle settles the landed card
 *     stays in place until the user taps it to open Topic Reveal. The
 *     Shuffle CTA owns all spin starts so accidental card taps never spin.
 *
 * v5.3 changes:
 *  9. **Saveable state** — active category, filter chips (tags + subtypes)
 *     and recent-topic history now persist via `rememberSaveable` across
 *     navigation (Spin → Reveal → back), rotation and process death.
 *     The landed topic stays transient on purpose so the deck opens cleanly
 *     after process restore.
 *
 * v5.4 changes:
 * 10. **Spec-timed spin window** — the shuffle duration is now randomized
 *     inside [CurioMotion.Durations.SpinMin]..[SpinMax] (2.4–3.2s) instead
 *     of a fixed loop, so every spin settles at a slightly different
 *     moment. The landed ticket swaps its helper copy while shuffling and
 *     then returns to the intentional "Tap to open" state.
 *
 * v5.5 changes:
 * 11. **Last-used category persists across launches** — the category the
 *     user spins in (chosen in-screen or opened via a category slug) is
 *     stored in [AppPreferences]; opening the plain Spin tab without a
 *     slug picks up where they left off instead of defaulting to Surprise.
 *
 * v5.6 changes:
 * 12. **Landed topic survives closing Reveal** — if the user closes Topic
 *     Reveal without saving, the topic stays on the card with "Tap to open"
 *     active until they explore (capture) it or tap Spin again.
 *
 * v5.7–v5.8 changes:
 * 13. The former gradient/glass ticket treatment was replaced by an opaque
 *     paper ticket with a category-color rule, crisp border, and layered
 *     elevation. The top-bar and bottom controls use solid paper containers;
 *     no ambient halo or glossy surface treatment is used on this screen.
 *
 * v5.9–v5.10 changes:
 * 14. The optional Spin page feature toggles (roulette dial, ritual &
 *     anticipation, deck enrichment, screen furniture) were removed — the
 *     screen keeps the fan-deck carousel and simple spacing as its
 *     permanent design. In their place a muted watermark backdrop of all
 *     the category glyphs sits behind the content, so the quiet space
 *     around the deck carries a whisper of the Curio world.
 * 15. **Bigger dice button** — the center CTA grew to 118dp idle / 100dp
 *     landed (176dp container) with a larger dice glyph inside.
 * 16. **Dice in every state** — the Casino dice shows even in the "Spin
 *     again" state (previously a Refresh icon); accent-tinted on the
 *     neutral landed surface.
 * 17. **Fluid dice tumble** — the in-button dice animation was slowed from
 *     980ms to 1600ms per turn with LinearEasing (no restart snap) plus a
 *     breathing pulse on the orbiting pips.
 *
 * v6.3 changes:
 * 18. **Bigger deck + CTA** — the hero ticket grew to 286×310dp (carousel
 *     444dp) and the dice button to 126dp idle / 108dp landed, with the
 *     dice glyphs scaled up to match.
 *
 * v6.4 changes:
 * 19. **Peek cards catch up** — the slim background cards grew ~6%
 *     (318×102dp near, 288×84dp far) so the whole fan scales with the
 *     hero ticket instead of the peeks staying small behind the big card.
 *
 * v6.5 changes:
 * 20. **Peek cards grow again (~13%)** — the topic title inside each
 *     background card now has room to read instead of hiding behind the
 *     fan (360×116dp near, 328×96dp far; proportions kept, only size up).
 * 21. **Gentler hero bounce** — smaller per-tick kick (1.035), half the
 *     tilt (40° factor), a softer hop, and a lower landing rest scale so
 *     the shuffle pulses instead of slamming the card.
 *
 * v6.6 changes:
 * 22. **Calm reel cadence** — the spin window lengthens slightly
 *     (2.8–3.6s) and the tick interval glides from ~200ms to ~520ms on a
 *     plain sine ease instead of the old squared-sine whip (105→400ms),
 *     so the wheel reads as a graceful reel slowing down.
 * 23. **Hero content reels** — the ticket's title/tags/teaser now animate
 *     through an eased upward slide + fade on every tick (mimicking a
 *     background card rising to the front) instead of snapping instantly.
 * 24. **Softer tick pulse** — per-tick kick drops to 1.02 on a heavily
 *     damped low-stiffness spring, the rock halves to a 16° tilt, and the
 *     landing settle uses the controlled Deliberate spring (no Elastic
 *     bounce). Peek wipes switch from 90ms linear blurs to ~200ms
 *     FastOutSlowInEasing slides.
 *
 * v6.10 changes:
 * 25. **Coherent reel (no more glitchy start)** — the fan is dealt as a
 *     stable hand and the reel rotates through it (+1 per tick), with the
 *     idle fan and the spinning reel reading the SAME window. A spin now
 *     starts from the current spread as a seamless continuation; the old
 *     per-spin re-shuffle made all five cards jump to arbitrary topics in
 *     one frame. The hand re-deals around the landed topic when a spin
 *     settles (masked by the confetti).
 * 26. **Fluid peek wipes** — every slot now rises THROUGH the card window
 *     at full height (in from below, out the top), all in the same
 *     direction (the old top-peek inverted slide glided backwards), and
 *     the wipe duration sits UNDER the 200ms tick floor so each step
 *     completes before the next tick lands — a clean slot-reel instead of
 *     an interrupted blur.
 * 27. **Dice settles instead of stopping** — the tumbling dots morph into
 *     the resting dice (spring scale + fade, then a gentle idle breathe on
 *     the landed die) instead of hard-swapping, and the tumble gains a
 *     slow vertical bob so the loop reads as a die shaking — seamless,
 *     and it never just stops.
 *
 * v7.1 changes:
 * 28. **Directional peek wipes** — top peek cards now feed the deck from
 *     ABOVE (their content drops DOWN into the card) while bottom peeks
 *     rise up, so the fan streams toward the hero from both ends and a
 *     top card's title is never sliced off the top edge by the old
 *     upward wipe.
 * 29. **Soft glides, not hard cuts** — the full-height slot wipe (which
 *     sliced the title mid-slide and read as cut off) is replaced by a
 *     partial-height glide + fade at ~320ms (under the ~340ms tick floor),
 *     so each step completes before the next tick and the reel reads as
 *     calm and smooth instead of fast and glitchy.
 *
 * v7.7 changes (EXPERIMENTAL, four independent toggles):
 * 30. **Deck card redesign toggles** — Settings → Appearance → "Deck
 *     cards" (each OFF by default) swaps the flat peek-card slabs for the
 *     recommended set from PEEK_CARD_DESIGN_SUGGESTIONS.md, one upgrade per
 *     toggle so each can be A/B'd alone: a top-lit two-stop gradient fill,
 *     a category-tinted hairline border (deep ink in light, light twin in
 *     dark), soft ambient shadows, and roomier near-card titles (16sp
 *     SemiBold, light tracking, two lines) with proportional glyphs (22dp
 *     near / 18dp far). The classic flat deck stays the shipping look
 *     until the experiment settles.
 * 31. **Pastel peek cards** — the peek fills now wear the pastel card
 *     family in pastel mode (airy pale layers in light, softly deepened
 *     muted twins in dark) instead of the old lerp-toward-black mid-tones.
 *
 * v7.9 changes:
 * 32. **Pastel hero ticket** — in pastel light mode a single-category
 *     deck's hero ticket opens on a pastel-family crown (a whisper of the
 *     pastel accent melting into the on-hue wash) instead of the
 *     black-darkened card fill, so the front card, the pale peek cards and
 *     the pastel spin button all read as one pastel story. Mixed decks
 *     already carried pure pastel stops; dark mode and non-pastel keep the
 *     classic card gradient.
 *
 * v7.8.1 changes:
 * 33. **Pastel brightness** — pastel card fills open on the full pastel
 *     accent (no black deepen) with a richer pastel saturation, so the
 *     shuffle main card no longer reads dimmed in either mode; pastel peek
 *     cards sit a step darker than the hero again (near 0.16 / far 0.28
 *     black-lerp in light) instead of glowing brighter than it.
 */
// ════════��══════════════════════════════════════════════════════════════════
// Saveable-state savers — category persisted by enum name, filter sets as
// lists (Set<String> has no built-in Bundle saver).
// ═══════════════════════════════════════════════════════════════════════════

/** Serializes a List<CategoryId> (single or multi-category launch set) by enum name. */
private val CategoryIdListSaver = listSaver<List<CategoryId>, String>(
    save = { it.map { id -> id.name } },
    restore = { names -> names.mapNotNull { name -> CategoryId.values().firstOrNull { it.name == name } } }
)

/** Serializes a Set<String> (filter chips, recent ids) as a saveable list. */
private val StringSetSaver = listSaver<Set<String>, String>(
    save = { it.toList() },
    restore = { it.toSet() }
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SpinScreen(categorySlug: String?, navController: NavController) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    // v5.7.1 — the slug branch is CurioCategory?; the prefs fallback returns
    // a CategoryId, so resolve it through byId(...) to keep BOTH elvis
    // branches CurioCategory (mixing them inferred Any → MutableState<Any>
    // vs the saver's MutableState<CurioCategory> → CI compile failure).
    // v5.11 — multi-category launch: the category picker's Mix button can
    // pass a comma-joined slug list ("artists,albums"). Resolve each part; fall
    // back to the last-used single category when the slug is absent or
    // unresolvable.
    val initialCats = remember(categorySlug) {
        val resolved = categorySlug
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.mapNotNull { CurioCategories.byRouteSlug(it) }
            .orEmpty()
        if (resolved.isNotEmpty()) resolved
        else AppPreferences.getLastSpinCategories(context).map { CurioCategories.byId(it) }
    }

    // v5.5 — remember which category this session opened in, so the plain
    // Spin tab opens where the user left off on the next launch. Persist the
    // FULL launch set (single or mixed) when a slug (single or multi) is
    // present so multi-select decks survive too.
    LaunchedEffect(Unit) {
        if (categorySlug != null) {
            AppPreferences.setLastSpinCategories(context, initialCats.map { it.id })
        }
    }

    // ── Saveable screen state — survives nav away/back, rotation and ──
    //    process death (v5.3). The active category SET persists across all
    //    of them; filters + recent history are keyed per first category so
    //    switching categories still resets them to fresh.
    var activeCatIds by rememberSaveable(
        initialCats.map { it.id },
        stateSaver = CategoryIdListSaver
    ) { mutableStateOf(initialCats.map { it.id }) }
    // v5.14 — a SLUG launch is authoritative. navigateToTab restores saved
    // state for the same route pattern, which could resurrect a stale
    // session (e.g. an in-screen category switch made inside an earlier
    // spin/artists visit) — picking "Artists" then reopened the deck with
    // Albums' pool. Whenever a slug is present, re-derive the category set
    // from it on arrival; user switches made AFTER arrival (picker sheet)
    // still win because this effect keys only on the slug.
    val slugCatIds: List<CategoryId>? = remember(categorySlug) {
        categorySlug
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.mapNotNull { CurioCategories.byRouteSlug(it)?.id }
            ?.takeIf { it.isNotEmpty() }
    }
    LaunchedEffect(categorySlug) {
        // Guard: on a normal fresh launch the value already matches; only
        // write when restoreState resurrected a stale set.
        if (slugCatIds != null && activeCatIds != slugCatIds) {
            activeCatIds = slugCatIds
        }
        // v5.15 — the plain Shuffle tab is equally authoritative from
        // prefs: the category picker ("What are we exploring?") now lands
        // here via navigateToTab(SPIN) after persisting its (possibly
        // mixed) selection, and restoreState could otherwise resurrect the
        // previous deck. Every in-screen switch also persists, so prefs
        // always reflect the user's latest deck.
        if (categorySlug == null) {
            val persisted = AppPreferences.getLastSpinCategories(context)
            if (persisted.isNotEmpty() && activeCatIds != persisted) {
                activeCatIds = persisted
            }
        }
    }
    // The first selected category drives chrome (top bar name, watermark
    // accent, confetti tint); the pool below merges every selected
    // category's topics so a multi-select launch spins across all of them.
    val activeCategory = remember(activeCatIds) {
        val id = activeCatIds.firstOrNull() ?: AppPreferences.getLastSpinCategory(context)
        CurioCategories.byId(id)
    }
    // Defensive: a corrupted saved state could restore an empty category
    // set — fall back to the last-used category so the pool still loads.
    val poolIds = if (activeCatIds.isEmpty()) listOf(AppPreferences.getLastSpinCategory(context)) else activeCatIds
    val pool by produceState<List<CurioTopic>>(initialValue = emptyList(), poolIds) {
        val merged = mutableListOf<CurioTopic>()
        val seen = mutableSetOf<String>()
        poolIds.forEach { id ->
            TopicJsonLoader.load(id).forEach { t -> if (seen.add(t.id)) merged.add(t) }
        }
        value = merged
    }

    // ── Multi-select filter state (per-category, saveable) ────────────
    var activeFilters by rememberSaveable(activeCategory.id, stateSaver = StringSetSaver) {
        mutableStateOf(setOf<String>())
    }
    var activeSubtypes by rememberSaveable(activeCategory.id, stateSaver = StringSetSaver) {
        mutableStateOf(setOf<String>())
    }
    var showFilters by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }

    // Broader OR-based filtering: a topic matches if it has ANY of the
    // selected tags AND its subtype is in the selected subtypes (or no
    // subtype filter is active).
    val filteredPool = remember(pool, activeFilters, activeSubtypes) {
        var r = pool
        if (activeFilters.isNotEmpty()) {
            r = r.filter { topic -> topic.tags.any { tag -> tag in activeFilters } }
        }
        if (activeSubtypes.isNotEmpty()) {
            r = r.filter { it.subtype in activeSubtypes }
        }
        r
    }
    // Smart filter groups — buckets raw tags into Type · Genre · Era ·
    // Origin sections and caps each, so the sheet stays ~10-15 chips
    // instead of dumping every raw tag (albums alone has 256 unique tags).
    val filterGroups = remember(pool) { buildFilterGroups(pool) }

    // ── Spin state ────────────────────────────────────────────────────
    var shuffling by remember { mutableStateOf(false) }
    var shuffleCount by remember { mutableIntStateOf(0) }
    var confettiTrigger by remember { mutableIntStateOf(0) }
    // ── Landed topic — persisted by NAME (v5.6) so closing Reveal without
    //    saving keeps it on the card, tappable, until explored or spun
    //    again. The full CurioTopic is re-derived from the pool below.
    var landedTopicName by rememberSaveable(activeCategory.id) {
        // Seeded from AppPreferences (v6): rememberSaveable survives tab
        // switches (saveState/restoreState) but dies when the Spin entry is
        // popped — e.g. the top-bar back arrow to Home. The prefs mirror
        // restores the landed card the next time Spin is composed.
        mutableStateOf(AppPreferences.getLandedTopic(context, activeCategory.id))
    }
    // v5.6 — true once THIS landing has already opened by tap; reset per spin.
    var landingAlreadyOpened by rememberSaveable(activeCategory.id) { mutableStateOf(false) }
    val landedTopic: CurioTopic? = remember(landedTopicName, filteredPool) {
        landedTopicName?.let { name ->
            filteredPool.firstOrNull { it.name == name }
                ?: TopicJsonLoader.cached(activeCategory.id)?.firstOrNull { it.name == name }
        }
    }
    // v6 — mirror the landed topic to AppPreferences whenever it changes
    // (landed on spin end, cleared on the next spin start), so it survives
    // ANY navigation — including popping the Spin back-stack entry.
    LaunchedEffect(activeCategory.id, landedTopicName) {
        AppPreferences.setLandedTopic(context, activeCategory.id, landedTopicName)
    }
    // True only during an explicit opening handoff; keeps copy flexible if
    // a future shared-element transition delays navigation.
    var isOpening by remember { mutableStateOf(false) }
    var recentTopicIds by rememberSaveable(activeCategory.id, stateSaver = StringSetSaver) {
        mutableStateOf(setOf<String>())
    }

    // ── Spin hand — the 6-topic fan window (v6.10) ─────────────────────
    // A stable "hand" reels during a spin: it's dealt ONCE (a random
    // spread, centered on the current front topic) and the reel advances by
    // rotating cycleIndex through it — so every tick is a clean +1 shift
    // and the deck visibly streams past. The OLD per-spin re-shuffle made
    // the start of every spin glitch: all five cards swapped to arbitrary
    // topics in a single frame before the reel even began.
    // The initial deal centers on the RESTORED landed topic when one
    // exists (nav-return from Reveal), so the idle fan reads coherent even
    // after the back-stack drops the composition. Keyed on filteredPool
    // ONLY — not on landedTopicName — so the spin start (which nulls the
    // landed topic) never re-deals the hand mid-flow.
    var hand by remember(filteredPool) { mutableStateOf(buildDeckHand(filteredPool, landedTopic)) }
    // cycleIndex is NOT reset per spin — the reel starts from wherever the
    // deck stopped (the landed topic sits at hand[0]), so the first tick is
    // a seamless continuation instead of a jump cut.
    var cycleIndex by remember { mutableIntStateOf(0) }
    // Keep the reel position in sync whenever the hand is re-dealt by a
    // pool change (filters/category), so the fan always fronts hand[0].
    LaunchedEffect(filteredPool) { cycleIndex = 0 }
    val cat = activeCategory

    // ── Mixed-deck colors (v5.12) ───────────────────────────────────────
    // When several categories are selected, the deck wears a curated blend
    // of every chosen accent instead of the first category's color alone:
    // peek cards / spin button / confetti take the blended accent, and the
    // hero ticket takes a multi-accent gradient (Spotify-style).
    // Resolved in the composable body (NOT remember) so the Material style's
    // device-color blend of each accent updates when the theme style changes.
    val deckAccents = activeCatIds.map { CurioCategories.byId(it).themedAccent() }
    // v7.5 — pastel mode: the curated pair/triple blends are deep, so the
    // resolved deck accent softens to its theme-aware pastel twin (airy in
    // light, muted deep pastel in dark). `dark` is resolved here (the
    // remember block is not a @Composable context).
    val pastelMode = AppPreferences.pastelColorsState
    val darkMode = isCurioDarkTheme()
    val deckAccent = remember(deckAccents, pastelMode, darkMode) {
        CurioMixedDeck.mixedDeckAccent(deckAccents, pastel = pastelMode, dark = darkMode)
    }

    // ── Mixed-deck identity (v5.13) ───────────────────────────────────────
    // A multi-select deck presents as ONE "Mixed" category instead of
    // wearing the first selected category's name/glyph: sparkles glyph,
    // blended accent + tint, and the merged topic pool. The synthetic
    // deckCat is display-only — its id stays the first category's id, so
    // logic keys (landed topic, filters, reveal guard, last-used prefs)
    // keep operating on the real category set.
    val isMixedDeck = remember(activeCatIds) { activeCatIds.distinct().size > 1 }

    // v7.9 — pastel LIGHT mode: a single-category deck opens its hero
    // ticket on the pastel-family crown (a whisper of the pastel accent,
    // melting into the on-hue page wash) instead of categoryCardFill's
    // black-darkened start, so the front card wears the same pastel story
    // as the peek cards and the pastel spin button. v7.8.1 — the peeks
    // behind it now sit a step DEEPER (black-lerp near 0.16 / far 0.28)
    // so the hero's crown reads as the brightest card of the deck. Mixed
    // decks already carry pure pastel stops + pastel seams; dark mode and
    // non-pastel keep the classic card gradient (the muted pastel twins
    // already match the dark peeks).
    val deckGradient = if (pastelMode && !darkMode && !isMixedDeck) {
        listOf(
            lerp(deckAccent, Color.White, 0.04f),
            lightAccentTint(deckAccent, saturation = 0.20f, lightness = 0.90f)
        )
    } else {
        CurioMixedDeck.mixedDeckGradient(deckAccents)
    }
    val deckCat = remember(activeCatIds, deckAccent, activeCategory) {
        if (isMixedDeck) {
            activeCategory.copy(
                displayName = "Mixed",
                iconGlyph = CurioIcons.AutoAwesome,
                accent = deckAccent,
                // Pastel twin of the blend so categoryInk() stays readable on
                // dark surfaces (ink = lightAccent in dark mode).
                lightAccent = lerp(deckAccent, Color.White, 0.45f),
                tint = deckAccent.copy(alpha = 0.20f)
            )
        } else {
            activeCategory
        }
    }

    // ── Mixed-deck arrangement seed (v6.9) ───────────────────────────────
    // A deck's hero-gradient arrangement is keyed off its sorted category
    // ids, so different mixes get different non-linear treatments (diagonal
    // sweep / reversed diagonal / radial glow) while a given deck stays
    // stable. Single decks keep the plain vertical card gradient.
    val mixSeed = remember(activeCatIds) { activeCatIds.sorted().hashCode() }

    // Publish the page wash so the bottom nav bar (rendered by the NavHost
    // scaffold, outside this screen) can blend with the tinted Spin page. The
    // bar gates on its own route (spin prefix only), so publishing here never
    // tints Home or Cabinet. Keys on the resolved color so theme/dark-mode
    // and category changes republish automatically. A mixed deck wears THE
    // blended color the mix resolves to (mixedDeckWash) instead of the first
    // category's wash, so the page reads in the deck's mixed color story.
    val pageWash = if (isMixedDeck) CurioMixedDeck.mixedDeckWash(deckAccent)
                   else deckCat.categoryBackgroundWash()
    LaunchedEffect(pageWash) {
        CurioNavTint.publishSpinWash(pageWash)
    }
    // Hygiene: clear the handoff when Spin leaves composition so a stale wash
    // never lingers for a future route that might share the tint.
    DisposableEffect(Unit) {
        onDispose { CurioNavTint.publishSpinWash(null) }
    }

    // Category switch resets transient animation state. The landed card is
    // deliberately NOT cleared here: landedTopicName is keyed by
    // activeCategory.id in rememberSaveable, so switching categories resets
    // it automatically — nulling it here would ALSO wipe the landed card on
    // every return from Topic Reveal (v5.6: stays tappable until spun again
    // or explored).
    LaunchedEffect(activeCategory.id) {
        shuffling = false
        isOpening = false
    }

    // ── Improved shuffle logic — sinusoidal ease-out deceleration ─────
    LaunchedEffect(shuffleCount) {
        if (shuffleCount == 0 || filteredPool.isEmpty()) return@LaunchedEffect
        shuffling = true
        landedTopicName = null
        landingAlreadyOpened = false
        isOpening = false
        // v5.4 — randomized within the spec's spin window; every spin
        // settles at a slightly different moment like a real wheel.
        val durationMs = Random.nextLong(
            CurioMotion.Durations.SpinMin.toLong(),
            CurioMotion.Durations.SpinMax.toLong() + 1
        )
        val start = System.currentTimeMillis()
        var tick = 0
        while (true) {
            val elapsed = System.currentTimeMillis() - start
            if (elapsed >= durationMs) break
            val progress = (elapsed.toFloat() / durationMs).coerceIn(0f, 1f)
            // Smooth reel deceleration: a plain sine ease (no squaring) so
            // the wheel starts at a readable cadence and glides gently to a
            // stop — a graceful slow-down instead of a snappy whip. The
            // ~340ms floor keeps the fastest early ticks readable and sits
            // ABOVE the ~320ms peek wipe, so every transition completes
            // before the next tick lands. Intervals ~340ms -> ~520ms.
            val eased = sin(progress * Math.PI.toFloat() / 2f)
            val interval = (340L + (180L * eased).toLong()).coerceAtMost(520L)
            cycleIndex = ++tick
            // Slot-machine ratchet: haptic intensity escalates as the wheel
            // decelerates — a light tick at the brisk opening cadence, a
            // firmer segment tick through the slowdown, and a solid
            // keyboard-tap click in the final settle phase. As intervals
            // lengthen, ticks naturally space out like a prize wheel
            // locking in. NOTE: SegmentFrequentTick / KeyboardTap are
            // the renamed equivalents of the old ClockTick / Keypress
            // constants (Compose UI 1.12) — do NOT revert them.
            val ratchet = when {
                progress < 0.5f -> HapticFeedbackType.TextHandleMove
                progress < 0.85f -> HapticFeedbackType.SegmentFrequentTick
                else -> HapticFeedbackType.KeyboardTap
            }
            haptics.performHapticFeedback(ratchet)
            delay(interval)
            if (System.currentTimeMillis() - start >= durationMs) break
        }
        shuffling = false

        // Pick a single topic — tier-biased, sentiment-weighted (liked /
        // disliked topics + category affinity), and never an already-
        // explored topic while alternatives remain.
        val exploredIds = runCatching {
            CurioRepositoryHolder.repo.getAll().map { it.topic.id }.toSet()
        }.getOrDefault(emptySet())
        val primary = pickFrom(
            filteredPool,
            recentTopicIds,
            exploredIds,
            AppPreferences.topicSentimentsState,
            AppPreferences.categoryAffinityMap()
        )
        landedTopicName = primary?.name
        if (primary != null) {
            // Re-deal the hand around the landed topic — the front becomes
            // the pick and its neighbors fill the fan — so the deck stops
            // on a coherent spread and the NEXT spin starts from it
            // seamlessly. This re-deal is masked by the confetti burst.
            hand = buildDeckHand(filteredPool, primary)
            cycleIndex = 0
            recentTopicIds = (recentTopicIds + primary.id).toList().takeLast(20).toSet()
            StreakTracker.recordActivity(context)
            // Final reel clunk — strong confirmation the wheel locked in.
            haptics.performHapticFeedback(HapticFeedbackType.Confirm)
        }
        confettiTrigger++

        // Auto-open the landed topic: once the wheel settles, reveal it
        // immediately. The landed card state is preserved (landedTopicName +
        // landingAlreadyOpened), so returning from Reveal keeps it tappable
        // until spun again — nothing else about the flow changes. A short
        // pause lets the settle + confetti read before navigating; spinning
        // again within that window cancels this effect (keyed on
        // shuffleCount) and no navigation happens.
        if (primary != null) {
            landingAlreadyOpened = true
            delay(600)
            // Guard against a category switch during the pause: the effect
            // captured `cat` at launch, so only navigate if it's still the
            // active category.
            if (cat.id != activeCategory.id) return@LaunchedEffect
            navController.navigate(CurioRoutes.revealFor(primary.categoryId.routeSlug, primary.name)) {
                launchSingleTop = true
            }
        }
    }

    // ── Landed topic auto-opens on landing ───────────────────────────
    // The wheel now reveals its landed topic automatically; the center card
    // is no longer a spin trigger — it opens an already landed topic, while
    // the Shuffle CTA owns all spin/shuffle starts.

    // ── v5.9 — landed card stays tappable until the user explicitly
    //    spins/shuffles again.  No longer auto-clears when explored.

    // ── Animations ────────────────────────────────────────────────────
    val buttonPulse by animateFloatAsState(
        targetValue = if (shuffling) 1.06f else 1f,
        animationSpec = CurioMotion.Springs.Snappy,
        label = "buttonPulse"
    )

    // ── Deck interaction callbacks — shared by the normal and compact
    //    layout branches (the Carousel call lives in SpinDeckSection) ─
    val onDeckCardTap: () -> Unit = {
        if (!shuffling && filteredPool.isNotEmpty()) {
            val resolved = landedTopic
                ?: landedTopicName?.let { name ->
                    TopicJsonLoader.cached(cat.id)?.firstOrNull { it.name == name }
                }
            if (resolved != null) {
                landingAlreadyOpened = true
                navController.navigate(CurioRoutes.revealFor(resolved.categoryId.routeSlug, resolved.name)) {
                    launchSingleTop = true
                }
            }
        }
    }
    val onSpinClick: () -> Unit = {
        if (!shuffling && filteredPool.isNotEmpty()) shuffleCount++
    }

    // ── Overall layout ─────────────────────────────────────────────────
    // Paper surfaces sit directly on the quiet theme background. All depth
    // comes from opaque cards, crisp rules, and elevation—not ambient washes.
    // v6.11 — BoxWithConstraints measures the height this screen is actually
    // granted (see [SpinCompactThresholdHeight]): short screens switch to
    // the compact layout below, normal screens keep this exact layout.
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            // Category tint wash — the Spin page wears a wash of the deck's
            // color over the theme background (same wash language as Topic
            // Reveal / Save / Cabinet). Mixed decks wear THE blended color
            // the mix resolves to (pageWash) at high strength, so each mix
            // visibly repaints the page in its own blended color story.
            .background(pageWash)
    ) {
        // ── Smart layout tiers (v7.3) ────────────────────────────────
        // 1. DENSITY (two-way, toggleable via Settings → Smart density
        //    layout) — devices under [SpinLowDensityDpi] get the compact
        //    (scrollable-band) layout no matter the height, and devices at
        //    or above [SpinHighDensityDpi] get a roomier deck (slightly
        //    LARGER scale), so low-dpi screens feel smaller and high-dpi
        //    screens feel bigger. The whole rule is gated by one switch.
        // 2. DIMENSION (toggleable via Settings → Smart Spin layout) —
        //    heights under [SpinCompactThresholdHeight] switch to the
        //    compact layout, and heights under
        //    [SpinExtraCompactThresholdHeight] get the EXTRA-compact tier —
        //    a smaller deck AND Categories/Filter as tall vertical pills
        //    pinned to the left/right screen edges.
        val densityMode = AppPreferences.smartDensityModeState
        val densityDpi = context.resources.displayMetrics.densityDpi
        val densityActive = densityMode != SmartDensityMode.OFF
        val lowDensity = densityActive && densityDpi < SpinLowDensityDpi
        val highDensity = densityActive && densityDpi >= SpinHighDensityDpi
        // v7.4 — the 2x tier: EXTRA_COMPACT mode shrinks the deck a step
        // further below [SpinExtraLowDensityDpi] so very low-dpi phones
        // (≈350 dpi) fit the whole spin comfortably.
        val densityExtraCompact =
            densityMode == SmartDensityMode.EXTRA_COMPACT && densityDpi < SpinExtraLowDensityDpi
        val smartLayout = AppPreferences.smartSpinLayoutState
        val heightCompact = maxHeight < SpinCompactThresholdHeight
        val extraCompact = smartLayout && maxHeight < SpinExtraCompactThresholdHeight
        // Extra-compact implies heightCompact (600 < 680), so this stays
        // true whenever the smaller tier is active.
        val compactHeight = lowDensity || (smartLayout && heightCompact)
        // Roomy tier — high-density screens get a slightly LARGER deck so
        // the density rule works both ways. Keyed off the RAW height (not
        // the toggle-gated compactHeight) so a short high-density screen
        // never gets the bigger deck even when the dimension rule is off.
        val roomy = highDensity && !heightCompact
        // ── Watermark backdrop — every category glyph scattered around ──
        //    the screen in a muted shade, behind all content, so the quiet
        //    space around the deck still carries a whisper of the Curio
        //    world. The active category's glyph gets a faint accent tint.
        CurioWatermarkBackdrop(activeCat = deckCat)

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
        // ── 1. Top bar — back, category name, topic count (pinned) ──
        TopBar(
            cat = deckCat,
            poolCount = pool.size,
            filteredCount = filteredPool.size,
            modifier = Modifier.statusBarsPadding(),
            onBack = {
                // Edge case — Spin can be a root destination (deep link or
                // restored tab) with nothing to pop; fall back to Home.
                if (!navController.popBackStack()) {
                    navController.navigate(CurioRoutes.HOME) { launchSingleTop = true }
                }
            }
        )

        if (compactHeight) {
            // ── Compact layout (small screens) ────────────────────────
            // The deck + spin button scroll inside the space between the
            // pinned top bar and the pinned Categories/Filter bar, so the
            // controls are never pushed off-screen; sizes step down via
            // SpinDeckSection(compact = true).
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                SpinDeckSection(
                    compact = true,
                    extraCompact = extraCompact,
                    densityExtraCompact = densityExtraCompact,
                    roomy = false,
                    cat = deckCat,
                    deckAccent = deckAccent,
                    deckGradient = deckGradient,
                    isMixed = isMixedDeck,
                    mixSeed = mixSeed,
                    displayPool = hand,
                    cycleIndex = cycleIndex,
                    shuffling = shuffling,
                    landedTopic = landedTopic,
                    opening = isOpening,
                    enabled = filteredPool.isNotEmpty() && !shuffling,
                    buttonPulse = buttonPulse,
                    onCardTap = onDeckCardTap,
                    onSpinClick = onSpinClick
                )
                // Breathing room under the spin button before the CTA.
                Spacer(Modifier.height(10.dp))
            }
        } else {
            // ── Normal layout — the exact pre-compact stack ──────────
            SpinDeckSection(
                compact = false,
                densityExtraCompact = densityExtraCompact,
                roomy = roomy,
                cat = deckCat,
                deckAccent = deckAccent,
                deckGradient = deckGradient,
                isMixed = isMixedDeck,
                mixSeed = mixSeed,
                displayPool = hand,
                cycleIndex = cycleIndex,
                shuffling = shuffling,
                landedTopic = landedTopic,
                opening = isOpening,
                enabled = filteredPool.isNotEmpty() && !shuffling,
                buttonPulse = buttonPulse,
                onCardTap = onDeckCardTap,
                onSpinClick = onSpinClick
            )
            // ── 4. Breathing room — keeps the bottom bar pinned to the
            //    screen edge instead of leaving dead space below it ─────
            Spacer(Modifier.weight(1f))
        }

        // ── 5. Bottom bar — Categories · Filter (controls only) ────
        // No duplicate shuffle button: the big center SpinButton above
        // owns all spin starts, so the bottom bar is controls only. On the
        // extra-compact tier the two pills move to the left/right screen
        // edges and stand vertically, so the middle stays clear.
        BottomCta(
            cat = deckCat,
            mixedCount = activeCatIds.distinct().size,
            filterActiveCount = activeFilters.size + activeSubtypes.size,
            vertical = extraCompact,
            onCategories = { showCategoryPicker = true },
            onFilter = { showFilters = true }
        )
        }
    }



    // ── CategoryPickerSheet ───────────────────────────────────────────
    if (showCategoryPicker) {
        CategoryPickerSheet(
            currentCat = deckCat,
            onDismiss = { showCategoryPicker = false },
            onCategorySelected = { c ->
                activeCatIds = listOf(c.id)
                // v5.5 — persist so the Spin tab reopens on this category
                // after the app is killed and relaunched.
                AppPreferences.setLastSpinCategories(context, listOf(c.id))
                showCategoryPicker = false
            },
            onCategoriesSelected = { cats ->
                activeCatIds = cats.map { it.id }
                // v5.15 — persist the FULL mixed set (not just the first)
                // so a multi-select deck survives back navigation, tab
                // switches and app restarts.
                AppPreferences.setLastSpinCategories(context, cats.map { it.id })
                showCategoryPicker = false
            },
            onBrowseAll = {
                showCategoryPicker = false
                navController.navigate(CurioRoutes.PICKER) { launchSingleTop = true }
            }
        )
    }

    // ── ModalBottomSheet — compact multi-select filter dialog ──────────
    if (showFilters) {
        FilterSheet(
            cat = deckCat,
            groups = filterGroups,
            initialSubtypes = activeSubtypes,
            initialFilters = activeFilters,
            onDismiss = { showFilters = false },
            onApply = { tags, subtypes ->
                activeFilters = tags
                activeSubtypes = subtypes
                showFilters = false
            }
        )
    }

    // ── Confetti on landing ────────────────────────────────────────────
    if (confettiTrigger > 0) {
        ConfettiBurst(
            colors = listOf(deckAccent, deckCat.tint, CurioColors.ButterYellow),
            trigger = confettiTrigger,
            particleCount = CurioMotion.ConfettiParticleCountLarge,
            modifier = Modifier.fillMaxSize(),
            onComplete = {}
        )
    }
}

/**
 * The deck section of the Spin layout — breathing spacer, card carousel and
 * center spin button — shared by the normal and compact layout branches.
 *
 * [compact] switches between the two size variants: normal keeps the exact
 * pre-v6.11 measurements (44dp spacer, 444dp carousel, 32/20dp button
 * padding); compact tightens them (20dp spacer, 390dp carousel, 16/10dp
 * padding) and scales the deck itself down via the carousel's compact flag.
 */
@Composable
private fun ColumnScope.SpinDeckSection(
    compact: Boolean,
    extraCompact: Boolean = false,
    densityExtraCompact: Boolean = false,
    roomy: Boolean = false,
    cat: CurioCategory,
    deckAccent: Color,
    deckGradient: List<Color>,
    isMixed: Boolean,
    mixSeed: Int,
    displayPool: List<CurioTopic>,
    cycleIndex: Int,
    shuffling: Boolean,
    landedTopic: CurioTopic?,
    opening: Boolean,
    enabled: Boolean,
    buttonPulse: Float,
    onCardTap: () -> Unit,
    onSpinClick: () -> Unit
) {
    // ── Breathing room — keeps the header off the deck (tighter when the
    //    screen is short so the deck fits between the pinned bars; roomier
    //    on high-density screens so the bigger deck has space) ─────────
    Spacer(
        Modifier.height(
            when {
                densityExtraCompact -> 12.dp
                extraCompact -> 12.dp
                compact -> 20.dp
                roomy -> 56.dp
                else -> 44.dp
            }
        )
    )

    // ── Carousel (interactive cards) ───────────────────────────────
    // Tapping the center card opens a landed topic only; the bottom
    // Shuffle CTA owns starting or re-starting the shuffle.
    Carousel(
        cat = cat,
        deckAccent = deckAccent,
        deckGradient = deckGradient,
        isMixed = isMixed,
        mixSeed = mixSeed,
        displayPool = displayPool,
        cycleIndex = cycleIndex,
        shuffling = shuffling,
        landedTopic = landedTopic,
        opening = opening,
        enabled = enabled,
        compact = compact,
        extraCompact = extraCompact,
        densityExtraCompact = densityExtraCompact,
        roomy = roomy,
        onCardTap = onCardTap,
        modifier = Modifier.fillMaxWidth()
    )

    // ── Center spin button — the ONLY shuffle CTA (v6) ──────────────
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = when {
                    densityExtraCompact -> 12.dp
                    compact && extraCompact -> 12.dp
                    compact -> 16.dp
                    roomy -> 40.dp
                    else -> 32.dp
                },
                bottom = when {
                    densityExtraCompact -> 8.dp
                    compact && extraCompact -> 8.dp
                    compact -> 10.dp
                    roomy -> 26.dp
                    else -> 20.dp
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        SpinButton(
            tint = deckAccent,
            isShuffling = shuffling,
            landedTopic = landedTopic,
            pulseScale = buttonPulse,
            enabled = enabled,
            compact = compact,
            onClick = onSpinClick
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Top bar — Back · CategoryMenu chip · topic-count badge
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun TopBar(
    cat: CurioCategory,
    poolCount: Int,
    filteredCount: Int,
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CurioBackButton(onClick = onBack)

        Spacer(Modifier.width(10.dp))

        // ── Category label — plain title text, no pill container ────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            CurioIcon(
                cat.iconGlyph, null,
                tint = cat.categoryInk(),
                size = 18.dp
            )
            Text(
                text = cat.displayName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }

        Spacer(Modifier.weight(1f))

        // ── Right-side topic count — plain text, no pill ────────────
        if (poolCount > 0) {
            Text(
                text = "$filteredCount / $poolCount",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


// ═══════════════════════════════════════════════════════════════════════════
// ═══════════════════════════════════════════════════════════════════════════
// Smart filter grouping — buckets a category's raw tags into compact
// Type · Genre · Era · Origin sections so the sheet stays ~10-15 chips
// instead of dumping every raw tag (albums alone has 256 unique tags).
// ═══════════════════════════════════════════════════════════════════════════

private data class FilterGroups(
    val types: List<String>,
    val genres: List<String>,
    val eras: List<String>,
    val origins: List<String>
)

/** Common nationality/origin tags — anything else is treated as a genre. */
private val NationalityTags = setOf(
    "American", "British", "French", "German", "Italian", "Japanese", "Chinese",
    "Nigerian", "Jamaican", "Canadian", "Swedish", "Norwegian", "Danish", "Finnish",
    "Icelandic", "Cuban", "Brazilian", "Indian", "Korean", "Australian", "Irish",
    "Scottish", "Welsh", "Russian", "Polish", "Spanish", "Portuguese", "Greek",
    "Turkish", "Mexican", "Argentine", "Argentinian", "Colombian", "Chilean", "Dutch",
    "Belgian", "Swiss", "Austrian", "Hungarian", "Czech", "Romanian", "Ukrainian",
    "Ghanaian", "Senegalese", "Ethiopian", "Kenyan", "South African", "Egyptian",
    "Moroccan", "Algerian", "Iranian", "Israeli", "Pakistani", "Filipino", "Indonesian",
    "Thai", "Vietnamese", "Malaysian", "Congolese", "Malian", "Lebanese", "Syrian",
    "Iraqi", "Afghan", "Armenian", "Georgian", "Kazakh", "Mongolian", "Nepali",
    "Sri Lankan", "Bangladeshi", "Haitian", "Puerto Rican", "Dominican", "Venezuelan",
    "Ecuadorian", "Bolivian", "Uruguayan", "Croatian", "Serbian", "Bulgarian", "Slovak",
    "Estonian", "Lithuanian", "New Zealand", "New Zealander", "Taiwanese", "Hong Kong",
    "Cape Verdean", "Barbadian", "Beninese", "African", "European", "Soviet", "Tuareg",
    // Film/TV industry regions — Hollywood (US studio system) and Bollywood
    // (Hindi cinema) read as origin tags on Films/Directors, so the Origin
    // bucket offers them alongside British / French / Korean / Indian…
    "Hollywood", "Bollywood",
    "Congolese", "Panamanian", "Chilean", "Argentine", "Puerto Rican",
    "American-British", "British-Nigerian", "American-Canadian", "French-Algerian",
    "Italian-American", "British-Irish", "African-American", "British-Canadian",
    "Brazilian-American", "Brazilian-British", "Ghanaian-British", "French-Spanish",
    "Irish-British", "British-American", "Canadian-American", "Greek-American",
    "Russian-French", "British-German", "Czech-Austrian", "Latvian-American",
    "French-American", "Swiss-American", "Japanese-American", "Hellenistic-Egyptian",
    "Roman-Egyptian", "Egyptian-Greek", "Polish-French", "New Zealand-British",
    "Welsh-British", "Scottish-British", "Hungarian-American", "British-Dutch",
    "American-French", "Austrian-Czech", "British-Welsh", "Indian-Bengali"
)

/**
 * Derives compact, meaningful filter chips from a category's pool.
 * Eras are the most frequent decades/centuries present, genres and origins
 * are the most-used tags, each capped so the sheet stays tidy.
 */
private fun buildFilterGroups(pool: List<CurioTopic>): FilterGroups {
    if (pool.isEmpty()) return FilterGroups(emptyList(), emptyList(), emptyList(), emptyList())
    val types = pool.map { it.subtype }.distinct().sorted()
    val counts = pool.flatMap { it.tags }.groupingBy { it }.eachCount()
    // Era chips: pick whichever family is more prevalent in this category —
    // decades (1970s…) for music/film, centuries (20th Century…) for books,
    // science and art. Comparing total frequency instead of mere presence
    // keeps the row coherent when a category mixes both (e.g. books has a
    // lone '2000s' tag but is dominated by '20th Century').
    val decadeRe = Regex("""\d{4}s""")
    val centuryRe = Regex("""^\d{1,2}(st|nd|rd|th) Century$|^Ancient$""")
    val decades = counts.keys.filter { decadeRe.matches(it) }
    val centuries = counts.keys.filter { centuryRe.matches(it) }
    val decadesTotal = decades.sumOf { counts[it] ?: 0 }
    val centuriesTotal = centuries.sumOf { counts[it] ?: 0 }
    val eras = (if (decadesTotal >= centuriesTotal) decades else centuries)
        .sortedByDescending { counts[it] ?: 0 }
        .take(4)
        .sorted()
    val origins = counts.keys
        .filter { it in NationalityTags }
        .sortedByDescending { counts[it] ?: 0 }
        .take(3)
    val genres = counts.keys
        .filter { !decadeRe.matches(it) && !centuryRe.matches(it) && it !in NationalityTags }
        .sortedByDescending { counts[it] ?: 0 }
        .take(4)
    return FilterGroups(types = types, genres = genres, eras = eras, origins = origins)
}

// ═══════════════════════════════════════════════════════════════════════════
// ═══════════════════════════════════════════════════════════════════════════
// Compact filter bottom sheet with visible selected-filter chips
// ═══════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSheet(
    cat: CurioCategory,
    groups: FilterGroups,
    initialSubtypes: Set<String>,
    initialFilters: Set<String>,
    onDismiss: () -> Unit,
    onApply: (tags: Set<String>, subtypes: Set<String>) -> Unit
) {
    val subtypes = groups.types
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var draftFilters by remember(initialFilters) { mutableStateOf(initialFilters) }
    var draftSubtypes by remember(initialSubtypes) { mutableStateOf(initialSubtypes) }
    val activeCount = draftFilters.size + draftSubtypes.size

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = cat.categorySurface(MaterialTheme.colorScheme.surfaceContainerLow),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            // ── Header row ────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CurioIcon(cat.iconGlyph, null, tint = cat.categoryInk(), size = 22.dp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = cat.displayName,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (activeCount > 0) {
                    TextButton(
                        onClick = {
                            draftFilters = emptySet()
                            draftSubtypes = emptySet()
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "Clear all",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // ── Active filter summary chips — this is what was missing ─
            if (activeCount > 0) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = "Active filters",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        draftSubtypes.forEach { st ->
                            ActiveFilterChip(
                                label = st,
                                accent = cat.themedAccent(),
                                ink = cat.onAccent(),
                                onRemove = { draftSubtypes = draftSubtypes - st }
                            )
                        }
                        draftFilters.forEach { tag ->
                            ActiveFilterChip(
                                label = tag,
                                accent = cat.themedAccent(),
                                ink = cat.onAccent(),
                                onRemove = { draftFilters = draftFilters - tag }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
            }

            val hasAny = subtypes.size > 1 ||
                groups.genres.isNotEmpty() ||
                groups.eras.isNotEmpty() ||
                groups.origins.isNotEmpty()
            if (!hasAny) {
                Text(
                    text = "No filters for this category yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                )
            } else {
                // ── Divider line ──────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                )

                // ── Compact lazy chip grid — grouped Type · Genre · Era ·
                //    Origin sections, each capped to a handful of chips so
                //    the sheet stays tidy instead of 100+ raw tags. ─────
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 112.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (subtypes.size > 1) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            SectionLabel("Type", Modifier.padding(bottom = 2.dp))
                        }
                        items(subtypes) { st ->
                            CompactChip(
                                label = st,
                                selected = st in draftSubtypes,
                                accent = cat.themedAccent(),
                                ink = cat.onAccent(),
                                chipSurface = cat.categorySurface(MaterialTheme.colorScheme.surfaceContainerHigh),
                                chipBorder = cat.categoryBorder(
                                    fallback = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ),
                                onClick = {
                                    draftSubtypes = if (st in draftSubtypes) draftSubtypes - st else draftSubtypes + st
                                }
                            )
                        }
                    }
                    if (groups.genres.isNotEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            SectionLabel(
                                "Genres",
                                Modifier.padding(top = if (subtypes.size > 1) 6.dp else 0.dp, bottom = 2.dp)
                            )
                        }
                        items(groups.genres) { tag ->
                            CompactChip(
                                label = tag,
                                selected = tag in draftFilters,
                                accent = cat.themedAccent(),
                                ink = cat.onAccent(),
                                chipSurface = cat.categorySurface(MaterialTheme.colorScheme.surfaceContainerHigh),
                                chipBorder = cat.categoryBorder(
                                    fallback = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ),
                                onClick = {
                                    draftFilters = if (tag in draftFilters) draftFilters - tag else draftFilters + tag
                                }
                            )
                        }
                    }
                    if (groups.eras.isNotEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            SectionLabel("Era", Modifier.padding(top = 6.dp, bottom = 2.dp))
                        }
                        items(groups.eras) { era ->
                            CompactChip(
                                label = era,
                                selected = era in draftFilters,
                                accent = cat.themedAccent(),
                                ink = cat.onAccent(),
                                chipSurface = cat.categorySurface(MaterialTheme.colorScheme.surfaceContainerHigh),
                                chipBorder = cat.categoryBorder(
                                    fallback = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ),
                                onClick = {
                                    draftFilters = if (era in draftFilters) draftFilters - era else draftFilters + era
                                }
                            )
                        }
                    }
                    if (groups.origins.isNotEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            SectionLabel("Origin", Modifier.padding(top = 6.dp, bottom = 2.dp))
                        }
                        items(groups.origins) { origin ->
                            CompactChip(
                                label = origin,
                                selected = origin in draftFilters,
                                accent = cat.themedAccent(),
                                ink = cat.onAccent(),
                                chipSurface = cat.categorySurface(MaterialTheme.colorScheme.surfaceContainerHigh),
                                chipBorder = cat.categoryBorder(
                                    fallback = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ),
                                onClick = {
                                    draftFilters = if (origin in draftFilters) draftFilters - origin else draftFilters + origin
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Apply button ──────────────────────────────────────────
            Button(
                onClick = { onApply(draftFilters, draftSubtypes) },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = cat.themedAccent(),
                    contentColor = cat.onAccent()
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                CurioIcon(CurioIcons.Check, null, tint = cat.onAccent(), size = 18.dp)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (activeCount > 0) "Apply filters ($activeCount)" else "Show all topics",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                )
            }
        }
    }
}

/** Chip showing an active filter with ✕ to remove. */
@Composable
private fun ActiveFilterChip(
    label: String,
    accent: Color,
    ink: Color = Color.White,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = accent,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = ink
            )
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.clickable(onClick = onRemove)
            ) {
                CurioIcon(
                    CurioIcons.Close, null,
                    tint = CurioColors.DeepPlum,
                    size = 14.dp,
                    modifier = Modifier.padding(2.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.2.sp
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

@Composable
private fun CompactChip(
    label: String,
    selected: Boolean,
    accent: Color,
    ink: Color = Color.White,
    chipSurface: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    chipBorder: BorderStroke? = null,
    onClick: () -> Unit
) {
    // Plain Surface + clickable (no M3 minimum touch-target inflation) keeps
    // the chips compact even with 100+ tags in the sheet.
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) accent else chipSurface,
        border = if (selected) null else chipBorder,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
    ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium
                ),
                color = if (selected) ink else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
}


// ═══════════════════════════════════════════════════════════════════════════
// Fan-deck carousel — hero "ticket" card + slim prev/next peek cards
// ═══════════════════════════════════════════════════════════════════════════

/** Rest scale the hero card settles to after a shuffle lands. */
private const val LandedRestScale = 1.02f

// v7.1 — peek wipe timings. Soft partial-height glides + fades (no hard
// slot cut), all under the ~340ms tick floor so each step completes before
// the next tick lands.
private const val PeekWipeInMs = 320
private const val PeekWipeOutMs = 300
private const val PeekIdleInMs = 300
private const val PeekIdleOutMs = 280

/** Fraction of the card height a peek wipe travels (partial = soft glide). */
private const val PeekWipeTravel = 0.45f

/**
 * Small-screen adaptive layout (v6.11). The Spin stack — top bar + 44dp
 * spacer + 444dp deck + spin button + Categories/Filter bar — needs ~830dp;
 * on short screens the bottom CTA gets pushed off-screen. When the height
 * the NavHost actually grants this screen (after status bar, bottom nav and
 * gesture insets) drops below this threshold, the page switches to the
 * compact layout: the deck + button move into a vertically scrollable
 * middle band pinned between the top bar and the bottom CTA, and every
 * fixed size steps down by [SpinCompactDeckScale]. Above the threshold the
 * layout is byte-for-byte the original — normal screens never change.
 */
private val SpinCompactThresholdHeight = 680.dp

/**
 * Extra-compact threshold — screens shorter than this get the smallest
 * Spin tier (v7.2): a smaller deck AND Categories/Filter as tall vertical
 * pills pinned to the left/right screen edges. Implies compact.
 */
private val SpinExtraCompactThresholdHeight = 600.dp

/**
 * Low-density threshold (v7.2) — devices under this density get the
 * compact layout regardless of height (gated by the "Smart density
 * layout" setting since v7.3).
 */
private const val SpinLowDensityDpi = 440

/**
 * Extra-low-density threshold (v7.4) — devices under this density get the
 * 2x density tier (an even smaller deck) when Settings → Experimental →
 * Smart density is set to Extra-compact ("2x").
 */
private const val SpinExtraLowDensityDpi = 350

/**
 * High-density threshold (v7.3) — devices at or above this density get the
 * roomy tier (a slightly LARGER deck), so the density rule scales both
 * ways: low dpi → smaller, high dpi → larger.
 */
private const val SpinHighDensityDpi = 440

/** Deck scale factor applied in compact (short-screen) mode. */
private const val SpinCompactDeckScale = 0.88f

/** Deck scale factor applied in extra-compact mode. */
private const val SpinExtraCompactDeckScale = 0.78f

/** Deck scale factor applied in the 2x density tier (v7.4). */
private const val SpinDensityExtraCompactDeckScale = 0.72f

/** Deck scale factor applied in roomy (high-density) mode. */
private const val SpinRoomyDeckScale = 1.05f

@Composable
private fun Carousel(
    cat: CurioCategory,
    deckAccent: Color,
    deckGradient: List<Color>,
    isMixed: Boolean,
    mixSeed: Int,
    displayPool: List<CurioTopic>,
    cycleIndex: Int,
    shuffling: Boolean,
    landedTopic: CurioTopic?,
    opening: Boolean,
    enabled: Boolean,
    compact: Boolean = false,
    extraCompact: Boolean = false,
    densityExtraCompact: Boolean = false,
    roomy: Boolean = false,
    onCardTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val poolSize = displayPool.size
    // v6.11 — compact screens shrink the whole fan ~12% so the deck keeps
    // its proportions inside the shorter box; v7.2 — extra-compact scales
    // a further step (~22% total) so the whole fan fits very short screens;
    // v7.3 — roomy scales the fan UP ~5% on high-density screens so the
    // density rule works both ways (low → smaller, high → larger).
    val deckScale = when {
        densityExtraCompact -> SpinDensityExtraCompactDeckScale
        extraCompact -> SpinExtraCompactDeckScale
        compact -> SpinCompactDeckScale
        roomy -> SpinRoomyDeckScale
        else -> 1f
    }
    Box(
        // v6.3 — grew with the hero ticket so the bigger card keeps its
        // breathing room above/below. The extra-compact box scales with the
        // fan so proportions stay identical; the roomy box grows ~6% to
        // match the up-scaled fan.
        modifier = modifier.height(
            when {
                densityExtraCompact -> 325.dp
                extraCompact -> 350.dp
                compact -> 390.dp
                roomy -> 470.dp
                else -> 444.dp
            }
        ),
        contentAlignment = Alignment.Center
    ) {
        if (poolSize == 0) {
            EmptyPoolHint(cat)
        } else {
            val slots = listOf(-2, 2, -1, 1, 0)
            slots.forEach { slot ->
                val topic = resolveTopicForSlot(
                    slot = slot,
                    pool = displayPool,
                    cycleIndex = cycleIndex,
                    landedTopic = landedTopic
                )
                if (slot == 0) {
                    HeroTicketCard(
                        accent = deckAccent,
                        gradient = deckGradient,
                        isMixed = isMixed,
                        mixSeed = mixSeed,
                        scale = deckScale,
                        glyph = cat.iconGlyph,
                        topic = topic,
                        cat = cat,
                        landed = landedTopic != null,
                        shuffling = shuffling,
                        opening = opening,
                        enabled = enabled && landedTopic != null,
                        onTap = onCardTap
                    )
                } else {
                    PeekCard(
                        slot = slot,
                        scale = deckScale,
                        accent = deckAccent,
                        cat = cat,
                        topic = topic,
                        shuffling = shuffling
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyPoolHint(cat: CurioCategory) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = cat.categorySurface(MaterialTheme.colorScheme.surfaceContainerLow),
            shadowElevation = 0.dp,
            border = cat.categoryBorder(),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CurioIcon(
                    cat.iconGlyph, null,
                    tint = cat.categoryInk().copy(alpha = 0.5f),
                    size = 56.dp
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "Coming soon",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Spin the wheel once topics land.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun HeroTicketCard(
    accent: Color,
    gradient: List<Color>,
    isMixed: Boolean,
    mixSeed: Int,
    scale: Float = 1f,
    glyph: String,
    topic: CurioTopic?,
    cat: CurioCategory,
    landed: Boolean,
    shuffling: Boolean,
    opening: Boolean,
    enabled: Boolean,
    onTap: () -> Unit
) {
    // v6.3 — slightly bigger ticket (~6% up) so the hero card reads a
    // touch more prominent on the deck.
    // v6.11 — compact screens scale the whole ticket down (small phones);
    // proportions and internal paddings stay identical.
    val w = 286.dp * scale
    val h = 310.dp * scale
    // Mixed decks render the multi-accent stops in a non-linear arrangement
    // (diagonal sweep / reversed diagonal / radial glow, keyed off the
    // deck's category set); single decks keep the plain vertical theme-aware
    // card gradient. Built at the card's pixel size so the brush geometry
    // matches the ticket exactly.
    val density = LocalDensity.current
    val wPx = with(density) { w.toPx() }
    val hPx = with(density) { h.toPx() }
    val ticketBrush = if (isMixed) {
        CurioMixedDeck.mixedDeckHeroBrush(gradient, wPx, hPx, mixSeed)
    } else {
        Brush.verticalGradient(gradient)
    }
    // v7.5 — pastel mode lightens the ticket gradient, so the content ink
    // flips from white to a deep ink of the deck color (light mode) / a
    // light tint (dark). White when pastel mode is off.
    val ink = if (isMixed) pastelFillInk(accent) else cat.onAccent()

    // ── Per-tick shuffle pulse — the front card bounces in sync with the
    //    wheel: every time the displayed topic switches, the card kicks
    //    instantly to peak scale then springs back down, rocking side to
    //    side. Even the fastest early ticks visibly jump (rhythmic pulse);
    //    the slower deceleration ticks ring out as full, readable bounces.
    //    The tilt alternates direction each tick so the rock feels organic
    //    instead of a one-way drift (the old per-topic hash rotation jumped
    //    randomly, which read as jitter).
    val tickPulse = remember { Animatable(1f) }
    var tickDir by remember { mutableStateOf(1f) }
    LaunchedEffect(topic?.id, shuffling) {
        if (!shuffling || topic == null) return@LaunchedEffect
        tickDir = -tickDir
        // v6.6 — calm breath instead of a kick: the card lifts barely
        // (1.02) and glides back on a heavily damped, low-stiffness
        // spring, so each tick reads as a soft pulse, never a slam.
        tickPulse.snapTo(1.02f)
        tickPulse.animateTo(1f, spring(dampingRatio = 0.85f, stiffness = 420f))
    }

    // ── Category switch — one welcoming bounce as the deck re-fans to the
    //    new category's topics (also fires on first mount).
    LaunchedEffect(cat.id) {
        if (!shuffling && !landed) {
            tickPulse.snapTo(1f)
            tickPulse.animateTo(1.025f, CurioMotion.Springs.Bouncy)
            tickPulse.animateTo(1f, CurioMotion.Springs.Elastic)
        }
    }

    // ── Landing settle — seamless handoff from the shuffle tick pulse to
    //    the elastic rest spring. On landing, snap to wherever the pulse
    //    left off (zero visual jump) then spring down to rest scale.
    val settleScale = remember { Animatable(1f) }
    val settleY = remember { Animatable(0f) }

    // Snap both to the pulse's last position on landing (zero visual jump),
    // reset to rest when a new shuffle begins.
    LaunchedEffect(landed) {
        if (landed) {
            settleScale.snapTo(tickPulse.value)
            settleY.snapTo(-(tickPulse.value - 1f) * 12f)
        } else {
            settleScale.snapTo(1f)
            settleY.snapTo(0f)
        }
    }

    // Settle scale + vertical position in parallel (separate coroutines)
    // so the card lands as one unified glide, not two sequential springs.
    // v6.6 — the landing settle uses the controlled Deliberate spring (85%
    // damping, no bounce) instead of the extreme Elastic overshoot, so the
    // wheel's stop reads as a confident rest, not a violent bounce.
    LaunchedEffect(landed) {
        if (landed) settleScale.animateTo(LandedRestScale, CurioMotion.Springs.Deliberate)
    }
    LaunchedEffect(landed) {
        if (landed) settleY.animateTo(0f, CurioMotion.Springs.Deliberate)
    }

    // Outer Box padded 12dp beyond card for shadow breathing room.
    // Inner clip layer keeps rounded corners crisp during scale.
    Box(
        modifier = Modifier
            .size(w + 24.dp, h + 24.dp)
            .graphicsLayer {
                // Idle and shuffling both track tickPulse (rest = exactly 1f);
                // the category-switch + per-tick bounces ride on it, and the
                // landing handoff snaps to whatever value it left off at.
                scaleX = if (landed) settleScale.value else tickPulse.value
                scaleY = if (landed) settleScale.value else tickPulse.value
                // v6.6 — the per-tick rock is a gentle tilt now (16° vs the
                // old 40°) so the card breathes instead of whipping side to
                // side, and the vertical hop shrinks to match.
                rotationZ = if (shuffling) (tickPulse.value - 1f) * 16f * tickDir else 0f
                translationY = if (landed) settleY.value else -(tickPulse.value - 1f) * 12f
            }
            .zIndex(10f)
            .then(
                if (enabled) Modifier.clickable(
                    indication = null,
                    interactionSource = null,
                    onClick = onTap
                ) else Modifier
            )
    ) {
        // Inner clip layer centered in outer box — prevents sharp edges during scale
        Box(
            modifier = Modifier
                .size(w, h)
                .align(Alignment.Center)
                .clip(RoundedCornerShape(30.dp))
        ) {
            Surface(
                shape = RoundedCornerShape(30.dp),
                color = Color.Transparent,
                shadowElevation = 0.dp,
                // Subtle outline — a slim edge that traces the ticket
                // silhouette so the hero card reads as a distinct surface
                // above the dimmer peek cards behind it (ink-colored in
                // pastel mode so it doesn't vanish on the pastel fill).
                border = BorderStroke(1.5.dp, ink.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            ticketBrush,
                            RoundedCornerShape(30.dp)
                        )
                ) {
                    // Gradient card — no side rule needed
                    // ── Watermark glyph — large, decorative ────────────
                    CurioIcon(
                        name = glyph,
                        contentDescription = null,
                        tint = ink.copy(alpha = 0.16f),
                        size = 150.dp,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 6.dp)
                    )

                    // ── Content column ─────────────────────────────────
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Subtype badge + landed check
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = ink.copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = topic?.subtype ?: "…",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ink,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                            if (landed) {
                                Surface(shape = CircleShape, color = Color.White) {
                                    CurioIcon(
                                        CurioIcons.Check, null,
                                        tint = ink,
                                        size = 16.dp,
                                        modifier = Modifier.padding(3.dp)
                                    )
                                }
                            }
                        }

                        // Name + tags + teaser — v6.6: reels with the deck.
                        // Previously the hero content snapped instantly on
                        // every tick; now it glides like a card rising from
                        // the back of the deck to the front — incoming
                        // content slides up from the lower edge while the
                        // outgoing exits upward, eased so each tick is a
                        // readable glide instead of a hard cut.
                        AnimatedContent(
                            targetState = topic,
                            transitionSpec = {
                                if (shuffling) {
                                    // v6.10 — same rhythm as the peek wipes
                                    // (under the 200ms tick floor) with a
                                    // stronger height/2 rise so the front
                                    // content visibly glides up as the deck
                                    // streams. clip=false keeps the title
                                    // reel from being sliced mid-slide.
                                    (slideInVertically(
                                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                                    ) { height -> height / 2 } +
                                        fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing))) togetherWith
                                    (slideOutVertically(
                                        animationSpec = tween(260, easing = FastOutSlowInEasing)
                                    ) { height -> -height / 2 } +
                                        fadeOut(animationSpec = tween(260, easing = FastOutSlowInEasing))) using SizeTransform(clip = false)
                                } else {
                                    (slideInVertically(
                                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                                    ) { height -> height / 4 } +
                                        fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing))) togetherWith
                                    (slideOutVertically(
                                        animationSpec = tween(260, easing = FastOutSlowInEasing)
                                    ) { height -> -height / 4 } +
                                        fadeOut(animationSpec = tween(260, easing = FastOutSlowInEasing))) using SizeTransform(clip = false)
                                }
                            },
                            label = "heroContentReel"
                        ) { currentTopic ->
                        Column {
                            Text(
                                text = currentTopic?.name ?: "Ready when you are",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    lineHeight = 34.sp
                                ),
                                color = ink,

                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (currentTopic != null && currentTopic.tags.isNotEmpty()) {
                                Spacer(Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    currentTopic.tags.take(2).forEach { tag ->
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                color = ink.copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ink,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            if (currentTopic != null && landed) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = currentTopic.teaser,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ink.copy(alpha = 0.88f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        }

                        // Tap hint — "tap to spin" idle, "tap to open" once
                        // landed, and a pulsing "Opening…" during the brief
                        // opening handoff.
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (opening) {
                                OpeningPulseDot(tint = ink)
                                Text(
                                    text = "Opening…",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = ink.copy(alpha = 0.88f)
                                )
                            } else {
                                CurioIcon(
                                    if (landed) CurioIcons.ChevronRight else CurioIcons.Casino, null,
                                    tint = ink,
                                    size = 16.dp
                                )
                                Text(
                                    text = when {
                                        landed -> "Tap to open"
                                        shuffling -> "Shuffling…"
                                        else -> "Press Shuffle"
                                    },
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = ink.copy(alpha = 0.88f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Slim "deck" peek card fanned behind the hero ticket — hints at the
 * neighboring topic from the visible edge (top peek = next up, bottom
 * peek = previous).
 */
@Composable
private fun PeekCard(
    slot: Int,
    scale: Float = 1f,
    accent: Color,
    cat: CurioCategory,
    topic: CurioTopic?,
    shuffling: Boolean
) {
    val isTop = slot < 0
    val far = kotlin.math.abs(slot) == 2
    // Slightly lower + wider fan: the whole deck sits a few px closer to
    // the spin button and the far pair is spread a touch more so each
    // layer reads as a separate card instead of one blurred pile.
    // v6.11 — compact screens scale the fan offsets + card sizes down so
    // the deck keeps the same look, just tighter on short screens.
    val yOff = when (slot) {
        -2 -> -178f * scale
        -1 -> -134f * scale
        1 -> 146f * scale
        else -> 188f * scale
    }
    // v6.5 — peek cards grew ~13% so the topic title inside each background
    // card has room to read instead of hiding behind the fan. Proportions
    // are kept — only the overall size went up, never the shape.
    val w = (if (far) 328.dp else 360.dp) * scale
    val h = (if (far) 96.dp else 116.dp) * scale
    // Corner radius scales with card height so the slim far deck cards
    // keep crisp, proportional corners instead of over-rounded ones.
    val corner = (if (far) 15.dp else 19.dp) * scale
    // Level-based shading — near cards step one shade down from the hero,
    // far cards step down again, so the deck fades into the background in
    // distinct layers. White content stays readable on the dimmed fill.
    // Mixed decks shade the blended accent so the whole deck reads mixed.
    // v7.7 — pastel mode keeps the peeks in the pastel CARD family instead
    // of the old flat mid-tones (which read neither pastel nor deep accent).
    // v7.8.1 — they sit a step DARKER than the hero ticket (which opens on
    // the pure pastel accent) so the classic level hierarchy holds: light
    // mode deepens toward black (near 0.16 / far 0.28), dark mode deepens
    // the muted pastel twin (near 0.14 / far 0.26).
    val pastelMode = AppPreferences.pastelColorsState
    val darkMode = isCurioDarkTheme()
    val cardColor = remember(accent, far, pastelMode, darkMode) {
        when {
            // v7.8.1 — pastel peeks sit a step DARKER than the hero ticket
            // (which opens on the pure pastel accent now), so the deck keeps
            // the classic level hierarchy. The old white-fade (0.10/0.35)
            // made the peeks BRIGHTER than the main card — the hero looked
            // dimmed by contrast and the far pair read near-white.
            pastelMode && !darkMode -> lerp(accent, Color.Black, if (far) 0.28f else 0.16f)
            pastelMode -> lerp(accent, Color.Black, if (far) 0.26f else 0.14f)
            else -> lerp(accent, Color.Black, if (far) 0.42f else 0.28f)
        }
    }
    // v7.5 — pastel mode lightens the peek fill, so content flips to a deep
    // ink of the deck color (light mode) / a light tint (dark).
    val ink = pastelFillInk(accent)

    // v7.7 — deck card redesign (EXPERIMENTAL, four independent Settings
    // toggles, each OFF by default): the flat shade becomes a top-lit
    // two-stop gradient, the generic hairline is tinted with the category's
    // own colors, near cards gain soft shadows, and near titles get two
    // readable lines. Reads each reactive preference directly so flipping
    // any toggle recomposes the deck instantly; when a flag is OFF its
    // feature resolves to the classic flat-deck look.
    val gradientOn = AppPreferences.peekGradientState
    val hairlineOn = AppPreferences.peekHairlineState
    val shadowsOn = AppPreferences.peekShadowsState
    val titlesOn = AppPreferences.peekTitlesState
    // 1a — top-lit gradient: crown = a whisper of light at the card top,
    // base = the level shade. The top peek catching more light also
    // whispers "next up" on the reel. (Gated by the gradient toggle.)
    val fillBrush = remember(accent, far, pastelMode, darkMode) {
        val crown = if (pastelMode && !darkMode) lerp(cardColor, Color.White, 0.12f)
                    else lerp(accent, Color.White, if (far) 0.10f else 0.14f)
        Brush.verticalGradient(listOf(crown, cardColor))
    }
    // 1b — category-tinted hairline so each deck layer whispers its
    // category instead of a generic white rule. The light twin reads on the
    // DARK deck fills (both non-pastel light and dark mode); in pastel
    // light mode the fills are pale, so the deep accent ink carries the
    // edge instead (a deep-on-deep hairline would vanish — reviewer catch).
    val hairline = if (hairlineOn) {
        if (pastelMode && !darkMode) {
            cat.categoryInk().copy(alpha = if (far) 0.22f else 0.30f)
        } else {
            cat.lightAccent.copy(alpha = if (far) 0.28f else 0.40f)
        }
    } else {
        ink.copy(alpha = if (far) 0.14f else 0.22f)
    }

    Box(
        modifier = Modifier
            .size(w, h)
            .graphicsLayer {
                translationY = yOff.dp.toPx()
                rotationZ = when (slot) { -2 -> -3.5f; -1 -> -1.4f; 1 -> 1.4f; else -> 3.5f }
                scaleX = if (far) 0.92f else 0.98f
                scaleY = if (far) 0.92f else 0.98f
                // Fully opaque — translucent layers blend badly with the tilt
                // and render the card as soft/pixelated. Depth comes from
                // scale + rotation + zIndex instead of transparency.
                alpha = 1f
            }
            .zIndex(if (far) 2f else 5f)
    ) {
        AnimatedContent(
            targetState = topic,
            transitionSpec = {
                // v7.1 — direction + softness. Top peek cards (title placed
                // at the card top) feed the deck from ABOVE — their content
                // DROPS down into the card — while bottom peeks rise up, so
                // the fan streams toward the hero from both ends and a top
                // card's title is never sliced off the top edge by the old
                // upward wipe (which read as cut off). The wipe itself is a
                // partial-height glide + fade (like the hero's content reel)
                // instead of a full-height hard slot cut, and the durations
                // sit UNDER the ~340ms tick floor so each step completes
                // before the next tick lands.
                val dir = if (isTop) -1f else 1f
                if (shuffling) {
                    slideInVertically(
                        animationSpec = tween(PeekWipeInMs, easing = FastOutSlowInEasing)
                    ) { height -> (height * dir * PeekWipeTravel).toInt() } +
                    fadeIn(animationSpec = tween(PeekWipeInMs, easing = FastOutSlowInEasing)) togetherWith
                    slideOutVertically(
                        animationSpec = tween(PeekWipeOutMs, easing = FastOutSlowInEasing)
                    ) { height -> (height * -dir * PeekWipeTravel).toInt() } +
                    fadeOut(animationSpec = tween(PeekWipeOutMs, easing = FastOutSlowInEasing)) using SizeTransform(clip = false)
                } else {
                    // Idle re-fan (landing re-deal / category switch) — a
                    // slower, softer pass in the same per-side direction.
                    slideInVertically(
                        animationSpec = tween(PeekIdleInMs, easing = FastOutSlowInEasing)
                    ) { height -> (height * dir * PeekWipeTravel).toInt() } +
                    fadeIn(animationSpec = tween(PeekIdleInMs, easing = FastOutSlowInEasing)) togetherWith
                    slideOutVertically(
                        animationSpec = tween(PeekIdleOutMs, easing = FastOutSlowInEasing)
                    ) { height -> (height * -dir * PeekWipeTravel).toInt() } +
                    fadeOut(animationSpec = tween(PeekIdleOutMs, easing = FastOutSlowInEasing)) using SizeTransform(clip = false)
                }
            },
            label = "peekSlot_$slot"
        ) { currentTopic ->
            Surface(
                shape = RoundedCornerShape(corner),
                color = if (gradientOn) Color.Transparent else cardColor,
                // 2 — soft ambient shadows lift the deck off the tinted page
                // (near cards sit higher than the far pair).
                shadowElevation = if (shadowsOn) (if (far) 1.dp else 3.dp) else 0.dp,
                tonalElevation = 0.dp,
                // Subtle hairline outline — kept very light so the rotated
                // stroke stays crisp instead of aliasing into pixel noise —
                // lets each deck layer read as a distinct card.
                border = BorderStroke(width = 1.dp, color = hairline),
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (gradientOn) {
                            Modifier.background(brush = fillBrush, shape = RoundedCornerShape(corner))
                        } else {
                            Modifier
                        }
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = if (isTop) Arrangement.Top else Arrangement.Bottom
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CurioIcon(
                            name = cat.iconGlyph,
                            contentDescription = null,
                            tint = ink.copy(alpha = if (far) 0.55f else 0.75f),
                            size = if (titlesOn) (if (far) 18.dp else 22.dp) else 20.dp
                        )
                        Text(
                            text = currentTopic?.name ?: "…",
                            style = if (titlesOn) {
                                if (far) {
                                    // 3 — far cards are hints, not reads: a
                                    // smaller, softer single line.
                                    MaterialTheme.typography.labelMedium.copy(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                } else {
                                    // 3 — near titles breathe: 16sp SemiBold
                                    // with light tracking and TWO lines so
                                    // long topic names stop clipping at one.
                                    MaterialTheme.typography.labelLarge.copy(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 0.15.sp
                                    )
                                }
                            } else {
                                MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            },
                            // Far deck cards dim their content too, reinforcing
                            // the layered fade into the background.
                            color = if (titlesOn && far) ink.copy(alpha = 0.72f)
                                    else ink.copy(alpha = if (far) 0.65f else 1f),
                            maxLines = if (titlesOn && !far) 2 else 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
// ═══════════════════════════════════════════════════════════════════════════
// Center spin button (with optional orbit ring)
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun SpinButton(
    tint: Color,
    isShuffling: Boolean,
    landedTopic: CurioTopic?,
    pulseScale: Float,
    enabled: Boolean,
    compact: Boolean = false,
    onClick: () -> Unit
) {
    // v6.3 — button grew a little (~7% up): 126dp idle, 108dp landed.
    // v6.11 — compact screens step the button + orbit down ~11% so the
    // pinned Categories/Filter bar always stays on screen.
    val buttonSize = if (compact) {
        if (landedTopic != null) 96.dp else 112.dp
    } else {
        if (landedTopic != null) 108.dp else 126.dp
    }
    Box(
        modifier = Modifier.size(if (compact) 156.dp else 176.dp),
        contentAlignment = Alignment.Center
    ) {
        OrbitRing(active = isShuffling, color = tint, modifier = Modifier.fillMaxSize())
        Surface(
            onClick = onClick,
            enabled = enabled,
            shape = CircleShape,
            // v6.2 — the dice button keeps its filled category color in EVERY
            // state (idle + landed "Tap to open"), so the CTA never goes grey.
            color = tint,
            shadowElevation = 0.dp,
            modifier = Modifier
                .size(buttonSize)
                .scale(pulseScale.coerceIn(0.9f, 1.10f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // v5.10 — the dice shows in EVERY state: tumbling while
                // shuffling, a steady white dice on the filled accent.
                // v6.10 — the tumble MORPHS into the resting dice (spring
                // scale + fade) instead of hard-swapping, so the end of a
                // spin reads as the die settling — never an abrupt stop.
                AnimatedContent(
                    targetState = isShuffling,
                    transitionSpec = {
                        (scaleIn(
                            initialScale = 0.55f,
                            animationSpec = spring(dampingRatio = 0.55f, stiffness = 380f)
                        ) + fadeIn(animationSpec = tween(170))) togetherWith
                        (scaleOut(
                            targetScale = 0.55f,
                            animationSpec = tween(150)
                        ) + fadeOut(animationSpec = tween(150)))
                    },
                    label = "diceMorph"
                ) { shuffling ->
                    if (shuffling) {
                        ShuffleGlyph(tint = pastelFillInk(tint), modifier = Modifier.size(72.dp))
                    } else {
                        // Gentle idle breathe on the resting die — a slow,
                        // even pulse so the settled dice stays alive.
                        val idleBreathe = rememberInfiniteTransition(label = "diceIdle")
                        val breathe by idleBreathe.animateFloat(
                            initialValue = 0f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1800, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "diceIdleBreathe"
                        )
                        CurioIcon(
                            CurioIcons.Casino, null,
                            tint = pastelFillInk(tint),
                            size = if (landedTopic != null) 52.dp else 60.dp,
                            modifier = Modifier.graphicsLayer {
                                scaleX = 1f + breathe * 0.05f
                                scaleY = 1f + breathe * 0.05f
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Rotating ring of 8 small dots around the spin button during shuffle.
 */
@Composable
private fun OrbitRing(active: Boolean, color: Color, modifier: Modifier = Modifier) {
    if (!active) return
    val infinite = rememberInfiniteTransition(label = "orbit")
    val rot by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing)
        ),
        label = "orbitRot"
    )
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = (size.minDimension / 2f) - 8f.dp.toPx()
        val dotR = 3f.dp.toPx()
        val n = 8
        rotate(degrees = rot, pivot = Offset(cx, cy)) {
            for (i in 0 until n) {
                val a = (i.toFloat() / n) * (2f * Math.PI.toFloat())
                val dx = cos(a) * radius
                val dy = sin(a) * radius
                drawCircle(
                    color = color.copy(alpha = 0.85f),
                    radius = dotR,
                    center = Offset(cx + dx, cy + dy)
                )
            }
        }
    }
}

@Composable
private fun ShuffleGlyph(tint: Color, modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "shuffleGlyph")
    // v5.10 — smooth, unhurried tumble: LinearEasing wraps 360°→0° with no
    // visible snap (the old FastOutSlowIn + Restart eased out then jumped
    // back, which read as fast and janky). The dot pattern is rotationally
    // symmetric, so the wrap-around is invisible — a true seamless loop.
    // 1600ms per turn completes ~1.5–2 rotations inside the 2.4–3.2s
    // shuffle window — fluid, never frantic, and never stalled.
    val angle by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing)
        ),
        label = "shuffleAngle"
    )
    // Gentle breathe so the die feels alive while it rolls.
    val pulse by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shufflePulse"
    )
    // v6.10 — a slow vertical bob so the die reads as shaking in the cup
    // while it rolls, not just spinning in place.
    val bob by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shuffleBob"
    )
    Canvas(modifier = modifier) {
        val r = size.minDimension / 2f
        val cx = size.width / 2f
        // cy lifts while the breathe is at rest, so the bob and the pulse
        // rock through a gentle, non-fighting loop.
        val cy = size.height / 2f - bob * r * 0.08f
        val breathe = 1f + pulse * 0.06f
        rotate(degrees = angle, pivot = Offset(cx, cy)) {
            for (i in 0 until 6) {
                val a = (i.toFloat() / 6) * (2f * Math.PI.toFloat())
                drawCircle(
                    color = tint,
                    radius = r * (0.15f * breathe),
                    center = Offset(cx + cos(a) * r * 0.58f, cy + sin(a) * r * 0.58f)
                )
            }
        }
    }
}

/**
 * Small pulsing dot shown on the landed ticket during the opening
 * pause — the subtle heartbeat that says the reveal is about to happen.
 */
@Composable
private fun OpeningPulseDot(tint: Color = Color.White) {
    val infinite = rememberInfiniteTransition(label = "openingPulse")
    val pulse by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(320, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "openingPulseScale"
    )
    Box(
        modifier = Modifier
            .size(7.dp)
            .graphicsLayer {
                scaleX = 1f + pulse * 0.5f
                scaleY = 1f + pulse * 0.5f
                this.alpha = 1f - pulse * 0.4f
            }
            .clip(CircleShape)
            // v7.5 — wears the ticket's ink so the heartbeat reads on the
            // pastel-lightened ticket fill (white when pastel mode is off).
            .background(tint.copy(alpha = 0.9f))
    )
}

// ═══════════════════════════════════════════════════════════════════════════
// Bottom bar — Categories · Filter (solid control buttons)
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun BottomCta(
    cat: CurioCategory,
    mixedCount: Int = 1,
    filterActiveCount: Int,
    onCategories: () -> Unit,
    onFilter: () -> Unit,
    vertical: Boolean = false
) {
    val hasFilters = filterActiveCount > 0

    // Anchored paper tray. v6.2 — it wore the SAME category-tint wash as
    // the page background; now transparent so the Categories/Filter buttons
    // sit directly on the Spin page background with no tinted band between
    // them and the nav bar.
    Surface(
        color = Color.Transparent,
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (vertical) {
            // ── Extra-compact edge buttons (v7.2) — on very short screens
            //    the bottom Categories/Filter row becomes two TALL pills
            //    pinned to the left/right screen edges, so the middle stays
            //    clear for the deck band and everything fits.
            // v7.4 — the Scaffold already ends the content area exactly at
            // the app's bottom nav bar (innerPadding.bottom = bar height),
            // so the extra navigationBarsPadding() here pushed the buttons
            // up by the system gesture/3-button inset — the "floating above
            // the nav bar" gap. Removed so the bar sits flush on the nav bar.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                VerticalDeckButton(
                    label = if (mixedCount > 1) "Mixed · $mixedCount" else cat.displayName,
                    icon = cat.iconGlyph,
                    cat = cat,
                    selected = true,
                    onClick = onCategories,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
                VerticalDeckButton(
                    label = if (hasFilters) "Filter · $filterActiveCount" else "Filter",
                    icon = CurioIcons.Search,
                    cat = cat,
                    selected = hasFilters,
                    onClick = onFilter,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        } else {
            // v7.4 — same inset fix as the vertical branch above: the
            // content area already ends above the app's bottom nav bar, so
            // navigationBarsPadding() only created a gap under the buttons.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // ── Categories · Filter — image-led deck buttons ────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DeckControlButton(
                        // A multi-select deck names itself "Mixed · N" so the
                        // mix is obvious at a glance instead of the first
                        // category's name.
                        label = if (mixedCount > 1) "Mixed · $mixedCount" else cat.displayName,
                        icon = cat.iconGlyph,
                        cat = cat,
                        selected = true,
                        onClick = onCategories,
                        modifier = Modifier.weight(1f)
                    )
                    DeckControlButton(
                        label = if (hasFilters) "Filter · $filterActiveCount" else "Filter",
                        icon = CurioIcons.Search,
                        cat = cat,
                        selected = hasFilters,
                        onClick = onFilter,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Tall vertical pill used by the extra-compact bottom bar (v7.2) — icon
 * over a stacked label, pinned to the left/right screen edge (Categories
 * left, Filter right) so the middle of a very short screen stays clear for
 * the deck band. Same fill/border language as [DeckControlButton].
 */
@Composable
private fun VerticalDeckButton(
    label: String,
    icon: String,
    cat: CurioCategory,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = if (selected) cat.themedAccent() else cat.categorySurface(MaterialTheme.colorScheme.surfaceContainerHigh),
        border = if (selected) null else cat.categoryBorder(),
        shadowElevation = 0.dp,
        modifier = modifier.size(width = 54.dp, height = 112.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CurioIcon(
                icon, null,
                tint = if (selected) cat.onAccent() else cat.categoryInk(),
                size = 22.dp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = if (selected) cat.onAccent() else cat.categoryInk(),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


@Composable
private fun DeckControlButton(
    label: String,
    icon: String,
    cat: CurioCategory,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Solid fills — no translucent tint, no border. Selected buttons get the
    // full accent color with white content; unselected buttons get a solid
    // surface fill with theme-aware accent ink (stays readable on the
    // midnight dark surfaces).
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = if (selected) cat.themedAccent() else cat.categorySurface(MaterialTheme.colorScheme.surfaceContainerHigh),
        border = if (selected) null else cat.categoryBorder(),
        shadowElevation = 0.dp,
        modifier = modifier.height(62.dp)
    ) {
        Row(
            // The icon + label group sits CENTERED in the pill box (not
            // left-flush), so Categories/Filter read as balanced buttons.
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            // spacedBy + CenterHorizontally keeps the icon/text gap while
            // centering the pair as one unit inside the pill.
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
        ) {
            CurioIcon(
                icon, null,
                tint = if (selected) cat.onAccent() else cat.categoryInk(),
                size = 24.dp
            )
            Text(
                text = label,
                // Text-only bump: 14sp → 16sp (icon stays 24dp) so the
                // button labels read a little larger per user request.
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = if (selected) cat.onAccent() else cat.categoryInk(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// ═══════════════════════════════════════════════════════════════════════════
// Full-screen category picker dialog — immersive tile grid
// ═══════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryPickerSheet(
    currentCat: CurioCategory,
    onDismiss: () -> Unit,
    onCategorySelected: (CurioCategory) -> Unit,
    onCategoriesSelected: (List<CurioCategory>) -> Unit,
    onBrowseAll: () -> Unit
) {
    val categories = remember { CurioCategories.visible }
    // Default = tap-to-open (single). Long-press enters multi-select mode.
    var multiSelectMode by remember { mutableStateOf(false) }
    var selectedSlugs by remember { mutableStateOf(setOf<String>()) }

    // Same full-screen + swipe-down-dismiss pattern as the filter page — a
    // ModalBottomSheet expanded to full height with a drag handle.
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        // v6.6 — the full-screen category selection page wears the
        // same category tint wash as the Spin page it sits on, so
        // the picker never flashes a foreign plain background.
        containerColor = currentCat.categoryBackgroundWash(),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
                    // ── Close button + header ────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = onDismiss,
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            CurioIcon(
                                CurioIcons.Close, "Close",
                                tint = MaterialTheme.colorScheme.onSurface,
                                size = 22.dp,
                                modifier = Modifier.padding(9.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "What are we exploring?",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        // Current category indicator — or selection count in
                        // multi-select mode.
                        if (multiSelectMode) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = currentCat.themedAccent().copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (selectedSlugs.isEmpty()) "Select decks"
                                    else "${selectedSlugs.size} selected",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = currentCat.categoryInk(),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = currentCat.themedAccent().copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = currentCat.displayName,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = currentCat.categoryInk(),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    // ── Mode hint — tap to open, hold to multi-select ──
                    Text(
                        text = if (multiSelectMode) {
                            "Tap to toggle decks · Done to spin together"
                        } else {
                            "Tap a deck to spin it · hold to pick several"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                    )

                    // ── Tile grid filling the screen ────────────────
                    // v7.4 — the grid sits inside a WEIGHTED Box that is a
                    // DIRECT child of the sheet Column. Weight inside the
                    // old MorphEntrance wrapper was ignored (MorphEntrance's
                    // lambda is not a ColumnScope child), so the grid
                    // rendered at full height and pushed the Mix/Cancel row
                    // off-screen on smaller phones. The Box keeps the
                    // entrance animation AND bounds the grid, so the action
                    // row stays pinned.
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        MorphEntrance {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                            // All tiles render at once — no per-tile stagger.
                            items(categories) { cat ->
                                val slug = cat.id.routeSlug
                                CurioCategoryCard(
                                    category = cat,
                                    isSelected = if (multiSelectMode) slug in selectedSlugs
                                    else cat.id == currentCat.id,
                                    onClick = {
                                        if (multiSelectMode) {
                                            selectedSlugs = if (slug in selectedSlugs) selectedSlugs - slug
                                            else selectedSlugs + slug
                                        } else {
                                            onCategorySelected(cat)
                                        }
                                    },
                                    onLongClick = {
                                        multiSelectMode = true
                                        if (slug !in selectedSlugs) {
                                            selectedSlugs = selectedSlugs + slug
                                        }
                                    }
                                )
                            }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // ── Browse all link, or Mix row in multi-select ──
                    if (multiSelectMode) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (selectedSlugs.isEmpty()) return@Button
                                    onCategoriesSelected(
                                        categories.filter { it.id.routeSlug in selectedSlugs }
                                    )
                                },
                                enabled = selectedSlugs.isNotEmpty(),
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                CurioIcon(CurioIcons.Check, null, size = 18.dp)
                                Text(
                                    text = if (selectedSlugs.isEmpty()) "Mix" else "Mix · ${selectedSlugs.size}",
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                            TextButton(
                                onClick = {
                                    multiSelectMode = false
                                    selectedSlugs = emptySet()
                                }
                            ) {
                                Text(
                                    "Cancel",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    } else {
                        TextButton(
                            onClick = onBrowseAll,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            CurioIcon(CurioIcons.Palette, null, tint = MaterialTheme.colorScheme.primary, size = 18.dp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Browse all categories",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))
        }
    }
}

// Helpers
// ═══════════════════════════════════════════════════════════════════════════

private fun resolveTopicForSlot(
    slot: Int,
    pool: List<CurioTopic>,
    cycleIndex: Int,
    landedTopic: CurioTopic?
): CurioTopic? {
    if (pool.isEmpty()) return null
    if (landedTopic != null && slot == 0) return landedTopic
    // v6.10 — the idle fan and the spinning reel are the SAME window into
    // the hand (front = hand[cycleIndex], neighbors fanned around it), so
    // a spin starts as a seamless +1 continuation — never a jump cut.
    val idxOf = { pos: Int -> ((pos % pool.size) + pool.size) % pool.size }
    return pool[idxOf(cycleIndex + slot)]
}

/**
 * Deals a spin hand — up to 6 topics for the fan. With [center] the landed
 * topic sits at the front (hand[0]) and its neighbors fill the rest; without
 * one the hand is a plain random spread. Stable across a spin: the reel
 * rotates through it via cycleIndex instead of re-shuffling mid-spin.
 */
private fun buildDeckHand(pool: List<CurioTopic>, center: CurioTopic?): List<CurioTopic> {
    if (pool.isEmpty()) return emptyList()
    val head = if (center != null && pool.any { it.id == center.id }) listOf(center) else emptyList()
    val rest = (if (center == null) pool else pool.filterNot { it.id == center.id }).shuffled()
    return (head + rest).take(6)
}

/**
 * Weighted picker — tier bias (tier 1 human-curated marquee first), then
 * tier 2, tier 3, while excluding topics in [recentIds] and any topic the
 * user already explored (captured). Sentiment further skews the weights:
 * liked topics get 2x, disliked drop to 0.25x, and each topic's CATEGORY
 * affinity (net likes − dislikes in that category) boosts or dampens the
 * whole genre — never fully blocked. Falls back gracefully when the pool
 * is all-recent or all-explored.
 */
private fun pickFrom(
    pool: List<CurioTopic>,
    recentIds: Set<String>,
    exploredIds: Set<String>,
    sentiments: Map<String, String>,
    categoryAffinity: Map<String, Int>
): CurioTopic? {
    if (pool.isEmpty()) return null
    var candidates = pool.filterNot { it.id in recentIds }
    // Explored topics (already captured) are excluded entirely — falling
    // back to the full candidate pool only when everything is explored so
    // the shuffle never runs dry.
    val unvisited = candidates.filterNot { it.id in exploredIds }
    if (unvisited.isNotEmpty()) candidates = unvisited
    if (candidates.isEmpty()) return null
    if (candidates.size == 1) return candidates[0]

    fun baseWeight(t: CurioTopic): Double = when (t.tier) {
        1 -> 100.0
        2 -> 60.0
        3 -> 20.0
        else -> 30.0
    }

    fun weight(t: CurioTopic): Double {
        // Per-topic sentiment: a liked topic gets 2x, a disliked one drops
        // to 0.25x — it can still appear, just far less often.
        val topicFactor = when (sentiments["${t.categoryId.name}:${t.id}"]) {
            AppPreferences.SENTIMENT_LIKE -> 2.0
            AppPreferences.SENTIMENT_DISLIKE -> 0.25
            else -> 1.0
        }
        // Category affinity (net likes − dislikes in the category): a liked
        // genre shows more (up to 2.5x), a disliked genre shows less (down
        // to 0.25x) — never fully blocked.
        val aff = categoryAffinity[t.categoryId.name] ?: 0
        val categoryFactor = when {
            aff > 0 -> 1.0 + 0.5 * aff.coerceAtMost(3)
            aff < 0 -> (1.0 + 0.4 * aff).coerceAtLeast(0.25)
            else -> 1.0
        }
        return baseWeight(t) * topicFactor * categoryFactor
    }

    val totalWeight = candidates.sumOf { weight(it) }
    if (totalWeight <= 0.0) return candidates.random()
    var target = Random.nextDouble(totalWeight)
    for (topic in candidates) {
        target -= weight(topic)
        if (target < 0) return topic
    }
    return candidates.random()
}
