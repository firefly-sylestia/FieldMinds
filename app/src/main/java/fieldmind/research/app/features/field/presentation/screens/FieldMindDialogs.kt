package fieldmind.research.app.features.field.presentation.screens

import android.app.KeyguardManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import java.io.File
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import fieldmind.research.app.features.field.data.database.entity.*
import fieldmind.research.app.features.field.presentation.components.*
import fieldmind.research.app.features.field.presentation.theme.FieldMindTheme
import fieldmind.research.app.features.field.presentation.viewmodel.DraftEvidenceAttachment
import fieldmind.research.app.ui.theme.CuteCardDefaults
import fieldmind.research.app.ui.theme.CuteElevations
import fieldmind.research.app.ui.theme.cuteShadow
import fieldmind.research.app.features.field.presentation.viewmodel.FieldMindViewModel
import fieldmind.research.app.shared.presentation.components.icons.Icon
import fieldmind.research.app.shared.presentation.components.icons.MaterialSymbolIcon
import androidx.compose.animation.animateContentSize
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import android.Manifest
import androidx.compose.ui.platform.LocalUriHandler
import fieldmind.research.app.features.field.data.location.FieldLocationProvider
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
// ══════════════════════════════════════════════════════════════════════
//  Shared dialog helpers — consistent containers for all edit/create dialogs
// ══════════════════════════════════════════════════════════════════════

/**
 * Wraps any dialog content in a consistent Material 3 card with scroll support.
 * @param fullScreen When true, renders as a full-screen dialog (like ObserveScreen).
 * @param isDirty When true and back is pressed, shows a confirmation dialog to prevent data loss.
 */
@Composable
internal fun DialogWrapper(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    fullScreen: Boolean = false,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(16.dp),
    isDirty: () -> Boolean = { false },
    content: @Composable ColumnScope.() -> Unit
) {
    var showExitConfirm by remember { mutableStateOf(false) }

    // Confirmation dialog when exiting with unsaved changes
    if (showExitConfirm) {
        SwipeableAlertDialog(
            onDismissRequest = { showExitConfirm = false },
            icon = { Icon(icon = FieldMindIcons.Info, contentDescription = null, size = 28.dp) },
            title = { Text("Unsaved changes") },
            text = {
                Text(
                    "You have unsaved changes. What would you like to do?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { showExitConfirm = false; onDismiss() },
                    shape = CuteCardDefaults.ButtonShape,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirm = false }) {
                    Text("Keep editing")
                }
            }
        )
    }

    Dialog(onDismissRequest = { if (fullScreen && isDirty()) showExitConfirm = true else onDismiss() }, properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = false, dismissOnClickOutside = !fullScreen)) {
        // Handle system back button — placed INSIDE Dialog content for proper composition context
        val dispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
        DisposableEffect(dispatcher) {
            val callback = if (dispatcher != null) {
                val cb = object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (fullScreen && isDirty()) showExitConfirm = true else onDismiss()
                    }
                }
                dispatcher.addCallback(cb)
                cb
            } else null
            onDispose { callback?.remove() }
        }
        if (fullScreen) {
            Surface(
                modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(Modifier.fillMaxSize()) {
                    // Standardized full-screen header with back button
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CuteCardDefaults.Shape,
                        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.4f),
                        tonalElevation = 0.dp
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                onClick = { if (isDirty()) showExitConfirm = true else onDismiss() },
                                shape = CuteCardDefaults.ButtonShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(FieldMindIcons.Back, null, tint = MaterialTheme.colorScheme.onSurface, size = 22.dp)
                                }
                            }
                        }
                    }
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                            .padding(bottom = 40.dp),
                        verticalArrangement = verticalArrangement
                    ) { content() }
                }
            }
        } else {
            Surface(
                modifier = modifier.fillMaxWidth(0.94f).wrapContentHeight().padding(vertical = 24.dp).cuteShadow(elevation = CuteElevations.plushTier4, shape = CuteCardDefaults.Shape),
                shape = CuteCardDefaults.Shape,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                tonalElevation = CuteElevations.plushTier4
            ) {
                Column(
                    Modifier.verticalScroll(rememberScrollState()).padding(20.dp),
                    verticalArrangement = verticalArrangement
                ) { content() }
            }
        }
    }
}

/**
 * Polished dialog header matching StandardScreenHeader visual style.
 * Icon with hero-color background, title (headlineSmall), and subtitle.
 */
@Composable
internal fun DialogHeader(
    icon: MaterialSymbolIcon,
    title: String,
    subtitle: String,
    accent: Color = MaterialTheme.colorScheme.primary
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(
            Modifier.size(44.dp).clip(CuteCardDefaults.ButtonShape)
                .background(accent.copy(alpha = if (FieldMindTheme.colors.isDark) 0.28f else 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = accent, size = 24.dp)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    Spacer(Modifier.height(8.dp))
}

/**
 * Section header with optional icon and thin divider.
 */
@Composable
private fun DialogDividerSection(
    title: String,
    icon: MaterialSymbolIcon? = null,
    accent: Color = MaterialTheme.colorScheme.primary
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (icon != null) {
            Icon(icon, null, tint = accent, size = 18.dp)
        }
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
}

/**
 * Standardized action buttons row (Cancel + Save).
 */
@Composable
internal fun DialogActions(
    onCancel: () -> Unit,
    onSave: () -> Unit,
    saveEnabled: Boolean = true,
    saveLabel: String = "Save"
) {
    Row(
        Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onCancel) { Text("Cancel") }
        Spacer(Modifier.size(8.dp))
        Button(
            onClick = onSave,
            shape = CuteCardDefaults.ShapeCompact,
            enabled = saveEnabled
        ) { Text(saveLabel) }
    }
}

/**
 * Compact label + ChoiceChips row for form sections.
 */
/**
 * Compact label + ChoiceChips row for form sections.
 * Used in New dialogs; Edit dialogs use OptionPickerField instead.
 */
@Composable
private fun ChoiceChipsField(
    label: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        ChoiceChips(options, selected, onSelected = onSelected)
    }
}

/**
 * Slide-in field section with animated expand/collapse.
 */
