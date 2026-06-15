package fieldmind.research.app.features.field.presentation.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import fieldmind.research.app.features.field.data.ai.AiProvider
import fieldmind.research.app.features.field.data.ai.AssistantTask
import fieldmind.research.app.features.field.data.ai.GeminiResearchAssistant
import fieldmind.research.app.features.field.data.database.entity.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import fieldmind.research.app.features.field.data.export.FieldMindExport
import fieldmind.research.app.features.field.data.location.FieldLocationProvider
import fieldmind.research.app.features.field.data.weather.WeatherUnitConverter
import fieldmind.research.app.features.field.presentation.components.*
import fieldmind.research.app.features.field.presentation.navigation.FieldMindScreen
import fieldmind.research.app.features.field.presentation.theme.FieldMindTheme
import fieldmind.research.app.features.field.presentation.viewmodel.DraftEvidenceAttachment
import fieldmind.research.app.features.field.presentation.viewmodel.FieldMindViewModel
import fieldmind.research.app.shared.presentation.components.icons.Icon
import fieldmind.research.app.shared.presentation.components.icons.MaterialSymbolIcon
import kotlinx.coroutines.launch
import java.util.Locale

// ══════════════════════════════════════════════════════════════════════
//  Detail Screen — Entity-specific rich layouts
// ══════════════════════════════════════════════════════════════════════

