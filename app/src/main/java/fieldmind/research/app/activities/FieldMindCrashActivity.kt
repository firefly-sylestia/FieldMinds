package fieldmind.research.app.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button as AndroidButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fieldmind.research.app.shared.presentation.components.icons.Icon
import fieldmind.research.app.shared.presentation.components.icons.MaterialSymbolIcon
import kotlin.system.exitProcess

/**
 * Full-screen crash recovery UI.
 *
 * Renders when [CrashReporter] catches an uncaught exception. The activity is
 * intentionally self-contained:
 *
 *  - It does NOT import [fieldmind.research.app.features.field.data.settings.FieldMindSettings]
 *    so it can still load and run when the settings class is the source of the crash.
 *  - All colors are hardcoded in [SafeColors] (no theme attributes, no dynamic color)
 *    so the screen still renders if the crash was caused by corrupted theme resources.
 *
 * Layout (v0.47.6 redesign, journal aesthetic):
 *  1. **Crash header card** — warm primaryContainer surface, large error icon, title + subtitle.
 *  2. **Crash log card** — monospace read-only text field with Copy / Share row.
 *  3. **Recovery card** — primary "Disable lock & PIN, then restart" button (resets the
 *     most common crash-causing settings: privacy lock, app PIN, decoy PIN, panic lock,
 *     export password protection) and an outlined "Restart FieldMind only" button.
 *  4. **Footer note** — explains the user can re-enable lock in Settings after restart.
 *
 * The recovery flow goes through an [AlertDialog] confirmation so a stray tap on the
 * crash screen does not silently wipe security settings.
 */
class FieldMindCrashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val crashLog = intent.getStringExtra(EXTRA_CRASH_LOG) ?: "No crash log available."

        runCatching {
            setContent {
                CrashTheme {
                    CrashScreen(crashLog = crashLog)
                }
            }
        }.onFailure {
            Log.e(TAG, "Compose crash screen failed; showing native fallback", it)
            showNativeFallback(crashLog)
        }
    }

    /**
     * Self-contained Material 3 theme. Uses hardcoded [SafeColors] so it never resolves
     * theme attributes, dynamic color, or app-specific styling. If the crash was caused
     * by corrupted theme resources, this theme still renders correctly.
     */
    @Composable
    private fun CrashTheme(content: @Composable () -> Unit) {
        MaterialTheme(
            colorScheme = lightColorScheme(
                primary = SafeColors.primary,
                onPrimary = SafeColors.onPrimary,
                primaryContainer = SafeColors.primaryContainer,
                secondary = SafeColors.secondary,
                onSecondary = SafeColors.onPrimary,
                tertiary = SafeColors.tertiary,
                onTertiary = SafeColors.onPrimary,
                error = SafeColors.error,
                onError = SafeColors.onError,
                errorContainer = SafeColors.errorContainer,
                onErrorContainer = SafeColors.error,
                surface = SafeColors.surface,
                onSurface = SafeColors.onSurface,
                onSurfaceVariant = SafeColors.onSurfaceVariant,
                outline = SafeColors.outline,
                surfaceVariant = SafeColors.surfaceVariant
            ),
            typography = MaterialTheme.typography, // System fonts only — no custom types
            content = content
        )
    }

    /**
     * Hardcoded "journal" palette so the crash theme never resolves theme attributes.
     * The warm cream + forest-green + terracotta palette echoes the FieldMind brand
     * without depending on theme resources, dynamic colors, or font assets.
     */
    private object SafeColors {
        val primary = Color(0xFF2E7D32)              // Forest green (FieldMind brand)
        val onPrimary = Color.White
        val primaryContainer = Color(0xFFD7E8D8)     // Soft sage
        val secondary = Color(0xFFE65100)            // Warm terracotta accent
        val tertiary = Color(0xFFB8651A)             // Warm amber
        val error = Color(0xFFC62828)                // Warm red
        val onError = Color.White
        val errorContainer = Color(0xFFFADAD8)       // Soft pink
        val surface = Color(0xFFFAF7F2)              // Warm cream paper
        val onSurface = Color(0xFF1B1B1B)            // Near-black
        val onSurfaceVariant = Color(0xFF5A5A5A)     // Soft gray
        val outline = Color(0xFF8B8680)              // Warm gray
        val surfaceVariant = Color(0xFFEEE8DE)       // Soft beige
    }

    @Composable
    private fun CrashScreen(crashLog: String) {
        val context = LocalContext.current
        var showDisableConfirm by remember { mutableStateOf(false) }
        Surface(Modifier.fillMaxSize(), color = SafeColors.surface) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SafeColors.surface)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                CrashHeader()
                CrashLogCard(
                    crashLog = crashLog,
                    onCopy = { copyCrashLog(context, crashLog) },
                    onShare = { shareCrashLog(context, crashLog) }
                )
                RecoveryCard(
                    onDisableAndRestart = { showDisableConfirm = true },
                    onRestart = { restartApp(context) }
                )
                CrashFooterNote()
            }
        }

        if (showDisableConfirm) {
            AlertDialog(
                onDismissRequest = { showDisableConfirm = false },
                icon = {
                    Icon(
                        icon = MaterialSymbolIcon("lock_open"),
                        contentDescription = null,
                        tint = SafeColors.primary,
                        size = 28.dp
                    )
                },
                title = { Text("Disable lock & PIN?") },
                text = {
                    Text(
                        "Your research data and observations are safe. Only the privacy lock, app PIN, " +
                            "decoy PIN, panic-lock setting, and export password will be turned off. " +
                            "You can re-enable them in Settings after the restart."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDisableConfirm = false
                            disableSecurityAndRestart(context)
                        },
                        shape = RoundedCornerShape(22.dp)
                    ) { Text("Disable & restart") }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDisableConfirm = false },
                        shape = RoundedCornerShape(22.dp)
                    ) { Text("Cancel") }
                },
                shape = RoundedCornerShape(28.dp),
                containerColor = SafeColors.surface
            )
        }
    }

    @Composable
    private fun CrashHeader() {
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = SafeColors.primaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(SafeColors.error.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon = MaterialSymbolIcon("error_outline"),
                        contentDescription = null,
                        tint = SafeColors.error,
                        size = 40.dp
                    )
                }
                Text(
                    "FieldMind crashed",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = SafeColors.onSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Something unexpected went wrong. The crash report below has been saved " +
                        "and can be shared with the developer to help fix the issue.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SafeColors.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    @Composable
    private fun CrashLogCard(
        crashLog: String,
        onCopy: () -> Unit,
        onShare: () -> Unit
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        icon = MaterialSymbolIcon("description"),
                        contentDescription = null,
                        tint = SafeColors.onSurfaceVariant,
                        size = 18.dp
                    )
                    Text(
                        "Crash report",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = SafeColors.onSurface
                    )
                }
                OutlinedTextField(
                    value = crashLog,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp, max = 380.dp),
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SafeColors.outline,
                        unfocusedBorderColor = SafeColors.outline,
                        focusedLabelColor = SafeColors.onSurfaceVariant,
                        unfocusedLabelColor = SafeColors.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCopy,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(22.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Icon(MaterialSymbolIcon("content_copy"), null, size = 16.dp)
                        Spacer(Modifier.size(6.dp))
                        Text("Copy")
                    }
                    Button(
                        onClick = onShare,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(22.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Icon(MaterialSymbolIcon("share"), null, size = 16.dp)
                        Spacer(Modifier.size(6.dp))
                        Text("Share")
                    }
                }
            }
        }
    }

    @Composable
    private fun RecoveryCard(
        onDisableAndRestart: () -> Unit,
        onRestart: () -> Unit
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        icon = MaterialSymbolIcon("healing"),
                        contentDescription = null,
                        tint = SafeColors.primary,
                        size = 18.dp
                    )
                    Text(
                        "Recovery options",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = SafeColors.onSurface
                    )
                }
                Text(
                    "If FieldMind keeps crashing on launch, the most common cause is a corrupted " +
                        "lock or PIN setting. You can disable those settings and restart to " +
                        "recover your data without losing any observations.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SafeColors.onSurfaceVariant
                )
                Button(
                    onClick = onDisableAndRestart,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Icon(MaterialSymbolIcon("lock_open"), null, size = 18.dp)
                    Spacer(Modifier.size(8.dp))
                    Text("Disable lock & PIN, then restart")
                }
                OutlinedButton(
                    onClick = onRestart,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Icon(MaterialSymbolIcon("restart_alt"), null, size = 18.dp)
                    Spacer(Modifier.size(8.dp))
                    Text("Restart FieldMind only")
                }
            }
        }
    }

    @Composable
    private fun CrashFooterNote() {
        Text(
            "Your research data is safe — only the lock and PIN settings will be disabled. " +
                "You can re-enable them in Settings after the app restarts.",
            style = MaterialTheme.typography.bodySmall,
            color = SafeColors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }

    private fun showNativeFallback(crashLog: String) {
        val padding = (20 * resources.displayMetrics.density).toInt()
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(padding, padding, padding, padding)
        }
        content.addView(TextView(this).apply {
            text = "FieldMind crashed"
            textSize = 24f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        })
        content.addView(TextView(this).apply {
            text = "A crash report was captured."
            textSize = 16f
        })
        content.addView(TextView(this).apply {
            text = crashLog
            textSize = 12f
            typeface = android.graphics.Typeface.MONOSPACE
            setTextIsSelectable(true)
        })
        content.addView(AndroidButton(this).apply {
            text = "Copy crash report"
            setOnClickListener { copyCrashLog(this@FieldMindCrashActivity, crashLog) }
        })
        content.addView(AndroidButton(this).apply {
            text = "Share crash report"
            setOnClickListener { shareCrashLog(this@FieldMindCrashActivity, crashLog) }
        })
        content.addView(AndroidButton(this).apply {
            text = "Disable lock & PIN, then restart"
            setOnClickListener { disableSecurityAndRestart(this@FieldMindCrashActivity) }
        })
        content.addView(AndroidButton(this).apply {
            text = "Restart FieldMind"
            setOnClickListener { restartApp(this@FieldMindCrashActivity) }
        })
        setContentView(ScrollView(this).apply { addView(content) })
    }

    companion object {
        private const val TAG = "FieldMindCrashActivity"
        const val EXTRA_CRASH_LOG = "extra_crash_log"

        // Keys must match the constants in FieldMindSettings.kt. The crash activity
        // is intentionally self-contained and does NOT import FieldMindSettings so
        // it can still run when the settings class itself is the source of the crash.
        private const val KEY_PRIVACY_LOCK = "privacy_lock"
        private const val KEY_APP_PIN_ENABLED = "app_pin_enabled"
        private const val KEY_APP_PIN_HASH = "app_pin_hash"
        private const val KEY_DECOY_PIN_ENABLED = "decoy_pin_enabled"
        private const val KEY_DECOY_PIN_HASH = "decoy_pin_hash"
        private const val KEY_DECOY_PIN_LABEL = "decoy_pin_label"
        private const val KEY_FAILED_UNLOCK_PANIC_LOCK = "failed_unlock_panic_lock"
        private const val KEY_FAILED_UNLOCK_BIOMETRICS = "failed_unlock_biometrics"
        private const val KEY_EXPORT_PASSWORD_PROTECTION = "export_password_protection"
        private const val KEY_EXPORT_PASSWORD_HASH = "export_password_hash"
        private const val PREFS_NAME = "fieldmind_settings"

        fun start(context: Context, crashLog: String) {
            val intent = Intent(context, FieldMindCrashActivity::class.java).apply {
                putExtra(EXTRA_CRASH_LOG, crashLog)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            context.startActivity(intent)
        }

        private fun copyCrashLog(context: Context, crashLog: String) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("FieldMind Crash Report", crashLog))
            Toast.makeText(context, "Crash report copied", Toast.LENGTH_SHORT).show()
        }

        private fun shareCrashLog(context: Context, crashLog: String) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "FieldMind Crash Report")
                putExtra(Intent.EXTRA_TEXT, crashLog)
            }
            runCatching {
                context.startActivity(
                    Intent.createChooser(shareIntent, "Share crash report")
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }.onFailure {
                copyCrashLog(context, crashLog)
            }
        }

        /**
         * Disable every security/privacy setting that can cause the app to crash on
         * launch (corrupt PIN hash, BiometricPrompt failure on the lock screen, panic
         * lock wiping data, export password loop, etc.) and then restart FieldMind.
         *
         * This is the user-visible recovery action. The actual writes go directly to
         * SharedPreferences (NOT through FieldMindSettings) so the activity does not
         * pull in the very class that might be broken.
         */
        private fun disableSecurityAndRestart(context: Context) {
            runCatching {
                val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                // commit() not apply() — the process is about to die via exitProcess(0)
                // in restartApp(), and apply()'s async write may not flush in time.
                prefs.edit()
                    .putBoolean(KEY_PRIVACY_LOCK, false)
                    .putBoolean(KEY_APP_PIN_ENABLED, false)
                    .putBoolean(KEY_DECOY_PIN_ENABLED, false)
                    .putBoolean(KEY_FAILED_UNLOCK_PANIC_LOCK, false)
                    .putBoolean(KEY_FAILED_UNLOCK_BIOMETRICS, false)
                    .putBoolean(KEY_EXPORT_PASSWORD_PROTECTION, false)
                    .putString(KEY_APP_PIN_HASH, "")
                    .putString(KEY_DECOY_PIN_HASH, "")
                    .putString(KEY_DECOY_PIN_LABEL, "")
                    .putString(KEY_EXPORT_PASSWORD_HASH, "")
                    .commit()
            }.onFailure {
                Log.e(TAG, "Failed to disable security settings before restart", it)
            }
            Toast.makeText(context, "Lock & PIN disabled. Restarting…", Toast.LENGTH_SHORT).show()
            restartApp(context)
        }

        private fun restartApp(context: Context) {
            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
                )
            }
            if (intent != null) context.startActivity(intent)
            exitProcess(0)
        }
    }
}
