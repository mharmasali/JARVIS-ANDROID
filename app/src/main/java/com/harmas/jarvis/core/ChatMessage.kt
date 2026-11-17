package com.harmas.jarvis.core

import java.util.UUID

enum class MessageRole {
    SYSTEM,
    USER,
    ASSISTANT,
    TOOL
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: MessageRole,
    val content: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val metadata: Map<String, String> = emptyMap()
)
