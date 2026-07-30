package fieldmind.research.app.features.field.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalInspectionMode
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI
import kotlin.random.Random
import fieldmind.research.app.features.field.presentation.theme.FieldMindTheme

enum class TimeOfDay {
    Dawn, Sunrise, Morning, Midday, Afternoon, Sunset, Twilight, Night
}

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
    val isDarkTheme = FieldMindTheme.colors.isDark
    val palette = weatherPalette(temperature, timeOfDay, isDarkTheme)

    if (LocalInspectionMode.current) {
        StaticWeatherFrame(weatherCode, palette, modifier)
        return
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val bgColors = when {
                palette.background.size >= 3 -> palette.background
                palette.background.size == 2 -> listOf(palette.background[0], palette.tertiary, palette.background[1])
                else -> listOf(palette.primary, palette.tertiary, palette.secondary)
            }
            drawRect(Brush.verticalGradient(bgColors, 0f, size.height), size = size)
            drawRect(
                Brush.radialGradient(
                    listOf(palette.accent.copy(alpha = 0.06f), Color.Transparent),
                    center = Offset(size.width * 0.5f, size.height * 0.75f),
                    radius = size.maxDimension * 0.5f
                ),
                size = size
            )
        }
        WeatherScene(weatherCode, palette, compact, timeOfDay, showCloudAnimation, modifier)
    }
}

