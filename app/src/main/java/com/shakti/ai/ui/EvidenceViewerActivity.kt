package com.shakti.ai.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.shakti.ai.R
import com.shakti.ai.data.EvidenceDatabase
import com.shakti.ai.data.EvidenceItem
import com.shakti.ai.databinding.ActivityEvidenceViewerBinding
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Evidence Viewer Activity - Display and play recorded evidence
 */
class EvidenceViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEvidenceViewerBinding
    private lateinit var database: EvidenceDatabase
    private val evidenceList = mutableListOf<EvidenceItem>()
    private lateinit var adapter: EvidenceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEvidenceViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = EvidenceDatabase.getDatabase(this)

        setupToolbar()
        setupRecyclerView()
        loadEvidence()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Recorded Evidence"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = EvidenceAdapter(evidenceList) { evidence ->
            playEvidence(evidence)
        }
        binding.recyclerEvidence.apply {
            layoutManager = LinearLayoutManager(this@EvidenceViewerActivity)
            adapter = this@EvidenceViewerActivity.adapter
        }
    }

    private fun loadEvidence() {
        val incidentId = intent.getStringExtra("incident_id")

        lifecycleScope.launch {
            val evidence = if (incidentId != null) {
                database.evidenceDao().getEvidenceForIncident(incidentId)
            } else {
                database.evidenceDao().getAllEvidence()
            }

            evidenceList.clear()
            evidenceList.addAll(evidence)
            adapter.notifyDataSetChanged()

            if (evidence.isEmpty()) {
                binding.tvNoData.visibility = android.view.View.VISIBLE
                binding.recyclerEvidence.visibility = android.view.View.GONE
            } else {
                binding.tvNoData.visibility = android.view.View.GONE
                binding.recyclerEvidence.visibility = android.view.View.VISIBLE
            }
        }
    }

    private fun playEvidence(evidence: EvidenceItem) {
        val file = File(evidence.filePath)
        if (!file.exists()) {
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show()
            return
        }

        // Open with system player
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
 * RecyclerView Adapter for Evidence
 */
class EvidenceAdapter(
    private val items: List<EvidenceItem>,
    private val onItemClick: (EvidenceItem) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<EvidenceAdapter.ViewHolder>() {

    class ViewHolder(val binding: com.shakti.ai.databinding.ItemEvidenceBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val binding = com.shakti.ai.databinding.ItemEvidenceBinding.inflate(
            android.view.LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
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
            val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.getDefault())
            tvTimestamp.text = dateFormat.format(Date(item.timestamp))

            // Format duration
            if (item.duration > 0) {
                val seconds = item.duration / 1000
                val minutes = seconds / 60
                val remainingSeconds = seconds % 60
                tvDuration.text = String.format("%02d:%02d", minutes, remainingSeconds)
            } else {
                tvDuration.text = "Unknown duration"
            }

            // Format file size
            tvFileSize.text = formatFileSize(item.fileSize)

            root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun getItemCount() = items.size

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${bytes / (1024 * 1024)} MB"
        }
    }
}
