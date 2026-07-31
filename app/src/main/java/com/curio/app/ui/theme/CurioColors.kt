package com.curio.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * Curio's Midnight Signal color system — see CURIO_SPEC.md §0.2.
 *
 * The identity is built around a deep navigation-night foundation, an electric
 * blue signal, a warm orange energy accent, and a mint aperture highlight. The
 * category tokens remain named for source compatibility, but their values now
 * belong to the new brand rather than the retired pastel palette.
 */
object CurioColors {

    // ── Midnight Signal foundation ─────────────────────────────────────────
    val CoralBlush       = Color(0xFF1264C5)  // Primary signal blue
    val ButterYellow     = Color(0xFFE6652F)  // Secondary signal orange
    val SkyMint          = Color(0xFF009E83)  // Tertiary aperture mint
    val CreamWhite       = Color(0xFFF8FBFF)  // Light surface
    val SoftSand         = Color(0xFFE7EEF6)  // Light surface container
    val WarmCoralRed     = Color(0xFFBA3A4B)  // Error
    val DeepPlum         = Color(0xFF081B33)  // Midnight ink / on-primary

    // ── Category signal colors ─────────────────────────────────────────────
    val Lilac            = Color(0xFF3D8CFF)  // Music / Artists — electric blue
    val DustyBlue        = Color(0xFF5B5FEF)  // Movies / Directors — cobalt
    val Sage             = Color(0xFF16B89A)  // Books / Authors — mint
    val Peach            = Color(0xFFE6652F)  // Visual Art / Painters — orange
    val Teal             = Color(0xFF079DB8)  // Science & Nature — cyan
    // Wildcard uses a signal-spectrum gradient (see CurioGradients).

    /** Restrained signal washes for category backgrounds. */
    val LilacTint     = Lilac.copy(alpha = 0.16f)
    val DustyBlueTint = DustyBlue.copy(alpha = 0.16f)
    val SageTint      = Sage.copy(alpha = 0.16f)
    val PeachTint     = Peach.copy(alpha = 0.16f)
    val TealTint      = Teal.copy(alpha = 0.16f)
}

/**
 * Signal-spectrum gradients used for wildcard and hero depth.
 * Gradients are decorative; core cards still use opaque theme surfaces.
 */
object CurioGradients {
    val WildcardGradientStops = listOf(
        CurioColors.Lilac,
        CurioColors.Teal,
        CurioColors.Sage,
        CurioColors.ButterYellow,
        CurioColors.DustyBlue
    )

    /** Hue-preserving signal stops for named-category hero treatments. */
    fun ticketStops(accent: Color, isDark: Boolean): List<Color> = listOf(
        lerp(accent, Color.Black, if (isDark) 0.04f else 0.10f),
        lerp(accent, Color.Black, if (isDark) 0.16f else 0.24f),
        lerp(accent, Color.Black, if (isDark) 0.32f else 0.42f),
        lerp(accent, Color.Black, if (isDark) 0.50f else 0.60f)
    )

    /** Signal-spectrum stops deepened enough for white text and small UI. */
    fun wildcardTicketStops(isDark: Boolean): List<Color> =
        WildcardGradientStops.map {
            lerp(it, Color.Black, if (isDark) 0.16f else 0.34f)
        }
}
