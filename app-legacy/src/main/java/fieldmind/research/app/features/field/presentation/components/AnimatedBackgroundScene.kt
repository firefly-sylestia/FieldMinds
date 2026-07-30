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
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * ════════════════════════════════════════════════════════════════════════
 *  🌿 AnimatedBackgroundScene — static backdrop
 *
 *  Purpose: Render a subtle warm-tinted background layer blended with
 *  [AnimatedWeatherScene].
 *
 *  v0.51.0 — JournalConfig dependency removed. Shows a static warmth
 *  overlay + vignette. Texture layers are dormant (disabled across all
 *  presets since v0.50.3).
 * ════════════════════════════════════════════════════════════════════════
 */

/** Warm tint colour used for the static background overlays. */
private val WarmthColor = Color(0xFFFBF7F0)

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
    val isDark = FieldMindTheme.colors.isDark

    // In preview/inspection mode, show a static gradient
    if (LocalInspectionMode.current) {
        StaticBackground(isDark, modifier)
        return
    }

    // When the user has toggled "Background weather animation" OFF, hold a static
    // backdrop without any weather scene.
    if (!weatherBackgroundAnimation) {
        Box(modifier = modifier.fillMaxSize()) {
            WarmthOverlay(isDark)
            VignetteOverlay(isDark)
        }
        return
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Layer 1: Weather scene
        AnimatedWeatherScene(
            weatherCode = weatherCode,
            temperature = temperature,
            sunrise = sunrise,
            sunset = sunset,
            forceNight = forceNight,
            showCloudAnimation = showCloudAnimation,
            modifier = Modifier.fillMaxSize()
        )

        // Layer 2: Static warmth tint overlay
        WarmthOverlay(isDark)

        // Layer 3: Vignette for depth and focus
        VignetteOverlay(isDark)
    }
}

/** Static gradient background for preview/inspection mode. */
@Composable
private fun StaticBackground(isDark: Boolean, modifier: Modifier) {
    val topColor = if (isDark) {
        Color(
            (WarmthColor.red * 0.3f).coerceAtMost(1f),
            (WarmthColor.green * 0.25f).coerceAtMost(1f),
            (WarmthColor.blue * 0.3f).coerceAtMost(1f)
        )
    } else {
        Color(
            (WarmthColor.red * 0.9f + 0.1f).coerceAtMost(1f),
            (WarmthColor.green * 0.9f + 0.1f).coerceAtMost(1f),
            (WarmthColor.blue * 0.9f + 0.1f).coerceAtMost(1f)
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

/** Static warmth overlay — subtle tint over the scene. */
@Composable
private fun WarmthOverlay(isDark: Boolean) {
    val baseAlpha = if (isDark) 0.06f else 0.08f
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            color = WarmthColor.copy(alpha = baseAlpha.coerceIn(0f, 0.2f)),
            size = size
        )
    }
}

/** Subtle vignette (darkened edges) to focus attention on center content. */
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
