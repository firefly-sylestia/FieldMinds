package fieldmind.research.app.features.field.presentation.screens
import fieldmind.research.app.ui.theme.CuteCardDefaults

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fieldmind.research.app.features.field.data.database.entity.*
import fieldmind.research.app.features.field.presentation.components.*
import fieldmind.research.app.features.field.presentation.theme.FieldMindTheme
import fieldmind.research.app.features.field.presentation.viewmodel.FieldMindViewModel
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import fieldmind.research.app.shared.presentation.components.icons.Icon
import fieldmind.research.app.shared.presentation.components.icons.MaterialSymbolIcon
import fieldmind.research.app.ui.theme.CuteElevations
import androidx.activity.compose.BackHandler
import fieldmind.research.app.features.field.presentation.components.LocalFieldMindSnackbar

// ══════════════════════════════════════════════════════════════════════
//  NEW PROJECT SCREEN — Redesigned: name, description, icon, color, template
// ══════════════════════════════════════════════════════════════════════

@Composable
fun NewProjectScreen(viewModel: FieldMindViewModel, onBack: () -> Unit, entity: ProjectEntity? = null) {
    val isEditing = entity != null
    val haptics = rememberFieldMindHaptics()
    var title by rememberSaveable { mutableStateOf(entity?.title ?: "") }
    var description by rememberSaveable { mutableStateOf(entity?.objective ?: "") }
    var selectedIcon by rememberSaveable { mutableStateOf(entity?.selectedMethods ?: "🌿") }
    var selectedColor by rememberSaveable { mutableStateOf(0xFF1F6B4CL) }
    var selectedTemplate by rememberSaveable { mutableStateOf(entity?.topicType ?: entity?.projectType ?: "Empty Project") }
    var showTemplatePicker by rememberSaveable { mutableStateOf(false) }
    val isDirty = title.isNotBlank() || description.isNotBlank()
    var savedEntityId by rememberSaveable { mutableStateOf(0L) }

    // Auto-save draft every 30 seconds when dirty
    val draftHelper = rememberDraftAutoSave(
        key = "new_project",
        isDirty = isDirty && !isEditing,
        onSaveToJson = {
            org.json.JSONObject().apply {
                put("title", title)
                put("description", description)
                put("icon", selectedIcon)
                put("color", selectedColor)
                put("template", selectedTemplate)
            }.toString()
        },
        onRestoreFromJson = { json ->
            val obj = org.json.JSONObject(json)
            title = obj.optString("title", "")
            description = obj.optString("description", "")
            selectedIcon = obj.optString("icon", "🌿")
            selectedColor = obj.optLong("color", 0xFF1F6B4CL)
            selectedTemplate = obj.optString("template", "Empty Project")
        }
    )

    UnsavedChangesGuard(
        isDirty = isDirty && savedEntityId == 0L,
        onDiscard = onBack
    )

    UndoSnackbar(
        hostState = LocalFieldMindSnackbar.current,
        entityName = "Project",
        entityId = savedEntityId,
        onUndo = { id -> viewModel.deleteProject(id); onBack() }
    )

    val projectIcons = listOf("🌿", "🦋", "🐦", "🌲", "📷")
    val colorOptions = listOf(
        0xFF1F6B4CL to FieldMindTheme.colors.observation,  // Observation green
        0xFF1565C0L to FieldMindTheme.colors.question,    // Question blue
        0xFF5E35B1L to FieldMindTheme.colors.source,      // Source violet
        0xFF8B5000L to FieldMindTheme.colors.hypothesis,  // Hypothesis amber
        0xFFE91E63L to FieldMindTheme.colors.flashcard    // Flashcard pink
    )
    val templates = listOf(
        "Empty Project",
        "Field Survey",
        "Species Observation",
        "Weather Log",
        "Site Monitoring",
        "Literature Review",
        "Experiment"
    )

    fun save() {
        if (title.isNotBlank()) {
            val colorHex = "#%06X".format(selectedColor and 0xFFFFFF)
            if (isEditing) {
                val latest = viewModel.projects.value.firstOrNull { it.id == entity!!.id } ?: entity!!
                viewModel.updateProjectEntity(latest.copy(
                    title = title,
                    topicType = selectedTemplate,
                    objective = description,
                    projectType = selectedTemplate,
                    selectedMethods = selectedIcon,
                    connectionMap = colorHex
                ))
            } else {
                viewModel.addProject(
                    title = title,
                    topicType = selectedTemplate,
                    objective = description,
                    researchQuestion = "",
                    methods = "",
                    futureQuestions = "",
                    backgroundNotes = "",
                    hypothesisSummary = "",
                    dataSummary = "",
                    analysis = "",
                    conclusion = "",
                    projectType = selectedTemplate,
                    selectedMethods = selectedIcon,
                    connectionMap = colorHex,
                    onSaved = { id -> savedEntityId = id; draftHelper.ClearDraft() }
                )
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding().background(MaterialTheme.colorScheme.background)) {
            // ── Custom header: back button + title/subtitle ──
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = CuteCardDefaults.Shape,
                color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.4f),
                tonalElevation = 0.dp
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        onClick = onBack,
                        shape = CuteCardDefaults.ButtonShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(FieldMindIcons.Back, null, tint = MaterialTheme.colorScheme.onSurface, size = 22.dp)
                        }
                    }
                    Column {
                        Text(if (isEditing) "Edit Project" else "New Project", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(if (isEditing) "Update your research workspace" else "Create a research workspace", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp).padding(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Basic Info Card ──
                Card(
                    shape = CuteCardDefaults.Shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Basic info", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        FieldTextField(
                            title, { title = it },
                            "Project Name",
                            supportingText = "Short, descriptive name for your research"
                        )
                        FieldTextField(
                            description, { description = it },
                            "Description (Optional)",
                            minLines = 3,
                            supportingText = "What is this project about?"
                        )
                    }
                }

                // ── Appearance Card ──
                Card(
                    shape = CuteCardDefaults.Shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Appearance", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                        // ── Project Icon ──
                        Text("Project Icon", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            projectIcons.forEach { icon ->
                                val isSelected = selectedIcon == icon
                                Surface(
                                    onClick = { haptics.light(); selectedIcon = icon },
                                    shape = CuteCardDefaults.ShapeCompact,
                                    color = if (isSelected) FieldMindTheme.colors.project.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, FieldMindTheme.colors.project) else null,
                                    modifier = Modifier.size(60.dp)
                                ) {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(icon, style = MaterialTheme.typography.headlineMedium)
                                    }
                                }
                            }
                        }

                        // ── Project Color ──
                        Text("Project Color", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            colorOptions.forEach { (colorLong, color) ->
                                val isSelected = selectedColor == colorLong
                                val borderMod = if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CuteCardDefaults.ShapeCompact) else Modifier
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CuteCardDefaults.ShapeCompact)
                                        .background(color)
                                        .then(borderMod)
                                        .clickable { haptics.light(); selectedColor = colorLong },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(MaterialSymbolIcon("check"), null, tint = Color.White, size = 24.dp)
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Template Card ──
                Card(
                    shape = CuteCardDefaults.Shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier),
                    onClick = { haptics.light(); showTemplatePicker = true }
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                Modifier.size(40.dp).clip(MaterialTheme.shapes.medium)
                                    .background(FieldMindTheme.colors.project.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(FieldMindIcons.Project, null, tint = FieldMindTheme.colors.project, size = 22.dp)
                            }
                            Column {
                                Text(selectedTemplate, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text("Pre-filled fields for $selectedTemplate", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Icon(MaterialSymbolIcon("chevron_right"), null, size = 20.dp, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    }
                }

                Spacer(Modifier.height(8.dp))

                // ── Create Button ──
                Button(
                    onClick = ::save,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = CuteCardDefaults.FieldShape,
                    enabled = title.isNotBlank()
                ) {
                    Icon(FieldMindIcons.Project, null, size = 20.dp)
                    Spacer(Modifier.size(8.dp))
                    Text("Create", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    // ── Template Picker Dialog ──
    if (showTemplatePicker) {
        SwipeableAlertDialog(
            onDismissRequest = { showTemplatePicker = false },
            icon = { Icon(FieldMindIcons.Project, null, size = 28.dp) },
            title = { Text("Choose a template") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Pre-filled templates help you get started faster.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    templates.forEach { template ->
                        val isSelected = selectedTemplate == template
                        Surface(
                            onClick = { haptics.light(); selectedTemplate = template; showTemplatePicker = false },
                            shape = CuteCardDefaults.ButtonShape,
                            color = if (isSelected) FieldMindTheme.colors.project.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(template, style = MaterialTheme.typography.bodyMedium, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
                                if (isSelected) {
                                    Icon(FieldMindIcons.Check, null, tint = FieldMindTheme.colors.project, size = 18.dp)
                                }
                            }
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showTemplatePicker = false }) { Text("Cancel") }
            }
        )
    }
}

// ══════════════════════════════════════════════════════════════════════
//  NEW QUESTION SCREEN — Full-screen creation form
// ══════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewQuestionScreen(viewModel: FieldMindViewModel, onBack: () -> Unit, entity: QuestionEntity? = null) {
    val isEditing = entity != null
    val haptics = rememberFieldMindHaptics()
    var question by rememberSaveable { mutableStateOf(entity?.questionText ?: "") }
    var category by rememberSaveable { mutableStateOf(entity?.category ?: "Other") }
    var source by rememberSaveable { mutableStateOf(entity?.sourceType ?: "Observation") }
    var status by rememberSaveable { mutableStateOf(entity?.status ?: "New") }
    var priority by rememberSaveable { mutableStateOf(entity?.priority ?: "Medium") }
    var answer by rememberSaveable { mutableStateOf(entity?.answer ?: "") }
    var showAdvanced by rememberSaveable { mutableStateOf(false) }
    val isDirty = question.isNotBlank() || answer.isNotBlank()
    var savedEntityId by rememberSaveable { mutableStateOf(0L) }

    UnsavedChangesGuard(
        isDirty = isDirty && savedEntityId == 0L,
        onDiscard = onBack
    )

    UndoSnackbar(
        hostState = LocalFieldMindSnackbar.current,
        entityName = "Question",
        entityId = savedEntityId,
        onUndo = { id -> viewModel.deleteQuestion(id); onBack() }
    )

    val colors = FieldMindTheme.colors
    val priorityColor = mapOf(
        "Low" to colors.positive,
        "Medium" to colors.warning,
        "High" to MaterialTheme.colorScheme.error
    )

    fun save() {
        if (question.isNotBlank()) {
            if (isEditing) {
                val latest = viewModel.questions.value.firstOrNull { it.id == entity!!.id } ?: entity!!
                viewModel.updateQuestionEntity(latest.copy(
                    questionText = question.trim(),
                    category = category,
                    sourceType = source,
                    status = status,
                    priority = priority,
                    answer = answer.trim()
                ))
                onBack()
            } else {
                viewModel.addQuestion(question, category, source, status, priority, answer = answer, onSaved = { id -> savedEntityId = id })
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding().background(MaterialTheme.colorScheme.background)) {
            StandardScreenHeader(
                title = if (isEditing) "Edit Question" else "New Question",
                subtitle = "Turn curiosity into something observable, measurable, comparable, or verifiable.",
                icon = FieldMindIcons.Question,
                heroColor = colors.question,
                trailing = { BackButton(onClick = onBack) },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp).padding(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Question Card ──
                Card(
                    shape = CuteCardDefaults.Shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Research question", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        FieldTextField(question, { question = it }, "Research Question", minLines = 3, supportingText = "Example: Do bird visits increase after rain at this site?")
                    }
                }

                // ── Classification Card ──
                Card(
                    shape = CuteCardDefaults.Shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        DividerSection("Classification", FieldMindIcons.Category, colors.question)
                        ChoiceChipsField("Category", observationCategories, category) { category = it }
                        ChoiceChipsField("Source", sourceTypes, source) { source = it }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                        Text("Priority", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            listOf("Low", "Medium", "High").forEach { level ->
                                val isSelected = priority == level
                                val accent = priorityColor[level] ?: FieldMindTheme.colors.positive
                                Surface(
                                    onClick = { haptics.light(); priority = level },
                                    shape = CuteCardDefaults.ButtonShape,
                                    color = if (isSelected) accent.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, accent) else null,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.size(18.dp)
                                                .clip(androidx.compose.foundation.shape.CircleShape)
                                                .background(if (isSelected) accent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Box(Modifier.size(8.dp).clip(androidx.compose.foundation.shape.CircleShape).background(accent))
                                            }
                                        }
                                        Text(level, style = MaterialTheme.typography.labelMedium, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal, color = if (isSelected) accent else MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                        DividerSection("Status", FieldMindIcons.Check, colors.question)
                        ChoiceChipsField("Status", questionStatuses, status) { status = it }
                    }
                }

                // ── Advanced Card ──
                Card(
                    shape = CuteCardDefaults.Shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        CollapsibleSection("Advanced options", "Answer, cross-links, and metadata", expanded = showAdvanced, onToggle = { showAdvanced = !showAdvanced }) {
                            FieldTextField(answer, { answer = it }, "Preliminary answer", minLines = 2, supportingText = "Optional — add if you already have a working answer")
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Button(onClick = ::save, modifier = Modifier.fillMaxWidth().height(50.dp), shape = CuteCardDefaults.ShapeCompact, enabled = question.isNotBlank()) {
                    Icon(FieldMindIcons.Question, null, size = 18.dp); Spacer(Modifier.size(8.dp)); Text("Create", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  NEW HYPOTHESIS SCREEN — Full-screen creation form
// ══════════════════════════════════════════════════════════════════════

@Composable
fun NewHypothesisScreen(viewModel: FieldMindViewModel, onBack: () -> Unit, entity: HypothesisEntity? = null) {
    val isEditing = entity != null
    val questions by viewModel.questions.collectAsState()
    var prediction by rememberSaveable { mutableStateOf(entity?.prediction ?: "") }
    var reasoning by rememberSaveable { mutableStateOf(entity?.reasoning ?: "") }
    var evidence by rememberSaveable { mutableStateOf(entity?.evidenceNeeded ?: "") }
    var support by rememberSaveable { mutableStateOf(entity?.supportCriteria ?: "") }
    var weaken by rememberSaveable { mutableStateOf(entity?.weakeningCriteria ?: "") }
    var test by rememberSaveable { mutableStateOf(entity?.testMethod ?: "") }
    var confidence by rememberSaveable { mutableStateOf((entity?.confidencePercent ?: 50).toFloat()) }
    var linkedId by rememberSaveable { mutableStateOf(entity?.linkedQuestionId) }
    var resultStatus by rememberSaveable { mutableStateOf(entity?.resultStatus ?: "Unknown") }
    var showAdvanced by rememberSaveable { mutableStateOf(false) }
    val isDirty = prediction.isNotBlank() || reasoning.isNotBlank() || evidence.isNotBlank()
    var savedEntityId by rememberSaveable { mutableStateOf(0L) }

    UnsavedChangesGuard(
        isDirty = isDirty && savedEntityId == 0L,
        onDiscard = onBack
    )

    UndoSnackbar(
        hostState = LocalFieldMindSnackbar.current,
        entityName = "Hypothesis",
        entityId = savedEntityId,
        onUndo = { id -> viewModel.deleteHypothesis(id); onBack() }
    )

    fun save() {
        if (prediction.isNotBlank()) {
            if (isEditing) {
                val latest = viewModel.hypotheses.value.firstOrNull { it.id == entity!!.id } ?: entity!!
                viewModel.updateHypothesisEntity(latest.copy(
                    prediction = prediction.trim(),
                    reasoning = reasoning.trim(),
                    evidenceNeeded = evidence.trim(),
                    supportCriteria = support.trim(),
                    weakeningCriteria = weaken.trim(),
                    testMethod = test.trim(),
                    confidencePercent = confidence.toInt(),
                    linkedQuestionId = linkedId,
                    resultStatus = resultStatus
                ))
                onBack()
            } else {
                viewModel.addHypothesis(linkedId, prediction, evidence, confidence.toInt(), reasoning, support, weaken, test, resultStatus = resultStatus, onSaved = { id -> savedEntityId = id })
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding().background(MaterialTheme.colorScheme.background)) {
            StandardScreenHeader(
                title = if (isEditing) "Edit Hypothesis" else "New Hypothesis",
                subtitle = "State the prediction, what would support it, and what would weaken it.",
                icon = FieldMindIcons.Hypothesis,
                heroColor = FieldMindTheme.colors.hypothesis,
                trailing = { BackButton(onClick = onBack) },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp).padding(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Prediction Card ──
                Card(
                    shape = CuteCardDefaults.Shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (questions.isNotEmpty()) {
                            ChoiceChipsField("Linked question", listOf("No question") + questions.take(8).map { it.questionText.take(28) }, questions.firstOrNull { it.id == linkedId }?.questionText?.take(28) ?: "No question") { picked ->
                                linkedId = questions.firstOrNull { it.questionText.startsWith(picked) }?.id
                            }
                        }
                        Text("Hypothesis", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        FieldTextField(prediction, { prediction = it }, "Prediction", minLines = 3)
                        FieldTextField(reasoning, { reasoning = it }, "Why this might happen", minLines = 2)
                    }
                }

                // ── Evidence Rules Card ──
                Card(
                    shape = CuteCardDefaults.Shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        DividerSection("Evidence rules", FieldMindIcons.Done, FieldMindTheme.colors.hypothesis)
                        Text("Decide success/failure before you bias yourself.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        FieldTextField(evidence, { evidence = it }, "Evidence needed", minLines = 2)
                        FieldTextField(support, { support = it }, "Support criteria")
                        FieldTextField(weaken, { weaken = it }, "Weakening criteria")
                        FieldTextField(test, { test = it }, "Test method")
                    }
                }

                // ── Confidence Card ──
                Card(
                    shape = CuteCardDefaults.Shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        DividerSection("Confidence", FieldMindIcons.Streak, FieldMindTheme.colors.hypothesis)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Confidence", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${confidence.toInt()}%", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(confidence, { confidence = it }, valueRange = 0f..100f)
                        LinearProgressIndicator(progress = { confidence / 100f }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(8.dp)), color = MaterialTheme.colorScheme.primary)
                    }
                }

                // ── Advanced Card ──
                Card(
                    shape = CuteCardDefaults.Shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        CollapsibleSection("Advanced options", "Result status tracking", expanded = showAdvanced, onToggle = { showAdvanced = !showAdvanced }) {
                            ChoiceChipsField("Result status", listOf("Unknown", "Supported", "Weakened", "Inconclusive"), resultStatus) { resultStatus = it }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Button(onClick = ::save, modifier = Modifier.fillMaxWidth(), shape = CuteCardDefaults.ShapeCompact, enabled = prediction.isNotBlank()) {
                    Icon(FieldMindIcons.Check, null, size = 18.dp); Spacer(Modifier.size(8.dp)); Text("Create hypothesis")
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  NEW DATA RECORD SCREEN — Full-screen creation form
// ══════════════════════════════════════════════════════════════════════

@Composable
fun NewDataRecordScreen(viewModel: FieldMindViewModel, onBack: () -> Unit, entity: DataRecordEntity? = null) {
    val isEditing = entity != null
    var tool by rememberSaveable { mutableStateOf(entity?.toolType ?: "Counter") }
    var label by rememberSaveable { mutableStateOf(entity?.label ?: "") }
    var value by rememberSaveable { mutableStateOf(entity?.value ?: "0") }
    var unit by rememberSaveable { mutableStateOf(entity?.unit ?: defaultUnitForTool(entity?.toolType ?: "Counter")) }
    var location by rememberSaveable { mutableStateOf(entity?.location ?: "") }
    var notes by rememberSaveable { mutableStateOf(entity?.notes ?: "") }
    val isDirty = label.isNotBlank() || notes.isNotBlank() || location.isNotBlank()
    var savedEntityId by rememberSaveable { mutableStateOf(0L) }

    UnsavedChangesGuard(
        isDirty = isDirty && savedEntityId == 0L,
        onDiscard = onBack
    )

    UndoSnackbar(
        hostState = LocalFieldMindSnackbar.current,
        entityName = "Data Record",
        entityId = savedEntityId,
        onUndo = { id -> viewModel.deleteDataRecord(id); onBack() }
    )

    fun save() {
        if (label.isNotBlank()) {
            if (isEditing) {
                val latest = viewModel.dataRecords.value.firstOrNull { it.id == entity!!.id } ?: entity!!
                viewModel.updateDataRecordEntity(latest.copy(
                    toolType = tool,
                    label = label.trim(),
                    value = value.trim(),
                    unit = unit.trim(),
                    notes = notes.trim(),
                    location = location.trim()
                ))
            } else {
                viewModel.addDataRecord(tool, label, value, unit, notes, location, onSaved = { id -> savedEntityId = id })
            }
            onBack()
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding().background(MaterialTheme.colorScheme.background)) {
            StandardScreenHeader(
                title = if (isEditing) "Edit Data Record" else "New Data Record",
                subtitle = "Choose a preset so units and labels match the kind of thing you measured.",
                icon = FieldMindIcons.Data,
                heroColor = FieldMindTheme.colors.data,
                trailing = { BackButton(onClick = onBack) },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp).padding(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Preset & Label Card ──
                Card(
                    shape = CuteCardDefaults.Shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        DividerSection("Preset", FieldMindIcons.Settings, FieldMindTheme.colors.data)
                        ChoiceChipsField("Tool", dataTools, tool) { tool = it; unit = defaultUnitForTool(it); label = defaultLabelForTool(it) }
                        FieldTextField(label, { label = it }, "Label")
                        if (tool == "Counter" || tool == "Species Tracker") {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                OutlinedButton({ value = ((value.toIntOrNull() ?: 0) - 1).toString() }) { Text("−1") }
                                Text(value, style = MaterialTheme.typography.headlineSmall)
                                Button({ value = ((value.toIntOrNull() ?: 0) + 1).toString() }) { Text("+1") }
                                TextButton({ value = "0" }) { Text("Reset") }
                            }
                        }
                    }
                }

                // ── Measurement Card ──
                Card(
                    shape = CuteCardDefaults.Shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        DividerSection("Measurement", FieldMindIcons.Line, FieldMindTheme.colors.data)
                        FieldTextField(value, { value = it }, "Value / items / samples", keyboardType = KeyboardType.Number)
                        FieldTextField(unit, { unit = it }, "Unit", supportingText = "Suggested for $tool: ${defaultUnitForTool(tool)}")
                        FieldTextField(location, { location = it }, "Location / site")
                    }
                }

                // ── Context Card ──
                Card(
                    shape = CuteCardDefaults.Shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        DividerSection("Context", FieldMindIcons.Note, FieldMindTheme.colors.data)
                        ChoiceChips(contextPresets, notes) { notes = if (notes.isBlank()) it else "$notes, $it" }
                        FieldTextField(notes, { notes = it }, "Notes", minLines = 3)
                    }
                }

                Spacer(Modifier.height(8.dp))
                Button(onClick = ::save, modifier = Modifier.fillMaxWidth(), shape = CuteCardDefaults.ShapeCompact, enabled = label.isNotBlank()) {
                    Icon(FieldMindIcons.Check, null, size = 18.dp); Spacer(Modifier.size(8.dp)); Text("Save record")
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  NEW TASK SCREEN — Full-screen creation form (mockup v2)
// ══════════════════════════════════════════════════════════════════════

@Composable
fun NewTaskScreen(viewModel: FieldMindViewModel, onBack: () -> Unit, entity: TaskEntity? = null) {
    val isEditing = entity != null
    // ── Form state ──
    var title by rememberSaveable { mutableStateOf(entity?.title ?: "") }
    var description by rememberSaveable { mutableStateOf(entity?.description ?: "") }
    var priority by rememberSaveable { mutableStateOf(entity?.priority ?: "Medium") }
    var projectId by rememberSaveable { mutableStateOf(entity?.projectId) }
    var dueDate by rememberSaveable { mutableStateOf(entity?.dueDate ?: "") }
    var dueTime by rememberSaveable { mutableStateOf(entity?.dueTime ?: "") }
    var reminder by rememberSaveable { mutableStateOf(entity?.reminder ?: 0) }
    var reminderUnit by rememberSaveable { mutableStateOf(entity?.reminderUnit ?: "minute") }
    var repeatInterval by rememberSaveable { mutableStateOf(entity?.repeatInterval ?: 0) }
    var repeatUnit by rememberSaveable { mutableStateOf(entity?.repeatUnit ?: "") }
    var checklistItems by rememberSaveable { mutableStateOf(listOf("")) }
    var attachmentUris by rememberSaveable { mutableStateOf(entity?.attachmentUris?.split(",")?.filter { it.isNotBlank() } ?: emptyList()) }
    val isDirty = title.isNotBlank() || description.isNotBlank() || checklistItems.any { it.isNotBlank() }
    var savedEntityId by rememberSaveable { mutableStateOf(0L) }

    UnsavedChangesGuard(
        isDirty = isDirty && savedEntityId == 0L,
        onDiscard = onBack
    )

    UndoSnackbar(
        hostState = LocalFieldMindSnackbar.current,
        entityName = "Task",
        entityId = savedEntityId,
        onUndo = { id -> viewModel.deleteTask(id); onBack() }
    )

    val projects by viewModel.projects.collectAsState()
    val haptics = rememberFieldMindHaptics()
    val context = LocalContext.current
    var showAttachmentMenu by rememberSaveable { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            runCatching { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            attachmentUris = attachmentUris + it.toString()
        }
    }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            runCatching { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            attachmentUris = attachmentUris + it.toString()
        }
    }

    // ── Priority colors ──
    val priorityColor = mapOf(
        "Low" to FieldMindTheme.colors.positive,
        "Medium" to FieldMindTheme.colors.warning,
        "High" to MaterialTheme.colorScheme.error
    )

    fun save() {
        if (title.isNotBlank()) {
            val checklistArr = org.json.JSONArray()
            checklistItems.filter { it.isNotBlank() }.forEach { item ->
                checklistArr.put(org.json.JSONObject().apply {
                    put("text", item.trim())
                    put("done", false)
                })
            }
            if (isEditing) {
                val latest = viewModel.tasks.value.firstOrNull { it.id == entity!!.id } ?: entity!!
                viewModel.updateTaskEntity(latest.copy(
                    title = title,
                    description = description,
                    priority = priority,
                    dueDate = dueDate,
                    dueTime = dueTime,
                    projectId = projectId,
                    checklistJson = checklistArr.toString(),
                    attachmentUris = attachmentUris.joinToString(","),
                    reminder = reminder,
                    reminderUnit = reminderUnit,
                    repeatInterval = repeatInterval,
                    repeatUnit = repeatUnit
                ))
            } else {
                viewModel.addTask(
                    title = title,
                    description = description,
                    priority = priority,
                    dueDate = dueDate,
                    dueTime = dueTime,
                    projectId = projectId,
                    checklistJson = checklistArr.toString(),
                    attachmentUris = attachmentUris.joinToString(","),
                    reminder = reminder,
                    reminderUnit = "minute",
                    repeatInterval = repeatInterval,
                    repeatUnit = repeatUnit,
                    onSaved = { id -> savedEntityId = id }
                )
            }
            onBack()
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding().background(MaterialTheme.colorScheme.background)) {
            StandardScreenHeader(
                title = if (isEditing) "Edit Task" else "New Task",
                subtitle = "Define a field task, survey, or to-do.",
                icon = MaterialSymbolIcon("checklist"),
                heroColor = FieldMindTheme.colors.flashcard,
                trailing = { BackButton(onClick = onBack) }
            )
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp).padding(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Task Info Card ──
                Card(
                    shape = CuteCardDefaults.Shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Task details", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        FieldTextField(title, { title = it }, "Task Name", supportingText = "Short, actionable title")
                        FieldTextField(description, { description = it }, "Description", minLines = 3, supportingText = "Details, context, or step-by-step instructions")
                    }
                }

                // ── Priority & Project Card ──
                Card(
                    shape = CuteCardDefaults.Shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Priority & project", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Priority", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                listOf("Low", "Medium", "High").forEach { level ->
                                    val isSelected = priority == level
                                    val accent = priorityColor[level] ?: FieldMindTheme.colors.positive
                                    Surface(
                                        onClick = { haptics.light(); priority = level },
                                        shape = CuteCardDefaults.ButtonShape,
                                        color = if (isSelected) accent.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, accent) else null,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier.size(18.dp)
                                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                                    .background(if (isSelected) accent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isSelected) {
                                                    Box(Modifier.size(8.dp).clip(androidx.compose.foundation.shape.CircleShape).background(accent))
                                                }
                                            }
                                            Text(level, style = MaterialTheme.typography.labelMedium, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal, color = if (isSelected) accent else MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Project", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (projects.isNotEmpty()) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    ChoiceChips(listOf("None") + projects.take(6).map { it.title.take(20) }, projects.firstOrNull { it.id == projectId }?.title?.take(20) ?: "None") { selected ->
                                        projectId = projects.firstOrNull { it.title.startsWith(selected) }?.id
                                    }
                                }
                            } else {
                                Text("No projects yet. Create a project first.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                            }
                        }
                    }
                }

                // ── Schedule Card ──
                Card(
                    shape = CuteCardDefaults.Shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Schedule", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FieldTextField(dueDate, { dueDate = it }, "Due Date", supportingText = "YYYY-MM-DD", modifier = Modifier.weight(1f))
                            FieldTextField(dueTime, { dueTime = it }, "Due Time", supportingText = "HH:MM", modifier = Modifier.weight(1f))
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Reminder", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                val reminderOptions = listOf("None" to 0, "5 min" to 5, "15 min" to 15, "30 min" to 30, "1 hour" to 60, "1 day" to 1440)
                                reminderOptions.forEach { (label, mins) ->
                                    val isSelected = reminder == mins
                                    Surface(
                                        onClick = { haptics.light(); reminder = mins },
                                        shape = RoundedCornerShape(18.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            label,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Repeat", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                val repeatOptions = listOf("None" to "" to 0, "Daily" to "day" to 1, "Weekly" to "week" to 1, "Monthly" to "month" to 1, "Yearly" to "year" to 1)
                                repeatOptions.forEach { (pair, interval) ->
                                    val (label, unit) = pair
                                    val isSelected = repeatUnit == unit
                                    Surface(
                                        onClick = { haptics.light(); repeatUnit = unit; repeatInterval = interval },
                                        shape = RoundedCornerShape(18.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            label,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Checklist Card ──
                Card(
                    shape = CuteCardDefaults.Shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Checklist", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            TextButton(onClick = { haptics.light(); checklistItems = checklistItems + "" }) {
                                Icon(MaterialSymbolIcon("add"), null, size = 16.dp)
                                Spacer(Modifier.size(4.dp))
                                Text("Add item", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        checklistItems.forEachIndexed { index, item ->
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = item,
                                    onValueChange = { newVal ->
                                        checklistItems = checklistItems.toMutableList().also { it[index] = newVal }
                                    },
                                    placeholder = { Text("Checklist item", style = MaterialTheme.typography.bodySmall) },
                                    shape = MaterialTheme.shapes.medium,
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodySmall
                                )
                                if (checklistItems.size > 1) {
                                    IconButton(onClick = { haptics.light(); checklistItems = checklistItems.toMutableList().also { it.removeAt(index) } }, modifier = Modifier.size(32.dp)) {
                                        Icon(MaterialSymbolIcon("close"), "Remove", size = 16.dp, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Attachments Card ──
                Card(
                    shape = CuteCardDefaults.Shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Attachments", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Box {
                                TextButton(onClick = { haptics.light(); showAttachmentMenu = true }) {
                                    Icon(MaterialSymbolIcon("attach_file"), null, size = 16.dp)
                                    Spacer(Modifier.size(4.dp))
                                    Text("Add file", style = MaterialTheme.typography.labelSmall)
                                }
                                DropdownMenu(
                                    expanded = showAttachmentMenu,
                                    onDismissRequest = { showAttachmentMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Photo from gallery") },
                                        onClick = {
                                            showAttachmentMenu = false
                                            haptics.light()
                                            imagePicker.launch("image/*")
                                        },
                                        leadingIcon = { Icon(MaterialSymbolIcon("photo_library"), null, size = 18.dp) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Document / PDF") },
                                        onClick = {
                                            showAttachmentMenu = false
                                            haptics.light()
                                            filePicker.launch(arrayOf(
                                                "application/pdf",
                                                "text/*",
                                                "application/msword",
                                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                                "audio/*",
                                                "image/*"
                                            ))
                                        },
                                        leadingIcon = { Icon(MaterialSymbolIcon("description"), null, size = 18.dp) }
                                    )
                                }
                            }
                        }
                        if (attachmentUris.isEmpty()) {
                            Text(
                                "No attachments yet. Tap \"Add file\" to attach images, PDFs, or audio.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        } else {
                            attachmentUris.forEach { uri ->
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(MaterialSymbolIcon("attachment"), null, size = 16.dp, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(uri.substringAfterLast("/").take(30), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                    IconButton(onClick = { haptics.light(); attachmentUris = attachmentUris - uri }, modifier = Modifier.size(24.dp)) {
                                        Icon(MaterialSymbolIcon("close"), "Remove", size = 14.dp, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = ::save,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = CuteCardDefaults.ShapeCompact,
                    enabled = title.isNotBlank()
                ) {
                    Icon(FieldMindIcons.Check, null, size = 20.dp)
                    Spacer(Modifier.size(8.dp))
                    Text("Save", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  NEW REPORT SCREEN — Full-screen creation form
// ══════════════════════════════════════════════════════════════════════

@Composable
fun NewReportScreen(viewModel: FieldMindViewModel, onBack: () -> Unit, entity: ReportEntity? = null) {
    val isEditing = entity != null
    var type by rememberSaveable { mutableStateOf(entity?.type ?: "Field Report") }
    var title by rememberSaveable { mutableStateOf(entity?.title ?: "") }
    var background by rememberSaveable { mutableStateOf(entity?.background ?: "") }
    var question by rememberSaveable { mutableStateOf(entity?.question ?: "") }
    var methods by rememberSaveable { mutableStateOf(entity?.methods ?: "") }
    var observations by rememberSaveable { mutableStateOf(entity?.observations ?: "") }
    var results by rememberSaveable { mutableStateOf(entity?.results ?: "") }
    var interpretation by rememberSaveable { mutableStateOf(entity?.interpretation ?: "") }
    var conclusion by rememberSaveable { mutableStateOf(entity?.conclusion ?: "") }
    var limitations by rememberSaveable { mutableStateOf(entity?.limitations ?: "") }
    var next by rememberSaveable { mutableStateOf(entity?.nextSteps ?: "") }
    val isDirty = title.isNotBlank() || background.isNotBlank() || question.isNotBlank() || observations.isNotBlank() || results.isNotBlank() || conclusion.isNotBlank()
    var savedEntityId by rememberSaveable { mutableStateOf(0L) }

    UnsavedChangesGuard(
        isDirty = isDirty && savedEntityId == 0L,
        onDiscard = onBack
    )

    UndoSnackbar(
        hostState = LocalFieldMindSnackbar.current,
        entityName = "Report",
        entityId = savedEntityId,
        onUndo = { id -> viewModel.deleteReport(id); onBack() }
    )

    fun save() {
        if (title.isNotBlank()) {
            if (isEditing) {
                val latest = viewModel.reports.value.firstOrNull { it.id == entity!!.id } ?: entity!!
                viewModel.updateReportEntity(latest.copy(
                    type = type,
                    title = title.trim(),
                    background = background.trim(),
                    question = question.trim(),
                    methods = methods.trim(),
                    observations = observations.trim(),
                    results = results.trim(),
                    interpretation = interpretation.trim(),
                    conclusion = conclusion.trim(),
                    limitations = limitations.trim(),
                    nextSteps = next.trim()
                ))
            } else {
                viewModel.addReport(type, title, background, question, methods, observations, results, interpretation, conclusion, limitations, next, onSaved = { id -> savedEntityId = id })
            }
            onBack()
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding().background(MaterialTheme.colorScheme.background)) {
            StandardScreenHeader(
                title = if (isEditing) "Edit Report" else "Report Builder",
                subtitle = "Create a clean local draft: claim, evidence, reasoning, limitations, and next steps.",
                icon = FieldMindIcons.Report,
                heroColor = FieldMindTheme.colors.report,
                trailing = { BackButton(onClick = onBack) },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp).padding(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Type & Title Card ──
                Card(
                    shape = CuteCardDefaults.Shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        DividerSection("Type & title", FieldMindIcons.Category, FieldMindTheme.colors.report)
                        ChoiceChipsField("Report type", reportTypes, type) { type = it }
                        FieldTextField(title, { title = it }, "Title")
                    }
                }

                // ── Setup Card ──
                Card(
                    shape = CuteCardDefaults.Shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        DividerSection("Setup", FieldMindIcons.School, FieldMindTheme.colors.report)
                        FieldTextField(background, { background = it }, "Background", minLines = 2)
                        FieldTextField(question, { question = it }, "Question", minLines = 2)
                        FieldTextField(methods, { methods = it }, "Methods", minLines = 2)
                    }
                }

                // ── Evidence Card ──
                Card(
                    shape = CuteCardDefaults.Shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        DividerSection("Evidence", FieldMindIcons.Data, FieldMindTheme.colors.report)
                        FieldTextField(observations, { observations = it }, "Observations", minLines = 2)
                        FieldTextField(results, { results = it }, "Data / results", minLines = 2)
                        FieldTextField(interpretation, { interpretation = it }, "Interpretation", minLines = 2)
                    }
                }

                // ── Conclusion Card ──
                Card(
                    shape = CuteCardDefaults.Shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        DividerSection("Conclusion", FieldMindIcons.Check, FieldMindTheme.colors.report)
                        FieldTextField(conclusion, { conclusion = it }, "Conclusion", minLines = 2)
                        FieldTextField(limitations, { limitations = it }, "Limitations", minLines = 2)
                        FieldTextField(next, { next = it }, "Next steps", minLines = 2)
                    }
                }

                Spacer(Modifier.height(8.dp))
                Button(onClick = ::save, modifier = Modifier.fillMaxWidth(), shape = CuteCardDefaults.ShapeCompact, enabled = title.isNotBlank()) {
                    Icon(FieldMindIcons.Check, null, size = 18.dp); Spacer(Modifier.size(8.dp)); Text("Build report")
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  NEW OBSERVATION SCREEN — Full-screen creation form for observations
// ══════════════════════════════════════════════════════════════════════

@Composable
fun NewObservationScreen(viewModel: FieldMindViewModel, onBack: () -> Unit, entity: ObservationEntity? = null) {
    val isEditing = entity != null
    val context = LocalContext.current
    val haptics = rememberFieldMindHaptics()
    var subject by rememberSaveable { mutableStateOf(entity?.subject ?: "") }; var category by rememberSaveable { mutableStateOf(entity?.category ?: "Other") }
    var facts by rememberSaveable { mutableStateOf(entity?.factsOnlyNotes ?: "") }; var confidence by rememberSaveable { mutableStateOf(entity?.confidenceLevel ?: "Likely") }
    var location by rememberSaveable { mutableStateOf(entity?.manualLocation ?: "") }; var latitude by rememberSaveable { mutableStateOf(entity?.latitude?.toString() ?: "") }; var longitude by rememberSaveable { mutableStateOf(entity?.longitude?.toString() ?: "") }
    var tags by rememberSaveable { mutableStateOf(entity?.tags ?: "") }; var evidence by rememberSaveable { mutableStateOf(entity?.evidenceSummary ?: "") }; var fieldContext by rememberSaveable { mutableStateOf(entity?.moodOrContext ?: "") }
    var showAdvanced by rememberSaveable { mutableStateOf(false) }
    val isDirty = subject.isNotBlank() || facts.isNotBlank() || tags.isNotBlank() || evidence.isNotBlank() || fieldContext.isNotBlank() || location.isNotBlank()
    var savedEntityId by rememberSaveable { mutableStateOf(0L) }

    UnsavedChangesGuard(
        isDirty = isDirty && savedEntityId == 0L,
        onDiscard = onBack
    )

    UndoSnackbar(
        hostState = LocalFieldMindSnackbar.current,
        entityName = "Observation",
        entityId = savedEntityId,
        onUndo = { id -> viewModel.deleteObservation(id); onBack() }
    )

    fun save() {
        if (subject.isNotBlank() || facts.isNotBlank()) {
            val effectiveSubject = subject.ifBlank { facts.take(48).ifBlank { "$category observation" } }
            if (isEditing) {
                val latest = viewModel.observations.value.firstOrNull { it.id == entity!!.id } ?: entity!!
                viewModel.updateObservation(latest.copy(
                    subject = effectiveSubject,
                    category = category,
                    factsOnlyNotes = facts.ifBlank { "Quick $category observation." },
                    confidenceLevel = confidence,
                    manualLocation = location.ifBlank { "" },
                    latitude = latitude.toDoubleOrNull() ?: latest.latitude,
                    longitude = longitude.toDoubleOrNull() ?: latest.longitude,
                    tags = if (tags.isNotBlank()) "$tags, $category" else category,
                    evidenceSummary = evidence,
                    moodOrContext = fieldContext
                ))
                onBack()
            } else {
                viewModel.addObservation(
                    subject = effectiveSubject,
                    category = category,
                    facts = facts.ifBlank { "Quick $category observation." },
                    confidence = confidence,
                    manualLocation = location.ifBlank { "" },
                    latitude = latitude.toDoubleOrNull(),
                    longitude = longitude.toDoubleOrNull(),
                    tags = if (tags.isNotBlank()) "$tags, $category" else category,
                    evidence = evidence,
                    context = fieldContext,
                    onSaved = { id -> savedEntityId = id }
                )
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding().background(MaterialTheme.colorScheme.background)) {
            StandardScreenHeader(
                title = if (isEditing) "Edit Observation" else "New Observation",
                subtitle = "Record what you observed — species, conditions, evidence.",
                icon = FieldMindIcons.Observation,
                heroColor = FieldMindTheme.colors.observation,
                trailing = { BackButton(onClick = onBack) }
            )
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp).padding(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Subject Card ──
                Card(
                    shape = CuteCardDefaults.Shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Observation", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        FieldTextField(subject, { subject = it }, "Species / Subject", supportingText = "Monarch Butterfly, Red-tailed Hawk…")
                        FieldTextField(facts, { facts = it }, "Description", minLines = 3, supportingText = "What exactly did you see, hear, or measure?")
                    }
                }

                // ── Classification Card ──
                Card(
                    shape = CuteCardDefaults.Shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        DividerSection("Classification", FieldMindIcons.Category, FieldMindTheme.colors.observation)
                        ChoiceChipsField("Category", observationCategories, category) { category = it }
                        ChoiceChipsField("Confidence", confidenceOptions, confidence) { confidence = it }
                    }
                }

                // ── Location Card ──
                Card(
                    shape = CuteCardDefaults.Shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        DividerSection("Location", FieldMindIcons.Location, FieldMindTheme.colors.observation)
                        FieldTextField(location, { location = it }, "Location", supportingText = "e.g. Trailhead, Zone A, GPS coordinate")
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FieldTextField(latitude, { latitude = it }, "Latitude", modifier = Modifier.weight(1f), keyboardType = KeyboardType.Decimal)
                            FieldTextField(longitude, { longitude = it }, "Longitude", modifier = Modifier.weight(1f), keyboardType = KeyboardType.Decimal)
                        }
                    }
                }

                // ── Tags Card ──
                Card(
                    shape = CuteCardDefaults.Shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        DividerSection("Tags", FieldMindIcons.Tag, FieldMindTheme.colors.observation)
                        FieldTextField(tags, { tags = it }, "Tags", supportingText = "Comma-separated keywords — e.g. Butterfly, Pollinator")
                        CollapsibleSection("Advanced", "Evidence summary & field context", expanded = showAdvanced, onToggle = { showAdvanced = !showAdvanced }) {
                            FieldTextField(evidence, { evidence = it }, "Evidence summary", minLines = 2)
                            FieldTextField(fieldContext, { fieldContext = it }, "Field context", minLines = 2)
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Button(onClick = ::save, modifier = Modifier.fillMaxWidth(), shape = CuteCardDefaults.ShapeCompact, enabled = subject.isNotBlank() || facts.isNotBlank()) {
                    Icon(FieldMindIcons.Check, null, size = 18.dp); Spacer(Modifier.size(8.dp)); Text("Save observation")
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  NEW NOTE SCREEN — Full-screen creation form for notes
// ══════════════════════════════════════════════════════════════════════

@Composable
fun NewNoteScreen(viewModel: FieldMindViewModel, onBack: () -> Unit, entity: NoteEntity? = null) {
    val isEditing = entity != null
    val colors = FieldMindTheme.colors
    var title by rememberSaveable { mutableStateOf(entity?.title ?: "") }; var body by rememberSaveable { mutableStateOf(entity?.body ?: "") }
    var category by rememberSaveable { mutableStateOf(entity?.category ?: "Other") }; var tags by rememberSaveable { mutableStateOf(entity?.tags ?: "") }
    var location by rememberSaveable { mutableStateOf("") }; var showAdvanced by rememberSaveable { mutableStateOf(false) }
    val isDirty = title.isNotBlank() || body.isNotBlank() || tags.isNotBlank() || location.isNotBlank()
    var savedEntityId by rememberSaveable { mutableStateOf(0L) }

    UnsavedChangesGuard(
        isDirty = isDirty && savedEntityId == 0L,
        onDiscard = onBack
    )

    UndoSnackbar(
        hostState = LocalFieldMindSnackbar.current,
        entityName = "Note",
        entityId = savedEntityId,
        onUndo = { id -> viewModel.deleteNote(id); onBack() }
    )

    fun save() {
        if (title.isNotBlank() || body.isNotBlank()) {
            val fallbackTitle = body.lineSequence().firstOrNull { it.isNotBlank() }?.take(48) ?: "Untitled note"
            if (isEditing) {
                val latest = viewModel.notes.value.firstOrNull { it.id == entity!!.id } ?: entity!!
                viewModel.updateNoteEntity(latest.copy(
                    title = title.ifBlank { fallbackTitle },
                    body = body,
                    category = category,
                    tags = tags
                ))
                onBack()
            } else {
                viewModel.addNote(
                    title = title.ifBlank { fallbackTitle },
                    body = body,
                    category = category,
                    tags = tags,
                    onSaved = { id -> savedEntityId = id }
                )
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding().background(MaterialTheme.colorScheme.background)) {
            StandardScreenHeader(
                title = if (isEditing) "Edit Note" else "New Note",
                subtitle = "Capture a quick idea, observation, or thought.",
                icon = FieldMindIcons.Note,
                heroColor = colors.source,
                trailing = { BackButton(onClick = onBack) },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp).padding(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Content Card ──
                Card(
                    shape = CuteCardDefaults.Shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Note content", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        FieldTextField(title, { title = it }, "Title", supportingText = "Auto-filled from body if left blank")
                        FieldTextField(body, { body = it }, "Content", minLines = 6, supportingText = "Start writing…")
                    }
                }

                // ── Classification Card ──
                Card(
                    shape = CuteCardDefaults.Shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        DividerSection("Classification", FieldMindIcons.Category, colors.source)
                        ChoiceChipsField("Category", observationCategories, category) { category = it }
                        DividerSection("Tags", FieldMindIcons.Tag, colors.source)
                        FieldTextField(tags, { tags = it }, "Tags", supportingText = "Comma-separated keywords")
                        CollapsibleSection("Advanced", "Location & metadata", expanded = showAdvanced, onToggle = { showAdvanced = !showAdvanced }) {
                            FieldTextField(location, { location = it }, "Location", supportingText = "Where was this note taken?")
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Button(onClick = ::save, modifier = Modifier.fillMaxWidth().height(50.dp), shape = CuteCardDefaults.ShapeCompact, enabled = title.isNotBlank() || body.isNotBlank()) {
                    Icon(FieldMindIcons.Note, null, size = 18.dp); Spacer(Modifier.size(8.dp)); Text("Save", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  NEW SOURCE SCREEN — Full-screen creation form for sources
// ══════════════════════════════════════════════════════════════════════

@Composable
fun NewSourceScreen(viewModel: FieldMindViewModel, onBack: () -> Unit, entity: SourceEntity? = null) {
    val isEditing = entity != null
    val colors = FieldMindTheme.colors
    val projects by viewModel.projects.collectAsState()
    var type by rememberSaveable { mutableStateOf(entity?.type ?: "Article") }
    var title by rememberSaveable { mutableStateOf(entity?.title ?: "") }; var author by rememberSaveable { mutableStateOf(entity?.author ?: "") }
    var dateOrYear by rememberSaveable { mutableStateOf(entity?.dateOrYear ?: "") }; var doiOrIsbn by rememberSaveable { mutableStateOf(entity?.doiOrIsbn ?: "") }
    var publisherOrJournal by rememberSaveable { mutableStateOf(entity?.publisherOrJournal ?: "") }; var accessDate by rememberSaveable { mutableStateOf(entity?.accessDate ?: today()) }
    var link by rememberSaveable { mutableStateOf(entity?.link ?: "") }; var fileUri by rememberSaveable { mutableStateOf(entity?.fileUri ?: "") }
    var citationStyleNote by rememberSaveable { mutableStateOf(entity?.citationStyleNote ?: "") }
    var importance by rememberSaveable { mutableStateOf(entity?.importance ?: "Normal") }; var readingStatus by rememberSaveable { mutableStateOf(entity?.readingStatus ?: "In progress") }
    var summary by rememberSaveable { mutableStateOf(entity?.personalSummary ?: "") }; var taught by rememberSaveable { mutableStateOf(entity?.whatThisSourceTaughtMe ?: "") }
    var findings by rememberSaveable { mutableStateOf(entity?.keyFindings ?: "") }; var questions by rememberSaveable { mutableStateOf(entity?.questionsGenerated ?: "") }
    var notes by rememberSaveable { mutableStateOf(entity?.paperNotes ?: "") }; var reliability by rememberSaveable { mutableStateOf((entity?.reliabilityScore ?: 3).toFloat()) }
    var projectId by rememberSaveable { mutableStateOf(entity?.relatedProjectId) }
    val isDirty = title.isNotBlank() || author.isNotBlank() || summary.isNotBlank() || findings.isNotBlank() || taught.isNotBlank() || notes.isNotBlank()
    var savedEntityId by rememberSaveable { mutableStateOf(0L) }

    UnsavedChangesGuard(
        isDirty = isDirty && savedEntityId == 0L,
        onDiscard = onBack
    )

    UndoSnackbar(
        hostState = LocalFieldMindSnackbar.current,
        entityName = "Source",
        entityId = savedEntityId,
        onUndo = { id -> viewModel.deleteSource(id); onBack() }
    )

    fun save() {
        if (title.isNotBlank()) {
            if (isEditing) {
                val latest = viewModel.sources.value.firstOrNull { it.id == entity!!.id } ?: entity!!
                viewModel.updateSourceEntity(latest.copy(
                    type = type, title = title.trim(), author = author.trim(),
                    dateOrYear = dateOrYear.trim(), doiOrIsbn = doiOrIsbn.trim(),
                    publisherOrJournal = publisherOrJournal.trim(), accessDate = accessDate.trim(),
                    link = link.trim(), fileUri = fileUri.trim(), citationStyleNote = citationStyleNote.trim(),
                    importance = importance, readingStatus = readingStatus,
                    personalSummary = summary.trim(), whatThisSourceTaughtMe = taught.trim(),
                    keyFindings = findings.trim(), questionsGenerated = questions.trim(),
                    paperNotes = notes.trim(), reliabilityScore = reliability.toInt(),
                    relatedProjectId = projectId
                ))
            } else {
                viewModel.addSource(
                    type = type, title = title.trim(), author = author.trim(),
                    link = link.trim(), summary = summary.trim(), taught = taught.trim(),
                    reliability = reliability.toInt(), keyFindings = findings.trim(),
                    questionsGenerated = questions.trim(), paperNotes = notes.trim(),
                    projectId = projectId, dateOrYear = dateOrYear.trim(), doiOrIsbn = doiOrIsbn.trim(),
                    publisherOrJournal = publisherOrJournal.trim(), accessDate = accessDate.trim(),
                    fileUri = fileUri.trim(), citationStyleNote = citationStyleNote.trim(),
                    importance = importance, readingStatus = readingStatus,
                    onSaved = { id -> savedEntityId = id }
                )
            }
            onBack()
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding().background(MaterialTheme.colorScheme.background)) {
            StandardScreenHeader(
                title = if (isEditing) "Edit Source" else "New Source",
                subtitle = "Start with title + type. Fill in what you have.",
                icon = FieldMindIcons.Source,
                heroColor = colors.source,
                trailing = { BackButton(onClick = onBack) },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp).padding(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Source Type Card ──
                Card(
                    shape = CuteCardDefaults.Shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Source type", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        ChoiceChipsField("Source type", sourceLibraryTypes, type) { type = it }
                    }
                }

                // ── Identity Card ──
                Card(
                    shape = CuteCardDefaults.Shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        DividerSection("Identity", FieldMindIcons.Article, FieldMindTheme.colors.source)
                        FieldTextField(title, { title = it }, "Title")
                        FieldTextField(author, { author = it }, "Author / creator")
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FieldTextField(dateOrYear, { dateOrYear = it }, "Date / year", modifier = Modifier.weight(1f))
                            FieldTextField(accessDate, { accessDate = it }, "Accessed", modifier = Modifier.weight(1f))
                        }
                        FieldTextField(doiOrIsbn, { doiOrIsbn = it }, "DOI / ISBN")
                        FieldTextField(publisherOrJournal, { publisherOrJournal = it }, "Publisher / journal")
                    }
                }

                // ── Notes Card ──
                Card(
                    shape = CuteCardDefaults.Shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        DividerSection("Link & notes", FieldMindIcons.Link, colors.source)
                        FieldTextField(link, { link = it }, "Web link")
                        FieldTextField(summary, { summary = it }, "Main idea", minLines = 2)
                        FieldTextField(findings, { findings = it }, "Key findings", minLines = 2)
                        FieldTextField(taught, { taught = it }, "What this taught me", minLines = 2)
                        FieldTextField(questions, { questions = it }, "New questions", minLines = 2)
                        FieldTextField(notes, { notes = it }, "Paper / Cornell notes", minLines = 3)
                    }
                }

                // ── Status Card ──
                Card(
                    shape = CuteCardDefaults.Shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        DividerSection("Status", FieldMindIcons.Check, colors.source)
                        ChoiceChipsField("Reading status", readingStatuses, readingStatus) { readingStatus = it }
                        ChoiceChipsField("Importance", sourceImportanceLevels, importance) { importance = it }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Credibility", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${reliability.toInt()}/5", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(reliability, { reliability = it }, valueRange = 1f..5f, steps = 3)
                        if (projects.isNotEmpty()) {
                            ChoiceChipsField("Link to project", listOf("No project") + projects.map { it.title }, projects.firstOrNull { it.id == projectId }?.title ?: "No project") { selected ->
                                projectId = projects.firstOrNull { it.title == selected }?.id
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Button(onClick = ::save, modifier = Modifier.fillMaxWidth().height(50.dp), shape = CuteCardDefaults.ShapeCompact, enabled = title.isNotBlank()) {
                    Icon(FieldMindIcons.Source, null, size = 18.dp); Spacer(Modifier.size(8.dp)); Text("Save", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  NEW ATTACHMENT SCREEN — File type picker grid (standalone)
// ══════════════════════════════════════════════════════════════════════

@Composable
fun NewAttachmentScreen(viewModel: FieldMindViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val haptics = rememberFieldMindHaptics()
    var capturedUri by rememberSaveable { mutableStateOf<String?>(null) }
    var capturedType by rememberSaveable { mutableStateOf("") }
    var showExitConfirmation by rememberSaveable { mutableStateOf(false) }
    val isDirty = capturedUri != null

    BackHandler(enabled = isDirty) { showExitConfirmation = true }

    if (showExitConfirmation) {
        SwipeableAlertDialog(
            onDismissRequest = { showExitConfirmation = false },
            title = { Text("Discard changes?") },
            text = { Text("You have unsaved changes. Are you sure you want to go back?") },
            confirmButton = {
                Button(
                    onClick = { showExitConfirmation = false; onBack() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Discard") }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmation = false }) { Text("Keep editing") }
            }
        )
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { runCatching { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) }; capturedUri = it.toString(); capturedType = "Image" }
    }
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { runCatching { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) }; capturedUri = it.toString(); capturedType = "Video" }
    }
    val audioPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { runCatching { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) }; capturedUri = it.toString(); capturedType = "Audio" }
    }
    val pdfPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let { runCatching { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) }; capturedUri = it.toString(); capturedType = "PDF" }
    }
    val sheetPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let { runCatching { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) }; capturedUri = it.toString(); capturedType = "Sheet" }
    }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let { runCatching { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) }; capturedUri = it.toString(); capturedType = "File" }
    }

    Column(Modifier.fillMaxSize().statusBarsPadding().background(MaterialTheme.colorScheme.background)) {
        StandardScreenHeader(
            title = "Add Attachment",
            subtitle = "Attach an image, video, audio, PDF, sheet, or other file.",
            icon = MaterialSymbolIcon("attach_file"),
            heroColor = FieldMindTheme.colors.warning,
            trailing = { BackButton(onClick = onBack) }
        )
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp).padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Choose attachment type", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AttachmentTypeItem("Image", MaterialSymbolIcon("photo"), FieldMindTheme.colors.observation, Modifier.weight(1f)) { haptics.light(); imagePicker.launch("image/*") }
                    AttachmentTypeItem("Video", MaterialSymbolIcon("videocam"), FieldMindTheme.colors.question, Modifier.weight(1f)) { haptics.light(); videoPicker.launch("video/*") }
                    AttachmentTypeItem("Audio", MaterialSymbolIcon("mic"), FieldMindTheme.colors.project, Modifier.weight(1f)) { haptics.light(); audioPicker.launch("audio/*") }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AttachmentTypeItem("PDF", MaterialSymbolIcon("picture_as_pdf"), MaterialTheme.colorScheme.error, Modifier.weight(1f)) { haptics.light(); pdfPicker.launch(arrayOf("application/pdf")) }
                    AttachmentTypeItem("Sheet", MaterialSymbolIcon("table_chart"), FieldMindTheme.colors.data, Modifier.weight(1f)) { haptics.light(); sheetPicker.launch(arrayOf("text/csv", "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) }
                    AttachmentTypeItem("File", MaterialSymbolIcon("description"), FieldMindTheme.colors.hypothesis, Modifier.weight(1f)) { haptics.light(); filePicker.launch(arrayOf("*/*")) }
                }
            }
            if (capturedUri != null) {
                Card(shape = CuteCardDefaults.ShapeCompact, colors = CardDefaults.cardColors(containerColor = FieldMindTheme.colors.positive.copy(alpha = 0.08f)), elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(MaterialSymbolIcon("check_circle"), null, tint = FieldMindTheme.colors.positive, size = 24.dp)
                        Column(Modifier.weight(1f)) {
                            Text("$capturedType attached", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(capturedUri?.substringAfterLast("/")?.take(40) ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                if (capturedUri != null) {
                    viewModel.addNote(
                        title = "Attachment: $capturedType",
                        body = "Attached on ${java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault()).format(java.util.Date())}\nURI: $capturedUri",
                        category = capturedType, tags = "attachment, $capturedType",
                        onSaved = { onBack() }
                    )
                }
            }, modifier = Modifier.fillMaxWidth(), shape = CuteCardDefaults.ShapeCompact, enabled = capturedUri != null) {
                Icon(MaterialSymbolIcon("attach_file"), null, size = 18.dp); Spacer(Modifier.size(8.dp)); Text("Attach")
            }
        }
    }
}

@Composable
private fun AttachmentTypeItem(
    label: String, icon: MaterialSymbolIcon, accent: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier, onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = CuteCardDefaults.ShapeCompact,
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.10f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.size(40.dp).clip(MaterialTheme.shapes.medium).background(accent.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = accent, size = 22.dp) }
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = accent)
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  NEW FOLDER SCREEN — Create a folder (organizational note)
// ══════════════════════════════════════════════════════════════════════

@Composable
fun NewFolderScreen(viewModel: FieldMindViewModel, onBack: () -> Unit) {
    val colors = FieldMindTheme.colors
    var folderName by rememberSaveable { mutableStateOf("") }
    var selectedColor by rememberSaveable { mutableStateOf(0xFF1F6B4C) }
    val haptics = rememberFieldMindHaptics()
    var showExitConfirmation by rememberSaveable { mutableStateOf(false) }
    val isDirty = folderName.isNotBlank()

    BackHandler(enabled = isDirty) { showExitConfirmation = true }

    if (showExitConfirmation) {
        SwipeableAlertDialog(
            onDismissRequest = { showExitConfirmation = false },
            title = { Text("Discard changes?") },
            text = { Text("You have unsaved changes. Are you sure you want to go back?") },
            confirmButton = {
                Button(
                    onClick = { showExitConfirmation = false; onBack() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Discard") }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmation = false }) { Text("Keep editing") }
            }
        )
    }

    val colorOptions = listOf(
        0xFF1F6B4CL to "Green",
        0xFF2196F3L to "Blue",
        0xFF9C27B0L to "Purple",
        0xFFFF9800L to "Orange",
        0xFFF44336L to "Red"
    )

    Column(Modifier.fillMaxSize().statusBarsPadding().background(MaterialTheme.colorScheme.background)) {
        StandardScreenHeader(
            title = "New Folder",
            subtitle = "Organize project entities into a folder.",
            icon = MaterialSymbolIcon("folder"),
            heroColor = colors.hypothesis,
            trailing = { BackButton(onClick = onBack) }
        )
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp).padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            FieldTextField(folderName, { folderName = it }, "Folder Name", supportingText = "e.g. Butterflies, Water Samples")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Color", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    colorOptions.forEach { (colorLong, colorName) ->
                        val isSelected = selectedColor == colorLong
                        val borderMod = if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CuteCardDefaults.ButtonShape) else Modifier
                        Box(
                            modifier = Modifier.size(48.dp).clip(CuteCardDefaults.ButtonShape).background(Color(colorLong))
                                .then(borderMod)
                                .clickable { haptics.light(); selectedColor = colorLong },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) Icon(MaterialSymbolIcon("check"), null, tint = Color.White, size = 22.dp)
                        }
                    }
                }
            }
            Text("Parent folder", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("None (root folder)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                if (folderName.isNotBlank()) {
                    viewModel.addFolder(
                        name = folderName.trim(),
                        color = selectedColor,
                        projectId = null,
                        onSaved = { onBack() }
                    )
                }
            }, modifier = Modifier.fillMaxWidth(), shape = CuteCardDefaults.ShapeCompact, enabled = folderName.isNotBlank()) {
                Icon(MaterialSymbolIcon("folder"), null, size = 18.dp); Spacer(Modifier.size(8.dp)); Text("Create Folder")
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Shared helpers
// ══════════════════════════════════════════════════════════════════════

private fun defaultUnitForTool(tool: String): String = when (tool) {
    "Weather Log" -> "°C"
    "Measurement Log" -> "cm"
    "Counter", "Species Tracker" -> "count"
    "Event Log" -> "event"
    "Site Log" -> "site"
    "Checklist" -> "done/total"
    "Comparison Table" -> "score"
    else -> ""
}

private fun defaultLabelForTool(tool: String): String = when (tool) {
    "Weather Log" -> "Air temperature"
    "Measurement Log" -> "Measured length"
    "Species Tracker" -> "Species count"
    "Checklist" -> "Checklist item"
    "Event Log" -> "Observed event"
    "Site Log" -> "Site condition"
    "Comparison Table" -> "Comparison variable"
    else -> ""
}

@Composable
private fun ChoiceChipsField(label: String, options: List<String>, selected: String, onSelected: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        ChoiceChips(options, selected, onSelected = onSelected)
    }
}

@Composable
private fun DividerSection(title: String, icon: MaterialSymbolIcon? = null, accent: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (icon != null) { Icon(icon, null, tint = accent, size = 18.dp) }
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
}
