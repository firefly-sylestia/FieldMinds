package com.curio.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.curio.app.ui.theme.CurioMotion

// ═══════════════════════════════════════════════════════════════════════════
// Screen-level entrance animations
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Universal entrance wrapper — fades + slides any screen content up from
 * a small offset over a quick spring. Wraps the screen's main scrollable
 * content; the back-bar / top-bar should be rendered outside so the bar
 * stays anchored while the body slides in.
 *
 * Per CURIO_SPEC.md §0.5: "Everywhere else, keep transitions under 400ms
 * so the app never feels like it's making you wait to be delighted."
 */
@Composable
fun ScreenEntrance(content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(CurioMotion.Durations.Standard)) +
                slideInVertically(
                    animationSpec = spring(dampingRatio = 0.85f, stiffness = 380f),
                    initialOffsetY = { it / 8 }
                ),
        content = { content() }
    )
}

/**
 * Dramatic screen entrance — scale up from 0.85 + fade in, with an elastic
 * spring for that premium "morph into view" feel. Use for hero screens:
 * Topic Reveal, Spin landing, Splash → Home.
 */
@Composable
fun MorphEntrance(
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = CurioMotion.Durations.Reveal,
                easing = FastOutSlowInEasing
            )
        ) + scaleIn(
            initialScale = 0.85f,
            animationSpec = CurioMotion.Springs.Elastic
        ),
        content = { content() }
    )
}

/**
 * Morphing container — smoothly crossfades + scales between two states.
 * When [trigger] changes, the old content scales down + fades out while
 * the new content scales up + fades in, creating a seamless morph effect.
 *
 * The [animationSpec] controls the spring feel — defaults to Morph spring
 * for an organic water-droplet feel.
 */
@Composable
fun MorphingContainer(
    trigger: Any,
    modifier: Modifier = Modifier,
    animationSpec: androidx.compose.animation.core.SpringSpec<Float> = CurioMotion.Springs.Morph,
    content: @Composable () -> Unit
) {
    @Suppress("UnusedContentLambdaTargetStateParameter")
    androidx.compose.animation.AnimatedContent(
        targetState = trigger,
        modifier = modifier,
        transitionSpec = {
            fadeIn(animationSpec = tween(CurioMotion.Durations.Morph)) +
                    scaleIn(
                        initialScale = 0.92f,
                        animationSpec = animationSpec
                    ) togetherWith
                    fadeOut(animationSpec = tween(CurioMotion.Durations.Quick)) +
                    androidx.compose.animation.scaleOut(
                        targetScale = 0.96f,
                        animationSpec = spring(dampingRatio = 0.95f, stiffness = 400f)
                    )
        },
        label = "morph"
    ) { content() }
}

// ═══════════════════════════════════════════════════════════════════════════
// Ambient / breathing animations
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Breathing scale — a slow, gentle pulse that gives static elements a
 * "living" feel. Use for hero cards, decorative glyphs, and ambient
 * backgrounds that should feel alive rather than frozen.
 *
 * Returns a scale value between 0.97 and 1.03, cycling over ~3.2 seconds.
 *
 * @param active Whether to animate. When false, returns 1f.
 * @param amplitude Range of the pulse (default 0.03 for subtle, 0.06 for noticeable).
 */
@Composable
fun rememberBreathingScale(
    active: Boolean = true,
    amplitude: Float = 0.03f
): Float {
    if (!active) return 1f
    val transition = rememberInfiniteTransition(label = "breathe")
    val scale by transition.animateFloat(
        initialValue = 1f - amplitude,
        targetValue = 1f + amplitude,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = CurioMotion.Durations.Breathe,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breatheScale"
    )
    return scale
}

/**
 * Shimmer brush — an animated linear gradient that sweeps left-to-right
 * across a surface, giving it a subtle \"light passing over\" effect.
 * Use on cards during loading, or as a premium ambient detail on hero
 * elements (subtle, low-alpha).
 *
 * Returns a [Brush] that animates continuously.
 *
 * @param shimmerColor The highlight color (typically white at low alpha).
 * @param baseColor The base surface color.
 */
@Composable
fun rememberShimmerBrush(
    shimmerColor: Color = Color.White.copy(alpha = 0.15f),
    baseColor: Color = Color.Transparent
): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = CurioMotion.Durations.Shimmer,
                easing = LinearEasing
            )
        ),
        label = "shimmerTranslate"
    )
    return Brush.horizontalGradient(
        colors = listOf(baseColor, shimmerColor, baseColor),
        startX = translateAnim * 1000f,
        endX = (translateAnim + 0.4f) * 1000f
    )
}

/**
 * Rotating reveal — a decorative element that slowly rotates while gently
 * pulsing. Used by the Topic Reveal sparkle motif and other decorative
 * glyphs throughout the app.
 *
 * @param rotationPeriodMs Full rotation cycle in ms (default 12s).
 * @param pulseAmplitude Scale pulse range (default 0.85 to 1.10).
 */
