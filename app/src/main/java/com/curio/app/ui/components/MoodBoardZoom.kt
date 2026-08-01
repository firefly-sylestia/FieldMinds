package com.curio.app.ui.components

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.curio.app.data.CaptureData
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import kotlin.math.roundToInt

/**
 * Mutable zoom state for one mood-board canvas: either a single tile URI is
 * magnified ([zoomedUri]) or the WHOLE board is magnified ([boardZoomed]).
 * [scaleTarget] (1..4) and the pan offsets in pixels are raw gesture
 * targets; the spring-animated values live at the board call site (via
 * [animateFloatAsState]) so zoom opens and closes smoothly.
 *
 * [closing] is a latch set by [zoomOut]: the overlay only removes itself
 * once the close spring has settled, so a fresh pinch that starts at zoom
 * 1.0 never pops the overlay open and then instantly closes it.
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
    // The zoom level [zoomIn] opens at for the CURRENT tile — fit-based so a
    // small tile opens large enough to read while a big tile doesn't blow
    // past the screen. [resetZoom] springs back to this.
    var defaultScale by mutableFloatStateOf(2.4f)

    val isZoomed: Boolean get() = zoomedUri != null || boardZoomed

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

    /** Two-finger pinch on the board itself: magnify the whole collage. */
    fun zoomBoard() {
        closing = false
        zoomedUri = null
        boardZoomed = true
        scaleTarget = 2.4f
        offsetX = 0f
        offsetY = 0f
    }

    /** Zoom back out to 1x; the overlay clears itself once the spring settles. */
    fun zoomOut() {
        closing = true
        scaleTarget = 1f
        offsetX = 0f
        offsetY = 0f
    }

    /**
     * Double-tap the zoomed image: spring back to the tile's fit-based
     * [defaultScale], centered + straight — a quick "reset view" while
     * staying zoomed. Unlike [zoomIn] (which opens a NEW tile), this never
     * switches the target.
     */
    fun resetZoom() {
        closing = false
        scaleTarget = defaultScale
        offsetX = 0f
        offsetY = 0f
    }

    /**
     * Pinch/pan update — [uri] is the tile being refined (null = the whole
     * board). Clamps scale so the content can't vanish or explode.
     */
    fun applyPinch(uri: String?, pan: Offset, zoom: Float) {
        closing = false
        if (uri != null && zoomedUri != uri) {
            zoomedUri = uri
            boardZoomed = false
            scaleTarget = 1f
            offsetX = 0f
            offsetY = 0f
        }
        scaleTarget = (scaleTarget * zoom).coerceIn(1f, 8f)
        offsetX = (offsetX + pan.x).coerceIn(-900f, 900f)
        offsetY = (offsetY + pan.y).coerceIn(-900f, 900f)
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
 * Two-finger pinch modifier for the mood-board CANVAS itself. Unlike
 * [detectTransformGestures], single-finger drags are left unconsumed so the
 * page keeps scrolling — the pinch only engages once a second finger lands.
 * On engage the whole board springs up ([MoodBoardZoomState.zoomBoard]);
 * per-event zoom/pan deltas flow into [MoodBoardZoomState.applyPinch].
 */
fun Modifier.moodBoardPinchZoom(zoomState: MoodBoardZoomState): Modifier =
    this.pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false)
            var engaged = false
            do {
                val event = awaitPointerEvent()
                val pressed = event.changes.filter { it.pressed }
                if (!engaged && pressed.size >= 2) {
                    engaged = true
                    zoomState.zoomBoard()
                }
                if (engaged && pressed.size >= 2) {
                    zoomState.applyPinch(
                        uri = null,
                        pan = event.calculatePan(),
                        zoom = event.calculateZoom()
                    )
                }
                if (pressed.isEmpty()) break
                if (pressed.size >= 2) {
                    // Take over the gesture from any parent scrollable while
                    // pinching — consume every change (consume() is guaranteed
                    // available; positionChanged() is not in this Compose BOM).
                    event.changes.forEach { it.consume() }
                }
            } while (true)
        }
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
 * [animatedOffsetX] / [animatedOffsetY] come from `animateFloatAsState` at
 * the board level; the SCALE is animated internally from 1x so the overlay
 * springs up on open instead of popping in at the target zoom.
 */
