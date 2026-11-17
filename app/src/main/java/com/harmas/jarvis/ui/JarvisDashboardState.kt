package com.harmas.jarvis.ui

enum class AssistantState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    ERROR
}

enum class ConnectionState {
    ONLINE,
    OFFLINE,
    CONNECTING
}

data class JarvisDashboardState(
    val assistantName: String = "J.A.R.V.I.S",
    val assistantState: AssistantState = AssistantState.IDLE,
    val connectionState: ConnectionState = ConnectionState.OFFLINE,
    val memoryEnabled: Boolean = true,
    val microphoneEnabled: Boolean = false,
    val accessibilityEnabled: Boolean = false,
    val currentMessage: String = "Systems ready",
    val lastError: String? = null
)

class DashboardStateManager {

    @Volatile
    private var state = JarvisDashboardState()

    fun getState(): JarvisDashboardState {
        return state
    }

    @Synchronized
    fun updateAssistantState(
        newState: AssistantState,
        message: String
    ) {
        state = state.copy(
            assistantState = newState,
            currentMessage = message,
            lastError = null
        )
    }

    @Synchronized
    fun updateConnection(
        connection: ConnectionState
    ) {
        state = state.copy(
            connectionState = connection
        )
    }

    @Synchronized
    fun updateMicrophone(enabled: Boolean) {
        state = state.copy(
            microphoneEnabled = enabled
        )
    }

    @Synchronized
    fun updateAccessibility(enabled: Boolean) {
        state = state.copy(
            accessibilityEnabled = enabled
        )
    }

    @Synchronized
    fun updateMemory(enabled: Boolean) {
        state = state.copy(
            memoryEnabled = enabled
        )
    }

    @Synchronized
    fun reportError(message: String) {
        state = state.copy(
            assistantState = AssistantState.ERROR,
            currentMessage = "An error occurred",
            lastError = message
        )
    }

    @Synchronized
    fun reset() {
        state = JarvisDashboardState()
    }
}
