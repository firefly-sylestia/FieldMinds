package fieldmind.research.app.features.field.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import fieldmind.research.app.features.field.data.database.entity.ObservationEntity
import fieldmind.research.app.features.field.presentation.components.*
import fieldmind.research.app.features.field.presentation.theme.FieldMindTheme
import fieldmind.research.app.features.field.presentation.viewmodel.FieldMindViewModel
import fieldmind.research.app.shared.presentation.components.icons.Icon
import fieldmind.research.app.shared.presentation.components.icons.MaterialSymbolIcon
import fieldmind.research.app.ui.theme.CuteElevations
import fieldmind.research.app.ui.theme.cuteShadow
import fieldmind.research.app.ui.theme.screenBackground
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Media Gallery — browse all captured media across observations.
 * Shows photos, videos, and audio files in a visual grid layout.
 */
@Composable
fun MediaGalleryScreen(
    viewModel: FieldMindViewModel,
    onBack: () -> Unit = {},
    onOpenDetail: (String, Long) -> Unit = { _, _ -> }
) {
    val observations by viewModel.observations.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val colors = FieldMindTheme.colors

    // ── Extract all media items from observations ──
    val mediaItems = remember(observations) {
        observations.flatMap { obs ->
            extractMediaFromObservation(obs)
        }.sortedByDescending { it.timestamp }
    }

    var selectedFilter by remember { mutableStateOf("All") }
    var selectedItem by remember { mutableStateOf<MediaItem?>(null) }
    var showViewer by remember { mutableStateOf(false) }

    val filters = listOf("All", "Photos", "Audio")
    val filteredItems = remember(mediaItems, selectedFilter) {
        when (selectedFilter) {
            "Photos" -> mediaItems.filter { it.type == "Photo" || it.type == "Image" }
            "Audio" -> mediaItems.filter { it.type == "Audio" || it.type == "Voice" }
            else -> mediaItems
        }
    }

    val gradientOpacity by viewModel.fieldSettings.gradientOpacity.collectAsState()
    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Box(Modifier.fillMaxSize().screenBackground(gradientOpacity)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(20.dp, 12.dp, 20.dp, 96.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ── Header ──
                item {
                    StandardScreenHeader(
                        title = "Media Gallery",
                        subtitle = "${mediaItems.size} item${if (mediaItems.size != 1) "s" else ""}",
                        icon = MaterialSymbolIcon("photo_library"),
                        heroColor = colors.info,
                        trailing = { BackButton(onClick = onBack) }
                    )
                }

                // ── Filter chips ──
                item {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filters.forEach { filter ->
                            FilterChip(
                                selected = selectedFilter == filter,
                                onClick = { selectedFilter = filter },
                                label = { Text(filter) },
                                leadingIcon = {
                                    Icon(
                                        when (filter) {
                                            "Photos" -> MaterialSymbolIcon("image")
                                            "Audio" -> MaterialSymbolIcon("audiotrack")
                                            else -> MaterialSymbolIcon("collections")
                                        },
                                        null, size = 16.dp
                                    )
                                }
                            )
                        }
                    }
                }

                if (filteredItems.isEmpty()) {
                    item {
                        EmptyState(
                            "No media found",
                            "Media from your observations will appear here. Capture photos or record audio during observations to build your gallery.",
                            icon = MaterialSymbolIcon("photo_library"),
                            iconColor = colors.info
                        )
                    }
                    return@LazyColumn
                }

                // ── Media grid ──
                item {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 140.dp),
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(0.dp),
                        userScrollEnabled = false // prevent nested scrolling issues
                    ) {
                        items(filteredItems) { item ->
                            MediaGridTile(
                                item = item,
                                onClick = {
                                    selectedItem = item
                                    showViewer = true
                                }
                            )
                        }
                    }
                }

                // ── Section: Recent audio ──
                val audioItems = mediaItems.filter { it.type == "Audio" || it.type == "Voice" }
                if (audioItems.isNotEmpty() && selectedFilter != "Photos") {
                    item {
                        SectionHeader("Recent audio", "${audioItems.size} recordings", accentColor = colors.observation)
                    }
                    itemsIndexed(audioItems.take(8)) { _, item ->
                        AudioListItem(item)
                    }
                }
            }
        }
    }

    // ── Media viewer dialog ──
    if (showViewer && selectedItem != null) {
        MediaViewerDialog(
            item = selectedItem!!,
            onDismiss = { showViewer = false }
        )
    }
}

