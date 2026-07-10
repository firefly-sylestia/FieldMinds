package fieldmind.research.app.shared.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * ════════════════════════════════════════════════════════════════════════
 *  🌿 JournalStyle — Whimsical theming system
 *
 *  Purpose: Define 4 distinct journal aesthetics that users can choose
 *  from in Settings → Appearance. Each style transforms the entire app's
 *  visual personality — card shapes, background textures, color warmth,
 *  border treatments, shadow character, and overall vibe.
 *
 *  The 4 styles:
 *  1. [Victorian] — Aged parchment, copperplate elegance, scientific illustration
 *  2. [Sketchbook] — Cream paper, pencil-sketch charm, field notes feel
 *  3. [BulletJournal] — Clean dot-grid, hand-lettered headings, organized
 *  4. [Ghibli] — Watercolor washes, soft dreamy edges, whimsical warmth
 *
 *  Usage:
 *  ```kotlin
 *  val style = LocalJournalStyle.current
 *  Card(
 *      shape = style.cardShape,
 *      colors = CardDefaults.cardColors(containerColor = style.cardBackground)
 *  )
 *  ```
 * ════════════════════════════════════════════════════════════════════════
 */

// ── Common Keyed Enum Interface ──────────────────────────────────────

/**
 * Shared contract for the four phase-1 select enums (JournalStyle,
 * MicroDelightIntensity, BackgroundAnimationLevel, NavBarStyle). Enables a
 * single generic pill-radio picker in the Appearance settings page.
 */
interface KeyedEnum {
    /** Stable lowercase-snake_case key persisted in user settings. */
    val key: String
    /** Human-readable display name shown in UI. */
    val displayName: String
}

// ── Journal Style Enum ──────────────────────────────────────────────

enum class JournalStyle(
    override val key: String,
    override val displayName: String,
    val description: String
) : KeyedEnum {
    Victorian(
        key = "victorian",
        displayName = "Victorian Naturalist",
        description = "Aged parchment, copperplate elegance, scientific illustrations"
    ),
    Sketchbook(
        key = "sketchbook",
        displayName = "Explorer's Sketchbook",
        description = "Cream paper, pencil-sketch charm, field notebook feel"
    ),
    BulletJournal(
        key = "bullet_journal",
        displayName = "Modern Bullet Journal",
        description = "Clean dot-grid, hand-lettered headings, organized"
    ),
    Ghibli(
        key = "ghibli",
        displayName = "Ghibli Storybook",
        description = "Watercolor washes, soft dreamy edges, whimsical warmth"
    );

    companion object {
        fun fromKey(key: String): JournalStyle =
            entries.find { it.key == key } ?: Sketchbook

        val default: JournalStyle = Sketchbook
    }
}

// ── Micro-Delight Intensity ─────────────────────────────────────────

enum class MicroDelightIntensity(
    override val key: String,
    override val displayName: String,
    val description: String
) : KeyedEnum {
    Minimal(
        key = "minimal",
        displayName = "Minimal",
        description = "Subtle haptics, gentle transitions"
    ),
    Normal(
        key = "normal",
        displayName = "Normal",
        description = "Celebrations, animations, streak effects"
    ),
    Maximum(
        key = "maximum",
        displayName = "Maximum",
        description = "Butterflies, fireflies, leaf showers, ambiance"
    );

    companion object {
        fun fromKey(key: String): MicroDelightIntensity =
            entries.find { it.key == key } ?: Normal

        val default: MicroDelightIntensity = Normal
    }
}

// ── Background Animation Level ──────────────────────────────────────

enum class BackgroundAnimationLevel(
    override val key: String,
    override val displayName: String,
    val description: String
) : KeyedEnum {
    Static(
        key = "static",
        displayName = "Static",
        description = "Flat color or gradient background"
    ),
    Gentle(
        key = "gentle",
        displayName = "Gentle",
        description = "Subtle parallax, slow cloud drift"
    ),
    Full(
        key = "full",
        displayName = "Full",
        description = "Time-of-day scenes, animated weather, fireflies"
    );

    companion object {
        fun fromKey(key: String): BackgroundAnimationLevel =
            entries.find { it.key == key } ?: Gentle

        val default: BackgroundAnimationLevel = Gentle
    }
}

