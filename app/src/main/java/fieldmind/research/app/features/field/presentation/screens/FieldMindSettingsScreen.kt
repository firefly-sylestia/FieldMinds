package fieldmind.research.app.features.field.presentation.screens
import fieldmind.research.app.ui.theme.CuteCardDefaults

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.animateContentSize
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import fieldmind.research.app.features.field.data.database.entity.*
import fieldmind.research.app.features.field.data.settings.FieldMindSettings
import fieldmind.research.app.features.field.data.vision.RegionalPack
import fieldmind.research.app.shared.data.model.AppSettings as SharedAppSettings
import fieldmind.research.app.features.field.data.vision.SpeciesDatabase
import fieldmind.research.app.features.field.data.weather.WeatherProviders
import fieldmind.research.app.features.field.presentation.components.*
import fieldmind.research.app.features.field.presentation.theme.FieldMindTheme
import fieldmind.research.app.features.field.presentation.viewmodel.FieldMindViewModel
import fieldmind.research.app.shared.presentation.components.icons.Icon
import fieldmind.research.app.shared.presentation.components.icons.MaterialSymbolIcon
import fieldmind.research.app.features.field.presentation.components.ColorSchemeSwatchPicker
import fieldmind.research.app.features.field.presentation.components.pressScale
import fieldmind.research.app.features.field.presentation.components.FieldMindLogo

import fieldmind.research.app.features.field.presentation.screens.DevWeatherTestPanel
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.BorderStroke
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import fieldmind.research.app.ui.theme.CuteGradients
import fieldmind.research.app.ui.theme.cuteShadow
import fieldmind.research.app.ui.theme.CuteElevations

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.platform.LocalUriHandler
import fieldmind.research.app.infrastructure.updates.UpdateChecker
import fieldmind.research.app.infrastructure.updates.UpdateInfo

// ══════════════════════════════════════════════════════════════════════
//  Data Classes
// ══════════════════════════════════════════════════════════════════════

private data class ScreenVisibilityItem(
    val title: String,
    val description: String,
    val isEnabled: Boolean,
    val icon: MaterialSymbolIcon,
    val accentColor: Color
)

// ══════════════════════════════════════════════════════════════════════
//  Settings Hub
// ══════════════════════════════════════════════════════════════════════

