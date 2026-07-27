package fieldmind.research.app.features.field.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.isActive
import kotlin.random.Random
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.abs
import kotlin.math.PI

/**
 * ════════════════════════════════════════════════════════════════════════
 *  🌦 AnimatedWeatherScene — 3D Physics-Based Weather Animations
 *
 *  Complete rewrite using:
 *  • [WeatherPhysicsEngine] — Newtonian mechanics, 3D depth, force accumulation
 *  • [AtmosphericChemistry] — Rayleigh/Mie scattering, chemical air masses
 *  • [WeatherEffects] — Rain, snow, clouds, fog, lightning, rainbow, stars
 *
 *  Features:
 *  • Frame-rate independent deltaTime simulation (via withFrameNanos loop)
 *  • 3D parallax depth layers for immersive depth perception
 *  • Touch interaction (tap for ripples/sparkles/wind gusts)
 *  • Adaptive performance detection
 *  • Chemistry-based sky colors derived from solar position and temperature
 * ════════════════════════════════════════════════════════════════════════
 */

// ── Time-of-day enum (kept for backward compatibility) ───────────────

enum class TimeOfDay {
    Dawn, Sunrise, Morning, Midday, Afternoon, Sunset, Twilight, Night
}

// ── Weather Palette (kept for backward compatibility) ────────────────

data class WeatherPalette(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val accent: Color,
    val background: List<Color>,
    val sunColor: Color = Color(0xFFFFF176),
    val sunGlowColor: Color = Color(0xFFFFF9C4),
    val sunFlareColor: Color = Color(0xFFFFAB91),
    val moonColor: Color = Color(0xFFECEFF1),
    val moonGlowColor: Color = Color(0xFFE3F2FD),
    val cloudBaseColor: Color = Color.White,
    val hazeColor: Color = Color.Transparent,
    val groundColor: Color = Color(0xFF4A7A4A),
    val groundDetailColor: Color = Color(0xFF4A6A4A)
)

// ── Internal time-of-day helpers ─────────────────────────────────────

private fun computeTimeOfDay(): TimeOfDay {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 4..5 -> TimeOfDay.Dawn
        in 6..7 -> TimeOfDay.Sunrise
        in 8..10 -> TimeOfDay.Morning
        in 11..14 -> TimeOfDay.Midday
        in 15..16 -> TimeOfDay.Afternoon
        in 17..18 -> TimeOfDay.Sunset
        in 19..20 -> TimeOfDay.Twilight
        else -> TimeOfDay.Night
    }
}

private fun timeOfDayToHour(tod: TimeOfDay): Int = when (tod) {
    TimeOfDay.Dawn -> 5
    TimeOfDay.Sunrise -> 7
    TimeOfDay.Morning -> 9
    TimeOfDay.Midday -> 12
    TimeOfDay.Afternoon -> 15
    TimeOfDay.Sunset -> 17
    TimeOfDay.Twilight -> 19
    TimeOfDay.Night -> 23
}

private fun sunVerticalY(timeOfDay: TimeOfDay, height: Float): Float = height * when (timeOfDay) {
    TimeOfDay.Dawn -> 0.72f; TimeOfDay.Sunrise -> 0.55f; TimeOfDay.Morning -> 0.32f
    TimeOfDay.Midday -> 0.10f; TimeOfDay.Afternoon -> 0.32f; TimeOfDay.Sunset -> 0.55f
    TimeOfDay.Twilight -> 0.72f; TimeOfDay.Night -> 1.20f
}

private fun moonVerticalY(timeOfDay: TimeOfDay, height: Float): Float = height * when (timeOfDay) {
    TimeOfDay.Dawn -> 0.78f; TimeOfDay.Sunrise -> 0.65f; TimeOfDay.Morning -> 0.60f
    TimeOfDay.Midday -> 0.55f; TimeOfDay.Afternoon -> 0.50f; TimeOfDay.Sunset -> 0.72f
    TimeOfDay.Twilight -> 0.42f; TimeOfDay.Night -> 0.15f
}

// ── Public API — AnimatedWeatherScene ────────────────────────────────

