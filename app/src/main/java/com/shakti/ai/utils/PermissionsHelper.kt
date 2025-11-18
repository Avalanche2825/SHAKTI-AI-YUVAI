package com.shakti.ai.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

/**
 * Helper class to handle all app permissions
 */
object PermissionsHelper {

    // Permission request codes
    const val REQUEST_CODE_ALL_PERMISSIONS = 100
    const val REQUEST_CODE_AUDIO = 101
    const val REQUEST_CODE_CAMERA = 102
    const val REQUEST_CODE_LOCATION = 103
    const val REQUEST_CODE_BLUETOOTH = 104

    /**
     * All required permissions for the app
     */
    private val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO
        )
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    } else {
        arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    /**
     * Check if all required permissions are granted
     */
    fun hasAllPermissions(context: Context): Boolean {
        return REQUIRED_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Request all permissions using Dexter library (simplified UX)
     */
    fun requestAllPermissions(
        activity: Activity,
        onAllGranted: () -> Unit,
        onAnyDenied: (List<String>) -> Unit
    ) {
        Dexter.withActivity(activity)
            .withPermissions(REQUIRED_PERMISSIONS.toList())
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        onAllGranted()
                    } else {
                        val deniedPermissions = report.deniedPermissionResponses
                            .map { it.permissionName }
                        onAnyDenied(deniedPermissions)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            })
            .check()
    }

    /**
     * Check specific permission
     */
    fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Request specific permission
     */
    fun requestPermission(
        activity: Activity,
        permission: String,
        requestCode: Int
    ) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(permission),
            requestCode
        )
    }

    /**
     * Check audio recording permission
     */
    fun hasAudioPermission(context: Context): Boolean {
        return hasPermission(context, Manifest.permission.RECORD_AUDIO)
    }

    /**
     * Check camera permission
     */
    fun hasCameraPermission(context: Context): Boolean {
        return hasPermission(context, Manifest.permission.CAMERA)
    }

    /**
     * Check location permission
     */
    fun hasLocationPermission(context: Context): Boolean {
        return hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) &&
                hasPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    /**
     * Check bluetooth permission (varies by Android version)
     */
    fun hasBluetoothPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            hasPermission(context, Manifest.permission.BLUETOOTH_CONNECT) &&
                    hasPermission(context, Manifest.permission.BLUETOOTH_SCAN)
        } else {
            hasPermission(context, Manifest.permission.BLUETOOTH) &&
                    hasPermission(context, Manifest.permission.BLUETOOTH_ADMIN)
        }
    }

    /**
     * Get friendly permission name for UI display
     */
    fun getPermissionName(permission: String): String {
        return when (permission) {
            Manifest.permission.RECORD_AUDIO -> "Microphone"
            Manifest.permission.CAMERA -> "Camera"
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION -> "Location"

            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN -> "Bluetooth"

            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO -> "Storage"

            Manifest.permission.POST_NOTIFICATIONS -> "Notifications"
            else -> "Unknown"
        }
    }

    /**
     * Get permission explanation for user
     */
    fun getPermissionExplanation(permission: String): String {
        return when (permission) {
            Manifest.permission.RECORD_AUDIO ->
                "Required to detect threats through audio analysis"

            Manifest.permission.CAMERA ->
                "Required to capture video evidence during incidents"

            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION ->
                "Required to share your location with emergency contacts"

            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN ->
                "Required to alert nearby SHAKTI users in emergencies"

            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO ->
                "Required to save evidence and legal documents"

            Manifest.permission.POST_NOTIFICATIONS ->
                "Required to alert you of detected threats"

            else -> "Required for app functionality"
        }
    }
}
