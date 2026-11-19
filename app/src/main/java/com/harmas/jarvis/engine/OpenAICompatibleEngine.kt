package com.harmas.jarvis.engine

import com.harmas.jarvis.core.MessageRole
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

    override val id = "gemini-api"

    private val executor = Executors.newCachedThreadPool()

    override fun supportedModels(): List<ModelSpec> {
        return listOf(
            ModelSpec(
                id = defaultModel,
                name = defaultModel,
                provider = "Google Gemini",
                local = false,
                capabilities = setOf("chat")
            )
        )
    }

    override suspend fun generate(
        request: InferenceRequest
    ): InferenceResponse = suspendCoroutine { continuation ->

        executor.execute {
            var connection: HttpURLConnection? = null

            try {
                val contents = JSONArray()
                val systemInstructions = JSONArray()

                request.messages.forEach { message ->

                    if (message.content.isBlank()) {
                        return@forEach
                    }

                    if (message.role == MessageRole.SYSTEM) {
                        systemInstructions.put(
                            JSONObject().put("text", message.content)
                        )
                    } else {
                        val role = when (message.role) {
                            MessageRole.ASSISTANT -> "model"
                            MessageRole.USER -> "user"
                            MessageRole.TOOL -> "user"
                            MessageRole.SYSTEM -> "user"
                        }

                        contents.put(
                            JSONObject().apply {
                                put("role", role)
                                put(
                                    "parts",
                                    JSONArray().put(
                                        JSONObject().put(
                                            "text",
                                            message.content
                                        )
                                    )
                                )
                            }
                        )
                    }
                }

                require(contents.length() > 0) {
                    "No user messages were provided."
                }

                val model = request.modelId ?: defaultModel

                val body = JSONObject().apply {

                    put("contents", contents)

                    if (systemInstructions.length() > 0) {
                        put(
                            "systemInstruction",
                            JSONObject().put(
                                "parts",
                                systemInstructions
                            )
                        )
                    }

                    put(
                        "generationConfig",
                        JSONObject().apply {
                            put(
                                "temperature",
                                request.config.temperature.toDouble()
                            )
                            put(
                                "maxOutputTokens",
                                request.config.maxTokens
                            )
                            put(
                                "topP",
                                request.config.topP.toDouble()
                            )
                        }
                    )
                }

                val endpoint =
                    "${baseUrl.trimEnd('/')}/models/$model:generateContent"

                connection = URL(endpoint)
                    .openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.connectTimeout = 20000
                connection.readTimeout = 90000
                connection.doOutput = true

                connection.setRequestProperty(
                    "Content-Type",
                    "application/json"
                )

                connection.setRequestProperty(
                    "x-goog-api-key",
                    apiKey
                )

                connection.outputStream.use { output ->
                    output.write(
                        body.toString().toByteArray(Charsets.UTF_8)
                    )
                }

                val status = connection.responseCode

                val responseText = (
                    if (status in 200..299) {
                        connection.inputStream
                    } else {
                        connection.errorStream
                    }
                )?.bufferedReader()?.use {
                    it.readText()
                } ?: throw IllegalStateException(
                    "Gemini returned an empty response."
                )

                if (status !in 200..299) {
                    val details = runCatching {
                        JSONObject(responseText)
                            .optJSONObject("error")
                            ?.optString("message")
                    }.getOrNull()

                    throw IllegalStateException(
                        "Gemini API HTTP $status: " +
                            (details ?: responseText)
                    )
                }

                val json = JSONObject(responseText)

                val candidates = json.optJSONArray("candidates")
                    ?: throw IllegalStateException(
                        "Gemini returned no candidates: $responseText"
                    )

                if (candidates.length() == 0) {
                    throw IllegalStateException(
                        "Gemini did not return an answer. " +
                            "The request may have been blocked."
                    )
                }

                val parts = candidates
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")

                val answer = buildString {
                    for (index in 0 until parts.length()) {
                        val part = parts.getJSONObject(index)
                        val text = part.optString("text")

                        if (text.isNotBlank()) {
                            if (isNotEmpty()) append("\n")
                            append(text)
                        }
                    }
                }.trim()

                if (answer.isBlank()) {
                    throw IllegalStateException(
                        "Gemini returned no text answer."
                    )
                }

                continuation.resume(
                    InferenceResponse(
                        content = answer,
                        modelId = model
                    )
                )

            } catch (exception: Exception) {
                continuation.resumeWithException(exception)
            } finally {
                connection?.disconnect()
            }
        }
    }

    override suspend fun healthCheck(): EngineStatus {
        val configured =
            baseUrl.startsWith("https://") &&
            apiKey.isNotBlank() &&
            defaultModel.isNotBlank()

        return EngineStatus(
            engineId = id,
            available = configured,
            message = if (configured) {
                "Gemini API settings configured. Live connection is checked during a request."
            } else {
                "Gemini API configuration is incomplete."
            }
        )
    }
}
