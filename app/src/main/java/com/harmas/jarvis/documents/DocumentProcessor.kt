package com.harmas.jarvis.documents

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns

data class DocumentData(
    val name: String,
    val mimeType: String,
    val content: String,
    val sizeBytes: Long
)

class DocumentProcessor(
    private val resolver: ContentResolver
) {

    companion object {
        private const val MAX_DOCUMENT_SIZE = 5L * 1024 * 1024
    }

    fun readDocument(uri: Uri): DocumentData {

        val metadata = getMetadata(uri)

        require(metadata.second <= MAX_DOCUMENT_SIZE) {
            "Document exceeds the 5 MB limit."
        }

        val mimeType = resolver.getType(uri)
            ?: "application/octet-stream"

        val content = resolver.openInputStream(uri)?.use {
            it.bufferedReader(Charsets.UTF_8).use { reader ->
                reader.readText()
            }
        } ?: throw IllegalStateException(
            "Unable to open selected document."
        )

        val extracted = TextExtractor.extract(
            content = content,
            mimeType = mimeType
        )

        return DocumentData(
            name = metadata.first,
            mimeType = mimeType,
            content = extracted,
            sizeBytes = metadata.second
        )
    }

    private fun getMetadata(uri: Uri): Pair<String, Long> {

        var name = "Unknown document"
        var size = 0L

        resolver.query(
            uri,
            arrayOf(
                OpenableColumns.DISPLAY_NAME,
                OpenableColumns.SIZE
            ),
            null,
            null,
            null
        )?.use { cursor ->

            if (cursor.moveToFirst()) {

                val nameIndex = cursor.getColumnIndex(
                    OpenableColumns.DISPLAY_NAME
                )

                val sizeIndex = cursor.getColumnIndex(
                    OpenableColumns.SIZE
                )

                if (nameIndex >= 0) {
                    name = cursor.getString(nameIndex)
                        ?: name
                }

                if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                    size = cursor.getLong(sizeIndex)
                }
            }
        }

        return Pair(name, size)
    }
}
