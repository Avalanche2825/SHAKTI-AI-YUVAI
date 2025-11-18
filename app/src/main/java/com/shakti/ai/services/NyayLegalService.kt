package com.shakti.ai.services

import android.content.Context
import com.shakti.ai.utils.Constants
import java.text.SimpleDateFormat
import java.util.*

/**
 * NYAY Legal Service - Automated legal assistance system
 *
 * Features:
 * - Auto-generates FIR (First Information Report)
 * - Maps incidents to IPC sections
 * - Calculates case strength
 * - Generates legal documentation
 * - Provides lawyer recommendations
 */
class NyayLegalService(private val context: Context) {

    /**
     * Generate FIR (First Information Report) from incident data
     */
    fun generateFIR(
        incidentType: String,
        incidentDescription: String,
        victimName: String,
        location: String,
        dateTime: Long,
        hasEvidence: Boolean
    ): FIRDocument {

        val ipcSections = getRelevantIPCSections(incidentType)
        val caseStrength = calculateCaseStrength(incidentType, hasEvidence)

        return FIRDocument(
            firNumber = generateFIRNumber(),
            victimName = victimName,
            incidentType = incidentType,
            incidentDescription = incidentDescription,
            location = location,
            dateTime = dateTime,
            ipcSections = ipcSections,
            caseStrength = caseStrength,
            generatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Get relevant IPC sections for incident type
     */
    private fun getRelevantIPCSections(incidentType: String): List<IPCSection> {
        val sections = when (incidentType.lowercase()) {
            "domestic_violence", "domestic violence" -> {
                Constants.IPC_SECTIONS["domestic_violence"] ?: emptyList()
            }

            "street_harassment", "street harassment", "groping" -> {
                Constants.IPC_SECTIONS["street_harassment"] ?: emptyList()
            }

            "workplace_harassment", "workplace harassment" -> {
                Constants.IPC_SECTIONS["workplace_harassment"] ?: emptyList()
            }

            "sextortion", "blackmail" -> {
                Constants.IPC_SECTIONS["sextortion"] ?: emptyList()
            }

            "stalking", "cyber stalking" -> {
                Constants.IPC_SECTIONS["stalking"] ?: emptyList()
            }

            "strangulation", "attempted murder" -> {
                Constants.IPC_SECTIONS["strangulation"] ?: emptyList()
            }

            "dowry_violence", "dowry violence", "dowry harassment" -> {
                Constants.IPC_SECTIONS["dowry_violence"] ?: emptyList()
            }

            "sexual_assault", "sexual assault", "rape" -> {
                Constants.IPC_SECTIONS["sexual_assault"] ?: emptyList()
            }

            else -> listOf("354", "509") // Default: harassment sections
        }

        return sections.map { sectionNumber ->
            IPCSection(
                number = sectionNumber,
                title = getIPCSectionTitle(sectionNumber),
                description = getIPCSectionDescription(sectionNumber),
                punishment = getIPCSectionPunishment(sectionNumber)
            )
        }
    }

    /**
     * Get IPC section title
     */
    private fun getIPCSectionTitle(section: String): String {
        return when (section) {
            "498A" -> "Husband or relative of husband subjecting woman to cruelty"
            "406" -> "Punishment for criminal breach of trust"
            "307" -> "Attempt to murder"
            "323" -> "Punishment for voluntarily causing hurt"
            "354" -> "Assault or criminal force to woman with intent to outrage her modesty"
            "354A" -> "Sexual harassment"
            "509" -> "Word, gesture or act intended to insult the modesty of a woman"
            "503" -> "Criminal intimidation"
            "504" -> "Intentional insult with intent to provoke breach of peace"
            "67IT" -> "Publishing obscene information in electronic form (IT Act)"
            "507" -> "Criminal intimidation by anonymous communication"
            "336" -> "Act endangering life or personal safety of others"
            "304B" -> "Dowry death"
            "375" -> "Rape"
            "376" -> "Punishment for rape"
            else -> "Section $section"
        }
    }

    /**
     * Get IPC section description
     */
    private fun getIPCSectionDescription(section: String): String {
        return when (section) {
            "498A" -> "Covers cruelty by husband or his relatives, including harassment for dowry, physical or mental harm"
            "354" -> "Covers assault or use of criminal force intending to outrage modesty"
            "354A" -> "Covers physical contact, demand for sexual favors, sexually colored remarks, showing pornography"
            "509" -> "Covers words, gestures or acts intended to insult the modesty of a woman"
            "307" -> "Covers attempt to commit murder with act that would cause death if successful"
            "503" -> "Covers threatening another with injury to person, reputation or property"
            "67IT" -> "Covers publishing or transmitting obscene material in electronic form"
            else -> "Legal provision under Indian Penal Code"
        }
    }

    /**
     * Get IPC section punishment
     */
    private fun getIPCSectionPunishment(section: String): String {
        return when (section) {
            "498A" -> "Imprisonment up to 3 years and fine"
            "354" -> "Imprisonment 1-5 years and fine"
            "354A" -> "Rigorous imprisonment 1-3 years, or fine, or both"
            "509" -> "Imprisonment up to 3 years, or fine, or both"
            "307" -> "Imprisonment up to 10 years and fine; life imprisonment if hurt caused"
            "503" -> "Imprisonment up to 2 years, or fine, or both"
            "67IT" -> "Imprisonment up to 5 years and fine up to ₹10 lakhs"
            "375/376" -> "Rigorous imprisonment not less than 10 years, may extend to life"
            else -> "As per IPC provisions"
        }
    }

    /**
     * Calculate case strength based on evidence and incident type
     */
    private fun calculateCaseStrength(incidentType: String, hasEvidence: Boolean): Int {
        var strength = 50 // Base strength

        // Evidence significantly increases strength
        if (hasEvidence) {
            strength += 40
        }

        // Certain incident types have higher conviction rates
        when (incidentType.lowercase()) {
            "domestic_violence", "dowry_violence" -> strength += 10
            "sexual_assault" -> strength += 5
            "sextortion" -> strength += 15
        }

        return strength.coerceIn(0, 100)
    }

    /**
     * Generate unique FIR number
     */
    private fun generateFIRNumber(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.US)
        val date = dateFormat.format(Date())
        val random = (1000..9999).random()
        return "FIR-$date-$random"
    }

    /**
     * Generate FIR text document
     */
    fun generateFIRText(fir: FIRDocument): String {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US)
        val incidentDateTime = dateFormat.format(Date(fir.dateTime))

        return buildString {
            appendLine("═══════════════════════════════════════")
            appendLine("     FIRST INFORMATION REPORT (FIR)")
            appendLine("═══════════════════════════════════════")
            appendLine()
            appendLine("FIR Number: ${fir.firNumber}")
            appendLine("Generated: ${dateFormat.format(Date(fir.generatedAt))}")
            appendLine()
            appendLine("───────────────────────────────────────")
            appendLine("VICTIM INFORMATION")
            appendLine("───────────────────────────────────────")
            appendLine("Name: ${fir.victimName}")
            appendLine()
            appendLine("───────────────────────────────────────")
            appendLine("INCIDENT DETAILS")
            appendLine("───────────────────────────────────────")
            appendLine("Type: ${fir.incidentType}")
            appendLine("Date/Time: $incidentDateTime")
            appendLine("Location: ${fir.location}")
            appendLine()
            appendLine("Description:")
            appendLine(fir.incidentDescription)
            appendLine()
            appendLine("───────────────────────────────────────")
            appendLine("APPLICABLE IPC SECTIONS")
            appendLine("───────────────────────────────────────")
            fir.ipcSections.forEach { section ->
                appendLine()
                appendLine("Section ${section.number}: ${section.title}")
                appendLine("Description: ${section.description}")
                appendLine("Punishment: ${section.punishment}")
            }
            appendLine()
            appendLine("───────────────────────────────────────")
            appendLine("CASE ASSESSMENT")
            appendLine("───────────────────────────────────────")
            appendLine("Case Strength: ${fir.caseStrength}%")
            appendLine("Conviction Probability: ${getCaseStrengthLabel(fir.caseStrength)}")
            appendLine()
            appendLine("───────────────────────────────────────")
            appendLine("NEXT STEPS")
            appendLine("───────────────────────────────────────")
            appendLine("1. File this FIR at the nearest police station")
            appendLine("2. Provide all evidence (video, audio, photos)")
            appendLine("3. Request medical examination if injured")
            appendLine("4. Consult with a lawyer (see recommendations)")
            appendLine("5. Keep copies of all documents")
            appendLine()
            appendLine("═══════════════════════════════════════")
            appendLine("   Generated by SHAKTI Legal AI")
            appendLine("═══════════════════════════════════════")
        }
    }

