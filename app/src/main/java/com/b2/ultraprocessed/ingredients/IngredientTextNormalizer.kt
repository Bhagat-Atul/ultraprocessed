package com.b2.ultraprocessed.ingredients

/**
 * Normalizes OCR or pasted ingredient text for classification.
 */
object IngredientTextNormalizer {
    private val ingredientsPrefix = Regex("""^ingredients\s*[:.\-]\s*""", RegexOption.IGNORE_CASE)

    fun normalize(raw: String): String {
        var text = raw.replace("\r\n", "\n").replace('\r', '\n')
        text = text.split('\n').joinToString(" ") { it.trim() }
        text = text.replace(Regex("\\s+"), " ").trim()
        text = ingredientsPrefix.replace(text, "").trim()
        return text
    }
}
