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
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.curio.app.data.CurioRepositoryHolder
import com.curio.app.navigation.CurioRoutes
import com.curio.app.navigation.navigateToTab
import com.curio.app.ui.components.CurioBackButton
import com.curio.app.ui.components.CurioEmptyState
import com.curio.app.ui.components.CurioEntryCard
import com.curio.app.ui.components.MorphEntrance
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.categoryBackgroundWash
import com.curio.app.ui.theme.categoryBorder
import com.curio.app.ui.theme.categoryChipSurface
import com.curio.app.ui.theme.categoryInk

/**
 * The Cabinet — see CURIO_SPEC.md §9. Library of saved captures.
 *
 * Upgraded with:
 *  - Entry cards render at once (no per-item stagger)
 *  - MorphEntrance for empty state content
 */
/**
 * Saves the active Cabinet filter chip by enum name; "All" (null) stays
 * null through an empty-string sentinel, surviving rotation and navigation.
 */
private val CategoryIdSaver = Saver<CategoryId?, String>(
    save = { it?.name ?: "" },
    restore = { name ->
        name.takeIf { it.isNotEmpty() }
            ?.let { n -> CategoryId.values().firstOrNull { it.name == n } }
    }
)

@Composable
fun CabinetScreen(navController: NavController) {
    var selectedFilter by rememberSaveable(stateSaver = CategoryIdSaver) { mutableStateOf<CategoryId?>(null) }
    // Saveable-backed scroll state — the grid keeps its position on rotation.
    val gridState = rememberLazyGridState()

    val entries by produceState<List<CurioEntry>>(initialValue = emptyList()) {
        try {
            CurioRepositoryHolder.repo.observeAll().collect { value = it }
        } catch (_: Exception) {
            value = emptyList()
        }
    }

    val visibleEntries = remember(entries, selectedFilter) {
        if (selectedFilter == null) entries
        else entries.filter { it.topic.categoryId == selectedFilter }
    }

    // The Cabinet wears the active filter's category wash — the same tinted
    // background as the filters page — ONLY while a category filter is
    // active. The "All" page stays on the plain theme background (like Home),
    // and the search button keeps its neutral look in every state.
    val filterCat = selectedFilter?.let { CurioCategories.byId(it) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(filterCat?.categoryBackgroundWash() ?: MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // ── Top bar ────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (selectedFilter != null) {
                    // Same top back button as the filters page — tapping it
                    // dismisses the active category filter back to "All".
                    CurioBackButton(onClick = { selectedFilter = null })
                }
                Text(
                    text = "The Cabinet",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Surface(
                onClick = { /* TODO Phase 4: expand search bar */ },
                shape = RoundedCornerShape(50),
                // Always neutral — the search button never wears the category
                // tint, only the page background does (when a filter is set).
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
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
        // Each category chip wears its OWN category tint in the background
        // (soft idle surface, brighter when active) — never the selected
        // filter's color, so tapping one chip can't re-tint the others.
        // The label text stays neutral in every state; only the background
        // carries the color. In dark mode the idle fill is desaturated
        // (less muddy) and the hairline picks up the light twin for
        // contrast. "All" keeps its plain neutral treatment.
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
                    ink = MaterialTheme.colorScheme.onPrimaryContainer,
                    chipSurface = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    chipBorder = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    selected = selectedFilter == null,
                    onClick = { selectedFilter = null }
                )
            }
            items(CurioCategories.visible) { cat ->
                FilterChipLite(
                    label = cat.displayName,
                    accent = cat.accent,
                    tint = cat.tint,
                    // The button (label text) never adapts to the category —
                    // it stays on the neutral theme ink in every state, so
                    // only the background carries the tint.
                    ink = MaterialTheme.colorScheme.onSurfaceVariant,
                    chipSurface = cat.categoryChipSurface(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    chipBorder = cat.categoryBorder(
                        fallback = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ),
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
                        subtext = "Everything you save will live here. Shuffle to find your first one.",
                        tint = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f),
                        ctaLabel = "Discover something",
                        onCtaClick = {
                            // Tab switch (not a plain push): Cabinet is itself
                            // a tab, so pushing spin on top of it would leave a
                            // hybrid back stack — back would walk into Cabinet
                            // and tab switches would pile up duplicates. Anchor
                            // to HOME like every other Spin launch in the app.
                            navController.navigateToTab(CurioRoutes.SPIN)
                        }
                    )
                } else {
                    val filterId = selectedFilter ?: CategoryId.WILDCARD
                    val cat = CurioCategories.byId(filterId)
                    CurioEmptyState(
                        glyph = CurioIcons.SearchOff,
                        headline = "No ${cat.displayName} captures yet",
                        subtext = "Shuffle for ${cat.displayName} to find your first one.",
                        tint = cat.categoryInk().copy(alpha = 0.4f),
                        ctaLabel = "Shuffle for ${cat.displayName}",
                        onCtaClick = {
                            // Same tab-switch contract as the "All" empty state
                            // (and Home's quest cards): anchor to HOME so the
                            // Shuffle tab replaces Cabinet instead of stacking
                            // a spin/… entry on top of the Cabinet tab entry.
                            navController.navigateToTab(
                                CurioRoutes.spinWithCategory(cat.id.routeSlug)
                            )
                        }
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(visibleEntries, key = { it.id }) { entry ->
                    CurioEntryCard(
                        entry = entry,
                        onClick = {
                            navController.navigate(
                                CurioRoutes.entryDetail(entry.id)
                            ) { launchSingleTop = true }
                        }
                    )
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
    ink: Color,
    chipSurface: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    chipBorder: BorderStroke? = null,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) tint else chipSurface,
        border = if (selected) null else chipBorder
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) ink else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}
