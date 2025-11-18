package com.shakti.ai.services

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.ParcelUuid
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.shakti.ai.R
import com.shakti.ai.ShaktiApplication
import com.shakti.ai.ui.CalculatorActivity
import com.shakti.ai.utils.Constants
import java.nio.ByteBuffer
import java.util.*

/**
 * Bluetooth Service - Community alert system using BLE
 *
 * Features:
 * - Broadcasts emergency alerts to nearby SHAKTI users (1km radius)
 * - Scans for nearby alerts from other users
 * - Works offline (no internet required)
 * - Low power consumption (BLE)
 * - Peer-to-peer emergency network
 */
class BluetoothService : Service() {

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothLeAdvertiser: BluetoothLeAdvertiser? = null
    private var bluetoothLeScanner: BluetoothLeScanner? = null

    private var isAdvertising = false
    private var isScanning = false

    // Service UUID for SHAKTI emergency alerts
    private val SHAKTI_SERVICE_UUID = UUID.fromString("12345678-1234-5678-1234-567812345678")

    companion object {
        const val ACTION_SEND_ALERT = "SEND_ALERT"
        const val ACTION_START_SCANNING = "START_SCANNING"
        const val ACTION_STOP_ALL = "STOP_ALL"
        const val NOTIFICATION_ID = 1004
    }

    override fun onCreate() {
        super.onCreate()

        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        bluetoothLeAdvertiser = bluetoothAdapter?.bluetoothLeAdvertiser
        bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner

        startForeground(NOTIFICATION_ID, createNotification("Bluetooth service active"))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SEND_ALERT -> {
                startAdvertising()
                startScanning() // Also scan for other alerts
            }

            ACTION_START_SCANNING -> startScanning()
            ACTION_STOP_ALL -> stopAllOperations()
        }

        return START_STICKY
    }

    /**
     * Start advertising emergency alert to nearby devices
     */
    private fun startAdvertising() {
        if (isAdvertising || !hasBluetoothPermission()) {
            return
        }

        try {
            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH) // Max range
                .setConnectable(false)
                .setTimeout(60000) // Advertise for 1 minute
                .build()

            val data = AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(true)
                .addServiceUuid(ParcelUuid(SHAKTI_SERVICE_UUID))
                .addServiceData(
                    ParcelUuid(SHAKTI_SERVICE_UUID),
                    createAlertData()
                )
                .build()

            bluetoothLeAdvertiser?.startAdvertising(
                settings,
                data,
                advertiseCallback
            )

            isAdvertising = true
            updateNotification("🚨 Broadcasting emergency alert...")

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Create alert data payload
     */
    private fun createAlertData(): ByteArray {
        val prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
        val userId = prefs.getString(Constants.KEY_USER_ID, "unknown") ?: "unknown"

        // Pack data: timestamp (8 bytes) + alert type (1 byte)
        val buffer = ByteBuffer.allocate(9)
        buffer.putLong(System.currentTimeMillis())
        buffer.put(0x01) // Alert type: Emergency

        return buffer.array()
    }

    /**
     * Advertising callback
     */
    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            android.util.Log.d("BluetoothService", "Advertising started successfully")
        }

        override fun onStartFailure(errorCode: Int) {
            android.util.Log.e("BluetoothService", "Advertising failed: $errorCode")
            isAdvertising = false
        }
    }

    /**
     * Start scanning for nearby alerts
     */
    private fun startScanning() {
        if (isScanning || !hasBluetoothPermission()) {
            return
        }

        try {
            val scanFilter = ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(SHAKTI_SERVICE_UUID))
                .build()

            val scanSettings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY) // Fast scanning
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .build()

            bluetoothLeScanner?.startScan(
                listOf(scanFilter),
                scanSettings,
                scanCallback
            )

            isScanning = true
            updateNotification("Scanning for nearby alerts...")

            // Auto-stop scanning after duration
            android.os.Handler(mainLooper).postDelayed({
                stopScanning()
            }, Constants.BLE_SCAN_DURATION)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Scan callback - receives alerts from nearby users
     */
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val rssi = result.rssi // Signal strength

            // Extract alert data
            val serviceData = result.scanRecord
                ?.getServiceData(ParcelUuid(SHAKTI_SERVICE_UUID))

            if (serviceData != null && serviceData.size >= 9) {
                val buffer = ByteBuffer.wrap(serviceData)
                val timestamp = buffer.long
                val alertType = buffer.get()

                // Calculate approximate distance from RSSI
                val distance = calculateDistance(rssi)

                if (distance <= Constants.BLE_ALERT_RADIUS_METERS) {
                    onNearbyAlertDetected(device.address, distance, timestamp)
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            android.util.Log.e("BluetoothService", "Scan failed: $errorCode")
            isScanning = false
        }
    }

    /**
     * Calculate distance from RSSI (signal strength)
     *
     * Formula: distance = 10 ^ ((Measured Power - RSSI) / (10 * N))
     * Where N = path loss exponent (typically 2-4)
     */
    private fun calculateDistance(rssi: Int): Int {
        val txPower = -59 // Measured power at 1 meter
        val n = 2.0 // Path loss exponent

        val distance = Math.pow(10.0, (txPower - rssi) / (10 * n))
        return distance.toInt()
    }

    /**
     * Handle nearby alert detection
     */
    private fun onNearbyAlertDetected(deviceId: String, distance: Int, timestamp: Long) {
        android.util.Log.d(
            "BluetoothService",
            "Alert detected from $deviceId at ${distance}m"
        )

        // Show notification to user
        showAlertNotification(distance)

        // Save to incident log
        saveNearbyAlert(deviceId, distance, timestamp)
    }

    /**
     * Show notification about nearby alert
     */
    private fun showAlertNotification(distance: Int) {
        val intent = Intent(this, CalculatorActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, ShaktiApplication.CHANNEL_ID_THREAT)
            .setContentTitle("⚠️ Emergency Alert Nearby")
            .setContentText("Another SHAKTI user needs help ($distance meters away)")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(9999, notification)
    }

    /**
     * Save nearby alert to preferences
     */
    private fun saveNearbyAlert(deviceId: String, distance: Int, timestamp: Long) {
        val prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
        val editor = prefs.edit()

        val alertId = UUID.randomUUID().toString()
        editor.putString("nearby_alert_${alertId}_device", deviceId)
        editor.putInt("nearby_alert_${alertId}_distance", distance)
        editor.putLong("nearby_alert_${alertId}_timestamp", timestamp)
        editor.apply()
    }

    /**
     * Stop scanning
     */
    private fun stopScanning() {
        if (isScanning) {
            try {
                bluetoothLeScanner?.stopScan(scanCallback)
                isScanning = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Stop advertising
     */
    private fun stopAdvertising() {
        if (isAdvertising) {
            try {
                bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
                isAdvertising = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Stop all Bluetooth operations
     */
    private fun stopAllOperations() {
        stopAdvertising()
        stopScanning()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    /**
     * Check Bluetooth permissions
     */
    private fun hasBluetoothPermission(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) ==
                    PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) ==
                    PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) ==
                    PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) ==
                    PackageManager.PERMISSION_GRANTED
        }
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

        return NotificationCompat.Builder(this, ShaktiApplication.CHANNEL_ID_GENERAL)
            .setContentTitle("Community Network")
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
        stopAllOperations()
    }
}
