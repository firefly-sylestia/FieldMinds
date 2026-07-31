package com.curio.app.features.spin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.curio.app.data.AppPreferences
import com.curio.app.data.CategoryId
import com.curio.app.data.CurioCategories
import com.curio.app.data.CurioCategory
import com.curio.app.data.CurioRepositoryHolder
import com.curio.app.data.CurioTopic
import com.curio.app.data.StreakTracker
import com.curio.app.data.TopicJsonLoader
import com.curio.app.navigation.CurioRoutes
import com.curio.app.ui.components.ConfettiBurst
import com.curio.app.ui.components.CurioBackButton
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioGradients
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.CurioMotion
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
 *     inside [CurioMotion.Durations.SpinMin]..[SpinMax] (3.5–4.8s) instead
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
 */
// ════════��══════════════════════════════════════════════════════════════════
// Saveable-state savers — category persisted by enum name, filter sets as
// lists (Set<String> has no built-in Bundle saver).
// ═══════════════════════════════════════════════════════════════════════════

/** Saves the active category by its enum name; falls back to Wildcard. */
private val CategorySaver = Saver<CurioCategory, String>(
    save = { it.id.name },
    restore = { name ->
        CategoryId.values().firstOrNull { it.name == name }
            ?.let { CurioCategories.byId(it) }
            ?: CurioCategories.byId(CategoryId.WILDCARD)
    }
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
    val initialCat = remember(categorySlug) {
        categorySlug?.let { CurioCategories.byRouteSlug(it) }
            ?: CurioCategories.byId(AppPreferences.getLastSpinCategory(context))
    }

    // v5.5 — remember which category this session opened in, so the plain
    // Spin tab opens where the user left off on the next launch. byRouteSlug
    // is nullable — only persist when the slug actually resolves (v5.7.1).
    LaunchedEffect(Unit) {
        categorySlug?.let { slug ->
            CurioCategories.byRouteSlug(slug)?.let { resolved ->
                AppPreferences.setLastSpinCategory(context, resolved.id)
            }
        }
    }

    // ── Saveable screen state — survives nav away/back, rotation and ──
    //    process death (v5.3). activeCategory persists across all of them;
    //    filters + recent history are keyed per category so switching
    //    categories still resets them to fresh.
    var activeCategory by rememberSaveable(stateSaver = CategorySaver) { mutableStateOf(initialCat) }
    val pool by produceState<List<CurioTopic>>(initialValue = emptyList(), activeCategory.id) {
        value = TopicJsonLoader.load(activeCategory.id)
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
    var landedTopicName by rememberSaveable(activeCategory.id) { mutableStateOf<String?>(null) }
    // v5.6 — true once THIS landing has already opened by tap; reset per spin.
    var landingAlreadyOpened by rememberSaveable(activeCategory.id) { mutableStateOf(false) }
    val landedTopic: CurioTopic? = remember(landedTopicName, filteredPool) {
        landedTopicName?.let { name ->
            filteredPool.firstOrNull { it.name == name }
                ?: TopicJsonLoader.cached(activeCategory.id)?.firstOrNull { it.name == name }
        }
    }
    // True only during an explicit opening handoff; keeps copy flexible if
    // a future shared-element transition delays navigation.
    var isOpening by remember { mutableStateOf(false) }
    var recentTopicIds by rememberSaveable(activeCategory.id, stateSaver = StringSetSaver) {
        mutableStateOf(setOf<String>())
    }

    val displayPool = remember(filteredPool) {
        if (filteredPool.isEmpty()) emptyList()
        else {
            val s = filteredPool.shuffled()
            if (s.size >= 6) s.take(6) else s
        }
    }
    var cycleIndex by remember(shuffleCount) { mutableIntStateOf(0) }
    val cat = activeCategory

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
            // Sinusoidal ease-out: slows down with a natural curve, not
            // an abrupt cubic halt.
            val eased = sin((1f - progress) * Math.PI.toFloat() / 2f)
            val interval = (40L + (360L * eased).toLong()).coerceAtMost(400L)
            cycleIndex = ++tick
            // Soft ratcheting tick — light haptic on each card cycle.
            // As intervals lengthen, ticks naturally space out like a
            // prize wheel settling.
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            delay(interval)
            if (System.currentTimeMillis() - start >= durationMs) break
        }
        shuffling = false

        // Pick a single topic
        val primary = pickFrom(filteredPool, recentTopicIds)
        landedTopicName = primary?.name
        if (primary != null) {
            val idx = displayPool.indexOfFirst { it.id == primary.id }
            if (idx >= 0) cycleIndex = idx
            recentTopicIds = (recentTopicIds + primary.id).toList().takeLast(20).toSet()
            StreakTracker.recordActivity(context)
        }
        confettiTrigger++
    }

    // ── Landed topics open only by user intent ───────────────────────
    // The center card is no longer a spin trigger: it opens an already
    // landed topic, while the Shuffle CTA owns all spin/shuffle starts.

    // ── v5.9 — landed card stays tappable until the user explicitly
    //    spins/shuffles again.  No longer auto-clears when explored.

    // ── Animations ────────────────────────────────────────────────────
    val buttonPulse by animateFloatAsState(
        targetValue = if (shuffling) 1.06f else 1f,
        animationSpec = CurioMotion.Springs.Snappy,
        label = "buttonPulse"
    )
    // ── Overall layout ─────────────────────────────────────────────────
    // Paper surfaces sit directly on the quiet theme background. All depth
    // comes from opaque cards, crisp rules, and elevation—not ambient washes.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
        // ── 1. Top bar — back, category name, topic count ───────────
        TopBar(
            cat = cat,
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

        // ── Breathing room — keeps the header off the deck ────────────
        Spacer(Modifier.height(14.dp))

        // ── 2. Carousel (interactive cards) ─────────────────────────
        // Tapping the center card opens a landed topic only; the bottom
        // Shuffle CTA owns starting or re-starting the shuffle.
        Carousel(
            cat = cat,
            displayPool = displayPool,
            cycleIndex = cycleIndex,
            shuffling = shuffling,
            landedTopic = landedTopic,
            opening = isOpening,
            enabled = filteredPool.isNotEmpty() && !shuffling,
            onCardTap = {
                if (shuffling || filteredPool.isEmpty()) return@Carousel
                val resolved = landedTopic
                    ?: landedTopicName?.let { name ->
                        TopicJsonLoader.cached(cat.id)?.firstOrNull { it.name == name }
                    }
                if (resolved != null) {
                    landingAlreadyOpened = true
                    navController.navigate(CurioRoutes.revealFor(cat.id.routeSlug, resolved.name)) {
                        launchSingleTop = true
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // ── 3. Center spin button — the ONLY shuffle CTA (v6) ──────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            SpinButton(
                tint = cat.accent,
                isShuffling = shuffling,
                landedTopic = landedTopic,
                pulseScale = buttonPulse,
                enabled = filteredPool.isNotEmpty(),
                onClick = { if (!shuffling && filteredPool.isNotEmpty()) shuffleCount++ }
            )
        }

        // ── 4. Breathing room — keeps the bottom bar pinned to the
        //    screen edge instead of leaving dead space below it ─────
        Spacer(Modifier.weight(1f))

        // ── 5. Bottom bar — Categories · Filter (controls only) ────
        // No duplicate shuffle button: the big center SpinButton above
        // owns all spin starts, so the bottom bar is controls only.
        BottomCta(
            cat = cat,
            filterActiveCount = activeFilters.size + activeSubtypes.size,
            onCategories = { showCategoryPicker = true },
            onFilter = { showFilters = true }
        )
        }
    }



    // ── CategoryPickerSheet ───────────────────────────────────────────
    if (showCategoryPicker) {
        CategoryPickerSheet(
            currentCat = cat,
            onDismiss = { showCategoryPicker = false },
            onCategorySelected = { c ->
                activeCategory = c
                // v5.5 — persist so the Spin tab reopens on this category
                // after the app is killed and relaunched.
                AppPreferences.setLastSpinCategory(context, c.id)
                showCategoryPicker = false
            },
            onBrowseAll = {
                showCategoryPicker = false
                navController.navigate(CurioRoutes.PICKER)
            }
        )
    }

    // ── ModalBottomSheet — compact multi-select filter dialog ──────────
    if (showFilters) {
        FilterSheet(
            cat = cat,
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
            colors = listOf(cat.accent, cat.tint, CurioColors.ButterYellow),
            trigger = confettiTrigger,
            particleCount = CurioMotion.ConfettiParticleCountLarge,
            modifier = Modifier.fillMaxSize(),
            onComplete = {}
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
                tint = cat.accent,
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
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
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
                CurioIcon(cat.iconGlyph, null, tint = cat.accent, size = 22.dp)
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
                                accent = cat.accent,
                                onRemove = { draftSubtypes = draftSubtypes - st }
                            )
                        }
                        draftFilters.forEach { tag ->
                            ActiveFilterChip(
                                label = tag,
                                accent = cat.accent,
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
                                accent = cat.accent,
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
                                accent = cat.accent,
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
                                accent = cat.accent,
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
                                accent = cat.accent,
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
                    containerColor = cat.accent,
                    contentColor = CurioColors.DeepPlum
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                CurioIcon(CurioIcons.Check, null, tint = CurioColors.DeepPlum, size = 18.dp)
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
                color = CurioColors.DeepPlum
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
    onClick: () -> Unit
) {
    // Plain Surface + clickable (no M3 minimum touch-target inflation) keeps
    // the chips compact even with 100+ tags in the sheet.
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) accent else MaterialTheme.colorScheme.surfaceContainerHigh,
        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
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
                color = if (selected) CurioColors.DeepPlum else MaterialTheme.colorScheme.onSurface,
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
private const val LandedRestScale = 1.04f

@Composable
private fun Carousel(
    cat: CurioCategory,
    displayPool: List<CurioTopic>,
    cycleIndex: Int,
    shuffling: Boolean,
    landedTopic: CurioTopic?,
    opening: Boolean,
    enabled: Boolean,
    onCardTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val poolSize = displayPool.size
    Box(
        modifier = modifier.height(396.dp),
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
                    shuffling = shuffling,
                    landedTopic = landedTopic
                )
                if (slot == 0) {
                    HeroTicketCard(
                        accent = cat.accent,
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
                        cat = cat,
                        topic = topic
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
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shadowElevation = 0.dp,
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
                    tint = cat.accent.copy(alpha = 0.5f),
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
    glyph: String,
    topic: CurioTopic?,
    cat: CurioCategory,
    landed: Boolean,
    shuffling: Boolean,
    opening: Boolean,
    enabled: Boolean,
    onTap: () -> Unit
) {
    val w = 270.dp
    val h = 292.dp
    val ticketGradient = remember(cat.id) {
        if (cat.id == CategoryId.WILDCARD) CurioGradients.wildcardCardGradient()
        else CurioGradients.cardGradient(accent)
    }

    // ── Fluid shuffle bounce — smooth 0→1→0 sine wave drives a gentle
    //    scale pulse + vertical bob on the front card while shuffling.
    //    sin() smooths the sawtooth restart so the bounce never snaps.
    val heroPulse by rememberInfiniteTransition(label = "heroBounce")
        .animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "heroPulse"
        )
    val bounceWave = sin(heroPulse * kotlin.math.PI.toFloat()) // 0→1→0
    // Single source of truth for the wave values — the graphicsLayer and
    // the landing snap must stay in sync or the handoff would jump.
    val waveScale = 1f + bounceWave * 0.035f
    val waveY = -6f - bounceWave * 8f

    // ── Landing settle — seamless handoff from the shuffle bounce wave
    //    to the elastic rest spring.  On landing, snap to wherever the
    //    wave left off (zero visual jump) then spring down to the rest
    //    scale + vertical position with the Elastic spring.
    val settleScale = remember { Animatable(1f) }
    val settleY = remember { Animatable(0f) }

    // Snap both to the wave's last position on landing (zero visual jump),
    // reset to rest when a new shuffle begins.
    LaunchedEffect(landed) {
        if (landed) {
            settleScale.snapTo(waveScale)
            settleY.snapTo(waveY)
        } else {
            settleScale.snapTo(1f)
            settleY.snapTo(0f)
        }
    }

    // Settle scale + vertical position in parallel (separate coroutines)
    // so the card lands as one unified bounce, not two sequential springs.
    LaunchedEffect(landed) {
        if (landed) settleScale.animateTo(LandedRestScale, CurioMotion.Springs.Elastic)
    }
    LaunchedEffect(landed) {
        if (landed) settleY.animateTo(0f, CurioMotion.Springs.Elastic)
    }

    // Outer Box padded 12dp beyond card for shadow breathing room.
    // Inner clip layer keeps rounded corners crisp during scale.
    Box(
        modifier = Modifier
            .size(w + 24.dp, h + 24.dp)
            .graphicsLayer {
                scaleX = when {
                    landed -> settleScale.value
                    shuffling -> waveScale
                    else -> 1f
                }
                scaleY = when {
                    landed -> settleScale.value
                    shuffling -> waveScale
                    else -> 1f
                }
                rotationZ = if (shuffling) ((cycleIndexPulse(glyph, topic?.id) - 0.5f) * 3.5f) else 0f
                translationY = when {
                    landed -> settleY.value
                    shuffling -> waveY
                    else -> 0f
                }
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
                // Subtle outline — a slim white edge that traces the ticket
                // silhouette so the hero card reads as a distinct surface
                // above the dimmer peek cards behind it.
                border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(ticketGradient),
                            RoundedCornerShape(30.dp)
                        )
                ) {
                    // Gradient card — no side rule needed
                    // ── Watermark glyph — large, decorative ────────────
                    CurioIcon(
                        name = glyph,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.16f),
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
                                color = Color.White.copy(alpha = 0.22f)
                            ) {
                                Text(
                                    text = topic?.subtype ?: "…",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                            if (landed) {
                                Surface(shape = CircleShape, color = Color.White) {
                                    CurioIcon(
                                        CurioIcons.Check, null,
                                        tint = accent,
                                        size = 16.dp,
                                        modifier = Modifier.padding(3.dp)
                                    )
                                }
                            }
                        }

                        // Name + tags + teaser
                        Column {
                            Text(
                                text = topic?.name ?: "Ready when you are",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    lineHeight = 34.sp
                                ),
                                color = Color.White,

                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (topic != null && topic.tags.isNotEmpty()) {
                                Spacer(Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    topic.tags.take(2).forEach { tag ->
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                color = Color.White.copy(alpha = 0.22f)
                            ) {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            if (landed && topic != null) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = topic.teaser,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.88f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
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
                                OpeningPulseDot()
                                Text(
                                    text = "Opening…",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White.copy(alpha = 0.88f)
                                )
                            } else {
                                CurioIcon(
                                    if (landed) CurioIcons.ChevronRight else CurioIcons.Casino, null,
                                    tint = Color.White,
                                    size = 16.dp
                                )
                                Text(
                                    text = when {
                                        landed -> "Tap to open"
                                        shuffling -> "Shuffling…"
                                        else -> "Press Shuffle"
                                    },
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White.copy(alpha = 0.88f)
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
    cat: CurioCategory,
    topic: CurioTopic?
) {
    val isTop = slot < 0
    val far = kotlin.math.abs(slot) == 2
    val yOff = when (slot) {
        -2 -> -178f
        -1 -> -136f
        1 -> 136f
        else -> 178f
    }
    val w = if (far) 272.dp else 300.dp
    val h = if (far) 78.dp else 96.dp
    // Corner radius scales with card height so the slim far deck cards
    // keep crisp, proportional corners instead of over-rounded ones.
    val corner = if (far) 12.dp else 16.dp
    // Solid card color derived from the accent, but a deeper shade than
    // the hero ticket so the fan of background cards recedes and the
    // center card pops. White content stays readable on the dimmed fill.
    val cardColor = remember(cat.id) {
        val base = if (cat.id == CategoryId.WILDCARD) CurioColors.CoralBlush else cat.accent
        lerp(base, Color.Black, 0.28f)
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
                slideInVertically(
                    animationSpec = tween(240, easing = FastOutSlowInEasing)
                ) { height -> if (isTop) -height / 3 else height / 3 } +
                fadeIn(animationSpec = tween(240, easing = FastOutSlowInEasing)) togetherWith
                slideOutVertically(
                    animationSpec = tween(200, easing = FastOutSlowInEasing)
                ) { height -> if (isTop) height / 3 else -height / 3 } +
                fadeOut(animationSpec = tween(200, easing = FastOutSlowInEasing))
            },
            label = "peekSlot_$slot"
        ) { currentTopic ->
            Surface(
                shape = RoundedCornerShape(corner),
                color = cardColor,
                shadowElevation = 0.dp,
                tonalElevation = 0.dp,
                // No border — a thin stroke on a rotated rounded card aliases
                // into jagged/pixelated edges. Solid fill keeps it crisp.
                modifier = Modifier.fillMaxSize()
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
                            tint = Color.White.copy(alpha = 0.75f),
                            size = 20.dp
                        )
                        Text(
                            text = currentTopic?.name ?: "…",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            maxLines = 1,
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
    onClick: () -> Unit
) {
    val buttonSize = if (landedTopic != null) 84.dp else 100.dp
    Box(
        modifier = Modifier.size(152.dp),
        contentAlignment = Alignment.Center
    ) {
        OrbitRing(active = isShuffling, color = tint, modifier = Modifier.fillMaxSize())
        Surface(
            onClick = onClick,
            enabled = enabled,
            shape = CircleShape,
            // Opaque paper button with a strong ink edge and elevation.
            color = if (landedTopic != null) MaterialTheme.colorScheme.surfaceContainerHigh else tint,
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
                if (isShuffling) {
                    ShuffleGlyph(tint = CurioColors.DeepPlum, modifier = Modifier.size(56.dp))
                } else if (landedTopic != null) {
                    CurioIcon(
                        CurioIcons.Refresh, null,
                        tint = tint,
                        size = 36.dp
                    )
                } else {
                    CurioIcon(
                        CurioIcons.Casino, null,
                        tint = Color.White,
                        size = 44.dp
                    )
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
    val angle by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(980, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shuffleAngle"
    )
    Canvas(modifier = modifier) {
        val radius = (size.minDimension / 2f) * 0.55f
        val cx = size.width / 2f
        val cy = size.height / 2f
        rotate(degrees = angle, pivot = Offset(cx, cy)) {
            for (i in 0 until 6) {
                val a = (i.toFloat() / 6) * (2f * Math.PI.toFloat())
                drawCircle(
                    color = tint,
                    radius = radius * 0.18f,
                    center = Offset(cx + cos(a) * radius, cy + sin(a) * radius)
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
private fun OpeningPulseDot() {
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
            .background(Color.White.copy(alpha = 0.9f))
    )
}

// ═══════════════════════════════════════════════════════════════════════════
// Bottom bar — Categories · Filter (solid control buttons)
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun BottomCta(
    cat: CurioCategory,
    filterActiveCount: Int,
    onCategories: () -> Unit,
    onFilter: () -> Unit
) {
    val hasFilters = filterActiveCount > 0

    // Anchored paper tray: opaque, elevated.  No dividing rule — the
    // surface elevation alone separates it from the content above.
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
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
                    label = cat.displayName,
                    icon = cat.iconGlyph,
                    accent = cat.accent,
                    selected = true,
                    onClick = onCategories,
                    modifier = Modifier.weight(1f)
                )
                DeckControlButton(
                    label = if (hasFilters) "Filter · $filterActiveCount" else "Filter",
                    icon = CurioIcons.Search,
                    accent = cat.accent,
                    selected = hasFilters,
                    onClick = onFilter,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}


@Composable
private fun DeckControlButton(
    label: String,
    icon: String,
    accent: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Solid fills — no translucent tint, no border. Selected buttons get the
    // full accent color (DeepPlum content, matching the center SpinButton),
    // unselected buttons get a solid surface fill with accent icon + text.
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = if (selected) accent else MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 0.dp,
        modifier = modifier.height(62.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CurioIcon(
                icon, null,
                tint = if (selected) CurioColors.DeepPlum else accent,
                size = 24.dp
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = if (selected) CurioColors.DeepPlum else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun cycleIndexPulse(glyph: String, topicId: String?): Float =
    kotlin.math.abs((glyph + topicId.orEmpty()).hashCode() % 100) / 100f

// ═══════════════════════════════════════════════════════════════════════════
// ═══════════════════════════════════════════════════════════════════════════
// Full-screen category picker dialog — immersive tile grid
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun CategoryPickerSheet(
    currentCat: CurioCategory,
    onDismiss: () -> Unit,
    onCategorySelected: (CurioCategory) -> Unit,
    onBrowseAll: () -> Unit
) {
    val categories = remember { CurioCategories.visible }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

    Dialog(
        onDismissRequest = { visible = false },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false
        )
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(350, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(280)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(240, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(180))
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
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
                            onClick = { visible = false },
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
                        // Current category indicator
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = currentCat.accent.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = currentCat.displayName,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = currentCat.accent,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }

                    // ── Tile grid filling the screen ────────────────
                    MorphEntrance {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            // All tiles render at once — no per-tile stagger.
                            items(categories) { cat ->
                                CategoryPickerTile(
                                    category = cat,
                                    isSelected = cat.id == currentCat.id,
                                    onClick = { onCategorySelected(cat) }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // ── Browse all link ─────────────────────────────
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

                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }

    // ── Dismiss after animation completes ──────────────────────────
    LaunchedEffect(visible) {
        if (!visible) {
            delay(260)
            onDismiss()
        }
    }
}

/** Full-height category tile matching the Explore page style with press animation. */
@Composable
private fun CategoryPickerTile(
    category: CurioCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = CurioMotion.Springs.Press,
        label = "catTileScale"
    )

    val isWildcard = category.id == CategoryId.WILDCARD
    val cardColor = if (isWildcard) CurioColors.CoralBlush else category.accent

    Surface(
        onClick = {
            pressed = true
            onClick()
        },
        shape = RoundedCornerShape(28.dp),
        color = cardColor,
        shadowElevation = 0.dp,
        tonalElevation = if (isSelected) 6.dp else 2.dp,
        border = if (isSelected) BorderStroke(2.5.dp, Color.White.copy(alpha = 0.7f)) else null,
        modifier = Modifier
            .fillMaxWidth()
            .height(156.dp)
            .scale(scale)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Large ghost icon — decorative
            CurioIcon(
                name = category.iconGlyph,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.16f),
                size = 104.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 4.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Icon badge
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    CurioIcon(
                        name = category.iconGlyph,
                        contentDescription = null,
                        tint = Color.White,
                        size = 34.dp,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.Bottom
                ) {
                    if (isSelected) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White
                        ) {
                            CurioIcon(
                                CurioIcons.Check, null,
                                tint = cardColor,
                                size = 20.dp,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Helpers
// ═══════════════════════════════════════════════════════════════════════════

private fun resolveTopicForSlot(
    slot: Int,
    pool: List<CurioTopic>,
    cycleIndex: Int,
    shuffling: Boolean,
    landedTopic: CurioTopic?
): CurioTopic? {
    if (pool.isEmpty()) return null
    val idxOf = { pos: Int -> ((pos % pool.size) + pool.size) % pool.size }
    return when {
        landedTopic != null && slot == 0 -> landedTopic
        !shuffling -> when (slot) {
            -2 -> pool[idxOf(pool.size - 2)]
            -1 -> pool[idxOf(pool.size - 1)]
            0 -> pool[0]
            1 -> pool[idxOf(1)]
            else -> pool[idxOf(2)]
        }
        else -> when (slot) {
            -2 -> pool[idxOf(cycleIndex - 2)]
            -1 -> pool[idxOf(cycleIndex - 1)]
            0 -> pool[idxOf(cycleIndex)]
            1 -> pool[idxOf(cycleIndex + 1)]
            else -> pool[idxOf(cycleIndex + 2)]
        }
    }
}

/**
 * Weighted picker — favours tier 1 (human-curated marquee), then tier 2,
 * then tier 3, while excluding any topics in [recentIds].
 */
private fun pickFrom(pool: List<CurioTopic>, recentIds: Set<String>): CurioTopic? {
    if (pool.isEmpty()) return null
    val withoutRecents = pool.filterNot { it.id in recentIds }
    val candidates = if (withoutRecents.isNotEmpty()) withoutRecents else pool
    if (candidates.isEmpty()) return null
    if (candidates.size == 1) return candidates[0]

    val totalWeight = candidates.sumOf { t ->
        when (t.tier) { 1 -> 100; 2 -> 60; 3 -> 20; else -> 30 }
    }
    if (totalWeight <= 0) return candidates.random()
    var target = Random.nextInt(totalWeight)
    for (topic in candidates) {
        target -= when (topic.tier) { 1 -> 100; 2 -> 60; 3 -> 20; else -> 30 }
        if (target < 0) return topic
    }
    return candidates.random()
}
