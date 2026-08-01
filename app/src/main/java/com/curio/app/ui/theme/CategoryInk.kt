package com.curio.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.curio.app.data.AppPreferences
import com.curio.app.data.CategoryFamily
import com.curio.app.data.CurioCategory

/**
 * Theme-aware ink color for a category's accent-colored TEXT and ICONS that
 * sit on plain surfaces.
 *
 * The researched category accents (Tailwind-700 depth) read beautifully as
 * *fills* — cards, chips, buttons, gradients — with white content on top.
 * But used as *ink* on the midnight dark surfaces they fall below readable
 * contrast (e.g. indigo-700 text on #111722 ≈ 1.9:1). Each category pairs
 * its deep accent with a light 300-level twin ([CurioCategory.lightAccent]);
 * this extension resolves the correct one for the active theme, so accent
 * text/icons stay readable in both light and dark mode.
 */
@Composable
fun CurioCategory.categoryInk(): Color =
    if (isCurioDarkTheme()) lightAccent else accent

/**
 * Theme-aware wash color for a category-aware page BACKGROUND (Spin, Topic
 * Reveal, Save/Capture, Cabinet filter).
 *
 * Light mode: the accent is first softened toward white (30%) then blended
 * at a gentler 14% over the cream background — lighter and whiter than the
 * original 20% wash, so the tint reads airy instead of dark.
 *
 * Dark mode: the deep Tailwind-700 accent alone at 20% reads muddy (amber
 * goes brown, teal goes grey-green), while its light 300-level twin at any
 * useful fraction reads WHITE-WASHED over the midnight surface. So this
 * builds a saturated mid-tone — the accent lerped partway toward its light
 * twin (≈ the 500-level shade) — and washes it at a moderate fraction. The
 * page keeps the category's hue with real color, never a washed-out grey-white.
 *
 * A few families need extra contrast: at the default 50% midpoint, rose
 * (movies), sky (science) and especially coral (wildcard — its accent is
 * already a pastel pink) read too pale/white-washed. Those pull the
 * mid-tone closer to the deep accent and blend a touch stronger, so the
 * hue survives over midnight instead of flattening to grey-white.
 */
@Composable
fun CurioCategory.categoryBackgroundWash(): Color {
    val background = MaterialTheme.colorScheme.background
    // Settings toggle (v6.4): when the category tint is turned off, pages use
    // the plain theme background (cream in light, midnight in dark) exactly
    // as they did before the wash rollout.
    if (!AppPreferences.tintWashEnabledState) return background
    return if (isCurioDarkTheme()) {
        val tuning = DARK_WASH_TUNING[family] ?: DEFAULT_DARK_WASH
        val midTone = lerp(accent, lightAccent, tuning.midToneFactor)
        lerp(background, midTone, tuning.blendFraction)
    } else {
        lerp(background, lerp(accent, Color.White, 0.30f), 0.14f)
    }
}

/**
 * Theme-aware surface color for CARDS that sit on a tinted page background.
 *
 * Cards used plain theme surfaces (cream in light, midnight grey in dark),
 * which look out of place sitting on a category-tinted page. This resolves
 * the same per-family mid-tone as [categoryBackgroundWash] but blends a
 * little stronger, so a card reads as a tinted elevated surface instead of
 * a foreign cream block. Honors the Settings tint toggle — when it's off,
 * [base] is returned unchanged so cards go back to the plain theme surface.
 */
@Composable
fun CurioCategory.categorySurface(base: Color = MaterialTheme.colorScheme.surfaceContainerLow): Color {
    if (!AppPreferences.tintWashEnabledState) return base
    return if (isCurioDarkTheme()) {
        val tuning = DARK_WASH_TUNING[family] ?: DEFAULT_DARK_WASH
        val midTone = lerp(accent, lightAccent, tuning.midToneFactor)
        lerp(base, midTone, tuning.blendFraction + 0.10f)
    } else {
        lerp(base, lerp(accent, Color.White, 0.30f), 0.24f)
    }
}

/** Per-family dark-mode wash tuning (mid-tone pull + blend fraction). */
private class DarkWashTuning(val midToneFactor: Float, val blendFraction: Float)

private val DEFAULT_DARK_WASH = DarkWashTuning(0.5f, 0.15f)

private val DARK_WASH_TUNING: Map<CategoryFamily, DarkWashTuning> = mapOf(
    // Rose (movies) + Sky (science) lean too pale at the 50% midpoint —
    // pull toward the deep accent and blend a bit stronger.
    CategoryFamily.MOVIES  to DarkWashTuning(0.35f, 0.18f),
    CategoryFamily.SCIENCE to DarkWashTuning(0.35f, 0.18f),
    // Coral (wildcard) is a pastel accent, so the mid-tone is pale at any
    // factor — contrast has to come from a stronger blend over midnight.
    CategoryFamily.WILDCARD to DarkWashTuning(0.35f, 0.20f)
)
