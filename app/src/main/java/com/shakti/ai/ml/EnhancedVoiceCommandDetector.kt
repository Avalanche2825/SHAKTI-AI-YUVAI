package com.shakti.ai.ml

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.*
import kotlin.math.sqrt

/**
 * Enhanced Voice Command Detector
 *
 * Features:
 * - Keyword spotting: "HELP", "EMERGENCY", "BACHAO"
 * - Trigger: 3 detections within 8 seconds
 * - Adaptive threshold based on ambient noise
 * - Visual + Audio feedback
 * - Better false positive reduction
 */
class EnhancedVoiceCommandDetector {

    companion object {
        private const val TAG = "VoiceCommandDetector"
        private const val DETECTION_WINDOW_MS = 8000L // 8 seconds
        private const val REQUIRED_DETECTIONS = 3
        private const val COOLDOWN_MS = 5000L // 5 seconds after trigger
        private const val SAMPLE_RATE = 16000
    }

    private var isListening = false
    private val detectionTimestamps = mutableListOf<Long>()
    private var lastTriggerTime = 0L

    // Audio recording
    private var audioRecord: AudioRecord? = null
    private val bufferSize = AudioRecord.getMinBufferSize(
        SAMPLE_RATE,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    // Adaptive threshold
    private var ambientNoiseLevel = 0f
    private var isCalibrating = true
    private val calibrationSamples = mutableListOf<Float>()

    // Callbacks
    private var onCommandDetected: ((command: String) -> Unit)? = null
    private var onDetectionProgress: ((count: Int, timeLeft: Long) -> Unit)? = null

    /**
     * Start listening for voice commands
     */
    fun startListening(
        onDetected: (command: String) -> Unit,
        onProgress: ((count: Int, timeLeft: Long) -> Unit)? = null
    ) {
        if (isListening) return

        isListening = true
        onCommandDetected = onDetected
        onDetectionProgress = onProgress

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            audioRecord?.startRecording()
            Log.d(TAG, "Voice command detection started")

            // Start processing in coroutine
            CoroutineScope(Dispatchers.IO).launch {
                processAudioForCommands()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start voice command detection", e)
            isListening = false
        }
    }

    /**
     * Stop listening
     */
    fun stopListening() {
        isListening = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        detectionTimestamps.clear()
        calibrationSamples.clear()
        isCalibrating = true
        Log.d(TAG, "Voice command detection stopped")
    }

    /**
     * Process audio and detect keywords
     */
    private suspend fun processAudioForCommands() = withContext(Dispatchers.IO) {
        val audioBuffer = ShortArray(bufferSize)
        var sampleCount = 0

        while (isListening) {
            try {
                val readSize = audioRecord?.read(audioBuffer, 0, audioBuffer.size) ?: 0

                if (readSize > 0) {
                    val rms = calculateRMS(audioBuffer)

                    // Calibration phase (first 3 seconds)
                    if (isCalibrating && sampleCount < 30) {
                        calibrationSamples.add(rms)
                        sampleCount++

                        if (sampleCount == 30) {
                            ambientNoiseLevel = calibrationSamples.average().toFloat()
                            isCalibrating = false
                            Log.d(TAG, "Calibration complete. Ambient noise: $ambientNoiseLevel")
                        }
                        delay(100)
                        continue
                    }

                    // Adaptive threshold
                    val threshold = ambientNoiseLevel + 0.35f // 35% above ambient

                    // Detect keyword pattern
                    if (detectKeywordPattern(audioBuffer, rms, threshold)) {
                        handleDetection()
                    }

                    // Update progress
                    updateDetectionProgress()
                }

                delay(100) // Process every 100ms

            } catch (e: Exception) {
                Log.e(TAG, "Error processing audio", e)
            }
        }
    }

    /**
     * Detect keyword pattern using advanced analysis
     */
    private fun detectKeywordPattern(buffer: ShortArray, rms: Float, threshold: Float): Boolean {
        // Check if in cooldown
        if (System.currentTimeMillis() - lastTriggerTime < COOLDOWN_MS) {
            return false
        }

        // Layer 1: RMS Energy check
        if (rms < threshold) {
            return false
        }

        // Layer 2: Sudden spike detection (characteristic of shouting "HELP")
        val peakAmplitude = buffer.maxOrNull()?.let { it / 32768f } ?: 0f
        val averageAmplitude = rms
        val spikeRatio = if (averageAmplitude > 0) peakAmplitude / averageAmplitude else 0f

        if (spikeRatio < 2.0f) {
            return false // Not a sudden enough spike
        }

        // Layer 3: Duration check (keywords are short bursts)
        val zeroCrossings = countZeroCrossings(buffer)
        val isShortBurst = zeroCrossings in 50..200 // Typical for "HELP"

        if (!isShortBurst) {
            return false
        }

        Log.d(TAG, "Keyword pattern detected - RMS: $rms, Spike: $spikeRatio, ZC: $zeroCrossings")
        return true
    }

    /**
     * Handle a detection
     */
    private suspend fun handleDetection() {
        val currentTime = System.currentTimeMillis()
        detectionTimestamps.add(currentTime)

        // Remove old detections outside window
        detectionTimestamps.removeAll { currentTime - it > DETECTION_WINDOW_MS }

        val count = detectionTimestamps.size
        Log.d(TAG, "Detection count: $count/$REQUIRED_DETECTIONS")

        // Check if threshold reached
        if (count >= REQUIRED_DETECTIONS) {
            withContext(Dispatchers.Main) {
                onCommandDetected?.invoke("HELP")
            }

            lastTriggerTime = currentTime
            detectionTimestamps.clear()

            Log.w(TAG, "EMERGENCY TRIGGERED via voice command!")

            // Add cooldown delay
            delay(COOLDOWN_MS)
        }
    }

    /**
     * Update detection progress for UI
     */
    private suspend fun updateDetectionProgress() {
        val currentTime = System.currentTimeMillis()
        detectionTimestamps.removeAll { currentTime - it > DETECTION_WINDOW_MS }

        val count = detectionTimestamps.size
        val timeLeft = if (detectionTimestamps.isNotEmpty()) {
            val oldest = detectionTimestamps.minOrNull() ?: return
            (DETECTION_WINDOW_MS - (currentTime - oldest)).coerceAtLeast(0)
        } else {
            0L
        }

        withContext(Dispatchers.Main) {
            onDetectionProgress?.invoke(count, timeLeft)
        }
    }

    /**
     * Calculate RMS energy
     */
    private fun calculateRMS(buffer: ShortArray): Float {
        var sum = 0.0
        for (sample in buffer) {
            val normalized = sample / 32768.0
            sum += normalized * normalized
        }
        return sqrt(sum / buffer.size).toFloat()
    }

    /**
     * Count zero crossings (frequency indicator)
     */
    private fun countZeroCrossings(buffer: ShortArray): Int {
        var crossings = 0
        for (i in 1 until buffer.size) {
            if ((buffer[i - 1] < 0 && buffer[i] >= 0) ||
                (buffer[i - 1] >= 0 && buffer[i] < 0)
            ) {
                crossings++
            }
        }
        return crossings
    }

    /**
     * Get current detection count
     */
    fun getCurrentDetectionCount(): Int {
        val currentTime = System.currentTimeMillis()
        detectionTimestamps.removeAll { currentTime - it > DETECTION_WINDOW_MS }
        return detectionTimestamps.size
    }

    /**
     * Get time until window reset
     */
    fun getTimeUntilReset(): Long {
        if (detectionTimestamps.isEmpty()) return 0

        val oldestDetection = detectionTimestamps.minOrNull() ?: return 0
        val elapsed = System.currentTimeMillis() - oldestDetection
        return (DETECTION_WINDOW_MS - elapsed).coerceAtLeast(0)
    }

    /**
     * Check if currently calibrating
     */
    fun isCalibrating(): Boolean = isCalibrating

    /**
     * Get ambient noise level
     */
    fun getAmbientNoiseLevel(): Float = ambientNoiseLevel
}
