package com.harmas.jarvis.engine

import com.harmas.jarvis.BuildConfig
import com.harmas.jarvis.intelligence.ModelRegistry
import com.harmas.jarvis.network.ServerConfig

object EngineBootstrap {

    fun initialize(): Boolean {

        val config = ServerConfig(
            baseUrl = BuildConfig.JARVIS_AI_BASE_URL,
            apiKey = BuildConfig.JARVIS_AI_API_KEY,
            model = BuildConfig.JARVIS_AI_MODEL
        )

        if (!config.isValid() || config.model.isBlank()) {
            return false
        }

        val engine = OpenAICompatibleEngine(
            baseUrl = config.baseUrl,
            apiKey = config.apiKey,
            defaultModel = config.model
        )

        if (EngineRegistry.get(engine.id) == null) {
            EngineRegistry.register(engine)
        }

        engine.supportedModels().forEach { model ->
            ModelRegistry.register(model)
        }

        return true
    }
}
