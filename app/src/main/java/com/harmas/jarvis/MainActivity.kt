package com.harmas.jarvis

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.harmas.jarvis.core.JarvisCore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var core: JarvisCore
    private lateinit var status: TextView
    private lateinit var response: TextView
    private lateinit var micButton: Button

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var ttsReady = false

    private val cyan = 0xFF00D9FF.toInt()
    private val background = 0xFF080F18.toInt()
    private val surface = 0xFF101D2B.toInt()
    private val white = 0xFFF2FAFF.toInt()
    private val muted = 0xFF9CB3C7.toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        core = (application as com.harmas.jarvis.service.JarvisApplication)
            .jarvisCore

        textToSpeech = TextToSpeech(this, this)

        createDashboard()
    }

    private fun createDashboard() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 30, 24, 20)
            setBackgroundColor(background)
        }

        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }

        val heading = TextView(this).apply {
            text = "J.A.R.V.I.S"
            textSize = 30f
            setTextColor(cyan)
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "JUST A RATHER VERY INTELLIGENT SYSTEM"
            textSize = 10f
            setTextColor(muted)
            gravity = Gravity.CENTER
        }

        status = TextView(this).apply {
            text = "SYSTEM ONLINE • DASHBOARD READY"
            textSize = 12f
            setTextColor(0xFF28D17C.toInt())
            gravity = Gravity.CENTER
            setPadding(8, 28, 8, 20)
        }

        val coreTitle = TextView(this).apply {
            text = "◉"
            textSize = 100f
            setTextColor(cyan)
            gravity = Gravity.CENTER
            setPadding(0, 15, 0, 15)
        }

        val assistant = TextView(this).apply {
            text = "AT YOUR SERVICE, SIR"
            textSize = 16f
            setTextColor(white)
            gravity = Gravity.CENTER
        }

        response = TextView(this).apply {
            text = "Hello, sir. How can I help you?"
            textSize = 16f
            setTextColor(white)
            gravity = Gravity.CENTER
            setPadding(18, 24, 18, 24)
            setBackgroundColor(surface)
        }

        micButton = Button(this).apply {
            text = "START VOICE INPUT"
            setTextColor(background)
            setBackgroundColor(cyan)
            setOnClickListener {
                startVoiceInput()
            }
        }

        val time = TextView(this).apply {
            text = "LOCAL TIME: " +
                SimpleDateFormat("hh:mm a", Locale.getDefault())
                    .format(Date())
            textSize = 13f
            setTextColor(muted)
            gravity = Gravity.CENTER
            setPadding(8, 22, 8, 10)
        }

        val memory = TextView(this).apply {
            text = "MEMORY: INITIALIZED"
            textSize = 13f
            setTextColor(cyan)
            gravity = Gravity.CENTER
            setPadding(8, 8, 8, 8)
        }

        val engines = TextView(this).apply {
            text = "REGISTERED AI ENGINES: " +
                core.getStatus()["registeredEngines"]
            textSize = 13f
            setTextColor(muted)
            gravity = Gravity.CENTER
            setPadding(8, 8, 8, 8)
        }

        content.addView(heading)
        content.addView(subtitle)
        content.addView(status)
        content.addView(coreTitle)
        content.addView(assistant)
        content.addView(response)
        content.addView(micButton)
        content.addView(time)
        content.addView(memory)
        content.addView(engines)

        scroll.addView(content)
        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)
    }

    private fun startVoiceInput() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                100
            )
            return
        }

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            response.text = "Speech recognition is not available."
            return
        }

        try {
            speechRecognizer?.destroy()

            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

            speechRecognizer?.setRecognitionListener(
                object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        status.text = "LISTENING..."
                        micButton.text = "LISTENING..."
                    }

                    override fun onBeginningOfSpeech() {}

                    override fun onRmsChanged(rmsdB: Float) {}

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        status.text = "PROCESSING..."
                        micButton.text = "START VOICE INPUT"
                    }

                    override fun onError(error: Int) {
                        status.text = "VOICE INPUT ERROR"
                        micButton.text = "TRY AGAIN"
                        response.text = "Please try speaking again."
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                        )

                        val spoken = matches?.firstOrNull().orEmpty()

                        if (spoken.isNotBlank()) {
                            processCommand(spoken)
                        }

                        micButton.text = "START VOICE INPUT"
                    }

                    override fun onPartialResults(partialResults: Bundle?) {}

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                }
            )

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE,
                    Locale.getDefault().toLanguageTag()
                )
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }

            speechRecognizer?.startListening(intent)

        } catch (e: Exception) {
            status.text = "VOICE INPUT UNAVAILABLE"
            response.text = e.localizedMessage ?: "Unknown error"
        }
    }

    private fun processCommand(command: String) {
        response.text = "YOU: $command"

        val normalized = command.lowercase(Locale.ROOT)

        val answer = when {
            normalized.contains("hello") ||
                normalized.contains("salam") ->
                "Hello, sir. How can I help you?"

            normalized.contains("who are you") ->
                "I am J.A.R.V.I.S, your personal Android assistant."

            normalized.contains("time") ->
                "The time is " +
                    SimpleDateFormat("hh:mm a", Locale.getDefault())
                        .format(Date())

            normalized.contains("stop") ->
                "Voice command stopped, sir."

            else ->
                "Command received, sir. AI conversation integration is pending."
        }

        response.text = "YOU: $command\n\nJ.A.R.V.I.S: $answer"
        status.text = "SYSTEM READY"
        speak(answer)
    }

    private fun speak(message: String) {
        if (ttsReady) {
            textToSpeech?.speak(
                message,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "JARVIS_REPLY"
            )
        }
    }

    override fun onInit(result: Int) {
        if (result == TextToSpeech.SUCCESS) {
            val languageResult = textToSpeech?.setLanguage(
                Locale.getDefault()
            )
            ttsReady = languageResult != TextToSpeech.LANG_MISSING_DATA &&
                languageResult != TextToSpeech.LANG_NOT_SUPPORTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (requestCode == 100 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startVoiceInput()
        }
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null

        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null

        super.onDestroy()
    }
}
