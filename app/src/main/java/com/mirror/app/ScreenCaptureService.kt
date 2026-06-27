package com.mirror.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.os.Process
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import java.io.DataOutputStream
import java.net.ServerSocket

class ScreenCaptureService : Service() {

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var encoder: MediaCodec? = null
    private var socketServer: ServerSocket? = null
    private var clientSocket: java.net.Socket? = null

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        private const val TAG = "ScreenCapture"
        private const val VIDEO_PORT = 9001
        private const val FRAME_RATE = 60
        private const val I_FRAME_INTERVAL = 1
        private const val CHANNEL_ID = "mirror_channel"
        private const val NOTIFICATION_ID = 1

        // Current video resolution being used for encoding
        @Volatile
        var currentVideoWidth: Int = 720
        @Volatile
        var currentVideoHeight: Int = 1280

        // Quality presets
        data class QualityPreset(val label: String, val width: Int, val height: Int, val bitrate: Int) {
            companion object {
                val P480 = QualityPreset("480p (Smooth)", 640, 480, 1_500_000)
                val P720 = QualityPreset("720p (Balanced)", 720, 1280, 4_000_000)
                val P1080 = QualityPreset("1080p (HD)", 1080, 1920, 8_000_000)
                val NATIVE = QualityPreset("Native (Max)", 0, 0, 12_000_000) // width/height set at runtime

                val ALL = listOf(P480, P720, P1080, NATIVE)
            }
        }

        // Selected quality (default 720p)
        var selectedQuality: QualityPreset = QualityPreset.P720

        // Static holder for MediaProjection data.
        private var pendingResultCode: Int? = null
        private var pendingData: Intent? = null

        // Callback when a controller connects
        var onControllerConnected: (() -> Unit)? = null
        var onControllerDisconnected: (() -> Unit)? = null

        fun setPendingProjectionData(resultCode: Int, data: Intent) {
            pendingResultCode = resultCode
            pendingData = data
        }

        private fun consumePendingProjectionData(): Pair<Int, Intent>? {
            val result = if (pendingResultCode != null && pendingData != null) {
                Pair(pendingResultCode!!, pendingData!!)
            } else null
            pendingResultCode = null
            pendingData = null
            return result
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val projectionData = consumePendingProjectionData()
        if (projectionData == null) {
            Log.e(TAG, "No pending MediaProjection data")
            stopSelf()
            return START_NOT_STICKY
        }

        val (resultCode, data) = projectionData

        startForeground(
            NOTIFICATION_ID,
            createNotification("Hosting screen..."),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
        )

        scope.launch {
            try {
                startScreenCapture(resultCode, data)
            } catch (e: Exception) {
                Log.e(TAG, "Screen capture failed", e)
                onControllerDisconnected?.invoke()
                stopSelf()
            }
        }

        return START_STICKY
    }

