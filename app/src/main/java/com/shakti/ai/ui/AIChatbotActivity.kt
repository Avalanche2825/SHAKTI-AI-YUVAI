package com.shakti.ai.ui

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.shakti.ai.databinding.ActivityAiChatbotBinding
import com.shakti.ai.ml.SafetyAIChatbot
import com.google.android.material.chip.Chip
import kotlinx.coroutines.*

/**
 * AI Chatbot Activity - Safety-focused AI Assistant
 *
 * Features:
 * - Context-aware responses about safety, legal rights, emergency procedures
 * - Pre-built knowledge base on domestic violence, harassment, legal sections
 * - Emotional support and guidance
 * - Integration with app features (FIR generation, escape planning, etc.)
 */
class AIChatbotActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAiChatbotBinding
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var aiChatbot: SafetyAIChatbot
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAiChatbotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupChat()
        setupSuggestions()
        initializeAI()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupChat() {
        chatAdapter = ChatAdapter(messages)
        binding.recyclerChat.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(this@AIChatbotActivity)
        }

        binding.btnSend.setOnClickListener {
            sendMessage()
        }

        binding.etMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else false
        }
    }

    private fun setupSuggestions() {
        val suggestions = listOf(
            "What are my legal rights?",
            "How to file an FIR?",
            "Emergency escape plan",
            "IPC sections for harassment",
            "How to stay safe at home?",
            "Emotional support resources"
        )

        suggestions.forEach { suggestion ->
            val chip = Chip(this).apply {
                text = suggestion
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    binding.etMessage.setText(suggestion)
                    sendMessage()
                }
            }
            binding.layoutSuggestions.addView(chip)
        }
    }

    private fun initializeAI() {
        aiChatbot = SafetyAIChatbot()

        // Send welcome message
        scope.launch {
            delay(500)
            addAiMessage(
                "Hi! I'm your SHAKTI AI Assistant. I'm here to help you with:\n\n" +
                        "• Legal advice and rights\n" +
                        "• Emergency procedures\n" +
                        "• Safety tips\n" +
                        "• Emotional support\n\n" +
                        "What would you like to know?"
            )
        }
    }

    private fun sendMessage() {
        val message = binding.etMessage.text.toString().trim()
        if (message.isEmpty()) return

        // Add user message
        addUserMessage(message)
        binding.etMessage.text.clear()

        // Hide empty state
        binding.layoutEmpty.visibility = View.GONE

        // Show loading
        binding.progressLoading.visibility = View.VISIBLE

        // Get AI response
        scope.launch {
            delay(800) // Simulate processing time
            val response = aiChatbot.getResponse(message)
            binding.progressLoading.visibility = View.GONE
            addAiMessage(response)
        }
    }

    private fun addUserMessage(text: String) {
        messages.add(ChatMessage(text, isUser = true))
        chatAdapter.notifyItemInserted(messages.size - 1)
        binding.recyclerChat.scrollToPosition(messages.size - 1)
    }

    private fun addAiMessage(text: String) {
        messages.add(ChatMessage(text, isUser = false))
        chatAdapter.notifyItemInserted(messages.size - 1)
        binding.recyclerChat.scrollToPosition(messages.size - 1)
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}

/**
 * Chat Message data class
 */
data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Chat Adapter for RecyclerView
 */
class ChatAdapter(private val messages: List<ChatMessage>) :
    androidx.recyclerview.widget.RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(val binding: com.shakti.ai.databinding.ItemChatMessageBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ChatViewHolder {
        val binding = com.shakti.ai.databinding.ItemChatMessageBinding.inflate(
            android.view.LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]

        if (message.isUser) {
            holder.binding.layoutUserMessage.visibility = View.VISIBLE
            holder.binding.layoutAiMessage.visibility = View.GONE
            holder.binding.tvUserMessage.text = message.text
        } else {
            holder.binding.layoutUserMessage.visibility = View.GONE
            holder.binding.layoutAiMessage.visibility = View.VISIBLE
            holder.binding.tvAiMessage.text = message.text
        }
    }

    override fun getItemCount() = messages.size
}