@Composable
internal fun CollapsibleSection(
    title: String,
    subtitle: String = "",
    icon: MaterialSymbolIcon = FieldMindIcons.Settings,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CuteCardDefaults.FieldShape,
        colors = CardDefaults.cardColors(
            containerColor = if (expanded) MaterialTheme.colorScheme.surfaceContainerLow
            else MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
    ) {
        Column(Modifier.padding(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    Modifier.size(36.dp).clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, size = 20.dp) }
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    if (subtitle.isNotBlank()) {
                        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Icon(
                    if (expanded) FieldMindIcons.Up else FieldMindIcons.Down,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    size = 18.dp
                )
            }
            AnimatedVisibility(visible = expanded, enter = expandVertically(), exit = shrinkVertically()) {
                Column(Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp)) { content() }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  New Entity Dialogs
// ══════════════════════════════════════════════════════════════════════

@Composable
internal fun NewQuestionDialog(viewModel: FieldMindViewModel, onDismiss: () -> Unit, projectId: Long? = null, entity: QuestionEntity? = null) {
    var question by remember { mutableStateOf(entity?.questionText ?: "") }
    var category by remember { mutableStateOf(entity?.category ?: "Other") }
    var source by remember { mutableStateOf(entity?.sourceType ?: "Observation") }
    var status by remember { mutableStateOf(entity?.status ?: "New") }
    var priority by remember { mutableStateOf(entity?.priority ?: "Medium") }
    var answer by remember { mutableStateOf(entity?.answer ?: "") }
    var showAdvanced by remember { mutableStateOf(false) }

    fun save() {
        if (question.isNotBlank()) {
            if (entity != null) {
                val latest = viewModel.questions.value.firstOrNull { it.id == entity.id } ?: entity
                viewModel.updateQuestionEntity(latest.copy(
                    questionText = question.trim(),
                    category = category,
                    sourceType = source,
                    status = status,
                    priority = priority,
                    answer = answer.trim(),
                    answeredAt = if (answer.isBlank()) null else (latest.answeredAt ?: System.currentTimeMillis())
                ))
            } else {
                viewModel.addQuestion(question, category, source, status, priority, projectId = projectId, answer = answer)
            }
            onDismiss()
        }
    }

    DialogWrapper(onDismiss = onDismiss, fullScreen = true, isDirty = { question.isNotBlank() || answer.isNotBlank() }) {
        DialogHeader(FieldMindIcons.Question, "New Question", "Turn curiosity into something observable, measurable, comparable, or verifiable.")
        if (projectId != null) {
            Text("Linked to project", style = MaterialTheme.typography.labelSmall, color = FieldMindTheme.colors.project, fontWeight = FontWeight.SemiBold)
        }
        FieldTextField(question, { question = it }, "What do you want to find out?", minLines = 3, supportingText = "Example: Do bird visits increase after rain at this site?")
        DialogDividerSection("Classification", FieldMindIcons.Category)
        ChoiceChipsField("Category", observationCategories, category) { category = it }
        ChoiceChipsField("Source", sourceTypes, source) { source = it }
        ChoiceChipsField("Priority", listOf("Low", "Medium", "High"), priority) { priority = it }
        ChoiceChipsField("Status", questionStatuses, status) { status = it }
        CollapsibleSection("Advanced options", "Answer, cross-links, and metadata", expanded = showAdvanced, onToggle = { showAdvanced = !showAdvanced }) {
            FieldTextField(answer, { answer = it }, "Preliminary answer", minLines = 2, supportingText = "Optional — add if you already have a working answer")
        }
        DialogActions(onCancel = onDismiss, onSave = { save() }, saveEnabled = question.isNotBlank())
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun NewProjectDialog(
    viewModel: FieldMindViewModel,
    initialTitle: String = "",
    initialTopic: String = "Biology",
    initialObjective: String = "",
    initialQuestion: String = "",
    initialBackground: String = "",
    initialMethods: String = "",
    initialHypothesis: String = "",
    initialDataPlan: String = "",
    initialAnalysis: String = "",
    initialConclusion: String = "",
    initialNextAction: String = "",
    initialProjectType: String = "Observation",
    initialSelectedMethods: String = initialMethods,
    initialConnectionMap: String = "",
    initialTags: String = "",
    onDismiss: () -> Unit,
    templateGuide: ProjectTemplateDef? = null,
    entity: ProjectEntity? = null
) {
    var title by remember(initialTitle.ifBlank { entity?.title ?: "" }) { mutableStateOf(entity?.title ?: initialTitle) }
    var topic by remember(initialTopic.ifBlank { entity?.topicType ?: "Biology" }) { mutableStateOf(entity?.topicType ?: initialTopic.ifBlank { "Biology" }) }
    var objective by remember(initialObjective.ifBlank { entity?.objective ?: "" }) { mutableStateOf(entity?.objective ?: initialObjective) }
    var question by remember(initialQuestion.ifBlank { entity?.researchQuestion ?: "" }) { mutableStateOf(entity?.researchQuestion ?: initialQuestion) }
    var background by remember(initialBackground.ifBlank { entity?.backgroundNotes ?: "" }) { mutableStateOf(entity?.backgroundNotes ?: initialBackground) }
    var methods by remember(initialMethods.ifBlank { entity?.methods ?: "" }) { mutableStateOf(entity?.methods ?: initialMethods) }
    var hypothesis by remember(initialHypothesis.ifBlank { entity?.hypothesisSummary ?: "" }) { mutableStateOf(entity?.hypothesisSummary ?: initialHypothesis) }
    var dataPlan by remember(initialDataPlan.ifBlank { entity?.dataSummary ?: "" }) { mutableStateOf(entity?.dataSummary ?: initialDataPlan) }
    var analysis by remember(initialAnalysis.ifBlank { entity?.analysis ?: "" }) { mutableStateOf(entity?.analysis ?: initialAnalysis) }
    var conclusion by remember(initialConclusion.ifBlank { entity?.conclusion ?: "" }) { mutableStateOf(entity?.conclusion ?: initialConclusion) }
    var nextAction by remember(initialNextAction.ifBlank { entity?.futureQuestions ?: "" }) { mutableStateOf(entity?.futureQuestions ?: initialNextAction) }
    var projectType by remember((initialProjectType.ifBlank { entity?.projectType ?: "Observation" })) { mutableStateOf(entity?.projectType ?: initialProjectType.ifBlank { "Observation" }) }
    var selectedMethods by remember(initialSelectedMethods.ifBlank { entity?.selectedMethods ?: "" }) { mutableStateOf(entity?.selectedMethods ?: initialSelectedMethods) }
    var connectionMap by remember(initialConnectionMap.ifBlank { entity?.connectionMap ?: "" }) { mutableStateOf(entity?.connectionMap ?: initialConnectionMap) }
    var tags by remember { mutableStateOf(initialTags) }
    var showAdvanced by remember { mutableStateOf(initialProjectType.isNotBlank() || initialSelectedMethods.isNotBlank() || initialTags.isNotBlank() || entity != null) }
    var showGuide by remember { mutableStateOf(templateGuide != null) }

    fun save() {
        if (title.isNotBlank()) {
            if (entity != null) {
                val latest = viewModel.projects.value.firstOrNull { it.id == entity.id } ?: entity
                viewModel.updateProjectEntity(latest.copy(
                    title = title.trim(),
                    topicType = topic.trim().ifBlank { "General" },
                    objective = objective.trim(),
                    researchQuestion = question.trim(),
                    backgroundNotes = background.trim(),
                    methods = methods.trim(),
                    hypothesisSummary = hypothesis.trim(),
                    dataSummary = dataPlan.trim(),
                    analysis = analysis.trim(),
                    conclusion = conclusion.trim(),
                    futureQuestions = nextAction.trim(),
                    projectType = projectType,
                    selectedMethods = selectedMethods,
                    connectionMap = connectionMap
                ))
            } else {
                viewModel.addProject(title, topic, objective, question, methods, nextAction, background, hypothesis, dataPlan, analysis, conclusion, projectType = projectType, selectedMethods = selectedMethods, connectionMap = listOfNotNull(connectionMap.takeIf { it.isNotBlank() }, tags.takeIf { it.isNotBlank() }?.let { "Template tags: $it" }).joinToString("\n"))
            }
            onDismiss()
        }
    }

    DialogWrapper(onDismiss = onDismiss, fullScreen = true, isDirty = { title.isNotBlank() || objective.isNotBlank() || question.isNotBlank() || methods.isNotBlank() }) {
        DialogHeader(FieldMindIcons.Project, "New Project", "Define the question, evidence plan, data fields, and report direction.", accent = FieldMindTheme.colors.project)
        
        // Template Guide — shows guidance from selected template without auto-filling fields
        if (templateGuide != null) {
            Card(
                shape = CuteCardDefaults.FieldShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(templateGuide.icon, null, tint = FieldMindTheme.colors.project, size = 22.dp)
                        Column(Modifier.weight(1f)) {
                            Text(templateGuide.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(templateGuide.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Surface(
                            onClick = { showGuide = !showGuide },
                            shape = RoundedCornerShape(18.dp),
                            color = FieldMindTheme.colors.project.copy(alpha = 0.12f)
                        ) {
                            Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(if (showGuide) "Hide" else "Guide", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = FieldMindTheme.colors.project)
                                Icon(if (showGuide) FieldMindIcons.Up else FieldMindIcons.Down, null, tint = FieldMindTheme.colors.project, size = 14.dp)
                            }
                        }
                    }
                    AnimatedVisibility(visible = showGuide, enter = expandVertically(), exit = shrinkVertically()) {
                        Column(Modifier.padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            HorizontalDivider(color = FieldMindTheme.colors.project.copy(alpha = 0.12f))
                            GuideRow("Question", templateGuide.question)
                            GuideRow("Background", templateGuide.background)
                            GuideRow("Method", templateGuide.methodPlan)
                            GuideRow("Hypothesis", templateGuide.hypothesis)
                            GuideRow("Data fields", templateGuide.dataPlan)
                            GuideRow("Analysis", templateGuide.analysisPlan)
                            GuideRow("Next action", templateGuide.nextAction)
                            if (templateGuide.defaultMethods.isNotEmpty()) {
                                Text("Recommended methods", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = FieldMindTheme.colors.project)
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    templateGuide.defaultMethods.forEach { m ->
                                        Surface(shape = CuteCardDefaults.ChipShape, color = FieldMindTheme.colors.project.copy(alpha = 0.1f)) {
                                            Text(m, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = FieldMindTheme.colors.project, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        DialogDividerSection("Topic & title", FieldMindIcons.Category, FieldMindTheme.colors.project)
        FieldTextField(title, { title = it }, "Project title")
        ChoiceChipsField("Topic", listOf("Biology", "Geology", "Wildlife", "Ecology", "Plant Study", "Weather", "Human Pattern", "Other"), topic) { topic = it }
        DialogDividerSection("Project type", FieldMindIcons.Category, FieldMindTheme.colors.project)
        OptionPickerField(label = "Type", selected = projectType, options = (listOf("Observation", "Experiment", "Literature Review", "Field Survey", "Long-term Monitor") + researchProjectTypes).distinct(), onSelected = { projectType = it }, icon = FieldMindIcons.Category)
        DialogDividerSection("Research purpose", FieldMindIcons.School, FieldMindTheme.colors.project)
        FieldTextField(objective, { objective = it }, "Objective", minLines = 2)
        FieldTextField(question, { question = it }, "Research question", minLines = 2)
        FieldTextField(background, { background = it }, "Background / context", minLines = 2)
        DialogDividerSection("Evidence plan", FieldMindIcons.Data, FieldMindTheme.colors.project)
        FieldTextField(methods, { methods = it }, "Method / data plan", minLines = 3)
        FieldTextField(hypothesis, { hypothesis = it }, "Hypothesis summary", minLines = 2)
        FieldTextField(dataPlan, { dataPlan = it }, "Data fields / units", minLines = 2, supportingText = "Example: temperature °C, height cm, water clarity, count")
        DialogDividerSection("Report direction", FieldMindIcons.Report, FieldMindTheme.colors.project)
        FieldTextField(analysis, { analysis = it }, "Analysis plan", minLines = 2)
        FieldTextField(conclusion, { conclusion = it }, "Early conclusion / expected output", minLines = 2)
        FieldTextField(nextAction, { nextAction = it }, "Next action", supportingText = "Example: observe the same site at sunset for 3 days")
        CollapsibleSection("Advanced options", "Methods, connection map, and tags", expanded = showAdvanced, onToggle = { showAdvanced = !showAdvanced }) {
            FieldTextField(selectedMethods, { selectedMethods = it }, "Selected methods", supportingText = "e.g. transect, quadrat, interview, water test")
            FieldTextField(connectionMap, { connectionMap = it }, "Connection map", minLines = 2, supportingText = "How this project relates to other observations or projects")
            FieldTextField(tags, { tags = it }, "Template tags", supportingText = "Comma-separated tags copied from the selected template")
        }
        DialogActions(onCancel = onDismiss, onSave = { save() }, saveEnabled = title.isNotBlank())
    }
}

@Composable
private fun GuideRow(label: String, value: String) {
    if (value.isNotBlank()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
            Text("$label:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = FieldMindTheme.colors.project.copy(alpha = 0.8f), modifier = Modifier.width(80.dp))
            Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        }
    }
}
@Composable
private fun SourceFormHero(title: String, body: String) {
    Card(shape = CuteCardDefaults.Shape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)) {
        Row(Modifier.fillMaxWidth().padding(18.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(CuteCardDefaults.ButtonShape).background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                Icon(FieldMindIcons.Source, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, size = 24.dp)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f))
            }
        }
    }
}

@Composable
internal fun SourcePreviewCard(link: String, fileUri: String, modifier: Modifier = Modifier) {
    val trimmedLink = link.trim()
    val videoId = remember(trimmedLink) { youtubeVideoId(trimmedLink) }
    if (trimmedLink.isBlank() && fileUri.isBlank()) return
    Card(modifier = modifier.fillMaxWidth(), shape = CuteCardDefaults.Shape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh), elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(icon = if (videoId != null) FieldMindIcons.Play else if (fileUri.isNotBlank()) FieldMindIcons.File else FieldMindIcons.OpenLink, contentDescription = null, tint = MaterialTheme.colorScheme.primary, size = 20.dp)
                Column(Modifier.weight(1f)) {
                    Text(if (videoId != null) "YouTube preview" else if (fileUri.isNotBlank()) "Local file reference" else "Link preview", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(if (videoId != null) "youtube.com/embed/$videoId" else fileUri.ifBlank { trimmedLink }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            videoId?.let { id ->
                AsyncImage(
                    model = "https://img.youtube.com/vi/$id/hqdefault.jpg",
                    contentDescription = "YouTube thumbnail",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(150.dp).clip(CuteCardDefaults.ShapeCompact).background(MaterialTheme.colorScheme.surfaceContainerHighest)
                )
            }
            if (trimmedLink.isNotBlank() && videoId == null) {
                InfoChip(trimmedLink.substringAfter("://", trimmedLink).substringBefore('/'), icon = FieldMindIcons.OpenLink)
            }
            if (fileUri.isNotBlank()) InfoChip(fileUri.substringAfterLast('/').take(48).ifBlank { "Attached file" }, icon = FieldMindIcons.File)
        }
    }
}


/**
 * Progressive disclosure section for forms.
 * Collapsible card section with expand/collapse toggle and filled-field counter.
 */
@Composable
private fun ProgressiveSection(
    title: String,
    description: String,
    icon: MaterialSymbolIcon,
    expanded: Boolean,
    onToggle: () -> Unit,
    filledCount: Int = 0,
    totalCount: Int = 0,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CuteCardDefaults.Shape,
        colors = CardDefaults.cardColors(
            containerColor = if (expanded) MaterialTheme.colorScheme.surfaceContainerLow
            else MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
    ) {
        Column(Modifier.padding(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(Modifier.size(36.dp).clip(RoundedCornerShape(18.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, size = 20.dp) }
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (filledCount > 0 && !expanded)
                            Text("($filledCount/$totalCount)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
                Icon(if (expanded) FieldMindIcons.Up else FieldMindIcons.Down, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 20.dp)
            }
            AnimatedVisibility(visible = expanded, enter = expandVertically(), exit = shrinkVertically()) {
                Column(Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp)) { content() }
            }
        }
    }
}

@Composable
internal fun NewSourceDialog(viewModel: FieldMindViewModel, onDismiss: () -> Unit, initialProjectId: Long? = null, entity: SourceEntity? = null) {
    val context = LocalContext.current
    val projects by viewModel.projects.collectAsState()
    val haptics = rememberFieldMindHaptics()
    var type by remember { mutableStateOf(entity?.type ?: "Article") }
    var title by remember { mutableStateOf(entity?.title ?: "") }
    var author by remember { mutableStateOf(entity?.author ?: "") }
    var dateOrYear by remember { mutableStateOf(entity?.dateOrYear ?: "") }
    var doiOrIsbn by remember { mutableStateOf(entity?.doiOrIsbn ?: "") }
    var publisherOrJournal by remember { mutableStateOf(entity?.publisherOrJournal ?: "") }
    var accessDate by remember { mutableStateOf(entity?.accessDate ?: today()) }
    var link by remember { mutableStateOf(entity?.link ?: "") }
    var fileUri by remember { mutableStateOf(entity?.fileUri ?: "") }
    var citationStyleNote by remember { mutableStateOf(entity?.citationStyleNote ?: "") }
    var importance by remember { mutableStateOf(entity?.importance ?: "Normal") }
    var readingStatus by remember { mutableStateOf(entity?.readingStatus ?: "In progress") }
    var summary by remember { mutableStateOf(entity?.personalSummary ?: "") }
    var taught by remember { mutableStateOf(entity?.whatThisSourceTaughtMe ?: "") }
    var findings by remember { mutableStateOf(entity?.keyFindings ?: "") }
    var questions by remember { mutableStateOf(entity?.questionsGenerated ?: "") }
    var notes by remember { mutableStateOf(entity?.paperNotes ?: "") }
    var reliability by remember { mutableStateOf((entity?.reliabilityScore ?: 3).toFloat()) }
    var projectId by remember { mutableStateOf(entity?.relatedProjectId ?: initialProjectId) }
    val docPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            fileUri = uri.toString()
            if (type !in listOf("PDF", "Image")) type = "Local document"
            haptics.light()
        }
    }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            fileUri = uri.toString()
            type = "Image"
            haptics.light()
        }
    }

    fun save() {
        if (title.isNotBlank()) {
            if (entity != null) {
                val latest = viewModel.sources.value.firstOrNull { it.id == entity.id } ?: entity
                viewModel.updateSourceEntity(
                    latest.copy(
                        type = type,
                        title = title.trim(),
                        author = author.trim(),
                        dateOrYear = dateOrYear.trim(),
                        doiOrIsbn = doiOrIsbn.trim(),
                        publisherOrJournal = publisherOrJournal.trim(),
                        accessDate = accessDate.trim(),
                        link = link.trim(),
                        fileUri = fileUri.trim(),
                        citationStyleNote = citationStyleNote.trim(),
                        importance = importance,
                        personalSummary = summary.trim(),
                        keyFindings = findings.trim(),
                        whatThisSourceTaughtMe = taught.trim(),
                        questionsGenerated = questions.trim(),
                        reliabilityScore = reliability.toInt(),
                        readingStatus = readingStatus,
                        paperNotes = notes.trim(),
                        relatedProjectId = projectId
                    )
                )
            } else {
                viewModel.addSource(
                    type = type,
                    title = title.trim(),
                    author = author.trim(),
                    link = link.trim(),
                    summary = summary.trim(),
                    taught = taught.trim(),
                    reliability = reliability.toInt(),
                    keyFindings = findings.trim(),
                    questionsGenerated = questions.trim(),
                    paperNotes = notes.trim(),
                    projectId = projectId,
                    dateOrYear = dateOrYear.trim(),
                    doiOrIsbn = doiOrIsbn.trim(),
                    publisherOrJournal = publisherOrJournal.trim(),
                    accessDate = accessDate.trim(),
                    fileUri = fileUri.trim(),
                    citationStyleNote = citationStyleNote.trim(),
                    importance = importance,
                    readingStatus = readingStatus
                )
            }
            onDismiss()
        }
    }

    DialogWrapper(onDismiss = onDismiss, fullScreen = true, isDirty = { title.isNotBlank() || author.isNotBlank() || summary.isNotBlank() || link.isNotBlank() || notes.isNotBlank() }) {
        DialogHeader(FieldMindIcons.Source, "Add Source", "Start with title + type. Fill in what you have.", accent = FieldMindTheme.colors.source)
        ChoiceChipsField("Source type", sourceLibraryTypes, type) { type = it }
        DialogDividerSection("Identity", FieldMindIcons.Article, FieldMindTheme.colors.source)
                FieldTextField(title, { title = it }, "Title")
                FieldTextField(author, { author = it }, "Author / creator")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FieldTextField(dateOrYear, { dateOrYear = it }, "Date / year", modifier = Modifier.weight(1f))
                    FieldTextField(accessDate, { accessDate = it }, "Accessed", modifier = Modifier.weight(1f))
                }
                FieldTextField(doiOrIsbn, { doiOrIsbn = it }, "DOI / ISBN")
                FieldTextField(publisherOrJournal, { publisherOrJournal = it }, "Publisher / journal")
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Text("Link or file", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                FieldTextField(link, { link = it }, "Web link")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { haptics.light(); docPicker.launch(arrayOf("application/pdf", "text/*", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "image/*")) }, modifier = Modifier.weight(1f)) {
                        Icon(FieldMindIcons.File, null, size = 18.dp); Spacer(Modifier.size(6.dp)); Text("Document")
                    }
                    OutlinedButton(onClick = { haptics.light(); imagePicker.launch("image/*") }, modifier = Modifier.weight(1f)) {
                        Icon(FieldMindIcons.Gallery, null, size = 18.dp); Spacer(Modifier.size(6.dp)); Text("Image")
                    }
                }
                if (fileUri.isNotBlank()) FieldTextField(fileUri, { fileUri = it }, "Attached file URI")
                SourcePreviewCard(link = link, fileUri = fileUri)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Text("Reading notes", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                FieldTextField(summary, { summary = it }, "Main idea", minLines = 2)
                FieldTextField(findings, { findings = it }, "Key findings", minLines = 2)
                FieldTextField(taught, { taught = it }, "What this taught me", minLines = 2)
                FieldTextField(questions, { questions = it }, "New questions", minLines = 2)
                FieldTextField(notes, { notes = it }, "Paper / Cornell notes", minLines = 3)
                FieldTextField(citationStyleNote, { citationStyleNote = it }, "Citation style note")
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Text("Review & project", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("Reading status", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                ChoiceChips(readingStatuses, readingStatus) { readingStatus = it }
                Text("Importance", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                ChoiceChips(sourceImportanceLevels, importance) { importance = it }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Credibility", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${reliability.toInt()}/5", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Slider(reliability, { reliability = it }, valueRange = 1f..5f, steps = 3)
                if (projects.isNotEmpty()) {
                    Text("Link to project", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    ChoiceChips(listOf("No project") + projects.map { it.title }, projects.firstOrNull { it.id == projectId }?.title ?: "No project") { selected -> projectId = projects.firstOrNull { it.title == selected }?.id }
                }
                DialogActions(onCancel = onDismiss, onSave = { save() }, saveEnabled = title.isNotBlank())
    }
}
@Composable
internal fun NewHypothesisDialog(viewModel: FieldMindViewModel, questions: List<QuestionEntity>, onDismiss: () -> Unit, entity: HypothesisEntity? = null) {
    var prediction by remember { mutableStateOf(entity?.prediction ?: "") }
    var reasoning by remember { mutableStateOf(entity?.reasoning ?: "") }
    var evidence by remember { mutableStateOf(entity?.evidenceNeeded ?: "") }
    var support by remember { mutableStateOf(entity?.supportCriteria ?: "") }
    var weaken by remember { mutableStateOf(entity?.weakeningCriteria ?: "") }
    var test by remember { mutableStateOf(entity?.testMethod ?: "") }
    var confidence by remember { mutableStateOf((entity?.confidencePercent ?: 50).toFloat()) }
    var linkedId by remember { mutableStateOf(entity?.linkedQuestionId ?: questions.firstOrNull()?.id) }
    var resultStatus by remember { mutableStateOf(entity?.resultStatus ?: "Unknown") }
    var showAdvanced by remember { mutableStateOf(false) }

    fun save() {
        if (prediction.isNotBlank()) {
            if (entity != null) {
                val latest = viewModel.hypotheses.value.firstOrNull { it.id == entity.id } ?: entity
                viewModel.updateHypothesisEntity(latest.copy(
                    prediction = prediction.trim(),
                    reasoning = reasoning.trim(),
                    evidenceNeeded = evidence.trim(),
                    supportCriteria = support.trim(),
                    weakeningCriteria = weaken.trim(),
                    testMethod = test.trim(),
                    resultStatus = resultStatus,
                    confidencePercent = confidence.toInt(),
                    linkedQuestionId = linkedId
                ))
            } else {
                viewModel.addHypothesis(linkedId, prediction, evidence, confidence.toInt(), reasoning, support, weaken, test, resultStatus = resultStatus)
            }
            onDismiss()
        }
    }

    DialogWrapper(onDismiss = onDismiss, fullScreen = true, isDirty = { prediction.isNotBlank() || reasoning.isNotBlank() }) {
        DialogHeader(FieldMindIcons.Hypothesis, "New Hypothesis", "State the prediction, what would support it, and what would weaken it.", accent = FieldMindTheme.colors.hypothesis)
        if (questions.isNotEmpty()) {
            ChoiceChipsField("Linked question", listOf("No question") + questions.take(8).map { it.questionText.take(28) }, questions.firstOrNull { it.id == linkedId }?.questionText?.take(28) ?: "No question") { picked -> linkedId = questions.firstOrNull { it.questionText.startsWith(picked) }?.id }
        }
        FieldTextField(prediction, { prediction = it }, "Prediction", minLines = 3)
        FieldTextField(reasoning, { reasoning = it }, "Why this might happen", minLines = 2)
        DialogDividerSection("Evidence rules", FieldMindIcons.Done, FieldMindTheme.colors.hypothesis)
        Text("Decide success/failure before you bias yourself.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        FieldTextField(evidence, { evidence = it }, "Evidence needed", minLines = 2)
        FieldTextField(support, { support = it }, "Support criteria")
        FieldTextField(weaken, { weaken = it }, "Weakening criteria")
        FieldTextField(test, { test = it }, "Test method")
        DialogDividerSection("Confidence", FieldMindIcons.Streak, FieldMindTheme.colors.hypothesis)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Confidence", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${confidence.toInt()}%", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Slider(confidence, { confidence = it }, valueRange = 0f..100f)
        LinearProgressIndicator(progress = { confidence / 100f }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(8.dp)), color = MaterialTheme.colorScheme.primary)
        CollapsibleSection("Advanced options", "Result status tracking", expanded = showAdvanced, onToggle = { showAdvanced = !showAdvanced }) {
            ChoiceChipsField("Result status", listOf("Unknown", "Supported", "Weakened", "Inconclusive"), resultStatus) { resultStatus = it }
        }
        DialogActions(onCancel = onDismiss, onSave = { save() }, saveEnabled = prediction.isNotBlank())
    }
}

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

private fun valueLabelForTool(tool: String): String = when (tool) {
    "Weather Log" -> "Temperature / humidity / wind value"
    "Measurement Log" -> "Measurement value"
    "Checklist" -> "Done / total"
    "Event Log" -> "Event count or time"
    else -> "Value / items / samples"
}

private fun notesLabelForTool(tool: String): String = when (tool) {
    "Weather Log" -> "Sky, wind, precipitation, pressure notes"
    "Measurement Log" -> "Method, instrument, calibration notes"
    "Species Tracker" -> "Species, behavior, confidence, evidence"
    "Site Log" -> "Habitat, substrate, disturbance, access notes"
    else -> "Notes"
}

@Composable
internal fun NewDataRecordDialog(viewModel: FieldMindViewModel, onDismiss: () -> Unit, entity: DataRecordEntity? = null) {
    var tool by remember { mutableStateOf(entity?.toolType ?: "Counter") }
    var label by remember { mutableStateOf(entity?.label ?: "") }
    var value by remember { mutableStateOf(entity?.value ?: "0") }
    var unit by remember { mutableStateOf(entity?.unit ?: defaultUnitForTool(entity?.toolType ?: "Counter")) }
    var location by remember { mutableStateOf(entity?.location ?: "") }
    var notes by remember { mutableStateOf(entity?.notes ?: "") }

    fun save() {
        if (label.isNotBlank()) {
            if (entity != null) {
                val latest = viewModel.dataRecords.value.firstOrNull { it.id == entity.id } ?: entity
                viewModel.updateDataRecordEntity(latest.copy(
                    toolType = tool,
                    label = label.trim(),
                    value = value.trim(),
                    unit = unit.trim(),
                    location = location.trim(),
                    notes = notes.trim()
                ))
            } else {
                viewModel.addDataRecord(tool, label, value, unit, notes, location)
            }
            onDismiss()
        }
    }

    DialogWrapper(onDismiss = onDismiss) {
        DialogHeader(FieldMindIcons.Data, "Data Collection Tool", "Choose a preset so units and labels match the kind of thing you measured.", accent = FieldMindTheme.colors.data)
        DialogDividerSection("Preset", FieldMindIcons.Settings, FieldMindTheme.colors.data)
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
        DialogDividerSection("Measurement", FieldMindIcons.Line, FieldMindTheme.colors.data)
        FieldTextField(value, { value = it }, "Value / items / samples", keyboardType = KeyboardType.Number)
        FieldTextField(unit, { unit = it }, "Unit", supportingText = "Suggested for $tool: ${defaultUnitForTool(tool)}")
        FieldTextField(location, { location = it }, "Location / site")
        DialogDividerSection("Context", FieldMindIcons.Note, FieldMindTheme.colors.data)
        ChoiceChips(contextPresets, notes) { notes = if (notes.isBlank()) it else "$notes, $it" }
        FieldTextField(notes, { notes = it }, "Notes", minLines = 3)
        DialogActions(onCancel = onDismiss, onSave = { save() }, saveEnabled = label.isNotBlank())
    }
}
@Composable
internal fun NewReportDialog(viewModel: FieldMindViewModel, onDismiss: () -> Unit, entity: ReportEntity? = null) {
    var type by remember { mutableStateOf(entity?.type ?: "Field Report") }
    var title by remember { mutableStateOf(entity?.title ?: "") }
    var background by remember { mutableStateOf(entity?.background ?: "") }
    var question by remember { mutableStateOf(entity?.question ?: "") }
    var methods by remember { mutableStateOf(entity?.methods ?: "") }
    var observations by remember { mutableStateOf(entity?.observations ?: "") }
    var results by remember { mutableStateOf(entity?.results ?: "") }
    var interpretation by remember { mutableStateOf(entity?.interpretation ?: "") }
    var conclusion by remember { mutableStateOf(entity?.conclusion ?: "") }
    var limitations by remember { mutableStateOf(entity?.limitations ?: "") }
    var next by remember { mutableStateOf(entity?.nextSteps ?: "") }

    fun save() {
        if (title.isNotBlank()) {
            if (entity != null) {
                val latest = viewModel.reports.value.firstOrNull { it.id == entity.id } ?: entity
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
                viewModel.addReport(type, title, background, question, methods, observations, results, interpretation, conclusion, limitations, next)
            }
            onDismiss()
        }
    }

    DialogWrapper(onDismiss = onDismiss, fullScreen = true, isDirty = { title.isNotBlank() || background.isNotBlank() || question.isNotBlank() || methods.isNotBlank() }) {
        DialogHeader(FieldMindIcons.Report, "Report Builder", "Create a clean local draft: claim, evidence, reasoning, limitations, and next steps.", accent = FieldMindTheme.colors.report)
        DialogDividerSection("Type & title", FieldMindIcons.Category, FieldMindTheme.colors.report)
        ChoiceChipsField("Report type", reportTypes, type) { type = it }
        FieldTextField(title, { title = it }, "Title")
        DialogDividerSection("Setup", FieldMindIcons.School, FieldMindTheme.colors.report)
        FieldTextField(background, { background = it }, "Background", minLines = 2)
        FieldTextField(question, { question = it }, "Question", minLines = 2)
        FieldTextField(methods, { methods = it }, "Methods", minLines = 2)
        DialogDividerSection("Evidence", FieldMindIcons.Data, FieldMindTheme.colors.report)
        FieldTextField(observations, { observations = it }, "Observations", minLines = 2)
        FieldTextField(results, { results = it }, "Data / results", minLines = 2)
        FieldTextField(interpretation, { interpretation = it }, "Interpretation", minLines = 2)
        DialogDividerSection("Conclusion", FieldMindIcons.Check, FieldMindTheme.colors.report)
        FieldTextField(conclusion, { conclusion = it }, "Conclusion", minLines = 2)
        FieldTextField(limitations, { limitations = it }, "Limitations", minLines = 2)
        FieldTextField(next, { next = it }, "Next steps", minLines = 2)
        DialogActions(onCancel = onDismiss, onSave = { save() }, saveEnabled = title.isNotBlank())
    }
}
@Composable
internal fun NewFlashcardDialog(viewModel: FieldMindViewModel, onDismiss: () -> Unit, entity: FlashcardEntity? = null) {
    var type by remember { mutableStateOf(entity?.type ?: "concept") }
    var front by remember { mutableStateOf(entity?.front ?: "") }
    var back by remember { mutableStateOf(entity?.back ?: "") }
    var useSm2 by remember { mutableStateOf(entity?.deckMode == "sm2") }

    fun save() {
        if (front.isNotBlank() && back.isNotBlank()) {
            if (entity != null) {
                val latest = viewModel.flashcards.value.firstOrNull { it.id == entity.id } ?: entity
                viewModel.updateFlashcardEntity(latest.copy(
                    type = type,
                    front = front.trim(),
                    back = back.trim(),
                    deckMode = if (useSm2) "sm2" else "basic"
                ))
            } else {
                viewModel.addFlashcard(front, back, type, deckMode = if (useSm2) "sm2" else "basic")
            }
            onDismiss()
        }
    }

    DialogWrapper(onDismiss = onDismiss) {
        DialogHeader(FieldMindIcons.Flashcard, "Create Flashcard", "Design one card that flips cleanly during review — one prompt, one answer.", accent = FieldMindTheme.colors.flashcard)
        ChoiceChipsField("Card preset", listOf("term", "definition", "concept", "question-answer", "mistake card", "field ID", "method step"), type) { type = it }
        DialogDividerSection("Content", FieldMindIcons.Edit, FieldMindTheme.colors.flashcard)
        FieldTextField(front, { front = it }, "Prompt / front", minLines = 2)
        FieldTextField(back, { back = it }, "Answer / back", minLines = 4)
        DialogDividerSection("Study mode", FieldMindIcons.Flip, FieldMindTheme.colors.flashcard)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Spaced repetition (SM-2)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("Review at optimal intervals", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = useSm2, onCheckedChange = { useSm2 = it })
        }
        DialogActions(onCancel = onDismiss, onSave = { save() }, saveEnabled = front.isNotBlank() && back.isNotBlank())
    }
}

@Composable
internal fun NewObservationDialog(viewModel: FieldMindViewModel, onDismiss: () -> Unit, projectId: Long? = null, entity: ObservationEntity? = null) {
    val context = LocalContext.current
    val haptics = rememberFieldMindHaptics()
    var subject by remember { mutableStateOf(entity?.subject ?: "") }
    var category by remember { mutableStateOf(entity?.category ?: "Other") }
    var facts by remember { mutableStateOf(entity?.factsOnlyNotes ?: "") }
    var confidence by remember { mutableStateOf(entity?.confidenceLevel ?: "Likely") }
    var location by remember { mutableStateOf(entity?.manualLocation ?: "") }
    var latitude by remember { mutableStateOf(entity?.latitude?.toString() ?: "") }
    var longitude by remember { mutableStateOf(entity?.longitude?.toString() ?: "") }
    var tags by remember { mutableStateOf(entity?.tags ?: "") }
    var evidence by remember { mutableStateOf(entity?.evidenceSummary ?: "") }
    var fieldContext by remember { mutableStateOf(entity?.moodOrContext ?: "") }
    var attachments by remember { mutableStateOf<List<DraftEvidenceAttachment>>(emptyList()) }
    var showCamera by remember { mutableStateOf(false) }
    var locating by remember { mutableStateOf(false) }
    var showAdvanced by remember { mutableStateOf(false) }
    val locationProvider = remember { FieldLocationProvider(context) }

    // ── Audio recording state ──
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var audioFile by remember { mutableStateOf<File?>(null) }
    var recording by remember { mutableStateOf(false) }
    var recordSeconds by remember { mutableIntStateOf(0) }

    fun startLocating() {
        locating = true
        locationProvider.requestCurrentLocation { captured ->
            locating = false
            if (captured != null) {
                latitude = captured.latitude.toString()
                longitude = captured.longitude.toString()
                location = captured.asDisplayText()
                locationProvider.resolvePlaceName(captured.latitude, captured.longitude) { place ->
                    if (!place.isNullOrBlank()) location = captured.copy(placeName = place).asDisplayText()
                }
            }
        }
    }

    val locationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (result.values.any { it }) startLocating() else {}
    }
    val mediaPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        attachments = attachments + uris.map { DraftEvidenceAttachment("Gallery", it.toString(), "Media") }
        haptics.light()
    }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            runCatching { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            attachments = attachments + DraftEvidenceAttachment("File", it.toString(), "File / PDF")
            haptics.light()
        }
    }
    val audioImportPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            attachments = attachments + durableEvidenceAttachment(context, "Audio", uri, "Imported field audio")
            haptics.light()
        }
    }
    val audioPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val file = createFieldMindFile(context, "audio", ".m4a")
            val newRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else MediaRecorder()
            runCatching {
                newRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
                newRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                newRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                newRecorder.setOutputFile(file.absolutePath)
                newRecorder.prepare()
                newRecorder.start()
                audioFile = file; recorder = newRecorder; recording = true
            }.onFailure { newRecorder.release() }
        }
    }

    LaunchedEffect(recording) { if (recording) { recordSeconds = 0; while (recording) { delay(1000); recordSeconds++ } } }

    fun save() {
        if (subject.isNotBlank() || facts.isNotBlank()) {
            if (entity != null) {
                val latest = viewModel.observations.value.firstOrNull { it.id == entity.id } ?: entity
                viewModel.updateObservation(latest.copy(
                    subject = subject.trim(),
                    category = category,
                    factsOnlyNotes = facts.trim(),
                    confidenceLevel = confidence,
                    manualLocation = location.trim(),
                    latitude = latitude.toDoubleOrNull(),
                    longitude = longitude.toDoubleOrNull(),
                    evidenceSummary = evidence.trim(),
                    moodOrContext = fieldContext.trim()
                ), tags)
            } else {
                val effectiveSubject = subject.ifBlank { facts.take(48).ifBlank { "$category observation" } }
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
                    projectId = projectId
                )
            }
            attachments.forEach { viewModel.addAttachmentToObservation(entity?.id ?: 0L, it) }
            onDismiss()
        }
    }

    DialogWrapper(onDismiss = onDismiss, fullScreen = true, isDirty = { subject.isNotBlank() || facts.isNotBlank() || location.isNotBlank() || tags.isNotBlank() || attachments.isNotEmpty() }) {
        DialogHeader(FieldMindIcons.Observation, "New Observation", "Record what you observed — species, conditions, evidence.", accent = FieldMindTheme.colors.observation)
        if (projectId != null) {
            Text("Linked to project", style = MaterialTheme.typography.labelSmall, color = FieldMindTheme.colors.project, fontWeight = FontWeight.SemiBold)
        }

        // ── Species / Subject ──
        DialogDividerSection("Species / Subject", FieldMindIcons.Observation, FieldMindTheme.colors.observation)
        FieldTextField(subject, { subject = it }, "Species / Subject", supportingText = "Monarch Butterfly, Red-tailed Hawk…")

        // ── Description ──
        DialogDividerSection("Description", FieldMindIcons.Edit, FieldMindTheme.colors.observation)
        FieldTextField(facts, { facts = it }, "Description", minLines = 3, supportingText = "What exactly did you see, hear, or measure?")
        ChoiceChipsField("Category", observationCategories, category) { category = it }
        ChoiceChipsField("Confidence", confidenceOptions, confidence) { confidence = it }

        // ── Location ──
        DialogDividerSection("Location", FieldMindIcons.Location, FieldMindTheme.colors.observation)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilledTonalButton(
                onClick = { if (locationProvider.hasAnyLocationPermission()) startLocating() else locationPermission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) },
                modifier = Modifier.weight(1f),
                enabled = !locating
            ) {
                if (locating) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp) else Icon(FieldMindIcons.Location, null, size = 18.dp)
                Spacer(Modifier.size(6.dp)); Text(if (locating) "Locating…" else "Current Location")
            }
            OutlinedButton(onClick = { latitude = ""; longitude = ""; location = "" }, modifier = Modifier.weight(1f)) { Text("Clear") }
        }
        FieldTextField(location, { location = it }, "Location", supportingText = "e.g. Trailhead, Zone A, GPS coordinate")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FieldTextField(latitude, { latitude = it }, "Latitude", modifier = Modifier.weight(1f), keyboardType = KeyboardType.Decimal)
            FieldTextField(longitude, { longitude = it }, "Longitude", modifier = Modifier.weight(1f), keyboardType = KeyboardType.Decimal)
        }

        // ── Photos ──
        DialogDividerSection("Photos", FieldMindIcons.Camera, FieldMindTheme.colors.observation)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { showCamera = true }, Modifier.weight(1f)) { Icon(FieldMindIcons.Camera, null, size = 18.dp); Spacer(Modifier.size(6.dp)); Text("Photo") }
            OutlinedButton(onClick = { haptics.light(); mediaPicker.launch("image/*") }, Modifier.weight(1f)) { Icon(FieldMindIcons.Gallery, null, size = 18.dp); Spacer(Modifier.size(6.dp)); Text("Gallery") }
            OutlinedButton(onClick = { haptics.light(); filePicker.launch(arrayOf("application/pdf", "text/*", "image/*", "video/*", "audio/*")) }, Modifier.weight(1f)) { Icon(FieldMindIcons.File, null, size = 18.dp); Spacer(Modifier.size(6.dp)); Text("File") }
        }

        // ── Audio recording section ──
        Spacer(Modifier.height(6.dp))
        Card(shape = CuteCardDefaults.ShapeCompact, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(FieldMindIcons.Mic, null, tint = FieldMindTheme.colors.observation, size = 20.dp)
                    Text("Voice note", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    if (recording) {
                        Spacer(Modifier.weight(1f))
                        Text("${recordSeconds}s", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    }
                }
                Button(
                    onClick = {
                        if (recording) {
                            val file = audioFile
                            runCatching { recorder?.stop() }; recorder?.release(); recorder = null; recording = false
                            file?.let { attachments = attachments + DraftEvidenceAttachment("Audio", Uri.fromFile(it).toString(), "Voice note", localPath = it.absolutePath, mimeType = "audio/mp4") }
                        } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) audioPermission.launch(Manifest.permission.RECORD_AUDIO) else audioPermission.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = if (recording) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer) else ButtonDefaults.buttonColors()
                ) {
                    Icon(icon = if (recording) FieldMindIcons.Stop else FieldMindIcons.Mic, contentDescription = null, size = 18.dp)
                    Spacer(Modifier.size(6.dp))
                    Text(if (recording) "Stop recording" else "Record voice note")
                }
                OutlinedButton(onClick = { haptics.light(); audioImportPicker.launch(arrayOf("audio/*")) }, Modifier.fillMaxWidth()) { Icon(FieldMindIcons.Mic, null, size = 18.dp); Spacer(Modifier.size(6.dp)); Text("Or import audio file") }
            }
        }
        AttachmentPreviewList(attachments, onCaptionChange = { index, caption -> attachments = attachments.mapIndexed { i, item -> if (i == index) item.copy(caption = caption) else item } }, onRemove = { remove -> attachments = attachments.filterIndexed { index, _ -> index != remove } })

        // ── Tags ──
        DialogDividerSection("Tags", FieldMindIcons.Tag, FieldMindTheme.colors.observation)
        FieldTextField(tags, { tags = it }, "Tags", supportingText = "Comma-separated keywords — e.g. Butterfly, Pollinator")

        // ── Advanced ──
        CollapsibleSection("Advanced", "Evidence summary & field context", expanded = showAdvanced, onToggle = { showAdvanced = !showAdvanced }) {
            FieldTextField(evidence, { evidence = it }, "Evidence summary", minLines = 2, supportingText = "What evidence supports this observation?")
            FieldTextField(fieldContext, { fieldContext = it }, "Field context", minLines = 2, supportingText = "Weather, habitat, behavior, or conditions")
        }

        DialogActions(onCancel = onDismiss, onSave = { save() }, saveEnabled = subject.isNotBlank() || facts.isNotBlank(), saveLabel = "Save observation")
    }
    // In-app camera overlay
    if (showCamera) {
        Dialog(onDismissRequest = { showCamera = false }) {
            FieldMindCameraCapture(
                onPhotoCaptured = { uri, mimeType ->
                    attachments = attachments + DraftEvidenceAttachment("Photo", uri, "Observation photo", mimeType = mimeType)
                    showCamera = false
                },
                onDismiss = { showCamera = false }
            )
        }
    }
}

