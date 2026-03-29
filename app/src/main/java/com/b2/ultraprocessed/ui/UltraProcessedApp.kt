package com.b2.ultraprocessed.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.b2.ultraprocessed.ui.theme.DarkBg

data class AppTimingConfig(
    val splashDurationMillis: Long = 4200L,
    val analysisDurationMillis: Long = 3500L,
)

@Composable
fun UltraProcessedApp(
    timingConfig: AppTimingConfig = AppTimingConfig(),
    enableLiveCamera: Boolean = true,
) {
    var destination by rememberSaveable { mutableStateOf(AppDestination.Splash) }
    var apiKey by rememberSaveable { mutableStateOf("") }
    var selectedModelId by rememberSaveable {
        mutableStateOf(StubUiData.modelOptions.first().id)
    }
    var currentResultIndex by rememberSaveable { mutableIntStateOf(0) }
    var lastCapturedPhotoPath by rememberSaveable { mutableStateOf<String?>(null) }
    val historyItems = remember {
        mutableStateListOf<HistoryItemUi>().apply {
            addAll(StubUiData.initialHistory())
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBg,
    ) {
        AnimatedContent(
            targetState = destination,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "destination-animation",
        ) { screen ->
            when (screen) {
                AppDestination.Splash -> SplashScreen(
                    displayDurationMillis = timingConfig.splashDurationMillis,
                    onComplete = { destination = AppDestination.Scanner },
                )

                AppDestination.Scanner -> ScannerScreen(
                    hasApiKey = apiKey.isNotBlank(),
                    enableLiveCamera = enableLiveCamera,
                    onScan = { path ->
                        lastCapturedPhotoPath = path
                        destination = AppDestination.Analyzing
                    },
                    onTryDemo = {
                        lastCapturedPhotoPath = null
                        destination = AppDestination.Analyzing
                    },
                    onSettings = { destination = AppDestination.Settings },
                    onHistory = { destination = AppDestination.History },
                )

                AppDestination.Analyzing -> AnalyzingScreen(
                    modelName = StubUiData.modelOptions
                        .firstOrNull { it.id == selectedModelId }
                        ?.name
                        ?: selectedModelId,
                    displayDurationMillis = timingConfig.analysisDurationMillis,
                    onComplete = {
                        val result =
                            StubUiData.results[currentResultIndex % StubUiData.results.size]
                        historyItems.add(
                            0,
                            HistoryItemUi(
                                id = "scan-${System.currentTimeMillis()}",
                                productName = result.productName,
                                novaGroup = result.novaGroup,
                                scannedAt = "Just now",
                                summary = if (lastCapturedPhotoPath != null) {
                                    "${result.summary} Captured image stored locally."
                                } else {
                                    result.summary
                                },
                                capturedImagePath = lastCapturedPhotoPath,
                            ),
                        )
                        currentResultIndex =
                            (currentResultIndex + 1) % StubUiData.results.size
                        destination = AppDestination.Results
                    },
                )

                AppDestination.Results -> ResultsScreen(
                    result = StubUiData.results[
                        (currentResultIndex + StubUiData.results.size - 1) %
                            StubUiData.results.size
                    ],
                    onScanAgain = { destination = AppDestination.Scanner },
                    onOpenHistory = { destination = AppDestination.History },
                )

                AppDestination.Settings -> SettingsScreen(
                    apiKey = apiKey,
                    selectedModelId = selectedModelId,
                    modelOptions = StubUiData.modelOptions,
                    onBack = { destination = AppDestination.Scanner },
                    onApiKeySaved = { apiKey = it },
                    onModelSelected = { selectedModelId = it },
                )

                AppDestination.History -> HistoryScreen(
                    historyItems = historyItems,
                    onBack = { destination = AppDestination.Scanner },
                    onClearItem = { item -> historyItems.remove(item) },
                )
            }
        }
    }
}
