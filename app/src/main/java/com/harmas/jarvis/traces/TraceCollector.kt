package com.harmas.jarvis.traces

import java.util.UUID
import java.util.concurrent.Executors

class TraceCollector(
    private val database: TraceDatabase
) {

    private val executor =
        Executors.newSingleThreadExecutor()

    fun record(
        component: String,
        operation: String,
        success: Boolean,
        details: String = ""
    ) {

        if (component.isBlank() || operation.isBlank()) {
            return
        }

        val record = TraceRecord(
            id = UUID.randomUUID().toString(),
            component = component,
            operation = operation,
            success = success,
            details = details,
            timestamp = System.currentTimeMillis()
        )

        executor.execute {
            runCatching {
                database.save(record)
            }
        }
    }

    fun getRecent(limit: Int = 50): List<TraceRecord> {
        return database.getRecent(limit)
    }

    fun clear() {
        executor.execute {
            database.deleteAll()
        }
    }

    fun shutdown() {
        executor.shutdown()
    }
}
