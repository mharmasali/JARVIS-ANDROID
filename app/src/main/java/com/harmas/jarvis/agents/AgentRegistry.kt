package com.harmas.jarvis.agents

import java.util.concurrent.ConcurrentHashMap

object AgentRegistry {

    private val agents =
        ConcurrentHashMap<String, Agent>()

    fun register(agent: Agent) {

        require(agent.id.isNotBlank()) {
            "Agent ID cannot be empty"
        }

        agents[agent.id] = agent
    }

    fun get(id: String): Agent? {
        return agents[id]
    }

    fun getAll(): List<Agent> {
        return agents.values.sortedBy { it.id }
    }

    fun unregister(id: String) {
        agents.remove(id)
    }

    fun clear() {
        agents.clear()
    }

    fun contains(id: String): Boolean {
        return agents.containsKey(id)
    }
}
