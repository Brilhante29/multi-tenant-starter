package com.portfolio.multitenant.benchmark;

import java.util.List;
import java.util.Map;

public record BenchmarkResult(
    int schema_version,
    String run_id,
    String project,
    String benchmark_id,
    Workload workload,
    List<Metric> metrics,
    Execution execution,
    Environment environment,
    Provenance provenance,
    String comparability_key
) {
    public record Workload(
        String version,
        String fixture_digest,
        String config_digest,
        int warmup_iterations,
        int measured_iterations,
        int concurrency
    ) {}

    public record Metric(
        String name,
        double value,
        String unit,
        String direction,
        List<Double> samples,
        int failures,
        Map<String, Object> summary
    ) {}

    public record Execution(
        String command,
        String started_at,
        double duration_seconds,
        int exit_code,
        int repeat
    ) {}

    public record Environment(
        String runtime,
        String architecture,
        String hardware_class,
        int cpus,
        String postgres_version,
        String postgres_image_digest
    ) {}

    public record Provenance(
        String source_commit,
        boolean clean_tree,
        String image_ref,
        String image_digest,
        String dependency_lock_digest,
        String producer,
        String artifact_digest
    ) {}
}
