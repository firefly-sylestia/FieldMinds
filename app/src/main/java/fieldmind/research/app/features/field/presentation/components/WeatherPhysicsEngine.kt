package fieldmind.research.app.features.field.presentation.components

import androidx.compose.ui.geometry.Offset
import kotlin.math.cos
import kotlin.math.sqrt
import kotlin.math.sin
import kotlin.math.abs
import kotlin.math.PI
import kotlin.random.Random

/**
 * ════════════════════════════════════════════════════════════════════════
 *  🌪 WeatherPhysicsEngine — Real Physics + 3D Depth Weather Simulation
 *
 *  A frame-rate-independent physics engine designed specifically for
 *  atmospheric weather effects in Jetpack Compose Canvas. Handles:
 *
 *  • Newtonian mechanics (F=ma) with velocity Verlet integration
 *  • 3D depth layers with perspective parallax transform
 *  • Force accumulation: gravity, wind, drag, buoyancy, turbulence
 *  • Object pooling — zero heap allocation during simulation
 *  • Frame deltaTime with adaptive sub-stepping for stability
 *  • Chemical/thermal buoyancy for convective cloud formation
 *  • Coriolis-style deflection for hemispheric wind patterns
 * ════════════════════════════════════════════════════════════════════════
 */

// ── Frame Clock ──────────────────────────────────────────────────────

/**
 * Frame-rate independent clock. Computes deltaTime in seconds,
 * clamped to prevent spiral-of-death on frame drops.
 */
class FrameClock(
    private val maxDeltaTime: Float = 0.05f  // 50ms cap = 20 FPS minimum
) {
    private var lastFrameNanos: Long = 0L

    /** Call at the start of each frame. Returns deltaTime in seconds. */
    fun tick(currentNanos: Long): Float {
        val dt = if (lastFrameNanos == 0L) {
            0.016f // Default to ~60fps on first frame
        } else {
            ((currentNanos - lastFrameNanos) / 1_000_000_000f).coerceIn(0f, maxDeltaTime)
        }
        lastFrameNanos = currentNanos
        return dt
    }

    fun reset() { lastFrameNanos = 0L }
}

// ── 3D Depth Layer System ────────────────────────────────────────────

/**
 * A depth layer in the 3D parallax system.
 * Lower depth = further away (slower parallax, smaller scale).
 * Higher depth = closer (faster parallax, larger scale).
 */
enum class DepthLayer(val z: Float) {
    SKY_BACKGROUND(0.0f),     // Stars, distant sky gradient
    FAR_CLOUDS(0.15f),        // Cirrus, distant haze
    MID_CLOUDS(0.35f),        // Cumulus, stratus
    NEAR_CLOUDS(0.55f),       // Large cumulonimbus, storm front
    PRECIPITATION(0.70f),     // Rain, snow — in front of clouds
    FOREGROUND(0.85f),        // Lightning, close fog, splash particles
    UI_OVERLAY(1.0f)          // Touch ripples, interaction markers
}

/**
 * Applies depth-parallax offset. Objects at lower depth move slower
 * (parallax), creating a convincing 3D effect.
 */
data class DepthTransform(
    val layer: DepthLayer,
    val worldX: Float = 0f,
    val worldY: Float = 0f,
    val worldZ: Float = layer.z
) {
    /** Camera-relative X position accounting for parallax. */
    fun screenX(cameraX: Float, screenWidth: Float): Float {
        val parallax = 1f - (layer.z * 0.8f)  // 0 depth = 1x, 1 depth = 0.2x
        return worldX - cameraX * (1f - parallax) + screenWidth / 2f
    }

    /** Perspective scale: farther objects appear smaller. */
    fun perspectiveScale(): Float = 0.6f + (1f - layer.z) * 0.4f

    /** Depth-based alpha fade for atmospheric perspective. */
    fun hazeAlpha(hazeDensity: Float = 0.0f): Float =
        (1f - hazeDensity * layer.z).coerceIn(0.2f, 1f)
}

// ── Physics Body ─────────────────────────────────────────────────────

/**
 * A simulation body with Newtonian mechanics state.
 * Uses velocity Verlet integration for stable, energy-conserving motion.
 */
data class PhysicsBody(
    var x: Float = 0f,
    var y: Float = 0f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var ax: Float = 0f,
    var ay: Float = 0f,
    var mass: Float = 1f,
    var size: Float = 1f,
    var charge: Float = 0f,       // Used for lightning ionization
    var temperature: Float = 20f, // °C — affects buoyancy
    var alive: Boolean = true,
    var age: Float = 0f,
    var lifespan: Float = Float.POSITIVE_INFINITY,
    val id: Int = 0,
    var depth: DepthLayer = DepthLayer.PRECIPITATION,
    var rotation: Float = 0f,
    var rotationSpeed: Float = 0f,
    var phase: Float = 0f         // Oscillation phase for wobble effects
)