@Composable
fun rememberRotatingReveal(
    rotationPeriodMs: Int = 12000,
    pulseAmplitude: Pair<Float, Float> = 0.85f to 1.10f
): Pair<Float, Float> {
    val transition = rememberInfiniteTransition(label = "rotatingReveal")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = rotationPeriodMs, easing = LinearEasing)
        ),
        label = "revealRot"
    )
    val pulse by transition.animateFloat(
        initialValue = pulseAmplitude.first,
        targetValue = pulseAmplitude.second,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "revealPulse"
    )
    return rotation to pulse
}

// ═══════════════════════════════════════════════════════════════════════════
// Interactive micro-animations
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Animated scale on press — a simple interactive scale-down that springs
 * back. Wrap any clickable element for tactile feedback.
 *
 * Usage:
 * ```
 * val scale by rememberAnimatedScaleOnPress(pressed = isPressed)
 * Box(Modifier.scale(scale).clickable { isPressed = true })
 * ```
 *
 * @param pressed Whether the element is currently pressed.
 * @param pressedScale Target scale when pressed (default 0.94).
 */
@Composable
fun rememberAnimatedScaleOnPress(
    pressed: Boolean,
    pressedScale: Float = 0.94f
): androidx.compose.runtime.State<Float> {
    val target = if (pressed) pressedScale else 1f
    return animateFloatAsState(
        targetValue = target,
        animationSpec = CurioMotion.Springs.Press,
        label = "pressScale"
    )
}

// ═══════════════════════════════════════════════════════════════════════════
// Pulsing + waveform animations (carried forward from v1)
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Pulsing scale for any element that needs a \"live\" feel — used by the
 * Sound Bite mic ring while recording (CURIO_SPEC.md §8.1: \"Button morphs
 * into a pulsing ring while live\"). Returns 1f when inactive; when active,
 * pulses between 1.0 and ~1.18 over a 900ms cycle.
 */
@Composable
fun rememberPulseScale(active: Boolean): Float {
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    return if (active) scale else 1f
}

/**
 * Live waveform display — REAL microphone-driven visualizer drawn as N
 * rounded vertical bars. Used by Save/Capture Sound Bite format
 * (CURIO_SPEC.md §8.1) while recording, and as a quiet \"armed\" indicator
 * when not yet started.
 *
 * [level] is the live mic amplitude (0.0–1.0) polled from
 * [com.curio.app.features.capture.AudioRecorder.maxAmplitude]; each frame
 * the newest level is appended to a short history ring so the bars ripple
 * with a decaying tail, exactly like a real audio meter. When [active] is
 * false, a flat quiet bar row (no motion) suggests the controls are armed.
 */
@Composable
fun LiveWaveform(
    modifier: Modifier = Modifier,
    color: Color,
    active: Boolean,
    barCount: Int = 36,
    level: Float = 0f
) {
    val levelState by rememberUpdatedState(level)
    // Short history ring — the newest mic level goes in at the end; earlier
    // entries decay toward a floor so the wave shows a natural falling tail.
    val history = remember { FloatArray(barCount) { 0.08f } }
    var historyTick by remember { mutableIntStateOf(0) }

    // Push a new level every frame while recording; when inactive, decay the
    // ring to the flat floor so the bars go quiet without jumping.
    LaunchedEffect(active) {
        while (true) {
            val target = if (active) levelState else 0.08f
            // Move the ring one step; each bar eases toward the target from
            // behind, giving a smooth chasing/decay feel per frame.
            for (i in 0 until barCount) {
                val current = history[i]
                val next = if (active) {
                    // Front bar snaps to the live level; older bars fall off.
                    if (i == barCount - 1) {
                        (current + (target - current) * 0.65f).coerceIn(0.08f, 1f)
                    } else {
                        (current * 0.86f).coerceAtLeast(0.08f)
                    }
                } else {
                    (current + (target - current) * 0.25f).coerceIn(0.06f, 0.2f)
                }
                history[i] = next
            }
            historyTick++
            kotlinx.coroutines.delay(70)
        }
    }

    val tick = historyTick
    Canvas(modifier = modifier) {
        val gap = 2f.dp.toPx()
        val barWidth = (size.width - gap * (barCount - 1)) / barCount
        for (i in 0 until barCount) {
            val amp = history[i]
            val h = size.height * amp
            drawRoundRect(
                color = color.copy(alpha = 0.9f),
                topLeft = Offset(i * (barWidth + gap), (size.height - h) / 2f),
                size = Size(barWidth, h),
                cornerRadius = CornerRadius(barWidth / 2f)
            )
        }
        // Read the tick so the Canvas recomposes each new mic frame.
        if (tick < 0) return@Canvas
    }
}

/**
 * Formats an elapsed-seconds count as mm:ss for the Sound Bite timer
 * (CURIO_SPEC.md §8.1: \"running timer\").
 */
fun formatRecordingTime(seconds: Int): String {
    val mm = seconds / 60
    val ss = seconds % 60
    return "%d:%02d".format(mm, ss)
}
