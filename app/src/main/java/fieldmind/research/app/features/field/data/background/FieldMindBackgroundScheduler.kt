package fieldmind.research.app.features.field.data.background

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import fieldmind.research.app.infrastructure.worker.FieldMindSessionReminderWorker
import fieldmind.research.app.infrastructure.worker.FieldMindTaskReminderWorker
import fieldmind.research.app.infrastructure.worker.FieldMindWeatherAlertWorker
import java.util.concurrent.TimeUnit

/** Central WorkManager wiring for FieldMind background features exposed in Settings. */
object FieldMindBackgroundScheduler {
    private const val AUTO_BACKUP_WORK = "fieldmind_auto_backup"
    private const val DAILY_REMINDER_WORK = "fieldmind_daily_reminder"
    private const val WEATHER_ALERT_WORK = "fieldmind_weather_alert"
    private const val TASK_REMINDER_WORK = "fieldmind_task_reminder"
    private const val SESSION_REMINDER_WORK = "fieldmind_session_reminder"

    fun syncAll(context: Context, autoBackupEnabled: Boolean, autoBackupInterval: String, remindersEnabled: Boolean) {
        scheduleAutoBackup(context, autoBackupEnabled, autoBackupInterval)
        scheduleDailyReminder(context, remindersEnabled)
        scheduleWeatherAlerts(context, true)
        scheduleTaskReminders(context, true)
        scheduleSessionReminders(context, true)
    }

    fun scheduleAutoBackup(context: Context, enabled: Boolean, intervalLabel: String) {
        val workManager = WorkManager.getInstance(context.applicationContext)
        if (!enabled) {
            workManager.cancelUniqueWork(AUTO_BACKUP_WORK)
            return
        }
        val (duration, unit) = when (intervalLabel) {
            "Every 6 hours" -> 6L to TimeUnit.HOURS
            "Every 12 hours" -> 12L to TimeUnit.HOURS
            "Daily" -> 1L to TimeUnit.DAYS
            "Monthly" -> 30L to TimeUnit.DAYS
            else -> 7L to TimeUnit.DAYS // Weekly fallback
        }
        val request = PeriodicWorkRequestBuilder<FieldMindAutoBackupWorker>(duration, unit)
            .addTag(AUTO_BACKUP_WORK)
            .build()
        workManager.enqueueUniquePeriodicWork(AUTO_BACKUP_WORK, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    fun scheduleDailyReminder(context: Context, enabled: Boolean) {
        val workManager = WorkManager.getInstance(context.applicationContext)
        if (!enabled) {
            workManager.cancelUniqueWork(DAILY_REMINDER_WORK)
            return
        }
        val request = PeriodicWorkRequestBuilder<FieldMindReminderWorker>(1, TimeUnit.DAYS)
            .addTag(DAILY_REMINDER_WORK)
            .build()
        workManager.enqueueUniquePeriodicWork(DAILY_REMINDER_WORK, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    fun scheduleWeatherAlerts(context: Context, enabled: Boolean) {
        val workManager = WorkManager.getInstance(context.applicationContext)
        if (!enabled) {
            workManager.cancelUniqueWork(WEATHER_ALERT_WORK)
            return
        }
        val request = PeriodicWorkRequestBuilder<FieldMindWeatherAlertWorker>(2, TimeUnit.HOURS)
            .addTag(WEATHER_ALERT_WORK)
            .build()
        workManager.enqueueUniquePeriodicWork(WEATHER_ALERT_WORK, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    fun scheduleTaskReminders(context: Context, enabled: Boolean) {
        val workManager = WorkManager.getInstance(context.applicationContext)
        if (!enabled) {
            workManager.cancelUniqueWork(TASK_REMINDER_WORK)
            return
        }
        val request = PeriodicWorkRequestBuilder<FieldMindTaskReminderWorker>(6, TimeUnit.HOURS)
            .addTag(TASK_REMINDER_WORK)
            .build()
        workManager.enqueueUniquePeriodicWork(TASK_REMINDER_WORK, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    fun scheduleSessionReminders(context: Context, enabled: Boolean) {
        val workManager = WorkManager.getInstance(context.applicationContext)
        if (!enabled) {
            workManager.cancelUniqueWork(SESSION_REMINDER_WORK)
            return
        }
        val request = PeriodicWorkRequestBuilder<FieldMindSessionReminderWorker>(1, TimeUnit.DAYS)
            .addTag(SESSION_REMINDER_WORK)
            .build()
        workManager.enqueueUniquePeriodicWork(SESSION_REMINDER_WORK, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    /**
     * Parse an interval label into milliseconds for countdown display.
     */
    fun intervalToMillis(intervalLabel: String): Long = when (intervalLabel) {
        "Every 6 hours" -> 6L * 60 * 60 * 1000
        "Every 12 hours" -> 12L * 60 * 60 * 1000
        "Daily" -> 24L * 60 * 60 * 1000
        "Weekly" -> 7L * 24 * 60 * 60 * 1000
        "Monthly" -> 30L * 24 * 60 * 60 * 1000
        else -> 7L * 24 * 60 * 60 * 1000
    }
}
