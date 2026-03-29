package com.b2.ultraprocessed.ui

data class ModelOption(
    val id: String,
    val name: String,
    val provider: String,
    val description: String,
    val recommended: Boolean = false,
)

data class ProblemIngredient(
    val name: String,
    val reason: String,
)

data class ScanResultUi(
    val productName: String,
    val novaGroup: Int,
    val summary: String,
    val problemIngredients: List<ProblemIngredient>,
    val allIngredients: List<String>,
    val engineLabel: String,
)

data class HistoryItemUi(
    val id: String,
    val productName: String,
    val novaGroup: Int,
    val scannedAt: String,
    val summary: String,
    val capturedImagePath: String? = null,
)

enum class AppDestination {
    Splash,
    Scanner,
    Analyzing,
    Results,
    Settings,
    History,
}

object StubUiData {
    val modelOptions = listOf(
        ModelOption(
            id = "gemini-2.0-flash",
            name = "Gemini 2.0 Flash",
            provider = "Google",
            description = "Fast, free tier available",
            recommended = true,
        ),
        ModelOption(
            id = "gpt-4o-mini",
            name = "GPT-4o Mini",
            provider = "OpenAI",
            description = "Balanced speed & accuracy",
        ),
        ModelOption(
            id = "gpt-4o",
            name = "GPT-4o",
            provider = "OpenAI",
            description = "Most accurate, higher cost",
        ),
        ModelOption(
            id = "claude-3.5-sonnet",
            name = "Claude 3.5 Sonnet",
            provider = "Anthropic",
            description = "Strong reasoning, detailed analysis",
        ),
    )

    val results = listOf(
        ScanResultUi(
            productName = "Strawberry Fruit Snacks",
            novaGroup = 4,
            summary = "This stubbed scan is flagged as ultra-processed because it includes multiple industrial additives and syrup-based sweeteners.",
            problemIngredients = listOf(
                ProblemIngredient("High Fructose Corn Syrup", "Industrial sweetener often seen in ultra-processed products."),
                ProblemIngredient("Red 40", "Synthetic food dye used for color standardization."),
                ProblemIngredient("TBHQ", "Synthetic preservative used to extend shelf life."),
            ),
            allIngredients = listOf(
                "Sugar",
                "High Fructose Corn Syrup",
                "Modified Corn Starch",
                "Citric Acid",
                "Natural Flavor",
                "Red 40",
                "TBHQ",
                "Gelatin",
            ),
            engineLabel = "Rules + demo stub",
        ),
        ScanResultUi(
            productName = "Whole Grain Cereal Bar",
            novaGroup = 3,
            summary = "This stubbed result is moderately processed. It contains recognizable grains, but also added sugar and emulsifiers.",
            problemIngredients = listOf(
                ProblemIngredient("Cane Sugar", "Added sugar increases processing and energy density."),
                ProblemIngredient("Soy Lecithin", "Emulsifier that usually indicates a more processed formulation."),
            ),
            allIngredients = listOf(
                "Whole Grain Oats",
                "Brown Rice Syrup",
                "Cane Sugar",
                "Soy Lecithin",
                "Sea Salt",
                "Natural Flavor",
            ),
            engineLabel = "Rules + demo stub",
        ),
        ScanResultUi(
            productName = "Organic Mixed Nuts",
            novaGroup = 1,
            summary = "This stubbed result looks minimally processed. The ingredient list is short and made of whole foods.",
            problemIngredients = emptyList(),
            allIngredients = listOf("Almonds", "Cashews", "Walnuts", "Pecans", "Sea Salt"),
            engineLabel = "Rules fallback stub",
        ),
    )

    fun initialHistory(): List<HistoryItemUi> = listOf(
        HistoryItemUi(
            id = "history-1",
            productName = "Strawberry Fruit Snacks",
            novaGroup = 4,
            scannedAt = "2 min ago",
            summary = "Flagged for multiple additives and synthetic dye markers.",
        ),
        HistoryItemUi(
            id = "history-2",
            productName = "Organic Mixed Nuts",
            novaGroup = 1,
            scannedAt = "15 min ago",
            summary = "Short ingredient list with whole-food composition.",
        ),
        HistoryItemUi(
            id = "history-3",
            productName = "Whole Grain Cereal Bar",
            novaGroup = 3,
            scannedAt = "1 hr ago",
            summary = "Processed, but less severe than an industrial snack.",
        ),
        HistoryItemUi(
            id = "history-4",
            productName = "Diet Cola Zero",
            novaGroup = 4,
            scannedAt = "1 hr ago",
            summary = "Artificial sweeteners and flavor system triggered a high-processing warning.",
        ),
        HistoryItemUi(
            id = "history-5",
            productName = "Fresh Orange Juice",
            novaGroup = 1,
            scannedAt = "2 hrs ago",
            summary = "Closer to minimally processed based on the simple ingredient profile.",
        ),
        HistoryItemUi(
            id = "history-6",
            productName = "Protein Energy Bar",
            novaGroup = 3,
            scannedAt = "3 hrs ago",
            summary = "Moderately processed with sweeteners and texture agents.",
        ),
    )
}
