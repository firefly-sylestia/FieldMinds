package fieldmind.research.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
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
 *  🌈 Phase 7: Depth Hierarchy & Dark Mode Shadows
 *  - [CuteElevations] now includes `clickableTier` and `nonClickableTier`
 *    for clear clickable vs non-clickable visual distinction.
 *  - [CuteShadow] now carries `ambientColor` / `spotColor` which auto-adapt
 *    to dark mode (white-tinted glow in dark, cool black in light).
 *  - [cuteShadow] modifier uses theme-aware colors for ambient/spot.
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
 *   plushTier2 (4dp)  → standard card elevation (non-clickable: InfoCard, stat cards)
 *   plushTier3 (6dp)  → clickable cards (ClickableCard, EntityCard, SettingsGroupCard)
 *   plushTier4 (8dp)  → dialogs, bottom sheets, floating elements
 *   plushTier5 (12dp) → highest emphasis (modals, pickers)
 *
 *  Clickable vs non-clickable distinction:
 *    nonClickableTier (4dp) — for cards that display info but are not tappable
 *    clickableTier (6dp)    — for cards that respond to tap/press
 */
object CuteElevations {
    /** Subtle lift — background surfaces, low-focus info cards. */
    val plushTier1: Dp = 2.dp
    /** Standard card elevation — non-clickable info cards, stat displays. */
    val plushTier2: Dp = 4.dp
    /** Prominent cards — clickable cards, featured content, hero surfaces. */
    val plushTier3: Dp = 6.dp
    /** Dialogs, bottom sheets, floating elements. */
    val plushTier4: Dp = 8.dp
    /** Highest emphasis — modals, pickers, important overlays. */
    val plushTier5: Dp = 12.dp

    // ── Semantic depth tiers: clickable vs non-clickable ──
    /** Non-clickable cards — info display, stat tiles, section headers. Lower lift. */
    val nonClickableTier: Dp = plushTier2
    /** Clickable cards — respond to tap with expressive press feedback. Higher lift. */
    val clickableTier: Dp = plushTier3

    // ── Quick-access presets for CardDefaults ──
    val cardDefault
        @Composable
        get() = CardDefaults.cardElevation(defaultElevation = clickableTier)

    val cardNonClickable
        @Composable
        get() = CardDefaults.cardElevation(defaultElevation = nonClickableTier)

    val cardProminent
        @Composable
        get() = CardDefaults.cardElevation(defaultElevation = plushTier3)

    val cardDialog
        @Composable
        get() = CardDefaults.cardElevation(defaultElevation = plushTier4)
}

/**
 * Shadow style configuration for a single tier with theme-aware ambient/spot colors.
 *
 * In light mode:
 *   ambientColor = Color.Black at low alpha (standard drop shadow)
 *   spotColor    = Color.Black at medium alpha (directional light shadow)
 *
 * In dark mode:
 *   ambientColor = Color.White at low alpha (subtle white glow, lifted feel)
 *   spotColor    = Color.White at low-medium alpha (white-tinted directional shadow)
 *
 * @property elevation the shadow height in dp.
 * @property ambientColor the ambient side color (default auto-adapts to light/dark mode).
 * @property spotColor the spot/light source color (default auto-adapts to light/dark mode).
 */
data class CuteShadow(
    val elevation: Dp,
    val ambientColor: Color = Color.Black.copy(alpha = 0.12f),
    val spotColor: Color = Color.Black.copy(alpha = 0.18f)
) {
    companion object {
        /**
         * Create a CuteShadow with dark-mode-aware colors.
         * In dark mode, shadows use white-tinted colors for a soft glow effect.
         * In light mode, shadows use standard black with appropriate alphas.
         */
        @Composable
        fun themeAware(elevation: Dp, isDark: Boolean = isSystemInDarkTheme()): CuteShadow {
            val ambientAlpha = 0.12f + (elevation.value / 12f) * 0.08f
            val spotAlpha = 0.18f + (elevation.value / 12f) * 0.14f
            return if (isDark) {
                // Dark mode: brighter white-tinted glow shadows so depth is clearly visible
                // against dark/AMOLED backgrounds. Significantly bumped from v0.31.0 levels
                // where shadows were too subtle (invisible on dark surfaces).
                // At 6dp: ambient ~0.16, spot ~0.25 — clearly visible luminous lift.
                CuteShadow(
                    elevation = elevation,
                    ambientColor = Color.White.copy(alpha = ambientAlpha.coerceIn(0.08f, 0.25f)),
                    spotColor = Color.White.copy(alpha = spotAlpha.coerceIn(0.12f, 0.35f))
                )
            } else {
                CuteShadow(
                    elevation = elevation,
                    ambientColor = Color.Black.copy(alpha = ambientAlpha.coerceIn(0.08f, 0.18f)),
                    spotColor = Color.Black.copy(alpha = spotAlpha.coerceIn(0.12f, 0.28f))
                )
            }
        }
    }
}

