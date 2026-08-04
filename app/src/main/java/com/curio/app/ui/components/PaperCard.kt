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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
    // v7.16 — normal paper is SHARP-edged (a real cut sheet), not rounded.
    // The folded style keeps its diagonal dog-ear; the other corners stay
    // square to match.
    corner: Dp = 0.dp,
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
    // calculate*Padding return Dp in this Compose version, so the safe inset
    // is built in Dp directly (no px round-trip); only the Canvas's
    // red-margin rule needs px.
    val marginInsetDp = 22.dp
    val foldInsetDp = 28.dp
    val marginInset = with(density) { marginInsetDp.toPx() }
    val safePadding = PaddingValues(
        // PaddingValues has no `left`/`right` parameters — the horizontal
        // insets are `start`/`end`. The app is LTR-only, so start = left and
        // end = right; the Ltr-direction padding we read above maps straight
        // onto them.
        start = if (redMargin) maxOf(
            contentPadding.calculateLeftPadding(LayoutDirection.Ltr),
            marginInsetDp + 8.dp
        ) else contentPadding.calculateLeftPadding(LayoutDirection.Ltr),
        top = contentPadding.calculateTopPadding(),
        end = if (folded) maxOf(
            contentPadding.calculateRightPadding(LayoutDirection.Ltr),
            foldInsetDp + 2.dp
        ) else contentPadding.calculateRightPadding(LayoutDirection.Ltr),
        bottom = contentPadding.calculateBottomPadding()
    )
    val shape = remember(corner, folded) {
        if (folded) FoldedCornerShape(corner, 26.dp) else RoundedCornerShape(corner)
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
            modifier = Modifier.background(rigidCardSheen())
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
            val paperSurface = notePaperSurface(paperColor)
            val paperEdge = notePaperBorder(paperColor)
            val paperInkColor = notePaperInk(paperColor)
            // Every paper card rolls its OWN texture seed, so no two sheets
            // on the page share the same grain pattern (the old fixed seeds
            // drew the identical scatter on every card).
            val paperSeed = remember { Random.nextInt(1, 1_000_000) }
            Canvas(modifier = Modifier.matchParentSize()) {
                // Real paper texture — fine grain + soft tonal patches (the
                // crumpled-then-flattened tooth) under the rules and ink.
                // Seeded per card so each sheet's pattern is its own.
                drawPaperTexture(size, density, sharedGrainBrush, paperInkColor, paperSeed)
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
                        color = PaperMarginRed.copy(alpha = 0.55f),
                        start = Offset(marginInset, 0f),
                        end = Offset(marginInset, size.height),
                        strokeWidth = with(density) { 1.2.dp.toPx() }
                    )
                }
                if (coffeeStains) drawCoffeeStains(size, density, paperSeed)
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
                    drawFoldFlap(size, density, paperSurface, paperEdge, paperInkColor)
                }
            }
        }
    }
}

/**
 * The rigid-card sheen every paper style wears — a whisper of top light
 * fading through transparent to a faint bottom depth, so the slip reads as
 * stiff paper stock rather than a flat fill. Strong enough to read on the
 * torn page's grain (where it is drawn ON TOP of the texture), subtle
 * enough to never fight the text.
 */
private fun rigidCardSheen(): Brush = Brush.verticalGradient(
    listOf(
        Color.White.copy(alpha = 0.10f),
        Color.Transparent,
        Color.Black.copy(alpha = 0.06f)
    )
)

/** The classic school-notebook red margin rule — shared by the ruled page
 *  and the torn slip (universal decoration), so both draw the identical
 *  warm red line at the same inset. */
private val PaperMarginRed = Color(0xFFC4524A)

// ─────────────────────────────────────────────────────────────────────────────
// Universal note-paper model (v7.16) — every decoration (rules / coffee /
// folded / red margin) applies to EITHER base (ruled paper or torn slip),
// instead of the old duplicated per-base options. The enum keeps its
// flat names for persistence; these flag views decode it, and
// [notePaperStyleOf] re-encodes any combination back into one value.
// v7.18 — decorations STACK: coffee / folded / red margin are independent,
// so any combination renders (a folded coffee page with a red margin is
// legal). The flag views decode purely from the enum NAME (startsWith /
// contains), so the appended v7.18 combo values need no per-value logic.
// ─────────────────────────────────────────────────────────────────────────────

/** True when the slip wears the ragged torn outline instead of the sharp ruled page. */
val NotePaperStyle.torn: Boolean
    get() = name.startsWith("TORN")

