package com.curio.app.features.capture

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.curio.app.data.AppPreferences
import com.curio.app.data.AudioStorageManager
import com.curio.app.data.CaptureData
import com.curio.app.data.CaptureFormat
import com.curio.app.data.CaptureRepository
import com.curio.app.data.NotePaperStyle
import com.curio.app.data.CategoryId
import com.curio.app.data.CurioCategories
import com.curio.app.data.CurioCategory
import com.curio.app.data.CurioEntry
import com.curio.app.data.CurioRepositoryHolder
import com.curio.app.data.CurioTopic
import com.curio.app.data.StreakTracker
import com.curio.app.data.TopicCatalog
import com.curio.app.data.TopicJsonLoader
import com.curio.app.data.shortName
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
import com.curio.app.ui.components.formatGlyph
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.CurioMotion
import com.curio.app.ui.theme.categoryBackgroundWash
import com.curio.app.ui.theme.categoryBorder
import com.curio.app.ui.theme.categoryInk
import com.curio.app.ui.theme.categorySurface
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
 * When [editEntryId] is set (edit mode — a single mood board or a whole
 * multi-section Portfolio), the screen preloads the saved entry's data into
 * the format body and saves changes back in place (same id → Room REPLACE),
 * then returns to the live-updating detail screen.
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
    // Falls back to a sample entry (not in the DB) so preview boards can be
    // edited too — saving then persists a real copy of the board.
    val editingEntry by produceState<CurioEntry?>(initialValue = null, editEntryId) {
        value = editEntryId?.let { id ->
            runCatching { CurioRepositoryHolder.repo.getById(id) }.getOrNull()
                ?: TopicCatalog.sampleEntries().find { it.id == id }
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

    // System back while editing → the same three-way leave dialog as the
    // top-bar back button (save and switch / keep editing / discard).
    BackHandler(enabled = canSave) {
        showDiscardDialog = true
    }

    val scope = rememberCoroutineScope()

    // ── Handle save ─────────────────────────────────────────────────────
    val performSave: () -> Unit = {
        val data = currentCaptureData
        if (data != null) {
            saveInProgress = true
            scope.launch {
                val entryId = editEntryId ?: CaptureRepository.createId()

                // Persist audio file from cache to internal storage before
                // saving — recurses through Portfolio/OpenNotebook so every
                // SoundBite section gets a stable path, not just top-level.
                val persistedData = persistAudioDeep(context, data, entryId)

                val resolvedTopic = topic
                if (resolvedTopic == null) {
                    saveInProgress = false
                    return@launch
                }

                // Local capture: editingEntry is a delegated property (produceState),
                // so the compiler can't smart-cast it — grab a stable local first.
                val existingEntry = editingEntry
                // Edit mode must NEVER write a fresh entry: Room REPLACEs by id,
                // so a fresh entry here would overwrite the original with blank
                // data. If the source entry is somehow missing, abort instead.
                if (editEntryId != null && existingEntry == null) {
                    saveInProgress = false
                    return@launch
                }
                val entry = if (existingEntry != null) {
                    // Edit mode: keep id/topic/title/timestamp, swap the data.
                    existingEntry.copy(
                        format = formatOf(persistedData),
                        captureData = persistedData
                    )
                } else {
                    CurioEntry(
                        id = entryId,
                        topic = resolvedTopic,
                        // A single section stores its own format; a Portfolio
                        // entry uses its first section's format so Cabinet glyph
                        // and detail dispatch stay correct.
                        format = formatOf(persistedData),
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
            // Category tint wash — the capture screen wears a faint wash of
            // the active category over the theme background, matching the
            // Spin page so saving stays in the same color story. Theme-aware:
            // deep accent over cream in light, pastel twin glow over midnight
            // in dark (deep accents look muddy on dark).
            .background(cat.categoryBackgroundWash())
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
                text = if (editEntryId != null) "Edit entry" else "Save your take",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.width(40.dp)) // balance the back button
        }

        // ── Topic reminder strip with gradient ───────────────────────────
        // Wears the category tint with the tint setting on; with it off it
        // falls back to a plain theme surface so the whole flow goes neutral.
        val tintWash = AppPreferences.tintWashEnabledState
        val stripColor = if (tintWash) cat.tint else MaterialTheme.colorScheme.surfaceContainerHigh
        val stripInk = if (tintWash) cat.categoryInk() else MaterialTheme.colorScheme.onSurface
        Surface(
            color = stripColor,
            shape = RoundedCornerShape(20.dp),
            border = cat.categoryBorder(),
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
                    color = if (tintWash) cat.accent.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    CurioIcon(
                        name = cat.iconGlyph,
                        contentDescription = null,
                        tint = if (tintWash) cat.categoryInk() else MaterialTheme.colorScheme.onSurfaceVariant,
                        size = 22.dp,
                        modifier = Modifier.padding(6.dp)
                    )
                }
                Column {
                    Text(
                        text = topic?.name ?: "Loading…",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = stripInk
                    )
                    Text(
                        text = cat.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = stripInk.copy(alpha = 0.7f)
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
                    if (editEntryId != null && editingEntry == null) {
                        // Edit mode: hold until the saved entry loads. Rendering
                        // the format body now would fall back to the Wildcard
                        // category's body and could emit blank data.
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = cat.accent)
                        }
                    } else {
                        FormatBodyForCategory(
                            category = cat,
                            // Dispatch on the SAVED entry's format in edit mode so
                            // the right body renders regardless of category default.
                            entryFormat = editingEntry?.format,
                            initialData = editingEntry?.captureData,
                            // Reuse the saved entry's id-derived seed so the editor's
                            // watermark pattern matches the saved view exactly.
                            boardSeed = editEntryId?.hashCode(),
                            onCanSaveChange = { canSave = it && topic != null && (editEntryId == null || editingEntry != null) },
                            onDataChanged = { currentCaptureData = it }
                        )
                    }
            }
        }

        // ── Sticky bottom Save CTA with gradient edge ────────────────────
        // Wears the same category wash as the page (and the Spin bottom bar)
        // so the CTA tray blends into the tinted screen instead of sitting on
        // a plain patch of theme background.
        Surface(
            color = cat.categoryBackgroundWash(),
            shadowElevation = 0.dp,
            tonalElevation = 0.dp
        ) {
            Box {
                // Top gradient edge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = if (tintWash) listOf(
                                    cat.tint,
                                    cat.accent.copy(alpha = 0.3f),
                                    cat.tint
                                ) else listOf(
                                    MaterialTheme.colorScheme.outlineVariant,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                    MaterialTheme.colorScheme.outlineVariant
                                )
                            )
                        )
                )
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    // The save button wears the category TINT with ink content
                    // when the tint setting is on; with it off it reverts to
                    // the plain accent fill + white content as before.
                    Button(
                        onClick = performSave,
                        enabled = canSave && !saveInProgress,
                        shape = RoundedCornerShape(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (tintWash) cat.tint else cat.accent,
                            contentColor = if (tintWash) cat.categoryInk() else Color.White,
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
                                color = if (tintWash) cat.categoryInk() else Color.White,
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
                                tint = if (tintWash) cat.categoryInk() else Color.White,
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

    // ── Three-way leave dialog (save and switch / keep editing / discard) ─
    //    Shown when leaving with unsaved edits. Discard sits on the LEFT;
    //    the primary "Save and switch" action is the rightmost button.
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Unsaved changes") },
            text = { Text("You have unsaved edits. Save them and switch away, or leave without saving.") },
            dismissButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    navController.popBackStack()
                }) {
                    Text("Discard", color = MaterialTheme.colorScheme.error)
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = { showDiscardDialog = false }) {
                        Text("Keep editing")
                    }
                    Button(
                        onClick = {
                            showDiscardDialog = false
                            performSave()
                        },
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Save and switch")
                    }
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
 * Universal capture body — the redesigned "How do you want to capture this
 * one?" picker, now offered for EVERY category (the Wildcard pick-any style
 * became universal). A compact chip row pre-selects the category's dedicated
 * format; users can add multiple different takes (sections) which all save
 * into ONE entry. On save, a single section stores bare data (backward
 * compatible) while 2+ sections store a [CaptureData.Portfolio] — the detail
 * page then shows a section switcher.
 *
 * Section state (format + live data) is held here so switching sections
 * never loses in-progress content: each editor emits its data continuously,
 * and the outgoing section's data is snapshotted as the next activation's
 * seed. [initialData] (edit mode) may be a Portfolio, a legacy
 * OpenNotebook, or a bare format payload — all are unwrapped into sections.
 */
