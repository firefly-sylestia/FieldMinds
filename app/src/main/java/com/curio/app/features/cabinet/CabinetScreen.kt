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
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.curio.app.data.MockTopics
import com.curio.app.navigation.CurioRoutes
import com.curio.app.ui.components.CurioEmptyState
import com.curio.app.ui.components.CurioEntryCard
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons

/**
 * The Cabinet — see CURIO_SPEC.md §9. Library of saved captures.
 *
 * Layout:
 *   - Top bar: "The Cabinet" title + search icon (Phase 4 wires search)
 *   - Filter chip row (All + 6 categories)
 *   - 2-col grid of CurioEntryCard, or empty state if filtered-empty
 *
 * Two empty states per §13.7:
 *   - Overall empty (no entries at all): "Your Cabinet is empty" → Spin CTA
 *   - Filtered empty: "No {Category} captures yet" → Spin-for-{Category} CTA
 */
@Composable
fun CabinetScreen(navController: NavController) {
    var selectedFilter by remember { mutableStateOf<CategoryId?>(null) }

    val entries = remember(selectedFilter) {
        if (selectedFilter == null) MockTopics.sampleEntries
        else MockTopics.sampleEntries.filter { it.topic.categoryId == selectedFilter }
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

        // ── Filter chip row ────────────────────────────────────────────────
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
        if (entries.isEmpty()) {
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
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(entries, key = { it.id }) { entry ->
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

/**
 * Lightweight filter chip used in the Cabinet row — separate from
 * CurioCategoryChip because this one is filter-only (no category glyph
 * required) and uses just text + accent color.
 */
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