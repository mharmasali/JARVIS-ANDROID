package com.harmas.jarvis.network

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.io.IOException
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class JarvisHttpClient(
    private val config: ServerConfig
) {

    private val executor = Executors.newFixedThreadPool(2)

    suspend fun post(
        endpoint: String,
        body: JSONObject
    ): JSONObject = suspendCoroutine { continuation ->

        executor.execute {
            var connection: HttpURLConnection? = null

            try {
                require(config.isValid()) {
                    "Invalid server configuration"
                }

                require(endpoint.startsWith("/")) {
                    "Endpoint must start with /"
                }

                val url = URL(
                    config.baseUrl.trimEnd('/') + endpoint
                )

                connection = url.openConnection()
                    as HttpURLConnection

                connection.apply {
                    requestMethod = "POST"
                    connectTimeout = config.connectTimeoutMillis
                    readTimeout = config.readTimeoutMillis
                    doOutput = true

                    setRequestProperty(
                        "Content-Type",
                        "application/json"
                    )

                    setRequestProperty(
                        "Accept",
                        "application/json"
                    )

                    if (config.apiKey.isNotBlank()) {
                        setRequestProperty(
                            "Authorization",
                            "Bearer ${config.apiKey}"
                        )
                    }
                }

                connection.outputStream.use {
                    it.write(
                        body.toString().toByteArray(Charsets.UTF_8)
                    )
                }

                val status = connection.responseCode

                val stream = if (status in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

                val response = stream?.bufferedReader()
                    ?.use { it.readText() }
                    ?: ""

                if (status !in 200..299) {
                    throw IOException(
                        "Server HTTP $status: $response"
                    )
                }

                continuation.resume(JSONObject(response))

            } catch (error: Exception) {
                continuation.resumeWithException(error)
            } finally {
                connection?.disconnect()
            }
        }
    }

    fun shutdown() {
        executor.shutdownNow()
    }
}
