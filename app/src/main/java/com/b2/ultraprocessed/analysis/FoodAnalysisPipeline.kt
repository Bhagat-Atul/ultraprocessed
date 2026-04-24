package com.b2.ultraprocessed.analysis

import android.content.Context
import com.b2.ultraprocessed.barcode.BarcodeResult
import com.b2.ultraprocessed.barcode.BarcodeScanner
import com.b2.ultraprocessed.barcode.MlKitBarcodeScanner
import com.b2.ultraprocessed.classify.ClassificationContext
import com.b2.ultraprocessed.classify.ClassifierOrchestrator
import com.b2.ultraprocessed.classify.EngineMode
import com.b2.ultraprocessed.classify.IngredientInput
import com.b2.ultraprocessed.classify.RulesClassifier
import com.b2.ultraprocessed.ingredients.IngredientTextNormalizer
import com.b2.ultraprocessed.network.usda.BuildConfigUsdaApiKeyProvider
import com.b2.ultraprocessed.network.usda.UsdaApiService
import com.b2.ultraprocessed.network.usda.UsdaRepository
import com.b2.ultraprocessed.ocr.MlKitOcrPipeline
import com.b2.ultraprocessed.ocr.OcrPipeline
import com.b2.ultraprocessed.ocr.OcrResult
import com.b2.ultraprocessed.ui.ClassificationUiMapper
import java.io.File

class FoodAnalysisPipeline(
    private val ocrPipeline: OcrPipeline,
    private val barcodeScanner: BarcodeScanner,
    private val usdaRepository: UsdaRepository,
    private val orchestrator: ClassifierOrchestrator,
) {
    suspend fun analyzeFromImage(imagePath: String): Result<AnalysisReport> {
        val ocr = ocrPipeline.recognizeText(imagePath)
        return when (ocr) {
            is OcrResult.Failure -> Result.failure(Exception(ocr.message))
            is OcrResult.Success -> classifyFromIngredientsText(
                rawText = ocr.rawText,
                sourceImagePath = imagePath,
                sourceLabel = "OCR",
                sourceType = AnalysisSourceType.Ocr,
                productNameOverride = null,
                warnings = emptyList(),
            )
        }
    }

    suspend fun analyzeFromDemoAsset(context: Context, assetPath: String): Result<AnalysisReport> {
        val safeName = assetPath.replace('/', '_')
        val cacheFile = File(context.cacheDir, "demo-asset-$safeName")
        return try {
            context.assets.open(assetPath).use { input ->
                cacheFile.outputStream().use { output -> input.copyTo(output) }
            }
            analyzeFromImage(cacheFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(Exception("Could not load sample image.", e))
        }
    }

    suspend fun analyzeFromBarcode(
        barcodeCode: String,
        sourceImagePath: String?,
    ): Result<AnalysisReport> {
        val usda = usdaRepository.lookupByBarcode(barcodeCode)
            ?: return fallbackToImageOrError(
                sourceImagePath = sourceImagePath,
                error = "No USDA match found for barcode $barcodeCode.",
            )

        val ingredients = usda.ingredientsText
        if (ingredients.isNullOrBlank()) {
            return fallbackToImageOrError(
                sourceImagePath = sourceImagePath,
                error = "USDA record found but no ingredient text was available.",
            )
        }

        return classifyFromIngredientsText(
            rawText = ingredients,
            sourceImagePath = sourceImagePath,
            sourceLabel = "Barcode → USDA",
            sourceType = AnalysisSourceType.Barcode,
            productNameOverride = usda.productName,
            warnings = emptyList(),
            scannedBarcode = barcodeCode.trim().takeIf { it.isNotEmpty() },
            brandOwner = usda.brandOwner,
        )
    }

    suspend fun analyzeFromBarcodeImage(imagePath: String): Result<AnalysisReport> {
        return when (val barcode = barcodeScanner.scanFromImagePath(imagePath)) {
            is BarcodeResult.Failure -> fallbackToImageOrError(
                sourceImagePath = imagePath,
                error = barcode.message,
            )
            is BarcodeResult.Success -> analyzeFromBarcode(barcode.code, sourceImagePath = imagePath)
        }
    }

    private suspend fun fallbackToImageOrError(
        sourceImagePath: String?,
        error: String,
    ): Result<AnalysisReport> {
        if (sourceImagePath != null) {
            val fallback = analyzeFromImage(sourceImagePath)
            if (fallback.isSuccess) {
                val report = fallback.getOrNull() ?: return fallback
                return Result.success(
                    report.copy(
                        sourceType = AnalysisSourceType.UsdaPlusOcr,
                        warnings = report.warnings + error + " Falling back to OCR ingredients.",
                        scanResult = report.scanResult.copy(
                            sourceLabel = "USDA+OCR",
                            warnings = report.scanResult.warnings + error +
                                " Falling back to OCR ingredients.",
                        ),
                    ),
                )
            }
        }
        return Result.failure(Exception(error))
    }

    private suspend fun classifyFromIngredientsText(
        rawText: String,
        sourceImagePath: String?,
        sourceLabel: String,
        sourceType: AnalysisSourceType,
        productNameOverride: String?,
        warnings: List<String>,
        scannedBarcode: String? = null,
        brandOwner: String? = null,
    ): Result<AnalysisReport> {
        val normalized = IngredientTextNormalizer.normalize(rawText)
        if (normalized.length < MIN_NORMALIZED_LENGTH) {
            return Result.failure(
                Exception("Could not read enough ingredient text. Please try again."),
            )
        }

        val classification = orchestrator.classify(
            input = IngredientInput(
                rawText = rawText,
                normalizedText = normalized,
            ),
            context = ClassificationContext(
                allowNetwork = false,
                apiFallbackEnabled = false,
                preferOnDevice = false,
            ),
            mode = EngineMode.Auto,
        )
        val scanResult = ClassificationUiMapper.toScanResultUi(
            classification = classification,
            normalizedIngredientLine = normalized,
            productNameOverride = productNameOverride,
            sourceLabel = sourceLabel,
            warnings = warnings,
            labelImagePath = sourceImagePath,
            scannedBarcode = scannedBarcode,
            brandOwner = brandOwner,
        )
        return Result.success(
            AnalysisReport(
                sourceType = sourceType,
                productName = scanResult.productName,
                ingredientsTextUsed = normalized,
                warnings = warnings,
                scanResult = scanResult,
            ),
        )
    }

    companion object {
        const val MIN_NORMALIZED_LENGTH: Int = 12

        fun create(context: Context): FoodAnalysisPipeline {
            val appContext = context.applicationContext
            return FoodAnalysisPipeline(
                ocrPipeline = MlKitOcrPipeline(appContext),
                barcodeScanner = MlKitBarcodeScanner(appContext),
                usdaRepository = UsdaRepository(
                    dataSource = UsdaApiService(
                        apiKeyProvider = BuildConfigUsdaApiKeyProvider(),
                    ),
                ),
                orchestrator = ClassifierOrchestrator(
                    rulesClassifier = RulesClassifier(),
                    onDeviceClassifier = null,
                    apiClassifier = null,
                ),
            )
        }
    }
}
