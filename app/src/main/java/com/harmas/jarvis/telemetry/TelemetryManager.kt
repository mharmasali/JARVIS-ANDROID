package com.harmas.jarvis.telemetry

import java.util.concurrent.atomic.AtomicLong

data class TelemetrySnapshot(
    val totalRequests: Long,
    val successfulRequests: Long,
    val failedRequests: Long,
    val averageResponseTimeMillis: Long
)

class TelemetryManager {

    private val totalRequests = AtomicLong(0)
    private val successfulRequests = AtomicLong(0)
    private val failedRequests = AtomicLong(0)
    private val totalResponseTime = AtomicLong(0)

    fun recordSuccess(durationMillis: Long) {

        totalRequests.incrementAndGet()
        successfulRequests.incrementAndGet()

        totalResponseTime.addAndGet(
            durationMillis.coerceAtLeast(0)
        )
    }

    fun recordFailure(durationMillis: Long) {

        totalRequests.incrementAndGet()
        failedRequests.incrementAndGet()

        totalResponseTime.addAndGet(
            durationMillis.coerceAtLeast(0)
        )
    }

    fun getSnapshot(): TelemetrySnapshot {

        val total = totalRequests.get()

        val average = if (total > 0) {
            totalResponseTime.get() / total
        } else {
            0L
        }

        return TelemetrySnapshot(
            totalRequests = total,
            successfulRequests = successfulRequests.get(),
            failedRequests = failedRequests.get(),
            averageResponseTimeMillis = average
        )
    }

    fun reset() {

        totalRequests.set(0)
        successfulRequests.set(0)
        failedRequests.set(0)
        totalResponseTime.set(0)
    }
}
