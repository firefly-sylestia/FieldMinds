package fieldmind.research.app

import android.app.Application
import android.content.ComponentCallbacks2
import android.os.Build
import android.os.Process
import android.util.Log
import fieldmind.research.app.shared.data.model.AppSettings
import fieldmind.research.app.util.ANRWatchdog
import fieldmind.research.app.util.CrashReporter

/**
 * Custom Application class for FieldMind.
 * Handles initialization of:
 * - AppSettings
 * - CrashReporter
 * - NetworkClient
 * - LeakCanary (debug builds)
 * - ANR Watchdog (debug builds)
 */
class FieldMindApplication : Application() {
    
    companion object {
        private const val TAG = "FieldMindApplication"
        private const val TRIM_MEMORY_RUNNING_MODERATE_LEVEL = 5
        private const val TRIM_MEMORY_RUNNING_LOW_LEVEL = 10
        private const val TRIM_MEMORY_RUNNING_CRITICAL_LEVEL = 15
        private const val TRIM_MEMORY_MODERATE_LEVEL = 60
        private const val TRIM_MEMORY_COMPLETE_LEVEL = 80
        
        lateinit var instance: FieldMindApplication
            private set
    }
    
    private var anrWatchdog: ANRWatchdog? = null
    
    override fun onCreate() {
        super.onCreate()

        instance = this

        // ── Crash-process guard ────────────────────────────────────────────
        // FieldMindCrashActivity runs in :crash_process so the crash UI
        // survives the corrupted main process.  However the <application>
        // tag's android:name causes FieldMindApplication.onCreate() to
        // execute in EVERY process.  Heavy init (AppSettings, CrashReporter,
        // LeakCanary, ANRWatchdog) can itself crash and prevent the crash
        // UI from ever appearing, creating an infinite crash loop.
        //
        // Detect the crash process and bail out early — the crash activity
        // is self-contained and does not need any application-level init.
        if (isCrashProcess()) {
            Log.d(TAG, "Running in :crash_process — skipping initialization")
            return
        }
        
        Log.d(TAG, "═══════════════════════════════════════════════════")
        Log.d(TAG, "FieldMindApplication onCreate")
        Log.d(TAG, "Build Type: ${BuildConfig.BUILD_TYPE}")
        Log.d(TAG, "Version: ${BuildConfig.VERSION_NAME}")
        Log.d(TAG, "═══════════════════════════════════════════════════")

        // ── Startup crash detection ──
        // Check if the PREVIOUS app session ended with an uncaught crash.
        // Must run before any initialization that could itself crash.
        val previousCrash = runCatching {
            val fallbackPrefs = getSharedPreferences("fieldmind_crash_fallback", 0)
            fallbackPrefs.getString("last_fallback_crash", null)
        }.getOrNull()
        if (previousCrash != null) {
            Log.w(TAG, "⚠ Previous session ended with an uncaught crash (not viewed by user)")
            Log.w(TAG, "Crash (first 500 chars): ${previousCrash.take(500)}")
        }

        // ── Step 1: Initialize AppSettings (must succeed for crash reporting) ──
        runCatching {
            AppSettings.getInstance(applicationContext)
            Log.d(TAG, "✓ AppSettings initialized")
        }.onFailure { e ->
            Log.e(TAG, "✗ FAILED to initialize AppSettings", e)
            // Try one more time — sometimes the first call fails during process start
            runCatching {
                AppSettings.getInstance(applicationContext)
                Log.d(TAG, "✓ AppSettings initialized (retry)")
            }.onFailure { e2 ->
                Log.e(TAG, "✗ AppSettings init failed twice — crash reporting may be degraded", e2)
            }
        }

        // ── Step 2: Initialize CrashReporter (must succeed) ──
        runCatching {
            CrashReporter.init(this)
            Log.d(TAG, "✓ CrashReporter initialized")
        }.onFailure { e ->
            Log.e(TAG, "✗ FAILED to initialize CrashReporter", e)
        }

        // ── Step 3: Clear the fallback crash marker (now being handled) ──
        if (previousCrash != null) {
            runCatching {
                val fbPrefs = getSharedPreferences("fieldmind_crash_fallback", 0)
                fbPrefs.edit().remove("last_fallback_crash").apply()
                Log.d(TAG, "✓ Cleared previous session crash marker")
            }
            // Also record a startup indicator so the user knows the previous run crashed
            if (CrashReporter.isInitialized) {
                CrashReporter.recordNonFatal(
                    RuntimeException("Previous session ended with uncaught crash (not viewed)"),
                    "StartupCrashDetector"
                )
            }
        }

        // ── Step 4: Start ANR Watchdog (all builds) ──
        runCatching {
            startANRWatchdog()
            Log.d(TAG, "✓ ANR Watchdog started")
        }.onFailure { e ->
            Log.e(TAG, "✗ FAILED to start ANR Watchdog", e)
        }

        // ── Step 5: LeakCanary (debug builds only) ──
        if (BuildConfig.DEBUG) {
            runCatching {
                configureLeakCanary()
            }.onFailure { e ->
                Log.e(TAG, "✗ FAILED to configure LeakCanary", e)
            }
        }

        Log.d(TAG, "FieldMindApplication initialization complete")
        Log.d(TAG, "CrashReporter ready: ${CrashReporter.isInitialized}")
    }