/**
 * True when the sheet draws the notebook ruled lines. The ruled paper base
 * is always ruled (its name carries no RULED segment — but it isn't torn,
 * so it rules by definition); the torn slip only when its name carries
 * RULED.
 */
val NotePaperStyle.ruled: Boolean
    get() = !torn || name.contains("RULED")

/** True when the sheet wears the coffee-stain blotches along its edges. */
val NotePaperStyle.coffee: Boolean
    get() = name.contains("COFFEE")

/** True when the top-right corner is folded into a dog-ear. */
val NotePaperStyle.folded: Boolean
    get() = name.contains("FOLDED")

/** True when the sheet draws the red school-notebook margin line. */
val NotePaperStyle.redMargin: Boolean
    get() = name.contains("RED_MARGIN")

/**
 * Re-encodes a (base + decorations) combination into its [NotePaperStyle]
 * value by composing the enum NAME from its parts. v7.18 — decorations
 * STACK: coffee / folded / red margin are independent flags, so this
 * builds the exact combo name (e.g. torn + rules + coffee + folded →
 * TORN_RULED_COFFEE_FOLDED). The old single-select when-chain is gone.
 * Note: [ruled] is only meaningful when [torn] — the ruled paper base is
 * always ruled by definition, so callers pass ruled=true for paper (the
 * toggle does; direct callers should too).
 */
fun notePaperStyleOf(
    torn: Boolean,
    ruled: Boolean,
    coffee: Boolean = false,
    folded: Boolean = false,
    redMargin: Boolean = false
): NotePaperStyle {
    val deco = buildString {
        if (coffee) append("_COFFEE")
        if (folded) append("_FOLDED")
        if (redMargin) append("_RED_MARGIN")
    }
    val name = when {
        !torn -> if (deco.isEmpty()) "RULED" else deco.trimStart('_')
        ruled -> "TORN_RULED$deco"
        else -> "TORN$deco"
    }
    return NotePaperStyle.valueOf(name)
}

