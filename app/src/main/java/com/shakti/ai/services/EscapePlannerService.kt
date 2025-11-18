package com.shakti.ai.services

import android.content.Context
import android.location.Location
import com.shakti.ai.utils.Constants
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Escape Planner Service - Financial planning and safe house recommendations
 *
 * Features:
 * - Financial needs calculation
 * - Safe house finder (distance-based)
 * - Step-by-step escape timeline
 * - Microfinance integration suggestions
 * - Document checklist
 */
class EscapePlannerService(private val context: Context) {

    /**
     * Calculate financial requirements for escape
     */
    fun calculateFinancialNeeds(
        hasChildren: Boolean,
        childrenCount: Int = 0,
        needsLegalHelp: Boolean = true,
        timeframe: TimeFrame = TimeFrame.THREE_MONTHS
    ): FinancialPlan {

        var totalAmount = 0
        val breakdown = mutableListOf<FinancialItem>()

        // Immediate transport (first day)
        val transport = Constants.EscapeCosts.IMMEDIATE_TRANSPORT
        breakdown.add(
            FinancialItem(
                category = "Immediate Transport",
                amount = transport,
                description = "Train/bus tickets, cab fare, emergency travel",
                priority = "Critical",
                timeframe = "Day 1"
            )
        )
        totalAmount += transport

        // Shelter costs
        val shelterMultiplier = when (timeframe) {
            TimeFrame.ONE_MONTH -> 1
            TimeFrame.THREE_MONTHS -> 3
            TimeFrame.SIX_MONTHS -> 6
        }
        val shelterCost = Constants.EscapeCosts.SHELTER_3_MONTHS / 3 * shelterMultiplier
        breakdown.add(
            FinancialItem(
                category = "Shelter & Living",
                amount = shelterCost,
                description = "Rent, food, utilities for $timeframe",
                priority = "Critical",
                timeframe = "${shelterMultiplier} months"
            )
        )
        totalAmount += shelterCost

        // Legal fees (if needed)
        if (needsLegalHelp) {
            val legalFees = Constants.EscapeCosts.LEGAL_FEES
            breakdown.add(
                FinancialItem(
                    category = "Legal Fees",
                    amount = legalFees,
                    description = "Lawyer consultation, court fees, FIR filing",
                    priority = "High",
                    timeframe = "Within 2 weeks"
                )
            )
            totalAmount += legalFees
        }

        // Children costs
        if (hasChildren && childrenCount > 0) {
            val childrenCost = 20000 * childrenCount * shelterMultiplier
            breakdown.add(
                FinancialItem(
                    category = "Children Care",
                    amount = childrenCost,
                    description = "School, food, clothes for $childrenCount child(ren)",
                    priority = "Critical",
                    timeframe = "${shelterMultiplier} months"
                )
            )
            totalAmount += childrenCost
        }

        // Emergency buffer
        val buffer = Constants.EscapeCosts.EMERGENCY_BUFFER
        breakdown.add(
            FinancialItem(
                category = "Emergency Fund",
                amount = buffer,
                description = "Medical emergencies, unexpected costs",
                priority = "Medium",
                timeframe = "Reserve"
            )
        )
        totalAmount += buffer

        // ID documents
        val documents = Constants.EscapeCosts.ID_DOCUMENTS
        breakdown.add(
            FinancialItem(
                category = "Documents",
                amount = documents,
                description = "Aadhar, PAN, Passport, Ration card duplicates",
                priority = "High",
                timeframe = "Within 1 month"
            )
        )
        totalAmount += documents

        return FinancialPlan(
            totalAmount = totalAmount,
            breakdown = breakdown,
            timeframe = timeframe,
            hasChildren = hasChildren,
            childrenCount = childrenCount,
            fundingSources = getFundingSources(totalAmount)
        )
    }

    /**
     * Get funding source suggestions
     */
    private fun getFundingSources(totalAmount: Int): List<FundingSource> {
        return listOf(
            FundingSource(
                name = "Personal Savings",
                type = "Self-funded",
                amount = (totalAmount * 0.3).toInt(),
                description = "Use emergency savings, FD, gold",
                contactInfo = null
            ),
            FundingSource(
                name = "NRLM Microfinance",
                type = "Government Loan",
                amount = (totalAmount * 0.4).toInt(),
                description = "National Rural Livelihood Mission - Women-focused loans",
                contactInfo = "www.nrlm.gov.in | 1800-111-555"
            ),
            FundingSource(
                name = "NGO Emergency Grants",
                type = "Grant/Aid",
                amount = (totalAmount * 0.2).toInt(),
                description = "Emergency grants from women's welfare NGOs",
                contactInfo = "Contact local women's helpline 181"
            ),
            FundingSource(
                name = "Family Support",
                type = "Personal",
                amount = (totalAmount * 0.1).toInt(),
                description = "Support from parents, siblings, trusted relatives",
                contactInfo = null
            )
        )
    }

    /**
     * Find nearby safe houses
     */
    fun findSafeHouses(currentLatitude: Double, currentLongitude: Double): List<SafeHouseInfo> {
        return Constants.SAFE_HOUSES.map { house ->
            val distance = calculateDistance(
                currentLatitude, currentLongitude,
                house.latitude, house.longitude
            )

            SafeHouseInfo(
                name = house.name,
                location = house.location,
                latitude = house.latitude,
                longitude = house.longitude,
                distance = distance,
                distanceText = formatDistance(distance),
                hours = house.hours,
                capacity = house.capacity,
                services = listOf(
                    "Safe shelter",
                    "Counseling",
                    "Legal aid",
                    "Job training",
                    "Medical support"
                ),
                contactNumber = "181", // National Women's Helpline
                rating = 4.5f
            )
        }.sortedBy { it.distance } // Sort by nearest first
    }

