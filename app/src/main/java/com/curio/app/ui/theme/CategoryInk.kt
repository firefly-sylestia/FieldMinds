package com.curio.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
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
 * Light mode: the deep accent at 20% over the cream background — the soft
 * pastel wash the user picked.
 *
 * Dark mode: the deep Tailwind-700 accents at 20% over the midnight surface
 * read muddy (amber goes brown, teal goes grey-green), so this uses each
 * category's light 300-level twin at a gentler 16% instead — a clean subtle
 * glow that keeps the page tied to the category without the murk.
 */
@Composable
fun CurioCategory.categoryBackgroundWash(): Color {
    val background = MaterialTheme.colorScheme.background
    return if (isCurioDarkTheme()) {
        lerp(background, lightAccent, 0.16f)
    } else {
        lerp(background, accent, 0.20f)
    }
}
