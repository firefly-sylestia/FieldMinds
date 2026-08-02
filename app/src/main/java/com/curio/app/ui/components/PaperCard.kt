package com.curio.app.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.curio.app.data.NotePaperColor
import com.curio.app.data.NotePaperStyle
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.notePaperBorder
import com.curio.app.ui.theme.notePaperInk
import com.curio.app.ui.theme.notePaperRule
import com.curio.app.ui.theme.notePaperSurface
import com.curio.app.ui.theme.paperAccent
import com.curio.app.ui.theme.paperInk
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.random.Random

/**
 * A note-paper card — the quotes entry's surface instead of the category
 * tint. Warm cream paper (both themes), with faint horizontal ruled lines
 * (notebook texture) and a soft hairline edge. [rotation] keeps the
 * hand-placed notecard feel in the saved view.
 *
 * Decoration flags extend the plain ruled page into the other notebook
 * styles: [redMargin] draws the classic school-notebook red vertical margin
 * line (text indented past it), [coffeeStains] spills deterministic coffee
 * blotches along the edges, and [folded] folds the top-right corner into a
 * dog-ear (corner cut by a diagonal, flap + crease shadow drawn over it;
 * the content is padded so text never runs under the flap).
 *
 * [contentPadding] defaults to a COMPACT inset (12dp) so quote cards stay
 * tight; pass a larger value for the journal page.
 */
