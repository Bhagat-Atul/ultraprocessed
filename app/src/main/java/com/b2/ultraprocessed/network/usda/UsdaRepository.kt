package com.b2.ultraprocessed.network.usda

class UsdaRepository(
    private val dataSource: UsdaApiDataSource,
) {
    suspend fun lookupByBarcode(upc: String): UsdaFoodRecord? {
        val searchDigits = upc.filter { it.isDigit() }
        if (searchDigits.isBlank()) return null

        // Query with full scanned digits (keep leading zeros); normalization is only for GTIN comparison.
        val candidates = dataSource.searchFoods(query = searchDigits, pageSize = 25)
        if (candidates.isEmpty()) return null
        val picked = rankBarcodeCandidates(candidates, searchDigits) ?: return null
        val detail = dataSource.fetchFoodDetail(picked.fdcId)
        return toRecord(detail ?: picked.toDetailFallback())
    }

    suspend fun lookupByQuery(query: String): UsdaFoodRecord? {
        if (query.isBlank()) return null
        val candidates = dataSource.searchFoods(query = query, pageSize = 10)
        val picked = candidates.firstOrNull() ?: return null
        val detail = dataSource.fetchFoodDetail(picked.fdcId)
        return toRecord(detail ?: picked.toDetailFallback())
    }

    private fun rankBarcodeCandidates(
        candidates: List<UsdaSearchFood>,
        scannedDigits: String,
    ): UsdaSearchFood? {
        val targetNorm = normalizeUpc(scannedDigits)
        if (targetNorm.isBlank()) return null
        // Only accept a row whose GTIN matches the scan — do not return an unrelated branded hit.
        return candidates.firstOrNull { it.gtinUpc.matchesUpc(targetNorm) }
    }

    private fun toRecord(detail: UsdaFoodDetail): UsdaFoodRecord = UsdaFoodRecord(
        fdcId = detail.fdcId,
        productName = detail.description.ifBlank { "USDA product" },
        brandOwner = detail.brandOwner,
        gtinUpc = detail.gtinUpc,
        ingredientsText = detail.ingredients,
    )
}

private fun UsdaSearchFood.toDetailFallback(): UsdaFoodDetail = UsdaFoodDetail(
    fdcId = fdcId,
    description = description,
    brandOwner = brandOwner,
    gtinUpc = gtinUpc,
    ingredients = ingredients,
)

private fun normalizeUpc(value: String?): String =
    value.orEmpty().filter(Char::isDigit).trimStart('0')

private fun String?.matchesUpc(normalizedTarget: String): Boolean {
    val mine = normalizeUpc(this)
    return mine == normalizedTarget || mine.endsWith(normalizedTarget) || normalizedTarget.endsWith(mine)
}
