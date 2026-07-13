package fieldmind.research.app.features.field.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.util.lerp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.activity.compose.BackHandler
import fieldmind.research.app.features.field.presentation.theme.FieldMindTheme
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
    // — Lively, snappy spring defaults —
    // Damping: lower = more visible bounce (0.82 = gentle pop, 0.65 = playful)
    // Stiffness: higher = faster completion (280 = snappy, 120 = relaxed)
    val entranceDampingRatio: Float = 0.82f,
    val entranceStiffness: Float = 280f,
    val expressiveDampingRatio: Float = 0.78f,
    val expressiveStiffness: Float = 320f,
    val pressDampingRatio: Float = 0.85f,
    val pressStiffness: Float = 350f,
    val swipeBackDampingRatio: Float = 0.88f,
    val swipeBackStiffness: Float = 340f,
    val cancelDampingRatio: Float = 0.90f,
    val cancelStiffness: Float = 200f,
    val tabEntranceDampingRatio: Float = 0.80f,
    val tabEntranceStiffness: Float = 300f,
    val swipeThreshold: Float = 0.20f,
    val swipeScaleFactor: Float = 0.92f,
    val slideStiffness: Float = 400f,
    val staggerItemDelayMs: Int = 30,
    val staggerInitialDelayMs: Int = 40,
    val staggerMaxDurationMs: Int = 300,
    // — New expressive motion tunables —
    val morphDurationMs: Int = 400,
    val sideRevealDistanceDp: Float = 40f,
    val shimmerSpeedMs: Int = 1200,
    val pulseDurationMs: Int = 1500,
    val listChoreographyEnabled: Boolean = true,
    val confettiEnabled: Boolean = true,
    val pageFlipEnabled: Boolean = true
) {
    companion object {
        /** Default config used when no LocalAnimationConfig is provided. */
        val DEFAULT = AnimationConfig()
    }

    fun entranceSpring() = spring<Float>(dampingRatio = entranceDampingRatio, stiffness = entranceStiffness)
    fun expressiveSpring() = spring<Float>(dampingRatio = expressiveDampingRatio, stiffness = expressiveStiffness)
    fun pressSpring() = spring<Float>(dampingRatio = pressDampingRatio, stiffness = pressStiffness)
    fun swipeBackSpring() = spring<Float>(dampingRatio = swipeBackDampingRatio, stiffness = swipeBackStiffness)
    fun cancelSpring() = spring<Float>(dampingRatio = cancelDampingRatio, stiffness = cancelStiffness)
    fun tabEntranceSpring() = spring<Float>(dampingRatio = tabEntranceDampingRatio, stiffness = tabEntranceStiffness)
    fun slideSpring() = spring<IntOffset>(dampingRatio = 0.90f, stiffness = slideStiffness)
}

val LocalAnimationConfig = compositionLocalOf { AnimationConfig.DEFAULT }

/** CompositionLocal controlling whether animations are globally enabled. */
val LocalAnimationsEnabled = compositionLocalOf { true }

/**
 * Material expressive motion specifications for FieldMind.
 * All spring specs now read from [LocalAnimationConfig] for runtime tuning.
 */
object FieldMindMotion {

    // — Lively, tunable spring specs —
    // Every spring reads from AnimationConfig so Developer sliders and speed
    // presets affect ALL animations throughout the app.

    val expressiveSpring
        @Composable get() = spring<Float>(
            dampingRatio = LocalAnimationConfig.current.entranceDampingRatio,
            stiffness = LocalAnimationConfig.current.entranceStiffness
        )

    val expressiveFloat
        @Composable get() = spring<Float>(
            dampingRatio = LocalAnimationConfig.current.entranceDampingRatio,
            stiffness = (LocalAnimationConfig.current.entranceStiffness * 0.78f).coerceAtLeast(60f)
        )

    val expressiveSoft
        @Composable get() = spring<Float>(
            dampingRatio = LocalAnimationConfig.current.entranceDampingRatio + 0.06f,
            stiffness = (LocalAnimationConfig.current.entranceStiffness * 0.45f).coerceAtLeast(40f)
        )

