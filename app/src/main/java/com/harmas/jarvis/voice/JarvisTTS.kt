package com.harmas.jarvis.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import java.util.Locale

class JarvisTTS(
    context: Context,
    private val onReady: (Boolean) -> Unit = {}
) {

    private var tts: TextToSpeech? = null
    private var ready = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->

            ready = status == TextToSpeech.SUCCESS

            if (ready) {
                setLanguage("en")
                tts?.setSpeechRate(0.92f)
            }

            onReady(ready)
        }
    }

    fun setLanguage(language: String): Boolean {

        if (!ready) return false

        val locale = when (language.lowercase()) {
            "ur", "urdu" -> Locale("ur", "PK")
            "en", "english" -> Locale.US
            else -> Locale.getDefault()
        }

        val result = tts?.setLanguage(locale)
            ?: TextToSpeech.ERROR

        return result != TextToSpeech.LANG_MISSING_DATA &&
            result != TextToSpeech.LANG_NOT_SUPPORTED
    }

    fun speak(text: String) {

        if (!ready || text.isBlank()) return

        tts?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "jarvis_${System.currentTimeMillis()}"
        )
    }

    fun getAvailableVoices(): List<String> {

        if (!ready) return emptyList()

        return tts?.voices
            ?.map { it.name }
            ?.sorted()
            ?: emptyList()
    }

    fun selectVoice(voiceName: String): Boolean {

        if (!ready) return false

        val voice: Voice = tts?.voices
            ?.firstOrNull { it.name == voiceName }
            ?: return false

        return tts?.setVoice(voice) == TextToSpeech.SUCCESS
    }

    fun setSpeechRate(rate: Float) {
        if (ready) {
            tts?.setSpeechRate(rate.coerceIn(0.75f, 1.25f))
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        ready = false
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
