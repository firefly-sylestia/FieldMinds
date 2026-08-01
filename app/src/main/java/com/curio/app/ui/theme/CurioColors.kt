package com.curio.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import kotlin.math.pow

/**
 * Curio's color palette.
 *
 * Warm brand foundation (coral / butter / mint / cream) plus the researched
 * category palette: Tailwind-700 harmonized accents with light 300-level
 * ink twins (see [com.curio.app.ui.theme.categoryInk]). All colors are
 * opaque; card surfaces use solid category gradients with shadow elevation
 * for depth.
 */
object CurioColors {

    // ── Warm pastel foundation ─────────────────────────────────────────
    val CoralBlush       = Color(0xFFFF8FA3)  // Soft pink — primary
    val ButterYellow     = Color(0xFFFFD97D)  // Warm butter — secondary
    val SkyMint          = Color(0xFF8FE3CF)  // Soft mint — tertiary
    val CreamWhite       = Color(0xFFFFFBF5)  // Warm white — surface
    val SoftSand         = Color(0xFFF6EFE4)  // Warm sand — surface container
    val WarmCoralRed     = Color(0xFFE4626F)  // Soft coral-red — error
    val DeepPlum         = Color(0xFF3B0A17)  // Deep maroon — on-primary

    // ── Category accents (researched palette) ──────────────────────────
    // Tailwind-700 harmonized shades: deep enough that WHITE content clears
    // WCAG AA (>= 4.5:1) on every accent, yet vivid enough to stay rich on
    // the cream paper surface. Each deep accent pairs with a light 300-level
    // "ink" twin for accent-colored text/icons on the midnight dark surfaces
    // (resolved theme-aware via categoryInk()).
    val CategoryIndigo   = Color(0xFF4338CA)  // Music — Artists / Albums
    val CategoryRose     = Color(0xFFBE123C)  // Movies — Directors / Films
    val CategoryAmber    = Color(0xFFB45309)  // Books — Authors / Books
    val CategoryTeal     = Color(0xFF0F766E)  // Visual Art — Painters / Artworks
    val CategorySky      = Color(0xFF0369A1)  // Science — Scientists / Discoveries
    val CategoryCoral    = CoralBlush  // Wildcard — the app's brand primary, not a deep accent

    /** Light 300-level twins for accent-colored ink on dark surfaces. */
    val CategoryIndigoInk = Color(0xFFA5B4FC)
    val CategoryRoseInk   = Color(0xFFFDA4AF)
    val CategoryAmberInk  = Color(0xFFFCD34D)
    val CategoryTealInk   = Color(0xFF5EEAD4)
    val CategorySkyInk    = Color(0xFF7DD3FC)
    val CategoryCoralInk  = Color(0xFFFFC2CE)  // light coral twin for dark-surface ink

    /** Tinted (20% alpha) washes of the researched category accents. */
    val CategoryIndigoTint = CategoryIndigo.copy(alpha = 0.20f)
    val CategoryRoseTint   = CategoryRose.copy(alpha = 0.20f)
    val CategoryAmberTint  = CategoryAmber.copy(alpha = 0.20f)
    val CategoryTealTint   = CategoryTeal.copy(alpha = 0.20f)
    val CategorySkyTint    = CategorySky.copy(alpha = 0.20f)
    val CategoryCoralTint  = CategoryCoral.copy(alpha = 0.20f)

    /**
     * Legacy warm pastels — retained ONLY for brand/decorative use
     * (profile stat icons, wildcard rainbow gradient). Categories now use
     * the researched [CategoryIndigo]..[CategorySky] tokens above plus the
     * brand-primary [CategoryCoral] used by the Wildcard.
     */
    val Lilac            = Color(0xFFC9A6F2)  // legacy soft purple
    val DustyBlue        = Color(0xFF9BB8E8)  // legacy soft blue
    val Sage             = Color(0xFFA8C99A)  // legacy soft green
    val Peach            = Color(0xFFFFB585)  // legacy soft orange
    val Teal             = Color(0xFF6FC7BE)  // legacy soft teal

