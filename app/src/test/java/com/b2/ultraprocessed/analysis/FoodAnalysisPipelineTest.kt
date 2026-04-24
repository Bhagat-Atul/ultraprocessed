package com.b2.ultraprocessed.analysis

import com.b2.ultraprocessed.barcode.BarcodeResult
import com.b2.ultraprocessed.barcode.BarcodeScanner
import com.b2.ultraprocessed.classify.ClassifierOrchestrator
import com.b2.ultraprocessed.classify.RulesClassifier
import com.b2.ultraprocessed.network.usda.UsdaApiDataSource
import com.b2.ultraprocessed.network.usda.UsdaFoodDetail
import com.b2.ultraprocessed.network.usda.UsdaRepository
import com.b2.ultraprocessed.network.usda.UsdaSearchFood
import com.b2.ultraprocessed.ocr.OcrPipeline
import com.b2.ultraprocessed.ocr.OcrResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FoodAnalysisPipelineTest {
    @Test
    fun analyzeFromBarcode_producesReportFromUsdaIngredients() = runTest {
        val pipeline = buildPipeline(
            ocrPipeline = OcrPipeline { OcrResult.Failure("unused") },
            barcodeScanner = BarcodeScanner { BarcodeResult.Success("078742195760") },
            usdaRepository = UsdaRepository(
                object : UsdaApiDataSource {
                    override suspend fun searchFoods(query: String, pageSize: Int): List<UsdaSearchFood> = listOf(
                        UsdaSearchFood(
                            fdcId = 100L,
                            description = "Frozen Cheeseburger",
                            dataType = "Branded",
                            brandOwner = "Great Value",
                            gtinUpc = "078742195760",
                            ingredients = "BEEF, BUN, ARTIFICIAL FLAVOR",
                        ),
                    )

                    override suspend fun fetchFoodDetail(fdcId: Long): UsdaFoodDetail? = UsdaFoodDetail(
                        fdcId = 100L,
                        description = "Frozen Cheeseburger",
                        brandOwner = "Great Value",
                        gtinUpc = "078742195760",
                        ingredients = "BEEF, BUN, ARTIFICIAL FLAVOR",
                    )
                },
            ),
        )

        val result = pipeline.analyzeFromBarcodeImage("/tmp/fake-image.jpg").getOrThrow()
        assertEquals(AnalysisSourceType.Barcode, result.sourceType)
        assertEquals("Barcode → USDA", result.scanResult.sourceLabel)
    }

    @Test
    fun analyzeFromBarcode_withRawCode_samePathAsImageBarcodeFlow() = runTest {
        val pipeline = buildPipeline(
            ocrPipeline = OcrPipeline { OcrResult.Failure("unused") },
            barcodeScanner = BarcodeScanner { BarcodeResult.Failure("unused") },
            usdaRepository = UsdaRepository(
                object : UsdaApiDataSource {
                    override suspend fun searchFoods(query: String, pageSize: Int): List<UsdaSearchFood> = listOf(
                        UsdaSearchFood(
                            fdcId = 100L,
                            description = "Frozen Cheeseburger",
                            dataType = "Branded",
                            brandOwner = "Great Value",
                            gtinUpc = "078742195760",
                            ingredients = "BEEF, BUN, ARTIFICIAL FLAVOR",
                        ),
                    )

                    override suspend fun fetchFoodDetail(fdcId: Long): UsdaFoodDetail? = UsdaFoodDetail(
                        fdcId = 100L,
                        description = "Frozen Cheeseburger",
                        brandOwner = "Great Value",
                        gtinUpc = "078742195760",
                        ingredients = "BEEF, BUN, ARTIFICIAL FLAVOR",
                    )
                },
            ),
        )

        val result = pipeline.analyzeFromBarcode("078742195760", sourceImagePath = null).getOrThrow()
        assertEquals(AnalysisSourceType.Barcode, result.sourceType)
        assertEquals("Barcode → USDA", result.scanResult.sourceLabel)
        assertEquals("078742195760", result.scanResult.scannedBarcode)
        assertEquals("Great Value", result.scanResult.brandOwner)
        assertFalse(result.scanResult.isBarcodeLookupOnly)
        assertTrue(result.scanResult.allIngredients.isNotEmpty())
    }

    @Test
    fun analyzeFromBarcode_fallsBackToOcr_whenUsdaMisses() = runTest {
        val pipeline = buildPipeline(
            ocrPipeline = OcrPipeline {
                OcrResult.Success("Ingredients: corn, salt, sunflower oil")
            },
            barcodeScanner = BarcodeScanner { BarcodeResult.Success("999999") },
            usdaRepository = UsdaRepository(
                object : UsdaApiDataSource {
                    override suspend fun searchFoods(query: String, pageSize: Int): List<UsdaSearchFood> = emptyList()
                    override suspend fun fetchFoodDetail(fdcId: Long): UsdaFoodDetail? = null
                },
            ),
        )

        val result = pipeline.analyzeFromBarcodeImage("/tmp/fake-image.jpg").getOrThrow()
        assertEquals(AnalysisSourceType.UsdaPlusOcr, result.sourceType)
        assertTrue(result.scanResult.warnings.isNotEmpty())
    }

    @Test
    fun analyzeFromImage_returnsFailure_whenNoOcrText() = runTest {
        val pipeline = buildPipeline(
            ocrPipeline = OcrPipeline { OcrResult.Failure("No text detected in image.") },
            barcodeScanner = BarcodeScanner { BarcodeResult.Failure("unused") },
            usdaRepository = UsdaRepository(
                object : UsdaApiDataSource {
                    override suspend fun searchFoods(query: String, pageSize: Int): List<UsdaSearchFood> = emptyList()
                    override suspend fun fetchFoodDetail(fdcId: Long): UsdaFoodDetail? = null
                },
            ),
        )

        val result = pipeline.analyzeFromImage("/tmp/fake-image.jpg")
        assertTrue(result.isFailure)
    }

}

private fun buildPipeline(
    ocrPipeline: OcrPipeline,
    barcodeScanner: BarcodeScanner,
    usdaRepository: UsdaRepository,
): FoodAnalysisPipeline = FoodAnalysisPipeline(
    ocrPipeline = ocrPipeline,
    barcodeScanner = barcodeScanner,
    usdaRepository = usdaRepository,
    orchestrator = ClassifierOrchestrator(
        rulesClassifier = RulesClassifier(),
        onDeviceClassifier = null,
        apiClassifier = null,
    ),
)
