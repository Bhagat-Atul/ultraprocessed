# Ultra Processed Detective

Android app for detecting ultra-processed foods from ingredient labels.

Current UI branding in the app is `Zest`.

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

## Current App State

- The app now runs as a native Android app in Android Studio using Jetpack Compose.
- The current UI flow includes splash, scanner, analyzing, results, settings, and history screens.
- Camera preview, camera capture, and gallery upload are wired in.
- Backend integration is not finished yet, so the analysis/result flow still uses local stub data.
- This means you can run the app and test the UI flow now, even without a backend.

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

## Run In Android Studio

### 1. Prerequisites

Before opening the project, make sure you have:

- Android Studio installed
- An internet connection for the first Gradle sync and SDK downloads
- At least one Android device option:
  - an Android emulator created in Android Studio, or
  - a physical Android phone with USB debugging enabled

You do **not** need to install Java separately in most cases because Android Studio ships with what this project needs.

### 2. Open the project

1. Open Android Studio.
2. Click `Open`.
3. Select the project root folder:
  `ultraprocessed`
4. Wait for Android Studio to index the project.

Important: open the `ultraprocessed` folder itself, not the old Figma web-export folder.

### 3. Let Gradle sync finish

When Android Studio opens the project, it will start a Gradle sync.

During this step, Android Studio may:

- download Gradle dependencies
- download missing Android SDK components
- ask you to install recommended plugins or tools

Do this:

1. Wait for the sync to finish.
2. If Android Studio shows an `Install missing SDK` or similar button, click it.
3. If prompted to trust the project or load Gradle scripts, accept it.

Do not try to run the app until the Gradle sync is complete.

### 4. Create an emulator

If you are not using a real phone, create an emulator:

1. In Android Studio, open `Device Manager`.
2. Click `Create Device`.
3. Choose a `Pixel` phone model.
4. Pick an Android image:
  - Android 13 or 14 is a good default
5. Finish creating the device.

Recommended setup:

- Device: Pixel
- Android version: 13 or 14
- Back Camera: `Emulated` first

### 5. Enable camera on the emulator

Because this app uses CameraX, camera configuration matters.

To enable camera support:

1. In `Device Manager`, click the pencil/edit icon on your emulator.
2. Open `Show Advanced Settings`.
3. Find the `Camera` section.
4. Set `Back Camera` to one of these:
  - `Emulated` for the easiest setup
  - `Webcam0` if you want to use your laptop webcam
5. Save the emulator settings.
6. Start the emulator.

If you use `Webcam0` and the emulator says the camera is closed:

- switch back to `Emulated` first
- close Zoom/Meet/FaceTime/Teams/browser tabs using camera
- on macOS, allow camera access for Android Studio in:
  - `System Settings > Privacy & Security > Camera`

### 6. Run the app

Once Gradle sync is complete and a device is available:

1. At the top of Android Studio, make sure the run configuration is `app`.
2. Select your emulator or physical device.
3. Click the green `Run` button.

Android Studio will build the app and install it on the selected device.

### 7. First launch behavior

On first launch:

- the splash screen will appear
- the app may ask for camera permission
- you should tap `Allow` if you want live camera scanning

If you do not want to use the camera right away, you can still use:

- `Upload Photo`
- `Try Demo`

### 8. What works right now

You can currently:

- open the app in Android Studio
- navigate through the Compose UI flow
- open the live camera preview
- take a photo using the camera
- upload a photo from the gallery
- open settings and history
- move through the analyzing/results flow

Important note:

- the result pipeline is still stubbed, so the analysis screens are UI/demo-ready rather than backend-complete

### 9. Run from terminal instead of the Run button

If you prefer terminal commands:

```bash
cd ultraprocessed
./gradlew :app:assembleDebug
./gradlew :app:installDebug
```

You still usually need Android Studio at least once for SDK setup and emulator management.

### 10. Run tests

To run the current unit test suite:

```bash
cd ultraprocessed
./gradlew :app:testDebugUnitTest
```

### 11. Troubleshooting

#### Gradle sync fails

Try:

- `File > Sync Project with Gradle Files`
- `Build > Clean Project`
- `Build > Rebuild Project`

#### Camera does not open

Check:

