package com.harmas.jarvis.learning

import java.util.UUID

data class TraceStep(
    val id: String = UUID.randomUUID().toString(),
    val component: String,
    val operation: String,
    val success: Boolean,
    val durationMillis: Long = 0,
    val details: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class InteractionTrace(
    val id: String = UUID.randomUUID().toString(),
    val userQuery: String,
    val finalResponse: String = "",
    val steps: List<TraceStep> = emptyList(),
    val successful: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

interface TraceStore {

    suspend fun save(trace: InteractionTrace)

    suspend fun getRecent(limit: Int): List<InteractionTrace>
}
