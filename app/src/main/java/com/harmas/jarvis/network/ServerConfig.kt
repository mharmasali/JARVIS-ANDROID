package com.harmas.jarvis.network

data class ServerConfig(
    val baseUrl: String,
    val apiKey: String = "",
    val model: String = "",
    val connectTimeoutMillis: Int = 15000,
    val readTimeoutMillis: Int = 60000
) {
    fun isValid(): Boolean {
        return (
            baseUrl.startsWith("https://") ||
            baseUrl.startsWith("http://")
        ) && connectTimeoutMillis > 0 &&
            readTimeoutMillis > 0
    }
}
