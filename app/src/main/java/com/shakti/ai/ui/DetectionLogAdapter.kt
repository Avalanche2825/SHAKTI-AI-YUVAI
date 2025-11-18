package com.shakti.ai.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shakti.ai.databinding.ItemDetectionLogBinding
import java.text.SimpleDateFormat
import java.util.*

/**
 * RecyclerView Adapter for AI Detection Log
 *
 * Displays history of ML detections with:
 * - Timestamp
 * - Detection type
 * - Confidence score
 * - Threat indicator
 */
class DetectionLogAdapter(
    private val detections: List<DetectionEvent>
) : RecyclerView.Adapter<DetectionLogAdapter.DetectionViewHolder>() {

    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetectionViewHolder {
        val binding = ItemDetectionLogBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DetectionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DetectionViewHolder, position: Int) {
        holder.bind(detections[position])
    }

    override fun getItemCount() = detections.size

    inner class DetectionViewHolder(
        private val binding: ItemDetectionLogBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(event: DetectionEvent) {
            // Timestamp
            binding.tvTimestamp.text = dateFormat.format(Date(event.timestamp))

            // Detection type
            binding.tvType.text = event.type

            // Confidence percentage
            val percentage = (event.confidence * 100).toInt()
            binding.tvConfidence.text = "$percentage%"

            // Confidence bar
            binding.progressConfidence.progress = percentage

            // Set colors based on confidence
            val (textColor, progressColor, icon) = when {
                event.confidence >= 0.8f -> Triple(
                    android.graphics.Color.parseColor("#10B981"),
                    android.graphics.Color.parseColor("#10B981"),
                    "⚠️"
                )
                event.confidence >= 0.6f -> Triple(
                    android.graphics.Color.parseColor("#F59E0B"),
                    android.graphics.Color.parseColor("#F59E0B"),
                    "⚠️"
                )
                else -> Triple(
                    android.graphics.Color.parseColor("#6B7280"),
                    android.graphics.Color.parseColor("#D1D5DB"),
                    "✓"
                )
            }

            binding.tvConfidence.setTextColor(textColor)
            binding.progressConfidence.progressTintList =
                android.content.res.ColorStateList.valueOf(progressColor)
            binding.tvThreatIcon.text = icon

            // Threat indicator
            if (event.isThreat) {
                binding.cardRoot.setCardBackgroundColor(
                    android.graphics.Color.parseColor("#FEF2F2")
                )
                binding.tvThreatLabel.visibility = android.view.View.VISIBLE
                binding.tvThreatLabel.text = "THREAT"
            } else {
                binding.cardRoot.setCardBackgroundColor(
                    android.graphics.Color.parseColor("#FFFFFF")
                )
                binding.tvThreatLabel.visibility = android.view.View.GONE
            }
        }
    }
}