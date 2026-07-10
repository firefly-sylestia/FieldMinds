package fieldmind.research.app.features.field.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import fieldmind.research.app.features.field.presentation.theme.FieldMindTheme
import fieldmind.research.app.shared.presentation.theme.BackgroundAnimationLevel
import fieldmind.research.app.shared.presentation.theme.JournalConfig
import fieldmind.research.app.shared.presentation.theme.JournalPresets
import fieldmind.research.app.shared.presentation.theme.JournalStyle
import fieldmind.research.app.shared.presentation.theme.LocalBackgroundAnimation
import fieldmind.research.app.shared.presentation.theme.LocalJournalStyle
import java.time.Instant
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * ════════════════════════════════════════════════════════════════════════
 *  🌄 AnimatedBackgroundScene — Full-screen immersive background
 *
 *  Purpose: Render the animated weather/time-of-day scene as a full-screen
 *  background layer, blended with the active journal aesthetic (warmth,
 *  texture, color grading). Respects the user's [BackgroundAnimationLevel]
 *  setting to control animation intensity.
 *
 *  Usage:
 *  ```kotlin
 *  Box(Modifier.fillMaxSize()) {
 *      AnimatedBackgroundScene(
 *          weatherCode = currentWeather?.weatherCode ?: 0,
 *          temperature = currentWeather?.temperature ?: 20.0,
 *          sunrise = currentWeather?.sunrise,
 *          sunset = currentWeather?.sunset,
 *      )
 *      // Screen content on top...
 *  }
 *  ```
 *
 *  This replaces the static [fieldmind.research.app.ui.theme.screenBackground]
 *  modifier with a living, breathing backdrop that changes with weather,
 *  time of day, and the user's chosen journal aesthetic.
 * ════════════════════════════════════════════════════════════════════════
 */

/**
 * Renders the animated weather/time-of-day scene as a full-screen background,
 * blended with the active journal aesthetic.
 *
 * @param weatherCode WMO weather code (0=clear, 1-3=cloudy, 45-48=fog, 51-67=rain, etc.)
 * @param temperature Current temperature in °C (drives palette shifts)
 * @param sunrise Sunrise ISO time string (optional, for time-of-day computation)
 * @param sunset Sunset ISO time string (optional, for time-of-day computation)
 * @param forceNight Override time-of-day to night (optional, for testing)
 * @param showCloudAnimation Whether to show animated clouds
 * @param modifier Modifier for the container
 */
@Composable
fun AnimatedBackgroundScene(
    weatherCode: Int,
    temperature: Double?,
    sunrise: String? = null,
    sunset: String? = null,
    forceNight: Boolean? = null,
    showCloudAnimation: Boolean = true,
    modifier: Modifier = Modifier
) {
    val journalConfig = LocalJournalStyle.current
    val animLevel = LocalBackgroundAnimation.current
    val isDark = FieldMindTheme.colors.isDark

    // ── Phase 2: time-of-day resolution + 16-mood scene palette ──
    val tod = resolveTimeOfDay(sunrise, sunset, System.currentTimeMillis(), forceNight)
    val palette = getBasePalette(tod).applyStyle(journalConfig.style)

    // In preview/inspection mode, show a static gradient
    if (LocalInspectionMode.current) {
        StaticJournalBackground(journalConfig, isDark, modifier)
        return
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Layer 1: The animated weather scene (existing)
        // We render it at reduced alpha and blend with journal warmth
        AnimatedWeatherScene(
            weatherCode = weatherCode,
            temperature = temperature,
            sunrise = sunrise,
            sunset = sunset,
            forceNight = forceNight,
            showCloudAnimation = showCloudAnimation && animLevel != BackgroundAnimationLevel.Static,
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (animLevel == BackgroundAnimationLevel.Static)
                        Modifier  // Static mode still renders weather scene but without animations
                    else
                        Modifier
                )
        )

        // Layer 1.5: Sky / celestial / clouds / horizon / stars / fireflies / birds
        // (Phase 2 — sits below warmth overlay so warmth tints the whole skybox.)
        AtmosphericSkyboxScene(
            tod = tod,
            palette = palette,
            journalStyle = journalConfig.style,
            animLevel = animLevel,
            isDark = isDark,
            modifier = Modifier.fillMaxSize()
        )

        // Layer 2: Journal warmth tint overlay
        JournalWarmthOverlay(
            journalConfig = journalConfig,
            isDark = isDark,
            animLevel = animLevel
        )

        // Layer 3: Paper texture overlay (for journal styles that use textures)
        if (journalConfig.showTexture && animLevel != BackgroundAnimationLevel.Full) {
            JournalTextureOverlay(
                journalConfig = journalConfig,
                animLevel = animLevel
            )
        }

        // Layer 4: Vignette for depth and focus
        VignetteOverlay(
            isDark = isDark,
            animLevel = animLevel
        )
    }
}

/**
 * Static gradient background for preview/inspection mode.
 * Uses the journal style's warmth color for a simple gradient backdrop.
 */
@Composable
private fun StaticJournalBackground(
    journalConfig: JournalConfig,
    isDark: Boolean,
    modifier: Modifier
) {
    val warmth = journalConfig.backgroundWarmth
    val topColor = if (isDark) {
        Color(
            (warmth.red * 0.3f).coerceAtMost(1f),
            (warmth.green * 0.25f).coerceAtMost(1f),
            (warmth.blue * 0.3f).coerceAtMost(1f)
        )
    } else {
        Color(
            (warmth.red * 0.9f + 0.1f).coerceAtMost(1f),
            (warmth.green * 0.9f + 0.1f).coerceAtMost(1f),
            (warmth.blue * 0.9f + 0.1f).coerceAtMost(1f)
        )
    }
    val bottomColor = if (isDark) {
        Color(
            (topColor.red * 0.6f).coerceAtMost(1f),
            (topColor.green * 0.5f).coerceAtMost(1f),
            (topColor.blue * 0.7f).coerceAtMost(1f)
        )
    } else {
        Color(
            (topColor.red * 0.85f).coerceAtMost(1f),
            (topColor.green * 0.82f).coerceAtMost(1f),
            (topColor.blue * 0.88f).coerceAtMost(1f)
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(topColor, bottomColor)
            ),
            size = size
        )
    }
}

/**
 * Overlays the journal style's warmth color as a subtle tint over the scene.
 * Different journal styles produce different atmospheric feels:
 * - Victorian: Warm sepia/parchment tint
 * - Sketchbook: Warm cream tint
 * - BulletJournal: Clean neutral tint (minimal)
 * - Ghibli: Soft dreamy warm tint
 */
