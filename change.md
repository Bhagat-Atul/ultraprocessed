# Change Log

This file records permanent project-level decisions made while shaping the runnable Android UI.

## 2026-03-29

- Reoriented the imported Figma UI into a native Jetpack Compose flow so the app can be rendered directly from Android Studio without requiring the original Vite/React runtime.
- Added a stubbed navigation flow in `app/src/main/java/com/b2/ultraprocessed/ui/` covering splash, scanner, analyzing, results, settings, and history.
- Added `AppModels.kt` with shared UI models plus deterministic stub data for scan results, model options, and history.
- Upgraded `MainActivity.kt` into the app entry point for the full Compose flow and wired screen transitions with local state instead of backend integration.
- Added missing Android resource files under `app/src/main/res/values/` so the manifest theme resolves correctly.
- Added `gradle.properties` and kept `settings.gradle.kts` as the Android Studio entrypoint with `:app` included.
- Preserved the classifier/domain packages as future integration targets; the current UI intentionally uses local stubs so contributors can build screens before camera, OCR, storage, and networking are finished.
- Kept the native Compose copy visually aligned with the Figma-exported UI by preserving the same screen flow, wording, section order, and verdict presentation.
- Removed duplicated web-export runtime files from `app/` (`src/app`, `src/styles`, `main.tsx`, Vite metadata, and generated build artifacts) so contributors only see one Android UI implementation inside the module.
- Added a shared footer composable and placed the Benevolent Bandwidth legal line plus the "Built with heart for humanity" line across all UI screens in a consistent small-text treatment.
- Renamed the in-app product branding to `Zest` and refreshed the splash screen to behave like a real brand landing screen with cleaner hierarchy, better spacing, and footer-safe layout.
- Added basic CameraX integration with live preview inside the scanner frame, runtime camera permission handling, and local photo capture to app storage when the user taps `Scan Label`.
- Replaced the one-off top bars with a single reusable `AppHeader` component that carries brand presence, page hierarchy, navigation, metadata chips, and consistent action buttons across splash, scanner, analyzing, results, settings, and history.
- After review, simplified the shared `AppHeader` back down to a cleaner Android-style top bar and removed the heavy card/chip treatment that made the UI feel overdesigned.
- Simplified the splash screen by removing the duplicated branded header and returning it to a centered logo, title, subtitle, and loading state.
- Added explicit top safe-area padding to the shared header so content screens sit below the status bar with cleaner spacing while keeping the underlying vertical rhythm intact.
- Extracted the app state host into `UltraProcessedApp.kt` so navigation and timing can be tested independently from `MainActivity`.
- Added test tags for the shared shell and scanner actions to support Compose UI automation.
- Added an initial QA baseline: unit tests for classifier logic, functional Compose tests for shared chrome, and an integration-style demo navigation test.
- Added persistent QA artifacts under `qa/`, including `test_cases.csv` and `quality_report.md`, so contributors can extend the test plan instead of rebuilding it from scratch.
- Added gallery upload support to the scanner flow using Android's content picker, with imported photos copied into app-local storage and routed through the same stubbed analysis/history pipeline as camera captures.
