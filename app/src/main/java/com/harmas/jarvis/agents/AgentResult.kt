package com.harmas.jarvis.agents

data class AgentResult(

    val reply: String,

    val success: Boolean = true,

    val data: Map<String, String> = emptyMap(),

    val error: String? = null
)
