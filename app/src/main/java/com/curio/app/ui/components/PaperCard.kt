package com.curio.app.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.curio.app.data.NotePaperStyle
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.paperAccent
import com.curio.app.ui.theme.paperBorder
import com.curio.app.ui.theme.paperInk
import com.curio.app.ui.theme.paperRule
import com.curio.app.ui.theme.paperSurface
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
 * [contentPadding] defaults to a COMPACT inset (12dp) so quote cards stay
 * tight; pass a larger value for the journal page.
 */
@Composable
fun PaperCard(
    modifier: Modifier = Modifier,
    ruled: Boolean = true,
    rotation: Float = 0f,
    corner: Dp = 14.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(corner),
        color = paperSurface(),
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, paperBorder()),
        modifier = modifier.rotate(rotation)
    ) {
        Box {
            // Faint ruled lines behind the content — the notebook texture.
            // (paperRule() is @Composable, so resolve it here in the
            // composable scope — the Canvas draw lambda is not composable.)
            val ruleColor = if (ruled) paperRule() else Color.Unspecified
            if (ruled) {
                // Notebook cadence: rules spaced at the body line height,
                // starting one cadence below the top content padding so the
                // first line of text sits ON the first rule (real paper).
                val density = LocalDensity.current
                // Guard against an Unspecified lineHeight (custom typography
                // that omits it) — fall back to the classic 24dp cadence.
                val bodyLineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                val ruleSpacing = with(density) {
                    if (bodyLineHeight == TextUnit.Unspecified) 24.dp.toPx() else bodyLineHeight.toPx()
                }
                val ruleStart = with(density) {
                    contentPadding.calculateTopPadding().toPx()
                } + ruleSpacing
                Canvas(modifier = Modifier.matchParentSize()) {
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
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                content = content
            )
        }
    }
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
    // Ragged bite amplitude + the slow deeper-tear layer, in px. Kept MODEST
    // on purpose: the torn edge must never reach far enough into the card to
    // clip the field text (cards carry 10-14dp of padding). Fractal noise is
    // in [0,1], so (n - 0.5f) * 2 ∈ [-1,1] → worst-case inward ≈ bite + tear.
    // The amplitudes were trimmed after the corner-clipping report — at the
    // corners two edges meet, so an inward bite there compounds diagonally
    // into the first characters; small rips still read as torn, just never
    // into the text.
    val bite = with(density) { 2.0.dp.toPx() }
    val tear = with(density) { 1.0.dp.toPx() }
    val step = with(density) { 8.dp.toPx() }
    // Base frequency ~0.06 (repo tornFrequency ≈ 0.05) + an offset per edge
    // so each side tears independently.
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
    // Faint speckle clusters — the paper grain.
    repeat(360) {
        val x = rnd.nextFloat() * sizePx
        val y = rnd.nextFloat() * sizePx
        val r = 0.5f + rnd.nextFloat() * 1.6f
        val alpha = (6 + rnd.nextInt(30)).coerceAtMost(55)
        paint.color = (alpha shl 24) or (inkArgb and 0xFFFFFF)
        canvas.drawCircle(x, y, r, paint)
    }
    // Sparse fiber dashes — the torn-sheet tooth.
    paint.strokeWidth = 1f
    repeat(44) {
        val x = rnd.nextFloat() * sizePx
        val y = rnd.nextFloat() * sizePx
        val len = 4f + rnd.nextFloat() * 10f
        val ang = rnd.nextFloat() * (Math.PI * 2).toFloat()
        val alpha = (5 + rnd.nextInt(14)).coerceAtMost(34)
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
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
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
    val surface = paperSurface()
    val edge = paperBorder()
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
    val ruleColor = if (ruled) paperRule() else Color.Unspecified
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
        modifier = modifier.rotate(rotation)
    ) {
        Box {
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
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    when (style) {
        NotePaperStyle.TORN -> TornPaperCard(
            modifier = modifier,
            rotation = rotation,
            ruled = false,
            contentPadding = contentPadding,
            content = content
        )
        NotePaperStyle.TORN_RULED -> TornPaperCard(
            modifier = modifier,
            rotation = rotation,
            ruled = true,
            contentPadding = contentPadding,
            content = content
        )
        NotePaperStyle.RULED -> PaperCard(
            modifier = modifier,
            ruled = ruled,
            rotation = rotation,
            corner = corner,
            contentPadding = contentPadding,
            content = content
        )
    }
}

/**
 * The per-text-box note-paper picker — a compact Ruled / Torn toggle plus a
 * \"rules\" toggle that appears while Torn is selected (switches the torn
 * slip between plain [NotePaperStyle.TORN] and ruled [NotePaperStyle.TORN_RULED]).
 * Lives in the field's own toolbar (alongside the format toolbox), NOT in a
 * section-level row — so each text box keeps its own independent paper look.
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
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NotePaperStyleChip(
            icon = CurioIcons.MenuBook,
            label = "Ruled",
            active = !torn,
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
