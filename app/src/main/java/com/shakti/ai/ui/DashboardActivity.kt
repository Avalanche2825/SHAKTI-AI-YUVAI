package com.shakti.ai.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.shakti.ai.R
import com.shakti.ai.databinding.ActivityDashboardBinding
import com.shakti.ai.utils.Constants
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
 */
class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
    }

    /**
     * Load incident history from preferences
     */
    private fun loadIncidentHistory() {
        val prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
        val incidentCount = prefs.getInt("total_incidents", 0)

        binding.tvIncidentCount.text = incidentCount.toString()

        if (incidentCount > 0) {
            // Get most recent incident
            val lastIncidentTime = prefs.getLong("last_incident_time", 0)
            if (lastIncidentTime > 0) {
                val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                binding.tvLastIncident.text = "Last: ${dateFormat.format(Date(lastIncidentTime))}"
            } else {
                binding.tvLastIncident.text = "No recent incidents"
            }
        } else {
            binding.tvLastIncident.text = "No incidents recorded"
        }
    }

    /**
     * Load safety statistics
     */
    private fun loadStatistics() {
        val prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)

        // Monitoring time (hours)
        val monitoringTimeMs = prefs.getLong("total_monitoring_time_ms", 0)
        val monitoringHours = (monitoringTimeMs / (1000 * 60 * 60)).toInt()
        binding.tvMonitoringHours.text = "$monitoringHours hrs"

        // Evidence captured
        val evidenceCount = prefs.getInt("total_evidence_count", 0)
        binding.tvEvidenceCount.text = "$evidenceCount files"

        // Alerts sent
        val alertsSent = prefs.getInt("total_alerts_sent", 0)
        binding.tvAlertsSent.text = "$alertsSent alerts"

        // Community helps
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
