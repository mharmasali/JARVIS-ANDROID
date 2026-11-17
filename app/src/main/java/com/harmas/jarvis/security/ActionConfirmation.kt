package com.harmas.jarvis.security

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class ConfirmationRequest(
    val id: String,
    val action: String,
    val description: String,
    val createdAt: Long
)

class ActionConfirmation {

    private val pending =
        ConcurrentHashMap<String, ConfirmationRequest>()

    fun request(
        action: String,
        description: String
    ): ConfirmationRequest {

        require(action.isNotBlank())
        require(description.isNotBlank())

        val request = ConfirmationRequest(
            id = UUID.randomUUID().toString(),
            action = action,
            description = description,
            createdAt = System.currentTimeMillis()
        )

        pending[request.id] = request

        return request
    }

    fun confirm(id: String): Boolean {

        val request = pending.remove(id) ?: return false

        return System.currentTimeMillis() -
            request.createdAt <= 60_000
    }

    fun reject(id: String): Boolean {
        return pending.remove(id) != null
    }

    fun getPending(): List<ConfirmationRequest> {

        cleanupExpired()

        return pending.values.toList()
    }

    private fun cleanupExpired() {

        val now = System.currentTimeMillis()

        pending.entries.removeIf {
            now - it.value.createdAt > 60_000
        }
    }
}
