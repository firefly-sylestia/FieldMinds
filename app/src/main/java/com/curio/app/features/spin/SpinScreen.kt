package com.curio.app.features.spin

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * The Shuffle — premium card-stack spin experience.
 *
 * Layout:
 *   1. Compact top bar: back + current category name
 *   2. Category rail: horizontal chips to switch categories in-place
 *   3. Tag/genre filter chips: dynamic from topic tags (e.g. "Rock", "1970s")
 *   4. Card stack: 3 layered cards, top cycles during shuffle
 *   5. Big shuffle button + confetti on landing
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

    // Tag filter state
    val allTags = remember(pool) {
        pool.flatMap { it.tags }.distinct().sorted()
    }
    var activeTag by remember(activeCategory.id, pool) { mutableStateOf<String?>(null) }
    val filteredPool = remember(pool, activeTag) {
        if (activeTag == null) pool
        else pool.filter { activeTag in it.tags }
    }

    var shuffling by remember { mutableStateOf(false) }
    var shuffleCount by remember { mutableIntStateOf(0) }
    var confettiTrigger by remember { mutableIntStateOf(0) }
    var landedTopic by remember { mutableStateOf<CurioTopic?>(null) }
    var recentTopicIds by remember(activeCategory.id) { mutableStateOf(setOf<String>()) }

    val displayPool = remember(filteredPool) {
        if (filteredPool.size <= 25) filteredPool.toList()
        else filteredPool.shuffled().take(25)
    }
    var visibleTopicIndex by remember(shuffleCount) { mutableStateOf(0) }
    val cardProgress = remember(shuffleCount) { Animatable(0f) }
    val cat = activeCategory

    // ── Shuffle logic ────────────────────────────────────────────────────
    LaunchedEffect(shuffleCount) {
        if (shuffleCount == 0) return@LaunchedEffect
        if (filteredPool.isEmpty()) return@LaunchedEffect
        shuffling = true
        landedTopic = null

        val cycles = Random.nextInt(CurioMotion.MinSpinTurns, CurioMotion.MaxSpinTurns + 1).toFloat()
        val durationMs = Random.nextInt(CurioMotion.Durations.SpinMin, CurioMotion.Durations.SpinMax + 1)

        val cyclerJob = launch {
            val totalTicks = (cycles * 7f).toInt()
            repeat(totalTicks) { tick ->
                if (displayPool.isNotEmpty()) {
                    visibleTopicIndex = tick % displayPool.size
                }
                val progress = tick.toFloat() / totalTicks
                val tickDuration = ((durationMs.toFloat() / totalTicks) * (1f + progress * 0.4f)).toLong()
                delay(tickDuration.coerceAtLeast(180))
            }
        }

        cardProgress.snapTo(0f)
        cardProgress.animateTo(
            targetValue = cycles,
            animationSpec = tween(durationMillis = durationMs, easing = CubicBezierEasing(0.10f, 0.70f, 0.25f, 1f))
        )

        shuffling = false
        cyclerJob.cancel()

        val finalPick = pickFrom(filteredPool, recentTopicIds)
        landedTopic = finalPick
        if (finalPick != null) {
            recentTopicIds = (recentTopicIds + finalPick.id).toList().takeLast(20).toSet()
            StreakTracker.recordActivity(context)
        }
        confettiTrigger++
    }

    // ── Navigate after celebration ───────────────────────────────────────
    LaunchedEffect(confettiTrigger) {
        if (confettiTrigger == 0) return@LaunchedEffect
        delay(CurioMotion.Durations.RevealHold.toLong())
        val topic = landedTopic ?: pickFrom(filteredPool, recentTopicIds) ?: return@LaunchedEffect
        navController.navigate(CurioRoutes.revealFor(cat.id.routeSlug, topic.name))
    }

    // ── Landing pop ──────────────────────────────────────────────────────
    val popScale by animateFloatAsState(
        if (landedTopic != null) 1f else 0.96f,
        CurioMotion.Springs.Elastic, label = "landScale"
    )

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        // ── 1. Compact top bar ──────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CurioBackButton(onClick = { navController.popBackStack() })
            Spacer(Modifier.width(8.dp))
            Text(
                cat.displayName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.weight(1f))
            if (filteredPool.isNotEmpty()) {
                Surface(shape = RoundedCornerShape(8.dp), color = cat.accent.copy(alpha = 0.12f)) {
                    Text(
                        "${filteredPool.size} topics",
                        style = MaterialTheme.typography.labelSmall,
                        color = cat.accent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // ── 2. Category rail ────────────────────────────────────────────
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            items(CurioCategories.visible) { c ->
                val sel = c.id == cat.id
                Surface(
                    onClick = { activeCategory = c },
                    shape = RoundedCornerShape(50),
                    color = if (sel) c.accent.copy(alpha = 0.18f) else Color.Transparent
                ) {
                    Row(
                        Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CurioIcon(c.iconGlyph, null, tint = if (sel) c.accent else MaterialTheme.colorScheme.onSurfaceVariant, size = 16.dp)
                        Text(
                            c.displayName,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal),
                            color = if (sel) c.accent else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // ── 3. Tag/genre filter chips ───────────────────────────────────
        if (allTags.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                item {
                    Surface(
                        onClick = { activeTag = null },
                        shape = RoundedCornerShape(50),
                        color = if (activeTag == null) cat.accent.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            "All",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (activeTag == null) FontWeight.Bold else FontWeight.Normal),
                            color = if (activeTag == null) cat.accent else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
                items(allTags) { tag ->
                    val sel = tag == activeTag
                    Surface(
                        onClick = { activeTag = if (sel) null else tag },
                        shape = RoundedCornerShape(50),
                        color = if (sel) cat.accent.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            tag,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal),
                            color = if (sel) cat.accent else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // ── 4. Card stack area ──────────────────────────────────────────
        Box(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentAlignment = Alignment.Center
        ) {
            CardStack(
                accent = cat.accent,
                glyph = cat.iconGlyph,
                shuffling = shuffling,
                landedTopic = landedTopic,
                displayTopic = displayPool.getOrNull(visibleTopicIndex),
                poolEmpty = filteredPool.isEmpty(),
                popScale = popScale,
                activeTag = activeTag,
                onClick = { if (!shuffling && filteredPool.isNotEmpty()) shuffleCount++ }
            )
        }

        // ── 5. Bottom CTA ───────────────────────────────────────────────
        Box(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = { if (!shuffling && filteredPool.isNotEmpty()) shuffleCount++ },
                enabled = !shuffling && filteredPool.isNotEmpty(),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = cat.accent,
                    contentColor = if (landedTopic != null) Color.White else CurioColors.DeepPlum,
                    disabledContainerColor = cat.tint,
                    disabledContentColor = CurioColors.DeepPlum.copy(alpha = 0.4f)
                ),
                contentPadding = PaddingValues(horizontal = 64.dp, vertical = 18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!shuffling) {
                        CurioIcon(
                            if (landedTopic != null) CurioIcons.Refresh else CurioIcons.Casino,
                            null,
                            tint = if (landedTopic != null) Color.White else CurioColors.DeepPlum,
                            size = 22.dp
                        )
                    }
                    Text(
                        when { shuffling -> "Shuffling…"; landedTopic != null -> "Shuffle again"; else -> "SHUFFLE" },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = if (landedTopic == null) 2.sp else 0.sp
                        )
                    )
                }
            }
        }
    }

    // ── Confetti ─────────────────────────────────────────────────────────
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
// 3-card stack with shuffle animation
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun CardStack(
    accent: Color,
    glyph: String,
    shuffling: Boolean,
    landedTopic: CurioTopic?,
    displayTopic: CurioTopic?,
    poolEmpty: Boolean,
    popScale: Float,
    activeTag: String?,
    onClick: () -> Unit
) {
    val deckSize = 3

    Box(modifier = Modifier.size(width = 260.dp, height = 340.dp), contentAlignment = Alignment.Center) {
        // Back cards (static, provide depth illusion)
        if (!poolEmpty) {
            for (i in (deckSize - 1) downTo 1) {
                val offsetPx = (deckSize - i) * 8.dp
                val alpha = 1f - i * 0.25f
                val rot = (i - 2) * 3f
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = offsetPx, start = offsetPx)
                        .zIndex(i.toFloat())
                        .graphicsLayer { alpha = alpha.coerceAtLeast(0.3f); rotationZ = rot }
                ) {
                    CardFace(accent = accent, glyph = glyph, isTop = false, topic = null)
                }
            }
        }

        // Top card — the animated one
        val isTop = shuffling || landedTopic != null
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(deckSize.toFloat())
                .scale(if (landedTopic != null) popScale else 1f)
        ) {
            CardFace(
                accent = accent,
                glyph = glyph,
                isTop = true,
                topic = when {
                    landedTopic != null -> landedTopic
                    shuffling -> displayTopic
                    else -> null
                },
                shuffling = shuffling,
                poolEmpty = poolEmpty
            )
        }

        // Invisible tap target covering the whole stack
        Surface(
            onClick = onClick,
            enabled = !shuffling && !poolEmpty,
            color = Color.Transparent,
            modifier = Modifier.fillMaxSize().zIndex((deckSize + 1).toFloat())
        ) {}
    }
}

@Composable
private fun CardFace(
    accent: Color,
    glyph: String,
    isTop: Boolean,
    topic: CurioTopic?,
    shuffling: Boolean = false,
    poolEmpty: Boolean = false
) {
    val cardColor = if (isTop) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = cardColor,
        shadowElevation = if (isTop) 8.dp else 4.dp,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (topic != null && isTop) {
                // Landed or cycling topic
                if (!shuffling) {
                    // Landed state
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        CurioIcon(CurioIcons.AutoAwesome, null, tint = accent, size = 16.dp)
                        Text("Your pick", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = accent)
                    }
                }

                // Topic name (cycling or landed)
                if (shuffling) {
                    AnimatedContent(
                        targetState = topic.name,
                        transitionSpec = {
                            (slideInVertically { it / 3 } + fadeIn(tween(200))) togetherWith
                            (slideOutVertically { -it / 3 } + fadeOut(tween(150)))
                        },
                        label = "cycle"
                    ) { name ->
                        Text(
                            name,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold, lineHeight = 32.sp),
                            color = CurioColors.DeepPlum,
                            textAlign = TextAlign.Center,
                            maxLines = 3, overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    Text(
                        topic.name,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold, lineHeight = 32.sp),
                        color = CurioColors.DeepPlum,
                        textAlign = TextAlign.Center,
                        maxLines = 3, overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    topic.subtype,
                    style = MaterialTheme.typography.bodyMedium,
                    color = accent
                )

                if (!shuffling) {
                    Spacer(Modifier.height(12.dp))
                    // Accent divider
                    Box(Modifier.width(48.dp).height(3.dp).background(accent, RoundedCornerShape(2.dp)))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        topic.teaser,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 2, overflow = TextOverflow.Ellipsis
                    )
                }
            } else if (isTop) {
                // Idle state
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = accent.copy(alpha = 0.10f),
                    modifier = Modifier.size(88.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        CurioIcon(glyph, null, tint = accent, size = 44.dp)
                    }
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    if (poolEmpty) "Coming soon" else "Tap to shuffle",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (poolEmpty) MaterialTheme.colorScheme.onSurfaceVariant else accent,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    if (poolEmpty) "Topics are on the way" else "Discover something new",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            } else {
                // Back card — subtle watermark
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    CurioIcon(glyph, null, tint = accent.copy(alpha = 0.06f), size = 100.dp)
                }
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
