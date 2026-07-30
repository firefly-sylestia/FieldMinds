package com.curio.app.features.spin

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import kotlin.random.Random

/**
 * The Shuffle — clean single-card design.
 *
 * The user has already picked a category from Home. Here they see:
 *   1. Top bar: back + category name
 *   2. A single premium card that cycles through topics
 *   3. One elegant shuffle button
 *   4. Confetti celebration on landing
 */
@Composable
fun SpinScreen(categorySlug: String?, navController: NavController) {
    val context = LocalContext.current
    val cat = remember(categorySlug) {
        categorySlug?.let { CurioCategories.byRouteSlug(it) }
            ?: CurioCategories.byId(CategoryId.WILDCARD)
    }

    val pool by produceState<List<CurioTopic>>(initialValue = emptyList(), cat.id) {
        value = TopicJsonLoader.load(cat.id)
    }

    var shuffling by remember { mutableStateOf(false) }
    var shuffleCount by remember { mutableIntStateOf(0) }
    var confettiTrigger by remember { mutableIntStateOf(0) }
    var landedTopic by remember { mutableStateOf<CurioTopic?>(null) }
    var recentTopicIds by remember { mutableStateOf(setOf<String>()) }

    // Display pool for the cycling animation
    val displayPool = remember(pool) {
        if (pool.size <= 25) pool.toList()
        else pool.shuffled().take(25)
    }
    var visibleTopicIndex by remember(shuffleCount) { mutableStateOf(0) }
    val cardProgress = remember(shuffleCount) { Animatable(0f) }

    // ── Shuffle logic ────────────────────────────────────────────────────
    LaunchedEffect(shuffleCount) {
        if (shuffleCount == 0) return@LaunchedEffect
        if (pool.isEmpty()) return@LaunchedEffect
        shuffling = true
        landedTopic = null

        val cycles = Random.nextInt(
            CurioMotion.MinSpinTurns,
            CurioMotion.MaxSpinTurns + 1
        ).toFloat()
        val durationMs = Random.nextInt(
            CurioMotion.Durations.SpinMin,
            CurioMotion.Durations.SpinMax + 1
        )

        // Cycle topic names on the card
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
            animationSpec = tween(
                durationMillis = durationMs,
                easing = CubicBezierEasing(0.10f, 0.70f, 0.25f, 1f)
            )
        )

        shuffling = false
        cyclerJob.cancel()

        val finalPick = pickFrom(pool, recentTopicIds)
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
        val topic = landedTopic ?: pickFrom(pool, recentTopicIds) ?: return@LaunchedEffect
        navController.navigate(CurioRoutes.revealFor(cat.id.routeSlug, topic.name))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Top bar ──────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CurioBackButton(onClick = { navController.popBackStack() })
            Column {
                Text(
                    text = cat.displayName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Shuffle to discover",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ── Card area ────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            PremiumSpinCard(
                accent = cat.accent,
                tint = cat.tint,
                glyph = cat.iconGlyph,
                shuffling = shuffling,
                landedTopic = landedTopic,
                displayTopic = displayPool.getOrNull(visibleTopicIndex),
                poolEmpty = pool.isEmpty(),
                onClick = { if (!shuffling && pool.isNotEmpty()) shuffleCount++ }
            )
        }

        // ── Bottom CTA ───────────────────────────────────────────────────
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when {
                    pool.isEmpty() -> "No topics in this category yet"
                    shuffling -> "Shuffling…"
                    landedTopic != null -> "Here's your pick"
                    else -> "Tap to discover"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { if (!shuffling && pool.isNotEmpty()) shuffleCount++ },
                enabled = !shuffling && pool.isNotEmpty(),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = cat.accent,
                    contentColor = CurioColors.DeepPlum,
                    disabledContainerColor = cat.tint,
                    disabledContentColor = CurioColors.DeepPlum.copy(alpha = 0.4f)
                ),
                contentPadding = PaddingValues(horizontal = 56.dp, vertical = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (landedTopic != null) "Shuffle again" else "SHUFFLE",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                )
            }
            Spacer(Modifier.height(8.dp))
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
// Premium single spin card
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun PremiumSpinCard(
    accent: Color,
    tint: Color,
    glyph: String,
    shuffling: Boolean,
    landedTopic: CurioTopic?,
    displayTopic: CurioTopic?,
    poolEmpty: Boolean,
    onClick: () -> Unit
) {
    // ── Landing pop ──────────────────────────────────────────────────────
    var justLanded by remember { mutableStateOf(false) }
    LaunchedEffect(landedTopic) {
        if (landedTopic != null) {
            justLanded = true
            delay(100)
            justLanded = false
        }
    }
    val popScale by animateFloatAsState(
        targetValue = if (justLanded) 1.05f else 1f,
        animationSpec = CurioMotion.Springs.Elastic,
        label = "popScale"
    )

    // ── Glow pulse during shuffle ────────────────────────────────────────
    val glowAlpha by animateFloatAsState(
        targetValue = if (shuffling) 0.18f else 0f,
        animationSpec = CurioMotion.Springs.Snappy,
        label = "glowAlpha"
    )

    Box(
        modifier = Modifier.size(width = 280.dp, height = 360.dp),
        contentAlignment = Alignment.Center
    ) {
        // ── Radial glow behind card ──────────────────────────────────────
        if (glowAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                accent.copy(alpha = glowAlpha),
                                accent.copy(alpha = glowAlpha * 0.3f),
                                Color.Transparent
                            ),
                            radius = 280f
                        ),
                        shape = RoundedCornerShape(32.dp)
                    )
            )
        }

        // ── The card itself ──────────────────────────────────────────────
        Surface(
            onClick = onClick,
            enabled = !shuffling && !poolEmpty,
            shape = RoundedCornerShape(32.dp),
            color = CurioColors.CreamWhite,
            shadowElevation = if (landedTopic != null) 12.dp else 6.dp,
            tonalElevation = if (landedTopic != null) 4.dp else 2.dp,
            modifier = Modifier
                .fillMaxSize()
                .scale(popScale)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = if (landedTopic != null) {
                                listOf(accent.copy(alpha = 0.08f), Color.Transparent)
                            } else {
                                listOf(Color.White.copy(alpha = 0.20f), Color.Transparent, tint.copy(alpha = 0.05f))
                            }
                        ),
                        shape = RoundedCornerShape(32.dp)
                    )
                    .padding(24.dp)
            ) {
                when {
                    landedTopic != null -> LandedContent(accent, glyph, landedTopic)
                    shuffling && displayTopic != null -> CyclingContent(accent, glyph, displayTopic)
                    else -> IdleContent(accent, tint, glyph, poolEmpty)
                }
            }
        }
    }
}

