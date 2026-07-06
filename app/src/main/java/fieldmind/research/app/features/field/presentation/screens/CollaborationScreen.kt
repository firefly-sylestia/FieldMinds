package fieldmind.research.app.features.field.presentation.screens

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.core.content.FileProvider
import fieldmind.research.app.features.field.data.export.FieldMindExport
import fieldmind.research.app.features.field.presentation.components.*
import fieldmind.research.app.features.field.presentation.theme.FieldMindTheme
import fieldmind.research.app.features.field.presentation.viewmodel.FieldMindViewModel
import fieldmind.research.app.shared.presentation.components.icons.Icon
import fieldmind.research.app.shared.presentation.components.icons.MaterialSymbolIcon
import fieldmind.research.app.ui.theme.CuteElevations
import fieldmind.research.app.ui.theme.cuteShadow
import fieldmind.research.app.ui.theme.screenBackground
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Collaboration & Sharing — share observations, projects, and data with
 * other researchers via export, QR codes, or direct sharing.
 */
@Composable
fun CollaborationScreen(
    viewModel: FieldMindViewModel,
    onBack: () -> Unit = {},
    onOpenExport: () -> Unit = {}
) {
    val observations by viewModel.observations.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val colors = FieldMindTheme.colors
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var expandedSection by remember { mutableStateOf<String?>(null) }
    var shareFormat by remember { mutableStateOf("FieldMind Archive") }
    var includeMedia by remember { mutableStateOf(true) }
    var isExporting by remember { mutableStateOf(false) }
    var exportProgress by remember { mutableFloatStateOf(0f) }
    var exportStepText by remember { mutableStateOf("") }

    val shareFormats = listOf("CSV", "JSON", "PDF Report", "FieldMind Archive")

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
                        title = "Collaborate",
                        subtitle = "Share your research with others",
                        icon = MaterialSymbolIcon("share"),
                        heroColor = colors.positive,
                        trailing = { BackButton(onClick = onBack) }
                    )
                }

                // ── Quick stats ──
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetricTile(
                            "Observations", observations.size.toString(),
                            FieldMindIcons.Observation, Modifier.weight(1f), colors.observation
                        )
                        MetricTile(
                            "Projects", projects.size.toString(),
                            FieldMindIcons.Project, Modifier.weight(1f), colors.project
                        )
                    }
                }

                // ── Collaboration cards ──

                // 1. Export & Share
                item {
                    CollaborationCard(
                        title = "Export & Share",
                        subtitle = "Export your data for collaborators or publishing",
                        icon = MaterialSymbolIcon("file_download"),
                        accent = colors.positive,
                        isExpanded = expandedSection == "export",
                        onToggle = { expandedSection = if (expandedSection == "export") null else "export" },
                        content = {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                // Format picker
                                Text("Export format", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    shareFormats.forEach { format ->
                                        FilterChip(
                                            selected = shareFormat == format,
                                            onClick = { shareFormat = format },
                                            label = { Text(format, fontSize = 11.sp) }
                                        )
                                    }
                                }

                                // Options
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = includeMedia, onCheckedChange = { includeMedia = it })
                                    Text("Include media attachments", style = MaterialTheme.typography.bodySmall)
                                }

                                if (isExporting) {
                                    LinearProgressIndicator(
                                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                                    )
                                    Text(
                                        exportStepText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                isExporting = true
                                                exportProgress = 0f
                                                exportStepText = "Preparing $shareFormat…"
                                                try {
                                                    withContext(Dispatchers.IO) {
                                                        val dateStamp = SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.getDefault()).format(Date())
                                                        val exportDir = File(context.cacheDir, "collaboration_exports").apply { mkdirs() }

                                                        // Collect all entity data from ViewModel
                                                        val obs = viewModel.observations.value
                                                        val nts = viewModel.notes.value
                                                        val qs = viewModel.questions.value
                                                        val hyps = viewModel.hypotheses.value
                                                        val projs = viewModel.projects.value
                                                        val srcs = viewModel.sources.value
                                                        val drs = viewModel.dataRecords.value
                                                        val rpts = viewModel.reports.value
                                                        val fcards = viewModel.flashcards.value
                                                        val spcs = viewModel.speciesRegistry.value
                                                        val wcat = viewModel.weatherCatalog.value
                                                        val rsessions = viewModel.researchSessions.value
                                                        val tks = viewModel.tasks.value

                                                        exportProgress = 0.3f
                                                        exportStepText = "Generating $shareFormat…"

                                                        val ext = when (shareFormat) {
                                                            "CSV" -> "csv"
                                                            "JSON" -> "json"
                                                            "PDF Report" -> "pdf"
                                                            "FieldMind Archive" -> "fieldmind"
                                                            else -> "md"
                                                        }
                                                        val fileName = "fieldmind-collab-$dateStamp.$ext"
                                                        val exportFile = File(exportDir, fileName)

                                                        when (shareFormat) {
                                                            "CSV" -> {
                                                                exportFile.writeText(FieldMindExport.observationsCsv(obs))
                                                            }
                                                            "JSON" -> {
                                                                val json = FieldMindExport.archiveJson(
                                                                    observations = obs, notes = nts,
                                                                    questions = qs, hypotheses = hyps,
                                                                    projects = projs, sources = srcs,
                                                                    dataRecords = drs, reports = rpts,
                                                                    flashcards = fcards, species = spcs,
                                                                    weatherCatalog = wcat,
                                                                    researchSessions = rsessions,
                                                                    tasks = tks
                                                                )
                                                                exportFile.writeText(json)
                                                            }
                                                            "PDF Report" -> {
                                                                val bodyText = obs.joinToString("\n") {
                                                                    FieldMindExport.singleObservationMarkdown(it)
                                                                }
                                                                exportFile.writeBytes(
                                                                    FieldMindExport.simplePdfBytes("FieldMind Collaboration", bodyText)
                                                                )
                                                            }
                                                            "FieldMind Archive" -> {
                                                                val json = FieldMindExport.archiveJson(
                                                                    observations = obs, notes = nts,
                                                                    questions = qs, hypotheses = hyps,
                                                                    projects = projs, sources = srcs,
                                                                    dataRecords = drs, reports = rpts,
                                                                    flashcards = fcards, species = spcs,
                                                                    weatherCatalog = wcat,
                                                                    researchSessions = rsessions,
                                                                    tasks = tks
                                                                )
                                                                val allAttachments = mutableMapOf<Long, List<fieldmind.research.app.features.field.data.database.entity.EvidenceAttachmentEntity>>()
                                                        obs.forEach { o ->
                                                            runCatching {
                                                                val atts = viewModel.attachmentsForObservation(o.id).first()
                                                                if (atts.isNotEmpty()) allAttachments[o.id] = atts
                                                            }
                                                        }
                                                        val result = fieldmind.research.app.features.field.data.export.FieldMindExportMediaPacker.buildPackage(
                                                                    context = context, archiveJson = json,
                                                                    observations = obs, notes = nts,
                                                                    projects = projs, sources = srcs,
                                                                    attachments = allAttachments,
                                                                    outputDir = exportDir
                                                                )
                                                                result.packageFile.copyTo(exportFile, overwrite = true)
                                                            }
                                                        }

                                                        exportProgress = 0.7f
                                                        exportStepText = "Building share intent…"

                                                        val shareUri = FileProvider.getUriForFile(
                                                            context,
                                                            "${context.packageName}.provider",
                                                            exportFile
                                                        )
                                                        val mimeType = when (shareFormat) {
                                                            "CSV" -> "text/csv"
                                                            "JSON" -> "application/json"
                                                            "PDF Report" -> "application/pdf"
                                                            "FieldMind Archive" -> "application/octet-stream"
                                                            else -> "text/plain"
                                                        }

                                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                            type = mimeType
                                                            putExtra(Intent.EXTRA_STREAM, shareUri)
                                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                        }
                                                        context.startActivity(Intent.createChooser(shareIntent, "Share FieldMind data"))
                                                    }
                                                    exportStepText = "Done"
                                                    showFastSnackbar(snackbar, scope, "$shareFormat exported and shared")
                                                } catch (e: Exception) {
                                                    showFastSnackbar(snackbar, scope, "Export failed: ${e.localizedMessage?.take(100) ?: "Unknown error"}")
                                                } finally {
                                                    isExporting = false
                                                    exportProgress = 0f
                                                }
                                            }
                                        },
                                        shape = RoundedCornerShape(22.dp),
                                        enabled = !isExporting
                                    ) {
                                        Icon(MaterialSymbolIcon("file_download"), null, size = 18.dp)
                                        Spacer(Modifier.size(6.dp))
                                        Text("Export & share")
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            // Generate a brief Markdown summary and share as text
                                            val summary = buildString {
                                                appendLine("# FieldMind Collaboration Summary")
                                                appendLine()
                                                appendLine("Shared from FieldMind on ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}")
                                                appendLine()
                                                appendLine("## Stats")
                                                appendLine("- Observations: ${observations.size}")
                                                appendLine("- Projects: ${projects.size}")
                                                appendLine("- Notes: ${viewModel.notes.value.size}")
                                                appendLine("- Sources: ${viewModel.sources.value.size}")
                                                appendLine()
                                                appendLine("## Recent observations")
                                                observations.take(10).forEach { obs ->
                                                    appendLine("- ${obs.subject} (${obs.date} ${obs.time})")
                                                }
                                                if (observations.size > 10) {
                                                    appendLine("- ... and ${observations.size - 10} more")
                                                }
                                                appendLine()
                                                appendLine("---")
                                                appendLine("Generated by FieldMind — an offline-first research notebook.")
                                            }
                                            safeShareText(
                                                context = context,
                                                snackbar = snackbar,
                                                scope = scope,
                                                chooserTitle = "Share summary via",
                                                clipboardLabel = "FieldMind collaboration summary",
                                                text = summary
                                            )
                                        },
                                        shape = RoundedCornerShape(22.dp)
                                    ) {
                                        Icon(MaterialSymbolIcon("share"), null, size = 18.dp)
                                        Spacer(Modifier.size(6.dp))
                                        Text("Share summary")
                                    }
                                }
                            }
                        }
                    )
                }

                // 2. Team Workspace
                item {
                    CollaborationCard(
                        title = "Team Workspace",
                        subtitle = "Invite collaborators and manage shared projects",
                        icon = MaterialSymbolIcon("group"),
                        accent = colors.info,
                        isExpanded = expandedSection == "team",
                        onToggle = { expandedSection = if (expandedSection == "team") null else "team" },
                        content = {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                                ) {
                                    Row(
                                        Modifier.fillMaxWidth().padding(14.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(Modifier.size(40.dp).clip(CircleShape).background(colors.info.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                                            Icon(MaterialSymbolIcon("person_add"), null, tint = colors.info, size = 22.dp)
                                        }
                                        Column(Modifier.weight(1f)) {
                                            Text("Invite collaborator", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                            Text("Send an invitation to collaborate on your projects", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        FilledTonalButton(
                                            onClick = {
                                                safeShareText(
                                                    context = context,
                                                    snackbar = snackbar,
                                                    scope = scope,
                                                    chooserTitle = "Invite via",
                                                    clipboardLabel = "FieldMind collaboration invite",
                                                    text = "Join my FieldMind research workspace! Download FieldMind to collaborate."
                                                )
                                            },
                                            shape = RoundedCornerShape(22.dp)
                                        ) { Text("Invite") }
                                    }
                                }

                                // Active projects for collaboration
                                val activeProjects = projects.filter { it.status == "Active" }
                                if (activeProjects.isNotEmpty()) {
                                    Text("Shared projects", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                    activeProjects.take(5).forEach { project ->
                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                Modifier.fillMaxWidth().padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Box(Modifier.size(36.dp).clip(RoundedCornerShape(18.dp)).background(colors.project.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                                                    Icon(FieldMindIcons.Project, null, tint = colors.project, size = 20.dp)
                                                }
                                                Column(Modifier.weight(1f)) {
                                                    Text(project.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                    Text("Tap to share", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                                Icon(MaterialSymbolIcon("share"), null, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 18.dp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    )
                }

                // 3. Publish & Showcase
                item {
                    CollaborationCard(
                        title = "Publish & Showcase",
                        subtitle = "Create a shareable portfolio of your research",
                        icon = MaterialSymbolIcon("public"),
                        accent = colors.warning,
                        isExpanded = expandedSection == "publish",
                        onToggle = { expandedSection = if (expandedSection == "publish") null else "publish" },
                        content = {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(40.dp).clip(RoundedCornerShape(20.dp)).background(colors.warning.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                                            Icon(MaterialSymbolIcon("public"), null, tint = colors.warning, size = 22.dp)
                                        }
                                        Column(Modifier.weight(1f)) {
                                            Text("Research Portfolio", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                            Text("Generate a public profile with your key findings", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                val portfolio = withContext(Dispatchers.IO) {
                                                    val allObs = viewModel.observations.value
                                                    val allNotes = viewModel.notes.value
                                                    val allQs = viewModel.questions.value
                                                    val allHyps = viewModel.hypotheses.value
                                                    val allProjs = viewModel.projects.value
                                                    val allSrcs = viewModel.sources.value
                                                    val allRpts = viewModel.reports.value
                                                    val allFcards = viewModel.flashcards.value
                                                    val allData = viewModel.dataRecords.value
                                                    val allSessions = viewModel.researchSessions.value
                                                    val allTks = viewModel.tasks.value

                                                    val dateStamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                                                    buildString {
                                                        appendLine("# FieldMind Research Portfolio")
                                                        appendLine()
                                                        appendLine("*Generated on $dateStamp*")
                                                        appendLine()
                                                        appendLine("---")
                                                        appendLine()

                                                        // ── Overview Stats ──
                                                        appendLine("## Overview")
                                                        appendLine()
                                                        appendLine("| Category | Count |")
                                                        appendLine("|----------|-------|")
                                                        appendLine("| Observations | ${allObs.size} |")
                                                        appendLine("| Notes | ${allNotes.size} |")
                                                        appendLine("| Questions | ${allQs.size} |")
                                                        appendLine("| Hypotheses | ${allHyps.size} |")
                                                        appendLine("| Projects | ${allProjs.size} |")
                                                        appendLine("| Sources | ${allSrcs.size} |")
                                                        appendLine("| Reports | ${allRpts.size} |")
                                                        appendLine("| Flashcards | ${allFcards.size} |")
                                                        appendLine("| Data Records | ${allData.size} |")
                                                        appendLine("| Research Sessions | ${allSessions.size} |")
                                                        appendLine("| Tasks | ${allTks.size} |")
                                                        appendLine()

                                                        // ── Projects ──
                                                        if (allProjs.isNotEmpty()) {
                                                            appendLine("---")
                                                            appendLine()
                                                            appendLine("## Projects")
                                                            appendLine()
                                                            allProjs.forEach { p ->
                                                                val obsCount = allObs.count { it.projectId == p.id }
                                                                appendLine("### ${p.title}")
                                                                appendLine("- **Status:** ${p.status}")
                                                                if (p.methodology.isNotBlank()) appendLine("- **Methodology:** ${p.methodology}")
                                                                if (p.description.isNotBlank()) appendLine("- **Description:** ${p.description.take(200)}")
                                                                appendLine("- **Linked observations:** $obsCount")
                                                                appendLine()
                                                            }
                                                        }

                                                        // ── Recent Observations ──
                                                        if (allObs.isNotEmpty()) {
                                                            appendLine("---")
                                                            appendLine()
                                                            appendLine("## Recent Observations")
                                                            appendLine()
                                                            allObs.sortedByDescending { it.timestamp }.take(20).forEach { o ->
                                                                appendLine("### ${o.subject}")
                                                                appendLine("- **Date:** ${o.date} ${o.time}")
                                                                appendLine("- **Category:** ${o.category}")
                                                                appendLine("- **Confidence:** ${o.confidence}")
                                                                if (o.manualLocation.isNotBlank()) appendLine("- **Location:** ${o.manualLocation}")
                                                                if (o.facts.isNotBlank()) appendLine("- **Notes:** ${o.facts.take(300)}")
                                                                if (o.tags.isNotBlank()) appendLine("- **Tags:** ${o.tags}")
                                                                appendLine()
                                                            }
                                                            if (allObs.size > 20) {
                                                                appendLine("... and ${allObs.size - 20} more observations")
                                                                appendLine()
                                                            }
                                                        }

                                                        // ── Questions & Hypotheses ──
                                                        if (allQs.isNotEmpty()) {
                                                            appendLine("---")
                                                            appendLine()
                                                            appendLine("## Research Questions")
                                                            appendLine()
                                                            allQs.forEach { q ->
                                                                appendLine("- **${q.question}**")
                                                                if (q.answer.isNotBlank()) appendLine("  - Answer: ${q.answer.take(200)}")
                                                                appendLine()
                                                            }
                                                        }

                                                        // ── Notes ──
                                                        if (allNotes.isNotEmpty()) {
                                                            appendLine("---")
                                                            appendLine()
                                                            appendLine("## Recent Notes")
                                                            appendLine()
                                                            allNotes.sortedByDescending { it.updatedAt }.take(10).forEach { n ->
                                                                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(n.updatedAt))
                                                                appendLine("- **${n.title.ifBlank { "Untitled" }}** ($date)")
                                                                if (n.body.isNotBlank()) appendLine("  - ${n.body.take(200)}")
                                                            }
                                                            appendLine()
                                                        }

                                                        // ── Active Research Sessions ──
                                                        val activeSessions = allSessions.filter { it.status == "Active" }
                                                        if (activeSessions.isNotEmpty()) {
                                                            appendLine("---")
                                                            appendLine()
                                                            appendLine("## Active Research Sessions")
                                                            appendLine()
                                                            activeSessions.forEach { s ->
                                                                appendLine("- ${s.name} (started: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(s.startedAt))})")
                                                            }
                                                            appendLine()
                                                        }

                                                        // ── Footer ──
                                                        appendLine("---")
                                                        appendLine()
                                                        appendLine("*Generated by FieldMind — an offline-first field research notebook.*")
                                                        appendLine()
                                                    }
                                                }
                                                safeShareText(
                                                    context = context,
                                                    snackbar = snackbar,
                                                    scope = scope,
                                                    chooserTitle = "Share portfolio via",
                                                    clipboardLabel = "FieldMind research portfolio",
                                                    text = portfolio
                                                )
                                            }
                                        },
                                        shape = RoundedCornerShape(22.dp)
                                    ) {
                                        Icon(MaterialSymbolIcon("description"), null, size = 18.dp)
                                        Spacer(Modifier.size(6.dp))
                                        Text("Generate portfolio")
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            scope.launch {
                                                isExporting = true
                                                exportStepText = "Building complete export…"
                                                try {
                                                    withContext(Dispatchers.IO) {
                                                        val allObs = viewModel.observations.value
                                                        val allNotes = viewModel.notes.value
                                                        val allQs = viewModel.questions.value
                                                        val allHyps = viewModel.hypotheses.value
                                                        val allProjs = viewModel.projects.value
                                                        val allSrcs = viewModel.sources.value
                                                        val allDrs = viewModel.dataRecords.value
                                                        val allRpts = viewModel.reports.value
                                                        val allFcards = viewModel.flashcards.value
                                                        val allSpcs = viewModel.speciesRegistry.value
                                                        val allWcat = viewModel.weatherCatalog.value
                                                        val allSessions = viewModel.researchSessions.value
                                                        val allTks = viewModel.tasks.value

                                                        val dateStamp = SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.getDefault()).format(Date())
                                                        val exportDir = File(context.cacheDir, "collaboration_exports").apply { mkdirs() }
                                                        val fileName = "fieldmind-complete-$dateStamp.fieldmind"
                                                        val exportFile = File(exportDir, fileName)

                                                        val json = FieldMindExport.archiveJson(
                                                            observations = allObs, notes = allNotes,
                                                            questions = allQs, hypotheses = allHyps,
                                                            projects = allProjs, sources = allSrcs,
                                                            dataRecords = allDrs, reports = allRpts,
                                                            flashcards = allFcards, species = allSpcs,
                                                            weatherCatalog = allWcat,
                                                            researchSessions = allSessions,
                                                            tasks = allTks
                                                        )

                                                        val allAttachments = mutableMapOf<Long, List<fieldmind.research.app.features.field.data.database.entity.EvidenceAttachmentEntity>>()
                                                        allObs.forEach { o ->
                                                            runCatching {
                                                                val atts = viewModel.attachmentsForObservation(o.id).first()
                                                                if (atts.isNotEmpty()) allAttachments[o.id] = atts
                                                            }
                                                        }

                                                        val result = fieldmind.research.app.features.field.data.export.FieldMindExportMediaPacker.buildPackage(
                                                            context = context, archiveJson = json,
                                                            observations = allObs, notes = allNotes,
                                                            projects = allProjs, sources = allSrcs,
                                                            attachments = allAttachments,
                                                            outputDir = exportDir
                                                        )
                                                        result.packageFile.copyTo(exportFile, overwrite = true)

                                                        val shareUri = FileProvider.getUriForFile(
                                                            context,
                                                            "${context.packageName}.provider",
                                                            exportFile
                                                        )
                                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                            type = "application/octet-stream"
                                                            putExtra(Intent.EXTRA_STREAM, shareUri)
                                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                        }
                                                        context.startActivity(Intent.createChooser(shareIntent, "Export all FieldMind data"))
                                                    }
                                                    showFastSnackbar(snackbar, scope, "Complete export shared")
                                                } catch (e: Exception) {
                                                    showFastSnackbar(snackbar, scope, "Export failed: ${e.localizedMessage?.take(100) ?: "Unknown error"}")
                                                } finally {
                                                    isExporting = false
                                                    exportStepText = ""
                                                }
                                            }
                                        },
                                        shape = RoundedCornerShape(22.dp),
                                        enabled = !isExporting
                                    ) {
                                        Icon(FieldMindIcons.Export, null, size = 18.dp)
                                        Spacer(Modifier.size(6.dp))
                                        Text("Export all")
                                    }
                                }
                            }
                        }
                    )
                }

                // 4. Sync Status
                item {
                    CollaborationCard(
                        title = "Sync & Backup",
                        subtitle = "Last synced: Not configured",
                        icon = MaterialSymbolIcon("sync"),
                        accent = MaterialTheme.colorScheme.primary,
                        isExpanded = expandedSection == "sync",
                        onToggle = { expandedSection = if (expandedSection == "sync") null else "sync" },
                        content = {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(MaterialSymbolIcon("cloud_off"), null, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 20.dp)
                                            Text("Cloud sync not configured", style = MaterialTheme.typography.bodyMedium)
                                        }
                                        Text("Enable backup in Settings to sync your data across devices.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Button(onClick = { onOpenExport() }, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                                    Icon(MaterialSymbolIcon("settings_backup_restore"), null, size = 18.dp)
                                    Spacer(Modifier.size(6.dp))
                                    Text("Open backup settings")
                                }
                            }
                        }
                    )
                }

                // ── Space for bottom nav ──
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

private fun safeShareText(
    context: Context,
    snackbar: SnackbarHostState,
    scope: kotlinx.coroutines.CoroutineScope,
    chooserTitle: String,
    clipboardLabel: String,
    text: String
) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }

    val canShare = shareIntent.resolveActivity(context.packageManager) != null
    if (canShare) {
        runCatching { context.startActivity(Intent.createChooser(shareIntent, chooserTitle)) }
            .onSuccess { return }
            .onFailure { error ->
                if (error !is ActivityNotFoundException) {
                    android.util.Log.w("CollaborationScreen", "Share failed; copying fallback text", error)
                }
            }
    }

    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(clipboardLabel, text))
    showFastSnackbar(snackbar, scope, "No share app found — copied text instead")
}

@Composable
private fun CollaborationCard(
    title: String,
    subtitle: String,
    icon: MaterialSymbolIcon,
    accent: Color,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .cuteShadow(elevation = CuteElevations.nonClickableTier, shape = RoundedCornerShape(34.dp)),
        shape = RoundedCornerShape(34.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    Modifier.size(44.dp).clip(RoundedCornerShape(22.dp))
                        .background(accent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = accent, size = 24.dp)
                }
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Icon(
                    if (isExpanded) MaterialSymbolIcon("expand_less") else MaterialSymbolIcon("expand_more"),
                    null, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 22.dp
                )
            }

            AnimatedVisibility(visible = isExpanded, enter = expandVertically(), exit = shrinkVertically()) {
                Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    content()
                }
            }
        }
    }
}
