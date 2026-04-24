package com.b2.ultraprocessed.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.b2.ultraprocessed.ui.theme.DarkBg
import com.b2.ultraprocessed.ui.theme.Emerald400
@Composable
fun DemoSamplePickerDialog(
    onDismiss: () -> Unit,
    onSampleSelected: (DemoImageSample) -> Unit,
) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = DarkBg,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(AppTestTags.DEMO_PICKER_DIALOG),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
            ) {
                Text(
                    text = "Try demo",
                    color = Color.White.copy(alpha = 0.95f),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Pick a sample image. We run real on-device OCR, then NOVA-style rules.",
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(
                    modifier = Modifier.height(320.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(DemoImageSamples.all, key = { it.id }) { sample ->
                        DemoSampleRow(
                            sample = sample,
                            context = context,
                            onClick = { onSampleSelected(sample) },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.textButtonColors(contentColor = Emerald400),
                ) {
                    Text("Cancel", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun DemoSampleRow(
    sample: DemoImageSample,
    context: android.content.Context,
    onClick: () -> Unit,
) {
    val bitmap = remember(sample.assetPath) {
        runCatching {
            context.assets.open(sample.assetPath).use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        }.getOrNull()
    }
    val thumb = remember(bitmap) {
        bitmap?.let { b ->
            val maxSide = 160
            val w = b.width
            val h = b.height
            if (w <= maxSide && h <= maxSide) b
            else {
                val scale = maxSide / maxOf(w, h).toFloat()
                val nw = (w * scale).toInt().coerceAtLeast(1)
                val nh = (h * scale).toInt().coerceAtLeast(1)
                Bitmap.createScaledBitmap(b, nw, nh, true)
            }
        }
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.06f),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("${AppTestTags.DEMO_SAMPLE_ROW_PREFIX}${sample.id}"),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (thumb != null) {
                Image(
                    bitmap = thumb.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(12.dp)),
                )
            } else {
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.08f),
                ) {}
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sample.title,
                    color = Color.White.copy(alpha = 0.92f),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = sample.subtitle,
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                )
            }
        }
    }
}
