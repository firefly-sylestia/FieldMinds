package com.curio.app.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.curio.app.data.AppPreferences
import com.curio.app.data.AudioQuality
import com.curio.app.data.AudioQualitySettings
import com.curio.app.data.CategoryId
import com.curio.app.data.CurioCategories
import com.curio.app.data.CurioCategory
import com.curio.app.data.CurioEntry
import com.curio.app.data.CurioRepositoryHolder
import com.curio.app.data.StreakTracker
import com.curio.app.infrastructure.CurioCrashReporter
import com.curio.app.navigation.CurioRoutes
import com.curio.app.ui.components.CurioBackButton
import com.curio.app.ui.components.MorphEntrance
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioGradients
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons

/**
 * Profile — modern Material 3 theme (v2).
 *
 * Layout, top → bottom on a 360×800 dp phone:
 *  24-44 dp   statusBarsPadding()
 *   48 dp     Top bar (`←  Profile`)
 *   12 dp     gap
 *  ~180 dp    **Hero profile card** — 96dp avatar, name (titleLarge),
 *             subtitle, edit-name pill, all on coral-tinted surface.
 *  16 dp     gap
 *  ~120 dp    **Stats grid** — 4 stat cards (Streak · Saved · Recent · Lanes).
 *  16 dp     gap
 *  ~110 dp    **Level / curiosity card** — title + progress strip
 *             (X of 25 topics → next badge).
 *  16 dp     gap
 *  ~330 dp    **Preferences card** — grouped rows: Display name, Theme
 *             (segmented Light/Dark/System), Audio quality, Notifications.
 *  16 dp     gap
 *  ~110 dp    **Your categories** — top 3 active category chips with
 *             capture counts; "Manage" trailing link.
 *  16 dp     gap
 *  ~260 dp    **Recent activity** — up to 5 entry rows.
 *  16 dp     gap
 *  ~180 dp    **Developer & about card** — Test crash, Crash logs (cond.),
 *             Report a bug, Replay intro, Version. All grouped.
 *  24 dp     Bottom + nav-bar inset padding.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val displayName = remember { mutableStateOf(AppPreferences.getDisplayName(context)) }
    val streakDays = StreakTracker.getStreak(context)
    val audioQuality = remember { mutableStateOf(AudioQualitySettings.get(context)) }
    val themeMode = remember { mutableStateOf(AppPreferences.getThemeMode(context)) }
    val reminderEnabled = remember { mutableStateOf(AppPreferences.isReminderEnabled(context)) }
    val showNameDialog = remember { mutableStateOf(false) }
    val nameInput = remember(displayName.value) { mutableStateOf(displayName.value) }
    val crashCount = remember { mutableIntStateOf(0) }

    var totalSaved by remember { mutableIntStateOf(0) }
    var recentEntries by remember { mutableStateOf<List<CurioEntry>>(emptyList()) }
    var categoryCounts by remember { mutableStateOf<Map<CategoryId, Int>>(emptyMap()) }

    LaunchedEffect(Unit) {
        runCatching {
            val entries = CurioRepositoryHolder.repo.getAll()
            totalSaved = entries.size
            recentEntries = entries.take(5)
            categoryCounts = entries.groupingBy { it.topic.categoryId }.eachCount()
        }.onFailure { android.util.Log.e("ProfileScreen", "Failed to load entries", it) }
        crashCount.intValue = CurioCrashReporter.getCrashHistory(context).size
    }

    // Level logic: tiers at 1, 5, 15, 30, 60, 100, 250, 500
    val level = remember(totalSaved) { levelFor(totalSaved) }
    val nextLevelProgress = remember(totalSaved) { progressTowardsNextLevel(totalSaved) }

    if (showNameDialog.value) {
        AlertDialog(
            onDismissRequest = { showNameDialog.value = false },
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    "Display name",
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column {
                    Text(
                        "How should we greet you?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = nameInput.value,
                        onValueChange = { nameInput.value = it },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val n = nameInput.value.ifBlank { "Curious Explorer" }
                        displayName.value = n
                        AppPreferences.setDisplayName(context, n)
                        showNameDialog.value = false
                    }
                ) {
                    Text(
                        "Save",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog.value = false }) {
                    Text(
                        "Cancel",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Top bar ──────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CurioBackButton(onClick = { navController.popBackStack() })
            Text(
                "Profile",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── 1. Hero profile card ─────────────────────────────────────
            item { HeroProfileCard(name = displayName.value, streakDays = streakDays) }

            // ── 2. Stats grid ────────────────────────────────────────────
            item {
                StatsGrid(
                    streak = streakDays,
                    saved = totalSaved,
                    recent = recentEntries.size,
                    lanes = CurioCategories.visible.size
                )
            }

            // ── 3. Level / curiosity card ────────────────────────────────
            item {
                LevelCard(
                    level = level,
                    saved = totalSaved,
                    progressFraction = nextLevelProgress.first,
                    nextThreshold = nextLevelProgress.second,
                    isMaxLevel = nextLevelProgress.first >= 1f && level == 8
                )
            }

            // ── 4. Preferences card (single M3 card; rows inside) ───────
            item {
                PreferencesCard(
                    displayName = displayName.value,
                    onEditName = { nameInput.value = displayName.value; showNameDialog.value = true },
                    themeMode = themeMode.value,
                    onThemeChange = {
                        themeMode.value = it
                        AppPreferences.setThemeMode(context, it)
                    },
                    audioQuality = audioQuality.value,
                    onAudioQualityChange = {
                        audioQuality.value = it
                        AudioQualitySettings.set(context, it)
                    },
                    reminderEnabled = reminderEnabled.value,
                    onReminderToggle = {
                        reminderEnabled.value = it
                        AppPreferences.setReminderEnabled(context, it)
                    },
                    onManageCategories = { navController.navigate(CurioRoutes.MANAGE_CATEGORIES) }
                )
            }

            // ── 5. Your categories ──────────────────────────────────────
            if (categoryCounts.isNotEmpty()) {
                item {
                    CategoriesPreviewCard(
                        counts = categoryCounts,
                        onSeeAll = { navController.navigate(CurioRoutes.MANAGE_CATEGORIES) },
                        onOpenCabinet = { navController.navigate(CurioRoutes.CABINET) }
                    )
                }
            }

            // ── 6. Recent activity ──────────────────────────────────────
            if (recentEntries.isNotEmpty()) {
                item { RecentActivityCard(entries = recentEntries) }
            }

            // ── 7. Dev & about card ─────────────────────────────────────
            item {
                DevAboutCard(
                    crashCount = crashCount.intValue,
                    onTestCrash = { CurioCrashReporter.testCrash() },
                    onCrashLogs = { navController.navigate(CurioRoutes.CRASH) },
                    onReportBug = { navController.navigate(CurioRoutes.BUG_REPORT) },
                    onReplayIntro = {
                        com.curio.app.features.onboarding.CurioOnboardingState.reset(context)
                        navController.navigate(CurioRoutes.ONBOARDING)
                    }
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// Hero profile card
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun HeroProfileCard(name: String, streakDays: Int) {
    MorphEntrance(delayMs = 60) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = CurioColors.CoralBlush.copy(alpha = 0.14f),
            shadowElevation = 0.dp,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Decorative watermark glyph (logomark)
                CurioIcon(
                    CurioIcons.AutoAwesome, null,
                    tint = CurioColors.CoralBlush.copy(alpha = 0.18f),
                    size = 160.dp,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp)
                )
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Avatar bubble
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(CurioColors.CoralBlush, CurioGradients.WildcardGradientStops[2]))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = name.firstOrNull()?.uppercase().orEmpty(),
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = CurioColors.DeepPlum
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = taglineForStreak(streakDays),
                            style = MaterialTheme.typography.bodyMedium,
                            color = CurioColors.DeepPlum.copy(alpha = 0.78f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (streakDays > 0) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = CurioColors.ButterYellow.copy(alpha = 0.45f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        CurioIcon(
                                            "local_fire_department", null,
                                            tint = CurioColors.DeepPlum,
                                            size = 14.dp
                                        )
                                        Text(
                                            "$streakDays-day streak",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = CurioColors.DeepPlum
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun taglineForStreak(streakDays: Int): String = when {
    streakDays >= 30 -> "Marathoner · you've been wonderfully consistent."
    streakDays >= 7 -> "Curious streak ahead of you."
    streakDays >= 1 -> "Keep the streak going."
    else -> "Stay curious."
}

// ═══════════════════════════════════════════════════════════════════════
// Stats grid
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun StatsGrid(streak: Int, saved: Int, recent: Int, lanes: Int) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        shadowElevation = 0.dp,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        // 2-row × 2-col grid
        Column(modifier = Modifier.padding(14.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCell(Modifier.weight(1f), CurioIcons.AutoAwesome, "$streak", "Day streak", CurioColors.CoralBlush)
                StatCell(Modifier.weight(1f), CurioIcons.Inventory2, "$saved", "Captured", CurioColors.Sage)
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCell(Modifier.weight(1f), CurioIcons.History, "$recent", "Recent", CurioColors.Lilac)
                StatCell(Modifier.weight(1f), CurioIcons.Palette, "$lanes", "Lanes open", CurioColors.Teal)
            }
        }
    }
}

@Composable
private fun StatCell(
    modifier: Modifier = Modifier,
    glyph: String,
    value: String,
    label: String,
    tint: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = tint.copy(alpha = 0.12f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tint.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                CurioIcon(glyph, null, tint = tint, size = 18.dp)
            }
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = tint
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// Level / curiosity card
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun LevelCard(
    level: Int,
    saved: Int,
    progressFraction: Float,
    nextThreshold: Int,
    isMaxLevel: Boolean
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(CurioGradients.WildcardGradientStops.take(3))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$level",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color.White
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Level $level · ${levelTitle(level)}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        if (isMaxLevel) "You've discovered it all. Beautiful work."
                        else "$saved / ${nextThreshold} captures · ${nextThreshold - saved} to next badge",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (!isMaxLevel) {
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { progressFraction.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(50)),
                    color = CurioColors.CoralBlush,
                    trackColor = CurioColors.CoralBlush.copy(alpha = 0.15f),
                    strokeCap = ProgressIndicatorDefaults.LinearStrokeCap
                )
            }
        }
    }
}

private fun levelTitle(level: Int): String = when (level) {
    1 -> "First spark"
    2 -> "Curious newcomer"
    3 -> "Tuned ear"
    4 -> "Pattern spotter"
    5 -> "Comparator"
    6 -> "Synthesizer"
    7 -> "Curator"
    else -> "Master explorer"
}

private val levelThresholds = listOf(0, 1, 5, 15, 30, 60, 100, 250, 500)

private fun levelFor(saved: Int): Int {
    var lvl = 1
    for (i in levelThresholds.indices) {
        if (saved >= levelThresholds[i]) lvl = i + 1
    }
    return lvl.coerceIn(1, 8)
}

private fun progressTowardsNextLevel(saved: Int): Pair<Float, Int> {
    val currentLevel = levelFor(saved)
    if (currentLevel >= 8) return 1f to 500
    val from = levelThresholds[currentLevel - 1]
    val to = levelThresholds[currentLevel]
    val span = (to - from).coerceAtLeast(1)
    return (saved - from).toFloat() / span to to
}

// ═══════════════════════════════════════════════════════════════════════
// Preferences card — single M3 card with grouped rows
// ═══════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PreferencesCard(
    displayName: String,
    onEditName: () -> Unit,
    themeMode: String,
    onThemeChange: (String) -> Unit,
    audioQuality: AudioQuality,
    onAudioQualityChange: (AudioQuality) -> Unit,
    reminderEnabled: Boolean,
    onReminderToggle: (Boolean) -> Unit,
    onManageCategories: () -> Unit
) {
    val themes = listOf(AppPreferences.THEME_LIGHT, AppPreferences.THEME_DARK, AppPreferences.THEME_SYSTEM)
    val currentThemeIndex = themes.indexOf(themeMode).coerceAtLeast(0)

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(vertical = 6.dp)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CurioIcon(CurioIcons.Settings, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 18.dp)
                Text(
                    "Preferences",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Display name
            SettingRow(
                icon = CurioIcons.Person,
                title = "Display name",
                supporting = displayName,
                onClick = onEditName
            )
            ThinDivider()

            // Theme segmented
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CurioIcon(CurioIcons.Settings, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 22.dp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Theme",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            listOf("Light", "Dark", "System")[currentThemeIndex],
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp)) {
                    listOf("Light", "Dark", "System").forEachIndexed { index, label ->
                        SegmentedButton(
                            selected = index == currentThemeIndex,
                            onClick = { onThemeChange(themes[index]) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = 3)
                        ) {
                            Text(label, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
            ThinDivider()

            // Audio quality
            SettingRow(
                icon = CurioIcons.Mic,
                title = "Audio quality",
                supporting = audioQuality.label,
                onClick = {
                    val next = when (audioQuality) {
                        AudioQuality.LOW -> AudioQuality.MEDIUM
                        AudioQuality.MEDIUM -> AudioQuality.HIGH
                        AudioQuality.HIGH -> AudioQuality.LOW
                    }
                    onAudioQualityChange(next)
                }
            )
            ThinDivider()

            // Notifications toggle row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                CurioIcon(CurioIcons.Notifications, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 22.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Daily spin reminder",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        if (reminderEnabled) "On — you'll be nudged each day" else "Off — turn on to never miss a spin",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = reminderEnabled,
                    onCheckedChange = onReminderToggle
                )
            }
            ThinDivider()

            // Manage categories
            SettingRow(
                icon = CurioIcons.Palette,
                title = "Manage categories",
                supporting = "${CurioCategories.visible.size} lanes visible",
                onClick = onManageCategories
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// Categories preview card
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun CategoriesPreviewCard(
    counts: Map<CategoryId, Int>,
    onSeeAll: () -> Unit,
    onOpenCabinet: () -> Unit
) {
    val top = counts.entries.sortedByDescending { it.value }.take(3)
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CurioIcon(CurioIcons.Palette, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 18.dp)
                    Text(
                        "Your lanes",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    onClick = onSeeAll,
                    shape = RoundedCornerShape(50),
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            "Manage",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        CurioIcon(
                            CurioIcons.ArrowForward, null,
                            tint = MaterialTheme.colorScheme.primary,
                            size = 14.dp
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(top) { (catId, count) ->
                    val cat = CurioCategories.byId(catId)
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = cat.accent.copy(alpha = 0.14f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(cat.accent.copy(alpha = 0.85f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CurioIcon(cat.iconGlyph, null, tint = Color.White, size = 16.dp)
                            }
                            Column {
                                Text(
                                    cat.displayName,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = cat.accent
                                )
                                Text(
                                    "$count captured",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Surface(
                onClick = onOpenCabinet,
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CurioIcon(
                        CurioIcons.Inventory2, null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        size = 18.dp
                    )
                    Text(
                        "Open the Cabinet",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.weight(1f))
                    CurioIcon(
                        CurioIcons.ArrowForward, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        size = 16.dp
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// Recent activity card
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun RecentActivityCard(entries: List<CurioEntry>) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(vertical = 6.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CurioIcon(CurioIcons.History, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 18.dp)
                Text(
                    "Recent activity",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            entries.forEachIndexed { idx, entry ->
                RecentEntryInline(
                    entry = entry,
                    onClick = { /* handled in row with surface click */ }
                )
                if (idx < entries.lastIndex) ThinDivider()
            }
        }
    }
}

