package com.harmas.jarvis.security

data class SecretFinding(
    val type: String,
    val line: Int
)

object SecretScanner {

    private val patterns = mapOf(
        "Possible API key" to
            Regex("""(?i)(api[_-]?key|secret[_-]?key)\s*[:=]\s*["'][^"']{12,}["']"""),

        "Possible bearer token" to
            Regex("""(?i)bearer\s+[A-Za-z0-9._~+/=-]{16,}"""),

        "Possible password" to
            Regex("""(?i)password\s*[:=]\s*["'][^"']{6,}["']"""),

        "Possible private key" to
            Regex("""-----BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY-----""")
    )

    fun scan(source: String): List<SecretFinding> {

        val findings = mutableListOf<SecretFinding>()

        source.lines().forEachIndexed { index, line ->

            patterns.forEach { (type, regex) ->

                if (regex.containsMatchIn(line)) {
                    findings.add(
                        SecretFinding(
                            type = type,
                            line = index + 1
                        )
                    )
                }
            }
        }

        return findings.distinct()
    }

    fun containsPossibleSecret(source: String): Boolean {
        return scan(source).isNotEmpty()
    }
}