@Composable
private fun WeatherScene(
    weatherCode: Int,
    palette: WeatherPalette,
    compact: Boolean,
    timeOfDay: TimeOfDay,
    showCloudAnimation: Boolean,
    modifier: Modifier
) {
    val isDark = FieldMindTheme.colors.isDark
    val isDaytime = timeOfDay != TimeOfDay.Night && timeOfDay != TimeOfDay.Twilight

    val infiniteTransition = rememberInfiniteTransition(label = "weatherScene")
    val drift by infiniteTransition.animateFloat(0f, 1f,
        infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Restart), label = "drift")
    val drift2 by infiniteTransition.animateFloat(0f, 1f,
        infiniteRepeatable(tween(8500, easing = LinearEasing), RepeatMode.Restart), label = "drift2")
    val glow by infiniteTransition.animateFloat(0.6f, 1f,
        infiniteRepeatable(tween(5000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "glow")
    val twinkle by infiniteTransition.animateFloat(0f, 6.28f,
        infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart), label = "twinkle")

    val starCount = if (compact) 15 else 40
    val stars = remember { rememberStarPositions(starCount) }
    val starPhases = remember {
        val rng = Random(42)
        List(starCount) { Triple(1.5f + rng.nextFloat() * 3f, rng.nextFloat() * 6.28f, 0.3f + rng.nextFloat() * 0.7f) }
    }

    val isCloudy = weatherCode in 2..3 || weatherCode == -1 || (showCloudAnimation && weatherCode in 0..1)
    val cloudIntensity = when {
        weatherCode in 2..3 -> 0.85f
        weatherCode == -1 || (showCloudAnimation && weatherCode in 0..1) -> 0.25f
        else -> 0f
    }
    val isRain = weatherCode in 51..67 || weatherCode in 80..82
    val isSnow = weatherCode in 71..77 || weatherCode in 85..86
    val isFog = weatherCode in 45..48
    val isThunder = weatherCode >= 95

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        if (isDaytime || timeOfDay == TimeOfDay.Dawn || timeOfDay == TimeOfDay.Twilight) {
            drawSunGeometric(palette, timeOfDay, glow, compact)
        }
        if (!isDaytime || timeOfDay == TimeOfDay.Dawn || timeOfDay == TimeOfDay.Twilight) {
            drawMoonGeometric(palette, timeOfDay, glow, compact)
        }

        if (!isDaytime) {
            val starColor = Color(0xFFB3E5FC)
            val warmColor = Color(0xFFFFF9C4)
            stars.forEachIndexed { i, (sx, sy) ->
                val (speed, phase, bright) = starPhases[i]
                val t = (sin(twinkle * speed + phase) * 0.5f + 0.5f).coerceIn(0.1f, 1f) * bright
                val color = if (t > 0.55f) starColor else warmColor
                drawCircle(color.copy(alpha = t * 0.8f), radius = 0.7f + t * 2f, center = Offset(sx * w, sy * h))
            }
        }

        if (cloudIntensity > 0f && !isFog) {
            val cloudAlpha = if (isDark) 0.25f * cloudIntensity else 0.38f * cloudIntensity
            val cloudColor = palette.cloudBaseColor.copy(alpha = cloudAlpha)
            val cloudDark = palette.primary.copy(alpha = cloudAlpha * 0.7f)
            drawGeometricClouds(w, h, drift, drift2, cloudColor, cloudDark, compact, cloudIntensity)
        }

        if (isRain) {
            val rainAlpha = if (isDark) 0.35f else 0.28f
            val rainColor = Color(0xFF90CAF9).copy(alpha = rainAlpha)
            val count = if (compact) 30 else 80
            for (i in 0 until count) {
                val seed = (i * 137 + 42).toLong()
                val rng = Random(seed)
                val rx = (rng.nextFloat() + drift) % 1.05f * w - w * 0.05f
                val ry = ((rng.nextFloat() + drift * 0.7f) % 1.1f) * h - h * 0.1f
                val len = h * (0.04f + rng.nextFloat() * 0.04f)
                val angle = 20f
                val rad = angle * PI.toFloat() / 180f
                drawLine(rainColor, Offset(rx, ry), Offset(rx - len * sin(rad), ry + len * kotlin.math.cos(rad)), strokeWidth = 1.2f)
            }
        }

        if (isSnow) {
            val snowAlpha = if (isDark) 0.7f else 0.55f
            val count = if (compact) 20 else 50
            for (i in 0 until count) {
                val seed = (i * 251 + 17).toLong()
                val rng = Random(seed)
                val sx = (rng.nextFloat() + drift * 0.3f) % 1.05f * w - w * 0.05f
                val sy = ((rng.nextFloat() + drift * 0.5f) % 1.1f) * h - h * 0.1f
                val sr = 1.5f + rng.nextFloat() * 2.5f
                drawCircle(Color.White.copy(alpha = snowAlpha * (0.4f + rng.nextFloat() * 0.6f)), sr, Offset(sx, sy))
            }
        }

        if (isFog) {
            val fogAlpha = if (isDark) 0.25f else 0.30f
            for (band in 0..3) {
                val by = h * (0.2f + band * 0.2f) + sin(drift * 3f + band) * h * 0.03f
                val bandH = h * 0.12f
                val path = Path().apply {
                    moveTo(0f, by)
                    for (x in 0..20) {
                        val px = x * w / 20f
                        val py = by + sin(drift2 * 2f + x * 0.3f + band) * bandH * 0.4f
                        lineTo(px, py)
                    }
                    lineTo(w, by + bandH)
                    lineTo(0f, by + bandH)
                    close()
                }
                drawPath(path, palette.cloudBaseColor.copy(alpha = fogAlpha * (0.5f + band * 0.15f)), style = Fill)
            }
        }

        if (isThunder) {
            val flashPhase = (drift2 * 8f).toInt() % 8
            if (flashPhase == 0 || flashPhase == 3) {
                drawRect(Color.White.copy(alpha = 0.15f), size = Size(w, h))
            }
            if (flashPhase <= 1) {
                val boltX = w * (0.3f + (sin(drift * 3f) * 0.5f + 0.5f) * 0.4f)
                val path = Path().apply {
                    moveTo(boltX, -10f)
                    var x = boltX; var y = -10f
                    val segments = 8
                    for (s in 0..segments) {
                        y += h / (segments + 1)
                        x += (sin(drift2 * 5f + s) * w * 0.06f)
                        lineTo(x, y)
                    }
                }
                drawPath(path, Color.White.copy(alpha = 0.7f), style = Stroke(width = 2.5f, cap = StrokeCap.Round))
                drawPath(path, Color(0xFF90CAF9).copy(alpha = 0.3f), style = Stroke(width = 5f, cap = StrokeCap.Round))
            }
        }

        val groundY = h * 0.82f
        val groundPath = Path().apply {
            moveTo(-10f, h + 10f)
            lineTo(-10f, groundY)
            for (i in 0..60) {
                val t = i.toFloat() / 60f
                val px = t * (w + 20f) - 10f
                val hill = sin(t * 3.5f) * 0.04f + sin(t * 8f + 1.1f) * 0.025f + sin(t * 16f + 3.3f) * 0.012f
                val py = groundY - hill * h
                lineTo(px, py)
            }
            lineTo(w + 10f, h + 10f)
            close()
        }
        val groundMul = when { isSnow -> 1.5f; isRain || isThunder -> 0.7f; !isDaytime -> 0.5f; else -> 1.0f }
        val groundColor = palette.groundColor.copy(
            red = (palette.groundColor.red * groundMul).coerceAtMost(1f),
            green = (palette.groundColor.green * groundMul).coerceAtMost(1f),
            blue = (palette.groundColor.blue * groundMul).coerceAtMost(1f),
            alpha = (palette.groundColor.alpha * (if (isSnow) 1.3f else 1f)).coerceAtMost(0.4f)
        )
        drawPath(groundPath, groundColor, style = Fill)
    }
}