// ── Nav Bar Style ───────────────────────────────────────────────────

enum class NavBarStyle(
    override val key: String,
    override val displayName: String,
    val description: String
) : KeyedEnum {
    Modern(
        key = "modern",
        displayName = "Modern",
        description = "Sleek pill with liquid glass animation"
    ),
    Nature(
        key = "nature",
        displayName = "Nature",
        description = "Active tab blooms like a flower / glows like a firefly"
    ),
    Journal(
        key = "journal",
        displayName = "Journal",
        description = "Tabs look like journal page tabs with hand-drawn markers"
    );

    companion object {
        fun fromKey(key: String): NavBarStyle =
            entries.find { it.key == key } ?: Modern

        val default: NavBarStyle = Modern
    }
}

// ── Card Border Style ───────────────────────────────────────────────

enum class CardBorderStyle(
    val key: String,
    val displayName: String
) {
    Rounded(
        key = "rounded",
        displayName = "Rounded"
    ),
    Irregular(
        key = "irregular",
        displayName = "Sketch-like"
    ),
    Minimal(
        key = "minimal",
        displayName = "Minimal"
    );
}

// ── Journal Configuration ───────────────────────────────────────────

/**
 * Complete visual configuration for a journal aesthetic.
 * Each [JournalStyle] maps to a set of these values.
 */
data class JournalConfig(
    // ── Identity ──
    val style: JournalStyle,

    // ── Colors ──
    /** Base background warmth — applies as a tint over the Material background. */
    val backgroundWarmth: Color,
    /** Card surface tint — slightly different from background for depth. */
    val cardSurfaceTint: Color,
    /** Accent warmth boost — shifts accent colors toward the era's palette. */
    val accentWarmth: Color,
    /** Whether cards should use tinted gradient backgrounds vs flat. */
    val useGradientCards: Boolean,

    // ── Shapes ──
    /** Corner radius for standard cards. */
    val cardCornerRadius: Dp,
    /** Corner shape for small chips/badges. */
    val chipCornerRadius: Dp,
    /** How card borders are styled. */
    val borderStyle: CardBorderStyle,
    /** Border stroke width (0.dp = no border). */
    val borderWidth: Dp,

    // ── Textures ──
    /** Whether to overlay a subtle paper/watercolor texture. */
    val showTexture: Boolean,
    /** Name of the texture to show (maps to Canvas draw routine). */
    val textureName: String,
    /** Opacity of the texture overlay (0.0 – 1.0). */
    val textureOpacity: Float,

    // ── Shadows ──
    /** Shadow warmth multiplier: >1 = warmer, <1 = cooler shadow tint. */
    val shadowWarmth: Float,

    // ── Typography hints ──
    /** Whether headings should use serif/more decorative styling. */
    val decorativeHeadings: Boolean,
    /** Whether body text should feel slightly irregular. */
    val irregularBody: Boolean,

    // ── Navigation ──
    /** The active tab indicator style. */
    val navBarStyle: NavBarStyle,

    // ── Ornaments ──
    /** Whether to show decorative flourishes/ornaments near section headers. */
    val showOrnaments: Boolean,
    /** Whether dividers should be decorative rather than plain lines. */
    val decorativeDividers: Boolean,

    // ── Delights ──
    /** Whether to show whimsical micro-animations (butterflies, etc.). */
    val microDelightsEnabled: Boolean,
)

// ════════════════════════════════════════════════════════════════════════
//  Preset Configurations
// ════════════════════════════════════════════════════════════════════════

object JournalPresets {

    /** Victorian Naturalist — Aged parchment, copperplate elegance */
    val Victorian = JournalConfig(
        style = JournalStyle.Victorian,
        backgroundWarmth = Color(0xFFF5E8D8),
        cardSurfaceTint = Color(0xFFFCF5E8),
        accentWarmth = Color(0xFF8B4513).copy(alpha = 0.12f),
        useGradientCards = true,
        cardCornerRadius = 12.dp,
        chipCornerRadius = 8.dp,
        borderStyle = CardBorderStyle.Rounded,
        borderWidth = 0.5.dp,
        showTexture = true,
        textureName = "parchment",
        textureOpacity = 0.12f,
        shadowWarmth = 1.3f,
        decorativeHeadings = true,
        irregularBody = false,
        navBarStyle = NavBarStyle.Modern,
        showOrnaments = true,
        decorativeDividers = true,
        microDelightsEnabled = true
    )

