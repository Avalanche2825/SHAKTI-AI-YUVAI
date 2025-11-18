package com.shakti.ai.services

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.shakti.ai.R
import com.shakti.ai.ShaktiApplication
import com.shakti.ai.ui.CalculatorActivity
import com.shakti.ai.utils.Constants
import java.util.*

/**
 * Location Service - Tracks GPS coordinates during incidents
 *
 * Features:
 * - High-accuracy GPS tracking
 * - Reverse geocoding (coordinates → address)
 * - Continuous updates every 5 seconds
 * - Battery optimized
 * - Saves location history
 */
class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var isTracking = false

    private val locationHistory = mutableListOf<LocationData>()

    companion object {
        const val NOTIFICATION_ID = 1003
    }

    data class LocationData(
        val latitude: Double,
        val longitude: Double,
        val accuracy: Float,
        val timestamp: Long,
        val address: String? = null
    )

    override fun onCreate() {
        super.onCreate()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Start as foreground service
        startForeground(NOTIFICATION_ID, createNotification("Tracking location..."))

        setupLocationCallback()
        startLocationUpdates()
    }

    /**
     * Setup location update callback
     */
    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    onLocationReceived(location)
                }
            }
        }
    }

    /**
     * Start receiving location updates
     */
    private fun startLocationUpdates() {
        if (!hasLocationPermission()) {
            stopSelf()
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            Constants.LOCATION_UPDATE_INTERVAL
        ).apply {
            setMinUpdateIntervalMillis(Constants.LOCATION_FASTEST_INTERVAL)
            setWaitForAccurateLocation(true)
        }.build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            isTracking = true

        } catch (e: SecurityException) {
            e.printStackTrace()
            stopSelf()
        }
    }

    /**
     * Handle new location update
     */
    private fun onLocationReceived(location: Location) {
        val address = getAddressFromLocation(location.latitude, location.longitude)

        val locationData = LocationData(
            latitude = location.latitude,
            longitude = location.longitude,
            accuracy = location.accuracy,
            timestamp = System.currentTimeMillis(),
            address = address
        )

        locationHistory.add(locationData)

        // Update notification with current location
        updateNotification(
            "📍 Location: ${String.format("%.6f", location.latitude)}, " +
                    "${String.format("%.6f", location.longitude)}"
        )

        // Save to preferences
        saveCurrentLocation(locationData)

        android.util.Log.d(
            "LocationService",
            "Location: ${location.latitude}, ${location.longitude}, Accuracy: ${location.accuracy}m"
        )
    }

    /**
     * Convert coordinates to readable address
     */
    private fun getAddressFromLocation(latitude: Double, longitude: Double): String? {
        return try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)

            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                buildString {
                    address.getAddressLine(0)?.let { append(it) }
                }
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Save current location to preferences
     */
    private fun saveCurrentLocation(locationData: LocationData) {
        val prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
        val incidentId = prefs.getString("current_incident_id", UUID.randomUUID().toString())

        val editor = prefs.edit()
        editor.putString("incident_${incidentId}_latitude", locationData.latitude.toString())
        editor.putString("incident_${incidentId}_longitude", locationData.longitude.toString())
        editor.putString("incident_${incidentId}_address", locationData.address)
        editor.putLong("incident_${incidentId}_location_timestamp", locationData.timestamp)
        editor.apply()
    }

    /**
     * Stop location updates
     */
    private fun stopLocationUpdates() {
        if (isTracking) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            isTracking = false
        }
    }

    /**
     * Check location permission
     */
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Create foreground notification
     */
    private fun createNotification(message: String): Notification {
        val intent = Intent(this, CalculatorActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, ShaktiApplication.CHANNEL_ID_LOCATION)
            .setContentTitle("Location Tracking")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    /**
     * Update notification
     */
    private fun updateNotification(message: String) {
        val notification = createNotification(message)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }
}
