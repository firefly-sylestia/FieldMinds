package com.curio.app.features.topichistory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.curio.app.data.CaptureFormat
import com.curio.app.data.CategoryId
import com.curio.app.data.CurioCategories
import com.curio.app.data.CurioEntry
import com.curio.app.data.CurioRepositoryHolder
import com.curio.app.ui.components.CurioBackButton
import com.curio.app.ui.components.CurioEmptyState
import com.curio.app.ui.components.ScreenEntrance
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons

/**
 * Topic History — see CURIO_SPEC.md §13.5.
 *
 * Lists every topic the user has ever spun, grouped by day ("Today" /
 * "Yesterday" / "Last week" headers). Each row shows the topic name +
 * the category accent + a small format glyph + relative time.
 *
 * Backed by saved capture persistence so the history reflects real topics
 * the user has captured instead of placeholder samples.
 */
@Composable
fun TopicHistoryScreen(navController: NavController) {
    val entriesState = produceState<List<HistoryEntry>>(initialValue = emptyList()) {
        try {
            CurioRepositoryHolder.repo.observeAll().collect { savedEntries ->
                value = savedEntries.map { it.toHistoryEntry() }
            }
        } catch (_: Exception) {
            value = emptyList()
        }
    }
    val entries = entriesState.value
    val grouped = remember(entries) { entries.groupBy { it.dayLabel } }
    // v5.8 — saveable-backed: the history list keeps its scroll position
    // across rotation and nav-away/back.
    val listState = rememberLazyListState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // ── Top bar ───────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CurioBackButton(onClick = { navController.popBackStack() })
            Text(
                text = "Topic history",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (entries.isEmpty()) {
            CurioEmptyState(
                glyph = CurioIcons.History,
                headline = "No shuffles yet",
                subtext = "Shuffle the deck and your picks will appear here, grouped by day.",
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            )
            return
        }

        ScreenEntrance {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 16.dp,
                    vertical = 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                grouped.forEach { (dayLabel: String, dayEntries: List<HistoryEntry>) ->
                    item(key = "header_$dayLabel") {
                        Text(
                            text = dayLabel,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }
                    items(dayEntries, key = { historyEntry: HistoryEntry -> historyEntry.id }) { entry ->
                        HistoryRow(
                            entry = entry,
                            onClick = {
                                navController.navigate(
                                    com.curio.app.navigation.CurioRoutes.revealFor(
                                        entry.categoryId.routeSlug,
                                        entry.topicName
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(entry: HistoryEntry, onClick: () -> Unit) {
    val cat = CurioCategories.byId(entry.categoryId)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Category accent dot ──────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(cat.tint, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                CurioIcon(
                    name = cat.iconGlyph,
                    contentDescription = null,
                    tint = cat.accent,
                    size = 22.dp
                )
            }

            Spacer(Modifier.size(12.dp))

            // ── Topic name + format glyph + category ──────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.topicName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = cat.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = cat.accent
                )
            }

            // ── Relative time + format glyph ──────────────────────────────
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = entry.relativeTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                CurioIcon(
                    name = entry.formatGlyph,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    size = 16.dp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}


// ── Mock data for the placeholder phase ────────────────────────────────────
private data class HistoryEntry(
    val id: String,
    val topicName: String,
    val categoryId: CategoryId,
    val relativeTime: String,
    val dayLabel: String,
    val formatGlyph: String
)

private fun CurioEntry.toHistoryEntry(): HistoryEntry = HistoryEntry(
    id = id,
    topicName = topic.name,
    categoryId = topic.categoryId,
    relativeTime = relativeTime(capturedAtMillis),
    dayLabel = dayLabel(capturedAtMillis),
    formatGlyph = format.toGlyph()
)

private fun CaptureFormat.toGlyph(): String = when (this) {
    CaptureFormat.SoundBite -> CurioIcons.Mic
    CaptureFormat.ReelNotes -> CurioIcons.Movie
    CaptureFormat.Marginalia -> CurioIcons.MenuBook
    CaptureFormat.GalleryWall -> CurioIcons.Image
    CaptureFormat.FieldNotes -> CurioIcons.Science
    CaptureFormat.OpenNotebook -> CurioIcons.Edit
}

private fun dayLabel(timestamp: Long): String {
    val elapsed = (System.currentTimeMillis() - timestamp).coerceAtLeast(0L)
    val days = elapsed / (24L * 60L * 60L * 1000L)
    return when (days) {
        0L -> "Today"
        1L -> "Yesterday"
        in 2L..6L -> "This week"
        else -> "Earlier"
    }
}

private fun relativeTime(timestamp: Long): String {
    val elapsedMinutes = ((System.currentTimeMillis() - timestamp).coerceAtLeast(0L) / 60_000L)
    return when {
        elapsedMinutes < 1L -> "just now"
        elapsedMinutes < 60L -> "${elapsedMinutes} min ago"
        elapsedMinutes < 24L * 60L -> "${elapsedMinutes / 60L} hr ago"
        elapsedMinutes < 48L * 60L -> "yesterday"
        else -> "${elapsedMinutes / (24L * 60L)} days ago"
    }
}
