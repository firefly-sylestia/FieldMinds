package com.curio.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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
