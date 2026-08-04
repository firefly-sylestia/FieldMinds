package com.curio.app.ui.components

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import coil.imageLoader
import coil.request.ImageRequest
import com.curio.app.data.CaptureData
import com.curio.app.data.CurioCategory
import com.curio.app.data.NotePaperColor
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.PatrickHandFontFamily
import com.curio.app.ui.theme.categoryInk
import com.curio.app.ui.theme.categorySurfaceMoodBoard
import com.curio.app.ui.theme.notePaperInk
import com.curio.app.ui.theme.onAccent
import com.curio.app.ui.theme.themedAccent
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * High-quality mood-board PNG export.
 *
 * Renders the FULL saved board — tinted surface + watermark backdrop + the
 * tile collage + floating on-board quote cards, and below it the caption and
 * below-board quote boxes — into a large off-screen [ComposeView] captured
 * to an ARGB PNG at a high pixel size, so the exported image stays crisp
 * even when zoomed in. The collage tiles are preloaded as full-resolution
 * [Bitmap]s BEFORE the capture (never async Coil painters), so the exported
 * image is deterministic — no blank tiles from an in-flight load.
 *
 * Two actions share one render: [saveMoodBoardPng] writes it to the gallery
 * via MediaStore (API 29+) or app-external Pictures + media scan (26–28);
 * [shareMoodBoardPng] hands it to the system share sheet via FileProvider.
 */
object MoodBoardExport {

    /** Captured PNG long side in px — ~2.5x a 1080p screen, still a sane file size. */
    private const val EXPORT_LONG_SIDE = 2400

    /**
     * Renders the board and saves the PNG to the gallery. [onDone] is invoked
     * on the main thread with the saved file path (or null on failure).
     */
    fun saveMoodBoardPng(
        context: Context,
        authority: String,
        data: CaptureData.GalleryWall,
        category: CurioCategory,
        boardSeed: Int,
        topicName: String,
        entryId: String,
        onDone: (String?) -> Unit
    ) {
        exportBoard(context, data, category, boardSeed, topicName, entryId) { bitmap, fileName ->
            withContext(Dispatchers.IO) {
                val path = saveBitmapToGallery(context, bitmap, fileName)
                bitmap.recycle()
                path
            }
        }?.let(onDone) ?: onDone(null)
    }

    /**
     * Renders the board and launches the share sheet with the PNG attached.
     * [onDone] fires on the main thread after the chooser opens.
     */
    fun shareMoodBoardPng(
        context: Context,
        authority: String,
        data: CaptureData.GalleryWall,
        category: CurioCategory,
        boardSeed: Int,
        topicName: String,
        entryId: String,
        onDone: () -> Unit
    ) {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        scope.launch {
            val path = exportBoard(context, data, category, boardSeed, topicName, entryId) { bitmap, fileName ->
                withContext(Dispatchers.IO) {
                    try {
                        val file = File(context.cacheDir, "share").apply { mkdirs() }
                        val dest = File(file, fileName)
                        FileOutputStream(dest).use { out ->
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        }
                        dest.absolutePath
                    } finally {
                        bitmap.recycle()
                    }
                }
            }
            if (path != null) {
                val uri = FileProvider.getUriForFile(context, authority, File(path))
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                runCatching {
                    context.startActivity(Intent.createChooser(intent, "Share mood board"))
                }
            }
            onDone()
        }
    }

