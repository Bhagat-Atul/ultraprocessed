package com.b2.ultraprocessed.analysis

import com.b2.ultraprocessed.ui.ScanResultUi

enum class AnalysisSourceType {
    Barcode,
    Ocr,
    UsdaPlusOcr,
}

data class AnalysisReport(
    val sourceType: AnalysisSourceType,
    val productName: String,
    val ingredientsTextUsed: String,
    val warnings: List<String>,
    val scanResult: ScanResultUi,
)
