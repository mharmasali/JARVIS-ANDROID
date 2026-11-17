package com.harmas.jarvis.memory

import com.harmas.jarvis.core.ChatMessage
import com.harmas.jarvis.core.MessageRole
import android.content.ContentValues

class ConversationRepository(
    private val database: JarvisMemoryDatabase
) {

    fun save(message: ChatMessage) {

        val values = ContentValues().apply {
            put("id", message.id)
            put("role", message.role.name)
            put("content", message.content)
            put("created_at", message.timestampMillis)
        }

        database.writableDatabase.insertWithOnConflict(
            "conversations",
            null,
            values,
            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun getRecent(limit: Int = 50): List<ChatMessage> {

        val cursor = database.readableDatabase.query(
            "conversations",
            null,
            null,
            null,
            null,
            null,
            "created_at DESC",
            limit.coerceIn(1, 500).toString()
        )

        return cursor.use {
            buildList {
                while (it.moveToNext()) {
                    val roleName = it.getString(
                        it.getColumnIndexOrThrow("role")
                    )

                    val role = runCatching {
                        MessageRole.valueOf(roleName)
                    }.getOrDefault(MessageRole.USER)

                    add(
                        ChatMessage(
                            id = it.getString(
                                it.getColumnIndexOrThrow("id")
                            ),
                            role = role,
                            content = it.getString(
                                it.getColumnIndexOrThrow("content")
                            ),
                            timestampMillis = it.getLong(
                                it.getColumnIndexOrThrow("created_at")
                            )
                        )
                    )
                }
            }.reversed()
        }
    }

    fun clear() {
        database.writableDatabase.delete(
            "conversations",
            null,
            null
        )
    }
}
