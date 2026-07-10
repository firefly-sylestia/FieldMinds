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
        // ── v0.45.0 — Personalized Onboarding & Daily Field Journal ──
        FieldMindChangelogEntry(
            version = "0.45.0",
            date = "2026-07-10",
            title = "🎯 Personalized Onboarding & Daily Field Journal",
            importance = "Major",
            tags = listOf("🎯", "📓", "✨", "🌅"),
            sections = listOf(
                "🎯 Fresh 5-step onboarding wizard" to listOf(
                    "✓ Replaced 11-page onboarding with a focused 5-step wizard",
                    "✓ Step 1: Name, role picker (7 roles), and frequency (Daily/Weekends/Spontaneously)",
                    "✓ Step 2: Interest grid with zoology subfields, botany subfields, ecology/astronomy/geology toggles",
                    "✓ Step 3: Permissions with per-item Grant buttons and status indicators",
                    "✓ Step 4: Theme, dynamic colors, layout style (Simple/Guided journal/Data-focused), units, daily goal",
                    "✓ Step 5: Review summary with edit buttons for each section",
                    "✓ Settings auto-configure screen visibility based on selected interests",
                    "✓ Extended tour removed — all those settings are discoverable in Settings"
                ),
                "📓 Daily Field Journal overlay" to listOf(
                    "✓ Beautiful half-sheet slides up with spring animation on first app open each day",
                    "✓ Time-adaptive greeting (Good morning/afternoon/evening) with gradient icon",
                    "✓ Quick capture text field with 5 category chips (Bird, Plant, Insect, Weather, Animal)",
                    "✓ Streak flame display with day count badge",
                    "✓ Random field research tip at the bottom",
                    "✓ Shows once per day — dismissed until next calendar day",
                    "✓ Background tap or 'Start exploring' button to dismiss"
                ),
                "🌅 Time-of-day adaptation" to listOf(
                    "✓ Morning: Sunrise icon, warm orange gradient, 'A fresh start to your field day'",
                    "✓ Afternoon: Weather icon, cool blue gradient, 'Perfect time to log your observations'",
                    "✓ Evening: Moon icon, purple gradient, 'Time to reflect on today's findings'",
                    "✓ Greeting icon dynamically matches the time period"
                ),
                "✨ New settings added" to listOf(
                    "✓ `onboardingFrequency`: How often the user goes out — drives reminder schedule",
                    "✓ `onboardingLayoutStyle`: Simple / Guided journal / Data-focused — configures Home",
                    "✓ `journalEnabled`: Toggle to enable/disable the daily journal overlay",
                    "✓ `journalLastShownDate`: Tracks which day the journal was last shown"
                )
            )
        ),
        // ── v0.44.1 — Compass/Level Accuracy, Checklist Tick & Edit Task Fixes ──
        FieldMindChangelogEntry(
            version = "0.44.1",
            date = "2026-07-08",
            title = "🧭 Compass/Level Accuracy & Bug Fixes",
            importance = "Patch",
            tags = listOf("🧭", "📋", "🐛", "🔧"),
            sections = listOf(
                "🧭 Compass & Level Tool fixes" to listOf(
                    "✓ Level tool vertical orientation (portrait/landscape) formulas corrected for accurate pitch and roll",
                    "✓ Compass now uses direct rotation matrix orientation without remapping — prevents wrong headings in non-flat orientations",
                    "✓ Pitch and roll display now shows correct values in all device orientations",
                    "✓ Fixed missing newline causing compilation error in FLAT orientation calculation"
                ),
                "📋 Checklist tick now works from detail view" to listOf(
                    "✓ Tapping a checklist item in TaskDetailScreen now correctly toggles the done state",
                    "✓ Reads the latest task state directly from ViewModel to avoid stale closure issues",
                    "✓ No longer relies on potentially-stale JSON cached in the composition closure",
                    "✓ Removed pressScale modifier from checklist Surface to prevent pointer event conflicts"
                ),
                "🔧 Edit Task dialog no longer makes task vanish" to listOf(
                    "✓ EditTaskDialog.save() now reads the latest entity from ViewModel state before copying",
                    "✓ Prevents overwriting checklist or progress data with stale entity closure when saving from TaskDetailScreen",
                    "✓ Changes to checklist items no longer get silently reverted when editing task metadata"
                )
            )
        ),

        // ── v0.44.0 — Stability Tester, Navigation, Screenshots & Weather UI ──
        FieldMindChangelogEntry(
            version = "0.44.0",
            date = "2026-07-06",
            title = "🧭 Stability Tester, Navigation, Screenshots & Weather UI",
            importance = "Major",
            tags = listOf("🧭", "🧪", "📸", "🌧️", "🌙"),
            sections = listOf(
                "🧭 Navigation & device Back" to listOf(
                    "✓ Bottom-tab Back now returns Projects/Insights/Library to Today before exiting",
                    "✓ Subpage Back uses a safe home fallback if the back stack cannot pop",
                    "✓ Developer diagnostics now test tab Back behavior and placeholder route metadata"
                ),
                "🧪 Persistent developer reports" to listOf(
                    "✓ Test reports are saved to durable app settings so they remain after leaving Developer Settings",
                    "✓ Reports now include crash coverage limits and a manual UI checklist",
                    "✓ Added synthetic crash persistence and screenshot policy checks"
                ),
                "📸 Screenshot control" to listOf(
                    "✓ Screenshot blocking is now controlled by the explicit screenshot toggle instead of preview mode",
                    "✓ Secure-window ownership is reason based so sensitive screens and global settings do not fight each other"
                ),
                "🌧️ Weather polish" to listOf(
                    "✓ Live weather card gets a stable viewport, real padding, and stronger dark-mode elevation",
                    "✓ Quick actions now use a stable layout wrapper to prevent overlap",
                    "✓ Rain scenes pre-seed particles and draw fallback streaks for immediate visibility"
                )
            )
        ),

        // ── v0.43.1 — CI Compilation Fix ──
        FieldMindChangelogEntry(
            version = "0.43.1",
            date = "2026-07-06",
            title = "🔧 CI Compilation Fix — Card tonalElevation",
            importance = "Patch",
            tags = listOf("🔧", "🐛", "⚡"),
            sections = listOf(
                "🔧 Card → InfoCard migration" to listOf(
                    "✓ Replaced 25 raw `Card(tonalElevation=…, shadowElevation=…)` calls with `InfoCard` across 3 screen files",
                    "✓ `Card` in current Material3 version doesn't support `tonalElevation`/`shadowElevation` params — those are `Surface`-only",
                    "✓ `InfoCard` wraps `Surface` and properly accepts both elevation parameters",
                    "✓ Fixes all 25 CI compilation errors in FieldMindLibraryScreen (18), SpeciesBrowserScreen (3), and WeatherDatabaseScreen (4)"
                )
            )
        ),

        // ── v0.43.0 — Stability, Security & Weather Repair ──
        FieldMindChangelogEntry(
            version = "0.43.0",
            date = "2026-07-05",
            title = "🛡️ Stability, Security & Weather Repair",
            importance = "Major",
            tags = listOf("🛡️", "🔒", "🌦️", "🧪"),
            sections = listOf(
                "💥 Crash & collaboration hardening" to listOf(
                    "✓ Crash reporter now captures richer reports and launches a minimal fallback crash screen",
                    "✓ Collaboration sharing now falls back to clipboard instead of crashing when no share app is available",
                    "✓ Collaboration export now opens the real Export Studio instead of showing a fake started message"
                ),
                "🔒 Lock security repair" to listOf(
                    "✓ In-app unlock PIN now uses a built-in numpad without opening the device keyboard",
                    "✓ 4/5/6 digit PINs now use exact-length verification and consistent failed-attempt rules",
                    "✓ Failed unlock cooldowns, biometric-required mode, panic reset, and auto-lock timeout handling are more consistent"
                ),
                "🌦️ Weather diagnostics" to listOf(
                    "✓ Open-Meteo free tier no longer requires an API key",
                    "✓ Weather refresh can request a fresh location when cached location is missing",
                    "✓ Weather dashboard now surfaces actionable diagnostic messages when fetches fail"
                ),
                "🧪 Developer test runner" to listOf(
                    "✓ Full App Test Runner is cancellable, restores settings, and uses shorter smoke-test timeouts",
                    "✓ Added policy checks for lock attempts, cooldowns, Open-Meteo free tier, and crash activity intent construction"
                )
            )
        ),

        // ── v0.42.0 — Snappier Animations Across the Board ──
        FieldMindChangelogEntry(
            version = "0.42.0",
            date = "2026-07-05",
            title = "⚡ Snappier Animations — ~35% Stiffness Increase",
            importance = "Patch",
            tags = listOf("⚡", "🎬", "✨"),
            sections = listOf(
                "⚡ All spring stiffness increased ~35%" to listOf(
                    "✓ expressiveFloat: 170→220 — main entrance/fade animations (29% increase)",
                    "✓ expressiveSpring: 130→180 — general spring animations (38% increase)",
                    "✓ expressiveSoft: 80→110 — card lift on press (37% increase)",
                    "✓ expressiveSnap: 180→240 — press feedback animations (33% increase)",
                    "✓ pressSpring: 160→200 — press scale animations (25% increase)",
                    "✓ swipeBackSpring: 120→160 — swipe-back gesture (33% increase)",
                    "✓ entranceStiffness: 80→110 — AnimationConfig default (37% increase)",
                    "✓ swipeBackStiffness: 100→140 — AnimationConfig swipe (40% increase)"
                ),
                "⏱ Duration tokens reduced ~20%" to listOf(
                    "✓ durationStandard: 350→300ms, durationEmphasized: 500→400ms",
                    "✓ durationExpressive: 800→600ms, countUpMs: 600→500ms",
                    "✓ stagger delays: initial 40→30ms, item 25→20ms",
                    "✓ Swipe threshold slightly lowered (0.20→0.18) for earlier gesture recognition"
                ),
                "🎬 Overall effect" to listOf(
                    "✓ Animations complete noticeably faster but remain smooth",
                    "✓ Privacy lock unlock, card entrances, and press feedback all feel more responsive",
                    "✓ Damping ratios slightly lowered to allow gentle bounce — adds liveliness"
                )
            )
        ),

        // ── v0.41.0 — InfoBadge Composable for 32dp Inline Badges ──
        FieldMindChangelogEntry(
            version = "0.41.0",
            date = "2026-07-05",
            title = "🏷️ InfoBadge — Compact Entity Type Badge",
            importance = "Patch",
            tags = listOf("🏷️", "🎨", "♻️"),
            sections = listOf(
                "🏷️ New InfoBadge composable" to listOf(
                    "✓ Fixed 32dp circular box with 16dp icon for entity type badges",
                    "✓ Enforces consistent clip radius (16dp) across all 5 detail screens",
                    "✓ Optional alpha parameter (default 0.14f) for dark mode variants",
                    "✓ Standardized ObserveScreen 32dp badge clip from 18dp→16dp to match other screens",
                    "✓ Standardized 28dp tag chip clips to 14dp (fully circular) across DeveloperDebug, Questions, Tasks"
                ),
                "♻️ Prevents future badge size drift" to listOf(
                    "✓ New inline badges should use InfoBadge instead of hand-coded Box/Icon sizes",
                    "✓ Consistent 50% box-to-icon ratio for compact info display"
                )
            )
        ),

        // ── v0.40.9 — StandardIconBox Reusable Composable ──
        FieldMindChangelogEntry(
            version = "0.40.9",
            date = "2026-07-05",
            title = "📦 StandardIconBox — Reusable Icon Container",
            importance = "Patch",
            tags = listOf("📦", "🎨", "♻️"),
            sections = listOf(
                "📦 New StandardIconBox composable" to listOf(
                    "✓ IconBoxSize enum with 3 tiers: Mini (36dp/20dp), Medium (40dp/22dp), Large (44dp/24dp)",
                    "✓ Enforces correct box size, clip radius, and icon size automatically",
                    "✓ Optional alpha parameter (default 0.14f) for dark mode variants",
                    "✓ Drop-in replacement for manual Box+Icon patterns across all screens"
                ),
                "♻️ Prevents future sizing drift" to listOf(
                    "✓ New composables should use StandardIconBox instead of hand-coded Box/Icon sizes",
                    "✓ Existing screens can gradually migrate from inline Box patterns to StandardIconBox"
                )
            )
        ),

        // ── v0.40.8 — Comprehensive Card Sizing Standardization ──
        FieldMindChangelogEntry(
            version = "0.40.8",
            date = "2026-07-05",
            title = "📐 Comprehensive Card Sizing Standardization",
            importance = "Patch",
            tags = listOf("📐", "🎨"),
            sections = listOf(
                "📐 Standardized icon box sizes across all files" to listOf(
                    "✓ 48dp→44dp: DetailScreen (9x), HomeScreen (2x), Dialogs, SettingsScreen, ProjectsScreen, OnboardingScreen, Components (2x) — 17 total",
                    "✓ 38dp→40dp: LibraryScreen (2x), DetailScreen (1x) — icons 20→22dp",
                    "✓ 34dp→36dp: LibraryScreen, Dialogs — icons 18→20dp",
                    "✓ 42dp→44dp: LibraryScreen, Components — icons 22→24dp",
                    "✓ 46dp→44dp: Dialogs — icon stays 24dp",
                    "✓ All modified boxes now use one of three standard sizes: 36dp (mini), 40dp (medium), 44dp (large)",
                    "✓ Every icon maintains ~55% box-to-icon ratio for visual consistency"
                ),
                "🎨 Color swatches left at 48dp" to listOf(
                    "✓ Color picker swatches in ProjectDetailScreen and NewEntityScreens remain 48dp for better tap target ergonomics"
                )
            )
        ),

        // ── v0.40.7 — IntegrityStat Missing Brace Fix ──
        FieldMindChangelogEntry(
            version = "0.40.7",
            date = "2026-07-05",
            title = "🔧 IntegrityStat Missing Brace Fix",
            importance = "Patch",
            tags = listOf("🔧", "🐛"),
            sections = listOf(
                "🔧 IntegrityStat missing closing brace" to listOf(
                    "✓ IntegrityStat composable was missing its closing } — Column `}` at line 1793 only closed the inner layout but never closed the function body",
                    "✓ This trapped ScreenVisibilitySettingsPage, DeveloperSettingsPage, SpeciesPackSettingsPage, and SpeciesIdentificationSettingsPage as local functions inside IntegrityStat",
                    "✓ All 8 Navigation.kt 'Unresolved reference' errors now resolved — functions are properly top-level and visible to Navigation.kt via wildcard import"
                ),
                "🧹 Code hygiene" to listOf(
                    "✓ Cleaned up remaining corrupted UTF-8 comment separator artifacts near IntegrityStat"
                )
            )
        ),

        // ── v0.40.6 — HomeScreen Brace Cascade Fix ──
        FieldMindChangelogEntry(
            version = "0.40.6",
            date = "2026-07-05",
            title = "🔧 HomeScreen Brace Cascade & CI Fixes",
            importance = "Patch",
            tags = listOf("🔧", "🐛"),
            sections = listOf(
                "🐛 HomeScreen: brace cascade fix" to listOf(
                    "✓ DataToolMiniCard was missing its closing braces after sed edits — caused all subsequent functions to be trapped as local functions inside it",
                    "✓ Added 3 missing closing braces to properly close Box, Card, and function",
                    "✓ QuickCaptureSheet, QuickCaptureOption, VoiceNoteCaptureDialog restored to top-level",
                    "✓ Removed duplicate @Composable annotation causing 'not repeatable' error",
                    "✓ Cleaned up corrupted UTF-8 comment separator artifacts"
                ),
                "🔧 Navigation unresolved references" to listOf(
                    "✓ Fixed cascade errors in FieldMindNavigation.kt (caused by HomeScreen parse failure)"
                )
            )
        ),

        // ── v0.40.5 — Card Sizing Standardization & CI Fixes ──
        FieldMindChangelogEntry(
            version = "0.40.5",
            date = "2026-07-05",
            title = "📐 Card Sizing Standardization & CI Fixes",
            importance = "Patch",
            tags = listOf("📐", "🔧", "🎨"),
            sections = listOf(
                "📐 Consistent icon box sizing" to listOf(
                    "✓ CreateOptionRow (ProjectDetail): icon box 40→44dp, icon 22→24dp — 55% ratio",
                    "✓ SettingsScreen: 3x inline icon boxes 40→44dp for consistent tap targets",
                    "✓ SettingsScreen: 6x metadata info badges 32→36dp boxes for better icon breathing room",
                    "✓ ProjectCard: box 48→44dp, icon 26→24dp — aligned with all other list-style cards"
                ),
                "🔧 CI compilation fixes" to listOf(
                    "✓ Added missing @Composable annotation to DataToolMiniCard",
                    "✓ Removed orphaned @Composable in SettingsScreen (StatChip extraction leftover)",
                    "✓ Removed extra closing braces in HomeScreen causing cascade syntax errors",
                    "✓ Cleaned up corrupted UTF-8 comment separators"
                )
            )
        ),

        // ── v0.40.4 — CI Compilation Error Fixes ──
        FieldMindChangelogEntry(
            version = "0.40.4",
            date = "2026-07-05",
            title = "🔧 CI Compilation Error Fixes",
            importance = "Patch",
            tags = listOf("🔧", "🐛"),
            sections = listOf(
                "🔧 ClickableCard missing Color import" to listOf(
                    "✓ Added missing import androidx.compose.ui.graphics.Color — fixes Unresolved reference error"
                ),
                "🐛 Extra brace cascade in FieldMindSettingsScreen" to listOf(
                    "✓ Removed extra closing brace at DataIntegritySettingsPage end that was causing 'Expecting top level declaration' error",
                    "✓ Moved StatChip from local function (invalid private modifier) to top-level — fixes accessibility from SpeciesPackSettingsPage"
                ),
                "🔧 SpeciesBrowserScreen premature LazyColumn close" to listOf(
                    "✓ Removed extra closing brace that was closing LazyColumn scope early, causing all item() calls to be unresolved"
                )
            )
        ),

        // ── v0.40.3 — Card Layout Fixes & heroColor Consistency ──
        FieldMindChangelogEntry(
            version = "0.40.3",
            date = "2026-07-05",
            title = "🎨 Card Layout Fixes & Header Color Consistency",
            importance = "Patch",
            tags = listOf("🎨", "💡", "🔧"),
            sections = listOf(
                "🎨 DataToolMiniCard redesigned" to listOf(
                    "✓ Changed from Row layout (icon left + text right) to centered Column layout",
                    "✓ Icon box: 32dp → 36dp, icon: 18dp → 20dp for better visibility",
                    "✓ Text now center-aligned — icon above text, centered in card",
                    "✓ The 4 mini tool cards on Home screen now show centered icon+text pattern"
                ),
                "💡 HeroActionChip sizing improved" to listOf(
                    "✓ Icon size: 20dp → 22dp for more prominence",
                    "✓ Text style: labelSmall → labelMedium for better readability",
                    "✓ Affects the 3 main action buttons (Capture, Note, Projects) on Home header"
                ),
            "🔧 Explicit heroColor on all StandardScreenHeaders" to listOf(
                "✓ LibraryScreen: heroColor = FieldMindTheme.colors.source",
                "✓ DataToolsHub + 8 tool screens: heroColor = FieldMindTheme.colors.data",
                "✓ SettingsScreen: heroColor = FieldMindTheme.colors.info",
                "✓ Previously relied on auto-derived default — now explicitly set per-screen"
            ),
            "🔧 ToolCardItem redesigned" to listOf(
                "✓ Changed from SpaceBetween (icon top, text bottom) to centered Column layout",
                "✓ Icon box: 40dp → 44dp, icon: 22dp → 24dp for better prominence",
                "✓ Height: 140dp → 130dp, text centered with textAlign = Center",
                "✓ Affects all 8 tool cards in DataTools hub"
            ),
            "🔧 QuickActionChip redesigned" to listOf(
                "✓ Changed from horizontal Row (icon+text side-by-side) to centered Column pattern",
                "✓ Icon box: 36dp → 40dp, icon: 22dp → 24dp",
                "✓ Tighter 14dp horizontal padding for proportional chip sizing",
                "✓ Affects 5 quick action chips on Home screen (Map, Export, Search, Review, Insights)"
            )
            )
        ),

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
            tags = listOf("🎨", "🌈", "✨"),                sections = listOf(
                    "🌈 Gradient card backgrounds" to listOf(
                        "✓ Settings cards use a clean flat tint background instead of multi-stop gradients",
                        "✓ Visual tint picker in Settings → Appearance with live preview swatches",
                        "✓ Tints adapt to light/dark and any color scheme"
                    ),
                    "✨ Card polish" to listOf(
                        "✓ CuteGradients infrastructure simplified to 3 flat-tint styles (Default, Sunny Lift, AMOLED Black)",
                        "✓ SettingsGroupCard shadow, CuteCardDefaults dialog presets"
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
