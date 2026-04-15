package com.b2.ultraprocessed.network.usda

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class UsdaRepositoryTest {
    @Test
    fun lookupByBarcode_prefersExactUpcMatch() = runTest {
        val dataSource = FakeUsdaApiDataSource(
            searchFoods = listOf(
                UsdaSearchFood(
                    fdcId = 1L,
                    description = "Different product",
                    dataType = "Branded",
                    brandOwner = "Brand A",
                    gtinUpc = "111111111111",
                    ingredients = "SUGAR",
                ),
                UsdaSearchFood(
                    fdcId = 2L,
                    description = "Correct product",
                    dataType = "Branded",
                    brandOwner = "Brand B",
                    gtinUpc = "078742195760",
                    ingredients = "BEEF, BUN",
                ),
            ),
            detailById = mapOf(
                2L to UsdaFoodDetail(
                    fdcId = 2L,
                    description = "Correct product",
                    brandOwner = "Brand B",
                    gtinUpc = "078742195760",
                    ingredients = "BEEF, BUN",
                ),
            ),
        )
        val repo = UsdaRepository(dataSource)

        val result = repo.lookupByBarcode("078742195760")

        assertNotNull(result)
        assertEquals(2L, result?.fdcId)
        assertEquals("Correct product", result?.productName)
    }

    @Test
    fun lookupByBarcode_returnsNull_whenNoCandidates() = runTest {
        val repo = UsdaRepository(FakeUsdaApiDataSource(searchFoods = emptyList()))
        assertNull(repo.lookupByBarcode("012345678905"))
    }

    @Test
    fun lookupByBarcode_returnsNull_whenSearchHitsHaveNoMatchingGtin() = runTest {
        val repo = UsdaRepository(
            FakeUsdaApiDataSource(
                searchFoods = listOf(
                    UsdaSearchFood(
                        fdcId = 9L,
                        description = "Unrelated snack",
                        dataType = "Branded",
                        brandOwner = "Other Co",
                        gtinUpc = "999999999999",
                        ingredients = "SUGAR",
                    ),
                ),
            ),
        )
        assertNull(repo.lookupByBarcode("078742195760"))
    }
}

private class FakeUsdaApiDataSource(
    private val searchFoods: List<UsdaSearchFood>,
    private val detailById: Map<Long, UsdaFoodDetail> = emptyMap(),
) : UsdaApiDataSource {
    override suspend fun searchFoods(query: String, pageSize: Int): List<UsdaSearchFood> = searchFoods

    override suspend fun fetchFoodDetail(fdcId: Long): UsdaFoodDetail? = detailById[fdcId]
}
