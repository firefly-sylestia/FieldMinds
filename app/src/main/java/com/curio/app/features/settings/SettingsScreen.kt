package com.curio.app.features.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.curio.app.data.AppPreferences
import com.curio.app.data.AudioQuality
import com.curio.app.data.AudioQualitySettings
import com.curio.app.data.CurioBackupManager
import com.curio.app.features.onboarding.CurioOnboardingState
import com.curio.app.navigation.CurioRoutes
import com.curio.app.ui.components.CurioBackButton
import com.curio.app.ui.components.CurioCardHeader
import com.curio.app.ui.components.CurioSectionLabel
import com.curio.app.ui.components.CurioSettingsCard
import com.curio.app.ui.components.CurioSettingsDivider
import com.curio.app.ui.components.CurioSettingsInfoRow
import com.curio.app.ui.components.CurioSettingsRow
import com.curio.app.ui.components.ScreenEntrance
import com.curio.app.ui.components.formatHour
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Settings screen — same card language as Profile so the two screens read
 * as one family: 28dp paper cards with icon-chip headers, arrow rows for
 * navigation, and switches for toggles.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val locale = LocalLocale.current.platformLocale
    var themeMode by remember { mutableStateOf(AppPreferences.getThemeMode(context)) }
    val reminderEnabled = AppPreferences.reminderEnabledState
    var reminderHour by remember { mutableStateOf(AppPreferences.getReminderHour(context)) }
    var audioQuality by remember { mutableStateOf(AudioQualitySettings.get(context)) }
    var displayName by remember { mutableStateOf(AppPreferences.getDisplayName(context)) }
    var showNameDialog by remember { mutableStateOf(false) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var nameInput by remember(displayName) { mutableStateOf(displayName) }
    val versionName = com.curio.app.BuildConfig.VERSION_NAME

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                themeMode = AppPreferences.getThemeMode(context)
                reminderHour = AppPreferences.getReminderHour(context)
                audioQuality = AudioQualitySettings.get(context)
                displayName = AppPreferences.getDisplayName(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // ── Backup & restore (in-app JSON export/import) ─────────────────
    var showRestoreConfirm by remember { mutableStateOf(false) }
    var backupStatus by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var lastBackupAt by remember { mutableStateOf(CurioBackupManager.lastBackupAtMillis(context)) }
    val scope = rememberCoroutineScope()
    val requestNotifications = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            AppPreferences.setReminderEnabled(context, true)
        }
    }

    fun setReminder(enabled: Boolean) {
        if (!enabled) {
            AppPreferences.setReminderEnabled(context, false)
        } else if (
            Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            AppPreferences.setReminderEnabled(context, true)
        }
    }

    // Export → user picks where the backup file goes (Downloads, Drive…).
    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(CurioBackupManager.MIME_TYPE)
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val result = CurioBackupManager.export(context, uri)
                    lastBackupAt = CurioBackupManager.lastBackupAtMillis(context)
                    backupStatus = true to
                        "Backed up ${result.captureCount} capture(s), your settings and sound recordings.\n" +
                        "Keep the file somewhere safe — it brings everything back on a new device."
                } catch (e: Exception) {
                    backupStatus = false to "Backup failed: ${e.message ?: "unknown error"}"
                }
            }
        }
    }

    // Import → user picks a Curio backup file.
    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val result = CurioBackupManager.restore(context, uri)
                    themeMode = AppPreferences.getThemeMode(context)
                    reminderHour = AppPreferences.getReminderHour(context)
                    audioQuality = AudioQualitySettings.get(context)
                    displayName = AppPreferences.getDisplayName(context)
                    backupStatus = true to
                        "Restored ${result.captureCount} capture(s), your settings and sound recordings."
                } catch (e: Exception) {
                    backupStatus = false to "Restore failed: ${e.message ?: "unknown error"}"
                }
            }
        }
    }

    val themes = listOf(AppPreferences.THEME_LIGHT, AppPreferences.THEME_DARK, AppPreferences.THEME_SYSTEM)
    val currentThemeIndex = themes.indexOf(themeMode).coerceAtLeast(0)

    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            shape = RoundedCornerShape(28.dp),
            title = { Text("Display name", fontWeight = FontWeight.ExtraBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("This is how Curio greets you.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    displayName = nameInput.ifBlank { "Curious Explorer" }
                    AppPreferences.setDisplayName(context, displayName)
                    showNameDialog = false
                }) { Text("Save", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showQualityDialog) {
        AudioQualityDialog(
            currentQuality = audioQuality,
            onDismiss = { showQualityDialog = false },
            onSelected = {
                audioQuality = it
                AudioQualitySettings.set(context, it)
                showQualityDialog = false
            }
        )
    }

    // ── Restore confirmation — warns that current data gets replaced ──
    if (showRestoreConfirm) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirm = false },
            shape = RoundedCornerShape(28.dp),
            title = { Text("Restore backup?", fontWeight = FontWeight.ExtraBold) },
            text = {
                Text(
                    "This replaces all of your current captures and settings with " +
                    "the contents of the backup file. This can't be undone."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showRestoreConfirm = false
                    restoreLauncher.launch(arrayOf(CurioBackupManager.MIME_TYPE))
                }) { Text("Continue", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirm = false }) { Text("Cancel") }
            }
        )
    }

    // ── Backup/restore result feedback ───────────────────────────────
    backupStatus?.let { (success, message) ->
        AlertDialog(
            onDismissRequest = { backupStatus = null },
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    if (success) "Done" else "Couldn't do that",
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { backupStatus = null }) { Text("OK", fontWeight = FontWeight.Bold) }
            }
        )
    }

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
                    "Settings",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Tune Curio your way",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        ScreenEntrance {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ── General: who you are + how it looks
                item { CurioSectionLabel("General") }

                // Profile
                item {
                    CurioSettingsCard {
                        CurioCardHeader(CurioIcons.Person, "Profile", "Your personal details")
                        CurioSettingsRow(CurioIcons.Edit, "Display name", displayName) {
                            nameInput = displayName; showNameDialog = true
                        }
                    }
                }

                // Appearance
                item {
                    CurioSettingsCard {
                        CurioCardHeader(CurioIcons.DarkMode, "Appearance", "Theme and look")
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CurioIcon(CurioIcons.DarkMode, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 22.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Theme", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    listOf("Light", "Dark", "System")[currentThemeIndex],
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            listOf("Light", "Dark", "System").forEachIndexed { index, label ->
                                SegmentedButton(
                                    selected = index == currentThemeIndex,
                                    onClick = {
                                        themeMode = themes[index]
                                        AppPreferences.setThemeMode(context, themes[index])
                                    },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = 3)
                                ) {
                                    Text(label, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                        CurioSettingsDivider()
                        // ── Category tint toggle — off restores the plain
                        //    theme background everywhere (cream/midnight).
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CurioIcon(
                                CurioIcons.Palette, null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                size = 22.dp
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Category tint", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    if (AppPreferences.tintWashEnabledState)
                                        "Colorful page backgrounds"
                                    else
                                        "Plain theme background",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = AppPreferences.tintWashEnabledState,
                                onCheckedChange = { AppPreferences.setTintWashEnabled(context, it) }
                            )
                        }
                    }
                }

                // ── Preferences: how content behaves
                item { CurioSectionLabel("Preferences") }

                // Recording
                item {
                    CurioSettingsCard {
                        CurioCardHeader(CurioIcons.Mic, "Recording", "How your voice notes sound")
                        CurioSettingsRow(CurioIcons.Mic, "Audio quality", audioQuality.label) {
                            showQualityDialog = true
                        }
                    }
                }

                // Notifications
                item {
                    CurioSettingsCard {
                        CurioCardHeader(CurioIcons.Notifications, "Notifications", "Daily shuffle reminder")
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CurioIcon(CurioIcons.Notifications, null, tint = CurioColors.CoralBlush, size = 22.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Daily shuffle reminder", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    if (reminderEnabled) "Every day at ${formatHour(reminderHour)}" else "Off · tap the switch to enable",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(checked = reminderEnabled, onCheckedChange = ::setReminder)
                        }
                        if (reminderEnabled) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CurioIcon(CurioIcons.Schedule, null, tint = CurioColors.CoralBlush, size = 18.dp)
                                Text(
                                    "Reminder time",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    formatHour(reminderHour),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(listOf(9, 12, 15, 18, 21)) { hour ->
                                    val selected = hour == reminderHour
                                    Surface(
                                        onClick = { reminderHour = hour; AppPreferences.setReminderHour(context, hour) },
                                        shape = RoundedCornerShape(50),
                                        color = if (selected) CurioColors.CoralBlush else MaterialTheme.colorScheme.surfaceContainerHighest,
                                        contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        border = BorderStroke(1.dp, if (selected) CurioColors.CoralBlush else MaterialTheme.colorScheme.outlineVariant),
                                        shadowElevation = 0.dp
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            if (selected) CurioIcon(CurioIcons.Check, null, tint = Color.White, size = 15.dp)
                                            Text(formatHour(hour), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Categories
                item {
                    CurioSettingsCard {
                        CurioCardHeader(CurioIcons.DragHandle, "Categories", "Show, hide, or reorder")
                        CurioSettingsRow(CurioIcons.DragHandle, "Manage categories", "Show, hide, or reorder") {
                            navController.navigate(CurioRoutes.MANAGE_CATEGORIES) { launchSingleTop = true }
                        }
                    }
                }

                // ── Data: keep everything safe
                item { CurioSectionLabel("Data") }

                // Backup & restore
                item {
                    CurioSettingsCard {
                        CurioCardHeader(CurioIcons.Backup, "Backup & restore", "Keep your data safe")
                        CurioSettingsRow(CurioIcons.Backup, "Back up now", "Save captures, settings + recordings") {
                            backupLauncher.launch(CurioBackupManager.suggestedFileName())
                        }
                        CurioSettingsDivider()
                        CurioSettingsRow(CurioIcons.Restore, "Restore from backup", "Replace current data from a file") {
                            showRestoreConfirm = true
                        }
                        CurioSettingsDivider()
                        val whenLast = if (lastBackupAt > 0L) {
                            SimpleDateFormat("MMM d, yyyy · h:mm a", locale)
                                .format(Date(lastBackupAt))
                        } else "Never"
                        CurioSettingsInfoRow(CurioIcons.History, "Last backup", whenLast)
                    }
                }

                // ── Support: help & app details
                item { CurioSectionLabel("Support") }

                // About
                item {
                    CurioSettingsCard {
                        CurioCardHeader(CurioIcons.Info, "About Curio", "Help, diagnostics, and app details")
                        CurioSettingsRow(CurioIcons.Replay, "Replay intro", "See the welcome screens again") {
                            CurioOnboardingState.reset(context)
                            navController.navigate(CurioRoutes.ONBOARDING) { launchSingleTop = true }
                        }
                        CurioSettingsDivider()
                        CurioSettingsInfoRow(CurioIcons.Info, "Version", versionName)
                    }
                }

                item { Spacer(Modifier.height(4.dp)) }
            }
        }
    }
}

@Composable
private fun AudioQualityDialog(
    currentQuality: AudioQuality,
    onDismiss: () -> Unit,
    onSelected: (AudioQuality) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = { Text("Recording quality", fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Higher quality sounds clearer but uses more storage.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AudioQuality.entries.forEach { quality ->
                    val selected = quality == currentQuality
                    Surface(
                        onClick = { onSelected(quality) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (selected) CurioColors.CoralBlush.copy(alpha = 0.12f) else Color.Transparent,
                        border = BorderStroke(
                            1.dp,
                            if (selected) CurioColors.CoralBlush else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RadioButton(
                                selected = selected,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = CurioColors.CoralBlush
                                )
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
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close", fontWeight = FontWeight.Bold) } }
    )
}

