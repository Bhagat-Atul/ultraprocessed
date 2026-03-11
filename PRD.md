# PRD: The Ultra Processed Detective
**Repo File:** `PRD.md`  
**Status:** Draft  
**Last Updated:** 2026-03-03  
**License Target:** MIT or Apache-2.0

---

## 1. Overview

Healthy marketing is often deceptive, and accurately identifying ultra-processed foods (as defined by the **NOVA** classification) is difficult for average consumers during grocery shopping. **The Ultra Processed Detective** is a **mobile, in-store “Red Light / Green Light” scanner** that reads ingredient labels using **OCR** and classifies products on the NOVA scale using an **LLM-based classification engine**.

The product’s primary value is speed and clarity: the user points their phone at an ingredient label, and the app instantly returns a high-contrast verdict (Green / Yellow / Red) with a short explanation and highlighted “problem” ingredients. The app is **stateless**, requires **no sign-in**, and **does not store user data** beyond the active session.

---

## 2. Goals and Non-Goals

### 2.1 Goals
- Provide a **fast, one-handed** in-store scanning experience.
- Convert ingredient list images into text via **OCR**, then classify into **NOVA Groups**.
- Show an immediate, unambiguous **traffic-light verdict**:
  - **Green**: likely NOVA 1 (Unprocessed / Minimally processed)
  - **Yellow**: likely NOVA 2–3 (Processed culinary ingredients / Processed foods) or **uncertain**
  - **Red**: likely NOVA 4 (Ultra-processed)
- Provide a **brief explanation** and **ingredient highlights** supporting the classification.
- Maintain **privacy-by-design**: no account, no retention of user scans.

### 2.2 Non-Goals
- Medical advice, dietary prescriptions, or allergy safety guarantees.
- Long-term user history, favorites, personalization, or meal planning.
- Full nutrition label parsing (calories/macros) beyond what is needed for NOVA inference.
- Barcode database lookups (optional future enhancement; not required for v1).

---

## 3. Target Audience and Context

### 3.1 Primary Users
- Grocery shoppers who want to avoid ultra-processed foods:
  - Parents buying for families
  - Health-conscious individuals
  - Consumers overwhelmed by marketing and “health halos”

### 3.2 Usage Context
- **In-store** grocery environments with:
  - Glare, curved packaging, small fonts
  - Variable lighting
  - Need for rapid interaction and one-handed use

### 3.3 Platform
- Mobile application (iOS + Android)  
- Designed for real-time camera scanning.

---

## 4. NOVA Classification Primer (Product Definition)

The NOVA system categorizes foods by degree and purpose of processing:

- **NOVA 1 — Unprocessed/Minimally Processed:** fresh/frozen fruits & vegetables, plain grains, milk, eggs, fresh meat; minimal alterations like drying, freezing, pasteurization.
- **NOVA 2 — Processed Culinary Ingredients:** oils, butter, sugar, salt—typically used in cooking, extracted from NOVA 1 foods.
- **NOVA 3 — Processed Foods:** foods made by adding NOVA 2 ingredients to NOVA 1 (e.g., canned vegetables with salt, simple breads, cheeses).
- **NOVA 4 — Ultra-Processed Foods:** industrial formulations with additives (e.g., emulsifiers, flavorings, colorants), often containing refined ingredients and designed for hyper-palatable convenience.

**Important note:** NOVA is sometimes ambiguous from ingredients alone (e.g., “flavorings” without specificity). The product must handle uncertainty safely.

---

## 5. User Experience Requirements

### 5.1 Information Architecture / Screens
1. **Home / Camera View (Default Landing)**
   - Immediate camera viewfinder
   - Minimal chrome (buttons: flash, help, accessibility toggle)
2. **Scanning State (Processing Overlay)**
   - “Reading ingredients…” overlay
   - Progress indicator
3. **Result Card (Modal / Overlay)**
   - Large high-contrast verdict shape + label:
     - Green circle (NOVA 1)
     - Yellow triangle/circle (NOVA 2–3 or uncertain)
     - Red octagon (NOVA 4)
   - Short explanation with highlighted ingredients
   - “Rescan” and “See details” actions

### 5.2 Interaction Flow
1. Open app → camera viewfinder appears instantly
2. Point at ingredient label
3. App captures frame(s) → OCR → classification
4. Overlay verdict appears (big, unmistakable)
5. Tap verdict to expand details (highlights + reasoning)
6. Exit/rescan; session ends with no data retention

