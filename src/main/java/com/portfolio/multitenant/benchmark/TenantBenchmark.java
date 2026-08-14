package com.portfolio.multitenant.benchmark;

import com.portfolio.multitenant.application.TenantDataService;
import com.portfolio.multitenant.application.TenantService;
import com.portfolio.multitenant.domain.Tenant;
import com.portfolio.multitenant.domain.TenantRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class TenantBenchmark {

    private final TenantService tenants;
    private final TenantDataService records;
    private final JdbcTemplate jdbc;

    public TenantBenchmark(TenantService tenants, TenantDataService records, JdbcTemplate jdbc) {
        this.tenants = tenants;
        this.records = records;
        this.jdbc = jdbc;
    }

    public BenchmarkResult run(int warmupTenants, int tenantsPerRepetition, int queriesPerTenant, int repetitions) {
        if (warmupTenants < 0 || tenantsPerRepetition <= 0 || queriesPerTenant <= 0 || repetitions < 3) {
            throw new IllegalArgumentException("benchmark requires warmup >= 0, positive workload, and at least 3 repetitions");
        }

        Instant startedAt = Instant.now();
        String runId = UUID.randomUUID().toString();
        warmup(warmupTenants, queriesPerTenant, runId);

        List<Double> onboardingP50Samples = new ArrayList<>();
        List<Double> onboardingP95Samples = new ArrayList<>();
        List<Double> queryP95Samples = new ArrayList<>();
        List<Double> leakageSamples = new ArrayList<>();
        List<Double> failureSamples = new ArrayList<>();

        for (int repetition = 1; repetition <= repetitions; repetition++) {
            RepetitionResult result = runRepetition(
                tenantsPerRepetition,
                queriesPerTenant,
                runId + "-r" + repetition
            );
            onboardingP50Samples.add(result.onboardingP50Ms());
            onboardingP95Samples.add(result.onboardingP95Ms());
            queryP95Samples.add(result.queryP95Ms());
            leakageSamples.add((double) result.leakageCount());
            failureSamples.add((double) result.failureCount());
        }

        int failures = (int) failureSamples.stream().mapToDouble(Double::doubleValue).sum();
        int leakage = (int) leakageSamples.stream().mapToDouble(Double::doubleValue).sum();
        if (failures != 0 || leakage != 0) {
            throw new IllegalStateException(
                "benchmark invariants failed: failures=" + failures + ", leakage=" + leakage
            );
        }

        String config = "postgres17|schema-per-tenant|warmup=" + warmupTenants
            + "|tenants=" + tenantsPerRepetition + "|queries=" + queriesPerTenant
            + "|repeat=" + repetitions;
        String evidence = onboardingP50Samples + "|" + onboardingP95Samples + "|"
            + queryP95Samples + "|" + leakageSamples + "|" + failureSamples;
        String postgresVersion = jdbc.queryForObject("SHOW server_version", String.class);

        return new BenchmarkResult(
            2,
            runId,
            "multi-tenant-starter",
            "postgres-schema-isolation",
            new BenchmarkResult.Workload(
                "2.0.0",
                sha256("tenant-name-and-owner-record-fixture-v2"),
                sha256(config),
                warmupTenants,
                tenantsPerRepetition,
                1
            ),
            List.of(
                metric("tenant_onboarding_p50_ms", median(onboardingP50Samples), "ms/tenant",
                    "lower_is_better", onboardingP50Samples, failures, Map.of("statistic", "median_of_repetition_p50")),
                metric("tenant_onboarding_p95_ms", median(onboardingP95Samples), "ms/tenant",
                    "lower_is_better", onboardingP95Samples, failures, Map.of("statistic", "median_of_repetition_p95")),
                metric("isolated_query_p95_ms", median(queryP95Samples), "ms/query",
                    "lower_is_better", queryP95Samples, failures, Map.of("statistic", "median_of_repetition_p95")),
                metric("leakage_count", leakage, "count", "target", leakageSamples, failures, Map.of("target", 0)),
                metric("failures_count", failures, "count", "target", failureSamples, failures, Map.of("target", 0))
            ),
            new BenchmarkResult.Execution(
                "powershell -NoProfile -ExecutionPolicy Bypass -File tools/run-benchmark.ps1",
                startedAt.toString(),
                Duration.between(startedAt, Instant.now()).toNanos() / 1_000_000_000.0,
                0,
                repetitions
            ),
            new BenchmarkResult.Environment(
                "Java " + System.getProperty("java.version") + " / PostgreSQL " + postgresVersion,
                System.getProperty("os.arch", "unknown"),
                environment("HARDWARE_CLASS", "local-docker"),
                Runtime.getRuntime().availableProcessors(),
                postgresVersion,
                requiredDigest("POSTGRES_IMAGE_DIGEST")
            ),
            new BenchmarkResult.Provenance(
                requiredSha("SOURCE_COMMIT"),
                Boolean.parseBoolean(environment("CLEAN_TREE", "true")),
                "multi-tenant-starter:benchmark",
                requiredDigest("APP_IMAGE_DIGEST"),
                requiredDigest("DEPENDENCY_LOCK_DIGEST"),
                environment("BENCHMARK_PRODUCER", "local"),
                sha256(evidence)
            ),
            "multi-tenant-starter:postgres17:schema-per-tenant:t" + tenantsPerRepetition
                + ":q" + queriesPerTenant + ":repeat" + repetitions
        );
    }

    private void warmup(int tenantCount, int queriesPerTenant, String runId) {
        if (tenantCount == 0) {
            return;
        }
        RepetitionResult result = runRepetition(tenantCount, queriesPerTenant, runId + "-warmup");
        if (result.failureCount() != 0 || result.leakageCount() != 0) {
            throw new IllegalStateException("warmup invariants failed");
        }
    }

    private RepetitionResult runRepetition(int tenantCount, int queriesPerTenant, String prefix) {
        List<Tenant> created = new ArrayList<>();
        List<Double> onboardingMillis = new ArrayList<>();
        List<Double> queryMillis = new ArrayList<>();
        int failures = 0;
        int leakage = 0;

        try {
            for (int index = 0; index < tenantCount; index++) {
                long started = System.nanoTime();
                Tenant tenant = tenants.createTenant(prefix + "-tenant-" + index);
                onboardingMillis.add(millisSince(started));
                created.add(tenant);
                records.append(tenant.getId(), prefix + "-owner-" + index);
            }

            for (int index = 0; index < created.size(); index++) {
                Tenant tenant = created.get(index);
                String expectedPayload = prefix + "-owner-" + index;
                for (int query = 0; query < queriesPerTenant; query++) {
                    try {
                        long started = System.nanoTime();
                        List<TenantRecord> found = records.list(tenant.getId());
                        queryMillis.add(millisSince(started));
                        boolean isolated = found.size() == 1
                            && found.getFirst().tenantId().equals(tenant.getId())
                            && found.getFirst().payload().equals(expectedPayload);
                        if (!isolated) {
                            leakage++;
                        }
                    } catch (RuntimeException exception) {
                        failures++;
                    }
                }
            }
        } catch (RuntimeException exception) {
            failures++;
        } finally {
            for (Tenant tenant : created) {
                try {
                    tenants.deleteTenant(tenant.getId());
                } catch (RuntimeException exception) {
                    failures++;
                }
            }
        }

        return new RepetitionResult(
            percentile(onboardingMillis, 0.50),
            percentile(onboardingMillis, 0.95),
            percentile(queryMillis, 0.95),
            leakage,
            failures
        );
    }

    private static BenchmarkResult.Metric metric(
        String name,
        double value,
        String unit,
        String direction,
        List<Double> samples,
        int failures,
        Map<String, Object> summary
    ) {
        return new BenchmarkResult.Metric(name, value, unit, direction, samples, failures, summary);
    }

    private static double percentile(List<Double> values, double percentile) {
        if (values.isEmpty()) {
            return 0.0;
        }
        List<Double> sorted = values.stream().sorted().toList();
        int index = (int) Math.ceil(percentile * sorted.size()) - 1;
        return sorted.get(Math.max(0, index));
    }

    private static double median(List<Double> values) {
        return percentile(values, 0.50);
    }

    private static double millisSince(long started) {
        return (System.nanoTime() - started) / 1_000_000.0;
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
            return "sha256:" + HexFormat.of().formatHex(digest);
        } catch (Exception exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }

    private static String requiredSha(String name) {
        String value = environment(name, "0".repeat(40));
        if (!value.matches("^[0-9a-f]{40}$")) {
            throw new IllegalArgumentException(name + " must be a lowercase Git SHA");
        }
        return value;
    }

    private static String requiredDigest(String name) {
        String value = environment(name, "sha256:" + "0".repeat(64));
        if (!value.matches("^sha256:[0-9a-f]{64}$")) {
            throw new IllegalArgumentException(name + " must be a SHA-256 digest");
        }
        return value;
    }

    private static String environment(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }

    private record RepetitionResult(
        double onboardingP50Ms,
        double onboardingP95Ms,
        double queryP95Ms,
        int leakageCount,
        int failureCount
    ) {}
}
