package fieldmind.research.app.features.field.presentation.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.scaleIn
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import fieldmind.research.app.features.field.presentation.components.FieldMindSnackbarProvider
import fieldmind.research.app.features.field.presentation.components.SwipeBackHost
import fieldmind.research.app.features.field.presentation.components.FieldMindIcons
import fieldmind.research.app.features.field.presentation.components.LocalSharedTransitionScope
import fieldmind.research.app.features.field.presentation.components.rememberFieldMindHaptics
import fieldmind.research.app.features.field.presentation.screens.*
import fieldmind.research.app.features.field.presentation.theme.FieldMindTheme
import fieldmind.research.app.features.field.data.database.entity.ResearchSessionEntity
import fieldmind.research.app.features.field.presentation.viewmodel.FieldMindViewModel
import fieldmind.research.app.shared.data.model.AppSettings
import fieldmind.research.app.shared.presentation.components.icons.Icon
import fieldmind.research.app.shared.presentation.components.icons.MaterialSymbolIcon
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import fieldmind.research.app.features.field.presentation.components.FieldMindMotion
import fieldmind.research.app.features.field.presentation.components.LocalPrivacyTypingEnabled
import fieldmind.research.app.features.field.presentation.components.PrivacyTextInputWrapper
import fieldmind.research.app.features.field.presentation.components.liquidGlassRefraction
import fieldmind.research.app.features.field.presentation.components.SwipeableAlertDialog
import fieldmind.research.app.features.field.presentation.components.PeekContentHolder
import fieldmind.research.app.features.field.presentation.components.AnimationConfig
import fieldmind.research.app.features.field.presentation.components.LocalAnimationConfig
import fieldmind.research.app.features.field.presentation.components.LocalPeekContentHolder
import androidx.activity.compose.BackHandler
import androidx.activity.ExperimentalActivityApi
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.CompositionLocalProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.math.abs

import fieldmind.research.app.features.field.presentation.utils.AppLifecycleManager
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

private fun formatElapsed(startedAt: Long): String {
    val ms = System.currentTimeMillis() - startedAt
    val totalSec = ms / 1000
    val hours = totalSec / 3600
    val minutes = (totalSec % 3600) / 60
    val seconds = totalSec % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds)
    else "%d:%02d".format(minutes, seconds)
}

/**
 * FieldMind destinations. Four primary lifecycle tabs (Today → Capture → Workspace → Library)
 * are surfaced in the navigation bar/rail; the remaining destinations are reached from within
 * those tabs, the capture FAB, or the overflow.
 */
sealed class FieldMindScreen(val route: String, val label: String, val icon: MaterialSymbolIcon) {
    data object Home : FieldMindScreen("field_today", "Today", FieldMindIcons.Today)
    data object Observe : FieldMindScreen("field_capture", "Capture", FieldMindIcons.Capture)
    data object Projects : FieldMindScreen("field_projects", "Workspace", FieldMindIcons.Projects)
    data object Library : FieldMindScreen("field_library", "Library", FieldMindIcons.Library)
    data object Insights : FieldMindScreen("field_insights", "Insights", FieldMindIcons.Insights)
    data object MapScreen : FieldMindScreen("field_map", "Map", FieldMindIcons.Map)
    data object ExportStudio : FieldMindScreen("field_export_studio", "Export", FieldMindIcons.Export)

    data object Learn : FieldMindScreen("field_learn", "Learn", FieldMindIcons.School)
    data object FieldMode : FieldMindScreen("field_mode", "Field Mode", FieldMindIcons.Bolt)
    data object Questions : FieldMindScreen("field_questions", "Questions", FieldMindIcons.Question)
    data object Hypotheses : FieldMindScreen("field_hypotheses", "Hypotheses", FieldMindIcons.Hypothesis)
    data object DataTools : FieldMindScreen("field_data_tools", "Data", FieldMindIcons.Data)
    data object Analysis : FieldMindScreen("field_analysis", "Analysis", FieldMindIcons.Trend)
    data object Reports : FieldMindScreen("field_reports", "Reports", FieldMindIcons.Report)
    data object Search : FieldMindScreen("field_search", "Search", FieldMindIcons.Search)
    data object Changelog : FieldMindScreen("field_changelog", "What's new", FieldMindIcons.Info)
    data object Progress : FieldMindScreen("field_progress", "Progress", FieldMindIcons.Check)
    data object Flashcards : FieldMindScreen("field_flashcards_session", "Review", FieldMindIcons.Flashcard)
    data object ResearchSession : FieldMindScreen("field_research_session", "Session", FieldMindIcons.Bolt)
    data object WeatherDatabase : FieldMindScreen("field_weather_database", "Weather", FieldMindIcons.Weather)
    data object Reader : FieldMindScreen("field_reader", "Reader", FieldMindIcons.Book)
    data object Settings : FieldMindScreen("field_settings", "Settings", FieldMindIcons.Settings)
    data object SettingsProfile : FieldMindScreen("field_settings_profile", "Profile", FieldMindIcons.Nature)
    data object SettingsAppearance : FieldMindScreen("field_settings_appearance", "Appearance", FieldMindIcons.Palette)
    data object SettingsEntityColors : FieldMindScreen("field_settings_entity_colors", "Entity Colors", MaterialSymbolIcon("palette"))
    data object SettingsCapture : FieldMindScreen("field_settings_capture", "Capture", FieldMindIcons.Capture)
    data object SettingsAi : FieldMindScreen("field_settings_ai", "AI Assistant", FieldMindIcons.Sparkle)
    data object SettingsLocalModel : FieldMindScreen("field_settings_local_model", "Local Model", FieldMindIcons.Download)
    data object SettingsBackup : FieldMindScreen("field_settings_backup", "Backup & Import", FieldMindIcons.Archive)
    data object SettingsSecurity : FieldMindScreen("field_settings_security", "Security", FieldMindIcons.Lock)
    data object SettingsScreenVisibility : FieldMindScreen("field_settings_screen_visibility", "Screen Visibility", FieldMindIcons.Visibility)
    data object SettingsAbout : FieldMindScreen("field_settings_about", "About", FieldMindIcons.Info)
    data object SettingsUnits : FieldMindScreen("field_settings_units", "Units", FieldMindIcons.Settings)
    data object SettingsWeather : FieldMindScreen("field_settings_weather", "Weather", FieldMindIcons.Weather)
    data object SettingsMap : FieldMindScreen("field_settings_map", "Map", FieldMindIcons.Map)
    data object SettingsDataIntegrity : FieldMindScreen("field_settings_data_integrity", "Data Integrity", FieldMindIcons.Archive)
    data object SettingsDeveloper : FieldMindScreen("field_settings_developer", "Developer", FieldMindIcons.Sparkle)
    data object SettingsSpeciesPacks : FieldMindScreen("field_settings_species_packs", "Species Packs", FieldMindIcons.Download)
    data object SettingsSpeciesId : FieldMindScreen("field_settings_species_id", "Species ID", FieldMindIcons.Nature)
    data object SettingsAutoGen : FieldMindScreen("field_settings_auto_gen", "Auto generation", FieldMindIcons.Sparkle)
    data object SettingsSecurityScore : FieldMindScreen("field_settings_security_score", "Security Score", MaterialSymbolIcon("security"))
    data object SettingsAnimationTuning : FieldMindScreen("field_settings_animation_tuning", "Animation Tuning", MaterialSymbolIcon("tune"))


    // ── Tasks screen ──
    data object Tasks : FieldMindScreen("field_tasks", "Tasks", MaterialSymbolIcon("checklist"))
    data object NewTask : FieldMindScreen("field_new_task", "New Task", MaterialSymbolIcon("checklist"))

    // ── Creation screens (converted from dialogs) ──
    data object NewProject : FieldMindScreen("field_new_project", "New Project", FieldMindIcons.Project)
    data object NewQuestion : FieldMindScreen("field_new_question", "New Question", FieldMindIcons.Question)
    data object NewHypothesis : FieldMindScreen("field_new_hypothesis", "New Hypothesis", FieldMindIcons.Hypothesis)
    data object NewDataRecord : FieldMindScreen("field_new_data_record", "New Data Record", FieldMindIcons.Data)
    data object NewReport : FieldMindScreen("field_new_report", "New Report", FieldMindIcons.Report)

    // Group 1: Interactive Data Tools
    data object CounterTool : FieldMindScreen("field_counter_tool", "Counter", FieldMindIcons.Add)
    data object MeasurementTool : FieldMindScreen("field_measurement_tool", "Measure", FieldMindIcons.Graph)
    data object WeatherLogTool : FieldMindScreen("field_weather_log_tool", "Weather", FieldMindIcons.Weather)
    data object SpeciesTool : FieldMindScreen("field_species_tool", "Species", FieldMindIcons.Nature)
    data object ChecklistTool : FieldMindScreen("field_checklist_tool", "Checklist", FieldMindIcons.Check)
    data object EventLogTool : FieldMindScreen("field_event_log_tool", "Event Log", FieldMindIcons.List)
    data object SiteLogTool : FieldMindScreen("field_site_log_tool", "Site Log", FieldMindIcons.Map)
    data object ComparisonTable : FieldMindScreen("field_comparison_table", "Comparison", FieldMindIcons.Data)
    data object SpeciesBrowser : FieldMindScreen("field_species_browser", "Species Browser", FieldMindIcons.Nature)
    data object TaxonomicBrowser : FieldMindScreen("field_taxonomic_browser", "Taxonomic Browser", FieldMindIcons.Category)
    data object FieldLog : FieldMindScreen("field_log", "Field Log", FieldMindIcons.List)
    data object TimerTool : FieldMindScreen("field_timer", "Timer", FieldMindIcons.Timer)

    // ── Task detail screen ──
    data object TaskDetail : FieldMindScreen("field_task_detail/{taskId}", "Task", MaterialSymbolIcon("checklist"))

    // ── Question detail screen ──
    data object QuestionDetail : FieldMindScreen("field_question_detail/{questionId}", "Question", FieldMindIcons.Question)

    // ── Project detail screen ──
    data object ProjectDetail : FieldMindScreen("field_project_detail/{projectId}", "Project", FieldMindIcons.Project)

    // ── Hypothesis detail screen ──
    data object HypothesisDetail : FieldMindScreen("field_hypothesis_detail/{hypothesisId}", "Hypothesis", FieldMindIcons.Hypothesis)

    // ── Project relations screen ──
    data object ProjectRelations : FieldMindScreen("field_project_relations/{projectId}", "Relations", MaterialSymbolIcon("hub"))

