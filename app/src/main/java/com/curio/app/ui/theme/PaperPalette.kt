package com.curio.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.curio.app.data.NotePaperColor

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

/** Warm amber accent for paper-mode controls (the Ruled/Torn style chips,
 *  format tools, cursor) — reads clearly on cream and harmonizes with the
 *  note-paper look in both themes, unlike the theme's tertiary tint. */
@Composable
fun paperAccent(): Color = Color(0xFF9A7B2F)

/** Hairline edge so paper cards read as distinct notecards — a warm tan
 *  with real contrast against the cream surface (the older near-cream edge
 *  was effectively invisible). */
@Composable
fun paperBorder(): Color = Color(0xFFCBB98F)

// ─────────────────────────────────────────────────────────────────────────────
// Note-paper COLORS — a per-text-box swatch picker next to the Ruled/Torn
// toggle. [notePaperSurface] resolves the chosen [NotePaperColor] to its
// paper sheet; [notePaperInk] keeps the warm dark ink readable on every
// pastel; [notePaperRule] and [notePaperBorder] derive the ruled lines and
// hairline edge from the sheet so each color stays coherent. All are
// theme-agnostic — the paper is the same in both modes, whatever the swatch.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun notePaperSurface(color: NotePaperColor): Color = when (color) {
    NotePaperColor.CREAM -> Color(0xFFFBF4E3)
    NotePaperColor.BUTTER -> Color(0xFFFDF0C8)
    NotePaperColor.PINK -> Color(0xFFFBE5E0)
    NotePaperColor.MINT -> Color(0xFFE4EFDC)
    NotePaperColor.SKY -> Color(0xFFE0EDF5)
    NotePaperColor.LILAC -> Color(0xFFEDE4F4)
}

/** The warm dark ink reads on every light pastel sheet. */
@Composable
fun notePaperInk(color: NotePaperColor): Color = Color(0xFF3B3124)

/** Ruled lines derived from the sheet — slightly darker than the paper. */
@Composable
fun notePaperRule(color: NotePaperColor): Color = when (color) {
    NotePaperColor.CREAM -> Color(0xFFE2D6BC)
    NotePaperColor.BUTTER -> Color(0xFFE4D59E)
    NotePaperColor.PINK -> Color(0xFFE5C9C0)
    NotePaperColor.MINT -> Color(0xFFC4D5B8)
    NotePaperColor.SKY -> Color(0xFFBFD3E2)
    NotePaperColor.LILAC -> Color(0xFFD5C6E2)
}

/** Hairline edge derived from the sheet — a touch darker than the rules. */
@Composable
fun notePaperBorder(color: NotePaperColor): Color = when (color) {
    NotePaperColor.CREAM -> Color(0xFFCBB98F)
    NotePaperColor.BUTTER -> Color(0xFFD8C578)
    NotePaperColor.PINK -> Color(0xFFDBB3A8)
    NotePaperColor.MINT -> Color(0xFFAFC39F)
    NotePaperColor.SKY -> Color(0xFFA9C2D6)
    NotePaperColor.LILAC -> Color(0xFFC3B0D4)
}
