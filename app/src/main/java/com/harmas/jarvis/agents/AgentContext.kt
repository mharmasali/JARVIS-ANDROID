package com.harmas.jarvis.agents

import com.harmas.jarvis.core.ChatMessage
import com.harmas.jarvis.tools.Tool

data class AgentContext(

    val input: String = "",

    val messages: List<ChatMessage> = emptyList(),

    val memoryContext: String = "",

    val tools: List<Tool> = emptyList(),

    val metadata: Map<String, String> = emptyMap()
)
