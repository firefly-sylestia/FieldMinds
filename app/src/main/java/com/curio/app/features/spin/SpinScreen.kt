package com.curio.app.features.spin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.curio.app.data.CategoryId
import com.curio.app.data.CurioCategories
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
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

/**
 * The Spin — redesigned card-fan experience.
 *
 * Layout:
 *   1. Top bar: back + category name + topic count
 *   2. Compact filter bar: icon-only category dots + broad decade/era chips
 *   3. Card fan: 5 cards fanned in semi-circle around center spin button
 *   4. Shuffle button at bottom
 *   5. Landed overlay when topic lands
 */
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

    // Broad decade/era chips from tags (group "1970s", "1980s" etc.)
    val broadFilters = remember(pool) {
        pool.flatMap { it.tags }
            .filter { it.matches(Regex("\\d{4}s?")) || it.contains("Century") }
            .distinct().sorted()
    }
    var activeFilter by remember(activeCategory.id, pool) { mutableStateOf<String?>(null) }

    // Subtype filter (for categories with multiple subtypes like Painters, Scientists)
    val allSubtypes = remember(pool) {
        pool.map { it.subtype }.distinct().sorted()
    }
    var activeSubtype by remember(activeCategory.id, pool) { mutableStateOf<String?>(null) }

    val filteredPool = remember(pool, activeFilter, activeSubtype) {
        var result = pool
        if (activeFilter != null) result = result.filter { activeFilter in it.tags }
        if (activeSubtype != null) result = result.filter { it.subtype == activeSubtype }
        result
    }

    var shuffling by remember { mutableStateOf(false) }
    var shuffleCount by remember { mutableIntStateOf(0) }
    var confettiTrigger by remember { mutableIntStateOf(0) }
    var landedTopic by remember { mutableStateOf<CurioTopic?>(null) }
    var recentTopicIds by remember(activeCategory.id) { mutableStateOf(setOf<String>()) }

    val displayPool = remember(filteredPool) {
        if (filteredPool.size <= 25) filteredPool.shuffled().toList()
        else filteredPool.shuffled().take(25)
    }
    var cycleIndex by remember(shuffleCount) { mutableIntStateOf(0) }
    val cat = activeCategory

    // Reset on category switch
    LaunchedEffect(activeCategory.id) {
        landedTopic = null; shuffling = false
    }

    // Shuffle logic
    LaunchedEffect(shuffleCount) {
        if (shuffleCount == 0 || filteredPool.isEmpty()) return@LaunchedEffect
        shuffling = true; landedTopic = null
        val durationMs = 2400

        val cyclerJob = launch {
            val ticks = 40
            repeat(ticks) { tick ->
                if (displayPool.isNotEmpty()) cycleIndex = (tick * 3) % displayPool.size
                delay((durationMs / ticks).toLong())
            }
        }

        delay((durationMs * 0.7f).toLong())  // deceleration phase
        cyclerJob.cancel()
        shuffling = false

        val pick = pickFrom(filteredPool, recentTopicIds)
        landedTopic = pick
        if (pick != null) {
            recentTopicIds = (recentTopicIds + pick.id).toList().takeLast(20).toSet()
            StreakTracker.recordActivity(context)
        }
        confettiTrigger++
    }

    // Navigate
    LaunchedEffect(confettiTrigger) {
        if (confettiTrigger == 0) return@LaunchedEffect
        delay(CurioMotion.Durations.RevealHold.toLong())
        val topic = landedTopic ?: pickFrom(filteredPool, recentTopicIds) ?: return@LaunchedEffect
        navController.navigate(CurioRoutes.revealFor(cat.id.routeSlug, topic.name))
    }

    // Landing pop scale
    val landScale by animateFloatAsState(
        if (landedTopic != null) 1.05f else 1f,
        CurioMotion.Springs.Elastic, label = "land"
    )

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()
    ) {
        // ── 1. Top bar ─────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CurioBackButton(onClick = { navController.popBackStack() })
            Spacer(Modifier.width(4.dp))
            Text(cat.displayName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.weight(1f))
            if (filteredPool.isNotEmpty()) {
                Surface(shape = RoundedCornerShape(8.dp), color = cat.accent.copy(alpha = 0.12f)) {
                    Text("${filteredPool.size}", style = MaterialTheme.typography.labelSmall,
                        color = cat.accent, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }
        }

        // ── 2. Compact filter bar ──────────────────────────────────────
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            // Category dots (icon only)
            items(CurioCategories.visible) { c ->
                val sel = c.id == cat.id
                Surface(
                    onClick = { activeCategory = c },
                    shape = CircleShape,
                    color = if (sel) c.accent else Color.Transparent,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        CurioIcon(c.iconGlyph, null,
                            tint = if (sel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            size = 18.dp)
                    }
                }
            }

            // Divider dot
            item {
                Box(Modifier.width(1.dp).height(20.dp).background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(1.dp)))
            }

            // Broad filters: subtypes first, then decade/era chips
            if (allSubtypes.size > 1) {
                items(allSubtypes) { subtype ->
                    val sel = subtype == activeSubtype
                    FilterChip(subtype, sel, cat.accent) { activeSubtype = if (sel) null else subtype }
                }
            }
            if (broadFilters.isNotEmpty()) {
                items(broadFilters) { filter ->
                    val sel = filter == activeFilter
                    FilterChip(filter, sel, cat.accent) { activeFilter = if (sel) null else filter }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── 3. Card fan area ───────────────────────────────────────────
        Box(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentAlignment = Alignment.Center
        ) {
            val fanCount = 5
            val fanRadius = 200.dp
            val fanAngle = 80f  // total spread in degrees

            val density = LocalDensity.current
                val radiusPx = with(density) { fanRadius.toPx() }

    for (i in 0 until fanCount) {
                val angle = -fanAngle / 2 + (fanAngle / (fanCount - 1)) * i
                val topicForCard = when {
                    landedTopic != null && i == fanCount / 2 -> landedTopic
                    shuffling && displayPool.isNotEmpty() -> displayPool.getOrNull((cycleIndex + i) % displayPool.size)
                    else -> displayPool.getOrNull(i % maxOf(displayPool.size, 1))
                }
                val isCenter = i == fanCount / 2

                // Fan position
                val rad = Math.toRadians(angle.toDouble())
                val offsetX = (radiusPx * sin(rad)).toInt()
                val offsetY = (radiusPx * cos(rad) - radiusPx).toInt()

                Box(
                    modifier = Modifier
                        .offset { IntOffset(offsetX, offsetY) }
                        .rotate(angle * 0.3f)
                        .scale(if (isCenter && landedTopic != null) landScale else 1f)
                        .zIndex(if (isCenter) 10f else (5f - i.coerceIn(0, 4)).toFloat())
                ) {
                    FanCard(
                        accent = cat.accent,
                        glyph = cat.iconGlyph,
                        topic = topicForCard,
                        isCenter = isCenter,
                        landed = landedTopic != null && isCenter,
                        isEmpty = filteredPool.isEmpty()
                    )
                }
            }

            // Center spin button
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.zIndex(20f)) {
                Surface(
                    onClick = { if (!shuffling && filteredPool.isNotEmpty()) shuffleCount++ },
                    enabled = !shuffling && filteredPool.isNotEmpty(),
                    shape = CircleShape,
                    color = if (landedTopic != null) cat.accent.copy(alpha = 0.12f) else cat.accent,
                    shadowElevation = 8.dp,
                    modifier = Modifier.size(if (landedTopic != null) 64.dp else 72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        if (shuffling) {
                            Text("…", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White)
                        } else {
                            CurioIcon(
                                if (landedTopic != null) CurioIcons.Refresh else CurioIcons.Casino, null,
                                tint = if (landedTopic != null) cat.accent else Color.White,
                                size = 32.dp)
                        }
                    }
                }
                if (landedTopic == null && !shuffling && filteredPool.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text("Tap to spin", style = MaterialTheme.typography.labelSmall, color = cat.accent)
                }
            }
        }

        // ── 4. Bottom button ───────────────────────────────────────────
        if (landedTopic != null) {
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically { it } + fadeIn(),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Button(
                    onClick = {
                        val name = landedTopic?.name ?: return@Button
                        navController.navigate(CurioRoutes.revealFor(cat.id.routeSlug, name))
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = cat.accent, contentColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 48.dp, vertical = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Explore this topic", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold))
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp), contentAlignment = Alignment.Center) {
                Button(
                    onClick = { if (!shuffling && filteredPool.isNotEmpty()) shuffleCount++ },
                    enabled = !shuffling && filteredPool.isNotEmpty(),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = cat.accent,
                        contentColor = Color.White,
                        disabledContainerColor = cat.tint,
                        disabledContentColor = Color.White.copy(alpha = 0.5f)
                    ),
                    contentPadding = PaddingValues(horizontal = 64.dp, vertical = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        when { shuffling -> "Spinning…"; else -> "Shuffle" },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                    )
                }
            }
        }
    }

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
// Compact filter chip
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun FilterChip(label: String, selected: Boolean, accent: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) accent.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal),
            color = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Individual fan card
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun FanCard(
    accent: Color,
    glyph: String,
    topic: CurioTopic?,
    isCenter: Boolean,
    landed: Boolean,
    isEmpty: Boolean = false
) {
    val cardW = if (isCenter) 150.dp else 100.dp
    val cardH = if (isCenter) 210.dp else 140.dp
    val bg = if (isCenter) accent.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    Surface(
        shape = RoundedCornerShape(if (isCenter) 24.dp else 16.dp),
        color = bg,
        shadowElevation = if (isCenter) 8.dp else 3.dp,
        modifier = Modifier.size(cardW, cardH)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            if (topic != null) {
                Column(
                    modifier = Modifier.padding(if (isCenter) 14.dp else 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (isCenter) {
                        CurioIcon(CurioIcons.AutoAwesome, null, tint = accent, size = 14.dp)
                        Spacer(Modifier.height(6.dp))
                    }
                    Text(
                        topic.name,
                        style = if (isCenter) MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold)
                        else MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = if (isCenter) 3 else 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isCenter && landed) {
                        Spacer(Modifier.height(4.dp))
                        Text(topic.subtype, style = MaterialTheme.typography.labelSmall, color = accent)
                        Spacer(Modifier.height(4.dp))
                        Box(Modifier.width(32.dp).height(2.dp).background(accent, RoundedCornerShape(1.dp)))
                        Spacer(Modifier.height(4.dp))
                        Text(topic.teaser,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
            } else if (isCenter) {
                // Idle center card
                if (isEmpty) {
                    Text("Coming soon", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    CurioIcon(glyph, null, tint = accent.copy(alpha = 0.4f), size = 40.dp)
                }
            } else {
                // Side card placeholder
                CurioIcon(glyph, null, tint = accent.copy(alpha = 0.15f), size = 24.dp)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Weighted picker
// ═══════════════════════════════════════════════════════════════════════════

private fun pickFrom(pool: List<CurioTopic>, recentIds: Set<String>): CurioTopic? {
    if (pool.isEmpty()) return null
    val withoutRecents = pool.filterNot { it.id in recentIds }
    val candidates = if (withoutRecents.isNotEmpty()) withoutRecents else pool
    if (candidates.isEmpty()) return null
    if (candidates.size == 1) return candidates[0]

    val totalWeight = candidates.sumOf { t -> when (t.tier) { 1 -> 100; 2 -> 60; 3 -> 20; else -> 30 } }
    if (totalWeight <= 0) return candidates.random()
    var target = Random.nextInt(totalWeight)
    for (topic in candidates) {
        target -= when (topic.tier) { 1 -> 100; 2 -> 60; 3 -> 20; else -> 30 }
        if (target < 0) return topic
    }
    return candidates.random()
}
