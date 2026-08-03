package com.curio.app.infrastructure

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.curio.app.MainActivity
import com.curio.app.R
import com.curio.app.data.ExploreReminderScheduler
import com.curio.app.data.ExploreSessionStore
import com.curio.app.navigation.PendingEntryOpen

/**
 * Two jobs for the explore-session flow:
 *  1. When the reminder alarm fires — a nudge that the recommended explore
 *     time is up: "Done exploring <topic>? If you are, write it down."
 *  2. When the "Done exploring" notification action is tapped — clears the
 *     session, cancels the alarm and stops the timer service.
 */
class ExploreReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == ACTION_STOP) {
            // "Done exploring" — tear the session down AND hand the user
            // straight to the write-it-down entry page for the topic, so the
            // action lands somewhere useful instead of just dismissing the
            // shade. The NavHost opens the page with HOME anchored beneath
            // it, so Back from the entry page returns to the app instead of
            // exiting it.
            val session = ExploreSessionStore.getActiveSession(context)
            ExploreSessionStore.clearSession(context)
            ExploreReminderScheduler.cancel(context)
            ExploreSessionService.stop(context)
            if (session != null) {
                val open = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra(PendingEntryOpen.EXTRA_CATEGORY_SLUG, session.categoryId.routeSlug)
                    putExtra(PendingEntryOpen.EXTRA_TOPIC_NAME, session.topicName)
                }
                context.startActivity(open)
            }
            return
        }

        // No active session → the reminder is stale, drop it silently.
        val session = ExploreSessionStore.getActiveSession(context) ?: return
        createChannel(context)
        if (android.os.Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val openAppIntent = PendingIntent.getActivity(
            context,
            4212,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Done exploring ${session.topicName}?")
            .setContentText("If you're finished, come back and write it down — ${session.verb} ${session.targetName}.")
            .setContentIntent(openAppIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    private fun createChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Explore reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminds you when the recommended explore time is up."
            }
        )
    }

    companion object {
        const val ACTION_STOP = "com.curio.app.action.STOP_EXPLORE_SESSION"
        const val CHANNEL_ID = "explore_reminders"
        const val NOTIFICATION_ID = 4213
    }
}
