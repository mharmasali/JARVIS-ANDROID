package com.harmas.jarvis.memory

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class JarvisMemoryDatabase(context: Context) :
    SQLiteOpenHelper(
        context.applicationContext,
        DATABASE_NAME,
        null,
        DATABASE_VERSION
    ) {

    override fun onCreate(db: SQLiteDatabase) {

        db.execSQL(
            """
            CREATE TABLE memories (
                id TEXT PRIMARY KEY,
                memory_key TEXT NOT NULL,
                memory_value TEXT NOT NULL,
                category TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE INDEX memory_key_index
            ON memories(memory_key)
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE INDEX memory_category_index
            ON memories(category)
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE conversations (
                id TEXT PRIMARY KEY,
                role TEXT NOT NULL,
                content TEXT NOT NULL,
                created_at INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        // Future database migrations will be added here.
    }

    companion object {
        private const val DATABASE_NAME = "jarvis_memory.db"
        private const val DATABASE_VERSION = 1
    }
    }
