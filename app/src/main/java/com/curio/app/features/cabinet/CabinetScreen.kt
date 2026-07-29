package com.curio.app.features.cabinet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.curio.app.data.CategoryId
import com.curio.app.data.CurioCategories
import com.curio.app.data.CurioEntry
import com.curio.app.data.TopicCatalog
import com.curio.app.navigation.CurioRoutes
import com.curio.app.ui.components.CurioEmptyState
import com.curio.app.ui.components.CurioEntryCard
import com.curio.app.ui.components.MorphEntrance
import com.curio.app.ui.components.StaggeredEntrance
import com.curio.app.ui.components.StaggeredItem
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.CurioMotion

/**
 * The Cabinet — see CURIO_SPEC.md §9. Library of saved captures.
 *
 * Upgraded with:
 *  - StaggeredEntrance for entry cards in the grid
 *  - MorphEntrance for empty state content
 */
@Composable
fun CabinetScreen(navController: NavController) {
    var selectedFilter by remember { mutableStateOf<CategoryId?>(null) }

    val entries by produceState<List<CurioEntry>>(initialValue = emptyList()) {
        value = TopicCatalog.sampleEntries()
    }

    val visibleEntries = remember(entries, selectedFilter) {
        if (selectedFilter == null) entries
        else entries.filter { it.topic.categoryId == selectedFilter }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // ── Top bar ────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "The Cabinet",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Surface(
                onClick = { /* TODO Phase 4: expand search bar */ },
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                CurioIcon(
                    name = CurioIcons.Search,
                    contentDescription = "Search captures",
                    tint = MaterialTheme.colorScheme.onSurface,
                    size = 24.dp,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        // ── Filter chip row ─────────────────────────────────────────────────
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            item("all") {
                FilterChipLite(
                    label = "All",
                    accent = MaterialTheme.colorScheme.primary,
                    tint = MaterialTheme.colorScheme.primaryContainer,
                    selected = selectedFilter == null,
                    onClick = { selectedFilter = null }
                )
            }
            items(CurioCategories.visible) { cat ->
                FilterChipLite(
                    label = cat.displayName,
                    accent = cat.accent,
                    tint = cat.tint,
                    selected = selectedFilter == cat.id,
                    onClick = { selectedFilter = cat.id }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Grid or empty state ────────────────────────────────────────────
        if (visibleEntries.isEmpty()) {
            MorphEntrance {
                if (selectedFilter == null) {
                    CurioEmptyState(
                        glyph = CurioIcons.Inventory2,
                        headline = "Your Cabinet is empty",
                        subtext = "Everything you save will live here. Spin to find your first one.",
                        tint = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f),
                        ctaLabel = "Discover something",
                        onCtaClick = { navController.navigate(CurioRoutes.SPIN) }
                    )
                } else {
                    val cat = CurioCategories.byId(selectedFilter!!)
                    CurioEmptyState(
                        glyph = CurioIcons.SearchOff,
                        headline = "No ${cat.displayName} captures yet",
                        subtext = "Spin for ${cat.displayName} to find your first one.",
                        tint = cat.accent.copy(alpha = 0.4f),
                        ctaLabel = "Spin for ${cat.displayName}",
                        onCtaClick = {
                            navController.navigate(
                                CurioRoutes.spinWithCategory(cat.id.routeSlug)
                            )
                        }
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(visibleEntries, key = { _, entry -> entry.id }) { index, entry ->
                    StaggeredItem(index = index, staggerDelayMs = CurioMotion.Stagger.Fast) {
                        CurioEntryCard(
                            entry = entry,
                            onClick = {
                                navController.navigate(
                                    CurioRoutes.entryDetail(entry.id)
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
private fun FilterChipLite(
    label: String,
    accent: Color,
    tint: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) tint else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) Color.Transparent
                    else MaterialTheme.colorScheme.outline
        )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}
