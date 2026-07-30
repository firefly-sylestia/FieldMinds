package com.curio.app.features.spin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.curio.app.data.CategoryFamily
import com.curio.app.data.CategoryId
import com.curio.app.data.CurioCategories
import com.curio.app.data.CurioCategory
import com.curio.app.data.CurioTopic
import com.curio.app.data.StreakTracker
import com.curio.app.data.TopicJsonLoader
import com.curio.app.navigation.CurioRoutes
import com.curio.app.ui.components.ConfettiBurst
import com.curio.app.ui.components.CurioBackButton
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.CurioMotion
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * The Spin — see CURIO_SPEC.md §5 (v3 redesign).
 *
 * Three changes from the previous Card-Fan design:
 *  1. **Compact vertical carousel** — three stacked cards (top / center /
 *     bottom) replace the 5-card fanned layout. The center card stays
 *     fully expanded; the top + bottom slots are scaled down + faded so
 *     the user feels the depth of "more topics below / above" without
 *     burning 200dp of empty space above the dial.
 *  2. **Proper Category Menu** — a top-bar Surface chip (accent dot +
 *     category name + caret) opens a `DropdownMenu` grouped by family
 *     (Music / Movies / Books / Visual Art / Science / Wildcard). Each
 *     menu item shows family accent dot, category glyph + label, and a
 *     live topic-count badge.
 *  3. **Genre Filter Dialog** — a single "Filter" chip opens a
 *     `ModalBottomSheet` with a wrap-grid of all tags + subtypes from
 *     the active category. Multi-select with "Clear all" + "Apply".
 *
 *  Also fixed: top-bar paddings are tight (vertical 4dp instead of
 *  6dp + extra LazyRow padding), and the carousel area uses measured
 *  height instead of `weight(1f)` so the layout reads as one tight
 *  stack rather than "screen / card / big empty bottom".
 *
 * Layout (top → bottom on a 360×800 dp phone):
 *   24-44 dp   statusBarsPadding()
 *    40 dp     TopBar (Back · CategoryMenu · topic count)
 *     8 dp     Filter row
 *   ~430 dp    Carousel (3 stacked vertical cards)
 *    ~72 dp    Center spin button + status label
 *    ~72 dp    Bottom CTA (Shuffle / Explore this topic)
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SpinScreen(categorySlug: String?, navController: NavController) {
    val context = LocalContext.current
    val initialCat = remember(categorySlug) {
        categorySlug?.let { CurioCategories.byRouteSlug(it) }
            ?: CurioCategories.byId(CategoryId.WILDCARD)
    }

    var activeCategory by remember { mutableStateOf(initialCat) }
    val pool by produceState<List<CurioTopic>>(initialValue = emptyList(), activeCategory.id) {
        value = TopicJsonLoader.load(activeCategory.id)
    }

    // ── Filter state ────────────────────────────────────────────────
    var activeFilter by remember(activeCategory.id, pool) { mutableStateOf<String?>(null) }
    var activeSubtype by remember(activeCategory.id, pool) { mutableStateOf<String?>(null) }
    var showFilters by remember { mutableStateOf(false) }
    val filteredPool = remember(pool, activeFilter, activeSubtype) {
        var r = pool
        if (activeFilter != null) r = r.filter { activeFilter in it.tags }
        if (activeSubtype != null) r = r.filter { it.subtype == activeSubtype }
        r
    }
    val allSubtypes = remember(pool) { pool.map { it.subtype }.distinct().sorted() }
    val allTags = remember(pool) { pool.flatMap { it.tags }.distinct().sorted() }

    // ── Spin state ──────────────────────────────────────────────────
    var shuffling by remember { mutableStateOf(false) }
    var shuffleCount by remember { mutableIntStateOf(0) }
    var confettiTrigger by remember { mutableIntStateOf(0) }
    var landedTopic by remember { mutableStateOf<CurioTopic?>(null) }
    var recentTopicIds by remember(activeCategory.id) { mutableStateOf(setOf<String>()) }

    val displayPool = remember(filteredPool) {
        if (filteredPool.isEmpty()) emptyList()
        else {
            val s = filteredPool.shuffled()
            if (s.size >= 6) s.take(6) else s  // keep carousel fast
        }
    }
    var cycleIndex by remember(shuffleCount) { mutableIntStateOf(0) }
    val cat = activeCategory

    LaunchedEffect(activeCategory.id) {
        landedTopic = null
        shuffling = false
        activeFilter = null
        activeSubtype = null
    }

    // ── Shuffle logic — ease-out deceleration ───────────────────────
    LaunchedEffect(shuffleCount) {
        if (shuffleCount == 0 || filteredPool.isEmpty()) return@LaunchedEffect
        shuffling = true
        landedTopic = null
        val durationMs = 2200L
        val start = System.currentTimeMillis()
        // Single ease-out loop: per-tick interval grows cubically so the
        // cadence genuinely decelerates instead of stopping abruptly.
        var tick = 0
        while (true) {
            val elapsed = System.currentTimeMillis() - start
            if (elapsed >= durationMs) break
            val progress = (elapsed.toFloat() / durationMs).coerceIn(0f, 1f)
            val eased = 1f - (1f - progress) * (1f - progress) * (1f - progress)
            val interval = (60L + (300L * eased).toLong()).coerceAtMost(380L)
            cycleIndex = ++tick
            delay(interval)
            if (System.currentTimeMillis() - start >= durationMs) break
        }
        shuffling = false

        val pick = pickFrom(filteredPool, recentTopicIds)
        landedTopic = pick
        if (pick != null) {
            // Make the carousel's final cycleIndex point at the pick so the
            // centermost card locks on it.
            val idx = displayPool.indexOfFirst { it.id == pick.id }
            if (idx >= 0) cycleIndex = idx
            recentTopicIds = (recentTopicIds + pick.id).toList().takeLast(20).toSet()
            StreakTracker.recordActivity(context)
        }
        confettiTrigger++
    }

    LaunchedEffect(confettiTrigger) {
        if (confettiTrigger == 0) return@LaunchedEffect
        delay(CurioMotion.Durations.RevealHold.toLong())
        val topic = landedTopic ?: pickFrom(filteredPool, recentTopicIds) ?: return@LaunchedEffect
        navController.navigate(CurioRoutes.revealFor(cat.id.routeSlug, topic.name))
    }

    // ── Animations ──────────────────────────────────────────────────
    val landScale by animateFloatAsState(
        targetValue = if (landedTopic != null) 1.04f else 1f,
        animationSpec = CurioMotion.Springs.Elastic,
        label = "landScale"
    )
    val buttonPulse by animateFloatAsState(
        targetValue = if (shuffling) 1.06f else 1f,
        animationSpec = CurioMotion.Springs.Snappy,
        label = "buttonPulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // ── 1. Top bar — Back · CategoryMenu · CountBadge ──────────────
        TopBar(
            cat = cat,
            poolCount = pool.size,
            filteredCount = filteredPool.size,
            onBack = { navController.popBackStack() },
            onCategoryChange = { activeCategory = it },
            onPickCategory = { navController.navigate(CurioRoutes.PICKER) }
        )

        // ── 2. Filter row ──────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterTrigger(
                accent = cat.accent,
                activeCount = (if (activeFilter != null) 1 else 0) +
                              (if (activeSubtype != null) 1 else 0),
                onClick = { showFilters = true }
            )
            Spacer(Modifier.weight(1f))
            if (cat.id != CategoryId.WILDCARD && filteredPool.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = cat.accent.copy(alpha = 0.12f),
                    modifier = Modifier
                ) {
                    Text(
                        text = "${filteredPool.size} ${if (filteredPool.size == 1) "topic" else "topics"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = cat.accent,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // ── 3. Carousel area (3 stacked vertical cards) ─────────────────
        Carousel(
            cat = cat,
            displayPool = displayPool,
            cycleIndex = cycleIndex,
            shuffling = shuffling,
            landedTopic = landedTopic,
            landScale = landScale,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp, bottom = 2.dp)
        )

        // ── 4. Center spin button (with optional orbit ring) ──────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
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

        // ── 5. Bottom CTA ──────────────────────────────────────────────
        BottomCta(
            cat = cat,
            landedTopic = landedTopic,
            shuffling = shuffling,
            canSpin = filteredPool.isNotEmpty(),
            onSpin = { if (!shuffling && filteredPool.isNotEmpty()) shuffleCount++ },
            onExplore = {
                val name = landedTopic?.name ?: return@BottomCta
                navController.navigate(CurioRoutes.revealFor(cat.id.routeSlug, name))
            }
        )
    }

    // ── ModalBottomSheet — filter dialog ───────────────────────────────
    if (showFilters) {
        FilterSheet(
            cat = cat,
            subtypes = allSubtypes,
            tags = allTags,
            initialSubtype = activeSubtype,
            initialFilter = activeFilter,
            onDismiss = { showFilters = false },
            onApply = { tag, subtype ->
                activeFilter = tag
                activeSubtype = subtype
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
    onBack: () -> Unit,
    onCategoryChange: (CurioCategory) -> Unit,
    onPickCategory: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    val groups = remember {
        CategoryFamily.values().map { fam ->
            fam to CurioCategories.byFamily(fam)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CurioBackButton(onClick = onBack)

        Spacer(Modifier.width(8.dp))

        // ── CategoryMenu trigger chip ─────────────────────────────────
        Box {
            Surface(
                onClick = { menuOpen = true },
                shape = RoundedCornerShape(50),
                color = cat.accent.copy(alpha = 0.15f),
                modifier = Modifier
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Family-colored dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(cat.accent)
                    )
                    CurioIcon(
                        cat.iconGlyph,
                        null,
                        tint = cat.accent,
                        size = 16.dp
                    )
                    Text(
                        text = cat.displayName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = cat.accent,
                        maxLines = 1
                    )
                    CurioIcon(
                        CurioIcons.KeyboardArrowDown,
                        null,
                        tint = cat.accent,
                        size = 18.dp
                    )
                }
            }

            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
                    .width(280.dp)
            ) {
                Text(
                    text = "Choose a category",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp)
                )

                groups.forEachIndexed { idx, (family, cats) ->
                    if (idx > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                    }
                    Text(
                        text = familyDisplayName(family),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 2.dp)
                    )
                    cats.forEach { c ->
                        CategoryMenuItem(
                            cat = c,
                            isSelected = c.id == cat.id,
                            onClick = {
                                menuOpen = false
                                if (c.id != cat.id) onCategoryChange(c)
                            }
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                DropdownMenuItem(
                    text = {
                        Text(
                            "Browse all categories →",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    onClick = {
                        menuOpen = false
                        onPickCategory()
                    },
                    leadingIcon = {
                        CurioIcon(
                            CurioIcons.ArrowForward,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            size = 18.dp
                        )
                    }
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // ── Right-side "Spin to discover" mini badge ──────────────────
        if (poolCount > 0 && filteredCount > 0) {
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier.padding(end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CurioIcon(
                        CurioIcons.AutoAwesome,
                        null,
                        tint = cat.accent.copy(alpha = 0.6f),
                        size = 14.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryMenuItem(
    cat: CurioCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(cat.accent)
                )
                Text(
                    text = cat.displayName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (isSelected) {
                    CurioIcon(CurioIcons.Check, null, tint = cat.accent, size = 16.dp)
                }
            }
        },
        onClick = onClick,
        modifier = Modifier
    )
}

private fun familyDisplayName(family: CategoryFamily): String = when (family) {
    CategoryFamily.MUSIC -> "Music"
    CategoryFamily.MOVIES -> "Movies"
    CategoryFamily.BOOKS -> "Books"
    CategoryFamily.VISUAL_ART -> "Visual art"
    CategoryFamily.SCIENCE -> "Science"
    CategoryFamily.WILDCARD -> "Surprise"
}

// ═══════════════════════════════════════════════════════════════════════════
// Filter trigger chip + ModalBottomSheet filter dialog
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun FilterTrigger(
    accent: Color,
    activeCount: Int,
    onClick: () -> Unit
) {
    val label = if (activeCount == 0) "Filter" else "Filter · $activeCount"
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (activeCount > 0) accent.copy(alpha = 0.18f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CurioIcon(
                if (activeCount > 0) CurioIcons.Check else CurioIcons.Search,
                null,
                tint = if (activeCount > 0) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                size = 14.dp
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (activeCount > 0) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (activeCount > 0) accent else MaterialTheme.colorScheme.onSurfaceVariant
            )
            CurioIcon(
                CurioIcons.KeyboardArrowDown,
                null,
                tint = if (activeCount > 0) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                size = 14.dp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSheet(
    cat: CurioCategory,
    subtypes: List<String>,
    tags: List<String>,
    initialSubtype: String?,
    initialFilter: String?,
    onDismiss: () -> Unit,
    onApply: (tag: String?, subtype: String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var draftFilter by remember(initialFilter) { mutableStateOf(initialFilter) }
    var draftSubtype by remember(initialSubtype) { mutableStateOf(initialSubtype) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
            // ── Header ────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Filter ${cat.displayName}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (cat.id == CategoryId.WILDCARD)
                            "Refine by genre or time period"
                        else "${tags.size} genres · ${subtypes.size} ${if (subtypes.size == 1) "type" else "types"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (draftFilter != null || draftSubtype != null) {
                    TextButton(onClick = {
                        draftFilter = null
                        draftSubtype = null
                    }) {
                        Text(
                            "Clear",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Subtype section (only when more than one) ──────────────
            if (subtypes.size > 1) {
                SectionLabel("Kinds", Modifier.padding(horizontal = 24.dp))
                Spacer(Modifier.height(10.dp))
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    subtypes.forEach { st ->
                        FilterChoiceChip(
                            label = st,
                            selected = st == draftSubtype,
                            accent = cat.accent,
                            onClick = { draftSubtype = if (st == draftSubtype) null else st }
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            if (tags.isNotEmpty()) {
                SectionLabel("Genres & tags", Modifier.padding(horizontal = 24.dp))
                Spacer(Modifier.height(10.dp))
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tags.forEach { tag ->
                        FilterChoiceChip(
                            label = tag,
                            selected = tag == draftFilter,
                            accent = cat.accent,
                            onClick = { draftFilter = if (tag == draftFilter) null else tag }
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            if (subtypes.size <= 1 && tags.isEmpty()) {
                Text(
                    text = "No filters for this category yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── Apply button ──────────────────────────────────────────
            Button(
                onClick = { onApply(draftFilter, draftSubtype) },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = cat.accent,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CurioIcon(
                        CurioIcons.AutoAwesome,
                        null,
                        tint = Color.White,
                        size = 18.dp
                    )
                    Text(
                        text = "Apply filters",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

@Composable
private fun FilterChoiceChip(
    label: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) accent
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (!selected)
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (selected) {
                CurioIcon(CurioIcons.Check, null, tint = Color.White, size = 14.dp)
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium
                ),
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// 3-card vertical carousel — replaces the previous card-fan
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun Carousel(
    cat: CurioCategory,
    displayPool: List<CurioTopic>,
    cycleIndex: Int,
    shuffling: Boolean,
    landedTopic: CurioTopic?,
    landScale: Float,
    modifier: Modifier = Modifier
) {
    val poolSize = displayPool.size
    Box(
        modifier = modifier.height(400.dp),
        contentAlignment = Alignment.Center
    ) {
        if (poolSize == 0) {
            EmptyPoolHint(cat)
        } else {
            // Three slots: -1 (top, small), 0 (center, big), +1 (bottom, small)
            val slots = listOf(-1, 0, 1)
            slots.forEach { slot ->
                val topic = resolveTopicForSlot(
                    slot = slot,
                    pool = displayPool,
                    cycleIndex = cycleIndex,
                    shuffling = shuffling,
                    landedTopic = landedTopic
                )
                CarouselCard(
                    slot = slot,
                    accent = cat.accent,
                    glyph = cat.iconGlyph,
                    topic = topic,
                    landedTopic = landedTopic,
                    landScale = landScale,
                    cat = cat
                )
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
            color = cat.accent.copy(alpha = 0.10f),
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
private fun CarouselCard(
    slot: Int,
    accent: Color,
    glyph: String,
    topic: CurioTopic?,
    landedTopic: CurioTopic?,
    landScale: Float,
    cat: CurioCategory
) {
    val isCenter = slot == 0
    val w = if (isCenter) 240.dp else 200.dp
    val h = if (isCenter) 220.dp else 130.dp
    val yOff: Float = when (slot) {
        -1 -> -120f
        0 -> 0f
        else -> 120f
    }
    val s = if (isCenter) 1f else 0.78f
    val alpha = if (isCenter) 1f else 0.50f
    val corner = if (isCenter) 26.dp else 20.dp
    val bg = if (isCenter) accent.copy(alpha = 0.14f)
             else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = if (isCenter && landedTopic != null) landScale else s
                scaleY = if (isCenter && landedTopic != null) landScale else s
                this.alpha = alpha
                translationY = yOff.dp.toPx()
            }
            .zIndex(if (isCenter) 10f else 5f)
    ) {
        Surface(
            shape = RoundedCornerShape(corner),
            color = bg,
            shadowElevation = if (isCenter) 6.dp else 2.dp,
            tonalElevation = if (isCenter) 2.dp else 0.dp,
            modifier = Modifier.size(w, h)
        ) {
            if (topic != null) {
                CarouselCardContent(
                    topic = topic,
                    accent = accent,
                    isCenter = isCenter,
                    landed = landedTopic != null && isCenter
                )
            } else if (isCenter) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CurioIcon(glyph, null, tint = accent.copy(alpha = 0.5f), size = 38.dp)
                        Text(
                            text = "Tap spin to draw a topic",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CurioIcon(glyph, null, tint = accent.copy(alpha = 0.2f), size = 22.dp)
                }
            }
        }
    }
}

@Composable
private fun CarouselCardContent(
    topic: CurioTopic,
    accent: Color,
    isCenter: Boolean,
    landed: Boolean
) {
    Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: type + family accent stripe
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(if (isCenter) 18.dp else 12.dp)
                        .background(accent, RoundedCornerShape(2.dp))
                )
                Text(
                    text = if (isCenter) "Curated pick" else "Up next",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.6.sp
                    ),
                    color = accent
                )
            }
            // Middle: topic name
            Text(
                text = topic.name,
                style = if (isCenter)
                    MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold)
                else
                    MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = if (isCenter) 3 else 1,
                overflow = TextOverflow.Ellipsis
            )
            // Footer
            if (isCenter) {
                Column {
                    if (landed) {
                        Text(
                            text = topic.teaser,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = accent.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = topic.subtype,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = accent,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            if (topic.tags.isNotEmpty()) {
                                Text(
                                    text = topic.tags.first(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        Text(
                            text = topic.subtype,
                            style = MaterialTheme.typography.labelMedium,
                            color = accent
                        )
                        Spacer(Modifier.height(4.dp))
                        if (topic.tags.isNotEmpty()) {
                            Row {
                                Text(
                                    text = topic.tags.take(3).joinToString(" · "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


// ═══════════════════════════════════════════════════════════════════════════
// Center spin button (with optional orbit ring during shuffle)
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
    val size = if (landedTopic != null) 64.dp else 76.dp
    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        // ── Orbit ring of small dots (active during shuffle) ──────────
        OrbitRing(active = isShuffling, color = tint, modifier = Modifier.fillMaxSize())
        // ── Breathing halo when idle to invite the tap ─────────────────
        IdleHalo(active = enabled && !isShuffling && landedTopic == null, color = tint)
        // ── Main button ───────────────────────────────────────────────
        Surface(
            onClick = onClick,
            enabled = enabled,
            shape = CircleShape,
            color = if (landedTopic != null) tint.copy(alpha = 0.15f) else tint,
            shadowElevation = if (isShuffling) 2.dp else 8.dp,
            modifier = Modifier
                .size(size)
                .scale(pulseScale.coerceIn(0.9f, 1.10f))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (isShuffling) {
                    ShuffleGlyph(tint = Color.White, modifier = Modifier.size(46.dp))
                } else if (landedTopic != null) {
                    CurioIcon(
                        CurioIcons.Refresh, null,
                        tint = tint,
                        size = 28.dp
                    )
                } else {
                    CurioIcon(
                        CurioIcons.Casino, null,
                        tint = Color.White,
                        size = 32.dp
                    )
                }
            }
        }
    }
}

/**
 * A rotating ring of 8 small dots drawn around the spin button during
 * shuffle — gives a "slot machine" / "swept dial" feel without polluting
 * the carousel area with extra UI.
 */
@Composable
private fun OrbitRing(active: Boolean, color: Color, modifier: Modifier = Modifier) {
    if (!active) return
    val infinite = androidx.compose.animation.core.rememberInfiniteTransition(label = "orbit")
    val rot by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(1400, easing = androidx.compose.animation.core.LinearEasing)
        ),
        label = "orbitRot"
    )
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = (size.minDimension / 2f) - 8f.dp.toPx()
        val dotR = 3f.dp.toPx()
        val n = 8
        rotate(rotation = rot, pivot = Offset(cx, cy)) {
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
private fun IdleHalo(active: Boolean, color: Color) {
    if (!active) return
    val infinite = androidx.compose.animation.core.rememberInfiniteTransition(label = "halo")
    val pulse by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(1400, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "haloPulse"
    )
    Box(
        modifier = Modifier
            .size(96.dp)
            .graphicsLayer {
                scaleX = 1f + pulse * 0.35f
                scaleY = 1f + pulse * 0.35f
                this.alpha = (1f - pulse) * 0.35f
            }
            .clip(CircleShape)
            .background(color.copy(alpha = 0.4f))
    )
}

/** A tiny inline pseudo-icon used during shuffle — three rotating dots. */
@Composable
private fun ShuffleGlyph(tint: Color, modifier: Modifier = Modifier) {
    val infinite = androidx.compose.animation.core.rememberInfiniteTransition(label = "shuffleGlyph")
    val angle by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(700, easing = androidx.compose.animation.core.LinearEasing)
        ),
        label = "shuffleAngle"
    )
    Canvas(modifier = modifier) {
        val radius = (size.minDimension / 2f) * 0.55f
        val cx = size.width / 2f
        val cy = size.height / 2f
        rotate(rotation = angle, pivot = Offset(cx, cy)) {
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

// ═══════════════════════════════════════════════════════════════════════════
// Bottom CTA
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun BottomCta(
    cat: CurioCategory,
    landedTopic: CurioTopic?,
    shuffling: Boolean,
    canSpin: Boolean,
    onSpin: () -> Unit,
    onExplore: () -> Unit
) {
    val showExplore = landedTopic != null
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically { it } + fadeIn(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        if (showExplore) {
            Button(
                onClick = onExplore,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = cat.accent,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CurioIcon(CurioIcons.AutoAwesome, null, tint = Color.White, size = 20.dp)
                    Text(
                        text = "Explore this topic",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                    )
                }
            }
        } else {
            Button(
                onClick = onSpin,
                enabled = canSpin,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = cat.accent,
                    contentColor = Color.White,
                    disabledContainerColor = cat.tint,
                    disabledContentColor = Color.White.copy(alpha = 0.6f)
                ),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CurioIcon(
                        CurioIcons.Casino, null,
                        tint = Color.White,
                        size = 20.dp
                    )
                    Text(
                        text = if (shuffling) "Spinning…" else "Shuffle",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
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
            -1 -> pool[idxOf(pool.size - 1)]
            0 -> pool[0]
            else -> pool[idxOf(1)]
        }
        else -> when (slot) {
            -1 -> pool[idxOf(cycleIndex - 1)]
            0 -> pool[idxOf(cycleIndex)]
            else -> pool[idxOf(cycleIndex + 1)]
        }
    }
}

/**
 * Weighted picker — favours tier 1 (human-curated marquee), then tier 2,
 * then tier 3, while excluding any topics in [recentIds] so the user
 * doesn't see the same pick twice in a stretch.
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
