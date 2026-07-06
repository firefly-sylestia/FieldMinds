package fieldmind.research.app.features.field.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import fieldmind.research.app.features.field.data.database.entity.TaskEntity
import fieldmind.research.app.features.field.presentation.components.*
import fieldmind.research.app.features.field.presentation.theme.FieldMindTheme
import fieldmind.research.app.features.field.presentation.viewmodel.FieldMindViewModel
import fieldmind.research.app.shared.presentation.components.icons.Icon
import fieldmind.research.app.shared.presentation.components.icons.MaterialSymbolIcon
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import fieldmind.research.app.ui.theme.CuteElevations

// ══════════════════════════════════════════════════════════════════════
//  TASKS SCREEN — Full task management with sections and filtering
// ══════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: FieldMindViewModel,
    onBack: () -> Unit = {},
    onOpenDetail: (String, Long) -> Unit = { _, _ -> },
    onNavigate: (String) -> Unit = {}
) {
    val tasks by viewModel.tasks.collectAsState()
    val haptics = rememberFieldMindHaptics()

    // Track checked-off tasks locally for optimistic UI
    val completedTaskIds = remember { mutableStateMapOf<Long, Boolean>() }
    val projects by viewModel.projects.collectAsState()
    val scope = rememberCoroutineScope()

    // Delete confirmation dialog state
    var taskToDelete by remember { mutableStateOf<TaskEntity?>(null) }

    // ── Filter state ──
    var filterStatus by remember { mutableStateOf<String?>(null) } // null = all, "Pending", "Done"
    var filterPriority by remember { mutableStateOf<String?>(null) } // null = all, "High", "Medium", "Low"
    var filterProjectId by remember { mutableStateOf<Long?>(null) } // null = all projects
    var showFilterSheet by remember { mutableStateOf(false) }
    val hasActiveFilters = filterStatus != null || filterPriority != null || filterProjectId != null

    // Apply filters to a list of tasks
    fun applyFilters(taskList: List<TaskEntity>): List<TaskEntity> {
        return taskList.filter { t ->
            (filterStatus == null || t.status == filterStatus) &&
            (filterPriority == null || t.priority == filterPriority) &&
            (filterProjectId == null || t.projectId == filterProjectId)
        }
    }

    // ── Compute sections ──
    val todayDate = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(System.currentTimeMillis()))
    }

    val todayTasks = remember(tasks, completedTaskIds, filterStatus, filterPriority, filterProjectId) {
        applyFilters(tasks.filter { t ->
            t.dueDate == todayDate && t.status != "Done" && completedTaskIds[t.id] != true
        }).sortedBy { it.priority }
    }

    val upcomingTasks = remember(tasks, completedTaskIds, filterStatus, filterPriority, filterProjectId) {
        applyFilters(tasks.filter { t ->
            t.dueDate.isNotBlank() && t.dueDate != todayDate && t.status != "Done" && completedTaskIds[t.id] != true
        }).sortedBy { it.dueDate }
    }

    val unscheduledTasks = remember(tasks, completedTaskIds, filterStatus, filterPriority, filterProjectId) {
        applyFilters(tasks.filter { t ->
            t.dueDate.isBlank() && t.status != "Done" && completedTaskIds[t.id] != true
        }).sortedByDescending { it.updatedAt }
    }

    val doneTasks = remember(tasks, completedTaskIds, filterStatus, filterPriority, filterProjectId) {
        applyFilters(tasks.filter { t ->
            t.status == "Done" || completedTaskIds[t.id] == true
        }).sortedByDescending { it.updatedAt }
    }

    // ── Section expand state ──
    var expandedToday by remember { mutableStateOf(true) }
    var expandedUpcoming by remember { mutableStateOf(true) }
    var expandedUnscheduled by remember { mutableStateOf(false) }
    var expandedDone by remember { mutableStateOf(false) }

    // ── Filter bottom sheet ──
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text("Filter tasks", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                // Status filter
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Status", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("All", "Pending", "Done").forEach { status ->
                            FilterChip(
                                selected = (status == "All" && filterStatus == null) || filterStatus == status,
                                onClick = { filterStatus = if (status == "All") null else status },
                                label = { Text(status) }
                            )
                        }
                    }
                }

                // Priority filter
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Priority", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("All", "High", "Medium", "Low").forEach { priority ->
                            FilterChip(
                                selected = (priority == "All" && filterPriority == null) || filterPriority == priority,
                                onClick = { filterPriority = if (priority == "All") null else priority },
                                label = { Text(priority) }
                            )
                        }
                    }
                }

                // Project filter
                if (projects.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Project", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = filterProjectId == null,
                                onClick = { filterProjectId = null },
                                label = { Text("All projects") }
                            )
                            projects.forEach { project ->
                                FilterChip(
                                    selected = filterProjectId == project.id,
                                    onClick = { filterProjectId = project.id },
                                    label = { Text(project.title.take(15)) }
                                )
                            }
                        }
                    }
                }

                // Clear filters
                if (hasActiveFilters) {
                    TextButton(
                        onClick = {
                            filterStatus = null
                            filterPriority = null
                            filterProjectId = null
                            showFilterSheet = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(MaterialSymbolIcon("clear"), null, size = 16.dp)
                        Spacer(Modifier.size(6.dp))
                        Text("Clear all filters")
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(20.dp, 20.dp, 20.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── Header ──
        item {
            StandardScreenHeader(
                title = "Tasks",
                subtitle = "Track field tasks, surveys, and to-dos.",
                icon = MaterialSymbolIcon("checklist"),
                heroColor = FieldMindTheme.colors.task,
                trailing = {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Filter button (with active indicator)
                        IconButton(
                            onClick = { showFilterSheet = true },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box {
                                Icon(
                                    MaterialSymbolIcon("filter_list"),
                                    "Filter tasks",
                                    size = 22.dp,
                                    tint = if (hasActiveFilters) FieldMindTheme.colors.positive
                                           else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (hasActiveFilters) {
                                    Box(
                                        Modifier
                                            .align(Alignment.TopEnd)
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(FieldMindTheme.colors.positive)
                                    )
                                }
                            }
                        }
                        IconButton(onClick = { onNavigate("field_new_task") }, modifier = Modifier.size(40.dp)) {
                            Icon(MaterialSymbolIcon("add"), "Add task", size = 22.dp, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            )
        }

        // ── Stats row ──
        if (tasks.isNotEmpty()) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        value = "${todayTasks.size + upcomingTasks.size + unscheduledTasks.size}",
                        label = "Pending",
                        icon = MaterialSymbolIcon("pending_actions"),
                        color = FieldMindTheme.colors.task,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        value = "${doneTasks.size}",
                        label = "Completed",
                        icon = MaterialSymbolIcon("check_circle"),
                        color = FieldMindTheme.colors.positive,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // ════════════════════════════════════════════════════════
        //  TODAY section
        // ════════════════════════════════════════════════════════
        item {
            TaskSectionHeader(
                title = "Today",
                icon = MaterialSymbolIcon("today"),
                count = todayTasks.size,
                accentColor = FieldMindTheme.colors.task,
                expanded = expandedToday,
                onToggle = { expandedToday = !expandedToday }
            )
        }

        if (expandedToday) {
            if (todayTasks.isEmpty()) {
                item {
                    EmptyTaskHint("No tasks due today. Tap + to add one.")
                }
            } else {
                items(todayTasks, key = { it.id }) { task ->
                    SwipeToCompleteTaskCard(
                        task = task,
                        accentColor = FieldMindTheme.colors.task,
                        onToggle = {
                            haptics.confirm()
                            completedTaskIds[task.id] = true
                            viewModel.updateTaskEntity(task.copy(status = "Done"))
                        },
                        onDelete = { taskToDelete = task },
                        onTap = { onNavigate("field_task_detail/${task.id}") }
                    )
                }
            }
        }

        // ════════════════════════════════════════════════════════
        //  UPCOMING section
        // ════════════════════════════════════════════════════════
        item {
            TaskSectionHeader(
                title = "Upcoming",
                icon = MaterialSymbolIcon("calendar_month"),
                count = upcomingTasks.size,
                accentColor = FieldMindTheme.colors.observation,
                expanded = expandedUpcoming,
                onToggle = { expandedUpcoming = !expandedUpcoming }
            )
        }

        if (expandedUpcoming) {
            if (upcomingTasks.isEmpty()) {
                item {
                    EmptyTaskHint("No upcoming tasks.")
                }
            } else {
                items(upcomingTasks, key = { it.id }) { task ->
                    SwipeToCompleteTaskCard(
                        task = task,
                        accentColor = FieldMindTheme.colors.observation,
                        onToggle = {
                            haptics.confirm()
                            completedTaskIds[task.id] = true
                            viewModel.updateTaskEntity(task.copy(status = "Done"))
                        },
                        onDelete = { taskToDelete = task },
                        onTap = { onNavigate("field_task_detail/${task.id}") }
                    )
                }
            }
        }

        // ════════════════════════════════════════════════════════
        //  UNSCHEDULED section
        // ════════════════════════════════════════════════════════
        item {
            TaskSectionHeader(
                title = "Unscheduled",
                icon = MaterialSymbolIcon("inbox"),
                count = unscheduledTasks.size,
                accentColor = FieldMindTheme.colors.data,
                expanded = expandedUnscheduled,
                onToggle = { expandedUnscheduled = !expandedUnscheduled }
            )
        }

        if (expandedUnscheduled) {
            if (unscheduledTasks.isEmpty()) {
                item {
                    EmptyTaskHint("All tasks have due dates.")
                }
            } else {
                items(unscheduledTasks, key = { it.id }) { task ->
                    SwipeToCompleteTaskCard(
                        task = task,
                        accentColor = FieldMindTheme.colors.data,
                        onToggle = {
                            haptics.confirm()
                            completedTaskIds[task.id] = true
                            viewModel.updateTaskEntity(task.copy(status = "Done"))
                        },
                        onDelete = { taskToDelete = task },
                        onTap = { onNavigate("field_task_detail/${task.id}") }
                    )
                }
            }
        }

        // ════════════════════════════════════════════════════════
        //  DONE section
        // ════════════════════════════════════════════════════════
        item {
            TaskSectionHeader(
                title = "Done",
                icon = MaterialSymbolIcon("check_circle"),
                count = doneTasks.size,
                accentColor = FieldMindTheme.colors.positive,
                expanded = expandedDone,
                onToggle = { expandedDone = !expandedDone }
            )
        }

        if (expandedDone) {
            if (doneTasks.isEmpty()) {
                item {
                    EmptyTaskHint("No completed tasks yet.")
                }
            } else {
                items(doneTasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        isChecked = true,
                        accentColor = FieldMindTheme.colors.positive,
                        onToggle = {
                            haptics.light()
                            completedTaskIds.remove(task.id)
                            viewModel.updateTaskEntity(task.copy(status = "Pending"))
                        },
                        onDelete = { taskToDelete = task },
                        onTap = { onNavigate("field_task_detail/${task.id}") }
                    )
                }
            }
        }
    }

    // ── Delete confirmation dialog ──
    if (taskToDelete != null) {
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            title = { Text("Delete task") },
            text = { Text("Are you sure you want to delete \"${taskToDelete?.title}\"? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    taskToDelete?.let { viewModel.deleteTask(it.id) }
                    taskToDelete = null
                    haptics.confirm()
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ══════════════════════════════════════════════════════════════════════
//  TASK SECTION HEADER — Collapsible with count badge
// ══════════════════════════════════════════════════════════════════════

@Composable
private fun TaskSectionHeader(
    title: String,
    icon: MaterialSymbolIcon,
    count: Int,
    accentColor: Color,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(accentColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = accentColor, size = 18.dp)
            }

            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            // Count badge
            if (count > 0) {
                Surface(
                    shape = RoundedCornerShape(30.dp),
                    color = accentColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        "$count",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }

            Icon(
                if (expanded) MaterialSymbolIcon("expand_less") else MaterialSymbolIcon("expand_more"),
                if (expanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                size = 20.dp
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  TASK CARD — Checkable task item with priority badge and due date
// ══════════════════════════════════════════════════════════════════════

@Composable
private fun TaskCard(
    task: TaskEntity,
    isChecked: Boolean,
    accentColor: Color,
    onToggle: () -> Unit,
    onDelete: () -> Unit = {},
    onTap: () -> Unit = {}
) {
    val priorityColor = when (task.priority) {
        "High" -> MaterialTheme.colorScheme.error
        "Medium" -> FieldMindTheme.colors.warning
        "Low" -> FieldMindTheme.colors.positive
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val animatedCheck by animateFloatAsState(
        targetValue = if (isChecked) 1f else 0f,
        animationSpec = spring(stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow),
        label = "taskCheck"
    )

    Card(
        onClick = onTap,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isChecked)
                MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.6f)
            else
                MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ── Checkbox with spring bounce ──
            val checkBounce = remember { Animatable(1f) }
            val scope = rememberCoroutineScope()
            LaunchedEffect(isChecked) {
                if (isChecked) {
                    checkBounce.snapTo(1.3f)
                    checkBounce.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 500f))
                }
            }
            Surface(
                onClick = {
                    scope.launch {
                        checkBounce.snapTo(1.3f)
                        checkBounce.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 350f))
                    }
                    onToggle()
                },
                shape = CircleShape,
                color = if (isChecked)
                    accentColor.copy(alpha = 0.14f)
                else
                    MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.size(36.dp).graphicsLayer {
                    scaleX = checkBounce.value
                    scaleY = checkBounce.value
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isChecked) {
                        Icon(
                            MaterialSymbolIcon("check_circle", filled = true),
                            "Toggle done",
                            size = 22.dp,
                            tint = accentColor
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        )
                    }
                }
            }

            // ── Task content ──
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isChecked) FontWeight.Normal else FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (isChecked)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    else
                        MaterialTheme.colorScheme.onSurface
                )

                // Description snippet
                if (task.description.isNotBlank() && !isChecked) {
                    Text(
                        task.description.take(80),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Meta row: priority + due date + type
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Priority badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = priorityColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            task.priority,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = priorityColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 9.sp
                        )
                    }

                    // Due date
                    if (task.dueDate.isNotBlank()) {
                        val isOverdue = try {
                            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val due = fmt.parse(task.dueDate)
                            due != null && due.before(Date(System.currentTimeMillis() - 86400000L)) && !isChecked
                        } catch (_: Exception) { false }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                MaterialSymbolIcon("calendar_today"),
                                null,
                                size = 10.dp,
                                tint = if (isOverdue) MaterialTheme.colorScheme.error
                                       else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Text(
                                formatDueDate(task.dueDate),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isOverdue) MaterialTheme.colorScheme.error
                                       else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                fontSize = 9.sp
                            )
                        }
                    }

                    // Task type
                    if (task.taskType.isNotBlank() && !isChecked) {
                        Text(
                            task.taskType,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            fontSize = 9.sp
                        )
                    }
                }
            }

            // ── Action buttons (visible on hover/always) ──
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Complete/uncomplete button (prominent check circle)
                Surface(
                    onClick = onToggle,
                    shape = CircleShape,
                    color = if (isChecked)
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    else
                        FieldMindTheme.colors.positive.copy(alpha = 0.12f),
                    modifier = Modifier.size(30.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            MaterialSymbolIcon(if (isChecked) "undo" else "check_circle", filled = !isChecked),
                            if (isChecked) "Mark pending" else "Mark done",
                            size = 18.dp,
                            tint = if (isChecked) MaterialTheme.colorScheme.onSurfaceVariant
                                   else FieldMindTheme.colors.positive
                        )
                    }
                }

                // Delete button
                Surface(
                    onClick = onDelete,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
                    modifier = Modifier.size(30.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            MaterialSymbolIcon("delete"),
                            "Delete task",
                            size = 16.dp,
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  EMPTY STATE HINT
// ══════════════════════════════════════════════════════════════════════

@Composable
private fun EmptyTaskHint(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

// ══════════════════════════════════════════════════════════════════════
//  SWIPE-TO-COMPLETE TASK CARD — Phase 4d: swipe right reveals green
//  checkmark background; commit triggers a satisfying snap animation
//  and marks the task as done.
// ══════════════════════════════════════════════════════════════════════

@Composable
private fun SwipeToCompleteTaskCard(
    task: TaskEntity,
    accentColor: Color,
    onToggle: () -> Unit,
    onDelete: () -> Unit = {},
    onTap: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val haptics = rememberFieldMindHaptics()
    val density = LocalDensity.current
    val reduceMotion = FieldMindMotion.isReduceMotion()

    var swipeOffset by remember { mutableFloatStateOf(0f) }
    var isCommitting by remember { mutableStateOf(false) }
    var contentWidthPx by remember { mutableFloatStateOf(1f) }

    val swipeThresholdPx = with(density) { 120.dp.toPx() }
    val maxSwipePx = with(density) { 240.dp.toPx() }

    // Animated offset with spring for smooth snap/spring-back
    val animatedOffset by animateFloatAsState(
        targetValue = swipeOffset,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "swipeOffset"
    )

    val swipeProgress = (animatedOffset / swipeThresholdPx).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .onGloballyPositioned { coords ->
                contentWidthPx = coords.size.width.toFloat().coerceAtLeast(1f)
            }
    ) {
        // ── Background layer: green checkmark revealed on swipe ──
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(FieldMindTheme.colors.positive.copy(alpha = 0.12f * swipeProgress))
                .padding(end = 20.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.graphicsLayer {
                    alpha = swipeProgress
                    scaleX = 0.5f + 0.5f * swipeProgress
                    scaleY = 0.5f + 0.5f * swipeProgress
                }
            ) {
                Text(
                    "Complete",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = FieldMindTheme.colors.positive
                )
                Icon(
                    MaterialSymbolIcon("check_circle", filled = true),
                    "Swipe to complete",
                    tint = FieldMindTheme.colors.positive,
                    size = 28.dp
                )
            }
        }

        // ── Foreground card: slides right with gesture ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationX = animatedOffset
                    scaleX = 1f - 0.03f * swipeProgress
                    scaleY = 1f - 0.03f * swipeProgress
                }
                .then(
                    if (!reduceMotion) {
                        Modifier.pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragStart = {},
                                onHorizontalDrag = { change, dragAmount ->
                                    change.consume()
                                    val newOffset = (swipeOffset + dragAmount).coerceIn(0f, maxSwipePx)
                                    swipeOffset = newOffset
                                },
                                onDragEnd = {
                                    if (swipeOffset >= swipeThresholdPx && !isCommitting) {
                                        // Snap to full, brief pause, then toggle
                                        isCommitting = true
                                        haptics.confirm()
                                        scope.launch {
                                            swipeOffset = maxSwipePx * 1.5f
                                            delay(120)
                                            swipeOffset = 0f
                                            isCommitting = false
                                            onToggle()
                                        }
                                    } else {
                                        // Spring back to 0
                                        scope.launch {
                                            swipeOffset = 0f
                                        }
                                    }
                                },
                                onDragCancel = {
                                    scope.launch {
                                        swipeOffset = 0f
                                    }
                                }
                            )
                        }
                    } else {
                        Modifier
                    }
                )
        ) {
            TaskCard(
                task = task,
                isChecked = false,
                accentColor = accentColor,
                onToggle = onToggle,
                onDelete = onDelete,
                onTap = onTap
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  STAT CARD (reused from QuestionsScreen pattern)
// ══════════════════════════════════════════════════════════════════════

@Composable
private fun StatCard(value: String, label: String, icon: MaterialSymbolIcon, color: Color, modifier: Modifier = Modifier) {
    Card(modifier, shape = RoundedCornerShape(30.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(Modifier.size(28.dp).clip(RoundedCornerShape(14.dp)).background(color.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, size = 16.dp)
            }
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  HELPERS
// ══════════════════════════════════════════════════════════════════════

private fun formatDueDate(dateStr: String): String {
    if (dateStr.isBlank()) return ""
    return try {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = fmt.parse(dateStr) ?: return dateStr
        val now = Date()
        val diffMs = date.time - now.time
        val diffDays = (diffMs / 86400000L).toInt()
        when {
            diffDays < 0 -> "Overdue"
            diffDays == 0 -> "Today"
            diffDays == 1 -> "Tomorrow"
            diffDays <= 7 -> "In $diffDays days"
            else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
        }
    } catch (_: Exception) {
        dateStr
    }
}
