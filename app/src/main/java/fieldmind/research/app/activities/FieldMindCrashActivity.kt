package fieldmind.research.app.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.system.exitProcess

class FieldMindCrashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val crashLog = intent.getStringExtra(EXTRA_CRASH_LOG) ?: "No crash log available."

        runCatching {
            setContent {
                MaterialTheme {
                    CrashScreen(crashLog = crashLog)
                }
            }
        }.onFailure {
            Log.e(TAG, "Compose crash screen failed; showing native fallback", it)
            showNativeFallback(crashLog)
        }
    }

    @Composable
    private fun CrashScreen(crashLog: String) {
        val context = LocalContext.current
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "FieldMind crashed",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    "A crash report was captured. Share it with the developer or restart FieldMind.",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = crashLog,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Crash report") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 220.dp, max = 420.dp),
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { copyCrashLog(context, crashLog) },
                        modifier = Modifier.weight(1f)
                    ) { Text("Copy") }
                    Button(
                        onClick = { shareCrashLog(context, crashLog) },
                        modifier = Modifier.weight(1f)
                    ) { Text("Share") }
                }
                Button(
                    onClick = { restartApp(context) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Restart FieldMind") }
            }
        }
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
            text = "Restart FieldMind"
            setOnClickListener { restartApp(this@FieldMindCrashActivity) }
        })
        setContentView(ScrollView(this).apply { addView(content) })
    }

    companion object {
        private const val TAG = "FieldMindCrashActivity"
        const val EXTRA_CRASH_LOG = "extra_crash_log"

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
                context.startActivity(Intent.createChooser(shareIntent, "Share crash report").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }.onFailure {
                copyCrashLog(context, crashLog)
            }
        }

        private fun restartApp(context: Context) {
            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            if (intent != null) context.startActivity(intent)
            exitProcess(0)
        }
    }
}
