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
import androidx.compose.ui.graphics.toArgb
import com.curio.app.MainActivity
import com.curio.app.R
import com.curio.app.data.AppPreferences
import com.curio.app.data.CurioCategories
import com.curio.app.data.ExploreReminderScheduler
import com.curio.app.data.ExploreSession
import com.curio.app.data.ExploreSessionStore
import com.curio.app.data.formatElapsed
import com.curio.app.data.parseExploreSession
import com.curio.app.data.toJsonString

/**
 * Foreground service behind an active explore session.
 *
 * Shows a persistent notification (topic name + what to do + a live
 * elapsed-time chronometer) that records how long the user has been
 * exploring — an elapsed clock, NOT a countdown. The notification is
 * tinted with the topic's category accent, carries Pause/Resume and
 * "Done exploring" actions, and switches to a frozen "Paused · 12m 5s"
 * readout while the timer is paused (only the visible timer pauses — the
 * end-of-session reminder still fires at the original start + duration).
 *
 * The service keeps running while the app is backgrounded or swiped away,
 * so the session "doesn't die", and every start re-arms the reminder alarm
 * in case the process was killed. Start/stop are governed by the Settings
 * "Live explore notifications" toggle: with it OFF the service is never
 * started (the in-app pill + end reminder are enough).
 */
class ExploreSessionService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            // Toggled pause from the notification — flip the store and
            // re-render the notification in the new state.
            ACTION_TOGGLE_PAUSE -> {
                val current = ExploreSessionStore.getActiveSession(this) ?: return stopQuietly()
                if (current.paused) ExploreSessionStore.resumeSession(this)
                else ExploreSessionStore.pauseSession(this)
                val updated = ExploreSessionStore.getActiveSession(this)
                    ?: return stopQuietly()
                startForegroundNotification(updated)
                return START_STICKY
            }

            // Pill / settings asked the notification to re-render with the
            // latest persisted session (pause state, elapsed, colors).
            ACTION_SYNC -> {
                // Guard: if live notifications are off, this start request
                // must not resurrect the persistent notification.
                if (!AppPreferences.isLiveNotificationsEnabled(this)) {
                    return stopQuietly()
                }
                val session = ExploreSessionStore.getActiveSession(this)
                    ?: return stopQuietly()
                startForegroundNotification(session)
                return START_STICKY
            }

            // Plain start / START_STICKY restart after process death.
            else -> {
                // Defensive: if live notifications were turned off, never
                // show the persistent notification (boot restore checks this
                // too, but a race can still land here).
                if (!AppPreferences.isLiveNotificationsEnabled(this)) {
                    return stopQuietly()
                }
                // Prefer the session handed over by the caller; a START_STICKY
                // restart after process death delivers a null intent, so fall
                // back to the persisted session — the session "doesn't die".
                val session = intent
                    ?.getStringExtra(EXTRA_SESSION)
                    ?.let { parseExploreSession(it) }
                    ?: ExploreSessionStore.getActiveSession(this)
                if (session == null) return stopQuietly()
                startForegroundNotification(session)
                // Re-arm the reminder on every start so a killed process
                // still nudges (idempotent — cancels then re-sets).
                ExploreReminderScheduler.schedule(this, session.startMillis, session.durationMinutes)
                return START_STICKY
            }
        }
    }

    private fun stopQuietly(): Int {
        stopSelf()
        return START_NOT_STICKY
    }

    private fun startForegroundNotification(session: ExploreSession) {
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
        val togglePauseIntent = PendingIntent.getService(
            this,
            4204,
            Intent(this, ExploreSessionService::class.java)
                .setAction(ACTION_TOGGLE_PAUSE),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val accent = CurioCategories.byId(session.categoryId).accent.toArgb()
        val elapsed = session.elapsedMillis()
        val paused = session.paused

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(accent)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setContentTitle(
                if (paused) "Paused — exploring ${session.topicName}"
                else "Exploring ${session.topicName}"
            )
            .setContentIntent(openAppIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        if (paused) {
            // Frozen readout — the chronometer would keep counting, so drop
            // it and print the banked elapsed time as text instead.
            builder
                .setUsesChronometer(false)
                .setShowWhen(false)
                .setContentText(
                    "Paused · ${formatElapsed(elapsed)} — ${session.verb.lowercase()} ${session.targetName}"
                )
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(
                            "Paused · ${formatElapsed(elapsed)}.\n" +
                            "${session.verb.lowercase()} ${session.targetName} · " +
                            "~${session.durationMinutes} min recommended."
                        )
                )
                .addAction(0, "Resume", togglePauseIntent)
        } else {
            // Live chronometer anchored at start + banked pauses, so it shows
            // active elapsed time even after pause/resume cycles — the system
            // ticks it in the shade without any app wakeups.
            builder
                .setUsesChronometer(true)
                .setShowWhen(true)
                .setWhen(session.startMillis + session.accumulatedPausedMillis)
                .setContentText(
                    "${session.verb.lowercase()} ${session.targetName} · " +
                    "~${session.durationMinutes} min recommended"
                )
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(
                            "${session.verb.lowercase()} ${session.targetName} — " +
                            "timing your explore.\n~${session.durationMinutes} min recommended; " +
                            "you're ${formatElapsed(elapsed)} in."
                        )
                )
                .addAction(0, "Pause", togglePauseIntent)
        }
        builder.addAction(0, "Done exploring", stopIntent)

        val notification = builder.build()
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
        const val ACTION_TOGGLE_PAUSE = "com.curio.app.action.TOGGLE_EXPLORE_PAUSE"
        const val ACTION_SYNC = "com.curio.app.action.SYNC_EXPLORE_SESSION"
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

        /**
         * Asks the running service to re-render its notification with the
         * latest persisted session — used after the pill pauses/resumes so
         * the shade matches. No-op when live notifications are off or the
         * service isn't running (the sync branch stops itself quietly).
         */
        fun sync(context: Context) {
            if (!AppPreferences.isLiveNotificationsEnabled(context)) return
            val session = ExploreSessionStore.getActiveSession(context) ?: return
            ContextCompat.startForegroundService(
                context,
                Intent(context, ExploreSessionService::class.java)
                    .setAction(ACTION_SYNC)
                    .putExtra(EXTRA_SESSION, session.toJsonString())
            )
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, ExploreSessionService::class.java))
        }
    }
}
