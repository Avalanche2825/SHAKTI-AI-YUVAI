package com.shakti.ai.utils

/**
 * App-wide constants for SHAKTI AI
 */
object Constants {

    // Shared Preferences
    const val PREFS_NAME = "shakti_prefs"
    const val KEY_FIRST_LAUNCH = "first_launch"
    const val KEY_USER_ID = "user_id"
    const val KEY_MONITORING_ENABLED = "monitoring_enabled"
    const val KEY_EMERGENCY_CONTACTS = "emergency_contacts"

    // Audio Detection
    const val AUDIO_SAMPLE_RATE = 16000
    const val AUDIO_BUFFER_SIZE = 1024
    const val SCREAM_CONFIDENCE_THRESHOLD = 0.75f
    const val STRESS_DETECTION_THRESHOLD = 0.70f

    // Video Recording
    const val VIDEO_QUALITY = "720p"
    const val VIDEO_FRAME_RATE = 30
    const val MAX_RECORDING_DURATION_MS = 180000L // 3 minutes

    // Location
    const val LOCATION_UPDATE_INTERVAL = 5000L // 5 seconds
    const val LOCATION_FASTEST_INTERVAL = 2000L // 2 seconds

    // Bluetooth
    const val BLE_SCAN_DURATION = 10000L // 10 seconds
    const val BLE_ALERT_RADIUS_METERS = 1000 // 1 km

    // Firebase Paths
    const val FIREBASE_USERS = "users"
    const val FIREBASE_INCIDENTS = "incidents"
    const val FIREBASE_EVIDENCE = "evidence"
    const val FIREBASE_ALERTS = "community_alerts"

    // IPC Sections (Indian Penal Code)
    val IPC_SECTIONS = mapOf(
        "domestic_violence" to listOf("498A", "406", "307", "323"),
        "street_harassment" to listOf("354", "509"),
        "workplace_harassment" to listOf("354A", "509"),
        "sextortion" to listOf("503", "504", "67IT"),
        "stalking" to listOf("507", "509"),
        "strangulation" to listOf("307", "336"),
        "dowry_violence" to listOf("498A", "304B", "406"),
        "sexual_assault" to listOf("354", "375", "376")
    )

    // Threat Detection Keywords
    val THREAT_KEYWORDS = listOf(
        "help", "stop", "no", "leave", "police", "emergency",
        "मदद", "रुको", "नहीं", "पुलिस", // Hindi
        "সাহায্য", "থামো", // Bengali
        "ಸಹಾಯ", "ನಿಲ್ಲು", // Kannada
        "உதவி", "நிறுத்து" // Tamil
    )

    // Safe Houses (Sample data - would come from API in production)
    val SAFE_HOUSES = listOf(
        SafeHouse("Shakti Foundation", "Delhi", 28.6139, 77.2090, "8 km", "24/7", "20 beds"),
        SafeHouse("ARIVAA", "Ghaziabad", 28.6692, 77.4538, "25 km", "24/7", "15 beds"),
        SafeHouse(
            "Breakthrough India",
            "Delhi NCR",
            28.5355,
            77.3910,
            "40 km",
            "9 AM - 6 PM",
            "30 beds"
        )
    )

    data class SafeHouse(
        val name: String,
        val location: String,
        val latitude: Double,
        val longitude: Double,
        val distance: String,
        val hours: String,
        val capacity: String
    )

    // Escape Plan Financial Estimates
    object EscapeCosts {
        const val IMMEDIATE_TRANSPORT = 15000
        const val SHELTER_3_MONTHS = 30000
        const val LEGAL_FEES = 20000
        const val EMERGENCY_BUFFER = 15000
        const val ID_DOCUMENTS = 10000
        const val MINIMUM_TOTAL = 90000
    }

    // Calculator Secret Codes
    const val SECRET_CODE_DASHBOARD = "999="
    const val SECRET_CODE_SOS = "911="
    const val SECRET_CODE_SETTINGS = "777="
}
