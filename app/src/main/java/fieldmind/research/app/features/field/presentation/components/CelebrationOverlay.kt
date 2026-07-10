package fieldmind.research.app.features.field.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import androidx.compose.ui.platform.LocalDensity

// ══════════════════════════════════════════════════════════════════════
//  Celebration Particle System
//  Delightful confetti, sparkle, and star bursts for achievement
//  unlocks, daily goal completion, streak milestones, and first saves.
// ══════════════════════════════════════════════════════════════════════

/**
 * Type of celebration particle: confetti (colored rectangle), sparkle (small circle with glow),
 * or star (5-pointed star shape).
 */
enum class ParticleType {
    CONFETTI,
    SPARKLE,
    STAR
}

/**
 * Variant of celebration effect.
 * - CONFETTI_BURST: 40+ colorful rotating rectangles exploding outward then falling with gravity
 * - GENTLE_SPARKLE: 15-20 small glowing circles rising upward in a fountain pattern
 * - STAR_BURST: 25+ stars spinning outward with trailing particles
 * - MIXED: Combination of confetti + sparkles for major celebrations
 */
enum class CelebrationVariant(
    val particleCount: Int,
    val durationMs: Long,
    val gravity: Float
) {
    CONFETTI_BURST(50, 3000L, 0.08f),
    GENTLE_SPARKLE(20, 2000L, -0.02f),  // negative = rise upward
    STAR_BURST(30, 2800L, 0.05f),
    MIXED(70, 3500L, 0.06f)
}

/**
 * A single celebration particle with physics state and visual properties.
 */
data class ConfettiParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var rotation: Float,        // current rotation angle in radians
    var angularVelocity: Float, // rotation speed
    var size: Float,            // base size (width for confetti, radius for sparkles/stars)
    var color: Color,
    var type: ParticleType,
    var alpha: Float = 1f,
    var alive: Boolean = true,
    var life: Float = 1f,       // normalized life remaining (1 → 0)
    var age: Float = 0f         // age in seconds
)

/**
 * State controller for triggering celebrations from any screen.
 * Create with [rememberCelebrationState()], then call [trigger()] on the desired event.
 */
class CelebrationState {
    internal var currentVariant by mutableStateOf<CelebrationVariant?>(null)
    internal var triggerKey by mutableStateOf(0L)

    /**
     * Trigger a celebration effect. Takes effect on next recomposition.
     * @param variant Type of celebration to show.
     */
    fun trigger(variant: CelebrationVariant = CelebrationVariant.CONFETTI_BURST) {
        currentVariant = variant
        triggerKey = System.nanoTime()
    }

    /** Reset the celebration state (dismiss current effect). */
    fun dismiss() {
        currentVariant = null
    }
}

/**
 * Remember a [CelebrationState] scoped to this composition. Use the returned
 * state to trigger celebrations from anywhere within the scope.
 */
@Composable
fun rememberCelebrationState(): CelebrationState {
    return remember { CelebrationState() }
}

/**
 * Full-screen celebration overlay that renders animated confetti, sparkles,
 * or stars on top of all content. Place this as the topmost layer in your screen.
 *
 * The overlay is transparent and non-interactive — touches pass through to
 * content below. It auto-dismisses once all particles have expired.
 *
 * @param celebrationState The state controller that drives this overlay.
 * @param modifier Modifier for the overlay box.
 */
