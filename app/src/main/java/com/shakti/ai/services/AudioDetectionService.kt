package com.shakti.ai.services

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.shakti.ai.R
import com.shakti.ai.ShaktiApplication
import com.shakti.ai.ui.CalculatorActivity
import com.shakti.ai.utils.Constants
import kotlinx.coroutines.*

/**
 * Foreground Service for continuous audio threat detection
 *
 * Features:
 * - Real-time audio monitoring using microphone
 * - TensorFlow Lite YAMNet model for acoustic classification
 * - Scream detection with confidence threshold
 * - Automatic evidence recording trigger
 * - Low battery impact (optimized sampling)
 */
class AudioDetectionService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Audio components
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private val audioBuffer = ShortArray(Constants.AUDIO_BUFFER_SIZE)

    // ML Model - TODO: Initialize when AudioThreatDetector is implemented
    // private lateinit var threatDetector: AudioThreatDetector

    // Detection state
    private var consecutiveThreats = 0
    private val THREAT_CONFIRMATION_COUNT = 3 // Need 3 consecutive detections

    companion object {
        private const val TAG = "AudioDetectionService"
        const val ACTION_START_MONITORING = "START_MONITORING"
        const val ACTION_STOP_MONITORING = "STOP_MONITORING"
        const val ACTION_MANUAL_SOS = "MANUAL_SOS"

        const val NOTIFICATION_ID = 1001
        private const val SAMPLE_RATE = Constants.AUDIO_SAMPLE_RATE
        private const val CHANNEL_SIZE = AudioFormat.CHANNEL_IN_MONO
        private const val ENCODING = AudioFormat.ENCODING_PCM_16BIT
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Audio Detection Service Created")

        // Initialize ML model
        // threatDetector = AudioThreatDetector(this)

        // Start as foreground service
        startForeground(NOTIFICATION_ID, createNotification("Monitoring active"))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_MONITORING -> startMonitoring()
            ACTION_STOP_MONITORING -> stopMonitoring()
            ACTION_MANUAL_SOS -> triggerManualSOS()
            else -> startMonitoring()
        }

        return START_STICKY
    }

    /**
     * Start audio monitoring
     */
    private fun startMonitoring() {
        if (isRecording) return

        Log.d(TAG, "Starting audio monitoring")

        // Check permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "Audio permission not granted")
            stopSelf()
            return
        }

        try {
            val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_SIZE, ENCODING)

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_SIZE,
                ENCODING,
                bufferSize
            )

            audioRecord?.startRecording()
            isRecording = true

            // Start audio processing in coroutine
            serviceScope.launch {
                processAudioStream()
            }

            updateNotification("Protection active - Listening")
            Log.d(TAG, "Audio monitoring started successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start audio monitoring", e)
            stopSelf()
        }
    }

    /**
     * Process audio stream and detect threats
     */
    private suspend fun processAudioStream() = withContext(Dispatchers.IO) {
        Log.d(TAG, "Audio processing loop started")

        while (isRecording) {
            try {
                // Read audio data
                val readSize = audioRecord?.read(audioBuffer, 0, audioBuffer.size) ?: 0

                if (readSize > 0) {
                    // Convert to float array for ML model
                    val floatAudio = convertToFloatArray(audioBuffer)

                    // Run threat detection
                    val threatConfidence = detectThreat(floatAudio)

                    // Check if threat detected
                    if (threatConfidence > Constants.SCREAM_CONFIDENCE_THRESHOLD) {
                        consecutiveThreats++

                        Log.d(
                            TAG,
                            "Potential threat detected: $threatConfidence (count: $consecutiveThreats)"
                        )

                        // Require multiple consecutive detections to reduce false positives
                        if (consecutiveThreats >= THREAT_CONFIRMATION_COUNT) {
                            onThreatDetected(threatConfidence)
                            consecutiveThreats = 0 // Reset counter
                        }
                    } else {
                        consecutiveThreats = 0 // Reset on non-threat
                    }
                }

                // Small delay to reduce CPU usage
                delay(100) // Process every 100ms

            } catch (e: Exception) {
                Log.e(TAG, "Error processing audio", e)
            }
        }

        Log.d(TAG, "Audio processing loop ended")
    }

    /**
     * Detect threat using ML model
     * TODO: Replace with actual AudioThreatDetector when implemented
     */
    private fun detectThreat(audioData: FloatArray): Float {
        // Placeholder: Simulate threat detection based on audio amplitude
        val amplitude = audioData.map { kotlin.math.abs(it) }.average().toFloat()

        // TODO: Replace with actual TFLite model inference
        // return threatDetector.detectThreat(audioData)

        // For now, return amplitude as confidence (0.0 to 1.0)
        return amplitude.coerceIn(0.0f, 1.0f)
    }

    /**
     * Convert short audio samples to float (-1.0 to 1.0)
     */
    private fun convertToFloatArray(shorts: ShortArray): FloatArray {
        return FloatArray(shorts.size) { i ->
            shorts[i] / 32768.0f // Normalize to -1.0 to 1.0
        }
    }

    /**
     * Handle detected threat
     */
    private fun onThreatDetected(confidence: Float) {
        serviceScope.launch(Dispatchers.Main) {
            Log.w(TAG, "THREAT CONFIRMED with confidence: $confidence")

            // Update notification
            updateNotification("⚠️ THREAT DETECTED (${(confidence * 100).toInt()}%)")

            // Trigger emergency response
            triggerEmergencyResponse(confidence)
        }
    }

    /**
     * Trigger full emergency response
     */
    private fun triggerEmergencyResponse(confidence: Float) {
        Log.w(TAG, "Triggering emergency response")

        // Start video recording
        val videoIntent = Intent(this, VideoRecorderService::class.java)
        videoIntent.action = "START_RECORDING"
        videoIntent.putExtra("threat_confidence", confidence)
        ContextCompat.startForegroundService(this, videoIntent)

        // Start location tracking
        val locationIntent = Intent(this, LocationService::class.java)
        ContextCompat.startForegroundService(this, locationIntent)

        // TODO: Send Bluetooth alert to nearby users
        // val bluetoothIntent = Intent(this, BluetoothService::class.java)
        // bluetoothIntent.action = "SEND_ALERT"
        // ContextCompat.startForegroundService(this, bluetoothIntent)

        // Send Firebase alert to emergency contacts
        sendEmergencyAlerts(confidence)
    }

    /**
     * Send alerts to emergency contacts via Firebase
     */
    private fun sendEmergencyAlerts(confidence: Float) {
        Log.d(TAG, "Sending emergency alerts")

        val prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
        val userId = prefs.getString(Constants.KEY_USER_ID, null)

        // TODO: Implement Firebase push notification logic
        // - Get emergency contacts from Firebase
        // - Send push notifications
        // - Upload location and evidence links

        Log.w(TAG, "Emergency alert triggered for user $userId with confidence $confidence")
    }

    /**
     * Handle manual SOS trigger (from secret code 911=)
     */
    private fun triggerManualSOS() {
        Log.w(TAG, "Manual SOS triggered")
        onThreatDetected(1.0f) // Max confidence for manual trigger
    }

    /**
     * Stop monitoring
     */
    private fun stopMonitoring() {
        Log.d(TAG, "Stopping audio monitoring")

        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        consecutiveThreats = 0

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    /**
     * Create foreground service notification
     */
    private fun createNotification(message: String): Notification {
        val intent = Intent(this, CalculatorActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, ShaktiApplication.CHANNEL_ID_THREAT)
            .setContentTitle("SHAKTI Protection")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    /**
     * Update notification message
     */
    private fun updateNotification(message: String) {
        val notification = createNotification(message)
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Audio Detection Service Destroyed")

        stopMonitoring()
        serviceScope.cancel()

        // TODO: Close ML model when implemented
        // threatDetector.close()
    }
}
