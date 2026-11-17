package com.harmas.jarvis.engine

import com.harmas.jarvis.core.MessageRole
import com.harmas.jarvis.engine.InferenceEngine
import com.harmas.jarvis.engine.InferenceRequest
import com.harmas.jarvis.engine.InferenceResponse
import com.harmas.jarvis.engine.EngineStatus
import com.harmas.jarvis.intelligence.ModelSpec
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class OpenAICompatibleEngine(
    private val baseUrl: String,
    private val apiKey: String,
    private val defaultModel: String
) : InferenceEngine {

    override val id = "openai-compatible"

    private val executor = Executors.newCachedThreadPool()

    override fun supportedModels(): List<ModelSpec> {
        return listOf(
            ModelSpec(
                id = defaultModel,
                name = defaultModel,
                provider = "OpenAI-compatible",
                local = false,
                capabilities = setOf("chat", "tools")
            )
        )
    }

    override suspend fun generate(
        request: InferenceRequest
    ): InferenceResponse = suspendCoroutine { continuation ->

        executor.execute {
            try {
                val messages = JSONArray()

                request.messages.forEach { message ->
                    messages.put(
                        JSONObject().apply {
                            put("role", message.role.name.lowercase())
                            put("content", message.content)
                        }
                    )
                }

                val model = request.modelId ?: defaultModel

                val body = JSONObject().apply {
                    put("model", model)
                    put("messages", messages)
                    put("temperature", request.config.temperature)
                    put("max_tokens", request.config.maxTokens)
                    put("top_p", request.config.topP)
                }

                val connection = URL(
                    "${baseUrl.trimEnd('/')}/chat/completions"
                ).openConnection() as HttpURLConnection

                try {
                    connection.requestMethod = "POST"
                    connection.connectTimeout = 15000
                    connection.readTimeout = 60000
                    connection.doOutput = true
                    connection.setRequestProperty(
                        "Content-Type",
                        "application/json"
                    )

                    if (apiKey.isNotBlank()) {
                        connection.setRequestProperty(
                            "Authorization",
                            "Bearer $apiKey"
                        )
                    }

                    connection.outputStream.use {
                        it.write(body.toString().toByteArray(Charsets.UTF_8))
                    }

                    val status = connection.responseCode

                    val responseText = (
                        if (status in 200..299) {
                            connection.inputStream
                        } else {
                            connection.errorStream
                        }
                    )?.bufferedReader()?.use { it.readText() }
                        ?: throw IllegalStateException("Empty server response")

                    if (status !in 200..299) {
                        throw IllegalStateException(
                            "AI server returned HTTP $status: $responseText"
                        )
                    }

                    val json = JSONObject(responseText)
                    val choices = json.getJSONArray("choices")
                    val content = choices
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .optString("content")

                    continuation.resume(
                        InferenceResponse(
                            content = content,
                            modelId = model
                        )
                    )

                } finally {
                    connection.disconnect()
                }

            } catch (exception: Exception) {
                continuation.resumeWithException(exception)
            }
        }
    }

    override suspend fun healthCheck(): EngineStatus {
        return EngineStatus(
            engineId = id,
            available = baseUrl.startsWith("https://") ||
                baseUrl.startsWith("http://"),
            message = "Endpoint configured; connection not yet verified."
        )
    }
}
