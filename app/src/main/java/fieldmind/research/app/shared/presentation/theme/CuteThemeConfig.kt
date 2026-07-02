package fieldmind.research.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * ════════════════════════════════════════════════════════════════════════
 *  ✨ Cute Theme Config — Soft shadows, plush elevations, adorable defaults
 *
 *  Purpose: Centralise every elevation, shadow, gradient, and card preset
 *  so the entire app breathes a consistent, soft, layered, "cute" aesthetic.
 *  All values are deliberately gentle — nothing harsh or sharp.
 *
 *  🌈 Phase 5: Gradients & Card Surface Polish
 *  - [CuteGradients] — Theme-aware gradient presets for card backgrounds
 *  - [GradientCard] — Card composable with beautiful gradient backgrounds
 *  - [cuteGradientBackground] — Modifier extension for gradient surfaces
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
 * ════════════════════════════════════════════════════════════════════════
 *  🌈 Cute Gradients — Theme-aware gradient presets (Phase 5)
 *
 *  Generates beautiful, harmonious gradient pairs from the current
 *  Material 3 color scheme. Each gradient uses the scheme's container
 *  colors at various opacities to create soft, blended backgrounds.
 *
 *  Usage:
 *  ```
 *  Card(
 *      modifier = Modifier.cuteGradientBackground(CuteGradients.Style.PrimaryTonal)
 *  ) { ... }
 *  ```
 *
 *  Or use [GradientCard] for a drop-in replacement:
 *  ```
 *  GradientCard(style = CuteGradients.Style.SecondaryBlush) { ... }
 *  ```
 * ════════════════════════════════════════════════════════════════════════
 */
object CuteGradients {

    /**
     * Available gradient styles. Each produces a beautiful 2-3 color
     * gradient using the current Material theme's palette.
     *
     * (っ◕◕)っ  Phase 5: fully cute and colorful!
     */
    enum class Style(val displayName: String) {
        /** surfaceContainerLow → surfaceContainerHigh — subtle lift */
        SurfaceSubtle("Surface Subtle"),
        /** primaryContainer → tertiaryContainer — soft, tonal */
        PrimaryTonal("Primary Tonal"),
        /** secondaryContainer → primaryContainer → tertiaryContainer — warm blush trio */
        BlushTrio("Blush Trio"),
        /** primary container at two alpha levels — monochromatic depth */
        PrimaryMono("Primary Mono"),
        /** tertiaryContainer → secondaryContainer — cool dream */
        CoolDream("Cool Dream"),
        /** primaryContainer → secondaryContainer → tertiaryContainer — soft rainbow */
        RainbowSoft("Rainbow Soft"),
        /** secondaryContainer → tertiaryContainer — pastel spring */
        SpringPastel("Spring Pastel"),
        /** primaryContainer → surfaceContainerHigh with extra lift */
        SunnyLift("Sunny Lift"),
        /** inverseSurface → surface — moonlight glow for dark mode lovers */
        Moonlight("Moonlight"),
    }

    /** The user's selected gradient style — persisted in settings. */
    const val DEFAULT_STYLE = "Surface Subtle"

    /**
     * Returns a horizontal gradient [Brush] for the given [style] using
     * the current MaterialTheme color scheme.
     */
    @Composable
    fun brushFor(style: Style): Brush {
        val scheme = MaterialTheme.colorScheme
        val isDark = scheme.background.luminance() < 0.5f
        return when (style) {
            Style.SurfaceSubtle -> Brush.horizontalGradient(
                colors = listOf(
                    scheme.surfaceContainerLow,
                    scheme.surfaceContainerHigh
                )
            )
            Style.PrimaryTonal -> Brush.horizontalGradient(
                colors = listOf(
                    scheme.primaryContainer,
                    scheme.tertiaryContainer
                )
            )
            Style.BlushTrio -> Brush.horizontalGradient(
                colors = listOf(
                    scheme.secondaryContainer,
                    scheme.primaryContainer,
                    scheme.tertiaryContainer
                )
            )
            Style.PrimaryMono -> {
                val base = scheme.primaryContainer
                Brush.horizontalGradient(
                    colors = listOf(
                        base,
                        if (isDark) base.copy(alpha = 0.5f) else base.copy(alpha = 0.35f)
                    )
                )
            }
            Style.CoolDream -> Brush.horizontalGradient(
                colors = listOf(
                    scheme.tertiaryContainer,
                    scheme.secondaryContainer
                )
            )
            Style.RainbowSoft -> Brush.horizontalGradient(
                colors = listOf(
                    scheme.primaryContainer,
                    scheme.secondaryContainer,
                    scheme.tertiaryContainer
                )
            )
            Style.SpringPastel -> Brush.horizontalGradient(
                colors = listOf(
                    scheme.secondaryContainer,
                    scheme.tertiaryContainer
                )
            )
            Style.SunnyLift -> Brush.horizontalGradient(
                colors = listOf(
                    scheme.primaryContainer,
                    scheme.surfaceContainerHigh
                )
            )
            Style.Moonlight -> Brush.horizontalGradient(
                colors = listOf(
                    if (isDark) scheme.inverseSurface.copy(alpha = 0.3f) else scheme.surfaceContainerLow,
                    scheme.surfaceContainerHigh
                )
            )
        }
    }

    /**
     * Parse a style from its display name string.
     */
    fun fromDisplayName(name: String): Style =
        Style.entries.find { it.displayName == name } ?: Style.SurfaceSubtle

    /**
     * Parse a style from a settings-stored string (supports both name and displayName).
     */
    fun fromString(s: String): Style =
        Style.entries.find { it.name == s || it.displayName == s } ?: Style.SurfaceSubtle

    /**
     * All display names for the settings picker.
     */
    val allDisplayNames: List<String> = Style.entries.map { it.displayName }
}

/**
 * A beautifully gradient-backed card that wraps your content.
 *
 * Features:
 * - Theme-aware gradient background from [CuteGradients.Style]
 * - Standard plush elevation and rounded corners
 * - All content is laid out with consistent padding
 *
 * @param style the gradient style (defaults to SurfaceSubtle)
 * @param shape corner shape (defaults to CuteCardDefaults.Shape = 32dp)
 * @param elevation card elevation (defaults to plushTier2 = 4dp)
 * @param modifier additional modifier
 * @param content card content
 */
@Composable
fun GradientCard(
    style: CuteGradients.Style = CuteGradients.Style.SurfaceSubtle,
    shape: Shape = CuteCardDefaults.Shape,
    elevation: Dp = CuteElevations.plushTier2,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val gradient = CuteGradients.brushFor(style)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = gradient, shape = shape)
        ) {
            Column(modifier = Modifier.fillMaxWidth(), content = content)
        }
    }
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