@Composable
internal fun NewNoteDialog(viewModel: FieldMindViewModel, onDismiss: () -> Unit, projectId: Long? = null, entity: NoteEntity? = null) {
    val context = LocalContext.current
    val haptics = rememberFieldMindHaptics()
    var title by remember { mutableStateOf(entity?.title ?: "") }
    var body by remember { mutableStateOf(entity?.body ?: "") }
    var category by remember { mutableStateOf(entity?.category ?: "Other") }
    var tags by remember { mutableStateOf(entity?.tags ?: "") }
    var location by remember { mutableStateOf("") }
    var showAdvanced by remember { mutableStateOf(false) }
    var showCamera by remember { mutableStateOf(false) }
    var attachments by remember { mutableStateOf<List<DraftEvidenceAttachment>>(emptyList()) }

    val mediaPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        attachments = attachments + uris.map { DraftEvidenceAttachment("Gallery", it.toString(), "Media") }
        haptics.light()
    }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            runCatching { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            attachments = attachments + DraftEvidenceAttachment("File", it.toString(), "File / PDF")
            haptics.light()
        }
    }

    fun save() {
        if (title.isNotBlank() || body.isNotBlank()) {
            if (entity != null) {
                val latest = viewModel.notes.value.firstOrNull { it.id == entity.id } ?: entity
                viewModel.updateNoteEntity(latest.copy(
                    title = title.trim().ifBlank { body.take(36) },
                    body = body.trim(),
                    category = category,
                    tags = tags.trim(),
                    attachmentUris = latest.attachmentUris
                ))
            } else {
                val fallbackTitle = body.lineSequence().firstOrNull { it.isNotBlank() }?.take(48) ?: "Untitled note"
                viewModel.addNote(
                    title = title.ifBlank { fallbackTitle },
                    body = body,
                    category = category,
                    tags = tags,
                    projectId = projectId,
                    onSaved = { onDismiss() }
                )
            }
            onDismiss()
        }
    }

    DialogWrapper(onDismiss = onDismiss, fullScreen = true, isDirty = { title.isNotBlank() || body.isNotBlank() || tags.isNotBlank() || attachments.isNotEmpty() }) {
        DialogHeader(FieldMindIcons.Note, "New Note", "Capture a quick idea, observation, or thought.", accent = FieldMindTheme.colors.source)

        if (projectId != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CuteCardDefaults.ButtonShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(FieldMindIcons.Project, null, tint = FieldMindTheme.colors.project, size = 18.dp)
                    Text("Linked to project", style = MaterialTheme.typography.labelMedium, color = FieldMindTheme.colors.project, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(4.dp))
        }

        // ── Title & Content ──
        DialogDividerSection("Title & Content", FieldMindIcons.Note, FieldMindTheme.colors.source)
        FieldTextField(title, { title = it }, "Title", supportingText = "Auto-filled from body if left blank")
        FieldTextField(body, { body = it }, "Content", minLines = 6, supportingText = "Start writing…")

        // ── Category ──
        DialogDividerSection("Classification", FieldMindIcons.Category, FieldMindTheme.colors.source)
        ChoiceChipsField("Category", observationCategories, category) { category = it }

        // ── Attachments ──
        DialogDividerSection("Attachments", FieldMindIcons.File, FieldMindTheme.colors.source)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { showCamera = true }, Modifier.weight(1f)) { Icon(FieldMindIcons.Camera, null, size = 18.dp); Spacer(Modifier.size(6.dp)); Text("Photo") }
            OutlinedButton(onClick = { haptics.light(); mediaPicker.launch("image/*") }, Modifier.weight(1f)) { Icon(FieldMindIcons.Gallery, null, size = 18.dp); Spacer(Modifier.size(6.dp)); Text("Gallery") }
            OutlinedButton(onClick = { haptics.light(); filePicker.launch(arrayOf("application/pdf", "text/*", "image/*", "video/*", "audio/*")) }, Modifier.weight(1f)) { Icon(FieldMindIcons.File, null, size = 18.dp); Spacer(Modifier.size(6.dp)); Text("File") }
        }
        AttachmentPreviewList(attachments, onCaptionChange = { index, caption -> attachments = attachments.mapIndexed { i, item -> if (i == index) item.copy(caption = caption) else item } }, onRemove = { remove -> attachments = attachments.filterIndexed { index, _ -> index != remove } })

        // ── Tags ──
        DialogDividerSection("Tags", FieldMindIcons.Tag, FieldMindTheme.colors.source)
        FieldTextField(tags, { tags = it }, "Tags", supportingText = "Comma-separated keywords")

        // ── Advanced ──
        CollapsibleSection("Advanced options", "Project linking & metadata", expanded = showAdvanced, onToggle = { showAdvanced = !showAdvanced }) {
            FieldTextField(location, { location = it }, "Location", supportingText = "Where was this note taken?")
        }

        DialogActions(onCancel = onDismiss, onSave = { save() }, saveEnabled = title.isNotBlank() || body.isNotBlank(), saveLabel = "Save note")
    }
    // In-app camera overlay
    if (showCamera) {
        Dialog(onDismissRequest = { showCamera = false }) {
            FieldMindCameraCapture(
                onPhotoCaptured = { uri, mimeType ->
                    attachments = attachments + DraftEvidenceAttachment("Photo", uri, "Note photo", mimeType = mimeType)
                    showCamera = false
                },
                onDismiss = { showCamera = false }
            )
        }
    }
}
@Composable
internal fun EditEntityDialog(kind: String, id: Long, viewModel: FieldMindViewModel, onDismiss: () -> Unit) {
    when (kind) {
        "observation" -> viewModel.observations.collectAsState().value.firstOrNull { it.id == id }?.let { NewObservationDialog(viewModel, onDismiss, entity = it) }
        "note" -> viewModel.notes.collectAsState().value.firstOrNull { it.id == id }?.let { NewNoteDialog(viewModel, onDismiss, entity = it) }
        "question" -> viewModel.questions.collectAsState().value.firstOrNull { it.id == id }?.let { NewQuestionDialog(viewModel, onDismiss, entity = it) }
        "hypothesis" -> {
            val questions = viewModel.questions.collectAsState().value
            viewModel.hypotheses.collectAsState().value.firstOrNull { it.id == id }?.let { NewHypothesisDialog(viewModel, questions, onDismiss, entity = it) }
        }
        "project" -> viewModel.projects.collectAsState().value.firstOrNull { it.id == id }?.let { NewProjectDialog(viewModel, onDismiss = onDismiss, entity = it) }
        "source" -> viewModel.sources.collectAsState().value.firstOrNull { it.id == id }?.let { NewSourceDialog(viewModel, onDismiss, entity = it) }
        "data" -> viewModel.dataRecords.collectAsState().value.firstOrNull { it.id == id }?.let { NewDataRecordDialog(viewModel, onDismiss, entity = it) }
        "report" -> viewModel.reports.collectAsState().value.firstOrNull { it.id == id }?.let { NewReportDialog(viewModel, onDismiss, entity = it) }
        "flashcard" -> viewModel.flashcards.collectAsState().value.firstOrNull { it.id == id }?.let { NewFlashcardDialog(viewModel, onDismiss, entity = it) }
        "task" -> viewModel.tasks.collectAsState().value.firstOrNull { it.id == id }?.let { EditTaskDialog(it, viewModel, onDismiss) }
        else -> onDismiss()
    }
}


