package com.portfolio.multitenant.benchmark;

import com.portfolio.multitenant.application.TenantService;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class TenantBenchmark {

    private final TenantService tenantService;

    public TenantBenchmark(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    public BenchmarkResult run(int tenantCount) {
        if (tenantCount <= 0) {
            throw new IllegalArgumentException("tenantCount must be positive");
        }
        long start = System.nanoTime();

        for (int i = 0; i < tenantCount; i++) {
            tenantService.createTenant("benchmark-tenant-" + i);
        }

        long end = System.nanoTime();
        double elapsedSeconds = (end - start) / 1_000_000_000.0;
        double perTenantSeconds = elapsedSeconds / tenantCount;

        return new BenchmarkResult(
            "multi-tenant-starter",
            "tenant_onboarding_seconds",
            perTenantSeconds,
            "seconds",
            tenantCount,
            Instant.now().toString(),
            System.getProperty("os.name", "unknown"),
            System.getProperty("os.arch", "unknown"),
            Runtime.getRuntime().availableProcessors()
        );
    }
}
