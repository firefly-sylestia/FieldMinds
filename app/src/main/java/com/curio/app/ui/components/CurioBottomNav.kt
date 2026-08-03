package com.curio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.curio.app.navigation.CurioRoutes
import com.curio.app.navigation.navigateToTab
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons

/**
 * Out-of-band handoff for the Spin page's category tint wash — published by
 * [com.curio.app.features.spin.SpinScreen] and consumed by [CurioBottomBar]
 * so the Scaffold-level nav bar can blend with the tinted Spin page (the bar
 * lives outside the NavHost content and can't read SpinScreen's state
 * directly). Mirrors the [com.curio.app.navigation.LightboxTarget] pattern.
 *
 * Only the Spin routes are tinted (the nav bar's own route check gates it)
 * — Home and Cabinet stay on the plain theme surface, matching their plain
 * pages.
 */
object CurioNavTint {
    var spinWash by mutableStateOf<Color?>(null)
        private set

    fun publishSpinWash(color: Color?) {
        spinWash = color
    }
}

/**
 * Curio's persistent bottom navigation — see CURIO_SPEC.md §1 + §3.
 *
 * Three destinations:
 *   [ Home ]   [ Shuffle ]   [ Cabinet ]
 *
 * Tapping a tab uses the standard Compose pattern: navigate with
 * popUpTo(startDestination) + saveState=true + restoreState=true +
 * launchSingleTop=true. This preserves each tab's back stack across
 * switches and avoids re-creating the screen UI from scratch.
 *
 * The bar is hidden outside of [CurioRoutes.bottomNavRoutes] by the
 * parent scaffold (see CurioNavHost). This composable assumes it IS visible.
 */
data class CurioBottomDestination(
    val route: String,
    val label: String,
    val icon: String,
    val selectedIcon: String = icon
)

object CurioBottomNavItems {
    val Home = CurioBottomDestination(
        route = CurioRoutes.HOME,
        label = "Home",
        icon = CurioIcons.Home
    )
    val Shuffle = CurioBottomDestination(
        route = CurioRoutes.SPIN,
        label = "Shuffle",
        icon = CurioIcons.AutoAwesome,
        selectedIcon = CurioIcons.AutoAwesome
    )
    val Cabinet = CurioBottomDestination(
        route = CurioRoutes.CABINET,
        label = "Cabinet",
        icon = CurioIcons.Inventory2,
        selectedIcon = CurioIcons.Inventory2
    )

    val all: List<CurioBottomDestination> = listOf(Home, Shuffle, Cabinet)
}

@Composable
fun CurioBottomBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val routePrefix = currentRoute?.substringBefore("/")

    // The nav bar wears the Spin page's category tint wash ONLY on the Spin
    // routes (tab + spin/{categorySlug}) — the screen that actually has a
    // tinted page. Home and Cabinet stay on the plain theme surface (their
    // pages are plain), so the bar never tints where the page doesn't.
    val containerColor = if (routePrefix == CurioRoutes.SPIN) {
        CurioNavTint.spinWash ?: MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surface
    }

    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp),
        containerColor = containerColor,
        tonalElevation = 0.dp,
        windowInsets = WindowInsets.navigationBars
    ) {
        CurioBottomNavItems.all.forEach { destination ->
            // The hierarchy walk handles nested-graph destinations; today all routes are flat
            // so the hierarchy contains exactly the current route + start destination.
            val selected = navBackStackEntry?.destination?.hierarchy?.any { routeEntry ->
                routeEntry.route == destination.route ||
                    routeEntry.route?.substringBefore("/") == destination.route
            } == true

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (currentRoute != destination.route) {
                        // Anchor to HOME (the persistent root), not the
                        // graph start destination: SPLASH is popped on
                        // launch, so popUpTo(startDestination) would be a
                        // no-op and tab switches would pile up duplicates.
                        navController.navigateToTab(destination.route)
                    }
                },
                icon = {
                    CurioIcon(
                        name = if (selected) destination.selectedIcon else destination.icon,
                        contentDescription = destination.label,
                        tint = if (selected)
                            MaterialTheme.colorScheme.onSecondaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        size = 24.dp
                    )
                },
                label = {
                    Text(
                        text = destination.label,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
