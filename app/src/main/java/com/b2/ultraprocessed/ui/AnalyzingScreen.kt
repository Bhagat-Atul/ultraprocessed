package com.b2.ultraprocessed.ui

import com.b2.ultraprocessed.analysis.FoodAnalysisPipeline
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.b2.ultraprocessed.ui.theme.DarkBg
import com.b2.ultraprocessed.ui.theme.Emerald400
import com.b2.ultraprocessed.ui.theme.Emerald500
import com.b2.ultraprocessed.ui.theme.Emerald600
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private data class Step(val text: String, val tag: String)

enum class AnalysisMode {
    /** Photo of ingredient label → OCR → normalize → classify (same downstream as barcode). */
    LabelImage,
    /** Photo still → read barcode from image → USDA → classify. */
    BarcodeImage,
    /** Live (or stub) barcode value → USDA → normalize → classify — parallel entry to OCR path. */
    BarcodeValue,
    DemoAsset,
}

private fun analysisSteps(
    modelName: String,
    mode: AnalysisMode,
) = listOf(
    Step(
        when {
            mode == AnalysisMode.BarcodeImage -> "Reading barcode image…"
            mode == AnalysisMode.BarcodeValue -> "Barcode captured…"
            mode == AnalysisMode.DemoAsset -> "Loading sample image…"
            else -> "Reading label image…"
        },
        when (mode) {
            AnalysisMode.BarcodeImage -> "Barcode"
            AnalysisMode.BarcodeValue -> "ML Kit"
            AnalysisMode.DemoAsset -> "Demo"
            AnalysisMode.LabelImage -> "ML Kit"
        },
    ),
    Step(
        text = when (mode) {
            AnalysisMode.BarcodeImage, AnalysisMode.BarcodeValue -> "Looking up USDA product data…"
            else -> "Extracting ingredient text…"
        },
        tag = if (mode == AnalysisMode.LabelImage || mode == AnalysisMode.DemoAsset) "OCR" else "USDA",
    ),
    Step(
        text = when (mode) {
            AnalysisMode.BarcodeImage -> "Extracting fallback OCR text if needed…"
            AnalysisMode.BarcodeValue -> "Loading ingredient list from USDA…"
            else -> "Normalizing ingredient text…"
        },
        tag = when (mode) {
            AnalysisMode.BarcodeImage -> "OCR"
            AnalysisMode.BarcodeValue -> "USDA"
            else -> "Normalize"
        },
    ),
    Step("Running NOVA-style rules…", "Rules"),
    Step("Preparing verdict…", modelName),
)

@Composable
fun AnalyzingScreen(
    scanSessionId: Int,
    imagePath: String?,
    barcodeValue: String?,
    demoAssetPath: String?,
    mode: AnalysisMode,
    minimumDisplayMillis: Long = 2600L,
    modelName: String,
    onSuccess: (ScanResultUi) -> Unit,
    onFailure: (String) -> Unit,
) {
    val context = LocalContext.current
    val pipeline = remember(context) { FoodAnalysisPipeline.create(context) }
    val steps = remember(modelName, mode) {
        analysisSteps(modelName, mode)
    }
    var currentStep by remember { mutableIntStateOf(0) }
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(scanSessionId) {
        val stepJob = launch {
            while (isActive) {
                delay(450)
                currentStep = (currentStep + 1).coerceAtMost(steps.lastIndex)
            }
        }
        val progressJob = launch {
            while (isActive && progress < 92f) {
                delay(50)
                progress += 1.8f
            }
        }

        val startAt = System.currentTimeMillis()
        val outcome = runCatching {
            when {
                mode == AnalysisMode.DemoAsset && demoAssetPath != null ->
                    pipeline.analyzeFromDemoAsset(context, demoAssetPath).getOrThrow().scanResult
                mode == AnalysisMode.BarcodeValue && !barcodeValue.isNullOrBlank() ->
                    pipeline.analyzeFromBarcode(
                        barcodeCode = barcodeValue.trim(),
                        sourceImagePath = null,
                    ).getOrThrow().scanResult
                mode == AnalysisMode.BarcodeImage && imagePath != null ->
                    pipeline.analyzeFromBarcodeImage(imagePath).getOrThrow().scanResult
                mode == AnalysisMode.LabelImage && imagePath != null ->
                    pipeline.analyzeFromImage(imagePath).getOrThrow().scanResult
                else ->
                    throw IllegalStateException("No scan input.")
            }
        }

        val elapsed = System.currentTimeMillis() - startAt
        val remaining = minimumDisplayMillis - elapsed
        if (remaining > 0) {
            delay(remaining)
        }

        stepJob.cancel()
        progressJob.cancel()
        progress = 100f
        currentStep = steps.lastIndex

        outcome.fold(
            onSuccess = { onSuccess(it) },
            onFailure = { e ->
                val msg = e.message?.takeIf { it.isNotBlank() }
                    ?: "Analysis failed. Please try again."
                val forUi = if (mode == AnalysisMode.BarcodeValue && !barcodeValue.isNullOrBlank()) {
                    val raw = barcodeValue.trim()
                    "$msg\n\nBarcode read by scanner: $raw"
                } else {
                    msg
                }
                onFailure(forUi)
            },
        )
    }

    val transition = rememberInfiniteTransition(label = "analysis-rings")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "rotation",
    )
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
    ) {
        AppHeader(
            title = "Analyzing",
            subtitle = modelName,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(148.dp)
                    .padding(bottom = 34.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(pulse)
                        .border(1.dp, Emerald500.copy(alpha = 0.12f), CircleShape),
                )
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .rotate(rotation)
                        .border(2.dp, Emerald500.copy(alpha = 0.26f), CircleShape),
                )
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .rotate(-rotation * 1.3f)
                        .border(2.dp, Emerald400.copy(alpha = 0.42f), CircleShape),
                )
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Emerald500.copy(alpha = 0.14f), RoundedCornerShape(16.dp))
                        .border(1.dp, Emerald500.copy(alpha = 0.22f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = AppBrand.monogram,
                        color = Emerald400,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .width(260.dp)
                    .padding(bottom = 24.dp),
            ) {
                LinearProgressIndicator(
                    progress = { (progress / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = Emerald600,
                    trackColor = Color.White.copy(alpha = 0.08f),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "ANALYZING",
                        color = Color.White.copy(alpha = 0.25f),
                        fontSize = 10.sp,
                        letterSpacing = 2.sp,
                    )
                    Text(
                        text = "${progress.toInt().coerceIn(0, 100)}%",
                        color = Emerald400.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                    )
                }
            }

            Text(
                text = steps[currentStep].text,
                color = Color.White.copy(alpha = 0.56f),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = Emerald500.copy(alpha = 0.1f),
                shape = RoundedCornerShape(100.dp),
            ) {
                Text(
                    text = steps[currentStep].tag,
                    color = Emerald400.copy(alpha = 0.7f),
                    fontSize = 9.sp,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                steps.forEachIndexed { index, _ ->
                    val active = index <= currentStep
                    Box(
                        modifier = Modifier
                            .width(if (active) 16.dp else 6.dp)
                            .height(4.dp)
                            .background(
                                if (active) Emerald400 else Color.White.copy(alpha = 0.1f),
                                CircleShape,
                            ),
                    )
                }
            }
        }

        AppFooter(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        )
    }
}
