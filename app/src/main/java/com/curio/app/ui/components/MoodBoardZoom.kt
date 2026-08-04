package com.curio.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.consume
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.curio.app.data.CaptureData
import com.curio.app.data.NotePaperColor
import com.curio.app.data.NotePaperStyle
import com.curio.app.ui.components.NotePaperCard
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.PatrickHandFontFamily
import com.curio.app.ui.theme.notePaperInk
import com.curio.app.ui.theme.CurioIcons
import androidx.compose.ui.util.lerp
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Mutable zoom state for one mood-board canvas. Only a SINGLE tile image is
 * ever magnified ([zoomedUri]) — the board around it stays put. The overlay
 * glides that one image from its resting spot on the collage to the center
 * of the viewport along an ARC (v7.24), then keeps it there until the user
 * taps (or double-taps) to close; pinch-to-zoom and one-finger pan refine
 * the magnified image while it's open.
 *
 * [scaleTarget] and the pan offsets in pixels are the animation TARGETS
 * (the image centered at that zoom); the animated values live inside
 * [MoodBoardZoomOverlay] so the glide opens and closes smoothly.
 *
 * [closing] is a latch set by [zoomOut]: the overlay only removes itself
 * once the close animation has settled.
 */
class MoodBoardZoomState {
    var zoomedUri by mutableStateOf<String?>(null)
    var scaleTarget by mutableFloatStateOf(1f)
    var offsetX by mutableFloatStateOf(0f)
    var offsetY by mutableFloatStateOf(0f)
    var closing by mutableStateOf(false)
    // The zoom level [zoomIn] opens at for the CURRENT tile — fit-based so a
    // small tile opens large enough to read while a big tile doesn't blow
    // past the screen.
    var defaultScale by mutableFloatStateOf(2.4f)

    /**
     * Double-tap a tile: only that image zooms. The overlay glides it from
     * its resting spot to the viewport CENTER ([centerX]/[centerY] = the
     * tile's center in viewport coordinates) at the fit-based zoom level,
     * so the screen visibly swoops to the image without moving the board.
     */
    fun zoomIn(
        uri: String,
        centerX: Float = 0f,
        centerY: Float = 0f,
        tileW: Float = 0f,
        tileH: Float = 0f,
        viewW: Float = 0f,
        viewH: Float = 0f
    ) {
        closing = false
        if (zoomedUri != uri) {
            zoomedUri = uri
            scaleTarget = 1f
            offsetX = 0f
            offsetY = 0f
        }
        defaultScale = fitZoomScale(tileW, tileH, viewW, viewH)
        scaleTarget = defaultScale
        // Glide target: with the image scaled around its own center, the
        // transform that places its center at the viewport center is
        //   translation = scaleTarget · (viewportCenter − imageCenter)
        offsetX = scaleTarget * (viewW / 2f - centerX)
        offsetY = scaleTarget * (viewH / 2f - centerY)
    }

    /**
     * Fit-based open zoom: ~90% of the viewport's short side, clamped 1.1–5x.
     * The 1.1f floor means a tile that already fills the board opens at
     * essentially its fit size (straight + a slight lift) instead of
     * exploding past the screen — while small tiles still zoom in a lot.
     */
    private fun fitZoomScale(tileW: Float, tileH: Float, viewW: Float, viewH: Float): Float {
        if (tileW <= 0f || tileH <= 0f || viewW <= 0f || viewH <= 0f) return 2.4f
        val fit = minOf(viewW / tileW, viewH / tileH)
        return (fit * 0.9f).coerceIn(1.1f, 5f)
    }

    /**
     * Zoom back out to 1x; the overlay clears itself once the close
     * animation settles.
     */
    fun zoomOut() {
        closing = true
        scaleTarget = 1f
        offsetX = 0f
        offsetY = 0f
    }
}

/** Remember a [MoodBoardZoomState] scoped to one board canvas. */
@Composable
fun rememberMoodBoardZoomState(): MoodBoardZoomState = remember { MoodBoardZoomState() }

