# Storage And Security

Zest has a no-human-data-storage policy. Scan images, OCR text, normalized ingredients, analysis results, chat context, usage metadata, and failures are session-scoped and are not persisted by the Android app or backend logs.

## Active storage contract

- Secrets: only the USDA lookup key is stored in encrypted preferences when configured.
- Preferences: sound and disclaimer state contain no scan content.
- Scan content: held in Compose memory for the active scan flow only.
- Images: camera/import working files are deleted after analysis succeeds or fails, on app teardown, and at the next startup to remove abandoned files after process death.
- Logs: `AnalysisDebugLogger` is a source-compatible no-op. OCR, requests, responses, failures, and telemetry are never written to Logcat or files.
- Network: OCR text and result/chat context are sent to the backend proxy for the active request. The backend must keep request logging disabled or redacted and must not persist request bodies.

## Archived capability

The former Room database, entity, DAO, History screen, and migration test are retained under `documentation/code-archive/session_only_storage/`. That directory is outside the Android Gradle source sets, so it cannot be compiled into or shipped in the APK. It is reference material only and must not be reactivated without an explicit product-policy change and privacy review.

The active app has no Room dependency, database initialization, History destination, result insert path, failed-scan insert path, or history UI action. The current result and chat transcript disappear when the process/session ends.

## Data boundaries

Local transient data:

- Camera captures and imported images while a scan is running.
- On-device OCR output while the current analysis is running.
- Current result and current result-chat messages while the result screen is open.

Network data:

- USDA receives barcode/product lookup data when that feature is used.
- The backend receives extracted text for analysis and current-result context plus chat history for result chat.
- Images are not sent to the AI backend.

## Secret lifecycle

USDA access uses Android Keystore-backed encrypted preferences. AI model credentials are not entered or stored in the app; the backend uses its Cloud Run runtime identity for Vertex AI.

## Verification requirements

Release verification must prove:

1. The APK has no Room classes or History route reachable from the UI.
2. A successful scan does not create a database, scan row, image-retention record, or diagnostic file.
3. A failed scan does not create a database row or diagnostic file.
4. Relaunching the app does not restore a previous result, chat transcript, OCR value, or capture.
5. Capture/import directories are empty after success, failure, teardown, and relaunch cleanup.
6. Release logs contain no OCR text, model request/response body, or user question.

Any future request to restore history must first define retention, deletion, encryption, consent, and log-redaction requirements and update this document before code is reactivated.