@Composable
fun PaperCard(
    modifier: Modifier = Modifier,
    ruled: Boolean = true,
    rotation: Float = 0f,
    corner: Dp = 14.dp,
    paperColor: NotePaperColor = NotePaperColor.CREAM,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
    /** Minimum card height — a floor so a single-line field still reads as
     *  a proper note slip instead of collapsing to text + padding. The
     *  editor's text fields enforce 96dp; saved views pass the same so the
     *  note keeps its shape between edit and detail. */
    minHeight: Dp = 0.dp,
    /** Red school-notebook margin — a vertical red rule near the left edge
     *  with the text indented past it (the classic ruled-with-red-margin
     *  page). */
    redMargin: Boolean = false,
    /** Coffee-stain blotches along the paper's edges — deterministic per
     *  size (seeded), so typing / recomposition never re-rolls them. */
    coffeeStains: Boolean = false,
    /** Folded (dog-ear) top-right corner — the corner is cut by a diagonal
     *  and the folded flap + crease shadow drawn over it. */
    folded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val density = LocalDensity.current
    // Effective padding — the red margin and the folded corner need extra
    // inset so the text never runs under the margin line or the flap.
    val marginInset = with(density) { 22.dp.toPx() }
    val foldInset = with(density) { 22.dp.toPx() }
    // calculate*Padding return px (Float) — convert back to Dp for the
    // PaddingValues constructor, which takes Dp.
    val safePadding = with(density) {
        PaddingValues(
            left = if (redMargin) maxOf(
                contentPadding.calculateLeftPadding(LayoutDirection.Ltr),
                marginInset + 8.dp.toPx()
            ).toDp()
            else contentPadding.calculateLeftPadding(LayoutDirection.Ltr).toDp(),
            top = contentPadding.calculateTopPadding().toDp(),
            right = if (folded) maxOf(
                contentPadding.calculateRightPadding(LayoutDirection.Ltr),
                foldInset + 2.dp.toPx()
            ).toDp()
            else contentPadding.calculateRightPadding(LayoutDirection.Ltr).toDp(),
            bottom = contentPadding.calculateBottomPadding().toDp()
        )
    }
    val shape = remember(corner, folded) {
        if (folded) FoldedCornerShape(corner, 22.dp) else RoundedCornerShape(corner)
    }
    Surface(
        shape = shape,
        color = notePaperSurface(paperColor),
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, notePaperBorder(paperColor)),
        modifier = modifier.heightIn(min = minHeight).rotate(rotation)
    ) {
        Box(
            // Subtle rigid-card sheen — a whisper of top light + bottom
            // depth so the slip reads as stiff paper stock, not a flat fill.
            modifier = Modifier.background(
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.08f),
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.05f)
                    )
                )
            )
        ) {
            // Faint ruled lines behind the content — the notebook texture.
            // (notePaperRule() is @Composable, so resolve it here in the
            // composable scope — the Canvas draw lambda is not composable.)
            val ruleColor = if (ruled) notePaperRule(paperColor) else Color.Unspecified
            // Notebook cadence: rules spaced at the body line height,
            // starting one cadence below the top content padding so the
            // first line of text sits ON the first rule (real paper).
            // Guard against an Unspecified lineHeight (custom typography
            // that omits it) — fall back to the classic 24dp cadence.
            val bodyLineHeight = MaterialTheme.typography.bodyLarge.lineHeight
            val ruleSpacing = with(density) {
                if (bodyLineHeight == TextUnit.Unspecified) 24.dp.toPx() else bodyLineHeight.toPx()
            }
            val ruleStart = with(density) {
                safePadding.calculateTopPadding().toPx()
            } + ruleSpacing
            // Red margin color — a warm school-notebook red that reads on
            // the cream sheet in both themes.
            val marginColor = Color(0xFFC4524A)
            val paperSurface = notePaperSurface(paperColor)
            val paperEdge = notePaperBorder(paperColor)
            Canvas(modifier = Modifier.matchParentSize()) {
                if (ruled) {
                    var y = ruleStart
                    while (y < size.height) {
                        drawLine(
                            color = ruleColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f
                        )
                        y += ruleSpacing
                    }
                }
                if (redMargin) {
                    drawLine(
                        color = marginColor.copy(alpha = 0.55f),
                        start = Offset(marginInset, 0f),
                        end = Offset(marginInset, size.height),
                        strokeWidth = with(density) { 1.2.dp.toPx() }
                    )
                }
                if (coffeeStains) drawCoffeeStains(size, density)
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(safePadding),
                content = content
            )
            // Fold flap drawn ABOVE the content — the cut corner (Surface
            // clips to the shape) shows the background through the missing
            // corner, and the flap triangle + crease shadow sit on the paper.
            if (folded) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawFoldFlap(size, density, paperSurface, paperEdge)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Folded-corner (dog-ear) page + coffee-stain + red-margin decorations.
// The folded shape cuts the top-right corner along a diagonal (Surface
// clips content to it, so text can never run under the flap); the flap +
// crease are drawn as an overlay. Coffee stains and the red margin line
// render inside the rules Canvas (behind the text, like real ink).
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Rounded-rect outline with the TOP-RIGHT corner replaced by a diagonal
 * cut — the "folded page" dog-ear. The crease runs from `(w - fold, 0)` to
 * `(w, fold)`; the missing corner above it shows the page background, and
 * [drawFoldFlap] paints the folded flap + crease shadow over the paper.
 * Deterministic pure function of (size, corner, fold) — stable across
 * recompositions, so typing never re-folds the page.
 */
private class FoldedCornerShape(
    private val corner: Dp,
    private val fold: Dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val c = with(density) { corner.toPx() }
        val f = with(density) { fold.toPx() }
        val w = size.width
        val h = size.height
        val path = Path().apply {
            if (w <= 0f || h <= 0f) {
                moveTo(0f, 0f); lineTo(w, 0f); lineTo(w, h); lineTo(0f, h); close()
                return@apply
            }
            // Clockwise from the top edge: rounded top-left, straight top
            // to the fold, diagonal cut, straight right, then the two
            // remaining rounded corners.
            moveTo(c, 0f)
            // Top-left corner (rounded).
            cubicTo(0f, 0f, 0f, c, 0f, c)
            // Left edge down to the bottom-left corner.
            lineTo(0f, h - c)
            cubicTo(0f, h, c, h, c, h)
            // Bottom edge to the bottom-right corner.
            lineTo(w - c, h)
            cubicTo(w, h, w, h - c, w, h - c)
            // Right edge up to the fold's lower point.
            lineTo(w, f)
            // The diagonal cut — the dog-ear crease.
            lineTo(w - f, 0f)
            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * Coffee-stain blotches along the paper's edges — soft radial brown blobs
 * with a couple of darker "rings" (the classic coffee-ring look). Positions
 * are seeded and derived from [size] FRACTIONS, so every recomposition and
 * every card size renders the same stains — never re-rolled while typing.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCoffeeStains(
    canvasSize: Size,
    density: Density
) {
    val rnd = Random(0xCAFE5EED)
    val brown = Color(0xFF5B3A22)
    // Five blotches, one per edge + one center-right, all kept near the
    // margins so the writing area stays clean.
    val spots = listOf(
        Offset(canvasSize.width * 0.12f, canvasSize.height * 0.14f),
        Offset(canvasSize.width * 0.86f, canvasSize.height * 0.18f),
        Offset(canvasSize.width * 0.14f, canvasSize.height * 0.86f),
        Offset(canvasSize.width * 0.84f, canvasSize.height * 0.82f),
        Offset(canvasSize.width * 0.5f, canvasSize.height * 0.5f)
    )
    spots.forEachIndexed { i, center ->
        val radius = with(density) { (7 + rnd.nextInt(5)).dp.toPx() }
        val alpha = 0.05f + rnd.nextFloat() * 0.06f
        // Soft body + translucent edge (radial falloff).
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    brown.copy(alpha = alpha + 0.04f),
                    brown.copy(alpha = alpha * 0.4f),
                    Color.Transparent
                ),
                center = center,
                radius = radius
            ),
            radius = radius,
            center = center
        )
        // A few get the classic darker ring.
        if (i % 2 == 0) {
            drawCircle(
                color = brown.copy(alpha = alpha + 0.05f),
                radius = radius * 0.62f,
                center = center,
                style = Stroke(width = with(density) { 1.2.dp.toPx() })
            )
        }
    }
}

