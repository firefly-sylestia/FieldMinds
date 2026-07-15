package fieldmind.research.app.activities

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color as AndroidColor
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.StateListDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import kotlin.system.exitProcess

/**
 * Clean, minimal crash recovery UI.
 *
 * Runs in :crash_process using plain Android Views — no Compose, no theme resources.
 * Features a flowing single-surface design with no layered shadow boxes,
 * soft rounded corners, and clean typography.
 */
class FieldMindCrashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val crashLog = intent.getStringExtra(EXTRA_CRASH_LOG) ?: "No crash log available."
        renderCrashUI(crashLog)
    }

    // ════════════════════════════════════════════════════════════════════
    //  Color Palette
    // ════════════════════════════════════════════════════════════════════

    private object Pal {
        val bg            = 0xFFF8F6F0.toInt()
        val surface       = 0xFFFFFFFF.toInt()
        val surfaceAlt    = 0xFFF0EDF4.toInt()
        val onSurface     = 0xFF1C1B1F.toInt()
        val onSurfaceVar  = 0xFF6B6478.toInt()
        val primary       = 0xFF5B5FC7.toInt()
        val onPrimary     = 0xFFFFFFFF.toInt()
        val error         = 0xFFBA1A1A.toInt()
        val errorSurface  = 0xFFFFF0EE.toInt()
        val onErrorSurface = 0xFF8C001A.toInt()
        val divider       = 0xFFE0DCE8.toInt()
    }

    // ════════════════════════════════════════════════════════════════════
    //  Crash Category Detection
    // ════════════════════════════════════════════════════════════════════

    private enum class CrashCategory {
        LOCK_PIN, DATABASE, SETTINGS, NETWORK, COMPOSE, OUT_OF_MEMORY, UNKNOWN
    }

    private data class CatMeta(
        val label: String, val recoveryDesc: String, val isDestructive: Boolean
    )

    private fun catMeta(cat: CrashCategory): CatMeta = when (cat) {
        CrashCategory.LOCK_PIN -> CatMeta(
            "The privacy lock or PIN system caused a crash.",
            "Disable the lock & PIN, then restart. You can re-enable them later in Settings.",
            true
        )
        CrashCategory.DATABASE -> CatMeta(
            "The local database appears to be corrupted.",
            "Clear the app cache and restart. Your research data is stored separately.",
            true
        )
        CrashCategory.SETTINGS -> CatMeta(
            "Corrupted preferences are preventing startup.",
            "Reset all preferences to defaults. Field data is preserved separately.",
            true
        )
        CrashCategory.NETWORK -> CatMeta(
            "A network error occurred.",
            "This is usually transient. Restarting should resolve it.",
            false
        )
        CrashCategory.COMPOSE -> CatMeta(
            "The UI encountered a rendering error.",
            "A restart should fix this. Try disabling custom animations if it recurs.",
            false
        )
        CrashCategory.OUT_OF_MEMORY -> CatMeta(
            "The device ran out of memory.",
            "Close other apps and restart. Reduce image quality in Capture Settings.",
            false
        )
        CrashCategory.UNKNOWN -> CatMeta(
            "The cause couldn't be automatically identified.",
            "Try disabling the privacy lock/PIN or resetting app preferences.",
            false
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
            l.contains("disk") || l.contains("storage") || l.contains("ioexception") || l.contains("file")
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
        if (l.contains("outofmemory") || l.contains("out of memory")) return CrashCategory.OUT_OF_MEMORY
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

    // ════════════════════════════════════════════════════════════════════
    //  UI Renderer
    // ════════════════════════════════════════════════════════════════════

    private fun renderCrashUI(crashLog: String) {
        val cat = detectCrashCategory(crashLog)
        val meta = catMeta(cat)
        val excName = extractExceptionName(crashLog)

        fun dp(v: Int) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics).toInt()
        val d4 = dp(4); val d6 = dp(6); val d8 = dp(8); val d10 = dp(10); val d12 = dp(12)
        val d16 = dp(16); val d18 = dp(18); val d20 = dp(20); val d24 = dp(24)
        val d28 = dp(28); val d32 = dp(32); val d36 = dp(36); val d40 = dp(40)
        val d44 = dp(44); val d48 = dp(48); val d52 = dp(52); val d56 = dp(56); val d64 = dp(64)

        val navBarH = navBarHeight()

        // ── Root scroll container ──
        val scroll = ScrollView(this).apply {
            setBackgroundColor(Pal.bg)
            setPadding(0, statusBarH(), 0, 0)
            isVerticalScrollBarEnabled = false
            isFillViewport = true
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(d20, d24, d20, d32 + navBarH)
        }

        // ═══════════════════════════════════════════════════════════════
        //  HERO SECTION — clean, centered, no card box
        // ═══════════════════════════════════════════════════════════════
        val heroSection = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }

        // Warning icon — soft circular background, no shadow rings
        val iconContainer = LinearLayout(this).apply { gravity = Gravity.CENTER }
        iconContainer.addView(textView("\u26A0\uFE0F", 36f, Pal.onSurface, center = true).apply {
            layoutParams = LinearLayout.LayoutParams(d64, d64)
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL; setColor(Pal.surfaceAlt)
            }
        })
        heroSection.addView(iconContainer)
        heroSection.addView(spaceY(d16))

        // App name
        heroSection.addView(textView("FieldMind", 28f, Pal.onSurface, bold = true, center = true))
        heroSection.addView(spaceY(d6))

        // Crash label
        heroSection.addView(textView("Stopped unexpectedly", 16f, Pal.onSurfaceVar, center = true))
        heroSection.addView(spaceY(d6))

        // Category label — soft pill chip
        val catLabel = when (cat) {
            CrashCategory.LOCK_PIN -> "Security"
            CrashCategory.DATABASE -> "Storage"
            CrashCategory.SETTINGS -> "Settings"
            CrashCategory.NETWORK -> "Network"
            CrashCategory.COMPOSE -> "UI"
            CrashCategory.OUT_OF_MEMORY -> "Memory"
            CrashCategory.UNKNOWN -> "Unknown"
        }
        heroSection.addView(chipView(catLabel, Pal.surfaceAlt, Pal.onSurfaceVar))

        root.addView(heroSection)
        root.addView(spaceY(d28))

        // ═══════════════════════════════════════════════════════════════
        //  DETAILS SECTION — single clean surface, no shadow box
        // ═══════════════════════════════════════════════════════════════
        val detailsSection = roundedSurface(Pal.surface, d28).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(d24, d20, d24, d20)
        }

        // Section title
        detailsSection.addView(textView("What happened", 15f, Pal.onSurface, bold = true))
        detailsSection.addView(spaceY(d8))

        // Crash description
        detailsSection.addView(textView(meta.label, 14f, Pal.onSurfaceVar).apply {
            setLineSpacing(4f, 1f)
        })
        detailsSection.addView(spaceY(d12))

        // Exception name chip
        detailsSection.addView(chipView(excName, Pal.surfaceAlt, Pal.primary))
        detailsSection.addView(spaceY(d12))

        // Divider
        detailsSection.addView(dividerView())
        detailsSection.addView(spaceY(d10))

        // Action row: Copy + Share
        val actionRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        actionRow.addView(flatButton("Copy report").apply {
            setOnClickListener { copyCrashLog(crashLog) }
            layoutParams = LinearLayout.LayoutParams(0, d48, 1f).apply { marginEnd = d8 }
        })
        actionRow.addView(flatButton("Share").apply {
            setOnClickListener { shareCrashLog(crashLog) }
            layoutParams = LinearLayout.LayoutParams(0, d48, 1f)
        })
        detailsSection.addView(actionRow)
        detailsSection.addView(spaceY(d10))

        // Expandable full log
        val expanded = booleanArrayOf(false)
        val fullLog = textView(crashLog, 11f, Pal.onSurfaceVar).apply {
            typeface = Typeface.MONOSPACE; setLineSpacing(2f, 1f); setTextIsSelectable(true)
            background = GradientDrawable().apply { setColor(Pal.surfaceAlt); cornerRadius = d12.toFloat() }
            setPadding(d16, d16, d16, d16); visibility = View.GONE
        }
        val toggleBtn = textView("Show full report", 13f, Pal.primary, bold = true, center = true).apply {
            setPadding(d12, d12, d12, d12)
            background = rippleBg(d16, Pal.primary)
            isClickable = true; isFocusable = true
            setOnClickListener {
                expanded[0] = !expanded[0]
                text = if (expanded[0]) "Hide full report" else "Show full report"
                fullLog.visibility = if (expanded[0]) View.VISIBLE else View.GONE
            }
        }
        detailsSection.addView(toggleBtn)
        detailsSection.addView(fullLog)

        root.addView(detailsSection)
        root.addView(spaceY(d16))

        // ═══════════════════════════════════════════════════════════════
        //  RECOVERY SECTION
        // ═══════════════════════════════════════════════════════════════
        val recoverySection = roundedSurface(Pal.surface, d28).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(d24, d20, d24, d20)
        }

        recoverySection.addView(textView("Recovery", 15f, Pal.onSurface, bold = true))
        recoverySection.addView(spaceY(d6))
        recoverySection.addView(textView(meta.recoveryDesc, 14f, Pal.onSurfaceVar).apply {
            setLineSpacing(4f, 1f); setPadding(0, 0, 0, d10)
        })
        recoverySection.addView(spaceY(d8))

        // Primary action
        when (cat) {
            CrashCategory.LOCK_PIN ->
                recoverySection.addView(pillButton("Disable lock & PIN, restart", Pal.error, Pal.onPrimary).apply {
                    layoutParams = btnLp(d52, d10)
                    setOnClickListener { showLockDisableConfirmDialog() }
                })
            CrashCategory.SETTINGS ->
                recoverySection.addView(pillButton("Reset settings & restart", Pal.error, Pal.onPrimary).apply {
                    layoutParams = btnLp(d52, d10)
                    setOnClickListener { showSettingsResetConfirmDialog() }
                })
            CrashCategory.DATABASE ->
                recoverySection.addView(pillButton("Clear cache & restart", Pal.error, Pal.onPrimary).apply {
                    layoutParams = btnLp(d52, d10)
                    setOnClickListener { runCatching { cacheDir.deleteRecursively() }; restartApp() }
                })
            else ->
                recoverySection.addView(pillButton("Restart app", Pal.primary, Pal.onPrimary).apply {
                    layoutParams = btnLp(d52, d10)
                    setOnClickListener { restartApp() }
                })
        }

        // Secondary actions
        if (cat != CrashCategory.LOCK_PIN) {
            recoverySection.addView(outlineButton("Disable lock & PIN, restart").apply {
                layoutParams = btnLp(d48, d8)
                setOnClickListener { showLockDisableConfirmDialog() }
            })
        }
        if (cat == CrashCategory.SETTINGS || cat == CrashCategory.DATABASE || cat == CrashCategory.LOCK_PIN) {
            recoverySection.addView(textButton("Just restart").apply {
                layoutParams = btnLp(d44, d6)
                setOnClickListener { restartApp() }
            })
        }

        root.addView(recoverySection)
        root.addView(spaceY(d28))

        // ═══════════════════════════════════════════════════════════════
        //  FOOTER
        // ═══════════════════════════════════════════════════════════════
        root.addView(textView(
            "Your research data is safe — observations, notes, and projects are fully preserved.",
            12f, Pal.onSurfaceVar, center = true
        ).apply { alpha = 0.65f; setPadding(d24, 0, d24, d6) })

        val ver = runCatching { packageManager.getPackageInfo(packageName, 0).versionName }.getOrDefault("")
        root.addView(textView("FieldMind · v$ver", 11f, Pal.onSurfaceVar, center = true).apply {
            alpha = 0.4f; setPadding(0, 0, 0, d16)
        })

        scroll.addView(root)
        setContentView(scroll)

        // Entrance animation
        root.alpha = 0f
        root.translationY = dp(20).toFloat()
        root.animate()
            .alpha(1f).translationY(0f)
            .setDuration(300).setStartDelay(50)
            .setInterpolator(DecelerateInterpolator(2f))
            .start()
    }

    // ════════════════════════════════════════════════════════════════════
    //  View Builders — clean, minimal, no box shadows
    // ════════════════════════════════════════════════════════════════════

    /** Simple rounded surface with solid background — no layered shadows. */
    private fun roundedSurface(bg: Int, radius: Int): LinearLayout =
        LinearLayout(this).apply {
            background = GradientDrawable().apply { setColor(bg); cornerRadius = radius.toFloat() }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

    /** Pill-shaped filled button. */
    private fun pillButton(text: String, bg: Int, textColor: Int): Button {
        val radius = dp(28).toFloat()
        val rrs = RoundRectShape(
            floatArrayOf(radius, radius, radius, radius, radius, radius, radius, radius), null, null
        )
        val normal = GradientDrawable().apply { setColor(bg); cornerRadius = radius }
        val pressed = GradientDrawable().apply { setColor(blendBlack(bg, 0.15f)); cornerRadius = radius }

        return Button(this).apply {
            this.text = text; setTextColor(textColor); textSize = 14f
            typeface = Typeface.DEFAULT_BOLD; setAllCaps(false); gravity = Gravity.CENTER
            minHeight = 0; minimumHeight = 0
            setPadding(dp(24), dp(14), dp(24), dp(14))
            background = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                RippleDrawable(
                    ColorStateList.valueOf(AndroidColor.WHITE),
                    stateListDrawable(normal, pressed),
                    ShapeDrawable(rrs)
                )
            } else stateListDrawable(normal, pressed)
        }
    }

    /** Outlined pill button. */
    private fun outlineButton(text: String): Button {
        val radius = dp(28).toFloat()
        val rrs = RoundRectShape(
            floatArrayOf(radius, radius, radius, radius, radius, radius, radius, radius), null, null
        )
        val strokeW = dp(1)
        val normal = GradientDrawable().apply {
            setColor(AndroidColor.TRANSPARENT); cornerRadius = radius; setStroke(strokeW, Pal.divider)
        }
        val pressed = GradientDrawable().apply {
            setColor(0x08000000); cornerRadius = radius; setStroke(strokeW, Pal.divider)
        }

        return Button(this).apply {
            this.text = text; setTextColor(Pal.onSurface); textSize = 14f
            typeface = Typeface.DEFAULT_BOLD; setAllCaps(false); gravity = Gravity.CENTER
            minHeight = 0; minimumHeight = 0
            setPadding(dp(24), dp(12), dp(24), dp(12))
            background = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                RippleDrawable(
                    ColorStateList.valueOf(0x20000000.toInt()),
                    stateListDrawable(normal, pressed),
                    ShapeDrawable(rrs)
                )
            } else stateListDrawable(normal, pressed)
        }
    }

    /** Text-only button. */
    private fun textButton(text: String): Button {
        val radius = dp(20).toFloat()
        val rrs = RoundRectShape(
            floatArrayOf(radius, radius, radius, radius, radius, radius, radius, radius), null, null
        )
        val normal = GradientDrawable().apply { setColor(AndroidColor.TRANSPARENT) }
        val pressed = GradientDrawable().apply { setColor(0x08000000) }

        return Button(this).apply {
            this.text = text; setTextColor(Pal.primary); textSize = 13f
            typeface = Typeface.DEFAULT_BOLD; setAllCaps(false); gravity = Gravity.CENTER
            minHeight = 0; minimumHeight = 0
            setPadding(dp(16), dp(8), dp(16), dp(8))
            background = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                RippleDrawable(
                    ColorStateList.valueOf(Pal.primary and 0x00FFFFFF or 0x1A000000),
                    stateListDrawable(normal, pressed),
                    ShapeDrawable(rrs)
                )
            } else stateListDrawable(normal, pressed)
        }
    }

    /** Flat action button (outlined style, compact). */
    private fun flatButton(text: String): Button {
        val radius = dp(16).toFloat()
        val rrs = RoundRectShape(
            floatArrayOf(radius, radius, radius, radius, radius, radius, radius, radius), null, null
        )
        val normal = GradientDrawable().apply {
            setColor(Pal.surfaceAlt); cornerRadius = radius
        }
        val pressed = GradientDrawable().apply {
            setColor(Pal.divider); cornerRadius = radius
        }

        return Button(this).apply {
            this.text = text; setTextColor(Pal.onSurface); textSize = 13f
            typeface = Typeface.DEFAULT_BOLD; setAllCaps(false); gravity = Gravity.CENTER
            minHeight = 0; minimumHeight = 0
            setPadding(dp(16), dp(10), dp(16), dp(10))
            background = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                RippleDrawable(
                    ColorStateList.valueOf(Pal.primary and 0x00FFFFFF or 0x20000000),
                    stateListDrawable(normal, pressed),
                    ShapeDrawable(rrs)
                )
            } else stateListDrawable(normal, pressed)
        }
    }

    /** Chip / badge label. */
    private fun chipView(text: String, bg: Int, textColor: Int): TextView =
        textView(text, 12f, textColor, bold = true, center = true).apply {
            setPadding(dp(16), dp(6), dp(16), dp(6))
            background = GradientDrawable().apply { setColor(bg); cornerRadius = dp(20).toFloat() }
        }

    /** Thin divider. */
    private fun dividerView(): View = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1))
        setBackgroundColor(Pal.divider)
    }

    /** Ripple background for text-like buttons. */
    private fun rippleBg(radius: Int, color: Int): GradientDrawable =
        GradientDrawable().apply { setColor(color and 0x00FFFFFF or 0x08000000); cornerRadius = radius.toFloat() }

    private fun textView(
        text: String, size: Float, color: Int,
        bold: Boolean = false, center: Boolean = false
    ): TextView = TextView(this).apply {
        this.text = text; setTextColor(color); textSize = size
        if (bold) typeface = Typeface.DEFAULT_BOLD
        if (center) gravity = Gravity.CENTER
    }

    private fun spaceY(h: Int): View = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h)
    }

    private fun btnLp(btnH: Int, marginB: Int = 0) =
        LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, btnH).apply { bottomMargin = marginB }

    private fun stateListDrawable(
        normal: android.graphics.drawable.Drawable,
        pressed: android.graphics.drawable.Drawable
    ): StateListDrawable = StateListDrawable().apply {
        addState(intArrayOf(android.R.attr.state_pressed, android.R.attr.state_enabled), pressed)
        addState(intArrayOf(android.R.attr.state_enabled), normal)
    }

    private fun blendBlack(color: Int, ratio: Float): Int {
        val r = ((color shr 16) and 0xFF) * (1f - ratio)
        val g = ((color shr 8) and 0xFF) * (1f - ratio)
        val b = (color and 0xFF) * (1f - ratio)
        return (0xFF shl 24) or (r.toInt() shl 16) or (g.toInt() shl 8) or b.toInt()
    }

    private fun dp(v: Int) = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics
    ).toInt()

    private fun statusBarH(): Int {
        val id = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (id > 0) resources.getDimensionPixelSize(id) else dp(24)
    }

    private fun navBarHeight(): Int {
        val id = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (id > 0) resources.getDimensionPixelSize(id) else dp(48)
    }

    // ════════════════════════════════════════════════════════════════════
    //  Actions & Dialogs
    // ════════════════════════════════════════════════════════════════════

    private fun showLockDisableConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("Disable lock & PIN?")
            .setMessage("This will disable the privacy lock, app PIN, decoy PIN, panic-lock, biometric settings, and export password protection.\n\nYour research data will NOT be affected. You can re-enable these in Settings.")
            .setPositiveButton("Disable & restart") { _, _ -> disableSecurityAndRestart() }
            .setNegativeButton("Cancel", null).show()
    }

    private fun showSettingsResetConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("Reset settings?")
            .setMessage("This will reset all app preferences to their defaults. Your observations, notes, and research data will NOT be affected.\n\nYou'll need to reconfigure your preferences after the restart.")
            .setPositiveButton("Reset & restart") { _, _ -> resetSettingsAndRestart() }
            .setNegativeButton("Cancel", null).show()
    }

    private fun copyCrashLog(crashLog: String) {
        (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
            .setPrimaryClip(ClipData.newPlainText("FieldMind Crash Report", crashLog))
        Toast.makeText(this, "Crash report copied", Toast.LENGTH_SHORT).show()
    }

    private fun shareCrashLog(crashLog: String) {
        runCatching {
            startActivity(Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "FieldMind Crash Report")
                    putExtra(Intent.EXTRA_TEXT, crashLog)
                },
                "Share crash report"
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }.onFailure { copyCrashLog(crashLog) }
    }

    private fun disableSecurityAndRestart() {
        runCatching {
            getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                .putBoolean(KEY_PRIVACY_LOCK, false).putBoolean(KEY_APP_PIN_ENABLED, false)
                .putBoolean(KEY_DECOY_PIN_ENABLED, false).putBoolean(KEY_FAILED_UNLOCK_PANIC_LOCK, false)
                .putBoolean(KEY_FAILED_UNLOCK_BIOMETRICS, false).putBoolean(KEY_EXPORT_PASSWORD_PROTECTION, false)
                .putString(KEY_APP_PIN_HASH, "").putString(KEY_DECOY_PIN_HASH, "")
                .putString(KEY_DECOY_PIN_LABEL, "").putString(KEY_EXPORT_PASSWORD_HASH, "")
                .commit()
        }.onFailure { Log.e(TAG, "Failed to disable security", it) }
        Toast.makeText(this, "Lock & PIN disabled. Restarting…", Toast.LENGTH_SHORT).show()
        restartApp()
    }

    private fun resetSettingsAndRestart() {
        runCatching {
            getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().commit()
            getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                .putBoolean(KEY_PRIVACY_LOCK, false).putBoolean(KEY_APP_PIN_ENABLED, false).commit()
        }.onFailure { Log.e(TAG, "Failed to reset settings", it) }
        Toast.makeText(this, "Settings reset. Restarting…", Toast.LENGTH_SHORT).show()
        restartApp()
    }

    private fun restartApp() {
        packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }?.let { startActivity(it) }
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
            context.startActivity(Intent(context, FieldMindCrashActivity::class.java).apply {
                putExtra(EXTRA_CRASH_LOG, crashLog)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            })
        }
    }
}
