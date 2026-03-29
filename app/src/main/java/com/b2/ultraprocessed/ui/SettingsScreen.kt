package com.b2.ultraprocessed.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.b2.ultraprocessed.ui.theme.DarkBg
import com.b2.ultraprocessed.ui.theme.Emerald400
import com.b2.ultraprocessed.ui.theme.Emerald500

@Composable
fun SettingsScreen(
    apiKey: String,
    selectedModelId: String,
    modelOptions: List<ModelOption>,
    onBack: () -> Unit,
    onApiKeySaved: (String) -> Unit,
    onModelSelected: (String) -> Unit,
) {
    var localKey by remember(apiKey) { mutableStateOf(apiKey) }
    var showKey by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    var showModelPicker by remember { mutableStateOf(false) }
    val selectedModel = modelOptions.firstOrNull { it.id == selectedModelId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
    ) {
        AppHeader(
            title = "Settings",
            subtitle = AppBrand.name,
            navigationAction = backHeaderAction(onBack),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            SectionHeader(icon = Icons.Default.Key, text = "API Key")
            Surface(
                color = Color.White.copy(alpha = 0.04f),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Your API key is stored locally in the Android Keystore and never leaves your device. It is used to call the LLM for ingredient classification.",
                        color = Color.White.copy(alpha = 0.45f),
                        lineHeight = 19.sp,
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = localKey,
                        onValueChange = {
                            localKey = it
                            saved = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("API key") },
                        singleLine = true,
                        visualTransformation = if (showKey) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            Row {
                                IconButton(onClick = { showKey = !showKey }) {
                                    Icon(
                                        imageVector = if (showKey) {
                                            Icons.Default.VisibilityOff
                                        } else {
                                            Icons.Default.Visibility
                                        },
                                        contentDescription = "Toggle visibility",
                                    )
                                }
                                if (localKey.isNotBlank()) {
                                    IconButton(
                                        onClick = {
                                            localKey = ""
                                            onApiKeySaved("")
                                            saved = false
                                        },
                                    ) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Emerald500,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White.copy(alpha = 0.84f),
                            focusedLabelColor = Emerald400,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                        ),
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                onApiKeySaved(localKey)
                                saved = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            if (saved) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Saved", color = Color.Black)
                            } else {
                                Text("Save key", color = Color.Black)
                            }
                        }
                        AssistChip(onClick = {}, label = { Text(if (apiKey.isBlank()) "No key saved" else "Key present") })
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(icon = Icons.Default.Memory, text = "LLM Model")
            Surface(
                onClick = { showModelPicker = !showModelPicker },
                color = Color.White.copy(alpha = 0.04f),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = selectedModel?.name ?: "Select Model",
                        color = Color.White.copy(alpha = 0.82f),
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${selectedModel?.provider ?: ""} · ${selectedModel?.description ?: ""}",
                        color = Color.White.copy(alpha = 0.34f),
                        fontSize = 12.sp,
                    )
                }
            }
            if (showModelPicker) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    modelOptions.forEach { option ->
                        val selected = option.id == selectedModelId
                        Surface(
                            onClick = {
                                onModelSelected(option.id)
                                showModelPicker = false
                            },
                            color = if (selected) Color(0x1410B981) else Color.White.copy(alpha = 0.03f),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (selected) {
                                    Emerald500.copy(alpha = 0.55f)
                                } else {
                                    Color.White.copy(alpha = 0.08f)
                                },
                            ),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = option.name,
                                            color = if (selected) Emerald400 else Color.White.copy(alpha = 0.72f),
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                        if (option.recommended) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .background(Emerald500.copy(alpha = 0.14f), CircleShape)
                                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                            ) {
                                                Text(
                                                    text = "Recommended",
                                                    color = Emerald400,
                                                    fontSize = 11.sp,
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${option.provider} · ${option.description}",
                                        color = Color.White.copy(alpha = 0.32f),
                                        fontSize = 12.sp,
                                    )
                                }
                                if (selected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Emerald400)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(icon = Icons.Default.Info, text = "Under the Hood")
            Surface(
                color = Color.White.copy(alpha = 0.03f),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    TechRow(Icons.Default.CameraAlt, "CameraX", "Camera preview & image capture")
                    Spacer(modifier = Modifier.height(10.dp))
                    TechRow(Icons.Default.Visibility, "ML Kit Text Recognition v2", "On-device OCR engine")
                    Spacer(modifier = Modifier.height(10.dp))
                    TechRow(Icons.Default.Storage, "Room Database", "Local scan history & cached results")
                    Spacer(modifier = Modifier.height(10.dp))
                    TechRow(Icons.Default.Security, "Android Keystore", "Encrypted API key storage")
                    Spacer(modifier = Modifier.height(10.dp))
                    TechRow(Icons.Default.Memory, "OkHttp", "Direct HTTPS calls to model APIs")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                color = Emerald500.copy(alpha = 0.05f),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Emerald500.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Privacy-First Design",
                        color = Emerald400.copy(alpha = 0.7f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "No sign-in required. No data leaves your device except the ingredient text sent to your chosen LLM provider. Scan history stays in local Room DB. API keys are encrypted via Android Keystore. DataStore handles preferences.",
                        color = Color.White.copy(alpha = 0.34f),
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AppFooter()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${AppBrand.name} · v1.0.0 · Kotlin · Jetpack Compose",
                    color = Color.White.copy(alpha = 0.12f),
                    fontSize = 11.sp,
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
) {
    Row(
        modifier = Modifier.padding(bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = Emerald400, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.34f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.8.sp,
        )
    }
}

@Composable
private fun TechRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Emerald500.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = Emerald400.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, color = Color.White.copy(alpha = 0.68f), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = description, color = Color.White.copy(alpha = 0.28f), fontSize = 12.sp)
        }
    }
}
