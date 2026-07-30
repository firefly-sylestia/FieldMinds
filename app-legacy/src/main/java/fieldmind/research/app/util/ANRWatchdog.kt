package fieldmind.research.app.util

import android.os.Handler
import android.os.Looper
import android.util.Log

/**
 * ANRWatchdog monitors the UI thread for Application Not Responding (ANR) situations.
 * It posts a task to the main thread and checks if it executes within the timeout period.
 * If the task doesn't execute in time, it:
 *   1. Logs the ANR with full thread stack traces to logcat
 *   2. Records the ANR as a crash log entry via [CrashReporter.recordAnr]
 *      (so it appears in the crash log history and can be shared in bug reports)
 *   3. Captures all thread stack traces for diagnosis
 *
 * The watchdog is now active in ALL builds (not just DEBUG) to ensure no ANR
 * goes undetected. The timeout is conservative (5s) to avoid false positives.
 */
class ANRWatchdog(private val timeoutMs: Long = 5000) : Thread("ANRWatchdog") {

    companion object {
        private const val TAG = "ANRWatchdog"
        private const val STARTUP_GRACE_PERIOD_MS = 15000L // 15 seconds grace period after start
    }

    @Volatile
    private var shouldContinue = true
    private val uiHandler = Handler(Looper.getMainLooper())
    private val startTime = System.currentTimeMillis()

    // Track consecutive ANRs to avoid spam
    @Volatile
    private var consecutiveAnrs = 0
    private var lastAnrReportedAt = 0L
    private val minIntervalBetweenAnrReports = 30_000L // 30 seconds minimum between ANR reports

    init {
        isDaemon = true // Make this a daemon thread so it doesn't prevent app shutdown
    }

    override fun run() {
        Log.d(TAG, "ANR Watchdog started with timeout ${timeoutMs}ms")

        while (shouldContinue) {
            try {
                // Skip monitoring during startup grace period to avoid false positives
                val timeSinceStart = System.currentTimeMillis() - startTime
                if (timeSinceStart < STARTUP_GRACE_PERIOD_MS) {
                    sleep(1000)
                    continue
                }

                val start = System.currentTimeMillis()
                var responded = false

                // Post a task to the UI thread
                uiHandler.post {
                    responded = true
                }

                // Wait for the timeout period
                sleep(timeoutMs)

                // Check if the UI thread responded
                if (!responded && shouldContinue) {
                    val blockedTime = System.currentTimeMillis() - start
                    val now = System.currentTimeMillis()
                    consecutiveAnrs++

                    // ── Throttle ANR reports to avoid spam ──
                    if (now - lastAnrReportedAt < minIntervalBetweenAnrReports && consecutiveAnrs > 1) {
                        Log.w(TAG, "ANR suppressed (throttled): consecutive #$consecutiveAnrs")
                        sleep(1000)
                        continue
                    }
                    lastAnrReportedAt = now
                    val isConsecutive = consecutiveAnrs > 1

                    Log.e(TAG, "")
                    Log.e(TAG, "╔═══════════════════════════════════════════════════════════╗")
                    Log.e(TAG, "║                    ANR DETECTED!                          ║")
                    Log.e(TAG, "║  UI thread blocked for ${blockedTime}ms (threshold: ${timeoutMs}ms)  ║")
                    if (isConsecutive) {
                        Log.e(TAG, "║  Consecutive ANR #$consecutiveAnrs                         ║")
                    }
                    Log.e(TAG, "╚═══════════════════════════════════════════════════════════╝")
                    Log.e(TAG, "")

                    // Get the main thread's stack trace
                    val mainThread = Looper.getMainLooper().thread
                    val mainStack = mainThread.stackTrace

                    Log.e(TAG, "📍 Main thread (UI thread) stack trace:")
                    Log.e(TAG, "───────────────────────────────────────────────────────────")
                    mainStack.forEach { element ->
                        Log.e(TAG, "    at $element")
                    }
                    Log.e(TAG, "───────────────────────────────────────────────────────────")
                    Log.e(TAG, "")

                    // Capture all thread dump for CrashReporter
                    val allThreadsDump = captureAllThreadsDump()

                    // ── Persist the ANR as a crash log entry ──
                    // This is critical: ANRs don't throw exceptions so they bypass
                    // the uncaught exception handler. Recording them explicitly ensures
                    // they appear in the crash history and can be attached to bug reports.
                    runCatching {
                        CrashReporter.recordAnr(
                            blockedTimeMs = blockedTime,
                            mainThreadStack = mainStack,
                            allThreadsDump = allThreadsDump
                        )
                    }.onFailure { e ->
                        Log.e(TAG, "Failed to record ANR via CrashReporter", e)
                    }
                } else {
                    // Reset consecutive counter on successful response
                    consecutiveAnrs = 0
                }

                // Small sleep before next check to avoid excessive CPU usage
                sleep(1000)

            } catch (e: InterruptedException) {
                Log.d(TAG, "ANR Watchdog interrupted")
                break
            } catch (e: Exception) {
                Log.e(TAG, "Error in ANR Watchdog", e)
            }
        }

        Log.d(TAG, "ANR Watchdog stopped")
    }

    /**
     * Captures a formatted dump of all thread stack traces for diagnosis.
     */
    private fun captureAllThreadsDump(): String {
        return try {
            val allThreads = Thread.getAllStackTraces()
            buildString {
                appendLine("All threads (${allThreads.size} total):")
                allThreads.forEach { (thread, stackTrace) ->
                    appendLine()
                    appendLine("Thread: '${thread.name}' (${thread.state}) priority=${thread.priority} daemon=${thread.isDaemon}")
                    stackTrace.take(15).forEach { element ->
                        appendLine("    at $element")
                    }
                    if (stackTrace.size > 15) {
                        appendLine("    ... (${stackTrace.size - 15} more)")
                    }
                }
            }
        } catch (e: Exception) {
            "(failed to dump threads: ${e.message})"
        }
    }

    /**
     * Stops the ANR watchdog
     */
    fun stopWatching() {
        Log.d(TAG, "Stopping ANR Watchdog")
        shouldContinue = false
        interrupt()
    }
}
