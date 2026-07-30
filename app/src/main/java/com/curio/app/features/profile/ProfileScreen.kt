package com.curio.app.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.curio.app.data.AudioQuality
import com.curio.app.data.AudioQualitySettings
import com.curio.app.features.onboarding.CurioOnboardingState
import com.curio.app.navigation.CurioRoutes
import com.curio.app.ui.components.CurioBackButton
import com.curio.app.ui.components.CurioSectionLabel
import com.curio.app.ui.components.CurioSettingsRow
import com.curio.app.ui.components.CurioThemeMode
import com.curio.app.ui.components.CurioTimePickerRow
import com.curio.app.ui.components.CurioToggleRow
import com.curio.app.ui.components.ScreenEntrance
import com.curio.app.ui.components.StaggeredEntrance
import com.curio.app.ui.components.StaggeredItem
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.CurioMotion

/** Holds placeholder data for a single activity-log row. */
private data class ActivityEntry(
    val topicName: String,
    val category: String,
    val time: String,
    val tint: Color
)

/**
 * Unified Profile + Settings screen.
 *
 * Layout (top → bottom):
 *   1. Top bar: ← Back + "Profile"
 *   2. Profile header card: avatar circle, name, edit button
 *   3. Stats row: streak, total spins, saved entries (3 stat cards)
 *   4. Activity log: recent topic history (last 5 spins)
 *   5. Settings sections inline:
 *      - Categories → Manage Categories
 *      - Appearance → Theme segmented button
 *      - Notifications → Daily reminder toggle + time picker
 *      - About → Replay intro, Version
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    var userName by remember { mutableStateOf("Curious Explorer") }
    var themeMode by remember { mutableStateOf(CurioThemeMode.SYSTEM) }
    var audioQuality by remember { mutableStateOf(AudioQualitySettings.get(context)) }
    var dailyReminderEnabled by remember { mutableStateOf(false) }
    val versionName = "1.0.0"

    // ── Mock stats (placeholder phase) ────────────────────────────────────
    val streakDays = remember { mutableIntStateOf(3) }
    val totalSpins = remember { mutableIntStateOf(12) }
    val savedEntries = remember { mutableIntStateOf(5) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // ── Top bar ───────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CurioBackButton(onClick = { navController.popBackStack() })
            Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        ScreenEntrance {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // ── Profile header card ────────────────────────────────────
                item {
                    StaggeredEntrance {
                        StaggeredItem(index = 0) {
                            ProfileHeader(
                                userName = userName,
                                onEditName = { /* name edit — placeholder */ }
                            )
                        }
                    }
                }

                item { Spacer(Modifier.height(20.dp)) }

                // ── Stats row ──────────────────────────────────────────────
                item {
                    StaggeredEntrance(staggerDelayMs = CurioMotion.Stagger.Fast) {
                        StaggeredItem(index = 1) {
                            Text(
                                text = "Your curiosity",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        StaggeredItem(index = 2) {
                            Spacer(Modifier.height(8.dp))
                            StatsRow(
                                streakDays = streakDays.intValue,
                                totalSpins = totalSpins.intValue,
                                savedEntries = savedEntries.intValue
                            )
                        }
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }

                // ── Activity log ───────────────────────────────────────────
                item {
                    StaggeredEntrance(staggerDelayMs = CurioMotion.Stagger.Base) {
                        StaggeredItem(index = 3) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Recent activity",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "See all →",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .clickable {
                                            navController.navigate(CurioRoutes.TOPIC_HISTORY)
                                        }
                                        .padding(4.dp)
                                )
                            }
                        }
                        StaggeredItem(index = 4) {
                            Spacer(Modifier.height(8.dp))
                            ActivityLogPreview()
                        }
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }
                item { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant) }

                // ── Settings: Categories ───────────────────────────────────                item { CurioSectionLabel("Categories") }
                item {
                    CurioSettingsRow(
                        title = "Manage categories",
                        subtitle = "Show, hide, or reorder the 6 categories",
                        onClick = { navController.navigate(CurioRoutes.MANAGE_CATEGORIES) }
                    )
                }

                // ── Settings: Appearance ───────────────────────────────────
                item { CurioSectionLabel("Appearance") }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "Theme",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CurioThemeMode.values().forEachIndexed { index, mode ->
                                SegmentedButton(
                                    selected =                            themeMode == mode,
                                    onClick = { themeMode = mode },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = CurioThemeMode.values().size
                                    )
                                ) {
                                    Text(
                                        text = mode.label,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Settings: Notifications ────────────────────────────────
                item { CurioSectionLabel("Notifications") }
                item {
                    CurioToggleRow(
                        title = "Daily spin reminder",
                        subtitle = if (dailyReminderEnabled) {
                            "We'll nudge you once a day"
                        } else {
                            "Off"
                        },
                        checked = dailyReminderEnabled,
                        onCheckedChange = { dailyReminderEnabled = it }
                    )
                }
                if (dailyReminderEnabled) {
                    item { CurioTimePickerRow() }
                }

                // ── Settings: Recording ──────────────────────────────────
                item { CurioSectionLabel("Recording") }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "Audio quality",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = audioQuality.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AudioQuality.entries.forEachIndexed { index, quality ->
                                SegmentedButton(
                                    selected = audioQuality == quality,
                                    onClick = {
                                        audioQuality = quality
                                        AudioQualitySettings.set(context, quality)
                                    },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = AudioQuality.entries.size
                                    )
                                ) {
                                    Text(
                                        text = quality.label,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Settings: About ────────────────────────────────────────
                item { CurioSectionLabel("About") }
                item {
                    CurioSettingsRow(
                        title = "Replay intro",
                        subtitle = "See the 3-slide welcome again",
                        onClick = {
                            CurioOnboardingState.isComplete = false
                            navController.navigate(CurioRoutes.ONBOARDING)
                        }
                    )
                }
                item {
                    CurioSettingsRow(
                        title = "Version",
                        subtitle = versionName,
                        onClick = {}
                    )
                }

                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Profile header
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun ProfileHeader(
    userName: String,
    onEditName: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                CurioColors.CoralBlush,
                                CurioColors.Peach
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                CurioIcon(
                    name = CurioIcons.Person,
                    contentDescription = null,
                    tint = CurioColors.CreamWhite,
                    size = 32.dp
                )
            }

            // Name + subtitle
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Keep exploring, stay curious ✦",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Edit button
            Surface(
                onClick = onEditName,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                CurioIcon(
                    name = CurioIcons.Edit,
                    contentDescription = "Edit name",
                    tint = MaterialTheme.colorScheme.onSurface,
                    size = 18.dp,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Stats row
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun StatsRow(
    streakDays: Int,
    totalSpins: Int,
    savedEntries: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            icon = CurioIcons.AutoAwesome,
            value = "$streakDays",
            label = "Day streak",
            tint = CurioColors.CoralBlush
        )
        StatCard(
            modifier = Modifier.weight(1f),
            icon = CurioIcons.Casino,
            value = "$totalSpins",
            label = "Spins",
            tint = CurioColors.Lilac
        )
        StatCard(
            modifier = Modifier.weight(1f),
            icon = CurioIcons.Inventory2,
            value = "$savedEntries",
            label = "Saved",
            tint = CurioColors.Sage
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: String,
    value: String,
    label: String,
    tint: Color
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = tint.copy(alpha = 0.10f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CurioIcon(
                name = icon,
                contentDescription = null,
                tint = tint,
                size = 20.dp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = tint
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Activity log preview
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun ActivityLogPreview() {
    // Placeholder activity entries — will be backed by real data later
    val recentActivity = remember {
        listOf(
            ActivityEntry("Frida Kahlo", "Visual Art", "2 hours ago", CurioColors.Peach),
            ActivityEntry("Brian Eno", "Music", "Yesterday", CurioColors.Lilac),
            ActivityEntry("Marie Curie", "Science", "2 days ago", CurioColors.Teal),
            ActivityEntry("Stanley Kubrick", "Movies", "3 days ago", CurioColors.DustyBlue)
        )
    }

    if (recentActivity.isEmpty()) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CurioIcon(
                    name = CurioIcons.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    size = 36.dp
                )
                Text(
                    text = "No activity yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Give the wheel a spin to start your journey",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            recentActivity.forEach { entry ->
                ActivityRow(entry = entry)
            }
        }
    }
}

@Composable
private fun ActivityRow(entry: ActivityEntry) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Category dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(entry.tint)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.topicName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = entry.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