    /** Tinted (20% alpha) versions of the legacy accents for backgrounds. */
    val LilacTint     = Lilac.copy(alpha = 0.20f)
    val DustyBlueTint = DustyBlue.copy(alpha = 0.20f)
    val SageTint      = Sage.copy(alpha = 0.20f)
    val PeachTint     = Peach.copy(alpha = 0.20f)
    val TealTint      = Teal.copy(alpha = 0.20f)

    /**
     * Warm taupe-gray watermark ink for the light surface. The onSurface
     * maroon reads muddy at watermark sizes over cream, so the backdrop
     * uses this instead in light mode (drawn at ~16% alpha). Dark mode
     * keeps the near-white onSurface ghosts.
     */
    val WarmWatermarkInk = Color(0xFF8E8177)
}

/**
 * Solid gradient definitions for card surfaces. Every card gradient opens on
 * the same deepened accent used by the flat category cards ([categoryCardFill])
 * and fades toward the active theme's background — white in light mode, black
 * in dark — so cards always echo the app surface behind them.
 */
object CurioGradients {
    /** Warm sunset spectrum for the Wildcard — cohesive with the brand palette (decorative use only). */
    val WildcardGradientStops = listOf(
        CurioColors.CoralBlush,
        CurioColors.Peach,
        CurioColors.ButterYellow
    )

    /**
     * The flat fill used on category cards/chips — the same color every card
     * gradient opens on, so tiles and big cards can never drift apart. A
     * shallow deepen toward black keeps the hue rich while softening
     * brightness for the full-width tile treatment.
     */
    fun categoryCardFill(accent: Color): Color = lerp(accent, Color.Black, 0.10f)

    /**
     * Theme-aware category card gradient: opens on [categoryCardFill] (the
     * category card color) and softens toward the theme surface — white in
     * light mode, black in dark — so the card background always matches the
     * app's background shade.
     */
    @Composable
    fun cardGradient(accent: Color): List<Color> {
        val end = if (isCurioDarkTheme()) Color.Black else Color.White
        val start = categoryCardFill(accent)
        return listOf(start, lerp(start, end, 0.30f))
    }
}

/**
 * Mixed-deck color system for multi-category spins.
 *
 * When a user selects several categories in the picker, the deck no longer
 * speaks with the first category's accent — it blends all the chosen accents
 * into one premium color story:
 *
 *  - **Peek cards + spin button + confetti** use [mixedDeckAccent]: a curated
 *    blend of every selected accent (deduped), so the deck visibly mixes the
 *    user's picks.
 *  - **Hero card** uses [mixedDeckGradient]: a multi-accent gradient across
 *    the selected colors (Spotify/duotone-style), or the standard theme-aware
 *    single-accent gradient when only one distinct category is active.
 *
 * Every pair and triple blend below was verified against a canonical HSL
 * computation: pairs are shortest-hue-path midpoints with a saturation boost
 * (naive RGB/RGB-lerp midpoints pass through muddy gray, and teal↔amber /
 * sky↔amber cross the olive-green dead zone, so those are deliberately
 * steered to a richer jade/teal instead); triples are the order-independent
 * HSL centroid of the three accents. Every blend clears WCAG AA (4.5:1)
 * against white — any that fell short were deepened to the brightest shade
 * that still clears it, keeping mixes vivid with crisp white labels. Four+
 * accents use the runtime [hslCentroid] with the same 4.5:1 steering.
 */
object CurioMixedDeck {

