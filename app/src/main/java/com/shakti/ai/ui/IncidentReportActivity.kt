package com.shakti.ai.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.shakti.ai.data.EvidenceDatabase
import com.shakti.ai.data.IncidentRecord
import com.shakti.ai.databinding.ActivityIncidentReportBinding
import com.shakti.ai.utils.Constants
import kotlinx.coroutines.launch
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
 * - Load from DATABASE instead of preferences
 */
class IncidentReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIncidentReportBinding
    private lateinit var database: EvidenceDatabase
    private var currentIncident: IncidentRecord? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncidentReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = EvidenceDatabase.getDatabase(this)

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
        // Get incident ID (passed via intent or use latest from database)
        val incidentId = intent.getStringExtra("incident_id")

        lifecycleScope.launch {
            try {
                // If no specific incident ID, get the most recent one
                val incident = if (incidentId != null) {
                    database.incidentDao().getIncidentById(incidentId)
                } else {
                    // Get the most recent incident
                    val allIncidents = database.incidentDao().getAllIncidents()
                    allIncidents.maxByOrNull { it.startTime }
                }

                if (incident == null) {
                    runOnUiThread {
                        binding.tvNoData.visibility = android.view.View.VISIBLE
                        binding.tvNoData.text = "No incident data available"
                    }
                    return@launch
                }

                val evidence = database.evidenceDao().getEvidenceForIncident(incident.id)

                runOnUiThread {
                    // Hide "no data" message
                    binding.tvNoData.visibility = android.view.View.GONE

                    currentIncident = incident

                    // Load timestamp
                    val dateFormat =
                        SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.getDefault())
                    binding.tvTimestamp.text =
                        "Time: ${dateFormat.format(Date(incident.startTime))}"

                    // Load trigger type
                    binding.tvTriggerType.text =
                        "Trigger: ${formatTriggerType(incident.triggerType)}"

                    // Load location
                    if (incident.latitude != 0.0 && incident.longitude != 0.0) {
                        binding.tvLocation.text =
                            "Location: ${
                                String.format(
                                    "%.6f",
                                    incident.latitude
                                )
                            }, ${String.format("%.6f", incident.longitude)}"
                        if (incident.address != null) {
                            binding.tvAddress.text = incident.address
                        } else {
                            binding.tvAddress.text = "Address not available"
                        }
                    } else {
                        binding.tvLocation.text = "Location: Checking..."
                        binding.tvAddress.text = "Address not available"
                    }

                    // Load video evidence count
                    val frontVideos = evidence.filter { it.type == "video_front" }
                    val backVideos = evidence.filter { it.type == "video_back" }
                    val audioFiles = evidence.filter { it.type == "audio" }

                    binding.tvFrontVideo.text = if (frontVideos.isNotEmpty()) {
                        "Front Camera: ✓ ${frontVideos.size} recorded"
                    } else {
                        "Front Camera: Checking..."
                    }

                    binding.tvBackVideo.text = if (backVideos.isNotEmpty()) {
                        "Back Camera: ✓ ${backVideos.size} recorded"
                    } else {
                        "Back Camera: Checking..."
                    }

                    binding.tvAudioRecording.text = if (audioFiles.isNotEmpty()) {
                        "Audio: ✓ ${audioFiles.size} recorded"
                    } else {
                        "Audio: Checking..."
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    binding.tvNoData.visibility = android.view.View.VISIBLE
                    binding.tvNoData.text = "Error loading incident data"

                    // Show default values
                    binding.tvTimestamp.text = "Time: Error loading"
                    binding.tvTriggerType.text = "Trigger: Unknown"
                    binding.tvLocation.text = "Location: Not available"
                    binding.tvAddress.text = "Address not available"
                    binding.tvFrontVideo.text = "Front Camera: Error"
                    binding.tvBackVideo.text = "Back Camera: Error"
                    binding.tvAudioRecording.text = "Audio: Error"
                }
            }
        }
    }

    private fun formatTriggerType(type: String): String {
        return when (type) {
            "voice_command" -> "Voice Command (HELP 3x)"
            "manual_sos" -> "Manual SOS (911=)"
            "ai_detection" -> "AI Detection"
            else -> "Unknown"
        }
    }

    private fun setupButtons() {
        binding.btnViewEvidence.setOnClickListener {
            // Use the current incident that was loaded
            if (currentIncident != null) {
                val intent = Intent(this, EvidenceViewerActivity::class.java)
                intent.putExtra("incident_id", currentIncident!!.id)
                startActivity(intent)
            } else {
                android.widget.Toast.makeText(
                    this,
                    "No evidence available",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.btnShareEvidence.setOnClickListener {
            shareEvidence()
        }

        binding.btnDeleteIncident.setOnClickListener {
            deleteIncident()
        }
    }

    private fun shareEvidence() {
        currentIncident?.let { incident ->
            val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.getDefault())
            val timeStr = dateFormat.format(Date(incident.startTime))

            val message = """
            SHAKTI INCIDENT REPORT
            
            Time: $timeStr
            Location: ${incident.latitude}, ${incident.longitude}
            Address: ${incident.address ?: "Unknown"}
            Trigger: ${formatTriggerType(incident.triggerType)}
            
            Evidence has been captured and is available.
            Incident ID: ${incident.id}
        """.trimIndent()

            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Incident Report - SHAKTI")
            shareIntent.putExtra(Intent.EXTRA_TEXT, message)

            startActivity(Intent.createChooser(shareIntent, "Share incident via"))
        }
    }

    private fun deleteIncident() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Incident")
            .setMessage("Are you sure you want to delete this incident and all evidence?")
            .setPositiveButton("Delete") { _, _ ->
                currentIncident?.let { incident ->
                    lifecycleScope.launch {
                        // Delete from database
                        database.evidenceDao().deleteEvidenceForIncident(incident.id)
                        database.incidentDao().deleteIncident(incident)

                        runOnUiThread {
                            android.widget.Toast.makeText(
                                this@IncidentReportActivity,
                                "Incident deleted",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
