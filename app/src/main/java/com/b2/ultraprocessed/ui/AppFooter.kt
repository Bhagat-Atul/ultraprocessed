package com.b2.ultraprocessed.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppFooter(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag(AppTestTags.FOOTER),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "© The Benevolent Bandwidth Foundation, Inc. · Massachusetts Nonprofit Corporation. All rights reserved.",
            modifier = Modifier.widthIn(max = 320.dp),
            color = Color.White.copy(alpha = 0.15f),
            fontSize = 9.sp,
            lineHeight = 13.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Built with ",
                color = Color.White.copy(alpha = 0.16f),
                fontSize = 9.sp,
            )
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = Color.Red.copy(alpha = 0.4f),
                modifier = Modifier.size(10.dp),
            )
            Text(
                text = " for humanity",
                color = Color.White.copy(alpha = 0.16f),
                fontSize = 9.sp,
            )
        }
    }
}
