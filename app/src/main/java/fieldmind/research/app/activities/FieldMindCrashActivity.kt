package fieldmind.research.app.activities

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
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
 * Material Design 3 crash recovery UI.
 *
 * Runs in :crash_process using plain Android Views — no Compose, no theme attributes.
 *
 * Color system follows M3 token roles:
 *   • Primary      = #4355B9 (indigo-blue)
 *   • OnPrimary    = white
 *   • PrimaryContainer = #DEE0FF
 *   • Surface      = white / #FEFBFF
 *   • SurfaceVariant = #E7E0EC
 *   • OnSurface    = #1C1B1F
 *   • OnSurfaceVariant = #49454F
 *   • Error        = #BA1A1A
 *   • ErrorContainer = #FFDAD6
 *   • Outline      = #79747E
 *   • OutlineVariant = #CAC4D0
 */
class FieldMindCrashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val crashLog = intent.getStringExtra(EXTRA_CRASH_LOG) ?: "No crash log available."
        renderCrashUI(crashLog)
    }

    // ════════════════════════════════════════════════════════════════════
    //  M3 Color Tokens
    // ════════════════════════════════════════════════════════════════════

    private object M3 {
        val primary          = 0xFF4355B9.toInt()
        val onPrimary        = 0xFFFFFFFF.toInt()
        val primaryContainer = 0xFFDEE0FF.toInt()
        val onPrimaryContainer = 0xFF001257.toInt()
        val secondary        = 0xFF595D72.toInt()
        val secondaryContainer = 0xFFDFE1F9.toInt()
        val surface          = 0xFFFEFBFF.toInt()
        val surfaceVariant   = 0xFFE7E0EC.toInt()
        val onSurface        = 0xFF1C1B1F.toInt()
        val onSurfaceVariant = 0xFF49454F.toInt()
        val outline          = 0xFF79747E.toInt()
        val outlineVariant   = 0xFFCAC4D0.toInt()
        val error            = 0xFFBA1A1A.toInt()
        val onError          = 0xFFFFFFFF.toInt()
        val errorContainer   = 0xFFFFDAD6.toInt()
        val onErrorContainer = 0xFF410002.toInt()
        val scrim            = 0xFF1C1B1F.toInt()
    }

    // ════════════════════════════════════════════════════════════════════
    //  Crash Category Detection + Metadata
    // ════════════════════════════════════════════════════════════════════

    private enum class CrashCategory {
        LOCK_PIN, DATABASE, SETTINGS, NETWORK, COMPOSE, OUT_OF_MEMORY, UNKNOWN
    }

    private data class CatMeta(
        val label: String,
        val recoveryDesc: String,
        val chipText: String,
        val chipBg: Int,
        val chipTextColor: Int,
        val isDestructive: Boolean
    )

    private fun catMeta(cat: CrashCategory): CatMeta = when (cat) {
        CrashCategory.LOCK_PIN -> CatMeta(
            label = "The privacy lock or PIN system is causing this crash.",
            recoveryDesc = "Disabling the privacy lock and PIN should let the app start normally. You can re-enable them later in Settings.",
            chipText = "Security",
            chipBg = M3.errorContainer,
            chipTextColor = M3.onErrorContainer,
            isDestructive = true
        )
        CrashCategory.DATABASE -> CatMeta(
            label = "The local database or storage appears to be corrupted.",
            recoveryDesc = "Try clearing the app cache. Your research observations, notes, and data are stored separately and will not be affected.",
            chipText = "Storage",
            chipBg = M3.errorContainer,
            chipTextColor = M3.onErrorContainer,
            isDestructive = true
        )
        CrashCategory.SETTINGS -> CatMeta(
            label = "Corrupted app preferences are preventing FieldMind from starting.",
            recoveryDesc = "Resetting all preferences to defaults should resolve this. Your field data (observations, notes, projects) is stored separately and will be preserved.",
            chipText = "Settings",
            chipBg = M3.errorContainer,
            chipTextColor = M3.onErrorContainer,
            isDestructive = true
        )
        CrashCategory.NETWORK -> CatMeta(
            label = "A network or connectivity error occurred.",
            recoveryDesc = "This is usually transient. Restarting should resolve it. If the issue persists, check your internet connection or API credentials in Settings.",
            chipText = "Network",
            chipBg = M3.primaryContainer,
            chipTextColor = M3.onPrimaryContainer,
            isDestructive = false
        )
        CrashCategory.COMPOSE -> CatMeta(
            label = "The user interface encountered a rendering error.",
            recoveryDesc = "This is usually a one-time glitch. A restart should resolve it. If it keeps happening, try disabling custom animations in Settings.",
            chipText = "UI",
            chipBg = M3.secondaryContainer,
            chipTextColor = M3.onSurface,
            isDestructive = false
        )
        CrashCategory.OUT_OF_MEMORY -> CatMeta(
            label = "The device ran out of memory.",
            recoveryDesc = "Close other apps and restart FieldMind. On older devices, consider reducing image quality in Capture Settings.",
            chipText = "Memory",
            chipBg = M3.errorContainer,
            chipTextColor = M3.onErrorContainer,
            isDestructive = false
        )
        CrashCategory.UNKNOWN -> CatMeta(
            label = "The cause could not be automatically identified from the crash log.",
            recoveryDesc = "If FieldMind keeps crashing, the most common fixes are disabling the privacy lock/PIN or resetting app preferences.",
            chipText = "Unknown",
            chipBg = M3.surfaceVariant,
            chipTextColor = M3.onSurfaceVariant,
            isDestructive = false
        )
    }

    private fun detectCrashCategory(crashLog: String): CrashCategory {
        val l = crashLog.lowercase()
        if (l.contains("cipher") || l.contains("keystore") || l.contains("fingerprint") ||
            l.contains("biometric") || l.contains("pin") || l.contains("encrypt") ||
            l.contains("decrypt") || l.contains("crypto") || l.contains("security") ||
            (l.contains("privacy") && l.contains("lock")) || l.contains("lock")
        ) return CrashCategory.LOCK_PIN
        if (l.contains("room") || l.contains("sqlite") || l.contains("database") ||
            l.contains("cursor") || l.contains("migration") || l.contains("dao") ||
            l.contains("disk") || l.contains("storage") || l.contains("ioexception") ||
            l.contains("file")
        ) return CrashCategory.DATABASE
        if (l.contains("sharedpreferences") || l.contains("settings") ||
            l.contains("preference") || l.contains("configuration") || l.contains("datastore")
        ) return CrashCategory.SETTINGS
        if (l.contains("http") || l.contains("socket") || l.contains("network") ||
            l.contains("timeout") || l.contains("connect") || l.contains("api") ||
            l.contains("retrofit") || l.contains("okhttp") || l.contains("ssl")
        ) return CrashCategory.NETWORK
        if (l.contains("compose") || l.contains("recompose") || l.contains("modifier") ||
            l.contains("graphics") || l.contains("layout") || l.contains("animation") ||
            l.contains("drawable") || l.contains("canvas") || l.contains("ripple")
        ) return CrashCategory.COMPOSE
        if (l.contains("outofmemory") || l.contains("out of memory"))
            return CrashCategory.OUT_OF_MEMORY
        return CrashCategory.UNKNOWN
    }

    private fun extractExceptionName(crashLog: String): String {
        for (line in crashLog.lines()) {
            if (line.contains("Exception:")) {
                val p = line.substringAfter("Exception:").trim()
                return p.substringAfterLast(".").ifBlank { p }
            }
        }
        for (line in crashLog.lines()) {
            val t = line.trim()
            if ((t.contains("Exception") || t.contains("Error")) && t.contains(".")) {
                val sn = t.substringAfterLast(".").substringBefore(":").substringBefore(" ").trim()
                if (sn.isNotBlank()) return sn
            }
        }
        return "Unknown error"
    }

    private fun extractExceptionMessage(crashLog: String): String {
        for (line in crashLog.lines()) {
            if (line.contains("Message:"))
                return line.substringAfter("Message:").trim().ifBlank { "No details" }
        }
        for (line in crashLog.lines()) {
            if (line.trimStart().startsWith("Caused by:") && line.contains(":"))
                return line.substringAfter("Caused by:").trim()
        }
        return "No details available"
    }

    // ════════════════════════════════════════════════════════════════════
    //  UI Renderer
    // ════════════════════════════════════════════════════════════════════

    private fun renderCrashUI(crashLog: String) {
        val cat = detectCrashCategory(crashLog)
        val meta = catMeta(cat)
        val excName = extractExceptionName(crashLog)
        val excMsg = extractExceptionMessage(crashLog)

        fun dp(v: Int) = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics
        ).toInt()

        val d2 = dp(2); val d4 = dp(4); val d6 = dp(6); val d8 = dp(8); val d10 = dp(10)
        val d12 = dp(12); val d16 = dp(16); val d20 = dp(20); val d24 = dp(24)
        val d28 = dp(28); val d32 = dp(32); val d36 = dp(36); val d40 = dp(40)
        val d48 = dp(48); val d52 = dp(52); val d56 = dp(56); val d64 = dp(64)

        val scroll = ScrollView(this).apply {
            setBackgroundColor(M3.surface)
            setPadding(0, statusBarH(), 0, 0)
            isVerticalScrollBarEnabled = false
            isFillViewport = true
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(d20, d24, d20, d32)
        }

        // ═══════════════════════════════════════════════════════════════
        //  1. HERO CARD — elevated M3 card
        // ═══════════════════════════════════════════════════════════════
        root.addView(m3Card(M3.surface, d28, elevationDp = 6).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(d24, d28, d24, d24)
        }.also { card ->
            // Error icon
            val iconRow = LinearLayout(this@FieldMindCrashActivity).apply {
                gravity = Gravity.CENTER
            }
            val iconView = TextView(this@FieldMindCrashActivity).apply {
                text = "\u26A0\uFE0F"
                textSize = 36f
                gravity = Gravity.CENTER
                val sz = LinearLayout.LayoutParams(d64, d64)
                layoutParams = sz
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(M3.errorContainer)
                }
            }
            iconRow.addView(iconView)
            card.addView(iconRow)
            card.addView(space(d16))

            // Title
            card.addView(tv("FieldMind stopped unexpectedly", 22f, M3.onSurface, bold = true, center = true))
            card.addView(space(d10))

            // Subtitle
            card.addView(tv(meta.label, 14f, M3.onSurfaceVariant, center = true).apply {
                setPadding(d8, 0, d8, 0)
                setLineSpacing(4f, 1f)
            })

            card.addView(space(d12))

            // Chip
            val chipRow = LinearLayout(this@FieldMindCrashActivity).apply { gravity = Gravity.CENTER }
            chipRow.addView(chip(meta.chipText, meta.chipBg, meta.chipTextColor))
            card.addView(chipRow)
        })

        root.addView(space(d16))

        // ═══════════════════════════════════════════════════════════════
        //  2. DETAILS CARD
        // ═══════════════════════════════════════════════════════════════
        val expanded = booleanArrayOf(false)

        root.addView(m3Card(M3.surface, d28, elevationDp = 3).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(d20, d20, d20, d20)
        }.also { card ->
            // Header row
            val hdr = LinearLayout(this@FieldMindCrashActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            hdr.addView(tv("Crash details", 16f, M3.onSurface, bold = true).apply {
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            })
            hdr.addView(textBtn("Copy", M3.primary).apply {
                setOnClickListener { copyCrashLog(crashLog) }
            })
            card.addView(hdr)
            card.addView(space(d12))

            // Exception chip
            card.addView(chip(excName, M3.surfaceVariant, M3.onSurfaceVariant))
            card.addView(space(d8))

            // Exception message
            card.addView(tv(excMsg.ifBlank { "No additional details" }, 13f, M3.onSurfaceVariant).apply {
                maxLines = 2
                setLineSpacing(3f, 1f)
            })

            card.addView(space(d10))

            // Divider
            card.addView(divider())

            card.addView(space(d8))

            // Toggle button
            val toggle = textBtn("Show full report", M3.primary)
            val fullLog = tv(crashLog, 11f, M3.onSurface).apply {
                typeface = Typeface.MONOSPACE
                setLineSpacing(2f, 1f)
                setTextIsSelectable(true)
                background = GradientDrawable().apply {
                    setColor(M3.surfaceVariant)
                    cornerRadius = d12.toFloat()
                }
                setPadding(d16, d16, d16, d16)
                visibility = View.GONE
            }
            toggle.setOnClickListener {
                expanded[0] = !expanded[0]
                toggle.text = if (expanded[0]) "Hide full report" else "Show full report"
                fullLog.visibility = if (expanded[0]) View.VISIBLE else View.GONE
            }

            val tglRow = LinearLayout(this@FieldMindCrashActivity).apply { gravity = Gravity.CENTER }
            tglRow.addView(toggle)
            card.addView(tglRow)
            card.addView(fullLog)

            card.addView(space(d12))

            // Outlined share button
            card.addView(m3OutlinedButton("Share report").apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, d48)
                setOnClickListener { shareCrashLog(crashLog) }
            })
        })

        root.addView(space(d16))

        // ═══════════════════════════════════════════════════════════════
        //  3. RECOVERY CARD
        // ═══════════════════════════════════════════════════════════════
        root.addView(m3Card(M3.surface, d28, elevationDp = 3).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(d20, d20, d20, d20)
        }.also { card ->
            card.addView(tv("Recovery options", 16f, M3.onSurface, bold = true))
            card.addView(space(d8))
            card.addView(tv(meta.recoveryDesc, 13f, M3.onSurfaceVariant).apply {
                setLineSpacing(4f, 1f)
                setPadding(0, 0, 0, d16)
            })

            // Primary filled button
            when (cat) {
                CrashCategory.LOCK_PIN -> card.addView(m3FilledButton("Disable lock & PIN, then restart",
                    bg = M3.error, textColor = M3.onError
                ).apply {
                    layoutParams = btnLp(btnH = d56, marginB = d10)
                    setOnClickListener { showLockDisableConfirmDialog() }
                })
                CrashCategory.SETTINGS -> card.addView(m3FilledButton("Reset settings & restart",
                    bg = M3.error, textColor = M3.onError
                ).apply {
                    layoutParams = btnLp(btnH = d56, marginB = d10)
                    setOnClickListener { showSettingsResetConfirmDialog() }
                })
                CrashCategory.DATABASE -> card.addView(m3FilledButton("Clear cache & restart",
                    bg = M3.error, textColor = M3.onError
                ).apply {
                    layoutParams = btnLp(btnH = d56, marginB = d10)
                    setOnClickListener {
                        runCatching { cacheDir.deleteRecursively() }
                        restartApp()
                    }
                })
                else -> card.addView(m3FilledButton("Restart FieldMind").apply {
                    layoutParams = btnLp(btnH = d56, marginB = d10)
                    setOnClickListener { restartApp() }
                })
            }

            // Secondary outlined button (disable lock — always shown except when it IS the primary)
            if (cat != CrashCategory.LOCK_PIN) {
                card.addView(m3OutlinedButton("Disable lock & PIN, restart").apply {
                    layoutParams = btnLp(btnH = d52, marginB = d8)
                    setOnClickListener { showLockDisableConfirmDialog() }
                })
            }

            // Tertiary text button (restart without changes)
            if (cat == CrashCategory.SETTINGS || cat == CrashCategory.DATABASE || cat == CrashCategory.LOCK_PIN) {
                card.addView(m3TextButton("Restart without changes").apply {
                    layoutParams = btnLp(btnH = d48)
                    setOnClickListener { restartApp() }
                })
            }
        })

        root.addView(space(d24))

        // ═══════════════════════════════════════════════════════════════
        //  4. FOOTER
        // ═══════════════════════════════════════════════════════════════
        root.addView(tv("Your research data is safe \u2014 observations, notes, and projects are fully preserved.", 12f,
            M3.onSurfaceVariant, center = true).apply {
            alpha = 0.7f
            setPadding(d24, 0, d24, d8)
        })
        val v = runCatching { packageManager.getPackageInfo(packageName, 0).versionName }.getOrDefault("")
        root.addView(tv("FieldMind \u2022 v$v", 11f, M3.onSurfaceVariant, center = true).apply {
            alpha = 0.5f
            setPadding(0, 0, 0, d20)
        })

        scroll.addView(root)
        setContentView(scroll)
    }

    // ════════════════════════════════════════════════════════════════════
    //  M3 View Builders
    // ════════════════════════════════════════════════════════════════════

    private fun m3Card(bg: Int, radius: Int, elevationDp: Int): LinearLayout {
        val elev = dp(elevationDp)
        return LinearLayout(this).apply {
            background = LayerDrawable(arrayOf(
                GradientDrawable().apply {  // shadow
                    setColor((M3.scrim and 0x00FFFFFF) or (0x08 shl 24))
                    cornerRadius = radius.toFloat()
                },
                GradientDrawable().apply {  // surface
                    setColor(bg)
                    cornerRadius = radius.toFloat()
                }
            )).apply {
                setLayerInset(0, 0, 0, 0, 0)       // shadow: full bounds
                setLayerInset(1, 0, 0, 0, elev)    // card: bottom-inset reveals shadow
            }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    private fun m3FilledButton(
        text: String,
        bg: Int = M3.primary,
        textColor: Int = M3.onPrimary
    ): Button = Button(this).apply {
        this.text = text
        setTextColor(textColor)
        textSize = 14f
        typeface = Typeface.DEFAULT_BOLD
        setAllCaps(false)
        gravity = Gravity.CENTER
        letterSpacing = 0.01f
        minHeight = 0
        minimumHeight = 0
        setPadding(dp(24), dp(12), dp(24), dp(12))
        background = GradientDrawable().apply {
            setColor(bg)
            cornerRadius = dp(28).toFloat()
        }
    }

    private fun m3OutlinedButton(
        text: String,
        textColor: Int = M3.primary,
        borderColor: Int = M3.outline
    ): Button = Button(this).apply {
        this.text = text
        setTextColor(textColor)
        textSize = 14f
        typeface = Typeface.DEFAULT_BOLD
        setAllCaps(false)
        gravity = Gravity.CENTER
        letterSpacing = 0.01f
        minHeight = 0
        minimumHeight = 0
        setPadding(dp(24), dp(12), dp(24), dp(12))
        background = GradientDrawable().apply {
            setColor(android.graphics.Color.TRANSPARENT)
            cornerRadius = dp(28).toFloat()
            setStroke(dp(1), borderColor)
        }
    }

    private fun m3TextButton(text: String, color: Int = M3.primary): Button = Button(this).apply {
        this.text = text
        setTextColor(color)
        textSize = 14f
        typeface = Typeface.DEFAULT_BOLD
        setAllCaps(false)
        gravity = Gravity.CENTER
        letterSpacing = 0.01f
        minHeight = 0
        minimumHeight = 0
        setPadding(dp(16), dp(10), dp(16), dp(10))
        background = ColorDrawable(android.graphics.Color.TRANSPARENT)
    }

    private fun textBtn(text: String, color: Int): TextView = TextView(this).apply {
        this.text = text
        setTextColor(color)
        textSize = 13f
        typeface = Typeface.DEFAULT_BOLD
        setPadding(dp(12), dp(6), dp(12), dp(6))
        background = GradientDrawable().apply {
            setColor((color and 0x00FFFFFF) or 0x0E000000)
            cornerRadius = dp(16).toFloat()
        }
    }

    private fun chip(text: String, bg: Int, textColor: Int): TextView = TextView(this).apply {
        this.text = text
        setTextColor(textColor)
        textSize = 12f
        typeface = Typeface.DEFAULT_BOLD
        setPadding(dp(16), dp(6), dp(16), dp(6))
        background = GradientDrawable().apply {
            setColor(bg)
            cornerRadius = dp(20).toFloat()
        }
    }

    private fun tv(text: String, size: Float, color: Int, bold: Boolean = false, center: Boolean = false): TextView =
        TextView(this).apply {
            this.text = text
            setTextColor(color)
            textSize = size
            if (bold) typeface = Typeface.DEFAULT_BOLD
            if (center) gravity = Gravity.CENTER
        }

    private fun space(h: Int): View = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h)
    }

    private fun divider(): View = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)).apply {
            topMargin = dp(4); bottomMargin = dp(4)
        }
        setBackgroundColor(M3.outlineVariant)
    }

    private fun btnLp(btnH: Int, marginB: Int = 0) =
        LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, btnH).apply { bottomMargin = marginB }

    private fun dp(v: Int) = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics
    ).toInt()

    private fun statusBarH(): Int {
        val id = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (id > 0) resources.getDimensionPixelSize(id) else dp(24)
    }

    // ════════════════════════════════════════════════════════════════════
    //  Actions & Dialogs
    // ════════════════════════════════════════════════════════════════════

    private fun showLockDisableConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("Disable lock & PIN?")
            .setMessage("This will disable the privacy lock, app PIN, decoy PIN, panic-lock, " +
                "biometric settings, and export password protection.\n\n" +
                "Your research data will NOT be affected. You can re-enable these " +
                "in Settings after the app restarts.")
            .setPositiveButton("Disable & restart") { _, _ -> disableSecurityAndRestart() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSettingsResetConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("Reset settings?")
            .setMessage("This will reset all app preferences to their defaults. " +
                "Your observations, notes, and research data will NOT be affected.\n\n" +
                "You'll need to reconfigure your preferences after the restart.")
            .setPositiveButton("Reset & restart") { _, _ -> resetSettingsAndRestart() }
            .setNegativeButton("Cancel", null)
            .show()
    }

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
            startActivity(Intent.createChooser(shareIntent, "Share crash report")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }.onFailure { copyCrashLog(crashLog) }
    }

    private fun disableSecurityAndRestart() {
        runCatching {
            getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
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
        }.onFailure { Log.e(TAG, "Failed to disable security settings", it) }
        Toast.makeText(this, "Lock & PIN disabled. Restarting\u2026", Toast.LENGTH_SHORT).show()
        restartApp()
    }

    private fun resetSettingsAndRestart() {
        runCatching {
            getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().commit()
            getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                .putBoolean(KEY_PRIVACY_LOCK, false)
                .putBoolean(KEY_APP_PIN_ENABLED, false)
                .commit()
        }.onFailure { Log.e(TAG, "Failed to reset settings", it) }
        Toast.makeText(this, "Settings reset. Restarting\u2026", Toast.LENGTH_SHORT).show()
        restartApp()
    }

    private fun restartApp() {
        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        if (intent != null) startActivity(intent)
        exitProcess(0)
    }

    companion object {
        private const val TAG = "FieldMindCrashActivity"
        const val EXTRA_CRASH_LOG = "extra_crash_log"
        private const val PREFS_NAME = "fieldmind_settings"
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

        fun start(context: Context, crashLog: String) {
            val intent = Intent(context, FieldMindCrashActivity::class.java).apply {
                putExtra(EXTRA_CRASH_LOG, crashLog)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            context.startActivity(intent)
        }
    }
}
