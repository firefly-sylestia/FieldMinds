package com.curio.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Curio's color palette — see CURIO_SPEC.md §0.2.
 *
 * Base on M3's tonal palette generator, but seed from a warm coral instead of
 * a cold blue so default M3 doesn't feel corporate.
 *
 * Each of the 6 categories also has its OWN accent color layered on top of the
 * base palette (used for chips, category headers, spin-segment fills).
 */
object CurioColors {

    // ── Base palette (warm coral seed) ─────────────────────────────────────
    val CoralBlush       = Color(0xFFFF8FA3)  // Primary, tone 60
    val ButterYellow     = Color(0xFFFFD97D)  // Secondary
    val SkyMint          = Color(0xFF8FE3CF)  // Tertiary
    val CreamWhite       = Color(0xFFFFFBF5)  // Surface
    val SoftSand         = Color(0xFFF6EFE4)  // Surface Container
    val WarmCoralRed     = Color(0xFFE4626F)  // Error
    val DeepPlum         = Color(0xFF3B0A17)  // On-Primary

    // ── Category accent colors ─────────────────────────────────────────────
    val Lilac            = Color(0xFFC9A6F2)  // Music / Artists
    val DustyBlue        = Color(0xFF9BB8E8)  // Movies / Directors
    val Sage             = Color(0xFFA8C99A)  // Books / Authors
    val Peach            = Color(0xFFFFB585)  // Visual Art / Painters
    val Teal             = Color(0xFF6FC7BE)  // Science & Nature
    // Wildcard uses a rainbow gradient (see CurioGradients.WildcardGradient)

    /** Tinted (20% alpha) versions of category accents for backgrounds. */
    val LilacTint    = Lilac.copy(alpha = 0.20f)
    val DustyBlueTint = DustyBlue.copy(alpha = 0.20f)
    val SageTint     = Sage.copy(alpha = 0.20f)
    val PeachTint    = Peach.copy(alpha = 0.20f)
    val TealTint     = Teal.copy(alpha = 0.20f)
}

/**
 * Rainbow gradient used ONLY by the Wildcard category — see CURIO_SPEC.md §0.2.
 * This is the one place in the app where a gradient appears, so it stays special.
 */
object CurioGradients {
    val WildcardGradientStops = listOf(
        CurioColors.Lilac,
        CurioColors.DustyBlue,
        CurioColors.Sage,
        CurioColors.Peach,
        CurioColors.Teal
    )
}