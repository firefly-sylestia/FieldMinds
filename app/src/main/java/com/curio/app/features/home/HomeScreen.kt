package com.curio.app.features.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.curio.app.data.CategoryId
import com.curio.app.data.CurioCategories
import com.curio.app.data.CurioCategory
import com.curio.app.data.CurioEntry
import com.curio.app.data.CurioRepositoryHolder
import com.curio.app.data.StreakTracker
import com.curio.app.navigation.CurioRoutes
import com.curio.app.ui.components.CurioStreakPill
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
 * Home — clean, minimal, no gradients.
 *
 * Layout (top to bottom):
 *   1. Compact top bar: ☰  Curio ✦  👤
 *   2. Welcome: greeting + streak + quick stats
 *   3. Compact hero banner: selected category + shuffle CTA
 *   4. Category grid: icon-only color pills
 *   5. Recently explored
 */
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf<CurioCategory?>(null) }
    val streakDays = StreakTracker.getStreak(context)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val recentEntries by produceState<List<CurioEntry>>(initialValue = emptyList()) {
        value = try { CurioRepositoryHolder.repo.getAll().take(4) } catch (e: Exception) {
            android.util.Log.e("HomeScreen", "Failed to load recent entries", e)
            emptyList()
        }
    }

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
        gesturesEnabled = drawerState.isOpen || drawerState.isAnimationRunning
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            // ── 1. Top Bar ───────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    onClick = { scope.launch { drawerState.open() } },
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    CurioIcon(
                        CurioIcons.Menu, "Menu",
                        tint = MaterialTheme.colorScheme.onSurface,
                        size = 24.dp, modifier = Modifier.padding(8.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Curio",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    CurioIcon(CurioIcons.AutoAwesome, null, tint = CurioColors.CoralBlush, size = 18.dp)
                }

                // Flat avatar — no gradient ring
                Surface(
                    onClick = { navController.navigate(CurioRoutes.PROFILE) },
                    shape = CircleShape,
                    color = CurioColors.CoralBlush.copy(alpha = 0.15f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        CurioIcon(CurioIcons.Person, "Profile", tint = CurioColors.CoralBlush, size = 20.dp)
                    }
                }
            }

            // ── 2. Welcome ───────────────────────────────────────────
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
                MorphEntrance(delayMs = 80) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            greetingForNow(),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CurioStreakPill(days = streakDays)
                            if (streakDays <= 0) {
                                Text(
                                    "Discover something new today",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.weight(1f))
                            if (recentEntries.isNotEmpty()) {
                                Surface(shape = RoundedCornerShape(12.dp), color = CurioColors.Sage.copy(alpha = 0.10f)) {
                                    Row(
                                        Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        CurioIcon(CurioIcons.Inventory2, null, tint = CurioColors.Sage, size = 14.dp)
                                        Text("${recentEntries.size} saved", style = MaterialTheme.typography.labelSmall, color = CurioColors.Sage)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── 3. Compact Hero Banner ───────────────────────────────
            MorphEntrance(delayMs = 100) {
                val accent = selectedCategory?.accent ?: CurioColors.CoralBlush
                val chosen = selectedCategory
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = accent.copy(alpha = 0.12f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                chosen?.displayName ?: "Discover something",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                if (chosen != null) "Shuffle for a topic" else "Pick a category to get started",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            onClick = {
                                if (chosen == null) navController.navigate(CurioRoutes.PICKER)
                                else navController.navigate(CurioRoutes.spinWithCategory(chosen.id.routeSlug))
                            },
                            shape = RoundedCornerShape(50),
                            color = accent
                        ) {
                            Row(
                                Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                CurioIcon(
                                    if (chosen != null) CurioIcons.Casino else CurioIcons.ArrowForward,
                                    null,
                                    tint =                                    if (chosen != null) CurioColors.DeepPlum else Color.White,
                                    size = 16.dp
                                )
                                Text(
                                    if (chosen != null) "Shuffle" else "Start",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color =                                    if (chosen != null) CurioColors.DeepPlum else Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // ── 4. Category Grid ─────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    "Explore by category",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(12.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    userScrollEnabled = false,
                    modifier = Modifier.height(200.dp).fillMaxWidth()
                ) {
                    item("wildcard") {
                        CategoryPill(
                            label = "Surprise", glyph = CurioIcons.Casino, accent = CurioColors.CoralBlush,
                            selected = selectedCategory?.id == CategoryId.WILDCARD,
                            onClick = {
                                selectedCategory = if (selectedCategory?.id == CategoryId.WILDCARD) null
                                else CurioCategories.byId(CategoryId.WILDCARD)
                            }
                        )
                    }
                    itemsIndexed(CurioCategories.visible) { _, cat ->
                        if (cat.id != CategoryId.WILDCARD) {
                            CategoryPill(
                                label = cat.displayName, glyph = cat.iconGlyph, accent = cat.accent,
                                selected = selectedCategory?.id == cat.id,
                                onClick = { selectedCategory = if (selectedCategory?.id == cat.id) null else cat }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── 5. Recently Explored ─────────────────────────────────
            StaggeredEntrance(staggerDelayMs = CurioMotion.Stagger.Base) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    StaggeredItem(index = 0) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Recently explored",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            if (recentEntries.isNotEmpty()) {
                                Surface(
                                    onClick = { navController.navigate(CurioRoutes.CABINET) },
                                    shape = RoundedCornerShape(12.dp), color = Color.Transparent
                                ) {
                                    Text(
                                        "See all", style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(4.dp)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                    StaggeredItem(index = 1) {
                        if (recentEntries.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    Modifier.fillMaxWidth().padding(28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    CurioIcon(CurioIcons.AutoAwesome, null, tint = CurioColors.CoralBlush.copy(alpha = 0.4f), size = 40.dp)
                                    Text(
                                        "Your journey starts here",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center
                                    )
                                    Text(
                                        "Spin the wheel to discover your first topic — then capture what you find",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center
                                    )
                                    val sel = selectedCategory
                                    Surface(
                                        onClick = {
                                            if (sel == null) navController.navigate(CurioRoutes.PICKER)
                                            else navController.navigate(CurioRoutes.spinWithCategory(sel.id.routeSlug))
                                        },
                                        shape = RoundedCornerShape(24.dp), color = CurioColors.CoralBlush
                                    ) {
                                        Row(
                                            Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            CurioIcon(CurioIcons.Casino, null, tint = CurioColors.DeepPlum, size = 16.dp)
                                            Text(
                                                if (sel != null) "Spin ${sel.displayName}" else "Pick a category & spin",
                                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                                color = CurioColors.DeepPlum
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(recentEntries, key = { it.id }) { entry ->
                                    RecentEntryCard(
                                        entry = entry,
                                        onClick = { navController.navigate(CurioRoutes.entryDetail(entry.id)) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// Category Pill
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun CategoryPill(label: String, glyph: String, accent: Color, selected: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.10f else 1f,
        animationSpec = CurioMotion.Springs.Snappy, label = "catPillScale"
    )
    Surface(
        onClick = onClick, shape = RoundedCornerShape(50), color = accent,
        shadowElevation = if (selected) 6.dp else 2.dp,
        modifier = Modifier.scale(scale)
    ) {
        Box(Modifier.size(52.dp), contentAlignment = Alignment.Center) {
            CurioIcon(glyph, label, tint = Color.White, size = 24.dp)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// Recent Entry Card
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun RecentEntryCard(entry: CurioEntry, onClick: () -> Unit) {
    val cat = CurioCategories.byId(entry.topic.categoryId)
    Surface(
        onClick = onClick, shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp, tonalElevation = 1.dp,
        modifier = Modifier.width(160.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(cat.accent))
                Text(cat.displayName, style = MaterialTheme.typography.labelSmall, color = cat.accent, maxLines = 1)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                entry.topic.name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                entry.bodyPreview,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// Navigation Drawer — flat, no gradients
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun HomeDrawerContent(onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    ModalDrawerSheet(
        modifier = Modifier.width(300.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(CurioColors.CoralBlush.copy(alpha = 0.08f))
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column {
                CurioIcon(CurioIcons.AutoAwesome, null, tint = CurioColors.CoralBlush, size = 32.dp)
                Spacer(Modifier.height(8.dp))
                Text("Curio", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.onSurface)
                Text("Stay curious", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.weight(1f).padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            item { DrawerNavItem(CurioIcons.Person, "Profile & Settings") { onNavigate(CurioRoutes.PROFILE) } }
            item { DrawerNavItem(CurioIcons.History, "Topic History") { onNavigate(CurioRoutes.TOPIC_HISTORY) } }
            item { DrawerNavItem(CurioIcons.DragHandle, "Manage Categories") { onNavigate(CurioRoutes.MANAGE_CATEGORIES) } }
            item {
                DrawerNavItem(CurioIcons.Replay, "Replay Intro") {
                    com.curio.app.features.onboarding.CurioOnboardingState.reset(context)
                    onNavigate(CurioRoutes.ONBOARDING)
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text("v1.0.0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            Text("Made with curiosity", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
        }
    }
}

@Composable
private fun DrawerNavItem(icon: String, label: String, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(14.dp), color = Color.Transparent, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            CurioIcon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 22.dp)
            Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

private fun greetingForNow(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) { in 5..11 -> "Good morning"; in 12..16 -> "Good afternoon"; in 17..21 -> "Good evening"; else -> "Welcome back" }
}
