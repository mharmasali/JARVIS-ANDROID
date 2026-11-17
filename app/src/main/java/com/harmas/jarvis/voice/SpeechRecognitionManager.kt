package com.harmas.jarvis.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

class SpeechRecognitionManager(
    private val context: Context,
    private val listener: Listener
) {

    interface Listener {
        fun onResult(text: String)
        fun onError(message: String)
        fun onListeningChanged(listening: Boolean)
    }

    private var recognizer: SpeechRecognizer? = null

    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun startListening(languageTag: String = "en-US") {

        if (!isAvailable()) {
            listener.onError("Speech recognition is unavailable.")
            return
        }

        if (context.checkSelfPermission(
                android.Manifest.permission.RECORD_AUDIO
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            listener.onError("Microphone permission is required.")
            return
        }

        stopListening()

        recognizer = SpeechRecognizer.createSpeechRecognizer(context)

        recognizer?.setRecognitionListener(
            object : RecognitionListener {

                override fun onReadyForSpeech(params: Bundle?) {
                    listener.onListeningChanged(true)
                }

                override fun onBeginningOfSpeech() {}

                override fun onRmsChanged(rmsdB: Float) {}

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    listener.onListeningChanged(false)
                }

                override fun onError(error: Int) {
                    listener.onListeningChanged(false)
                    listener.onError("Speech recognition error: $error")
                }

                override fun onResults(results: Bundle?) {
                    listener.onListeningChanged(false)

                    val text = results
                        ?.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                        )
                        ?.firstOrNull()

                    if (!text.isNullOrBlank()) {
                        listener.onResult(text)
                    } else {
                        listener.onError("No speech detected.")
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {}

                override fun onEvent(eventType: Int, params: Bundle?) {}
            }
        )

        val intent = Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        ).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                languageTag
            )
            putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                true
            )
            putExtra(
                RecognizerIntent.EXTRA_MAX_RESULTS,
                3
            )
        }

        recognizer?.startListening(intent)
    }

    fun stopListening() {
        recognizer?.cancel()
        recognizer?.destroy()
        recognizer = null
        listener.onListeningChanged(false)
    }
}
