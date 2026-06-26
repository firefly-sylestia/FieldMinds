package com.mirror.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.content.pm.ServiceInfo
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket

class InputReceiverService : Service() {

    private var socketServer: ServerSocket? = null
    private var clientSocket: java.net.Socket? = null
    private lateinit var inputInjector: InputInjector
    private var cursorOverlay: CursorOverlay? = null
    private var hostDiscovery: HostDiscovery? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var blackOverlay: BlackOverlay? = null

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        private const val TAG = "InputReceiver"
        private const val INPUT_PORT = 9002
        private const val CHANNEL_ID = "mirror_input_channel"
        private const val NOTIFICATION_ID = 2

        // Protocol types
        private const val TYPE_TOUCH = 0x01
        private const val TYPE_KEY = 0x02
        private const val TYPE_SCROLL = 0x03
        private const val TYPE_CURSOR = 0x04
        private const val TYPE_KEY_CHAR = 0x05
    }

    override fun onCreate() {
        super.onCreate()
        inputInjector = InputInjector(this)

        // Connect cursor overlay to input injector (only if enabled in settings)
        if (SettingsActivity.isCursorOverlayEnabled(this)) {
            val overlay = CursorOverlay(this)
            cursorOverlay = overlay
            inputInjector.onCursorPosition = { x, y ->
                overlay.showAt(x, y)
            }
        }

        createNotificationChannel()
    }

    override fun onDestroy() {
        inputInjector.stopScrollInertia()
        cursorOverlay?.destroy()
        cursorOverlay = null
        releaseWakeLock()
        dismissBlackOverlay()
        hostDiscovery?.stopBroadcasting()
        hostDiscovery = null
        try {
            clientSocket?.close()
        } catch (_: Exception) {}
        try {
            socketServer?.close()
        } catch (_: Exception) {}
        scope.cancel()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(
            NOTIFICATION_ID,
            createNotification("Waiting for input..."),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )

        // ── Keep screen on (WakeLock) ──
        if (SettingsActivity.isKeepScreenOnEnabled(this)) {
            acquireWakeLock()
        }

        // ── Black overlay (AMOLED battery saver) ──
        if (SettingsActivity.isBlackOverlayEnabled(this)) {
            showBlackOverlay()
        }

        // ── Start UDP beacon broadcasting for automatic discovery ──
        // This lets controllers find this device without typing the IP.
        hostDiscovery = HostDiscovery(this)
        val deviceName = Build.MODEL
        val localIp = getLocalIpAddress()
        if (localIp != null) {
            hostDiscovery?.startBroadcasting("$deviceName (AndroidMirror)", localIp)
            Log.d(TAG, "Started beacon broadcasting as '$deviceName' @ $localIp")
        }

        scope.launch {
            try {
                Log.d(TAG, "Listening for input connections on port $INPUT_PORT...")
                socketServer = ServerSocket(INPUT_PORT)
                clientSocket = socketServer!!.accept()
                clientSocket!!.tcpNoDelay = true

                Log.d(TAG, "Controller connected for input!")

                // ── CRITICAL: coordinate mapping setup ──
                // The controller maps touch coordinates to the target's screen dimensions
                // (from the handshake below). On the target side, InputInjector.scaleToScreen()
                // maps from video resolution to screen resolution.
                //
                // If ScreenCaptureService IS running: syncVideoResolution() will read the
                // actual video resolution (e.g. 720x1280) and set videoWidth/videoHeight.
                // Then scaleToScreen correctly maps incoming video-space coords to screen.
                //
                // If ScreenCaptureService is NOT running (input-only mode): syncVideoResolution()
                // finds no video source, so videoWidth stays at its default (720x1280). But the
                // controller sends coords already in SCREEN space — we'd double-scale them.
                //
                // Fix: set video res = screen res FIRST, then syncVideoResolution OVERWRITES
                // if ScreenCaptureService is running. If not running, video res stays =
                // screen res → scaleToScreen becomes identity → correct.

                // Step 1: Get target screen dimensions
                val targetW = inputInjector.getTargetWidth()
                val targetH = inputInjector.getTargetHeight()

                // Step 2: Set video resolution = screen resolution (safe default for input-only)
                inputInjector.setVideoResolution(targetW, targetH)

                // Step 3: syncVideoResolution WILL OVERWRITE video res with actual video
                // dimensions if ScreenCaptureService is running (normal mode)
                inputInjector.syncVideoResolution()

                // Step 4: Send handshake
                val output = DataOutputStream(clientSocket!!.getOutputStream())
                output.writeInt(targetW)
                output.writeInt(targetH)
                output.flush()
                Log.d(TAG, "Sent handshake: ${targetW}x${targetH}")

                handleInputStream()
            } catch (e: Exception) {
                Log.e(TAG, "Input receiver error", e)
            }
        }

        return START_STICKY
    }

    private suspend fun handleInputStream() {
        val input = DataInputStream(clientSocket!!.inputStream)

        while (scope.isActive) {
            try {
                val type = input.readByte().toInt()

                when (type) {
                    TYPE_TOUCH -> {
                        val x = input.readFloat()
                        val y = input.readFloat()
                        val pressure = input.readFloat()
                        val action = input.readInt()
                        inputInjector.injectTouchEvent(x, y, pressure, action)
                    }
                    TYPE_KEY -> {
                        val keyCode = input.readInt()
                        inputInjector.injectKeyEvent(keyCode)
                    }
                    TYPE_KEY_CHAR -> {
                        val keyCode = input.readInt()
                        val unicodeChar = input.readInt()
                        inputInjector.injectKeyEvent(keyCode, unicodeChar)
                    }
                    TYPE_SCROLL -> {
                        val dx = input.readFloat()
                        val dy = input.readFloat()
                        inputInjector.injectScroll(dx, dy)
                    }
                    TYPE_CURSOR -> {
                        val x = input.readFloat()
                        val y = input.readFloat()
                        inputInjector.updateCursorPosition(x, y)
                    }
                }
            } catch (e: java.io.EOFException) {
                break
            } catch (e: Exception) {
                Log.e(TAG, "Input read error", e)
                break
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ── WakeLock (Keep Screen On) ──

    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            @Suppress("DEPRECATION")
            val lock = powerManager.newWakeLock(
                PowerManager.SCREEN_DIM_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "AndroidMirror:KeepScreenOn"
            )
            lock.acquire()
            wakeLock = lock
            Log.d(TAG, "WakeLock acquired (keep screen on)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to acquire WakeLock", e)
        }
    }

    private fun releaseWakeLock() {
        try {
            wakeLock?.release()
            Log.d(TAG, "WakeLock released")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release WakeLock", e)
        }
        wakeLock = null
    }

    // ── Black Overlay (AMOLED Battery Saver) ──

    private fun showBlackOverlay() {
        if (blackOverlay == null) {
            blackOverlay = BlackOverlay(this)
        }
        val shown = blackOverlay!!.show()
        if (shown) {
            Log.d(TAG, "Black overlay active")
        } else {
            Log.w(TAG, "Black overlay not shown (permission?)")
        }
    }

    private fun dismissBlackOverlay() {
        blackOverlay?.dismiss()
        blackOverlay = null
    }

    /** Get the device's primary local IPv4 address. */
    private fun getLocalIpAddress(): String? {
        try {
            NetworkInterface.getNetworkInterfaces()?.iterator()?.forEach { ni ->
                if (!ni.isLoopback && ni.isUp) {
                    ni.inetAddresses?.iterator()?.forEach { addr ->
                        if (addr is Inet4Address && !addr.isLoopbackAddress) {
                            return addr.hostAddress
                        }
                    }
                }
            }
        } catch (_: Exception) {}
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Input Receiver",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(text: String) =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AndroidMirror Input")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .setOngoing(true)
            .build()
}
