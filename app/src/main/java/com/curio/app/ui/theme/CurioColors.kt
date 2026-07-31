package com.curio.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * Curio's Midnight Signal color system — see CURIO_SPEC.md §0.2.
 *
 * The identity is built around a deep navigation-night foundation, an electric
 * blue signal, a warm orange energy accent, and a mint aperture highlight.
 *
 * All colors are 100 % opaque. Card surfaces use solid category gradients
 * with shadow elevation for depth — no alpha tricks, no borders, no glow.
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

    // ── Opaque lighter variants (lerp toward white, no alpha) ─────────────
    val LilacTint     = lerp(Lilac,     Color.White, 0.45f)
    val DustyBlueTint = lerp(DustyBlue, Color.White, 0.45f)
    val SageTint      = lerp(Sage,      Color.White, 0.45f)
    val PeachTint     = lerp(Peach,     Color.White, 0.45f)
    val TealTint      = lerp(Teal,      Color.White, 0.45f)
}

/**
 * Solid, fully-opaque gradient definitions used across all card surfaces.
 * No alpha tricks — every stop is a real, opaque color.  Shadow elevation
 * provides depth instead of borders or transparency.
 */
object CurioGradients {
    /** Wildcard rainbow spectrum — spans the full signal palette. */
    val WildcardGradientStops = listOf(
        CurioColors.Lilac,
        CurioColors.Teal,
        CurioColors.Sage,
        CurioColors.ButterYellow,
        CurioColors.DustyBlue
    )

    /**
     * Solid category card gradient — deep → accent → light.
     * Every stop is 100 % opaque so the card always reads as a solid
     * block of color regardless of theme background.
     */
    fun cardGradient(accent: Color): List<Color> = listOf(
        lerp(accent, CurioColors.DeepPlum, 0.42f),
        accent,
        lerp(accent, Color.White, 0.42f)
    )

    /**
     * Solid wildcard card gradient — the rainbow spectrum, deepened
     * at the ends so white text stays readable.
     */
    fun wildcardCardGradient(): List<Color> = listOf(
        lerp(CurioColors.Lilac,     CurioColors.DeepPlum, 0.35f),
        CurioColors.Lilac,
        CurioColors.Teal,
        CurioColors.ButterYellow,
        CurioColors.DustyBlue,
        lerp(CurioColors.DustyBlue, Color.White, 0.30f)
    )
}
