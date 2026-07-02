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

internal data class FieldMindChangelogEntry(
    val version: String,
    val date: String,
    val title: String,
    val importance: String,
    val tags: List<String>,
    val sections: List<Pair<String, List<String>>>
)    private val fieldMindChangelog = listOf(
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
    ) {
        item {
            StandardScreenHeader(
                title = "What's New",
                subtitle = "Complete field research redesign with 12 phases of new features",
                icon = FieldMindIcons.Info,
                trailing = {
                    BackButton(onClick = onBack)
                }
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
