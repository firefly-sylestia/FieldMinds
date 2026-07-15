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
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
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
 * Premium Material Design 3 crash recovery UI.
 *
 * Runs in :crash_process using plain Android Views — no Compose, no theme resources.
 *
 * Design follows M3 token roles with proper elevation (multi-layer shadows),
 * ripple/pressed-state feedback on all interactive elements, surface-color
 * hierarchy, and subtle entrance animations.
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
        val surface          = 0xFFFEFBFF.toInt()
        val surfaceContainer = 0xFFF3EDF7.toInt()
        val surfaceVariant   = 0xFFE7E0EC.toInt()
        val onSurface        = 0xFF1C1B1F.toInt()
        val onSurfaceVariant = 0xFF49454F.toInt()
        val outline          = 0xFF79747E.toInt()
        val outlineVariant   = 0xFFCAC4D0.toInt()
        val error            = 0xFFBA1A1A.toInt()
        val onError          = 0xFFFFFFFF.toInt()
        val errorContainer   = 0xFFFFDAD6.toInt()
        val onErrorContainer = 0xFF410002.toInt()
    }

    // ════════════════════════════════════════════════════════════════════
    //  Crash Category Detection + Metadata
    // ════════════════════════════════════════════════════════════════════

    private enum class CrashCategory {
        LOCK_PIN, DATABASE, SETTINGS, NETWORK, COMPOSE, OUT_OF_MEMORY, UNKNOWN
    }

    private data class CatMeta(
        val label: String, val recoveryDesc: String,
        val chipText: String, val chipBg: Int, val chipTextColor: Int,
        val isDestructive: Boolean
    )

    private fun catMeta(cat: CrashCategory): CatMeta = when (cat) {
        CrashCategory.LOCK_PIN -> CatMeta(
            "The privacy lock or PIN system is causing this crash.",
            "Disabling the privacy lock and PIN should let the app start normally. You can re-enable them later in Settings.",
            "Security", M3.errorContainer, M3.onErrorContainer, true
        )
        CrashCategory.DATABASE -> CatMeta(
            "The local database or storage appears to be corrupted.",
            "Try clearing the app cache. Your research data (observations, notes) is stored separately and will not be affected.",
            "Storage", M3.errorContainer, M3.onErrorContainer, true
        )
        CrashCategory.SETTINGS -> CatMeta(
            "Corrupted app preferences are preventing FieldMind from starting.",
            "Resetting all preferences to defaults should resolve this. Your field data is stored separately and preserved.",
            "Settings", M3.errorContainer, M3.onErrorContainer, true
        )
        CrashCategory.NETWORK -> CatMeta(
            "A network or connectivity error occurred.",
            "This is usually transient. Restarting should resolve it. If the issue persists, check your connection or API credentials.",
            "Network", M3.primaryContainer, M3.onPrimaryContainer, false
        )
        CrashCategory.COMPOSE -> CatMeta(
            "The user interface encountered a rendering error.",
            "This is usually a one-time glitch. A restart should resolve it. Try disabling custom animations in Settings if it recurs.",
            "UI", M3.primaryContainer, M3.onPrimaryContainer, false
        )
        CrashCategory.OUT_OF_MEMORY -> CatMeta(
            "The device ran out of memory.",
            "Close other apps and restart FieldMind. On older devices, reduce image quality in Capture Settings.",
            "Memory", M3.errorContainer, M3.onErrorContainer, false
        )
        CrashCategory.UNKNOWN -> CatMeta(
            "The cause could not be automatically identified.",
            "If FieldMind keeps crashing, the most common fixes are disabling the privacy lock/PIN or resetting app preferences.",
            "Unknown", M3.surfaceVariant, M3.onSurfaceVariant, false
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

        fun dp(v: Int) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics).toInt()
        val d2 = dp(2); val d4 = dp(4); val d6 = dp(6); val d8 = dp(8); val d10 = dp(10)
        val d12 = dp(12); val d16 = dp(16); val d20 = dp(20); val d24 = dp(24)
        val d28 = dp(28); val d32 = dp(32); val d36 = dp(36); val d40 = dp(40)
        val d48 = dp(48); val d52 = dp(52); val d56 = dp(56); val d64 = dp(64); val d72 = dp(72)

        val navBarH = navBarHeight()

        val scroll = ScrollView(this).apply {
            setBackgroundColor(M3.surfaceContainer)
            setPadding(0, statusBarH(), 0, 0)
            isVerticalScrollBarEnabled = false
            isFillViewport = true
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(d16, d20, d16, d24 + navBarH)
        }

        // ═══════════════════════════════════════════════════════════════
        //  1. HERO CARD
        // ═══════════════════════════════════════════════════════════════
        val heroCard = m3ElevatedCard(M3.surface, d28, elevationDp = 6).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(d24, d28, d24, d24)
        }
        // Warning icon with gradient ring
        val iconContainer = LinearLayout(this).apply { gravity = Gravity.CENTER }
        val iconSize = d72
        iconContainer.addView(TextView(this).apply {
            text = "\u26A0\uFE0F"
            textSize = 32f; gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(iconSize, iconSize)
            background = iconCircleBg(M3.errorContainer)
        })
        heroCard.addView(iconContainer)
        heroCard.addView(spaceY(d16))

        // Title
        heroCard.addView(tv("FieldMind stopped unexpectedly", 22f, M3.onSurface, bold = true, center = true))
        heroCard.addView(spaceY(d8))

        // Subtitle
        heroCard.addView(tv(meta.label, 14f, M3.onSurfaceVariant, center = true).apply {
            setPadding(d16, 0, d16, 0); setLineSpacing(4f, 1f)
        })
        heroCard.addView(spaceY(d12))

        // Category chip
        val chipRow = LinearLayout(this).apply { gravity = Gravity.CENTER }
        chipRow.addView(m3Chip(meta.chipText, meta.chipBg, meta.chipTextColor))
        heroCard.addView(chipRow)

        root.addView(heroCard)
        root.addView(spaceY(d16))

        // ═══════════════════════════════════════════════════════════════
        //  2. DETAILS CARD
        // ═══════════════════════════════════════════════════════════════
        val expanded = booleanArrayOf(false)
        val detailsCard = m3ElevatedCard(M3.surface, d28, elevationDp = 3).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(d20, d20, d20, d20)
        }

        // Header row with Copy button
        val hdr = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        hdr.addView(tv("Crash details", 16f, M3.onSurface, bold = true).apply {
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        })
        hdr.addView(m3RippleTextBtn("Copy", M3.primary).apply {
            setOnClickListener { copyCrashLog(crashLog) }
        })
        detailsCard.addView(hdr)
        detailsCard.addView(spaceY(d12))

        // Exception chip
        detailsCard.addView(m3Chip(excName, M3.surfaceVariant, M3.onSurfaceVariant))
        detailsCard.addView(spaceY(d8))

        // Exception message
        detailsCard.addView(tv(excMsg.ifBlank { "No additional details" }, 13f, M3.onSurfaceVariant).apply {
            maxLines = 2; setLineSpacing(3f, 1f)
        })
        detailsCard.addView(spaceY(d8))

        // Divider + toggle
        detailsCard.addView(m3Divider())
        detailsCard.addView(spaceY(d6))

        val toggle = m3RippleTextBtn("Show full report", M3.primary)
        val fullLog = tv(crashLog, 11f, M3.onSurface).apply {
            typeface = Typeface.MONOSPACE; setLineSpacing(2f, 1f); setTextIsSelectable(true)
            background = GradientDrawable().apply { setColor(M3.surfaceVariant); cornerRadius = d12.toFloat() }
            setPadding(d16, d16, d16, d16); visibility = View.GONE
        }
        toggle.setOnClickListener {
            expanded[0] = !expanded[0]
            toggle.text = if (expanded[0]) "Hide full report" else "Show full report"
            fullLog.visibility = if (expanded[0]) View.VISIBLE else View.GONE
        }
        val tglRow = LinearLayout(this).apply { gravity = Gravity.CENTER }
        tglRow.addView(toggle)
        detailsCard.addView(tglRow)
        detailsCard.addView(fullLog)
        detailsCard.addView(spaceY(d12))

        // Share button
        detailsCard.addView(m3OutlinedButton("Share report").apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, d48)
            setOnClickListener { shareCrashLog(crashLog) }
        })

        root.addView(detailsCard)
        root.addView(spaceY(d16))

        // ═══════════════════════════════════════════════════════════════
        //  3. RECOVERY CARD
        // ═══════════════════════════════════════════════════════════════
        val recoveryCard = m3ElevatedCard(M3.surface, d28, elevationDp = 3).apply {
            orientation = LinearLayout.VERTICAL; setPadding(d20, d20, d20, d20)
        }

        recoveryCard.addView(tv("Recovery options", 16f, M3.onSurface, bold = true))
        recoveryCard.addView(spaceY(d8))
        recoveryCard.addView(tv(meta.recoveryDesc, 13f, M3.onSurfaceVariant).apply {
            setLineSpacing(4f, 1f); setPadding(0, 0, 0, d16)
        })

        // Primary action
        when (cat) {
            CrashCategory.LOCK_PIN ->
                recoveryCard.addView(m3FilledButton("Disable lock & PIN, then restart", M3.error, M3.onError).apply {
                    layoutParams = btnLp(d56, d10)
                    setOnClickListener { showLockDisableConfirmDialog() }
                })
            CrashCategory.SETTINGS ->
                recoveryCard.addView(m3FilledButton("Reset settings & restart", M3.error, M3.onError).apply {
                    layoutParams = btnLp(d56, d10)
                    setOnClickListener { showSettingsResetConfirmDialog() }
                })
            CrashCategory.DATABASE ->
                recoveryCard.addView(m3FilledButton("Clear cache & restart", M3.error, M3.onError).apply {
                    layoutParams = btnLp(d56, d10)
                    setOnClickListener { runCatching { cacheDir.deleteRecursively() }; restartApp() }
                })
            else ->
                recoveryCard.addView(m3FilledButton("Restart").apply {
                    layoutParams = btnLp(d56, d10)
                    setOnClickListener { restartApp() }
                })
        }

        // Secondary: disable lock
        if (cat != CrashCategory.LOCK_PIN) {
            recoveryCard.addView(m3OutlinedButton("Disable lock & PIN, restart").apply {
                layoutParams = btnLp(d52, d8)
                setOnClickListener { showLockDisableConfirmDialog() }
            })
        }

        // Tertiary: plain restart
        if (cat == CrashCategory.SETTINGS || cat == CrashCategory.DATABASE || cat == CrashCategory.LOCK_PIN) {
            recoveryCard.addView(m3TextButton("Restart").apply {
                layoutParams = btnLp(d48)
                setOnClickListener { restartApp() }
            })
        }

        root.addView(recoveryCard)
        root.addView(spaceY(d24))

        // ═══════════════════════════════════════════════════════════════
        //  4. FOOTER
        // ═══════════════════════════════════════════════════════════════
        root.addView(tv("Your research data is safe \u2014 observations, notes, and projects are fully preserved.", 12f, M3.onSurfaceVariant, center = true).apply {
            alpha = 0.65f; setPadding(d24, 0, d24, d6)
        })
        val ver = runCatching { packageManager.getPackageInfo(packageName, 0).versionName }.getOrDefault("")
        root.addView(tv("FieldMind \u2022 v$ver", 11f, M3.onSurfaceVariant, center = true).apply {
            alpha = 0.45f; setPadding(0, 0, 0, d16)
        })

        scroll.addView(root)
        setContentView(scroll)

        // ── Entrance animations ──
        animateCardEntrance(heroCard, 0)
        animateCardEntrance(detailsCard, 80)
        animateCardEntrance(recoveryCard, 160)
    }

    // ════════════════════════════════════════════════════════════════════
    //  Premium M3 View Builders
    // ════════════════════════════════════════════════════════════════════

    /** Multi-layer elevated card with realistic M3 shadow depth. */
    private fun m3ElevatedCard(bg: Int, radius: Int, elevationDp: Int): LinearLayout {
        val elev = dp(elevationDp)
        val r = radius.toFloat()
        return LinearLayout(this).apply {
            background = LayerDrawable(arrayOf(
                // Layer 0: ambient shadow (soft, wide)
                GradientDrawable().apply {
                    setColor(0x0A000000); cornerRadius = r
                },
                // Layer 1: penumbra shadow
                GradientDrawable().apply {
                    setColor(0x08000000); cornerRadius = r
                },
                // Layer 2: umbra shadow (tight, darkest)
                GradientDrawable().apply {
                    setColor(0x0C000000); cornerRadius = r
                },
                // Layer 3: surface
                GradientDrawable().apply {
                    setColor(bg); cornerRadius = r
                }
            )).apply {
                // ambient shadow: fills full bounds at bottom
                setLayerInset(0, 0, 0, 0, 0)
                // penumbra: slightly inset all around
                setLayerInset(1, 0, elev / 3, 0, elev / 3)
                // umbra: tightest, darkest at bottom
                setLayerInset(2, 0, elev / 2, 0, elev / 2)
                // surface: shifted up to reveal layered shadows below
                setLayerInset(3, 0, 0, 0, elev)
            }
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    /** Filled button with ripple and pressed-state feedback. */
    private fun m3FilledButton(text: String, bg: Int = M3.primary, textColor: Int = M3.onPrimary): Button {
        val radius = dp(28).toFloat()
        val rrs = RoundRectShape(floatArrayOf(radius, radius, radius, radius, radius, radius, radius, radius), null, null)
        val normal = GradientDrawable().apply { setColor(bg); cornerRadius = radius }
        val pressed = GradientDrawable().apply { setColor(blendWithWhite(bg, 0.12f)); cornerRadius = radius }

        return Button(this).apply {
            this.text = text; setTextColor(textColor); textSize = 14f
            typeface = Typeface.DEFAULT_BOLD; setAllCaps(false); gravity = Gravity.CENTER
            minHeight = 0; minimumHeight = 0
            setPadding(dp(24), dp(14), dp(24), dp(14))
            background = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                RippleDrawable(ColorStateList.valueOf(AndroidColor.WHITE), stateList(normal, pressed), ShapeDrawable(rrs))
            } else {
                stateList(normal, pressed)
            }
        }
    }

    /** Outlined button with ripple and pressed-state feedback. */
    private fun m3OutlinedButton(text: String, textColor: Int = M3.primary, borderColor: Int = M3.outline): Button {
        val radius = dp(28).toFloat()
        val rrs = RoundRectShape(floatArrayOf(radius, radius, radius, radius, radius, radius, radius, radius), null, null)
        val strokeW = dp(1)
        val normal = GradientDrawable().apply { setColor(AndroidColor.TRANSPARENT); cornerRadius = radius; setStroke(strokeW, borderColor) }
        val pressed = GradientDrawable().apply { setColor(textColor and 0x00FFFFFF or 0x0A000000); cornerRadius = radius; setStroke(strokeW, borderColor) }

        return Button(this).apply {
            this.text = text; setTextColor(textColor); textSize = 14f
            typeface = Typeface.DEFAULT_BOLD; setAllCaps(false); gravity = Gravity.CENTER
            minHeight = 0; minimumHeight = 0
            setPadding(dp(24), dp(14), dp(24), dp(14))
            background = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                RippleDrawable(ColorStateList.valueOf(textColor and 0x00FFFFFF or 0x20000000), stateList(normal, pressed), ShapeDrawable(rrs))
            } else {
                stateList(normal, pressed)
            }
        }
    }

    /** Text button with ripple and pressed-state feedback. */
    private fun m3TextButton(text: String, color: Int = M3.primary): Button {
        val radius = dp(20).toFloat()
        val rrs = RoundRectShape(floatArrayOf(radius, radius, radius, radius, radius, radius, radius, radius), null, null)
        val normal = ColorDrawable(AndroidColor.TRANSPARENT)
        val pressed = ColorDrawable(color and 0x00FFFFFF or 0x0A000000)

        return Button(this).apply {
            this.text = text; setTextColor(color); textSize = 14f
            typeface = Typeface.DEFAULT_BOLD; setAllCaps(false); gravity = Gravity.CENTER
            minHeight = 0; minimumHeight = 0
            setPadding(dp(16), dp(10), dp(16), dp(10))
            background = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                RippleDrawable(ColorStateList.valueOf(color and 0x00FFFFFF or 0x1A000000), stateList(normal, pressed), ShapeDrawable(rrs))
            } else {
                stateList(normal, pressed)
            }
        }
    }

    /** Compact ripple-bg text button (used for Copy/Show full report). */
    private fun m3RippleTextBtn(text: String, color: Int): TextView {
        val radius = dp(16).toFloat()
        val rrs = RoundRectShape(floatArrayOf(radius, radius, radius, radius, radius, radius, radius, radius), null, null)
        val normal = GradientDrawable().apply { setColor(color and 0x00FFFFFF or 0x0A000000); cornerRadius = radius }
        val pressed = GradientDrawable().apply { setColor(color and 0x00FFFFFF or 0x18000000); cornerRadius = radius }

        return TextView(this).apply {
            this.text = text; setTextColor(color); textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(dp(12), dp(6), dp(12), dp(6))
            isClickable = true; isFocusable = true
            background = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                RippleDrawable(ColorStateList.valueOf(color and 0x00FFFFFF or 0x20000000), stateList(normal, pressed), ShapeDrawable(rrs))
            } else {
                stateList(normal, pressed)
            }
        }
    }

    /** Category chip with rounded background. */
    private fun m3Chip(text: String, bg: Int, textColor: Int): TextView = TextView(this).apply {
        this.text = text; setTextColor(textColor); textSize = 12f
        typeface = Typeface.DEFAULT_BOLD; setPadding(dp(16), dp(6), dp(16), dp(6))
        background = GradientDrawable().apply { setColor(bg); cornerRadius = dp(20).toFloat() }
    }

    /** Warning icon circle with subtle inner ring. */
    private fun iconCircleBg(fillColor: Int): GradientDrawable = GradientDrawable().apply {
        shape = GradientDrawable.OVAL; setColor(fillColor)
        setStroke(dp(2), fillColor and 0x00FFFFFF or 0x30000000)
    }

    /** Thin M3 divider. */
    private fun m3Divider(): View = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)).apply {
            topMargin = dp(4); bottomMargin = dp(4)
        }
        setBackgroundColor(M3.outlineVariant)
    }

    private fun tv(text: String, size: Float, color: Int, bold: Boolean = false, center: Boolean = false): TextView =
        TextView(this).apply {
            this.text = text; setTextColor(color); textSize = size
            if (bold) typeface = Typeface.DEFAULT_BOLD
            if (center) gravity = Gravity.CENTER
        }

    private fun spaceY(h: Int): View = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h)
    }

    private fun btnLp(btnH: Int, marginB: Int = 0) =
        LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, btnH).apply { bottomMargin = marginB }

    /** StateListDrawable with pressed/unpressed states. */
    private fun stateList(normal: android.graphics.drawable.Drawable, pressed: android.graphics.drawable.Drawable): StateListDrawable =
        StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_pressed, android.R.attr.state_enabled), pressed)
            addState(intArrayOf(android.R.attr.state_enabled), normal)
        }

    /** Blend a color with white at a given ratio (0–1). */
    private fun blendWithWhite(color: Int, ratio: Float): Int {
        val r = ((color shr 16) and 0xFF) + ((255 - ((color shr 16) and 0xFF)) * ratio).toInt()
        val g = ((color shr 8) and 0xFF) + ((255 - ((color shr 8) and 0xFF)) * ratio).toInt()
        val b = (color and 0xFF) + ((255 - (color and 0xFF)) * ratio).toInt()
        return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
    }

    /** Animate a card view entrance: fade up from below. */
    private fun animateCardEntrance(view: View, delayMs: Long) {
        view.alpha = 0f; view.translationY = dp(24).toFloat()
        view.animate()
            .alpha(1f).translationY(0f)
            .setDuration(350).setStartDelay(delayMs)
            .setInterpolator(DecelerateInterpolator(2f))
            .start()
    }

    private fun dp(v: Int) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics).toInt()
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
            .setMessage("This will disable the privacy lock, app PIN, decoy PIN, panic-lock, biometric settings, and export password protection.\n\nYour research data will NOT be affected. You can re-enable these in Settings after the app restarts.")
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
                Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_SUBJECT, "FieldMind Crash Report"); putExtra(Intent.EXTRA_TEXT, crashLog) },
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
        Toast.makeText(this, "Lock & PIN disabled. Restarting\u2026", Toast.LENGTH_SHORT).show()
        restartApp()
    }

    private fun resetSettingsAndRestart() {
        runCatching {
            getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().commit()
            getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                .putBoolean(KEY_PRIVACY_LOCK, false).putBoolean(KEY_APP_PIN_ENABLED, false).commit()
        }.onFailure { Log.e(TAG, "Failed to reset settings", it) }
        Toast.makeText(this, "Settings reset. Restarting\u2026", Toast.LENGTH_SHORT).show()
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