// ── High-resolution image loading ─────────────────────────────────────
// Coil's default decode size tracks the composable's on-screen size
// (~160dp), so magnifying a tile to 2.4-4x upscaled a tiny bitmap and looked
// awful. Decode larger instead: board tiles get a ~3-4x headroom cap and the
// magnified overlays a higher cap, so zoomed-in detail stays pixel-sharp.

/**
 * v7.19 — zoom animation spec: opening uses a deliberate spring so the
 * magnify reads as a physical glide, but CLOSING uses a fast tween so the
 * minimize snaps shut instead of lagging/delaying behind a long spring
 * tail (the old 280-stiffness spring took ~half a second to settle and
 * the overlay only removed itself after that).
 */
private fun moodBoardZoomSpec(closing: Boolean): FiniteAnimationSpec<Float> =
    if (closing) tween(durationMillis = 170, easing = FastOutSlowInEasing)
    else spring(dampingRatio = 0.8f, stiffness = 320f)

/** Decode cap for board tiles (~3-4x their on-screen size). */
private const val MoodBoardTileDecodePx = 1024

/** Decode cap for the magnified overlays (supports ~4x zoom at 3x density). */
private const val MoodBoardZoomDecodePx = 2048

/**
 * High-resolution Coil painter for mood-board images. [zoomed] requests the
 * larger cap for the magnified overlays so the zoomed image stays crisp.
 */
@Composable
fun moodBoardPainter(uri: String, zoomed: Boolean = false): Painter {
    val context = LocalContext.current
    val cap = if (zoomed) MoodBoardZoomDecodePx else MoodBoardTileDecodePx
    return rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(uri)
            .size(cap, cap)
            .build()
    )
}

/**
 * Static mood-board tile collage — renders saved [CaptureData.TileLayout]s at
 * their pixel offsets with their rotations. Shared by the saved EntryDetail
 * board, the expanded full-screen board and the board-magnifier overlay.
 * When [onTileZoom] is provided each tile responds to double-tap.
 */
@Composable
fun MoodBoardTiles(
    tiles: List<CaptureData.TileLayout>,
    canvasWPx: Float,
    canvasHPx: Float,
    onTileZoom: ((String, Float, Float, Float, Float, Float, Float) -> Unit)? = null,
    zoomed: Boolean = false
) {
    val density = LocalDensity.current
    tiles.forEachIndexed { i, tile ->
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        tile.offsetXPx.roundToInt().coerceIn(0, canvasWPx.roundToInt()),
                        tile.offsetYPx.roundToInt().coerceIn(0, canvasHPx.roundToInt())
                    )
                }
                .zIndex(i.toFloat())
                .then(
                    if (onTileZoom != null) {
                        // Double-tap zooms the tile — same gesture as the
                        // editor. A single tap on the saved board intentionally
                        // does nothing, so the double-tap timeout doesn't
                        // delay any action (there is no single-tap action).
                        // v7.24 — the callback now reports the tile's
                        // VIEWPORT position (x, y in this Box's space), so
                        // the overlay can glide the image from its resting
                        // spot on the collage to the viewport center.
                        Modifier.pointerInput(tile.uri) {
                            detectTapGestures(
                                onDoubleTap = {
                                    onTileZoom(
                                        tile.uri,
                                        tile.offsetXPx,
                                        tile.offsetYPx,
                                        tile.widthPx,
                                        tile.heightPx,
                                        canvasWPx,
                                        canvasHPx
                                    )
                                }
                            )
                        }
                    } else Modifier
                )
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 0.dp,
                modifier = Modifier
                    .size(
                        width = with(density) { tile.widthPx.toDp() },
                        height = with(density) { tile.heightPx.toDp() }
                    )
                    .rotate(tile.rotationDeg)
            ) {
                Image(
                    painter = moodBoardPainter(tile.uri, zoomed),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        // v6.1 — no inner padding: tiles are sized to the
                        // photo's own aspect ratio, so the image fills the
                        // rounded box edge-to-edge (no white frame).
                        .clip(RoundedCornerShape(18.dp))
                )
            }
        }
    }
}

