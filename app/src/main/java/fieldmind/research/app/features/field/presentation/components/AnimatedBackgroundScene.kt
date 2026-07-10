package fieldmind.research.app.features.field.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalInspectionMode
import fieldmind.research.app.features.field.presentation.theme.FieldMindTheme
import fieldmind.research.app.shared.presentation.theme.JournalConfig
import fieldmind.research.app.shared.presentation.theme.LocalJournalStyle
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * ════════════════════════════════════════════════════════════════════════
 *  🌿 AnimatedBackgroundScene — static per-journal backdrop
 *
 *  Purpose: Render the active journal aesthetic as a full-screen background
 *  layer, blended with [AnimatedWeatherScene].
 *
 *  History: Phase 2 of the Whimsical Redesign (v0.48.0) added an atmospheric
 *  skybox here — a 4-time-of-day (Dawn / Day / Evening / Night) scene with
 *  drifting clouds, twinkling stars, firefly pulses, shooting stars, and
 *  bird formations. v0.49.0 stripped it: users reported the skybox caused
 *  device lag and overheating. The 5 `rememberInfiniteTransition` slots +
 *  8 heavy DrawScope extensions are gone. Layer 1 (`AnimatedWeatherScene`,
 *  controlled by its own `weatherBackgroundAnimation` flag) still runs.
 *  Layers 2 (warmth), 3 (texture), 4 (vignette) are now static per-journal
 *  overlays — no transitions, no morphs, no per-tick work.
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
 * ════════════════════════════════════════════════════════════════════════
 */
@Composable
fun AnimatedBackgroundScene(
    weatherCode: Int,
    temperature: Double?,
    sunrise: String? = null,
    sunset: String? = null,
    forceNight: Boolean? = null,
    showCloudAnimation: Boolean = true,
    weatherBackgroundAnimation: Boolean = true,
    modifier: Modifier = Modifier
) {
    val journalConfig = LocalJournalStyle.current
    val isDark = FieldMindTheme.colors.isDark

    // In preview/inspection mode, show a static gradient
    if (LocalInspectionMode.current) {
        StaticJournalBackground(journalConfig, isDark, modifier)
        return
    }

    // When the user has toggled "Background weather animation" OFF, hold a static
    // journal-themed gradient without any weather scene.
    if (!weatherBackgroundAnimation) {
        Box(modifier = modifier.fillMaxSize()) {
            JournalWarmthOverlay(journalConfig, isDark)
            if (journalConfig.showTexture) {
                JournalTextureOverlay(journalConfig)
            }
            VignetteOverlay(isDark)
        }
        return
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Layer 1: Weather scene (own animation toggle, untouched by v0.49.0).
        AnimatedWeatherScene(
            weatherCode = weatherCode,
            temperature = temperature,
            sunrise = sunrise,
            sunset = sunset,
            forceNight = forceNight,
            showCloudAnimation = showCloudAnimation,
            modifier = Modifier.fillMaxSize()
        )

        // Layer 2: Per-journal warmth tint overlay — static.
        JournalWarmthOverlay(journalConfig, isDark)

        // Layer 3: Per-journal paper texture overlay — static.
        if (journalConfig.showTexture) {
            JournalTextureOverlay(journalConfig)
        }

        // Layer 4: Vignette for depth and focus — static.
        VignetteOverlay(isDark)
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
 *
 * Phase 2 removed: this was previously read `BackgroundAnimationLevel` from
 * the environment and added a slow warmth pulse when Full was selected. The
 * pulse is gone; the tint is now uniformly static.
 */
@Composable
private fun JournalWarmthOverlay(
    journalConfig: JournalConfig,
    isDark: Boolean
) {
    val warmthColor = journalConfig.backgroundWarmth
    val baseAlpha = if (isDark) 0.06f else 0.08f

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            color = warmthColor.copy(alpha = baseAlpha.coerceIn(0f, 0.2f)),
            size = size
        )
    }
}

