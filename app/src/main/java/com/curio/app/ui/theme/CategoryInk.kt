package com.curio.app.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import com.curio.app.data.AppPreferences
import com.curio.app.data.CategoryFamily
import com.curio.app.data.CurioCategory

/**
 * Theme-aware ink color for a category's accent-colored TEXT and ICONS that
 * sit on plain surfaces.
 *
 * The researched category accents (Tailwind-700 depth) read beautifully as
 * *fills* — cards, chips, buttons, gradients — with white content on top.
 * But used as *ink* on the midnight dark surfaces they fall below readable
 * contrast (e.g. indigo-700 text on #111722 ≈ 1.9:1). Each category pairs
 * its deep accent with a light 300-level twin ([CurioCategory.lightAccent]);
 * this extension resolves the correct one for the active theme, so accent
 * text/icons stay readable in both light and dark mode.
 */
@Composable
fun CurioCategory.categoryInk(): Color =
    if (isCurioDarkTheme()) lightAccent else accent

/**
 * Theme-aware wash color for a category-aware page BACKGROUND (Spin, Topic
 * Reveal, Save/Capture, Cabinet filter).
 *
 * Light mode: the accent is first softened toward white (30%) then blended
 * at a gentler 14% over the cream background — lighter and whiter than the
 * original 20% wash, so the tint reads airy instead of dark.
 *
 * Dark mode: the deep Tailwind-700 accent alone at 20% reads muddy (amber
 * goes brown, teal goes grey-green), while its light 300-level twin at any
 * useful fraction reads WHITE-WASHED over the midnight surface. So this
 * builds a saturated mid-tone — the accent lerped partway toward its light
 * twin (≈ the 500-level shade) — and washes it at a moderate fraction. The
 * page keeps the category's hue with real color, never a washed-out grey-white.
 *
 * A few families need extra contrast: at the default 50% midpoint, rose
 * (movies), sky (science), amber (books — brown) and especially coral
 * (wildcard — its accent is already a pastel pink) read too
 * pale/white-washed. Those pull the mid-tone closer to the deep accent
 * (or a deep hue twin, for pale accents) and blend a touch stronger, so
 * the hue survives over midnight instead of flattening to grey-white.
 */
@Composable
fun CurioCategory.categoryBackgroundWash(): Color {
    val background = MaterialTheme.colorScheme.background
    // Settings toggle (v6.4): when the category tint is turned off, pages use
    // the plain theme background (cream in light, midnight in dark) exactly
    // as they did before the wash rollout.
    if (!AppPreferences.tintWashEnabledState) return background
    return if (isCurioDarkTheme()) {
        val tuning = DARK_WASH_TUNING[family] ?: DEFAULT_DARK_WASH
        val midTone = tuning.resolveMidTone(accent, lightAccent)
        lerp(background, midTone, tuning.blendFraction)
    } else {
        lerp(background, lerp(accent, Color.White, 0.30f), 0.14f)
    }
}

/**
 * Theme-aware surface color for CARDS that sit on a tinted page background.
 *
 * Cards used plain theme surfaces (cream in light, midnight grey in dark),
 * which look out of place sitting on a category-tinted page. This resolves
 * the same per-family mid-tone as [categoryBackgroundWash] but blends a
 * little stronger (markedly stronger in dark mode, where the wash stays
 * deep), so a card reads as a tinted elevated surface instead of a foreign
 * cream block. Honors the Settings tint toggle — when it's off, [base] is
 * returned unchanged so cards go back to the plain theme surface.
 */
@Composable
fun CurioCategory.categorySurface(base: Color = MaterialTheme.colorScheme.surfaceContainerLow): Color {
    if (!AppPreferences.tintWashEnabledState) return base
    return if (isCurioDarkTheme()) {
        val tuning = DARK_WASH_TUNING[family] ?: DEFAULT_DARK_WASH
        val midTone = tuning.resolveMidTone(accent, lightAccent)
        // Dark cards blend the proper dark mid-tone much harder than the
        // page wash (which stays deep) — same "cards = wash's stronger
        // sibling" relationship as light mode — so tiles and chips visibly
        // wear their category tint on the midnight page instead of sinking
        // into a near-invisible +0.10 whisper.
        lerp(base, midTone, tuning.blendFraction + 0.30f)
    } else {
        lerp(base, lerp(accent, Color.White, 0.30f), 0.24f)
    }
}

