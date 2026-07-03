package fieldmind.research.app.features.field.presentation.screens

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
import fieldmind.research.app.features.field.presentation.components.*
import fieldmind.research.app.features.field.presentation.theme.FieldMindTheme
import fieldmind.research.app.features.field.presentation.viewmodel.FieldMindViewModel
import fieldmind.research.app.shared.presentation.components.icons.Icon
import fieldmind.research.app.shared.presentation.components.icons.MaterialSymbolIcon
import fieldmind.research.app.ui.theme.CuteElevations
import fieldmind.research.app.ui.theme.CuteGradients
import fieldmind.research.app.ui.theme.cuteShadow
import java.io.File

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
    var shareFormat by remember { mutableStateOf("CSV") }
    var includeMedia by remember { mutableStateOf(true) }

    val shareFormats = listOf("CSV", "JSON", "PDF Report", "FieldMind Archive")

    val screenBgGradient = CuteGradients.brushFor(CuteGradients.Style.ScreenBackground)
    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Box(Modifier.fillMaxSize().background(brush = screenBgGradient)) {
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

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            showFastSnackbar(snackbar, scope, "Export started ($shareFormat)")
                                        },
                                        shape = RoundedCornerShape(22.dp)
                                    ) {
                                        Icon(MaterialSymbolIcon("file_download"), null, size = 18.dp)
                                        Spacer(Modifier.size(6.dp))
                                        Text("Export")
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(android.content.Intent.EXTRA_TEXT, "Shared from FieldMind: ${observations.size} observations across ${projects.size} projects.")
                                            }
                                            context.startActivity(android.content.Intent.createChooser(shareIntent, "Share via"))
                                        },
                                        shape = RoundedCornerShape(22.dp)
                                    ) {
                                        Icon(MaterialSymbolIcon("share"), null, size = 18.dp)
                                        Spacer(Modifier.size(6.dp))
                                        Text("Share link")
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
                                                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(android.content.Intent.EXTRA_TEXT, "Join my FieldMind research workspace! Download FieldMind to collaborate.")
                                                }
                                                context.startActivity(android.content.Intent.createChooser(shareIntent, "Invite via"))
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
                                    Button(onClick = { showFastSnackbar(snackbar, scope, "Portfolio generated!") }, shape = RoundedCornerShape(22.dp)) {
                                        Icon(MaterialSymbolIcon("description"), null, size = 18.dp)
                                        Spacer(Modifier.size(6.dp))
                                        Text("Generate portfolio")
                                    }
                                    OutlinedButton(onClick = { onOpenExport() }, shape = RoundedCornerShape(22.dp)) {
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