/**
 * The folded flap + crease shadow for the dog-ear. The flap is the
 * reflected corner triangle (w - fold, 0) → (w, fold) → (w - fold, fold),
 * painted slightly darker than the sheet (the underside of the fold); the
 * crease is a hairline along the diagonal; a faint drop shadow under the
 * flap gives the fold depth.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFoldFlap(
    size: Size,
    density: Density,
    paperSurface: Color,
    paperEdge: Color
) {
    val f = with(density) { 22.dp.toPx() }
    val w = size.width
    // Folded flap triangle.
    val flap = Path().apply {
        moveTo(w - f, 0f)
        lineTo(w, f)
        lineTo(w - f, f)
        close()
    }
    // Slightly darker paper tone for the underside of the fold.
    drawPath(flap, color = lerp(paperSurface, Color.Black, 0.07f))
    // Hairline crease along the diagonal.
    drawLine(
        color = lerp(paperEdge, Color.Black, 0.15f),
        start = Offset(w - f, 0f),
        end = Offset(w, f),
        strokeWidth = with(density) { 1.dp.toPx() }
    )
    // Soft drop shadow just under the flap.
    drawPath(
        Path().apply {
            moveTo(w - f, 0f)
            lineTo(w, f)
            lineTo(w - f + f * 0.18f, f * 0.82f)
            close()
        },
        color = Color.Black.copy(alpha = 0.06f)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Torn-note paper — a properly ripped slip. The edge is displaced with
// multi-octave FRACTAL noise (the feTurbulence technique from the TornPaper
// repo: fractalNoise + displacement), and the slip wears a pre-rendered
// grunge texture instead of hundreds of per-frame specks. Both are built
// ONCE per seed and cached, so typing / recomposition never rebuilds them —
// that was the lag source in the earlier implementation.
// ─────────────────────────────────────────────────────────────────────────────

/** Deterministic integer hash → [0, 1) — the lattice for the value noise. */
private fun hash2(seed: Int, x: Int, y: Int): Float {
    var n = x * 374761393 + y * 668265263 + seed * 1274126177
    n = (n xor (n ushr 13)) * 1274126177
    n = n xor (n ushr 16)
    return (n and 0x7fffffff).toFloat() / 0x7fffffff.toFloat()
}

