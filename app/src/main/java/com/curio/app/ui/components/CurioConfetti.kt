package com.curio.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.curio.app.ui.theme.CurioMotion
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Confetti / sparkle burst — see CURIO_SPEC.md section 0.5 ("Rewarding
 * moments (topic revealed, entry saved) get a small confetti / sparkle
 * burst — a scatter of 6 to 10 tiny shapes in the category's accent color,
 * fading and falling with slight rotation, ~600ms total").
 *
 * This is a Canvas-based particle system. Particles are emitted from the
 * center of the modifier, given an initial outward velocity + slight upward
 * bias, then fall with simulated gravity. Each particle also rotates as it
 * falls. After [CurioMotion.Durations.Confetti] (600ms by default), the
 * particles fade out and the composable removes itself from the composition.
 *
 * Usage:
 * ```
 * var burstKey by remember { mutableStateOf(0) }
 * ConfettiBurst(
 *     color = category.accent,
 *     trigger = burstKey,
 *     modifier = Modifier.fillMaxSize(),
 *     onComplete = { /* maybe navigate */ }
 * )
 * Button(onClick = { burstKey++ }) { Text("Burst") }
 * ```
 *
 * The [trigger] key increments to re-trigger; a new burst fires each time
 * the key changes (mirrors the LaunchedEffect-key pattern).
 *
 * Particles are drawn as tiny rectangles + small stars + circles mixed, all
 * in the supplied [color] (tinted slightly for variety). The single accent
 * color is sufficient — per the spec, the confetti is "in the category's
 * accent color" and one color reads as more unified than many.
 */
@Composable
fun ConfettiBurst(
    color: Color,
    modifier: Modifier = Modifier,
    trigger: Any = Unit,
    particleCount: Int = CurioMotion.ConfettiParticleCount,
    onComplete: () -> Unit = {}
) {
    // One particle set per trigger value; Animatable drives the progress
    val particles = remember(trigger) {
        ParticleFactory.makeParticles(particleCount)
    }
    val progress = remember(trigger) { Animatable(0f) }

    LaunchedEffect(trigger) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = CurioMotion.Durations.Confetti,
                easing = LinearEasing
            )
        )
        onComplete()
    }

    Canvas(modifier = modifier) {
        drawParticles(
            particles = particles,
            progress = progress.value,
            color = color,
            canvasSize = size
        )
    }
}

/**
 * A single confetti particle's parameters. Position is parameterized as
 * fractions of the canvas size so the particle moves correctly regardless
 * of the actual screen dimensions. Velocity is in normalized units per
 * millisecond (multiplied by progress at draw time).
 */
private data class Particle(
    val angleDeg: Float,         // 0 = up, increases clockwise
    val speedPxPerMs: Float,     // initial outward speed (px per ms)
    val upwardBiasPxPerMs: Float, // initial upward kick (px per ms)
    val gravityPxPerMs2: Float,  // downward acceleration
    val rotationDeg: Float,      // initial rotation
    val rotationSpeedDegPerMs: Float, // rotation per ms
    val sizePx: Float,           // particle bounding box size in px
    val shape: ParticleShape
)

private enum class ParticleShape { Circle, Star, Ribbon }

private object ParticleFactory {
    fun makeParticles(count: Int): List<Particle> = List(count) {
        val angle = Random.nextFloat() * 360f
        Particle(
            angleDeg = angle,
            speedPxPerMs = Random.nextFloat() * 0.40f + 0.15f,   // 0.15 - 0.55 px/ms
            upwardBiasPxPerMs = Random.nextFloat() * 0.30f + 0.20f, // 0.20 - 0.50
            gravityPxPerMs2 = 0.0009f + Random.nextFloat() * 0.0005f,
            rotationDeg = Random.nextFloat() * 360f,
            rotationSpeedDegPerMs = (Random.nextFloat() - 0.5f) * 1.2f, // +/- 0.6 deg/ms
            sizePx = 6f + Random.nextFloat() * 6f,                   // 6 - 12 px
            shape = ParticleShape.values()[Random.nextInt(ParticleShape.values().size)]
        )
    }
}

