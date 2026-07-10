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
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import fieldmind.research.app.features.field.presentation.theme.FieldMindTheme
import fieldmind.research.app.shared.presentation.theme.BackgroundAnimationLevel
import fieldmind.research.app.shared.presentation.theme.JournalConfig
import fieldmind.research.app.shared.presentation.theme.JournalPresets
import fieldmind.research.app.shared.presentation.theme.JournalStyle
import fieldmind.research.app.shared.presentation.theme.LocalBackgroundAnimation
import fieldmind.research.app.shared.presentation.theme.LocalJournalStyle
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

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawJournalTexture(
                name = journalConfig.textureName,
                opacity = textureOpacity,
                morph = morphProgress
            )
        }
    } else if (textureOpacity > 0.01f) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawJournalTexture(
                name = journalConfig.textureName,
                opacity = textureOpacity,
                morph = 0f
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
    morph: Float
) {
    when (name) {
        "parchment" -> drawParchmentTexture(opacity, morph)
        "paper" -> drawPaperTexture(opacity, morph)
        "dotgrid" -> drawDotGridTexture(opacity, morph)
        "watercolor" -> drawWatercolorTexture(opacity, morph)
    }
}

/**
 * Parchment texture: warm mottled organic spots with varying size and opacity.
 * Creates an aged look with subtle dark/light variations across the surface.
 */
private fun DrawScope.drawParchmentTexture(opacity: Float, morph: Float) {
    val warmColor = Color(0xFF8B6914)
    val rng = rememberTextureRng("parchment")

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
private fun DrawScope.drawPaperTexture(opacity: Float, morph: Float) {
    val rng = rememberTextureRng("paper")
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
private fun DrawScope.drawDotGridTexture(opacity: Float, morph: Float) {
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
private fun DrawScope.drawWatercolorTexture(opacity: Float, morph: Float) {
    val rng = rememberTextureRng("watercolor")
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
