package com.curio.app.infrastructure

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.curio.app.MainActivity
import com.curio.app.R
import com.curio.app.data.ExploreReminderScheduler
import com.curio.app.data.ExploreSession
import com.curio.app.data.ExploreSessionStore
import com.curio.app.data.parseExploreSession
import com.curio.app.data.toJsonString

/**
 * Foreground service behind an active explore session.
 *
 * Shows a persistent chronometer notification (topic name + what to do)
 * that records how long the user has been exploring — an elapsed-time
 * clock, NOT a countdown. The service keeps running while the app is
 * backgrounded or swiped away, so the session "doesn't die", and every
 * start re-arms the reminder alarm in case the process was killed.
 */
class ExploreSessionService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Prefer the session handed over by the caller; a START_STICKY
        // restart after process death delivers a null intent, so fall back
        // to the persisted session — the session "doesn't die" even then.
        val session = intent
            ?.getStringExtra(EXTRA_SESSION)
            ?.let { parseExploreSession(it) }
            ?: ExploreSessionStore.getActiveSession(this)
        if (session == null) {
            stopSelf()
            return START_NOT_STICKY
        }
        startForegroundWithChronometer(session)
        // Re-arm the reminder on every start so a killed process still nudges.
        ExploreReminderScheduler.schedule(this, session.startMillis, session.durationMinutes)
        return START_STICKY
    }

    private fun startForegroundWithChronometer(session: ExploreSession) {
        val openAppIntent = PendingIntent.getActivity(
            this,
            4201,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = PendingIntent.getBroadcast(
            this,
            4202,
            Intent(this, ExploreReminderReceiver::class.java)
                .setAction(ExploreReminderReceiver.ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(session.topicName)
            .setContentText("${session.verb} ${session.targetName} · ~${session.durationMinutes} min — timing your explore")
            .setContentIntent(openAppIntent)
            .setWhen(session.startMillis)
            .setUsesChronometer(true)
            .setShowWhen(true)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(0, "Done exploring", stopIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Explore session timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows how long you've been exploring a topic and when to wrap up."
            }
        )
    }

    companion object {
        const val EXTRA_SESSION = "explore_session_json"
        const val CHANNEL_ID = "explore_session_timer"
        const val NOTIFICATION_ID = 4211

        /** Starts the elapsed-timer foreground service for [session]. */
        fun start(context: Context, session: ExploreSession) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, ExploreSessionService::class.java)
                    .putExtra(EXTRA_SESSION, session.toJsonString())
            )
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, ExploreSessionService::class.java))
        }
    }
}
