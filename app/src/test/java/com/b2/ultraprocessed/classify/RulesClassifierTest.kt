package com.b2.ultraprocessed.classify

import com.b2.ultraprocessed.ingredients.IngredientTextNormalizer
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
    fun classify_returnsNovaFour_whenMultipleUltraProcessedMarkersExist() = runTest {
        val raw = "Sugar, Maltodextrin, Natural Flavor, Color Added"
        val normalized = IngredientTextNormalizer.normalize(raw)
        val result = classifier.classify(
            input = IngredientInput(rawText = raw, normalizedText = normalized),
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
    fun classify_returnsNovaOne_whenNoMarkersAndNoProcessedHeuristic() = runTest {
        val raw = "Ingredients: oats, almonds, dates"
        val normalized = IngredientTextNormalizer.normalize(raw)
        val result = classifier.classify(
            input = IngredientInput(rawText = raw, normalizedText = normalized),
            context = context,
        )

        assertEquals(1, result.novaGroup)
        assertEquals("rules", result.engine)
        assertTrue(result.markers.isEmpty())
        assertEquals(0.62f, result.confidence, 0.0001f)
    }

    @Test
    fun classify_returnsNovaThree_whenSingleMarker() = runTest {
        val raw = "whole grain oats, cane sugar, natural flavor, sea salt"
        val normalized = IngredientTextNormalizer.normalize(raw)
        val result = classifier.classify(
            input = IngredientInput(rawText = raw, normalizedText = normalized),
            context = context,
        )

        assertEquals(3, result.novaGroup)
        assertEquals(1, result.markers.size)
        assertTrue(result.markers.contains("natural flavor"))
        assertEquals(0.56f, result.confidence, 0.0001f)
    }

    @Test
    fun classify_returnsNovaThree_whenSaltAndCookingOilWithoutUpfMarkers() = runTest {
        val raw = "Ingredients: corn, salt, sunflower oil"
        val normalized = IngredientTextNormalizer.normalize(raw)
        val result = classifier.classify(
            input = IngredientInput(rawText = raw, normalizedText = normalized),
            context = context,
        )

        assertEquals(3, result.novaGroup)
        assertTrue(result.markers.isEmpty())
        assertEquals(0.58f, result.confidence, 0.0001f)
    }

    @Test
    fun classify_flagsRetailPackaging_whenOcrReadsFrontPanelNotIngredients() = runTest {
        val raw = "Cheeseburger 4 Count microwave in minutes fully cooked individually wrapped flame-broiled beef"
        val normalized = IngredientTextNormalizer.normalize(raw)
        val result = classifier.classify(
            input = IngredientInput(rawText = raw, normalizedText = normalized),
            context = context,
        )

        assertEquals(4, result.novaGroup)
        assertTrue(result.markers.any { it.contains("cheeseburger") || it.contains("microwave") })
        assertTrue(result.explanation.contains("OCR", ignoreCase = true))
    }
}
