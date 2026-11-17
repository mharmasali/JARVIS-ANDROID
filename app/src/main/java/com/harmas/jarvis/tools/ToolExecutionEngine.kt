package com.harmas.jarvis.tools

import com.harmas.jarvis.core.JarvisEvent
import com.harmas.jarvis.core.JarvisEventBus
import com.harmas.jarvis.core.JarvisEventType

class ToolExecutionEngine {

    suspend fun execute(
        toolId: String,
        request: ToolRequest,
        confirmed: Boolean = false
    ): ToolResult {

        val tool = ToolRegistry.get(toolId)
            ?: return ToolResult.Failure(
                "Tool not found: $toolId"
            )

        if (tool.requiresConfirmation && !confirmed) {

            return ToolResult.NeedsConfirmation(
                "Confirmation required before executing: ${tool.id}"
            )
        }

        JarvisEventBus.publish(
            JarvisEvent(
                type = JarvisEventType.TOOL_CALL_START,
                data = mapOf("tool" to toolId)
            )
        )

        return try {

            val result = tool.execute(request)

            JarvisEventBus.publish(
                JarvisEvent(
                    type = JarvisEventType.TOOL_CALL_END,
                    data = mapOf(
                        "tool" to toolId,
                        "success" to (
                            result is ToolResult.Success
                        ).toString()
                    )
                )
            )

            result

        } catch (exception: Exception) {

            JarvisEventBus.publish(
                JarvisEvent(
                    type = JarvisEventType.ERROR,
                    data = mapOf(
                        "tool" to toolId,
                        "message" to (
                            exception.message ?: "Unknown error"
                        )
                    )
                )
            )

            ToolResult.Failure(
                exception.message ?: "Tool execution failed"
            )
        }
    }
}
