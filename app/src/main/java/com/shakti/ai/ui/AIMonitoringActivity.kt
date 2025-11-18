package com.shakti.ai.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.shakti.ai.databinding.ActivityAiMonitoringBinding
import com.shakti.ai.ml.VoiceCommandDetector
import com.shakti.ai.services.AudioDetectionService
import com.shakti.ai.utils.Constants
import java.text.SimpleDateFormat
import java.util.*

/**
 * AI Monitoring Activity - Real-time audio monitoring dashboard
 *
 * Features:
 * - Live audio waveform visualization
 * - Voice command detection ("HELP" - 3 times in 8 seconds)
 * - Threat detection confidence display
 * - Emergency SOS trigger
 * - Detection log (last 50 events)
 */
class AIMonitoringActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAiMonitoringBinding
    private var voiceCommandDetector: VoiceCommandDetector? = null
    private val handler = Handler(Looper.getMainLooper())
    private val detectionLog = mutableListOf<DetectionEvent>()
    private lateinit var logAdapter: DetectionLogAdapter

    // Simulated audio data for visualization
    private var isMonitoring = false

    companion object {
        private const val REQUEST_RECORD_AUDIO = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAiMonitoringBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupButtons()
        setupDetectionLog()
        checkPermissions()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "AI Monitoring Dashboard"
            setDisplayHomeAsUpEnabled(true)
        }

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupButtons() {
        // Start Monitoring Button
        binding.btnStartMonitoring.setOnClickListener {
            if (isMonitoring) {
                stopMonitoring()
            } else {
                startMonitoring()
            }
        }

        // Emergency SOS Button
        binding.btnEmergencySOS.setOnClickListener {
            triggerEmergencySOS()
        }

        // Voice Command Toggle
        binding.switchVoiceCommand.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startVoiceCommandDetection()
            } else {
                stopVoiceCommandDetection()
            }
        }
    }

    private fun setupDetectionLog() {
        logAdapter = DetectionLogAdapter(detectionLog)
        binding.recyclerDetectionLog.apply {
            layoutManager = LinearLayoutManager(this@AIMonitoringActivity)
            adapter = logAdapter
        }

        // Load detection history from preferences
        loadDetectionHistory()
    }

    private fun loadDetectionHistory() {
        val prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)

        // Load last 50 detections
        val detectionCount = prefs.getInt("detection_count", 0)
        val maxToLoad = minOf(detectionCount, 50)

        for (i in 0 until maxToLoad) {
            val timestamp = prefs.getLong("detection_${i}_time", 0)
            val type = prefs.getString("detection_${i}_type", "Unknown") ?: "Unknown"
            val confidence = prefs.getFloat("detection_${i}_confidence", 0f)

            if (timestamp > 0) {
                detectionLog.add(
                    0, DetectionEvent(
                        timestamp = timestamp,
                        type = type,
                        confidence = confidence,
                        isThreat = confidence > 0.75f
                    )
                )
            }
        }

        logAdapter.notifyDataSetChanged()

        // Update empty state
        if (detectionLog.isEmpty()) {
            binding.tvEmptyState.visibility = android.view.View.VISIBLE
            binding.recyclerDetectionLog.visibility = android.view.View.GONE
        } else {
            binding.tvEmptyState.visibility = android.view.View.GONE
            binding.recyclerDetectionLog.visibility = android.view.View.VISIBLE
        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO
            )
        }
    }

    private fun startMonitoring() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Microphone permission required", Toast.LENGTH_SHORT).show()
            return
        }

        isMonitoring = true
        binding.btnStartMonitoring.text = "Stop Monitoring"
        binding.tvMonitoringStatus.text = "🎤 Listening for threats..."

        // Start audio detection service
        val intent = Intent(this, AudioDetectionService::class.java)
        intent.action = AudioDetectionService.ACTION_START_MONITORING
        ContextCompat.startForegroundService(this, intent)

        // Start visualizer animation
        binding.audioVisualizer.animateIdle()

        // Simulate real-time audio updates
        startSimulatedAudioUpdates()

        Toast.makeText(this, "Protection active", Toast.LENGTH_SHORT).show()
    }

    private fun stopMonitoring() {
        isMonitoring = false
        binding.btnStartMonitoring.text = "Start Monitoring"
        binding.tvMonitoringStatus.text = "Monitoring stopped"

        // Stop audio detection service
        val intent = Intent(this, AudioDetectionService::class.java)
        intent.action = AudioDetectionService.ACTION_STOP_MONITORING
        startService(intent)

        // Reset visualizer
        binding.audioVisualizer.reset()

        handler.removeCallbacksAndMessages(null)

        Toast.makeText(this, "Monitoring stopped", Toast.LENGTH_SHORT).show()
    }

    private fun startVoiceCommandDetection() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            binding.switchVoiceCommand.isChecked = false
            Toast.makeText(this, "Microphone permission required", Toast.LENGTH_SHORT).show()
            return
        }

        voiceCommandDetector = VoiceCommandDetector()
        voiceCommandDetector?.startListening { command ->
            // Voice command detected
            runOnUiThread {
                binding.tvVoiceCommandStatus.text = "🗣️ Detected: $command - Triggering SOS!"
                triggerEmergencySOS()
            }
        }

        binding.tvVoiceCommandStatus.text = "🗣️ Listening for: HELP, EMERGENCY, BACHAO"

        // Update detection count periodically
        updateVoiceCommandUI()
    }

    private fun stopVoiceCommandDetection() {
        voiceCommandDetector?.stopListening()
        voiceCommandDetector = null
        binding.tvVoiceCommandStatus.text = "Voice command detection off"
        handler.removeCallbacks(voiceCommandUpdateRunnable)
    }

    private val voiceCommandUpdateRunnable = object : Runnable {
        override fun run() {
            voiceCommandDetector?.let { detector ->
                val count = detector.getCurrentDetectionCount()
                val timeLeft = detector.getTimeUntilReset() / 1000

                if (count > 0) {
                    binding.tvVoiceCommandStatus.text =
                        "🗣️ Detected: $count/3 (${timeLeft}s left)"
                } else {
                    binding.tvVoiceCommandStatus.text =
                        "🗣️ Listening for: HELP, EMERGENCY, BACHAO"
                }
            }

            handler.postDelayed(this, 500)
        }
    }

    private fun updateVoiceCommandUI() {
        handler.post(voiceCommandUpdateRunnable)
    }

    private fun startSimulatedAudioUpdates() {
        val updateRunnable = object : Runnable {
            override fun run() {
                if (isMonitoring) {
                    // Simulate random audio amplitude
                    val amplitude = (Math.random() * 0.5).toFloat()
                    binding.audioVisualizer.updateWaveform(amplitude)

                    // Simulate threat confidence
                    val confidence = (Math.random() * 30).toInt() // 0-30% (low confidence)
                    binding.tvThreatConfidence.text = "Threat Confidence: $confidence%"

                    // Update stats
                    updateStats()

                    handler.postDelayed(this, 100)
                }
            }
        }
        handler.post(updateRunnable)
    }

    private fun updateStats() {
        // Simulated stats - in real app, get from service
        val prefs = getSharedPreferences("shakti_prefs", MODE_PRIVATE)
        val audioSamplesProcessed = prefs.getInt("audio_samples_processed", 0) + 1
        prefs.edit().putInt("audio_samples_processed", audioSamplesProcessed).apply()

        binding.tvAudioSamplesProcessed.text = "Samples: ${audioSamplesProcessed}"
        binding.tvActiveTime.text = "Active: ${audioSamplesProcessed / 10}s"
    }

    private fun triggerEmergencySOS() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("⚠️ Emergency SOS")
            .setMessage(
                "This will immediately:\n\n" +
                        "• Start recording evidence\n" +
                        "• Alert emergency contacts\n" +
                        "• Share your location\n" +
                        "• Notify nearby users\n\n" +
                        "Continue?"
            )
            .setPositiveButton("YES - EMERGENCY") { _, _ ->
                activateSOS()
            }
            .setNegativeButton("Cancel", null)
            .setCancelable(true)
            .show()
    }

    private fun activateSOS() {
        val intent = Intent(this, AudioDetectionService::class.java)
        intent.action = AudioDetectionService.ACTION_MANUAL_SOS
        ContextCompat.startForegroundService(this, intent)

        Toast.makeText(this, "🚨 SOS ACTIVATED", Toast.LENGTH_LONG).show()
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_RECORD_AUDIO) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    this,
                    "Audio permission required for monitoring",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVoiceCommandDetection()
        handler.removeCallbacksAndMessages(null)
    }

    fun addDetectionEvent(type: String, confidence: Float) {
        val event = DetectionEvent(
            timestamp = System.currentTimeMillis(),
            type = type,
            confidence = confidence,
            isThreat = confidence > 0.75f
        )

        detectionLog.add(0, event)

        // Keep only last 50
        if (detectionLog.size > 50) {
            detectionLog.removeAt(detectionLog.lastIndex)
        }

        logAdapter.notifyItemInserted(0)
        binding.recyclerDetectionLog.scrollToPosition(0)

        // Update empty state
        binding.tvEmptyState.visibility = android.view.View.GONE
        binding.recyclerDetectionLog.visibility = android.view.View.VISIBLE

        // Save to preferences
        saveDetectionEvent(event)
    }

    private fun saveDetectionEvent(event: DetectionEvent) {
        val prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
        val editor = prefs.edit()

        val index = prefs.getInt("detection_count", 0)
        editor.putLong("detection_${index}_time", event.timestamp)
        editor.putString("detection_${index}_type", event.type)
        editor.putFloat("detection_${index}_confidence", event.confidence)
        editor.putInt("detection_count", index + 1)
        editor.apply()
    }
}

/**
 * Detection Event Data Class
 */
data class DetectionEvent(
    val timestamp: Long,
    val type: String,
    val confidence: Float,
    val isThreat: Boolean
)
