package com.curio.app.features.spin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import com.curio.app.ui.components.MorphEntrance
import com.curio.app.ui.components.StaggeredItem
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
 *  5. **Unified bottom bar** — Categories · Filter · Shuffle/Explore all in
 *     one row for quick one-thumb access.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SpinScreen(categorySlug: String?, navController: NavController) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
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
        landedTopic = primary
        if (primary != null) {
            val idx = displayPool.indexOfFirst { it.id == primary.id }
            if (idx >= 0) cycleIndex = idx
            recentTopicIds = (recentTopicIds + primary.id).toList().takeLast(20).toSet()
            StreakTracker.recordActivity(context)
        }
        confettiTrigger++
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
        // ── 1. Top bar — back, category name, topic count ───────────
        TopBar(
            cat = cat,
            poolCount = pool.size,
            filteredCount = filteredPool.size,
            modifier = Modifier.statusBarsPadding().offset(y = (-6).dp),
            onBack = { navController.popBackStack() }
        )

        // ── 2. Carousel (interactive cards) ─────────────────────────
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
                .padding(vertical = 0.dp)
        )

        // ── 4. Center spin button ───────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 0.dp),
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

        // ── 4. Bottom bar — Categories · Filter · CTA ──────────────
        BottomCta(
            cat = cat,
            landedTopic = landedTopic,
            shuffling = shuffling,
            canSpin = filteredPool.isNotEmpty(),
            filterActiveCount = activeFilters.size + activeSubtypes.size,
            onSpin = { if (!shuffling && filteredPool.isNotEmpty()) shuffleCount++ },
            onExplore = {
                val name = landedTopic?.name ?: return@BottomCta
                navController.navigate(CurioRoutes.revealFor(cat.id.routeSlug, name))
            },
            onCategories = { showCategoryPicker = true },
            onFilter = { showFilters = true }
        )
    }



    // ── CategoryPickerSheet ───────────────────────────────────────────
    if (showCategoryPicker) {
        CategoryPickerSheet(
            currentCat = cat,
            onDismiss = { showCategoryPicker = false },
            onCategorySelected = { c ->
                activeCategory = c
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
    onBack: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CurioBackButton(onClick = onBack)

        Spacer(Modifier.width(10.dp))

        // ── Category label (read-only — switching moves to bottom bar) ──
        Row(
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
                size = 18.dp
            )
            Text(
                text = cat.displayName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = cat.accent,
                maxLines = 1
            )
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
// ═══════════════════════════════════════════════════════════════════════════
// Compact filter bottom sheet with visible selected-filter chips
// ═══════════════════════════════════════════════════════════════════════════

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
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
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
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
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
                        style = MaterialTheme.typography.labelSmall.copy(
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
            } else {
                // ── Divider line ──────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                )
                Spacer(Modifier.height(10.dp))

                // ── Subtype chips ────────────────────────────────────
                if (subtypes.size > 1) {
                    SectionLabel("Type", Modifier.padding(horizontal = 20.dp, vertical = 2.dp))
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        subtypes.forEach { st ->
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
                }

                // ── Tag chips ────────────────────────────────────────
                if (tags.isNotEmpty()) {
                    if (subtypes.size > 1) Spacer(Modifier.height(10.dp))
                    SectionLabel("Genres", Modifier.padding(horizontal = 20.dp, vertical = 2.dp))
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        tags.forEach { tag ->
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
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Apply button ──────────────────────────────────────────
            Button(
                onClick = { onApply(draftFilters, draftSubtypes) },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = cat.accent,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                CurioIcon(CurioIcons.Check, null, tint = Color.White, size = 18.dp)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (activeCount > 0) "Apply filters ($activeCount)" else "Show all topics",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
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
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = Color.White
            )
            Surface(
                onClick = onRemove,
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.25f)
            ) {
                CurioIcon(
                    CurioIcons.Close, null,
                    tint = Color.White,
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
        style = MaterialTheme.typography.labelMedium.copy(
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
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) accent else MaterialTheme.colorScheme.surfaceContainerHigh,
        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium
            ),
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
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
        modifier = modifier.height(360.dp),
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
    val isDark = isSystemInDarkTheme()
    val isCenter = slot == 0
    val w = if (isCenter) 250.dp else 210.dp
    val h = if (isCenter) 230.dp else 140.dp
    val yOff: Float = when (slot) {
        -1 -> -108f
        0 -> 0f
        else -> 108f
    }
    val s = if (isCenter) 1f else 0.78f
    // Side cards: 0.55 in light, 0.68 in dark — keeps them visible against dark backgrounds.
    val alpha = if (isCenter) 1f else if (isDark) 0.68f else 0.55f
    val corner = if (isCenter) 28.dp else 22.dp
    val isLanded = landedTopic != null && isCenter

    // Outer Box padded 12dp beyond card for shadow breathing room.
    // Inner Box clipped to rounded corners preserves edges during animation.
    val boxW = w + if (isCenter) 24.dp else 0.dp
    val boxH = h + if (isCenter) 24.dp else 0.dp
    Box(
        modifier = Modifier
            .size(boxW, boxH)
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
        // Inner clip layer centered in outer box — prevents sharp edges during scale
        Box(
            modifier = Modifier
                .size(w, h)
                .align(Alignment.Center)
                .clip(RoundedCornerShape(corner))
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
                        BorderStroke(1.5.dp, accent.copy(alpha = 0.45f))
                    } else {
                        // Subtle border on side cards for definition in both modes.
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    },
                    shadowElevation = if (isCenter) 12.dp else 2.dp,
                    tonalElevation = if (isCenter) 4.dp else 0.dp,
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
}

@Composable
private fun CarouselCardContent(
    topic: CurioTopic,
    accent: Color,
    isCenter: Boolean,
    landed: Boolean
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(if (isCenter) 22.dp else 14.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // Small accent dot + subtype badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
            Text(
                text = topic.subtype,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.4.sp
                ),
                color = accent
            )
        }
        Spacer(Modifier.height(if (isCenter) 12.dp else 4.dp))
        // Topic name — large and bold
        Text(
            text = topic.name,
            style = if (isCenter)
                MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 28.sp
                )
            else
                MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = if (isCenter) 3 else 1,
            overflow = TextOverflow.Ellipsis
        )
        // Tags row below name
        if (isCenter && topic.tags.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                topic.tags.take(2).forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = accent.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.labelSmall,
                            color = accent,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
        if (landed && isCenter) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = topic.teaser,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
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
// Bottom bar — Categories · Filter · Shuffle/Explore
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun BottomCta(
    cat: CurioCategory,
    landedTopic: CurioTopic?,
    shuffling: Boolean,
    canSpin: Boolean,
    filterActiveCount: Int,
    onSpin: () -> Unit,
    onExplore: () -> Unit,
    onCategories: () -> Unit,
    onFilter: () -> Unit
) {
    val showExplore = landedTopic != null
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ── Categories button — same level as Filter ────────────────
        Surface(
            onClick = onCategories,
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CurioIcon(
                    cat.iconGlyph, null,
                    tint = cat.accent,
                    size = 18.dp
                )
                Text(
                    text = cat.displayName,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                CurioIcon(
                    CurioIcons.KeyboardArrowDown, null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    size = 16.dp
                )
            }
        }

        // ── Filter button — same visual level as Categories ─────────
        val hasFilters = filterActiveCount > 0
        Surface(
            onClick = onFilter,
            shape = RoundedCornerShape(50),
            color = if (hasFilters) cat.accent.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.surfaceContainerHigh,
            border = if (hasFilters) BorderStroke(1.5.dp, cat.accent.copy(alpha = 0.6f))
                else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shadowElevation = if (hasFilters) 2.dp else 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CurioIcon(
                    CurioIcons.Search, null,
                    tint = if (hasFilters) cat.accent else MaterialTheme.colorScheme.onSurfaceVariant,
                    size = 18.dp
                )
                Text(
                    text = "Filter",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = if (hasFilters) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (hasFilters) cat.accent else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                if (hasFilters) {
                    Spacer(Modifier.width(2.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = cat.accent
                    ) {
                        Text(
                            text = "$filterActiveCount",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // ── Main CTA — Shuffle or Explore ──────────────────────────
        if (showExplore) {
            Button(
                onClick = onExplore,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = cat.accent,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
            ) {
                CurioIcon(CurioIcons.AutoAwesome, null, tint = Color.White, size = 18.dp)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Explore",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                )
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
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
            ) {
                CurioIcon(
                    CurioIcons.Casino, null,
                    tint = Color.White,
                    size = 18.dp
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (shuffling) "Spinning…" else "Shuffle",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                )
            }
        }
    }
}

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
                animationSpec = CurioMotion.Springs.Snappy
            ) + fadeIn(animationSpec = tween(280)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(240)
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
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
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
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = currentCat.accent,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
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
                            itemsIndexed(categories) { index, cat ->
                                StaggeredItem(index = index, staggerDelayMs = CurioMotion.Stagger.Fast) {
                                    CategoryPickerTile(
                                        category = cat,
                                        isSelected = cat.id == currentCat.id,
                                        onClick = { onCategorySelected(cat) }
                                    )
                                }
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
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
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
    val cardColor = if (isWildcard) CurioColors.CoralBlush.copy(alpha = 0.85f) else category.accent

    Surface(
        onClick = {
            pressed = true
            onClick()
        },
        shape = RoundedCornerShape(28.dp),
        color = cardColor,
        shadowElevation = if (isSelected) 10.dp else 6.dp,
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
                    color = Color.White.copy(alpha = 0.22f)
                ) {
                    CurioIcon(
                        name = category.iconGlyph,
                        contentDescription = null,
                        tint = Color.White,
                        size = 34.dp,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                // Name + selected check
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = category.displayName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
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