/**
 * In-place zoom overlay for ONE magnified tile (v7.24). Drop it as the LAST
 * child of the board's Box scope, sized to the viewport: the tapped image
 * GLIDES from its resting spot on the collage to the CENTER of the viewport
 * along an ARC (perpendicular bow, sin(π·t) — the same swoop the old
 * whole-board zoom used), scaling up as it travels. The board around it
 * stays completely still. Once landed, pinch-to-zoom (up to 8x) and
 * one-finger pan refine the image; a tap or the dismiss button closes it
 * (fast tween back to the resting spot, no laggy spring tail).
 *
 * [tileX]/[tileY] are the tile's top-left in VIEWPORT pixels (board offset
 * already applied), [widthPx]/[heightPx] its size, [viewW]/[viewH] the
 * viewport the image glides within.
 */
@Composable
fun MoodBoardZoomOverlay(
    zoomState: MoodBoardZoomState,
    tileUri: String,
    tileX: Float,
    tileY: Float,
    widthPx: Float,
    heightPx: Float,
    viewW: Float,
    viewH: Float,
    modifier: Modifier = Modifier
) {
    if (zoomState.zoomedUri == null) return
    val density = LocalDensity.current

    // ── Arc glide clock — ONE shared clock drives scale + pan in phase so
    // the image swoops from its resting spot to the centered target, and
    // back on close. Plain float states (the arc writes them per-frame).
    var glideScale by remember { mutableFloatStateOf(1f) }
    var glideX by remember { mutableFloatStateOf(tileX) }
    var glideY by remember { mutableFloatStateOf(tileY) }
    val glideProgress = remember { Animatable(0f) }

    // ── Pinch / pan refinement — applied ON TOP of the glide (neutral at
    // rest; per-gesture deltas are consumed so no tap fires after a drag).
    var pinchScale by remember { mutableFloatStateOf(1f) }
    var pinchX by remember { mutableFloatStateOf(0f) }
    var pinchY by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(
        zoomState.scaleTarget, zoomState.offsetX, zoomState.offsetY,
        zoomState.closing
    ) {
        // Resting spot: the tile's own position at scale 1. Target: the
        // image centered at the fit zoom ([offsetX]/[offsetY] are the
        // translation that centers it). The arc bows perpendicular to the
        // travel direction, peaking mid-flight, straight on close.
        val fromScale = glideScale
        val fromX = glideX
        val fromY = glideY
        val toScale = zoomState.scaleTarget
        val toX = zoomState.offsetX
        val toY = zoomState.offsetY
        val dx = toX - fromX
        val dy = toY - fromY
        val len = sqrt(dx * dx + dy * dy)
        val perpX = if (len > 0.5f) dy / len else 0f
        val perpY = if (len > 0.5f) -dx / len else 0f
        val arcPeak = if (zoomState.closing) 0f else (len * 0.16f).coerceIn(32f, 150f)
        glideProgress.snapTo(0f)
        glideProgress.animateTo(1f, moodBoardZoomSpec(zoomState.closing)) {
            val t = this.value
            // Clamp the BULGE progress: a spring overshoots past 1.0, and
            // sin(π·t) goes negative there — unclamped it would flip the
            // arc the other way at the very end. The lerp stays unclamped
            // so the spring's natural settle still lands softly.
            val arcT = t.coerceIn(0f, 1f)
            val bulge = arcPeak * sin(PI * arcT).toFloat()
            glideScale = lerp(fromScale, toScale, t)
            glideX = lerp(fromX, toX, t) + perpX * bulge
            glideY = lerp(fromY, toY, t) + perpY * bulge
        }
        // The glide (open or close) finished — reset the pinch so the next
        // open starts neutral.
        pinchScale = 1f
        pinchX = 0f
        pinchY = 0f
    }

    // Remove the overlay once the close animation settles back at 1x.
    LaunchedEffect(glideScale) {
        if (zoomState.closing && zoomState.scaleTarget <= 1.01f && glideScale <= 1.01f) {
            zoomState.zoomedUri = null
            zoomState.closing = false
        }
    }

    // ONE box owns the whole overlay and ALL of its gestures: a tap closes,
    // while a drag (one finger) pans and a pinch zooms the image. The two are
    // disambiguated in a single [awaitEachGesture] loop — a clean tap (no
    // second pointer, no slop-crossing movement) closes; anything else is a
    // pan/zoom applied to [pinchScale]/[pinchX]/[pinchY] ON TOP of the glide.
    // One detector means a pan/zoom can never fall through to the tap-close
    // handler (two separate detectors would let the parent's tap fire on
    // release after every drag/pinch).
    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(1000f)
            .pointerInput(tileUri) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    var isTap = true
                    var gestureZoom = 1f
                    var gesturePan = Offset.Zero
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.changes.size > 1) isTap = false
                        val zoomChange = event.calculateZoom()
                        val panChange = event.calculatePan()
                        if (zoomChange != 1f || panChange != Offset.Zero) {
                            isTap = false
                            gestureZoom *= zoomChange
                            gesturePan += panChange
                        }
                        event.changes.forEach { it.consume() }
                        if (event.changes.none { it.pressed }) break
                    }
                    if (isTap) {
                        zoomState.zoomOut()
                    } else {
                        pinchScale = (pinchScale * gestureZoom).coerceIn(1f, 8f)
                        pinchX += gesturePan.x
                        pinchY += gesturePan.y
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // The zoomed image — glides from its board spot to center, then
        // follows the user's pinch/pan. The whole transform lives in the
        // graphicsLayer (no layout offset): translationX/Y directly ARE the
        // image's top-left in viewport pixels, so at scale 1 with the
        // resting (tileX, tileY) it sits exactly on its tile.
        Box(
            modifier = Modifier.graphicsLayer {
                // The image scales around its own center, so a
                // translation of (tileX, tileY) at scale 1 puts it
                // exactly on its resting tile.
                scaleX = glideScale * pinchScale
                scaleY = glideScale * pinchScale
                translationX = glideX + pinchX
                translationY = glideY + pinchY
            }
        ) {
            val imageModifier = Modifier
                .size(
                    width = with(density) { widthPx.toDp() },
                    height = with(density) { heightPx.toDp() }
                )
                .clip(RoundedCornerShape(14.dp))
            // Frameless, like the editor tiles. The board-size painter renders
            // instantly (already cached from the collage) while the hi-res
            // decode for the magnifier streams in — no blank flash mid-glide.
            Image(
                painter = moodBoardPainter(tileUri),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = imageModifier
            )
            Image(
                painter = moodBoardPainter(tileUri, zoomed = true),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = imageModifier
            )
        }

        // ── Dismiss — closes the zoom ──────────────────────────────────
        Surface(
            onClick = { zoomState.zoomOut() },
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            shadowElevation = 0.dp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                CurioIcon(
                    name = CurioIcons.Close,
                    contentDescription = "Close zoom",
                    tint = MaterialTheme.colorScheme.onSurface,
                    size = 18.dp
                )
            }
        }
    }
}