/**
 * The paper's own texture — a clean, quiet tooth:
 *
 * Layer 1 — Soft tonal variation: large-scale cloudy patches of slightly
 *   darker/lighter tone, like uneven fiber density in real paper.
 * Layer 2 — Fine grain field: scattered micro-specks (the paper tooth)
 *   drawn as individual dots, denser than the old bitmap but still soft.
 *
 * v7.16 — the old long curved fiber strands and big S-curve crease lines
 * are GONE (they read as muddy streaks on every sheet), and everything is
 * seeded from [seed] so every card wears its OWN pattern instead of the
 * same one on all papers. [grainBrush] stays as a subtle shared whisper
 * underneath (uniform grain, not a pattern) — the per-card seeded layers
 * are what make each sheet unique.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPaperTexture(
    canvasSize: Size,
    density: Density,
    grainBrush: Brush,
    ink: Color,
    seed: Int,
    grainAlpha: Float = 0.30f
) {
    val w = canvasSize.width
    val h = canvasSize.height
    // Subtle base wash — the SHARED grain bitmap as a whisper underneath
    // the per-card layers. Fixed low alpha so every sheet gets a uniform
    // paper-grain feel without repeating a visible pattern (the per-card
    // seeded specks below are what differ between sheets).
    drawRect(brush = grainBrush, alpha = 0.12f)
    // Layer 1 — soft tonal variation: several large cloudy patches of
    // slightly altered tone, like uneven pulp density. Radial gradients
    // at random positions, very low alpha so they whisper.
    val rndTone = Random(seed)
    repeat(4) {
        val cx = w * (0.1f + rndTone.nextFloat() * 0.8f)
        val cy = h * (0.1f + rndTone.nextFloat() * 0.8f)
        val radius = (w.coerceAtMost(h)) * (0.25f + rndTone.nextFloat() * 0.45f)
        val toneAlpha = 0.015f + rndTone.nextFloat() * 0.020f
        val toneColor = if (it % 2 == 0) ink.copy(alpha = toneAlpha)
                        else Color.White.copy(alpha = toneAlpha)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(toneColor, Color.Transparent),
                center = Offset(cx, cy),
                radius = radius
            ),
            radius = radius,
            center = Offset(cx, cy)
        )
    }
    // Layer 2 — fine grain field: individual micro-specks scattered across
    // the page. More numerous and slightly larger than the old bitmap so
    // the tooth actually reads instead of looking like dirt. Seeded per
    // card so no two sheets share the same speckle scatter.
    val rndGrain = Random(seed + 0x1F3D5)
    val specCount = (w * h / (with(density) { 80.dp.toPx() * 80.dp.toPx() })).toInt().coerceIn(60, 300)
    repeat(specCount) {
        val sx = rndGrain.nextFloat() * w
        val sy = rndGrain.nextFloat() * h
        val sr = with(density) { (0.4f + rndGrain.nextFloat() * 0.9f).dp.toPx() }
        val sa = (0.03f + rndGrain.nextFloat() * 0.05f) * grainAlpha
        drawCircle(color = ink.copy(alpha = sa), radius = sr, center = Offset(sx, sy))
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
 * Realistic coffee-stain blotches along the paper's edges — the dried-cup
 * look with proper ring-concentrated rims (the classic coffee-ring effect
 * where dissolved solids migrate to the edge and leave a dark, crisp ring
 * with a light translucent body), irregular organic pooling shapes, and
 * directional drip runs. v7.16 — seeded from [seed] (the card's own texture
 * seed) so every coffee paper spills a DIFFERENT stain layout instead of
 * the same blotches on every sheet; deterministic per card, so
 * recomposition/typing never re-rolls them.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCoffeeStains(
    canvasSize: Size,
    density: Density,
    seed: Int
) {
    val rnd = Random(seed * 31 + 0xCAFE5EED)
    // Richer warm coffee brown — deeper than the old #6B4226 so the ring
    // reads clearly on cream and pastel sheets.
    val coffee = Color(0xFF5C3620)
    val w = canvasSize.width
    val h = canvasSize.height
    // Main stains pinned to edges/corners; the writing area stays clean.
    val spots = listOf(
        0.11f to 0.14f,
        0.85f to 0.19f,
        0.07f to 0.81f,
        0.84f to 0.82f,
        0.48f to 0.09f
    )
    spots.forEachIndexed { i, (fx, fy) ->
        val center = Offset(
            w * (fx + (rnd.nextFloat() - 0.5f) * 0.04f),
            h * (fy + (rnd.nextFloat() - 0.5f) * 0.04f)
        )
        val r = with(density) { (10 + rnd.nextInt(15)).dp.toPx() }
        val ringAlpha = 0.12f + rnd.nextFloat() * 0.10f
        // Create an organic pooling shape — sampled N points around the
        // circle with radial wobble so the puddle reads as real dried
        // coffee, not a compass-drawn circle.
        fun poolPath(points: Int, wobble: Float, squashX: Float = 1f, squashY: Float = 1f): Path = Path().apply {
            var first = true
            repeat(points) { k ->
                val ang = (k.toFloat() / points) * (Math.PI * 2).toFloat()
                val rr = r * (1f + (rnd.nextFloat() - 0.5f) * wobble)
                val px = center.x + cos(ang) * rr * squashX
                val py = center.y + sin(ang) * rr * squashY
                if (first) { moveTo(px, py); first = false } else lineTo(px, py)
            }
            close()
        }
        // Slightly squashed pools — coffee never dries in a perfect circle.
        val sqX = 0.85f + rnd.nextFloat() * 0.30f
        val sqY = 0.85f + rnd.nextFloat() * 0.30f
        val pool = poolPath(20, 0.32f, sqX, sqY)
        // Light translucent body inside the ring — the dried liquid's
        // faint stain, fading toward the rim where the ring concentrates.
        drawPath(
            pool,
            brush = Brush.radialGradient(
                colors = listOf(
                    coffee.copy(alpha = ringAlpha * 0.22f),
                    coffee.copy(alpha = ringAlpha * 0.08f),
                    Color.Transparent
                ),
                center = center,
                radius = r * 1.15f
            )
        )
        // The concentrated ring — darker, slightly wider on the outside.
        // Real coffee rings: dissolved solids migrate to the perimeter,
        // leaving a crisp dark rim. Two passes: a soft wider stroke and a
        // crisp thinner stroke on top for the concentrated edge.
        drawPath(
            pool,
            color = coffee.copy(alpha = ringAlpha * 0.65f),
            style = Stroke(width = with(density) { 2.6.dp.toPx() })
        )
        drawPath(
            pool,
            color = coffee.copy(alpha = ringAlpha * 0.45f),
            style = Stroke(width = with(density) { 1.2.dp.toPx() })
        )
        // Inner ghost ring — some stains have a fainter inner ring where
        // the cup was lifted and set back down (rocking cup effect).
        if (i % 2 == 1) {
            val inner = poolPath(14, 0.18f, sqX * 0.85f, sqY * 0.85f)
            drawPath(
                inner,
                color = coffee.copy(alpha = ringAlpha * 0.55f),
                style = Stroke(width = with(density) { 1.4.dp.toPx() })
            )
        }
        // Directional drip runs — a couple of small streaks from the stain
        // edge trailing downward, like a drip that ran down the page.
        repeat(if (i % 3 == 0) 2 else 1) {
            val angle = (-0.3f + rnd.nextFloat() * 0.6f) // roughly downward ±0.3 rad
            val dripStart = Offset(
                center.x + cos(angle) * r * 0.9f,
                center.y + sin(angle) * r * 0.9f
            )
            val dripLen = with(density) { (5 + rnd.nextInt(9)).dp.toPx() }
            val dripEnd = Offset(
                dripStart.x + cos(angle) * dripLen * 0.5f,
                dripStart.y + sin(angle) * dripLen
            )
            val dripMid = Offset(
                (dripStart.x + dripEnd.x) / 2f + (rnd.nextFloat() - 0.5f) * dripLen * 0.25f,
                (dripStart.y + dripEnd.y) / 2f
            )
            val dripPath = Path().apply {
                moveTo(dripStart.x, dripStart.y)
                quadraticBezierTo(dripMid.x, dripMid.y, dripEnd.x, dripEnd.y)
            }
            drawPath(
                dripPath,
                color = coffee.copy(alpha = ringAlpha * 0.5f),
                style = Stroke(width = with(density) { 1.dp.toPx() })
            )
        }
        // Satellite splatter dots — tiny drops around the main stain.
        repeat(if (i % 2 == 0) 5 else 3) {
            val a = rnd.nextFloat() * (Math.PI * 2).toFloat()
            val d = r * (1.15f + rnd.nextFloat() * 0.65f)
            val drop = Offset(center.x + cos(a) * d, center.y + sin(a) * d)
            if (drop.x in 0f..w && drop.y in 0f..h) {
                drawCircle(
                    color = coffee.copy(alpha = ringAlpha * 0.55f),
                    radius = with(density) { (0.8 + rnd.nextFloat() * 1.5).dp.toPx() },
                    center = drop
                )
            }
        }
    }
    // Faint washed patches — the cup's moist footprint on the page,
    // larger and softer than the ring stains.
    repeat(3) {
        val px = w * (0.18f + rnd.nextFloat() * 0.64f)
        val py = h * (0.30f + rnd.nextFloat() * 0.45f)
        val pr = with(density) { (20 + rnd.nextInt(28)).dp.toPx() }
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    coffee.copy(alpha = 0.04f),
                    coffee.copy(alpha = 0.015f),
                    Color.Transparent
                ),
                center = Offset(px, py),
                radius = pr
            ),
            radius = pr,
            center = Offset(px, py)
        )
    }
}

/**
 * The folded flap + crease shadow for the dog-ear (v7.18 quality pass).
 * The flap is the reflected corner triangle with a rich THREE-stop gradient
 * — darkest along the crease (the underside of the bend), lightening toward
 * the tip. v7.18 — every color is derived from the PAPER's own palette
 * ([paperInk] / [paperEdge]) instead of raw black/white, so the fold reads
 * naturally on every sheet (cream + the pastel swatches) and in dark mode:
 * the flap is the paper's shaded backside, the shadow is a warm ink-tinted
 * feather, and the crease hairline stays on the sheet's own edge tone. A
 * soft specular still catches the fold ridge; the drop shadow is a feathered
 * wedge that fades to transparent (reads as lift on the torn grain too,
 * instead of a hard black triangle pasted over the rips).
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFoldFlap(
    canvasSize: Size,
    density: Density,
    paperSurface: Color,
    paperEdge: Color,
    paperInk: Color
) {
    val f = with(density) { 26.dp.toPx() }
    val s = with(density) { 4.dp.toPx() }
    val w = canvasSize.width
    // Feathered drop shadow — an ink-tinted wedge (NOT raw black: on the
    // torn grain and pastel sheets a hard black triangle reads as pasted
    // on) that casts down-left of the crease and fades out softly, so the
    // flap reads as genuinely lifted off the paper.
    drawPath(
        Path().apply {
            moveTo(w - f - s, s)
            lineTo(w - s, f + s)
            lineTo(w - f - s, f + s)
            close()
        },
        brush = Brush.linearGradient(
            colors = listOf(
                paperInk.copy(alpha = 0.16f),
                paperInk.copy(alpha = 0.07f),
                Color.Transparent
            ),
            start = Offset(w - f, s),
            end = Offset(w - f - s * 1.8f, f + s)
        )
    )
    // The flap itself — the paper's BACK side: darkest along the crease
    // (a shade of the sheet's own ink, not black), lightening toward the
    // tip with a faint light catch at the corner.
    val flap = Path().apply {
        moveTo(w - f, 0f)
        lineTo(w, f)
        lineTo(w - f, f)
        close()
    }
    drawPath(
        flap,
        brush = Brush.linearGradient(
            colors = listOf(
                lerp(paperSurface, paperInk, 0.22f),
                lerp(paperSurface, paperInk, 0.09f),
                lerp(paperSurface, Color.White, 0.06f)
            ),
            start = Offset(w - f / 2f, f / 2f),
            end = Offset(w - f * 0.10f, f * 1.05f)
        )
    )
    // Specular highlight — a thin light stroke just below the crease on
    // the flap body, where the folded paper bulges and catches the light.
    drawLine(
        color = Color.White.copy(alpha = 0.14f),
        start = Offset(w - f + s * 1.2f, s * 0.5f),
        end = Offset(w - s * 0.5f, f - s * 0.6f),
        strokeWidth = with(density) { 1.4.dp.toPx() }
    )
    // Soft crease halo — a wide, faint ink blur along the fold line.
    drawLine(
        color = paperInk.copy(alpha = 0.10f),
        start = Offset(w - f, 0f),
        end = Offset(w, f),
        strokeWidth = with(density) { 3.5.dp.toPx() }
    )
    // Crisp crease hairline — the actual fold edge, darkened toward the
    // sheet's ink so it stays crisp on cream AND pastel AND dark paper.
    drawLine(
        color = lerp(paperEdge, paperInk, 0.35f),
        start = Offset(w - f, 0f),
        end = Offset(w, f),
        strokeWidth = with(density) { 1.1.dp.toPx() }
    )
    // Corner-tip lift — the outermost point catches a brighter spot of
    // light, selling the "peeled-up corner" read.
    drawCircle(
        color = Color.White.copy(alpha = 0.10f),
        radius = with(density) { 2.8.dp.toPx() },
        center = Offset(w - f, 0f)
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
    // Ragged bite + slow deeper-tear + micro-detail layer, in px. The torn
    // edge must never reach far enough into the card to clip the field text:
    // TornPaperCard floors its content inset at 16dp horizontal / 14dp
    // vertical, and fractal noise is in [0,1] so (n - 0.5f) * 2 ∈ [-1,1] →
    // worst-case inward ≈ bite + tear + micro. The amplitudes were raised
    // for a rougher, more genuinely torn look — the current worst case
    // (~5.8dp) still sits well inside the 16/14dp floor, so the rips read
    // jagged and fibrous without ever reaching the text.
    val bite = with(density) { 3.8.dp.toPx() }
    val tear = with(density) { 2.0.dp.toPx() }
    val step = with(density) { 5.dp.toPx() }
    // Base frequency ~0.06 (repo tornFrequency ≈ 0.05) + an offset per edge
    // so each side tears independently. The finer 5dp step + a third micro-
    // octave layer puts denser, more varied vertices on the perimeter, so
    // the edge reads genuinely torn and fibrous rather than softly wavy.
    fun jitter(coord: Float, edgePhase: Float): Float =
        (fractalNoise(seed, coord * 0.07f, edgePhase) - 0.5f) * 2f * bite +
            (fractalNoise(seed + 31, coord * 0.015f, edgePhase + 7f) - 0.5f) * 2f * tear +
            (fractalNoise(seed + 47, coord * 0.035f, edgePhase + 13f) - 0.5f) * bite * 0.35f

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
 * A SOFT torn-paper edge — the hero's gradient blend. Unlike the sharp
 * multi-octave [TornPaperShape] (jagged rip), this edge is torn with small,
 * ROUNDED textures: single-octave smooth noise sampled at a fine step, so
 * each tooth is a gentle rounded bump (a real torn-fiber feel) rather than
 * a sharp zigzag. Amplitudes are tiny (≤ ~3dp) so the tear reads as fine
 * torn paper grain, not a shredded edge.
 *
 * Only ONE side is torn — the opposite long side stays straight — because
 * the hero clip tears just the lower edge where the gradient dissolves into
 * the page; the two white under-sheets layered behind it each carry their
 * OWN seed, so the seam reads as layered paper sheets with slightly
 * different tears.
 */
