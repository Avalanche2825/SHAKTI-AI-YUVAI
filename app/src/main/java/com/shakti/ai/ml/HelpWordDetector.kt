package com.shakti.ai.ml

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.*
import kotlin.math.*

/**
 * Simplified HELP Word Detector
 *
 * ULTRA-SENSITIVE: Detects ANY loud sound (including speech)
 * Perfect for emergency situations - better to over-detect than miss a real emergency
 *
 * Detection Method:
 * - Monitors audio energy (loudness)
 * - Triggers on ANY sound above threshold
 * - No complex pattern matching (more reliable)
 * - 3 detections within 8 seconds = EMERGENCY
 */
class HelpWordDetector(private val context: Context) {

    companion object {
        private const val TAG = "HelpWordDetector"
    }

    // Configuration
    private var isListening = false
    private var sensitivity = 0.30f // LOWER = MORE SENSITIVE (default 30%)

    // Detection parameters
    private val detectionWindow = 8000L // 8 seconds
    private val requiredDetections = 3   // Need 3 loud sounds
    private val minTimeBetweenDetections = 500L // 500ms between detections

    // Audio parameters
    private val sampleRate = 16000 // 16kHz sampling rate
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ) * 2

    // Detection history
    private val detectionTimestamps = mutableListOf<Long>()
    private var lastDetectionTime = 0L

    // Audio recording
    private var audioRecord: AudioRecord? = null
    private var detectionJob: Job? = null

    // Stats
    private var samplesProcessed = 0
    private var lastLogTime = 0L
    private var detectionAttempts = 0

    /**
     * Set sensitivity (0.0 to 1.0)
     * LOWER = MORE SENSITIVE
     * 0.1 = Detects whispers
     * 0.3 = Detects normal speech (DEFAULT)
     * 0.5 = Detects loud speech only
     * 0.7 = Detects shouting only
     */
    fun setSensitivity(value: Float) {
        sensitivity = value.coerceIn(0.1f, 0.9f)
        Log.d(
            TAG,
            "⚙️ Sensitivity set to: ${(sensitivity * 100).toInt()}% (lower = more sensitive)"
        )
    }

    /**
     * Start listening for loud sounds (HELP detection)
     */
    @SuppressLint("MissingPermission")
    fun startListening(onHelpDetected: () -> Unit) {
        if (isListening) {
            Log.w(TAG, "Already listening")
            return
        }

        Log.d(TAG, "🎤 Starting ULTRA-SENSITIVE audio detection...")
        Log.d(TAG, "📊 Sample rate: $sampleRate Hz")
        Log.d(TAG, "📦 Buffer size: $bufferSize bytes")
        Log.d(TAG, "⚙️ Sensitivity: ${(sensitivity * 100).toInt()}% (LOWER = MORE SENSITIVE)")
        Log.d(TAG, "🎯 Will trigger on ANY loud sound!")

        isListening = true
        detectionTimestamps.clear()
        lastDetectionTime = 0L
        samplesProcessed = 0
        detectionAttempts = 0
        lastLogTime = System.currentTimeMillis()

        try {
            // Initialize audio recorder
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                throw IllegalStateException("❌ AudioRecord failed to initialize - state: ${audioRecord?.state}")
            }

            Log.d(TAG, "✅ AudioRecord initialized")
            audioRecord?.startRecording()
            Log.d(TAG, "🎙️ Recording started - state: ${audioRecord?.recordingState}")

            if (audioRecord?.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
                throw IllegalStateException("❌ AudioRecord is not recording! State: ${audioRecord?.recordingState}")
            }

            Log.i(TAG, "✅ MICROPHONE IS ACTIVE!")
            Log.i(TAG, "🔊 Speak loudly or say HELP to test detection!")

            // Start detection in background
            detectionJob = CoroutineScope(Dispatchers.IO).launch {
                processAudioStream(onHelpDetected)
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to start: ${e.message}", e)
            e.printStackTrace()
            isListening = false
            stopListening()
        }
    }

    /**
     * Stop listening
     */
    fun stopListening() {
        Log.d(TAG, "🛑 Stopping audio detection...")
        isListening = false

        try {
            detectionJob?.cancel()
            detectionJob = null

            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null

            Log.d(TAG, "✅ Stopped. Samples: $samplesProcessed, Detections: $detectionAttempts")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping: ${e.message}", e)
        }

        detectionTimestamps.clear()
        broadcastDetectionUpdate(0)
    }

    /**
     * Main audio processing loop - SIMPLIFIED FOR RELIABILITY
     */
    private suspend fun processAudioStream(onHelpDetected: () -> Unit) =
        withContext(Dispatchers.IO) {
            val audioBuffer = ShortArray(bufferSize)
            val analysisWindowSize = sampleRate / 8 // 125ms windows (faster detection)
            val analysisBuffer = mutableListOf<Short>()

            var consecutiveZeroReads = 0
            var maxEnergySeenSoFar = 0.0f

            Log.d(TAG, "🔄 Audio processing loop STARTED")
            Log.d(TAG, "🎯 Waiting for audio input...")

            while (isListening) {
                try {
                    // Read audio data
                    val readSize = audioRecord?.read(audioBuffer, 0, audioBuffer.size) ?: 0

                    if (readSize > 0) {
                        consecutiveZeroReads = 0
                        samplesProcessed += readSize

                        // Get amplitude range
                        val maxAmp = audioBuffer.take(readSize).maxOrNull() ?: 0
                        val minAmp = audioBuffer.take(readSize).minOrNull() ?: 0
                        val range = maxAmp - minAmp

                        // Log detailed stats every 2 seconds
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastLogTime > 2000) {
                            Log.d(
                                TAG,
                                "📊 Samples: $samplesProcessed | Range: [$minAmp, $maxAmp] | Detections: $detectionAttempts"
                            )
                            Log.d(TAG, "🔊 Max energy seen: ${(maxEnergySeenSoFar * 100).toInt()}%")
                            lastLogTime = currentTime
                        }

                        // Add to analysis buffer
                        analysisBuffer.addAll(audioBuffer.take(readSize))

                        // Process in smaller windows for faster detection
                        if (analysisBuffer.size >= analysisWindowSize) {
                            val windowData = analysisBuffer.take(analysisWindowSize).toShortArray()
                            analysisBuffer.clear()

                            // Calculate simple energy
                            val energy = calculateSimpleEnergy(windowData)

                            // Track max energy for calibration
                            if (energy > maxEnergySeenSoFar) {
                                maxEnergySeenSoFar = energy
                            }

                            // Broadcast for visualization (ALWAYS)
                            broadcastAudioLevel(energy, range.toDouble())

                            // Simple detection: ANY energy above threshold
                            detectionAttempts++
                            if (detectLoudSound(energy)) {
                                Log.i(
                                    TAG,
                                    "🎯 LOUD SOUND DETECTED! Energy: ${(energy * 100).toInt()}%"
                                )
                                handleDetection(onHelpDetected)
                            }
                        }
                    } else if (readSize == 0) {
                        consecutiveZeroReads++
                        if (consecutiveZeroReads == 10) {
                            Log.w(TAG, "⚠️ No audio data - check microphone permission!")
                        }
                        if (consecutiveZeroReads > 50) {
                            Log.e(TAG, "❌ CRITICAL: $consecutiveZeroReads consecutive zero reads!")
                        }
                    } else {
                        Log.e(TAG, "❌ AudioRecord.read() error code: $readSize")
                    }

                    // Faster updates = smoother visualization
                    delay(20) // 50 FPS

                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    Log.e(TAG, "❌ Error: ${e.message}", e)
                }
            }

            Log.d(TAG, "🛑 Audio processing loop ENDED")
        }

    /**
     * SIMPLIFIED ENERGY CALCULATION
     * Just RMS (Root Mean Square) - simple and reliable
     */
    private fun calculateSimpleEnergy(samples: ShortArray): Float {
        if (samples.isEmpty()) return 0f

        var sum = 0.0
        for (sample in samples) {
            val normalized = sample.toDouble() / 32768.0 // Normalize to -1.0 to 1.0
            sum += normalized * normalized
        }

        val rms = sqrt(sum / samples.size)
        return rms.toFloat()
    }

    /**
     * SIMPLIFIED DETECTION
     * Returns true if energy is above threshold (loud sound detected)
     */
    private fun detectLoudSound(energy: Float): Boolean {
        // Dynamic threshold based on sensitivity
        // At 30% sensitivity: threshold = 0.015 (very sensitive)
        // At 50% sensitivity: threshold = 0.05 (moderate)
        // At 70% sensitivity: threshold = 0.15 (only loud sounds)
        val threshold = sensitivity * 0.3f

        val detected = energy > threshold

        if (detected) {
            Log.d(
                TAG,
                "✅ Detection! Energy: ${(energy * 100).toInt()}% > Threshold: ${(threshold * 100).toInt()}%"
            )
        }

        return detected
    }

    /**
     * Handle detection (3 detections = emergency)
     */
    private suspend fun handleDetection(onHelpDetected: () -> Unit) {
        val currentTime = System.currentTimeMillis()

        // Debounce (prevent too-fast duplicate detections)
        if (currentTime - lastDetectionTime < minTimeBetweenDetections) {
            Log.d(TAG, "⏭️ Skipping (debounce)")
            return
        }

        lastDetectionTime = currentTime
        detectionTimestamps.add(currentTime)

        // Remove old detections outside time window
        val before = detectionTimestamps.size
        detectionTimestamps.removeAll { currentTime - it > detectionWindow }
        val removed = before - detectionTimestamps.size

        if (removed > 0) {
            Log.d(TAG, "🗑️ Removed $removed old detections (outside 8s window)")
        }

        val count = detectionTimestamps.size
        Log.i(TAG, "📈 Detection count: $count / $requiredDetections")

        // Broadcast UI update
        broadcastDetectionUpdate(count)

        // Check if we have enough detections
        if (count >= requiredDetections) {
            Log.i(TAG, "🚨 EMERGENCY TRIGGERED! ($count detections)")

            withContext(Dispatchers.Main) {
                onHelpDetected()
            }

            // Clear and cooldown
            detectionTimestamps.clear()
            broadcastDetectionUpdate(0)
            Log.d(TAG, "⏸️ 5 second cooldown...")
            delay(5000)
            Log.d(TAG, "▶️ Cooldown ended, listening again")
        }
    }

    /**
     * Broadcast detection count to UI (for 3 dots indicator)
     */
    private fun broadcastDetectionUpdate(count: Int) {
        try {
            val intent = Intent("com.shakti.ai.HELP_DETECTION_UPDATE")
            intent.putExtra("detection_count", count)
            intent.putExtra("total_required", requiredDetections)
            context.sendBroadcast(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Broadcast audio level for visualization
     */
    private fun broadcastAudioLevel(energy: Float, amplitudeRange: Double) {
        try {
            val intent = Intent("com.shakti.ai.AUDIO_LEVEL_UPDATE")
            intent.putExtra("audio_energy", energy)
            intent.putExtra("audio_frequency", amplitudeRange)
            intent.putExtra("is_speech", energy > 0.01f) // Very low threshold
            context.sendBroadcast(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Get current detection count
     */
    fun getCurrentDetectionCount(): Int {
        val currentTime = System.currentTimeMillis()
        detectionTimestamps.removeAll { currentTime - it > detectionWindow }
        return detectionTimestamps.size
    }

    /**
     * Get time until window reset
     */
    fun getTimeUntilReset(): Long {
        if (detectionTimestamps.isEmpty()) return 0
        val oldestDetection = detectionTimestamps.minOrNull() ?: return 0
        val elapsed = System.currentTimeMillis() - oldestDetection
        return (detectionWindow - elapsed).coerceAtLeast(0)
    }

    /**
     * Check if currently listening
     */
    fun isCurrentlyListening(): Boolean = isListening
}
