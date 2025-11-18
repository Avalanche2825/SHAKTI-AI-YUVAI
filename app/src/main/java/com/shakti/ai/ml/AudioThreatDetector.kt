package com.shakti.ai.ml

import android.content.Context
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.max

/**
 * TensorFlow Lite wrapper for YAMNet acoustic threat detection
 *
 * YAMNet is a pre-trained deep neural network that classifies audio into 521 categories
 * We use it to detect: screams, shouts, distress sounds, aggressive speech
 *
 * Model details:
 * - Input: Audio waveform (15600 samples at 16kHz ≈ 0.975 seconds)
 * - Output: 521-class probability distribution
 * - Relevant classes for threat detection: indices 7, 36, 37, 146, 381
 */
class AudioThreatDetector(context: Context) {

    private var interpreter: Interpreter? = null
    private val modelInputSize = 15600 // YAMNet expects 15600 samples

    // YAMNet class indices for threat-related sounds
    private val THREAT_CLASS_INDICES = mapOf(
        7 to "Scream",          // High-pitched screaming
        36 to "Shout",          // Loud shouting
        37 to "Yell",           // Distress yelling
        146 to "Crying",        // Sobbing/crying
        381 to "Gasp"           // Sudden gasp
    )

    init {
        try {
            // Load TFLite model from assets
            val modelFile = FileUtil.loadMappedFile(context, "models/audio_threat_model.tflite")

            val options = Interpreter.Options().apply {
                setNumThreads(2) // Use 2 threads for faster inference
                setUseNNAPI(false) // Disable NNAPI for compatibility
            }

            interpreter = Interpreter(modelFile, options)

        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Failed to load threat detection model: ${e.message}")
        }
    }

    /**
     * Detect threat in audio samples
     *
     * @param audioSamples Float array of audio samples (normalized -1.0 to 1.0)
     * @return Confidence score (0.0 to 1.0) - higher means more likely a threat
     */
    fun detectThreat(audioSamples: FloatArray): Float {
        try {
            // Ensure we have enough samples
            val processedAudio = prepareInput(audioSamples)

            // Prepare input buffer
            val inputBuffer = ByteBuffer.allocateDirect(modelInputSize * 4) // 4 bytes per float
            inputBuffer.order(ByteOrder.nativeOrder())
            inputBuffer.rewind()

            processedAudio.forEach { sample ->
                inputBuffer.putFloat(sample)
            }

            // Prepare output buffer (521 classes)
            val outputBuffer = ByteBuffer.allocateDirect(521 * 4)
            outputBuffer.order(ByteOrder.nativeOrder())

            // Run inference
            interpreter?.run(inputBuffer, outputBuffer)

            // Extract probabilities
            outputBuffer.rewind()
            val probabilities = FloatArray(521) { outputBuffer.float }

            // Calculate max threat probability
            val threatScore = calculateThreatScore(probabilities)

            return threatScore

        } catch (e: Exception) {
            e.printStackTrace()
            return 0.0f
        }
    }

    /**
     * Prepare audio input (resize/pad to model input size)
     */
    private fun prepareInput(audio: FloatArray): FloatArray {
        return when {
            audio.size == modelInputSize -> audio
            audio.size > modelInputSize -> {
                // Take middle portion
                val start = (audio.size - modelInputSize) / 2
                audio.copyOfRange(start, start + modelInputSize)
            }

            else -> {
                // Pad with zeros
                FloatArray(modelInputSize) { i ->
                    if (i < audio.size) audio[i] else 0f
                }
            }
        }
    }

    /**
     * Calculate threat score from YAMNet probabilities
     *
     * We look at the probability of threat-related classes (scream, shout, yell, etc.)
     * and return the maximum probability among them
     */
    private fun calculateThreatScore(probabilities: FloatArray): Float {
        var maxThreatProb = 0f

        for ((classIndex, className) in THREAT_CLASS_INDICES) {
            if (classIndex < probabilities.size) {
                val prob = probabilities[classIndex]
                if (prob > maxThreatProb) {
                    maxThreatProb = prob

                    // Log detected threat type (for debugging)
                    if (prob > 0.5f) {
                        android.util.Log.d(
                            "AudioThreatDetector",
                            "Detected: $className with confidence ${(prob * 100).toInt()}%"
                        )
                    }
                }
            }
        }

        return maxThreatProb
    }

    /**
     * Alternative: Detect using keyword spotting (if TFLite model not available)
     * This is a fallback method using amplitude-based detection
     */
    fun detectByAmplitude(audioSamples: FloatArray): Float {
        // Calculate RMS (Root Mean Square) amplitude
        var sum = 0.0
        for (sample in audioSamples) {
            sum += sample * sample
        }
        val rms = kotlin.math.sqrt(sum / audioSamples.size)

        // Normalize to 0-1 range (typical scream has RMS > 0.3)
        val normalizedAmplitude = (rms * 3.33).coerceIn(0.0, 1.0)

        return normalizedAmplitude.toFloat()
    }

    /**
     * Check if audio contains sudden loud spike (potential scream)
     */
    fun detectSuddenSpike(audioSamples: FloatArray): Boolean {
        if (audioSamples.isEmpty()) return false

        // Calculate peak amplitude
        val peak = audioSamples.maxOrNull() ?: 0f

        // Calculate average amplitude
        val average = audioSamples.average().toFloat()

        // If peak is significantly higher than average, it's a spike
        val ratio = if (average > 0) peak / average else 0f

        return ratio > 5.0f && peak > 0.5f
    }

    /**
     * Close interpreter and release resources
     */
    fun close() {
        interpreter?.close()
        interpreter = null
    }
}
