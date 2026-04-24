package com.b2.ultraprocessed.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.camera.core.CameraSelector
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
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.b2.ultraprocessed.BuildConfig
import com.b2.ultraprocessed.barcode.BarcodeLiveScanController
import com.b2.ultraprocessed.camera.CameraCaptureController
import com.b2.ultraprocessed.camera.LocalImageImportController
import com.b2.ultraprocessed.ui.theme.Amber400
import com.b2.ultraprocessed.ui.theme.Amber500
import com.b2.ultraprocessed.ui.theme.DarkBg
import com.b2.ultraprocessed.ui.theme.DarkerBg
import com.b2.ultraprocessed.ui.theme.Emerald400
import com.b2.ultraprocessed.ui.theme.Emerald500

private enum class ScannerMode {
    /** Preview + still capture for ingredient OCR path. */
    Label,
    /** Preview + ML Kit live barcode → USDA path. */
    BarcodeLive,
}

@Composable
fun ScannerScreen(
    hasApiKey: Boolean,
    enableLiveCamera: Boolean = true,
    onScan: (String) -> Unit,
    onBarcodeScanned: (String) -> Unit,
    onTryDemo: () -> Unit,
    onSettings: () -> Unit,
    onHistory: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraController = remember(context) { CameraCaptureController(context) }
    val barcodeLiveController = remember(context) { BarcodeLiveScanController(context) }
    val importController = remember(context) { LocalImageImportController(context) }
    var scannerMode by remember { mutableStateOf(ScannerMode.Label) }
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
    /** False until CameraX label bind completes; avoids capture while [ImageCapture] is still null. */
    var isCameraPipelineReady by remember { mutableStateOf(false) }
    /** False until live barcode analysis use case is bound. */
    var isBarcodeLiveReady by remember { mutableStateOf(false) }
    val hasUsdaKey = remember { BuildConfig.USDA_API_KEY.isNotBlank() }
    var useFrontCamera by rememberSaveable { mutableStateOf(false) }
    val cameraSelector = remember(useFrontCamera) {
        if (useFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
    }
    val hasFrontCamera = remember {
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)
    }
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
                scannerMode = ScannerMode.Label
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
                isCameraPipelineReady = false
                isBarcodeLiveReady = false
                cameraController.unbind()
                barcodeLiveController.unbind()
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

    LaunchedEffect(Unit) {
        if (enableLiveCamera && !hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(scannerMode) {
        when (scannerMode) {
            ScannerMode.Label -> isBarcodeLiveReady = false
            ScannerMode.BarcodeLive -> isCameraPipelineReady = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
    ) {
        AppHeader(
            title = AppBrand.name,
            subtitle = when (scannerMode) {
                ScannerMode.Label -> "Live scanner"
                ScannerMode.BarcodeLive -> "Barcode scanner"
            },
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

        if (!hasUsdaKey) {
            Surface(
                color = Amber500.copy(alpha = 0.1f),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Amber500.copy(alpha = 0.22f),
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
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
                        text = "Add USDA_API_KEY in local.properties for barcode → USDA product lookup.",
                        color = Amber400.copy(alpha = 0.84f),
                        fontSize = 12.sp,
                    )
                }
            }
        }

        if (!hasApiKey) {
            Surface(
                onClick = onSettings,
                color = Amber500.copy(alpha = 0.08f),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Amber500.copy(alpha = 0.18f),
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Amber400.copy(alpha = 0.7f), CircleShape),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Optional: add an API key in Settings for future cloud models.",
                        color = Amber400.copy(alpha = 0.72f),
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
                        key(scannerMode, useFrontCamera) {
                            AndroidView(
                                factory = { viewContext ->
                                    when (scannerMode) {
                                        ScannerMode.Label -> barcodeLiveController.unbind()
                                        ScannerMode.BarcodeLive -> cameraController.unbind()
                                    }
                                    PreviewView(viewContext).apply {
                                        scaleType = PreviewView.ScaleType.FILL_CENTER
                                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                                        when (scannerMode) {
                                            ScannerMode.Label -> {
                                                cameraController.bind(
                                                    previewView = this,
                                                    lifecycleOwner = lifecycleOwner,
                                                    cameraSelector = cameraSelector,
                                                    onBound = { isCameraPipelineReady = true },
                                                )
                                            }
                                            ScannerMode.BarcodeLive -> {
                                                barcodeLiveController.bind(
                                                    previewView = this,
                                                    lifecycleOwner = lifecycleOwner,
                                                    onBarcodeDetected = onBarcodeScanned,
                                                    cameraSelector = cameraSelector,
                                                    onBound = { isBarcodeLiveReady = true },
                                                )
                                            }
                                        }
                                    }
                                },
                                update = {
                                    if (scannerMode == ScannerMode.BarcodeLive) {
                                        barcodeLiveController.updateBarcodeCallback(onBarcodeScanned)
                                    }
                                },
                                // Do not call bind() from [update]: scan-line animation recomposes every frame.
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag(AppTestTags.SCANNER_PREVIEW),
                            )
                        }
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

                if (enableLiveCamera && hasCameraPermission && hasFrontCamera) {
                    IconButton(
                        onClick = {
                            isCameraPipelineReady = false
                            isBarcodeLiveReady = false
                            useFrontCamera = !useFrontCamera
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 22.dp, bottom = 44.dp)
                            .testTag(AppTestTags.SCANNER_FLIP_CAMERA),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Cameraswitch,
                            contentDescription = "Switch between front and back camera",
                            tint = Color.White.copy(alpha = 0.88f),
                        )
                    }
                }
            }

            Surface(
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(100.dp),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 340.dp),
            ) {
                Text(
                    text = when (scannerMode) {
                        ScannerMode.Label -> if (useFrontCamera) {
                            "Front camera · ML Kit OCR"
                        } else {
                            "CameraX Preview · ML Kit OCR Ready"
                        }
                        ScannerMode.BarcodeLive -> if (useFrontCamera) {
                            "Front camera · live barcode"
                        } else {
                            "Live barcode · USDA product lookup"
                        }
                    },
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
                text = when (scannerMode) {
                    ScannerMode.Label -> "AIM AT THE INGREDIENT LABEL"
                    ScannerMode.BarcodeLive -> "AIM AT THE PRODUCT BARCODE"
                },
                color = Color.White.copy(alpha = 0.35f),
                fontSize = 11.sp,
                letterSpacing = 2.sp,
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (scannerMode == ScannerMode.BarcodeLive) {
                Surface(
                    onClick = {
                        scannerMode = ScannerMode.Label
                        cameraStatusMessage = null
                    },
                    color = Color.White.copy(alpha = 0.06f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag(AppTestTags.SCANNER_CAPTURE_BUTTON),
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Back to label scan",
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            } else {
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

                        if (!isCameraPipelineReady) {
                            cameraStatusMessage = "Camera is still starting. Try again in a moment."
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
                    enabled = !isCapturing && (!enableLiveCamera || hasCameraPermission),
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
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (scannerMode == ScannerMode.Label) {
                Surface(
                    onClick = {
                        if (!enableLiveCamera) {
                            onBarcodeScanned("078742195760")
                            return@Surface
                        }
                        if (!hasCameraPermission) {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                            return@Surface
                        }
                        cameraStatusMessage = null
                        scannerMode = ScannerMode.BarcodeLive
                        if (!hasUsdaKey) {
                            cameraStatusMessage =
                                "USDA_API_KEY missing in local.properties — barcode lookup will not find products."
                        }
                    },
                    color = Color.White.copy(alpha = 0.04f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(AppTestTags.SCANNER_BARCODE_BUTTON),
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.55f),
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Scan Barcode",
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = 12.sp,
                        )
                    }
                }
            } else {
                Surface(
                    color = Color.White.copy(alpha = 0.04f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(AppTestTags.SCANNER_BARCODE_BUTTON),
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = when {
                                !isBarcodeLiveReady -> "Starting barcode camera…"
                                else -> "Listening for barcode…"
                            },
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = 12.sp,
                        )
                    }
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
                    onClick = {
                        scannerMode = ScannerMode.Label
                        onTryDemo()
                    },
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
