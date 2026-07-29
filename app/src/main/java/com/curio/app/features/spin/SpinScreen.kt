package com.curio.app.features.spin

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.curio.app.data.CategoryId
import com.curio.app.data.CurioCategories
import com.curio.app.data.CurioTopic
import com.curio.app.data.TopicJsonLoader
import com.curio.app.navigation.CurioRoutes
import com.curio.app.ui.components.ConfettiBurst
import com.curio.app.ui.components.CurioBackButton
import com.curio.app.ui.components.CurioSparkle
import com.curio.app.ui.components.ScreenEntrance
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.CurioMotion
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * The Shuffle — see CURIO_SPEC.md §5.
 *
 * Visual metaphor: a deck of 3 cards being shuffled, matching the launcher
 * icon's stacked-card design. For each of the 11 categories, a horizontal
 * chip row above the deck lets the user pick a tag filter — the shuffle
 * then picks from that tag's pool.
 *
 * Enhanced shuffle animation:
 *  - Topic name cycler: the front card visibly flips through real topic
 *    names during the shuffle (slot-machine style), starting fast and
 *    decelerating before the final pick.
 *  - Flying topic particles: tiny topic-name chips spray outward from
 *    the stack during the shuffle, with intensity matching speed.
 *  - Shuffle intensity bars: horizontal bars beneath the stack that
 *    pulse with the shuffle rhythm.
 *  - Glorious landing: radial glow behind the landed card + border flash +
 *    scale pop + sparkle ring + confetti burst.
 *  - Ambient glow ring behind the cards that intensifies during shuffle.
 */
