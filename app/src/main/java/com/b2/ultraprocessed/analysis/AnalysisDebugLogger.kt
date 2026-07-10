package com.b2.ultraprocessed.analysis

import android.content.Context
/**
 * Retained as a source-compatible no-op while diagnostics are archived.
 * Human OCR/model data must never be written to Logcat or an app file.
 */
object AnalysisDebugLogger {
    fun initialize(context: Context) {
        AnalysisTelemetry.sink = null
    }

    fun log(stage: String, message: String) = Unit

    fun clear(context: Context) = Unit
}
