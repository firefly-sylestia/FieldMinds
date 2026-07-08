package fieldmind.research.app.features.field.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import fieldmind.research.app.shared.presentation.components.icons.MaterialSymbolIcon
import fieldmind.research.app.ui.theme.getCustomColorScheme
import fieldmind.research.app.ui.theme.getTypographyForFont

/**
 * FieldMind brand theme.
 *
 * Provides a dedicated "field notebook" palette (forest green + warm ochre) as the
 * default brand, with an opt-in Material You / dynamic-color path that auto-adapts to the
 * system wallpaper and light/dark setting. Layered on top is [FieldMindColors], a set of
 * semantic colors for research entity types and confidence levels used consistently across
 * cards, chips, charts, and the knowledge graph.
 */

// ── Brand palette: light ──────────────────────────────────────────────
private val BrandPrimaryLight = Color(0xFF1F6B4C)
private val BrandLight = lightColorScheme(
    primary = BrandPrimaryLight,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFA8F2C8),
    onPrimaryContainer = Color(0xFF00210F),
    secondary = Color(0xFF4F6353),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD2E8D4),
    onSecondaryContainer = Color(0xFF0C1F13),
    tertiary = Color(0xFF8A5A00),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDDB0),
    onTertiaryContainer = Color(0xFF2C1700),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFAF9F7),
    onBackground = Color(0xFF1C1B19),
    surface = Color(0xFFFAF9F7),
    onSurface = Color(0xFF1C1B19),
    surfaceVariant = Color(0xFFDEE3DB),
    onSurfaceVariant = Color(0xFF4F4846),
    outline = Color(0xFF8B7D75),
    outlineVariant = Color(0xFFD0C4BB),
    inverseSurface = Color(0xFF31302B),
    inverseOnSurface = Color(0xFFF2F0E8),
    inversePrimary = Color(0xFF8DD5A9),
    surfaceDim = Color(0xFFDCD8D0),
    surfaceBright = Color(0xFFFAF9F7),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF3F0E8),
    surfaceContainer = Color(0xFFEDE9E0),
    surfaceContainerHigh = Color(0xFFE7E3DA),
    surfaceContainerHighest = Color(0xFFE1DDD3),
    scrim = Color(0xFF000000)
)

// ── Brand palette: dark ───────────────────────────────────────────────
private val BrandDark = darkColorScheme(
    primary = Color(0xFF8DD5A9),
    onPrimary = Color(0xFF00391E),
    primaryContainer = Color(0xFF005230),
    onPrimaryContainer = Color(0xFFA8F2C8),
    secondary = Color(0xFFB6CCB9),
    onSecondary = Color(0xFF213528),
    secondaryContainer = Color(0xFF374B3D),
    onSecondaryContainer = Color(0xFFD2E8D4),
    tertiary = Color(0xFFFFB951),
    onTertiary = Color(0xFF492C00),
    tertiaryContainer = Color(0xFF694200),
    onTertiaryContainer = Color(0xFFFFDDB0),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0F1409),
    onBackground = Color(0xFFE2E7DF),
    surface = Color(0xFF0F1409),
    onSurface = Color(0xFFE2E7DF),
    surfaceVariant = Color(0xFF4A4844),
    onSurfaceVariant = Color(0xFFC7C0B8),
    outline = Color(0xFF91897F),
    outlineVariant = Color(0xFF4A4844),
    inverseSurface = Color(0xFFE2E7DF),
    inverseOnSurface = Color(0xFF2D322C),
    inversePrimary = BrandPrimaryLight,
    surfaceDim = Color(0xFF0F1409),
    surfaceBright = Color(0xFF353A33),
    surfaceContainerLowest = Color(0xFF0A0E08),
    surfaceContainerLow = Color(0xFF171C15),
    surfaceContainer = Color(0xFF1B2019),
    surfaceContainerHigh = Color(0xFF252A23),
    surfaceContainerHighest = Color(0xFF2F342D),
    scrim = Color(0xFF000000)
)

/**
 * Semantic, research-specific colors layered on top of the Material color scheme.
 * Each research entity type and confidence level has a stable accent color that drives
 * badges, chips, chart series, and knowledge-graph edges.
 */
