package com.shakti.ai.services

import android.Manifest
import android.annotation.SuppressLint
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
import com.shakti.ai.data.EvidenceDatabase
import com.shakti.ai.data.EvidenceItem
import com.shakti.ai.data.IncidentRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Video Recorder Service - Captures dual camera evidence
 *
 * Features:
 * - Records from both front and back cameras simultaneously
 * - Auto-starts on threat detection
 * - 3-minute maximum recording duration
 * - Saves to HIDDEN INTERNAL storage (secure and private)
 * - Captures attacker's face (front camera) and surroundings (back camera)
 */
class VideoRecorderService : LifecycleService() {

    private var frontCameraRecording: Recording? = null
    private var backCameraRecording: Recording? = null
    private var isRecording = false

    private lateinit var frontVideoCapture: VideoCapture<Recorder>
    private lateinit var backVideoCapture: VideoCapture<Recorder>

    private var recordingStartTime = 0L
    private var currentIncidentId: String? = null

    private lateinit var database: EvidenceDatabase
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        const val ACTION_START_RECORDING = "START_RECORDING"
        const val ACTION_STOP_RECORDING = "STOP_RECORDING"
        const val NOTIFICATION_ID = 1002

        private const val MAX_RECORDING_DURATION_MS = Constants.MAX_RECORDING_DURATION_MS

