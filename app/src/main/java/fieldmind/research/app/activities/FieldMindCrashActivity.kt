package fieldmind.research.app.activities

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import kotlin.system.exitProcess

/**
 * Full-screen crash recovery UI — redesigned with compact summary card,
 * auto-detection of crash cause, and targeted recovery options.
 *
 * Runs in :crash_process with plain Android Views (no Compose) so it
 * cannot itself crash from resource-loading failures or broken themes.
 *
 * Layout:
 *  1. Error header — icon, title, auto-detected cause summary.
 *  2. Compact crash card — exception type + message, expandable full log.
 *  3. Targeted recovery — auto-detected fix button(s) + generic restart.
 *  4. Footer — reassurance that data is safe.
 */
class FieldMindCrashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val crashLog = intent.getStringExtra(EXTRA_CRASH_LOG) ?: "No crash log available."
        renderCrashUI(crashLog)
    }

    // ── Primary UI ────────────────────────────────────────────────────

    private fun renderCrashUI(crashLog: String) {
        val crashCategory = detectCrashCategory(crashLog)

        // Density helpers
        fun dp(v: Int) = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics
        ).toInt()

        val dp2 = dp(2); val dp4 = dp(4); val dp8 = dp(8); val dp10 = dp(10)
        val dp12 = dp(12); val dp14 = dp(14); val dp16 = dp(16); val dp20 = dp(20)
        val dp24 = dp(24); val dp28 = dp(28); val dp32 = dp(32); val dp36 = dp(36)
        val dp48 = dp(48); val dp52 = dp(52)

        // Hardcoded colors — no theme attributes
        val surfaceColor = 0xFFFAF7F2.toInt()
        val onSurfaceColor = 0xFF1B1B1B.toInt()
        val onSurfaceVariantColor = 0xFF5A5A5A.toInt()
        val cardBgColor = 0xFFFFFFFF.toInt()
        val primaryColor = 0xFF2E7D32.toInt()
        val primaryContainerColor = 0xFFD7E8D8.toInt()
        val errorColor = 0xFFC62828.toInt()
        val errorContainerColor = 0xFFFFEBEE.toInt()
        val warningColor = 0xFFE65100.toInt()
        val warningContainerColor = 0xFFFFF3E0.toInt()
        val infoColor = 0xFF1565C0.toInt()
        val infoContainerColor = 0xFFE3F2FD.toInt()
        val whiteColor = 0xFFFFFFFF.toInt()
        val outlineColor = 0xFF8B8680.toInt()

        val scrollView = ScrollView(this).apply {
            setBackgroundColor(surfaceColor)
            setPadding(0, statusBarHeight(), 0, 0)
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp20, dp20, dp20, dp24)
        }

        // ── 1. Header — compact error card ─────────────────────────
        root.addView(roundedCard(errorContainerColor, dp32).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp20, dp20, dp20, dp20)
        }.also { card ->
            // Error icon + title row
            val titleRow = LinearLayout(this@FieldMindCrashActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            titleRow.addView(TextView(this@FieldMindCrashActivity).apply {
                text = "\u26A0"
                textSize = 28f
                gravity = Gravity.CENTER
                val size = LinearLayout.LayoutParams(dp48, dp48)
                layoutParams = size
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(errorColor and 0x00FFFFFF or 0x22000000)
                }
            })
            titleRow.addView(space(dp14, 0))
            titleRow.addView(TextView(this@FieldMindCrashActivity).apply {
                text = "FieldMind encountered an issue"
                setTextColor(onSurfaceColor)
                textSize = 18f
                typeface = Typeface.DEFAULT_BOLD
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            })
            card.addView(titleRow)
            card.addView(space(dp10, 0))

            // Auto-detected cause
            val causeText = when (crashCategory) {
                CrashCategory.LOCK_PIN -> "This crash appears related to the privacy lock or PIN system."
                CrashCategory.DATABASE -> "This crash appears related to the local database or storage."
                CrashCategory.SETTINGS -> "This crash appears related to corrupted app settings."
                CrashCategory.NETWORK -> "This crash appears related to a network or API call."
                CrashCategory.COMPOSE -> "This crash appears related to the user interface rendering."
                CrashCategory.OUT_OF_MEMORY -> "The app ran out of memory — this may happen on older devices."
                CrashCategory.UNKNOWN -> "The cause could not be automatically determined from the crash report."
            }
            card.addView(TextView(this@FieldMindCrashActivity).apply {
                text = causeText
                setTextColor(onSurfaceVariantColor)
                textSize = 13f
                setLineSpacing(3f, 1f)
            })
        })

        root.addView(space(dp16, 0))

        // ── 2. Compact crash card with expandable details ──────────
        val exceptionName = extractExceptionName(crashLog)
        val exceptionMessage = extractExceptionMessage(crashLog)
        val fullLogVisible = booleanArrayOf(false) // mutable via array wrapper

        root.addView(roundedCard(cardBgColor, dp28).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp16, dp16, dp16, dp16)
        }.also { card ->
            // Title row
            val titleRow = LinearLayout(this@FieldMindCrashActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            titleRow.addView(TextView(this@FieldMindCrashActivity).apply {
                text = "\uD83D\uDCDD Crash details"
                setTextColor(onSurfaceColor)
                textSize = 15f
                typeface = Typeface.DEFAULT_BOLD
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            })
            // Copy button in title row
            titleRow.addView(textButton("Copy", primaryColor).apply {
                setOnClickListener { copyCrashLog(crashLog) }
            })
            card.addView(titleRow)
            card.addView(space(dp12, 0))

            // Exception type pill
            val pillBg = when (crashCategory) {
                CrashCategory.LOCK_PIN -> warningContainerColor
                CrashCategory.DATABASE -> errorContainerColor
                CrashCategory.SETTINGS -> warningContainerColor
                CrashCategory.NETWORK -> infoContainerColor
                CrashCategory.COMPOSE -> primaryContainerColor
                CrashCategory.OUT_OF_MEMORY -> errorContainerColor
                CrashCategory.UNKNOWN -> outlineColor and 0x00FFFFFF or 0x10000000
            }
            val pillTextColor = when (crashCategory) {
                CrashCategory.LOCK_PIN -> warningColor
                CrashCategory.DATABASE -> errorColor
                CrashCategory.SETTINGS -> warningColor
                CrashCategory.NETWORK -> infoColor
                CrashCategory.COMPOSE -> primaryColor
                CrashCategory.OUT_OF_MEMORY -> errorColor
                CrashCategory.UNKNOWN -> onSurfaceVariantColor
            }
            card.addView(roundedCard(pillBg, dp16).apply {
                setPadding(dp12, dp(6), dp12, dp(6))
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }.also { pill ->
                pill.addView(TextView(this@FieldMindCrashActivity).apply {
                    text = exceptionName
                    setTextColor(pillTextColor)
                    textSize = 12f
                    typeface = Typeface.MONOSPACE
                })
            })

            card.addView(space(dp8, 0))

            // Exception message (single line, truncated)
            card.addView(TextView(this@FieldMindCrashActivity).apply {
                text = exceptionMessage.ifBlank { "No additional details" }
                setTextColor(onSurfaceVariantColor)
                textSize = 12f
                maxLines = 2
                setLineSpacing(2f, 1f)
            })

            card.addView(space(dp10, 0))

            // ── Expandable full report toggle ──
            val toggleButton = textButton(
                if (fullLogVisible[0]) "\u25B2 Hide full report" else "\u25BC Show full report",
                primaryColor
            )
            val fullReportView = TextView(this@FieldMindCrashActivity).apply {
                text = crashLog
                setTextColor(onSurfaceColor)
                textSize = 10f
                typeface = Typeface.MONOSPACE
                setLineSpacing(1f, 1f)
                setTextIsSelectable(true)
                setPadding(dp12, dp12, dp12, dp12)
                background = GradientDrawable().apply {
                    setColor(outlineColor and 0x00FFFFFF or 0x0C000000)
                    cornerRadius = dp12.toFloat()
                }
                visibility = View.GONE
            }

            toggleButton.setOnClickListener {
                fullLogVisible[0] = !fullLogVisible[0]
                toggleButton.text = if (fullLogVisible[0]) "\u25B2 Hide full report" else "\u25BC Show full report"
                fullReportView.visibility = if (fullLogVisible[0]) View.VISIBLE else View.GONE
            }
            card.addView(toggleButton)
            card.addView(fullReportView)

            card.addView(space(dp10, 0))

            // Share button
            card.addView(styledButton(
                text = "Share report",
                bgColor = whiteColor,
                textColor = onSurfaceColor,
                borderColor = outlineColor,
                cornerRadius = dp24
            ).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(44)
                )
                setOnClickListener { shareCrashLog(crashLog) }
            })
        })

        root.addView(space(dp16, 0))

        // ── 3. Targeted Recovery card ──────────────────────────────
        root.addView(roundedCard(cardBgColor, dp28).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp16, dp16, dp16, dp16)
        }.also { card ->
            card.addView(TextView(this@FieldMindCrashActivity).apply {
                text = "\uD83D\uDD27 Recovery"
                setTextColor(onSurfaceColor)
                textSize = 15f
                typeface = Typeface.DEFAULT_BOLD
                setPadding(0, 0, 0, dp8)
            })

            // Targeted recovery description
            val recoveryDesc = when (crashCategory) {
                CrashCategory.LOCK_PIN -> "The lock/PIN system appears to be causing this crash. " +
                    "Disabling privacy lock and PIN will let the app start normally. " +
                    "You can re-enable them in Settings afterwards."
                CrashCategory.DATABASE -> "The local database may be corrupted. " +
                    "You can try clearing the app cache or restarting. Your cloud-synced data is safe."
                CrashCategory.SETTINGS -> "Some app settings may be corrupted. " +
                    "Resetting to default settings should resolve this without losing your research data."
                CrashCategory.NETWORK -> "A network or API error occurred. " +
                    "Restart the app — if the issue persists, check your connection or API configuration."
                CrashCategory.COMPOSE -> "The user interface encountered a rendering issue. " +
                    "This is usually transient — a restart should resolve it."
                CrashCategory.OUT_OF_MEMORY -> "The device ran out of memory. " +
                    "Close other apps and restart FieldMind. If this recurs, try reducing image sizes in Settings."
                CrashCategory.UNKNOWN -> "If FieldMind keeps crashing, the most common fixes are " +
                    "disabling the privacy lock/PIN or resetting app settings."
            }
            card.addView(TextView(this@FieldMindCrashActivity).apply {
                text = recoveryDesc
                setTextColor(onSurfaceVariantColor)
                textSize = 13f
                setLineSpacing(3f, 1f)
                setPadding(0, 0, 0, dp16)
            })

            // Primary targeted fix button
            when (crashCategory) {
                CrashCategory.LOCK_PIN -> {
                    card.addView(styledButton(
                        text = "\uD83D\uDD13 Disable lock & PIN, restart",
                        bgColor = warningColor,
                        textColor = whiteColor,
                        borderColor = null,
                        cornerRadius = dp28
                    ).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, dp52
                        ).apply { bottomMargin = dp10 }
                        setOnClickListener { showLockDisableConfirmDialog() }
                    })
                }
                CrashCategory.SETTINGS -> {
                    card.addView(styledButton(
                        text = "\u2699\uFE0F Reset settings & restart",
                        bgColor = warningColor,
                        textColor = whiteColor,
                        borderColor = null,
                        cornerRadius = dp28
                    ).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, dp52
                        ).apply { bottomMargin = dp10 }
                        setOnClickListener { showSettingsResetConfirmDialog() }
                    })
                }
                CrashCategory.DATABASE -> {
                    card.addView(styledButton(
                        text = "\uD83D\uDD04 Clear cache & restart",
                        bgColor = errorColor,
                        textColor = whiteColor,
                        borderColor = null,
                        cornerRadius = dp28
                    ).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, dp52
                        ).apply { bottomMargin = dp10 }
                        setOnClickListener {
                            runCatching { cacheDir.deleteRecursively() }
                            restartApp()
                        }
                    })
                }
                else -> {
                    // For network, compose, OOM, unknown — primary action is just restart
                    card.addView(styledButton(
                        text = "\uD83D\uDD04 Restart FieldMind",
                        bgColor = primaryColor,
                        textColor = whiteColor,
                        borderColor = null,
                        cornerRadius = dp28
                    ).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, dp52
                        ).apply { bottomMargin = dp10 }
                        setOnClickListener { restartApp() }
                    })
                }
            }

            // Secondary: always offer disable lock & PIN if not already the primary
            if (crashCategory != CrashCategory.LOCK_PIN) {
                card.addView(styledButton(
                    text = "Disable lock & PIN, then restart",
                    bgColor = whiteColor,
                    textColor = onSurfaceColor,
                    borderColor = outlineColor,
                    cornerRadius = dp28
                ).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, dp48
                    ).apply { bottomMargin = dp8 }
                    setOnClickListener { showLockDisableConfirmDialog() }
                })
            }

            // Generic restart if a targeted fix was already shown
            if (crashCategory != CrashCategory.UNKNOWN && crashCategory != CrashCategory.NETWORK &&
                crashCategory != CrashCategory.COMPOSE && crashCategory != CrashCategory.OUT_OF_MEMORY) {
                card.addView(styledButton(
                    text = "Restart without changes",
                    bgColor = whiteColor,
                    textColor = onSurfaceColor,
                    borderColor = outlineColor,
                    cornerRadius = dp28
                ).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, dp48
                    )
                    setOnClickListener { restartApp() }
                })
            }
        })

        root.addView(space(dp20, 0))

        // ── 4. Footer ──────────────────────────────────────────────
        root.addView(TextView(this@FieldMindCrashActivity).apply {
            text = "\uD83D\uDCAB Your research data is safe. Observations, notes, and projects " +
                "are preserved. Only the settings related to the detected issue will be affected."
            setTextColor(onSurfaceVariantColor)
            textSize = 11f
            gravity = Gravity.CENTER
            setLineSpacing(3f, 1f)
            setPadding(dp8, 0, dp8, dp24)
        })

        scrollView.addView(root)
        setContentView(scrollView)
    }

    // ── Crash Auto-Detection ────────────────────────────────────────

    private enum class CrashCategory {
        LOCK_PIN, DATABASE, SETTINGS, NETWORK, COMPOSE, OUT_OF_MEMORY, UNKNOWN
    }

    private fun detectCrashCategory(crashLog: String): CrashCategory {
        val lowerLog = crashLog.lowercase()

        // Check for lock/PIN related crashes
        if (lowerLog.contains("cipher") || lowerLog.contains("keystore") ||
            lowerLog.contains("fingerprint") || lowerLog.contains("biometric") ||
            lowerLog.contains("pin") || lowerLog.contains("lock") ||
            lowerLog.contains("encrypt") || lowerLog.contains("decrypt") ||
            lowerLog.contains("crypto") || lowerLog.contains("security") ||
            (lowerLog.contains("privacy") && lowerLog.contains("lock"))
        ) return CrashCategory.LOCK_PIN

        // Check for database/storage crashes
        if (lowerLog.contains("room") || lowerLog.contains("sqlite") ||
            lowerLog.contains("database") || lowerLog.contains("cursor") ||
            lowerLog.contains("migration") || lowerLog.contains("dao") ||
            lowerLog.contains("disk") || lowerLog.contains("storage") ||
            lowerLog.contains("ioexception") || lowerLog.contains("file")
        ) return CrashCategory.DATABASE

        // Check for settings-related crashes
        if (lowerLog.contains("sharedpreferences") || lowerLog.contains("settings") ||
            lowerLog.contains("preference") || lowerLog.contains("configuration") ||
            lowerLog.contains("datastore")
        ) return CrashCategory.SETTINGS

        // Check for network/API crashes
        if (lowerLog.contains("http") || lowerLog.contains("socket") ||
            lowerLog.contains("network") || lowerLog.contains("timeout") ||
            lowerLog.contains("connect") || lowerLog.contains("api") ||
            lowerLog.contains("retrofit") || lowerLog.contains("okhttp") ||
            lowerLog.contains("volley") || lowerLog.contains("ssl")
        ) return CrashCategory.NETWORK

        // Check for Compose/UI crashes
        if (lowerLog.contains("compose") || lowerLog.contains("recompose") ||
            lowerLog.contains("modifier") || lowerLog.contains("graphics") ||
            lowerLog.contains("layout") || lowerLog.contains("animation") ||
            lowerLog.contains("drawable") || lowerLog.contains("canvas") ||
            lowerLog.contains("ripple") || lowerLog.contains("surface")
        ) return CrashCategory.COMPOSE

        // OutOfMemoryError
        if (lowerLog.contains("outofmemory") || lowerLog.contains("out of memory"))
            return CrashCategory.OUT_OF_MEMORY

        return CrashCategory.UNKNOWN
    }

    private fun extractExceptionName(crashLog: String): String {
        // Look for "Exception: com.example.SomeException" or the class name
        val lines = crashLog.lines()
        for (line in lines) {
            if (line.contains("Exception:")) {
                val parts = line.substringAfter("Exception:").trim()
                // Extract just the simple class name
                return parts.substringAfterLast(".").ifBlank { parts }
            }
        }
        // Fallback: look for the first line with a Java exception pattern
        for (line in lines) {
            val trimmed = line.trim()
            if ((trimmed.contains("Exception") || trimmed.contains("Error")) &&
                trimmed.contains(".")
            ) {
                val simpleName = trimmed.substringAfterLast(".").substringBefore(":")
                    .substringBefore(" ").trim()
                if (simpleName.isNotBlank()) return simpleName
            }
        }
        return "Unknown error"
    }

    private fun extractExceptionMessage(crashLog: String): String {
        val lines = crashLog.lines()
        for (line in lines) {
            if (line.contains("Message:")) {
                return line.substringAfter("Message:").trim().ifBlank { "No details available" }
            }
        }
        // Fallback: Caused by message
        for (line in lines) {
            if (line.trimStart().startsWith("Caused by:") && line.contains(":")) {
                return line.substringAfter("Caused by:").trim()
            }
        }
        return "No details available"
    }

    // ── View Builders ───────────────────────────────────────────────

    private fun roundedCard(bgColor: Int, cornerRadius: Int): LinearLayout {
        return LinearLayout(this).apply {
            background = GradientDrawable().apply {
                setColor(bgColor)
                this.cornerRadius = cornerRadius.toFloat()
            }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    private fun styledButton(
        text: String,
        bgColor: Int,
        textColor: Int,
        borderColor: Int?,
        cornerRadius: Int
    ): Button {
        return Button(this).apply {
            this.text = text
            setTextColor(textColor)
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setAllCaps(false)
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                setColor(bgColor)
                this.cornerRadius = cornerRadius.toFloat()
                if (borderColor != null) {
                    setStroke(dp(2), borderColor)
                }
            }
            setPadding(dp(16), 0, dp(16), 0)
        }
    }

    private fun textButton(text: String, color: Int): TextView {
        return TextView(this).apply {
            this.text = text
            setTextColor(color)
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(dp(12), dp(6), dp(12), dp(6))
            background = GradientDrawable().apply {
                setColor(color and 0x00FFFFFF or 0x10000000)
                cornerRadius = dp(16).toFloat()
            }
        }
    }

    private fun space(heightDp: Int, widthDp: Int): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                if (widthDp > 0) widthDp else ViewGroup.LayoutParams.MATCH_PARENT,
                if (heightDp > 0) heightDp else ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    private fun dp(value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics
        ).toInt()
    }

    private fun statusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else dp(24)
    }

    // ── Confirmation Dialogs ────────────────────────────────────────

    private fun showLockDisableConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("Disable lock & PIN?")
            .setMessage(
                "This will disable the privacy lock, app PIN, decoy PIN, panic-lock, " +
                    "biometric settings, and export password protection.\n\n" +
                    "Your research data will NOT be affected. You can re-enable these " +
                    "in Settings after the app restarts."
            )
            .setPositiveButton("Disable & restart") { _, _ -> disableSecurityAndRestart() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSettingsResetConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("Reset settings?")
            .setMessage(
                "This will reset all app preferences to their defaults. " +
                    "Your observations, notes, and research data will NOT be affected.\n\n" +
                    "You'll need to reconfigure your preferences after the restart."
            )
            .setPositiveButton("Reset & restart") { _, _ -> resetSettingsAndRestart() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ── Actions ─────────────────────────────────────────────────────

    private fun copyCrashLog(crashLog: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("FieldMind Crash Report", crashLog))
        Toast.makeText(this, "Crash report copied", Toast.LENGTH_SHORT).show()
    }

    private fun shareCrashLog(crashLog: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "FieldMind Crash Report")
            putExtra(Intent.EXTRA_TEXT, crashLog)
        }
        runCatching {
            startActivity(
                Intent.createChooser(shareIntent, "Share crash report")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }.onFailure {
            copyCrashLog(crashLog)
        }
    }

    private fun disableSecurityAndRestart() {
        runCatching {
            val prefs: SharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
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
        Toast.makeText(this, "Lock & PIN disabled. Restarting\u2026", Toast.LENGTH_SHORT).show()
        restartApp()
    }

    private fun resetSettingsAndRestart() {
        runCatching {
            val prefs: SharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().clear().commit()
            // Re-apply security defaults (disabled) for safe restart
            prefs.edit()
                .putBoolean(KEY_PRIVACY_LOCK, false)
                .putBoolean(KEY_APP_PIN_ENABLED, false)
                .commit()
        }.onFailure {
            Log.e(TAG, "Failed to reset settings before restart", it)
        }
        Toast.makeText(this, "Settings reset. Restarting\u2026", Toast.LENGTH_SHORT).show()
        restartApp()
    }

    private fun restartApp() {
        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
            )
        }
        if (intent != null) startActivity(intent)
        exitProcess(0)
    }

    companion object {
        private const val TAG = "FieldMindCrashActivity"
        const val EXTRA_CRASH_LOG = "extra_crash_log"

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
    }
}
