package com.b2.ultraprocessed.scan

import android.content.Context
import com.b2.ultraprocessed.classify.ClassificationContext
import com.b2.ultraprocessed.classify.ClassifierOrchestrator
import com.b2.ultraprocessed.classify.EngineMode
import com.b2.ultraprocessed.classify.IngredientInput
import com.b2.ultraprocessed.classify.RulesClassifier
import com.b2.ultraprocessed.ingredients.IngredientTextNormalizer
import com.b2.ultraprocessed.ocr.MlKitOcrPipeline
import com.b2.ultraprocessed.ocr.OcrPipeline
import com.b2.ultraprocessed.ocr.OcrResult
import com.b2.ultraprocessed.ui.ClassificationUiMapper
import com.b2.ultraprocessed.ui.ScanResultUi
import java.io.File

/**
 * End-to-end: image path or raw demo text → OCR (if needed) → normalize → [ClassifierOrchestrator] → UI model.
 */
class LabelScanPipeline(
    private val ocrPipeline: OcrPipeline,
    private val orchestrator: ClassifierOrchestrator,
) {
    suspend fun analyzeImage(imagePath: String): Result<ScanResultUi> {
        return when (val ocr = ocrPipeline.recognizeText(imagePath)) {
            is OcrResult.Failure -> Result.failure(Exception(ocr.message))
            is OcrResult.Success -> classifyFromRawText(ocr.rawText, sourceImagePath = imagePath)
        }
    }

    suspend fun analyzeDemoText(rawLabelText: String): Result<ScanResultUi> =
        classifyFromRawText(rawLabelText, sourceImagePath = null)

    /**
     * Copies a file from [context] assets (e.g. `demo_samples/demo_cherries.png`) to cache, then runs OCR.
     */
    suspend fun analyzeDemoAsset(context: Context, assetPath: String): Result<ScanResultUi> {
        val safeName = assetPath.replace('/', '_')
        val cacheFile = File(context.cacheDir, "demo-asset-$safeName")
        return try {
            context.assets.open(assetPath).use { input ->
                cacheFile.outputStream().use { output -> input.copyTo(output) }
            }
            analyzeImage(cacheFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(Exception("Could not load sample image.", e))
        }
    }

    private suspend fun classifyFromRawText(
        rawText: String,
        sourceImagePath: String?,
    ): Result<ScanResultUi> {
        val normalized = IngredientTextNormalizer.normalize(rawText)
        if (normalized.length < MIN_NORMALIZED_LENGTH) {
            return Result.failure(
                Exception("Could not read enough ingredient text. Please try again."),
            )
        }

        val input = IngredientInput(
            rawText = rawText,
            normalizedText = normalized,
        )

        val context = ClassificationContext(
            allowNetwork = false,
            apiFallbackEnabled = false,
            preferOnDevice = false,
        )

        val classification = orchestrator.classify(
            input = input,
            context = context,
            mode = EngineMode.Auto,
        )

        return Result.success(
            ClassificationUiMapper.toScanResultUi(
                classification = classification,
                normalizedIngredientLine = normalized,
                labelImagePath = sourceImagePath,
            ),
        )
    }

    companion object {
        const val MIN_NORMALIZED_LENGTH: Int = 12

        fun create(context: Context): LabelScanPipeline {
            val app = context.applicationContext
            val ocr = MlKitOcrPipeline(app)
            val orchestrator = ClassifierOrchestrator(
                rulesClassifier = RulesClassifier(),
                onDeviceClassifier = null,
                apiClassifier = null,
            )
            return LabelScanPipeline(ocr, orchestrator)
        }
    }
}