data class FieldMindColors(
    val isDark: Boolean,
    // ── Research entity colors ──
    val observation: Color,
    val question: Color,
    val hypothesis: Color,
    val project: Color,
    val source: Color,
    val note: Color,
    val task: Color,
    val folder: Color,
    val species: Color,
    val data: Color,
    val report: Color,
    val flashcard: Color,
    // ── Semantic state colors ──
    val positive: Color,
    val warning: Color,
    val info: Color,
    // ── Confidence level colors ──
    val confidenceSure: Color,
    val confidenceGuess: Color,
    val confidenceVerify: Color,
    /** Distinct, harmonious series colors for charts and per-category accents. */
    val categorical: List<Color>
) {
    /** Stable distinct accent for an arbitrary label (e.g. an observation category). */
    fun categoryColor(label: String): Color {
        if (label.isBlank()) return info
        val idx = (Math.floorMod(label.trim().lowercase().hashCode(), categorical.size))
        return categorical[idx]
    }

    /** Accent color for an entity kind keyword (case-insensitive). */
    fun accentFor(kind: String): Color = when (kind.trim().lowercase()) {
        "observation", "observations", "observe" -> observation
        "question", "questions" -> question
        "hypothesis", "hypotheses" -> hypothesis
        "project", "projects" -> project
        "source", "sources", "read", "reading", "library" -> source
        "note", "notes" -> note
        "task", "tasks" -> task
        "folder", "folders" -> folder
        "species" -> species
        "data", "data record", "datarecord" -> data
        "report", "reports" -> report
        "flashcard", "flashcards", "card", "cards" -> flashcard
        else -> info
    }

    /** Accent color for a confidence label. */
    fun confidenceColor(level: String): Color = when (level.trim().lowercase()) {
        "sure", "high", "confirmed" -> confidenceSure
        "guess", "low", "maybe" -> confidenceGuess
        else -> confidenceVerify
    }

    // ── isDark is provided by the constructor parameter above ──

    /**
     * Apply per-category color overrides from Settings.
     * Any entity type in [overrides] replaces its default color.
     * Entity types not in [overrides] keep their current default value.
     */
    fun applyOverrides(overrides: Map<String, Long>): FieldMindColors {
        if (overrides.isEmpty()) return this
        return copy(
            observation = overrides["observation"]?.let { Color(it) } ?: observation,
            question = overrides["question"]?.let { Color(it) } ?: question,
            hypothesis = overrides["hypothesis"]?.let { Color(it) } ?: hypothesis,
            project = overrides["project"]?.let { Color(it) } ?: project,
            source = overrides["source"]?.let { Color(it) } ?: source,
            note = overrides["note"]?.let { Color(it) } ?: note,
            task = overrides["task"]?.let { Color(it) } ?: task,
            folder = overrides["folder"]?.let { Color(it) } ?: folder,
            species = overrides["species"]?.let { Color(it) } ?: species,
            data = overrides["data"]?.let { Color(it) } ?: data,
            report = overrides["report"]?.let { Color(it) } ?: report,
            flashcard = overrides["flashcard"]?.let { Color(it) } ?: flashcard,
            positive = overrides["positive"]?.let { Color(it) } ?: positive,
            warning = overrides["warning"]?.let { Color(it) } ?: warning,
            info = overrides["info"]?.let { Color(it) } ?: info,
            confidenceSure = overrides["confidenceSure"]?.let { Color(it) } ?: confidenceSure,
            confidenceGuess = overrides["confidenceGuess"]?.let { Color(it) } ?: confidenceGuess,
            confidenceVerify = overrides["confidenceVerify"]?.let { Color(it) } ?: confidenceVerify
        )
    }
}

private val LightFieldMindColors = FieldMindColors(
    isDark = false,
    // Entity colors
    observation = Color(0xFF1F6B4C),
    question = Color(0xFF1565C0),
    hypothesis = Color(0xFF8B5000),
    project = Color(0xFF00695C),
    source = Color(0xFF5E35B1),
    note = Color(0xFF8E24AA),
    task = Color(0xFF00897B),
    folder = Color(0xFF6D4C41),
    species = Color(0xFF1F6B4C),
    data = Color(0xFF006D7A),
    report = Color(0xFFA1531F),
    flashcard = Color(0xFFE91E63),
    // State colors (distinct from entity colors)
    positive = Color(0xFF1F6B4C),
    warning = Color(0xFFE67E22),
    info = Color(0xFF546E7A),
    // Confidence colors (distinct from entity and state)
    confidenceSure = Color(0xFF1F6B4C),
    confidenceGuess = Color(0xFFF39C12),
    confidenceVerify = Color(0xFFE53935),
    categorical = listOf(
        Color(0xFF1F6B4C), // brand green — unified
        Color(0xFF1565C0), // question blue
        Color(0xFF8B5000), // hypothesis amber
        Color(0xFF5E35B1), // source violet
        Color(0xFF8E24AA), // note purple
        Color(0xFF00897B), // task teal
        Color(0xFF6D4C41), // folder brown
        Color(0xFF006D7A), // data teal
        Color(0xFFE91E63), // flashcard pink
        Color(0xFFE67E22), // warning orange
    )
)

