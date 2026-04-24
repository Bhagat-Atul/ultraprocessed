# Emre update: end-to-end label scan to NOVA-style classification

This document describes the work that connects **camera or gallery images**, **bundled demo sample images** (and an optional **raw demo text** API used by tests or future UI), to **on-device OCR**, **ingredient normalization**, **rules-based NOVA-style classification**, and the **Compose UI**. It is meant as a handoff for contributors and reviewers.

**Last reviewed:** 2026-03-30 (aligned with repo: demo picker + `analyzeDemoAsset` path).

---

## 1. Goals achieved

| Goal | Status |
|------|--------|
| Real pipeline: image → OCR → normalize → classify → UI | Done |
| Google ML Kit Text Recognition (Latin) | Done |
| Pluggable `OcrPipeline` + `MlKitOcrPipeline` | Done |
| `IngredientTextNormalizer` (prefix, whitespace, line breaks) | Done |
| `IngredientInput` → `ClassifierOrchestrator` (local-only: no network) | Done |
| Marker-based `RulesClassifier` (0 / 1 / 2+ markers + processed heuristic) | Done |
| Replace timed stub analysis with real work in `AnalyzingScreen` | Done |
| Demo: `DemoSamplePickerDialog` + `assets/demo_samples/` → `analyzeDemoAsset` → OCR | Done |
| User-visible error when text is too short after OCR | Done |
| Unit tests + fixture samples for expected NOVA groups | Done |
| Gradle: ML Kit + `kotlinx-coroutines-play-services` | Done |

**Still optional / future:** `ApiLLMClassifier` + `RemoteClassifierGateway`, on-device LLM, Room/DataStore persistence for keys and history (UI copy may still mention them).

---

## 2. High-level user flow

```mermaid
flowchart LR
    subgraph Input
        A[Camera capture or gallery]
        B[Try Demo → pick bundled sample]
    end
    subgraph App
        S[Scanner screen]
        Z[Analyzing screen]
        R[Results screen]
        E[Analysis error screen]
    end
    A --> S
    B --> S
    S -->|image path or demo asset| Z
    Z -->|success| R
    Z -->|OCR fail / too little text| E
    E -->|Try again| S
    R --> S
```

---

## 3. Technical pipeline (data flow)

Two entry paths converge on the same classification step:

```mermaid
flowchart TB
    subgraph PhotoPath["Photo path"]
        P[Image file path]
        OCR[MlKitOcrPipeline]
        P --> OCR
        OCR -->|Success: rawText| NORM
        OCR -->|Failure| ERR[Result.failure → UI error]
    end
    subgraph DemoPath["Demo paths"]
        DA[Bundled asset → cache file path]
        DT[Raw demo text optional — analyzeDemoText]
        DA --> OCR
        DT --> NORM
    end
    NORM[IngredientTextNormalizer.normalize]
    NORM --> LEN{normalized.length ≥ 12?}
    LEN -->|no| SHORT[Could not read enough ingredient text…]
    LEN -->|yes| IN[IngredientInput raw + normalized]
    IN --> ORCH[ClassifierOrchestrator EngineMode.Auto]
    ORCH --> RULES[RulesClassifier today: API and on-device are null]
    RULES --> CR[ClassificationResult]
    CR --> MAP[ClassificationUiMapper]
    MAP --> UI[ScanResultUi]
```

---

## 4. Layered architecture (packages)

```mermaid
flowchart TB
    subgraph ui["ui"]
        UPA[UltraProcessedApp]
        DSP[DemoSamplePickerDialog]
        AN[AnalyzingScreen]
        RS[ResultsScreen]
        AES[AnalysisErrorScreen]
        CUM[ClassificationUiMapper]
    end
    subgraph scan["scan"]
        LSP[LabelScanPipeline]
    end
    subgraph ocr["ocr"]
        OP[OcrPipeline]
        ML[MlKitOcrPipeline]
    end
    subgraph ingredients["ingredients"]
        ITN[IngredientTextNormalizer]
    end
    subgraph classify["classify"]
        CO[ClassifierOrchestrator]
        RC[RulesClassifier]
        API[ApiLLMClassifier — not wired]
        ODL[OnDeviceLLMClassifier — not wired]
    end
    UPA --> DSP
    UPA --> AN
    AN --> LSP
    LSP --> OP
    LSP --> ITN
    LSP --> CO
    OP -.-> ML
    CO --> RC
    CO -.-> API
    CO -.-> ODL
    LSP --> CUM
```

Solid lines: used in the current local-only build. Dotted lines: interfaces or classes present for future wiring.

---

## 5. Core data schema (Kotlin models)

### 5.1 OCR

| Type | Fields / variants |
|------|-------------------|
| `OcrResult` | `Success(rawText)` or `Failure(message, cause?)` |
| `OcrPipeline` | `suspend fun recognizeText(imagePath: String): OcrResult` |

### 5.2 Classification (domain)

| Type | Purpose |
|------|---------|
| `IngredientInput` | `rawText`, `normalizedText`, optional `languageTag` |
| `ClassificationContext` | `allowNetwork`, `apiFallbackEnabled`, `preferOnDevice` |
| `ClassificationResult` | `novaGroup`, `confidence`, `markers`, `explanation`, `highlightTerms`, `engine` |
| `Classifier` | Implementations: `RulesClassifier`, future API / on-device |

### 5.3 UI

