package com.portfolio.multitenant.benchmark;

import com.portfolio.multitenant.application.TenantDataService;
import com.portfolio.multitenant.application.TenantService;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class TenantBenchmarkTest {

    @Test
    void rejectsFewerThanThreeRepetitions() {
        TenantBenchmark benchmark = new TenantBenchmark(
            mock(TenantService.class),
            mock(TenantDataService.class),
            mock(JdbcTemplate.class)
        );

        assertThrows(IllegalArgumentException.class, () -> benchmark.run(0, 1, 1, 2));
    }
}
