package com.curio.app.features.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.curio.app.data.CaptureFormat
import com.curio.app.data.CategoryId
import com.curio.app.data.CurioCategories
import com.curio.app.data.CurioCategory
import com.curio.app.data.MockEntry
import com.curio.app.data.MockTopics
import com.curio.app.navigation.CurioRoutes
import com.curio.app.ui.components.ScreenEntrance
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioGradients
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons

/**
 * Entry Detail — see CURIO_SPEC.md §10. Framed presentation of a saved
 * capture. Renders each format in its most "finished" presentational state
 * — NOT the same editable widgets from Save/Capture.
 *
 * Layout:
 *   - Hero image placeholder (full-width, 260dp tall, category gradient)
 *     with back-arrow + overflow menu floating over the top edge
 *   - Topic name + category chip + "Xd ago"
 *   - Format-specific render body
 */
@Composable
fun EntryDetailScreen(entryId: String, navController: NavController) {
    val entry = remember(entryId) {
        MockTopics.sampleEntries.find { it.id == entryId }
    }
    if (entry == null) {
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }

    val cat = CurioCategories.byId(entry.topic.categoryId)
    var menuExpanded by remember { mutableStateOf(false) }
    var deleteDialogVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Hero image placeholder ──────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    if (cat.id == CategoryId.WILDCARD)
                        Brush.horizontalGradient(CurioGradients.WildcardGradientStops)
                    else Brush.verticalGradient(listOf(cat.accent, cat.tint))
                ),
            contentAlignment = Alignment.Center
        ) {
            CurioIcon(
                name = cat.iconGlyph,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.9f),
                size = 96.dp
            )

            // Top bar overlay (back + overflow)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = { navController.popBackStack() },
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.25f)
                ) {
                    CurioIcon(
                        name = CurioIcons.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        size = 24.dp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Box {
                    Surface(
                        onClick = { menuExpanded = true },
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.25f)
                    ) {
                        CurioIcon(
                            name = CurioIcons.MoreVert,
                            contentDescription = "More",
                            tint = Color.White,
                            size = 24.dp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                menuExpanded = false
                                navController.navigate(
                                    CurioRoutes.captureFor(cat.id.routeSlug, entry.topic.name)
                                )
                            },
                            leadingIcon = {
                                CurioIcon(
                                    name = CurioIcons.Edit,
                                    contentDescription = null,
                                    size = 20.dp
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share") },
                            onClick = {
                                menuExpanded = false
                                // TODO Phase 4: render share card + Intent.ACTION_SEND
                            },
                            leadingIcon = {
                                CurioIcon(
                                    name = CurioIcons.Share,
                                    contentDescription = null,
                                    size = 20.dp
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Delete",
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                deleteDialogVisible = true
                            },
                            leadingIcon = {
                                CurioIcon(
                                    name = CurioIcons.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    size = 20.dp
                                )
                            }
                        )
                    }
                }
            }
        }

        // ── Topic name + meta ───────────────────────────────────────────────
        ScreenEntrance {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = entry.topic.name,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = cat.tint
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 6.dp
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            CurioIcon(
                                name = cat.iconGlyph,
                                contentDescription = null,
                                tint = cat.accent,
                                size = 14.dp
                            )
                            Text(
                                text = cat.displayName,
                                style = MaterialTheme.typography.labelMedium,
                                color = cat.accent
                            )
                        }
                    }
                    Text(
                        text = when (entry.capturedAtDaysAgo) {
                            0    -> "Captured today"
                            1    -> "Captured yesterday"
                            else -> "Captured ${entry.capturedAtDaysAgo}d ago"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // ── Format-specific render body ─────────────────────────────────────
        ScreenEntrance {
            Box(
                modifier = Modifier.padding(
                    horizontal = 20.dp,
                    vertical = 8.dp
                )
            ) {
                FormatBody(entry = entry, category = cat)
            }
        }

        Spacer(Modifier.height(32.dp))
    }

    if (deleteDialogVisible) {
        AlertDialog(
            onDismissRequest = { deleteDialogVisible = false },
            title = { Text("Delete this entry?") },
            text = {
                Text(
                    "This capture will be permanently removed from your Cabinet."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    deleteDialogVisible = false
                    // TODO Phase 4: actual Room delete
                    navController.popBackStack()
                }) {
                    Text(
                        "Delete",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteDialogVisible = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Format-specific renders — each format in its "finished" presentational
// state, NOT the same editable widgets from Save/Capture.

@Composable
private fun FormatBody(entry: MockEntry, category: CurioCategory) {
    when (entry.format) {
        CaptureFormat.SoundBite    -> SoundBitePlayback(entry, category)
        CaptureFormat.ReelNotes    -> ReelNotesRender(entry, category)
        CaptureFormat.Marginalia   -> MarginaliaRender(entry, category)
        CaptureFormat.GalleryWall  -> GalleryWallRender(entry, category)
        CaptureFormat.FieldNotes   -> FieldNotesRender(entry, category)
        CaptureFormat.OpenNotebook -> OpenNotebookRender(entry, category)
    }
}

@Composable
private fun SoundBitePlayback(entry: MockEntry, category: CurioCategory) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = category.tint,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    onClick = { /* TODO Phase 4: playback */ },
                    shape = RoundedCornerShape(50),
                    color = category.accent
                ) {
                    CurioIcon(
                        name = CurioIcons.PlayArrow,
                        contentDescription = "Play",
                        tint = CurioColors.DeepPlum,
                        size = 32.dp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Voice note",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = entry.bodyPreview,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = entry.bodyContent,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ReelNotesRender(entry: MockEntry, category: CurioCategory) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(4) {
                CurioIcon(
                    name = CurioIcons.Star,
                    contentDescription = null,
                    tint = category.accent,
                    size = 20.dp
                )
            }
            CurioIcon(
                name = CurioIcons.StarOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                size = 20.dp
            )
        }
        Text(
            text = entry.bodyContent,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun MarginaliaRender(entry: MockEntry, category: CurioCategory) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = category.tint,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "From the entry",
                    style = MaterialTheme.typography.labelMedium,
                    color = category.accent
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = entry.bodyPreview,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontStyle = FontStyle.Italic
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Text(
            text = entry.bodyContent,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun GalleryWallRender(entry: MockEntry, category: CurioCategory) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(120.dp)
                    .background(category.accent, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                CurioIcon(
                    name = CurioIcons.Image,
                    contentDescription = null,
                    tint = Color.White,
                    size = 32.dp
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(120.dp)
                    .background(category.tint, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                CurioIcon(
                    name = CurioIcons.Image,
                    contentDescription = null,
                    tint = category.accent,
                    size = 32.dp
                )
            }
        }
        Text(
            text = entry.bodyContent,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun FieldNotesRender(entry: MockEntry, category: CurioCategory) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Observations",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = category.accent
        )
        Text(
            text = entry.bodyContent,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun OpenNotebookRender(entry: MockEntry, category: CurioCategory) {
    Text(
        text = entry.bodyContent,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface
    )
}