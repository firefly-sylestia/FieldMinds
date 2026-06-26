package com.mirror.app

import android.util.Log
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketException

class LatencyChecker(
    private val targetIp: String,
    private val port: Int,
    private val onLatencyUpdate: (Long) -> Unit,
    private val onError: ((String) -> Unit)? = null
) {

    private var socket: Socket? = null
    private var pingThread: Thread? = null
    @Volatile
    private var isRunning = false

    companion object {
        private const val TAG = "LatencyChecker"
        private const val PING_INTERVAL_MS = 2000L
        private const val CONNECT_TIMEOUT_MS = 3000
        private const val INITIAL_RECONNECT_DELAY_MS = 2000L
        private const val MAX_RECONNECT_DELAY_MS = 10000L
    }

    fun start() {
        isRunning = true
        pingThread = Thread {
            var reconnectDelay = INITIAL_RECONNECT_DELAY_MS

            while (isRunning) {
                try {
                    Log.d(TAG, "Connecting latency ping to $targetIp:$port...")
                    socket = Socket()
                    socket!!.connect(InetSocketAddress(targetIp, port), CONNECT_TIMEOUT_MS)
                    socket!!.tcpNoDelay = true
                    socket!!.soTimeout = CONNECT_TIMEOUT_MS
                    val input = DataInputStream(socket!!.inputStream)
                    val output = DataOutputStream(socket!!.getOutputStream())
                    reconnectDelay = INITIAL_RECONNECT_DELAY_MS // reset on successful connect

                    Log.d(TAG, "Latency checker connected")

                    while (isRunning && socket!!.isConnected && !socket!!.isClosed) {
                        try {
                            val startTime = System.currentTimeMillis()
                            output.writeLong(startTime)
                            output.flush()

                            val echo = input.readLong()
                            val latency = System.currentTimeMillis() - startTime
                            onLatencyUpdate(latency)

                            Thread.sleep(PING_INTERVAL_MS)
                        } catch (e: java.io.EOFException) {
                            Log.w(TAG, "Ping connection closed by server, reconnecting...")
                            break
                        } catch (e: java.net.SocketTimeoutException) {
                            Log.w(TAG, "Ping read timed out")
                        } catch (e: SocketException) {
                            if (isRunning) {
                                Log.w(TAG, "Ping socket error, reconnecting...", e)
                            }
                            break
                        } catch (e: Exception) {
                            Log.e(TAG, "Ping error", e)
                            break
                        }
                    }

                    try {
                        socket?.close()
                    } catch (_: Exception) {}
                    socket = null
                } catch (e: java.net.SocketTimeoutException) {
                    Log.e(TAG, "Latency ping connection timed out to $targetIp:$port")
                    onError?.invoke("Latency ping timed out")
                } catch (e: Exception) {
                    Log.e(TAG, "Latency checker connect error to $targetIp:$port", e)
                    onError?.invoke(e.message ?: "Latency connection failed")
                }

                if (isRunning) {
                    Log.d(TAG, "Reconnecting latency ping in ${reconnectDelay}ms...")
                    Thread.sleep(reconnectDelay)
                    reconnectDelay = (reconnectDelay * 2).coerceAtMost(MAX_RECONNECT_DELAY_MS)
                }
            }
        }.apply {
            name = "latency-checker"
            start()
        }
    }

    fun stop() {
        isRunning = false
        try {
            socket?.close()
        } catch (_: Exception) {}
        pingThread?.join(1000)
        socket = null
    }
}
