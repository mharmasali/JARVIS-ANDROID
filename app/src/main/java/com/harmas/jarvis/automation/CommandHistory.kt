package com.harmas.jarvis.automation

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class CommandEntry(
    val command: String,
    val timestamp: Long
)

class CommandHistory(context: Context) {

    private val preferences = context.applicationContext
        .getSharedPreferences(
            "jarvis_command_history",
            Context.MODE_PRIVATE
        )

    @Synchronized
    fun add(command: String) {

        if (command.isBlank()) return

        val history = getAll().toMutableList()

        history.add(
            CommandEntry(
                command = command.trim(),
                timestamp = System.currentTimeMillis()
            )
        )

        val jsonArray = JSONArray()

        history.takeLast(100).forEach { entry ->

            jsonArray.put(
                JSONObject().apply {
                    put("command", entry.command)
                    put("timestamp", entry.timestamp)
                }
            )
        }

        preferences.edit()
            .putString("history", jsonArray.toString())
            .apply()
    }

    @Synchronized
    fun getAll(): List<CommandEntry> {

        val raw = preferences.getString(
            "history",
            "[]"
        ) ?: "[]"

        return try {

            val array = JSONArray(raw)

            buildList {
                for (index in 0 until array.length()) {

                    val item = array.getJSONObject(index)

                    add(
                        CommandEntry(
                            command = item.getString("command"),
                            timestamp = item.getLong("timestamp")
                        )
                    )
                }
            }

        } catch (error: Exception) {
            emptyList()
        }
    }

    fun clear() {
        preferences.edit()
            .remove("history")
            .apply()
    }
}
