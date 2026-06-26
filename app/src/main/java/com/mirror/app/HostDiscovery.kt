package com.mirror.app

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

/**
 * UDP beacon-based host discovery for finding nearby AndroidMirror hosts.
 *
 * Two roles:
 * - **Host (broadcaster)**: Periodically sends a UDP broadcast packet containing
 *   the device name and IP so nearby controllers can discover it.
 * - **Controller (listener)**: Listens for UDP beacon packets on a background
 *   thread and reports discovered hosts via [onHostDiscovered].
 *
 * The beacon format is a simple text string:
 *   `ANDROIDMIRROR\n<device_name>\n<ip_address>`
 */
class HostDiscovery(private val context: Context) {

    companion object {
        private const val TAG = "HostDiscovery"
        private const val DISCOVERY_PORT = 9005
        private const val BEACON_INTERVAL_MS = 2000L
        private const val BUFFER_SIZE = 1024
        private const val MAGIC_PREFIX = "ANDROIDMIRROR"
    }

    /** Represents a discovered host on the network. */
    data class DiscoveredHost(
        val name: String,
        val ip: String
    )

    // ── Broadcasting (host side) ──

    private var broadcastThread: Thread? = null
    @Volatile
    private var isBroadcasting = false

    /**
     * Start broadcasting UDP beacons so controllers can discover this device.
     * Call when hosting begins.
     */
    fun startBroadcasting(deviceName: String, localIp: String) {
        if (isBroadcasting) return
        isBroadcasting = true

        broadcastThread = Thread {
            val message = "$MAGIC_PREFIX\n$deviceName\n$localIp"
            val data = message.toByteArray(Charsets.UTF_8)

            val socket = DatagramSocket()
            try {
                socket.broadcast = true
                socket.reuseAddress = true

                val broadcastAddresses = getBroadcastAddresses()

                while (isBroadcasting && !Thread.currentThread().isInterrupted) {
                    for (addr in broadcastAddresses) {
                        try {
                            val packet = DatagramPacket(data, data.size, addr, DISCOVERY_PORT)
                            socket.send(packet)
                        } catch (_: Exception) {}
                    }
                    try {
                        Thread.sleep(BEACON_INTERVAL_MS)
                    } catch (_: InterruptedException) {
                        Thread.currentThread().interrupt()
                        break
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Broadcast error", e)
            } finally {
                socket.close()
            }
        }.apply { name = "discovery-broadcast"; start() }
    }

    /** Stop broadcasting. Call when hosting ends. */
    fun stopBroadcasting() {
        isBroadcasting = false
        broadcastThread?.interrupt()
        broadcastThread = null
    }

    // ── Listening (controller side) ──

    private var listenThread: Thread? = null
    @Volatile
    private var isListening = false

    /** Callback invoked when a host is discovered. */
    var onHostDiscovered: ((DiscoveredHost) -> Unit)? = null

    /**
     * Start listening for UDP beacon broadcasts from nearby hosts.
     * Call when the controller connect screen is visible.
     */
    fun startListening() {
        if (isListening) return
        isListening = true

        // Acquire multicast lock so the device can receive broadcasts
        val multicastLock: WifiManager.MulticastLock? = try {
            val wifi = context.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as WifiManager
            wifi.createMulticastLock(TAG).apply { acquire() }
        } catch (_: Exception) { null }

        listenThread = Thread {
            try {
                val socket = DatagramSocket(DISCOVERY_PORT)
                socket.broadcast = true
                socket.reuseAddress = true
                socket.soTimeout = 1000 // 1s timeout so we can check isListening

                val buffer = ByteArray(BUFFER_SIZE)

                while (isListening) {
                    try {
                        val packet = DatagramPacket(buffer, buffer.size)
                        socket.receive(packet)

                        val message = String(
                            packet.data, packet.offset, packet.length,
                            Charsets.UTF_8
                        )

                        if (message.startsWith(MAGIC_PREFIX)) {
                            val parts = message.split("\n")
                            if (parts.size >= 3) {
                                val name = parts[1].trim()
                                val ip = parts[2].trim()
                                if (ip.isNotEmpty()) {
                                    val host = DiscoveredHost(name, ip)
                                    Log.d(TAG, "Discovered: $name @ $ip")
                                    onHostDiscovered?.invoke(host)
                                }
                            }
                        }
                    } catch (_: java.net.SocketTimeoutException) {
                        // Expected timeout — loop checks isListening
                    }
                }

                socket.close()
            } catch (e: Exception) {
                Log.e(TAG, "Listen error", e)
            } finally {
                try { multicastLock?.release() } catch (_: Exception) {}
            }
        }.apply { name = "discovery-listen"; start() }
    }

    /** Stop listening. Call when leaving the connect screen. */
    fun stopListening() {
        isListening = false
        listenThread?.interrupt()
        listenThread = null
    }

    // ── Helpers ──

    /**
     * Get broadcast addresses for all non-loopback network interfaces.
     * Tries both the specific subnet broadcast and the global broadcast.
     */
    private fun getBroadcastAddresses(): List<InetAddress> {
        val addrs = mutableListOf<InetAddress>()
        try {
            // Always include the global broadcast address
            addrs.add(InetAddress.getByName("255.255.255.255"))

            java.net.NetworkInterface.getNetworkInterfaces()?.iterator()?.forEach { ni ->
                if (!ni.isLoopback && ni.isUp) {
                    ni.interfaceAddresses.forEach { ifaceAddr ->
                        val broadcast = ifaceAddr.broadcast
                        if (broadcast != null && !addrs.contains(broadcast)) {
                            addrs.add(broadcast)
                        }
                    }
                }
            }
        } catch (_: Exception) {}
        return addrs
    }
}
