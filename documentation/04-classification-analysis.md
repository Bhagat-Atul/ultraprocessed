# Classification And Analysis

This layer turns on-device OCR or USDA ingredient evidence into the final result model shown by the UI. The Android pipeline still exposes logical NOVA, ingredient, and allergen stages, but the proxy-backed production path obtains them from one backend `/analyze` call. Images never enter the API workflow.

## Files

- `analysis/FoodAnalysisPipeline.kt`
- `analysis/AnalysisReport.kt`
- `analysis/AnalysisStage.kt`
- `analysis/UsageEstimateCalculator.kt`
- `network/llm/FoodLabelLlmWorkflow.kt`
- `network/llm/ProxyFoodLabelLlmWorkflow.kt`
- `network/llm/LlmContractRetry.kt`
- `network/llm/ResultChatWorkflow.kt`
- `ui/ClassificationUiMapper.kt`

## Pipeline Overview

```mermaid
flowchart TB
    Input[Image or barcode text] --> Extract[On-device OCR or USDA text extraction]
    Extract --> Gate{Enough ingredient text?}
    Gate -->|No| Invalid[OCR/text failure]
    Gate -->|Yes| Proxy[Backend /analyze single structured call]
    Proxy --> Nova[NOVA classification + non-food gate]
    Nova -->|containsConsumableFoodItem false| Error[AnalysisErrorScreen AI response]
    Nova -->|containsConsumableFoodItem true| Ingredients[Corrected ingredient list + UP markers]
    Ingredients --> Allergen[Allergen detection from corrected names]
    Nova --> Result[AnalysisReport]
    Ingredients --> Result
    Allergen --> Result
    Result --> Usage[Usage estimate]
    Usage --> Ui
    Result --> Ui[ScanResultUi]
```

## Stage Contracts

### OCR/Text Extraction

- Input: image path or USDA ingredient text.
- Output: `IngredientExtraction`.
- Purpose: produce text evidence without sending images to the API.
- Failure: missing or too-short text stops the pipeline before classification.

### NOVA Classification

- Input: `IngredientExtraction`, served from the cached backend analysis response in the proxy path.
- Output: `NovaClassification`.
- Purpose: classify the whole label by NOVA group and reject non-food/non-ingredient scans.
- No image access.
- No brand or marketing inference.
- No allergen logic.

If `containsConsumableFoodItem` is false, the pipeline stops immediately and shows `rejectionReason` on the error screen.

### Ingredient List Analysis

- Input: `IngredientExtraction`, served from the cached backend analysis response in the proxy path.
- Output: `IngredientListAnalysis`.
- Purpose: correct ingredient names and return the subset of corrected names that are ultra-processed or industrial formulation markers.
- No overall NOVA classification.
- No allergen logic.

This stage directly controls ingredient capsule coloration:

- corrected ingredient in `ultraProcessedIngredients` -> red capsule,
- corrected ingredient not in `ultraProcessedIngredients` -> green capsule.

### Allergen Detection

- Input: corrected ingredient names from `IngredientListAnalysis`, served from the cached backend analysis response in the proxy path.
- Output: `AllergenDetection`.
- Purpose: identify common US / Western allergen signals from cleaned ingredient names.
- Separate UI surface from ingredient NOVA bubbles.

## JSON Outputs

### Extraction

```json
{
  "code": 0,
  "productName": "Scanned food label",
  "rawIngredientText": "Ingredients: sugar, wheat flour, milk",
  "ingredients": ["Sugar", "Wheat Flour", "Milk"],
  "confidence": 0.91,
  "warnings": []
}
```

### Classification

```json
{
  "containsConsumableFoodItem": true,
  "novaGroup": 4,
  "summary": "The list contains industrial formulation markers.",
  "rejectionReason": "",
  "confidence": 0.84,
  "warnings": []
}
```

### Non-Food Classification Response

```json
{
  "containsConsumableFoodItem": false,
  "novaGroup": 0,
  "summary": "Text doesn't contain any consumable food item.",
  "rejectionReason": "Text doesn't contain any consumable food item.",
  "confidence": 0.0,
  "warnings": ["No food ingredient evidence was found in the supplied text."]
}
```

