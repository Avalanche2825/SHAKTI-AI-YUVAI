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
import java.io.File
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
                    android.util.Log.d(
                        "IncidentReport",
                        "📊 Total incidents in database: ${allIncidents.size}"
                    )
                    allIncidents.maxByOrNull { it.startTime }
                }

                if (incident == null) {
                    runOnUiThread {
                        binding.tvNoData.visibility = android.view.View.VISIBLE
                        binding.tvNoData.text = "No incident data available"
                        android.util.Log.w("IncidentReport", "❌ No incident found in database")
                    }
                    return@launch
                }

                android.util.Log.d("IncidentReport", "✅ Loaded incident: ${incident.id}")
                android.util.Log.d(
                    "IncidentReport",
                    "   Start time: ${java.util.Date(incident.startTime)}"
                )
                android.util.Log.d("IncidentReport", "   Trigger: ${incident.triggerType}")

                val evidence = database.evidenceDao().getEvidenceForIncident(incident.id)
                android.util.Log.d("IncidentReport", "📁 Evidence count: ${evidence.size}")

                // Log each evidence item
                evidence.forEach { ev ->
                    android.util.Log.d(
                        "IncidentReport",
                        "   - ${ev.type}: ${ev.filePath} (${ev.fileSize} bytes)"
                    )
                }

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

                    // Check if incident is recent (within last 5 minutes) - might still be recording
                    val incidentAge = System.currentTimeMillis() - incident.startTime
                    val isRecent = incidentAge < (5 * 60 * 1000) // 5 minutes

                    android.util.Log.d(
                        "IncidentReport",
                        "Incident age: ${incidentAge / 1000}s, isRecent: $isRecent"
                    )

                    binding.tvFrontVideo.text = when {
                        frontVideos.isNotEmpty() -> {
                            val video = frontVideos.first()
                            val exists = File(video.filePath).exists()
                            android.util.Log.d(
                                "IncidentReport",
                                "Front video exists: $exists (${video.filePath})"
                            )
                            if (exists) {
                                "Front Camera: ✓ Recorded (${video.fileSize / 1024}KB)"
                            } else {
                                "Front Camera: ✓ ${frontVideos.size} recorded (file moved)"
                            }
                        }

                        isRecent && incident.endTime == 0L -> "Front Camera: 🎥 Recording..."
                        isRecent -> "Front Camera: ⏳ Processing..."
                        else -> "Front Camera: Not recorded"
                    }

                    binding.tvBackVideo.text = when {
                        backVideos.isNotEmpty() -> {
                            val video = backVideos.first()
                            val exists = File(video.filePath).exists()
                            android.util.Log.d(
                                "IncidentReport",
                                "Back video exists: $exists (${video.filePath})"
                            )
                            if (exists) {
                                "Back Camera: ✓ Recorded (${video.fileSize / 1024}KB)"
                            } else {
                                "Back Camera: ✓ ${backVideos.size} recorded (file moved)"
                            }
                        }

                        isRecent && incident.endTime == 0L -> "Back Camera: 🎥 Recording..."
                        isRecent -> "Back Camera: ⏳ Processing..."
                        else -> "Back Camera: Not recorded"
                    }

                    binding.tvAudioRecording.text = when {
                        audioFiles.isNotEmpty() -> {
                            val audio = audioFiles.first()
                            val exists = File(audio.filePath).exists()
                            android.util.Log.d(
                                "IncidentReport",
                                "Audio exists: $exists (${audio.filePath})"
                            )
                            if (exists) {
                                "Audio: ✓ Recorded (${audio.fileSize / 1024}KB)"
                            } else {
                                "Audio: ✓ ${audioFiles.size} recorded (file moved)"
                            }
                        }

                        isRecent && incident.endTime == 0L -> "Audio: 🎙️ Recording..."
                        isRecent -> "Audio: ⏳ Processing..."
                        else -> "Audio: Not recorded"
                    }

                    // If no evidence and not recent, show warning
                    if (evidence.isEmpty() && !isRecent) {
                        binding.tvNoData.visibility = android.view.View.VISIBLE
                        binding.tvNoData.text = "⚠️ No evidence recorded for this incident"
                        android.util.Log.w(
                            "IncidentReport",
                            "⚠️ No evidence found for incident ${incident.id}"
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                android.util.Log.e("IncidentReport", "❌ Error loading incident data", e)
                runOnUiThread {
                    binding.tvNoData.visibility = android.view.View.VISIBLE
                    binding.tvNoData.text = "Error loading incident data: ${e.message}"

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

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to activity (in case recording finished)
        android.util.Log.d("IncidentReport", "🔄 Activity resumed, refreshing data...")
        loadIncidentData()
    }
}
