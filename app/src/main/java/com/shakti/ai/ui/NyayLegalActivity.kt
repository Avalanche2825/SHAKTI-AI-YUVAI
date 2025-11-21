package com.shakti.ai.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import com.shakti.ai.R
import com.shakti.ai.databinding.ActivityNyayLegalBinding
import com.shakti.ai.services.NyayLegalService
import com.shakti.ai.utils.Constants
import java.text.SimpleDateFormat
import java.util.*

/**
 * NYAY Legal Activity - AI-powered legal assistance
 *
 * Features:
 * - Auto-generated FIR display
 * - IPC sections with descriptions
 * - Case strength assessment
 * - Lawyer recommendations
 * - Export FIR as text/PDF
 */
class NyayLegalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNyayLegalBinding
    private lateinit var legalService: NyayLegalService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNyayLegalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        legalService = NyayLegalService(this)

        setupToolbar()
        setupInputFields()
        setupButtons()
        setupAIChatButton()
    }

    /**
     * Setup toolbar
     */
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "NYAY Legal Assistant"
            setDisplayHomeAsUpEnabled(true)
        }

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    /**
     * Setup input fields
     */
    private fun setupInputFields() {
        // Pre-fill from preferences if available
        val prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)

        // Set default incident type
        val incidentTypes = resources.getStringArray(R.array.incident_types)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, incidentTypes)
        binding.spinnerIncidentType.setAdapter(adapter)

        // Set default selection
        if (incidentTypes.isNotEmpty()) {
            binding.spinnerIncidentType.setText(incidentTypes[0], false)
        }
    }

    /**
     * Setup buttons
     */
    private fun setupButtons() {
        // Generate FIR button
        binding.btnGenerateFir.setOnClickListener {
            generateFIR()
        }

        // View Lawyers button
        binding.btnViewLawyers.setOnClickListener {
            showLawyerRecommendations()
        }

        // Share FIR button
        binding.btnShareFir.setOnClickListener {
            shareFIR()
        }
    }

    /**
     * Generate FIR document
     */
    private fun generateFIR() {
        // Get input values
        val victimName = binding.etVictimName.text.toString()
        val incidentType = binding.spinnerIncidentType.text.toString()
        val incidentDescription = binding.etIncidentDescription.text.toString()
        val location = binding.etLocation.text.toString()

        // Validate
        if (victimName.isEmpty() || incidentType.isEmpty() || incidentDescription.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if evidence exists
        val prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
        val hasEvidence = prefs.contains("incident_video_front") ||
                prefs.contains("incident_video_back")

        // Generate FIR
        val fir = legalService.generateFIR(
            incidentType = incidentType,
            incidentDescription = incidentDescription,
            victimName = victimName,
            location = location,
            dateTime = System.currentTimeMillis(),
            hasEvidence = hasEvidence
        )

        // Display FIR
        displayFIR(fir)

        Toast.makeText(this, "FIR Generated Successfully", Toast.LENGTH_SHORT).show()
    }

    /**
     * Display generated FIR
     */
    private fun displayFIR(fir: NyayLegalService.FIRDocument) {
        // Show FIR details
        binding.layoutFirResult.visibility = android.view.View.VISIBLE

        // FIR Number
        binding.tvFirNumber.text = fir.firNumber

        // Case Strength
        binding.tvCaseStrength.text = "${fir.caseStrength}%"
        binding.progressCaseStrength.progress = fir.caseStrength

        // Set color based on strength
        val strengthColor = when {
            fir.caseStrength >= 80 -> R.color.success
            fir.caseStrength >= 60 -> R.color.info
            fir.caseStrength >= 40 -> R.color.warning
            else -> R.color.error
        }
        binding.progressCaseStrength.progressTintList =
            android.content.res.ColorStateList.valueOf(getColor(strengthColor))

        // IPC Sections
        val ipcText = buildString {
            fir.ipcSections.forEachIndexed { index, section ->
                if (index > 0) append("\n\n")
                append("Section ${section.number}: ${section.title}\n")
                append("${section.description}\n")
                append("Punishment: ${section.punishment}")
            }
        }
        binding.tvIpcSections.text = ipcText

        // Full FIR text
        val fullFirText = legalService.generateFIRText(fir)
        binding.tvFullFir.text = fullFirText

        // Save FIR to preferences
        val prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
        prefs.edit()
            .putString("latest_fir_number", fir.firNumber)
            .putString("latest_fir_text", fullFirText)
            .putLong("latest_fir_timestamp", fir.generatedAt)
            .apply()
    }

    /**
     * Show lawyer recommendations
     */
    private fun showLawyerRecommendations() {
        val incidentType = binding.spinnerIncidentType.text.toString()
        val location = binding.etLocation.text.toString()

        if (incidentType.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill incident type and location first", Toast.LENGTH_SHORT)
                .show()
            return
        }

        val lawyers = legalService.getLawyerRecommendations(incidentType, location)

        // Build lawyer list
        val lawyerText = buildString {
            append("Recommended Lawyers:\n\n")
            lawyers.forEachIndexed { index, lawyer ->
                if (index > 0) append("\n\n")
                append("${index + 1}. ${lawyer.name}\n")
                append("   Specialization: ${lawyer.specialization}\n")
                append("   Experience: ${lawyer.experience}\n")
                append("   Rating: ${lawyer.rating}⭐ (${lawyer.casesWon} cases won)\n")
                append("   Phone: ${lawyer.phone}\n")
                append("   Consultation: ${lawyer.consultationFee}")
            }
        }

        // Show in dialog
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Lawyer Recommendations")
            .setMessage(lawyerText)
            .setPositiveButton("Call First Lawyer") { _, _ ->
                if (lawyers.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.data = android.net.Uri.parse("tel:${lawyers[0].phone}")
                    startActivity(intent)
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }

    /**
     * Share FIR document
     */
    private fun shareFIR() {
        val prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
        val firText = prefs.getString("latest_fir_text", null)

        if (firText == null) {
            Toast.makeText(this, "Please generate FIR first", Toast.LENGTH_SHORT).show()
            return
        }

        // Share via intent
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "FIR Document - SHAKTI AI")
        shareIntent.putExtra(Intent.EXTRA_TEXT, firText)

        startActivity(Intent.createChooser(shareIntent, "Share FIR via"))
    }

    /**
     * Setup AI Chat Assistant button
     */
    private fun setupAIChatButton() {
        try {
            binding.fabAiChat.setOnClickListener {
                // Create and show chat fragment
                val fragment = com.shakti.ai.ui.components.AIChatFragment.newInstance(
                    com.shakti.ai.models.ChatContext.LEGAL
                )

                // Show in a dialog fragment style
                supportFragmentManager.beginTransaction()
                    .add(android.R.id.content, fragment, "chat_fragment")
                    .addToBackStack("chat")
                    .commit()
            }
        } catch (e: Exception) {
            // FAB not found or error, skip silently
            android.util.Log.e("NyayLegal", "Failed to setup chat FAB: ${e.message}")
        }
    }
}
