package com.harmas.jarvis.intelligence

import com.harmas.jarvis.engine.EngineRegistry
import com.harmas.jarvis.engine.InferenceRequest
import com.harmas.jarvis.engine.InferenceResponse

enum class RoutingMode {
    AUTO,
    LOCAL_ONLY,
    CLOUD_ONLY
}

class AIProviderRouter {

    var routingMode: RoutingMode = RoutingMode.AUTO

    suspend fun generate(
        request: InferenceRequest
    ): InferenceResponse {

        val engines = EngineRegistry.getAll()

        require(engines.isNotEmpty()) {
            "No AI inference engines are registered."
        }

        val selected = when (routingMode) {

            RoutingMode.LOCAL_ONLY ->
                engines.firstOrNull { engine ->
                    engine.supportedModels().any { it.local }
                }

            RoutingMode.CLOUD_ONLY ->
                engines.firstOrNull { engine ->
                    engine.supportedModels().any { !it.local }
                }

            RoutingMode.AUTO -> {

                if (request.modelId != null) {
                    engines.firstOrNull { engine ->
                        engine.supportedModels().any {
                            it.id == request.modelId
                        }
                    }
                } else {
                    engines.firstOrNull { engine ->
                        engine.supportedModels().any { it.local }
                    } ?: engines.firstOrNull()
                }
            }
        }

        val engine = requireNotNull(selected) {
            "No compatible AI engine available for $routingMode"
        }

        return engine.generate(request)
    }
}