@Composable
private fun JournalWarmthOverlay(
    journalConfig: JournalConfig,
    isDark: Boolean,
    animLevel: BackgroundAnimationLevel
) {
    val warmthColor = journalConfig.backgroundWarmth
    val baseAlpha = if (isDark) 0.06f else 0.08f

    // Animate the warmth overlay gently when at Full animation level
    if (animLevel == BackgroundAnimationLevel.Full) {
        val infiniteTransition = rememberInfiniteTransition(label = "warmthPulse")
        val pulseAlpha by infiniteTransition.animateFloat(
            initialValue = baseAlpha * 0.8f,
            targetValue = baseAlpha * 1.2f,
            animationSpec = infiniteRepeatable(
                tween(8000, easing = LinearEasing),
                RepeatMode.Reverse
            ),
            label = "warmthPulse"
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                color = warmthColor.copy(alpha = pulseAlpha.coerceIn(0f, 0.2f)),
                size = size
            )
        }
    } else {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                color = warmthColor.copy(alpha = baseAlpha.coerceIn(0f, 0.2f)),
                size = size
            )
        }
    }
}

/**
 * Renders a subtle paper texture overlay that gives the background a tactile feel.
 * The texture pattern changes based on the journal style's textureName:
 * - "parchment": subtle mottled warmth (Victorian)
 * - "paper": gentle fiber texture (Sketchbook)
 * - "dotgrid": subtle dot pattern (BulletJournal)
 * - "watercolor": soft organic washes (Ghibli)
 */
@Composable
private fun JournalTextureOverlay(
    journalConfig: JournalConfig,
    animLevel: BackgroundAnimationLevel
) {
    val textureOpacity = journalConfig.textureOpacity.coerceIn(0f, 0.25f)

    // At Full animation, let the texture breathe with a slow morph
    if (animLevel == BackgroundAnimationLevel.Full && textureOpacity > 0.01f) {
        val infiniteTransition = rememberInfiniteTransition(label = "textureMorph")
        val morphProgress by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                tween(30000, easing = LinearEasing),
                RepeatMode.Restart
            ),
            label = "textureMorph"
        )

        val rng = rememberTextureRng(journalConfig.textureName)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawJournalTexture(
                name = journalConfig.textureName,
                opacity = textureOpacity,
                morph = morphProgress,
                rng = rng
            )
        }
    } else if (textureOpacity > 0.01f) {
        val rng = rememberTextureRng(journalConfig.textureName)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawJournalTexture(
                name = journalConfig.textureName,
                opacity = textureOpacity,
                morph = 0f,
                rng = rng
            )
        }
    }
}

/**
 * Draws a subtle vignette (darkened edges) to focus attention on center content.
 */
@Composable
private fun VignetteOverlay(
    isDark: Boolean,
    animLevel: BackgroundAnimationLevel
) {
    val vignetteColor = if (isDark) Color.Black else Color(0xFF1A1A2E)
    val vignetteAlpha = if (isDark) 0.15f else 0.06f

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.Transparent,
                    vignetteColor.copy(alpha = vignetteAlpha)
                ),
                center = Offset(size.width * 0.5f, size.height * 0.4f),
                radius = size.maxDimension * 0.7f
            ),
            size = size
        )
    }
}

// ════════════════════════════════════════════════════════════════════════
//  Texture Drawing Routines
// ════════════════════════════════════════════════════════════════════════

/**
 * Draws a procedural paper-like texture using repeated noise patterns.
 * The texture name selects the pattern:
 * - parchment: Warm, mottled organic spots
 * - paper: Fine grain fibers
 * - dotgrid: Subtle grid of dots
 * - watercolor: Soft, irregular wash shapes
 */
private fun DrawScope.drawJournalTexture(
    name: String,
    opacity: Float,
    morph: Float,
    rng: List<Float>
) {
    when (name) {
        "parchment" -> drawParchmentTexture(opacity, morph, rng)
        "paper" -> drawPaperTexture(opacity, morph, rng)
        "dotgrid" -> drawDotGridTexture(opacity, morph, rng)
        "watercolor" -> drawWatercolorTexture(opacity, morph, rng)
    }
}

/**
 * Parchment texture: warm mottled organic spots with varying size and opacity.
 * Creates an aged look with subtle dark/light variations across the surface.
 */
private fun DrawScope.drawParchmentTexture(opacity: Float, morph: Float, rng: List<Float>) {
    val warmColor = Color(0xFF8B6914)

    // Large-scale mottling
    for (i in 0..8) {
        val cx = size.width * (rng[i * 3] + morph * 0.02f)
        val cy = size.height * (rng[i * 3 + 1] + morph * 0.015f)
        val cr = size.maxDimension * (0.08f + rng[i * 3 + 2] * 0.12f)
        val alpha = opacity * 0.3f * (0.5f + rng[i * 3 + 2] * 0.5f)

        drawCircle(
            color = warmColor.copy(alpha = alpha.coerceIn(0f, 0.08f)),
            radius = cr,
            center = Offset(cx, cy)
        )
    }

    // Fine grain overlay
    for (i in 0..20) {
        val cx = size.width * (rng[i * 7 % 63] + morph * 0.01f)
        val cy = size.height * (rng[i * 7 % 63 + 1] + morph * 0.005f)
        val cr = size.maxDimension * (0.01f + rng[i * 7 % 63 + 2] * 0.02f)
        val alpha = opacity * 0.5f * (0.3f + rng[i * 7 % 63 + 2] * 0.7f)

        drawCircle(
            color = warmColor.copy(alpha = alpha.coerceIn(0f, 0.04f)),
            radius = cr,
            center = Offset(cx, cy)
        )
    }
}

/**
 * Paper texture: fine, even grain across the surface.
 * Creates a subtle fiber texture reminiscent of quality sketch paper.
 */
private fun DrawScope.drawPaperTexture(opacity: Float, morph: Float, @Suppress("UNUSED_PARAMETER") rng: List<Float>) {
    val fiberColor = Color(0xFF8B7355)

    // Short fiber-like strokes
    for (i in 0..15) {
        val cx = size.width * (rng[i * 5] + morph * 0.01f)
        val cy = size.height * (rng[i * 5 + 1] + morph * 0.008f)
        val length = size.maxDimension * (0.02f + rng[i * 5 + 2] * 0.04f)
        val angle = rng[i * 5 + 3] * 360f
        val alpha = opacity * 0.4f * rng[i * 5 + 4]

        val endX = cx + kotlin.math.cos(angle) * length
        val endY = cy + kotlin.math.sin(angle) * length

        drawLine(
            color = fiberColor.copy(alpha = alpha.coerceIn(0f, 0.03f)),
            start = Offset(cx, cy),
            end = Offset(endX, endY),
            strokeWidth = 0.5f
        )
    }
}

/**
 * Dot grid texture: subtle grid of small dots.
 * Creates the bullet journal dot-grid feel in the background.
 */
