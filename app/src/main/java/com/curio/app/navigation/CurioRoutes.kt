package com.curio.app.navigation

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.mutableIntStateOf
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

/**
 * Out-of-band handoff for the "Done exploring" notification action.
 *
 * The action's broadcast receiver tears the session down and launches
 * MainActivity with the topic's category slug + name as extras. The extras
 * are stashed here (like [LightboxTarget]) because MainActivity may be
 * cold-started (onCreate) or already running (onNewIntent), and the NavHost
 * is the only place that can navigate to the write-it-down entry page with
 * a HOME-anchored back stack. The NavHost consumes the target once it is on
 * a stable root route.
 */
object PendingEntryOpen {
    const val EXTRA_CATEGORY_SLUG = "com.curio.app.extra.OPEN_ENTRY_CATEGORY_SLUG"
    const val EXTRA_TOPIC_NAME = "com.curio.app.extra.OPEN_ENTRY_TOPIC_NAME"

    private var categorySlug: String? = null
    private var topicName: String? = null
    // Compose-observable bump: capture() may run from MainActivity (outside
    // composition), so the NavHost must recompose when it fires — a plain
    // Int would never invalidate the LaunchedEffect key.
    private val counter = mutableIntStateOf(0)

    /** Stashes a deep-link target carried by [intent], if one is present. */
    fun capture(intent: Intent?) {
        val slug = intent?.getStringExtra(EXTRA_CATEGORY_SLUG)
        val name = intent?.getStringExtra(EXTRA_TOPIC_NAME)
        if (slug != null && name != null) {
            categorySlug = slug
            topicName = name
            counter.intValue++
        }
    }

    /** Monotonic bump — the NavHost keys its open-effect on this. */
    val trigger: Int get() = counter.intValue

    /** Consumes and returns the pending target, if one is set. */
    fun take(): Pair<String, String>? {
        val slug = categorySlug ?: return null
        val name = topicName ?: return null
        categorySlug = null
        topicName = null
        return slug to name
    }
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
    const val EDIT_MOODBOARD = "edit-moodboard/{entryId}"
    const val EDIT_ENTRY = "edit-entry/{entryId}"
    const val SETTINGS = "settings"
    const val MANAGE_CATEGORIES = "manage-categories"
    const val TOPIC_HISTORY = "topic-history"
    const val RECENTS_ALL = "recents"
    const val LIGHTBOX = "lightbox"
    const val CRASH = "crash"
    const val BUG_REPORT = "bug-report"
    const val FIELDMIND_OBSERVATION = "fieldmind-observation"

    // ── Route builders ──────────────────────────────────────────────────────
    fun spinWithCategory(slug: String) = "spin/$slug"
    /** Multi-category launch — comma-joined slugs ("spin/artists,albums"). */
    fun spinWithCategories(slugs: List<String>) = "spin/${slugs.joinToString(",")}"
    fun revealFor(categorySlug: String, topicName: String) =
        "reveal/$categorySlug/${Uri.encode(topicName)}"
    fun captureFor(categorySlug: String, topicName: String) =
        "capture/$categorySlug/${Uri.encode(topicName)}"
    fun entryDetail(entryId: String) = "detail/$entryId"
    /** Edit a saved GalleryWall (mood board) entry — preloads + re-saves in place. */
    fun editMoodBoard(entryId: String) = "edit-moodboard/$entryId"
    /**
     * Edit a saved multi-section entry — reopens every take (the whole
     * Portfolio) in the universal editor, preloaded + re-saved in place.
     */
    fun editEntry(entryId: String) = "edit-entry/$entryId"
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

    /**
     * Route PREFIXES that own navigation during app boot (splash → home /
     * onboarding / crash gate). The NavHost waits for these to finish before
     * acting on a deep-linked entry open.
     */
    val bootGatePrefixes: Set<String> = setOf(SPLASH, ONBOARDING, CRASH)
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
