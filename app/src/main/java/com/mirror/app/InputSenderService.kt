package com.mirror.app

import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.util.Log
import android.view.MotionEvent
import java.io.DataOutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.atomic.AtomicInteger

class InputSenderService(
    private val targetIp: String,
    private val port: Int,
    private val onConnected: (() -> Unit)? = null,
    private val onError: ((String) -> Unit)? = null,
    /** Called when the target sends its screen dimensions via handshake. */
    private val onHandshake: ((targetWidth: Int, targetHeight: Int) -> Unit)? = null
) {

    @Volatile
    private var socket: Socket? = null
    @Volatile
    private var output: DataOutputStream? = null
    @Volatile
    private var isRunning = false
    private var ioHandler: Handler? = null
    private var ioThread: HandlerThread? = null

    // ── MOVE event coalescing ──
    private val pendingMoveX = FloatArray(1)
    private val pendingMoveY = FloatArray(1)
    private val pendingMovePressure = FloatArray(1)
    private val pendingMoveCount = AtomicInteger(0)

    /** Target screen dimensions received from handshake. */
    @Volatile
    var targetWidth: Int = 0
        private set
    @Volatile
    var targetHeight: Int = 0
        private set

    companion object {
        private const val TAG = "InputSender"
        private const val CONNECT_TIMEOUT_MS = 5000

        // Protocol types
        private const val TYPE_TOUCH = 0x01
        private const val TYPE_KEY = 0x02
        private const val TYPE_SCROLL = 0x03
        private const val TYPE_CURSOR = 0x04
        private const val TYPE_KEY_CHAR = 0x05 // keyCode + unicodeChar for text input
    }

    /**
     * Runnable posted to the background HandlerThread for each touch event.
     * Using a HandlerThread ensures all socket I/O happens off the main thread,
     * avoiding NetworkOnMainThreadException on Android 9+.
     */
    private class TouchEventRunnable(
        private val outputRef: () -> DataOutputStream?,
        private val x: Float,
        private val y: Float,
        private val pressure: Float,
        private val action: Int
    ) : Runnable {
        override fun run() {
            try {
                val out = outputRef() ?: return
                synchronized(out) {
                    out.writeByte(TYPE_TOUCH)
                    out.writeFloat(x)
                    out.writeFloat(y)
                    out.writeFloat(pressure)
                    out.writeInt(action)
                    out.flush()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Send touch error", e)
            }
        }
    }

    private class KeyEventRunnable(
        private val outputRef: () -> DataOutputStream?,
        private val keyCode: Int
    ) : Runnable {
        override fun run() {
            try {
                val out = outputRef() ?: return
                synchronized(out) {
                    out.writeByte(TYPE_KEY)
                    out.writeInt(keyCode)
                    out.flush()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Send key error", e)
            }
        }
    }

    /** Runnable for sending a keyCode + unicode character (for text input). */
    private class KeyCharEventRunnable(
        private val outputRef: () -> DataOutputStream?,
        private val keyCode: Int,
        private val unicodeChar: Int
    ) : Runnable {
        override fun run() {
            try {
                val out = outputRef() ?: return
                synchronized(out) {
                    out.writeByte(TYPE_KEY_CHAR)
                    out.writeInt(keyCode)
                    out.writeInt(unicodeChar)
                    out.flush()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Send key+char error", e)
            }
        }
    }

    private class ScrollEventRunnable(
        private val outputRef: () -> DataOutputStream?,
        private val dx: Float,
        private val dy: Float
    ) : Runnable {
        override fun run() {
            try {
                val out = outputRef() ?: return
                synchronized(out) {
                    out.writeByte(TYPE_SCROLL)
                    out.writeFloat(dx)
                    out.writeFloat(dy)
                    out.flush()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Send scroll error", e)
            }
        }
    }

    private class CursorEventRunnable(
        private val outputRef: () -> DataOutputStream?,
        private val x: Float,
        private val y: Float
    ) : Runnable {
        override fun run() {
            try {
                val out = outputRef() ?: return
                synchronized(out) {
                    out.writeByte(TYPE_CURSOR)
                    out.writeFloat(x)
                    out.writeFloat(y)
                    out.flush()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Send cursor error", e)
            }
        }
    }

    /**
     * Runnable that sends the LATEST coalesced MOVE position.
     * Uses shared float arrays updated by [sendTouchEvent] on the calling thread.
     */
    private class CoalescedMoveRunnable(
        private val outputRef: () -> DataOutputStream?,
        private val xRef: FloatArray,
        private val yRef: FloatArray,
        private val pressureRef: FloatArray,
        private val pendingCount: AtomicInteger
    ) : Runnable {
        override fun run() {
            try {
                synchronized(xRef) {
                    pendingCount.set(0)
                    val out = outputRef()
                    if (out == null) return@synchronized
                    synchronized(out) {
                        out.writeByte(TYPE_TOUCH)
                        out.writeFloat(xRef[0])
                        out.writeFloat(yRef[0])
                        out.writeFloat(pressureRef[0])
                        out.writeInt(MotionEvent.ACTION_MOVE)
                        out.flush()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Send coalesced move error", e)
            }
        }
    }

    fun start() {
        isRunning = true
        // Create a dedicated background thread for all socket I/O
        ioThread = HandlerThread("input-sender-io", Process.THREAD_PRIORITY_DEFAULT).apply {
            start()
        }
        ioHandler = Handler(ioThread!!.looper)

        // Connection still on its own thread (non-blocking for UI)
        Thread {
            try {
                Log.d(TAG, "Connecting to $targetIp:$port (timeout: ${CONNECT_TIMEOUT_MS}ms)...")
                socket = Socket()
                socket!!.connect(InetSocketAddress(targetIp, port), CONNECT_TIMEOUT_MS)
                socket!!.tcpNoDelay = true
                output = DataOutputStream(socket!!.getOutputStream())

                // Read handshake: target sends screen dimensions first
                val input = java.io.DataInputStream(socket!!.getInputStream())
                val w = input.readInt()
                val h = input.readInt()
                targetWidth = w
                targetHeight = h
                Log.d(TAG, "Input sender connected, target screen: ${w}x${h}")
                onHandshake?.invoke(w, h)
                onConnected?.invoke()
            } catch (e: java.net.SocketTimeoutException) {
                Log.e(TAG, "Input sender connection timed out")
                onError?.invoke("Input connection timed out")
                isRunning = false
            } catch (e: Exception) {
                Log.e(TAG, "Input sender connect error", e)
                onError?.invoke(e.message ?: "Input connection failed")
                isRunning = false
            }
        }.apply {
            name = "input-sender-connect"
            start()
        }
    }

    /**
     * Queue a touch event for sending on the background I/O thread.
     * This is safe to call from the main/UI thread.
     *
     * MOVE events are coalesced: rapid MOVE events overwrite the pending
     * position, and only the latest is sent when the handler processes
     * the queue. This prevents flooding the socket with intermediate
     * positions that the target will never use.
     */
    fun sendTouchEvent(x: Float, y: Float, pressure: Float, action: Int) {
        if (action == MotionEvent.ACTION_MOVE) {
            // Coalesce move events — only send the latest position.
            // pendingMoveCount.getAndIncrement() returns 0 when no runnable
            // is queued, in which case we post one. The runnable resets
            // pendingMoveCount to 0 when it runs, allowing the next batch.
            synchronized(pendingMoveX) {
                pendingMoveX[0] = x
                pendingMoveY[0] = y
                pendingMovePressure[0] = pressure
                if (pendingMoveCount.getAndIncrement() == 0) {
                    ioHandler?.post(CoalescedMoveRunnable(
                        { output },
                        pendingMoveX, pendingMoveY, pendingMovePressure, pendingMoveCount
                    ))
                }
            }
        } else {
            // DOWN and UP are sent immediately
            ioHandler?.post(TouchEventRunnable({ output }, x, y, pressure, action))
        }
    }

    /**
     * Queue a key event for sending on the background I/O thread.
     * If unicodeChar > 0, sends the character along with the keyCode
     * so the target can inject proper text input.
     * This is safe to call from the main/UI thread.
     */
    fun sendKeyEvent(keyCode: Int, unicodeChar: Int = 0) {
        if (unicodeChar > 0) {
            ioHandler?.post(KeyCharEventRunnable({ output }, keyCode, unicodeChar))
        } else {
            ioHandler?.post(KeyEventRunnable({ output }, keyCode))
        }
    }

    /**
     * Queue a scroll event for sending on the background I/O thread.
     */
    fun sendScrollEvent(dy: Float) {
        ioHandler?.post(ScrollEventRunnable({ output }, 0f, dy))
    }

    /**
     * Queue a cursor position update (hover) — updates the cursor on the target
     * without injecting any touch event.
     */
    fun sendCursorPosition(x: Float, y: Float) {
        ioHandler?.post(CursorEventRunnable({ output }, x, y))
    }

    fun stop() {
        isRunning = false
        ioHandler?.removeCallbacksAndMessages(null)
        ioThread?.quitSafely()
        try {
            socket?.close()
        } catch (_: Exception) {}
        output = null
        socket = null
        ioHandler = null
        ioThread = null
    }

    fun isOutputReady(): Boolean = output != null
}
