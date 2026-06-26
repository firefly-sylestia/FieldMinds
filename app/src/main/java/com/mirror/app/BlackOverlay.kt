package com.mirror.app

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager

/**
 * Manages a full-screen black overlay on the target device.
 *
 * When the overlay is visible:
 * - AMOLED pixels are truly off → significant battery savings
 * - Touch events pass through to the UI underneath (FLAG_NOT_TOUCHABLE)
 * - The overlay has no focus and receives no input (FLAG_NOT_FOCUSABLE)
 *
 * Requires `SYSTEM_ALERT_WINDOW` permission (already declared in the manifest).
 */
class BlackOverlay(private val context: Context) {

    private val windowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private var overlayView: View? = null

    companion object {
        private const val TAG = "BlackOverlay"
    }

    /**
     * Show the black overlay. Does nothing if already shown or if overlay permission
     * is not granted on API 23+.
     *
     * @return true if the overlay was shown, false otherwise.
     */
    fun show(): Boolean {
        if (overlayView != null) {
            Log.d(TAG, "Overlay already visible")
            return true
        }

        // On API 23+ we need SYSTEM_ALERT_WINDOW permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!android.provider.Settings.canDrawOverlays(context)) {
                Log.w(TAG, "Overlay permission not granted — can't show black overlay")
                return false
            }
        }

        try {
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                android.graphics.PixelFormat.TRANSLUCENT
            )

            val view = View(context).apply {
                setBackgroundColor(Color.BLACK)
                // Consume touches silently so they don't reach anything unnecessarily,
                // but FLAG_NOT_TOUCHABLE already handles this — the flag means "don't
                // send touch events to this window at all", so they pass through to
                // windows beneath.
            }

            windowManager.addView(view, params)
            overlayView = view
            Log.d(TAG, "Black overlay shown")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show black overlay", e)
            return false
        }
    }

    /**
     * Hide the black overlay. Does nothing if not shown.
     */
    fun dismiss() {
        val view = overlayView ?: return
        try {
            windowManager.removeView(view)
            Log.d(TAG, "Black overlay dismissed")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to dismiss black overlay", e)
        }
        overlayView = null
    }

    /**
     * @return true if the overlay is currently visible.
     */
    fun isShowing(): Boolean = overlayView != null
}
