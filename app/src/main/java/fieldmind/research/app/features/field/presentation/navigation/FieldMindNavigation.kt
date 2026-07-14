package fieldmind.research.app.features.field.presentation.navigation
import fieldmind.research.app.ui.theme.CuteCardDefaults

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import fieldmind.research.app.features.field.presentation.components.FieldMindSnackbarProvider
import fieldmind.research.app.features.field.presentation.components.LocalFieldMindSnackbar
import fieldmind.research.app.features.field.presentation.components.SwipeBackHost
import fieldmind.research.app.features.field.presentation.components.FieldMindIcons
import fieldmind.research.app.features.field.presentation.components.LocalSharedTransitionScope
import fieldmind.research.app.features.field.presentation.components.rememberFieldMindHaptics
import fieldmind.research.app.features.field.data.learn.FieldSkillsLessons
import fieldmind.research.app.features.field.data.stats.FieldMindStreaks
import fieldmind.research.app.features.field.presentation.screens.*
import fieldmind.research.app.features.field.presentation.theme.FieldMindTheme
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
import fieldmind.research.app.features.field.presentation.components.LocalAnimationsEnabled
import fieldmind.research.app.features.field.presentation.components.LocalPeekContentHolder
import fieldmind.research.app.features.field.presentation.components.FieldMindAnimatedSplash
import androidx.activity.compose.BackHandler
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.math.abs

import fieldmind.research.app.features.field.presentation.utils.AppLifecycleManager
import fieldmind.research.app.features.field.presentation.components.LocalAnimatedVisibilityScope
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.hazeEffect
import fieldmind.research.app.features.field.presentation.components.UpdateBannerOverlay
import fieldmind.research.app.infrastructure.updates.UpdateChecker
import fieldmind.research.app.infrastructure.updates.UpdateInfo

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
    data object Projects : FieldMindScreen("field_projects", "Projects", FieldMindIcons.Projects)
    data object Library : FieldMindScreen("field_library", "Library", FieldMindIcons.Library)
    data object Insights : FieldMindScreen("field_insights", "Insights", FieldMindIcons.Insights)
    data object MapScreen : FieldMindScreen("field_map", "Map", FieldMindIcons.Map)
    data object ExportStudio : FieldMindScreen("field_export_studio", "Export", FieldMindIcons.Export)
    data object WeatherDatabase : FieldMindScreen("field_weather_database", "Weather", FieldMindIcons.Weather)
    data object WeatherCatalog : FieldMindScreen("field_weather_catalog", "Weather Catalog", MaterialSymbolIcon("cloud"))

    data object Learn : FieldMindScreen("field_learn", "Learn", FieldMindIcons.School)
    data object FieldMode : FieldMindScreen("field_mode", "Field Mode", FieldMindIcons.Bolt)
    data object Questions : FieldMindScreen("field_questions", "Questions", FieldMindIcons.Question)
    data object Hypotheses : FieldMindScreen("field_hypotheses", "Hypotheses", FieldMindIcons.Hypothesis)
    data object DataTools : FieldMindScreen("field_data_tools", "Data", FieldMindIcons.Data)
    data object Analysis : FieldMindScreen("field_analysis", "Analysis", FieldMindIcons.Trend)
    data object Reports : FieldMindScreen("field_reports", "Reports", FieldMindIcons.Report)
    data object Search : FieldMindScreen("field_search", "Search", FieldMindIcons.Search)
    data object Changelog : FieldMindScreen("field_changelog", "What's new", FieldMindIcons.Info)
    data object BugReport : FieldMindScreen("field_bug_report", "Report a bug", MaterialSymbolIcon("bug_report"))
    data object CheckForUpdates : FieldMindScreen("field_check_updates", "Check for updates", MaterialSymbolIcon("system_update"))
    data object Progress : FieldMindScreen("field_progress", "Progress", FieldMindIcons.Check)
    data object Flashcards : FieldMindScreen("field_flashcards_session", "Review", FieldMindIcons.Flashcard)
    data object Reader : FieldMindScreen("field_reader", "Reader", FieldMindIcons.Book)
    data object LessonViewer : FieldMindScreen("field_lesson/{lessonSlug}", "Lesson", MaterialSymbolIcon("school"))
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
    data object SettingsNotifications : FieldMindScreen("field_settings_notifications", "Notifications", FieldMindIcons.Notifications)
    data object SettingsAnimation : FieldMindScreen("field_settings_animation", "Animations", MaterialSymbolIcon("motion_photos_on"))


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
    data object ResearchSession : FieldMindScreen("field_research_session", "Research Session", MaterialSymbolIcon("science"))
    data object CompassTool : FieldMindScreen("field_compass_tool", "Compass", MaterialSymbolIcon("explore"))
    data object LevelTool : FieldMindScreen("field_level_tool", "Level", MaterialSymbolIcon("straighten"))

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

    // ── New screens: Voice Notes, Media Gallery, Citation Manager, Collaboration ──
    data object VoiceNotes : FieldMindScreen("field_voice_notes", "Voice Notes", MaterialSymbolIcon("mic"))
    data object MediaGallery : FieldMindScreen("field_media_gallery", "Media Gallery", MaterialSymbolIcon("photo_library"))
    data object CitationManager : FieldMindScreen("field_bibliography", "Bibliography", MaterialSymbolIcon("book"))
    data object Collaboration : FieldMindScreen("field_collaboration", "Collaborate", MaterialSymbolIcon("share"))
}



enum class FieldMindBackAction { ReturnToHomeTab, PopSubPage, NavigateHomeFallback, AllowSystemExit }
private const val FIELDJOURNAL_DISMISS_ANIM_MS: Long = 450L


fun fieldMindBackAction(currentRoute: String?, activeTabIndex: Int, canPopSubPage: Boolean = true): FieldMindBackAction = when {
    currentRoute == "field_tab_container" && activeTabIndex != 0 -> FieldMindBackAction.ReturnToHomeTab
    currentRoute == "field_tab_container" -> FieldMindBackAction.AllowSystemExit
    canPopSubPage -> FieldMindBackAction.PopSubPage
    else -> FieldMindBackAction.NavigateHomeFallback
}

data class FieldMindRouteMetadata(
    val route: String,
    val expectedSurface: String,
    val status: String
)

