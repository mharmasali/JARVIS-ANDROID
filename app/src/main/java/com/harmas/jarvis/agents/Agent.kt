package com.harmas.jarvis.agents

interface Agent {

    val id: String

    val name: String
        get() = id

    val description: String
        get() = ""

    suspend fun run(
        context: AgentContext
    ): AgentResult
}
