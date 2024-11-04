package com.example.licentaincercarea1

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.ProgressBar

class CircularGradientProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ProgressBar(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 20f
        strokeCap = Paint.Cap.ROUND
    }
    private val strokeWidth = 20f
    private val rect = RectF()

    override fun onDraw(canvas: Canvas) {
        val width = width.toFloat()
        val height = height.toFloat()

        // Define the oval shape for circular progress
        val size = Math.min(width, height) - strokeWidth
        rect.set(
            strokeWidth / 2,
            strokeWidth / 2,
            size + strokeWidth / 2,
            size + strokeWidth / 2
        )

        // Background circle (empty grey circle when no progress)
        paint.color = Color.LTGRAY
        paint.shader = null  // No gradient for background
        canvas.drawArc(rect, 0f, 360f, false, paint)

        // Don't draw progress if there's none
        if (progress == 0) return

        // Gradient colors based on progress level (blue -> green -> red)
        val gradient = SweepGradient(
            width / 2, height / 2,
            intArrayOf(
                Color.parseColor("#ADD8E6"),  // Light Blue
                Color.parseColor("#32CD32"),  // Green (at goal)
                Color.parseColor("#FF4500")   // Red (over goal)
            ),
            floatArrayOf(0f, 0.5f, 1f)  // Color stops: blue -> green -> red
        )

        paint.shader = gradient

        // Rotate canvas to start from 12 o'clock
        canvas.rotate(-90f, width / 2, height / 2)

        // Calculate sweep angle based on progress
        val sweepAngle = (progress.toFloat() / max.toFloat()) * 360f
        canvas.drawArc(rect, 0f, sweepAngle, false, paint)
    }
}
