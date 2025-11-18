package com.shakti.ai.ui

import android.content.Intent
import android.os.Bundle
import android.os.Vibrator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.shakti.ai.R
import com.shakti.ai.databinding.ActivityCalculatorBinding
import com.shakti.ai.services.AudioDetectionService
import com.shakti.ai.utils.Constants
import com.shakti.ai.utils.PermissionsHelper

/**
 * Calculator Activity - Hidden UI that disguises the safety app
 *
 * Features:
 * - Fully functional calculator
 * - Secret codes to access dashboard (999=), SOS (911=), settings (777=)
 * - Visual monitoring indicator (green dot when active)
 * - Starts audio detection service in background
 */
class CalculatorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalculatorBinding
    private var currentInput = StringBuilder()
    private var operator = ""
    private var firstValue = 0.0
    private var isNewOperation = true
    private var secretCodeBuffer = StringBuilder()

    // Monitoring state
    private var isMonitoring = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalculatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCalculatorButtons()
        setupMonitoringToggle()
        checkFirstLaunch()
    }

    /**
     * Check if this is first launch and redirect to onboarding
     */
    private fun checkFirstLaunch() {
        val prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
        val isFirstLaunch = prefs.getBoolean(Constants.KEY_FIRST_LAUNCH, true)

        if (isFirstLaunch) {
            // First time opening app - go to onboarding
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }

        // Check if monitoring was enabled previously
        isMonitoring = prefs.getBoolean(Constants.KEY_MONITORING_ENABLED, false)
        updateMonitoringIndicator()

        if (isMonitoring) {
            startMonitoring()
        }
    }

    /**
     * Setup all calculator button click listeners
     */
    private fun setupCalculatorButtons() {
        // Number buttons
        binding.btn0.setOnClickListener { onNumberClick("0") }
        binding.btn1.setOnClickListener { onNumberClick("1") }
        binding.btn2.setOnClickListener { onNumberClick("2") }
        binding.btn3.setOnClickListener { onNumberClick("3") }
        binding.btn4.setOnClickListener { onNumberClick("4") }
        binding.btn5.setOnClickListener { onNumberClick("5") }
        binding.btn6.setOnClickListener { onNumberClick("6") }
        binding.btn7.setOnClickListener { onNumberClick("7") }
        binding.btn8.setOnClickListener { onNumberClick("8") }
        binding.btn9.setOnClickListener { onNumberClick("9") }

        // Operator buttons
        binding.btnAdd.setOnClickListener { onOperatorClick("+") }
        binding.btnSubtract.setOnClickListener { onOperatorClick("-") }
        binding.btnMultiply.setOnClickListener { onOperatorClick("×") }
        binding.btnDivide.setOnClickListener { onOperatorClick("÷") }

        // Special buttons
        binding.btnDecimal.setOnClickListener { onDecimalClick() }
        binding.btnEquals.setOnClickListener { onEqualsClick() }
        binding.btnClear.setOnClickListener { onClearClick() }
        binding.btnBackspace.setOnClickListener { onBackspaceClick() }
    }

    /**
     * Handle number button clicks
     */
    private fun onNumberClick(number: String) {
        if (isNewOperation) {
            currentInput.clear()
            isNewOperation = false
        }

        currentInput.append(number)
        secretCodeBuffer.append(number)
        updateDisplay()
        checkSecretCode()
    }

    /**
     * Handle operator button clicks
     */
    private fun onOperatorClick(op: String) {
        if (currentInput.isNotEmpty()) {
            if (operator.isNotEmpty()) {
                calculateResult()
            } else {
                firstValue = currentInput.toString().toDoubleOrNull() ?: 0.0
            }
            operator = op
            isNewOperation = true
        }
    }

    /**
     * Handle decimal point click
     */
    private fun onDecimalClick() {
        if (isNewOperation) {
            currentInput.clear()
            currentInput.append("0")
            isNewOperation = false
        }

        if (!currentInput.contains(".")) {
            currentInput.append(".")
            updateDisplay()
        }
    }

    /**
     * Handle equals button click - also checks for secret codes
     */
    private fun onEqualsClick() {
        secretCodeBuffer.append("=")
        val secretCode = secretCodeBuffer.toString()

        // Check secret codes BEFORE calculation
        when (secretCode) {
            Constants.SECRET_CODE_DASHBOARD -> {
                vibrate()
                openDashboard()
                clearAll()
                return
            }
            Constants.SECRET_CODE_SOS -> {
                vibrate()
                triggerSOS()
                clearAll()
                return
            }

            Constants.SECRET_CODE_SETTINGS -> {
                vibrate()
                openSettings()
                clearAll()
                return
            }
        }

        // Normal calculation
        if (currentInput.isNotEmpty() && operator.isNotEmpty()) {
            calculateResult()
        }

        secretCodeBuffer.clear()
    }

    /**
     * Perform calculation
     */
    private fun calculateResult() {
        val secondValue = currentInput.toString().toDoubleOrNull() ?: 0.0

        val result = when (operator) {
            "+" -> firstValue + secondValue
            "-" -> firstValue - secondValue
            "×" -> firstValue * secondValue
            "÷" -> if (secondValue != 0.0) firstValue / secondValue else 0.0
            else -> secondValue
        }

        currentInput.clear()
        currentInput.append(formatResult(result))
        binding.tvDisplay.text = currentInput.toString()

        firstValue = result
        operator = ""
        isNewOperation = true
    }

    /**
     * Format calculation result (remove unnecessary decimals)
     */
    private fun formatResult(value: Double): String {
        return if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            String.format("%.2f", value)
        }
    }

    /**
     * Handle clear button
     */
    private fun onClearClick() {
        clearAll()
    }

    /**
     * Handle backspace button
     */
    private fun onBackspaceClick() {
        if (currentInput.isNotEmpty()) {
            currentInput.deleteCharAt(currentInput.length - 1)
            updateDisplay()
        }

        if (secretCodeBuffer.isNotEmpty()) {
            secretCodeBuffer.deleteCharAt(secretCodeBuffer.length - 1)
        }
    }

    /**
     * Clear all calculator state
     */
    private fun clearAll() {
        currentInput.clear()
        secretCodeBuffer.clear()
        firstValue = 0.0
        operator = ""
        isNewOperation = true
        binding.tvDisplay.text = "0"
    }

    /**
     * Update calculator display
     */
    private fun updateDisplay() {
        binding.tvDisplay.text = if (currentInput.isEmpty()) "0" else currentInput.toString()
    }

    /**
     * Check if secret code is being entered
     */
    private fun checkSecretCode() {
        val buffer = secretCodeBuffer.toString()

        // Clear buffer if it gets too long (prevents memory leak)
        if (buffer.length > 10) {
            secretCodeBuffer.clear()
        }
    }

    /**
     * Setup monitoring toggle button (long press on AC button)
     */
    private fun setupMonitoringToggle() {
        binding.btnClear.setOnLongClickListener {
            toggleMonitoring()
            true
        }
    }

    /**
     * Toggle monitoring on/off
     */
    private fun toggleMonitoring() {
        if (!PermissionsHelper.hasAllPermissions(this)) {
            Toast.makeText(this, "Permissions required. Go to Settings.", Toast.LENGTH_SHORT).show()
            return
        }

        isMonitoring = !isMonitoring

        val prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
        prefs.edit().putBoolean(Constants.KEY_MONITORING_ENABLED, isMonitoring).apply()

        if (isMonitoring) {
            startMonitoring()
            Toast.makeText(this, "Protection Active", Toast.LENGTH_SHORT).show()
        } else {
            stopMonitoring()
            Toast.makeText(this, "Protection Paused", Toast.LENGTH_SHORT).show()
        }

        updateMonitoringIndicator()
        vibrate()
    }

    /**
     * Start audio detection service
     */
    private fun startMonitoring() {
        val intent = Intent(this, AudioDetectionService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    /**
     * Stop audio detection service
     */
    private fun stopMonitoring() {
        val intent = Intent(this, AudioDetectionService::class.java)
        stopService(intent)
    }

    /**
     * Update visual monitoring indicator (green dot)
     */
    private fun updateMonitoringIndicator() {
        binding.indicatorMonitoring.setBackgroundResource(
            if (isMonitoring) R.drawable.indicator_active else R.drawable.indicator_inactive
        )
    }

    /**
     * Open dashboard (hidden feature - secret code 999=)
     */
    private fun openDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java))
    }

    /**
     * Trigger SOS (secret code 911=)
     */
    private fun triggerSOS() {
        // Immediately start recording and alert
        val intent = Intent(this, AudioDetectionService::class.java)
        intent.action = "MANUAL_SOS"
        ContextCompat.startForegroundService(this, intent)

        Toast.makeText(this, "SOS Activated", Toast.LENGTH_SHORT).show()
    }

    /**
     * Open settings (secret code 777=)
     */
    private fun openSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    /**
     * Vibrate phone for tactile feedback
     */
    private fun vibrate() {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(50)
    }

    override fun onResume() {
        super.onResume()
        // Refresh monitoring state when returning to calculator
        val prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
        isMonitoring = prefs.getBoolean(Constants.KEY_MONITORING_ENABLED, false)
        updateMonitoringIndicator()
    }
}
