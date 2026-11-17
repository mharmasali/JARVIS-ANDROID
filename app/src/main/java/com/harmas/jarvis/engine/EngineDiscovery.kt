package com.harmas.jarvis.engine

data class EngineDiscoveryResult(
    val engineId: String,
    val available: Boolean,
    val message: String
)

object EngineDiscovery {

    suspend fun discover(): List<EngineDiscoveryResult> {

        return EngineRegistry.getAll().map { engine ->

            try {
                val result = engine.healthCheck()

                EngineDiscoveryResult(
                    engineId = result.engineId,
                    available = result.available,
                    message = result.message
                )

            } catch (exception: Exception) {

                EngineDiscoveryResult(
                    engineId = engine.id,
                    available = false,
                    message = exception.message ?: "Health check failed"
                )
            }
        }
    }
}
