package com.b2.ultraprocessed.ui

/** Built-in images under `assets/demo_samples/`. */
data class DemoImageSample(
    val id: String,
    val title: String,
    val subtitle: String,
    val assetFileName: String,
) {
    val assetPath: String get() = "demo_samples/$assetFileName"
}

object DemoImageSamples {
    /** Cheeseburger first: strongest OCR + NOVA demo; macro photos last. */
    val all: List<DemoImageSample> = listOf(
        DemoImageSample(
            id = "cheeseburger",
            title = "Frozen cheeseburger box",
            subtitle = "Packaging with clear printed text",
            assetFileName = "demo_cheeseburger.png",
        ),
        DemoImageSample(
            id = "bread",
            title = "Multigrain loaf",
            subtitle = "Bread on a board",
            assetFileName = "demo_multigrain_bread.png",
        ),
        DemoImageSample(
            id = "bakery",
            title = "Bakery display",
            subtitle = "Pastry case through glass",
            assetFileName = "demo_bakery.png",
        ),
        DemoImageSample(
            id = "cherries",
            title = "Fresh cherries",
            subtitle = "Macro photo · little or no label text",
            assetFileName = "demo_cherries.png",
        ),
    )
}
