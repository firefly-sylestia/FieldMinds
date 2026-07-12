package fieldmind.research.app.shared.presentation.theme

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * ════════════════════════════════════════════════════════════════════════
 *  ✨ AppStyle — Micro-delight & navigation bar preferences
 *
 *  As of v0.51.0 the 4 journal aesthetics (Victorian, Sketchbook,
 *  BulletJournal, Ghibli) have been retired in favour of a single
 *  unified "cute rounded" design language driven by [CuteCardDefaults]
 *  in CuteThemeConfig.kt.
 *
 *  Two user-facing preferences remain:
 *  1. [MicroDelightIntensity] — controls animation/celebration richness
 *  2. [NavBarStyle]           — controls bottom nav bar appearance
 * ════════════════════════════════════════════════════════════════════════
 */

// ── Common Keyed Enum Interface ──────────────────────────────────────

/**
 * Shared contract for the select enums (MicroDelightIntensity, NavBarStyle).
 * Enables a single generic pill-radio picker in the Appearance settings page.
 */
interface KeyedEnum {
    /** Stable lowercase-snake_case key persisted in user settings. */
    val key: String
    /** Human-readable display name shown in UI. */
    val displayName: String
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

// ════════════════════════════════════════════════════════════════════════
//  CompositionLocals
// ════════════════════════════════════════════════════════════════════════

/**
 * CompositionLocal providing the active [MicroDelightIntensity].
 */
val LocalMicroDelightIntensity = staticCompositionLocalOf { MicroDelightIntensity.Normal }

/**
 * CompositionLocal providing the active [NavBarStyle].
 */
val LocalNavBarStyle = staticCompositionLocalOf { NavBarStyle.Modern }
