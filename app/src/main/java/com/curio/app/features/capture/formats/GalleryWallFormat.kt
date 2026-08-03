package com.curio.app.features.capture.formats

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import com.curio.app.data.CaptureData
import com.curio.app.data.NotePaperColor
import com.curio.app.data.NotePaperStyle
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import android.content.Context
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.curio.app.data.AppPreferences
import com.curio.app.ui.components.CurioMoodBoardBackdrop
import com.curio.app.ui.components.MoodBoardZoomOverlay
import com.curio.app.ui.components.moodBoardPainter
import com.curio.app.ui.components.rememberMoodBoardZoomState
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.pastelFillInk
import kotlin.math.roundToInt
import kotlin.random.Random

// ── Mood board tile with pixel-based positioning ─────────────────────

private data class MoodTile(
    val id: Int,
    val uri: String,
    val offsetXPx: Float,
    val offsetYPx: Float,
    val rotationDeg: Float,
    val widthPx: Float,
    val heightPx: Float
)

/**
 * Transient drag/pinch preview for one tile while a finger gesture is in
 * flight. Kept OUTSIDE the [MoodTile] list so per-frame gesture updates
 * recompose only the dragged tile instead of mutating the list (which would
 * re-run the whole save pipeline) on every pointer move. The final values are
 * committed into the list once when the finger lifts.
 */
private data class TileDragPreview(
    val dx: Float,
    val dy: Float,
    val scale: Float = 1f,
    val rotation: Float = 0f,
    /** True when a single-finger drag moved the tile (pin-zone eligible). */
    val byDrag: Boolean = false
)

/**
 * Gallery Wall / Mood Board format — CURIO_SPEC §8.4 (Visual Art / Painters).
 *
 * A freeform collage canvas where users can:
 *  - Add images from their gallery
 *  - Drag tiles anywhere on the board
 *  - Tap to bring a tile to the front (z-order)
 *  - Remove tiles via corner × button
 *  - Add an optional caption below
 *  - Expand to a full-screen canvas for precise placement (top-right button)
 *
 * Tiles are placed with random initial positions, slight rotations, and
 * varying sizes to create a natural mood-board collage aesthetic. Tiles
 * render with [ContentScale.Fit] + inner padding — the same logic the saved
 * EntryDetail view uses — so images are never cropped or pixelated while
 * composing.
 *
 * The board canvas sits on a theme-aware watermark backdrop whose random
 * glyph scatter is seeded per board, so every mood board gets its own quiet
 * background pattern. New boards get a fresh random seed; edit mode passes
 * [boardSeed] (the saved entry's id hash) so the editor's pattern matches
 * the saved EntryDetail view exactly.
 *
 * When [initialData] is supplied (edit mode), the board preloads the saved
 * tiles and caption so the user can continue arranging and re-save.
 */
