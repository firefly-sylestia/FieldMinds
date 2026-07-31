package com.curio.app.features.profile

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.curio.app.data.AppPreferences
import com.curio.app.data.AudioQuality
import com.curio.app.data.AudioQualitySettings
import com.curio.app.data.CategoryId
import com.curio.app.data.CurioCategories
import com.curio.app.data.CurioEntry
import com.curio.app.data.CurioRepositoryHolder
import com.curio.app.data.StreakTracker
import com.curio.app.features.onboarding.CurioOnboardingState
import com.curio.app.infrastructure.CurioCrashReporter
import com.curio.app.navigation.CurioRoutes
import com.curio.app.ui.components.CurioBackButton
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioGradients
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons

/**
 * Profile and personal settings hub.
 *
 * The screen intentionally uses a calm hierarchy: a compact identity hero,
 * four glanceable stats, then settings and activity cards. Every row that
 * looks interactive either opens a dialog or navigates; informational values
 * are rendered as text instead of dead buttons.
 */
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var displayName by remember { mutableStateOf(AppPreferences.getDisplayName(context)) }
    var themeMode by remember { mutableStateOf(AppPreferences.getThemeMode(context)) }
    var audioQuality by remember { mutableStateOf(AudioQualitySettings.get(context)) }
    val reminderEnabled = AppPreferences.reminderEnabledState
    var reminderHour by remember { mutableIntStateOf(AppPreferences.getReminderHour(context)) }
    var showNameDialog by remember { mutableStateOf(false) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }
    var showVersionDialog by remember { mutableStateOf(false) }
    var nameInput by remember(displayName) { mutableStateOf(displayName) }
    var crashCount by remember { mutableIntStateOf(0) }
    var totalSaved by remember { mutableIntStateOf(0) }
    var recentEntries by remember { mutableStateOf<List<CurioEntry>>(emptyList()) }
    var categoryCounts by remember { mutableStateOf<Map<CategoryId, Int>>(emptyMap()) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                displayName = AppPreferences.getDisplayName(context)
                themeMode = AppPreferences.getThemeMode(context)
                audioQuality = AudioQualitySettings.get(context)
                reminderHour = AppPreferences.getReminderHour(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val requestNotifications = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            AppPreferences.setReminderEnabled(context, true)
        }
    }

    fun enableReminder() {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            AppPreferences.setReminderEnabled(context, true)
        }
    }

    fun setReminder(enabled: Boolean) {
        if (enabled) enableReminder()
        else {
            AppPreferences.setReminderEnabled(context, false)
        }
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
    val themes = listOf(AppPreferences.THEME_LIGHT, AppPreferences.THEME_DARK, AppPreferences.THEME_SYSTEM)
    val themeLabels = listOf("Light", "Dark", "System")
    val themeIndex = themes.indexOf(themeMode).coerceAtLeast(0)
    val versionName = com.curio.app.BuildConfig.VERSION_NAME

    ProfileDialogs(
        showNameDialog = showNameDialog,
        nameInput = nameInput,
        onNameInputChange = { nameInput = it },
        onDismissName = { showNameDialog = false },
        onSaveName = {
            displayName = nameInput.trim().ifBlank { "Curious Explorer" }
            AppPreferences.setDisplayName(context, displayName)
            showNameDialog = false
        },
        showQualityDialog = showQualityDialog,
        currentQuality = audioQuality,
        onQualitySelected = {
            audioQuality = it
            AudioQualitySettings.set(context, it)
            showQualityDialog = false
        },
        showReminderDialog = showReminderDialog,
        reminderHour = reminderHour,
        onReminderHourSelected = {
            reminderHour = it
            AppPreferences.setReminderHour(context, it)
            showReminderDialog = false
        },
        showVersionDialog = showVersionDialog,
        versionName = versionName,
        onDismissVersion = { showVersionDialog = false }
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
                PreferencesCard(
                    displayName = displayName,
                    themeIndex = themeIndex,
                    themeLabels = themeLabels,
                    onEditName = {
                        nameInput = displayName
                        showNameDialog = true
                    },
                    onThemeChange = {
                        themeMode = themes[it]
                        AppPreferences.setThemeMode(context, themes[it])
                    },
                    audioQuality = audioQuality,
                    onAudioQualityClick = { showQualityDialog = true },
                    reminderEnabled = reminderEnabled,
                    reminderHour = reminderHour,
                    onReminderToggle = ::setReminder,
                    onReminderTimeClick = { if (reminderEnabled) showReminderDialog = true },
                    onManageCategories = { navController.navigate(CurioRoutes.MANAGE_CATEGORIES) }
                )
            }
            if (categoryCounts.isNotEmpty()) {
                item {
                    CategoriesCard(
                        counts = categoryCounts,
                        onManage = { navController.navigate(CurioRoutes.MANAGE_CATEGORIES) },
                        onCabinet = { navController.navigate(CurioRoutes.CABINET) }
                    )
                }
            }
            if (recentEntries.isNotEmpty()) {
                item {
                    RecentActivityCard(
                        entries = recentEntries,
                        onEntryClick = { navController.navigate(CurioRoutes.entryDetail(it)) }
                    )
                }
            }
            item {
                DeveloperCard(
                    crashCount = crashCount,
                    onTestCrash = { CurioCrashReporter.testCrash() },
                    onCrashLogs = { navController.navigate(CurioRoutes.CRASH) },
                    onReportBug = { navController.navigate(CurioRoutes.BUG_REPORT) },
                    onReplayIntro = {
                        CurioOnboardingState.reset(context)
                        navController.navigate(CurioRoutes.ONBOARDING)
                    },
                    versionName = versionName,
                    onVersion = { showVersionDialog = true }
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
    onSaveName: () -> Unit,
    showQualityDialog: Boolean,
    currentQuality: AudioQuality,
    onQualitySelected: (AudioQuality) -> Unit,
    showReminderDialog: Boolean,
    reminderHour: Int,
    onReminderHourSelected: (Int) -> Unit,
    showVersionDialog: Boolean,
    versionName: String,
    onDismissVersion: () -> Unit
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

    if (showQualityDialog) {
        AlertDialog(
            onDismissRequest = { onQualitySelected(currentQuality) },
            shape = RoundedCornerShape(28.dp),
            title = { Text("Recording quality", fontWeight = FontWeight.ExtraBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Choose the balance that feels right for your voice notes.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    AudioQuality.entries.forEach { quality ->
                        val selected = quality == currentQuality
                        Surface(
                            onClick = { onQualitySelected(quality) },
                            shape = RoundedCornerShape(16.dp),
                            color = if (selected) CurioColors.CoralBlush.copy(alpha = 0.12f) else Color.Transparent,
                            border = BorderStroke(
                                1.dp,
                                if (selected) CurioColors.CoralBlush else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                RadioButton(
                                    selected = selected,
                                    onClick = null,
                                    colors = RadioButtonDefaults.colors(selectedColor = CurioColors.CoralBlush)
                                )
                                Column {
                                    Text(quality.label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
                                    Text(quality.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { onQualitySelected(currentQuality) }) { Text("Close") } }
        )
    }

    if (showReminderDialog) {
        val hours = listOf(9, 12, 15, 18, 21)
        AlertDialog(
            onDismissRequest = { onReminderHourSelected(reminderHour) },
            shape = RoundedCornerShape(28.dp),
            title = { Text("Reminder time", fontWeight = FontWeight.ExtraBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Choose when Curio should send your daily nudge.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    hours.forEach { hour ->
                        val selected = hour == reminderHour
                        Surface(
                            onClick = { onReminderHourSelected(hour) },
                            shape = RoundedCornerShape(16.dp),
                            color = if (selected) CurioColors.ButterYellow.copy(alpha = 0.22f) else Color.Transparent,
                            border = BorderStroke(
                                1.dp,
                                if (selected) CurioColors.ButterYellow else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CurioIcon(CurioIcons.Schedule, null, tint = CurioColors.CoralBlush, size = 20.dp)
                                Text(formatHour(hour), fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
                                if (selected) {
                                    Spacer(Modifier.weight(1f))
                                    CurioIcon(CurioIcons.Check, null, tint = CurioColors.CoralBlush, size = 18.dp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { onReminderHourSelected(reminderHour) }) { Text("Done") } }
        )
    }

    if (showVersionDialog) {
        AlertDialog(
            onDismissRequest = onDismissVersion,
            shape = RoundedCornerShape(28.dp),
            title = { Text("Curio", fontWeight = FontWeight.ExtraBold) },
            text = { Text("Version $versionName\n\nA small place for big curiosity.") },
            confirmButton = { TextButton(onClick = onDismissVersion) { Text("Close") } }
        )
    }
}

@Composable
private fun ProfileHero(name: String, streakDays: Int, onEditName: () -> Unit) {
    val initial = name.firstOrNull()?.uppercase().orEmpty()
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color.Transparent,
        shadowElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            CurioColors.CoralBlush.copy(alpha = 0.24f),
                            CurioColors.Lilac.copy(alpha = 0.12f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            CurioIcon(
                CurioIcons.AutoAwesome,
                null,
                tint = CurioColors.CoralBlush.copy(alpha = 0.16f),
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
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            taglineForStreak(streakDays),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CurioIcon(CurioIcons.Edit, null, tint = CurioColors.CoralBlush, size = 16.dp)
                            Text("Edit profile", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
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
    // Coloured container — accent-tinted background, readable onSurface text.
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            CurioIcon(icon, null, tint = tint, size = 17.dp)
            Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = tint)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LevelCard(level: Int, saved: Int, progress: Float, nextThreshold: Int, isMaxLevel: Boolean) {
    ProfileCard {
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
private fun PreferencesCard(
    displayName: String,
    themeIndex: Int,
    themeLabels: List<String>,
    onEditName: () -> Unit,
    onThemeChange: (Int) -> Unit,
    audioQuality: AudioQuality,
    onAudioQualityClick: () -> Unit,
    reminderEnabled: Boolean,
    reminderHour: Int,
    onReminderToggle: (Boolean) -> Unit,
    onReminderTimeClick: () -> Unit,
    onManageCategories: () -> Unit
) {
    ProfileCard {
        CardHeader(CurioIcons.Settings, "Preferences", "Personalize how Curio feels")
        ProfileSettingRow(CurioIcons.Person, "Display name", displayName, onEditName)
        ProfileDivider()
        Column(modifier = Modifier.padding(vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CurioIcon(CurioIcons.DarkMode, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 22.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text("Theme", style = MaterialTheme.typography.bodyLarge)
                    Text(themeLabels[themeIndex], style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(10.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                themeLabels.forEachIndexed { index, label ->
                    SegmentedButton(
                        selected = index == themeIndex,
                        onClick = { onThemeChange(index) },
                        shape = SegmentedButtonDefaults.itemShape(index, themeLabels.size)
                    ) { Text(label, style = MaterialTheme.typography.labelSmall) }
                }
            }
        }
        ProfileDivider()
        ProfileSettingRow(CurioIcons.Mic, "Audio quality", audioQuality.label, onAudioQualityClick)
        ProfileDivider()
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CurioIcon(CurioIcons.Notifications, null, tint = CurioColors.CoralBlush, size = 22.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text("Daily spin reminder", style = MaterialTheme.typography.bodyLarge)
                Text(
                    if (reminderEnabled) "Every day at ${formatHour(reminderHour)}" else "Off · tap the switch to enable",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = reminderEnabled, onCheckedChange = onReminderToggle)
        }
        if (reminderEnabled) {
            Surface(
                onClick = onReminderTimeClick,
                shape = RoundedCornerShape(14.dp),
                color = CurioColors.ButterYellow.copy(alpha = 0.14f),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CurioIcon(CurioIcons.Schedule, null, tint = CurioColors.CoralBlush, size = 18.dp)
                    Text("Change reminder time", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    Text(formatHour(reminderHour), style = MaterialTheme.typography.labelLarge, color = CurioColors.CoralBlush, fontWeight = FontWeight.Bold)
                }
            }
        }
        ProfileDivider()
        ProfileSettingRow(CurioIcons.Palette, "Manage categories", "${CurioCategories.visible.size} lanes visible", onManageCategories)
    }
}

@Composable
private fun CategoriesCard(counts: Map<CategoryId, Int>, onManage: () -> Unit, onCabinet: () -> Unit) {
    ProfileCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CardHeader(CurioIcons.Palette, "Your lanes", "Where you've been exploring", Modifier.weight(1f))
            TextButton(onClick = onManage) { Text("Manage") }
        }
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(counts.entries.sortedByDescending { it.value }.take(4)) { (categoryId, count) ->
                val category = CurioCategories.byId(categoryId)
                Surface(shape = RoundedCornerShape(16.dp), color = category.accent.copy(alpha = 0.14f)) {
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
                CurioIcon(CurioIcons.ArrowForward, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 16.dp)
            }
        }
    }
}

@Composable
private fun RecentActivityCard(entries: List<CurioEntry>, onEntryClick: (String) -> Unit) {
    ProfileCard {
        CardHeader(CurioIcons.History, "Recent activity", "Your latest captures")
        Spacer(Modifier.height(4.dp))
        entries.forEachIndexed { index, entry ->
            RecentEntryRow(entry, { onEntryClick(entry.id) })
            if (index < entries.lastIndex) ProfileDivider()
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
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(category.accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) { CurioIcon(category.iconGlyph, null, tint = category.accent, size = 20.dp) }
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.topic.name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${category.displayName} · ${capturedLabel(entry)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            CurioIcon(CurioIcons.ArrowForward, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), size = 17.dp)
        }
    }
}

@Composable
private fun DeveloperCard(
    crashCount: Int,
    onTestCrash: () -> Unit,
    onCrashLogs: () -> Unit,
    onReportBug: () -> Unit,
    onReplayIntro: () -> Unit,
    versionName: String,
    onVersion: () -> Unit
) {
    ProfileCard {
        CardHeader(CurioIcons.Info, "About Curio", "Help, diagnostics, and app details")
        ProfileSettingRow(CurioIcons.BugReport, "Report a bug", "Send feedback or an issue", onReportBug)
        ProfileDivider()
        ProfileSettingRow(CurioIcons.Replay, "Replay intro", "See the welcome screens again", onReplayIntro)
        if (crashCount > 0) {
            ProfileDivider()
            ProfileSettingRow(CurioIcons.History, "Crash logs", "$crashCount saved report${if (crashCount == 1) "" else "s"}", onCrashLogs)
        }
        ProfileDivider()
        ProfileSettingRow(CurioIcons.ErrorOutline, "Test crash", "Diagnostic tool", onTestCrash)
        ProfileDivider()
        ProfileSettingRow(CurioIcons.Info, "Version", versionName, onVersion)
    }
}

@Composable
private fun ProfileCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) { Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), content = content) }
}

@Composable
private fun CardHeader(icon: String, title: String, subtitle: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(shape = RoundedCornerShape(11.dp), color = CurioColors.CoralBlush.copy(alpha = 0.12f), modifier = Modifier.size(34.dp)) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) { CurioIcon(icon, null, tint = CurioColors.CoralBlush, size = 18.dp) }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun ProfileSettingRow(icon: String, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.Transparent, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(vertical = 13.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CurioIcon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 21.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            CurioIcon(CurioIcons.ArrowForward, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f), size = 17.dp)
        }
    }
}

@Composable
private fun ProfileDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f), modifier = Modifier.padding(start = 33.dp))
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

private fun formatHour(hour: Int): String {
    val normalized = hour.coerceIn(0, 23)
    val suffix = if (normalized < 12) "AM" else "PM"
    val display = when (val h = normalized % 12) { 0 -> 12 else -> h }
    return "$display:00 $suffix"
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
