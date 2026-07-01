package fieldmind.research.app.features.field.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import androidx.activity.ExperimentalActivityApi
import androidx.activity.compose.PredictiveBackHandler
import fieldmind.research.app.shared.presentation.components.icons.Icon
import kotlin.math.abs
import kotlin.math.roundToInt

// ══════════════════════════════════════════════════════════════════════
//  CompositionLocals for real-content peek (ScreenCache)
// ══════════════════════════════════════════════════════════════════════

/**
 * Holder for the previous screen's composable content to show during the
 * predictive back peek animation. Set by [FieldMindNavHost] so that every
 * [SwipeBackHost] instance can render real composable content behind the
 * current screen instead of mock placeholder cards.
 */
class PeekContentHolder {
    /** Stable key used with [Key] — the previous route string. */
    var peekKey: Any? by mutableStateOf(null)

    /** Composable lambda that renders the previous screen's content. */
    var peekContent: (@Composable () -> Unit)? by mutableStateOf(null)
}

val LocalPeekContentHolder = compositionLocalOf { PeekContentHolder() }

// ══════════════════════════════════════════════════════════════════════
//  Animation Configuration (runtime-tunable via Developer Settings)
// ══════════════════════════════════════════════════════════════════════

/**
 * Runtime-tunable animation parameters. Override via [LocalAnimationConfig]
 * to customize spring physics without recompiling.
 */
data class AnimationConfig(
    // — Ultra-soft, elegant, slow spring defaults —
    // These power the tunable sliders in Developer settings.
    // Very low stiffness for buttery-smooth, slow motion.
    // High damping (close to 1.0) = no bounce, pure smoothness.
    val entranceDampingRatio: Float = 0.95f,
    val entranceStiffness: Float = 120f,
    val swipeBackDampingRatio: Float = 0.92f,
    val swipeBackStiffness: Float = 120f,
    val cancelDampingRatio: Float = 0.95f,
    val cancelStiffness: Float = 80f,
    val tabEntranceDampingRatio: Float = 0.95f,
    val tabEntranceStiffness: Float = 180f,
    val swipeThreshold: Float = 0.20f,
    val swipeScaleFactor: Float = 0.92f
) {
    companion object {
        /** Default config used when no LocalAnimationConfig is provided. */
        val DEFAULT = AnimationConfig()
    }

    fun entranceSpring() = spring<Float>(
        dampingRatio = entranceDampingRatio,
        stiffness = entranceStiffness
    )

    fun swipeBackSpring() = spring<Float>(
        dampingRatio = swipeBackDampingRatio,
        stiffness = swipeBackStiffness
    )

    fun cancelSpring() = spring<Float>(
        dampingRatio = cancelDampingRatio,
        stiffness = cancelStiffness
    )

    fun tabEntranceSpring() = spring<Float>(
        dampingRatio = tabEntranceDampingRatio,
        stiffness = tabEntranceStiffness
    )
}

val LocalAnimationConfig = compositionLocalOf { AnimationConfig.DEFAULT }

/**
 * Material expressive motion specifications for FieldMind.
 */
object FieldMindMotion {

    // — Slow, elegant, buttery-smooth spring defaults —
    // Every spring throughout the app uses these specs.
    // Very low stiffness (~120-300) = slow, gentle, elegant motion.
    // High damping (≥0.85) = smooth glide, no bounce.

    val expressiveSpring = spring<Float>(
        dampingRatio = 0.92f,
        stiffness = 200f
    )

    val expressiveSoft = spring<Float>(
        dampingRatio = 0.95f,
        stiffness = 120f
    )

    val expressiveElastic = spring<Float>(
        dampingRatio = 0.93f,
        stiffness = 180f
    )

    val expressiveFloat = spring<Float>(
        dampingRatio = 0.94f,
        stiffness = 180f
    )

    val expressiveSnap = spring<Float>(
        dampingRatio = 0.92f,
        stiffness = 280f
    )

