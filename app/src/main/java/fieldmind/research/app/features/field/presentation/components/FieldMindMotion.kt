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

/**
 * Material expressive motion specifications for FieldMind.
 */
object FieldMindMotion {

    // -- Expressive Springs (overshoot / bounce / elastic) --

    val expressiveSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val expressiveSoft = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    val expressiveElastic = spring<Float>(
        dampingRatio = 0.3f,
        stiffness = Spring.StiffnessMedium
    )

    val expressiveFloat = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    val expressiveSnap = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessHigh
    )

    val expressiveDramatic = spring<Float>(
        dampingRatio = 0.4f,
        stiffness = 400f
    )

    // -- Standard Springs (no overshoot) --

    val layoutSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow
    )

    val pressSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val confirmSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    // -- Navigation Springs --

    val swipeBackSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = 800f
    )

    val sharedElementSpring = spring<Float>(
        dampingRatio = 0.7f,
        stiffness = 600f
    )

    val slideSpring = spring<Float>(
        dampingRatio = 0.75f,
        stiffness = 700f
    )

    val fadeThroughSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    val slideOffsetSpring = spring<IntOffset>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    // -- Duration Tokens (ms) --

    const val durationMicro = 120
    const val durationSubtle = 200
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
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    val cornerSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
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
    const val swipeCornerRadiusDp = 22f
    const val swipeBaseCornerRadiusDp = 4f

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

// -- Swipe-back Gesture Host -- iOS-style with predictive peek --

private enum class SwipeDirection { Horizontal, Vertical }

/**
 * Categorizes the previous screen for its peek preview mock content.
 * Each type produces a different visual placeholder during the back gesture.
 */
enum class PeekScreenType {
    Settings, Detail, Tool, Creation, Generic
}

/**
 * Previous screen peek state for the navigation peek animation.
 * Passed from the navigation layer (e.g., NavHost) to show which
 * destination is behind the current screen during the back gesture.
 *
 * @param label Human-readable name of the previous destination
 * @param route Route string of the previous destination (for matching icons)
 * @param screenType Categorizes the screen for a type-appropriate peek preview
 */
data class PreviousScreenInfo(
    val label: String,
    val route: String = "",
    val screenType: PeekScreenType = PeekScreenType.Generic
)

// ── Screen-type-specific peek preview content ──

/**
 * Renders a mock preview of the previous screen's content during the back-gesture peek.
 * The mock style depends on [screenType], giving the user a visual hint of what
 * screen they're navigating back to without needing the real composable.
 */