    /** Curated premium blends for every pair of the six researched accents. */
    private val PairBlends: Map<Set<Color>, Color> = mapOf(
        // Indigo family mixes — violet-magenta, azure, petrol
        setOf(CurioColors.CategoryIndigo, CurioColors.CategoryRose)  to Color(0xFFA72CD6),
        setOf(CurioColors.CategoryIndigo, CurioColors.CategoryAmber) to Color(0xFFA926B5),
        setOf(CurioColors.CategoryIndigo, CurioColors.CategoryTeal)  to Color(0xFF1F62A8),
        setOf(CurioColors.CategoryIndigo, CurioColors.CategorySky)   to Color(0xFF1649C4),
        setOf(CurioColors.CategoryIndigo, CurioColors.CategoryCoral) to Color(0xFFBE39CE),
        // Rose family mixes — ember, violet, blush
        setOf(CurioColors.CategoryRose,   CurioColors.CategoryAmber) to Color(0xFFBF1E14),
        setOf(CurioColors.CategoryRose,   CurioColors.CategoryTeal)  to Color(0xFF4A12A8),
        setOf(CurioColors.CategoryRose,   CurioColors.CategorySky)   to Color(0xFF6D0BB8),
        setOf(CurioColors.CategoryRose,   CurioColors.CategoryCoral) to Color(0xFFEA1142),
        // Amber mixes — ember, jade (steered off the olive dead zone), flame
        setOf(CurioColors.CategoryAmber,  CurioColors.CategoryTeal)  to Color(0xFF15875A),
        setOf(CurioColors.CategoryAmber,  CurioColors.CategorySky)   to Color(0xFF0B8484),
        setOf(CurioColors.CategoryAmber,  CurioColors.CategoryCoral) to Color(0xFFE32D0F),
        // Teal / Sky / Coral family mixes
        setOf(CurioColors.CategoryTeal,   CurioColors.CategorySky)   to Color(0xFF067E94),
        setOf(CurioColors.CategoryTeal,   CurioColors.CategoryCoral) to Color(0xFF6C18F5),
        setOf(CurioColors.CategorySky,    CurioColors.CategoryCoral) to Color(0xFF9E1BFF)
    )

    /** Curated premium blends for every triple of the six researched accents. */
    private val TripleBlends: Map<Set<Color>, Color> = mapOf(
        // Indigo-anchored triples — magenta-rose, periwinkle, azure
        setOf(CurioColors.CategoryIndigo, CurioColors.CategoryRose,  CurioColors.CategoryAmber) to Color(0xFFE41772),
        setOf(CurioColors.CategoryIndigo, CurioColors.CategoryRose,  CurioColors.CategoryTeal)  to Color(0xFF7262EB),
        setOf(CurioColors.CategoryIndigo, CurioColors.CategoryRose,  CurioColors.CategorySky)   to Color(0xFF815AF1),
        setOf(CurioColors.CategoryIndigo, CurioColors.CategoryRose,  CurioColors.CategoryCoral) to Color(0xFFDD129E),
        setOf(CurioColors.CategoryIndigo, CurioColors.CategoryAmber, CurioColors.CategoryTeal)  to Color(0xFF2572E7),
        setOf(CurioColors.CategoryIndigo, CurioColors.CategoryAmber, CurioColors.CategorySky)   to Color(0xFF6563F4),
        setOf(CurioColors.CategoryIndigo, CurioColors.CategoryAmber, CurioColors.CategoryCoral) to Color(0xFFE70F66),
        setOf(CurioColors.CategoryIndigo, CurioColors.CategoryTeal,  CurioColors.CategorySky)   to Color(0xFF1479CB),
        setOf(CurioColors.CategoryIndigo, CurioColors.CategoryTeal,  CurioColors.CategoryCoral) to Color(0xFF6F61F1),
        setOf(CurioColors.CategoryIndigo, CurioColors.CategorySky,   CurioColors.CategoryCoral) to Color(0xFF8158F6),
        // Rose-anchored triples — burnt orange, crimson, magenta
        setOf(CurioColors.CategoryRose,   CurioColors.CategoryAmber, CurioColors.CategoryTeal)  to Color(0xFFD4450D),
        setOf(CurioColors.CategoryRose,   CurioColors.CategoryAmber, CurioColors.CategorySky)   to Color(0xFFEC0630),
        setOf(CurioColors.CategoryRose,   CurioColors.CategoryAmber, CurioColors.CategoryCoral) to Color(0xFFEE0505),
        setOf(CurioColors.CategoryRose,   CurioColors.CategoryTeal,  CurioColors.CategorySky)   to Color(0xFF0B76DC),
        setOf(CurioColors.CategoryRose,   CurioColors.CategoryTeal,  CurioColors.CategoryCoral) to Color(0xFFE90A57),
        setOf(CurioColors.CategoryRose,   CurioColors.CategorySky,   CurioColors.CategoryCoral) to Color(0xFFE20291),
        // Amber / Teal / Sky triples — jade, ember, azure (steered off olive)
        setOf(CurioColors.CategoryAmber,  CurioColors.CategoryTeal,  CurioColors.CategorySky)   to Color(0xFF058673),
        setOf(CurioColors.CategoryAmber,  CurioColors.CategoryTeal,  CurioColors.CategoryCoral) to Color(0xFFCF4B06),
        setOf(CurioColors.CategoryAmber,  CurioColors.CategorySky,   CurioColors.CategoryCoral) to Color(0xFFEE001A),
        setOf(CurioColors.CategoryTeal,   CurioColors.CategorySky,   CurioColors.CategoryCoral) to Color(0xFF0478D3)
    )

