package com.curio.app.features.home

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.graphics.Brush
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
 * Home — see CURIO_SPEC.md §3. Premium redesign.
 *
 * Layout (top to bottom):
 *   1. Glass-morphism top bar: ☰ Curio ✦  👤
 *   2. Welcome section: greeting + streak badge + quick stats
 *   3. Hero card: category-responsive gradient, glowing ring, floating sparkle
 *   4. Category quick-jump grid: 2-row compact cards
 *   5. Quick actions: Shuffle | Cabinet | History
 *   6. Recently explored section (real data from repository)
 */
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf<CurioCategory?>(null) }
    val streakDays = StreakTracker.getStreak(context)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Load recent captures from Room
    val recentEntries by produceState<List<CurioEntry>>(initialValue = emptyList()) {
        value = try { CurioRepositoryHolder.repo?.getAll()?.take(4) ?: emptyList() } catch (_: Exception) { emptyList() }
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
            // ═══════════════════════════════════════════════════════════
            // 1. Premium Glass-Morphism Top Bar
            // ═══════════════════════════════════════════════════════════
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                CurioColors.CoralBlush.copy(alpha = 0.08f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // ☰ Hamburger
                    Surface(
                        onClick = { scope.launch { drawerState.open() } },
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        CurioIcon(
                            name = CurioIcons.Menu,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.onSurface,
                            size = 24.dp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    // Brand center
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Curio",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        CurioIcon(
                            name = CurioIcons.AutoAwesome,
                            contentDescription = null,
                            tint = CurioColors.CoralBlush,
                            size = 18.dp
                        )
                    }

                    // 👤 Avatar with gradient ring
                    Surface(
                        onClick = { navController.navigate(CurioRoutes.PROFILE) },
                        shape = CircleShape,
                        color = Color.Transparent,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.sweepGradient(
                                    listOf(
                                        CurioColors.CoralBlush,
                                        CurioColors.Peach,
                                        CurioColors.CoralBlush
                                    )
                                )
                            )
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            CurioIcon(
                                name = CurioIcons.Person,
                                contentDescription = "Profile",
                                tint = MaterialTheme.colorScheme.onSurface,
                                size = 18.dp
                            )
                        }
                    }
                }
            }

            // ═══════════════════════════════════════════════════════════
            // 2. Welcome Section
            // ═══════════════════════════════════════════════════════════
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                MorphEntrance(delayMs = 80) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = greetingForNow(),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CurioStreakPill(days = streakDays)
                            if (streakDays <= 0) {
                                Text(
                                    text = "Discover something new today ✦",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.weight(1f))
                            // Quick stat chips
                            if (recentEntries.isNotEmpty()) {
                                MiniStatBadge(
                                    icon = CurioIcons.Inventory2,
                                    text = "${recentEntries.size} saved",
                                    tint = CurioColors.Sage
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ═══════════════════════════════════════════════════════════
            // 3. Premium Hero Card
            // ═══════════════════════════════════════════════════════════
            MorphEntrance(delayMs = 120) {
                PremiumHeroCard(
                    selectedCategory = selectedCategory,
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

            // ═══════════════════════════════════════════════════════════
            // 4. Category Quick-Jump Grid
            // ═══════════════════════════════════════════════════════════
            StaggeredEntrance(staggerDelayMs = CurioMotion.Stagger.Fast) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    StaggeredItem(index = 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Explore by category",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            if (selectedCategory != null) {
                                Surface(
                                    onClick = { selectedCategory = null },
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = "Clear",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                    StaggeredItem(index = 1) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            userScrollEnabled = false,
                            modifier = Modifier
                                .height(280.dp)
                                .fillMaxWidth()
                        ) {
                            item("wildcard") {
                                CategoryMiniCard(
                                    label = "Surprise",
                                    glyph = CurioIcons.Casino,
                                    accent = CurioColors.CoralBlush,
                                    tint = CurioColors.CoralBlush.copy(alpha = 0.15f),
                                    selected = selectedCategory?.id == CategoryId.WILDCARD,
                                    onClick = {
                                        selectedCategory = if (selectedCategory?.id == CategoryId.WILDCARD) null
                                        else CurioCategories.byId(CategoryId.WILDCARD)
                                    }
                                )
                            }
                            itemsIndexed(CurioCategories.visible) { _, cat ->
                                if (cat.id != CategoryId.WILDCARD) {
                                    CategoryMiniCard(
                                        label = cat.displayName,
                                        glyph = cat.iconGlyph,
                                        accent = cat.accent,
                                        tint = cat.tint,
                                        selected = selectedCategory?.id == cat.id,
                                        onClick = {
                                            selectedCategory = if (selectedCategory?.id == cat.id) null else cat
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ═══════════════════════════════════════════════════════════
            // 5. Quick Actions Row
            // ═══════════════════════════════════════════════════════════
            StaggeredEntrance(staggerDelayMs = CurioMotion.Stagger.Fast) {
                StaggeredItem(index = 0) {
                    Text(
                        text = "Quick actions",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                }
                StaggeredItem(index = 1) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            QuickActionCard(
                                icon = CurioIcons.AutoAwesome,
                                label = "Shuffle",
                                subtitle = "Spin now",
                                accent = CurioColors.CoralBlush,
                                onClick = {
                                    val chosen = selectedCategory
                                    if (chosen == null) navController.navigate(CurioRoutes.SPIN)
                                    else navController.navigate(CurioRoutes.spinWithCategory(chosen.id.routeSlug))
                                }
                            )
                        }
                        item {
                            QuickActionCard(
                                icon = CurioIcons.Inventory2,
                                label = "Cabinet",
                                subtitle = "Your saves",
                                accent = CurioColors.Sage,
                                onClick = { navController.navigate(CurioRoutes.CABINET) }
                            )
                        }
                        item {
                            QuickActionCard(
                                icon = CurioIcons.History,
                                label = "History",
                                subtitle = "Past topics",
                                accent = CurioColors.DustyBlue,
                                onClick = { navController.navigate(CurioRoutes.TOPIC_HISTORY) }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ═══════════════════════════════════════════════════════════
            // 6. Recently Explored
            // ═══════════════════════════════════════════════════════════
            StaggeredEntrance(staggerDelayMs = CurioMotion.Stagger.Base) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    StaggeredItem(index = 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recently explored",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            if (recentEntries.isNotEmpty()) {
                                Surface(
                                    onClick = { navController.navigate(CurioRoutes.CABINET) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.Transparent
                                ) {
                                    Text(
                                        text = "See all",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(4.dp)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                    StaggeredItem(index = 1) {
                        if (recentEntries.isEmpty()) {
                            RecentEmptyState(
                                selectedCategory = selectedCategory,
                                onSpin = {
                                    val chosen = selectedCategory
                                    if (chosen == null) navController.navigate(CurioRoutes.PICKER)
                                    else navController.navigate(CurioRoutes.spinWithCategory(chosen.id.routeSlug))
                                }
                            )
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
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
// Premium Hero Card
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun PremiumHeroCard(
    selectedCategory: CurioCategory?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pressed by remember { mutableStateOf(false) }
    val isWildcard = selectedCategory?.id == CategoryId.WILDCARD
    val activeAccent = when {
        isWildcard -> CurioColors.CoralBlush
        selectedCategory != null -> selectedCategory.accent
        else -> CurioColors.CoralBlush
    }
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = CurioMotion.Springs.Press,
        label = "heroPress"
    )

    Surface(
        onClick = {
            pressed = true
            onClick()
        },
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .scale(pressScale),
        shape = RoundedCornerShape(32.dp),
        color = activeAccent,
        shadowElevation = 12.dp,
        tonalElevation = 2.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Gradient overlay for depth
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.25f),
                                Color.Transparent,
                                activeAccent.copy(alpha = 0.3f)
                            )
                        ),
                        RoundedCornerShape(32.dp)
                    )
            )

            // Large watermarked category glyph
            CurioIcon(
                name = selectedCategory?.iconGlyph ?: CurioIcons.AutoAwesome,
                contentDescription = null,
                tint = CurioColors.DeepPlum.copy(alpha = 0.08f),
                size = 160.dp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 8.dp, bottom = 8.dp)
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top row: label + sparkle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CurioColors.DeepPlum.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (selectedCategory != null) "Let's go" else "Discover",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = CurioColors.DeepPlum,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    CurioIcon(
                        name = CurioIcons.AutoAwesome,
                        contentDescription = null,
                        tint = CurioColors.ButterYellow.copy(alpha = 0.7f),
                        size = 22.dp
                    )
                }

                // Bottom content
                Column {
                    Text(
                        text = when {
                            isWildcard -> "Surprise me!"
                            selectedCategory != null -> selectedCategory.displayName
                            else -> "Spin the wheel"
                        },
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = CurioColors.DeepPlum,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = when {
                            selectedCategory != null -> "Shuffle for a new discovery"
                            else -> "Tap to discover something new"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = CurioColors.DeepPlum.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }

    LaunchedEffect(pressed) {
        if (pressed) {
            kotlinx.coroutines.delay(300)
            pressed = false
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// Category Mini Card (grid)
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun CategoryMiniCard(
    label: String,
    glyph: String,
    accent: Color,
    tint: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.04f else 1f,
        animationSpec = CurioMotion.Springs.Snappy,
        label = "catCardScale"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) tint else MaterialTheme.colorScheme.surface,
        shadowElevation = if (selected) 4.dp else 1.dp,
        tonalElevation = if (selected) 2.dp else 0.dp,
        modifier = Modifier.scale(scale)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (selected) accent.copy(alpha = 0.15f)
                        else tint
                    ),
                contentAlignment = Alignment.Center
            ) {
                CurioIcon(
                    name = glyph,
                    contentDescription = label,
                    tint = if (selected) accent else accent.copy(alpha = 0.6f),
                    size = 22.dp
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// Quick Action Card
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun QuickActionCard(
    icon: String,
    label: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = accent.copy(alpha = 0.08f),
        modifier = Modifier.width(130.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                CurioIcon(
                    name = icon,
                    contentDescription = label,
                    tint = accent,
                    size = 22.dp
                )
            }
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// Mini Stat Badge
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun MiniStatBadge(
    icon: String,
    text: String,
    tint: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = tint.copy(alpha = 0.10f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CurioIcon(name = icon, contentDescription = null, tint = tint, size = 14.dp)
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = tint
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// Recent Entry Card
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun RecentEntryCard(
    entry: CurioEntry,
    onClick: () -> Unit
) {
    val cat = CurioCategories.byId(entry.topic.categoryId)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        tonalElevation = 1.dp,
        modifier = Modifier.width(160.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(cat.accent)
                )
                Text(
                    text = cat.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = cat.accent,
                    maxLines = 1
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = entry.topic.name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = entry.bodyPreview,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// Recent Empty State
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun RecentEmptyState(
    selectedCategory: CurioCategory?,
    onSpin: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CurioIcon(
                name = CurioIcons.AutoAwesome,
                contentDescription = null,
                tint = CurioColors.CoralBlush.copy(alpha = 0.4f),
                size = 40.dp
            )
            Text(
                text = "Your journey starts here",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Spin the wheel to discover your first topic — then capture what you find ✦",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Surface(
                onClick = onSpin,
                shape = RoundedCornerShape(24.dp),
                color = CurioColors.CoralBlush
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CurioIcon(
                        name = CurioIcons.Casino,
                        contentDescription = null,
                        tint = CurioColors.DeepPlum,
                        size = 16.dp
                    )
                    Text(
                        text = if (selectedCategory != null) "Spin ${selectedCategory.displayName}"
                               else "Pick a category & spin",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = CurioColors.DeepPlum
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// Navigation Drawer (kept from previous version)
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun HomeDrawerContent(onNavigate: (String) -> Unit) {
    ModalDrawerSheet(
        modifier = Modifier.width(300.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface
    ) {
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
                CurioIcon(CurioIcons.AutoAwesome, null, tint = CurioColors.CoralBlush, size = 32.dp)
                Spacer(Modifier.height(8.dp))
                Text("Curio", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.onSurface)
                Text("Stay curious", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(8.dp))
        LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            item { DrawerNavItem(CurioIcons.Person, "Profile & Settings") { onNavigate(CurioRoutes.PROFILE) } }
            item { DrawerNavItem(CurioIcons.History, "Topic History") { onNavigate(CurioRoutes.TOPIC_HISTORY) } }
            item { DrawerNavItem(CurioIcons.DragHandle, "Manage Categories") { onNavigate(CurioRoutes.MANAGE_CATEGORIES) } }
            item {
                DrawerNavItem(CurioIcons.Replay, "Replay Intro") {
                    com.curio.app.features.onboarding.CurioOnboardingState.isComplete = false
                    onNavigate(CurioRoutes.ONBOARDING)
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text("Curio v1.0.0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            Text("Made with curiosity ✦", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
        }
    }
}

@Composable
private fun DrawerNavItem(icon: String, label: String, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(14.dp), color = Color.Transparent, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            CurioIcon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 22.dp)
            Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

/** Returns a time-aware greeting. */
private fun greetingForNow(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11  -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else      -> "Welcome back"
    }
}
