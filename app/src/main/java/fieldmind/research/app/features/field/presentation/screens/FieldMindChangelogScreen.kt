package fieldmind.research.app.features.field.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fieldmind.research.app.features.field.presentation.components.FieldMindIcons
import fieldmind.research.app.features.field.presentation.components.BackButton
import fieldmind.research.app.features.field.presentation.components.StandardScreenHeader
import fieldmind.research.app.features.field.presentation.components.InfoChip
import fieldmind.research.app.features.field.presentation.theme.FieldMindTheme
import fieldmind.research.app.shared.presentation.components.icons.Icon
import fieldmind.research.app.ui.theme.CuteElevations

internal data class FieldMindChangelogEntry(
    val version: String,
    val date: String,
    val title: String,
    val importance: String,
    val tags: List<String>,
    val sections: List<Pair<String, List<String>>>
)    private    val fieldMindChangelog = listOf(
        // ── v0.40.2 — Species Catalog: No Reload & Detail Polish ──
        FieldMindChangelogEntry(
            version = "0.40.2",
            date = "2026-07-05",
            title = "🐞 Species Catalog: No Reload & Detail Polish",
            importance = "Patch",
            tags = listOf("🐞", "⚡", "🎨"),
            sections = listOf(
                "⚡ SpeciesDatabase is now a singleton" to listOf(
                    "✓ SpeciesDatabase made application-scoped with getInstance() — parsed catalog JSON persists across navigation",
                    "✓ Eliminates full reload every time user opens Species Browser or navigates back from species detail",
                    "✓ In-memory speciesCache now survives across screens — instant load on return"
                ),
                "🎨 Species detail screen redesigned" to listOf(
                    "✓ Added 20dp horizontal padding to LazyColumn for consistent content margins",
                    "✓ Hero header now uses proper Surface with elevation depth instead of flat accent background",
                    "✓ All cards upgraded: replaced 0dp + cuteShadow with CuteElevations.nonClickableTier (4dp)",
                    "✓ Habitat and Diet cards now use proper elevation with surface container",
                    "✓ Similar species cards now use ClickableCard with press animation instead of raw Card",
                    "✓ Consistent 12dp spacing between sections via Arrangement.spacedBy"
                )
            )
        ),

        // ── v0.40.1 — CI Compilation Fixes ──
        FieldMindChangelogEntry(
            version = "0.40.1",
            date = "2026-07-05",
            title = "🔧 CI Compilation Fixes",
            importance = "Patch",
            tags = listOf("🔧", "🐛", "⚡"),
            sections = listOf(
                "🔧 WeatherSettingsPage brace fix" to listOf(
                    "✓ Fixed missing closing brace in WeatherSettingsPage that was causing cascade syntax errors throughout the file",
                    "✓ Resolved 'item' context error and end-of-file syntax error"
                ),
                "🐛 IntegrityStat moved to top-level" to listOf(
                    "✓ Moved IntegrityStat composable from inside DataIntegritySettingsPage to a top-level function",
                    "✓ Fixed 'private not applicable to local function' and 'Unresolved reference' errors"
                ),
                "⚡ Resolved cascade compilation errors" to listOf(
                    "✓ Fixed all navigation unresolved reference errors in FieldMindNavigation.kt",
                    "✓ All 22+ CI compilation errors across SettingsScreen and Navigation now resolved"
                )
            )
        ),

        // ── v0.40.0 — Map Feature Completeness ──
        FieldMindChangelogEntry(
            version = "0.40.0",
            date = "2026-07-05",
            title = "🗺️ Map Completeness: Persistence, Geo-fence Viz & Home Card",
            importance = "Major",
            tags = listOf("🗺️", "💾", "🎯"),
            sections = listOf(
                "💾 Drawings & tracks now persist" to listOf(
                    "✓ Map overlays (points, lines, polygons) saved to SharedPreferences — survive app restart",
                    "✓ GPS track recordings also persisted and restored on relaunch",
                    "✓ Drawings tab and Tracks tab now show real saved data across sessions"
                ),
                "🎯 Geo-fence circles visible on map" to listOf(
                    "✓ Active geo-fence regions now rendered as translucent green circles on the map",
                    "✓ Circle radius matches the configured fence size",
                    "✓ Visible in both tab view and full-screen map mode"
                ),
                "🏠 Map card on home screen" to listOf(
                    "✓ New 'Field Map' card between Data Tools and Media sections",
                    "✓ Quick access to the map screen from the home dashboard",
                    "✓ Descriptive text and 'Open' button"
                )
            )
        ),

        // ── v0.39.0 — Session Swipe Guard & State Cleanup ──
        FieldMindChangelogEntry(
            version = "0.39.0",
            date = "2026-07-05",
            title = "🛡️ Session Swipe Guard & State Cleanup",
            importance = "Patch",
            tags = listOf("🛡️", "🐛", "🔄"),
            sections = listOf(
                "🛡️ Block swipe navigation during active session" to listOf(
                    "✓ Horizontal swipe gesture on tabs now disabled when an observation session is active",
                    "✓ Prevents accidental session dismissal by swiping — users must use the nav bar which shows a confirmation dialog",
                    "✓ Previously the swipe bypassed the navigation guard entirely"
                ),
                "🔄 Reset stale session state on external navigation" to listOf(
                    "✓ When capture session is dismissed via the navigation guard dialog, local session state is now properly reset",
                    "✓ Coming back to Capture after discarding no longer shows a stale active session",
                    "✓ LaunchedEffect watches captureSessionActive and cleans up session, form, and activeSessionId"
                )
            )
        ),

        // ── v0.38.0 — Placeholder & No-Op Fixes ──
        FieldMindChangelogEntry(
            version = "0.38.0",
            date = "2026-07-05",
            title = "🔧 Placeholder Fixes & Export Improvements",
            importance = "Patch",
            tags = listOf("🔧", "🎨", "📋"),
            sections = listOf(
                "🔧 Map drawing overlay editor" to listOf(
                    "✓ Tapping a drawing in Drawings tab now opens an edit dialog — rename label + pick color",
                    "✓ 10-color palette with check mark selection indicator",
                    "✓ Replaces the old /* future: edit label/color */ no-op"
                ),
                "🎨 Onboarding tour pages filled in" to listOf(
                    "✓ Pages 7 (Backup), 8 (Data Tools), 9 (Species ID) are no longer bare 'Coming Soon' stubs",
                    "✓ Now show real UI: backup interval picker, data tool cards, species ID intro",
                    "✓ Page 10 final page also replaced with proper OnboardingFinalPage"
                ),
                "📋 Export buttons now work" to listOf(
                    "✓ PDF/CSV/JSON buttons in observation detail no longer show 'coming soon'",
                    "✓ Markdown: copies observation as formatted Markdown",
                    "✓ CSV: generates proper CSV with header row",
                    "✓ JSON: generates pretty-printed JSON with all observation fields"
                )
            )
        ),

        // ── v0.37.0 — Map Improvements, Region Picker, GPS Retry ──
        FieldMindChangelogEntry(
            version = "0.37.0",
            date = "2026-07-05",
            title = "🗺️ Map Improvements, Region Picker & GPS Retry",
            importance = "Major",
            tags = listOf("🗺️", "📍", "⚡"),
            sections = listOf(
                "🗺️ Map zoom & place display" to listOf(
                    "✓ Default map zoom capped at 13 — shows town/village context instead of tiny block area",
                    "✓ Place names now combine locality + block (e.g. \"Hosur, Krishnagiri\") for richer location context",
                    "✓ 'Use my location' button auto-fills a 5km bounding box for tile downloads from GPS"
                ),
                "📍 Pick from map for tile downloads" to listOf(
                    "✓ Full-screen map region picker — tap NW then SE corner to draw bounding box",
                    "✓ Semi-transparent rectangle overlay with corner markers",
                    "✓ Coordinates auto-fill back to the download dialog on confirmation",
                    "✓ Existing observation points shown as teal markers for context",
                    "✓ 'Zoom to my location' button centers map on current GPS position"
                ),
                "⚡ Aggressive GPS retry" to listOf(
                    "✓ GPS acquisition now retries automatically after 5s if first attempt fails",
                    "✓ Falls back to cached location as final fallback",
                    "✓ Loading spinner and attempt progress (\"Attempt 2/3…\") shown during acquisition",
                    "✓ Works in both auto-fetch and manual GPS fetch modes"
                )
            )
        ),

        // ── v0.36.0 — New Tools, Clickable Sessions, Snappier Animations ──
        FieldMindChangelogEntry(
            version = "0.36.0",
            date = "2026-07-03",
            title = "🎯 New Tools, Clickable Sessions & Snappier Animations",
            importance = "Patch",
            tags = listOf("🎯", "⚡", "🔧"),
            sections = listOf(
                "🎯 New tools in Home" to listOf(
                    "✓ Voice Notes, Media Gallery, Bibliography, and Collaborate now surfaced on Home screen",
                    "✓ Quick-access cards in a new \"Media & sharing\" section",
                    "✓ Previously hidden routes—now discoverable from the home dashboard"
                ),
                "🔧 Past sessions clickable" to listOf(
                    "✓ Past research sessions in Capture screen now open detail on tap",
                    "✓ Expressive press feedback + navigation to session details"
                ),
                "⚡ Snappier animations" to listOf(
                    "✓ Cancel spring stiffness increased 60→120 (2x snappier)",
                    "✓ Swipe-back spring stiffness increased 80→100 for more responsive feel",
                    "✓ Still buttery-smooth—just noticeably faster"
                )
            )
        ),

        // ── v0.35.0 — Full App Test Runner in Developer Options ──
        FieldMindChangelogEntry(
            version = "0.35.0",
            date = "2026-07-03",
            title = "🧪 Full App Test Runner",
            importance = "Patch",
            tags = listOf("🧪", "🔧", "📋"),
            sections = listOf(
                "🧪 DevFullAppTestRunner composable" to listOf(
                    "✓ Comprehensive in-app test runner in Developer Settings",
                    "✓ 8 test categories: Navigation, ViewModel, Settings, Database, Security, AI, Capture, Error Handling",
                    "✓ 45+ individual assertions across all app layers",
                    "✓ Real-time progress with pass/fail summary",
                    "✓ Collapsible detailed results log",
                    "✓ Copy to clipboard or share via Android share sheet"
                ),
                "🔧 Tests included" to listOf(
                    "✓ Navigation: tab screens, settings routes, FieldMindScreen objects",
                    "✓ ViewModel: all 10 StateFlows verified non-null (observations, notes, questions, projects, etc.)",
                    "✓ Settings: theme, developer mode, profile, temperature, map, time, date, weather toggles",
                    "✓ Database: entity constructors for all 7 entity types",
                    "✓ Security: PIN hashing, verification, export password, privacy lock, screen capture",
                    "✓ AI: provider switching, confirm-before-save, local model toggle",
                    "✓ Capture: categories, confidence, location, media, audio, reminders toggles",
                    "✓ Error handling: CrashReporter class, crash logs, integrity check, SDK version"
                )
            )
        ),

        // ── v0.34.0 — Dark Mode Depth & Shadow Visibility ──
        FieldMindChangelogEntry(
            version = "0.34.0",
		date = "2026-07-03",
		title = "🎚️ Dark Mode Depth & Shadow Visibility",
		importance = "Patch",
		tags = listOf("🎨", "🌙", "💡"),
		sections = listOf(
			"💡 Brighter dark mode shadows" to listOf(
				"✓ Dark mode white-tinted shadow alphas increased ~45% for clearly visible card depth",
				"✓ Ambient alpha: ~0.11→0.16, Spot alpha: ~0.17→0.25 at 6dp elevation",
				"✓ Shadows now provide visible luminous lift against dark/AMOLED backgrounds"
			),
			"📐 Tonal elevation for visible depth in dark mode" to listOf(
				"✓ ClickableCard: tonalElevation = 6dp — surfaceTint overlay makes card edges visible in dark mode",
				"✓ InfoCard: tonalElevation = 4dp — consistent depth for non-interactive cards",
				"✓ tonalElevation renders a Material3 surfaceTint overlay proportional to elevation, creating visible depth even when shadows are not perceivable"
			)
		)
	),
        // ── v0.33.0 — New Detailed Moon Phase Icons ──
        FieldMindChangelogEntry(
            version = "0.33.0",
            date = "2026-07-03",
            title = "🌙 Detailed Moon Phase Icons Everywhere",
            importance = "Patch",
            tags = listOf("🌙", "🎨", "✨"),
            sections = listOf(
                "🌙 MoonPhaseIcon composable" to listOf(
                    "✓ Canvas-based moon phase icon with mare/crater surface features, glow halo, and accurate phase shadow",
                    "✓ 8 moon phase enum: New Moon, Waxing Crescent, First Quarter, Waxing Gibbous, Full Moon, Waning Gibbous, Third Quarter, Waning Crescent",
                    "✓ Automatic current phase detection using astronomical calculation",
                    "✓ Lunar mare positions (Mare Imbrium, Tranquillitatis, Serenitatis, etc.) and crater highlights (Tycho, Copernicus, Aristarchus)",
                    "✓ Optional animated glow pulse for Full Moon"
                ),
                "🔄 Integrated across UI" to listOf(
                    "✓ Weather widget moon phase now shows detailed phase-specific icon instead of generic MaterialSymbol",
                    "✓ Home screen, moon phase label, and developer test panel all use the new composable",
                    "✓ Original SVG crater data preserved in app resources for reference"
                )
            )
        ),
        // ── v0.31.0 — Fixed Card Elevation Hierarchy ──
        FieldMindChangelogEntry(
            version = "0.31.0",
            date = "2026-07-03",
            title = "📐 Fixed Card Elevation Hierarchy",
            importance = "Patch",
            tags = listOf("💡", "🎨", "🔧"),
            sections = listOf(
                "💡 Clickable vs non-clickable elevation fix" to listOf(
                    "✓ ClickableCard default elevation: 4dp → 6dp (CuteElevations.clickableTier)",
                    "✓ InfoCard now uses CuteElevations.nonClickableTier (4dp) for semantic clarity",
                    "✓ Clickable cards now lift higher than info-only cards, matching the defined depth hierarchy",
                    "✓ All existing ClickableCard call sites (~10+) automatically inherit the correct elevation"
                )
            )
        ),
        // ── v0.30.0 — Smoother Card Entrance Animations ──
        FieldMindChangelogEntry(
            version = "0.30.0",
            date = "2026-07-03",
            title = "✨ Smoother Card Entrance Animations",
            importance = "Patch",
            tags = listOf("🎬", "🐌", "✨"),
            sections = listOf(
                "🐌 ExpressiveFloat softened" to listOf(
                    "✓ expressiveFloat stiffness reduced from 220→170 (23% reduction)",
                    "✓ Powers staggeredEntrance() — every card's fade+slide entrance across the entire app",
                    "✓ Still the snappiest spring (above expressiveSpring at 130) — lively but no longer jarring"
                )
            )
        ),
        // ── v0.29.0 — AMOLED Black Gradient Style ──
        FieldMindChangelogEntry(
            version = "0.29.0",
            date = "2026-07-03",
            title = "🌑 AMOLED Black Gradient for OLED Power Saving",
            importance = "Patch",
            tags = listOf("🌙", "🎨", "🔋"),
            sections = listOf(
                "🌙 AMOLED Black gradient" to listOf(
                    "✓ New \"AMOLED Black\" card gradient style — true black (#000000) to barely-lit dark (#080808)",
                    "✓ Saves battery on OLED screens by keeping pixels near-off",
                    "✓ Selectable from Settings → Appearance → Card Style picker",
                    "✓ Only affects card backgrounds in dark mode with AMOLED enabled; light mode falls back to subtle neutral"
                )
            )
        ),
        // ── v0.28.0 — Official FieldMind Logo in App UI ──
        FieldMindChangelogEntry(
            version = "0.28.0",
            date = "2026-07-03",
            title = "🎯 Official FieldMind Logo in App UI",
            importance = "Major",
            tags = listOf("🎨", "✨", "🖼️"),
            sections = listOf(
                "🎯 FieldMind logo" to listOf(
                    "✓ New FieldMindLogo composable loads the official fieldmind_logo.png from resources",
                    "✓ Welcome page: real logo replaces generic leaf icon with gradient background",
                    "✓ Home header: real logo replaces green-tinted Nature icon",
                    "✓ About page: real logo replaces Nature icon in branding card",
                    "✓ Decoy lock screen: real logo replaces placeholder leaf icon"
                )
            )
        ),
        // ── v0.27.0 — Depth Hierarchy, Dark Mode Shadows, AMOLED & Smoother Animations ──
        FieldMindChangelogEntry(
            version = "0.27.0",
            date = "2026-07-03",
            title = "📐 Depth Hierarchy, Dark Mode Shadows & AMOLED",
            importance = "Major",
            tags = listOf("💡", "🎨", "⚡", "🔧"),
            sections = listOf(
                "💡 Shadow depth hierarchy" to listOf(
                    "✓ Clickable vs non-clickable elevation tiers: clickable cards (6dp) lift higher than info-only cards (4dp)",
                    "✓ Home screen header now has proper 6dp shadow depth and 2dp tonal elevation",
                    "✓ Dark mode: shadows use white-tinted ambient/spot colors for a luminous lifted glow",
                    "✓ cuteShadow() now theme-aware — delegates to cuteShadowAdaptive() so all existing callers get dark mode white shadows"
                ),
                "🌙 AMOLED mode" to listOf(
                    "✓ True black backgrounds (surface, background) when AMOLED is enabled in dark mode",
                    "✓ Surface containers shift to deeper blacks (#0A0A0A, #121212, #1E1E1E, #2A2A2A)",
                    "✓ Wired through FieldMindTheme and MainActivity",
                    "✓ New toggle in Settings → Appearance → Theme section"
                ),
                "⚡ Softer animations" to listOf(
                    "✓ All spring stiffness values reduced ~30-40% for buttery-smooth motion",
                    "✓ Default entrance stiffness: 120→80, swipe-back: 120→80, tab entrance: 180→120",
                    "✓ Nav transitions, press feedback, and gesture springs all softened"
                ),
                "🔧 Navigation fix" to listOf(
                    "✓ Removed PredictiveBackHandler from AllTabScreen — was conflicting with SwipeBackHost's BackHandler causing double-fire on back button",
                    "✓ BackHandler now cleanly handles hardware back without gesture conflicts"
                )
            )
        ),
        // ── v0.26.0 — Beautification Phases 4–7 & Fixes ──
        FieldMindChangelogEntry(
            version = "0.26.0",
            date = "2026-07-02",
            title = "🎨 Beautification Phases 4–7 & Polish",
            importance = "Major",
            tags = listOf("🎨", "⚡", "🔧"),
            sections = listOf(
                "🌈 Theme & Colors" to listOf(
                    "✓ Entity accent colors (obs, project, source icons) now auto-adapt to any color scheme via deriveFieldMindColors() — Warm, Cool, Forest, Rose, Monochrome, and all 12 schemes get proper entity tints",
                    "✓ ScreenBackground gradient: subtle vertical gradient using primaryContainer/tertiaryContainer tints for a scheme-reflective background wash on all 5 tab screens",
                    "✓ Gradient card system with 9 gradient styles and visual picker in Settings → Appearance"
                ),
                "⚡ Animations & Interactions" to listOf(
                    "✓ Card staggered entrance animations sped up 3× (stiffness 180→350, delays halved) — no more sluggish fades",
                    "✓ Swipe-to-complete animation for task items with haptic confirmation and spring overshoot",
                    "✓ SettingsGroupCard shadow, gradient EmptyState, and plush dialog presets"
                ),
                "🔧 Fixes" to listOf(
                    "✓ Animation tuning sliders now reactive — changes apply instantly to all screens",
                    "✓ Swipe-back gesture no longer conflicts with scrollable content (LazyColumn in settings, etc.)",
                    "✓ CI compilation fixes for awaitFirstDown import and positionChange() API"
                )
            )
        ),
        // ── v0.25.0 — Gradient Cards & Theme Picker ──
        FieldMindChangelogEntry(
            version = "0.25.0",
            date = "2026-07-02",
            title = "🌸 Gradient Cards & Preset Theme Picker",
            importance = "Major",
            tags = listOf("🎨", "🌈", "✨"),
            sections = listOf(
                "🌈 Gradient card backgrounds" to listOf(
                    "✓ Every settings card now has a theme-aware gradient background with 9 styles (Primary Tonal, Blush Trio, Cool Dream, etc.)",
                    "✓ Visual gradient picker in Settings → Appearance with live preview swatches",
                    "✓ Gradients adapt to light/dark and any color scheme"
                ),
                "✨ Card polish" to listOf(
                    "✓ GradientCard composable, cuteGradientBackground modifier, CuteGradients infrastructure",
                    "✓ SettingsGroupCard shadow, gradient EmptyState, CuteCardDefaults dialog presets"
                )
            )
        ),
        // ── v0.24.2 — Staggered Entrance Animations ──
        FieldMindChangelogEntry(
            version = "0.24.2",
            date = "2026-07-01",
            title = "✨ Staggered Entrance Animations",
            importance = "Patch",
            tags = listOf("🎬", "✨", "🛠️"),
            sections = listOf(
                "🎬 Fade-in + slide-up cards" to listOf(
                    "✓ New staggeredEntrance() modifier: items fade in and slide up with staggered delays",
                    "✓ Applied to SectionHeader, EntityCard, ClickableCard, InfoCard, MetricTile",
                    "✓ Respects system reduce-motion accessibility setting"
                )
            )
        ),
        // ── v0.24.0 — Pastel Theme ──
        FieldMindChangelogEntry(
            version = "0.24.0",
            date = "2026-07-01",
            title = "🌸 Pastel Color Scheme",
            importance = "Major",
            tags = listOf("🎨", "🌸", "✨"),
            sections = listOf(
                "🌸 New Pastel theme" to listOf(
                    "✓ Soft lavender/blush/mint palette with warm pink-white background",
                    "✓ Pastel entity accent colors for all 12 research entity types",
                    "✓ Selectable from Settings → Appearance → Color scheme"
                )
            )
        ),
        // ── v0.23.0 — Soft Shadows & Cute Cards ──
        FieldMindChangelogEntry(
            version = "0.23.0",
            date = "2026-07-01",
            title = "Soft Shadows & Cute Cards Theme",
            importance = "Major",
            tags = listOf("🎨", "✨", "🛠️"),
            sections = listOf(
                "🎨 Plush card elevation" to listOf(
                    "✓ All cards elevated with 4dp soft shadows for a layered, plush feel",
                    "✓ CuteThemeConfig.kt: CuteElevations (5 tiers), CuteShadows, CuteCardDefaults",
                    "✓ cuteShadow() modifier for consistent shadows anywhere"
                )
            )
        ),
        // ── v0.22.0 — Animation Tuning ──
        FieldMindChangelogEntry(
            version = "0.22.0",
            date = "2026-06-30",
            title = "Animation Tuning & Smoother Transitions",
            importance = "Major",
            tags = listOf("🎬", "🛠️", "✨"),
            sections = listOf(
                "🎬 Smoother animations" to listOf(
                    "✓ Reduced entrance bounce with configurable damping/stiffness",
                    "✓ Developer animation tuning: 5 sliders for entrance, swipe-back, tab transition physics",
                    "✓ Real-content preview peek on back gesture for all 40+ routes"
                )
            )
        ),
        // ── v2.3.26.24 — Tab Overhaul ──
        FieldMindChangelogEntry(
            version = "2.3.26.24",
            date = "2026-06-28",
            title = "Tab Overhaul & UI Consistency",
            importance = "Major",
            tags = listOf("Navigation", "UI", "Fixes"),
            sections = listOf(
                "🔄 5-tab rendering & predictive peek" to listOf(
                    "✓ All 5 tabs render simultaneously behind active tab — predictive back peek shows real content",
                    "✓ StandardScreenHeader and Geom font applied throughout",
                    "✓ Liquid nav bar blob indicator with spring animation",
                    "✓ statusBarsPadding on 6 screens, 50+ CI compile fixes"
                )
            )
        ),
        // ── v2.2.26.28 — Security & Auto-Lock ──
        FieldMindChangelogEntry(
            version = "2.2.26.28",
            date = "2026-06-24",
            title = "Security & Auto-Lock Overhaul",
            importance = "Patch",
            tags = listOf("Security", "PIN", "Auto-Lock"),
            sections = listOf(
                "🔐 PIN & security" to listOf(
                    "✓ PIN lockout fix: respects 4-6 digit PIN length",
                    "✓ Decoy PIN opens clean empty version of app",
                    "✓ Auto-lock on background, keep-screen-on, customizable lock timeout"
                )
            )
        ),
        // ── v2.2.26.27 — Color Customizer ──
        FieldMindChangelogEntry(
            version = "2.2.26.27",
            date = "2026-06-24",
            title = "Per-Category Color Customizer",
            importance = "Patch",
            tags = listOf("Colors", "Theme", "Settings"),
            sections = listOf(
                "🎨 Entity color customization" to listOf(
                    "✓ Inline color picker for all 18 entity/state/confidence colors",
                    "✓ 28-color preset grid + custom hex input",
                    "✓ Persisted in SharedPreferences with backup/restore support"
                )
            )
        ),
        // ── v2.2.26.25 — Project Detail Redesign ──
        FieldMindChangelogEntry(
            version = "2.2.26.25",
            date = "2026-06-24",
            title = "Project Detail Redesign",
            importance = "Patch",
            tags = listOf("Project", "Redesign", "Create Sheet"),
            sections = listOf(
                "📋 Redesigned project screen" to listOf(
                    "✓ Modern header, stats row, filter tabs, activity feed",
                    "✓ New Create sheet (COLLECT/ANALYZE/EVIDENCE/PLAN groups)",
                    "✓ Search bar and empty state guidance"
                )
            )
        ),
        // ── v2.2.26.22 — Quick Capture FAB ──
        FieldMindChangelogEntry(
            version = "2.2.26.22",
            date = "2026-06-24",
            title = "Quick Capture FAB & Voice Notes",
            importance = "Patch",
            tags = listOf("FAB", "Voice Notes", "Capture"),
            sections = listOf(
                "📷 Quick capture" to listOf(
                    "✓ Floating + button on Home with 5 capture options",
                    "✓ Voice note recording with MediaRecorder AAC",
                    "✓ Seamless camera, note, and question capture"
                )
            )
        ),
    )

