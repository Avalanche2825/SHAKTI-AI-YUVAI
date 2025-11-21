package com.shakti.ai.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.shakti.ai.R
import com.shakti.ai.data.EvidenceDatabase
import com.shakti.ai.databinding.ActivityDashboardBinding
import com.shakti.ai.utils.Constants
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Dashboard Activity - Main control center (accessed via secret code 999=)
 *
 * Features:
 * - Shows incident history
 * - Quick access to NYAY Legal, Escape Planner
 * - Safety statistics
 * - Emergency contacts management
 * - AI Monitoring Dashboard (NEW)
 * - AI Chatbot Assistant (NEW)
 */
class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var database: EvidenceDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = EvidenceDatabase.getDatabase(this)

        setupToolbar()
        setupQuickActions()
        loadIncidentHistory()
        loadStatistics()
    }

    /**
     * Setup toolbar
     */
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "SHAKTI Dashboard"
            setDisplayHomeAsUpEnabled(true)
        }

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    /**
     * Setup quick action buttons
     */
    private fun setupQuickActions() {
        // AI Monitoring Dashboard (NEW)
        binding.cardAiMonitoring.setOnClickListener {
            startActivity(Intent(this, AIMonitoringActivity::class.java))
        }

        // NYAY Legal Assistant
        binding.cardNyayLegal.setOnClickListener {
            startActivity(Intent(this, NyayLegalActivity::class.java))
        }

        // Escape Planner
        binding.cardEscapePlanner.setOnClickListener {
            startActivity(Intent(this, EscapePlannerActivity::class.java))
        }

        // Incident Reports
        binding.cardIncidents.setOnClickListener {
            startActivity(Intent(this, IncidentReportActivity::class.java))
        }

        // Settings
        binding.cardSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Emergency SOS
        binding.btnEmergencySOS.setOnClickListener {
            triggerEmergencySOS()
        }

        // AI Chatbot (NEW) - Check if card exists, if not will be added via layout
        try {
            val cardChatbot =
                binding.root.findViewById<com.google.android.material.card.MaterialCardView>(
                    resources.getIdentifier("cardAiChatbot", "id", packageName)
                )
            cardChatbot?.setOnClickListener {
                startActivity(Intent(this, AIChatbotActivity::class.java))
            }
        } catch (e: Exception) {
            // Card not in layout yet, will be added
        }
    }

    /**
     * Load incident history from DATABASE
     */
    private fun loadIncidentHistory() {
        lifecycleScope.launch {
            try {
                // Get all incidents from database
                val incidents = database.incidentDao().getAllIncidents()
                val incidentCount = incidents.size

                runOnUiThread {
                    binding.tvIncidentCount.text = incidentCount.toString()

                    if (incidentCount > 0) {
                        // Get most recent incident
                        val lastIncident = incidents.maxByOrNull { it.startTime }
                        if (lastIncident != null) {
                            val dateFormat =
                                SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                            binding.tvLastIncident.text =
                                "Last: ${dateFormat.format(Date(lastIncident.startTime))}"
                        } else {
                            binding.tvLastIncident.text = "No recent incidents"
                        }
                    } else {
                        binding.tvLastIncident.text = "No incidents recorded"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    binding.tvIncidentCount.text = "0"
                    binding.tvLastIncident.text = "Error loading data"
                }
            }
        }
    }

    /**
     * Load safety statistics from DATABASE
     */
    private fun loadStatistics() {
        val prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)

        // Monitoring time (hours) - from preferences
        val monitoringTimeMs = prefs.getLong("total_monitoring_time_ms", 0)
        val monitoringHours = (monitoringTimeMs / (1000 * 60 * 60)).toInt()
        binding.tvMonitoringHours.text = "$monitoringHours hrs"

        // Load evidence count from DATABASE
        lifecycleScope.launch {
            try {
                val evidenceCount = database.evidenceDao().getAllEvidence().size
                runOnUiThread {
                    binding.tvEvidenceCount.text = "$evidenceCount files"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    binding.tvEvidenceCount.text = "0 files"
                }
            }
        }

        // Alerts sent - from preferences
        val alertsSent = prefs.getInt("total_alerts_sent", 0)
        binding.tvAlertsSent.text = "$alertsSent alerts"

        // Community helps - from preferences (if implemented)
        val communityHelps = prefs.getInt("community_helps", 0)
        binding.tvCommunityHelps.text = "$communityHelps women"
    }

    /**
     * Trigger emergency SOS
     */
    private fun triggerEmergencySOS() {
        // Confirm dialog
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Emergency SOS")
            .setMessage(
                "This will immediately:\n\n" +
                        "• Start recording evidence\n" +
                        "• Alert emergency contacts\n" +
                        "• Share your location\n" +
                        "• Notify nearby SHAKTI users\n\n" +
                        "Continue?"
            )
            .setPositiveButton("YES - EMERGENCY") { _, _ ->
                activateSOS()
            }
            .setNegativeButton("Cancel", null)
            .setCancelable(true)
            .show()
    }

    /**
     * Activate SOS mode
     */
    private fun activateSOS() {
        // Start all emergency services
        val audioIntent = Intent(this, com.shakti.ai.services.AudioDetectionService::class.java)
        audioIntent.action = "MANUAL_SOS"
        startService(audioIntent)

        Toast.makeText(this, "🚨 SOS ACTIVATED", Toast.LENGTH_LONG).show()

        // Return to calculator (to hide activity)
        finish()
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to dashboard
        loadIncidentHistory()
        loadStatistics()
    }
}
