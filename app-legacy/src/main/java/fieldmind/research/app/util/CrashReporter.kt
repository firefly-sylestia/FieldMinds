package fieldmind.research.app.util

import android.app.Application
import android.os.Build
import android.os.Process
import android.util.Log
import fieldmind.research.app.BuildConfig
import fieldmind.research.app.activities.FieldMindCrashActivity
import fieldmind.research.app.shared.data.model.AppSettings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

object CrashReporter {

    private const val TAG = "CrashReporter"
    private val handlingCrash = AtomicBoolean(false)
    private var appSettings: AppSettings? = null
    private var previousHandler: Thread.UncaughtExceptionHandler? = null

    /**
     * Whether the crash reporter has been fully initialized.
     * Used by startup crash detection to distinguish "before init" crashes.
     */
    @Volatile
    var isInitialized: Boolean = false
        private set

    /**
     * Whether the crash activity was successfully launched.
     * Reset to false on each init() call.
     */
    @Volatile
    var lastCrashActivityLaunched: Boolean = false
        private set

    /**
     * Timestamp of the last detected crash (System.currentTimeMillis()).
     * 0 if no crash has been detected since init().
     */
    @Volatile
    var lastCrashTimestampMs: Long = 0L
        private set

    fun init(application: Application) {
        appSettings = runCatching { AppSettings.getInstance(application) }.getOrNull()
        previousHandler = Thread.getDefaultUncaughtExceptionHandler()

        // ── Detect startup crash from previous run ──
        // If the crash activity failed to launch last time (the app was killed before
        // the crash screen could render), we mark the flag here so the app can show
        // a recovery banner on first launch. The flag is cleared once the crash activity
        // successfully launches.
        val previousCrashLog = appSettings?.lastCrashLog?.value
        if (previousCrashLog != null && !previousCrashLog.contains("Startup crash check")) {
            Log.w(TAG, "Previous run ended with an unviewed crash log — crash activity may have failed")
        }

        // Capture the current thread's stack to help diagnose startup hangs
        val initThreadStack = Thread.currentThread().stackTrace.take(30).joinToString("\n") { "    at $it" }
        Log.d(TAG, "CrashReporter.init() called from:\n$initThreadStack")

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            if (isRuntimeShutdownThrowable(throwable)) {
                Log.w(TAG, "Ignoring uncaught exception during runtime shutdown on thread ${thread.name}")
                previousHandler?.uncaughtException(thread, throwable)
                return@setDefaultUncaughtExceptionHandler
            }

            if (!handlingCrash.compareAndSet(false, true)) {
                Log.e(TAG, "Recursive crash while handling previous crash", throwable)
                // Still try to persist the recursive crash (different thread maybe)
                runCatching {
                    val recursiveLog = buildCrashLog(thread, throwable).let { log ->
                        "[RECURSIVE CRASH - previous handler may fail]\n$log"
                    }
                    appSettings?.addCrashLogEntry(recursiveLog)
                }
                previousHandler?.uncaughtException(thread, throwable) ?: Process.killProcess(Process.myPid())
                return@setDefaultUncaughtExceptionHandler
            }

            lastCrashTimestampMs = System.currentTimeMillis()

            val crashLog = buildCrashLog(thread, throwable)
            Log.e(TAG, "Uncaught exception on thread ${thread.name}: ${throwable::class.java.name}: ${throwable.message}")

            // ── Persist crash log IMMEDIATELY before any fallible launch ──
            // This ensures the log is saved even if the crash activity fails.
            runCatching {
                appSettings?.setLastCrashLog(crashLog)
                appSettings?.addCrashLogEntry(crashLog)
            }.onFailure { e ->
                Log.e(TAG, "Failed to persist crash log", e)
                // Fallback: write to a separate key in case AppSettings itself is corrupted
                runCatching {
                    val prefs = application.getSharedPreferences("fieldmind_crash_fallback", 0)
                    prefs.edit().putString("last_fallback_crash", crashLog).commit()
                }
            }

            // ── Launch crash activity ──
            lastCrashActivityLaunched = runCatching {
                FieldMindCrashActivity.start(application.applicationContext, crashLog)
            }.onFailure { e ->
                Log.e(TAG, "Failed to launch crash activity", e)
            }.isSuccess

            // ── If crash activity failed, fall through to previous handler ──
            if (!lastCrashActivityLaunched) {
                Log.e(TAG, "Crash activity launch FAILED — falling through to default handler")
                // Write an explicit marker so startup crash detection can confirm
                runCatching {
                    appSettings?.addCrashLogEntry(
                        "[CRASH ACTIVITY LAUNCH FAILED — app will restart without user-visible crash screen]\n" +
                                "Crash: ${throwable::class.java.name}: ${throwable.message}"
                    )
                }
                previousHandler?.uncaughtException(thread, throwable) ?: run {
                    // No previous handler — kill process directly
                }
            }

            // The crash UI runs in :crash_process. Stop the corrupted app process after the
            // launch request has been handed to ActivityManager so the user is not left on a
            // half-rendered/blank task.
            // Small delay to let the crash activity start before killing this process.
            // 500ms gives ActivityManager enough time to receive the startActivity intent
            // and begin creating the crash process, even on slow/loaded devices.
            try { Thread.sleep(500) } catch (_: InterruptedException) {}
            Process.killProcess(Process.myPid())
            kotlin.system.exitProcess(10)
        }

