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
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import androidx.graphics.shapes.circle
import androidx.graphics.shapes.rectangle
import androidx.graphics.shapes.star
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
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
    // ── Organic Motion spring physics (nature-inspired) ──
    // DewDrop: gentle ease with micro-overshoot, like water settling on a leaf
    val dampingRatio: Float = 0.72f,
    val stiffness: Float = 280f,
    // ── Predictive back (Android 13+ system back gesture) ──
    val predictiveBackEnabled: Boolean = true,
    val predictiveBackScaleMin: Float = 0.88f,
    // Swipe-back spring
    val swipeBackDampingRatio: Float = 0.75f,
    val swipeBackStiffness: Float = 300f,
    // Swipe behavior
    val swipeThreshold: Float = 0.20f,
    val swipeScaleFactor: Float = 0.92f,
    // Transition slide stiffness
    val slideStiffness: Float = 350f,
    // ── Side swipe (item-level swipe actions) ──
    val sideSwipeEnabled: Boolean = true,
    val sideSwipeThreshold: Float = 0.25f,
    val sideSwipeDampingRatio: Float = 0.62f,
    val sideSwipeStiffness: Float = 380f,
    val sideSwipeMaxRevealDp: Float = 80f,
    // ── Shape morphing ──
    val morphEnabled: Boolean = true,
    val morphDampingRatio: Float = 0.78f,
    val morphStiffness: Float = 200f,
    // ── Duration tokens ──
    val morphDurationMs: Int = 380,
    val shimmerSpeedMs: Int = 1200,
    val pulseDurationMs: Int = 1500,
    // ── Feature toggles ──
    val pageFlipEnabled: Boolean = true
) {
    companion object {
        val DEFAULT = AnimationConfig()
    }

    fun spring() = spring<Float>(dampingRatio = dampingRatio, stiffness = stiffness)
    fun swipeBackSpring() = spring<Float>(dampingRatio = swipeBackDampingRatio, stiffness = swipeBackStiffness)
    fun slideSpring() = spring<IntOffset>(dampingRatio = 0.88f, stiffness = slideStiffness)
    fun bouncySpring() = spring<Float>(dampingRatio = 0.45f, stiffness = 160f)
    fun morphSpring() = spring<Float>(dampingRatio = morphDampingRatio, stiffness = morphStiffness)
    fun sideSwipeSpring() = spring<Float>(dampingRatio = sideSwipeDampingRatio, stiffness = sideSwipeStiffness)
}

val LocalAnimationConfig = compositionLocalOf { AnimationConfig.DEFAULT }

/** CompositionLocal controlling whether animations are globally enabled. */
val LocalAnimationsEnabled = compositionLocalOf { true }

/**
 * Material expressive motion specifications for FieldMind.
 * All spring specs now read from [LocalAnimationConfig] for runtime tuning.
 * Simplified to fewer, better-named springs with Telegram-inspired defaults.
 */
object FieldMindMotion {

    // ── Organic Motion — Nature-Inspired Springs ──
    // Each spring models a natural phenomenon for intuitive, elegant motion.

    /** DewDrop — gentle rise-and-settle. Primary entrance spring. */
    val expressiveSpring
        @Composable get() = spring<Float>(
            dampingRatio = LocalAnimationConfig.current.dampingRatio,
            stiffness = LocalAnimationConfig.current.stiffness
        )

    /** LeafSway — soft oscillation for subtle layout changes. */
    val expressiveFloat
        @Composable get() = spring<Float>(
            dampingRatio = LocalAnimationConfig.current.dampingRatio + 0.04f,
            stiffness = (LocalAnimationConfig.current.stiffness * 0.72f).coerceAtLeast(60f)
        )

    /** StoneDrop — gradual settle, no bounce. For heavy transitions. */
    val expressiveSoft
        @Composable get() = spring<Float>(
            dampingRatio = 0.84f,
            stiffness = (LocalAnimationConfig.current.stiffness * 0.48f).coerceAtLeast(40f)
        )

    /** FireflyFlicker — quick, responsive micro-bounce for press feedback. */
    val expressiveSnap
        @Composable get() = spring<Float>(
            dampingRatio = 0.68f,
            stiffness = (LocalAnimationConfig.current.stiffness * 0.90f).coerceAtLeast(70f)
        )

