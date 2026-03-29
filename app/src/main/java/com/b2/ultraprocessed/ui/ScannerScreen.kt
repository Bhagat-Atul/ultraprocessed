package com.b2.ultraprocessed.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import com.b2.ultraprocessed.camera.CameraCaptureController
import com.b2.ultraprocessed.camera.LocalImageImportController
import com.b2.ultraprocessed.ui.theme.Amber400
import com.b2.ultraprocessed.ui.theme.Amber500
import com.b2.ultraprocessed.ui.theme.DarkBg
import com.b2.ultraprocessed.ui.theme.DarkerBg
import com.b2.ultraprocessed.ui.theme.Emerald400
import com.b2.ultraprocessed.ui.theme.Emerald500

@Composable
fun ScannerScreen(
    hasApiKey: Boolean,
    enableLiveCamera: Boolean = true,
    onScan: (String) -> Unit,
    onTryDemo: () -> Unit,
    onSettings: () -> Unit,
    onHistory: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraController = remember(context) { CameraCaptureController(context) }
    val importController = remember(context) { LocalImageImportController(context) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA,
            ) == PackageManager.PERMISSION_GRANTED || !enableLiveCamera,
        )
    }
    var isCapturing by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }
    var cameraStatusMessage by remember { mutableStateOf<String?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasCameraPermission = granted
        cameraStatusMessage = if (granted) {
            null
        } else {
            "Camera permission is required for live scanning."
        }
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri == null) {
            cameraStatusMessage = "No photo selected."
            return@rememberLauncherForActivityResult
        }

        isImporting = true
        cameraStatusMessage = null
        importController.importFromUri(
            uri = uri,
            onSuccess = { capture ->
                isImporting = false
                onScan(capture.absolutePath)
            },
            onError = { throwable ->
                isImporting = false
                cameraStatusMessage = throwable.message ?: "Failed to import image."
            },
        )
    }

    DisposableEffect(lifecycleOwner, hasCameraPermission, enableLiveCamera) {
        onDispose {
            if (enableLiveCamera && hasCameraPermission) {
                cameraController.unbind()
            }
        }
    }

    val scannerTransition = rememberInfiniteTransition(label = "scanner-line")
    val scanProgress by scannerTransition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.88f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "scan-progress",
    )

    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (enableLiveCamera && !hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
    ) {
        AppHeader(
            title = AppBrand.name,
            subtitle = "Live scanner",
            actions = listOf(
                AppHeaderAction(
                    icon = Icons.Default.History,
                    contentDescription = "History",
                    onClick = onHistory,
                    testTag = AppTestTags.HEADER_ACTION_HISTORY,
                ),
                AppHeaderAction(
                    icon = Icons.Default.Settings,
                    contentDescription = "Settings",
                    onClick = onSettings,
                    badgeVisible = !hasApiKey,
                    testTag = AppTestTags.HEADER_ACTION_SETTINGS,
                ),
            ),
        )

        if (!hasApiKey) {
            Surface(
                onClick = onSettings,
                color = Amber500.copy(alpha = 0.1f),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Amber500.copy(alpha = 0.22f),
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Amber400, CircleShape),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Add your API key in Settings to enable live scanning",
                        color = Amber400.copy(alpha = 0.84f),
                        fontSize = 12.sp,
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(DarkBg, Color(0xFF101017), DarkBg),
                        ),
                    ),
            )

            Box(modifier = Modifier.size(284.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                        .clip(RoundedCornerShape(28.dp)),
                ) {
                    if (enableLiveCamera && hasCameraPermission) {
                        AndroidView(
                            factory = { viewContext ->
                                PreviewView(viewContext).apply {
                                    scaleType = PreviewView.ScaleType.FILL_CENTER
                                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                                    cameraController.bind(this, lifecycleOwner)
                                }
                            },
                            update = { previewView ->
                                cameraController.bind(previewView, lifecycleOwner)
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag(AppTestTags.SCANNER_PREVIEW),
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF11131A))
                                .testTag(AppTestTags.SCANNER_PREVIEW),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = if (enableLiveCamera) {
                                    "Camera access needed"
                                } else {
                                    "Scanner preview stub"
                                },
                                color = Color.White.copy(alpha = 0.82f),
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (enableLiveCamera) {
                                    cameraStatusMessage ?: "Enable camera permission to see the live feed."
                                } else {
                                    "Camera preview is intentionally disabled in this test-safe mode."
                                },
                                color = Color.White.copy(alpha = 0.38f),
                                fontSize = 12.sp,
                            )
                            if (enableLiveCamera) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Surface(
                                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                    color = Emerald500.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Emerald500.copy(alpha = 0.24f)),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = null,
                                            tint = Emerald400,
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Enable Camera",
                                            color = Emerald400,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                        .border(
                            width = 2.dp,
                            color = Emerald400.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(28.dp),
                        ),
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 26.dp)
                        .fillMaxHeight(scanProgress)
                        .align(Alignment.TopCenter),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color.Transparent, Emerald400, Color.Transparent),
                                ),
                            ),
                    )
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .align(Alignment.Center)
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(10.dp),
                        ),
                )
            }

            Surface(
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(100.dp),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 340.dp),
            ) {
                Text(
                    text = "CameraX Preview · ML Kit OCR Ready",
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 9.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "AIM AT THE INGREDIENT LABEL",
                color = Color.White.copy(alpha = 0.35f),
                fontSize = 11.sp,
                letterSpacing = 2.sp,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (!enableLiveCamera) {
                        onScan("stubbed://local-capture.jpg")
                        return@Button
                    }

                    if (!hasCameraPermission) {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                        return@Button
                    }

                    isCapturing = true
                    cameraStatusMessage = null
                    cameraController.capturePhoto(
                        onSuccess = { capture ->
                            isCapturing = false
                            onScan(capture.absolutePath)
                        },
                        onError = { throwable ->
                            isCapturing = false
                            cameraStatusMessage = throwable.message ?: "Failed to capture image."
                        },
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag(AppTestTags.SCANNER_CAPTURE_BUTTON),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                enabled = !isCapturing,
            ) {
                if (isCapturing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Capturing...",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                    )
                } else {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Scan Label",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(
                    onClick = {
                        if (isImporting) return@Surface
                        galleryLauncher.launch("image/*")
                    },
                    color = Color.White.copy(alpha = 0.04f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag(AppTestTags.SCANNER_UPLOAD_BUTTON),
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isImporting) "Importing..." else "Upload Photo",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                        )
                    }
                }

                Surface(
                    onClick = onTryDemo,
                    color = Color.White.copy(alpha = 0.04f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag(AppTestTags.SCANNER_DEMO_BUTTON),
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Try Demo",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                        )
                    }
                }
            }

            if (cameraStatusMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = cameraStatusMessage ?: "",
                    color = Amber400.copy(alpha = 0.84f),
                    fontSize = 11.sp,
                )
            } else if (hasCameraPermission) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (enableLiveCamera) {
                        "Captured and imported photos are stored locally on this device."
                    } else {
                        "Scanner actions are running in stub mode for testing."
                    },
                    color = Color.White.copy(alpha = 0.25f),
                    fontSize = 11.sp,
                )
            }
        }

        AppFooter(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        )

        Box(
            modifier = Modifier
                .height(20.dp)
                .fillMaxWidth()
                .background(DarkerBg),
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