/** Smooth value noise at [x], [y] — interpolated lattice hashes. */
private fun valueNoise(seed: Int, x: Float, y: Float): Float {
    val xi = floor(x).toInt()
    val yi = floor(y).toInt()
    val xf = x - floor(x)
    val yf = y - floor(y)
    val a = hash2(seed, xi, yi)
    val b = hash2(seed, xi + 1, yi)
    val c = hash2(seed, xi, yi + 1)
    val d = hash2(seed, xi + 1, yi + 1)
    val u = xf * xf * (3f - 2f * xf)
    val v = yf * yf * (3f - 2f * yf)
    return a + (b - a) * u + (c - a) * v + (a - b - c + d) * u * v
}

/**
 * Multi-octave fractal noise in [0, 1] — the torn edge's ragged bite. Pure
 * function of (seed, position), so the jagged outline is deterministic and
 * stable across recompositions; typing never re-rolls the rips.
 */
private fun fractalNoise(seed: Int, x: Float, y: Float, octaves: Int = 4): Float {
    var total = 0f
    var amp = 0.5f
    var freq = 1f
    var norm = 0f
    repeat(octaves) {
        total += amp * valueNoise(seed, x * freq, y * freq)
        norm += amp
        amp *= 0.5f
        freq *= 2.1f
    }
    return total / norm
}

/**
 * A jagged \"torn note\" outline — walks the card perimeter and displaces
 * each edge with [fractalNoise] (the TornPaper feTurbulence technique:
 * fractal noise sampled at ~0.06 base frequency, displacement map style),
 * so the result looks ripped from a notebook rather than cut.
 *
 * The computed outline is CACHED per size: `createOutline` is called on
 * every draw, but the Path only depends on (seed, size, density), so a
 * recomposition with an unchanged size reuses the cached outline instead of
 * rebuilding a ~150-point path every frame.
 */
private class TornPaperShape(private val seed: Int = 7) : Shape {
    private var cachedSize: Size? = null
    private var cachedOutline: Outline? = null

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        cachedOutline?.let { cached ->
            if (cachedSize == size) return cached
        }
        val outline = Outline.Generic(buildTornPath(seed, size, density))
        cachedSize = size
        cachedOutline = outline
        return outline
    }
}

private fun buildTornPath(seed: Int, size: Size, density: Density): Path {
    val w = size.width
    val h = size.height
    val path = Path()
    if (w <= 0f || h <= 0f) {
        path.moveTo(0f, 0f)
        path.lineTo(w, 0f)
        path.lineTo(w, h)
        path.lineTo(0f, h)
        path.close()
        return path
    }
    // Ragged bite amplitude + the slow deeper-tear layer, in px. The torn
    // edge must never reach far enough into the card to clip the field text:
    // TornPaperCard floors its content inset at 16dp horizontal / 14dp
    // vertical, and fractal noise is in [0,1] so (n - 0.5f) * 2 ∈ [-1,1] →
    // worst-case inward ≈ bite + tear. The amplitudes were trimmed after the
    // corner-clipping report (two edges meet at corners, compounding the
    // inward bite diagonally), then raised again for a rougher rip — the
    // current worst case (~4.6dp) still sits well inside the 16/14dp floor,
    // so the rips read rough without ever reaching the text.
    val bite = with(density) { 3.0.dp.toPx() }
    val tear = with(density) { 1.6.dp.toPx() }
    val step = with(density) { 6.dp.toPx() }
    // Base frequency ~0.06 (repo tornFrequency ≈ 0.05) + an offset per edge
    // so each side tears independently. The finer step (6dp vs 8dp) puts more
    // vertices on the perimeter, so the edge reads jagged and fibrous rather
    // than softly undulating.
    fun jitter(coord: Float, edgePhase: Float): Float =
        (fractalNoise(seed, coord * 0.06f, edgePhase) - 0.5f) * 2f * bite +
            (fractalNoise(seed + 31, coord * 0.013f, edgePhase + 7f) - 0.5f) * 2f * tear

    var first = true
    fun add(p: Offset) {
        if (first) {
            path.moveTo(p.x, p.y)
            first = false
        } else {
            path.lineTo(p.x, p.y)
        }
    }

    // ── Top edge (left → right) ───────────────────────────────────────
    var x = 0f
    while (x <= w) {
        add(Offset(x, jitter(x, 1f)))
        x += step
    }
    // ── Right edge (top → bottom) ─────────────────────────────────────
    var y = 0f
    while (y <= h) {
        add(Offset(w + jitter(y, 2f), y))
        y += step
    }
    // ── Bottom edge (right → left) ────────────────────────────────────
    x = w
    while (x >= 0f) {
        add(Offset(x, h + jitter(x, 3f)))
        x -= step
    }
    // ── Left edge (bottom → top) ──────────────────────────────────────
    y = h
    while (y >= 0f) {
        add(Offset(jitter(y, 4f), y))
        y -= step
    }
    path.close()
    return path
}

