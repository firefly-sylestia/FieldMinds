package com.curio.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
 * Mutable zoom state for one mood-board canvas: either a single tile URI is
 * magnified ([zoomedUri]) or the WHOLE board is magnified ([boardZoomed]).
 * [scaleTarget] (1..4) and the pan offsets in pixels are the animation
 * targets; the animated values live at the overlays so zoom opens and
 * closes smoothly.
 *
 * v7.22 — one-shot zoom, no pinch/pan: double-tap magnifies (the board
 * glides to the tile, or a single tile springs up centered + straight) and
 * the magnified view STAYS at that fixed zoom until the user taps to close.
 *
 * [closing] is a latch set by [zoomOut]: the overlay only removes itself
 * once the close animation has settled.
 *
 * Tile/board zoom never navigates — the magnified content is rendered in
 * place by [MoodBoardZoomOverlay] (single tile, centered + straight) and
 * [MoodBoardZoomCanvas] (the whole collage) over the canvas.
 */
class MoodBoardZoomState {
    var zoomedUri by mutableStateOf<String?>(null)
    var boardZoomed by mutableStateOf(false)
    var scaleTarget by mutableFloatStateOf(1f)
    var offsetX by mutableFloatStateOf(0f)
    var offsetY by mutableFloatStateOf(0f)
    var closing by mutableStateOf(false)
    // The zoom level [zoomIn]/[zoomToTile] open at for the CURRENT tile —
    // fit-based so a small tile opens large enough to read while a big tile
    // doesn't blow past the screen.
    var defaultScale by mutableFloatStateOf(2.4f)

