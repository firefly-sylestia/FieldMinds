package com.curio.app.features.spin

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
 * The Spin — see CURIO_SPEC.md §5 (v3 redesign, further polished v4).
 *
 * Improvements over v3:
 *  1. **Tighter top spacing** — top bar uses statusBarsPadding directly on
 *     the row with zero extra vertical padding so menu + profile sit higher.
 *  2. **Rounded corners on animated cards** — explicit .clip() before
 *     .graphicsLayer() so scaled/translated cards keep their corner radius.
 *  3. **Interactive shuffle cards** — tapping any card in the carousel
 *     triggers a spin (same as the button).
 *  4. **Beautiful category bottom sheet** — replaces the cramped DropdownMenu
 *     with a full ModalBottomSheet grouped by family, each with accent-color
 *     cards, checkmark for current selection, and a "Browse all" link.
 *  5. **Multi-select filters** — both genres/tags and subtypes now support
 *     selecting MULTIPLE chips. Filtering uses OR logic (match any selected).
 *  6. **Redesigned filter sheet** — multi-select chip grid, active count
 *     in header, clear-all button, better visual hierarchy.
 *  7. **Smoother spin animation** — improved deceleration curve with a
 *     sinusoidal ease-out for more natural settling.
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

    // ── Multi-select filter state ─────────────────────────────────────
    var activeFilters by remember(activeCategory.id, pool) { mutableStateOf(setOf<String>()) }
    var activeSubtypes by remember(activeCategory.id, pool) { mutableStateOf(setOf<String>()) }
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
    val allSubtypes = remember(pool) { pool.map { it.subtype }.distinct().sorted() }
    val allTags = remember(pool) { pool.flatMap { it.tags }.distinct().sorted() }

    // ── Spin state ────────────────────────────────────────────────────
    var shuffling by remember { mutableStateOf(false) }
    var shuffleCount by remember { mutableIntStateOf(0) }
    var confettiTrigger by remember { mutableIntStateOf(0) }
    var landedTopic by remember { mutableStateOf<CurioTopic?>(null) }
    var recentTopicIds by remember(activeCategory.id) { mutableStateOf(setOf<String>()) }

    val displayPool = remember(filteredPool) {
        if (filteredPool.isEmpty()) emptyList()
        else {
            val s = filteredPool.shuffled()
            if (s.size >= 6) s.take(6) else s
        }
    }
    var cycleIndex by remember(shuffleCount) { mutableIntStateOf(0) }
    val cat = activeCategory

    LaunchedEffect(activeCategory.id) {
        landedTopic = null
        shuffling = false
        activeFilters = emptySet()
        activeSubtypes = emptySet()
    }

    // ── Improved shuffle logic — sinusoidal ease-out deceleration ─────
    LaunchedEffect(shuffleCount) {
        if (shuffleCount == 0 || filteredPool.isEmpty()) return@LaunchedEffect
        shuffling = true
        landedTopic = null
        val durationMs = 2800L
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
            delay(interval)
            if (System.currentTimeMillis() - start >= durationMs) break
        }
        shuffling = false

        val pick = pickFrom(filteredPool, recentTopicIds)
        landedTopic = pick
        if (pick != null) {
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

    // ── Animations ────────────────────────────────────────────────────
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

    // ── Overall layout ─────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── 1. Top bar — back, category chip, topic count ───────────
        TopBar(
            cat = cat,
            poolCount = pool.size,
            filteredCount = filteredPool.size,
            modifier = Modifier.statusBarsPadding(),
            onBack = { navController.popBackStack() },
            onOpenCategoryPicker = { showCategoryPicker = true }
        )

        // ── 2. Filter row ──────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterTrigger(
                accent = cat.accent,
                activeCount = activeFilters.size + activeSubtypes.size,
                onClick = { showFilters = true }
            )
            if (activeFilters.isNotEmpty() || activeSubtypes.isNotEmpty()) {
                TextButton(
                    onClick = {
                        activeFilters = emptySet()
                        activeSubtypes = emptySet()
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "Clear",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            if (cat.id != CategoryId.WILDCARD && filteredPool.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = cat.accent.copy(alpha = 0.12f)
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

        // ── 3. Carousel (interactive cards) ─────────────────────────
        Carousel(
            cat = cat,
            displayPool = displayPool,
            cycleIndex = cycleIndex,
            shuffling = shuffling,
            landedTopic = landedTopic,
            landScale = landScale,
            enabled = filteredPool.isNotEmpty() && !shuffling,
            onCardTap = {
                if (!shuffling && filteredPool.isNotEmpty()) shuffleCount++
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp, bottom = 2.dp)
        )

        // ── 4. Center spin button ───────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
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

        // ── 5. Bottom CTA ───────────────────────────────────────────
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

    // ── Category picker bottom sheet ──────────────────────────────────
    if (showCategoryPicker) {
        CategoryPickerSheet(
            currentCat = cat,
            onDismiss = { showCategoryPicker = false },
            onCategoryChange = { c ->
                activeCategory = c
                showCategoryPicker = false
            },
            onBrowseAll = {
                showCategoryPicker = false
                navController.navigate(CurioRoutes.PICKER)
            }
        )
    }

    // ── ModalBottomSheet — multi-select filter dialog ─────────────────
    if (showFilters) {
        FilterSheet(
            cat = cat,
            subtypes = allSubtypes,
            tags = allTags,
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
    onBack: () -> Unit,
    onOpenCategoryPicker: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CurioBackButton(onClick = onBack)

        Spacer(Modifier.width(10.dp))

        // ── CategoryMenu trigger chip ─────────────────────────────────
        Surface(
            onClick = onOpenCategoryPicker,
            shape = RoundedCornerShape(50),
            color = cat.accent.copy(alpha = 0.15f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(cat.accent)
                )
                CurioIcon(
                    cat.iconGlyph, null,
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
                    CurioIcons.KeyboardArrowDown, null,
                    tint = cat.accent,
                    size = 18.dp
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // ── Right-side topic count pill ─────────────────────────────
        if (poolCount > 0) {
            Surface(
                shape = RoundedCornerShape(50),
                color = cat.accent.copy(alpha = 0.12f)
            ) {
                Text(
                    text = "$filteredCount / $poolCount",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = cat.accent,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Category picker — beautiful ModalBottomSheet grouped by family
// ═══════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryPickerSheet(
    currentCat: CurioCategory,
    onDismiss: () -> Unit,
    onCategoryChange: (CurioCategory) -> Unit,
    onBrowseAll: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val groups = remember {
        CategoryFamily.values().map { fam ->
            fam to CurioCategories.byFamily(fam)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp)
        ) {
            // ── Header ────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Choose a category",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "What are you curious about today?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Category cards by family ──────────────────────────────
            groups.forEach { (family, cats) ->
                // Family header
                Text(
                    text = familyDisplayName(family),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )

                // Family row of cards
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    cats.forEach { c ->
                        val isSelected = c.id == currentCat.id
                        val cardMod = if (cats.size == 1) Modifier.fillMaxWidth() else Modifier.weight(1f)
                        CategoryPickerCard(
                            cat = c,
                            isSelected = isSelected,
                            onClick = { onCategoryChange(c) },
                            modifier = cardMod
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
            }

            // ── Divider ────────────────────────────────────────────────
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // ── Browse all link ────────────────────────────────────────
            Surface(
                onClick = onBrowseAll,
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = CurioColors.CoralBlush.copy(alpha = 0.15f)
                        ) {
                            CurioIcon(
                                CurioIcons.Palette, null,
                                tint = CurioColors.CoralBlush,
                                size = 20.dp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Text(
                            "Browse all categories",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    CurioIcon(
                        CurioIcons.ArrowForward, null,
                        tint = MaterialTheme.colorScheme.primary,
                        size = 20.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryPickerCard(
    cat: CurioCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.03f else 1f,
        animationSpec = CurioMotion.Springs.Snappy,
        label = "catCardScale"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = if (isSelected) cat.accent else cat.accent.copy(alpha = 0.18f),
        shadowElevation = if (isSelected) 6.dp else 2.dp,
        tonalElevation = if (isSelected) 3.dp else 0.dp,
        border = if (isSelected) BorderStroke(2.dp, cat.accent)
        else BorderStroke(1.dp, cat.accent.copy(alpha = 0.15f)),
        modifier = modifier
            .height(100.dp)
            .scale(scale)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Watermark glyph
            CurioIcon(
                cat.iconGlyph, null,
                tint = if (isSelected) Color.White.copy(alpha = 0.2f)
                else cat.accent.copy(alpha = 0.2f),
                size = 72.dp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 8.dp, bottom = 4.dp)
            )
            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CurioIcon(
                        cat.iconGlyph, null,
                        tint = if (isSelected) Color.White else cat.accent,
                        size = 20.dp
                    )
                    if (isSelected) {
                        CurioIcon(
                            CurioIcons.Check, null,
                            tint = Color.White,
                            size = 16.dp
                        )
                    }
                }
                Text(
                    text = cat.displayName,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold
                    ),
                    color = if (isSelected) Color.White else cat.accent,
                    maxLines = 1
                )
            }
        }
    }
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
// Filter trigger chip + ModalBottomSheet filter dialog (multi-select)
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun FilterTrigger(
    accent: Color,
    activeCount: Int,
    onClick: () -> Unit
) {
    val label = if (activeCount == 0) "Filter" else "Filter ($activeCount)"
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (activeCount > 0) accent.copy(alpha = 0.18f)
        else MaterialTheme.colorScheme.surfaceVariant
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
                CurioIcons.KeyboardArrowDown, null,
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
    initialSubtypes: Set<String>,
    initialFilters: Set<String>,
    onDismiss: () -> Unit,
    onApply: (tags: Set<String>, subtypes: Set<String>) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var draftFilters by remember(initialFilters) { mutableStateOf(initialFilters) }
    var draftSubtypes by remember(initialSubtypes) { mutableStateOf(initialSubtypes) }

    val activeCount = draftFilters.size + draftSubtypes.size

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp)
        ) {
            // ── Header ────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Filter ${cat.displayName}",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (activeCount > 0) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = cat.accent
                            ) {
                                Text(
                                    text = "$activeCount",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "${tags.size} genres · ${subtypes.size} ${if (subtypes.size == 1) "type" else "types"} — select as many as you like",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (activeCount > 0) {
                    TextButton(onClick = {
                        draftFilters = emptySet()
                        draftSubtypes = emptySet()
                    }) {
                        Text(
                            "Clear all",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
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
                        MultiSelectChip(
                            label = st,
                            selected = st in draftSubtypes,
                            accent = cat.accent,
                            onClick = {
                                draftSubtypes = if (st in draftSubtypes)
                                    draftSubtypes - st
                                else
                                    draftSubtypes + st
                            }
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // ── Tags section ───────────────────────────────────────────
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
                        MultiSelectChip(
                            label = tag,
                            selected = tag in draftFilters,
                            accent = cat.accent,
                            onClick = {
                                draftFilters = if (tag in draftFilters)
                                    draftFilters - tag
                                else
                                    draftFilters + tag
                            }
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
                onClick = { onApply(draftFilters, draftSubtypes) },
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
                        CurioIcons.AutoAwesome, null,
                        tint = Color.White,
                        size = 18.dp
                    )
                    Text(
                        text = if (activeCount > 0)
                            "Apply $activeCount filter${if (activeCount > 1) "s" else ""}"
                        else
                            "Show all topics",
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
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.3.sp
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

@Composable
private fun MultiSelectChip(
    label: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.05f else 1f,
        animationSpec = CurioMotion.Springs.Snappy,
        label = "msChipScale"
    )
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) accent
        else MaterialTheme.colorScheme.surfaceContainerHigh,
        border = if (!selected)
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        else null,
        modifier = Modifier.scale(scale)
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
// 3-card vertical carousel with interactive cards + proper clipping
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun Carousel(
    cat: CurioCategory,
    displayPool: List<CurioTopic>,
    cycleIndex: Int,
    shuffling: Boolean,
    landedTopic: CurioTopic?,
    landScale: Float,
    enabled: Boolean,
    onCardTap: () -> Unit,
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
                    cat = cat,
                    enabled = enabled,
                    onTap = onCardTap
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
    cat: CurioCategory,
    enabled: Boolean,
    onTap: () -> Unit
) {
    val isCenter = slot == 0
    val w = if (isCenter) 250.dp else 210.dp
    val h = if (isCenter) 230.dp else 140.dp
    val yOff: Float = when (slot) {
        -1 -> -120f
        0 -> 0f
        else -> 120f
    }
    val s = if (isCenter) 1f else 0.78f
    val alpha = if (isCenter) 1f else 0.55f
    val corner = if (isCenter) 28.dp else 22.dp
    val isLanded = landedTopic != null && isCenter

    // Explicit clip before graphicsLayer so scaled cards keep rounded corners.
    Box(
        modifier = Modifier
            .size(w, h)
            .clip(RoundedCornerShape(corner))
            .graphicsLayer {
                scaleX = if (isLanded) landScale else s
                scaleY = if (isLanded) landScale else s
                this.alpha = alpha
                translationY = yOff.dp.toPx()
            }
            .zIndex(if (isCenter) 10f else 5f)
            .then(
                if (enabled || isCenter) Modifier.clickable(
                    indication = null,
                    interactionSource = null,
                    onClick = onTap
                ) else Modifier
            )
    ) {
        AnimatedContent(
            targetState = topic,
            transitionSpec = {
                slideInVertically { height -> height } +
                fadeIn(animationSpec = tween(160)) togetherWith
                slideOutVertically { height -> -height } +
                fadeOut(animationSpec = tween(120))
            },
            label = "carouselSlot_$slot"
        ) { currentTopic ->
            Surface(
                shape = RoundedCornerShape(corner),
                color = if (isCenter) {
                    MaterialTheme.colorScheme.surfaceContainerHighest
                } else {
                    MaterialTheme.colorScheme.surfaceContainer
                },
                border = if (isCenter) {
                    BorderStroke(1.dp, accent.copy(alpha = 0.25f))
                } else null,
                shadowElevation = if (isCenter) 8.dp else 2.dp,
                tonalElevation = if (isCenter) 2.dp else 0.dp,
                modifier = Modifier.fillMaxSize()
            ) {
                if (currentTopic != null) {
                    CarouselCardContent(
                        topic = currentTopic,
                        accent = accent,
                        isCenter = isCenter,
                        landed = isLanded
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
            // Header: type + accent stripe
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
    val size = if (landedTopic != null) 64.dp else 76.dp
    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        OrbitRing(active = isShuffling, color = tint, modifier = Modifier.fillMaxSize())
        IdleHalo(active = enabled && !isShuffling && landedTopic == null, color = tint)
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
private fun IdleHalo(active: Boolean, color: Color) {
    if (!active) return
    val infinite = rememberInfiniteTransition(label = "halo")
    val pulse by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
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

@Composable
private fun ShuffleGlyph(tint: Color, modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "shuffleGlyph")
    val angle by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing)
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