@Composable
fun FieldMindChangelogScreen(onBack: () -> Unit) {
    val changelogScrollState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }

    LazyColumn(
        state = changelogScrollState,
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(20.dp, 20.dp, 20.dp, 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {    item {
            StandardScreenHeader(
                title = "What's New",
                subtitle = "Complete field research redesign with 12 phases of new features",
                icon = FieldMindIcons.Info,
                heroColor = FieldMindTheme.colors.observation,
                trailing = { BackButton(onClick = onBack) }
            )
        }
        
        // Introduction card
        item {
            Card(
                shape = RoundedCornerShape(34.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            FieldMindIcons.Sparkle,
                            null,
                            size = 24.dp,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Phases 1-12: Complete Redesign",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        "From dashboard foundations to a complete research workspace. Observations, projects, analysis, hypotheses, reports, and knowledge management—all redesigned for modern field research.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        
        items(fieldMindChangelog) { entry -> ChangelogEntryCard(entry) }
    }
}

@Composable
private fun ChangelogEntryCard(entry: FieldMindChangelogEntry) {
    val isLatest = entry.version == fieldMindChangelog.first().version
    val accentColor = when {
        isLatest -> MaterialTheme.colorScheme.primary
        entry.importance == "Major" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.tertiary
    }
    
    Card(
        shape = RoundedCornerShape(36.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLatest) MaterialTheme.colorScheme.primaryContainer 
                          else MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLatest) 4.dp else 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Header with version badge
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    Modifier.size(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isLatest) FieldMindIcons.Sparkle else FieldMindIcons.Info,
                        null,
                        tint = accentColor,
                        size = 32.dp
                    )
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        entry.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(FieldMindIcons.Calendar, null, size = 14.dp, 
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            entry.date,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            entry.version,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                // Importance badge
                Box(
                    Modifier.clip(RoundedCornerShape(20.dp))
                        .background(
                            when (entry.importance) {
                                "Major" -> MaterialTheme.colorScheme.errorContainer
                                else -> MaterialTheme.colorScheme.tertiaryContainer
                            }
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        entry.importance,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = when (entry.importance) {
                            "Major" -> MaterialTheme.colorScheme.onErrorContainer
                            else -> MaterialTheme.colorScheme.onTertiaryContainer
                        }
                    )
                }
            }

            // Tags with icons
            if (entry.tags.isNotEmpty()) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    entry.tags.forEach { tag ->
                        Box(
                            Modifier.clip(RoundedCornerShape(18.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                tag,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            // Divider
            Spacer(
                Modifier.fillMaxWidth().height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            )

            // Feature sections with icons
            entry.sections.forEach { (heading, bullets) ->
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Section heading with icon
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            Modifier.size(6.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(accentColor)
                        )
                        Text(
                            heading,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = accentColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // Feature bullets with improved spacing
                    bullets.forEach { bullet ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                FieldMindIcons.Check,
                                null,
                                size = 18.dp,
                                tint = accentColor.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Text(
                                bullet,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            
            // Latest badge for current version
            if (isLatest) {
                Spacer(Modifier.height(4.dp))
                Box(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            FieldMindIcons.Sparkle,
                            null,
                            size = 16.dp,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Latest version with comprehensive research features",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
