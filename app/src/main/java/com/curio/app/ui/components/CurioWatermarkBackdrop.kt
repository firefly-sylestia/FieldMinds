package com.curio.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.curio.app.data.CurioCategory
import com.curio.app.ui.theme.CurioIcon

/**
 * Decorative backdrop pinned behind screen content: all eleven category
 * glyphs scattered around the screen edges in a very muted shade so the
 * quiet background carries a whisper of the Curio world without competing
 * with the content on top. The [activeCat]'s glyph gets a faint accent tint
 * so the page subtly echoes the category you're browsing.
 *
 * Shared by the Spin page and Home so the design language carries across
 * screens.
 *
 * Placement uses [Alignment] bias (-1..1 across the Box) so the collage
 * stays inside the screen on any device; the center is left free for the
 * main content.
 *
 * Glyphs mirror `CurioCategories.all` in data/Category.kt (11 categories,
 * verified 1:1 at startup) — if the catalog ever grows, add a tile here.
 */
@Composable
fun CurioWatermarkBackdrop(activeCat: CurioCategory, modifier: Modifier = Modifier) {
    val neutral = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
    val highlight = activeCat.accent.copy(alpha = 0.11f)
    Box(modifier = modifier.fillMaxSize()) {
        WatermarkGlyph("person", Alignment(-0.92f, -0.88f), 92.dp, -12f, neutral, activeCat, highlight)
        WatermarkGlyph("album", Alignment(0.62f, -0.92f), 64.dp, 10f, neutral, activeCat, highlight)
        WatermarkGlyph("videocam", Alignment(0.95f, -0.55f), 108.dp, -8f, neutral, activeCat, highlight)
        WatermarkGlyph("movie", Alignment(0.82f, -0.15f), 56.dp, 16f, neutral, activeCat, highlight)
        WatermarkGlyph("edit_note", Alignment(0.9f, 0.38f), 80.dp, -14f, neutral, activeCat, highlight)
        WatermarkGlyph("menu_book", Alignment(-0.88f, -0.38f), 72.dp, 8f, neutral, activeCat, highlight)
        WatermarkGlyph("brush", Alignment(-0.92f, 0.15f), 96.dp, -6f, neutral, activeCat, highlight)
        WatermarkGlyph("palette", Alignment(-0.78f, 0.68f), 88.dp, 12f, neutral, activeCat, highlight)
        WatermarkGlyph("science", Alignment(0.75f, 0.62f), 104.dp, -12f, neutral, activeCat, highlight)
        WatermarkGlyph("lightbulb", Alignment(0.05f, 0.92f), 76.dp, 6f, neutral, activeCat, highlight)
        WatermarkGlyph("casino", Alignment(0.3f, -0.2f), 92.dp, -4f, neutral, activeCat, highlight)
    }
}

/** One scattered glyph — muted neutral, or a whisper of accent if it's the active category's. */
@Composable
private fun BoxScope.WatermarkGlyph(
    glyph: String,
    alignment: Alignment,
    size: Dp,
    rotation: Float,
    neutral: Color,
    activeCat: CurioCategory,
    highlight: Color
) {
    CurioIcon(
        name = glyph,
        contentDescription = null,
        tint = if (glyph == activeCat.iconGlyph) highlight else neutral,
        size = size,
        modifier = Modifier
            .align(alignment)
            .graphicsLayer { rotationZ = rotation }
    )
}