@Composable
fun DetailScreen(
    kind: String,
    id: Long,
    viewModel: FieldMindViewModel,
    onBack: () -> Unit,
    onOpenDetail: (String, Long) -> Unit = { _, _ -> },
    onOpenReader: (String, String) -> Unit = { _, _ -> }
) {
    val observations by viewModel.observations.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val questions by viewModel.questions.collectAsState()
    val hypotheses by viewModel.hypotheses.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val sources by viewModel.sources.collectAsState()
    val data by viewModel.dataRecords.collectAsState()
    val reports by viewModel.reports.collectAsState()
    val flashcards by viewModel.flashcards.collectAsState()
    val title = kind.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    var showEdit by remember(kind, id) { mutableStateOf(false) }
    var showDelete by remember(kind, id) { mutableStateOf(false) }
    val editable = kind in setOf("observation", "note", "question", "hypothesis", "project", "source", "data", "report", "flashcard")
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val exportText = remember(kind, id, observations, projects, data, reports) {
        when (kind) {
            "observation" -> observations.firstOrNull { it.id == id }?.let(FieldMindExport::singleObservationMarkdown)
            "project" -> projects.firstOrNull { it.id == id }?.let(FieldMindExport::singleProjectMarkdown)
            "data" -> data.firstOrNull { it.id == id }?.let(FieldMindExport::singleDataRecordMarkdown)
            "report" -> reports.firstOrNull { it.id == id }?.let(FieldMindExport::singleReportMarkdown)
            else -> null
        }.orEmpty()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(0.dp, 0.dp, 0.dp, 40.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Back header ──
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        onClick = onBack,
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.size(44.dp)
                    ) { Box(contentAlignment = Alignment.Center) { Icon(FieldMindIcons.Back, null, size = 22.dp) } }
                    EntityBadge(kind)
                    Spacer(Modifier.weight(1f))
                    if (editable && exportText.isNotBlank()) {
                        IconButton(onClick = { clipboard.setText(AnnotatedString(exportText)) }, modifier = Modifier.size(36.dp)) {
                            Icon(FieldMindIcons.Article, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 20.dp)
                        }
                        IconButton(onClick = { sharePlainText(context, exportText) }, modifier = Modifier.size(36.dp)) {
                            Icon(FieldMindIcons.Export, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 20.dp)
                        }
                    }
                }
            }

            if (editable && !showEdit) {
                item { DetailActionBar(onEdit = { showEdit = true }, onDelete = { showDelete = true }) }
            }

            if (showEdit) {
                when (kind) {
                    "note" -> notes.firstOrNull { it.id == id }?.let { n ->
                        item { InlineEditNote(n, viewModel, { showEdit = false; onBack() }) }
                    }
                    "observation" -> observations.firstOrNull { it.id == id }?.let { o ->
                        item { InlineEditObservation(o, viewModel, { showEdit = false; onBack() }) }
                    }
                }
            } else {
                when (kind) {
                    "note" -> notes.firstOrNull { it.id == id }?.let { n ->
                        item { NoteDetailContent(n, onOpenDetail) }
                        item { BacklinksPanel(buildList {
                            projects.firstOrNull { it.id == n.projectId }?.let { add(Triple("project", it.title, it.id)) }
                            sources.firstOrNull { it.id == n.sourceId }?.let { add(Triple("source", it.title, it.id)) }
                        }, onOpenDetail) }
                    }
                    "observation" -> observations.firstOrNull { it.id == id }?.let { o ->
                        item { ObservationDetailContent(o, viewModel, onOpenReader, onOpenDetail) }
                        item { BacklinksPanel(buildList {
                            projects.firstOrNull { it.id == o.projectId }?.let { add(Triple("project", it.title, it.id)) }
                            data.filter { it.observationId == o.id }.forEach { add(Triple("data", it.label, it.id)) }
                        }, onOpenDetail) }
                    }
                    "question" -> questions.firstOrNull { it.id == id }?.let { qn ->
                        item { QuestionDetailContent(qn, hypotheses) { ans -> viewModel.setQuestionAnswer(qn, ans) } }
                        item { BacklinksPanel(buildList {
                            projects.firstOrNull { it.id == qn.relatedProjectId }?.let { add(Triple("project", it.title, it.id)) }
                            hypotheses.filter { it.linkedQuestionId == qn.id }.forEach { add(Triple("hypothesis", it.prediction, it.id)) }
                        }, onOpenDetail) }
                    }
                    "hypothesis" -> hypotheses.firstOrNull { it.id == id }?.let { h ->
                        item { HypothesisDetailContent(h) }
                        item { BacklinksPanel(buildList {
                            questions.firstOrNull { it.id == h.linkedQuestionId }?.let { add(Triple("question", it.questionText, it.id)) }
                        }, onOpenDetail) }
                    }
                    "project" -> projects.firstOrNull { it.id == id }?.let { p ->
                        item { ProjectDetailContent(p, observations, questions, sources, data, reports, viewModel) }
                        item { BacklinksPanel(buildList {
                            observations.filter { it.projectId == p.id }.forEach { add(Triple("observation", it.subject, it.id)) }
                            questions.filter { it.relatedProjectId == p.id }.forEach { add(Triple("question", it.questionText, it.id)) }
                            sources.filter { it.relatedProjectId == p.id }.forEach { add(Triple("source", it.title, it.id)) }
                            data.filter { it.projectId == p.id }.forEach { add(Triple("data", it.label, it.id)) }
                            reports.filter { it.projectId == p.id }.forEach { add(Triple("report", it.title, it.id)) }
                        }, onOpenDetail) }
                    }
                    "source" -> sources.firstOrNull { it.id == id }?.let { s ->
                        item { DetailBody(s.title, "source", listOf("Type" to s.type, "Author" to s.author, "Year" to s.dateOrYear, "DOI / ISBN" to s.doiOrIsbn, "Publisher / journal" to s.publisherOrJournal, "Link" to s.link, "Access date" to s.accessDate, "Citation note" to s.citationStyleNote, "Importance" to s.importance, "Reading status" to s.readingStatus, "Project" to (projects.firstOrNull { it.id == s.relatedProjectId }?.title ?: "None"), "Main idea" to s.personalSummary, "Key findings" to s.keyFindings, "Taught me" to s.whatThisSourceTaughtMe, "Paper prompts" to s.paperNotes, "Questions" to s.questionsGenerated)) }
                        item { SourcePreviewPanel(s, onOpenReader) }
                        item { SourceActionPanel(s, projects, viewModel, onOpenDetail) }
                        item { BacklinksPanel(buildList {
                            projects.firstOrNull { it.id == s.relatedProjectId }?.let { add(Triple("project", it.title, it.id)) }
                            flashcards.filter { it.sourceId == s.id }.forEach { add(Triple("flashcard", it.front, it.id)) }
                        }, onOpenDetail) }
                    }
                    "data" -> data.firstOrNull { it.id == id }?.let { d ->
                        item { DataRecordDetailContent(d) }
                        item { BacklinksPanel(buildList {
                            projects.firstOrNull { it.id == d.projectId }?.let { add(Triple("project", it.title, it.id)) }
                            observations.firstOrNull { it.id == d.observationId }?.let { add(Triple("observation", it.subject, it.id)) }
                        }, onOpenDetail) }
                    }
                    "report" -> reports.firstOrNull { it.id == id }?.let { r ->
                        item { ReportDetailContent(r) }
                        item { BacklinksPanel(buildList { projects.firstOrNull { it.id == r.projectId }?.let { add(Triple("project", it.title, it.id)) } }, onOpenDetail) }
                    }
                    "flashcard" -> flashcards.firstOrNull { it.id == id }?.let { f ->
                        item { FlashcardDetailContent(f) }
                        item { BacklinksPanel(buildList {
                            sources.firstOrNull { it.id == f.sourceId }?.let { add(Triple("source", it.title, it.id)) }
                            projects.firstOrNull { it.id == f.projectId }?.let { add(Triple("project", it.title, it.id)) }
                        }, onOpenDetail) }
                    }
                }
            }
        }
    }
    if (showEdit && kind !in setOf("note", "observation")) EditEntityDialog(kind, id, viewModel) { showEdit = false }
    if (showDelete) ConfirmDeleteDialog(kind, onDismiss = { showDelete = false }) {
        deleteEntityByKind(kind, id, viewModel); showDelete = false; onBack()
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Entity-Specific Detail Content
// ══════════════════════════════════════════════════════════════════════

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun ObservationDetailContent(
    o: ObservationEntity,
    viewModel: FieldMindViewModel,
    onOpenReader: (String, String) -> Unit,
    onOpenDetail: (String, Long) -> Unit = { _, _ -> }
) {
    val colors = FieldMindTheme.colors
    val tempUnit by viewModel.fieldSettings.tempUnit.collectAsState()
    val windSpeedUnit by viewModel.fieldSettings.windSpeedUnit.collectAsState()
    val distUnit by viewModel.fieldSettings.distanceUnit.collectAsState()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            // ── 1. Swipeable Media Gallery (hero carousel) ──
            ObservationHeroCarousel(viewModel, o.id, onOpenReader)

            // ── 2. Header with subject and badges ──
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier.size(48.dp).clip(RoundedCornerShape(16.dp))
                        .background(colors.observation.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) { Icon(FieldMindIcons.Observation, null, tint = colors.observation, size = 26.dp) }
                Column(Modifier.weight(1f)) {
                    Text(o.subject.ifBlank { "Observation" }, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoChip(o.category, icon = FieldMindIcons.Category)
                        ConfidenceChip(o.confidenceLevel)
                    }
                }
            }

            // ── 3. Date / GPS / Weather stat bar ──
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.4f))
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ObsStatItem("${o.date} ${o.time}", "Date", FieldMindIcons.Calendar)
                ObsStatItem(
                    if (o.latitude != null && o.longitude != null) "GPS" else "No GPS",
                    "Location",
                    FieldMindIcons.Location
                )
                ObsStatItem(
                    if (o.weatherTemperature != null) "${WeatherUnitConverter.formatTemp(o.weatherTemperature, tempUnit)}" else "—",
                    "Weather",
                    FieldMindIcons.Weather
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // ── 4. Species Information ──
            ObservationSpeciesInfoSection(o, viewModel)

            // ── 5. Observation Notes (Facts-only) ──
            if (o.factsOnlyNotes.isNotBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader("Observation notes", "Facts-only record of what was observed")
                    Text(o.factsOnlyNotes, style = MaterialTheme.typography.bodyLarge)
                }
            }

            // ── 6. Behavior & Context ──
            ObservationBehaviorSection(o)

            // ── 7. Context / mood ──
            if (o.moodOrContext.isNotBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader("Context", "Field conditions and mood")
                    Text(o.moodOrContext, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // ── 8. Evidence summary ──
            if (o.evidenceSummary.isNotBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader("Evidence", "Attached media and files")
                    Text(o.evidenceSummary, style = MaterialTheme.typography.bodyMedium)
                }
            }

            // ── 9. Tags ──
            if (o.tags.isNotBlank()) {
                FlowRow(verticalArrangement = Arrangement.spacedBy(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    o.tags.split(",").filter { it.isNotBlank() }.forEach { tag ->
                        TagChip(tag.trim())
                    }
                }
            }

            // ── 10. Evidence counts (Photo / Video / Audio) ──
            ObservationEvidenceCountsRow(viewModel, o.id)

            // ── 11. Weather & Location details ──
            ObservationWeatherLocationSection(o, viewModel, tempUnit, windSpeedUnit)

            // ── 12. Map preview ──
            if (o.latitude != null && o.longitude != null) {
                ObservationLocationCard(o.latitude, o.longitude, o.manualLocation)
            }

            // ── 13. AI Species Analysis ──
            ObservationAiAnalysisCard(o, viewModel)

            // ── 14. Provenance collapsible ──
            var showProvenance by remember { mutableStateOf(false) }
            Card(
                modifier = Modifier.fillMaxWidth().clickable { showProvenance = !showProvenance },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(FieldMindIcons.Info, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 18.dp)
                            Text("Provenance & metadata", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        }
                        Icon(if (showProvenance) FieldMindIcons.Up else FieldMindIcons.Down, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 18.dp)
                    }
                    if (showProvenance) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            ProvenanceRow("Date & time", "${o.date} ${o.time}")
                            o.startedAt?.let { ProvenanceRow("Session start", formatTimestamp(it)) }
                            o.endedAt?.let { ProvenanceRow("Session end", formatTimestamp(it)) }
                            o.durationMs?.let { ProvenanceRow("Duration", formatDuration(it)) }
                            o.latitude?.let { lat ->
                                o.longitude?.let { lng -> ProvenanceRow("Coordinates", "%.6f, %.6f".format(lat, lng)) }
                            }
                            if (o.manualLocation.isNotBlank()) ProvenanceRow("Place", o.manualLocation)
                            o.weatherSnapshotAt?.let { ProvenanceRow("Weather snapshot", formatTimestamp(it)) }
                            ProvenanceRow("Record ID", "#${o.id}")
                            ProvenanceRow("Created", formatTimestamp(o.createdAt))
                            ProvenanceRow("Updated", formatTimestamp(o.updatedAt))
                        }
                    }
                }
            }

            // ── 15. Related Observations (re-observation chain) ──
            ReObservationLink(o, viewModel, onOpenDetail)

            // ── 16. Attachments gallery ──
            ObservationAttachmentsPanel(viewModel, o.id, onOpenReader)

            // ── 17. Export & Sharing ──
            ObservationExportSection(o, viewModel, context, clipboard, snackbar, scope)
        }
    }
    
    // Export menu dialog removed — export is handled inline in ObservationExportSection
}

