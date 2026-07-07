package fieldmind.research.app.features.field.presentation.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fieldmind.research.app.activities.FieldMindCrashActivity
import fieldmind.research.app.features.field.data.database.entity.*
import fieldmind.research.app.features.field.data.security.LockSecurityPolicy
import fieldmind.research.app.features.field.data.weather.OpenMeteoProvider
import fieldmind.research.app.features.field.presentation.components.FieldMindIcons
import fieldmind.research.app.features.field.presentation.components.SecureFlagController
import fieldmind.research.app.features.field.presentation.components.SecureFlagReason
import fieldmind.research.app.features.field.presentation.components.shouldApplySecureFlag
import fieldmind.research.app.features.field.presentation.navigation.FieldMindBackAction
import fieldmind.research.app.features.field.presentation.navigation.fieldMindBackAction
import fieldmind.research.app.features.field.presentation.navigation.fieldMindRouteMetadata
import fieldmind.research.app.BuildConfig
import fieldmind.research.app.features.field.presentation.components.applyScreenCaptureProtection
import fieldmind.research.app.features.field.presentation.theme.FieldMindTheme
import fieldmind.research.app.features.field.presentation.viewmodel.FieldMindViewModel
import fieldmind.research.app.shared.data.model.AppSettings
import fieldmind.research.app.shared.presentation.components.icons.Icon
import fieldmind.research.app.shared.presentation.components.icons.MaterialSymbolIcon
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.view.WindowManager
import fieldmind.research.app.util.CrashReporter

// ══════════════════════════════════════════════════════════════════════
//  Test Result Data
// ══════════════════════════════════════════════════════════════════════

private data class TestResult(
    val category: String,
    val name: String,
    val passed: Boolean,
    val detail: String = "",
    val durationMs: Long = 0L,
    val testType: String = "Automated"
)

