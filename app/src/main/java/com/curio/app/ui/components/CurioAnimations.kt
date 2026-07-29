package com.curio.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.curio.app.ui.theme.CurioMotion
import kotlin.math.PI
import kotlin.math.sin

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
 * Pulsing scale for any element that needs a "live" feel — used by the
 * Sound Bite mic ring while recording (CURIO_SPEC.md §8.1: "Button morphs
 * into a pulsing ring while live"). Returns 1f when inactive; when active,
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
 * Live waveform display — Canvas-based animation that draws N vertical
 * bars with heights driven by a sine wave. Used by Save/Capture Sound Bite
 * format (CURIO_SPEC.md §8.1) both while recording and as a quiet
 * "armed" indicator when not yet started.
 *
 * When [active] is true, the waveform pulses at full amplitude (0.25 to 1.0).
 * When false, it draws a quieter waveform (0.10 to 0.28) to suggest the
 * controls are armed.
 */
@Composable
fun LiveWaveform(
    modifier: Modifier = Modifier,
    color: Color,
    active: Boolean,
    barCount: Int = 36
) {
    val transition = rememberInfiniteTransition(label = "wave")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing)
        ),
        label = "wavePhase"
    )
    Canvas(modifier = modifier) {
        val gap = 2f.dp.toPx()
        val barWidth = (size.width - gap * (barCount - 1)) / barCount
        for (i in 0 until barCount) {
            val t = phase + i * 0.45f
            val raw = (0.5f + 0.5f * sin(t.toDouble()).toFloat())
            val amp = if (active) {
                (0.25f + 0.75f * raw)
            } else {
                (0.10f + 0.18f * raw).coerceAtLeast(0.05f)
            }
            val h = size.height * amp
            drawRoundRect(
                color = color,
                topLeft = Offset(i * (barWidth + gap), (size.height - h) / 2f),
                size = Size(barWidth, h),
                cornerRadius = CornerRadius(barWidth / 2f)
            )
        }
    }
}

/**
 * Formats an elapsed-seconds count as mm:ss for the Sound Bite timer
 * (CURIO_SPEC.md §8.1: "running timer").
 */
fun formatRecordingTime(seconds: Int): String {
    val mm = seconds / 60
    val ss = seconds % 60
    return "%d:%02d".format(mm, ss)
}