@Composable
private fun PeekPreviewContent(
    screenType: PeekScreenType,
    label: String,
    accentColor: Color
) {
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface
    val outlineAlpha = 0.12f

    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (screenType) {
                PeekScreenType.Settings -> {
                    // ── Settings-style mock: labeled rows with toggles/chevrons ──
                    Text(
                        label,
                        style = MaterialTheme.typography.titleSmall,
                        color = onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    repeat(4) { idx ->
                        val rowColor = if (idx == 0) accentColor else surfaceVariant
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Icon placeholder
                                Box(
                                    Modifier.size(22.dp).background(
                                        rowColor.copy(alpha = outlineAlpha),
                                        RoundedCornerShape(6.dp)
                                    )
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Box(
                                        Modifier.width((80 + idx * 20).dp).height(8.dp).background(
                                            onSurface.copy(alpha = 0.10f),
                                            RoundedCornerShape(4.dp)
                                        )
                                    )
                                    Box(
                                        Modifier.width((40 + idx * 10).dp).height(6.dp).background(
                                            onSurface.copy(alpha = 0.06f),
                                            RoundedCornerShape(3.dp)
                                        )
                                    )
                                }
                            }
                            // Toggle / chevron placeholder
                            Box(
                                Modifier.size(16.dp).background(
                                    onSurface.copy(alpha = 0.08f),
                                    RoundedCornerShape(4.dp)
                                )
                            )
                        }
                        if (idx < 3) {
                            Spacer(
                                Modifier.fillMaxWidth().height(1.dp).background(
                                    onSurface.copy(alpha = 0.04f)
                                )
                            )
                        }
                    }
                }

                PeekScreenType.Detail -> {
                    // ── Detail-style mock: header + content cards ──
                    // Header area
                    Box(
                        Modifier.fillMaxWidth().height(80.dp).background(
                            accentColor.copy(alpha = 0.06f),
                            RoundedCornerShape(12.dp)
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                Modifier.size(28.dp).background(
                                    accentColor.copy(alpha = 0.15f),
                                    RoundedCornerShape(8.dp)
                                )
                            )
                            Spacer(Modifier.height(6.dp))
                            Box(
                                Modifier.width(80.dp).height(8.dp).background(
                                    onSurface.copy(alpha = 0.12f),
                                    RoundedCornerShape(4.dp)
                                )
                            )
                        }
                    }
                    // Content cards
                    repeat(3) { idx ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = surfaceVariant.copy(alpha = 0.3f)
                        ) {
                            Row(
                                Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    Modifier.size(36.dp).background(
                                        accentColor.copy(alpha = 0.08f * (3 - idx)),
                                        RoundedCornerShape(8.dp)
                                    )
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(
                                        Modifier.width((60 + idx * 30).dp).height(8.dp).background(
                                            onSurface.copy(alpha = 0.10f),
                                            RoundedCornerShape(4.dp)
                                        )
                                    )
                                    Box(
                                        Modifier.width((100 + idx * 20).dp).height(6.dp).background(
                                            onSurface.copy(alpha = 0.06f),
                                            RoundedCornerShape(3.dp)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                PeekScreenType.Tool -> {
                    // ── Tool-style mock: input field + controls ──
                    Text(
                        label,
                        style = MaterialTheme.typography.titleSmall,
                        color = onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    // Large value display
                    Box(
                        Modifier.fillMaxWidth().height(64.dp).background(
                            surfaceVariant.copy(alpha = 0.25f),
                            RoundedCornerShape(14.dp)
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            Modifier.width(48.dp).height(20.dp).background(
                                onSurface.copy(alpha = 0.08f),
                                RoundedCornerShape(6.dp)
                            )
                        )
                    }
                    // Control row
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(3) {
                            Box(
                                Modifier.weight(1f).height(36.dp).background(
                                    surfaceVariant.copy(alpha = 0.3f),
                                    RoundedCornerShape(10.dp)
                                )
                            )
                        }
                    }
                    // Entry list
                    repeat(2) {
                        Box(
                            Modifier.fillMaxWidth().height(40.dp).background(
                                surfaceVariant.copy(alpha = 0.15f),
                                RoundedCornerShape(10.dp)
                            )
                        )
                    }
                }

                PeekScreenType.Creation -> {
                    // ── Creation-style mock: form fields ──
                    Text(
                        label,
                        style = MaterialTheme.typography.titleSmall,
                        color = onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    repeat(4) { idx ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                Modifier.width((50 + idx * 15).dp).height(6.dp).background(
                                    onSurface.copy(alpha = 0.07f),
                                    RoundedCornerShape(3.dp)
                                )
                            )
                            Box(
                                Modifier.fillMaxWidth().height(if (idx == 3) 56.dp else 40.dp).background(
                                    surfaceVariant.copy(alpha = 0.2f),
                                    RoundedCornerShape(if (idx == 3) 10.dp else 8.dp)
                                )
                            )
                        }
                    }
                    // Button placeholder
                    Box(
                        Modifier.fillMaxWidth().height(40.dp).background(
                            accentColor.copy(alpha = 0.15f),
                            RoundedCornerShape(10.dp)
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            Modifier.width(50.dp).height(8.dp).background(
                                accentColor.copy(alpha = 0.25f),
                                RoundedCornerShape(4.dp)
                            )
                        )
                    }
                }

                PeekScreenType.Generic -> {
                    // ── Generic clean gradient fallback (same as before) ──
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    accentColor.copy(alpha = 0.03f),
                                    Color.Transparent
                                )
                            )
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalActivityApi::class)
@Composable
fun SwipeBackHost(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    previousScreen: PreviousScreenInfo? = null,
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
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
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
    val contentScale = 1f - progress * (1f - FieldMindMotion.swipeScaleFactor)
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
        // Slides in from the left behind the current content.
        // Uses parallax (70% speed) for depth layering effect.
        // When [PeekContentHolder] provides real composable content (set by
        // [FieldMindNavHost]), renders the ACTUAL previous screen's composable
        // with [Key] for state preservation — always in the tree (hidden
        // offscreen when not peeking) so state survives across peek cycles.
        // Falls back to the [PeekPreviewContent] mock when no real content.
        val peekHolder = LocalPeekContentHolder.current
        val realPeekContent = peekHolder.peekContent
        val realPeekKey = peekHolder.peekKey
        val hasRealContent = realPeekContent != null && realPeekKey != null
        val showLayer = isHorizontalPeek && (hasRealContent || (progress > 0.01f && previousScreen != null))

        if (showLayer) {
            val previewWidth = if (hasRealContent) contentWidth else contentWidth * 0.85f
            val previewScale = 0.94f + (1f - 0.94f) * (1f - progress)
            val screenColor = MaterialTheme.colorScheme.primary

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset {
                        val offset = if (hasRealContent) {
                            // Real content: always in tree; offscreen (-contentWidth) when
                            // not peeking, pinned to left of current screen when peeking.
                            if (progress > 0.005f) animX.value - contentWidth else -contentWidth
                        } else {
                            // Mock content: only visible when peeking
                            animX.value - previewWidth
                        }
                        IntOffset(offset.roundToInt(), 0)
                    }
                    .width(Dp(previewWidth))
                    .fillMaxHeight()
                    .graphicsLayer {
                        scaleX = previewScale
                        scaleY = previewScale
                        transformOrigin = TransformOrigin(1f, 0.5f)
                        // Hide when not peeking (but keep in tree for state)
                        alpha = if (hasRealContent && progress <= 0.005f) 0f else 1f
                    }
            ) {
                if (hasRealContent) {
                    // ── REAL previous screen composable (kept alive with Key) ──
                    key(realPeekKey) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
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
                } else if (previousScreen != null && progress > 0.01f) {
                    // ── MOCK preview (no real composable available) ──
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                        tonalElevation = 3.dp,
                        shadowElevation = 16.dp,
                        border = androidx.compose.foundation.BorderStroke(
                            0.5.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // ── Mock status bar area ──
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(screenColor.copy(alpha = 0.08f))
                                    .padding(horizontal = 20.dp, vertical = 14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Back arrow + screen name
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            FieldMindIcons.ChevronLeft,
                                            "Back",
                                            size = 22.dp,
                                            tint = screenColor.copy(alpha = 0.8f)
                                        )
                                        Text(
                                            previousScreen.label,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            // ── Screen-type-specific peek preview content ──
                            PeekPreviewContent(
                                screenType = previousScreen.screenType,
                                label = previousScreen.label,
                                accentColor = screenColor
                            )
                        }
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
                                    if (currentVal > maxVal * FieldMindMotion.swipeThreshold) {
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
                                            animX.animateTo(0f, FieldMindMotion.swipeBackSpring)
                                            animY.animateTo(0f, FieldMindMotion.swipeBackSpring)
                                        }
                                    }
                                },
                                onDragCancel = {
                                    activeDirection = null
                                    scope.launch {
                                        animX.animateTo(0f, FieldMindMotion.swipeBackSpring)
                                        animY.animateTo(0f, FieldMindMotion.swipeBackSpring)
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
                        .clip(RoundedCornerShape(12.dp))
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
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(FieldMindIcons.ChevronDown, "Swipe down to dismiss", size = 22.dp, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
