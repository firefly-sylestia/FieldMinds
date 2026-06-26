package com.mirror.app

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.Build
import android.provider.Settings
import android.view.View
import android.content.ActivityNotFoundException
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mirror.app.databinding.ActivityTargetBinding
import java.net.Inet4Address
import java.net.NetworkInterface

class TargetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTargetBinding
    private lateinit var tvIpAddress: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvSessions: TextView
    private lateinit var tvFps: TextView
    private lateinit var btnStartHosting: Button
    private lateinit var btnInputOnlyHosting: Button
    private lateinit var btnStopHosting: Button
    private lateinit var spinnerQuality: Spinner
    private lateinit var qualityCard: View

    companion object {
        private const val REQUEST_CODE_MEDIA_PROJECTION = 1001
        private const val PREFS_NAME = "mirror_prefs"
        private const val KEY_INPUT_ONLY = "input_only_hosting"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTargetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tvIpAddress = binding.tvIpAddress
        tvStatus = binding.tvStatus
        tvSessions = binding.tvSessions
        tvFps = binding.tvFps
        btnStartHosting = binding.btnStartHosting
        btnInputOnlyHosting = binding.btnInputOnlyHosting
        btnStopHosting = binding.btnStopHosting
        spinnerQuality = binding.spinnerQuality
        qualityCard = binding.qualityCard

        // Set up quality selector
        val qualityLabels = listOf("480p (Smooth)", "720p (Balanced)", "1080p (HD)", "Native (Max)")
        val qualityPresets = listOf(
            ScreenCaptureService.Companion.QualityPreset.P480,
            ScreenCaptureService.Companion.QualityPreset.P720,
            ScreenCaptureService.Companion.QualityPreset.P1080,
            ScreenCaptureService.Companion.QualityPreset.NATIVE
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, qualityLabels)
        spinnerQuality.adapter = adapter

        // Load default quality from global settings
        val savedQualityIndex = SettingsActivity.getDefaultQualityIndex(this)
            .coerceIn(0, qualityPresets.size - 1)
        spinnerQuality.setSelection(savedQualityIndex)
        ScreenCaptureService.selectedQuality = qualityPresets[savedQualityIndex]

        spinnerQuality.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position < qualityPresets.size) {
                    ScreenCaptureService.selectedQuality = qualityPresets[position]
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Get and display local IP — show all interfaces for diagnostics
        val allIps = getAllLocalIpAddresses()
        val primaryIp = allIps.firstOrNull() ?: "0.0.0.0"
        tvIpAddress.text = primaryIp

        // On long-press, show all detected IPs (useful for debugging)
        tvIpAddress.setOnLongClickListener {
            val msg = if (allIps.size <= 1) {
                "Only found: $primaryIp\n\nIf connected via WiFi, your phone should have a WiFi IP."
            } else {
                "All IPs found:\n${allIps.joinToString("\n")}"
            }
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            true
        }

        // Copy IP to clipboard on tap
        tvIpAddress.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("IP", primaryIp))
            Toast.makeText(this, "IP copied!", Toast.LENGTH_SHORT).show()
        }

        // Start full hosting (with screen sharing)
        btnStartHosting.setOnClickListener {
            if (!isAccessibilityServiceEnabled()) {
                showAccessibilityPermissionDialog()
            } else if (!isOverlayPermissionGranted()) {
                showOverlayPermissionDialog()
            } else {
                startMediaProjectionFlow()
            }
        }

        // Start input-only hosting (no video, just keyboard & mouse)
        btnInputOnlyHosting.setOnClickListener {
            if (!isAccessibilityServiceEnabled()) {
                showAccessibilityPermissionDialog()
            } else {
                // No overlay permission needed for input-only (no cursor overlay)
                // Start only InputReceiverService + LatencyPingService
                startService(Intent(this, InputReceiverService::class.java))
                startService(Intent(this, LatencyPingService::class.java))

                btnStartHosting.visibility = View.GONE
                btnInputOnlyHosting.visibility = View.GONE
                btnStopHosting.visibility = View.VISIBLE
                qualityCard.visibility = View.GONE
                updateStatus("Status: ● Input-Only Hosting")
                tvSessions.text = "Sessions: 0 connected"
            }
        }

        // Stop hosting button
        btnStopHosting.setOnClickListener {
            stopAllServices()
            btnStartHosting.visibility = View.VISIBLE
            btnInputOnlyHosting.visibility = View.VISIBLE
            btnStopHosting.visibility = View.GONE
            qualityCard.visibility = View.VISIBLE
            unregisterControllerCallbacks()
            updateStatus("Status: ● Stopped")
            tvSessions.text = "Sessions: 0 connected"
        }

        // Add a help button / info section
        binding.tvWifiHelp.setOnClickListener {
            showConnectivityHelp(allIps)
        }

        // No action bar with the dark theme
    }

    override fun onResume() {
        super.onResume()
        if (btnStopHosting.visibility == View.VISIBLE) {
            registerControllerCallbacks()
        }
        // Refresh IP display in case network changed
        val allIps = getAllLocalIpAddresses()
        val primaryIp = allIps.firstOrNull() ?: "0.0.0.0"
        tvIpAddress.text = primaryIp
    }

    override fun onDestroy() {
        unregisterControllerCallbacks()
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        stopAllServices()
        finish()
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_MEDIA_PROJECTION && resultCode == RESULT_OK && data != null) {
            ScreenCaptureService.setPendingProjectionData(resultCode, data)

            startService(Intent(this, ScreenCaptureService::class.java))
            startService(Intent(this, InputReceiverService::class.java))
            startService(Intent(this, LatencyPingService::class.java))

            btnStartHosting.visibility = View.GONE
            btnStopHosting.visibility = View.VISIBLE
            updateStatus("Status: ● Waiting for controller...")
            registerControllerCallbacks()

            // Show the IPs again after starting (in case the user needs to tell controller)
            val allIps = getAllLocalIpAddresses()
            if (allIps.size > 1) {
                Toast.makeText(
                    this,
                    "IP: ${allIps.firstOrNull()}\n" +
                    "If controller can't connect, try one of the other IPs",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun registerControllerCallbacks() {
        ScreenCaptureService.onControllerConnected = {
            runOnUiThread {
                updateStatus("Status: ● Controller connected!")
                tvSessions.text = "Sessions: 1 connected"
            }
        }
        ScreenCaptureService.onControllerDisconnected = {
            runOnUiThread {
                updateStatus("Status: ● Waiting for controller...")
                tvSessions.text = "Sessions: 0 connected"
            }
        }
    }

    private fun unregisterControllerCallbacks() {
        ScreenCaptureService.onControllerConnected = null
        ScreenCaptureService.onControllerDisconnected = null
    }

    private fun startMediaProjectionFlow() {
        val mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(
            mediaProjectionManager.createScreenCaptureIntent(),
            REQUEST_CODE_MEDIA_PROJECTION
        )
    }

    private fun isOverlayPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else true
    }

    private fun showOverlayPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Overlay Permission Required")
            .setMessage(
                "To show a touch cursor on your screen when the controller is touching, " +
                    "you must enable 'Display over other apps' permission.\n\n" +
                    "Steps:\n1. Tap OPEN SETTINGS\n2. Find AndroidMirror in the list\n3. Toggle ON 'Allow display over other apps'\n4. Come back and try again"
            )
            .setPositiveButton("OPEN SETTINGS") { _, _ ->
                try {
                    startActivity(Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        android.net.Uri.parse("package:${packageName}")
                    ))
                } catch (_: ActivityNotFoundException) {
                    startActivity(Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS))
                }
            }
            .setNegativeButton("Not now", null)
            .show()
    }

    private fun showAccessibilityPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Accessibility Permission Required")
            .setMessage(
                "To inject touch input from the controller device, you must enable this app " +
                    "in Accessibility Settings.\n\nSteps:\n1. Tap OPEN SETTINGS\n2. Scroll down, " +
                    "tap AndroidMirror\n3. Toggle ON\n4. Come back and try again"
            )
            .setPositiveButton("OPEN SETTINGS") { _, _ ->
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: ""
        return enabledServices.contains("com.mirror.app/com.mirror.app.ControlAccessibilityService")
    }

    private fun stopAllServices() {
        unregisterControllerCallbacks()
        stopService(Intent(this, ScreenCaptureService::class.java))
        stopService(Intent(this, InputReceiverService::class.java))
        stopService(Intent(this, LatencyPingService::class.java))
    }

    private fun updateStatus(status: String) {
        runOnUiThread { tvStatus.text = status }
    }

    private fun showConnectivityHelp(ips: List<String>) {
        val message = buildString {
            append("Your device's IPs:\n${ips.joinToString("\n")}")
            append("\n\n━━━ Can't connect? ━━━")
            append("\n\n1️⃣ If both devices are on the SAME WiFi and can't connect,")
            append("\n   your router likely has AP Isolation enabled.")
            append("\n   → Disable it in router settings, OR")
            append("\n   → Turn off WiFi and use mobile hotspot instead.")
            append("\n\n2️⃣ If using hotspot:")
            append("\n   Enable hotspot on this device, connect the controller")
            append("\n   to that hotspot, enter the IP shown above.")
            append("\n\n3️⃣ If neither works:")
            append("\n   Use USB/ADB (see help text in the app).")
        }

        AlertDialog.Builder(this)
            .setTitle("📶 Connectivity Help")
            .setMessage(message)
            .setPositiveButton("Got it", null)
            .show()
    }

    /**
     * Get all non-loopback IPv4 addresses on this device.
     * Returns them sorted: WiFi first, then others.
     */
    private fun getAllLocalIpAddresses(): List<String> {
        val ips = mutableListOf<String>()
        try {
            NetworkInterface.getNetworkInterfaces().asSequence()
                .filter { !it.isLoopback }
                .forEach { networkInterface ->
                    networkInterface.inetAddresses.asSequence()
                        .filter { it is Inet4Address && !it.isLoopbackAddress }
                        .forEach { inet ->
                            val name = networkInterface.name ?: "unknown"
                            val ip = inet.hostAddress ?: ""
                            // Prioritize wlan (WiFi) interfaces
                            if (name.startsWith("wlan")) {
                                ips.add(0, "$ip ($name)")
                            } else if (!name.startsWith("dummy") && !name.startsWith("lo")) {
                                ips.add("$ip ($name)")
                            }
                        }
                }
        } catch (_: Exception) {}
        if (ips.isEmpty()) ips.add("0.0.0.0")
        return ips
    }
}
