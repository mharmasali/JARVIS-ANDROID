package com.harmas.jarvis.documents

object TextExtractor {

    fun extract(
        content: String,
        mimeType: String
    ): String {

        require(content.isNotBlank()) {
            "Document is empty."
        }

        return when {

            mimeType == "text/plain" ||
                mimeType.startsWith("text/") -> {
                cleanText(content)
            }

            mimeType == "application/json" -> {
                content.trim()
            }

            mimeType == "application/pdf" -> {
                throw UnsupportedOperationException(
                    "PDF text extraction requires a PDF library. " +
                    "It is not enabled in this build yet."
                )
            }

            mimeType.contains("word") ||
                mimeType.contains("officedocument") -> {
                throw UnsupportedOperationException(
                    "Word document extraction requires DOCX support."
                )
            }

            else -> {
                throw UnsupportedOperationException(
                    "Unsupported document format: $mimeType"
                )
            }
        }
    }

    private fun cleanText(text: String): String {

        return text
            .replace("\u0000", "")
            .replace("\r\n", "\n")
            .trim()
            .take(200_000)
    }
}
