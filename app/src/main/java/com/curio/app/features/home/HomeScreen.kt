package com.curio.app.features.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
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
import com.curio.app.ui.components.SoftTornBottomShape
import com.curio.app.ui.components.SoftTornSheetShape
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.categorySurface
import com.curio.app.ui.theme.isCurioDarkTheme
import com.curio.app.ui.theme.pastelAccent
import com.curio.app.ui.theme.pastelFillInk
import com.curio.app.ui.theme.themedAccent
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.content.Intent
import android.net.Uri
import java.util.Calendar

/**
 * Home — clean, minimal, personalized.
 *
 * Layout (top to bottom), tuned for 360×800 dp:
 *   1. **Quest hero** — the detail screen's torn-banner language, extended
 *      to the very top: the solid rose-wood banner runs up BEHIND the
 *      status bar, and the menu / avatar pills overlay it. Same seeded
 *      soft tear + white under-sheet (the identical EntryDetail
 *      construction, so the tear style stays UNIFORM — no blur). Inside:
 *      the greeting (one line) with the name beneath, and a Streak ·
 *      Cabinet · Recent bar pinned just above the tear on a soft rose
 *      gradient pane (streak in fire orange). The banner itself is NOT
 *      tappable.
 *   2. **Quest block** — "TODAY'S QUEST" eyebrow + the big solid Shuffle
 *      button, sitting between the hero tear and the content below. The
 *      button picks a random category (or a random mix) and opens that
 *      deck on the Shuffle tab.
 *   3. **Currently exploring / Queued** — the live session card and any
 *      paused sessions set aside for later.
 *   4. **Saved** — bookmarked quotes + pinned topics (hidden when empty),
 *      each row tappable through to its entry / topic.
 *   5. **Recents** — explored topics, unexplored topics (tagged
 *      "Unexplored"), and the latest saved entries as solid category-
 *      tinted cards (View all → Cabinet), or a beautiful empty-state card
 *      prompting the first spin.
 *   6. **Reminder CTA** (only when reminder is OFF) — a subtle ghost-style
 *      card suggesting the user try a daily shuffle reminder, navigating to
 *      Settings.
 *
 *  The screen still hosts the `ModalNavigationDrawer` for secondary
 *  navigation (Profile, History, Manage Categories, Replay Intro).
 */
/** The quest hero's solid body height — the torn banner. Tall enough for
 *  the greeting + the Streak · Cabinet · Recent bar (pinned just above the
 *  tear) and generous at large font scales. */
private val HomeQuestHeroHeight = 300.dp
/** Extra layout space reserved for the white sheet below the torn banner. */
private val HomeQuestSheetExtent = 24.dp
/** Scroll distance (dp) before the menu + profile pills fully pin as
 *  frosted floating pills. */
private val StickyBarThreshold = 90.dp
/** Fixed tear seed — Home's tear never re-rolls and matches the detail
 *  hero's SoftTorn construction exactly (uniform tear style). */