val fieldMindRouteMetadata = listOf(
    FieldMindRouteMetadata("field_tab_container", "AllTabScreen", "implemented"),
    FieldMindRouteMetadata(FieldMindScreen.Home.route, "Home tab inside AllTabScreen", "implemented"),
    FieldMindRouteMetadata(FieldMindScreen.Observe.route, "Capture tab inside AllTabScreen", "implemented"),
    FieldMindRouteMetadata(FieldMindScreen.Projects.route, "Projects tab inside AllTabScreen", "implemented"),
    FieldMindRouteMetadata(FieldMindScreen.Insights.route, "Insights tab inside AllTabScreen", "implemented"),
    FieldMindRouteMetadata(FieldMindScreen.Library.route, "Library tab inside AllTabScreen", "implemented"),
    FieldMindRouteMetadata(FieldMindScreen.Hypotheses.route, "QuestionsScreen with linked hypotheses", "placeholder"),
    FieldMindRouteMetadata(FieldMindScreen.Analysis.route, "ProjectsScreen analysis placeholder", "placeholder"),
    FieldMindRouteMetadata(FieldMindScreen.Progress.route, "InsightsScreen progress placeholder", "placeholder")
)

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
    var showSplash by remember { mutableStateOf(true) }
    val splashActive = showSplash && onboardingCompleted
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
    } else if (splashActive) {
        FieldMindAnimatedSplash(
            durationMs = 600,
            onSplashComplete = { showSplash = false }
        )
    } else {            val scope = rememberCoroutineScope()
            var showJournal by rememberSaveable { mutableStateOf(true) }
        Box(Modifier.fillMaxSize()) {
        FieldMindAppLock(
            settings = viewModel.fieldSettings,
            isUnlocked = appUnlocked,
            isDecoyMode = isDecoyMode,
            onUnlock = { appUnlocked = true; AppLifecycleManager.dismissLock() },
            onDecoyUnlock = { isDecoyMode = true }
        ) {
            val privacyTyping by viewModel.fieldSettings.privacyTypingEnabled.collectAsState()
            CompositionLocalProvider(LocalPrivacyTypingEnabled provides privacyTyping) {
                PrivacyTextInputWrapper {
                    FieldMindSnackbarProvider { _ ->
                        FieldMindNavigation(viewModel = viewModel, appSettings = appSettings, requestedDestination = requestedDestination, onResetOnboarding = { appSettings.setOnboardingCompleted(false); appUnlocked = false })
                    }
                }
            }
            // Journal overlay inside app lock so it doesn't compete with
            // the lock's gesture handling. Fixes: overlay vanishing early
            // and touch being broken after dismiss.
            val observations by viewModel.observations.collectAsState()
            val journalStreakEnabled by viewModel.fieldSettings.streaksEnabled.collectAsState()
            val journalStreakCount = remember(observations, journalStreakEnabled) {
                if (journalStreakEnabled) FieldMindStreaks.currentStreakDays(observations.map { it.date }) else 0
            }                // Compose-collected journal state (replaces the old shouldShowJournalToday()
                // helper, which read .value directly off StateFlows — an anti-pattern).
                val journalEnabled by viewModel.fieldSettings.journalEnabled.collectAsState()
                val journalLastShownDate by viewModel.fieldSettings.journalLastShownDate.collectAsState()
                val showJournalToday = remember(journalEnabled, journalLastShownDate) {
                    val today = getTodayDateString()
                    journalEnabled && journalLastShownDate != today
                }
                if (showJournal && showJournalToday) {
                    val journalDefaultConfidence by viewModel.fieldSettings.defaultConfidence.collectAsState()
                    DailyFieldJournalOverlay(
                        settings = viewModel.fieldSettings,
                        streakCount = journalStreakCount,
                        onDismiss = {
                            // Mark dismissed immediately (idempotent for the day)…
                            viewModel.fieldSettings.setJournalLastShownDate(getTodayDateString())
                            // …then defer the parent unmount so the composable's slide-down +
                            // fade-out animation gets to play instead of being interrupted
                            // by an immediate teardown. The spring spec in DailyFieldJournalOverlay
                            // (MediumBouncy + Low stiffness) finishes well within this budget.
                            scope.launch {
                                delay(FIELDJOURNAL_DISMISS_ANIM_MS)
                                showJournal = false
                            }
                        },
                        onSave = { subject, category ->
                            // Persist the quick-capture text from the journal overlay as a real
                            // Observation. Tags it with "daily-journal" so the user can spot /
                            // filter these quick entries elsewhere in the app.
                            viewModel.addObservation(
                                subject = subject,
                                category = category,
                                facts = "",
                                confidence = journalDefaultConfidence,
                                manualLocation = "",
                                tags = "daily-journal",
                                evidence = "",
                                context = "Saved from daily journal overlay"
                            )
                        }
                    )
                }
                // Mount UpdateBannerOverlay as a peer to the journal overlay (both live
                // inside the privacy lock so they never float over the lock screen).
                // It only renders when a newer release exists AND the user hasn't tapped
                // "Later" for this tag yet. Both Update and Notes route to the GitHub release
                // page (which itself contains release notes + download link).
                val context = LocalContext.current
                val updateChecker = remember { UpdateChecker(appSettings) }
                val updateInfo by updateChecker.updateInfo.collectAsState()
                val updateDismissedTag by appSettings.updateLastDismissedTag.collectAsState()
                val updateEnabled by appSettings.updateCheckEnabled.collectAsState()
                LaunchedEffect(updateEnabled) {
                    if (updateEnabled) updateChecker.check(force = false)
                }
                val currentUpdate = updateInfo as? UpdateInfo.UpdateAvailable
                if (currentUpdate != null && currentUpdate.tag != updateDismissedTag) {
                    UpdateBannerOverlay(
                        info = currentUpdate,
                        onUpdate = {
                            runCatching {
                                context.startActivity(
                                    android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(currentUpdate.releaseUrl))
                                        .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }
                        },
                        onLater = {
                            appSettings.setUpdateLastDismissedTag(currentUpdate.tag)
                        },
                        onOpenChangelog = {
                            // GitHub release page contains both the changelog and the
                            // download link — open the same URL the Update button uses.
                            runCatching {
                                context.startActivity(
                                    android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(currentUpdate.releaseUrl))
                                        .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }
                        }
                    )
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
fun FieldMindNavigation(viewModel: FieldMindViewModel, appSettings: AppSettings, requestedDestination: String? = null, onResetOnboarding: () -> Unit) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val currentRoute = currentDestination?.route
    val haptics = rememberFieldMindHaptics()

    fun navigateHomeFallback() {
        navController.navigate("field_tab_container") {
            launchSingleTop = true
        }
    }

    fun safePopOrHome() {
        if (!navController.popBackStack()) {
            navigateHomeFallback()
        }
    }

    // ── Active tab index — tabs are rendered simultaneously inside TabContentHost ──
    var activeTabIndex by rememberSaveable { mutableIntStateOf(0) }

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
        if (index == activeTabIndex && currentRoute == "field_tab_container") return

        // Protect against accidental navigation while a capture session is active
        if (activeTabIndex == 1 && viewModel.captureSessionActive) {
            pendingNavRoute = visibleTabs.getOrNull(index)?.route
            showNavigateConfirm = true
            return
        }
        if (currentRoute != "field_tab_container") {
            navigateHomeFallback()
        }
        activeTabIndex = index
    }


    LaunchedEffect(requestedDestination) {
        when (requestedDestination) {
            FieldMindScreen.FieldMode.route, "field_mode" -> navController.navigateToDestination(FieldMindScreen.FieldMode.route)
            "field_timer" -> navController.navigateToDestination("field_tab_container") // redirect to tabs (capture is tab index 1)
            // Note: Capture tab can be activated by setting activeTabIndex = 1 after navigation
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
                    shape = CuteCardDefaults.ButtonShape,
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
                // NavHost content behind the rail (captured via hazeSource() below);
                // .liquidGlassRefraction() applies GPU displacement & specular.
                if (!hideChrome) {                        Surface(
                            shape = RoundedCornerShape(size = 38.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            tonalElevation = 0.dp,
                            shadowElevation = if (FieldMindTheme.colors.isDark) 14.dp else 8.dp,
                            border = androidx.compose.foundation.BorderStroke(
                                width = 0.6.dp,
                                color = if (FieldMindTheme.colors.isDark)
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)
                                else
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f)
                            ),
                            modifier = Modifier
                                .padding(start = 8.dp, top = 8.dp, bottom = 8.dp)
                                .width(IntrinsicSize.Min)
                                .hazeEffect(
                                    state = hazeState,
                                    style = HazeStyle(
                                        blurRadius = 32.dp,
                                        noiseFactor = 0.06f,
                                        tints = listOf(
                                            HazeTint(
                                                color = if (FieldMindTheme.colors.isDark)
                                                    MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.55f)
                                                else
                                                    MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.45f)
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
                    appSettings = appSettings,
                    visibleTabs = visibleTabs,
                    onNavigateToTabRoute = null,
                    activeTabIndex = activeTabIndex,
                    onActiveTabChange = { index -> activeTabIndex = index },
                    onSafeBack = { safePopOrHome() },
                    modifier = Modifier.weight(1f).hazeSource(state = hazeState)
                )
            }
        } else {
            // ── True floating overlay nav bar with liquid glass effect ──
            // We use a raw Box instead of Scaffold so Android never draws a
            // solid rectangular bottom-bar background behind the pill.
            // The content fills the full screen edge-to-edge; the pill is
            // overlaid at the bottom with real backdrop blur via Haze.
            //
            // IMPORTANT: hazeSource() is ONLY on the NavHost content, NOT the outer
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
                    appSettings = appSettings,
                    visibleTabs = visibleTabs,
                    onNavigateToTabRoute = null,
                    activeTabIndex = activeTabIndex,
                    onActiveTabChange = { index -> activeTabIndex = index },
                    onSafeBack = { safePopOrHome() },
                    modifier = Modifier.fillMaxSize().hazeSource(state = hazeState)
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
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            tonalElevation = 0.dp,
                            shadowElevation = if (FieldMindTheme.colors.isDark) 14.dp else 8.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(68.dp)
                                .clip(RoundedCornerShape(50.dp))
                                .hazeEffect(
                                    state = hazeState,
                                    style = HazeStyle(
                                        blurRadius = 32.dp,
                                        noiseFactor = 0.06f,
                                        tints = listOf(
                                            HazeTint(
                                                color = if (FieldMindTheme.colors.isDark)
                                                    MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.55f)
                                                else
                                                    MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.45f)
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

    // ── Nav bar blob color — uses theme primary ──
    val isDark = FieldMindTheme.colors.isDark
    val primary = MaterialTheme.colorScheme.primary
    val blobColor = primary.copy(alpha = 0.15f)

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
        val scope = rememberCoroutineScope()
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            visibleTabs.forEachIndexed { index, screen ->
                val selected = isSelected(screen)
                var isPressed by remember { mutableStateOf(false) }
                // Spring bounce pulse on tap — briefly scales up, then springs back
                val tapBounce = remember { Animatable(1f) }
                val iconBounce = remember { Animatable(1f) }

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
                            val childWindow = coordinates.localToWindow(Offset.Zero)
                            val parentWindow = coordinates.parentCoordinates?.localToWindow(Offset.Zero) ?: Offset.Zero
                            val x = (childWindow - parentWindow).x
                            if (tabBounds.size <= index) {
                                while (tabBounds.size <= index) tabBounds.add(TabBounds(0f, 0f))
                            }
                            tabBounds[index] = TabBounds(x, width)
                        }
                        .clip(CuteCardDefaults.Shape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                isPressed = true
                                // Spring bounce: snap to 1.08, spring back to 1.0
                                scope.launch {
                                    tapBounce.snapTo(1.15f)
                                    tapBounce.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 250f))
                                }
                                scope.launch {
                                    delay(50)
                                    iconBounce.snapTo(1.25f)
                                    iconBounce.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 350f))
                                }
                                onTabClick(screen)
                            }
                        )
                        .graphicsLayer {
                            scaleX = pressScale * tapBounce.value
                            scaleY = pressScale * tapBounce.value
                        }
                        .defaultMinSize(minWidth = 60.dp, minHeight = 56.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier.size(width = 48.dp, height = 36.dp)
                            .graphicsLayer {
                                scaleX = iconBounce.value
                                scaleY = iconBounce.value
                            },
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
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )


                // Reset press state when selection changes
                LaunchedEffect(selected) {
                    if (selected) isPressed = false                }
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
            .clip(CuteCardDefaults.ShapeCompact)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .defaultMinSize(minHeight = 48.dp)
            .background(
                color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
                shape = CuteCardDefaults.ShapeCompact
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
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
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
        route.startsWith("field_edit/") -> RouteCategory.Creation
        route in listOf(
            FieldMindScreen.CounterTool.route, FieldMindScreen.MeasurementTool.route,
            FieldMindScreen.WeatherLogTool.route, FieldMindScreen.SpeciesTool.route,
            FieldMindScreen.ChecklistTool.route, FieldMindScreen.EventLogTool.route,
            FieldMindScreen.SiteLogTool.route, FieldMindScreen.ComparisonTable.route,
            FieldMindScreen.SpeciesBrowser.route, FieldMindScreen.TaxonomicBrowser.route,
            FieldMindScreen.FieldLog.route, FieldMindScreen.TimerTool.route,
            FieldMindScreen.CompassTool.route, FieldMindScreen.LevelTool.route,
            FieldMindScreen.ResearchSession.route,
            FieldMindScreen.Flashcards.route,
            FieldMindScreen.WeatherDatabase.route,
            FieldMindScreen.WeatherCatalog.route
        ) -> RouteCategory.Tool
        else -> RouteCategory.Other
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.routeEnterTransition(config: AnimationConfig): EnterTransition {
    val fromRoute = initialState.destination.route ?: ""
    val toRoute = targetState.destination.route ?: ""
    val fromCat = categorizeRoute(fromRoute)
    val toCat = categorizeRoute(toRoute)
    // Telegram-like spring-based sliding with slight bounce
    val slideSpec = config.slideSpring()
    val fadeSpec = spring<Float>(
        dampingRatio = config.dampingRatio,
        stiffness = (config.stiffness * 0.78f).coerceAtLeast(60f)
    )
    val bouncySpec = spring<Float>(
        dampingRatio = 0.50f,
        stiffness = (config.stiffness * 0.55f).coerceAtLeast(60f)
    )

    return when {
        fromCat == RouteCategory.Tab && toCat == RouteCategory.Tab -> {
            val direction = primaryTabDirection(fromRoute, toRoute)
            if (direction == 0)
                fadeIn(animationSpec = fadeSpec) +
                scaleIn(initialScale = 0.96f, animationSpec = fadeSpec)
            else
                // Telegram-like: full slide with scale pop
                slideInHorizontally(slideSpec) { direction * it / 3 } + fadeIn(fadeSpec) +
                scaleIn(initialScale = 0.97f, animationSpec = bouncySpec)
        }
        fromCat == RouteCategory.Tab && toCat in listOf(
            RouteCategory.SettingsHub, RouteCategory.SettingsSubPage,
            RouteCategory.Tool, RouteCategory.Detail, RouteCategory.Creation,
            RouteCategory.Other
        ) -> slideInHorizontally(slideSpec) { it / 3 } + fadeIn(fadeSpec) +
            scaleIn(initialScale = 0.97f, animationSpec = bouncySpec)
        fromCat == RouteCategory.SettingsHub && toCat == RouteCategory.SettingsSubPage ->
            fadeIn(animationSpec = fadeSpec) + scaleIn(initialScale = 0.98f, animationSpec = fadeSpec)
        fromCat == RouteCategory.SettingsSubPage && toCat == RouteCategory.SettingsHub ->
            fadeIn(animationSpec = fadeSpec) + scaleIn(initialScale = 0.98f, animationSpec = fadeSpec)
        toCat == RouteCategory.Tab && fromCat in listOf(
            RouteCategory.SettingsHub, RouteCategory.SettingsSubPage,
            RouteCategory.Tool, RouteCategory.Detail, RouteCategory.Creation, RouteCategory.Other
        ) -> slideInHorizontally(slideSpec) { -it / 3 } + fadeIn(fadeSpec) +
            scaleIn(initialScale = 0.97f, animationSpec = bouncySpec)
        else -> fadeIn(animationSpec = fadeSpec) +
            scaleIn(initialScale = 0.97f, animationSpec = fadeSpec)
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.routeExitTransition(config: AnimationConfig): ExitTransition {
    val fromRoute = initialState.destination.route ?: ""
    val toRoute = targetState.destination.route ?: ""
    val fromCat = categorizeRoute(fromRoute)
    val toCat = categorizeRoute(toRoute)
    val slideSpec = config.slideSpring()
    val fadeSpec = spring<Float>(
        dampingRatio = config.dampingRatio,
        stiffness = (config.stiffness * 0.78f).coerceAtLeast(60f)
    )

    return when {
        fromCat == RouteCategory.Tab && toCat == RouteCategory.Tab -> {
            val direction = primaryTabDirection(fromRoute, toRoute)
            if (direction == 0) fadeOut(fadeSpec)
            else {
                // Telegram-like: quick exit with slight slide
                slideOutHorizontally(slideSpec) { -direction * it / 6 } + fadeOut(fadeSpec)
            }
        }
        fromCat == RouteCategory.Tab && toCat in listOf(
            RouteCategory.SettingsHub, RouteCategory.SettingsSubPage,
            RouteCategory.Tool, RouteCategory.Detail, RouteCategory.Creation,
            RouteCategory.Other
        ) -> {
            slideOutHorizontally(slideSpec) { -it / 6 } + fadeOut(fadeSpec)
        }
        fromCat == RouteCategory.SettingsHub && toCat == RouteCategory.SettingsSubPage ->
            fadeOut(fadeSpec)
        fromCat == RouteCategory.SettingsSubPage && toCat == RouteCategory.SettingsHub ->
            fadeOut(fadeSpec)
        toCat == RouteCategory.Tab && fromCat in listOf(
            RouteCategory.SettingsHub, RouteCategory.SettingsSubPage,
            RouteCategory.Tool, RouteCategory.Detail, RouteCategory.Creation, RouteCategory.Other
        ) -> {
            slideOutHorizontally(slideSpec) { it / 6 } + fadeOut(fadeSpec)
        }
        else -> fadeOut(fadeSpec)
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.routePopEnterTransition(config: AnimationConfig): EnterTransition {
    val fromRoute = initialState.destination.route ?: ""
    val toRoute = targetState.destination.route ?: ""
    val fromCat = categorizeRoute(fromRoute)
    val toCat = categorizeRoute(toRoute)
    val slideSpec = config.slideSpring()
    val fadeSpec = spring<Float>(
        dampingRatio = config.dampingRatio,
        stiffness = (config.stiffness * 0.78f).coerceAtLeast(60f)
    )
    val bouncySpec = spring<Float>(
        dampingRatio = 0.50f,
        stiffness = (config.stiffness * 0.55f).coerceAtLeast(60f)
    )

    return when {
        fromCat == RouteCategory.Tab && toCat == RouteCategory.Tab -> {
            val direction = primaryTabDirection(toRoute, fromRoute)
            slideInHorizontally(slideSpec) { -direction * it } + fadeIn(animationSpec = fadeSpec) +
                scaleIn(initialScale = 0.97f, animationSpec = bouncySpec)
        }
        toCat == RouteCategory.Tab && fromCat in listOf(
            RouteCategory.SettingsHub, RouteCategory.SettingsSubPage,
            RouteCategory.Tool, RouteCategory.Detail, RouteCategory.Creation,
            RouteCategory.Other
        ) -> {
            slideInHorizontally(slideSpec) { -it } + fadeIn(animationSpec = fadeSpec) +
                scaleIn(initialScale = 0.97f, animationSpec = bouncySpec)
        }
        toCat == RouteCategory.SettingsHub && fromCat == RouteCategory.SettingsSubPage ->
            fadeIn(animationSpec = fadeSpec) + scaleIn(initialScale = 0.98f, animationSpec = fadeSpec)
        else -> slideInHorizontally(slideSpec) { -it } + fadeIn(animationSpec = fadeSpec) +
            scaleIn(initialScale = 0.97f, animationSpec = bouncySpec)
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.routePopExitTransition(config: AnimationConfig): ExitTransition {
    val fromRoute = initialState.destination.route ?: ""
    val toRoute = targetState.destination.route ?: ""
    val fromCat = categorizeRoute(fromRoute)
    val toCat = categorizeRoute(toRoute)
    val slideSpec = config.slideSpring()
    val fadeSpec = spring<Float>(
        dampingRatio = config.dampingRatio,
        stiffness = (config.stiffness * 0.78f).coerceAtLeast(60f)
    )

    return when {
        fromCat == RouteCategory.Tab && toCat == RouteCategory.Tab -> {
            val direction = primaryTabDirection(toRoute, fromRoute)
            slideOutHorizontally(slideSpec) { direction * it } + fadeOut(animationSpec = fadeSpec)
        }
        fromCat == RouteCategory.Tab && toCat in listOf(
            RouteCategory.SettingsHub, RouteCategory.SettingsSubPage,
            RouteCategory.Tool, RouteCategory.Detail, RouteCategory.Creation,
            RouteCategory.Other
        ) -> {
            slideOutHorizontally(slideSpec) { it } + fadeOut(animationSpec = fadeSpec)
        }
        toCat == RouteCategory.Tab && fromCat in listOf(
            RouteCategory.SettingsHub, RouteCategory.SettingsSubPage,
            RouteCategory.Tool, RouteCategory.Detail, RouteCategory.Creation,
            RouteCategory.Other
        ) -> {
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
    appSettings: AppSettings,
    modifier: Modifier = Modifier,
    visibleTabs: List<FieldMindScreen> = emptyList(),
    onNavigateToTabRoute: ((String) -> Unit)? = null,
    activeTabIndex: Int = 0,
    onActiveTabChange: ((Int) -> Unit)? = null,
    onSafeBack: (() -> Unit)? = null
) {
    val safeBack: () -> Unit = onSafeBack ?: { navController.popBackStack(); Unit }
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
            { RouteContent(route, viewModel) }
        } else null
    }

    // ── Read reactive animation config from settings and provide via CompositionLocal ──
    // Uses the combined animationConfig StateFlow which re-emits whenever any
    // animation parameter changes (set via animation tuning sliders).
    // collectAsState() ensures the CompositionLocal reactively updates.
    val animConfig by viewModel.fieldSettings.animationConfig.collectAsState(AnimationConfig.DEFAULT)
    val animationsEnabled by viewModel.fieldSettings.animationsEnabled.collectAsState()

    SharedTransitionLayout(modifier = modifier) {
        val composableScope = this
        CompositionLocalProvider(
            LocalSharedTransitionScope provides composableScope,
            LocalPeekContentHolder provides peekHolder,
            LocalAnimationConfig provides animConfig,
            LocalAnimationsEnabled provides animationsEnabled
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
                CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
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
            }
            composable(FieldMindScreen.Learn.route) { SwipeBackHost(onBack = { safeBack() }) { FieldMindLearnScreen(viewModel = viewModel, onBack = { safeBack() }, onOpenReader = openReader, onOpenLesson = { slug -> navController.navigateToDestination("field_lesson/$slug") }) } }
            composable(FieldMindScreen.Reader.route) { SwipeBackHost(onBack = { safeBack() }) { LearnReaderScreen(url = readerTarget.first, title = readerTarget.second, onBack = { safeBack() }) } }
            composable("field_lesson/{lessonSlug}") { entry ->
                val lessonSlug = entry.arguments?.getString("lessonSlug") ?: ""
                SwipeBackHost(onBack = { safeBack() }) {
                    val lesson = FieldSkillsLessons.bySlug[lessonSlug]
                    if (lesson != null) {
                        LessonViewerScreen(lesson = lesson, onBack = { safeBack() })
                    }
                }
            }
            composable(FieldMindScreen.FieldMode.route) { SwipeBackHost(onBack = { safeBack() }) { ObserveScreen(viewModel = viewModel, compactFieldMode = true, onBack = { safeBack() }, onOpenDetail = openDetail) } }
            composable(FieldMindScreen.Questions.route) { SwipeBackHost(onBack = { safeBack() }) { QuestionsScreen(viewModel = viewModel, onBack = { safeBack() }, onOpenDetail = openDetail) } }
            // NOTE: Hypotheses currently renders QuestionsScreen — a dedicated hypotheses
            // list/browser screen isn't built yet. QuestionsScreen shows both questions and
            // linked hypotheses. Same for Analysis→ProjectsScreen and Progress→InsightsScreen.
            composable(FieldMindScreen.Hypotheses.route) { SwipeBackHost(onBack = { safeBack() }) { QuestionsScreen(viewModel = viewModel, onBack = { safeBack() }, onOpenDetail = openDetail) } }
            composable(FieldMindScreen.DataTools.route) { SwipeBackHost(onBack = { safeBack() }) { DataToolsHubScreen(viewModel = viewModel, onBack = { safeBack() }, onNavigate = { navController.navigateToDestination(it.route) }, onOpenDetail = openDetail) } }
            composable(FieldMindScreen.Analysis.route) { SwipeBackHost(onBack = { safeBack() }) { ProjectsScreen(viewModel = viewModel, startTab = 0, onOpenDetail = { _, id -> navController.navigateToDestination("field_project_detail/$id") }, onNavigate = { navController.navigateToDestination(it.route) }) } }
            composable(FieldMindScreen.Reports.route) { SwipeBackHost(onBack = { safeBack() }) { FieldMindReportScreen(viewModel = viewModel, onBack = { safeBack() }) } }
            composable(FieldMindScreen.Search.route) { SwipeBackHost(onBack = { safeBack() }) { ArchiveScreen(viewModel = viewModel, onBack = { safeBack() }, onOpenDetail = openDetail, onOpenReader = openReader) } }
            composable(FieldMindScreen.MapScreen.route) { SwipeBackHost(onBack = { safeBack() }) { MapFieldScreen(viewModel = viewModel, onBack = { safeBack() }, onNavigate = { navController.navigateToDestination(it.route) }, onOpenDetail = openDetail) } }
            composable(FieldMindScreen.ExportStudio.route) { SwipeBackHost(onBack = { safeBack() }) { BackupAndRestoreScreen(viewModel = viewModel, onBack = { safeBack() }) } }
            composable(FieldMindScreen.Changelog.route) { SwipeBackHost(onBack = { safeBack() }) { FieldMindChangelogScreen(onBack = { safeBack() }) } }
            composable(FieldMindScreen.BugReport.route) {
                val crashHistory by appSettings.crashLogHistory.collectAsState()
                val latestCrashLog = remember(crashHistory) { crashHistory.lastOrNull()?.log }
                SwipeBackHost(onBack = { safeBack() }) {
                    FieldMindBugReportScreen(
                        viewModel = viewModel,
                        latestCrashLog = latestCrashLog,
                        onBack = { safeBack() }
                    )
                }
            }
            composable(FieldMindScreen.CheckForUpdates.route) {
                SwipeBackHost(onBack = { safeBack() }) {
                    CheckForUpdatesScreen(
                        appSettings = appSettings,
                        onBack = { safeBack() },
                        onOpenChangelog = { navController.navigateToDestination(FieldMindScreen.Changelog.route) }
                    )
                }
            }
            composable(FieldMindScreen.Progress.route) { SwipeBackHost(onBack = { safeBack() }) { InsightsScreen(viewModel = viewModel, onBack = { safeBack() }, onNavigate = { navController.navigateToDestination(it.route) }, onOpenDetail = openDetail) } }
            composable(FieldMindScreen.Flashcards.route) { SwipeBackHost(onBack = { safeBack() }) { FlashcardSessionScreen(viewModel = viewModel, onBack = { safeBack() }) } }
            composable(FieldMindScreen.WeatherDatabase.route) { SwipeBackHost(onBack = { safeBack() }) { WeatherDatabaseScreen(viewModel = viewModel, onBack = { safeBack() }, onOpenSettings = { navController.navigateToDestination(FieldMindScreen.SettingsWeather.route) }, onOpenDetail = openDetail, onOpenWeatherCatalog = { navController.navigateToDestination(FieldMindScreen.WeatherCatalog.route) }) } }
            composable(FieldMindScreen.WeatherCatalog.route) { SwipeBackHost(onBack = { safeBack() }) { WeatherCatalogScreen(viewModel = viewModel, onBack = { safeBack() }, onOpenSettings = { navController.navigateToDestination(FieldMindScreen.SettingsWeather.route) }) } }
            composable(FieldMindScreen.Settings.route) {
                SwipeBackHost(onBack = { safeBack() }) {
                    FieldMindSettingsScreen(
                        viewModel = viewModel,
                        onBack = { safeBack() },
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
                        onOpenBugReport = { navController.navigateToDestination(FieldMindScreen.BugReport.route) },
                        onOpenUnits = { navController.navigateToDestination(FieldMindScreen.SettingsUnits.route) },
                        onOpenScreenVisibility = { navController.navigateToDestination(FieldMindScreen.SettingsScreenVisibility.route) },
                        onOpenMap = { navController.navigateToDestination(FieldMindScreen.SettingsMap.route) },
                        onOpenDataIntegrity = { navController.navigateToDestination(FieldMindScreen.SettingsDataIntegrity.route) },
                        onOpenDeveloper = { navController.navigateToDestination(FieldMindScreen.SettingsDeveloper.route) },
                        onOpenSpeciesPacks = { navController.navigateToDestination(FieldMindScreen.SettingsSpeciesPacks.route) },
                        onOpenSpeciesId = { navController.navigateToDestination(FieldMindScreen.SettingsSpeciesId.route) },
                        onOpenAutoGen = { navController.navigateToDestination(FieldMindScreen.SettingsAutoGen.route) },
                        onOpenNotifications = { navController.navigateToDestination(FieldMindScreen.SettingsNotifications.route) },
                        onOpenAnimations = { navController.navigateToDestination(FieldMindScreen.SettingsAnimation.route) },
                        onOpenCheckForUpdates = { navController.navigateToDestination(FieldMindScreen.CheckForUpdates.route) },
                    )
                }
            }
            composable(FieldMindScreen.SettingsProfile.route) { SwipeBackHost(onBack = { safeBack() }) { ProfileSettingsPage(viewModel = viewModel, onBack = { safeBack() }) } }
            composable(FieldMindScreen.SettingsAppearance.route) { SwipeBackHost(onBack = { safeBack() }) { AppearanceSettingsPage(viewModel = viewModel, onBack = { safeBack() }, onOpenEntityColors = { navController.navigateToDestination(FieldMindScreen.SettingsEntityColors.route) }) } }
            composable(FieldMindScreen.SettingsEntityColors.route) { SwipeBackHost(onBack = { safeBack() }) { EntityAccentColorsPage(settings = viewModel.fieldSettings, onBack = { safeBack() }) } }
            composable(FieldMindScreen.SettingsCapture.route) { SwipeBackHost(onBack = { safeBack() }) { CaptureDefaultsSettingsPage(viewModel = viewModel, onBack = { safeBack() }) } }
            composable(FieldMindScreen.SettingsAi.route) { SwipeBackHost(onBack = { safeBack() }) { AiAssistantSettingsPage(viewModel = viewModel, onBack = { safeBack() }) } }
            composable(FieldMindScreen.SettingsLocalModel.route) { SwipeBackHost(onBack = { safeBack() }) { LocalModelSettingsPage(viewModel = viewModel, onBack = { safeBack() }) } }
            composable(FieldMindScreen.SettingsBackup.route) { SwipeBackHost(onBack = { safeBack() }) { BackupImportSettingsPage(viewModel = viewModel, onBack = { safeBack() }, onOpenExport = { navController.navigateToDestination(FieldMindScreen.ExportStudio.route) }) } }
            composable(FieldMindScreen.SettingsSecurity.route) {
                SwipeBackHost(onBack = { safeBack() }) {
                    SecuritySettingsPage(
                        viewModel = viewModel,
                        onBack = { safeBack() },
                        onOpenSecurityScore = { navController.navigateToDestination(FieldMindScreen.SettingsSecurityScore.route) }
                    )
                }
            }
            composable(FieldMindScreen.SettingsSecurityScore.route) { SwipeBackHost(onBack = { safeBack() }) { SecurityScoreDetailPage(settings = viewModel.fieldSettings, onBack = { safeBack() }) } }
            composable(FieldMindScreen.SettingsScreenVisibility.route) {
                SwipeBackHost(onBack = { safeBack() }) {
                    ScreenVisibilitySettingsPage(viewModel = viewModel, onBack = { safeBack() })
                }
            }
            composable(FieldMindScreen.SettingsAbout.route) { SwipeBackHost(onBack = { safeBack() }) { AboutPage(onBack = { safeBack() }, onOpenChangelog = { navController.navigateToDestination(FieldMindScreen.Changelog.route) }) } }
            composable(FieldMindScreen.SettingsUnits.route) { SwipeBackHost(onBack = { safeBack() }) { UnitsFormatSettingsPage(viewModel = viewModel, onBack = { safeBack() }) } }
            composable(FieldMindScreen.SettingsWeather.route) { SwipeBackHost(onBack = { safeBack() }) { WeatherSettingsPage(viewModel = viewModel, onBack = { safeBack() }) } }
            composable(FieldMindScreen.SettingsMap.route) { SwipeBackHost(onBack = { safeBack() }) { MapSettingsPage(viewModel = viewModel, onBack = { safeBack() }) } }
            composable(FieldMindScreen.SettingsDataIntegrity.route) { SwipeBackHost(onBack = { safeBack() }) { DataIntegritySettingsPage(viewModel = viewModel, onBack = { safeBack() }) } }
            composable(FieldMindScreen.SettingsDeveloper.route) { SwipeBackHost(onBack = { safeBack() }) { DeveloperSettingsPage(viewModel = viewModel, onBack = { safeBack() }, onOpenAnimationTuning = { navController.navigateToDestination(FieldMindScreen.SettingsAnimationTuning.route) }) } }
            composable(FieldMindScreen.SettingsAnimationTuning.route) { SwipeBackHost(onBack = { safeBack() }) { AnimationTuningSettingsPage(settings = viewModel.fieldSettings, onBack = { safeBack() }) } }
            composable(FieldMindScreen.SpeciesBrowser.route) { SwipeBackHost(onBack = { safeBack() }) { SpeciesBrowserScreen(onBack = { safeBack() }, onOpenDetail = { id -> navController.navigateToDestination("field_species_detail/$id") }) } }
            composable(FieldMindScreen.TaxonomicBrowser.route) { SwipeBackHost(onBack = { safeBack() }) { TaxonomicBrowserScreen(onBack = { safeBack() }, onOpenDetail = { id -> navController.navigateToDestination("field_species_detail/$id") }) } }
            composable("field_species_detail/{speciesId}") { entry ->
                val speciesId = entry.arguments?.getString("speciesId") ?: ""
                SwipeBackHost(onBack = { safeBack() }) {
                    SpeciesDetailScreen(speciesId = speciesId, onBack = { safeBack() })
                }
            }
            composable(FieldMindScreen.SettingsSpeciesPacks.route) { SwipeBackHost(onBack = { safeBack() }) { SpeciesPackSettingsPage(onBack = { safeBack() }) } }
            composable(FieldMindScreen.SettingsSpeciesId.route) { SwipeBackHost(onBack = { safeBack() }) { SpeciesIdentificationSettingsPage(viewModel = viewModel, onBack = { safeBack() }) } }
            composable(FieldMindScreen.SettingsAutoGen.route) { SwipeBackHost(onBack = { safeBack() }) { AutoGenerationSettingsPage(viewModel = viewModel, onBack = { safeBack() }) } }
            composable(FieldMindScreen.SettingsNotifications.route) { SwipeBackHost(onBack = { safeBack() }) { NotificationsSettingsPage(viewModel = viewModel, onBack = { safeBack() }) } }
            composable(FieldMindScreen.SettingsAnimation.route) { SwipeBackHost(onBack = { safeBack() }) { AnimationSettingsPage(viewModel = viewModel, onBack = { safeBack() }) } }
            composable(FieldMindScreen.CounterTool.route) { SwipeBackHost(onBack = { safeBack() }) { CounterToolScreen(viewModel = viewModel, onBack = { safeBack() }) } }
            composable(FieldMindScreen.MeasurementTool.route) { SwipeBackHost(onBack = { safeBack() }) { MeasurementToolScreen(viewModel = viewModel, onBack = { safeBack() }) } }
            composable(FieldMindScreen.WeatherLogTool.route) { SwipeBackHost(onBack = { safeBack() }) { WeatherLogToolScreen(viewModel = viewModel, onBack = { safeBack() }) } }
            composable(FieldMindScreen.NewProject.route) { SwipeBackHost(onBack = { safeBack() }) { NewProjectScreen(viewModel = viewModel, onBack = { safeBack() }) } }
            composable(FieldMindScreen.NewQuestion.route) { SwipeBackHost(onBack = { safeBack() }) { NewQuestionScreen(viewModel = viewModel, onBack = { safeBack() }) } }
            composable(FieldMindScreen.NewHypothesis.route) { SwipeBackHost(onBack = { safeBack() }) { NewHypothesisScreen(viewModel = viewModel, onBack = { safeBack() }) } }
            composable(FieldMindScreen.NewDataRecord.route) { SwipeBackHost(onBack = { safeBack() }) { NewDataRecordScreen(viewModel = viewModel, onBack = { safeBack() }) } }
            composable(FieldMindScreen.Tasks.route) { SwipeBackHost(onBack = { safeBack() }) { TasksScreen(viewModel = viewModel, onBack = { safeBack() }, onOpenDetail = openDetail, onNavigate = { route -> navController.navigateToDestination(route) }) } }
            composable(FieldMindScreen.NewTask.route) { SwipeBackHost(onBack = { safeBack() }) { NewTaskScreen(viewModel = viewModel, onBack = { safeBack() }) } }
            composable(FieldMindScreen.NewReport.route) { SwipeBackHost(onBack = { safeBack() }) { NewReportScreen(viewModel = viewModel, onBack = { safeBack() }) } }
            composable(FieldMindScreen.NewObservation.route) { SwipeBackHost(onBack = { safeBack() }) { NewObservationScreen(viewModel = viewModel, onBack = { safeBack() }) } }
            composable(FieldMindScreen.NewNote.route) { SwipeBackHost(onBack = { safeBack() }) { NewNoteScreen(viewModel = viewModel, onBack = { safeBack() }) } }
            composable(FieldMindScreen.NewSource.route) { SwipeBackHost(onBack = { safeBack() }) { NewSourceScreen(viewModel = viewModel, onBack = { safeBack() }) } }
            composable(FieldMindScreen.NewAttachment.route) { SwipeBackHost(onBack = { safeBack() }) { NewAttachmentScreen(viewModel = viewModel, onBack = { safeBack() }) } }
            composable(FieldMindScreen.NewFolder.route) { SwipeBackHost(onBack = { safeBack() }) { NewFolderScreen(viewModel = viewModel, onBack = { safeBack() }) } }
            composable(FieldMindScreen.SpeciesTool.route) { SwipeBackHost(onBack = { safeBack() }) { SpeciesToolScreen(viewModel = viewModel, onBack = { safeBack() }, onOpenBrowser = { navController.navigateToDestination(FieldMindScreen.SpeciesBrowser.route) }, onOpenTaxonomicBrowser = { navController.navigateToDestination(FieldMindScreen.TaxonomicBrowser.route) }) } }
            composable(FieldMindScreen.ChecklistTool.route) { SwipeBackHost(onBack = { safeBack() }) { ChecklistToolScreen(viewModel = viewModel, onBack = { safeBack() }) } }
            composable(FieldMindScreen.EventLogTool.route) { SwipeBackHost(onBack = { safeBack() }) { EventLogToolScreen(viewModel = viewModel, onBack = { safeBack() }) } }
            composable(FieldMindScreen.SiteLogTool.route) { SwipeBackHost(onBack = { safeBack() }) { SiteLogToolScreen(viewModel = viewModel, onBack = { safeBack() }) } }
            composable(FieldMindScreen.ComparisonTable.route) { SwipeBackHost(onBack = { safeBack() }) { ComparisonTableScreen(viewModel = viewModel, onBack = { safeBack() }) } }
            composable(FieldMindScreen.TimerTool.route) { SwipeBackHost(onBack = { safeBack() }) { TimerToolScreen(onBack = { safeBack() }) } }
            composable(FieldMindScreen.ResearchSession.route) { SwipeBackHost(onBack = { safeBack() }) { ResearchSessionScreen(viewModel = viewModel, onBack = { safeBack() }, onOpenDetail = openDetail) } }
            composable(FieldMindScreen.CompassTool.route) { SwipeBackHost(onBack = { safeBack() }) { CompassToolScreen(viewModel = viewModel, onBack = { safeBack() }) } }
            composable(FieldMindScreen.LevelTool.route) { SwipeBackHost(onBack = { safeBack() }) { LevelToolScreen(viewModel = viewModel, onBack = { safeBack() }) } }
            composable("field_task_detail/{taskId}") { entry ->
                val taskId = entry.arguments?.getString("taskId")?.toLongOrNull() ?: 0L
                SwipeBackHost(onBack = { safeBack() }) {
                    TaskDetailScreen(
                        taskId = taskId,
                        viewModel = viewModel,
                        onBack = { safeBack() },
                        onOpenDetail = openDetail,
                        onOpenEdit = { kind, id -> navController.navigateToDestination("field_edit/$kind/$id") }
                    )
                }
            }
            composable("field_question_detail/{questionId}") { entry ->
                val questionId = entry.arguments?.getString("questionId")?.toLongOrNull() ?: 0L
                SwipeBackHost(onBack = { safeBack() }) {
                    QuestionDetailScreen(
                        questionId = questionId,
                        viewModel = viewModel,
                        onBack = { safeBack() },
                        onOpenDetail = openDetail,
                        onOpenEdit = { kind, id -> navController.navigateToDestination("field_edit/$kind/$id") }
                    )
                }
            }
            composable("field_hypothesis_detail/{hypothesisId}") { entry ->
                val hypothesisId = entry.arguments?.getString("hypothesisId")?.toLongOrNull() ?: 0L
                SwipeBackHost(onBack = { safeBack() }) {
                    HypothesisDetailScreen(
                        hypothesisId = hypothesisId,
                        viewModel = viewModel,
                        onBack = { safeBack() },
                        onOpenDetail = openDetail,
                        onOpenEdit = { kind, id -> navController.navigateToDestination("field_edit/$kind/$id") }
                    )
                }
            }
            composable("field_project_detail/{projectId}") { entry ->
                CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                val projectId = entry.arguments?.getString("projectId")?.toLongOrNull() ?: 0L
                SwipeBackHost(onBack = { safeBack() }) {
                    ProjectDetailScreen(
                        projectId = projectId,
                        viewModel = viewModel,
                        onBack = { safeBack() },
                        onOpenDetail = openDetail,
                        onNavigate = { navController.navigateToDestination(it.route) },
                        onOpenRelations = { navController.navigateToDestination("field_project_relations/$projectId") },
                        onOpenSettings = { id -> navController.navigateToDestination("field_project_settings/$id") }
                    )
                }
                }
            }
            composable("field_project_relations/{projectId}") { entry ->
                val projectId = entry.arguments?.getString("projectId")?.toLongOrNull() ?: 0L
                SwipeBackHost(onBack = { safeBack() }) {
                    ProjectRelationsScreen(
                        projectId = projectId,
                        viewModel = viewModel,
                        onBack = { safeBack() },
                        onOpenDetail = openDetail
                    )
                }
            }
            composable("field_project_settings/{projectId}") { entry ->
                val projectId = entry.arguments?.getString("projectId")?.toLongOrNull() ?: 0L
                SwipeBackHost(onBack = { safeBack() }) {
                    ProjectSettingsScreen(
                        projectId = projectId,
                        viewModel = viewModel,
                        onBack = { safeBack() },
                        onOpenBackupSettings = { navController.navigateToDestination(FieldMindScreen.SettingsBackup.route) }
                    )
                }
            }
            composable("field_canvas/{noteId}") { entry ->
                val noteId = entry.arguments?.getString("noteId")?.toLongOrNull() ?: 0L
                SwipeBackHost(onBack = { safeBack() }) {
                    CanvasScreen(
                        noteId = noteId,
                        fieldViewModel = viewModel,
                        onBack = { safeBack() },
                        onOpenLinkedEntity = { kind, id ->
                            navController.navigateToDestination("field_detail/$kind/$id")
                        }
                    )
                }
            }
            composable(FieldMindScreen.FieldLog.route) { SwipeBackHost(onBack = { safeBack() }) { FieldLogScreen(
                viewModel = viewModel,
                onBack = { safeBack() },
                onOpenDetail = openDetail,
                onOpenExport = { navController.navigateToDestination(FieldMindScreen.ExportStudio.route) }
            ) } }
            composable(FieldMindScreen.VoiceNotes.route) { SwipeBackHost(onBack = { safeBack() }) { VoiceNotesScreen(viewModel = viewModel, onBack = { safeBack() }) } }
            composable(FieldMindScreen.MediaGallery.route) { SwipeBackHost(onBack = { safeBack() }) { MediaGalleryScreen(viewModel = viewModel, onBack = { safeBack() }, onOpenDetail = openDetail) } }
            composable(FieldMindScreen.CitationManager.route) { SwipeBackHost(onBack = { safeBack() }) { CitationManagerScreen(viewModel = viewModel, onBack = { safeBack() }, onOpenDetail = openDetail) } }
            composable(FieldMindScreen.Collaboration.route) { SwipeBackHost(onBack = { safeBack() }) { CollaborationScreen(viewModel = viewModel, onBack = { safeBack() }, onOpenExport = { navController.navigateToDestination(FieldMindScreen.ExportStudio.route) }) } }
            composable("field_detail/{kind}/{id}") { entry ->
                val kind = entry.arguments?.getString("kind") ?: "observation"
                val id = entry.arguments?.getString("id")?.toLongOrNull() ?: 0L
                SwipeBackHost(onBack = { safeBack() }) {
                    DetailScreen(
                        kind = kind,
                        id = id,
                        viewModel = viewModel,
                        onBack = { safeBack() },
                        onOpenDetail = openDetail,
                        onOpenReader = openReader,
                        onOpenCanvas = { noteId ->
                            navController.navigateToDestination("field_canvas/$noteId")
                        },
                        onOpenEdit = { editKind, editId ->
                            navController.navigateToDestination("field_edit/$editKind/$editId")
                        }
                    )
                }
            }
            // ── Edit entity routes (full-screen, same composable as creation, with entity pre-filled) ──
            composable("field_edit/{kind}/{id}") { entry ->
                val kind = entry.arguments?.getString("kind") ?: "observation"
                val id = entry.arguments?.getString("id")?.toLongOrNull() ?: 0L
                SwipeBackHost(onBack = { safeBack() }) {
                    val observations by viewModel.observations.collectAsState()
                    val notes by viewModel.notes.collectAsState()
                    val questions by viewModel.questions.collectAsState()
                    val hypotheses by viewModel.hypotheses.collectAsState()
                    val projects by viewModel.projects.collectAsState()
                    val sources by viewModel.sources.collectAsState()
                    val dataRecords by viewModel.dataRecords.collectAsState()
                    val reports by viewModel.reports.collectAsState()
                    val tasks by viewModel.tasks.collectAsState()
                    when (kind) {
                        "observation" -> observations.firstOrNull { it.id == id }?.let {
                            NewObservationScreen(viewModel = viewModel, onBack = { safeBack() }, entity = it)
                        }
                        "note" -> notes.firstOrNull { it.id == id }?.let {
                            NewNoteScreen(viewModel = viewModel, onBack = { safeBack() }, entity = it)
                        }
                        "project" -> projects.firstOrNull { it.id == id }?.let {
                            NewProjectScreen(viewModel = viewModel, onBack = { safeBack() }, entity = it)
                        }
                        "question" -> questions.firstOrNull { it.id == id }?.let {
                            NewQuestionScreen(viewModel = viewModel, onBack = { safeBack() }, entity = it)
                        }
                        "hypothesis" -> hypotheses.firstOrNull { it.id == id }?.let {
                            NewHypothesisScreen(viewModel = viewModel, onBack = { safeBack() }, entity = it)
                        }
                        "source" -> sources.firstOrNull { it.id == id }?.let {
                            NewSourceScreen(viewModel = viewModel, onBack = { safeBack() }, entity = it)
                        }
                        "data" -> dataRecords.firstOrNull { it.id == id }?.let { entity ->
                            when (entity.toolType) {
                                "Counter" -> CounterToolScreen(viewModel = viewModel, onBack = { safeBack() }, entity = entity)
                                "Measurement Log" -> MeasurementToolScreen(viewModel = viewModel, onBack = { safeBack() }, entity = entity)
                                "Weather Log" -> WeatherLogToolScreen(viewModel = viewModel, onBack = { safeBack() }, entity = entity)
                                "Checklist" -> ChecklistToolScreen(viewModel = viewModel, onBack = { safeBack() }, entity = entity)
                                "Event Log" -> EventLogToolScreen(viewModel = viewModel, onBack = { safeBack() }, entity = entity)
                                "Site Log" -> SiteLogToolScreen(viewModel = viewModel, onBack = { safeBack() }, entity = entity)
                                "Comparison Table" -> ComparisonTableScreen(viewModel = viewModel, onBack = { safeBack() }, entity = entity)
                                else -> NewDataRecordScreen(viewModel = viewModel, onBack = { safeBack() }, entity = entity)
                            }
                        }
                        "report" -> reports.firstOrNull { it.id == id }?.let {
                            NewReportScreen(viewModel = viewModel, onBack = { safeBack() }, entity = it)
                        }
                        "task" -> tasks.firstOrNull { it.id == id }?.let {
                            NewTaskScreen(viewModel = viewModel, onBack = { safeBack() }, entity = it)
                        }
                        else -> safeBack()
                    }
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
    onTabSelected: ((Int) -> Unit)? = null,
    slideInFromPx: Float = 0f // horizontal slide offset for directional entrance
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
            .offset { IntOffset((offsetX + slideInFromPx * (1f - entranceProgress)).roundToInt(), 0) }
            .graphicsLayer {
                // ── Entrance micro-scale for newly active tab ──
                // When a tab becomes active via tapping, just a subtle scale pop
                // (0.98 → 1.0) with full opacity — no alpha fade, no blank screen.
                // Swipe-triggered changes skip this entirely (entranceProgress = 1).
                val entranceScale = 0.98f + 0.02f * entranceProgress
                scaleX = scale * entranceScale
                scaleY = scale * entranceScale
                this.alpha = alpha
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
                        onStartSession = { onNavigateToDestination("field_tab_container") }, // redirect to capture tab
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
    var hapticFiredCrossThreshold by remember { mutableStateOf(false) }

    val isFirstTab = activeTabIndex == 0

    // ── Exit confirmation dialog from Home tab ──
    var showExitConfirm by remember { mutableStateOf(false) }
    val activity = LocalContext.current as? android.app.Activity
    BackHandler(enabled = isFirstTab) {
        showExitConfirm = true
    }

    // ── Tab entrance animation (scale+fade+slide on tap-switch) ──
    // When a tab is activated via tapping, the new content slides in
    // from the appropriate direction (like the predictive back peek).
    // Swipe-triggered tab changes don't use the slide since the swipe
    // gesture already provides the directional reveal.
    val tabEntranceProgress = remember { Animatable(1f) }
    var lastActiveIndex by remember { mutableIntStateOf(activeTabIndex) }
    var tabSlideDirection by remember { mutableIntStateOf(0) } // +1=from right, -1=from left, 0=none
    var wasSwipeTriggered by remember { mutableStateOf(false) }
    val animConfig = fieldmind.research.app.features.field.presentation.components.LocalAnimationConfig.current
    LaunchedEffect(activeTabIndex) {
        if (activeTabIndex != lastActiveIndex) {
            // Determine slide direction: new tab to the right → slides from right (dir=+1)
            // If swipe-triggered, skip the entrance animation entirely and keep the tab
            // at full scale/alpha since the swipe gesture already revealed the content.
            if (wasSwipeTriggered) {
                wasSwipeTriggered = false
                tabSlideDirection = 0
                lastActiveIndex = activeTabIndex
                tabEntranceProgress.snapTo(1f)
            } else {
                // Button tap: no off-screen slide — just a subtle scale pop (0.98→1.0)
                // The slide was causing the new tab to appear to animate from a blank screen.
                tabSlideDirection = 0
                lastActiveIndex = activeTabIndex
                tabEntranceProgress.snapTo(0f)
                tabEntranceProgress.animateTo(
                    1f,
                    animationSpec = animConfig.spring()
                )
            }
        }
    }

    // ── Device back button: return to Home tab, or exit on Home tab ──
    BackHandler(enabled = !isFirstTab) {
        onTabSelected(0)
    }

    // ── System back gesture (left edge): handle all tabs ──
    // First tab: exit app on back. Other tabs: navigate to previous tab.
    // NOTE: PredictiveBackHandler was removed because it conflicted with
    // SwipeBackHost's BackHandler during navigation transitions (double-fire issue).
    // Manual drag gestures on the content layer handle inter-tab swiping instead.
    // BackHandler fires on hardware back button press.
    // The detectHorizontalDragGestures gesture overlay below handles touch-based
    // inter-tab swiping independently.

    // ── Determine if we can swipe left/right ──
    val canSwipeLeft = activeTabIndex < visibleTabs.size - 1
    val canSwipeRight = activeTabIndex > 0
    // Block swipe gestures during an active capture session on the Observe tab
    // so the session can't be accidentally dismissed by swiping (which bypasses
    // the navigation confirmation dialog).
    val observeTabIndex = visibleTabs.indexOfFirst { it == FieldMindScreen.Observe }
    val sessionLocked = activeTabIndex == observeTabIndex && viewModel.captureSessionActive
    val hasSwipeDirection = (canSwipeLeft || canSwipeRight) && !sessionLocked

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
                                scope.launch {
                                    animX.snapTo(clampedTarget)
                                    // Fire haptic the moment the swipe crosses the threshold
                                    val thresholdPx = contentWidth * animConfig.swipeThreshold
                                    if (abs(clampedTarget) >= thresholdPx && !hapticFiredCrossThreshold) {
                                        hapticFiredCrossThreshold = true
                                        haptics.confirm()
                                    }
                                }
                            },
                            onDragEnd = {
                                hapticFiredCrossThreshold = false
                                val threshold = contentWidth * animConfig.swipeThreshold
                                if (animX.value > threshold && canSwipeRight) {
                                    haptics.confirm()
                                    wasSwipeTriggered = true
                                    scope.launch {
                                        // Switch tab IMMEDIATELY — no waiting for off-screen animation.
                                        // Adjacent tab is already rendered behind current one.
                                        animX.snapTo(0f)
                                        onTabSelected(activeTabIndex - 1)
                                    }
                                } else if (animX.value < -threshold && canSwipeLeft) {
                                    haptics.confirm()
                                    wasSwipeTriggered = true
                                    scope.launch {
                                        animX.snapTo(0f)
                                        onTabSelected(activeTabIndex + 1)
                                    }
                                } else {
                                    scope.launch {
                                        animX.animateTo(
                                            0f,
                                            animationSpec = spring<Float>(
                                                dampingRatio = animConfig.dampingRatio,
                                                stiffness = (animConfig.stiffness * 0.78f).coerceAtLeast(60f)
                                            )
                                        )
                                    }
                                }
                            },
                            onDragCancel = {
                                hapticFiredCrossThreshold = false
                                scope.launch {
                                    animX.animateTo(
                                        0f,
                                        animationSpec = spring<Float>(
                                            dampingRatio = animConfig.dampingRatio,
                                            stiffness = (animConfig.stiffness * 0.78f).coerceAtLeast(60f)
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
                scale = if (isAdjacent) 0.94f + 0.06f * swipeProgress else 1f,
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

        // Phase 2: Active tab (on top) — with entrance + directional slide from tapping
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
            onTabSelected = onTabSelected,
            slideInFromPx = tabSlideDirection * contentWidth
        )
    }

    // ── Exit confirmation dialog ──
    if (showExitConfirm) {
        AlertDialog(
            onDismissRequest = { showExitConfirm = false },
            icon = { Icon(MaterialSymbolIcon("exit_to_app"), contentDescription = null, size = 28.dp) },
            title = { Text("Exit FieldMind?") },
            text = { Text("Your data is saved automatically. You can pick up where you left off.") },
            confirmButton = {
                Button(
                    onClick = {
                        haptics.confirm()
                        activity?.moveTaskToBack(true)
                        showExitConfirm = false
                    },
                    shape = CuteCardDefaults.ButtonShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) { Text("Exit") }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirm = false }) {
                    Text("Cancel")
                }
            }
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
        route.startsWith("field_lesson/") -> {
            val slug = route.removePrefix("field_lesson/")
            val lesson = FieldSkillsLessons.bySlug[slug]
            if (lesson != null) LessonViewerScreen(lesson = lesson, onBack = noop)
        }
        route == FieldMindScreen.Learn.route -> FieldMindLearnScreen(viewModel = viewModel, onBack = noop, onOpenReader = noopReader, onOpenLesson = {})
        route == FieldMindScreen.Reader.route -> LearnReaderScreen(url = "", title = "", onBack = noop)
        route == FieldMindScreen.FieldMode.route -> ObserveScreen(viewModel = viewModel, compactFieldMode = true, onBack = noop, onOpenDetail = noopDetail)
        route == FieldMindScreen.Questions.route -> QuestionsScreen(viewModel = viewModel, onBack = noop, onOpenDetail = noopDetail)
        route == FieldMindScreen.Hypotheses.route -> QuestionsScreen(viewModel = viewModel, onBack = noop, onOpenDetail = noopDetail)
        route == FieldMindScreen.DataTools.route -> DataToolsHubScreen(viewModel = viewModel, onBack = noop, onNavigate = noopNav, onOpenDetail = noopDetail)
        route == FieldMindScreen.Analysis.route -> ProjectsScreen(viewModel = viewModel, startTab = 0, onOpenDetail = noopDetail, onNavigate = noopNav)
        route == FieldMindScreen.Reports.route -> FieldMindReportScreen(viewModel = viewModel, onBack = noop)
        route == FieldMindScreen.Search.route -> ArchiveScreen(viewModel = viewModel, onBack = noop, onOpenDetail = noopDetail, onOpenReader = noopReader)
        route == FieldMindScreen.MapScreen.route -> MapFieldScreen(viewModel = viewModel, onBack = noop, onNavigate = noopNav, onOpenDetail = noopDetail)
        route == FieldMindScreen.ExportStudio.route -> BackupAndRestoreScreen(viewModel = viewModel, onBack = noop)
        route == FieldMindScreen.Changelog.route -> FieldMindChangelogScreen(onBack = noop)
        route == FieldMindScreen.Progress.route -> InsightsScreen(viewModel = viewModel, onBack = noop, onNavigate = noopNav, onOpenDetail = noopDetail)
        route == FieldMindScreen.Flashcards.route -> FlashcardSessionScreen(viewModel = viewModel, onBack = noop)
        route == FieldMindScreen.WeatherDatabase.route -> WeatherDatabaseScreen(viewModel = viewModel, onBack = noop, onOpenSettings = noop, onOpenDetail = noopDetail, onOpenWeatherCatalog = noop)
        route == FieldMindScreen.WeatherCatalog.route -> WeatherCatalogScreen(viewModel = viewModel, onBack = noop, onOpenSettings = noop)
        route == FieldMindScreen.ResearchSession.route -> ResearchSessionScreen(viewModel = viewModel, onBack = noop, onOpenDetail = noopDetail)

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
        route == FieldMindScreen.CompassTool.route -> CompassToolScreen(viewModel = viewModel, onBack = noop)
        route == FieldMindScreen.LevelTool.route -> LevelToolScreen(viewModel = viewModel, onBack = noop)
        route == FieldMindScreen.VoiceNotes.route -> VoiceNotesScreen(viewModel = viewModel, onBack = noop)
        route == FieldMindScreen.MediaGallery.route -> MediaGalleryScreen(viewModel = viewModel, onBack = noop, onOpenDetail = noopDetail)
        route == FieldMindScreen.CitationManager.route -> CitationManagerScreen(viewModel = viewModel, onBack = noop, onOpenDetail = noopDetail)
        route == FieldMindScreen.Collaboration.route -> CollaborationScreen(viewModel = viewModel, onBack = noop, onOpenExport = noop)
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

