package com.shakti.ai.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.shakti.ai.databinding.FragmentAiChatBinding
import com.shakti.ai.models.ChatContext
import com.shakti.ai.models.ChatMessage
import com.shakti.ai.services.AIChatService
import com.shakti.ai.utils.VoiceInputHelper
import kotlinx.coroutines.launch

/**
 * AI Chat Fragment - Conversational AI assistant
 */
class AIChatFragment : Fragment() {

    private var _binding: FragmentAiChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var chatService: AIChatService
    private lateinit var voiceInputHelper: VoiceInputHelper
    private lateinit var adapter: ChatMessageAdapter

    private val messages = mutableListOf<ChatMessage>()
    private var chatContext: ChatContext = ChatContext.GENERAL
    private var isSpeechEnabled = false

    companion object {
        private const val ARG_CONTEXT = "chat_context"
        private const val REQUEST_RECORD_AUDIO = 101

        fun newInstance(context: ChatContext): AIChatFragment {
            return AIChatFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CONTEXT, context.name)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get context from arguments
        arguments?.getString(ARG_CONTEXT)?.let {
            chatContext = ChatContext.valueOf(it)
        }

        // Initialize services
        chatService = AIChatService(requireContext())
        voiceInputHelper = VoiceInputHelper(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAiChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupInputArea()
        initializeServices()
        showWelcomeMessage()
    }

    private fun setupToolbar() {
        val title = when (chatContext) {
            ChatContext.LEGAL -> "SHAKTI Legal Assistant"
            ChatContext.ESCAPE -> "SHAKTI Escape Planner"
            ChatContext.GENERAL -> "SHAKTI AI Assistant"
        }
        binding.tvTitle.text = title

        binding.btnClose.setOnClickListener {
            parentFragmentManager.beginTransaction().remove(this).commit()
        }

        binding.btnClear.setOnClickListener {
            clearChat()
        }

        binding.btnSpeech.setOnClickListener {
            toggleSpeech()
        }
    }

    private fun setupRecyclerView() {
        adapter = ChatMessageAdapter(messages)
        binding.recyclerChat.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AIChatFragment.adapter
        }
    }

    private fun setupInputArea() {
        // Send button
        binding.btnSend.setOnClickListener {
            sendMessage()
        }

        // Voice button
        binding.btnVoice.setOnClickListener {
            startVoiceInput()
        }

        // Enter key listener
        binding.etMessage.setOnEditorActionListener { _, _, _ ->
            sendMessage()
            true
        }
    }