private class SoftTornEdgeShape(
    private val seed: Int,
    private val tornSide: SoftTornSide
) : Shape {
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
        val outline = Outline.Generic(buildSoftTornPath(seed, size, density, tornSide))
        cachedSize = size
        cachedOutline = outline
        return outline
    }
}

/** Which long edge of a [SoftTornEdgeShape] carries the soft tear. */
private enum class SoftTornSide { BOTTOM, TOP }

/** Public bottom-torn clip — the hero's lower edge (meeting point of the blur). */
class SoftTornBottomShape(seed: Int) : Shape {
    private val inner = SoftTornEdgeShape(seed, SoftTornSide.BOTTOM)
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline =
        inner.createOutline(size, layoutDirection, density)
}

/** Public top-torn clip — the white under-sheets peeking behind the hero. */
class SoftTornTopShape(seed: Int) : Shape {
    private val inner = SoftTornEdgeShape(seed, SoftTornSide.TOP)
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline =
        inner.createOutline(size, layoutDirection, density)
}

/**
 * Builds the soft torn outline: three straight edges + one softly torn
 * long edge. The torn edge is sampled every ~4dp along its length with a
 * SINGLE-octave smooth value-noise displacement (rounded, small amplitude
 * — the real-torn-paper feel), plus a whisper of a second octave for fine
 * fiber micro-texture. Pure function of (seed, size, density), cached per
 * size by [SoftTornEdgeShape].
 */
