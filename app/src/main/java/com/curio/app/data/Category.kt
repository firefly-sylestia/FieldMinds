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
 */
data class CurioCategory(
    val id: CategoryId,
    val displayName: String,
    val accent: Color,
    val tint: Color,
    val iconGlyph: String,
    val isHidden: Boolean = false
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

    /** Returns the category for [id], or null if the id is unknown. */
    fun byId(id: CategoryId): CurioCategory? = all.firstOrNull { it.id == id }

    /** Returns the category whose routeSlug matches [slug], or null. */
    fun byRouteSlug(slug: String): CurioCategory? =
        all.firstOrNull { it.id.routeSlug == slug }

    /** Visible-only list — for Home/Cabinet chip rows. Once §13.4 Manage Categories lands, this filters by isHidden. */
    val visible: List<CurioCategory> = all.filterNot { it.isHidden }
}
