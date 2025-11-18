package com.shakti.ai.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.shakti.ai.databinding.ActivityIncidentReportBinding
import com.shakti.ai.utils.Constants
import java.text.SimpleDateFormat
import java.util.*

/**
 * Incident Report Activity - View captured evidence
 *
 * Features:
 * - Display captured videos (front + back camera)
 * - Show location with map
 * - Timestamp and duration
 * - Share evidence
 */
class IncidentReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIncidentReportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncidentReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        loadIncidentData()
        setupButtons()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Incident Report"
            setDisplayHomeAsUpEnabled(true)
        }

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadIncidentData() {
        val prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)

        // Get incident ID (passed via intent or use latest)
        val incidentId = intent.getStringExtra("incident_id")
            ?: prefs.getString("current_incident_id", null)

        if (incidentId == null) {
            binding.tvNoData.visibility = android.view.View.VISIBLE
            return
        }

        // Load timestamp
        val timestamp = prefs.getLong("incident_${incidentId}_timestamp", 0)
        if (timestamp > 0) {
            val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.getDefault())
            binding.tvTimestamp.text = "Time: ${dateFormat.format(Date(timestamp))}"
        }

        // Load location
        val latitude = prefs.getString("incident_${incidentId}_latitude", null)
        val longitude = prefs.getString("incident_${incidentId}_longitude", null)
        val address = prefs.getString("incident_${incidentId}_address", null)

        if (latitude != null && longitude != null) {
            binding.tvLocation.text = "Location: $latitude, $longitude"
            if (address != null) {
                binding.tvAddress.text = address
            }
        }

        // Load video paths
        val frontVideo = prefs.getString("incident_${incidentId}_video_front", null)
        val backVideo = prefs.getString("incident_${incidentId}_video_back", null)

        binding.tvFrontVideo.text = if (frontVideo != null) {
            "Front Camera: ✓ Recorded"
        } else {
            "Front Camera: Not available"
        }

        binding.tvBackVideo.text = if (backVideo != null) {
            "Back Camera: ✓ Recorded"
        } else {
            "Back Camera: Not available"
        }
    }

    private fun setupButtons() {
        binding.btnViewEvidence.setOnClickListener {
            // TODO: Open video player to view evidence
            android.widget.Toast.makeText(
                this,
                "Video player coming soon",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }

        binding.btnShareEvidence.setOnClickListener {
            shareEvidence()
        }

        binding.btnDeleteIncident.setOnClickListener {
            deleteIncident()
        }
    }

    private fun shareEvidence() {
        // Share incident details
        val prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
        val incidentId = intent.getStringExtra("incident_id")
            ?: prefs.getString("current_incident_id", null)

        if (incidentId == null) return

        val timestamp = prefs.getLong("incident_${incidentId}_timestamp", 0)
        val latitude = prefs.getString("incident_${incidentId}_latitude", "Unknown")
        val longitude = prefs.getString("incident_${incidentId}_longitude", "Unknown")
        val address = prefs.getString("incident_${incidentId}_address", "Unknown")

        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.getDefault())
        val timeStr = if (timestamp > 0) dateFormat.format(Date(timestamp)) else "Unknown"

        val message = """
            SHAKTI INCIDENT REPORT
            
            Time: $timeStr
            Location: $latitude, $longitude
            Address: $address
            
            Evidence has been captured and is available.
            Incident ID: $incidentId
        """.trimIndent()

        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Incident Report - SHAKTI")
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, message)

        startActivity(android.content.Intent.createChooser(shareIntent, "Share incident via"))
    }

    private fun deleteIncident() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Incident")
            .setMessage("Are you sure you want to delete this incident and all evidence?")
            .setPositiveButton("Delete") { _, _ ->
                val prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
                val incidentId = intent.getStringExtra("incident_id")
                    ?: prefs.getString("current_incident_id", null)

                if (incidentId != null) {
                    // Delete all incident data
                    val editor = prefs.edit()
                    editor.remove("incident_${incidentId}_timestamp")
                    editor.remove("incident_${incidentId}_latitude")
                    editor.remove("incident_${incidentId}_longitude")
                    editor.remove("incident_${incidentId}_address")
                    editor.remove("incident_${incidentId}_video_front")
                    editor.remove("incident_${incidentId}_video_back")
                    editor.apply()

                    android.widget.Toast.makeText(
                        this,
                        "Incident deleted",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
