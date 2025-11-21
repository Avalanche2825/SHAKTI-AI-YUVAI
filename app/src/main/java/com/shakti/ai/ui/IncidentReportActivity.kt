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
                    if (allIncidents.isEmpty()) {
                        null
                    } else {
                        allIncidents.maxByOrNull { it.startTime }
                    }
                }

                // Check if activity is still alive before updating UI
                if (!isFinishing && !isDestroyed) {
                    if (incident == null) {
                        runOnUiThread {
                            binding.tvNoData.visibility = android.view.View.VISIBLE
                            binding.tvNoData.text =
                                "No incident data available. Trigger an emergency to record evidence."

                            // Hide other cards
                            binding.tvTimestamp.text = "Time: No data"
                            binding.tvTriggerType.text = "Trigger: No data"
                            binding.tvLocation.text = "Location: No data"
                            binding.tvAddress.text = "No data"
                            binding.tvFrontVideo.text = "Front Camera: No recordings"
                            binding.tvBackVideo.text = "Back Camera: No recordings"
                            binding.tvAudioRecording.text = "Audio: No recordings"

                            // Disable buttons
                            binding.btnViewEvidence.isEnabled = false
                            binding.btnShareEvidence.isEnabled = false
                            binding.btnDeleteIncident.isEnabled = false
                        }
                        return@launch
                    }

                    val evidence = database.evidenceDao().getEvidenceForIncident(incident.id)

                    // Check again before updating UI
                    if (!isFinishing && !isDestroyed) {
                        runOnUiThread {
                            try {
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
                                    if (incident.address != null && incident.address.isNotEmpty()) {
                                        binding.tvAddress.text = incident.address
                                    } else {
                                        binding.tvAddress.text = "Address not available"
                                    }
                                } else {
                                    binding.tvLocation.text = "Location: Not captured"
                                    binding.tvAddress.text = "GPS was not available"
                                }

                                // Load video evidence count
                                val frontVideos = evidence.filter { it.type == "video_front" }
                                val backVideos = evidence.filter { it.type == "video_back" }
                                val audioFiles = evidence.filter { it.type == "audio" }

                                binding.tvFrontVideo.text = if (frontVideos.isNotEmpty()) {
                                    "Front Camera: ✓ ${frontVideos.size} recorded"
                                } else {
                                    "Front Camera: Not recorded"
                                }

                                binding.tvBackVideo.text = if (backVideos.isNotEmpty()) {
                                    "Back Camera: ✓ ${backVideos.size} recorded"
                                } else {
                                    "Back Camera: Not recorded"
                                }

                                binding.tvAudioRecording.text = if (audioFiles.isNotEmpty()) {
                                    "Audio: ✓ ${audioFiles.size} recorded"
                                } else {
                                    "Audio: Not recorded"
                                }

                                // Enable buttons
                                binding.btnViewEvidence.isEnabled = evidence.isNotEmpty()
                                binding.btnShareEvidence.isEnabled = true
                                binding.btnDeleteIncident.isEnabled = true
                            } catch (e: Exception) {
                                e.printStackTrace()
                                android.widget.Toast.makeText(
                                    this@IncidentReportActivity,
                                    "Error displaying incident data",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                android.util.Log.e("IncidentReport", "Error loading incident: ${e.message}", e)

                if (!isFinishing && !isDestroyed) {
                    runOnUiThread {
                        try {
                            binding.tvNoData.visibility = android.view.View.VISIBLE
                            binding.tvNoData.text = "Error loading incident data: ${e.message}"

                            // Show error values
                            binding.tvTimestamp.text = "Time: Error loading"
                            binding.tvTriggerType.text = "Trigger: Error"
                            binding.tvLocation.text = "Location: Error"
                            binding.tvAddress.text = "Error"
                            binding.tvFrontVideo.text = "Front Camera: Error"
                            binding.tvBackVideo.text = "Back Camera: Error"
                            binding.tvAudioRecording.text = "Audio: Error"

                            // Disable buttons
                            binding.btnViewEvidence.isEnabled = false
                            binding.btnShareEvidence.isEnabled = false
                            binding.btnDeleteIncident.isEnabled = false

                            android.widget.Toast.makeText(
                                this@IncidentReportActivity,
                                "Failed to load incident: ${e.message}",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        } catch (uiException: Exception) {
                            uiException.printStackTrace()
                        }
                    }
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
            try {
                // Use the current incident that was loaded
                if (currentIncident != null) {
                    val intent = Intent(this, EvidenceViewerActivity::class.java)
                    intent.putExtra("incident_id", currentIncident!!.id)
                    startActivity(intent)
                } else {
                    android.widget.Toast.makeText(
                        this,
                        "No incident data available",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                android.widget.Toast.makeText(
                    this,
                    "Error opening evidence viewer",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.btnShareEvidence.setOnClickListener {
            try {
                shareEvidence()
            } catch (e: Exception) {
                e.printStackTrace()
                android.widget.Toast.makeText(
                    this,
                    "Error sharing evidence",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.btnDeleteIncident.setOnClickListener {
            try {
                deleteIncident()
            } catch (e: Exception) {
                e.printStackTrace()
                android.widget.Toast.makeText(
                    this,
                    "Error deleting incident",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun shareEvidence() {
        if (currentIncident == null) {
            android.widget.Toast.makeText(
                this,
                "No incident to share",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            return
        }

        try {
            val incident = currentIncident!!
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
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(
                this,
                "Failed to share: ${e.message}",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun deleteIncident() {
        if (currentIncident == null) {
            android.widget.Toast.makeText(
                this,
                "No incident to delete",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            return
        }

        try {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Incident")
                .setMessage("Are you sure you want to delete this incident and all evidence?")
                .setPositiveButton("Delete") { _, _ ->
                    val incident = currentIncident!!
                    lifecycleScope.launch {
                        try {
                            // Delete from database
                            database.evidenceDao().deleteEvidenceForIncident(incident.id)
                            database.incidentDao().deleteIncident(incident)

                            if (!isFinishing && !isDestroyed) {
                                runOnUiThread {
                                    android.widget.Toast.makeText(
                                        this@IncidentReportActivity,
                                        "Incident deleted",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                    finish()
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            if (!isFinishing && !isDestroyed) {
                                runOnUiThread {
                                    android.widget.Toast.makeText(
                                        this@IncidentReportActivity,
                                        "Failed to delete: ${e.message}",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(
                this,
                "Error: ${e.message}",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
}
