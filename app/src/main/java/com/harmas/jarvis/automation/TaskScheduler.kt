package com.harmas.jarvis.automation

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

data class ScheduledTask(
    val id: String,
    val title: String,
    val delayMillis: Long,
    val createdAt: Long
)

class TaskScheduler {

    private val executor =
        Executors.newSingleThreadScheduledExecutor()

    private val tasks =
        ConcurrentHashMap<String, ScheduledFuture<*>>()

    fun schedule(
        title: String,
        delayMillis: Long,
        callback: () -> Unit
    ): ScheduledTask {

        require(title.isNotBlank())
        require(delayMillis >= 0)

        val id = UUID.randomUUID().toString()

        val task = ScheduledTask(
            id = id,
            title = title,
            delayMillis = delayMillis,
            createdAt = System.currentTimeMillis()
        )

        val future = executor.schedule(
            {
                try {
                    callback()
                } finally {
                    tasks.remove(id)
                }
            },
            delayMillis,
            TimeUnit.MILLISECONDS
        )

        tasks[id] = future

        return task
    }

    fun cancel(id: String): Boolean {

        val future = tasks.remove(id)
            ?: return false

        return future.cancel(false)
    }

    fun cancelAll() {

        tasks.values.forEach {
            it.cancel(false)
        }

        tasks.clear()
    }

    fun getActiveTaskCount(): Int {
        return tasks.size
    }

    fun shutdown() {
        cancelAll()
        executor.shutdownNow()
    }
}