@Composable
fun GalleryWallFormat(
    accent: Color,
    tint: Color,
    onCanSaveChange: (Boolean) -> Unit,
    onDataChanged: (CaptureData?) -> Unit = {},
    initialData: CaptureData.GalleryWall? = null,
    boardSeed: Int? = null
) {
    val tiles = remember(initialData) {
        mutableStateListOf<MoodTile>().apply {
            initialData?.tileLayouts?.forEachIndexed { i, t ->
                add(
                    MoodTile(
                        id = i,
                        uri = t.uri,
                        offsetXPx = t.offsetXPx,
                        offsetYPx = t.offsetYPx,
                        rotationDeg = t.rotationDeg,
                        widthPx = t.widthPx,
                        heightPx = t.heightPx
                    )
                )
            }
        }
    }
    var caption by remember(initialData) { mutableStateOf(initialData?.caption ?: "") }
    // Note-paper style for the caption box — legacy entries lack the field
    // (Gson → null), fall back to the take-level paperStyle → RULED.
    var captionStyle by remember(initialData) {
        mutableStateOf(initialData?.captionStyle ?: initialData?.paperStyle ?: NotePaperStyle.RULED)
    }
    // Note-paper color for the caption box — legacy entries lack the field
    // (Gson → null), fall back to CREAM.
    var captionColor by remember(initialData) {
        mutableStateOf(initialData?.captionColor ?: NotePaperColor.CREAM)
    }
    // Quote cards — the SHARED hand-placed paper notecard section (same
    // component Marginalia / Reel Notes / Sound Bite use). Owns the parallel
    // lists (text / spans / tilt / style / color); new cards inherit the
    // caption's current paper style + color.
    val quoteCards = rememberQuoteCardsState(
        initialQuotes = initialData?.quotes.orEmpty(),
        initialSpans = initialData?.quoteSpans.orEmpty(),
        initialTilts = initialData?.quoteTilts.orEmpty(),
        initialStyles = initialData?.quoteStyles.orEmpty(),
        initialColors = initialData?.quoteColors.orEmpty(),
        defaultStyle = initialData?.captionStyle ?: initialData?.paperStyle ?: NotePaperStyle.RULED,
        defaultColor = initialData?.captionColor ?: NotePaperColor.CREAM
    )
    var boardExpanded by remember { mutableStateOf(false) }
    // Mood — the shared "How did it make you feel?" row. The board carries
    // its own mood field now (CaptureData.GalleryWall.mood); legacy entries
    // have none (Gson → null).
    var mood by remember(initialData) { mutableStateOf(initialData?.mood) }
    // New board: fresh random pattern. Edit mode: reuse the caller-provided
    // seed (entry-id hash) so the editor matches the saved view's backdrop.
    val seed = remember(boardSeed, initialData) { boardSeed ?: Random.nextInt() }

    // A caption-only board is still a draft — it must save and must trigger
    // the leave / format-switch guards (the old tiles/quotes-only rule let
    // a caption-only take exit silently and lose the caption).
    val canSave = tiles.isNotEmpty() || quoteCards.hasContent || caption.isNotBlank()
    LaunchedEffect(
        canSave, caption, tiles.toList(), captionStyle, captionColor, mood,
        quoteCards.quotes.toList(), quoteCards.spans.toList(), quoteCards.tilts.toList(),
        quoteCards.styles.toList(), quoteCards.colors.toList()
    ) {
        onCanSaveChange(canSave)
        onDataChanged(
            if (canSave) CaptureData.GalleryWall(
                imageCount = tiles.size,
                caption = caption,
                imageUris = tiles.map { it.uri },
                tileLayouts = tiles.map { CaptureData.TileLayout(it.uri, it.offsetXPx, it.offsetYPx, it.rotationDeg, it.widthPx, it.heightPx) },
                captionStyle = captionStyle,
                captionColor = captionColor,
                quotes = quoteCards.quotes.toList(),
                quoteSpans = quoteCards.spans.toList(),
                quoteTilts = quoteCards.tilts.toList(),
                quoteStyles = quoteCards.styles.toList(),
                quoteColors = quoteCards.colors.toList(),
                // Legacy fallback — mirror the caption's style.
                paperStyle = captionStyle,
                mood = mood
            )
            else null
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── Nudge past 8 images ──────────────────────────────────────
        if (tiles.size > 8) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CurioIcon(
                        name = CurioIcons.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        size = 18.dp
                    )
                    Text(
                        text = "Getting full! Consider saving and starting a new board.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }

        // ═══════════════════════════════════════════════════════════════
        // Inline mood board canvas (editable)
        // ═══════════════════════════════════════════════════════════════
        MoodBoardCanvas(
            tiles = tiles,
            accent = accent,
            tint = tint,
            seed = seed,
            fullScreen = false,
            onExpand = { boardExpanded = true },
            onCollapse = {}
        )

        // ── Caption field — wears the note-paper slip like the other text
        //    boxes, with its own per-field paper-style toggle.
        PaperLineField(
            value = caption,
            onValueChange = { caption = it },
            label = "Add a caption (optional)",
            paperStyle = captionStyle,
            onPaperStyleChange = { captionStyle = it },
            paperColor = captionColor,
            onPaperColorChange = { captionColor = it }
        )

        // ── Quote cards — the SHARED hand-placed paper notecard section ──
        // New cards inherit the caption's current paper style + color.
        QuoteCardsSection(
            state = quoteCards,
            newCardStyle = { captionStyle },
            newCardColor = { captionColor }
        )
    }

    // ── Full-screen editing canvas ────────────────────────────────────
    if (boardExpanded) {
        Dialog(
            onDismissRequest = { boardExpanded = false },
            // True full screen: the dialog draws behind the system bars so
            // the board fills the whole display instead of floating like a
            // dialog page. The controls below pad for the bars.
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                MoodBoardCanvas(
                    tiles = tiles,
                    accent = accent,
                    tint = tint,
                    seed = seed,
                    fullScreen = true,
                    onExpand = {},
                    onCollapse = { boardExpanded = false }
                )
            }
        }
    }
}

/**
 * The editable mood-board canvas — shared by the inline card and the
 * full-screen expanded dialog so the same tile interactions (drag, tap to
 * front, drag-to-pin-zone, remove, add, clear) work at any size. Renders
 * tiles with [ContentScale.Fit] + padding exactly like the saved
 * EntryDetail view.
 */
@Composable
private fun MoodBoardCanvas(
    tiles: SnapshotStateList<MoodTile>,
    accent: Color,
    tint: Color,
    seed: Int,
    fullScreen: Boolean,
    onExpand: () -> Unit,
    onCollapse: () -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    var canvasWPx by remember { mutableFloatStateOf(0f) }
    var canvasHPx by remember { mutableFloatStateOf(0f) }
    var draggingTileId by remember { mutableStateOf<Int?>(null) }
    var inPinZone by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    // Tile × deletion confirms first — edit-mode boards arrive prefilled, so
    // the × must never silently throw away a saved image (same protection as
    // the take-tab × in the universal editor).
    var pendingRemoveTileId by remember { mutableStateOf<Int?>(null) }
    // In-place tile zoom: double-tap springs the image up over the canvas —
    // no separate dialog page. Pinch/pan continue on the zoom overlay.
    val zoomState = rememberMoodBoardZoomState()
    // v6.7 — offsets snap 1:1 while a pinch is live so panning tracks the
    // fingers; the spring only runs for open/close/reset (avoids the old
    // delayed-pan feel where the image caught up only after the gesture).
    val animatedOffsetX by animateFloatAsState(
        targetValue = zoomState.offsetX,
        animationSpec = if (zoomState.gestureActive) snap()
        else spring(dampingRatio = 0.8f, stiffness = 280f),
        label = "editorMoodZoomOffsetX"
    )
    val animatedOffsetY by animateFloatAsState(
        targetValue = zoomState.offsetY,
        animationSpec = if (zoomState.gestureActive) snap()
        else spring(dampingRatio = 0.8f, stiffness = 280f),
        label = "editorMoodZoomOffsetY"
    )
    val pinZoneHeightPx = with(density) { 52.dp.toPx() }
    // Smallest a tile can be pinched to — shared by the live drag preview
    // and the commit so what you see while dragging is exactly what saves.
    val minTilePx = with(density) { 60.dp.toPx() }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        uris.forEach { uri ->
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
        }
        uris.forEach { uri ->
            // v6.1 — size each new tile to the photo's own aspect ratio so
            // ContentScale.Fit fills the rounded box with no bars/cropping.
            val bounds = decodeImageBounds(context, uri)
            val baseW = with(density) { (100..160).random().dp.toPx() }
            val baseH = with(density) { (120..180).random().dp.toPx() }
            val minPx = with(density) { 80.dp.toPx() }
            val maxPx = with(density) { 320.dp.toPx() }
            val (tileW, tileH) = if (bounds != null && bounds.second > 0) {
                val aspect = bounds.first.toFloat() / bounds.second.toFloat()
                if (aspect >= 1f) {
                    // Landscape: anchor width, derive height.
                    val w = baseW
                    val h = (w / aspect).coerceIn(minPx, maxPx)
                    w to h
                } else {
                    // Portrait: anchor height, derive width.
                    val h = baseH
                    val w = (h * aspect).coerceIn(minPx, maxPx)
                    w to h
                }
            } else {
                baseW to baseH
            }
            val maxX = (canvasWPx - tileW).coerceAtLeast(0f)
            val maxY = (canvasHPx - tileH).coerceAtLeast(0f)
            tiles.add(
                MoodTile(
                    id = (tiles.maxOfOrNull { it.id } ?: -1) + 1,
                    uri = uri.toString(),
                    offsetXPx = if (maxX > 0f) Random.nextFloat() * maxX else 0f,
                    offsetYPx = if (maxY > 0f) Random.nextFloat() * maxY else 0f,
                    rotationDeg = (-12..12).random().toFloat(),
                    widthPx = tileW,
                    heightPx = tileH
                )
            )
        }
    }

    Surface(
        // RoundedCornerShape(0.dp) is a rectangle — RectangleShape isn't
        // available in the Compose BOM this project resolves.
        shape = if (fullScreen) RoundedCornerShape(0.dp) else RoundedCornerShape(24.dp),
        // The board background wears the category tint so the collage reads
        // as a tinted surface (same wash language as the page around it);
        // with the tint setting off it returns to a plain transparent board.
        // The AMOLED theme style does NOT black this out — the mood board's
        // tinted canvas is its identity, so only the manual Settings toggle
        // turns it off (tintWashEnabledState, not tintWashEffective).
        color = if (AppPreferences.tintWashEnabledState) tint else Color.Transparent,
        tonalElevation = 0.dp,
        // Faint accent rule — the board sits on the tinted page, so a slim
        // category-colored border keeps it from visually blending into the
        // wash (full-screen editor is on a plain dialog background, no need).
        border = if (fullScreen) null else BorderStroke(1.dp, accent.copy(alpha = 0.26f)),
        modifier = if (fullScreen) Modifier.fillMaxSize() else Modifier
            .fillMaxWidth()
            .height(420.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            // Read actual canvas size from constraints (fixes "stuck on left" bug)
            LaunchedEffect(maxWidth, maxHeight) {
                canvasWPx = with(density) { maxWidth.toPx() }
                canvasHPx = with(density) { maxHeight.toPx() }
            }

            // ── Theme-aware random watermark backdrop ─────────────────
            CurioMoodBoardBackdrop(
                seed = seed,
                accent = accent,
                modifier = Modifier.fillMaxSize()
            )

            // (No board-level pinch in the editor — single-finger drags move
            // tiles; image zoom is per-tile via double-tap or the magnifier.)
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                if (tiles.isEmpty()) {
                    // Empty state
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = accent.copy(alpha = 0.12f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                CurioIcon(
                                    name = CurioIcons.Image,
                                    contentDescription = null,
                                    tint = accent,
                                    size = 32.dp
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Start your mood board",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Add images, drag them around",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    tiles.forEachIndexed { i, tile ->
                        MoodBoardEditorTile(
                            tile = tile,
                            index = i,
                            isDragging = draggingTileId == tile.id,
                            canvasWPx = canvasWPx,
                            canvasHPx = canvasHPx,
                            pinZoneHeightPx = pinZoneHeightPx,
                            minTilePx = minTilePx,
                            onBringToFront = { id ->
                                val idx = tiles.indexOfFirst { it.id == id }
                                if (idx >= 0 && idx != tiles.lastIndex) tiles.add(tiles.removeAt(idx))
                            },
                            onRemove = { id ->
                                // Confirm before deleting a tile — the × sits on
                                // a real image, so route through the dialog.
                                pendingRemoveTileId = id
                            },
                            onZoomIn = { uri, tw, th, vw, vh -> zoomState.zoomIn(uri, tw, th, vw, vh) },
                            onDragStart = { draggingTileId = it },
                            onPinZoneChange = { if (it != inPinZone) inPinZone = it },
                            onCommit = { id, preview ->
                                val idx = tiles.indexOfFirst { it.id == id }
                                if (idx >= 0) {
                                    val t = tiles[idx]
                                    // Same clamps as the live preview (with the
                                    // same pre-measure fallback), so the tile
                                    // never snaps or collapses when released.
                                    val cw = if (canvasWPx > 0f) canvasWPx else t.widthPx
                                    val ch = if (canvasHPx > 0f) canvasHPx else t.heightPx
                                    val newW = (t.widthPx * preview.scale).coerceIn(minTilePx, cw)
                                    val newH = (t.heightPx * preview.scale).coerceIn(minTilePx, ch)
                                    val newX = (t.offsetXPx + preview.dx)
                                        .coerceIn(0f, (cw - newW).coerceAtLeast(0f))
                                    val newY = (t.offsetYPx + preview.dy)
                                        .coerceIn(0f, (ch - newH).coerceAtLeast(0f))
                                    tiles[idx] = t.copy(
                                        offsetXPx = newX,
                                        offsetYPx = newY,
                                        widthPx = newW,
                                        heightPx = newH,
                                        rotationDeg = (t.rotationDeg + preview.rotation) % 360f
                                    )
                                    // Pin-to-front drop zone: releasing near
                                    // the top pins the tile to the front —
                                    // single-finger drags only, not pinches.
                                    if (preview.byDrag && newY < pinZoneHeightPx && idx != tiles.lastIndex) {
                                        tiles.add(tiles.removeAt(idx))
                                    }
                                }
                            },
                            onDragEnd = { draggingTileId = null }
                        )
                    }
                }

                // ── Floating "+" add button ──────────────────────────
                Surface(
                    onClick = { imagePicker.launch(arrayOf("image/*")) },
                    shape = RoundedCornerShape(28.dp),
                    color = accent,
                    shadowElevation = 0.dp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .then(if (fullScreen) Modifier.navigationBarsPadding() else Modifier)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CurioIcon(
                            name = CurioIcons.Add,
                            contentDescription = "Add images",
                            tint = pastelFillInk(accent),
                            size = 20.dp
                        )
                        Text(
                            text = "Add images",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = pastelFillInk(accent)
                        )
                    }
                }
            }                // ── Pin-to-front drop zone (appears while dragging) ──────
                if (draggingTileId != null) {
                    val highlight = inPinZone
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .then(if (fullScreen) Modifier.statusBarsPadding() else Modifier)
                            .fillMaxWidth()
                            .height(52.dp)
                            .background(
                                color = accent.copy(alpha = if (highlight) 0.3f else 0.13f),
                                shape = RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp)
                            )
                            .zIndex(500f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CurioIcon(
                                name = CurioIcons.KeyboardArrowUp,
                                contentDescription = null,
                                tint = accent,
                                size = 18.dp
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (highlight) "Release to pin to front" else "Drag here to pin to front",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = accent
                            )
                        }
                    }
                }

                // ── Clear board (expanded editor only, hidden when empty) ──
                if (fullScreen && tiles.isNotEmpty()) {
                    Surface(
                        onClick = { showClearConfirm = true },
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.errorContainer,
                        shadowElevation = 0.dp,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .then(if (fullScreen) Modifier.navigationBarsPadding() else Modifier)
                            .padding(16.dp)
                            .zIndex(999f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            CurioIcon(
                                name = CurioIcons.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                size = 15.dp
                            )
                            Text(
                                text = "Clear board",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // ── Expand / collapse button ──────────────────────────────
            Surface(
                onClick = { if (fullScreen) onCollapse() else onExpand() },
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                shadowElevation = 0.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .then(if (fullScreen) Modifier.statusBarsPadding() else Modifier)
                    .padding(10.dp)
                    .size(36.dp)
                    .zIndex(999f)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    CurioIcon(
                        name = if (fullScreen) CurioIcons.Close else CurioIcons.Fullscreen,
                        contentDescription = if (fullScreen) "Collapse mood board" else "Expand mood board",
                        tint = MaterialTheme.colorScheme.onSurface,
                        size = 18.dp
                    )
                }
            }

            // ── In-place image zoom overlay (double-tap / search — no page) ──
            tiles.firstOrNull { it.uri == zoomState.zoomedUri }?.let { tile ->
                MoodBoardZoomOverlay(
                    zoomState = zoomState,
                    animatedOffsetX = animatedOffsetX,
                    animatedOffsetY = animatedOffsetY,
                    tileUri = tile.uri,
                    widthPx = tile.widthPx,
                    heightPx = tile.heightPx
                )
            }
        }
    }


    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear mood board?") },
            text = { Text("Remove all ${tiles.size} images? This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    tiles.clear()
                    showClearConfirm = false
                }) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Keep")
                }
            }
        )
    }

    // ── Confirm before removing a single tile via its × ──────────────
    if (pendingRemoveTileId != null) {
        AlertDialog(
            onDismissRequest = { pendingRemoveTileId = null },
            title = { Text("Remove this image?") },
            text = { Text("This will delete the image from your mood board. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    val id = pendingRemoveTileId
                    val idx = tiles.indexOfFirst { it.id == id }
                    if (idx >= 0) tiles.removeAt(idx)
                    pendingRemoveTileId = null
                }) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingRemoveTileId = null }) {
                    Text("Keep")
                }
            }
        )
    }
}