- emulator `Back Camera` is set to `Emulated` or `Webcam0`
- app camera permission is allowed
- no other app is using your webcam

#### Emulator says camera is closed

Most often this is an emulator/webcam configuration problem, not an app code problem.

Best fix order:

1. switch `Back Camera` to `Emulated`
2. cold boot the emulator
3. rerun the app

#### App builds but scanner still cannot be used

You can still test the UI flow with:

- `Upload Photo`
- `Try Demo`

## Recommended First-Time Contributor Workflow

If this is your first time running the project, the safest order is:

1. Open the project in Android Studio
2. Let Gradle sync finish
3. Create/start an emulator
4. Run the app
5. Verify splash, scanner, settings, and history screens
6. Run unit tests with `./gradlew :app:testDebugUnitTest`

## Contributors

This section is for people contributing to the project, especially if you are new to Android or new to this codebase.

### Technical Advisor

- Atul Bhagat - LinkedIn: https://www.linkedin.com/in/bhagatatul/

### Contributors

- Emmy - LinkedIn: 
- Emre Can Baykurt - LinkedIn: https://www.linkedin.com/in/ebaykurt/
- Samet Temurcin - LinkedIn: https://www.linkedin.com/in/samet-temurcin/
- Ola Ajayi - LinkedIn: https://www.linkedin.com/in/olaajayi1234/


### Where to start

If you want to contribute safely, start in one of these areas:

- `ui/`
  - screen layout, spacing, visual polish, shared composables
- `camera/`
  - camera preview, capture, gallery import
- `classify/`
  - rules, orchestration, model routing
- `qa/`
  - test cases, test reports, QA planning

### Suggested contribution order

If you are new, a good order is:

1. Run the app successfully in Android Studio
2. Read `change.md`
3. Read this README fully once
4. Pick one small area
5. Make a focused change
6. Run tests before raising a PR

### Before making changes

Please do these first:

1. Pull the latest changes
2. Open the app and make sure it runs
3. Check [change.md](change.md) to understand recent decisions
4. Check whether the change belongs to:
  - UI polish
  - camera/import flow
  - classification logic
  - storage/backend integration
  - QA/docs

### When making UI changes

Please keep these principles in mind:

- prefer clarity over decoration
- keep layouts simple
- avoid adding extra visual chrome unless it clearly improves usability
- try to stay aligned with the Figma intent
- keep screens easy to scan and easy to maintain

### When making functional changes

If you touch scanner, capture, import, or classification flow:

- keep stub behavior working unless you are replacing it end-to-end
- do not break the demo flow
- keep local storage behavior predictable
- update tests if behavior changes

### Tests contributors should run

At minimum, run:

```bash
cd ultraprocessed
./gradlew :app:testDebugUnitTest
```

If you have an emulator/device ready, also run Android instrumentation tests when possible.

### Documents contributors should update

If your change is meaningful, update the relevant documents:

- [change.md](change.md)
- [README.md](README.md)
- [qa/test_cases.csv](qa/test_cases.csv)
- [qa/quality_report.md](qa/quality_report.md)

### Good PR habits for contributors

- keep PRs focused
- explain what changed and why
- mention what you tested
- mention anything still stubbed or incomplete
- include screenshots for UI changes

### Current contribution reality

This project is still in an active build-up phase.

That means:

- some flows are UI-complete but backend-stubbed
- camera and gallery flows are available for UI/testing use
- classification and storage layers are partially scaffolded for future work

So the best contributions right now are usually:

- making the Android UI cleaner and more usable
- improving stability
- improving test coverage
- improving docs for future contributors

## Documents

- [design.md](design.md)
- [requirements.md](requirements.md)
- [PRD.md](PRD.md)

## Build Plan

1. Compose UI + CameraX + ML Kit OCR + RulesClassifier
2. Settings + Keystore + ApiLLMClassifier
3. On-device LLM + capability detection + orchestration

## Notes

- The earlier temporary UI mock files have been removed from the app source.
- The `design/` folder still contains Figma-ready handoff assets for product design work.
- The persistent implementation notes are maintained in [change.md](change.md).
- QA planning artifacts live in:
  - [qa/test_cases.csv](qa/test_cases.csv)
  - [qa/quality_report.md](qa/quality_report.md)
