package com.b2.ultraprocessed.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.b2.ultraprocessed.ui.theme.DarkBg
import com.b2.ultraprocessed.ui.theme.DarkerBg
import com.b2.ultraprocessed.ui.theme.Emerald500
import java.io.File

@Composable
fun ResultsScreen(
    result: ScanResultUi,
    onScanAgain: () -> Unit,
    onOpenHistory: () -> Unit,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
    ) {
        AppHeader(
            title = if (result.isBarcodeLookupOnly) "Product found" else "What this means",
            subtitle = if (result.isBarcodeLookupOnly) {
                "USDA FoodData Central · barcode lookup"
            } else {
                "From your ingredient label"
            },
            navigationAction = backHeaderAction(onScanAgain),
            actions = listOf(
                AppHeaderAction(
                    icon = Icons.Default.History,
                    contentDescription = "History",
                    onClick = onOpenHistory,
                    testTag = AppTestTags.HEADER_ACTION_HISTORY,
                ),
            ),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp),
        ) {
            if (result.isBarcodeLookupOnly) {
                BarcodeLookupResultBody(result = result)
            } else {
                FullAnalysisResultBody(result = result)
            }

            Spacer(modifier = Modifier.height(36.dp))
        }

        Surface(
            color = DarkerBg,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Button(
                    onClick = onScanAgain,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                ) {
                    Text(
                        text = if (result.isBarcodeLookupOnly) "Scan again" else "Scan another label",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onOpenHistory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Icon(Icons.Default.History, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open history")
                }

                Spacer(modifier = Modifier.height(16.dp))

                AppFooter()

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .width(112.dp)
                            .height(4.dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape),
                    )
                }
            }
        }
    }
}