    // ── Bouncy Springs ──
    fun bouncySpring(dampingRatio: Float = 0.48f, stiffness: Float = 170f) =
        spring<Float>(dampingRatio = dampingRatio, stiffness = stiffness)

    val bouncyEntrance
        @Composable get() = bouncySpring(dampingRatio = 0.42f, stiffness = 150f)

    val bouncyPress
        @Composable get() = bouncySpring(dampingRatio = 0.52f, stiffness = 200f)

    // ── Standard Springs ──

    /** WindDrift — smooth directional ease for layout shifts. */
    val layoutSpring
        @Composable get() = spring<Float>(dampingRatio = 0.85f, stiffness = (LocalAnimationConfig.current.stiffness * 0.55f).coerceAtLeast(60f))

    /** RootSnap — decisive, grounded press response. */
    val pressSpring
        @Composable get() = spring<Float>(dampingRatio = 0.80f, stiffness = (LocalAnimationConfig.current.stiffness * 0.88f).coerceAtLeast(70f))

    /** PebbleSettle — firm, satisfying confirmation settle. */
    val confirmSpring
        @Composable get() = spring<Float>(dampingRatio = 0.88f, stiffness = (LocalAnimationConfig.current.stiffness * 0.52f).coerceAtLeast(60f))

    // ── Navigation Springs ──

    val swipeBackSpring
        @Composable get() = spring<Float>(dampingRatio = LocalAnimationConfig.current.swipeBackDampingRatio, stiffness = LocalAnimationConfig.current.swipeBackStiffness)

    val sharedElementSpring
        @Composable get() = spring<Float>(dampingRatio = LocalAnimationConfig.current.dampingRatio, stiffness = (LocalAnimationConfig.current.stiffness * 0.6f).coerceAtLeast(80f))

    val slideSpring
        @Composable get() = spring<Float>(dampingRatio = 0.86f, stiffness = (LocalAnimationConfig.current.slideStiffness * 0.45f).coerceAtLeast(80f))

    /** MistDrift — gentle fade transition between screens. */
    val fadeThroughSpring
        @Composable get() = spring<Float>(dampingRatio = 0.88f, stiffness = (LocalAnimationConfig.current.stiffness * 0.55f).coerceAtLeast(80f))

    val slideOffsetSpring
        @Composable get() = spring<IntOffset>(dampingRatio = 0.86f, stiffness = LocalAnimationConfig.current.slideStiffness)

    // ── Shape Morphing ──

    val morphSpring
        @Composable get() = spring<Float>(dampingRatio = 0.84f, stiffness = (LocalAnimationConfig.current.stiffness * 0.58f).coerceAtLeast(70f))

    val cornerSpring
        @Composable get() = spring<Float>(dampingRatio = 0.84f, stiffness = (LocalAnimationConfig.current.stiffness * 0.68f).coerceAtLeast(80f))

    // ── Duration Tokens (ms) ──

    const val durationMicro = 80
    const val durationSubtle = 150
    const val durationStandard = 300
    const val durationEmphasized = 400
    const val durationExpressive = 600
    const val countUpMs = 500

    // ── Convenience Tween ──

    val fadeTween = tween<Float>(durationMillis = durationSubtle)
    val pressScaleTween = tween<Float>(durationMillis = durationMicro)

    // ── Predictive Back Constants ──

    const val predictiveBackScaleMin = 0.88f
    const val predictiveBackScrimAlpha = 0.20f

    // ── Swipe-back Constants ──

    const val swipeEdgeWidthDp = 36f
    const val swipeEdgeHeightDp = 36f
    const val swipeThreshold = 0.18f
    const val swipeScaleFactor = 0.90f
    const val swipeScrimAlpha = 0.28f
    const val swipeShadowElevationDp = 24f
    const val swipeCornerRadiusDp = 36f
    const val swipeBaseCornerRadiusDp = 20f
    // Elastic overshoot for snap-back (Telegram-style)
    const val swipeOvershootDamping = 0.82f
    const val swipeOvershootStiffness = 220f

