package com.harmas.jarvis.intelligence

import com.harmas.jarvis.engine.EngineRegistry

object ModelSelector {

    fun selectLocal(): ModelSpec? {
        return EngineRegistry.getAll()
            .flatMap { it.supportedModels() }
            .firstOrNull { it.local }
    }

    fun selectCloud(): ModelSpec? {
        return EngineRegistry.getAll()
            .flatMap { it.supportedModels() }
            .firstOrNull { !it.local }
    }

    fun selectById(id: String): ModelSpec? {
        return EngineRegistry.getAll()
            .flatMap { it.supportedModels() }
            .firstOrNull { it.id == id }
    }

    fun getAvailableModels(): List<ModelSpec> {
        return EngineRegistry.getAll()
            .flatMap { it.supportedModels() }
            .distinctBy { it.id }
    }
}
