"""Prompt loading and input assembly for the Zest backend proxy."""

from __future__ import annotations

import json
from pathlib import Path
from typing import Any

PROMPT_DIR = Path(__file__).resolve().parent / "prompts"

FULL_ANALYSIS_PROMPT = "food_label_full_analysis_prompt.md"
RESULT_CHAT_PROMPT = "food_label_result_chat_prompt.md"


def read_prompt(name: str) -> str:
    return (PROMPT_DIR / name).read_text(encoding="utf-8")


def compact_json(data: dict[str, Any] | list[dict[str, Any]]) -> str:
    return json.dumps(data, ensure_ascii=False, separators=(",", ":"))


def build_full_analysis_prompt(input_json: dict[str, Any]) -> str:
    return (
        read_prompt(FULL_ANALYSIS_PROMPT).strip()
        + "\n\n## Input JSON\n"
        + compact_json(input_json)
    )


def build_chat_prompt(
    result: dict[str, Any],
    question: str,
    history: list[dict[str, str]],
) -> str:
    return (
        read_prompt(RESULT_CHAT_PROMPT).strip()
        + "\n\n## Current scan result JSON\n"
        + compact_json(result)
        + "\n\n## Current chat session history JSON\n"
        + compact_json(history)
        + "\n\n## User question\n"
        + question.strip()
    )
