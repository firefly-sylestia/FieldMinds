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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
 * The Shuffle — immersive full-screen spin experience.
 *
 * No cards, no clutter. Just:
 *   1. Compact top bar with category name
 *   2. Large category icon as the visual anchor
 *   3. Big cycling topic name in center
 *   4. One dramatic CTA button
 *   5. Full-screen confetti on landing
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

    // ── Landing pop animation ────────────────────────────────────────────
    val popScale by animateFloatAsState(
        targetValue = if (landedTopic != null) 1f else 0.96f,
        animationSpec = CurioMotion.Springs.Elastic,
        label = "landScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Compact top bar ──────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CurioBackButton(onClick = { navController.popBackStack() })
            Spacer(Modifier.width(8.dp))
            Text(
                text = cat.displayName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.weight(1f))
            // Pool count badge
            if (pool.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = cat.accent.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "${pool.size} topics",
                        style = MaterialTheme.typography.labelSmall,
                        color = cat.accent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // ═══════════════════════════════════════════════════════════════════
        // Main content — centered, immersive
        // ═══════════════════════════════════════════════════════════════════
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Large category icon as visual anchor
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = cat.accent.copy(alpha = 0.10f),
                modifier = Modifier.size(120.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    CurioIcon(
                        name = cat.iconGlyph,
                        contentDescription = null,
                        tint = cat.accent,
                        size = 64.dp
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── Cycling topic name or landed result ──────────────────────
            if (landedTopic != null) {
                // Landed state
                val topic = landedTopic!!
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.scale(popScale)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CurioIcon(CurioIcons.AutoAwesome, null, tint = cat.accent, size = 20.dp)
                        Text(
                            text = "Your pick",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = cat.accent
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = topic.name,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 40.sp
                        ),
                        color = CurioColors.DeepPlum,
                        textAlign = TextAlign.Center,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = topic.subtype,
                        style = MaterialTheme.typography.titleMedium,
                        color = cat.accent
                    )
                    Spacer(Modifier.height(12.dp))
                    // Accent bar
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(4.dp)
                            .background(cat.accent, RoundedCornerShape(2.dp))
                    )
                }
            } else if (shuffling && displayPool.isNotEmpty()) {
                // Cycling state — animated topic names
                AnimatedContent(
                    targetState = displayPool.getOrNull(visibleTopicIndex)?.name ?: "",
                    transitionSpec = {
                        (slideInVertically(initialOffsetY = { it / 3 }) + fadeIn(tween(200))) togetherWith
                        (slideOutVertically(targetOffsetY = { -it / 3 }) + fadeOut(tween(150)))
                    },
                    label = "topicCycle"
                ) { topicName ->
                    Text(
                        text = topicName,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 44.sp
                        ),
                        color = if (landedTopic != null) CurioColors.DeepPlum
                                else CurioColors.DeepPlum.copy(alpha = 0.35f),
                        textAlign = TextAlign.Center,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                // Idle state
                Text(
                    text = "Tap shuffle to discover",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = if (pool.isEmpty()) "Topics are on the way"
                           else "You'll get a random topic from this category",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // ═══════════════════════════════════════════════════════════════════
        // Bottom CTA
        // ═══════════════════════════════════════════════════════════════════
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = { if (!shuffling && pool.isNotEmpty()) shuffleCount++ },
                enabled = !shuffling && pool.isNotEmpty(),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = cat.accent,
                    contentColor = if (landedTopic != null) Color.White else CurioColors.DeepPlum,
                    disabledContainerColor = cat.tint,
                    disabledContentColor = CurioColors.DeepPlum.copy(alpha = 0.4f)
                ),
                contentPadding = PaddingValues(horizontal = 64.dp, vertical = 18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!shuffling) {
                        CurioIcon(
                            name = if (landedTopic != null) CurioIcons.Refresh else CurioIcons.Casino,
                            contentDescription = null,
                            tint = if (landedTopic != null) Color.White else CurioColors.DeepPlum,
                            size = 22.dp
                        )
                    }
                    Text(
                        text = when {
                            shuffling -> "Shuffling…"
                            landedTopic != null -> "Shuffle again"
                            else -> "SHUFFLE"
                        },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = if (landedTopic == null) 2.sp else 0.sp
                        )
                    )
                }
            }
        }
    }

    // ── Full-screen confetti ─────────────────────────────────────────────
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