/** One deterministic floating-card slot on a [boardW]×[boardH] board. */
private data class MoodQuoteSlot(val x: Float, val y: Float, val w: Float, val h: Float)

private fun moodBoardQuoteSlot(index: Int, boardW: Float, boardH: Float): MoodQuoteSlot {
    val cols = 2
    val col = index % cols
    val row = index / cols
    val slotW = boardW * 0.5f
    val cardW = (slotW * 0.82f).coerceIn(120f, 240f)
    val cardH = cardW * 0.62f
    val x = col * slotW + (slotW - cardW) / 2f
    val y = boardH * 0.56f + row * (cardH * 1.02f) + (if (col == 1) cardH * 0.45f else 0f)
    return MoodQuoteSlot(x, y, cardW, cardH)
}

/**
 * v7.20 — the shared floating quote-card layer for mood boards.
 *
 * Renders the entry's paper quote boxes floating ON the collage at their
 * saved positions (editor board-pixel space, mapped into the current board
 * by [boardScale] — and shifted by [offsetX]/[offsetY] when the board is
 * centered inside a larger viewport, e.g. the inline card's center crop or
 * the expanded dialog's contain-fit). Cards never dragged (-1,-1) fall back
 * to the deterministic slot for their index, so legacy entries and freshly
 * added cards land exactly where the editor's "Quote" chip shows them.
 *
 * The EDITOR passes [onMoveCard] to make the cards draggable (committed to
 * the entry's [CaptureData.QuotePos] list); saved views pass null so the
 * cards are read-only there.
 */
