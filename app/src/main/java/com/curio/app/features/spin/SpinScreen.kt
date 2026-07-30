package com.curio.app.features.spin

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
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * The Spin — redesigned v5 with:
 *  1. **Persistent landed topic** — saved to SharedPreferences, restored on return
 *  2. **Horizontal category chips** — scrollable row below spin button (like CategoryPicker)
 *  3. **Sticky filter sheet** — improved design, stays visible when scrolling
 *  4. **Beautiful fluid card animations** — enhanced carousel with swipe gestures
 *  5. **Auto-navigate to detail** — when topic lands, automatically show TopicRevealScreen
 *  6. **Reduced top padding** — tighter statusBarsPadding (0dp offset)
 *  7. **Interactive swipe cards** — swipe left/right to trigger spin
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

    // ── Spin state with persistence ───────────────────────────────────
    var shuffling by remember { mutableStateOf(false) }
    var shuffleCount by remember { mutableIntStateOf(0) }
    var confettiTrigger by remember { mutableIntStateOf(0) }
    var recentTopicIds by remember(activeCategory.id) { mutableStateOf(setOf<String>()) }
    
    // Load persisted landed topic
    var landedTopic by remember(activeCategory.id) {
        mutableStateOf(loadPersistedTopic(context, activeCategory.id, filteredPool))
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

    // Swipe state for interactive cards
    var swipeOffsetX by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(activeCategory.id) {
        landedTopic = loadPersistedTopic(context, activeCategory.id, filteredPool)
        shuffling = false
        activeFilters = emptySet()
        activeSubtypes = emptySet()
        swipeOffsetX = 0f
    }

    // ── Improved shuffle logic with auto-navigation ───────────────────
    LaunchedEffect(shuffleCount) {
        if (shuffleCount == 0 || filteredPool.isEmpty()) return@LaunchedEffect
        shuffling = true
        landedTopic = null
        swipeOffsetX = 0f
        val durationMs = 2800L
        val start = System.currentTimeMillis()
        var tick = 0
        while (true) {
            val elapsed = System.currentTimeMillis() - start
            if (elapsed >= durationMs) break
            val progress = (elapsed.toFloat() / durationMs).coerceIn(0f, 1f)
            val eased = sin((1f - progress) * Math.PI.toFloat() / 2f)
            val interval = (40L + (360L * eased).toLong()).coerceAtMost(400L)
            cycleIndex = ++tick
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            delay(interval)
            if (System.currentTimeMillis() - start >= durationMs) break
        }
        shuffling = false

        val primary = pickFrom(filteredPool, recentTopicIds)
        landedTopic = primary
        if (primary != null) {
            val idx = displayPool.indexOfFirst { it.id == primary.id }
            if (idx >= 0) cycleIndex = idx
            recentTopicIds = (recentTopicIds + primary.id).toList().takeLast(20).toSet()
            StreakTracker.recordActivity(context)
            
            // Persist the landed topic
            persistTopic(context, activeCategory.id, primary)
            
            confettiTrigger++
            
            // Auto-navigate to reveal screen after a short delay
            delay(1200)
            navController.navigate(CurioRoutes.revealFor(cat.id.routeSlug, primary.name))
        }
    }

    // ── Animations ────────────────────────────────────────────────────
    val landScale by animateFloatAsState(
        targetValue = if (landedTopic != null) 1.06f else 1f,
        animationSpec = CurioMotion.Springs.Elastic,
        label = "landScale"
    )
    val buttonPulse by animateFloatAsState(
        targetValue = if (shuffling) 1.08f else 1f,
        animationSpec = CurioMotion.Springs.Snappy,
        label = "buttonPulse"
    )

    // Detect swipe threshold to trigger spin
    val swipeThreshold = 120f
    LaunchedEffect(swipeOffsetX) {
        if (abs(swipeOffsetX) > swipeThreshold && !shuffling && filteredPool.isNotEmpty()) {
            shuffleCount++
            swipeOffsetX = 0f
        }
    }

    // ── Overall layout ─────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── 1. Top bar — back button only ──────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CurioBackButton(onClick = { navController.popBackStack() })
            Spacer(Modifier.weight(1f))
            if (pool.isNotEmpty()) {
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

        Spacer(Modifier.height(8.dp))

        // ── 2. Carousel (interactive swipeable cards) ──────────────────
        Carousel(
            cat = cat,
            displayPool = displayPool,
            cycleIndex = cycleIndex,
            shuffling = shuffling,
            landedTopic = landedTopic,
            landScale = landScale,
            swipeOffsetX = swipeOffsetX,
            enabled = filteredPool.isNotEmpty() && !shuffling,
            onCardTap = {
                if (!shuffling && filteredPool.isNotEmpty()) shuffleCount++
            },
            onSwipe = { delta ->
                if (!shuffling && filteredPool.isNotEmpty()) {
                    swipeOffsetX += delta
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        // ── 3. Center spin button ──────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
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

        // ── 4. Category chips row ──────────────────────────────────────
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            item(key = "wildcard") {
                CategoryChip(
                    name = "Surprise",
                    glyph = CurioIcons.Casino,
                    accent = CurioColors.CoralBlush,
                    selected = activeCategory.id == CategoryId.WILDCARD,
                    onClick = { activeCategory = CurioCategories.byId(CategoryId.WILDCARD) }
                )
            }
            items(items = CurioCategories.visible.filter { it.id != CategoryId.WILDCARD }, key = { it.id.name }) { category ->
                CategoryChip(
                    name = category.displayName,
                    glyph = category.iconGlyph,
                    accent = category.accent,
                    selected = activeCategory.id == category.id,
                    onClick = { activeCategory = category }
                )
            }
        }

        // ── 5. Filter button row ───────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
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
        }

        Spacer(Modifier.height(16.dp))
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
// Persistence helpers
// ═══════════════════════════════════════════════════════════════════════════

private fun persistTopic(context: android.content.Context, categoryId: CategoryId, topic: CurioTopic) {
    val prefs = context.getSharedPreferences("spin_state", android.content.Context.MODE_PRIVATE)
    prefs.edit()
        .putString("landed_topic_${categoryId.name}", topic.id)
        .apply()
}

private fun loadPersistedTopic(
    context: android.content.Context,
    categoryId: CategoryId,
    pool: List<CurioTopic>
): CurioTopic? {
    val prefs = context.getSharedPreferences("spin_state", android.content.Context.MODE_PRIVATE)
    val topicId = prefs.getString("landed_topic_${categoryId.name}", null) ?: return null
    return pool.firstOrNull { it.id == topicId }
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
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CurioIcon(
                if (activeCount > 0) CurioIcons.Check else CurioIcons.Search,
                null,
                tint = if (activeCount > 0) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                size = 16.dp
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (activeCount > 0) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (activeCount > 0) accent else MaterialTheme.colorScheme.onSurfaceVariant
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
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
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
                .padding(bottom = 32.dp)
        ) {
            // ── Header ────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${tags.size} genres · ${subtypes.size} ${if (subtypes.size == 1) "type" else "types"}",
                        style = MaterialTheme.typography.bodyMedium,
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

            Spacer(Modifier.height(12.dp))

            // ── Subtype section (only when more than one) ──────────────
            if (subtypes.size > 1) {
                SectionLabel("Kinds", Modifier.padding(horizontal = 24.dp))
                Spacer(Modifier.height(12.dp))
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                Spacer(Modifier.height(24.dp))
            }

            // ── Tags section ───────────────────────────────────────────
            if (tags.isNotEmpty()) {
                SectionLabel("Genres & tags", Modifier.padding(horizontal = 24.dp))
                Spacer(Modifier.height(12.dp))
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                Spacer(Modifier.height(24.dp))
            }

            if (subtypes.size <= 1 && tags.isEmpty()) {
                Text(
                    text = "No filters for this category yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp)
                )
            }

            // ── Apply button ──────────────────────────────────────────
            Button(
                onClick = { onApply(draftFilters, draftSubtypes) },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = cat.accent,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 28.dp, vertical = 18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CurioIcon(
                        CurioIcons.AutoAwesome, null,
                        tint = Color.White,
                        size = 20.dp
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
            letterSpacing = 0.5.sp
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
        border = if (selected) null
        else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.scale(scale)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium
            ),
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Category chip — pill with leading icon + label (like CategoryPickerScreen)
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun CategoryChip(
    name: String,
    glyph: String,
    accent: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.06f else 1f,
        animationSpec = CurioMotion.Springs.Snappy,
        label = "catChipScale"
    )
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = if (selected) accent.copy(alpha = 0.90f) else accent.copy(alpha = 0.15f),
        shadowElevation = if (selected) 6.dp else 0.dp,
        modifier = Modifier.scale(scale)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.20f),
                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.30f)),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    CurioIcon(
                        glyph, null,
                        tint = if (selected) Color.White else accent,
                        size = 20.dp
                    )
                }
            }
            Text(
                text = name,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold
                ),
                color = if (selected) Color.White else accent
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Enhanced carousel with swipe gestures
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun Carousel(
    cat: CurioCategory,
    displayPool: List<CurioTopic>,
    cycleIndex: Int,
    shuffling: Boolean,
    landedTopic: CurioTopic?,
    landScale: Float,
    swipeOffsetX: Float,
    enabled: Boolean,
    onCardTap: () -> Unit,
    onSwipe: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val poolSize = displayPool.size
    Box(
        modifier = modifier
            .pointerInput(enabled) {
                if (enabled) {
                    detectHorizontalDragGestures(
                        onDragEnd = { onSwipe(0f) },
                        onDragCancel = { onSwipe(0f) },
                        onHorizontalDrag = { _, dragAmount ->
                            onSwipe(dragAmount)
                        }
                    )
                }
            },
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
                    swipeOffsetX = swipeOffsetX,
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = cat.accent.copy(alpha = 0.10f),
            shadowElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CurioIcon(
                    cat.iconGlyph, null,
                    tint = cat.accent.copy(alpha = 0.5f),
                    size = 64.dp
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Coming soon",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(6.dp))
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
    swipeOffsetX: Float,
    cat: CurioCategory,
    enabled: Boolean,
    onTap: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val isCenter = slot == 0
    val w = if (isCenter) 280.dp else 230.dp
    val h = if (isCenter) 260.dp else 160.dp
    val yOff: Float = when (slot) {
        -1 -> -120f
        0 -> 0f
        else -> 120f
    }
    val s = if (isCenter) 1f else 0.80f
    val alpha = if (isCenter) 1f else if (isDark) 0.70f else 0.58f
    val corner = if (isCenter) 32.dp else 24.dp
    val isLanded = landedTopic != null && isCenter

    val boxW = w + if (isCenter) 28.dp else 0.dp
    val boxH = h + if (isCenter) 28.dp else 0.dp
    
    // Apply swipe offset only to center card
    val xOffset = if (isCenter) swipeOffsetX else 0f
    
    Box(
        modifier = Modifier
            .size(boxW, boxH)
            .graphicsLayer {
                scaleX = if (isLanded) landScale else s
                scaleY = if (isLanded) landScale else s
                this.alpha = alpha
                translationY = yOff.dp.toPx()
                translationX = xOffset
                rotationZ = if (isCenter) xOffset * 0.02f else 0f
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
                    fadeIn(animationSpec = tween(180)) togetherWith
                    slideOutVertically { height -> -height } +
                    fadeOut(animationSpec = tween(140))
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
                        BorderStroke(2.dp, accent.copy(alpha = 0.50f))
                    } else {
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    },
                    shadowElevation = if (isCenter) 16.dp else 3.dp,
                    tonalElevation = if (isCenter) 6.dp else 0.dp,
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
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CurioIcon(glyph, null, tint = accent.copy(alpha = 0.5f), size = 42.dp)
                                Text(
                                    text = "Tap spin to draw a topic",
                                    style = MaterialTheme.typography.labelLarge,
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
                            CurioIcon(glyph, null, tint = accent.copy(alpha = 0.25f), size = 26.dp)
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
        modifier = Modifier
            .fillMaxSize()
            .padding(if (isCenter) 24.dp else 16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
            Text(
                text = topic.subtype,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = accent
            )
        }
        Spacer(Modifier.height(if (isCenter) 14.dp else 6.dp))
        Text(
            text = topic.name,
            style = if (isCenter)
                MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 30.sp
                )
            else
                MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = if (isCenter) 3 else 2,
            overflow = TextOverflow.Ellipsis
        )
        if (isCenter && topic.tags.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                topic.tags.take(2).forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = accent.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.labelSmall,
                            color = accent,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }
        if (landed && isCenter) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = topic.teaser,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
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
    val size = if (landedTopic != null) 68.dp else 80.dp
    Box(
        modifier = Modifier.size(130.dp),
        contentAlignment = Alignment.Center
    ) {
        OrbitRing(active = isShuffling, color = tint, modifier = Modifier.fillMaxSize())
        IdleHalo(active = enabled && !isShuffling && landedTopic == null, color = tint)
        Surface(
            onClick = onClick,
            enabled = enabled,
            shape = CircleShape,
            color = if (landedTopic != null) tint.copy(alpha = 0.18f) else tint,
            shadowElevation = if (isShuffling) 3.dp else 10.dp,
            modifier = Modifier
                .size(size)
                .scale(pulseScale.coerceIn(0.9f, 1.12f))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (isShuffling) {
                    ShuffleGlyph(tint = Color.White, modifier = Modifier.size(50.dp))
                } else if (landedTopic != null) {
                    CurioIcon(
                        CurioIcons.Refresh, null,
                        tint = tint,
                        size = 32.dp
                    )
                } else {
                    CurioIcon(
                        CurioIcons.Casino, null,
                        tint = Color.White,
                        size = 36.dp
                    )
                }
            }
        }
    }
}

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
        val radius = (size.minDimension / 2f) - 10f.dp.toPx()
        val dotR = 4f.dp.toPx()
        val n = 8
        rotate(degrees = rot, pivot = Offset(cx, cy)) {
            for (i in 0 until n) {
                val a = (i.toFloat() / n) * (2f * Math.PI.toFloat())
                val dx = cos(a) * radius
                val dy = sin(a) * radius
                drawCircle(
                    color = color.copy(alpha = 0.90f),
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
            .size(104.dp)
            .graphicsLayer {
                scaleX = 1f + pulse * 0.40f
                scaleY = 1f + pulse * 0.40f
                this.alpha = (1f - pulse) * 0.40f
            }
            .clip(CircleShape)
            .background(color.copy(alpha = 0.45f))
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
        val radius = (size.minDimension / 2f) * 0.58f
        val cx = size.width / 2f
        val cy = size.height / 2f
        rotate(degrees = angle, pivot = Offset(cx, cy)) {
            for (i in 0 until 6) {
                val a = (i.toFloat() / 6) * (2f * Math.PI.toFloat())
                drawCircle(
                    color = tint,
                    radius = radius * 0.20f,
                    center = Offset(cx + cos(a) * radius, cy + sin(a) * radius)
                )
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
