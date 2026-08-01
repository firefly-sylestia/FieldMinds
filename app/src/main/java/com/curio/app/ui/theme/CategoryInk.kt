package com.curio.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.curio.app.data.AppPreferences
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
 * builds a saturated mid-tone — the accent lerped halfway toward its light
 * twin (≈ the 500-level shade) — and washes it at a moderate 15%. The page
 * keeps the category's hue with real color, never a washed-out grey-white.
 */
@Composable
fun CurioCategory.categoryBackgroundWash(): Color {
    val background = MaterialTheme.colorScheme.background
    // Settings toggle (v6.4): when the category tint is turned off, pages use
    // the plain theme background (cream in light, midnight in dark) exactly
    // as they did before the wash rollout.
    if (!AppPreferences.tintWashEnabledState) return background
    return if (isCurioDarkTheme()) {
        val midTone = lerp(accent, lightAccent, 0.5f)
        lerp(background, midTone, 0.15f)
    } else {
        lerp(background, lerp(accent, Color.White, 0.30f), 0.14f)
    }
}