    val expressiveDramatic = spring<Float>(
        dampingRatio = 0.93f,
        stiffness = 160f
    )

    // -- Standard Springs (no overshoot) --

    val layoutSpring = spring<Float>(
        dampingRatio = 0.94f,
        stiffness = 140f
    )

    val pressSpring = spring<Float>(
        dampingRatio = 0.92f,
        stiffness = 250f
    )

    val confirmSpring = spring<Float>(
        dampingRatio = 0.95f,
        stiffness = 140f
    )

    // -- Navigation Springs --

    val swipeBackSpring = spring<Float>(
        dampingRatio = 0.92f,
        stiffness = 180f
    )

    val sharedElementSpring = spring<Float>(
        dampingRatio = 0.93f,
        stiffness = 180f
    )

    val slideSpring = spring<Float>(
        dampingRatio = 0.92f,
        stiffness = 180f
    )

    val fadeThroughSpring = spring<Float>(
        dampingRatio = 0.94f,
        stiffness = 180f
    )

    val slideOffsetSpring = spring<IntOffset>(
        dampingRatio = 0.94f,
        stiffness = 180f
    )

    // -- Duration Tokens (ms) --

    const val durationMicro = 100
    const val durationSubtle = 180
    const val durationStandard = 350
    const val durationEmphasized = 500
    const val durationExpressive = 800
    const val countUpMs = 600

    // -- Stagger & Delay Tokens --

    const val staggerItemDelayMs = 50
    const val staggerInitialDelayMs = 80
    const val staggerMaxDurationMs = 500

    // -- Shape Morphing --

    val morphSpring = spring<Float>(
        dampingRatio = 0.93f,
        stiffness = 180f
    )

    val cornerSpring = spring<Float>(
        dampingRatio = 0.94f,
        stiffness = 250f
    )

    // -- Convenience Tween --

    val fadeTween = tween<Float>(durationMillis = durationSubtle)
    val pressScaleTween = tween<Float>(durationMillis = durationMicro)

    // -- Swipe-back Constants --

    const val swipeEdgeWidthDp = 30f
    const val swipeEdgeHeightDp = 30f
    const val swipeThreshold = 0.20f
    const val swipeScaleFactor = 0.92f
    const val swipeScrimAlpha = 0.35f
    const val swipeShadowElevationDp = 24f
    const val swipeCornerRadiusDp = 28f
    const val swipeBaseCornerRadiusDp = 8f

    // -- Utility --

    fun entranceSpec(emphasis: Emphasis = Emphasis.Standard): AnimationSpec<Float> = when (emphasis) {
        Emphasis.Expressive -> expressiveDramatic
        Emphasis.Emphasized -> expressiveSpring
        Emphasis.Standard -> expressiveFloat
        Emphasis.Snap -> expressiveSnap
    }

    enum class Emphasis { Expressive, Emphasized, Standard, Snap }

    @Composable
    fun isReduceMotion(): Boolean {
        if (LocalInspectionMode.current) return false
        val context = LocalContext.current
        val animatorScale = try {
            android.provider.Settings.Global.getFloat(
                context.contentResolver,
                android.provider.Settings.Global.ANIMATOR_DURATION_SCALE
            )
        } catch (_: Exception) { 1f }
        return animatorScale == 0f
    }

    fun staggerDelay(index: Int): Int =
        (staggerInitialDelayMs + index * staggerItemDelayMs).coerceAtMost(staggerMaxDurationMs)
}

// -- Expressive Press Modifiers --

fun Modifier.expressivePress(
    scaleDown: Float = 0.95f,
    enabled: Boolean = true
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "expressivePress"
        properties["scaleDown"] = scaleDown
        properties["enabled"] = enabled
    }
) {
    var isPressed by remember { mutableStateOf(false) }
    val reduceMotion = FieldMindMotion.isReduceMotion()
    val target = if (isPressed && enabled && !reduceMotion) scaleDown else 1f

    val scale by animateFloatAsState(
        targetValue = target,
        animationSpec = if (isPressed) FieldMindMotion.expressiveSnap else FieldMindMotion.expressiveSpring,
        label = "expressivePress"
    )

    this
        .pointerInput(enabled) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    isPressed = event.changes.any { it.pressed }
                }
            }
        }
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            transformOrigin = TransformOrigin.Center
        }
}