/**
 * Pre-renders the torn note's grunge texture — a tiled bitmap of faint ink
 * speckles + fiber dashes, generated once (see [sharedGrainBitmap]). Drawn
 * as a single shader rect every frame instead of ~300 drawCircle calls (the
 * old lag). The texture is GENERIC — the per-card seed already makes each
 * tear unique — so every torn card reuses ONE shared bitmap instead of
 * building a ~100KB texture per card.
 */
private fun buildGrainBitmap(seed: Int): ImageBitmap {
    val sizePx = 192
    val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bmp)
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
    val rnd = Random(seed)
    val inkArgb = 0xFF3B3124.toInt()
    // Faint speckle clusters — the paper grain. Kept sparse + low-alpha:
    // dense, high-alpha specks read as "dirty" rather than paper tooth.
    repeat(150) {
        val x = rnd.nextFloat() * sizePx
        val y = rnd.nextFloat() * sizePx
        val r = 0.5f + rnd.nextFloat() * 1.2f
        val alpha = (3 + rnd.nextInt(14)).coerceAtMost(22)
        paint.color = (alpha shl 24) or (inkArgb and 0xFFFFFF)
        canvas.drawCircle(x, y, r, paint)
    }
    // Sparse fiber dashes — the torn-sheet tooth, also kept faint.
    paint.strokeWidth = 1f
    repeat(18) {
        val x = rnd.nextFloat() * sizePx
        val y = rnd.nextFloat() * sizePx
        val len = 4f + rnd.nextFloat() * 8f
        val ang = rnd.nextFloat() * (Math.PI * 2).toFloat()
        val alpha = (3 + rnd.nextInt(8)).coerceAtMost(16)
        paint.color = (alpha shl 24) or (inkArgb and 0xFFFFFF)
        canvas.drawLine(x, y, x + cos(ang) * len, y + sin(ang) * len, paint)
    }
    return bmp.asImageBitmap()
}

/** One shared grunge texture for ALL torn cards — built lazily, once. */
private val sharedGrainBitmap: ImageBitmap by lazy { buildGrainBitmap(0x1F3D5) }

/**
 * The torn-note paper card — a properly ripped paper slip instead of the
 * rounded ruled notebook page. Jagged fractal-noise edges on every side
 * (deterministic per [seed], so recomposition/typing never re-rolls the
 * tear), a pre-rendered grunge texture (no per-frame speckle loop — the lag
 * fix), and no drop shadow (rasterizing a shadow for a ~150-point outline
 * every frame was the other lag source). [ruled] adds the notebook ruled
 * lines inside the torn outline (the \"rules on torn\" toggle). Theme-aware
 * cream paper in both themes. [rotation] keeps the hand-placed notecard feel.
 */
