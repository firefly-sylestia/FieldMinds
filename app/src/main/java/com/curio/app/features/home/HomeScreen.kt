package com.curio.app.features.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.curio.app.data.AppPreferences
import com.curio.app.data.CategoryId
import com.curio.app.data.CurioCategories
import com.curio.app.data.CurioCategory
import com.curio.app.data.CurioEntry
import com.curio.app.data.CurioRepositoryHolder
import com.curio.app.data.StreakTracker
import com.curio.app.navigation.CurioRoutes
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.CurioMotion
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Home — clean, minimal, personalized.
 *
 * Layout (top to bottom), tuned for 360×800 dp:
 *   1. **Minimal top bar** — only the avatar pill on the right; tapping
 *      it opens the Profile drawer menu.
 *   2. **Greeting hero** — large personalized greeting using the user's
 *      display name (e.g. "Good afternoon, Alex"). Subtitle row with a
 *      small streak badge if active.
 *   3. **Quest card** — a flatter, more legible hero card. When a category
 *      is selected it shows the chosen category's accent + CTA "Spin for
 *      $name". Otherwise it shows the wildcard + a "Spin" CTA. Re-uses
 *   4. **Stats strip** — four compact stat pills: Streak · Saved · Recent
 *      · Categories.
 *   5. **Categories chip row** — horizontally-scrollable color chips.
 *      Each chip shows the family color, category glyph, and label.
 *      Tapping sets the active category.
 *   6. **Recently explored** — header row + 2-col grid of recent entry
 *      cards, or a beautiful empty-state card prompting the first spin.
 *   7. **Reminder CTA** (only when reminder is OFF) — a subtle ghost-style
 *      card suggesting the user try a daily spin reminder, navigating to
 *      Settings.
 *
 *  The screen still hosts the `ModalNavigationDrawer` for secondary
 *  navigation (Profile, History, Manage Categories, Replay Intro).
 *
 *  Top paddings tightened: `statusBarsPadding()` + `vertical = 4dp` for the
 *  bar, `vertical = 6dp` between sections — keeps the "no empty top"
 *  guarantee we established in Spin/TopicReveal.
 */
/**
 * Saves Home's selected category chip by enum name. The wildcard "Surprise"
 * state is `null` and round-trips through an empty-string sentinel, so
 * rotating the device or navigating away/back keeps the chip selection.
 */
