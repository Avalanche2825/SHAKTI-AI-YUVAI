package com.shakti.ai.ui.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.animation.ValueAnimator
import android.animation.AnimatorSet
import androidx.core.content.ContextCompat
import com.shakti.ai.R

/**
 * AI Confidence Meter Widget
 *
 * Shows real-time ML model confidence in a beautiful, animated meter
 * Makes AI visible to judges during demo!
 */
class AIConfidenceMeterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var currentConfidence = 0f
    private var targetConfidence = 0f
    private var animationProgress = 0f

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 42f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 28f
        color = Color.parseColor("#626C7C")
        textAlign = Paint.Align.CENTER
    }

    private val barBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E5E7EB")
        style = Paint.Style.FILL
    }

    private val barFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        maskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.NORMAL)
    }

    private var animator: ValueAnimator? = null

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null) // Enable blur
    }

    /**
     * Update confidence value with smooth animation
     */
    fun setConfidence(confidence: Float) {
        targetConfidence = confidence.coerceIn(0f, 1f)
        animateToTarget()
    }

    private fun animateToTarget() {
        animator?.cancel()

        animator = ValueAnimator.ofFloat(currentConfidence, targetConfidence).apply {
            duration = 500
            addUpdateListener { animation ->
                currentConfidence = animation.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()

        // Draw AI icon and label
        val labelY = 50f
        labelPaint.color = Color.parseColor("#626C7C")
        canvas.drawText("🤖 AI CONFIDENCE", width / 2, labelY, labelPaint)

        // Draw confidence percentage
        val percentage = (currentConfidence * 100).toInt()
        textPaint.color = getConfidenceColor(currentConfidence)
        canvas.drawText("$percentage%", width / 2, labelY + 60f, textPaint)

        // Draw confidence bar
        val barTop = labelY + 90f
        val barHeight = 24f
        val barMargin = 40f
        val barWidth = width - (barMargin * 2)

        // Background bar
        val backgroundRect = RectF(
            barMargin,
            barTop,
            width - barMargin,
            barTop + barHeight
        )
        canvas.drawRoundRect(backgroundRect, 12f, 12f, barBackgroundPaint)

        // Fill bar with gradient
        val fillWidth = barWidth * currentConfidence
        val fillRect = RectF(
            barMargin,
            barTop,
            barMargin + fillWidth,
            barTop + barHeight
        )

        // Create gradient based on confidence
        val gradient = LinearGradient(
            barMargin, barTop,
            barMargin + fillWidth, barTop,
            getGradientColors(currentConfidence),
            null,
            Shader.TileMode.CLAMP
        )
        barFillPaint.shader = gradient

        // Draw glow effect for high confidence
        if (currentConfidence > 0.7f) {
            glowPaint.color = getConfidenceColor(currentConfidence)
            glowPaint.alpha = (currentConfidence * 100).toInt()
            canvas.drawRoundRect(fillRect, 12f, 12f, glowPaint)
        }

        // Draw fill
        canvas.drawRoundRect(fillRect, 12f, 12f, barFillPaint)

        // Draw confidence level markers
        drawMarkers(canvas, barTop, barHeight, barMargin, barWidth)

        // Draw status text
        val statusY = barTop + barHeight + 35f
        labelPaint.textSize = 24f
        labelPaint.color = getConfidenceColor(currentConfidence)
        canvas.drawText(getStatusText(currentConfidence), width / 2, statusY, labelPaint)
    }

    private fun drawMarkers(
        canvas: Canvas,
        barTop: Float,
        barHeight: Float,
        barMargin: Float,
        barWidth: Float
    ) {
        val markerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#9CA3AF")
            strokeWidth = 2f
        }

        // Draw vertical markers at 25%, 50%, 75%
        val markers = listOf(0.25f, 0.5f, 0.75f)
        for (marker in markers) {
            val x = barMargin + (barWidth * marker)
            canvas.drawLine(x, barTop - 8f, x, barTop + barHeight + 8f, markerPaint)
        }
    }

    private fun getConfidenceColor(confidence: Float): Int {
        return when {
            confidence >= 0.8f -> Color.parseColor("#10B981") // Green
            confidence >= 0.6f -> Color.parseColor("#F59E0B") // Yellow
            confidence >= 0.4f -> Color.parseColor("#F97316") // Orange
            else -> Color.parseColor("#EF4444") // Red
        }
    }

    private fun getGradientColors(confidence: Float): IntArray {
        return when {
            confidence >= 0.8f -> intArrayOf(
                Color.parseColor("#10B981"),
                Color.parseColor("#059669")
            )

            confidence >= 0.6f -> intArrayOf(
                Color.parseColor("#F59E0B"),
                Color.parseColor("#D97706")
            )

            confidence >= 0.4f -> intArrayOf(
                Color.parseColor("#F97316"),
                Color.parseColor("#EA580C")
            )

            else -> intArrayOf(
                Color.parseColor("#EF4444"),
                Color.parseColor("#DC2626")
            )
        }
    }

    private fun getStatusText(confidence: Float): String {
        return when {
            confidence >= 0.8f -> "HIGH CONFIDENCE - Analyzing..."
            confidence >= 0.6f -> "MEDIUM CONFIDENCE - Monitoring..."
            confidence >= 0.4f -> "LOW CONFIDENCE - Listening..."
            else -> "VERY LOW - Background Noise"
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = 180
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> minOf(desiredHeight, heightSize)
            else -> desiredHeight
        }

        setMeasuredDimension(
            MeasureSpec.getSize(widthMeasureSpec),
            height
        )
    }

    /**
     * Start pulsing animation for active monitoring
     */
    fun startPulseAnimation() {
        val pulseAnimator = ValueAnimator.ofFloat(0.7f, 0.9f, 0.7f).apply {
            duration = 2000
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                animationProgress = it.animatedValue as Float
                invalidate()
            }
        }
        pulseAnimator.start()
    }

    /**
     * Stop animations
     */
    fun stopAnimations() {
        animator?.cancel()
    }
}