    val expressiveElastic
        @Composable get() = spring<Float>(
            dampingRatio = (LocalAnimationConfig.current.entranceDampingRatio - 0.04f).coerceIn(0.5f, 0.95f),
            stiffness = LocalAnimationConfig.current.entranceStiffness
        )

    val expressiveSnap
        @Composable get() = spring<Float>(
            dampingRatio = LocalAnimationConfig.current.pressDampingRatio,
            stiffness = LocalAnimationConfig.current.pressStiffness
        )

    val expressiveDramatic
        @Composable get() = spring<Float>(
            dampingRatio = LocalAnimationConfig.current.expressiveDampingRatio,
            stiffness = LocalAnimationConfig.current.expressiveStiffness
        )

    // -- Bouncy Springs (visible overshoot) --

    fun bouncySpring(dampingRatio: Float = 0.58f, stiffness: Float = 160f) =
        spring<Float>(dampingRatio = dampingRatio, stiffness = stiffness)

    val bouncyEntrance
        @Composable get() = bouncySpring(dampingRatio = 0.55f, stiffness = (LocalAnimationConfig.current.entranceStiffness * 0.55f).coerceAtLeast(60f))

    val bouncyPress
        @Composable get() = bouncySpring(dampingRatio = 0.65f, stiffness = (LocalAnimationConfig.current.pressStiffness * 0.6f).coerceAtLeast(80f))

    val bouncyCelebration
        @Composable get() = bouncySpring(dampingRatio = 0.45f, stiffness = 120f)

    // -- Standard Springs (no overshoot) --

    val layoutSpring
        @Composable get() = spring<Float>(dampingRatio = 0.88f, stiffness = (LocalAnimationConfig.current.entranceStiffness * 0.6f).coerceAtLeast(60f))

    val pressSpring
        @Composable get() = spring<Float>(dampingRatio = LocalAnimationConfig.current.pressDampingRatio, stiffness = LocalAnimationConfig.current.pressStiffness)

    val confirmSpring
        @Composable get() = spring<Float>(dampingRatio = 0.88f, stiffness = (LocalAnimationConfig.current.entranceStiffness * 0.55f).coerceAtLeast(60f))

    // -- Navigation Springs --

    val swipeBackSpring
        @Composable get() = spring<Float>(dampingRatio = LocalAnimationConfig.current.swipeBackDampingRatio, stiffness = LocalAnimationConfig.current.swipeBackStiffness)

    val sharedElementSpring
        @Composable get() = spring<Float>(dampingRatio = LocalAnimationConfig.current.entranceDampingRatio, stiffness = (LocalAnimationConfig.current.entranceStiffness * 0.7f).coerceAtLeast(80f))

    val slideSpring
        @Composable get() = spring<Float>(dampingRatio = 0.90f, stiffness = (LocalAnimationConfig.current.slideStiffness * 0.5f).coerceAtLeast(80f))

    val fadeThroughSpring
        @Composable get() = spring<Float>(dampingRatio = 0.88f, stiffness = (LocalAnimationConfig.current.entranceStiffness * 0.6f).coerceAtLeast(80f))

    val slideOffsetSpring
        @Composable get() = spring<IntOffset>(dampingRatio = 0.90f, stiffness = LocalAnimationConfig.current.slideStiffness)

    // -- Duration Tokens (ms) --

    const val durationMicro = 80
    const val durationSubtle = 150
    const val durationStandard = 300
    const val durationEmphasized = 400
    const val durationExpressive = 600
    const val countUpMs = 500

    // -- Stagger & Delay Tokens (now tunable via AnimationConfig) --

    val staggerItemDelayMs
        @Composable get() = LocalAnimationConfig.current.staggerItemDelayMs
    val staggerInitialDelayMs
        @Composable get() = LocalAnimationConfig.current.staggerInitialDelayMs
    val staggerMaxDurationMs
        @Composable get() = LocalAnimationConfig.current.staggerMaxDurationMs

    // -- Shape Morphing --

    val morphSpring
        @Composable get() = spring<Float>(dampingRatio = 0.88f, stiffness = (LocalAnimationConfig.current.entranceStiffness * 0.65f).coerceAtLeast(70f))

