package com.mirror.app

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.lifecycleScope
import com.mirror.app.databinding.ActivityControllerBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.NetworkInterface
import android.graphics.Color

/**
 * Helper to auto-repeat an action while a view is held down.
 * Starts with an initial delay, then repeats at a faster interval.
 */
private class RepeatHandler(
    private val onRepeat: () -> Unit
) {
    companion object {
        private const val INITIAL_DELAY_MS = 400L
        private const val REPEAT_INTERVAL_MS = 100L
    }

    private val handler = Handler(Looper.getMainLooper())
    @Volatile
    var isHolding = false
        private set

    private val repeatRunnable = object : Runnable {
        override fun run() {
            if (isHolding) {
                onRepeat()
                handler.postDelayed(this, REPEAT_INTERVAL_MS)
            }
        }
    }

    fun start() {
        isHolding = true
        onRepeat()
        handler.postDelayed(repeatRunnable, INITIAL_DELAY_MS)
    }

    fun stop() {
        isHolding = false
        handler.removeCallbacks(repeatRunnable)
    }

    fun destroy() {
        stop()
    }
}

class ControllerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityControllerBinding
    private lateinit var etTargetIp: EditText
    private lateinit var btnConnect: Button
    private lateinit var switchInputOnly: SwitchCompat
    private lateinit var tvStatus: TextView
    private lateinit var tvLatency: TextView
    private lateinit var connectionUI: ScrollView
    private lateinit var mirrorContainer: FrameLayout
    private lateinit var mirrorView: MirrorSurfaceView
    private lateinit var statusBar: FrameLayout
    private lateinit var tvLatencyBar: TextView
    private lateinit var tvFpsBar: TextView
    private lateinit var btnDisconnect: Button
    private lateinit var btnToggleKeyboard: Button
    private lateinit var navButtons: View
    private lateinit var btnNavBack: TextView
    private lateinit var btnNavHome: TextView
    private lateinit var btnNavRecent: TextView
    private lateinit var btnRatioToggle: TextView
    private lateinit var btnToggleDpad: TextView
    private lateinit var cursorOverlay: CursorOverlayView
    private lateinit var dpadRow: View
    private lateinit var btnDpadUp: TextView
    private lateinit var btnDpadDown: TextView
    private lateinit var btnDpadLeft: TextView
    private lateinit var btnDpadRight: TextView
    private lateinit var btnScan: Button
    private lateinit var discoveredHostsCard: FrameLayout
    private lateinit var discoveredHostsList: LinearLayout
    private val hostDiscovery = HostDiscovery(this)
    private val discoveredHosts = mutableSetOf<String>()

    private var inputSender: InputSenderService? = null
    private var streamReceiver: StreamReceiverService? = null
    private var latencyChecker: LatencyChecker? = null

    companion object {
        private const val TAG = "Controller"
    }

    @Volatile
    private var isConnecting = false
    private var lastAttemptedIp = ""

    // ── Button repeat state ──
    private var repeatHandlers = mutableListOf<RepeatHandler>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityControllerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        etTargetIp = binding.etTargetIp
        btnConnect = binding.btnConnect
        switchInputOnly = binding.switchInputOnly
        tvStatus = binding.tvStatus
        tvLatency = binding.tvLatency
        connectionUI = binding.connectionUI
        mirrorContainer = binding.mirrorContainer
        mirrorView = binding.mirrorView
        statusBar = binding.statusBar
        tvLatencyBar = binding.tvLatencyBar
        tvFpsBar = binding.tvFpsBar
        btnDisconnect = binding.btnDisconnect
        btnToggleKeyboard = binding.btnToggleKeyboard
        btnToggleDpad = binding.btnToggleDpad
        navButtons = binding.navButtons
        btnNavBack = binding.btnNavBack
        btnNavHome = binding.btnNavHome
        btnNavRecent = binding.btnNavRecent
        btnRatioToggle = binding.btnRatioToggle
        cursorOverlay = binding.cursorOverlay
        dpadRow = binding.dpadRow
        btnScan = binding.btnScan
        discoveredHostsCard = binding.discoveredHostsCard
        discoveredHostsList = binding.discoveredHostsList

        // Wire up cursor overlay to MirrorSurfaceView
        mirrorView.setCursorOverlay(cursorOverlay)
        btnDpadUp = binding.btnDpadUp
        btnDpadDown = binding.btnDpadDown
        btnDpadLeft = binding.btnDpadLeft
        btnDpadRight = binding.btnDpadRight

        // Load last IP from SharedPreferences
        val prefs = getSharedPreferences("mirror_prefs", MODE_PRIVATE)
        etTargetIp.setText(prefs.getString("last_ip", ""))

        // Show own IP for debugging
        tvLatency.text = "Your IP: ${getLocalIpAddress()}"

        // ── Auto-Connect (if enabled in settings) ──
        if (SettingsActivity.isAutoConnectEnabled(this)) {
            val lastIp = prefs.getString("last_ip", "") ?: ""
            if (lastIp.isNotEmpty() && !isConnecting) {
                // Auto-connect after a brief delay so the UI settles
                android.os.Handler(mainLooper).postDelayed({
                    if (!isConnecting && lastIp == prefs.getString("last_ip", "")) {
                        etTargetIp.setText(lastIp)
                        isConnecting = true
                        btnConnect.isEnabled = false
                        tvStatus.text = "● Auto-connecting..."
                        connect(lastIp)
                    }
                }, 500)
            }
        }

        // ── Scan for nearby hosts ──
        btnScan.setOnClickListener {
            discoveredHosts.clear()
            discoveredHostsCard.visibility = View.GONE
            discoveredHostsList.removeAllViews()
            btnScan.text = "⏳ Scanning..."
            btnScan.isEnabled = false
            hostDiscovery.onHostDiscovered = { host ->
                runOnUiThread {
                    addDiscoveredHost(host)
                }
            }
            hostDiscovery.startListening()
            // Stop scanning after 5 seconds
            android.os.Handler(mainLooper).postDelayed({
                hostDiscovery.stopListening()
                btnScan.text = "🔍  SCAN NEARBY DEVICES"
                btnScan.isEnabled = true
                if (discoveredHosts.isEmpty()) {
                    Toast.makeText(this, "No hosts found nearby — make sure the target has started hosting", Toast.LENGTH_LONG).show()
                }
            }, 5000)
        }

        btnConnect.setOnClickListener {
            if (isConnecting) return@setOnClickListener
            val ip = etTargetIp.text.toString().trim()
            if (ip.isEmpty()) {
                Toast.makeText(this, "Enter target IP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save IP
            prefs.edit().putString("last_ip", ip).apply()
            lastAttemptedIp = ip

            isConnecting = true
            btnConnect.isEnabled = false
            tvStatus.text = "● Connecting..."
            connect(ip)
        }

        btnDisconnect.setOnClickListener {
            disconnect()
        }

        // ── DPAD toggle (show/hide arrow buttons) ──
        btnToggleDpad.setOnClickListener {
            val isVisible = dpadRow.visibility == View.VISIBLE
            dpadRow.visibility = if (isVisible) View.GONE else View.VISIBLE
            btnToggleDpad.setTextColor(
                if (isVisible) android.graphics.Color.parseColor("#666666")
                else android.graphics.Color.parseColor("#00E5FF")
            )
            btnToggleDpad.text = if (isVisible) "\u229E" else "\u229F"
        }

        // ── Keyboard toggle (respects global settings) ──
        btnToggleKeyboard.setOnClickListener {
            val isEnabled = !mirrorView.isKeyboardForwarding()
            mirrorView.setKeyboardForwarding(isEnabled)
            btnToggleKeyboard.setTextColor(
                if (isEnabled) android.graphics.Color.parseColor("#4CAF50")
                else android.graphics.Color.parseColor("#666666")
            )
            Toast.makeText(this,
                if (isEnabled) "Keyboard forwarding: ON" else "Keyboard forwarding: OFF",
                Toast.LENGTH_SHORT
            ).show()
        }

        // ── Screen ratio toggle (initial: CROP mode) ──
        updateRatioButtonText()
        btnRatioToggle.setOnClickListener {
            mirrorView.cycleDisplayMode()
            updateRatioButtonText()
        }

        // ── Navigation buttons (send key events to target) ──
        btnNavBack.setOnClickListener {
            inputSender?.sendKeyEvent(android.view.KeyEvent.KEYCODE_BACK)
        }
        btnNavHome.setOnClickListener {
            inputSender?.sendKeyEvent(android.view.KeyEvent.KEYCODE_HOME)
        }
        btnNavRecent.setOnClickListener {
            inputSender?.sendKeyEvent(android.view.KeyEvent.KEYCODE_APP_SWITCH)
        }

        // ── DPAD buttons with hold-to-repeat ──
        setupRepeatButton(btnDpadUp) {
            inputSender?.sendKeyEvent(android.view.KeyEvent.KEYCODE_DPAD_UP)
        }
        setupRepeatButton(btnDpadDown) {
            inputSender?.sendKeyEvent(android.view.KeyEvent.KEYCODE_DPAD_DOWN)
        }
        setupRepeatButton(btnDpadLeft) {
            inputSender?.sendKeyEvent(android.view.KeyEvent.KEYCODE_DPAD_LEFT)
        }
        setupRepeatButton(btnDpadRight) {
            inputSender?.sendKeyEvent(android.view.KeyEvent.KEYCODE_DPAD_RIGHT)
        }
    }

    /**
     * Set up a view with hold-to-repeat behavior.
     * Tapping fires once. Holding fires after [RepeatHandler.INITIAL_DELAY_MS],
     * then repeats every [RepeatHandler.REPEAT_INTERVAL_MS].
     *
     * We handle EVERYTHING in [setOnTouchListener] and consume the event
     * (return true) so the system doesn't also fire the click listener.
     * [performClick] is called on UP for accessibility compatibility.
     */
    private fun setupRepeatButton(view: View, action: () -> Unit) {
        view.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    action() // Fire once immediately on touch down
                    val handler = RepeatHandler(action)
                    repeatHandlers.add(handler)
                    handler.start()
                    v.isPressed = true
                }
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    v.isPressed = false
                    val idx = repeatHandlers.indexOfFirst { it.isHolding }
                    if (idx >= 0) {
                        repeatHandlers[idx].stop()
                        repeatHandlers.removeAt(idx)
                    }
                    // Call performClick for accessibility (screen readers)
                    if (event.actionMasked == MotionEvent.ACTION_UP) {
                        v.performClick()
                    }
                }
            }
            true // Consume the event — prevent system from also firing click listener
        }
    }

    override fun onDestroy() {
        hostDiscovery.stopListening()
        repeatHandlers.forEach { it.destroy() }
        repeatHandlers.clear()
        super.onDestroy()
    }

    private fun updateRatioButtonText() {
        val mode = mirrorView.getDisplayMode()
        val label = MirrorSurfaceView.label(mode)
        btnRatioToggle.text = label
        btnRatioToggle.setTextColor(
            when (mode) {
                MirrorSurfaceView.DISPLAY_CROP -> android.graphics.Color.parseColor("#4CAF50")   // green = optimal
                MirrorSurfaceView.DISPLAY_FILL -> android.graphics.Color.parseColor("#FFD740")   // yellow = stretched
                else -> android.graphics.Color.parseColor("#6C6C88")                            // muted = letterbox
            }
        )
    }

    /**
     * Connect to the target. In normal mode, chains input + video connections.
     * In input-only mode, connects only to the input socket (port 9002) and
     * shows a touchpad instead of the mirror view.
     *
     * 1. Start InputSenderService (TCP 9002) — sends the handshake with target resolution
     * 2. If not input-only: start StreamReceiverService (TCP 9001) for video
     * 3. Once ready, show the appropriate UI
     */
    private fun connect(ip: String) {
        val isInputOnly = switchInputOnly.isChecked

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // ── 1. Connect input sender with handshake callback ──
                val sender = InputSenderService(
                    ip, 9002,
                    onConnected = null,
                    onError = { errorMsg ->
                        runOnUiThread { showConnectionError(errorMsg) }
                    },
                    onHandshake = { targetW, targetH ->
                        Log.d(TAG, "Target screen: ${targetW}x${targetH}")
                        // CRITICAL: Update MirrorSurfaceView with actual target resolution
                        // so mapCoordinates() scales touch events correctly.
                        runOnUiThread {
                            mirrorView.setTargetResolution(targetW, targetH)
                        }
                    }
                )
                inputSender = sender

                // Wait for input connection + handshake
                var inputConnected = false
                sender.start()

                val inputTimeout = 5000L
                val inputStartTime = System.currentTimeMillis()
                while (!inputConnected && System.currentTimeMillis() - inputStartTime < inputTimeout) {
                    if (sender.isOutputReady() && sender.targetWidth > 0) {
                        inputConnected = true
                    } else {
                        kotlinx.coroutines.delay(100)
                    }
                }

                if (!inputConnected) {
                    Log.w(TAG, "Input connection timeout")
                    runOnUiThread {
                        showConnectionError("Can't reach $ip — target may be on a different network, or router blocks connections (AP Isolation)")
                    }
                    return@launch
                }

                if (isInputOnly) {
                    // ── Input-Only mode: no video, show touchpad ──
                    runOnUiThread {
                        mirrorView.setInputSender(inputSender!!)
                        mirrorView.setKeyboardForwarding(SettingsActivity.isKeyboardEnabled(this@ControllerActivity))
                        btnToggleKeyboard.setTextColor(
                            if (SettingsActivity.isKeyboardEnabled(this@ControllerActivity))
                                android.graphics.Color.parseColor("#4CAF50")
                            else android.graphics.Color.parseColor("#666666")
                        )
                        // Hide FPS bar (no video), show "Input" label
                        tvFpsBar.text = "Input: ${sender.targetWidth}x${sender.targetHeight}"
                        hideConnectUI()
                        tvStatus.text = "\u25CF Input-Only"
                        isConnecting = false

                        latencyChecker = LatencyChecker(ip, 9003, { latencyMs ->
                            runOnUiThread {
                                tvLatencyBar.text = "Lat: ${latencyMs}ms"
                                tvLatency.text = "Latency: ${latencyMs} ms"
                            }
                        })
                        latencyChecker!!.start()
                    }
                } else {
                    // ── Normal mode: connect video stream after input ──
                    streamReceiver = StreamReceiverService(
                        ip, 9001, mirrorView,
                        onConnected = {
                            runOnUiThread {
                                mirrorView.setInputSender(inputSender!!)
                                mirrorView.setKeyboardForwarding(SettingsActivity.isKeyboardEnabled(this@ControllerActivity))
                                btnToggleKeyboard.setTextColor(
                                    if (SettingsActivity.isKeyboardEnabled(this@ControllerActivity))
                                        android.graphics.Color.parseColor("#4CAF50")
                                    else android.graphics.Color.parseColor("#666666")
                                )
                                hideConnectUI()
                                tvStatus.text = "\u25CF Connected"
                                isConnecting = false

                                latencyChecker = LatencyChecker(ip, 9003, { latencyMs ->
                                    runOnUiThread {
                                        tvLatencyBar.text = "Lat: ${latencyMs}ms"
                                        tvLatency.text = "Latency: ${latencyMs} ms"
                                    }
                                })
                                latencyChecker!!.start()
                            }
                        },
                        onError = { errorMsg ->
                            runOnUiThread { showConnectionError(errorMsg) }
                        },
                        onFpsUpdate = { fps ->
                            runOnUiThread { tvFpsBar.text = "FPS: $fps" }
                        }
                    )
                    streamReceiver!!.start()
                }

            } catch (e: Exception) {
                runOnUiThread {
                    showConnectionError(e.message ?: "Unknown connection error")
                }
            }
        }
    }

    /**
     * Show a detailed error message with AP isolation detection and suggestions.
     */
    private fun showConnectionError(errorMsg: String) {
        isConnecting = false
        btnConnect.isEnabled = true

        val lower = errorMsg.lowercase()
        val isTimeout = lower.contains("timeout") || lower.contains("timed out")
        val isReachability = lower.contains("can't reach") || lower.contains("refused")

        val message = buildString {
            append(errorMsg)
            append("\n\n\u2501\u2501\u2501 Troubleshooting \u2501\u2501\u2501")

            if (isTimeout || isReachability) {
                append("\n\n\u26A0\uFE0F Most likely: **AP Isolation** (router blocks WiFi-to-WiFi connections)")
                append("\n\n\u2705 Fix option 1: Turn off your phone's WiFi and enable")
                append("\n   MOBILE HOTSPOT on the TARGET device.")
                append("\n   Then connect the CONTROLLER to that hotspot.")
                append("\n   No router involved \u2014 works every time.")
                append("\n\n\u2705 Fix option 2: Log into your router settings and")
                append("\n   turn off AP Isolation / Client Isolation / Wireless Isolation.")
                append("\n\n\u2705 Fix option 3: Use USB (ADB reverse tcp) \u2014 see the help text above.")
            } else {
                append("\n\nCheck that:")
                append("\n\u2022 Both devices are on the same network")
                append("\n\u2022 The Target has started hosting (green status)")
                append("\n\u2022 You entered the correct IP")
            }

            append("\n\nYour IP: ${getLocalIpAddress()}")
        }

        tvStatus.text = "\u25CB Connection failed"
        Toast.makeText(this@ControllerActivity, message, Toast.LENGTH_LONG).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        disconnect()
        finish()
        return true
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        disconnect()
        super.onBackPressed()
    }

    private fun hideConnectUI() {
        connectionUI.visibility = View.GONE
        mirrorContainer.visibility = View.VISIBLE
        statusBar.visibility = View.VISIBLE
    }

    private fun showConnectUI() {
        connectionUI.visibility = View.VISIBLE
        mirrorContainer.visibility = View.GONE
        statusBar.visibility = View.GONE
    }

    private fun disconnect() {
        hostDiscovery.stopListening()
        inputSender?.stop()
        streamReceiver?.stop()
        latencyChecker?.stop()
        inputSender = null
        streamReceiver = null
        latencyChecker = null
        isConnecting = false
        btnConnect.isEnabled = true
        showConnectUI()
        tvStatus.text = "Status: \u25CB Disconnected"
    }

    /** Add a discovered host to the list and make it tappable. */
    private fun addDiscoveredHost(host: HostDiscovery.DiscoveredHost) {
        // Avoid duplicates
        if (!discoveredHosts.add(host.ip)) return

        discoveredHostsCard.visibility = View.VISIBLE

        val item = TextView(this).apply {
            text = "\uD83D\uDCF1  ${host.name}\n     ${host.ip}"
            textSize = 14f
            setTextColor(Color.parseColor("#E0E0E0"))
            setPadding(12, 12, 12, 12)
            setBackgroundResource(android.R.drawable.list_selector_background)
            isClickable = true
            isFocusable = true
            setOnClickListener {
                etTargetIp.setText(host.ip)
                discoveredHostsCard.visibility = View.GONE
                hostDiscovery.stopListening()
                btnScan.text = "🔍  SCAN NEARBY DEVICES"
                btnScan.isEnabled = true
                Toast.makeText(this@ControllerActivity, "Selected ${host.name}", Toast.LENGTH_SHORT).show()
            }
        }
        discoveredHostsList.addView(item)
    }

    private fun getLocalIpAddress(): String {
        return try {
            NetworkInterface.getNetworkInterfaces().asSequence()
                .flatMap { it.inetAddresses.asSequence() }
                .first { !it.isLoopbackAddress && it is java.net.Inet4Address }
                .hostAddress
        } catch (e: Exception) {
            "Unknown"
        }
    }
}