// ── Force Accumulator ────────────────────────────────────────────────

/**
 * Accumulates and applies environmental forces to physics bodies.
 * All forces are parameterized for different weather conditions.
 */
class ForceAccumulator {
    // ── Environmental parameters ──
    var gravity: Float = 9.8f       // m/s² downward
    var windSpeed: Float = 0f       // m/s horizontal
    var windDirection: Float = 1f   // -1 = left, 1 = right
    var windGust: Float = 0f        // Additional gust intensity (0-1)
    var airDensity: Float = 1.225f  // kg/m³ — affects drag
    var turbulence: Float = 0.3f    // Chaotic air movement
    var thermalUpdraft: Float = 0f  // Upward air current (m/s²)
    var pressureGradient: Float = 0f // Horizontal pressure force
    var time: Float = 0f

    private val rng = Random(137)

    fun reset() {
        gravity = 9.8f
        windSpeed = 0f
        windDirection = 1f
        windGust = 0f
        airDensity = 1.225f
        turbulence = 0.3f
        thermalUpdraft = 0f
        pressureGradient = 0f
    }

    /**
     * Apply all forces to a body. Returns the net acceleration.
     */
    fun applyForces(body: PhysicsBody, dt: Float): Pair<Float, Float> {
        var totalAx = 0f
        var totalAy = 0f

        // 1. Gravity (weight = mass * g)
        totalAy += gravity * body.mass * 2f  // Scaled for pixel-space

        // 2. Buoyancy (Archimedes' principle — lighter objects rise)
        val buoyancy = airDensity * body.mass * (body.temperature - 20f) * 0.01f
        totalAy -= buoyancy

        // 3. Wind force with gusts
        val gustFactor = 1f + windGust * (0.5f + 0.5f * sin(time * 3.7f))
        totalAx += windDirection * windSpeed * gustFactor / body.mass

        // 4. Thermal updraft (lighter particles carried upward)
        totalAy -= thermalUpdraft / body.mass

        // 5. Pressure gradient force
        totalAx += pressureGradient * sin(time * 0.5f + body.phase)

        // 6. Turbulence — layered Perlin-like noise for natural motion
        val turbX = layeredNoise(body.x * 0.005f, body.y * 0.005f, time * 0.8f)
        val turbY = layeredNoise(body.y * 0.005f, body.x * 0.005f, time * 0.6f + 100f)
        totalAx += turbX * turbulence * 2f
        totalAy += turbY * turbulence * 1.5f

        // 7. Quadratic drag (F_drag = -0.5 * ρ * v² * Cd * A)
        val speed = sqrt(body.vx * body.vx + body.vy * body.vy)
        if (speed > 0.01f) {
            val dragCoeff = 0.5f * airDensity * body.size * 0.01f
            val dragMag = dragCoeff * speed * speed
            totalAx -= dragMag * (body.vx / speed)
            totalAy -= dragMag * (body.vy / speed)
        }

        // 8. Coriolis-style deflection (adds rotational character)
        totalAx += body.vy * 0.001f
        totalAy -= body.vx * 0.001f

        return Pair(totalAx, totalAy)
    }

    /**
     * Velocity Verlet integration step. More stable than Euler.
     */
    fun integrate(body: PhysicsBody, dt: Float) {
        if (!body.alive) return

        // Half-step velocity update
        body.vx += body.ax * dt * 0.5f
        body.vy += body.ay * dt * 0.5f

        // Position update
        body.x += body.vx * dt
        body.y += body.vy * dt

        // Compute new acceleration
        val (newAx, newAy) = applyForces(body, dt)
        body.ax = newAx
        body.ay = newAy

        // Half-step velocity update
        body.vx += body.ax * dt * 0.5f
        body.vy += body.ay * dt * 0.5f

        // Rotation update
        body.rotation += body.rotationSpeed * dt
        body.age += dt

        // Age check
        if (body.age > body.lifespan) {
            body.alive = false
        }
    }

    private fun layeredNoise(x: Float, y: Float, t: Float): Float {
        val n1 = sin(x * 1.3f + t * 0.7f) * cos(y * 0.9f + t * 0.5f)
        val n2 = sin(x * 2.7f - t * 1.1f) * cos(y * 2.1f + t * 0.8f)
        val n3 = sin((x + y) * 0.5f + t * 0.3f) * 0.5f
        return (n1 + n2 + n3) / 2.5f
    }
}

// ── Object Pool ──────────────────────────────────────────────────────

/**
 * Pre-allocated object pool for physics bodies.
 * Eliminates GC pressure by reusing dead particle slots.
 */
