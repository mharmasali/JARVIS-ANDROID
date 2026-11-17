package com.harmas.jarvis.core

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

enum class JarvisEventType {
    INFERENCE_START,
    INFERENCE_END,
    TOOL_CALL_START,
    TOOL_CALL_END,
    MEMORY_STORE,
    MEMORY_RETRIEVE,
    AGENT_START,
    AGENT_END,
    TRACE_COMPLETE,
    ERROR
}

data class JarvisEvent(
    val type: JarvisEventType,
    val timestamp: Long = System.currentTimeMillis(),
    val data: Map<String, String> = emptyMap()
)

object JarvisEventBus {

    private val listeners =
        ConcurrentHashMap<JarvisEventType,
            CopyOnWriteArrayList<(JarvisEvent) -> Unit>>()

    fun subscribe(
        type: JarvisEventType,
        listener: (JarvisEvent) -> Unit
    ): AutoCloseable {

        val callbacks = listeners.getOrPut(type) {
            CopyOnWriteArrayList()
        }

        callbacks.add(listener)

        return AutoCloseable {
            callbacks.remove(listener)
        }
    }

    fun publish(event: JarvisEvent) {
        listeners[event.type]?.forEach { listener ->
            runCatching {
                listener(event)
            }
        }
    }

    fun clear() {
        listeners.clear()
    }
}
