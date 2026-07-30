package com.curio.app.features.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavController
import com.curio.app.data.CaptureData
import com.curio.app.data.CaptureFormat
import com.curio.app.data.CategoryId
import com.curio.app.data.CurioCategories
import com.curio.app.data.CurioCategory
import com.curio.app.data.CurioEntry
import com.curio.app.data.CurioRepositoryHolder
import com.curio.app.data.TopicCatalog
import com.curio.app.navigation.CurioRoutes
import com.curio.app.ui.components.MorphEntrance
import com.curio.app.ui.components.StaggeredEntrance
import com.curio.app.ui.components.StaggeredItem
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioGradients
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import kotlinx.coroutines.launch

/**
 * Entry Detail — see CURIO_SPEC.md §10. Framed presentation of a saved capture.
 *
 * Upgraded with:
 *  - Room database persistence (loads from CaptureRepository)
 *  - Structured CaptureData rendering per format
 *  - MorphEntrance for hero image + StaggeredEntrance for metadata
 *  - Delete functionality with Room
 */
@Composable
fun EntryDetailScreen(entryId: String, navController: NavController) {
    val scope = rememberCoroutineScope()
    val entry by produceState<CurioEntry?>(initialValue = null, entryId) {
        value = CurioRepositoryHolder.repo.getById(entryId)
            ?: TopicCatalog.sampleEntries().find { it.id == entryId }
    }

    LaunchedEffect(entry) {
        if (entry == null) {
            kotlinx.coroutines.delay(400)
            if (entry == null) navController.popBackStack()
        }
    }

    val resolvedEntry = entry ?: return
    val cat = CurioCategories.byId(resolvedEntry.topic.categoryId)
    var menuExpanded by remember { mutableStateOf(false) }
    var deleteDialogVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Hero image placeholder ──────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    if (cat.id == CategoryId.WILDCARD)
                        Brush.horizontalGradient(CurioGradients.WildcardGradientStops)
                    else Brush.verticalGradient(listOf(cat.accent, cat.tint))
                ),
            contentAlignment = Alignment.Center
        ) {
            CurioIcon(
                name = cat.iconGlyph,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.9f),
                size = 96.dp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = { navController.popBackStack() },
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.25f)
                ) {
                    CurioIcon(
                        name = CurioIcons.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        size = 24.dp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Box {
                    Surface(
                        onClick = { menuExpanded = true },
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.25f)
                    ) {
                        CurioIcon(
                            name = CurioIcons.MoreVert,
                            contentDescription = "More",
                            tint = Color.White,
                            size = 24.dp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Share") },
                            onClick = { menuExpanded = false },
                            leadingIcon = { CurioIcon(name = CurioIcons.Share, contentDescription = null, size = 20.dp) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                menuExpanded = false
                                deleteDialogVisible = true
                            },
                            leadingIcon = {
                                CurioIcon(name = CurioIcons.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, size = 20.dp)
                            }
                        )
                    }
                }
            }
        }

        // ── Topic meta ─────────────────────────────────────────────────
        MorphEntrance {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = resolvedEntry.topic.name,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = RoundedCornerShape(12.dp), color = cat.tint) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            CurioIcon(name = cat.iconGlyph, contentDescription = null, tint = cat.accent, size = 14.dp)
                            Text(text = cat.displayName, style = MaterialTheme.typography.labelMedium, color = cat.accent)
                        }
                    }
                    if (resolvedEntry.title != null) {
                        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                            Text(
                                text = resolvedEntry.title!!,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                Text(
                    text = when (resolvedEntry.capturedAtDaysAgo) {
                        0 -> "Captured today"
                        1 -> "Captured yesterday"
                        else -> "Captured ${resolvedEntry.capturedAtDaysAgo}d ago"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ── Format body ────────────────────────────────────────────────
        StaggeredEntrance {
            StaggeredItem(index = 0) {
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    FormatBody(entry = resolvedEntry, category = cat)
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }

    if (deleteDialogVisible) {
        AlertDialog(
            onDismissRequest = { deleteDialogVisible = false },
            title = { Text("Delete this entry?") },
            text = { Text("This capture will be permanently removed from your Cabinet.") },
            confirmButton = {
                TextButton(onClick = {
                    deleteDialogVisible = false
                    scope.launch {
                        CurioRepositoryHolder.repo.deleteById(resolvedEntry.id)
                        navController.popBackStack()
                    }
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deleteDialogVisible = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun FormatBody(entry: CurioEntry, category: CurioCategory) {
    when (entry.format) {
        CaptureFormat.SoundBite -> SoundBiteRender(entry, category)
        CaptureFormat.ReelNotes -> ReelNotesRender(entry, category)
        CaptureFormat.Marginalia -> MarginaliaRender(entry, category)
        CaptureFormat.GalleryWall -> GalleryWallRender(entry, category)
        CaptureFormat.FieldNotes -> FieldNotesRender(entry, category)
        CaptureFormat.OpenNotebook -> OpenNotebookRender(entry, category)
    }
}

// ── Per-format render composables ─────────────────────────────────────

@Composable
private fun SoundBiteRender(entry: CurioEntry, category: CurioCategory) {
    val data = entry.captureData as? CaptureData.SoundBite ?: return
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = category.tint,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = category.accent
                ) {
                    CurioIcon(
                        name = CurioIcons.PlayArrow,
                        contentDescription = "Play",
                        tint = CurioColors.DeepPlum,
                        size = 32.dp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Voice note · ${data.durationSeconds}s",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (data.title.isNotBlank()) {
                        Text(
                            data.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── Real audio player (when file path is available) ─────────
            if (!data.audioFilePath.isNullOrBlank()) {
                AudioPlayerBar(
                    audioFilePath = data.audioFilePath!!,
                    accent = category.accent,
                    tint = category.tint
                )
            }
        }
    }
}

/**
 * Compact ExoPlayer-based audio playback bar with play/pause + seek slider.
 * Handles player lifecycle automatically via [DisposableEffect].
 */
@Composable
private fun AudioPlayerBar(
    audioFilePath: String,
    accent: Color,
    tint: Color
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var sliderPosition by remember { mutableFloatStateOf(0f) }

    val player = remember {
        ExoPlayer.Builder(context.applicationContext).build().apply {
            setMediaItem(MediaItem.fromUri(audioFilePath))
            prepare()
            playWhenReady = false
        }
    }

    // ── Observe player state ────────────────────────────────────────────
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_READY -> {
                        duration = player.duration.coerceAtLeast(0)
                    }
                    Player.STATE_ENDED -> {
                        isPlaying = false
                        currentPosition = 0L
                        sliderPosition = 0f
                    }
                }
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }

    // ── Poll position while playing ─────────────────────────────────────
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPosition = player.currentPosition.coerceAtLeast(0)
            sliderPosition = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f
            kotlinx.coroutines.delay(200)
        }
    }

    // ── Seek slider + play/pause ────────────────────────────────────────
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Slider(
            value = sliderPosition,
            onValueChange = { fraction ->
                sliderPosition = fraction
                val seekMs = (fraction * duration).toLong()
                player.seekTo(seekMs)
                currentPosition = seekMs
            },
            colors = SliderDefaults.colors(
                thumbColor = accent,
                activeTrackColor = accent,
                inactiveTrackColor = tint
            ),
            modifier = Modifier.fillMaxWidth().height(24.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    onClick = {
                        if (isPlaying) player.pause() else player.play()
                    },
                    shape = RoundedCornerShape(50),
                    color = accent
                ) {
                    CurioIcon(
                        name = if (isPlaying) CurioIcons.Pause else CurioIcons.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = CurioColors.DeepPlum,
                        size = 28.dp,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }

            // Time readout
            Text(
                text = "${formatMs(currentPosition)} / ${formatMs(duration)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Format milliseconds to mm:ss */
private fun formatMs(ms: Long): String {
    val totalSecs = (ms / 1000).coerceAtLeast(0)
    val mins = totalSecs / 60
    val secs = totalSecs % 60
    return "%d:%02d".format(mins, secs)
}

@Composable
private fun ReelNotesRender(entry: CurioEntry, category: CurioCategory) {
    val data = entry.captureData as? CaptureData.ReelNotes ?: return
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (data.rating > 0) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(data.rating) { CurioIcon(CurioIcons.Star, null, tint = category.accent, size = 22.dp) }
                repeat(5 - data.rating) { CurioIcon(CurioIcons.StarOutline, null, tint = MaterialTheme.colorScheme.outline, size = 22.dp) }
            }
        }
        if (data.imageCount > 0) {
            Text("${data.imageCount} image${if (data.imageCount != 1) "s" else ""} attached", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (data.reviewText.isNotBlank()) {
            Text(data.reviewText, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun MarginaliaRender(entry: CurioEntry, category: CurioCategory) {
    val data = entry.captureData as? CaptureData.Marginalia ?: return
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (data.journalText.isNotBlank()) {
            Surface(shape = RoundedCornerShape(20.dp), color = category.tint, modifier = Modifier.fillMaxWidth()) {
                Text(data.journalText, modifier = Modifier.padding(20.dp), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        data.quotes.filter { it.isNotBlank() }.forEach { quote ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = category.tint,
                modifier = Modifier.fillMaxWidth().rotate(if (data.quotes.indexOf(quote) % 2 == 0) 1.5f else -1.5f)
            ) {
                Text("\"$quote\"", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic), color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun GalleryWallRender(entry: CurioEntry, category: CurioCategory) {
    val data = entry.captureData as? CaptureData.GalleryWall ?: return
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (data.imageCount > 0) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f).height(120.dp).background(category.accent, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                    CurioIcon(CurioIcons.Image, null, tint = Color.White, size = 36.dp)
                }
                if (data.imageCount > 1) {
                    Box(modifier = Modifier.weight(1f).height(120.dp).background(category.tint, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                        CurioIcon(CurioIcons.Image, null, tint = category.accent, size = 36.dp)
                    }
                }
            }
            if (data.imageCount > 2) {
                Text("+${data.imageCount - 2} more", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (data.caption.isNotBlank()) {
            Text(data.caption, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun FieldNotesRender(entry: CurioEntry, category: CurioCategory) {
    val data = entry.captureData as? CaptureData.FieldNotes ?: return
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        data.observed.takeIf { it.isNotBlank() }?.let { text ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("🔍 Observed", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = category.accent)
                Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        data.surprised.takeIf { it.isNotBlank() }?.let { text ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("✨ Surprised me", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = category.accent)
                Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        data.learnNext.takeIf { it.isNotBlank() }?.let { text ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("📖 Want to learn next", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = category.accent)
                Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun OpenNotebookRender(entry: CurioEntry, category: CurioCategory) {
    val data = entry.captureData as? CaptureData.OpenNotebook ?: return
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Format: ${data.subFormat.name}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        // Recursively render the sub-data
        val subEntry = CurioEntry(
            id = entry.id,
            topic = entry.topic,
            format = data.subFormat,
            captureData = data.subData
        )
        FormatBody(entry = subEntry, category = category)
    }
}