    // ── Project settings screen ──
    data object ProjectSettings : FieldMindScreen("field_project_settings/{projectId}", "Project Settings", MaterialSymbolIcon("settings"))

    // ── New entity screens (converted from dialogs) ──
    data object NewObservation : FieldMindScreen("field_new_observation", "New Observation", FieldMindIcons.Observation)
    data object NewNote : FieldMindScreen("field_new_note", "New Note", FieldMindIcons.Note)
    data object NewSource : FieldMindScreen("field_new_source", "New Source", FieldMindIcons.Source)
    data object NewAttachment : FieldMindScreen("field_new_attachment", "Add Attachment", MaterialSymbolIcon("attach_file"))
    data object NewFolder : FieldMindScreen("field_new_folder", "New Folder", MaterialSymbolIcon("folder"))

    // ── Canvas note editor ──
    data object Canvas : FieldMindScreen("field_canvas/{noteId}", "Canvas", MaterialSymbolIcon("dashboard_customize"))
}

private val bottomTabs = listOf(
    FieldMindScreen.Home,
    FieldMindScreen.Observe,
    FieldMindScreen.Projects,
    FieldMindScreen.Insights,
    FieldMindScreen.Library
)

@Composable
fun FieldMindApp(appSettings: AppSettings, viewModel: FieldMindViewModel, requestedDestination: String? = null) {
    val onboardingCompleted by appSettings.onboardingCompleted.collectAsState()
    var appUnlocked by remember { mutableStateOf(!viewModel.fieldSettings.privacyLockEnabled.value) }
    var isDecoyMode by remember { mutableStateOf(false) }
    val privacyEnabled by viewModel.fieldSettings.privacyLockEnabled.collectAsState()
    LaunchedEffect(privacyEnabled) { if (!privacyEnabled) appUnlocked = true }
    // Observe auto-lock from AppLifecycleManager
    val shouldAutoLock by AppLifecycleManager.shouldShowLock.collectAsState()
    LaunchedEffect(shouldAutoLock) {
        if (shouldAutoLock) {
            appUnlocked = false
        }
    }

    if (!onboardingCompleted) {
        FieldMindOnboardingScreen(
            settings = viewModel.fieldSettings,
            onFinish = { appSettings.setOnboardingCompleted(true) }
        )
    } else {
        FieldMindAppLock(
            settings = viewModel.fieldSettings,
            isUnlocked = appUnlocked,
            isDecoyMode = isDecoyMode,
            onUnlock = { appUnlocked = true },
            onDecoyUnlock = { isDecoyMode = true }
        ) {
            val privacyTyping by viewModel.fieldSettings.privacyTypingEnabled.collectAsState()
            CompositionLocalProvider(LocalPrivacyTypingEnabled provides privacyTyping) {
                PrivacyTextInputWrapper {
                    FieldMindSnackbarProvider { _ ->
                        FieldMindNavigation(viewModel = viewModel, requestedDestination = requestedDestination, onResetOnboarding = { appSettings.setOnboardingCompleted(false); appUnlocked = false })
                    }
                }
            }
        }
    }
}

/** Navigate to a non-tab destination. Does NOT use restoreState to avoid the
 * "NavBackStackEntry destroyed" crash that occurs when the navigation library
 * tries to access a destroyed entry's ViewModelStore during state restoration. */
private fun NavHostController.navigateToDestination(route: String) {
    navigate(route) {
        launchSingleTop = true
        // Intentionally no restoreState — prevents crash when navigating back
        // to a destination whose NavBackStackEntry ViewModel was already disposed.
    }
}

@Composable
fun FieldMindNavigation(viewModel: FieldMindViewModel, requestedDestination: String? = null, onResetOnboarding: () -> Unit) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val currentRoute = currentDestination?.route
    val haptics = rememberFieldMindHaptics()

    // ── Active tab index — tabs are rendered simultaneously inside TabContentHost ──
    var activeTabIndex by remember { mutableIntStateOf(0) }

    // Observe screen visibility settings so nav bar reflects user customizations
    val screenVisibility by viewModel.fieldSettings.screenVisibility.collectAsState()

    // Filter bottom tabs based on user's screen visibility preferences
    val visibleTabs = remember(bottomTabs, screenVisibility) {
        bottomTabs.filter { tab ->
            when (tab.route) {
                FieldMindScreen.Observe.route -> screenVisibility.showCapture
                FieldMindScreen.Projects.route -> screenVisibility.showProjects
                FieldMindScreen.FieldMode.route -> true
                FieldMindScreen.Insights.route -> screenVisibility.showInsights
                FieldMindScreen.Library.route -> screenVisibility.showLibrary
                else -> true // Home always visible
            }
        }
    }

    // Positive-list approach: only show bottom nav on the tab container route.
    val showChrome = currentRoute == "field_tab_container" && !(activeTabIndex == 1 && viewModel.captureSessionActive)
    val hideChrome = !showChrome

    // ── Capture session navigation guard ──
    var showNavigateConfirm by remember { mutableStateOf(false) }
    var pendingNavRoute by remember { mutableStateOf<String?>(null) }

    fun navigateToTab(index: Int) {
        if (index == activeTabIndex) return

        // Protect against accidental navigation while a capture session is active
        if (activeTabIndex == 1 && viewModel.captureSessionActive) {
            pendingNavRoute = visibleTabs.getOrNull(index)?.route
            showNavigateConfirm = true
            return
        }
        activeTabIndex = index
    }

    LaunchedEffect(requestedDestination) {
        when (requestedDestination) {
            FieldMindScreen.FieldMode.route, "field_mode" -> navController.navigateToDestination(FieldMindScreen.FieldMode.route)
            "field_timer" -> navController.navigateToDestination(FieldMindScreen.ResearchSession.route)
        }
    }

    fun isSelected(screen: FieldMindScreen) =
        visibleTabs.getOrNull(activeTabIndex) == screen

    // ── Navigation confirmation dialog (for active capture session) ──
    if (showNavigateConfirm) {
        SwipeableAlertDialog(
            onDismissRequest = {
                showNavigateConfirm = false
                pendingNavRoute = null
            },
            icon = { Icon(icon = FieldMindIcons.Info, contentDescription = null, size = 28.dp) },
            title = { Text("Active capture session") },
            text = {
                Text(
                    "You have an active observation session with unsaved data. Navigate away and lose your progress?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setCaptureSessionActive(false)
                        showNavigateConfirm = false
                        pendingNavRoute?.let { route ->
                            val targetIndex = visibleTabs.indexOfFirst { it.route == route }
                            if (targetIndex >= 0) activeTabIndex = targetIndex
                        }
                        pendingNavRoute = null
                    },
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Discard & navigate") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showNavigateConfirm = false
                    pendingNavRoute = null
                }) { Text("Stay on Capture") }
            }
        )
    }

    // ── HazeState for backdrop blur on the floating nav pill ──
    val hazeState = remember { HazeState() }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val expanded = maxWidth >= 840.dp
        if (expanded) {
            Row(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                // Tablet rail — liquid-glass side panel. .hazeChild blurs the
                // NavHost content behind the rail (captured via .haze() below);
                // .liquidGlassRefraction() applies GPU displacement & specular.
                if (!hideChrome) {
                    Surface(
                        shape = RoundedCornerShape(size = 38.dp),
                        color = Color.Transparent,
                        tonalElevation = 0.dp,
                        shadowElevation = 12.dp,
                        border = androidx.compose.foundation.BorderStroke(
                            width = 0.6.dp,
                            color = if (isSystemInDarkTheme())
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)
                            else
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier
                            .padding(start = 8.dp, top = 8.dp, bottom = 8.dp)
                            .width(IntrinsicSize.Min)
                            .hazeChild(
                                state = hazeState,
                                style = HazeStyle(
                                    blurRadius = 32.dp,
                                    noiseFactor = 0.06f,
                                    tints = listOf(
                                        HazeTint(
                                            color = if (isSystemInDarkTheme())
                                                MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.88f)
                                            else
                                                Color(0xFFFFF5E6).copy(alpha = 0.78f)
                                        )
                                    )
                                )
                            )
                            .liquidGlassRefraction()
                    ) {
                        NavigationRail(
                            header = {
                                Spacer(Modifier.height(8.dp))
                            }
                        ) {
                            visibleTabs.forEach { screen ->
                                val selected = isSelected(screen)
                                                RailNavTabItem(
                                    screen = screen,
                                    selected = selected,
                                    onClick = {
                                        val idx = visibleTabs.indexOf(screen)
                                        if (idx >= 0) { haptics.light(); navigateToTab(idx) }
                                    }
                                )
                            }
                        }
                    }
                }
                // Content — blur source (captured by Haze for the rail's glass)
                FieldMindNavHost(
                    navController = navController,
                    viewModel = viewModel,
                    onResetOnboarding = onResetOnboarding,
                    visibleTabs = visibleTabs,
                    onNavigateToTabRoute = null,
                    activeTabIndex = activeTabIndex,
                    onActiveTabChange = { index -> activeTabIndex = index },
                    modifier = Modifier.weight(1f).haze(state = hazeState)
                )
            }
        } else {
            // ── True floating overlay nav bar with liquid glass effect ──
            // We use a raw Box instead of Scaffold so Android never draws a
            // solid rectangular bottom-bar background behind the pill.
            // The content fills the full screen edge-to-edge; the pill is
            // overlaid at the bottom with real backdrop blur via Haze.
            //
            // IMPORTANT: .haze() is ONLY on the NavHost content, NOT the outer
            // Box — this ensures the pill and its shadow/glow layers are never
            // captured into the blur source, preventing visible layer artifacts.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Content — fills full screen edge-to-edge (blur source ONLY)
                FieldMindNavHost(
                    navController = navController,
                    viewModel = viewModel,
                    onResetOnboarding = onResetOnboarding,
                    visibleTabs = visibleTabs,
                    onNavigateToTabRoute = null,
                    activeTabIndex = activeTabIndex,
                    onActiveTabChange = { index -> activeTabIndex = index },
                    modifier = Modifier.fillMaxSize().haze(state = hazeState)
                )

                // Floating pill — liquid glass with Haze blur + GPU refraction
                if (!hideChrome) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .wrapContentHeight(align = Alignment.Bottom)
                    ) {
                        // Glassmorphic nav pill — real backdrop blur via Haze with
                        // GPU liquid-glass displacement, specular highlights, and
                        // Fresnel edge glow via the .liquidGlassRefraction() modifier.
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = Color.Transparent,
                            tonalElevation = 0.dp,
                            shadowElevation = 16.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp)
                                .clip(RoundedCornerShape(50.dp))
                                .hazeChild(
                                    state = hazeState,
                                    style = HazeStyle(
                                        blurRadius = 32.dp,
                                        noiseFactor = 0.06f,
                                        tints = listOf(
                                            HazeTint(
                                                color = if (isSystemInDarkTheme())
                                                    MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.88f)
                                                else
                                                    Color(0xFFFFF5E6).copy(alpha = 0.78f)
                                            )
                                        )
                                    )
                                )
                                .liquidGlassRefraction()
                        ) {
                            LiquidNavRow(
                                visibleTabs = visibleTabs,
                                isSelected = { screen -> isSelected(screen) },
                                onTabClick = { screen ->
                                    val idx = visibleTabs.indexOf(screen)
                                    if (idx >= 0) { haptics.light(); navigateToTab(idx) }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}



/** Actual per-tab position+width from onGloballyPositioned, used for
 * precise blob centering regardless of Arrangement inter-item spacing. */
private data class TabBounds(val x: Float, val width: Float)

/**
 * Liquid glassmorphism nav row with animated blob indicator.
 * Draws a fluid rounded pill that slides between active tab positions
 * with spring physics, creating the "liquid/blob" micro-interaction.
 */
@Composable
private fun LiquidNavRow(
    visibleTabs: List<FieldMindScreen>,
    isSelected: (FieldMindScreen) -> Boolean,
    onTabClick: (FieldMindScreen) -> Unit
) {
    // Calculate selected index directly (no remember wrapper — isSelected lambda
    // captures currentDestination and changes every frame)
    val selectedIndex = visibleTabs.indexOfFirst { isSelected(it) }.coerceAtLeast(0)

    // ── Animate the indicator position with spring physics for liquid feel ──
    val animatedPosition = remember { Animatable(0f) }
    val animSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessVeryLow
    )
    LaunchedEffect(selectedIndex) {
        animatedPosition.animateTo(selectedIndex.toFloat(), animSpec)
    }

    // ── Item bounds tracked via onGloballyPositioned ──
    // positionInRoot() (minus parent's root position) gives the actual
    // x-offset within the Row, which automatically accounts for
    // Arrangement.SpaceEvenly inter-item gaps.
    val tabBounds = remember { mutableStateListOf<TabBounds>() }

    // Capture color scheme in composable scope (Canvas DrawScope is not composable)
    val blobColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        // ── Liquid blob indicator drawn behind the tabs ──
        // (Canvas is drawn first, so it appears behind the Row)
        // Uses actual per-tab x-position (from onGloballyPositioned) for
        // precise centering regardless of inter-item spacing, with smooth
        // interpolation of both position and width between tab stops.
        if (tabBounds.isNotEmpty() && selectedIndex < tabBounds.size) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val pos = animatedPosition.value.coerceIn(0f, (tabBounds.size - 1).toFloat())
                val leftIdx = pos.toInt().coerceIn(0, tabBounds.size - 1)
                val rightIdx = (leftIdx + 1).coerceAtMost(tabBounds.size - 1)
                val fraction = pos - leftIdx

                val leftBounds = tabBounds[leftIdx]
                val rightBounds = tabBounds.getOrElse(rightIdx) { leftBounds }

                // Interpolate center position
                val leftCenter = leftBounds.x + leftBounds.width / 2f
                val rightCenter = rightBounds.x + rightBounds.width / 2f
                val centerX = if (leftIdx == rightIdx) leftCenter
                    else leftCenter + (rightCenter - leftCenter) * fraction

                // Also interpolate width smoothly between tabs so the blob
                // doesn't abruptly jump when selection changes mid-animation.
                val indicatorWidth = leftBounds.width + (rightBounds.width - leftBounds.width) * fraction
                val indicatorHeight = size.height * 0.82f
                val indicatorY = (size.height - indicatorHeight) / 2f

                drawRoundRect(
                    color = blobColor,
                    topLeft = Offset(centerX - indicatorWidth / 2f, indicatorY),
                    size = Size(indicatorWidth, indicatorHeight),
                    cornerRadius = CornerRadius(indicatorHeight / 2f, indicatorHeight / 2f)
                )
            }
        }

        // ── Tab items drawn on top of the indicator ──
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            visibleTabs.forEachIndexed { index, screen ->
                val selected = isSelected(screen)
                var isPressed by remember { mutableStateOf(false) }

                val pressScale by animateFloatAsState(
                    targetValue = if (isPressed && !selected) 0.92f else 1f,
                    animationSpec = if (isPressed)
                        tween(durationMillis = FieldMindMotion.durationMicro, easing = FastOutSlowInEasing)
                    else
                        FieldMindMotion.expressiveSpring,
                    label = "tabScale_$index"
                )

                Column(
                    modifier = Modifier
                        .onGloballyPositioned { coordinates ->
                            val width = coordinates.size.width.toFloat()
                            // positionInParent() was removed from Compose. Calculate
                            // relative position by subtracting parent's root position.
                            // positionInRoot/positionInWindow were removed in Compose BOM 2026.05.01.
                            // Use localToWindow(Offset.Zero) to compute relative x-position.
                            val childWindow = coordinates.localToWindow(Offset.Zero)
                            val parentWindow = coordinates.parentCoordinates?.localToWindow(Offset.Zero) ?: Offset.Zero
                            val x = (childWindow - parentWindow).x
                            if (tabBounds.size <= index) {
                                while (tabBounds.size <= index) tabBounds.add(TabBounds(0f, 0f))
                            }
                            tabBounds[index] = TabBounds(x, width)
                        }
                        .clip(RoundedCornerShape(30.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                isPressed = true
                                onTabClick(screen)
                            }
                        )
                        .graphicsLayer {
                            scaleX = pressScale
                            scaleY = pressScale
                        }
                        .defaultMinSize(minWidth = 60.dp, minHeight = 56.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier.size(width = 48.dp, height = 36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon = if (selected) screen.icon.copy(filled = true) else screen.icon,
                            contentDescription = screen.label,
                            tint = if (selected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                            size = if (selected) 28.dp else 22.dp,
                            weight = if (selected) 500 else 400
                        )
                    }
                    Text(
                        screen.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                        maxLines = 1
                    )
                }

                // Reset press state when selection changes
                LaunchedEffect(selected) {
                    if (selected) isPressed = false
                }
            }
        }
    }
}