    private suspend fun startScreenCapture(resultCode: Int, data: Intent) {
        val mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        try {
            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get MediaProjection", e)
            return
        }

        if (mediaProjection == null) {
            Log.e(TAG, "MediaProjection is null")
            return
        }

        // Determine the actual encoding resolution from the selected quality preset.
        // CRITICAL: Match the device's actual display orientation so the captured
        // content isn't squished/stretched. If the preset dimensions don't match
        // the display aspect ratio, swap them to match orientation.
        val quality = selectedQuality
        val dm = resources.displayMetrics
        val displayWidth = dm.widthPixels
        val displayHeight = dm.heightPixels

        val (encWidth, encHeight) = if (quality.width == 0 || quality.height == 0) {
            // Native: use the device's actual display resolution
            Pair(displayWidth, displayHeight)
        } else {
            // Match the preset's LONGER side to the display's longer side
            val displayIsLandscape = displayWidth > displayHeight
            val presetIsLandscape = quality.width > quality.height
            if (displayIsLandscape == presetIsLandscape) {
                // Orientation matches — use as-is
                Pair(quality.width, quality.height)
            } else {
                // Orientation differs — swap to match display
                Pair(quality.height, quality.width)
            }
        }
        val bitrate = quality.bitrate

        Log.d(TAG, "Starting stream with quality: ${quality.label} (${encWidth}x${encHeight} @ ${bitrate / 1000000}.${(bitrate % 1000000) / 100000}Mbps)")

        // Store the actual encoding resolution IMMEDIATELY — before ServerSocket creation.
        // InputReceiverService calls syncVideoResolution() when the input client connects
        // (which happens before the video client connects). If we don't set these now,
        // InputInjector will use stale defaults and scale coordinates incorrectly.
        currentVideoWidth = encWidth
        currentVideoHeight = encHeight

        socketServer = ServerSocket(VIDEO_PORT)

        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Waiting for controller to connect on port $VIDEO_PORT...")
                clientSocket = socketServer!!.accept()
                clientSocket!!.tcpNoDelay = true

                Log.d(TAG, "Controller connected!")
                onControllerConnected?.invoke()

                val output = DataOutputStream(clientSocket!!.getOutputStream())
                output.writeInt(encWidth)
                output.writeInt(encHeight)
                output.writeInt(1)
                output.flush()

                startVideoEncoding(encWidth, encHeight, bitrate, output)
            } catch (e: Exception) {
                Log.e(TAG, "Socket error", e)
            }
        }
    }

    private fun startVideoEncoding(width: Int, height: Int, bitrate: Int, output: DataOutputStream) {
        val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
            setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
            setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL)
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            // Force periodic keyframes and repeated frames for reliable streaming
            setInteger("repeat-previous-frame-after", 500_000) // 500ms
            if (Build.VERSION.SDK_INT >= 30) {
                setInteger(MediaFormat.KEY_LOW_LATENCY, 1)
            }
        }

        encoder = try {
            MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create H.264 encoder", e)
            return
        }

        // Store the actual video resolution in a companion object so InputInjector
        // can read it for correct coordinate scaling
        currentVideoWidth = width
        currentVideoHeight = height

        // Register a callback on MediaProjection BEFORE creating VirtualDisplay.
        // IMPORTANT: registerCallback must be called on the MAIN THREAD because it
        // creates a Handler internally (otherwise: Can't create Handler inside thread
        // that has not called Looper.prepare()).
        val mp = mediaProjection!!
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        handler.post {
            mp.registerCallback(object : MediaProjection.Callback() {
                override fun onStop() {
                    Log.d(TAG, "MediaProjection stopped by system")
                }
            }, handler)
        }

        // Use synchronous API — NO setCallback()
        encoder!!.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)

        val surface = encoder!!.createInputSurface()
        val displayMetrics = resources.displayMetrics
        val densityDpi = displayMetrics.densityDpi
        Log.d(TAG, "Creating VirtualDisplay at ${width}x${height} density=$densityDpi")
        try {
            virtualDisplay = mediaProjection!!.createVirtualDisplay(
                "ScreenCapture",
                width, height, densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                surface, null, null
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create virtual display", e)
            return
        }

        encoder!!.start()
        Log.d(TAG, "Encoder started, draining output on background thread")

        // Dedicated thread: drain encoder output and send over TCP
        Thread {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
            val bufferInfo = MediaCodec.BufferInfo()
            var frameCount = 0
            var stallWarnings = 0
            var lastSyncFrameMs = 0L
            var hasForceSync = false

            // Force a sync frame immediately after encoder starts.
            // This ensures the decoder always gets a keyframe to start decoding.
            try {
                val syncParams = android.os.Bundle().apply {
                    putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0)
                }
                encoder!!.setParameters(syncParams)
                hasForceSync = true
                Log.d(TAG, "Forced initial sync frame")
            } catch (e: Exception) {
                Log.w(TAG, "Could not force initial sync frame", e)
            }

            while (encoder != null && clientSocket?.isConnected == true) {
                try {
                    val outputIndex = encoder!!.dequeueOutputBuffer(bufferInfo, 50_000) // 50ms timeout

                    when {
                        outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER -> {
                            stallWarnings++
                            if (stallWarnings % 100 == 0) { // every ~5 seconds
                                Log.w(TAG, "Encoder stall: no output for ${stallWarnings * 50}ms")
                                // Force a sync frame after 5 seconds of stall to help decoder recover
                                if (stallWarnings % 200 == 0) {
                                    try {
                                        val syncParams = android.os.Bundle().apply {
                                            putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0)
                                        }
                                        encoder!!.setParameters(syncParams)
                                        Log.d(TAG, "Forced recovery sync frame after stall")
                                    } catch (_: Exception) {}
                                }
                            }
                        }
                        outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                            Log.d(TAG, "Encoder output format changed: ${encoder!!.outputFormat}")
                            stallWarnings = 0
                        }
                        outputIndex >= 0 -> {
                            stallWarnings = 0
                            if (bufferInfo.size > 0) {
                                val buffer = encoder!!.getOutputBuffer(outputIndex) ?: continue
                                buffer.position(bufferInfo.offset)
                                val data = ByteArray(bufferInfo.size)
                                buffer.get(data)

                                frameCount++

                                // Log keyframes and codec config separately
                                val isKeyframe = (bufferInfo.flags and MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0 ||
                                    (bufferInfo.flags and MediaCodec.BUFFER_FLAG_SYNC_FRAME) != 0 ||
                                    (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0
                                if (isKeyframe) {
                                    Log.d(TAG, "Frame $frameCount: ${data.size}B KEYFRAME/CODEC_CONFIG, flags=${
                                        String.format("0x%x", bufferInfo.flags)}")
                                    lastSyncFrameMs = System.currentTimeMillis()
                                } else if (frameCount % 30 == 0) {
                                    Log.v(TAG, "Frame $frameCount: ${data.size}B, flags=${
                                        String.format("0x%x", bufferInfo.flags)}")
                                }

                                // Force periodic sync frames evey 2s in case I_FRAME_INTERVAL isn't honored
                                if (frameCount > 1 &&
                                    System.currentTimeMillis() - lastSyncFrameMs > 2000) {
                                    try {
                                        val syncParams = android.os.Bundle().apply {
                                            putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0)
                                        }
                                        encoder!!.setParameters(syncParams)
                                        lastSyncFrameMs = System.currentTimeMillis()
                                        Log.d(TAG, "Forced periodic sync frame")
                                    } catch (_: Exception) {}
                                }

                                try {
                                    output.writeInt(data.size)
                                    output.write(data)
                                    output.flush()
                                } catch (e: Exception) {
                                    Log.e(TAG, "Send error after $frameCount frames", e)
                                    break
                                }
                            }
                            encoder!!.releaseOutputBuffer(outputIndex, false)
                        }
                        else -> {
                            Log.w(TAG, "Unexpected dequeueOutputBuffer result: $outputIndex")
                        }
                    }
                } catch (e: IllegalStateException) {
                    Log.d(TAG, "Encoder stopped (IllegalStateException)")
                    break
                } catch (e: Exception) {
                    Log.e(TAG, "Encoder drain error", e)
                    break
                }
            }
            Log.d(TAG, "Encoder thread exiting, sent $frameCount frames")
        }.apply {
            name = "encoder-drain"
            start()
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "Shutting down ScreenCaptureService")

        // CRITICAL: Cancel the scope FIRST so the coroutine (which is blocked in
        // ServerSocket.accept()) is interrupted and any in-flight operations abort
        // BEFORE we release resources. Previously scope.cancel() was called LAST
        // which meant resources were released while the coroutine was still alive.
        scope.cancel()

        // Close the server socket to unblock accept() immediately
        try { socketServer?.close() } catch (_: Exception) {}
        try { clientSocket?.close() } catch (_: Exception) {}

        // CORRECT SHUTDOWN ORDER (reverse of creation):
        // 1. VirtualDisplay depends on both the encoder's input surface AND the
        //    MediaProjection. Release it FIRST to avoid dangling Surface references
        //    that can crash SurfaceFlinger / mediaserver and trigger a system reboot.
        // 2. Encoder depends on the codec hardware. Release after VirtualDisplay.
        // 3. MediaProjection is the top-level resource. Release LAST.
        try { virtualDisplay?.release() } catch (_: Exception) {}
        try { encoder?.stop() } catch (_: Exception) {}
        try { encoder?.release() } catch (_: Exception) {}
        try { mediaProjection?.stop() } catch (_: Exception) {}

        encoder = null
        virtualDisplay = null
        mediaProjection = null
        clientSocket = null
        socketServer = null
        onControllerConnected = null
        onControllerDisconnected = null

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Screen Mirroring",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(text: String) =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AndroidMirror")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setOngoing(true)
            .build()
}