@Composable
fun MoodBoardFloatingCards(
    quotes: List<String>,
    styles: List<NotePaperStyle>,
    colors: List<NotePaperColor>,
    tilts: List<Float>,
    positions: List<CaptureData.QuotePos>,
    canvasWPx: Float,
    canvasHPx: Float,
    boardScale: Float = 1f,
    offsetX: Float = 0f,
    offsetY: Float = 0f,
    onEditCard: ((Int) -> Unit)? = null,
    onMoveCard: ((index: Int, x: Float, y: Float) -> Unit)? = null,
    // v7.22 — parallel per-card flag: false = the card renders BELOW the
    // board, so it must NOT float on the collage. Legacy entries lack the
    // list → null → every card floats (the v7.19 look).
    onBoard: List<Boolean>? = null
) {
    // Guard the pre-measure first frame (canvas = 0) and degenerate scales
    // so cards never stack at the top-left or divide by zero.
    if (canvasWPx <= 0f || canvasHPx <= 0f) return
    val scale = if (boardScale > 0f) boardScale else 1f
    quotes.forEachIndexed { i, quote ->
        // Skip below-board cards — they render under the board in their own
        // section, not on the collage. Missing flags (legacy) → on-board.
        if (onBoard != null && onBoard.getOrElse(i) { true } == false) return@forEachIndexed
        val slot = moodBoardQuoteSlot(i, canvasWPx, canvasHPx)
        val saved = positions.getOrElse(i) { CaptureData.QuotePos(-1f, -1f) }
        val placed = if (saved.x >= 0f && saved.y >= 0f) saved
            else CaptureData.QuotePos(slot.x, slot.y)
        MoodBoardFloatingCard(
            text = quote,
            style = styles.getOrElse(i) { NotePaperStyle.RULED },
            color = colors.getOrElse(i) { NotePaperColor.CREAM },
            rotation = tilts.getOrElse(i) { (i * 4.2f % 8f) - 4f },
            x = placed.x * scale + offsetX,
            y = placed.y * scale + offsetY,
            w = slot.w * scale,
            h = slot.h * scale,
            boardW = canvasWPx * scale,
            boardH = canvasHPx * scale,
            onEdit = { onEditCard?.invoke(i) },
            onMove = onMoveCard?.let { move ->
                { rx, ry -> move(i, (rx - offsetX) / scale, (ry - offsetY) / scale) }
            }
        )
    }
}

/**
 * One floating paper quote card — tilt + paper look, draggable in the
 * editor, read-only (tap → no-op) in saved views. Drag preview lives INSIDE
 * the card (same scoping trick as the editor tiles) so per-frame drags
 * recompose only this card. Released positions are reported in RENDER
 * space, clamped to the board bounds.
 */