@Composable
private fun FormatBodyForCategory(
    category: CurioCategory,
    onCanSaveChange: (Boolean) -> Unit,
    onDataChanged: (CaptureData?) -> Unit,
    entryFormat: CaptureFormat? = null,
    initialData: CaptureData? = null,
    boardSeed: Int? = null
) {
    // Wildcard has no dedicated page — default its first take to Voice.
    val defaultFormat = if (category.defaultFormat == CaptureFormat.OpenNotebook)
        CaptureFormat.SoundBite else category.defaultFormat

    val sections = remember(entryFormat, initialData) {
        mutableStateListOf<CaptureSectionState>().apply {
            when {
                initialData is CaptureData.Portfolio && initialData.sections.isNotEmpty() ->
                    initialData.sections.forEachIndexed { i, s ->
                        add(CaptureSectionState(i, s.format).apply {
                            seed = s.data
                            data = s.data
                            canSave = true
                            // Each saved take keeps its own paper style.
                            paperStyle = s.data.notePaperStyle()
                        })
                    }
                initialData is CaptureData.OpenNotebook ->
                    add(CaptureSectionState(0, initialData.subFormat).apply {
                        seed = initialData.subData
                        data = initialData.subData
                        canSave = true
                        paperStyle = initialData.notePaperStyle()
                    })
                initialData != null ->
                    add(CaptureSectionState(0, entryFormat ?: defaultFormat).apply {
                        seed = initialData
                        data = initialData
                        canSave = true
                        paperStyle = initialData.notePaperStyle()
                    })
                else -> add(CaptureSectionState(0, defaultFormat))
            }
        }
    }
    // Mood-board edits reopen on their board section; everything else starts
    // on the first take.
    var activeIndex by remember(entryFormat, initialData) {
        mutableIntStateOf(
            (initialData as? CaptureData.Portfolio)
                ?.sections?.indexOfFirst { it.format == CaptureFormat.GalleryWall }
                ?.coerceAtLeast(0) ?: 0
        )
    }
    var nextId by remember(entryFormat, initialData) {
        mutableIntStateOf(sections.maxOfOrNull { it.id }?.plus(1) ?: 0)
    }

    // Snapshot the outgoing section's data so switching back restores it.
    fun snapshotActive() {
        sections.getOrNull(activeIndex)?.let { it.seed = it.data }
    }

    // Removes a take and re-anchors the active index — shared by the X
    // button's direct-remove path and the remove-confirmation dialog so the
    // two can never drift apart.
    fun removeSection(i: Int) {
        if (i < activeIndex) activeIndex--
        sections.removeAt(i)
        if (activeIndex >= sections.size) activeIndex = sections.size - 1
    }

    // Switching a FILLED section's format clears its content — confirm first
    // so a fat-finger on the format chips never silently wipes a take.
    var pendingFormatSwitch by remember { mutableStateOf<CaptureFormat?>(null) }
    // Removing a take that holds drafted content (or a live recording) also
    // confirms first — in edit mode every take arrives prefilled, so the X
    // must never silently throw away drafted changes.
    var pendingRemoveIndex by remember { mutableStateOf<Int?>(null) }
    fun applyFormat(section: CaptureSectionState, fmt: CaptureFormat) {
        section.format = fmt
        section.canSave = false
        section.data = null
        section.seed = null
        section.busy = false
    }

    // ── Aggregate: all sections must be filled to save ──────────────────
    val allReady = sections.isNotEmpty() && sections.all { it.canSave && it.data != null }
    val combinedData: CaptureData? = when {
        !allReady -> null
        sections.size == 1 -> sections[0].data
        else -> CaptureData.Portfolio(
            sections.map { CaptureData.CaptureSection(it.format, it.data!!) }
        )
    }
    LaunchedEffect(allReady, combinedData) {
        onCanSaveChange(allReady)
        onDataChanged(combinedData)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "How do you want to capture this one?",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        // ── Note-paper style chips — the torn-note option alongside the
        // classic ruled page. Controls the ACTIVE section only, so each take
        // keeps its own independent paper look.
        val paperTarget = sections.getOrNull(activeIndex)
        if (paperTarget != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Paper",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                NotePaperStyle.values().forEach { style ->
                    val selected = paperTarget.paperStyle == style
                    Surface(
                        onClick = { paperTarget.paperStyle = style },
                        shape = RoundedCornerShape(50),
                        color = if (selected) category.tint
                                else category.categorySurface(MaterialTheme.colorScheme.surface),
                        border = if (selected) BorderStroke(
                            1.dp,
                            category.accent.copy(alpha = 0.5f)
                        ) else category.categoryBorder(
                            fallback = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ),
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CurioIcon(
                                name = if (style == NotePaperStyle.TORN) CurioIcons.Palette
                                       else CurioIcons.MenuBook,
                                contentDescription = null,
                                tint = if (selected) category.accent
                                       else MaterialTheme.colorScheme.onSurfaceVariant,
                                size = 16.dp
                            )
                            Text(
                                text = if (style == NotePaperStyle.TORN) "Torn note" else "Ruled",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                ),
                                color = if (selected) category.accent
                                       else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // ── Compact format chips — control the ACTIVE section ────────────
        val active = sections.getOrNull(activeIndex)
        if (active != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CAPTURE_FORMATS.forEach { fmt ->
                    Surface(
                        onClick = {
                            if (active.format != fmt) {
                                // Confirm when this take has content OR a live
                                // recording is in progress; an empty take (and
                                // no active recording) switches freely.
                                if ((active.canSave && active.data != null) || active.busy) {
                                    pendingFormatSwitch = fmt
                                } else {
                                    applyFormat(active, fmt)
                                }
                            }
                        },
                        shape = RoundedCornerShape(50),
                        color = if (active.format == fmt) category.tint
                                else category.categorySurface(MaterialTheme.colorScheme.surface),
                        border = if (active.format == fmt) BorderStroke(
                            1.dp,
                            category.accent.copy(alpha = 0.5f)
                        ) else category.categoryBorder(
                            fallback = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ),
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CurioIcon(
                                name = formatGlyph(fmt),
                                contentDescription = null,
                                tint = if (active.format == fmt) category.accent
                                       else MaterialTheme.colorScheme.onSurfaceVariant,
                                size = 16.dp
                            )
                            Text(
                                text = fmt.shortName,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (active.format == fmt) FontWeight.SemiBold else FontWeight.Normal
                                ),
                                color = if (active.format == fmt) category.accent
                                       else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // ── Section tabs + add another take ──────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sections.forEachIndexed { i, s ->
                Surface(
                    onClick = { snapshotActive(); activeIndex = i },
                    shape = RoundedCornerShape(50),
                    color = if (i == activeIndex) category.accent
                            else category.categorySurface(MaterialTheme.colorScheme.surfaceVariant),
                    border = if (i == activeIndex) null else category.categoryBorder(),
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(
                            start = 12.dp,
                            end = if (sections.size > 1) 4.dp else 12.dp,
                            top = 8.dp,
                            bottom = 8.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CurioIcon(
                            name = formatGlyph(s.format),
                            contentDescription = null,
                            tint = if (i == activeIndex) Color.White
                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                            size = 14.dp
                        )
                        Text(
                            text = "${i + 1} · ${s.format.shortName}",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = if (i == activeIndex) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                        if (sections.size > 1) {
                            Surface(
                                onClick = {
                                    val section = sections.getOrNull(i)
                                    // Confirm removal when the take has drafted
                                    // content or a live recording in progress;
                                    // an empty take (and no recording) removes
                                    // freely.
                                    if ((section?.canSave == true && section.data != null) || section?.busy == true) {
                                        pendingRemoveIndex = i
                                    } else {
                                        removeSection(i)
                                    }
                                },
                                shape = CircleShape,
                                color = Color.Transparent
                            ) {
                                CurioIcon(
                                    name = CurioIcons.Close,
                                    contentDescription = "Remove take",
                                    tint = if (i == activeIndex) Color.White
                                           else MaterialTheme.colorScheme.onSurfaceVariant,
                                    size = 16.dp,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }
                }
            }
            Surface(
                onClick = {
                    snapshotActive()
                    sections.add(CaptureSectionState(nextId++, defaultFormat))
                    activeIndex = sections.lastIndex
                },
                shape = RoundedCornerShape(50),
                color = category.tint,
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CurioIcon(
                        name = CurioIcons.Add,
                        contentDescription = null,
                        tint = category.accent,
                        size = 16.dp
                    )
                    Text(
                        text = "Add take",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = category.accent
                    )
                }
            }
        }

        // ── Active section's format body ─────────────────────────────────
        val current = sections.getOrNull(activeIndex)
        if (current != null) {
            key(current.id) {
                when (current.format) {
                    CaptureFormat.SoundBite -> SoundBiteFormat(
                        category.accent, category.tint,
                        { current.canSave = it }, { current.data = it },
                        onBusyChange = { current.busy = it },
                        initialData = current.seed as? CaptureData.SoundBite,
                        paperStyle = current.paperStyle
                    )
                    CaptureFormat.ReelNotes -> ReelNotesFormat(
                        category.accent, category.tint,
                        { current.canSave = it }, { current.data = it },
                        initialData = current.seed as? CaptureData.ReelNotes,
                        paperStyle = current.paperStyle
                    )
                    CaptureFormat.Marginalia -> MarginaliaFormat(
                        category.accent, category.tint,
                        { current.canSave = it }, { current.data = it },
                        initialData = current.seed as? CaptureData.Marginalia,
                        paperStyle = current.paperStyle
                    )
                    CaptureFormat.GalleryWall -> GalleryWallFormat(
                        category.accent, category.tint,
                        { current.canSave = it }, { current.data = it },
                        initialData = current.seed as? CaptureData.GalleryWall,
                        boardSeed = boardSeed,
                        paperStyle = current.paperStyle
                    )
                    CaptureFormat.FieldNotes -> FieldNotesFormat(
                        category.accent, category.tint,
                        { current.canSave = it }, { current.data = it },
                        initialData = current.seed as? CaptureData.FieldNotes,
                        paperStyle = current.paperStyle
                    )
                    CaptureFormat.OpenNotebook -> OpenNotebookFormat(
                        category.accent, category.tint,
                        { current.canSave = it }, { current.data = it },
                        initialData = current.seed as? CaptureData.OpenNotebook,
                        boardSeed = boardSeed,
                        paperStyle = current.paperStyle
                    )
                }
            }
        }
    }

    // ── Confirm before removing a take with drafted content ─────────────
    pendingRemoveIndex?.let { removeIdx ->
        AlertDialog(
            onDismissRequest = { pendingRemoveIndex = null },
            title = { Text("Remove this take?") },
            text = { Text("This will delete the content you've drafted in this take (including any live recording).") },
            confirmButton = {
                TextButton(onClick = {
                    removeSection(removeIdx)
                    pendingRemoveIndex = null
                }) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingRemoveIndex = null }) {
                    Text("Keep editing")
                }
            }
        )
    }

    // ── Confirm before switching a filled take's format ──────────────────
    pendingFormatSwitch?.let { fmt ->
        AlertDialog(
            onDismissRequest = { pendingFormatSwitch = null },
            title = { Text("Switch format?") },
            text = { Text("This will clear what you've added to this take (including any live recording) and switch to ${fmt.shortName}.") },
            confirmButton = {
                TextButton(onClick = {
                    val section = sections.getOrNull(activeIndex)
                    if (section != null) applyFormat(section, fmt)
                    pendingFormatSwitch = null
                }) {
                    Text("Switch")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingFormatSwitch = null }) {
                    Text("Keep editing")
                }
            }
        )
    }
}

/** One take (section) inside the universal multi-section picker. */
private class CaptureSectionState(val id: Int, initialFormat: CaptureFormat) {
    var format by mutableStateOf(initialFormat)
    var canSave by mutableStateOf(false)
    var data by mutableStateOf<CaptureData?>(null)
    var seed by mutableStateOf<CaptureData?>(null)
    // True while a live recording is in progress — format-switch confirmation
    // must also trigger here (data/canSave are null mid-recording).
    var busy by mutableStateOf(false)
    // The note-paper style this take wears — ruled notebook page or torn
    // note. Lives per-section so each take keeps its own look, and rides
    // into the saved CaptureData so the detail view matches the editor.
    var paperStyle by mutableStateOf(NotePaperStyle.RULED)
}

/** The 5 concrete format chips offered by the universal picker. */
private val CAPTURE_FORMATS = listOf(
    CaptureFormat.SoundBite,
    CaptureFormat.ReelNotes,
    CaptureFormat.Marginalia,
    CaptureFormat.GalleryWall,
    CaptureFormat.FieldNotes
)

/**
 * Recursively persists every SoundBite audio file inside [data] (through
 * OpenNotebook wrappers and Portfolio sections) so each recording gets a
 * stable internal path before saving.
 */
private suspend fun persistAudioDeep(
    context: Context,
    data: CaptureData,
    entryId: String
): CaptureData = when (data) {
    is CaptureData.SoundBite ->
        if (data.audioFilePath.isNullOrBlank()) data
        else {
            val result = AudioStorageManager.persistAudio(
                context, data.audioFilePath, entryId
            )
            data.copy(
                audioFilePath = result.persistentPath,
                fileSizeBytes = result.fileSizeBytes
            )
        }
    is CaptureData.OpenNotebook ->
        data.copy(subData = persistAudioDeep(context, data.subData, entryId))
    is CaptureData.Portfolio ->
        data.copy(
            sections = data.sections.map {
                it.copy(data = persistAudioDeep(context, it.data, entryId))
            }
        )
    else -> data
}

/**
 * The [CaptureFormat] that best represents [data] for the entry's format
 * column — the first section's format for a Portfolio.
 */
private fun formatOf(data: CaptureData): CaptureFormat = when (data) {
    is CaptureData.SoundBite -> CaptureFormat.SoundBite
    is CaptureData.ReelNotes -> CaptureFormat.ReelNotes
    is CaptureData.Marginalia -> CaptureFormat.Marginalia
    is CaptureData.GalleryWall -> CaptureFormat.GalleryWall
    is CaptureData.FieldNotes -> CaptureFormat.FieldNotes
    is CaptureData.OpenNotebook -> data.subFormat
    is CaptureData.Portfolio -> data.sections.firstOrNull()?.format ?: CaptureFormat.SoundBite
}
