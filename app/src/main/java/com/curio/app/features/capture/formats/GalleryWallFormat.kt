package com.curio.app.features.capture.formats

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import com.curio.app.data.CaptureData
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.curio.app.ui.components.CurioMoodBoardBackdrop
import com.curio.app.ui.components.MoodBoardZoomOverlay
import com.curio.app.ui.components.moodBoardPainter
import com.curio.app.ui.components.rememberMoodBoardZoomState
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
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
    var boardExpanded by remember { mutableStateOf(false) }
    // New board: fresh random pattern. Edit mode: reuse the caller-provided
    // seed (entry-id hash) so the editor matches the saved view's backdrop.
    val seed = remember(boardSeed, initialData) { boardSeed ?: Random.nextInt() }

    val canSave = tiles.isNotEmpty()
    LaunchedEffect(canSave, caption, tiles.toList()) {
        onCanSaveChange(canSave)
        onDataChanged(
            if (canSave) CaptureData.GalleryWall(
                imageCount = tiles.size,
                caption = caption,
                imageUris = tiles.map { it.uri },
                tileLayouts = tiles.map { CaptureData.TileLayout(it.uri, it.offsetXPx, it.offsetYPx, it.rotationDeg, it.widthPx, it.heightPx) }
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
            seed = seed,
            fullScreen = false,
            onExpand = { boardExpanded = true },
            onCollapse = {}
        )

        // ── Caption field ─────────────────────────────────────────────
        OutlinedTextField(
            value = caption,
            onValueChange = { caption = it },
            label = { Text("Add a caption (optional)") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth()
        )
    }

    // ── Full-screen editing canvas ────────────────────────────────────
    if (boardExpanded) {
        Dialog(
            onDismissRequest = { boardExpanded = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                MoodBoardCanvas(
                    tiles = tiles,
                    accent = accent,
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
    // In-place tile zoom: double-tap springs the image up over the canvas —
    // no separate dialog page. Pinch/pan continue on the zoom overlay.
    val zoomState = rememberMoodBoardZoomState()
    val animatedScale by animateFloatAsState(
        targetValue = zoomState.scaleTarget,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 280f),
        label = "editorMoodZoomScale"
    )
    val animatedOffsetX by animateFloatAsState(
        targetValue = zoomState.offsetX,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 280f),
        label = "editorMoodZoomOffsetX"
    )
    val animatedOffsetY by animateFloatAsState(
        targetValue = zoomState.offsetY,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 280f),
        label = "editorMoodZoomOffsetY"
    )
    val pinZoneHeightPx = with(density) { 52.dp.toPx() }

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
            val tileW = with(density) { (100..160).random().dp.toPx() }
            val tileH = with(density) { (120..180).random().dp.toPx() }
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
        color = Color.Transparent,
        tonalElevation = 0.dp,
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
                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        tile.offsetXPx.roundToInt().coerceIn(0, canvasWPx.roundToInt()),
                                        tile.offsetYPx.roundToInt().coerceIn(0, canvasHPx.roundToInt())
                                    )
                                }
                                .zIndex(i.toFloat())
                                .pointerInput(tile.id) {
                                    detectTapGestures(
                                        onTap = {
                                            val idx = tiles.indexOfFirst { it.id == tile.id }
                                            if (idx >= 0 && idx != tiles.lastIndex) {
                                                tiles.add(tiles.removeAt(idx))
                                            }
                                        },
                                        // Double-tap zooms the image in place
                                        // instead of opening a full-screen page.
                                        onDoubleTap = { zoomState.zoomIn(tile.uri) }
                                    )
                                }
                                .pointerInput(tile.id) {
                                    detectDragGestures(
                                        onDragStart = {
                                            draggingTileId = tile.id
                                            inPinZone = false
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            val idx = tiles.indexOfFirst { it.id == tile.id }
                                            if (idx >= 0) {
                                                val t = tiles[idx]
                                                tiles[idx] = t.copy(
                                                    offsetXPx = (t.offsetXPx + dragAmount.x)
                                                        .coerceIn(0f, (canvasWPx - t.widthPx).coerceAtLeast(0f)),
                                                    offsetYPx = (t.offsetYPx + dragAmount.y)
                                                        .coerceIn(0f, (canvasHPx - t.heightPx).coerceAtLeast(0f))
                                                )
                                                inPinZone = tiles[idx].offsetYPx < pinZoneHeightPx
                                            }
                                        },
                                        onDragEnd = {
                                            draggingTileId = null
                                            if (inPinZone) {
                                                val idx = tiles.indexOfFirst { it.id == tile.id }
                                                if (idx >= 0 && idx != tiles.lastIndex) {
                                                    tiles.add(tiles.removeAt(idx))
                                                }
                                            }
                                            inPinZone = false
                                        },
                                        onDragCancel = {
                                            draggingTileId = null
                                            inPinZone = false
                                        }
                                    )
                                }
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White,
                                shadowElevation = 0.dp,
                                modifier = Modifier
                                    .size(
                                        width = with(density) { tile.widthPx.toDp() },
                                        height = with(density) { tile.heightPx.toDp() }
                                    )
                                    .rotate(tile.rotationDeg)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Image(
                                        painter = moodBoardPainter(tile.uri),
                                        contentDescription = null,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(6.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                    )
                                    Surface(
                                        onClick = { zoomState.zoomIn(tile.uri) },
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
                                }
                            }

                            // × Remove button
                            Surface(
                                onClick = {
                                    val idx = tiles.indexOfFirst { it.id == tile.id }
                                    if (idx >= 0) tiles.removeAt(idx)
                                },
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

                            // ⟲ Rotate button — +15° per tap
                            Surface(
                                onClick = {
                                    val idx = tiles.indexOfFirst { it.id == tile.id }
                                    if (idx >= 0) {
                                        val t = tiles[idx]
                                        tiles[idx] = t.copy(rotationDeg = (t.rotationDeg + 15f) % 360f)
                                    }
                                },
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.55f),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .offset(x = (-4).dp, y = (-4).dp)
                                    .size(22.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    CurioIcon(
                                        name = CurioIcons.Refresh,
                                        contentDescription = "Rotate",
                                        tint = Color.White,
                                        size = 13.dp
                                    )
                                }
                            }

                            // ── Resize buttons (− shrink / + grow) ────────
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .offset(x = (-4).dp, y = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Surface(
                                    onClick = {
                                        val idx = tiles.indexOfFirst { it.id == tile.id }
                                        if (idx >= 0) {
                                            val t = tiles[idx]
                                            val minPx = with(density) { 60.dp.toPx() }
                                            val newW = (t.widthPx * 0.8f).coerceAtLeast(minPx)
                                            val newH = (t.heightPx * 0.8f).coerceAtLeast(minPx)
                                            tiles[idx] = t.copy(
                                                widthPx = newW,
                                                heightPx = newH,
                                                offsetXPx = t.offsetXPx.coerceIn(0f, (canvasWPx - newW).coerceAtLeast(0f)),
                                                offsetYPx = t.offsetYPx.coerceIn(0f, (canvasHPx - newH).coerceAtLeast(0f))
                                            )
                                        }
                                    },
                                    shape = CircleShape,
                                    color = Color.Black.copy(alpha = 0.55f),
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            "−",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                                Surface(
                                    onClick = {
                                        val idx = tiles.indexOfFirst { it.id == tile.id }
                                        if (idx >= 0) {
                                            val t = tiles[idx]
                                            val minPx = with(density) { 60.dp.toPx() }
                                            val newW = (t.widthPx * 1.2f)
                                                .coerceIn(minPx, (canvasWPx - t.offsetXPx).coerceAtLeast(minPx))
                                            val newH = (t.heightPx * 1.2f)
                                                .coerceIn(minPx, (canvasHPx - t.offsetYPx).coerceAtLeast(minPx))
                                            tiles[idx] = t.copy(widthPx = newW, heightPx = newH)
                                        }
                                    },
                                    shape = CircleShape,
                                    color = Color.Black.copy(alpha = 0.55f),
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            "+",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }
                        }
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
                            tint = Color.White,
                            size = 20.dp
                        )
                        Text(
                            text = "Add images",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                    }
                }
            }                // ── Pin-to-front drop zone (appears while dragging) ──────
                if (draggingTileId != null) {
                    val highlight = inPinZone
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
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
                    animatedScale = animatedScale,
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
}

