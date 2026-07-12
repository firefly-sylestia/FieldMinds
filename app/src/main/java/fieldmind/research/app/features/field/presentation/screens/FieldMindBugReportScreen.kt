package fieldmind.research.app.features.field.presentation.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fieldmind.research.app.BuildConfig
import fieldmind.research.app.features.field.data.settings.FieldMindSettings
import fieldmind.research.app.features.field.presentation.components.FieldMindIcons
import fieldmind.research.app.features.field.presentation.viewmodel.FieldMindViewModel
import fieldmind.research.app.infrastructure.bugreport.BugReportRequest
import fieldmind.research.app.infrastructure.bugreport.BugReportReporter
import fieldmind.research.app.infrastructure.bugreport.BugReportResult
import fieldmind.research.app.infrastructure.bugreport.BugReportSanitizer
import fieldmind.research.app.shared.presentation.components.icons.Icon
import fieldmind.research.app.shared.presentation.components.icons.MaterialSymbolIcon
import kotlinx.coroutines.launch

/**
 * Full-screen form that mirrors `.github/ISSUE_TEMPLATE/bug-report.yml`.
 *
 * The form composes a Markdown blob (matching the YAML fields) and hands it to
 * [BugReportReporter], which tries the Cloudflare Worker first and falls back
 * to opening the GitHub web-URL issue form if the Worker isn't configured or
 * unreachable.
 *
 * @param latestCrashLog most recent [CrashLogEntry.log] from
 *   [AppSettings.crashLogHistory], sanitized via [BugReportSanitizer] before
 *   being previewed / appended. Nullable when the user has no crash history.
 */