    /**
     * Get case strength label
     */
    private fun getCaseStrengthLabel(strength: Int): String {
        return when {
            strength >= 80 -> "Very High (${strength}%)"
            strength >= 60 -> "High (${strength}%)"
            strength >= 40 -> "Moderate (${strength}%)"
            else -> "Needs More Evidence (${strength}%)"
        }
    }

    /**
     * Get lawyer recommendations
     */
    fun getLawyerRecommendations(incidentType: String, location: String): List<LawyerInfo> {
        // In production, this would query a database or API
        // For MVP, return sample data
        return listOf(
            LawyerInfo(
                name = "Adv. Priya Sharma",
                specialization = "Women's Rights & Domestic Violence",
                experience = "12 years",
                rating = 4.8f,
                casesWon = 156,
                location = location,
                phone = "+91-9876543210",
                consultationFee = "Free first consultation"
            ),
            LawyerInfo(
                name = "Adv. Rajesh Kumar",
                specialization = "Criminal Law & Sexual Harassment",
                experience = "15 years",
                rating = 4.7f,
                casesWon = 203,
                location = location,
                phone = "+91-9876543211",
                consultationFee = "₹500"
            ),
            LawyerInfo(
                name = "Adv. Meera Desai",
                specialization = "Cybercrime & IT Act",
                experience = "8 years",
                rating = 4.9f,
                casesWon = 89,
                location = location,
                phone = "+91-9876543212",
                consultationFee = "Free first consultation"
            )
        )
    }

    // Data classes
    data class FIRDocument(
        val firNumber: String,
        val victimName: String,
        val incidentType: String,
        val incidentDescription: String,
        val location: String,
        val dateTime: Long,
        val ipcSections: List<IPCSection>,
        val caseStrength: Int,
        val generatedAt: Long
    )

    data class IPCSection(
        val number: String,
        val title: String,
        val description: String,
        val punishment: String
    )

    data class LawyerInfo(
        val name: String,
        val specialization: String,
        val experience: String,
        val rating: Float,
        val casesWon: Int,
        val location: String,
        val phone: String,
        val consultationFee: String
    )
}
