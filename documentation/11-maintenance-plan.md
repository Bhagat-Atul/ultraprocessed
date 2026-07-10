# Zest Maintenance Plan

## Purpose

This document defines how Zest is maintained after launch. It supplements the engineering backlog in [09-todo-roadmap.md](09-todo-roadmap.md) and the technical release gates in [07-testing-release.md](07-testing-release.md).

The plan covers the Android application, the Cloud Run backend proxy, backend-owned prompts, USDA lookup integration, session-only storage policy, and user-facing support.

## Ownership

### Accountable Group

The B2 group is accountable for the product after launch, including prioritization, release approval, incident response, and maintenance of this plan.

Every release must identify the following owners in the release record:

- **Release owner:** coordinates the release checklist, versioning, artifact validation, and rollout decision.
- **Application owner:** handles Android UI, camera/OCR, navigation, session cleanup, and client-side regressions.
- **Backend owner:** handles Cloud Run deployment, Vertex AI access, API behavior, prompt assets, quotas, and backend incidents.
- **Product/support owner:** triages user reports, decides user-facing severity, and confirms support communication.

These are operational roles, not permanent staffing assignments. One person may hold more than one role for a small release, but the release record must still name the assignments.

### Source Of Truth

- GitHub issues are the source of truth for defects, maintenance tasks, and follow-up actions.
- Git history is the source of truth for code and prompt changes.
- GitHub releases contain the shipped Android version, release notes, validation status, and rollback notes.
- Backend deployment records identify the Cloud Run revision, model, region, and environment configuration used for each deployment.
- Do not record API keys, service-account credentials, user scan content, or raw diagnostic traces in issues or release notes.

## Maintenance Cadence

### Per Change

The change author must:

- Update the relevant documentation when an architecture, API contract, storage, prompt, privacy, or release behavior changes.
- Add or update tests for changed behavior.
- Identify whether the change affects the Android app, backend, prompt behavior, data storage, or release process.
- Record known limitations and rollback considerations in the issue or pull request.

### Weekly Triage

The product/support owner reviews new issues and support reports at least weekly and assigns:

- severity,
- affected release or backend revision,
- component owner,
- reproducibility status,
- next action and target date.

Duplicate, unreproducible, and expected-behavior reports should be closed with a clear explanation rather than silently ignored.

### Monthly Operational Review

The B2 group reviews at least monthly:

- crash, failed-scan, timeout, quota, and backend error reports;
- Cloud Run health and recent deployment history;
- `/healthz` availability and representative `/analyze` and `/chat` behavior;
- benchmark results when latency or model configuration changes;
- incorrect classification, ingredient cleanup, allergen, or chat reports;
- dependency, SDK, Android, and model-provider changes;
- open security, privacy, abuse-control, and documentation risks.

The outcome is recorded in a GitHub issue, release note, or maintenance log. If no action is required, record that decision and the review date.

### Release Cycle

Before each Android or backend release, the release owner confirms:

- the change scope and assigned owners;
- the appropriate unit, Android, source-tree, compile, minification, and release checks from [07-testing-release.md](07-testing-release.md);
- session-only storage and privacy impact;
- prompt and response-contract compatibility;
- backend model, region, quota, and environment configuration;
- privacy and diagnostic logging behavior;
- user-facing release notes and known limitations;
- rollback or mitigation steps.

## Incident Classification

### Severity 1: Critical

Examples:

- confirmed exposure of credentials, private scan content, or diagnostic data;
- a backend issue causing widespread requests to fail or unexpectedly increasing provider cost;
- materially unsafe allergen or medical guidance behavior;
- a release that prevents users from starting or completing ordinary scans.

Response:

- acknowledge immediately when discovered;
- assign an incident coordinator and component owner;
- stop or roll back the affected release/deployment when practical;
- disable the affected capability if rollback is not sufficient;
- preserve relevant non-sensitive logs and deployment identifiers;
- communicate impact and workaround to affected users when applicable;
- create a post-incident issue with root cause, corrective action, and verification evidence.

### Severity 2: High

Examples:

- repeated backend timeouts, 5xx responses, or quota failures;
- incorrect result parsing, missing allergen output, broken session cleanup, or failed scan behavior;
- a regression affecting a significant scan path but leaving a workaround.

Response:

- triage within one business day;
- reproduce with a safe fixture or redacted input;
- determine whether the issue is client, backend, provider, prompt, USDA, or data-migration related;
- ship a patch or documented workaround as soon as practical;
- verify the fix against the relevant release checks.

### Severity 3: Normal

Examples:

- isolated OCR quality issues;
- UI defects with a usable workaround;
- documentation gaps, minor prompt wording issues, or non-blocking performance regressions.

Response:

- schedule through the normal backlog;
- include regression coverage when the issue is fixed;
- close with the affected version and verification result.

