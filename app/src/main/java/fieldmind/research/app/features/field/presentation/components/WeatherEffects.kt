package fieldmind.research.app.features.field.presentation.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.abs
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.pow
import kotlin.random.Random

/**
 * ════════════════════════════════════════════════════════════════════════
 *  🌦 WeatherEffects — All Atmospheric Phenomena Rendering
 *
 *  Renders weather effects using the [PhysicsScene] engine and
 *  [AtmosphericChemistry] color model. Each effect is a self-contained
 *  system that manages particle spawning, physics, and Canvas drawing.
 *
 *  Effects included:
 *  • RainSystem — Raindrops with wind shear, air resistance, splash
 *  • SnowSystem — Crystals with 6-fold symmetry, wobble, accumulation
 *  • CloudSystem — 3D volumetric clouds with depth parallax
 *  • FogSystem — Density field with chemical composition coloring
 *  • LightningSystem — Fractal branching with stepped leaders
 *  • RainbowSystem — Atmospheric optics (sun + water droplets)
 *  • StarSystem — Twinkling star field
 *  • GroundSystem — Procedural terrain with horizon
 * ════════════════════════════════════════════════════════════════════════
 */

// ── Rain System ──────────────────────────────────────────────────────

class RainSystem(
    private val physics: PhysicsScene
) {
    private var lastSpawn: Float = 0f
    private var rng = Random(111)

    /** Rain intensity parameters. */
    data class RainConfig(
        val density: Float = 0.6f,     // 0-1: how many drops
        val windShear: Float = 0.3f,   // Horizontal wind gradient
        val dropSizeMin: Float = 1.5f,  // Min drop radius (px)
        val dropSizeMax: Float = 4f,    // Max drop radius (px)
        val splashProbability: Float = 0.3f,
        val color: Color = Color(0xFF90CAF9)
    )

    fun update(dt: Float, config: RainConfig) {
        physics.forces.gravity = 15f // Rain falls faster
        physics.forces.airDensity = 1.2f
        physics.forces.turbulence = 0.15f

        val targetCount = (config.density * physics.poolSize * 0.4f).toInt().coerceAtLeast(10)
        val currentCount = physics.pool.activeCount()
        val spawnRate = ((targetCount - currentCount).coerceAtLeast(0) * dt * 10f).toInt()

        // Spawn raindrops
        lastSpawn += dt
        val spawnInterval = 0.02f / (config.density + 0.1f)
        while (lastSpawn > spawnInterval && currentCount < targetCount) {
            val body = physics.pool.borrow() ?: break
            val windOffset = config.windShear * (rng.nextFloat() - 0.5f) * 200f
            body.x = rng.nextFloat() * (physics.width + abs(windOffset)) - abs(windOffset) * 0.5f
            body.y = -body.size - rng.nextFloat() * 20f
            body.vx = physics.forces.windDirection * physics.forces.windSpeed * 30f + windOffset
            body.vy = 200f + rng.nextFloat() * 150f
            body.size = config.dropSizeMin + rng.nextFloat() * (config.dropSizeMax - config.dropSizeMin)
            body.mass = body.size * 0.8f
            body.lifespan = 3f + rng.nextFloat() * 2f
            body.depth = DepthLayer.PRECIPITATION
            body.temperature = 10f
            lastSpawn = 0f
        }
    }

    fun draw(scope: DrawScope, config: RainConfig, alpha: Float) {
        physics.pool.forEachActive { drop ->
            // Stretch drops based on velocity (motion blur effect)
            val speed = sqrt(drop.vx * drop.vx + drop.vy * drop.vy)
            val stretch = (speed * 0.03f).coerceAtLeast(1f)
            val angle = kotlin.math.atan2(drop.vy, drop.vx)

            if (drop.y < scope.size.height) {
                val endX = drop.x - cos(angle) * drop.size * stretch
                val endY = drop.y - sin(angle) * drop.size * stretch

                scope.drawLine(
                    color = config.color.copy(alpha = alpha * (0.4f + drop.mass * 0.3f)),
                    start = Offset(drop.x, drop.y),
                    end = Offset(endX, endY),
                    strokeWidth = drop.size * 0.8f,
                    cap = StrokeCap.Round
                )
            }

            // Splash effect on ground
            if (drop.y > scope.size.height * 0.85f && drop.vy > 100f) {
                if (rng.nextFloat() < config.splashProbability) {
                    physics.spawnBurst(
                        drop.x, scope.size.height * 0.85f,
                        count = 3, speedMin = 30f, speedMax = 80f,
                        sizeMin = 1f, sizeMax = 2f
                    )
                }
            }
        }

        // Draw splash particles
        physics.pool.forEachActive { body ->
            if (body.depth == DepthLayer.FOREGROUND && body.age < body.lifespan) {
                val fade = 1f - (body.age / body.lifespan)
                scope.drawCircle(
                    color = config.color.copy(alpha = alpha * fade * 0.5f),
                    radius = body.size,
                    center = Offset(body.x, body.y)
                )
            }
        }
    }
}

