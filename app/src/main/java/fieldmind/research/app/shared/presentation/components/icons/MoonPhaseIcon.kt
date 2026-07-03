package fieldmind.research.app.shared.presentation.components.icons

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.floor
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Enum representing the 8 major lunar phases.
 * Each phase has a display name and a numerical phase value (0.0–1.0)
 * where 0.0 = New Moon, 0.25 = First Quarter, 0.5 = Full Moon, 0.75 = Last Quarter.
 */
enum class MoonPhase(
    val displayName: String,
    val phaseValue: Float
) {
    NewMoon("New Moon", 0f),
    WaxingCrescent("Waxing Crescent", 0.125f),
    FirstQuarter("First Quarter", 0.25f),
    WaxingGibbous("Waxing Gibbous", 0.375f),
    FullMoon("Full Moon", 0.5f),
    WaningGibbous("Waning Gibbous", 0.625f),
    ThirdQuarter("Third Quarter", 0.75f),
    WaningCrescent("Waning Crescent", 0.875f);

    companion object {
        /** Returns the [MoonPhase] closest to the given phase value (0.0–1.0). */
        fun fromPhaseValue(value: Float): MoonPhase {
            val normalized = value - floor(value)
            return entries.minByOrNull { abs(it.phaseValue - normalized) } ?: NewMoon
        }

        /** Computes the current moon phase value (0.0–1.0) for today's date. */
        fun currentPhaseValue(): Float {
            val knownNewMoon = LocalDate.of(2000, 1, 6)
            val today = LocalDate.now()
            val daysSince = ChronoUnit.DAYS.between(knownNewMoon, today).toDouble()
            val lunations = daysSince / 29.53058770576
            return (lunations - floor(lunations)).toFloat()
        }

        /** Returns the current [MoonPhase] for today's date. */
        fun current(): MoonPhase = fromPhaseValue(currentPhaseValue())
    }
}

/**
 * A detailed moon phase icon rendered with Compose Canvas.
 *
 * Renders a beautiful moon phase with:
 * - Outer glow halo
 * - Moon body filled with the tint color
 * - Mare/crater surface features (dark basaltic plains representing the
 *   actual lunar maria: Mare Imbrium, Mare Serenitatis, Mare Tranquillitatis, etc.)
 * - Phase shadow overlay that accurately shows the current moon phase
 *
 * Usage:
 * ```kotlin
 * MoonPhaseIcon(
 *     phase = MoonPhase.FullMoon,
 *     tint = Color.White,
 *     size = 32.dp,
 *     showGlow = true
 * )
 * ```
 */
