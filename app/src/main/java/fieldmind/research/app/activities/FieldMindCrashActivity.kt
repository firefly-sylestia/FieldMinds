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
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import kotlin.system.exitProcess

/**
 * Full-screen crash recovery UI.
 *
 * Renders when [fieldmind.research.app.util.CrashReporter] catches an uncaught
 * exception. The activity runs in :crash_process and uses plain Android Views
 * (no Compose, no Material3, no painterResource) so it cannot itself crash from
 * resource-loading failures, corrupted themes, or broken Compose state.
 *
 * Layout:
 *  1. Header — large error icon, title, subtitle.
 *  2. Crash log — monospace selectable text area.
 *  3. Action buttons — Copy, Share, Disable lock & PIN + restart, Restart only.
 *  4. Footer note — explains data safety.
 */
class FieldMindCrashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val crashLog = intent.getStringExtra(EXTRA_CRASH_LOG) ?: "No crash log available."
        renderCrashUI(crashLog)
    }

    // ── Primary UI: Native Android Views (no Compose, no painterResource) ──

    private fun renderCrashUI(crashLog: String) {
        val dp16 = dp(16)
        val dp20 = dp(20)
        val dp24 = dp(24)
        val dp12 = dp(12)
        val dp8 = dp(8)
        val dp4 = dp(4)
        val dp40 = dp(40)
        val dp72 = dp(72)
        val dp2 = dp(2)

        // Colors — hardcoded, no theme attributes
        val surfaceColor = 0xFFFAF7F2.toInt()
        val onSurfaceColor = 0xFF1B1B1B.toInt()
        val onSurfaceVariantColor = 0xFF5A5A5A.toInt()
        val primaryColor = 0xFF2E7D32.toInt()
        val primaryContainerColor = 0xFFD7E8D8.toInt()
        val errorColor = 0xFFC62828.toInt()
        val whiteColor = 0xFFFFFFFF.toInt()
        val outlineColor = 0xFF8B8680.toInt()
        val cardBgColor = 0xFFFFFFFF.toInt()

        val scrollView = ScrollView(this).apply {
            setBackgroundColor(surfaceColor)
            setPadding(0, statusBarHeight(), 0, 0)
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp20, dp24, dp20, dp24)
        }

        // ── 1. Header Card ──
        root.addView(roundedCard(
            bgColor = primaryContainerColor,
            cornerRadius = dp40
        ).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp24, dp24, dp24, dp24)
        }.also { card ->
            // Error icon circle
            card.addView(TextView(this).apply {
                text = "!"
                setTextColor(errorColor)
                textSize = 36f
                gravity = Gravity.CENTER
                val size = LayoutParams(dp72, dp72)
                layoutParams = size
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(errorColor and 0x00FFFFFF or 0x28000000)
                }
            })

            card.addView(space(dp16))

            card.addView(TextView(this).apply {
                text = "FieldMind crashed"
                setTextColor(onSurfaceColor)
                textSize = 22f
                setTypeface(typeface, Typeface.BOLD)
                gravity = Gravity.CENTER
            })

            card.addView(space(dp12))

            card.addView(TextView(this).apply {
                text = "Something unexpected went wrong. The crash report below has been saved " +
                    "and can be shared with the developer to help fix the issue."
                setTextColor(onSurfaceVariantColor)
                textSize = 14f
                gravity = Gravity.CENTER
                setLineSpacing(4f, 1f)
            })
        })

        root.addView(space(dp20))

        // ── 2. Crash Log Card ──
        root.addView(roundedCard(
            bgColor = cardBgColor,
            cornerRadius = dp24
        ).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp16, dp16, dp16, dp16)
        }.also { card ->
            card.addView(TextView(this).apply {
                text = "▸ Crash report"
                setTextColor(onSurfaceColor)
                textSize = 15f
                setTypeface(typeface, Typeface.DEFAULT_BOLD)
                setPadding(0, 0, 0, dp12)
            })

            card.addView(TextView(this).apply {
                text = crashLog
                setTextColor(onSurfaceColor)
                textSize = 11f
                typeface = Typeface.MONOSPACE
                setLineSpacing(2f, 1f)
                setTextIsSelectable(true)
                setPadding(dp12, dp12, dp12, dp12)
                background = GradientDrawable().apply {
                    setColor(outlineColor and 0x00FFFFFF or 0x10000000) // ~0.06 alpha
                    cornerRadius = dp12.toFloat()
                }
                minHeight = dp(200)
            })

            card.addView(space(dp12))

            val buttonRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            buttonRow.addView(styledButton(
                text = "Copy",
                bgColor = whiteColor,
                textColor = onSurfaceColor,
                borderColor = outlineColor,
                cornerRadius = dp24
            ).apply {
                val lp = LinearLayout.LayoutParams(0, dp(48), 1f)
                lp.rightMargin = dp8
                layoutParams = lp
                setOnClickListener { copyCrashLog(crashLog) }
            })
            buttonRow.addView(styledButton(
                text = "Share",
                bgColor = primaryColor,
                textColor = whiteColor,
                borderColor = null,
                cornerRadius = dp24
            ).apply {
                val lp = LinearLayout.LayoutParams(0, dp(48), 1f)
                lp.leftMargin = dp8
                layoutParams = lp
                setOnClickListener { shareCrashLog(crashLog) }
            })
            card.addView(buttonRow)
        })

        root.addView(space(dp20))

        // ── 3. Recovery Card ──
        root.addView(roundedCard(
            bgColor = cardBgColor,
            cornerRadius = dp24
        ).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp16, dp16, dp16, dp16)
        }.also { card ->
            card.addView(TextView(this).apply {
                text = "▸ Recovery options"
                setTextColor(onSurfaceColor)
                textSize = 15f
                setTypeface(typeface, Typeface.DEFAULT_BOLD)
                setPadding(0, 0, 0, dp8)
            })

            card.addView(TextView(this).apply {
                text = "If FieldMind keeps crashing on launch, the most common cause is a " +
                    "corrupted lock or PIN setting. You can disable those settings and restart " +
                    "to recover your data without losing any observations."
                setTextColor(onSurfaceVariantColor)
                textSize = 13f
                setLineSpacing(3f, 1f)
                setPadding(0, 0, 0, dp16)
            })

            card.addView(styledButton(
                text = "Disable lock & PIN, then restart",
                bgColor = primaryColor,
                textColor = whiteColor,
                borderColor = null,
                cornerRadius = dp24
            ).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(52)
                ).apply { bottomMargin = dp12 }
                setOnClickListener { showDisableConfirmDialog() }
            })

            card.addView(styledButton(
                text = "Restart FieldMind only",
                bgColor = whiteColor,
                textColor = onSurfaceColor,
                borderColor = outlineColor,
                cornerRadius = dp24
            ).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(52)
                )
                setOnClickListener { restartApp() }
            })
        })

        root.addView(space(dp24))

        // ── 4. Footer ──
        root.addView(TextView(this).apply {
            text = "Your research data is safe — only the lock and PIN settings will be disabled. " +
                "You can re-enable them in Settings after the app restarts."
            setTextColor(onSurfaceVariantColor)
            textSize = 12f
            gravity = Gravity.CENTER
            setLineSpacing(3f, 1f)
            setPadding(dp8, 0, dp8, dp24)
        })

        scrollView.addView(root)
        setContentView(scrollView)
    }

    // ── View Builders ──

    private fun roundedCard(bgColor: Int, cornerRadius: Int): LinearLayout {
        return LinearLayout(this).apply {
            background = GradientDrawable().apply {
                setColor(bgColor)
                this.cornerRadius = cornerRadius.toFloat()
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
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
            textSize = 14f
            setTypeface(typeface, Typeface.DEFAULT_BOLD)
            setAllCaps(false)
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                setColor(bgColor)
                this.cornerRadius = cornerRadius.toFloat()
                if (borderColor != null) {
                    setStroke(dp(2), borderColor)
                }
            }
            setPadding(dp20, 0, dp20, 0)
        }
    }

    private fun space(heightDp: Int): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, heightDp
            )
        }
    }

    private fun dp(value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    private fun statusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else dp(24)
    }

    // ── Confirmation Dialog ──

    private fun showDisableConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("Disable lock & PIN?")
            .setMessage(
                "Your research data and observations are safe. Only the privacy lock, app PIN, " +
                    "decoy PIN, panic-lock setting, and export password will be turned off. " +
                    "You can re-enable them in Settings after the restart."
            )
            .setPositiveButton("Disable & restart") { _, _ -> disableSecurityAndRestart() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ── Actions ──

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
        Toast.makeText(this, "Lock & PIN disabled. Restarting…", Toast.LENGTH_SHORT).show()
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