// ── Snow System ──────────────────────────────────────────────────────

class SnowSystem(
    private val physics: PhysicsScene
) {
    private var rng = Random(222)
    private var lastSpawn: Float = 0f

    data class SnowConfig(
        val density: Float = 0.4f,
        val windDrift: Float = 0.5f,
        val flakeSizeMin: Float = 2f,
        val flakeSizeMax: Float = 8f,
        val wobbleAmplitude: Float = 1.5f,
        val wobbleFrequency: Float = 2f,
        val color: Color = Color.White,
        val sparkleProbability: Float = 0.1f
    )

    fun update(dt: Float, config: SnowConfig) {
        physics.forces.gravity = 4f // Snow falls slower
        physics.forces.airDensity = 1.3f
        physics.forces.turbulence = 0.4f
        physics.forces.thermalUpdraft = 0.5f

        val targetCount = (config.density * physics.poolSize * 0.5f).toInt().coerceAtLeast(5)
        val currentCount = physics.pool.activeCount()

        lastSpawn += dt
        val spawnInterval = 0.03f / (config.density + 0.1f)
        while (lastSpawn > spawnInterval && currentCount < targetCount) {
            val body = physics.pool.borrow() ?: break
            body.x = rng.nextFloat() * physics.width * 1.2f - physics.width * 0.1f
            body.y = -body.size - rng.nextFloat() * 10f
            body.vx = physics.forces.windDirection * physics.forces.windSpeed * 10f + (rng.nextFloat() - 0.5f) * 20f
            body.vy = 40f + rng.nextFloat() * 60f
            body.size = config.flakeSizeMin + rng.nextFloat() * (config.flakeSizeMax - config.flakeSizeMin)
            body.mass = body.size * 0.3f // Snow is light
            body.lifespan = 5f + rng.nextFloat() * 3f
            body.depth = DepthLayer.PRECIPITATION
            body.rotationSpeed = (rng.nextFloat() - 0.5f) * 4f
            body.phase = rng.nextFloat() * PI.toFloat() * 2f
            body.temperature = -2f
            lastSpawn = 0f
        }
    }

    fun draw(scope: DrawScope, config: SnowConfig, alpha: Float) {
        physics.pool.forEachActive { flake ->
            // Wobble
            val wobbleOffset = sin(physics.time * config.wobbleFrequency + flake.phase) * config.wobbleAmplitude
            val drawX = flake.x + wobbleOffset
            val drawY = flake.y

            if (drawY > 0 && drawY < scope.size.height) {
                val flakeAlpha = alpha * (0.5f + (1f - flake.age / flake.lifespan) * 0.5f)
                val rotation = flake.rotation

                // Crystal glow
                scope.drawCircle(
                    color = config.color.copy(alpha = flakeAlpha * 0.15f),
                    radius = flake.size * 1.5f,
                    center = Offset(drawX, drawY)
                )

                // Main crystal
                val crystalRadius = flake.size / 2f
                scope.drawCircle(
                    color = config.color.copy(alpha = flakeAlpha),
                    radius = crystalRadius,
                    center = Offset(drawX, drawY)
                )

                // 6-fold symmetry arms (simplified — 3 lines through center at 60°)
                for (arm in 0 until 6) {
                    val angle = rotation + arm * PI.toFloat() / 3f
                    val armLen = crystalRadius * 1.2f
                    scope.drawLine(
                        color = config.color.copy(alpha = flakeAlpha * 0.6f),
                        start = Offset(drawX, drawY),
                        end = Offset(
                            drawX + cos(angle) * armLen,
                            drawY + sin(angle) * armLen
                        ),
                        strokeWidth = crystalRadius * 0.3f,
                        cap = StrokeCap.Round
                    )

                    // Side branches
                    val branchAngle1 = angle + PI.toFloat() / 6f
                    val branchAngle2 = angle - PI.toFloat() / 6f
                    val branchLength = armLen * 0.4f
                    val branchStart = 0.6f
                    scope.drawLine(
                        color = config.color.copy(alpha = flakeAlpha * 0.4f),
                        start = Offset(
                            drawX + cos(angle) * armLen * branchStart,
                            drawY + sin(angle) * armLen * branchStart
                        ),
                        end = Offset(
                            drawX + cos(angle) * armLen * branchStart + cos(branchAngle1) * branchLength,
                            drawY + sin(angle) * armLen * branchStart + sin(branchAngle1) * branchLength
                        ),
                        strokeWidth = crystalRadius * 0.2f,
                        cap = StrokeCap.Round
                    )
                    scope.drawLine(
                        color = config.color.copy(alpha = flakeAlpha * 0.4f),
                        start = Offset(
                            drawX + cos(angle) * armLen * branchStart,
                            drawY + sin(angle) * armLen * branchStart
                        ),
                        end = Offset(
                            drawX + cos(angle) * armLen * branchStart + cos(branchAngle2) * branchLength,
                            drawY + sin(angle) * armLen * branchStart + sin(branchAngle2) * branchLength
                        ),
                        strokeWidth = crystalRadius * 0.2f,
                        cap = StrokeCap.Round
                    )
                }

                // Sparkle (random bright highlights)
                if (rng.nextFloat() < config.sparkleProbability) {
                    scope.drawCircle(
                        color = Color.White.copy(alpha = flakeAlpha * 0.8f),
                        radius = 1f,
                        center = Offset(
                            drawX + (rng.nextFloat() - 0.5f) * crystalRadius,
                            drawY + (rng.nextFloat() - 0.5f) * crystalRadius
                        )
                    )
                }
            }
        }
    }
}