// ============================================
// Midnight Flora — Forest entity colors (vibrant greens + amber + cyan)
// ============================================
private val FloraLightFieldMindColors = FieldMindColors(
    isDark = false,
    observation = Color(0xFF1A6B4C), // Deep forest emerald
    question = Color(0xFF1565C0),    // Rich sky blue
    hypothesis = Color(0xFFB8860B),  // Dark goldenrod amber
    project = Color(0xFF00695C),     // Deep teal
    source = Color(0xFF5E35B1),      // Rich plum
    note = Color(0xFFD45D5D),        // Warm coral rose
    task = Color(0xFF2E7D32),        // Fresh green
    folder = Color(0xFF6D4C41),      // Bark brown
    species = Color(0xFF2E7D32),     // Forest green
    data = Color(0xFF00838F),        // Vibrant cyan
    report = Color(0xFFBF360C),      // Burnt orange
    flashcard = Color(0xFFD81B60),   // Magenta
    positive = Color(0xFF1A6B4C),
    warning = Color(0xFFE67E22),
    info = Color(0xFF1565C0),
    confidenceSure = Color(0xFF1A6B4C),
    confidenceGuess = Color(0xFFF39C12),
    confidenceVerify = Color(0xFFE53935),
    categorical = listOf(
        Color(0xFF1A6B4C), // emerald
        Color(0xFF1565C0), // blue
        Color(0xFFB8860B), // amber
        Color(0xFF5E35B1), // plum
        Color(0xFFD45D5D), // coral
        Color(0xFF2E7D32), // green
        Color(0xFF00838F), // cyan
        Color(0xFF6D4C41), // brown
        Color(0xFFD81B60), // magenta
        Color(0xFFBF360C), // orange
    )
)

private val FloraDarkFieldMindColors = FieldMindColors(
    isDark = true,
    observation = Color(0xFF7DCDA0), // Soft green glow
    question = Color(0xFF64B5F6),    // Sky blue
    hypothesis = Color(0xFFF0C860),  // Warm amber glow
    project = Color(0xFF4DB6AC),     // Teal
    source = Color(0xFFB39DDB),      // Lavender
    note = Color(0xFFE57373),        // Soft coral
    task = Color(0xFF81C784),        // Mint green
    folder = Color(0xFFA1887F),      // Warm taupe
    species = Color(0xFF81C784),     // Mint
    data = Color(0xFF4DD0E1),        // Cyan glow
    report = Color(0xFFFF8A65),      // Soft orange
    flashcard = Color(0xFFF06292),   // Pink
    positive = Color(0xFF7DCDA0),
    warning = Color(0xFFFFD54F),
    info = Color(0xFF64B5F6),
    confidenceSure = Color(0xFF7DCDA0),
    confidenceGuess = Color(0xFFFFD54F),
    confidenceVerify = Color(0xFFEF9A9A),
    categorical = listOf(
        Color(0xFF7DCDA0),
        Color(0xFF64B5F6),
        Color(0xFFF0C860),
        Color(0xFFB39DDB),
        Color(0xFFE57373),
        Color(0xFF81C784),
        Color(0xFF4DD0E1),
        Color(0xFFA1887F),
        Color(0xFFF06292),
        Color(0xFFFF8A65),
    )
)

// ============================================
// Noir Amethyst — Deep violet entity colors (violet + blue + pink + cyan)
// ============================================
private val AmethystLightFieldMindColors = FieldMindColors(
    isDark = false,
    observation = Color(0xFF5B3E96), // Deep violet
    question = Color(0xFF1565C0),    // Electric blue
    hypothesis = Color(0xFFB8860B),  // Golden amber
    project = Color(0xFF283593),     // Deep indigo
    source = Color(0xFF7B1FA2),      // Rich amethyst
    note = Color(0xFFD4726A),        // Warm rose
    task = Color(0xFF00897B),        // Soft teal
    folder = Color(0xFF6D4C41),      // Warm grey-brown
    species = Color(0xFF5B3E96),     // Deep violet
    data = Color(0xFF00ACC1),        // Bright cyan
    report = Color(0xFFE65100),      // Warm orange
    flashcard = Color(0xFFE91E63),   // Vibrant pink
    positive = Color(0xFF5B3E96),
    warning = Color(0xFFE67E22),
    info = Color(0xFF1565C0),
    confidenceSure = Color(0xFF5B3E96),
    confidenceGuess = Color(0xFFF39C12),
    confidenceVerify = Color(0xFFE53935),
    categorical = listOf(
        Color(0xFF5B3E96), // violet
        Color(0xFF1565C0), // blue
        Color(0xFFB8860B), // amber
        Color(0xFF7B1FA2), // amethyst
        Color(0xFFD4726A), // rose
        Color(0xFF00897B), // teal
        Color(0xFF00ACC1), // cyan
        Color(0xFF6D4C41), // brown
        Color(0xFFE91E63), // pink
        Color(0xFFE65100), // orange
    )
)