private fun DrawScope.drawDotGridTexture(opacity: Float, morph: Float, @Suppress("UNUSED_PARAMETER") rng: List<Float>) {
    val dotColor = Color(0xFF9E9E9E)
    val spacing = size.minDimension / 28f
    val offset = morph * spacing

    val startX = (offset % spacing) - spacing
    val startY = (offset * 0.7f % spacing) - spacing

    var x = startX
    while (x < size.width + spacing) {
        var y = startY
        while (y < size.height + spacing) {
            drawCircle(
                color = dotColor.copy(alpha = (opacity * 0.5f).coerceIn(0f, 0.06f)),
                radius = 1f,
                center = Offset(x, y)
            )
            y += spacing
        }
        x += spacing
    }
}

/**
 * Watercolor texture: soft, overlapping organic washes.
 * Creates the Ghibli watercolor feel with irregular color blotches.
 */
private fun DrawScope.drawWatercolorTexture(opacity: Float, morph: Float, rng: List<Float>) {
    val washColor = Color(0xFFD4A574)

    // Large soft washes
    for (i in 0..5) {
        val cx = size.width * (rng[i * 11] + morph * 0.03f)
        val cy = size.height * (rng[i * 11 + 1] + morph * 0.02f)
        val cr = size.maxDimension * (0.15f + rng[i * 11 + 2] * 0.2f)
        val alpha = opacity * 0.6f * (0.3f + rng[i * 11 + 3] * 0.7f)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    washColor.copy(alpha = alpha.coerceIn(0f, 0.06f)),
                    washColor.copy(alpha = 0f)
                ),
                center = Offset(cx, cy),
                radius = cr
            ),
            radius = cr,
            center = Offset(cx, cy)
        )
    }

    // Small pigment flecks
    for (i in 0..12) {
        val cx = size.width * (rng[i * 13 + 5] + morph * 0.02f)
        val cy = size.height * (rng[i * 13 + 6] + morph * 0.015f)
        val cr = size.maxDimension * (0.005f + rng[i * 13 + 7] * 0.015f)
        val alpha = opacity * 0.3f * rng[i * 13 + 8]

        drawCircle(
            color = washColor.copy(alpha = alpha.coerceIn(0f, 0.04f)),
            radius = cr,
            center = Offset(cx, cy)
        )
    }
}

// ════════════════════════════════════════════════════════════════════════
//  Staggered entrance (re-declared here for independent editing)
// ════════════════════════════════════════════════════════════════════════

// ════════════════════════════════════════════════════════════════════════
//  Texture RNG Cache
// ════════════════════════════════════════════════════════════════════════

/**
 * Caches pseudo-random values for texture generation so they're stable
 * across recompositions (no visual flicker). Each texture name gets its
 * own seeded sequence.
 */
private val textureRngCache = mutableMapOf<String, List<Float>>()

