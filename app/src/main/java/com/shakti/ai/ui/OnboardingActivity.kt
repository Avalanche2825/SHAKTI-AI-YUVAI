package com.shakti.ai.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.shakti.ai.databinding.ActivityOnboardingBinding
import com.shakti.ai.utils.Constants
import com.shakti.ai.utils.PermissionsHelper

/**
 * Onboarding Activity - First-time user setup
 *
 * Features:
 * - Welcome screen
 * - Permission requests with explanations
 * - Emergency contacts setup
 * - How to use guide
 */
class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private var currentPage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupPages()
        setupButtons()
    }

    private fun setupPages() {
        currentPage = 0
        showPage(currentPage)
    }

    private fun setupButtons() {
        binding.btnNext.setOnClickListener {
            when (currentPage) {
                0 -> {
                    currentPage = 1
                    showPage(currentPage)
                }

                1 -> {
                    requestPermissions()
                }

                2 -> {
                    finishOnboarding()
                }
            }
        }

        binding.btnSkip.setOnClickListener {
            finishOnboarding()
        }
    }

    private fun showPage(page: Int) {
        when (page) {
            0 -> showWelcomePage()
            1 -> showPermissionsPage()
            2 -> showHowToUsePage()
        }

        binding.btnNext.text = if (page == 2) "Get Started" else "Next"
        binding.btnSkip.visibility =
            if (page == 2) android.view.View.GONE else android.view.View.VISIBLE
    }

    private fun showWelcomePage() {
        binding.tvTitle.text = "Welcome to SHAKTI"
        binding.tvDescription.text = """
            Your personal safety companion powered by AI.
            
            SHAKTI provides:
            • Real-time threat detection
            • Automatic evidence recording
            • Legal assistance (NYAY)
            • Escape planning tools
            • Community safety network
        """.trimIndent()
    }

    private fun showPermissionsPage() {
        binding.tvTitle.text = "Required Permissions"
        binding.tvDescription.text = """
            SHAKTI needs these permissions to protect you:
            
            Microphone - Detect threats through audio
            Camera - Capture video evidence
            Location - Share your location with emergency contacts
            Bluetooth - Alert nearby SHAKTI users
            
            All data is encrypted and stays on your device.
        """.trimIndent()
    }

    private fun showHowToUsePage() {
        binding.tvTitle.text = "How to Use"
        binding.tvDescription.text = """
            The app disguises as a calculator for your safety.
            
            Secret Codes:
            • Long press AC → Enable/Disable monitoring
            • 999= → Open dashboard
            • 911= → Emergency SOS
            • 777= → Settings
            
            When monitoring is active (green dot), SHAKTI listens for threats 24/7.
        """.trimIndent()
    }

    private fun requestPermissions() {
        PermissionsHelper.requestAllPermissions(
            this,
            onAllGranted = {
                currentPage = 2
                showPage(currentPage)
            },
            onAnyDenied = { deniedPermissions ->
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Permissions Required")
                    .setMessage("SHAKTI needs all permissions to protect you effectively. Please grant all permissions.")
                    .setPositiveButton("Try Again") { _, _ ->
                        requestPermissions()
                    }
                    .setNegativeButton("Skip") { _, _ ->
                        currentPage = 2
                        showPage(currentPage)
                    }
                    .show()
            }
        )
    }

    private fun finishOnboarding() {
        // Mark onboarding as complete
        val prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
        prefs.edit()
            .putBoolean(Constants.KEY_FIRST_LAUNCH, false)
            .apply()

        // Go to calculator
        startActivity(Intent(this, CalculatorActivity::class.java))
        finish()
    }
}
