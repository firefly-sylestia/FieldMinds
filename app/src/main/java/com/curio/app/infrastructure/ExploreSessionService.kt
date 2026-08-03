package com.curio.app.infrastructure

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.curio.app.MainActivity
import com.curio.app.R
import com.curio.app.data.AppPreferences
import com.curio.app.data.CurioCategories
import com.curio.app.data.ExploreReminderScheduler
import com.curio.app.data.ExploreSession
import com.curio.app.data.ExploreSessionStore
import com.curio.app.data.formatElapsed
import com.curio.app.data.toJsonString
import com.curio.app.ui.components.ExploreBubbleContent
import com.curio.app.ui.theme.CurioShapes
import com.curio.app.ui.theme.CurioTypography
import com.curio.app.ui.theme.curioColorScheme
import kotlin.math.hypot

/**
 * Foreground service behind an active explore session.
 *
 * Two jobs, both controlled by the session + Settings:
 *
 * 1. **Live explore notification** (when "Live explore notification" is ON):
 *    a persistent notification with a live elapsed-time chronometer, topic
 *    name, and Pause/Resume + "Done exploring" actions, tinted with the
 *    topic's category accent. An elapsed clock, NOT a countdown.
 *
 * 2. **Floating explore bubble** (when "Floating explore bubble" is ON and
 *    the "Display over other apps" permission is granted): a Messenger-style
 *    bubble rendered in a `TYPE_APPLICATION_OVERLAY` window that floats over
 *    OTHER apps — including the browser — while the session runs. It shows
 *    the same topic + live timer with Pause/Resume, Stop and Hide, and can
 *    be dragged anywhere (snaps to the nearest horizontal edge on release).
 *
 * The service runs while EITHER is active. When only the bubble is on, the
 * mandatory FGS notification downgrades to a quiet, non-chronometer
 * "bubble active" note (Android requires every foreground service to have a
 * notification). Every start re-arms the end-of-session reminder alarm so a
 * killed process still nudges the user.
 */
class ExploreSessionService : Service() {

    private val windowManager: WindowManager by lazy {
        getSystemService(WINDOW_SERVICE) as WindowManager
    }

    // ── Overlay bubble window ─────────────────────────────────────────
    private var bubbleView: ComposeView? = null

    // ── Overlay-window owners ────────────────────────────────────────
    // A TYPE_APPLICATION_OVERLAY window has no Activity behind it, so the
    // bubble's ComposeView inherits no ViewTree owners. Without them,
    // attaching the view throws "ViewTreeLifecycleOwner not found" — the
    // crash when tapping Explore now. Provide service-owned owners (the
    // standard pattern for WindowManager-hosted Compose) and keep the
    // lifecycle RESUMED for the window's lifetime.
    private val overlayLifecycleOwner = object : LifecycleOwner {
        val registry: LifecycleRegistry = LifecycleRegistry.createUnsafe(this).apply {
            currentState = Lifecycle.State.RESUMED
        }
        override val lifecycle: Lifecycle get() = registry
    }

    private val overlayViewModelStoreOwner = object : ViewModelStoreOwner {
        private val store = ViewModelStore()
        override val viewModelStore: ViewModelStore get() = store
    }

    private val overlaySavedStateRegistryOwner = object : SavedStateRegistryOwner {
        private val controller = SavedStateRegistryController.create(this).apply {
            performRestore(null)
        }
        override val savedStateRegistry: SavedStateRegistry
            get() = controller.savedStateRegistry
    }

