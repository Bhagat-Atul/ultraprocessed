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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.b2.ultraprocessed.ui.theme.DarkBg
import com.b2.ultraprocessed.ui.theme.Emerald400

@Composable
fun HistoryScreen(
    historyItems: List<HistoryItemUi>,
    onBack: () -> Unit,
    onClearItem: (HistoryItemUi) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
    ) {
        AppHeader(
            title = "History",
            subtitle = AppBrand.name,
            navigationAction = backHeaderAction(onBack),
        )

        Surface(
            color = Color.White.copy(alpha = 0.03f),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Stored locally via Room DB · Session only · No cloud sync",
                    color = Color.White.copy(alpha = 0.25f),
                    fontSize = 11.sp,
                )
            }
        }

        if (historyItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No scans yet. Run the demo flow from the scanner screen.",
                    color = Color.White.copy(alpha = 0.4f),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 20.dp,
                    vertical = 12.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(historyItems, key = { it.id }) { item ->
                    val verdict = when (item.novaGroup) {
                        1 -> Triple("PASS", Emerald400, Icons.Default.CheckCircle)
                        2, 3 -> Triple("CAUTION", Color(0xFFFBBF24), Icons.Default.Warning)
                        else -> Triple("AVOID", Color(0xFFF87171), Icons.Default.Warning)
                    }
                    Surface(
                        color = verdict.second.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, verdict.second.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(verdict.second.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(verdict.third, contentDescription = null, tint = verdict.second, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.productName,
                                    color = Color.White.copy(alpha = 0.78f),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "NOVA ${item.novaGroup} · ${item.scannedAt}",
                                    color = Color.White.copy(alpha = 0.25f),
                                    fontSize = 11.sp,
                                )
                                if (item.capturedImagePath != null) {
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "Saved image: ${item.capturedImagePath.substringAfterLast('/')}",
                                        color = Color.White.copy(alpha = 0.22f),
                                        fontSize = 10.sp,
                                    )
                                }
                            }
                            Text(
                                text = verdict.first,
                                color = verdict.second,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.4.sp,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                onClick = { onClearItem(item) },
                                color = Color.White.copy(alpha = 0.04f),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    Color.White.copy(alpha = 0.08f),
                                ),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete",
                                    tint = Color.White.copy(alpha = 0.4f),
                                    modifier = Modifier.padding(10.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        AppFooter(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        )
    }
}