    /**
     * Shared pipeline: preload tile bitmaps off-thread → render the board
     * composable off-screen at export resolution → hand the bitmap + file name
     * to [emit]. Returns the [emit] result on the main thread.
     */
    private suspend fun exportBoard(
        context: Context,
        data: CaptureData.GalleryWall,
        category: CurioCategory,
        boardSeed: Int,
        topicName: String,
        entryId: String,
        emit: suspend (Bitmap, String) -> String?
    ): String? {
        // Preload every collage image as a full-size software bitmap so the
        // off-screen capture never races an async Coil load.
        val bitmaps = withContext(Dispatchers.IO) {
            data.tileLayouts.map { t ->
                runCatching {
                    val request = ImageRequest.Builder(context)
                        .data(t.uri)
                        .allowHardware(false)
                        .build()
                    (context.imageLoader.execute(request).drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                }.getOrNull()
            }
        }

        return withContext(Dispatchers.Main) {
            val fileName = "moodboard_${entryId.take(8)}.png"
            val bitmap = renderBoardBitmap(context, data, category, boardSeed, topicName, bitmaps)
                ?: return@withContext null
            emit(bitmap, fileName)
        }
    }

    /**
     * Renders the whole saved mood board into an ARGB bitmap at export
     * resolution. The board is drawn by a real composition pass in an
     * off-screen [ComposeView] (same technique as [shareComposableCard]) so
     * the exported image is a faithful, theme-accurate picture of the saved
     * entry — surface color, watermark backdrop, collage, floating quote
     * cards, caption and below-board quote boxes included.
     */
    private suspend fun renderBoardBitmap(
        context: Context,
        data: CaptureData.GalleryWall,
        category: CurioCategory,
        boardSeed: Int,
        topicName: String,
        bitmaps: List<Bitmap?>
    ): Bitmap? {
        // Square export canvas; the board card is composed with a vertical
        // layout (board collage + caption + quotes) so use a 3:4 portrait.
        val width = EXPORT_LONG_SIDE
        val height = (EXPORT_LONG_SIDE * 1.33f).toInt()

        return withContext(Dispatchers.Main) {
            val lifecycleOwner = SyntheticLifecycleOwner()
            val composeView = ComposeView(context).apply {
                lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
                setContent {
                    // Fixed export density (4x = 1dp → 4px): the same dp values
                    // resolve to the same relative size on EVERY device, so the
                    // exported image's proportions are identical everywhere —
                    // not dependent on the phone's own density.
                    androidx.compose.runtime.CompositionLocalProvider(
                        LocalDensity provides androidx.compose.ui.unit.Density(4f, 1f)
                    ) {
                        MoodBoardShareCard(
                            data = data,
                            category = category,
                            boardSeed = boardSeed,
                            topicName = topicName,
                            bitmaps = bitmaps
                        )
                    }
                }
                layoutParams = ViewGroup.LayoutParams(width, height)
                val widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
                val heightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
                measure(widthSpec, heightSpec)
                layout(0, 0, width, height)
            }
            lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
            lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

            val result = kotlinx.coroutines.suspendCancellableCoroutine<Bitmap?> { cont ->
                composeView.post {
                    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val canvas = android.graphics.Canvas(bmp)
                    canvas.drawColor(android.graphics.Color.WHITE)
                    composeView.draw(canvas)
                    lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                    cont.resume(bmp)
                }
            }
            result
        }
    }

    /**
     * Writes [bitmap] to the gallery. MediaStore insert on API 29+ (no
     * permission needed); app-external Pictures + media scan on 26–28.
     */
    private fun saveBitmapToGallery(context: Context, bitmap: Bitmap, fileName: String): String? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            runCatching {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + "/Curio"
                    )
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val uri: Uri? = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
                )
                uri ?: return@runCatching null
                try {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    context.contentResolver.update(uri, values, null, null)
                } catch (t: Throwable) {
                    // Never leave a ghost IS_PENDING row hidden from the
                    // gallery — delete the entry on any write failure.
                    runCatching { context.contentResolver.delete(uri, null, null) }
                    throw t
                }
                uri.toString()
            }.getOrNull()
        } else {
            runCatching {
                val dir = File(
                    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Curio"
                ).apply { mkdirs() }
                val file = File(dir, fileName)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                MediaScannerConnection.scanFile(
                    context, arrayOf(file.absolutePath), arrayOf("image/png"), null
                )
                file.absolutePath
            }.getOrNull()
        }

    /**
     * Minimal [LifecycleOwner] for off-screen [ComposeView] instances — same
     * synthetic registry [shareComposableCard] uses to drive composition.
     */
    private class SyntheticLifecycleOwner : LifecycleOwner {
        private val registry = LifecycleRegistry(this)
        override val lifecycle: Lifecycle get() = registry
        fun handleLifecycleEvent(event: Lifecycle.Event) {
            registry.handleLifecycleEvent(event)
        }
    }
}

/**
 * Self-contained render of the FULL saved mood board — the exact picture the
 * entry's detail page shows, composed for off-screen capture:
 *
 *  1. category-tinted board surface + seeded watermark backdrop (the whole
 *     collage background),
 *  2. the tile collage, contain-fit to the export card and centered,
 *  3. floating on-board quote cards at their saved spots,
 *  4. the caption slip and below-board quote boxes under the board.
 *
 * Tiles draw from [bitmaps] (preloaded) so the capture never waits on Coil.
 */
