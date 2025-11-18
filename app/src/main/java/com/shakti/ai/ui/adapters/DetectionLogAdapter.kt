package com.shakti.ai.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shakti.ai.databinding.ItemDetectionLogBinding
import com.shakti.ai.ui.DetectionEvent
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter for Detection Log in AI Monitoring Dashboard
 */
class DetectionLogAdapter(
    private val detectionLog: List<DetectionEvent>
) : RecyclerView.Adapter<DetectionLogAdapter.DetectionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetectionViewHolder {
        val binding = ItemDetectionLogBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DetectionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DetectionViewHolder, position: Int) {
        holder.bind(detectionLog[position])
    }

    override fun getItemCount(): Int = detectionLog.size

    class DetectionViewHolder(
        private val binding: ItemDetectionLogBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        fun bind(event: DetectionEvent) {
            // Time
            binding.tvTime.text = timeFormat.format(Date(event.timestamp))

            // Detection type
            binding.tvType.text = event.type

            // Confidence
            val confidenceText = "${(event.confidence * 100).toInt()}%"
            binding.tvConfidence.text = confidenceText

            // Status icon and color
            if (event.isThreat) {
                binding.ivStatus.setImageResource(android.R.drawable.ic_dialog_alert)
                binding.ivStatus.setColorFilter(Color.parseColor("#EF4444")) // Red
                binding.root.setBackgroundColor(Color.parseColor("#FEF2F2")) // Light red bg
            } else {
                binding.ivStatus.setImageResource(android.R.drawable.ic_dialog_info)
                binding.ivStatus.setColorFilter(Color.parseColor("#10B981")) // Green
                binding.root.setBackgroundColor(Color.parseColor("#F0FDF4")) // Light green bg
            }

            // Confidence color
            binding.tvConfidence.setTextColor(
                when {
                    event.confidence >= 0.8f -> Color.parseColor("#10B981")
                    event.confidence >= 0.6f -> Color.parseColor("#F59E0B")
                    else -> Color.parseColor("#EF4444")
                }
            )
        }
    }
}
