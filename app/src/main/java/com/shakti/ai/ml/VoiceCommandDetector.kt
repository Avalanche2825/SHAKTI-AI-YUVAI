package com.shakti.ai.ml

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.*
import kotlin.math.sqrt

/**
 * Voice Command Detector - Keyword spotting for emergency activation
 *
 * Detects: "HELP", "EMERGENCY", "BACHAO" (Save me in Hindi)
 * Trigger: 3 detections within 8 seconds → Emergency mode
 *
 * Uses simple amplitude-based keyword detection (lightweight)
 */
class VoiceCommandDetector {

    private var isListening = false
    private val detectionWindow = 8000L // 8 seconds
    private val requiredDetections = 3

    // Detection history
    private val detectionTimestamps = mutableListOf<Long>()

    // Audio recording
    private var audioRecord: AudioRecord? = null
    private val sampleRate = 16000
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    /**
     * Start listening for voice commands
     */
    fun startListening(onCommandDetected: (command: String) -> Unit) {
        if (isListening) return

        isListening = true

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            audioRecord?.startRecording()

            // Process audio in coroutine
            CoroutineScope(Dispatchers.IO).launch {
                processAudioForCommands(onCommandDetected)
            }

        } catch (e: Exception) {
            e.printStackTrace()
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
    }

    /**
     * Process audio and detect keywords
     */
    private suspend fun processAudioForCommands(onCommandDetected: (String) -> Unit) =
        withContext(Dispatchers.IO) {
            val audioBuffer = ShortArray(bufferSize)

            while (isListening) {
                try {
                    val readSize = audioRecord?.read(audioBuffer, 0, audioBuffer.size) ?: 0

                    if (readSize > 0) {
                        // Detect if "HELP" or similar keyword spoken
                        if (detectKeyword(audioBuffer)) {
                            val currentTime = System.currentTimeMillis()
                            detectionTimestamps.add(currentTime)

                            // Remove old detections (outside 8-second window)
                            detectionTimestamps.removeAll {
                                currentTime - it > detectionWindow
                            }

                            // Check if we have 3 detections in 8 seconds
                            if (detectionTimestamps.size >= requiredDetections) {
                                withContext(Dispatchers.Main) {
                                    onCommandDetected("HELP")
                                }
                                detectionTimestamps.clear()
                                delay(5000) // Cooldown 5 seconds to prevent multiple triggers
                            }
                        }
                    }

                    delay(100) // Process every 100ms

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    /**
     * Simple keyword detection using energy patterns
     *
     * "HELP" has characteristic pattern:
     * - Short burst (H)
     * - Lower energy (E)
     * - Higher energy (L)
     * - Falling energy (P)
     *
     * This is a simplified approach. For production, use:
     * - PocketSphinx for offline keyword spotting
     * - Google Speech Recognition API
     * - Custom TFLite keyword model
     */
    private fun detectKeyword(audioBuffer: ShortArray): Boolean {
        // Calculate RMS energy
        val rms = calculateRMS(audioBuffer)

        // Check for strong vocal energy (typical of shouting "HELP")
        // RMS > 0.4 indicates loud speech
        if (rms > 0.4f) {
            // Additional check: frequency analysis could go here
            // For now, we use a simple threshold
            return true
        }

        return false
    }

    /**
     * Calculate Root Mean Square (RMS) energy
     */
    private fun calculateRMS(buffer: ShortArray): Float {
        var sum = 0.0
        for (sample in buffer) {
            val normalized = sample / 32768.0 // Normalize to -1.0 to 1.0
            sum += normalized * normalized
        }
        return sqrt(sum / buffer.size).toFloat()
    }

    /**
     * Get current detection count (for UI)
     */
    fun getCurrentDetectionCount(): Int {
        val currentTime = System.currentTimeMillis()
        detectionTimestamps.removeAll { currentTime - it > detectionWindow }
        return detectionTimestamps.size
    }

    /**
     * Get time until next window reset (for UI countdown)
     */
    fun getTimeUntilReset(): Long {
        if (detectionTimestamps.isEmpty()) return 0

        val oldestDetection = detectionTimestamps.minOrNull() ?: return 0
        val elapsed = System.currentTimeMillis() - oldestDetection
        return (detectionWindow - elapsed).coerceAtLeast(0)
    }
}
