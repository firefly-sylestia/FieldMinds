package com.curio.app.features.spin

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.curio.app.data.CurioCategory
import com.curio.app.data.CurioTopic
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.CurioMotion
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sin

/**
 * The Spin deck — an interactive, swipeable stack of topic cards.
 *
 * Replaces the old fixed 3-slot vertical carousel. Gestures on the front
 * card:
 *
 *  - **Drag up / down** — browse the shuffled pool one card at a time with
 *    a snap-back spring, so the user can preview neighbours without
 *    re-spinning.
 *  - **Drag left** — reject the current topic and draw a fresh one. A
 *    "Skip" badge fades in once the gesture passes the commit threshold.
 *  - **Tap** — open the topic's detail (Topic Reveal) page.
 *
 * The axis is locked on the first few pixels of movement so a vertical
 * browse never accidentally registers as a reject (and vice versa).
 *
 * While [shuffling] is true all gestures are disabled, the deck compresses
 * slightly, a sweeping glow orbits behind the stack, and the front card's
 * content cross-slides on every cycle tick.
 */
@Composable
fun SpinDeck(
    cat: CurioCategory,
    pool: List<CurioTopic>,
    centerIndex: Int,
    shuffling: Boolean,
    landedTopic: CurioTopic?,
    onBrowse: (delta: Int) -> Unit,
    onReject: () -> Unit,
    onOpen: () -> Unit,
    onSpin: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (pool.isEmpty()) {
            EmptyDeckHint(cat = cat)
            return@BoxWithConstraints
        }

        val density = LocalDensity.current
        val cardWidth = (maxWidth * 0.78f).coerceAtMost(320.dp)
        val cardHeight = (maxHeight * 0.82f).coerceIn(180.dp, 300.dp)
        val flingDistance = with(density) { (maxWidth + 120.dp).toPx() }
        val browseThreshold = with(density) { 64.dp.toPx() }
        val rejectThreshold = with(density) { 96.dp.toPx() }
        val axisLock = with(density) { 8.dp.toPx() }

        val scope = rememberCoroutineScope()
        val offsetX = remember { Animatable(0f) }
        val offsetY = remember { Animatable(0f) }
        // 0 = undecided, 1 = horizontal (reject), 2 = vertical (browse)
        var axis by remember { mutableIntStateOf(0) }
        var dragging by remember { mutableStateOf(false) }

        // Any change of pool / landing resets a half-finished gesture so the
        // new front card is never left visually offset.
        LaunchedEffect(pool, landedTopic, shuffling) {
            offsetX.snapTo(0f)
            offsetY.snapTo(0f)
            axis = 0
        }

        val interactive = !shuffling && pool.isNotEmpty()

        // Deck compresses while spinning, and pops on landing.
        val deckScale by animateFloatAsState(
            targetValue = when {
                shuffling -> 0.94f
                landedTopic != null -> 1.03f
                else -> 1f
            },
            animationSpec = if (landedTopic != null) CurioMotion.Springs.Elastic
            else CurioMotion.Springs.Deliberate,
            label = "deckScale"
        )

        // ── Sweeping glow behind the deck while shuffling ──────────────
        SweepGlow(
            active = shuffling,
            color = cat.accent,
            modifier = Modifier.size(cardWidth + 72.dp, cardHeight + 72.dp)
        )

        Box(
            modifier = Modifier
                .size(cardWidth, cardHeight)
                .graphicsLayer {
                    scaleX = deckScale
                    scaleY = deckScale
                },
            contentAlignment = Alignment.Center
        ) {
            // ── Two peeking cards behind the front one ─────────────────
            listOf(2, 1).forEach { depth ->
                val backTopic = pool.getOrNull(wrapIndex(centerIndex + depth, pool.size))
                BackCard(
                    depth = depth,
                    accent = cat.accent,
                    glyph = cat.iconGlyph,
                    topic = backTopic,
                    shuffling = shuffling,
                    // The front card being dragged away lifts the deck below it.
                    lift = if (depth == 1) {
                        (abs(offsetX.value) / flingDistance).coerceIn(0f, 1f)
                    } else 0f,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // ── Front (interactive) card ───────────────────────────────
            val frontTopic = landedTopic
                ?: pool.getOrNull(wrapIndex(centerIndex, pool.size))
            val swipeProgress = (abs(offsetX.value) / rejectThreshold).coerceIn(0f, 1f)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(10f)
                    .graphicsLayer {
                        translationX = offsetX.value
                        translationY = offsetY.value
                        rotationZ = offsetX.value / 46f
                        alpha = 1f - (abs(offsetX.value) / flingDistance)
                            .coerceIn(0f, 1f) * 0.85f
                    }
                    .pointerInput(interactive) {
                        if (!interactive) return@pointerInput
                        detectTapGestures(
                            onTap = { onOpen() },
                            onDoubleTap = { onSpin() }
                        )
                    }
                    .pointerInput(interactive, pool.size) {
                        if (!interactive) return@pointerInput
                        detectDragGestures(
                            onDragStart = {
                                axis = 0
                                dragging = true
                            },
                            onDrag = { _, drag ->
                                if (axis == 0) {
                                    when {
                                        abs(drag.x) > axisLock && abs(drag.x) > abs(drag.y) -> axis = 1
                                        abs(drag.y) > axisLock && abs(drag.y) > abs(drag.x) -> axis = 2
                                    }
                                }
                                scope.launch {
                                    when (axis) {
                                        1 -> offsetX.snapTo(offsetX.value + drag.x)
                                        2 -> offsetY.snapTo(offsetY.value + drag.y)
                                    }
                                }
                            },
                            onDragCancel = {
                                dragging = false
                                scope.launch { settle(offsetX, offsetY) }
                            },
                            onDragEnd = {
                                dragging = false
                                val dx = offsetX.value
                                val dy = offsetY.value
                                scope.launch {
                                    when {
                                        // Reject → fling off to the left, draw fresh.
                                        dx < -rejectThreshold -> {
                                            offsetX.animateTo(
                                                -flingDistance,
                                                tween(220, easing = LinearEasing)
                                            )
                                            onReject()
                                            offsetX.snapTo(0f)
                                            offsetY.snapTo(0f)
                                        }
                                        // Browse forward.
                                        dy < -browseThreshold -> {
                                            offsetY.animateTo(
                                                -with(density) { cardHeight.toPx() },
                                                tween(150, easing = LinearEasing)
                                            )
                                            onBrowse(1)
                                            offsetY.snapTo(with(density) { cardHeight.toPx() } * 0.5f)
                                            offsetX.snapTo(0f)
                                            offsetY.animateTo(0f, CurioMotion.Springs.Deliberate)
                                        }
                                        // Browse backward.
                                        dy > browseThreshold -> {
                                            offsetY.animateTo(
                                                with(density) { cardHeight.toPx() },
                                                tween(150, easing = LinearEasing)
                                            )
                                            onBrowse(-1)
                                            offsetY.snapTo(-with(density) { cardHeight.toPx() } * 0.5f)
                                            offsetX.snapTo(0f)
                                            offsetY.animateTo(0f, CurioMotion.Springs.Deliberate)
                                        }
                                        else -> settle(offsetX, offsetY)
                                    }
                                }
                            }
                        )
                    }
            ) {
                FrontCard(
                    topic = frontTopic,
                    cat = cat,
                    shuffling = shuffling,
                    landed = landedTopic != null,
                    modifier = Modifier.fillMaxSize()
                )

                // ── "Skip" affordance while dragging left ──────────────
                if (offsetX.value < 0f && swipeProgress > 0.15f) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.error.copy(
                            alpha = 0.16f + swipeProgress * 0.7f
                        ),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            CurioIcon(
                                CurioIcons.Close, null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                size = 14.dp
                            )
                            Text(
                                "Skip",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }

        // ── Swipe hint — only before the first spin, and never mid-drag ──
        if (interactive && landedTopic == null && !dragging) {
            SwipeHint(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 2.dp)
            )
        }

        // ── Position dots (which card of the deck you're browsing) ──────
        if (pool.size > 1 && landedTopic == null) {
            DeckPosition(
                index = wrapIndex(centerIndex, pool.size),
                total = pool.size,
                accent = cat.accent,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 2.dp)
            )
        }
    }
}

