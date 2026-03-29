package com.b2.ultraprocessed.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.b2.ultraprocessed.ui.theme.UltraProcessedTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            UltraProcessedTheme {
                UltraProcessedApp()
            }
        }
    }
}