fun Modifier.expressiveCardPress(
    liftDp: Float = 2f,
    scaleDown: Float = 0.98f,
    enabled: Boolean = true
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "expressiveCardPress"
        properties["liftDp"] = liftDp
        properties["scaleDown"] = scaleDown
    }
) {
    var isPressed by remember { mutableStateOf(false) }
    val reduceMotion = FieldMindMotion.isReduceMotion()

    val animScale by animateFloatAsState(
        targetValue = if (isPressed && enabled && !reduceMotion) scaleDown else 1f,
        animationSpec = if (isPressed) FieldMindMotion.expressiveSnap else FieldMindMotion.expressiveSpring,
        label = "cardScale"
    )
    val animLift by animateFloatAsState(
        targetValue = if (isPressed && enabled && !reduceMotion) -liftDp else 0f,
        animationSpec = if (isPressed) FieldMindMotion.expressiveSnap else FieldMindMotion.expressiveSoft,
        label = "cardLift"
    )

    this
        .pointerInput(enabled) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    isPressed = event.changes.any { it.pressed }
                }
            }
        }
        .graphicsLayer {
            scaleX = animScale
            scaleY = animScale
            translationY = animLift
            transformOrigin = TransformOrigin.Center
        }
}

fun Modifier.pressScale(
    scaleDown: Float = 0.97f,
    enabled: Boolean = true
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "pressScale"
        properties["scaleDown"] = scaleDown
        properties["enabled"] = enabled
    }
) {
    var isPressed by remember { mutableStateOf(false) }
    val reduceMotion = FieldMindMotion.isReduceMotion()
    val target = if (isPressed && enabled && !reduceMotion) scaleDown else 1f

    val scale by animateFloatAsState(
        targetValue = target,
        animationSpec = FieldMindMotion.pressSpring,
        label = "pressScale"
    )

    this
        .pointerInput(enabled) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    isPressed = event.changes.any { it.pressed }
                }
            }
        }
        .scale(scale)
}

fun Modifier.pressCardScale(): Modifier = composed {
    this.pressScale(scaleDown = 0.97f)
}

// ── Staggered Entrance Animation (fadeIn + slideUp) ──

/**
 * A [Modifier] that animates a composable's entrance with a fade-in and
 * upward slide, staggered by [index] for a delightful cascade reveal.
 *
 * When [animate] is true, the item starts invisible and slightly below its
 * final position, then fades in and slides up after a staggered delay.
 * Pass `animate = true` and the item's [index] when rendering cards in a
 * LazyColumn for a polished scroll-reveal effect.
 *
 * @param index  Zero-based position in the list; determines stagger delay.
 * @param animate  Whether to play the entrance animation. Default false.
 * @param offsetY  The vertical slide distance. Default 20dp.
 */
fun Modifier.staggeredEntrance(
    index: Int = 0,
    animate: Boolean = false,
    offsetY: Dp = 20.dp
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "staggeredEntrance"
        properties["index"] = index
        properties["animate"] = animate
    }
) {
    val reduceMotion = FieldMindMotion.isReduceMotion()
    var hasAnimatedIn by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val shouldAnimate = animate && !reduceMotion

    LaunchedEffect(animate, index, reduceMotion) {
        if (shouldAnimate) {
            delay(FieldMindMotion.staggerDelay(index).toLong())
            hasAnimatedIn = true
        } else {
            // No animation — immediately visible
            hasAnimatedIn = true
        }
    }

    val targetAlpha = if (!shouldAnimate || hasAnimatedIn) 1f else 0f
    val targetTranslationY = if (!shouldAnimate || hasAnimatedIn) 0f else with(density) { offsetY.toPx() }

    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = FieldMindMotion.expressiveFloat,
        label = "entranceAlpha_$index"
    )
    val translationY by animateFloatAsState(
        targetValue = targetTranslationY,
        animationSpec = FieldMindMotion.expressiveFloat,
        label = "entranceSlide_$index"
    )

    this.graphicsLayer {
        this.alpha = alpha
        this.translationY = translationY
    }
}

