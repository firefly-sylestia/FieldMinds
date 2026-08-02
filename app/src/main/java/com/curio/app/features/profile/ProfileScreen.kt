package com.curio.app.features.profile

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.curio.app.data.AppPreferences
import com.curio.app.data.CategoryId
import com.curio.app.data.CurioCategories
import com.curio.app.data.CurioEntry
import com.curio.app.data.CurioRepositoryHolder
import com.curio.app.data.StreakTracker
import com.curio.app.infrastructure.CurioCrashReporter
import com.curio.app.navigation.CurioRoutes
import com.curio.app.ui.components.CurioBackButton
import com.curio.app.ui.components.CurioCardHeader
import com.curio.app.ui.components.CurioForwardArrow
import com.curio.app.ui.components.CurioSettingsCard
import com.curio.app.ui.components.CurioSettingsDivider
import com.curio.app.ui.components.CurioSettingsRow
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioGradients
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.categoryInk
import com.curio.app.ui.theme.themedAccent

/**
 * Profile hub — identity, stats, and a single proper Settings entry.
 *
 * Personalization settings (theme, audio quality, reminders, categories,
 * backup) live in the Settings screen; Profile keeps only a hero, glanceable
 * stats, lanes/activity, and a Settings card that opens that screen. Every
 * row that looks interactive either opens a dialog or navigates; informational
 * values are rendered as text instead of dead buttons.
 */
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var displayName by remember { mutableStateOf(AppPreferences.getDisplayName(context)) }
    var showNameDialog by remember { mutableStateOf(false) }
    var nameInput by remember(displayName) { mutableStateOf(displayName) }
    var crashCount by remember { mutableIntStateOf(0) }
    var totalSaved by remember { mutableIntStateOf(0) }
    var recentEntries by remember { mutableStateOf<List<CurioEntry>>(emptyList()) }
    var categoryCounts by remember { mutableStateOf<Map<CategoryId, Int>>(emptyMap()) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                displayName = AppPreferences.getDisplayName(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        runCatching {
            val entries = CurioRepositoryHolder.repo.getAll()
            totalSaved = entries.size
            recentEntries = entries.take(5)
            categoryCounts = entries.groupingBy { it.topic.categoryId }.eachCount()
        }.onFailure { android.util.Log.e("ProfileScreen", "Failed to load entries", it) }
        crashCount = CurioCrashReporter.getCrashHistory(context).size
    }

    val streakDays = StreakTracker.getStreak(context)
    val level = levelFor(totalSaved)
    val progress = progressTowardsNextLevel(totalSaved)

    ProfileDialogs(
        showNameDialog = showNameDialog,
        nameInput = nameInput,
        onNameInputChange = { nameInput = it },
        onDismissName = { showNameDialog = false },
        onSaveName = {
            displayName = nameInput.trim().ifBlank { "Curious Explorer" }
            AppPreferences.setDisplayName(context, displayName)
            showNameDialog = false
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CurioBackButton(onClick = { navController.popBackStack() })
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Profile",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Your curiosity, in one place",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Gear entry → full Settings screen (backup & restore, categories,
            // theme, etc.). Mirror of CurioBackButton so the top bar stays
            // balanced: back arrow left, gear right.
            Surface(
                onClick = { navController.navigate(CurioRoutes.SETTINGS) { launchSingleTop = true } },
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                CurioIcon(
                    name = CurioIcons.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurface,
                    size = 24.dp,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                ProfileHero(
                    name = displayName,
                    streakDays = streakDays,
                    onEditName = {
                        nameInput = displayName
                        showNameDialog = true
                    }
                )
            }
            item {
                StatsStrip(
                    streak = streakDays,
                    saved = totalSaved,
                    recent = recentEntries.size,
                    lanes = CurioCategories.visible.size
                )
            }
            item {
                LevelCard(
                    level = level,
                    saved = totalSaved,
                    progress = progress.first,
                    nextThreshold = progress.second,
                    isMaxLevel = level >= 9
                )
            }
            item {
                SettingsCard(
                    onOpenSettings = { navController.navigate(CurioRoutes.SETTINGS) { launchSingleTop = true } }
                )
            }
            if (categoryCounts.isNotEmpty()) {
                item {
                    CategoriesCard(
                        counts = categoryCounts,
                        onCabinet = { navController.navigate(CurioRoutes.CABINET) { launchSingleTop = true } }
                    )
                }
            }
            if (recentEntries.isNotEmpty()) {
                item {
                    RecentActivityCard(
                        entries = recentEntries,
                        onEntryClick = { navController.navigate(CurioRoutes.entryDetail(it)) { launchSingleTop = true } }
                    )
                }
            }
            item {
                DeveloperCard(
                    crashCount = crashCount,
                    onTestCrash = { CurioCrashReporter.testCrash() },
                    onCrashLogs = { navController.navigate(CurioRoutes.CRASH) { launchSingleTop = true } },
                    onReportBug = { navController.navigate(CurioRoutes.BUG_REPORT) { launchSingleTop = true } }
                )
            }
            item { Spacer(Modifier.navigationBarsPadding().height(4.dp)) }
        }
    }
}

@Composable
private fun ProfileDialogs(
    showNameDialog: Boolean,
    nameInput: String,
    onNameInputChange: (String) -> Unit,
    onDismissName: () -> Unit,
    onSaveName: () -> Unit
) {
    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = onDismissName,
            shape = RoundedCornerShape(28.dp),
            title = { Text("Display name", fontWeight = FontWeight.ExtraBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("This is how Curio greets you.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = onNameInputChange,
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = { TextButton(onClick = onSaveName) { Text("Save", fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = onDismissName) { Text("Cancel") } }
        )
    }
}

@Composable
private fun ProfileHero(name: String, streakDays: Int, onEditName: () -> Unit) {
    val initial = name.firstOrNull()?.uppercase().orEmpty()
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color.Transparent,
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            CurioColors.CoralBlush,
                            CurioColors.Peach,
                            CurioColors.ButterYellow
                        )
                    )
                )
                .padding(18.dp)
        ) {
            CurioIcon(
                CurioIcons.AutoAwesome,
                null,
                tint = Color.White.copy(alpha = 0.20f),
                size = 116.dp,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(CurioColors.CoralBlush, CurioColors.Peach))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            initial,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = CurioColors.DeepPlum
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            name,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = CurioColors.DeepPlum,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            taglineForStreak(streakDays),
                            style = MaterialTheme.typography.bodyMedium,
                            color = CurioColors.DeepPlum.copy(alpha = 0.76f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        onClick = onEditName,
                        shape = RoundedCornerShape(50),
                        color = CurioColors.DeepPlum,
                        contentColor = Color.White,
                        shadowElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CurioIcon(CurioIcons.Edit, null, tint = Color.White, size = 16.dp)
                            Text("Edit profile", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    if (streakDays > 0) {
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = CurioColors.ButterYellow.copy(alpha = 0.32f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                CurioIcon(CurioIcons.LocalFire, null, tint = CurioColors.DeepPlum, size = 16.dp)
                                Text("$streakDays-day streak", style = MaterialTheme.typography.labelMedium, color = CurioColors.DeepPlum, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsStrip(streak: Int, saved: Int, recent: Int, lanes: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ProfileStat(Modifier.weight(1f), CurioIcons.LocalFire, "$streak", "Streak", CurioColors.CoralBlush)
        ProfileStat(Modifier.weight(1f), CurioIcons.Inventory2, "$saved", "Saved", CurioColors.Sage)
        ProfileStat(Modifier.weight(1f), CurioIcons.History, "$recent", "Recent", CurioColors.Lilac)
        ProfileStat(Modifier.weight(1f), CurioIcons.Palette, "$lanes", "Lanes", CurioColors.Teal)
    }
}

@Composable
private fun ProfileStat(modifier: Modifier, icon: String, value: String, label: String, tint: Color) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = Color.Transparent,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(CurioGradients.cardGradient(tint)))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                CurioIcon(icon, null, tint = CurioColors.DeepPlum.copy(alpha = 0.82f), size = 18.dp)
                Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = CurioColors.DeepPlum)
                Text(label, style = MaterialTheme.typography.labelSmall, color = CurioColors.DeepPlum.copy(alpha = 0.72f))
            }
        }
    }
}

