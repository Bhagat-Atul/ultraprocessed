package com.b2.ultraprocessed.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.b2.ultraprocessed.ui.theme.DarkBg
import com.b2.ultraprocessed.ui.theme.DarkerBg
import com.b2.ultraprocessed.ui.theme.Emerald500

@Composable
fun ResultsScreen(
    result: ScanResultUi,
    onScanAgain: () -> Unit,
    onOpenHistory: () -> Unit,
) {
    val scrollState = rememberScrollState()
    var showIngredients by remember { mutableStateOf(false) }
    val verdict = verdictColors(result.novaGroup)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
    ) {
        AppHeader(
            title = "Scan result",
            subtitle = result.productName,
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
            Surface(
                color = verdict.cardColor,
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, verdict.borderColor),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(verdict.pillColor, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = verdict.label.take(1),
                            color = verdict.textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = verdict.label,
                        color = verdict.textColor,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        text = "NOVA Group ${result.novaGroup} · ${verdict.subLabel}",
                        color = Color.White.copy(alpha = 0.32f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = result.productName,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = result.summary,
                color = Color.White.copy(alpha = 0.42f),
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "PROBLEM INGREDIENTS",
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            if (result.problemIngredients.isEmpty()) {
                Surface(
                    color = Color.White.copy(alpha = 0.04f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "No major red-flag ingredients were found in this stubbed scan.",
                        color = Color.White.copy(alpha = 0.65f),
                        modifier = Modifier.padding(16.dp),
                    )
                }
            } else {
                result.problemIngredients.forEach { ingredient ->
                    Surface(
                        color = Color.White.copy(alpha = 0.04f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(verdict.textColor.copy(alpha = 0.84f), CircleShape),
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = ingredient.name,
                                    color = Color.White.copy(alpha = 0.88f),
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            Text(
                                text = ingredient.reason,
                                color = Color.White.copy(alpha = 0.46f),
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                modifier = Modifier.padding(start = 18.dp, top = 6.dp),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                onClick = { showIngredients = !showIngredients },
                color = Color.White.copy(alpha = 0.04f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (showIngredients) {
                            "All Ingredients (${result.allIngredients.size})"
                        } else {
                            "All Ingredients (${result.allIngredients.size})"
                        },
                        color = Color.White.copy(alpha = 0.78f),
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (showIngredients) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = result.allIngredients.joinToString(", "),
                            color = Color.White.copy(alpha = 0.48f),
                            lineHeight = 20.sp,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "NOVA Classification: A food classification system that groups foods by the extent and purpose of processing. Group 1 (unprocessed) to Group 4 (ultra-processed).",
                color = Color.White.copy(alpha = 0.36f),
                fontSize = 12.sp,
                lineHeight = 18.sp,
            )

            Spacer(modifier = Modifier.height(40.dp))
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
                        text = "Scan Another Product",
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
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open History")
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
