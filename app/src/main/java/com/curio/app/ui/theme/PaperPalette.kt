package com.curio.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Paper palette for the quotes entry (Marginalia journal + quote cards) and
 * the format text fields that wear rich text. Deliberately theme-agnostic —
 * a "note paper" look instead of the category tint:
 *
 * The paper text boxes are NOT theme-aware: they wear the same warm cream
 * paper (with its dark ink, faint ruled lines, amber highlighter, and soft
 * hairline edge) in BOTH light and dark mode, so a note reads as a physical
 * slip of paper regardless of the app theme.
 *
 * The highlighter marker is a translucent amber that reads on the cream
 * paper.
 */
@Composable
fun paperSurface(): Color = Color(0xFFFBF4E3)

/** Warm dark ink that reads on the cream [paperSurface]. */
@Composable
fun paperInk(): Color = Color(0xFF3B3124)

/** Faint ruled line on the paper — the notebook texture. */
@Composable
fun paperRule(): Color = Color(0xFFE2D6BC)

/** Highlighter marker color — translucent amber on the cream paper. */
@Composable
fun paperHighlight(): Color = Color(0x66FFD54F)

/** Soft hairline edge so paper cards read as distinct notecards. */
@Composable
fun paperBorder(): Color = Color(0xFFE0D3B5)