| Type | Purpose |
|------|---------|
| `ScanResultUi` | Includes `confidence` and `engineLabel` for the results card |
| `AppDestination` | Added `AnalysisError` for failed analysis |

---

## 6. RulesClassifier decision logic (summary)

```mermaid
flowchart TD
    START[Normalized lowercase text]
    M[Count UPF substring markers]
    START --> M
    M --> C{marker count}
    C -->|≥ 2| N4[NOVA 4 — higher confidence]
    C -->|1| N3A[NOVA 3 — moderate confidence]
    C -->|0| H{Salt + cooking oil heuristic?}
    H -->|yes| N3B[NOVA 3 — processed pattern]
    H -->|no| N1[NOVA 1 — minimal markers]
```

UPF markers include (among others): maltodextrin, natural/artificial flavor, flavoring, emulsifier, stabilizer, color added, modified starch, hydrogenated oil, HFCS, MSG, sodium benzoate, carrageenan, sweeteners, polysorbate, protein isolates, etc. The **salt + oil** branch covers lists like “corn, salt, sunflower oil” without additive keywords.

---

## 7. Screen navigation (app state)

```mermaid
stateDiagram-v2
    [*] --> Splash
    Splash --> Scanner
    Scanner --> Analyzing : scan / demo
    Analyzing --> Results : success
    Analyzing --> AnalysisError : failure
    AnalysisError --> Scanner : try again
    Results --> Scanner : scan again
    Scanner --> Settings
    Scanner --> History
    Settings --> Scanner
    History --> Scanner
```

`UltraProcessedApp` holds:

- `scanSessionId` (re-triggers `LaunchedEffect` on `AnalyzingScreen`)
- `lastCapturedPhotoPath` vs `demoAssetPath` (mutually exclusive for a given run; camera/gallery vs **Try Demo** sample)
- optional `demoRawIngredientText` for `AnalyzingScreen` (wired for `analyzeDemoText`; currently passed as `null` from the main app)
- `showDemoPicker` to present `DemoSamplePickerDialog` on the scanner
- `AppTimingConfig.analysisMinimumDisplayMillis` so the analyzing screen stays readable when the pipeline finishes quickly (e.g. demo text)
- `currentScanResult` for `ResultsScreen`
- `analysisErrorMessage` for `AnalysisErrorScreen`
- in-memory `historyItems` (still not Room-backed)

---

## 8. File map (where things live)

| Path | Role |
|------|------|
| `app/.../ocr/OcrResult.kt` | OCR outcome sealed class |
| `app/.../ocr/OcrPipeline.kt` | OCR interface |
| `app/.../ocr/MlKitOcrPipeline.kt` | ML Kit implementation |
| `app/.../ingredients/IngredientTextNormalizer.kt` | Text cleanup |
| `app/.../scan/LabelScanPipeline.kt` | `analyzeImage`, `analyzeDemoAsset` (asset → cache → OCR), `analyzeDemoText` |
| `app/.../ui/ClassificationUiMapper.kt` | `ClassificationResult` → `ScanResultUi` |
| `app/.../ui/AnalysisErrorScreen.kt` | Error UX |
| `app/.../ui/UltraProcessedApp.kt` | Wires navigation and callbacks |
| `app/.../ui/DemoSamplePickerDialog.kt` | **Try Demo** sample chooser |
| `app/.../ui/DemoImageSamples.kt` | Built-in demo metadata + `assets/demo_samples/` paths |
| `app/.../ui/AnalyzingScreen.kt` | Runs pipeline in `LaunchedEffect` (`analyzeImage` / `analyzeDemoAsset` / `analyzeDemoText`) |
| `app/.../ui/AppModels.kt` | `ScanResultUi`, destinations, stubs |
| `app/.../classify/RulesClassifier.kt` | Marker scoring + explanations |
| `app/.../classify/ClassifierOrchestrator.kt` | Unchanged contract; used with `null` API/on-device |
| `app/src/main/assets/demo_samples/*.png` | Bundled images for **Try Demo** |
| `app/build.gradle.kts` | ML Kit + coroutines Play Services |
| `app/src/test/.../NovaIngredientSampleFixturesTest.kt` | Four canonical ingredient strings |
| `app/src/test/.../IngredientTextNormalizerTest.kt` | Normalizer tests |
| `app/src/test/.../RulesClassifierTest.kt` | Rules + heuristic tests |

---

## 9. Dependencies added

```text
com.google.mlkit:text-recognition:16.0.1
org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0
```

OCR uses `InputImage.fromFilePath(context, Uri.fromFile(file))` because the ML Kit API expects a `Uri`.

---

## 10. How to verify quickly

1. **Unit tests:** `./gradlew :app:testDebugUnitTest`
2. **App:** Run on device/emulator → **Try Demo** → pick a sample (e.g. multigrain bread vs cheeseburger) → results follow OCR + rules on real label text from the image.
3. **Camera/gallery:** Capture a label with readable English ingredients → results should reflect rules + OCR quality.

---

## 11. Related project docs

- `README.md` — contributor runbook (some stack lines still mention Room/OkHttp as planned; this update completes the OCR → rules path only).
- `change.md` — day-to-day change log; add entries when demo UX or pipeline behavior changes, and optionally point readers here for architecture detail.

---

*Document version: 2026-03-30 — OCR + rules integration; demo flow uses bundled sample images and `LabelScanPipeline.analyzeDemoAsset`.*