private fun buildSoftTornPath(
    seed: Int,
    size: Size,
    density: Density,
    tornSide: SoftTornSide
): Path {
    val w = size.width
    val h = size.height
    val path = Path()
    if (w <= 0f || h <= 0f) {
        path.moveTo(0f, 0f); path.lineTo(w, 0f); path.lineTo(w, h); path.lineTo(0f, h); path.close()
        return path
    }
    // Small rounded amplitudes: the main tooth ~2.2dp + a fiber micro-layer
    // ~0.9dp, so the tear reads as fine rounded paper grain. Sampling step
    // ~4dp keeps the bumps smooth (each sample is a gentle curve, no sharp
    // corners).
    val tooth = with(density) { 2.2.dp.toPx() }
    val micro = with(density) { 0.9.dp.toPx() }
    val step = with(density) { 4.dp.toPx() }
    // Single-octave smooth displacement (rounded bumps) + a fine second
    // octave for fiber texture.
    fun disp(x: Float): Float {
        val main = (valueNoise(seed, x * 0.045f, 0.5f) - 0.5f) * 2f * tooth
        val fiber = (valueNoise(seed + 71, x * 0.16f, 3.5f) - 0.5f) * 2f * micro
        return main + fiber
    }

    if (tornSide == SoftTornSide.BOTTOM) {
        // Clockwise: straight top, straight right, torn bottom (right→left),
        // straight left, close.
        path.moveTo(0f, 0f)
        path.lineTo(w, 0f)
        path.lineTo(w, h + disp(w))
        var x = w - step
        while (x > 0f) {
            path.lineTo(x, h + disp(x))
            x -= step
        }
        path.lineTo(0f, h + disp(0f))
        path.close()
    } else {
        // Torn TOP (the under-sheets): torn top (left→right), straight
        // right, straight bottom, straight left, close.
        path.moveTo(0f, disp(0f))
        var x = step
        while (x < w) {
            path.lineTo(x, disp(x))
            x += step
        }
        path.lineTo(w, disp(w))
        path.lineTo(w, h)
        path.lineTo(0f, h)
        path.close()
    }
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
 * One shared grain BRUSH wrapping [sharedGrainBitmap] — every paper card
 * (torn + ruled) tiles the same texture: the per-card seed keeps each torn
 * EDGE unique, and ruled sheets draw the grain at low alpha so they read as
 * smooth paper with tooth instead of grunge.
 */
private val sharedGrainBrush: Brush by lazy {
    ShaderBrush(ImageShader(sharedGrainBitmap, TileMode.Repeated, TileMode.Repeated))
}

/**
 * The torn-note paper card — a properly ripped paper slip instead of the
 * rounded ruled notebook page. Jagged fractal-noise edges on every side
 * (deterministic per [seed], so recomposition/typing never re-rolls the
 * tear), a pre-rendered grunge texture (no per-frame speckle loop — the lag
 * fix), and no drop shadow (rasterizing a shadow for a ~150-point outline
 * every frame was the other lag source). [ruled] adds the notebook ruled
 * lines inside the torn outline (the \"rules on torn\" toggle);
 * [coffeeStains] spills the same coffee blotches as the ruled page,
 * [folded] folds the top-right corner into a dog-ear (flap + crease drawn
 * OVER the torn outline — the fold covers the ragged corner, content is
 * inset past the flap), and [redMargin] draws the red school-notebook
 * margin line — the v7.16 universal decorations, all available on the torn
 * slip exactly like the ruled page. Theme-aware cream paper in both
 * themes. [rotation]
 * keeps the hand-placed notecard feel.
 */
@Composable
fun TornPaperCard(
    modifier: Modifier = Modifier,
    rotation: Float = 0f,
    seed: Int? = null,
    ruled: Boolean = false,
    coffeeStains: Boolean = false,
    folded: Boolean = false,
    // v7.16 — universal decoration: the red school-notebook margin line can
    // now ride a torn slip too, exactly like the ruled paper.
    redMargin: Boolean = false,
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
    // One shared tiling brush wraps the SHARED texture — no per-card brush
    // allocation (the seed makes each card's EDGE unique, so the generic
    // grain can be shared). The grain itself is drawn at FULL strength here
    // (it IS the torn tooth), with soft creases on top.
    // Hoisted (not built per frame): the torn canvas re-draws on every
    // frame while typing, and per-frame Brush allocation was one of the
    // original lag sources in this file.
    val paperInkColor = notePaperInk(paperColor)
    val sheen = remember { rigidCardSheen() }
    // The torn outline can intrude up to ~3dp past the caller's inset (some
    // callers pass as little as 10dp of vertical padding for tight quote
    // cards). Floor the inset so the ragged edge NEVER clips the field text
    // — especially the first characters near the top-left corner, where two
    // torn edges meet and their inward bites compound diagonally.
    // The red margin needs the same left inset as [PaperCard] so the text
    // never runs under the vertical rule.
    val safeContentPadding = PaddingValues(
        start = maxOf(
            contentPadding.calculateLeftPadding(LayoutDirection.Ltr),
            if (redMargin) 30.dp else 16.dp
        ),
        top = maxOf(
            contentPadding.calculateTopPadding(),
            14.dp
        ),
        // The folded dog-ear needs an end inset so text never runs under
        // the flap (same 24dp floor as [PaperCard]'s foldInsetDp + 2dp).
        end = maxOf(
            contentPadding.calculateRightPadding(LayoutDirection.Ltr),
            if (folded) 24.dp else 16.dp
        ),
        bottom = maxOf(
            contentPadding.calculateBottomPadding(),
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
        Box {
            // One Canvas: the grain texture + soft creases + (optionally)
            // the ruled lines + (optionally) the coffee stains + the
            // rigid-card sheen. Single draw calls per frame — cheap even
            // while typing. The sheen is drawn LAST so it reads ON TOP of
            // the grain — under it, the texture flattens the vertical light
            // gradient and the torn slip looks flat.
            // The torn edge can intrude up to ~3dp past the caller's inset;
            // the margin line is drawn at the same 22dp position [PaperCard]
            // uses, clear of the ragged left edge.
            val marginInset = with(density) { 22.dp.toPx() }
            Canvas(modifier = Modifier.matchParentSize()) {
                drawPaperTexture(
                    size, density, sharedGrainBrush, paperInkColor,
                    seed = effectiveSeed, grainAlpha = 1f
                )
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
                if (redMargin) {
                    drawLine(
                        color = PaperMarginRed.copy(alpha = 0.55f),
                        start = Offset(marginInset, 0f),
                        end = Offset(marginInset, size.height),
                        strokeWidth = with(density) { 1.2.dp.toPx() }
                    )
                }
                if (coffeeStains) drawCoffeeStains(size, density, effectiveSeed)
                drawRect(brush = sheen)
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(safeContentPadding),
                content = content
            )
            // Fold flap drawn ABOVE the content — the flap + crease shadow
            // sit on the torn paper, covering the ragged corner like a real
            // dog-ear (Surface clips to the torn outline, so the missing
            // corner shows the page background behind the flap). The flap is
            // shaded with the sheet's OWN ink so it reads as the paper's
            // backside on the torn grain instead of a pasted-on black wedge.
            if (folded) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawFoldFlap(size, density, surface, edge, paperInkColor)
                }
            }
        }
    }
}