private val AmethystDarkFieldMindColors = FieldMindColors(
    isDark = true,
    observation = Color(0xFFC4A5FF), // Lavender glow
    question = Color(0xFF64B5F6),    // Sky blue
    hypothesis = Color(0xFFF0C860),  // Amber glow
    project = Color(0xFF5C6BC0),     // Indigo
    source = Color(0xFFCE93D8),      // Soft amethyst
    note = Color(0xFFF0A098),        // Warm peach
    task = Color(0xFF4DB6AC),        // Teal
    folder = Color(0xFFA1887F),      // Warm taupe
    species = Color(0xFFC4A5FF),     // Lavender glow
    data = Color(0xFF4DD0E1),        // Cyan
    report = Color(0xFFFF8A65),      // Soft orange
    flashcard = Color(0xFFF48FB1),   // Pink
    positive = Color(0xFFC4A5FF),
    warning = Color(0xFFFFD54F),
    info = Color(0xFF64B5F6),
    confidenceSure = Color(0xFFC4A5FF),
    confidenceGuess = Color(0xFFFFD54F),
    confidenceVerify = Color(0xFFEF9A9A),
    categorical = listOf(
        Color(0xFFC4A5FF),
        Color(0xFF64B5F6),
        Color(0xFFF0C860),
        Color(0xFFCE93D8),
        Color(0xFFF0A098),
        Color(0xFF4DB6AC),
        Color(0xFF4DD0E1),
        Color(0xFFA1887F),
        Color(0xFFF48FB1),
        Color(0xFFFF8A65),
    )
)

// ============================================
// Warm Terrain — Earthy entity colors (olive + terracotta + sage + tan)
// ============================================
private val TerrainLightFieldMindColors = FieldMindColors(
    isDark = false,
    observation = Color(0xFF558B2F), // Olive green
    question = Color(0xFFAF5F00),    // Ochre yellow
    hypothesis = Color(0xFFC07050),  // Terracotta
    project = Color(0xFF6B7D6B),     // Sage green
    source = Color(0xFF8D6E63),      // Warm brown
    note = Color(0xFFD4726A),        // Warm rose
    task = Color(0xFF4E6B3E),        // Deep olive
    folder = Color(0xFF5D4037),      // Dark brown
    species = Color(0xFF558B2F),     // Olive
    data = Color(0xFF006D7A),        // Teal
    report = Color(0xFFBF360C),      // Burnt sienna
    flashcard = Color(0xFFE0616E),   // Warm pink
    positive = Color(0xFF558B2F),
    warning = Color(0xFFE67E22),
    info = Color(0xFF6B7D6B),
    confidenceSure = Color(0xFF558B2F),
    confidenceGuess = Color(0xFFF39C12),
    confidenceVerify = Color(0xFFE53935),
    categorical = listOf(
        Color(0xFF558B2F), // olive
        Color(0xFFAF5F00), // ochre
        Color(0xFFC07050), // terracotta
        Color(0xFF8D6E63), // brown
        Color(0xFFD4726A), // rose
        Color(0xFF4E6B3E), // deep olive
        Color(0xFF006D7A), // teal
        Color(0xFF5D4037), // dark brown
        Color(0xFFE0616E), // warm pink
        Color(0xFFBF360C), // sienna
    )
)

