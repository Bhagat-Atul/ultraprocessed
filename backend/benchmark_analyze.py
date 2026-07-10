"""Measure deployed /analyze latency against a small fixed OCR corpus.

Usage:
    python benchmark_analyze.py https://example.run.app --iterations 3
"""

from __future__ import annotations

import argparse
import json
import statistics
import sys
import time
import urllib.error
import urllib.request


CORPUS = [
    {
        "product_name": "Simple oats",
        "ingredient_text": "whole grain oats",
    },
    {
        "product_name": "Chocolate cereal",
        "ingredient_text": (
            "whole grain oats, sugar, corn syrup, salt, natural flavor, soy lecithin, "
            "caramel color, vitamins and minerals"
        ),
    },
    {
        "product_name": "Noisy OCR snack",
        "ingredient_text": (
            "INGREDIENTS: wheat flour; sugar; vegetable oil (canola, palm); cocoa powder; "
            "soy lecithin. NUTRITION FACTS serving size calories barcode 123456"
        ),
    },
    {
        "product_name": "Allergen dense bar",
        "ingredient_text": (
            "almond flour, wheat flour, milk powder, egg white, sesame seed, soy protein isolate, "
            "peanut butter, natural flavor"
        ),
    },
    {
        "product_name": "Non-food text",
        "ingredient_text": "white painted wall, window frame, barcode 00881234, recycle symbol",
    },
]


def percentile(values: list[float], pct: float) -> float:
    if not values:
        return 0.0
    ordered = sorted(values)
    index = min(len(ordered) - 1, max(0, int(round((pct / 100) * len(ordered) + 0.5)) - 1))
    return ordered[index]


def post_json(url: str, payload: dict, timeout: float) -> tuple[int, str]:
    data = json.dumps(payload).encode("utf-8")
    request = urllib.request.Request(
        url,
        data=data,
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    try:
        with urllib.request.urlopen(request, timeout=timeout) as response:
            return response.status, response.read().decode("utf-8", errors="replace")
    except urllib.error.HTTPError as exc:
        return exc.code, exc.read().decode("utf-8", errors="replace")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("base_url", help="Cloud Run base URL or full /analyze URL")
    parser.add_argument("--iterations", type=int, default=3)
    parser.add_argument("--timeout", type=float, default=30.0)
    args = parser.parse_args()

    analyze_url = args.base_url.rstrip("/")
    if not analyze_url.endswith("/analyze"):
        analyze_url += "/analyze"

    latencies: list[float] = []
    errors: dict[str, int] = {}
    total = 0
    for _ in range(args.iterations):
        for sample in CORPUS:
            total += 1
            payload = {"type": "analysis", **sample}
            start = time.perf_counter()
            status, body = post_json(analyze_url, payload, args.timeout)
            elapsed_ms = (time.perf_counter() - start) * 1000
            if 200 <= status < 300:
                latencies.append(elapsed_ms)
            else:
                key = f"HTTP {status}"
                errors[key] = errors.get(key, 0) + 1
                print(f"{key}: {body[:300]}", file=sys.stderr)

    success = len(latencies)
    result = {
        "url": analyze_url,
        "totalRequests": total,
        "successfulRequests": success,
        "successRate": round(success / total, 4) if total else 0,
        "p50Ms": round(statistics.median(latencies), 2) if latencies else 0,
        "p95Ms": round(percentile(latencies, 95), 2),
        "p99Ms": round(percentile(latencies, 99), 2),
        "errors": errors,
    }
    print(json.dumps(result, indent=2))
    return 0 if not errors else 1


if __name__ == "__main__":
    raise SystemExit(main())
