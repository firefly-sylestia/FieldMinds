package com.curio.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Paper palette for the quotes entry (Marginalia journal + quote cards) and
 * the format text fields that wear rich text. Deliberately theme-agnostic —
 * a "note paper" look instead of the category tint:
 *
 * - Light mode: warm cream paper with a soft warm ink and faint ruled lines.
 * - Dark mode: warm off-black "toned paper" (the dark-paper variant), so the
 *   texture reads as paper in both themes rather than the app's surfaces.
 *
 * The highlighter marker is a translucent amber that reads on both papers.
 */
@Composable
fun paperSurface(): Color =
    if (isCurioDarkTheme()) Color(0xFF2A251D) else Color(0xFFFBF4E3)

/** Warm ink that reads on [paperSurface] in the active theme. */
@Composable
fun paperInk(): Color =
    if (isCurioDarkTheme()) Color(0xFFEADFC8) else Color(0xFF3B3124)

/** Faint ruled line on the paper — the notebook texture. */
@Composable
fun paperRule(): Color =
    if (isCurioDarkTheme()) Color(0xFF4A4134) else Color(0xFFE2D6BC)

/** Highlighter marker color — translucent amber that reads on both papers. */
@Composable
fun paperHighlight(): Color =
    if (isCurioDarkTheme()) Color(0x73D9B84A) else Color(0x66FFD54F)

/** Soft hairline edge so paper cards read as distinct notecards. */
@Composable
fun paperBorder(): Color =
    if (isCurioDarkTheme()) Color(0xFF55493A) else Color(0xFFE0D3B5)
