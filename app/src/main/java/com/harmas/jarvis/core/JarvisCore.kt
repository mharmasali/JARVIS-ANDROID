package com.harmas.jarvis.core

import android.content.Context
import com.harmas.jarvis.agents.AgentOrchestrator
import com.harmas.jarvis.engine.EngineRegistry
import com.harmas.jarvis.intelligence.AIProviderRouter
import com.harmas.jarvis.memory.ConversationRepository
import com.harmas.jarvis.memory.JarvisMemoryDatabase
import com.harmas.jarvis.memory.MemoryManager
import com.harmas.jarvis.memory.MemoryRepository
import com.harmas.jarvis.tools.ToolExecutionEngine

class JarvisCore private constructor(context: Context) {

    private val database = JarvisMemoryDatabase(
        context.applicationContext
    )

    private val memoryRepository = MemoryRepository(database)

    val memoryManager = MemoryManager(memoryRepository)

    val conversationRepository =
        ConversationRepository(database)

    val aiRouter = AIProviderRouter()

    val agentOrchestrator = AgentOrchestrator()

    val toolExecutor = ToolExecutionEngine()

    fun getStatus(): Map<String, Any> {
        return mapOf(
            "name" to "J.A.R.V.I.S",
            "version" to "1.0",
            "memory" to true,
            "registeredEngines" to EngineRegistry.getAll().size,
            "registeredAgents" to
                agentOrchestrator.getAvailableAgents().size
        )
    }

    fun close() {
        database.close()
    }

    companion object {

        @Volatile
        private var instance: JarvisCore? = null

        fun getInstance(context: Context): JarvisCore {

            return instance ?: synchronized(this) {

                instance ?: JarvisCore(
                    context.applicationContext
                ).also {
                    instance = it
                }
            }
        }
    }
}