@Composable
fun TornPaperCard(
    modifier: Modifier = Modifier,
    rotation: Float = 0f,
    seed: Int? = null,
    ruled: Boolean = false,
    paperColor: NotePaperColor = NotePaperColor.CREAM,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    /** Same min-height floor as [PaperCard] — saved views pass it so short
     *  torn notes keep a proper slip shape. */
    minHeight: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    // Each card remembers its own seed so every torn note gets a distinct
    // tear pattern — stable per composition, so typing never re-rolls the
    // rips mid-edit. Explicit seeds (e.g. a saved entry id) can pin a
    // pattern across views.
    val effectiveSeed = seed ?: remember { Random.nextInt(1, 1_000_000) }
    // The Shape instance is remembered — Surface would otherwise construct a
    // fresh one every recomposition, defeating the outline cache inside it.
    val tornShape = remember(effectiveSeed) { TornPaperShape(effectiveSeed) }
    val surface = notePaperSurface(paperColor)
    val edge = notePaperBorder(paperColor)
    // The tiling brush wraps the SHARED texture — one bitmap, one brush, all
    // torn cards (the seed makes each card's EDGE unique, so the generic
    // grain can be shared).
    // This Compose version's ImageShader takes (image, tileModeX, tileModeY)
    // with NO filterQuality param, and the tiling enum is TileMode.Repeated
    // (not Repeat) — matching the version in gradle/libs.versions.toml.
    val grainBrush = remember {
        ShaderBrush(
            ImageShader(sharedGrainBitmap, TileMode.Repeated, TileMode.Repeated)
        )
    }
    // The torn outline can intrude up to ~3dp past the caller's inset (some
    // callers pass as little as 10dp of vertical padding for tight quote
    // cards). Floor the inset so the ragged edge NEVER clips the field text
    // — especially the first characters near the top-left corner, where two
    // torn edges meet and their inward bites compound diagonally.
    val safeContentPadding = PaddingValues(
        horizontal = maxOf(
            contentPadding.calculateLeftPadding(LayoutDirection.Ltr),
            16.dp
        ),
        vertical = maxOf(
            contentPadding.calculateTopPadding(),
            14.dp
        )
    )
    // Ruled lines (\"rules on torn\"): same notebook cadence as [PaperCard].
    val density = LocalDensity.current
    val ruleColor = if (ruled) notePaperRule(paperColor) else Color.Unspecified
    val ruleSpacingPx = if (ruled) with(density) {
        val lh = MaterialTheme.typography.bodyLarge.lineHeight
        if (lh == TextUnit.Unspecified) 24.dp.toPx() else lh.toPx()
    } else 0f
    val ruleStartPx = if (ruled) with(density) {
        safeContentPadding.calculateTopPadding().toPx()
    } + ruleSpacingPx else 0f

    Surface(
        shape = tornShape,
        color = surface,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, edge),
        modifier = modifier.heightIn(min = minHeight).rotate(rotation)
    ) {
        Box(
            // Subtle rigid-card sheen — matches PaperCard so torn + ruled
            // slips share the same stiff-paper feel.
            modifier = Modifier.background(
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.08f),
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.05f)
                    )
                )
            )
        ) {
            // One Canvas: the grunge shader rect + (optionally) the ruled
            // lines. Both are single draw calls per frame — cheap even while
            // typing.
            Canvas(modifier = Modifier.matchParentSize()) {
                drawRect(brush = grainBrush)
                if (ruled) {
                    var y = ruleStartPx
                    while (y < size.height) {
                        drawLine(
                            color = ruleColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f
                        )
                        y += ruleSpacingPx
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(safeContentPadding),
                content = content
            )
        }
    }
}

/**
 * Dispatch helper — renders a capture's note-paper in the style it was
 * written in: torn styles → [TornPaperCard] (with ruled lines when the
 * torn page carries them), otherwise the classic ruled [PaperCard]. Used by
 * the saved EntryDetail views so a torn note stays torn and a ruled note
 * stays ruled.
 */
