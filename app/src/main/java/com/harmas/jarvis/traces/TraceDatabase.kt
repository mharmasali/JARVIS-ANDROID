package com.harmas.jarvis.traces

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class TraceRecord(
    val id: String,
    val component: String,
    val operation: String,
    val success: Boolean,
    val details: String,
    val timestamp: Long
)

class TraceDatabase(context: Context) :
    SQLiteOpenHelper(
        context.applicationContext,
        "jarvis_traces.db",
        null,
        1
    ) {

    override fun onCreate(db: SQLiteDatabase) {

        db.execSQL(
            """
            CREATE TABLE traces (
                id TEXT PRIMARY KEY,
                component TEXT NOT NULL,
                operation TEXT NOT NULL,
                success INTEGER NOT NULL,
                details TEXT NOT NULL,
                timestamp INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE INDEX trace_timestamp_index
            ON traces(timestamp)
            """.trimIndent()
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        // Use database migrations for future schema changes.
    }

    fun save(record: TraceRecord) {

        val values = ContentValues().apply {
            put("id", record.id)
            put("component", record.component)
            put("operation", record.operation)
            put("success", if (record.success) 1 else 0)
            put("details", record.details.take(2000))
            put("timestamp", record.timestamp)
        }

        writableDatabase.insertWithOnConflict(
            "traces",
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun getRecent(limit: Int = 50): List<TraceRecord> {

        val cursor = readableDatabase.query(
            "traces",
            null,
            null,
            null,
            null,
            null,
            "timestamp DESC",
            limit.coerceIn(1, 500).toString()
        )

        return cursor.use {
            buildList {
                while (it.moveToNext()) {

                    add(
                        TraceRecord(
                            id = it.getString(
                                it.getColumnIndexOrThrow("id")
                            ),
                            component = it.getString(
                                it.getColumnIndexOrThrow("component")
                            ),
                            operation = it.getString(
                                it.getColumnIndexOrThrow("operation")
                            ),
                            success = it.getInt(
                                it.getColumnIndexOrThrow("success")
                            ) == 1,
                            details = it.getString(
                                it.getColumnIndexOrThrow("details")
                            ),
                            timestamp = it.getLong(
                                it.getColumnIndexOrThrow("timestamp")
                            )
                        )
                    )
                }
            }
        }
    }

    fun deleteAll() {
        writableDatabase.delete(
            "traces",
            null,
            null
        )
    }
    }
