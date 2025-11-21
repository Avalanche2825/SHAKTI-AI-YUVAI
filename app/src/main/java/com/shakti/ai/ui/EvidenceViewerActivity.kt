package com.shakti.ai.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.shakti.ai.R
import com.shakti.ai.data.EvidenceDatabase
import com.shakti.ai.data.EvidenceItem
import com.shakti.ai.data.IncidentRecord
import com.shakti.ai.databinding.ActivityEvidenceViewerBinding
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Evidence Viewer Activity - Display and play ALL recorded evidence
 */
class EvidenceViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEvidenceViewerBinding
    private lateinit var database: EvidenceDatabase
    private val groupedEvidence = mutableListOf<EvidenceGroup>()
    private lateinit var adapter: EvidenceGroupAdapter
    private var showAllEvidence = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEvidenceViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = EvidenceDatabase.getDatabase(this)

        // Check if specific incident ID is provided
        val incidentId = intent.getStringExtra("incident_id")
        showAllEvidence = incidentId == null

        setupToolbar()
        setupRecyclerView()
        setupFilterButtons()
        loadEvidence()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = if (showAllEvidence) "All Evidence" else "Incident Evidence"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = EvidenceGroupAdapter(groupedEvidence) { evidence ->
            playEvidence(evidence)
        }
        binding.recyclerEvidence.apply {
            layoutManager = LinearLayoutManager(this@EvidenceViewerActivity)
            adapter = this@EvidenceViewerActivity.adapter
        }
    }

    private fun setupFilterButtons() {
        // Show All button
        binding.chipAll.setOnClickListener {
            filterEvidence(null)
        }

        // Show Videos button
        binding.chipVideos.setOnClickListener {
            filterEvidence("video")
        }

        // Show Audio button
        binding.chipAudio.setOnClickListener {
            filterEvidence("audio")
        }

        // Default: Show All
        binding.chipAll.isChecked = true
    }

    private fun loadEvidence() {
        val incidentId = intent.getStringExtra("incident_id")

        lifecycleScope.launch {
            try {
                // Load ALL evidence with incidents
                val incidents = if (incidentId != null) {
                    listOfNotNull(database.incidentDao().getIncidentById(incidentId))
                } else {
                    database.incidentDao().getAllIncidents()
                }

                val groups = mutableListOf<EvidenceGroup>()

                for (incident in incidents) {
                    val evidenceList = database.evidenceDao().getEvidenceForIncident(incident.id)
                    if (evidenceList.isNotEmpty()) {
                        groups.add(EvidenceGroup(incident, evidenceList))
                    }
                }

                runOnUiThread {
                    groupedEvidence.clear()
                    groupedEvidence.addAll(groups)
                    adapter.notifyDataSetChanged()

                    // Update stats
                    val totalEvidence = groups.sumOf { it.evidenceList.size }
                    binding.tvTotalEvidence.text =
                        "$totalEvidence files from ${groups.size} incidents"

                    if (groups.isEmpty()) {
                        binding.tvNoData.visibility = View.VISIBLE
                        binding.recyclerEvidence.visibility = View.GONE
                        binding.layoutStats.visibility = View.GONE
                    } else {
                        binding.tvNoData.visibility = View.GONE
                        binding.recyclerEvidence.visibility = View.VISIBLE
                        binding.layoutStats.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(
                        this@EvidenceViewerActivity,
                        "Error loading evidence: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun filterEvidence(type: String?) {
        lifecycleScope.launch {
            val incidentId = intent.getStringExtra("incident_id")

            val incidents = if (incidentId != null) {
                listOfNotNull(database.incidentDao().getIncidentById(incidentId))
            } else {
                database.incidentDao().getAllIncidents()
            }

            val groups = mutableListOf<EvidenceGroup>()

            for (incident in incidents) {
                val allEvidence = database.evidenceDao().getEvidenceForIncident(incident.id)
                val filteredEvidence = if (type != null) {
                    allEvidence.filter { it.type.contains(type) }
                } else {
                    allEvidence
                }

                if (filteredEvidence.isNotEmpty()) {
                    groups.add(EvidenceGroup(incident, filteredEvidence))
                }
            }

            runOnUiThread {
                groupedEvidence.clear()
                groupedEvidence.addAll(groups)
                adapter.notifyDataSetChanged()

                val totalEvidence = groups.sumOf { it.evidenceList.size }
                binding.tvTotalEvidence.text = "$totalEvidence files from ${groups.size} incidents"
            }
        }
    }

    private fun playEvidence(evidence: EvidenceItem) {
        val file = File(evidence.filePath)
        if (!file.exists()) {
            Toast.makeText(this, "File not found: ${evidence.filePath}", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val intent = Intent(Intent.ACTION_VIEW)
            val uri = androidx.core.content.FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )
            intent.setDataAndType(uri, getMimeType(evidence.type))
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Cannot open file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getMimeType(type: String): String {
        return when {
            type.startsWith("video") -> "video/*"
            type.startsWith("audio") -> "audio/*"
            else -> "*/*"
        }
    }
}

/**
 * Evidence Group - Groups evidence by incident
 */
data class EvidenceGroup(
    val incident: IncidentRecord,
    val evidenceList: List<EvidenceItem>
)

/**
 * RecyclerView Adapter for Grouped Evidence
 */
class EvidenceGroupAdapter(
    private val groups: List<EvidenceGroup>,
    private val onItemClick: (EvidenceItem) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_HEADER = 0
        const val VIEW_TYPE_ITEM = 1
    }

    private val flatList = mutableListOf<Any>()

    init {
        updateFlatList()
    }

    private fun updateFlatList() {
        flatList.clear()
        groups.forEach { group ->
            flatList.add(group) // Header
            flatList.addAll(group.evidenceList) // Items
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (flatList[position]) {
            is EvidenceGroup -> VIEW_TYPE_HEADER
            else -> VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(
        parent: android.view.ViewGroup,
        viewType: Int
    ): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val binding = com.shakti.ai.databinding.ItemEvidenceHeaderBinding.inflate(
                android.view.LayoutInflater.from(parent.context),
                parent,
                false
            )
            HeaderViewHolder(binding)
        } else {
            val binding = com.shakti.ai.databinding.ItemEvidenceBinding.inflate(
                android.view.LayoutInflater.from(parent.context),
                parent,
                false
            )
            ItemViewHolder(binding)
        }
    }

    override fun onBindViewHolder(
        holder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
        position: Int
    ) {
        when (holder) {
            is HeaderViewHolder -> bindHeader(holder, flatList[position] as EvidenceGroup)
            is ItemViewHolder -> bindItem(holder, flatList[position] as EvidenceItem)
        }
    }

    private fun bindHeader(holder: HeaderViewHolder, group: EvidenceGroup) {
        with(holder.binding) {
            val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            tvIncidentTime.text = dateFormat.format(Date(group.incident.startTime))

            tvTriggerType.text = when (group.incident.triggerType) {
                "voice_command" -> "Voice Command"
                "manual_sos" -> "Manual SOS"
                "ai_detection" -> "AI Detection"
                else -> "Unknown"
            }

            tvEvidenceCount.text = "${group.evidenceList.size} files"

            // Show location if available
            if (group.incident.latitude != 0.0 && group.incident.longitude != 0.0) {
                tvLocation.text = "${group.incident.latitude}, ${group.incident.longitude}"
                tvLocation.visibility = View.VISIBLE
            } else {
                tvLocation.visibility = View.GONE
            }
        }
    }

    private fun bindItem(holder: ItemViewHolder, item: EvidenceItem) {
        with(holder.binding) {
            // Set icon based on type
            val iconRes = when {
                item.type.contains("video") -> R.drawable.ic_video
                item.type.contains("audio") -> R.drawable.ic_mic
                else -> R.drawable.ic_file
            }
            ivIcon.setImageResource(iconRes)

            // Set title
            tvTitle.text = when (item.type) {
                "video_front" -> "Front Camera Video"
                "video_back" -> "Back Camera Video"
                "audio" -> "Audio Recording"
                else -> "Evidence File"
            }

            // Format timestamp
            val dateFormat = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
            tvTimestamp.text = dateFormat.format(Date(item.timestamp))

            // Format duration
            if (item.duration > 0) {
                val seconds = item.duration / 1000
                val minutes = seconds / 60
                val remainingSeconds = seconds % 60
                tvDuration.text = String.format("%02d:%02d", minutes, remainingSeconds)
                tvDuration.visibility = View.VISIBLE
            } else {
                tvDuration.visibility = View.GONE
            }

            // Format file size
            tvFileSize.text = formatFileSize(item.fileSize)

            root.setOnClickListener { onItemClick(item) }
            btnPlay.setOnClickListener { onItemClick(item) }
        }
    }

    override fun getItemCount() = flatList.size

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes == 0L -> "Unknown"
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        }
    }

    class HeaderViewHolder(val binding: com.shakti.ai.databinding.ItemEvidenceHeaderBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)

    class ItemViewHolder(val binding: com.shakti.ai.databinding.ItemEvidenceBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)
}
