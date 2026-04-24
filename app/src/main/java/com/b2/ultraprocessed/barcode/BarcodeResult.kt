package com.b2.ultraprocessed.barcode

sealed class BarcodeResult {
    data class Success(val code: String) : BarcodeResult()
    data class Failure(val message: String, val cause: Throwable? = null) : BarcodeResult()
}