@Composable
fun CelebrationOverlay(
    celebrationState: CelebrationState,
    modifier: Modifier = Modifier
) {
    val variant = celebrationState.currentVariant ?: return
    val triggerKey = celebrationState.triggerKey
    val reduceMotion = FieldMindMotion.isReduceMotion()
    if (reduceMotion) return

    var particles by remember(triggerKey) { mutableStateOf(emptyList<ConfettiParticle>()) }
    var elapsedMs by remember(triggerKey) { mutableStateOf(0L) }

    // Compute canvas dimensions in composable scope (NOT inside LaunchedEffect)
    val density = LocalDensity.current
    val canvasWidthPx = with(density) { 360.dp.toPx() }
    val canvasHeightPx = with(density) { 640.dp.toPx() }

    // Initialize particles on trigger
    LaunchedEffect(triggerKey) {
        val rng = Random(triggerKey)
        val count = variant.particleCount
        val duration = variant.durationMs
        val particleList = mutableListOf<ConfettiParticle>()

        // Color palette — vibrant, celebratory hues
        val palette = listOf(
            Color(0xFFFF6B6B), // coral
            Color(0xFF4ECDC4), // teal
            Color(0xFFFFD93D), // gold
            Color(0xFF6C5CE7), // purple
            Color(0xFFA8E6CF), // mint
            Color(0xFFFF8A5C), // orange
            Color(0xFF74B9FF), // sky blue
            Color(0xFFFD79A8), // pink
            Color(0xFF00CEC9), // cyan
            Color(0xFFE17055), // terracotta
            Color(0xFF0984E3), // blue
            Color(0xFFFDCB6E)  // amber
        )

        repeat(count) { i ->
            val type = when (variant) {
                CelebrationVariant.CONFETTI_BURST -> ParticleType.CONFETTI
                CelebrationVariant.GENTLE_SPARKLE -> ParticleType.SPARKLE
                CelebrationVariant.STAR_BURST -> ParticleType.STAR
                CelebrationVariant.MIXED -> {
                    when (rng.nextInt(3)) {
                        0 -> ParticleType.CONFETTI
                        1 -> ParticleType.SPARKLE
                        else -> ParticleType.STAR
                    }
                }
            }

            val color = palette[rng.nextInt(palette.size)]
            val size = when (type) {
                ParticleType.CONFETTI -> 6f + rng.nextFloat() * 10f
                ParticleType.SPARKLE -> 2f + rng.nextFloat() * 4f
                ParticleType.STAR -> 4f + rng.nextFloat() * 8f
            }

            // Burst origin: slightly above center with horizontal spread
            val centerX = canvasWidthPx * 0.5f
            val centerY = canvasHeightPx * 0.35f
            val angle = rng.nextFloat() * Math.PI.toFloat() * 2f
            val burstSpeed = when (type) {
                ParticleType.CONFETTI -> 200f + rng.nextFloat() * 400f
                ParticleType.SPARKLE -> 80f + rng.nextFloat() * 150f
                ParticleType.STAR -> 150f + rng.nextFloat() * 300f
            }
            val speed = burstSpeed * (0.5f + rng.nextFloat() * 0.8f)

            particleList.add(
                ConfettiParticle(
                    x = centerX + cos(angle) * rng.nextFloat() * 40f,
                    y = centerY + sin(angle) * rng.nextFloat() * 20f,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed - speed * 0.3f, // bias upward
                    rotation = rng.nextFloat() * 6.28f,
                    angularVelocity = (rng.nextFloat() - 0.5f) * 12f,
                    size = size,
                    color = color,
                    type = type,
                    life = 1f,
                    alive = true
                )
            )
        }

        particles = particleList

        // Animation loop
        val frameInterval = 16L // ~60fps
        while (elapsedMs < duration) {
            val dt = frameInterval / 1000f
            val gravity = variant.gravity * 60f // scaled per second

            particles = particles.map { p ->
                if (!p.alive) return@map p

                // Update physics
                val newVx = p.vx
                val newVy = p.vy + gravity * dt * 60f
                val newX = p.x + newVx * dt
                val newY = p.y + newVy * dt
                val newRotation = p.rotation + p.angularVelocity * dt
                val newAge = p.age + dt
                val newLife = (1f - newAge / (duration / 1000f)).coerceIn(0f, 1f)

                // Alpha: fade in quickly, hold, then fade out in last 30%
                val newAlpha = when {
                    newAge < 0.15f -> newAge / 0.15f
                    newLife < 0.3f -> newLife / 0.3f
                    else -> 1f
                }.coerceIn(0f, 1f)

                p.copy(
                    x = newX,
                    y = newY,
                    vx = newVx,
                    vy = newVy,
                    rotation = newRotation,
                    age = newAge,
                    life = newLife,
                    alpha = newAlpha,
                    alive = newX > -200f && newX < canvasWidthPx + 200f &&
                            newY < canvasHeightPx + 200f &&
                            newAlpha > 0.01f
                )
            }
            elapsedMs += frameInterval
            delay(frameInterval)
        }
        // Clear when done
        particles = emptyList()
        celebrationState.dismiss()
    }

    // Render particles on Canvas
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        val w = size.width
        val h = size.height

        // Scale particles to actual canvas size (handle different screen sizes)
        val densityFactor = w / 360f // relative to 360dp design width

        particles.forEach { p ->
            if (!p.alive) return@forEach
            val sx = p.x * densityFactor
            val sy = p.y * densityFactor
            val s = p.size * densityFactor

            drawParticle(
                particle = p,
                x = sx,
                y = sy,
                size = s,
                alpha = p.alpha
            )
        }
    }
}

