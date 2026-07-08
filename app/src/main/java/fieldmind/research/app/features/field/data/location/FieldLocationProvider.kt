package fieldmind.research.app.features.field.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import androidx.core.content.ContextCompat
import kotlinx.parcelize.Parcelize

@Parcelize
data class CapturedLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float?,
    val provider: String,
    val capturedAt: Long = System.currentTimeMillis(),
    val placeName: String? = null
) : Parcelable {
    /** Decimal coordinates only, e.g. "12.97160, 77.59456". */
    fun coordinateText(): String = "%.5f, %.5f".format(latitude, longitude)

    fun asDisplayText(): String = placeName?.takeIf { it.isNotBlank() } ?: coordinateText()
}

class FieldLocationProvider(private val context: Context) {
    fun hasAnyLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    fun lastKnownLocation(): CapturedLocation? {
        if (!hasAnyLocationPermission()) return null
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        return manager.getProviders(true)
            .mapNotNull { provider -> runCatching { manager.getLastKnownLocation(provider) }.getOrNull() }
            .maxByOrNull(Location::getTime)
            ?.toCaptured()
    }

    /**
     * Actively requests a fresh location fix and delivers it on the main thread. Falls back to the
     * most recent cached fix if no new reading arrives within [timeoutMs]. This is the reliable path
     * for capture — [lastKnownLocation] alone is frequently null on real devices.
     */
    @SuppressLint("MissingPermission")
    fun requestCurrentLocation(timeoutMs: Long = 10_000L, onResult: (CapturedLocation?) -> Unit) {
        if (!hasAnyLocationPermission()) { onResult(null); return }
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (manager == null) { onResult(null); return }

        val cached = lastKnownLocation()
        val provider = when {
            runCatching { manager.isProviderEnabled(LocationManager.GPS_PROVIDER) }.getOrDefault(false) -> LocationManager.GPS_PROVIDER
            runCatching { manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) }.getOrDefault(false) -> LocationManager.NETWORK_PROVIDER
            else -> null
        }
        if (provider == null) { onResult(cached); return }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val signal = CancellationSignal()
            val executor = ContextCompat.getMainExecutor(context)
            runCatching {
                manager.getCurrentLocation(provider, signal, executor) { loc ->
                    onResult(loc?.toCaptured() ?: cached)
                }
            }.onFailure { onResult(cached) }
            return
        }

        // API 26-29: single-shot update with a timeout fallback.
        var delivered = false
        val handler = Handler(Looper.getMainLooper())
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (delivered) return
                delivered = true
                runCatching { manager.removeUpdates(this) }
                onResult(location.toCaptured())
            }
            override fun onProviderDisabled(p: String) {}
            override fun onProviderEnabled(p: String) {}
            @Deprecated("Deprecated in Java") override fun onStatusChanged(p: String?, status: Int, extras: Bundle?) {}
        }
        runCatching { manager.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper()) }
            .onFailure { onResult(cached); return }
        handler.postDelayed({
            if (!delivered) {
                delivered = true
                runCatching { manager.removeUpdates(listener) }
                onResult(cached)
            }
        }, timeoutMs)
    }

    /**
     * Requests a fresh GPS fix with aggressive retry logic for better reliability.
     *
     * Strategy:
     * 1. First checks [lastKnownLocation] for an instant cached result
     * 2. Actively requests a fresh fix with a shorter timeout (5s)
     * 3. If null, waits 2s and retries once more
     * 4. Final fallback: returns [lastKnownLocation] again (may have appeared in the meantime)
     *
     * Each attempt state is reported via [onAttempt] so the UI can show meaningful progress.
     * Delivers the final result via [onResult], always on the main thread.
     */
    @SuppressLint("MissingPermission")
    fun requestCurrentLocationWithRetry(
        onAttempt: (attempt: Int, totalAttempts: Int, status: String) -> Unit = { _, _, _ -> },
        onResult: (CapturedLocation?) -> Unit
    ) {
        if (!hasAnyLocationPermission()) {
            onAttempt(0, 3, "Location permission not granted")
            onResult(null)
            return
        }

        // Attempt 0: immediate cached result
        val cached = lastKnownLocation()
        if (cached != null) {
            onAttempt(0, 3, "Using cached location (${cached.accuracyMeters?.toInt() ?: "?"}m)")
            onResult(cached)
            return
        }

        onAttempt(1, 3, "Acquiring GPS signal…")
        requestCurrentLocation(timeoutMs = 5_000L) { loc ->
            if (loc != null) {
                onResult(loc)
                return@requestCurrentLocation
            }

            // Attempt 2: retry after a brief delay for better GPS lock
            onAttempt(2, 3, "GPS weak — retrying…")
            android.os.Handler(Looper.getMainLooper()).postDelayed({
                requestCurrentLocation(timeoutMs = 5_000L) { retryLoc ->
                    if (retryLoc != null) {
                        onResult(retryLoc)
                    } else {
                        // Final fallback: check lastKnownLocation one more time
                        val finalCached = lastKnownLocation()
                        if (finalCached != null) {
                            onAttempt(3, 3, "Using cached location (${finalCached.accuracyMeters?.toInt() ?: "?"}m)")
                            onResult(finalCached)
                        } else {
                            onAttempt(3, 3, "GPS unavailable after 2 attempts")
                            onResult(null)
                        }
                    }
                }
            }, 2_000L)
        }
    }

    /**
     * Checks whether the device's GPS (or any location provider) is currently enabled.
     * Returns false when GPS is turned off in system settings, even if location
     * permission has been granted.
     */
    @SuppressLint("MissingPermission")
    fun isGpsEnabled(): Boolean {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
        return runCatching { manager.isProviderEnabled(LocationManager.GPS_PROVIDER) }.getOrDefault(false) ||
            runCatching { manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) }.getOrDefault(false)
    }

    /**
     * Creates an Intent that opens the system Location Settings page so the user
     * can enable GPS / location services.
     */
    fun openLocationSettingsIntent(): android.content.Intent =
        android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)

    private fun Location.toCaptured(): CapturedLocation =
        CapturedLocation(latitude, longitude, accuracy.takeIf { it > 0f }, provider ?: "device")

    /**
     * Reverse-geocodes coordinates into a short, human-readable place name (e.g.
     * "Bengaluru"). Delivers on the main thread; returns null if geocoding is
     * unavailable or fails. Geocoding may require connectivity, so callers must handle null.
     */
    fun resolvePlaceName(latitude: Double, longitude: Double, onResult: (String?) -> Unit) {
        if (!android.location.Geocoder.isPresent()) { onResult(null); return }
        val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            runCatching {
                geocoder.getFromLocation(latitude, longitude, 1) { results ->
                    onResult(results.firstOrNull()?.let(::formatPlace))
                }
            }.onFailure { onResult(null) }
        } else {
            Thread {
                val name = runCatching {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()?.let(::formatPlace)
                }.getOrNull()
                Handler(Looper.getMainLooper()).post { onResult(name) }
            }.start()
        }
    }

    /**
     * Formats an [android.location.Address] into a concise place name.
     *
     * First tries [locality] (town/village) alone — the most recognizable part.
     * If locality is not available, falls back to [subAdminArea] (block/district).
     * If both are available and different (common in rural India where the geocoder
     * doesn't return a locality), combines them as "Locality, Block" for clarity.
     */
    private fun formatPlace(address: android.location.Address): String {
        val locality = address.locality?.trim()
        val subArea = address.subAdminArea?.trim()
        val adminArea = address.adminArea?.trim()

        // Best case: we have a town/village name
        if (!locality.isNullOrBlank()) {
            // If both locality and subAdminArea exist and are different, show both
            // e.g. "Hosur, Krishnagiri" — helps users distinguish between
            // the village and the block they're actually in.
            if (!subArea.isNullOrBlank() && !subArea.equals(locality, ignoreCase = true)) {
                return "$locality, $subArea"
            }
            return locality
        }

        // Fallback to block/district
        if (!subArea.isNullOrBlank()) return subArea

        // Fallback to state
        if (!adminArea.isNullOrBlank()) return adminArea

        // Last resort: full address line, stripped of street/feature prefix
        return address.getAddressLine(0)?.let { line ->
            val parts = line.split(",")
            if (parts.size > 1) parts.drop(1).joinToString(",").trim() else line
        } ?: "Unknown place"
    }
}
