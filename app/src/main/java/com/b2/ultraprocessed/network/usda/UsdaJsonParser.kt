package com.b2.ultraprocessed.network.usda

import org.json.JSONObject

object UsdaJsonParser {
    fun parseSearchFoods(body: String): List<UsdaSearchFood> {
        val root = JSONObject(body)
        val foods = root.optJSONArray("foods") ?: return emptyList()
        return buildList(foods.length()) {
            for (index in 0 until foods.length()) {
                val item = foods.optJSONObject(index) ?: continue
                val fdcId = item.optLong("fdcId", 0L)
                if (fdcId <= 0L) continue
                add(
                    UsdaSearchFood(
                        fdcId = fdcId,
                        description = item.optString("description", ""),
                        dataType = item.getNullableString("dataType"),
                        brandOwner = item.getNullableString("brandOwner"),
                        gtinUpc = item.getNullableString("gtinUpc"),
                        ingredients = item.getNullableString("ingredients"),
                    ),
                )
            }
        }
    }

    fun parseFoodDetail(body: String): UsdaFoodDetail? {
        val root = JSONObject(body)
        val fdcId = root.optLong("fdcId", 0L)
        if (fdcId <= 0L) return null
        return UsdaFoodDetail(
            fdcId = fdcId,
            description = root.optString("description", ""),
            brandOwner = root.getNullableString("brandOwner"),
            gtinUpc = root.getNullableString("gtinUpc"),
            ingredients = root.getNullableString("ingredients"),
        )
    }
}

private fun JSONObject.getNullableString(name: String): String? {
    if (!has(name) || isNull(name)) return null
    return optString(name).trim().takeIf { it.isNotEmpty() }
}
