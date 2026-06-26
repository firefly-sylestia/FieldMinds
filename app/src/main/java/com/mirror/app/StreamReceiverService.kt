package com.mirror.app

import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Build
import android.os.Process
import android.util.Log
import android.view.Surface
import java.io.DataInputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.atomic.AtomicLong

class StreamReceiverService(
    private val targetIp: String,
    private val port: Int,
    private val mirrorView: MirrorSurfaceView,
    private val onConnected: (() -> Unit)? = null,
    private val onError: ((String) -> Unit)? = null,
    private val onFpsUpdate: ((Int) -> Unit)? = null
) {

    private var socket: Socket? = null
    private var decoder: MediaCodec? = null
    private var decoderThread: Thread? = null
    @Volatile
    private var isRunning = false
    private val frameCount = AtomicLong(0)
    private var fpsThread: Thread? = null

    companion object {
        private const val TAG = "StreamReceiver"
        private const val CONNECT_TIMEOUT_MS = 5000
    }

    fun start() {
        isRunning = true

        Thread {
            try {
                Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)

                Log.d(TAG, "Connecting to $targetIp:$port (timeout: ${CONNECT_TIMEOUT_MS}ms)...")
                socket = Socket()
                socket!!.connect(InetSocketAddress(targetIp, port), CONNECT_TIMEOUT_MS)
                socket!!.tcpNoDelay = true
                socket!!.setPerformancePreferences(0, 1, 0)
                socket!!.receiveBufferSize = 65536

                val input = DataInputStream(socket!!.inputStream)

                // Read handshake
                val screenWidth = input.readInt()
                val screenHeight = input.readInt()
                val protocolVersion = input.readInt()
                Log.d(TAG, "Handshake received: ${screenWidth}x${screenHeight} v$protocolVersion")

                mirrorView.setTargetResolution(screenWidth, screenHeight)

                // Notify that connection is established (before video starts)
                onConnected?.invoke()

                startVideoDecoding(screenWidth, screenHeight, input)

            } catch (e: java.net.SocketTimeoutException) {
                Log.e(TAG, "Connection timed out after ${CONNECT_TIMEOUT_MS}ms")
                onError?.invoke("Connection timed out — check IP and WiFi")
                isRunning = false
            } catch (e: Exception) {
                Log.e(TAG, "Connection/stream error", e)
                onError?.invoke(e.message ?: "Unknown connection error")
                isRunning = false
            }
        }.apply {
            name = "stream-receiver"
            start()
        }
    }

    private fun startVideoDecoding(width: Int, height: Int, input: DataInputStream) {
        val format = MediaFormat.createVideoFormat(
            MediaFormat.MIMETYPE_VIDEO_AVC, width, height
        ).apply {
            if (Build.VERSION.SDK_INT >= 30) {
                setInteger(MediaFormat.KEY_LOW_LATENCY, 1)
            }
        }

        // Wait for SurfaceView surface to be ready using CountDownLatch (efficient, no polling)
        val startWait = System.currentTimeMillis()
        val surface = mirrorView.waitForSurface(5000)
        if (surface == null || !surface.isValid) {
            Log.e(TAG, "Surface never became valid after 5s")
            onError?.invoke("Video surface not ready — try reconnecting")
            return
        }
        Log.d(TAG, "Surface ready after ${System.currentTimeMillis() - startWait}ms")

        // Use PURELY synchronous API — NO setCallback()
        decoder = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        decoder!!.configure(format, surface, null, 0)
        decoder!!.start()

        Log.d(TAG, "Decoder started, beginning decode loop")

        // FPS counting thread
        fpsThread = Thread {
            while (isRunning) {
                try {
                    Thread.sleep(1000)
                    val fps = frameCount.getAndSet(0)
                    onFpsUpdate?.invoke(fps.toInt())
                } catch (_: InterruptedException) {
                    break
                }
            }
        }.apply {
            name = "fps-counter"
            start()
        }

        // Single thread: read NAL → feed decoder → drain decoded frames → render
        decoderThread = Thread {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)

            val bufferInfo = MediaCodec.BufferInfo()
            var totalNalRead = 0
            var frameStallWarnings = 0

            while (isRunning) {
                try {
                    // ── 1. Read one NAL unit from socket ──
                    val size = input.readInt()
                    if (size <= 0 || size > 1_000_000) {
                        Log.w(TAG, "Invalid NAL size: $size")
                        continue
                    }

                    val nal = ByteArray(size)
                    input.readFully(nal)
                    totalNalRead++

                    if (totalNalRead <= 3) {
                        Log.d(TAG, "NAL #$totalNalRead: ${size}B, first bytes=${nal.take(16).joinToString(" ") { String.format("%02x", it) }}")
                    }

                    // ── 2. Feed to decoder ──
                    val inputIndex = decoder!!.dequeueInputBuffer(50_000) // 50ms timeout
                    if (inputIndex >= 0) {
                        val inBuf = decoder!!.getInputBuffer(inputIndex)!!
                        inBuf.clear()
                        inBuf.put(nal)
                        decoder!!.queueInputBuffer(
                            inputIndex, 0, nal.size,
                            System.nanoTime() / 1000,
                            0
                        )
                    } else {
                        Log.w(TAG, "Dropping NAL #$totalNalRead (no input buffer avail after 50ms)")
                    }

                    // ── 3. Drain all available output frames ──
                    var outputIndex = decoder!!.dequeueOutputBuffer(bufferInfo, 0)
                    while (outputIndex >= 0) {
                        frameStallWarnings = 0

                        if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                            Log.d(TAG, "End of stream")
                            break
                        }

                        // Render to Surface
                        decoder!!.releaseOutputBuffer(outputIndex, true)
                        frameCount.incrementAndGet()
                        val totalFrames = frameCount.get()
                        if (totalFrames <= 3 || totalFrames % 30 == 0L) {
                            Log.d(TAG, "Frame #$totalFrames rendered, flags=${bufferInfo.flags}, size=${bufferInfo.size}")
                        }

                        // Try next output (non-blocking)
                        outputIndex = decoder!!.dequeueOutputBuffer(bufferInfo, 0)
                    }

                    // Handle decoder output format changes (e.g., device rotation)
                    if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        val newFormat = decoder!!.outputFormat
                        val newWidth = newFormat.getInteger(MediaFormat.KEY_WIDTH)
                        val newHeight = newFormat.getInteger(MediaFormat.KEY_HEIGHT)
                        Log.d(TAG, "Output format changed: ${newWidth}x${newHeight} (was ${width}x${height})")
                        if (newWidth != width || newHeight != height) {
                            mirrorView.setTargetResolution(newWidth, newHeight)
                            Log.d(TAG, "Updated MirrorSurfaceView target to ${newWidth}x${newHeight}")
                        }
                    } else if (outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        frameStallWarnings++
                        if (frameStallWarnings % 100 == 0) {
                            Log.w(TAG, "Decoder output stall: fed $totalNalRead NALs, rendered ${frameCount.get()} frames")
                        }
                    }

                } catch (e: java.io.EOFException) {
                    Log.d(TAG, "Socket closed by target after $totalNalRead NALs, ${frameCount.get()} frames")
                    break
                } catch (e: Exception) {
                    Log.e(TAG, "Decode loop error after $totalNalRead NALs, ${frameCount.get()} frames", e)
                    break
                }
            }

            // Log final stats
            Log.d(TAG, "Decode loop ended: $totalNalRead NALs → ${frameCount.get()} frames")

            // Drain any remaining output before stopping
            try {
                var outputIndex = decoder!!.dequeueOutputBuffer(bufferInfo, 0)
                while (outputIndex >= 0) {
                    decoder!!.releaseOutputBuffer(outputIndex, true)
                    frameCount.incrementAndGet()
                    outputIndex = decoder!!.dequeueOutputBuffer(bufferInfo, 0)
                }
            } catch (_: Exception) {}
        }.apply {
            name = "decoder-loop"
            start()
        }
    }

    fun stop() {
        isRunning = false
        try {
            decoder?.stop()
            decoder?.release()
        } catch (_: Exception) {}
        try {
            socket?.close()
        } catch (_: Exception) {}
        decoderThread?.join(2000)
        fpsThread?.join(2000)
        decoder = null
        socket = null
    }

    fun isSurfaceReady(): Boolean = mirrorView.isSurfaceReallyValid()
}