/**
 * Reusable shadow presets that map 1:1 to [CuteElevations] tiers.
 * Each preset auto-adapts ambient/spot colors to dark/light mode.
 */
object CuteShadows {
    /** Subtle — for tier-1 elements. */
    @Composable
    fun subtle(): CuteShadow = CuteShadow.themeAware(CuteElevations.plushTier1)
    /** Standard non-clickable — for tier-2 info cards. */
    @Composable
    fun standard(): CuteShadow = CuteShadow.themeAware(CuteElevations.nonClickableTier)
    /** Clickable — for tier-3 interactive cards. */
    @Composable
    fun clickable(): CuteShadow = CuteShadow.themeAware(CuteElevations.clickableTier)
    /** Float — for tier-4 floating elements. */
    @Composable
    fun float(): CuteShadow = CuteShadow.themeAware(CuteElevations.plushTier4)
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
 *
 * NEW: elevationPreset lets you pick clickable vs non-clickable elevation:
 * ```
 * Card(
 *     elevation = CuteCardDefaults.elevation(isClickable = true),
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

    /**
     * Semantic elevation preset — pick clickable vs non-clickable.
     * @param isClickable true for interactive cards (higher lift), false for info-only cards.
     */
    @Composable
    fun elevation(isClickable: Boolean = true): CardElevation =
        if (isClickable) CuteElevations.cardDefault
        else CuteElevations.cardNonClickable
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
        /** surface → primaryContainer hint → surface — scheme-aware vertical background wash */
        ScreenBackground("Screen Background"),
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
        /** true black → deep gray — extreme power saving for AMOLED screens in dark mode */
        AmoledBlack("AMOLED Black"),
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
        val bg = scheme.background
        val isDark = (0.299f * bg.red + 0.587f * bg.green + 0.114f * bg.blue) < 0.5f
        return when (style) {
            Style.SurfaceSubtle -> Brush.horizontalGradient(
                colors = listOf(
                    scheme.surfaceContainerLow,
                    scheme.surfaceContainerHigh
                )
            )
            Style.ScreenBackground -> Brush.verticalGradient(
                colors = listOf(
                    scheme.surface,
                    scheme.primaryContainer.copy(alpha = 0.08f),
                    scheme.tertiaryContainer.copy(alpha = 0.04f),
                    scheme.surface
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
            Style.AmoledBlack -> {
                if (isDark) {
                    // True black gradient for AMOLED — barely lifts pixels to save battery
                    // Only uses the deepest blacks for maximum power saving on OLED panels
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Black,
                            Color(0xFF050505),
                            Color(0xFF080808)
                        )
                    )
                } else {
                    // Fall back to a subtle neutral gradient in light mode
                    // (AMOLED mode typically only matters in dark mode, but keep it graceful)
                    Brush.horizontalGradient(
                        colors = listOf(
                            scheme.surfaceContainerLow,
                            scheme.surfaceContainer
                        )
                    )
                }
            }
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
 * A [Modifier] extension that applies a soft custom shadow with theme-aware colors.
 *
 * This gives a plush, layered look: the elevation shadow lifts the card
 * off the background for a clear visual separation.
 *
 * In dark mode, shadows use white-tinted ambient/spot colors for a luminous lift.
 * In light mode, standard warm-black shadows with cooler undertones.
 *
 * @param elevation the dp height of the shadow.
 * @param shape the shape to clip the shadow to (defaults to pill shape).
 * @param isClickable if true, uses clickable-tier elevation; otherwise uses non-clickable.
 */
@Composable
fun Modifier.cuteShadow(
    elevation: Dp = CuteElevations.clickableTier,
    shape: Shape = CuteCardDefaults.Shape
): Modifier = cuteShadowAdaptive(elevation, shape)

/**
 * Theme-aware shadow that adapts ambient/spot colors to dark/light mode.
 * In dark mode, white-tinted shadows give a luminous glow effect.
 * In light mode, standard cool-black shadows.
 *
 * Usage:
 * ```
 * Modifier.cuteShadowAdaptive(elevation = CuteElevations.clickableTier)
 * ```
 */
@Composable
fun Modifier.cuteShadowAdaptive(
    elevation: Dp = CuteElevations.clickableTier,
    shape: Shape = CuteCardDefaults.Shape,
    isDark: Boolean = isSystemInDarkTheme()
): Modifier {
    val shadowStyle = CuteShadow.themeAware(elevation, isDark)
    return this.then(
        shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = shadowStyle.ambientColor,
            spotColor = shadowStyle.spotColor,
            clip = false
        )
    )
}