/**
 * Physics-based animated weather scene.
 * Maintains the same public API as the original for backward compatibility.
 *
 * @param weatherCode WMO weather code (0=clear, 1=mainly clear, 2=partly cloudy,
 *                    3=overcast, 45-48=fog, 51-67=rain, 71-86=snow, 95+=thunderstorm)
 * @param temperature Current temperature in °C
 * @param sunrise ISO sunrise time (kept for API compat, not used directly)
 * @param sunset ISO sunset time (kept for API compat, not used directly)
 * @param modifier Compose modifier
 * @param compact Whether to use a compact (lower quality) rendering
 * @param forceNight Force night mode when true, day when false
 * @param showCloudAnimation Whether to animate clouds
 */
@Composable
fun AnimatedWeatherScene(
    weatherCode: Int,
    temperature: Double?,
    sunrise: String? = null,
    sunset: String? = null,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    forceNight: Boolean? = null,
    showCloudAnimation: Boolean = true
) {
    val computedTimeOfDay = computeTimeOfDay()
    val timeOfDay = when (forceNight) {
        true -> TimeOfDay.Night
        false -> TimeOfDay.Midday
        null -> computedTimeOfDay
    }
    val hour = timeOfDayToHour(timeOfDay)

    // Generate chemistry-based palette once per weather/temperature/time change
    val palette = remember(weatherCode, temperature, hour) {
        WeatherPaletteGenerator.generate(
            weatherCode = weatherCode,
            temperature = temperature,
            hour = hour
        )
    }

    // Preview/inspection mode: static rendering
    if (LocalInspectionMode.current) {
        StaticWeatherFrame(weatherCode, palette, modifier)
        return
    }

    // ── Scene state (hoisted via onSizeChanged, not set inside Canvas) ──
    var sceneWidth by remember { mutableFloatStateOf(1f) }
    var sceneHeight by remember { mutableFloatStateOf(1f) }

    // Physics engine & effects (remember across recompositions)
    val physics = remember { PhysicsScene(1f, 1f) }
    val rainSystem = remember { RainSystem(physics) }
    val snowSystem = remember { SnowSystem(physics) }
    val cloudSystem = remember { CloudSystem(physics) }
    val fogSystem = remember { FogSystem() }
    val lightningSystem = remember { LightningSystem(physics) }
    val rainbowSystem = remember { RainbowSystem() }
    val starSystem = remember { StarSystem() }
    val groundSystem = remember { GroundSystem() }

    // Initialize star field once
    remember(compact) { starSystem.initialize(if (compact) 20 else 60) }

    // Weather condition flags (derived from weatherCode)
    val isClear = weatherCode <= 1
    val isCloudy = weatherCode in 2..3 || weatherCode == -1
    val isRain = weatherCode in 51..67 || weatherCode in 80..82
    val isSnow = weatherCode in 71..77 || weatherCode in 85..86
    val isFog = weatherCode in 45..48
    val isThunder = weatherCode >= 95

    val cloudIntensity = when {
        weatherCode in 2..3 -> 0.85f
        weatherCode in 51..67 -> 0.9f
        weatherCode >= 95 -> 1.0f
        showCloudAnimation && weatherCode in 0..1 -> 0.25f
        else -> 0f
    }

    val windSpeed = when {
        isThunder -> 0.6f
        isRain -> 0.4f
        isSnow -> 0.2f
        else -> 0.1f
    }
    val windDir = 1f

    val isNight = timeOfDay == TimeOfDay.Night || timeOfDay == TimeOfDay.Twilight
    val isDaytime = !isNight && timeOfDay != TimeOfDay.Dawn
    val isDawnDusk = timeOfDay == TimeOfDay.Dawn || timeOfDay == TimeOfDay.Twilight ||
                     timeOfDay == TimeOfDay.Sunrise || timeOfDay == TimeOfDay.Sunset

    // Initialize/reset systems when weather changes
    LaunchedEffect(weatherCode, sceneWidth, sceneHeight) {
        physics.resize(sceneWidth.coerceAtLeast(1f), sceneHeight.coerceAtLeast(1f))
        physics.windTargetSpeed = windSpeed
        physics.windTargetDirection = windDir

        when {
            isRain || isSnow -> {
                // Precipitation uses physics pool — no cloud initialization needed
            }
            cloudIntensity > 0f && !isFog -> {
                cloudSystem.initialize(
                    physics.width, physics.height,
                    count = if (compact) 3 else 6
                )
            }
            else -> {
                cloudSystem.clear()
                lightningSystem.clear()
            }
        }
    }

    // ── Frame loop: physics simulation runs at draw-frame rate ──
    // DeltaTime is sourced from physics.clock ONCE per frame and shared
    // across all subsystems to keep the simulation coherent.
    LaunchedEffect(Unit) {
        while (isActive) {
            withFrameNanos { frameNanos ->
                // 1. Tick the clock once — this is the single source of dt
                val dt = physics.clock.tick(frameNanos)

                // 2. Update physics (integrates all active bodies)
                physics.update(dt)

                // 3. Update effects with the same deltaTime
                if (isRain) {
                    rainSystem.update(dt, RainSystem.RainConfig(
                        density = if (compact) 0.3f else 0.6f,
                        windShear = 0.3f,
                        color = Color(0xFF90CAF9)
                    ))
                }
                if (isSnow) {
                    snowSystem.update(dt, SnowSystem.SnowConfig(
                        density = if (compact) 0.3f else 0.5f,
                        windDrift = 0.5f,
                        color = Color.White
                    ))
                }
                if (cloudIntensity > 0f && !isFog) {
                    cloudSystem.update(dt, CloudSystem.CloudConfig(
                        coverage = cloudIntensity,
                        baseColor = palette.cloudBaseColor,
                        shadowColor = palette.primary,
                        isDark = isNight
                    ), windSpeed, windDir)
                }
                if (isThunder) {
                    lightningSystem.update(dt, LightningSystem.LightningConfig(
                        frequency = if (compact) 0.01f else 0.025f,
                        intensity = 0.9f
                    ), sceneHeight * 0.2f)
                }
            }
        }
    }

    // ── Sun/Moon twinkle animation (used by draw helpers) ──
    val infiniteTransition = rememberInfiniteTransition(label = "weatherTransition")
    val glow by infiniteTransition.animateFloat(0.6f, 1f,
        infiniteRepeatable(tween(5000, easing = LinearEasing), RepeatMode.Reverse), label = "sceneGlow")
    val drift by infiniteTransition.animateFloat(0f, 1f,
        infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Restart), label = "sceneDrift")
    val drift2 by infiniteTransition.animateFloat(0f, 1f,
        infiniteRepeatable(tween(8500, easing = LinearEasing), RepeatMode.Restart), label = "sceneDrift2")

    // ── Main render surface ──
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { size ->
                    sceneWidth = size.width.toFloat()
                    sceneHeight = size.height.toFloat()
                }
                .pointerInput(Unit) {
                    // Touch interaction: tap to create ripples / wind gusts
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: continue
                            if (change.pressed) {
                                change.consume()
                                val tapX = change.position.x
                                val tapY = change.position.y
                                when {
                                    isRain || isSnow -> {
                                        physics.spawnBurst(
                                            tapX, tapY,
                                            count = 8, speedMin = 30f, speedMax = 150f,
                                            sizeMin = 1f, sizeMax = 3f
                                        )
                                    }
                                    isThunder -> {
                                        lightningSystem.triggerBolt(
                                            tapX, tapY * 0.2f,
                                            tapX + (Random.nextFloat() - 0.5f) * 200f,
                                            sceneHeight * 0.85f,
                                            LightningSystem.LightningConfig()
                                        )
                                    }
                                    else -> {
                                        physics.forces.windGust = 1f
                                    }
                                }
                            }
                        }
                    }
                }
        ) {
            // ── 1. Sky background (chemistry-based gradient) ──
            val bgColors = when {
                palette.background.size >= 3 -> palette.background
                palette.background.size == 2 -> listOf(palette.background[0], palette.tertiary, palette.background[1])
                else -> listOf(palette.primary, palette.tertiary, palette.secondary)
            }
            drawRect(Brush.verticalGradient(bgColors, 0f, size.height), size = size)

            // Subtle radial glow from sun/moon position
            val glowCenterX = size.width * if (isDaytime || isDawnDusk) 0.85f else 0.82f
            val glowCenterY = if (isDaytime || isDawnDusk) {
                sunVerticalY(timeOfDay, size.height)
            } else {
                moonVerticalY(timeOfDay, size.height)
            }
            if (glowCenterY < size.height) {
                drawRect(
                    Brush.radialGradient(
                        listOf(palette.accent.copy(alpha = 0.06f * glow), Color.Transparent),
                        center = Offset(glowCenterX, glowCenterY),
                        radius = size.maxDimension * 0.5f
                    ),
                    size = size
                )
            }

            // ── 2. Stars (nighttime only) ──
            if (isNight) {
                starSystem.draw(this, physics.time, 0.8f)
            }

            // ── 3. Sun (daytime) ──
            if (isDaytime || isDawnDusk) {
                drawSun(palette, timeOfDay, glow, compact)
            }

            // ── 4. Moon (nighttime) ──
            if (isNight || isDawnDusk) {
                drawMoon(palette, timeOfDay, glow, compact)
            }

            // ── 5. Clouds (drawn during frame loop, render here) ──
            if (cloudIntensity > 0f && !isFog) {
                val cloudAlpha = if (isNight) 0.25f * cloudIntensity else 0.38f * cloudIntensity
                cloudSystem.draw(this, CloudSystem.CloudConfig(
                    coverage = cloudIntensity,
                    baseColor = palette.cloudBaseColor,
                    shadowColor = palette.primary,
                    isDark = isNight
                ), cloudAlpha)
            }

            // ── 6. Rain ──
            if (isRain) {
                val rainAlpha = if (isNight) 0.35f else 0.28f
                rainSystem.draw(this, RainSystem.RainConfig(
                    density = if (compact) 0.3f else 0.6f,
                    windShear = 0.3f,
                    color = Color(0xFF90CAF9)
                ), rainAlpha)
            }

            // ── 7. Snow ──
            if (isSnow) {
                val snowAlpha = if (isNight) 0.7f else 0.55f
                snowSystem.draw(this, SnowSystem.SnowConfig(
                    density = if (compact) 0.3f else 0.5f,
                    windDrift = 0.5f,
                    color = Color.White
                ), snowAlpha)
            }

            // ── 8. Fog ──
            if (isFog) {
                val fogAlpha = if (isNight) 0.25f else 0.30f
                fogSystem.draw(this, FogSystem.FogConfig(
                    density = 0.4f,
                    baseColor = palette.cloudBaseColor
                ), physics.time, windSpeed, windDir, fogAlpha)
            }

            // ── 9. Lightning & Thunder ──
            if (isThunder) {
                lightningSystem.draw(this, LightningSystem.LightningConfig(
                    frequency = if (compact) 0.01f else 0.025f,
                    intensity = 0.9f
                ))
            }

            // ── 10. Rainbow ──
            if (isRain && isDaytime) {
                rainbowSystem.draw(this, RainbowSystem.RainbowConfig(
                    probability = 0.3f, intensity = 0.5f
                ), sin(PI.toFloat() / 2f - abs(hour - 12).toFloat() / 12f * PI.toFloat()),
                true, 0.6f)
            }

            // ── 11. Terrain ground ──
            groundSystem.draw(this, GroundSystem.GroundConfig(
                color = palette.groundColor,
                detailColor = palette.groundDetailColor,
                isSnow = isSnow,
                isDark = isNight,
                alpha = palette.groundColor.alpha
            ), physics.time)
        }
    }
}

