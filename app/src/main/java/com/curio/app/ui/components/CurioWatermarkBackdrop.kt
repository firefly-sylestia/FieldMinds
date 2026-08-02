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
import com.curio.app.ui.theme.themedAccent
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
 * Theme-aware alpha: dark mode keeps the glyphs as soft color ghosts over
 * the midnight surface (raised from the old near-invisible values so the
 * palette still reads); light mode stays a touch stronger over the warm
 * cream surface.
 */
@Composable
fun CurioWatermarkBackdrop(activeCat: CurioCategory, modifier: Modifier = Modifier) {
    val isDark = isCurioDarkTheme()
    // Every glyph maps to its category accent — the same colors that open
    // the main-card gradients — so the backdrop palette always matches the
    // deck. Wildcard's glyph picks up the brand coral automatically.
    // Rebuilt in the composable body (NOT remember) so the Material style's
    // device-color blend updates the backdrop glyphs when the style changes.
    val accentByGlyph = CurioCategories.all.associate { it.iconGlyph to it.themedAccent() }
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
    // Dark mode: glyphs are muted ghosts but must stay visible (raised from
    // 0.07/0.15 which barely read on the midnight surface). Light mode gets
    // a modest bump too so the palette stays present over cream.
    val alpha = when {
        active -> if (isDark) 0.22f else 0.30f
        else -> if (isDark) 0.11f else 0.15f
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
 * a [seed]-driven pattern over a sparse ring of anchor slots, so every mood
 * board gets its own quiet background collage WITHOUT glyphs overlapping
 * (positions are a seeded subset of well-spaced slots plus a small jitter).
 * Theme-aware (muted in dark mode, slightly stronger in light mode) and
 * centred on [accent] tones so it stays legible behind tiles. The seed is
 * stable per board — derive it from the entry id when re-rendering a saved
 * board, or a fresh random value when creating one.
 */
@Composable
fun CurioMoodBoardBackdrop(
    seed: Int,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val isDark = isCurioDarkTheme()
    // Rebuilt in the composable body (NOT remember) so the Material style's
    // device-color blend updates the backdrop glyphs when the style changes.
    val accentByGlyph = CurioCategories.all.associate { it.iconGlyph to it.themedAccent() }
    val glyphs = remember {
        CurioCategories.all.map { it.iconGlyph }
    }
    // Deterministic per-seed scatter: pick a seeded SUBSET of a sparse ring
    // of anchor slots (never two glyphs on the same slot) so the collage can
    // never overlap, then jitter each a little so it reads organic instead of
    // grid-locked. Sizes are capped so neighbouring slots stay clear. The
    // alpha boost stays in the seeded pattern (each glyph keeps its own
    // random weight); the theme-aware base alpha is applied at draw time so
    // light/dark toggles re-render without re-seeding.
    val pattern = remember(seed, accentByGlyph, glyphs) {
        val rng = Random(seed)
        // Sparse ring of well-spaced slots around the perimeter — the centre
        // stays clear for tiles.
        val slots = listOf(
            BiasAlignment(-0.90f, -0.90f), BiasAlignment(-0.35f, -0.92f),
            BiasAlignment(0.30f, -0.90f),  BiasAlignment(0.88f, -0.86f),
            BiasAlignment(0.95f, -0.42f),  BiasAlignment(0.90f, 0.10f),
            BiasAlignment(0.92f, 0.58f),   BiasAlignment(0.62f, 0.92f),
            BiasAlignment(0.05f, 0.94f),   BiasAlignment(-0.50f, 0.92f),
            BiasAlignment(-0.94f, 0.72f),  BiasAlignment(-0.95f, 0.18f),
            BiasAlignment(-0.92f, -0.35f), BiasAlignment(-0.60f, -0.55f)
        )
        val count = 9 + rng.nextInt(3) // 9..11 glyphs
        slots.shuffled(rng).take(count).map { slot ->
            val glyph = glyphs[rng.nextInt(glyphs.size)]
            val accentForGlyph = accentByGlyph[glyph] ?: accent
            // Small jitter keeps the subset from looking grid-locked without
            // letting neighbours collide (slots are far apart).
            val jitterX = (rng.nextFloat() * 2f - 1f) * 0.05f
            val jitterY = (rng.nextFloat() * 2f - 1f) * 0.05f
            WatermarkPlacement(
                glyph = glyph,
                alignment = BiasAlignment(
                    (slot.horizontalBias + jitterX).coerceIn(-1f, 1f),
                    (slot.verticalBias + jitterY).coerceIn(-1f, 1f)
                ),
                size = (46f + rng.nextFloat() * 38f).dp, // 46..84 dp
                rotation = -18f + rng.nextFloat() * 36f,
                // Mostly the board's own accent family, sometimes a sibling
                // category colour for variety.
                tint = if (rng.nextFloat() < 0.75f) accent else accentForGlyph,
                alphaBoost = 0.8f + rng.nextFloat() * 0.4f // 0.8..1.2 per glyph
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        pattern.forEach { p ->
            // Theme-aware base alpha × the glyph's seeded boost, computed at
            // draw time so light/dark toggles apply immediately. Dark mode
            // was raised from 0.05 so the collage is actually visible on the
            // midnight surface.
            val alpha = (if (isDark) 0.10f else 0.14f) * p.alphaBoost
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
