package com.curio.app.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.curio.app.data.CurioCategories
import com.curio.app.data.CurioEntry
import com.curio.app.data.CurioRepositoryHolder
import com.curio.app.data.StreakTracker
import com.curio.app.infrastructure.CurioCrashReporter
import com.curio.app.navigation.CurioRoutes
import com.curio.app.ui.components.CurioBackButton
import com.curio.app.ui.components.ScreenEntrance
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons

/**
 * Curio Profile — redesigned, clean, with real data and developer section.
 *
 * Sections:
 *   1. Profile header with avatar + greeting
 *   2. Stats: streak, spins, saved entries
 *   3. Quick settings: Audio quality, Theme, Manage categories
 *   4. Developer: Test crash, Crash logs, Report bug
 *   5. About: Replay intro, Version
 */
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val streakDays = StreakTracker.getStreak(context)
    var audioQuality by remember { mutableStateOf(AudioQualitySettings.get(context)) }

    var totalSaved by remember { mutableIntStateOf(0) }
    var recentEntries by remember { mutableStateOf<List<CurioEntry>>(emptyList()) }
    var crashCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        runCatching {
            val entries = CurioRepositoryHolder.repo.getAll()
            totalSaved = entries.size
            recentEntries = entries.take(5)
        }
        crashCount = CurioCrashReporter.getCrashHistory(context).size
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CurioBackButton(onClick = { navController.popBackStack() })
            Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        ScreenEntrance {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // ═══════════════════════════════════════════════════════
                // Profile header
                // ═══════════════════════════════════════════════════════
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Gradient avatar
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.sweepGradient(
                                        listOf(CurioColors.CoralBlush, CurioColors.Peach, CurioColors.CoralBlush)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            CurioIcon(CurioIcons.Person, null, tint = Color.White, size = 36.dp)
                        }

                        Column {
                            Text(
                                "Curious Explorer",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                "Stay curious ✦",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // ═══════════════════════════════════════════════════════
                // Stats
                // ═══════════════════════════════════════════════════════
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon = CurioIcons.AutoAwesome,
                            value = "$streakDays",
                            label = "Day streak",
                            color = CurioColors.CoralBlush
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon = CurioIcons.Inventory2,
                            value = "$totalSaved",
                            label = "Saved",
                            color = CurioColors.Sage
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon = CurioIcons.History,
                            value = "${recentEntries.size}",
                            label = "Recent",
                            color = CurioColors.Lilac
                        )
                    }
                }

                // ═══════════════════════════════════════════════════════
                // Recent activity
                // ═══════════════════════════════════════════════════════
                if (recentEntries.isNotEmpty()) {
                    item {
                        Text(
                            "Recent activity",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    items(recentEntries.size) { i ->
                        val entry = recentEntries[i]
                        val cat = CurioCategories.byId(entry.topic.categoryId)
                        Surface(
                            onClick = { navController.navigate(CurioRoutes.entryDetail(entry.id)) },
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 1.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(cat.accent)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        entry.topic.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        maxLines = 1, overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        cat.displayName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    "${entry.capturedAtDaysAgo}d ago",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // ═══════════════════════════════════════════════════════
                // Quick settings
                // ═══════════════════════════════════════════════════════
                item {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Quick settings",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                item {
                    SettingsRow(
                        icon = CurioIcons.Mic,
                        label = "Audio quality",
                        value = audioQuality.label,
                        onClick = {
                            val next = when (audioQuality) {
                                AudioQuality.LOW -> AudioQuality.MEDIUM
                                AudioQuality.MEDIUM -> AudioQuality.HIGH
                                AudioQuality.HIGH -> AudioQuality.LOW
                            }
                            audioQuality = next
                            AudioQualitySettings.set(context, next)
                        }
                    )
                }

                item {
                    SettingsRow(
                        icon = CurioIcons.Palette,
                        label = "Manage categories",
                        value = "${CurioCategories.visible.size} active",
                        onClick = { navController.navigate(CurioRoutes.MANAGE_CATEGORIES) }
                    )
                }

                // ═══════════════════════════════════════════════════════
                // Developer
                // ═══════════════════════════════════════════════════════
                item {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Developer",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (crashCount > 0) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CurioColors.WarmCoralRed.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    "$crashCount crash${if (crashCount != 1) "es" else ""}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CurioColors.WarmCoralRed,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    SettingsRow(
                        icon = CurioIcons.ErrorOutline,
                        label = "Test crash",
                        value = "Simulate a crash for testing",
                        onClick = { CurioCrashReporter.testCrash() }
                    )
                }

                if (crashCount > 0) {
                    item {
                        SettingsRow(
                            icon = CurioIcons.History,
                            label = "Crash logs",
                            value = "$crashCount log(s) saved",
                            onClick = { navController.navigate(CurioRoutes.CRASH) }
                        )
                    }
                }

                item {
                    SettingsRow(
                        icon = CurioIcons.BugReport,
                        label = "Report a bug",
                        value = "Send feedback or report an issue",
                        onClick = { navController.navigate(CurioRoutes.BUG_REPORT) }
                    )
                }

                // ═══════════════════════════════════════════════════════
                // About
                // ═══════════════════════════════════════════════════════
                item {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(4.dp))
                }

                item {
                    SettingsRow(
                        icon = CurioIcons.Replay,
                        label = "Replay intro",
                        value = "See the welcome screens again",
                        onClick = {
                            com.curio.app.features.onboarding.CurioOnboardingState.reset(context)
                            navController.navigate(CurioRoutes.ONBOARDING)
                        }
                    )
                }

                item {
                    SettingsRow(
                        icon = CurioIcons.Info,
                        label = "Version",
                        value = "0.1.0-curio",
                        onClick = {}
                    )
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: String,
    value: String,
    label: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.10f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CurioIcon(icon, null, tint = color, size = 22.dp)
            Text(
                value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = color
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsRow(
    icon: String,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CurioIcon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 22.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    value,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            CurioIcon(
                CurioIcons.ChevronRight, null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                size = 18.dp
            )
        }
    }
}