// -- Swipe-back Gesture Host -- iOS-style with predictive peek --

private enum class SwipeDirection { Horizontal, Vertical }

/** DEPRECATED — kept to avoid binary compatibility issues; will be removed in next major release. */
enum class PeekScreenType { Settings, Detail, Tool, Creation, Generic }

/** DEPRECATED — kept to avoid binary compatibility issues; will be removed in next major release. */
data class PreviousScreenInfo(
    val label: String,
    val route: String = "",
    val screenType: PeekScreenType = PeekScreenType.Generic
)

/** DEPRECATED — kept for binary compatibility. Renders nothing. */
@Composable
@Suppress("UNUSED_PARAMETER")
private fun PeekPreviewContent(
    screenType: PeekScreenType,
    label: String,
    accentColor: Color
) = Unit

@OptIn(ExperimentalActivityApi::class)
@Composable
fun SwipeBackHost(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val reduceMotion = FieldMindMotion.isReduceMotion()
    val scope = rememberCoroutineScope()
    val haptics = rememberFieldMindHaptics()

    var activeDirection by remember { mutableStateOf<SwipeDirection?>(null) }
    val animX = remember { Animatable(0f) }
    val animY = remember { Animatable(0f) }
    var contentWidth by remember { mutableFloatStateOf(1f) }
    var contentHeight by remember { mutableFloatStateOf(1f) }

    // Reactively detect keyboard visibility via WindowInsets (no polling needed)
    val density = LocalDensity.current
    val imeBottom = with(density) { WindowInsets.ime.getBottom(density) }
    val isImeVisible = imeBottom > 0

    // ── Flag to prevent detectDragGestures from competing with PredictiveBackHandler ──
    var isPredictiveBackActive by remember { mutableStateOf(false) }

    // Predictive back gesture (Android 14+) — drives peek animation from system back gesture
    val animConfig = LocalAnimationConfig.current
    PredictiveBackHandler(enabled = !reduceMotion && !isImeVisible) { progressFlow ->
        isPredictiveBackActive = true
        try {
            // Inside PredictiveBackHandler coroutine — snapTo is suspend, call directly
            progressFlow.collect { backEvent ->
                animX.snapTo((contentWidth * backEvent.progress).coerceAtLeast(0f))
            }
            // Flow completed → gesture committed
            isPredictiveBackActive = false
            // Reset offset before navigating to prevent blank/offset screen
            // during the pop exit transition.
            animX.snapTo(0f)
            animY.snapTo(0f)
            // Navigate immediately for both gesture swipes AND hardware button presses.
            // Note: detectDragGestures.onDragEnd does NOT fire for system back gestures,
            // so we must navigate here directly rather than deferring to onDragEnd.
            haptics.confirm()
            onBack()
        } catch (_: CancellationException) {
            // Gesture cancelled — smooth spring animation back to 0
            isPredictiveBackActive = false
            scope.launch {
                animX.animateTo(
                    0f,
                    animationSpec = animConfig.cancelSpring()
                )
            }
        }
    }

    // ── Unified progress computation ──
    // PredictiveBackHandler drives animX but leaves activeDirection=null.
    // Manual drag sets activeDirection. We compute progress from whichever
    // source has positive offset.
    val horizontalProgress = (abs(animX.value) / contentWidth).coerceIn(0f, 1f)
    val verticalProgress = (abs(animY.value) / contentHeight).coerceIn(0f, 1f)
    val (progress, isHorizontalPeek) = when (activeDirection) {
        SwipeDirection.Horizontal -> Pair(horizontalProgress, true)
        SwipeDirection.Vertical -> Pair(verticalProgress, false)
        null -> Pair(horizontalProgress.coerceAtLeast(verticalProgress), horizontalProgress >= verticalProgress)
    }
    val scrimAlpha = progress * FieldMindMotion.swipeScrimAlpha
    val contentScale = 1f - progress * (1f - animConfig.swipeScaleFactor)
    val swipeElevation = progress * FieldMindMotion.swipeShadowElevationDp
    val swipeCornerRadius = (FieldMindMotion.swipeBaseCornerRadiusDp + progress * (FieldMindMotion.swipeCornerRadiusDp - FieldMindMotion.swipeBaseCornerRadiusDp)).dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coords ->
                contentWidth = coords.size.width.toFloat().coerceAtLeast(1f)
                contentHeight = coords.size.height.toFloat().coerceAtLeast(1f)
            }
            .background(Color.Transparent) // ensure transparent background so previous screen preview is visible
    ) {
        // ── Layer 1: Previous screen peek preview ──
        // Renders the REAL previous screen composable behind the current screen
        // using [PeekContentHolder] (set by [FieldMindNavHost]). The composable
        // is always in the tree with [Key] for state preservation — hidden
        // offscreen when not peeking, revealed on the left during the back gesture.
        val peekHolder = LocalPeekContentHolder.current
        val realPeekContent = peekHolder.peekContent
        val realPeekKey = peekHolder.peekKey
        val hasRealContent = realPeekContent != null && realPeekKey != null

        if (isHorizontalPeek && hasRealContent) {
            val previewScale = 0.94f + (1f - 0.94f) * (1f - progress)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset {
                        // Always in tree; offscreen (-contentWidth) when
                        // not peeking, pinned to left of current screen when peeking.
                        val offset = if (progress > 0.005f) animX.value - contentWidth else -contentWidth
                        IntOffset(offset.roundToInt(), 0)
                    }
                    .width(Dp(contentWidth))
                    .fillMaxHeight()
                    .graphicsLayer {
                        scaleX = previewScale
                        scaleY = previewScale
                        transformOrigin = TransformOrigin(1f, 0.5f)
                        // Hide when not peeking (but keep in tree for state)
                        alpha = if (progress <= 0.005f) 0f else 1f
                    }
            ) {
                // ── REAL previous screen composable (kept alive with Key) ──
                key(realPeekKey) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(topEnd = 32.dp, bottomEnd = 36.dp),
                        tonalElevation = 3.dp,
                        shadowElevation = 16.dp,
                        border = androidx.compose.foundation.BorderStroke(
                            0.5.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                        )
                    ) {
                        realPeekContent()
                    }
                }
            }
        }

        // ── Layer 2: Scrim (dark gradient on reveal side) ──
        if (progress > 0.01f && isHorizontalPeek) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = scrimAlpha * 0.85f),
                                Color.Black.copy(alpha = scrimAlpha * 0.35f),
                                Color.Transparent
                            ),
                            startX = 0f,
                            endX = contentWidth * 0.5f
                        )
                    )
            )
        } else if (progress > 0.01f) {
            // Vertical scrim for downward swipe
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = scrimAlpha * 0.85f),
                                Color.Black.copy(alpha = scrimAlpha * 0.35f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = contentHeight * 0.5f
                        )
                    )
            )
        }

        // ── Layer 3: Current screen content (transformed) ──
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(swipeCornerRadius))
                .graphicsLayer {
                    val ox = animX.value.roundToInt()
                    val oy = animY.value.roundToInt()
                    translationX = ox.toFloat()
                    translationY = oy.toFloat()
                    scaleX = contentScale
                    scaleY = contentScale
                    this.shadowElevation = swipeElevation
                    transformOrigin = TransformOrigin(
                        if (ox > 0) 0f else 0.5f,
                        if (oy > 0) 0f else 0.5f
                    )
                    clip = true
                }
                .then(
                    if (!reduceMotion && !isImeVisible) {
                        Modifier.pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { startPos ->
                                    // Skip manual drag if PredictiveBackHandler is already active
                                    // to prevent competing animation drivers.
                                    if (isPredictiveBackActive) return@detectDragGestures
                                    if (startPos.x <= FieldMindMotion.swipeEdgeWidthDp) {
                                        activeDirection = SwipeDirection.Horizontal
                                    } else if (startPos.y <= FieldMindMotion.swipeEdgeHeightDp) {
                                        activeDirection = SwipeDirection.Vertical
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    // Safety net: if PredictiveBackHandler became active after
                                    // onDragStart, skip to prevent competing animation drivers.
                                    if (isPredictiveBackActive) return@detectDragGestures
                                    change.consume()
                                    when (activeDirection) {
                                        SwipeDirection.Horizontal -> {
                                            val newX = (animX.value + dragAmount.x).coerceAtLeast(0f)
                                            scope.launch { animX.snapTo(newX) }
                                        }
                                        SwipeDirection.Vertical -> {
                                            val newY = (animY.value + dragAmount.y).coerceAtLeast(0f)
                                            scope.launch { animY.snapTo(newY) }
                                        }
                                        null -> {
                                            val dx = dragAmount.x
                                            val dy = dragAmount.y
                                            if (abs(dx) > abs(dy) && dx > 0) {
                                                activeDirection = SwipeDirection.Horizontal
                                                scope.launch { animX.snapTo(dx.coerceAtLeast(0f)) }
                                            } else if (abs(dy) > abs(dx) && dy > 0) {
                                                activeDirection = SwipeDirection.Vertical
                                                scope.launch { animY.snapTo(dy.coerceAtLeast(0f)) }
                                            }
                                        }
                                    }
                                },
                                onDragEnd = {
                                    // Safety net: if PredictiveBackHandler became active,
                                    // don't navigate — PredictiveBackHandler already handles it.
                                    if (isPredictiveBackActive) {
                                        activeDirection = null
                                        scope.launch {
                                            animX.snapTo(0f)
                                            animY.snapTo(0f)
                                        }
                                        return@detectDragGestures
                                    }
                                    val maxVal = when (activeDirection) {
                                        SwipeDirection.Horizontal -> contentWidth
                                        SwipeDirection.Vertical -> contentHeight
                                        null -> Float.MAX_VALUE
                                    }
                                    val currentVal = when (activeDirection) {
                                        SwipeDirection.Horizontal -> animX.value
                                        SwipeDirection.Vertical -> animY.value
                                        null -> 0f
                                    }
                                    if (currentVal > maxVal * animConfig.swipeThreshold) {
                                        haptics.confirm()
                                        activeDirection = null
                                        // Snap offset to 0 immediately before navigating.
                                        // snapTo is a suspend function — must be called inside scope.launch.
                                        scope.launch {
                                            animX.snapTo(0f)
                                            animY.snapTo(0f)
                                            onBack()
                                        }
                                    } else {
                                        // Smooth spring back to 0
                                        activeDirection = null
                                        scope.launch {
                                            animX.animateTo(0f, animConfig.swipeBackSpring())
                                            animY.animateTo(0f, animConfig.swipeBackSpring())
                                        }
                                    }
                                },
                                onDragCancel = {
                                    activeDirection = null
                                    scope.launch {
                                        animX.animateTo(0f, animConfig.swipeBackSpring())
                                        animY.animateTo(0f, animConfig.swipeBackSpring())
                                    }
                                }
                            )
                        }
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.TopStart
        ) {
            content()

            // ── Back arrow indicator ──
            if (isHorizontalPeek && animX.value > contentWidth * 0.05f) {
                Box(
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .align(Alignment.CenterStart)
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(FieldMindIcons.ChevronLeft, "Swipe back", size = 22.dp, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // ── Downward swipe indicator ──
            if (!isHorizontalPeek && animY.value > contentHeight * 0.05f) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .align(Alignment.TopCenter)
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(FieldMindIcons.ChevronDown, "Swipe down to dismiss", size = 22.dp, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