    // ── Utility ──

    @Composable
    fun entranceSpec(emphasis: Emphasis = Emphasis.Standard): AnimationSpec<Float> = when (emphasis) {
        Emphasis.Expressive -> expressiveSpring
        Emphasis.Emphasized -> expressiveFloat
        Emphasis.Standard -> expressiveSoft
        Emphasis.Snap -> expressiveSnap
        Emphasis.Bouncy -> bouncyEntrance
    }

    enum class Emphasis { Expressive, Emphasized, Standard, Snap, Bouncy }

    @Composable
    fun isReduceMotion(): Boolean {
        if (LocalInspectionMode.current) return false
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
                        // Wait for up or cancellation (do NOT consume — let clickable/scrollable work)
                        var upEvent: PointerEvent
                        do {
                            upEvent = awaitPointerEvent()
                        } while (upEvent.changes.all { it.pressed })
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
                        // Wait for up or cancellation (do NOT consume — let clickable/scrollable work)
                        var upEvent: PointerEvent
                        do {
                            upEvent = awaitPointerEvent()
                        } while (upEvent.changes.all { it.pressed })
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
                        // Wait for up or cancellation (do NOT consume — let clickable/scrollable work)
                        var upEvent: PointerEvent
                        do {
                            upEvent = awaitPointerEvent()
                        } while (upEvent.changes.all { it.pressed })
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

// ── Entrance animations removed: staggeredEntrance, bouncyEntrance, sideReveal ──
// All items render instantly together. No staggered one-by-one reveals.

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
            spring(dampingRatio = 0.72f, stiffness = 280f)
        } else {
            spring(stiffness = Spring.StiffnessMedium)
        },
        label = "morphShape"
    )

    this.clip(RoundedCornerShape(animatedRadius))
}

// ══════════════════════════════════════════════════════════════════════
//  Polygon Morph Shape — graphics-shapes powered morphing between
//  arbitrary RoundedPolygons (circle ↔ rounded rect, etc.)
// ══════════════════════════════════════════════════════════════════════

/**
 * A [Shape] implementation powered by [androidx.graphics.shapes.Morph]
 * that interpolates between two [RoundedPolygon] instances based on a
 * [progress] value (0f = start polygon, 1f = end polygon).
 *
 * Use with [Modifier.clip] + [Modifier.background] to clip any composable
 * (image, text, gradient) to a smoothly morphing shape.
 *
 * @param morph   Pre-computed [Morph] between two [RoundedPolygon]s.
 * @param progress  0f → start shape, 1f → end shape.
 */
class MorphPolygonShape(
    private val morph: Morph,
    private val progress: Float
) : Shape {
    private val matrix = Matrix()

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = morph.toPath(progress).asComposePath()
        // Scale to fill the available size (polygons are unit-centered)
        val scaleX = size.width / 2f
        val scaleY = size.height / 2f
        matrix.reset()
        matrix.scale(scaleX, scaleY)
        matrix.translate(size.width / 2f, size.height / 2f)
        path.transform(matrix)
        return Outline.Generic(path)
    }
}

/**
 * Predefined [RoundedPolygon] shapes available for morphing.
 * Each factory creates a unit-sized polygon suitable for use with [Morph].
 */
object MorphPolygons {
    /** Smooth circle (64 vertices for high-quality rendering). */
    fun circle(radius: Float = 1f) = RoundedPolygon.circle(
        numVertices = 64, radius = radius, centerX = 0f, centerY = 0f
    )

    /** Standard rounded rectangle with uniform corner rounding. */
    fun roundedRect(
        width: Float = 1f,
        height: Float = 1f,
        cornerRadius: Float = 0.16f
    ) = RoundedPolygon.rectangle(
        width = width, height = height,
        rounding = CornerRounding(cornerRadius),
        centerX = 0f, centerY = 0f
    )

    /** Rounded rectangle with different corner rounding per corner (superellipse-like). */
    fun pillRect(
        width: Float = 1f,
        height: Float = 0.6f
    ) = RoundedPolygon.rectangle(
        width = width, height = height,
        rounding = CornerRounding(height / 2f),
        centerX = 0f, centerY = 0f
    )