@Composable
private fun RecentEntryInline(entry: CurioEntry, onClick: () -> Unit) {
    val cat = CurioCategories.byId(entry.topic.categoryId)
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(cat.accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                CurioIcon(cat.iconGlyph, null, tint = cat.accent, size = 18.dp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    entry.topic.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${cat.displayName} · ${entry.capturedAtDaysAgoLabel()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            CurioIcon(
                CurioIcons.ArrowForward, null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                size = 16.dp
            )
        }
    }
}

private fun CurioEntry.capturedAtDaysAgoLabel(): String = when (val d = capturedAtDaysAgo) {
    0 -> "today"
    1 -> "yesterday"
    else -> "${d}d ago"
}

// ═══════════════════════════════════════════════════════════════════════
// Developer & about card
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun DevAboutCard(
    crashCount: Int,
    onTestCrash: () -> Unit,
    onCrashLogs: () -> Unit,
    onReportBug: () -> Unit,
    onReplayIntro: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.dp,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(vertical = 6.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CurioIcon(CurioIcons.BugReport, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 18.dp)
                Text(
                    "Developer",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (crashCount > 0) {
                    Spacer(Modifier.weight(1f))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = CurioColors.WarmCoralRed.copy(alpha = 0.15f)
                    ) {
                        Text(
                            "$crashCount crash${if (crashCount != 1) "es" else ""}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = CurioColors.WarmCoralRed,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            SettingRow(
                icon = CurioIcons.BugReport,
                title = "Report a bug",
                supporting = "Send feedback or report an issue",
                onClick = onReportBug
            )
            ThinDivider()

            SettingRow(
                icon = CurioIcons.ErrorOutline,
                title = "Test crash",
                supporting = "Simulate a crash for diagnostics",
                onClick = onTestCrash
            )
            if (crashCount > 0) {
                ThinDivider()
                SettingRow(
                    icon = CurioIcons.History,
                    title = "Crash logs",
                    supporting = "$crashCount log(s) saved",
                    onClick = onCrashLogs
                )
            }
            ThinDivider()

            SettingRow(
                icon = CurioIcons.Replay,
                title = "Replay intro",
                supporting = "See the welcome screens again",
                onClick = onReplayIntro
            )
            ThinDivider()
            SettingRow(
                icon = CurioIcons.Info,
                title = "Version",
                supporting = "1.0.0",
                onClick = { }
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// Row primitives
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun SettingRow(
    icon: String,
    title: String,
    supporting: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CurioIcon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 22.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (supporting.isNotBlank()) {
                    Text(
                        supporting,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            CurioIcon(
                CurioIcons.ArrowForward, null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                size = 16.dp
            )
        }
    }
}

@Composable
private fun ThinDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        modifier = Modifier.padding(start = 54.dp, end = 16.dp)
    )
}