        isInitialized = true
        Log.d(TAG, "CrashReporter initialized. Previous handler: ${previousHandler?.javaClass?.name ?: "none"}")
    }

    fun buildCrashLog(thread: Thread, throwable: Throwable): String = buildString {
        appendLine("FieldMind Crash Report")
        appendLine("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z", Locale.US).format(Date())}")
        appendLine("Thread: ${thread.name} (id=${thread.id}, priority=${thread.priority})")
        appendLine("Thread group: ${thread.threadGroup?.name ?: "none"}")
        appendLine("Exception: ${throwable::class.java.name}")
        appendLine("Message: ${throwable.message ?: "—"}")
        appendLine("App: ${BuildConfig.APPLICATION_ID} ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        appendLine("Build: ${BuildConfig.BUILD_TYPE}")
        appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
        appendLine("Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
        appendLine("isInitialized: $isInitialized")
        appendLine("lastCrashActivityLaunched: $lastCrashActivityLaunched")
        appendLine()
        appendLine(Log.getStackTraceString(throwable))
        appendLine()
        appendLine("--- All thread stack traces ---")
        try {
            val allThreads = Thread.getAllStackTraces()
            appendLine("Active threads: ${allThreads.size}")
            allThreads.forEach { (thread, stack) ->
                appendLine()
                appendLine("Thread: '${thread.name}' (${thread.state}) priority=${thread.priority} daemon=${thread.isDaemon}")
                stack.take(20).forEach { element ->
                    appendLine("    at $element")
                }
                if (stack.size > 20) appendLine("    ... (${stack.size - 20} more)")
            }
        } catch (_: Exception) {
            appendLine("(failed to dump threads)")
        }
        appendLine("--- End thread traces ---")
    }

    private fun isRuntimeShutdownThrowable(throwable: Throwable?): Boolean {
        var cause = throwable
        while (cause != null) {
            val message = cause.message
            if (message != null && (
                    message.contains("Thread starting during runtime shutdown") ||
                        message.contains("Runtime shutdown in progress") ||
                        message.contains("shut down")
                    )
            ) {
                return true
            }
            cause = cause.cause
        }
        return false
    }

    fun testCrash() {
        throw RuntimeException("This is a test crash generated by the app for demonstration purposes.")
    }

    fun recordNonFatal(throwable: Throwable, source: String = "unknown") {
        val crashLog = buildString {
            appendLine("Non-fatal error captured from: $source")
            appendLine("Exception: ${throwable::class.java.name}: ${throwable.message ?: "—"}")
            appendLine("Timestamp: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z", Locale.US).format(Date())}")
            appendLine()
            append(Log.getStackTraceString(throwable))
        }
        Log.e(TAG, crashLog)
        runCatching {
            appSettings?.addCrashLogEntry(crashLog)
        }.onFailure { e ->
            Log.e(TAG, "Failed to record non-fatal", e)
        }
    }

    /**
     * Records an ANR (Application Not Responding) event as a crash log entry.
     * ANRs don't throw exceptions, so they bypass the uncaught exception handler.
     * This method explicitly captures the ANR context and persists it.
     *
     * @param blockedTimeMs How long the UI thread was blocked
     * @param mainThreadStack The main (UI) thread's stack trace at the time of ANR
     * @param allThreadsDump Optional dump of all thread stack traces
     */
    fun recordAnr(
        blockedTimeMs: Long,
        mainThreadStack: Array<StackTraceElement>,
        allThreadsDump: String? = null
    ) {
        val anrLog = buildString {
            appendLine("ANR DETECTED")
            appendLine("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z", Locale.US).format(Date())}")
            appendLine("UI thread blocked for: ${blockedTimeMs}ms")
            appendLine("App: ${BuildConfig.APPLICATION_ID} ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            appendLine("Build: ${BuildConfig.BUILD_TYPE}")
            appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
            appendLine()
            appendLine("📍 Main thread (UI thread) stack trace:")
            mainThreadStack.forEach { element ->
                appendLine("    at $element")
            }
            appendLine()
            if (allThreadsDump != null) {
                appendLine(allThreadsDump)
            }
        }
        Log.e(TAG, anrLog)
        runCatching {
            appSettings?.addCrashLogEntry(anrLog)
        }.onFailure { e ->
            Log.e(TAG, "Failed to record ANR crash log", e)
        }
    }

    /**
     * Detects whether the previous app session ended with an uncaught crash that was
     * NOT shown to the user (i.e. the crash activity failed to launch).
     *
     * Call this early in [Application.onCreate] to decide whether to show a
     * "previous session crashed" recovery prompt.
     *
     * @return The crash log text from the previous session, or null if no unviewed crash exists.
     */
    fun detectPreviousSessionCrash(application: Application): String? {
        return runCatching {
            val prefs = application.getSharedPreferences("fieldmind_crash_fallback", 0)
            val fallbackCrash = prefs.getString("last_fallback_crash", null)
            if (fallbackCrash != null) {
                // Clear so we don't report it twice
                prefs.edit().remove("last_fallback_crash").apply()
                return@runCatching fallbackCrash
            }

            // Check AppSettings for unviewed crash
            val settings = runCatching { AppSettings.getInstance(application) }.getOrNull()
            val lastLog = settings?.lastCrashLog?.value
            if (lastLog != null && !lastLog.contains("Startup crash check")) {
                return@runCatching lastLog
            }
            null
        }.getOrNull()
    }
}
