package com.curio.app.features.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.curio.app.data.AppPreferences
import com.curio.app.data.AudioQuality
import com.curio.app.data.AudioQualitySettings
import com.curio.app.features.onboarding.CurioOnboardingState
import com.curio.app.navigation.CurioRoutes
import com.curio.app.ui.components.CurioCardHeader
import com.curio.app.ui.components.CurioSectionLabel
import com.curio.app.ui.components.CurioSettingsCard
import com.curio.app.ui.components.CurioSettingsDivider
import com.curio.app.ui.components.CurioSettingsInfoRow
import com.curio.app.ui.components.CurioSettingsRow
import com.curio.app.ui.components.formatHour
import com.curio.app.ui.theme.CurioIcons

/** Settings destination selected from the compact hub. */
enum class SettingsPage(val title: String, val subtitle: String) {
    APPEARANCE("Appearance", "Theme, tint, and color mood"),
    NOTIFICATIONS("Notifications", "Reminders and explore controls"),
    RECORDING("Recording", "Voice-note quality and dictation"),
    DATA("Backup & restore", "Keep your captures safe"),
    ABOUT("About Curio", "Help and app details")
}

@Composable
fun SettingsSectionScreen(navController: NavController, page: SettingsPage) {
    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        SettingsHeader(title = page.title, subtitle = page.subtitle, onBack = { navController.popBackStack() })
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { CurioSectionLabel(page.title) }
            item {
                when (page) {
                    SettingsPage.APPEARANCE -> AppearanceSection()
                    SettingsPage.NOTIFICATIONS -> NotificationsSection()
                    SettingsPage.RECORDING -> RecordingSection()
                    SettingsPage.DATA -> DataSection(navController)
                    SettingsPage.ABOUT -> AboutSection(navController)
                }
            }
        }
    }
}

@Composable
private fun AppearanceSection() {
    val context = LocalContext.current
    val themeStyles = listOf(AppPreferences.THEME_STYLE_DEFAULT, AppPreferences.THEME_STYLE_AMOLED, AppPreferences.THEME_STYLE_MATERIAL)
    val themes = listOf(AppPreferences.THEME_LIGHT, AppPreferences.THEME_DARK, AppPreferences.THEME_SYSTEM)
    val themeStyle = AppPreferences.themeStyleState
    val themeMode = AppPreferences.themeModeState
    val styleIndex = themeStyles.indexOf(themeStyle).coerceAtLeast(0)
    val themeIndex = themes.indexOf(themeMode).coerceAtLeast(0)
    CurioSettingsCard {
        CurioCardHeader(CurioIcons.AutoAwesome, "Visual language", "Small choices shape every page")
        CompactSegmentedRow("Theme style", listOf("Curio", "AMOLED", "Material"), styleIndex) { index ->
            AppPreferences.setThemeStyle(context, themeStyles[index])
        }
        CurioSettingsDivider()
        CompactSegmentedRow("Theme", listOf("Light", "Dark", "System"), themeIndex, enabled = themeStyle != AppPreferences.THEME_STYLE_AMOLED) { index ->
            AppPreferences.setThemeMode(context, themes[index])
        }
        CurioSettingsDivider()
        CompactSwitchRow("Category tint", "Colorful page backgrounds", AppPreferences.tintWashEffective(), themeStyle == AppPreferences.THEME_STYLE_DEFAULT) {
            AppPreferences.setTintWashEnabled(context, it)
        }
        CurioSettingsDivider()
        CompactSwitchRow("Pastel colors", "Soft category accents and page tints", AppPreferences.pastelColorsState) {
            AppPreferences.setPastelColorsEnabled(context, it)
        }
        CurioSettingsDivider()
        CompactSwitchRow("Entry date & mood", "Date, mood, and attachments on saved entries", AppPreferences.entryMetaEnabledState) {
            AppPreferences.setEntryMetaEnabled(context, it)
        }
    }
}