@Composable
internal fun EditTaskDialog(entity: TaskEntity, viewModel: FieldMindViewModel, onDismiss: () -> Unit) {
    val taskTypes = listOf("Field Survey", "Observation Collection", "Species Count", "Audio Recording", "Photo Collection", "Video Collection", "Habitat Mapping", "Literature Review", "Data Analysis", "Report Writing", "Verification", "Sample Collection", "GPS Tracking", "Custom")
    val priorityLevels = listOf("Low", "Medium", "High")
    var title by remember { mutableStateOf(entity.title) }
    var description by remember { mutableStateOf(entity.description) }
    var taskType by remember { mutableStateOf(entity.taskType) }
    var priority by remember { mutableStateOf(entity.priority) }
    var dueDate by remember { mutableStateOf(entity.dueDate) }
    var dueTime by remember { mutableStateOf(entity.dueTime) }
    var assignedTo by remember { mutableStateOf(entity.assignedTo) }

    fun save() {
        if (title.isNotBlank()) {
            // Use the latest entity from ViewModel state to avoid stale closures
            val latestEntity = viewModel.tasks.value.firstOrNull { it.id == entity.id } ?: entity
            viewModel.updateTaskEntity(latestEntity.copy(
                title = title.trim(),
                description = description.trim(),
                taskType = taskType,
                priority = priority,
                dueDate = dueDate.trim(),
                dueTime = dueTime.trim(),
                assignedTo = assignedTo.trim()
            ))
            onDismiss()
        }
    }

    val original = entity
    DialogWrapper(onDismiss = onDismiss, fullScreen = true, isDirty = {
        title != original.title || description != original.description ||
        taskType != original.taskType || priority != original.priority ||
        dueDate != original.dueDate || assignedTo != original.assignedTo
    }) {
        DialogHeader(FieldMindIcons.List, "Edit Task", "Update title, type, priority, and schedule", accent = FieldMindTheme.colors.task)
        DialogDividerSection("Task details", FieldMindIcons.Edit, FieldMindTheme.colors.task)
        FieldTextField(title, { title = it }, "Title", supportingText = "Task name")
        FieldTextField(description, { description = it }, "Description", minLines = 2)
        OptionPickerField(label = "Type", selected = taskType, options = taskTypes, onSelected = { taskType = it }, icon = FieldMindIcons.Category)
        OptionPickerField(label = "Priority", selected = priority, options = priorityLevels, onSelected = { priority = it }, icon = FieldMindIcons.Streak)
        DialogDividerSection("Schedule & assignment", FieldMindIcons.Calendar, FieldMindTheme.colors.task)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FieldTextField(dueDate, { dueDate = it }, "Due date", modifier = Modifier.weight(1f), supportingText = "YYYY-MM-DD")
            FieldTextField(dueTime, { dueTime = it }, "Due time", modifier = Modifier.weight(1f), supportingText = "HH:MM")
        }
        FieldTextField(assignedTo, { assignedTo = it }, "Assigned to")
        DialogActions(onCancel = onDismiss, onSave = { save() }, saveEnabled = title.isNotBlank(), saveLabel = "Save changes")
    }
}

