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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import coil.compose.rememberAsyncImagePainter
import kotlin.math.roundToInt

/**
 * Gallery Wall format body — CURIO_SPEC §8.4 (Visual Art / Painters).
 *
 * Freeform canvas where image tiles are positioned with random initial
 * offsets + slight rotations to give a hand-placed collage feel. Each
 * tile has a small "x" remove button in its top-right corner; tapping
 * the tile body brings it to the front (z-order = list-render-order,
 * last in list = on top).
 *
 * Per spec §8.4:
 *   - Min 1 image required to save ([onCanSaveChange] fires true once
 *     at least one tile exists)
 *   - Soft nudge past 8 images ("Getting full! Consider saving this
 *     one and starting a new board")
 *   - Single-line caption field below the canvas
 *
 * Phase 4 will add drag-to-reposition + pinch-to-resize gestures; for
 * the design phase, the static initial layout is the "composition" the
 * user starts from and the remove / bring-to-front controls let them
 * shape it.
 */
@Composable
fun GalleryWallFormat(
    accent: Color,
    tint: Color,
    onCanSaveChange: (Boolean) -> Unit,
    onDataChanged: (CaptureData?) -> Unit = {}
) {
    val context = LocalContext.current
    val tiles = remember { mutableStateListOf<TileData>() }
    var caption by remember { mutableStateOf("") }
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
            tiles.add(
                TileData(
                    uri = uri.toString(),
                    offsetXDp = (10..120).random().toFloat(),
                    offsetYDp = (10..160).random().toFloat(),
                    rotationDeg = (-10..10).random().toFloat(),
                    sizeDp = (90..140).random().toFloat()
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
                imageUris = tiles.map { it.uri }
            )
            else null
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Soft nudge past 8 images (CURIO_SPEC §8.4) ──────────────────────
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
                        text = "Getting full! Consider saving this one and starting a new board.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }

        // ── Freeform canvas ────────────────────────────────────────────────
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (tiles.isEmpty()) {
                    // Empty state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CurioIcon(
                                name = CurioIcons.Image,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                size = 48.dp
                            )
                            Text(
                                text = "Add your first image to start the board",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    // Positioned tiles (later in list = rendered on top)
                    tiles.forEachIndexed { i, tile ->
                        Box(
                            modifier = Modifier
                                .offset { IntOffset(tile.offsetXDp.roundToInt(), tile.offsetYDp.roundToInt()) }
                                .rotate(tile.rotationDeg)
                                .size(tile.sizeDp.dp)
                                .zIndex(i.toFloat())
                                .pointerInput(tile.uri) {
                                    detectTapGestures {
                                        // Bring to front: move to end of list
                                        val idx = tiles.indexOfFirst { it.uri == tile.uri }
                                        if (idx >= 0 && idx != tiles.lastIndex) {
                                            val moved = tiles.removeAt(idx)
                                            tiles.add(moved)
                                        }
                                    }
                                }
                                .pointerInput(tile.uri) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        val idx = tiles.indexOfFirst { it.uri == tile.uri }
                                        if (idx >= 0) {
                                            val t = tiles[idx]
                                            tiles[idx] = t.copy(
                                                offsetXDp = (t.offsetXDp + dragAmount.x).coerceIn(0f, 240f),
                                                offsetYDp = (t.offsetYDp + dragAmount.y).coerceIn(0f, 260f)
                                            )
                                        }
                                    }
                                }
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = accent,
                                shadowElevation = 4.dp,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(tile.uri),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                            // Tiny remove button in top-right corner of tile
                            Surface(
                                onClick = { tiles.removeAt(i) },
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(2.dp)
                                    .size(20.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    CurioIcon(
                                        name = CurioIcons.Close,
                                        contentDescription = "Remove image",
                                        tint = Color.White,
                                        size = 14.dp
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Floating "+ Add" button (bottom-right) ───────────────────
                Surface(
                    onClick = {
                        imagePicker.launch(arrayOf("image/*"))
                    },
                    shape = CircleShape,
                    color = accent,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = 14.dp,
                            vertical = 8.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CurioIcon(
                            name = CurioIcons.Add,
                            contentDescription = "Add image",
                            tint = CurioColors.DeepPlum,
                            size = 18.dp
                        )
                        Text(
                            text = "Add",
                            style = MaterialTheme.typography.labelMedium,
                            color = CurioColors.DeepPlum
                        )
                    }
                }
            }
        }

        // ── Caption field (CURIO_SPEC §8.4) ─────────────────────────────────
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

/** Per-tile positioning + size metadata. */
private data class TileData(
    val uri: String,
    val offsetXDp: Float,
    val offsetYDp: Float,
    val rotationDeg: Float,
    val sizeDp: Float
)