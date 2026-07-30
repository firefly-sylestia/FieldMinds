package com.curio.app.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
import com.curio.app.ui.components.MorphEntrance
import com.curio.app.ui.components.StaggeredEntrance
import com.curio.app.ui.components.StaggeredItem
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.CurioMotion
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Home — see CURIO_SPEC.md §3.
 *
 * Upgraded with:
 *  - MorphEntrance wrapping the hero card for dramatic first appearance
 *  - StaggeredEntrance for the chip row + recently explored section
 *  - Breathing hero card (ambient pulse on background glyph)
 *  - Time-aware greeting
 *
 * Layout (top to bottom):
 *   1. Top bar: ☰  Curio              👤    (transparent, no elevation)
 *   2. Greeting row: "Good morning, Alex" + streak pill (if any)
 *   3. Hero Spin card (~40% vertical, the single largest tap target)
 *   4. "Pick a category" chip row (horizontally scrollable)
 *   5. "Recently explored" section
 *      - Empty state from §13.7 (no entries in placeholder phase)
 *   6. (Bottom nav is rendered by the parent scaffold, not here)
 */
@Composable
fun HomeScreen(navController: NavController) {
    var selectedCategory by remember { mutableStateOf<CurioCategory?>(null) }
    val streakDays = 0
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            HomeDrawerContent(
                onNavigate = { route ->
                    scope.launch { drawerState.close() }
                    navController.navigate(route)
                }
            )
        },
        // Disable swipe-to-open when closed to prevent conflicts with
        // the vertical scrollable home content. Drawer opens via ☰ icon only.
        gesturesEnabled = drawerState.isOpen || drawerState.isAnimationRunning
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp)
        ) {
            // ── Top bar ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    onClick = { scope.launch { drawerState.open() } },
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Transparent
                ) {
                    CurioIcon(
                        name = CurioIcons.Menu,
                        contentDescription = "Open navigation menu",
                        tint = MaterialTheme.colorScheme.onSurface,
                        size = 28.dp,
                        modifier = Modifier.padding(4.dp)
                    )
                }
                Text(
                    text = "Curio",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Surface(
                    onClick = { navController.navigate(CurioRoutes.PROFILE) },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    CurioIcon(
                        name = CurioIcons.Person,
                        contentDescription = "Profile",
                        tint = MaterialTheme.colorScheme.onSurface,
                        size = 22.dp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
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

        // ── Hero Spin card — morph entrance ──────────────────────────────────
        MorphEntrance {
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
        }

        Spacer(Modifier.height(24.dp))

        // ── Pick a category chip row — staggered entrance ────────────────────
        StaggeredEntrance(staggerDelayMs = CurioMotion.Stagger.Fast) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                StaggeredItem(index = 0) {
                    Text(
                        text = "Pick a category",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(12.dp))
                }
                StaggeredItem(index = 1) {
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
            }
        }

        Spacer(Modifier.height(28.dp))

        // ── Recently explored section ────────────────────────────────────────
        StaggeredEntrance(staggerDelayMs = CurioMotion.Stagger.Base) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                StaggeredItem(index = 0) {
                    Text(
                        text = "Recently explored",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(12.dp))
                }
                StaggeredItem(index = 1) {
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
            }
        }

        Spacer(Modifier.height(32.dp))
    }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Navigation drawer content
// ═══════════════════════════════════════════════════════════════════════════

/**
 * The slide-in navigation drawer accessible from the ☰ hamburger icon.
 *
 * Sections:
 *   - Header: Curio brand + greeting
 *   - Navigation items: Profile, Topic History, Manage Categories
 *   - Bottom: About / version info
 */
@Composable
private fun HomeDrawerContent(
    onNavigate: (String) -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(300.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface
    ) {
        // ── Drawer header ────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            CurioColors.CoralBlush.copy(alpha = 0.15f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column {
                CurioIcon(
                    name = CurioIcons.AutoAwesome,
                    contentDescription = null,
                    tint = CurioColors.CoralBlush,
                    size = 32.dp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Curio",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Stay curious",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(8.dp))

        // ── Navigation items ─────────────────────────────────────────────
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            item {
                DrawerNavItem(
                    icon = CurioIcons.Person,
                    label = "Profile & Settings",
                    onClick = { onNavigate(CurioRoutes.PROFILE) }
                )
            }
            item {
                DrawerNavItem(
                    icon = CurioIcons.History,
                    label = "Topic History",
                    onClick = { onNavigate(CurioRoutes.TOPIC_HISTORY) }
                )
            }
            item {
                DrawerNavItem(
                    icon = CurioIcons.DragHandle,
                    label = "Manage Categories",
                    onClick = { onNavigate(CurioRoutes.MANAGE_CATEGORIES) }
                )
            }
            item {
                DrawerNavItem(
                    icon = CurioIcons.Replay,
                    label = "Replay Intro",
                    onClick = {
                        com.curio.app.features.onboarding.CurioOnboardingState.isComplete = false
                        onNavigate(CurioRoutes.ONBOARDING)
                    }
                )
            }
        }

        // ── Bottom section ───────────────────────────────────────────────
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Curio v1.0.0",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Text(
                text = "Made with curiosity ✦",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun DrawerNavItem(
    icon: String,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CurioIcon(
                name = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                size = 22.dp
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
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
