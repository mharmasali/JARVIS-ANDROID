package com.harmas.jarvis.network

import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

data class NetworkHealth(
    val reachable: Boolean,
    val statusCode: Int = -1,
    val message: String = ""
)

class NetworkStatus {

    private val executor = Executors.newSingleThreadExecutor()

    suspend fun check(
        url: String
    ): NetworkHealth = suspendCoroutine { continuation ->

        executor.execute {
            var connection: HttpURLConnection? = null

            try {
                connection = URL(url)
                    .openConnection() as HttpURLConnection

                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val status = connection.responseCode

                continuation.resume(
                    NetworkHealth(
                        reachable = status in 200..499,
                        statusCode = status,
                        message = "HTTP $status"
                    )
                )

            } catch (error: Exception) {

                continuation.resume(
                    NetworkHealth(
                        reachable = false,
                        message = error.message ?: "Connection failed"
                    )
                )

            } finally {
                connection?.disconnect()
            }
        }
    }

    fun shutdown() {
        executor.shutdownNow()
    }
}
