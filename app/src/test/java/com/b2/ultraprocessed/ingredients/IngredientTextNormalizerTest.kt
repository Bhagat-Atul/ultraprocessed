package com.b2.ultraprocessed.ingredients

import org.junit.Assert.assertEquals
import org.junit.Test

class IngredientTextNormalizerTest {
    @Test
    fun normalize_collapsesLineBreaksAndStripsIngredientsPrefix() {
        val raw = "Ingredients:\noats,\ndates,\nalmonds"
        assertEquals("oats, dates, almonds", IngredientTextNormalizer.normalize(raw))
    }

    @Test
    fun normalize_collapsesRepeatedSpaces() {
        assertEquals("a, b, c", IngredientTextNormalizer.normalize("a,  b,   c"))
    }

    @Test
    fun normalize_handlesIngredientsWithDash() {
        assertEquals("oats", IngredientTextNormalizer.normalize("Ingredients - oats"))
    }
}
