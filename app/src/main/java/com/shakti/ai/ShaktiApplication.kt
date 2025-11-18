package com.shakti.ai

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase

/**
 * Application class for SHAKTI AI
 * Initializes Firebase, notification channels, and global app state
 */
class ShaktiApplication : Application() {

    companion object {
        const val CHANNEL_ID_THREAT = "threat_detection_channel"
        const val CHANNEL_ID_RECORDING = "recording_channel"
        const val CHANNEL_ID_LOCATION = "location_channel"
        const val CHANNEL_ID_GENERAL = "general_channel"

        lateinit var instance: ShaktiApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        // Create notification channels for Android 8.0+
        createNotificationChannels()
    }

    /**
     * Creates notification channels for different types of alerts
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Threat Detection Channel (Critical)
            val threatChannel = NotificationChannel(
                CHANNEL_ID_THREAT,
                "Threat Detection",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical alerts for detected threats"
                enableVibration(true)
                setShowBadge(true)
            }

            // Recording Channel (High)
            val recordingChannel = NotificationChannel(
                CHANNEL_ID_RECORDING,
                "Evidence Recording",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Active recording notifications"
                enableVibration(false)
                setShowBadge(true)
            }

            // Location Channel (Medium)
            val locationChannel = NotificationChannel(
                CHANNEL_ID_LOCATION,
                "Location Tracking",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Background location tracking"
                enableVibration(false)
                setShowBadge(false)
            }

            // General Channel (Low)
            val generalChannel = NotificationChannel(
                CHANNEL_ID_GENERAL,
                "General",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "General app notifications"
                enableVibration(false)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannels(
                listOf(threatChannel, recordingChannel, locationChannel, generalChannel)
            )
        }
    }
}