/**
 * Renders a subtle paper texture overlay that gives the background a tactile feel.
 * The texture pattern changes based on the journal style's textureName:
 * - "parchment": subtle mottled warmth (Victorian)
 * - "paper": gentle fiber texture (Sketchbook)
 * - "dotgrid": subtle dot pattern (BulletJournal)
 * - "watercolor": soft organic washes (Ghibli)
 *
 * Phase 2 removed: a `morph: Float` parameter driven by a 30-second
 * `rememberInfiniteTransition` was previously passed through all 4 texture
 * sub-routines. The morph is gone; textures are now first-paint stable.
 */
@Composable
private fun JournalTextureOverlay(journalConfig: JournalConfig) {
    val textureOpacity = journalConfig.textureOpacity.coerceIn(0f, 0.25f)
    if (textureOpacity <= 0.01f) return

    val rng = rememberTextureRng(journalConfig.textureName)
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawJournalTexture(
            name = journalConfig.textureName,
            opacity = textureOpacity,
            rng = rng
        )
    }
}

/**
 * Draws a subtle vignette (darkened edges) to focus attention on center content.
 */
@Composable
private fun VignetteOverlay(isDark: Boolean) {
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
//  Texture Drawing Routines (static, first-paint stable)
// ════════════════════════════════════════════════════════════════════════

/**
 * Dispatch to the per-style texture routine.
 */
private fun DrawScope.drawJournalTexture(
    name: String,
    opacity: Float,
    rng: List<Float>
) {
    when (name) {
        "parchment" -> drawParchmentTexture(opacity, rng)
        "paper" -> drawPaperTexture(opacity, rng)
        "dotgrid" -> drawDotGridTexture(opacity, rng)
        "watercolor" -> drawWatercolorTexture(opacity, rng)
    }
}

/**
 * Parchment texture: warm mottled organic spots with varying size and opacity.
 */
private fun DrawScope.drawParchmentTexture(opacity: Float, rng: List<Float>) {
    val warmColor = Color(0xFF8B6914)

    // Large-scale mottling
    for (i in 0..8) {
        val cx = size.width * rng[i * 3]
        val cy = size.height * rng[i * 3 + 1]
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
        val cx = size.width * rng[i * 7 % 63]
        val cy = size.height * rng[i * 7 % 63 + 1]
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
 */
private fun DrawScope.drawPaperTexture(opacity: Float, @Suppress("UNUSED_PARAMETER") rng: List<Float>) {
    val fiberColor = Color(0xFF8B7355)

    for (i in 0..15) {
        val cx = size.width * rng[i * 5]
        val cy = size.height * rng[i * 5 + 1]
        val length = size.maxDimension * (0.02f + rng[i * 5 + 2] * 0.04f)
        val angle = rng[i * 5 + 3] * 360f
        val alpha = opacity * 0.4f * rng[i * 5 + 4]

        val endX = cx + cos(angle) * length
        val endY = cy + sin(angle) * length

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
private fun DrawScope.drawDotGridTexture(opacity: Float, @Suppress("UNUSED_PARAMETER") rng: List<Float>) {
    val dotColor = Color(0xFF9E9E9E)
    val spacing = size.minDimension / 28f

    var x = 0f
    while (x < size.width + spacing) {
        var y = 0f
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
 */
private fun DrawScope.drawWatercolorTexture(opacity: Float, rng: List<Float>) {
    val washColor = Color(0xFFD4A574)

    // Large soft washes
    for (i in 0..5) {
        val cx = size.width * rng[i * 11]
        val cy = size.height * rng[i * 11 + 1]
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
        val cx = size.width * rng[i * 13 + 5]
        val cy = size.height * rng[i * 13 + 6]
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
//  Texture RNG Cache (small, first-paint stable)
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
            List(200) { rng.nextFloat() }
        }
    }
}