@Composable
private fun rememberTextureRng(name: String): List<Float> {
    return remember(name) {
        textureRngCache.getOrPut(name) {
            val rng = Random(name.hashCode())
            List(100) { rng.nextFloat() }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════════════
//  ☀️ TIME-OF-DAY SYSTEM (Phase 2 — atmospheric skybox)
// ═══════════════════════════════════════════════════════════════════════════════════════
//
//  Goal: Replace the static "warmth + texture + vignette" feel with a living, breathing
//  skybox that changes with time-of-day (Dawn / Day / Evening / Night) and tints each of
//  the four journal aesthetics onto the result. 4 × 4 = 16 distinct visual moods.
//
//  Layered between AnimatedWeatherScene (Layer 1) and JournalWarmthOverlay (Layer 2) so
//  the warmth overlay color-grades the whole skybox consistently.
//
//  Each mood draws from a `ScenePalette` of 17 colors. Per-journal tints (sepia / pencil-
//  desat / crisp-boost / watercolor-bleed) are tunnelled through 4 Color extensions.

/** Active coarse daily interval driving the skybox. */
enum class TimeOfDay { Dawn, Day, Evening, Night }

/** Where the celestial body sits on screen + current moon phase (Night only). */
private data class CelestialBody(
    val showSun: Boolean,
    val sunCx01: Float,
    val sunCy01: Float,
    val sunR01: Float,
    val showMoon: Boolean,
    val moonCx01: Float,
    val moonCy01: Float,
    val moonR01: Float,
    val moonPhase: Float
)

/** 17-color scene palette: drives every atmospheric layer. */
private data class ScenePalette(
    val skyTop: Color,
    val skyBottom: Color,
    val sunDiscColor: Color,
    val sunGlowColor: Color,
    val sunFlareColor: Color,
    val moonDiscColor: Color,
    val moonShadowColor: Color,
    val starColor: Color,
    val constellationColor: Color,
    val cloudColor: Color,
    val cloudShadowColor: Color,
    val cloudHighlightColor: Color,
    val mistColor: Color,
    val mistStrongColor: Color,
    val fireflyColor: Color,
    val fireflyGlowColor: Color,
    val horizonColor: Color,
    val birdColor: Color
)

// ── Resolver & helpers ─────────────────────────────────────────────────────────

/** Current moon phase as a 0..1 fraction through the 29.5306-day synodic cycle. */
private fun moonPhase(nowMillis: Long): Float {
    val synodic = 29.530588
    // 2000-01-06 18:14 UTC ≈ a known new moon.
    val refMillis = 947_182_440_000L
    val days = (nowMillis - refMillis) / 86_400_000.0
    return (((days % synodic) + synodic) % synodic / synodic).toFloat()
}

/** Parse ISO-8601 instant (e.g. "2024-03-15T06:30:00Z") → epoch millis, or null. */
private fun parseIsoMillisOrNull(iso: String): Long? = try {
    Instant.parse(iso).toEpochMilli()
} catch (_: Exception) { null }

/**
 * Pick the active [TimeOfDay] from sunrise/sunset ISO strings + clock.
 * `forceNight` short-circuits to Night (used by dev launcher + tests).
 */
private fun resolveTimeOfDay(
    sunriseIso: String?,
    sunsetIso: String?,
    nowMillis: Long,
    forceNight: Boolean?
): TimeOfDay {
    if (forceNight == true) return TimeOfDay.Night
    val sr = sunriseIso?.let { parseIsoMillisOrNull(it) }
    val ss = sunsetIso?.let { parseIsoMillisOrNull(it) }
    if (sr != null && ss != null) {
        val dawn0 = sr - 30 * 60_000L;  val dawn1 = sr + 90 * 60_000L
        val eve0  = ss - 60 * 60_000L;  val eve1  = ss + 90 * 60_000L
        return when {
            nowMillis in dawn0..dawn1 -> TimeOfDay.Dawn
            nowMillis in dawn1..eve0  -> TimeOfDay.Day
            nowMillis in eve0..eve1   -> TimeOfDay.Evening
            else                       -> TimeOfDay.Night
        }
    }
    val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
    return when (cal.get(Calendar.HOUR_OF_DAY)) {
        in 5..7   -> TimeOfDay.Dawn
        in 8..16  -> TimeOfDay.Day
        in 17..19 -> TimeOfDay.Evening
        else      -> TimeOfDay.Night
    }
}

/** Position on screen for sun or moon disc, indexed by time-of-day. */
private fun resolveCelestial(tod: TimeOfDay, nowMillis: Long): CelestialBody = when (tod) {
    TimeOfDay.Dawn -> CelestialBody(
        showSun = true, sunCx01 = 0.78f, sunCy01 = 0.62f, sunR01 = 0.105f,
        showMoon = false, moonCx01 = 0f, moonCy01 = 0f, moonR01 = 0f,
        moonPhase = 0f
    )
    TimeOfDay.Day -> CelestialBody(
        showSun = true, sunCx01 = 0.82f, sunCy01 = 0.18f, sunR01 = 0.075f,
        showMoon = false, moonCx01 = 0f, moonCy01 = 0f, moonR01 = 0f,
        moonPhase = 0f
    )
    TimeOfDay.Evening -> CelestialBody(
        showSun = true, sunCx01 = 0.78f, sunCy01 = 0.78f, sunR01 = 0.115f,
        showMoon = false, moonCx01 = 0f, moonCy01 = 0f, moonR01 = 0f,
        moonPhase = 0f
    )
    TimeOfDay.Night -> CelestialBody(
        showSun = false, sunCx01 = 0f, sunCy01 = 0f, sunR01 = 0f,
        showMoon = true, moonCx01 = 0.22f, moonCy01 = 0.18f, moonR01 = 0.065f,
        moonPhase = moonPhase(nowMillis)
    )
}

// ── Per-Journal tint extensions (4 distinct transformations) ───────────────────

/** Victorian: collapse every color toward sepia. */
private fun Color.sepiaTint(): Color {
    val lum = red * 0.299f + green * 0.587f + blue * 0.114f
    return Color(
        red   = (lum * 1.18f).coerceIn(0f, 1f),
        green = (lum * 0.86f).coerceIn(0f, 1f),
        blue  = (lum * 0.55f).coerceIn(0f, 1f),
        alpha = alpha
    )
}

/** Sketchbook: pull saturation down (55% original + 40% luminance + slight green bias). */
private fun Color.pencilDesat(): Color {
    val lum = red * 0.299f + green * 0.587f + blue * 0.114f
    return Color(
        red   = (red   * 0.55f + lum * 0.40f).coerceIn(0f, 1f),
        green = (green * 0.60f + lum * 0.40f + 0.02f).coerceIn(0f, 1f),
        blue  = (blue  * 0.55f + lum * 0.40f).coerceIn(0f, 1f),
        alpha = alpha
    )
}

/** BulletJournal: boost saturation by pulling each channel away from channel mean. */
private fun Color.crispBoost(): Color {
    val mid = (red + green + blue) / 3f
    fun ch(c: Float) = (c * 1.20f + (c - mid) * 0.30f).coerceIn(0f, 1f)
    return Color(red = ch(red), green = ch(green), blue = ch(blue), alpha = alpha)
}

/** Ghibli: slight desaturation + lift highlights toward warm cream for painterly bleed. */
private fun Color.watercolorBleed(): Color {
    val lum = red * 0.299f + green * 0.587f + blue * 0.114f
    return Color(
        red   = (red   * 0.85f + lum * 0.20f + 0.04f).coerceIn(0f, 1f),
        green = (green * 0.92f + lum * 0.15f + 0.04f).coerceIn(0f, 1f),
        blue  = (blue  * 1.00f + lum * 0.05f + 0.06f).coerceIn(0f, 1f),
        alpha = (alpha * 0.93f).coerceIn(0f, 1f)
    )
}

/** Apply the active journal tint to every field of a [ScenePalette]. */
private fun ScenePalette.applyStyle(style: JournalStyle): ScenePalette {
    val mapper: (Color) -> Color = when (style) {
        JournalStyle.Victorian     -> { c -> c.sepiaTint() }
        JournalStyle.Sketchbook    -> { c -> c.pencilDesat() }
        JournalStyle.BulletJournal -> { c -> c.crispBoost() }
        JournalStyle.Ghibli        -> { c -> c.watercolorBleed() }
    }
    return ScenePalette(
        skyTop              = mapper(skyTop),              skyBottom           = mapper(skyBottom),
        sunDiscColor        = mapper(sunDiscColor),        sunGlowColor        = mapper(sunGlowColor),
        sunFlareColor       = mapper(sunFlareColor),
        moonDiscColor       = mapper(moonDiscColor),       moonShadowColor     = mapper(moonShadowColor),
        starColor           = mapper(starColor),           constellationColor  = mapper(constellationColor),
        cloudColor          = mapper(cloudColor),          cloudShadowColor    = mapper(cloudShadowColor),
        cloudHighlightColor = mapper(cloudHighlightColor),
        mistColor           = mapper(mistColor),           mistStrongColor     = mapper(mistStrongColor),
        fireflyColor        = mapper(fireflyColor),        fireflyGlowColor    = mapper(fireflyGlowColor),
        horizonColor        = mapper(horizonColor),        birdColor           = mapper(birdColor)
    )
}

// ── 4 base palettes (one per TimeOfDay) ────────────────────────────────────────

private fun getBasePalette(tod: TimeOfDay): ScenePalette = when (tod) {
    TimeOfDay.Dawn -> ScenePalette(
        skyTop              = Color(0xFF4B5D8F),
        skyBottom           = Color(0xFFFFB07C),
        sunDiscColor        = Color(0xFFFFECCC),
        sunGlowColor        = Color(0xFFFF8C42).copy(alpha = 0.50f),
        sunFlareColor       = Color(0xFFFFAA66).copy(alpha = 0.30f),
        moonDiscColor       = Color(0xFFE2E8F0),
        moonShadowColor     = Color(0xFF1A1A2E),
        starColor           = Color.Transparent,
        constellationColor  = Color.Transparent,
        cloudColor          = Color(0xFFFFCFA8),
        cloudShadowColor    = Color(0xFFC07060),
        cloudHighlightColor = Color(0xFFFFF0D0),
        mistColor           = Color(0xFFFFE0D0).copy(alpha = 0.40f),
        mistStrongColor     = Color(0xFFFFB090).copy(alpha = 0.65f),
        fireflyColor        = Color.Transparent,
        fireflyGlowColor    = Color.Transparent,
        horizonColor        = Color(0xFF382329),
        birdColor           = Color(0xFF201015).copy(alpha = 0.85f)
    )
    TimeOfDay.Day -> ScenePalette(
        skyTop              = Color(0xFF4A90E2),
        skyBottom           = Color(0xFF90C8FF),
        sunDiscColor        = Color(0xFFFFFBE0),
        sunGlowColor        = Color(0xFFFFD700).copy(alpha = 0.45f),
        sunFlareColor       = Color(0xFFFFFF99).copy(alpha = 0.18f),
        moonDiscColor       = Color(0xFFD8DBE0),
        moonShadowColor     = Color(0xFF1A1A2E),
        starColor           = Color.Transparent,
        constellationColor  = Color.Transparent,
        cloudColor          = Color(0xFFFFFFFF),
        cloudShadowColor    = Color(0xFFB0C4DE),
        cloudHighlightColor = Color(0xFFFFFFFF),
        mistColor           = Color.Transparent,
        mistStrongColor     = Color.Transparent,
        fireflyColor        = Color.Transparent,
        fireflyGlowColor    = Color.Transparent,
        horizonColor        = Color(0xFF2C5E3A),
        birdColor           = Color.Transparent
    )
    TimeOfDay.Evening -> ScenePalette(
        skyTop              = Color(0xFF191970),
        skyBottom           = Color(0xFFD85A7F),
        sunDiscColor        = Color(0xFFFFDAB9),
        sunGlowColor        = Color(0xFFFF4500).copy(alpha = 0.55f),
        sunFlareColor       = Color(0xFFFF8844).copy(alpha = 0.28f),
        moonDiscColor       = Color(0xFFE2E8F0),
        moonShadowColor     = Color(0xFF1A1A2E),
        starColor           = Color(0xFFFFFFFF),
        constellationColor  = Color(0xFFFFFFFF).copy(alpha = 0.32f),
        cloudColor          = Color(0xFFE6A8D7),
        cloudShadowColor    = Color(0xFF704070),
        cloudHighlightColor = Color(0xFFFFD0E0),
        mistColor           = Color.Transparent,
        mistStrongColor     = Color.Transparent,
        fireflyColor        = Color(0xFFCEFF66),
        fireflyGlowColor    = Color(0xFFAAFF00).copy(alpha = 0.35f),
        horizonColor        = Color(0xFF1A1025),
        birdColor           = Color(0xFF100815).copy(alpha = 0.90f)
    )
    TimeOfDay.Night -> ScenePalette(
        skyTop              = Color(0xFF070B19),
        skyBottom           = Color(0xFF151B2E),
        sunDiscColor        = Color.Transparent,
        sunGlowColor        = Color.Transparent,
        sunFlareColor       = Color.Transparent,
        moonDiscColor       = Color(0xFFEFE8D5),
        moonShadowColor     = Color(0xFF050810),
        starColor           = Color(0xFFFFFFFF),
        constellationColor  = Color(0xFFB5C4D8).copy(alpha = 0.45f),
        cloudColor          = Color(0xFF2A3441),
        cloudShadowColor    = Color(0xFF111827),
        cloudHighlightColor = Color(0xFF374151),
        mistColor           = Color.Transparent,
        mistStrongColor     = Color.Transparent,
        fireflyColor        = Color(0xFFCEFF66),
        fireflyGlowColor    = Color(0xFFAAFF00).copy(alpha = 0.40f),
        horizonColor        = Color(0xFF04060C),
        birdColor           = Color.Transparent
    )
}

// ── Deterministic RNG cache for atmospheric features ───────────────────────────

private val featureRngCache = mutableMapOf<String, List<Float>>()

@Composable
private fun rememberFeatureRng(prefix: String, poolSize: Int, count: Int = 100): List<Float> {
    val key = "$prefix-$poolSize-$count"
    return remember(key) {
        featureRngCache.getOrPut(key) {
            val random = Random(key.hashCode())
            List(count) { random.nextFloat() }
        }
    }
}

// ── AtmosphericSkyboxScene — orchestrator (Layer 1.5) ──────────────────────────

/**
 * Renders the time-of-day skybox between [AnimatedWeatherScene] and [JournalWarmthOverlay].
 *
 * Feature matrix (Static / Gentle / Full):
 *  - Sky gradient                              — all tiers
 *  - Sun (Dawn/Day/Evening) or moon+phase      — all tiers
 *  - Per-journal ornament on celestial body     — all tiers
 *  - 5 cloud banks with per-style ornament     — Gentle / Full (non-Night)
 *  - Horizon silhouette (5-hump mountain)      — all tiers
 *  - 4 morning-mist bands                      — Dawn, Gentle / Full
 *  - 60 stars + 3 constellations + shooting    — Night, Full only
 *  - 12 fireflies + pulse glow                 — Evening always, Night non-Bullet, Gentle+ pulse
 *  - 4 bird V-formations drifting top edge     — Dawn / Evening, Full only
 */
@Composable
private fun AtmosphericSkyboxScene(
    tod: TimeOfDay,
    palette: ScenePalette,
    journalStyle: JournalStyle,
    animLevel: BackgroundAnimationLevel,
    isDark: Boolean,
    modifier: Modifier
) {
    val isStatic = animLevel == BackgroundAnimationLevel.Static
    val isFull   = animLevel == BackgroundAnimationLevel.Full
    val celestial = resolveCelestial(tod, System.currentTimeMillis())

    // Stable deterministic RNG pools per feature (no recomposition flicker).
    val cloudRng   = rememberFeatureRng("clouds",  poolSize = 5)
    val starRng    = rememberFeatureRng("stars",   poolSize = 60)
    val fireflyRng = rememberFeatureRng("firefly", poolSize = 12)
    val birdRng    = rememberFeatureRng("birds",   poolSize = 4)
    val celTexRng  = rememberFeatureRng("celTex",  poolSize = 10)

    // Animations — always created; values are gated by tier below.
    val txCloud by rememberInfiniteTransition(label = "skyCloud").animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(if (isFull) 60_000 else 120_000, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "cloudDrift"
    )
    val txTwinkle by rememberInfiniteTransition(label = "skyTwinkle").animateFloat(
        initialValue = 0.5f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(4_500, easing = LinearEasing), RepeatMode.Reverse),
        label = "twinkle"
    )
    val txFireflyPulse by rememberInfiniteTransition(label = "skyFirefly").animateFloat(
        initialValue = 0.5f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            tween(if (isFull) 3_000 else 8_000, easing = LinearEasing),
            RepeatMode.Reverse
        ),
        label = "fireflyPulse"
    )
    val txMist by rememberInfiniteTransition(label = "skyMist").animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(if (isFull) 18_000 else 40_000, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "mistDrift"
    )
    val txSun by rememberInfiniteTransition(label = "skySun").animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(if (isFull) 120_000 else 240_000, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "sunDrift"
    )

    // Use 0f when Static so all drift/pulse values are motionless.
    val cloudDrift   = if (isStatic) 0f else txCloud
    val twinkle      = txTwinkle
    val fireflyPulse = if (isStatic) 0.85f else txFireflyPulse
    val mistDrift    = if (isStatic) 0f else txMist
    val sunDrift     = if (isStatic) 0f else txSun

    // Hoist shooting-star phase capture out of DrawScope (fire once per recomposition, not per frame).
    val shootPhase = System.currentTimeMillis() % 25_000L

    Canvas(modifier = modifier.fillMaxSize()) {
        drawSky(palette)
        drawCelestialBodies(celestial, palette, journalStyle, sunDrift, celTexRng)
        if (tod != TimeOfDay.Night && animLevel != BackgroundAnimationLevel.Static) {
            drawCloudBanks(palette, journalStyle, cloudDrift, cloudRng, isFull)
        }
        drawHorizonAndMist(tod, palette, mistDrift)

        if (tod == TimeOfDay.Night && isFull) {
            drawStarsAndConstellations(palette, twinkle, starRng, shootPhase)
        }
        drawFireflies(tod, palette, journalStyle, isStatic, isFull, fireflyPulse, fireflyRng)
        if (isFull) {
            drawBirds(tod, palette, sunDrift, birdRng)
        }
    }
}

// ── Draw primitives (private DrawScope extensions) ─────────────────────────────

private fun DrawScope.drawSky(palette: ScenePalette) {
    drawRect(
        brush = Brush.verticalGradient(colors = listOf(palette.skyTop, palette.skyBottom)),
        size = size
    )
}

private fun DrawScope.drawCelestialBodies(
    body: CelestialBody,
    palette: ScenePalette,
    style: JournalStyle,
    sunDrift: Float,
    texRng: List<Float>
) {
    if (body.showSun && palette.sunDiscColor.alpha > 0.01f) {
        val cx = size.width  * body.sunCx01
        val cy = size.height * (body.sunCy01 + sunDrift * 0.006f)
        val r  = size.minDimension * body.sunR01

        // Outer warm halo (3.5x)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(palette.sunGlowColor.copy(alpha = 0.55f), Color.Transparent),
                center = Offset(cx, cy), radius = r * 3.5f
            ),
            radius = r * 3.5f, center = Offset(cx, cy)
        )
        // Mid halo (1.7x)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(palette.sunGlowColor, Color.Transparent),
                center = Offset(cx, cy), radius = r * 1.7f
            ),
            radius = r * 1.7f, center = Offset(cx, cy)
        )
        // Disc
        drawCircle(color = palette.sunDiscColor, radius = r, center = Offset(cx, cy))
        drawCelestialJournalOverlay(cx, cy, r, style, texRng)
    }
    if (body.showMoon && palette.moonDiscColor.alpha > 0.01f) {
        val cx = size.width  * body.moonCx01
        val cy = size.height * body.moonCy01
        val r  = size.minDimension * body.moonR01

        // Halo
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(palette.moonDiscColor.copy(alpha = 0.28f), Color.Transparent),
                center = Offset(cx, cy), radius = r * 4.0f
            ),
            radius = r * 4.0f, center = Offset(cx, cy)
        )
        drawCircle(color = palette.moonDiscColor, radius = r, center = Offset(cx, cy))
        // Phase shadow (offset disc masking part of the moon)
        val shadowOffset = (body.moonPhase - 0.5f) * 2.0f * r * 0.92f
        if (abs(shadowOffset) > r * 0.05f) {
            drawCircle(
                color = palette.moonShadowColor.copy(alpha = 0.85f),
                radius = r,
                center = Offset(cx + shadowOffset, cy)
            )
        }
        drawCelestialJournalOverlay(cx, cy, r, style, texRng)
    }
}