@Composable
private fun BarcodeLookupResultBody(result: ScanResultUi) {
    Text(
        text = result.productName,
        color = Color.White.copy(alpha = 0.92f),
        fontSize = 22.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 28.sp,
    )
    if (!result.brandOwner.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = result.brandOwner.orEmpty(),
            color = Color.White.copy(alpha = 0.55f),
            fontSize = 14.sp,
            lineHeight = 20.sp,
        )
    }
    Spacer(modifier = Modifier.height(12.dp))
    if (!result.scannedBarcode.isNullOrBlank()) {
        Surface(
            color = Color.White.copy(alpha = 0.06f),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "BARCODE",
                    color = Color.White.copy(alpha = 0.35f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = result.scannedBarcode.orEmpty(),
                    color = Color.White.copy(alpha = 0.88f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp,
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(14.dp))
    Surface(
        color = Color.White.copy(alpha = 0.08f),
        shape = RoundedCornerShape(100.dp),
    ) {
        Text(
            text = "Source: ${result.sourceLabel}",
            color = Color.White.copy(alpha = 0.72f),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
    Spacer(modifier = Modifier.height(20.dp))
    Text(
        text = result.summary,
        color = Color.White.copy(alpha = 0.55f),
        fontSize = 15.sp,
        lineHeight = 22.sp,
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "Database reference, not medical advice. For a full label read, scan the ingredient panel with the camera.",
        color = Color.White.copy(alpha = 0.34f),
        fontSize = 11.sp,
        lineHeight = 16.sp,
    )
}

@Composable
private fun FullAnalysisResultBody(result: ScanResultUi) {
    var showIngredients by remember { mutableStateOf(true) }
    val verdict = verdictColors(result.novaGroup)
    val confidenceLabel = confidenceBandLabel(result.confidence)
    val headline = shopperHeadline(result.novaGroup)
    val emptyWatchListMessage = watchListEmptyCopy(result.novaGroup)
    val nextSteps = nextStepLines(result.novaGroup)

    Text(
        text = result.productName,
        color = Color.White.copy(alpha = 0.92f),
        fontSize = 17.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 24.sp,
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = "Rules-based read · not medical advice",
        color = Color.White.copy(alpha = 0.35f),
        fontSize = 11.sp,
        letterSpacing = 0.3.sp,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Surface(
        color = Color.White.copy(alpha = 0.08f),
        shape = RoundedCornerShape(100.dp),
    ) {
        Text(
            text = "Source: ${result.sourceLabel}",
            color = Color.White.copy(alpha = 0.72f),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
    if (!result.brandOwner.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Brand: ${result.brandOwner}",
            color = Color.White.copy(alpha = 0.42f),
            fontSize = 12.sp,
        )
    }
    if (!result.scannedBarcode.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Barcode: ${result.scannedBarcode}",
            color = Color.White.copy(alpha = 0.42f),
            fontSize = 12.sp,
        )
    }

    Spacer(modifier = Modifier.height(16.dp))
    ScannedLabelPhotoSection(imagePath = result.labelImagePath)

    Spacer(modifier = Modifier.height(18.dp))

    Surface(
        color = verdict.cardColor,
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, verdict.borderColor),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(verdict.pillColor, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = verdict.label.take(1),
                    color = verdict.textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = verdict.label,
                color = verdict.textColor,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = headline,
                color = Color.White.copy(alpha = 0.72f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp),
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "NOVA-style group ${result.novaGroup} · ${verdict.subLabel}",
                color = Color.White.copy(alpha = 0.32f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.6.sp,
            )
            Text(
                text = "$confidenceLabel signal (${(result.confidence * 100).toInt()}%) · ${result.engineLabel}",
                color = Color.White.copy(alpha = 0.28f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp),
            )
        }
    }

    Spacer(modifier = Modifier.height(22.dp))

    Text(
        text = "WHY YOU SEE THIS",
        color = Color.White.copy(alpha = 0.32f),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.4.sp,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = result.summary,
        color = Color.White.copy(alpha = 0.55f),
        fontSize = 15.sp,
        lineHeight = 22.sp,
    )

    if (result.warnings.isNotEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            color = Color(0x16F59E0B),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                Color(0x44F59E0B),
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "DATA WARNING",
                    color = Color(0xFFFBBF24),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                )
                Spacer(modifier = Modifier.height(6.dp))
                result.warnings.forEach { warning ->
                    Text(
                        text = warning,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(22.dp))

    Text(
        text = "WATCH LIST (ADDITIVE CLUES)",
        color = Color.White.copy(alpha = 0.32f),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.4.sp,
    )
    Spacer(modifier = Modifier.height(10.dp))

    if (result.problemIngredients.isEmpty()) {
        Surface(
            color = Color.White.copy(alpha = 0.05f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = emptyWatchListMessage,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                lineHeight = 21.sp,
                modifier = Modifier.padding(16.dp),
            )
        }
    } else {
        result.problemIngredients.forEach { ingredient ->
            Surface(
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(verdict.textColor.copy(alpha = 0.9f), CircleShape),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = ingredient.name,
                            color = Color.White.copy(alpha = 0.92f),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                        )
                    }
                    Text(
                        text = ingredient.reason,
                        color = Color.White.copy(alpha = 0.48f),
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        modifier = Modifier.padding(start = 18.dp, top = 6.dp),
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text(
        text = "YOUR NEXT MOVE",
        color = Color.White.copy(alpha = 0.32f),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.4.sp,
    )
    Spacer(modifier = Modifier.height(10.dp))
    nextSteps.forEach { line ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Start,
        ) {
            Text(
                text = "·",
                color = verdict.textColor.copy(alpha = 0.85f),
                fontSize = 16.sp,
                modifier = Modifier.padding(end = 10.dp),
            )
            Text(
                text = line,
                color = Color.White.copy(alpha = 0.62f),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                modifier = Modifier.weight(1f),
            )
        }
    }

    Spacer(modifier = Modifier.height(18.dp))

    Surface(
        onClick = { showIngredients = !showIngredients },
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Full ingredient list (${result.allIngredients.size})",
                    color = Color.White.copy(alpha = 0.82f),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                )
                Text(
                    text = if (showIngredients) "Hide" else "Show",
                    color = Emerald500.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            if (showIngredients) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = result.allIngredients.joinToString(", "),
                    color = Color.White.copy(alpha = 0.5f),
                    lineHeight = 22.sp,
                    fontSize = 14.sp,
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "NOVA groups foods by processing (1 = minimal → 4 = ultra-processed). This app uses on-device rules, not a full nutrition audit.",
        color = Color.White.copy(alpha = 0.34f),
        fontSize = 11.sp,
        lineHeight = 16.sp,
    )
}

@Composable
private fun ScannedLabelPhotoSection(imagePath: String?) {
    if (imagePath.isNullOrBlank()) return
    val file = File(imagePath)
    if (!file.isFile || !file.canRead()) return

    val bitmap = remember(imagePath) { decodeSampledBitmap(imagePath, maxSidePx = 960) }
    if (bitmap == null) return

    Text(
        text = "YOUR SCAN",
        color = Color.White.copy(alpha = 0.32f),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.4.sp,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Surface(
        color = Color.White.copy(alpha = 0.04f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Scanned label photo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
                    .clip(RoundedCornerShape(12.dp)),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "This is the image we ran through OCR. Big marketing text is read first—zoom the real ingredient list when you can.",
                color = Color.White.copy(alpha = 0.38f),
                fontSize = 11.sp,
                lineHeight = 15.sp,
            )
        }
    }
}

private fun decodeSampledBitmap(path: String, maxSidePx: Int): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    var sample = 1
    while (bounds.outWidth / sample > maxSidePx || bounds.outHeight / sample > maxSidePx) {
        sample *= 2
    }
    val opts = BitmapFactory.Options().apply { inSampleSize = sample }
    return BitmapFactory.decodeFile(path, opts)
}

private fun confidenceBandLabel(confidence: Float): String = when {
    confidence >= 0.75f -> "Strong"
    confidence >= 0.55f -> "Moderate"
    else -> "Low"
}

private fun shopperHeadline(novaGroup: Int): String = when (novaGroup) {
    1 -> "Likely a simpler choice—fewer industrial additives flagged on this list."
    2, 3 -> "Processed—compare with shorter lists and fewer additives when you can."
    else -> "High ultra-processing risk on this label—worth swapping if you want to cut UPFs."
}

private fun watchListEmptyCopy(novaGroup: Int): String = when (novaGroup) {
    1 ->
        "Nothing on our additive watch list matched. That usually means fewer " +
            "industrial-style ingredients—still check sugar, salt, and portions on the package."
    2, 3 ->
        "No classic ultra-processed additive hits, but the recipe still looks processed " +
            "(e.g. salt + oils or similar). Compare with a shorter list if you want simpler food."
    else ->
        "We expected markers for this group—if this looks wrong, rescan with clearer lighting " +
            "or check the full list below."
}

private fun nextStepLines(novaGroup: Int): List<String> = when (novaGroup) {
    1 -> listOf(
        "If the taste and price work for you, this is often an easier cart choice.",
        "Still read the nutrition panel for sugar, sodium, and serving size.",
        "Scan another similar product to see if another brand keeps the list shorter.",
    )
    2, 3 -> listOf(
        "Pick one alternative with fewer lines in the ingredient list.",
        "Prioritize items without emulsifiers, flavor systems, or long additive tails.",
        "When unsure, choose the product you understand every ingredient name on.",
    )
    else -> listOf(
        "Try a version with a shorter list or without industrial sweeteners and emulsifiers.",
        "Shop the perimeter for whole foods when you want to avoid NOVA 4 patterns.",
        "Rescan if the label was blurry—bad OCR can miss additives.",
    )
}

private data class VerdictPalette(
    val label: String,
    val subLabel: String,
    val cardColor: Color,
    val borderColor: Color,
    val pillColor: Color,
    val textColor: Color,
)

private fun verdictColors(novaGroup: Int): VerdictPalette =
    when (novaGroup) {
        1 -> VerdictPalette(
            label = "PASS",
            subLabel = "Minimally processed",
            cardColor = Color(0x1622C55E),
            borderColor = Color(0x4422C55E),
            pillColor = Color(0x2422C55E),
            textColor = Color(0xFF4ADE80),
        )
        2, 3 -> VerdictPalette(
            label = "CAUTION",
            subLabel = "Processed",
            cardColor = Color(0x16F59E0B),
            borderColor = Color(0x44F59E0B),
            pillColor = Color(0x24F59E0B),
            textColor = Color(0xFFFBBF24),
        )
        else -> VerdictPalette(
            label = "AVOID",
            subLabel = "Ultra-processed",
            cardColor = Color(0x16EF4444),
            borderColor = Color(0x44EF4444),
            pillColor = Color(0x24EF4444),
            textColor = Color(0xFFF87171),
        )
    }