private fun DrawScope.drawGeometricClouds(
    w: Float, h: Float, drift: Float, drift2: Float,
    cloudColor: Color, cloudDark: Color, compact: Boolean, intensity: Float
) {
    val count = if (compact) 3 else 6
    for (i in 0 until count) {
        val seed = (i * 73 + 11).toLong()
        val rng = Random(seed)
        val baseX = rng.nextFloat()
        val baseY = 0.1f + rng.nextFloat() * 0.45f
        val scale = w * (0.2f + rng.nextFloat() * 0.25f)
        val driftSpeed = 0.7f + rng.nextFloat() * 0.6f
        val cx = ((baseX + drift * driftSpeed) % 1.15f) * w - w * 0.15f
        val cy = (baseY + sin(drift2 * 2f + i) * 0.03f) * h
        val cr = scale * 0.12f
        val color = if (i % 2 == 0) cloudColor else cloudDark
        drawCircle(color, cr * 1.2f, Offset(cx, cy))
        drawCircle(color.copy(alpha = color.alpha * 0.8f), cr * 1.0f, Offset(cx - cr * 0.6f, cy + cr * 0.3f))
        drawCircle(color.copy(alpha = color.alpha * 0.7f), cr * 0.9f, Offset(cx + cr * 0.7f, cy + cr * 0.2f))
        drawCircle(color.copy(alpha = color.alpha * 0.9f), cr * 0.6f, Offset(cx + cr * 0.1f, cy - cr * 0.5f))
        if (!compact) {
            drawCircle(color.copy(alpha = color.alpha * 0.5f), cr * 0.7f, Offset(cx - cr * 1.1f, cy + cr * 0.1f))
            drawCircle(color.copy(alpha = color.alpha * 0.5f), cr * 0.6f, Offset(cx + cr * 1.2f, cy))
        }
    }
}

private fun DrawScope.drawSunGeometric(
    palette: WeatherPalette, timeOfDay: TimeOfDay, glow: Float, compact: Boolean
) {
    val cx = size.width * 0.85f
    val cy = sunVerticalY(timeOfDay, size.height)
    if (cy > size.height) return
    val r = if (compact) size.minDimension * 0.07f else size.minDimension * 0.06f
    drawCircle(palette.accent.copy(alpha = 0.12f * glow), r * 2f, Offset(cx, cy))
    drawCircle(Color.White.copy(alpha = 0.06f * glow), r * 3f, Offset(cx, cy))
    drawCircle(palette.accent, r, Offset(cx, cy))
    drawCircle(Color.White.copy(alpha = 0.35f), r * 0.55f, Offset(cx, cy))
}