/**
 * Per-journal ornament drawn ON TOP of the celestial body:
 *  - Victorian: ornate double-ring + 8 compass tick marks + fleuron dot
 *  - Sketchbook: cream outline + graphite smudge + pencil cross-hatch
 *  - BulletJournal: 5×5 dot grid behind the disc
 *  - Ghibli: 5 watercolor wash blobs + 1 four-pointed sparkle
 */
private fun DrawScope.drawCelestialJournalOverlay(
    cx: Float, cy: Float, r: Float, style: JournalStyle, rng: List<Float>
) {
    when (style) {
        JournalStyle.Victorian -> {
            drawCircle(
                color = Color(0xFF8B4513).copy(alpha = 0.55f),
                radius = r * 1.55f, center = Offset(cx, cy),
                style = Stroke(width = 1.4f)
            )
            drawCircle(
                color = Color(0xFF8B4513).copy(alpha = 0.35f),
                radius = r * 0.86f, center = Offset(cx, cy),
                style = Stroke(width = 0.6f)
            )
            for (i in 0 until 8) {
                val angle = i * (kotlin.math.PI.toFloat() / 4f)
                val inner = r * 0.78f; val outer = r * 0.86f
                drawLine(
                    color = Color(0xFF8B4513).copy(alpha = 0.55f),
                    start = Offset(cx + cos(angle) * inner, cy + sin(angle) * inner),
                    end   = Offset(cx + cos(angle) * outer, cy + sin(angle) * outer),
                    strokeWidth = 0.8f
                )
            }
            // Fleuron dot at the top
            drawCircle(
                color = Color(0xFF8B4513).copy(alpha = 0.7f),
                radius = 2.2f,
                center = Offset(cx, cy - r * 1.55f)
            )
        }
        JournalStyle.Sketchbook -> {
            drawCircle(
                color = Color(0xFFFFFCF5).copy(alpha = 0.80f),
                radius = r * 1.06f, center = Offset(cx, cy),
                style = Stroke(width = 0.8f)
            )
            drawCircle(
                color = Color(0xFF5D4037).copy(alpha = 0.18f),
                radius = r * 0.45f,
                center = Offset(cx, cy + r * 0.32f)
            )
            drawLine(
                color = Color(0xFF5D4037).copy(alpha = 0.30f),
                start = Offset(cx - r * 0.5f, cy),
                end   = Offset(cx + r * 0.5f, cy),
                strokeWidth = 0.6f
            )
        }
        JournalStyle.BulletJournal -> {
            // 5×5 dot grid behind the celestial, anchored top-left of bounding box.
            val boxR = r * 2.4f
            val step = boxR / 4.5f
            val left = cx - boxR
            val top  = cy - boxR
            for (row in 0 until 5) {
                for (col in 0 until 5) {
                    val gx = left + col * step
                    val gy = top  + row * step
                    drawCircle(
                        color = Color(0xFF9E9E9E).copy(alpha = 0.18f),
                        radius = 1.3f,
                        center = Offset(gx, gy)
                    )
                }
            }
        }
        JournalStyle.Ghibli -> {
            for (i in 0 until 5) {
                val wx = cx + (rng[i * 2] - 0.5f) * r * 2.6f
                val wy = cy + (rng[i * 2 + 1] - 0.5f) * r * 2.6f
                val wr = r * (0.7f + rng[(i + 5).coerceAtMost(rng.size - 1)] * 0.8f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFE8B4B4).copy(alpha = 0.20f),
                            Color(0xFFE8B4B4).copy(alpha = 0f)
                        ),
                        center = Offset(wx, wy), radius = wr
                    ),
                    radius = wr, center = Offset(wx, wy)
                )
            }
            // Four-pointed sparkle above celestial
            drawCircle(
                color = Color(0xFFFFFFFF).copy(alpha = 0.85f),
                radius = 2.5f,
                center = Offset(cx + r * 1.4f, cy - r * 0.9f)
            )
            drawCircle(
                color = Color(0xFFFFFFFF).copy(alpha = 0.50f),
                radius = 5.5f,
                center = Offset(cx + r * 1.4f, cy - r * 0.9f)
            )
        }
    }
}