/** Springs both axes back to rest — the "didn't pass the threshold" case. */
private suspend fun settle(
    offsetX: Animatable<Float, *>,
    offsetY: Animatable<Float, *>
) {
    kotlinx.coroutines.coroutineScope {
        launch { offsetX.animateTo(0f, CurioMotion.Springs.Snappy) }
        launch { offsetY.animateTo(0f, CurioMotion.Springs.Snappy) }
    }
}

private fun wrapIndex(pos: Int, size: Int): Int =
    if (size <= 0) 0 else ((pos % size) + size) % size

// ═══════════════════════════════════════════════════════════════════════════
// Cards
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun BackCard(
    depth: Int,
    accent: Color,
    glyph: String,
    topic: CurioTopic?,
    shuffling: Boolean,
    lift: Float,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    // Deeper cards sit lower, smaller, and more transparent.
    val baseScale = 1f - depth * 0.06f
    val baseY = depth * 16f
    val tilt = if (depth == 1) 1.6f else -2.2f

    Box(
        modifier = modifier
            .graphicsLayer {
                val l = lift * (2 - depth).coerceAtLeast(0)
                scaleX = baseScale + l * 0.06f
                scaleY = baseScale + l * 0.06f
                translationY = (baseY - l * baseY).dp.toPx()
                rotationZ = if (shuffling) tilt * 2f else tilt
                alpha = (if (isDark) 0.62f else 0.5f) - depth * 0.12f + lift * 0.35f
            }
            .zIndex((5 - depth).toFloat())
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                if (topic != null) {
                    Text(
                        text = topic.subtype,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.4.sp
                        ),
                        color = accent
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = topic.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    CurioIcon(glyph, null, tint = accent.copy(alpha = 0.25f), size = 24.dp)
                }
            }
        }
    }
}

