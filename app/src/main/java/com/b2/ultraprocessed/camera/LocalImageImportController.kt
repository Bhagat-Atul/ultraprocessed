package com.b2.ultraprocessed.camera

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocalImageImportController(
    private val context: Context,
) {
    fun importFromUri(
        uri: Uri,
        onSuccess: (StoredCapture) -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        runCatching {
            val outputDirectory = outputDirectory(context)
            if (!outputDirectory.exists()) {
                outputDirectory.mkdirs()
            }

            val extension = resolveExtension(uri)
            val timestamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
            val outputFile = File(outputDirectory, "zest-upload-$timestamp.$extension")

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                outputFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: throw IOException("Unable to open the selected image.")

            StoredCapture(
                absolutePath = outputFile.absolutePath,
                fileName = outputFile.name,
            )
        }.onSuccess(onSuccess).onFailure(onError)
    }

    private fun resolveExtension(uri: Uri): String {
        val displayName = context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null,
        )?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) {
                cursor.getString(index)
            } else {
                null
            }
        }

        val fileNameExtension = displayName
            ?.substringAfterLast('.', missingDelimiterValue = "")
            ?.lowercase()
            ?.takeIf { it.isNotBlank() }
        if (fileNameExtension != null) {
            return fileNameExtension
        }

        val mimeType = context.contentResolver.getType(uri)
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(mimeType)
            ?.lowercase()
            ?.takeIf { it.isNotBlank() }
            ?: "jpg"
    }

    private fun outputDirectory(context: Context): File {
        val externalPicturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File(externalPicturesDir ?: context.filesDir, "imports")
    }
}