private val TerrainDarkFieldMindColors = FieldMindColors(
    isDark = true,
    observation = Color(0xFFA0BFA0), // Sage glow
    question = Color(0xFFE8A080),    // Terracotta glow
    hypothesis = Color(0xFFD4B896),  // Tan
    project = Color(0xFFA0B8A0),     // Sage
    source = Color(0xFFBCAAA4),      // Warm taupe
    note = Color(0xFFE8A080),        // Peach
    task = Color(0xFF81A881),        // Soft olive
    folder = Color(0xFF8D6E63),      // Warm brown
    species = Color(0xFFA0BFA0),     // Sage glow
    data = Color(0xFF80CBC4),        // Soft teal
    report = Color(0xFFFFAB91),      // Soft sienna
    flashcard = Color(0xFFF48FB1),   // Pink
    positive = Color(0xFFA0BFA0),
    warning = Color(0xFFFFD54F),
    info = Color(0xFFA0B8A0),
    confidenceSure = Color(0xFFA0BFA0),
    confidenceGuess = Color(0xFFFFD54F),
    confidenceVerify = Color(0xFFEF9A9A),
    categorical = listOf(
        Color(0xFFA0BFA0),
        Color(0xFFE8A080),
        Color(0xFFD4B896),
        Color(0xFFBCAAA4),
        Color(0xFFE8A080),
        Color(0xFF81A881),
        Color(0xFF80CBC4),
        Color(0xFF8D6E63),
        Color(0xFFF48FB1),
        Color(0xFFFFAB91),
    )
)

private val DarkFieldMindColors = FieldMindColors(
    isDark = true,
    // Entity colors
    observation = Color(0xFF8DD5A9),
    question = Color(0xFF90CAF9),
    hypothesis = Color(0xFFFFCC80),
    project = Color(0xFF80CBC4),
    source = Color(0xFFB39DDB),
    note = Color(0xFFCE93D8),
    task = Color(0xFF4DB6AC),
    folder = Color(0xFFBCAAA4),
    species = Color(0xFF8DD5A9),
    data = Color(0xFF80DEEA),
    report = Color(0xFFFFB74D),
    flashcard = Color(0xFFF48FB1),
    // State colors (distinct from entity colors)
    positive = Color(0xFF8DD5A9),
    warning = Color(0xFFFFB74D),
    info = Color(0xFFB0BEC5),
    // Confidence colors (distinct from entity and state)
    confidenceSure = Color(0xFF8DD5A9),
    confidenceGuess = Color(0xFFFFD54F),
    confidenceVerify = Color(0xFFEF9A9A),
    categorical = listOf(
        Color(0xFF8DD5A9), // brand green — unified
        Color(0xFF90CAF9), // question blue
        Color(0xFFFFCC80), // hypothesis amber
        Color(0xFFB39DDB), // source violet
        Color(0xFFCE93D8), // note purple
        Color(0xFF4DB6AC), // task teal
        Color(0xFFBCAAA4), // folder brown
        Color(0xFF80DEEA), // data teal
        Color(0xFFF48FB1), // flashcard pink
        Color(0xFFFFB74D), // warning orange
    )
)

val LocalFieldMindColors = staticCompositionLocalOf { LightFieldMindColors }

/**
 * Derives harmonious [FieldMindColors] from a Material [ColorScheme] by blending
 * the scheme's primary color with fixed hue targets for each entity type.
 * This ensures entity accent colors automatically adapt to any color scheme.
 */
private fun deriveFieldMindColors(colorScheme: ColorScheme, isDark: Boolean): FieldMindColors {
    fun blend(a: Color, b: Color, t: Float): Color = Color(
        (a.red * (1 - t) + b.red * t).coerceIn(0f, 1f),
        (a.green * (1 - t) + b.green * t).coerceIn(0f, 1f),
        (a.blue * (1 - t) + b.blue * t).coerceIn(0f, 1f),
        (a.alpha * (1 - t) + b.alpha * t).coerceIn(0f, 1f)
    )

    val p = colorScheme.primary

    // Fixed hue targets — each entity type maps to a distinct hue
    val green = Color(0xFF4CAF50)
    val blue = Color(0xFF2196F3)
    val amber = Color(0xFFFFC107)
    val teal = Color(0xFF009688)
    val purple = Color(0xFF9C27B0)
    val pink = Color(0xFFE91E63)
    val brown = Color(0xFF795548)
    val cyan = Color(0xFF00BCD4)
    val orange = Color(0xFFFF9800)
    val red = Color(0xFFF44336)

    return FieldMindColors(
        isDark = isDark,
        observation = blend(p, green, 0.55f),
        question = blend(p, blue, 0.55f),
        hypothesis = blend(p, amber, 0.55f),
        project = blend(p, teal, 0.5f),
        source = blend(p, purple, 0.5f),
        note = blend(p, pink, 0.5f),
        task = blend(p, teal, 0.35f),
        folder = blend(p, brown, 0.5f),
        species = blend(p, green, 0.65f),
        data = blend(p, cyan, 0.55f),
        report = blend(p, orange, 0.55f),
        flashcard = blend(p, pink, 0.65f),
        // State colors derived from scheme
        positive = blend(p, green, 0.4f),
        warning = blend(p, amber, 0.5f),
        info = blend(p, blue, 0.5f),
        // Confidence colors
        confidenceSure = blend(p, green, 0.3f),
        confidenceGuess = blend(p, amber, 0.5f),
        confidenceVerify = blend(p, red, 0.4f),
        // Categorical palette — 10 distinct hues
        categorical = listOf(
            blend(p, green, 0.55f),
            blend(p, blue, 0.55f),
            blend(p, amber, 0.55f),
            blend(p, purple, 0.5f),
            blend(p, pink, 0.5f),
            blend(p, teal, 0.4f),
            blend(p, cyan, 0.5f),
            blend(p, brown, 0.5f),
            blend(p, orange, 0.55f),
            blend(p, red, 0.4f),
        )
    )
}