    /**
     * Double-tap a tile: spring it up, centered + straight. The target zoom
     * is fit-based — the tile is scaled to fill ~90% of the board viewport's
     * smaller dimension, clamped so a small tile really zooms in while a
     * near-full-board tile only lifts slightly (instead of exploding past
     * the screen at a flat 2.4x).
     */
    fun zoomIn(uri: String, tileW: Float = 0f, tileH: Float = 0f, viewW: Float = 0f, viewH: Float = 0f) {
        closing = false
        boardZoomed = false
        if (zoomedUri != uri) {
            zoomedUri = uri
            scaleTarget = 1f
            offsetX = 0f
            offsetY = 0f
        }
        defaultScale = fitZoomScale(tileW, tileH, viewW, viewH)
        scaleTarget = defaultScale
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
     * Double-tap a tile (v7.19): the WHOLE board — background included —
     * magnifies toward the tapped tile, centering it, and the tile's image
     * pops up over the magnified collage. [centerX]/[centerY] are the
     * tile's center in VIEWPORT coordinates (board offset already applied);
     * the target offset places that point at the viewport center at the
     * fit-based zoom level, so the screen visibly glides to the tile.
     */
    fun zoomToTile(
        uri: String,
        centerX: Float,
        centerY: Float,
        tileW: Float,
        tileH: Float,
        viewW: Float,
        viewH: Float
    ) {
        closing = false
        zoomedUri = uri
        boardZoomed = true
        defaultScale = fitZoomScale(tileW, tileH, viewW, viewH)
        scaleTarget = defaultScale
        // The magnified layer scales around its CENTER, so the on-screen
        // position of a content point p is (p - c)·s + c + t. Solving for t
        // so the tapped tile's center lands at the viewport center c:
        //   t = s · (c - p)
        offsetX = scaleTarget * (viewW / 2f - centerX)
        offsetY = scaleTarget * (viewH / 2f - centerY)
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
    onTileZoom: ((String, Float, Float, Float, Float) -> Unit)? = null,
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
                        Modifier.pointerInput(tile.uri) {
                            detectTapGestures(
                                onDoubleTap = { onTileZoom(tile.uri, tile.widthPx, tile.heightPx, canvasWPx, canvasHPx) }
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
 * In-place zoom overlay for ONE magnified tile. Drop it as the LAST child of
 * the board's Box scope: it springs the tapped/pinched image up CENTERED and
 * STRAIGHT (no rotation, no offset) — no navigation, no separate page. Tap
 * anywhere closes it; pinch/pan refine the zoom up to 8x; double-tap the
 * image resets to its fit-based default zoom; double-tap the board around it
 * closes.
 *
 * v7.19 — scale AND pan are animated INTERNALLY (the call-site springs are
 * gone), and closing uses a fast tween instead of a long spring tail, so
 * the minimize animation snaps shut instead of lagging/delaying.
 */
@Composable
fun MoodBoardZoomOverlay(
    zoomState: MoodBoardZoomState,
    tileUri: String,
    widthPx: Float,
    heightPx: Float,
    modifier: Modifier = Modifier
) {
    if (zoomState.zoomedUri == null) return
    val density = LocalDensity.current

    // v7.22 — one-shot zoom, no pinch/pan. Animate scale + pan from the
    // current value toward the targets — open springs to the fit zoom,
    // close uses [moodBoardZoomSpec]'s quick tween so the shrink feels
    // immediate, not delayed.
    val overlayScale = remember { Animatable(1f) }
    val panX = remember { Animatable(0f) }
    val panY = remember { Animatable(0f) }
    LaunchedEffect(zoomState.scaleTarget) {
        overlayScale.animateTo(zoomState.scaleTarget, moodBoardZoomSpec(zoomState.closing))
    }
    LaunchedEffect(zoomState.offsetX) {
        panX.animateTo(zoomState.offsetX, moodBoardZoomSpec(zoomState.closing))
    }
    LaunchedEffect(zoomState.offsetY) {
        panY.animateTo(zoomState.offsetY, moodBoardZoomSpec(zoomState.closing))
    }

    // Remove the overlay once the close animation settles back at 1x.
    LaunchedEffect(overlayScale.value) {
        if (zoomState.closing && zoomState.scaleTarget <= 1.01f && overlayScale.value <= 1.01f) {
            zoomState.zoomedUri = null
            zoomState.closing = false
        }
    }

    // ONE box owns the whole overlay: tap-to-close + the image as a CHILD,
    // so every pointer event reaches the same handler. No dark scrim; a
    // dismiss button sits at the top while the image is zoomed.
    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(1000f)
            .pointerInput(tileUri) {
                detectTapGestures(onTap = { zoomState.zoomOut() })
            },
        contentAlignment = Alignment.Center
    ) {
        // The zoomed image — centered on the canvas and upright, animated.
        Box(
            modifier = Modifier.graphicsLayer {
                scaleX = overlayScale.value
                scaleY = overlayScale.value
                translationX = panX.value
                translationY = panY.value
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
            // decode for the magnifier streams in — no blank flash mid-spring.
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

/**
 * Whole-board magnifier overlay — the entire board (background + collage)
 * magnifies. Two-finger pinch on the board itself springs it up centered;
 * double-tapping a tile ([MoodBoardZoomState.zoomToTile]) makes the whole
 * screen — background included — glide to that tile and center it, then the
 * tile's image pops up over the magnified collage. Pinch inside zooms
 * further up to 8x and drag pans; tap or double-tap (or pinch back to 1x)
 * closes it.
 *
 * v7.19 — scale + pan animate INTERNALLY (call-site springs removed) and
 * closing uses a fast tween so the minimize snaps shut instead of lagging.
 * [backdrop] renders INSIDE the magnified layer, so the background really
 * magnifies with the board instead of staying static behind it.
 */
@Composable
fun MoodBoardZoomCanvas(
    zoomState: MoodBoardZoomState,
    tiles: List<CaptureData.TileLayout>,
    canvasWPx: Float,
    canvasHPx: Float,
    modifier: Modifier = Modifier,
    backdrop: @Composable BoxScope.() -> Unit = {}
) {
    if (!zoomState.boardZoomed) return
    val density = LocalDensity.current

    // v7.22 — internal scale + pan animation runs on ONE shared clock so
    // scale and pan land in phase, and the OPEN glide follows an ARC: the
    // pan path bows perpendicular to the travel direction (a sin(π·t) hump,
    // 0 at both ends) instead of a dead-straight line, so the board swoops
    // to the tile. One-shot zoom — no pinch: the magnified view stays at
    // the fixed zoom until tapped, and closing runs a fast straight tween
    // so the minimize feels immediate.
    // Plain float states (not Animatables) for scale + pan: the arc writes
    // them per-frame, and Animatable.value isn't publicly writable from app
    // code — only snapTo()/animateTo() are. glideProgress stays an
    // Animatable because it's the shared clock (driven by animateTo, read
    // via .value).
    var overlayScale by remember { mutableFloatStateOf(1f) }
    var panX by remember { mutableFloatStateOf(0f) }
    var panY by remember { mutableFloatStateOf(0f) }
    val glideProgress = remember { Animatable(0f) }
    LaunchedEffect(
        zoomState.scaleTarget, zoomState.offsetX, zoomState.offsetY,
        zoomState.closing
    ) {
        val fromScale = overlayScale
        val fromX = panX
        val fromY = panY
        val toScale = zoomState.scaleTarget
        val toX = zoomState.offsetX
        val toY = zoomState.offsetY
        // Arc geometry: bow perpendicular to the travel direction, peaking
        // mid-flight (sin(π·t) is 0 at both ends). Scaled to the glide
        // distance but capped so short hops don't over-swoop.
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
            // arc the other way at the very end. The lerp below stays
            // unclamped so the spring's natural settle still lands softly.
            val arcT = t.coerceIn(0f, 1f)
            val bulge = arcPeak * sin(PI * arcT).toFloat()
            overlayScale = lerp(fromScale, toScale, t)
            panX = lerp(fromX, toX, t) + perpX * bulge
            panY = lerp(fromY, toY, t) + perpY * bulge
        }
    }

    // Remove once the close animation settles back at 1x.
    LaunchedEffect(overlayScale) {
        if (zoomState.closing && zoomState.scaleTarget <= 1.01f && overlayScale <= 1.01f) {
            zoomState.boardZoomed = false
            zoomState.closing = false
        }
    }

    // ONE box owns the whole overlay: tap-to-close + the collage as a
    // CHILD. No dark scrim; a dismiss button sits at the top.
    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(1000f)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { zoomState.zoomOut() },
                    onDoubleTap = { zoomState.zoomOut() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // The whole magnified layer — backdrop + collage, scaled and panned
        // together so the SCREEN (not just the images) glides to the tile.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = overlayScale
                    scaleY = overlayScale
                    translationX = panX
                    translationY = panY
                }
        ) {
            // The board's own watermark backdrop — inside the transformed
            // layer, so it magnifies with the collage (identical pattern to
            // the resting card at scale 1: both fill the viewport).
            backdrop()

            // The collage + focus pop share ONE centered box, so the pop's
            // board-space offsets stay aligned with the collage inside the
            // (viewport-sized) magnified layer.
            Box(modifier = Modifier.align(Alignment.Center)) {
                // The whole collage — centered, straight, animated.
                Box(
                    modifier = Modifier.size(
                        width = with(density) { canvasWPx.toDp() },
                        height = with(density) { canvasHPx.toDp() }
                    )
                ) {
                    MoodBoardTiles(
                        tiles = tiles,
                        canvasWPx = canvasWPx,
                        canvasHPx = canvasHPx,
                        zoomed = true
                    )
                }

                // ── Focus-tile pop (v7.19) — after the board glides to the
                // tapped tile, its hi-res image pops up over the magnified
                // collage: a bouncy over-scale + soft shadow, at the tile's
                // own board position (which the layer transform places
                // exactly where the collage shows it).
                val focusUri = zoomState.zoomedUri
                if (focusUri != null) {
                    tiles.firstOrNull { it.uri == focusUri }?.let { focus ->
                        val popScale = remember(focus.uri) { Animatable(1f) }
                        LaunchedEffect(focus.uri) {
                            popScale.snapTo(1f)
                            popScale.animateTo(1.14f, spring(dampingRatio = 0.55f, stiffness = 420f))
                        }
                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        focus.offsetXPx.roundToInt(),
                                        focus.offsetYPx.roundToInt()
                                    )
                                }
                                .zIndex(600f)
                        ) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 14.dp,
                            modifier = Modifier
                                .size(
                                    width = with(density) { focus.widthPx.toDp() },
                                    height = with(density) { focus.heightPx.toDp() }
                                )
                                .graphicsLayer {
                                    scaleX = popScale.value
                                    scaleY = popScale.value
                                }
                        ) {
                            Image(
                                painter = moodBoardPainter(focus.uri, zoomed = true),
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(18.dp))
                            )
                        }
                    }
                }
                }
            }
        }

        // ── Dismiss — closes the board zoom ────────────────────────────
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