@Composable
fun SpinScreen(categorySlug: String?, navController: NavController) {
    val cat = remember(categorySlug) {
        val resolved = categorySlug?.let { CurioCategories.byRouteSlug(it) }
            ?: CurioCategories.byId(CategoryId.WILDCARD)
        resolved
    }

    val pool by produceState<List<CurioTopic>>(initialValue = emptyList(), cat.id) {
        value = TopicJsonLoader.load(cat.id)
    }
    val tags by produceState<List<String>>(initialValue = emptyList(), pool) {
        value = pool.flatMap { it.tags }.distinct().sorted()
    }

    var selectedTag by remember { mutableStateOf<String?>(null) }

    val filteredPool = remember(pool, selectedTag) {
        if (selectedTag == null) pool
        else pool.filter { it.tags.contains(selectedTag) }
    }

    var shuffling by remember { mutableStateOf(false) }
    var shuffleCount by remember { mutableIntStateOf(0) }
    var confettiTrigger by remember { mutableIntStateOf(0) }
    var sparkleTrigger by remember { mutableIntStateOf(0) }
    var landedTopic by remember { mutableStateOf<CurioTopic?>(null) }
    // Track recently-shown topic IDs to avoid repeats within a session
    var recentTopicIds by remember { mutableStateOf(setOf<String>()) }

    val shuffleProgress = remember(shuffleCount) { Animatable(0f) }

    // ── Display pool: topics shown cycling on the front card during shuffle ─
    val displayPool = remember(filteredPool) {
        if (filteredPool.size <= 25) filteredPool.toList()
        else filteredPool.shuffled().take(25)
    }
    // Track which topic is currently visible on the front card
    var visibleTopicIndex by remember(shuffleCount) { mutableStateOf(0) }
    // Track the cycler progress independently (runs faster than shuffleProgress)
    val cyclerProgress = remember(shuffleCount) { Animatable(0f) }

    LaunchedEffect(shuffleCount) {
        if (shuffleCount == 0) return@LaunchedEffect
        if (pool.isEmpty()) return@LaunchedEffect
        shuffling = true
        landedTopic = null

        val cycles = Random.nextInt(
            CurioMotion.MinSpinTurns,
            CurioMotion.MaxSpinTurns + 1
        ).toFloat()
        val durationMillis = Random.nextInt(
            CurioMotion.Durations.SpinMin,
            CurioMotion.Durations.SpinMax + 1
        )

        // Run the cycler faster — it cycles through ~10x more "ticks"
        val cyclerJob = launch {
            // Decelerating speed: start at 1.0 (fast), end at 0.08 (slow)
            cyclerProgress.snapTo(0f)
            val totalCyclerTicks = (cycles * 14f).toInt()
            var tick = 0
            val startMs = durationMillis.toFloat() / totalCyclerTicks
            while (tick < totalCyclerTicks && shuffling) {
                val progress = tick.toFloat() / totalCyclerTicks
                // Deceleration: fast early, slow late
                val speedMultiplier = 1.0f - progress * 0.88f
                val tickDuration = (startMs * (1.0f + progress * 0.8f)).toLong()
                cyclerProgress.snapTo(progress)
                tick++
                visibleTopicIndex = tick % displayPool.size
                delay(tickDuration.coerceAtLeast(8))
            }
            // Force final state
            visibleTopicIndex = (displayPool.size - 1).coerceAtLeast(0)
        }

        shuffleProgress.animateTo(
            targetValue = cycles,
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = CubicBezierEasing(0.15f, 0.85f, 0.2f, 1f)
            )
        )

        shuffling = false
        val finalPick = pickRandomFrom(filteredPool, cat.id, recentTopicIds)
        landedTopic = finalPick
        // Add to recent IDs (keep last 20)
        if (finalPick != null) {
            recentTopicIds = (recentTopicIds + finalPick.id).toList().takeLast(20).toSet()
        }
        // Set visibleTopicIndex to the landed topic's position in displayPool
        val finalIdx = displayPool.indexOfFirst { it.name == finalPick?.name }
        if (finalIdx >= 0) visibleTopicIndex = finalIdx

        confettiTrigger++
        sparkleTrigger++
        cyclerJob.cancel()
    }

    LaunchedEffect(confettiTrigger) {
        if (confettiTrigger == 0) return@LaunchedEffect
        delay(CurioMotion.Durations.RevealHold.toLong())
        val topic = landedTopic
            ?: pickRandomFrom(filteredPool, cat.id, recentTopicIds)
            ?: return@LaunchedEffect
        navController.navigate(
            CurioRoutes.revealFor(cat.id.routeSlug, topic.name)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CurioBackButton(onClick = { navController.popBackStack() })
            Text(
                text = "${cat.displayName} · Shuffle",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (tags.isNotEmpty()) {
            TagChipRow(
                tags = tags,
                selected = selectedTag,
                accent = cat.accent,
                onSelect = { newTag ->
                    if (!shuffling) {
                        selectedTag = newTag
                        landedTopic = null
                        shuffleCount++
                    }
                }
            )
            Spacer(Modifier.height(8.dp))
        }

        ScreenEntrance {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // ── Ambient glow halo behind cards ────────────────────────
                    ShuffleGlowHalo(
                        accent = cat.accent,
                        shuffling = shuffling,
                        modifier = Modifier
                            .size(260.dp)
                    )

                    ShuffleStack(
                        accent = cat.accent,
                        tint = cat.tint,
                        glyph = cat.iconGlyph,
                        progress = shuffleProgress.value,
                        shuffling = shuffling,
                        landedTopic = landedTopic,
                        sparkleTrigger = sparkleTrigger,
                        displayTopic = displayPool.getOrNull(visibleTopicIndex),
                        onTap = { if (!shuffling) shuffleCount++ }
                    )

                    // ── Flying topic particles during shuffle ────────────────
                    if (shuffling) {
                        ShuffleParticleSpray(
                            accent = cat.accent,
                            tint = cat.tint,
                            progress = cyclerProgress.value,
                            modifier = Modifier
                                .size(width = 300.dp, height = 380.dp)
                                .align(Alignment.Center)
                        )
                    }
                }

                // ── Shuffle intensity indicator ──────────────────────────────
                ShuffleIntensityBars(
                    accent = cat.accent,
                    shuffling = shuffling,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = when {
                        pool.isEmpty() && !shuffling -> "No topics yet — check back soon!"
                        shuffling -> "Shuffling…"
                        landedTopic != null -> "Here's your pick"
                        else -> "Tap to shuffle"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = { if (!shuffling) shuffleCount++ },
                    enabled = !shuffling && pool.isNotEmpty(),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = cat.accent,
                        contentColor = CurioColors.DeepPlum,
                        disabledContainerColor = cat.tint,
                        disabledContentColor =
                            CurioColors.DeepPlum.copy(alpha = 0.5f)
                    ),
                    contentPadding = PaddingValues(
                        horizontal = 48.dp,
                        vertical = 16.dp
                    )
                ) {
                    Text(
                        text = when {
                            shuffling -> "Shuffling…"
                            landedTopic != null -> "Shuffle again"
                            else -> "SHUFFLE"
                        },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold
                        )
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
// Shuffle stack — the core visual
// ═══════════════════════════════════════════════════════════════════════════

/**
 * The 3-card shuffle stack with topic name cycling on the front card.
 *
 * During shuffle:
 *  - Back + Middle cards oscillate with sine/cosine driven by progress
 *  - Front card shows [displayTopic], transitioning smoothly between topics
 *  - A glow border intensifies on the front card
 *
 * On landing:
 *  - Cards snap into elegant positions
 *  - Front card shows the LandedCard layout with full topic details
 *  - Sparkle ring fires
 */
@Composable
private fun ShuffleStack(
    accent: Color,
    tint: Color,
    glyph: String,
    progress: Float,
    shuffling: Boolean,
    landedTopic: CurioTopic?,
    sparkleTrigger: Int,
    displayTopic: CurioTopic?,
    onTap: () -> Unit
) {
    // ── Landing slam scale ────────────────────────────────────────────────
    var justLanded by remember { mutableStateOf(false) }
    LaunchedEffect(landedTopic) {
        if (landedTopic != null) {
            justLanded = true
            delay(120)
            justLanded = false
        }
    }
    val slamScale by animateFloatAsState(
        targetValue = if (justLanded) 1.06f else 1f,
        animationSpec = CurioMotion.Springs.Elastic,
        label = "slamScale"
    )

    // ── Glow alpha: intensifies during shuffle ────────────────────────────
    val glowAlpha by animateFloatAsState(
        targetValue = if (shuffling) 0.55f else if (landedTopic != null) 0.25f else 0f,
        animationSpec = CurioMotion.Springs.Snappy,
        label = "glowAlpha"
    )

    Box(
        modifier = Modifier
            .size(width = 260.dp, height = 340.dp)
            .clickable(enabled = !shuffling, onClick = onTap),
        contentAlignment = Alignment.Center
    ) {
        // ── Card 1: Back (deeper in the stack) ──────────────────────────────
        Box(
            modifier = Modifier
                .size(width = 240.dp, height = 300.dp)
                .graphicsLayer {
                    translationY = 60.dp.toPx() + sin(progress * 6f) * 8f
                    translationX = -16.dp.toPx() + cos(progress * 5f) * 4f
                    rotationZ = -6f + sin(progress * 4f) * 2f
                    scaleX = 0.84f
                    scaleY = 0.84f
                    alpha = 0.65f
                }
        ) {
            CardSurface(
                color = tint,
                borderColor = accent.copy(alpha = 0.35f),
                glyph = glyph,
                glyphTint = accent.copy(alpha = 0.6f)
            )
        }

        // ── Card 2: Middle (bridging back and front) ────────────────────────
        Box(
            modifier = Modifier
                .size(width = 240.dp, height = 300.dp)
                .graphicsLayer {
                    translationY = 28.dp.toPx() + sin(progress * 6f + 1.2f) * 8f
                    translationX = sin(progress * 5f + 0.8f) * 4f
                    rotationZ = 3f + cos(progress * 4f + 0.5f) * 2f
                    scaleX = 0.92f
                    scaleY = 0.92f
                    alpha = 0.80f
                }
        ) {
            CardSurface(
                color = accent.copy(alpha = 0.50f),
                borderColor = accent.copy(alpha = 0.6f),
                glyph = glyph,
                glyphTint = CurioColors.CreamWhite
            )
        }

        // ── Card 3: Front (the focal point) ─────────────────────────────────
        Box(
            modifier = Modifier
                .size(width = 240.dp, height = 300.dp)
                .graphicsLayer {
                    translationY = sin(progress * 6f + 2.4f) * 5f
                    rotationZ = sin(progress * 4f + 1.0f) * 1f
                    scaleX = slamScale
                    scaleY = slamScale
                    alpha = 1f
                }
        ) {
            // ── Glow border overlay ──────────────────────────────────────────
            if (glowAlpha > 0.01f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            // Draw a subtle larger background for the glow
                        }
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    accent.copy(alpha = glowAlpha),
                                    Color.Transparent
                                ),
                                radius = 300f
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                )
            }

            if (landedTopic != null) {
                // ── Landed state: full topic card ────────────────────────────
                Box {
                    LandedCard(accent = accent, glyph = glyph, topic = landedTopic)
                    if (sparkleTrigger > 0) {
                        CurioSparkle(
                            color = accent,
                            trigger = sparkleTrigger,
                            size = 240.dp,
                            ringCount = 3,
                            modifier = Modifier.size(240.dp, 300.dp)
                        )
                    }
                }
            } else if (shuffling && displayTopic != null) {
                // ── Shuffling: show cycling topic names ──────────────────────
                CyclingTopicCard(
                    accent = accent,
                    glyph = glyph,
                    topic = displayTopic
                )
            } else {
                // ── Idle: show the category glyph ────────────────────────────
                CardSurface(
                    color = CurioColors.CreamWhite,
                    borderColor = accent.copy(alpha = 0.5f),
                    glyph = glyph,
                    glyphTint = accent
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Cycling topic card — shown during shuffle
// ═══════════════════════════════════════════════════════════════════════════

/**
 * A front card that shows a topic name/subtype, smoothly cycling between
 * topics during the shuffle. Uses [AnimatedContent] with a quick vertical
 * slide transition for the slot-machine feel.
 */
@Composable
private fun CyclingTopicCard(
    accent: Color,
    glyph: String,
    topic: CurioTopic
) {
    AnimatedContent(
        targetState = topic.name,
        transitionSpec = {
            (slideInVertically(
                initialOffsetY = { h -> h / 3 },
                animationSpec = tween(120, easing = FastOutSlowInEasing)
            ) + fadeIn(tween(80))) togetherWith
            (slideOutVertically(
                targetOffsetY = { h -> -h / 3 },
                animationSpec = tween(100, easing = FastOutSlowInEasing)
            ) + fadeOut(tween(60)))
        },
        label = "topicCycle"
    ) { _ ->
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CurioColors.CreamWhite,
            tonalElevation = 4.dp,
            border = BorderStroke(width = 2.dp, color = accent.copy(alpha = 0.7f)),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Mini image placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(
                            color = CurioColors.ButterYellow.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    CurioIcon(
                        name = glyph,
                        contentDescription = null,
                        tint = accent.copy(alpha = 0.8f),
                        size = 56.dp
                    )
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    // Accent bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(
                                color = accent,
                                shape = RoundedCornerShape(3.dp)
                            )
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = topic.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = CurioColors.DeepPlum,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = topic.subtype,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Glow halo — ambient light behind the cards
// ═══════════════════════════════════════════════════════════════════════════

/**
 * A radial glow halo that sits behind the card stack and intensifies
 * during shuffle, giving a magical/cinematic feel.
 */
@Composable
private fun ShuffleGlowHalo(
    accent: Color,
    shuffling: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "glow")
    val pulse by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    val alphaTarget = if (shuffling) 0.22f else 0.06f
    val glowAlpha by animateFloatAsState(
        targetValue = alphaTarget * pulse,
        animationSpec = CurioMotion.Springs.Bouncy,
        label = "glowAlpha"
    )

    // Only render the glow when it would be visible (save the infinite
    // transition when idle — glowAlpha < 0.02f means effectively invisible)
    if (!shuffling && !(glowAlpha > 0.02f)) return

    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val maxRadius = size.minDimension * 0.7f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    accent.copy(alpha = glowAlpha),
                    accent.copy(alpha = glowAlpha * 0.3f),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = maxRadius
            ),
            radius = maxRadius,
            center = Offset(cx, cy)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Shuffle particle spray — topic chips flying off the cards
// ═══════════════════════════════════════════════════════════════════════════

/**
 * A Canvas particle system that sprays tiny topic-name-style chips outward
 * from the card stack during the shuffle. Each particle is a small rounded
 * rect in the category accent or tint color, with random velocity, gravity,
 * and rotation.
 */
@Composable
private fun ShuffleParticleSpray(
    accent: Color,
    tint: Color,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val particles = remember { ShuffleParticleFactory.make(22) }
    // Regenerate particles each time composition enters (progress resets on new shuffle)
    // They stay stable during the animation via remember without a key.

    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        // Particles live for ~800ms each, we use a sawtooth to cycle them
        val t = (progress * 1800f) % 800f // sawtooth cycling

        particles.forEach { p ->
            val particleTime = (t + p.phaseOffset) % 800f
            val particleProgress = (particleTime / 800f).coerceIn(0f, 1f)

            val dist = p.speed * particleTime
            val upOffset = p.upwardBias * particleTime - 0.5f * 0.0012f * particleTime * particleTime
            val angleRad = Math.toRadians(p.angleDeg.toDouble()).toFloat()
            val dx = cos(angleRad) * dist
            val dy = sin(angleRad) * dist + upOffset
            val px = cx + dx
            val py = cy + dy + 20f // slightly below center for a natural spray origin

            // Fade out
            val alpha = (1f - particleProgress).coerceIn(0f, 1f) * 0.85f
            if (alpha < 0.02f) return@forEach

            val color = if (p.colorIdx % 2 == 0) accent.copy(alpha = alpha)
                        else tint.copy(alpha = alpha)
            val rotation = p.rotation + p.rotationSpeed * particleTime

            rotate(degrees = rotation, pivot = Offset(px, py)) {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(px - p.sizePx / 2f, py - p.sizePx / 4f),
                    size = Size(p.sizePx, p.sizePx / 2f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(p.sizePx / 4f)
                )
            }
        }
    }
}

private data class SprayParticle(
    val angleDeg: Float,
    val speed: Float,
    val upwardBias: Float,
    val rotation: Float,
    val rotationSpeed: Float,
    val sizePx: Float,
    val colorIdx: Int,
    val phaseOffset: Float // 0–800ms offset for staggered emission
)

private object ShuffleParticleFactory {
    fun make(count: Int): List<SprayParticle> = List(count) {
        SprayParticle(
            angleDeg = Random.nextFloat() * 360f,
            speed = Random.nextFloat() * 0.28f + 0.08f,
            upwardBias = Random.nextFloat() * 0.35f + 0.15f,
            rotation = Random.nextFloat() * 360f,
            rotationSpeed = (Random.nextFloat() - 0.5f) * 0.8f,
            sizePx = 5f + Random.nextFloat() * 10f,
            colorIdx = it,
            phaseOffset = Random.nextFloat() * 800f
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Shuffle intensity bars — rhythmic visual beneath the card stack
// ═══════════════════════════════════════════════════════════════════════════

/**
 * A row of small horizontal bars that pulse in a wave pattern during shuffle.
 * Gives a subtle "equalizer / heartbeat" feel that reinforces the shuffle.
 */
@Composable
private fun ShuffleIntensityBars(
    accent: Color,
    shuffling: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "bars")
    val wave by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing)
        ),
        label = "barWave"
    )

    val barAlpha by animateFloatAsState(
        targetValue = if (shuffling) 0.5f else 0.12f,
        animationSpec = CurioMotion.Springs.Snappy,
        label = "barAlpha"
    )

    Row(
        modifier = modifier.padding(horizontal = 48.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(7) { i ->
            val barPhase = wave + i * 40f
            val barHeight = (8f + 16f * abs(sin(Math.toRadians(barPhase.toDouble())))).dp
            Box(
            modifier = Modifier
                .padding(horizontal = 3.dp)
                .size(width = 3.dp, height = barHeight)
                    .background(
                        color = accent.copy(alpha = barAlpha),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Card surfaces
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun CardSurface(
    color: Color,
    borderColor: Color,
    glyph: String,
    glyphTint: Color
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = color,
        tonalElevation = 2.dp,
        border = BorderStroke(width = 1.5.dp, color = borderColor),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            CurioIcon(
                name = glyph,
                contentDescription = null,
                tint = glyphTint,
                size = 72.dp
            )
        }
    }
}

@Composable
private fun LandedCard(
    accent: Color,
    glyph: String,
    topic: CurioTopic
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = CurioColors.CreamWhite,
        tonalElevation = 8.dp,
        border = BorderStroke(width = 2.5.dp, color = accent),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(
                        color = CurioColors.ButterYellow.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                CurioIcon(
                    name = glyph,
                    contentDescription = null,
                    tint = accent,
                    size = 72.dp
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    CurioIcon(
                        name = CurioIcons.AutoAwesome,
                        contentDescription = null,
                        tint = CurioColors.DeepPlum,
                        size = 20.dp
                    )
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(
                            color = accent,
                            shape = RoundedCornerShape(4.dp)
                        )
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = topic.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = CurioColors.DeepPlum,
                    maxLines = 2
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .height(6.dp)
                        .background(
                            color = CurioColors.DeepPlum.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(3.dp)
                        )
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
}

// ═══════════════════════════════════════════════════════════════════════════
// Tag chips
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun TagChipRow(
    tags: List<String>,
    selected: String?,
    accent: Color,
    onSelect: (String?) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item("all") {
            TagChip(
                label = "All",
                glyph = CurioIcons.AutoAwesome,
                accent = accent,
                selected = selected == null,
                onClick = { onSelect(null) }
            )
        }
        items(tags) { tag ->
            TagChip(
                label = tag,
                glyph = CurioIcons.AutoAwesome,
                accent = accent,
                selected = selected == tag,
                onClick = { onSelect(tag) }
            )
        }
    }
}

@Composable
private fun TagChip(
    label: String,
    glyph: String,
    accent: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) accent.copy(alpha = 0.20f)
                else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) accent
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CurioIcon(
                name = glyph,
                contentDescription = null,
                tint = if (selected) accent
                       else MaterialTheme.colorScheme.onSurfaceVariant,
                size = 16.dp
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) accent
                        else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Pick a random topic from the filtered pool using tier-weighted selection.
 *
 * Weight distribution (per CURIO_DATA_PLAN.md §4.3):
 *   - Tier 1 (human-curated marquee): weight 100 — surfaces ~50% of the time
 *   - Tier 2 (AI-curated long tail):  weight 60  — surfaces ~30% of the time
 *   - Tier 3 (draft / placeholder):  weight 20  — surfaces ~12% of the time
 *
 * This ensures the best-quality topics appear more frequently while still
 * giving the long tail a chance. Fallback to uniform random if weights
 * fail to produce a pick (e.g. empty pool after filtering).
 *
 * @param filteredPool The tag-filtered pool to pick from.
 * @param fallbackCategory Fallback category if filtered pool is empty.
 * @param recentIds Set of recently-shown topic IDs to avoid repeats.
 */
private fun pickRandomFrom(
    filteredPool: List<CurioTopic>,
    fallbackCategory: CategoryId,
    recentIds: Set<String> = emptySet()
): CurioTopic? {
    // ── Prefer filtered pool with tier weighting ─────────────────────────
    if (filteredPool.isNotEmpty()) {
        // Try weighted pick, avoiding recent repeats if possible
        val withoutRecents = filteredPool.filterNot { it.id in recentIds }
        val pool = if (withoutRecents.isNotEmpty()) withoutRecents else filteredPool
        return pickWeighted(pool)
    }

    // ── Fallback: load from cached category pool ─────────────────────────
    return try {
        val cached = TopicJsonLoader.cached(fallbackCategory)
        if (cached != null && cached.isNotEmpty()) {
            val withoutRecents = cached.filterNot { it.id in recentIds }
            val pool = if (withoutRecents.isNotEmpty()) withoutRecents else cached
            pickWeighted(pool)
        } else null
    } catch (e: Exception) {
        null
    }
}

/**
 * Weighted random pick from a pool using tier-based weights.
 * Falls back to uniform random if weights produce an invalid result.
 */
private fun pickWeighted(pool: List<CurioTopic>): CurioTopic? {
    if (pool.isEmpty()) return null
    if (pool.size == 1) return pool[0]

    // Calculate total weight
    val totalWeight = pool.sumOf { topic ->
        when (topic.tier) {
            1 -> 100
            2 -> 60
            3 -> 20
            else -> 30
        }
    }
    if (totalWeight <= 0) return pool.random()

    // Spin the weighted wheel
    var target = Random.nextInt(totalWeight)
    for (topic in pool) {
        val w = when (topic.tier) {
            1 -> 100
            2 -> 60
            3 -> 20
            else -> 30
        }
        target -= w
        if (target < 0) return topic
    }

    // Fallback (shouldn't reach here, but be safe)
    return pool.random()
}
