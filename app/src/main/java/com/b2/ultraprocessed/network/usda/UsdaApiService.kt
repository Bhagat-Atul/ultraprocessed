package com.b2.ultraprocessed.network.usda

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class UsdaApiService(
    private val apiKeyProvider: UsdaApiKeyProvider,
    private val client: OkHttpClient = OkHttpClient(),
) : UsdaApiDataSource {
    override suspend fun searchFoods(
        query: String,
        pageSize: Int,
    ): List<UsdaSearchFood> = withContext(Dispatchers.IO) {
        val apiKey = apiKeyProvider.getApiKey()
        if (apiKey.isBlank()) return@withContext emptyList()

        // POST is the documented path; `dataType` must be a JSON array (GET array query encoding was unreliable).
        val url = "$BASE_URL/foods/search".toHttpUrl().newBuilder()
            .addQueryParameter("api_key", apiKey)
            .build()
        val json = JSONObject().apply {
            put("query", query)
            put("pageSize", pageSize.coerceIn(1, 200))
            put("dataType", JSONArray().put("Branded"))
        }
        val body = json.toString().toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext emptyList()
            val responseBody = response.body?.string() ?: return@withContext emptyList()
            UsdaJsonParser.parseSearchFoods(responseBody)
        }
    }

    override suspend fun fetchFoodDetail(fdcId: Long): UsdaFoodDetail? = withContext(Dispatchers.IO) {
        val apiKey = apiKeyProvider.getApiKey()
        if (apiKey.isBlank()) return@withContext null

        val url = "$BASE_URL/food/$fdcId".toHttpUrl().newBuilder()
            .addQueryParameter("api_key", apiKey)
            .build()
        val request = Request.Builder().url(url).get().build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext null
            val body = response.body?.string() ?: return@withContext null
            UsdaJsonParser.parseFoodDetail(body)
        }
    }

    companion object {
        const val BASE_URL: String = "https://api.nal.usda.gov/fdc/v1"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}
