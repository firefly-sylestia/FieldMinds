package fieldmind.research.app.features.field.presentation.theme

import androidx.compose.ui.graphics.Color
import java.util.Calendar

/**
 * Seasonal color accents that subtly shift the app's highlight palette
 * based on the current month:
 * - **Spring** (Mar–May): Fresh green (#4CAF50)
 * - **Summer** (Jun–Aug): Golden sun (#FFB300)
 * - **Autumn** (Sep–Nov): Warm orange (#E65100)
 * - **Winter** (Dec–Feb): Cool blue (#42A5F5)
 *
 * The blend is subtle (~25%) — the base brand identity is preserved while
 * the overall tone gently shifts with the seasons.
 */
object SeasonalTheme {

    /** Blend factor applied to each entity color. 0.25 = 25% seasonal, 75% original. */
    private const val SEASONAL_BLEND_FACTOR = 0.25f

    /** Seasonal accent color for the current month. */
    val currentAccent: Color
        get() = accentForMonth(Calendar.getInstance().get(Calendar.MONTH))

    /** Description of the current season (for Settings display). */
    val currentSeasonName: String
        get() = seasonName(Calendar.getInstance().get(Calendar.MONTH))

    fun accentForMonth(month: Int): Color = when (month) {
        Calendar.MARCH, Calendar.APRIL, Calendar.MAY -> SPRING_GREEN
        Calendar.JUNE, Calendar.JULY, Calendar.AUGUST -> SUMMER_GOLD
        Calendar.SEPTEMBER, Calendar.OCTOBER, Calendar.NOVEMBER -> AUTUMN_ORANGE
        else -> WINTER_BLUE // December, January, February
    }

    private fun seasonName(month: Int): String = when (month) {
        Calendar.MARCH, Calendar.APRIL, Calendar.MAY -> "Spring"
        Calendar.JUNE, Calendar.JULY, Calendar.AUGUST -> "Summer"
        Calendar.SEPTEMBER, Calendar.OCTOBER, Calendar.NOVEMBER -> "Autumn"
        else -> "Winter"
    }

    /**
     * Blend a [FieldMindColors] instance with the current seasonal accent.
     * Each entity color is blended toward the seasonal accent using [SEASONAL_BLEND_FACTOR].
     */
    fun blend(colors: FieldMindColors, enabled: Boolean): FieldMindColors {
        if (!enabled) return colors
        val accent = currentAccent
        return colors.copy(
            observation = blendToward(colors.observation, accent),
            question = blendToward(colors.question, accent),
            hypothesis = blendToward(colors.hypothesis, accent),
            project = blendToward(colors.project, accent),
            source = blendToward(colors.source, accent),
            note = blendToward(colors.note, accent),
            task = blendToward(colors.task, accent),
            folder = blendToward(colors.folder, accent),
            species = blendToward(colors.species, accent),
            data = blendToward(colors.data, accent),
            report = blendToward(colors.report, accent),
            flashcard = blendToward(colors.flashcard, accent),
            positive = blendToward(colors.positive, accent),
            warning = blendToward(colors.warning, accent),
            info = blendToward(colors.info, accent),
            confidenceSure = blendToward(colors.confidenceSure, accent),
            confidenceGuess = blendToward(colors.confidenceGuess, accent),
            confidenceVerify = blendToward(colors.confidenceVerify, accent),
            categorical = colors.categorical.map { blendToward(it, accent) }
        )
    }

    /**
     * Linearly interpolate [color] toward [target] by [SEASONAL_BLEND_FACTOR].
     */
    private fun blendToward(color: Color, target: Color): Color {
        val t = SEASONAL_BLEND_FACTOR
        return Color(
            red = color.red * (1 - t) + target.red * t,
            green = color.green * (1 - t) + target.green * t,
            blue = color.blue * (1 - t) + target.blue * t,
            alpha = color.alpha
        )
    }

    // ── Season accent colors ──
    private val SPRING_GREEN = Color(0xFF4CAF50)
    private val SUMMER_GOLD = Color(0xFFFFB300)
    private val AUTUMN_ORANGE = Color(0xFFE65100)
    private val WINTER_BLUE = Color(0xFF42A5F5)
}