/**
 * Rail nav tab item used in the side rail (tablet layout).
 * Mirrors the floating island style: filled rounded background for active tab.
 */
@Composable
private fun RailNavTabItem(
    screen: FieldMindScreen,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .defaultMinSize(minHeight = 48.dp)
            .background(
                color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            icon = if (selected) screen.icon.copy(filled = true) else screen.icon,
            contentDescription = screen.label,
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            size = if (selected) 26.dp else 22.dp
        )
        Text(
            screen.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            maxLines = 1
        )
    }
}


private fun primaryTabDirection(fromRoute: String?, toRoute: String?): Int {
    val fromIndex = bottomTabs.indexOfFirst { it.route == fromRoute }
    val toIndex = bottomTabs.indexOfFirst { it.route == toRoute }
    if (fromIndex == -1 || toIndex == -1 || fromIndex == toIndex) return 0
    return if (toIndex > fromIndex) 1 else -1
}

private enum class RouteCategory { Tab, SettingsHub, SettingsSubPage, Detail, Tool, Creation, Other }

private fun categorizeRoute(route: String): RouteCategory = when (route) {
    "field_tab_container" -> RouteCategory.Tab
    FieldMindScreen.Settings.route -> RouteCategory.SettingsHub
    FieldMindScreen.MapScreen.route, FieldMindScreen.ExportStudio.route,
    FieldMindScreen.Reader.route -> RouteCategory.Other
    else -> when {
        route.startsWith("field_settings") -> RouteCategory.SettingsSubPage
        route.startsWith("field_detail/") -> RouteCategory.Detail
        route.startsWith("field_new_") -> RouteCategory.Creation
        route in listOf(
            FieldMindScreen.CounterTool.route, FieldMindScreen.MeasurementTool.route,
            FieldMindScreen.WeatherLogTool.route, FieldMindScreen.SpeciesTool.route,
            FieldMindScreen.ChecklistTool.route, FieldMindScreen.EventLogTool.route,
            FieldMindScreen.SiteLogTool.route, FieldMindScreen.ComparisonTable.route,
            FieldMindScreen.SpeciesBrowser.route, FieldMindScreen.TaxonomicBrowser.route,
            FieldMindScreen.FieldLog.route, FieldMindScreen.TimerTool.route,
            FieldMindScreen.Flashcards.route, FieldMindScreen.ResearchSession.route,
            FieldMindScreen.WeatherDatabase.route
        ) -> RouteCategory.Tool
        else -> RouteCategory.Other
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.routeEnterTransition(
    animConfig: AnimationConfig? = null
): EnterTransition {
    val fromRoute = initialState.destination.route ?: ""
    val toRoute = targetState.destination.route ?: ""
    val fromCat = categorizeRoute(fromRoute)
    val toCat = categorizeRoute(toRoute)
    val damping = animConfig?.entranceDampingRatio ?: 0.88f
    val stiffness = animConfig?.entranceStiffness ?: 200f
    val slideSpec = spring<IntOffset>(dampingRatio = damping, stiffness = stiffness * 0.5f)
    val fadeSpec = spring<Float>(dampingRatio = damping, stiffness = stiffness)

    return when {
        fromCat == RouteCategory.Tab && toCat == RouteCategory.Tab -> {
            val direction = primaryTabDirection(fromRoute, toRoute)
            if (direction == 0)
                fadeIn(animationSpec = FieldMindMotion.expressiveFloat) +
                scaleIn(initialScale = 0.97f, animationSpec = FieldMindMotion.expressiveFloat)
            else
                slideInHorizontally(slideSpec) { direction * it / 8 } + fadeIn(fadeSpec)
        }
        fromCat == RouteCategory.Tab && toCat in listOf(
            RouteCategory.SettingsHub, RouteCategory.SettingsSubPage,
            RouteCategory.Tool, RouteCategory.Detail, RouteCategory.Creation,
            RouteCategory.Other
        ) -> slideInHorizontally(slideSpec) { it / 8 } + fadeIn(fadeSpec)
        fromCat == RouteCategory.SettingsHub && toCat == RouteCategory.SettingsSubPage ->
            fadeIn(animationSpec = fadeSpec)
        fromCat == RouteCategory.SettingsSubPage && toCat == RouteCategory.SettingsHub ->
            fadeIn(animationSpec = fadeSpec)
        toCat == RouteCategory.Tab && fromCat in listOf(
            RouteCategory.SettingsHub, RouteCategory.SettingsSubPage,
            RouteCategory.Tool, RouteCategory.Detail, RouteCategory.Creation, RouteCategory.Other
        ) -> slideInHorizontally(slideSpec) { -it / 8 } + fadeIn(fadeSpec)
        else -> fadeIn(animationSpec = fadeSpec) +
            scaleIn(initialScale = 0.97f, animationSpec = FieldMindMotion.expressiveFloat)
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.routeExitTransition(
    animConfig: AnimationConfig? = null
): ExitTransition {
    val fromRoute = initialState.destination.route ?: ""
    val toRoute = targetState.destination.route ?: ""
    val fromCat = categorizeRoute(fromRoute)
    val toCat = categorizeRoute(toRoute)
    val damping = animConfig?.entranceDampingRatio ?: 0.88f
    val stiffness = animConfig?.entranceStiffness ?: 200f
    val slideSpec = spring<IntOffset>(dampingRatio = damping, stiffness = stiffness * 0.5f)
    val fadeSpec = spring<Float>(dampingRatio = damping, stiffness = stiffness)

    return when {
        fromCat == RouteCategory.Tab && toCat == RouteCategory.Tab -> {
            val direction = primaryTabDirection(fromRoute, toRoute)
            if (direction == 0) fadeOut(fadeSpec)
            else {
                slideOutHorizontally(slideSpec) { -direction * it / 8 } + fadeOut(fadeSpec)
            }
        }
        fromCat == RouteCategory.Tab && toCat in listOf(
            RouteCategory.SettingsHub, RouteCategory.SettingsSubPage,
            RouteCategory.Tool, RouteCategory.Detail, RouteCategory.Creation,
            RouteCategory.Other
        ) -> {
            slideOutHorizontally(slideSpec) { -it / 8 } + fadeOut(fadeSpec)
        }
        fromCat == RouteCategory.SettingsHub && toCat == RouteCategory.SettingsSubPage ->
            fadeOut(fadeSpec)
        fromCat == RouteCategory.SettingsSubPage && toCat == RouteCategory.SettingsHub ->
            fadeOut(fadeSpec)
        toCat == RouteCategory.Tab && fromCat in listOf(
            RouteCategory.SettingsHub, RouteCategory.SettingsSubPage,
            RouteCategory.Tool, RouteCategory.Detail, RouteCategory.Creation, RouteCategory.Other
        ) -> {
            slideOutHorizontally(slideSpec) { it / 8 } + fadeOut(fadeSpec)
        }
        else -> fadeOut(fadeSpec)
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.routePopEnterTransition(
    animConfig: AnimationConfig? = null
): EnterTransition {
    val fromRoute = initialState.destination.route ?: ""
    val toRoute = targetState.destination.route ?: ""
    val fromCat = categorizeRoute(fromRoute)
    val toCat = categorizeRoute(toRoute)
    val damping = animConfig?.entranceDampingRatio ?: 0.88f
    val stiffness = animConfig?.entranceStiffness ?: 200f
    val slideSpec = spring<IntOffset>(dampingRatio = damping, stiffness = stiffness * 0.5f)
    val fadeSpec = spring<Float>(dampingRatio = damping, stiffness = stiffness)

    return when {
        fromCat == RouteCategory.Tab && toCat == RouteCategory.Tab -> {
            val direction = primaryTabDirection(toRoute, fromRoute)
            // Full-width slide from the opposite side
            slideInHorizontally(slideSpec) { -direction * it } + fadeIn(animationSpec = fadeSpec)
        }
        toCat == RouteCategory.Tab && fromCat in listOf(
            RouteCategory.SettingsHub, RouteCategory.SettingsSubPage,
            RouteCategory.Tool, RouteCategory.Detail, RouteCategory.Creation,
            RouteCategory.Other
        ) -> {
            // Previous screen (tab) slides in from the left at full width — iOS predictive peek
            slideInHorizontally(slideSpec) { -it } + fadeIn(animationSpec = fadeSpec)
        }
        toCat == RouteCategory.SettingsHub && fromCat == RouteCategory.SettingsSubPage ->
            fadeIn(animationSpec = fadeSpec)
        else -> slideInHorizontally(slideSpec) { -it } + fadeIn(animationSpec = fadeSpec)
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.routePopExitTransition(
    animConfig: AnimationConfig? = null
): ExitTransition {
    val fromRoute = initialState.destination.route ?: ""
    val toRoute = targetState.destination.route ?: ""
    val fromCat = categorizeRoute(fromRoute)
    val toCat = categorizeRoute(toRoute)
    val damping = animConfig?.entranceDampingRatio ?: 0.88f
    val stiffness = animConfig?.entranceStiffness ?: 200f
    val slideSpec = spring<IntOffset>(dampingRatio = damping, stiffness = stiffness * 0.5f)
    val fadeSpec = spring<Float>(dampingRatio = damping, stiffness = stiffness)

    return when {
        fromCat == RouteCategory.Tab && toCat == RouteCategory.Tab -> {
            val direction = primaryTabDirection(toRoute, fromRoute)
            // Full-width slide out in the direction of the pop
            slideOutHorizontally(slideSpec) { direction * it } + fadeOut(animationSpec = fadeSpec)
        }
        fromCat == RouteCategory.Tab && toCat in listOf(
            RouteCategory.SettingsHub, RouteCategory.SettingsSubPage,
            RouteCategory.Tool, RouteCategory.Detail, RouteCategory.Creation,
            RouteCategory.Other
        ) -> {
            // Current screen (tab) slides out to the right at full width
            slideOutHorizontally(slideSpec) { it } + fadeOut(animationSpec = fadeSpec)
        }
        toCat == RouteCategory.Tab && fromCat in listOf(
            RouteCategory.SettingsHub, RouteCategory.SettingsSubPage,
            RouteCategory.Tool, RouteCategory.Detail, RouteCategory.Creation,
            RouteCategory.Other
        ) -> {
            // Current screen (sub-screen) slides out to the right at full width — iOS predictive
            slideOutHorizontally(slideSpec) { it } + fadeOut(animationSpec = fadeSpec)
        }
        else -> fadeOut(animationSpec = fadeSpec)
    }
}

@Composable
private fun FieldMindNavHost(
    navController: NavHostController,
    viewModel: FieldMindViewModel,
    onResetOnboarding: () -> Unit,
    modifier: Modifier = Modifier,
    visibleTabs: List<FieldMindScreen> = emptyList(),
    onNavigateToTabRoute: ((String) -> Unit)? = null,
    activeTabIndex: Int = 0,
    onActiveTabChange: ((Int) -> Unit)? = null
) {
    var readerTarget by remember { mutableStateOf("" to "") }
    val openDetail: (String, Long) -> Unit = { kind, id ->
        when (kind) {
            "question" -> navController.navigateToDestination("field_question_detail/$id")
            "task" -> navController.navigateToDestination("field_task_detail/$id")
            "species" -> navController.navigateToDestination("field_species_detail/$id")
            "hypothesis" -> navController.navigateToDestination("field_hypothesis_detail/$id")
            else -> navController.navigateToDestination("field_detail/$kind/$id")
        }
    }
    val openReader: (String, String) -> Unit = { url: String, title: String ->
        readerTarget = url to title
        navController.navigateToDestination(FieldMindScreen.Reader.route)
    }

    // ── Real-content peek cache (ScreenCache) ──
    // Tracks the previous route and keeps its composable alive inside SwipeBackHost
    // via PeekContentHolder + CompositionLocal, so the predictive back gesture
    // shows the ACTUAL previous screen composable (with real data, same ViewModel)
    // instead of mock placeholder cards.
    val backStackEntry by navController.currentBackStackEntryAsState()
    val previousRoute = remember(backStackEntry) {
        navController.previousBackStackEntry?.destination?.route
    }
    val peekHolder = remember { PeekContentHolder() }
    LaunchedEffect(previousRoute) {
        // Include all routes in the cache, including tab container.
        // For tab container, RouteContent renders the Home screen so
        // the predictive back peek shows REAL tab content instead of
        // a mock placeholder card.
        val route = previousRoute
        val isCachable = route != null
        peekHolder.peekKey = if (isCachable) route else null
        peekHolder.peekContent = if (isCachable) {
            { RouteContent(route!!, viewModel) }
        } else null
    }

    // ── Read animation config from settings and provide via CompositionLocal ──
    // Uses derivedStateOf so all 8 animation state flows are reactively tracked.
    val animConfig by remember {
        derivedStateOf {
            viewModel.fieldSettings.currentAnimationConfig()
        }
    }

    SharedTransitionLayout(modifier = modifier) {
        val composableScope = this
        CompositionLocalProvider(
            LocalSharedTransitionScope provides composableScope,
            LocalPeekContentHolder provides peekHolder,
            LocalAnimationConfig provides animConfig
        ) {
            NavHost(
            navController = navController,
            startDestination = "field_tab_container",
            modifier = Modifier,
            enterTransition = { routeEnterTransition(animConfig) },
            exitTransition = { routeExitTransition(animConfig) },
            popEnterTransition = { routePopEnterTransition(animConfig) },
            popExitTransition = { routePopExitTransition(animConfig) }
        ) {
            // ── Single tab container: all 5 tabs rendered simultaneously ──
            // Swipe gestures reveal the real adjacent tab content behind the current one.
            // No placeholder mock UI — the actual adjacent tab composable is visible through peek.
            composable("field_tab_container") {
                AllTabScreen(
                    sharedTransitionScope = composableScope,
                    activeTabIndex = activeTabIndex,
                    onTabSelected = { index -> onActiveTabChange?.invoke(index) },
                    viewModel = viewModel,
                    visibleTabs = visibleTabs,
                    openDetail = openDetail,
                    openReader = openReader,
                    onOpenSettings = { navController.navigateToDestination(FieldMindScreen.Settings.route) },
                    onOpenCanvas = { viewModel.addNote(title = "Canvas", body = "", category = "Other", tags = "canvas") { noteId -> navController.navigateToDestination("field_canvas/$noteId") } },
                    onNavigateToDestination = { route -> navController.navigateToDestination(route) },
                    onPopBackStack = { navController.popBackStack() }
                )
            }
            composable(FieldMindScreen.Learn.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { FieldMindLearnScreen(viewModel = viewModel, onBack = { navController.popBackStack() }, onOpenReader = openReader) } }
            composable(FieldMindScreen.Reader.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { LearnReaderScreen(url = readerTarget.first, title = readerTarget.second, onBack = { navController.popBackStack() }) } }
            composable(FieldMindScreen.FieldMode.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { ObserveScreen(viewModel = viewModel, compactFieldMode = true, onBack = { navController.popBackStack() }, onOpenDetail = openDetail) } }
            composable(FieldMindScreen.Questions.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { QuestionsScreen(viewModel = viewModel, onOpenDetail = openDetail) } }
            composable(FieldMindScreen.Hypotheses.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { QuestionsScreen(viewModel = viewModel, onOpenDetail = openDetail) } }
            composable(FieldMindScreen.DataTools.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { DataToolsHubScreen(viewModel = viewModel, onBack = { navController.popBackStack() }, onNavigate = { navController.navigateToDestination(it.route) }, onOpenDetail = openDetail) } }
            composable(FieldMindScreen.Analysis.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { ProjectsScreen(viewModel = viewModel, startTab = 0, onOpenDetail = { _, id -> navController.navigateToDestination("field_project_detail/$id") }, onNavigate = { navController.navigateToDestination(it.route) }) } }
            composable(FieldMindScreen.Reports.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { FieldMindReportScreen(viewModel = viewModel, onBack = { navController.popBackStack() }) } }
            composable(FieldMindScreen.Search.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { ArchiveScreen(viewModel = viewModel, onBack = { navController.popBackStack() }, onOpenDetail = openDetail, onOpenReader = openReader) } }
            composable(FieldMindScreen.MapScreen.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { MapFieldScreen(viewModel = viewModel, onBack = { navController.popBackStack() }, onNavigate = { navController.navigateToDestination(it.route) }, onOpenDetail = openDetail) } }
            composable(FieldMindScreen.ExportStudio.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { BackupAndRestoreScreen(viewModel = viewModel, onBack = { navController.popBackStack() }) } }
            composable(FieldMindScreen.Changelog.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { FieldMindChangelogScreen(onBack = { navController.popBackStack() }) } }
            composable(FieldMindScreen.Progress.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { InsightsScreen(viewModel = viewModel, onBack = { navController.popBackStack() }, onNavigate = { navController.navigateToDestination(it.route) }, onOpenDetail = openDetail) } }
            composable(FieldMindScreen.Flashcards.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { FlashcardSessionScreen(viewModel = viewModel, onBack = { navController.popBackStack() }) } }
            composable(FieldMindScreen.ResearchSession.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { ResearchSessionScreen(viewModel = viewModel, onBack = { navController.popBackStack() }, onOpenDetail = openDetail) } }
            composable(FieldMindScreen.WeatherDatabase.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { WeatherDatabaseScreen(viewModel = viewModel, onBack = { navController.popBackStack() }, onOpenSettings = { navController.navigateToDestination(FieldMindScreen.SettingsWeather.route) }, onOpenDetail = openDetail) } }
            composable(FieldMindScreen.Settings.route) {
                SwipeBackHost(onBack = { navController.popBackStack() }) {
                    FieldMindSettingsScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onResetOnboarding = onResetOnboarding,
                        onOpenExport = { navController.navigateToDestination(FieldMindScreen.ExportStudio.route) },
                        onOpenAbout = { navController.navigateToDestination(FieldMindScreen.SettingsAbout.route) },
                        onOpenProfile = { navController.navigateToDestination(FieldMindScreen.SettingsProfile.route) },
                        onOpenAppearance = { navController.navigateToDestination(FieldMindScreen.SettingsAppearance.route) },
                        onOpenCapture = { navController.navigateToDestination(FieldMindScreen.SettingsCapture.route) },
                        onOpenWeather = { navController.navigateToDestination(FieldMindScreen.SettingsWeather.route) },
                        onOpenAi = { navController.navigateToDestination(FieldMindScreen.SettingsAi.route) },
                        onOpenLocalModel = { navController.navigateToDestination(FieldMindScreen.SettingsLocalModel.route) },
                        onOpenBackup = { navController.navigateToDestination(FieldMindScreen.ExportStudio.route) },
                        onOpenSecurity = { navController.navigateToDestination(FieldMindScreen.SettingsSecurity.route) },
                        onOpenChangelog = { navController.navigateToDestination(FieldMindScreen.Changelog.route) },
                        onOpenUnits = { navController.navigateToDestination(FieldMindScreen.SettingsUnits.route) },
                        onOpenScreenVisibility = { navController.navigateToDestination(FieldMindScreen.SettingsScreenVisibility.route) },
                        onOpenMap = { navController.navigateToDestination(FieldMindScreen.SettingsMap.route) },
                        onOpenDataIntegrity = { navController.navigateToDestination(FieldMindScreen.SettingsDataIntegrity.route) },
                        onOpenDeveloper = { navController.navigateToDestination(FieldMindScreen.SettingsDeveloper.route) },
                        onOpenSpeciesPacks = { navController.navigateToDestination(FieldMindScreen.SettingsSpeciesPacks.route) },
                        onOpenSpeciesId = { navController.navigateToDestination(FieldMindScreen.SettingsSpeciesId.route) },
                        onOpenAutoGen = { navController.navigateToDestination(FieldMindScreen.SettingsAutoGen.route) }
                    )
                }
            }
            composable(FieldMindScreen.SettingsProfile.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { ProfileSettingsPage(viewModel = viewModel, onBack = { navController.popBackStack() }) } }
            composable(FieldMindScreen.SettingsAppearance.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { AppearanceSettingsPage(viewModel = viewModel, onBack = { navController.popBackStack() }, onOpenEntityColors = { navController.navigateToDestination(FieldMindScreen.SettingsEntityColors.route) }) } }
            composable(FieldMindScreen.SettingsEntityColors.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { EntityAccentColorsPage(settings = viewModel.fieldSettings, onBack = { navController.popBackStack() }) } }
            composable(FieldMindScreen.SettingsCapture.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { CaptureDefaultsSettingsPage(viewModel = viewModel, onBack = { navController.popBackStack() }) } }
            composable(FieldMindScreen.SettingsAi.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { AiAssistantSettingsPage(viewModel = viewModel, onBack = { navController.popBackStack() }) } }
            composable(FieldMindScreen.SettingsLocalModel.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { LocalModelSettingsPage(viewModel = viewModel, onBack = { navController.popBackStack() }) } }
            composable(FieldMindScreen.SettingsBackup.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { BackupImportSettingsPage(viewModel = viewModel, onBack = { navController.popBackStack() }, onOpenExport = { navController.navigateToDestination(FieldMindScreen.ExportStudio.route) }) } }
            composable(FieldMindScreen.SettingsSecurity.route) {
                SwipeBackHost(onBack = { navController.popBackStack() }) {
                    SecuritySettingsPage(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onOpenSecurityScore = { navController.navigateToDestination(FieldMindScreen.SettingsSecurityScore.route) }
                    )
                }
            }
            composable(FieldMindScreen.SettingsSecurityScore.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { SecurityScoreDetailPage(settings = viewModel.fieldSettings, onBack = { navController.popBackStack() }) } }
            composable(FieldMindScreen.SettingsScreenVisibility.route) {
                SwipeBackHost(onBack = { navController.popBackStack() }) {
                    ScreenVisibilitySettingsPage(viewModel = viewModel, onBack = { navController.popBackStack() })
                }
            }
            composable(FieldMindScreen.SettingsAbout.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { AboutPage(onBack = { navController.popBackStack() }, onOpenChangelog = { navController.navigateToDestination(FieldMindScreen.Changelog.route) }) } }
            composable(FieldMindScreen.SettingsUnits.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { UnitsFormatSettingsPage(viewModel = viewModel, onBack = { navController.popBackStack() }) } }
            composable(FieldMindScreen.SettingsWeather.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { WeatherSettingsPage(viewModel = viewModel, onBack = { navController.popBackStack() }) } }
            composable(FieldMindScreen.SettingsMap.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { MapSettingsPage(viewModel = viewModel, onBack = { navController.popBackStack() }) } }
            composable(FieldMindScreen.SettingsDataIntegrity.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { DataIntegritySettingsPage(viewModel = viewModel, onBack = { navController.popBackStack() }) } }
            composable(FieldMindScreen.SettingsDeveloper.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { DeveloperSettingsPage(viewModel = viewModel, onBack = { navController.popBackStack() }, onOpenAnimationTuning = { navController.navigateToDestination(FieldMindScreen.SettingsAnimationTuning.route) }) } }
            composable(FieldMindScreen.SettingsAnimationTuning.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { AnimationTuningSettingsPage(settings = viewModel.fieldSettings, onBack = { navController.popBackStack() }) } }
            composable(FieldMindScreen.SpeciesBrowser.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { SpeciesBrowserScreen(onBack = { navController.popBackStack() }, onOpenDetail = { id -> navController.navigateToDestination("field_species_detail/$id") }) } }
            composable(FieldMindScreen.TaxonomicBrowser.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { TaxonomicBrowserScreen(onBack = { navController.popBackStack() }, onOpenDetail = { id -> navController.navigateToDestination("field_species_detail/$id") }) } }
            composable("field_species_detail/{speciesId}") { entry ->
                val speciesId = entry.arguments?.getString("speciesId") ?: ""
                SwipeBackHost(onBack = { navController.popBackStack() }) {
                    SpeciesDetailScreen(speciesId = speciesId, onBack = { navController.popBackStack() })
                }
            }
            composable(FieldMindScreen.SettingsSpeciesPacks.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { SpeciesPackSettingsPage(onBack = { navController.popBackStack() }) } }
            composable(FieldMindScreen.SettingsSpeciesId.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { SpeciesIdentificationSettingsPage(viewModel = viewModel, onBack = { navController.popBackStack() }) } }
            composable(FieldMindScreen.SettingsAutoGen.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { AutoGenerationSettingsPage(viewModel = viewModel, onBack = { navController.popBackStack() }) } }
            composable(FieldMindScreen.CounterTool.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { CounterToolScreen(viewModel = viewModel, onBack = { navController.popBackStack() }) } }
            composable(FieldMindScreen.MeasurementTool.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { MeasurementToolScreen(viewModel = viewModel, onBack = { navController.popBackStack() }) } }
            composable(FieldMindScreen.WeatherLogTool.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { WeatherLogToolScreen(viewModel = viewModel, onBack = { navController.popBackStack() }) } }
            composable(FieldMindScreen.NewProject.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { NewProjectScreen(viewModel = viewModel, onBack = { navController.popBackStack() }) } }
            composable(FieldMindScreen.NewQuestion.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { NewQuestionScreen(viewModel = viewModel, onBack = { navController.popBackStack() }) } }
            composable(FieldMindScreen.NewHypothesis.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { NewHypothesisScreen(viewModel = viewModel, onBack = { navController.popBackStack() }) } }
            composable(FieldMindScreen.NewDataRecord.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { NewDataRecordScreen(viewModel = viewModel, onBack = { navController.popBackStack() }) } }
            composable(FieldMindScreen.Tasks.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { TasksScreen(viewModel = viewModel, onBack = { navController.popBackStack() }, onOpenDetail = openDetail, onNavigate = { route -> navController.navigateToDestination(route) }) } }
            composable(FieldMindScreen.NewTask.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { NewTaskScreen(viewModel = viewModel, onBack = { navController.popBackStack() }) } }
            composable(FieldMindScreen.NewReport.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { NewReportScreen(viewModel = viewModel, onBack = { navController.popBackStack() }) } }
            composable(FieldMindScreen.NewObservation.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { NewObservationScreen(viewModel = viewModel, onBack = { navController.popBackStack() }) } }
            composable(FieldMindScreen.NewNote.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { NewNoteScreen(viewModel = viewModel, onBack = { navController.popBackStack() }) } }
            composable(FieldMindScreen.NewSource.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { NewSourceScreen(viewModel = viewModel, onBack = { navController.popBackStack() }) } }
            composable(FieldMindScreen.NewAttachment.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { NewAttachmentScreen(viewModel = viewModel, onBack = { navController.popBackStack() }) } }
            composable(FieldMindScreen.NewFolder.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { NewFolderScreen(viewModel = viewModel, onBack = { navController.popBackStack() }) } }
            composable(FieldMindScreen.SpeciesTool.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { SpeciesToolScreen(viewModel = viewModel, onBack = { navController.popBackStack() }, onOpenBrowser = { navController.navigateToDestination(FieldMindScreen.SpeciesBrowser.route) }, onOpenTaxonomicBrowser = { navController.navigateToDestination(FieldMindScreen.TaxonomicBrowser.route) }) } }
            composable(FieldMindScreen.ChecklistTool.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { ChecklistToolScreen(viewModel = viewModel, onBack = { navController.popBackStack() }) } }
            composable(FieldMindScreen.EventLogTool.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { EventLogToolScreen(viewModel = viewModel, onBack = { navController.popBackStack() }) } }
            composable(FieldMindScreen.SiteLogTool.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { SiteLogToolScreen(viewModel = viewModel, onBack = { navController.popBackStack() }) } }
            composable(FieldMindScreen.ComparisonTable.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { ComparisonTableScreen(viewModel = viewModel, onBack = { navController.popBackStack() }) } }
            composable(FieldMindScreen.TimerTool.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { TimerToolScreen(onBack = { navController.popBackStack() }) } }
            composable("field_task_detail/{taskId}") { entry ->
                val taskId = entry.arguments?.getString("taskId")?.toLongOrNull() ?: 0L
                SwipeBackHost(onBack = { navController.popBackStack() }) {
                    TaskDetailScreen(
                        taskId = taskId,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onOpenDetail = openDetail
                    )
                }
            }
            composable("field_question_detail/{questionId}") { entry ->
                val questionId = entry.arguments?.getString("questionId")?.toLongOrNull() ?: 0L
                SwipeBackHost(onBack = { navController.popBackStack() }) {
                    QuestionDetailScreen(
                        questionId = questionId,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onOpenDetail = openDetail
                    )
                }
            }
            composable("field_project_detail/{projectId}") { entry ->
                val projectId = entry.arguments?.getString("projectId")?.toLongOrNull() ?: 0L
                SwipeBackHost(onBack = { navController.popBackStack() }) {
                    ProjectDetailScreen(
                        projectId = projectId,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onOpenDetail = openDetail,
                        onNavigate = { navController.navigateToDestination(it.route) },
                        onOpenRelations = { navController.navigateToDestination("field_project_relations/$projectId") },
                        onOpenSettings = { id -> navController.navigateToDestination("field_project_settings/$id") }
                    )
                }
            }
            composable("field_project_relations/{projectId}") { entry ->
                val projectId = entry.arguments?.getString("projectId")?.toLongOrNull() ?: 0L
                SwipeBackHost(onBack = { navController.popBackStack() }) {
                    ProjectRelationsScreen(
                        projectId = projectId,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onOpenDetail = openDetail
                    )
                }
            }
            composable("field_project_settings/{projectId}") { entry ->
                val projectId = entry.arguments?.getString("projectId")?.toLongOrNull() ?: 0L
                SwipeBackHost(onBack = { navController.popBackStack() }) {
                    ProjectSettingsScreen(
                        projectId = projectId,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onOpenBackupSettings = { navController.navigateToDestination(FieldMindScreen.SettingsBackup.route) }
                    )
                }
            }
            composable("field_canvas/{noteId}") { entry ->
                val noteId = entry.arguments?.getString("noteId")?.toLongOrNull() ?: 0L
                SwipeBackHost(onBack = { navController.popBackStack() }) {
                    CanvasScreen(
                        noteId = noteId,
                        fieldViewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onOpenLinkedEntity = { kind, id ->
                            navController.navigateToDestination("field_detail/$kind/$id")
                        }
                    )
                }
            }
            composable(FieldMindScreen.FieldLog.route) { SwipeBackHost(onBack = { navController.popBackStack() }) { FieldLogScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onOpenDetail = openDetail,
                onOpenExport = { navController.navigateToDestination(FieldMindScreen.ExportStudio.route) }
            ) } }
            composable("field_detail/{kind}/{id}") { entry ->
                val kind = entry.arguments?.getString("kind") ?: "observation"
                val id = entry.arguments?.getString("id")?.toLongOrNull() ?: 0L
                SwipeBackHost(onBack = { navController.popBackStack() }) {
                    DetailScreen(
                        kind = kind,
                        id = id,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onOpenDetail = openDetail,
                        onOpenReader = openReader,
                        onOpenCanvas = { noteId ->
                            navController.navigateToDestination("field_canvas/$noteId")
                        }
                    )
                }
            }
        } // end NavHost
        } // end CompositionLocalProvider
    } // end SharedTransitionLayout
}

/**
 * Renders a single tab screen at a given offset/scale/alpha.
 * Used by [AllTabScreen] to position tabs in a horizontal stack.
 */
@Composable
private fun TabContentBox(
    screen: FieldMindScreen,
    offsetX: Float,
    scale: Float,
    alpha: Float,
    userInputEnabled: Boolean,
    viewModel: FieldMindViewModel,
    openDetail: (String, Long) -> Unit,
    openReader: (String, String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCanvas: (Long) -> Unit,
    onNavigateToDestination: (String) -> Unit,
    onPopBackStack: () -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    entranceProgress: Float = 1f, // 0 = just became active (scale up + fade in), 1 = fully entered
    visibleTabs: List<FieldMindScreen> = emptyList(),
    onTabSelected: ((Int) -> Unit)? = null
) {
    val onNav: (FieldMindScreen) -> Unit = { screen ->
        val tabIdx = visibleTabs.indexOf(screen)
        if (tabIdx >= 0) {
            onTabSelected?.invoke(tabIdx)
        } else {
            onNavigateToDestination(screen.route)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset { IntOffset(offsetX.roundToInt(), 0) }
            .graphicsLayer {
                // ── Entrance animation for newly active tab ──
                // When a tab becomes active via tapping (entranceProgress animates 0→1),
                // the content scales up from 0.95 and fades in from alpha 0.7,
                // creating a smooth "pop" entrance. For swipe gestures the entrance
                // animation is fast enough (spring, ~300ms) to blend naturally with
                // the slide animation driven by animX.
                val entranceScale = 0.95f + 0.05f * entranceProgress
                val entranceAlpha = 0.7f + 0.3f * entranceProgress
                scaleX = scale * entranceScale
                scaleY = scale * entranceScale
                this.alpha = alpha * entranceAlpha
                clip = true
            }
            .then(
                if (!userInputEnabled) {
                    Modifier.pointerInput(screen.route) {
                        awaitPointerEventScope {
                            while (true) {
                                // Consume all pointer events so inactive tabs
                                // behind the active tab cannot capture touches
                                awaitPointerEvent(androidx.compose.ui.input.pointer.PointerEventPass.Main)
                            }
                        }
                    }
                } else Modifier
            )
    ) {
        when (screen) {
            FieldMindScreen.Home -> {
                with(sharedTransitionScope ?: return@Box) {
                    HomeScreen(
                        viewModel = viewModel,
                        onOpenSettings = onOpenSettings,
                        onNavigate = onNav,
                        onOpenDetail = openDetail,
                        onOpenReader = openReader,
                        onOpenCanvas = { onOpenCanvas(0L) }
                    )
                }
            }
            FieldMindScreen.Observe -> {
                with(sharedTransitionScope ?: return@Box) {
                    ObserveScreen(
                        viewModel = viewModel,
                        onBack = onPopBackStack,
                        onOpenDetail = openDetail
                    )
                }
            }
            FieldMindScreen.Projects -> {
                with(sharedTransitionScope ?: return@Box) {
                    ProjectsScreen(
                        viewModel = viewModel,
                        onOpenDetail = { _, id -> onNavigateToDestination("field_project_detail/$id") },
                        onStartSession = { onNavigateToDestination(FieldMindScreen.ResearchSession.route) },
                        onNavigate = onNav
                    )
                }
            }
            FieldMindScreen.Insights -> {
                with(sharedTransitionScope ?: return@Box) {
                    InsightsScreen(
                        viewModel = viewModel,
                        onBack = onPopBackStack,
                        onNavigate = onNav,
                        onOpenDetail = openDetail
                    )
                }
            }
            FieldMindScreen.Library -> {
                with(sharedTransitionScope ?: return@Box) {
                    KnowledgeLibraryScreen(
                        viewModel = viewModel,
                        onNavigate = onNav,
                        onOpenDetail = openDetail,
                        onOpenReader = openReader
                    )
                }
            }
            else -> {}
        }
    }
}

/**
 * Renders all visible tabs simultaneously in a horizontal stack.
 * The active tab is on top and interactive; adjacent tabs sit behind it.
 * During a swipe gesture, the active tab slides to reveal the REAL adjacent
 * tab content — no mock placeholder cards or labels.
 */
@OptIn(ExperimentalActivityApi::class)
@Composable
private fun AllTabScreen(
    activeTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    viewModel: FieldMindViewModel,
    visibleTabs: List<FieldMindScreen>,
    openDetail: (String, Long) -> Unit,
    openReader: (String, String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCanvas: (Long) -> Unit,
    onNavigateToDestination: (String) -> Unit,
    onPopBackStack: () -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null
) {
    val reduceMotion = FieldMindMotion.isReduceMotion()
    val animX = remember { Animatable(0f) }
    var contentWidth by remember { mutableFloatStateOf(1f) }
    val scope = rememberCoroutineScope()
    val haptics = rememberFieldMindHaptics()

    val isFirstTab = activeTabIndex == 0

    // ── Tab entrance animation (scale-up + fade-in on tap-switch) ──
    // When a tab is activated via tapping (not swiping), the content
    // smoothly scales up from 0.95 and fades in from alpha 0.7 using
    // spring physics for a polished "pop" entrance.
    val tabEntranceProgress = remember { Animatable(1f) }
    var lastActiveIndex by remember { mutableIntStateOf(activeTabIndex) }
    val animConfig = fieldmind.research.app.features.field.presentation.components.LocalAnimationConfig.current
    LaunchedEffect(activeTabIndex) {
        if (activeTabIndex != lastActiveIndex) {
            lastActiveIndex = activeTabIndex
            tabEntranceProgress.snapTo(0f)
            tabEntranceProgress.animateTo(
                1f,
                animationSpec = animConfig.tabEntranceSpring()
            )
        }
    }

    // ── Device back button: previous tab, or exit on first tab ──
    BackHandler(enabled = !isFirstTab) {
        onTabSelected(activeTabIndex - 1)
    }

    // ── System back gesture (left edge): handle all tabs ──
    // First tab: predictive peek → exit app on commit (full screen reveal).
    // Other tabs: predictive peek → show REAL adjacent tab content behind current
    // tab (reveals 60% of previous tab), then switch to it on commit.
    //
    // ── Predictive back gesture (system swipe from left edge) ──
    // Drives animX to reveal the previous tab behind the current one.
    // Uses the system's PredictiveBackHandler instead of a custom gesture overlay
    // to avoid breaking taps, clicks, and vertical scrolls in tab content.
    PredictiveBackHandler(enabled = !reduceMotion) { progressFlow ->
        try {
            // First tab: reveal full screen behind (exit). Other tabs: reveal 60% of previous.
            val maxOffset = if (isFirstTab) contentWidth else contentWidth * 0.6f
            progressFlow.collect { backEvent ->
                val offset = (maxOffset * backEvent.progress).coerceAtLeast(0f)
                animX.snapTo(offset)
            }
            // Gesture committed — snap back before navigating
            animX.snapTo(0f)
            haptics.confirm()
            if (isFirstTab) {
                onPopBackStack()
            } else {
                onTabSelected(activeTabIndex - 1)
            }
        } catch (_: CancellationException) {
            // Gesture cancelled — spring animation back to 0
            scope.launch {
                animX.animateTo(
                    0f,
                    animationSpec = spring(
                        dampingRatio = 0.9f,
                        stiffness = 200f
                    )
                )
            }
        }
    }

    // ── Determine if we can swipe left/right ──
    val canSwipeLeft = activeTabIndex < visibleTabs.size - 1
    val canSwipeRight = activeTabIndex > 0
    val hasSwipeDirection = canSwipeLeft || canSwipeRight

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coords ->
                contentWidth = coords.size.width.toFloat().coerceAtLeast(1f)
            }
            .then(
                if (!reduceMotion && hasSwipeDirection) {
                    // Tab swipe gesture on the content layer — uses
                    // detectHorizontalDragGestures which properly distinguishes
                    // taps from drags; taps pass through to interactive content.
                    Modifier.pointerInput(activeTabIndex, visibleTabs.size) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                val maxSwipe = contentWidth * 0.35f
                                val rawTarget = animX.value + dragAmount
                                val clampedTarget = when {
                                    rawTarget > 0f && !canSwipeRight -> 0f
                                    rawTarget < 0f && !canSwipeLeft -> 0f
                                    else -> rawTarget.coerceIn(-maxSwipe, maxSwipe)
                                }
                                scope.launch { animX.snapTo(clampedTarget) }
                            },
                            onDragEnd = {
                                val threshold = contentWidth * 0.18f
                                if (animX.value > threshold && canSwipeRight) {
                                    haptics.confirm()
                                    scope.launch { animX.snapTo(0f) }
                                    onTabSelected(activeTabIndex - 1)
                                } else if (animX.value < -threshold && canSwipeLeft) {
                                    haptics.confirm()
                                    scope.launch { animX.snapTo(0f) }
                                    onTabSelected(activeTabIndex + 1)
                                } else {
                                    scope.launch {
                                        animX.animateTo(
                                            0f,
                                            animationSpec = spring(
                                                dampingRatio = 0.85f,
                                                stiffness = 300f
                                            )
                                        )
                                    }
                                }
                            },
                            onDragCancel = {
                                scope.launch {
                                    animX.animateTo(
                                        0f,
                                        animationSpec = spring(
                                            dampingRatio = 0.85f,
                                            stiffness = 300f
                                        )
                                    )
                                }
                            }
                        )
                    }
                } else {
                    Modifier
                }
            )
    ) {
        // ── Render all tabs in a horizontal stack ──
        // Non-active tabs rendered FIRST (behind in z-order), active tab LAST (on top).
        val swipeProgress = abs(animX.value / contentWidth).coerceIn(0f, 1f)

        // Phase 1: Non-active tabs (behind)
        visibleTabs.forEachIndexed { index, screen ->
            if (index == activeTabIndex) return@forEachIndexed
            val isLeftAdjacent = index == activeTabIndex - 1
            val isRightAdjacent = index == activeTabIndex + 1
            val isAdjacent = isLeftAdjacent || isRightAdjacent
            val adjAlpha = 0.94f + 0.06f * (1f - swipeProgress)

            TabContentBox(
                screen = screen,
                offsetX = when {
                    isLeftAdjacent -> -(contentWidth) + animX.value
                    isRightAdjacent -> contentWidth + animX.value
                    index < activeTabIndex -> -(contentWidth * 2f)
                    else -> contentWidth * 2f
                },
                scale = if (isAdjacent) 0.94f else 1f,
                alpha = if (isAdjacent) adjAlpha else 0f,
                userInputEnabled = false,
                viewModel = viewModel,
                openDetail = openDetail,
                openReader = openReader,
                onOpenSettings = onOpenSettings,
                onOpenCanvas = onOpenCanvas,
                onNavigateToDestination = onNavigateToDestination,
                onPopBackStack = onPopBackStack,
                sharedTransitionScope = sharedTransitionScope,
                entranceProgress = 1f // non-active tabs don't animate entrance
            )
        }

        // Phase 2: Active tab (on top) — with entrance animation from tapping
        TabContentBox(
            screen = visibleTabs[activeTabIndex],
            offsetX = animX.value,
            scale = 1f - swipeProgress * 0.08f,
            alpha = 1f,
            userInputEnabled = true,
            viewModel = viewModel,
            openDetail = openDetail,
            openReader = openReader,
            onOpenSettings = onOpenSettings,
            onOpenCanvas = onOpenCanvas,
            onNavigateToDestination = onNavigateToDestination,
            onPopBackStack = onPopBackStack,
            sharedTransitionScope = sharedTransitionScope,
            entranceProgress = tabEntranceProgress.value,
            visibleTabs = visibleTabs,
            onTabSelected = onTabSelected
        )
    }
}

/**
 * Dispatches a route string to its screen composable for the real-content peek
 * preview (ScreenCache). Called by [SwipeBackHost] via [PeekContentHolder] when
 * rendering the previous screen behind the current one during the predictive
 * back gesture. All interaction callbacks are empty no-ops because the peek
 * composable is never interactive (the current screen captures all touches).
 *
 * This lives in the navigation package alongside the [NavHost] route definitions
 * to keep the route→screen mapping in a single source of truth.
 */
@Composable
private fun RouteContent(route: String, viewModel: FieldMindViewModel) {
    val noop: () -> Unit = {}
    val noopDetail: (String, Long) -> Unit = { _, _ -> }
    val noopNav: (FieldMindScreen) -> Unit = {}
    val noopReader: (String, String) -> Unit = { _, _ -> }
    val noopStr: (String) -> Unit = {}
    val noopLong: (Long) -> Unit = {}

    when {
        // ── Tab container: render the Home screen for peek preview ──
        route == "field_tab_container" -> {
            val scope = LocalSharedTransitionScope.current
            if (scope != null) {
                with(scope) {
                    HomeScreen(
                        viewModel = viewModel,
                        onOpenSettings = noop,
                        onNavigate = noopNav,
                        onOpenDetail = noopDetail,
                        onOpenReader = noopReader,
                        onOpenCanvas = noop
                    )
                }
            }
        }

        // ── Dynamic routes (parameterised) ──
        route.startsWith("field_task_detail/") -> {
            val taskId = route.removePrefix("field_task_detail/").toLongOrNull() ?: return
            TaskDetailScreen(taskId = taskId, viewModel = viewModel, onBack = noop, onOpenDetail = noopDetail)
        }
        route.startsWith("field_question_detail/") -> {
            val id = route.removePrefix("field_question_detail/").toLongOrNull() ?: return
            QuestionDetailScreen(questionId = id, viewModel = viewModel, onBack = noop, onOpenDetail = noopDetail)
        }
        route.startsWith("field_project_detail/") -> {
            val id = route.removePrefix("field_project_detail/").toLongOrNull() ?: return
            ProjectDetailScreen(projectId = id, viewModel = viewModel, onBack = noop, onOpenDetail = noopDetail, onNavigate = null, onOpenRelations = noop, onOpenSettings = noopLong)
        }
        route.startsWith("field_project_relations/") -> {
            val id = route.removePrefix("field_project_relations/").toLongOrNull() ?: return
            ProjectRelationsScreen(projectId = id, viewModel = viewModel, onBack = noop, onOpenDetail = noopDetail)
        }
        route.startsWith("field_project_settings/") -> {
            val id = route.removePrefix("field_project_settings/").toLongOrNull() ?: return
            ProjectSettingsScreen(projectId = id, viewModel = viewModel, onBack = noop, onOpenBackupSettings = noop)
        }
        route.startsWith("field_canvas/") -> {
            val id = route.removePrefix("field_canvas/").toLongOrNull() ?: return
            CanvasScreen(noteId = id, fieldViewModel = viewModel, onBack = noop, onOpenLinkedEntity = null)
        }
        route.startsWith("field_species_detail/") -> {
            val speciesId = route.removePrefix("field_species_detail/")
            with(LocalSharedTransitionScope.current ?: return) {
                SpeciesDetailScreen(speciesId = speciesId, onBack = noop)
            }
        }
        route.startsWith("field_detail/") -> {
            // Routes like "field_detail/observation/42" → kind="observation", id=42
            val parts = route.removePrefix("field_detail/").split("/")
            if (parts.size < 2) return
            val kind = parts[0]
            val id = parts[1].toLongOrNull() ?: return
            with(LocalSharedTransitionScope.current ?: return) {
                DetailScreen(kind = kind, id = id, viewModel = viewModel, onBack = noop, onOpenDetail = noopDetail, onOpenReader = noopReader, onOpenCanvas = noopLong)
            }
        }

        // ── Exact-match static routes ──
        route == FieldMindScreen.Learn.route -> FieldMindLearnScreen(viewModel = viewModel, onBack = noop, onOpenReader = noopReader)
        route == FieldMindScreen.Reader.route -> LearnReaderScreen(url = "", title = "", onBack = noop)
        route == FieldMindScreen.FieldMode.route -> ObserveScreen(viewModel = viewModel, compactFieldMode = true, onBack = noop, onOpenDetail = noopDetail)
        route == FieldMindScreen.Questions.route -> QuestionsScreen(viewModel = viewModel, onOpenDetail = noopDetail)
        route == FieldMindScreen.Hypotheses.route -> QuestionsScreen(viewModel = viewModel, onOpenDetail = noopDetail)
        route == FieldMindScreen.DataTools.route -> DataToolsHubScreen(viewModel = viewModel, onBack = noop, onNavigate = noopNav, onOpenDetail = noopDetail)
        route == FieldMindScreen.Analysis.route -> ProjectsScreen(viewModel = viewModel, startTab = 0, onOpenDetail = noopDetail, onNavigate = noopNav)
        route == FieldMindScreen.Reports.route -> FieldMindReportScreen(viewModel = viewModel, onBack = noop)
        route == FieldMindScreen.Search.route -> ArchiveScreen(viewModel = viewModel, onBack = noop, onOpenDetail = noopDetail, onOpenReader = noopReader)
        route == FieldMindScreen.MapScreen.route -> MapFieldScreen(viewModel = viewModel, onBack = noop, onNavigate = noopNav, onOpenDetail = noopDetail)
        route == FieldMindScreen.ExportStudio.route -> BackupAndRestoreScreen(viewModel = viewModel, onBack = noop)
        route == FieldMindScreen.Changelog.route -> FieldMindChangelogScreen(onBack = noop)
        route == FieldMindScreen.Progress.route -> InsightsScreen(viewModel = viewModel, onBack = noop, onNavigate = noopNav, onOpenDetail = noopDetail)
        route == FieldMindScreen.Flashcards.route -> FlashcardSessionScreen(viewModel = viewModel, onBack = noop)
        route == FieldMindScreen.ResearchSession.route -> ResearchSessionScreen(viewModel = viewModel, onBack = noop, onOpenDetail = noopDetail)
        route == FieldMindScreen.WeatherDatabase.route -> WeatherDatabaseScreen(viewModel = viewModel, onBack = noop, onOpenSettings = noop, onOpenDetail = noopDetail)

        // ── Settings hub (many callbacks) ──
        route == FieldMindScreen.Settings.route -> FieldMindSettingsScreen(
            viewModel = viewModel,
            onBack = noop,
            onResetOnboarding = noop,
            onOpenExport = null,
            onOpenAbout = null,
            onOpenProfile = null,
            onOpenAppearance = null,
            onOpenCapture = null,
            onOpenWeather = null,
            onOpenAi = null,
            onOpenLocalModel = null,
            onOpenBackup = null,
            onOpenSecurity = null,
            onOpenChangelog = null,
            onOpenUnits = null,
            onOpenMap = null,
            onOpenDataIntegrity = null,
            onOpenDeveloper = null,
            onOpenSpeciesPacks = null,
            onOpenSpeciesId = null,
            onOpenAutoGen = null,
            onOpenScreenVisibility = null
        )

        // ── Settings sub-pages ──
        route == FieldMindScreen.SettingsProfile.route -> ProfileSettingsPage(viewModel = viewModel, onBack = noop)
        route == FieldMindScreen.SettingsAppearance.route -> AppearanceSettingsPage(viewModel = viewModel, onBack = noop, onOpenEntityColors = null)
        route == FieldMindScreen.SettingsEntityColors.route -> EntityAccentColorsPage(settings = viewModel.fieldSettings, onBack = noop)
        route == FieldMindScreen.SettingsCapture.route -> CaptureDefaultsSettingsPage(viewModel = viewModel, onBack = noop)
        route == FieldMindScreen.SettingsAi.route -> AiAssistantSettingsPage(viewModel = viewModel, onBack = noop)
        route == FieldMindScreen.SettingsLocalModel.route -> LocalModelSettingsPage(viewModel = viewModel, onBack = noop)
        route == FieldMindScreen.SettingsBackup.route -> BackupImportSettingsPage(viewModel = viewModel, onBack = noop, onOpenExport = noop)
        route == FieldMindScreen.SettingsSecurity.route -> SecuritySettingsPage(viewModel = viewModel, onBack = noop, onOpenSecurityScore = null)
        route == FieldMindScreen.SettingsSecurityScore.route -> SecurityScoreDetailPage(settings = viewModel.fieldSettings, onBack = noop)
        route == FieldMindScreen.SettingsScreenVisibility.route -> ScreenVisibilitySettingsPage(viewModel = viewModel, onBack = noop)
        route == FieldMindScreen.SettingsAbout.route -> AboutPage(onBack = noop, onOpenChangelog = null)
        route == FieldMindScreen.SettingsUnits.route -> UnitsFormatSettingsPage(viewModel = viewModel, onBack = noop)
        route == FieldMindScreen.SettingsWeather.route -> WeatherSettingsPage(viewModel = viewModel, onBack = noop)
        route == FieldMindScreen.SettingsMap.route -> MapSettingsPage(viewModel = viewModel, onBack = noop)
        route == FieldMindScreen.SettingsDataIntegrity.route -> DataIntegritySettingsPage(viewModel = viewModel, onBack = noop)
        route == FieldMindScreen.SettingsDeveloper.route -> DeveloperSettingsPage(viewModel = viewModel, onBack = noop, onOpenAnimationTuning = null)
        route == FieldMindScreen.SettingsAnimationTuning.route -> AnimationTuningSettingsPage(settings = viewModel.fieldSettings, onBack = noop)
        route == FieldMindScreen.SettingsSpeciesPacks.route -> SpeciesPackSettingsPage(onBack = noop)
        route == FieldMindScreen.SettingsSpeciesId.route -> SpeciesIdentificationSettingsPage(viewModel = viewModel, onBack = noop)
        route == FieldMindScreen.SettingsAutoGen.route -> AutoGenerationSettingsPage(viewModel = viewModel, onBack = noop)

        // ── Species browsers ──
        route == FieldMindScreen.SpeciesBrowser.route -> SpeciesBrowserScreen(onBack = noop, onOpenDetail = noopStr)
        route == FieldMindScreen.TaxonomicBrowser.route -> TaxonomicBrowserScreen(onBack = noop, onOpenDetail = noopStr)

        // ── Tools ──
        route == FieldMindScreen.CounterTool.route -> CounterToolScreen(viewModel = viewModel, onBack = noop)
        route == FieldMindScreen.MeasurementTool.route -> MeasurementToolScreen(viewModel = viewModel, onBack = noop)
        route == FieldMindScreen.WeatherLogTool.route -> WeatherLogToolScreen(viewModel = viewModel, onBack = noop)
        route == FieldMindScreen.SpeciesTool.route -> SpeciesToolScreen(viewModel = viewModel, onBack = noop, onOpenBrowser = noop, onOpenTaxonomicBrowser = noop)
        route == FieldMindScreen.ChecklistTool.route -> ChecklistToolScreen(viewModel = viewModel, onBack = noop)
        route == FieldMindScreen.EventLogTool.route -> EventLogToolScreen(viewModel = viewModel, onBack = noop)
        route == FieldMindScreen.SiteLogTool.route -> SiteLogToolScreen(viewModel = viewModel, onBack = noop)
        route == FieldMindScreen.ComparisonTable.route -> ComparisonTableScreen(viewModel = viewModel, onBack = noop)
        route == FieldMindScreen.TimerTool.route -> TimerToolScreen(onBack = noop)
        route == FieldMindScreen.FieldLog.route -> FieldLogScreen(viewModel = viewModel, onBack = noop, onOpenDetail = noopDetail, onOpenExport = noop)

        // ── Creation screens ──
        route == FieldMindScreen.NewProject.route -> NewProjectScreen(viewModel = viewModel, onBack = noop)
        route == FieldMindScreen.NewQuestion.route -> NewQuestionScreen(viewModel = viewModel, onBack = noop)
        route == FieldMindScreen.NewHypothesis.route -> NewHypothesisScreen(viewModel = viewModel, onBack = noop)
        route == FieldMindScreen.NewDataRecord.route -> NewDataRecordScreen(viewModel = viewModel, onBack = noop)
        route == FieldMindScreen.NewReport.route -> NewReportScreen(viewModel = viewModel, onBack = noop)
        route == FieldMindScreen.Tasks.route -> TasksScreen(viewModel = viewModel, onBack = noop, onOpenDetail = noopDetail, onNavigate = noopStr)
        route == FieldMindScreen.NewTask.route -> NewTaskScreen(viewModel = viewModel, onBack = noop)
        route == FieldMindScreen.NewObservation.route -> NewObservationScreen(viewModel = viewModel, onBack = noop)
        route == FieldMindScreen.NewNote.route -> NewNoteScreen(viewModel = viewModel, onBack = noop)
        route == FieldMindScreen.NewSource.route -> NewSourceScreen(viewModel = viewModel, onBack = noop)
        route == FieldMindScreen.NewAttachment.route -> NewAttachmentScreen(viewModel = viewModel, onBack = noop)
        route == FieldMindScreen.NewFolder.route -> NewFolderScreen(viewModel = viewModel, onBack = noop)
    }
}

/**
 * Composable helper to make a [SharedTransitionScope] available as a receiver
 * inside the [content] lambda. Screens that need [Modifier.sharedElement] or
 * [Modifier.sharedBounds] should be wrapped with this.
 */
@Composable
fun WithSharedTransitionScope(
    scope: SharedTransitionScope,
    content: @Composable SharedTransitionScope.() -> Unit
) {
    @Suppress("FunctionName")
    content(scope)
}

