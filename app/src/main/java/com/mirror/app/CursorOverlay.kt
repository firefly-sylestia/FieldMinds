package com.mirror.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.WindowManager

/**
 * Draws a small, precise cursor overlay on the controlled device showing where
 * the controller's pointer is. The cursor stays visible as long as hover/touch
 * updates keep coming — it only hides after [HIDE_DELAY_MS] of inactivity.
 *
 * CRITICAL: The overlay window uses explicit screen dimensions (from getRealMetrics)
 * instead of MATCH_PARENT. MATCH_PARENT on overlay windows may be offset by system
 * insets (status bar, navigation bar), causing the cursor to appear below the
 * actual touch position. Explicit dimensions + FLAG_LAYOUT_NO_LIMITS ensure the
 * overlay canvas coordinates match physical screen coordinates exactly.
 */
class CursorOverlay(private val context: Context) {

    companion object {
        private const val TAG = "CursorOverlay"
        private const val HIDE_DELAY_MS = 3000L
    }

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val mainHandler = Handler(Looper.getMainLooper())

    private var overlayView: CursorView? = null
    private var isShowing = false
    private var hideRunnable: Runnable? = null

    /** Screen dimensions read directly from the physical display. */
    private var screenWidth = 1080
    private var screenHeight = 1920

    init {
        updateScreenDimensions()
    }

    private fun updateScreenDimensions() {
        try {
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getRealMetrics(metrics)
            screenWidth = metrics.widthPixels
            screenHeight = metrics.heightPixels
        } catch (_: Exception) {}
    }

    /** Show the cursor at the given screen coordinates. Updates position if already showing. */
    fun showAt(x: Float, y: Float) {
        mainHandler.post {
            if (!isShowing) {
                createOverlay()
            }
            overlayView?.let { view ->
                view.updatePosition(x, y)
                view.visibility = View.VISIBLE
            }
            // Reset the auto-hide timer
            hideRunnable?.let { mainHandler.removeCallbacks(it) }
            val runnable = Runnable { hide() }
            hideRunnable = runnable
            mainHandler.postDelayed(runnable, HIDE_DELAY_MS)
        }
    }

    /** Hide the cursor overlay immediately. */
    fun hide() {
        mainHandler.post {
            overlayView?.let { view ->
                view.visibility = View.INVISIBLE
            }
            isShowing = false
        }
    }

    /** Remove the overlay view and release resources. */
    fun destroy() {
        mainHandler.post {
            try {
                overlayView?.let { view ->
                    windowManager.removeView(view)
                }
            } catch (_: Exception) {}
            overlayView = null
            isShowing = false
            hideRunnable?.let { mainHandler.removeCallbacks(it) }
            hideRunnable = null
        }
    }

    private fun createOverlay() {
        updateScreenDimensions()

        val view = CursorView(context)
        overlayView = view

        // Use explicit screen dimensions instead of MATCH_PARENT.
        // This avoids the system applying status-bar/nav-bar insets to the window frame,
        // which would shift the canvas origin and cause the cursor to draw at the wrong
        // position relative to injected touch events.
        val layoutParams = WindowManager.LayoutParams(
            screenWidth,
            screenHeight,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 0
        }

        try {
            windowManager.addView(view, layoutParams)
            isShowing = true
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to add cursor overlay", e)
        }
    }

    /**
     * Custom View that draws a small, precise cursor.
     * Small white circle with a distinct black outline and short crosshair.
     */
    private class CursorView(context: Context) : View(context) {

        private var cursorX = 0f
        private var cursorY = 0f
        private val radius = 10f

        // Black outline (outer ring)
        private val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF000000.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        // White fill (inner circle)
        private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            style = Paint.Style.FILL
        }

        // Center dot (black, larger for visibility)
        private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF000000.toInt()
            style = Paint.Style.FILL
        }

        // Short crosshair lines (black, subtle)
        private val crossPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xAA000000.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        fun updatePosition(x: Float, y: Float) {
            cursorX = x
            cursorY = y
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            // White fill
            canvas.drawCircle(cursorX, cursorY, radius, fillPaint)

            // Black outline
            canvas.drawCircle(cursorX, cursorY, radius, outlinePaint)

            // Center dot
            canvas.drawCircle(cursorX, cursorY, 4f, dotPaint)

            // Subtle crosshair
            val cl = radius * 0.6f
            canvas.drawLine(cursorX - cl, cursorY, cursorX + cl, cursorY, crossPaint)
            canvas.drawLine(cursorX, cursorY - cl, cursorX, cursorY + cl, crossPaint)
        }
    }
}
