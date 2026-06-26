package com.mirror.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * Transparent overlay that draws the cursor crosshair and diagnostic messages
 * on top of the [MirrorSurfaceView] (TextureView).
 *
 * MirrorSurfaceView calls [setCursorPosition] and [setDiagnostic] from its
 * event handlers, and this View redraws accordingly.
 */
class CursorOverlayView(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var cursorX = -1f
    private var cursorY = -1f
    private var cursorVisible = false
    private var cursorRadius = 24f

    private var diagnosticText: String? = null
    private var diagnosticColor = 0xFFFF4444.toInt()

    // ── Paints (created once for performance) ──

    private val outerRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xAAFFFFFF.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    private val midRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xAAFFFFFF.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x44FFFFFF.toInt()
        style = Paint.Style.FILL
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xCCFFFFFF.toInt()
        style = Paint.Style.FILL
    }

    private val diagTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFF4444.toInt()
        textSize = 48f
    }

    private val diagBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x88000000.toInt()
        style = Paint.Style.FILL
    }

    // ── Public API called from MirrorSurfaceView ──

    fun setCursorPosition(x: Float, y: Float, visible: Boolean) {
        cursorX = x
        cursorY = y
        cursorVisible = visible
        postInvalidate()
    }

    fun hideCursor() {
        cursorVisible = false
        postInvalidate()
    }

    fun setDiagnostic(text: String?, color: Int = 0xFFFF4444.toInt()) {
        diagnosticText = text
        diagnosticColor = color
        postInvalidate()
    }

    // ── Drawing ──

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawCursor(canvas)
        drawDiagnostic(canvas)
    }

    private fun drawCursor(canvas: Canvas) {
        if (!cursorVisible || cursorX < 0 || cursorY < 0) return

        // Outer glow ring
        outerRingPaint.alpha = 100
        canvas.drawCircle(cursorX, cursorY, cursorRadius + 8f, outerRingPaint)
        outerRingPaint.alpha = 255

        // Mid ring
        canvas.drawCircle(cursorX, cursorY, cursorRadius, midRingPaint)

        // Filled center
        canvas.drawCircle(cursorX, cursorY, cursorRadius - 2f, fillPaint)

        // Center dot
        canvas.drawCircle(cursorX, cursorY, 4f, dotPaint)

        // Crosshair lines
        val crossLen = cursorRadius * 0.4f
        val crossPaint = Paint(midRingPaint).apply { alpha = 180 }
        canvas.drawLine(cursorX - crossLen, cursorY, cursorX + crossLen, cursorY, crossPaint)
        canvas.drawLine(cursorX, cursorY - crossLen, cursorX, cursorY + crossLen, crossPaint)
    }

    private fun drawDiagnostic(canvas: Canvas) {
        val text = diagnosticText ?: return
        diagTextPaint.color = diagnosticColor
        val textWidth = diagTextPaint.measureText(text)
        val x = (width - textWidth) / 2f
        val y = height / 2f
        val padding = 30f
        canvas.drawRoundRect(
            x - padding, y - 40f,
            x + textWidth + padding, y + 20f,
            15f, 15f, diagBgPaint
        )
        canvas.drawText(text, x, y - 8f, diagTextPaint)
    }
}