/** Draws 5 horizontal soft-edge cloud banks with per-style ornament underneath. At Full tier, each top puff gets an extra inner highlight stroke for a 3D "alive" feel. */
private fun DrawScope.drawCloudBanks(
    palette: ScenePalette,
    style: JournalStyle,
    drift: Float,
    rng: List<Float>,
    isFull: Boolean
) {
    val driftOffset = (drift - 0.5f) * size.width * 0.22f
    for (i in 0 until 5) {
        val widthRatio = 0.32f + rng[i * 7] * 0.28f
        val cxBase = rng[i * 7 + 1] * size.width
        val cyBase = size.height * (0.20f + i * 0.13f + rng[i * 7 + 2] * 0.05f)
        val cx = cxBase + driftOffset * (if (i % 2 == 0) 1f else -1f)
        val cloudW = size.width  * widthRatio
        val cloudH = size.height * 0.048f

        // Shadow disc beneath cloud
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(palette.cloudShadowColor.copy(alpha = 0.45f), Color.Transparent),
                center = Offset(cx, cyBase + cloudH * 0.6f),
                radius = cloudW * 0.55f
            ),
            radius = cloudW * 0.55f, center = Offset(cx, cyBase + cloudH * 0.6f)
        )
        // 4 overlapping soft puffs (main body)
        for (puff in 0 until 4) {
            val px = cx + (puff - 1.5f) * cloudW * 0.22f
            val py = cyBase + sin(puff.toFloat()) * cloudH * 0.10f
            val pr = cloudW * (0.32f - abs(puff - 1.5f) * 0.08f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(palette.cloudColor.copy(alpha = 0.85f), palette.cloudColor.copy(alpha = 0f)),
                    center = Offset(px, py), radius = pr
                ),
                radius = pr, center = Offset(px, py)
            )
            // Extra inner highlight at Full — makes clouds feel alive (only top puffs).
            if (isFull && puff in 1..2) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(palette.cloudHighlightColor.copy(alpha = 0.85f), Color.Transparent),
                        center = Offset(px, py - pr * 0.45f),
                        radius = pr * 0.45f
                    ),
                    radius = pr * 0.45f,
                    center = Offset(px, py - pr * 0.45f)
                )
            }
        }
        // Highlight on top
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(palette.cloudHighlightColor.copy(alpha = 0.55f), Color.Transparent),
                center = Offset(cx - cloudW * 0.06f, cyBase - cloudH * 0.25f),
                radius = cloudW * 0.32f
            ),
            radius = cloudW * 0.32f, center = Offset(cx - cloudW * 0.06f, cyBase - cloudH * 0.25f)
        )

        // Per-style ornament under cloud
        when (style) {
            JournalStyle.Victorian -> {
                // 4 cross-hatch rule lines
                for (h in 0 until 4) {
                    val hlY = cyBase + cloudH * (0.30f + h * 0.18f)
                    drawLine(
                        color = Color(0xFF8B4513).copy(alpha = 0.18f),
                        start = Offset(cx - cloudW * 0.36f, hlY),
                        end   = Offset(cx + cloudW * 0.36f, hlY),
                        strokeWidth = 0.5f
                    )
                }
            }
            JournalStyle.Sketchbook -> {
                // 12-dot pencil stipple under cloud shadow
                for (s in 0 until 12) {
                    val sx = cx + (rng[(s + i * 13) % rng.size] - 0.5f) * cloudW * 0.70f
                    val sy = cyBase + cloudH * (0.40f + rng[(s + i * 13 + 3) % rng.size] * 0.50f)
                    drawCircle(
                        color = Color(0xFF5D4037).copy(alpha = 0.20f),
                        radius = 0.8f,
                        center = Offset(sx, sy)
                    )
                }
            }
            JournalStyle.BulletJournal -> {
                // Tiny dot-grid beneath the cloud band
                val gridStep = size.minDimension / 56f
                val startX = cx - cloudW * 0.35f
                val endX   = cx + cloudW * 0.35f
                val startY = cyBase - cloudH * 0.10f
                val endY   = cyBase + cloudH * 0.85f
                var gx = startX
                while (gx < endX) {
                    var gy = startY
                    while (gy < endY) {
                        drawCircle(
                            color = Color(0xFF9E9E9E).copy(alpha = 0.10f),
                            radius = 0.7f,
                            center = Offset(gx, gy)
                        )
                        gy += gridStep
                    }
                    gx += gridStep
                }
            }
            JournalStyle.Ghibli -> { /* palette colors already watercolor-bleed */ }
        }
    }
}