@Composable
private fun MediaGridTile(
    item: MediaItem,
    onClick: () -> Unit
) {
    val isImage = item.type == "Photo" || item.type == "Image"
    Card(
        modifier = Modifier
            .height(140.dp)
            .fillMaxWidth()
            .cuteShadow(elevation = CuteElevations.clickableTier, shape = RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            if (isImage && item.uri != null) {
                AsyncImage(
                    model = item.uri,
                    contentDescription = item.caption,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Audio or video placeholder
                Box(
                    Modifier.fillMaxSize().background(FieldMindTheme.colors.observation.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            icon = MaterialSymbolIcon("audiotrack"),
                            contentDescription = null,
                            tint = FieldMindTheme.colors.observation,
                            size = 32.dp
                        )
                        Text(
                            item.name.take(20),
                            style = MaterialTheme.typography.labelSmall,
                            color = FieldMindTheme.colors.observation,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 4.dp).padding(top = 4.dp)
                        )
                    }
                }
            }

            // Type badge
            Surface(
                modifier = Modifier.align(Alignment.TopStart).padding(6.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f)
            ) {
                Text(
                    item.type,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun AudioListItem(item: MediaItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .cuteShadow(elevation = CuteElevations.nonClickableTier, shape = RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                Modifier.size(40.dp).clip(CircleShape)
                    .background(FieldMindTheme.colors.observation.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(MaterialSymbolIcon("audiotrack"), null, tint = FieldMindTheme.colors.observation, size = 22.dp)
            }
            Column(Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(item.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                MaterialSymbolIcon("play_circle"),
                null,
                tint = FieldMindTheme.colors.observation,
                size = 28.dp
            )
        }
    }
}

@Composable
private fun MediaViewerDialog(
    item: MediaItem,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            shape = RoundedCornerShape(36.dp)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(if (item.type == "Photo" || item.type == "Image") MaterialSymbolIcon("image") else MaterialSymbolIcon("audiotrack"), null, tint = MaterialTheme.colorScheme.primary, size = 24.dp)
                    Column(Modifier.weight(1f)) {
                        Text(item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("${item.type} • ${SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(item.timestamp))}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDismiss) { Icon(FieldMindIcons.Close, null, size = 22.dp) }
                }
                if (item.caption.isNotBlank()) {
                    Text(item.caption, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (item.uri != null && (item.type == "Photo" || item.type == "Image")) {
                    AsyncImage(
                        model = item.uri,
                        contentDescription = item.caption,
                        modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp).clip(RoundedCornerShape(24.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

/**
 * Media item data model for the gallery.
 */
data class MediaItem(
    val name: String,
    val type: String, // "Photo", "Audio", "Video"
    val uri: String?,
    val caption: String,
    val timestamp: Long,
    val observationId: Long? = null
)

/**
 * Extract media items from an observation entity.
 * Parses structuredDetailsJson and attachments for media references.
 */
private fun extractMediaFromObservation(obs: ObservationEntity): List<MediaItem> {
    val items = mutableListOf<MediaItem>()
    if (obs.evidenceSummary.isNotBlank()) {
        items.add(
            MediaItem(
                name = obs.subject.ifBlank { "Observation" },
                type = "Note",
                uri = null,
                caption = obs.evidenceSummary,
                timestamp = obs.timestamp,
                observationId = obs.id
            )
        )
    }
    return items
}