@Composable
private fun FrontCard(
    topic: CurioTopic?,
    cat: CurioCategory,
    shuffling: Boolean,
    landed: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.clip(RoundedCornerShape(28.dp))) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            border = BorderStroke(
                width = if (landed) 2.dp else 1.5.dp,
                color = cat.accent.copy(alpha = if (landed) 0.85f else 0.4f)
            ),
            shadowElevation = if (shuffling) 4.dp else 14.dp,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxSize()
        ) {
            AnimatedContent(
                targetState = topic,
                transitionSpec = {
                    (slideInVertically { h -> h / 2 } + fadeIn(tween(140))) togetherWith
                        (slideOutVertically { h -> -h / 2 } + fadeOut(tween(110)))
                },
                label = "frontCardContent"
            ) { current ->
                if (current == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CurioIcon(
                                cat.iconGlyph, null,
                                tint = cat.accent.copy(alpha = 0.5f),
                                size = 40.dp
                            )
                            Text(
                                text = "Tap spin to draw a topic",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    FrontCardContent(topic = current, cat = cat, landed = landed)
                }
            }
        }
    }
}

@Composable
private fun FrontCardContent(
    topic: CurioTopic,
    cat: CurioCategory,
    landed: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(22.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(cat.accent)
            )
            Text(
                text = topic.subtype,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.4.sp
                ),
                color = cat.accent
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = topic.name,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 28.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )

        if (topic.tags.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                topic.tags.take(2).forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = cat.accent.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.labelSmall,
                            color = cat.accent,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        if (landed) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = topic.teaser,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CurioIcon(
                    CurioIcons.AutoAwesome, null,
                    tint = cat.accent,
                    size = 14.dp
                )
                Text(
                    text = "Tap to open",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = cat.accent
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Decoration
// ═══════════════════════════════════════════════════════════════════════════

/**
 * A soft arc that orbits behind the deck while the wheel spins — gives the
 * shuffle a sense of rotational momentum without a literal spinning wheel.
 */
@Composable
private fun SweepGlow(active: Boolean, color: Color, modifier: Modifier = Modifier) {
    if (!active) return
    val infinite = rememberInfiniteTransition(label = "sweep")
    val rot by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing)),
        label = "sweepRot"
    )
    Canvas(modifier = modifier) {
        val inset = 10.dp.toPx()
        val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
        // Three trailing arcs of decreasing opacity read as a motion trail.
        listOf(0f to 0.5f, -28f to 0.28f, -56f to 0.14f).forEach { (lag, alpha) ->
            drawArc(
                color = color.copy(alpha = alpha),
                startAngle = rot + lag,
                sweepAngle = 26f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
private fun SwipeHint(modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "hint")
    val bob by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing)),
        label = "hintBob"
    )
    Row(
        modifier = modifier.graphicsLayer {
            translationY = sin(bob * 2f * Math.PI.toFloat()) * 3f
            alpha = 0.55f
        },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        CurioIcon(
            CurioIcons.KeyboardArrowUp, null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            size = 14.dp
        )
        Text(
            text = "Swipe to browse · tap to open",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Vertical dot rail showing where in the deck the user is. */
@Composable
private fun DeckPosition(
    index: Int,
    total: Int,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val shown = total.coerceAtMost(5)
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(shown) { i ->
            val isActive = (index % shown) == i
            Box(
                modifier = Modifier
                    .size(width = if (isActive) 4.dp else 3.dp, height = if (isActive) 14.dp else 4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (isActive) accent
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.28f)
                    )
            )
        }
    }
}

@Composable
private fun EmptyDeckHint(cat: CurioCategory) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = cat.accent.copy(alpha = 0.10f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .height(200.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CurioIcon(
                cat.iconGlyph, null,
                tint = cat.accent.copy(alpha = 0.5f),
                size = 52.dp
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = "Nothing matches",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Loosen your filters to bring topics back into the deck.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
