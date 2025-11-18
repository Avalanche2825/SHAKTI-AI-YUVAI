package com.shakti.ai.ui.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import kotlin.math.sin
import kotlin.math.cos

/**
 * Enhanced Audio Visualizer with Glassmorphism
 *
 * Features:
 * - Real-time waveform with smooth interpolation
 * - Glassmorphic background
 * - Gradient colors based on threat level
 * - Animated confidence meter
 * - Particle effects for detections
 * - Frequency bars
 */
class EnhancedAudioVisualizerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Waveform data
    private val waveformData = FloatArray(100) { 0f }
    private val smoothedWaveformData = FloatArray(100) { 0f }
    private var currentIndex = 0

    // Threat confidence
    private var threatConfidence = 0f
    private var targetConfidence = 0f
    private var confidenceAnimator: ValueAnimator? = null

    // Colors based on threat level
    private val safeColor = Color.parseColor("#32B8C6")      // Teal
    private val cautionColor = Color.parseColor("#FFA726")   // Orange
    private val warningColor = Color.parseColor("#EF5350")   // Red

    // Paint objects
    private val waveformPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#20FFFFFF")
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }

    private val confidencePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 48f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    // Paths
    private val waveformPath = Path()
    private val fillPath = Path()

    // Particles for detection events
    private val particles = mutableListOf<Particle>()

    // Animation
    private var animationProgress = 0f

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null) // Hardware acceleration
    }

    /**
     * Update waveform with new audio sample
     */
    fun updateWaveform(amplitude: Float) {
        waveformData[currentIndex] = amplitude.coerceIn(-1f, 1f)
        currentIndex = (currentIndex + 1) % waveformData.size

        // Smooth the waveform
        smoothWaveform()

        invalidate()
    }

    /**
     * Update with batch samples
     */
    fun updateWaveformBatch(samples: FloatArray) {
        val step = samples.size / waveformData.size
        for (i in waveformData.indices) {
            val sampleIndex = i * step
            if (sampleIndex < samples.size) {
                waveformData[i] = samples[sampleIndex].coerceIn(-1f, 1f)
            }
        }
        smoothWaveform()
        invalidate()
    }

    /**
     * Set threat confidence (0.0 to 1.0)
     */
    fun setThreatConfidence(confidence: Float) {
        targetConfidence = confidence.coerceIn(0f, 1f)

        // Animate confidence change
        confidenceAnimator?.cancel()
        confidenceAnimator = ValueAnimator.ofFloat(threatConfidence, targetConfidence).apply {
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                threatConfidence = it.animatedValue as Float
                invalidate()
            }
            start()
        }

        // Add particle burst if high confidence
        if (confidence > 0.7f) {
            addParticleBurst()
        }
    }

    /**
     * Smooth waveform using simple moving average
     */
    private fun smoothWaveform() {
        val windowSize = 3
        for (i in waveformData.indices) {
            var sum = 0f
            var count = 0
            for (j in -windowSize..windowSize) {
                val index = (i + j + waveformData.size) % waveformData.size
                sum += waveformData[index]
                count++
            }
            smoothedWaveformData[i] = sum / count
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val centerY = height / 2

        // Draw glassmorphic background
        drawGlassmorphicBackground(canvas, width, height)

        // Draw grid
        drawGrid(canvas, width, height)

        // Draw waveform
        drawWaveform(canvas, width, centerY)

        // Draw confidence meter
        drawConfidenceMeter(canvas, width, height)

        // Draw particles
        drawParticles(canvas)

        // Continue animation
        animationProgress += 0.02f
        if (particles.isNotEmpty()) {
            postInvalidateOnAnimation()
        }
    }

    /**
     * Draw glassmorphic background
     */
    private fun drawGlassmorphicBackground(canvas: Canvas, width: Float, height: Float) {
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            // Gradient based on threat level
            val startColor = interpolateColor(safeColor, warningColor, threatConfidence)
            val endColor = Color.argb(
                (255 * 0.05f).toInt(),
                Color.red(startColor),
                Color.green(startColor),
                Color.blue(startColor)
            )

            shader = LinearGradient(
                0f, 0f, 0f, height,
                startColor and 0x20FFFFFF,
                endColor,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width, height, bgPaint)
    }

    /**
     * Draw grid lines
     */
    private fun drawGrid(canvas: Canvas, width: Float, height: Float) {
        val gridSpacing = height / 4
        for (i in 1..3) {
            val y = i * gridSpacing
            canvas.drawLine(0f, y, width, y, gridPaint)
        }
    }

    /**
     * Draw waveform with gradient
     */
    private fun drawWaveform(canvas: Canvas, width: Float, centerY: Float) {
        waveformPath.reset()
        fillPath.reset()

        val spacing = width / smoothedWaveformData.size
        val amplitude = centerY * 0.8f

        // Set color based on threat
        val color = interpolateColor(safeColor, warningColor, threatConfidence)
        waveformPaint.color = color

        // Create gradient shader
        fillPaint.shader = LinearGradient(
            0f, 0f, 0f, centerY * 2,
            intArrayOf(
                color and 0x80FFFFFF,  // 50% alpha at top
                color and 0x00FFFFFF   // 0% alpha at bottom
            ),
            null,
            Shader.TileMode.CLAMP
        )

        // Start fill path
        fillPath.moveTo(0f, centerY * 2)

        for (i in smoothedWaveformData.indices) {
            val index = (currentIndex + i) % smoothedWaveformData.size
            val x = i * spacing
            val y = centerY + (smoothedWaveformData[index] * amplitude)

            if (i == 0) {
                waveformPath.moveTo(x, y)
                fillPath.lineTo(x, y)
            } else {
                // Use quadratic bezier for smooth curves
                val prevX = (i - 1) * spacing
                val prevIndex = (currentIndex + i - 1) % smoothedWaveformData.size
                val prevY = centerY + (smoothedWaveformData[prevIndex] * amplitude)
                val controlX = (prevX + x) / 2
                val controlY = (prevY + y) / 2

                waveformPath.quadTo(prevX, prevY, controlX, controlY)
                fillPath.quadTo(prevX, prevY, controlX, controlY)
            }
        }

        // Complete fill path
        fillPath.lineTo(width, centerY * 2)
        fillPath.close()

        // Draw fill and waveform
        canvas.drawPath(fillPath, fillPaint)
        canvas.drawPath(waveformPath, waveformPaint)

        // Draw center line
        canvas.drawLine(0f, centerY, width, centerY, gridPaint)
    }

    /**
     * Draw confidence meter
     */
    private fun drawConfidenceMeter(canvas: Canvas, width: Float, height: Float) {
        val meterHeight = 60f
        val meterY = height - meterHeight - 20f
        val meterWidth = width - 40f
        val meterX = 20f

        // Background
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#20FFFFFF")
            style = Paint.Style.FILL
        }
        val bgRect = RectF(meterX, meterY, meterX + meterWidth, meterY + 30f)
        canvas.drawRoundRect(bgRect, 15f, 15f, bgPaint)

        // Confidence bar
        val barWidth = meterWidth * threatConfidence
        val barColor = interpolateColor(safeColor, warningColor, threatConfidence)
        val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = barColor
            style = Paint.Style.FILL
        }
        val barRect = RectF(meterX, meterY, meterX + barWidth, meterY + 30f)
        canvas.drawRoundRect(barRect, 15f, 15f, barPaint)

        // Percentage text
        confidencePaint.color = Color.WHITE
        confidencePaint.textSize = 36f
        val percentage = (threatConfidence * 100).toInt()
        canvas.drawText("$percentage%", meterX + meterWidth / 2, meterY + 20f, confidencePaint)
    }

    /**
     * Draw particle effects
     */
    private fun drawParticles(canvas: Canvas) {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val particle = iterator.next()
            particle.update()

            if (particle.isDead()) {
                iterator.remove()
            } else {
                particlePaint.color = particle.color
                particlePaint.alpha = (particle.alpha * 255).toInt()
                canvas.drawCircle(particle.x, particle.y, particle.size, particlePaint)
            }
        }
    }

    /**
     * Add particle burst effect
     */
    private fun addParticleBurst() {
        val centerX = width / 2f
        val centerY = height / 2f

        for (i in 0..10) {
            val angle = (i * 36f) * Math.PI / 180
            val speed = 3f + Math.random().toFloat() * 2f
            val color = interpolateColor(safeColor, warningColor, threatConfidence)

            particles.add(Particle(centerX, centerY, speed, angle.toFloat(), color))
        }
    }

    /**
     * Interpolate between two colors
     */
    private fun interpolateColor(startColor: Int, endColor: Int, fraction: Float): Int {
        val f = fraction.coerceIn(0f, 1f)
        val startA = (startColor shr 24) and 0xff
        val startR = (startColor shr 16) and 0xff
        val startG = (startColor shr 8) and 0xff
        val startB = startColor and 0xff

        val endA = (endColor shr 24) and 0xff
        val endR = (endColor shr 16) and 0xff
        val endG = (endColor shr 8) and 0xff
        val endB = endColor and 0xff

        return ((startA + (endA - startA) * f).toInt() shl 24) or
                ((startR + (endR - startR) * f).toInt() shl 16) or
                ((startG + (endG - startG) * f).toInt() shl 8) or
                (startB + (endB - startB) * f).toInt()
    }

    /**
     * Reset visualizer
     */
    fun reset() {
        waveformData.fill(0f)
        smoothedWaveformData.fill(0f)
        currentIndex = 0
        threatConfidence = 0f
        targetConfidence = 0f
        particles.clear()
        invalidate()
    }

    /**
     * Animate idle state
     */
    fun animateIdle() {
        for (i in waveformData.indices) {
            val angle = (i.toFloat() / waveformData.size) * Math.PI * 2
            waveformData[i] = (sin(angle + animationProgress) * 0.1).toFloat()
        }
        smoothWaveform()
        invalidate()

        postDelayed({ if (isAttachedToWindow) animateIdle() }, 50)
    }

    /**
     * Particle class for effects
     */
    private data class Particle(
        var x: Float,
        var y: Float,
        val speed: Float,
        val angle: Float,
        val color: Int
    ) {
        var alpha = 1f
        var size = 8f
        var life = 1f

        fun update() {
            x += cos(angle.toDouble()).toFloat() * speed
            y += sin(angle.toDouble()).toFloat() * speed
            life -= 0.02f
            alpha = life.coerceIn(0f, 1f)
            size = 8f * life
        }

        fun isDead() = life <= 0f
    }
}