/**
 * Dispatch helper — renders a capture's note-paper in the style it was
 * written in: torn styles → [TornPaperCard], otherwise the sharp ruled
 * [PaperCard]. Every decoration (rules / coffee / folded / red margin) is
 * decoded from the style's flags and passed to whichever base, so any
 * combination renders correctly. Used by the saved EntryDetail views so a
 * torn note stays torn and a ruled note stays ruled.
 */
@Composable
fun NotePaperCard(
    style: NotePaperStyle,
    modifier: Modifier = Modifier,
    ruled: Boolean = true,
    rotation: Float = 0f,
    corner: Dp = 0.dp,
    paperColor: NotePaperColor = NotePaperColor.CREAM,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
    minHeight: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    if (style.torn) {
        TornPaperCard(
            modifier = modifier.heightIn(min = minHeight),
            rotation = rotation,
            ruled = style.ruled,
            coffeeStains = style.coffee,
            folded = style.folded,
            redMargin = style.redMargin,
            paperColor = paperColor,
            contentPadding = contentPadding,
            content = content
        )
    } else {
        // The ruled paper base is always ruled; [ruled] only relaxes the
        // plain RULED style for callers that explicitly ask (none do today).
        PaperCard(
            modifier = modifier.heightIn(min = minHeight),
            ruled = if (style == NotePaperStyle.RULED) ruled else true,
            rotation = rotation,
            corner = corner,
            paperColor = paperColor,
            contentPadding = contentPadding,
            coffeeStains = style.coffee,
            folded = style.folded,
            redMargin = style.redMargin,
            content = content
        )
    }
}