// ── Sun draw helper ──────────────────────────────────────────────────

private fun DrawScope.drawSun(
    palette: WeatherPalette, timeOfDay: TimeOfDay, glow: Float, compact: Boolean
) {
    val cx = size.width * 0.85f
    val cy = sunVerticalY(timeOfDay, size.height)
    if (cy > size.height) return
    val r = if (compact) size.minDimension * 0.07f else size.minDimension * 0.06f

    // Outer glow layers
    drawCircle(palette.sunGlowColor.copy(alpha = 0.12f * glow), r * 2f, Offset(cx, cy))
    drawCircle(Color.White.copy(alpha = 0.06f * glow), r * 3f, Offset(cx, cy))

    // Solar corona rays (subtle radial lines)
    val coronaRays = 12
    for (i in 0 until coronaRays) {
        val angle = i * PI.toFloat() * 2f / coronaRays
        val rayLen = r * (0.3f + 0.2f * sin(i * 3f + glow * 2f))
        drawLine(
            palette.sunGlowColor.copy(alpha = 0.08f * glow),
            Offset(cx + cos(angle) * r * 1.1f, cy + sin(angle) * r * 1.1f),
            Offset(cx + cos(angle) * (r * 1.1f + rayLen), cy + sin(angle) * (r * 1.1f + rayLen)),
            strokeWidth = 1.5f
        )
    }

    // Sun disk
    drawCircle(palette.sunColor, r, Offset(cx, cy))
    drawCircle(Color.White.copy(alpha = 0.35f), r * 0.55f, Offset(cx, cy))
}

