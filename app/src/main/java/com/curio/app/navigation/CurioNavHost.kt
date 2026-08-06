package com.curio.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavBackStackEntry
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.curio.app.data.AppPreferences
import com.curio.app.data.ExploreReminderScheduler
import com.curio.app.data.ExploreSessionStore
import com.curio.app.data.formatElapsed
import com.curio.app.infrastructure.ExploreSessionService
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import kotlinx.coroutines.delay
import com.curio.app.features.bugreport.BugReportScreen
import com.curio.app.features.crash.CurioCrashScreen
import com.curio.app.features.lightbox.LightboxScreen
import com.curio.app.features.managecategories.ManageCategoriesScreen
import com.curio.app.features.onboarding.OnboardingScreen
import com.curio.app.features.profile.ProfileScreen
import com.curio.app.features.settings.BackupToolsScreen
import com.curio.app.features.settings.ExperimentsScreen
import com.curio.app.features.settings.SettingsHubScreen
import com.curio.app.features.settings.SettingsPage
import com.curio.app.features.settings.SettingsSectionScreen
import com.curio.app.features.topichistory.TopicHistoryScreen
import com.curio.app.features.recent.RecentScreen
import com.curio.app.features.cabinet.CabinetScreen
import com.curio.app.features.capture.SaveCaptureScreen
import com.curio.app.features.detail.EntryDetailScreen
import com.curio.app.features.picker.CategoryPickerScreen
import com.curio.app.features.reveal.TopicRevealScreen
import com.curio.app.features.spin.SpinScreen
import com.curio.app.features.home.HomeScreen
import com.curio.app.features.splash.SplashScreen
import com.curio.app.features.fieldmind.FieldMindObservationScreen
import com.curio.app.ui.components.CurioBottomBar
import com.curio.app.ui.theme.CurioMotion

/**
 * Decodes a nav-argument string safely — malformed percent-escapes or
 * unpaired surrogates fall back to the raw value instead of crashing
 * with IllegalArgumentException.
 */
private fun safeDecode(raw: String?): String =
    runCatching { Uri.decode(raw.orEmpty()) }.getOrDefault(raw.orEmpty())

/**
 * True when a navigation is a bottom-nav TAB switch — both the screen being
 * left and the screen being shown are tab routes. Tab switches crossfade
 * (no directional slide): the tabs are peer screens that restore saved
 * state, and sliding them (worse, with the old underdamped spring) read as
 * the page-switch glitch.
 */
private fun AnimatedContentTransitionScope<NavBackStackEntry>.isTabSwitch(
    initialState: NavBackStackEntry,
    targetState: NavBackStackEntry
): Boolean =
    initialState.destination.route?.substringBefore("/") in CurioRoutes.bottomNavRoutePrefixes &&
        targetState.destination.route?.substringBefore("/") in CurioRoutes.bottomNavRoutePrefixes

/**
 * The Curio NavHost — single-NavHost scaffold for the active app.
 *
 * All routes are flat. The bottom nav is rendered by a [Scaffold] wrapper
 * and is conditionally visible based on the current route (see
 * [CurioRoutes.bottomNavRoutes]). When the user is on a non-bottom-nav
 * route (push destinations like Picker/Reveal/Capture/Detail/Settings/
 * ManageCategories/TopicHistory/Lightbox), the bottom bar is omitted.
 *
 * Each tab uses the standard Compose Navigation pattern when navigated to:
 *   navigate(route) { popUpTo(startDestination) { saveState = true }; ... }
 * — see CurioBottomNav for the actual call site. This preserves each tab's
 * back stack across switches.
 *
 * Upgraded navigation transitions:
 *  - Forward navigations: slide left + fade (matched tweens)
 *  - Back navigations: slide right + fade (matched tweens)
 *  - Tab switches (bottom nav): simple crossfade (no directional slide)
 *  - Splash → Home / Onboarding: fade-only reveal
 * v7.17 — the old exit/pop-enter slides used underdamped springs that
 * overshot and bounced (and never matched their paired fade) — the
 * page-switch glitch. All transitions now use matched tweens, and tab
 * switches crossfade.
 */