    /**
     * The single blend color a mixed deck wears on peeks, the spin button and
     * confetti. A single distinct accent returns itself unchanged; pairs and
     * triples use the curated tables; four+ use the order-independent
     * [hslCentroid] (circular hue mean), so every mix stays vivid, white-label
     * safe, and never depends on selection order.
     */
    fun mixedDeckAccent(accents: List<Color>): Color {
        // Color is a value class — value-based equality means distinct() alone
        // dedupes (toArgb() isn't part of the Compose BOM resolved here).
        val distinct = accents.distinct()
        return when (distinct.size) {
            0 -> CurioColors.CategoryCoral
            1 -> distinct.first()
            2 -> PairBlends[distinct.toSet()] ?: hslBlend(distinct[0], distinct[1])
            3 -> TripleBlends[distinct.toSet()] ?: hslCentroid(distinct)
            else -> hslCentroid(distinct)
        }
    }

    /**
     * Hero-card gradient stops. Single accent → the standard theme-aware
     * [CurioGradients.cardGradient]. Two+ accents → a multi-stop gradient
     * across the selected accents (capped at three stops so a five-way mix
     * doesn't slide into rainbow), which is what gives the mixed deck its
     * signature blended look.
     */
    @Composable
    fun mixedDeckGradient(accents: List<Color>): List<Color> {
        // Color is a value class — value-based equality means distinct() alone
        // dedupes (toArgb() isn't part of the Compose BOM resolved here).
        val distinct = accents.distinct()
        if (distinct.size <= 1) {
            return CurioGradients.cardGradient(mixedDeckAccent(distinct))
        }
        return distinct.take(3).map { CurioGradients.categoryCardFill(it) }
    }

    /**
     * HSL midpoint blend along the shortest hue path with a small saturation
     * boost — the premium way to mix two deep accents without a muddy middle.
     *
     * Uses local HSL conversion ([toHsl], [fromHsl]) built only on the bedrock
     * RGBA channels of [Color], so the blend stays version-proof across the
     * Compose BOM this project resolves (no hue/saturation/lightness
     * accessors, which aren't part of that API surface).
     */
    private fun hslBlend(a: Color, b: Color): Color {
        val ha = toHsl(a)
        val hb = toHsl(b)
        var dh = hb.h - ha.h
        if (dh > 180f) dh -= 360f
        if (dh < -180f) dh += 360f
        val hue = ((ha.h + dh / 2f) + 360f) % 360f
        // Boosted midpoint saturation keeps the blend vivid (naive averaging
        // can drift toward gray when the endpoints differ in lightness).
        val sat = ((ha.s + hb.s) / 2f + 0.05f).coerceIn(0f, 1f)
        val light = (ha.l + hb.l) / 2f
        return fromHsl(hue, sat, light)
    }

