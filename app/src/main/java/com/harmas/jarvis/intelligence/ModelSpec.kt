package com.harmas.jarvis.intelligence

import java.util.concurrent.ConcurrentHashMap

data class ModelSpec(
    val id: String,
    val name: String,
    val provider: String,
    val parameterCount: String = "Unknown",
    val local: Boolean = false,
    val quantization: String? = null,
    val capabilities: Set<String> = emptySet()
)

object ModelRegistry {

    private val models =
        ConcurrentHashMap<String, ModelSpec>()

    fun register(model: ModelSpec) {
        require(model.id.isNotBlank())
        models[model.id] = model
    }

    fun get(id: String): ModelSpec? {
        return models[id]
    }

    fun getAll(): List<ModelSpec> {
        return models.values.sortedBy { it.id }
    }

    fun getLocalModels(): List<ModelSpec> {
        return getAll().filter { it.local }
    }

    fun getCloudModels(): List<ModelSpec> {
        return getAll().filter { !it.local }
    }

    fun remove(id: String) {
        models.remove(id)
    }

    fun clear() {
        models.clear()
    }
}
