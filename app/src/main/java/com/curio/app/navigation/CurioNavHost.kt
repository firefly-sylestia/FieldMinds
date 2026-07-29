package com.curio.app.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import com.curio.app.features.lightbox.LightboxScreen
import com.curio.app.features.managecategories.ManageCategoriesScreen
import com.curio.app.features.onboarding.OnboardingScreen
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

/**
 * The Curio NavHost — single-NavHost scaffold for the placeholder phase.
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
 */
@Composable
fun CurioNavHost(
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    // Route-prefix match: destination.route returns the route TEMPLATE
    // (e.g. "spin/{categorySlug}"), not the resolved URL, so the old
    // exact-string `in bottomNavRoutes` check broke for any parameterised
    // route (the user-visible bug: bottom nav flashed during the
    // splash→home transition AND on SpinScreen-with-category because
    // "spin/{categorySlug}" wasn't in the set). Matching the first
    // path segment fixes both cases.
    val showBottomBar = remember(currentRoute) {
        val routePrefix = currentRoute?.substringBefore("/")
        routePrefix in CurioRoutes.bottomNavRoutePrefixes
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                CurioBottomBar(navController = navController)
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = CurioRoutes.SPLASH,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                    topicName    = entry.arguments?.getString("topicName").orEmpty(),
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
                    topicName    = entry.arguments?.getString("topicName").orEmpty(),
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
            composable(CurioRoutes.SETTINGS) {
                SettingsScreen(navController = navController)
            }
            composable(CurioRoutes.MANAGE_CATEGORIES) {
                ManageCategoriesScreen(navController = navController)
            }
            composable(CurioRoutes.TOPIC_HISTORY) {
                TopicHistoryScreen(navController = navController)
            }
            composable(
                route = CurioRoutes.LIGHTBOX,
                arguments = listOf(navArgument("imageUrl") { type = NavType.StringType })
            ) { entry ->
                LightboxScreen(
                    imageUrl = entry.arguments?.getString("imageUrl").orEmpty(),
                    navController = navController
                )
            }
        }
    }
}
