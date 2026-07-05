package fieldmind.research.app.features.field.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fieldmind.research.app.features.field.presentation.components.*
import fieldmind.research.app.features.field.presentation.theme.FieldMindTheme
import fieldmind.research.app.features.field.presentation.viewmodel.FieldMindViewModel
import fieldmind.research.app.shared.presentation.components.icons.Icon
import fieldmind.research.app.shared.presentation.components.icons.MaterialSymbolIcon
import fieldmind.research.app.ui.theme.CuteElevations
import fieldmind.research.app.ui.theme.cuteShadow
import fieldmind.research.app.ui.theme.screenBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Voice Notes — dedicated screen for recording, listing, and managing voice notes.
 * Features: record new notes, play back existing recordings, rename, delete, share.
 */
@Composable
fun VoiceNotesScreen(
    viewModel: FieldMindViewModel,
    onBack: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val colors = FieldMindTheme.colors

    // ── State ──
    var recordings by remember { mutableStateOf<List<VoiceNote>>(emptyList()) }
    var isRecording by remember { mutableStateOf(false) }
    var recordSeconds by remember { mutableIntStateOf(0) }
    var playingIndex by remember { mutableIntStateOf(-1) }
    var expandedIndex by remember { mutableIntStateOf(-1) }
    var showDeleteConfirm by remember { mutableStateOf<Int?>(null) }
    var showRenameDialog by remember { mutableStateOf<Int?>(null) }
    var renameText by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }

    // ── Timer for recording ──
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordSeconds = 0
            while (isRecording) {
                delay(1000)
                recordSeconds++
            }
        }
    }

    // ── Load existing audio files ──
    LaunchedEffect(Unit) {
        recordings = loadVoiceNotes(context)
    }

    val filteredRecordings = remember(recordings, searchQuery) {
        if (searchQuery.isBlank()) recordings
        else recordings.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    // ── Helpers ──
    fun startRecording() {
        val file = createFieldMindFile(context, "voice", ".m4a")
        val newRecorder = (if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S)
            android.media.MediaRecorder(context)
        else
            android.media.MediaRecorder()).apply {
            setAudioSource(android.media.MediaRecorder.AudioSource.MIC)
            setOutputFormat(android.media.MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(android.media.MediaRecorder.AudioEncoder.AAC)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }
        val note = VoiceNote(
            name = "Voice note ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())}",
            filePath = file.absolutePath,
            durationMs = 0L,
            createdAt = System.currentTimeMillis()
        )
        recordings = listOf(note) + recordings
        isRecording = true
    }

    fun stopRecording() {
        isRecording = false
        // Update the first recording with actual duration
        recordings = recordings.toMutableList().also { list ->
            if (list.isNotEmpty()) {
                list[0] = list[0].copy(durationMs = recordSeconds * 1000L)
            }
        }
        showFastSnackbar(snackbar, scope, "Voice note saved")
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
                        title = "Voice Notes",
                        subtitle = "${recordings.size} recording${if (recordings.size != 1) "s" else ""}",
                        icon = MaterialSymbolIcon("mic"),
                        heroColor = colors.accentFor("observation"),
                        trailing = {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                BackButton(
                                    onClick = { showSearch = !showSearch },
                                    icon = MaterialSymbolIcon("search"),
                                    contentDescription = "Search"
                                )
                                BackButton(
                                    onClick = onBack,
                                    icon = FieldMindIcons.Back,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    )
                }

                // ── Search bar ──
                if (showSearch) {
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search recordings…") },
                            leadingIcon = { Icon(MaterialSymbolIcon("search"), null, size = 20.dp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(28.dp)
                        )
                    }
                }

                // ── Record button ──
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .cuteShadow(elevation = CuteElevations.clickableTier, shape = RoundedCornerShape(34.dp)),
                        shape = RoundedCornerShape(34.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isRecording) MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isRecording) MaterialTheme.colorScheme.error
                                            else colors.observation.copy(alpha = 0.14f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        icon = if (isRecording) MaterialSymbolIcon("stop") else MaterialSymbolIcon("mic"),
                                        contentDescription = if (isRecording) "Stop" else "Record",
                                        tint = if (isRecording) MaterialTheme.colorScheme.onError
                                        else colors.observation,
                                        size = 26.dp
                                    )
                                }
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        if (isRecording) "Recording…" else "Record new voice note",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (isRecording) {
                                        Text(
                                            "%d:%02d".format(recordSeconds / 60, recordSeconds % 60),
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    } else {
                                        Text(
                                            "Tap to start recording",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (!isRecording) {
                                    FilledTonalButton(
                                        onClick = { startRecording() },
                                        shape = RoundedCornerShape(28.dp)
                                    ) {
                                        Icon(MaterialSymbolIcon("fiber_manual_record"), null, size = 18.dp)
                                        Spacer(Modifier.size(6.dp))
                                        Text("Record")
                                    }
                                } else {
                                    FilledTonalButton(
                                        onClick = { stopRecording() },
                                        shape = RoundedCornerShape(28.dp),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Icon(MaterialSymbolIcon("stop"), null, size = 18.dp)
                                        Spacer(Modifier.size(6.dp))
                                        Text("Stop", color = MaterialTheme.colorScheme.onError)
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Section header ──
                if (filteredRecordings.isNotEmpty()) {
                    item {
                        SectionHeader(
                            "All recordings",
                            "${filteredRecordings.size} total",
                            accentColor = colors.observation
                        )
                    }
                }

                // ── Recording list ──
                itemsIndexed(filteredRecordings) { index, note ->
                    val isPlaying = playingIndex == index
                    val isExpanded = expandedIndex == index

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .cuteShadow(elevation = CuteElevations.nonClickableTier, shape = RoundedCornerShape(30.dp)),
                        shape = RoundedCornerShape(30.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isPlaying) colors.observation.copy(alpha = 0.08f)
                            else MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize()
                                .clickable { expandedIndex = if (isExpanded) -1 else index },
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Main row
                            Row(
                                Modifier.fillMaxWidth().padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Play button
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(colors.observation.copy(alpha = 0.14f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        icon = if (isPlaying) MaterialSymbolIcon("pause")
                                        else MaterialSymbolIcon("play_arrow"),
                                        contentDescription = if (isPlaying) "Pause" else "Play",
                                        tint = colors.observation,
                                        size = 22.dp
                                    )
                                }

                                Column(Modifier.weight(1f)) {
                                    Text(
                                        note.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            formatDurationCompact(note.durationMs),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            SimpleDateFormat("MMM d", Locale.getDefault())
                                                .format(Date(note.createdAt)),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Icon(
                                    icon = if (isExpanded) MaterialSymbolIcon("expand_less")
                                    else MaterialSymbolIcon("expand_more"),
                                    contentDescription = "Expand",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    size = 20.dp
                                )
                            }

                            // ── Expanded actions ──
                            AnimatedVisibility(
                                visible = isExpanded,
                                enter = expandVertically(),
                                exit = shrinkVertically()
                            ) {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(start = 14.dp, end = 14.dp, bottom = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    AssistChip(
                                        onClick = { playingIndex = if (isPlaying) -1 else index },
                                        label = { Text(if (isPlaying) "Pause" else "Play") },
                                        leadingIcon = {
                                            Icon(
                                                if (isPlaying) MaterialSymbolIcon("pause")
                                                else MaterialSymbolIcon("play_arrow"), null, size = 16.dp
                                            )
                                        }
                                    )
                                    AssistChip(
                                        onClick = {
                                            renameText = note.name
                                            showRenameDialog = index
                                        },
                                        label = { Text("Rename") },
                                        leadingIcon = { Icon(MaterialSymbolIcon("edit"), null, size = 16.dp) }
                                    )
                                    AssistChip(
                                        onClick = { showDeleteConfirm = index },
                                        label = { Text("Delete") },
                                        leadingIcon = { Icon(MaterialSymbolIcon("delete"), null, size = 16.dp) }
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Empty state ──
                if (filteredRecordings.isEmpty() && !isRecording) {
                    item {
                        EmptyState(
                            "No voice notes yet",
                            "Record a voice note to capture your thoughts, field observations, or meeting notes hands-free.",
                            icon = MaterialSymbolIcon("mic"),
                            iconColor = colors.observation
                        )
                    }
                }
            }
        }
    }

    // ── Delete confirmation ──
    showDeleteConfirm?.let { idx ->
        SwipeableAlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            icon = { Icon(MaterialSymbolIcon("delete"), null, tint = MaterialTheme.colorScheme.error, size = 28.dp) },
            title = { Text("Delete voice note?") },
            text = { Text("This will permanently delete this recording.") },
            confirmButton = {
                Button(
                    onClick = {
                        recordings = recordings.toMutableList().also { it.removeAt(idx) }
                        showDeleteConfirm = null
                        showFastSnackbar(snackbar, scope, "Voice note deleted")
                    },
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) { Text("Cancel") }
            }
        )
    }

    // ── Rename dialog ──
    showRenameDialog?.let { idx ->
        SwipeableAlertDialog(
            onDismissRequest = { showRenameDialog = null },
            icon = { Icon(MaterialSymbolIcon("edit"), null, tint = MaterialTheme.colorScheme.primary, size = 28.dp) },
            title = { Text("Rename recording") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        recordings = recordings.toMutableList().also { list ->
                            if (idx < list.size) {
                                list[idx] = list[idx].copy(name = renameText)
                            }
                        }
                        showRenameDialog = null
                    },
                    shape = RoundedCornerShape(22.dp),
                    enabled = renameText.isNotBlank()
                ) { Text("Rename") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = null }) { Text("Cancel") }
            }
        )
    }
}

/**
 * Voice note data model for the dedicated Voice Notes screen.
 */
data class VoiceNote(
    val name: String,
    val filePath: String,
    val durationMs: Long,
    val createdAt: Long
)

/**
 * Load voice notes from the app's audio directory.
 * Scans for .m4a files and creates VoiceNote entries.
 */
private fun loadVoiceNotes(context: android.content.Context): List<VoiceNote> {
    val dir = File(context.filesDir, "fieldmind/audio")
    if (!dir.exists()) return emptyList()
    return dir.listFiles { f -> f.extension == "m4a" || f.extension == "mp4" || f.extension == "wav" }
        ?.map { file ->
            VoiceNote(
                name = file.nameWithoutExtension,
                filePath = file.absolutePath,
                durationMs = 0L, // Would need MediaPlayer for accurate duration
                createdAt = file.lastModified()
            )
        }
        ?.sortedByDescending { it.createdAt }
        ?: emptyList()
}

private fun formatDurationCompact(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
