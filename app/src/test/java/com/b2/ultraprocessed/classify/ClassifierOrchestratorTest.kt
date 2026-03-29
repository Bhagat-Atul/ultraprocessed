package com.b2.ultraprocessed.classify

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ClassifierOrchestratorTest {
    private val input = IngredientInput(
        rawText = "Oats, dates, nuts",
        normalizedText = "Oats, dates, nuts",
    )

    @Test
    fun auto_prefersOnDeviceClassifier_whenConfiguredAndAvailable() = runTest {
        val orchestrator = ClassifierOrchestrator(
            rulesClassifier = RulesClassifier(),
            onDeviceClassifier = OnDeviceLLMClassifier { true },
            apiClassifier = ApiLLMClassifier {
                ClassificationResult(
                    novaGroup = 4,
                    confidence = 0.9f,
                    markers = listOf("network"),
                    explanation = "API fallback",
                    highlightTerms = listOf("network"),
                    engine = "api_llm",
                )
            },
        )

        val result = orchestrator.classify(
            input = input,
            context = ClassificationContext(
                allowNetwork = true,
                apiFallbackEnabled = true,
                preferOnDevice = true,
            ),
            mode = EngineMode.Auto,
        )

        assertEquals("on_device_llm", result.engine)
        assertEquals(3, result.novaGroup)
    }

    @Test
    fun auto_usesApiFallback_whenNetworkIsAllowed() = runTest {
        val orchestrator = ClassifierOrchestrator(
            rulesClassifier = RulesClassifier(),
            onDeviceClassifier = null,
            apiClassifier = ApiLLMClassifier {
                ClassificationResult(
                    novaGroup = 2,
                    confidence = 0.76f,
                    markers = listOf("api"),
                    explanation = "Remote classifier response",
                    highlightTerms = listOf("api"),
                    engine = "api_llm",
                )
            },
        )

        val result = orchestrator.classify(
            input = input,
            context = ClassificationContext(
                allowNetwork = true,
                apiFallbackEnabled = true,
                preferOnDevice = false,
            ),
            mode = EngineMode.Auto,
        )

        assertEquals("api_llm", result.engine)
        assertEquals(2, result.novaGroup)
    }

    @Test
    fun apiOnly_fallsBackToRules_whenApiClassifierIsUnavailable() = runTest {
        val orchestrator = ClassifierOrchestrator(
            rulesClassifier = RulesClassifier(),
            onDeviceClassifier = null,
            apiClassifier = null,
        )

        val result = orchestrator.classify(
            input = input,
            context = ClassificationContext(
                allowNetwork = true,
                apiFallbackEnabled = true,
                preferOnDevice = false,
            ),
            mode = EngineMode.ApiOnly,
        )

        assertEquals("rules", result.engine)
        assertEquals(1, result.novaGroup)
    }
}
