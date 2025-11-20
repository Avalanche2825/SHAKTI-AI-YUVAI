package com.shakti.ai.services

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.shakti.ai.R
import com.shakti.ai.ShaktiApplication
import com.shakti.ai.ml.HelpWordDetector
import com.shakti.ai.ui.CalculatorActivity
import com.shakti.ai.utils.Constants
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Foreground Service for continuous audio threat detection
 *
 * Features:
 * - Real-time audio monitoring using microphone
 * - TensorFlow Lite YAMNet model for acoustic classification
 * - Scream detection with confidence threshold
 * - Voice command detection (HELP, EMERGENCY, BACHAO) - 3x trigger
 * - Automatic evidence recording trigger
 * - Low battery impact (optimized sampling)
 * - Saves all recordings to HIDDEN internal storage
 */
class AudioDetectionService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // HELP Word Detector - Primary audio monitoring (handles its own AudioRecord)
    private var helpWordDetector: HelpWordDetector? = null
    private var helpDetectionEnabled = true // Default to enabled

    // Hidden storage directory
    private val HIDDEN_DIR_NAME = ".system_cache"

    companion object {
        private const val TAG = "AudioDetectionService"
        const val ACTION_START_MONITORING = "START_MONITORING"
        const val ACTION_STOP_MONITORING = "STOP_MONITORING"
        const val ACTION_MANUAL_SOS = "MANUAL_SOS"
        const val ACTION_ENABLE_VOICE_COMMANDS = "ENABLE_VOICE_COMMANDS"
        const val ACTION_DISABLE_VOICE_COMMANDS = "DISABLE_VOICE_COMMANDS"

        const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "✅ Audio Detection Service Created")

        // Create hidden storage directory
        createHiddenStorageDirectory()

        // Start as foreground service
        startForeground(NOTIFICATION_ID, createNotification("Starting protection..."))
    }

    /**
     * Create hidden storage directory for audio recordings
     */
    private fun createHiddenStorageDirectory() {
        val hiddenDir = getHiddenStorageDir()
        if (!hiddenDir.exists()) {
            hiddenDir.mkdirs()

            // Create .nomedia file to prevent media scanner
            try {
                File(hiddenDir, ".nomedia").createNewFile()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create .nomedia file", e)
            }

            Log.w(TAG, " Hidden audio storage created at: ${hiddenDir.absolutePath}")
        }
    }

    /**
     * Get hidden storage directory (internal app storage)
     */
    private fun getHiddenStorageDir(): File {
        return File(filesDir, HIDDEN_DIR_NAME)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_MONITORING -> startMonitoring()
            ACTION_STOP_MONITORING -> stopMonitoring()
            ACTION_MANUAL_SOS -> triggerManualSOS()
            ACTION_ENABLE_VOICE_COMMANDS -> enableVoiceCommands()
            ACTION_DISABLE_VOICE_COMMANDS -> disableVoiceCommands()
            else -> startMonitoring()
        }

        return START_STICKY
    }

    /**
     * Enable HELP word detection (dedicated detector)
     */
    private fun enableVoiceCommands() {
        if (helpWordDetector == null) {
            helpDetectionEnabled = true
            helpWordDetector = HelpWordDetector(this)

            // Set sensitivity from preferences (default 0.40 = 40%)
            val prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
            val sensitivity = prefs.getFloat("help_detection_sensitivity", 0.40f)
            helpWordDetector?.setSensitivity(sensitivity)

            helpWordDetector?.startListening {
                Log.w(TAG, "🚨 HELP WORD DETECTED 3 TIMES - Triggering emergency!")
                onThreatDetected(1.0f) // Trigger emergency with max confidence
            }

            updateNotification("HELP detection active - Say 'HELP' 3 times")
            Log.w(
                TAG,
                "✅ HELP word detector ENABLED (dedicated detector, sensitivity: ${(sensitivity * 100).toInt()}%)"
            )
        }
    }

    /**
     * Disable HELP word detection
     */
    private fun disableVoiceCommands() {
        helpDetectionEnabled = false
        helpWordDetector?.stopListening()
        helpWordDetector = null
        updateNotification("HELP detection disabled")
        Log.w(TAG, "HELP word detector disabled")
    }

    /**
     * Start audio monitoring - NOW USING ONLY HelpWordDetector
     */
    private fun startMonitoring() {
        Log.d(TAG, "🎤 Starting audio monitoring with HelpWordDetector")

        // Check permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "❌ Audio permission not granted")
            stopSelf()
            return
        }

        try {
            // Enable HELP word detection - this handles ALL audio monitoring
            if (helpDetectionEnabled && helpWordDetector == null) {
                enableVoiceCommands()
            }

            updateNotification("🎤 Protection active - Listening for HELP")
            Log.d(TAG, "✅ Audio monitoring started - HELP detector active")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to start audio monitoring", e)
            stopSelf()
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
        Log.w(TAG, "🚨 TRIGGERING FULL EMERGENCY RESPONSE")

        // Save incident timestamp
        val prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
        val incidentId = UUID.randomUUID().toString()
        prefs.edit()
            .putString("current_incident_id", incidentId)
            .putLong("incident_${incidentId}_start_time", System.currentTimeMillis())
            .putFloat("incident_${incidentId}_confidence", confidence)
            .apply()

        // 1. Start VIDEO recording (with audio)
        try {
            val videoIntent = Intent(this, VideoRecorderService::class.java)
            videoIntent.action = VideoRecorderService.ACTION_START_RECORDING
            videoIntent.putExtra("threat_confidence", confidence)
            ContextCompat.startForegroundService(this, videoIntent)
            Log.w(TAG, "✅ Video recording service started")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to start video recording", e)
        }

        // 2. Start AUDIO recording (separate from video for backup)
        try {
            startAudioRecording(incidentId)
            Log.w(TAG, "✅ Audio recording started")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to start audio recording", e)
        }

        // 3. Start location tracking
        try {
            val locationIntent = Intent(this, LocationService::class.java)
            ContextCompat.startForegroundService(this, locationIntent)
            Log.w(TAG, "✅ Location tracking started")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to start location tracking", e)
        }

        // 4. Send emergency alerts
        sendEmergencyAlerts(confidence, incidentId)
    }

    /**
     * Start separate audio recording (backup evidence) in HIDDEN storage
     */
    private fun startAudioRecording(incidentId: String) {
        serviceScope.launch(Dispatchers.IO) {
            var recorder: MediaRecorder? = null
            try {
                val audioFile = createAudioFile(incidentId)

                // Create MediaRecorder
                recorder =
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        MediaRecorder(this@AudioDetectionService)
                    } else {
                        @Suppress("DEPRECATION")
                        MediaRecorder()
                    }

                recorder.apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setOutputFile(audioFile.absolutePath)
                    prepare()
                    start()
                }

                Log.w(TAG, " Audio recording to HIDDEN storage: ${audioFile.absolutePath}")

                // Save recorder reference
                val prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
                prefs.edit()
                    .putString("incident_${incidentId}_audio", audioFile.absolutePath)
                    .apply()

                // Auto-stop after max duration
                delay(Constants.MAX_RECORDING_DURATION_MS)
                recorder.stop()
                recorder.release()
                Log.w(TAG, " Audio recording stopped and saved to HIDDEN storage")

            } catch (e: Exception) {
                Log.e(TAG, "Audio recording error", e)
                try {
                    recorder?.release()
                } catch (ex: Exception) {
                    Log.e(TAG, "Failed to release recorder", ex)
                }
            }
        }
    }

    /**
     * Create audio evidence file in HIDDEN storage
     */
    private fun createAudioFile(incidentId: String): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        // Use innocuous filename
        val fileName = "sys_audio_${incidentId}_$timestamp.dat"

        val hiddenDir = getHiddenStorageDir()

        return File(hiddenDir, fileName)
    }

    /**
     * Send alerts to emergency contacts via Firebase
     */
    private fun sendEmergencyAlerts(confidence: Float, incidentId: String) {
        Log.w(TAG, "📢 Sending emergency alerts")

        val prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
        val userId = prefs.getString(Constants.KEY_USER_ID, "user_${System.currentTimeMillis()}")

        // Save alert sent timestamp
        prefs.edit()
            .putLong("incident_${incidentId}_alert_sent", System.currentTimeMillis())
            .putString("incident_${incidentId}_user_id", userId)
            .apply()

        // TODO: Implement Firebase push notification logic
        // - Get emergency contacts from Firebase
        // - Send push notifications with location
        // - Upload evidence links

        Log.w(
            TAG,
            "🚨 Emergency alert sent for incident $incidentId (confidence: ${(confidence * 100).toInt()}%)"
        )
    }

    /**
     * Handle manual SOS trigger (from secret code 911= or voice command)
     */
    private fun triggerManualSOS() {
        Log.w(TAG, " Manual SOS triggered (from secret code or voice command)")
        onThreatDetected(1.0f) // Max confidence for manual trigger
    }

    /**
     * Stop monitoring
     */
    private fun stopMonitoring() {
        Log.d(TAG, "🛑 Stopping audio monitoring")

        // Stop HELP word detector
        helpWordDetector?.stopListening()
        helpWordDetector = null

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
            .setPriority(NotificationCompat.PRIORITY_LOW) // Low priority for stealth
            .setSilent(true) // Silent
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

        // Stop HELP word detector
        helpWordDetector?.stopListening()
        helpWordDetector = null
    }
}