private fun DrawScope.drawMoonGeometric(
    palette: WeatherPalette, timeOfDay: TimeOfDay, glow: Float, compact: Boolean
) {
    val cx = size.width * 0.82f
    val cy = moonVerticalY(timeOfDay, size.height)
    if (cy > size.height) return
    val r = if (compact) size.minDimension * 0.07f else size.minDimension * 0.06f
    drawCircle(palette.moonGlowColor.copy(alpha = 0.10f * glow), r * 2f, Offset(cx, cy))
    drawCircle(palette.moonGlowColor.copy(alpha = 0.04f * glow), r * 3.5f, Offset(cx, cy))
    drawCircle(palette.moonColor, r, Offset(cx, cy))
    drawCircle(palette.secondary.copy(alpha = 0.65f), r * 0.85f, Offset(cx + r * 0.35f, cy - r * 0.1f))
}

@Composable
private fun StaticWeatherFrame(weatherCode: Int, palette: WeatherPalette, modifier: Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(Brush.verticalGradient(palette.background), size = size)
        val cx = size.width / 2; val cy = size.height * 0.35f; val r = size.minDimension * 0.12f
        when {
            weatherCode <= 1 -> { drawCircle(palette.accent, r, Offset(cx, cy)); drawCircle(Color.White.copy(alpha = 0.3f), r * 0.6f, Offset(cx, cy)) }
            weatherCode in 2..3 -> { drawCircle(palette.cloudBaseColor.copy(alpha = 0.4f), r * 1.2f, Offset(cx, cy)); drawCircle(palette.cloudBaseColor.copy(alpha = 0.3f), r * 0.9f, Offset(cx - r * 0.5f, cy + r * 0.2f)) }
            weatherCode in 51..67 || weatherCode in 80..82 -> { for (i in 0..6) drawLine(Color(0xFF90CAF9).copy(alpha = 0.5f), Offset(cx + (i-3)*r*0.4f, cy-r*0.8f), Offset(cx + (i-3)*r*0.4f + r*0.1f, cy+r*0.6f), strokeWidth = 1f) }
            weatherCode in 71..77 || weatherCode in 85..86 -> { for (i in 0..5) drawCircle(Color.White.copy(alpha = 0.6f), r * 0.15f, Offset(cx + sin(i*1.05f)*r*0.6f, cy + kotlin.math.cos(i*1.05f)*r*0.6f)) }
            weatherCode in 45..48 -> { drawRect(palette.cloudBaseColor.copy(alpha = 0.35f), Offset(cx - r * 2f, cy - r * 0.5f), Size(r * 4f, r * 1.2f)) }
            weatherCode >= 95 -> { drawRect(Color.White.copy(alpha = 0.15f), size = size); drawCircle(palette.accent.copy(alpha = 0.5f), r, Offset(cx, cy)) }
            else -> { drawCircle(palette.accent.copy(alpha = 0.5f), r, Offset(cx, cy)) }
        }
    }
}

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

private fun rememberStarPositions(count: Int): List<Pair<Float, Float>> {
    val rng = Random(42)
    return List(count) { rng.nextFloat() to rng.nextFloat() }
}

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

private data class TimeOfDayColors(
    val skyTop: Color, val skyMid: Color, val skyBottom: Color, val skyAccent: Color,
    val sunCol: Color, val sunGlowCol: Color, val sunLensFlare: Color,
    val moonCol: Color, val moonGlowCol: Color,
    val cloudCol: Color, val hazeCol: Color, val groundCol: Color, val groundDetail: Color
)