    /** Explorer's Sketchbook — Cream paper, pencil-sketch charm */
    val Sketchbook = JournalConfig(
        style = JournalStyle.Sketchbook,
        backgroundWarmth = Color(0xFFFBF7F0),
        cardSurfaceTint = Color(0xFFFFFCF5),
        accentWarmth = Color(0xFF5D4037).copy(alpha = 0.08f),
        useGradientCards = false,
        cardCornerRadius = 16.dp,
        chipCornerRadius = 12.dp,
        borderStyle = CardBorderStyle.Irregular,
        borderWidth = 1.dp,
        showTexture = true,
        textureName = "paper",
        textureOpacity = 0.15f,
        shadowWarmth = 1.1f,
        decorativeHeadings = false,
        irregularBody = true,
        navBarStyle = NavBarStyle.Journal,
        showOrnaments = false,
        decorativeDividers = false,
        microDelightsEnabled = true
    )

    /** Modern Bullet Journal — Clean dot-grid, organized */
    val BulletJournal = JournalConfig(
        style = JournalStyle.BulletJournal,
        backgroundWarmth = Color(0xFFF8F8FA),
        cardSurfaceTint = Color(0xFFFFFFFF),
        accentWarmth = Color(0xFF37474F).copy(alpha = 0.06f),
        useGradientCards = false,
        cardCornerRadius = 8.dp,
        chipCornerRadius = 6.dp,
        borderStyle = CardBorderStyle.Minimal,
        borderWidth = 0.dp,
        showTexture = true,
        textureName = "dotgrid",
        textureOpacity = 0.08f,
        shadowWarmth = 0.9f,
        decorativeHeadings = false,
        irregularBody = false,
        navBarStyle = NavBarStyle.Modern,
        showOrnaments = false,
        decorativeDividers = false,
        microDelightsEnabled = false
    )

    /** Ghibli Storybook — Watercolor washes, soft dreamy edges */
    val Ghibli = JournalConfig(
        style = JournalStyle.Ghibli,
        backgroundWarmth = Color(0xFFF0ECE4),
        cardSurfaceTint = Color(0xFFFFFBF5),
        accentWarmth = Color(0xFFE8B4B4).copy(alpha = 0.10f),
        useGradientCards = true,
        cardCornerRadius = 24.dp,
        chipCornerRadius = 16.dp,
        borderStyle = CardBorderStyle.Rounded,
        borderWidth = 0.dp,
        showTexture = true,
        textureName = "watercolor",
        textureOpacity = 0.10f,
        shadowWarmth = 1.2f,
        decorativeHeadings = true,
        irregularBody = false,
        navBarStyle = NavBarStyle.Nature,
        showOrnaments = true,
        decorativeDividers = true,
        microDelightsEnabled = true
    )

    /** Get preset by JournalStyle. */
    fun forStyle(style: JournalStyle): JournalConfig = when (style) {
        JournalStyle.Victorian -> Victorian
        JournalStyle.Sketchbook -> Sketchbook
        JournalStyle.BulletJournal -> BulletJournal
        JournalStyle.Ghibli -> Ghibli
    }
}

// ════════════════════════════════════════════════════════════════════════
//  CompositionLocal
// ════════════════════════════════════════════════════════════════════════

/**
 * CompositionLocal providing the active [JournalConfig].
 * Set by [FieldMindTheme] based on the user's settings.
 * Defaults to Sketchbook.
 */
val LocalJournalStyle = staticCompositionLocalOf { JournalPresets.Sketchbook }

/**
 * CompositionLocal providing the active [MicroDelightIntensity].
 */
val LocalMicroDelightIntensity = staticCompositionLocalOf { MicroDelightIntensity.Normal }

/**
 * CompositionLocal providing the active [BackgroundAnimationLevel].
 */
val LocalBackgroundAnimation = staticCompositionLocalOf { BackgroundAnimationLevel.Gentle }

/**
 * CompositionLocal providing the active [NavBarStyle].
 */
val LocalNavBarStyle = staticCompositionLocalOf { NavBarStyle.Modern }