/**
 * Internal draw routine. Computes each particle's position at the current
 * progress, then draws the particle shape in [color]. Particles fade out
 * linearly across the second half of the animation (progress 0.5 - 1.0).
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawParticles(
    particles: List<Particle>,
    progress: Float,
    color: Color,
    canvasSize: Size
) {
    if (canvasSize.width <= 0f || canvasSize.height <= 0f) return

    val cx = canvasSize.width / 2f
    val cy = canvasSize.height / 2f
    // Lifetime in ms — matches CurioMotion.Durations.Confetti.
    val totalMs = CurioMotion.Durations.Confetti.toFloat()
    val t = progress * totalMs // current time in ms

    particles.forEach { p ->
        // Position physics: distance = speed * t, with initial upward bias,
        // and gravity pulling down over time.
        val dist = p.speedPxPerMs * t
        val upOffset = p.upwardBiasPxPerMs * t - 0.5f * p.gravityPxPerMs2 * t * t
        val angleRad = Math.toRadians(p.angleDeg.toDouble()).toFloat()
        // Particle offset from center.
        val dx = cos(angleRad) * dist
        val dy = sin(angleRad) * dist + upOffset
        val px = cx + dx
        val py = cy + dy

        // Linear fade-out during the second half of the animation.
        val alpha = if (progress < 0.5f) 1f else (1f - (progress - 0.5f) * 2f).coerceIn(0f, 1f)
        val tinted = color.copy(alpha = alpha)

        // Rotation
        val rotation = p.rotationDeg + p.rotationSpeedDegPerMs * t

        rotate(degrees = rotation, pivot = Offset(px, py)) {
            when (p.shape) {
                ParticleShape.Circle -> {
                    drawCircle(
                        color = tinted,
                        radius = p.sizePx / 2f,
                        center = Offset(px, py)
                    )
                }
                ParticleShape.Star -> {
                    // 4-point star centered at (px, py) with outer radius size/2.
                    val s = p.sizePx / 2f
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(px, py - s)
                        lineTo(px + s * 0.35f, py - s * 0.35f)
                        lineTo(px + s, py)
                        lineTo(px + s * 0.35f, py + s * 0.35f)
                        lineTo(px, py + s)
                        lineTo(px - s * 0.35f, py + s * 0.35f)
                        lineTo(px - s, py)
                        lineTo(px - s * 0.35f, py - s * 0.35f)
                        close()
                    }
                    drawPath(path = path, color = tinted)
                }
                ParticleShape.Ribbon -> {
                    // Thin rectangle (width 2dp, height 12dp-ish) — feels like paper confetti.
                    val w = 2.5f
                    val h = p.sizePx
                    drawRect(
                        color = tinted,
                        topLeft = Offset(px - w / 2f, py - h / 2f),
                        size = Size(w, h)
                    )
                }
            }
        }
    }
}

/**
 * A simple circular spark that pulses around a point — used inline as a
 * sparkle accent (e.g. near a topic's image on Reveal). Independent of the
 * full confetti burst so it can be embedded in cards without triggering
 * the whole animation.
 *
 * Renders as an expanding ring that fades out over the duration, then
 * removes itself. Designed for small subtle use (not full-screen).
 */
@Composable
fun CurioSparkle(
    color: Color,
    modifier: Modifier = Modifier,
    trigger: Any = Unit,
    size: Dp = 24.dp,
    onComplete: () -> Unit = {}
) {
    val progress = remember(trigger) { Animatable(0f) }

    LaunchedEffect(trigger) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = CurioMotion.Durations.Confetti,
                easing = LinearEasing
            )
        )
        onComplete()
    }

    Canvas(modifier = modifier) {
        val maxRadiusPx = (this.size.minDimension / 2f).coerceAtLeast(1f)
        val radius = maxRadiusPx * progress.value
        val alpha = (1f - progress.value).coerceIn(0f, 1f)
        drawCircle(
            color = color.copy(alpha = alpha),
            radius = radius,
            center = Offset(this.size.width / 2f, this.size.height / 2f),
            style = Stroke(width = 2f)
        )
    }
}