@Composable
private fun LevelCard(level: Int, saved: Int, progress: Float, nextThreshold: Int, isMaxLevel: Boolean) {
    CurioSettingsCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(CurioGradients.WildcardGradientStops.take(3))),
                contentAlignment = Alignment.Center
            ) {
                Text("$level", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = Color.White)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Level $level · ${levelTitle(level)}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text(
                    if (isMaxLevel) "Your curiosity has no ceiling."
                    else "$saved / $nextThreshold saved · ${nextThreshold - saved} to next level",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (!isMaxLevel) {
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)),
                color = CurioColors.CoralBlush,
                trackColor = CurioColors.CoralBlush.copy(alpha = 0.14f)
            )
        }
    }
}

@Composable
private fun SettingsCard(onOpenSettings: () -> Unit) {
    CurioSettingsCard {
        Surface(
            onClick = onOpenSettings,
            color = Color.Transparent,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(Brush.linearGradient(listOf(CurioColors.CoralBlush, CurioColors.Peach))),
                    contentAlignment = Alignment.Center
                ) {
                    CurioIcon(CurioIcons.Settings, null, tint = Color.White, size = 23.dp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Settings", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold))
                    Text(
                        "Theme · reminders · audio · backup",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                CurioForwardArrow(tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f), size = 18.dp)
            }
        }
    }
}

