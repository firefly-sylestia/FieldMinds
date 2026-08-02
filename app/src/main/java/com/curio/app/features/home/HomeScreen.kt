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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
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
import com.curio.app.data.PinnedTopic
import com.curio.app.data.SavedQuote
import com.curio.app.data.CurioCategory
import com.curio.app.data.CurioEntry
import com.curio.app.data.CurioRepositoryHolder
import com.curio.app.data.ExploreReminderScheduler
import com.curio.app.data.ExploreSession
import com.curio.app.data.ExploreSessionStore
import com.curio.app.data.StreakTracker
import com.curio.app.data.formatElapsed
import com.curio.app.infrastructure.ExploreSessionService
import com.curio.app.navigation.CurioRoutes
import com.curio.app.navigation.navigateToTab
import com.curio.app.ui.components.CurioForwardArrow
import com.curio.app.ui.components.CurioWatermarkBackdrop
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioGradients
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.CurioMotion
import com.curio.app.ui.theme.categoryInk
import com.curio.app.ui.theme.themedAccent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.content.Intent
import android.net.Uri
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
 *      is selected it shows the chosen category's accent + CTA "Shuffle for
 *      $name". Otherwise it shows the wildcard + a "Shuffle" CTA. Re-uses
 *   4. **Stats strip** — four compact stat pills: Streak · Saved · Recent
 *      · Categories.
 *   5. **Categories chip row** — horizontally-scrollable color chips.
 *      Each chip shows the family color, category glyph, and label.
 *      Tapping sets the active category.
 *   6. **Saved** — bookmarked quotes + pinned topics (hidden when empty),
 *      each row tappable through to its entry / topic.
 *   7. **Recently explored** — header row + 2-col grid of recent entry
 *      cards, or a beautiful empty-state card prompting the first spin.
 *   8. **Reminder CTA** (only when reminder is OFF) — a subtle ghost-style
 *      card suggesting the user try a daily shuffle reminder, navigating to
 *      Settings.
 *
 *  The screen still hosts the `ModalNavigationDrawer` for secondary
 *  navigation (Profile, History, Manage Categories, Replay Intro).
 *
 *  Top paddings tightened: `statusBarsPadding()` + `vertical = 4dp` for the
 *  bar, `vertical = 6dp` between sections — keeps the "no empty top"
 *  guarantee we established in Shuffle/TopicReveal.
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
    val reminderEnabled = AppPreferences.reminderEnabledState
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
                    navController.navigate(route) { launchSingleTop = true }
                }
            )
        },
        gesturesEnabled = drawerState.isOpen || drawerState.isAnimationRunning
    ) {
        // v6.7 — Home sits on the plain theme background: the category tint
        // wash is removed from Home (other screens still tint via Settings).
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // ── Watermark backdrop — muted category glyphs behind all ──
            //    content (same treatment as the Spin page). The selected
            //    category's glyph gets a faint accent tint; "Surprise"
            //    highlights the wildcard die.
            CurioWatermarkBackdrop(
                activeCat = selectedCategory ?: CurioCategories.byId(CategoryId.WILDCARD)
            )
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
                // Refined menu button with better icon.
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
                    onClick = { navController.navigate(CurioRoutes.PROFILE) { launchSingleTop = true } },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shadowElevation = 0.dp,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        CurioIcon(
                            CurioIcons.Person, "Profile",
                            tint = MaterialTheme.colorScheme.onSurface,
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
            // Wildcard reuses the brand-primary coral, so a single elvis
            // covers both the Surprise and named-category cases.
            val accent = chosen?.themedAccent() ?: CurioColors.CategoryCoral
            val questGradient = CurioGradients.cardGradient(accent)
            Surface(
                    onClick = {
                        // Both quest branches go through the tab switch helper
                        // (anchor = HOME, the persistent root): plain pushes
                        // here used to pile up duplicate back-stack entries
                        // when the same quest card was opened repeatedly.
                        if (chosen == null || chosen.id == CategoryId.WILDCARD) {
                            navController.navigateToTab(CurioRoutes.SPIN)
                        } else {
                            navController.navigateToTab(CurioRoutes.spinWithCategory(chosen.id.routeSlug))
                        }
                    },
                    shape = RoundedCornerShape(28.dp),
                    color = Color.Transparent,
                    shadowElevation = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(168.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Solid gradient background
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(questGradient),
                                    RoundedCornerShape(28.dp)
                                )
                        )
                        // Watermark glyph
                        CurioIcon(
                            name = if (chosen != null) chosen.iconGlyph else CurioIcons.Casino,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.20f),
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
                                        .background(Color.White.copy(alpha = 0.60f), RoundedCornerShape(2.dp))
                                )
                                Text(
                                    text = "TODAY'S QUEST",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.2.sp
                                    ),
                                    color = Color.White.copy(alpha = 0.88f)
                                )
                            }
                            Column {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = chosen?.displayName ?: "Shuffle the deck",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                    color = Color.White,
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
                                    color = Color.White.copy(alpha = 0.22f)
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
                                            text = if (isWildcard) "Shuffle" else "Shuffle",
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
                    tint = CurioColors.CoralBlush,
                    surface = MaterialTheme.colorScheme.surfaceContainerLow
                )
                StatPill(
                    modifier = Modifier.weight(1f),
                    glyph = CurioIcons.Inventory2,
                    value = "$totalSaved",
                    tint = CurioColors.Sage,
                    surface = MaterialTheme.colorScheme.surfaceContainerLow
                )
                StatPill(
                    modifier = Modifier.weight(1f),
                    glyph = CurioIcons.History,
                    value = "${recentEntries.size}",
                    tint = CurioColors.Lilac,
                    surface = MaterialTheme.colorScheme.surfaceContainerLow
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── 4b. Currently exploring — live session card ────────────
            val activeSession = ExploreSessionStore.activeSessionState
            if (activeSession != null) {
                CurrentlyExploringCard(
                    session = activeSession,
                    onDone = {
                        ExploreSessionStore.clearSession(context)
                        ExploreReminderScheduler.cancel(context)
                        ExploreSessionService.stop(context)
                        navController.navigate(
                            CurioRoutes.captureFor(activeSession.categoryId.routeSlug, activeSession.topicName)
                        ) { launchSingleTop = true }
                    },
                    onKeepExploring = {
                        // Re-open the Google search — the session keeps
                        // ticking in the background.
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(activeSession.searchUrl)))
                        }
                    }
                )
                Spacer(Modifier.height(20.dp))
            }

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
                        onClick = { navController.navigate(CurioRoutes.PICKER) { launchSingleTop = true } },
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
                        shadowElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "All",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)) {
                                CurioForwardArrow(
                                    tint = MaterialTheme.colorScheme.primary,
                                    size = 14.dp,
                                    modifier = Modifier.padding(3.dp)
                                )
                            }
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
                            accent = CurioColors.CategoryCoral,
                            selected = selectedCategory == null,
                            surface = MaterialTheme.colorScheme.surfaceContainerLow,
                            onClick = { selectedCategory = null }
                        )
                    }
                    items(items = CurioCategories.visible, key = { it.id.name }) { cat ->
                        if (cat.id != CategoryId.WILDCARD) {
                            CategoryChip(
                                name = cat.displayName,
                                accent = cat.themedAccent(),
                                selected = selectedCategory?.id == cat.id,
                                surface = MaterialTheme.colorScheme.surfaceContainerLow,
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

            // ── 6. Saved — bookmarked quotes + pinned topics ───────────
            val savedQuotes = AppPreferences.savedQuotesState
            val pinnedTopics = AppPreferences.pinnedTopicsState
            if (savedQuotes.isNotEmpty() || pinnedTopics.isNotEmpty()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        "Saved",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        savedQuotes.forEach { quote ->
                            SavedQuoteRow(
                                quote = quote,
                                onClick = {
                                    navController.navigate(CurioRoutes.entryDetail(quote.entryId)) {
                                        launchSingleTop = true
                                    }
                                },
                                onRemove = {
                                    AppPreferences.removeSavedQuote(context, quote.entryId, quote.quoteText)
                                }
                            )
                        }
                        pinnedTopics.forEach { pinned ->
                            PinnedTopicRow(
                                pinned = pinned,
                                onClick = {
                                    navController.navigate(
                                        CurioRoutes.revealFor(pinned.categoryId.routeSlug, pinned.topicName)
                                    ) { launchSingleTop = true }
                                },
                                onUnpin = {
                                    AppPreferences.unpinTopic(context, pinned.categoryId, pinned.topicName)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── 7. Recently explored — renders all at once (no stagger) ──
            val exploredTopics = ExploreSessionStore.recentlyExploredState
            val unexploredTopics = ExploreSessionStore.recentlyUnexploredState
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
                            onClick = { navController.navigateToTab(CurioRoutes.CABINET) },
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surfaceContainerLow
                        ) {
                            CurioForwardArrow(
                                "Open Cabinet",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))

                if (recentEntries.isEmpty() && exploredTopics.isEmpty()) {
                    FirstTimeEmpty(
                        surface = MaterialTheme.colorScheme.surfaceContainerLow,
                        onPickCategory = { navController.navigate(CurioRoutes.PICKER) { launchSingleTop = true } },
                        onShuffleSurprise = { navController.navigateToTab(CurioRoutes.SPIN) }
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Explore-session topics — recorded the moment the
                        // user tapped Explore on a reveal, before (or even
                        // without) anything being saved to the Cabinet.
                        exploredTopics.forEach { explored ->
                            val category = CurioCategories.byId(explored.categoryId)
                            ExploreTopicRow(
                                category = category,
                                topicName = explored.topicName,
                                subtitle = "Explored · tap to write about it",
                                onClick = {
                                    navController.navigate(
                                        CurioRoutes.captureFor(explored.categoryId.routeSlug, explored.topicName)
                                    ) { launchSingleTop = true }
                                }
                            )
                        }
                        recentEntries.forEach { entry ->
                            RecentEntryRow(
                                entry = entry,
                                surface = MaterialTheme.colorScheme.surface,
                                onClick = { navController.navigate(CurioRoutes.entryDetail(entry.id)) { launchSingleTop = true } }
                            )
                        }
                    }
                }

                // Add breathing room before the bottom card / nav bar
                Spacer(Modifier.height(12.dp))
            }

            // ── 7b. Recently unexplored — topics the user left without ──
            //     exploring, so Home offers a way to resume them.
            if (unexploredTopics.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        "Recently unexplored",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        unexploredTopics.forEach { unexplored ->
                            val category = CurioCategories.byId(unexplored.categoryId)
                            ExploreTopicRow(
                                category = category,
                                topicName = unexplored.topicName,
                                subtitle = "Left without exploring · tap to resume",
                                onClick = {
                                    navController.navigate(
                                        CurioRoutes.revealFor(unexplored.categoryId.routeSlug, unexplored.topicName)
                                    ) { launchSingleTop = true }
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }

            // ── 8. Reminder nudge (when reminders off) ─────────────────
            if (!reminderEnabled) {
                Spacer(Modifier.height(16.dp))
                ReminderNudgeCard(
                    surface = MaterialTheme.colorScheme.surfaceContainerLow,
                    onTap = { navController.navigate(CurioRoutes.SETTINGS) { launchSingleTop = true } }
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
    tint: Color,
    surface: Color = MaterialTheme.colorScheme.surfaceContainerLow
) {
    // Color accent on the icon, readable onSurface text, solid theme surface.
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = surface,
        shadowElevation = 0.dp
    ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CurioIcon(glyph, null, tint = tint, size = 17.dp)
                Spacer(Modifier.width(8.dp))
                Text(
                    value,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
            }
        }
}

// ═══════════════════════════════════════════════════════════════════════
// Category chip — text-only pill (matches the picker cards: no boxed icon)
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun CategoryChip(
    name: String,
    accent: Color,
    selected: Boolean,
    onClick: () -> Unit,
    surface: Color = MaterialTheme.colorScheme.surfaceContainerLow
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.06f else 1f,
        animationSpec = CurioMotion.Springs.Snappy,
        label = "catChipScale"
    )
    val inactiveContainer = lerp(surface, accent, 0.10f)
    // Muted selected fill — same treatment as the picker cards: the raw
    // accent is deepened just a touch toward black so it stays rich.
    val selectedContainer = lerp(accent, Color.Black, 0.10f)
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = if (selected) selectedContainer else inactiveContainer,
        border = if (selected) null else BorderStroke(1.dp, accent.copy(alpha = 0.24f)),
        shadowElevation = 0.dp,
        modifier = Modifier.scale(scale)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold
                ),
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// ── Saved shelf rows — bookmarked quotes + pinned topics ───────────────

@Composable
private fun SavedQuoteRow(
    quote: SavedQuote,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val cat = CurioCategories.byId(quote.categoryId)
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
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(cat.tint, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                CurioIcon(
                    name = CurioIcons.FormatQuote,
                    contentDescription = null,
                    tint = cat.categoryInk(),
                    size = 18.dp
                )
            }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "\u201C${quote.quoteText}\u201D",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "from ${quote.topicName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                onClick = onRemove,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                CurioIcon(
                    CurioIcons.BookmarkBorder, "Remove bookmark",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    size = 18.dp,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}

@Composable
private fun PinnedTopicRow(
    pinned: PinnedTopic,
    onClick: () -> Unit,
    onUnpin: () -> Unit
) {
    val cat = CurioCategories.byId(pinned.categoryId)
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
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(cat.tint, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                CurioIcon(
                    name = CurioIcons.Bookmark,
                    contentDescription = null,
                    tint = cat.categoryInk(),
                    size = 18.dp
                )
            }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pinned.topicName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = cat.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = cat.categoryInk()
                )
            }
            Surface(
                onClick = onUnpin,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                CurioIcon(
                    CurioIcons.BookmarkBorder, "Unpin topic",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    size = 18.dp,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}

// Recent entry row (compact)
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun RecentEntryRow(entry: CurioEntry, onClick: () -> Unit, surface: Color = MaterialTheme.colorScheme.surface) {
    val cat = CurioCategories.byId(entry.topic.categoryId)
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = surface,
        shadowElevation = 0.dp,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Color swatch with category glyph — bolder accent
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(cat.themedAccent().copy(alpha = 0.32f)),
                contentAlignment = Alignment.Center
            ) {
                CurioIcon(
                    cat.iconGlyph, null, tint = cat.categoryInk(), size = 22.dp
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
            CurioForwardArrow(
                "Open capture",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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
    onShuffleSurprise: () -> Unit,
    surface: Color = MaterialTheme.colorScheme.surfaceContainerLow
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = surface,
        shadowElevation = 0.dp,
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
                "Shuffle the deck to discover your first topic. Capture what you find and it'll land here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Surface(
                    onClick = onShuffleSurprise,
                    shape = RoundedCornerShape(50),
                    color = CurioColors.CoralBlush
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CurioIcon(CurioIcons.Casino, null, tint = CurioColors.DeepPlum, size = 16.dp)
                        Text(
                            "Surprise me",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = CurioColors.DeepPlum
                        )
                    }
                }
                Surface(
                    onClick = onPickCategory,
                    shape = RoundedCornerShape(50),
                    // v6.6 — derive from the tinted card surface so this
                    // secondary button never reads as a foreign cream pill
                    // on the tinted first-run card.
                    color = lerp(surface, MaterialTheme.colorScheme.surfaceContainerLow, 0.5f),
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
private fun ReminderNudgeCard(onTap: () -> Unit, surface: Color = MaterialTheme.colorScheme.surfaceContainerLow) {
    val fg = MaterialTheme.colorScheme.onSurface
    Surface(
        onClick = onTap,
        shape = RoundedCornerShape(20.dp),
        color = surface,
        shadowElevation = 0.dp,
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
                        "Try a daily shuffle reminder",
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
                CurioForwardArrow(
                    "Open settings",
                    tint = fg.copy(alpha = 0.7f)
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
                        color = CurioColors.CoralBlush.copy(alpha = 0.18f),
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
                color = iconTint.copy(alpha = 0.24f),
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

// ═══════════════════════════════════════════════════════════════════════
// Explore-session topic row (recently explored / recently unexplored)
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun ExploreTopicRow(
    category: CurioCategory,
    topicName: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val accent = category.themedAccent()
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.dp,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                CurioIcon(category.iconGlyph, null, tint = accent, size = 20.dp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    topicName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            CurioForwardArrow(
                contentDescription = subtitle,
                tint = accent,
                modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// Currently exploring — live session card
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun CurrentlyExploringCard(
    session: ExploreSession,
    onDone: () -> Unit,
    onKeepExploring: () -> Unit
) {
    val accent = CurioCategories.byId(session.categoryId).themedAccent()
    // Live elapsed time — recomputed from the persisted session start so it
    // survives process restarts; the tick cancels when the card leaves
    // composition (session ended / Home left).
    var elapsedMillis by remember(session.startMillis) {
        mutableStateOf(System.currentTimeMillis() - session.startMillis)
    }
    LaunchedEffect(session.startMillis) {
        while (true) {
            elapsedMillis = System.currentTimeMillis() - session.startMillis
            delay(1_000)
        }
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(accent.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    CurioIcon(
                        CurioIcons.Timer, null,
                        tint = accent,
                        size = 22.dp
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Currently exploring",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = accent
                    )
                    Text(
                        session.topicName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            val overRecommended = elapsedMillis >= session.durationMinutes * 60_000L
            Text(
                if (overRecommended) {
                    "${session.verb.lowercase()} ${session.targetName} · ${formatElapsed(elapsedMillis)} so far — past the ~${session.durationMinutes} min mark"
                } else {
                    "${session.verb.lowercase()} ${session.targetName} · ${formatElapsed(elapsedMillis)} so far · ~${session.durationMinutes} min recommended"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onDone,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Done — write about it", style = MaterialTheme.typography.labelLarge)
                }
                OutlinedButton(
                    onClick = onKeepExploring,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = accent),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Keep exploring", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}


