package com.harmas.jarvis.tools

data class ToolRequest(
    val arguments: Map<String, String> = emptyMap()
)

sealed interface ToolResult {

    data class Success(
        val output: String
    ) : ToolResult

    data class Failure(
        val error: String
    ) : ToolResult

    data class NeedsConfirmation(
        val description: String
    ) : ToolResult
}

interface JarvisTool {

    val id: String

    val description: String

    val requiresConfirmation: Boolean
        get() = false

    suspend fun execute(
        request: ToolRequest
    ): ToolResult
}

object ToolRegistry {

    private val tools =
        java.util.concurrent.ConcurrentHashMap<String, JarvisTool>()

    fun register(tool: JarvisTool) {
        require(tool.id.isNotBlank())
        tools[tool.id] = tool
    }

    fun get(id: String): JarvisTool? {
        return tools[id]
    }

    fun getAll(): List<JarvisTool> {
        return tools.values.sortedBy { it.id }
    }

    fun unregister(id: String) {
        tools.remove(id)
    }
}
