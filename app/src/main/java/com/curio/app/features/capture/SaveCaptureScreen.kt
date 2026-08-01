package com.curio.app.features.capture

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.curio.app.data.AudioStorageManager
import com.curio.app.data.CaptureData
import com.curio.app.data.CaptureFormat
import com.curio.app.data.CaptureRepository
import com.curio.app.data.CategoryId
import com.curio.app.data.CurioCategories
import com.curio.app.data.CurioCategory
import com.curio.app.data.CurioEntry
import com.curio.app.data.CurioRepositoryHolder
import com.curio.app.data.CurioTopic
import com.curio.app.data.StreakTracker
import com.curio.app.data.TopicCatalog
import com.curio.app.data.TopicJsonLoader
import com.curio.app.features.capture.formats.FieldNotesFormat
import com.curio.app.features.capture.formats.GalleryWallFormat
import com.curio.app.features.capture.formats.MarginaliaFormat
import com.curio.app.features.capture.formats.OpenNotebookFormat
import com.curio.app.features.capture.formats.ReelNotesFormat
import com.curio.app.features.capture.formats.SoundBiteFormat
import com.curio.app.navigation.CurioRoutes
import com.curio.app.ui.components.CurioBackButton
import com.curio.app.ui.components.ConfettiBurst
import com.curio.app.ui.components.EmberBurst
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.CurioMotion
import com.curio.app.ui.theme.categoryInk
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Save / Capture — see CURIO_SPEC.md §8.
 *
 * Premium redesign with:
 *  - Structured data collection via [CaptureData] callbacks from each format
 *  - Room database persistence via [CaptureRepository]
 *  - Dual confetti + ember burst on save success
 *  - Format body renders instantly (no entrance delay)
 *  - Proper back-navigation with discard confirmation
 *
 * When [editEntryId] is set (mood-board edit mode), the screen preloads the
 * saved entry's data into the format body and saves changes back in place
 * (same id → Room REPLACE), then returns to the live-updating detail screen.
 */
