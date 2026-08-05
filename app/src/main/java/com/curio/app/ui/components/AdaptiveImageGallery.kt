package com.curio.app.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * v7.36 — adaptive, aspect-ratio-true gallery for saved-entry attachments.
 *
 * Instead of the old fixed square tiles, every image keeps its real shape
 * and packs into JUSTIFIED rows (Google-Photos style): a wide landscape
 * takes a full slot, a portrait a narrow one, so the user's example — one
 * horizontal + one vertical, then three verticals on the next line — falls
 * out naturally. Each row stretches to exactly fill the container width
 * (heights scale with it, so nothing distorts).
 *
 * Tapping an image zooms it IN PLACE over the page — the same no-dark-scrim
 * [MoodBoardZoomOverlay] the mood boards use: the image glides from its
 * gallery spot to the center (arc), pinch/pan refine, tap closes. No
 * Lightbox page.
 *
 * Aspect ratios are measured off the main thread with header-only decodes
 * (content URIs via ContentResolver, file paths directly), EXIF-rotated to
 * match Coil's rendering; images that can't be read fall back to square.
 */
@Composable
fun AdaptiveImageGallery(
    uris: List<String>,
    modifier: Modifier = Modifier,
    rowHeight: Dp = 152.dp,
    gap: Dp = 8.dp,
    cornerRadius: Dp = 16.dp
) {
    if (uris.isEmpty()) return
    val context = LocalContext.current
    // Aspect ratio (w/h) per uri — measured off-thread; empty until loaded,
    // so the first frame renders as square fallbacks and recomposes once the
    // real shapes arrive.
    val aspects by produceState<Map<String, Float>>(initialValue = emptyMap(), uris) {
        value = uris.associateWith { uri -> imageAspectOf(context, uri) }
    }
    val zoomState = rememberMoodBoardZoomState()
    BoxWithConstraints(
        // While zoomed the gallery (and its overlay) must draw ABOVE later
        // siblings in the detail column — the same zIndex trick the saved
        // mood board uses — so the gliding image never slides under the
        // audio bar or the next section.
        modifier = modifier.zIndex(if (zoomState.zoomedUri != null) 1000f else 0f)
    ) {
        val density = LocalDensity.current
        val containerW = with(density) { maxWidth.toPx() }
        val gapPx = with(density) { gap.toPx() }
        val rowHPx = with(density) { rowHeight.toPx() }
        val layout = remember(uris, aspects, containerW, gapPx, rowHPx) {
            computeGalleryLayout(uris, aspects, containerW, gapPx, rowHPx)
        }
        Box(
            modifier = Modifier
                .width(with(density) { containerW.toDp() })
                .height(with(density) { layout.heightPx.toDp() })
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(gap)
            ) {
                layout.rows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (layout.single) Arrangement.Center
                        else Arrangement.spacedBy(gap)
                    ) {
                        row.tiles.forEach { tile ->
                            Surface(
                                onClick = {
                                    // Report the tile's spot in the gallery's
                                    // own viewport so the zoom glides from
                                    // exactly where the image sits.
                                    zoomState.zoomIn(
                                        uri = tile.uri,
                                        centerX = tile.xPx + tile.widthPx / 2f,
                                        centerY = tile.yPx + tile.heightPx / 2f,
                                        tileW = tile.widthPx,
                                        tileH = tile.heightPx,
                                        viewW = containerW,
                                        viewH = layout.heightPx
                                    )
                                },
                                shape = RoundedCornerShape(cornerRadius),
                                shadowElevation = 0.dp,
                                modifier = Modifier
                                    .width(with(density) { tile.widthPx.toDp() })
                                    .height(with(density) { tile.heightPx.toDp() })
                            ) {
                                // Tiles are sized to the photo's own aspect
                                // ratio, so Fit fills the rounded box edge to
                                // edge — no bars, no cropping.
                                Image(
                                    painter = rememberAsyncImagePainter(tile.uri),
                                    contentDescription = "Attached image",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(cornerRadius))
                                )
                            }
                        }
                    }
                }
            }
            // In-place zoom overlay — LAST child (same as the mood boards):
            // glides the tapped image from its gallery spot to the gallery's
            // center (arc), pinch/pan refine, tap closes. No dark scrim.
            layout.rows.flatMap { it.tiles }.firstOrNull { it.uri == zoomState.zoomedUri }
                ?.let { tile ->
                    MoodBoardZoomOverlay(
                        zoomState = zoomState,
                        tileUri = tile.uri,
                        tileX = tile.xPx,
                        tileY = tile.yPx,
                        widthPx = tile.widthPx,
                        heightPx = tile.heightPx,
                        viewW = containerW,
                        viewH = layout.heightPx
                    )
                }
        }
    }
}

/** One laid-out tile: its spot and size in the gallery's viewport pixels. */
private data class GalleryTile(
    val uri: String,
    val xPx: Float,
    val yPx: Float,
    val widthPx: Float,
    val heightPx: Float
)

