package com.shakti.ai.ui.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.abs
import kotlin.math.min

/**
 * Real-time Audio Waveform Visualizer
 *
 * Shows AI is actively listening and processing audio
 * Perfect for hackathon demo - makes ML visible!
 */
class AudioVisualizerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Waveform data
    private val waveformData = FloatArray(100) { 0f }
    private var currentIndex = 0

    // Paint objects
    private val waveformPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#32B8C6") // Teal
        style = Paint.Style.STROKE
        strokeWidth = 4f
        strokeCap = Paint.Cap.ROUND
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(
            0f, 0f, 0f, 200f,
            intArrayOf(
                Color.parseColor("#5532B8C6"),
                Color.parseColor("#0032B8C6")
            ),
            null,
            Shader.TileMode.CLAMP
        )
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#20FFFFFF")
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }

    private val path = Path()
    private val fillPath = Path()

    /**
     * Update waveform with new audio sample
     */
    fun updateWaveform(amplitude: Float) {
        waveformData[currentIndex] = amplitude.coerceIn(-1f, 1f)
        currentIndex = (currentIndex + 1) % waveformData.size
        invalidate() // Redraw
    }

    /**
     * Update with array of samples (for batch processing)
     */
    fun updateWaveformBatch(samples: FloatArray) {
        val step = samples.size / waveformData.size
        for (i in waveformData.indices) {
            val sampleIndex = i * step
            if (sampleIndex < samples.size) {
                waveformData[i] = samples[sampleIndex].coerceIn(-1f, 1f)
            }
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val centerY = height / 2

        // Draw grid lines (optional, for cool effect)
        drawGrid(canvas, width, height)

        // Draw waveform
        path.reset()
        fillPath.reset()

        val spacing = width / waveformData.size

        // Start fill path at bottom
        fillPath.moveTo(0f, height)

        for (i in waveformData.indices) {
            val index = (currentIndex + i) % waveformData.size
            val x = i * spacing
            val y = centerY + (waveformData[index] * centerY * 0.8f)

            if (i == 0) {
                path.moveTo(x, y)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        // Complete fill path
        fillPath.lineTo(width, height)
        fillPath.close()

        // Draw fill (gradient under waveform)
        canvas.drawPath(fillPath, fillPaint)

        // Draw waveform line
        canvas.drawPath(path, waveformPaint)

        // Draw center line
        canvas.drawLine(0f, centerY, width, centerY, gridPaint)
    }

    /**
     * Draw background grid
     */
    private fun drawGrid(canvas: Canvas, width: Float, height: Float) {
        val gridSpacing = height / 4

        for (i in 1..3) {
            val y = i * gridSpacing
            canvas.drawLine(0f, y, width, y, gridPaint)
        }
    }

    /**
     * Reset waveform to zero
     */
    fun reset() {
        waveformData.fill(0f)
        currentIndex = 0
        invalidate()
    }

    /**
     * Set waveform color
     */
    fun setWaveformColor(color: Int) {
        waveformPaint.color = color
        invalidate()
    }

    /**
     * Animate idle state (when no audio)
     */
    fun animateIdle() {
        // Create gentle sine wave
        for (i in waveformData.indices) {
            val angle = (i.toFloat() / waveformData.size) * Math.PI * 2
            waveformData[i] = (Math.sin(angle + System.currentTimeMillis() / 500.0) * 0.1).toFloat()
        }
        invalidate()

        // Continue animation
        postDelayed({ if (isAttachedToWindow) animateIdle() }, 50)
    }
}