    /**
     * Calculate distance between two coordinates (Haversine formula)
     */
    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val R = 6371.0 // Earth radius in kilometers

        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val deltaLat = Math.toRadians(lat2 - lat1)
        val deltaLon = Math.toRadians(lon2 - lon1)

        val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(deltaLon / 2) * sin(deltaLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return R * c // Distance in km
    }

    /**
     * Format distance for display
     */
    private fun formatDistance(distanceKm: Double): String {
        return when {
            distanceKm < 1.0 -> "${(distanceKm * 1000).toInt()} meters"
            distanceKm < 10.0 -> String.format("%.1f km", distanceKm)
            else -> "${distanceKm.toInt()} km"
        }
    }

    /**
     * Generate step-by-step escape timeline
     */
    fun generateEscapeTimeline(
        hasEvidence: Boolean,
        hasLegalCase: Boolean,
        hasChildren: Boolean
    ): List<TimelineStep> {
        val timeline = mutableListOf<TimelineStep>()

        // Day 1: Immediate Actions
        timeline.add(
            TimelineStep(
                day = 1,
                phase = "Immediate Safety",
                title = "Leave Immediately",
                actions = listOf(
                    "Pack essentials: clothes, phone, charger, documents",
                    "Inform trusted friend/family",
                    "Leave when abuser is away",
                    "Disable location sharing on phone"
                ),
                duration = "1-2 hours",
                priority = "CRITICAL"
            )
        )

        // Day 1-2: Safe Shelter
        timeline.add(
            TimelineStep(
                day = 2,
                phase = "Initial Safety",
                title = "Reach Safe Shelter",
                actions = listOf(
                    "Go to nearest safe house or trusted person's home",
                    "Call Women's Helpline 181",
                    "Block abuser's number",
                    "Inform local police station (if safe to do so)"
                ),
                duration = "1 day",
                priority = "CRITICAL"
            )
        )

        // Week 1: Legal Action
        if (hasLegalCase) {
            timeline.add(
                TimelineStep(
                    day = 7,
                    phase = "Legal Protection",
                    title = "File Legal Cases",
                    actions = listOf(
                        "File FIR at police station",
                        "Apply for Protection Order under PWDVA",
                        "Consult with lawyer",
                        if (hasEvidence) "Submit evidence (videos, photos, audio)" else "Collect witness statements"
                    ),
                    duration = "3-5 days",
                    priority = "HIGH"
                )
            )
        }

        // Week 2-3: Financial Setup
        timeline.add(
            TimelineStep(
                day = 14,
                phase = "Financial Independence",
                title = "Setup Finances",
                actions = listOf(
                    "Open new bank account",
                    "Apply for microfinance loan",
                    "Contact NGOs for emergency grants",
                    "Retrieve personal savings/jewelry (with police help if needed)",
                    "Apply for government schemes (PM Awas Yojana, etc.)"
                ),
                duration = "1-2 weeks",
                priority = "HIGH"
            )
        )

        // Month 1: Documents
        timeline.add(
            TimelineStep(
                day = 30,
                phase = "Documentation",
                title = "Get ID Documents",
                actions = listOf(
                    "Apply for Aadhar duplicate",
                    "Get PAN card copy",
                    "Apply for ration card",
                    "School transfer certificate (if children)",
                    "Open post office savings account"
                ),
                duration = "2-3 weeks",
                priority = "MEDIUM"
            )
        )

        // Month 2: Job/Income
        timeline.add(
            TimelineStep(
                day = 60,
                phase = "Economic Stability",
                title = "Start Earning",
                actions = listOf(
                    "Enroll in skill training (NGO programs)",
                    "Apply for jobs (online/offline)",
                    "Start small business with microfinance",
                    "Join self-help group (SHG)",
                    "Explore work-from-home options"
                ),
                duration = "Ongoing",
                priority = "HIGH"
            )
        )

        // Month 3+: Long-term
        timeline.add(
            TimelineStep(
                day = 90,
                phase = "Rebuilding Life",
                title = "Long-term Stability",
                actions = listOf(
                    "Secure permanent housing",
                    "Establish stable income source",
                    "Build support network",
                    "Focus on mental health recovery",
                    "Help other women in similar situations"
                ),
                duration = "Ongoing",
                priority = "MEDIUM"
            )
        )

        return timeline
    }

    // Data classes
    data class FinancialPlan(
        val totalAmount: Int,
        val breakdown: List<FinancialItem>,
        val timeframe: TimeFrame,
        val hasChildren: Boolean,
        val childrenCount: Int,
        val fundingSources: List<FundingSource>
    )

    data class FinancialItem(
        val category: String,
        val amount: Int,
        val description: String,
        val priority: String, // Critical, High, Medium, Low
        val timeframe: String
    )

    data class FundingSource(
        val name: String,
        val type: String, // Self-funded, Government Loan, Grant/Aid, Personal
        val amount: Int,
        val description: String,
        val contactInfo: String?
    )

    data class SafeHouseInfo(
        val name: String,
        val location: String,
        val latitude: Double,
        val longitude: Double,
        val distance: Double,
        val distanceText: String,
        val hours: String,
        val capacity: String,
        val services: List<String>,
        val contactNumber: String,
        val rating: Float
    )

    data class TimelineStep(
        val day: Int,
        val phase: String,
        val title: String,
        val actions: List<String>,
        val duration: String,
        val priority: String // CRITICAL, HIGH, MEDIUM, LOW
    )

    enum class TimeFrame(val displayName: String) {
        ONE_MONTH("1 Month"),
        THREE_MONTHS("3 Months"),
        SIX_MONTHS("6 Months")
    }
}
