package com.shakti.ai.ui

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.shakti.ai.databinding.ActivityAimonitoringBinding
import com.shakti.ai.ml.HelpWordDetector
import com.shakti.ai.services.AudioDetectionService
import com.shakti.ai.utils.Constants
import java.text.SimpleDateFormat
import java.util.*

/**
 * AI Monitoring Dashboard - Shows AI working in real-time
 * Now with voice command "HELP" 3x detection!
 */
class AIMonitoringActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAimonitoringBinding
    private val detectionLog = mutableListOf<DetectionEvent>()
    private lateinit var logAdapter: DetectionLogAdapter
    private var helpWordDetector: HelpWordDetector? = null
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())

    // Broadcast receiver for audio levels
    private val audioLevelReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "com.shakti.ai.AUDIO_LEVEL_UPDATE" -> {
                    val energy = intent.getFloatExtra("audio_energy", 0f)
                    val frequency = intent.getDoubleExtra("audio_frequency", 0.0)
                    val isSpeech = intent.getBooleanExtra("is_speech", false)

                    // Update waveform visualizer
                    binding.audioVisualizer.updateWaveform(energy * 2f) // Scale for visibility

                    // Update audio level text
                    val percentage = (energy * 100).toInt().coerceIn(0, 100)
                    binding.tvAudioLevel.text = "Level: $percentage% ${if (isSpeech) "🎤" else ""}"

                    // Update status if speech detected
                    if (isSpeech) {
                        binding.audioVisualizer.setWaveformColor(
                            android.graphics.Color.parseColor("#10B981") // Green when speech
                        )
                        binding.tvAudioLevel.setTextColor(
                            android.graphics.Color.parseColor("#10B981")
                        )
                    } else {
                        binding.audioVisualizer.setWaveformColor(
                            android.graphics.Color.parseColor("#32B8C6") // Teal when idle
                        )
                        binding.tvAudioLevel.setTextColor(
                            ContextCompat.getColor(
                                this@AIMonitoringActivity,
                                com.shakti.ai.R.color.text_light
                            )
                        )
                    }
                }
            }
        }
    }

    companion object {
        private const val REQUEST_RECORD_AUDIO = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAimonitoringBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupVoiceCommands()
        setupDetectionLog()
        setupWaveform()
        loadStatistics()
        startLiveUpdates()
        registerAudioLevelReceiver()
    }

    /**
     * Register broadcast receiver for audio level updates
     */
    private fun registerAudioLevelReceiver() {
        val filter = IntentFilter().apply {
            addAction("com.shakti.ai.AUDIO_LEVEL_UPDATE")
            addAction("com.shakti.ai.HELP_DETECTION_UPDATE")
        }

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(audioLevelReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                registerReceiver(audioLevelReceiver, filter)
            }
        } catch (e: Exception) {
            android.util.Log.e("AIMonitoring", "Failed to register receiver", e)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "AI Monitoring"
            setDisplayHomeAsUpEnabled(true)
        }

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupVoiceCommands() {
        // Voice command toggle
        binding.switchVoiceCommand.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (checkAudioPermission()) {
                    startVoiceCommandDetection()
                } else {
                    binding.switchVoiceCommand.isChecked = false
                    requestAudioPermission()
                }
            } else {
                stopVoiceCommandDetection()
            }
        }

        // Test voice command button
        binding.btnTestVoice.setOnClickListener {
            if (binding.switchVoiceCommand.isChecked) {
                Toast.makeText(
                    this,
                    "Say 'HELP' 3 times within 8 seconds",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    this,
                    "Please enable voice commands first",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun checkAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestAudioPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            REQUEST_RECORD_AUDIO
        )
    }

    private fun startVoiceCommandDetection() {
        try {
            // Stop idle animation when starting real detection
            handler.removeCallbacksAndMessages(null)
            binding.audioVisualizer.reset()

            helpWordDetector = HelpWordDetector(this)

            // Load sensitivity from preferences
            val prefs = getSharedPreferences(com.shakti.ai.utils.Constants.PREFS_NAME, MODE_PRIVATE)
            val sensitivity = prefs.getFloat("help_detection_sensitivity", 0.40f)
            helpWordDetector?.setSensitivity(sensitivity)

            helpWordDetector?.startListening {
                runOnUiThread {
                    onVoiceCommandDetected("HELP")
                }
            }

            updateVoiceCommandStatus("🎤 Listening for HELP word... (sensitivity: ${(sensitivity * 100).toInt()}%)")
            startVoiceCommandUpdates()

            Toast.makeText(
                this,
                "HELP word detection enabled! Say 'HELP' 3 times",
                Toast.LENGTH_LONG
            ).show()

        } catch (e: Exception) {
            e.printStackTrace()
            binding.switchVoiceCommand.isChecked = false
            updateVoiceCommandStatus("Failed to start: ${e.message}")
            Toast.makeText(
                this,
                "Error starting HELP detection",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun stopVoiceCommandDetection() {
        helpWordDetector?.stopListening()
        helpWordDetector = null
        handler.removeCallbacks(voiceCommandUpdateRunnable)
        updateVoiceCommandStatus("HELP word detection disabled")

        // Resume idle animation
        binding.audioVisualizer.reset()
        binding.audioVisualizer.animateIdle()

        Toast.makeText(this, "HELP detection stopped", Toast.LENGTH_SHORT).show()
    }

    private fun startVoiceCommandUpdates() {
        handler.post(voiceCommandUpdateRunnable)
    }

    private val voiceCommandUpdateRunnable = object : Runnable {
        override fun run() {
            helpWordDetector?.let { detector ->
                val count = detector.getCurrentDetectionCount()
                val timeLeft = detector.getTimeUntilReset() / 1000

                if (count > 0) {
                    updateVoiceCommandStatus(
                        "🗣️ Detected: $count/3 HELP words (${timeLeft}s remaining)"
                    )
                } else {
                    updateVoiceCommandStatus(
                        "🎤 Listening for HELP word... (say it 3 times)"
                    )
                }
            }

            if (binding.switchVoiceCommand.isChecked) {
                handler.postDelayed(this, 500)
            }
        }
    }

    private fun updateVoiceCommandStatus(status: String) {
        binding.tvVoiceCommandStatus.text = status
    }

    private fun onVoiceCommandDetected(command: String) {
        // Show alert dialog
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("⚠️ Voice Command Detected!")
            .setMessage(
                "You said 'HELP' 3 times.\n\n" +
                        "This will immediately:\n" +
                        "• Start recording evidence\n" +
                        "• Alert emergency contacts\n" +
                        "• Share your location\n" +
                        "• Notify nearby users\n\n" +
                        "Trigger emergency?"
            )
            .setPositiveButton("YES - EMERGENCY!") { _, _ ->
                triggerEmergencySOS()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(
                    this,
                    "Emergency canceled. Voice commands still active.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setCancelable(false)
            .show()
    }

    private fun triggerEmergencySOS() {
        // Trigger emergency through service
        val intent = Intent(this, AudioDetectionService::class.java)
        intent.action = AudioDetectionService.ACTION_MANUAL_SOS
        ContextCompat.startForegroundService(this, intent)

        // Save detection event
        addDetectionEvent("Voice Command (HELP 3x)", 1.0f)

        Toast.makeText(this, "🚨 EMERGENCY SOS ACTIVATED!", Toast.LENGTH_LONG).show()

        // Optionally close activity
        finish()
    }

    private fun setupDetectionLog() {
        logAdapter = DetectionLogAdapter(detectionLog)
        binding.recyclerDetectionLog.apply {
            layoutManager = LinearLayoutManager(this@AIMonitoringActivity)
            adapter = logAdapter
        }

        loadDetectionHistory()
    }

    private fun setupWaveform() {
        // Start idle animation initially
        binding.audioVisualizer.animateIdle()
    }

    private fun loadDetectionHistory() {
        val prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)

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

        if (detectionLog.isEmpty()) {
            binding.tvEmptyState.visibility = android.view.View.VISIBLE
            binding.recyclerDetectionLog.visibility = android.view.View.GONE
        } else {
            binding.tvEmptyState.visibility = android.view.View.GONE
            binding.recyclerDetectionLog.visibility = android.view.View.VISIBLE
        }
    }

    private fun loadStatistics() {
        val prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)

        val totalSamples = prefs.getInt("total_samples_analyzed", 0)
        binding.tvTotalSamples.text = formatNumber(totalSamples)

        val threatsDetected = prefs.getInt("total_threats_detected", 0)
        binding.tvThreatsDetected.text = threatsDetected.toString()

        val accuracy = prefs.getFloat("detection_accuracy", 0.87f)
        binding.tvAccuracy.text = "${(accuracy * 100).toInt()}%"

        val avgConfidence = prefs.getFloat("avg_confidence", 0.65f)
        binding.tvAvgConfidence.text = "${(avgConfidence * 100).toInt()}%"

        binding.tvModelVersion.text = "YAMNet v1.0"

        val lastUpdate = prefs.getLong("last_model_update", System.currentTimeMillis())
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        binding.tvLastUpdate.text = dateFormat.format(Date(lastUpdate))
    }

    private fun startLiveUpdates() {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        val updateInterval = 500L

        val updateRunnable = object : Runnable {
            override fun run() {
                val prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
                val currentConfidence = prefs.getFloat("current_confidence", 0.2f)

                binding.confidenceMeter.setConfidence(currentConfidence)
                binding.tvLastCheck.text =
                    "Last check: ${System.currentTimeMillis() / 1000 % 60}s ago"

                handler.postDelayed(this, updateInterval)
            }
        }

        handler.post(updateRunnable)
    }

    private fun formatNumber(number: Int): String {
        return when {
            number >= 1_000_000 -> "${number / 1_000_000}M"
            number >= 1_000 -> "${number / 1_000}K"
            else -> number.toString()
        }
    }

    private fun addDetectionEvent(type: String, confidence: Float) {
        val event = DetectionEvent(
            timestamp = System.currentTimeMillis(),
            type = type,
            confidence = confidence,
            isThreat = confidence > 0.75f
        )

        detectionLog.add(0, event)

        if (detectionLog.size > 50) {
            detectionLog.removeAt(detectionLog.lastIndex)
        }

        logAdapter.notifyItemInserted(0)
        binding.recyclerDetectionLog.scrollToPosition(0)

        binding.tvEmptyState.visibility = android.view.View.GONE
        binding.recyclerDetectionLog.visibility = android.view.View.VISIBLE

        // Save to preferences
        val prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
        val editor = prefs.edit()
        val index = prefs.getInt("detection_count", 0)
        editor.putLong("detection_${index}_time", event.timestamp)
        editor.putString("detection_${index}_type", event.type)
        editor.putFloat("detection_${index}_confidence", event.confidence)
        editor.putInt("detection_count", index + 1)
        editor.apply()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_RECORD_AUDIO) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                binding.switchVoiceCommand.isChecked = true
                startVoiceCommandDetection()
            } else {
                Toast.makeText(
                    this,
                    "Microphone permission is required for voice commands",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVoiceCommandDetection()
        binding.confidenceMeter.stopAnimations()
        handler.removeCallbacksAndMessages(null)

        // Unregister broadcast receiver
        try {
            unregisterReceiver(audioLevelReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

data class DetectionEvent(
    val timestamp: Long,
    val type: String,
    val confidence: Float,
    val isThreat: Boolean
)