// ── Cloud System ─────────────────────────────────────────────────────

class CloudSystem(
    private val physics: PhysicsScene
) {
    private var rng = Random(333)
    private val cloudData = mutableListOf<CloudData>()

    data class CloudData(
        var x: Float,
        var y: Float,
        val width: Float,
        val height: Float,
        val opacity: Float,
        val depth: DepthLayer,
        val type: CloudMorphology,
        val billowCount: Int = 5,
        var driftPhase: Float = 0f
    )

    enum class CloudMorphology { CIRRUS, CUMULUS, STRATUS, CUMULONIMBUS, ALTOCUMULUS }

    data class CloudConfig(
        val coverage: Float = 0.3f,
        val thickness: Float = 0.5f,
        val windAffinity: Float = 1f,
        val baseColor: Color = Color.White,
        val shadowColor: Color = Color(0xFF78909C),
        val isDark: Boolean = false
    )

    fun initialize(width: Float, height: Float, count: Int = 6) {
        cloudData.clear()
        repeat(count) {
            val depth = when (rng.nextInt(3)) {
                0 -> DepthLayer.FAR_CLOUDS
                1 -> DepthLayer.MID_CLOUDS
                else -> DepthLayer.NEAR_CLOUDS
            }
            val type = when (rng.nextInt(5)) {
                0 -> CloudMorphology.CIRRUS
                1 -> CloudMorphology.CUMULUS
                2 -> CloudMorphology.STRATUS
                3 -> CloudMorphology.CUMULONIMBUS
                else -> CloudMorphology.ALTOCUMULUS
            }
            val (w, h) = cloudDimensions(type, width)
            cloudData.add(CloudData(
                x = rng.nextFloat() * width * 1.5f - width * 0.25f,
                y = height * (0.05f + rng.nextFloat() * 0.4f),
                width = w, height = h,
                opacity = cloudOpacity(type),
                depth = depth,
                type = type,
                billowCount = 4 + rng.nextInt(6),
                driftPhase = rng.nextFloat() * PI.toFloat() * 2f
            ))
        }
    }

    fun update(dt: Float, config: CloudConfig, windSpeed: Float, windDir: Float) {
        for (cloud in cloudData) {
            val speed = windSpeed * config.windAffinity * (0.3f + cloud.depth.z * 0.7f)
            cloud.x += windDir * speed * dt * 50f
            cloud.driftPhase += dt * (0.2f + cloud.depth.z * 0.5f)

            // Wrap horizontally
            if (cloud.x > physics.width + cloud.width) cloud.x = -cloud.width
            if (cloud.x < -cloud.width) cloud.x = physics.width + cloud.width
        }
    }

    fun draw(scope: DrawScope, config: CloudConfig, alpha: Float) {
        for (cloud in cloudData.sortedBy { it.depth.z }) {
            val parallaxScale = 0.5f + cloud.depth.z * 0.5f
            val drawAlpha = alpha * cloud.opacity * (config.coverage + 0.3f)
            val verticalDrift = sin(cloud.driftPhase) * cloud.height * 0.1f
            val cx = cloud.x
            val cy = cloud.y + verticalDrift

            when (cloud.type) {
                CloudMorphology.CIRRUS -> drawCirrus(scope, cx, cy, cloud, config, drawAlpha, parallaxScale)
                CloudMorphology.CUMULUS -> drawCumulus(scope, cx, cy, cloud, config, drawAlpha, parallaxScale)
                CloudMorphology.STRATUS -> drawStratus(scope, cx, cy, cloud, config, drawAlpha, parallaxScale)
                CloudMorphology.CUMULONIMBUS -> drawCumulonimbus(scope, cx, cy, cloud, config, drawAlpha, parallaxScale)
                CloudMorphology.ALTOCUMULUS -> drawAltocumulus(scope, cx, cy, cloud, config, drawAlpha, parallaxScale)
            }
        }
    }

    private fun cloudDimensions(type: CloudMorphology, canvasW: Float): Pair<Float, Float> = when (type) {
        CloudMorphology.CIRRUS -> Pair(canvasW * 0.3f, canvasW * 0.04f)
        CloudMorphology.CUMULUS -> Pair(canvasW * 0.2f, canvasW * 0.12f)
        CloudMorphology.STRATUS -> Pair(canvasW * 0.5f, canvasW * 0.06f)
        CloudMorphology.CUMULONIMBUS -> Pair(canvasW * 0.35f, canvasW * 0.22f)
        CloudMorphology.ALTOCUMULUS -> Pair(canvasW * 0.25f, canvasW * 0.06f)
    }

    private fun cloudOpacity(type: CloudMorphology): Float = when (type) {
        CloudMorphology.CIRRUS -> 0.3f
        CloudMorphology.CUMULUS -> 0.7f
        CloudMorphology.STRATUS -> 0.85f
        CloudMorphology.CUMULONIMBUS -> 0.95f
        CloudMorphology.ALTOCUMULUS -> 0.5f
    }

    private fun drawCirrus(scope: DrawScope, cx: Float, cy: Float, cloud: CloudData, config: CloudConfig, alpha: Float, scale: Float) {
        val path = Path().apply {
            moveTo(cx - cloud.width * 0.5f, cy)
            for (i in 0..8) {
                val t = i / 8f
                val px = cx - cloud.width * 0.5f + t * cloud.width
                val py = cy + sin(t * PI.toFloat() * 4f + cloud.driftPhase) * cloud.height * 0.5f
                lineTo(px, py)
            }
        }
        scope.drawPath(
            path,
            config.baseColor.copy(alpha = alpha * 0.5f),
            style = Stroke(width = 2f * scale, cap = StrokeCap.Round)
        )
        // Second wispy layer
        val path2 = Path().apply {
            moveTo(cx - cloud.width * 0.4f, cy - cloud.height * 0.3f)
            for (i in 0..7) {
                val t = i / 7f
                val px = cx - cloud.width * 0.4f + t * cloud.width * 0.8f
                val py = cy - cloud.height * 0.3f + sin(t * PI.toFloat() * 5f + cloud.driftPhase * 0.7f) * cloud.height * 0.3f
                lineTo(px, py)
            }
        }
        scope.drawPath(
            path2,
            config.baseColor.copy(alpha = alpha * 0.3f),
            style = Stroke(width = 1.5f * scale, cap = StrokeCap.Round)
        )
    }

    private fun drawCumulus(scope: DrawScope, cx: Float, cy: Float, cloud: CloudData, config: CloudConfig, alpha: Float, scale: Float) {
        val billowRadius = cloud.width * 0.08f * scale
        for (i in 0 until cloud.billowCount) {
            val angle = i.toFloat() / cloud.billowCount * PI.toFloat() * 2f
            val dist = (billowRadius * 0.7f)
            val bx = cx + cos(angle + cloud.driftPhase * 0.3f) * dist
            val by = cy + sin(angle + cloud.driftPhase * 0.3f) * dist * 0.6f
            val br = billowRadius * (0.6f + sin(i * 1.7f + cloud.driftPhase) * 0.2f)

            // Bottom shadow
            scope.drawCircle(
                color = config.shadowColor.copy(alpha = alpha * 0.3f),
                radius = br,
                center = Offset(bx, by + br * 0.15f)
            )
            // Main puff
            scope.drawCircle(
                color = config.baseColor.copy(alpha = alpha * 0.6f),
                radius = br,
                center = Offset(bx, by)
            )
            // Top highlight
            scope.drawCircle(
                color = Color.White.copy(alpha = alpha * 0.3f),
                radius = br * 0.6f,
                center = Offset(bx - br * 0.15f, by - br * 0.2f)
            )
        }
    }

    private fun drawStratus(scope: DrawScope, cx: Float, cy: Float, cloud: CloudData, config: CloudConfig, alpha: Float, scale: Float) {
        val path = Path().apply {
            moveTo(cx - cloud.width * 0.5f, cy)
            for (i in 0..12) {
                val t = i / 12f
                val px = cx - cloud.width * 0.5f + t * cloud.width
                val py = cy + sin(t * PI.toFloat() * 3f + cloud.driftPhase) * cloud.height * 0.3f
                lineTo(px, py)
            }
            lineTo(cx + cloud.width * 0.5f, cy + cloud.height)
            for (i in 12 downTo 0) {
                val t = i / 12f
                val px = cx - cloud.width * 0.5f + t * cloud.width
                val py = cy + cloud.height + sin(t * PI.toFloat() * 3f + cloud.driftPhase + 1f) * cloud.height * 0.2f
                lineTo(px, py)
            }
            close()
        }
        scope.drawPath(
            path,
            config.baseColor.copy(alpha = alpha * 0.7f),
            style = Fill
        )
    }

    private fun drawCumulonimbus(scope: DrawScope, cx: Float, cy: Float, cloud: CloudData, config: CloudConfig, alpha: Float, scale: Float) {
        // Massive anvil-shaped cloud
        val billowCount = cloud.billowCount + 3
        for (i in 0 until billowCount) {
            val t = i.toFloat() / billowCount
            val bx = cx - cloud.width * 0.45f + t * cloud.width * 0.9f
            val by = cy + sin(t * PI.toFloat() * 3f + cloud.driftPhase) * cloud.height * 0.15f
            val br = cloud.width * 0.06f * scale * (0.7f + sin(t * 4f + cloud.driftPhase) * 0.3f)

            // Shadow
            scope.drawCircle(
                color = config.shadowColor.copy(alpha = (alpha * 0.5f).coerceAtMost(1f)),
                radius = br,
                center = Offset(bx, by + br * 0.2f)
            )
            // Dark base
            val baseColor = if (config.isDark) Color(0xFF37474F) else Color(0xFF546E7A)
            scope.drawCircle(
                color = baseColor.copy(alpha = (alpha * 0.7f).coerceAtMost(1f)),
                radius = br * 0.9f,
                center = Offset(bx, by)
            )
            // Top highlight
            scope.drawCircle(
                color = Color.White.copy(alpha = (alpha * 0.2f).coerceAtMost(1f)),
                radius = br * 0.5f,
                center = Offset(bx - br * 0.1f, by - br * 0.3f)
            )
        }
        // Flat anvil top
        scope.drawRect(
            color = Color(0xFF78909C).copy(alpha = (alpha * 0.3f).coerceAtMost(0.4f)),
            topLeft = Offset(cx - cloud.width * 0.5f, cy - cloud.height * 0.4f),
            size = androidx.compose.ui.geometry.Size(cloud.width * 1.2f, cloud.height * 0.15f)
        )
    }

    private fun drawAltocumulus(scope: DrawScope, cx: Float, cy: Float, cloud: CloudData, config: CloudConfig, alpha: Float, scale: Float) {
        val rows = 3
        val cols = 5
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val cellX = cx - cloud.width * 0.4f + c * (cloud.width / cols)
                val cellY = cy - cloud.height * 0.3f + r * (cloud.height / rows)
                val cellR = cloud.width * 0.03f * scale
                val phaseOffset = r * 2f + c * 1.5f
                val wobble = sin(cloud.driftPhase * 0.5f + phaseOffset) * cellR * 0.3f

                scope.drawCircle(
                    color = config.baseColor.copy(alpha = (alpha * 0.4f).coerceAtMost(1f)),
                    radius = cellR,
                    center = Offset(cellX + wobble, cellY)
                )
            }
        }
    }

    fun clear() { cloudData.clear() }
}

