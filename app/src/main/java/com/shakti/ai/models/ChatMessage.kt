package com.shakti.ai.models

/**
 * Chat Message Model
 */
data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isVoice: Boolean = false
)

/**
 * Chat Context - Determines which domain-specific responses to use
 */
enum class ChatContext {
    LEGAL,      // NYAY Legal Assistant - Legal advice, FIR, IPC sections
    ESCAPE,     // Escape Planner - Financial planning, safe houses, resources
    GENERAL     // General women safety assistant
}