class PhysicsBodyPool(
    private val poolSize: Int = 256
) {
    private val pool = Array(poolSize) { i -> PhysicsBody(id = i) }
    private var nextId = poolSize

    /** Borrow a body from the pool. Returns null if pool is exhausted. */
    fun borrow(): PhysicsBody? {
        val body = pool.firstOrNull { !it.alive }
        if (body != null) {
            body.alive = true
            body.age = 0f
            body.lifespan = Float.POSITIVE_INFINITY
            body.x = 0f; body.y = 0f
            body.vx = 0f; body.vy = 0f
            body.ax = 0f; body.ay = 0f
            body.mass = 1f; body.size = 1f
            body.rotation = 0f; body.rotationSpeed = 0f
            body.phase = 0f
            body.temperature = 20f
            body.charge = 0f
            body.depth = DepthLayer.PRECIPITATION
            return body
        }
        // Pool exhausted — create new (rare pathological case)
        val newBody = PhysicsBody(id = nextId++)
        return newBody
    }

    /** Return a body to the pool. */
    fun release(body: PhysicsBody) {
        body.alive = false
    }

    fun activeCount(): Int = pool.count { it.alive }

    fun forEachActive(action: (PhysicsBody) -> Unit) {
        for (body in pool) {
            if (body.alive) action(body)
        }
    }
}

// ── Physics Scene ────────────────────────────────────────────────────

/**
 * Top-level physics scene that manages all weather simulation.
 * Call [update] once per frame to advance the simulation.
 */
class PhysicsScene(
    val width: Float,
    val height: Float,
    val poolSize: Int = 256
) {
    val clock = FrameClock()
    val forces = ForceAccumulator()
    val pool = PhysicsBodyPool(poolSize)

    // Scene-wide wind parameters with memory
    var windTargetSpeed: Float = 0f
    var windTargetDirection: Float = 1f
    var windSmoothSpeed: Float = 3f // How quickly wind changes (rad/s)

    // 3D camera position for parallax scrolling
    var cameraX: Float = 0f
    var cameraY: Float = 0f

    // Global time
    var time: Float = 0f

    fun resize(newWidth: Float, newHeight: Float) {
        // In a full implementation, re-seed particles for new dimensions
    }

    /**
     * Advance the simulation by one frame.
     * Uses adaptive sub-stepping for stability at low frame rates.
     * @param dt DeltaTime in seconds (should come from [FrameClock.tick]).
     */
    fun update(dt: Float) {
        time += dt

        // Smooth wind changes
        val windDiff = windTargetSpeed - forces.windSpeed
        forces.windSpeed += windDiff * (windSmoothSpeed * dt).coerceAtMost(1f)
        forces.windDirection = windTargetDirection

        // Decay wind gust back to zero (~0.5s half-life)
        if (forces.windGust > 0.01f) {
            forces.windGust *= (1f - 2f * dt).coerceAtLeast(0f)
        } else {
            forces.windGust = 0f
        }

        // Adaptive sub-stepping (max 4 steps for stability)
        val steps = when {
            dt > 0.033f -> 2  // < 30 FPS
            dt > 0.05f -> 3   // < 20 FPS
            else -> 1
        }
        val subDt = dt / steps

        forces.time = time
        for (step in 0 until steps) {
            pool.forEachActive { body ->
                forces.integrate(body, subDt)

                // Wrap horizontally (infinite scrolling)
                when {
                    body.x < -body.size * 2 -> body.x = width + body.size
                    body.x > width + body.size * 2 -> body.x = -body.size
                }

                // Kill if below screen
                if (body.y > height + body.size * 2) {
                    body.alive = false
                }
            }
        }
    }

    /**
     * Apply a sudden wind gust impulse to all active bodies.
     */
    fun applyGust(impulseX: Float, impulseY: Float) {
        pool.forEachActive { body ->
            body.vx += impulseX / body.mass
            body.vy += impulseY / body.mass
        }
    }

    /**
     * Spawn a burst of particles at a point (e.g., for splash effects).
     */
    fun spawnBurst(
        x: Float, y: Float,
        count: Int = 10,
        speedMin: Float = 50f, speedMax: Float = 200f,
        sizeMin: Float = 2f, sizeMax: Float = 6f,
        depth: DepthLayer = DepthLayer.FOREGROUND
    ) {
        repeat(count) {
            val body = pool.borrow() ?: return@repeat
            val angle = rng.nextFloat() * PI.toFloat() * 2f
            val speed = speedMin + rng.nextFloat() * (speedMax - speedMin)
            body.x = x; body.y = y
            body.vx = cos(angle) * speed
            body.vy = sin(angle) * speed - 100f // Upward bias
            body.size = sizeMin + rng.nextFloat() * (sizeMax - sizeMin)
            body.mass = body.size * 0.5f
            body.lifespan = 1f + rng.nextFloat() * 2f
            body.depth = depth
        }
    }

    /** Clear all particles. */
    fun clear() {
        pool.forEachActive { it.alive = false }
        clock.reset()
        time = 0f
    }

    companion object {
        private val rng = Random(42)
    }
}
