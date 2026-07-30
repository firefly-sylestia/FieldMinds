package com.curio.app.features.capture.formats

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.zIndex
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import coil.compose.rememberAsyncImagePainter
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
 *
 * Tiles are placed with random initial positions, slight rotations, and
 * varying sizes to create a natural mood-board collage aesthetic.
 */
@Composable
fun GalleryWallFormat(
    accent: Color,
    tint: Color,
    onCanSaveChange: (Boolean) -> Unit,
    onDataChanged: (CaptureData?) -> Unit = {}
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val tiles = remember { mutableStateListOf<MoodTile>() }
    var nextId by remember { mutableStateOf(0) }
    var caption by remember { mutableStateOf("") }
    var canvasWPx by remember { mutableStateOf(0f) }
    val canvasHPx = with(density) { 420.dp.toPx() }

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
                    id = nextId++,
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

    val canSave = tiles.isNotEmpty()
    LaunchedEffect(canSave, caption, tiles.size) {
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
        // Mood board canvas — uses BoxWithConstraints for real width
        // ═══════════════════════════════════════════════════════════════
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            tonalElevation = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                // Read actual canvas width from constraints (fixes "stuck on left" bug)
                LaunchedEffect(maxWidth) {
                    canvasWPx = with(density) { maxWidth.toPx() }
                }

                Box(modifier = Modifier.fillMaxSize()) {
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
                                        detectTapGestures {
                                            val idx = tiles.indexOfFirst { it.id == tile.id }
                                            if (idx >= 0 && idx != tiles.lastIndex) {
                                                tiles.add(tiles.removeAt(idx))
                                            }
                                        }
                                    }
                                    .pointerInput(tile.id) {
                                        detectDragGestures { change, dragAmount ->
                                            change.consume()
                                            val idx = tiles.indexOfFirst { it.id == tile.id }
                                            if (idx >= 0) {
                                                val t = tiles[idx]
                                                tiles[idx] = t.copy(
                                                    offsetXPx = (t.offsetXPx + dragAmount.x)
                                                        .coerceIn(0f, canvasWPx - t.widthPx),
                                                    offsetYPx = (t.offsetYPx + dragAmount.y)
                                                        .coerceIn(0f, canvasHPx - t.heightPx)
                                                )
                                            }
                                        }
                                    }
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color.White,
                                    shadowElevation = 6.dp,
                                    modifier = Modifier
                                        .size(
                                            width = with(density) { tile.widthPx.toDp() },
                                            height = with(density) { tile.heightPx.toDp() }
                                        )
                                        .rotate(tile.rotationDeg)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Image(
                                            painter = rememberAsyncImagePainter(tile.uri),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(14.dp))
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                            .background(
                                                Color.Black.copy(alpha = 0.08f),
                                                    RoundedCornerShape(14.dp)
                                                )
                                        )
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
                            }
                        }
                    }

                    // ── Floating "+" add button ──────────────────────────
                    Surface(
                        onClick = { imagePicker.launch(arrayOf("image/*")) },
                        shape = RoundedCornerShape(28.dp),
                        color = accent,
                        shadowElevation = 8.dp,
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
                                tint = CurioColors.DeepPlum,
                                size = 20.dp
                            )
                            Text(
                                text = "Add images",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = CurioColors.DeepPlum
                            )
                        }
                    }
                }
            }
        }

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
}
