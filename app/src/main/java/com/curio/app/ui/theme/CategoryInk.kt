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
    if (isCurioDarkTheme()) lightAccent else themedAccent()

/**
 * The accent color a category WEARS in the active theme style.
 *
 *  - Curio (default) and AMOLED: the researched accent unchanged — category
 *    identity stays exact.
 *  - Material: the accent is blended ~40% toward the device's dynamic
 *    Material primary, so every category keeps its hue but reads as a shade
 *    of the palette the device generated ("the material color according to
 *    the device"). The tint washes stay off in this style, but the category
 *    colors themselves are NOT turned off — they harmonize with the device
 *    theme instead of disappearing.
 */
@Composable
fun CurioCategory.themedAccent(): Color =
    if (AppPreferences.themeStyleState == AppPreferences.THEME_STYLE_MATERIAL) {
        lerp(accent, MaterialTheme.colorScheme.primary, 0.40f)
    } else {
        accent
    }

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
    if (!AppPreferences.tintWashEffective()) return background
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
    if (!AppPreferences.tintWashEffective()) return base
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
 * The mood board's tinted canvas — same resolution as [categorySurface]
 * but NOT gated by the theme STYLE: the AMOLED style blacks out category
 * tints app-wide, and the mood board's tinted surface is its identity, so
 * it keeps wearing the category mid-tone even on the pure-black style.
 * The manual Settings tint toggle is still honored — turning it off here
 * returns [base] unchanged just like [categorySurface].
 */
@Composable
fun CurioCategory.categorySurfaceMoodBoard(base: Color = MaterialTheme.colorScheme.surfaceContainerHigh): Color {
    if (!AppPreferences.tintWashEnabledState) return base
    return if (isCurioDarkTheme()) {
        val tuning = DARK_WASH_TUNING[family] ?: DEFAULT_DARK_WASH
        val midTone = tuning.resolveMidTone(accent, lightAccent)
        lerp(base, midTone, tuning.blendFraction + 0.30f)
    } else {
        lerp(base, lerp(accent, Color.White, 0.30f), 0.24f)
    }
}

/**
 * Theme-aware surface color for SMALL CATEGORY CHIPS (Cabinet filter pills).
 *
 * Chips sit directly on the washed page, so they need to read as distinct
 * tappable pills without shouting. Light mode matches [categorySurface]'s
 * soft cream tint. Dark mode deliberately differs from cards: the family
 * mid-tone is desaturated toward a neutral grey (deep accents otherwise
 * read muddy over midnight) and blended a touch stronger than the page wash
 * so the chip LIFTS off the tinted background instead of sinking into it —
 * less saturated, more contrast. The crisp edge comes from
 * [categoryBorder]'s light-twin hairline. Honors the Settings tint toggle —
 * when it's off, [base] is returned unchanged so chips go back to the plain
 * theme surface.
 */
@Composable
fun CurioCategory.categoryChipSurface(base: Color = MaterialTheme.colorScheme.surfaceContainerLow): Color {
    if (!AppPreferences.tintWashEffective()) return base
    return if (isCurioDarkTheme()) {
        val tuning = DARK_WASH_TUNING[family] ?: DEFAULT_DARK_WASH
        val midTone = tuning.resolveMidTone(accent, lightAccent)
        // Pull the mid-tone toward neutral grey (less saturated), then blend
        // harder than the page wash (which uses blendFraction) so the chip
        // reads brighter than the tinted background for contrast.
        val desaturated = lerp(midTone, Color(0xFF9AA3B0), 0.40f)
        lerp(base, desaturated, tuning.blendFraction + 0.40f)
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
    if (!AppPreferences.tintWashEffective()) return fallback
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
