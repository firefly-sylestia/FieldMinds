package fieldmind.research.app.features.field.presentation.components

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.IntOffset

// ────────────────────────────────────────────────────────────────────────────
//  CompositionLocal for SharedTransitionScope
// ────────────────────────────────────────────────────────────────────────────

/**
 * CompositionLocal that provides the [SharedTransitionScope] from the nearest
 * [SharedTransitionLayout] ancestor. Screens use this to apply
 * [Modifier.sharedElement] or [Modifier.sharedBounds] for hero-style morph
 * transitions without needing explicit parameter threading through the
 * composable hierarchy.
 */
val LocalSharedTransitionScope: ProvidableCompositionLocal<SharedTransitionScope?> =
    compositionLocalOf { null }

/**
 * CompositionLocal that provides the [AnimatedVisibilityScope] from the nearest
 * [NavHost] composable route. Screens that need [Modifier.sharedElement] or
 * [Modifier.sharedBounds] should read this scope alongside [LocalSharedTransitionScope].
 *
 * Each [composable] route in NavHost has its own AnimatedVisibilityScope (the `this`
 * receiver of the content lambda). The scope is provided via this CompositionLocal
 * so screens can access it without explicit parameter threading.
 */
val LocalAnimatedVisibilityScope: ProvidableCompositionLocal<AnimatedVisibilityScope?> =
    compositionLocalOf { null }

// ────────────────────────────────────────────────────────────────────────────
//  Transition Helpers
// ────────────────────────────────────────────────────────────────────────────

/**
 * A shared-axis horizontal transition — slides content in/out along the X axis.
 *
 * [direction] indicates the slide direction:
 *   - `1`  → slides in from the right, out to the left
 *   - `-1` → slides in from the left, out to the right
 *   - `0`  → uses a gentle fade + scale (no directional bias)
 *
 * Uses springs derived from the provided [AnimationConfig] for smooth
 * slide offset and fade physics.
 */
fun AnimatedContentTransitionScope<*>.sharedAxisHorizontal(
    direction: Int,
    initialScale: Float = 0.97f,
    config: AnimationConfig = AnimationConfig.DEFAULT
): ContentTransform {
    val slideSpec = config.slideSpring()
    val fadeSpec = spring<Float>(
        dampingRatio = config.entranceDampingRatio,
        stiffness = (config.entranceStiffness * 0.78f).coerceAtLeast(60f)
    )

    val enter: EnterTransition = if (direction != 0) {
        slideInHorizontally(slideSpec) { direction * it } +
            fadeIn(animationSpec = fadeSpec)
    } else {
        fadeIn(animationSpec = fadeSpec) +
            scaleIn(
                initialScale = initialScale,
                animationSpec = fadeSpec
            )
    }

    val exit: ExitTransition = if (direction != 0) {
        slideOutHorizontally(slideSpec) { -direction * it } +
            fadeOut(animationSpec = fadeSpec)
    } else {
        fadeOut(animationSpec = fadeSpec)
    }

    return enter togetherWith exit
}

/**
 * A fade-through transition — the current screen fades out while the next
 * screen fades in, with a subtle cross-fade overlap.
 *
 * Uses a spring derived from the provided [AnimationConfig] for a
 * smooth, non-bouncy fade.
 */
fun AnimatedContentTransitionScope<*>.fadeThrough(
    fadeDurationMs: Int = 400,
    config: AnimationConfig = AnimationConfig.DEFAULT
): ContentTransform {
    val spec = tween<Float>(durationMillis = fadeDurationMs, easing = FastOutSlowInEasing)
    val enter = fadeIn(animationSpec = spec)
    val exit = fadeOut(animationSpec = spec)
    return enter togetherWith exit
}

/**
 * A scale-in entry transition for screens that don't have a directional slide.
 * Content scales up from [initialScale] with a subtle fade.
 */
fun scaleEnter(
    initialScale: Float = 0.97f,
    config: AnimationConfig = AnimationConfig.DEFAULT
): EnterTransition {
    val spec = spring<Float>(
        dampingRatio = config.entranceDampingRatio,
        stiffness = (config.entranceStiffness * 0.78f).coerceAtLeast(60f)
    )
    return fadeIn(animationSpec = spec) +
        scaleIn(initialScale = initialScale, animationSpec = spec)
}

/**
 * A simple fade-out exit transition.
 */
fun fadeExit(
    config: AnimationConfig = AnimationConfig.DEFAULT
): ExitTransition {
    val spec = spring<Float>(
        dampingRatio = config.entranceDampingRatio,
        stiffness = (config.entranceStiffness * 0.78f).coerceAtLeast(60f)
    )
    return fadeOut(animationSpec = spec)
}

// ────────────────────────────────────────────────────────────────────────────
//  Route Transition Spec Map
// ────────────────────────────────────────────────────────────────────────────

/**
 * Route category pairs mapped to their transition type.
 * Used by callers to select the appropriate transition for a given route pair.
 */
enum class RouteTransitionType {
    /** No directional slide — gentle fade + scale */
    Neutral,
    /** Slide to the right (forward navigation) */
    SlideRight,
    /** Slide to the left (back navigation) */
    SlideLeft,
    /** Fade-through with cross-fade overlap */
    FadeThrough
}

/**
 * Maps a route category pair to a [RouteTransitionType].
 * Categories are ordered — [fromCategory] is the route we're leaving,
 * [toCategory] is the route we're entering.
 *
 * This provides a single source of truth for transition direction decisions
 * that were previously duplicated across enter/exit/popEnter/popExit functions.
 */
fun routeTransitionType(
    fromCategory: String,
    toCategory: String,
    fromIndex: Int = -1,
    toIndex: Int = -1
): RouteTransitionType = when {
    // Tab → Tab: Slide based on index direction
    fromCategory == "Tab" && toCategory == "Tab" -> {
        if (fromIndex == -1 || toIndex == -1 || fromIndex == toIndex) RouteTransitionType.Neutral
        else if (toIndex > fromIndex) RouteTransitionType.SlideRight
        else RouteTransitionType.SlideLeft
    }
    // Tab → sub-screen: Slide left (forward)
    fromCategory == "Tab" && toCategory in listOf("SettingsHub", "SettingsSubPage", "Tool", "Detail", "Creation", "Other") ->
        RouteTransitionType.SlideLeft
    // Sub-screen → Tab (back): Slide right (reverse)
    toCategory == "Tab" && fromCategory in listOf("SettingsHub", "SettingsSubPage", "Tool", "Detail", "Creation", "Other") ->
        RouteTransitionType.SlideRight
    // Settings hub ↔ sub-page: Fade-through
    fromCategory == "SettingsHub" && toCategory == "SettingsSubPage" -> RouteTransitionType.FadeThrough
    fromCategory == "SettingsSubPage" && toCategory == "SettingsHub" -> RouteTransitionType.FadeThrough
    // Default
    else -> RouteTransitionType.Neutral
}
