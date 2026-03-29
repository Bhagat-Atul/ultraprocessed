package com.b2.ultraprocessed.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.b2.ultraprocessed.ui.theme.Emerald400
import com.b2.ultraprocessed.ui.theme.Emerald600

data class AppHeaderAction(
    val icon: ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit,
    val badgeVisible: Boolean = false,
    val testTag: String? = null,
)

@Composable
fun AppHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    navigationAction: AppHeaderAction? = null,
    actions: List<AppHeaderAction> = emptyList(),
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 12.dp)
            .testTag(AppTestTags.HEADER),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (navigationAction != null) {
                HeaderActionButton(
                    action = navigationAction.copy(
                        testTag = navigationAction.testTag ?: AppTestTags.HEADER_NAVIGATION,
                    ),
                )
                Spacer(modifier = Modifier.width(10.dp))
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Brush.linearGradient(listOf(Emerald400, Emerald600)),
                        RoundedCornerShape(14.dp),
                    )
                    .border(
                        1.dp,
                        Color.White.copy(alpha = 0.12f),
                        RoundedCornerShape(14.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = AppBrand.monogram,
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White.copy(alpha = 0.94f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = 0.48f),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            actions.forEach { action ->
                Spacer(modifier = Modifier.width(8.dp))
                HeaderActionButton(action = action)
            }
        }
    }
}

@Composable
private fun HeaderActionButton(action: AppHeaderAction) {
    Box {
        Surface(
            onClick = action.onClick,
            color = Color.White.copy(alpha = 0.04f),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.08f),
            ),
            modifier = Modifier
                .size(40.dp)
                .then(
                    if (action.testTag != null) {
                        Modifier.testTag(action.testTag)
                    } else {
                        Modifier
                    },
                ),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = action.contentDescription,
                    tint = Color.White.copy(alpha = 0.74f),
                    modifier = Modifier.padding(10.dp),
                )
            }
        }

        if (action.badgeVisible) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 3.dp, end = 3.dp)
                    .size(8.dp)
                    .background(Color(0xFFFBBF24), CircleShape)
                    .border(1.dp, Color(0xFF111318), CircleShape),
            )
        }
    }
}

fun backHeaderAction(onClick: () -> Unit): AppHeaderAction =
    AppHeaderAction(
        icon = Icons.AutoMirrored.Filled.ArrowBack,
        contentDescription = "Back",
        onClick = onClick,
        testTag = AppTestTags.HEADER_NAVIGATION,
    )
