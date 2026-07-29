package com.curio.app.data

import androidx.compose.ui.graphics.Color
import com.curio.app.ui.theme.CurioColors

/**
 * Curio's 6 content categories — see CURIO_SPEC.md §0.2.
 *
 * The 6th category (Wildcard) uses a rainbow gradient rather than a flat tint,
 * so [CurioCategory.accent] is nullable for it. Wildcard-colored code paths
 * should look up the gradient from [com.curio.app.ui.theme.CurioGradients].
 *
 * The id enum uses UPPER_SNAKE so it survives serialization later (we don't
 * have a real DB yet, but this lets us swap to Room/DataStore cleanly when
 * logic lands in the next phase).
 */
enum class CategoryId {
    MUSIC,
    MOVIES,
    BOOKS,
    VISUAL_ART,
    SCIENCE,
    WILDCARD;

    companion object {
        fun fromRoute(slug: String): CategoryId? =
            values().firstOrNull { it.routeSlug == slug }

        val defaultOrder: List<CategoryId> = listOf(
            MUSIC, MOVIES, BOOKS, VISUAL_ART, SCIENCE, WILDCARD
        )
    }

    /** URL-safe slug used in navigation routes (`spin/music`, `picker/from-home`). */
    val routeSlug: String get() = when (this) {
        MUSIC      -> "music"
        MOVIES     -> "movies"
        BOOKS      -> "books"
        VISUAL_ART -> "visual_art"
        SCIENCE    -> "science"
        WILDCARD   -> "wildcard"
    }
}

/**
 * A category as rendered to the user — accent color, glyph, display name.
 * Visibility (hidden via §13.4 Manage Categories) is part of the state layer
 * but defaults to visible here.
 *
 * Two boolean flags, with different owners:
 *
 * - `isHidden` — **user-controlled**, set via Settings → Manage Categories
 *   (§13.4). When true, the category is filtered out of the Home chip row,
 *   Category Picker, and Cabinet filter chips. Past entries in hidden
 *   categories are preserved — they reappear the moment the user re-enables
 *   the category. Defaults to `false`.
 *
 * - `isReady` — **data-layer-controlled**, set when 100+ topics are authored
 *   + reviewed per CURIO_DATA_PLAN.md §1 + §5.2 step 5. When false, the
 *   category is filtered out of the chip row + Picker and surfaces as a
 *   "Coming soon" empty-state slot. Defaults to `false`; never flip to
 *   `true` without a corresponding data drop in `assets/topics/{id}.json`.
 *
 * The two flags are independent — a category can be `isReady = true` (data
 * shipped) and `isHidden = true` (user hid it) at the same time.
 */
data class CurioCategory(
    val id: CategoryId,
    val displayName: String,
    val accent: Color,
    val tint: Color,
    val iconGlyph: String,
    val isHidden: Boolean = false,
    val isReady: Boolean = false
)

/**
 * The canonical 6 Curio categories in the default order.
 *
 * Display names are resolved by the calling screen from string resources so
 * they can be localised; this list uses English placeholders so the
 * placeholder-phase previews have something to render. Once a string resource
 * pass lands these will become resource IDs.
 */
object CurioCategories {

    val all: List<CurioCategory> = listOf(
        CurioCategory(
            id           = CategoryId.MUSIC,
            displayName  = "Music",
            accent       = CurioColors.Lilac,
            tint         = CurioColors.LilacTint,
            iconGlyph    = "album"
        ),
        CurioCategory(
            id           = CategoryId.MOVIES,
            displayName  = "Movies",
            accent       = CurioColors.DustyBlue,
            tint         = CurioColors.DustyBlueTint,
            iconGlyph    = "movie"
        ),
        CurioCategory(
            id           = CategoryId.BOOKS,
            displayName  = "Books",
            accent       = CurioColors.Sage,
            tint         = CurioColors.SageTint,
            iconGlyph    = "menu_book"
        ),
        CurioCategory(
            id           = CategoryId.VISUAL_ART,
            displayName  = "Visual Art",
            accent       = CurioColors.Peach,
            tint         = CurioColors.PeachTint,
            iconGlyph    = "palette"
        ),
        CurioCategory(
            id           = CategoryId.SCIENCE,
            displayName  = "Science",
            accent       = CurioColors.Teal,
            tint         = CurioColors.TealTint,
            iconGlyph    = "science"
        ),
        CurioCategory(
            id           = CategoryId.WILDCARD,
            displayName  = "Wildcard",
            accent       = CurioColors.CoralBlush,    // fallback; UI uses the gradient
            tint         = CurioColors.CoralBlush.copy(alpha = 0.20f),
            iconGlyph    = "casino"
        )
    )

    init {
        // Fail at app startup (not deep inside a screen mid-session) if the
        // data layer drifts out of sync with the CategoryId enum. Without
        // this, a new CategoryId value added without a matching entry in
        // `all` would scatter NoSuchElementExceptions across every screen.
        check(all.size == CategoryId.values().size) {
            "CurioCategories.all has ${all.size} entries but CategoryId " +
            "has ${CategoryId.values().size} values; keep them in sync."
        }
    }

    /**
     * Returns the category for [id].
     *
     * The return type is non-nullable because [CategoryId.values()] is
     * 1:1 covered by [all] (verify at startup via the [init] block) — no
     * valid [CategoryId] can be missing a category entry. An unknown id is
     * a bug (new enum value added without a matching entry in `all`), and
     * we want to crash LOUDLY with a descriptive message rather than
     * silently render an unstyled UI or scatter opaque
     * `NoSuchElementException`s across screens.
     *
     * **Maintenance contract (important):** when adding a new [CategoryId]
     * value, you MUST also add a matching entry to `all` in the SAME commit.
     * The [init] check above will turn this from a runtime crash into a
     * startup-time crash message that names the mismatch directly.
     *
     * If you genuinely need "unknown id" handling (e.g. when parsing a route
     * slug from user input or external JSON), use [byRouteSlug] instead —
     * it stays nullable for that reason.
     */
    fun byId(id: CategoryId): CurioCategory =
        all.firstOrNull { it.id == id }
            ?: error(
                "CurioCategories.all has no entry for CategoryId.${id.name}. " +
                "Add a matching CurioCategory(...) to `all` so the count " +
                "matches CategoryId.values().size (currently " +
                "${CategoryId.values().size})."
            )

    /** Returns the category whose routeSlug matches [slug], or null. */
    fun byRouteSlug(slug: String): CurioCategory? =
        all.firstOrNull { it.id.routeSlug == slug }

    /** Visible-only list — for Home/Cabinet chip rows. Once §13.4 Manage Categories lands, this filters by isHidden. */
    val visible: List<CurioCategory> = all.filterNot { it.isHidden }
}