/**
 * Accessor object so callers can read semantic colors via `FieldMindTheme.colors`.
 */
object FieldMindTheme {
    val colors: FieldMindColors
        @Composable
        @ReadOnlyComposable
        get() = LocalFieldMindColors.current
}

/**
 * Applies the FieldMind brand color scheme (or dynamic Material You colors) and provides
 * [FieldMindColors] to descendants. Typography and shapes are inherited from any outer
 * [MaterialTheme] so the rest of the app's font/shape choices are preserved.
 *
 * @param darkTheme follow the system/app dark setting so the theme auto-adapts.
 * @param dynamicColor when true and supported (Android 12+), use Material You wallpaper colors.
 */
/**
 * Default entity colors map used for the color picker UI reset functionality.
 * Matches [LightFieldMindColors] values.
 */
val DEFAULT_ENTITY_COLORS: Map<String, Long> = mapOf(
    "observation" to 0xFF1F6B4C,
    "question" to 0xFF1565C0,
    "hypothesis" to 0xFF8B5000,
    "project" to 0xFF00695C,
    "source" to 0xFF5E35B1,
    "note" to 0xFF8E24AA,
    "task" to 0xFF00897B,
    "folder" to 0xFF6D4C41,
    "species" to 0xFF1F6B4C,
    "data" to 0xFF006D7A,
    "report" to 0xFFA1531F,
    "flashcard" to 0xFFE91E63,
    "positive" to 0xFF1F6B4C,
    "warning" to 0xFFE67E22,
    "info" to 0xFF546E7A,
    "confidenceSure" to 0xFF1F6B4C,
    "confidenceGuess" to 0xFFF39C12,
    "confidenceVerify" to 0xFFE53935
)

/** Display labels for each entity color key. */
val ENTITY_COLOR_LABELS: Map<String, String> = mapOf(
    "observation" to "Observation",
    "question" to "Question",
    "hypothesis" to "Hypothesis",
    "project" to "Project",
    "source" to "Source",
    "note" to "Note",
    "task" to "Task",
    "folder" to "Folder",
    "species" to "Species",
    "data" to "Data",
    "report" to "Report",
    "flashcard" to "Flashcard",
    "positive" to "Positive (Success)",
    "warning" to "Warning",
    "info" to "Info",
    "confidenceSure" to "Confidence: Sure",
    "confidenceGuess" to "Confidence: Guess",
    "confidenceVerify" to "Confidence: Verify"
)

/** Icons for each entity color key. */
val ENTITY_COLOR_ICONS: Map<String, MaterialSymbolIcon> = mapOf(
    "observation" to MaterialSymbolIcon("visibility"),
    "question" to MaterialSymbolIcon("help"),
    "hypothesis" to MaterialSymbolIcon("lightbulb"),
    "project" to MaterialSymbolIcon("science"),
    "source" to MaterialSymbolIcon("menu_book"),
    "note" to MaterialSymbolIcon("edit_note"),
    "task" to MaterialSymbolIcon("checklist"),
    "folder" to MaterialSymbolIcon("folder"),
    "species" to MaterialSymbolIcon("pets"),
    "data" to MaterialSymbolIcon("bar_chart"),
    "report" to MaterialSymbolIcon("description"),
    "flashcard" to MaterialSymbolIcon("style"),
    "positive" to MaterialSymbolIcon("check_circle"),
    "warning" to MaterialSymbolIcon("warning"),
    "info" to MaterialSymbolIcon("info"),
    "confidenceSure" to MaterialSymbolIcon("thumb_up"),
    "confidenceGuess" to MaterialSymbolIcon("help_outline"),
    "confidenceVerify" to MaterialSymbolIcon("verified")
)