/** Draws the 5-hump mountain silhouette + foreground hills + 4 dawn mist bands. */
private fun DrawScope.drawHorizonAndMist(tod: TimeOfDay, palette: ScenePalette, mistDrift: Float) {
    val horizonY = size.height * 0.78f

    val mountain = androidx.compose.ui.graphics.Path().apply {
        moveTo(0f, horizonY)
        lineTo(size.width * 0.18f, horizonY - size.height * 0.060f)
        lineTo(size.width * 0.32f, horizonY - size.height * 0.020f)
        lineTo(size.width * 0.50f, horizonY - size.height * 0.100f)
        lineTo(size.width * 0.68f, horizonY - size.height * 0.030f)
        lineTo(size.width * 0.85f, horizonY - size.height * 0.080f)
        lineTo(size.width, horizonY - size.height * 0.020f)
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }
    drawPath(mountain, color = palette.horizonColor)

    val hills = androidx.compose.ui.graphics.Path().apply {
        moveTo(0f, horizonY + size.height * 0.04f)
        lineTo(size.width * 0.30f, horizonY + size.height * 0.02f)
        lineTo(size.width * 0.55f, horizonY + size.height * 0.06f)
        lineTo(size.width * 0.80f, horizonY + size.height * 0.03f)
        lineTo(size.width, horizonY + size.height * 0.05f)
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }
    drawPath(hills, color = palette.horizonColor.copy(alpha = 0.7f))

    if (tod == TimeOfDay.Dawn && palette.mistColor.alpha > 0.01f) {
        for (i in 0 until 4) {
            val bandY = horizonY - size.height * 0.05f - i * size.height * 0.026f
            val bandH = size.height * 0.038f
            val driftOffset = (mistDrift * size.width * 0.05f) * (if (i % 2 == 0) 1f else -1f)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        palette.mistColor.copy(alpha = 0.40f),
                        palette.mistStrongColor.copy(alpha = 0.62f),
                        palette.mistColor.copy(alpha = 0.40f),
                        Color.Transparent
                    ),
                    startY = bandY - bandH * 0.5f,
                    endY = bandY + bandH * 0.5f
                ),
                topLeft = Offset(-size.width * 0.08f + driftOffset, bandY - bandH * 0.5f),
                size = androidx.compose.ui.geometry.Size(size.width * 1.16f, bandH)
            )
        }
    }
}