@Composable
internal fun EditNoteDialog(entity: NoteEntity, viewModel: FieldMindViewModel, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf(entity.title) }; var body by remember { mutableStateOf(entity.body) }; var category by remember { mutableStateOf(entity.category) }; var tags by remember { mutableStateOf(entity.tags) }; var attachments by remember { mutableStateOf(entity.attachmentUris) }

    fun save() {
        if (title.isNotBlank() || body.isNotBlank()) {
            viewModel.updateNoteEntity(entity.copy(
                title = title.trim().ifBlank { body.take(36) },
                body = body.trim(),
                category = category,
                tags = tags.trim(),
                attachmentUris = attachments.trim()
            ))
            onDismiss()
        }
    }

    DialogWrapper(onDismiss = onDismiss) {
        DialogHeader(FieldMindIcons.Note, "Edit Note", "Update title, content, and metadata", accent = FieldMindTheme.colors.source)
        OptionPickerField(label = "Category", selected = category, options = observationCategories, onSelected = { category = it }, icon = FieldMindIcons.Category)
        FieldTextField(title, { title = it }, "Title", supportingText = "Auto-filled from body if left blank")
        FieldTextField(body, { body = it }, "Body", minLines = 6)
        FieldTextField(tags, { tags = it }, "Tags", supportingText = "Comma-separated keywords")
        FieldTextField(attachments, { attachments = it }, "Attachments", minLines = 2, supportingText = "One per line: type|caption|uri")
        DialogActions(onCancel = onDismiss, onSave = { save() }, saveEnabled = title.isNotBlank() || body.isNotBlank(), saveLabel = "Save changes")
    }
}

@Composable
private fun EditObservationDialog(entity: ObservationEntity, viewModel: FieldMindViewModel, onDismiss: () -> Unit) {
    val appContext = LocalContext.current
    var subject by remember { mutableStateOf(entity.subject) }
    var category by remember { mutableStateOf(entity.category) }
    var facts by remember { mutableStateOf(entity.factsOnlyNotes) }
    var confidence by remember { mutableStateOf(entity.confidenceLevel) }
    var location by remember { mutableStateOf(entity.manualLocation) }
    var latitude by remember { mutableStateOf(entity.latitude?.toString().orEmpty()) }
    var longitude by remember { mutableStateOf(entity.longitude?.toString().orEmpty()) }
    var tags by remember { mutableStateOf(entity.tags) }
    var evidence by remember { mutableStateOf(entity.evidenceSummary) }
    var fieldContext by remember { mutableStateOf(entity.moodOrContext) }
    var attachments by remember { mutableStateOf<List<DraftEvidenceAttachment>>(emptyList()) }
    var showEditCamera by remember { mutableStateOf(false) }
    var locating by remember { mutableStateOf(false) }
    val locationProvider = remember { FieldLocationProvider(appContext) }

    fun startLocating() {
        locating = true
        locationProvider.requestCurrentLocation { captured ->
            locating = false
            if (captured != null) {
                latitude = captured.latitude.toString()
                longitude = captured.longitude.toString()
                location = captured.asDisplayText()
                locationProvider.resolvePlaceName(captured.latitude, captured.longitude) { place ->
                    if (!place.isNullOrBlank()) location = captured.copy(placeName = place).asDisplayText()
                }
            }
        }
    }

    fun save() {
        if (subject.isNotBlank()) {
            viewModel.updateObservation(entity.copy(
                subject = subject.trim(),
                category = category,
                factsOnlyNotes = facts.trim(),
                confidenceLevel = confidence,
                manualLocation = location.trim(),
                latitude = latitude.toDoubleOrNull(),
                longitude = longitude.toDoubleOrNull(),
                evidenceSummary = evidence.trim(),
                moodOrContext = fieldContext.trim()
            ), tags)
            attachments.forEach { viewModel.addAttachmentToObservation(entity.id, it) }
            onDismiss()
        }
    }

    val locationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (result.values.any { it }) startLocating() else {}
    }
    val mediaPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        attachments = attachments + uris.map { DraftEvidenceAttachment("Gallery", it.toString(), "Edited media") }
    }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            runCatching { appContext.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            attachments = attachments + DraftEvidenceAttachment("File", it.toString(), "Edited file / PDF")
        }
    }

    // ── Audio recording state ──
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var audioFile by remember { mutableStateOf<File?>(null) }
    var recording by remember { mutableStateOf(false) }
    var recordSeconds by remember { mutableIntStateOf(0) }

    val audioImportPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching { appContext.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            attachments = attachments + durableEvidenceAttachment(appContext, "Audio", uri, "Imported field audio")
        }
    }
    val audioPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val file = createFieldMindFile(appContext, "audio", ".m4a")
            val newRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(appContext) else MediaRecorder()
            runCatching {
                newRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
                newRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                newRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                newRecorder.setOutputFile(file.absolutePath)
                newRecorder.prepare()
                newRecorder.start()
                audioFile = file; recorder = newRecorder; recording = true
            }.onFailure { newRecorder.release() }
        }
    }

    LaunchedEffect(recording) { if (recording) { recordSeconds = 0; while (recording) { delay(1000); recordSeconds++ } } }

    val originalEntity = entity
    DialogWrapper(onDismiss = onDismiss, fullScreen = true, isDirty = {
        subject != originalEntity.subject || facts != originalEntity.factsOnlyNotes ||
        category != originalEntity.category || confidence != originalEntity.confidenceLevel ||
        location != originalEntity.manualLocation || tags != originalEntity.tags ||
        evidence != originalEntity.evidenceSummary || fieldContext != originalEntity.moodOrContext
    }) {
        DialogHeader(FieldMindIcons.Observation, "Edit Observation", "Update facts, location, evidence, and metadata", accent = FieldMindTheme.colors.observation)
        FieldTextField(subject, { subject = it }, "Subject")
        DialogDividerSection("Classification", FieldMindIcons.Category, FieldMindTheme.colors.observation)
        OptionPickerField(label = "Category", selected = category, options = observationCategories, onSelected = { category = it }, icon = FieldMindIcons.Category)
        OptionPickerField(label = "Confidence", selected = confidence, options = confidenceOptions, onSelected = { confidence = it }, icon = FieldMindIcons.Check)
        DialogDividerSection("Facts & context", FieldMindIcons.Edit, FieldMindTheme.colors.observation)
        FieldTextField(facts, { facts = it }, "Facts only", minLines = 3)
        MultiSelectPickerField(label = "Context presets", selected = if (fieldContext.isBlank()) emptySet() else fieldContext.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet(), options = contextPresets, onSelectionChanged = { fieldContext = it.joinToString(", ") }, subtitle = "Select field conditions", icon = FieldMindIcons.Info, showSearch = false)
        FieldTextField(fieldContext, { fieldContext = it }, "Context / mood", minLines = 2)
        DialogDividerSection("GPS & Location", FieldMindIcons.Location, FieldMindTheme.colors.observation)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilledTonalButton(
                onClick = { if (locationProvider.hasAnyLocationPermission()) startLocating() else locationPermission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) },
                modifier = Modifier.weight(1f),
                enabled = !locating
            ) {
                if (locating) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp) else Icon(FieldMindIcons.Location, null, size = 18.dp)
                Spacer(Modifier.size(6.dp)); Text(if (locating) "Locating…" else "Use GPS")
            }
            OutlinedButton(onClick = { latitude = ""; longitude = ""; location = "" }, modifier = Modifier.weight(1f)) { Text("Clear") }
        }
        FieldTextField(location, { location = it }, "Location")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FieldTextField(latitude, { latitude = it }, "Latitude", modifier = Modifier.weight(1f), keyboardType = KeyboardType.Decimal)
            FieldTextField(longitude, { longitude = it }, "Longitude", modifier = Modifier.weight(1f), keyboardType = KeyboardType.Decimal)
        }
        DialogDividerSection("Evidence & tags", FieldMindIcons.Camera, FieldMindTheme.colors.observation)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { showEditCamera = true }, Modifier.weight(1f)) { Icon(FieldMindIcons.Camera, null, size = 18.dp); Spacer(Modifier.size(6.dp)); Text("Photo") }
            OutlinedButton(onClick = { mediaPicker.launch("image/*") }, Modifier.weight(1f)) { Icon(FieldMindIcons.Gallery, null, size = 18.dp); Spacer(Modifier.size(6.dp)); Text("Gallery") }
            OutlinedButton(onClick = { filePicker.launch(arrayOf("application/pdf", "text/*", "image/*", "video/*", "audio/*")) }, Modifier.weight(1f)) { Icon(FieldMindIcons.File, null, size = 18.dp); Spacer(Modifier.size(6.dp)); Text("File") }
        }
        // ── Audio recording section ──
        Spacer(Modifier.height(6.dp))
        Card(shape = CuteCardDefaults.ShapeCompact, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(FieldMindIcons.Mic, null, tint = FieldMindTheme.colors.observation, size = 20.dp)
                    Text("Voice note", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    if (recording) {
                        Spacer(Modifier.weight(1f))
                        Text("${recordSeconds}s", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    }
                }
                Button(
                    onClick = {
                        if (recording) {
                            val file = audioFile
                            runCatching { recorder?.stop() }; recorder?.release(); recorder = null; recording = false
                            file?.let { attachments = attachments + DraftEvidenceAttachment("Audio", Uri.fromFile(it).toString(), "Voice note", localPath = it.absolutePath, mimeType = "audio/mp4") }
                        } else if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) audioPermission.launch(Manifest.permission.RECORD_AUDIO) else audioPermission.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = if (recording) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer) else ButtonDefaults.buttonColors()
                ) {
                    Icon(icon = if (recording) FieldMindIcons.Stop else FieldMindIcons.Mic, contentDescription = null, size = 18.dp)
                    Spacer(Modifier.size(6.dp))
                    Text(if (recording) "Stop recording" else "Record voice note")
                }
                OutlinedButton(onClick = { audioImportPicker.launch(arrayOf("audio/*")) }, Modifier.fillMaxWidth()) { Icon(FieldMindIcons.Mic, null, size = 18.dp); Spacer(Modifier.size(6.dp)); Text("Or import audio file") }
            }
        }
        AttachmentPreviewList(attachments, onCaptionChange = { index, caption -> attachments = attachments.mapIndexed { i, item -> if (i == index) item.copy(caption = caption) else item } }, onRemove = { remove -> attachments = attachments.filterIndexed { index, _ -> index != remove } })
        FieldTextField(tags, { tags = it }, "Tags", supportingText = "Comma-separated keywords")
        FieldTextField(evidence, { evidence = it }, "Evidence summary", minLines = 2)
        DialogActions(onCancel = onDismiss, onSave = { save() }, saveEnabled = subject.isNotBlank(), saveLabel = "Save changes")
    }
    // In-app camera overlay
    if (showEditCamera) {
        Dialog(onDismissRequest = { showEditCamera = false }) {
            FieldMindCameraCapture(
                onPhotoCaptured = { uri, mimeType ->
                    attachments = attachments + DraftEvidenceAttachment("Photo", uri, "Edited observation photo", mimeType = mimeType)
                    showEditCamera = false
                },
                onDismiss = { showEditCamera = false }
            )
        }
    }
}

