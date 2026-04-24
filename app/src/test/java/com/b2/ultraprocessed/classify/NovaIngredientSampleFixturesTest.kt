package com.b2.ultraprocessed.classify

import com.b2.ultraprocessed.ingredients.IngredientTextNormalizer
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Quick regression set for rules-based NOVA-style outcomes from plain ingredient strings.
 */
class NovaIngredientSampleFixturesTest {
    private val classifier = RulesClassifier()
    private val classificationContext = ClassificationContext(
        allowNetwork = false,
        apiFallbackEnabled = false,
        preferOnDevice = false,
    )

    @Test
    fun fixtureSamples_matchExpectedNovaGroups() = runTest {
        NovaIngredientSampleFixtures.all.forEach { sample ->
            val normalized = IngredientTextNormalizer.normalize(sample.rawLabel)
            val result = classifier.classify(
                input = IngredientInput(
                    rawText = sample.rawLabel,
                    normalizedText = normalized,
                ),
                context = classificationContext,
            )
            assertEquals(
                "Unexpected NOVA for: ${sample.rawLabel}",
                sample.expectedNovaGroup,
                result.novaGroup,
            )
        }
    }
}

data class NovaIngredientSample(
    val rawLabel: String,
    val expectedNovaGroup: Int,
)

object NovaIngredientSampleFixtures {
    val all = listOf(
        NovaIngredientSample(
            rawLabel = "Ingredients: oats, dates, almonds",
            expectedNovaGroup = 1,
        ),
        NovaIngredientSample(
            rawLabel = "Ingredients: corn, salt, sunflower oil",
            expectedNovaGroup = 3,
        ),
        NovaIngredientSample(
            rawLabel = "Ingredients: wheat flour, sugar, palm oil, emulsifier, natural flavor, color added",
            expectedNovaGroup = 4,
        ),
        NovaIngredientSample(
            rawLabel = "Ingredients: skim milk, cocoa, carrageenan, natural flavor, sucralose",
            expectedNovaGroup = 4,
        ),
    )
}
