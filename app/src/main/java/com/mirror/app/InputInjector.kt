package com.mirror.app

import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.DisplayMetrics
import android.util.Log
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.WindowManager
import kotlin.math.sqrt

class InputInjector(private val context: Context) {

    companion object {
        private const val TAG = "InputInjector"

        /** Default video resolution — used as fallback when actual resolution isn't known yet. */
        const val DEFAULT_VIDEO_WIDTH = 720
        const val DEFAULT_VIDEO_HEIGHT = 1280

        /** Minimum movement before a touch is classified as a swipe/drag. */
        private const val SWIPE_THRESHOLD_PX = 2f

        /** Minimum movement between MOVE segments. */
        private const val MOVE_SEGMENT_THRESHOLD_PX = 1f

        /** Duration for a tap gesture. */
        private const val TAP_DURATION_MS = 15L

        private const val MAX_SCROLL_FRACTION = 0.50f
        private const val MIN_SCROLL_PX = 80f
        /** Scroll gesture duration — shorter = snappier perceived scroll. */
        private const val SCROLL_DURATION_MS = 20L

        // ── Scroll Inertia Physics ──
        /** Friction factor applied per inertia step (0..1, lower = more friction). */
        private const val INERTIA_FRICTION = 0.82f
        /** Interval between inertia continuation steps (ms). */
        private const val INERTIA_INTERVAL_MS = 25L
        /** If no new scroll event within this time, inertia begins (ms). */
        private const val INERTIA_TIMEOUT_MS = 50L
        /** Minimum gesture distance (px) — stop inertia below this. */
        private const val MIN_INERTIA_DIST = 20f
    }

    private var videoWidth = DEFAULT_VIDEO_WIDTH
    private var videoHeight = DEFAULT_VIDEO_HEIGHT

    fun syncVideoResolution() {
        val w = ScreenCaptureService.currentVideoWidth
        val h = ScreenCaptureService.currentVideoHeight
        if (w > 0 && h > 0) {
            videoWidth = w
            videoHeight = h
            Log.d(TAG, "Synced video resolution: ${w}x${h}")
        }
        readDisplayMetrics()
    }

    private var screenWidth = 1080
    private var screenHeight = 1920

    // ── Gesture state (shared across DOWN / MOVE / UP) ──
    private var downX = 0f
    private var downY = 0f
    private var lastSentX = 0f
    private var lastSentY = 0f
    private var isDragging = false
    private var hasPendingTap = false

    private var scrollCenterX = 540f
    private var scrollStartY = 1248f
    private var scrollEndY = 480f

    // ── Scroll inertia state ──
    @Volatile
    private var scrollVelocity = 0f
    @Volatile
    private var scrollDirection = 0  // -1 = up, +1 = down
    @Volatile
    private var lastScrollTime = 0L
    private val inertiaHandler = Handler(Looper.getMainLooper())
    private val inertiaRunnable = object : Runnable {
        override fun run() {
            stepScrollInertia()
        }
    }

    init {
        readDisplayMetrics()
    }

    private fun readDisplayMetrics() {
        try {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val metrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            wm.defaultDisplay.getRealMetrics(metrics)
            screenWidth = metrics.widthPixels
            screenHeight = metrics.heightPixels
            scrollCenterX = screenWidth / 2f
            scrollStartY = screenHeight * 0.65f
            scrollEndY = screenHeight * 0.25f
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read display metrics", e)
        }
    }

    fun refreshDisplayMetrics() { readDisplayMetrics() }

    fun setVideoResolution(width: Int, height: Int) {
        videoWidth = width; videoHeight = height
    }

    fun getTargetWidth(): Int = screenWidth
    fun getTargetHeight(): Int = screenHeight
    fun getVideoWidth(): Int = videoWidth
    fun getVideoHeight(): Int = videoHeight

    private fun scaleToScreen(videoX: Float, videoY: Float): Pair<Float, Float> {
        if (videoWidth <= 0 || videoHeight <= 0 || screenWidth <= 0 || screenHeight <= 0) return Pair(videoX, videoY)
        return Pair(videoX * screenWidth.toFloat() / videoWidth, videoY * screenHeight.toFloat() / videoHeight)
    }

    private fun getService(): android.accessibilityservice.AccessibilityService? {
        val service = ControlAccessibilityService.instance
        if (service == null) {
            Log.e(TAG, "AccessibilityService unavailable")
            return null
        }
        return service
    }