### Ingredient List Analysis

```json
{
  "correctedIngredients": ["Sugar", "Wheat Flour", "Artificial Flavor"],
  "ultraProcessedIngredients": [
    { "name": "Artificial Flavor", "reason": "Industrial flavor marker." }
  ],
  "confidence": 0.84,
  "warnings": []
}
```

### Allergen Detection

```json
{
  "allergens": ["Milk", "Wheat"],
  "warnings": [],
  "confidence": 0.87
}
```

## Ingredient Capsule Contract

- Each `correctedIngredients` entry must be atomic and short.
- Do not return comma blobs or sentence-like strings.
- Keep ingredient order stable.
- The UI renders each ingredient as a compact chip.
- Names returned in `ultraProcessedIngredients` render red.
- Corrected names not returned in `ultraProcessedIngredients` render green.
- NOVA 1/2/3/4 colors are only for the overall classification tile, not ingredient capsule coloring.

## Contract Enforcement

```mermaid
classDiagram
    class IngredientExtraction {
        +code: Int
        +productName: String
        +rawText: String
        +ingredients: List~String~
        +confidence: Float
        +warnings: List~String~
    }
    class NovaClassification {
        +novaGroup: Int
        +summary: String
        +confidence: Float
        +warnings: List~String~
        +containsConsumableFoodItem: Boolean
        +rejectionReason: String
    }
    class IngredientListAnalysis {
        +correctedIngredients: List~String~
        +ultraProcessedIngredients: List~IngredientRiskMarker~
        +warnings: List~String~
        +confidence: Float
    }
    class AllergenDetection {
        +allergens: List~String~
        +warnings: List~String~
        +confidence: Float
    }
    IngredientListAnalysis --> IngredientRiskMarker
    FoodAnalysisPipeline --> IngredientExtraction
    FoodAnalysisPipeline --> NovaClassification
    FoodAnalysisPipeline --> IngredientListAnalysis
    FoodAnalysisPipeline --> AllergenDetection
```

## Retry Model

- Timeout retries are handled by `FoodAnalysisPipeline.runLlmStage`.
- `LLM_TIMEOUT_RETRY_ATTEMPTS` is currently 2.
- Contract/schema retry is intentionally minimal; `LlmContractRetry.kt` currently allows one contract attempt.
- If a stage returns malformed JSON, missing required fields, or an unsupported value, the pipeline fails that scan rather than inventing a rule-based fallback.

For the full API request/response contract, see [08-llm-api-contracts.md](08-llm-api-contracts.md).

## Result Mapping

`ClassificationUiMapper` converts analysis output into `ScanResultUi`:

- `novaGroup` becomes the top-level classification card.
- `correctedIngredients` becomes the ingredient chip list.
- `ultraProcessedIngredients` becomes `problemIngredients` and marks matching chips as red.
- `allergens` becomes a separate allergen block.
- `UsageEstimateCalculator` maps provider-reported input tokens, output tokens, total tokens, and cost for the active result, and estimates only when provider usage metadata is absent. These values are session-only.

## Usage And Cost Metadata

The active result displays provider-reported usage values when available. If a provider omits usage metadata, the app falls back to a local estimate. No usage values are persisted.

```mermaid
flowchart LR
    Prompt[Prompt + ingredient evidence] --> Estimator[UsageEstimateCalculator]
    Estimator --> Tokens[Estimated tokens]
    Estimator --> Cost[Estimated cost]
    Tokens --> Session[In-memory result]
    Cost --> Session
```

## Operational Notes

- OCR text may be noisy; the prompt and parser treat it as evidence, not truth.
- If OCR cannot read enough text, the entire flow stops before any LLM request.
- If the first LLM stage says the text is not a consumable food item, later LLM stages are skipped and the user sees the returned reason.
- If NOVA classification, ingredient cleanup, or allergen detection fails, the analysis fails for that scan.
- There is no rule-based fallback classifier in the runtime path.
