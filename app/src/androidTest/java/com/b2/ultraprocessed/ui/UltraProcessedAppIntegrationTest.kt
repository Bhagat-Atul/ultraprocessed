package com.b2.ultraprocessed.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.b2.ultraprocessed.ui.theme.UltraProcessedTheme
import org.junit.Rule
import org.junit.Test

class UltraProcessedAppIntegrationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun demoFlow_movesFromSplashToScannerToResultsToHistory() {
        composeRule.setContent {
            UltraProcessedTheme {
                UltraProcessedApp(
                    timingConfig = AppTimingConfig(
                        splashDurationMillis = 0L,
                        analysisDurationMillis = 0L,
                    ),
                    enableLiveCamera = false,
                )
            }
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Live scanner").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Live scanner").assertExists()
        composeRule.onNodeWithTag(AppTestTags.SCANNER_DEMO_BUTTON).performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Scan result").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Scan result").assertExists()
        composeRule.onNodeWithText("Open History").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Scan history").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Scan history").assertExists()
    }
}
