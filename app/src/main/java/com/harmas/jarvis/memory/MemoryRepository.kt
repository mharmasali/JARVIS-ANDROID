package com.harmas.jarvis.memory

import android.content.ContentValues

class MemoryRepository(
    private val database: JarvisMemoryDatabase
) {

    fun save(memory: MemoryRecord) {

        val values = ContentValues().apply {
            put("id", memory.id)
            put("memory_key", memory.key)
            put("memory_value", memory.value)
            put("category", memory.category)
            put("created_at", memory.createdAt)
            put("updated_at", memory.updatedAt)
        }

        database.writableDatabase.insertWithOnConflict(
            "memories",
            null,
            values,
            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun getByKey(key: String): MemoryRecord? {

        val cursor = database.readableDatabase.query(
            "memories",
            null,
            "memory_key = ?",
            arrayOf(key),
            null,
            null,
            "updated_at DESC",
            "1"
        )

        return cursor.use {
            if (it.moveToFirst()) {
                readMemory(it)
            } else null
        }
    }

    fun getAll(limit: Int = 100): List<MemoryRecord> {

        val cursor = database.readableDatabase.query(
            "memories",
            null,
            null,
            null,
            null,
            null,
            "updated_at DESC",
            limit.coerceIn(1, 1000).toString()
        )

        return cursor.use {
            buildList {
                while (it.moveToNext()) {
                    add(readMemory(it))
                }
            }
        }
    }

    fun search(query: String, limit: Int = 20): List<MemoryRecord> {

        val cursor = database.readableDatabase.query(
            "memories",
            null,
            "memory_key LIKE ? OR memory_value LIKE ?",
            arrayOf("%$query%", "%$query%"),
            null,
            null,
            "updated_at DESC",
            limit.coerceIn(1, 100).toString()
        )

        return cursor.use {
            buildList {
                while (it.moveToNext()) {
                    add(readMemory(it))
                }
            }
        }
    }

    fun getByCategory(category: String): List<MemoryRecord> {

        val cursor = database.readableDatabase.query(
            "memories",
            null,
            "category = ?",
            arrayOf(category),
            null,
            null,
            "updated_at DESC"
        )

        return cursor.use {
            buildList {
                while (it.moveToNext()) {
                    add(readMemory(it))
                }
            }
        }
    }

    fun delete(id: String): Boolean {

        return database.writableDatabase.delete(
            "memories",
            "id = ?",
            arrayOf(id)
        ) > 0
    }

    fun deleteAll() {
        database.writableDatabase.delete(
            "memories",
            null,
            null
        )
    }

    private fun readMemory(
        cursor: android.database.Cursor
    ): MemoryRecord {

        return MemoryRecord(
            id = cursor.getString(
                cursor.getColumnIndexOrThrow("id")
            ),
            key = cursor.getString(
                cursor.getColumnIndexOrThrow("memory_key")
            ),
            value = cursor.getString(
                cursor.getColumnIndexOrThrow("memory_value")
            ),
            category = cursor.getString(
                cursor.getColumnIndexOrThrow("category")
            ),
            createdAt = cursor.getLong(
                cursor.getColumnIndexOrThrow("created_at")
            ),
            updatedAt = cursor.getLong(
                cursor.getColumnIndexOrThrow("updated_at")
            )
        )
    }
}