@Composable
private fun EditQuestionDialog(entity: QuestionEntity, viewModel: FieldMindViewModel, onDismiss: () -> Unit) {
    var question by remember { mutableStateOf(entity.questionText) }; var category by remember { mutableStateOf(entity.category) }; var source by remember { mutableStateOf(entity.sourceType) }; var status by remember { mutableStateOf(entity.status) }; var priority by remember { mutableStateOf(entity.priority) }; var answer by remember { mutableStateOf(entity.answer) }
    var showAdvanced by remember { mutableStateOf(false) }

    fun save() {
        if (question.isNotBlank()) {
            viewModel.updateQuestionEntity(entity.copy(
                questionText = question.trim(),
                category = category,
                sourceType = source,
                status = status,
                priority = priority,
                answer = answer.trim(),
                answeredAt = if (answer.isBlank()) null else (entity.answeredAt ?: System.currentTimeMillis())
            ))
            onDismiss()
        }
    }

    DialogWrapper(onDismiss = onDismiss, fullScreen = true, isDirty = { question != entity.questionText || answer != entity.answer }) {
        DialogHeader(FieldMindIcons.Question, "Edit Question", "Update question text, classification, and answer")
        FieldTextField(question, { question = it }, "What do you want to find out?", minLines = 3, supportingText = "Example: Do bird visits increase after rain at this site?")
        DialogDividerSection("Classification", FieldMindIcons.Category)
        OptionPickerField(label = "Category", selected = category, options = observationCategories, onSelected = { category = it }, icon = FieldMindIcons.Category)
        OptionPickerField(label = "Source type", selected = source, options = sourceTypes, onSelected = { source = it }, icon = FieldMindIcons.Source)
        OptionPickerField(label = "Status", selected = status, options = questionStatuses, onSelected = { status = it }, icon = FieldMindIcons.Check)
        OptionPickerField(label = "Priority", selected = priority, options = listOf("Low", "Medium", "High"), onSelected = { priority = it }, icon = FieldMindIcons.Streak)
        CollapsibleSection("Advanced options", "Answer, cross-links, and metadata", expanded = showAdvanced, onToggle = { showAdvanced = !showAdvanced }) {
            FieldTextField(answer, { answer = it }, "Preliminary answer", minLines = 2, supportingText = "Optional — add if you already have a working answer")
        }
        DialogActions(onCancel = onDismiss, onSave = { save() }, saveEnabled = question.isNotBlank(), saveLabel = "Save changes")
    }
}

@Composable
private fun EditHypothesisDialog(entity: HypothesisEntity, viewModel: FieldMindViewModel, onDismiss: () -> Unit) {
    val questions by viewModel.questions.collectAsState()
    var prediction by remember { mutableStateOf(entity.prediction) }; var reasoning by remember { mutableStateOf(entity.reasoning) }; var evidence by remember { mutableStateOf(entity.evidenceNeeded) }; var support by remember { mutableStateOf(entity.supportCriteria) }; var weaken by remember { mutableStateOf(entity.weakeningCriteria) }; var test by remember { mutableStateOf(entity.testMethod) }; var result by remember { mutableStateOf(entity.resultStatus) }; var confidence by remember { mutableStateOf(entity.confidencePercent.toFloat()) }
    var linkedId by remember { mutableStateOf(entity.linkedQuestionId) }
    var showAdvanced by remember { mutableStateOf(false) }

    fun save() {
        if (prediction.isNotBlank()) {
            viewModel.updateHypothesisEntity(entity.copy(
                prediction = prediction.trim(),
                reasoning = reasoning.trim(),
                evidenceNeeded = evidence.trim(),
                supportCriteria = support.trim(),
                weakeningCriteria = weaken.trim(),
                testMethod = test.trim(),
                resultStatus = result,
                confidencePercent = confidence.toInt(),
                linkedQuestionId = linkedId
            ))
            onDismiss()
        }
    }

    DialogWrapper(onDismiss = onDismiss, fullScreen = true, isDirty = { prediction != entity.prediction || reasoning != entity.reasoning }) {
        DialogHeader(FieldMindIcons.Hypothesis, "Edit Hypothesis", "Update prediction, evidence rules, and confidence", accent = FieldMindTheme.colors.hypothesis)
        if (questions.isNotEmpty()) {
            OptionPickerField(label = "Linked question", selected = questions.firstOrNull { it.id == linkedId }?.questionText?.take(28) ?: "No question", options = listOf("No question") + questions.take(8).map { it.questionText.take(28) }, onSelected = { picked -> linkedId = questions.firstOrNull { it.questionText.startsWith(picked) }?.id }, icon = FieldMindIcons.Question)
        }
        FieldTextField(prediction, { prediction = it }, "Prediction", minLines = 3)
        FieldTextField(reasoning, { reasoning = it }, "Why this might happen", minLines = 2)
        DialogDividerSection("Evidence rules", FieldMindIcons.Done, FieldMindTheme.colors.hypothesis)
        Text("Decide success/failure before you bias yourself.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        FieldTextField(evidence, { evidence = it }, "Evidence needed", minLines = 2)
        FieldTextField(support, { support = it }, "Support criteria")
        FieldTextField(weaken, { weaken = it }, "Weakening criteria")
        FieldTextField(test, { test = it }, "Test method")
        DialogDividerSection("Confidence", FieldMindIcons.Streak, FieldMindTheme.colors.hypothesis)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Confidence", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${confidence.toInt()}%", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Slider(confidence, { confidence = it }, valueRange = 0f..100f)
        LinearProgressIndicator(progress = { confidence / 100f }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(8.dp)), color = MaterialTheme.colorScheme.primary)
        CollapsibleSection("Advanced options", "Result status tracking", expanded = showAdvanced, onToggle = { showAdvanced = !showAdvanced }) {
            OptionPickerField(label = "Result status", selected = result, options = listOf("Unknown", "Supported", "Weakened", "Inconclusive"), onSelected = { result = it }, icon = FieldMindIcons.Check)
        }
        DialogActions(onCancel = onDismiss, onSave = { save() }, saveEnabled = prediction.isNotBlank(), saveLabel = "Save changes")
    }
}

@Composable
private fun EditProjectDialog(entity: ProjectEntity, viewModel: FieldMindViewModel, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf(entity.title) }; var topic by remember { mutableStateOf(entity.topicType) }; var objective by remember { mutableStateOf(entity.objective) }; var question by remember { mutableStateOf(entity.researchQuestion) }
    var background by remember { mutableStateOf(entity.backgroundNotes) }; var methods by remember { mutableStateOf(entity.methods) }; var hypothesis by remember { mutableStateOf(entity.hypothesisSummary) }; var dataSummary by remember { mutableStateOf(entity.dataSummary) }; var analysis by remember { mutableStateOf(entity.analysis) }; var conclusion by remember { mutableStateOf(entity.conclusion) }; var future by remember { mutableStateOf(entity.futureQuestions) }
    var projectType by remember { mutableStateOf(entity.projectType ?: "Observation") }; var selectedMethods by remember { mutableStateOf(entity.selectedMethods ?: "") }; var connectionMap by remember { mutableStateOf(entity.connectionMap ?: "") }
    var showAdvanced by remember { mutableStateOf(false) }

    fun save() {
        if (title.isNotBlank()) {
            viewModel.updateProjectEntity(entity.copy(
                title = title.trim(),
                topicType = topic.trim().ifBlank { "General" },
                objective = objective.trim(),
                researchQuestion = question.trim(),
                backgroundNotes = background.trim(),
                methods = methods.trim(),
                hypothesisSummary = hypothesis.trim(),
                dataSummary = dataSummary.trim(),
                analysis = analysis.trim(),
                conclusion = conclusion.trim(),
                futureQuestions = future.trim(),
                projectType = projectType,
                selectedMethods = selectedMethods.trim(),
                connectionMap = connectionMap.trim()
            ))
            onDismiss()
        }
    }

    val originalProject = entity
    DialogWrapper(onDismiss = onDismiss, fullScreen = true, isDirty = {
        title != originalProject.title || objective != originalProject.objective ||
        question != originalProject.researchQuestion || background != originalProject.backgroundNotes ||
        methods != originalProject.methods || conclusion != originalProject.conclusion
    }) {
        DialogHeader(FieldMindIcons.Project, "Edit Project", "Update project details and research plan", accent = FieldMindTheme.colors.project)
        DialogDividerSection("Topic & title", FieldMindIcons.Category, FieldMindTheme.colors.project)
        FieldTextField(title, { title = it }, "Project title")
        OptionPickerField(label = "Topic", selected = topic, options = listOf("Biology", "Geology", "Wildlife", "Ecology", "Plant Study", "Weather", "Human Pattern", "Other"), onSelected = { topic = it }, icon = FieldMindIcons.Category)
        DialogDividerSection("Research purpose", FieldMindIcons.School, FieldMindTheme.colors.project)
        FieldTextField(objective, { objective = it }, "Objective", minLines = 2)
        FieldTextField(question, { question = it }, "Research question", minLines = 2)
        FieldTextField(background, { background = it }, "Background / context", minLines = 2)
        DialogDividerSection("Evidence plan", FieldMindIcons.Data, FieldMindTheme.colors.project)
        FieldTextField(methods, { methods = it }, "Method / data plan", minLines = 3)
        FieldTextField(hypothesis, { hypothesis = it }, "Hypothesis summary", minLines = 2)
        FieldTextField(dataSummary, { dataSummary = it }, "Data fields / units", supportingText = "Example: temperature °C, height cm, water clarity, count")
        DialogDividerSection("Report direction", FieldMindIcons.Report, FieldMindTheme.colors.project)
        FieldTextField(analysis, { analysis = it }, "Analysis plan", minLines = 2)
        FieldTextField(conclusion, { conclusion = it }, "Early conclusion / expected output", minLines = 2)
        CollapsibleSection("Advanced options", "Project type, methods, and connection map", expanded = showAdvanced, onToggle = { showAdvanced = !showAdvanced }) {
            OptionPickerField(label = "Project type", selected = projectType, options = (listOf("Observation", "Experiment", "Literature Review", "Field Survey", "Long-term Monitor") + researchProjectTypes).distinct(), onSelected = { projectType = it }, icon = FieldMindIcons.Category)
            FieldTextField(selectedMethods, { selectedMethods = it }, "Selected methods", supportingText = "e.g. transect, quadrat, interview, water test")
            FieldTextField(connectionMap, { connectionMap = it }, "Connection map", minLines = 2, supportingText = "How this project relates to other observations or projects")
        }
        DialogActions(onCancel = onDismiss, onSave = { save() }, saveEnabled = title.isNotBlank(), saveLabel = "Save changes")
    }
}
@Composable
private fun EditSourceDialog(entity: SourceEntity, viewModel: FieldMindViewModel, onDismiss: () -> Unit) {
    val appContext = LocalContext.current
    val projects by viewModel.projects.collectAsState()
    val haptics = rememberFieldMindHaptics()
    var type by remember { mutableStateOf(entity.type) }
    var title by remember { mutableStateOf(entity.title) }
    var author by remember { mutableStateOf(entity.author) }
    var dateOrYear by remember { mutableStateOf(entity.dateOrYear) }
    var doiOrIsbn by remember { mutableStateOf(entity.doiOrIsbn) }
    var publisherOrJournal by remember { mutableStateOf(entity.publisherOrJournal) }
    var accessDate by remember { mutableStateOf(entity.accessDate) }
    var link by remember { mutableStateOf(entity.link) }
    var fileUri by remember { mutableStateOf(entity.fileUri) }
    var citationStyleNote by remember { mutableStateOf(entity.citationStyleNote) }
    var importance by remember { mutableStateOf(entity.importance) }
    var readingStatus by remember { mutableStateOf(entity.readingStatus) }
    var projectId by remember { mutableStateOf(entity.relatedProjectId) }
    var summary by remember { mutableStateOf(entity.personalSummary) }
    var findings by remember { mutableStateOf(entity.keyFindings) }
    var taught by remember { mutableStateOf(entity.whatThisSourceTaughtMe) }
    var questions by remember { mutableStateOf(entity.questionsGenerated) }
    var notes by remember { mutableStateOf(entity.paperNotes) }
    var reliability by remember { mutableStateOf(entity.reliabilityScore.toFloat()) }

    val docPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching { appContext.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            fileUri = uri.toString()
            if (type !in listOf("PDF", "Image")) type = "Local document"
            haptics.light()
        }
    }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            fileUri = uri.toString()
            type = "Image"
            haptics.light()
        }
    }

    fun save() {
        if (title.isNotBlank()) {
            viewModel.updateSourceEntity(
                entity.copy(
                    type = type,
                    title = title.trim(),
                    author = author.trim(),
                    dateOrYear = dateOrYear.trim(),
                    link = link.trim(),
                    doiOrIsbn = doiOrIsbn.trim(),
                    publisherOrJournal = publisherOrJournal.trim(),
                    accessDate = accessDate.trim(),
                    fileUri = fileUri.trim(),
                    citationStyleNote = citationStyleNote.trim(),
                    importance = importance,
                    personalSummary = summary.trim(),
                    keyFindings = findings.trim(),
                    whatThisSourceTaughtMe = taught.trim(),
                    questionsGenerated = questions.trim(),
                    paperNotes = notes.trim(),
                    reliabilityScore = reliability.toInt(),
                    readingStatus = readingStatus,
                    relatedProjectId = projectId
                )
            )
            onDismiss()
        }
    }

    val originalSource = entity
    DialogWrapper(onDismiss = onDismiss, fullScreen = true, isDirty = {
        title != originalSource.title || author != originalSource.author ||
        summary != originalSource.personalSummary || findings != originalSource.keyFindings ||
        notes != originalSource.paperNotes || link != originalSource.link
    }) {
        DialogHeader(FieldMindIcons.Source, "Edit Source", "Update source identity, notes, and status", accent = FieldMindTheme.colors.source)
        DialogDividerSection("Source type", FieldMindIcons.Category, FieldMindTheme.colors.source)
        OptionPickerField(label = "Type", selected = type, options = sourceLibraryTypes, onSelected = { type = it }, icon = FieldMindIcons.Category)
        DialogDividerSection("Identity", FieldMindIcons.Article, FieldMindTheme.colors.source)
        FieldTextField(title, { title = it }, "Title")
        FieldTextField(author, { author = it }, "Author / creator")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FieldTextField(dateOrYear, { dateOrYear = it }, "Date / year", modifier = Modifier.weight(1f))
            FieldTextField(accessDate, { accessDate = it }, "Accessed", modifier = Modifier.weight(1f))
        }
        FieldTextField(doiOrIsbn, { doiOrIsbn = it }, "DOI / ISBN")
        FieldTextField(publisherOrJournal, { publisherOrJournal = it }, "Publisher / journal")
        DialogDividerSection("Link and file", FieldMindIcons.Link, FieldMindTheme.colors.source)
        FieldTextField(link, { link = it }, "Web link")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { haptics.light(); docPicker.launch(arrayOf("application/pdf", "text/*", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "image/*")) }, modifier = Modifier.weight(1f)) {
                Icon(FieldMindIcons.File, null, size = 18.dp); Spacer(Modifier.size(6.dp)); Text("Document")
            }
            OutlinedButton(onClick = { haptics.light(); imagePicker.launch("image/*") }, modifier = Modifier.weight(1f)) {
                Icon(FieldMindIcons.Gallery, null, size = 18.dp); Spacer(Modifier.size(6.dp)); Text("Image")
            }
        }
        if (fileUri.isNotBlank()) FieldTextField(fileUri, { fileUri = it }, "Attached file URI")
        SourcePreviewCard(link = link, fileUri = fileUri)
        DialogDividerSection("Reading notes", FieldMindIcons.Edit, FieldMindTheme.colors.source)
        FieldTextField(summary, { summary = it }, "Main idea", minLines = 2)
        FieldTextField(findings, { findings = it }, "Key findings", minLines = 2)
        FieldTextField(taught, { taught = it }, "What this taught me", minLines = 2)
        FieldTextField(questions, { questions = it }, "New questions", minLines = 2)
        FieldTextField(notes, { notes = it }, "Paper / Cornell notes", minLines = 3)
        FieldTextField(citationStyleNote, { citationStyleNote = it }, "Citation style note")
        DialogDividerSection("Status", FieldMindIcons.Check, FieldMindTheme.colors.source)
        OptionPickerField(label = "Reading status", selected = readingStatus, options = readingStatuses, onSelected = { readingStatus = it }, icon = FieldMindIcons.Check)
        OptionPickerField(label = "Importance", selected = importance, options = sourceImportanceLevels, onSelected = { importance = it }, icon = FieldMindIcons.Streak)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Credibility", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${reliability.toInt()}/5", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Slider(reliability, { reliability = it }, valueRange = 1f..5f, steps = 3)
        if (projects.isNotEmpty()) {
            OptionPickerField(label = "Link to project", selected = projects.firstOrNull { it.id == projectId }?.title ?: "No project", options = listOf("No project") + projects.map { it.title }, onSelected = { selected -> projectId = projects.firstOrNull { it.title == selected }?.id }, icon = FieldMindIcons.Project)
        }
        DialogActions(onCancel = onDismiss, onSave = { save() }, saveEnabled = title.isNotBlank(), saveLabel = "Save changes")
    }
}
// ══════════════════════════════════════════════════════════════════════
//  Tool-Specific Edit Dialogs (UX #22)
// ══════════════════════════════════════════════════════════════════════

