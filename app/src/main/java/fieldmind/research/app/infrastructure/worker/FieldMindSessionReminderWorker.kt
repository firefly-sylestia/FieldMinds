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
import fieldmind.research.app.features.field.data.database.FieldMindDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Research Session Reminder Worker — sends periodic prompts encouraging research sessions.
 * Uses time-of-day greetings and personalizes based on recent activity.
 * Respects user's reminder settings.
 */
class FieldMindSessionReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "fieldmind_session_reminder_work"
        private const val CHANNEL_ID = "fieldmind_session_reminders"
        private const val NOTIFICATION_ID = 8301
        private const val PREFS_NAME = "fieldmind_session_reminder"
        private const val KEY_LAST_PROMPT_DATE = "last_prompt_date"
        private const val KEY_PROMPT_COUNT = "prompt_count"
    }

    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return@runCatching Result.success()

        ensureChannel()

        // Don't send more than once per day
        val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastPromptDate = prefs.getString(KEY_LAST_PROMPT_DATE, "") ?: ""
        if (lastPromptDate == today) return@runCatching Result.success()

        // Check if user already made observations today
        val dao = FieldMindDatabase.getInstance(applicationContext).fieldMindDao()
        val observations = dao.observeObservations().let { flow ->
            var result = emptyList<fieldmind.research.app.features.field.data.database.entity.ObservationEntity>()
            kotlinx.coroutines.flow.first(flow) { list -> result = list; true }
            result
        }
        val todayCount = observations.count { it.date == today }
        if (todayCount > 0) return@runCatching Result.success()

        // Time-of-day greeting
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 6..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Quick research break"
        }

        val promptCount = prefs.getInt(KEY_PROMPT_COUNT, 0) + 1
        val message = when (promptCount % 3) {
            0 -> "$greeting! How about a quick field observation? Nature has stories waiting."
            1 -> "$greeting! Time for a research session. What species will you document today?"
            else -> "$greeting! Your field notebook is ready. Capture one observation to keep your research going."
        }

        showSessionPrompt(message)

        prefs.edit()
            .putString(KEY_LAST_PROMPT_DATE, today)
            .putInt(KEY_PROMPT_COUNT, promptCount)
            .apply()

        Result.success()
    }.getOrElse { Result.retry() }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = applicationContext.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(CHANNEL_ID, "Research Session Reminders", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Friendly prompts to conduct research sessions and capture observations"
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("MissingPermission")
    private fun showSessionPrompt(message: String) {
        val openIntent = PendingIntent.getActivity(
            applicationContext, 0,
            Intent(applicationContext, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val recordIntent = PendingIntent.getActivity(
            applicationContext, 1,
            Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(MainActivity.EXTRA_FIELDMIND_DESTINATION, "field_capture")
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("FieldMind Research Session")
            .setContentText(message)
            .setColor(0xFF1F6B4C.toInt())
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(openIntent)
            .addAction(android.R.drawable.ic_menu_camera, "Record", recordIntent)
            .addAction(android.R.drawable.ic_menu_compass, "Open", openIntent)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, notification)
    }
}
