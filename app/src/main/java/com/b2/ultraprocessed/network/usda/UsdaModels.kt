package com.b2.ultraprocessed.network.usda

data class UsdaSearchFood(
    val fdcId: Long,
    val description: String,
    val dataType: String?,
    val brandOwner: String?,
    val gtinUpc: String?,
    val ingredients: String?,
)

data class UsdaFoodDetail(
    val fdcId: Long,
    val description: String,
    val brandOwner: String?,
    val gtinUpc: String?,
    val ingredients: String?,
)

data class UsdaFoodRecord(
    val fdcId: Long,
    val productName: String,
    val brandOwner: String?,
    val gtinUpc: String?,
    val ingredientsText: String?,
)