## Incident Workflow

1. **Detect:** Use user reports, Android failures, backend responses, Cloud Run logs, health checks, or release validation.
2. **Classify:** Assign severity, affected component, first known version/revision, and whether privacy or safety is involved.
3. **Contain:** Stop rollout, revert the Android artifact, deploy a known-good Cloud Run revision, disable a failing capability, or provide a user workaround.
4. **Diagnose:** Compare the failing request path across OCR, USDA lookup, proxy input, prompt, model response, parser, session cleanup, and UI mapping. Use test fixtures or redacted data only.
5. **Correct:** Implement the smallest safe fix. Prompt changes must preserve the structured response contract and safety boundaries.
6. **Verify:** Run the relevant automated and release checks, plus a representative manual scan when the issue is user-facing.
7. **Communicate:** Update the issue and release/support record with impact, status, workaround, and resolution.
8. **Prevent:** Add a regression test, guard, documentation update, or operational check when the incident reveals a repeatable failure mode.

## Patch Policy

### Security And Privacy

Security or privacy patches take priority over feature work and may be released outside the normal cadence. The patch must include:

- affected versions or revisions;
- exposure assessment;
- containment and remediation;
- credential rotation instructions when relevant;
- verification that secrets and user content are not present in committed artifacts or logs.

The unauthenticated Cloud Run proxy remains a limited-rollout risk until an approved abuse-control layer is deployed. Any change to that posture requires an explicit security review and release record.

### Android Patches

Android patches must account for:

- camera and gallery permissions and local image lifecycle;
- on-device OCR behavior;
- system back and navigation behavior;
- session-only image and result lifecycle;
- failed-scan cleanup paths;
- release signing, R8, resource shrinking, and diagnostics flags.

Do not ship a storage change that persists human scan data without an explicit product-policy change, privacy review, retention/deletion design, and migration coverage. Do not ship a release with analysis diagnostics that write OCR or model data to disk or logs.

### Backend And Prompt Patches

Backend or prompt patches must account for:

- `POST /analyze` and `POST /chat` request and response schemas;
- non-food rejection behavior;
- corrected ingredient and ultra-processed marker semantics;
- separate allergen output and advisory-text handling;
- confidence and warning fields;
- result-chat scope and prompt-injection refusal behavior;
- provider usage, timeout, quota, and generic public error handling.

Prompt changes are production changes. They require prompt contract tests and a small representative fixture check before deployment. Do not change the model, region, or prompt without recording the expected behavior and rollback revision.

### Dependency And Platform Patches

Review Android, Kotlin, Gradle, ML Kit, FastAPI, Google GenAI, and provider model changes for compatibility before adoption. Apply security fixes promptly; batch routine upgrades into a planned maintenance change with build and release verification.

## Rollback And Recovery

### Android

- Keep the previous signed release artifact and release notes available.
- If a shipped Android version blocks core scanning, prioritize store rollback or a hotfix.
- Do not reactivate archived Room/history code without a deliberate retention, deletion, consent, and migration strategy.
- Preserve the no-human-data-storage policy unless a documented product-policy change explicitly supersedes it.

### Backend

- Record every Cloud Run revision, model, region, and environment change.
- On a backend regression, route traffic to the last known-good revision or redeploy the last known-good source.
- If the provider is unavailable, keep public errors generic and provide user retry guidance.
- Re-run `/healthz`, a representative analysis request, and the benchmark when latency or model behavior is implicated.

## Monitoring And Evidence

The current system is local-first and does not provide a centralized user analytics stream. Maintenance evidence therefore comes from:

- Android failure and support reports;
- safe, non-sensitive diagnostic output when explicitly enabled;
- Cloud Run logs and revision history;
- `/healthz` checks;
- contract and unit tests;
- release builds and Android tests;
- representative backend benchmark results;
- documented model, prompt, dependency, and schema changes.

Do not enable diagnostic logging as a substitute for privacy-safe production observability. Diagnostics must not contain OCR text, model context, label images, chat questions, or model responses.

## Support And User Communication

Support guidance should first direct users to the existing troubleshooting steps in the root README: use a clearer ingredient-panel image, retry temporary service failures, and verify USDA configuration for barcode scans.

When an incident affects users, communicate:

- what is affected;
- the affected app version or service period;
- whether temporary scan images, OCR text, or session results may have been affected;
- the available workaround;
- whether users need to update or retry;
- when the next update is expected.

Do not request users to send label images, API keys, OCR text, chat transcripts, or private scan content through public issue threads. Use synthetic fixtures for reproduction.

## Plan Review

The B2 group reviews this plan at least annually and after any Severity 1 incident, ownership change, backend architecture change, or launch of a new provider or data source. Updates must be reviewed alongside the affected architecture, security, testing, or product documentation.
