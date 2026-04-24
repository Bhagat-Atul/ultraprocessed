package com.b2.ultraprocessed.network.usda

interface UsdaApiDataSource {
    suspend fun searchFoods(
        query: String,
        pageSize: Int = 10,
    ): List<UsdaSearchFood>

    suspend fun fetchFoodDetail(fdcId: Long): UsdaFoodDetail?
}