@Composable
fun NotePaperCard(
    style: NotePaperStyle,
    modifier: Modifier = Modifier,
    ruled: Boolean = true,
    rotation: Float = 0f,
    corner: Dp = 14.dp,
    paperColor: NotePaperColor = NotePaperColor.CREAM,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
    minHeight: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    when (style) {
        NotePaperStyle.TORN -> TornPaperCard(
            modifier = modifier.heightIn(min = minHeight),
            rotation = rotation,
            ruled = false,
            paperColor = paperColor,
            contentPadding = contentPadding,
            content = content
        )
        NotePaperStyle.TORN_RULED -> TornPaperCard(
            modifier = modifier.heightIn(min = minHeight),
            rotation = rotation,
            ruled = true,
            paperColor = paperColor,
            contentPadding = contentPadding,
            content = content
        )
        NotePaperStyle.COFFEE -> PaperCard(
            modifier = modifier.heightIn(min = minHeight),
            ruled = true,
            rotation = rotation,
            corner = corner,
            paperColor = paperColor,
            contentPadding = contentPadding,
            coffeeStains = true,
            content = content
        )
        NotePaperStyle.FOLDED -> PaperCard(
            modifier = modifier.heightIn(min = minHeight),
            ruled = true,
            rotation = rotation,
            corner = corner,
            paperColor = paperColor,
            contentPadding = contentPadding,
            folded = true,
            content = content
        )
        NotePaperStyle.RED_MARGIN -> PaperCard(
            modifier = modifier.heightIn(min = minHeight),
            ruled = true,
            rotation = rotation,
            corner = corner,
            paperColor = paperColor,
            contentPadding = contentPadding,
            redMargin = true,
            content = content
        )
        NotePaperStyle.RULED -> PaperCard(
            modifier = modifier.heightIn(min = minHeight),
            ruled = ruled,
            rotation = rotation,
            corner = corner,
            paperColor = paperColor,
            contentPadding = contentPadding,
            content = content
        )
    }
}

/**
 * The per-text-box note-paper picker — a chip row for every style
 * (Ruled / Torn / Coffee / Folded / Red Margin, plus a \"rules\" chip that
 * appears while Torn is selected to switch between plain
 * [NotePaperStyle.TORN] and ruled [NotePaperStyle.TORN_RULED]). The row is
 * HORIZONTALLY SCROLLABLE — six chips overflow a phone-width toolbar row
 * (Rows don't wrap), so the chips scroll instead of clipping. Lives in the
 * field's own toolbar (alongside the format toolbox), NOT in a section-level
 * row — so each text box keeps its own independent paper look.
 */
@Composable
fun NotePaperStyleToggle(
    style: NotePaperStyle,
    onStyleChange: (NotePaperStyle) -> Unit,
    accent: Color = paperAccent(),
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val torn = style == NotePaperStyle.TORN || style == NotePaperStyle.TORN_RULED
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NotePaperStyleChip(
            icon = CurioIcons.MenuBook,
            label = "Ruled",
            active = style == NotePaperStyle.RULED,
            accent = accent,
            enabled = enabled,
            onClick = { onStyleChange(NotePaperStyle.RULED) }
        )
        NotePaperStyleChip(
            icon = CurioIcons.Palette,
            label = "Torn",
            active = torn,
            accent = accent,
            enabled = enabled,
            onClick = { onStyleChange(NotePaperStyle.TORN) }
        )
        NotePaperStyleChip(
            icon = CurioIcons.LocalCafe,
            label = "Coffee",
            active = style == NotePaperStyle.COFFEE,
            accent = accent,
            enabled = enabled,
            onClick = { onStyleChange(NotePaperStyle.COFFEE) }
        )
        NotePaperStyleChip(
            icon = CurioIcons.FoldedCorner,
            label = "Folded",
            active = style == NotePaperStyle.FOLDED,
            accent = accent,
            enabled = enabled,
            onClick = { onStyleChange(NotePaperStyle.FOLDED) }
        )
        NotePaperStyleChip(
            icon = CurioIcons.RedMarginLine,
            label = "Red Margin",
            active = style == NotePaperStyle.RED_MARGIN,
            accent = accent,
            enabled = enabled,
            onClick = { onStyleChange(NotePaperStyle.RED_MARGIN) }
        )
        // Rules on the torn slip — only meaningful while a torn style is on.
        if (torn) {
            NotePaperStyleChip(
                icon = CurioIcons.DragHandle,
                label = "Rules",
                active = style == NotePaperStyle.TORN_RULED,
                accent = accent,
                enabled = enabled,
                onClick = {
                    onStyleChange(
                        if (style == NotePaperStyle.TORN_RULED) NotePaperStyle.TORN
                        else NotePaperStyle.TORN_RULED
                    )
                }
            )
        }
    }
}

