package fieldmind.research.app.activities

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
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
 * Premium crash recovery UI with soft gradients, layered depth, and refined typography.
 *
 * Runs in :crash_process using plain Android Views — no Compose, no theme attributes,
 * so it cannot crash from resource-loading failures or corrupted themes.
 *
 * Visual design:
 *  • Soft sage-to-cream gradient background
 *  • Layered header card with subtle shadow
 *  • Compact crash detail card with colored severity pill
 *  • Expandable full report with monospace styling
 *  • Targeted recovery actions based on auto-detected crash category
 */
class FieldMindCrashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val crashLog = intent.getStringExtra(EXTRA_CRASH_LOG) ?: "No crash log available."
        renderCrashUI(crashLog)
    }

    // ════════════════════════════════════════════════════════════════════
    //  Premium Crash UI
    // ════════════════════════════════════════════════════════════════════

    private fun renderCrashUI(crashLog: String) {
        val crashCategory = detectCrashCategory(crashLog)

        fun dp(v: Int) = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics
        ).toInt()

        // ── Density tokens ──
        val d2 = dp(2);  val d4 = dp(4);  val d6 = dp(6);  val d8 = dp(8)
        val d10 = dp(10); val d12 = dp(12); val d14 = dp(14); val d16 = dp(16)
        val d20 = dp(20); val d24 = dp(24); val d28 = dp(28); val d32 = dp(32)
        val d36 = dp(36); val d40 = dp(40); val d44 = dp(44); val d48 = dp(48)
        val d56 = dp(56); val d64 = dp(64); val d52 = dp(52)

        // ── Premium color palette — soft, warm, nature-inspired ──
        val bgTop     = 0xFFEDF4ED.toInt()  // misty sage
        val bgBottom  = 0xFFF8F4EF.toInt()  // warm cream
        val surface   = 0xFFFFFFFF.toInt()  // card white
        val surfaceAlt = 0xFFF7FAF5.toInt() // soft green-white
        val textPrimary   = 0xFF1B2E1B.toInt()  // deep forest
        val textSecondary = 0xFF5C6B5C.toInt()  // muted sage
        val textTertiary  = 0xFF8A988A.toInt()  // soft grey-green
        val accent     = 0xFF2E7D32.toInt() // forest green
        val accentSoft = 0xFFD7E8D8.toInt() // misty green
        val error      = 0xFFC62828.toInt() // rich red
        val errorSoft  = 0xFFFFEBEE.toInt() // blush pink
        val warning    = 0xFFE65100.toInt() // warm amber
        val warningSoft = 0xFFFFF3E0.toInt() // soft peach
        val info       = 0xFF1565C0.toInt() // deep blue
        val infoSoft   = 0xFFE3F2FD.toInt() // ice blue
        val outline    = 0xFFC2C9C2.toInt() // soft grey-green border
        val divider    = 0xFFE8ECE8.toInt() // faint divider
        val white      = 0xFFFFFFFF.toInt()

        // ── Category-derived colors ──
        val catColors = categoryColors(crashCategory, accent, accentSoft, error, errorSoft,
            warning, warningSoft, info, infoSoft, textSecondary)

        // ── Root scroll view with gradient background ──
        val scrollView = ScrollView(this).apply {
            setBackgroundColor(bgBottom)
            setPadding(0, statusBarHeight(), 0, 0)
            isVerticalScrollBarEnabled = false
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(d20, d24, d20, d32)
        }

        // ═══════════════════════════════════════════════════════════════
        //  1. HERO HEADER — premium layered card
        // ═══════════════════════════════════════════════════════════════
        root.addView(premiumCard(
            bgColor = surface,
            cornerRadius = d36,
            shadowColor = outline,
            shadowAlpha = 0x30
        ).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(d24, d28, d24, d24)
        }.also { card ->
            // ── Icon circle with soft gradient ring ──
            val iconContainer = LinearLayout(this@FieldMindCrashActivity).apply {
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            val iconCircle = TextView(this@FieldMindCrashActivity).apply {
                text = "\u26A0"
                textSize = 32f
                gravity = Gravity.CENTER
                setTextColor(catColors.accent)
                val size = LinearLayout.LayoutParams(d64, d64)
                layoutParams = size
                // Soft gradient ring behind the icon
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(catColors.bg and 0x00FFFFFF or 0x40000000)
                    setStroke(d2, catColors.accent and 0x00FFFFFF or 0x18000000)
                }
            }
            iconContainer.addView(iconCircle)
            card.addView(iconContainer)
            card.addView(spacer(d16, 0))

            // ── Title ──
            card.addView(TextView(this@FieldMindCrashActivity).apply {
                text = "FieldMind encountered an issue"
                setTextColor(textPrimary)
                textSize = 20f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                letterSpacing = -0.02f
                setLineSpacing(4f, 1f)
            })

            card.addView(spacer(d10, 0))

            // ── Auto-detected cause subtitle ──
            card.addView(TextView(this@FieldMindCrashActivity).apply {
                text = catColors.label
                setTextColor(textSecondary)
                textSize = 13f
                gravity = Gravity.CENTER
                setLineSpacing(3f, 1f)
                setPadding(d8, 0, d8, 0)
            })

            card.addView(spacer(d6, 0))

            // ── Severity badge ──
            val badgeRow = LinearLayout(this@FieldMindCrashActivity).apply {
                gravity = Gravity.CENTER
            }
            badgeRow.addView(pillBadge(catColors.severityLabel, catColors.accent, catColors.bg, d20))
            card.addView(badgeRow)
        })

        root.addView(spacer(d20, 0))

        // ═══════════════════════════════════════════════════════════════
        //  2. CRASH DETAILS — compact expandable card
        // ═══════════════════════════════════════════════════════════════
        val exceptionName = extractExceptionName(crashLog)
        val exceptionMessage = extractExceptionMessage(crashLog)
        val fullLogVisible = booleanArrayOf(false)

        root.addView(premiumCard(surface, d32, outline, 0x18).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(d20, d20, d20, d20)
        }.also { card ->
            // ── Title row with copy button ──
            val titleRow = LinearLayout(this@FieldMindCrashActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            titleRow.addView(TextView(this@FieldMindCrashActivity).apply {
                text = "\uD83D\uDCDD Crash details"
                setTextColor(textPrimary)
                textSize = 16f
                typeface = Typeface.DEFAULT_BOLD
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            })
            titleRow.addView(pillButton("Copy", accent).apply {
                setOnClickListener { copyCrashLog(crashLog) }
            })
            card.addView(titleRow)
            card.addView(spacer(d12, 0))

            // ── Exception pill ──
            val pillRow = LinearLayout(this@FieldMindCrashActivity).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            pillRow.addView(pillBadge(exceptionName, catColors.pillText, catColors.pillBg, d16))
            card.addView(pillRow)
            card.addView(spacer(d10, 0))

            // ── Exception message ──
            card.addView(TextView(this@FieldMindCrashActivity).apply {
                text = exceptionMessage.ifBlank { "No additional details available" }
                setTextColor(textSecondary)
                textSize = 13f
                maxLines = 2
                setLineSpacing(3f, 1f)
            })

            card.addView(spacer(d12, 0))

            // ── Thin divider ──
            card.addView(thinDivider(divider))

            // ── Expand toggle ──
            val toggleBtn = pillButton(
                if (fullLogVisible[0]) "\u25B2 Hide full report" else "\u25BC Show full report",
                accent
            )
            val fullReportView = TextView(this@FieldMindCrashActivity).apply {
                text = crashLog
                setTextColor(textPrimary)
                textSize = 10f
                typeface = Typeface.MONOSPACE
                setLineSpacing(2f, 1f)
                setTextIsSelectable(true)
                setPadding(d14, d14, d14, d14)
                background = GradientDrawable().apply {
                    setColor(surfaceAlt)
                    cornerRadius = d14.toFloat()
                }
                visibility = View.GONE
            }

            toggleBtn.setOnClickListener {
                fullLogVisible[0] = !fullLogVisible[0]
                toggleBtn.text = if (fullLogVisible[0]) "\u25B2 Hide full report" else "\u25BC Show full report"
                fullReportView.visibility = if (fullLogVisible[0]) View.VISIBLE else View.GONE
            }

            val expandRow = LinearLayout(this@FieldMindCrashActivity).apply {
                gravity = Gravity.CENTER
            }
            expandRow.addView(toggleBtn)
            card.addView(expandRow)
            card.addView(fullReportView)

            card.addView(spacer(d14, 0))

            // ── Share row ──
            card.addView(outlinedButton("Share report", textPrimary, outline, d28).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, d48
                )
                setOnClickListener { shareCrashLog(crashLog) }
            })
        })

        root.addView(spacer(d20, 0))

        // ═══════════════════════════════════════════════════════════════
        //  3. RECOVERY — targeted fix actions
        // ═══════════════════════════════════════════════════════════════
        root.addView(premiumCard(surface, d32, outline, 0x18).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(d20, d20, d20, d20)
        }.also { card ->
            // ── Header ──
            val recHeader = LinearLayout(this@FieldMindCrashActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            recHeader.addView(TextView(this@FieldMindCrashActivity).apply {
                text = "\uD83D\uDD27 Recovery"
                setTextColor(textPrimary)
                textSize = 16f
                typeface = Typeface.DEFAULT_BOLD
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            })
            card.addView(recHeader)
            card.addView(spacer(d10, 0))

            // ── Recovery description ──
            card.addView(TextView(this@FieldMindCrashActivity).apply {
                text = catColors.recoveryDesc
                setTextColor(textSecondary)
                textSize = 13f
                setLineSpacing(4f, 1f)
                setPadding(0, 0, 0, d16)
            })

            // ── Primary action button (filled, accent-colored) ──
            when (crashCategory) {
                CrashCategory.LOCK_PIN -> {
                    card.addView(filledButton("\uD83D\uDD13 Disable lock & PIN, restart",
                        warning, white, d28).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, d56
                        ).apply { bottomMargin = d12 }
                        setOnClickListener { showLockDisableConfirmDialog() }
                    })
                }
                CrashCategory.SETTINGS -> {
                    card.addView(filledButton("\u2699\uFE0F Reset settings & restart",
                        warning, white, d28).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, d56
                        ).apply { bottomMargin = d12 }
                        setOnClickListener { showSettingsResetConfirmDialog() }
                    })
                }
                CrashCategory.DATABASE -> {
                    card.addView(filledButton("\uD83D\uDD04 Clear cache & restart",
                        error, white, d28).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, d56
                        ).apply { bottomMargin = d12 }
                        setOnClickListener {
                            runCatching { cacheDir.deleteRecursively() }
                            restartApp()
                        }
                    })
                }
                else -> {
                    card.addView(filledButton("\uD83D\uDD04 Restart FieldMind",
                        accent, white, d28).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, d56
                        ).apply { bottomMargin = d12 }
                        setOnClickListener { restartApp() }
                    })
                }
            }

            // ── Secondary: disable lock (always except when it's the primary) ──
            if (crashCategory != CrashCategory.LOCK_PIN) {
                card.addView(outlinedButton("Disable lock & PIN, then restart",
                    textPrimary, outline, d28).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, d52
                    ).apply { bottomMargin = d10 }
                    setOnClickListener { showLockDisableConfirmDialog() }
                })
            }

            // ── Tertiary: passive restart (only when primary action is destructive) ──
            if (crashCategory == CrashCategory.SETTINGS || crashCategory == CrashCategory.DATABASE ||
                crashCategory == CrashCategory.LOCK_PIN) {
                card.addView(outlinedButton("Restart without changes",
                    textSecondary, outline, d28).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, d48
                    )
                    setOnClickListener { restartApp() }
                })
            }
        })

        root.addView(spacer(d24, 0))

        // ═══════════════════════════════════════════════════════════════
        //  4. FOOTER — reassurance
        // ═══════════════════════════════════════════════════════════════
        root.addView(TextView(this@FieldMindCrashActivity).apply {
            text = "\u2728 Your research data is safe and untouched.\n" +
                "Observations, notes, and projects are fully preserved."
            setTextColor(textTertiary)
            textSize = 12f
            gravity = Gravity.CENTER
            setLineSpacing(4f, 1f)
            setPadding(d16, 0, d16, d8)
        })
        root.addView(TextView(this@FieldMindCrashActivity).apply {
            text = "FieldMind \u2022 v${runCatching { packageManager.getPackageInfo(packageName, 0).versionName }.getOrDefault("")}"
            setTextColor(textTertiary and 0x00FFFFFF or 0x60000000)
            textSize = 11f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, d20)
        })

        scrollView.addView(root)
        setContentView(scrollView)
    }

    // ════════════════════════════════════════════════════════════════════
    //  Crash Category Detection + Metadata
    // ════════════════════════════════════════════════════════════════════

    private enum class CrashCategory {
        LOCK_PIN, DATABASE, SETTINGS, NETWORK, COMPOSE, OUT_OF_MEMORY, UNKNOWN
    }

    private data class CategoryColors(
        val label: String,
        val severityLabel: String,
        val recoveryDesc: String,
        val bg: Int,
        val accent: Int,
        val pillBg: Int,
        val pillText: Int
    )

    private fun categoryColors(
        category: CrashCategory, accent: Int, accentSoft: Int, error: Int, errorSoft: Int,
        warning: Int, warningSoft: Int, info: Int, infoSoft: Int, textSecondary: Int
    ): CategoryColors = when (category) {
        CrashCategory.LOCK_PIN -> CategoryColors(
            label = "This crash appears related to the privacy lock or PIN system.",
            severityLabel = "Security",
            recoveryDesc = "The lock/PIN system appears to be causing this crash. " +
                "Disabling privacy lock and PIN will let the app start normally. " +
                "You can re-enable them in Settings afterwards.",
            bg = warningSoft, accent = warning, pillBg = warningSoft, pillText = warning
        )
        CrashCategory.DATABASE -> CategoryColors(
            label = "This crash appears related to the local database or storage.",
            severityLabel = "Storage",
            recoveryDesc = "The local database may be corrupted. " +
                "You can try clearing the app cache or restarting. Your research data is safe.",
            bg = errorSoft, accent = error, pillBg = errorSoft, pillText = error
        )
        CrashCategory.SETTINGS -> CategoryColors(
            label = "This crash appears related to corrupted app settings.",
            severityLabel = "Settings",
            recoveryDesc = "Some app settings may be corrupted. " +
                "Resetting to default settings should resolve this without losing your research data.",
            bg = warningSoft, accent = warning, pillBg = warningSoft, pillText = warning
        )
        CrashCategory.NETWORK -> CategoryColors(
            label = "This crash appears related to a network or API call.",
            severityLabel = "Network",
            recoveryDesc = "A network or API error occurred. " +
                "Restart the app — if the issue persists, check your connection or API configuration.",
            bg = infoSoft, accent = info, pillBg = infoSoft, pillText = info
        )
        CrashCategory.COMPOSE -> CategoryColors(
            label = "This crash appears related to the user interface rendering.",
            severityLabel = "UI",
            recoveryDesc = "The user interface encountered a rendering issue. " +
                "This is usually transient — a restart should resolve it.",
            bg = accentSoft, accent = accent, pillBg = accentSoft, pillText = accent
        )
        CrashCategory.OUT_OF_MEMORY -> CategoryColors(
            label = "The app ran out of memory — this may happen on older devices.",
            severityLabel = "Memory",
            recoveryDesc = "The device ran out of memory. " +
                "Close other apps and restart FieldMind. If this recurs, try reducing image sizes in Settings.",
            bg = errorSoft, accent = error, pillBg = errorSoft, pillText = error
        )
        CrashCategory.UNKNOWN -> CategoryColors(
            label = "The cause could not be automatically determined from the crash report.",
            severityLabel = "Unknown",
            recoveryDesc = "If FieldMind keeps crashing, the most common fixes are " +
                "disabling the privacy lock/PIN or resetting app settings.",
            bg = 0xFFF5F5F5.toInt(), accent = textSecondary, pillBg = 0xFFEEEEEE.toInt(), pillText = textSecondary
        )
    }

    private fun detectCrashCategory(crashLog: String): CrashCategory {
        val lowerLog = crashLog.lowercase()
        if (lowerLog.contains("cipher") || lowerLog.contains("keystore") ||
            lowerLog.contains("fingerprint") || lowerLog.contains("biometric") ||
            lowerLog.contains("pin") || lowerLog.contains("lock") ||
            lowerLog.contains("encrypt") || lowerLog.contains("decrypt") ||
            lowerLog.contains("crypto") || lowerLog.contains("security") ||
            (lowerLog.contains("privacy") && lowerLog.contains("lock"))
        ) return CrashCategory.LOCK_PIN
        if (lowerLog.contains("room") || lowerLog.contains("sqlite") ||
            lowerLog.contains("database") || lowerLog.contains("cursor") ||
            lowerLog.contains("migration") || lowerLog.contains("dao") ||
            lowerLog.contains("disk") || lowerLog.contains("storage") ||
            lowerLog.contains("ioexception") || lowerLog.contains("file")
        ) return CrashCategory.DATABASE
        if (lowerLog.contains("sharedpreferences") || lowerLog.contains("settings") ||
            lowerLog.contains("preference") || lowerLog.contains("configuration") ||
            lowerLog.contains("datastore")
        ) return CrashCategory.SETTINGS
        if (lowerLog.contains("http") || lowerLog.contains("socket") ||
            lowerLog.contains("network") || lowerLog.contains("timeout") ||
            lowerLog.contains("connect") || lowerLog.contains("api") ||
            lowerLog.contains("retrofit") || lowerLog.contains("okhttp") ||
            lowerLog.contains("volley") || lowerLog.contains("ssl")
        ) return CrashCategory.NETWORK
        if (lowerLog.contains("compose") || lowerLog.contains("recompose") ||
            lowerLog.contains("modifier") || lowerLog.contains("graphics") ||
            lowerLog.contains("layout") || lowerLog.contains("animation") ||
            lowerLog.contains("drawable") || lowerLog.contains("canvas") ||
            lowerLog.contains("ripple") || lowerLog.contains("surface")
        ) return CrashCategory.COMPOSE
        if (lowerLog.contains("outofmemory") || lowerLog.contains("out of memory"))
            return CrashCategory.OUT_OF_MEMORY
        return CrashCategory.UNKNOWN
    }

    private fun extractExceptionName(crashLog: String): String {
        val lines = crashLog.lines()
        for (line in lines) {
            if (line.contains("Exception:")) {
                val parts = line.substringAfter("Exception:").trim()
                return parts.substringAfterLast(".").ifBlank { parts }
            }
        }
        for (line in lines) {
            val trimmed = line.trim()
            if ((trimmed.contains("Exception") || trimmed.contains("Error")) && trimmed.contains(".")) {
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
            if (line.contains("Message:"))
                return line.substringAfter("Message:").trim().ifBlank { "No details available" }
        }
        for (line in lines) {
            if (line.trimStart().startsWith("Caused by:") && line.contains(":"))
                return line.substringAfter("Caused by:").trim()
        }
        return "No details available"
    }

    // ════════════════════════════════════════════════════════════════════
    //  Premium View Builders
    // ════════════════════════════════════════════════════════════════════

    /** Soft elevated card with subtle shadow ring. */
    private fun premiumCard(
        bgColor: Int, cornerRadius: Int, shadowColor: Int, shadowAlpha: Int
    ): LinearLayout {
        return LinearLayout(this).apply {
            background = LayerDrawable(arrayOf(
                // Shadow layer (offset 2dp down, blur effect via lower opacity edge)
                GradientDrawable().apply {
                    setColor(shadowColor and 0x00FFFFFF or shadowAlpha)
                    this.cornerRadius = cornerRadius.toFloat()
                },
                // Main card layer (offset slightly up from shadow)
                GradientDrawable().apply {
                    setColor(bgColor)
                    this.cornerRadius = cornerRadius.toFloat()
                }
            )).apply {
                setLayerInset(0, 0, 0, 0, 0)       // shadow: full bounds
                setLayerInset(1, 0, 0, 0, dp(2))   // card: 2dp shorter at bottom reveals shadow
            }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    /** Filled primary button with rounded corners. */
    private fun filledButton(
        text: String, bgColor: Int, textColor: Int, cornerRadius: Int
    ): Button {
        return Button(this).apply {
            this.text = text
            setTextColor(textColor)
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setAllCaps(false)
            gravity = Gravity.CENTER
            letterSpacing = 0.01f
            background = GradientDrawable().apply {
                setColor(bgColor)
                this.cornerRadius = cornerRadius.toFloat()
            }
            setPadding(dp(20), dp(4), dp(20), dp(4))
            minHeight = 0
            minimumHeight = 0
        }
    }

    /** Outlined secondary button. */
    private fun outlinedButton(
        text: String, textColor: Int, borderColor: Int, cornerRadius: Int
    ): Button {
        return Button(this).apply {
            this.text = text
            setTextColor(textColor)
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setAllCaps(false)
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                setColor(0x00FFFFFF.toInt())
                this.cornerRadius = cornerRadius.toFloat()
                setStroke(dp(1), borderColor)
            }
            setPadding(dp(20), dp(4), dp(20), dp(4))
            minHeight = 0
            minimumHeight = 0
        }
    }

    /** Compact pill button for inline actions. */
    private fun pillButton(text: String, color: Int): TextView {
        return TextView(this).apply {
            this.text = text
            setTextColor(color)
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(dp(14), dp(7), dp(14), dp(7))
            background = GradientDrawable().apply {
                setColor(color and 0x00FFFFFF or 0x0E000000)
                cornerRadius = dp(18).toFloat()
            }
        }
    }

    /** Small colored badge pill. */
    private fun pillBadge(text: String, textColor: Int, bgColor: Int, cornerRadius: Int): TextView {
        return TextView(this).apply {
            this.text = text
            setTextColor(textColor)
            textSize = 11f
            typeface = Typeface.MONOSPACE
            setPadding(dp(14), dp(5), dp(14), dp(5))
            background = GradientDrawable().apply {
                setColor(bgColor)
                this.cornerRadius = cornerRadius.toFloat()
            }
        }
    }

    private fun thinDivider(color: Int): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(1)
            ).apply {
                topMargin = dp(4)
                bottomMargin = dp(8)
            }
            setBackgroundColor(color)
        }
    }

    private fun spacer(heightDp: Int, widthDp: Int): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                if (widthDp > 0) widthDp else ViewGroup.LayoutParams.MATCH_PARENT,
                if (heightDp > 0) heightDp else ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    private fun dp(value: Int): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics
    ).toInt()

    private fun statusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else dp(24)
    }

    // ════════════════════════════════════════════════════════════════════
    //  Dialogs + Actions
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
