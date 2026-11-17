package com.harmas.jarvis.memory

class MemoryManager(
    private val repository: MemoryRepository
) {

    fun remember(
        key: String,
        value: String,
        category: String = "general"
    ) {

        require(key.isNotBlank())
        require(value.isNotBlank())

        val existing = repository.getByKey(key)

        val record = MemoryRecord(
            id = existing?.id ?: java.util.UUID.randomUUID().toString(),
            key = key.trim(),
            value = value.trim(),
            category = category,
            createdAt = existing?.createdAt
                ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        repository.save(record)
    }

    fun recall(key: String): String? {
        return repository.getByKey(key)?.value
    }

    fun search(query: String): List<MemoryRecord> {
        return repository.search(query)
    }

    fun getAllMemories(): List<MemoryRecord> {
        return repository.getAll()
    }

    fun forget(key: String): Boolean {
        val record = repository.getByKey(key)
            ?: return false

        return repository.delete(record.id)
    }

    fun clearAll() {
        repository.deleteAll()
    }

    fun buildContext(limit: Int = 20): String {

        return repository.getAll(limit)
            .joinToString("\n") { memory ->
                "${memory.key}: ${memory.value}"
            }
    }
}
