package com.shakti.ai.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.shakti.ai.R
import com.shakti.ai.databinding.ActivityEscapePlannerBinding
import com.shakti.ai.services.EscapePlannerService
import com.shakti.ai.utils.Constants

/**
 * Escape Planner Activity - Financial planning and safe house finder
 *
 * Features:
 * - Financial needs calculator
 * - Safe house recommendations (sorted by distance)
 * - Step-by-step escape timeline
 * - Funding sources and microfinance options
 */
class EscapePlannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEscapePlannerBinding
    private lateinit var escapePlannerService: EscapePlannerService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEscapePlannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        escapePlannerService = EscapePlannerService(this)

        setupToolbar()
        setupInputs()
        setupButtons()
        loadSafeHouses()
        setupAIChatButton()
    }

    /**
     * Setup toolbar
     */
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Escape Planner"
            setDisplayHomeAsUpEnabled(true)
        }

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    /**
     * Setup input fields
     */
    private fun setupInputs() {
        // Timeframe spinner
        val timeframes = arrayOf("1 Month", "3 Months", "6 Months")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timeframes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTimeframe.adapter = adapter
        binding.spinnerTimeframe.setSelection(1) // Default: 3 months
    }

    /**
     * Setup buttons
     */
    private fun setupButtons() {
        // Calculate button
        binding.btnCalculate.setOnClickListener {
            calculateFinancialPlan()
        }

        // View Timeline button
        binding.btnViewTimeline.setOnClickListener {
            showTimeline()
        }

        // Find Safe Houses button
        binding.btnFindSafeHouses.setOnClickListener {
            showSafeHouses()
        }
    }

    /**
     * Calculate financial plan
     */
    private fun calculateFinancialPlan() {
        val hasChildren = binding.switchHasChildren.isChecked
        val childrenCount = if (hasChildren) {
            binding.etChildrenCount.text.toString().toIntOrNull() ?: 0
        } else {
            0
        }
        val needsLegalHelp = binding.switchNeedsLegalHelp.isChecked

        val timeframe = when (binding.spinnerTimeframe.selectedItemPosition) {
            0 -> EscapePlannerService.TimeFrame.ONE_MONTH
            1 -> EscapePlannerService.TimeFrame.THREE_MONTHS
            2 -> EscapePlannerService.TimeFrame.SIX_MONTHS
            else -> EscapePlannerService.TimeFrame.THREE_MONTHS
        }

        // Calculate plan
        val plan = escapePlannerService.calculateFinancialNeeds(
            hasChildren = hasChildren,
            childrenCount = childrenCount,
            needsLegalHelp = needsLegalHelp,
            timeframe = timeframe
        )

        // Display results
        displayFinancialPlan(plan)
    }

    /**
     * Display financial plan
     */
    private fun displayFinancialPlan(plan: EscapePlannerService.FinancialPlan) {
        binding.layoutResults.visibility = android.view.View.VISIBLE

        // Total amount
        binding.tvTotalAmount.text =
            "₹${plan.totalAmount.toString().replace(Regex("(\\d)(?=(\\d{3})+$)"), "$1,")}"

        // Breakdown
        val breakdownText = buildString {
            append("Financial Breakdown:\n\n")
            plan.breakdown.forEach { item ->
                append("${item.category}\n")
                append(
                    "₹${
                        item.amount.toString().replace(Regex("(\\d)(?=(\\d{3})+$)"), "$1,")
                    } - ${item.timeframe}\n"
                )
                append("${item.description}\n")
                append("Priority: ${item.priority}\n\n")
            }
        }
        binding.tvBreakdown.text = breakdownText

        // Funding sources
        val fundingText = buildString {
            append("Suggested Funding Sources:\n\n")
            plan.fundingSources.forEachIndexed { index, source ->
                if (index > 0) append("\n")
                append("${index + 1}. ${source.name}\n")
                append("   Type: ${source.type}\n")
                append(
                    "   Amount: ₹${
                        source.amount.toString().replace(Regex("(\\d)(?=(\\d{3})+$)"), "$1,")
                    }\n"
                )
                append("   ${source.description}\n")
                if (source.contactInfo != null) {
                    append("   Contact: ${source.contactInfo}\n")
                }
            }
        }
        binding.tvFundingSources.text = fundingText
    }

    /**
     * Show escape timeline
     */
    private fun showTimeline() {
        val hasEvidence = true // Check from preferences
        val hasLegalCase = binding.switchNeedsLegalHelp.isChecked
        val hasChildren = binding.switchHasChildren.isChecked

        val timeline = escapePlannerService.generateEscapeTimeline(
            hasEvidence = hasEvidence,
            hasLegalCase = hasLegalCase,
            hasChildren = hasChildren
        )

        // Build timeline text
        val timelineText = buildString {
            append("Step-by-Step Escape Timeline:\n\n")
            timeline.forEach { step ->
                append("━━━━━━━━━━━━━━━━━━━━━━\n")
                append("Day ${step.day}: ${step.phase}\n")
                append("${step.title}\n")
                append("Duration: ${step.duration}\n")
                append("Priority: ${step.priority}\n\n")
                append("Actions:\n")
                step.actions.forEachIndexed { index, action ->
                    append("${index + 1}. $action\n")
                }
                append("\n")
            }
        }

        // Show in dialog
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Escape Timeline")
            .setMessage(timelineText)
            .setPositiveButton("OK", null)
            .show()
    }

    /**
     * Load and display safe houses
     */
    private fun loadSafeHouses() {
        // Get current location (default to Delhi)
        val latitude = 28.6139
        val longitude = 77.2090

        val safeHouses = escapePlannerService.findSafeHouses(latitude, longitude)

        binding.tvSafeHouseCount.text = "${safeHouses.size} safe houses nearby"
    }

    /**
     * Show safe houses
     */
    private fun showSafeHouses() {
        val latitude = 28.6139
        val longitude = 77.2090

        val safeHouses = escapePlannerService.findSafeHouses(latitude, longitude)

        val safeHouseText = buildString {
            append("Nearby Safe Houses (Sorted by Distance):\n\n")
            safeHouses.forEachIndexed { index, house ->
                if (index > 0) append("\n")
                append("${index + 1}. ${house.name}\n")
                append("   Location: ${house.location}\n")
                append("   Distance: ${house.distanceText}\n")
                append("   Hours: ${house.hours}\n")
                append("   Capacity: ${house.capacity}\n")
                append("   Rating: ${house.rating}⭐\n")
                append("   Contact: ${house.contactNumber}\n")
                append("   Services: ${house.services.joinToString(", ")}\n")
            }
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Safe Houses")
            .setMessage(safeHouseText)
            .setPositiveButton("Call Women's Helpline") { _, _ ->
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = android.net.Uri.parse("tel:181")
                startActivity(intent)
            }
            .setNegativeButton("Close", null)
            .show()
    }

    /**
     * Setup AI Chat Assistant button
     */
    private fun setupAIChatButton() {
        try {
            // Create FAB programmatically if not in layout
            val fabChat =
                com.google.android.material.floatingactionbutton.FloatingActionButton(this)
            fabChat.setImageResource(android.R.drawable.ic_dialog_info)
            fabChat.backgroundTintList = android.content.res.ColorStateList.valueOf(
                androidx.core.content.ContextCompat.getColor(this, com.shakti.ai.R.color.accent)
            )

            fabChat.setOnClickListener {
                val fragment = com.shakti.ai.ui.components.AIChatFragment.newInstance(
                    com.shakti.ai.models.ChatContext.ESCAPE
                )

                // Show as bottom sheet dialog
                val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(this)
                dialog.setContentView(fragment.requireView())
                dialog.show()
            }

            // Add to layout if root is ViewGroup
            val rootView = binding.root
            if (rootView is android.view.ViewGroup) {
                val params = android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = android.view.Gravity.BOTTOM or android.view.Gravity.END
                    setMargins(0, 0, 48, 48)
                }
                rootView.addView(fabChat, params)
            }
        } catch (e: Exception) {
            // FAB creation failed, skip silently
            android.util.Log.e("EscapePlanner", "Failed to add chat FAB: ${e.message}")
        }
    }
}
