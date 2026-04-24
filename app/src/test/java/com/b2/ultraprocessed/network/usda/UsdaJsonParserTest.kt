package com.b2.ultraprocessed.network.usda

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class UsdaJsonParserTest {
    @Test
    fun parseSearchFoods_extractsBrandedItems() {
        val json = """
            {
              "foods": [
                {
                  "fdcId": 12345,
                  "description": "Frozen Cheeseburger Sandwich",
                  "dataType": "Branded",
                  "brandOwner": "Great Value",
                  "gtinUpc": "078742195760",
                  "ingredients": "BEEF, BUN, CHEESE"
                }
              ]
            }
        """.trimIndent()

        val result = UsdaJsonParser.parseSearchFoods(json)
        assertEquals(1, result.size)
        assertEquals(12345L, result.first().fdcId)
        assertEquals("Branded", result.first().dataType)
        assertEquals("078742195760", result.first().gtinUpc)
    }

    @Test
    fun parseFoodDetail_extractsIngredients() {
        val json = """
            {
              "fdcId": 12345,
              "description": "Frozen Cheeseburger Sandwich",
              "brandOwner": "Great Value",
              "gtinUpc": "078742195760",
              "ingredients": "BEEF, BUN, CHEESE"
            }
        """.trimIndent()

        val result = UsdaJsonParser.parseFoodDetail(json)
        assertNotNull(result)
        assertEquals("BEEF, BUN, CHEESE", result?.ingredients)
    }
}