@Composable
fun SaveCaptureScreen(
    categorySlug: String,
    topicName: String,
    navController: NavController,
    editEntryId: String? = null
) {
    val context = LocalContext.current

    // Edit mode: load the saved entry so its data can prefill the format.
    val editingEntry by produceState<CurioEntry?>(initialValue = null, editEntryId) {
        value = editEntryId?.let { id ->
            runCatching { CurioRepositoryHolder.repo.getById(id) }.getOrNull()
        }
    }

    // Always call remember (stable slot) — the entry-driven category only
    // applies in edit mode.
    val fallbackCat = remember(categorySlug) {
        CurioCategories.byRouteSlug(categorySlug)
            ?: CurioCategories.byId(CategoryId.WILDCARD)
    }
    val cat = editingEntry?.let { CurioCategories.byId(it.topic.categoryId) } ?: fallbackCat

    val topic by produceState<CurioTopic?>(initialValue = null, topicName, cat.id, editingEntry) {
        val existing = editingEntry
        if (existing != null) {
            value = existing.topic
            return@produceState
        }
        val cached = TopicCatalog.findByName(topicName)
        if (cached != null) {
            value = cached
            return@produceState
        }
        val pool = TopicJsonLoader.load(cat.id)
        // Graceful fallback: an unknown topic stays null so the save CTA
        // stays disabled instead of silently capturing the wrong topic.
        value = pool.firstOrNull { it.name == topicName }
    }

    var canSave by remember { mutableStateOf(false) }
    var currentCaptureData by remember { mutableStateOf<CaptureData?>(null) }
    var saveInProgress by remember { mutableStateOf(false) }
    var confettiTrigger by remember { mutableIntStateOf(0) }
    var emberTrigger by remember { mutableIntStateOf(0) }
    var savedEntryId by remember { mutableStateOf<String?>(null) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // ── Handle save ─────────────────────────────────────────────────────
    val performSave: () -> Unit = {
        val data = currentCaptureData
        if (data != null) {
            saveInProgress = true
            scope.launch {
                val entryId = editEntryId ?: CaptureRepository.createId()

                // Persist audio file from cache to internal storage before saving
                val persistedData = if (data is CaptureData.SoundBite && !data.audioFilePath.isNullOrBlank()) {
                    val result = AudioStorageManager.persistAudio(
                        context, data.audioFilePath, entryId
                    )
                    data.copy(
                        audioFilePath = result.persistentPath,
                        fileSizeBytes = result.fileSizeBytes
                    )
                } else {
                    data
                }

                val resolvedTopic = topic
                if (resolvedTopic == null) {
                    saveInProgress = false
                    return@launch
                }

                // Local capture: editingEntry is a delegated property (produceState),
                // so the compiler can't smart-cast it — grab a stable local first.
                val existingEntry = editingEntry
                val entry = if (existingEntry != null) {
                    // Edit mode: keep id/topic/title/timestamp, swap the data.
                    existingEntry.copy(captureData = persistedData)
                } else {
                    CurioEntry(
                        id = entryId,
                        topic = resolvedTopic,
                        format = cat.defaultFormat,
                        captureData = persistedData
                    )
                }
                runCatching { CurioRepositoryHolder.repo.save(entry) }
                    .onSuccess {
                        savedEntryId = entry.id
                        StreakTracker.recordActivity(context)
                        delay(400)
                        confettiTrigger++
                        emberTrigger++
                    }
                saveInProgress = false
            }
        }
    }

    // ── Navigate after save celebration ─────────────────────────────────
    LaunchedEffect(confettiTrigger) {
        if (confettiTrigger > 0) {
            delay(800)
            savedEntryId?.let { id ->
                if (editEntryId != null) {
                    // Edit mode: return to the detail screen — it observes the
                    // repository flow, so the updated board renders live.
                    navController.popBackStack()
                } else {
                    navController.navigate(CurioRoutes.entryDetail(id)) {
                        popUpTo(CurioRoutes.HOME)
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Premium top bar ──────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CurioBackButton(
                onClick = {
                    if (canSave) showDiscardDialog = true
                    else navController.popBackStack()
                }
            )
            Text(
                text = if (editEntryId != null) "Edit mood board" else "Save your take",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.width(40.dp)) // balance the back button
        }

        // ── Topic reminder strip with gradient ───────────────────────────
        Surface(
            color = cat.tint,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = cat.accent.copy(alpha = 0.15f)
                ) {
                    CurioIcon(
                        name = cat.iconGlyph,
                        contentDescription = null,
                        tint = cat.categoryInk(),
                        size = 22.dp,
                        modifier = Modifier.padding(6.dp)
                    )
                }
                Column {
                    Text(
                        text = topic?.name ?: "Loading…",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = cat.categoryInk()
                    )
                    Text(
                        text = cat.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = cat.categoryInk().copy(alpha = 0.7f)
                    )
                }
            }
        }

        // ── Scrollable format body ───────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                    FormatBodyForCategory(
                        category = cat,
                        initialData = editingEntry?.captureData,
                        // Reuse the saved entry's id-derived seed so the editor's
                        // watermark pattern matches the saved view exactly.
                        boardSeed = editEntryId?.hashCode(),
                        onCanSaveChange = { canSave = it && topic != null },
                        onDataChanged = { currentCaptureData = it }
                    )
            }
        }

        // ── Sticky bottom Save CTA with gradient edge ────────────────────
        Surface(
            color = MaterialTheme.colorScheme.background,
            shadowElevation = 0.dp,
            tonalElevation = 2.dp
        ) {
            Box {
                // Top gradient edge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    cat.tint,
                                    cat.accent.copy(alpha = 0.3f),
                                    cat.tint
                                )
                            )
                        )
                )
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Button(
                        onClick = performSave,
                        enabled = canSave && !saveInProgress,
                        shape = RoundedCornerShape(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = cat.accent,
                            contentColor = Color.White,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        contentPadding = PaddingValues(vertical = 18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .scale(if (saveInProgress) 0.97f else 1f)
                    ) {
                        if (saveInProgress) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "Saving…",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        } else {
                            CurioIcon(
                                name = CurioIcons.Check,
                                contentDescription = null,
                                tint = Color.White,
                                size = 20.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (editEntryId != null) "Save changes" else "Save entry",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }
            }
        }
    }

    // ── Discard confirmation dialog ─────────────────────────────────────
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard this capture?") },
            text = { Text("You'll lose what you've added here. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    navController.popBackStack()                }) {
                    Text("Discard", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Keep editing")
                }
            }
        )
    }

    // ── Confetti + ember celebration ────────────────────────────────────
    if (confettiTrigger > 0) {
        ConfettiBurst(
            colors = listOf(cat.accent, cat.tint, CurioColors.ButterYellow),
            trigger = confettiTrigger,
            particleCount = CurioMotion.ConfettiParticleCountLarge,
            modifier = Modifier.fillMaxSize(),
            onComplete = {}
        )
    }
    if (emberTrigger > 0) {
        EmberBurst(
            colors = listOf(cat.accent, CurioColors.ButterYellow),
            trigger = emberTrigger,
            particleCount = 12,
            modifier = Modifier.fillMaxSize(),
            onComplete = {}
        )
    }
}

/**
 * Dispatch the right format body based on the active category's default format.
 * Each format now reports both [onCanSaveChange] and [onDataChanged].
 *
 * [initialData] (edit mode) is passed through to [GalleryWallFormat] so a
 * saved mood board can preload its tiles and caption; [boardSeed] (edit
 * mode only) is the saved entry's id hash so the editor's watermark pattern
 * matches the saved view.
 */
@Composable
private fun FormatBodyForCategory(
    category: CurioCategory,
    onCanSaveChange: (Boolean) -> Unit,
    onDataChanged: (CaptureData?) -> Unit,
    initialData: CaptureData? = null,
    boardSeed: Int? = null
) {
    when (category.defaultFormat) {
        CaptureFormat.SoundBite ->
            SoundBiteFormat(category.accent, category.tint, onCanSaveChange, onDataChanged)
        CaptureFormat.ReelNotes ->
            ReelNotesFormat(category.accent, category.tint, onCanSaveChange, onDataChanged)
        CaptureFormat.Marginalia ->
            MarginaliaFormat(category.accent, category.tint, onCanSaveChange, onDataChanged)
        CaptureFormat.GalleryWall ->
            GalleryWallFormat(
                category.accent, category.tint, onCanSaveChange, onDataChanged,
                initialData = initialData as? CaptureData.GalleryWall,
                boardSeed = boardSeed
            )
        CaptureFormat.FieldNotes ->
            FieldNotesFormat(category.accent, category.tint, onCanSaveChange, onDataChanged)
        CaptureFormat.OpenNotebook ->
            OpenNotebookFormat(category.accent, category.tint, onCanSaveChange, onDataChanged)
    }
}
