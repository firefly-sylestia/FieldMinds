package fieldmind.research.app.features.field.presentation.screens
import fieldmind.research.app.ui.theme.CuteCardDefaults

import androidx.compose.animation.AnimatedVisibility
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
import fieldmind.research.app.features.field.data.database.entity.SourceEntity
import fieldmind.research.app.features.field.presentation.components.*
import fieldmind.research.app.features.field.presentation.theme.FieldMindTheme
import fieldmind.research.app.features.field.presentation.viewmodel.FieldMindViewModel
import fieldmind.research.app.shared.presentation.components.icons.Icon
import fieldmind.research.app.shared.presentation.components.icons.MaterialSymbolIcon
import fieldmind.research.app.ui.theme.CuteElevations
import fieldmind.research.app.ui.theme.cuteShadow
import fieldmind.research.app.ui.theme.screenBackground
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Citation & Bibliography Manager — track sources, papers, and references
 * with proper citation formatting and export.
 */
@Composable
fun CitationManagerScreen(
    viewModel: FieldMindViewModel,
    onBack: () -> Unit = {},
    onOpenDetail: (String, Long) -> Unit = { _, _ -> }
) {
    val sources by viewModel.sources.collectAsState()
    val colors = FieldMindTheme.colors
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    var showNewSource by remember { mutableStateOf(false) }
    var selectedFormat by remember { mutableStateOf("APA") }
    var expandedSourceId by remember { mutableStateOf<Long?>(null) }

    // ── Citation formats ──
    val citationFormats = listOf("APA", "MLA", "Chicago", "IEEE", "Harvard")

    val filteredSources = remember(sources, searchQuery) {
        if (searchQuery.isBlank()) sources
        else sources.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.author.contains(searchQuery, ignoreCase = true)
        }
    }

    fun formatCitation(source: SourceEntity, format: String): String {
        val authorPart = source.author.ifBlank { "Unknown" }
        val yearPart = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date(source.createdAt))
        val titlePart = source.title
        val linkPart = if (source.link.isNotBlank()) " Retrieved from ${source.link}" else ""
        return when (format) {
            "MLA" -> "$authorPart. \"$titlePart.\" $yearPart.$linkPart"
            "Chicago" -> "$authorPart. \"$titlePart.\" $yearPart.$linkPart"
            "IEEE" -> "$authorPart, \"$titlePart,\" $yearPart.$linkPart"
            "Harvard" -> "$authorPart ($yearPart) $titlePart.$linkPart"
            else -> "$authorPart ($yearPart). $titlePart.$linkPart" // APA
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
                        title = "Bibliography",
                        subtitle = "${sources.size} source${if (sources.size != 1) "s" else ""}",
                        icon = MaterialSymbolIcon("book"),
                        heroColor = colors.source,
                        trailing = {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                BackButton(
                                    onClick = { showSearch = !showSearch },
                                    icon = MaterialSymbolIcon("search"),
                                    contentDescription = "Search"
                                )
                                BackButton(
                                    onClick = { showNewSource = true },
                                    icon = FieldMindIcons.Add,
                                    contentDescription = "Add source"
                                )
                                BackButton(onClick = onBack)
                            }
                        }
                    )
                }

                // ── Search ──
                if (showSearch) {
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search by title or author…") },
                            leadingIcon = { Icon(MaterialSymbolIcon("search"), null, size = 20.dp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = CuteCardDefaults.OptionShape
                        )
                    }
                }

                // ── Citation format picker ──
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .cuteShadow(elevation = CuteElevations.nonClickableTier, shape = CuteCardDefaults.Shape),
                        shape = CuteCardDefaults.Shape,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(MaterialSymbolIcon("format_quote"), null, tint = colors.source, size = 20.dp)
                                Text("Citation format", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                citationFormats.forEach { format ->
                                    FilterChip(
                                        selected = selectedFormat == format,
                                        onClick = { selectedFormat = format },
                                        label = { Text(format, fontSize = if (format.length > 5) 11.sp else 13.sp) }
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Sources list ──
                if (filteredSources.isEmpty()) {
                    item {
                        EmptyState(
                            "No sources yet",
                            "Add books, papers, websites, and other references to build your bibliography.",
                            icon = MaterialSymbolIcon("book"),
                            iconColor = colors.source,
                            actionLabel = "Add first source",
                            onAction = { showNewSource = true }
                        )
                    }
                }

                itemsIndexed(filteredSources) { index, source ->
                    val isExpanded = expandedSourceId == source.id
                    val citation = formatCitation(source, selectedFormat)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .cuteShadow(elevation = CuteElevations.nonClickableTier, shape = CuteCardDefaults.Shape),
                        shape = CuteCardDefaults.Shape,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.clickable {
                                expandedSourceId = if (isExpanded) null else source.id
                            }
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(14.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    Modifier.size(40.dp).clip(MaterialTheme.shapes.medium)
                                        .background(colors.source.copy(alpha = 0.14f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(MaterialSymbolIcon("book"), null, tint = colors.source, size = 22.dp)
                                }
                                Column(Modifier.weight(1f)) {
                                    Text(source.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (source.author.isNotBlank()) {
                                            Text(source.author, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Text(
                                            SimpleDateFormat("yyyy", Locale.getDefault()).format(Date(source.createdAt)),
                                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Icon(
                                    if (isExpanded) MaterialSymbolIcon("expand_less") else MaterialSymbolIcon("expand_more"),
                                    null, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 20.dp
                                )
                            }

                            AnimatedVisibility(visible = isExpanded, enter = expandVertically(), exit = shrinkVertically()) {
                                Column(Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                                    // Citation preview
                                    Surface(
                                        shape = MaterialTheme.shapes.medium,
                                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                                    ) {
                                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text("Citation ($selectedFormat)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = colors.source)
                                            Text(citation, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }

                                    // Copy citation button
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {                                            AssistChip(
                                                onClick = {
                                                    val clipboard = (context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager)
                                                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("citation", citation))
                                                    showFastSnackbar(snackbar, scope, "Citation copied!")
                                                },
                                                label = { Text("Copy citation") },
                                                leadingIcon = { Icon(MaterialSymbolIcon("content_copy"), null, size = 16.dp) }
                                            )
                            if (source.link.isNotBlank()) {
                                AssistChip(
                                    onClick = {
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(source.link))
                                        context.startActivity(intent)
                                    },
                                    label = { Text("Open source") },
                                    leadingIcon = { Icon(MaterialSymbolIcon("open_in_new"), null, size = 16.dp) }
                                )
                            }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ── New source dialog ──
    if (showNewSource) {
        NewSourceDialog(
            onDismiss = { showNewSource = false },
            onSave = { title, author, url, notes ->
                viewModel.addSource(
                    type = "webpage",
                    title = title,
                    author = author,
                    link = url,
                    summary = notes,
                    taught = "",
                    reliability = 3,
                    dateOrYear = java.text.SimpleDateFormat("yyyy", java.util.Locale.getDefault()).format(java.util.Date())
                )
                showNewSource = false
                showFastSnackbar(snackbar, scope, "Source added to bibliography")
            }
        )
    }
}

@Composable
private fun NewSourceDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    SwipeableAlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(MaterialSymbolIcon("book"), null, tint = MaterialTheme.colorScheme.primary, size = 28.dp) },
        title = { Text("Add source") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = CuteCardDefaults.ShapeCompact)
                OutlinedTextField(value = author, onValueChange = { author = it }, label = { Text("Author") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = CuteCardDefaults.ShapeCompact)
                OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("URL (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = CuteCardDefaults.ShapeCompact)
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes (optional)") }, minLines = 2, modifier = Modifier.fillMaxWidth(), shape = CuteCardDefaults.ShapeCompact)
            }
        },
        confirmButton = {
            Button(onClick = { onSave(title, author, url, notes) }, shape = CuteCardDefaults.ButtonShape, enabled = title.isNotBlank()) { Text("Add source") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