### 5.3 Accessibility
- Large tap targets, one-handed reach zones
- High-contrast mode by default
- Support dynamic text sizing
- Haptics: short vibration on verdict (optional)

---

## 6. Functional Requirements (MVP)

### 6.1 Ingredient Scanning
- **FR-1:** Use device camera to capture ingredient label text.
- **FR-2:** Provide guidance overlays (e.g., “Center ingredients in frame”).
- **FR-3:** Handle common failure modes:
  - glare, blur, low light → prompt to rescan
  - multiple languages → best-effort detection (v1 may prioritize English)

**Acceptance Criteria**
- Scan-to-verdict median time ≤ 3 seconds on modern devices (target; measured in QA).
- User can rescan in one tap.

### 6.2 OCR Processing
- **FR-4:** Extract ingredient list text from captured image.
- **FR-5:** Provide lightweight pre-processing:
  - cropping heuristics, contrast enhancement (on-device)
- **FR-6:** Return structured OCR result:
  - `raw_text`, `normalized_text`, `confidence`, and bounding boxes (if available)

**Implementation Options**
- On-device: Google ML Kit (Android) / Vision (iOS) / ML Kit for iOS
- Fallback: Tesseract (if needed), though typically weaker on mobile packaging.

### 6.3 NOVA Classification Engine (LLM Logic Layer)
- **FR-7:** Classify into NOVA groups (1–4) plus return a confidence score.
- **FR-8:** Map NOVA result to traffic light:
  - NOVA 1 → Green
  - NOVA 2–3 → Yellow
  - NOVA 4 → Red
  - Low confidence / ambiguous → Yellow + “Uncertain”
- **FR-9:** Explain “why”:
  - list of suspected ultra-processing markers (e.g., emulsifiers, artificial flavors)
  - short natural-language summary (1–2 sentences)
- **FR-10:** Provide ingredient highlights:
  - show detected ingredients flagged as markers and emphasize in UI

**Guidance**
- The engine must be deterministic and auditable:
  - store prompt templates in-repo
  - log only ephemeral session diagnostics (no images/text persisted)

### 6.4 Visual Verdict UI
- **FR-11:** Show a large verdict shape overlay during and after scan.
- **FR-12:** Provide expandable details modal with explanation + highlights.
- **FR-13:** Include mandatory branding at bottom of app:
  - Phrase: **“Built with ❤️for Humanity. The Benevolent Bandwidth Foundation”**
  - Mini **B2** logo (asset provided by design)

### 6.5 Disclaimers / Safety Copy
- **FR-14:** Always display informational disclaimer in details view:
  - “For informational purposes only. Not medical advice.”
  - “Allergen safety is not guaranteed—check labels and consult professionals.”

---

## 7. Non-Functional Requirements

### 7.1 Privacy & Data Handling (Hard Requirements)
- **NFR-1:** No user accounts / authentication.
- **NFR-2:** No persistent storage of scanned images or ingredient text.
- **NFR-3:** Session data cleared on:
  - closing result, app background timeout, or app close
- **NFR-4:** If using a remote LLM API:
  - transmit only the **extracted ingredient text** (not the image)
  - avoid sending user identifiers
  - ensure TLS in transit

### 7.2 Performance
- **NFR-5:** Target < 3s median from capture → verdict (device/network dependent).
- **NFR-6:** App should remain usable on mid-tier devices.

### 7.3 Reliability & Robustness
- **NFR-7:** Graceful degradation:
  - OCR fails → show “Couldn’t read label. Try again.”
  - LLM fails/timeouts → show “Unable to classify right now.” + rescan

### 7.4 Security
- **NFR-8:** Secure API keys using platform secret management:
  - iOS Keychain / Android Keystore
  - Backend proxy recommended (avoid shipping raw LLM keys in client)
- **NFR-9:** No PII collection; no analytics that fingerprint users.

---

## 8. Technical Architecture (Proposed)

### 8.1 On-Device First (Recommended)
1. Camera capture (frame selection)
2. On-device OCR
3. Text normalization + ingredient parsing
4. Classification:
   - Option A: LLM via secure backend proxy (preferred for quality)
   - Option B: Lightweight on-device model (optional future)
5. UI verdict + explanation

### 8.2 Backend (If LLM is remote)
- **Stateless classification endpoint**:
  - Input: normalized ingredient list text + locale + model version
  - Output: nova_group, confidence, markers, explanation
- **No data persistence** (no logs containing ingredient text; only aggregated metrics if allowed)

