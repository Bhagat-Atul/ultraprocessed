package com.b2.ultraprocessed.storage.secrets

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// This creates the actual DataStore file on the device
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nova_settings")

class AppSettingsRepository(private val context: Context) {

    // These are the keys — think of them as column names in a settings table
    companion object {
        val ENGINE_MODE = stringPreferencesKey("engine_mode")
        val SCAN_HISTORY_ENABLED = booleanPreferencesKey("scan_history_enabled")
        val ACCESSIBILITY_MODE = booleanPreferencesKey("accessibility_mode")
        val API_FALLBACK_ALLOWED = booleanPreferencesKey("api_fallback_allowed")
    }

    // READ settings
    val engineMode: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[ENGINE_MODE] ?: "local" }

    val scanHistoryEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[SCAN_HISTORY_ENABLED] ?: true }

    val accessibilityMode: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[ACCESSIBILITY_MODE] ?: false }

    val apiFallbackAllowed: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[API_FALLBACK_ALLOWED] ?: true }

    // WRITE settings
    suspend fun setEngineMode(mode: String) {
        context.dataStore.edit { settings -> settings[ENGINE_MODE] = mode }
    }

    suspend fun setScanHistoryEnabled(enabled: Boolean) {
        context.dataStore.edit { settings -> settings[SCAN_HISTORY_ENABLED] = enabled }
    }

    suspend fun setAccessibilityMode(enabled: Boolean) {
        context.dataStore.edit { settings -> settings[ACCESSIBILITY_MODE] = enabled }
    }

    suspend fun setApiFallbackAllowed(allowed: Boolean) {
        context.dataStore.edit { settings -> settings[API_FALLBACK_ALLOWED] = allowed }
    }
}