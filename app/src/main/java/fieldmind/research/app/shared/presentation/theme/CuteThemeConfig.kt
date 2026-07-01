package fieldmind.research.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * ════════════════════════════════════════════════════════════════════════
 *  ✨ Cute Theme Config — Soft shadows, plush elevations, adorable defaults
 *
 *  Purpose: Centralise every elevation, shadow, and card preset so the
 *  entire app breathes a consistent, soft, layered, "cute" aesthetic.
 *  All values are deliberately gentle — nothing harsh or sharp.
 *
 *  🌸 Pastel Theme: See [fieldmind.research.app.ui.theme.PastelPrimaryLight] et al.
 *  in [Color.kt] for the full pastel M3 palette, wired as "Pastel" in
 *  [fieldmind.research.app.ui.theme.getCustomColorScheme]. Selectable from
 *  Settings → Appearance → Color scheme.
 *
 *  Pastel entity colors: [fieldmind.research.app.features.field.presentation.theme.PastelLightFieldMindColors]
 *  in [FieldMindTheme.kt] — soft sage, sky blue, blush, lavender for each research entity type.
 * ════════════════════════════════════════════════════════════════════════
 */

/**
 * Named elevation presets — every magic number lives here.
 *
 * Convention: "plush" tiers correspond to information depth.
 *   plushTier1 (2dp)  → subtle lift for background surfaces / low-focus cards
 *   plushTier2 (4dp)  → standard card elevation (SettingsGroupCard, EntityCard)
 *   plushTier3 (6dp)  → prominent cards, FeaturedCard, hero surfaces
 *   plushTier4 (8dp)  → dialogs, bottom sheets, floating elements
 *   plushTier5 (12dp) → highest emphasis (modals, pickers)
 */
object CuteElevations {
    /** Subtle lift — background surfaces, low-focus info cards. */
    val plushTier1: Dp = 2.dp
    /** Standard card elevation — most cards use this. */
    val plushTier2: Dp = 4.dp
    /** Prominent cards — featured content, hero surfaces. */
    val plushTier3: Dp = 6.dp
    /** Dialogs, bottom sheets, floating elements. */
    val plushTier4: Dp = 8.dp
    /** Highest emphasis — modals, pickers, important overlays. */
    val plushTier5: Dp = 12.dp

    // ── Quick-access presets for CardDefaults ──
    val cardDefault
        @Composable
        get() = CardDefaults.cardElevation(defaultElevation = plushTier2)

    val cardProminent
        @Composable
        get() = CardDefaults.cardElevation(defaultElevation = plushTier3)

    val cardDialog
        @Composable
        get() = CardDefaults.cardElevation(defaultElevation = plushTier4)
}

/**
 * Shadow style configuration for a single tier.
 *
 * @property elevation the shadow height in dp.
 */
data class CuteShadow(
    val elevation: Dp
)

/**
 * Reusable shadow presets that map 1:1 to [CuteElevations] tiers.
 */
object CuteShadows {
    /** Subtle — for tier-1 elements. */
    val subtle: CuteShadow
        get() = CuteShadow(elevation = CuteElevations.plushTier1)
    /** Standard — for tier-2 cards. */
    val standard: CuteShadow
        get() = CuteShadow(elevation = CuteElevations.plushTier2)
    /** Prominent — for tier-3 featured surfaces. */
    val prominent: CuteShadow
        get() = CuteShadow(elevation = CuteElevations.plushTier3)
    /** Float — for tier-4 floating elements. */
    val float: CuteShadow
        get() = CuteShadow(elevation = CuteElevations.plushTier4)
}

/**
 * Convenient card colour + shape + elevation presets.
 *
 * Use these as one-shot arguments to [Card] / [Surface]:
 * ```
 * Card(
 *     shape = CuteCardDefaults.Shape,
 *     colors = CuteCardDefaults.colors(),
 *     elevation = CuteElevations.cardDefault,
 * ) { ... }
 * ```
 */
object CuteCardDefaults {
    /** Standard cute card shape — very rounded, pill-like. */
    val Shape: Shape = RoundedCornerShape(32.dp)
    /** Slightly smaller card shape for inline / compact cards. */
    val ShapeCompact: Shape = RoundedCornerShape(24.dp)
    /** Large hero card shape. */
    val ShapeHero: Shape = RoundedCornerShape(36.dp)

    /**
     * Default card colours — uses surfaceContainerLowest so the card
     * contrasts distinctly from the background (surface / surfaceContainerLow).
     * In light mode this is nearly white; in dark mode it's a lighter surface.
     *
     * @param containerColor override if a tinted card is needed.
     */
    @Composable
    fun colors(
        containerColor: Color = MaterialTheme.colorScheme.surface
    ) = CardDefaults.cardColors(containerColor = containerColor)

    /** Tinted card with an accent colour at a very soft alpha. */
    @Composable
    fun tinted(accent: Color, isDark: Boolean = false) =
        CardDefaults.cardColors(
            containerColor = accent.copy(alpha = if (isDark) 0.20f else 0.08f)
        )
}

/**
 * A [Modifier] extension that applies a soft custom shadow.
 *
 * This gives a plush, layered look: the elevation shadow lifts the card
 * off the background for a clear visual separation.
 *
 * @param elevation the dp height of the shadow.
 * @param shape the shape to clip the shadow to (defaults to pill shape).
 */
fun Modifier.cuteShadow(
    elevation: Dp = CuteElevations.plushTier2,
    shape: Shape = CuteCardDefaults.Shape
): Modifier = this.then(
    shadow(elevation = elevation, shape = shape, clip = false)
)