private const val HOME_TEAR_SEED = 0x5EED

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val displayName = remember { AppPreferences.getDisplayName(context) }
    // Saved-shelf unsave confirmation — set when the user taps the remove
    // bookmark on a saved quote row; the dialog confirms before removal.
    var pendingUnsave by remember { mutableStateOf<SavedQuote?>(null) }
    // Unpin-topic confirmation — set when the user taps unpin on a pinned
    // topic row; the dialog confirms before the pin is dropped.
    var pendingUnpin by remember { mutableStateOf<PinnedTopic?>(null) }
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
            //    content (same treatment as the Spin page). The quest is
            //    always the wildcard Surprise now (no category chips), so
            //    the wildcard die stays highlighted.
            CurioWatermarkBackdrop(
                activeCat = CurioCategories.byId(CategoryId.WILDCARD)
            )
            // Hoisted scroll state — the sticky top bar (menu + profile
            // pills) reads it to pop out of the hero into frosted pills.
            val homeScroll = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(homeScroll)
            ) {
            // ── 1. Quest hero — the detail screen's torn-banner language,
            // extended to the very top: the solid rose-wood banner runs up
            // BEHIND the status bar, and the menu / avatar pills overlay it
            // (added at the end of this Box, so they sit on the banner).
            // Same seeded SOFT tear (SoftTornBottomShape) + white under-
            // sheet (SoftTornSheetShape — same seed → aligned pixel-
            // perfect): the identical EntryDetail construction, so the tear
            // style stays UNIFORM across the app. No blur on the banner:
            // flat color + a real torn seam. Fixed seed → never re-rolls.
            // Inside: the greeting (one line) + name beneath, and the
            // Streak · Cabinet · Recent bar pinned just above the tear on a
            // soft rose gradient pane. The banner itself is NOT tappable —
            // the Shuffle deck CTA lives below the hero.
            val heroTornShape = remember(HOME_TEAR_SEED) { SoftTornBottomShape(HOME_TEAR_SEED) }
            val sheetShape = remember(HOME_TEAR_SEED) {
                SoftTornSheetShape(HOME_TEAR_SEED, lip = 10.dp, baseline = 14.dp)
            }
            // The quest is always the wildcard Surprise now (the category
            // chip row is gone). The banner wears the muted rose-wood hero
            // accent — in pastel mode (the shipped default) it resolves to
            // the airy rose-wood pastel twin, otherwise the calm base.
            val accent = CurioColors.HomeRosewood
            val heroFill = homeRoseAccent()
            val questInk = pastelFillInk(accent)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HomeQuestHeroHeight + HomeQuestSheetExtent)
            ) {
                // ── White under-sheet — same as the detail hero's: the
                // sheet's torn top hides behind the opaque banner while its
                // uneven lip reads white below the tear, and the page wash
                // starts right after it.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .offset(y = HomeQuestHeroHeight - 18.dp)
                        .clip(sheetShape)
                        .background(Color(0xFFFDFCF9))
                )
                // ── Solid rose-wood banner, torn bottom edge. The banner is
                // NOT tappable — only the Shuffle button below the hero
                // drives the deck.
                Surface(
                    shape = heroTornShape,
                    color = heroFill,
                    shadowElevation = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(HomeQuestHeroHeight)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Watermark glyph — sparkles, clipped by the tear
                        // (a different glyph from the wildcard die).
                        CurioIcon(
                            name = CurioIcons.AutoAwesome,
                            contentDescription = null,
                            tint = questInk.copy(alpha = 0.16f),
                            size = 140.dp,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 8.dp)
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .statusBarsPadding()
                                .padding(start = 20.dp, end = 20.dp, top = 64.dp, bottom = 18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Greeting — one line, left-aligned, with the
                            // name beneath it (the quest CTA moved below the
                            // hero). Proper hierarchy: big bold greeting, then
                            // a smaller, softer name below.
                            Text(
                                text = greetingWordForNow(),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = questInk,
                                textAlign = TextAlign.Start,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = questInk.copy(alpha = 0.85f),
                                textAlign = TextAlign.Start,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth()
                            )
                            // Flex spacer — pins the stat card to the bottom
                            // of the banner, just above the tear.
                            Spacer(Modifier.weight(1f))
                            // ── Streak · Cabinet · Recent — the detail bar's
                            // icon/value/label design, sitting just above the
                            // torn seam on a soft rose gradient pane (the
                            // banner's own color, not white frost).
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.Transparent,
                                border = BorderStroke(1.dp, questInk.copy(alpha = 0.28f)),
                                shadowElevation = 0.dp
                            ) {
                                // The gradient must wear the card's rounded
                                // shape itself — Surface does not clip its
                                // content, so a plain background() would bleed
                                // square corners past the rounded border.
                                Box(
                                    modifier = Modifier.background(
                                        Brush.verticalGradient(
                                            listOf(
                                                heroFill.copy(alpha = 0.12f),
                                                lerp(heroFill, Color.White, 0.26f).copy(alpha = 0.55f)
                                            )
                                        ),
                                        RoundedCornerShape(20.dp)
                                    )
                                ) {
                                    // Watermark sparkle — echoes the hero's
                                    // glyph, a faint ink ghost on the right.
                                    // The wrapper Box uses matchParentSize() so
                                    // the glyph can never inflate the pane: a
                                    // wrap-content Box would otherwise size to
                                    // its tallest child and grow the stat card
                                    // away from the tear.
                                    Box(
                                        modifier = Modifier.matchParentSize(),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        CurioIcon(
                                            name = CurioIcons.AutoAwesome,
                                            contentDescription = null,
                                            tint = questInk.copy(alpha = 0.08f),
                                            size = 76.dp,
                                            modifier = Modifier.padding(end = 6.dp)
                                        )
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 6.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Icons wear the HERO ink (not the
                                        // pastel tints) so they stay visible
                                        // on the rose pane — deeper, same
                                        // family as the banner text.
                                        HeroStatSegment(
                                            glyph = "local_fire_department",
                                            value = "$streakDays",
                                            label = "Streak",
                                            tint = questInk,
                                            ink = questInk,
                                            modifier = Modifier.weight(1f)
                                        )
                                        VerticalDivider(
                                            modifier = Modifier.height(34.dp),
                                            color = questInk.copy(alpha = 0.22f)
                                        )
                                        HeroStatSegment(
                                            glyph = CurioIcons.Inventory2,
                                            value = "$totalSaved",
                                            label = "Cabinet",
                                            tint = questInk,
                                            ink = questInk,
                                            modifier = Modifier.weight(1f)
                                        )
                                        VerticalDivider(
                                            modifier = Modifier.height(34.dp),
                                            color = questInk.copy(alpha = 0.22f)
                                        )
                                        HeroStatSegment(
                                            glyph = CurioIcons.History,
                                            value = "${recentEntries.size}",
                                            label = "Recent",
                                            tint = questInk,
                                            ink = questInk,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                // The menu + profile pills no longer live here — they moved
                // to a scroll-reactive STICKY bar outside the hero (they pop
                // out of the coral into frosted floating pills on scroll).
            }

            Spacer(Modifier.height(14.dp))

            // ── Quest block — below the hero tear, above the content ────
            // "TODAY'S QUEST" eyebrow (no indicator) + the big solid Shuffle
            // button. The button picks a random category — or a random mix —
            // persists it (the plain Shuffle tab is authoritative from
            // prefs) and opens the deck.
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                QuestShuffleCard(
                    accent = homeRoseAccent(),
                    onShuffle = {
                        val all = CurioCategories.all
                        val pickMix = Random.nextBoolean()
                        val chosen =
                            if (pickMix) all.shuffled().take(2 + Random.nextInt(2))
                            else listOf(all.random())
                        AppPreferences.setLastSpinCategories(context, chosen.map { it.id })
                        navController.navigateToTab(CurioRoutes.SPIN)
                    }
                )
            }
            Spacer(Modifier.height(20.dp))

            // ── 2. Currently exploring — live session card ──────────────
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

            // ── 3. Queued explores — sessions set aside for later ──────
            // When a new explore replaced the running one, the old session is
            // paused (time banked) and queued here. Tap a row to swap it back
            // into the active slot; the ✕ discards it.
            val queuedSessions = ExploreSessionStore.queuedSessionsState
            if (queuedSessions.isNotEmpty()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        "Queued explores",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        queuedSessions.forEachIndexed { index, queued ->
                            QueuedExploreRow(
                                session = queued,
                                onResume = {
                                    // Cancel the running session's reminder
                                    // (it's about to be queued), swap the
                                    // queues, then re-arm everything for the
                                    // resumed session.
                                    ExploreReminderScheduler.cancel(context)
                                    ExploreSessionStore.resumeQueuedSession(context, index)
                                    ExploreSessionStore.getActiveSession(context)?.let { resumed ->
                                        ExploreReminderScheduler.schedule(
                                            context, resumed.startMillis, resumed.durationMinutes
                                        )
                                        // Same gate as every other re-arm: the
                                        // service only runs when a notification
                                        // or the bubble wants it.
                                        if (AppPreferences.exploreServiceShouldRun(context)) {
                                            ExploreSessionService.start(context, resumed)
                                        }
                                    }
                                },
                                onDiscard = { ExploreSessionStore.removeQueued(context, index) }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            Spacer(Modifier.height(20.dp))

            // ── 4. Saved — bookmarked quotes + pinned topics ───────────
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
                                onRemove = { pendingUnsave = quote }
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
                                onUnpin = { pendingUnpin = pinned }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── 5. Recents — explored + unexplored topics and recent entries ──
            val exploredTopics = ExploreSessionStore.recentlyExploredState
            val unexploredTopics = ExploreSessionStore.recentlyUnexploredState
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Recents",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (recentEntries.isNotEmpty()) {
                        Surface(
                            onClick = { navController.navigateToTab(CurioRoutes.CABINET) },
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surfaceContainerLow
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    "View all",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                CurioForwardArrow(
                                    "Open Cabinet",
                                    tint = MaterialTheme.colorScheme.primary,
                                    size = 16.dp
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))

                if (recentEntries.isEmpty() && exploredTopics.isEmpty() && unexploredTopics.isEmpty()) {
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
                                // A topic the user left unexplored and later
                                // came back to wears a small "Resumed" tag.
                                tag = if (explored.wasUnexplored) "Resumed" else null,
                                subtitle = "Explored · tap to write about it",
                                onClick = {
                                    navController.navigate(
                                        CurioRoutes.captureFor(explored.categoryId.routeSlug, explored.topicName)
                                    ) { launchSingleTop = true }
                                }
                            )
                        }
                        // Unexplored topics now live INSIDE Recents, tagged —
                        // no separate section; tap resumes the reveal.
                        unexploredTopics.forEach { unexplored ->
                            val category = CurioCategories.byId(unexplored.categoryId)
                            ExploreTopicRow(
                                category = category,
                                topicName = unexplored.topicName,
                                tag = "Unexplored",
                                subtitle = "Left without exploring · tap to resume",
                                onClick = {
                                    navController.navigate(
                                        CurioRoutes.revealFor(unexplored.categoryId.routeSlug, unexplored.topicName)
                                    ) { launchSingleTop = true }
                                }
                            )
                        }
                        recentEntries.forEach { entry ->
                            RecentEntryRow(
                                entry = entry,
                                onClick = { navController.navigate(CurioRoutes.entryDetail(entry.id)) { launchSingleTop = true } }
                            )
                        }
                    }
                }

                // Add breathing room before the bottom card / nav bar
                Spacer(Modifier.height(12.dp))
            }

            // ── 6. Reminder nudge (when reminders off) ─────────────────
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

            // ── Sticky top bar — menu + profile pills ─────────────────
            // Pinned OUTSIDE the scroll content so they stay on screen.
            // Resting on the hero they read as translucent hero-ink pills;
            // as the hero scrolls away they POP out into floating frosted
            // pills (spring scale + frost morph + shadow), and they glide
            // back into the hero smoothly when you scroll up.
            val stickyThresholdPx = with(LocalDensity.current) { StickyBarThreshold.toPx() }
            val stickyProgress by remember {
                derivedStateOf { (homeScroll.value / stickyThresholdPx).coerceIn(0f, 1f) }
            }
            // Hysteresis — the pop engages once past the threshold and only
            // releases after scrolling well back, so a fling that oscillates
            // around the boundary can't re-trigger the pop repeatedly. The
            // engaged flag is derived state (not a scroll-keyed effect), so
            // the pop only fires on actual crossings.
            var stickyEngaged by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                snapshotFlow { stickyProgress }.collect { p ->
                    if (p >= 1f) stickyEngaged = true
                    else if (p <= 0.6f) stickyEngaged = false
                }
            }
            // Sweet pop — a quick anticipation dip, then a springy overshoot
            // that settles at rest (never a hard snap); a gentle rotation
            // wobble rides the pop so the pills flick into place. On the way
            // back everything eases to the hero state.
            val popScale = remember { Animatable(1f) }
            val popRotate = remember { Animatable(0f) }
            LaunchedEffect(stickyEngaged) {
                if (stickyEngaged) {
                    // Anticipation dip → lively overshoot → settle.
                    popScale.animateTo(0.90f, tween(90, easing = FastOutSlowInEasing))
                    popScale.animateTo(1.08f, spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium))
                    popScale.animateTo(1f, spring(dampingRatio = 0.60f, stiffness = Spring.StiffnessLow))
                    // Wobble — tilts out, overshoots back, rights itself.
                    popRotate.animateTo(-3.5f, tween(110, easing = FastOutSlowInEasing))
                    popRotate.animateTo(2.2f, tween(150, easing = FastOutSlowInEasing))
                    popRotate.animateTo(0f, spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow))
                } else {
                    popScale.animateTo(1f, spring(dampingRatio = 0.80f, stiffness = Spring.StiffnessLow))
                    popRotate.animateTo(0f, spring(dampingRatio = 0.80f, stiffness = Spring.StiffnessLow))
                }
            }
            // Eased frost shift — smoothstep so the glass morph accelerates
            // in and decelerates out instead of sliding linearly with scroll.
            val frostShift = stickyProgress * stickyProgress * (3f - 2f * stickyProgress)
            val stickyDark = isCurioDarkTheme()
            // Re-resolve the hero ink here — the original questInk lives in
            // the scroll Column's scope; the sticky bar is OUTSIDE it.
            val stickyInk = pastelFillInk(CurioColors.HomeRosewood)
            val frostBg = if (stickyDark) Color(0xE623242C) else Color.White.copy(alpha = 0.80f)
            val frostRim = if (stickyDark) Color.White.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.75f)
            val frostIcon = if (stickyDark) Color.White.copy(alpha = 0.92f) else stickyInk
            val pillBg = lerp(stickyInk.copy(alpha = 0.14f), frostBg, frostShift)
            val pillRim = lerp(stickyInk.copy(alpha = 0.26f), frostRim, frostShift)
            val pillIcon = lerp(stickyInk, frostIcon, frostShift)
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp)
                    .graphicsLayer {
                        scaleX = popScale.value
                        scaleY = popScale.value
                        rotationZ = popRotate.value
                        // Lifts off the hero as the frost deepens (eased).
                        translationY = -2.dp.toPx() * frostShift
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TopBarPill(
                    onClick = { scope.launch { drawerState.open() } },
                    glyph = CurioIcons.Menu,
                    contentDescription = "Open menu",
                    shape = RoundedCornerShape(50),
                    bg = pillBg,
                    rim = pillRim,
                    iconTint = pillIcon,
                    elevation = 6.dp * frostShift
                )
                TopBarPill(
                    onClick = { navController.navigate(CurioRoutes.PROFILE) { launchSingleTop = true } },
                    glyph = CurioIcons.Person,
                    contentDescription = "Profile",
                    shape = CircleShape,
                    bg = pillBg,
                    rim = pillRim,
                    iconTint = pillIcon,
                    elevation = 6.dp * frostShift
                )
            }
        }
    }

    // ── Unsave-quote confirmation — never remove a bookmark silently ──
    pendingUnsave?.let { quote ->
        AlertDialog(
            onDismissRequest = { pendingUnsave = null },
            title = { Text("Remove saved quote?") },
            text = { Text("This removes \u201C${quote.quoteText}\u201D from your Saved shelf. The entry itself stays in the Cabinet.") },
            confirmButton = {
                TextButton(onClick = {
                    AppPreferences.removeSavedQuote(context, quote.entryId, quote.quoteText)
                    pendingUnsave = null
                }) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { pendingUnsave = null }) { Text("Keep") }
            }
        )
    }

    // ── Unpin-topic confirmation — never drop a pin silently ──
    pendingUnpin?.let { pinned ->
        AlertDialog(
            onDismissRequest = { pendingUnpin = null },
            title = { Text("Unpin ${pinned.topicName}?") },
            text = { Text("This removes ${pinned.topicName} from your Saved shelf. The topic stays in the deck — you can pin it again anytime.") },
            confirmButton = {
                TextButton(onClick = {
                    AppPreferences.unpinTopic(context, pinned.categoryId, pinned.topicName)
                    pendingUnpin = null
                }) { Text("Unpin") }
            },
            dismissButton = {
                TextButton(onClick = { pendingUnpin = null }) { Text("Keep") }
            }
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════
// Hero stat segment — the detail bar's icon/value/label design, on the
// home banner (Streak · Cabinet · Recent). No blur, per the home spec.
// ══════════════════════════════════════════════���════════════════════════

@Composable
private fun HeroStatSegment(
    glyph: String,
    value: String,
    label: String,
    tint: Color,
    ink: Color,
    modifier: Modifier = Modifier
) {
    // Colored icon accent, extra-bold value, soft label — mirrors
    // EntryDetail's FrostedSegment, with the icon wearing the color accent.
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        CurioIcon(
            name = glyph,
            contentDescription = null,
            tint = tint,
            size = 18.dp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = ink,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = ink.copy(alpha = 0.85f)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════
// Sticky top-bar pill — one circular menu / profile button for the
// scroll-linked frosted bar that pops out of the hero.
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun TopBarPill(
    onClick: () -> Unit,
    glyph: String,
    contentDescription: String,
    shape: Shape,
    bg: Color,
    rim: Color,
    iconTint: Color,
    elevation: Dp
) {
    Surface(
        onClick = onClick,
        shape = shape,
        color = bg,
        border = BorderStroke(1.dp, rim),
        shadowElevation = elevation,
        modifier = Modifier.size(42.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            CurioIcon(
                name = glyph,
                contentDescription = contentDescription,
                tint = iconTint,
                size = 22.dp
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// Quest block — the big solid Shuffle CTA that lives between the hero
// tear and the content below.
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun QuestShuffleCard(
    accent: Color,
    onShuffle: () -> Unit
) {
    // Deep ink twin for the eyebrow — the airy pastel accent reads too
    // light against the page, so the eyebrow wears the darker ink instead
    // (the button keeps the solid accent fill).
    val ink = pastelFillInk(accent)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "TODAY'S QUEST",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.4.sp
                ),
                color = ink
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Shuffle the deck",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        // Solid inline CTA — sits on the SAME level as the title, not
        // stacked below it. Compact (44dp, snug 12dp side padding) so the
        // eyebrow + title column never gets squeezed or cut on narrow
        // screens.
        Surface(
            onClick = onShuffle,
            shape = RoundedCornerShape(16.dp),
            color = accent,
            shadowElevation = 0.dp,
            modifier = Modifier.height(44.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CurioIcon(
                    CurioIcons.Casino, "Shuffle a random deck",
                    tint = ink,
                    size = 18.dp
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "Shuffle",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = ink
                )
            }
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
    // Backgroundless row — the Saved shelf is a plain list now: no card
    // fill, no icon box — just a bare category glyph, the quote text and
    // the remove affordance.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CurioIcon(
            name = CurioIcons.FormatQuote,
            contentDescription = null,
            tint = cat.themedAccent(),
            size = 22.dp
        )
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

@Composable
private fun PinnedTopicRow(
    pinned: PinnedTopic,
    onClick: () -> Unit,
    onUnpin: () -> Unit
) {
    val cat = CurioCategories.byId(pinned.categoryId)
    // Backgroundless row — matches the plain Saved-shelf list style.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CurioIcon(
            name = CurioIcons.Bookmark,
            contentDescription = null,
            tint = cat.themedAccent(),
            size = 22.dp
        )
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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

// Recent entry row (compact)
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun RecentEntryRow(entry: CurioEntry, onClick: () -> Unit) {
    val cat = CurioCategories.byId(entry.topic.categoryId)
    // Solid category-tinted card — matches the recents topic rows.
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = cat.categorySurface(),
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CurioIcon(
                cat.iconGlyph, null, tint = cat.themedAccent(), size = 24.dp
            )
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

/**
 * The Home accent, resolved like the hero banner: the muted rose-wood base
 * normally, its airy pastel twin when pastel mode (the shipped default) is
 * on — so the hero, empty state and drawer all wear the SAME rose-wood.
 */
@Composable
private fun homeRoseAccent(): Color =
    if (AppPreferences.pastelColorsState) {
        pastelAccent(CurioColors.HomeRosewood, isCurioDarkTheme())
    } else {
        CurioColors.HomeRosewood
    }

@Composable
private fun FirstTimeEmpty(
    onPickCategory: () -> Unit,
    onShuffleSurprise: () -> Unit,
    surface: Color = MaterialTheme.colorScheme.surfaceContainerLow
) {
    val roseAccent = homeRoseAccent()
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
                tint = roseAccent,
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
                    color = roseAccent
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
    val roseAccent = homeRoseAccent()
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
                        color = roseAccent.copy(alpha = 0.18f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            CurioIcon(
                                CurioIcons.AutoAwesome, null,
                                tint = roseAccent,
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
                .background(roseAccent)
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

private fun greetingWordForNow(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Welcome back"
    }
}

// ═══════════════════════════════════════════════════════════════════════
// Explore-session topic row (recently explored / recently unexplored)
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun ExploreTopicRow(
    category: CurioCategory,
    topicName: String,
    subtitle: String,
    onClick: () -> Unit,
    tag: String? = null
) {
    val accent = category.themedAccent()
    // Solid category-tinted card — the recents topics wear a solid
    // background in their category's color family (matching the gradient
    // identity), instead of a backgroundless row.
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = category.categorySurface(),
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CurioIcon(category.iconGlyph, null, tint = accent, size = 24.dp)
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        topicName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (tag != null) {
                        // Small accent pill — signals a topic the user left
                        // unexplored earlier and came back to (resumed).
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = accent.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = accent,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
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
    // Live elapsed time — pause-aware (session.elapsedMillis banks paused
    // time, so a paused session shows a frozen reading) and recomputed from
    // the persisted session start so it survives process restarts; the tick
    // cancels when the card leaves composition.
    var elapsedMillis by remember(session.startMillis) {
        mutableStateOf(session.elapsedMillis())
    }
    LaunchedEffect(session.startMillis, session.paused) {
        if (session.paused) return@LaunchedEffect
        while (true) {
            elapsedMillis = session.elapsedMillis()
            delay(1_000)
        }
    }

    val cat = CurioCategories.byId(session.categoryId)
    // Same design language as the rest of Home: a solid category-tinted
    // card (matching the recents rows) with a faint category glyph
    // watermark echoing the hero, and a quest-style eyebrow.
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = cat.categorySurface(),
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Box {
            // Watermark glyph — the session's category, like the hero's.
            CurioIcon(
                name = cat.iconGlyph,
                contentDescription = null,
                tint = accent.copy(alpha = 0.10f),
                size = 96.dp,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 10.dp)
            )
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
                            "CURRENTLY EXPLORING",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.4.sp
                            ),
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
                    when {
                        session.paused ->
                            "Paused at ${formatElapsed(elapsedMillis)} — ${session.verb.lowercase()} ${session.targetName}"
                        overRecommended ->
                            "${session.verb.lowercase()} ${session.targetName} · ${formatElapsed(elapsedMillis)} so far — past the ~${session.durationMinutes} min mark"
                        else ->
                            "${session.verb.lowercase()} ${session.targetName} · ${formatElapsed(elapsedMillis)} so far · ~${session.durationMinutes} min recommended"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (session.paused) accent else MaterialTheme.colorScheme.onSurfaceVariant,
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
                            contentColor = pastelFillInk(accent)
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
}

// ═══════════════════════════════════════════════════════════════════════
// Queued explore row — a paused session saved for later (tap to resume)
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun QueuedExploreRow(
    session: ExploreSession,
    onResume: () -> Unit,
    onDiscard: () -> Unit
) {
    val accent = CurioCategories.byId(session.categoryId).themedAccent()
    // Plain backgroundless row, matching the Recents / Saved list style —
    // the frozen elapsed readout comes from the session's banked pause.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onResume)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CurioIcon(CurioIcons.Schedule, null, tint = accent, size = 22.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                session.topicName,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "Paused at ${formatElapsed(session.elapsedMillis())} · tap to resume",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Surface(
            onClick = onDiscard,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            CurioIcon(
                CurioIcons.Close, "Discard queued explore",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                size = 16.dp,
                modifier = Modifier.padding(5.dp)
            )
        }
    }
}
