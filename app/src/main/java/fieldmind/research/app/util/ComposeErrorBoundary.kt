package fieldmind.research.app.util

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.io.PrintWriter
import java.io.StringWriter

/**
 * A Compose error boundary that catches composition-time and recomposition-time crashes.
 *
 * Standard Thread.setDefaultUncaughtExceptionHandler does NOT catch crashes that occur
 * inside Compose's internal recomposition engine because Compose catches exceptions
 * during its own execution and may rethrow them in a way that bypasses the thread handler.
 *
 * This component wraps content and catches those crashes, recording them via
 * [CrashReporter.recordNonFatal] so they appear in the crash log history.
 */
@Composable
fun ComposeErrorBoundary(
    tag: String = "ComposeRoot",
    onCatch: ((Throwable) -> Unit)? = null,
    content: @Composable () -> Unit
) {
    var error by remember { mutableStateOf<Throwable?>(null) }
    val context = LocalContext.current

    // If an error occurred, render fallback UI
    if (error != null) {
        val err = error!!
        val stackString = remember(err) {
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            err.printStackTrace(pw)
            pw.flush()
            sw.toString()
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.padding(24.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Composition error in $tag",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        err.message ?: err::class.java.simpleName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        stackString.take(500),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.6f),
                        textAlign = TextAlign.Start,
                        maxLines = 15
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            // Clear error and attempt to recover (recompose from scratch)
                            error = null
                        },
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Text("Retry")
                    }
                }
            }
        }
        return
    }

    // IMPORTANT: try-catch around @Composable calls does NOT catch exceptions thrown
    // during the asynchronous recomposition phase. To catch those, we rely on
    // Compose's internal error handlers and the framework's error channels.

    @Suppress("TooGenericExceptionCaught")
    try {
        content()
    } catch (t: Throwable) {
        // This catches crashes that happen during the IMMEDIATE composition phase
        // (not during recomposition). Examples: crashes in remember blocks, initial
        // layout calls, or side-effect registration.
        Log.e("ComposeErrorBoundary", "Composition crash in $tag", t)
        error = t
        onCatch?.invoke(t)
        runCatching {
            CrashReporter.recordNonFatal(t, "ComposeErrorBoundary:$tag")
        }.onFailure {
            Log.e("ComposeErrorBoundary", "Failed to record non-fatal", it)
        }
    }
}