    /** Hexagon shape (6 vertices). */
    fun hexagon(radius: Float = 1f) = RoundedPolygon(
        numVertices = 6, radius = radius,
        rounding = CornerRounding(0.08f),
        centerX = 0f, centerY = 0f
    )

    /** Diamond/rhombus shape (4 vertices, rotated 45°). */
    fun diamond(radius: Float = 1f) = RoundedPolygon(
        numVertices = 4, radius = radius,
        rounding = CornerRounding(0.06f),
        centerX = 0f, centerY = 0f
    )

    /** Star-like shape (5 points with inner/outer radius for star effect). */
    fun star(
        outerRadius: Float = 1f,
        innerRadius: Float = 0.4f
    ) = RoundedPolygon.star(
        numVerticesPerRadius = 5,
        radius = outerRadius,
        innerRadius = innerRadius,
        rounding = CornerRounding(0.08f),
        innerRounding = CornerRounding(0.04f),
        centerX = 0f, centerY = 0f
    )

    /** Triangle (3 vertices). */
    fun triangle(radius: Float = 1f) = RoundedPolygon(
        numVertices = 3, radius = radius,
        rounding = CornerRounding(0.06f),
        centerX = 0f, centerY = 0f
    )
}

/**
 * A [Modifier] that clips the composable using a morphing polygon shape,
 * animating between [startPolygon] and [endPolygon] driven by [progress].
 *
 * Uses [androidx.graphics.shapes.Morph] for smooth, hardware-accelerated
 * shape interpolation. Combine with [animateFloatAsState] for smooth progress
 * animation, or use [Modifier.morphPolygon] with [trigger] for declarative
 * toggling.
 *
 * @param startPolygon  The shape at progress=0f.
 * @param endPolygon    The shape at progress=1f.
 * @param progress      0f → [startPolygon], 1f → [endPolygon].
 */
fun Modifier.morphPolygon(
    startPolygon: RoundedPolygon,
    endPolygon: RoundedPolygon,
    progress: Float
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "morphPolygon"
        properties["progress"] = progress
    }
) {
    val morph = remember(startPolygon, endPolygon) {
        Morph(startPolygon, endPolygon)
    }
    this.clip(MorphPolygonShape(morph, progress.coerceIn(0f, 1f)))
}

/**
 * A [Modifier] that morphs between two [RoundedPolygon] shapes triggered by
 * a togglable boolean [trigger]. When [trigger] is true, the shape animates
 * from [startPolygon] to [endPolygon] (and back when false).
 *
 * Uses the [AnimationConfig.morphSpring] for smooth, tunable spring physics.
 *
 * @param trigger       Toggle state (true=end shape, false=start shape).
 * @param startPolygon  The shape at trigger=false.
 * @param endPolygon    The shape at trigger=true.
 * @param enabled       Whether the morph animation is enabled.
 */
@Composable
fun Modifier.animateMorphPolygon(
    trigger: Boolean,
    startPolygon: RoundedPolygon,
    endPolygon: RoundedPolygon,
    enabled: Boolean = true
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "animateMorphPolygon"
        properties["trigger"] = trigger
        properties["enabled"] = enabled
    }
) {
    val reduceMotion = FieldMindMotion.isReduceMotion()
    val animConfig = LocalAnimationConfig.current
    val shouldAnimate = enabled && !reduceMotion && animConfig.morphEnabled

    val progress by animateFloatAsState(
        targetValue = if (trigger) 1f else 0f,
        animationSpec = if (shouldAnimate) {
            spring(dampingRatio = 0.78f, stiffness = 200f)
        } else {
            spring(stiffness = Spring.StiffnessMedium)
        },
        label = "animateMorphPolygon"
    )

    this.morphPolygon(startPolygon, endPolygon, progress)
}

// ══════════════════════════════════════════════════════════════════════
//  MorphTransition Composable — full stateful morph between shapes
// ══════════════════════════════════════════════════════════════════════