@Composable
private fun NotificationsSection() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var reminderHour by remember { mutableStateOf(AppPreferences.getReminderHour(context)) }
    var overlayEnabled by remember { mutableStateOf(AppPreferences.overlayBubbleEnabledState) }
    var liveNotificationsEnabled by remember { mutableStateOf(AppPreferences.liveNotificationsEnabledState) }
    var exploreSessionsEnabled by remember { mutableStateOf(AppPreferences.exploreSessionsEnabledState) }
    val permissionMissing = Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                reminderHour = AppPreferences.getReminderHour(context)
                overlayEnabled = AppPreferences.isOverlayBubbleEnabled(context)
                liveNotificationsEnabled = AppPreferences.isLiveNotificationsEnabled(context)
                exploreSessionsEnabled = AppPreferences.isExploreSessionsEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    var pendingEnable by remember { mutableStateOf<(() -> Unit)?>(null) }
    val overlaySettingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (AppPreferences.overlayActuallyUsable(context)) {
            AppPreferences.setOverlayBubbleEnabled(context, true)
            overlayEnabled = true
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) pendingEnable?.invoke()
        pendingEnable = null
    }
    fun enableNotifications(action: () -> Unit) {
        if (!permissionMissing) action() else {
            pendingEnable = action
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    CurioSettingsCard {
        CurioCardHeader(CurioIcons.Notifications, "Notifications", "Quiet nudges, when you want them")
        CompactSwitchRow("Daily shuffle reminder", if (AppPreferences.reminderEnabledState) "Every day at ${formatHour(AppPreferences.getReminderHour(context))}" else "Off", AppPreferences.reminderEnabledState) { enabled ->
            if (enabled) enableNotifications { AppPreferences.setReminderEnabled(context, true) } else AppPreferences.setReminderEnabled(context, false)
        }
        if (AppPreferences.reminderEnabledState) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 6.dp)) {
                items(listOf(9, 12, 15, 18, 21)) { hour ->
                    val selected = hour == reminderHour
                    Surface(
                        onClick = {
                            reminderHour = hour
                            AppPreferences.setReminderHour(context, hour)
                        },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Text(
                            formatHour(hour),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
        CurioSettingsDivider()
        CompactSwitchRow("Explore sessions", "Timer, reminder, and done prompt", exploreSessionsEnabled) {
            exploreSessionsEnabled = it
            AppPreferences.setExploreSessionsEnabled(context, it)
        }
        CurioSettingsDivider()
        CompactSwitchRow("Live explore notification", "Ongoing timer with pause and stop", liveNotificationsEnabled) { enabled ->
            if (enabled) enableNotifications {
                liveNotificationsEnabled = true
                AppPreferences.setLiveNotificationsEnabled(context, true)
            } else {
                liveNotificationsEnabled = false
                AppPreferences.setLiveNotificationsEnabled(context, false)
            }
        }
        CurioSettingsDivider()
        CompactSwitchRow("Floating explore bubble", "Timer bubble over other apps", overlayEnabled) { enabled ->
            if (enabled && !AppPreferences.overlayActuallyUsable(context)) {
                runCatching {
                    overlaySettingsLauncher.launch(
                        Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                    )
                }
            } else {
                overlayEnabled = enabled
                AppPreferences.setOverlayBubbleEnabled(context, enabled)
            }
        }
    }
}

@Composable
private fun RecordingSection() {
    val context = LocalContext.current
    var quality by remember { mutableStateOf(AudioQualitySettings.get(context)) }
    var showQualityDialog by remember { mutableStateOf(false) }
    CurioSettingsCard {
        CurioCardHeader(CurioIcons.Mic, "Recording", "Voice notes that sound like you")
        CurioSettingsRow(CurioIcons.Mic, "Audio quality", quality.label) {
            showQualityDialog = true
        }
        CurioSettingsDivider()
        CompactSwitchRow("Voice-to-text", "Dictation buttons on voice-note fields", AppPreferences.voiceToTextEnabledState) {
            AppPreferences.setVoiceToTextEnabled(context, it)
        }
    }
    if (showQualityDialog) {
        AudioQualityDialog(
            currentQuality = quality,
            onDismiss = { showQualityDialog = false },
            onSelected = {
                quality = it
                AudioQualitySettings.set(context, it)
                showQualityDialog = false
            }
        )
    }
}

@Composable
private fun DataSection(navController: NavController) {
    CurioSettingsCard {
        CurioCardHeader(CurioIcons.Backup, "Backup & restore", "Your captures stay yours")
        CurioSettingsRow(CurioIcons.Backup, "Open backup tools", "Export, restore, or import FieldMind data") {
            navController.navigate(CurioRoutes.SETTINGS_DATA) { launchSingleTop = true }
        }
        CurioSettingsDivider()
        CurioSettingsInfoRow(CurioIcons.History, "Backup workspace", "Full backup tools remain in the data workspace")
    }
}

@Composable
private fun AboutSection(navController: NavController) {
    val context = LocalContext.current
    CurioSettingsCard {
        CurioCardHeader(CurioIcons.Info, "About Curio", "Help and app details")
        CurioSettingsRow(CurioIcons.Replay, "Replay intro", "See the welcome screens again") {
            CurioOnboardingState.reset(context)
            navController.navigate(CurioRoutes.ONBOARDING) { launchSingleTop = true }
        }
        CurioSettingsDivider()
        CurioSettingsInfoRow(CurioIcons.Info, "Version", com.curio.app.BuildConfig.VERSION_NAME)
    }
}

@Composable
private fun CompactSegmentedRow(title: String, labels: List<String>, selectedIndex: Int, enabled: Boolean = true, onSelected: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            labels.forEachIndexed { index, label ->
                SegmentedButton(
                    selected = index == selectedIndex,
                    onClick = { onSelected(index) },
                    enabled = enabled,
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = labels.size)
                ) { Text(label, style = MaterialTheme.typography.labelSmall) }
            }
        }
    }
}

@Composable
private fun CompactSwitchRow(title: String, subtitle: String, checked: Boolean, enabled: Boolean = true, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, enabled = enabled, onCheckedChange = onCheckedChange)
    }
}
