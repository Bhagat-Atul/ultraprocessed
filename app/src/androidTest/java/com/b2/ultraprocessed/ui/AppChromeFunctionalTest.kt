package com.b2.ultraprocessed.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.b2.ultraprocessed.ui.theme.UltraProcessedTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AppChromeFunctionalTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun scannerScreen_rendersSharedHeaderAndFooter_andRoutesHeaderActions() {
        var historyClicks = 0
        var settingsClicks = 0

        composeRule.setContent {
            UltraProcessedTheme {
                ScannerScreen(
                    hasApiKey = false,
                    enableLiveCamera = false,
                    onScan = {},
                    onTryDemo = {},
                    onSettings = { settingsClicks += 1 },
                    onHistory = { historyClicks += 1 },
                )
            }
        }

        composeRule.onNodeWithTag(AppTestTags.HEADER).assertExists()
        composeRule.onNodeWithTag(AppTestTags.FOOTER).assertExists()
        composeRule.onNodeWithText("Live scanner").assertExists()
        composeRule.onNodeWithTag(AppTestTags.HEADER_ACTION_HISTORY).performClick()
        composeRule.onNodeWithTag(AppTestTags.HEADER_ACTION_SETTINGS).performClick()

        composeRule.runOnIdle {
            assertEquals(1, historyClicks)
            assertEquals(1, settingsClicks)
        }
    }

    @Test
    fun resultsScreen_rendersSharedChrome() {
        composeRule.setContent {
            UltraProcessedTheme {
                ResultsScreen(
                    result = StubUiData.results.first(),
                    onScanAgain = {},
                    onOpenHistory = {},
                )
            }
        }

        composeRule.onNodeWithTag(AppTestTags.HEADER).assertExists()
        composeRule.onNodeWithTag(AppTestTags.FOOTER).assertExists()
        composeRule.onNodeWithText("Scan result").assertExists()
        composeRule.onNodeWithText("Strawberry Fruit Snacks").assertExists()
    }
}
