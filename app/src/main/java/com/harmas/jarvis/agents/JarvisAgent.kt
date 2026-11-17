package com.harmas.jarvis.agents

import com.harmas.jarvis.core.ChatMessage
import com.harmas.jarvis.tools.JarvisTool
import java.util.concurrent.ConcurrentHashMap

data class AgentContext(
    val messages: List<ChatMessage>,
    val memoryContext: String = "",
    val tools: List<JarvisTool> = emptyList(),
    val modelId: String? = null
)

data class AgentResult(
    val reply: String,
    val usedTools: List<String> = emptyList(),
    val success: Boolean = true
)

interface JarvisAgent {

    val id: String

    val description: String

    suspend fun run(
        context: AgentContext
    ): AgentResult
}

object AgentRegistry {

    private val agents =
        ConcurrentHashMap<String, JarvisAgent>()

    fun register(agent: JarvisAgent) {
        require(agent.id.isNotBlank())
        agents[agent.id] = agent
    }

    fun get(id: String): JarvisAgent? {
        return agents[id]
    }

    fun getAll(): List<JarvisAgent> {
        return agents.values.sortedBy { it.id }
    }

    fun unregister(id: String) {
        agents.remove(id)
    }
}
