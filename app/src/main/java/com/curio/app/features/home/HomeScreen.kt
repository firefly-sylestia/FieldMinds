package com.curio.app.features.home

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.curio.app.data.CategoryId
import com.curio.app.data.CurioCategories
import com.curio.app.data.CurioCategory
import com.curio.app.navigation.CurioRoutes
import com.curio.app.ui.components.CurioCategoryChip
import com.curio.app.ui.components.CurioEmptyState
import com.curio.app.ui.components.CurioHeroSpinCard
import com.curio.app.ui.components.CurioStreakPill
import com.curio.app.ui.components.CurioWildcardChip
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import java.util.Calendar

/**
 * Home — see CURIO_SPEC.md §3.
 *
 * Layout (top to bottom):
 *   1. Top bar: ☰  Curio              👤    (transparent, no elevation)
 *   2. Greeting row: "Good morning, Alex" + streak pill (if any)
 *   3. Hero Spin card (~40% vertical, the single largest tap target)
 *   4. "Pick a category" chip row (horizontally scrollable)
 *   5. "Recently explored" section
 *      - Empty state from §13.7 (no entries in placeholder phase)
 *   6. (Bottom nav is rendered by the parent scaffold, not here)
 *
 * Behavior notes:
 *   - Tapping the hero card with NO chip selected → Category Picker
 *   - Tapping the hero card WITH a chip selected → The Spin pre-loaded
 *   - Tapping a chip toggles selection
 *   - Time-aware greeting falls back to "Welcome back" when name unknown
 *   - Streak pill is hidden when days <= 0
 */
@Composable
fun HomeScreen(navController: NavController) {
    var selectedCategory by remember { mutableStateOf<CurioCategory?>(null) }
    val streakDays = 0 // placeholder phase — logic wires this in next

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
    ) {
        // ── Top bar ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CurioIcon(
                name = CurioIcons.Menu,
                contentDescription = "Open navigation",
                tint = MaterialTheme.colorScheme.onSurface,
                size = 28.dp
            )
            Text(
                text = "Curio",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            CurioIcon(
                name = CurioIcons.Person,
                contentDescription = "Profile",
                tint = MaterialTheme.colorScheme.onSurface,
                size = 28.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(6.dp)
            )
        }

        // ── Greeting + streak pill ───────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = greetingForNow(),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            CurioStreakPill(days = streakDays)
        }

        Spacer(Modifier.height(16.dp))

        // ── Hero Spin card ───────────────────────────────────────────────────
        CurioHeroSpinCard(
            selectedCategory = selectedCategory,
            wildcardSelected = selectedCategory?.id == CategoryId.WILDCARD,
            onClick = {
                val chosen = selectedCategory
                if (chosen == null) {
                    navController.navigate(CurioRoutes.PICKER)
                } else {
                    navController.navigate(CurioRoutes.spinWithCategory(chosen.id.routeSlug))
                }
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(24.dp))

        // ── Pick a category chip row ─────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "Pick a category",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                item("surprise-me") {
                    CurioWildcardChip(
                        selected = selectedCategory?.id == CategoryId.WILDCARD,
                        onClick = {
                            selectedCategory =
                                if (selectedCategory?.id == CategoryId.WILDCARD) null
                                else CurioCategories.byId(CategoryId.WILDCARD)
                        }
                    )
                }
                items(CurioCategories.visible.size - 1) { index ->
                    val cat = CurioCategories.visible[index]
                    CurioCategoryChip(
                        category = cat,
                        selected = selectedCategory?.id == cat.id,
                        onClick = {
                            selectedCategory =
                                if (selectedCategory?.id == cat.id) null
                                else cat
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        // ── Recently explored section ────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "Recently explored",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(12.dp))
            CurioEmptyState(
                glyph = CurioIcons.AutoAwesome,
                headline = "Nothing here yet",
                subtext = "Give the wheel a spin — your first discovery is one tap away.",
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                ctaLabel = "Spin the wheel",
                onCtaClick = {
                    val chosen = selectedCategory
                    if (chosen == null) {
                        navController.navigate(CurioRoutes.PICKER)
                    } else {
                        navController.navigate(CurioRoutes.spinWithCategory(chosen.id.routeSlug))
                    }
                },
                modifier = Modifier.height(180.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        // ── Settings shortcut (low-emphasis; Settings is also reachable from
        //     the top-right Person icon in a future iteration — Phase 3)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Surface(
                onClick = { navController.navigate(CurioRoutes.SETTINGS) },
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CurioIcon(
                        name = CurioIcons.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        size = 18.dp
                    )
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/** Returns a time-aware greeting string for the home screen. */
private fun greetingForNow(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11  -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else      -> "Welcome back"
    }
}
