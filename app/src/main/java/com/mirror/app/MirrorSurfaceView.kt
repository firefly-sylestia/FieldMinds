package com.mirror.app

import android.content.Context
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.Surface
import android.view.TextureView
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * A TextureView-based display for the remote screen stream.
 *
 * Supports three display modes:
 * - [DISPLAY_FIT]: letterbox — preserves aspect ratio, fills one dimension, black bars on the other
 * - [DISPLAY_FILL]: stretch — fills the entire view, distorts aspect ratio
 * - [DISPLAY_CROP]: center-crop — fills the entire view, preserves aspect ratio by cropping overflow
 *
 * Switching to TextureView (from SurfaceView) enables CROP mode via [Matrix] transformations,
 * which SurfaceView cannot do.
 *
 * Cursor and diagnostic overlay drawing is handled by [CursorOverlayView] which sits on top
 * of this view in the layout (TextureView.draw() is final and cannot be overridden).
 */
class MirrorSurfaceView(
    context: Context,
    attrs: AttributeSet? = null
) : TextureView(context, attrs), TextureView.SurfaceTextureListener {

    private var targetWidth = 720
    private var targetHeight = 1280
    private var inputSender: InputSenderService? = null
    private var cursorOverlay: CursorOverlayView? = null

    /** Signaled when the SurfaceTexture is created — used by [waitForSurface]. */
    private val surfaceCreatedLatch = CountDownLatch(1)

    /** Cached SurfaceTexture for creating a [Surface] for MediaCodec. */
    private var surfaceTexture: SurfaceTexture? = null

    /** Display mode constants. */
    companion object {
        const val DISPLAY_FIT = 0
        const val DISPLAY_FILL = 1
        const val DISPLAY_CROP = 2

        fun label(mode: Int): String = when (mode) {
            DISPLAY_FIT -> "Fit"
            DISPLAY_FILL -> "Fill"
            DISPLAY_CROP -> "Crop"
            else -> "Fit"
        }
    }

    private var displayMode = DISPLAY_FIT // default to FIT (letterbox) for correct aspect ratio on any orientation
    private var keyboardForwardingEnabled = false

    init {
        surfaceTextureListener = this
    }

    /** Set the overlay used for cursor and diagnostics drawing. */
    fun setCursorOverlay(overlay: CursorOverlayView) {
        cursorOverlay = overlay
    }

    /**
     * Call after receiving the target's screen resolution from the handshake.
     */
    fun setTargetResolution(width: Int, height: Int) {
        targetWidth = width
        targetHeight = height
        updateCropTransform()
        requestLayout()
        postInvalidate()
    }

    // ── Display Mode Cycling ──

    fun cycleDisplayMode() {
        displayMode = when (displayMode) {
            DISPLAY_FIT -> DISPLAY_FILL
            DISPLAY_FILL -> DISPLAY_CROP
            DISPLAY_CROP -> DISPLAY_FIT
            else -> DISPLAY_FIT
        }
        applyDisplayMode()
        requestLayout()
        postInvalidate()
    }

    fun getDisplayMode(): Int = displayMode

    private fun applyDisplayMode() {
        if (displayMode == DISPLAY_CROP && targetWidth > 0 && targetHeight > 0) {
            updateCropTransform()
        } else {
            // FIT and FILL use no matrix (SurfaceTexture fills the view naturally)
            // For FIT: onMeasure handles the sizing smaller than the parent
            // For FILL: onMeasure fills the parent
            setTransform(null)
        }
    }

    /**
     * Apply a center-crop matrix transform: scale the video uniformly so it
     * completely fills the view, cropping any overflow, with the visible
     * portion centered.
     */
    private fun updateCropTransform() {
        if (targetWidth <= 0 || targetHeight <= 0) return
        val viewW = width.toFloat()
        val viewH = height.toFloat()
        if (viewW <= 0 || viewH <= 0) return

        val scale = maxOf(
            viewW / targetWidth.toFloat(),
            viewH / targetHeight.toFloat()
        )
        val dx = (viewW - targetWidth * scale) * 0.5f
        val dy = (viewH - targetHeight * scale) * 0.5f

        val matrix = Matrix()
        matrix.setScale(scale, scale)
        matrix.postTranslate(dx, dy)
        setTransform(matrix)
    }

    // ── Aspect-Ratio Measurement ──

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (displayMode == DISPLAY_FILL || displayMode == DISPLAY_CROP || targetWidth <= 0 || targetHeight <= 0) {
            // Fill or Crop: fill the entire parent
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }

        // FIT mode: preserve aspect ratio within the container (letterboxing)
        val containerWidth = MeasureSpec.getSize(widthMeasureSpec)
        val containerHeight = MeasureSpec.getSize(heightMeasureSpec)
        if (containerWidth <= 0 || containerHeight <= 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }

        val videoAspect = targetWidth.toFloat() / targetHeight.toFloat()
        val containerAspect = containerWidth.toFloat() / containerHeight.toFloat()

        val viewWidth: Int
        val viewHeight: Int

        if (videoAspect > containerAspect) {
            viewWidth = containerWidth
            viewHeight = (containerWidth / videoAspect).toInt()
        } else {
            viewHeight = containerHeight
            viewWidth = (containerHeight * videoAspect).toInt()
        }

        setMeasuredDimension(
            viewWidth.coerceAtMost(containerWidth),
            viewHeight.coerceAtMost(containerHeight)
        )
    }

    // ── Surface Latch ──

    /**
     * Wait until the underlying SurfaceTexture is ready, then return a [Surface]
     * wrapping it — ready to pass to [android.media.MediaCodec].
     */
    fun waitForSurface(timeoutMs: Long = 5000): Surface? {
        try {
            val ok = surfaceCreatedLatch.await(timeoutMs, TimeUnit.MILLISECONDS)
            if (ok && surfaceTexture != null) {
                return Surface(surfaceTexture)
            }
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
        return null
    }

    fun isSurfaceReallyValid(): Boolean = surfaceTexture != null

    fun setDiagnosticStatus(text: String?, color: Int = 0xFFFF4444.toInt()) {
        cursorOverlay?.setDiagnostic(text, color)
    }

    fun setInputSender(sender: InputSenderService) {
        inputSender = sender
    }

    fun setKeyboardForwarding(enabled: Boolean) {
        keyboardForwardingEnabled = enabled
        if (enabled) {
            isFocusable = true
            isFocusableInTouchMode = true
            requestFocus()
        } else {
            isFocusable = false
            isFocusableInTouchMode = false
        }
    }

    fun isKeyboardForwarding(): Boolean = keyboardForwardingEnabled

    // ── Keyboard Forwarding ──

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (!keyboardForwardingEnabled || inputSender == null) return super.onKeyDown(keyCode, event)
        when (keyCode) {
            KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_HOME,
            KeyEvent.KEYCODE_APP_SWITCH, KeyEvent.KEYCODE_MENU,
            KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN -> {
                return super.onKeyDown(keyCode, event)
            }
        }
        val unicodeChar = event?.getUnicodeChar() ?: 0
        inputSender?.sendKeyEvent(keyCode, unicodeChar)
        return true
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (!keyboardForwardingEnabled || inputSender == null) return super.onKeyUp(keyCode, event)
        when (keyCode) {
            KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_HOME,
            KeyEvent.KEYCODE_APP_SWITCH, KeyEvent.KEYCODE_MENU,
            KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN -> {
                return super.onKeyUp(keyCode, event)
            }
        }
        return true
    }

    /**
     * Convert view-relative coordinates to overlay-relative coordinates.
     *
     * The [CursorOverlayView] fills the entire parent container (`match_parent`),
     * while this MirrorSurfaceView may be sized smaller (e.g. FIT mode letterbox).
     * Touch events report coordinates relative to this view, so we need to add
     * this view's offset within the parent for the overlay to draw at the correct
     * absolute screen position.
     */
    private fun overlayCoords(viewX: Float, viewY: Float): Pair<Float, Float> {
        // this.left / top is the position of this view within its parent
        val parents = parent
        if (parents is android.view.ViewGroup) {
            // Calculate offset relative to the overlay's coordinate space
            // Both this view and the overlay are children of the same parent
            return Pair(viewX + left.toFloat(), viewY + top.toFloat())
        }
        return Pair(viewX, viewY)
    }

    // ── Touch & Mouse Events ──

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val (x, y) = mapCoordinates(event.x, event.y)
        val (ox, oy) = overlayCoords(event.x, event.y)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                cursorOverlay?.setCursorPosition(ox, oy, true)
                inputSender?.sendTouchEvent(x, y, event.pressure, MotionEvent.ACTION_DOWN)
            }
            MotionEvent.ACTION_MOVE -> {
                cursorOverlay?.setCursorPosition(ox, oy, true)
                inputSender?.sendTouchEvent(x, y, event.pressure, MotionEvent.ACTION_MOVE)
            }
            MotionEvent.ACTION_UP -> {
                cursorOverlay?.hideCursor()
                inputSender?.sendTouchEvent(x, y, event.pressure, MotionEvent.ACTION_UP)
            }
            MotionEvent.ACTION_CANCEL -> {
                cursorOverlay?.hideCursor()
                inputSender?.sendTouchEvent(x, y, event.pressure, MotionEvent.ACTION_UP)
            }
        }
        return true
    }

    // ── Generic Motion: Hover + Scroll ──

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        val (ox, oy) = overlayCoords(event.x, event.y)

        when (event.action) {
            MotionEvent.ACTION_HOVER_MOVE -> {
                cursorOverlay?.setCursorPosition(ox, oy, true)
                val (tx, ty) = mapCoordinates(event.x, event.y)
                inputSender?.sendCursorPosition(tx, ty)
                return true
            }
            MotionEvent.ACTION_HOVER_ENTER -> {
                cursorOverlay?.setCursorPosition(ox, oy, true)
                return true
            }
            MotionEvent.ACTION_HOVER_EXIT -> {
                cursorOverlay?.hideCursor()
                return true
            }
            MotionEvent.ACTION_SCROLL -> {
                val scrollAmount = event.getAxisValue(MotionEvent.AXIS_VSCROLL)
                if (scrollAmount != 0f) {
                    inputSender?.sendScrollEvent(-scrollAmount)
                    return true
                }
            }
            MotionEvent.ACTION_BUTTON_PRESS -> {
                val button = event.actionButton
                if (button == MotionEvent.BUTTON_SECONDARY || button == MotionEvent.BUTTON_TERTIARY) {
                    val (x, y) = mapCoordinates(event.x, event.y)
                    cursorOverlay?.setCursorPosition(ox, oy, true)
                    inputSender?.sendTouchEvent(x, y, event.pressure, MotionEvent.ACTION_DOWN)
                    return true
                }
            }
            MotionEvent.ACTION_BUTTON_RELEASE -> {
                val button = event.actionButton
                if (button == MotionEvent.BUTTON_SECONDARY || button == MotionEvent.BUTTON_TERTIARY) {
                    val (x, y) = mapCoordinates(event.x, event.y)
                    cursorOverlay?.hideCursor()
                    inputSender?.sendTouchEvent(x, y, event.pressure, MotionEvent.ACTION_UP)
                    return true
                }
            }
        }
        return super.onGenericMotionEvent(event)
    }

    /**
     * Map touch/view coordinates to the target's native video resolution.
     *
     * For FIT: view is sized to video aspect ratio, simple linear mapping.
     * For FILL: view fills parent, video stretched, simple linear mapping.
     * For CROP: view fills parent, video is matrix-transformed (scaled up +
     * centered), so we apply the INVERSE of the center-crop transform.
     */
    private fun mapCoordinates(viewX: Float, viewY: Float): Pair<Float, Float> {
        if (width == 0 || height == 0 || targetWidth <= 0 || targetHeight <= 0) {
            return Pair(viewX, viewY)
        }

        if (displayMode == DISPLAY_CROP) {
            // Inverse of center-crop matrix
            val scale = maxOf(
                width.toFloat() / targetWidth.toFloat(),
                height.toFloat() / targetHeight.toFloat()
            )
            val dx = (width - targetWidth * scale) * 0.5f
            val dy = (height - targetHeight * scale) * 0.5f
            val videoX = (viewX - dx) / scale
            val videoY = (viewY - dy) / scale
            return Pair(
                videoX.coerceIn(0f, targetWidth.toFloat()),
                videoY.coerceIn(0f, targetHeight.toFloat())
            )
        }

        // FIT / FILL: direct linear scale
        val scaleX = targetWidth.toFloat() / width.toFloat()
        val scaleY = targetHeight.toFloat() / height.toFloat()
        return Pair(viewX * scaleX, viewY * scaleY)
    }

    // ── TextureView.SurfaceTextureListener ──

    override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
        this.surfaceTexture = surfaceTexture
        surfaceCreatedLatch.countDown()
        applyDisplayMode()
    }

    override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
        applyDisplayMode()
    }

    override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
        this.surfaceTexture = null
        return true
    }

    override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {
        // No-op
    }
}
