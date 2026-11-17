package com.harmas.jarvis.engine

import com.harmas.jarvis.core.ChatMessage
import com.harmas.jarvis.intelligence.ModelSpec
import java.util.concurrent.ConcurrentHashMap

data class GenerationConfig(
    val temperature: Float = 0.7f,
    val maxTokens: Int = 512,
    val topP: Float = 0.9f
)

data class InferenceRequest(
    val messages: List<ChatMessage>,
    val modelId: String? = null,
    val config: GenerationConfig = GenerationConfig()
)

data class InferenceResponse(
    val content: String,
    val modelId: String,
    val inputTokens: Int = 0,
    val outputTokens: Int = 0
)

data class EngineStatus(
    val engineId: String,
    val available: Boolean,
    val message: String = ""
)

interface InferenceEngine {

    val id: String

    suspend fun generate(
        request: InferenceRequest
    ): InferenceResponse

    suspend fun healthCheck(): EngineStatus

    fun supportedModels(): List<ModelSpec>
}

object EngineRegistry {

    private val engines =
        ConcurrentHashMap<String, InferenceEngine>()

    fun register(engine: InferenceEngine) {
        require(engine.id.isNotBlank())
        engines[engine.id] = engine
    }

    fun get(id: String): InferenceEngine? {
        return engines[id]
    }

    fun getAll(): List<InferenceEngine> {
        return engines.values.sortedBy { it.id }
    }

    fun unregister(id: String) {
        engines.remove(id)
    }
}
