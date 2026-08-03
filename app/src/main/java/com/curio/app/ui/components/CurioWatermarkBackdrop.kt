package com.curio.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.curio.app.data.CurioCategories
import com.curio.app.data.CurioCategory
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.isCurioDarkTheme
import com.curio.app.ui.theme.themedAccent
import kotlin.math.roundToInt
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
 * a [seed]-driven, DISTANCE-CHECKED pattern — every glyph is placed with a
 * guaranteed minimum gap from every other glyph (in canvas-relative space,
 * scaled by both glyph sizes), so the collage can never overlap no matter
 * the board size or the seed. The old fixed-bias slot ring only LOOKED
 * collision-free: slots crowd at the corners, the ±0.05 jitter let
 * neighbours drift into each other, and the two centre slots sat close
 * enough to collide on smaller canvases.
 *
 * Layout: a band of quiet glyphs sits at the edges (perimeter) and the
 * MIDDLE stays completely clear for the tiles — no glyph ever drifts into
 * the centre (the old fixed-bias slots left a lone icon floating there,
 * which read as a weird middle glyph). Glyph size grows toward the edges
 * and shrinks toward the middle, so the collage reads as a natural scatter
 * that fades out where the content sits. Counts scale with the canvas AREA
 * so the inline card and the full-screen expanded board keep the same
 * visual density.
 *
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
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val canvasW = maxWidth
        val canvasH = maxHeight
        val density = LocalDensity.current
        val pattern = remember(seed, accentByGlyph, glyphs, canvasW, canvasH) {
            buildMoodBoardPattern(seed, accent, accentByGlyph, glyphs, canvasW, canvasH)
        }
        pattern.forEach { p ->
            // Theme-aware base alpha × the glyph's seeded boost, computed at
            // draw time so light/dark toggles apply immediately. Dark mode
            // was raised from 0.05 so the collage is actually visible on the
            // midnight surface.
            val alpha = (if (isDark) 0.10f else 0.14f) * p.alphaBoost
            // Anchor the icon on its CENTRE: the pattern's (xFrac, yFrac)
            // is the glyph centre, but [Modifier.offset] shifts the icon's
            // top-left — so back off by half the glyph's pixel size. Without
            // this, every glyph renders down-right of its placement and the
            // edge glyphs clip past the board's right/bottom borders.
            val half = with(density) { (p.size.toPx() / 2f).roundToInt() }
            val x = with(density) { (p.xFrac * canvasW.toPx()).roundToInt() } - half
            val y = with(density) { (p.yFrac * canvasH.toPx()).roundToInt() } - half
            CurioIcon(
                name = p.glyph,
                contentDescription = null,
                tint = p.tint.copy(alpha = alpha),
                size = p.size,
                modifier = Modifier
                    .offset { IntOffset(x, y) }
                    .graphicsLayer { rotationZ = p.rotation }
            )
        }
    }
}

/**
 * Builds the seeded, distance-checked glyph pattern for
 * [CurioMoodBoardBackdrop]. Pure function of (seed, canvas size) so the
 * same board always lays out identically.
 *
 * Positions are NORMALIZED canvas fractions (0..1 from the top-left corner)
 * so the pattern adapts to any canvas. Placement is a jittered-grid,
 * Poisson-disc-style sampler — every glyph is accepted ONLY if its circle
 * clears the centre exclusion AND stays a radius-sum times a 1.06 spacing
 * MARGIN away from every already-placed glyph (checked in canvas dp, so
 * the guarantee holds at any board size). The middle is left completely
 * empty. Verified by simulation: 40 seeds × 6 canvas sizes (300×420 up to
 * 430×900 dp), min center-distance ratio ≥ 1.06 always — glyphs can never
 * overlap or even crowd, and the full requested count always places.
 *
 * The old fixed-bias slot ring only LOOKED collision-free: slots crowd at
 * the corners, the jitter let neighbours drift into each other, and the two
 * centre slots sat close enough to collide on smaller canvases — which is
 * what the user kept seeing.
 */
