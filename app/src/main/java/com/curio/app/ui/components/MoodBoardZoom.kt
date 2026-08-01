package com.curio.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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

    val isZoomed: Boolean get() = zoomedUri != null || boardZoomed

    /** Tap/double-tap a tile: spring it up to ~2.4x, centered + straight. */
    fun zoomIn(uri: String) {
        closing = false
        boardZoomed = false
        if (zoomedUri != uri) {
            zoomedUri = uri
            scaleTarget = 1f
            offsetX = 0f
            offsetY = 0f
        }
        scaleTarget = 2.4f
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
        scaleTarget = (scaleTarget * zoom).coerceIn(1f, 4f)
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
 * When [onTileZoom] is provided each tile responds to tap / double-tap.
 */
@Composable
fun MoodBoardTiles(
    tiles: List<CaptureData.TileLayout>,
    canvasWPx: Float,
    canvasHPx: Float,
    onTileZoom: ((String) -> Unit)? = null,
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
                        Modifier.pointerInput(tile.uri) {
                            detectTapGestures(
                                onTap = { onTileZoom(tile.uri) },
                                onDoubleTap = { onTileZoom(tile.uri) }
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
                        .padding(6.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
            }
        }
    }
}

/**
 * In-place zoom overlay for ONE magnified tile. Drop it as the LAST child of
 * the board's Box scope: it darkens the rest of the board and springs the
 * tapped/pinched image up CENTERED and STRAIGHT (no rotation, no offset) —
 * no navigation, no separate page. Tap anywhere closes it; pinch/pan refine
 * the zoom up to 4x.
 *
 * [animatedScale] / [animatedOffsetX] / [animatedOffsetY] must come from
 * `animateFloatAsState` at the board level so the spring interpolates from
 * 1x on open and back on close.
 */
@Composable
fun MoodBoardZoomOverlay(
    zoomState: MoodBoardZoomState,
    animatedScale: Float,
    animatedOffsetX: Float,
    animatedOffsetY: Float,
    tileUri: String,
    widthPx: Float,
    heightPx: Float,
    modifier: Modifier = Modifier
) {
    if (zoomState.zoomedUri == null) return
    val density = LocalDensity.current

    // Remove the overlay once the close spring settles back at 1x. The
    // `closing` latch prevents a fresh pinch (which starts at zoom 1.0)
    // from popping the overlay open and instantly closing it.
    LaunchedEffect(animatedScale) {
        if (zoomState.closing && zoomState.scaleTarget <= 1.01f && animatedScale <= 1.01f) {
            zoomState.zoomedUri = null
            zoomState.closing = false
        }
    }

    // Scrim + gestures: tap closes, pinch/pan refines the zoom. The image
    // layer above has no pointer handlers, so events pass through to here.
    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(1000f)
            .background(Color.Black.copy(alpha = 0.55f))
            .pointerInput(tileUri) {
                detectTransformGestures { _, pan, zoom, _ ->
                    zoomState.applyPinch(tileUri, pan, zoom)
                }
            }
            .pointerInput(tileUri) {
                detectTapGestures(onTap = { zoomState.zoomOut() })
            }
    )

    // The zoomed image — centered on the canvas and upright, spring-scaled.
    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(1001f),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
                translationX = animatedOffsetX
                translationY = animatedOffsetY
            }
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 0.dp,
                modifier = Modifier.size(
                    width = with(density) { widthPx.toDp() },
                    height = with(density) { heightPx.toDp() }
                )
            ) {
                Image(
                    painter = moodBoardPainter(tileUri, zoomed = true),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
            }
        }
    }
}

/**
 * Whole-board magnifier overlay — two-finger pinch on the mood board itself
 * (not just the images) springs the entire collage up, centered and straight.
 * Pinch inside it zooms further up to 4x and drag pans; tap (or pinch back
 * to 1x) closes it.
 */
@Composable
fun MoodBoardZoomCanvas(
    zoomState: MoodBoardZoomState,
    animatedScale: Float,
    animatedOffsetX: Float,
    animatedOffsetY: Float,
    tiles: List<CaptureData.TileLayout>,
    canvasWPx: Float,
    canvasHPx: Float,
    modifier: Modifier = Modifier
) {
    if (!zoomState.boardZoomed) return
    val density = LocalDensity.current

    // Auto-close when the user pinches the board back down to 1x.
    LaunchedEffect(zoomState.scaleTarget) {
        if (zoomState.boardZoomed && !zoomState.closing && zoomState.scaleTarget <= 1.01f) {
            zoomState.boardZoomed = false
        }
    }
    // Remove once the close spring settles back at 1x.
    LaunchedEffect(animatedScale) {
        if (zoomState.closing && zoomState.scaleTarget <= 1.01f && animatedScale <= 1.01f) {
            zoomState.boardZoomed = false
            zoomState.closing = false
        }
    }

    // Scrim + gestures: tap closes, pinch/pan refines the board zoom.
    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(1000f)
            .background(Color.Black.copy(alpha = 0.55f))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    zoomState.applyPinch(null, pan, zoom)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(onTap = { zoomState.zoomOut() })
            }
    )

    // The whole collage — centered, straight, spring-scaled.
    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(1001f),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
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
    }
}