    /**
     * Returns true when the current process is the dedicated :crash_process
     * that hosts [fieldmind.research.app.activities.FieldMindCrashActivity].
     *
     * We read /proc/self/cmdline rather than ActivityManager.getProcessName()
     * because the former works without any Android framework services and
     * cannot throw if the system is unstable (which is likely during a crash).
     */
    private fun isCrashProcess(): Boolean {
        return runCatching {
            val cmdline = java.io.File("/proc/${Process.myPid()}/cmdline").readText()
            cmdline.contains(":crash_process")
        }.getOrDefault(false)
    }
    
    private fun configureLeakCanary() {
        try {
            val debugConfigClass = Class.forName("fieldmind.research.app.debug.LeakCanaryDebugConfig")
            val applyMethod = debugConfigClass.getDeclaredMethod("applyKnownReferenceMatchers")
            applyMethod.invoke(null)
            Log.d(TAG, "✓ LeakCanary configured (auto-init + debug matcher tuning)")
        } catch (_: ClassNotFoundException) {
            Log.d(TAG, "✓ LeakCanary configured (auto-init)")
        } catch (e: Exception) {
            Log.e(TAG, "Error configuring LeakCanary", e)
        }
    }
    
    private fun startANRWatchdog() {
        try {
            anrWatchdog = ANRWatchdog(timeoutMs = 5000).apply {
                start()
            }
            Log.d(TAG, "✓ ANR Watchdog started")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting ANR Watchdog", e)
        }
    }
    
    override fun onTerminate() {
        Log.d(TAG, "FieldMindApplication onTerminate")
        anrWatchdog?.stopWatching()
        anrWatchdog = null
        super.onTerminate()
    }
    
    override fun onLowMemory() {
        super.onLowMemory()
        Log.w(TAG, "═══════════════════════════════════════════════════")
        Log.w(TAG, "LOW MEMORY WARNING!")
        Log.w(TAG, "═══════════════════════════════════════════════════")
    }
    
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        
        val levelName = when (level) {
            TRIM_MEMORY_RUNNING_MODERATE_LEVEL -> "RUNNING_MODERATE"
            TRIM_MEMORY_RUNNING_LOW_LEVEL -> "RUNNING_LOW"
            TRIM_MEMORY_RUNNING_CRITICAL_LEVEL -> "RUNNING_CRITICAL"
            ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> "UI_HIDDEN"
            ComponentCallbacks2.TRIM_MEMORY_BACKGROUND -> "BACKGROUND"
            TRIM_MEMORY_MODERATE_LEVEL -> "MODERATE"
            TRIM_MEMORY_COMPLETE_LEVEL -> "COMPLETE"
            else -> "UNKNOWN($level)"
        }
        
        Log.w(TAG, "onTrimMemory: $levelName")
        
        when (level) {
            TRIM_MEMORY_RUNNING_CRITICAL_LEVEL,
            TRIM_MEMORY_COMPLETE_LEVEL -> {
                Log.w(TAG, "Critical memory pressure - performing aggressive cleanup")
            }
            TRIM_MEMORY_RUNNING_LOW_LEVEL,
            TRIM_MEMORY_MODERATE_LEVEL -> {
                Log.w(TAG, "Moderate memory pressure - performing standard cleanup")
            }
        }
    }
}