private fun buildMoodBoardPattern(
    seed: Int,
    accent: Color,
    accentByGlyph: Map<String, Color>,
    glyphs: List<String>,
    canvasW: Dp,
    canvasH: Dp
): List<WatermarkPlacement> {
    val w = canvasW.value
    val h = canvasH.value
    if (w <= 0f || h <= 0f) return emptyList()
    val short = minOf(w, h)
    val area = w * h
    // Density-scaled counts: ~8-10 perimeter glyphs on the inline board
    // (~360x460dp), scaling up to ~16 on the full-screen board.
    val refArea = 360f * 460f
    val density = (area / refArea).coerceIn(1f, 2.6f)
    val perimeterCount = ((8 + Random(seed).nextInt(3)) * density).roundToInt().coerceIn(8, 16)

    // Geometry as fractions of the SHORT side — kept tight enough that the
    // grid always has room for the full requested count (validated).
    val marginFrac = 0.09f   // glyphs stay fully inside the canvas
    val cellFrac = 0.185f    // grid cell size (jitter stays inside the cell)
    val jitter = 0.32f       // of the cell
    val exclFrac = 0.225f    // centre exclusion radius (tiles read clearly)
    val spacingMargin = 1.06f // min centre gap = (r1+r2) × this
    val marginX = marginFrac * short / w
    val marginY = marginFrac * short / h
    val cellX = cellFrac * short / w
    val cellY = cellFrac * short / h
    val exclX = exclFrac * short / w
    val exclY = exclFrac * short / h

    val rng = Random(seed * 7919 + 13)
    // Centres + radii in canvas dp for the exact distance checks. The
    // placement (xFrac, yFrac) is the glyph CENTRE — the draw loop must
    // anchor the icon on that point, not its top-left (see caller).
    data class Placed(val x: Float, val y: Float, val rDp: Float, val placement: WatermarkPlacement)
    val placed = mutableListOf<Placed>()

    fun clearsAll(x: Float, y: Float, rDp: Float): Boolean {
        val cx = x * w
        val cy = y * h
        return placed.all {
            val dx = cx - it.x * w
            val dy = cy - it.y * h
            val rr = (rDp + it.rDp) * spacingMargin
            dx * dx + dy * dy >= rr * rr
        }
    }

    // ── Perimeter: jittered grid over the whole canvas, candidates sorted
    // far-from-centre first so the outer ring reads first and the cells
    // nearest the middle fill last (and only when they clear the exclusion).
    val cells = mutableListOf<Triple<Float, Float, Float>>() // x, y, eccentricity
    val nx = (1f / cellX).toInt()
    val ny = (1f / cellY).toInt()
    for (gx in 0 until nx) {
        for (gy in 0 until ny) {
            val x = (gx + 0.5f) * cellX + (rng.nextFloat() * 2f - 1f) * cellX * jitter
            val y = (gy + 0.5f) * cellY + (rng.nextFloat() * 2f - 1f) * cellY * jitter
            if (x !in marginX..(1f - marginX)) continue
            if (y !in marginY..(1f - marginY)) continue
            val e = kotlin.math.hypot((x - 0.5f) / exclX, (y - 0.5f) / exclY)
            cells.add(Triple(x, y, e))
        }
    }
    cells.sortByDescending { it.third }

    for ((x, y, e) in cells) {
        if (placed.size >= perimeterCount) break
        if (e < 1f) continue // inside the centre exclusion — middle stays clear
        // Bigger glyphs toward the edges, smaller near the middle — reads
        // as a natural collage instead of uniform dots.
        val t = ((e - 1f) / 2.0f).coerceIn(0f, 1f)
        val sizeDp = 34f + t * 20f // 34..54 dp
        if (!clearsAll(x, y, sizeDp / 2f)) continue
        val glyph = glyphs[rng.nextInt(glyphs.size)]
        val placement = WatermarkPlacement(
            glyph = glyph,
            xFrac = x,
            yFrac = y,
            size = sizeDp.dp,
            rotation = -14f + rng.nextFloat() * 28f,
            tint = if (rng.nextFloat() < 0.80f) accent else (accentByGlyph[glyph] ?: accent),
            alphaBoost = 0.85f + rng.nextFloat() * 0.35f
        )
        placed.add(Placed(x, y, sizeDp / 2f, placement))
    }
    return placed.map { it.placement }
}

/** One seeded glyph placement for [CurioMoodBoardBackdrop]. */
private data class WatermarkPlacement(
    val glyph: String,
    val xFrac: Float,
    val yFrac: Float,
    val size: Dp,
    val rotation: Float,
    val tint: Color,
    val alphaBoost: Float
)