// ── Fog System ───────────────────────────────────────────────────────

class FogSystem {
    data class FogConfig(
        val density: Float = 0.4f,
        val layerCount: Int = 4,
        val baseColor: Color = Color(0xFFB0BEC5),
        val windInfluence: Float = 0.3f
    )

    fun draw(scope: DrawScope, config: FogConfig, time: Float, windSpeed: Float, windDir: Float, alpha: Float) {
        val w = scope.size.width
        val h = scope.size.height

        for (band in 0 until config.layerCount) {
            val bandY = h * (0.1f + band * 0.25f)
            val bandH = h * (0.08f + config.density * 0.1f)
            val driftPhase = time * (0.2f + band * 0.15f) + windDir * windSpeed * time * 0.1f

            val path = Path().apply {
                moveTo(0f, bandY)
                for (x in 0..24) {
                    val px = x * w / 24f
                    val py = bandY +
                        sin(px * 0.01f + driftPhase) * bandH * 0.5f +
                        sin(px * 0.003f + driftPhase * 0.5f + band) * bandH * 0.3f
                    lineTo(px, py)
                }
                lineTo(w, bandY + bandH)
                lineTo(0f, bandY + bandH)
                close()
            }

            val layerAlpha = alpha * config.density * (0.3f + band * 0.2f) * 0.4f
            scope.drawPath(
                path,
                config.baseColor.copy(alpha = layerAlpha.coerceAtMost(0.3f)),
                style = Fill
            )
        }
    }
}