    private fun initializeServices() {
        lifecycleScope.launch {
            try {
                chatService.initialize()
                voiceInputHelper.initialize()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to initialize AI", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun showWelcomeMessage() {
        val welcomeText = when (chatContext) {
            ChatContext.LEGAL -> """
                Welcome! I'm your Legal AI Assistant 👨‍⚖️
                
                I can help you with:
                • Filing FIR
                • IPC sections
                • Legal rights
                • Finding lawyers
                • Evidence guidelines
                
                Ask me anything!
            """.trimIndent()

            ChatContext.ESCAPE -> """
                Welcome! I'm your Escape Planning Assistant 🏠
                
                I can help you with:
                • Financial planning
                • Safe houses
                • Planning with children
                • Finding employment
                • Important documents
                
                How can I help you plan?
            """.trimIndent()

            ChatContext.GENERAL -> """
                Welcome! I'm SHAKTI AI Assistant 🛡️
                
                I'm here to help with:
                • Legal matters
                • Escape planning
                • Emergency assistance
                • App features
                
                What would you like to know?
            """.trimIndent()
        }

        addMessage(ChatMessage(text = welcomeText, isUser = false))
    }

    private fun sendMessage() {
        val text = binding.etMessage.text.toString().trim()
        if (text.isEmpty()) return

        // Clear input
        binding.etMessage.text?.clear()

        // Add user message
        addMessage(ChatMessage(text = text, isUser = true))

        // Show typing indicator
        binding.progressTyping.visibility = View.VISIBLE

        // Generate AI response
        lifecycleScope.launch {
            try {
                val response = chatService.generateResponse(text, chatContext)

                // Hide typing indicator
                binding.progressTyping.visibility = View.GONE

                // Add AI response
                addMessage(ChatMessage(text = response, isUser = false))

                // Speak response if enabled
                if (isSpeechEnabled) {
                    chatService.speakResponse(response)
                }
            } catch (e: Exception) {
                binding.progressTyping.visibility = View.GONE
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startVoiceInput() {
        // Check permission
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO
            )
            return
        }

        // Start listening
        binding.btnVoice.isEnabled = false
        binding.tvListening.visibility = View.VISIBLE

        voiceInputHelper.startListening(
            onResult = { text ->
                binding.btnVoice.isEnabled = true
                binding.tvListening.visibility = View.GONE

                if (text.isNotEmpty()) {
                    binding.etMessage.setText(text)
                    sendMessage()
                }
            },
            onError = { error ->
                binding.btnVoice.isEnabled = true
                binding.tvListening.visibility = View.GONE
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            },
            onPartialResult = { partial ->
                binding.etMessage.setText(partial)
            }
        )
    }

    private fun toggleSpeech() {
        isSpeechEnabled = !isSpeechEnabled

        if (isSpeechEnabled) {
            binding.btnSpeech.setImageResource(android.R.drawable.ic_lock_silent_mode_off)
            Toast.makeText(requireContext(), "Speech enabled", Toast.LENGTH_SHORT).show()
        } else {
            binding.btnSpeech.setImageResource(android.R.drawable.ic_lock_silent_mode)
            chatService.stopSpeaking()
            Toast.makeText(requireContext(), "Speech disabled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearChat() {
        messages.clear()
        adapter.notifyDataSetChanged()
        chatService.clearHistory()
        showWelcomeMessage()
    }

    private fun addMessage(message: ChatMessage) {
        messages.add(message)
        adapter.notifyItemInserted(messages.size - 1)
        binding.recyclerChat.smoothScrollToPosition(messages.size - 1)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_RECORD_AUDIO) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVoiceInput()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Microphone permission required",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        voiceInputHelper.destroy()
        chatService.shutdown()
        _binding = null
    }
}

/**
 * Chat Message Adapter
 */
class ChatMessageAdapter(
    private val messages: List<ChatMessage>
) : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_AI = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) VIEW_TYPE_USER else VIEW_TYPE_AI
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return if (viewType == VIEW_TYPE_USER) {
            val binding = com.shakti.ai.databinding.ItemChatMessageUserBinding.inflate(
                inflater,
                parent,
                false
            )
            UserMessageViewHolder(binding)
        } else {
            val binding =
                com.shakti.ai.databinding.ItemChatMessageAiBinding.inflate(inflater, parent, false)
            AIMessageViewHolder(binding)
        }
    }

    override fun onBindViewHolder(
        holder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
        position: Int
    ) {
        val message = messages[position]

        when (holder) {
            is UserMessageViewHolder -> holder.bind(message)
            is AIMessageViewHolder -> holder.bind(message)
        }
    }

    override fun getItemCount() = messages.size

    class UserMessageViewHolder(
        private val binding: com.shakti.ai.databinding.ItemChatMessageUserBinding
    ) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            binding.tvMessage.text = message.text

            val dateFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
            binding.tvTime.text = dateFormat.format(java.util.Date(message.timestamp))
        }
    }

    class AIMessageViewHolder(
        private val binding: com.shakti.ai.databinding.ItemChatMessageAiBinding
    ) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            binding.tvMessage.text = message.text

            val dateFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
            binding.tvTime.text = dateFormat.format(java.util.Date(message.timestamp))
        }
    }
}