---

## 9. Prompting & Classification Strategy

### 9.1 Deterministic Outputs
- Enforce structured output (JSON schema) from the LLM:
  - `nova_group: 1|2|3|4`
  - `confidence: 0..1`
  - `markers: [string]`
  - `explanation: string`
  - `highlight_terms: [string]`

### 9.2 Heuristic + LLM Hybrid (Stability Recommendation)
To improve reliability, use a hybrid approach:
- **Rule-based pre-checks** for common NOVA 4 markers (e.g., “emulsifier”, “artificial flavor”, “hydrolyzed”, specific additive codes where applicable)
- LLM for contextual interpretation and edge cases
- If heuristic strongly indicates NOVA 4 and OCR confidence is high, allow direct Red verdict even if LLM is uncertain (configurable).

---

## 10. Evaluation Plan (Reliability is Key)

### 10.1 Offline Benchmark Dataset
- Curate a labeled set of ingredient lists:
  - Balanced across NOVA 1–4
  - Reviewed by nutrition-literate reviewers (and ideally a dietitian for spot checks)
- Track:
  - Accuracy by NOVA group
  - Confusion matrix (esp. 3 vs 4)
  - Calibration: does confidence correlate with correctness?

### 10.2 OCR Quality Metrics
- Character error rate (CER) on a representative packaging set
- Failure rate by environment (lighting, distance, glare)

### 10.3 Product KPIs (Privacy-Safe)
Given no user data retention:
- Only capture aggregated, non-identifying counts if permitted:
  - number of scans per session (in-memory, aggregated)
  - OCR failure rate
  - classification timeout rate
  - median latency buckets (e.g., 0–1s, 1–3s, 3–7s, >7s)
> If B2 principles forbid any telemetry, all metrics must be local-only during QA.

---

## 11. Risks and Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| NOVA ambiguity from ingredients alone | Misclassification | “Uncertain” state (Yellow), confidence scoring, conservative defaults |
| OCR errors on small fonts / glare | Wrong classification | Multi-frame capture, user prompts, OCR confidence gating |
| LLM variability | Inconsistent results | Strict JSON schema output, temperature=0, hybrid heuristic checks |
| Liability: health decisions | Medium | Prominent disclaimer, avoid medical guidance, uncertainty handling |
| API key exposure (if remote LLM) | High | Backend proxy + rotation, do not embed raw keys in client |

---

## 12. Compliance and Ethics

### 12.1 Ethical Requirements
- No data storage; destroy session data.
- Make uncertainty visible; do not overclaim.
- Not a medical device; no allergy guarantees.

### 12.2 B2 Core Principles
The app must adhere to B2’s core principles as described in the referenced document:
- **Reference:** `docs/B2_CORE_PRINCIPLES.md` (to be added to repo)
- Engineering must incorporate any constraints (telemetry, hosting, governance) once provided.

---

## 13. Milestones (Proposed)

### M0 — Foundations (Week 1–2)
- Repo setup, CI, basic camera screen, OCR POC

### M1 — End-to-End MVP (Week 3–5)
- OCR → classification → verdict UI working
- No data retention, disclaimer copy, branding footer

### M2 — Reliability Pass (Week 6–8)
- Benchmark dataset + eval harness
- Hybrid rules + LLM schema hardening
- UI for “Uncertain” state and rescan prompts

### M3 — Polish + Release Prep (Week 9–10)
- Accessibility pass
- Performance tuning
- App store readiness (if applicable)

---

## 14. Open Questions
- Will v1 require offline-only operation, or is network access acceptable in stores?
- Primary language support (English only for v1 vs multi-lingual)?
- Is barcode scanning desired for faster product identification (future)?
- Final decision on license: MIT vs Apache-2.0
- Confirm allowed telemetry under B2 principles (none vs aggregated only)

---

## 15. Appendix: Suggested Repo Structure

```text
/
├── PRD.md
├── README.md
├── LICENSE
├── docs/
│   ├── B2_CORE_PRINCIPLES.md          # to be provided
│   ├── NOVA_GUIDELINES.md             # internal interpretation + examples
│   └── PROMPT_TEMPLATES.md
├── app/
│   ├── mobile/                        # iOS/Android code
│   └── shared/                        # shared logic (parsing/classification contracts)
├── backend/                           # optional stateless proxy
└── eval/
    ├── dataset/                       # non-sensitive ingredient list dataset
    └── harness/                       # scripts to evaluate OCR + classifier