package com.curio.app.features.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.compose.ui.draw.alpha
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
import com.curio.app.data.SmartDensityMode
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
    var themeStyle by remember { mutableStateOf(AppPreferences.getThemeStyle(context)) }
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
                themeStyle = AppPreferences.getThemeStyle(context)
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
    // ── Notification permission (Android 13+) — requested on demand when
    //    the user turns ON a notification feature. The granted callback
    //    runs the EXACT action the user was trying to perform, so a request
    //    from one toggle never silently enables a different one.
    var pendingNotificationEnable by remember { mutableStateOf<(() -> Unit)?>(null) }
    val requestNotifications = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val action = pendingNotificationEnable
        pendingNotificationEnable = null
        if (granted) action?.invoke()
    }

    fun notificationPermissionMissing(): Boolean =
        Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED

    /** Runs [onGranted] now, or right after the user grants the permission. */
    fun requestNotificationPermission(onGranted: () -> Unit) {
        if (!notificationPermissionMissing()) {
            onGranted()
        } else {
            pendingNotificationEnable = onGranted
            requestNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun setReminder(enabled: Boolean) {
        if (!enabled) {
            AppPreferences.setReminderEnabled(context, false)
        } else {
            requestNotificationPermission {
                AppPreferences.setReminderEnabled(context, true)
            }
        }
    }

    fun setLiveNotifications(enabled: Boolean) {
        if (!enabled) {
            AppPreferences.setLiveNotificationsEnabled(context, false)
        } else {
            requestNotificationPermission {
                AppPreferences.setLiveNotificationsEnabled(context, true)
            }
        }
    }

    // ── Floating explore bubble — needs the "Display over other apps"
    //    special access (no runtime dialog on Android 10+, so we open the
    //    system settings screen and apply the toggle on return).
    val requestOverlaySettings = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        if (Settings.canDrawOverlays(context)) {
            AppPreferences.setOverlayBubbleEnabled(context, true)
        }
    }

    fun openOverlaySettings() {
        runCatching {
            requestOverlaySettings.launch(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                )
            )
        }
    }

    fun setOverlayBubble(enabled: Boolean) {
        if (enabled && !Settings.canDrawOverlays(context)) {
            // Granting the permission first (the toggle applies on return).
            openOverlaySettings()
        } else {
            AppPreferences.setOverlayBubbleEnabled(context, enabled)
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
                    themeStyle = AppPreferences.getThemeStyle(context)
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
    val themeStyles = listOf(
        AppPreferences.THEME_STYLE_DEFAULT,
        AppPreferences.THEME_STYLE_AMOLED,
        AppPreferences.THEME_STYLE_MATERIAL
    )
    val currentStyleIndex = themeStyles.indexOf(themeStyle).coerceAtLeast(0)
    val styleLabel = listOf("Curio", "AMOLED", "Material")[currentStyleIndex]
    val styleDescription = when (themeStyle) {
        AppPreferences.THEME_STYLE_AMOLED ->
            "True black background, always dark, category tints off."
        AppPreferences.THEME_STYLE_MATERIAL ->
            "Your device's Material colors for backgrounds and controls; " +
            "category colors stay true and tints turn off."
        else ->
            "Curio's warm cream palette with category colors."
    }
    // AMOLED ignores the Light/Dark/System pick (it is always dark).
    val themePickerEnabled = themeStyle != AppPreferences.THEME_STYLE_AMOLED

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
                        // ── Theme style — Curio (default) / AMOLED / Material ──
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CurioIcon(CurioIcons.AutoAwesome, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 22.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Theme style", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    styleLabel,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            listOf("Curio", "AMOLED", "Material").forEachIndexed { index, label ->
                                SegmentedButton(
                                    selected = index == currentStyleIndex,
                                    onClick = {
                                        themeStyle = themeStyles[index]
                                        AppPreferences.setThemeStyle(context, themeStyles[index])
                                    },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = 3)
                                ) {
                                    Text(label, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                        Text(
                            styleDescription,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                        )
                        CurioSettingsDivider()
                        // ── Light / Dark / System — ignored while AMOLED is
                        //    active (AMOLED is always dark).
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                                .alpha(if (themePickerEnabled) 1f else 0.4f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CurioIcon(CurioIcons.DarkMode, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 22.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Theme", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    if (themePickerEnabled)
                                        listOf("Light", "Dark", "System")[currentThemeIndex]
                                    else
                                        "AMOLED is always dark",
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
                                    enabled = themePickerEnabled,
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = 3)
                                ) {
                                    Text(label, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                        CurioSettingsDivider()
                        // ── Category tint toggle — off restores the plain
                        //    theme background everywhere (cream/midnight).
                        //    AMOLED + Material force it off regardless of
                        //    this switch (it re-enables on the Curio style).
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
                                    when {
                                        !AppPreferences.tintWashEffective() &&
                                            themeStyle == AppPreferences.THEME_STYLE_DEFAULT ->
                                            "Plain theme background"
                                        !AppPreferences.tintWashEffective() ->
                                            "Off in the $styleLabel theme"
                                        else -> "Colorful page backgrounds"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = AppPreferences.tintWashEffective(),
                                onCheckedChange =
                                    if (themeStyle == AppPreferences.THEME_STYLE_DEFAULT) {
                                        // Double-brace: the outer block carries an
                                        // inner lambda so `it` resolves as the arg.
                                        { AppPreferences.setTintWashEnabled(context, it) }
                                    } else null
                            )
                        }
                        CurioSettingsDivider()
                        // ── Pastel colors (v7.5) — a soft recolor of every
                        //    category accent, the mixed-deck blends and the
                        //    blended page tints: airy pastels with deep ink in
                        //    light mode, muted deep pastels with light ink in
                        //    dark. Independent of the theme style (Curio /
                        //    AMOLED / Material) and the Light/Dark pick.
                        //    Default OFF.
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CurioIcon(
                                CurioIcons.Colorize, null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                size = 22.dp
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Pastel colors", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "Soft pastel accents, blends and page tints in light & dark",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = AppPreferences.pastelColorsState,
                                onCheckedChange = { AppPreferences.setPastelColorsEnabled(context, it) }
                            )
                        }
                        CurioSettingsDivider()
                        // ── Material card blends (v7.8, EXPERIMENTAL) — in the
                        //    Material theme style, cards wear a MIXED gradient
                        //    of the category accent + the device's dynamic
                        //    Material colors (primary / secondary / tertiary),
                        //    in the same multi-stop style as the mixed deck so
                        //    the device palette leads. Default ON; only takes
                        //    effect while the Material style is active.
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CurioIcon(
                                CurioIcons.AutoAwesome, null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                size = 22.dp
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Material card blends", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    when (themeStyle) {
                                        AppPreferences.THEME_STYLE_MATERIAL ->
                                            "Cards mix your category colors with your device's palette"
                                        else -> "Only takes effect in the Material theme style"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = AppPreferences.materialCardBlendsState,
                                onCheckedChange = { AppPreferences.setMaterialCardBlendsEnabled(context, it) }
                            )
                        }
                        CurioSettingsDivider()
                        // ── 3D button gradient & shadow (v7.11, EXPERIMENTAL) ──
                        //    The Spin shuffle button wears a radial 3D gradient
                        //    (highlight at top-left, shadow at bottom) with a
                        //    soft ambient shadow, so it reads as a raised sphere
                        //    instead of a flat circle. Also fixes orbiting ring
                        //    dot visibility in pastel mode. Default ON.
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CurioIcon(
                                CurioIcons.Casino, null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                size = 22.dp
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text("3D button gradient & shadow", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "The shuffle button pops with a sphere-like gradient, soft shadow, and visible orbiting dots",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = AppPreferences.threeDButtonState,
                                onCheckedChange = { AppPreferences.set3DButtonGradientEnabled(context, it) }
                            )
                        }
                        CurioSettingsDivider()
                        // ── Deck cards (v7.7, EXPERIMENTAL) — the Spin deck's
                        //    peek-card look is four independent toggles so each
                        //    upgrade can be A/B'd on its own: top-lit gradient
                        //    fill, category-tinted hairline edges, soft ambient
                        //    shadows, and roomier two-line near-card titles.
                        //    Each OFF by default — the classic flat deck stays
                        //    the shipping look until the experiment settles.
                        Text(
                            "Deck cards",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                        )
                        DeckCardToggleRow(
                            icon = CurioIcons.Layers,
                            title = "Top-lit gradient",
                            subtitle = "Peek cards catch light at the top edge",
                            checked = AppPreferences.peekGradientState,
                            onCheckedChange = { AppPreferences.setPeekGradientEnabled(context, it) }
                        )
                        CurioSettingsDivider()
                        DeckCardToggleRow(
                            icon = CurioIcons.Colorize,
                            title = "Tinted card edges",
                            subtitle = "Category-tinted hairline border on each card",
                            checked = AppPreferences.peekHairlineState,
                            onCheckedChange = { AppPreferences.setPeekHairlineEnabled(context, it) }
                        )
                        CurioSettingsDivider()
                        DeckCardToggleRow(
                            icon = CurioIcons.AutoAwesome,
                            title = "Soft shadows",
                            subtitle = "Gentle ambient shadow under each card",
                            checked = AppPreferences.peekShadowsState,
                            onCheckedChange = { AppPreferences.setPeekShadowsEnabled(context, it) }
                        )
                        CurioSettingsDivider()
                        DeckCardToggleRow(
                            icon = CurioIcons.FormatText,
                            title = "Roomier titles",
                            subtitle = "Two-line near titles with light tracking",
                            checked = AppPreferences.peekTitlesState,
                            onCheckedChange = { AppPreferences.setPeekTitlesEnabled(context, it) }
                        )
                        CurioSettingsDivider()
                        // ── Entry date & mood — the meta card (date / time /
                        //    mood / type) on saved entries + the journal's mood
                        //    and attachment sections. Default ON.
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CurioIcon(
                                CurioIcons.CalendarToday, null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                size = 22.dp
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Entry date & mood", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "Date, time, mood, and attachments on saved entries",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = AppPreferences.entryMetaEnabledState,
                                onCheckedChange = { AppPreferences.setEntryMetaEnabled(context, it) }
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
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CurioIcon(CurioIcons.Timer, null, tint = CurioColors.CoralBlush, size = 22.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Explore sessions", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    if (AppPreferences.exploreSessionsEnabledState)
                                        "Timer, reminder & done prompt when you explore a topic"
                                    else "Off · explore still opens the browser without tracking",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = AppPreferences.exploreSessionsEnabledState,
                                onCheckedChange = { AppPreferences.setExploreSessionsEnabled(context, it) }
                            )
                        }
                        // ── Live explore notification — the persistent
                        //    chronometer notification with Pause/Stop controls
                        //    (like Samsung/Google's live-updating ongoing
                        //    notifications). Off = no ongoing notification,
                        //    only the end-of-session reminder + bubble.
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CurioIcon(CurioIcons.Notifications, null, tint = CurioColors.CoralBlush, size = 22.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Live explore notification", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    if (AppPreferences.liveNotificationsEnabledState)
                                        "Ongoing timer with Pause/Resume & Stop controls"
                                    else "Off · only the end-of-session reminder shows",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = AppPreferences.liveNotificationsEnabledState,
                                onCheckedChange = ::setLiveNotifications
                            )
                        }
                        // ── Floating explore bubble — a Messenger-style timer
                        //    bubble that floats over OTHER apps (the browser)
                        //    while exploring. Needs the "Display over other
                        //    apps" special access; tapping the row opens the
                        //    system settings page when it's missing.
                        Surface(
                            onClick = {
                                if (AppPreferences.overlayBubbleEnabledState) {
                                    if (!Settings.canDrawOverlays(context)) {
                                        openOverlaySettings()
                                    }
                                } else {
                                    setOverlayBubble(true)
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Transparent,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CurioIcon(CurioIcons.BubbleChart, null, tint = CurioColors.CoralBlush, size = 22.dp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Floating explore bubble", style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        when {
                                            !AppPreferences.overlayBubbleEnabledState ->
                                                "Off · timer lives in the notification only"
                                            Settings.canDrawOverlays(context) ->
                                                "Floats over other apps while exploring"
                                            else ->
                                                "Needs \"Display over other apps\" — tap to allow"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = AppPreferences.overlayBubbleEnabledState,
                                    onCheckedChange = ::setOverlayBubble
                                )
                            }
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

                // ── Experimental: new ideas in testing
                item { CurioSectionLabel("Experimental") }

                item {
                    CurioSettingsCard {
                        CurioCardHeader(CurioIcons.ScienceGlyph, "Experimental", "New ideas in testing")
                        // ── Smart Spin layout — the DIMENSION rule of the
                        //    Spin page's smart compact system: short screens
                        //    get the compact (or extra-compact) layout so the
                        //    deck fits. Low-density devices (under 440 dpi)
                        //    always compact regardless of this switch. It
                        //    lives here (not in Appearance) while it's
                        //    experimental — once the winning layout is
                        //    decided, the toggle is removed and the behavior
                        //    hardcoded.
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CurioIcon(
                                CurioIcons.AspectRatio, null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                size = 22.dp
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Smart Spin layout", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "Fits the Spin page on short screens",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = AppPreferences.smartSpinLayoutState,
                                onCheckedChange = { AppPreferences.setSmartSpinLayoutEnabled(context, it) }
                            )
                        }
                        CurioSettingsDivider()
                        // ── Smart density — the DENSITY rule of the Spin
                        //    page's smart sizing, now a 3-way strength picker
                        //    (v7.4): Off disables density sizing, Compact
                        //    keeps the classic two-way rule (< 440 dpi →
                        //    smaller deck, 440+ → roomier), and 2x adds an
                        //    even smaller tier for very low dpi (< 350 dpi).
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CurioIcon(
                                CurioIcons.PhotoSizeSelectLarge, null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                size = 22.dp
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Smart density", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    densityModeSummary(AppPreferences.smartDensityModeState),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            SmartDensityMode.entries.forEachIndexed { index, mode ->
                                SegmentedButton(
                                    selected = mode == AppPreferences.smartDensityModeState,
                                    onClick = { AppPreferences.setSmartDensityMode(context, mode) },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = SmartDensityMode.entries.size
                                    )
                                ) {
                                    Text(densityModeSegmentLabel(mode), style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                        Text(
                            "Smaller deck below 440 dpi · 2x shrinks it further below 350 dpi · roomier deck at 440+ dpi",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                        )
                        Text(
                            "These features are still finding their shape — they may change in future updates.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                        )
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

// ── Deck-card experimental toggles (v7.7) ────────────────────────────────
/** One experimental deck-card toggle row in the Appearance card. */
@Composable
private fun DeckCardToggleRow(
    icon: String,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CurioIcon(
            icon, null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            size = 22.dp
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

// ── Smart density — segment labels + summary copy for the 3-way picker ──
private fun densityModeSegmentLabel(mode: SmartDensityMode): String = when (mode) {
    SmartDensityMode.OFF -> "Off"
    SmartDensityMode.COMPACT -> "Compact"
    SmartDensityMode.EXTRA_COMPACT -> "2x"
}

private fun densityModeSummary(mode: SmartDensityMode): String = when (mode) {
    SmartDensityMode.OFF -> "Density sizing off"
    SmartDensityMode.COMPACT -> "Smaller on low-density phones · larger on high-density"
    SmartDensityMode.EXTRA_COMPACT -> "2x — even smaller on very low-density phones"
}