@Composable
private fun MoodBoardShareCard(
    data: CaptureData.GalleryWall,
    category: CurioCategory,
    boardSeed: Int,
    topicName: String,
    bitmaps: List<Bitmap?>
) {
    val density = LocalDensity.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(category.categorySurfaceMoodBoard())
    ) {
        // ── Seeded watermark backdrop — same pattern as the saved view ──
        CurioMoodBoardBackdrop(
            seed = boardSeed,
            accent = category.themedAccent(),
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 36.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Header: category + topic ────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = category.themedAccent()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CurioIcon(
                            name = category.iconGlyph,
                            contentDescription = null,
                            tint = category.onAccent(),
                            size = 15.dp
                        )
                        Text(
                            text = category.displayName,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = category.onAccent()
                        )
                    }
                }
                Text(
                    text = topicName,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = category.categoryInk(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }

            // ── Collage — contain-fit + centered, floating quotes on top ──
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(category.categorySurfaceMoodBoard())
            ) {
                val viewW = maxWidth.value * density.density
                val viewH = maxHeight.value * density.density
                if (data.tileLayouts.isNotEmpty() && viewW > 0f && viewH > 0f) {
                    val maxX = data.tileLayouts.maxOfOrNull { it.offsetXPx + it.widthPx } ?: 0f
                    val maxY = data.tileLayouts.maxOfOrNull { it.offsetYPx + it.heightPx } ?: 0f
                    val scale = if (maxX > 0f && maxY > 0f) {
                        (viewW / maxX).coerceAtMost(viewH / maxY)
                    } else 1f
                    val boardW = maxX * scale
                    val boardH = maxY * scale
                    val offsetX = (viewW - boardW) / 2f
                    val offsetY = (viewH - boardH) / 2f

                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(offsetX.roundToInt(), offsetY.roundToInt())
                            }
                    ) {
                        data.tileLayouts.forEachIndexed { i, tile ->
                            val bmp = bitmaps.getOrNull(i)
                            if (bmp == null) return@forEachIndexed
                            Box(
                                modifier = Modifier
                                    .offset {
                                        IntOffset(
                                            (tile.offsetXPx * scale).roundToInt(),
                                            (tile.offsetYPx * scale).roundToInt()
                                        )
                                    }
                                    .size(
                                        width = with(density) { (tile.widthPx * scale).toDp() },
                                        height = with(density) { (tile.heightPx * scale).toDp() }
                                    )
                                    .rotate(tile.rotationDeg)
                            ) {
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(14.dp))
                                )
                            }
                        }

                        // Floating on-board quote cards at their saved spots.
                        if (data.quotes.orEmpty().isNotEmpty()) {
                            MoodBoardFloatingCards(
                                quotes = data.quotes.orEmpty(),
                                styles = data.quoteStyles.orEmpty(),
                                colors = data.quoteColors.orEmpty(),
                                tilts = data.quoteTilts.orEmpty(),
                                positions = data.quotePositions.orEmpty(),
                                onBoard = data.quoteOnBoard.orEmpty(),
                                canvasWPx = maxX,
                                canvasHPx = maxY,
                                boardScale = scale
                            )
                        }
                    }
                }
            }

            // ── Caption slip ────────────────────────────────────────────
            if (!data.caption.isNullOrBlank()) {
                val sheet = data.captionColor ?: NotePaperColor.CREAM
                NotePaperCard(
                    style = data.captionStyle ?: NotePaperStyleFallback(data),
                    paperColor = sheet,
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = data.caption,
                        style = MaterialTheme.typography.bodyLarge.copy(fontFamily = PatrickHandFontFamily),
                        color = notePaperInk(sheet)
                    )
                }
            }

            // ── Below-board quote boxes ─────────────────────────────────
            val belowFlags = data.quoteOnBoard.orEmpty()
            data.quotes.orEmpty().forEachIndexed { i, q ->
                if (q.isNullOrBlank()) return@forEachIndexed
                if (belowFlags.getOrElse(i) { true }) return@forEachIndexed
                val sheet = data.quoteColors.orEmpty().getOrNull(i) ?: NotePaperColor.CREAM
                val tilt = data.quoteTilts.orEmpty().getOrNull(i) ?: 0f
                NotePaperCard(
                    style = data.quoteStyles.orEmpty().getOrNull(i)
                        ?: NotePaperStyleFallback(data),
                    paperColor = sheet,
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .rotate(tilt)
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CurioIcon(
                            name = CurioIcons.FormatQuote,
                            contentDescription = null,
                            tint = notePaperInk(sheet).copy(alpha = 0.45f),
                            size = 20.dp
                        )
                        Text(
                            text = "\u201C$q\u201D",
                            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = PatrickHandFontFamily),
                            color = notePaperInk(sheet)
                        )
                    }
                }
            }
        }
    }
}

/** Legacy fallback paper style for caption / below-board quote slips. */
private fun NotePaperStyleFallback(data: CaptureData.GalleryWall) =
    data.paperStyle ?: com.curio.app.data.NotePaperStyle.RULED