// ── Lightning System ─────────────────────────────────────────────────

class LightningSystem(
    private val physics: PhysicsScene
) {
    private val bolts = mutableListOf<LightningBoltData>()
    private var rng = Random(777)
    private var flashIntensity: Float = 0f

    data class LightningBoltData(
        val segments: List<Pair<Offset, Offset>>,
        val branches: List<List<Pair<Offset, Offset>>>,
        val startTime: Float,
        val duration: Float,
        val intensity: Float,
        val color: Color
    )

    data class LightningConfig(
        val frequency: Float = 0.02f,    // Probability per second
        val intensity: Float = 0.8f,
        val boltColor: Color = Color(0xFFE0E8FF),
        val coreColor: Color = Color.White,
        val flashColor: Color = Color.White
    )

    private var timeSinceLastStrike: Float = 0f

    fun update(dt: Float, config: LightningConfig, cloudBase: Float) {
        timeSinceLastStrike += dt

        // Random lightning trigger
        if (timeSinceLastStrike > 2f && rng.nextFloat() < config.frequency * dt * 20f) {
            val startX = rng.nextFloat() * physics.width
            val startY = cloudBase
            val endX = startX + (rng.nextFloat() - 0.5f) * 200f
            val endY = physics.height * 0.85f
            triggerBolt(startX, startY, endX, endY, config)
            timeSinceLastStrike = 0f
        }

        // Remove expired bolts
        bolts.removeAll { physics.time - it.startTime > it.duration }

        // Flash intensity fades
        flashIntensity = bolts.maxOfOrNull { bolt ->
            val elapsed = physics.time - bolt.startTime
            val progress = (elapsed / bolt.duration).coerceIn(0f, 1f)
            (1f - progress) * bolt.intensity * 0.15f
        } ?: 0f
    }

    fun triggerBolt(
        startX: Float, startY: Float,
        endX: Float, endY: Float,
        config: LightningConfig
    ) {
        val segments = generateBoltPath(startX, startY, endX, endY, 6)
        val branches = mutableListOf<List<Pair<Offset, Offset>>>()

        // Add 2-3 branches
        repeat(2 + rng.nextInt(2)) {
            val splitT = 0.2f + rng.nextFloat() * 0.5f
            val splitIdx = (splitT * segments.size).toInt().coerceIn(1, segments.size - 1)
            val (sx, sy) = segments[splitIdx]
            val bx = sx + (rng.nextFloat() - 0.5f) * physics.width * 0.15f
            val by = sy + rng.nextFloat() * physics.height * 0.2f
            branches.add(generateBoltPath(sx, sy, bx, by, 3))
        }

        bolts.add(LightningBoltData(
            segments = segments,
            branches = branches,
            startTime = physics.time,
            duration = 0.3f + rng.nextFloat() * 0.2f,
            intensity = config.intensity * (0.8f + rng.nextFloat() * 0.2f),
            color = config.boltColor
        ))
    }

    private fun generateBoltPath(
        startX: Float, startY: Float,
        endX: Float, endY: Float,
        detail: Int
    ): List<Pair<Offset, Offset>> {
        val segments = mutableListOf<Pair<Offset, Offset>>()
        var x = startX; var y = startY
        val steps = detail.coerceAtLeast(2)
        val dx = (endX - startX) / steps
        val dy = (endY - startY) / steps

        for (i in 0 until steps) {
            val nx = x + dx + (rng.nextFloat() - 0.5f) * physics.width * 0.04f * (1f - i.toFloat() / steps)
            val ny = y + dy
            segments.add(Pair(Offset(x, y), Offset(nx, ny)))
            x = nx; y = ny
        }
        // Final segment to exact endpoint
        segments.add(Pair(Offset(x, y), Offset(endX, endY)))
        return segments
    }

    fun draw(scope: DrawScope, config: LightningConfig) {
        // Flash overlay
        if (flashIntensity > 0.01f) {
            scope.drawRect(
                color = config.flashColor.copy(alpha = flashIntensity),
                size = scope.size
            )
        }

        // Draw bolts
        for (bolt in bolts) {
            val elapsed = physics.time - bolt.startTime
            val progress = (elapsed / bolt.duration).coerceIn(0f, 1f)
            val boltAlpha = (1f - progress) * bolt.intensity

            // Core (bright center)
            for ((start, end) in bolt.segments) {
                scope.drawLine(
                    color = config.coreColor.copy(alpha = boltAlpha),
                    start = start, end = end,
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
                // Glow
                scope.drawLine(
                    color = config.boltColor.copy(alpha = boltAlpha * 0.4f),
                    start = start, end = end,
                    strokeWidth = 7f,
                    cap = StrokeCap.Round
                )
            }

            // Branches (thinner)
            for (branch in bolt.branches) {
                for ((start, end) in branch) {
                    scope.drawLine(
                        color = config.coreColor.copy(alpha = boltAlpha * 0.6f),
                        start = start, end = end,
                        strokeWidth = 1.5f,
                        cap = StrokeCap.Round
                    )
                    scope.drawLine(
                        color = config.boltColor.copy(alpha = boltAlpha * 0.2f),
                        start = start, end = end,
                        strokeWidth = 4f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }

    fun clear() { bolts.clear(); flashIntensity = 0f }
}

// ── Rainbow System ───────────────────────────────────────────────────

class RainbowSystem {
    data class RainbowConfig(
        val probability: Float = 0.3f,
        val intensity: Float = 0.6f
    )

    fun draw(scope: DrawScope, config: RainbowConfig, sunAltitude: Float, isRaining: Boolean, alpha: Float) {
        // Rainbow visible when sun is behind observer (low angle) and raining in front
        if (!isRaining || sunAltitude > 0.5f || sunAltitude < -0.1f) return

        val w = scope.size.width
        val h = scope.size.height
        val centerX = w * 0.5f
        val centerY = h * 0.85f
        val radius = h * 0.6f

        // Rainbow arc (ROYGBIV)
        val rainbowColors = listOf(
            Color(0xFFFF0000), // Red
            Color(0xFFFF7F00), // Orange
            Color(0xFFFFFF00), // Yellow
            Color(0xFF00FF00), // Green
            Color(0xFF0000FF), // Blue
            Color(0xFF4B0082), // Indigo
            Color(0xFF8B00FF)  // Violet
        )

        val bandWidth = radius * 0.03f
        for ((i, color) in rainbowColors.withIndex()) {
            val r = radius + i * bandWidth * 1.2f
            // Draw arc using Path
            val path = Path().apply {
                moveTo(centerX - r, centerY)
                for (angle in 0..180) {
                    val rad = angle * PI.toFloat() / 180f
                    val px = centerX + cos(rad) * r
                    val py = centerY - sin(rad) * r
                    lineTo(px, py)
                }
            }
            scope.drawPath(
                path,
                color.copy(alpha = alpha * config.intensity * 0.3f),
                style = Stroke(width = bandWidth)
            )
        }

        // Secondary rainbow (fainter, reversed colors)
        if (config.intensity > 0.4f) {
            val r2 = radius * 1.3f
            val bandWidth2 = radius * 0.02f
            val reversedColors = rainbowColors.reversed()
            for ((i, color) in reversedColors.withIndex()) {
                val r = r2 + i * bandWidth2 * 1.1f
                val path = Path().apply {
                    moveTo(centerX - r, centerY)
                    for (angle in 0..165) {
                        val rad = angle * PI.toFloat() / 180f
                        val px = centerX + cos(rad) * r
                        val py = centerY - sin(rad) * r
                        lineTo(px, py)
                    }
                }
                scope.drawPath(
                    path,
                    color.copy(alpha = alpha * config.intensity * 0.15f),
                    style = Stroke(width = bandWidth2)
                )
            }
        }
    }
}

// ── Star System ──────────────────────────────────────────────────────

class StarSystem {
    private var stars = mutableListOf<StarData>()

    data class StarData(
        val x: Float, val y: Float,
        val baseBrightness: Float,
        val twinkleSpeed: Float,
        val twinklePhase: Float,
        val size: Float,
        val color: Color
    )

    fun initialize(count: Int, rng: Random = Random(42)) {
        stars.clear()
        repeat(count) {
            stars.add(StarData(
                x = rng.nextFloat(),
                y = rng.nextFloat() * 0.6f, // Stars only in upper portion
                baseBrightness = 0.3f + rng.nextFloat() * 0.7f,
                twinkleSpeed = 1.5f + rng.nextFloat() * 3f,
                twinklePhase = rng.nextFloat() * PI.toFloat() * 2f,
                size = 0.5f + rng.nextFloat() * 2f,
                color = when (rng.nextInt(5)) {
                    0 -> Color(0xFFFFF9C4) // Warm
                    1 -> Color(0xFFE3F2FD) // Blue-white
                    2 -> Color(0xFFF3E5F5) // Purple
                    else -> Color.White
                }
            ))
        }
    }

    fun draw(scope: DrawScope, time: Float, alpha: Float) {
        val w = scope.size.width
        val h = scope.size.height

        for (star in stars) {
            val twinkle = sin(time * star.twinkleSpeed + star.twinklePhase) * 0.5f + 0.5f
            val brightness = star.baseBrightness * (0.4f + twinkle * 0.6f)
            val starAlpha = (alpha * brightness).coerceIn(0f, 1f)

            if (starAlpha > 0.05f) {
                // Glow
                scope.drawCircle(
                    color = star.color.copy(alpha = starAlpha * 0.15f),
                    radius = star.size * 2.5f,
                    center = Offset(star.x * w, star.y * h)
                )
                // Core
                scope.drawCircle(
                    color = star.color.copy(alpha = starAlpha),
                    radius = star.size * 0.7f,
                    center = Offset(star.x * w, star.y * h)
                )
            }
        }
    }
}

// ── Ground System ────────────────────────────────────────────────────

class GroundSystem {
    data class GroundConfig(
        val color: Color = Color(0xFF2A5A2A),
        val detailColor: Color = Color(0xFF1A3A1A),
        val horizonHeight: Float = 0.82f,
        val hilliness: Float = 1f,
        val isSnow: Boolean = false,
        val isDark: Boolean = false,
        val alpha: Float = 0.4f
    )

    fun draw(scope: DrawScope, config: GroundConfig, time: Float) {
        val w = scope.size.width
        val h = scope.size.height
        val groundY = h * config.horizonHeight

        val path = Path().apply {
            moveTo(-10f, h + 10f)
            lineTo(-10f, groundY)
            for (i in 0..60) {
                val t = i.toFloat() / 60f
                val px = t * (w + 20f) - 10f
                val hill = sin(t * 3.5f) * 0.04f +
                    sin(t * 8f + 1.1f) * 0.025f +
                    sin(t * 16f + 3.3f + time * 0.1f) * 0.012f
                val py = groundY - hill * h * config.hilliness
                lineTo(px, py)
            }
            lineTo(w + 10f, h + 10f)
            close()
        }

        val snowMul = if (config.isSnow) 1.5f else 1f
        val nightMul = if (config.isDark) 0.5f else 1f
        val groundColor = config.color.copy(
            red = (config.color.red * snowMul * nightMul).coerceAtMost(1f),
            green = (config.color.green * snowMul * nightMul).coerceAtMost(1f),
            blue = (config.color.blue * snowMul * nightMul).coerceAtMost(1f),
            alpha = (config.alpha * (if (config.isSnow) 1.3f else 1f)).coerceAtMost(0.5f)
        )

        scope.drawPath(path, groundColor, style = Fill)
    }
}
