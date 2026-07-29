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
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Task Reminder Worker — notifies about upcoming or overdue research tasks.
 * Runs periodically to check for tasks due soon or past due.
 */
class FieldMindTaskReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "fieldmind_task_reminder_work"
        private const val CHANNEL_ID = "fieldmind_task_reminders"
        private const val NOTIFICATION_ID = 8201
    }

    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return@runCatching Result.success()

        ensureChannel()

        val dao = FieldMindDatabase.getInstance(applicationContext).fieldMindDao()
        val tasks = dao.observeTasks().first()

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val overdueTasks = tasks.filter { it.dueDate != null && it.dueDate < today && it.status != "complete" }
        val dueTodayTasks = tasks.filter { it.dueDate == today && it.status != "complete" }
        val dueSoonTasks = tasks.filter { it.dueDate != null && it.dueDate in today..getDatePlusDays(3) && it.status != "complete" }

        when {
            overdueTasks.isNotEmpty() -> {
                val count = overdueTasks.size
                val names = overdueTasks.take(3).joinToString(", ") { it.title.take(20) }
                val suffix = if (count > 3) " and ${count - 3} more" else ""
                showTaskAlert("$count overdue task${if (count > 1) "s" else ""}: $names$suffix")
            }
            dueTodayTasks.isNotEmpty() -> {
                val names = dueTodayTasks.joinToString(", ") { it.title.take(20) }
                showTaskAlert("Due today: $names")
            }
            dueSoonTasks.isNotEmpty() -> {
                val count = dueSoonTasks.size
                showTaskAlert("$count task${if (count > 1) "s" else ""} due within 3 days")
            }
        }

        Result.success()
    }.getOrElse { Result.retry() }

    private fun getDatePlusDays(days: Int): String {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, days)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = applicationContext.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(CHANNEL_ID, "Task Reminders", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Reminders for upcoming and overdue research tasks"
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("MissingPermission")
    private fun showTaskAlert(message: String) {
        val intent = PendingIntent.getActivity(
            applicationContext, 0,
            Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(MainActivity.EXTRA_FIELDMIND_DESTINATION, "tasks")
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Research Tasks")
            .setContentText(message)
            .setColor(0xFF00897B.toInt())
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(intent)
            .addAction(android.R.drawable.ic_menu_agenda, "View Tasks", intent)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, notification)
    }
}