@Composable
fun FieldMindSettingsScreen(
    viewModel: FieldMindViewModel? = null,
    onBack: () -> Unit,
    onResetOnboarding: () -> Unit,
    onOpenExport: (() -> Unit)? = null,
    onOpenAbout: (() -> Unit)? = null,
    onOpenProfile: (() -> Unit)? = null,
    onOpenAppearance: (() -> Unit)? = null,
    onOpenCapture: (() -> Unit)? = null,
    onOpenAi: (() -> Unit)? = null,
    onOpenLocalModel: (() -> Unit)? = null,
    onOpenBackup: (() -> Unit)? = null,
    onOpenSecurity: (() -> Unit)? = null,
    onOpenChangelog: (() -> Unit)? = null,
    onOpenUnits: (() -> Unit)? = null,
    onOpenWeather: (() -> Unit)? = null,
    onOpenMap: (() -> Unit)? = null,
    onOpenDataIntegrity: (() -> Unit)? = null,
    onOpenDeveloper: (() -> Unit)? = null,
    onOpenSpeciesPacks: (() -> Unit)? = null,
    onOpenSpeciesId: (() -> Unit)? = null,
    onOpenAutoGen: (() -> Unit)? = null,
    onOpenScreenVisibility: (() -> Unit)? = null,
    onOpenNotifications: (() -> Unit)? = null,
    onOpenAnimations: (() -> Unit)? = null,
    onOpenBugReport: (() -> Unit)? = null,
    onOpenCheckForUpdates: (() -> Unit)? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    val settingsScrollState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    LazyColumn(
        state = settingsScrollState,
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(20.dp, 12.dp, 20.dp, 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            StandardScreenHeader(
                title = "Settings",
                subtitle = "Offline-first setup, profile, capture, local AI, backup, and privacy.",
                icon = FieldMindIcons.Settings,
                heroColor = FieldMindTheme.colors.info,
                trailing = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { isSearchActive = !isSearchActive; if (!isSearchActive) searchQuery = "" }, modifier = Modifier.size(40.dp)) {
                            Icon(FieldMindIcons.Search, contentDescription = "Search settings", size = 20.dp, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        BackButton(onClick = onBack)
                    }
                }
            )
        }

        if (isSearchActive) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search settings...") },
                    leadingIcon = { Icon(FieldMindIcons.Search, null, size = 20.dp) },
                    trailingIcon = { if (searchQuery.isNotBlank()) IconButton(onClick = { searchQuery = "" }) { Icon(MaterialSymbolIcon("close"), contentDescription = "Clear", size = 18.dp) } },
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                )
            }
        }



        // ╔════════════════════════════════════════════╗
        // ║  PROFILE                                   ║
        // ╚════════════════════════════════════════════╝
        item { SectionHeader("Profile", "Your research identity and preferences") }
        item { SettingsNavCard("Research profile", "Name, role, and research focus", FieldMindIcons.Nature, FieldMindTheme.colors.observation) { onOpenProfile?.invoke() } }
        item { SettingsNavCard("Screen visibility", "Show/hide navigation tabs and features", FieldMindIcons.Visibility, FieldMindTheme.colors.info) { onOpenScreenVisibility?.invoke() } }

        // ╔════════════════════════════════════════════╗
        // ║  DISPLAY & FORMAT                          ║
        // ╚════════════════════════════════════════════╝
        item { SectionHeader("Display & format", "Appearance, units, and display preferences") }
        item { SettingsNavCard("Appearance", "Theme, dynamic color, map, and layout", FieldMindIcons.Palette, FieldMindTheme.colors.info) { onOpenAppearance?.invoke() } }
        item { SettingsNavCard("Units & format", "Temperature, distance, date/time display", FieldMindIcons.Settings, FieldMindTheme.colors.info) { onOpenUnits?.invoke() } }
        item { SettingsNavCard("Animations", "Entrance effects, speed preset, disable animations", MaterialSymbolIcon("motion_photos_on"), FieldMindTheme.colors.flashcard) { onOpenAnimations?.invoke() } }

        // ╔════════════════════════════════════════════╗
        // ║  DATA ENTRY                                ║
        // ╚════════════════════════════��═══════════════╝
        item { SectionHeader("Data & capture", "Capture defaults, weather, and species tools") }
        item { SettingsNavCard("Capture defaults", "Categories, confidence, goal, location", FieldMindIcons.Capture, FieldMindTheme.colors.observation) { onOpenCapture?.invoke() } }
        item { SettingsNavCard("Weather", "Auto-weather, temperature unit, refresh, widget display", FieldMindIcons.Weather, FieldMindTheme.colors.info) { onOpenWeather?.invoke() } }
        item { SettingsNavCard("Species tools", "Image ID, API keys, model packs, and regional catalogs", FieldMindIcons.Nature, FieldMindTheme.colors.observation) { onOpenSpeciesId?.invoke() } }
        item { SettingsNavCard("Notifications", "Weather alerts, task reminders, and session prompts", FieldMindIcons.Notifications, FieldMindTheme.colors.info) { onOpenNotifications?.invoke() } }

        // ╔════════════════════════════════════════════╗
        // ║  AI ASSISTANCE                             ║
        // ╚════════════════════════════════════════════╝
        item { SectionHeader("AI & intelligence", "AI assistant, local model, and auto-generation") }
        item { SettingsNavCard("AI assistant", "Gemini, OpenAI, provider settings", FieldMindIcons.Sparkle, FieldMindTheme.colors.flashcard) { onOpenAi?.invoke() } }
        item { SettingsNavCard("Local model", "On-device study generation profiles", FieldMindIcons.Download, FieldMindTheme.colors.hypothesis) { onOpenLocalModel?.invoke() } }
        item { SettingsNavCard("Auto generation", "Automatic flashcards & questions from observations", FieldMindIcons.Sparkle, FieldMindTheme.colors.flashcard) { onOpenAutoGen?.invoke() } }

        // ╔════════════════════════════════════════════╗
        // ║  STORAGE & SECURITY                        ║
        // ╚════════════════════════════════════════════╝
        item { SectionHeader("Storage & security", "Backup, data health, and privacy") }
        item { SettingsNavCard("Backup & Restore", "Export, import, auto-backup, folder picker, encryption", FieldMindIcons.Archive, FieldMindTheme.colors.data) { onOpenBackup?.invoke() } }
        item { SettingsNavCard("Security", "App lock, PIN, biometrics, privacy typing, screen protection", FieldMindIcons.Lock, FieldMindTheme.colors.confidenceVerify) { onOpenSecurity?.invoke() } }
        item { SettingsNavCard("Data integrity", "Orphaned records, database health checks", FieldMindIcons.Archive, FieldMindTheme.colors.hypothesis) { onOpenDataIntegrity?.invoke() } }

        // ╔════════════════════════════════════════════╗
        // ║  ABOUT & ADVANCED                          ║
        // ╚════════════════════════════════════════════╝
        item { SectionHeader("Updates", "New releases from GitHub and bug reporting") }
        item {
            val attachCrashLog by viewModel?.fieldSettings?.bugReportsAttachCrashLog?.collectAsState() ?: remember { mutableStateOf(true) }
            SettingsGroupCard {
                ToggleItem(
                    "Auto-attach crash log to bug reports",
                    "When you file a bug from the app, the most recent entry from your local crash log is sanitized and appended to the report.",
                    attachCrashLog,
                    { viewModel?.fieldSettings?.setBugReportsAttachCrashLog(it) },
                    MaterialSymbolIcon("description")
                )
            }
        }
        item { SettingsNavCard("Check for updates", "Manually check for the latest release on GitHub", FieldMindIcons.Info, FieldMindTheme.colors.info) { onOpenCheckForUpdates?.invoke() } }
        item { SettingsNavCard("Report a bug", "File feedback directly to the project's GitHub issues", MaterialSymbolIcon("bug_report"), MaterialTheme.colorScheme.error) { onOpenBugReport?.invoke() } }

        item { SectionHeader("About & advanced", "Developer tools, changelog, and app info") }
        item { SettingsNavCard("What’s new", "FieldMind redesign notes and migration changes", FieldMindIcons.Info, FieldMindTheme.colors.info) { onOpenChangelog?.invoke() } }
        item { SettingsNavCard("About", "Credits, acknowledgements, and version", FieldMindIcons.Info, FieldMindTheme.colors.source) { onOpenAbout?.invoke() } }
        item { SettingsNavCard("Developer options", "Debug tools, logging, performance stats, and test data", MaterialSymbolIcon("tune"), FieldMindTheme.colors.hypothesis) { onOpenDeveloper?.invoke() } }

        item {
            OutlinedButton(onClick = onResetOnboarding, modifier = Modifier.fillMaxWidth(), shape = CuteCardDefaults.ShapeCompact) {
                Text("Reset onboarding")
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SettingsNavCard(title: String, subtitle: String, icon: MaterialSymbolIcon, color: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    val context = LocalContext.current
    val gradientSettings = remember {  FieldMindSettings.getInstance(context) }
    val gradientStyleName by gradientSettings.cardGradientStyle.collectAsState()
    val gradientStyle = remember(gradientStyleName) { CuteGradients.fromString(gradientStyleName) }
    val gradientOpacity by gradientSettings.gradientOpacity.collectAsState()
    val gradient = CuteGradients.brushFor(gradientStyle, opacity = gradientOpacity)
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).pressScale(scaleDown = 0.97f),
        shape = CuteCardDefaults.Shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = gradient, shape = CuteCardDefaults.Shape)
        ) {
            Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(44.dp).clip(CuteCardDefaults.ButtonShape).background(color.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                    Icon(icon = icon, contentDescription = null, tint = color, size = 24.dp)
                }
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(icon = FieldMindIcons.Forward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 20.dp)
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Profile Settings Page
// ══════════════════════════════════════════════════════════════════════

@Composable
fun ProfileSettingsPage(viewModel: FieldMindViewModel, onBack: () -> Unit) {
    val settings = viewModel.fieldSettings
    val profileName by settings.profileName.collectAsState()
    val profileRole by settings.profileRole.collectAsState()
    val profileFocus by settings.profileFocus.collectAsState()
    val journalEnabled by settings.journalEnabled.collectAsState()
    val onboardingFrequency by settings.onboardingFrequency.collectAsState()

    SettingsSubPage("Research profile", icon = FieldMindIcons.Nature, onBack = onBack) {
        item {
            SettingsGroupCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("FieldMind has no app server: profile, observations, sources, and local model settings are stored on this device unless you export or share them.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(value = profileName, onValueChange = settings::setProfileName, label = { Text("Display name") }, modifier = Modifier.fillMaxWidth(), shape = CuteCardDefaults.FieldShape, singleLine = true, keyboardOptions = KeyboardOptions.Default.withPrivacyTyping(LocalPrivacyTypingEnabled.current),
                                        trailingIcon = {
                                            if (LocalPrivacyTypingEnabled.current) {
                                                PrivacyTypingIndicator()
                                            }
                                        }
                                    )
                    Text("Role", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OptionPickerField(label = "Role", selected = profileRole, options = listOf("Field learner", "Student", "Naturalist", "Researcher"), onSelected = { settings.setProfileRole(it) }, icon = FieldMindIcons.User)
                    Text("Focus", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OptionPickerField(label = "Focus", selected = profileFocus, options = listOf("Wildlife & ecology", "Plants & botany", "Weather", "Water", "Geology", "General science"), onSelected = { settings.setProfileFocus(it) }, icon = FieldMindIcons.Category)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Text("How often do you go out?", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Daily", "A few times a week", "Weekends", "Spontaneously").forEach { freq ->
                            val isSelected = onboardingFrequency == freq
                            Surface(
                                onClick = { settings.setOnboardingFrequency(freq) },
                                shape = CuteCardDefaults.ShapeCompact,
                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .pressScale(scaleDown = 0.96f)
                            ) {
                                Text(
                                    freq,
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
        item { SectionHeader("Daily Journal", "Greeting and quick-capture overlay on open") }
        item {
            val showJournalChips by settings.journalShowCategoryChips.collectAsState()
            val journalQuickCategory by settings.journalQuickCategory.collectAsState()
            SettingsGroupCard {
                ToggleItem(
                    "Show daily journal",
                    "A time-adaptive greeting with quick capture, category chips, and streak display on first open each day.",
                    journalEnabled,
                    settings::setJournalEnabled,
                    FieldMindIcons.Article
                )
                HorizontalDivider(
                    Modifier.padding(start = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
                ToggleItem(
                    "Show category chips in overlay",
                    "Tap a chip (Bird, Plant, etc.) to pre-tag your observation before saving. The chosen chip is remembered for the next day's overlay.",
                    showJournalChips,
                    settings::setJournalShowCategoryChips,
                    FieldMindIcons.Category
                )
                if (showJournalChips) {
                    HorizontalDivider(
                        Modifier.padding(start = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                    ChoiceItemForm(
                        "Quick-capture category",
                        observationCategories,
                        journalQuickCategory,
                        FieldMindIcons.Observation,
                        settings::setJournalQuickCategory
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Appearance Settings Page
// ══════════════════════════════════════════════════════════════════════

@Composable
fun AppearanceSettingsPage(viewModel: FieldMindViewModel, onBack: () -> Unit, onOpenEntityColors: (() -> Unit)? = null) {
    val settings = viewModel.fieldSettings
    val themeMode by settings.themeMode.collectAsState()
    val dynamicColor by settings.dynamicColorEnabled.collectAsState()
    val mapType by settings.mapType.collectAsState()
    val mapShowLocation by settings.mapShowLocation.collectAsState()
    val sharedAppSettings = SharedAppSettings.getInstance(androidx.compose.ui.platform.LocalContext.current)
    val amoledTheme by sharedAppSettings.amoledTheme.collectAsState()
    val customColorScheme by sharedAppSettings.customColorScheme.collectAsState()
    val layoutStyle by settings.onboardingLayoutStyle.collectAsState()

    SettingsSubPage("Appearance", icon = FieldMindIcons.Palette, onBack = onBack) {
        // ── Home Layout section ──
        item { SectionHeader("Home Layout", "Choose how your Home screen looks") }
        item {
            SettingsGroupCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Layout style", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("How your Home screen is arranged after the daily journal.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("Simple", "Guided journal", "Data-focused").forEach { style ->
                            val sel = layoutStyle == style
                            Surface(
                                onClick = { settings.setOnboardingLayoutStyle(style) },
                                shape = RoundedCornerShape(26.dp),
                                color = if (sel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = if (sel) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(84.dp)
                                    .pressScale(scaleDown = 0.96f)
                            ) {
                                Column(
                                    Modifier.fillMaxSize().padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        when (style) {
                                            "Simple" -> FieldMindIcons.Check
                                            "Guided journal" -> FieldMindIcons.Article
                                            else -> FieldMindIcons.Data
                                        },
                                        null,
                                        tint = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        size = 24.dp
                                    )
                                    Text(
                                        style,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        // ── Theme section ──
        item { SectionHeader("Theme", "Control the look and feel of FieldMind") }
        item {
            SettingsGroupCard {
                ThemeToggle(themeMode, settings::setThemeMode)
                HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ToggleItem("AMOLED dark mode", "Pure black backgrounds in dark mode for OLED screens. Automatically switches theme to dark when enabled. Deeper blacks save battery on OLED displays.", amoledTheme, { enabled ->
                    sharedAppSettings.setAmoledTheme(enabled)
                    if (enabled) settings.setThemeMode("Dark")
                }, MaterialSymbolIcon("dark_mode"))
                HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ToggleItem("Material You dynamic color", "Use system wallpaper colors that auto-adapt to light/dark. Off keeps the FieldMind brand palette.", dynamicColor, settings::setDynamicColorEnabled, FieldMindIcons.Palette)
                HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                // ── Seasonal color shift toggle ──
                val seasonalEnabled by settings.seasonalColorsEnabled.collectAsState()
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val seasonAccent = when (java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)) {
                        java.util.Calendar.MARCH, java.util.Calendar.APRIL, java.util.Calendar.MAY -> Color(0xFF4CAF50)
                        java.util.Calendar.JUNE, java.util.Calendar.JULY, java.util.Calendar.AUGUST -> Color(0xFFFFB300)
                        java.util.Calendar.SEPTEMBER, java.util.Calendar.OCTOBER, java.util.Calendar.NOVEMBER -> Color(0xFFE65100)
                        else -> Color(0xFF42A5F5)
                    }
                    val seasonName = when (java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)) {
                        java.util.Calendar.MARCH, java.util.Calendar.APRIL, java.util.Calendar.MAY -> "Spring"
                        java.util.Calendar.JUNE, java.util.Calendar.JULY, java.util.Calendar.AUGUST -> "Summer"
                        java.util.Calendar.SEPTEMBER, java.util.Calendar.OCTOBER, java.util.Calendar.NOVEMBER -> "Autumn"
                        else -> "Winter"
                    }
                    Box(
                        Modifier.size(44.dp).clip(CuteCardDefaults.ButtonShape)
                            .background(seasonAccent.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            when (seasonName) {
                                "Spring" -> MaterialSymbolIcon("local_florist")
                                "Summer" -> MaterialSymbolIcon("wb_sunny")
                                "Autumn" -> MaterialSymbolIcon("park")
                                else -> MaterialSymbolIcon("ac_unit")
                            },
                            null,
                            tint = seasonAccent,
                            size = 22.dp
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text("Seasonal color shift", fontWeight = FontWeight.SemiBold)
                        Text(
                            "$seasonName accent — colors subtly shift with the seasons",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = seasonalEnabled,
                        onCheckedChange = { settings.setSeasonalColorsEnabled(it) }
                    )
                }
                HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Color scheme", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Choose a premium color palette. The matching gradient style auto-selects.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    ColorSchemeSwatchPicker(
                        selected = customColorScheme,
                        onSelected = { scheme ->
                            sharedAppSettings.setCustomColorScheme(scheme)
                            // Auto-select matching gradient style
                            // All color schemes use the default tint now
                            settings.setCardGradientStyle("Screen Background")
                        }
                    )
                }
            }
        }
        // ── Card Gradient section ──
        item { SectionHeader("Card Style", "Card background gradient style") }
        item {
            val gradientStyleName by settings.cardGradientStyle.collectAsState()
            val gradientOpacity by settings.gradientOpacity.collectAsState()
            SettingsGroupCard {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Card gradient", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    // Gradient preview chips
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CuteGradients.Style.entries.forEach { style ->
                            val isSelected = gradientStyleName == style.displayName
                            val previewBrush = CuteGradients.brushFor(style, opacity = gradientOpacity)
                            Surface(
                                onClick = { settings.setCardGradientStyle(style.displayName) },
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier.width(IntrinsicSize.Min)
                            ) {
                                Row(
                                    Modifier.padding(start = 4.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Gradient preview swatch
                                    Box(
                                        Modifier
                                            .size(32.dp)
                                            .clip(CuteCardDefaults.ChipShape)
                                            .background(brush = previewBrush)
                                    )
                                    Text(
                                        style.displayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1
                                    )
                                    if (isSelected) {
                                        Icon(
                                            FieldMindIcons.Check,
                                            null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            size = 16.dp
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(Modifier.height(8.dp))
                    Text("Gradient intensity", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Adjust how bold or subtle card gradients appear.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    val sliderValue = remember(gradientOpacity) { mutableFloatStateOf(gradientOpacity) }
                    Slider(
                        value = sliderValue.floatValue,
                        onValueChange = { sliderValue.floatValue = it },
                        onValueChangeFinished = { settings.setGradientOpacity(sliderValue.floatValue) },
                        valueRange = 0.1f..1.0f,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtle", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Bold", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }


        // ── Entity accent colors ──
        item { SectionHeader("Entity Colors", "Per-category accent color customization") }
        item {
            SettingsNavCard(
                "Entity accent colors",
                "Customize colors for observations, notes, tasks, questions, and more",
                MaterialSymbolIcon("palette"),
                FieldMindTheme.colors.flashcard
            ) { onOpenEntityColors?.invoke() }
        }

        item { SectionHeader("Map", "Map type and location display preferences") }
        item {
            SettingsGroupCard {
                ChoiceItemForm("Default map type", listOf("Standard", "Satellite", "Terrain"), mapType, FieldMindIcons.Map, settings::setMapType)
                HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ToggleItem("Show my location", "Display your current position as a marker on the map.", mapShowLocation, settings::setMapShowLocation, FieldMindIcons.Location)
            }
        }
        item {
            Card(shape = CuteCardDefaults.Shape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Map data", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Text("FieldMind uses OpenStreetMap tiles for map rendering. No map data is sent to any server beyond the tile request.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun ThemeToggle(current: String, onSet: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon = FieldMindIcons.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary, size = 22.dp)
        Column(Modifier.weight(1f)) {
            Text("Theme", fontWeight = FontWeight.SemiBold)
            OptionPickerField(label = "Theme", selected = current, options = listOf("System", "Light", "Dark"), onSelected = { onSet(it) }, icon = FieldMindIcons.Image)
        }
    }
}

// ══════════════════════════════════════════════════════════════════════

// ══════════════════════════════════════════════════════════════════════
//  Capture Defaults Settings Page
// ══════════════════════════════════════════════════════════════════════

@Composable
fun CaptureDefaultsSettingsPage(viewModel: FieldMindViewModel, onBack: () -> Unit) {
    val settings = viewModel.fieldSettings
    val goal by settings.dailyObservationGoal.collectAsState()
    val category by settings.defaultCategory.collectAsState()
    val confidence by settings.defaultConfidence.collectAsState()
    val locationMode by settings.locationMode.collectAsState()
    val media by settings.mediaAttachmentsEnabled.collectAsState()
    val audio by settings.audioRecordingEnabled.collectAsState()
    val exportMode by settings.attachmentExportMode.collectAsState()
    val reminders by settings.remindersEnabled.collectAsState()
    val weatherAlerts by settings.weatherAlertsEnabled.collectAsState()
    val taskReminders by settings.taskRemindersEnabled.collectAsState()
    val sessionReminders by settings.sessionRemindersEnabled.collectAsState()
    val streaks by settings.streaksEnabled.collectAsState()

    SettingsSubPage("Capture defaults", icon = FieldMindIcons.Capture, onBack = onBack) {
        item {
            SettingsGroupCard {
                StepperItem("Daily observation goal", "Drives the Today dashboard and progress ring.", goal, FieldMindIcons.Today) { settings.setDailyObservationGoal(it) }
            }
        }
        item {
            SettingsGroupCard {
                ChoiceItemForm("Default category", observationCategories, category, FieldMindIcons.Observation, settings::setDefaultCategory)
                HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ChoiceItemForm("Default confidence", confidenceOptions, confidence, FieldMindIcons.Check, settings::setDefaultConfidence)
                HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ChoiceItemForm("Location mode", listOf("Manual only", "Approximate", "Precise"), locationMode, FieldMindIcons.Location, settings::setLocationMode)
            }
        }
        item {
            SettingsGroupCard {
                ToggleItem("Media attachments", "Enable camera, gallery, and file evidence tools.", media, settings::setMediaAttachmentsEnabled, FieldMindIcons.Camera)
                HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ToggleItem("Audio recording", "Enable voice-note evidence capture.", audio, settings::setAudioRecordingEnabled, FieldMindIcons.Mic)
                HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ChoiceItemForm("Attachment export", listOf("Reference URIs", "Copy media later", "Skip media"), exportMode, FieldMindIcons.Export, settings::setAttachmentExportMode)
            }
        }
        item {
            SettingsGroupCard {
                ToggleItem("Daily reminders", "Schedules a daily prompt and skips after logging today's observation.", reminders, settings::setRemindersEnabled, FieldMindIcons.Notifications)
                HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ToggleItem("Weather alerts", "Alerts for storms, heavy rain, snow, and extreme heat at your location.", weatherAlerts, settings::setWeatherAlertsEnabled, FieldMindIcons.Weather)
                HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ToggleItem("Task reminders", "Reminders for overdue and due-soon observation tasks.", taskReminders, settings::setTaskRemindersEnabled, FieldMindIcons.Check)
                HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ToggleItem("Session reminders", "Daily prompts to start a research session based on your schedule.", sessionReminders, settings::setSessionRemindersEnabled, FieldMindIcons.Timer)
                HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ToggleItem("Streaks", "Shows consecutive observation days on the Today dashboard.", streaks, settings::setStreaksEnabled, FieldMindIcons.Streak)
            }
        }

    }
}

// ══════════════════════════════════════════════════════════════════════
//  Notifications Settings Page
// ══════════════════════════════════════════════════════════════════════

@Composable
fun NotificationsSettingsPage(viewModel: FieldMindViewModel, onBack: () -> Unit) {
    val settings = viewModel.fieldSettings
    val reminders by settings.remindersEnabled.collectAsState()
    val weatherAlerts by settings.weatherAlertsEnabled.collectAsState()
    val taskReminders by settings.taskRemindersEnabled.collectAsState()
    val sessionReminders by settings.sessionRemindersEnabled.collectAsState()

    SettingsSubPage("Notifications", icon = FieldMindIcons.Notifications, onBack = onBack) {
        item { SectionHeader("Background jobs", "Enable or disable each WorkManager-backed notification job independently.") }
        item {
            SettingsGroupCard {
                ToggleItem("Daily reminders", "Schedules a daily prompt and skips after logging today's observation.", reminders, settings::setRemindersEnabled, FieldMindIcons.Notifications)
                HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ToggleItem("Weather alerts", "Periodically checks conditions and alerts on storms, heavy rain, snow, and extreme heat.", weatherAlerts, settings::setWeatherAlertsEnabled, FieldMindIcons.Weather)
                HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ToggleItem("Task reminders", "Checks for overdue, due-today, and due-soon tasks and sends notifications.", taskReminders, settings::setTaskRemindersEnabled, FieldMindIcons.Check)
                HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ToggleItem("Session reminders", "Daily prompts to start a research session based on time of day.", sessionReminders, settings::setSessionRemindersEnabled, FieldMindIcons.Timer)
            }
        }
        item {
            Card(shape = CuteCardDefaults.Shape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("How it works", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Text("Each notification type runs as a separate WorkManager periodic job. Toggling a notification off cancels its recurring schedule. Data is checked locally — no network calls are made for notifications.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  AI Assistant Settings Page
// ══════════════════════════════════════════════════════════════════════

@Composable
fun AiAssistantSettingsPage(viewModel: FieldMindViewModel, onBack: () -> Unit) {
    val settings = viewModel.fieldSettings
    val ai by settings.geminiEnabled.collectAsState()
    val provider by settings.aiProvider.collectAsState()
    val key by settings.geminiApiKey.collectAsState()
    val model by settings.geminiModel.collectAsState()
    val openAiKey by settings.openAiApiKey.collectAsState()
    val openAiModel by settings.openAiModel.collectAsState()
    val confirm by settings.aiRequireConfirmBeforeSave.collectAsState()
    val sendAttachments by settings.aiSendAttachments.collectAsState()

    SettingsSubPage("AI assistant", icon = FieldMindIcons.Sparkle, onBack = onBack) {
        item {
            SettingsGroupCard {
                ToggleItem("Enable AI assistant", "Review factuality, suggest papers, and answer questions.", ai, settings::setGeminiEnabled, FieldMindIcons.Sparkle)
            }
        }
        if (ai) {
            item {
                SettingsGroupCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ChoiceItemForm("Provider", listOf("Gemini", "OpenAI"), provider, FieldMindIcons.Sparkle, settings::setAiProvider)

                        if (provider == "OpenAI") {
                            OutlinedTextField(value = openAiKey, onValueChange = settings::setOpenAiApiKey, label = { Text("OpenAI API key") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = CuteCardDefaults.FieldShape, singleLine = true, supportingText = { Text(if (openAiKey.isBlank()) "No OpenAI key saved." else "OpenAI key saved locally.") }, keyboardOptions = KeyboardOptions.Default.withPrivacyTyping(LocalPrivacyTypingEnabled.current),
                                                trailingIcon = {
                                                    if (LocalPrivacyTypingEnabled.current) {
                                                        PrivacyTypingIndicator()
                                                    }
                                                }
                                            )
                            Text("Model", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            OptionPickerField(label = "OpenAI model", selected = openAiModel, options = listOf("gpt-4.1-mini", "gpt-4.1", "gpt-4o-mini"), onSelected = { settings.setOpenAiModel(it) }, icon = FieldMindIcons.Bolt)
                        } else {
                            OutlinedTextField(value = key, onValueChange = settings::setGeminiApiKey, label = { Text("Gemini API key") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = CuteCardDefaults.FieldShape, singleLine = true, supportingText = { Text(if (key.isBlank()) "No key saved — get one at aistudio.google.com." else "Key saved locally.") }, keyboardOptions = KeyboardOptions.Default.withPrivacyTyping(LocalPrivacyTypingEnabled.current),
                                                trailingIcon = {
                                                    if (LocalPrivacyTypingEnabled.current) {
                                                        PrivacyTypingIndicator()
                                                    }
                                                }
                                            )
                            Text("Model", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            OptionPickerField(label = "Gemini model", selected = model, options = listOf("gemini-1.5-flash", "gemini-1.5-pro", "gemini-2.0-flash"), onSelected = { settings.setGeminiModel(it) }, icon = FieldMindIcons.Bolt)
                        }
                    }
                }
            }
            item {
                SettingsGroupCard {
                    ToggleItem("Confirm before saving AI output", "AI suggestions stay as previews unless you apply them.", confirm, settings::setAiRequireConfirmBeforeSave, FieldMindIcons.Check)
                    HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ToggleItem("Allow attachment context", "Off by default to protect field evidence privacy.", sendAttachments, settings::setAiSendAttachments, FieldMindIcons.File)
                }
            }
        }
        item {
            Card(shape = CuteCardDefaults.Shape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Privacy note", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Text("Nothing is sent to any AI provider without an explicit action. Your API key is stored only on this device.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Local Model Settings Page
// ══════════════════════════════════════════════════════════════════════

@Composable
fun LocalModelSettingsPage(viewModel: FieldMindViewModel, onBack: () -> Unit) {
    val settings = viewModel.fieldSettings
    val localModelEnabled by settings.localModelEnabled.collectAsState()
    val localModelOption by settings.localModelOption.collectAsState()
    val localModelUseForStudy by settings.localModelUseForStudy.collectAsState()

    SettingsSubPage("Study profiles", icon = FieldMindIcons.Sparkle, onBack = onBack) {
        item {
            Card(shape = CuteCardDefaults.Shape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.size(36.dp).clip(CuteCardDefaults.ChipShape).background(FieldMindTheme.colors.flashcard.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                            Icon(FieldMindIcons.Sparkle, null, tint = FieldMindTheme.colors.flashcard, size = 18.dp)
                        }
                        Text("On-device study generation", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        "This is not a web download — FieldMind generates study content and flashcards directly on your device using your existing observations and sources. The \"profile\" you select controls how much detail the on-device generator aims for.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "No internet connection, no external server, no model files to download. Everything stays on your device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        item {
            SettingsGroupCard {
                ToggleItem("Enable study generator", "Generates flashcards and review content from your observations and sources using on-device logic.", localModelEnabled, settings::setLocalModelEnabled, FieldMindIcons.Sparkle)
                if (localModelEnabled) {
                    HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    val profiles = listOf("FieldLite — Quick cards", "FieldCore — Balanced detail", "FieldPro — Deep study")
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Generation profile", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        profiles.forEach { profile ->
                            val selected = localModelOption == profile
                            Surface(
                                onClick = { settings.setLocalModelOption(profile) },
                                shape = CuteCardDefaults.ButtonShape,
                                color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
                            ) {
                                Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(profile, fontWeight = FontWeight.SemiBold)
                                        val spec = when (profile) {
                                            "FieldLite — Quick cards" -> "Fast, concise flashcard generation"
                                            "FieldCore — Balanced detail" -> "Balanced speed and depth"
                                            else -> "Most detailed, thorough study content"
                                        }
                                        Text(spec, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (selected) Icon(FieldMindIcons.Check, null, tint = MaterialTheme.colorScheme.primary, size = 20.dp)
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                    HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ToggleItem("Use for flashcards/reviews", "Use generated content for flashcard review sessions.", localModelUseForStudy, settings::setLocalModelUseForStudy, FieldMindIcons.Flashcard)
                }
            }
        }
        item {
            Card(shape = CuteCardDefaults.Shape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("How it works", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Text(
                        "FieldMind's study generator creates flashcards by scanning your saved observations, sources, and notes for key concepts, terms, and facts. It then builds question-answer pairs — all on your device, with no data leaving your phone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  SettingsSubPage Helper — Scaffold for settings sub-pages
// ══════════════════════════════════════════════════════════════════════

@Composable
internal fun SettingsSubPage(
    title: String,
    icon: MaterialSymbolIcon,
    onBack: () -> Unit,
    headerAction: @Composable (() -> Unit)? = null,
    content: LazyListScope.() -> Unit
) {
    // Hardware/gesture back button support
    BackHandler(enabled = true) { onBack() }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(20.dp, 12.dp, 20.dp, 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            StandardScreenHeader(
                title = title,
                icon = icon,
                trailing = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        headerAction?.invoke()
                        BackButton(onClick = onBack)
                    }
                }
            )
        }
        content()
    }
}
// ══════════════════════════════════════════════════════════════════════
//  Security Settings Page (ALL settings inline, no sub-pages)
// ══════════════════════════════════════════════════════════════════════

@Composable
fun SecuritySettingsPage(
    viewModel: FieldMindViewModel,
    onBack: () -> Unit,
    onOpenSecurityScore: (() -> Unit)? = null
) {
    val settings = viewModel.fieldSettings
    // ── Lock settings ──
    val privacy by settings.privacyLockEnabled.collectAsState()
    val privacyTyping by settings.privacyTypingEnabled.collectAsState()
    val lockTimeout by settings.lockTimeout.collectAsState()
    val autoLockOnBackground by settings.autoLockOnBackground.collectAsState()
    val appPinEnabled by settings.appPinEnabled.collectAsState()
    val appPinHash by settings.appPinHash.collectAsState()
    val screenCapture by settings.screenCaptureProtectionEnabled.collectAsState()
    val clipboardCleanup by settings.clipboardAutoCleanupEnabled.collectAsState()
    val backupEncryption by settings.autoBackupEnabled.collectAsState()
    // ── New inline settings ──
    val appPinLen by settings.appPinLength.collectAsState()
    val decoyEnabled by settings.decoyPinEnabled.collectAsState()
    val decoyLabel by settings.decoyPinLabel.collectAsState()
    val failedCooldown by settings.failedUnlockCooldown.collectAsState()
    val failedBiometrics by settings.failedUnlockRequireBiometrics.collectAsState()
    val failedPanic by settings.failedUnlockPanicLock.collectAsState()
    val exportPassEnabled by settings.exportPasswordProtectionEnabled.collectAsState()
    val exportEncLevel by settings.exportEncryptionLevel.collectAsState()
    val previewMode by settings.appPreviewMode.collectAsState()
    val metaGps by settings.metadataRemoveGps.collectAsState()
    val metaCamera by settings.metadataRemoveCamera.collectAsState()
    val metaDevice by settings.metadataRemoveDevice.collectAsState()
    val metaExif by settings.metadataRemoveExif.collectAsState()

    // ── PIN setup state ──
    var showPinSetup by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var pinConfirm by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }
    var showCurrentPinDialog by remember { mutableStateOf(false) }
    var currentPinInput by remember { mutableStateOf("") }
    var currentPinError by remember { mutableStateOf(false) }

    // ── Export password state ──
    var showExportPassDialog by remember { mutableStateOf(false) }
    var exportPassInput by remember { mutableStateOf("") }
    var exportPassConfirm by remember { mutableStateOf("") }
    var exportPassError by remember { mutableStateOf(false) }

    // ── Decoy PIN setup state ──
    var showDecoyDialog by remember { mutableStateOf(false) }
    var decoyInput by remember { mutableStateOf("") }
    var decoyConfirm by remember { mutableStateOf("") }
    var decoyLabelInput by remember { mutableStateOf("") }
    var decoyError by remember { mutableStateOf(false) }

    val appLockActive = privacy || (appPinEnabled && appPinHash.isNotBlank())
    val enabledCount = listOfNotNull(appLockActive, backupEncryption, clipboardCleanup, privacyTyping, screenCapture).count { it }

    SettingsSubPage("Privacy & Security", icon = FieldMindIcons.Lock, onBack = onBack) {
        // ── Security Status ──
        item {
            Card(shape = CuteCardDefaults.Shape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column { Text("Security Status", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold); Text("$enabledCount of 5 protections enabled", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { listOf(appLockActive, backupEncryption, clipboardCleanup, privacyTyping, screenCapture).forEach { active -> Box(Modifier.size(10.dp).clip(CircleShape).background(if (active) FieldMindTheme.colors.positive else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))) } }
                    }
                    LinearProgressIndicator(progress = { enabledCount / 5f }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(CuteCardDefaults.ProgressBarShape), color = FieldMindTheme.colors.positive, trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Surface(onClick = { onOpenSecurityScore?.invoke() }, shape = CuteCardDefaults.ShapeCompact, color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), modifier = Modifier
                        .fillMaxWidth()
                        .pressScale(scaleDown = 0.97f)
                        .cuteShadow(
                            elevation = CuteElevations.nonClickableTier,
                            shape = CuteCardDefaults.ShapeCompact
                        )
                    ) {
                        Row(Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(MaterialSymbolIcon("security"), null, tint = MaterialTheme.colorScheme.primary, size = 16.dp)
                            Text("View full security score", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.weight(1f)); Icon(FieldMindIcons.Forward, null, tint = MaterialTheme.colorScheme.primary, size = 14.dp)
                        }
                    }
                }
            }
        }

        // ── Card 1: Quick Protection (4 settings: Device lock, App PIN + length + decoy, Screenshot) ──
        item { SectionHeader("Quick Protection", "App lock, PIN, and screen security") }
        item {
            SettingsGroupCard {
                ToggleItem("Device biometric lock", "Fingerprint, face, or device PIN", privacy, settings::setPrivacyLockEnabled, FieldMindIcons.Lock)

                HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(44.dp).clip(CuteCardDefaults.ButtonShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) { Icon(FieldMindIcons.Lock, null, tint = MaterialTheme.colorScheme.primary, size = 22.dp) }
                    Column(Modifier.weight(1f)) { Text("App PIN lock", fontWeight = FontWeight.SemiBold); Text("Self-contained 4-6 digit PIN", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    Switch(checked = appPinEnabled && appPinHash.isNotBlank(), onCheckedChange = { enabled -> if (enabled) showPinSetup = true else showCurrentPinDialog = true })
                }

                // PIN Length (shown when PIN is active)
                if (appPinEnabled && appPinHash.isNotBlank()) {
                    HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("PIN Length", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("4 digits", "5 digits", "6 digits").forEach { option ->
                                val selected = appPinLen == option
                                Surface(
                                    onClick = { settings.setAppPinLength(option) },
                                    shape = CuteCardDefaults.ButtonShape,
                                    color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                                    modifier = Modifier
                                        .weight(1f)
                                        .pressScale(scaleDown = 0.95f)
                                ) {
                                    Row(Modifier.padding(vertical = 12.dp, horizontal = 10.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(option.take(1), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(option.split(" ")[1], style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }

                // PIN setup form
                if (showPinSetup) {
                    HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(if (appPinHash.isNotBlank()) "Change PIN" else "Set a PIN", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                        // PIN Length selector — shown upfront so user chooses length BEFORE entering PIN
                        Text("PIN Length", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("4 digits", "5 digits", "6 digits").forEach { option ->
                                val selected = appPinLen == option
                                Surface(
                                    onClick = { settings.setAppPinLength(option) },
                                    shape = CuteCardDefaults.ButtonShape,
                                    color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                                    modifier = Modifier
                                        .weight(1f)
                                        .pressScale(scaleDown = 0.95f)
                                ) {
                                    Row(Modifier.padding(vertical = 12.dp, horizontal = 10.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(option.take(1), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(option.split(" ")[1], style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        val setupMinPinLen = when (appPinLen) { "5 digits" -> 5; "6 digits" -> 6; else -> 4 }
                        OutlinedTextField(value = pinInput, onValueChange = { if (it.length <= setupMinPinLen) { pinInput = it; pinError = false } }, label = { Text("Enter PIN") }, singleLine = true, isError = pinError, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword).withPrivacyTyping(LocalPrivacyTypingEnabled.current), trailingIcon = { if (LocalPrivacyTypingEnabled.current) PrivacyTypingIndicator() }, modifier = Modifier.fillMaxWidth(), shape = CuteCardDefaults.FieldShape, textStyle = MaterialTheme.typography.headlineSmall.copy(letterSpacing = 8.sp, textAlign = TextAlign.Center))
                        OutlinedTextField(value = pinConfirm, onValueChange = { if (it.length <= setupMinPinLen) { pinConfirm = it; pinError = false } }, label = { Text("Confirm PIN") }, singleLine = true, isError = pinError, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword).withPrivacyTyping(LocalPrivacyTypingEnabled.current), trailingIcon = { if (LocalPrivacyTypingEnabled.current) PrivacyTypingIndicator() }, modifier = Modifier.fillMaxWidth(), shape = CuteCardDefaults.FieldShape, textStyle = MaterialTheme.typography.headlineSmall.copy(letterSpacing = 8.sp, textAlign = TextAlign.Center))
                        if (pinError) Text("PINs don't match. Try again.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(onClick = { showPinSetup = false; pinInput = ""; pinConfirm = ""; pinError = false }, modifier = Modifier.weight(1f), shape = CuteCardDefaults.ButtonShape) { Text("Cancel") }
                            val minPinLen = setupMinPinLen
                            Button(onClick = { if (pinInput.length == minPinLen && pinInput == pinConfirm) { val hash = settings.hashAppPin(pinInput); settings.setAppPinHash(hash); settings.setAppPinEnabled(true); showPinSetup = false; pinInput = ""; pinConfirm = ""; pinError = false } else pinError = true }, enabled = pinInput.length == minPinLen && pinConfirm.length == minPinLen, modifier = Modifier.weight(1f), shape = CuteCardDefaults.ButtonShape) { Text("Save PIN") }
                        }
                    }
                }

                // Decoy PIN (shown when PIN is active)
                if (appPinEnabled && appPinHash.isNotBlank()) {
                    HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) { Icon(MaterialSymbolIcon("lock_open"), null, tint = MaterialTheme.colorScheme.tertiary, size = 22.dp) }
                        Column(Modifier.weight(1f)) { Text("Decoy PIN", fontWeight = FontWeight.SemiBold); val decoyStatus = if (decoyEnabled && settings.decoyPinHash.value.isNotBlank()) "Active — ${decoyLabel.ifBlank { "Opens clean app" }}" else "Off"; Text(decoyStatus, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        Switch(checked = decoyEnabled && settings.decoyPinHash.value.isNotBlank(), onCheckedChange = { enabled -> if (enabled) { showDecoyDialog = true; decoyLabelInput = decoyLabel } else { settings.setDecoyPinEnabled(false); settings.setDecoyPinHash(""); settings.setDecoyPinLabel("") } })
                    }
                }

                HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ToggleItem("Screenshot block", "Prevent screenshots and recordings in app switcher", screenCapture, settings::setScreenCaptureProtectionEnabled, MaterialSymbolIcon("no_photography"))
            }
        }

        // ── Card 2: Lock Behavior (4 settings: Auto Lock, Background lock, Failed unlock × 3 grouped) ──
        item { SectionHeader("Lock Behavior", "Timeout, background lock, and failed attempt handling") }
        item {
            SettingsGroupCard {
                ChoiceItemForm("Auto Lock", listOf("Immediate", "1 minute", "5 minutes", "15 minutes"), lockTimeout, FieldMindIcons.Timer, settings::setLockTimeout)
                HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ToggleItem("Lock on background", "Lock when app goes to background", autoLockOnBackground, settings::setAutoLockOnBackground, FieldMindIcons.Lock)

                // Failed unlock section
                HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("After 5 failed attempts", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    listOf("Do Nothing", "30 Second Cooldown", "5 Minute Cooldown").forEach { option ->
                        val selected = failedCooldown == option
                        Surface(
                            onClick = { settings.setFailedUnlockCooldown(option) },
                            shape = MaterialTheme.shapes.medium,
                            color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = if (selected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = selected, onClick = { settings.setFailedUnlockCooldown(option) })
                                Text(option, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    ToggleItem("Require biometrics after failure", "After failed attempts, enforce fingerprint/face unlock", failedBiometrics, settings::setFailedUnlockRequireBiometrics, MaterialSymbolIcon("fingerprint"))
                    ToggleItem("Panic lock after failure", "Wipe sensitive data after repeated failed attempts", failedPanic, settings::setFailedUnlockPanicLock, MaterialSymbolIcon("warning"))
                }
            }
        }

        // ── Card 3: Export & Data Protection (3 settings: Encrypted backups, Export password, Encryption level) ──
        item { SectionHeader("Export & Data Protection", "Backup encryption, password, and export security") }
        item {
            SettingsGroupCard {
                ToggleItem("Encrypted backups", "Encrypt backup archives with strong AES-256", backupEncryption, settings::setAutoBackupEnabled, MaterialSymbolIcon("enhanced_encryption"))

                HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).clip(MaterialTheme.shapes.medium).background(FieldMindTheme.colors.data.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) { Icon(FieldMindIcons.Export, null, tint = FieldMindTheme.colors.data, size = 22.dp) }
                    Column(Modifier.weight(1f)) { Text("Password protect exports", fontWeight = FontWeight.SemiBold); Text(if (exportPassEnabled && settings.exportPasswordHash.value.isNotBlank()) "Password set ✓" else "Off", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    Switch(checked = exportPassEnabled, onCheckedChange = { enabled -> if (enabled && settings.exportPasswordHash.value.isBlank()) { showExportPassDialog = true } else { settings.setExportPasswordProtectionEnabled(enabled); if (!enabled) { settings.setExportPasswordHash("") } } })
                }

                // Export password setup button (shown when enabled)
                if (exportPassEnabled) {
                    HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Surface(
                        onClick = { showExportPassDialog = true; exportPassInput = ""; exportPassConfirm = ""; exportPassError = false },
                        shape = CuteCardDefaults.ButtonShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(MaterialSymbolIcon("password"), null, tint = FieldMindTheme.colors.hypothesis, size = 20.dp)
                            Column(Modifier.weight(1f)) {
                                Text(if (settings.exportPasswordHash.value.isNotBlank()) "Change export password" else "Set export password", fontWeight = FontWeight.SemiBold)
                                Text(if (settings.exportPasswordHash.value.isNotBlank()) "••••••••••" else "No password set", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(FieldMindIcons.Forward, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 18.dp)
                        }
                    }
                }

                HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ChoiceItemForm("Encryption Level", listOf("Standard", "Strong", "Maximum"), exportEncLevel, FieldMindIcons.Lock, settings::setExportEncryptionLevel)
            }
        }

        // ── Card 4: Metadata & Privacy (8 settings: GPS, Camera, Device, EXIF, App Preview, Privacy keyboard, Clipboard, GPS privacy) ──
        item { SectionHeader("Metadata & Privacy", "Strip data from exports, screen preview, and keyboard protection") }
        item {
            SettingsGroupCard {
                // Metadata removal
                ToggleItem("Remove GPS coordinates", "Strip location data from exported files", metaGps, settings::setMetadataRemoveGps, MaterialSymbolIcon("location_off"))
                HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ToggleItem("Remove camera info", "Strip camera model, make, and settings", metaCamera, settings::setMetadataRemoveCamera, MaterialSymbolIcon("camera_alt"))
                HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ToggleItem("Remove device info", "Strip device name and identifiers", metaDevice, settings::setMetadataRemoveDevice, MaterialSymbolIcon("smartphone"))
                HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ToggleItem("Remove EXIF data", "Strip all EXIF metadata from images", metaExif, settings::setMetadataRemoveExif, MaterialSymbolIcon("image"))

                HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                // App Preview inline
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("App Preview in recent apps", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    listOf("Normal", "Blur Content", "Privacy Screen").forEach { mode ->
                        val selected = previewMode == mode
                        Surface(
                            onClick = { settings.setAppPreviewMode(mode) },
                            shape = MaterialTheme.shapes.medium,
                            color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = if (selected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = selected, onClick = { settings.setAppPreviewMode(mode) })
                                Text(mode, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ToggleItem("Best-effort privacy keyboard", "Ask supported keyboards not to learn from input", privacyTyping, settings::setPrivacyTypingEnabled, FieldMindIcons.Lock)
                HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ToggleItem("Auto clear clipboard", "Clear sensitive copied data automatically", clipboardCleanup, settings::setClipboardAutoCleanupEnabled, MaterialSymbolIcon("content_copy"))
            }
        }

        // ── Info card ──
        item {
            Card(shape = CuteCardDefaults.Shape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Your data stays on this device", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Text("FieldMind stores everything locally. No data is sent to any server unless you explicitly export or share it.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    // ── Confirm current PIN before disabling ──
    if (showCurrentPinDialog) {
        val minPinLen = when (appPinLen) { "5 digits" -> 5; "6 digits" -> 6; else -> 4 }
        SwipeableAlertDialog(
            onDismissRequest = { showCurrentPinDialog = false; currentPinInput = ""; currentPinError = false },
            icon = { Icon(FieldMindIcons.Lock, null, size = 28.dp) },
            title = { Text("Enter current PIN", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Enter your current PIN to disable the app PIN lock.")
                    OutlinedTextField(value = currentPinInput, onValueChange = { if (it.length <= minPinLen) { currentPinInput = it; currentPinError = false } }, label = { Text("Current PIN") }, singleLine = true, isError = currentPinError, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword).withPrivacyTyping(LocalPrivacyTypingEnabled.current), trailingIcon = { if (LocalPrivacyTypingEnabled.current) PrivacyTypingIndicator() }, modifier = Modifier.fillMaxWidth(), shape = CuteCardDefaults.FieldShape, textStyle = MaterialTheme.typography.headlineSmall.copy(letterSpacing = 8.sp, textAlign = TextAlign.Center))
                    if (currentPinError) Text("Incorrect PIN", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = { Button(onClick = { if (settings.verifyAppPin(currentPinInput)) { settings.setAppPinEnabled(false); settings.setAppPinHash(""); showCurrentPinDialog = false; currentPinInput = ""; currentPinError = false } else currentPinError = true }, enabled = currentPinInput.length >= minPinLen) { Text("Disable") } },
            dismissButton = { TextButton(onClick = { showCurrentPinDialog = false; currentPinInput = ""; currentPinError = false }) { Text("Cancel") } }
        )
    }

    // ── Export password setup dialog ──
    if (showExportPassDialog) {
        SwipeableAlertDialog(
            onDismissRequest = { showExportPassDialog = false; exportPassInput = ""; exportPassConfirm = ""; exportPassError = false },
            icon = { Icon(FieldMindIcons.Lock, null, size = 28.dp) },
            title = { Text("Set export password", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Enter a password to protect exported files.")
                    OutlinedTextField(value = exportPassInput, onValueChange = { if (it.length <= 32) { exportPassInput = it; exportPassError = false } }, label = { Text("Password") }, singleLine = true, isError = exportPassError, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = CuteCardDefaults.FieldShape, textStyle = MaterialTheme.typography.titleMedium.copy(letterSpacing = 4.sp, textAlign = TextAlign.Center))
                    OutlinedTextField(value = exportPassConfirm, onValueChange = { if (it.length <= 32) { exportPassConfirm = it; exportPassError = false } }, label = { Text("Confirm password") }, singleLine = true, isError = exportPassError, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = CuteCardDefaults.FieldShape, textStyle = MaterialTheme.typography.titleMedium.copy(letterSpacing = 4.sp, textAlign = TextAlign.Center))
                    if (exportPassError) Text("Passwords don't match or too short", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = { Button(onClick = { if (exportPassInput.length >= 4 && exportPassInput == exportPassConfirm) { settings.setExportPasswordHash(settings.hashExportPassword(exportPassInput)); settings.setExportPasswordProtectionEnabled(true); showExportPassDialog = false; exportPassInput = ""; exportPassConfirm = ""; exportPassError = false } else exportPassError = true }, enabled = exportPassInput.length >= 4 && exportPassConfirm.length >= 4) { Text("Save") } },
            dismissButton = { TextButton(onClick = { showExportPassDialog = false; exportPassInput = ""; exportPassConfirm = ""; exportPassError = false }) { Text("Cancel") } }
        )
    }

    // ── Decoy PIN setup dialog ──
    if (showDecoyDialog) {
        val minPinLen = when (appPinLen) { "5 digits" -> 5; "6 digits" -> 6; else -> 4 }
        SwipeableAlertDialog(
            onDismissRequest = { showDecoyDialog = false; decoyInput = ""; decoyConfirm = ""; decoyLabelInput = ""; decoyError = false },
            icon = { Icon(MaterialSymbolIcon("lock_open"), null, size = 28.dp) },
            title = { Text("Set decoy PIN", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Enter a 4-6 digit PIN that opens a clean, empty version of FieldMind.")
                    OutlinedTextField(value = decoyInput, onValueChange = { if (it.length <= minPinLen) { decoyInput = it; decoyError = false } }, label = { Text("Decoy PIN") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = CuteCardDefaults.FieldShape, textStyle = MaterialTheme.typography.headlineSmall.copy(letterSpacing = 8.sp, textAlign = TextAlign.Center))
                    OutlinedTextField(value = decoyConfirm, onValueChange = { if (it.length <= minPinLen) { decoyConfirm = it; decoyError = false } }, label = { Text("Confirm decoy PIN") }, singleLine = true, isError = decoyError, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = CuteCardDefaults.FieldShape, textStyle = MaterialTheme.typography.headlineSmall.copy(letterSpacing = 8.sp, textAlign = TextAlign.Center))
                    OutlinedTextField(value = decoyLabelInput, onValueChange = { if (it.length <= 40) decoyLabelInput = it }, label = { Text("Label (shown when decoy is active)") }, placeholder = { Text("e.g. \"Guest mode\"") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = CuteCardDefaults.FieldShape)
                    if (decoyError) Text("PINs don't match. Try again.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = { Button(onClick = { if (decoyInput.length >= minPinLen && decoyInput == decoyConfirm) { val hash = settings.hashAppPin(decoyInput); settings.setDecoyPinHash(hash); settings.setDecoyPinLabel(decoyLabelInput.trim()); settings.setDecoyPinEnabled(true); showDecoyDialog = false; decoyInput = ""; decoyConfirm = ""; decoyLabelInput = ""; decoyError = false } else decoyError = true }, enabled = decoyInput.length >= minPinLen && decoyConfirm.length >= minPinLen) { Text("Save") } },
            dismissButton = { TextButton(onClick = { showDecoyDialog = false; decoyInput = ""; decoyConfirm = ""; decoyLabelInput = ""; decoyError = false }) { Text("Cancel") } }
        )
    }
}
@Composable
fun BackupImportSettingsPage(viewModel: FieldMindViewModel, onBack: () -> Unit, onOpenExport: () -> Unit) {
    val settings = viewModel.fieldSettings
    val autoBackupEnabled by settings.autoBackupEnabled.collectAsState()
    val autoBackupInterval by settings.autoBackupInterval.collectAsState()
    val exportFormat by settings.defaultExportFormat.collectAsState()

    SettingsSubPage("Backup & import", icon = FieldMindIcons.Archive, onBack = onBack) {
        item {
            SettingsGroupCard {
                ToggleItem("Auto backup", "Writes private archive JSON files on the selected schedule.", autoBackupEnabled, settings::setAutoBackupEnabled, FieldMindIcons.Archive)
                if (autoBackupEnabled) {
                    HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ChoiceItemForm("Backup interval", listOf("Every 6 hours", "Every 12 hours", "Daily", "Weekly", "Monthly"), autoBackupInterval, FieldMindIcons.Today, settings::setAutoBackupInterval)
                }
            }
        }
        item {
            SettingsGroupCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Export formats", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Text("Choose your preferred format for quick exports.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    ExportFormatSelector(exportFormat) { settings.setDefaultExportFormat(it) }
                }
            }
        }
        item {
            FilledTonalButton(onClick = onOpenExport, modifier = Modifier.fillMaxWidth(), shape = CuteCardDefaults.ShapeCompact) {
                Icon(FieldMindIcons.Export, null, size = 18.dp)
                Spacer(Modifier.size(8.dp))
                Text("Open Backup & Restore")
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  About Page
// ══════════════════════════════════════════════════════════════════════

@Composable
fun AboutPage(onBack: () -> Unit, onOpenChangelog: (() -> Unit)? = null) {
    val uriHandler = LocalUriHandler.current

    SettingsSubPage("About", icon = FieldMindIcons.Info, onBack = onBack) {
        item {
            Card(
                shape = CuteCardDefaults.Shape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
            ) {
                Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FieldMindLogo(
                        size = 64.dp,
                        modifier = Modifier.clip(CuteCardDefaults.Shape).background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.08f))
                    )
                    Text("FieldMind", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("Observe. Question. Research clearly.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f), textAlign = TextAlign.Center)
                    Text("A free, offline-first research notebook for curious naturalists, students, and citizen scientists.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.68f), textAlign = TextAlign.Center)
                }
            }
        }
        item { SettingsNavCard("What’s new", "See the FieldMind redesign changelog", FieldMindIcons.Info, FieldMindTheme.colors.info) { onOpenChangelog?.invoke() } }
        item {
            Card(shape = CuteCardDefaults.Shape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Built with", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    listOf(
                        "Jetpack Compose" to "Android's modern declarative UI toolkit",
                        "Material Symbols & Material 3" to "Google's icon set and design system",
                        "Room Database" to "Local-first structured data storage"
                    ).forEach { (name, desc) ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                            Icon(FieldMindIcons.Check, null, tint = MaterialTheme.colorScheme.primary, size = 16.dp, modifier = Modifier.padding(top = 2.dp))
                            Column {
                                Text(name, fontWeight = FontWeight.SemiBold)
                                Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
        item {
            Card(shape = CuteCardDefaults.Shape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Research data sources", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    listOf(
                        "Crossref" to "Free scholarly metadata API",
                        "OpenAlex" to "Open catalog of papers, authors, and venues",
                        "arXiv" to "Open-access research preprints",
                        "Open Library" to "Open, editable library catalog",
                        "Semantic Scholar" to "AI-powered research paper search"
                    ).forEach { (name, desc) ->
                        Row(Modifier.fillMaxWidth().clickable { runCatching { uriHandler.openUri("https://www.${name.lowercase().replace(" ", "")}.org") } }, horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(FieldMindIcons.OpenLink, null, tint = MaterialTheme.colorScheme.primary, size = 16.dp)
                            Column(Modifier.weight(1f)) {
                                Text(name, fontWeight = FontWeight.SemiBold)
                                Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
        item {
            Text("Made with care for people who learn by looking closely.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 4.dp))
            Spacer(Modifier.height(40.dp))
        }
    }
}

// ════════════════════════════════════════════��═════════════════════════
//  Redesigned Export Format Selector — 4-column full grid with consistent sizing
// ══════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExportFormatSelector(selected: String, onSelect: (String) -> Unit) {
    val formats = listOf(
        FormatOption("Markdown", "Readable text", FieldMindIcons.Article, FieldMindTheme.colors.source),
        FormatOption("CSV", "Tabular data", FieldMindIcons.Data, FieldMindTheme.colors.data),
        FormatOption("JSON", "Structured archive", FieldMindIcons.Archive, FieldMindTheme.colors.hypothesis),
        FormatOption("HTML", "Web layout", FieldMindIcons.Article, FieldMindTheme.colors.question),
        FormatOption("PNG", "Snapshot image", FieldMindIcons.Graph, FieldMindTheme.colors.observation),
        FormatOption("SVG", "Vector graphic", FieldMindIcons.Graph, FieldMindTheme.colors.flashcard),
        FormatOption("PDF", "Document format", FieldMindIcons.Report, FieldMindTheme.colors.report),
        FormatOption("Plain text", "Raw text", FieldMindIcons.Note, FieldMindTheme.colors.info)
    )
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        formats.forEach { format ->
            val isSelected = selected == format.name
            val itemWeight = 0.235f // ~4 items per row with spacing (100% - 3*8dp gaps / 4)
            Surface(
                onClick = { onSelect(format.name) },
                modifier = Modifier.fillMaxWidth(itemWeight),
                shape = CuteCardDefaults.ShapeCompact,
                color = if (isSelected) format.color.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                border = if (isSelected) BorderStroke(1.5.dp, format.color) else null
            ) {
                Column(
                    Modifier.padding(10.dp).heightIn(min = 72.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        Modifier.size(36.dp).clip(RoundedCornerShape(18.dp))
                            .background(if (isSelected) format.color.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceContainerLow),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(format.icon, null, tint = format.color, size = 20.dp)
                    }
                    Text(format.name, style = MaterialTheme.typography.labelSmall, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(format.desc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
                    if (isSelected) {
                        Box(
                            Modifier.size(18.dp).clip(CircleShape)
                                .background(format.color),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(FieldMindIcons.Check, null, tint = MaterialTheme.colorScheme.onPrimary, size = 12.dp)
                        }
                    }
                }
            }
        }
    }
}


// ══════════════════════════════════════════════════════════════════════
//  Observation Reading UI (Redesigned)
// ══════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ObservationReaderContent(observation: ObservationEntity, onAttachments: @Composable () -> Unit, onMap: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Header card
        Card(
            shape = CuteCardDefaults.Shape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Subject line
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ConfidenceChip(observation.confidenceLevel)
                    InfoChip(observation.category, icon = FieldMindIcons.iconForCategory(observation.category))
                    InfoChip("${observation.date} ${observation.time}", icon = FieldMindIcons.Today)
                    observation.durationMs?.let { InfoChip("${it / 1000}s", icon = FieldMindIcons.Timer) }
                }
                if (observation.manualLocation.isNotBlank() || observation.weatherCondition.isNotBlank() || observation.weatherTemperature != null) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (observation.manualLocation.isNotBlank()) InfoChip(observation.manualLocation, icon = FieldMindIcons.Location)
                        if (observation.weatherCondition.isNotBlank() || observation.weatherTemperature != null) InfoChip(listOfNotNull(observation.weatherTemperature?.let { "%.1f°C".format(it) }, observation.weatherCondition.ifBlank { null }, observation.weatherHumidity?.let { "$it% humidity" }).joinToString(" • "), icon = FieldMindIcons.Weather)
                        observation.changeDurationMs?.let { InfoChip("Change at +${it / 1000}s", icon = FieldMindIcons.Timer) }
                    }
                }

                Text(observation.subject.ifBlank { "Untitled observation" },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold)

                if (observation.tags.isNotBlank()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        observation.tags.split(",").filter { it.isNotBlank() }.forEach { tag ->
                            TagChip(tag.trim())
                        }
                    }
                }
            }
        }

        // Facts section
        Card(
            shape = CuteCardDefaults.Shape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(36.dp).clip(RoundedCornerShape(18.dp)).background(FieldMindTheme.colors.observation.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                        Icon(FieldMindIcons.Edit, null, tint = FieldMindTheme.colors.observation, size = 18.dp)
                    }
                    Text("Facts-only notes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Text(
                    observation.factsOnlyNotes.ifBlank { "No factual notes recorded." },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Context section
        if (observation.moodOrContext.isNotBlank()) {
            Card(
                shape = CuteCardDefaults.Shape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(36.dp).clip(RoundedCornerShape(18.dp)).background(FieldMindTheme.colors.hypothesis.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                            Icon(FieldMindIcons.Lightbulb, null, tint = FieldMindTheme.colors.hypothesis, size = 18.dp)
                        }
                        Text("Context", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    Text(observation.moodOrContext, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        if (observation.structuredDetailsJson.isNotBlank() || observation.timeNote.isNotBlank()) {
            Card(
                shape = CuteCardDefaults.Shape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Structured research details", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    if (observation.structuredDetailsJson.isNotBlank()) Text(observation.structuredDetailsJson, style = MaterialTheme.typography.bodyMedium)
                    if (observation.timeNote.isNotBlank()) Text("Timing note: ${observation.timeNote}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Evidence summary
        if (observation.evidenceSummary.isNotBlank()) {
            Card(
                shape = CuteCardDefaults.Shape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(36.dp).clip(RoundedCornerShape(18.dp)).background(FieldMindTheme.colors.data.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                            Icon(FieldMindIcons.Camera, null, tint = FieldMindTheme.colors.data, size = 18.dp)
                        }
                        Text("Evidence summary", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    Text(observation.evidenceSummary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Attachments
        onAttachments()

        // Location map
        onMap()
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Units & Format Settings Page
// ══════════════════════════════════════════════════════════════════════

@Composable
fun UnitsFormatSettingsPage(viewModel: FieldMindViewModel, onBack: () -> Unit) {
    val settings = viewModel.fieldSettings
    val tempUnit by settings.tempUnit.collectAsState()
    val distanceUnit by settings.distanceUnit.collectAsState()
    val windSpeedUnit by settings.windSpeedUnit.collectAsState()
    val timeFormat by settings.timeFormat.collectAsState()
    val dateFormat by settings.dateFormat.collectAsState()

    SettingsSubPage("Units & format", icon = FieldMindIcons.Settings, onBack = onBack) {
        item {
            SectionHeader("Measurement units", "Choose how values are displayed in the app.")
        }
        item {
            SettingsGroupCard {
                ChoiceItemForm("Temperature", listOf("Celsius", "Fahrenheit"), tempUnit, FieldMindIcons.Weather, settings::setTempUnit)
                HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ChoiceItemForm("Distance", listOf("km", "miles"), distanceUnit, FieldMindIcons.Map, settings::setDistanceUnit)
                HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ChoiceItemForm("Wind speed", listOf("km/h", "mph", "knots"), windSpeedUnit, FieldMindIcons.AirWave, settings::setWindSpeedUnit)
            }
        }
        item {
            SectionHeader("Date & time format", "Control how timestamps appear.")
        }
        item {
            SettingsGroupCard {
                ChoiceItemForm("Time format", listOf("24h", "12h"), timeFormat, FieldMindIcons.Timer, settings::setTimeFormat)
                HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ChoiceItemForm("Date format", listOf("ISO", "Local"), dateFormat, FieldMindIcons.Today, settings::setDateFormat)
            }
        }
        item {
            Card(shape = CuteCardDefaults.Shape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Display only", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Text("Unit and format changes only affect how data is displayed in the UI. Your stored data is always saved in base metric/ISO formats.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// ═════════��════════════════════════════════════════════════════════════
//  Weather Settings Page (separate from Capture defaults)
// ═════════════════════════════════════════════════���════════════════════

@Composable
fun WeatherSettingsPage(viewModel: FieldMindViewModel, onBack: () -> Unit) {
    val settings = viewModel.fieldSettings
    val autoWeather by settings.autoWeatherEnabled.collectAsState()
    val tempUnit by settings.tempUnit.collectAsState()
    val weatherRefresh by settings.weatherRefreshInterval.collectAsState()
    val showTemp by settings.weatherShowTemperature.collectAsState()
    val showCondition by settings.weatherShowCondition.collectAsState()
    val showHumidity by settings.weatherShowHumidity.collectAsState()
    val showWind by settings.weatherShowWind.collectAsState()
    val showCloud by settings.weatherShowCloudCover.collectAsState()
    val showPressure by settings.weatherShowPressure.collectAsState()
    val showCloudAnimation by settings.weatherShowCloudAnimation.collectAsState()
    val providerSlugs by settings.weatherProviders.collectAsState()
    val apiKey by settings.weatherApiKey.collectAsState()
    val openWeatherMapKey by settings.openWeatherMapApiKey.collectAsState()
    val weatherApiDotComKey by settings.weatherApiDotComApiKey.collectAsState()
    val imdApiKey by settings.imdApiKey.collectAsState()
    val openMeteoApiKey by settings.openMeteoApiKey.collectAsState()
    val selectedProviderSet = remember(providerSlugs) { providerSlugs.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet().ifEmpty { setOf("met-norway") } }
    val keyProvider = remember(selectedProviderSet) { WeatherProviders.selectedProviders(selectedProviderSet.joinToString(",")).firstOrNull { it.requiresApiKey } ?: WeatherProviders.selectedProviders(selectedProviderSet.joinToString(",")).first() }
    SettingsSubPage("Weather", icon = FieldMindIcons.Weather, onBack = onBack) {
        item {
            SectionHeader("Data capture", "Weather automatically attached to observations.")
        }
        item {
            SettingsGroupCard {
                ToggleItem("Auto weather", "Fetch live weather when adding observations.", autoWeather, settings::setAutoWeatherEnabled, FieldMindIcons.Weather)
                HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ChoiceItemForm("Temperature unit", listOf("Celsius", "Fahrenheit"), tempUnit, FieldMindIcons.Weather, settings::setTempUnit)
                HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ChoiceItemForm("Auto-refresh interval", listOf("15 min", "30 min", "60 min"), weatherRefresh, FieldMindIcons.Timer, settings::setWeatherRefreshInterval)
            }
        }
        item {
            SectionHeader("Weather services", "Choose one or more services. FieldMind merges every successful response and keeps partial data when a service misses a field.")
        }
        item {
            SettingsGroupCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Enabled services", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    WeatherProviders.providers.forEach { provider ->
                        val isSelected = provider.slug in selectedProviderSet
                        val providerColor = FieldMindTheme.colors.info
                        Surface(
                            onClick = { settings.setWeatherProviderEnabled(provider.slug, !isSelected) },
                            shape = CuteCardDefaults.ButtonShape,
                            color = if (isSelected) providerColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = if (isSelected) BorderStroke(1.5.dp, providerColor) else null
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(provider.displayName, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        if (provider.requiresApiKey) "Requires API key" else "Free, no key needed",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Checkbox(checked = isSelected, onCheckedChange = { settings.setWeatherProviderEnabled(provider.slug, it) })
                            }
                        }
                    }

                    // ── Per-provider API key fields ──
                    // Show individual API key fields for each enabled provider that requires a key
                    val enabledKeyProviders = WeatherProviders.selectedProviders(providerSlugs).filter { it.requiresApiKey }
                    enabledKeyProviders.forEach { provider ->
                        Spacer(Modifier.height(8.dp))
                        val (keyValue, onKeyChange) = when (provider.slug) {
                            "openweathermap" -> openWeatherMapKey to { v: String -> settings.setOpenWeatherMapApiKey(v) }
                            "weatherapi" -> weatherApiDotComKey to { v: String -> settings.setWeatherApiDotComApiKey(v) }
                            "imd-india" -> imdApiKey to { v: String -> settings.setImdApiKey(v) }
                            "open-meteo" -> openMeteoApiKey to { v: String -> settings.setOpenMeteoApiKey(v) }
                            else -> apiKey to { v: String -> settings.setWeatherApiKey(v) }
                        }
                        OutlinedTextField(
                            value = keyValue,
                            onValueChange = onKeyChange,
                            label = { Text(provider.apiKeyLabel) },
                            placeholder = { Text(provider.apiKeyPlaceholder) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = CuteCardDefaults.FieldShape,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.withPrivacyTyping(LocalPrivacyTypingEnabled.current),
                            trailingIcon = {
                                if (LocalPrivacyTypingEnabled.current) {
                                    PrivacyTypingIndicator()
                                }
                            },
                            supportingText = {
                                Text(
                                    if (keyValue.isBlank()) "No API key saved. Get one free from the provider's website."
                                    else "API key saved locally on this device."
                                )
                            }
                        )
                    }

                    // ── Info note about free providers ──
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        shape = CuteCardDefaults.ButtonShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ) {
                        Row(
                            Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(FieldMindIcons.Info, null, tint = MaterialTheme.colorScheme.primary, size = 18.dp)
                            Text(
                                "Each provider that requires an API key shows its own field above. " +
                                "Open-Meteo, MET Norway, and NWS are free with no key needed. " +
                                "IMD is best inside India; NWS only returns data for U.S. points.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
            }
        }
    }
        item {
            SectionHeader("Home screen widget display", "Choose which weather fields appear on the home dashboard card. The Weather Database screen always shows all available data.")
        }
        item {
            SettingsGroupCard {
                ToggleItem("Show temperature", "Current temperature on the weather card.", showTemp, settings::setWeatherShowTemperature, FieldMindIcons.Weather)
                HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ToggleItem("Show condition", "Weather condition icon and description.", showCondition, settings::setWeatherShowCondition, FieldMindIcons.Cloud)
                HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ToggleItem("Show humidity", "Humidity percentage.", showHumidity, settings::setWeatherShowHumidity, FieldMindIcons.Water)
                HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ToggleItem("Show wind", "Wind speed.", showWind, settings::setWeatherShowWind, FieldMindIcons.AirWave)
                HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ToggleItem("Show cloud cover", "Cloud cover percentage.", showCloud, settings::setWeatherShowCloudCover, FieldMindIcons.Cloud)
                HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ToggleItem("Show pressure", "Atmospheric pressure in hPa.", showPressure, settings::setWeatherShowPressure, FieldMindIcons.Compress)
                    HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

        }
        }
    }

}

// ══════════════════════════════════════════════════════════════════════
//  Map Settings Page
// ══════════════════════════════════════════════════════════════════════

@Composable
fun MapSettingsPage(viewModel: FieldMindViewModel, onBack: () -> Unit) {
    val settings = viewModel.fieldSettings
    val mapType by settings.mapType.collectAsState()
    val mapShowLocation by settings.mapShowLocation.collectAsState()

    SettingsSubPage("Map settings", icon = FieldMindIcons.Map, onBack = onBack) {
        item {
            SettingsGroupCard {
                ChoiceItemForm("Default map type", listOf("Standard", "Satellite", "Terrain"), mapType, FieldMindIcons.Map, settings::setMapType)
                HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ToggleItem("Show my location", "Display your current position as a marker on the map.", mapShowLocation, settings::setMapShowLocation, FieldMindIcons.Location)
            }
        }
        item {
            Card(shape = CuteCardDefaults.Shape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Map data", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Text("FieldMind uses OpenStreetMap tiles for map rendering. No map data is sent to any server beyond the tile request.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Data Integrity Settings Page
// ══════════════════════════════════════════════════════════════════════

@Composable
fun DataIntegritySettingsPage(viewModel: FieldMindViewModel, onBack: () -> Unit) {
    val settings = viewModel.fieldSettings
    val checkOnLaunch by settings.dataIntegrityCheckOnLaunch.collectAsState()
    val observations by viewModel.observations.collectAsState()
    val questions by viewModel.questions.collectAsState()
    val sources by viewModel.sources.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val hypotheses by viewModel.hypotheses.collectAsState()
    val dataRecords by viewModel.dataRecords.collectAsState()
    val reports by viewModel.reports.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isRunning by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }
    var resultReport by remember { mutableStateOf("") }

    val orphanedObs = remember(observations, projects) {
        observations.count { obs -> (obs.projectId ?: 0L) > 0 && projects.none { it.id == obs.projectId } }
    }
    val totalRecords = observations.size + questions.size + sources.size

    suspend fun runIntegrityCheck(): String {
        val dao = fieldmind.research.app.features.field.data.database.FieldMindDatabase
            .getInstance(context).fieldMindDao()
        val issues = mutableListOf<String>()
        val fixes = mutableListOf<String>()

        // 1. Check orphaned observations (referencing deleted projects)
        if (orphanedObs > 0) {
            issues.add("$orphanedObs observation(s) reference missing or deleted projects")
            observations.filter { obs -> (obs.projectId ?: 0L) > 0 && projects.none { it.id == obs.projectId } }
                .forEach { obs ->
                    dao.updateObservation(obs.copy(projectId = null))
                }
            fixes.add("Cleared projectId on $orphanedObs orphaned observation(s)")
        }

        // 2. Check orphaned notes (referencing deleted projects)
        val orphanedNotes = notes.count { n -> (n.projectId ?: 0L) > 0 && projects.none { it.id == n.projectId } }
        if (orphanedNotes > 0) {
            issues.add("$orphanedNotes note(s) reference missing or deleted projects")
            notes.filter { n -> (n.projectId ?: 0L) > 0 && projects.none { it.id == n.projectId } }
                .forEach { n -> dao.updateNote(n.copy(projectId = null)) }
            fixes.add("Cleared projectId on $orphanedNotes orphaned note(s)")
        }

        // 3. Check orphaned sources (referencing deleted projects)
        val orphanedSources = sources.count { s -> (s.relatedProjectId ?: 0L) > 0 && projects.none { it.id == s.relatedProjectId } }
        if (orphanedSources > 0) {
            issues.add("$orphanedSources source(s) reference missing or deleted projects")
            sources.filter { s -> (s.relatedProjectId ?: 0L) > 0 && projects.none { it.id == s.relatedProjectId } }
                .forEach { s -> dao.updateSource(s.copy(relatedProjectId = null)) }
            fixes.add("Cleared relatedProjectId on $orphanedSources orphaned source(s)")
        }

        // 4. Check for observations with blank/empty subjects
        val blankSubjects = observations.count { it.subject.isBlank() }
        if (blankSubjects > 0) {
            issues.add("$blankSubjects observation(s) have blank subjects")
            fixes.add("Flagged $blankSubjects observation(s) with blank subjects for review")
        }

        // 5. Check for duplicate observations (same subject + date)
        val dupes = observations.groupBy { it.subject.lowercase() to it.date }
            .filter { (key, group) -> group.size > 1 && key.first.isNotBlank() }
        val dupeCount = dupes.values.sumOf { it.size - 1 }
        if (dupeCount > 0) {
            issues.add("$dupeCount potential duplicate observation(s) found (same subject + date)")
            fixes.add("Found ${dupes.size} group(s) of duplicate observations — review manually")
        }

        // 6. Check for null/invalid dates
        val invalidDates = observations.count { it.date.isBlank() }
        if (invalidDates > 0) {
            issues.add("$invalidDates observation(s) have blank dates")
            fixes.add("Flagged $invalidDates observation(s) with blank dates")
        }

        // Build report
        val sb = StringBuilder()
        if (issues.isEmpty()) {
            sb.appendLine("✅ No integrity issues found!")
            sb.appendLine("Your database appears healthy.")
        } else {
            sb.appendLine("🔍 Issues found: ${issues.size}")
            issues.forEach { sb.appendLine("• $it") }
            sb.appendLine()
            sb.appendLine("✅ Auto-fixed: ${fixes.size}")
            fixes.forEach { sb.appendLine("• $it") }
            if (dupes.isNotEmpty()) {
                sb.appendLine()
                sb.appendLine("📋 Manual review needed:")
                dupes.entries.take(5).forEach { (key, group) ->
                    sb.appendLine("• \"${key.first}\" (${key.second}) — ${group.size} entries")
                }
                if (dupes.size > 5) sb.appendLine("• ...and ${dupes.size - 5} more duplicate groups")
            }
        }
        return sb.toString()
    }

    SettingsSubPage("Data integrity", icon = FieldMindIcons.Archive, onBack = onBack) {
        item {
            SettingsGroupCard {
                ToggleItem("Check on launch", "Validate database integrity and report issues when the app starts.", checkOnLaunch, settings::setDataIntegrityCheckOnLaunch, FieldMindIcons.Check)
            }
        }
        item {
            SettingsGroupCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Database summary", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        IntegrityStat("$totalRecords", "Total records")
                        IntegrityStat("${observations.size}", "Observations")
                        IntegrityStat("${questions.size}", "Questions")
                        IntegrityStat("${sources.size}", "Sources")
                    }
                    if (orphanedObs > 0) {
                        Spacer(Modifier.height(8.dp))
                        Surface(shape = CuteCardDefaults.ShapeCompact, color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)) {
                            Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(FieldMindIcons.Info, null, tint = MaterialTheme.colorScheme.error, size = 18.dp)
                                Text("$orphanedObs observation${if (orphanedObs != 1) "s" else ""} reference missing or deleted projects", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }
                }
            }
        }
        item {
            if (isRunning) {
                Card(shape = CuteCardDefaults.ShapeCompact, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)) {
                    Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text("Running integrity check…", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                }
            } else {
                FilledTonalButton(
                    onClick = {
                        isRunning = true
                        scope.launch {
                            resultReport = withContext(Dispatchers.IO) { runIntegrityCheck() }
                            isRunning = false
                            showResultDialog = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CuteCardDefaults.ShapeCompact
                ) {
                    Icon(FieldMindIcons.Check, null, size = 18.dp)
                    Spacer(Modifier.size(8.dp))
                    Text("Run integrity check")
                }
            }
        }
        item {
            Card(shape = CuteCardDefaults.Shape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("What this checks", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    listOf(
                        "Orphaned records referencing deleted projects",
                        "Missing or corrupted attachment URIs",
                        "Inconsistent date/time values",
                        "Duplicate record detection"
                    ).forEach { item ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                            Icon(FieldMindIcons.Check, null, tint = MaterialTheme.colorScheme.primary, size = 14.dp, modifier = Modifier.padding(top = 2.dp))
                            Text(item, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    // ── Integrity check result dialog ──
    if (showResultDialog) {
        AlertDialog(
            onDismissRequest = { showResultDialog = false },
            icon = { Icon(FieldMindIcons.Check, null, size = 28.dp) },
            title = { Text("Integrity check complete", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    resultReport,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(onClick = { showResultDialog = false }) { Text("Done") }
            }
        )
    }
}


@Composable
private fun IntegrityStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
// ══════════════════════════════════════════════════════════════════════
//  Screen Visibility Settings Page
// ══════════════════════════════════════════════════════════════════════

@Composable
fun ScreenVisibilitySettingsPage(viewModel: FieldMindViewModel, onBack: () -> Unit) {
    val settings = viewModel.fieldSettings
    val screenVis by settings.screenVisibility.collectAsState()
    val colors = FieldMindTheme.colors

    val visibilityToggles = listOf(
        ScreenVisibilityItem("Capture / Observe", "Observation capture screen in bottom nav", screenVis.showCapture, FieldMindIcons.Capture, colors.observation),
        ScreenVisibilityItem("Projects", "Project workspace and management", screenVis.showProjects, FieldMindIcons.Project, colors.project),
        ScreenVisibilityItem("Insights", "Research insights, health scores, graphs", screenVis.showInsights, FieldMindIcons.Graph, colors.info),
        ScreenVisibilityItem("Library", "Sources, notes, flashcards, reading", screenVis.showLibrary, FieldMindIcons.Book, colors.source),
        ScreenVisibilityItem("Map", "Offline map with drawing tools", screenVis.showMap, FieldMindIcons.Map, colors.data),
        ScreenVisibilityItem("Weather database", "Historical weather data screen", screenVis.showWeather, FieldMindIcons.Weather, colors.info),
        ScreenVisibilityItem("Species browser", "Taxonomic browser and species catalog", screenVis.showSpeciesBrowser, FieldMindIcons.Nature, colors.observation),
        ScreenVisibilityItem("Flashcards", "Flashcard review sessions", screenVis.showFlashcards, FieldMindIcons.Flashcard, colors.flashcard),
        ScreenVisibilityItem("Export studio", "Data export and report builder", screenVis.showExport, FieldMindIcons.Export, colors.data),

    )

    SettingsSubPage("Screen visibility", icon = FieldMindIcons.Visibility, onBack = onBack) {
        item {
            Card(
                shape = CuteCardDefaults.Shape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(FieldMindIcons.Info, null, tint = MaterialTheme.colorScheme.primary, size = 20.dp)
                        Text("Hide screens you don't use to keep navigation clean. Hidden screens are still accessible from settings.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item {
            SettingsGroupCard {
                visibilityToggles.forEach { item ->
                    Row(
                        Modifier.fillMaxWidth().clickable {
                            val cur = screenVis
                            val updated = when (item.icon) {
                                FieldMindIcons.Capture -> cur.copy(showCapture = !item.isEnabled)
                                FieldMindIcons.Project -> cur.copy(showProjects = !item.isEnabled)
                                FieldMindIcons.Graph -> cur.copy(showInsights = !item.isEnabled)
                                FieldMindIcons.Book -> cur.copy(showLibrary = !item.isEnabled)
                                FieldMindIcons.Map -> cur.copy(showMap = !item.isEnabled)
                                FieldMindIcons.Weather -> cur.copy(showWeather = !item.isEnabled)
                                FieldMindIcons.Nature -> cur.copy(showSpeciesBrowser = !item.isEnabled)
                                FieldMindIcons.Flashcard -> cur.copy(showFlashcards = !item.isEnabled)
                                FieldMindIcons.Export -> cur.copy(showExport = !item.isEnabled)
                                else -> cur
                            }
                            settings.setScreenVisibility(updated)
                        }.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(44.dp).clip(CuteCardDefaults.ButtonShape).background(item.accentColor.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                            Icon(icon = item.icon, contentDescription = null, tint = item.accentColor, size = 22.dp)
                        }
                        Column(Modifier.weight(1f)) {
                            Text(item.title, fontWeight = FontWeight.SemiBold)
                            Text(item.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = item.isEnabled, onCheckedChange = {
                            val cur = screenVis
                            val updated = when (item.icon) {
                                FieldMindIcons.Capture -> cur.copy(showCapture = it)
                                FieldMindIcons.Project -> cur.copy(showProjects = it)
                                FieldMindIcons.Graph -> cur.copy(showInsights = it)
                                FieldMindIcons.Book -> cur.copy(showLibrary = it)
                                FieldMindIcons.Map -> cur.copy(showMap = it)
                                FieldMindIcons.Weather -> cur.copy(showWeather = it)
                                FieldMindIcons.Nature -> cur.copy(showSpeciesBrowser = it)
                                FieldMindIcons.Flashcard -> cur.copy(showFlashcards = it)
                                FieldMindIcons.Export -> cur.copy(showExport = it)
                                else -> cur
                            }
                            settings.setScreenVisibility(updated)
                        })
                    }
                    if (item.title != visibilityToggles.last().title) {
                        HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    }
                }
            }
        }
        item {
            Card(shape = CuteCardDefaults.Shape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Navigation impact", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Text("Disabling the Capture, Projects, Insights, or Library tabs removes them from the bottom navigation bar. The screens remain accessible via deep links and search.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Developer Settings Page
// ══════════════════════════════════════════════════════════════════════

@Composable
fun DeveloperSettingsPage(viewModel: FieldMindViewModel, onBack: () -> Unit, onOpenAnimationTuning: (() -> Unit)? = null) {
    val settings = viewModel.fieldSettings
    val developerMode by settings.developerMode.collectAsState()
    val debugLogging by settings.debugLogging.collectAsState()
    val dataIntegrityCheck by settings.dataIntegrityCheckOnLaunch.collectAsState()
    val showWeatherTest by settings.showWeatherTestPanel.collectAsState()
    var testWeatherCode by remember { mutableStateOf<Int?>(null) }
    var testIsNight by remember { mutableStateOf(false) }
    var testTemperature by remember { mutableStateOf<Int?>(null) }
    var testHumidity by remember { mutableStateOf<Int?>(null) }
    var testWeatherPanelExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showSchemaDialog by remember { mutableStateOf(false) }
    var schemaInfo by remember { mutableStateOf("") }
    var showClearConfirmation by remember { mutableStateOf(false) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            android.widget.Toast.makeText(context, "Notification permission granted. Tap test notifications again.", android.widget.Toast.LENGTH_SHORT).show()
        } else {
            android.widget.Toast.makeText(context, "Notification permission denied. Enable in Settings.", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    SettingsSubPage("Developer", icon = FieldMindIcons.Sparkle, onBack = onBack) {
        item {
            SettingsGroupCard {
                ToggleItem("Developer mode", "Enable developer options and debug UI elements.", developerMode, settings::setDeveloperMode, FieldMindIcons.Sparkle)
            }
        }
        if (developerMode) {
            item {
                SettingsGroupCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Developer tools", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        listOf(
                            "View database schema",
                            "Export raw JSON dump",
                            "Test notifications",
                            "Clear all preferences (restart required)"
                        ).forEach { tool ->
                            Surface(
                                onClick = {
                                    when (tool) {
                                        "View database schema" -> {
                                            schemaInfo = "Database: fieldmind_database\\nSchema version: 12\\nEntity types:\\n" +
                                                "• Observations, Notes, Questions\\n" +
                                                "• Hypotheses, Projects, Sources\\n" +
                                                "• Data Records, Reports, Flashcards\\n" +
                                                "• Species, Tasks, Weather Catalog\\n" +
                                                "• Research Sessions, Evidence Attachments\\n" +
                                                "• Tags, Cross-references (14 tables)" +
                                                "\\n\\nStatus: Room with KSP annotation processing"
                                            showSchemaDialog = true
                                        }
                                        "Export raw JSON dump" -> {
                                            scope.launch {
                                                android.widget.Toast.makeText(context, "Building JSON dump…", android.widget.Toast.LENGTH_SHORT).show()
                                                try {
                                                    val json = withContext(Dispatchers.IO) {
                                                        fieldmind.research.app.features.field.data.export.FieldMindExport.archiveJson(
                                                            observations = viewModel.observations.value,
                                                            notes = viewModel.notes.value,
                                                            questions = viewModel.questions.value,
                                                            hypotheses = viewModel.hypotheses.value,
                                                            projects = viewModel.projects.value,
                                                            sources = viewModel.sources.value,
                                                            dataRecords = viewModel.dataRecords.value,
                                                            reports = viewModel.reports.value,
                                                            flashcards = viewModel.flashcards.value,
                                                            species = viewModel.speciesRegistry.value,
                                                            weatherCatalog = viewModel.weatherCatalog.value,
                                                            researchSessions = viewModel.researchSessions.value,
                                                            tasks = viewModel.tasks.value
                                                        )
                                                    }
                                                    val exportDir = java.io.File(context.cacheDir, "exports").apply { mkdirs() }
                                                    val stamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
                                                    val dumpFile = java.io.File(exportDir, "fieldmind_raw_dump_$stamp.json")
                                                    dumpFile.writeText(json)
                                                    // Share via FileProvider for FileUriExposedException safety
                                                    val uri = androidx.core.content.FileProvider.getUriForFile(
                                                        context,
                                                        context.packageName + ".provider",
                                                        dumpFile
                                                    )
                                                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                                        type = "application/json"
                                                        putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    }
                                                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Share JSON dump"))
                                                    android.widget.Toast.makeText(context, "Dump saved: ${dumpFile.name}", android.widget.Toast.LENGTH_LONG).show()
                                                } catch (e: Exception) {
                                                    android.widget.Toast.makeText(context, "Export failed: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        }
                                        "Test notifications" -> {
                                            val nm = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                                nm.createNotificationChannel(
                                                    android.app.NotificationChannel("fieldmind_test", "Test", android.app.NotificationManager.IMPORTANCE_DEFAULT).apply {
                                                        description = "Test notification channel"
                                                    }
                                                )
                                                // Check POST_NOTIFICATIONS permission on Android 13+
                                                if (androidx.core.content.ContextCompat.checkSelfPermission(
                                                        context, android.Manifest.permission.POST_NOTIFICATIONS
                                                ) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                                } else {
                                                    nm.notify(1001, android.app.Notification.Builder(context, "fieldmind_test")
                                                        .setContentTitle("FieldMind Test")
                                                        .setContentText("This is a test notification from Developer settings")
                                                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                                                        .setAutoCancel(true)
                                                        .build())
                                                    android.widget.Toast.makeText(context, "Test notification sent", android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                            nm.notify(1001, android.app.Notification.Builder(context, "fieldmind_test")
                                                .setContentTitle("FieldMind Test")
                                                .setContentText("This is a test notification from Developer settings")
                                                .setSmallIcon(android.R.drawable.ic_dialog_info)
                                                .setAutoCancel(true)
                                                .build())
                                            android.widget.Toast.makeText(context, "Test notification sent", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                        "Clear all preferences (restart required)" -> {
                                            showClearConfirmation = true
                                        }
                                    }
                                },
                                shape = CuteCardDefaults.ButtonShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh
                            ) {
                                Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(tool, style = MaterialTheme.typography.bodyMedium)
                                    Icon(FieldMindIcons.Forward, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 18.dp)
                                }
                            }
                        }
                    }
                }
            }
            // ── Animation Speed Preset, Enable animations, Cloud animations moved to Settings → Animations page ──
            item {
                SettingsGroupCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Debug options", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        ToggleItem("Debug logging", "Enable verbose logging for troubleshooting.", debugLogging, settings::setDebugLogging, FieldMindIcons.Sparkle)
                        HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ToggleItem("Show weather test panel", "Show weather condition test controls on the Home screen weather widget.", showWeatherTest, settings::setShowWeatherTestPanel, MaterialSymbolIcon("test_tube"))
                        ToggleItem("Data integrity check on launch", "Run database health checks on app startup.", dataIntegrityCheck, settings::setDataIntegrityCheckOnLaunch, FieldMindIcons.Archive)
                    }
                }
            }
            item {
                CollapsibleSection(
                    title = "Test weather conditions",
                    subtitle = "Override live weather for debugging",
                    expanded = testWeatherPanelExpanded,
                    onToggle = { testWeatherPanelExpanded = !testWeatherPanelExpanded }
                ) {
                    DevWeatherTestPanel(
                        testCode = testWeatherCode,
                        testNight = testIsNight,
                        testTemperature = testTemperature,
                        testHumidity = testHumidity,
                        onCodeChange = { testWeatherCode = it },
                        onNightChange = { testIsNight = it },
                        onTemperatureChange = { testTemperature = it },
                        onHumidityChange = { testHumidity = it }
                    )
                }
            }
            item {
                Card(shape = CuteCardDefaults.Shape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Version info", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        Text("FieldMind 4.3.0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Database schema version: 9", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Room: SQLite-backed local storage", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            item { SettingsNavCard("Animation Tuning", "Adjust spring physics, damping, stiffness, and preview live animations", MaterialSymbolIcon("tune"), FieldMindTheme.colors.flashcard) { onOpenAnimationTuning?.invoke() } }
            // ── Debug gesture thresholds, animation state, and tap test ──
            item { GestureThresholdsCard() }
            item { AnimationStateCard() }
            item { TapTestCard() }
        item { DevFullAppTestRunner(viewModel = viewModel) }
        item {
            Card(shape = CuteCardDefaults.Shape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Caution", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                    Text("Developer options are intended for troubleshooting and testing. Incorrect changes to stored data could cause data loss.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
    }

    // ���─ Clear preferences confirmation dialog ──
    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            icon = { Icon(FieldMindIcons.Settings, null, size = 28.dp) },
            title = { Text("Clear all preferences?", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("This will reset ALL settings to defaults, including:")
                    Text("\u2022 Theme, units, and display preferences\n"
                        + "\u2022 Weather providers and API keys\n"
                        + "\u2022 AI assistant configuration\n"
                        + "\u2022 Capture defaults and security settings")
                    Spacer(Modifier.height(8.dp))
                    Text("The app needs to restart for changes to take effect. Your observations, notes, and other data will not be affected.",
                         style = MaterialTheme.typography.bodySmall,
                         color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirmation = false
                        scope.launch {
                            settings.clearAllPreferences()
                            android.widget.Toast.makeText(context, "Preferences cleared. Closing app...", android.widget.Toast.LENGTH_LONG).show()
                            // Restart the activity to reload settings
                            (context as? androidx.activity.ComponentActivity)?.let { activity ->
                                activity.finishAffinity()
                            }
                        }
                    }
                ) { Text("Clear and restart") }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) { Text("Cancel") }
            }
        )
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Shared helpers
// ══════════════════════════════════════════════════════════════════════
// ── Species Tools (merged page) ──
@Composable
fun SpeciesToolsPage(viewModel: FieldMindViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val settings = viewModel.fieldSettings
    val scope = rememberCoroutineScope()
    val haptics = rememberFieldMindHaptics()
    val database = remember { SpeciesDatabase.getInstance(context) }
    val apiKey by settings.speciesIdApiKey.collectAsState()
    val offlineFirst by settings.speciesIdOfflineFirst.collectAsState()
    val modelBaseUrl by settings.speciesModelBaseUrl.collectAsState()
    val perenualKey by settings.perenualApiKey.collectAsState()
    var packs by remember { mutableStateOf(database.getRegionalPacks()) }
    var downloadingId by remember { mutableStateOf<String?>(null) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    fun refreshPacks() { packs = database.getRegionalPacks() }
    LaunchedEffect(Unit) { database.setProgressListener { _, d, t -> if (t > 0) downloadProgress = (d.toFloat() / t).coerceIn(0f, 1f) } }
    DisposableEffect(Unit) { onDispose { database.setProgressListener(null) } }
    LaunchedEffect(packs) { refreshPacks() }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp, 12.dp, 20.dp, 40.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { StandardScreenHeader(title = "Species tools", subtitle = "Identification settings, API keys, and regional model packs", icon = FieldMindIcons.Nature, trailing = { BackButton(onClick = onBack) }) }
            item {
                Card(shape = CuteCardDefaults.Shape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.size(36.dp).clip(RoundedCornerShape(18.dp)).background(FieldMindTheme.colors.observation.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                                Icon(FieldMindIcons.Nature, null, tint = FieldMindTheme.colors.observation, size = 18.dp)
                            }
                            Text("How identification works", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                        Text("FieldMind uses a pure-Kotlin image analysis engine - color histograms, edge detection, texture analysis, and perceptual hashing - to identify species from photos. No AI, no internet, no server needed.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Predictions improve as you confirm IDs. Download regional packs below to expand the built-in ~500 species database.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            item { SectionHeader("Identification", "Offline analysis and cloud API keys") }
            item { SettingsGroupCard { ToggleItem("Offline-first mode (recommended)", "Use the on-device image analyzer. No internet required. Turn off to allow cloud reference lookups if you add an API key.", offlineFirst, settings::setSpeciesIdOfflineFirst, FieldMindIcons.Nature) } }
            if (!offlineFirst) {
                item {
                    SettingsGroupCard {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Perenual API (plant and botany data)", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                            OutlinedTextField(value = perenualKey, onValueChange = settings::setPerenualApiKey, label = { Text("Perenual API key") }, placeholder = { Text("Paste your Perenual API key") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = CuteCardDefaults.FieldShape, singleLine = true, supportingText = { Text(if (perenualKey.isBlank()) "No key saved. Sign up free at perenual.com" else "Perenual key saved locally.") })
                        }
                    }
                }
                item {
                    SettingsGroupCard {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Other species API (optional)", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            OutlinedTextField(value = apiKey, onValueChange = settings::setSpeciesIdApiKey, label = { Text("Custom API key (optional)") }, placeholder = { Text("Leave blank if using iNaturalist") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = CuteCardDefaults.FieldShape, singleLine = true, supportingText = { Text(if (apiKey.isBlank()) "No key - iNaturalist API is free" else "Custom key saved locally.") })
                        }
                    }
                }
            }
            item { SectionHeader("Regional packs", "Download and manage species identification packs") }
            item {
                SettingsGroupCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = modelBaseUrl, onValueChange = settings::setSpeciesModelBaseUrl, label = { Text("Pack base URL (advanced)") }, placeholder = { Text("https://...") }, modifier = Modifier.fillMaxWidth(), shape = CuteCardDefaults.FieldShape, singleLine = true, supportingText = { Text(if (modelBaseUrl.isBlank()) "Default URL" else "Using: $modelBaseUrl") })
                    }
                }
            }
            items(packs, key = { it.regionId }) { pack ->
                val isDownloaded = pack.isDownloaded
                val isDownloading = downloadingId == pack.regionId
                Card(shape = CuteCardDefaults.Shape, colors = CardDefaults.cardColors(containerColor = if (isDownloaded) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow), elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(44.dp).clip(CuteCardDefaults.ButtonShape).background(FieldMindTheme.colors.observation.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) { Icon(if (isDownloaded) FieldMindIcons.Check else FieldMindIcons.Download, null, tint = FieldMindTheme.colors.observation, size = 24.dp) }
                            Column(Modifier.weight(1f)) {
                                Text(pack.regionName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(pack.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        if (isDownloading) {
                            Column { LinearProgressIndicator(progress = { downloadProgress }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(8.dp)), color = FieldMindTheme.colors.observation)
                                Text("${(downloadProgress * 100).toInt()}% - Downloading...", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (isDownloaded) {
                                OutlinedButton(onClick = { haptics.light(); scope.launch { database.deletePack(pack.regionId); refreshPacks() } }, modifier = Modifier.weight(1f), shape = CuteCardDefaults.ButtonShape, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                                    Icon(FieldMindIcons.Delete, null, size = 18.dp); Spacer(Modifier.size(6.dp)); Text("Delete")
                                }
                            } else {
                                Button(onClick = { haptics.confirm(); downloadingId = pack.regionId; downloadProgress = 0f; scope.launch { runCatching { database.downloadPack(pack.regionId) }; downloadingId = null; refreshPacks() } }, modifier = Modifier.weight(1f), shape = CuteCardDefaults.ButtonShape) {
                                    Icon(FieldMindIcons.Download, null, size = 18.dp); Spacer(Modifier.size(6.dp)); Text("Download")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun SpeciesPackSettingsPage(onBack: () -> Unit) {
    val context = LocalContext.current
    val database = remember { SpeciesDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()
    val haptics = rememberFieldMindHaptics()
    val snackbar = remember { SnackbarHostState() }

    var packs by remember { mutableStateOf(database.getRegionalPacks()) }
    var downloadingId by remember { mutableStateOf<String?>(null) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }

    // Update pack list whenever download state changes
    fun refreshPacks() {
        packs = database.getRegionalPacks()
    }

    // Set up progress listener
    LaunchedEffect(Unit) {
        database.setProgressListener { regionId, downloaded, total ->
            if (total > 0) {
                downloadProgress = (downloaded.toFloat() / total).coerceIn(0f, 1f)
            }
        }
    }

    // Clean up listener
    DisposableEffect(Unit) {
        onDispose {
            database.setProgressListener(null)
        }
    }

    LaunchedEffect(packs) {
        refreshPacks()
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp, 20.dp, 20.dp, 40.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                StandardScreenHeader(
                    title = "Species packs",
                    subtitle = "Download regional identification model packs.",
                    icon = FieldMindIcons.Download,
                    trailing = {
                        BackButton(onClick = onBack)
                    }
                )
            }

            item {
                Card(
                    shape = CuteCardDefaults.Shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(FieldMindIcons.Info, null, tint = MaterialTheme.colorScheme.primary, size = 20.dp)
                            Text(
                                "Regional packs expand the species identification model beyond the bundled ~500 species.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            items(packs, key = { it.regionId }) { pack ->
                val isDownloaded = pack.isDownloaded
                val isDownloading = downloadingId == pack.regionId

                Card(
                    shape = CuteCardDefaults.Shape,
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDownloaded)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                        else
                            MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
                ) {
                    Column(
                        Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header row
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier
                                    .size(48.dp)
                                    .clip(CuteCardDefaults.ButtonShape)
                                    .background(
                                        FieldMindTheme.colors.observation.copy(alpha = 0.14f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (isDownloaded) FieldMindIcons.Check else FieldMindIcons.Download,
                                    null,
                                    tint = FieldMindTheme.colors.observation,
                                    size = 24.dp
                                )
                            }
                            Column(Modifier.weight(1f)) {
                                Text(
                                    pack.regionName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    pack.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            // Status badge
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = if (isDownloaded)
                                    FieldMindTheme.colors.positive.copy(alpha = 0.14f)
                                else
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                            ) {
                                Text(
                                    if (isDownloaded) "Ready" else "${pack.downloadSizeMb} MB",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDownloaded) FieldMindTheme.colors.positive else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Stats row
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatChip("${pack.speciesCount}", "species", FieldMindTheme.colors.observation)
                            StatChip("${pack.downloadSizeMb} MB", "size", MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        // Download progress bar
                        if (isDownloading) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                LinearProgressIndicator(
                                    progress = { downloadProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    color = FieldMindTheme.colors.observation
                                )
                                Text(
                                    "${(downloadProgress * 100).toInt()}% — Downloading…",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Action buttons
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (isDownloaded) {
                                OutlinedButton(
                                    onClick = {
                                        haptics.light()
                                        scope.launch {
                                            val success = database.deletePack(pack.regionId)
                                            refreshPacks()
                                            showFastSnackbar(snackbar, scope, if (success) "${pack.regionName} pack removed"
                                                else "Could not delete pack"
                                            )
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = CuteCardDefaults.ButtonShape,
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Icon(FieldMindIcons.Delete, null, size = 18.dp)
                                    Spacer(Modifier.size(6.dp))
                                    Text("Delete")
                                }
                    TextButton(
                        onClick = {
                            haptics.light()
                            scope.launch {
                                showFastSnackbar(snackbar, scope, "Model at: ${database.getPackModelPath(pack.regionId)}")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                                    Icon(FieldMindIcons.Info, null, size = 18.dp)
                                    Spacer(Modifier.size(6.dp))
                                    Text("Info", maxLines = 1)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        haptics.confirm()
                                        downloadingId = pack.regionId
                                        downloadProgress = 0f
                                        scope.launch {
                                            val result = runCatching {
                                                database.downloadPack(pack.regionId)
                                            }
                                            downloadingId = null
                                            refreshPacks()
                                            if (result.isSuccess) {
                                                showFastSnackbar(snackbar, scope, "${pack.regionName} pack downloaded")
                                            } else {
                                                val errorMsg = result.exceptionOrNull()?.message
                                                    ?: "Unknown error"
                                                showFastSnackbar(
                                                    snackbar,
                                                    scope,
                                                    "Download failed: $errorMsg"
                                                )
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = CuteCardDefaults.ButtonShape,
                                    enabled = !isDownloading
                                ) {
                                    if (isDownloading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    } else {
                                        Icon(FieldMindIcons.Download, null, size = 18.dp)
                                    }
                                    Spacer(Modifier.size(6.dp))
                                    Text(if (isDownloading) "Downloading…" else "Download")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Top snackbar overlay
        FieldMindSnackbarOverlay(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp, start = 16.dp, end = 16.dp)
        )
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Species Identification Settings Page
// ══════════════════════════════════════════════════════════════════════

@Composable
fun SpeciesIdentificationSettingsPage(
    viewModel: FieldMindViewModel,
    onBack: () -> Unit
) {
    val settings = viewModel.fieldSettings
    val apiKey by settings.speciesIdApiKey.collectAsState()
    val offlineFirst by settings.speciesIdOfflineFirst.collectAsState()
    val modelBaseUrl by settings.speciesModelBaseUrl.collectAsState()
    val perenualKey by settings.perenualApiKey.collectAsState()
    val uriHandler = LocalUriHandler.current

    SettingsSubPage("Species identification", icon = FieldMindIcons.Nature, onBack = onBack) {
        // ── How it works ──
        item {
            Card(
                shape = CuteCardDefaults.Shape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            Modifier.size(36.dp).clip(RoundedCornerShape(18.dp)).background(FieldMindTheme.colors.observation.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(FieldMindIcons.Nature, null, tint = FieldMindTheme.colors.observation, size = 18.dp)
                        }
                        Text("How identification works", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        "FieldMind uses a pure-Kotlin image analysis engine — color histograms, edge detection, texture analysis, and perceptual hashing — to identify species from photos. No AI, no internet, no server needed.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Predictions improve as you confirm IDs. Regional species packs (Settings > Species packs) can expand the built-in ~500 species database.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // ── Offline-first mode ──
        item {
            SettingsGroupCard {
                ToggleItem(
                    "Offline-first mode (recommended)",
                    "Use the on-device image analyzer. No internet required. Turn off to allow cloud reference lookups if you add an API key below.",
                    offlineFirst,
                    settings::setSpeciesIdOfflineFirst,
                    FieldMindIcons.Nature
                )
            }
        }

        // ── Cloud API section (Perenual + generic) ──
        if (!offlineFirst) {
            item {
                SettingsGroupCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                Modifier.size(44.dp).clip(CuteCardDefaults.ButtonShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(FieldMindIcons.Sparkle, null, tint = MaterialTheme.colorScheme.primary, size = 22.dp)
                            }
                            Column(Modifier.weight(1f)) {
                                Text("Cloud species reference", fontWeight = FontWeight.SemiBold)
                                Text(
                                    "Optional cloud APIs help look up species details and images. The offline analyzer still runs first.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        // ���─ Perenual API key ──
                        Text("Perenual API (plant & botany data)", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        Text(
                            "Free tier: 100 requests/day, 3,000+ species. Get a key at perenual.com (free signup). Used to fetch plant details, care guides, and images.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = perenualKey,
                            onValueChange = settings::setPerenualApiKey,
                            label = { Text("Perenual API key") },
                            placeholder = { Text("Paste your Perenual API key") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions.Default.withPrivacyTyping(LocalPrivacyTypingEnabled.current),
                            trailingIcon = {
                                if (LocalPrivacyTypingEnabled.current) {
                                    PrivacyTypingIndicator()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = CuteCardDefaults.FieldShape,
                            singleLine = true,
                            supportingText = {
                                Text(
                                    if (perenualKey.isBlank()) "No key saved. Sign up free at perenual.com"
                                    else "Perenual key saved locally."
                                )
                            }
                        )
                        OutlinedButton(
                            onClick = {
                                runCatching {
                                    uriHandler.openUri("https://perenual.com/")
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = CuteCardDefaults.ButtonShape
                        ) {
                            Icon(FieldMindIcons.OpenLink, null, size = 18.dp)
                            Spacer(Modifier.size(8.dp))
                            Text("Get Perenual API key (free)")
                        }

                        HorizontalDivider(Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                        // ── Generic species API key ──
                        Text("Other species API (optional)", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "For custom or alternative species data APIs. iNaturalist API is free and open — no key required (just use the public endpoint).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = apiKey,
                            onValueChange = settings::setSpeciesIdApiKey,
                            label = { Text("Custom API key (optional)") },
                            placeholder = { Text("Leave blank if using iNaturalist") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = CuteCardDefaults.FieldShape,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.withPrivacyTyping(LocalPrivacyTypingEnabled.current),
                            trailingIcon = {
                                if (LocalPrivacyTypingEnabled.current) {
                                    PrivacyTypingIndicator()
                                }
                            },
                            supportingText = {
                                Text(
                                    if (apiKey.isBlank()) "No key — iNaturalist API is free without a key. Just paste the URL: api.inaturalist.org"
                                    else "Custom key saved locally."
                                )
                            }
                        )

                        Surface(
                            shape = CuteCardDefaults.ButtonShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ) {
                            Row(
                                Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(FieldMindIcons.Info, null, tint = MaterialTheme.colorScheme.primary, size = 18.dp)
                                Text(
                                    "All API keys are stored only on this device. iNaturalist does not require an API key for basic read-only access.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Regional pack URL ──
        item {
            SettingsGroupCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            Modifier.size(44.dp).clip(CuteCardDefaults.ButtonShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(FieldMindIcons.Download, null, tint = MaterialTheme.colorScheme.primary, size = 22.dp)
                        }
                        Column(Modifier.weight(1f)) {
                            Text("Species pack URL (advanced)", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Custom URL for regional species pack downloads. Only needed if hosting your own pack server.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedTextField(
                        value = modelBaseUrl,
                        onValueChange = settings::setSpeciesModelBaseUrl,
                        label = { Text("Pack base URL") },
                        placeholder = { Text("https://...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = CuteCardDefaults.FieldShape,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.withPrivacyTyping(LocalPrivacyTypingEnabled.current),
                        trailingIcon = {
                            if (LocalPrivacyTypingEnabled.current) {
                                PrivacyTypingIndicator()
                            }
                        },
                        supportingText = {
                            Text(
                                if (modelBaseUrl.isBlank()) "Default URL — packs list species, not models"
                                else "Using: $modelBaseUrl"
                            )
                        }
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Auto Generation Settings Page
// ══════════════════════════════════════════════════════════════════════

@Composable
private fun StatChip(value: String, label: String, color: Color) {
    Row(
        Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.7f))
    }
}


@Composable
fun AutoGenerationSettingsPage(
    viewModel: FieldMindViewModel,
    onBack: () -> Unit
) {
    val settings = viewModel.fieldSettings
    val autoFlashcards by settings.autoFlashcardsEnabled.collectAsState()
    val autoQuestions by settings.autoQuestionsEnabled.collectAsState()
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showErrorDialog by remember { mutableStateOf(false) }

    // Error dialog
    if (showErrorDialog && errorMessage != null) {
        AlertDialog(
            onDismissRequest = { 
                showErrorDialog = false
                errorMessage = null
            },
            title = { Text("Error") },
            text = { Text(errorMessage ?: "An unknown error occurred") },
            confirmButton = {
                Button(
                    onClick = { 
                        showErrorDialog = false
                        errorMessage = null
                    },
                    shape = CuteCardDefaults.ButtonShape
                ) { Text("OK") }
            }
        )
    }

    SettingsSubPage("Auto generation", icon = FieldMindIcons.Sparkle, onBack = onBack) {
        item {
            SettingsGroupCard {
                ToggleItem(
                    "Generate flashcards from observations",
                    "When enabled, new flashcards are automatically created from your observation data, notes, and sources as they come in.",
                    autoFlashcards,
                    { enabled ->
                        try {
                            settings.setAutoFlashcardsEnabled(enabled)
                        } catch (e: Exception) {
                            errorMessage = "Failed to toggle flashcard generation: ${e.message ?: "Unknown error"}"
                            showErrorDialog = true
                        }
                    },
                    FieldMindIcons.Flashcard
                )
            }
        }
        item {
            SettingsGroupCard {
                ToggleItem(
                    "Generate questions from observations",
                    "When enabled, research questions are automatically derived from your observation patterns and data.",
                    autoQuestions,
                    { enabled ->
                        try {
                            settings.setAutoQuestionsEnabled(enabled)
                        } catch (e: Exception) {
                            errorMessage = "Failed to toggle question generation: ${e.message ?: "Unknown error"}"
                            showErrorDialog = true
                        }
                    },
                    FieldMindIcons.Question
                )
            }
        }
        item {
            Card(shape = CuteCardDefaults.Shape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.size(36.dp).clip(RoundedCornerShape(18.dp)).background(FieldMindTheme.colors.info.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                            Icon(FieldMindIcons.Info, null, tint = FieldMindTheme.colors.info, size = 18.dp)
                        }
                        Text("Daily generation cap", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        """FieldMind limits auto-generation to 20 items (flashcards + questions combined) per day.
                        This prevents duplicate content loops and keeps your study queue manageable.
                        The counter resets automatically each day.""".trimIndent(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        item {
            Card(shape = CuteCardDefaults.Shape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("How it works", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Text(
                        """When enabled, auto-generation runs in the background after you add new observations.
                        It scans your data for key concepts, patterns, and facts, then builds flashcards and questions — all on your device, with no data leaving your phone.
                        You can always manually create flashcards and questions from the Library tab regardless of these settings.""".trimIndent(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Animation Settings Page — Speed preset, disable toggle
// ══════════════════════════════════════════════════════════════════════

@Composable
fun AnimationSettingsPage(viewModel: FieldMindViewModel, onBack: () -> Unit) {
    val settings = viewModel.fieldSettings
    val animationsEnabled by settings.animationsEnabled.collectAsState()
    val speedPreset by settings.animationSpeedPreset.collectAsState()

    SettingsSubPage("Animations", icon = MaterialSymbolIcon("motion_photos_on"), onBack = onBack) {
        item {
            SettingsGroupCard {
                ToggleItem(
                    "Enable animations",
                    "Disable to stop all entrance transitions, swipe-back gestures, and spring effects. Provides a static, instant-response experience.",
                    animationsEnabled,
                    settings::setAnimationsEnabled,
                    MaterialSymbolIcon("motion_photos_on")
                )
            }
        }

        // ── Visual Motion ─ Atmospheric scene liveness + whimsical touches ──
        item { SectionHeader("Visual motion", "Atmospheric scene liveness and whimsical touches") }
        item {
            val showCloudAnimation by settings.weatherShowCloudAnimation.collectAsState()
            val weatherBackgroundAnimation by settings.weatherBackgroundAnimationEnabled.collectAsState()
            SettingsGroupCard {
                Column(Modifier.padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ToggleItem(
                        "Background weather animation",
                        "Animated skybox, drifting clouds, fireflies, and atmospheric motion behind the weather widget. Turn off to render a static journal-themed gradient.",
                        weatherBackgroundAnimation,
                        settings::setWeatherBackgroundAnimationEnabled,
                        MaterialSymbolIcon("blur_on")
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                Modifier.size(28.dp).clip(CuteCardDefaults.ChipShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(MaterialSymbolIcon("cloud"), contentDescription = null, tint = MaterialTheme.colorScheme.primary, size = 16.dp)
                            }
                            Text("Cloud animations", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("Animated cloud effects in the weather widget. Turn off to render flat cloud icons.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(FieldMindIcons.Cloud, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 20.dp)
                                Text("Enable cloud animations", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                            Switch(checked = showCloudAnimation, onCheckedChange = { settings.setWeatherShowCloudAnimation(it) })
                        }
                    }
                }
            }
        }

        item { SectionHeader("Animation speed", "Controls how fast entrance animations play") }
        item {
            SettingsGroupCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Choose how quickly animations complete. Changes take effect immediately.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    listOf("Reduced", "Normal", "Enhanced").forEach { preset ->
                        val selected = speedPreset == preset
                        val icon = when (preset) {
                            "Reduced" -> MaterialSymbolIcon("slow_motion_video")
                            "Normal" -> MaterialSymbolIcon("speed")
                            else -> MaterialSymbolIcon("fast_forward")
                        }
                        Surface(
                            onClick = { settings.setAnimationSpeedPreset(preset) },
                            shape = CuteCardDefaults.ButtonShape,
                            color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = if (selected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    Modifier.size(44.dp).clip(CuteCardDefaults.ButtonShape)
                                        .background(
                                            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                            else MaterialTheme.colorScheme.surfaceContainerLow
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        icon,
                                        null,
                                        tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        size = 22.dp
                                    )
                                }
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        preset,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        when (preset) {
                                            "Reduced" -> "Gentle, minimal motion — 40% speed"
                                            "Normal" -> "Balanced spring physics — default speed"
                                            else -> "Fast, lively animations — 2x speed"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (selected) {
                                    Icon(
                                        FieldMindIcons.Check,
                                        null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        size = 20.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Interactive Animation Preview ──
        item { SectionHeader("Live preview", "See how each speed preset affects animation feel") }
        item {
            SpeedPresetAnimationPreview(
                speedPreset = speedPreset,
                animationsEnabled = animationsEnabled
            )
        }

        item {
            Card(
                shape = CuteCardDefaults.Shape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Fine-tuning",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "For granular control over damping, stiffness, and swipe thresholds for each animation type, open Developer Options → Animation Tuning.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Interactive Animation Preview — Demonstrates speed preset effect live
// ══════════════════════════════════════════════════════════════════════

@Composable
private fun SpeedPresetAnimationPreview(
    speedPreset: String,
    animationsEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val animConfig = LocalAnimationConfig.current
    val scope = rememberCoroutineScope()
    val colors = FieldMindTheme.colors

    val scaleAnim = remember { Animatable(0f) }
    val offsetXAnim = remember { Animatable(0f) }
    val offsetYAnim = remember { Animatable(0f) }
    val rotationAnim = remember { Animatable(0f) }

    var lastAction by remember { mutableStateOf("Tap to preview") }

    // Auto-play entrance when speed preset changes
    LaunchedEffect(speedPreset, animationsEnabled) {
        if (!animationsEnabled) return@LaunchedEffect
        lastAction = "Speed: " + speedPreset
        scaleAnim.snapTo(0f)
        offsetXAnim.snapTo(0f)
        offsetYAnim.snapTo(0f)
        rotationAnim.snapTo(0f)
        delay(150)
        scaleAnim.animateTo(1f, animConfig.entranceSpring())
        delay(400)
        scaleAnim.animateTo(0.95f, animConfig.entranceSpring())
        scaleAnim.animateTo(1f, animConfig.entranceSpring())
        delay(200)
        scaleAnim.animateTo(0f, animConfig.swipeBackSpring())
        lastAction = "Ready"
    }

    val speedLabel = when (speedPreset) {
        "Reduced" -> "40% speed — gentle, minimal motion"
        "Enhanced" -> "2× speed — fast, lively animations"
        else -> "Default speed — balanced spring physics"
    }
    val speedIcon = when (speedPreset) {
        "Reduced" -> MaterialSymbolIcon("slow_motion_video")
        "Normal" -> MaterialSymbolIcon("speed")
        else -> MaterialSymbolIcon("fast_forward")
    }

    Card(
        modifier = modifier.fillMaxWidth()
            .cuteShadow(elevation = CuteElevations.nonClickableTier, shape = CuteCardDefaults.Shape),
        shape = CuteCardDefaults.Shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(36.dp).clip(RoundedCornerShape(18.dp))
                            .background(colors.flashcard.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(speedIcon, null, tint = colors.flashcard, size = 20.dp)
                    }
                    Column {
                        Text(speedPreset, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(speedLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth().height(140.dp).clip(CuteCardDefaults.ShapeCompact)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceContainerHigh,
                                MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.size(72.dp).graphicsLayer {
                        scaleX = if (animationsEnabled) scaleAnim.value else 1f
                        scaleY = if (animationsEnabled) scaleAnim.value else 1f
                        translationX = if (animationsEnabled) offsetXAnim.value else 0f
                        translationY = if (animationsEnabled) offsetYAnim.value else 0f
                        rotationZ = if (animationsEnabled) rotationAnim.value else 0f
                    }.clip(CuteCardDefaults.Shape)
                        .background(
                            Brush.sweepGradient(
                                colors = listOf(colors.positive, colors.observation, colors.data, colors.positive)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(speedIcon, null, tint = Color.White, size = 34.dp)
                }

                if (!animationsEnabled) {
                    Box(
                        modifier = Modifier.matchParentSize()
                            .background(Color.Black.copy(alpha = 0.4f), CuteCardDefaults.ShapeCompact),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(MaterialSymbolIcon("motion_photos_paused"), null, tint = Color.White, size = 24.dp)
                            Text("Animations disabled", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(lastAction, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    val dartCount = when (speedPreset) { "Reduced" -> 1; "Normal" -> 2; else -> 3 }
                    repeat(dartCount) {
                        Icon(MaterialSymbolIcon("chevron_right"), null, tint = colors.flashcard, size = 16.dp)
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        lastAction = "Entrance — scale spring"
                        scope.launch {
                            scaleAnim.snapTo(0f); offsetXAnim.snapTo(0f); offsetYAnim.snapTo(0f); rotationAnim.snapTo(0f)
                            scaleAnim.animateTo(1f, animConfig.entranceSpring())
                        }
                    },
                    enabled = animationsEnabled,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                ) { Text("Entrance", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold) }

                OutlinedButton(
                    onClick = {
                        lastAction = "Swipe-back — snap spring"
                        scope.launch {
                            offsetXAnim.snapTo(0f); scaleAnim.snapTo(1f)
                            offsetXAnim.animateTo(160f, animConfig.swipeBackSpring())
                            offsetXAnim.animateTo(0f, animConfig.swipeBackSpring())
                        }
                    },
                    enabled = animationsEnabled,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                ) { Text("Swipe-back", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold) }

                OutlinedButton(
                    onClick = {
                        lastAction = "Rotation — spinner"
                        scope.launch {
                            rotationAnim.snapTo(0f); scaleAnim.snapTo(1f); offsetXAnim.snapTo(0f); offsetYAnim.snapTo(0f)
                            rotationAnim.animateTo(360f, animConfig.entranceSpring())
                            rotationAnim.snapTo(0f)
                        }
                    },
                    enabled = animationsEnabled,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                ) { Text("Spin", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold) }
            }
        }
    }
}


// ══════════════════════════════════════════════════════════════════════
//  Check for Updates Page — Manually run UpdateChecker.check + show result
// ══════════════════════════════════════════════════════════════════════

@Composable
fun CheckForUpdatesScreen(
    appSettings: fieldmind.research.app.shared.data.model.AppSettings,
    onBack: () -> Unit,
    onOpenChangelog: () -> Unit
) {
    val context = LocalContext.current
    val updateChecker = remember { fieldmind.research.app.infrastructure.updates.UpdateChecker(appSettings) }
    val updateInfo by updateChecker.updateInfo.collectAsState()
    val updateEnabled by appSettings.updateCheckEnabled.collectAsState()
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    // Helper to run a check
    fun refreshNow(force: Boolean = true) {
        if (!updateEnabled) {
            context.startActivity(android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(android.net.Uri.parse("package:${context.packageName}")))
            return
        }
        scope.launch {
            updateChecker.check(force = force)
        }
    }

    SettingsSubPage("Check for updates", icon = MaterialSymbolIcon("system_update"), onBack = onBack) {
        item { SectionHeader("Update status", "Run a manual check against the latest GitHub release") }

        item {
            val cardShape = CuteCardDefaults.Shape
            val cardColors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            Card(shape = cardShape, colors = cardColors, elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier), modifier = Modifier.fillMaxWidth().cuteShadow(elevation = CuteElevations.nonClickableTier, shape = cardShape)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // ── Status icon + title + body + action per UpdateInfo state ──
                when (val info = updateInfo) {
                    fieldmind.research.app.infrastructure.updates.UpdateInfo.Idle -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(44.dp).clip(CuteCardDefaults.ButtonShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                                Icon(MaterialSymbolIcon("system_update"), contentDescription = null, tint = MaterialTheme.colorScheme.primary, size = 24.dp)
                            }
                            Column(Modifier.weight(1f)) {
                                Text("Ready to check", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("Tap below to fetch the latest release from GitHub.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    fieldmind.research.app.infrastructure.updates.UpdateInfo.Loading -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(36.dp), strokeWidth = 3.dp, color = MaterialTheme.colorScheme.primary)
                            Column(Modifier.weight(1f)) {
                                Text("Checking for updates…", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("Reaching out to api.github.com", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    is fieldmind.research.app.infrastructure.updates.UpdateInfo.UpdateAvailable -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                            Box(Modifier.size(44.dp).clip(CuteCardDefaults.ButtonShape).background(FieldMindTheme.colors.positive.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
                                Icon(FieldMindIcons.Sparkle, contentDescription = null, tint = FieldMindTheme.colors.positive, size = 24.dp)
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("New release available", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = FieldMindTheme.colors.positive)
                                Text(info.versionName, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                                if (info.publishedAt.isNotBlank()) {
                                    Text("Published ${info.publishedAt}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (info.notes.isNotBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(info.notes.take(400) + if (info.notes.length > 400) "…" else "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                    fieldmind.research.app.infrastructure.updates.UpdateInfo.UpToDate -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(44.dp).clip(CuteCardDefaults.ButtonShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                                Icon(FieldMindIcons.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, size = 22.dp)
                            }
                            Column(Modifier.weight(1f)) {
                                Text("You're on the latest version", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("Your installation matches the newest release on GitHub.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    is fieldmind.research.app.infrastructure.updates.UpdateInfo.Unavailable -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(44.dp).clip(CuteCardDefaults.ButtonShape).background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                                Icon(MaterialSymbolIcon("info"), contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, size = 22.dp)
                            }
                            Column(Modifier.weight(1f)) {
                                Text("Update checker unavailable", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text(info.reason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    is fieldmind.research.app.infrastructure.updates.UpdateInfo.Errored -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(44.dp).clip(CuteCardDefaults.ButtonShape).background(MaterialTheme.colorScheme.errorContainer), contentAlignment = Alignment.Center) {
                                Icon(MaterialSymbolIcon("error"), contentDescription = null, tint = MaterialTheme.colorScheme.error, size = 22.dp)
                            }
                            Column(Modifier.weight(1f)) {
                                Text("Couldn't reach GitHub", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                                Text(info.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                // ── Action button row: Check now / Open release / View changelog ──
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    val isUpToDate = updateInfo is fieldmind.research.app.infrastructure.updates.UpdateInfo.UpToDate
                    val isAvailable = updateInfo is fieldmind.research.app.infrastructure.updates.UpdateInfo.UpdateAvailable
                    val isLoading = updateInfo is fieldmind.research.app.infrastructure.updates.UpdateInfo.Loading
                    Button(
                        onClick = { refreshNow(force = true) },
                        enabled = !isLoading,
                        shape = CuteCardDefaults.ButtonShape,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(MaterialSymbolIcon("refresh"), contentDescription = null, size = 18.dp)
                        Spacer(Modifier.width(6.dp))
                        Text(if (isUpToDate) "Check again" else "Check now")
                    }
                    if (isAvailable) {
                        val releaseUrl = (updateInfo as fieldmind.research.app.infrastructure.updates.UpdateInfo.UpdateAvailable).releaseUrl
                        OutlinedButton(
                            onClick = { runCatching { uriHandler.openUri(releaseUrl) } },
                            shape = CuteCardDefaults.ButtonShape,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(MaterialSymbolIcon("open_in_new"), contentDescription = null, size = 18.dp)
                            Spacer(Modifier.width(6.dp))
                            Text("Open release")
                        }
                    }
                }
                if (updateInfo is fieldmind.research.app.infrastructure.updates.UpdateInfo.UpdateAvailable) {
                    OutlinedButton(
                        onClick = onOpenChangelog,
                        shape = CuteCardDefaults.ButtonShape,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(FieldMindIcons.Info, contentDescription = null, size = 18.dp)
                        Spacer(Modifier.width(6.dp))
                        Text("View full changelog")
                    }
                }
                // ── Last-checked timestamp + auto-check toggle info ──
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(
                            if (updateEnabled) "Auto-check on launch is enabled" else "Auto-check on launch is disabled — toggle in Settings → Updates",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            }
        }
    }
}
