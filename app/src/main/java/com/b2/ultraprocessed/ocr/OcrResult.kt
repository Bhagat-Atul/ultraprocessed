package com.b2.ultraprocessed.ocr

/**
 * Outcome of on-device text recognition on a label image.
 */
sealed class OcrResult {
    data class Success(val rawText: String) : OcrResult()

    data class Failure(
        val message: String,
        val cause: Throwable? = null,
    ) : OcrResult()
}
