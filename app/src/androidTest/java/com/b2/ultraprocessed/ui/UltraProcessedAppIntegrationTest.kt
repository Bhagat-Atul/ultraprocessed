package com.b2.ultraprocessed.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
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
                        analysisMinimumDisplayMillis = 0L,
                    ),
                    enableLiveCamera = false,
                )
            }
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Live scanner").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Live scanner").assertIsDisplayed()
        composeRule.onNodeWithTag(AppTestTags.SCANNER_DEMO_BUTTON).performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Frozen cheeseburger box").fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithTag("${AppTestTags.DEMO_SAMPLE_ROW_PREFIX}cheeseburger").performClick()

        composeRule.waitUntil(timeoutMillis = 60_000) {
            composeRule.onAllNodesWithText("What this means").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("What this means").assertIsDisplayed()
        composeRule.onNodeWithText("Open History").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Scan history").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Scan history").assertIsDisplayed()
    }
}
