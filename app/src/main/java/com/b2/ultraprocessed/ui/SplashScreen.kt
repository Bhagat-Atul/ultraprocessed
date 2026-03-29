package com.b2.ultraprocessed.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.b2.ultraprocessed.ui.theme.DarkerBg
import com.b2.ultraprocessed.ui.theme.Emerald400
import com.b2.ultraprocessed.ui.theme.Emerald500
import com.b2.ultraprocessed.ui.theme.Emerald600
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    displayDurationMillis: Long = 4200L,
    onComplete: () -> Unit,
) {
    val pulse = rememberInfiniteTransition(label = "pulse")
    val scale by pulse.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "splash-scale",
    )

    LaunchedEffect(Unit) {
        delay(displayDurationMillis)
        onComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkerBg),
    ) {
        Box(
            modifier = Modifier
                .size(520.dp)
                .scale(scale)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Emerald500.copy(alpha = 0.14f), Color.Transparent),
                    ),
                ),
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 176.dp)
                .size(108.dp)
                .background(
                    Brush.linearGradient(listOf(Emerald400, Emerald600)),
                    RoundedCornerShape(32.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = AppBrand.monogram,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 46.sp,
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 112.dp, start = 28.dp, end = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = AppBrand.name,
                color = Color.White.copy(alpha = 0.94f),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 42.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = AppBrand.subtitle,
                color = Emerald400.copy(alpha = 0.82f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = AppBrand.loadingLine,
                modifier = Modifier.widthIn(max = 280.dp),
                color = Color.White.copy(alpha = 0.42f),
                fontSize = 14.sp,
                lineHeight = 21.sp,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(28.dp))

            Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Emerald400, CircleShape),
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AppFooter()
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "v1.0.0 · Android · Kotlin + Jetpack Compose",
                color = Color.White.copy(alpha = 0.12f),
                fontSize = 9.sp,
            )
        }
    }
}