/** One justified row of tiles, all sharing the row's rendered height. */
private data class GalleryRow(
    val tiles: List<GalleryTile>,
    val heightPx: Float
)

/** The full gallery layout: rows + total height (px). */
private data class GalleryLayout(
    val rows: List<GalleryRow>,
    val heightPx: Float,
    val single: Boolean
)

/**
 * Packs [uris] into justified rows. Each image's base width = rowHeight ×
 * aspect, so portraits are narrow and landscapes wide; rows wrap when the
 * next image would overflow the container, then stretch to fill it exactly
 * (heights scale with the stretch so aspect ratios hold). A lone image is
 * shown at natural aspect, capped so an extreme portrait doesn't tower.
 */
private fun computeGalleryLayout(
    uris: List<String>,
    aspects: Map<String, Float>,
    containerW: Float,
    gapPx: Float,
    rowHPx: Float
): GalleryLayout {
    if (uris.size == 1) {
        val a = (aspects[uris[0]] ?: 1f).coerceIn(0.25f, 4f)
        // Cap the single-image height so a portrait can't tower off the page.
        val maxH = rowHPx * 2.6f
        var w = containerW
        var h = w / a
        if (h > maxH) {
            h = maxH
            w = h * a
        }
        val tile = GalleryTile(uris[0], (containerW - w) / 2f, 0f, w, h)
        return GalleryLayout(listOf(GalleryRow(listOf(tile), h)), h, single = true)
    }

    // Pack base widths into rows (uri → base width).
    val rawRows = mutableListOf<MutableList<Pair<String, Float>>>()
    var cur = mutableListOf<Pair<String, Float>>()
    var curW = 0f
    uris.forEach { uri ->
        val a = (aspects[uri] ?: 1f).coerceIn(0.25f, 4f)
        val w = rowHPx * a
        if (cur.isNotEmpty() && curW + w + gapPx > containerW) {
            rawRows.add(cur)
            cur = mutableListOf()
            curW = 0f
        }
        cur.add(uri to w)
        curW += w + gapPx
    }
    if (cur.isNotEmpty()) rawRows.add(cur)

    // Justify every row to the container width; heights follow the stretch.
    var y = 0f
    val rows = rawRows.map { row ->
        val avail = (containerW - (row.size - 1) * gapPx).coerceAtLeast(0f)
        // sumOf has no Float overload — fold the widths in Float instead of
        // triggering an Int/UInt overload ambiguity on the compiler.
        val sum = row.fold(0f) { acc, pair -> acc + pair.second }
        val s = if (sum > 0f) avail / sum else 1f
        val h = rowHPx * s
        var x = 0f
        val tiles = row.map { (uri, baseW) ->
            val w = baseW * s
            val tile = GalleryTile(uri, x, y, w, h)
            x += w + gapPx
            tile
        }
        y += h + gapPx
        GalleryRow(tiles, h)
    }
    val totalH = (y - gapPx).coerceAtLeast(0f)
    return GalleryLayout(rows, totalH, single = false)
}

/**
 * Header-only decode of an attachment's aspect ratio (w/h), EXIF-rotated to
 * match Coil's on-screen rendering. Content URIs resolve through the
 * ContentResolver; file paths decode directly. Falls back to 1f (square)
 * when the image can't be read. Runs on [Dispatchers.IO] — metadata-only,
 * no pixel data is decoded.
 */
private suspend fun imageAspectOf(context: Context, uriString: String): Float {
    val size = withContext(Dispatchers.IO) {
        runCatching {
            val uri = Uri.parse(uriString)
            val scheme = uri.scheme
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            when (scheme) {
                "content" -> context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, opts)
                }
                "file" -> BitmapFactory.decodeFile(uri.path, opts)
                else -> BitmapFactory.decodeFile(uriString, opts)
            }
            if (opts.outWidth <= 0 || opts.outHeight <= 0) return@runCatching null
            var w = opts.outWidth
            var h = opts.outHeight
            // Photos shot sideways carry EXIF rotation; Coil renders them
            // rotated, so swap the raw sensor bounds to match the on-screen
            // aspect (same treatment the mood-board tiles use).
            val orientation = runCatching {
                when (scheme) {
                    "content" -> context.contentResolver.openInputStream(uri)?.use { stream ->
                        ExifInterface(stream).getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL
                        )
                    }
                    // uri.path is nullable — resolve through let so the
                    // decode falls back to the normal-orientation default.
                    "file" -> uri.path?.let { path ->
                        ExifInterface(path).getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL
                        )
                    }
                    else -> null
                }
            }.getOrNull() ?: ExifInterface.ORIENTATION_NORMAL
            val rotationDeg = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
            if (rotationDeg == 90 || rotationDeg == 270) {
                val swap = w
                w = h
                h = swap
            }
            w.toFloat() / h.toFloat()
        }.getOrNull()
    }
    return size ?: 1f
}
