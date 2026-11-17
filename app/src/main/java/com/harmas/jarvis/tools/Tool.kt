package com.harmas.jarvis.tools

data class ToolRequest(

    val input: String = "",

    val arguments: Map<String, String> = emptyMap(),

    val metadata: Map<String, String> = emptyMap()
)

sealed class ToolResult {

    data class Success(
        val message: String,
        val data: Map<String, String> = emptyMap()
    ) : ToolResult()

    data class Failure(
        val message: String
    ) : ToolResult()

    data class NeedsConfirmation(
        val message: String
    ) : ToolResult()
}

interface Tool {

    val id: String

    val name: String
        get() = id

    val description: String
        get() = ""

    val requiresConfirmation: Boolean
        get() = false

    suspend fun execute(
        request: ToolRequest
    ): ToolResult
}