@Composable
private fun EditCounterDialog(entity: DataRecordEntity, viewModel: FieldMindViewModel, onDismiss: () -> Unit) {
    var label by remember { mutableStateOf(entity.label) }; var value by remember { mutableStateOf(entity.value) }; var unit by remember { mutableStateOf(entity.unit.ifBlank { "count" }) }; var notes by remember { mutableStateOf(entity.notes) }; var location by remember { mutableStateOf(entity.location) }

    fun save() {
        if (label.isNotBlank()) {
            viewModel.updateDataRecordEntity(entity.copy(label = label.trim(), value = value.trim(), unit = unit.trim(), location = location.trim(), notes = notes.trim()))
            onDismiss()
        }
    }

    DialogWrapper(onDismiss = onDismiss) {
        DialogHeader(FieldMindIcons.Tag, "Edit Counter", "Adjust count, label, and notes", accent = FieldMindTheme.colors.data)
        DialogDividerSection("Label & count", FieldMindIcons.Tag, FieldMindTheme.colors.data)
        FieldTextField(label, { label = it }, "Label", supportingText = "What is being counted?")
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton({ value = ((value.toIntOrNull() ?: 0) - 10).coerceAtLeast(0).toString() }) { Text("-10") }
            OutlinedButton({ value = ((value.toIntOrNull() ?: 0) - 1).coerceAtLeast(0).toString() }) { Text("-1") }
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.width(64.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Button({ value = ((value.toIntOrNull() ?: 0) + 1).toString() }) { Text("+1") }
            Button({ value = ((value.toIntOrNull() ?: 0) + 10).toString() }) { Text("+10") }
            TextButton({ value = "0" }) { Text("Reset") }
        }
        DialogDividerSection("Details", FieldMindIcons.Settings, FieldMindTheme.colors.data)
        FieldTextField(unit, { unit = it }, "Unit")
        FieldTextField(location, { location = it }, "Location / site")
        DialogDividerSection("Notes", FieldMindIcons.Note, FieldMindTheme.colors.data)
        FieldTextField(notes, { notes = it }, "Notes", minLines = 2)
        DialogActions(onCancel = onDismiss, onSave = { save() }, saveEnabled = label.isNotBlank(), saveLabel = "Save changes")
    }
}

@Composable
private fun EditMeasurementDialog(entity: DataRecordEntity, viewModel: FieldMindViewModel, onDismiss: () -> Unit) {
    var label by remember { mutableStateOf(entity.label) }; var value by remember { mutableStateOf(entity.value) }; var unit by remember { mutableStateOf(entity.unit.ifBlank { "cm" }) }; var location by remember { mutableStateOf(entity.location) }; var notes by remember { mutableStateOf(entity.notes) }

    fun save() {
        if (label.isNotBlank()) {
            viewModel.updateDataRecordEntity(entity.copy(label = label.trim(), value = value.trim(), unit = unit.trim(), location = location.trim(), notes = notes.trim()))
            onDismiss()
        }
    }

    DialogWrapper(onDismiss = onDismiss) {
        DialogHeader(FieldMindIcons.Line, "Edit Measurement", "Adjust value, unit, location, and notes", accent = FieldMindTheme.colors.data)
        DialogDividerSection("What was measured", FieldMindIcons.Tag, FieldMindTheme.colors.data)
        FieldTextField(label, { label = it }, "Label", supportingText = "e.g. Leaf length, Water depth, Wind speed")
        DialogDividerSection("Measurement", FieldMindIcons.Line, FieldMindTheme.colors.data)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FieldTextField(value, { value = it }, "Value", modifier = Modifier.weight(2f), keyboardType = KeyboardType.Decimal)
            FieldTextField(unit, { unit = it }, "Unit", modifier = Modifier.weight(1f), supportingText = "cm, m, °C, km/h…")
        }
        DialogDividerSection("Where", FieldMindIcons.Location, FieldMindTheme.colors.data)
        FieldTextField(location, { location = it }, "Location / site")
        DialogDividerSection("Notes", FieldMindIcons.Note, FieldMindTheme.colors.data)
        MultiSelectPickerField(label = "Context presets", selected = if (notes.isBlank()) emptySet() else notes.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet(), options = contextPresets, onSelectionChanged = { notes = it.joinToString(", ") }, subtitle = "Select field conditions", icon = FieldMindIcons.Info, showSearch = false)
        FieldTextField(notes, { notes = it }, "Method & calibration notes", minLines = 2, supportingText = "Instrument, calibration, observer")
        DialogActions(onCancel = onDismiss, onSave = { save() }, saveEnabled = label.isNotBlank(), saveLabel = "Save changes")
    }
}

@Composable
private fun EditWeatherLogDialog(entity: DataRecordEntity, viewModel: FieldMindViewModel, onDismiss: () -> Unit) {
    var label by remember { mutableStateOf(entity.label) }; var value by remember { mutableStateOf(entity.value) }; var unit by remember { mutableStateOf(entity.unit.ifBlank { "°C" }) }; var location by remember { mutableStateOf(entity.location) }; var notes by remember { mutableStateOf(entity.notes) }
    val weatherMeasurements = listOf("Air temperature", "Humidity", "Wind speed", "Wind direction", "Cloud cover", "Pressure", "Precipitation", "Visibility", "UV index")
    val weatherUnits = mapOf("Air temperature" to "°C", "Humidity" to "%", "Wind speed" to "km/h", "Wind direction" to "°", "Cloud cover" to "%", "Pressure" to "hPa", "Precipitation" to "mm", "Visibility" to "km", "UV index" to "index")

    fun save() {
        if (label.isNotBlank()) {
            viewModel.updateDataRecordEntity(entity.copy(label = label.trim(), value = value.trim(), unit = unit.trim(), location = location.trim(), notes = notes.trim()))
            onDismiss()
        }
    }

    DialogWrapper(onDismiss = onDismiss) {
        DialogHeader(FieldMindIcons.Weather, "Edit Weather Log", "Adjust measurement, conditions, and location", accent = FieldMindTheme.colors.data)
        DialogDividerSection("Weather measurement", FieldMindIcons.Weather, FieldMindTheme.colors.data)
        OptionPickerField(label = "Measurement", selected = label, options = weatherMeasurements, onSelected = { lbl -> label = lbl; unit = weatherUnits[lbl] ?: unit }, icon = FieldMindIcons.Category)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FieldTextField(value, { value = it }, "Value", modifier = Modifier.weight(2f), keyboardType = KeyboardType.Decimal)
            FieldTextField(unit, { unit = it }, "Unit", modifier = Modifier.weight(1f))
        }
        DialogDividerSection("Where & when", FieldMindIcons.Location, FieldMindTheme.colors.data)
        FieldTextField(location, { location = it }, "Location / site")
        DialogDividerSection("Conditions", FieldMindIcons.Note, FieldMindTheme.colors.data)
        MultiSelectPickerField(label = "Sky conditions", selected = if (notes.isBlank()) emptySet() else notes.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet(), options = contextPresets, onSelectionChanged = { notes = it.joinToString(", ") }, subtitle = "Select weather conditions", icon = FieldMindIcons.Info, showSearch = false)
        FieldTextField(notes, { notes = it }, "Sky, wind, precipitation, pressure notes", minLines = 3)
        DialogActions(onCancel = onDismiss, onSave = { save() }, saveEnabled = label.isNotBlank(), saveLabel = "Save changes")
    }
}

@Composable
private fun EditChecklistDialog(entity: DataRecordEntity, viewModel: FieldMindViewModel, onDismiss: () -> Unit) {
    var label by remember { mutableStateOf(entity.label) }; var notes by remember { mutableStateOf(entity.notes) }; var location by remember { mutableStateOf(entity.location) }
    var items by remember {
        mutableStateOf(run {
            val raw = entity.value
            if (raw.startsWith("[")) {
                try {
                    val arr = org.json.JSONArray(raw)
                    (0 until arr.length()).map { i ->
                        val obj = arr.getJSONObject(i)
                        Pair(obj.optString("item", ""), obj.optBoolean("done", false))
                    }
                } catch (_: Exception) { listOf(Pair(raw, false)) }
            } else if (raw.isNotBlank()) {
                raw.split(",").map { Pair(it.trim(), false) }
            } else emptyList()
        })
    }

    fun save() {
        if (label.isNotBlank()) {
            val json = org.json.JSONArray().apply {
                items.forEach { (item, done) -> put(org.json.JSONObject().apply { put("item", item); put("done", done) }) }
            }.toString()
            val done = items.count { it.second }
            val total = items.size
            viewModel.updateDataRecordEntity(entity.copy(
                label = label.trim(),
                value = json,
                unit = "$done/$total",
                location = location.trim(),
                notes = notes.trim()
            ))
            onDismiss()
        }
    }

    DialogWrapper(onDismiss = onDismiss) {
        DialogHeader(FieldMindIcons.Check, "Edit Checklist", "Manage checklist items and progress", accent = FieldMindTheme.colors.data)
        DialogDividerSection("Title", FieldMindIcons.Tag, FieldMindTheme.colors.data)
        FieldTextField(label, { label = it }, "Checklist title")
        DialogDividerSection("Items", FieldMindIcons.List, FieldMindTheme.colors.data)
        items.forEachIndexed { index, (item, done) ->
            Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val cb_bounce = remember { Animatable(1f) }
                val cb_scope = rememberCoroutineScope()
                Surface(
                    onClick = {
                        val wasDone = items[index].second; items = items.toMutableList().apply { this[index] = Pair(item, !wasDone) }
                        cb_scope.launch {
                            cb_bounce.snapTo(1.3f)
                            cb_bounce.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 350f))
                        }
                    },
                    shape = CircleShape,
                    color = if (done) FieldMindTheme.colors.positive.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.size(28.dp).graphicsLayer {
                        scaleX = cb_bounce.value
                        scaleY = cb_bounce.value
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (done) {
                            Icon(MaterialSymbolIcon("check_circle", filled = true), null, size = 18.dp, tint = FieldMindTheme.colors.positive)
                        } else {
                            Box(Modifier.size(16.dp).clip(CircleShape).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)))
                        }
                    }
                }
                TextField(
                    value = item,
                    onValueChange = { items = items.toMutableList().apply { this[index] = Pair(it, done) } },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    colors = OutlinedTextFieldDefaults.colors()
                )
                IconButton(onClick = { items = items.toMutableList().apply { removeAt(index) } }) {
                    Icon(FieldMindIcons.Close, "Remove", tint = MaterialTheme.colorScheme.error, size = 20.dp)
                }
            }
        }
        TextButton(onClick = { items = items + Pair("", false) }) {
            Icon(FieldMindIcons.Add, null, size = 18.dp); Spacer(Modifier.size(4.dp)); Text("Add item")
        }
        DialogDividerSection("Details", FieldMindIcons.Settings, FieldMindTheme.colors.data)
        FieldTextField(location, { location = it }, "Location / site")
        FieldTextField(notes, { notes = it }, "Notes", minLines = 2)
        DialogActions(onCancel = onDismiss, onSave = { save() }, saveEnabled = label.isNotBlank(), saveLabel = "Save changes")
    }
}

@Composable
private fun EditEventLogDialog(entity: DataRecordEntity, viewModel: FieldMindViewModel, onDismiss: () -> Unit) {
    var label by remember { mutableStateOf(entity.label) }; var value by remember { mutableStateOf(entity.value) }; var location by remember { mutableStateOf(entity.location) }; var notes by remember { mutableStateOf(entity.notes) }
    val eventCategories = listOf("Sighting", "Behavior", "Weather event", "Geological", "Human activity", "Mark/recapture", "Life cycle", "Disturbance", "Other")

    fun save() {
        if (label.isNotBlank()) {
            viewModel.updateDataRecordEntity(entity.copy(label = label.trim(), value = value.trim(), unit = "event", location = location.trim(), notes = notes.trim()))
            onDismiss()
        }
    }

    DialogWrapper(onDismiss = onDismiss) {
        DialogHeader(FieldMindIcons.Flag, "Edit Event Log", "Update event type, details, and location", accent = FieldMindTheme.colors.data)
        DialogDividerSection("Event", FieldMindIcons.Tag, FieldMindTheme.colors.data)
        OptionPickerField(label = "Category", selected = label, options = eventCategories, onSelected = { label = it }, icon = FieldMindIcons.Category)
        FieldTextField(value, { value = it }, "Event detail / description", minLines = 2)
        DialogDividerSection("Where", FieldMindIcons.Location, FieldMindTheme.colors.data)
        FieldTextField(location, { location = it }, "Location / site")
        DialogDividerSection("Notes", FieldMindIcons.Note, FieldMindTheme.colors.data)
        MultiSelectPickerField(label = "Context presets", selected = if (notes.isBlank()) emptySet() else notes.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet(), options = contextPresets, onSelectionChanged = { notes = it.joinToString(", ") }, subtitle = "Select field conditions", icon = FieldMindIcons.Info, showSearch = false)
        FieldTextField(notes, { notes = it }, "Notes", minLines = 3)
        DialogActions(onCancel = onDismiss, onSave = { save() }, saveEnabled = label.isNotBlank(), saveLabel = "Save changes")
    }
}