@Composable
fun CurioNavHost(
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val routePrefix = remember(currentRoute) {
        currentRoute?.substringBefore("/")
    }
    val showBottomBar = routePrefix in CurioRoutes.bottomNavRoutePrefixes

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var showDoneDialog by rememberSaveable { mutableStateOf(false) }
    // v7.31 — two-step "Cancel session": the first tap flips the done-now
    // dialog into a confirm step, the second tap actually ends the explore.
    var confirmSessionCancel by rememberSaveable { mutableStateOf(false) }
    // Survives rotation so the startup prompt only fires on a truly fresh
    // process (an active session left behind by a killed app).
    var startupPromptDone by rememberSaveable { mutableStateOf(false) }

    // Ask "are you done exploring?" whenever the app returns to the
    // foreground while an explore session is active — mid-session, after
    // the browser search, or after the app was killed in the background.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (AppPreferences.isExploreSessionsEnabled(context)) {
                    val resumed = ExploreSessionStore.getActiveSession(context)
                    showDoneDialog = resumed != null
                    // A background/foreground cycle must not reopen the dialog
                    // already sitting in the cancel-confirm step.
                    confirmSessionCancel = false
                    // If the user hid the bubble but no other controller
                    // exists (live notifications off) and the bubble is
                    // still enabled, bring it back on return — otherwise
                    // there'd be no visible timer controller at all.
                    if (resumed != null && resumed.pillHidden &&
                        !AppPreferences.liveNotificationsEnabledState &&
                        AppPreferences.isOverlayBubbleEnabled(context)
                    ) {
                        ExploreSessionStore.setPillHidden(context, false)
                    }
                    // Re-arm the explore service (live notification + bubble)
                    // after returning to the app — covers permissions granted
                    // mid-session, Settings toggles, and the restore above.
                    if (resumed != null && AppPreferences.exploreServiceShouldRun(context)) {
                        ExploreSessionService.start(context, resumed)
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    // Startup restore: the observer above is added after the activity is
    // already RESUMED on launch, so a persisted session from a killed
    // process surfaces here instead (dialog + re-armed service).
    LaunchedEffect(Unit) {
        if (!startupPromptDone) {
            startupPromptDone = true
            if (AppPreferences.isExploreSessionsEnabled(context)) {
                val session = ExploreSessionStore.getActiveSession(context)
                showDoneDialog = session != null
                confirmSessionCancel = false
                if (session != null && AppPreferences.exploreServiceShouldRun(context)) {
                    ExploreSessionService.start(context, session)
                }
            }
        }
    }

    // ── "Done exploring" notification handoff ─────────────────────────
    // The notification action stashes the topic (category slug + name) via
    // PendingEntryOpen and launches the activity. Once this NavHost is on a
    // stable root (a bottom-nav tab), open the write-it-down entry page with
    // HOME anchored beneath it — so Back from the entry page returns to the
    // app instead of exiting it. During the boot gates (splash/onboarding/
    // crash) the effect returns WITHOUT consuming; it re-runs when the
    // splash lands on HOME (keyed on currentRoute).
    LaunchedEffect(currentRoute, PendingEntryOpen.trigger) {
        val prefix = currentRoute?.substringBefore("/")
        // Wait for a stable root: null (first frame) and the boot gates own
        // navigation until the splash lands on HOME — the effect re-runs
        // there (keyed on currentRoute) and consumes the target once.
        if (prefix == null || prefix in CurioRoutes.bootGatePrefixes) return@LaunchedEffect
        val target = PendingEntryOpen.take() ?: return@LaunchedEffect
        if (prefix != CurioRoutes.HOME) {
            navController.popBackStack(CurioRoutes.HOME, inclusive = false)
        }
        navController.navigate(CurioRoutes.captureFor(target.first, target.second)) {
            launchSingleTop = true
        }
    }

    // The floating explore bubble now lives in the explore service's overlay
    // window (over other apps), so the Scaffold simply fills the screen.
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    CurioBottomBar(navController = navController)
                }
            },
            // Every screen applies its own statusBarsPadding().  This Scaffold
            // has no topBar, so without pinning the insets to the bottom only
            // M3 would add the status-bar inset to innerPadding AND the screens
            // would add it again — a double top gap (huge empty space above the
            // status bar).  Screens without a bottom bar still get the nav-bar
            // inset from here.
            contentWindowInsets = WindowInsets.navigationBars,
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = CurioRoutes.SPLASH,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            // ── Animated screen transitions ────────────────────────────────
            // v7.17 — page-switch glitch fix. The old exit/pop-enter slides
            // used an UNDERDAMPED spring (damping 0.9): it overshot past the
            // target and bounced back, and its timing never matched the
            // paired fade — the "weird glitchy" look on page switches. All
            // slides are now matched tweens (slide + fade finish together,
            // no overshoot), and bottom-nav TAB switches crossfade instead
            // of sliding (peer tabs restore saved state; sliding them reads
            // glitchy — this was promised in the header doc but never
            // implemented).
            enterTransition = {
                when {
                    // Splash → Home / Onboarding: special elastic morph
                    initialState.destination.route == CurioRoutes.SPLASH ->
                        fadeIn(
                            animationSpec = tween(
                                durationMillis = CurioMotion.Durations.Reveal,
                                delayMillis = 0
                            )
                        )
                    // Tab switches: simple crossfade (no directional slide)
                    isTabSwitch(initialState, targetState) ->
                        fadeIn(animationSpec = tween(CurioMotion.Durations.Standard))
                    // Other forward navigations: slide left + fade
                    else -> slideInHorizontally(
                        initialOffsetX = { fullWidth -> fullWidth / 4 },
                        animationSpec = tween(CurioMotion.Durations.Morph, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(CurioMotion.Durations.Morph))
                }
            },
            exitTransition = {
                when {
                    // Navigating away from splash: no exit needed
                    initialState.destination.route == CurioRoutes.SPLASH ->
                        fadeOut(animationSpec = tween(CurioMotion.Durations.Quick))
                    isTabSwitch(initialState, targetState) ->
                        fadeOut(animationSpec = tween(CurioMotion.Durations.Standard))
                    // Other exits: slide out slightly + fade
                    else -> slideOutHorizontally(
                        targetOffsetX = { fullWidth -> -fullWidth / 6 },
                        animationSpec = tween(CurioMotion.Durations.Morph, easing = FastOutSlowInEasing)
                    ) + fadeOut(animationSpec = tween(CurioMotion.Durations.Quick))
                }
            },
            popEnterTransition = {
                // Tab switch back: crossfade too (no directional slide).
                if (isTabSwitch(initialState, targetState)) {
                    fadeIn(animationSpec = tween(CurioMotion.Durations.Standard))
                } else {
                    // Back navigation: slide right + fade
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> -fullWidth / 6 },
                        animationSpec = tween(CurioMotion.Durations.Morph, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(CurioMotion.Durations.Quick))
                }
            },
            popExitTransition = {
                if (isTabSwitch(initialState, targetState)) {
                    fadeOut(animationSpec = tween(CurioMotion.Durations.Standard))
                } else {
                    // Pop exit: slide right + fade out
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> fullWidth / 4 },
                        animationSpec = tween(CurioMotion.Durations.Morph, easing = FastOutSlowInEasing)
                    ) + fadeOut(animationSpec = tween(CurioMotion.Durations.Morph))
                }
            }
        ) {
            // ── Splash + Onboarding (no bottom nav) ──────────────────────────
            composable(CurioRoutes.SPLASH) {
                SplashScreen(navController = navController)
            }
            composable(CurioRoutes.ONBOARDING) {
                OnboardingScreen(navController = navController)
            }

            // ── Bottom-nav tabs ──────────────────────────────────────────────
            composable(CurioRoutes.HOME) {
                HomeScreen(navController = navController)
            }
            composable(CurioRoutes.SPIN) {
                SpinScreen(categorySlug = null, navController = navController)
            }
            composable(CurioRoutes.CABINET) {
                CabinetScreen(navController = navController)
            }

            // ── Spin flow (no bottom nav) ──────────────────────────────────
            composable(
                route = CurioRoutes.PICKER,
            ) {
                CategoryPickerScreen(navController = navController)
            }
            composable(
                route = CurioRoutes.SPIN_WITH_CATEGORY,
                arguments = listOf(navArgument("categorySlug") { type = NavType.StringType })
            ) { entry ->
                SpinScreen(
                    categorySlug = entry.arguments?.getString("categorySlug"),
                    navController = navController
                )
            }
            composable(
                route = CurioRoutes.REVEAL,
                arguments = listOf(
                    navArgument("categorySlug") { type = NavType.StringType },
                    navArgument("topicName")     { type = NavType.StringType }
                )
            ) { entry ->
                TopicRevealScreen(
                    categorySlug = entry.arguments?.getString("categorySlug").orEmpty(),
                    topicName    = safeDecode(entry.arguments?.getString("topicName")),
                    navController = navController
                )
            }
            composable(
                route = CurioRoutes.CAPTURE,
                arguments = listOf(
                    navArgument("categorySlug") { type = NavType.StringType },
                    navArgument("topicName")     { type = NavType.StringType }
                )
            ) { entry ->
                SaveCaptureScreen(
                    categorySlug = entry.arguments?.getString("categorySlug").orEmpty(),
                    topicName    = safeDecode(entry.arguments?.getString("topicName")),
                    navController = navController
                )
            }

            // ── Push destinations (no bottom nav) ──────────────────────────
            composable(
                route = CurioRoutes.ENTRY_DETAIL,
                arguments = listOf(navArgument("entryId") { type = NavType.StringType })
            ) { entry ->
                EntryDetailScreen(
                    entryId = entry.arguments?.getString("entryId").orEmpty(),
                    navController = navController
                )
            }
            // Both edit routes reopen a saved entry (a single mood board or a
            // whole multi-section Portfolio) in the universal editor — the
            // screen preloads the entry, lets the user rearrange any take,
            // and re-saves in place (same id → Room REPLACE).
            composable(
                route = CurioRoutes.EDIT_MOODBOARD,
                arguments = listOf(navArgument("entryId") { type = NavType.StringType })
            ) { entry ->
                SaveCaptureScreen(
                    categorySlug = "",
                    topicName = "",
                    navController = navController,
                    editEntryId = entry.arguments?.getString("entryId").orEmpty()
                )
            }
            composable(
                route = CurioRoutes.EDIT_ENTRY,
                arguments = listOf(navArgument("entryId") { type = NavType.StringType })
            ) { entry ->
                SaveCaptureScreen(
                    categorySlug = "",
                    topicName = "",
                    navController = navController,
                    editEntryId = entry.arguments?.getString("entryId").orEmpty()
                )
            }
            composable(CurioRoutes.PROFILE) {
                ProfileScreen(navController = navController)
            }
            composable(CurioRoutes.SETTINGS) {
                SettingsHubScreen(navController = navController)
            }
            composable(CurioRoutes.SETTINGS_APPEARANCE) {
                SettingsSectionScreen(navController = navController, page = SettingsPage.APPEARANCE)
            }
            composable(CurioRoutes.SETTINGS_NOTIFICATIONS) {
                SettingsSectionScreen(navController = navController, page = SettingsPage.NOTIFICATIONS)
            }
            composable(CurioRoutes.SETTINGS_RECORDING) {
                SettingsSectionScreen(navController = navController, page = SettingsPage.RECORDING)
            }
            composable(CurioRoutes.SETTINGS_DATA) {
                BackupToolsScreen(navController = navController)
            }
            composable(CurioRoutes.SETTINGS_ABOUT) {
                SettingsSectionScreen(navController = navController, page = SettingsPage.ABOUT)
            }
            composable(CurioRoutes.EXPERIMENTS) {
                ExperimentsScreen(navController = navController)
            }
            composable(CurioRoutes.MANAGE_CATEGORIES) {
                ManageCategoriesScreen(navController = navController)
            }
            composable(CurioRoutes.TOPIC_HISTORY) {
                TopicHistoryScreen(navController = navController)
            }
            composable(CurioRoutes.RECENTS_ALL) {
                RecentScreen(navController = navController)
            }
            composable(CurioRoutes.CRASH) {
                CurioCrashScreen(navController = navController)
            }
            composable(CurioRoutes.BUG_REPORT) {
                BugReportScreen(navController = navController)
            }
            composable(CurioRoutes.FIELDMIND_OBSERVATION) {
                FieldMindObservationScreen(navController = navController)
            }
            composable(route = CurioRoutes.LIGHTBOX) {
                // The image URI is handed off out-of-band via LightboxTarget
                // (see CurioRoutes.lightbox) — no route arg, so no encoding/
                // decoding round-trip that could corrupt content URIs.
                LightboxScreen(navController = navController)
            }
        }
        }
    }

    // ── Done-exploring prompt (app return while a session is active) ────
    val activeSession = ExploreSessionStore.activeSessionState
    if (showDoneDialog && activeSession != null) {
        // Live elapsed time — ticks every second while the dialog is open
        // (pause-aware: session.elapsedMillis banks paused time, so a paused
        // session shows a frozen reading). Cancels on dismiss.
        var elapsedMillis by remember(activeSession.startMillis) {
            mutableStateOf(activeSession.elapsedMillis())
        }
        LaunchedEffect(activeSession.startMillis, activeSession.paused) {
            while (true) {
                elapsedMillis = activeSession.elapsedMillis()
                delay(1_000)
            }
        }
        AlertDialog(
            onDismissRequest = {
                showDoneDialog = false
                confirmSessionCancel = false
            },
            title = {
                Text(
                    if (confirmSessionCancel) "Cancel this explore?"
                    else "Done exploring ${activeSession.topicName}?"
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (confirmSessionCancel) {
                        // The double-confirmation step — make the cost of
                        // cancelling explicit before the session is dropped.
                        Text(
                            "This ends the session now — the ${formatElapsed(elapsedMillis)} isn't saved and you won't be asked to write about ${activeSession.topicName}. You can explore it again anytime.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CurioIcon(
                                name = if (activeSession.paused) CurioIcons.Pause else CurioIcons.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                size = 18.dp
                            )
                            Text(
                                if (activeSession.paused)
                                    "Paused at ${formatElapsed(elapsedMillis)} — tap Resume on the bubble or notification to continue"
                                else
                                    "You've been exploring for ${formatElapsed(elapsedMillis)}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                        Text(
                            "You started ${activeSession.verb.lowercase()} ${activeSession.targetName} — if you're done, write it down while it's fresh. Or keep exploring, no rush.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                if (confirmSessionCancel) {
                    // Second tap — the actual end. Quiet teardown, same as
                    // the notification's Cancel action (no write-it-down
                    // page, no done prompt on the next return).
                    TextButton(onClick = {
                        showDoneDialog = false
                        confirmSessionCancel = false
                        ExploreSessionStore.clearSession(context)
                        ExploreReminderScheduler.cancel(context)
                        ExploreSessionService.stop(context)
                    }) {
                        Text("Yes, cancel session", color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    TextButton(onClick = {
                        showDoneDialog = false
                        confirmSessionCancel = false
                        ExploreSessionStore.clearSession(context)
                        ExploreReminderScheduler.cancel(context)
                        ExploreSessionService.stop(context)
                        // Anchor HOME beneath the entry page so Back returns to
                        // the app instead of exiting from a deep-opened page.
                        val routePrefix = currentRoute?.substringBefore("/")
                        if (routePrefix != null &&
                            routePrefix != CurioRoutes.HOME &&
                            routePrefix !in CurioRoutes.bootGatePrefixes
                        ) {
                            navController.popBackStack(CurioRoutes.HOME, inclusive = false)
                        }
                        navController.navigate(
                            CurioRoutes.captureFor(activeSession.categoryId.routeSlug, activeSession.topicName)
                        ) { launchSingleTop = true }
                    }) { Text("Done — write about it") }
                }
            },
            dismissButton = {
                if (confirmSessionCancel) {
                    // Back out of the cancel — keep exploring.
                    TextButton(onClick = { confirmSessionCancel = false }) { Text("Keep exploring") }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { confirmSessionCancel = true }) {
                            Text("Cancel session", color = MaterialTheme.colorScheme.error)
                        }
                        TextButton(onClick = { showDoneDialog = false }) { Text("Keep exploring") }
                    }
                }
            }
        )
    }
}


