package com.mirror.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.content.pm.ServiceInfo
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetAddress
import java.net.ServerSocket

class LatencyPingService : Service() {

    private var socketServer: ServerSocket? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        private const val TAG = "LatencyPing"
        private const val PING_PORT = 9003
        private const val CHANNEL_ID = "mirror_ping_channel"
        private const val NOTIFICATION_ID = 3
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(
            NOTIFICATION_ID,
            createNotification("Latency monitoring..."),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )

        scope.launch {
            try {
                // Explicitly bind to all network interfaces (0.0.0.0) so the server
                // is reachable from any local network interface (WiFi, hotspot, USB, etc.)
                socketServer = ServerSocket(PING_PORT, 50, InetAddress.getByName("0.0.0.0"))
                Log.d(TAG, "Ping server listening on 0.0.0.0:$PING_PORT")

                while (scope.isActive) {
                    try {
                        val client = socketServer!!.accept()
                        Log.d(TAG, "Ping client connected from ${client.inetAddress.hostAddress}")
                        launch {
                            handlePingClient(client)
                        }
                    } catch (e: Exception) {
                        if (scope.isActive) Log.e(TAG, "Accept error", e)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ping server error", e)
            }
        }

        return START_STICKY
    }

    private suspend fun handlePingClient(client: java.net.Socket) {
        try {
            client.tcpNoDelay = true
            val input = DataInputStream(client.inputStream)
            val output = DataOutputStream(client.outputStream)

            while (scope.isActive && client.isConnected) {
                try {
                    val timestamp = input.readLong()
                    output.writeLong(timestamp)
                    output.flush()
                } catch (e: Exception) {
                    break
                }
            }
        } catch (_: Exception) {
        } finally {
            try {
                client.close()
            } catch (_: Exception) {}
        }
    }

    override fun onDestroy() {
        try {
            socketServer?.close()
        } catch (_: Exception) {}
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Latency Monitor",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(text: String) =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AndroidMirror Ping")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_sort_by_size)
            .setOngoing(true)
            .build()
}