@Composable
fun MoonPhaseIcon(
    phase: MoonPhase = MoonPhase.current(),
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    shadowColor: Color = Color(0xFF0A0A1A),
    size: Dp = 24.dp,
    showGlow: Boolean = true,
    animatedGlow: Boolean = false
) {
    val isInspection = LocalInspectionMode.current
    val glowAlpha = if (showGlow) {
        if (animatedGlow && !isInspection) {
            val transition = rememberInfiniteTransition(label = "moonGlow")
            val glow by transition.animateFloat(
                initialValue = 0.6f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(3000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "moonGlowPulse"
            )
            glow
        } else 0.8f
    } else 0f

    Box(
        modifier = modifier
            .size(size)
            .semantics(mergeDescendants = true) {
                if (contentDescription != null) {
                    this.contentDescription = contentDescription
                }
                this.role = Role.Image
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val r = minOf(size.width, size.height) * 0.42f

            // Glow halo
            if (showGlow && glowAlpha > 0f) {
                drawCircle(
                    color = tint.copy(alpha = 0.12f * glowAlpha),
                    radius = r * 2.5f,
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = tint.copy(alpha = 0.06f * glowAlpha),
                    radius = r * 4.0f,
                    center = Offset(cx, cy)
                )
            }

            // Moon body
            drawCircle(color = tint, radius = r, center = Offset(cx, cy))

            // Mare & crater surface features
            if (r >= 8f) {
                drawMoonSurfaceFeatures(cx, cy, r, tint)
            }

            // Phase shadow
            drawPhaseShadow(phase.phaseValue, cx, cy, r, shadowColor)
        }
    }
}

/**
 * Draws the lunar mare (dark basaltic plains) and crater highlights to give
 * the moon surface texture. Uses the actual positions of prominent lunar features:
 *
 * - Mare Imbrium (upper-left large dark plain)
 * - Mare Serenitatis (upper-center mid-sized plain)
 * - Mare Tranquillitatis (right-center medium plain)
 * - Mare Fecunditatis (lower-right smaller plain)
 * - Mare Nubium (lower-center plain)
 * - Oceanus Procellarum (left side large plain)
 * - Mare Frigoris (top elongated dark area)
 * - Mare Orientale (far-left subtle ring)
 * - Tycho crater (lower-center bright spot with rays)
 * - Copernicus crater (center-left bright crater)
 *
 * All positions are relative to the moon center and scaled to the current radius.
 */
private fun DrawScope.drawMoonSurfaceFeatures(cx: Float, cy: Float, r: Float, tint: Color) {
    // Mare/feature positions and sizes as fractions of moon radius
    // Position (x, y) where (0, 0) = center, (-1, 0) = left edge, (0, -1) = top edge
    data class LunarFeature(
        val xOffset: Float,    // Fraction of r from center
        val yOffset: Float,
        val widthFraction: Float,  // Fraction of r
        val heightFraction: Float,
        val alphaMul: Float    // Darkness multiplier vs base
    )

    val features = listOf(
        // Mare Imbrium — large dark plain in upper-left
        LunarFeature(-0.35f, -0.30f, 0.40f, 0.30f, 1.0f),
        // Mare Serenitatis — mid-sized, upper-center-right
        LunarFeature(-0.05f, -0.25f, 0.25f, 0.22f, 0.85f),
        // Mare Tranquillitatis — medium, right side (Apollo 11 landing site)
        LunarFeature(0.25f, -0.10f, 0.30f, 0.25f, 0.80f),
        // Mare Fecunditatis — smaller, lower-right
        LunarFeature(0.40f, 0.20f, 0.22f, 0.18f, 0.75f),
        // Mare Nubium — lower-center
        LunarFeature(-0.15f, 0.30f, 0.30f, 0.20f, 0.70f),
        // Oceanus Procellarum — large dark area on left side
        LunarFeature(-0.45f, 0.05f, 0.35f, 0.45f, 0.85f),
        // Mare Frigoris — elongated dark area at top
        LunarFeature(-0.20f, -0.42f, 0.55f, 0.12f, 0.75f),
        // Mare Vaporum — small, center
        LunarFeature(-0.12f, 0.02f, 0.15f, 0.12f, 0.65f),
        // Mare Insularum — center-left
        LunarFeature(-0.25f, 0.08f, 0.20f, 0.15f, 0.60f),
        // Mare Cognitum — lower-center-left
        LunarFeature(-0.30f, 0.28f, 0.18f, 0.12f, 0.55f),
        // Mare Orientale — subtle ring on far left edge
        LunarFeature(-0.65f, -0.05f, 0.18f, 0.18f, 0.35f),
        // Sinus Iridum — bay on northwest edge of Imbrium
        LunarFeature(-0.40f, -0.20f, 0.15f, 0.10f, 0.55f),
        // Lacus Mortis — small dark patch upper-right
        LunarFeature(0.20f, -0.38f, 0.10f, 0.08f, 0.50f),
    )

    // Feature color: darker version of tint with appropriate alpha
    val featureColor = if (tint == Color.White || tint == Color(0xFFECEFF1)) {
        Color(0xFF607D8B).copy(alpha = 0.25f)
    } else {
        tint.copy(
            red = (tint.red * 0.5f).coerceIn(0f, 1f),
            green = (tint.green * 0.5f).coerceIn(0f, 1f),
            blue = (tint.blue * 0.5f).coerceIn(0f, 1f),
            alpha = tint.alpha * 0.20f
        )
    }

    // Draw each mare/feature as an oval
    features.forEach { f ->
        val featureR = r * f.widthFraction * 0.5f
        val featureRh = r * f.heightFraction * 0.5f
        drawOval(
            color = featureColor.copy(alpha = featureColor.alpha * f.alphaMul),
            topLeft = Offset(
                cx + f.xOffset * r - featureR,
                cy + f.yOffset * r - featureRh
            ),
            size = androidx.compose.ui.geometry.Size(featureR * 2f, featureRh * 2f)
        )
    }

    // ── Crater highlights (bright impact craters) ──
    // These are subtle bright spots representing prominent craters
    data class CraterHighlight(
        val xOffset: Float,
        val yOffset: Float,
        val size: Float,  // fraction of r
        val brightness: Float
    )

    // Skip tiny craters for small rendering sizes
    if (r < 14f) return

    val craters = listOf(
        // Tycho — prominent bright crater with rays (lower-center)
        CraterHighlight(-0.08f, 0.42f, 0.08f, 0.35f),
        // Copernicus — bright crater (center-left)
        CraterHighlight(-0.30f, 0.12f, 0.06f, 0.25f),
        // Kepler — bright crater (left)
        CraterHighlight(-0.48f, 0.20f, 0.04f, 0.20f),
        // Aristarchus — very bright crater (upper-left)
        CraterHighlight(-0.42f, -0.25f, 0.04f, 0.30f),
        // Plato — dark-floor crater (upper-left)
        CraterHighlight(-0.15f, -0.40f, 0.05f, 0.15f),
        // Grimaldi — dark-floor crater (far left)
        CraterHighlight(-0.60f, 0.05f, 0.05f, 0.12f),
        // Langrenus — bright crater (lower-right)
        CraterHighlight(0.52f, 0.28f, 0.04f, 0.18f),
        // Petavius — large crater (lower-right)
        CraterHighlight(0.55f, 0.10f, 0.06f, 0.12f),
    )

    val highlightColor = if (tint == Color.White || tint == Color(0xFFECEFF1)) {
        Color.White.copy(alpha = 0.20f)
    } else {
        tint.copy(
            red = (tint.red * 1.3f).coerceIn(0f, 1f),
            green = (tint.green * 1.3f).coerceIn(0f, 1f),
            blue = (tint.blue * 1.3f).coerceIn(0f, 1f),
            alpha = tint.alpha * 0.15f
        )
    }

    craters.forEach { c ->
        drawCircle(
            color = highlightColor.copy(alpha = highlightColor.alpha * c.brightness),
            radius = r * c.size * 0.5f,
            center = Offset(cx + c.xOffset * r, cy + c.yOffset * r)
        )
    }

    // ── Crater rim shadows (subtle dark rings for larger craters) ──
    if (r >= 24f) {
        data class CraterRim(val xOffset: Float, val yOffset: Float, val size: Float)
        val rims = listOf(
            CraterRim(-0.08f, 0.42f, 0.09f),  // Tycho rim
            CraterRim(-0.30f, 0.12f, 0.07f),  // Copernicus rim
            CraterRim(-0.48f, 0.20f, 0.05f),  // Kepler rim
            CraterRim(0.52f, 0.28f, 0.05f),   // Langrenus rim
        )
        rims.forEach { rim ->
            drawCircle(
                color = featureColor.copy(alpha = 0.15f),
                radius = r * rim.size * 0.5f,
                center = Offset(cx + rim.xOffset * r, cy + rim.yOffset * r),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = r * 0.02f)
            )
        }
    }
}

