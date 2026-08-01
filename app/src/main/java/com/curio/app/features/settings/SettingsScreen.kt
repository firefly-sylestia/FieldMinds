package com.curio.app.features.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.curio.app.data.AppPreferences
import com.curio.app.data.AudioQuality
import com.curio.app.data.AudioQualitySettings
import com.curio.app.data.CurioBackupManager
import com.curio.app.features.onboarding.CurioOnboardingState
import com.curio.app.navigation.CurioRoutes
import com.curio.app.ui.components.CurioBackButton
import com.curio.app.ui.components.CurioForwardArrow
import com.curio.app.ui.components.ScreenEntrance
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

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
            shape = RoundedCornerShape(24.dp),
            title = { Text("Display name", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    displayName = nameInput.ifBlank { "Curious Explorer" }
                    AppPreferences.setDisplayName(context, displayName)
                    showNameDialog = false
                }) { Text("Save") }
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
            shape = RoundedCornerShape(24.dp),
            title = { Text("Restore backup?", fontWeight = FontWeight.Bold) },
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
                }) { Text("Continue") }
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
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    if (success) "Done" else "Couldn't do that",
                    fontWeight = FontWeight.Bold
                )
            },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { backupStatus = null }) { Text("OK") }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CurioBackButton(onClick = { navController.popBackStack() })
            Text("Settings", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        ScreenEntrance {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Profile
                item { SectionHeader("Profile") }
                item {
                    SettingsItem(CurioIcons.Person, "Display name", displayName) {
                        nameInput = displayName; showNameDialog = true
                    }
                }

                // Appearance
                item { SectionHeader("Appearance") }
                item {
                    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Text(
                            "Theme",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                            listOf("Light", "Dark", "System").forEachIndexed { index, label ->
                                SegmentedButton(
                                    selected = index == currentThemeIndex,
                                    onClick = {
                                        themeMode = themes[index]
                                        AppPreferences.setThemeMode(context, themes[index])
                                    },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = 3)
                                ) {
                                    Text(label, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }

                // Audio
                item { SectionHeader("Recording") }
                item {
                    SettingsItem(CurioIcons.Mic, "Audio quality", audioQuality.label) {
                        showQualityDialog = true
                    }
                }

                // Notifications
                item { SectionHeader("Notifications") }
                item {
                    SettingsToggle(
                        CurioIcons.Notifications, "Daily shuffle reminder",
                        if (reminderEnabled) "Reminder at ${String.format("%02d:00", reminderHour)}" else "Off",
                        reminderEnabled,
                        onToggle = ::setReminder
                    )
                }
                if (reminderEnabled) {
                    item {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Time:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            listOf(9, 12, 15, 18, 21).forEach { hour ->
                                Surface(
                                    onClick = { reminderHour = hour; AppPreferences.setReminderHour(context, hour) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (reminderHour == hour) CurioColors.CoralBlush.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        String.format("%02d:00", hour),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (reminderHour == hour) FontWeight.Bold else FontWeight.Normal),
                                        color = if (reminderHour == hour) CurioColors.CoralBlush else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Categories
                item { SectionHeader("Categories") }
                item {
                    SettingsItem(CurioIcons.DragHandle, "Manage categories", "Show, hide, or reorder") {
                        navController.navigate(CurioRoutes.MANAGE_CATEGORIES) { launchSingleTop = true }
                    }
                }

                // Backup & restore
                item { SectionHeader("Backup & restore") }
                item {
                    SettingsItem(CurioIcons.Backup, "Back up now", "Save captures, settings + recordings") {
                        backupLauncher.launch(CurioBackupManager.suggestedFileName())
                    }
                }
                item {
                    SettingsItem(CurioIcons.Restore, "Restore from backup", "Replace current data from a file") {
                        showRestoreConfirm = true
                    }
                }
                item {
                    val whenLast = if (lastBackupAt > 0L) {
                        SimpleDateFormat("MMM d, yyyy · h:mm a", locale)
                            .format(Date(lastBackupAt))
                    } else "Never"
                    SettingsInfo(CurioIcons.History, "Last backup", whenLast)
                }

                // About
                item { SectionHeader("About") }
                item {
                    SettingsItem(CurioIcons.Replay, "Replay intro", "See the welcome screens again") {
                        CurioOnboardingState.reset(context)
                        navController.navigate(CurioRoutes.ONBOARDING) { launchSingleTop = true }
                    }
                }
                item { SettingsInfo(CurioIcons.Info, "Version", versionName) }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Spacer(Modifier.height(8.dp))
    Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onBackground)
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun SettingsInfo(icon: String, label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CurioIcon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 22.dp)
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        shape = RoundedCornerShape(24.dp),
        title = { Text("Recording quality", fontWeight = FontWeight.Bold) },
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
                        shape = RoundedCornerShape(14.dp),
                        color = if (selected) CurioColors.CoralBlush.copy(alpha = 0.12f) else Color.Transparent,
                        border = androidx.compose.foundation.BorderStroke(
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
                            androidx.compose.material3.RadioButton(
                                selected = selected,
                                onClick = null,
                                colors = androidx.compose.material3.RadioButtonDefaults.colors(
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
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun SettingsItem(icon: String, label: String, value: String, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 0.5.dp, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            CurioIcon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 22.dp)
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            CurioForwardArrow(tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
        }
    }
}

@Composable
private fun SettingsToggle(icon: String, label: String, value: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 0.5.dp, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            CurioIcon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 22.dp)
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            androidx.compose.material3.Switch(checked = checked, onCheckedChange = onToggle)
        }
    }
}
