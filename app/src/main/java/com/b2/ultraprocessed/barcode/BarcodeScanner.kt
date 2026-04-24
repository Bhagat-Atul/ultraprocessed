package com.b2.ultraprocessed.barcode

fun interface BarcodeScanner {
    suspend fun scanFromImagePath(imagePath: String): BarcodeResult
}
