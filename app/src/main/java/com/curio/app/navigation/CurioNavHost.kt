package com.curio.app.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import android.net.Uri
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.curio.app.features.bugreport.BugReportScreen
import com.curio.app.features.crash.CurioCrashScreen
import com.curio.app.features.lightbox.LightboxScreen
import com.curio.app.features.managecategories.ManageCategoriesScreen
import com.curio.app.features.onboarding.OnboardingScreen
import com.curio.app.features.profile.ProfileScreen
import com.curio.app.features.settings.SettingsScreen
import com.curio.app.features.topichistory.TopicHistoryScreen
import com.curio.app.features.cabinet.CabinetScreen
import com.curio.app.features.capture.SaveCaptureScreen
import com.curio.app.features.detail.EntryDetailScreen
import com.curio.app.features.picker.CategoryPickerScreen
import com.curio.app.features.reveal.TopicRevealScreen
import com.curio.app.features.spin.SpinScreen
import com.curio.app.features.home.HomeScreen
import com.curio.app.features.splash.SplashScreen
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
 *  - Forward navigations: slide left + fade, with morph spring
 *  - Back navigations: slide right + fade, with morph spring
 *  - Tab switches (bottom nav): simple crossfade (no directional slide)
 *  - Reveal screen: special elastic entrance
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
                    // Other exits: slide out slightly + fade
                    else -> slideOutHorizontally(
                        targetOffsetX = { fullWidth -> -fullWidth / 6 },
                        animationSpec = spring(
                            dampingRatio = 0.9f,
                            stiffness = 300f
                        )
                    ) + fadeOut(animationSpec = tween(CurioMotion.Durations.Quick))
                }
            },
            popEnterTransition = {
                // Back navigation: slide right + fade
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> -fullWidth / 6 },
                    animationSpec = spring(dampingRatio = 0.9f, stiffness = 300f)
                ) + fadeIn(animationSpec = tween(CurioMotion.Durations.Quick))
            },
            popExitTransition = {
                // Pop exit: slide right + fade out
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth / 4 },
                    animationSpec = tween(CurioMotion.Durations.Morph, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(CurioMotion.Durations.Morph))
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
            composable(CurioRoutes.PROFILE) {
                ProfileScreen(navController = navController)
            }
            composable(CurioRoutes.SETTINGS) {
                SettingsScreen(navController = navController)
            }
            composable(CurioRoutes.MANAGE_CATEGORIES) {
                ManageCategoriesScreen(navController = navController)
            }
            composable(CurioRoutes.TOPIC_HISTORY) {
                TopicHistoryScreen(navController = navController)
            }
            composable(CurioRoutes.CRASH) {
                CurioCrashScreen(navController = navController)
            }
            composable(CurioRoutes.BUG_REPORT) {
                BugReportScreen(navController = navController)
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
