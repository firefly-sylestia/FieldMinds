package com.mirror.app

import android.content.Context
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.mirror.app.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var switchKeyboard: Switch
    private lateinit var switchCursorOverlay: Switch
    private lateinit var switchAutoConnect: Switch
    private lateinit var switchKeepScreenOn: Switch
    private lateinit var switchBlackOverlay: Switch
    private lateinit var spinnerQuality: Spinner
    private lateinit var seekScrollSpeed: SeekBar
    private lateinit var tvScrollSpeed: TextView
    private lateinit var seekDragSpeed: SeekBar
    private lateinit var tvDragSpeed: TextView

    companion object {
        private const val PREFS_NAME = "mirror_settings"

        const val KEY_KEYBOARD_ENABLED = "keyboard_enabled"
        const val KEY_CURSOR_OVERLAY_ENABLED = "cursor_overlay_enabled"
        const val KEY_DEFAULT_QUALITY_INDEX = "default_quality_index"
        const val KEY_SCROLL_SPEED = "scroll_speed"
        const val KEY_AUTO_CONNECT = "auto_connect"
        const val KEY_KEEP_SCREEN_ON = "keep_screen_on"
        const val KEY_BLACK_OVERLAY = "black_overlay"
        const val KEY_DRAG_SPEED = "drag_speed"

        /** Read whether keyboard forwarding is enabled globally. */
        fun isKeyboardEnabled(context: Context): Boolean {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_KEYBOARD_ENABLED, false)
        }

        /** Read whether the cursor overlay is enabled globally. */
        fun isCursorOverlayEnabled(context: Context): Boolean {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_CURSOR_OVERLAY_ENABLED, true)
        }

        /** Read whether auto-connect is enabled (controller auto-connects to last host). */
        fun isAutoConnectEnabled(context: Context): Boolean {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_AUTO_CONNECT, false)
        }

        /** Read whether the target should keep the screen on while hosting. */
        fun isKeepScreenOnEnabled(context: Context): Boolean {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_KEEP_SCREEN_ON, false)
        }

        /** Read whether the black overlay (AMOLED battery saver) is enabled on the target. */
        fun isBlackOverlayEnabled(context: Context): Boolean {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_BLACK_OVERLAY, false)
        }

        /** Read the default quality preset index (0=480p, 1=720p, 2=1080p, 3=Native). */
        fun getDefaultQualityIndex(context: Context): Int {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_DEFAULT_QUALITY_INDEX, 1)
        }

        /**
         * Read the scroll speed multiplier (1–10).
         * Default: 6 (matches the original SCROLL_SPEED_MULTIPLIER).
         * Storage: SeekBar progress 2..18 maps to multiplier 1..10.
         * Progress 12 yields 1 + (12-2)*0.5 = 6x.
         */
        fun getScrollSpeedMultiplier(context: Context): Float {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val progress = prefs.getInt(KEY_SCROLL_SPEED, 12)
            return 1f + (progress - 2) * 0.5f
        }

        /**
         * Read the drag/swipe segment duration in milliseconds.
         * Lower = faster swipes; higher = slower, more deliberate.
         * Storage: SeekBar progress 2 (slow) .. 18 (fast), default 10.
         * Mapping: duration = 22 - progress, clamped to [4, 20].
         * Progress 12 → 10ms (matches the original DRAG_SEGMENT_DURATION_MS).
         */
        fun getDragDurationMs(context: Context): Long {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val progress = prefs.getInt(KEY_DRAG_SPEED, 12)
            val clamped = progress.coerceIn(2, 18)
            return (22L - clamped).coerceIn(4L, 20L)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setTitle("Settings")

        switchKeyboard = binding.switchKeyboard
        switchCursorOverlay = binding.switchCursorOverlay
        switchAutoConnect = binding.switchAutoConnect
        switchKeepScreenOn = binding.switchKeepScreenOn
        switchBlackOverlay = binding.switchBlackOverlay
        spinnerQuality = binding.spinnerSettingsQuality
        seekScrollSpeed = binding.seekScrollSpeed
        tvScrollSpeed = binding.tvScrollSpeed
        seekDragSpeed = binding.seekDragSpeed
        tvDragSpeed = binding.tvDragSpeed

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        // Load current values
        switchKeyboard.isChecked = prefs.getBoolean(KEY_KEYBOARD_ENABLED, false)
        switchCursorOverlay.isChecked = prefs.getBoolean(KEY_CURSOR_OVERLAY_ENABLED, true)
        switchAutoConnect.isChecked = prefs.getBoolean(KEY_AUTO_CONNECT, false)
        switchKeepScreenOn.isChecked = prefs.getBoolean(KEY_KEEP_SCREEN_ON, false)
        switchBlackOverlay.isChecked = prefs.getBoolean(KEY_BLACK_OVERLAY, false)

        // Quality spinner
        val qualityLabels = listOf("480p (Smooth)", "720p (Balanced)", "1080p (HD)", "Native (Max)")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, qualityLabels)
        spinnerQuality.adapter = adapter
        spinnerQuality.setSelection(prefs.getInt(KEY_DEFAULT_QUALITY_INDEX, 1))

        // Scroll speed seek bar (progress 2..18 → multiplier 1..10, default 6 = progress 12)
        val savedScrollProgress = prefs.getInt(KEY_SCROLL_SPEED, 12)
        seekScrollSpeed.progress = savedScrollProgress.coerceIn(2, 18)
        updateScrollSpeedLabel(savedScrollProgress)

        seekScrollSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val clamped = progress.coerceIn(2, 18)
                    prefs.edit().putInt(KEY_SCROLL_SPEED, clamped).apply()
                    updateScrollSpeedLabel(clamped)
                }
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        // Swipe speed seek bar (progress 2..18 → duration 20..4ms, default 10ms = progress 12)
        val savedDragProgress = prefs.getInt(KEY_DRAG_SPEED, 12)
        seekDragSpeed.progress = savedDragProgress.coerceIn(2, 18)
        updateDragSpeedLabel(savedDragProgress)

        seekDragSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val clamped = progress.coerceIn(2, 18)
                    prefs.edit().putInt(KEY_DRAG_SPEED, clamped).apply()
                    updateDragSpeedLabel(clamped)
                }
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        // Save changes
        switchKeyboard.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_KEYBOARD_ENABLED, isChecked).apply()
        }

        switchCursorOverlay.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_CURSOR_OVERLAY_ENABLED, isChecked).apply()
        }

        switchAutoConnect.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_AUTO_CONNECT, isChecked).apply()
        }

        switchKeepScreenOn.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_KEEP_SCREEN_ON, isChecked).apply()
        }

        switchBlackOverlay.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_BLACK_OVERLAY, isChecked).apply()
        }

        spinnerQuality.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                prefs.edit().putInt(KEY_DEFAULT_QUALITY_INDEX, position).apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun updateScrollSpeedLabel(progress: Int) {
        val multiplier = 1f + (progress - 2) * 0.5f
        tvScrollSpeed.text = "Scroll Speed: ${multiplier}x"
    }

    private fun updateDragSpeedLabel(progress: Int) {
        val clamped = progress.coerceIn(2, 18)
        val duration = (22L - clamped).coerceIn(4L, 20L)
        val label = when {
            duration <= 6 -> "🚀 Fast"
            duration <= 10 -> "Balanced"
            else -> "🐢 Deliberate"
        }
        tvDragSpeed.text = "Swipe Speed: ${duration}ms ($label)"
    }
}
