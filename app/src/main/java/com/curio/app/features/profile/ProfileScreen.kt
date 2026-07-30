package com.curio.app.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.curio.app.data.AppPreferences
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

@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val streakDays = StreakTracker.getStreak(context)
    var audioQuality by remember { mutableStateOf(AudioQualitySettings.get(context)) }
    var displayName by remember { mutableStateOf(AppPreferences.getDisplayName(context)) }
    var themeMode by remember { mutableStateOf(AppPreferences.getThemeMode(context)) }
    var showNameDialog by remember { mutableStateOf(false) }
    var nameInput by remember(displayName) { mutableStateOf(displayName) }

    var totalSaved by remember { mutableIntStateOf(0) }
    var recentEntries by remember { mutableStateOf<List<CurioEntry>>(emptyList()) }
    var crashCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        runCatching {
            val entries = CurioRepositoryHolder.repo.getAll()
            totalSaved = entries.size
            recentEntries = entries.take(5)
        }.onFailure { android.util.Log.e("ProfileScreen", "Failed to load entries", it) }
        crashCount = CurioCrashReporter.getCrashHistory(context).size
    }

    // Name edit dialog
    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            shape = RoundedCornerShape(24.dp),
            title = { Text("Your name", fontWeight = FontWeight.Bold) },
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
            Text("Profile", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        ScreenEntrance {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(
                            Modifier.size(72.dp).clip(CircleShape).background(CurioColors.CoralBlush.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CurioIcon(CurioIcons.Person, null, tint = CurioColors.CoralBlush, size = 36.dp)
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(
                                    displayName,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Surface(
                                    onClick = { showNameDialog = true; nameInput = displayName },
                                    shape = RoundedCornerShape(8.dp),
                                    color = CurioColors.CoralBlush.copy(alpha = 0.10f)
                                ) {
                                    CurioIcon(CurioIcons.Edit, "Edit name", tint = CurioColors.CoralBlush, size = 16.dp, modifier = Modifier.padding(4.dp))
                                }
                            }
                            Text("Stay curious", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Stats
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatCard(Modifier.weight(1f), CurioIcons.AutoAwesome, "$streakDays", "Day streak", CurioColors.CoralBlush)
                        StatCard(Modifier.weight(1f), CurioIcons.Inventory2, "$totalSaved", "Saved", CurioColors.Sage)
                        StatCard(Modifier.weight(1f), CurioIcons.History, "${recentEntries.size}", "Recent", CurioColors.Lilac)
                    }
                }

                // Recent activity
                if (recentEntries.isNotEmpty()) {
                    item {
                        Text("Recent activity", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onBackground)
                    }
                    items(recentEntries.size) { i ->
                        val entry = recentEntries[i]
                        val cat = CurioCategories.byId(entry.topic.categoryId)
                        Surface(
                            onClick = { navController.navigate(CurioRoutes.entryDetail(entry.id)) },
                            shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp
                        ) {
                            Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(Modifier.size(10.dp).clip(CircleShape).background(cat.accent))
                                Column(Modifier.weight(1f)) {
                                    Text(entry.topic.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(cat.displayName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text("${entry.capturedAtDaysAgo}d ago", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                // Quick settings
                item {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(4.dp))
                    Text("Quick settings", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onBackground)
                }
                item {
                    SettingsRow(CurioIcons.Person, "Display name", displayName) { showNameDialog = true; nameInput = displayName }
                }
                item {
                    val themes = listOf(AppPreferences.THEME_LIGHT, AppPreferences.THEME_DARK, AppPreferences.THEME_SYSTEM)
                    val themeLabels = listOf("Light", "Dark", "System")
                    val idx = themes.indexOf(themeMode).coerceAtLeast(0)
                    SettingsRow(CurioIcons.Settings, "Theme", themeLabels[idx]) {
                        val next = themes[(idx + 1) % themes.size]
                        themeMode = next
                        AppPreferences.setThemeMode(context, next)
                    }
                }
                item {
                    SettingsRow(CurioIcons.Mic, "Audio quality", audioQuality.label) {
                        val next = when (audioQuality) {
                            AudioQuality.LOW -> AudioQuality.MEDIUM
                            AudioQuality.MEDIUM -> AudioQuality.HIGH
                            AudioQuality.HIGH -> AudioQuality.LOW
                        }
                        audioQuality = next
                        AudioQualitySettings.set(context, next)
                    }
                }
                item {
                    SettingsRow(CurioIcons.Palette, "Manage categories", "${CurioCategories.visible.size} active") {
                        navController.navigate(CurioRoutes.MANAGE_CATEGORIES)
                    }
                }

                // Data management
                item {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(4.dp))
                    Text("Data", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onBackground)
                }
                item {
                    SettingsRow(CurioIcons.Inventory2, "Export all entries", "$totalSaved entries") {
                        // TODO: implement export
                    }
                }
                item {
                    SettingsRow(CurioIcons.Delete, "Clear all data", "This cannot be undone") {
                        // TODO: implement clear
                    }
                }

                // Developer
                item {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(4.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Developer", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onBackground)
                        if (crashCount > 0) {
                            Surface(shape = RoundedCornerShape(8.dp), color = CurioColors.WarmCoralRed.copy(alpha = 0.15f)) {
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
                    SettingsRow(CurioIcons.ErrorOutline, "Test crash", "Simulate a crash for testing") { CurioCrashReporter.testCrash() }
                }
                if (crashCount > 0) {
                    item {
                        SettingsRow(CurioIcons.History, "Crash logs", "$crashCount log(s) saved") { navController.navigate(CurioRoutes.CRASH) }
                    }
                }
                item {
                    SettingsRow(CurioIcons.BugReport, "Report a bug", "Send feedback or report an issue") { navController.navigate(CurioRoutes.BUG_REPORT) }
                }

                // About
                item {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(4.dp))
                }
                item {
                    SettingsRow(CurioIcons.Replay, "Replay intro", "See the welcome screens again") {
                        com.curio.app.features.onboarding.CurioOnboardingState.reset(context)
                        navController.navigate(CurioRoutes.ONBOARDING)
                    }
                }
                item { SettingsRow(CurioIcons.Info, "Version", "1.0.0") {} }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun StatCard(modifier: Modifier = Modifier, icon: String, value: String, label: String, color: Color) {
    Surface(shape = RoundedCornerShape(16.dp), color = color.copy(alpha = 0.10f), modifier = modifier) {
        Column(Modifier.fillMaxWidth().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            CurioIcon(icon, null, tint = color, size = 22.dp)
            Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SettingsRow(icon: String, label: String, value: String, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(14.dp), color = Color.Transparent) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            CurioIcon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 22.dp)
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            CurioIcon(CurioIcons.ArrowForward, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), size = 18.dp)
        }
    }
}
