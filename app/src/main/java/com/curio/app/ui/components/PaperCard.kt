package com.curio.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.curio.app.data.NotePaperStyle
import com.curio.app.ui.theme.paperBorder
import com.curio.app.ui.theme.paperInk
import com.curio.app.ui.theme.paperRule
import com.curio.app.ui.theme.paperSurface
import kotlin.random.Random

/**
 * A note-paper card — the quotes entry's surface instead of the category
 * tint. Warm cream paper in light mode, warm off-black "toned paper" in dark,
 * with faint horizontal ruled lines (notebook texture) and a soft hairline
 * edge. [rotation] keeps the hand-placed notecard feel in the saved view.
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

/**
 * Deterministic pseudo-noise in [0, 1) for a given [seed] + coordinate — the
 * torn edge's jitter is a PURE function of position (not a random stream), so
 * the jagged outline stays rock-stable while the card grows/shrinks (typing,
 * layout changes) instead of re-rolling every recomposition. Two noise layers:
 * a fine jitter for the ragged bite + a slow wave for occasional deeper tears.
 */
private fun tornNoise(seed: Int, coord: Float): Float {
    val fine = (kotlin.math.sin(coord * 12.9898f + seed * 78.233f) * 43758.5453f)
    val slow = (kotlin.math.sin(coord * 0.35f + seed * 13.17f) * 1937.29f)
    return (fine - kotlin.math.floor(fine)) * 0.7f + (slow - kotlin.math.floor(slow)) * 0.3f
}

/**
 * A jagged "torn note" outline — walks the card perimeter and jitters each
 * edge inward/outward with [tornNoise], so the result looks ripped from a
 * notebook rather than cut. Pure function of (seed, size): deterministic,
 * stable across recompositions, and cheap to recompute per size change.
 */
private class TornPaperShape(private val seed: Int = 7) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) {
            return Outline.Rectangle(androidx.compose.ui.geometry.Rect(0f, 0f, w, h))
        }
        val path = Path()
        // Ragged bite amplitude + the slow deeper-tear amplitude, in px.
        // Kept MODEST on purpose: the torn edge must never reach far enough
        // into the card to clip the field text (quote cards carry only 10dp
        // of vertical padding). 2.5dp bite + 1.5dp tear ≈ 4dp worst-case
        // inward intrusion, well inside the smallest content inset.
        val bite = with(density) { 2.5.dp.toPx() }
        val tear = with(density) { 1.5.dp.toPx() }
        fun jitter(coord: Float): Float = (tornNoise(seed, coord) - 0.5f) * 2f * bite +
            (tornNoise(seed + 31, coord) - 0.5f) * 2f * tear

        var first = true
        fun add(p: Offset) {
            if (first) {
                path.moveTo(p.x, p.y)
                first = false
            } else {
                path.lineTo(p.x, p.y)
            }
        }

        // ── Top edge (left → right) ───────────────────────────────────
        var x = 0f
        while (x <= w) {
            add(Offset(x, jitter(x)))
            x += with(density) { 18.dp.toPx() }
        }
        // ── Right edge (top → bottom) ──────────────────────────────────
        var y = 0f
        while (y <= h) {
            add(Offset(w + jitter(y + 1000f), y))
            y += with(density) { 18.dp.toPx() }
        }
        // ── Bottom edge (right → left) ─────────────────────────────────
        x = w
        while (x >= 0f) {
            add(Offset(x, h + jitter(x + 2000f)))
            x -= with(density) { 18.dp.toPx() }
        }
        // ── Left edge (bottom → top) ───────────────────────────────────
        y = h
        while (y >= 0f) {
            add(Offset(jitter(y + 3000f), y))
            y -= with(density) { 18.dp.toPx() }
        }
        path.close()
        return Outline.Generic(path)
    }
}

/**
 * The torn-note paper card — a properly ripped paper slip instead of the
 * rounded ruled notebook page. Jagged edges on every side (deterministic per
 * [seed], so recomposition/typing never re-rolls the tear), a subtle paper
 * grain, no ruled lines, and a soft shadow that follows the ragged outline.
 * Theme-aware like [PaperCard]: cream in light, warm toned paper in dark.
 * [rotation] keeps the hand-placed notecard feel.
 */
@Composable
fun TornPaperCard(
    modifier: Modifier = Modifier,
    rotation: Float = 0f,
    seed: Int? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    // Each card remembers its own seed so every torn note gets a distinct
    // tear pattern — but it's STABLE per composition, so typing and
    // recomposition never re-roll the rips mid-edit. Explicit seeds (e.g. a
    // saved entry id) can pin a specific pattern across views.
    val effectiveSeed = seed ?: remember { Random.nextInt(1, 1_000_000) }
    val grain = paperInk()
    val surface = paperSurface()
    val edge = paperBorder()
    // The torn outline can intrude up to ~4dp past the caller's inset (some
    // callers pass as little as 10dp of vertical padding for tight quote
    // cards). Floor the inset so the ragged edge NEVER clips the field text
    // — especially the first characters near the top-left corner.
    val safeContentPadding = PaddingValues(
        horizontal = maxOf(
            contentPadding.calculateLeftPadding(LayoutDirection.Ltr),
            14.dp
        ),
        vertical = maxOf(
            contentPadding.calculateTopPadding(),
            12.dp
        )
    )
    Surface(
        shape = TornPaperShape(effectiveSeed),
        color = surface,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, edge),
        modifier = modifier.rotate(rotation)
    ) {
        Box {
            // Subtle paper grain — faint speckles so the slip reads as
            // textured paper rather than a flat fill. Deterministic from the
            // same seed (pure function of position), so it never flickers.
            Canvas(modifier = Modifier.matchParentSize()) {
                val grainSeed = effectiveSeed
                var gx = 7f
                var gy = 13f
                while (gy < size.height) {
                    gx += 11f
                    if (gx > size.width) {
                        gx = (tornNoise(grainSeed, gy) * size.width).coerceAtLeast(4f)
                        gy += 24f
                    }
                    val alpha = 0.03f + tornNoise(grainSeed * 3 + 17, gx * 3.7f + gy) * 0.05f
                    drawCircle(
                        color = grain.copy(alpha = alpha),
                        radius = 0.6f + tornNoise(grainSeed * 5 + 1, gx + gy) * 1.1f,
                        center = Offset(gx, gy)
                    )
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
 * written in: [NotePaperStyle.TORN] → [TornPaperCard], otherwise the classic
 * [PaperCard]. Used by the saved EntryDetail views so a torn note stays torn
 * and a ruled note stays ruled.
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
    if (style == NotePaperStyle.TORN) {
        TornPaperCard(
            modifier = modifier,
            rotation = rotation,
            contentPadding = contentPadding,
            content = content
        )
    } else {
        PaperCard(
            modifier = modifier,
            ruled = ruled,
            rotation = rotation,
            corner = corner,
            contentPadding = contentPadding,
            content = content
        )
    }
}
