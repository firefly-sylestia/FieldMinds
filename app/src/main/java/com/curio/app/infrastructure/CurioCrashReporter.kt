package com.curio.app.infrastructure

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.util.Log
import com.curio.app.BuildConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Minimal crash reporter for Curio.
 *
 * Captures uncaught exceptions, persists them to SharedPreferences,
 * and can launch a Compose crash-recovery screen. Much simpler than the
 * legacy FieldMind CrashReporter — no ANR watchdog, no separate crash process.
 */
object CurioCrashReporter {

    private const val TAG = "CurioCrashReporter"
    private const val PREFS_NAME = "curio_crash_logs"
    private const val KEY_LAST_CRASH = "last_crash_log"
    private const val KEY_CRASH_HISTORY = "crash_history"

    private val handlingCrash = AtomicBoolean(false)
    private var previousHandler: Thread.UncaughtExceptionHandler? = null
    private var appContext: Context? = null

    @Volatile var lastCrashLog: String? = null
        private set

    fun init(context: Context) {
        appContext = context.applicationContext
        previousHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            if (!handlingCrash.compareAndSet(false, true)) {
                previousHandler?.uncaughtException(thread, throwable)
                return@setDefaultUncaughtExceptionHandler
            }

            val crashLog = buildCrashLog(thread, throwable)
            lastCrashLog = crashLog
            Log.e(TAG, "Uncaught: ${throwable::class.java.name}: ${throwable.message}")

            persistCrash(context.applicationContext, crashLog)

            // Give the launcher a moment to start the crash activity
            try { Thread.sleep(300) } catch (_: InterruptedException) {}
            previousHandler?.uncaughtException(thread, throwable)
                ?: Process.killProcess(Process.myPid())
        }

        Log.d(TAG, "CrashReporter initialized")
    }

    fun testCrash() {
        throw RuntimeException("Test crash from Curio — this is intentional.")
    }

    fun buildCrashLog(thread: Thread, throwable: Throwable): String = buildString {
        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        appendLine("Curio Crash Report")
        appendLine("Time: ${fmt.format(Date())}")
        appendLine("Thread: ${thread.name}")
        appendLine("Exception: ${throwable::class.java.name}")
        appendLine("Message: ${throwable.message ?: "—"}")
        runCatching {
            appendLine("App: ${BuildConfig.APPLICATION_ID} ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        }
        appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
        appendLine("Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
        appendLine()
        appendLine(Log.getStackTraceString(throwable))
    }

    private fun persistCrash(context: Context, log: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LAST_CRASH, log).apply()
        val history = getCrashHistory(context).toMutableList()
        history.add(0, log)
        prefs.edit().putString(KEY_CRASH_HISTORY, history.take(20).joinToString("\n---\n")).apply()
    }

    fun getCrashHistory(context: Context): List<String> {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_CRASH_HISTORY, null) ?: return emptyList()
        return raw.split("\n---\n").filter { it.isNotBlank() }
    }

    fun getLastCrash(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LAST_CRASH, null)
    }

    fun clearCrashHistory(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().clear().apply()
    }
}