/**
 * One editable mood-board tile — the photo floats on the board with rounded
 * corners (no card/box behind it). One finger drags it, two fingers pinch to
 * resize + twist to rotate, a tap brings it to the front, and double-tap (or
 * the search button) zooms it in place.
 *
 * The drag/pinch gesture accumulates into a [TileDragPreview] held in
 * per-tile [remember] state — NOT into the [MoodTile] list — so a frame of
 * dragging recomposes ONLY this tile instead of mutating the list (and
 * re-firing the save pipeline) on every pointer move. The final values are
 * committed once through [onCommit] when the finger lifts.
 */
@Composable
private fun MoodBoardEditorTile(
    tile: MoodTile,
    index: Int,
    isDragging: Boolean,
    canvasWPx: Float,
    canvasHPx: Float,
    pinZoneHeightPx: Float,
    minTilePx: Float,
    onBringToFront: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onZoomIn: (String, Float, Float, Float, Float) -> Unit,
    onDragStart: (Int) -> Unit,
    onPinZoneChange: (Boolean) -> Unit,
    onCommit: (Int, TileDragPreview) -> Unit,
    onDragEnd: () -> Unit
) {
    val density = LocalDensity.current
    // Preview lives INSIDE the tile so per-frame writes recompose only this
    // tile (Compose scopes snapshot reads to the composable that reads them).
    val dragPreview = remember(tile.id) { mutableStateOf<TileDragPreview?>(null) }
    // pointerInput never restarts (its key is tile.id), so the gesture
    // coroutine must read the LATEST tile — never the first composition's.
    val currentTile by rememberUpdatedState(tile)

    // Before the canvas size is measured (first frame), fall back to the
    // tile's stored size so tiles never flash at 0x0 or drift to the corner.
    val canvasW = if (canvasWPx > 0f) canvasWPx else tile.widthPx
    val canvasH = if (canvasHPx > 0f) canvasHPx else tile.heightPx
    val preview = dragPreview.value
    val scale = preview?.scale ?: 1f
    val renderW = (tile.widthPx * scale).coerceIn(minTilePx, canvasW)
    val renderH = (tile.heightPx * scale).coerceIn(minTilePx, canvasH)
    val renderX = (tile.offsetXPx + (preview?.dx ?: 0f))
        .coerceIn(0f, (canvasW - renderW).coerceAtLeast(0f))
    val renderY = (tile.offsetYPx + (preview?.dy ?: 0f))
        .coerceIn(0f, (canvasH - renderH).coerceAtLeast(0f))
    val renderRotation = tile.rotationDeg + (preview?.rotation ?: 0f)

    Box(
        modifier = Modifier
            .offset {
                IntOffset(renderX.roundToInt(), renderY.roundToInt())
            }
            .zIndex(if (isDragging) 400f else index.toFloat())
            .pointerInput(tile.id) {
                detectTapGestures(
                    onTap = { onBringToFront(currentTile.id) },
                    // Double-tap zooms the image in place instead of opening
                    // a full-screen page.
                    onDoubleTap = {
                        onZoomIn(currentTile.uri, currentTile.widthPx, currentTile.heightPx, canvasW, canvasH)
                    }
                )
            }
            .pointerInput(tile.id) {
                // One handler for every move: one finger drags the tile (with
                // pin-to-front drop zone); two fingers pinch to resize and
                // twist to rotate. Updates only the local preview state, so
                // nothing above this tile recomposes mid-gesture.
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    // Touch slop gates only the START of a drag: total travel
                    // from the down point must cross slop before the tile is
                    // claimed, so a tiny jitter on an intended tap never
                    // flashes the pin-to-front zone. Once dragging, EVERY
                    // event delta applies 1:1 — gating each move by slop made
                    // slow/moderate drags stutter, because per-frame deltas
                    // at 60-120Hz sit far below slop and the tile only jumped
                    // when a single event happened to cross it.
                    val slop = viewConfiguration.touchSlop
                    var multiTouch = false
                    var dragged = false
                    var dx = 0f
                    var dy = 0f
                    var gestureScale = 1f
                    var gestureRotation = 0f
                    do {
                        val event = awaitPointerEvent()
                        val pressed = event.changes.filter { it.pressed }
                        if (pressed.size >= 2) {
                            multiTouch = true
                            gestureScale *= event.calculateZoom()
                            gestureRotation += event.calculateRotation()
                            dragPreview.value = TileDragPreview(dx, dy, gestureScale, gestureRotation, byDrag = dragged)
                            event.changes.forEach { it.consume() }
                        } else if (pressed.size == 1 && !multiTouch) {
                            val change = pressed.first()
                            val dragAmount = change.position - change.previousPosition
                            if (!dragged && (change.position - down.position).getDistance() >= slop) {
                                dragged = true
                                onDragStart(tile.id)
                            }
                            if (dragged) {
                                change.consume()
                                dx += dragAmount.x
                                dy += dragAmount.y
                                dragPreview.value = TileDragPreview(dx, dy, gestureScale, gestureRotation, byDrag = true)
                                // Highlight the pin zone (the parent only
                                // recomposes when the value actually flips).
                                onPinZoneChange(currentTile.offsetYPx + dy < pinZoneHeightPx)
                            }
                        }
                        if (pressed.isEmpty()) break
                    } while (true)

                    if (dragged || multiTouch) {
                        onCommit(tile.id, TileDragPreview(dx, dy, gestureScale, gestureRotation, byDrag = dragged))
                    }
                    dragPreview.value = null
                    onDragEnd()
                }
            }
    ) {
        // Frameless: the photo itself is the tile — rounded corners, no card.
        // Rotate first, then clip, so the rounded shape rotates with the image
        // (clip-after-rotate would slice the corners off).
        Image(
            painter = moodBoardPainter(tile.uri),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(
                    width = with(density) { renderW.toDp() },
                    height = with(density) { renderH.toDp() }
                )
                .rotate(renderRotation)
                .clip(RoundedCornerShape(14.dp))
        )

        // ── Zoom-in-place button (bottom-end) ─────────────────────────
        Surface(
            onClick = { onZoomIn(tile.uri, tile.widthPx, tile.heightPx, canvasW, canvasH) },
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.48f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(7.dp)
                .size(26.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                CurioIcon(
                    name = CurioIcons.Search,
                    contentDescription = "Zoom image",
                    tint = Color.White,
                    size = 14.dp
                )
            }
        }

        // ── × Remove button ───────────────────────────────────────────
        Surface(
            onClick = { onRemove(tile.id) },
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.55f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 4.dp, y = (-4).dp)
                .size(22.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                CurioIcon(
                    name = CurioIcons.Close,
                    contentDescription = "Remove",
                    tint = Color.White,
                    size = 13.dp
                )
            }
        }
    }
}

