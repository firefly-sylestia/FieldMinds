package com.curio.app.features.settings

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.curio.app.data.AppPreferences
import com.curio.app.data.AudioQuality
import com.curio.app.data.AudioQualitySettings
import com.curio.app.features.onboarding.CurioOnboardingState
import com.curio.app.navigation.CurioRoutes
import com.curio.app.ui.components.CurioBackButton
import com.curio.app.ui.components.ScreenEntrance
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    var themeMode by remember { mutableStateOf(AppPreferences.getThemeMode(context)) }
    var reminderEnabled by remember { mutableStateOf(AppPreferences.isReminderEnabled(context)) }
    var reminderHour by remember { mutableStateOf(AppPreferences.getReminderHour(context)) }
    var audioQuality by remember { mutableStateOf(AudioQualitySettings.get(context)) }
    var displayName by remember { mutableStateOf(AppPreferences.getDisplayName(context)) }
    var showNameDialog by remember { mutableStateOf(false) }
    var nameInput by remember(displayName) { mutableStateOf(displayName) }
    val versionName = "0.1.0-curio"

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

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
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
                        val next = when (audioQuality) {
                            AudioQuality.LOW -> AudioQuality.MEDIUM
                            AudioQuality.MEDIUM -> AudioQuality.HIGH
                            AudioQuality.HIGH -> AudioQuality.LOW
                        }
                        audioQuality = next
                        AudioQualitySettings.set(context, next)
                    }
                }

                // Notifications
                item { SectionHeader("Notifications") }
                item {
                    SettingsToggle(
                        CurioIcons.Notifications, "Daily spin reminder",
                        if (reminderEnabled) "Reminder at ${String.format("%02d:00", reminderHour)}" else "Off",
                        reminderEnabled,
                        onToggle = {
                            reminderEnabled = it
                            AppPreferences.setReminderEnabled(context, it)
                        }
                    )
                }
                if (reminderEnabled) {
                    item {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Time:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            listOf(9, 12, 18, 21).forEach { hour ->
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
                        navController.navigate(CurioRoutes.MANAGE_CATEGORIES)
                    }
                }

                // About
                item { SectionHeader("About") }
                item {
                    SettingsItem(CurioIcons.Replay, "Replay intro", "See the welcome screens again") {
                        CurioOnboardingState.reset(context)
                        navController.navigate(CurioRoutes.ONBOARDING)
                    }
                }
                item { SettingsItem(CurioIcons.Info, "Version", versionName) {} }
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
private fun SettingsItem(icon: String, label: String, value: String, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 0.5.dp, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            CurioIcon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 22.dp)
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            CurioIcon(CurioIcons.ArrowForward, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f), size = 18.dp)
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
