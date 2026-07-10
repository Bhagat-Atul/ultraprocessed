# Architecture

Zest is a native Android app for label analysis. It launches through a branded Android splash and Compose splash, captures a food label image or barcode, extracts image text on device with ML Kit OCR, sends only extracted text to the backend proxy, and keeps the result in memory for the active session only.

## Design Goals

- Keep NOVA classification, ingredient cleanup, ultra-processed marker detection, and allergen logic API-driven.
- Keep secret storage encrypted and out of source control.
- Keep the UI deterministic and driven by explicit contracts.
- Never persist scan content, images, results, chat, usage, or failures.
- Keep the pipeline modular so on-device OCR can feed the same API contracts later.
- Keep brand, typography, sounds, and app chrome shared so screens stay visually consistent.
- Keep build guards in front of KSP and release tasks so retired or dataless source files cannot silently ship.

## Runtime Layers

```mermaid
flowchart LR
    Android[Android launch theme] --> UI[Compose UI]
    UI --> Orchestration[FoodAnalysisPipeline]
    Orchestration --> Input[Camera / Gallery / Barcode / OCR]
    Orchestration --> LLM[network/llm proxy client]
    Orchestration --> USDA[network/usda]
    LLM --> Backend[Cloud Run /analyze]
    Backend --> Prompts[backend-owned prompts]
    UI --> Storage[Encrypted Secrets + Preferences]
    UI --> Resources[Fonts / Icons / Sounds]
```

## Startup Architecture

```mermaid
sequenceDiagram
    participant OS as Android OS
    participant Activity as MainActivity
    participant App as UltraProcessedApp
    participant Splash as SplashScreen
    participant Scanner as ScannerScreen

    OS->>Activity: Start activity with Zest launch theme
    Activity->>App: setContent with Zest theme
    App->>Splash: show branded loading animation
    Splash-->>App: onComplete
    App->>Scanner: render scanner home
```

The native splash and launcher icon live in Android resources. The animated loading screen lives in Compose, so it can use app typography, logo composition, footer copy, sound timing, and animation state.

## Main User Flows

### Label Image

```mermaid
sequenceDiagram
    participant User
    participant Scanner as ScannerScreen
    participant Pipeline as FoodAnalysisPipeline
    participant LLM as FoodLabelLlmWorkflow

    User->>Scanner: Capture or import image
    Scanner->>Pipeline: analyzeFromImage(path)
    Pipeline->>Pipeline: ML Kit OCR extracts text on device
    Pipeline->>LLM: classifyNova(text extraction)
    LLM->>Backend: POST /analyze with OCR text
    Backend-->>LLM: single structured analysis response
    LLM-->>Pipeline: NovaClassification or non-food marker
    Pipeline->>LLM: analyzeIngredientList(text extraction)
    LLM-->>Pipeline: cached corrected ingredients + ultra-processed marker list
    Pipeline->>LLM: detectAllergens(corrected ingredients)
    LLM-->>Pipeline: cached allergen detection
    Pipeline-->>Scanner: AnalysisReport
    Scanner->>UI: Keep result in session memory
```

### Barcode

```mermaid
sequenceDiagram
    participant User
    participant Scanner as ScannerScreen
    participant Pipeline as FoodAnalysisPipeline
    participant USDA as UsdaRepository
    participant LLM as FoodLabelLlmWorkflow

    User->>Scanner: Scan barcode
    Scanner->>Pipeline: analyzeFromBarcode(code)
    Pipeline->>USDA: lookupByBarcode(code)
    USDA-->>Pipeline: USDA product record
    Pipeline->>LLM: classifyNova(from USDA text)
    LLM->>Backend: POST /analyze with ingredient text
    Backend-->>LLM: single structured analysis response
    Pipeline->>LLM: analyzeIngredientList(from cached proxy result)
    Pipeline->>LLM: detectAllergens(from cached proxy result)
    Pipeline-->>Scanner: AnalysisReport
```

## Component Boundaries

- `ui/` owns Compose state, screen transitions, and display logic.
- `ui/theme/` owns Material theme, colors, Inter, and Space Grotesk.
- `ui/audio/` owns app sound playback and sound event mapping.
- `analysis/` owns orchestration, stage timing, and failure policy.
- `network/llm/` owns backend proxy requests, parsing, and result-scoped chat clients. Analysis prompts are backend-owned.
- `network/usda/` owns FoodData Central lookup, retry handling, and exact-hit ranking.
- `documentation/code-archive/session_only_storage/` contains the retired persistence implementation and is outside active source sets.
- `storage/secrets/` owns encrypted API key storage.
- `storage/preferences/` owns non-secret local preferences such as the sound toggle.
- `res/` owns fonts, raw sounds, launcher icon resources, splash drawables, colors, strings, and themes.

## Key Contracts

### Analysis Report

```text
AnalysisReport
├── sourceType
├── productName
├── ingredientsTextUsed
├── warnings
└── scanResult: ScanResultUi
```

### Result UI Model

```text
ScanResultUi
├── productName
├── novaGroup
├── summary
├── problemIngredients
├── allIngredients
├── allergens
├── ingredientAssessments
├── rawIngredientText
├── labelImagePath
└── usageEstimate
```

### App Shell State

```text
UltraProcessedApp
├── destination
├── current scan mode
├── current scan result
├── encrypted key presence flags
├── selected model id
├── sound preference state
├── Session-only result flow
└── Result chat workflow
```

## Production Rules

```mermaid
classDiagram
    class FoodAnalysisPipeline {
        +analyzeFromImage()
        +analyzeFromBarcode()
        +analyzeFromBarcodeImage()
    }
    class FoodLabelLlmWorkflow {
        <<interface>>
        +classifyNova()
        +analyzeIngredientList()
        +detectAllergens()
    }
    class UsdaRepository {
        +lookupByBarcode()
    }
    class SecretKeyManager {
        +saveApiKey()
        +getApiKey()
    }
    class AppPreferences {
        +soundEffectsEnabled
        +setSoundEffectsEnabled()
    }
    class ScanResultUi
    FoodAnalysisPipeline --> FoodLabelLlmWorkflow
    FoodAnalysisPipeline --> UsdaRepository
    FoodAnalysisPipeline --> ScanResultUi
    SecretKeyManager --> FoodAnalysisPipeline
    AppPreferences --> UI
```

- Do not use rules-based NOVA classification in runtime code.
- Do not treat allergens as a signal inside ingredient coloring.
- Do not infer ingredients from product name or package art.
- Do not persist plaintext keys or secret values in Compose state.
- Do not create one-off typography or brand marks in individual screens.
- Do not keep demo or legacy classifier files in `app/src`.

## Failure Policy

- OCR failures stop before any LLM request is made.
- Non-food/non-ingredient text is rejected by the first LLM stage using `containsConsumableFoodItem = false`; the pipeline stops before ingredient cleanup or allergen detection.
- API rate limit errors surface as 429-specific UI messages.
- USDA lookup miss falls back to on-device OCR only when an image exists.
- If the LLM workflow is unavailable, the analysis fails rather than inventing a result.

## Build-Time Protection

`app/build.gradle.kts` defines source-tree verification tasks that run before normal Android build work:

- `verifyNoRetiredSourceFiles` fails if retired demo, legacy, or rule-based classifier files reappear.
- `verifyNoDatalessSources` fails on macOS dataless placeholders under `app/src`.
- `verifySourceTreeForBuild` runs both checks and is attached to `preBuild`.

These checks exist because stale source files and dataless placeholders can create testing, KSP, and deployment failures that are hard to diagnose after the build has already started.
