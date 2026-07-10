# Zest Product Requirements

## Product Summary

Zest helps people understand how processed a packaged food is while they are shopping. It turns an ingredient label into a NOVA-style classification, readable ingredient signals, allergen information, and a concise explanation.

Zest is a decision-support tool, not medical advice, a nutrition-label verifier, or a replacement for a user's dietary judgment.

## Problem

Ingredient panels are dense, unfamiliar, and difficult to interpret quickly. Shoppers need a low-friction way to identify processing signals and potential allergens without sending label images or maintaining an account.

## Target User

The primary user is a grocery shopper using an Android phone who wants quick, understandable ingredient context for a packaged food. The user may have limited knowledge of food additives, NOVA terminology, or allergen labeling.

## Core Session Definition

A **scan session** begins when the user starts a label or barcode scan and ends when the active result/error flow is abandoned or the app process ends. Scan data is never saved beyond that session.

The expected successful session is:

1. Open Zest and choose camera, gallery, or barcode input.
2. Capture or select a label, or provide a barcode.
3. Extract ingredient text on device when an image is used.
4. Analyze extracted text or USDA-enriched product data through the Zest backend.
5. Review the NOVA-style result, explanation, ingredient signals, and separate allergen block.
6. Optionally ask a question scoped to the current result.

Images must remain on the device. Only extracted text, corrected ingredient names, and necessary barcode/product data may leave the device.

## Product Scope

### In Scope

- Camera capture and gallery import for ingredient labels.
- On-device OCR before AI analysis.
- Barcode lookup through USDA when configured and available.
- NOVA-style classification with a non-food rejection gate.
- Corrected ingredient display with ultra-processed markers highlighted.
- Separate allergen detection and presentation.
- Result-scoped follow-up chat.
- Local encrypted storage for configured secrets.
- Clear errors for unreadable, invalid, unavailable, or rejected inputs.

### Non-Goals

- Medical, allergy, or dietary diagnosis.
- Guaranteed nutrition, ingredient, or product-label verification.
- General-purpose conversational AI.
- Mandatory accounts, cloud history, or cross-device synchronization.
- Sending captured label images to AI providers.
- A rule-based NOVA fallback that bypasses the backend analysis contract.

## Functional Requirements

### Input And Extraction

- Users can start a session from camera, gallery, or barcode input.
- OCR runs locally for image-based scans.
- OCR failure stops the pipeline before any AI request.
- Non-food or insufficient ingredient evidence is rejected with a readable reason.

### Analysis And Results

- The result shows a NOVA group, concise summary, confidence, and warnings when provided.
- Corrected ingredients are shown as compact, readable items.
- Ingredients identified as ultra-processed markers are visually distinct from other ingredients.
- Allergen signals are shown in their own section and are not merged into processing colors.
- Follow-up questions are constrained to the current scan result.

### Session And Privacy

- Completed and failed scans exist only in the active session.
- Scan images, OCR, results, chat, usage, and failures are not stored on the device or in logs.
- Images are not sent to AI providers.
- Provider usage metadata is shown when available; local estimates are fallback-only.

## Acceptance Criteria

- A user can complete a label scan without creating an account.
- An image scan performs OCR locally and sends no image bytes to an AI provider.
- A valid food label produces all required result blocks: classification, summary, ingredients, and allergens.
- An invalid or non-food input ends with an actionable explanation and no misleading classification.
- A user can distinguish ultra-processed ingredient markers from other corrected ingredients.
- A user can ask about the current scan without starting a general chat.
- A completed scan is discarded when the active session ends or the app restarts.
- Missing or failing backend, USDA, OCR, or configuration dependencies produce explicit user-facing errors.

## Trust And Safety Boundaries

- Zest must clearly communicate that results are informational.
- Allergen results must not be presented as a guarantee of safety.
- Classification confidence and warnings should remain visible when supplied by the analysis contract.
- Backend abuse controls are required before broad production exposure of the proxy.

## Success Signals

- Users can reach a result from a readable label with minimal interaction.
- Users understand the result without needing technical knowledge of NOVA or model behavior.
- Failed scans explain what to fix and support a practical retry path.
- Repeat usage starts a new session without requiring sign-in.

## Open Product Decisions

- Define launch targets for scan completion rate, median time to result, and retry recovery rate.
- Decide which confidence and warning language is appropriate for consumer release.
- Define production authentication and abuse-control requirements for the backend proxy.
- Confirm the supported geography and allergen taxonomy beyond the current US / Western focus.