    // Drag state (view-level touch listener — slop-gated so taps on the
    // bubble's Compose buttons still land while drags move the window).
    private var dragStartX = 0f
    private var dragStartY = 0f
    private var dragTouchX = 0f
    private var dragTouchY = 0f
    private var dragging = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            // Toggled pause from the notification or the bubble — flip the
            // store; the render below re-draws in the new state (and the
            // bubble/notification recompose from the reactive session).
            ACTION_TOGGLE_PAUSE -> {
                val current = ExploreSessionStore.getActiveSession(this) ?: return stopQuietly()
                if (current.paused) ExploreSessionStore.resumeSession(this)
                else ExploreSessionStore.pauseSession(this)
            }
        }
        return render()
    }

    /**
     * Re-evaluates what the service should show from the persisted session
     * and the Settings toggles, and stops itself when nothing is wanted.
     */
    private fun render(): Int {
        val session = ExploreSessionStore.getActiveSession(this) ?: return stopQuietly()
        val liveNotif = AppPreferences.isLiveNotificationsEnabled(this)
        val bubbleWanted = AppPreferences.isOverlayBubbleEnabled(this) &&
            Settings.canDrawOverlays(this) &&
            !session.pillHidden
        if (!liveNotif && !bubbleWanted) return stopQuietly()

        promote(if (liveNotif) liveNotification(session) else bubbleOnlyNotification(session))
        if (bubbleWanted) showBubble() else removeBubble()
        return START_STICKY
    }

    private fun stopQuietly(): Int {
        removeBubble()
        stopSelf()
        return START_NOT_STICKY
    }

    /** Puts the service into the foreground with [notification]. */
    private fun promote(notification: Notification) {
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

    // ── Notifications ─────────────────────────────────────────────────

    private fun openAppIntent(): PendingIntent =
        PendingIntent.getActivity(
            this,
            4201,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun stopSessionIntent(): PendingIntent =
        PendingIntent.getBroadcast(
            this,
            4202,
            Intent(this, ExploreReminderReceiver::class.java)
                .setAction(ExploreReminderReceiver.ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun togglePauseIntent(): PendingIntent =
        PendingIntent.getService(
            this,
            4204,
            Intent(this, ExploreSessionService::class.java)
                .setAction(ACTION_TOGGLE_PAUSE),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    /** Full chronometer notification — used when live notifications are ON. */
    private fun liveNotification(session: ExploreSession): Notification {
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
            .setContentIntent(openAppIntent())
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
                .addAction(0, "Resume", togglePauseIntent())
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
                .addAction(0, "Pause", togglePauseIntent())
        }
        builder.addAction(0, "Done exploring", stopSessionIntent())
        return builder.build()
    }

    /**
     * Minimal quiet notification — used when live notifications are OFF but
     * the floating bubble is on (Android mandates a notification for every
     * foreground service). No chronometer; the bubble carries the live timer.
     */
    private fun bubbleOnlyNotification(session: ExploreSession): Notification {
        val accent = CurioCategories.byId(session.categoryId).accent.toArgb()
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(accent)
            .setContentTitle("Explore bubble · ${session.topicName}")
            .setContentText("Floating timer active — ${formatElapsed(session.elapsedMillis())} in")
            .setContentIntent(openAppIntent())
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(0, "Done exploring", stopSessionIntent())
            .build()
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

    // ── Floating bubble (system overlay window) ───────────────────────

    /** Adds the bubble window if it isn't already showing. */
    private fun showBubble() {
        if (bubbleView != null) return

        val view = ComposeView(this).apply {
            // Overlay windows have no Activity to supply the ViewTree owners
            // Compose requires — install the service-owned ones above so
            // attaching this view doesn't throw (crash fix).
            setViewTreeLifecycleOwner(overlayLifecycleOwner)
            setViewTreeViewModelStoreOwner(overlayViewModelStoreOwner)
            setViewTreeSavedStateRegistryOwner(overlaySavedStateRegistryOwner)
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent {
                // Reads the reactive session — pause/resume/hide from the
                // notification or the bubble recompose this content live.
                val session = ExploreSessionStore.activeSessionState
                if (session != null) {
                    MaterialTheme(
                        colorScheme = curioColorScheme(),
                        typography = CurioTypography,
                        shapes = CurioShapes
                    ) {
                        ExploreBubbleContent(
                            session = session,
                            onTogglePause = {
                                val current = ExploreSessionStore.getActiveSession(this@ExploreSessionService)
                                    ?: return@ExploreBubbleContent
                                if (current.paused) {
                                    ExploreSessionStore.resumeSession(this@ExploreSessionService)
                                } else {
                                    ExploreSessionStore.pauseSession(this@ExploreSessionService)
                                }
                                render()
                            },
                            onStop = {
                                ExploreSessionStore.clearSession(this@ExploreSessionService)
                                ExploreReminderScheduler.cancel(this@ExploreSessionService)
                                render()
                            },
                            onHide = {
                                ExploreSessionStore.setPillHidden(this@ExploreSessionService, true)
                                render()
                            }
                        )
                    }
                }
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        val added = runCatching { windowManager.addView(view, params) }.isSuccess
        if (!added) {
            // Overlay unavailable (permission revoked between the check and
            // the add, or an OEM rejection) — skip the bubble; the
            // notification-only path keeps running. Never crash a
            // START_STICKY service here (an uncaught exception would
            // restart it into the same failure — a crash loop).
            return
        }
        // Initial placement: bottom-center, clear of the nav-bar area.
        view.doOnLayout {
            val bounds = windowBounds()
            val density = resources.displayMetrics.density
            val margin = (12 * density).toInt()
            params.x = ((bounds.width() - view.width) / 2).coerceAtLeast(margin)
            params.y = (bounds.height() - view.height - (96 * density).toInt()).coerceAtLeast(margin)
            windowManager.updateViewLayout(view, params)
        }
        view.setOnTouchListener(makeBubbleTouchListener(view, params))
        bubbleView = view
    }

    /** Removes the bubble window (no-op when it isn't showing). */
    private fun removeBubble() {
        bubbleView?.let { v ->
            runCatching { windowManager.removeView(v) }
            bubbleView = null
        }
    }

    /**
     * Slop-gated drag: taps (no movement beyond touch slop) fall through to
     * the bubble's Compose buttons; real drags move the window and snap to
     * the nearest horizontal edge on release.
     */
    private fun makeBubbleTouchListener(
        view: View,
        params: WindowManager.LayoutParams
    ): View.OnTouchListener {
        val slop = ViewConfiguration.get(this).scaledTouchSlop
        val density = resources.displayMetrics.density
        val marginPx = (12 * density).toInt()
        return View.OnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    dragStartX = params.x.toFloat()
                    dragStartY = params.y.toFloat()
                    dragTouchX = event.rawX
                    dragTouchY = event.rawY
                    dragging = false
                    false
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - dragTouchX
                    val dy = event.rawY - dragTouchY
                    if (!dragging && hypot(dx, dy) > slop) dragging = true
                    if (dragging) {
                        params.x = (dragStartX + dx).toInt()
                        params.y = (dragStartY + dy).toInt()
                        windowManager.updateViewLayout(view, params)
                        true
                    } else {
                        false
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (dragging) {
                        dragging = false
                        snapBubble(view, params, marginPx)
                        true
                    } else {
                        false
                    }
                }
                MotionEvent.ACTION_CANCEL -> {
                    dragging = false
                    false
                }
                else -> false
            }
        }
    }

    /** Snaps the bubble to the nearest horizontal edge, clamped on-screen. */
    private fun snapBubble(view: View, params: WindowManager.LayoutParams, marginPx: Int) {
        val bounds = windowBounds()
        val snapLeft = params.x + view.width / 2 <= bounds.width() / 2
        params.x = if (snapLeft) {
            marginPx
        } else {
            (bounds.width() - view.width - marginPx).coerceAtLeast(marginPx)
        }
        params.y = params.y.coerceIn(
            marginPx,
            (bounds.height() - view.height - marginPx).coerceAtLeast(marginPx)
        )
        windowManager.updateViewLayout(view, params)
    }

    private fun windowBounds(): Rect =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowManager.maximumWindowMetrics.bounds
        } else {
            val dm = resources.displayMetrics
            Rect(0, 0, dm.widthPixels, dm.heightPixels)
        }

    override fun onDestroy() {
        removeBubble()
        overlayLifecycleOwner.registry.currentState = Lifecycle.State.DESTROYED
        super.onDestroy()
    }

    companion object {
        const val EXTRA_SESSION = "explore_session_json"
        const val ACTION_TOGGLE_PAUSE = "com.curio.app.action.TOGGLE_EXPLORE_PAUSE"
        const val ACTION_SYNC = "com.curio.app.action.SYNC_EXPLORE_SESSION"
        const val CHANNEL_ID = "explore_session_timer"
        const val NOTIFICATION_ID = 4211

        /** Starts the explore foreground service for [session]. */
        fun start(context: Context, session: ExploreSession) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, ExploreSessionService::class.java)
                    .putExtra(EXTRA_SESSION, session.toJsonString())
            )
        }

        /**
         * Asks the service to re-render from the persisted session — used
         * after Settings toggles change or the bubble is restored, so the
         * notification + bubble match the current state. No-op effect when
         * nothing wants the service (the render stops it quietly).
         */
        fun sync(context: Context) {
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
