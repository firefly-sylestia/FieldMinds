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
import fieldmind.research.app.features.field.presentation.theme.FieldMindTheme

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
 *  🌟 Premium Themes: Midnight Flora, Noir Amethyst, Warm Terrain
 *  Full M3 palettes in [Color.kt]. Selectable from Settings → Appearance → Color scheme.
 *  Each has hand-tuned entity colors in [FieldMindTheme.kt].
 *  Default scheme uses the original FieldMind brand (forest green + warm ochre).
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
    val plushTier2: Dp = 6.dp
    /** Prominent cards — clickable cards, featured content, hero surfaces. */
    val plushTier3: Dp = 8.dp
    /** Dialogs, bottom sheets, floating elements. */
    val plushTier4: Dp = 12.dp
    /** Highest emphasis — modals, pickers, important overlays. */
    val plushTier5: Dp = 16.dp

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
        fun themeAware(elevation: Dp, isDark: Boolean = FieldMindTheme.colors.isDark): CuteShadow {
            val ambientAlpha = 0.16f + (elevation.value / 12f) * 0.12f
            val spotAlpha = 0.24f + (elevation.value / 12f) * 0.18f
            return if (isDark) {
                // Premium dark mode: warm-tinted glow shadows so depth is clearly visible
                // against dark/AMOLED backgrounds. Uses warm white (candlelight) instead
                // of pure white for a luxurious luminous lift.
                // At 6dp: ambient ~0.22, spot ~0.33 — warm glow with real presence.
                // At 8dp: ambient ~0.24, spot ~0.36 — premium depth for clickable cards.
                // Neutral-warm glow: works across green (Flora), purple (Amethyst),
                // and brown (Terrain) themes without color clash.
                val warmGlow = Color(0xFFF8F4E8)
                CuteShadow(
                    elevation = elevation,
                    ambientColor = warmGlow.copy(alpha = ambientAlpha.coerceIn(0.14f, 0.36f)),
                    spotColor = warmGlow.copy(alpha = spotAlpha.coerceIn(0.20f, 0.50f))
                )
            } else {
                CuteShadow(
                    elevation = elevation,
                    ambientColor = Color.Black.copy(alpha = ambientAlpha.coerceIn(0.10f, 0.26f)),
                    spotColor = Color.Black.copy(alpha = spotAlpha.coerceIn(0.16f, 0.38f))
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
 *  🌈 Cute Card Tints — Theme-aware flat color tints for card backgrounds
 *
 *  Provides subtle single-color background tints instead of multi-stop
 *  gradients. Each style uses one scheme container color at a very low
 *  opacity for a clean, minimal card background that doesn't fight the
 *  card content.
 *
 *  Usage:
 *  ```
 *  Box(modifier = Modifier.background(brush = CuteGradients.brushFor(style)))
 *  ```
 *
 *  Or use [GradientCard] for a drop-in replacement:
 *  ```
 *  GradientCard(style = CuteGradients.Style.BlushTrio) { ... }
 *  ```
 * ════════════════════════════════════════════════════════════════════════
 */
object CuteGradients {

    /**
     * Available tint styles. Each uses a single scheme container color at
     * a very low opacity for a clean, minimal card background.
     */
    enum class Style(val displayName: String) {
        /** primaryContainer flat tint — the default subtle card wash */
        ScreenBackground("Screen Background"),
        /** secondaryContainer flat tint */
        BlushTrio("Blush Trio"),
        /** primaryContainer flat tint */
        RainbowSoft("Rainbow Soft"),
        /** tertiaryContainer flat tint */
        CoolDream("Cool Dream"),
        /** secondaryContainer flat tint */
        SpringPastel("Spring Pastel"),
        /** primaryContainer flat tint (slightly stronger) */
        SunnyLift("Sunny Lift"),
        /** true black / dark gray — AMOLED power saving */
        AmoledBlack("AMOLED Black"),
        // ── Premium theme-specific tints (responsive to active scheme) ──
        /** scheme primary flat tint — matches Midnight Flora palette */
        FloraGlow("Flora Glow"),
        /** scheme primary flat tint — matches Noir Amethyst palette */
        AmethystAura("Amethyst Aura"),
        /** scheme primary flat tint — matches Warm Terrain palette */
        TerrainWarmth("Terrain Warmth"),
    }

    /** The user's selected tint style — persisted in settings. */
    const val DEFAULT_STYLE = "Screen Background"

    /**
     * Returns a flat tint [Brush] for the given [style].
     * Each style is a single scheme container color at a very low opacity,
     * producing a clean, minimal card background. The [opacity] parameter
     * scales the tint strength uniformly across all styles.
     */
    @Composable
    fun brushFor(style: Style, opacity: Float = 0.55f): Brush {
        val scheme = MaterialTheme.colorScheme
        val isDark = (0.299f * scheme.background.red + 0.587f * scheme.background.green + 0.114f * scheme.background.blue) < 0.5f
        // Opacity multiplier: baseAlpha controls each style's inherent strength,
        // multiplied by the user's opacity preference for uniform scaling.
        fun alpha(base: Float): Float = (base * opacity).coerceIn(0f, 1f)

        return when (style) {
            // ── Scheme-aware flat tints — each uses a single container color ──
            Style.ScreenBackground -> Brush.verticalGradient(
                colors = listOf(
                    scheme.primaryContainer.copy(alpha = alpha(0.07f)),
                    scheme.primaryContainer.copy(alpha = alpha(0.07f))
                )
            )
            Style.BlushTrio -> Brush.verticalGradient(
                colors = listOf(
                    scheme.secondaryContainer.copy(alpha = alpha(0.09f)),
                    scheme.secondaryContainer.copy(alpha = alpha(0.09f))
                )
            )
            Style.CoolDream -> Brush.verticalGradient(
                colors = listOf(
                    scheme.tertiaryContainer.copy(alpha = alpha(0.09f)),
                    scheme.tertiaryContainer.copy(alpha = alpha(0.09f))
                )
            )
            Style.RainbowSoft -> Brush.verticalGradient(
                colors = listOf(
                    scheme.primaryContainer.copy(alpha = alpha(0.09f)),
                    scheme.primaryContainer.copy(alpha = alpha(0.09f))
                )
            )
            Style.SpringPastel -> Brush.verticalGradient(
                colors = listOf(
                    scheme.secondaryContainer.copy(alpha = alpha(0.09f)),
                    scheme.secondaryContainer.copy(alpha = alpha(0.09f))
                )
            )
            Style.SunnyLift -> Brush.verticalGradient(
                colors = listOf(
                    scheme.primaryContainer.copy(alpha = alpha(0.12f)),
                    scheme.primaryContainer.copy(alpha = alpha(0.12f))
                )
            )
            Style.AmoledBlack -> {
                if (isDark) {
                    Brush.verticalGradient(
                        colors = listOf(Color.Black, Color(0xFF050505))
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(scheme.surfaceContainerLow, scheme.surfaceContainer)
                    )
                }
            }

            // ── Premium theme-specific tints — scheme primary gives each theme's character ──
            Style.FloraGlow -> Brush.verticalGradient(
                colors = listOf(
                    scheme.primary.copy(alpha = alpha(0.07f)),
                    scheme.primary.copy(alpha = alpha(0.07f))
                )
            )
            Style.AmethystAura -> Brush.verticalGradient(
                colors = listOf(
                    scheme.primary.copy(alpha = alpha(0.07f)),
                    scheme.primary.copy(alpha = alpha(0.07f))
                )
            )
            Style.TerrainWarmth -> Brush.verticalGradient(
                colors = listOf(
                    scheme.primary.copy(alpha = alpha(0.07f)),
                    scheme.primary.copy(alpha = alpha(0.07f))
                )
            )
        }
    }

    /**
     * Parse a style from its display name string.
     */
    fun fromDisplayName(name: String): Style =
        Style.entries.find { it.displayName == name } ?: Style.ScreenBackground

    /**
     * Parse a style from a settings-stored string (supports both name and displayName).
     */
    fun fromString(s: String): Style =
        Style.entries.find { it.name == s || it.displayName == s } ?: Style.ScreenBackground

    /**
     * All display names for the settings picker.
     */
    val allDisplayNames: List<String> = Style.entries.map { it.displayName }
}

/**
 * A clean, tint-backed card that wraps your content.
 *
 * Features:
 * - Theme-aware flat color tint background from [CuteGradients.Style]
 * - Standard plush elevation and rounded corners
 * - All content is laid out with consistent padding
 *
 * @param style the tint style (defaults to ScreenBackground)
 * @param shape corner shape (defaults to CuteCardDefaults.Shape = 32dp)
 * @param elevation card elevation (defaults to plushTier2 = 4dp)
 * @param modifier additional modifier
 * @param content card content
 */
@Composable
fun GradientCard(
    style: CuteGradients.Style = CuteGradients.Style.ScreenBackground,
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
    isDark: Boolean = FieldMindTheme.colors.isDark
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

/**
 * A centralized screen background modifier that applies the ScreenBackground flat tint
 * at the user's preferred opacity. Use this on the outermost Box/Column of every screen
 * instead of manually copying the boilerplate everywhere.
 *
 * Usage:
 * ```kotlin
 * Box(Modifier.fillMaxSize().screenBackground(gradientOpacity)) {
 *     // screen content
 * }
 * ```
 */
@Composable
fun Modifier.screenBackground(gradientOpacity: Float = 0.75f): Modifier {
    val brush = CuteGradients.brushFor(CuteGradients.Style.ScreenBackground, opacity = gradientOpacity)
    return this.background(brush = brush)
}
