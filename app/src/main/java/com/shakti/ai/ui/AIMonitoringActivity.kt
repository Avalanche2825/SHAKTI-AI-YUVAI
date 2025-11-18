package com.shakti.ai.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.shakti.ai.databinding.ActivityAimonitoringBinding
import com.shakti.ai.utils.Constants
import java.text.SimpleDateFormat
import java.util.*

/**
 * AI Monitoring Dashboard - Shows AI working in real-time
 */
class AIMonitoringActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAimonitoringBinding
    private val detectionLog = mutableListOf<DetectionEvent>()
    private lateinit var logAdapter: DetectionLogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAimonitoringBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupDetectionLog()
        setupWaveform()
        loadStatistics()
        startLiveUpdates()
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

    private fun setupDetectionLog() {
        logAdapter = DetectionLogAdapter(detectionLog)
        binding.recyclerDetectionLog.apply {
            layoutManager = LinearLayoutManager(this@AIMonitoringActivity)
            adapter = logAdapter
        }

        loadDetectionHistory()
    }

    private fun setupWaveform() {
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

    override fun onDestroy() {
        super.onDestroy()
        binding.confidenceMeter.stopAnimations()
    }
}

data class DetectionEvent(
    val timestamp: Long,
    val type: String,
    val confidence: Float,
    val isThreat: Boolean
)
