package com.b2.ultraprocessed.classify

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RulesClassifierTest {
    private val classifier = RulesClassifier()
    private val context = ClassificationContext(
        allowNetwork = false,
        apiFallbackEnabled = false,
        preferOnDevice = false,
    )

    @Test
    fun classify_returnsNovaFour_whenUltraProcessedMarkersExist() = runTest {
        val result = classifier.classify(
            input = IngredientInput(
                rawText = "Sugar, Maltodextrin, Natural Flavor, Color Added",
                normalizedText = "Sugar, Maltodextrin, Natural Flavor, Color Added",
            ),
            context = context,
        )

        assertEquals(4, result.novaGroup)
        assertEquals("rules", result.engine)
        assertTrue(result.markers.contains("maltodextrin"))
        assertTrue(result.markers.contains("natural flavor"))
        assertTrue(result.markers.contains("color added"))
        assertTrue(result.confidence > 0.8f)
    }

    @Test
    fun classify_returnsNovaOne_whenMarkersAreAbsent() = runTest {
        val result = classifier.classify(
            input = IngredientInput(
                rawText = "Oats, almonds, dates, sea salt",
                normalizedText = "Oats, almonds, dates, sea salt",
            ),
            context = context,
        )

        assertEquals(1, result.novaGroup)
        assertEquals("rules", result.engine)
        assertTrue(result.markers.isEmpty())
        assertEquals(0.55f, result.confidence, 0.0001f)
    }
}