/**
 * Applies the FieldMind brand color scheme (or dynamic Material You colors) and provides
 * [FieldMindColors] to descendants. Typography and shapes are inherited from any outer
 * [MaterialTheme] so the rest of the app's font/shape choices are preserved.
 *
 * @param darkTheme follow the system/app dark setting so the theme auto-adapts.
 * @param dynamicColor when true and supported (Android 12+), use Material You wallpaper colors.
 * @param entityColorOverrides per-category color overrides from Settings (entity type → hex Long).
 */
@Composable
fun FieldMindTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    amoledTheme: Boolean = false,
    customColorScheme: String = "Default",
    entityColorOverrides: Map<String, Long> = emptyMap(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme: ColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        customColorScheme != "Default" -> getCustomColorScheme(customColorScheme, darkTheme)
        darkTheme -> BrandDark
        else -> BrandLight
    }.let { scheme ->
        // Apply AMOLED theme modifications if enabled and in dark mode
        if (amoledTheme && darkTheme) {
            scheme.copy(
                background = Color.Black,
                surface = Color.Black,
                surfaceVariant = Color(0xFF121212),
                surfaceContainer = Color(0xFF121212),
                surfaceContainerLow = Color(0xFF0A0A0A),
                surfaceContainerLowest = Color.Black,
                surfaceContainerHigh = Color(0xFF1E1E1E),
                surfaceContainerHighest = Color(0xFF2A2A2A),
                surfaceDim = Color.Black,
                surfaceBright = Color(0xFF2A2A2A)
            )
        } else scheme
    }
    val semantic = when {
            // Hand-tuned entity colors for Default scheme without dynamic color
            !dynamicColor && customColorScheme == "Default" ->
                if (darkTheme) DarkFieldMindColors else LightFieldMindColors
            // Midnight Flora — hand-tuned forest entity colors
            !dynamicColor && customColorScheme == "Midnight Flora" ->
                if (darkTheme) FloraDarkFieldMindColors else FloraLightFieldMindColors
            // Noir Amethyst — hand-tuned violet entity colors
            !dynamicColor && customColorScheme == "Noir Amethyst" ->
                if (darkTheme) AmethystDarkFieldMindColors else AmethystLightFieldMindColors
            // Warm Terrain — hand-tuned earthy entity colors
            !dynamicColor && customColorScheme == "Warm Terrain" ->
                if (darkTheme) TerrainDarkFieldMindColors else TerrainLightFieldMindColors
            // For dynamic colors, derive from the active scheme
            else -> deriveFieldMindColors(colorScheme, darkTheme)
        }
        .applyOverrides(entityColorOverrides)

    val geomTypography = getTypographyForFont("Geom")

    // ── Animated color scheme transition ──
    // Smoothly animate the 10 most visible color scheme properties
    // so the theme change feels fluid rather than a hard snap.
    val animSpec = tween<Color>(durationMillis = 400)
    val animatedPrimary by animateColorAsState(targetValue = colorScheme.primary, animationSpec = animSpec, label = "primary")
    val animatedOnPrimary by animateColorAsState(targetValue = colorScheme.onPrimary, animationSpec = animSpec, label = "onPrimary")
    val animatedPrimaryContainer by animateColorAsState(targetValue = colorScheme.primaryContainer, animationSpec = animSpec, label = "primaryContainer")
    val animatedOnPrimaryContainer by animateColorAsState(targetValue = colorScheme.onPrimaryContainer, animationSpec = animSpec, label = "onPrimaryContainer")
    val animatedSecondary by animateColorAsState(targetValue = colorScheme.secondary, animationSpec = animSpec, label = "secondary")
    val animatedOnSecondary by animateColorAsState(targetValue = colorScheme.onSecondary, animationSpec = animSpec, label = "onSecondary")
    val animatedTertiary by animateColorAsState(targetValue = colorScheme.tertiary, animationSpec = animSpec, label = "tertiary")
    val animatedOnTertiary by animateColorAsState(targetValue = colorScheme.onTertiary, animationSpec = animSpec, label = "onTertiary")
    val animatedBackground by animateColorAsState(targetValue = colorScheme.background, animationSpec = animSpec, label = "background")
    val animatedOnBackground by animateColorAsState(targetValue = colorScheme.onBackground, animationSpec = animSpec, label = "onBackground")
    val animatedSurface by animateColorAsState(targetValue = colorScheme.surface, animationSpec = animSpec, label = "surface")
    val animatedOnSurface by animateColorAsState(targetValue = colorScheme.onSurface, animationSpec = animSpec, label = "onSurface")
    val animatedSurfaceVariant by animateColorAsState(targetValue = colorScheme.surfaceVariant, animationSpec = animSpec, label = "surfaceVariant")
    val animatedOnSurfaceVariant by animateColorAsState(targetValue = colorScheme.onSurfaceVariant, animationSpec = animSpec, label = "onSurfaceVariant")
    val animatedOutline by animateColorAsState(targetValue = colorScheme.outline, animationSpec = animSpec, label = "outline")
    val animatedError by animateColorAsState(targetValue = colorScheme.error, animationSpec = animSpec, label = "error")

    val animatedColorScheme = colorScheme.copy(
        primary = animatedPrimary,
        onPrimary = animatedOnPrimary,
        primaryContainer = animatedPrimaryContainer,
        onPrimaryContainer = animatedOnPrimaryContainer,
        secondary = animatedSecondary,
        onSecondary = animatedOnSecondary,
        tertiary = animatedTertiary,
        onTertiary = animatedOnTertiary,
        background = animatedBackground,
        onBackground = animatedOnBackground,
        surface = animatedSurface,
        onSurface = animatedOnSurface,
        surfaceVariant = animatedSurfaceVariant,
        onSurfaceVariant = animatedOnSurfaceVariant,
        outline = animatedOutline,
        error = animatedError
    )

    // Animate surface container colors too for complete card/dialog depth transitions
    val animSpecTier = tween<Color>(durationMillis = 500)
    val animatedSurfaceContainerHighest by animateColorAsState(
        targetValue = colorScheme.surfaceContainerHighest,
        animationSpec = animSpecTier, label = "surfaceContainerHighest"
    )
    val animatedSurfaceContainerHigh by animateColorAsState(
        targetValue = colorScheme.surfaceContainerHigh,
        animationSpec = animSpecTier, label = "surfaceContainerHigh"
    )
    val animatedSurfaceContainer by animateColorAsState(
        targetValue = colorScheme.surfaceContainer,
        animationSpec = animSpecTier, label = "surfaceContainer"
    )
    val animatedSurfaceContainerLow by animateColorAsState(
        targetValue = colorScheme.surfaceContainerLow,
        animationSpec = animSpecTier, label = "surfaceContainerLow"
    )
    val animatedSurfaceContainerLowest by animateColorAsState(
        targetValue = colorScheme.surfaceContainerLowest,
        animationSpec = animSpecTier, label = "surfaceContainerLowest"
    )

    val fullAnimatedColorScheme = animatedColorScheme.copy(
        surfaceContainerLowest = animatedSurfaceContainerLowest,
        surfaceContainerLow = animatedSurfaceContainerLow,
        surfaceContainer = animatedSurfaceContainer,
        surfaceContainerHigh = animatedSurfaceContainerHigh,
        surfaceContainerHighest = animatedSurfaceContainerHighest
    )

    // ── System bar appearance ──
    // Set navigation bar to a translucent frosty version of the surface color
    // so it looks clean in light mode (white/frosty) and subtle in dark mode.
    // This overrides enableEdgeToEdge() which doesn't account for FieldMind's
    // manual theme mode control (System/Light/Dark independent of system setting).
    val view = LocalView.current
    val navBarColor = if (darkTheme) {
        colorScheme.surface.copy(alpha = 0.20f)
    } else {
        // Frosty translucent white in light mode
        colorScheme.surfaceContainerLow.copy(alpha = 0.85f)
    }
    SideEffect {
        if (!view.isInEditMode) {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            @Suppress("DEPRECATION")
            window.navigationBarColor = navBarColor.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalFieldMindColors provides semantic) {
        MaterialTheme(
            colorScheme = fullAnimatedColorScheme,
            typography = geomTypography,
            shapes = MaterialTheme.shapes,
            content = content
        )
    }
}

// ── Top-level opacity helpers ──
// These were originally member extension functions inside FieldMindColors.
// Moved top-level so they can be called from any file without dispatch-receiver scope.

/** Background tint for cards, chips – auto-adapts alpha to dark mode. */
fun Color.cardBg(isDark: Boolean): Color = copy(alpha = if (isDark) 0.22f else 0.14f)
/** Subtle border for selected state. */
fun Color.cardBorder(): Color = copy(alpha = 0.40f)
/** Muted text / secondary decoration. */
fun Color.muted(): Color = copy(alpha = 0.60f)

// ── Re-export for convenient single import ──
// import fieldmind.research.app.features.field.presentation.theme.MaterialSymbolIcon
// (MaterialSymbolIcon is already in components/icons package, not re-exported here)