/**
 * Cheap header-only decode of a content-URI image's pixel bounds — used to
 * size each new mood-board tile to the photo's own aspect ratio so
 * [ContentScale.Fit] fills the rounded box with no bars or cropping.
 * Returns null when the image can't be read or has no dimensions.
 */
private fun decodeImageBounds(context: Context, uri: Uri): Pair<Int, Int>? = runCatching {
    val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, opts)
    }
    if (opts.outWidth <= 0 || opts.outHeight <= 0) return@runCatching null
    var width = opts.outWidth
    var height = opts.outHeight
    // Photos shot sideways carry EXIF rotation; Coil renders them rotated, so
    // swap the raw sensor bounds to match the on-screen aspect. Without this,
    // tiles get sized to the wrong aspect and ContentScale.Fit letterboxes.
    // (Framework android.media.ExifInterface exposes the raw orientation tag —
    // no `rotationDegrees` property — so map the ORIENTATION_* constants.)
    val orientation = context.contentResolver.openInputStream(uri)?.use { stream ->
        ExifInterface(stream).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
    } ?: ExifInterface.ORIENTATION_NORMAL
    val rotationDeg = when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90
        ExifInterface.ORIENTATION_ROTATE_180 -> 180
        ExifInterface.ORIENTATION_ROTATE_270 -> 270
        else -> 0
    }
    if (rotationDeg == 90 || rotationDeg == 270) {
        val swap = width
        width = height
        height = swap
    }
    width to height
}.getOrNull()