@Composable
private fun EditSiteLogDialog(entity: DataRecordEntity, viewModel: FieldMindViewModel, onDismiss: () -> Unit) {
    var label by remember { mutableStateOf(entity.label) }; var value by remember { mutableStateOf(entity.value) }; var location by remember { mutableStateOf(entity.location) }; var notes by remember { mutableStateOf(entity.notes) }

    fun save() {
        if (label.isNotBlank()) {
            viewModel.updateDataRecordEntity(entity.copy(label = label.trim(), value = value.trim(), unit = "site", location = location.trim(), notes = notes.trim()))
            onDismiss()
        }
    }

    DialogWrapper(onDismiss = onDismiss) {
        DialogHeader(FieldMindIcons.Location, "Edit Site Log", "Update site name, conditions, and notes", accent = FieldMindTheme.colors.data)
        DialogDividerSection("Site identity", FieldMindIcons.Tag, FieldMindTheme.colors.data)
        FieldTextField(label, { label = it }, "Site name / code")
        DialogDividerSection("Purpose", FieldMindIcons.School, FieldMindTheme.colors.data)
        FieldTextField(value, { value = it }, "Visit purpose / objective", minLines = 2)
        DialogDividerSection("Coordinates", FieldMindIcons.Location, FieldMindTheme.colors.data)
        FieldTextField(location, { location = it }, "Location / GPS")
        DialogDividerSection("Conditions & findings", FieldMindIcons.Note, FieldMindTheme.colors.data)
        MultiSelectPickerField(label = "Context presets", selected = if (notes.isBlank()) emptySet() else notes.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet(), options = contextPresets, onSelectionChanged = { notes = it.joinToString(", ") }, subtitle = "Select habitat conditions", icon = FieldMindIcons.Info, showSearch = false)
        FieldTextField(notes, { notes = it }, "Habitat, substrate, disturbance, access notes", minLines = 3)
        DialogActions(onCancel = onDismiss, onSave = { save() }, saveEnabled = label.isNotBlank(), saveLabel = "Save changes")
    }
}

@Composable
private fun EditComparisonDialog(entity: DataRecordEntity, viewModel: FieldMindViewModel, onDismiss: () -> Unit) {
    var label by remember { mutableStateOf(entity.label) }; var value by remember { mutableStateOf(entity.value) }; var unit by remember { mutableStateOf(entity.unit.ifBlank { "score" }) }; var notes by remember { mutableStateOf(entity.notes) }; var location by remember { mutableStateOf(entity.location) }
    var rows by remember {
        mutableStateOf(run {
            val raw = entity.value
            if (raw.startsWith("[")) {
                try {
                    val arr = org.json.JSONArray(raw)
                    (0 until arr.length()).map { i ->
                        val obj = arr.getJSONObject(i)
                        val key = obj.keys().next(); Pair(key, obj.optString(key, ""))
                    }
                } catch (_: Exception) { listOf(Pair("Variable", raw)) }
            } else {
                raw.lines().filter { it.isNotBlank() }.map { line ->
                    val parts = line.split(":", limit = 2)
                    Pair(parts.getOrElse(0) { "" }.trim(), parts.getOrElse(1) { "" }.trim())
                }.ifEmpty { listOf(Pair("Variable", "")) }
            }
        })
    }

    fun save() {
        if (label.isNotBlank()) {
            val json = org.json.JSONArray().apply {
                rows.forEach { (variable, score) -> put(org.json.JSONObject().apply { put(variable, score) }) }
            }.toString()
            viewModel.updateDataRecordEntity(entity.copy(
                label = label.trim(),
                value = json,
                unit = unit.trim(),
                location = location.trim(),
                notes = notes.trim()
            ))
            onDismiss()
        }
    }

    DialogWrapper(onDismiss = onDismiss) {
        DialogHeader(FieldMindIcons.Sort, "Edit Comparison", "Update comparison variables and scores", accent = FieldMindTheme.colors.data)
        DialogDividerSection("Title", FieldMindIcons.Tag, FieldMindTheme.colors.data)
        FieldTextField(label, { label = it }, "Comparison title")
        DialogDividerSection("Variables", FieldMindIcons.List, FieldMindTheme.colors.data)
        rows.forEachIndexed { index, (variable, score) ->
            Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = variable,
                    onValueChange = { rows = rows.toMutableList().apply { this[index] = Pair(it, score) } },
                    modifier = Modifier.weight(2f),
                    singleLine = true,
                    placeholder = { Text("Variable") },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    colors = OutlinedTextFieldDefaults.colors()
                )
                TextField(
                    value = score,
                    onValueChange = { rows = rows.toMutableList().apply { this[index] = Pair(variable, it) } },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    placeholder = { Text("Score") },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    colors = OutlinedTextFieldDefaults.colors()
                )
                IconButton(onClick = { rows = rows.toMutableList().apply { removeAt(index) } }) {
                    Icon(FieldMindIcons.Close, "Remove", tint = MaterialTheme.colorScheme.error, size = 20.dp)
                }
            }
        }
        TextButton(onClick = { rows = rows + Pair("", "") }) {
            Icon(FieldMindIcons.Add, null, size = 18.dp); Spacer(Modifier.size(4.dp)); Text("Add variable")
        }
        DialogDividerSection("Details", FieldMindIcons.Settings, FieldMindTheme.colors.data)
        FieldTextField(unit, { unit = it }, "Score unit", supportingText = "e.g. score, rank, yes/no")
        FieldTextField(location, { location = it }, "Location / site")
        FieldTextField(notes, { notes = it }, "Notes", minLines = 2)
        DialogActions(onCancel = onDismiss, onSave = { save() }, saveEnabled = label.isNotBlank(), saveLabel = "Save changes")
    }
}

@Composable
private fun EditDataRecordDialog(entity: DataRecordEntity, viewModel: FieldMindViewModel, onDismiss: () -> Unit) {
    var tool by remember { mutableStateOf(entity.toolType) }; var label by remember { mutableStateOf(entity.label) }; var value by remember { mutableStateOf(entity.value) }; var unit by remember { mutableStateOf(entity.unit ?: defaultUnitForTool(entity.toolType)) }; var location by remember { mutableStateOf(entity.location) }; var notes by remember { mutableStateOf(entity.notes) }

    fun save() {
        if (label.isNotBlank()) {
            viewModel.updateDataRecordEntity(entity.copy(
                toolType = tool,
                label = label.trim(),
                value = value.trim(),
                unit = unit.trim(),
                location = location.trim(),
                notes = notes.trim()
            ))
            onDismiss()
        }
    }

    DialogWrapper(onDismiss = onDismiss) {
        DialogHeader(FieldMindIcons.Data, "Edit Data Record", "Update tool type, value, and notes", accent = FieldMindTheme.colors.data)
        DialogDividerSection("Preset", FieldMindIcons.Settings, FieldMindTheme.colors.data)
        OptionPickerField(label = "Tool", selected = tool, options = dataTools, onSelected = { tool = it; unit = defaultUnitForTool(it); label = defaultLabelForTool(it) }, icon = FieldMindIcons.Settings)
        FieldTextField(label, { label = it }, "Label")
        if (tool == "Counter" || tool == "Species Tracker") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton({ value = ((value.toIntOrNull() ?: 0) - 1).toString() }) { Text("−1") }
                Text(value, style = MaterialTheme.typography.headlineSmall)
                Button({ value = ((value.toIntOrNull() ?: 0) + 1).toString() }) { Text("+1") }
                TextButton({ value = "0" }) { Text("Reset") }
            }
        }
        DialogDividerSection("Measurement", FieldMindIcons.Line, FieldMindTheme.colors.data)
        FieldTextField(value, { value = it }, "Value / items / samples", keyboardType = KeyboardType.Number)
        FieldTextField(unit, { unit = it }, "Unit", supportingText = "Suggested for $tool: ${defaultUnitForTool(tool)}")
        FieldTextField(location, { location = it }, "Location / site")
        DialogDividerSection("Context", FieldMindIcons.Note, FieldMindTheme.colors.data)
        MultiSelectPickerField(label = "Context presets", selected = if (notes.isBlank()) emptySet() else notes.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet(), options = contextPresets, onSelectionChanged = { notes = it.joinToString(", ") }, subtitle = "Select field conditions", icon = FieldMindIcons.Info, showSearch = false)
        FieldTextField(notes, { notes = it }, "Notes", minLines = 3)
        DialogActions(onCancel = onDismiss, onSave = { save() }, saveEnabled = label.isNotBlank(), saveLabel = "Save changes")
    }
}
@Composable
private fun EditReportDialog(entity: ReportEntity, viewModel: FieldMindViewModel, onDismiss: () -> Unit) {
    var type by remember { mutableStateOf(entity.type) }; var title by remember { mutableStateOf(entity.title) }; var background by remember { mutableStateOf(entity.background) }; var question by remember { mutableStateOf(entity.question) }; var methods by remember { mutableStateOf(entity.methods) }; var observations by remember { mutableStateOf(entity.observations) }; var results by remember { mutableStateOf(entity.results) }; var interpretation by remember { mutableStateOf(entity.interpretation) }; var conclusion by remember { mutableStateOf(entity.conclusion) }; var limitations by remember { mutableStateOf(entity.limitations) }; var next by remember { mutableStateOf(entity.nextSteps) }

    fun save() {
        if (title.isNotBlank()) {
            viewModel.updateReportEntity(entity.copy(
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
            onDismiss()
        }
    }

    val originalReport = entity
    DialogWrapper(onDismiss = onDismiss, fullScreen = true, isDirty = {
        title != originalReport.title || question != originalReport.question ||
        methods != originalReport.methods || results != originalReport.results ||
        conclusion != originalReport.conclusion || background != originalReport.background
    }) {
        DialogHeader(FieldMindIcons.Report, "Edit Report", "Update report content and findings", accent = FieldMindTheme.colors.report)
        DialogDividerSection("Type & title", FieldMindIcons.Category, FieldMindTheme.colors.report)
        OptionPickerField(label = "Report type", selected = type, options = reportTypes, onSelected = { type = it }, icon = FieldMindIcons.Category)
        FieldTextField(title, { title = it }, "Title")
        DialogDividerSection("Setup", FieldMindIcons.School, FieldMindTheme.colors.report)
        FieldTextField(background, { background = it }, "Background", minLines = 2)
        FieldTextField(question, { question = it }, "Question", minLines = 2)
        FieldTextField(methods, { methods = it }, "Methods", minLines = 2)
        DialogDividerSection("Evidence", FieldMindIcons.Data, FieldMindTheme.colors.report)
        FieldTextField(observations, { observations = it }, "Observations", minLines = 2)
        FieldTextField(results, { results = it }, "Data / results", minLines = 2)
        FieldTextField(interpretation, { interpretation = it }, "Interpretation", minLines = 2)
        DialogDividerSection("Conclusion", FieldMindIcons.Check, FieldMindTheme.colors.report)
        FieldTextField(conclusion, { conclusion = it }, "Conclusion", minLines = 2)
        FieldTextField(limitations, { limitations = it }, "Limitations", minLines = 2)
        FieldTextField(next, { next = it }, "Next steps", minLines = 2)
        DialogActions(onCancel = onDismiss, onSave = { save() }, saveEnabled = title.isNotBlank(), saveLabel = "Save changes")
    }
}
@Composable
private fun EditFlashcardDialog(entity: FlashcardEntity, viewModel: FieldMindViewModel, onDismiss: () -> Unit) {
    var type by remember { mutableStateOf(entity.type) }; var front by remember { mutableStateOf(entity.front) }; var back by remember { mutableStateOf(entity.back) }; var useSm2 by remember { mutableStateOf(entity.deckMode == "sm2") }

    fun save() {
        if (front.isNotBlank() && back.isNotBlank()) {
            viewModel.updateFlashcardEntity(entity.copy(
                type = type,
                front = front.trim(),
                back = back.trim(),
                deckMode = if (useSm2) "sm2" else "basic"
            ))
            onDismiss()
        }
    }

    DialogWrapper(onDismiss = onDismiss) {
        DialogHeader(FieldMindIcons.Flashcard, "Edit Flashcard", "Update card content and study mode", accent = FieldMindTheme.colors.flashcard)
        OptionPickerField(label = "Card type", selected = type, options = listOf("term", "definition", "concept", "question-answer", "mistake card", "field ID", "method step"), onSelected = { type = it }, icon = FieldMindIcons.Category)
        FieldTextField(front, { front = it }, "Prompt / front", minLines = 2, supportingText = "The prompt shown during review")
        FieldTextField(back, { back = it }, "Answer / back", minLines = 4, supportingText = "The answer or explanation")
        DialogDividerSection("Study mode", FieldMindIcons.Flip, FieldMindTheme.colors.flashcard)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Spaced repetition (SM-2)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("Review at optimal intervals", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = useSm2, onCheckedChange = { useSm2 = it })
        }
        DialogActions(onCancel = onDismiss, onSave = { save() }, saveEnabled = front.isNotBlank() && back.isNotBlank(), saveLabel = "Save changes")
    }
}
/**
 * Map card for an observation detail: a small offline preview marker, the resolved place name
 * (when known), the exact coordinates, and a link out to the device map app.
 */
@Composable
internal fun ObservationLocationCard(latitude: Double, longitude: Double, manualLocation: String) {
    val colors = FieldMindTheme.colors
    val uriHandler = LocalUriHandler.current
    val coords = "%.5f, %.5f".format(latitude, longitude)
    val placeName = manualLocation.substringBefore(" • GPS").trim().takeIf { it.isNotBlank() && !it.startsWith("GPS") }
    Card(
        Modifier.fillMaxWidth(),
        shape = CuteCardDefaults.Shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon = FieldMindIcons.Location, contentDescription = null, tint = colors.observation, size = 20.dp)
                Text("Location", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            if (placeName != null) Text(placeName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Box(Modifier.fillMaxWidth().clip(CuteCardDefaults.ShapeCompact)) {
                OsmMapView(
                    points = listOf(latitude to longitude),
                    showEmptyState = false,
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(coords, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                TextButton(onClick = {
                    val label = Uri.encode(placeName ?: "Observation")
                    runCatching { uriHandler.openUri("geo:$latitude,$longitude?q=$latitude,$longitude($label)") }
                }) {
                    Icon(icon = FieldMindIcons.Location, contentDescription = null, size = 18.dp); Spacer(Modifier.size(6.dp)); Text("Open in maps")
                }
            }
        }
    }
}

