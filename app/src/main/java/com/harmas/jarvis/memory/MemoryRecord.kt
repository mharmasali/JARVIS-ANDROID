package com.harmas.jarvis.memory

import java.util.UUID

data class MemoryRecord(
    val id: String = UUID.randomUUID().toString(),
    val key: String,
    val value: String,
    val category: String = "general",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
