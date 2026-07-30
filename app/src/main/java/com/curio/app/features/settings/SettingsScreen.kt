package com.curio.app.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.curio.app.features.onboarding.CurioOnboardingState
import com.curio.app.navigation.CurioRoutes
import com.curio.app.ui.components.CurioBackButton
import com.curio.app.ui.components.CurioSectionLabel
import com.curio.app.ui.components.CurioSettingsRow
import com.curio.app.ui.components.CurioThemeMode
import com.curio.app.ui.components.CurioTimePickerRow
import com.curio.app.ui.components.CurioToggleRow
import com.curio.app.ui.components.ScreenEntrance

/**
 * Settings — see CURIO_SPEC.md §11 (v2).
 *
 * The LEAST playful screen in the app. Uses Material3 ListItem layout with
 * no brand-colored chips, no gradients, no whimsy. Aim: calm + utilitarian.
 *
 * Sections (top → bottom):
 *   - Profile (placeholder row showing name field)
 *   - Categories → routes to MANAGE_CATEGORIES
 *   - Appearance (Light / Dark / System segmented buttons)
 *   - Notifications (Daily spin reminder switch → reveals time picker)
 *   - About (Replay intro → routes to ONBOARDING; Version)
 *
 * Uses shared settings components from CurioSettingsComponents.kt.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    var themeMode by remember { mutableStateOf(CurioThemeMode.SYSTEM) }
    var dailyReminderEnabled by remember { mutableStateOf(false) }
    val versionName = "1.0.0"
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CurioBackButton(onClick = { navController.popBackStack() })
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        ScreenEntrance {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                item { CurioSectionLabel("Profile") }
                item {
                    CurioSettingsRow(
                        title = "Name",
                        subtitle = "Curious Explorer",
                        onClick = { /* name edit launchpad */ }
                    )
                }

                item { CurioSectionLabel("Categories") }
                item {
                    CurioSettingsRow(
                        title = "Manage categories",
                        subtitle = "Show, hide, or reorder the 6 categories",
                        onClick = { navController.navigate(CurioRoutes.MANAGE_CATEGORIES) }
                    )
                }

                item { CurioSectionLabel("Appearance") }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "Theme",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CurioThemeMode.values().forEachIndexed { index, mode ->
                                SegmentedButton(
                                    selected = themeMode == mode,
                                    onClick = { themeMode = mode },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = CurioThemeMode.values().size
                                    )
                                ) {
                                    Text(
                                        text = mode.label,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }
                }

                item { CurioSectionLabel("Notifications") }
                item {
                    CurioToggleRow(
                        title = "Daily spin reminder",
                        subtitle = if (dailyReminderEnabled) {
                            "We'll nudge you once a day"
                        } else {
                            "Off"
                        },
                        checked = dailyReminderEnabled,
                        onCheckedChange = { dailyReminderEnabled = it }
                    )
                }
                if (dailyReminderEnabled) {
                    item { CurioTimePickerRow() }
                }

                item { CurioSectionLabel("About") }
                item {
                    CurioSettingsRow(
                        title = "Replay intro",
                        subtitle = "See the 3-slide welcome again",
                        onClick = {
                            CurioOnboardingState.reset(context)
                            navController.navigate(CurioRoutes.ONBOARDING)
                        }
                    )
                }
                item {
                    CurioSettingsRow(
                        title = "Version",
                        subtitle = versionName,
                        onClick = {}
                    )
                }
            }
        }
    }
}
