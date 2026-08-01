package com.curio.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.curio.app.data.CurioCategories
import com.curio.app.data.CurioCategory
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.isCurioDarkTheme
import kotlin.random.Random

/**
 * Decorative backdrop pinned behind screen content: all eleven category
 * glyphs scattered around the screen edges, each tinted with its own
 * category's accent — the exact color that drives that category's main-card
 * gradient — so the quiet background carries the same palette as the cards
 * without competing with the content on top. The [activeCat]'s glyph gets a
 * stronger whisper so the page subtly echoes the category you're browsing.
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
 *
 * Theme-aware alpha: dark mode keeps the glyphs very faint (they read as
 * color ghosts over the midnight surface); light mode raises the alpha a
 * touch so the accents stay visible over the warm cream surface.
 */
@Composable
fun CurioWatermarkBackdrop(activeCat: CurioCategory, modifier: Modifier = Modifier) {
    val isDark = isCurioDarkTheme()
    // Every glyph maps to its category accent — the same colors that open
    // the main-card gradients — so the backdrop palette always matches the
    // deck. Wildcard's glyph picks up the brand coral automatically.
    val accentByGlyph = remember {
        CurioCategories.all.associate { it.iconGlyph to it.accent }
    }
    Box(modifier = modifier.fillMaxSize()) {
        WatermarkGlyph("person", BiasAlignment(-0.92f, -0.88f), 92.dp, -12f, activeCat, accentByGlyph, isDark)
        WatermarkGlyph("album", BiasAlignment(0.62f, -0.92f), 64.dp, 10f, activeCat, accentByGlyph, isDark)
        WatermarkGlyph("videocam", BiasAlignment(0.95f, -0.55f), 108.dp, -8f, activeCat, accentByGlyph, isDark)
        WatermarkGlyph("movie", BiasAlignment(0.82f, -0.15f), 56.dp, 16f, activeCat, accentByGlyph, isDark)
        WatermarkGlyph("edit_note", BiasAlignment(0.9f, 0.38f), 80.dp, -14f, activeCat, accentByGlyph, isDark)
        WatermarkGlyph("menu_book", BiasAlignment(-0.88f, -0.38f), 72.dp, 8f, activeCat, accentByGlyph, isDark)
        WatermarkGlyph("brush", BiasAlignment(-0.92f, 0.15f), 96.dp, -6f, activeCat, accentByGlyph, isDark)
        WatermarkGlyph("palette", BiasAlignment(-0.78f, 0.68f), 88.dp, 12f, activeCat, accentByGlyph, isDark)
        WatermarkGlyph("science", BiasAlignment(0.75f, 0.62f), 104.dp, -12f, activeCat, accentByGlyph, isDark)
        WatermarkGlyph("lightbulb", BiasAlignment(0.05f, 0.92f), 76.dp, 6f, activeCat, accentByGlyph, isDark)
        WatermarkGlyph("casino", BiasAlignment(0.3f, -0.2f), 92.dp, -4f, activeCat, accentByGlyph, isDark)
    }
}

/**
 * One scattered glyph — its category's accent at a low alpha, or a stronger
 * whisper of the same color if it's the active category's glyph.
 */
/**
 * One scattered glyph — its category's accent at a low alpha, or a stronger
 * whisper of the same color if it's the active category's glyph.
 */
@Composable
private fun BoxScope.WatermarkGlyph(
    glyph: String,
    alignment: Alignment,
    size: Dp,
    rotation: Float,
    activeCat: CurioCategory,
    accentByGlyph: Map<String, Color>,
    isDark: Boolean
) {
    val active = glyph == activeCat.iconGlyph
    val accent = accentByGlyph[glyph] ?: CurioColors.WarmWatermarkInk
    val alpha = when {
        active -> if (isDark) 0.15f else 0.26f
        else -> if (isDark) 0.07f else 0.12f
    }
    CurioIcon(
        name = glyph,
        contentDescription = null,
        tint = accent.copy(alpha = alpha),
        size = size,
        modifier = Modifier
            .align(alignment)
            .graphicsLayer { rotationZ = rotation }
    )
}

/**
 * Decorative mood-board backdrop: a scatter of category glyphs laid out by
 * a [seed]-driven random pattern, so every mood board gets its own quiet
 * background collage. Theme-aware (faint in dark mode, slightly stronger in
 * light mode) and centred on [accent] tones so it stays legible behind
 * tiles. The seed is stable per board — derive it from the entry id when
 * re-rendering a saved board, or a fresh random value when creating one.
 */
@Composable
fun CurioMoodBoardBackdrop(
    seed: Int,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val isDark = isCurioDarkTheme()
    val accentByGlyph = remember {
        CurioCategories.all.associate { it.iconGlyph to it.accent }
    }
    val glyphs = remember {
        CurioCategories.all.map { it.iconGlyph }
    }
    // Deterministic per-seed scatter: count, glyphs, biases, sizes, rotations
    // and an alpha boost. The alpha boost stays in the seeded pattern (so each
    // glyph keeps its own random weight); the theme-aware base alpha is applied
    // at draw time so light/dark toggles re-render without re-seeding.
    val pattern = remember(seed, accentByGlyph, glyphs) {
        val rng = Random(seed)
        val count = 9 + rng.nextInt(4) // 9..12 glyphs
        List(count) {
            val glyph = glyphs[rng.nextInt(glyphs.size)]
            val accentForGlyph = accentByGlyph[glyph] ?: accent
            // BiasAlignment is -1..1 across the Box; bias toward the edges
            // and corners so the centre stays clear for tiles.
            val biasX = (if (rng.nextBoolean()) -1 else 1) * (0.55f + rng.nextFloat() * 0.4f)
            val biasY = (if (rng.nextBoolean()) -1 else 1) * (0.55f + rng.nextFloat() * 0.4f)
            WatermarkPlacement(
                glyph = glyph,
                alignment = BiasAlignment(biasX, biasY),
                size = (52f + rng.nextFloat() * 56f).dp, // 52..108 dp
                rotation = -18f + rng.nextFloat() * 36f,
                // Mostly the board's own accent family, sometimes a sibling
                // category colour for variety.
                tint = if (rng.nextFloat() < 0.75f) accent else accentForGlyph,
                alphaBoost = 0.7f + rng.nextFloat() * 0.6f // 0.7..1.3 per glyph
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        pattern.forEach { p ->
            // Theme-aware base alpha × the glyph's seeded boost, computed at
            // draw time so light/dark toggles apply immediately.
            val alpha = (if (isDark) 0.05f else 0.10f) * p.alphaBoost
            CurioIcon(
                name = p.glyph,
                contentDescription = null,
                tint = p.tint.copy(alpha = alpha),
                size = p.size,
                modifier = Modifier
                    .align(p.alignment)
                    .graphicsLayer { rotationZ = p.rotation }
            )
        }
    }
}

/** One seeded glyph placement for [CurioMoodBoardBackdrop]. */
private data class WatermarkPlacement(
    val glyph: String,
    val alignment: Alignment,
    val size: Dp,
    val rotation: Float,
    val tint: Color,
    val alphaBoost: Float
)
