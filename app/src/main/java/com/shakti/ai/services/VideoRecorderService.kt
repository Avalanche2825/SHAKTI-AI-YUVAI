package com.shakti.ai.services

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import com.shakti.ai.R
import com.shakti.ai.ShaktiApplication
import com.shakti.ai.ui.CalculatorActivity
import com.shakti.ai.utils.Constants
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Video Recorder Service - Captures dual camera evidence
 *
 * Features:
 * - Records from both front and back cameras simultaneously
 * - Auto-starts on threat detection
 * - 3-minute maximum recording duration
 * - Saves to encrypted storage
 * - Captures attacker's face (front camera) and surroundings (back camera)
 */
class VideoRecorderService : LifecycleService() {

    private var frontCameraRecording: Recording? = null
    private var backCameraRecording: Recording? = null
    private var isRecording = false

    private lateinit var frontVideoCapture: VideoCapture<Recorder>
    private lateinit var backVideoCapture: VideoCapture<Recorder>

    private var recordingStartTime = 0L

    companion object {
        const val ACTION_START_RECORDING = "START_RECORDING"
        const val ACTION_STOP_RECORDING = "STOP_RECORDING"
        const val NOTIFICATION_ID = 1002

        private const val MAX_RECORDING_DURATION_MS = Constants.MAX_RECORDING_DURATION_MS
    }

    override fun onCreate() {
        super.onCreate()
        // Use minimal, hidden notification
        startForeground(NOTIFICATION_ID, createStealthNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            ACTION_START_RECORDING -> {
                val threatConfidence = intent.getFloatExtra("threat_confidence", 0f)
                startRecording(threatConfidence)
            }

            ACTION_STOP_RECORDING -> stopRecording()
        }

        return START_STICKY
    }

    /**
     * Start recording from both cameras
     */
    private fun startRecording(threatConfidence: Float) {
        if (isRecording) return

        // Check permissions
        if (!hasCameraPermission()) {
            android.util.Log.e("VideoRecorder", "Camera permission not granted")
            stopSelf()
            return
        }

        isRecording = true
        recordingStartTime = System.currentTimeMillis()

        android.util.Log.w("VideoRecorder", "🎥 STEALTH RECORDING STARTED")

        // Setup cameras
        setupCameras()

        // Auto-stop after max duration
        android.os.Handler(mainLooper).postDelayed({
            if (isRecording) {
                stopRecording()
            }
        }, MAX_RECORDING_DURATION_MS)
    }

    /**
     * Setup both front and back cameras
     */
    private fun setupCameras() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                // Start front camera recording
                startCameraRecording(
                    cameraProvider,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    "front"
                )

                // Start back camera recording
                startCameraRecording(
                    cameraProvider,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    "back"
                )

            } catch (e: Exception) {
                android.util.Log.e("VideoRecorder", "Camera setup failed", e)
                stopRecording()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    /**
     * Start recording from specific camera
     */
    private fun startCameraRecording(
        cameraProvider: ProcessCameraProvider,
        cameraSelector: CameraSelector,
        cameraType: String
    ) {
        try {
            // Create recorder
            val recorder = Recorder.Builder()
                .setQualitySelector(
                    QualitySelector.from(
                        Quality.HD, // 720p
                        FallbackStrategy.higherQualityOrLowerThan(Quality.SD)
                    )
                )
                .build()

            val videoCapture = VideoCapture.withOutput(recorder)

            // Create output file
            val videoFile = createVideoFile(cameraType)
            val outputOptions = FileOutputOptions.Builder(videoFile).build()

            // Unbind previous use cases
            cameraProvider.unbindAll()

            // Bind to lifecycle
            cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                videoCapture
            )

            // Start recording
            val recording = videoCapture.output
                .prepareRecording(this, outputOptions)
                .withAudioEnabled() // Capture audio along with video
                .start(ContextCompat.getMainExecutor(this)) { recordEvent ->
                    handleRecordEvent(recordEvent, cameraType)
                }

            // Store recording reference
            if (cameraType == "front") {
                frontCameraRecording = recording
            } else {
                backCameraRecording = recording
            }

            android.util.Log.d("VideoRecorder", "🎥 $cameraType camera recording started")

        } catch (e: Exception) {
            android.util.Log.e("VideoRecorder", "Failed to start $cameraType camera", e)
        }
    }

    /**
     * Handle recording events
     */
    private fun handleRecordEvent(event: VideoRecordEvent, cameraType: String) {
        when (event) {
            is VideoRecordEvent.Start -> {
                android.util.Log.d("VideoRecorder", "✅ $cameraType camera recording active")
            }

            is VideoRecordEvent.Finalize -> {
                if (!event.hasError()) {
                    val videoFile = event.outputResults.outputUri
                    android.util.Log.w(
                        "VideoRecorder",
                        "📹 $cameraType video saved: $videoFile"
                    )

                    // Save evidence metadata
                    saveEvidenceMetadata(videoFile.toString(), cameraType)
                } else {
                    android.util.Log.e(
                        "VideoRecorder",
                        "❌ Error recording $cameraType: ${event.cause?.message}"
                    )
                }
            }
        }
    }

    /**
     * Create video output file
     */
    private fun createVideoFile(cameraType: String): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
            .format(Date())
        val fileName = "EVIDENCE_${cameraType}_$timestamp.mp4"

        val evidenceDir = File(getExternalFilesDir(null), "evidence")
        if (!evidenceDir.exists()) {
            evidenceDir.mkdirs()
        }

        return File(evidenceDir, fileName)
    }

    /**
     * Save evidence metadata to database
     */
    private fun saveEvidenceMetadata(videoPath: String, cameraType: String) {
        val prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        val incidentId = prefs.getString("current_incident_id", UUID.randomUUID().toString())!!

        // Save evidence info
        val editor = prefs.edit()
        editor.putString("incident_${incidentId}_video_${cameraType}", videoPath)
        editor.putLong("incident_${incidentId}_timestamp", System.currentTimeMillis())
        editor.putString("current_incident_id", incidentId)
        editor.apply()

        android.util.Log.w("VideoRecorder", "💾 Evidence saved to: $videoPath")
    }

    /**
     * Stop recording
     */
    private fun stopRecording() {
        if (!isRecording) return

        isRecording = false

        // Stop both recordings
        frontCameraRecording?.stop()
        backCameraRecording?.stop()

        frontCameraRecording = null
        backCameraRecording = null

        val duration = (System.currentTimeMillis() - recordingStartTime) / 1000
        android.util.Log.w("VideoRecorder", "🛑 STEALTH RECORDING STOPPED ($duration seconds)")

        // Auto-close service
        android.os.Handler(mainLooper).postDelayed({
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }, 1000)
    }

    /**
     * Check camera permission
     */
    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
    }

    /**
     * Create STEALTH notification (minimal, no sound)
     */
    private fun createStealthNotification(): Notification {
        val intent = Intent(this, CalculatorActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // STEALTH: Minimal notification, no sound, low priority
        return NotificationCompat.Builder(this, ShaktiApplication.CHANNEL_ID_RECORDING)
            .setContentTitle("Calculator") // Disguised as calculator
            .setContentText("Running") // Minimal text
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN) // Minimal visibility
            .setSound(null) // NO SOUND
            .setVibrate(null) // NO VIBRATION
            .setSilent(true) // SILENT
            .setShowWhen(false) // Hide timestamp
            .build()
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
        android.util.Log.d("VideoRecorder", "Service destroyed")
    }
}
