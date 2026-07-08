package fieldmind.research.app.infrastructure.worker

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import fieldmind.research.app.activities.MainActivity
import fieldmind.research.app.R

/**
 * Weather Alert Worker — sends notifications when weather conditions change significantly.
 * Compares current conditions with previous snapshot and alerts on thresholds (storms, extreme temps, etc.).
 */
class FieldMindWeatherAlertWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "fieldmind_weather_alert_work"
        private const val CHANNEL_ID = "fieldmind_weather_alerts"
        private const val NOTIFICATION_ID = 8101
        private const val PREFS_NAME = "fieldmind_weather_alerts"
        private const val KEY_LAST_CODE = "last_weather_code"
        private const val KEY_LAST_TEMP = "last_temp"
    }

    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return@runCatching Result.success()

        ensureChannel()

        val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastCode = prefs.getInt(KEY_LAST_CODE, -1)
        val lastTemp = prefs.getString(KEY_LAST_TEMP, "") ?: ""

        val weatherPrefs = applicationContext.getSharedPreferences("fieldmind_weather_widget", Context.MODE_PRIVATE)
        val currentCode = weatherPrefs.getInt("weather_code", 0)
        val currentTemp = weatherPrefs.getString("weather_temp", "") ?: ""

        val alertMessage = checkWeatherAlert(lastCode, currentCode, lastTemp, currentTemp)

        if (alertMessage != null) {
            showAlert(alertMessage)
            prefs.edit()
                .putInt(KEY_LAST_CODE, currentCode)
                .putString(KEY_LAST_TEMP, currentTemp)
                .apply()
        } else {
            // Just update last known values
            prefs.edit()
                .putInt(KEY_LAST_CODE, currentCode)
                .putString(KEY_LAST_TEMP, currentTemp)
                .apply()
        }

        Result.success()
    }.getOrElse { Result.retry() }

    private fun checkWeatherAlert(lastCode: Int, currentCode: Int, lastTemp: String, currentTemp: String): String? {
        // Storm alert
        if (currentCode in 200..232 && (lastCode !in 200..232 || lastCode == -1)) {
            return "\\u26A1 Thunderstorm alert in your area. Stay safe and check conditions before heading out."
        }
        // Heavy rain alert
        if (currentCode in 500..531 && currentCode >= 502 && (lastCode !in 500..531 || lastCode == -1)) {
            return "\\uD83C\\uDF27 Heavy rain expected. Field conditions may be slippery."
        }
        // Snow alert
        if (currentCode in 600..622 && (lastCode !in 600..622 || lastCode == -1)) {
            return "\\u2744 Snowfall detected. Dress warmly for field observations."
        }
        // Extreme heat (above 35°C/95°F)
        if (currentTemp.isNotBlank() && lastTemp.isNotBlank()) {
            val current = currentTemp.toFloatOrNull()
            val previous = lastTemp.toFloatOrNull()
            if (current != null && current >= 35 && (previous == null || previous < 35)) {
                return "\\uD83C\\uDF21 Extreme heat (${currentTemp}\\u00B0C). Limit outdoor activity and stay hydrated."
            }
            // Significant temperature drop (>10°C)
            if (current != null && previous != null && (previous - current) >= 10) {
                return "\\uD83C\\uDF26 Temperature dropping rapidly (${currentTemp}\\u00B0C). Dress accordingly."
            }
        }
        return null
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = applicationContext.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(CHANNEL_ID, "Weather Alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Significant weather changes and alerts for field safety"
                enableVibration(true)
                enableLights(true)
            }
            manager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("MissingPermission")
    private fun showAlert(message: String) {
        val intent = PendingIntent.getActivity(
            applicationContext, 0,
            Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(MainActivity.EXTRA_FIELDMIND_DESTINATION, "weather")
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("FieldMind Weather Alert")
            .setContentText(message)
            .setColor(0xFF1F6B4C.toInt())
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(intent)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, notification)
    }
}