/**
 * Draw a single particle in the Canvas DrawScope.
 * Renders differently based on ParticleType.
 */
private fun DrawScope.drawParticle(
    particle: ConfettiParticle,
    x: Float,
    y: Float,
    size: Float,
    alpha: Float
) {
    val color = particle.color.copy(alpha = alpha)

    when (particle.type) {
        ParticleType.CONFETTI -> {
            // Rotated rectangle — like a piece of real confetti
            val halfW = size
            val halfH = size * 0.5f
            rotate(
                degrees = Math.toDegrees(particle.rotation.toDouble()).toFloat(),
                pivot = Offset(x, y)
            ) {
                drawRect(
                    color = color,
                    topLeft = Offset(x - halfW, y - halfH),
                    size = Size(halfW * 2, halfH * 2),
                    style = Fill
                )
                // Slight highlight edge
                drawRect(
                    color = Color.White.copy(alpha = alpha * 0.3f),
                    topLeft = Offset(x - halfW, y - halfH),
                    size = Size(halfW * 2, halfH * 0.5f),
                    style = Fill
                )
            }
        }

        ParticleType.SPARKLE -> {
            // Glowing circle — rises upward with soft brightness
            val glowRadius = size * 2.5f
            // Outer glow
            drawCircle(
                color = color.copy(alpha = alpha * 0.08f),
                radius = glowRadius,
                center = Offset(x, y)
            )
            drawCircle(
                color = color.copy(alpha = alpha * 0.2f),
                radius = size * 1.3f,
                center = Offset(x, y)
            )
            // Bright center
            drawCircle(
                color = Color.White.copy(alpha = alpha * 0.6f),
                radius = size * 0.4f,
                center = Offset(x, y)
            )
            drawCircle(
                color = color,
                radius = size * 0.7f,
                center = Offset(x, y)
            )
        }

        ParticleType.STAR -> {
            // 5-pointed star
            val points = 5
            val outerR = size
            val innerR = size * 0.4f
            val starPath = androidx.compose.ui.graphics.Path().apply {
                for (i in 0 until points * 2) {
                    val r = if (i % 2 == 0) outerR else innerR
                    val angle = Math.PI.toFloat() * i / points - Math.PI.toFloat() / 2f
                    val px = x + cos(angle) * r
                    val py = y + sin(angle) * r
                    if (i == 0) moveTo(px, py) else lineTo(px, py)
                }
                close()
            }
            // Glow behind star
            drawCircle(
                color = color.copy(alpha = alpha * 0.12f),
                radius = size * 2f,
                center = Offset(x, y)
            )
            // Star fill
            drawPath(
                path = starPath,
                color = color.copy(alpha = alpha),
                style = Fill
            )
            // Star outline
            drawPath(
                path = starPath,
                color = Color.White.copy(alpha = alpha * 0.4f),
                style = Stroke(width = 1.5f)
            )
            // Bright center dot
            drawCircle(
                color = Color.White.copy(alpha = alpha * 0.5f),
                radius = size * 0.2f,
                center = Offset(x, y)
            )
        }
    }
}