/**
 * The per-text-box note-paper picker — v7.16 UNIVERSAL: a base row
 * (Ruled / Torn) plus a decoration row whose chips work on EITHER base.
 * v7.18 — decorations STACK: Coffee / Folded / Red Margin are independent
 * toggles, so any combination can be on at once (a folded coffee page with
 * a red margin is legal), and, while the torn base is active, a "+ Rules"
 * chip toggles the ruled lines onto the torn slip. Switching bases keeps
 * the chosen decorations, so nothing is lost. Lives in the field's own
 * toolbar (alongside the format toolbox), NOT in a section-level row — so
 * each text box keeps its own independent paper look.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotePaperStyleToggle(
    style: NotePaperStyle,
    onStyleChange: (NotePaperStyle) -> Unit,
    accent: Color = paperAccent(),
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Row 1 — base: Ruled (sharp ruled page) · Torn (ragged slip).
        // Switching keeps the current decoration via [notePaperStyleOf].
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            CompactPaperChip("Ruled", !style.torn, accent, enabled) {
                onStyleChange(notePaperStyleOf(false, true, style.coffee, style.folded, style.redMargin))
            }
            CompactPaperChip("Torn", style.torn, accent, enabled) {
                onStyleChange(notePaperStyleOf(true, style.ruled, style.coffee, style.folded, style.redMargin))
            }
        }
        // Row 2 — UNIVERSAL decorations: the same chips apply to both bases.
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (style.torn) {
                // Rules only matter on the torn slip — the ruled page is
                // always ruled by definition.
                CompactPaperChip("+ Rules", style.ruled, accent, enabled) {
                    onStyleChange(notePaperStyleOf(true, !style.ruled, style.coffee, style.folded, style.redMargin))
                }
            }
            // Independent STACKABLE toggles (v7.18) — each chip flips ONLY
            // its own flag and passes the others through, so decorations
            // combine instead of cancelling each other.
            CompactPaperChip("+ Coffee", style.coffee, accent, enabled) {
                onStyleChange(notePaperStyleOf(
                    style.torn, style.ruled,
                    coffee = !style.coffee,
                    folded = style.folded,
                    redMargin = style.redMargin
                ))
            }
            CompactPaperChip("+ Folded", style.folded, accent, enabled) {
                onStyleChange(notePaperStyleOf(
                    style.torn, style.ruled,
                    coffee = style.coffee,
                    folded = !style.folded,
                    redMargin = style.redMargin
                ))
            }
            CompactPaperChip("+ Red Margin", style.redMargin, accent, enabled) {
                onStyleChange(notePaperStyleOf(
                    style.torn, style.ruled,
                    coffee = style.coffee,
                    folded = style.folded,
                    redMargin = !style.redMargin
                ))
            }
        }
    }
}

/** Compact paper-style pill chip — label-only, tight padding, tiny font. */
@Composable
private fun CompactPaperChip(
    label: String,
    active: Boolean,
    accent: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(6.dp),
        color = if (active) accent.copy(alpha = 0.20f) else Color.Transparent,
        border = BorderStroke(
            1.dp,
            if (active) accent.copy(alpha = 0.55f) else accent.copy(alpha = 0.18f)
        )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (active) accent else paperInk().copy(alpha = 0.50f),
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        )
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