/**
 * The per-text-box note-paper COLOR picker — a compact row of circular
 * swatches (cream / butter / pink / mint / sky / lilac) shown next to the
 * Ruled/Torn toggle in the field's toolbar. The active swatch wears a
 * check + accent ring. Same always-cream-in-both-themes rule: the sheet
 * color is theme-agnostic, chosen per text box and persisted per field.
 */
@Composable
fun NotePaperColorToggle(
    color: NotePaperColor,
    onColorChange: (NotePaperColor) -> Unit,
    accent: Color = paperAccent(),
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    // Collapsible: the six swatches hide behind a compact "Color" chip so
    // the toolbar rows stay clean; the chip's own dot shows the current
    // paper color, and tapping expands the swatch row below it.
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Surface(
            onClick = { expanded = !expanded },
            enabled = enabled,
            shape = RoundedCornerShape(8.dp),
            color = if (expanded) accent.copy(alpha = 0.18f) else Color.Transparent,
            border = BorderStroke(
                1.dp,
                if (expanded) accent.copy(alpha = 0.6f) else accent.copy(alpha = 0.25f)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Live swatch of the current paper color — the border must
                // use CircleShape too, or a square outline would draw over
                // the circular fill (border defaults to RectangleShape).
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(notePaperSurface(color), CircleShape)
                        .border(1.dp, notePaperBorder(color), CircleShape)
                )
                CurioIcon(
                    name = CurioIcons.Palette,
                    contentDescription = null,
                    tint = if (expanded) accent else paperInk().copy(alpha = 0.55f),
                    size = 14.dp
                )
                Text(
                    text = "Color",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (expanded) accent else paperInk().copy(alpha = 0.55f)
                )
            }
        }
        if (expanded) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NotePaperColor.entries.forEach { candidate ->
                    val selected = candidate == color
                    Surface(
                        onClick = { onColorChange(candidate) },
                        enabled = enabled,
                        shape = CircleShape,
                        color = notePaperSurface(candidate),
                        border = BorderStroke(
                            if (selected) 2.dp else 1.dp,
                            if (selected) accent
                            else notePaperBorder(candidate).copy(alpha = 0.7f)
                        ),
                        // Each swatch announces itself by color name; the
                        // check icon is purely visual (no invisible-icon hack).
                        modifier = Modifier
                            .size(if (selected) 24.dp else 20.dp)
                            .semantics {
                                contentDescription =
                                    "${candidate.name.lowercase()} paper" + if (selected) " (selected)" else ""
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (selected) {
                                // A tiny paper-ink check inside the active
                                // swatch — the accent ring already marks it.
                                CurioIcon(
                                    name = CurioIcons.Check,
                                    contentDescription = null,
                                    tint = notePaperInk(candidate),
                                    size = 13.dp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotePaperStyleChip(
    icon: String,
    label: String,
    active: Boolean,
    accent: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        color = if (active) accent.copy(alpha = 0.18f) else Color.Transparent,
        border = BorderStroke(
            1.dp,
            if (active) accent.copy(alpha = 0.6f) else accent.copy(alpha = 0.25f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Inactive chips wear the paper ink at low alpha (not the theme's
            // onSurfaceVariant) — the paper controls sit on cream in BOTH
            // themes, so a theme-aware grey reads wrong against the paper.
            val inactive = paperInk().copy(alpha = 0.55f)
            CurioIcon(
                name = icon,
                contentDescription = label,
                tint = if (active) accent else inactive,
                size = 14.dp
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (active) accent else inactive
            )
        }
    }
}
