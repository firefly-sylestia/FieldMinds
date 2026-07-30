package com.curio.app.navigation

import android.net.Uri

/**
 * Centralized route names for the Curio NavHost — see CURIO_SPEC.md §1.
 *
 * Routes that take arguments use placeholder-path syntax that maps directly
 * to Compose Navigation `composable("route/{argName}")` patterns. The
 * bottom-nav tab routes are flat (no nested graph) because the placeholder
 * phase keeps everything in one NavHost — switching tabs uses saveState /
 * restoreState so back-stack inside each tab is preserved.
 */
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
    const val LIGHTBOX = "lightbox/{imageUrl}"
    const val CRASH = "crash"
    const val BUG_REPORT = "bug-report"

    // ── Route builders ──────────────────────────────────────────────────────
    fun spinWithCategory(slug: String) = "spin/$slug"
    fun revealFor(categorySlug: String, topicName: String) =
        "reveal/$categorySlug/${Uri.encode(topicName)}"
    fun captureFor(categorySlug: String, topicName: String) =
        "capture/$categorySlug/${Uri.encode(topicName)}"
    fun entryDetail(entryId: String) = "detail/$entryId"
    fun lightbox(imageUrl: String) = "lightbox/${Uri.encode(imageUrl)}"

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