private fun weatherPalette(temp: Double?, timeOfDay: TimeOfDay, isDarkTheme: Boolean): WeatherPalette {
    val tempC = temp ?: 20.0
    val timeColors = when (timeOfDay) {
        TimeOfDay.Dawn -> TimeOfDayColors(Color(0xFF5B3E8A),Color(0xFFD48BAA),Color(0xFFFFD6A8),Color(0xFFE8A0B4),Color(0xFFFFDAB0),Color(0xFFFFE8C0),Color(0xFFFFAA88),Color(0xFFC8B8D8),Color(0xFFD0C0E0),Color(0xFFF0E0D0),Color(0xFFFFE0C0),Color(0xFF6A5A4A),Color(0xFF4A3A3A))
        TimeOfDay.Sunrise -> TimeOfDayColors(Color(0xFFFF6A30),Color(0xFFFFAA50),Color(0xFFFFDD70),Color(0xFFFF8A60),Color(0xFFFFF8C8),Color(0xFFFFE888),Color(0xFFFF8833),Color(0xFFE0C8A0),Color(0xFFFFE0B0),Color(0xFFFFE0C0),Color(0xFFFFE0A0),Color(0xFF6A4A2A),Color(0xFF4A2A1A))
        TimeOfDay.Morning -> TimeOfDayColors(Color(0xFF6AB0E8),Color(0xFF8AC8EE),Color(0xFFC0E8F0),Color(0xFF64B5F6),Color(0xFFFFF9C4),Color(0xFFFFFDE8),Color(0xFFFFDD88),Color(0xFFD0D8E0),Color(0xFFE0E8F0),Color(0xFFFFF8E1),Color(0xFFE8F4FA),Color(0xFF3A6A3A),Color(0xFF1A3A1A))
        TimeOfDay.Midday -> TimeOfDayColors(Color(0xFF1A6AC8),Color(0xFF4A9AEA),Color(0xFF8AC8F0),Color(0xFF42A5F5),Color(0xFFFFF9C4),Color(0xFFFFFDE8),Color(0xFFFFDD88),Color(0xFFD0D8E0),Color(0xFFE0E8F0),Color.White,Color(0xFFD6ECFA),Color(0xFF2A5A2A),Color(0xFF0A2A1A))
        TimeOfDay.Afternoon -> TimeOfDayColors(Color(0xFF3A80C8),Color(0xFF6AAAD8),Color(0xFFD6D8B0),Color(0xFFFFB74D),Color(0xFFFFF9C4),Color(0xFFFFFDE8),Color(0xFFFFDDA0),Color(0xFFD0D8E0),Color(0xFFE0E8F0),Color(0xFFFFF0D0),Color(0xFFF0E8D0),Color(0xFF3A5A3A),Color(0xFF1A3A1A))
        TimeOfDay.Sunset -> TimeOfDayColors(Color(0xFFFF4A20),Color(0xFFE83888),Color(0xFF8822AA),Color(0xFFFF5252),Color(0xFFFFE880),Color(0xFFFFAA44),Color(0xFFFF4400),Color(0xFFC8A0C0),Color(0xFFE0B0D0),Color(0xFFE0A090),Color(0xFFE08070),Color(0xFF4A2A2A),Color(0xFF2A1A1A))
        TimeOfDay.Twilight -> TimeOfDayColors(Color(0xFF0D0D3A),Color(0xFF2A1878),Color(0xFF6A1A8E),Color(0xFF7C4DFF),Color(0xFFFFCC88),Color(0xFFFF8855),Color(0xFFCC5500),Color(0xFFC8D0E0),Color(0xFF90A0C0),Color(0xFF4A5878),Color(0xFF3A2860),Color(0xFF0A1018),Color(0xFF05080C))
        TimeOfDay.Night -> TimeOfDayColors(Color(0xFF040418),Color(0xFF0A0A30),Color(0xFF1A1A48),Color(0xFF4A5AC0),Color(0xFFD8DCE0),Color(0xFFA0A8B0),Color(0xFF606870),Color(0xFFE8ECF0),Color(0xFFA0B8E8),Color(0xFF283048),Color(0xFF0A0A28),Color(0xFF04080C),Color(0xFF020406))
    }
    val tempBlend = when { tempC < -10 -> 0.2f; tempC < 0 -> 0.1f; tempC > 35 -> 0.15f; tempC > 28 -> 0.08f; else -> 0f }
    val warmColor = Color(0xFFFF8A65); val coolColor = Color(0xFF64B5F6)
    val modulatedTop = if (tempBlend > 0f && tempC > 28) timeColors.skyTop.let { Color((it.red+(warmColor.red-it.red)*tempBlend).coerceIn(0f,1f),(it.green+(warmColor.green-it.green)*tempBlend).coerceIn(0f,1f),(it.blue+(warmColor.blue-it.blue)*tempBlend).coerceIn(0f,1f),it.alpha) } else if (tempBlend > 0f && tempC < 0) timeColors.skyTop.let { Color((it.red+(coolColor.red-it.red)*tempBlend).coerceIn(0f,1f),(it.green+(coolColor.green-it.green)*tempBlend).coerceIn(0f,1f),(it.blue+(coolColor.blue-it.blue)*tempBlend).coerceIn(0f,1f),it.alpha) } else timeColors.skyTop
    val isNighttime = timeOfDay == TimeOfDay.Night || timeOfDay == TimeOfDay.Twilight
    if (isDarkTheme) {
        val topMul = if (isNighttime) 0.52f else 0.72f; val botMul = if (isNighttime) 0.42f else 0.65f
        val dt = modulatedTop.copy(red=(modulatedTop.red*topMul).coerceAtMost(0.75f),green=(modulatedTop.green*topMul).coerceAtMost(0.70f),blue=(modulatedTop.blue*topMul*1.1f).coerceAtMost(0.80f))
        val db = timeColors.skyBottom.copy(red=(timeColors.skyBottom.red*botMul).coerceAtMost(0.65f),green=(timeColors.skyBottom.green*botMul).coerceAtMost(0.60f),blue=(timeColors.skyBottom.blue*botMul*1.1f).coerceAtMost(0.70f))
        val bg = listOf(dt.copy(alpha=0.92f),db.copy(alpha=0.88f))
        return WeatherPalette(dt,db,timeColors.skyMid.copy(alpha=0.6f),timeColors.skyAccent.copy(alpha=0.8f),bg,timeColors.sunCol.copy(alpha=0.8f),timeColors.sunGlowCol.copy(alpha=0.4f),timeColors.sunLensFlare.copy(alpha=0.25f),timeColors.moonCol,timeColors.moonGlowCol.copy(alpha=0.5f),timeColors.cloudCol.copy(alpha=0.4f),timeColors.hazeCol.copy(alpha=0.08f),timeColors.groundCol.copy(alpha=0.3f),timeColors.groundDetail.copy(alpha=0.25f))
    } else {
        if (isNighttime) {
            val nb = listOf(Color(0xFF04041A),timeColors.skyMid,timeColors.skyBottom.copy(alpha=0.7f))
            return WeatherPalette(Color(0xFF080820),Color(0xFF1A1A3E),timeColors.skyMid.copy(alpha=0.5f),timeColors.skyAccent.copy(alpha=0.6f),nb,timeColors.sunCol.copy(alpha=0.5f),timeColors.sunGlowCol.copy(alpha=0.2f),timeColors.sunLensFlare.copy(alpha=0.15f),Color(0xFFECEFF1),Color(0xFFB3E5FC).copy(alpha=0.5f),timeColors.cloudCol.copy(alpha=0.22f),timeColors.hazeCol.copy(alpha=0.04f),timeColors.groundCol.copy(alpha=0.25f),timeColors.groundDetail.copy(alpha=0.2f))
        }
        val lt = modulatedTop.copy(red=(modulatedTop.red*0.88f+0.12f).coerceAtMost(1f),green=(modulatedTop.green*0.88f+0.12f).coerceAtMost(1f),blue=(modulatedTop.blue*0.90f+0.10f).coerceAtMost(1f))
        val lb = timeColors.skyBottom.copy(red=(timeColors.skyBottom.red*0.80f+0.20f).coerceAtMost(1f),green=(timeColors.skyBottom.green*0.80f+0.20f).coerceAtMost(1f),blue=(timeColors.skyBottom.blue*0.82f+0.18f).coerceAtMost(1f))
        val bg = listOf(lt,timeColors.skyMid.copy(alpha=0.95f),lb)
        return WeatherPalette(lt,lb,timeColors.skyMid.copy(alpha=0.85f),timeColors.skyAccent.copy(alpha=0.8f),bg,timeColors.sunCol,timeColors.sunGlowCol.copy(alpha=0.6f),timeColors.sunLensFlare.copy(alpha=0.35f),timeColors.moonCol.copy(alpha=0.8f),timeColors.moonGlowCol.copy(alpha=0.3f),timeColors.cloudCol.copy(alpha=0.50f),timeColors.hazeCol.copy(alpha=0.08f),timeColors.groundCol.copy(alpha=0.25f),timeColors.groundDetail.copy(alpha=0.2f))
    }
}
