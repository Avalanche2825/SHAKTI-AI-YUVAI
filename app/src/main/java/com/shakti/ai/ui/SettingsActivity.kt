package com.shakti.ai.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.shakti.ai.databinding.ActivitySettingsBinding
import com.shakti.ai.utils.Constants

/**
 * Settings Activity - App configuration and preferences
 *
 * Features:
 * - Emergency contacts management
 * - Detection sensitivity adjustment
 * - Privacy settings
 * - Test features
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        loadSettings()
        setupButtons()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Settings"
            setDisplayHomeAsUpEnabled(true)
        }

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)

        // Load monitoring state
        binding.switchMonitoring.isChecked =
            prefs.getBoolean(Constants.KEY_MONITORING_ENABLED, false)

        // Load emergency contacts
        val contacts = prefs.getString(Constants.KEY_EMERGENCY_CONTACTS, "")
        binding.etEmergencyContacts.setText(contacts)
    }

    private fun setupButtons() {
        // Monitoring toggle
        binding.switchMonitoring.setOnCheckedChangeListener { _, isChecked ->
            val prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
            prefs.edit().putBoolean(Constants.KEY_MONITORING_ENABLED, isChecked).apply()

            Toast.makeText(
                this,
                if (isChecked) "Monitoring enabled" else "Monitoring disabled",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Save emergency contacts
        binding.btnSaveContacts.setOnClickListener {
            val contacts = binding.etEmergencyContacts.text.toString()
            val prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
            prefs.edit().putString(Constants.KEY_EMERGENCY_CONTACTS, contacts).apply()

            Toast.makeText(this, "Emergency contacts saved", Toast.LENGTH_SHORT).show()
        }

        // Test audio detection
        binding.btnTestAudio.setOnClickListener {
            Toast.makeText(this, "Make a loud noise to test detection", Toast.LENGTH_LONG).show()
        }

        // Clear data
        binding.btnClearData.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Clear All Data")
                .setMessage("This will delete all incident reports and evidence. Continue?")
                .setPositiveButton("Yes, Clear") { _, _ ->
                    clearAllData()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun clearAllData() {
        val prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
        prefs.edit().clear().apply()

        Toast.makeText(this, "All data cleared", Toast.LENGTH_SHORT).show()
        finish()
    }
}
