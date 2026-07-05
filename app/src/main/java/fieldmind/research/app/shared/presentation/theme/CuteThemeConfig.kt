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
        fun themeAware(elevation: Dp, isDark: Boolean = FieldMindTheme.colors.isDark): CuteShadow {
            val ambientAlpha = 0.12f + (elevation.value / 12f) * 0.10f
            val spotAlpha = 0.18f + (elevation.value / 12f) * 0.16f
            return if (isDark) {
                // Premium dark mode: warm-tinted glow shadows so depth is clearly visible
                // against dark/AMOLED backgrounds. Uses warm white (candlelight) instead
                // of pure white for a luxurious luminous lift.
                // At 6dp: ambient ~0.17, spot ~0.26 — clearly visible warm glow.
                // Neutral-warm glow: works across green (Flora), purple (Amethyst),
                // and brown (Terrain) themes without color clash.
                val warmGlow = Color(0xFFF8F4E8)
                CuteShadow(
                    elevation = elevation,
                    ambientColor = warmGlow.copy(alpha = ambientAlpha.coerceIn(0.10f, 0.30f)),
                    spotColor = warmGlow.copy(alpha = spotAlpha.coerceIn(0.15f, 0.42f))
                )
            } else {
                CuteShadow(
                    elevation = elevation,
                    ambientColor = Color.Black.copy(alpha = ambientAlpha.coerceIn(0.08f, 0.20f)),
                    spotColor = Color.Black.copy(alpha = spotAlpha.coerceIn(0.12f, 0.30f))
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
 *      modifier = Modifier.cuteGradientBackground(CuteGradients.Style.CyberpunkSunset)
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
     * Available gradient styles. Combines scheme-aware tonal gradients
     * with curated artistic fixed-color gradients for bold visual impact.
     */
    enum class Style(val displayName: String) {
        // ── Scheme-aware (theme-responsive) ──
        /** surface → primaryContainer hint → surface — vertical background wash */
        ScreenBackground("Screen Background"),
        /** secondaryContainer → primaryContainer → tertiaryContainer */
        BlushTrio("Blush Trio"),
        /** primaryContainer → secondaryContainer → tertiaryContainer */
        RainbowSoft("Rainbow Soft"),
        /** tertiaryContainer → secondaryContainer */
        CoolDream("Cool Dream"),
        /** secondaryContainer → tertiaryContainer */
        SpringPastel("Spring Pastel"),
        /** primaryContainer → surfaceContainerHigh */
        SunnyLift("Sunny Lift"),
        /** true black → deep gray — AMOLED power saving */
        AmoledBlack("AMOLED Black"),

        // ── Artistic fixed-color gradients (abstract, bold) ──
        /** #0D0221 → #3A015C → #7B2D8E → #E879F9 — deep space nebula */
        NebulaPurple("Nebula Purple"),
        /** #1A1A2E → #16213E → #0F3460 → #E94560 — neon cyberpunk */
        CyberpunkSunset("Cyberpunk Sunset"),
        /** #0F2027 → #203A43 → #2C5364 — deep ocean twilight */
        OceanDepths("Ocean Depths"),
        /** #FF6B6B → #4ECDC4 → #292F36 — bold triadic pop */
        TropicalLagoon("Tropical Lagoon"),
        /** #2C3E50 → #3498DB → #ECF0F1 — arctic aurora */
        ArcticAurora("Arctic Aurora"),
        /** #FDEB71 → #F8D800 → #FF8A5C — warm golden hour */
        GoldenHour("Golden Hour"),
        /** #EA8D8D → #A890FE → #D8B4FE — cherry blossom dream */
        SakuraDream("Sakura Dream"),
        /** #4158D0 → #C850C0 → #FFCC70 — vibrant gradient mesh */
        SunsetVibes("Sunset Vibes"),
    }

    /** The user's selected gradient style — persisted in settings. */
    const val DEFAULT_STYLE = "Screen Background"

    /**
     * Returns a gradient [Brush] for the given [style].
     * Scheme-aware styles adapt to the current MaterialTheme color scheme.
     * Artistic styles use fixed curated color combinations for bold impact.
     */
    @Composable
    fun brushFor(style: Style, opacity: Float = 1f): Brush {
        val scheme = MaterialTheme.colorScheme
        val isDark = (0.299f * scheme.background.red + 0.587f * scheme.background.green + 0.114f * scheme.background.blue) < 0.5f
        // Apply opacity multiplier to a color, keeping the base alpha sensible
        fun Color.withOpacity(baseAlpha: Float): Color = this.copy(alpha = (alpha * baseAlpha * opacity).coerceIn(0f, 1f))
        return when (style) {
            // ── Scheme-aware (theme-responsive) — reduced base alpha for consistency ──
            Style.ScreenBackground -> Brush.verticalGradient(
                colors = listOf(
                    scheme.surface,
                    scheme.primaryContainer.copy(alpha = (0.08f * opacity).coerceIn(0f, 0.25f)),
                    scheme.tertiaryContainer.copy(alpha = (0.04f * opacity).coerceIn(0f, 0.15f)),
                    scheme.surface
                )
            )
            Style.BlushTrio -> Brush.horizontalGradient(
                colors = listOf(
                    scheme.secondaryContainer.copy(alpha = (0.50f * opacity).coerceIn(0f, 1f)),
                    scheme.primaryContainer.copy(alpha = (0.45f * opacity).coerceIn(0f, 1f)),
                    scheme.tertiaryContainer.copy(alpha = (0.40f * opacity).coerceIn(0f, 1f))
                )
            )
            Style.CoolDream -> Brush.horizontalGradient(
                colors = listOf(
                    scheme.tertiaryContainer.copy(alpha = (0.45f * opacity).coerceIn(0f, 1f)),
                    scheme.secondaryContainer.copy(alpha = (0.40f * opacity).coerceIn(0f, 1f))
                )
            )
            Style.RainbowSoft -> Brush.horizontalGradient(
                colors = listOf(
                    scheme.primaryContainer.copy(alpha = (0.40f * opacity).coerceIn(0f, 1f)),
                    scheme.secondaryContainer.copy(alpha = (0.35f * opacity).coerceIn(0f, 1f)),
                    scheme.tertiaryContainer.copy(alpha = (0.30f * opacity).coerceIn(0f, 1f))
                )
            )
            Style.SpringPastel -> Brush.horizontalGradient(
                colors = listOf(
                    scheme.secondaryContainer.copy(alpha = (0.50f * opacity).coerceIn(0f, 1f)),
                    scheme.tertiaryContainer.copy(alpha = (0.45f * opacity).coerceIn(0f, 1f))
                )
            )
            Style.SunnyLift -> Brush.horizontalGradient(
                colors = listOf(
                    scheme.primaryContainer.copy(alpha = (0.40f * opacity).coerceIn(0f, 1f)),
                    scheme.surfaceContainerHigh
                )
            )
            Style.AmoledBlack -> {
                if (isDark) {
                    Brush.horizontalGradient(
                        colors = listOf(Color.Black, Color(0xFF050505), Color(0xFF080808))
                    )
                } else {
                    Brush.horizontalGradient(
                        colors = listOf(scheme.surfaceContainerLow, scheme.surfaceContainer)
                    )
                }
            }

            // ── Artistic fixed-color gradients (abstract, bold) with opacity ──
            Style.NebulaPurple -> if (isDark) {
                Brush.horizontalGradient(colors = listOf(Color(0xFF0A0015).withOpacity(1f), Color(0xFF1A0040).withOpacity(1f), Color(0xFF3A1060).withOpacity(1f), Color(0xFF6A2A8A).withOpacity(1f)))
            } else {
                Brush.horizontalGradient(colors = listOf(Color(0xFF1A0030).withOpacity(1f), Color(0xFF3A1060).withOpacity(1f), Color(0xFF7B2D8E).withOpacity(1f), Color(0xFFE879F9).withOpacity(1f)))
            }
            Style.CyberpunkSunset -> if (isDark) {
                Brush.horizontalGradient(colors = listOf(Color(0xFF0D0D1A).withOpacity(1f), Color(0xFF1A1040).withOpacity(1f), Color(0xFF3A2060).withOpacity(1f), Color(0xFFC94050).withOpacity(1f)))
            } else {
                Brush.horizontalGradient(colors = listOf(Color(0xFF1A1A2E).withOpacity(1f), Color(0xFF16213E).withOpacity(1f), Color(0xFF0F3460).withOpacity(1f), Color(0xFFE94560).withOpacity(1f)))
            }
            Style.OceanDepths -> if (isDark) {
                Brush.horizontalGradient(colors = listOf(Color(0xFF051015).withOpacity(1f), Color(0xFF0A1A25).withOpacity(1f), Color(0xFF102A35).withOpacity(1f)))
            } else {
                Brush.horizontalGradient(colors = listOf(Color(0xFF0F2027).withOpacity(1f), Color(0xFF203A43).withOpacity(1f), Color(0xFF2C5364).withOpacity(1f)))
            }
            Style.TropicalLagoon -> if (isDark) {
                Brush.horizontalGradient(colors = listOf(Color(0xFF1A2020).withOpacity(1f), Color(0xFFC95050).withOpacity(1f), Color(0xFF3AB0A0).withOpacity(1f)))
            } else {
                Brush.horizontalGradient(colors = listOf(Color(0xFFFF6B6B).withOpacity(1f), Color(0xFF4ECDC4).withOpacity(1f), Color(0xFF292F36).withOpacity(1f)))
            }
            Style.ArcticAurora -> if (isDark) {
                Brush.horizontalGradient(colors = listOf(Color(0xFF0A1520).withOpacity(1f), Color(0xFF1A4A70).withOpacity(1f), Color(0xFF2A5A8A).withOpacity(1f)))
            } else {
                Brush.horizontalGradient(colors = listOf(Color(0xFF2C3E50).withOpacity(1f), Color(0xFF3498DB).withOpacity(1f), Color(0xFFD4E6F1).withOpacity(1f)))
            }
            Style.GoldenHour -> if (isDark) {
                Brush.horizontalGradient(colors = listOf(Color(0xFF2A1A00).withOpacity(1f), Color(0xFF5A3A00).withOpacity(1f), Color(0xFF8A5A00).withOpacity(1f), Color(0xFFCC7A30).withOpacity(1f)))
            } else {
                Brush.horizontalGradient(colors = listOf(Color(0xFFFDEB71).withOpacity(1f), Color(0xFFF8D800).withOpacity(1f), Color(0xFFFF8A5C).withOpacity(1f)))
            }
            Style.SakuraDream -> if (isDark) {
                Brush.horizontalGradient(colors = listOf(Color(0xFF1A0A20).withOpacity(1f), Color(0xFF3A1A40).withOpacity(1f), Color(0xFF5A2A60).withOpacity(1f), Color(0xFF8A4A7A).withOpacity(1f)))
            } else {
                Brush.horizontalGradient(colors = listOf(Color(0xFFEA8D8D).withOpacity(1f), Color(0xFFA890FE).withOpacity(1f), Color(0xFFD8B4FE).withOpacity(1f)))
            }
            Style.SunsetVibes -> if (isDark) {
                Brush.horizontalGradient(colors = listOf(Color(0xFF0A0A2A).withOpacity(1f), Color(0xFF2A1060).withOpacity(1f), Color(0xFF6A2060).withOpacity(1f), Color(0xFFAA6030).withOpacity(1f)))
            } else {
                Brush.horizontalGradient(colors = listOf(Color(0xFF4158D0).withOpacity(1f), Color(0xFFC850C0).withOpacity(1f), Color(0xFFFFCC70).withOpacity(1f)))
            }
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
 * A beautifully gradient-backed card that wraps your content.
 *
 * Features:
 * - Theme-aware gradient background from [CuteGradients.Style]
 * - Standard plush elevation and rounded corners
 * - All content is laid out with consistent padding
 *
 * @param style the gradient style (defaults to ScreenBackground)
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