/**
 * A composable that wraps any content in a container whose shape smoothly
 * morphs between [startPolygon] and [endPolygon] with a spring animation.
 *
 * Call [onTrigger] (e.g., `onClick`) to toggle between the two shapes.
 * The backing shape is pre-computed with [Morph] for efficient rendering.
 *
 * Provides a beautiful, interactive shape-transition effect for cards,
 * buttons, avatars, and any content — especially striking with gradients
 * or images as children.
 *
 * @param trigger       External toggle state.
 * @param onTrigger     Called when the user wants to toggle the shape.
 * @param startPolygon  The shape at trigger=false.
 * @param endPolygon    The shape at trigger=true.
 * @param modifier      Modifier applied to the outer container.
 * @param enabled       Whether morph animation is active.
 * @param content       The composable content inside the morphing shape.
 */
@Composable
fun MorphTransition(
    trigger: Boolean,
    onTrigger: () -> Unit,
    startPolygon: RoundedPolygon = MorphPolygons.roundedRect(),
    endPolygon: RoundedPolygon = MorphPolygons.circle(),
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val reduceMotion = FieldMindMotion.isReduceMotion()
    val animConfig = LocalAnimationConfig.current
    val shouldAnimate = enabled && !reduceMotion && animConfig.morphEnabled

    val progress by animateFloatAsState(
        targetValue = if (trigger) 1f else 0f,
        animationSpec = if (shouldAnimate) {
            spring(dampingRatio = 0.78f, stiffness = 200f)
        } else {
            spring(stiffness = Spring.StiffnessMedium)
        },
        label = "MorphTransition"
    )

    val shapeMorph = remember(startPolygon, endPolygon) {
        Morph(startPolygon, endPolygon)
    }

    Box(
        modifier = modifier
            .clip(MorphPolygonShape(shapeMorph, progress))
            .then(
                if (shouldAnimate) {
                    Modifier.clickable(
                        indication = null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    ) { onTrigger() }
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
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
            spring(dampingRatio = 0.72f, stiffness = 280f)
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

// ── ConfettiOverlay removed — particles were jarring, not elegant ──

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
    swipeFromCenter: Boolean = false,
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
    BackHandler(enabled = !isImeVisible) {
        onBack()
    }

    // ── PredictiveBackHandler: Android 13+ system back gesture ──
    // Drives the same peek/scrim/scale transforms as manual edge-swipe,
    // giving a unified iOS/Telegram-style peek experience for both
    // the system back gesture and manual drag-from-edge.
    // Disabled when keyboard is visible or predictive back is turned off.
    val animConfig = LocalAnimationConfig.current
    val predictiveBackEnabled = animConfig.predictiveBackEnabled && !isImeVisible
    if (predictiveBackEnabled && !reduceMotion) {
        PredictiveBackHandler(enabled = true) { backEventFlow ->
            try {
                backEventFlow.collect { event ->
                    activeDirection = SwipeDirection.Horizontal
                    animX.snapTo(event.progress * contentWidth)
                }
                // Flow completed → gesture committed
                haptics.confirm()
                activeDirection = null
                onBack()
            } catch (_: CancellationException) {
                // Gesture cancelled → elastic spring back to origin
                activeDirection = null
                animX.animateTo(0f, spring(dampingRatio = FieldMindMotion.swipeOvershootDamping, stiffness = FieldMindMotion.swipeOvershootStiffness))
                animY.animateTo(0f, spring(dampingRatio = FieldMindMotion.swipeOvershootDamping, stiffness = FieldMindMotion.swipeOvershootStiffness))
            }
        }
    }

    // ── Unified progress computation ──
    // Progress is driven by both manual edge-swipe (detectDragGestures)
    // and the PredictiveBackHandler above, both updating animX/animY.
    val horizontalProgress = (abs(animX.value) / contentWidth).coerceIn(0f, 1f)
    val verticalProgress = (abs(animY.value) / contentHeight).coerceIn(0f, 1f)
    val (progress, isHorizontalPeek) = when (activeDirection) {
        SwipeDirection.Horizontal -> Pair(horizontalProgress, true)
        SwipeDirection.Vertical -> Pair(verticalProgress, false)
        null -> Pair(horizontalProgress.coerceAtLeast(verticalProgress), horizontalProgress >= verticalProgress)
    }
    val scrimAlpha = progress * (if (predictiveBackEnabled && activeDirection == SwipeDirection.Horizontal) FieldMindMotion.predictiveBackScrimAlpha else FieldMindMotion.swipeScrimAlpha)
    val contentScale = 1f - progress * (1f - if (predictiveBackEnabled && activeDirection == SwipeDirection.Horizontal) animConfig.predictiveBackScaleMin else animConfig.swipeScaleFactor)
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
                        shape = RoundedCornerShape(topEnd = 40.dp, bottomEnd = 44.dp),
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
                                // requireUnconsumed = true: only handle touches NOT consumed
                                // by child composables (e.g., clickable cards, buttons, sliders).
                                // This fixes the settings navigation bug where taps on cards
                                // produced visual feedback but didn't navigate.
                                val down = awaitFirstDown(requireUnconsumed = true)
                                val isAtLeftEdge = down.position.x <= FieldMindMotion.swipeEdgeWidthDp
                                val isAtTopEdge = down.position.y <= FieldMindMotion.swipeEdgeHeightDp

                                // When swipeFromCenter is enabled, any horizontal drag triggers back.
                                // When false (default), only edge swipes trigger back.
                                if (!swipeFromCenter) {
                                    if (!isAtLeftEdge && !isAtTopEdge) {
                                        return@awaitEachGesture
                                    }
                                    // Near edge — consume the down event and handle drag
                                    down.consume()
                                    activeDirection = if (isAtLeftEdge) SwipeDirection.Horizontal else SwipeDirection.Vertical
                                } else {
                                    // Center swipe: only horizontal, ignore vertical
                                    down.consume()
                                    activeDirection = SwipeDirection.Horizontal
                                }

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
                                            animX.animateTo(0f, spring(dampingRatio = FieldMindMotion.swipeOvershootDamping, stiffness = FieldMindMotion.swipeOvershootStiffness))
                                            animY.animateTo(0f, spring(dampingRatio = FieldMindMotion.swipeOvershootDamping, stiffness = FieldMindMotion.swipeOvershootStiffness))
                                        }
                                    }
                                } catch (_: CancellationException) {
                                    // ── onDragCancel equivalent ──
                                    activeDirection = null
                                    scope.launch {
                                        animX.animateTo(0f, spring(dampingRatio = FieldMindMotion.swipeOvershootDamping, stiffness = FieldMindMotion.swipeOvershootStiffness))
                                        animY.animateTo(0f, spring(dampingRatio = FieldMindMotion.swipeOvershootDamping, stiffness = FieldMindMotion.swipeOvershootStiffness))
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
            if (isHorizontalPeek && animX.value > contentWidth * 0.10f) {
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
            if (!isHorizontalPeek && animY.value > contentHeight * 0.10f) {
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

// ══════════════════════════════════════════════════════════════════════
//  SwipeActionHost — Telegram-style item-level swipe actions
// ══════════════════════════════════════════════════════════════════════

/**
 * Result of a swipe action — which side was activated (if any).
 */
enum class SwipeActionResult { Left, Right, None }

/**
 * A composable that wraps a list item or card and provides Telegram-style
 * horizontal swipe-to-reveal actions. Drag from anywhere on the item to
 * reveal action buttons behind.
 *
 * Uses spring-animated snap-to-actions: swipe past the threshold and release
 * to snap to the revealed state; swipe less and it springs back to neutral.
 *
 * @param onSwipe        Called with [SwipeActionResult] AFTER the snap animation completes.
 * @param resetTrigger   Change this value to programmatically snap back to neutral.
 * @param modifier       Modifier for the outer container.
 * @param leftActions    Composable rendered behind the content on the left side.
 * @param rightActions   Composable rendered behind the content on the right side.
 * @param enabled        Whether swipe actions are enabled.
 * @param content        The main item content.
 */
@Composable
fun SwipeActionHost(
    onSwipe: (SwipeActionResult) -> Unit,
    resetTrigger: Any? = null,
    modifier: Modifier = Modifier,
    leftActions: @Composable (androidx.compose.foundation.layout.RowScope.() -> Unit)? = null,
    rightActions: @Composable (androidx.compose.foundation.layout.RowScope.() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val reduceMotion = FieldMindMotion.isReduceMotion()
    val animConfig = LocalAnimationConfig.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val shouldAnimate = enabled && !reduceMotion && animConfig.sideSwipeEnabled

    val offsetX = remember { Animatable(0f) }

    // Compute the max reveal distance in pixels
    val maxRevealPx = with(density) { animConfig.sideSwipeMaxRevealDp.dp.toPx() }

    // ── Reset to neutral when resetTrigger changes ──
    LaunchedEffect(resetTrigger) {
        if (resetTrigger != null && offsetX.value != 0f) {
            offsetX.animateTo(0f,            spring(dampingRatio = 0.62f, stiffness = 380f))
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        // ── Layer 1: Action buttons (behind content) ──
        val leftAlpha = (offsetX.value / maxRevealPx).coerceIn(0f, 1f)
        val rightAlpha = (-offsetX.value / maxRevealPx).coerceIn(0f, 1f)

        Row(modifier = Modifier.fillMaxSize()) {
            // Left action area
            if (leftActions != null) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .graphicsLayer { alpha = leftAlpha },
                    content = leftActions
                )
            }
            Spacer(Modifier.weight(1f))
            // Right action area
            if (rightActions != null) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .graphicsLayer { alpha = rightAlpha },
                    content = rightActions
                )
            }
        }

        // ── Layer 2: Content (slides to reveal actions) ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationX = offsetX.value
                }
                .then(
                    if (shouldAnimate) {
                        Modifier.pointerInput(Unit) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                down.consume()

                                try {
                                    // Track cumulative drag offset locally to avoid
                                    // race conditions from reading stale offsetX.value
                                    var dragOffset = offsetX.value
                                    var lastPosition = down.position
                                    var pointerUp = false
                                    do {
                                        val event = awaitPointerEvent()
                                        val change = event.changes.firstOrNull() ?: break
                                        if (change.isConsumed || !change.pressed) {
                                            pointerUp = !change.pressed
                                            break
                                        }
                                        change.consume()
                                        val deltaX = change.position.x - lastPosition.x
                                        lastPosition = change.position
                                        dragOffset = (dragOffset + deltaX)
                                            .coerceIn(-maxRevealPx, maxRevealPx)
                                        scope.launch { offsetX.snapTo(dragOffset) }
                                    } while (true)

                                    // ── onDragEnd: snap to nearest anchor ──
                                    val thresholdPx = maxRevealPx * animConfig.sideSwipeThreshold
                                    when {
                                        pointerUp && dragOffset > thresholdPx -> {
                                            scope.launch {
                                                offsetX.animateTo(maxRevealPx,            spring(dampingRatio = 0.62f, stiffness = 380f))
                                                onSwipe(SwipeActionResult.Left)
                                            }
                                        }
                                        pointerUp && dragOffset < -thresholdPx -> {
                                            scope.launch {
                                                offsetX.animateTo(-maxRevealPx,            spring(dampingRatio = 0.62f, stiffness = 380f))
                                                onSwipe(SwipeActionResult.Right)
                                            }
                                        }
                                        else -> {
                                            scope.launch {
                                                offsetX.animateTo(0f,            spring(dampingRatio = 0.62f, stiffness = 380f))
                                            }
                                        }
                                    }
                                } catch (_: CancellationException) {
                                    scope.launch {
                                        offsetX.animateTo(0f,            spring(dampingRatio = 0.62f, stiffness = 380f))
                                    }
                                }
                            }
                        }
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.CenterStart
        ) {
            content()
        }
    }
}

// ── Convenience: side swipe action buttons ──

/**
 * A styled action button for use inside [SwipeActionHost]'s [leftActions]
 * or [rightActions]. Renders an icon slot with a colored background, sized to
 * the swipe reveal distance.
 */
@Composable
fun SwipeActionButton(
    icon: @Composable () -> Unit,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animConfig = LocalAnimationConfig.current
    Surface(
        onClick = onClick,
        modifier = modifier
            .width(animConfig.sideSwipeMaxRevealDp.dp)
            .fillMaxHeight(),
        color = backgroundColor,
        shape = RoundedCornerShape(0)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            icon()
        }
    }
}