@Composable
fun MoodBoardZoomOverlay(
    zoomState: MoodBoardZoomState,
    animatedOffsetX: Float,
    animatedOffsetY: Float,
    tileUri: String,
    widthPx: Float,
    heightPx: Float,
    modifier: Modifier = Modifier
) {
    if (zoomState.zoomedUri == null) return
    val density = LocalDensity.current

    // The call-site springs initialize to their target on first composition,
    // so an overlay that composes already at 2.4x would POP in instead of
    // springing. Animate our own scale from the initial 1x toward the live
    // target so open AND close spring smoothly (pinch retargets mid-flight).
    var overlayScale by remember { mutableFloatStateOf(1f) }
    LaunchedEffect(zoomState.scaleTarget) {
        animate(
            initialValue = overlayScale,
            targetValue = zoomState.scaleTarget,
            animationSpec = spring(dampingRatio = 0.8f, stiffness = 280f)
        ) { value, _ -> overlayScale = value }
    }

    // Remove the overlay once the close spring settles back at 1x. The
    // `closing` latch prevents a fresh pinch (which starts at zoom 1.0)
    // from popping the overlay open and instantly closing it.
    LaunchedEffect(overlayScale) {
        if (zoomState.closing && zoomState.scaleTarget <= 1.01f && overlayScale <= 1.01f) {
            zoomState.zoomedUri = null
            zoomState.closing = false
        }
    }

    // Live animated values for the double-tap hit-test — the gesture
    // coroutine must read the CURRENT scale/pan without restarting on every
    // animation frame (keying pointerInput on them would cancel gestures).
    val liveScale by rememberUpdatedState(overlayScale)
    val liveOffsetX by rememberUpdatedState(animatedOffsetX)
    val liveOffsetY by rememberUpdatedState(animatedOffsetY)

    // ONE box owns the whole overlay: gestures + the image as a CHILD, so
    // every pointer event — on the image or around it — reaches the same
    // transform/tap handlers. No dark scrim; a dismiss button sits at the
    // top while the image is zoomed.
    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(1000f)
            .pointerInput(tileUri) {
                detectTransformGestures { _, pan, zoom, _ ->
                    zoomState.applyPinch(tileUri, pan, zoom)
                }
            }
            .pointerInput(tileUri) {
                detectTapGestures(
                    onTap = { zoomState.zoomOut() },
                    onDoubleTap = { tap ->
                        // Double-tap the zoomed image → spring back to the
                        // default 2.4x. Double-tap the board around it → close.
                        val halfW = widthPx / 2f * liveScale
                        val halfH = heightPx / 2f * liveScale
                        val cx = size.width / 2f + liveOffsetX
                        val cy = size.height / 2f + liveOffsetY
                        val onImage = tap.x in (cx - halfW)..(cx + halfW) &&
                            tap.y in (cy - halfH)..(cy + halfH)
                        if (onImage) zoomState.resetZoom() else zoomState.zoomOut()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // The zoomed image — centered on the canvas and upright, spring-scaled.
        Box(
            modifier = Modifier.graphicsLayer {
                scaleX = overlayScale
                scaleY = overlayScale
                translationX = animatedOffsetX
                translationY = animatedOffsetY
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
 * Whole-board magnifier overlay — two-finger pinch on the mood board itself
 * (not just the images) springs the entire collage up, centered and straight.
 * Pinch inside it zooms further up to 8x and drag pans; tap or double-tap
 * (or pinch back to 1x) closes it. The scale is animated internally from 1x
 * so the board springs up on open instead of popping in.
 */
@Composable
fun MoodBoardZoomCanvas(
    zoomState: MoodBoardZoomState,
    animatedOffsetX: Float,
    animatedOffsetY: Float,
    tiles: List<CaptureData.TileLayout>,
    canvasWPx: Float,
    canvasHPx: Float,
    modifier: Modifier = Modifier
) {
    if (!zoomState.boardZoomed) return
    val density = LocalDensity.current

    // Same in-place spring as the tile overlay: compose at 1x and animate
    // toward the live target so open and close both spring smoothly.
    var overlayScale by remember { mutableFloatStateOf(1f) }
    LaunchedEffect(zoomState.scaleTarget) {
        animate(
            initialValue = overlayScale,
            targetValue = zoomState.scaleTarget,
            animationSpec = spring(dampingRatio = 0.8f, stiffness = 280f)
        ) { value, _ -> overlayScale = value }
    }

    // Auto-close when the user pinches the board back down to 1x.
    LaunchedEffect(zoomState.scaleTarget) {
        if (zoomState.boardZoomed && !zoomState.closing && zoomState.scaleTarget <= 1.01f) {
            zoomState.boardZoomed = false
        }
    }
    // Remove once the close spring settles back at 1x.
    LaunchedEffect(overlayScale) {
        if (zoomState.closing && zoomState.scaleTarget <= 1.01f && overlayScale <= 1.01f) {
            zoomState.boardZoomed = false
            zoomState.closing = false
        }
    }

    // ONE box owns the whole overlay: gestures + the collage as a CHILD, so
    // a pinch anywhere over the board magnifier reaches the same transform
    // handler. No dark scrim; a dismiss button sits at the top.
    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(1000f)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    zoomState.applyPinch(null, pan, zoom)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { zoomState.zoomOut() },
                    onDoubleTap = { zoomState.zoomOut() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // The whole collage — centered, straight, spring-scaled.
        Box(
            modifier = Modifier.graphicsLayer {
                scaleX = overlayScale
                scaleY = overlayScale
                translationX = animatedOffsetX
                translationY = animatedOffsetY
            }
        ) {
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