// ── Moon draw helper ─────────────────────────────────────────────────

private fun DrawScope.drawMoon(
    palette: WeatherPalette, timeOfDay: TimeOfDay, glow: Float, compact: Boolean
) {
    val cx = size.width * 0.82f
    val cy = moonVerticalY(timeOfDay, size.height)
    if (cy > size.height) return
    val r = if (compact) size.minDimension * 0.07f else size.minDimension * 0.06f

    // Glow layers
    drawCircle(palette.moonGlowColor.copy(alpha = 0.10f * glow), r * 2f, Offset(cx, cy))
    drawCircle(palette.moonGlowColor.copy(alpha = 0.04f * glow), r * 3.5f, Offset(cx, cy))

    // Moon disk
    drawCircle(palette.moonColor, r, Offset(cx, cy))

    // Terminator shadow (phase effect)
    drawCircle(palette.secondary.copy(alpha = 0.65f), r * 0.85f,
        Offset(cx + r * 0.35f, cy - r * 0.1f))

    // Subtle crater texture
    for (i in 0..3) {
        val ca = i * 1.7f + 0.5f
        val cr = r * (0.1f + i * 0.04f)
        val ccx = cx + cos(ca) * r * 0.5f
        val ccy = cy + sin(ca) * r * 0.4f
        drawCircle(Color(0xFFB0BEC5).copy(alpha = 0.1f), cr, Offset(ccx, ccy))
    }
}

