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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import kotlin.math.roundToInt

/**
 * Mutable zoom state for one mood-board canvas: which tile URI is currently
 * magnified ([zoomedUri], null = none), the target [scaleTarget] (1..4) and
 * the pan offsets in pixels. Raw targets are updated by gestures; the
 * spring-animated values live at the board call site (via
 * [animateFloatAsState]) so tap-to-zoom opens and closes smoothly.
 *
 * [closing] is a latch set by [zoomOut]: the overlay only removes itself
 * once the close spring has settled, so a fresh pinch that starts at zoom
 * 1.0 never pops the overlay open and then instantly closes it.
 *
 * Tiles stay in place — the zoomed image is rendered by
 * [MoodBoardZoomOverlay] at the tile's own board position, so zooming never
 * navigates to a new page.
 */
class MoodBoardZoomState {
    var zoomedUri by mutableStateOf<String?>(null)
    var scaleTarget by mutableFloatStateOf(1f)
    var offsetX by mutableFloatStateOf(0f)
    var offsetY by mutableFloatStateOf(0f)
    var closing by mutableStateOf(false)

    val isZoomed: Boolean get() = zoomedUri != null

    /** Tap-to-zoom: spring the tile up to ~2.4x, centered. */
    fun zoomIn(uri: String) {
        closing = false
        if (zoomedUri != uri) {
            zoomedUri = uri
            scaleTarget = 1f
            offsetX = 0f
            offsetY = 0f
        }
        scaleTarget = 2.4f
    }

    /** Zoom back out to 1x; the overlay clears itself once the spring settles. */
    fun zoomOut() {
        closing = true
        scaleTarget = 1f
        offsetX = 0f
        offsetY = 0f
    }

    /** Pinch/pan update — clamps scale so the image can't vanish or explode. */
    fun applyPinch(uri: String, pan: Offset, zoom: Float) {
        closing = false
        if (zoomedUri != uri) {
            zoomedUri = uri
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

/**
 * Two-finger pinch modifier for a mood-board tile. Unlike
 * [detectTransformGestures], single-finger drags are left unconsumed so the
 * page keeps scrolling — the pinch only engages once a second finger lands.
 * Per-event zoom/pan deltas flow into [MoodBoardZoomState.applyPinch] so the
 * overlay can take over the image from its own board position.
 */
fun Modifier.moodBoardPinch(zoomState: MoodBoardZoomState, uri: String): Modifier =
    this.pointerInput(uri) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false)
            var engaged = false
            do {
                val event = awaitPointerEvent()
                val pressed = event.changes.filter { it.pressed }
                if (!engaged && pressed.size >= 2) {
                    engaged = true
                }
                if (engaged && pressed.size >= 2) {
                    zoomState.applyPinch(
                        uri = uri,
                        pan = event.calculatePan(),
                        zoom = event.calculateZoom()
                    )
                }
                if (pressed.isEmpty()) break
                if (pressed.size >= 2) {
                    event.changes.forEach { if (it.positionChanged()) it.consume() }
                }
            } while (true)
        }
    }

/**
 * In-place zoom overlay for one mood-board tile. Drop it as the LAST child of
 * the board's Box scope: it darkens the rest of the board and springs the
 * tapped/pinched image up from its own board position — no navigation, no
 * separate page. Tap anywhere (or pinch back out to 1x) closes it.
 *
 * [animatedScale] / [animatedOffsetX] / [animatedOffsetY] must come from
 * `animateFloatAsState` at the board level so the spring interpolates from
 * 1x on open and back on close.
 *
 * [offsetXPx]…[rotationDeg] are the tile's geometry IN THE OVERLAY'S
 * coordinate space (pass the raw values, or scaled values for the expanded
 * dialog which scales the whole board).
 */
@Composable
fun MoodBoardZoomOverlay(
    zoomState: MoodBoardZoomState,
    animatedScale: Float,
    animatedOffsetX: Float,
    animatedOffsetY: Float,
    tileUri: String,
    offsetXPx: Float,
    offsetYPx: Float,
    widthPx: Float,
    heightPx: Float,
    rotationDeg: Float,
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

    // The zoomed image, in place at the tile's board position.
    Box(
        modifier = modifier
            .offset { IntOffset(offsetXPx.roundToInt(), offsetYPx.roundToInt()) }
            .zIndex(1001f)
            .graphicsLayer {
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
            modifier = Modifier
                .size(
                    width = with(density) { widthPx.toDp() },
                    height = with(density) { heightPx.toDp() }
                )
                .rotate(rotationDeg)
        ) {
            Image(
                painter = rememberAsyncImagePainter(tileUri),
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