/**
 * Theme-aware border for CARDS and BUTTONS that wear a tinted surface on a
 * tinted page background.
 *
 * Tinted surfaces ([categorySurface], `category.tint`, etc.) sit on a
 * category-washed page, so without a rule they can visually melt into the
 * background. This returns a slim theme-aware edge — deep accent in light
 * mode, light twin in dark (same resolution as [categoryInk]) — at a low
 * alpha so the card/button reads as a distinct surface without a hard line.
 *
 * Honors the Settings tint toggle: when it's off, [fallback] is returned
 * (null by default = no border), so plain-theme pages keep their exact
 * pre-tint look.
 */
@Composable
fun CurioCategory.categoryBorder(fallback: BorderStroke? = null): BorderStroke? {
    if (!AppPreferences.tintWashEnabledState) return fallback
    return BorderStroke(1.dp, categoryInk().copy(alpha = 0.30f))
}

/**
 * Per-family dark-mode wash tuning.
 *
 * @param midToneFactor How far the mid-tone is pulled from the deep accent
 *   toward its light twin (lower = stays closer to the deep accent = darker).
 * @param blendFraction How strongly the mid-tone is blended over midnight.
 * @param darken Extra darkening of the mid-tone toward [deepTwin] (or black
 *   when no twin is given) — needed for families whose accent is itself
 *   pale (e.g. wildcard coral), where no mid-tone pull can reach a real
 *   shade on its own.
 * @param deepTwin A deeper shade of the same hue to darken toward. Falling
 *   back to black for pale accents (coral) produced a muddy grey-pink over
 *   midnight; a real deep pink twin keeps the hue while going dark.
 */
private class DarkWashTuning(
    val midToneFactor: Float,
    val blendFraction: Float,
    val darken: Float = 0f,
    val deepTwin: Color? = null
) {
    /** The wash mid-tone for this family — deepened toward [deepTwin]/black when tuned. */
    fun resolveMidTone(accent: Color, lightAccent: Color): Color {
        val midTone = lerp(accent, lightAccent, midToneFactor)
        if (darken <= 0f) return midTone
        return lerp(midTone, deepTwin ?: Color.Black, darken)
    }
}

private val DEFAULT_DARK_WASH = DarkWashTuning(0.5f, 0.15f)

private val DARK_WASH_TUNING: Map<CategoryFamily, DarkWashTuning> = mapOf(
    // Rose (movies, red) read whitewashed over midnight — hug the deep
    // accent (low factor) and deepen toward the shared deep-rose twin so
    // the wash is a dark #5E0034 burgundy instead of a pale rose.
    CategoryFamily.MOVIES  to DarkWashTuning(0.10f, 0.22f, darken = 0.60f, deepTwin = Color(0xFF5E0034)),
    // Sky (science, light blue) — slightly darker than the earlier deep-pull:
    // keep the azure hue but nudge the mid-tone a bit toward black so the
    // wash doesn't float pale-blue over midnight.
    CategoryFamily.SCIENCE to DarkWashTuning(0.12f, 0.20f, darken = 0.10f),
    // Amber (books, brown) — the accent is already a warm brown, but the
    // default 50% midpoint pulled it toward its gold twin and washed out.
    // Keep it near the deep amber and deepen toward a dark coffee brown.
    CategoryFamily.BOOKS   to DarkWashTuning(0.15f, 0.22f, darken = 0.35f, deepTwin = Color(0xFF78350F)),
    // Coral (wildcard, pink) is a pastel accent — no mid-tone pull gets it
    // dark, and deepening toward black turned it a muddy grey-pink. Deepen
    // toward the same deep-rose twin as the movies family so the wash is a
    // dark #5E0034 burgundy instead of a pale rose-pink.
    CategoryFamily.WILDCARD to DarkWashTuning(0.10f, 0.24f, darken = 0.60f, deepTwin = Color(0xFF5E0034))
)
