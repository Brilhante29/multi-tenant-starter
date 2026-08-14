#!/usr/bin/env python3
import json
import os
import re
import sys
from pathlib import Path


RESULT = Path(os.getenv("BENCHMARK_RESULT_PATH", "benchmarks/results/multi-tenant-starter-v2.json"))
REQUIRED_METRICS = {
    "tenant_onboarding_p50_ms",
    "tenant_onboarding_p95_ms",
    "isolated_query_p95_ms",
    "leakage_count",
    "failures_count",
}
DIGEST = re.compile(r"^sha256:[0-9a-f]{64}$")


def require(condition: bool, message: str) -> None:
    if not condition:
        raise ValueError(message)


def main() -> int:
    document = json.loads(RESULT.read_text(encoding="utf-8"))
    require(document["schema_version"] == 2, "schema_version must be 2")
    require(document["project"] == "multi-tenant-starter", "project mismatch")
    require(document["benchmark_id"] == "postgres-schema-isolation", "benchmark_id mismatch")
    require(document["execution"]["repeat"] >= 3, "at least three repetitions are required")
    require(document["execution"]["exit_code"] == 0, "benchmark exit code must be zero")
    require(document["workload"]["measured_iterations"] > 0, "measured workload must be positive")

    metrics = {metric["name"]: metric for metric in document["metrics"]}
    require(REQUIRED_METRICS == set(metrics), "metric set mismatch")
    for name, metric in metrics.items():
        require(len(metric["samples"]) >= 3, f"{name} requires at least three samples")
        require(metric["failures"] == 0, f"{name} reports failures")
    require(metrics["leakage_count"]["value"] == 0, "tenant leakage detected")
    require(metrics["failures_count"]["value"] == 0, "benchmark failures detected")

    provenance = document["provenance"]
    require(re.fullmatch(r"[0-9a-f]{40}", provenance["source_commit"]) is not None, "invalid source commit")
    require(provenance["clean_tree"] is True, "benchmark source tree was dirty")
    for field in ("image_digest", "dependency_lock_digest", "artifact_digest"):
        require(DIGEST.fullmatch(provenance[field]) is not None, f"invalid {field}")
    require(DIGEST.fullmatch(document["workload"]["fixture_digest"]) is not None, "invalid fixture digest")
    require(DIGEST.fullmatch(document["workload"]["config_digest"]) is not None, "invalid config digest")

    expected_commit = os.getenv("EXPECTED_SOURCE_COMMIT")
    if expected_commit:
        require(provenance["source_commit"] == expected_commit, "source commit does not match expected head")
    print("benchmark V2 validation passed")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except (KeyError, TypeError, ValueError, json.JSONDecodeError) as error:
        print(f"benchmark V2 validation failed: {error}", file=sys.stderr)
        raise SystemExit(1)
