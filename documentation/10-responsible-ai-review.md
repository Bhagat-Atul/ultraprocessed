# Responsible AI Review

## Review Scope

This review covers the implemented label-analysis and result-chat paths, with attention to harmful behavior related to gender, language, geography, and socioeconomic status.

Reviewed implementation:

- Image scans run ML Kit OCR on device in `app/src/main/java/com/b2/ultraprocessed/analysis/FoodAnalysisPipeline.kt`.
- The production proxy receives extracted ingredient text, not label images, in `app/src/main/java/com/b2/ultraprocessed/network/llm/ProxyFoodLabelLlmWorkflow.kt`.
- The backend assembles the full-analysis and result-chat prompts in `backend/prompt.py`.
- The production analysis rules are in `backend/prompts/food_label_full_analysis_prompt.md` and `backend/prompts/food_label_result_chat_prompt.md`.

## Review Result

No explicit harmful behavior related to gender, identity, socioeconomic status, or geographic targeting was found in the reviewed code or prompts.

Specifically:

- The app does not collect or use gender, identity, income, or socioeconomic attributes.
- The prompts do not assign food preferences, health behavior, or capability based on gender or identity.
- Product and brand context is not used to make demographic or socioeconomic judgments.
- Result chat is restricted to the current scan and is instructed not to provide medical advice or unrelated content.
- Summaries are required to be plain-language shopping guidance and not to overstate safety.

## Concrete Residual Risks

- The backend accepts an optional `locale`, but the Android proxy request does not currently send one. The model may therefore produce English/default-locale output for users with other language settings.
- Allergen detection is explicitly limited to common US/Western allergens in the full-analysis prompt. Products and labeling conventions from other regions may be underrepresented.
- OCR and USDA coverage may work less reliably for packaging, scripts, or products outside the data these services support. The app surfaces OCR, lookup, confidence, and warning failures rather than silently treating them as verified facts.

These are coverage and accuracy limitations, not evidence of intentional harmful demographic treatment. They should be reconsidered before expanding supported languages or regions.

## Review Status

The current implementation passes this documentation review for the requested harmful-content check. This is not a statistical fairness evaluation and does not establish equal performance across languages, regions, or packaging formats.
