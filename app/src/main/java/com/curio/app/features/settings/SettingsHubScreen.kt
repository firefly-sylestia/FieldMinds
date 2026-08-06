package com.curio.app.features.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.curio.app.navigation.CurioRoutes
import com.curio.app.ui.components.CurioBackButton
import com.curio.app.ui.components.CurioCardHeader
import com.curio.app.ui.components.CurioSectionLabel
import com.curio.app.ui.components.CurioSettingsCard
import com.curio.app.ui.components.CurioSettingsRow
import com.curio.app.ui.components.ScreenEntrance
import com.curio.app.ui.theme.CurioIcons

/** Compact hub for the redesigned settings experience. */
@Composable
fun SettingsHubScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        SettingsHeader(
            title = "Settings",
            subtitle = "Tune Curio your way",
            onBack = { navController.popBackStack() }
        )
        ScreenEntrance {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { CurioSectionLabel("Personalize") }
                item {
                    CurioSettingsCard {
                        CurioCardHeader(CurioIcons.AutoAwesome, "How Curio feels", "Appearance and color")
                        CurioSettingsRow(CurioIcons.DarkMode, "Appearance", "Theme, tint, and pastel color") {
                            navController.navigate(CurioRoutes.SETTINGS_APPEARANCE) { launchSingleTop = true }
                        }
                        com.curio.app.ui.components.CurioSettingsDivider()
                        CurioSettingsRow(CurioIcons.Notifications, "Notifications", "Reminders and explore controls") {
                            navController.navigate(CurioRoutes.SETTINGS_NOTIFICATIONS) { launchSingleTop = true }
                        }
                        com.curio.app.ui.components.CurioSettingsDivider()
                        CurioSettingsRow(CurioIcons.Mic, "Recording", "Voice-note quality and dictation") {
                            navController.navigate(CurioRoutes.SETTINGS_RECORDING) { launchSingleTop = true }
                        }
                    }
                }
                item { CurioSectionLabel("Explore") }
                item {
                    CurioSettingsCard {
                        CurioCardHeader(CurioIcons.ScienceGlyph, "Experiments", "Try visual ideas before they ship")
                        CurioSettingsRow(CurioIcons.Layers, "Card & deck experiments", "Main card, peek deck, and Spin tests") {
                            navController.navigate(CurioRoutes.EXPERIMENTS) { launchSingleTop = true }
                        }
                        com.curio.app.ui.components.CurioSettingsDivider()
                        CurioSettingsRow(CurioIcons.DragHandle, "Manage categories", "Show, hide, or reorder lanes") {
                            navController.navigate(CurioRoutes.MANAGE_CATEGORIES) { launchSingleTop = true }
                        }
                        com.curio.app.ui.components.CurioSettingsDivider()
                        CurioSettingsRow(CurioIcons.History, "Topic history", "Revisit what you explored") {
                            navController.navigate(CurioRoutes.TOPIC_HISTORY) { launchSingleTop = true }
                        }
                    }
                }
                item { CurioSectionLabel("Safety & support") }
                item {
                    CurioSettingsCard {
                        CurioCardHeader(CurioIcons.Backup, "Your data", "Backups and restore")
                        CurioSettingsRow(CurioIcons.Backup, "Backup & restore", "Keep captures and settings safe") {
                            navController.navigate(CurioRoutes.SETTINGS_DATA) { launchSingleTop = true }
                        }
                        com.curio.app.ui.components.CurioSettingsDivider()
                        CurioSettingsRow(CurioIcons.Info, "About Curio", "Replay intro and app details") {
                            navController.navigate(CurioRoutes.SETTINGS_ABOUT) { launchSingleTop = true }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CurioBackButton(onClick = onBack)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold))
            Text(subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
