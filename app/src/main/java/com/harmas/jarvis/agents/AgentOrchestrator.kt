package com.harmas.jarvis.agents

import com.harmas.jarvis.core.JarvisEvent
import com.harmas.jarvis.core.JarvisEventBus
import com.harmas.jarvis.core.JarvisEventType
import com.harmas.jarvis.tools.ToolRegistry

class AgentOrchestrator {

    suspend fun execute(
        agentId: String,
        context: AgentContext
    ): AgentResult {

        val agent = AgentRegistry.get(agentId)
            ?: return AgentResult(
                reply = "Agent not found: $agentId",
                success = false
            )

        JarvisEventBus.publish(
            JarvisEvent(
                type = JarvisEventType.AGENT_START,
                data = mapOf("agent" to agentId)
            )
        )

        return try {

            val updatedContext = context.copy(
                tools = ToolRegistry.getAll()
            )

            val result = agent.run(updatedContext)

            JarvisEventBus.publish(
                JarvisEvent(
                    type = JarvisEventType.AGENT_END,
                    data = mapOf(
                        "agent" to agentId,
                        "success" to result.success.toString()
                    )
                )
            )

            result

        } catch (exception: Exception) {

            JarvisEventBus.publish(
                JarvisEvent(
                    type = JarvisEventType.ERROR,
                    data = mapOf(
                        "agent" to agentId,
                        "message" to (
                            exception.message ?: "Unknown error"
                        )
                    )
                )
            )

            AgentResult(
                reply = "Agent execution failed: ${
                    exception.message ?: "Unknown error"
                }",
                success = false
            )
        }
    }

    fun getAvailableAgents(): List<String> {
        return AgentRegistry.getAll().map { it.id }
    }
}