    /**
     * Order-independent blend for 4+ accents (and the fallback for any
     * unmapped pair/triple): circular mean of the hues, mean saturation with
     * a small boost, then lightness steered to the brightest shade that still
     * clears WCAG AA (4.5:1) against white — matching the curated tables.
     */
    private fun hslCentroid(colors: List<Color>): Color {
        val hs = colors.map { toHsl(it) }
        val n = hs.size.toFloat()
        var sx = 0f
        var sy = 0f
        for (h in hs) {
            val rad = h.h * (kotlin.math.PI.toFloat() / 180f)
            sx += kotlin.math.cos(rad)
            sy += kotlin.math.sin(rad)
        }
        val hue = (kotlin.math.atan2(sy, sx) * (180f / kotlin.math.PI.toFloat()) + 360f) % 360f
        val sat = (hs.sumOf { it.s.toDouble() }.toFloat() / n + 0.05f).coerceIn(0f, 1f)
        return steerLightness(hue, sat, 4.5f)
    }

    /** Brightest shade of (hue, sat) that still clears [target] contrast vs white. */
    private fun steerLightness(hue: Float, sat: Float, target: Float): Color {
        var lo = 0f
        var hi = 1f
        repeat(32) {
            val mid = (lo + hi) / 2f
            if (contrastVsWhite(fromHsl(hue, sat, mid)) >= target) lo = mid else hi = mid
        }
        return fromHsl(hue, sat, lo)
    }

    /** WCAG contrast ratio of [color] against white. */
    private fun contrastVsWhite(color: Color): Float {
        val l = 0.2126f * toLinear(color.red) + 0.7152f * toLinear(color.green) + 0.0722f * toLinear(color.blue)
        return 1.05f / (l + 0.05f)
    }

    /** sRGB channel → linear light (WCAG relative luminance). */
    private fun toLinear(c: Float): Float =
        if (c <= 0.03928f) c / 12.92f else ((c + 0.055f) / 1.055f).pow(2.4f)

    /** HSL components of a color, computed from its RGBA channels. */
    private data class Hsl(val h: Float, val s: Float, val l: Float)

    /** Standard RGB → HSL conversion (channels in [0,1], hue in degrees). */
    private fun toHsl(color: Color): Hsl {
        val r = color.red
        val g = color.green
        val b = color.blue
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val l = (max + min) / 2f
        val d = max - min
        val s = if (d == 0f) 0f else d / (1f - kotlin.math.abs(2f * l - 1f))
        val h = when {
            d == 0f -> 0f
            max == r -> ((g - b) / d) % 6f
            max == g -> (b - r) / d + 2f
            else -> (r - g) / d + 4f
        } * 60f
        return Hsl((h + 360f) % 360f, s.coerceIn(0f, 1f), l.coerceIn(0f, 1f))
    }

    /** Standard HSL → RGB conversion (hue in degrees, s/l in [0,1]). */
    private fun fromHsl(h: Float, s: Float, l: Float): Color {
        val c = (1f - kotlin.math.abs(2f * l - 1f)) * s
        val hp = h / 60f
        val x = c * (1f - kotlin.math.abs(hp % 2f - 1f))
        val (r, g, b) = when {
            hp < 1f -> Triple(c, x, 0f)
            hp < 2f -> Triple(x, c, 0f)
            hp < 3f -> Triple(0f, c, x)
            hp < 4f -> Triple(0f, x, c)
            hp < 5f -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }
        val m = l - c / 2f
        return Color(r + m, g + m, b + m)
    }
}
