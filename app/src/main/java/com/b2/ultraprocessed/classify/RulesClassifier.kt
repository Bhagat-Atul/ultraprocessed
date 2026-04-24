package com.b2.ultraprocessed.classify

class RulesClassifier : Classifier {
    override val id: String = "rules"

    override suspend fun classify(
        input: IngredientInput,
        context: ClassificationContext,
    ): ClassificationResult {
        val normalized = input.normalizedText.lowercase()
        val markers = findUltraProcessedMarkers(normalized)
        val retailHits = findRetailPackagingCues(normalized)
        val processedHeuristic = markers.isEmpty() &&
            retailHits.isEmpty() &&
            matchesProcessedFoodHeuristic(normalized)

        val (novaGroup, confidence, displayMarkers, explanation) = when {
            markers.size >= 2 -> {
                val extra = ((markers.size - 2).coerceAtMost(4)) * 0.02f
                Quad(
                    4,
                    (0.82f + extra).coerceAtMost(0.92f),
                    markers,
                    buildAdditiveExplanation(markers),
                )
            }
            markers.size == 1 -> Quad(
                3,
                0.56f,
                markers,
                buildAdditiveExplanation(markers),
            )
            retailHits.size >= 2 && markers.isEmpty() -> Quad(
                4,
                0.74f,
                retailHits.map { "packaging: $it" },
                buildRetailExplanation(retailHits, strong = true),
            )
            retailHits.size == 1 && markers.isEmpty() -> Quad(
                3,
                0.57f,
                retailHits.map { "packaging: $it" },
                buildRetailExplanation(retailHits, strong = false),
            )
            processedHeuristic -> Quad(
                3,
                0.58f,
                emptyList(),
                "Salt with added cooking oil points to a processed recipe rather than a single unprocessed ingredient.",
            )
            else -> Quad(
                1,
                0.62f,
                emptyList(),
                "No strong ultra-processed additive markers showed up; the list looks closer to whole or simple ingredients.",
            )
        }

        return ClassificationResult(
            novaGroup = novaGroup,
            confidence = confidence,
            markers = displayMarkers,
            explanation = explanation,
            highlightTerms = displayMarkers,
            engine = id,
        )
    }

    private fun buildAdditiveExplanation(markers: List<String>): String = when {
        markers.size >= 2 -> {
            val preview = markers.take(3).joinToString(", ")
            val suffix = if (markers.size > 3) ", …" else ""
            "Several industrial-style additives appear ($preview$suffix), typical of ultra-processed foods."
        }
        else ->
            "One additive linked to industrial formulations (${markers.first()}) suggests more than minimal processing."
    }

    private fun buildRetailExplanation(hits: List<String>, strong: Boolean): String {
        val preview = hits.take(3).joinToString(", ")
        val tail = if (hits.size > 3) ", …" else ""
        val base = "OCR picked up front-of-package or reheating claims ($preview$tail). " +
            "Those lines often describe factory-prepared foods even when the tiny ingredient panel was not read."
        return if (strong) {
            base + " For additive detail, re-scan the ingredient list in sharp light."
        } else {
            base + " Try capturing the ingredient panel only."
        }
    }

    private data class Quad(
        val nova: Int,
        val conf: Float,
        val displayMarkers: List<String>,
        val explanation: String,
    )

    companion object {
        private val ultraProcessedMarkers: List<String> = listOf(
            "high fructose corn syrup",
            "soy protein isolate",
            "whey protein isolate",
            "acesulfame potassium",
            "monosodium glutamate",
            "hydrogenated oil",
            "modified starch",
            "artificial flavor",
            "natural flavor",
            "maltodextrin",
            "sodium benzoate",
            "carrageenan",
            "sucralose",
            "aspartame",
            "polysorbate",
            "emulsifier",
            "stabilizer",
            "color added",
            "flavoring",
        ).sortedByDescending { it.length }

        /**
         * Phrases common on frozen / ready meals and burger boxes. OCR often reads the big panel,
         * not the fine-print ingredients—so we still flag likely ultra-processing.
         */
        private val retailPackagingCues: List<String> = listOf(
            "serving suggestion enlarged",
            "individually wrapped",
            "fully cooked",
            "keep frozen",
            "cheeseburger",
            "flame-broiled",
            "microwave",
        ).sortedByDescending { it.length }

        private val cookingOils = listOf(
            "sunflower oil",
            "palm oil",
            "vegetable oil",
            "canola oil",
            "corn oil",
            "soybean oil",
            "olive oil",
        )

        fun findUltraProcessedMarkers(normalizedLowercase: String): List<String> {
            val found = LinkedHashSet<String>()
            for (marker in ultraProcessedMarkers) {
                if (normalizedLowercase.contains(marker)) {
                    found.add(marker)
                }
            }
            return found.toList()
        }

        fun findRetailPackagingCues(normalizedLowercase: String): List<String> {
            val found = LinkedHashSet<String>()
            for (cue in retailPackagingCues) {
                if (normalizedLowercase.contains(cue)) {
                    found.add(cue)
                }
            }
            return found.toList()
        }

        fun matchesProcessedFoodHeuristic(normalizedLowercase: String): Boolean {
            if (!normalizedLowercase.contains("salt")) return false
            return cookingOils.any { normalizedLowercase.contains(it) }
        }
    }
}