// ── Idle: show category icon ─────────────────────────────────────────────

@Composable
private fun IdleContent(accent: Color, tint: Color, glyph: String, poolEmpty: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = accent.copy(alpha = 0.12f),
            modifier = Modifier.size(100.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                CurioIcon(
                    name = glyph,
                    contentDescription = null,
                    tint = accent,
                    size = 52.dp
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = if (poolEmpty) "Coming soon" else "Tap to shuffle",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = if (poolEmpty) MaterialTheme.colorScheme.onSurfaceVariant else accent
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (poolEmpty) "Topics are on the way"
                   else "Discover something new",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Cycling: animated topic names during shuffle ─────────────────────────

@Composable
private fun CyclingContent(accent: Color, glyph: String, topic: CurioTopic) {
    AnimatedContent(
        targetState = topic.name,
        transitionSpec = {
            (slideInVertically(initialOffsetY = { it / 3 }) { it } + fadeIn(tween(200))) togetherWith
            (slideOutVertically(targetOffsetY = { -it / 3 }) { it } + fadeOut(tween(150)))
        },
        label = "topicCycle"
    ) { topicName ->
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            // Small icon badge
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = accent.copy(alpha = 0.15f)
            ) {
                CurioIcon(glyph, null, tint = accent, size = 28.dp, modifier = Modifier.padding(10.dp))
            }
            Spacer(Modifier.height(20.dp))
            // Accent bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(color = accent, shape = RoundedCornerShape(2.dp))
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = topicName,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = CurioColors.DeepPlum,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = topic.subtype,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Landed: full topic card ──────────────────────────────────────────────

@Composable
private fun LandedContent(accent: Color, glyph: String, topic: CurioTopic) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top: sparkle badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = accent.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CurioIcon(CurioIcons.AutoAwesome, null, tint = accent, size = 14.dp)
                    Text("Match!", style = MaterialTheme.typography.labelSmall, color = accent)
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // Center: topic details
        Column {
            // Thick accent bar
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(6.dp)
                    .background(color = accent, shape = RoundedCornerShape(3.dp))
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = topic.name,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = CurioColors.DeepPlum,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = topic.subtype,
                style = MaterialTheme.typography.bodyLarge,
                color = accent
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = topic.teaser,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.weight(1f))

        // Bottom: large watermark icon
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            CurioIcon(
                name = glyph,
                contentDescription = null,
                tint = accent.copy(alpha = 0.12f),
                size = 80.dp
            )
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