// ── Static frame for preview/inspection mode ─────────────────────────

@Composable
private fun StaticWeatherFrame(weatherCode: Int, palette: WeatherPalette, modifier: Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width; val h = size.height

        drawRect(Brush.verticalGradient(palette.background), size = size)

        val cx = w / 2; val cy = h * 0.35f; val r = minOf(w, h) * 0.12f

        when {
            weatherCode <= 1 -> {
                drawCircle(palette.accent, r, Offset(cx, cy))
                drawCircle(Color.White.copy(alpha = 0.3f), r * 0.6f, Offset(cx, cy))
            }
            weatherCode in 2..3 -> {
                drawCircle(palette.cloudBaseColor.copy(alpha = 0.4f), r * 1.2f, Offset(cx, cy))
                drawCircle(palette.cloudBaseColor.copy(alpha = 0.3f), r * 0.9f,
                    Offset(cx - r * 0.5f, cy + r * 0.2f))
            }
            weatherCode in 51..67 || weatherCode in 80..82 -> {
                for (i in 0..6) {
                    drawLine(Color(0xFF90CAF9).copy(alpha = 0.5f),
                        Offset(cx + (i - 3) * r * 0.4f, cy - r * 0.8f),
                        Offset(cx + (i - 3) * r * 0.4f + r * 0.1f, cy + r * 0.6f),
                        strokeWidth = 1f)
                }
            }
            weatherCode in 71..77 || weatherCode in 85..86 -> {
                for (i in 0..5) {
                    val sa = i * PI.toFloat() / 3f
                    drawCircle(Color.White.copy(alpha = 0.6f), r * 0.15f,
                        Offset(cx + sin(sa) * r * 0.6f, cy + cos(sa) * r * 0.6f))
                }
            }
            weatherCode in 45..48 -> {
                drawRect(palette.cloudBaseColor.copy(alpha = 0.35f),
                    Offset(cx - r * 2f, cy - r * 0.5f), Size(r * 4f, r * 1.2f))
            }
            weatherCode >= 95 -> {
                drawRect(Color.White.copy(alpha = 0.15f), size = size)
                drawCircle(palette.accent.copy(alpha = 0.5f), r, Offset(cx, cy))
            }
            else -> {
                drawCircle(palette.accent.copy(alpha = 0.5f), r, Offset(cx, cy))
            }
        }
    }
}
