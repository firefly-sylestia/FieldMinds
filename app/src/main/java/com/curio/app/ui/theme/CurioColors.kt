package com.curio.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * Curio's pastel color palette — soft, warm, inviting.
 *
 * White + soft pink coral foundation with buttery yellow, mint, lavender,
 * and peachy category accents. All colors are opaque; card surfaces use
 * solid category gradients with shadow elevation for depth.
 */
object CurioColors {

    // ── Warm pastel foundation ─────────────────────────────────────────
    val CoralBlush       = Color(0xFFFF8FA3)  // Soft pink — primary
    val ButterYellow     = Color(0xFFFFD97D)  // Warm butter — secondary
    val SkyMint          = Color(0xFF8FE3CF)  // Soft mint — tertiary
    val CreamWhite       = Color(0xFFFFFBF5)  // Warm white — surface
    val SoftSand         = Color(0xFFF6EFE4)  // Warm sand — surface container
    val WarmCoralRed     = Color(0xFFE4626F)  // Soft coral-red — error
    val DeepPlum         = Color(0xFF3B0A17)  // Deep maroon — on-primary

    // ── Category pastel accents ────────────────────────────────────────
    val Lilac            = Color(0xFFC9A6F2)  // Music / Artists — soft purple
    val DustyBlue        = Color(0xFF9BB8E8)  // Movies / Directors — soft blue
    val Sage             = Color(0xFFA8C99A)  // Books / Authors — soft green
    val Peach            = Color(0xFFFFB585)  // Visual Art / Painters — soft orange
    val Teal             = Color(0xFF6FC7BE)  // Science & Nature — soft teal

    /** Tinted (20% alpha) versions of category accents for backgrounds. */
    val LilacTint     = Lilac.copy(alpha = 0.20f)
    val DustyBlueTint = DustyBlue.copy(alpha = 0.20f)
    val SageTint      = Sage.copy(alpha = 0.20f)
    val PeachTint     = Peach.copy(alpha = 0.20f)
    val TealTint      = Teal.copy(alpha = 0.20f)
}

/**
 * Solid gradient definitions for card surfaces.
 * Wildcard spans the full pastel rainbow; named categories use a
 * deep-maroon → accent → warm-white progression.
 */
object CurioGradients {
    /** Warm sunset spectrum for the Wildcard — cohesive with the brand palette. */
    val WildcardGradientStops = listOf(
        CurioColors.CoralBlush,
        CurioColors.Peach,
        CurioColors.ButterYellow
    )

    /**
     * Solid category card gradient: accent → accent → warm white.
     * No black — pure pastel warmth from top to bottom.
     */
    fun cardGradient(accent: Color): List<Color> = listOf(
        accent,
        lerp(accent, CurioColors.CreamWhite, 0.42f)
    )

    /**
     * Solid wildcard card gradient — pure pastel warmth, no black or plum.
     */
    fun wildcardCardGradient(): List<Color> = listOf(
        CurioColors.CoralBlush,
        lerp(CurioColors.CoralBlush, CurioColors.Peach, 0.55f),
        CurioColors.Peach,
        CurioColors.ButterYellow,
        lerp(CurioColors.ButterYellow, CurioColors.CreamWhite, 0.45f)
    )
}
