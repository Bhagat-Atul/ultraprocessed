package com.b2.ultraprocessed.storage.secrets

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecretKeyManager(context: Context) {

    // This builds the master encryption key using Android Keystore
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    // This is the encrypted storage — like SharedPreferences but locked in a safe
    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "nova_secrets",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // Save an API key securely
    fun saveApiKey(keyName: String, apiKey: String) {
        encryptedPrefs.edit().putString(keyName, apiKey).apply()
    }

    // Retrieve an API key
    fun getApiKey(keyName: String): String? {
        return encryptedPrefs.getString(keyName, null)
    }

    // Delete an API key
    fun deleteApiKey(keyName: String) {
        encryptedPrefs.edit().remove(keyName).apply()
    }

    // Check if a key exists without revealing it
    fun hasApiKey(keyName: String): Boolean {
        return encryptedPrefs.contains(keyName)
    }
}