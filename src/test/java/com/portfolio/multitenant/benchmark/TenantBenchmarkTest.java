package com.portfolio.multitenant.benchmark;

import com.portfolio.multitenant.application.TenantService;
import com.portfolio.multitenant.domain.Tenant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantBenchmarkTest {

    @Mock
    private TenantService tenantService;

    private TenantBenchmark benchmark;

    @BeforeEach
    void setUp() {
        benchmark = new TenantBenchmark(tenantService);
    }

    @Test
    void shouldMeasureTenantOnboarding() {
        when(tenantService.createTenant(anyString())).thenReturn(
            new Tenant("id", "name", "schema", Tenant.Status.ACTIVE, Instant.now()));

        BenchmarkResult result = benchmark.run(50);

        assertEquals("multi-tenant-starter", result.project());
        assertEquals("tenant_onboarding_seconds", result.metric());
        assertTrue(result.value() >= 0, "Time should be non-negative");
        assertEquals("seconds", result.unit());
        assertEquals(50, result.iterations());
        assertNotNull(result.timestamp());
        assertNotNull(result.os());
        assertNotNull(result.arch());
        assertTrue(result.cpus() > 0);
    }

    @Test
    void benchmarkResultShouldProduceValidJson() {
        BenchmarkResult result = new BenchmarkResult(
            "multi-tenant-starter", "tenant_onboarding_seconds", 0.015,
            "seconds", 100, Instant.now().toString(),
            "Linux", "amd64", 4
        );

        String json = result.toJson();
        assertTrue(json.startsWith("{"));
        assertTrue(json.endsWith("}"));
        assertTrue(json.contains("\"project\":\"multi-tenant-starter\""));
        assertTrue(json.contains("\"metric\":\"tenant_onboarding_seconds\""));
        assertTrue(json.contains("\"value\":0.015"));
        assertTrue(json.contains("\"unit\":\"seconds\""));
        assertTrue(json.contains("\"iterations\":100"));
        assertTrue(json.contains("\"command\":\"docker run --rm multi-tenant-starter benchmark\""));
        assertTrue(json.contains("\"environment\":"));
    }

    @Test
    void shouldHandleEmptyIterations() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> benchmark.run(0));
        assertTrue(ex.getMessage().contains("positive"));
    }
}
