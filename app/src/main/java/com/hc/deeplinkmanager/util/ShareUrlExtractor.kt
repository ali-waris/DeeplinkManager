package com.hc.deeplinkmanager.util

/**
 * Extracts a deeplink/URL from free-form shared text. Pure Kotlin so it is unit-testable
 * without Android runtime stubs.
 */
object ShareUrlExtractor {

    private val schemeRegex = Regex("^[a-zA-Z][a-zA-Z0-9+.\\-]*:")

    private val trailingPunctuation = ".\\,;!?)}\"'>"
    private val leadingPunctuation = "(\\[{\"'<"

    /**
     * Returns the first whitespace-delimited token that carries a valid scheme (`scheme://...`
     * or bare `scheme:`), or null if the text contains no recognizable URL.
     */
    fun extractFirstUrl(text: String): String? {
        return text.split(Regex("\\s+")).firstNotNullOfOrNull { token ->
            val cleaned = token.trim().trimEnd(*trailingPunctuation.toCharArray())
                .trimStart(*leadingPunctuation.toCharArray())
            if (schemeRegex.containsMatchIn(cleaned)) cleaned else null
        }
    }

    /**
     * Derives a readable name for [url]: the host when present, else the last non-blank
     * path segment, else a generic fallback.
     */
    fun deriveName(url: String): String {
        val body = url.substringAfter("://", url)
        val host = body.takeWhile { it != '/' && it != '?' && it != '#' }
        if (host.isNotBlank()) return host
        val path = body.substringBefore('?').substringBefore('#')
        val segment = path.substringAfterLast('/')
        return segment.ifBlank { "Shared deeplink" }
    }
}