    val cornerSpring
        @Composable get() = spring<Float>(dampingRatio = 0.88f, stiffness = (LocalAnimationConfig.current.entranceStiffness * 0.75f).coerceAtLeast(80f))

    // -- Convenience Tween --

    val fadeTween = tween<Float>(durationMillis = durationSubtle)
    val pressScaleTween = tween<Float>(durationMillis = durationMicro)

    // -- Swipe-back Constants --

    const val swipeEdgeWidthDp = 30f
    const val swipeEdgeHeightDp = 30f
    const val swipeThreshold = 0.18f
    const val swipeScaleFactor = 0.90f
    const val swipeScrimAlpha = 0.30f
    const val swipeShadowElevationDp = 24f
    const val swipeCornerRadiusDp = 28f
    const val swipeBaseCornerRadiusDp = 8f

    // -- Utility --

    @Composable
    fun entranceSpec(emphasis: Emphasis = Emphasis.Standard): AnimationSpec<Float> = when (emphasis) {
        Emphasis.Expressive -> expressiveDramatic
        Emphasis.Emphasized -> expressiveSpring
        Emphasis.Standard -> expressiveFloat
        Emphasis.Snap -> expressiveSnap
        Emphasis.Bouncy -> bouncyEntrance
    }

    enum class Emphasis { Expressive, Emphasized, Standard, Snap, Bouncy }

    @Composable
    fun isReduceMotion(): Boolean {
        if (LocalInspectionMode.current) return false
        // Respect app's global animation toggle
        if (!LocalAnimationsEnabled.current) return true
        val context = LocalContext.current
        val animatorScale = try {
            android.provider.Settings.Global.getFloat(
                context.contentResolver,
                android.provider.Settings.Global.ANIMATOR_DURATION_SCALE
            )
        } catch (_: Exception) { 1f }
        return animatorScale == 0f
    }