@Composable
private fun ReObservationLink(
    o: ObservationEntity,
    viewModel: FieldMindViewModel,
    onOpenDetail: (String, Long) -> Unit
) {
    val observations by viewModel.observations.collectAsState()
    val colors = FieldMindTheme.colors

    // ── Parent observation (this is a re-observation of) ──
    o.parentObservationId?.let { parentId ->
        val parent = observations.firstOrNull { it.id == parentId }
        if (parent != null) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colors.hypothesis.copy(alpha = 0.08f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(FieldMindIcons.Link, null, tint = colors.hypothesis, size = 18.dp)
                        Text("Re-observation chain", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = colors.hypothesis)
                    }
                    Text("This is a follow-up observation of:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    EntityCard(
                        title = parent.subject.ifBlank { "Observation #${parent.id}" },
                        kind = "observation",
                        meta = listOf(parent.date, parent.category, parent.confidenceLevel),
                        onClick = { onOpenDetail("observation", parent.id) }
                    )
                }
            }
        }
    }

    // ── Child observations (re-observations of this one) ──
    val children = observations.filter { it.parentObservationId == o.id }
    if (children.isNotEmpty()) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(FieldMindIcons.Project, null, tint = colors.observation, size = 18.dp)
                        Text("${children.size} follow-up observation${if (children.size != 1) "s" else ""}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                }
                children.forEach { child ->
                    EntityCard(
                        title = child.subject.ifBlank { "Observation #${child.id}" },
                        kind = "observation",
                        meta = listOf(child.date, child.category, child.confidenceLevel),
                        onClick = { onOpenDetail("observation", child.id) }
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Species Information Section — Scientific name, taxonomy, conservation
// ══════════════════════════════════════════════════════════════════════

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun ObservationSpeciesInfoSection(
    o: ObservationEntity,
    viewModel: FieldMindViewModel
) {
    val colors = FieldMindTheme.colors
    // Parse structured details JSON for species info if available
    val speciesInfo = remember(o.structuredDetailsJson) {
        if (o.structuredDetailsJson.isNotBlank()) {
            try {
                val json = org.json.JSONObject(o.structuredDetailsJson)
                val sciName = json.optString("scientificName", "")
                val taxonomy = json.optString("taxonomy", "")
                val conservation = json.optString("conservationStatus", "")
                val description = json.optString("speciesDescription", "")
                SpeciesInfoData(sciName, taxonomy, conservation, description)
            } catch (_: Exception) { null }
        } else null
    }
    
    val hasAnyInfo = speciesInfo != null || o.category.isNotBlank()
    if (!hasAnyInfo) return
    
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.observation.copy(alpha = 0.06f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(FieldMindIcons.Nature, null, tint = colors.observation, size = 20.dp)
                Text("Species information", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = colors.observation)
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (speciesInfo?.scientificName?.isNotBlank() == true) {
                    InfoRow("Scientific name", speciesInfo.scientificName)
                }
                if (speciesInfo?.taxonomy?.isNotBlank() == true) {
                    InfoRow("Taxonomy", speciesInfo.taxonomy)
                }
                if (speciesInfo?.conservationStatus?.isNotBlank() == true) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val statusColor = when {
                            speciesInfo.conservationStatus.contains("Endangered") || speciesInfo.conservationStatus.contains("Critically") -> MaterialTheme.colorScheme.error
                            speciesInfo.conservationStatus.contains("Vulnerable") || speciesInfo.conservationStatus.contains("Near") -> colors.warning
                            else -> colors.positive
                        }
                        Text("Conservation", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Box(
                            Modifier.clip(RoundedCornerShape(8.dp))
                                .background(statusColor.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                speciesInfo.conservationStatus,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                    }
                }
                if (speciesInfo?.description?.isNotBlank() == true) {
                    Text(
                        speciesInfo.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (speciesInfo == null && o.subject.isNotBlank()) {
                    Text(
                        "Category: ${o.category}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private data class SpeciesInfoData(
    val scientificName: String,
    val taxonomy: String,
    val conservationStatus: String,
    val description: String
)

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Behavior & Context Section
// ══════════════════════════════════════════════════════════════════════

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun ObservationBehaviorSection(
    o: ObservationEntity
) {
    val colors = FieldMindTheme.colors
    // Parse structured details JSON for behavior, life stage, sex
    val behaviorData = remember(o.structuredDetailsJson) {
        if (o.structuredDetailsJson.isNotBlank()) {
            try {
                val json = org.json.JSONObject(o.structuredDetailsJson)
                val behavior = json.optString("behavior", "")
                val lifeStage = json.optString("lifeStage", "")
                val sex = json.optString("sex", "")
                val feeding = json.optString("feeding", "")
                val nesting = json.optString("nesting", "")
                BehaviorData(behavior, lifeStage, sex, feeding, nesting)
            } catch (_: Exception) { null }
        } else null
    }
    
    if (behaviorData == null) return
    val hasAny = listOf(behaviorData.behavior, behaviorData.lifeStage, behaviorData.sex).any { it.isNotBlank() }
    if (!hasAny) return
    
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.hypothesis.copy(alpha = 0.06f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(FieldMindIcons.Trend, null, tint = colors.hypothesis, size = 20.dp)
                Text("Behavior & context", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = colors.hypothesis)
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (behaviorData.behavior.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), 
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Behavior", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(behaviorData.behavior, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                if (behaviorData.lifeStage.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Life stage", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(behaviorData.lifeStage, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                if (behaviorData.sex.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Sex", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(behaviorData.sex, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            if (behaviorData.feeding.isNotBlank() || behaviorData.nesting.isNotBlank()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (behaviorData.feeding.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Feeding:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(if (behaviorData.feeding == "true") "Yes" else "No", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (behaviorData.nesting.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Nesting:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(if (behaviorData.nesting == "true") "Yes" else "No", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private data class BehaviorData(
    val behavior: String,
    val lifeStage: String,
    val sex: String,
    val feeding: String,
    val nesting: String
)

// ══════════════════════════════════════════════════════════════════════
//  Evidence Counts Row — Photo / Video / Audio counts
// ══════════════════════════════════════════════════════════════════════

@Composable
private fun ObservationEvidenceCountsRow(
    viewModel: FieldMindViewModel,
    observationId: Long
) {
    val attachments by viewModel.attachmentsForObservation(observationId).collectAsState(initial = emptyList())
    if (attachments.isEmpty()) return
    
    val photoCount = attachments.count { a -> 
        a.type.equals("Photo", true) || a.type.equals("Gallery", true) || 
        a.uri.contains(Regex("\\.(jpg|jpeg|png|webp|gif|heic|bmp)", RegexOption.IGNORE_CASE))
    }
    val videoCount = attachments.count { a ->
        a.type.equals("Video", true) || 
        a.uri.contains(Regex("\\.(mp4|mov|avi|mkv|webm|3gp)", RegexOption.IGNORE_CASE))
    }
    val audioCount = attachments.count { a ->
        a.type.equals("Audio", true) || a.type.equals("Mic", true) || 
        a.uri.contains(Regex("\\.(m4a|mp3|wav|ogg|flac|aac)", RegexOption.IGNORE_CASE))
    }
    val otherCount = attachments.size - photoCount - videoCount - audioCount
    
    val colors = FieldMindTheme.colors
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.3f))
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        EvidenceCountItem("Photo", photoCount, FieldMindIcons.Gallery, colors.observation)
        EvidenceCountItem("Video", videoCount, FieldMindIcons.Play, colors.info)
        EvidenceCountItem("Audio", audioCount, FieldMindIcons.Mic, colors.data)
        if (otherCount > 0) {
            EvidenceCountItem("Files", otherCount, FieldMindIcons.File, MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EvidenceCountItem(
    label: String,
    count: Int,
    icon: MaterialSymbolIcon,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, null, tint = color, size = 20.dp)
        Text(
            "$count",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Weather & Location Section — Full details
// ══════════════════════════════════════════════════════════════════════

@Composable
private fun ObservationWeatherLocationSection(
    o: ObservationEntity,
    viewModel: FieldMindViewModel,
    tempUnit: String,
    windSpeedUnit: String
) {
    val colors = FieldMindTheme.colors
    val hasWeather = o.weatherTemperature != null || o.weatherCondition.isNotBlank()
    val hasLocation = o.latitude != null || o.manualLocation.isNotBlank()
    if (!hasWeather && !hasLocation) return
    
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.info.copy(alpha = 0.06f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(FieldMindIcons.Weather, null, tint = colors.info, size = 20.dp)
                Text("Weather & location", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = colors.info)
            }
            
            // Weather details
            if (hasWeather) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (o.weatherTemperature != null) {
                            WeatherDetailRow("Temperature", WeatherUnitConverter.formatTemp(o.weatherTemperature, tempUnit))
                        }
                        if (o.weatherHumidity != null) {
                            WeatherDetailRow("Humidity", "${o.weatherHumidity}%")
                        }
                        if (o.weatherWindSpeed != null) {
                            WeatherDetailRow("Wind", WeatherUnitConverter.formatWind(o.weatherWindSpeed, windSpeedUnit))
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (o.weatherCloudCover != null) {
                            WeatherDetailRow("Cloud cover", "${o.weatherCloudCover}%")
                        }
                        if (o.weatherPressure != null) {
                            WeatherDetailRow("Pressure", "${o.weatherPressure?.toInt()} hPa")
                        }
                        if (o.weatherCondition.isNotBlank()) {
                            WeatherDetailRow("Condition", o.weatherCondition)
                        }
                    }
                }
            }
            
            // Location details
            if (hasLocation) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (o.manualLocation.isNotBlank()) {
                        WeatherDetailRow("Location", o.manualLocation)
                    }
                    if (o.latitude != null && o.longitude != null) {
                        val latStr = "%.5f".format(o.latitude)
                        val lngStr = "%.5f".format(o.longitude)
                        WeatherDetailRow("GPS", "$latStr, $lngStr")
                    }
                    // GPS accuracy from structured details
                    val accuracy = remember(o.structuredDetailsJson) {
                        if (o.structuredDetailsJson.isNotBlank()) {
                            try {
                                org.json.JSONObject(o.structuredDetailsJson).optString("gpsAccuracy", "")
                            } catch (_: Exception) { "" }
                        } else ""
                    }
                    if (accuracy.isNotBlank()) {
                        WeatherDetailRow("Accuracy", "±${accuracy}m")
                    }
                    // Altitude from structured details
                    val altitude = remember(o.structuredDetailsJson) {
                        if (o.structuredDetailsJson.isNotBlank()) {
                            try {
                                val a = org.json.JSONObject(o.structuredDetailsJson).optString("altitude", "")
                                if (a.isNotBlank()) "${a}m" else ""
                            } catch (_: Exception) { "" }
                        } else ""
                    }
                    if (altitude.isNotBlank()) {
                        WeatherDetailRow("Altitude", altitude)
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherDetailRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

// ══════════════════════════════════════════════════════════════════════
//  AI Species Analysis Card
// ══════════════════════════════════════════════════════════════════════

@Composable
private fun ObservationAiAnalysisCard(
    o: ObservationEntity,
    viewModel: FieldMindViewModel
) {
    val colors = FieldMindTheme.colors
    // Parse stored AI analysis results from structured details
    val analysisResult = remember(o.structuredDetailsJson) {
        if (o.structuredDetailsJson.isNotBlank()) {
            try {
                val json = org.json.JSONObject(o.structuredDetailsJson)
                val topMatch = json.optString("aiTopMatch", "")
                val topConfidence = json.optString("aiTopConfidence", "")
                val secondMatch = json.optString("aiSecondMatch", "")
                val secondConfidence = json.optString("aiSecondConfidence", "")
                val analysisTime = json.optString("aiAnalysisTime", "")
                if (topMatch.isNotBlank()) {
                    AiAnalysisData(topMatch, topConfidence, secondMatch, secondConfidence, analysisTime)
                } else null
            } catch (_: Exception) { null }
        } else null
    }
    
    if (analysisResult == null) return
    
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.project.copy(alpha = 0.06f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(FieldMindIcons.Bolt, null, tint = colors.project, size = 20.dp)
                Text("AI species analysis", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = colors.project)
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Top match
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(analysisResult.topMatch, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("Top match", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            analysisResult.topConfidence,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.project
                        )
                    }
                }
                
                // Second match (if available)
                if (analysisResult.secondMatch.isNotBlank()) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(analysisResult.secondMatch, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(analysisResult.secondConfidence, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                
                if (analysisResult.analysisTime.isNotBlank()) {
                    Text(
                        "Analysis: ${analysisResult.analysisTime}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

private data class AiAnalysisData(
    val topMatch: String,
    val topConfidence: String,
    val secondMatch: String,
    val secondConfidence: String,
    val analysisTime: String
)

// ══════════════════════════════════════════════════════════════════════
//  Export & Sharing Section
// ══════════════════════════════════════════════════════════════════════

@Composable
private fun ObservationExportSection(
    o: ObservationEntity,
    viewModel: FieldMindViewModel,
    context: android.content.Context,
    clipboard: androidx.compose.ui.platform.ClipboardManager,
    snackbar: SnackbarHostState,
    scope: kotlinx.coroutines.CoroutineScope
) {
    val exportText = remember(o) { FieldMindExport.singleObservationMarkdown(o) }
    val haptics = rememberFieldMindHaptics()
    
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(FieldMindIcons.Export, null, tint = MaterialTheme.colorScheme.primary, size = 20.dp)
                Text("Export & sharing", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // PDF Export
                FilledTonalButton(
                    onClick = {
                        haptics.light()
                        scope.launch { snackbar.showSnackbar("PDF export coming soon") }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(FieldMindIcons.Article, null, size = 16.dp)
                    Spacer(Modifier.size(4.dp))
                    Text("PDF", style = MaterialTheme.typography.labelSmall)
                }
                // CSV Export
                FilledTonalButton(
                    onClick = {
                        haptics.light()
                        scope.launch { snackbar.showSnackbar("CSV export coming soon") }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(FieldMindIcons.Data, null, size = 16.dp)
                    Spacer(Modifier.size(4.dp))
                    Text("CSV", style = MaterialTheme.typography.labelSmall)
                }
                // JSON Export
                FilledTonalButton(
                    onClick = {
                        haptics.light()
                        scope.launch { snackbar.showSnackbar("JSON export coming soon") }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(FieldMindIcons.Data, null, size = 16.dp)
                    Spacer(Modifier.size(4.dp))
                    Text("JSON", style = MaterialTheme.typography.labelSmall)
                }
                // Share
                FilledTonalButton(
                    onClick = {
                        haptics.confirm()
                        clipboard.setText(AnnotatedString(exportText))
                        sharePlainText(context, exportText)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(FieldMindIcons.Export, null, size = 16.dp)
                    Spacer(Modifier.size(4.dp))
                    Text("Share", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun WeatherChip(text: String, color: androidx.compose.ui.graphics.Color) {
    Box(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ProvenanceRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
    }
}

private fun formatTimestamp(millis: Long): String {
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(millis))
}

private fun formatDuration(millis: Long): String {
    val totalSec = millis / 1000
    val hours = totalSec / 3600
    val minutes = (totalSec % 3600) / 60
    val seconds = totalSec % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds)
    else "%d:%02d".format(minutes, seconds)
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ObservationHeroCarousel(viewModel: FieldMindViewModel, observationId: Long, onOpenReader: (String, String) -> Unit) {
    val attachments by viewModel.attachmentsForObservation(observationId).collectAsState(initial = emptyList())
    val media = attachments.filter { it.type.equals("Photo", true) || it.type.equals("Gallery", true) || uriLooksImage(it.uri) || uriLooksImage(it.localPath.orEmpty()) }
    if (media.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { media.size })

    Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val item = media[page]
            val displayUri = item.localPath ?: item.uri
            AsyncImage(
                model = displayUri,
                contentDescription = item.caption.ifBlank { "Observation media" },
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .clickable { onOpenReader(displayUri, item.caption.ifBlank { "Observation media" }) }
            )
        }

        // Page indicator dots
        if (media.size > 1) {
            Row(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(media.size) { index ->
                    Box(
                        Modifier
                            .size(if (pagerState.currentPage == index) 10.dp else 7.dp, 7.dp)
                            .clip(CircleShape)
                            .run { if (pagerState.currentPage == index) background(MaterialTheme.colorScheme.primary) else background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)) }
                    )
                }
            }
        }

        // Media counter badge
        if (media.size > 1) {
            Surface(
                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f)
            ) {
                Text(
                    "${pagerState.currentPage + 1} / ${media.size}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ObsStatItem(value: String, label: String, icon: MaterialSymbolIcon) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 18.dp)
        Text(value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun NoteDetailContent(
    n: NoteEntity,
    onOpenDetail: (String, Long) -> Unit
) {
    val colors = FieldMindTheme.colors
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier.size(48.dp).clip(RoundedCornerShape(16.dp))
                        .background(colors.source.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) { Icon(FieldMindIcons.Note, null, tint = colors.source, size = 26.dp) }
                Column(Modifier.weight(1f)) {
                    Text(n.title.ifBlank { "Untitled note" }, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoChip(n.category.ifBlank { "Uncategorized" }, icon = FieldMindIcons.Category)
                        InfoChip(recentRelativeTime(n.updatedAt), icon = FieldMindIcons.Calendar)
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Body
            if (n.body.isNotBlank()) {
                Text(n.body, style = MaterialTheme.typography.bodyLarge)
            }

            // Tags
            if (n.tags.isNotBlank()) {
                FlowRow(verticalArrangement = Arrangement.spacedBy(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    n.tags.split(",").filter { it.isNotBlank() }.forEach { tag ->
                        TagChip(tag.trim())
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Entity-Specific Detail Composables
// ══════════════════════════════════════════════════════════════════════

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun QuestionDetailContent(
    qn: QuestionEntity,
    hypotheses: List<HypothesisEntity>,
    onSaveAnswer: (String) -> Unit
) {
    val colors = FieldMindTheme.colors
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header with icon and badges
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier.size(48.dp).clip(RoundedCornerShape(16.dp))
                        .background(colors.question.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) { Icon(FieldMindIcons.Question, null, tint = colors.question, size = 26.dp) }
                Column(Modifier.weight(1f)) {
                    Text(qn.questionText.ifBlank { "Question" }, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoChip(qn.category, icon = FieldMindIcons.Category)
                        StatusChip(qn.status, colors.question)
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Details
            DetailRow("Source type", qn.sourceType)
            DetailRow("Priority", qn.priority)
            if (qn.relatedProjectId != null) {
                DetailRow("Project", "Linked to project")
            }

            // Answer section
            QuestionAnswerCard(qn, onSaveAnswer)

            // Linked hypotheses
            val linked = hypotheses.filter { it.linkedQuestionId == qn.id }
            if (linked.isNotEmpty()) {
                Text("Hypotheses (${linked.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = colors.hypothesis)
                linked.forEach { h ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .background(colors.hypothesis.copy(alpha = 0.08f)).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(FieldMindIcons.Hypothesis, null, tint = colors.hypothesis, size = 18.dp)
                        Column(Modifier.weight(1f)) {
                            Text(h.prediction, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text("Confidence: ${h.confidencePercent}%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun HypothesisDetailContent(
    h: HypothesisEntity
) {
    val colors = FieldMindTheme.colors
    val resultColor = when ((h.resultStatus ?: "").lowercase()) {
        "supported" -> colors.positive
        "refuted" -> MaterialTheme.colorScheme.error
        "inconclusive" -> colors.warning
        else -> colors.hypothesis
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier.size(48.dp).clip(RoundedCornerShape(16.dp))
                        .background(colors.hypothesis.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) { Icon(FieldMindIcons.Hypothesis, null, tint = colors.hypothesis, size = 26.dp) }
                Column(Modifier.weight(1f)) {
                    Text(h.prediction.ifBlank { "Hypothesis" }, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    StatusChip(h.resultStatus.ifBlank { "Untested" }, resultColor)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Reasoning
            if (h.reasoning.isNotBlank()) {
                Text("Reasoning", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = colors.hypothesis)
                Text(h.reasoning, style = MaterialTheme.typography.bodyMedium)
            }

            // Support criteria
            if (h.supportCriteria.isNotBlank()) {
                Text("Support criteria", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = colors.positive)
                Text(h.supportCriteria, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Weakening criteria
            if (h.weakeningCriteria.isNotBlank()) {
                Text("Weakening criteria", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
                Text(h.weakeningCriteria, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Test method
            if (h.testMethod.isNotBlank()) {
                Text("Test method", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = colors.info)
                Text(h.testMethod, style = MaterialTheme.typography.bodyMedium)
            }

            // Confidence meter
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Confidence", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${h.confidencePercent}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = resultColor)
            }
            LinearProgressIndicator(
                progress = { (h.confidencePercent).coerceIn(0, 100) / 100f },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = resultColor,
                trackColor = resultColor.copy(alpha = 0.12f)
            )
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun ProjectDetailContent(
    p: ProjectEntity,
    observations: List<ObservationEntity>,
    questions: List<QuestionEntity>,
    sources: List<SourceEntity>,
    dataRecords: List<DataRecordEntity>,
    reports: List<ReportEntity>,
    viewModel: FieldMindViewModel
) {
    val colors = FieldMindTheme.colors
    val haptics = rememberFieldMindHaptics()
    var tab by remember { mutableIntStateOf(0) }
    val projectTabs = listOf("Overview", "Questions", "Observations", "Evidence", "Reports", "Sources", "Species", "Tasks")
    val obsCount = observations.count { it.projectId == p.id }
    val qCount = questions.count { it.relatedProjectId == p.id }
    val srcCount = sources.count { it.relatedProjectId == p.id }
    val dataCount = dataRecords.count { it.projectId == p.id }
    val repCount = reports.count { it.projectId == p.id }
    val projectObs = observations.filter { it.projectId == p.id }
    val projectQs = questions.filter { it.relatedProjectId == p.id }
    val projectSrcs = sources.filter { it.relatedProjectId == p.id }
    val projectReports = reports.filter { it.projectId == p.id }
    val projectData = dataRecords.filter { it.projectId == p.id }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header with action bar
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier.size(48.dp).clip(RoundedCornerShape(16.dp))
                        .background(colors.project.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) { Icon(FieldMindIcons.Project, null, tint = colors.project, size = 26.dp) }
                Column(Modifier.weight(1f)) {
                    Text(p.title.ifBlank { "Project" }, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusChip(p.status.ifBlank { "Active" }, colors.project)
                        InfoChip(p.topicType)
                    }
                }
            }

            // Tab row (now includes Species and Tasks)
            ScrollableTabRow(selectedTabIndex = tab, edgePadding = 0.dp, containerColor = androidx.compose.ui.graphics.Color.Transparent) {
                projectTabs.forEachIndexed { i, label ->
                    Tab(tab == i, { haptics.light(); tab = i }, text = { 
                        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = if (tab == i) FontWeight.Bold else FontWeight.Normal) 
                    })
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Tab content (existing tabs preserved, new tabs added)
            when (tab) {
                0 -> { // Overview — stats + description
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        ProjectStatTile("${obsCount}", "Observations", colors.observation)
                        ProjectStatTile("${qCount}", "Questions", colors.question)
                        ProjectStatTile("${srcCount}", "Sources", colors.source)
                        ProjectStatTile("${dataCount}", "Data", colors.data)
                        ProjectStatTile("${repCount}", "Reports", colors.report)
                    }
                    if (p.researchQuestion.isNotBlank()) {
                        Text("Research question", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = colors.project)
                        Text(p.researchQuestion, style = MaterialTheme.typography.bodyMedium)
                    }
                    if (p.objective.isNotBlank()) {
                        Text("Objective", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(p.objective, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (p.methods.isNotBlank()) {
                        Text("Methods", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = colors.info)
                        Text(p.methods, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (p.conclusion.isNotBlank()) {
                        Text("Conclusion", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = colors.positive)
                        Text(p.conclusion, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                1 -> { // Questions
                    if (projectQs.isEmpty()) {
                        Text("No questions for this project yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        projectQs.forEach { q ->
                            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(colors.question.copy(alpha = 0.06f)).padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(FieldMindIcons.Question, null, tint = colors.question, size = 18.dp)
                                Column(Modifier.weight(1f)) {
                                    Text(q.questionText, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    Text("Status: ${q.status}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
                2 -> { // Observations
                    if (projectObs.isEmpty()) {
                        Text("No observations linked to this project.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        projectObs.take(5).forEach { o ->
                            EntityCard(o.subject.ifBlank { "Observation" }, "observation",
                                body = "${o.category} • ${o.date}",
                                meta = listOf(o.confidenceLevel)) { }
                        }
                        if (projectObs.size > 5) {
                            Text("+${projectObs.size - 5} more observations", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                3 -> { // Evidence
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        ProjectStatTile("${projectObs.count { it.evidenceSummary.isNotBlank() }}", "With evidence", colors.observation)
                        ProjectStatTile("${projectObs.count { it.weatherTemperature != null }}", "Weather data", colors.info)
                    }
                    Text("Collect photos, audio, video, and notes as evidence for each observation.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                4 -> { // Reports
                    if (projectReports.isEmpty()) {
                        Text("No reports for this project.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        projectReports.forEach { r ->
                            EntityCard(r.title, "report", body = r.conclusion.ifBlank { r.question }, meta = listOf(r.type, r.status)) { }
                        }
                    }
                }
                5 -> { // Sources
                    if (projectSrcs.isEmpty()) {
                        Text("No sources linked to this project.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        projectSrcs.forEach { s ->
                            EntityCard(s.title, "source", body = s.author, meta = listOf(s.type, s.readingStatus)) { }
                        }
                    }
                }
                6 -> { // Species Registry Builder
                    SpeciesRegistryBuilder(p.id, viewModel)
                }
                7 -> { // Project Tasks Builder
                    ProjectTasksBuilder(p.id, viewModel)
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Species Registry Builder — Full taxonomy form + list per spec
// ══════════════════════════════════════════════════════════════════════

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun SpeciesRegistryBuilder(projectId: Long, viewModel: FieldMindViewModel) {
    val colors = FieldMindTheme.colors
    val haptics = rememberFieldMindHaptics()
    var showForm by remember { mutableStateOf(false) }
    
    // Form state
    var commonName by remember { mutableStateOf("") }
    var scientificName by remember { mutableStateOf("") }
    var kingdom by remember { mutableStateOf("") }
    var phylum by remember { mutableStateOf("") }
    var classs by remember { mutableStateOf("") }
    var order by remember { mutableStateOf("") }
    var family by remember { mutableStateOf("") }
    var genus by remember { mutableStateOf("") }
    var speciesName by remember { mutableStateOf("") }
    var conservationStatus by remember { mutableStateOf("Not Evaluated") }
    var targetCount by remember { mutableStateOf("") }
    var autoCount by remember { mutableStateOf(false) }

    // Live species list for this project
    val allSpecies by viewModel.speciesRegistry.collectAsState()
    val projectSpecies = remember(allSpecies, projectId) { allSpecies.filter { it.projectId == projectId } }

    val conservationOptions = listOf("Not Evaluated", "Data Deficient", "Least Concern", "Near Threatened", "Vulnerable", "Endangered", "Critically Endangered", "Extinct in Wild", "Extinct")
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = { showForm = !showForm; if (!showForm) { commonName = ""; scientificName = ""; speciesName = "" } },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(FieldMindIcons.Add, null, size = 18.dp)
            Spacer(Modifier.size(6.dp))
            Text(if (showForm) "Cancel" else "Add Species")
        }

        if (showForm) {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Add Species to Registry", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = colors.observation)
                    FieldTextField(commonName, { commonName = it }, "Common Name *", supportingText = "e.g. House Crow")
                    FieldTextField(scientificName, { scientificName = it }, "Scientific Name", supportingText = "e.g. Corvus splendens")
                    Text("Taxonomy", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FieldTextField(kingdom, { kingdom = it }, "Kingdom", modifier = Modifier.weight(1f))
                        FieldTextField(phylum, { phylum = it }, "Phylum", modifier = Modifier.weight(1f))
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FieldTextField(classs, { classs = it }, "Class", modifier = Modifier.weight(1f))
                        FieldTextField(order, { order = it }, "Order", modifier = Modifier.weight(1f))
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FieldTextField(family, { family = it }, "Family", modifier = Modifier.weight(1f))
                        FieldTextField(genus, { genus = it }, "Genus", modifier = Modifier.weight(1f))
                    }
                    FieldTextField(speciesName, { speciesName = it }, "Species", supportingText = "Specific epithet")
                    Text("Conservation Status", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    ChoiceChips(conservationOptions, conservationStatus) { conservationStatus = it }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        FieldTextField(targetCount, { targetCount = it }, "Target Count", modifier = Modifier.weight(1f), supportingText = "Goal")
                        FilterChip(selected = autoCount, onClick = { autoCount = !autoCount }, label = { Text("Auto Count", style = MaterialTheme.typography.labelSmall) })
                    }
                    Button(
                        onClick = {
                            haptics.confirm()
                            viewModel.addSpecies(
                                commonName = commonName,
                                scientificName = scientificName,
                                kingdom = kingdom, phylum = phylum, classs = classs,
                                order = order, family = family, genus = genus,
                                species = speciesName,
                                conservationStatus = conservationStatus,
                                targetCount = targetCount.toIntOrNull() ?: 0,
                                autoCountTracking = autoCount,
                                projectId = projectId
                            )
                            showForm = false
                            commonName = ""; scientificName = ""; speciesName = ""
                            kingdom = ""; phylum = ""; classs = ""
                            order = ""; family = ""; genus = ""
                        },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                        enabled = commonName.isNotBlank()
                    ) { Text("Save to Registry") }
                }
            }
        }

        Text("Species Registry (${projectSpecies.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        if (projectSpecies.isEmpty()) {
            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(FieldMindIcons.Nature, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), size = 40.dp)
                        Text("No species registered yet", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Add species with full taxonomy and conservation status", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                }
            }
        } else {
            projectSpecies.forEach { sp ->
                Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(FieldMindIcons.Nature, null, tint = colors.observation, size = 18.dp)
                            Column(Modifier.weight(1f)) {
                                Text(sp.commonName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                if (sp.scientificName.isNotBlank()) Text(sp.scientificName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (sp.conservationStatus != "Not Evaluated") {
                                val statusColor = when {
                                    sp.conservationStatus.contains("Endangered") || sp.conservationStatus.contains("Critically") -> MaterialTheme.colorScheme.error
                                    sp.conservationStatus.contains("Vulnerable") || sp.conservationStatus.contains("Near") -> colors.warning
                                    else -> colors.positive
                                }
                                Surface(shape = RoundedCornerShape(8.dp), color = statusColor.copy(alpha = 0.12f)) {
                                    Text(sp.conservationStatus.take(12), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = statusColor)
                                }
                            }
                        }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOfNotNull(sp.genus.takeIf { it.isNotBlank() }, sp.family.takeIf { it.isNotBlank() }, sp.order.takeIf { it.isNotBlank() }).forEach { tax ->
                                Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surfaceContainerHighest) {
                                    Text(tax, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Project Tasks Builder — Per spec: title, type, priority, due date, assignee, subtasks
// ══════════════════════════════════════════════════════════════════════

@Composable
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProjectTasksBuilder(projectId: Long, viewModel: FieldMindViewModel) {
    val colors = FieldMindTheme.colors
    val haptics = rememberFieldMindHaptics()
    var showForm by remember { mutableStateOf(false) }
    val taskTypes = listOf("Field Survey", "Observation Collection", "Species Count", "Audio Recording", "Photo Collection", "Video Collection", "Habitat Mapping", "Literature Review", "Data Analysis", "Report Writing", "Verification", "Sample Collection", "GPS Tracking", "Custom")
    val priorityLevels = listOf("Low", "Medium", "High")
    
    // Form state
    var taskTitle by remember { mutableStateOf("") }
    var taskDesc by remember { mutableStateOf("") }
    var taskType by remember { mutableStateOf(taskTypes[0]) }
    var taskPriority by remember { mutableStateOf("Medium") }
    var taskDueDate by remember { mutableStateOf("") }
    var taskAssignee by remember { mutableStateOf("") }
    var subtaskInput by remember { mutableStateOf("") }
    var subtasks by remember { mutableStateOf<List<String>>(emptyList()) }

    // Live task list for this project
    val allTasks by viewModel.tasks.collectAsState()
    val projectTasks = remember(allTasks, projectId) { allTasks.filter { it.projectId == projectId } }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = { showForm = !showForm; if (!showForm) { taskTitle = ""; taskDesc = ""; subtasks = emptyList() } },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)
        ) {
            Icon(FieldMindIcons.Add, null, size = 18.dp)
            Spacer(Modifier.size(6.dp))
            Text(if (showForm) "Cancel" else "Add Task")
        }

        if (showForm) {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Create Project Task", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = colors.project)
                    FieldTextField(taskTitle, { taskTitle = it }, "Task Title *", supportingText = "e.g. Survey Zone A")
                    FieldTextField(taskDesc, { taskDesc = it }, "Description", minLines = 2)
                    Text("Task Type", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(taskTypes) { type ->
                            FilterChip(selected = taskType == type, onClick = { taskType = type }, label = { Text(type, style = MaterialTheme.typography.labelSmall, maxLines = 1) })
                        }
                    }
                    Text("Priority", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        priorityLevels.forEach { p ->
                            FilterChip(
                                selected = taskPriority == p,
                                onClick = { taskPriority = p },
                                label = { Text(p) },
                                leadingIcon = if (taskPriority == p) ({ Icon(FieldMindIcons.Check, null, size = 16.dp) }) else null
                            )
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FieldTextField(taskDueDate, { taskDueDate = it }, "Due Date", modifier = Modifier.weight(1f), supportingText = "YYYY-MM-DD")
                        FieldTextField(taskAssignee, { taskAssignee = it }, "Assigned To", modifier = Modifier.weight(1f))
                    }
                    Text("Subtasks", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = subtaskInput, onValueChange = { subtaskInput = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Add subtask...") },
                            shape = RoundedCornerShape(12.dp), singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                        FilledTonalButton(onClick = { if (subtaskInput.isNotBlank()) { subtasks = subtasks + subtaskInput; subtaskInput = "" } }, shape = RoundedCornerShape(12.dp)) {
                            Icon(FieldMindIcons.Add, null, size = 18.dp)
                        }
                    }
                    subtasks.forEach { sub ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(FieldMindIcons.List, null, tint = colors.project, size = 16.dp)
                            Text(sub, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            IconButton(onClick = { subtasks = subtasks - sub }, modifier = Modifier.size(24.dp)) {
                                Icon(FieldMindIcons.Close, null, size = 14.dp)
                            }
                        }
                    }
                    Button(
                        onClick = {
                            haptics.confirm()
                            viewModel.addTask(
                                title = taskTitle,
                                description = taskDesc,
                                taskType = taskType,
                                priority = taskPriority,
                                dueDate = taskDueDate,
                                assignedTo = taskAssignee,
                                projectId = projectId
                            )
                            // Create subtasks as separate tasks linked via parentTaskId
                            var parentId: Long? = null
                            subtasks.forEach { sub ->
                                viewModel.addTask(
                                    title = sub,
                                    taskType = taskType,
                                    priority = taskPriority,
                                    projectId = projectId,
                                    parentTaskId = parentId
                                )
                            }
                            showForm = false
                            taskTitle = ""; taskDesc = ""; subtasks = emptyList()
                        },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                        enabled = taskTitle.isNotBlank()
                    ) { Text(if (subtasks.isEmpty()) "Save Task" else "Save Task with ${subtasks.size} Subtasks") }
                }
            }
        }

        Text("Project Tasks (${projectTasks.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        if (projectTasks.isEmpty()) {
            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(FieldMindIcons.Check, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), size = 40.dp)
                        Text("No tasks created yet", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Tasks can be linked to observations, species, and questions", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                }
            }
        } else {
            projectTasks.forEach { task ->
                val priorityColor = when (task.priority.lowercase()) {
                    "high" -> MaterialTheme.colorScheme.error
                    "medium" -> colors.warning
                    else -> colors.positive
                }
                Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(
                            if (task.status == "Completed") FieldMindIcons.Check else FieldMindIcons.List,
                            null,
                            tint = if (task.status == "Completed") colors.positive else colors.project,
                            size = 20.dp
                        )
                        Column(Modifier.weight(1f)) {
                            Text(task.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(shape = RoundedCornerShape(6.dp), color = colors.project.copy(alpha = 0.1f)) {
                                    Text(task.taskType, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = colors.project)
                                }
                                Surface(shape = RoundedCornerShape(6.dp), color = priorityColor.copy(alpha = 0.1f)) {
                                    Text(task.priority, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = priorityColor)
                                }
                            }
                        }
                        if (task.dueDate.isNotBlank()) {
                            Text(task.dueDate, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectStatTile(value: String, label: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DataRecordDetailContent(
    d: DataRecordEntity
) {
    val colors = FieldMindTheme.colors
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier.size(48.dp).clip(RoundedCornerShape(16.dp))
                        .background(colors.data.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) { Icon(FieldMindIcons.Data, null, tint = colors.data, size = 26.dp) }
                Column(Modifier.weight(1f)) {
                    Text(d.label.ifBlank { "Data record" }, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    InfoChip(d.toolType, icon = FieldMindIcons.Data)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Value display (prominent)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "${d.value} ${d.unit}".trim(),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.data
                    )
                    Text(d.toolType, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Location
            if (d.locatio
