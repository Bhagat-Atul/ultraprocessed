package com.b2.ultraprocessed.analysis

object AnalysisTelemetry {
    @Volatile
    var sink: ((String) -> Unit)? = null

    fun markStart(): Long = System.currentTimeMillis()

    fun stageSucceeded(stage: String, startedAtMillis: Long) {
        log("stage=$stage status=success durationMs=${elapsedSince(startedAtMillis)}")
    }

    fun stageFailed(stage: String, startedAtMillis: Long, category: String) {
        log("stage=$stage status=failure category=$category durationMs=${elapsedSince(startedAtMillis)}")
    }

    fun event(name: String) {
        log("event=$name")
    }

    private fun elapsedSince(startedAtMillis: Long): Long =
        System.currentTimeMillis() - startedAtMillis

    private fun log(message: String) {
        val formatted = "ZestAnalysis $message"
        sink?.invoke(formatted)
    }
}