    var onCursorPosition: ((Float, Float) -> Unit)? = null

    // ═══════════════════════════════════════════════════════════════
    //  Touch Injection — uses GestureDescription.dispatchGesture()
    //  exclusively (the official AccessibilityService API).
    //  The reflective injectPointerEvent path was removed because it
    //  created inconsistent state between two injection mechanisms.
    // ═══════════════════════════════════════════════════════════════

    fun injectTouchEvent(x: Float, y: Float, pressure: Float, action: Int) {
        val service = getService() ?: return
        val (screenX, screenY) = scaleToScreen(x, y)

        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            onCursorPosition?.invoke(screenX, screenY)
        }

        injectGesture(service, screenX, screenY, pressure, action)
    }

    @Suppress("DEPRECATION")
    private fun injectGesture(
        service: android.accessibilityservice.AccessibilityService,
        x: Float, y: Float, pressure: Float, action: Int
    ) {
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                downX = x
                downY = y
                lastSentX = x
                lastSentY = y
                isDragging = false
                hasPendingTap = false
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = x - downX
                val dy = y - downY
                val dist = sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                val dragDuration = SettingsActivity.getDragDurationMs(context)

                if (dist > SWIPE_THRESHOLD_PX && !isDragging) {
                    hasPendingTap = false
                    isDragging = true
                    val path = Path().apply { moveTo(downX, downY); lineTo(x, y) }
                    val stroke = GestureDescription.StrokeDescription(path, 0, dragDuration, true)
                    service.dispatchGesture(GestureDescription.Builder().addStroke(stroke).build(), null, null)
                    lastSentX = x
                    lastSentY = y
                } else if (isDragging) {
                    val moveDx = x - lastSentX
                    val moveDy = y - lastSentY
                    if (sqrt((moveDx * moveDx + moveDy * moveDy).toDouble()).toFloat() > MOVE_SEGMENT_THRESHOLD_PX) {
                        val path = Path().apply { moveTo(lastSentX, lastSentY); lineTo(x, y) }
                        val stroke = GestureDescription.StrokeDescription(path, 0, dragDuration, true)
                        service.dispatchGesture(GestureDescription.Builder().addStroke(stroke).build(), null, null)
                        lastSentX = x
                        lastSentY = y
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                if (isDragging) {
                    val path = Path().apply { moveTo(x, y) }
                    val stroke = GestureDescription.StrokeDescription(path, 0, 1L)
                    service.dispatchGesture(GestureDescription.Builder().addStroke(stroke).build(), null, null)
                } else if (!hasPendingTap) {
                    val path = Path().apply { moveTo(x, y) }
                    val stroke = GestureDescription.StrokeDescription(path, 0, TAP_DURATION_MS)
                    service.dispatchGesture(GestureDescription.Builder().addStroke(stroke).build(), null, null)
                }
                isDragging = false
                hasPendingTap = false
            }

            MotionEvent.ACTION_CANCEL -> {
                val path = Path().apply { moveTo(x, y) }
                val stroke = GestureDescription.StrokeDescription(path, 0, 1L)
                service.dispatchGesture(GestureDescription.Builder().addStroke(stroke).build(), null, null)
                isDragging = false
                hasPendingTap = false
            }
        }
    }

    fun updateCursorPosition(x: Float, y: Float) {
        val (screenX, screenY) = scaleToScreen(x, y)
        onCursorPosition?.invoke(screenX, screenY)
    }

    // ═══════════════════════════════════════════════════════════════
    //  Key Events
    // ═══════════════════════════════════════════════════════════════

    fun injectKeyEvent(keyCode: Int, unicodeChar: Int = 0) {
        val service = getService()

        if (unicodeChar > 0) {
            injectTextChar(keyCode, unicodeChar)
            return
        }

        // System global actions
        if (service != null) {
            when (keyCode) {
                KeyEvent.KEYCODE_BACK -> {
                    service.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK); return
                }
                KeyEvent.KEYCODE_HOME -> {
                    service.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_HOME); return
                }
                KeyEvent.KEYCODE_APP_SWITCH -> {
                    service.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_RECENTS); return
                }
                KeyEvent.KEYCODE_POWER -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        service.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN)
                    }; return
                }
            }
        }

        // Navigation and action keys → inject as touch gestures
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> { injectKeyAsTouch(screenWidth / 2f, screenHeight * 0.6f, screenWidth / 2f, screenHeight * 0.4f); return }
            KeyEvent.KEYCODE_DPAD_DOWN -> { injectKeyAsTouch(screenWidth / 2f, screenHeight * 0.4f, screenWidth / 2f, screenHeight * 0.6f); return }
            KeyEvent.KEYCODE_DPAD_LEFT -> { injectKeyAsTouch(screenWidth * 0.6f, screenHeight / 2f, screenWidth * 0.4f, screenHeight / 2f); return }
            KeyEvent.KEYCODE_DPAD_RIGHT -> { injectKeyAsTouch(screenWidth * 0.4f, screenHeight / 2f, screenWidth * 0.6f, screenHeight / 2f); return }
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_SPACE -> { injectKeyAsTap(screenWidth / 2f, screenHeight / 2f); return }
            KeyEvent.KEYCODE_DEL, KeyEvent.KEYCODE_FORWARD_DEL -> { injectKeyAsTap(screenWidth / 2f, screenHeight * 0.9f); return }
            KeyEvent.KEYCODE_ESCAPE -> { service?.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK); return }
            KeyEvent.KEYCODE_TAB -> { injectKeyAsSwipe(screenWidth * 0.8f, screenHeight / 2f, screenWidth * 0.2f, screenHeight / 2f); return }
        }

        Log.w(TAG, "No injection path for key $keyCode")
    }

    /**
     * Primary: InputManager.injectInputEvent via reflection.
     * Fallback: clipboard + paste via AccessibilityNodeInfo.
     */
    private fun injectTextChar(keyCode: Int, unicodeChar: Int) {
        val charStr = unicodeChar.toChar().toString()
        val now = SystemClock.uptimeMillis()

        // ── Primary: InputManager ──
        try {
            val inputManager = context.getSystemService(Context.INPUT_SERVICE)
            val injectMethod = inputManager?.javaClass?.getMethod(
                "injectInputEvent", android.view.InputEvent::class.java, Int::class.javaPrimitiveType
            )
            if (inputManager != null && injectMethod != null) {
                val source = InputDevice.SOURCE_KEYBOARD
                val flags = KeyEvent.FLAG_FROM_SYSTEM
                val downEvent = KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0, 0, 0, 0, flags, source)
                val upEvent = KeyEvent(now, now + 1, KeyEvent.ACTION_UP, keyCode, 0, 0, 0, 0, flags, source)
                injectMethod.invoke(inputManager, downEvent, 0)
                injectMethod.invoke(inputManager, upEvent, 0)
                Log.d(TAG, "Injected char '$charStr' via InputManager")
                return
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "InputManager not permitted for '$charStr'", e)
        } catch (e: Exception) {
            Log.w(TAG, "InputManager failed for '$charStr'", e)
        }

        // ── Fallback: clipboard + paste ──
        val accessibilityService = ControlAccessibilityService.instance
        if (accessibilityService != null) {
            try {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("text", charStr))
                val root = accessibilityService.rootInActiveWindow
                if (root != null) {                    val focused = root.findFocus(
                        android.view.accessibility.AccessibilityNodeInfo.FOCUS_INPUT
                    )
                    if (focused != null) {
                        focused.performAction(
                            android.view.accessibility.AccessibilityNodeInfo.ACTION_PASTE
                        )
                        Log.d(TAG, "Pasted '$charStr' via clipboard")
                        return
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Clipboard paste fallback failed for '$charStr'", e)
            }
        }

        Log.w(TAG, "Text input not available for '$charStr' (keyCode=$keyCode)")
    }

    @Suppress("DEPRECATION")
    private fun injectKeyAsTouch(x1: Float, y1: Float, x2: Float, y2: Float) {
        val service = getService() ?: return
        try {
            val path = Path().apply { moveTo(x1, y1); lineTo(x2, y2) }
            val stroke = GestureDescription.StrokeDescription(path, 0, 60L, false)
            service.dispatchGesture(GestureDescription.Builder().addStroke(stroke).build(), null, null)
        } catch (_: Exception) {}
    }

    @Suppress("DEPRECATION")
    private fun injectKeyAsTap(x: Float, y: Float) {
        val service = getService() ?: return
        try {
            val path = Path().apply { moveTo(x, y) }
            val stroke = GestureDescription.StrokeDescription(path, 0, 30L, false)
            service.dispatchGesture(GestureDescription.Builder().addStroke(stroke).build(), null, null)
        } catch (_: Exception) {}
    }

    @Suppress("DEPRECATION")
    private fun injectKeyAsSwipe(x1: Float, y1: Float, x2: Float, y2: Float) {
        val service = getService() ?: return
        try {
            val path = Path().apply { moveTo(x1, y1); lineTo(x2, y2) }
            val stroke = GestureDescription.StrokeDescription(path, 0, 80L, false)
            service.dispatchGesture(GestureDescription.Builder().addStroke(stroke).build(), null, null)
        } catch (_: Exception) {}
    }

    // ═══════════════════════════════════════════════════════════════
    //  Scroll
    // ═══════════════════════════════════════════════════════════════

    fun injectScroll(dx: Float, dy: Float) {
        val service = getService() ?: return
        val absDy = kotlin.math.abs(dy)
        val multiplier = SettingsActivity.getScrollSpeedMultiplier(context)
        val rawDist = absDy * multiplier * screenHeight * 0.12f
        val scrollDist = rawDist.toInt().coerceIn(MIN_SCROLL_PX.toInt(), (screenHeight * MAX_SCROLL_FRACTION).toInt()).toFloat()

        // 1. Dispatch the immediate scroll gesture
        dispatchScrollGesture(service, scrollDist, dy < 0)

        // 2. Track velocity (exponential moving average for smoothness)
        scrollVelocity = scrollDist * 0.4f + scrollVelocity * 0.6f
        scrollDirection = if (dy < 0) -1 else 1
        lastScrollTime = SystemClock.uptimeMillis()

        // 3. Cancel previous inertia timeout, reschedule after INERTIA_TIMEOUT_MS
        inertiaHandler.removeCallbacks(inertiaRunnable)
        inertiaHandler.postDelayed(inertiaRunnable, INERTIA_TIMEOUT_MS)
    }

    /**
     * Stepping the scroll inertia: apply friction, dispatch one gesture step,
     * and re-schedule if velocity is still above threshold.
     *
     * Only fires if no explicit scroll events arrived within INERTIA_TIMEOUT_MS.
     */
    private fun stepScrollInertia() {
        // If a new scroll event arrived recently, reschedule and wait
        if (SystemClock.uptimeMillis() - lastScrollTime < INERTIA_TIMEOUT_MS) {
            inertiaHandler.postDelayed(inertiaRunnable, INERTIA_TIMEOUT_MS)
            return
        }

        val service = getService() ?: return

        // Apply friction decay
        scrollVelocity *= INERTIA_FRICTION

        // Stop if velocity is negligible
        if (scrollVelocity < MIN_INERTIA_DIST) {
            scrollVelocity = 0f
            return
        }

        // Dispatch one inertia step gesture
        val inertiaDist = scrollVelocity.coerceIn(
            MIN_INERTIA_DIST,
            (screenHeight * MAX_SCROLL_FRACTION).toInt().toFloat()
        )
        dispatchScrollGesture(service, inertiaDist, scrollDirection < 0)

        // Schedule the next inertia step
        inertiaHandler.postDelayed(inertiaRunnable, INERTIA_INTERVAL_MS)
    }

    /**
     * Dispatch a single scroll gesture (swipe up or down) of [dist] pixels.
     */
    private fun dispatchScrollGesture(
        service: android.accessibilityservice.AccessibilityService,
        dist: Float,
        scrollUp: Boolean
    ) {
        if (scrollUp) {
            val path = Path().apply {
                moveTo(scrollCenterX, scrollStartY)
                lineTo(scrollCenterX, scrollStartY - dist)
            }
            val stroke = GestureDescription.StrokeDescription(path, 0, SCROLL_DURATION_MS)
            service.dispatchGesture(GestureDescription.Builder().addStroke(stroke).build(), null, null)
        } else {
            val path = Path().apply {
                moveTo(scrollCenterX, scrollEndY)
                lineTo(scrollCenterX, scrollEndY + dist)
            }
            val stroke = GestureDescription.StrokeDescription(path, 0, SCROLL_DURATION_MS)
            service.dispatchGesture(GestureDescription.Builder().addStroke(stroke).build(), null, null)
        }
    }

    /**
     * Immediately cancel any ongoing scroll inertia.
     * Called when the input service stops.
     */
    fun stopScrollInertia() {
        inertiaHandler.removeCallbacks(inertiaRunnable)
        scrollVelocity = 0f
        scrollDirection = 0
    }
}