        // Hidden directory name (appears innocuous)
        private const val HIDDEN_DIR_NAME = ".system_cache"
    }

    override fun onCreate() {
        super.onCreate()
        database = EvidenceDatabase.getDatabase(this)
        // Use minimal, STEALTH notification - NO SOUND, NO VIBRATION
        startForeground(NOTIFICATION_ID, createStealthNotification())

        // Create hidden storage directory
        createHiddenStorageDirectory()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            ACTION_START_RECORDING -> {
                val threatConfidence = intent.getFloatExtra("threat_confidence", 0f)
                val incidentId = intent.getStringExtra("incident_id")
                startRecording(threatConfidence, incidentId)
            }

            ACTION_STOP_RECORDING -> stopRecording()
        }

        return START_STICKY
    }

    /**
     * Create hidden storage directory for recordings
     * Directory is hidden (starts with .) and has innocuous name
     */
    private fun createHiddenStorageDirectory() {
        val hiddenDir = getHiddenStorageDir()
        if (!hiddenDir.exists()) {
            hiddenDir.mkdirs()

            // Create .nomedia file to prevent media scanner from indexing
            try {
                File(hiddenDir, ".nomedia").createNewFile()
            } catch (e: Exception) {
                android.util.Log.e("VideoRecorder", "Failed to create .nomedia file", e)
            }

            android.util.Log.w(
                "VideoRecorder",
                "✅ Hidden storage created at: ${hiddenDir.absolutePath}"
            )
        }
    }

    /**
     * Get hidden storage directory (internal app storage)
     * This is private to the app and hidden from file managers
     */
    private fun getHiddenStorageDir(): File {
        // Use internal storage (not accessible by other apps or user)
        return File(filesDir, HIDDEN_DIR_NAME)
    }

    /**
     * Start recording from both cameras
     */
    private fun startRecording(threatConfidence: Float, incidentIdFromIntent: String?) {
        if (isRecording) return

        // Check permissions
        if (!hasCameraPermission()) {
            android.util.Log.e("VideoRecorder", "Camera permission not granted")
            stopSelf()
            return
        }

        isRecording = true
        recordingStartTime = System.currentTimeMillis()

        // Use incident ID from intent (should always be provided now)
        currentIncidentId = incidentIdFromIntent

        if (currentIncidentId == null) {
            // Fallback: create new incident if not provided
            currentIncidentId = "incident_${System.currentTimeMillis()}"
            android.util.Log.w(
                "VideoRecorder",
                "⚠️ No incident_id provided, created new: $currentIncidentId"
            )

            // Create incident record in database
            serviceScope.launch {
                try {
                    database.incidentDao().insertIncident(
                        IncidentRecord(
                            id = currentIncidentId!!,
                            startTime = recordingStartTime,
                            triggerType = "manual_video",
                            confidence = threatConfidence
                        )
                    )
                    android.util.Log.w(
                        "VideoRecorder",
                        "✅ Fallback incident created in database: $currentIncidentId"
                    )
                } catch (e: Exception) {
                    android.util.Log.e("VideoRecorder", "❌ Failed to create fallback incident", e)
                    e.printStackTrace()
                }
            }
        } else {
            android.util.Log.w(
                "VideoRecorder",
                "✅ Using incident_id from intent: $currentIncidentId"
            )
        }

        android.util.Log.w(
            "VideoRecorder",
            "🎥 STEALTH RECORDING STARTED (Incident: $currentIncidentId)"
        )

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
    @SuppressLint("MissingPermission")
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

            // Create output file in HIDDEN storage
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
                    val videoPath = event.outputResults.outputUri.path
                    if (videoPath == null) {
                        android.util.Log.e("VideoRecorder", "❌ Video path is null for $cameraType")
                        return
                    }

                    val videoFile = File(videoPath)
                    if (!videoFile.exists()) {
                        android.util.Log.e(
                            "VideoRecorder",
                            "❌ Video file doesn't exist: $videoPath"
                        )
                        return
                    }

                    val fileSize = videoFile.length()
                    val duration = System.currentTimeMillis() - recordingStartTime

                    android.util.Log.w(
                        "VideoRecorder",
                        "📹 $cameraType video saved: $videoPath (${fileSize / 1024}KB, ${duration / 1000}s)"
                    )

                    // Save evidence metadata to DATABASE
                    val incidentId = currentIncidentId
                    if (incidentId != null) {
                        serviceScope.launch {
                            try {
                                val evidence = EvidenceItem(
                                    incidentId = incidentId,
                                    type = "video_$cameraType",
                                    filePath = videoPath,
                                    timestamp = System.currentTimeMillis(),
                                    duration = duration,
                                    fileSize = fileSize
                                )
                                database.evidenceDao().insertEvidence(evidence)
                                android.util.Log.w(
                                    "VideoRecorder",
                                    "💾 Evidence saved to DATABASE (incident: $incidentId, type: video_$cameraType)"
                                )

                                // Verify it was saved
                                val saved =
                                    database.evidenceDao().getEvidenceForIncident(incidentId)
                                android.util.Log.w(
                                    "VideoRecorder",
                                    "✅ Verification: ${saved.size} evidence items for incident $incidentId"
                                )
                            } catch (e: Exception) {
                                android.util.Log.e(
                                    "VideoRecorder",
                                    "❌ Failed to save evidence to database",
                                    e
                                )
                                e.printStackTrace()
                            }
                        }

                        // Also save to preferences (backward compatibility)
                        saveEvidenceMetadata(videoPath, cameraType)
                    } else {
                        android.util.Log.e(
                            "VideoRecorder",
                            "❌ No incident_id available, cannot save evidence"
                        )
                    }
                } else {
                    android.util.Log.e(
                        "VideoRecorder",
                        "❌ Error recording $cameraType: ${event.cause?.message}"
                    )
                    event.cause?.printStackTrace()
                }
            }
        }
    }

    /**
     * Create video output file in HIDDEN storage
     */
    private fun createVideoFile(cameraType: String): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
            .format(Date())
        // Use innocuous filename
        val fileName = "sys_${cameraType}_$timestamp.dat"

        val hiddenDir = getHiddenStorageDir()

        return File(hiddenDir, fileName)
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

        android.util.Log.w("VideoRecorder", "💾 Evidence saved to HIDDEN location: $videoPath")
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
     * Create STEALTH notification (minimal, no sound, no vibration)
     */
    private fun createStealthNotification(): Notification {
        val intent = Intent(this, CalculatorActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // MAXIMUM STEALTH: Minimal notification, NO sound, NO vibration, LOW priority
        return NotificationCompat.Builder(this, ShaktiApplication.CHANNEL_ID_RECORDING)
            .setContentTitle("System") // Generic title
            .setContentText("Running") // Minimal text
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Use system icon
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN) // Minimal visibility
            .setSound(null) // NO SOUND
            .setVibrate(null) // NO VIBRATION
            .setSilent(true) // SILENT
            .setShowWhen(false) // Hide timestamp
            .setVisibility(NotificationCompat.VISIBILITY_SECRET) // Hide from lock screen
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
        serviceScope.cancel()
        android.util.Log.d("VideoRecorder", "Service destroyed")
    }
}
