package com.b2.ultraprocessed.ocr

/**
 * Extracts raw text from a food label image stored at [imagePath].
 */
fun interface OcrPipeline {
    suspend fun recognizeText(imagePath: String): OcrResult
}
