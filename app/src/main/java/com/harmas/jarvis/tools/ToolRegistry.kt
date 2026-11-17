package com.harmas.jarvis.tools

import java.util.concurrent.ConcurrentHashMap

object ToolRegistry {

    private val tools =
        ConcurrentHashMap<String, Tool>()

    fun register(tool: Tool) {

        require(tool.id.isNotBlank()) {
            "Tool ID cannot be empty"
        }

        tools[tool.id] = tool
    }

    fun get(id: String): Tool? {
        return tools[id]
    }

    fun getAll(): List<Tool> {
        return tools.values.sortedBy { it.id }
    }

    fun unregister(id: String) {
        tools.remove(id)
    }

    fun clear() {
        tools.clear()
    }

    fun contains(id: String): Boolean {
        return tools.containsKey(id)
    }
}