/**
 * Draws the phase shadow — a circle that covers the unlit portion of the moon.
 *
 * For waxing phases (0.0–0.5): shadow on the left, revealing from the right.
 * For waning phases (0.5–1.0): shadow on the right, revealing from the left.
 * At 0.0 (new moon): full shadow. At 0.5 (full moon): no shadow.
 */
private fun DrawScope.drawPhaseShadow(
    phaseValue: Float,
    cx: Float,
    cy: Float,
    radius: Float,
    shadowColor: Color
) {
    val phase = phaseValue - floor(phaseValue)

    // The shadow is a circle of the same radius, shifted horizontally
    // Linear mapping: phase 0→0, 0.5→2*radius, 1.0→0 (from the other side)
    val shadowOffset: Float = if (phase <= 0.5f) {
        // Waxing: shadow moved right, revealing illuminated portion on the right
        phase * 4f * radius
    } else {
        // Waning: shadow moved left, revealing illuminated portion on the left
        -(1f - phase) * 4f * radius
    }

    // Draw shadow circle
    drawCircle(
        color = shadowColor,
        radius = radius + 1f,  // +1 pixel overlap to avoid seam
        center = Offset(cx + shadowOffset, cy)
    )

    // Subtle soft edge: draw a thin blurred line along the shadow terminator
    // The terminator is at the intersection of the moon circle and shadow circle
    val terminatorX = if (phase <= 0.5f) {
        cx + radius * (1f - phase * 2f).coerceIn(-1f, 1f)
    } else {
        cx - radius * (1f - (phase - 0.5f) * 2f).coerceIn(-1f, 1f)
    }

    // Only draw the edge blur when there's a visible terminator
    if (phase in 0.05f..0.45f || phase in 0.55f..0.95f) {
        val edgeHeightFraction = 0.75f  // Shorter than full diameter
        val edgeAlpha = 0.10f
        val edgeWidth = radius * 0.035f
        drawLine(
            color = Color.Black.copy(alpha = edgeAlpha),
            start = Offset(terminatorX, cy - radius * edgeHeightFraction),
            end = Offset(terminatorX, cy + radius * edgeHeightFraction),
            strokeWidth = edgeWidth
        )
    }
}

/**
 * Returns the display name for a moon phase value (0.0–1.0).
 */
fun moonPhaseDisplayName(phaseValue: Float): String =
    MoonPhase.fromPhaseValue(phaseValue).displayName

/**
 * Returns the [MoonPhase] matching a textual phase name.
 * Used to map weather/moon phase text labels to the appropriate enum value.
 */
fun moonPhaseFromName(phaseName: String): MoonPhase {
    val name = phaseName.trim().lowercase()
    return when {
        name.startsWith("new") -> MoonPhase.NewMoon
        name.startsWith("waxing crescent") -> MoonPhase.WaxingCrescent
        name.startsWith("first") || name == "quarter" -> MoonPhase.FirstQuarter
        name.startsWith("waxing gibbous") -> MoonPhase.WaxingGibbous
        name.startsWith("full") -> MoonPhase.FullMoon
        name.startsWith("waning gibbous") -> MoonPhase.WaningGibbous
        name.startsWith("last") || name.startsWith("third") -> MoonPhase.ThirdQuarter
        name.startsWith("waning crescent") -> MoonPhase.WaningCrescent
        else -> MoonPhase.NewMoon
    }
}