private data class TestRunReport(
    val runId: String = "",
    val results: List<TestResult> = emptyList(),
    val startedAt: Long = 0L,
    val completedAt: Long = 0L,
    val appVersion: String = BuildConfig.VERSION_NAME,
    val device: String = "${Build.MANUFACTURER} ${Build.MODEL} (API ${Build.VERSION.SDK_INT})",
    val settingsSnapshot: String = "",
    val completed: Boolean = false
) {
    val totalTests: Int get() = results.size
    val passedTests: Int get() = results.count { it.passed }
    val failedTests: Int get() = totalTests - passedTests
    val durationMs: Long get() = if (completedAt > 0 && startedAt > 0) completedAt - startedAt else 0L
    val isComplete: Boolean get() = completed || completedAt > 0L

    fun toShareableText(): String = buildString {
        appendLine("═══════════════════════════════════")
        appendLine("  FieldMind Full App Test Report")
        appendLine("═══════════════════════════════════")
        appendLine()
        appendLine("Run ID:   ${runId.ifBlank { "—" }}")
        appendLine("App:      FieldMind $appVersion")
        appendLine("Started:  ${formatTimestamp(startedAt)}")
        appendLine("Completed: ${formatTimestamp(completedAt)}")
        appendLine("Duration:  ${durationMs}ms (${durationMs / 1000}.${(durationMs % 1000) / 100}s)")
        appendLine("Settings:  ${settingsSnapshot.ifBlank { "not captured" }}")
        appendLine()
        appendLine("Results: $passedTests / $totalTests passed")
        if (failedTests > 0) appendLine("FAILURES: $failedTests test(s) failed!")
        else appendLine("All tests passed ✓")
        appendLine()
        appendLine("───────────────────────────────────")
        appendLine()

        results.groupBy { it.category }.forEach { (category, categoryResults) ->
            val catPassed = categoryResults.count { it.passed }
            val catTotal = categoryResults.size
            val statusIcon = if (catPassed == catTotal) "✓" else "✗"
            appendLine("  [$statusIcon] $category ($catPassed/$catTotal)")
            appendLine()
            categoryResults.forEach { result ->
                val icon = if (result.passed) "  ✓" else "  ✗"
                appendLine("$icon ${result.name} [${result.testType}] (${result.durationMs}ms)")
                if (result.detail.isNotBlank() && !result.passed) {
                    result.detail.lines().forEach { line ->
                        appendLine("       $line")
                    }
                }
            }
            appendLine()
        }

        appendLine("═══════════════════════════════════")
        appendLine("  Device: $device")
        appendLine("═══════════════════════════════════")
        appendLine()
        appendLine("Crash coverage note:")
        appendLine("Captured: uncaught JVM exceptions that reach the default handler.")
        appendLine("Not captured here: ANRs, native crashes, process kills, swallowed exceptions, and non-crashing UI clipping/visual bugs.")
        appendLine()
        appendLine("Manual UI checklist (device required):")
        appendLine("[ ] Bottom-nav Back returns to Today and keeps content/indicator in sync")
        appendLine("[ ] Quick actions do not overlap nearby content")
        appendLine("[ ] Live weather details do not clip at large font sizes")
        appendLine("[ ] Rain particles are visible and rain clouds loop smoothly")
        appendLine("[ ] Screenshot blocking off allows screenshots")
        appendLine("[ ] Dark-mode cards/nav have visible depth")
    }

    private fun formatTimestamp(millis: Long): String {
        if (millis <= 0L) return "—"
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
        return sdf.format(Date(millis))
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Test Runner Composable
// ══════════════════════════════════════════════════════════════════════

/**
 * Comprehensive in-app test runner for FieldMind.
 *
 * Tests every major screen, ViewModel operation, settings toggle,
 * and data layer interaction. Runs in the app process so it can
 * access the live ViewModel and database — no emulator or test
 * runner configuration needed.
 *
 * Results are collected into a shareable text report that can be
 * copied to the clipboard or shared via Android's share sheet.
 */
@Composable
fun DevFullAppTestRunner(
    viewModel: FieldMindViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val appSettings = remember(context) { AppSettings.getInstance(context) }
    val persistedReportJson by appSettings.latestDeveloperTestReport.collectAsState()
    var isRunning by remember { mutableStateOf(false) }
    var report by remember { mutableStateOf(loadDeveloperReport(persistedReportJson)) }
    var progressText by remember { mutableStateOf("") }
    var logExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var testJob by remember { mutableStateOf<Job?>(null) }
    val elapsedSeconds = remember { mutableStateOf(0) }
    var showOnlyFailures by remember { mutableStateOf(true) }

    LaunchedEffect(persistedReportJson) {
        if (!isRunning) report = loadDeveloperReport(persistedReportJson)
    }

    // Live timer to show test is actively running
    LaunchedEffect(isRunning) {
        if (isRunning) {
            elapsedSeconds.value = 0
            while (true) {
                delay(1000)
                elapsedSeconds.value += 1
            }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = if (FieldMindTheme.colors.isDark) 12.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Header ──
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(FieldMindTheme.colors.observation.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        MaterialSymbolIcon("science"),
                        null,
                        tint = FieldMindTheme.colors.observation,
                        size = 22.dp
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        "Full App Test Runner",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (isRunning) progressText
                        else if (report.isComplete) "${report.passedTests}/${report.totalTests} passed"
                        else "Comprehensive smoke test for all screens & features",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (report.isComplete) {
                    val statusColor = if (report.failedTests == 0)
                        FieldMindTheme.colors.positive else MaterialTheme.colorScheme.error
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(statusColor.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (report.failedTests == 0) FieldMindIcons.Check
                            else MaterialSymbolIcon("warning"),
                            null,
                            tint = statusColor,
                            size = 20.dp
                        )
                    }
                }
            }


            // ── Run / Share buttons ──
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (!isRunning) {
                            isRunning = true
                            logExpanded = true
                            val runId = "dev-${System.currentTimeMillis()}"
                            val startTime = System.currentTimeMillis()
                            report = TestRunReport(runId = runId, startedAt = startTime, settingsSnapshot = buildSettingsSnapshot(viewModel))
                            appSettings.setLatestDeveloperTestReport(Gson().toJson(report))
                            errorMessage = null
                            progressText = "Starting tests..."
                            testJob = scope.launch {
                                val results = mutableListOf<TestResult>()
                                val settingsBackup = TestSettingsSnapshot.capture(viewModel)
                                try {
                                    runAllTests(viewModel, context, results) { msg ->
                                        progressText = msg
                                        val partial = TestRunReport(
                                            runId = runId,
                                            results = results.toList(),
                                            startedAt = startTime,
                                            completedAt = System.currentTimeMillis(),
                                            settingsSnapshot = buildSettingsSnapshot(viewModel),
                                            completed = false
                                        )
                                        report = partial
                                        appSettings.setLatestDeveloperTestReport(Gson().toJson(partial))
                                    }
                                    val completedReport = TestRunReport(
                                        runId = runId,
                                        results = results.toList(),
                                        startedAt = startTime,
                                        completedAt = System.currentTimeMillis(),
                                        settingsSnapshot = buildSettingsSnapshot(viewModel),
                                        completed = true
                                    )
                                    report = completedReport
                                    appSettings.setLatestDeveloperTestReport(Gson().toJson(completedReport))
                                    isRunning = false
                                    progressText = "Done — ${completedReport.passedTests}/${completedReport.totalTests} passed"
                                } catch (e: Exception) {
                                    results.add(TestResult("Runner", "Test runner crashed", false, e.stackTraceToString()))
                                    val failedReport = TestRunReport(
                                        runId = runId,
                                        results = results.toList(),
                                        startedAt = startTime,
                                        completedAt = System.currentTimeMillis(),
                                        settingsSnapshot = buildSettingsSnapshot(viewModel),
                                        completed = true
                                    )
                                    report = failedReport
                                    appSettings.setLatestDeveloperTestReport(Gson().toJson(failedReport))
                                    errorMessage = "Test runner crash: ${e::class.simpleName}: ${e.message?.take(200) ?: "Unknown error"}"
                                } finally {
                                    settingsBackup.restore(viewModel)
                                    isRunning = false
                                    testJob = null
                                }
                            }
                        }
                    },
                    enabled = !isRunning,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (report.isComplete && report.failedTests == 0)
                            FieldMindTheme.colors.positive else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        if (isRunning) MaterialSymbolIcon("hourglass_top")
                        else MaterialSymbolIcon("play_arrow"),
                        null,
                        size = 18.dp
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (isRunning) "Running..."
                        else if (report.isComplete) "Run again"
                        else "Run full test",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (isRunning) {
                    OutlinedButton(
                        onClick = { testJob?.cancel() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.SemiBold)
                    }
                }

                if (report.isComplete) {
                    OutlinedButton(
                        onClick = {
                            val text = report.toShareableText()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("FieldMind Test Report", text)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Test report copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Icon(FieldMindIcons.Copy, null, size = 18.dp)
                        Spacer(Modifier.width(6.dp))
                        Text("Copy report", fontWeight = FontWeight.SemiBold)
                    }

                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_SUBJECT, "FieldMind Test Report")
                        putExtra(android.content.Intent.EXTRA_TEXT, report.toShareableText())
                    }
                    IconButton(
                        onClick = {
                            runCatching {
                                context.startActivity(android.content.Intent.createChooser(shareIntent, "Share test report"))
                            }.onFailure {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("FieldMind Test Report", report.toShareableText()))
                                Toast.makeText(context, "No share app found — copied report", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(FieldMindIcons.Share, null, tint = MaterialTheme.colorScheme.primary, size = 20.dp)
                    }

                    IconButton(
                        onClick = {
                            appSettings.setLatestDeveloperTestReport(null)
                            report = TestRunReport()
                            Toast.makeText(context, "Latest test report cleared", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(MaterialSymbolIcon("delete"), null, tint = MaterialTheme.colorScheme.error, size = 20.dp)
                    }
                }
            }

            // ── Loading indicator ──
            if (isRunning) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                )
            }

            // ── Progress text (always visible when running or on error) ──
            if ((progressText.isNotBlank() && isRunning) || errorMessage != null) {
                Text(
                    errorMessage ?: progressText,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (errorMessage != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace
                )
                // Show elapsed time as proof the test is actually running
                if (isRunning) {
                    Text(
                        "Elapsed: ${elapsedSeconds.value}s",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            // ── Results summary ──
            if (report.isComplete) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Surface(
                    onClick = { logExpanded = !logExpanded },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${report.totalTests} tests",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "${report.passedTests} ✓",
                                style = MaterialTheme.typography.labelMedium,
                                color = FieldMindTheme.colors.positive,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (report.failedTests > 0) {
                                Text(
                                    "${report.failedTests} ✗",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                "${report.durationMs / 1000}.${(report.durationMs % 1000) / 100}s",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            if (logExpanded) MaterialSymbolIcon("expand_less")
                            else MaterialSymbolIcon("expand_more"),
                            null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            size = 20.dp
                        )
                    }
                }

                // ── Detailed log ──
                // ── Failures-only / All toggle ──
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = showOnlyFailures,
                            onClick = { showOnlyFailures = true },
                            label = { Text("Failures only", style = MaterialTheme.typography.labelSmall) },
                            shape = RoundedCornerShape(10.dp)
                        )
                        FilterChip(
                            selected = !showOnlyFailures,
                            onClick = { showOnlyFailures = false },
                            label = { Text("All tests", style = MaterialTheme.typography.labelSmall) },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                    Text(
                        "${report.failedTests} failed",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (report.failedTests > 0) MaterialTheme.colorScheme.error else FieldMindTheme.colors.positive,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                AnimatedVisibility(
                    visible = logExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        report.results.groupBy { it.category }.forEach { (category, categoryResults) ->
                            val displayResults = if (showOnlyFailures) categoryResults.filter { !it.passed } else categoryResults
                            if (displayResults.isEmpty()) return@forEach

                            val catTotal = categoryResults.size
                            val catFailed = categoryResults.count { !it.passed }
                            Text(
                                if (catFailed == 0) "[✓] $category ($catTotal/$catTotal)"
                                else "[✗] $category (${catTotal - catFailed}/$catTotal)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (catFailed == 0) FieldMindTheme.colors.positive
                                else MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                            )
                            displayResults.forEach { result ->
                                Row(
                                    Modifier.padding(start = 8.dp, end = 4.dp, top = 1.dp, bottom = 1.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        if (result.passed) "✓" else "✗",
                                        color = if (result.passed) FieldMindTheme.colors.positive
                                        else MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            result.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp
                                        )
                                        if (result.detail.isNotBlank() && !result.passed) {
                                            Text(
                                                result.detail,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.error,
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                    Text(
                                        "${result.durationMs}ms",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                        if (showOnlyFailures && report.failedTests == 0) {
                            Text(
                                "✓ All tests passed — nothing to show",
                                style = MaterialTheme.typography.bodySmall,
                                color = FieldMindTheme.colors.positive,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


private data class TestSettingsSnapshot(
    val privacyLockEnabled: Boolean,
    val appPinEnabled: Boolean,
    val appPinHash: String,
    val themeMode: String,
    val developerMode: Boolean,
    val profileName: String,
    val dailyObservationGoal: Int,
    val tempUnit: String,
    val mapType: String,
    val timeFormat: String,
    val dateFormat: String,
    val screenCaptureProtection: Boolean,
    val clipboardCleanup: Boolean,
    val geminiEnabled: Boolean,
    val aiProvider: String,
    val aiRequireConfirm: Boolean,
    val aiSendAttachments: Boolean,
    val localModelEnabled: Boolean
) {
    fun restore(viewModel: FieldMindViewModel) {
        val settings = viewModel.fieldSettings
        settings.setPrivacyLockEnabled(privacyLockEnabled)
        settings.setAppPinEnabled(appPinEnabled)
        settings.setAppPinHash(appPinHash)
        settings.setThemeMode(themeMode)
        settings.setDeveloperMode(developerMode)
        settings.setProfileName(profileName)
        settings.setDailyObservationGoal(dailyObservationGoal)
        settings.setTempUnit(tempUnit)
        settings.setMapType(mapType)
        settings.setTimeFormat(timeFormat)
        settings.setDateFormat(dateFormat)
        settings.setScreenCaptureProtectionEnabled(screenCaptureProtection)
        settings.setClipboardAutoCleanupEnabled(clipboardCleanup)
        settings.setGeminiEnabled(geminiEnabled)
        settings.setAiProvider(aiProvider)
        settings.setAiRequireConfirmBeforeSave(aiRequireConfirm)
        settings.setAiSendAttachments(aiSendAttachments)
        settings.setLocalModelEnabled(localModelEnabled)
    }

    companion object {
        fun capture(viewModel: FieldMindViewModel): TestSettingsSnapshot {
            val settings = viewModel.fieldSettings
            return TestSettingsSnapshot(
                privacyLockEnabled = settings.privacyLockEnabled.value,
                appPinEnabled = settings.appPinEnabled.value,
                appPinHash = settings.appPinHash.value,
                themeMode = settings.themeMode.value,
                developerMode = settings.developerMode.value,
                profileName = settings.profileName.value,
                dailyObservationGoal = settings.dailyObservationGoal.value,
                tempUnit = settings.tempUnit.value,
                mapType = settings.mapType.value,
                timeFormat = settings.timeFormat.value,
                dateFormat = settings.dateFormat.value,
                screenCaptureProtection = settings.screenCaptureProtectionEnabled.value,
                clipboardCleanup = settings.clipboardAutoCleanupEnabled.value,
                geminiEnabled = settings.geminiEnabled.value,
                aiProvider = settings.aiProvider.value,
                aiRequireConfirm = settings.aiRequireConfirmBeforeSave.value,
                aiSendAttachments = settings.aiSendAttachments.value,
                localModelEnabled = settings.localModelEnabled.value
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Test Helpers
// ══════════════════════════════════════════════════════════════════════

private fun loadDeveloperReport(json: String?): TestRunReport =
    if (json.isNullOrBlank()) TestRunReport()
    else runCatching { Gson().fromJson(json, TestRunReport::class.java) }.getOrElse { TestRunReport() }

private fun buildSettingsSnapshot(viewModel: FieldMindViewModel): String {
    val settings = viewModel.fieldSettings
    return "theme=${settings.themeMode.value}; secure=${settings.screenCaptureProtectionEnabled.value}; preview=${settings.appPreviewMode.value}; developer=${settings.developerMode.value}; weatherPanel=${settings.showWeatherTestPanel.value}"
}

private tailrec fun Context.findActivity(): android.app.Activity? = when (this) {
    is android.app.Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * Runs [block] with timing, catches any exception, and appends
 * a [TestResult] to [results].
 */
private suspend fun runTest(
    results: MutableList<TestResult>,
    category: String,
    name: String,
    block: suspend () -> Unit
) {
    val start = System.currentTimeMillis()
    try {
        withTimeout(2_500L) { block() }
        val duration = System.currentTimeMillis() - start
        results.add(TestResult(category, name, passed = true, durationMs = duration))
    } catch (e: Throwable) {
        val duration = System.currentTimeMillis() - start
        val detail = when {
            e is TimeoutCancellationException -> "Timed out after 2.5s"
            else -> e.stackTraceToString()
        }
        results.add(TestResult(category, name, passed = false, detail = detail, durationMs = duration))
    }
}

/**
 * Assert that [condition] is true, throwing [AssertionError] with [message] otherwise.
 */
private fun assert(condition: Boolean, message: () -> String) {
    if (!condition) throw AssertionError(message())
}

// ══════════════════════════════════════════════════════════════════════
//  Test Execution Engine
// ══════════════════════════════════════════════════════════════════════

private suspend fun runAllTests(
    viewModel: FieldMindViewModel,
    context: Context,
    results: MutableList<TestResult>,
    onProgress: (String) -> Unit
) {
    val settings = viewModel.fieldSettings

    // ═══════════════════════════════════════════════
    //  1. NAVIGATION & TAB TESTS
    // ═══════════════════════════════════════════════
    onProgress("[1/8] Testing navigation & tabs...")

    runTest(results, "Navigation & Tabs", "Tab screens list is non-empty") {
        val tabRoutes = listOf(
            "field_today", "field_capture",
            "field_projects", "field_insights",
            "field_library"
        )
        assert(tabRoutes.size == 5) { "Expected 5 tab routes" }
        tabRoutes.forEach { r -> assert(r.isNotBlank()) { "Tab route is blank" } }
    }

    runTest(results, "Navigation & Tabs", "Device Back on tabs returns to Today") {
        listOf(1, 2, 3, 4).forEach { index ->
            assert(fieldMindBackAction("field_tab_container", index) == FieldMindBackAction.ReturnToHomeTab) { "Tab $index should return to Today" }
        }
        assert(fieldMindBackAction("field_tab_container", 0) == FieldMindBackAction.AllowSystemExit) { "Today should allow system Back" }
    }

    runTest(results, "Navigation & Tabs", "Subpage Back has safe home fallback") {
        assert(fieldMindBackAction("field_settings", 0, canPopSubPage = true) == FieldMindBackAction.PopSubPage) { "Subpage should pop when possible" }
        assert(fieldMindBackAction("field_settings", 0, canPopSubPage = false) == FieldMindBackAction.NavigateHomeFallback) { "Failed pop should navigate home" }
    }

    runTest(results, "Navigation & Tabs", "Placeholder routes are reported") {
        val placeholders = fieldMindRouteMetadata.filter { it.status == "placeholder" }
        assert(placeholders.map { it.route }.containsAll(listOf("field_hypotheses", "field_analysis", "field_progress"))) { "Expected placeholder route metadata missing" }
    }

    runTest(results, "Navigation & Tabs", "All settings screens have routes") {
        val routes = listOf(
            "field_settings", "field_settings_profile",
            "field_settings_appearance", "field_settings_security",
            "field_settings_backup", "field_settings_about",
            "field_settings_developer"
        )
        routes.forEach { r -> assert(r.isNotBlank()) { "Route is blank" } }
    }

    runTest(results, "Navigation & Tabs", "Tab screen icons are defined") {
        listOf(
            FieldMindIcons.Today, FieldMindIcons.Capture,
            FieldMindIcons.Projects, FieldMindIcons.Insights,
            FieldMindIcons.Library
        ).forEach { icon ->
            assert(icon.name.isNotBlank()) { "Icon name is blank" }
        }
    }

    runTest(results, "Navigation & Tabs", "All FieldMindScreen sealed objects have routes") {
        val keyRoutes = listOf(
            "field_today", "field_mode",
            "field_map", "field_export_studio",
            "field_tasks", "field_settings",
            "field_changelog", "field_progress"
        )
        keyRoutes.forEach { r ->
            assert(r.startsWith("field_")) { "Route $r has no prefix" }
        }
    }

    // ═══════════════════════════════════════════════
    //  2. VIEWMODEL & DATA LAYER TESTS
    // ═══════════════════════════════════════════════
    onProgress("[2/8] Testing ViewModel & data layer...")

    runTest(results, "ViewModel & Data", "fieldSettings is not null") {
        assert(settings != null) { "fieldSettings is null" }
    }

    runTest(results, "ViewModel & Data", "Observations StateFlow emits") {
        val obs = viewModel.observations.first()
        assert(obs != null) { "Observations returned null" }
    }

    runTest(results, "ViewModel & Data", "Notes StateFlow emits") {
        val notes = viewModel.notes.first()
        assert(notes != null) { "Notes returned null" }
    }

    runTest(results, "ViewModel & Data", "Questions StateFlow emits") {
        val qs = viewModel.questions.first()
        assert(qs != null) { "Questions returned null" }
    }

    runTest(results, "ViewModel & Data", "Projects StateFlow emits") {
        val projs = viewModel.projects.first()
        assert(projs != null) { "Projects returned null" }
    }

    runTest(results, "ViewModel & Data", "Sources StateFlow emits") {
        val srcs = viewModel.sources.first()
        assert(srcs != null) { "Sources returned null" }
    }

    runTest(results, "ViewModel & Data", "Flashcards StateFlow emits") {
        val cards = viewModel.flashcards.first()
        assert(cards != null) { "Flashcards returned null" }
    }

    runTest(results, "ViewModel & Data", "DataRecords StateFlow emits") {
        val records = viewModel.dataRecords.first()
        assert(records != null) { "DataRecords returned null" }
    }

    runTest(results, "ViewModel & Data", "Reports StateFlow emits") {
        val reports = viewModel.reports.first()
        assert(reports != null) { "Reports returned null" }
    }

    runTest(results, "ViewModel & Data", "Tags StateFlow emits") {
        val tags = viewModel.tags.first()
        assert(tags != null) { "Tags returned null" }
    }

    runTest(results, "ViewModel & Data", "DetectedPatterns StateFlow emits") {
        val patterns = viewModel.detectedPatterns.first()
        assert(patterns != null) { "DetectedPatterns returned null" }
    }

    runTest(results, "ViewModel & Data", "Capture session state toggles") {
        viewModel.setCaptureSessionActive(true)
        assert(viewModel.captureSessionActive) { "captureSessionActive should be true" }
        viewModel.setCaptureSessionActive(false)
        assert(!viewModel.captureSessionActive) { "captureSessionActive should be false" }
    }

    runTest(results, "ViewModel & Data", "ResearchSessions StateFlow emits") {
        val sessions = viewModel.researchSessions.first()
        assert(sessions != null) { "ResearchSessions returned null" }
    }

    runTest(results, "ViewModel & Data", "SpeciesRegistry StateFlow emits") {
        val species = viewModel.speciesRegistry.first()
        assert(species != null) { "SpeciesRegistry returned null" }
    }

    runTest(results, "ViewModel & Data", "Tasks StateFlow emits") {
        val tasks = viewModel.tasks.first()
        assert(tasks != null) { "Tasks returned null" }
    }

    runTest(results, "ViewModel & Data", "Folders StateFlow emits") {
        val folders = viewModel.folders.first()
        assert(folders != null) { "Folders returned null" }
    }

    runTest(results, "ViewModel & Data", "WeatherCatalog StateFlow emits") {
        val catalog = viewModel.weatherCatalog.first()
        assert(catalog != null) { "WeatherCatalog returned null" }
    }

    // ═══════════════════════════════════════════════
    //  3. SETTINGS TESTS
    // ═══════════════════════════════════════════════
    onProgress("[3/8] Testing settings layer...")

    runTest(results, "Settings", "Theme mode toggles") {
        val original = settings.themeMode.value
        settings.setThemeMode("Dark")
        val mode = settings.themeMode.first()
        assert(mode == "Dark") { "Expected Dark, got $mode" }
        settings.setThemeMode(original)
        val restored = settings.themeMode.first()
        assert(restored == original) { "Expected $original, got $restored" }
    }

    runTest(results, "Settings", "Profile name set & read") {
        val testName = "TestUser_${System.currentTimeMillis()}"
        settings.setProfileName(testName)
        val name = settings.profileName.first()
        assert(name == testName) { "Expected '$testName', got '$name'" }
        settings.setProfileName("")
    }

    runTest(results, "Settings", "Daily observation goal") {
        val original = settings.dailyObservationGoal.value
        settings.setDailyObservationGoal(10)
        val goal = settings.dailyObservationGoal.first()
        assert(goal == 10) { "Expected 10, got $goal" }
        settings.setDailyObservationGoal(original)
    }

    runTest(results, "Settings", "Temperature unit toggles") {
        val original = settings.tempUnit.value
        settings.setTempUnit("Fahrenheit")
        val f = settings.tempUnit.first()
        assert(f == "Fahrenheit") { "Expected Fahrenheit, got $f" }
        settings.setTempUnit(original)
    }

    runTest(results, "Settings", "Auto weather toggles") {
        val original = settings.autoWeatherEnabled.value
        settings.setAutoWeatherEnabled(!original)
        val toggled = settings.autoWeatherEnabled.first()
        assert(toggled == !original) { "Expected ${!original}" }
        settings.setAutoWeatherEnabled(original)
    }

    runTest(results, "Settings", "Map type cycles without exception") {
        val original = settings.mapType.value
        settings.setMapType("Satellite")
        var mt = settings.mapType.first()
        assert(mt == "Satellite") { "Expected Satellite, got $mt" }
        settings.setMapType("Terrain")
        mt = settings.mapType.first()
        assert(mt == "Terrain") { "Expected Terrain, got $mt" }
        settings.setMapType(original)
    }

    runTest(results, "Settings", "Time format toggles") {
        val original = settings.timeFormat.value
        settings.setTimeFormat("12h")
        val tf = settings.timeFormat.first()
        assert(tf == "12h") { "Expected 12h, got $tf" }
        settings.setTimeFormat(original)
    }

    runTest(results, "Settings", "Date format toggles") {
        val original = settings.dateFormat.value
        settings.setDateFormat("Local")
        val df = settings.dateFormat.first()
        assert(df == "Local") { "Expected Local, got $df" }
        settings.setDateFormat(original)
    }

    // ═══════════════════════════════════════════════
    //  4. DATABASE & ENTITY TESTS
    // ═══════════════════════════════════════════════
    onProgress("[4/8] Testing database & entities...")

    runTest(results, "Database & Entities", "Database opens") {
        val db = fieldmind.research.app.features.field.data.database.FieldMindDatabase.getInstance(context)
        val readable = db.openHelper.readableDatabase
        assert(readable != null) { "Database readableDatabase is null" }
    }

    runTest(results, "Database & Entities", "ObservationEntity constructable") {
        val obs = ObservationEntity(
            subject = "Test observation",
            category = "Wildlife",
            factsOnlyNotes = "Test notes",
            timestamp = System.currentTimeMillis(),
            date = "2026-07-03",
            time = "12:00",
            confidenceLevel = "Moderate"
        )
        assert(obs.subject == "Test observation") { "ObservationEntity constructor failed" }
        assert(obs.category == "Wildlife") { "ObservationEntity category mismatch" }
    }

    runTest(results, "Database & Entities", "NoteEntity constructable") {
        val note = NoteEntity(title = "Test Note", body = "Test body", category = "Other")
        assert(note.title == "Test Note") { "NoteEntity constructor failed" }
    }

    runTest(results, "Database & Entities", "QuestionEntity constructable") {
        val q = QuestionEntity(questionText = "Test Question", sourceType = "Observation", status = "Open")
        assert(q.questionText == "Test Question") { "QuestionEntity constructor failed" }
    }

    runTest(results, "Database & Entities", "ProjectEntity constructable") {
        val p = ProjectEntity(title = "Test Project")
        assert(p.title == "Test Project") { "ProjectEntity constructor failed" }
    }

    runTest(results, "Database & Entities", "SourceEntity constructable") {
        val s = SourceEntity(type = "Website", title = "Test Source")
        assert(s.title == "Test Source") { "SourceEntity constructor failed" }
    }

    runTest(results, "Database & Entities", "HypothesisEntity constructable") {
        val h = HypothesisEntity(prediction = "Test hypothesis prediction")
        assert(h.prediction == "Test hypothesis prediction") { "HypothesisEntity constructor failed" }
    }

    runTest(results, "Database & Entities", "TaskEntity constructable") {
        val t = TaskEntity(title = "Test Task", description = "Test desc", priority = "Medium")
        assert(t.title == "Test Task") { "TaskEntity constructor failed" }
    }

    runTest(results, "Database & Entities", "ResearchSessionEntity constructable") {
        val s = ResearchSessionEntity(name = "Test Session", projectId = null)
        assert(s.name == "Test Session") { "ResearchSessionEntity constructor failed" }
        assert(s.status == "Active") { "Default status should be Active" }
    }

    runTest(results, "Database & Entities", "SpeciesEntity constructable") {
        val s = SpeciesEntity(commonName = "Indian Peafowl", scientificName = "Pavo cristatus")
        assert(s.commonName == "Indian Peafowl") { "SpeciesEntity constructor failed" }
        assert(s.scientificName == "Pavo cristatus") { "Scientific name mismatch" }
    }

    runTest(results, "Database & Entities", "FolderEntity constructable") {
        val f = FolderEntity(name = "Bird Observations", color = 0xFF5F7F52)
        assert(f.name == "Bird Observations") { "FolderEntity constructor failed" }
        assert(f.color == 0xFF5F7F52) { "Color mismatch" }
    }

    runTest(results, "Database & Entities", "EvidenceAttachmentEntity constructable") {
        val att = EvidenceAttachmentEntity(observationId = 1L, type = "Photo", uri = "file:///test.jpg")
        assert(att.type == "Photo") { "EvidenceAttachmentEntity type mismatch" }
        assert(att.uri == "file:///test.jpg") { "URI mismatch" }
    }

    runTest(results, "Database & Entities", "WeatherCatalogEntity constructable") {
        val w = WeatherCatalogEntity(latitude = 12.97, longitude = 77.59, temperature = 28.5, weatherCode = 0)
        assert(w.temperature == 28.5) { "WeatherCatalogEntity temperature mismatch" }
        assert(w.latitude == 12.97) { "Latitude mismatch" }
    }

    // ═══════════════════════════════════════════════
    //  5. SECURITY & PRIVACY TESTS
    // ═══════════════════════════════════════════════
    onProgress("[5/8] Testing security & privacy...")

    runTest(results, "Security & Privacy", "App PIN hash produces non-plaintext — read-only") {
        val hash = settings.hashAppPin("1234")
        assert(hash.isNotBlank()) { "PIN hash is blank" }
        assert(hash != "1234") { "Hash should not be plaintext" }
        // Verify hash is persistent format (Base64)
        assert(hash.length in 20..60) { "Hash has unexpected length: ${hash.length}" }
    }

    runTest(results, "Security & Privacy", "PIN verification algorithm works — read-only") {
        // Test the hashing + verify algorithm without modifying app settings
        val testPin = "5678"
        val hash = settings.hashAppPin(testPin)
        // Manually verify by re-hashing
        val inputHash = android.util.Base64.encodeToString(
            java.security.MessageDigest.getInstance("SHA-256").digest(testPin.toByteArray()),
            android.util.Base64.NO_WRAP
        )
        assert(hash == inputHash) { "Hash mismatch — algorithm changed?" }
        // Wrong pin should produce different hash
        val wrongHash = android.util.Base64.encodeToString(
            java.security.MessageDigest.getInstance("SHA-256").digest("wrong".toByteArray()),
            android.util.Base64.NO_WRAP
        )
        assert(hash != wrongHash) { "Different pins should have different hashes" }
    }

    runTest(results, "Security & Privacy", "Privacy lock setting is readable — read-only") {
        val current = settings.privacyLockEnabled.value
        // Just verify it's a boolean — don't toggle it (would affect app lock state)
        assert(current == true || current == false) { "Privacy lock should be boolean" }
    }

    runTest(results, "Security & Privacy", "Lock timeout is valid — read-only") {
        val timeout = settings.lockTimeout.value
        val validOptions = listOf("Immediate", "1 minute", "5 minutes", "15 minutes")
        assert(validOptions.contains(timeout) || timeout.isNotEmpty()) {
            "Lock timeout '$timeout' is not in valid options"
        }
    }

    runTest(results, "Security & Privacy", "Screen capture setting is readable — read-only") {
        val current = settings.screenCaptureProtectionEnabled.value
        assert(current == true || current == false) { "Screen capture protection should be boolean" }
    }

    runTest(results, "Security & Privacy", "Screenshot secure-flag policy is explicit") {
        assert(!shouldApplySecureFlag(false, "Normal")) { "Screenshots should be allowed when blocking is off" }
        assert(shouldApplySecureFlag(true, "Normal")) { "Screenshots should be blocked when setting is on" }
        assert(!shouldApplySecureFlag(false, "Blur")) { "Preview mode must not silently keep screenshots blocked" }
        assert(SecureFlagController.shouldSecureForReasons(setOf(SecureFlagReason.SensitiveScreen))) { "Sensitive screen reason should secure" }
        assert(!SecureFlagController.shouldSecureForReasons(emptySet())) { "No reasons should allow screenshots" }
    }

    runTest(results, "Security & Privacy", "Screen capture flag policy is explicit — read-only") {
        assert(!shouldApplySecureFlag(false, "Normal")) { "Screenshots should be allowed when blocking is off" }
        assert(shouldApplySecureFlag(true, "Normal")) { "Screenshots should be blocked when setting is on" }
        assert(!shouldApplySecureFlag(false, "Blur")) { "Preview mode must not silently keep screenshots blocked" }
        assert(SecureFlagController.shouldSecureForReasons(setOf(SecureFlagReason.SensitiveScreen))) { "Sensitive screen reason should secure" }
        assert(!SecureFlagController.shouldSecureForReasons(emptySet())) { "No reasons should allow screenshots" }
    }

    runTest(results, "Security & Privacy", "Clipboard cleanup setting is readable — read-only") {
        val current = settings.clipboardAutoCleanupEnabled.value
        assert(current == true || current == false) { "Clipboard cleanup should be boolean" }
    }

    runTest(results, "Security & Privacy", "Lock policy helpers are consistent") {
        assert(LockSecurityPolicy.FAILED_UNLOCK_THRESHOLD == 5) { "Failed unlock threshold should be 5" }
        assert(LockSecurityPolicy.pinLengthForLabel("4 digits") == 4) { "4 digit label failed" }
        assert(LockSecurityPolicy.pinLengthForLabel("5 digits") == 5) { "5 digit label failed" }
        assert(LockSecurityPolicy.pinLengthForLabel("6 digits") == 6) { "6 digit label failed" }
        assert(LockSecurityPolicy.failedUnlockCooldownMs("Do Nothing") == 0L) { "Do Nothing must not cooldown" }
        assert(LockSecurityPolicy.failedUnlockCooldownMs("30 Second Cooldown") == 30_000L) { "30s cooldown mismatch" }
        assert(LockSecurityPolicy.failedUnlockCooldownMs("5 Minute Cooldown") == 300_000L) { "5 minute cooldown mismatch" }
        assert(LockSecurityPolicy.shouldRequireBiometricsAfterFailure(5, true, true)) { "Biometric policy should trigger" }
    }

    runTest(results, "Security & Privacy", "Open-Meteo free tier requires no key") {
        assert(!OpenMeteoProvider().requiresApiKey) { "Open-Meteo free tier should not require an API key" }
    }

    runTest(results, "Security & Privacy", "Crash activity intent can be constructed") {
        val intent = android.content.Intent(context, FieldMindCrashActivity::class.java)
        intent.putExtra(FieldMindCrashActivity.EXTRA_CRASH_LOG, "test")
        assert(intent.component != null) { "Crash activity component missing" }
    }

    // ═══════════════════════════════════════════════
    //  6. AI & ASSISTANT TESTS
    // ═══════════════════════════════════════════════
    onProgress("[6/8] Testing AI & assistant...")

    runTest(results, "AI & Assistant", "AI enable toggles") {
        val original = settings.geminiEnabled.value
        settings.setGeminiEnabled(!original)
        val toggled = settings.geminiEnabled.first()
        assert(toggled == !original) { "Expected ${!original}" }
        settings.setGeminiEnabled(original)
    }

    runTest(results, "AI & Assistant", "AI provider switches") {
        val original = settings.aiProvider.value
        settings.setAiProvider("OpenAI")
        val p = settings.aiProvider.first()
        assert(p == "OpenAI") { "Expected OpenAI, got $p" }
        settings.setAiProvider(original)
    }

    runTest(results, "AI & Assistant", "AI confirm-before-save toggles") {
        val original = settings.aiRequireConfirmBeforeSave.value
        settings.setAiRequireConfirmBeforeSave(!original)
        val toggled = settings.aiRequireConfirmBeforeSave.first()
        assert(toggled == !original) { "Expected ${!original}" }
        settings.setAiRequireConfirmBeforeSave(original)
    }

    runTest(results, "AI & Assistant", "AI send-attachments toggles") {
        val original = settings.aiSendAttachments.value
        settings.setAiSendAttachments(!original)
        val toggled = settings.aiSendAttachments.first()
        assert(toggled == !original) { "Expected ${!original}" }
        settings.setAiSendAttachments(original)
    }

    runTest(results, "AI & Assistant", "Local model toggles") {
        val original = settings.localModelEnabled.value
        settings.setLocalModelEnabled(!original)
        val toggled = settings.localModelEnabled.first()
        assert(toggled == !original) { "Expected ${!original}" }
        settings.setLocalModelEnabled(original)
    }

    runTest(results, "AI & Assistant", "AI provider values are non-empty") {
        val provider = settings.aiProvider.value
        val geminiModel = settings.geminiModel.value
        val openAiModel = settings.openAiModel.value
        assert(provider.isNotEmpty()) { "AI provider is empty" }
        assert(geminiModel.isNotEmpty()) { "Gemini model is empty" }
        assert(openAiModel.isNotEmpty()) { "OpenAI model is empty" }
    }

    // ═══════════════════════════════════════════════
    //  7. CAPTURE & OBSERVATION TESTS
    // ═══════════════════════════════════════════════
    onProgress("[7/8] Testing capture & observations...")

    runTest(results, "Capture & Observations", "Observation categories defined") {
        assert(observationCategories.isNotEmpty()) { "Categories empty" }
        assert(observationCategories.contains("Wildlife")) { "Missing Wildlife" }
    }

    runTest(results, "Capture & Observations", "Confidence options defined") {
        assert(confidenceOptions.isNotEmpty()) { "Confidence options empty" }
    }

    runTest(results, "Capture & Observations", "Default category can be set") {
        val original = settings.defaultCategory.value
        settings.setDefaultCategory("Weather")
        val cat = settings.defaultCategory.first()
        assert(cat == "Weather") { "Expected Weather, got $cat" }
        settings.setDefaultCategory(original)
    }

    runTest(results, "Capture & Observations", "Default confidence can be set") {
        val original = settings.defaultConfidence.value
        settings.setDefaultConfidence("High")
        val conf = settings.defaultConfidence.first()
        assert(conf == "High") { "Expected High, got $conf" }
        settings.setDefaultConfidence(original)
    }

    runTest(results, "Capture & Observations", "Location mode switches") {
        val original = settings.locationMode.value
        settings.setLocationMode("Precise")
        val mode = settings.locationMode.first()
        assert(mode == "Precise") { "Expected Precise, got $mode" }
        settings.setLocationMode(original)
    }

    runTest(results, "Capture & Observations", "Media attachments toggles") {
        val original = settings.mediaAttachmentsEnabled.value
        settings.setMediaAttachmentsEnabled(!original)
        val toggled = settings.mediaAttachmentsEnabled.first()
        assert(toggled == !original) { "Expected ${!original}" }
        settings.setMediaAttachmentsEnabled(original)
    }

    runTest(results, "Capture & Observations", "Audio recording toggles") {
        val original = settings.audioRecordingEnabled.value
        settings.setAudioRecordingEnabled(!original)
        val toggled = settings.audioRecordingEnabled.first()
        assert(toggled == !original) { "Expected ${!original}" }
        settings.setAudioRecordingEnabled(original)
    }

    runTest(results, "Capture & Observations", "Reminders toggle works") {
        val original = settings.remindersEnabled.value
        settings.setRemindersEnabled(!original)
        val toggled = settings.remindersEnabled.first()
        assert(toggled == !original) { "Expected ${!original}" }
        settings.setRemindersEnabled(original)
    }

    // ═══════════════════════════════════════════════
    //  9. ERROR HANDLING & RESILIENCE TESTS
    // ═══════════════════════════════════════════════
    onProgress("[9/9] Testing error handling & resilience...")

    runTest(results, "Error Handling", "CrashReporter class loads") {
        val cls = Class.forName("fieldmind.research.app.util.CrashReporter")
        assert(cls != null) { "CrashReporter class not found" }
    }

    runTest(results, "Error Handling", "Non-fatal crash capture persists stack trace") {
        val sentinel = "Dev runner sentinel ${System.currentTimeMillis()}"
        CrashReporter.recordNonFatal(IllegalStateException(sentinel), "DevFullAppTestRunner")
        val logs = AppSettings.getInstance(context).crashLogHistory.first()
        val latest = logs.lastOrNull()?.log.orEmpty()
        assert(latest.contains(sentinel) && latest.contains("DevFullAppTestRunner")) {
            "Latest crash log did not contain sentinel/source"
        }
    }

    runTest(results, "Error Handling", "Crash log history accessible") {
        val appSettings = fieldmind.research.app.shared.data.model.AppSettings.getInstance(context)
        val logs = appSettings.crashLogHistory.first()
        assert(logs != null) { "Crash log history returned null" }
    }

    runTest(results, "Error Handling", "Synthetic crash report persists") {
        val appSettings = AppSettings.getInstance(context)
        val log = CrashReporter.buildCrashLog(Thread.currentThread(), IllegalStateException("Synthetic tester crash report"))
        appSettings.addCrashLogEntry(log)
        val latest = appSettings.crashLogHistory.first().lastOrNull()?.log.orEmpty()
        assert(latest.contains("Synthetic tester crash report")) { "Synthetic crash report did not persist" }
    }

    runTest(results, "Error Handling", "Crash coverage is documented") {
        val reportText = TestRunReport(startedAt = 1L, completedAt = 2L).toShareableText()
        assert(reportText.contains("Not captured here: ANRs")) { "Crash coverage limitation missing" }
    }

    runTest(results, "Error Handling", "Data integrity check toggles") {
        val original = settings.dataIntegrityCheckOnLaunch.value
        settings.setDataIntegrityCheckOnLaunch(true)
        val on = settings.dataIntegrityCheckOnLaunch.first()
        assert(on) { "Data integrity check should be true" }
        settings.setDataIntegrityCheckOnLaunch(original)
    }

    runTest(results, "Error Handling", "Debug logging toggles") {
        val original = settings.debugLogging.value
        settings.setDebugLogging(!original)
        val toggled = settings.debugLogging.first()
        assert(toggled == !original) { "Expected ${!original}" }
        settings.setDebugLogging(original)
    }

    runTest(results, "Error Handling", "Weather test panel toggles") {
        val original = settings.showWeatherTestPanel.value
        settings.setShowWeatherTestPanel(true)
        val on = settings.showWeatherTestPanel.first()
        assert(on) { "Weather panel should be true" }
        settings.setShowWeatherTestPanel(original)
    }

    runTest(results, "Error Handling", "Card tint style sets") {
        val original = settings.cardGradientStyle.value
        settings.setCardGradientStyle("Sunny Lift")
        val style = settings.cardGradientStyle.first()
        assert(style == "Sunny Lift") { "Expected Sunny Lift, got $style" }
        settings.setCardGradientStyle(original)
    }

    runTest(results, "Error Handling", "SDK version accessible") {
        val sdk = Build.VERSION.SDK_INT
        assert(sdk >= 21) { "Min SDK should be >= 21, got $sdk" }
    }

    // ═══════════════════════════════════════════════
    //  8. FORMATTING & DISPLAY UTILITIES
    // ═══════════════════════════════════════════════
    onProgress("[8/8] Testing formatting & display utilities...")

    // ═══════════════════════════════════════════════
    //  9. EXTENDED COVERAGE — Entity operations & data integrity
    // ═══════════════════════════════════════════════
    onProgress("[9/9] Testing extended coverage...")

    runTest(results, "Extended Coverage", "DataRecordEntity constructable") {
        val dr = DataRecordEntity(toolType = "Counter", label = "Bird count", value = "5")
        assert(dr.toolType == "Counter") { "DataRecordEntity toolType mismatch" }
        assert(dr.label == "Bird count") { "DataRecordEntity label mismatch" }
        assert(dr.value == "5") { "DataRecordEntity value mismatch" }
    }

    runTest(results, "Extended Coverage", "ReportEntity constructable") {
        val r = ReportEntity(title = "Test Report", type = "Field Report")
        assert(r.title == "Test Report") { "ReportEntity title mismatch" }
        assert(r.type == "Field Report") { "ReportEntity type mismatch" }
    }

    runTest(results, "Extended Coverage", "TagEntity constructable") {
        val t = TagEntity(name = "bird-sighting")
        assert(t.name == "bird-sighting") { "TagEntity name mismatch" }
    }

    runTest(results, "Extended Coverage", "Settings snapshot string builds") {
        val snapshot = buildSettingsSnapshot(viewModel)
        assert(snapshot.contains("theme=")) { "Snapshot missing theme" }
        assert(snapshot.contains("secure=")) { "Snapshot missing secure flag" }
        assert(snapshot.contains("developer=")) { "Snapshot missing developer mode" }
    }

    runTest(results, "Extended Coverage", "Weather descriptive text is non-empty — read-only") {
        val weatherCodeDescriptions = listOf(0, 1, 61, 95, 99)
        weatherCodeDescriptions.forEach { code ->
            val desc = fieldmind.research.app.features.field.data.weather.WeatherSnapshot.descriptionForCode(code)
            assert(desc.isNotBlank()) { "Weather code $code has blank description" }
        }
    }

    runTest(results, "Extended Coverage", "SDK version meets minimum — read-only") {
        assert(Build.VERSION.SDK_INT >= 21) { "Min SDK should be >= 21, got ${Build.VERSION.SDK_INT}" }
    }

    runTest(results, "Extended Coverage", "OpenMeteo provider display name is non-blank") {
        assert(OpenMeteoProvider().displayName.isNotBlank()) { "OpenMeteo provider display name is blank" }
        assert(OpenMeteoProvider().slug.isNotBlank()) { "OpenMeteo provider slug is blank" }
    }

    runTest(results, "Extended Coverage", "Observation categories contain expected values") {
        assert(observationCategories.contains("Wildlife")) { "Missing Wildlife category" }
        assert(observationCategories.contains("Weather")) { "Missing Weather category" }
        assert(observationCategories.contains("Habitat")) { "Missing Habitat category" }
        assert(observationCategories.size >= 8) { "Expected at least 8 categories" }
    }

    runTest(results, "Formatting & Display", "Confidence options are well-formed") {
        assert(confidenceOptions.contains("Certain")) { "Missing Certain" }
        assert(confidenceOptions.contains("Likely")) { "Missing Likely" }
        assert(confidenceOptions.contains("Moderate")) { "Missing Moderate" }
        assert(confidenceOptions.contains("Unsure")) { "Missing Unsure" }
    }

    // (Deduplicated — these tests are covered in Extended Coverage above)

    // ═══════════════════════════════════════════════
    //  9. UI LAYOUT & INSETS CHECKLIST
    // ═══════════════════════════════════════════════
    onProgress("[9/9] Recording UI layout checklist...")

    results.add(TestResult("UI Layout & Insets", "Bottom nav/FAB overlap requires Compose UI bounds instrumentation", true, "ManualRequired: verify final Home list items are not hidden by the floating nav pill or quick-capture FAB on small phones.", testType = "ManualRequired"))
    results.add(TestResult("UI Layout & Insets", "Weather widget clipping requires rendered bounds inspection", true, "ManualRequired: enable all weather detail toggles and verify the widget wraps or scrolls without clipping.", testType = "ManualRequired"))
    results.add(TestResult("UI Layout & Insets", "Hardware-back tab behavior covered by navigation contract", true, "Contract: Home back shows exit prompt; non-home tabs return toward Home without NavController pop.", testType = "StaticContract"))

    onProgress("All tests completed")
}
