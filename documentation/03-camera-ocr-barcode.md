# Camera, OCR, And Barcode

This component owns product input capture. It turns physical product labels or barcodes into local image paths or barcode values for downstream analysis. OCR is always on device. Captured and uploaded label images are never sent to the API.

## Files

- `camera/CameraCaptureController.kt`
- `camera/LocalImageImportController.kt`
- `ocr/MlKitOcrPipeline.kt`
- `ocr/OcrPipeline.kt`
- `ocr/OcrResult.kt`
- `barcode/BarcodeLiveScanController.kt`
- `barcode/MlKitBarcodeScanner.kt`
- `barcode/BarcodeScanner.kt`
- `barcode/BarcodeResult.kt`

## Label Capture Flow

```mermaid
flowchart LR
    Mode[Ingredient Label mode] --> User[User taps Scan Label]
    User --> CameraX[CameraX ImageCapture]
    CameraX --> File[App-local image file]
    File --> Analysis[FoodAnalysisPipeline]
    Analysis --> OCR[ML Kit OCR on device]
    OCR --> LLM[Text-only backend /analyze call]
```

## Gallery Import Flow

```mermaid
flowchart LR
    Picker[Android content picker] --> Uri[Content URI]
    Uri --> Copy[Copy stream into app-local imports folder]
    Copy --> Analysis[FoodAnalysisPipeline]
    Analysis --> OCR[ML Kit OCR on device]
    OCR --> LLM[Text-only backend /analyze call]
```

## Barcode Flow

```mermaid
flowchart LR
    Mode[Barcode Scan mode] --> CTA[Primary button reads Scan Barcode]
    CTA --> Preview[CameraX Preview]
    Preview --> Analyzer[ImageAnalysis]
    Analyzer --> MLKit[ML Kit Barcode Scanner]
    MLKit --> Code[UPC/EAN/code value]
    Code --> USDA[USDA lookup]
```

## Scanner UI Rules

- The camera preview is the content surface; scan brackets are an overlay with padding, not a border around the image.
- The scan line and brackets should feel related: subtle green, similar opacity, and restrained thickness.
- Upload and scan mode pills use the shared 8pt spacing system and shared typography.
- Barcode mode changes the main CTA text to `Scan Barcode`.
- Ingredient mode changes the main CTA text to `Scan Label`.

## Implementation Notes

- Camera capture uses `CAPTURE_MODE_MINIMIZE_LATENCY`.
- Barcode live scanning uses `ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST` to prevent analyzer backlog.
- Barcode delivery is guarded so the same visible barcode does not trigger repeated navigation.
- Imported images are copied into app-local external files before analysis.
- OCR uses ML Kit Text Recognition with Latin options.
- OCR is intentionally behind the `OcrPipeline` interface so future on-device OCR can feed the same backend analysis contract.
- Test mode can disable live camera preview so UI tests do not require camera hardware.

## Failure Behavior

- Missing image file returns a typed OCR/barcode failure.
- Empty OCR text returns a user-friendly failure.
- OCR text that is readable but not food-related is sent to backend `/analyze`, which can return `containsConsumableFoodItem = false`. The app then stops and shows the model's human-readable rejection reason.
- Barcode miss can fall back to on-device OCR when an image path is available.
