package com.harmas.jarvis

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.harmas.jarvis.core.ChatMessage
import com.harmas.jarvis.core.JarvisCore
import com.harmas.jarvis.core.MessageRole
import com.harmas.jarvis.engine.InferenceRequest
import com.harmas.jarvis.intelligence.RoutingMode
import com.harmas.jarvis.service.JarvisApplication
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var core: JarvisCore
    private lateinit var status: TextView
    private lateinit var response: TextView
    private lateinit var micButton: Button
    private lateinit var engineStatus: TextView

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null

    private var ttsReady = false
    private var continuousListening = false
    private var processing = false

    private val handler = Handler(Looper.getMainLooper())

    private val cyan = 0xFF00D9FF.toInt()
    private val screenBackground = 0xFF080F18.toInt()
    private val surface = 0xFF101D2B.toInt()
    private val white = 0xFFF2FAFF.toInt()
    private val muted = 0xFF9CB3C7.toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        core = (application as JarvisApplication).jarvisCore

        // Prefer the configured cloud AI engine.
        core.aiRouter.routingMode = RoutingMode.CLOUD_ONLY

        textToSpeech = TextToSpeech(this, this)

        createDashboard()
    }

    private fun createDashboard() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 30, 24, 20)
            setBackgroundColor(screenBackground)
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
            text = "INITIALIZING GEMINI..."
            textSize = 12f
            setTextColor(0xFFFFB547.toInt())
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
            text = "Hello, sir. Ask me anything."
            textSize = 16f
            setTextColor(white)
            gravity = Gravity.CENTER
            setPadding(18, 24, 18, 24)
            setBackgroundColor(surface)
        }

        micButton = Button(this).apply {
            text = "START LISTENING"
            setTextColor(screenBackground)
            setBackgroundColor(cyan)

            setOnClickListener {
                if (continuousListening) {
                    stopListening()
                } else {
                    requestMicrophonePermission()
                }
            }
        }

        val time = TextView(this).apply {
            text = "LOCAL TIME: " +
                SimpleDateFormat(
                    "hh:mm a",
                    Locale.getDefault()
                ).format(Date())

            textSize = 13f
            setTextColor(muted)
            gravity = Gravity.CENTER
            setPadding(8, 22, 8, 10)
        }

        val memory = TextView(this).apply {
            text = "MEMORY: LOCAL DATABASE"
            textSize = 13f
            setTextColor(cyan)
            gravity = Gravity.CENTER
            setPadding(8, 8, 8, 8)
        }

        engineStatus = TextView(this).apply {
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
        content.addView(engineStatus)

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

        refreshEngineStatus()
    }

    private fun refreshEngineStatus() {
        val count = core.getStatus()["registeredEngines"] as? Int ?: 0

        engineStatus.text = "REGISTERED AI ENGINES: $count"

        status.text = if (count > 0) {
            "GEMINI ENGINE CONFIGURED"
        } else {
            "GEMINI NOT CONFIGURED"
        }
    }

    private fun requestMicrophonePermission() {
        if (
            ContextCompat.checkSelfPermission(
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

        continuousListening = true
        processing = false
        micButton.text = "STOP LISTENING"

        startListeningSession()
    }

    private fun startListeningSession() {
        if (
            !continuousListening ||
            processing ||
            isFinishing ||
            isDestroyed
        ) return

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            status.text = "SPEECH RECOGNITION UNAVAILABLE"
            stopListening()
            return
        }

        try {
            speechRecognizer?.destroy()

            speechRecognizer =
                SpeechRecognizer.createSpeechRecognizer(this)

            speechRecognizer?.setRecognitionListener(
                object : RecognitionListener {

                    override fun onReadyForSpeech(params: Bundle?) {
                        if (continuousListening) {
                            status.text = "LISTENING..."
                        }
                    }

                    override fun onBeginningOfSpeech() {
                        status.text = "HEARING YOU..."
                    }

                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        status.text = "PROCESSING SPEECH..."
                    }

                    override fun onError(error: Int) {
                        if (continuousListening && !processing) {
                            status.text = "RESTARTING LISTENING..."

                            handler.postDelayed(
                                { startListeningSession() },
                                1000
                            )
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        val spoken = results
                            ?.getStringArrayList(
                                SpeechRecognizer.RESULTS_RECOGNITION
                            )
                            ?.firstOrNull()
                            .orEmpty()

                        if (spoken.isNotBlank() && continuousListening) {
                            handleUserMessage(spoken)
                        } else if (continuousListening) {
                            handler.postDelayed(
                                { startListeningSession() },
                                500
                            )
                        }
                    }

                    override fun onPartialResults(
                        partialResults: Bundle?
                    ) {}

                    override fun onEvent(
                        eventType: Int,
                        params: Bundle?
                    ) {}
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
                    Locale.getDefault().toLanguageTag()
                )

                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }

            speechRecognizer?.startListening(intent)

        } catch (e: Exception) {
            status.text = "VOICE ERROR"
            handler.postDelayed(
                { startListeningSession() },
                1200
            )
        }
    }

    private fun stopListening() {
        continuousListening = false
        processing = false

        handler.removeCallbacksAndMessages(null)

        speechRecognizer?.cancel()

        micButton.text = "START LISTENING"
        status.text = "SYSTEM READY"
    }

    private fun handleUserMessage(userText: String) {
        val normalized = userText.lowercase(Locale.ROOT).trim()

        if (
            normalized == "stop listening" ||
            normalized == "stop jarvis" ||
            normalized == "jarvis stop"
        ) {
            stopListening()
            speak("Listening stopped, sir.")
            return
        }

        if (normalized.startsWith("remember my name is ")) {
            val name = userText
                .substringAfter("remember my name is ")
                .trim()

            if (name.isNotBlank()) {
                core.memoryManager.remember(
                    "user_name",
                    name,
                    "personal"
                )

                response.text =
                    "YOU: $userText\n\nJ.A.R.V.I.S: I will remember your name, $name."

                speak("I will remember your name, $name, sir.")
                return
            }
        }

        if (
            normalized.contains("what is my name") ||
            normalized.contains("who am i")
        ) {
            val name = core.memoryManager.recall("user_name")

            val answer = if (name.isNullOrBlank()) {
                "You have not told me your name yet, sir."
            } else {
                "Your name is $name, sir."
            }

            response.text =
                "YOU: $userText\n\nJ.A.R.V.I.S: $answer"

            speak(answer)
            return
        }

        askGemini(userText)
    }

    private fun askGemini(userText: String) {
        processing = true
        status.text = "GEMINI IS THINKING..."

        response.text =
            "YOU: $userText\n\nJ.A.R.V.I.S: Thinking..."

        lifecycleScope.launch {
            try {
                val previousMessages =
                    core.conversationRepository.getRecent(12)

                val memoryContext =
                    core.memoryManager.buildContext(20)

                val systemMessage = ChatMessage(
                    role = MessageRole.SYSTEM,
                    content = """
                        You are J.A.R.V.I.S, Just A Rather Very Intelligent System.
                        You are a personal Android assistant.
                        Address the user respectfully as sir.
                        Understand English, Urdu and Roman Urdu.
                        Reply naturally and clearly.
                        Keep normal answers concise.
                        Never claim an Android action was completed unless
                        an actual connected tool performed it.

                        Saved memory:
                        ${memoryContext.ifBlank { "No saved memories." }}
                    """.trimIndent()
                )

                val userMessage = ChatMessage(
                    role = MessageRole.USER,
                    content = userText
                )

                val messages = buildList {
                    add(systemMessage)
                    addAll(
                        previousMessages.filter {
                            it.role == MessageRole.USER ||
                                it.role == MessageRole.ASSISTANT
                        }
                    )
                    add(userMessage)
                }

                core.conversationRepository.save(userMessage)

                val result = core.aiRouter.generate(
                    InferenceRequest(
                        messages = messages
                    )
                )

                val assistantMessage = ChatMessage(
                    role = MessageRole.ASSISTANT,
                    content = result.content
                )

                core.conversationRepository.save(assistantMessage)

                response.text =
                    "YOU: $userText\n\nJ.A.R.V.I.S: ${result.content}"

                status.text = "GEMINI RESPONSE RECEIVED"

                speak(result.content)

            } catch (e: Exception) {
                val error = e.message ?: "Unknown Gemini error"

                response.text =
                    "YOU: $userText\n\nJ.A.R.V.I.S: $error"

                status.text = "GEMINI CONNECTION ERROR"

                speak("I could not connect to Gemini, sir.")
            } finally {
                processing = false
                refreshEngineStatus()
            }
        }
    }

    private fun speak(message: String) {
        if (!ttsReady) {
            resumeListeningAfterReply()
            return
        }

        textToSpeech?.speak(
            message,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "JARVIS_REPLY"
        )
    }

    private fun resumeListeningAfterReply() {
        if (continuousListening && !processing) {
            handler.postDelayed(
                { startListeningSession() },
                900
            )
        }
    }

    override fun onInit(result: Int) {
        if (result == TextToSpeech.SUCCESS) {
            val languageResult =
                textToSpeech?.setLanguage(Locale.US)

            ttsReady =
                languageResult != TextToSpeech.LANG_MISSING_DATA &&
                languageResult != TextToSpeech.LANG_NOT_SUPPORTED

            textToSpeech?.setOnUtteranceProgressListener(
                object : UtteranceProgressListener() {

                    override fun onStart(utteranceId: String?) {}

                    override fun onDone(utteranceId: String?) {
                        handler.post {
                            resumeListeningAfterReply()
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        handler.post {
                            resumeListeningAfterReply()
                        }
                    }
                }
            )
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

        if (
            requestCode == 100 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            requestMicrophonePermission()
        }
    }

    override fun onDestroy() {
        continuousListening = false
        handler.removeCallbacksAndMessages(null)

        speechRecognizer?.destroy()
        speechRecognizer = null

        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null

        super.onDestroy()
    }
}
