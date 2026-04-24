package com.b2.ultraprocessed.storage.secrets

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_results")
data class ScanResult(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Raw text pulled from the camera scan
    val ocrText: String,

    // Cleaned up ingredient list after processing
    val cleanedIngredients: String,

    // Final verdict e.g. "Ultra Processed", "Minimally Processed"
    val verdict: String,

    // How confident the engine is in its verdict (0.0 to 1.0)
    val confidenceScore: Float,

    // Specific markers that triggered the verdict e.g. "contains emulsifiers"
    val detectedMarkers: String,

    // Human readable explanation of the verdict
    val explanation: String,

    // Which engine produced this result e.g. "local", "openai"
    val engineUsed: String,

    // Timestamp of when the scan happened
    val scannedAt: Long = System.currentTimeMillis()
)