@Composable
private fun CategoriesCard(counts: Map<CategoryId, Int>, onCabinet: () -> Unit) {
    CurioSettingsCard {
        CurioCardHeader(CurioIcons.Palette, "Your lanes", "Where you've been exploring")
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(counts.entries.sortedByDescending { it.value }.take(4)) { (categoryId, count) ->
                val category = CurioCategories.byId(categoryId)
                Surface(shape = RoundedCornerShape(16.dp), color = category.themedAccent().copy(alpha = 0.14f)) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                        CurioIcon(category.iconGlyph, null, tint = MaterialTheme.colorScheme.onSurface, size = 20.dp)
                        Spacer(Modifier.height(4.dp))
                        Text(category.displayName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                        Text("$count saved", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Surface(
            onClick = onCabinet,
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                CurioIcon(CurioIcons.Inventory2, null, size = 18.dp)
                Spacer(Modifier.width(8.dp))
                Text("Open the Cabinet", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                CurioForwardArrow(size = 16.dp)
            }
        }
    }
}

@Composable
private fun RecentActivityCard(entries: List<CurioEntry>, onEntryClick: (String) -> Unit) {
    CurioSettingsCard {
        CurioCardHeader(CurioIcons.History, "Recent activity", "Your latest captures")
        Spacer(Modifier.height(4.dp))
        entries.forEachIndexed { index, entry ->
            RecentEntryRow(entry, { onEntryClick(entry.id) })
            if (index < entries.lastIndex) CurioSettingsDivider()
        }
    }
}

@Composable
private fun RecentEntryRow(entry: CurioEntry, onClick: () -> Unit) {
    val category = CurioCategories.byId(entry.topic.categoryId)
    Surface(onClick = onClick, color = Color.Transparent, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(category.themedAccent().copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) { CurioIcon(category.iconGlyph, null, tint = category.categoryInk(), size = 20.dp) }
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.topic.name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${category.displayName} · ${capturedLabel(entry)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            CurioForwardArrow(tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), size = 17.dp)
        }
    }
}

@Composable
private fun DeveloperCard(
    crashCount: Int,
    onTestCrash: () -> Unit,
    onCrashLogs: () -> Unit,
    onReportBug: () -> Unit
) {
    CurioSettingsCard {
        CurioCardHeader(CurioIcons.Info, "Support & diagnostics", "Help, reports, and developer tools")
        CurioSettingsRow(CurioIcons.BugReport, "Report a bug", "Send feedback or an issue", onReportBug)
        if (crashCount > 0) {
            CurioSettingsDivider()
            CurioSettingsRow(CurioIcons.History, "Crash logs", "$crashCount saved report${if (crashCount == 1) "" else "s"}", onCrashLogs)
        }
        CurioSettingsDivider()
        CurioSettingsRow(CurioIcons.ErrorOutline, "Test crash", "Diagnostic tool", onTestCrash)
    }
}



private fun taglineForStreak(streakDays: Int): String = when {
    streakDays >= 30 -> "Marathon explorer · beautifully consistent."
    streakDays >= 7 -> "A strong curiosity streak is underway."
    streakDays > 0 -> "Keep the spark going today."
    else -> "Stay curious. There is always more to find."
}

private fun capturedLabel(entry: CurioEntry): String = when (entry.capturedAtDaysAgo) {
    0 -> "today"
    1 -> "yesterday"
    else -> "${entry.capturedAtDaysAgo}d ago"
}

private val levelThresholds = listOf(0, 1, 5, 15, 30, 60, 100, 250, 500)

private fun levelFor(saved: Int): Int {
    var level = 1
    levelThresholds.forEachIndexed { index, threshold -> if (saved >= threshold) level = index + 1 }
    return level.coerceIn(1, 9)
}

private fun progressTowardsNextLevel(saved: Int): Pair<Float, Int> {
    val level = levelFor(saved)
    if (level >= 8) return 1f to 500
    val from = levelThresholds[level - 1]
    val to = levelThresholds[level]
    return ((saved - from).toFloat() / (to - from).coerceAtLeast(1)) to to
}

private fun levelTitle(level: Int): String = when (level) {
    1 -> "First spark"
    2 -> "Curious newcomer"
    3 -> "Tuned ear"
    4 -> "Pattern spotter"
    5 -> "Comparator"
    6 -> "Synthesizer"
    7 -> "Curator"
    8 -> "Master curator"
    else -> "Master explorer"
}