@Composable
private fun MoodBoardFloatingCard(
    text: String,
    style: NotePaperStyle,
    color: NotePaperColor,
    rotation: Float,
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    boardW: Float,
    boardH: Float,
    onEdit: (() -> Unit)? = null,
    onMove: ((Float, Float) -> Unit)? = null
) {
    val density = LocalDensity.current
    var dragDelta by remember { mutableStateOf(Offset.Zero) }
    var dragging by remember { mutableStateOf(false) }
    // pointerInput never restarts, so the gesture coroutine must read the
    // LATEST geometry/callbacks — never the first composition's.
    val currentX by rememberUpdatedState(x)
    val currentY by rememberUpdatedState(y)
    val currentW by rememberUpdatedState(w)
    val currentH by rememberUpdatedState(h)
    val currentBoardW by rememberUpdatedState(boardW)
    val currentBoardH by rememberUpdatedState(boardH)
    val currentOnEdit by rememberUpdatedState(onEdit)
    val currentOnMove by rememberUpdatedState(onMove)

    val renderX = (x + dragDelta.x).coerceIn(0f, (boardW - w).coerceAtLeast(0f))
    val renderY = (y + dragDelta.y).coerceIn(0f, (boardH - h).coerceAtLeast(0f))

    Box(
        modifier = Modifier
            .offset { IntOffset(renderX.roundToInt(), renderY.roundToInt()) }
            .zIndex(if (dragging) 55f else 50f)
            .size(
                width = with(density) { w.toDp() },
                height = with(density) { h.toDp() }
            )
            .rotate(rotation)
            .then(
                if (currentOnMove != null) Modifier.pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { dragging = true },
                        onDragEnd = {
                            dragging = false
                            // Compute the commit from LIVE state inside the
                            // coroutine: pointerInput(Unit) never restarts, so
                            // composition-captured renderX/renderY would be the
                            // FIRST frame's (pre-drag) values and every drop
                            // would snap the card back. dragDelta is snapshot-
                            // state-backed, so reading it here is current.
                            val commitX = (currentX + dragDelta.x)
                                .coerceIn(0f, (currentBoardW - currentW).coerceAtLeast(0f))
                            val commitY = (currentY + dragDelta.y)
                                .coerceIn(0f, (currentBoardH - currentH).coerceAtLeast(0f))
                            currentOnMove?.invoke(commitX, commitY)
                            // CRITICAL: the commit stores the position as the
                            // card's new x/y, so the visual delta must be
                            // cleared — otherwise the card renders at
                            // stored+delta (a double-offset SNAP) on the next
                            // frame and keeps accumulating on later drags.
                            dragDelta = Offset.Zero
                        },
                        onDragCancel = {
                            // No commit happens on cancel — clear the delta so
                            // the card settles back exactly on its stored
                            // position instead of floating mid-offset.
                            dragging = false
                            dragDelta = Offset.Zero
                        },
                        onDrag = { change, amount ->
                            change.consume()
                            // Clamp the ACCUMULATED delta so the card sticks
                            // at the edges (clamping only the visual position
                            // would let the delta run away and snap back).
                            val nx = (currentX + dragDelta.x + amount.x)
                                .coerceIn(0f, (currentBoardW - currentW).coerceAtLeast(0f))
                            val ny = (currentY + dragDelta.y + amount.y)
                                .coerceIn(0f, (currentBoardH - currentH).coerceAtLeast(0f))
                            dragDelta = Offset(nx - currentX, ny - currentY)
                        }
                    )
                } else Modifier
            )
            // Press-detection (drag) comes BEFORE click-consumption so a drag
            // never also triggers the edit tap. Saved views pass onEdit = null
            // → no clickable at all, so taps fall through to the board's own
            // handlers (e.g. the expanded dialog's tap-to-dismiss).
            .then(
                if (currentOnEdit != null) Modifier.clickable(onClick = { currentOnEdit?.invoke() })
                else Modifier
            )
    ) {
        NotePaperCard(
            style = style,
            paperColor = color,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = text.ifBlank { "Quote…" },
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = PatrickHandFontFamily),
                color = notePaperInk(color),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}