/** Draws 60 twinkling stars + 3 constellations + occasional shooting star (Night, Full). */
private fun DrawScope.drawStarsAndConstellations(
    palette: ScenePalette, twinkle: Float, rng: List<Float>, shootPhase: Long
) {
    if (palette.starColor.alpha < 0.01f) return

    // 60 twinkle stars
    for (i in 0 until 60) {
        val cx = size.width  * rng[i * 3]
        val cy = size.height * rng[i * 3 + 1]
        val r  = 0.8f + rng[i * 3 + 2] * 1.6f
        val baseAlpha = 0.45f + rng[i * 3] * 0.45f
        drawCircle(
            color = palette.starColor.copy(alpha = (baseAlpha * twinkle).coerceIn(0f, 0.95f)),
            radius = r,
            center = Offset(cx, cy)
        )
    }

    // Local helper — draw connected star constellation
    fun mark(stars: List<androidx.compose.ui.geometry.Offset>, lineWidth: Float, starR: Float) {
        for (s in stars) {
            drawCircle(
                color = palette.constellationColor,
                radius = starR,
                center = Offset(size.width * s.x, size.height * s.y)
            )
        }
        for (i in 0 until stars.size - 1) {
            drawLine(
                color = palette.constellationColor,
                start = Offset(size.width * stars[i].x,     size.height * stars[i].y),
                end   = Offset(size.width * stars[i + 1].x, size.height * stars[i + 1].y),
                strokeWidth = lineWidth
            )
        }
    }
    mark(
        stars = listOf(
            Offset(0.12f, 0.30f), Offset(0.20f, 0.32f),
            Offset(0.27f, 0.28f), Offset(0.35f, 0.26f)
        ),
        lineWidth = 0.8f, starR = 2.0f
    )
    mark(
        stars = listOf(
            Offset(0.55f, 0.45f), Offset(0.60f, 0.50f), Offset(0.65f, 0.55f)
        ),
        lineWidth = 0.9f, starR = 2.5f
    )
    mark(
        stars = listOf(
            Offset(0.78f, 0.38f), Offset(0.82f, 0.34f), Offset(0.86f, 0.38f),
            Offset(0.90f, 0.34f), Offset(0.94f, 0.38f)
        ),
        lineWidth = 0.7f, starR = 1.8f
    )

    // Shooting star — fires briefly every ~25 s (shootPhase captured at compose time, not draw time)
    if (shootPhase < 1500L) {
        val p = shootPhase / 1500f
        val sx = size.width  * (0.05f + p * 0.85f)
        val sy = size.height * (0.10f + p * 0.20f)
        drawLine(
            color = palette.constellationColor.copy(alpha = (1f - p).coerceIn(0f, 1f)),
            start = Offset(sx, sy),
            end   = Offset(sx + 60f, sy - 30f),
            strokeWidth = 2.5f
        )
        drawCircle(
            color = palette.starColor.copy(alpha = (1f - p).coerceIn(0f, 1f)),
            radius = 2.5f * (1f - p),
            center = Offset(sx + 60f, sy - 30f)
        )
    }
}

/** Draws 12 fireflies with halo + pulse glow.
 *  Skipped at Static tier (per the Static preset's "no moving things" contract).
 *  Skipped for BulletJournal (clean style rejects fireflies).
 *  Shown at Evening (always), and Night (for Victorian + Sketchbook + Ghibli). */
private fun DrawScope.drawFireflies(
    tod: TimeOfDay,
    palette: ScenePalette,
    style: JournalStyle,
    isStatic: Boolean,
    isFull: Boolean,
    pulse: Float,
    rng: List<Float>
) {
    val showFireflies = !isStatic &&
        palette.fireflyColor.alpha > 0.01f &&
        (tod == TimeOfDay.Evening || tod == TimeOfDay.Night) &&
        style != JournalStyle.BulletJournal
    if (!showFireflies) return

    val count = 12
    val flyCenterY = size.height * 0.55f
    for (i in 0 until count) {
        val baseCx = size.width * rng[i]
        val baseCy = flyCenterY + (rng[i + 1] - 0.5f) * size.height * 0.30f
        val drift = if (isFull) (rng[i + 2] - 0.5f) * size.width * 0.12f else 0f
        val basePulse = (rng[i + 3] * 0.5f + 0.5f) * pulse
        val cx = baseCx + drift
        val cy = baseCy + (rng[i + 4] - 0.5f) * 7f

        // Glow halo
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    palette.fireflyGlowColor.copy(alpha = (0.70f * basePulse).coerceIn(0f, 1f)),
                    palette.fireflyGlowColor.copy(alpha = 0f)
                ),
                radius = size.minDimension * 0.022f
            ),
            radius = size.minDimension * 0.022f,
            center = Offset(cx, cy)
        )
        // Core
        drawCircle(
            color = palette.fireflyColor.copy(alpha = (0.95f * basePulse).coerceIn(0f, 1f)),
            radius = 1.8f,
            center = Offset(cx, cy)
        )
    }
}

/** Draws 4 V-shaped bird silhouettes drifting at Dawn / Evening (Full tier only). */
private fun DrawScope.drawBirds(
    tod: TimeOfDay,
    palette: ScenePalette,
    globalDrift: Float,
    rng: List<Float>
) {
    val showBirds = palette.birdColor.alpha > 0.01f &&
        (tod == TimeOfDay.Dawn || tod == TimeOfDay.Evening)
    if (!showBirds) return

    val count = 4
    for (i in 0 until count) {
        val progress = ((globalDrift + rng[i * 3] * 0.6f) % 1f)
        val bx = size.width  * progress
        val by = size.height * (0.15f + rng[i * 3 + 1] * 0.20f)
        val wingSpan = size.minDimension * 0.04f
        val wingDip  = wingSpan * 0.32f

        drawLine(
            color = palette.birdColor,
            start = Offset(bx, by),
            end   = Offset(bx - wingSpan * 0.5f, by + wingDip),
            strokeWidth = 2.5f
        )
        drawLine(
            color = palette.birdColor,
            start = Offset(bx, by),
            end   = Offset(bx + wingSpan * 0.5f, by + wingDip),
            strokeWidth = 2.5f
        )
        drawCircle(color = palette.birdColor, radius = 2.2f, center = Offset(bx, by))
    }
}
