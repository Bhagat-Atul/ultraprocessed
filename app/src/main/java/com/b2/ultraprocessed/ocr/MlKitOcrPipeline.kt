package com.b2.ultraprocessed.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MlKitOcrPipeline(
    context: Context,
) : OcrPipeline {
    private val appContext = context.applicationContext
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun recognizeText(imagePath: String): OcrResult = withContext(Dispatchers.Default) {
        val file = File(imagePath)
        if (!file.isFile || !file.canRead()) {
            return@withContext OcrResult.Failure("Image file not found or unreadable.")
        }

        try {
            val image = InputImage.fromFilePath(appContext, Uri.fromFile(file))
            val visionText = recognizer.process(image).await()
            val raw = visionText.text.trim()
            if (raw.isBlank()) {
                OcrResult.Failure("No text detected in image.")
            } else {
                OcrResult.Success(raw)
            }
        } catch (e: Exception) {
            OcrResult.Failure(e.message ?: "OCR failed.", e)
        }
    }
}