    /** Compute stagger delay from the provided [config]. Call from @Composable scope. */
    fun staggerDelay(index: Int, config: AnimationConfig = AnimationConfig.DEFAULT): Int =
        (config.staggerInitialDelayMs + index * config.staggerItemDelayMs).coerceAtMost(config.staggerMaxDurationMs)
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
            if (!enabled) return@pointerInput
            awaitPointerEventScope {
                while (true) {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    if (down.pressed) {
                        isPressed = true
                        down.consume()
                        // Wait for up or cancellation
                        do {
                            val upEvent = awaitPointerEvent()
                        } while (upEvent.changes.all { it.pressed })
                        upEvent.changes.forEach { it.consume() }
                        isPressed = false
                    }
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
            if (!enabled) return@pointerInput
            awaitPointerEventScope {
                while (true) {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    if (down.pressed) {
                        isPressed = true
                        down.consume()
                        // Wait for up or cancellation
                        do {
                            val upEvent = awaitPointerEvent()
                        } while (upEvent.changes.all { it.pressed })
                        upEvent.changes.forEach { it.consume() }
                        isPressed = false
                    }
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
            if (!enabled) return@pointerInput
            awaitPointerEventScope {
                while (true) {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    if (down.pressed) {
                        isPressed = true
                        down.consume()
                        // Wait for up or cancellation
                        do {
                            val upEvent = awaitPointerEvent()
                        } while (upEvent.changes.all { it.pressed })
                        upEvent.changes.forEach { it.consume() }
                        isPressed = false
                    }
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
    val animConfig = LocalAnimationConfig.current
    val staggerMs = FieldMindMotion.staggerDelay(index, animConfig)

    LaunchedEffect(animate, index, reduceMotion) {
        if (shouldAnimate) {
            delay(staggerMs.toLong())
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

// ── Bouncy Entrance Animation (scale-pop + overshoot) ──

/**
 * A [Modifier] that animates a composable's entrance with a playful scale-up
 * bounce, overshooting slightly above the target before settling.
 *
 * Items start at [initialScale] (e.g., 0.85 = 85% size), pop up past 1.0 with
 * a bouncy spring, then settle at 1.0. Works best on cards, buttons, and icons
 * that benefit from a lively "boing" reveal — use instead of [staggeredEntrance]
 * when you want attention-grabbing energy rather than smooth fade+slide.
 *
 * Respects system reduce-motion accessibility settings.
 *
 * @param index       Zero-based position; determines stagger delay.
 * @param animate     Whether to play the entrance animation. Default false.
 * @param initialScale  Starting scale (below 1.0). Default 0.85.
 * @param dampingRatio   Lower = more bounces. Default 0.62.
 * @param stiffness      Higher = faster. Default 140.
 */
fun Modifier.bouncyEntrance(
    index: Int = 0,
    animate: Boolean = false,
    initialScale: Float = 0.85f,
    dampingRatio: Float = 0.62f,
    stiffness: Float = 140f
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "bouncyEntrance"
        properties["index"] = index
        properties["animate"] = animate
    }
) {
    val reduceMotion = FieldMindMotion.isReduceMotion()
    val shouldAnimate = animate && !reduceMotion
    val scale = remember { Animatable(if (shouldAnimate) initialScale else 1f) }

    val animConfigB = LocalAnimationConfig.current
    val staggerMsB = FieldMindMotion.staggerDelay(index, animConfigB)

    LaunchedEffect(animate, index, reduceMotion) {
        if (shouldAnimate) {
            scale.snapTo(initialScale)
            delay(staggerMsB.toLong())
            // Single animateTo(1f) — low dampingRatio naturally overshoots
            scale.animateTo(1f, FieldMindMotion.bouncySpring(
                dampingRatio = dampingRatio,
                stiffness = stiffness
            ))
        } else {
            scale.snapTo(1f)
        }
    }

    this.graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
        transformOrigin = TransformOrigin.Center
    }
}

// ── Side Reveal Animation (slide in from a side) ──

/**
 * A [Modifier] that animates a composable's entrance by sliding it in from
 * one of the four sides. The composable starts off-screen (or partially
 * offset) and slides into its final position with a spring.
 *
 * @param fromSide  Which side to slide from: [SideRevealDirection.Start],
 *                  [End], [Top], or [Bottom].
 * @param animate   Whether to play the entrance animation.
 * @param distance  How far to slide (in dp). Defaults to the config value.
 */
fun Modifier.sideReveal(
    fromSide: SideRevealDirection = SideRevealDirection.Start,
    animate: Boolean = false,
    distance: Dp? = null
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "sideReveal"
        properties["fromSide"] = fromSide
        properties["animate"] = animate
    }
) {
    val reduceMotion = FieldMindMotion.isReduceMotion()
    val shouldAnimate = animate && !reduceMotion
    var hasAnimatedIn by remember { mutableStateOf(false) }
    val animConfig = LocalAnimationConfig.current
    val density = LocalDensity.current
    val slideDistance = with(density) { (distance ?: animConfig.sideRevealDistanceDp.dp).toPx() }

    LaunchedEffect(animate, fromSide, reduceMotion) {
        if (shouldAnimate) {
            delay(animConfig.staggerInitialDelayMs.toLong())
            hasAnimatedIn = true
        } else {
            hasAnimatedIn = true
        }
    }

    val target = if (hasAnimatedIn) 0f else slideDistance
    val offsetX by animateFloatAsState(
        targetValue = when (fromSide) {
            SideRevealDirection.Start -> if (hasAnimatedIn) 0f else slideDistance
            SideRevealDirection.End -> if (hasAnimatedIn) 0f else -slideDistance
            else -> 0f
        },
        animationSpec = FieldMindMotion.expressiveSpring,
        label = "sideRevealX"
    )
    val offsetY by animateFloatAsState(
        targetValue = when (fromSide) {
            SideRevealDirection.Top -> if (hasAnimatedIn) 0f else slideDistance
            SideRevealDirection.Bottom -> if (hasAnimatedIn) 0f else -slideDistance
            else -> 0f
        },
        animationSpec = FieldMindMotion.expressiveSpring,
        label = "sideRevealY"
    )

    this.graphicsLayer {
        this.translationX = offsetX
        this.translationY = offsetY
        this.alpha = if (hasAnimatedIn) 1f else 0f
    }
}

enum class SideRevealDirection { Start, End, Top, Bottom }

// ── Morph Shape Animation (corner radius / shape morph) ──

/**
 * A [Modifier] that animates the corner radius of a composable between two
 * values. Useful for morphing chips into cards, or buttons into pills.
 *
 * @param targetCornerRadius  Target corner radius in dp.
 * @param animate             Whether to animate the change.
 */
fun Modifier.morphShape(
    targetCornerRadius: Dp,
    animate: Boolean = true
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "morphShape"
        properties["targetCornerRadius"] = targetCornerRadius
        properties["animate"] = animate
    }
) {
    val reduceMotion = FieldMindMotion.isReduceMotion()
    val animConfig = LocalAnimationConfig.current
    val density = LocalDensity.current
    val targetPx = with(density) { targetCornerRadius.toPx() }

    val animatedRadius by animateFloatAsState(
        targetValue = targetPx,
        animationSpec = if (animate && !reduceMotion) {
            spring(dampingRatio = animConfig.entranceDampingRatio, stiffness = animConfig.entranceStiffness)
        } else {
            spring(stiffness = Spring.StiffnessMedium)
        },
        label = "morphShape"
    )

    this.clip(RoundedCornerShape(animatedRadius))
}

// ── Shimmer Loading Effect ──

/**
 * A [Modifier] that applies a shimmering sweep effect over the composable.
 * Ideal for loading placeholders. Respects reduce-motion and the global
 * animation toggle.
 *
 * @param enabled  Whether the shimmer is active.
 * @param baseColor  Base color of the shimmer; defaults to surface variant.
 * @param highlightColor  Highlight color; defaults to a lighter variant.
 */
fun Modifier.shimmer(
    enabled: Boolean = true,
    baseColor: Color? = null,
    highlightColor: Color? = null
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "shimmer"
        properties["enabled"] = enabled
    }
) {
    val reduceMotion = FieldMindMotion.isReduceMotion()
    val animConfig = LocalAnimationConfig.current
    val shouldAnimate = enabled && !reduceMotion

    val transition = rememberInfiniteTransition(label = "shimmer")
    val shimmerProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(animConfig.shimmerSpeedMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerProgress"
    )

    val colorBase = baseColor ?: MaterialTheme.colorScheme.surfaceVariant
    val colorHighlight = highlightColor ?: MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    if (shouldAnimate) {
        this.drawWithCache {
            val gradientWidth = size.width * 1.5f
            onDrawWithContent {
                drawContent()
                val offsetX = (shimmerProgress * (size.width + gradientWidth)) - gradientWidth
                val brush = Brush.linearGradient(
                    0f to colorBase,
                    0.5f to colorHighlight,
                    1f to colorBase,
                    start = Offset(offsetX, 0f),
                    end = Offset(offsetX + gradientWidth, 0f)
                )
                drawRect(brush = brush, alpha = 0.6f)
            }
        }
    } else {
        this
    }
}

// ── Pulse / Breathe Animation ──

/**
 * A [Modifier] that applies a gentle pulsing/breathing scale animation to a
 * composable. Great for attention badges, recording indicators, or live
 * status dots.
 *
 * @param enabled  Whether the pulse is active.
 * @param minScale Minimum scale during the pulse.
 * @param maxScale Maximum scale during the pulse.
 */
fun Modifier.pulse(
    enabled: Boolean = true,
    minScale: Float = 0.95f,
    maxScale: Float = 1.05f
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "pulse"
        properties["enabled"] = enabled
        properties["minScale"] = minScale
        properties["maxScale"] = maxScale
    }
) {
    val reduceMotion = FieldMindMotion.isReduceMotion()
    val animConfig = LocalAnimationConfig.current
    val shouldAnimate = enabled && !reduceMotion

    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(animConfig.pulseDurationMs / 2, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    if (shouldAnimate) {
        this.graphicsLayer {
            scaleX = scale
            scaleY = scale
            transformOrigin = TransformOrigin.Center
        }
    } else {
        this
    }
}

// ── Page Flip / 3D Card Rotation ──

/**
 * A [Modifier] that applies a 3D page-flip rotation around the Y axis.
 * Useful for card flip reveals or dramatic transitions.
 *
 * @param progress  0f = front face, 1f = back face (180 degrees).
 * @param enabled   Whether to apply the flip transform.
 */
fun Modifier.pageFlip(
    progress: Float,
    enabled: Boolean = true
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "pageFlip"
        properties["progress"] = progress
        properties["enabled"] = enabled
    }
) {
    val reduceMotion = FieldMindMotion.isReduceMotion()
    val animConfig = LocalAnimationConfig.current
    val shouldAnimate = enabled && !reduceMotion

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = if (shouldAnimate) {
            spring(dampingRatio = animConfig.entranceDampingRatio, stiffness = animConfig.entranceStiffness)
        } else {
            spring(stiffness = Spring.StiffnessMedium)
        },
        label = "pageFlip"
    )

    if (shouldAnimate) {
        this.graphicsLayer {
            rotationY = animatedProgress * 180f
            cameraDistance = 36f
            transformOrigin = TransformOrigin.Center
        }
    } else {
        this
    }
}

// ── Confetti / Particle Celebration Overlay ──

/**
 * Renders a burst of confetti particles over the full screen. Trigger by
 * toggling [trigger] to a new value. Particles respect reduce-motion and
 * the global animation toggle.
 *
 * @param trigger      Change this value to fire a new burst.
 * @param particleCount  Number of particles in the burst.
 * @param colors       Optional list of colors for the particles.
 */
@Composable
fun ConfettiOverlay(
    trigger: Any,
    modifier: Modifier = Modifier,
    particleCount: Int = 60,
    colors: List<Color> = emptyList()
) {
    val reduceMotion = FieldMindMotion.isReduceMotion()
    val animConfig = LocalAnimationConfig.current
    if (reduceMotion || !animConfig.confettiEnabled) return

    val defaultColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        FieldMindTheme.colors.positive,
        FieldMindTheme.colors.observation,
        FieldMindTheme.colors.flashcard
    )
    val palette = colors.ifEmpty { defaultColors }
    val density = LocalDensity.current

    data class Particle(
        val color: Color,
        val startX: Float,
        val startY: Float,
        val angle: Float,
        val velocity: Float,
        val size: Float,
        val rotation: Float,
        val rotationSpeed: Float
    )

    var particles by remember { mutableStateOf<List<Particle>>(emptyList()) }
    var burstKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(trigger) {
        burstKey++
        val newParticles = List(particleCount) {
            Particle(
                color = palette.random(),
                startX = 0f,
                startY = 0f,
                angle = Random.nextDouble(0.0, 2 * PI).toFloat(),
                velocity = Random.nextDouble(200.0, 600.0).toFloat(),
                size = Random.nextDouble(4.0, 12.0).toFloat(),
                rotation = Random.nextDouble(0.0, 360.0).toFloat(),
                rotationSpeed = Random.nextDouble(-180.0, 180.0).toFloat()
            )
        }
        particles = newParticles
    }

    if (particles.isEmpty()) return

    Box(modifier = modifier.fillMaxSize()) {
        particles.forEachIndexed { index, particle ->
            val anim = remember(burstKey, index) { Animatable(0f) }
            LaunchedEffect(burstKey) {
                anim.animateTo(1f, animationSpec = tween(1200, easing = FastOutSlowInEasing))
            }
            val progress = anim.value
            val x = with(density) { particle.startX + cos(particle.angle) * particle.velocity * progress }.dp
            val y = with(density) { particle.startY + sin(particle.angle) * particle.velocity * progress + 200f * progress * progress }.dp
            val rotation = particle.rotation + particle.rotationSpeed * progress
            val alpha = 1f - progress

            Box(
                modifier = Modifier
                    .offset(x = x, y = y)
                    .size(with(density) { particle.size }.dp)
                    .graphicsLayer {
                        rotationZ = rotation
                        this.alpha = alpha
                    }
                    .background(particle.color, RoundedCornerShape(2.dp))
            )
        }
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

    // ── BackHandler for hardware button press (innermost wins priority) ──
    // This replaces PredictiveBackHandler which caused double-fire issues
    // during navigation transitions when two SwipeBackHost instances
    // (outgoing and incoming screen) were simultaneously active.
    // The manual detectDragGestures below handles swipe gesture animations.
    BackHandler(enabled = !isImeVisible) {
        onBack()
    }

    // ── Unified progress computation ──
    // Only manual drag drives animX (PredictiveBackHandler was removed to
    // prevent nested SwipeBackHost conflicts during nav transitions).
    val animConfig = LocalAnimationConfig.current
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
                    if (!reduceMotion) {
                        // Use isImeVisible as key so the gesture handler restarts when
                        // keyboard state changes, preventing stale handler/removal cycles.
                        Modifier.pointerInput(isImeVisible) {
                            if (isImeVisible) return@pointerInput
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                val isAtLeftEdge = down.position.x <= FieldMindMotion.swipeEdgeWidthDp
                                val isAtTopEdge = down.position.y <= FieldMindMotion.swipeEdgeHeightDp

                                // NOT near any edge — release the gesture and let children
                                // (e.g. LazyColumn in settings) process it normally.
                                if (!isAtLeftEdge && !isAtTopEdge) {
                                    return@awaitEachGesture
                                }

                                // Near edge — consume the down event and handle drag
                                down.consume()
                                activeDirection = if (isAtLeftEdge) SwipeDirection.Horizontal else SwipeDirection.Vertical

                                try {
                                    var pointerUp = false
                                    var lastPosition = down.position
                                    do {
                                        val event = awaitPointerEvent()
                                        val change = event.changes.firstOrNull() ?: break
                                        if (change.isConsumed || !change.pressed) {
                                            pointerUp = !change.pressed
                                            break
                                        }
                                        change.consume()
                                        val deltaX = change.position.x - lastPosition.x
                                        val deltaY = change.position.y - lastPosition.y
                                        lastPosition = change.position
                                        when (activeDirection) {
                                            SwipeDirection.Horizontal -> {
                                                val newX = (animX.value + deltaX).coerceAtLeast(0f)
                                                scope.launch { animX.snapTo(newX) }
                                            }
                                            SwipeDirection.Vertical -> {
                                                val newY = (animY.value + deltaY).coerceAtLeast(0f)
                                                scope.launch { animY.snapTo(newY) }
                                            }
                                            null -> {}
                                        }
                                    } while (true)

                                    // ── onDragEnd equivalent ──
                                    // Capture direction BEFORE clearing it
                                    val endDirection = activeDirection
                                    activeDirection = null
                                    val maxVal = when (endDirection) {
                                        SwipeDirection.Horizontal -> contentWidth
                                        SwipeDirection.Vertical -> contentHeight
                                        null -> Float.MAX_VALUE
                                    }
                                    val currentVal = when (endDirection) {
                                        SwipeDirection.Horizontal -> animX.value
                                        SwipeDirection.Vertical -> animY.value
                                        null -> 0f
                                    }
                                    if (pointerUp && currentVal > maxVal * animConfig.swipeThreshold) {
                                        haptics.confirm()
                                        scope.launch {
                                            animX.snapTo(0f)
                                            animY.snapTo(0f)
                                            onBack()
                                        }
                                    } else {
                                        scope.launch {
                                            animX.animateTo(0f, animConfig.swipeBackSpring())
                                            animY.animateTo(0f, animConfig.swipeBackSpring())
                                        }
                                    }
                                } catch (_: CancellationException) {
                                    // ── onDragCancel equivalent ──
                                    activeDirection = null
                                    scope.launch {
                                        animX.animateTo(0f, animConfig.swipeBackSpring())
                                        animY.animateTo(0f, animConfig.swipeBackSpring())
                                    }
                                }
                            }
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
                        .clip(MaterialTheme.shapes.medium)
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
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(FieldMindIcons.ChevronDown, "Swipe down to dismiss", size = 22.dp, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