@Composable
fun FieldMindBugReportScreen(
    viewModel: FieldMindViewModel,
    latestCrashLog: String?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val reporter = remember { BugReportReporter() }

    // ── Form state — rememberSaveable so rotation & process-recreation
    // preserve what the user already typed in.──────────────────────────────
    var title by rememberSaveable { mutableStateOf("[BUG]: ") }
    var description by rememberSaveable { mutableStateOf("") }
    var repro by rememberSaveable { mutableStateOf("") }
    var expectedActual by rememberSaveable { mutableStateOf("") }
    var extraContext by rememberSaveable { mutableStateOf("") }
    // Compose-collected StateFlow (consistent with the journal overlay refactor — avoid `.value` reads off StateFlows).
    val includeCrashLog by viewModel.fieldSettings.bugReportsAttachCrashLog.collectAsState()
    var submitting by remember { mutableStateOf(false) }
    var resultText by remember { mutableStateOf<String?>(null) }
    var resultIsError by remember { mutableStateOf(false) }

    val canSend = !submitting &&
        description.isNotBlank() &&
        title.removePrefix("[BUG]:").trim().length >= 3

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Custom header bar (back + title).───────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CuteCardDefaults.ChipShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    onClick = onBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            icon = MaterialSymbolIcon("arrow_back"),
                            contentDescription = "Back",
                            size = 22.dp,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    icon = MaterialSymbolIcon("bug_report"),
                    contentDescription = null,
                    size = 22.dp,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Report a bug",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // ── Notice panel explaining what gets sent.───────────────────
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f),
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "We sanitize before sending",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = "Token keys, file paths, email addresses, IP addresses, and Slack tokens are stripped on both client and server. App version, Android version, and device are added automatically. Edit or remove anything you don't want to share.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                placeholder = { Text("[BUG]: <one-line summary>") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Bug description *") },
                placeholder = { Text("A clear and concise description of what went wrong.") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 96.dp)
            )
            OutlinedTextField(
                value = repro,
                onValueChange = { repro = it },
                label = { Text("Steps to reproduce (optional)") },
                placeholder = { Text("1. ...\n2. ...\n3. ...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 72.dp)
            )
            OutlinedTextField(
                value = expectedActual,
                onValueChange = { expectedActual = it },
                label = { Text("Expected vs actual behavior (optional)") },
                placeholder = { Text("Expected: ...\nActual: ...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 72.dp)
            )
            OutlinedTextField(
                value = extraContext,
                onValueChange = { extraContext = it },
                label = { Text("Additional context (optional)") },
                placeholder = { Text("When did this start? Does it happen consistently?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 72.dp)
            )

            // ── Auto-attach crash log toggle.──────────────────────────────
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            icon = MaterialSymbolIcon("description"),
                            contentDescription = null,
                            size = 20.dp,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Auto-attach latest crash log",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = includeCrashLog,
                            onCheckedChange = { value ->
                                // Persist via the underlying StateFlow; the Compose-collected
                                // `includeCrashLog` will reflect the change on the next tick.
                                viewModel.fieldSettings.setBugReportsAttachCrashLog(value)
                            }
                        )
                    }
                    Text(
                        text = "We'll only attach the most recent entry from your local crash log. The text will be sanitized client-side before preview.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (includeCrashLog) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                        if (latestCrashLog.isNullOrBlank()) {
                            Text(
                                text = "No crash log on file — nothing will be attached.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "Preview (sanitized, truncated to 2000 chars):",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = BugReportSanitizer
                                        .sanitizeForPreview(latestCrashLog)
                                        .take(2000),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ── Auto-filled metadata strip.───────────────────────────────
            Surface(
                shape = CuteCardDefaults.ChipShape,
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    MetaLine("App version", "${BuildConfig.VERSION_NAME} (code ${BuildConfig.VERSION_CODE})")
                    MetaLine("Android", "Android ${Build.VERSION.RELEASE} · SDK ${Build.VERSION.SDK_INT}")
                    MetaLine("Device", "${Build.MANUFACTURER} ${Build.MODEL}")
                    MetaLine("Install method", installMethodLabel())
                }
            }

            // ── Send button.────────────────────────────────────────────────
            Button(
                onClick = {
                    if (!canSend) return@Button
                    submitting = true
                    resultText = null
                    resultIsError = false
                    scope.launch {
                        val body = buildReportBody(
                            description = description.trim(),
                            repro = repro.trim(),
                            expectedActual = expectedActual.trim(),
                            extraContext = extraContext.trim(),
                            includeCrashLog = includeCrashLog,
                            sanitizedCrashLog = latestCrashLog?.let(BugReportSanitizer::sanitizeForPreview)
                        )
                        val req = BugReportRequest(title = title.trim(), body = body)
                        val settled = runCatching { reporter.send(req) }.getOrElse { BugReportResult.HardFail(it.message ?: it::class.java.simpleName) }
                        when (settled) {
                            is BugReportResult.Success -> {
                                resultText = "Filed as issue #${settled.issueNumber}."
                                runCatching { openUrl(context, settled.issueUrl) }
                            }
                            is BugReportResult.SoftFail -> {
                                resultText = "Server rejected the request: ${settled.message}"
                                resultIsError = true
                            }
                            is BugReportResult.WebUrl -> {
                                runCatching { openUrl(context, settled.url) }
                                resultText = "Opened your browser to finish the report — sign in to GitHub there."
                            }
                            is BugReportResult.HardFail -> {
                                resultText = "Couldn't reach server: ${settled.reason}"
                                resultIsError = true
                            }
                        }
                        submitting = false
                    }
                },
                enabled = canSend,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (submitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (submitting) "Sending…" else "Send report")
                } else {
                    Text(if (canSend) "Send report" else "Describe the bug to enable")
                }
            }

            // ── Result feedback.──────────────────────────────────────────
            resultText?.let { msg ->
                Surface(
                    shape = CuteCardDefaults.ChipShape,
                    color = if (resultIsError)
                        MaterialTheme.colorScheme.errorContainer
                    else
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = msg,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (resultIsError)
                            MaterialTheme.colorScheme.onErrorContainer
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp)) // breathing room above bottom nav
        }
    }
}

@Composable
private fun MetaLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(110.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun installMethodLabel(): String = when (BuildConfig.FLAVOR.lowercase()) {
    "github" -> "GitHub Releases (APK sideload)"
    "fdroid" -> "F-Droid"
    else -> BuildConfig.FLAVOR.ifBlank { "Unknown" }
}

private fun openUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

/** Compose a Markdown body that mirrors `.github/ISSUE_TEMPLATE/bug-report.yml`. */
private fun buildReportBody(
    description: String,
    repro: String,
    expectedActual: String,
    extraContext: String,
    includeCrashLog: Boolean,
    sanitizedCrashLog: String?
): String = buildString {
    appendLine("## Bug Description")
    appendLine(description)
    appendLine()
    if (repro.isNotBlank()) {
        appendLine("## Steps to Reproduce")
        appendLine(repro)
        appendLine()
    }
    if (expectedActual.isNotBlank()) {
        appendLine("## Expected vs Actual Behavior")
        appendLine(expectedActual)
        appendLine()
    }
    appendLine("---")
    appendLine("**App Version:** ${BuildConfig.VERSION_NAME} (code ${BuildConfig.VERSION_CODE})")
    appendLine("**Android Version:** Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
    appendLine("**Device:** ${Build.MANUFACTURER} ${Build.MODEL}")
    appendLine("**Installation Method:** ${installMethodLabel()}")
    appendLine()
    if (extraContext.isNotBlank()) {
        appendLine("## Additional Context")
        appendLine(extraContext)
        appendLine()
    }
    if (includeCrashLog && !sanitizedCrashLog.isNullOrBlank()) {
        appendLine("## Logs")
        appendLine("```text")
        appendLine(sanitizedCrashLog.take(2000))
        appendLine("```")
    }
}
