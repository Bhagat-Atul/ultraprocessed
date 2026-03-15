# Ultra Processed Detective

Android app for detecting ultra-processed foods from ingredient labels.

## Stack

- Kotlin
- Jetpack Compose
- CameraX
- ML Kit OCR
- Room
- DataStore
- Android Keystore
- OkHttp

## Core Idea

The app is built around a pluggable classifier layer instead of one hardcoded engine.

Flow:

`camera -> OCR -> ingredient normalization -> choose engine -> verdict -> save locally`

## Classifier Engines

- `RulesClassifier`
  - Always available
  - Fast offline safety net
  - Handles obvious NOVA 1 and NOVA 4 patterns
- `OnDeviceLLMClassifier`
  - Local AI path
  - Used when the device supports on-device inference
- `ApiLLMClassifier`
  - Direct HTTPS provider calls using a user-provided key
  - No backend dependency required

## Project Structure

```text
app/
  ui/
  camera/
  ocr/
  classify/
    Classifier.kt
    RulesClassifier.kt
    OnDeviceLLMClassifier.kt
    ApiLLMClassifier.kt
    ClassifierOrchestrator.kt
  storage/
    room/
    datastore/
    secrets/
  network/
  settings/
```

In source form, this lives under:

`app/src/main/java/com/b2/ultraprocessed/`

## Documents

- [design.md](/Users/ambarsariya/Documents/Learnings/B2/ultraprocessed/design.md)
- [requirements.md](/Users/ambarsariya/Documents/Learnings/B2/ultraprocessed/requirements.md)
- [PRD.md](/Users/ambarsariya/Documents/Learnings/B2/ultraprocessed/PRD.md)

## Build Plan

1. Compose UI + CameraX + ML Kit OCR + RulesClassifier
2. Settings + Keystore + ApiLLMClassifier
3. On-device LLM + capability detection + orchestration

## Notes

- The earlier temporary UI mock files have been removed from the app source.
- The `design/` folder still contains Figma-ready handoff assets for product design work.
