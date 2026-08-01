package com.curio.app.navigation

import android.net.Uri
import androidx.navigation.NavController

/**
 * Centralized route names for the Curio NavHost — see CURIO_SPEC.md §1.
 *
 * Routes that take arguments use placeholder-path syntax that maps directly
 * to Compose Navigation `composable("route/{argName}")` patterns. The
 * bottom-nav tab routes are flat (no nested graph) because the placeholder
 * phase keeps everything in one NavHost — switching tabs uses saveState /
 * restoreState so back-stack inside each tab is preserved.
 */
/**
 * Out-of-band handoff for the Lightbox target URI.
 *
 * The image URI is passed here (not through the nav route string) because
 * Compose Navigation auto-decodes path arguments — combined with a second
 * decode in the NavHost this corrupts percent-encoded content URIs and the
 * image never loads. Setting [uri] right before navigating and reading it in
 * the Lightbox keeps the URI byte-for-byte intact.
 */
object LightboxTarget {
    var uri: String? = null
}

object CurioRoutes {

    // ── Bottom-nav tabs (always rendered with the bottom nav bar)
    const val HOME = "home"
    const val SPIN = "spin"
    const val CABINET = "cabinet"

    // ── Splash / gates (no bottom nav)
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"

    // ── Inside the Spin flow (no bottom nav)
    const val PICKER = "picker"
    const val SPIN_WITH_CATEGORY = "spin/{categorySlug}"
    const val REVEAL = "reveal/{categorySlug}/{topicName}"
    const val CAPTURE = "capture/{categorySlug}/{topicName}"

    // ── Push destinations (no bottom nav)
    const val PROFILE = "profile"
    const val ENTRY_DETAIL = "detail/{entryId}"
    const val SETTINGS = "settings"
    const val MANAGE_CATEGORIES = "manage-categories"
    const val TOPIC_HISTORY = "topic-history"
    const val LIGHTBOX = "lightbox"
    const val CRASH = "crash"
    const val BUG_REPORT = "bug-report"

    // ── Route builders ──────────────────────────────────────────────────────
    fun spinWithCategory(slug: String) = "spin/$slug"
    fun revealFor(categorySlug: String, topicName: String) =
        "reveal/$categorySlug/${Uri.encode(topicName)}"
    fun captureFor(categorySlug: String, topicName: String) =
        "capture/$categorySlug/${Uri.encode(topicName)}"
    fun entryDetail(entryId: String) = "detail/$entryId"
    /** Sets the out-of-band target and returns the arg-free Lightbox route. */
    fun lightbox(imageUrl: String): String {
        LightboxTarget.uri = imageUrl
        return LIGHTBOX
    }

    /** Routes where the bottom navigation bar should be visible. */
    val bottomNavRoutes: Set<String> = setOf(HOME, SPIN, CABINET)

    /**
     * Route PREFIXES where the bottom navigation bar should be visible.
     * Use this (not [bottomNavRoutes]) when checking `destination.route`
     * — the Nav library returns the route TEMPLATE (e.g.
     * `spin/{categorySlug}`), not the resolved URL, so exact-string
     * membership fails for any parameterised route. The previous check
     * `currentRoute in bottomNavRoutes` hid the bar when on
     * `spin/{categorySlug}` (the Spin screen WITH a category), which is
     * exactly the user-visible splash-nav bug.
     */
    val bottomNavRoutePrefixes: Set<String> = setOf(HOME, SPIN, CABINET)
}

/**
 * Standard bottom-nav tab switch: pop the back stack back to the persistent
 * HOME root (saving the popped states), then navigate to [route] with
 * `launchSingleTop` + `restoreState` so every tab keeps at most one entry on
 * the back stack while preserving its scroll/UI state across switches.
 *
 * Anchored to HOME — NOT `graph.findStartDestination()`: the NavHost's
 * declared start destination (SPLASH) is popped inclusively on launch, so
 * `popUpTo(findStartDestination())` would target a destination that is no
 * longer in the stack — a silent no-op that lets every tab switch and every
 * re-opened screen pile up duplicate back-stack entries (back then walks
 * through the same screens repeatedly). HOME is the persistent root that
 * always remains after Splash/Onboarding/Crash routes land.
 */
fun NavController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(CurioRoutes.HOME) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
