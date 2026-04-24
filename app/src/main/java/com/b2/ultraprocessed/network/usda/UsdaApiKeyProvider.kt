package com.b2.ultraprocessed.network.usda

import com.b2.ultraprocessed.BuildConfig

fun interface UsdaApiKeyProvider {
    fun getApiKey(): String
}

class BuildConfigUsdaApiKeyProvider : UsdaApiKeyProvider {
    override fun getApiKey(): String = BuildConfig.USDA_API_KEY.trim()
}