private val CategorySaver = Saver<CurioCategory?, String>(
    save = { it?.id?.name ?: "" },
    restore = { name ->
        name.takeIf { it.isNotEmpty() }
            ?.let { n -> CategoryId.values().firstOrNull { it.name == n } }
            ?.let { CurioCategories.byId(it) }
    }
)

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val displayName = remember { AppPreferences.getDisplayName(context) }
    var selectedCategory by rememberSaveable(stateSaver = CategorySaver) { mutableStateOf<CurioCategory?>(null) }
    val streakDays = StreakTracker.getStreak(context)
    val reminderEnabled by AppPreferences.reminderEnabledState
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val recentEntries by produceState<List<CurioEntry>>(initialValue = emptyList()) {
        try {
            value = CurioRepositoryHolder.repo.getAll().take(4)
        } catch (_: Exception) {
            value = emptyList()
        }
    }
    var totalSaved by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        try {
            totalSaved = CurioRepositoryHolder.repo.count()
        } catch (_: Exception) {}
    }

    val navInsets = WindowInsets.navigationBars.asPaddingValues()

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
            // ── 1. Minimal top bar — reduced padding, refined icons ─────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Refined menu button with better icon
                Surface(
                    onClick = { scope.launch { drawerState.open() } },
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        CurioIcon(
                            CurioIcons.Menu, "Open menu",
                            tint = MaterialTheme.colorScheme.onSurface,
                            size = 22.dp
                        )
                    }
                }
                // Refined avatar pill with better styling
                Surface(
                    onClick = { navController.navigate(CurioRoutes.PROFILE) },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(2.dp, CurioColors.CoralBlush),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        CurioIcon(
                            CurioIcons.Person, "Profile",
                            tint = CurioColors.CoralBlush,
                            size = 22.dp
                        )
                    }
                }
            }

            // ── 2. Greeting hero ────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                Text(
                    text = greetingForNow(displayName),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 36.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (streakDays > 0) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "$streakDays-day streak",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = CurioColors.CoralBlush
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── 3. Quest card ───────────────────────────────────────────
            val chosen = selectedCategory
            val isWildcard = chosen == null || chosen.id == CategoryId.WILDCARD
            val accent = if (isWildcard) CurioColors.CoralBlush else chosen!!.accent
            Surface(
                    onClick = {
                        if (chosen == null || chosen.id == CategoryId.WILDCARD) {
                            navController.navigate(CurioRoutes.SPIN)
                        } else {
                            navController.navigate(CurioRoutes.spinWithCategory(chosen.id.routeSlug))
                        }
                    },
                    shape = RoundedCornerShape(28.dp),
                    // Opaque paper card: depth comes from a crisp edge and
                    // shadow, never from transparency or a background wash.
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(2.dp, accent),
                    shadowElevation = 8.dp,
                    tonalElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(168.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // A quiet category tint keeps the paper surface
                        // tactile without turning it into a translucent overlay.
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(accent.copy(alpha = 0.08f))
                        )
                        // Watermark glyph
                        CurioIcon(
                            name = if (chosen != null) chosen.iconGlyph else CurioIcons.Casino,
                            contentDescription = null,
                            tint = accent.copy(alpha = 0.18f),
                            size = 140.dp,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 8.dp)
                        )
                        // Content
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(16.dp)
                                        .background(accent, RoundedCornerShape(2.dp))
                                )
                                Text(
                                    text = "TODAY'S QUEST",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.2.sp
                                    ),
                                    color = accent
                                )
                            }
                            Column {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = chosen?.displayName ?: "Spin the wheel",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = accent
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        CurioIcon(
                                            CurioIcons.Casino, null,
                                            tint = Color.White,
                                            size = 16.dp
                                        )
                                        Text(
                                            text = if (isWildcard) "Shuffle" else "Spin",
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                    }
                                }
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }

            Spacer(Modifier.height(14.dp))

            // ── 4. Stats strip — 3 compact pills ────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatPill(
                    modifier = Modifier.weight(1f),
                    glyph = "local_fire_department",
                    value = "$streakDays",
                    tint = CurioColors.CoralBlush
                )
                StatPill(
                    modifier = Modifier.weight(1f),
                    glyph = CurioIcons.Inventory2,
                    value = "$totalSaved",
                    tint = CurioColors.Sage
                )
                StatPill(
                    modifier = Modifier.weight(1f),
                    glyph = CurioIcons.History,
                    value = "${recentEntries.size}",
                    tint = CurioColors.Lilac
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── 5. Categories chip row ──────────────────────────────────
            Column {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Categories",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Surface(
                        onClick = { navController.navigate(CurioRoutes.PICKER) },
                        shape = RoundedCornerShape(50),                            color = MaterialTheme.colorScheme.surfaceContainerLow

                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                "All",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            CurioIcon(
                                CurioIcons.ArrowForward, null,
                                tint = MaterialTheme.colorScheme.primary,
                                size = 14.dp
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    // Surprise wildcard pinned first
                    item(key = "wildcard") {
                        CategoryChip(
                            name = "Surprise",
                            glyph = CurioIcons.Casino,
                            accent = CurioColors.CoralBlush,
                            selected = selectedCategory == null,
                            onClick = { selectedCategory = null }
                        )
                    }
                    items(items = CurioCategories.visible, key = { it.id.name }) { cat ->
                        if (cat.id != CategoryId.WILDCARD) {
                            CategoryChip(
                                name = cat.displayName,
                                glyph = cat.iconGlyph,
                                accent = cat.accent,
                                selected = selectedCategory?.id == cat.id,
                                onClick = {
                                    selectedCategory =
                                        if (selectedCategory?.id == cat.id) null else cat
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── 6. Recently explored — renders all at once (no stagger) ──
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
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
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surfaceContainerLow
                        ) {
                            CurioIcon(
                                CurioIcons.ArrowForward, "Open Cabinet",
                                tint = MaterialTheme.colorScheme.primary,
                                size = 18.dp,
                                modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))

                if (recentEntries.isEmpty()) {
                    FirstTimeEmpty(
                        onPickCategory = { navController.navigate(CurioRoutes.PICKER) },
                        onSpinSurprise = { navController.navigate(CurioRoutes.SPIN) }
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        recentEntries.forEach { entry ->
                            RecentEntryRow(
                                entry = entry,
                                onClick = { navController.navigate(CurioRoutes.entryDetail(entry.id)) }
                            )
                        }
                    }
                }

                // Add breathing room before the bottom card / nav bar
                Spacer(Modifier.height(12.dp))
            }

            // ── 7. Reminder nudge (when reminders off) ─────────────────
            if (!reminderEnabled) {
                Spacer(Modifier.height(16.dp))
                ReminderNudgeCard(
                    onTap = { navController.navigate(CurioRoutes.SETTINGS) }
                )
            }

            Spacer(Modifier.height(32.dp))
            Spacer(Modifier.height(navInsets.calculateBottomPadding()))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// Stat pill (compact)
// ══════════════════════════════════════════════���════════════════════════

@Composable
private fun StatPill(
    modifier: Modifier = Modifier,
    glyph: String,
    value: String,
    tint: Color
) {
    // Opaque paper tile with a defined edge and modest elevation.
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, tint.copy(alpha = 0.45f)),
        tonalElevation = 2.dp
    ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CurioIcon(glyph, null, tint = tint, size = 16.dp)
                Spacer(Modifier.width(8.dp))
                Text(
                    value,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = tint,
                    maxLines = 1
                )
    }
}

// ═══════════════════════════════════════════════════════════════════════
// Category chip — pill with leading icon + label
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun CategoryChip(
    name: String,
    glyph: String,
    accent: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.04f else 1f,
        animationSpec = CurioMotion.Springs.Snappy,
        label = "catChipScale"
    )
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = if (selected) accent else MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, if (selected) accent else accent.copy(alpha = 0.45f)),
        shadowElevation = if (selected) 4.dp else 1.dp,
        modifier = Modifier.scale(scale)
    ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
            // Opaque paper tile for the category glyph
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (selected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(1.dp, if (selected) Color.White.copy(alpha = 0.35f) else accent.copy(alpha = 0.30f)),
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    CurioIcon(
                        glyph, null,
                        tint = if (selected) Color.White else accent,
                        size = 18.dp
                    )
                }
            }
            Text(
                text = name,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold
                ),
                color = if (selected) Color.White else accent
            )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// Recent entry row (compact)
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun RecentEntryRow(entry: CurioEntry, onClick: () -> Unit) {
    val cat = CurioCategories.byId(entry.topic.categoryId)
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shadowElevation = 1.dp,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Color swatch with category glyph
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(cat.accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                CurioIcon(
                    cat.iconGlyph, null, tint = cat.accent, size = 22.dp
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    entry.topic.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${cat.displayName} · ${entry.capturedAtDaysAgoLabel()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            CurioIcon(
                CurioIcons.ArrowForward, "Open capture",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                size = 18.dp
            )
        }
    }
}

private fun CurioEntry.capturedAtDaysAgoLabel(): String = when (val d = capturedAtDaysAgo) {
    0 -> "today"
    1 -> "yesterday"
    else -> "${d}d ago"
}

// ═══════════════════════════════════════════════════════════════════════
// First-time empty state
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun FirstTimeEmpty(
    onPickCategory: () -> Unit,
    onSpinSurprise: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),                    color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CurioIcon(
                CurioIcons.AutoAwesome, null,
                tint = CurioColors.CoralBlush,
                size = 36.dp
            )
            Text(
                "Your journey starts here",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                "Spin the wheel to discover your first topic. Capture what you find and it'll land here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Surface(
                    onClick = onSpinSurprise,
                    shape = RoundedCornerShape(50),
                    color = CurioColors.CoralBlush
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CurioIcon(CurioIcons.Casino, null, tint = Color.White, size = 16.dp)
                        Text(
                            "Surprise me",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
                Surface(
                    onClick = onPickCategory,
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Text(
                        "Pick a lane",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// Reminder nudge card (only when reminder OFF)
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun ReminderNudgeCard(onTap: () -> Unit) {
    val fg = MaterialTheme.colorScheme.onSurface
    Surface(
        onClick = onTap,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, CurioColors.ButterYellow),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CurioColors.ButterYellow.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center
                ) {
                    CurioIcon(
                        CurioIcons.Notifications, null,
                        tint = fg,
                        size = 18.dp
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Try a daily spin reminder",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = fg
                    )
                    Text(
                        "Pick a time → we nudge you to discover",
                        style = MaterialTheme.typography.bodySmall,
                        color = fg.copy(alpha = 0.78f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                CurioIcon(
                    CurioIcons.ArrowForward, "Open settings",
                    tint = fg.copy(alpha = 0.7f),
                    size = 18.dp
                )
            }
        }
    }
}

// Drawer (kept; minor polish)
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun HomeDrawerContent(onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    val displayName = AppPreferences.getDisplayName(context)
    ModalDrawerSheet(
        modifier = Modifier.width(320.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface
    ) {
        // ── Opaque paper header with a clear category edge ──────────────
        Box(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(2.dp, CurioColors.CoralBlush),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            CurioIcon(
                                CurioIcons.AutoAwesome, null,
                                tint = CurioColors.CoralBlush,
                                size = 28.dp
                            )
                        }
                    }
                    Column {
                        Text(
                            "Curio",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Hi $displayName",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(CurioColors.CoralBlush)
        )
        
        Spacer(Modifier.height(16.dp))
        
        // ── Redesigned nav items with better spacing and icons ──────────
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item("profile") {
                DrawerNavItem(
                    icon = CurioIcons.Person,
                    label = "Profile & Settings",
                    iconTint = CurioColors.Lilac
                ) { onNavigate(CurioRoutes.PROFILE) }
            }
            item("history") {
                DrawerNavItem(
                    icon = CurioIcons.History,
                    label = "Topic History",
                    iconTint = CurioColors.DustyBlue
                ) { onNavigate(CurioRoutes.TOPIC_HISTORY) }
            }
            item("manage") {
                DrawerNavItem(
                    icon = CurioIcons.DragHandle,
                    label = "Manage Categories",
                    iconTint = CurioColors.Sage
                ) { onNavigate(CurioRoutes.MANAGE_CATEGORIES) }
            }
            item("replay") {
                DrawerNavItem(
                    icon = CurioIcons.Replay,
                    label = "Replay Intro",
                    iconTint = CurioColors.Peach
                ) {
                    com.curio.app.features.onboarding.CurioOnboardingState.reset(context)
                    onNavigate(CurioRoutes.ONBOARDING)
                }
            }
        }
        
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        // ── Footer with version info ────────────────────────────────────
        Column(
            Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "v1.0.0",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Text(
                "Made with curiosity",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun DrawerNavItem(
    icon: String,
    label: String,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconTint.copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    CurioIcon(
                        icon, null,
                        tint = iconTint,
                        size = 22.dp
                    )
                }
            }
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// Greeting helpers
// ═══════════════════════════════════════════════════════════════════════

private fun greetingForNow(displayName: String): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Welcome back"
    }
    return "$greeting, $displayName"
}


