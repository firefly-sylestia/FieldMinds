package com.curio.app.features.picker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.curio.app.data.CurioCategories
import com.curio.app.navigation.CurioRoutes
import com.curio.app.ui.components.CurioBackButton
import com.curio.app.ui.components.CurioCategoryCard
import com.curio.app.ui.components.MorphEntrance
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons

/**
 * Full-screen Category Picker.
 *
 * Default (single-select): **tap a card to open that category in the Spin
 * page immediately** — like the original picker. **Tap and hold** a card to
 * enter multi-select mode; in that mode taps toggle selection (any number)
 * and a **Done** button appears (only then) to launch the Shuffle across
 * every chosen deck. Cards carry a topic count so as many decks as possible
 * fit on screen at once.
 */
@Composable
fun CategoryPickerScreen(navController: NavController) {
    val categories = remember { CurioCategories.visible }
    val gridState = rememberLazyGridState()
    // Null = not in multi-select mode (tap-to-open). Once set, cards toggle.
    var selectedSlugs by rememberSaveable { mutableStateOf(listOf<String>()) }
    var multiSelectMode by rememberSaveable { mutableStateOf(false) }

    val toggleSlug = { slug: String ->
        selectedSlugs = if (slug in selectedSlugs) selectedSlugs - slug else selectedSlugs + slug
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CurioBackButton(onClick = { navController.popBackStack() })
            Text(
                text = "What are we exploring?",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Text(
            text = if (multiSelectMode) {
                "Tap to toggle decks · Done to spin them together"
            } else {
                "Tap a deck to spin it · hold to pick several"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
        )

        MorphEntrance {
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(top = 4.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(categories) { cat ->
                    val slug = cat.id.routeSlug
                    CurioCategoryCard(
                        category = cat,
                        isSelected = multiSelectMode && slug in selectedSlugs,
                        onClick = {
                            if (multiSelectMode) {
                                toggleSlug(slug)
                            } else {
                                // Default: tap opens this category in Spin.
                                navController.navigate(CurioRoutes.spinWithCategory(slug)) {
                                    launchSingleTop = true
                                }
                            }
                        },
                        onLongClick = {
                            // Enter multi-select mode and select this card.
                            multiSelectMode = true
                            if (slug !in selectedSlugs) toggleSlug(slug)
                        }
                    )
                }
            }
        }

        if (multiSelectMode) {
            // ── Done row — only visible in multi-select mode ──────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        if (selectedSlugs.isEmpty()) return@Button
                        val slugs = selectedSlugs
                        navController.navigate(
                            if (slugs.size == 1) CurioRoutes.spinWithCategory(slugs.first())
                            else CurioRoutes.spinWithCategories(slugs)
                        ) { launchSingleTop = true }
                    },
                    enabled = selectedSlugs.isNotEmpty(),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    CurioIcon(CurioIcons.Check, null, size = 18.dp)
                    Text(
                        text = if (selectedSlugs.isEmpty()) "Done" else "Done · ${selectedSlugs.size}",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                TextButton(
                    onClick = {
                        // Exit multi-select mode; selection is discarded.
                        multiSelectMode = false
                        selectedSlugs = emptyList()
                    }
                ) {
                    Text(
                        "Cancel",
                        style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        }
    }
}
