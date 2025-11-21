package com.shakti.ai.services

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.shakti.ai.R
import com.shakti.ai.ShaktiApplication
import com.shakti.ai.data.EvidenceDatabase
import com.shakti.ai.data.IncidentRecord
import com.shakti.ai.ui.CalculatorActivity
import com.shakti.ai.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
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
    private lateinit var database: EvidenceDatabase
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var currentIncidentId: String? = null

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
        database = EvidenceDatabase.getDatabase(this)

        // Start as foreground service with ULTRA STEALTH notification
        startForeground(NOTIFICATION_ID, createStealthNotification("Tracking"))

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

        // NO notification update for stealth mode - completely silent

        // Save to preferences
        saveCurrentLocation(locationData)

        // Update incident in database
        updateIncidentLocation(locationData)

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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Use new API for Android 13+
                var addressResult: String? = null
                geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                    if (addresses.isNotEmpty()) {
                        addressResult = addresses[0].getAddressLine(0)
                    }
                }
                addressResult
            } else {
                // Use deprecated API for older Android versions
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)

                if (!addresses.isNullOrEmpty()) {
                    addresses[0].getAddressLine(0)
                } else {
                    null
                }
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
        currentIncidentId?.let {
            val prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
            val editor = prefs.edit()
            editor.putString("incident_${it}_latitude", locationData.latitude.toString())
            editor.putString("incident_${it}_longitude", locationData.longitude.toString())
            editor.putString("incident_${it}_address", locationData.address)
            editor.putLong("incident_${it}_location_timestamp", locationData.timestamp)
            editor.apply()
        }
    }

    /**
     * Update incident record with location in database
     */
    private fun updateIncidentLocation(locationData: LocationData) {
        val incidentId = currentIncidentId
            ?: getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
                .getString("current_incident_id", null)

        incidentId?.let {
            serviceScope.launch {
                try {
                    val incident = database.incidentDao().getIncidentById(it)
                    incident?.let { inc ->
                        database.incidentDao().updateIncident(
                            inc.copy(
                                latitude = locationData.latitude,
                                longitude = locationData.longitude,
                                address = locationData.address
                            )
                        )
                        android.util.Log.d(
                            "LocationService",
                            "✅ Location updated in database for incident: $it"
                        )
                    }
                } catch (e: Exception) {
                    android.util.Log.e("LocationService", "Failed to update location", e)
                }
            }
        }
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
     * Create ULTRA STEALTH foreground notification (completely invisible)
     */
    private fun createStealthNotification(message: String): Notification {
        val intent = Intent(this, CalculatorActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // MAXIMUM STEALTH: Minimal notification, NO sound, NO vibration, completely hidden
        return NotificationCompat.Builder(this, ShaktiApplication.CHANNEL_ID_LOCATION)
            .setContentTitle("System") // Generic title
            .setContentText("") // Empty text for stealth
            .setSmallIcon(android.R.drawable.ic_dialog_info) // System icon
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN) // Minimal visibility
            .setSound(null) // NO SOUND
            .setVibrate(null) // NO VIBRATION
            .setSilent(true) // SILENT
            .setShowWhen(false) // Hide timestamp
            .setVisibility(NotificationCompat.VISIBILITY_SECRET) // Hide from lock screen
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        serviceScope.cancel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Get incident ID from intent if provided
        currentIncidentId = intent?.getStringExtra("incident_id")
            ?: getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
                .getString("current_incident_id", null)

        android.util.Log.d("LocationService", "Location tracking for incident: $currentIncidentId")

        return START_STICKY
    }
}
