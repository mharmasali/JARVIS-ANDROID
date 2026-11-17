package com.harmas.jarvis.voice

class VoiceCommandProcessor(
    private val callback: Callback
) {

    interface Callback {
        fun onOpenApp(appName: String)
        fun onStopAssistant()
        fun onGeneralQuery(query: String)
        fun onRememberRequest(key: String, value: String)
        fun onUnknownCommand(command: String)
    }

    fun process(rawText: String) {

        val command = rawText.trim()

        if (command.isBlank()) {
            callback.onUnknownCommand(command)
            return
        }

        val normalized = command.lowercase()

        when {

            normalized in listOf(
                "stop jarvis",
                "stop assistant",
                "jarvis stop"
            ) -> {
                callback.onStopAssistant()
            }

            normalized.startsWith("open ") -> {

                val appName = command.substringAfter(
                    "open ",
                    ""
                ).trim()

                if (appName.isNotBlank()) {
                    callback.onOpenApp(appName)
                } else {
                    callback.onUnknownCommand(command)
                }
            }

            normalized.startsWith("remember ") -> {

                val memory = command.substringAfter(
                    "remember "
                )

                val separator = memory.indexOf(" is ")

                if (separator > 0) {

                    val key = memory.substring(
                        0,
                        separator
                    ).trim()

                    val value = memory.substring(
                        separator + 4
                    ).trim()

                    if (key.isNotBlank() && value.isNotBlank()) {
                        callback.onRememberRequest(key, value)
                    } else {
                        callback.onUnknownCommand(command)
                    }

                } else {
                    callback.onUnknownCommand(command)
                }
            }

            else -> {
                callback.onGeneralQuery(command)
            }
        }
    }
}
